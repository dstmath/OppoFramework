package com.android.internal.telephony.cat;

import android.annotation.UnsupportedAppUsage;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.LocaleList;
import android.os.Message;
import android.os.RemoteException;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.TelephonyComponentFactory;
import com.android.internal.telephony.cat.AppInterface;
import com.android.internal.telephony.cat.Duration;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IccRefreshResponse;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.uicc.UiccProfile;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Locale;

public class CatService extends Handler implements AppInterface {
    private static final boolean DBG = false;
    protected static final int DEV_ID_DISPLAY = 2;
    protected static final int DEV_ID_KEYPAD = 1;
    protected static final int DEV_ID_NETWORK = 131;
    protected static final int DEV_ID_TERMINAL = 130;
    protected static final int DEV_ID_UICC = 129;
    protected static final int MSG_ID_ALPHA_NOTIFY = 9;
    public static final int MSG_ID_CALL_SETUP = 4;
    public static final int MSG_ID_EVENT_NOTIFY = 3;
    protected static final int MSG_ID_ICC_CHANGED = 8;
    public static final int MSG_ID_ICC_RECORDS_LOADED = 20;
    public static final int MSG_ID_ICC_REFRESH = 30;
    public static final int MSG_ID_PROACTIVE_COMMAND = 2;
    public static final int MSG_ID_REFRESH = 5;
    public static final int MSG_ID_RESPONSE = 6;
    public static final int MSG_ID_RIL_MSG_DECODED = 10;
    public static final int MSG_ID_SESSION_END = 1;
    public static final int MSG_ID_SIM_READY = 7;
    public static final String STK_DEFAULT = "Default Message";
    protected static IccRecords mIccRecords;
    protected static UiccCardApplication mUiccApplication;
    @UnsupportedAppUsage
    protected static CatService[] sInstance = null;
    @UnsupportedAppUsage
    protected static final Object sInstanceLock = new Object();
    protected IccCardStatus.CardState mCardState = IccCardStatus.CardState.CARDSTATE_ABSENT;
    @UnsupportedAppUsage
    protected CommandsInterface mCmdIf;
    @UnsupportedAppUsage
    protected Context mContext;
    @UnsupportedAppUsage
    protected CatCmdMessage mCurrntCmd = null;
    @UnsupportedAppUsage
    protected CatCmdMessage mMenuCmd = null;
    @UnsupportedAppUsage
    protected RilMessageDecoder mMsgDecoder = null;
    @UnsupportedAppUsage
    protected int mSlotId;
    @UnsupportedAppUsage
    protected boolean mStkAppInstalled = false;
    @UnsupportedAppUsage
    protected UiccController mUiccController;

