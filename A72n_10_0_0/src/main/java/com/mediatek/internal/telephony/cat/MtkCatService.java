package com.mediatek.internal.telephony.cat;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.cat.AppInterface;
import com.android.internal.telephony.cat.BIPClientParams;
import com.android.internal.telephony.cat.CallSetupParams;
import com.android.internal.telephony.cat.CatCmdMessage;
import com.android.internal.telephony.cat.CatResponseMessage;
import com.android.internal.telephony.cat.CatService;
import com.android.internal.telephony.cat.CommandDetails;
import com.android.internal.telephony.cat.CommandParams;
import com.android.internal.telephony.cat.DisplayTextParams;
import com.android.internal.telephony.cat.LaunchBrowserParams;
import com.android.internal.telephony.cat.ResponseData;
import com.android.internal.telephony.cat.ResultCode;
import com.android.internal.telephony.cat.RilMessage;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccProfile;
import com.mediatek.internal.telephony.ModemSwitchHandler;
import com.mediatek.internal.telephony.MtkRIL;
import com.mediatek.internal.telephony.datasub.DataSubConstants;
import com.mediatek.internal.telephony.worldphone.WorldMode;
import com.mediatek.telephony.MtkTelephonyManagerEx;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MtkCatService extends CatService implements MtkAppInterface {
    static final String BIP_STATE_CHANGED = "mediatek.intent.action.BIP_STATE_CHANGED";
    private static final boolean DBG = true;
    private static final int DISABLE_DISPLAY_TEXT_DELAYED_TIME = 30000;
    private static final int IVSR_DELAYED_TIME = 60000;
    public static final int MSG_ID_CACHED_DISPLAY_TEXT_TIMEOUT = 46;
    private static final int MSG_ID_CALL_CTRL = 25;
    public static final int MSG_ID_CONN_RETRY_TIMEOUT = 47;
    static final int MSG_ID_DB_HANDLER = 12;
    private static final int MSG_ID_DISABLE_DISPLAY_TEXT_DELAYED = 15;
    static final int MSG_ID_EVENT_DOWNLOAD = 11;
    private static final int MSG_ID_IVSR_DELAYED = 14;
    static final int MSG_ID_LAUNCH_DB_SETUP_MENU = 13;
    private static final int MSG_ID_SETUP_MENU_RESET = 24;
    public static String NET_BUILD_TYPE = SystemProperties.get("persist.sys.net_build_type", "allnet");
    protected static Object mLock = new Object();
    private static CatCmdMessage[] sCurrntCmd = null;
    private static String[] sInstKey = {"sInstanceSim1", "sInstanceSim2", "sInstanceSim3", "sInstanceSim4"};
    private BroadcastReceiver MtkCatServiceReceiver = new BroadcastReceiver() {
        /* class com.mediatek.internal.telephony.cat.MtkCatService.AnonymousClass2 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            MtkCatLog.d(this, "CatServiceReceiver action: " + action);
            if (action.equals("com.mediatek.intent.action.IVSR_NOTIFY")) {
                if (MtkCatService.this.mSlotId == intent.getIntExtra("slot", 0) && intent.getStringExtra("action").equals("start")) {
                    MtkCatLog.d(this, "[IVSR set IVSR flag");
                    MtkCatService.this.isIvsrBootUp = true;
                    MtkCatService.this.sendEmptyMessageDelayed(14, 60000);
                }
            } else if (action.equals("com.mediatek.phone.ACTION_SIM_RECOVERY_DONE") || action.equals(ModemSwitchHandler.ACTION_MD_TYPE_CHANGE)) {
                if (action.equals("com.mediatek.phone.ACTION_SIM_RECOVERY_DONE")) {
                    MtkCatLog.d(this, "[Set SIM Recovery flag, sim: " + MtkCatService.this.mSlotId + ", isDisplayTextDisabled: " + (MtkCatService.this.isDisplayTextDisabled ? 1 : 0));
                } else {
                    MtkCatLog.d(this, "[World phone flag: " + MtkCatService.this.mSlotId + ", isDisplayTextDisabled: " + (MtkCatService.this.isDisplayTextDisabled ? 1 : 0));
                }
                MtkCatService.this.startTimeOut(15, 30000);
                MtkCatService.this.isDisplayTextDisabled = true;
            } else if (action.equals("android.telephony.action.SIM_CARD_STATE_CHANGED")) {
                int id = intent.getIntExtra("slot", -1);
                MtkCatLog.d(this, "SIM state change, id: " + id + ", simId: " + MtkCatService.this.mSlotId);
                if (id == MtkCatService.this.mSlotId) {
                    MtkCatService.this.simState = intent.getIntExtra("android.telephony.extra.SIM_STATE", 0);
                    MtkCatService.this.simIdfromIntent = id;
                    MtkCatLog.d(this, "simIdfromIntent[" + MtkCatService.this.simIdfromIntent + "],simState[" + MtkCatService.this.simState + "]");
                    if (MtkCatService.this.simState != 1) {
                        return;
                    }
                    if (TelephonyManager.getDefault().hasIccCard(MtkCatService.this.mSlotId)) {
                        MtkCatLog.d(this, "Igonre absent sim state");
                        return;
                    }
                    MtkCatService mtkCatService = MtkCatService.this;
                    mtkCatService.mSaveNewSetUpMenu = false;
                    mtkCatService.handleDBHandler(mtkCatService.mSlotId);
                }
            } else if (action.equals("android.intent.action.SIM_STATE_CHANGED")) {
                String reason = intent.getStringExtra(DataSubConstants.EXTRA_MOBILE_DATA_ENABLE_REASON);
                int slotId = intent.getIntExtra("slot", -1);
                if ("PLUGOUT".equals(reason) && slotId == MtkCatService.this.mSlotId) {
                    MtkCatLog.d(this, "SIM plug out set mHasShownCUMessage to false");
                    MtkCatService.this.mHasShownCUMessage = false;
                }
            }
        }
    };
    private boolean isDisplayTextDisabled = false;
    private boolean isIvsrBootUp = false;
    private BipService mBipService = null;
    private boolean mHasShownCUMessage = false;
    private boolean mIsProactiveCmdResponsed = false;
    private MtkRIL mMtkCmdIf;
    private boolean mMtkStkAppInstalled = false;
    private int mPhoneType = 0;
    private boolean mReadFromPreferenceDone = false;
    public boolean mSaveNewSetUpMenu = false;
    private boolean mSetUpMenuFromMD = false;
    Handler mTimeoutHandler = new Handler() {
        /* class com.mediatek.internal.telephony.cat.MtkCatService.AnonymousClass1 */

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 15) {
                MtkCatLog.d(this, "[Reset Disable Display Text flag because timeout");
                MtkCatService.this.isDisplayTextDisabled = false;
            } else if (i == 46) {
                MtkCatLog.d(this, "Cache DISPLAY_TEXT time out, sim_id: " + MtkCatService.this.mSlotId);
            }
        }
    };
    private int simIdfromIntent = 0;
    private int simState = 0;

    /* access modifiers changed from: package-private */
    public void cancelTimeOut(int msg) {
        MtkCatLog.d(this, "cancelTimeOut, sim_id: " + this.mSlotId + ", msg id: " + msg);
        this.mTimeoutHandler.removeMessages(msg);
    }

    /* access modifiers changed from: package-private */
    public void startTimeOut(int msg, long delay) {
        MtkCatLog.d(this, "startTimeOut, sim_id: " + this.mSlotId + ", msg id: " + msg);
        cancelTimeOut(msg);
        Handler handler = this.mTimeoutHandler;
        handler.sendMessageDelayed(handler.obtainMessage(msg), delay);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r6v0, resolved type: com.mediatek.internal.telephony.cat.MtkCatService */
    /* JADX WARN: Multi-variable type inference failed */
    public MtkCatService(CommandsInterface ci, UiccCardApplication ca, IccRecords ir, Context context, IccFileHandler fh, UiccProfile uiccProfile, int slotId) {
        super(ci, ca, ir, context, fh, uiccProfile, slotId);
        MtkCatLog.d(this, "slotId " + slotId);
        this.mMtkCmdIf = (MtkRIL) ci;
        if (!SystemProperties.get("ro.vendor.mtk_ril_mode").equals("c6m_1rild")) {
            this.mBipService = BipService.getInstance(this.mContext, this, this.mSlotId, this.mCmdIf, fh);
        }
        IntentFilter intentFilter = new IntentFilter("com.mediatek.intent.action.IVSR_NOTIFY");
        intentFilter.addAction("com.mediatek.phone.ACTION_SIM_RECOVERY_DONE");
        if (!"allnetcutest".equals(NET_BUILD_TYPE)) {
            intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        }
        intentFilter.addAction(ModemSwitchHandler.ACTION_MD_TYPE_CHANGE);
        IntentFilter mSIMStateChangeFilter = new IntentFilter("android.telephony.action.SIM_CARD_STATE_CHANGED");
        this.mContext.registerReceiver(this.MtkCatServiceReceiver, intentFilter);
        this.mContext.registerReceiver(this.MtkCatServiceReceiver, mSIMStateChangeFilter);
        MtkCatLog.d(this, "CatService: is running");
        this.mMtkCmdIf.setOnStkSetupMenuReset(this, MSG_ID_SETUP_MENU_RESET, null);
        this.mMtkStkAppInstalled = isMtkStkAppInstalled();
        MtkCatLog.d(this, "MTK STK app installed = " + this.mMtkStkAppInstalled);
        if (sCurrntCmd == null) {
            int simCount = TelephonyManager.getDefault().getSimCount();
            sCurrntCmd = new CatCmdMessage[simCount];
            for (int i = 0; i < simCount; i++) {
                sCurrntCmd[i] = null;
            }
        }
    }

    private void sendTerminalResponseByCurrentCmd(CatCmdMessage catCmd) {
        if (catCmd == null) {
            MtkCatLog.e(this, "catCmd is null.");
            return;
        }
        AppInterface.CommandType cmdType = AppInterface.CommandType.fromInt(catCmd.mCmdDet.typeOfCommand);
        MtkCatLog.d(this, "Send TR for cmd: " + cmdType);
        int i = AnonymousClass3.$SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[cmdType.ordinal()];
        if (i == 1 || i == 2) {
            sendTerminalResponse(catCmd.mCmdDet, ResultCode.OK, false, 0, null);
        } else if (i != 3) {
            sendTerminalResponse(catCmd.mCmdDet, ResultCode.UICC_SESSION_TERM_BY_USER, false, 0, null);
        } else {
            this.mMtkCmdIf.handleStkCallSetupRequestFromSimWithResCode(false, ResultCode.OK.value(), null);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: com.mediatek.internal.telephony.cat.MtkCatService */
    /* JADX WARN: Multi-variable type inference failed */
    public void dispose() {
        synchronized (sInstanceLock) {
            MtkCatLog.d(this, "Disposing MtkCatService object : " + this.mSlotId);
            this.mContext.unregisterReceiver(this.MtkCatServiceReceiver);
            if (!this.mIsProactiveCmdResponsed && this.mCurrntCmd != null) {
                MtkCatLog.d(this, "Send TR for the last pending commands.");
                sendTerminalResponseByCurrentCmd(this.mCurrntCmd);
            }
            this.mMtkCmdIf.unSetOnStkSetupMenuReset(this);
            this.mCmdIf.unregisterForIccRefresh(this);
            handleDBHandler(this.mSlotId);
        }
        BipService bipService = this.mBipService;
        if (bipService != null) {
            bipService.dispose();
        }
        MtkCatService.super.dispose();
    }

    /* access modifiers changed from: protected */
    public void handleRilMsg(RilMessage rilMsg) {
        if (rilMsg != null) {
            int i = rilMsg.mId;
            if (i == 2) {
                if (rilMsg.mId == 2) {
                    this.mIsProactiveCmdResponsed = false;
                }
                try {
                    CommandParams cmdParams = (CommandParams) rilMsg.mData;
                    if (cmdParams != null) {
                        if (cmdParams.getCommandType() == AppInterface.CommandType.SET_UP_MENU) {
                            this.mSetUpMenuFromMD = ((MtkRilMessage) rilMsg).mSetUpMenuFromMD;
                        }
                        if (rilMsg.mResCode == ResultCode.OK || rilMsg.mResCode == ResultCode.PRFRMD_ICON_NOT_DISPLAYED) {
                            handleCommand(cmdParams, true);
                            return;
                        }
                        MtkCatLog.d("CAT", "SS-handleMessage: invalid proactive command: " + cmdParams.mCmdDet.typeOfCommand);
                        sendTerminalResponse(cmdParams.mCmdDet, rilMsg.mResCode, false, 0, null);
                    }
                } catch (ClassCastException e) {
                    MtkCatLog.d(this, "Fail to parse proactive command");
                    if (this.mCurrntCmd != null) {
                        sendTerminalResponse(this.mCurrntCmd.mCmdDet, ResultCode.CMD_DATA_NOT_UNDERSTOOD, false, 0, null);
                    }
                }
            } else if (i != 3) {
                MtkCatService.super.handleRilMsg(rilMsg);
            } else {
                CommandParams cmdParams2 = (CommandParams) rilMsg.mData;
                if (cmdParams2 == null) {
                    return;
                }
                if (rilMsg.mResCode == ResultCode.OK) {
                    handleCommand(cmdParams2, false);
                    return;
                }
                MtkCatLog.d(this, "event notify error code: " + rilMsg.mResCode);
                if (rilMsg.mResCode == ResultCode.PRFRMD_ICON_NOT_DISPLAYED && (cmdParams2.mCmdDet.typeOfCommand == 17 || cmdParams2.mCmdDet.typeOfCommand == 18 || cmdParams2.mCmdDet.typeOfCommand == 19 || cmdParams2.mCmdDet.typeOfCommand == 20)) {
                    MtkCatLog.d(this, "notify user text message even though get icon fail");
                    handleCommand(cmdParams2, false);
                }
                if (cmdParams2.mCmdDet.typeOfCommand == 64) {
                    MtkCatLog.d(this, "Open Channel with ResultCode");
                    handleCommand(cmdParams2, false);
                }
            }
        }
    }

    /* JADX INFO: Multiple debug info for r1v75 int: [D('temp' int), D('hibyte' int)] */
    /* access modifiers changed from: protected */
    public void handleCommand(CommandParams cmdParams, boolean isProactiveCmd) {
        ResultCode resultCode;
        int i;
        ResultCode resultCode2;
        int flightMode;
        String tmpMccMnc;
        int flightMode2;
        boolean noAlphaUsrCnf;
        MtkCatLog.d(this, cmdParams.getCommandType().name());
        if (isProactiveCmd && this.mUiccController != null) {
            this.mUiccController.addCardLog("ProactiveCommand mSlotId=" + this.mSlotId + " cmdParams=" + cmdParams);
        }
        MtkCatCmdMessage cmdMsg = new MtkCatCmdMessage(cmdParams);
        boolean isFlightMode = true;
        switch (AnonymousClass3.$SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[cmdParams.getCommandType().ordinal()]) {
            case 1:
                if (removeMenu(cmdMsg.getMenu())) {
                    this.mMenuCmd = null;
                } else {
                    this.mMenuCmd = cmdMsg;
                }
                MtkCatLog.d("CAT", "mSetUpMenuFromMD: " + this.mSetUpMenuFromMD);
                if (cmdMsg.getMenu() != null) {
                    MtkMenu mtkMenu = (MtkMenu) cmdMsg.getMenu();
                    if (this.mSetUpMenuFromMD) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    mtkMenu.setSetUpMenuFlag(i);
                }
                if (this.mSetUpMenuFromMD) {
                    this.mSetUpMenuFromMD = false;
                    if (cmdParams.mLoadIconFailed) {
                        resultCode = ResultCode.PRFRMD_ICON_NOT_DISPLAYED;
                    } else {
                        resultCode = ResultCode.OK;
                    }
                    sendTerminalResponse(cmdParams.mCmdDet, resultCode, false, 0, null);
                    break;
                } else {
                    this.mIsProactiveCmdResponsed = true;
                    break;
                }
            case 2:
                if (cmdParams.mLoadIconFailed) {
                    resultCode2 = ResultCode.PRFRMD_ICON_NOT_DISPLAYED;
                } else {
                    resultCode2 = ResultCode.OK;
                }
                sendTerminalResponse(cmdParams.mCmdDet, resultCode2, false, 0, null);
                break;
            case 3:
                if (((CallSetupParams) cmdParams).mConfirmMsg.text != null && ((CallSetupParams) cmdParams).mConfirmMsg.text.equals("Default Message")) {
                    ((CallSetupParams) cmdParams).mConfirmMsg.text = this.mContext.getText(17039428).toString();
                    break;
                }
            case 4:
                boolean isAlarmState = isAlarmBoot();
                try {
                    flightMode = Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on");
                } catch (Settings.SettingNotFoundException e) {
                    MtkCatLog.d(this, "fail to get property from Settings");
                    flightMode = 0;
                }
                boolean isFlightMode2 = flightMode != 0;
                MtkCatLog.d(this, "isAlarmState = " + isAlarmState + ", isFlightMode = " + isFlightMode2 + ", flightMode = " + flightMode);
                if (isAlarmState && isFlightMode2) {
                    sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, null);
                    return;
                } else if (!checkSetupWizardInstalled() || SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("US")) {
                    String tmpText = cmdMsg.geTextMessage().text;
                    if (tmpText == null || ((!tmpText.equals("Error in application") && !tmpText.equals("invalid input") && !tmpText.equals("DF A8'H Default Error") && !tmpText.equals("DF A8'H, Default Error") && !tmpText.equals("Out of variable memory")) || (((tmpMccMnc = TelephonyManager.getDefault().getSimOperatorNumeric()) == null || (!tmpMccMnc.startsWith("404") && !tmpMccMnc.startsWith("405") && !tmpMccMnc.equals(""))) && tmpMccMnc != null))) {
                        if (tmpText != null && !"allnetcutest".equals(NET_BUILD_TYPE) && tmpText.equals("尊敬的用户，欢迎您使用中国联通业务！")) {
                            if (this.mHasShownCUMessage) {
                                MtkCatLog.d(this, "Ignore CU sim card popup info, send TR directly");
                                sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, null);
                                return;
                            }
                            this.mHasShownCUMessage = true;
                        }
                        if (this.isIvsrBootUp) {
                            MtkCatLog.d(this, "[IVSR send TR directly");
                            sendTerminalResponse(cmdParams.mCmdDet, ResultCode.BACKWARD_MOVE_BY_USER, false, 0, null);
                            return;
                        } else if (this.isDisplayTextDisabled) {
                            MtkCatLog.d(this, "[Sim Recovery send TR directly");
                            sendTerminalResponse(cmdParams.mCmdDet, ResultCode.BACKWARD_MOVE_BY_USER, false, 0, null);
                            return;
                        }
                    } else {
                        MtkCatLog.d(this, "Ignore India sim card popup info, send TR directly");
                        sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, null);
                        return;
                    }
                } else {
                    sendTerminalResponse(cmdParams.mCmdDet, ResultCode.BACKWARD_MOVE_BY_USER, false, 0, null);
                    return;
                }
                break;
            case 5:
                ResponseData resp = this.mBipService;
                if (resp != null) {
                    resp.setSetupEventList(cmdMsg);
                }
                this.mIsProactiveCmdResponsed = true;
                break;
            case 6:
                if (cmdParams.mCmdDet.commandQualifier == 3) {
                    Calendar cal = Calendar.getInstance();
                    int temp = cal.get(1) - 2000;
                    int lobyte = (temp % 10) << 4;
                    int temp2 = cal.get(2) + 1;
                    int lobyte2 = (temp2 % 10) << 4;
                    int temp3 = cal.get(5);
                    int lobyte3 = (temp3 % 10) << 4;
                    int temp4 = cal.get(11);
                    int lobyte4 = (temp4 % 10) << 4;
                    int temp5 = cal.get(12);
                    int lobyte5 = (temp5 % 10) << 4;
                    int temp6 = cal.get(13);
                    int temp7 = cal.get(15) / 900000;
                    byte[] datetime = {(byte) (lobyte | (temp / 10)), (byte) (lobyte2 | (temp2 / 10)), (byte) (lobyte3 | (temp3 / 10)), (byte) (lobyte4 | (temp4 / 10)), (byte) (lobyte5 | (temp5 / 10)), (byte) (((temp6 % 10) << 4) | (temp6 / 10)), (byte) (((temp7 % 10) << 4) | (temp7 / 10))};
                    sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, new MtkProvideLocalInformationResponseData(datetime[0], datetime[1], datetime[2], datetime[3], datetime[4], datetime[5], datetime[6]));
                    return;
                } else if (cmdParams.mCmdDet.commandQualifier == 4) {
                    Locale locale = Locale.getDefault();
                    sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, new MtkProvideLocalInformationResponseData(new byte[]{(byte) locale.getLanguage().charAt(0), (byte) locale.getLanguage().charAt(1)}));
                    return;
                } else if (cmdParams.mCmdDet.commandQualifier == 10) {
                    sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, new MtkProvideLocalInformationResponseData(getBatteryState(this.mContext)));
                    return;
                } else {
                    return;
                }
            case 7:
                if (((LaunchBrowserParams) cmdParams).mConfirmMsg.text != null && ((LaunchBrowserParams) cmdParams).mConfirmMsg.text.equals("Default Message")) {
                    ((LaunchBrowserParams) cmdParams).mConfirmMsg.text = this.mContext.getText(17040229).toString();
                    break;
                }
            case 8:
                boolean isAlarmState2 = isAlarmBoot();
                try {
                    flightMode2 = Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on");
                } catch (Settings.SettingNotFoundException e2) {
                    MtkCatLog.d(this, "fail to get property from Settings");
                    flightMode2 = 0;
                }
                if (flightMode2 == 0) {
                    isFlightMode = false;
                }
                MtkCatLog.d(this, "isAlarmState = " + isAlarmState2 + ", isFlightMode = " + isFlightMode + ", flightMode = " + flightMode2);
                if (isAlarmState2 && isFlightMode) {
                    sendTerminalResponse(cmdParams.mCmdDet, ResultCode.UICC_SESSION_TERM_BY_USER, false, 0, null);
                    return;
                }
            case 9:
            case 10:
                this.simState = MtkTelephonyManagerEx.getDefault().getSimCardState(this.mSlotId);
                MtkCatLog.d(this, "simState: " + this.simState);
                if (this.simState != 11) {
                    sendTerminalResponse(cmdParams.mCmdDet, ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS, false, 0, null);
                    return;
                }
                break;
            case 11:
            case 12:
                if ("Default Message".equals(((DisplayTextParams) cmdParams).mTextMsg.text)) {
                    ((DisplayTextParams) cmdParams).mTextMsg.text = null;
                }
                this.mIsProactiveCmdResponsed = true;
                break;
            case 13:
            case 14:
            case 15:
            case 16:
                this.mIsProactiveCmdResponsed = true;
                if (((DisplayTextParams) cmdParams).mTextMsg.text != null && ((DisplayTextParams) cmdParams).mTextMsg.text.equals("Default Message")) {
                    ((DisplayTextParams) cmdParams).mTextMsg.text = this.mContext.getText(17040988).toString();
                    break;
                }
            case 17:
                this.mIsProactiveCmdResponsed = true;
                break;
            case 18:
            case WorldMode.MD_WORLD_MODE_LTWCG /* 19 */:
            case 20:
            case WorldMode.MD_WORLD_MODE_LFCTG /* 21 */:
                BIPClientParams cmd = (BIPClientParams) cmdParams;
                try {
                    noAlphaUsrCnf = this.mContext.getResources().getBoolean(17891527);
                } catch (Resources.NotFoundException e3) {
                    noAlphaUsrCnf = false;
                }
                if (cmd.mTextMsg.text != null || (!cmd.mHasAlphaId && !noAlphaUsrCnf)) {
                    if (!this.mStkAppInstalled && !this.mMtkStkAppInstalled) {
                        MtkCatLog.d(this, "No STK application found.");
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
                    MtkCatLog.d(this, "cmd " + cmdParams.getCommandType() + " with null alpha id");
                    if (isProactiveCmd) {
                        sendTerminalResponse(cmdParams.mCmdDet, ResultCode.OK, false, 0, null);
                        return;
                    } else if (cmdParams.getCommandType() == AppInterface.CommandType.OPEN_CHANNEL) {
                        this.mMtkCmdIf.handleStkCallSetupRequestFromSimWithResCode(true, ResultCode.OK.value(), null);
                        return;
                    } else {
                        return;
                    }
                }
                break;
            default:
                MtkCatLog.d(this, "HandleCommand Callback to CatService");
                MtkCatService.super.handleCommand(cmdParams, isProactiveCmd);
                return;
        }
        this.mCurrntCmd = cmdMsg;
        sCurrntCmd[this.mSlotId] = cmdMsg;
        mtkBroadcastCatCmdIntent(cmdMsg);
        broadcastCatCmdIntent(cmdMsg.convertToCatCmdMessage(cmdParams, cmdMsg));
    }

    private void mtkBroadcastCatCmdIntent(CatCmdMessage cmdMsg) {
        Intent intent = new Intent(MtkAppInterface.MTK_CAT_CMD_ACTION);
        intent.addFlags(268435456);
        intent.putExtra("STK CMD", (Parcelable) cmdMsg);
        intent.putExtra("SLOT_ID", this.mSlotId);
        intent.setComponent(AppInterface.getDefaultSTKApplication());
        MtkCatLog.d(this, "mtkBroadcastCatCmdIntent Sending CmdMsg: " + cmdMsg + " on slotid:" + this.mSlotId);
        this.mContext.sendBroadcast(intent, "android.permission.RECEIVE_STK_COMMANDS");
    }

    /* access modifiers changed from: protected */
    public void onSetResponsedFlag() {
        this.mIsProactiveCmdResponsed = true;
    }

    /* access modifiers changed from: protected */
    public void sendMenuSelection(int menuId, boolean helpRequired) {
        MtkCatLog.d("CatService", "sendMenuSelection SET_UP_MENU");
        MtkCatService.super.sendMenuSelection(menuId, helpRequired);
        cancelTimeOut(15);
        this.isDisplayTextDisabled = false;
    }

    public static CatService getInstance(CommandsInterface ci, Context context, UiccProfile uiccProfile) {
        MtkCatLog.d("CatService", "call getInstance 2");
        int sim_id = 0;
        if (uiccProfile != null) {
            sim_id = uiccProfile.getPhoneId();
            MtkCatLog.d("CatService", "get SIM id from UiccCard. sim id: " + sim_id);
        }
        return CatService.getInstance(ci, context, uiccProfile, sim_id);
    }

    public static MtkAppInterface getInstance() {
        MtkCatLog.d("CatService", "call getInstance 4");
        return getInstance(null, null, null, 0);
    }

    public static MtkAppInterface getInstance(int slotId) {
        MtkCatLog.d("CatService", "call getInstance 3");
        return getInstance(null, null, null, slotId);
    }

    private static void handleProactiveCmdFromDB(MtkCatService inst, String data) {
        if (data == null) {
            MtkCatLog.d("MtkCatService", "handleProactiveCmdFromDB: cmd = null");
            return;
        }
        MtkCatLog.d("MtkCatService", " handleProactiveCmdFromDB: cmd = " + data + " from: " + inst);
        inst.mMsgDecoder.sendStartDecodingMessageParams(new MtkRilMessage(2, data));
        MtkCatLog.d("MtkCatService", "handleProactiveCmdFromDB: over");
    }

    private boolean isSetUpMenuCmd(String cmd) {
        if (cmd == null) {
            return false;
        }
        try {
            if (cmd.charAt(2) == '8' && cmd.charAt(3) == '1') {
                if (cmd.charAt(12) == '2' && cmd.charAt(13) == '5') {
                    return true;
                }
                return false;
            } else if (cmd.charAt(10) == '2' && cmd.charAt(11) == '5') {
                return true;
            } else {
                return false;
            }
        } catch (IndexOutOfBoundsException e) {
            MtkCatLog.d(this, "IndexOutOfBoundsException isSetUpMenuCmd: " + cmd);
            e.printStackTrace();
            return false;
        }
    }

    public static boolean getSaveNewSetUpMenuFlag(int sim_id) {
        if (sInstance == null || sInstance[sim_id] == null) {
            return false;
        }
        boolean result = sInstance[sim_id].mSaveNewSetUpMenu;
        MtkCatLog.d("CatService", sim_id + " , mSaveNewSetUpMenu: " + result);
        return result;
    }

    public void handleMessage(Message msg) {
        MtkCatLog.d(this, "MtkCatservice handleMessage[" + msg.what + "]");
        int i = msg.what;
        if (i == 1 || i == 2 || i == 3 || i == 5) {
            MtkCatLog.d(this, "ril message arrived, slotid:" + this.mSlotId);
            if (msg.obj != null) {
                AsyncResult ar = (AsyncResult) msg.obj;
                if (this.mMsgDecoder == null) {
                    MtkCatLog.e(this, "mMsgDecoder == null, return.");
                    return;
                } else if (!(ar == null || ar.result == null)) {
                    try {
                        String data = (String) ar.result;
                        if (isSetUpMenuCmd(data) && this == sInstance[this.mSlotId]) {
                            saveCmdToPreference(this.mContext, sInstKey[this.mSlotId], data);
                            this.mSaveNewSetUpMenu = true;
                            MtkRilMessage rilMsg = new MtkRilMessage(msg.what, data);
                            rilMsg.setSetUpMenuFromMD(true);
                            this.mMsgDecoder.sendStartDecodingMessageParams(rilMsg);
                            return;
                        } else if (data.contains("BIP")) {
                            Intent intent = new Intent(BIP_STATE_CHANGED);
                            intent.putExtra("BIP_CMD", data);
                            intent.putExtra("SLOT_ID", this.mSlotId);
                            intent.setPackage("com.mediatek.engineermode");
                            MtkCatLog.d(this, "Broadcast BIP Intent: Sending data: " + data + " on slotid:" + this.mSlotId);
                            this.mContext.sendBroadcast(intent);
                            return;
                        }
                    } catch (ClassCastException e) {
                        return;
                    }
                }
            }
        } else if (i != MSG_ID_SETUP_MENU_RESET) {
            switch (i) {
                case 11:
                    handleEventDownload((MtkCatResponseMessage) msg.obj);
                    return;
                case 12:
                    handleDBHandler(msg.arg1);
                    return;
                case 13:
                    MtkCatLog.d(this, "MSG_ID_LAUNCH_DB_SETUP_MENU");
                    String strCmd = readCmdFromPreference(sInstance[this.mSlotId], this.mContext, sInstKey[this.mSlotId]);
                    if (sInstance[this.mSlotId] != null && strCmd != null) {
                        handleProactiveCmdFromDB(sInstance[this.mSlotId], strCmd);
                        this.mSaveNewSetUpMenu = true;
                        return;
                    }
                    return;
                case 14:
                    MtkCatLog.d(this, "[IVSR cancel IVSR flag");
                    this.isIvsrBootUp = false;
                    return;
            }
        } else {
            MtkCatLog.d(this, "SETUP_MENU_RESET : Setup menu reset.");
            AsyncResult ar2 = (AsyncResult) msg.obj;
            if (ar2 == null || ar2.exception != null) {
                MtkCatLog.d(this, "SETUP_MENU_RESET : AsyncResult null.");
                return;
            } else {
                this.mSaveNewSetUpMenu = false;
                return;
            }
        }
        MtkCatService.super.handleMessage(msg);
    }

    public synchronized void onCmdResponse(CatResponseMessage resMsg) {
        MtkCatResponseMessage resMtkMsg;
        MtkCatLog.d(this, "MtkCatService onCmdResponse");
        if (resMsg != null) {
            if (MtkCatResponseMessage.class.isInstance(resMsg)) {
                obtainMessage(6, resMsg).sendToTarget();
            } else {
                if (this.mCurrntCmd != null) {
                    resMtkMsg = new MtkCatResponseMessage(this.mCurrntCmd, resMsg);
                } else {
                    resMtkMsg = new MtkCatResponseMessage(MtkCatCmdMessage.getCmdMsg(), resMsg);
                }
                obtainMessage(6, resMtkMsg).sendToTarget();
            }
        }
    }

    @Override // com.mediatek.internal.telephony.cat.MtkAppInterface
    public synchronized void onEventDownload(MtkCatResponseMessage resMsg) {
        if (resMsg != null) {
            obtainMessage(11, resMsg).sendToTarget();
        }
    }

    @Override // com.mediatek.internal.telephony.cat.MtkAppInterface
    public synchronized void onDBHandler(int sim_id) {
        obtainMessage(12, sim_id, 0).sendToTarget();
    }

    @Override // com.mediatek.internal.telephony.cat.MtkAppInterface
    public synchronized void onLaunchCachedSetupMenu() {
        obtainMessage(13, this.mSlotId, 0).sendToTarget();
    }

    private void handleEventDownload(MtkCatResponseMessage resMsg) {
        eventDownload(resMsg.mEvent, resMsg.mSourceId, resMsg.mDestinationId, resMsg.mAdditionalInfo, resMsg.mOneShot);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleDBHandler(int sim_id) {
        MtkCatLog.d(this, "handleDBHandler, sim_id: " + sim_id);
        saveCmdToPreference(this.mContext, sInstKey[sim_id], null);
    }

    /* access modifiers changed from: protected */
    public void handleCmdResponse(CatResponseMessage resMsg) {
        if (validateResponse(resMsg)) {
            CommandDetails cmdDet = resMsg.getCmdDetails();
            AppInterface.CommandType type = AppInterface.CommandType.fromInt(cmdDet.typeOfCommand);
            switch (AnonymousClass3.$SwitchMap$com$android$internal$telephony$cat$ResultCode[resMsg.mResCode.ordinal()]) {
                case 1:
                    if (type == AppInterface.CommandType.SET_UP_CALL || type == AppInterface.CommandType.OPEN_CHANNEL) {
                        this.mMtkCmdIf.handleStkCallSetupRequestFromSimWithResCode(resMsg.mUsersConfirm, resMsg.mResCode.value(), null);
                        this.mCurrntCmd = null;
                        return;
                    }
                    MtkCatService.super.handleCmdResponse(resMsg);
                    return;
                case 2:
                case 3:
                case 4:
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
                case 16:
                case 17:
                    this.mMtkCmdIf.handleStkCallSetupRequestFromSimWithResCode(resMsg.mUsersConfirm, resMsg.mResCode.value(), null);
                    this.mCurrntCmd = null;
                    return;
                case 18:
                    if (type == AppInterface.CommandType.SET_UP_CALL) {
                        this.mMtkCmdIf.handleStkCallSetupRequestFromSimWithResCode(resMsg.mUsersConfirm, resMsg.mResCode.value(), null);
                        this.mCurrntCmd = null;
                        return;
                    }
                    if (type == AppInterface.CommandType.DISPLAY_TEXT) {
                        sendTerminalResponse(cmdDet, resMsg.mResCode, resMsg.mIncludeAdditionalInfo, resMsg.mAdditionalInfo, null);
                        this.mCurrntCmd = null;
                        return;
                    }
                    MtkCatService.super.handleCmdResponse(resMsg);
                    return;
                case WorldMode.MD_WORLD_MODE_LTWCG /* 19 */:
                    if (type == AppInterface.CommandType.LAUNCH_BROWSER) {
                        if (resMsg.mAdditionalInfo == 0) {
                            resMsg.setAdditionalInfo(4);
                        }
                        sendTerminalResponse(cmdDet, resMsg.mResCode, resMsg.mIncludeAdditionalInfo, resMsg.mAdditionalInfo, null);
                        this.mCurrntCmd = null;
                        return;
                    }
                    MtkCatService.super.handleCmdResponse(resMsg);
                    return;
                default:
                    MtkCatService.super.handleCmdResponse(resMsg);
                    return;
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.mediatek.internal.telephony.cat.MtkCatService$3  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType = new int[AppInterface.CommandType.values().length];
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$cat$ResultCode = new int[ResultCode.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$cat$ResultCode[ResultCode.HELP_INFO_REQUIRED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$ResultCode[ResultCode.OK.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$ResultCode[ResultCode.PRFRMD_WITH_PARTIAL_COMPREHENSION.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$ResultCode[ResultCode.PRFRMD_WITH_MISSING_INFO.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$ResultCode[ResultCode.PRFRMD_WITH_ADDITIONAL_EFS_READ.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$ResultCode[ResultCode.PRFRMD_ICON_NOT_DISPLAYED.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$ResultCode[ResultCode.PRFRMD_MODIFIED_BY_NAA.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$ResultCode[ResultCode.PRFRMD_LIMITED_SERVICE.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$ResultCode[ResultCode.PRFRMD_WITH_MODIFICATION.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$ResultCode[ResultCode.PRFRMD_NAA_NOT_ACTIVE.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$ResultCode[ResultCode.PRFRMD_TONE_NOT_PLAYED.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$ResultCode[ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$ResultCode[ResultCode.NO_RESPONSE_FROM_USER.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$ResultCode[ResultCode.UICC_SESSION_TERM_BY_USER.ordinal()] = 14;
            } catch (NoSuchFieldError e14) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$ResultCode[ResultCode.BACKWARD_MOVE_BY_USER.ordinal()] = 15;
            } catch (NoSuchFieldError e15) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$ResultCode[ResultCode.CMD_DATA_NOT_UNDERSTOOD.ordinal()] = 16;
            } catch (NoSuchFieldError e16) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$ResultCode[ResultCode.USER_NOT_ACCEPT.ordinal()] = 17;
            } catch (NoSuchFieldError e17) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$ResultCode[ResultCode.NETWORK_CRNTLY_UNABLE_TO_PROCESS.ordinal()] = 18;
            } catch (NoSuchFieldError e18) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$ResultCode[ResultCode.LAUNCH_BROWSER_ERROR.ordinal()] = 19;
            } catch (NoSuchFieldError e19) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.SET_UP_MENU.ordinal()] = 1;
            } catch (NoSuchFieldError e20) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.SET_UP_IDLE_MODE_TEXT.ordinal()] = 2;
            } catch (NoSuchFieldError e21) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.SET_UP_CALL.ordinal()] = 3;
            } catch (NoSuchFieldError e22) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.DISPLAY_TEXT.ordinal()] = 4;
            } catch (NoSuchFieldError e23) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.SET_UP_EVENT_LIST.ordinal()] = 5;
            } catch (NoSuchFieldError e24) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.PROVIDE_LOCAL_INFORMATION.ordinal()] = 6;
            } catch (NoSuchFieldError e25) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.LAUNCH_BROWSER.ordinal()] = 7;
            } catch (NoSuchFieldError e26) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.SELECT_ITEM.ordinal()] = 8;
            } catch (NoSuchFieldError e27) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.GET_INPUT.ordinal()] = 9;
            } catch (NoSuchFieldError e28) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.GET_INKEY.ordinal()] = 10;
            } catch (NoSuchFieldError e29) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.REFRESH.ordinal()] = 11;
            } catch (NoSuchFieldError e30) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.RUN_AT.ordinal()] = 12;
            } catch (NoSuchFieldError e31) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.SEND_DTMF.ordinal()] = 13;
            } catch (NoSuchFieldError e32) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.SEND_SMS.ordinal()] = 14;
            } catch (NoSuchFieldError e33) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.SEND_SS.ordinal()] = 15;
            } catch (NoSuchFieldError e34) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.SEND_USSD.ordinal()] = 16;
            } catch (NoSuchFieldError e35) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.PLAY_TONE.ordinal()] = 17;
            } catch (NoSuchFieldError e36) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.OPEN_CHANNEL.ordinal()] = 18;
            } catch (NoSuchFieldError e37) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.CLOSE_CHANNEL.ordinal()] = 19;
            } catch (NoSuchFieldError e38) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.RECEIVE_DATA.ordinal()] = 20;
            } catch (NoSuchFieldError e39) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$cat$AppInterface$CommandType[AppInterface.CommandType.SEND_DATA.ordinal()] = 21;
            } catch (NoSuchFieldError e40) {
            }
        }
    }

    public Context getContext() {
        return this.mContext;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r6v0, resolved type: com.mediatek.internal.telephony.cat.MtkCatService */
    /* JADX WARN: Multi-variable type inference failed */
    public void update(CommandsInterface ci, Context context, UiccProfile uiccProfile) {
        UiccCardApplication ca = null;
        IccRecords ir = null;
        if (uiccProfile != null) {
            int newPhoneType = getPhoneType();
            MtkCatLog.d("MtkCatService", "update PhoneType : " + newPhoneType + ", mSlotId: " + this.mSlotId);
            int oldPhoneType = this.mPhoneType;
            this.mPhoneType = newPhoneType;
            if (!(oldPhoneType == 0 || oldPhoneType == newPhoneType)) {
                MtkCatLog.d("MtkCatService", "phone type change,reset card state to absent.....");
                this.mCardState = IccCardStatus.CardState.CARDSTATE_ABSENT;
            }
            if (this.mPhoneType == 2) {
                ca = uiccProfile.getApplication(2);
            } else {
                ca = uiccProfile.getApplicationIndex(0);
            }
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
                    MtkCatLog.d(this, "Reinitialize the Service with SIMRecords and UiccCardApplication");
                    mIccRecords = ir;
                    mUiccApplication = ca;
                    mIccRecords.registerForRecordsLoaded(this, 20, (Object) null);
                    MtkCatLog.d(this, "registerForRecordsLoaded slotid=" + this.mSlotId + " instance:" + this);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updateIccAvailability() {
        if (this.mUiccController == null) {
            MtkCatLog.d(this, "updateIccAvailability, mUiccController is null");
            return;
        }
        IccCardStatus.CardState newState = IccCardStatus.CardState.CARDSTATE_ABSENT;
        UiccCard newCard = this.mUiccController.getUiccCard(this.mSlotId);
        if (newCard != null) {
            newState = newCard.getCardState();
        }
        IccCardStatus.CardState oldState = this.mCardState;
        this.mCardState = newState;
        MtkCatLog.d(this, "Slot id: " + this.mSlotId + " New Card State = " + newState + " Old Card State = " + oldState);
        if (oldState == IccCardStatus.CardState.CARDSTATE_PRESENT && newState != IccCardStatus.CardState.CARDSTATE_PRESENT) {
            broadcastCardStateAndIccRefreshResp(newState, null);
        } else if (oldState != IccCardStatus.CardState.CARDSTATE_PRESENT && newState == IccCardStatus.CardState.CARDSTATE_PRESENT) {
            if (this.mCmdIf.getRadioState() == 2) {
                MtkCatLog.w(this, "updateIccAvailability(): Radio unavailable");
                this.mCardState = oldState;
                return;
            }
            MtkCatLog.d(this, "SIM present. Reporting STK service running now...");
            this.mCmdIf.reportStkServiceIsRunning((Message) null);
        }
    }

    private boolean isAlarmBoot() {
        String bootReason = SystemProperties.get("vendor.sys.boot.reason");
        return bootReason != null && bootReason.equals("1");
    }

    private boolean checkSetupWizardInstalled() {
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            MtkCatLog.d(this, "fail to get PM");
            return false;
        }
        boolean isPkgInstalled = true;
        try {
            pm.getInstallerPackageName("com.google.android.setupwizard");
        } catch (IllegalArgumentException e) {
            MtkCatLog.d(this, "fail to get SetupWizard package");
            isPkgInstalled = false;
        }
        if (isPkgInstalled) {
            int pkgEnabledState = pm.getComponentEnabledSetting(new ComponentName("com.google.android.setupwizard", "com.google.android.setupwizard.SetupWizardActivity"));
            if (pkgEnabledState == 1 || pkgEnabledState == 0) {
                MtkCatLog.d(this, "should not show DISPLAY_TEXT immediately");
                return true;
            }
            MtkCatLog.d(this, "Setup Wizard Activity is not activate");
        }
        MtkCatLog.d(this, "isPkgInstalled = false");
        return false;
    }

    @Override // com.mediatek.internal.telephony.cat.MtkAppInterface
    public IccRecords getIccRecords() {
        IccRecords iccRecords;
        synchronized (sInstanceLock) {
            iccRecords = mIccRecords;
        }
        return iccRecords;
    }

    private static void saveCmdToPreference(Context context, String key, String cmd) {
        synchronized (mLock) {
            MtkCatLog.d("MtkCatService", "saveCmdToPreference, key: " + key + ", cmd: " + cmd);
            SharedPreferences.Editor editor = context.getSharedPreferences("set_up_menu", 0).edit();
            editor.putString(key, cmd);
            editor.apply();
        }
    }

    private static String readCmdFromPreference(MtkCatService inst, Context context, String key) {
        String cmd = String.valueOf("");
        if (inst == null) {
            MtkCatLog.d("MtkCatService", "readCmdFromPreference with null instance");
            return null;
        }
        synchronized (mLock) {
            if (!inst.mReadFromPreferenceDone) {
                cmd = context.getSharedPreferences("set_up_menu", 0).getString(key, "");
                inst.mReadFromPreferenceDone = true;
                MtkCatLog.d("MtkCatService", "readCmdFromPreference, key: " + key + ", cmd: " + cmd);
            } else {
                MtkCatLog.d("MtkCatService", "readCmdFromPreference, do not read again");
            }
        }
        if (cmd.length() == 0) {
            return null;
        }
        return cmd;
    }

    public static int getBatteryState(Context context) {
        int batteryState = 255;
        Intent batteryStatus = context.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra("level", -1);
            int scale = batteryStatus.getIntExtra("scale", -1);
            int status = batteryStatus.getIntExtra("status", -1);
            boolean isCharging = status == 2 || status == 5;
            float batteryPct = ((float) level) / ((float) scale);
            MtkCatLog.d("MtkCatService", " batteryPct == " + batteryPct + "isCharging:" + isCharging);
            if (isCharging) {
                batteryState = 255;
            } else if (((double) batteryPct) <= 0.05d) {
                batteryState = 0;
            } else if (((double) batteryPct) > 0.05d && ((double) batteryPct) <= 0.15d) {
                batteryState = 1;
            } else if (((double) batteryPct) > 0.15d && ((double) batteryPct) <= 0.6d) {
                batteryState = 2;
            } else if (((double) batteryPct) > 0.6d && batteryPct < 1.0f) {
                batteryState = 3;
            } else if (batteryPct == 1.0f) {
                batteryState = 4;
            }
        }
        MtkCatLog.d("MtkCatService", "getBatteryState() batteryState = " + batteryState);
        return batteryState;
    }

    private boolean isMtkStkAppInstalled() {
        List<ResolveInfo> broadcastReceivers = this.mContext.getPackageManager().queryBroadcastReceivers(new Intent(MtkAppInterface.MTK_CAT_CMD_ACTION), 128);
        if ((broadcastReceivers == null ? 0 : broadcastReceivers.size()) > 0) {
            return true;
        }
        return false;
    }

    private int getPhoneType() {
        int[] subId = SubscriptionManager.getSubId(this.mSlotId);
        if (subId == null) {
            return 0;
        }
        int phoneType = TelephonyManager.getDefault().getCurrentPhoneType(subId[0]);
        MtkCatLog.v(this, "getPhoneType phoneType:  " + phoneType + ", mSlotId: " + this.mSlotId);
        return phoneType;
    }

    /* access modifiers changed from: protected */
    public void handleSessionEnd() {
        sCurrntCmd[this.mSlotId] = this.mMenuCmd;
        MtkCatService.super.handleSessionEnd();
    }

    /* access modifiers changed from: protected */
    public boolean validateResponse(CatResponseMessage resMsg) {
        if (this.mCurrntCmd != null) {
            MtkCatLog.d(this, "lxj mCurrntCmd: " + this.mCurrntCmd.mCmdDet.typeOfCommand);
            if (!(sCurrntCmd[this.mSlotId] == null || this.mCurrntCmd == sCurrntCmd[this.mSlotId])) {
                this.mCurrntCmd = sCurrntCmd[this.mSlotId];
            }
        }
        if (this.mCurrntCmd == null) {
            MtkCatLog.d(this, "lxj  mCurrntCmd: is null ");
            this.mCurrntCmd = sCurrntCmd[this.mSlotId];
        }
        if (sCurrntCmd[this.mSlotId] != null) {
            MtkCatLog.d(this, "lxj sCurrntCmd[" + this.mSlotId + "]: " + sCurrntCmd[this.mSlotId].mCmdDet.typeOfCommand);
        }
        boolean validResponse = true;
        if (resMsg.mCmdDet.typeOfCommand == AppInterface.CommandType.SET_UP_EVENT_LIST.value() || resMsg.mCmdDet.typeOfCommand == AppInterface.CommandType.SET_UP_MENU.value()) {
            MtkCatLog.d(this, "CmdType: " + resMsg.mCmdDet.typeOfCommand);
        } else if (this.mCurrntCmd != null) {
            if (resMsg.mCmdDet.typeOfCommand != AppInterface.CommandType.DISPLAY_TEXT.value()) {
                validResponse = resMsg.mCmdDet.compareTo(this.mCurrntCmd.mCmdDet);
            }
            MtkCatLog.d(this, "isResponse for last valid cmd: " + validResponse);
        }
        return validResponse;
    }
}
