package com.android.internal.telephony.cat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources.NotFoundException;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.cat.AppInterface.CommandType;
import com.android.internal.telephony.cat.Duration.TimeUnit;
import com.android.internal.telephony.uicc.IccCardStatus.CardState;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IccRefreshResponse;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Locale;

public class CatService extends Handler implements AppInterface {
    /* renamed from: -com-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues */
    private static final /* synthetic */ int[] f28x72eb89a2 = null;
    /* renamed from: -com-android-internal-telephony-cat-ResultCodeSwitchesValues */
    private static final /* synthetic */ int[] f29-com-android-internal-telephony-cat-ResultCodeSwitchesValues = null;
    private static final boolean DBG = false;
    private static final int DEV_ID_DISPLAY = 2;
    private static final int DEV_ID_KEYPAD = 1;
    private static final int DEV_ID_NETWORK = 131;
    private static final int DEV_ID_TERMINAL = 130;
    private static final int DEV_ID_UICC = 129;
    protected static final int MSG_ID_ALPHA_NOTIFY = 9;
    protected static final int MSG_ID_CALL_SETUP = 4;
    protected static final int MSG_ID_EVENT_NOTIFY = 3;
    protected static final int MSG_ID_ICC_CHANGED = 8;
    private static final int MSG_ID_ICC_RECORDS_LOADED = 20;
    private static final int MSG_ID_ICC_REFRESH = 30;
    protected static final int MSG_ID_PROACTIVE_COMMAND = 2;
    static final int MSG_ID_REFRESH = 5;
    static final int MSG_ID_RESPONSE = 6;
    static final int MSG_ID_RIL_MSG_DECODED = 10;
    protected static final int MSG_ID_SESSION_END = 1;
    static final int MSG_ID_SIM_READY = 7;
    static final String STK_DEFAULT = "Default Message";
    private static IccRecords mIccRecords;
    private static UiccCardApplication mUiccApplication;
    private static CatCmdMessage[] sCurrntCmd = null;
    private static CatService[] sInstance = null;
    private static final Object sInstanceLock = new Object();
    private CardState mCardState = CardState.CARDSTATE_ABSENT;
    private CommandsInterface mCmdIf;
    private Context mContext;
    private CatCmdMessage mCurrntCmd = null;
    private HandlerThread mHandlerThread;
    private CatCmdMessage mMenuCmd = null;
    private RilMessageDecoder mMsgDecoder = null;
    private int mSlotId;
    private boolean mStkAppInstalled = false;
    private UiccController mUiccController;