    public CatService(CommandsInterface ci, UiccCardApplication ca, IccRecords ir, Context context, IccFileHandler fh, UiccProfile uiccProfile, int slotId) {
        if (ci == null || ca == null || ir == null || context == null || fh == null || uiccProfile == null) {
            throw new NullPointerException("Service: Input parameters must not be null");
        }
        this.mCmdIf = ci;
        this.mContext = context;
        this.mSlotId = slotId;
        this.mMsgDecoder = RilMessageDecoder.getInstance(this, fh, slotId);
        RilMessageDecoder rilMessageDecoder = this.mMsgDecoder;
        if (rilMessageDecoder == null) {
            CatLog.d(this, "Null RilMessageDecoder instance");
            return;
        }
        rilMessageDecoder.start();
        this.mCmdIf.setOnCatSessionEnd(this, 1, null);
        this.mCmdIf.setOnCatProactiveCmd(this, 2, null);
        this.mCmdIf.setOnCatEvent(this, 3, null);
        this.mCmdIf.setOnCatCallSetUp(this, 4, null);
        this.mCmdIf.registerForIccRefresh(this, 30, null);
        this.mCmdIf.setOnCatCcAlphaNotify(this, 9, null);
        mIccRecords = ir;
        mUiccApplication = ca;
        mIccRecords.registerForRecordsLoaded(this, 20, null);
        CatLog.d(this, "registerForRecordsLoaded slotid=" + this.mSlotId + " instance:" + this);
        this.mUiccController = UiccController.getInstance();
        this.mUiccController.registerForIccChanged(this, 8, null);
        this.mStkAppInstalled = isStkAppInstalled();
        CatLog.d(this, "Running CAT service on Slotid: " + this.mSlotId + ". STK app installed:" + this.mStkAppInstalled);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0076, code lost:
        return null;
     */
    public static CatService getInstance(CommandsInterface ci, Context context, UiccProfile uiccProfile, int slotId) {
        IccFileHandler fh;
        IccRecords ir;
        UiccCardApplication ca;
        if (uiccProfile != null) {
            UiccCardApplication ca2 = uiccProfile.getApplicationIndex(0);
            if (ca2 != null) {
                ca = ca2;
                fh = ca2.getIccFileHandler();
                ir = ca2.getIccRecords();
            } else {
                ca = ca2;
                fh = null;
                ir = null;
            }
        } else {
            ca = null;
            fh = null;
            ir = null;
        }
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                int simCount = TelephonyManager.getDefault().getSimCount();
                sInstance = new CatService[simCount];
                for (int i = 0; i < simCount; i++) {
                    sInstance[i] = null;
                }
            }
            if (sInstance[slotId] == null) {
                if (!(ci == null || ca == null || ir == null || context == null || fh == null)) {
                    if (uiccProfile != null) {
                        sInstance[slotId] = TelephonyComponentFactory.getInstance().inject(TelephonyComponentFactory.class.getName()).makeCatService(ci, ca, ir, context, fh, uiccProfile, slotId);
                    }
                }
            } else if (!(ir == null || mIccRecords == ir)) {
                if (mIccRecords != null) {
                    mIccRecords.unregisterForRecordsLoaded(sInstance[slotId]);
                }
                mIccRecords = ir;
                mUiccApplication = ca;
                mIccRecords.registerForRecordsLoaded(sInstance[slotId], 20, null);
                CatLog.d(sInstance[slotId], "registerForRecordsLoaded slotid=" + slotId + " instance:" + sInstance[slotId]);
            }
            CatService catService = sInstance[slotId];
            return catService;
        }
    }

    @UnsupportedAppUsage
    public void dispose() {
        synchronized (sInstanceLock) {
            CatLog.d(this, "Disposing CatService object");
            mIccRecords.unregisterForRecordsLoaded(this);
            broadcastCardStateAndIccRefreshResp(IccCardStatus.CardState.CARDSTATE_ABSENT, null);
            this.mCmdIf.unSetOnCatSessionEnd(this);
            this.mCmdIf.unSetOnCatProactiveCmd(this);
            this.mCmdIf.unSetOnCatEvent(this);
            this.mCmdIf.unSetOnCatCallSetUp(this);
            this.mCmdIf.unSetOnCatCcAlphaNotify(this);
            this.mCmdIf.unregisterForIccRefresh(this);
            if (this.mUiccController != null) {
                this.mUiccController.unregisterForIccChanged(this);
                this.mUiccController = null;
            }
            this.mMsgDecoder.dispose();
            this.mMsgDecoder = null;
            removeCallbacksAndMessages(null);
            if (sInstance != null) {
                if (SubscriptionManager.isValidSlotIndex(this.mSlotId)) {
                    sInstance[this.mSlotId] = null;
                } else {
                    CatLog.d(this, "error: invaild slot id: " + this.mSlotId);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() {
        CatLog.d(this, "Service finalized");
    }

    /* access modifiers changed from: protected */
    public void handleRilMsg(RilMessage rilMsg) {
        CommandParams cmdParams;
        CommandParams cmdParams2;
        if (rilMsg != null) {
            int i = rilMsg.mId;
            if (i == 1) {
                handleSessionEnd();
            } else if (i == 2) {
                try {
                    CommandParams cmdParams3 = (CommandParams) rilMsg.mData;
                    if (cmdParams3 == null) {
                        return;
                    }
                    if (rilMsg.mResCode == ResultCode.OK) {
                        handleCommand(cmdParams3, true);
                    } else {
                        sendTerminalResponse(cmdParams3.mCmdDet, rilMsg.mResCode, false, 0, null);
                    }
                } catch (ClassCastException e) {
                    CatLog.d(this, "Fail to parse proactive command");
                    CatCmdMessage catCmdMessage = this.mCurrntCmd;
                    if (catCmdMessage != null) {
                        sendTerminalResponse(catCmdMessage.mCmdDet, ResultCode.CMD_DATA_NOT_UNDERSTOOD, false, 0, null);
                    }
                }
            } else if (i != 3) {
                if (i == 5 && (cmdParams2 = (CommandParams) rilMsg.mData) != null) {
                    handleCommand(cmdParams2, false);
                }
            } else if (rilMsg.mResCode == ResultCode.OK && (cmdParams = (CommandParams) rilMsg.mData) != null) {
                handleCommand(cmdParams, false);
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isSupportedSetupEventCommand(CatCmdMessage cmdMsg) {
        boolean flag = true;
        int[] iArr = cmdMsg.getSetEventList().eventList;
        for (int eventVal : iArr) {
            CatLog.d(this, "Event: " + eventVal);
            if (!(eventVal == 4 || eventVal == 5 || eventVal == 7)) {
                flag = false;
            }
        }
        return flag;
    }

    /* access modifiers changed from: protected */
    public void handleCommand(CommandParams cmdParams, boolean isProactiveCmd) {
        ResultCode resultCode;
        ResultCode resultCode2;
        ResultCode result;
        boolean noAlphaUsrCnf;
        UiccController uiccController;
        CatLog.d(this, cmdParams.getCommandType().name());
        if (isProactiveCmd && (uiccController = this.mUiccController) != null) {
            uiccController.addCardLog("ProactiveCommand mSlotId=" + this.mSlotId + " cmdParams=" + cmdParams);
        }
        CatCmdMessage cmdMsg = new CatCmdMessage(cmdParams);
        switch (cmdParams.getCommandType()) {
            case SET_UP_MENU:
                if (removeMenu(cmdMsg.getMenu())) {
                    this.mMenuCmd = null;
                } else {
                    this.mMenuCmd = cmdMsg;
                }
                if (cmdParams.mLoadIconFailed) {
                    resultCode = ResultCode.PRFRMD_ICON_NOT_DISPLAYED;
                } else {
                    resultCode = ResultCode.OK;
                }
                sendTerminalResponse(cmdParams.mCmdDet, resultCode, false, 0, null);
                break;
            case DISPLAY_TEXT:
            case SELECT_ITEM:
            case GET_INPUT:
            case GET_INKEY:
            case PLAY_TONE:
                break;
            case SET_UP_IDLE_MODE_TEXT:
                if (cmdParams.mLoadIconFailed) {
                    resultCode2 = ResultCode.PRFRMD_ICON_NOT_DISPLAYED;
                } else {
                    resultCode2 = ResultCode.OK;
                }
                sendTerminalResponse(cmdParams.mCmdDet, resultCode2, false, 0, null);
                break;
            case SET_UP_EVENT_LIST:
                if (!isSupportedSetupEventCommand(cmdMsg)) {
                    sendTerminalResponse(cmdParams.mCmdDet, ResultCode.BEYOND_TERMINAL_CAPABILITY, false, 0, null);
                    break;
                } else {
                    sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, null);
                    break;
                }
            case PROVIDE_LOCAL_INFORMATION:
                int i = cmdParams.mCmdDet.commandQualifier;
                if (i == 3) {
                    sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, new DTTZResponseData(null));
                    return;
                } else if (i != 4) {
                    sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, null);
                    return;
                } else {
                    sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, new LanguageResponseData(Locale.getDefault().getLanguage()));
                    return;
                }
            case LAUNCH_BROWSER:
                if (((LaunchBrowserParams) cmdParams).mConfirmMsg.text != null && ((LaunchBrowserParams) cmdParams).mConfirmMsg.text.equals(STK_DEFAULT)) {
                    ((LaunchBrowserParams) cmdParams).mConfirmMsg.text = this.mContext.getText(17040229).toString();
                    break;
                }
            case REFRESH:
            case RUN_AT:
                if (STK_DEFAULT.equals(((DisplayTextParams) cmdParams).mTextMsg.text)) {
                    ((DisplayTextParams) cmdParams).mTextMsg.text = null;
                    break;
                }
                break;
            case SEND_DTMF:
            case SEND_SMS:
            case SEND_SS:
            case SEND_USSD:
                if (((DisplayTextParams) cmdParams).mTextMsg.text != null && ((DisplayTextParams) cmdParams).mTextMsg.text.equals(STK_DEFAULT)) {
                    ((DisplayTextParams) cmdParams).mTextMsg.text = this.mContext.getText(17040988).toString();
                    break;
                }
            case SET_UP_CALL:
                if (((CallSetupParams) cmdParams).mConfirmMsg.text != null && ((CallSetupParams) cmdParams).mConfirmMsg.text.equals(STK_DEFAULT)) {
                    ((CallSetupParams) cmdParams).mConfirmMsg.text = this.mContext.getText(17039428).toString();
                    break;
                }
            case LANGUAGE_NOTIFICATION:
                String language = ((LanguageParams) cmdParams).mLanguage;
                ResultCode result2 = ResultCode.OK;
                if (language != null && language.length() > 0) {
                    try {
                        changeLanguage(language);
                    } catch (RemoteException e) {
                        result = ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS;
                    }
                }
                result = result2;
                sendTerminalResponse(cmdParams.mCmdDet, result, false, 0, null);
                return;
            case OPEN_CHANNEL:
            case CLOSE_CHANNEL:
            case RECEIVE_DATA:
            case SEND_DATA:
                BIPClientParams cmd = (BIPClientParams) cmdParams;
                try {
                    noAlphaUsrCnf = this.mContext.getResources().getBoolean(17891527);
                } catch (Resources.NotFoundException e2) {
                    noAlphaUsrCnf = false;
                }
                if (cmd.mTextMsg.text != null || (!cmd.mHasAlphaId && !noAlphaUsrCnf)) {
                    if (!this.mStkAppInstalled) {
                        CatLog.d(this, "No STK application found.");
                        if (isProactiveCmd) {
                            sendTerminalResponse(cmdParams.mCmdDet, ResultCode.BEYOND_TERMINAL_CAPABILITY, false, 0, null);
                            return;
                        }
                    }
                    if (isProactiveCmd && (cmdParams.getCommandType() == AppInterface.CommandType.CLOSE_CHANNEL || cmdParams.getCommandType() == AppInterface.CommandType.RECEIVE_DATA || cmdParams.getCommandType() == AppInterface.CommandType.SEND_DATA)) {
                        sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, null);
                        break;
                    }
                } else {
                    CatLog.d(this, "cmd " + cmdParams.getCommandType() + " with null alpha id");
                    if (isProactiveCmd) {
                        sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, null);
                        return;
                    } else if (cmdParams.getCommandType() == AppInterface.CommandType.OPEN_CHANNEL) {
                        this.mCmdIf.handleCallSetupRequestFromSim(true, null);
                        return;
                    } else {
                        return;
                    }
                }
                break;
            default:
                CatLog.d(this, "Unsupported command");
                return;
        }
        this.mCurrntCmd = cmdMsg;
        broadcastCatCmdIntent(cmdMsg);
    }

    /* access modifiers changed from: protected */
    public void broadcastCatCmdIntent(CatCmdMessage cmdMsg) {
        Intent intent = new Intent(AppInterface.CAT_CMD_ACTION);
        intent.putExtra("STK CMD", cmdMsg);
        intent.putExtra("SLOT_ID", this.mSlotId);
        intent.setComponent(AppInterface.getDefaultSTKApplication());
        CatLog.d(this, "Sending CmdMsg: " + cmdMsg + " on slotid:" + this.mSlotId);
        this.mContext.sendBroadcast(intent, AppInterface.STK_PERMISSION);
    }

    /* access modifiers changed from: protected */
    public void handleSessionEnd() {
        CatLog.d(this, "SESSION END on " + this.mSlotId);
        this.mCurrntCmd = this.mMenuCmd;
        Intent intent = new Intent(AppInterface.CAT_SESSION_END_ACTION);
        intent.putExtra("SLOT_ID", this.mSlotId);
        intent.setComponent(AppInterface.getDefaultSTKApplication());
        this.mContext.sendBroadcast(intent, AppInterface.STK_PERMISSION);
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void sendTerminalResponse(CommandDetails cmdDet, ResultCode resultCode, boolean includeAdditionalInfo, int additionalInfo, ResponseData resp) {
        if (cmdDet != null) {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            Input cmdInput = null;
            CatCmdMessage catCmdMessage = this.mCurrntCmd;
            if (catCmdMessage != null) {
                cmdInput = catCmdMessage.geInput();
            }
            int tag = ComprehensionTlvTag.COMMAND_DETAILS.value();
            if (cmdDet.compRequired) {
                tag |= 128;
            }
            buf.write(tag);
            buf.write(3);
            buf.write(cmdDet.commandNumber);
            buf.write(cmdDet.typeOfCommand);
            buf.write(cmdDet.commandQualifier);
            buf.write(ComprehensionTlvTag.DEVICE_IDENTITIES.value());
            int length = 2;
            buf.write(2);
            buf.write(130);
            buf.write(129);
            int tag2 = ComprehensionTlvTag.RESULT.value();
            if (cmdDet.compRequired) {
                tag2 |= 128;
            }
            buf.write(tag2);
            if (!includeAdditionalInfo) {
                length = 1;
            }
            buf.write(length);
            buf.write(resultCode.value());
            if (includeAdditionalInfo) {
                buf.write(additionalInfo);
            }
            if (resp != null) {
                resp.format(buf);
            } else {
                encodeOptionalTags(cmdDet, resultCode, cmdInput, buf);
            }
            this.mCmdIf.sendTerminalResponse(IccUtils.bytesToHexString(buf.toByteArray()), null);
            onSetResponsedFlag();
        }
    }

    /* access modifiers changed from: protected */
    public void encodeOptionalTags(CommandDetails cmdDet, ResultCode resultCode, Input cmdInput, ByteArrayOutputStream buf) {
        AppInterface.CommandType cmdType = AppInterface.CommandType.fromInt(cmdDet.typeOfCommand);
        if (cmdType != null) {
            int i = AnonymousClass1.$SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[cmdType.ordinal()];
            if (i != 5) {
                if (i != 8 && i != 9) {
                    CatLog.d(this, "encodeOptionalTags() Unsupported Cmd details=" + cmdDet);
                } else if (resultCode.value() == ResultCode.NO_RESPONSE_FROM_USER.value() && cmdInput != null && cmdInput.duration != null) {
                    getInKeyResponse(buf, cmdInput);
                }
            } else if (cmdDet.commandQualifier == 4 && resultCode.value() == ResultCode.OK.value()) {
                getPliResponse(buf);
            }
        } else {
            CatLog.d(this, "encodeOptionalTags() bad Cmd details=" + cmdDet);
        }
    }

    /* access modifiers changed from: protected */
    public void getInKeyResponse(ByteArrayOutputStream buf, Input cmdInput) {
        buf.write(ComprehensionTlvTag.DURATION.value());
        buf.write(2);
        Duration.TimeUnit timeUnit = cmdInput.duration.timeUnit;
        buf.write(Duration.TimeUnit.SECOND.value());
        buf.write(cmdInput.duration.timeInterval);
    }

    /* access modifiers changed from: protected */
    public void getPliResponse(ByteArrayOutputStream buf) {
        String lang = Locale.getDefault().getLanguage();
        if (lang != null) {
            buf.write(ComprehensionTlvTag.LANGUAGE.value());
            ResponseData.writeLength(buf, lang.length());
            buf.write(lang.getBytes(), 0, lang.length());
        }
    }

    /* access modifiers changed from: protected */
    public void sendMenuSelection(int menuId, boolean helpRequired) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        buf.write(211);
        buf.write(0);
        buf.write(ComprehensionTlvTag.DEVICE_IDENTITIES.value() | 128);
        buf.write(2);
        buf.write(1);
        buf.write(129);
        buf.write(ComprehensionTlvTag.ITEM_ID.value() | 128);
        buf.write(1);
        buf.write(menuId);
        if (helpRequired) {
            buf.write(ComprehensionTlvTag.HELP_REQUEST.value());
            buf.write(0);
        }
        byte[] rawData = buf.toByteArray();
        rawData[1] = (byte) (rawData.length - 2);
        this.mCmdIf.sendEnvelope(IccUtils.bytesToHexString(rawData), null);
    }

    /* access modifiers changed from: protected */
    public void eventDownload(int event, int sourceId, int destinationId, byte[] additionalInfo, boolean oneShot) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        buf.write(BerTlv.BER_EVENT_DOWNLOAD_TAG);
        buf.write(0);
        buf.write(ComprehensionTlvTag.EVENT_LIST.value() | 128);
        buf.write(1);
        buf.write(event);
        buf.write(ComprehensionTlvTag.DEVICE_IDENTITIES.value() | 128);
        buf.write(2);
        buf.write(sourceId);
        buf.write(destinationId);
        if (event != 4) {
            if (event == 5) {
                CatLog.d(sInstance, " Sending Idle Screen Available event download to ICC");
            } else if (event == 7) {
                CatLog.d(sInstance, " Sending Language Selection event download to ICC");
                buf.write(ComprehensionTlvTag.LANGUAGE.value() | 128);
                buf.write(2);
            }
        }
        if (additionalInfo != null) {
            for (byte b : additionalInfo) {
                buf.write(b);
            }
        }
        byte[] rawData = buf.toByteArray();
        rawData[1] = (byte) (rawData.length - 2);
        String hexString = IccUtils.bytesToHexString(rawData);
        CatLog.d(this, "ENVELOPE COMMAND: " + hexString);
        this.mCmdIf.sendEnvelope(hexString, null);
    }

    public static AppInterface getInstance() {
        int slotId = 0;
        SubscriptionController sControl = SubscriptionController.getInstance();
        if (sControl != null) {
            slotId = sControl.getSlotIndex(sControl.getDefaultSubId());
        }
        return getInstance(null, null, null, slotId);
    }

    public static AppInterface getInstance(int slotId) {
        return getInstance(null, null, null, slotId);
    }

    public void handleMessage(Message msg) {
        AsyncResult ar;
        CatLog.d(this, "handleMessage[" + msg.what + "]");
        int i = msg.what;
        if (i == 20) {
            return;
        }
        if (i != 30) {
            switch (i) {
                case 1:
                case 2:
                case 3:
                case 5:
                    CatLog.d(this, "ril message arrived,slotid:" + this.mSlotId);
                    String data = null;
                    if (!(msg.obj == null || (ar = (AsyncResult) msg.obj) == null || ar.result == null)) {
                        try {
                            data = (String) ar.result;
                        } catch (ClassCastException e) {
                            return;
                        }
                    }
                    this.mMsgDecoder.sendStartDecodingMessageParams(new RilMessage(msg.what, data));
                    return;
                case 4:
                    this.mMsgDecoder.sendStartDecodingMessageParams(new RilMessage(msg.what, null));
                    return;
                case 6:
                    handleCmdResponse((CatResponseMessage) msg.obj);
                    return;
                default:
                    switch (i) {
                        case 8:
                            CatLog.d(this, "MSG_ID_ICC_CHANGED");
                            updateIccAvailability();
                            return;
                        case 9:
                            CatLog.d(this, "Received CAT CC Alpha message from card");
                            if (msg.obj != null) {
                                AsyncResult ar2 = (AsyncResult) msg.obj;
                                if (ar2 == null || ar2.result == null) {
                                    CatLog.d(this, "CAT Alpha message: ar.result is null");
                                    return;
                                } else {
                                    broadcastAlphaMessage((String) ar2.result);
                                    return;
                                }
                            } else {
                                CatLog.d(this, "CAT Alpha message: msg.obj is null");
                                return;
                            }
                        case 10:
                            handleRilMsg((RilMessage) msg.obj);
                            return;
                        default:
                            throw new AssertionError("Unrecognized CAT command: " + msg.what);
                    }
            }
        } else if (msg.obj != null) {
            AsyncResult ar3 = (AsyncResult) msg.obj;
            if (ar3 == null || ar3.result == null) {
                CatLog.d(this, "Icc REFRESH with exception: " + ar3.exception);
                return;
            }
            broadcastCardStateAndIccRefreshResp(IccCardStatus.CardState.CARDSTATE_PRESENT, (IccRefreshResponse) ar3.result);
        } else {
            CatLog.d(this, "IccRefresh Message is null");
        }
    }

    /* access modifiers changed from: protected */
    public void broadcastCardStateAndIccRefreshResp(IccCardStatus.CardState cardState, IccRefreshResponse iccRefreshState) {
        Intent intent = new Intent(AppInterface.CAT_ICC_STATUS_CHANGE);
        intent.addFlags(268435456);
        boolean cardPresent = cardState == IccCardStatus.CardState.CARDSTATE_PRESENT;
        if (iccRefreshState != null) {
            intent.putExtra(AppInterface.REFRESH_RESULT, iccRefreshState.refreshResult);
            CatLog.d(this, "Sending IccResult with Result: " + iccRefreshState.refreshResult);
        }
        intent.putExtra(AppInterface.CARD_STATUS, cardPresent);
        intent.setComponent(AppInterface.getDefaultSTKApplication());
        intent.putExtra("SLOT_ID", this.mSlotId);
        CatLog.d(this, "Sending Card Status: " + cardState + " cardPresent: " + cardPresent + "SLOT_ID: " + this.mSlotId);
        this.mContext.sendBroadcast(intent, AppInterface.STK_PERMISSION);
    }

    /* access modifiers changed from: protected */
    public void broadcastAlphaMessage(String alphaString) {
        CatLog.d(this, "Broadcasting CAT Alpha message from card: " + alphaString);
        Intent intent = new Intent(AppInterface.CAT_ALPHA_NOTIFY_ACTION);
        intent.addFlags(268435456);
        intent.putExtra(AppInterface.ALPHA_STRING, alphaString);
        intent.putExtra("SLOT_ID", this.mSlotId);
        intent.setComponent(AppInterface.getDefaultSTKApplication());
        this.mContext.sendBroadcast(intent, AppInterface.STK_PERMISSION);
    }

    @Override // com.android.internal.telephony.cat.AppInterface
    public synchronized void onCmdResponse(CatResponseMessage resMsg) {
        if (resMsg != null) {
            obtainMessage(6, resMsg).sendToTarget();
        }
    }

    /* access modifiers changed from: protected */
    public boolean validateResponse(CatResponseMessage resMsg) {
        if (resMsg.mCmdDet.typeOfCommand == AppInterface.CommandType.SET_UP_EVENT_LIST.value() || resMsg.mCmdDet.typeOfCommand == AppInterface.CommandType.SET_UP_MENU.value()) {
            CatLog.d(this, "CmdType: " + resMsg.mCmdDet.typeOfCommand);
            return true;
        } else if (this.mCurrntCmd == null) {
            return false;
        } else {
            boolean validResponse = resMsg.mCmdDet.compareTo(this.mCurrntCmd.mCmdDet);
            CatLog.d(this, "isResponse for last valid cmd: " + validResponse);
            return validResponse;
        }
    }

    /* access modifiers changed from: protected */
    public boolean removeMenu(Menu menu) {
        try {
            return menu.items.size() == 1 && menu.items.get(0) == null;
        } catch (NullPointerException e) {
            CatLog.d(this, "Unable to get Menu's items size");
            return true;
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0059  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00ee  */
    public void handleCmdResponse(CatResponseMessage resMsg) {
        boolean helpRequired;
        int i;
        if (validateResponse(resMsg)) {
            ResponseData resp = null;
            CommandDetails cmdDet = resMsg.getCmdDetails();
            AppInterface.CommandType type = AppInterface.CommandType.fromInt(cmdDet.typeOfCommand);
            switch (resMsg.mResCode) {
                case HELP_INFO_REQUIRED:
                    helpRequired = true;
                    i = AnonymousClass1.$SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[type.ordinal()];
                    boolean helpRequired2 = true;
                    if (i == 1) {
                        if (i != 2) {
                            if (i != 4) {
                                if (i != 17 && i != 19) {
                                    switch (i) {
                                        case 6:
                                            if (resMsg.mResCode != ResultCode.LAUNCH_BROWSER_ERROR) {
                                                resMsg.mIncludeAdditionalInfo = false;
                                                resMsg.mAdditionalInfo = 0;
                                                break;
                                            } else {
                                                resMsg.setAdditionalInfo(4);
                                                break;
                                            }
                                        case 7:
                                            resp = new SelectItemResponseData(resMsg.mUsersMenuSelection);
                                            break;
                                        case 8:
                                        case 9:
                                            Input input = this.mCurrntCmd.geInput();
                                            if (!input.yesNo) {
                                                if (!helpRequired) {
                                                    resp = new GetInkeyInputResponseData(resMsg.mUsersInput, input.ucs2, input.packed);
                                                    break;
                                                }
                                            } else {
                                                resp = new GetInkeyInputResponseData(resMsg.mUsersYesNoSelection);
                                                break;
                                            }
                                            break;
                                    }
                                } else {
                                    this.mCmdIf.handleCallSetupRequestFromSim(resMsg.mUsersConfirm, null);
                                    this.mCurrntCmd = null;
                                    return;
                                }
                            } else if (5 == resMsg.mEventValue) {
                                eventDownload(resMsg.mEventValue, 2, 129, resMsg.mAddedInfo, false);
                                return;
                            } else {
                                eventDownload(resMsg.mEventValue, 130, 129, resMsg.mAddedInfo, false);
                                return;
                            }
                        } else if (resMsg.mResCode == ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS) {
                            resMsg.setAdditionalInfo(1);
                        } else {
                            resMsg.mIncludeAdditionalInfo = false;
                            resMsg.mAdditionalInfo = 0;
                        }
                        sendTerminalResponse(cmdDet, resMsg.mResCode, resMsg.mIncludeAdditionalInfo, resMsg.mAdditionalInfo, resp);
                        this.mCurrntCmd = null;
                        return;
                    }
                    if (resMsg.mResCode != ResultCode.HELP_INFO_REQUIRED) {
                        helpRequired2 = false;
                    }
                    sendMenuSelection(resMsg.mUsersMenuSelection, helpRequired2);
                    return;
                case OK:
                case PRFRMD_WITH_PARTIAL_COMPREHENSION:
                case PRFRMD_WITH_MISSING_INFO:
                case PRFRMD_WITH_ADDITIONAL_EFS_READ:
                case PRFRMD_ICON_NOT_DISPLAYED:
                case PRFRMD_MODIFIED_BY_NAA:
                case PRFRMD_LIMITED_SERVICE:
                case PRFRMD_WITH_MODIFICATION:
                case PRFRMD_NAA_NOT_ACTIVE:
                case PRFRMD_TONE_NOT_PLAYED:
                case LAUNCH_BROWSER_ERROR:
                case TERMINAL_CRNTLY_UNABLE_TO_PROCESS:
                    helpRequired = false;
                    i = AnonymousClass1.$SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[type.ordinal()];
                    boolean helpRequired22 = true;
                    if (i == 1) {
                    }
                    break;
                case BACKWARD_MOVE_BY_USER:
                case USER_NOT_ACCEPT:
                    if (type == AppInterface.CommandType.SET_UP_CALL || type == AppInterface.CommandType.OPEN_CHANNEL) {
                        this.mCmdIf.handleCallSetupRequestFromSim(false, null);
                        this.mCurrntCmd = null;
                        return;
                    }
                    resp = null;
                    sendTerminalResponse(cmdDet, resMsg.mResCode, resMsg.mIncludeAdditionalInfo, resMsg.mAdditionalInfo, resp);
                    this.mCurrntCmd = null;
                    return;
                case NO_RESPONSE_FROM_USER:
                    if (type == AppInterface.CommandType.SET_UP_CALL) {
                        this.mCmdIf.handleCallSetupRequestFromSim(false, null);
                        this.mCurrntCmd = null;
                        return;
                    }
                    resp = null;
                    sendTerminalResponse(cmdDet, resMsg.mResCode, resMsg.mIncludeAdditionalInfo, resMsg.mAdditionalInfo, resp);
                    this.mCurrntCmd = null;
                    return;
                case UICC_SESSION_TERM_BY_USER:
                    resp = null;
                    sendTerminalResponse(cmdDet, resMsg.mResCode, resMsg.mIncludeAdditionalInfo, resMsg.mAdditionalInfo, resp);
                    this.mCurrntCmd = null;
                    return;
                default:
                    return;
            }
        }
    }

    @UnsupportedAppUsage
    private boolean isStkAppInstalled() {
        List<ResolveInfo> broadcastReceivers = this.mContext.getPackageManager().queryBroadcastReceivers(new Intent(AppInterface.CAT_CMD_ACTION), 128);
        if ((broadcastReceivers == null ? 0 : broadcastReceivers.size()) > 0) {
            return true;
        }
        return false;
    }

    public void update(CommandsInterface ci, Context context, UiccProfile uiccProfile) {
        UiccCardApplication ca = null;
        IccRecords ir = null;
        if (!(uiccProfile == null || (ca = uiccProfile.getApplicationIndex(0)) == null)) {
            ir = ca.getIccRecords();
        }
        synchronized (sInstanceLock) {
            if (ir != null) {
                if (mIccRecords != ir) {
                    if (mIccRecords != null) {
                        mIccRecords.unregisterForRecordsLoaded(this);
                    }
                    CatLog.d(this, "Reinitialize the Service with SIMRecords and UiccCardApplication");
                    mIccRecords = ir;
                    mUiccApplication = ca;
                    mIccRecords.registerForRecordsLoaded(this, 20, null);
                    CatLog.d(this, "registerForRecordsLoaded slotid=" + this.mSlotId + " instance:" + this);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updateIccAvailability() {
        if (this.mUiccController != null) {
            IccCardStatus.CardState newState = IccCardStatus.CardState.CARDSTATE_ABSENT;
            UiccCard newCard = this.mUiccController.getUiccCard(this.mSlotId);
            if (newCard != null) {
                newState = newCard.getCardState();
            }
            IccCardStatus.CardState oldState = this.mCardState;
            this.mCardState = newState;
            CatLog.d(this, "New Card State = " + newState + " Old Card State = " + oldState);
            if (oldState == IccCardStatus.CardState.CARDSTATE_PRESENT && newState != IccCardStatus.CardState.CARDSTATE_PRESENT) {
                broadcastCardStateAndIccRefreshResp(newState, null);
            } else if (oldState != IccCardStatus.CardState.CARDSTATE_PRESENT && newState == IccCardStatus.CardState.CARDSTATE_PRESENT) {
                this.mCmdIf.reportStkServiceIsRunning(null);
            }
        }
    }

    private void changeLanguage(String language) throws RemoteException {
        IActivityManager am = ActivityManagerNative.getDefault();
        Configuration config = am.getConfiguration();
        config.setLocales(new LocaleList(new Locale(language), LocaleList.getDefault()));
        config.userSetLocale = true;
        am.updatePersistentConfiguration(config);
        BackupManager.dataChanged("com.android.providers.settings");
    }

    /* access modifiers changed from: protected */
    public void onSetResponsedFlag() {
    }
}