    /* renamed from: -getcom-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues */
    private static /* synthetic */ int[] m29xe796fd46() {
        if (f28x72eb89a2 != null) {
            return f28x72eb89a2;
        }
        int[] iArr = new int[CommandType.values().length];
        try {
            iArr[CommandType.ACTIVATE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[CommandType.CLOSE_CHANNEL.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[CommandType.DISPLAY_TEXT.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[CommandType.GET_CHANNEL_STATUS.ordinal()] = 39;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[CommandType.GET_INKEY.ordinal()] = 4;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[CommandType.GET_INPUT.ordinal()] = 5;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[CommandType.LAUNCH_BROWSER.ordinal()] = 6;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[CommandType.OPEN_CHANNEL.ordinal()] = 7;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[CommandType.PLAY_TONE.ordinal()] = 8;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[CommandType.PROVIDE_LOCAL_INFORMATION.ordinal()] = 9;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[CommandType.RECEIVE_DATA.ordinal()] = 10;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[CommandType.REFRESH.ordinal()] = 11;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[CommandType.SELECT_ITEM.ordinal()] = 12;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[CommandType.SEND_DATA.ordinal()] = 13;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[CommandType.SEND_DTMF.ordinal()] = 14;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[CommandType.SEND_SMS.ordinal()] = 15;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[CommandType.SEND_SS.ordinal()] = 16;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[CommandType.SEND_USSD.ordinal()] = 17;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[CommandType.SET_UP_CALL.ordinal()] = 18;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[CommandType.SET_UP_EVENT_LIST.ordinal()] = 19;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[CommandType.SET_UP_IDLE_MODE_TEXT.ordinal()] = 20;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[CommandType.SET_UP_MENU.ordinal()] = 21;
        } catch (NoSuchFieldError e22) {
        }
        f28x72eb89a2 = iArr;
        return iArr;
    }

    /* renamed from: -getcom-android-internal-telephony-cat-ResultCodeSwitchesValues */
    private static /* synthetic */ int[] m30-getcom-android-internal-telephony-cat-ResultCodeSwitchesValues() {
        if (f29-com-android-internal-telephony-cat-ResultCodeSwitchesValues != null) {
            return f29-com-android-internal-telephony-cat-ResultCodeSwitchesValues;
        }
        int[] iArr = new int[ResultCode.values().length];
        try {
            iArr[ResultCode.ACCESS_TECH_UNABLE_TO_PROCESS.ordinal()] = 39;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ResultCode.BACKWARD_MOVE_BY_USER.ordinal()] = 1;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ResultCode.BEYOND_TERMINAL_CAPABILITY.ordinal()] = 40;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ResultCode.BIP_ERROR.ordinal()] = 41;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ResultCode.CMD_DATA_NOT_UNDERSTOOD.ordinal()] = 42;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ResultCode.CMD_NUM_NOT_KNOWN.ordinal()] = 43;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ResultCode.CMD_TYPE_NOT_UNDERSTOOD.ordinal()] = 44;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[ResultCode.CONTRADICTION_WITH_TIMER.ordinal()] = 45;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[ResultCode.FRAMES_ERROR.ordinal()] = 46;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[ResultCode.HELP_INFO_REQUIRED.ordinal()] = 2;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[ResultCode.LAUNCH_BROWSER_ERROR.ordinal()] = 3;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[ResultCode.MMS_ERROR.ordinal()] = 47;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[ResultCode.MMS_TEMPORARY.ordinal()] = 48;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[ResultCode.MULTI_CARDS_CMD_ERROR.ordinal()] = 49;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[ResultCode.NAA_CALL_CONTROL_TEMPORARY.ordinal()] = 50;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[ResultCode.NETWORK_CRNTLY_UNABLE_TO_PROCESS.ordinal()] = 51;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[ResultCode.NO_RESPONSE_FROM_USER.ordinal()] = 4;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[ResultCode.OK.ordinal()] = 5;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[ResultCode.PRFRMD_ICON_NOT_DISPLAYED.ordinal()] = 6;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[ResultCode.PRFRMD_LIMITED_SERVICE.ordinal()] = 7;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[ResultCode.PRFRMD_MODIFIED_BY_NAA.ordinal()] = 8;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[ResultCode.PRFRMD_NAA_NOT_ACTIVE.ordinal()] = 9;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[ResultCode.PRFRMD_TONE_NOT_PLAYED.ordinal()] = 10;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[ResultCode.PRFRMD_WITH_ADDITIONAL_EFS_READ.ordinal()] = 11;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[ResultCode.PRFRMD_WITH_MISSING_INFO.ordinal()] = 12;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[ResultCode.PRFRMD_WITH_MODIFICATION.ordinal()] = 13;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[ResultCode.PRFRMD_WITH_PARTIAL_COMPREHENSION.ordinal()] = 14;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[ResultCode.REQUIRED_VALUES_MISSING.ordinal()] = 52;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[ResultCode.SMS_RP_ERROR.ordinal()] = 53;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[ResultCode.SS_RETURN_ERROR.ordinal()] = 54;
        } catch (NoSuchFieldError e30) {
        }
        try {
            iArr[ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS.ordinal()] = 15;
        } catch (NoSuchFieldError e31) {
        }
        try {
            iArr[ResultCode.UICC_SESSION_TERM_BY_USER.ordinal()] = 16;
        } catch (NoSuchFieldError e32) {
        }
        try {
            iArr[ResultCode.USER_CLEAR_DOWN_CALL.ordinal()] = 55;
        } catch (NoSuchFieldError e33) {
        }
        try {
            iArr[ResultCode.USER_NOT_ACCEPT.ordinal()] = 17;
        } catch (NoSuchFieldError e34) {
        }
        try {
            iArr[ResultCode.USIM_CALL_CONTROL_PERMANENT.ordinal()] = 56;
        } catch (NoSuchFieldError e35) {
        }
        try {
            iArr[ResultCode.USSD_RETURN_ERROR.ordinal()] = 57;
        } catch (NoSuchFieldError e36) {
        }
        try {
            iArr[ResultCode.USSD_SS_SESSION_TERM_BY_USER.ordinal()] = 58;
        } catch (NoSuchFieldError e37) {
        }
        f29-com-android-internal-telephony-cat-ResultCodeSwitchesValues = iArr;
        return iArr;
    }

    private CatService(CommandsInterface ci, UiccCardApplication ca, IccRecords ir, Context context, IccFileHandler fh, UiccCard ic, int slotId) {
        if (ci == null || ca == null || ir == null || context == null || fh == null || ic == null) {
            throw new NullPointerException("Service: Input parameters must not be null");
        }
        this.mCmdIf = ci;
        this.mContext = context;
        this.mSlotId = slotId;
        this.mHandlerThread = new HandlerThread("Cat Telephony service" + slotId);
        this.mHandlerThread.start();
        this.mMsgDecoder = RilMessageDecoder.getInstance(this, fh, slotId);
        if (this.mMsgDecoder == null) {
            CatLog.d((Object) this, "Null RilMessageDecoder instance");
            return;
        }
        this.mMsgDecoder.start();
        this.mCmdIf.setOnCatSessionEnd(this, 1, null);
        this.mCmdIf.setOnCatProactiveCmd(this, 2, null);
        this.mCmdIf.setOnCatEvent(this, 3, null);
        this.mCmdIf.setOnCatCallSetUp(this, 4, null);
        this.mCmdIf.registerForIccRefresh(this, 30, null);
        this.mCmdIf.setOnCatCcAlphaNotify(this, 9, null);
        mIccRecords = ir;
        mUiccApplication = ca;
        mIccRecords.registerForRecordsLoaded(this, 20, null);
        CatLog.d((Object) this, "registerForRecordsLoaded slotid=" + this.mSlotId + " instance:" + this);
        this.mUiccController = UiccController.getInstance();
        this.mUiccController.registerForIccChanged(this, 8, null);
        this.mStkAppInstalled = isStkAppInstalled();
        if (sCurrntCmd == null) {
            int simCount = TelephonyManager.getDefault().getSimCount();
            sCurrntCmd = new CatCmdMessage[simCount];
            for (int i = 0; i < simCount; i++) {
                sCurrntCmd[i] = null;
            }
        }
        CatLog.d((Object) this, "Running CAT service on Slotid: " + this.mSlotId + ". STK app installed:" + this.mStkAppInstalled);
    }

    /* JADX WARNING: Missing block: B:18:0x003e, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static CatService getInstance(CommandsInterface ci, Context context, UiccCard ic, int slotId) {
        UiccCardApplication ca = null;
        IccFileHandler fh = null;
        IccRecords ir = null;
        if (ic != null) {
            ca = ic.getApplicationIndex(0);
            if (ca != null) {
                fh = ca.getIccFileHandler();
                ir = ca.getIccRecords();
            }
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
                if (ci == null || ca == null || ir == null || context == null || fh == null || ic == null) {
                } else {
                    sInstance[slotId] = new CatService(ci, ca, ir, context, fh, ic, slotId);
                }
            } else if (ir != null) {
                if (mIccRecords != ir) {
                    if (mIccRecords != null) {
                        mIccRecords.unregisterForRecordsLoaded(sInstance[slotId]);
                    }
                    mIccRecords = ir;
                    mUiccApplication = ca;
                    mIccRecords.registerForRecordsLoaded(sInstance[slotId], 20, null);
                    CatLog.d(sInstance[slotId], "registerForRecordsLoaded slotid=" + slotId + " instance:" + sInstance[slotId]);
                }
            }
            CatService catService = sInstance[slotId];
            return catService;
        }
    }

    public void dispose() {
        synchronized (sInstanceLock) {
            CatLog.d((Object) this, "Disposing CatService object");
            mIccRecords.unregisterForRecordsLoaded(this);
            broadcastCardStateAndIccRefreshResp(CardState.CARDSTATE_ABSENT, null);
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
            this.mHandlerThread.quit();
            this.mHandlerThread = null;
            removeCallbacksAndMessages(null);
            if (sInstance != null) {
                if (SubscriptionManager.isValidSlotIndex(this.mSlotId)) {
                    sInstance[this.mSlotId] = null;
                } else {
                    CatLog.d((Object) this, "error: invaild slot id: " + this.mSlotId);
                }
            }
        }
    }

    protected void finalize() {
        CatLog.d((Object) this, "Service finalized");
    }

    private void handleRilMsg(RilMessage rilMsg) {
        if (rilMsg != null) {
            CommandParams cmdParams;
            switch (rilMsg.mId) {
                case 1:
                    handleSessionEnd();
                    break;
                case 2:
                    try {
                        cmdParams = (CommandParams) rilMsg.mData;
                        if (cmdParams != null) {
                            if (rilMsg.mResCode != ResultCode.OK) {
                                sendTerminalResponse(cmdParams.mCmdDet, rilMsg.mResCode, false, 0, null);
                                break;
                            }
                            handleCommand(cmdParams, true);
                            break;
                        }
                    } catch (ClassCastException e) {
                        CatLog.d((Object) this, "Fail to parse proactive command");
                        if (this.mCurrntCmd != null) {
                            sendTerminalResponse(this.mCurrntCmd.mCmdDet, ResultCode.CMD_DATA_NOT_UNDERSTOOD, false, 0, null);
                            break;
                        }
                    }
                    break;
                case 3:
                    if (rilMsg.mResCode == ResultCode.OK) {
                        cmdParams = rilMsg.mData;
                        if (cmdParams != null) {
                            handleCommand(cmdParams, false);
                            break;
                        }
                    }
                    break;
                case 5:
                    cmdParams = rilMsg.mData;
                    if (cmdParams != null) {
                        handleCommand(cmdParams, false);
                        break;
                    }
                    break;
            }
        }
    }

    private boolean isSupportedSetupEventCommand(CatCmdMessage cmdMsg) {
        boolean flag = true;
        for (int eventVal : cmdMsg.getSetEventList().eventList) {
            CatLog.d((Object) this, "Event: " + eventVal);
            switch (eventVal) {
                case 5:
                case 7:
                case 19:
                    break;
                default:
                    flag = false;
                    break;
            }
        }
        return flag;
    }

    /* JADX WARNING: Missing block: B:29:0x00dd, code:
            if (r23.equals("Out of variable memory") != false) goto L_0x00df;
     */
    /* JADX WARNING: Missing block: B:35:0x00fd, code:
            if (r22.startsWith("405") == false) goto L_0x00ff;
     */
    /* JADX WARNING: Missing block: B:36:0x00ff, code:
            if (r22 == null) goto L_0x0101;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleCommand(CommandParams cmdParams, boolean isProactiveCmd) {
        CatLog.d((Object) this, cmdParams.getCommandType().name());
        if (isProactiveCmd && this.mUiccController != null) {
            this.mUiccController.addCardLog("ProactiveCommand mSlotId=" + this.mSlotId + " cmdParams=" + cmdParams);
        }
        CatCmdMessage catCmdMessage = new CatCmdMessage(cmdParams);
        CharSequence message;
        ResultCode resultCode;
        switch (m29xe796fd46()[cmdParams.getCommandType().ordinal()]) {
            case 1:
                sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, null);
                break;
            case 2:
            case 7:
            case 10:
            case 13:
                BIPClientParams cmd = (BIPClientParams) cmdParams;
                boolean noAlphaUsrCnf;
                try {
                    noAlphaUsrCnf = this.mContext.getResources().getBoolean(17957024);
                } catch (NotFoundException e) {
                    noAlphaUsrCnf = false;
                }
                if (cmd.mTextMsg.text != null || (!cmd.mHasAlphaId && !noAlphaUsrCnf)) {
                    if (!this.mStkAppInstalled) {
                        CatLog.d((Object) this, "No STK application found.");
                        if (isProactiveCmd) {
                            sendTerminalResponse(cmdParams.mCmdDet, ResultCode.BEYOND_TERMINAL_CAPABILITY, false, 0, null);
                            return;
                        }
                    }
                    if (isProactiveCmd && (cmdParams.getCommandType() == CommandType.CLOSE_CHANNEL || cmdParams.getCommandType() == CommandType.RECEIVE_DATA || cmdParams.getCommandType() == CommandType.SEND_DATA)) {
                        sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, null);
                        break;
                    }
                }
                CatLog.d((Object) this, "cmd " + cmdParams.getCommandType() + " with null alpha id");
                if (isProactiveCmd) {
                    sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, null);
                } else if (cmdParams.getCommandType() == CommandType.OPEN_CHANNEL) {
                    this.mCmdIf.handleCallSetupRequestFromSim(true, null);
                }
                return;
                break;
            case 3:
                String tmpText = catCmdMessage.geTextMessage().text;
                if (tmpText != null) {
                    if (!tmpText.equals("Error in application")) {
                        if (!tmpText.equals("invalid input")) {
                            if (!tmpText.equals("DF A8'H Default Error")) {
                                break;
                            }
                        }
                    }
                    String tmpMccMnc = TelephonyManager.getDefault().getSimOperatorNumeric();
                    if (tmpMccMnc != null) {
                        if (!tmpMccMnc.startsWith("404")) {
                            break;
                        }
                        CatLog.d((Object) this, "Ignore India sim card popup info, send TR directly");
                        sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, null);
                        return;
                    }
                }
                break;
            case 4:
            case 5:
            case 8:
            case 12:
                break;
            case 6:
                if (((LaunchBrowserParams) cmdParams).mConfirmMsg.text != null && ((LaunchBrowserParams) cmdParams).mConfirmMsg.text.equals(STK_DEFAULT)) {
                    message = this.mContext.getText(17040142);
                    ((LaunchBrowserParams) cmdParams).mConfirmMsg.text = message.toString();
                    break;
                }
            case 9:
                switch (cmdParams.mCmdDet.commandQualifier) {
                    case 3:
                        sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, new DTTZResponseData(null));
                        break;
                    case 4:
                        sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, new LanguageResponseData(Locale.getDefault().getLanguage()));
                        break;
                    default:
                        sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, null);
                        break;
                }
                return;
            case 11:
                CatLog.d((Object) this, "Pass Refresh to Stk app");
                break;
            case 14:
            case 15:
            case 16:
            case 17:
                if (((DisplayTextParams) cmdParams).mTextMsg.text != null && ((DisplayTextParams) cmdParams).mTextMsg.text.equals(STK_DEFAULT)) {
                    message = this.mContext.getText(17040832);
                    ((DisplayTextParams) cmdParams).mTextMsg.text = message.toString();
                    break;
                }
            case 18:
                if (((CallSetupParams) cmdParams).mConfirmMsg.text != null && ((CallSetupParams) cmdParams).mConfirmMsg.text.equals(STK_DEFAULT)) {
                    message = this.mContext.getText(17039428);
                    ((CallSetupParams) cmdParams).mConfirmMsg.text = message.toString();
                    break;
                }
            case 19:
                if (!isSupportedSetupEventCommand(catCmdMessage)) {
                    sendTerminalResponse(cmdParams.mCmdDet, ResultCode.BEYOND_TERMINAL_CAPABILITY, false, 0, null);
                    break;
                }
                sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, null);
                break;
            case 20:
                if (cmdParams.mLoadIconFailed) {
                    resultCode = ResultCode.PRFRMD_ICON_NOT_DISPLAYED;
                } else {
                    resultCode = ResultCode.OK;
                }
                sendTerminalResponse(cmdParams.mCmdDet, resultCode, false, 0, null);
                break;
            case 21:
                if (removeMenu(catCmdMessage.getMenu())) {
                    this.mMenuCmd = null;
                } else {
                    this.mMenuCmd = catCmdMessage;
                }
                if (cmdParams.mLoadIconFailed) {
                    resultCode = ResultCode.PRFRMD_ICON_NOT_DISPLAYED;
                } else {
                    resultCode = ResultCode.OK;
                }
                sendTerminalResponse(cmdParams.mCmdDet, resultCode, false, 0, null);
                break;
            default:
                CatLog.d((Object) this, "Unsupported command");
                return;
        }
        this.mCurrntCmd = catCmdMessage;
        sCurrntCmd[this.mSlotId] = catCmdMessage;
        broadcastCatCmdIntent(catCmdMessage);
    }

    private void broadcastCatCmdIntent(CatCmdMessage cmdMsg) {
        Intent intent = new Intent(AppInterface.CAT_CMD_ACTION);
        intent.addFlags(268435456);
        intent.putExtra("STK CMD", cmdMsg);
        intent.putExtra("SLOT_ID", this.mSlotId);
        intent.setComponent(AppInterface.getDefaultSTKApplication());
        CatLog.d((Object) this, "Sending CmdMsg: " + cmdMsg + " on slotid:" + this.mSlotId);
        this.mContext.sendBroadcast(intent, AppInterface.STK_PERMISSION);
    }

    private void handleSessionEnd() {
        CatLog.d((Object) this, "SESSION END on " + this.mSlotId);
        this.mCurrntCmd = this.mMenuCmd;
        sCurrntCmd[this.mSlotId] = this.mMenuCmd;
        Intent intent = new Intent(AppInterface.CAT_SESSION_END_ACTION);
        intent.putExtra("SLOT_ID", this.mSlotId);
        intent.setComponent(AppInterface.getDefaultSTKApplication());
        intent.addFlags(268435456);
        this.mContext.sendBroadcast(intent, AppInterface.STK_PERMISSION);
    }

    private void sendTerminalResponse(CommandDetails cmdDet, ResultCode resultCode, boolean includeAdditionalInfo, int additionalInfo, ResponseData resp) {
        if (cmdDet != null) {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            Input cmdInput = null;
            if (this.mCurrntCmd != null) {
                cmdInput = this.mCurrntCmd.geInput();
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
            buf.write(2);
            buf.write(130);
            buf.write(129);
            tag = ComprehensionTlvTag.RESULT.value();
            if (cmdDet.compRequired) {
                tag |= 128;
            }
            buf.write(tag);
            buf.write(includeAdditionalInfo ? 2 : 1);
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
        }
    }

    private void encodeOptionalTags(CommandDetails cmdDet, ResultCode resultCode, Input cmdInput, ByteArrayOutputStream buf) {
        CommandType cmdType = CommandType.fromInt(cmdDet.typeOfCommand);
        if (cmdType != null) {
            switch (m29xe796fd46()[cmdType.ordinal()]) {
                case 4:
                    if (resultCode.value() == ResultCode.NO_RESPONSE_FROM_USER.value() && cmdInput != null && cmdInput.duration != null) {
                        getInKeyResponse(buf, cmdInput);
                        return;
                    }
                    return;
                case 9:
                    if (cmdDet.commandQualifier == 4 && resultCode.value() == ResultCode.OK.value()) {
                        getPliResponse(buf);
                        return;
                    }
                    return;
                default:
                    CatLog.d((Object) this, "encodeOptionalTags() Unsupported Cmd details=" + cmdDet);
                    return;
            }
        }
        CatLog.d((Object) this, "encodeOptionalTags() bad Cmd details=" + cmdDet);
    }

    private void getInKeyResponse(ByteArrayOutputStream buf, Input cmdInput) {
        buf.write(ComprehensionTlvTag.DURATION.value());
        buf.write(2);
        TimeUnit timeUnit = cmdInput.duration.timeUnit;
        buf.write(TimeUnit.SECOND.value());
        buf.write(cmdInput.duration.timeInterval);
    }

    private void getPliResponse(ByteArrayOutputStream buf) {
        String lang = Locale.getDefault().getLanguage();
        if (lang != null) {
            buf.write(ComprehensionTlvTag.LANGUAGE.value());
            ResponseData.writeLength(buf, lang.length());
            buf.write(lang.getBytes(), 0, lang.length());
        }
    }

    private void sendMenuSelection(int menuId, boolean helpRequired) {
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

    private void eventDownload(int event, int sourceId, int destinationId, byte[] additionalInfo, boolean oneShot) {
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
        switch (event) {
            case 5:
                CatLog.d(sInstance, " Sending Idle Screen Available event download to ICC");
                break;
            case 7:
                CatLog.d(sInstance, " Sending Language Selection event download to ICC");
                buf.write(ComprehensionTlvTag.LANGUAGE.value() | 128);
                buf.write(2);
                break;
            case 19:
                CatLog.d((Object) this, " Sending HCI Connectivity event download to ICC");
                break;
        }
        if (additionalInfo != null) {
            for (byte b : additionalInfo) {
                buf.write(b);
            }
        }
        byte[] rawData = buf.toByteArray();
        rawData[1] = (byte) (rawData.length - 2);
        String hexString = IccUtils.bytesToHexString(rawData);
        CatLog.d((Object) this, "ENVELOPE COMMAND: " + hexString);
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
        CatLog.d((Object) this, "handleMessage[" + msg.what + "]");
        AsyncResult ar;
        switch (msg.what) {
            case 1:
            case 2:
            case 3:
            case 5:
                CatLog.d((Object) this, "ril message arrived,slotid:" + this.mSlotId);
                String str = null;
                if (msg.obj != null) {
                    ar = msg.obj;
                    if (!(ar == null || ar.result == null)) {
                        try {
                            str = ar.result;
                        } catch (ClassCastException e) {
                            return;
                        }
                    }
                }
                this.mMsgDecoder.sendStartDecodingMessageParams(new RilMessage(msg.what, str));
                return;
            case 4:
                this.mMsgDecoder.sendStartDecodingMessageParams(new RilMessage(msg.what, null));
                return;
            case 6:
                handleCmdResponse((CatResponseMessage) msg.obj);
                return;
            case 8:
                CatLog.d((Object) this, "MSG_ID_ICC_CHANGED");
                updateIccAvailability();
                return;
            case 9:
                CatLog.d((Object) this, "Received CAT CC Alpha message from card");
                if (msg.obj != null) {
                    ar = (AsyncResult) msg.obj;
                    if (ar == null || ar.result == null) {
                        CatLog.d((Object) this, "CAT Alpha message: ar.result is null");
                        return;
                    } else {
                        broadcastAlphaMessage((String) ar.result);
                        return;
                    }
                }
                CatLog.d((Object) this, "CAT Alpha message: msg.obj is null");
                return;
            case 10:
                handleRilMsg((RilMessage) msg.obj);
                return;
            case 20:
                return;
            case 30:
                if (msg.obj != null) {
                    ar = (AsyncResult) msg.obj;
                    if (ar == null || ar.result == null) {
                        CatLog.d((Object) this, "Icc REFRESH with exception: " + ar.exception);
                        return;
                    } else {
                        broadcastCardStateAndIccRefreshResp(CardState.CARDSTATE_PRESENT, (IccRefreshResponse) ar.result);
                        return;
                    }
                }
                CatLog.d((Object) this, "IccRefresh Message is null");
                return;
            default:
                throw new AssertionError("Unrecognized CAT command: " + msg.what);
        }
    }

    private void broadcastCardStateAndIccRefreshResp(CardState cardState, IccRefreshResponse iccRefreshState) {
        Intent intent = new Intent(AppInterface.CAT_ICC_STATUS_CHANGE);
        intent.addFlags(268435456);
        boolean cardPresent = cardState == CardState.CARDSTATE_PRESENT;
        if (iccRefreshState != null) {
            intent.putExtra(AppInterface.REFRESH_RESULT, iccRefreshState.refreshResult);
            intent.putExtra(AppInterface.AID, iccRefreshState.aid);
            CatLog.d((Object) this, "Sending IccResult with Result: " + iccRefreshState.refreshResult + " " + "aid: " + iccRefreshState.aid);
        }
        intent.putExtra(AppInterface.CARD_STATUS, cardPresent);
        intent.setComponent(AppInterface.getDefaultSTKApplication());
        intent.putExtra("SLOT_ID", this.mSlotId);
        CatLog.d((Object) this, "Sending Card Status: " + cardState + " " + "cardPresent: " + cardPresent + "SLOT_ID: " + this.mSlotId);
        this.mContext.sendBroadcast(intent, AppInterface.STK_PERMISSION);
    }

    private void broadcastAlphaMessage(String alphaString) {
        CatLog.d((Object) this, "Broadcasting CAT Alpha message from card: " + alphaString);
        Intent intent = new Intent(AppInterface.CAT_ALPHA_NOTIFY_ACTION);
        intent.addFlags(268435456);
        intent.putExtra(AppInterface.ALPHA_STRING, alphaString);
        intent.putExtra("SLOT_ID", this.mSlotId);
        intent.setComponent(AppInterface.getDefaultSTKApplication());
        this.mContext.sendBroadcast(intent, AppInterface.STK_PERMISSION);
    }

    public synchronized void onCmdResponse(CatResponseMessage resMsg) {
        if (resMsg != null) {
            obtainMessage(6, resMsg).sendToTarget();
        }
    }

    private boolean validateResponse(CatResponseMessage resMsg) {
        if (this.mCurrntCmd != null) {
            CatLog.d((Object) this, "lxj mCurrntCmd: " + this.mCurrntCmd.mCmdDet.typeOfCommand);
            if (!(sCurrntCmd[this.mSlotId] == null || this.mCurrntCmd == sCurrntCmd[this.mSlotId])) {
                this.mCurrntCmd = sCurrntCmd[this.mSlotId];
            }
        }
        if (this.mCurrntCmd == null) {
            CatLog.d((Object) this, "lxj  mCurrntCmd: is null ");
            this.mCurrntCmd = sCurrntCmd[this.mSlotId];
        }
        if (sCurrntCmd[this.mSlotId] != null) {
            CatLog.d((Object) this, "lxj sCurrntCmd[" + this.mSlotId + "]: " + sCurrntCmd[this.mSlotId].mCmdDet.typeOfCommand);
        }
        boolean validResponse = true;
        if (resMsg.mCmdDet.typeOfCommand == CommandType.SET_UP_EVENT_LIST.value() || resMsg.mCmdDet.typeOfCommand == CommandType.SET_UP_MENU.value()) {
            CatLog.d((Object) this, "CmdType: " + resMsg.mCmdDet.typeOfCommand);
        } else if (this.mCurrntCmd != null) {
            if (resMsg.mCmdDet.typeOfCommand != CommandType.DISPLAY_TEXT.value()) {
                validResponse = resMsg.mCmdDet.compareTo(this.mCurrntCmd.mCmdDet);
            }
            CatLog.d((Object) this, "isResponse for last valid cmd: " + validResponse);
        }
        return validResponse;
    }

    private boolean removeMenu(Menu menu) {
        try {
            return menu.items.size() == 1 && menu.items.get(0) == null;
        } catch (NullPointerException e) {
            CatLog.d((Object) this, "Unable to get Menu's items size");
            return true;
        }
    }

    /* JADX WARNING: Missing block: B:8:0x0032, code:
            switch(-getcom-android-internal-telephony-cat-AppInterface$CommandTypeSwitchesValues()[r9.ordinal()]) {
                case 3: goto L_0x0086;
                case 4: goto L_0x0060;
                case 5: goto L_0x0060;
                case 6: goto L_0x0097;
                case 7: goto L_0x00a8;
                case 8: goto L_0x0035;
                case 9: goto L_0x0035;
                case 10: goto L_0x0035;
                case 11: goto L_0x0035;
                case 12: goto L_0x0058;
                case 13: goto L_0x0035;
                case 14: goto L_0x0035;
                case 15: goto L_0x0035;
                case 16: goto L_0x0035;
                case 17: goto L_0x0035;
                case 18: goto L_0x00a8;
                case 19: goto L_0x00b8;
                case 20: goto L_0x0035;
                case 21: goto L_0x0049;
                default: goto L_0x0035;
            };
     */
    /* JADX WARNING: Missing block: B:9:0x0035, code:
            r5 = null;
     */
    /* JADX WARNING: Missing block: B:10:0x0036, code:
            sendTerminalResponse(r1, r12.mResCode, r12.mIncludeAdditionalInfo, r12.mAdditionalInfo, r5);
            r11.mCurrntCmd = null;
            sCurrntCmd[r11.mSlotId] = null;
     */
    /* JADX WARNING: Missing block: B:11:0x0048, code:
            return;
     */
    /* JADX WARNING: Missing block: B:13:0x004d, code:
            if (r12.mResCode != com.android.internal.telephony.cat.ResultCode.HELP_INFO_REQUIRED) goto L_0x0056;
     */
    /* JADX WARNING: Missing block: B:14:0x004f, code:
            r6 = true;
     */
    /* JADX WARNING: Missing block: B:15:0x0050, code:
            sendMenuSelection(r12.mUsersMenuSelection, r6);
     */
    /* JADX WARNING: Missing block: B:16:0x0055, code:
            return;
     */
    /* JADX WARNING: Missing block: B:17:0x0056, code:
            r6 = false;
     */
    /* JADX WARNING: Missing block: B:18:0x0058, code:
            r5 = new com.android.internal.telephony.cat.SelectItemResponseData(r12.mUsersMenuSelection);
     */
    /* JADX WARNING: Missing block: B:20:0x0062, code:
            if (r11.mCurrntCmd != null) goto L_0x0066;
     */
    /* JADX WARNING: Missing block: B:21:0x0064, code:
            r5 = null;
     */
    /* JADX WARNING: Missing block: B:22:0x0066, code:
            r7 = r11.mCurrntCmd.geInput();
     */
    /* JADX WARNING: Missing block: B:23:0x006e, code:
            if (r7.yesNo != false) goto L_0x007e;
     */
    /* JADX WARNING: Missing block: B:24:0x0070, code:
            if (r6 != false) goto L_0x0100;
     */
    /* JADX WARNING: Missing block: B:25:0x0072, code:
            r5 = new com.android.internal.telephony.cat.GetInkeyInputResponseData(r12.mUsersInput, r7.ucs2, r7.packed);
     */
    /* JADX WARNING: Missing block: B:26:0x007e, code:
            r5 = new com.android.internal.telephony.cat.GetInkeyInputResponseData(r12.mUsersYesNoSelection);
     */
    /* JADX WARNING: Missing block: B:28:0x008a, code:
            if (r12.mResCode != com.android.internal.telephony.cat.ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS) goto L_0x0092;
     */
    /* JADX WARNING: Missing block: B:29:0x008c, code:
            r12.setAdditionalInfo(1);
     */
    /* JADX WARNING: Missing block: B:30:0x0090, code:
            r5 = null;
     */
    /* JADX WARNING: Missing block: B:31:0x0092, code:
            r12.mIncludeAdditionalInfo = false;
            r12.mAdditionalInfo = 0;
     */
    /* JADX WARNING: Missing block: B:33:0x009b, code:
            if (r12.mResCode != com.android.internal.telephony.cat.ResultCode.LAUNCH_BROWSER_ERROR) goto L_0x00a3;
     */
    /* JADX WARNING: Missing block: B:34:0x009d, code:
            r12.setAdditionalInfo(4);
     */
    /* JADX WARNING: Missing block: B:35:0x00a1, code:
            r5 = null;
     */
    /* JADX WARNING: Missing block: B:36:0x00a3, code:
            r12.mIncludeAdditionalInfo = false;
            r12.mAdditionalInfo = 0;
     */
    /* JADX WARNING: Missing block: B:37:0x00a8, code:
            r11.mCmdIf.handleCallSetupRequestFromSim(r12.mUsersConfirm, null);
            r11.mCurrntCmd = null;
            sCurrntCmd[r11.mSlotId] = null;
     */
    /* JADX WARNING: Missing block: B:38:0x00b7, code:
            return;
     */
    /* JADX WARNING: Missing block: B:40:0x00bb, code:
            if (5 != r12.mEventValue) goto L_0x00c7;
     */
    /* JADX WARNING: Missing block: B:41:0x00bd, code:
            eventDownload(r12.mEventValue, 2, 129, r12.mAddedInfo, false);
     */
    /* JADX WARNING: Missing block: B:42:0x00c6, code:
            return;
     */
    /* JADX WARNING: Missing block: B:43:0x00c7, code:
            eventDownload(r12.mEventValue, 130, 129, r12.mAddedInfo, false);
     */
    /* JADX WARNING: Missing block: B:55:0x00fd, code:
            r5 = null;
     */
    /* JADX WARNING: Missing block: B:56:0x0100, code:
            r5 = null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleCmdResponse(CatResponseMessage resMsg) {
        if (validateResponse(resMsg)) {
            boolean helpRequired = false;
            CommandDetails cmdDet = resMsg.getCmdDetails();
            CommandType type = CommandType.fromInt(cmdDet.typeOfCommand);
            switch (m30-getcom-android-internal-telephony-cat-ResultCodeSwitchesValues()[resMsg.mResCode.ordinal()]) {
                case 1:
                case 17:
                    if (type != CommandType.SET_UP_CALL && type != CommandType.OPEN_CHANNEL) {
                        ResponseData resp = null;
                        break;
                    }
                    this.mCmdIf.handleCallSetupRequestFromSim(false, null);
                    this.mCurrntCmd = null;
                    sCurrntCmd[this.mSlotId] = null;
                    return;
                    break;
                case 2:
                    helpRequired = true;
                    break;
                case 3:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                case 13:
                case 14:
                case 15:
                    break;
                case 4:
                    if (type == CommandType.SET_UP_CALL) {
                        this.mCmdIf.handleCallSetupRequestFromSim(false, null);
                        this.mCurrntCmd = null;
                        sCurrntCmd[this.mSlotId] = null;
                        return;
                    }
                    break;
                case 16:
                    break;
                default:
                    return;
            }
        }
    }

    private boolean isStkAppInstalled() {
        List<ResolveInfo> broadcastReceivers = this.mContext.getPackageManager().queryBroadcastReceivers(new Intent(AppInterface.CAT_CMD_ACTION), 128);
        if ((broadcastReceivers == null ? 0 : broadcastReceivers.size()) > 0) {
            return true;
        }
        return false;
    }

    public void update(CommandsInterface ci, Context context, UiccCard ic) {
        UiccCardApplication ca = null;
        IccRecords ir = null;
        if (ic != null) {
            ca = ic.getApplicationIndex(0);
            if (ca != null) {
                ir = ca.getIccRecords();
            }
        }
        synchronized (sInstanceLock) {
            if (ir != null) {
                if (mIccRecords != ir) {
                    if (mIccRecords != null) {
                        mIccRecords.unregisterForRecordsLoaded(this);
                    }
                    CatLog.d((Object) this, "Reinitialize the Service with SIMRecords and UiccCardApplication");
                    mIccRecords = ir;
                    mUiccApplication = ca;
                    mIccRecords.registerForRecordsLoaded(this, 20, null);
                    CatLog.d((Object) this, "registerForRecordsLoaded slotid=" + this.mSlotId + " instance:" + this);
                }
            }
        }
    }

    void updateIccAvailability() {
        if (this.mUiccController != null) {
            CardState newState = CardState.CARDSTATE_ABSENT;
            UiccCard newCard = this.mUiccController.getUiccCard(this.mSlotId);
            if (newCard != null) {
                newState = newCard.getCardState();
            }
            CardState oldState = this.mCardState;
            this.mCardState = newState;
            CatLog.d((Object) this, "New Card State = " + newState + " " + "Old Card State = " + oldState);
            if (oldState == CardState.CARDSTATE_PRESENT && newState != CardState.CARDSTATE_PRESENT) {
                broadcastCardStateAndIccRefreshResp(newState, null);
            } else if (oldState != CardState.CARDSTATE_PRESENT && newState == CardState.CARDSTATE_PRESENT) {
                this.mCmdIf.reportStkServiceIsRunning(null);
            }
        }
    }
}
