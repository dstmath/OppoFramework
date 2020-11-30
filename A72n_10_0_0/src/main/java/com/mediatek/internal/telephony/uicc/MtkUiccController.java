package com.mediatek.internal.telephony.uicc;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.provider.Settings;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.IOppoUiccManager;
import com.android.internal.telephony.OppoSimlockManager;
import com.android.internal.telephony.OppoTelephonyFactory;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.mediatek.internal.telephony.MtkGsmCdmaPhone;
import com.mediatek.internal.telephony.MtkIccCardConstants;
import com.mediatek.internal.telephony.MtkSubscriptionInfoUpdater;
import com.mediatek.internal.telephony.OpTelephonyCustomizationUtils;
import com.mediatek.internal.telephony.RadioCapabilitySwitchUtil;
import com.mediatek.internal.telephony.RadioManager;
import com.mediatek.internal.telephony.datasub.DataSubConstants;
import com.mediatek.telephony.internal.telephony.vsim.ExternalSimConstants;
import com.mediatek.telephony.internal.telephony.vsim.ExternalSimManager;
import java.util.Arrays;

public class MtkUiccController extends UiccController {
    protected static final String COMMON_SLOT_PROPERTY = "ro.vendor.mtk_sim_hot_swap_common_slot";
    protected static final String DECRYPT_STATE = "trigger_restart_framework";
    protected static final int EVENT_BASE_ID = 100;
    protected static final int EVENT_CARD_DETECTED_IND = 115;
    protected static final int EVENT_COMMON_SLOT_NO_CHANGED = 111;
    protected static final int EVENT_GET_ICC_STATUS_DONE_FOR_SIM_MISSING = 105;
    protected static final int EVENT_GET_ICC_STATUS_DONE_FOR_SIM_RECOVERY = 106;
    protected static final int EVENT_INVALID_SIM_DETECTED = 109;
    protected static final int EVENT_REPOLL_SML_STATE = 110;
    protected static final int EVENT_SIM_MISSING = 103;
    protected static final int EVENT_SIM_PLUG_IN = 108;
    protected static final int EVENT_SIM_PLUG_OUT = 107;
    protected static final int EVENT_SIM_POWER_CHANGED = 114;
    protected static final int EVENT_SIM_RECOVERY = 104;
    protected static final int EVENT_SML_SLOT_LOCK_INFO_CHANGED = 112;
    protected static final int EVENT_SUPPLY_DEVICE_LOCK_DONE = 113;
    protected static final int EVENT_VIRTUAL_SIM_OFF = 102;
    protected static final int EVENT_VIRTUAL_SIM_ON = 101;
    private static final String LOG_TAG_EX = "MtkUiccCtrl";
    private static final String PROPERTY_SIM_CARD_ONOFF = "ro.vendor.mtk_sim_card_onoff";
    private static final String[] PROPERTY_SIM_ONOFF_STATE = {"vendor.ril.sim.onoff.state1", "vendor.ril.sim.onoff.state2", "vendor.ril.sim.onoff.state3", "vendor.ril.sim.onoff.state4"};
    private static final String PROPERTY_SIM_ONOFF_SUPPORT = "vendor.ril.sim.onoff.support";
    private static final int SML_FEATURE_NEED_BROADCAST_INTENT = 1;
    private static final int SML_FEATURE_NO_NEED_BROADCAST_INTENT = 0;
    public static final String WHITE_SIM_CARD_INSERT_PROC = "persist.radio.oppo.white_sim_card_insert";
    private int[] UICCCONTROLLER_STRING_NOTIFICATION_VIRTUAL_SIM_ON = {134545753, 134545754, 134545755, 134545756};
    private BroadcastReceiver mMdStateReceiver;
    private IMtkRsuSml mMtkRsuSml = null;
    private int[] mSimPower;
    private int[] mSimPowerExecutingState;

    /* JADX DEBUG: Multi-variable search result rejected for r8v0, resolved type: com.mediatek.internal.telephony.uicc.MtkUiccController */
    /* JADX WARN: Multi-variable type inference failed */
    public MtkUiccController(Context c, CommandsInterface[] ci) {
        super(c, ci);
        Rlog.d(LOG_TAG_EX, "Creating MtkUiccController");
        for (int i = 0; i < this.mCis.length; i++) {
            Integer index = new Integer(i);
            this.mCis[i].unregisterForAvailable(this);
            this.mCis[i].unregisterForOn(this);
            if (SystemProperties.get("ro.crypto.state").equals("unencrypted") || SystemProperties.get("ro.crypto.state").equals("unsupported") || SystemProperties.get("ro.crypto.type").equals("file") || DECRYPT_STATE.equals(SystemProperties.get("vold.decrypt")) || !StorageManager.inCryptKeeperBounce()) {
                this.mCis[i].registerForAvailable(this, 6, index);
            } else {
                this.mCis[i].registerForOn(this, 5, index);
            }
            this.mCis[i].registerForVirtualSimOn(this, 101, index);
            this.mCis[i].registerForVirtualSimOff(this, 102, index);
            this.mCis[i].registerForSimMissing(this, EVENT_SIM_MISSING, index);
            this.mCis[i].registerForSimRecovery(this, 104, index);
            this.mCis[i].registerForSimPlugOut(this, EVENT_SIM_PLUG_OUT, index);
            this.mCis[i].registerForSimPlugIn(this, EVENT_SIM_PLUG_IN, index);
            this.mCis[i].registerForCommonSlotNoChanged(this, 111, index);
            this.mCis[i].registerForSmlSlotLockInfoChanged(this, 112, index);
            this.mCis[i].registerForSimTrayPlugIn(this, ExternalSimConstants.EVENT_TYPE_RSIM_AUTH_DONE, index);
            this.isTrayPlugIn[i] = false;
            this.isSimPlugIn[i] = false;
            this.mCis[i].registerForSimPower(this, 114, index);
            this.mCis[i].registerForCardDetectedInd(this, EVENT_CARD_DETECTED_IND, index);
        }
        if (SystemProperties.getInt("ro.vendor.mtk_external_sim_support", 0) == 1) {
            ExternalSimManager.make(c, ci);
        }
        this.mMdStateReceiver = new ModemStateChangedReceiver(this, null);
        IntentFilter filter = new IntentFilter();
        filter.addAction(RadioManager.ACTION_MODEM_POWER_NO_CHANGE);
        this.mContext.registerReceiver(this.mMdStateReceiver, filter);
        this.mSimPower = new int[this.mCis.length];
        this.mSimPowerExecutingState = new int[this.mCis.length];
        Arrays.fill(this.mSimPower, -1);
        Arrays.fill(this.mSimPowerExecutingState, -1);
        try {
            this.mMtkRsuSml = OpTelephonyCustomizationUtils.getOpFactory(this.mContext).makeRsuSml(this.mContext, this.mCis);
            Rlog.d(LOG_TAG_EX, "[RSU-SIMLOCK] Create RsuSml");
        } catch (Exception e) {
            Rlog.e(LOG_TAG_EX, "[RSU-SIMLOCK] e = " + e);
        }
    }

    public IMtkRsuSml getRsuSml() {
        if (this.mMtkRsuSml == null) {
            Rlog.e(LOG_TAG_EX, "getRsuSml : [RSU-SIMLOCK] Sml not supported");
        }
        return this.mMtkRsuSml;
    }

    public UiccCardApplication getUiccCardApplication(int family) {
        return getUiccCardApplication(SubscriptionController.getInstance().getPhoneId(SubscriptionController.getInstance().getDefaultSubId()), family);
    }

    public int getIccApplicationChannel(int slotId, int family) {
        return 0;
    }

    public void handleMessage(Message msg) {
        OppoSimlockManager simlockMgr;
        synchronized (mLock) {
            Integer index = getCiIndex(msg);
            if (index.intValue() >= 0) {
                if (index.intValue() < this.mCis.length) {
                    if (msg.obj != null && (msg.obj instanceof AsyncResult)) {
                        AsyncResult asyncResult = (AsyncResult) msg.obj;
                    }
                    int i = msg.what;
                    if (i == 1) {
                        mtkLog("Received EVENT_ICC_STATUS_CHANGED, calling getIccCardStatus,index: " + index);
                        if (ignoreGetSimStatus()) {
                            mtkLog("FlightMode ON, Modem OFF: ignore get sim status");
                        } else {
                            this.mCis[index.intValue()].getIccCardStatus(obtainMessage(3, index));
                        }
                    } else if (i == 203) {
                        mtkLog("EVENT_TRAY_PLUG_IN, isTrayPlugIn[" + index + "]=" + this.isTrayPlugIn[index.intValue()] + ",will set to true");
                        this.isTrayPlugIn[index.intValue()] = true;
                        notifyIccIdForTrayPlugIn(index.intValue());
                        saveSimPlugState(index.intValue(), 162);
                    } else if (i != 5 && i != 6) {
                        boolean needIntent = false;
                        switch (i) {
                            case 101:
                                mtkLog("handleMessage (EVENT_VIRTUAL_SIM_ON)");
                                setNotificationVirtual(index.intValue(), 101);
                                SharedPreferences.Editor editorOn = this.mContext.getSharedPreferences("AutoAnswer", 0).edit();
                                editorOn.putBoolean("flag", true);
                                editorOn.commit();
                                break;
                            case 102:
                                mtkLog("handleMessage (EVENT_VIRTUAL_SIM_OFF)");
                                removeNotificationVirtual(index.intValue(), 101);
                                SharedPreferences.Editor editorOff = this.mContext.getSharedPreferences("AutoAnswer", 0).edit();
                                editorOff.putBoolean("flag", false);
                                editorOff.commit();
                                break;
                            case EVENT_SIM_MISSING /* 103 */:
                                mtkLog("handleMessage (EVENT_SIM_MISSING)");
                                this.mCis[index.intValue()].getIccCardStatus(obtainMessage(105, index));
                                break;
                            case 104:
                                mtkLog("handleMessage (EVENT_SIM_RECOVERY)");
                                this.mCis[index.intValue()].getIccCardStatus(obtainMessage(EVENT_GET_ICC_STATUS_DONE_FOR_SIM_RECOVERY, index));
                                Intent intent = new Intent();
                                intent.setAction("com.mediatek.phone.ACTION_SIM_RECOVERY_DONE");
                                this.mContext.sendBroadcast(intent);
                                break;
                            case 105:
                                mtkLog("Received EVENT_GET_ICC_STATUS_DONE_FOR_SIM_MISSING");
                                onGetIccCardStatusDone((AsyncResult) msg.obj, index);
                                break;
                            case EVENT_GET_ICC_STATUS_DONE_FOR_SIM_RECOVERY /* 106 */:
                                mtkLog("Received EVENT_GET_ICC_STATUS_DONE_FOR_SIM_RECOVERY");
                                onGetIccCardStatusDone((AsyncResult) msg.obj, index);
                                break;
                            case EVENT_SIM_PLUG_OUT /* 107 */:
                                mtkLog("EVENT_SIM_PLUG_OUT, index=" + index);
                                notifyIccIdForSimPlugOut(index.intValue());
                                this.isSimPlugIn[index.intValue()] = false;
                                this.isTrayPlugIn[index.intValue()] = false;
                                broadcastCardHotSwapState(index.intValue());
                                saveSimPlugState(index.intValue(), 163);
                                if (getHaveInsertTestCard()) {
                                    for (int i2 = 0; i2 < TelephonyManager.getDefault().getPhoneCount(); i2++) {
                                        SystemProperties.set(WHITE_SIM_CARD_INSERT_PROC, String.valueOf(0));
                                        this.mCis[i2].invokeOemRilRequestStrings(new String[]{"AT+E5GOPT=5", ""}, null);
                                        Rlog.d(LOG_TAG_EX, "oppo debug set NSA mode for default!");
                                    }
                                    break;
                                }
                                break;
                            case EVENT_SIM_PLUG_IN /* 108 */:
                                mtkLog("Received EVENT_SIM_PLUG_IN, index=" + index);
                                mtkLog("EVENT_SIM_PLUG_IN,isTrayPlugIn[" + index + "]=" + this.isTrayPlugIn[index.intValue()]);
                                if (this.isTrayPlugIn[index.intValue()]) {
                                    mtkLog("notifyIccIdForSimPlugIn, index=" + index);
                                    notifyIccIdForSimPlugIn(index.intValue());
                                    broadcastCardHotSwapState(index.intValue());
                                    this.isSimPlugIn[index.intValue()] = true;
                                    this.isTrayPlugIn[index.intValue()] = false;
                                    break;
                                } else {
                                    mtkLog("not call notifyIccIdForSimPlugIn, index=" + index);
                                    saveSimPlugState(index.intValue(), 164);
                                    break;
                                }
                            default:
                                switch (i) {
                                    case 110:
                                        mtkLog("Received EVENT_REPOLL_SML_STATE");
                                        AsyncResult ar = (AsyncResult) msg.obj;
                                        if (msg.arg1 == 1) {
                                            needIntent = true;
                                        }
                                        onGetIccCardStatusDone(ar, index);
                                        if (needIntent) {
                                            UiccCardApplication app = getUiccCardApplication(index.intValue(), 1);
                                            if (app == null) {
                                                mtkLog("UiccCardApplication = null");
                                                break;
                                            } else {
                                                if (app.getState() == IccCardApplicationStatus.AppState.APPSTATE_SUBSCRIPTION_PERSO) {
                                                    Intent lockIntent = new Intent();
                                                    mtkLog("Broadcast ACTION_UNLOCK_SIM_LOCK");
                                                    lockIntent.setAction("com.mediatek.phone.ACTION_UNLOCK_SIM_LOCK");
                                                    lockIntent.putExtra("ss", "LOCKED");
                                                    lockIntent.putExtra(DataSubConstants.EXTRA_MOBILE_DATA_ENABLE_REASON, parsePersoType(app.getPersoSubState()));
                                                    SubscriptionManager.putPhoneIdAndSubIdExtra(lockIntent, index.intValue());
                                                    this.mContext.sendBroadcast(lockIntent);
                                                }
                                                break;
                                            }
                                        }
                                        break;
                                    case 111:
                                        mtkLog("handleMessage (EVENT_COMMON_SLOT_NO_CHANGED)");
                                        Intent intentNoChanged = new Intent("com.mediatek.phone.ACTION_COMMON_SLOT_NO_CHANGED");
                                        int slotId = index.intValue();
                                        SubscriptionManager.putPhoneIdAndSubIdExtra(intentNoChanged, slotId);
                                        mtkLog("Broadcasting intent ACTION_COMMON_SLOT_NO_CHANGED for mSlotId : " + slotId);
                                        this.mContext.sendBroadcast(intentNoChanged);
                                        break;
                                    case 112:
                                        mtkLog("handleMessage (EVENT_SML_SLOT_LOCK_INFO_CHANGED)");
                                        AsyncResult ar2 = (AsyncResult) msg.obj;
                                        IOppoUiccManager uiccManager = OppoTelephonyFactory.getInstance().getFeature(IOppoUiccManager.DEFAULT, new Object[0]);
                                        if (!(uiccManager == null || (simlockMgr = uiccManager.getOppoSimlockManager()) == null)) {
                                            mtkLog("OppoSimlockManager onSmlSlotLoclInfoChaned");
                                            simlockMgr.onSmlSlotLoclInfoChaned(ar2, index);
                                        }
                                        onSmlSlotLoclInfoChaned(ar2, index);
                                        triggerUpdateInternalSimMountState(index.intValue());
                                        break;
                                    case 113:
                                        mtkLog("handleMessage (EVENT_SUPPLY_DEVICE_LOCK_DONE)");
                                        int attemptsRemaining = -1;
                                        AsyncResult ar3 = (AsyncResult) msg.obj;
                                        if (ar3.result != null) {
                                            attemptsRemaining = parseUnlockDeviceResult(ar3);
                                        }
                                        Message response = (Message) ar3.userObj;
                                        AsyncResult.forMessage(response).exception = ar3.exception;
                                        response.arg1 = attemptsRemaining;
                                        response.sendToTarget();
                                        break;
                                    case 114:
                                        AsyncResult ar4 = (AsyncResult) msg.obj;
                                        if (ar4.exception == null) {
                                            if (ar4.result != null) {
                                                int[] state = (int[]) ar4.result;
                                                if (state.length == 1) {
                                                    this.mSimPower[index.intValue()] = state[0];
                                                    if (state[0] == 10 || state[0] == 11) {
                                                        this.mSimPowerExecutingState[index.intValue()] = -1;
                                                    }
                                                }
                                                mtkLog("Received EVENT_SIM_POWER_CHANGED, index: " + index + " simPower: " + this.mSimPower[index.intValue()] + " mSimPowerExecutingState = " + this.mSimPowerExecutingState[index.intValue()]);
                                                SendbroadcastSimInfoContentChanged();
                                                broadcastManualProvisionStatusChanged(index.intValue(), this.mSimPower[index.intValue()]);
                                                break;
                                            }
                                        }
                                        Rlog.e(LOG_TAG_EX, "EVENT_SIM_POWER_CHANGED exception");
                                        return;
                                    case EVENT_CARD_DETECTED_IND /* 115 */:
                                        Intent cardDetectedInd = new Intent("com.mediatek.phone.ACTION_CARD_DETECTED");
                                        int slotId2 = index.intValue();
                                        SubscriptionManager.putPhoneIdAndSubIdExtra(cardDetectedInd, slotId2);
                                        mtkLog("Broadcasting intent ACTION_CARD_DETECTED, mSlotId : " + slotId2);
                                        this.mContext.sendBroadcast(cardDetectedInd);
                                        break;
                                    default:
                                        MtkUiccController.super.handleMessage(msg);
                                        break;
                                }
                        }
                    } else if (ignoreGetSimStatus()) {
                        mtkLog("FlightMode ON, Modem OFF: ignore get sim status, index: " + index);
                    } else {
                        MtkUiccController.super.handleMessage(msg);
                    }
                    return;
                }
            }
            Rlog.e(LOG_TAG_EX, "Invalid index : " + index + " received with event " + msg.what);
        }
    }

    private void onSmlSlotLoclInfoChaned(AsyncResult ar, Integer index) {
        if (ar.exception != null || ar.result == null) {
            Rlog.e(LOG_TAG_EX, "onSmlSlotLoclInfoChaned exception");
            return;
        }
        int[] info = (int[]) ar.result;
        if (info.length != 4) {
            Rlog.e(LOG_TAG_EX, "onSmlSlotLoclInfoChaned exception");
            return;
        }
        mtkLog("onSmlSlotLoclInfoChaned, infomation:,lock policy:" + info[0] + ",lock state:" + info[1] + ",service capability:" + info[2] + ",sim valid:" + info[3]);
        Intent smlLockInfoChanged = new Intent("com.mediatek.phone.ACTION_SIM_SLOT_LOCK_POLICY_INFORMATION");
        int slotId = index.intValue();
        SubscriptionManager.putPhoneIdAndSubIdExtra(smlLockInfoChanged, slotId);
        smlLockInfoChanged.putExtra("slot", slotId);
        smlLockInfoChanged.putExtra("DEVICE_LOCK_POLICY", info[0]);
        smlLockInfoChanged.putExtra("DEVICE_LOCK_STATE", info[1]);
        smlLockInfoChanged.putExtra("SIM_SERVICE_CAPABILITY", info[2]);
        smlLockInfoChanged.putExtra("SIM_VALID", info[3]);
        mtkLog("Broadcasting intent ACTION_SIM_SLOT_LOCK_POLICY_INFORMATION for mSlotId : " + slotId);
        this.mContext.sendBroadcastAsUser(smlLockInfoChanged, UserHandle.ALL);
    }

    private void triggerUpdateInternalSimMountState(int phoneId) {
        MtkSubscriptionInfoUpdater subInfoUpdator = PhoneFactory.getSubscriptionInfoUpdater();
        if (subInfoUpdator != null) {
            subInfoUpdator.triggerUpdateInternalSimMountState(phoneId);
        } else {
            mtkLog("subInfoUpdate is null.");
        }
    }

    public void supplyDeviceNetworkDepersonalization(String pwd, Message onComplete) {
        this.mCis[0].supplyDeviceNetworkDepersonalization(pwd, obtainMessage(113, onComplete));
    }

    private int parseUnlockDeviceResult(AsyncResult ar) {
        int[] result = (int[]) ar.result;
        if (result == null) {
            return -1;
        }
        int attemptsRemaining = -1;
        if (result.length > 0) {
            attemptsRemaining = result[0];
        }
        mtkLog("parseUnlockDeviceResult: attemptsRemaining=" + attemptsRemaining);
        return attemptsRemaining;
    }

    private void setNotificationVirtual(int slot, int notifyType) {
        String title;
        mtkLog("setNotificationVirtual(): notifyType = " + notifyType);
        Notification notification = new Notification();
        notification.when = System.currentTimeMillis();
        notification.flags = 16;
        notification.icon = 17301642;
        notification.contentIntent = PendingIntent.getActivity(this.mContext, 0, new Intent(), 134217728);
        if (TelephonyManager.getDefault().getSimCount() > 1) {
            title = Resources.getSystem().getText(this.UICCCONTROLLER_STRING_NOTIFICATION_VIRTUAL_SIM_ON[slot]).toString();
        } else {
            title = Resources.getSystem().getText(134545752).toString();
        }
        CharSequence detail = this.mContext.getText(134545752).toString();
        notification.tickerText = this.mContext.getText(134545752).toString();
        notification.setLatestEventInfo(this.mContext, title, detail, notification.contentIntent);
        ((NotificationManager) this.mContext.getSystemService("notification")).notify(notifyType + slot, notification);
    }

    private void removeNotificationVirtual(int slot, int notifyType) {
        ((NotificationManager) this.mContext.getSystemService("notification")).cancel(notifyType + slot);
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.mediatek.internal.telephony.uicc.MtkUiccController$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$uicc$IccCardApplicationStatus$PersoSubState = new int[IccCardApplicationStatus.PersoSubState.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$uicc$IccCardApplicationStatus$PersoSubState[IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_SIM_NETWORK.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$uicc$IccCardApplicationStatus$PersoSubState[IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_SIM_NETWORK_SUBSET.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$uicc$IccCardApplicationStatus$PersoSubState[IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_SIM_CORPORATE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$uicc$IccCardApplicationStatus$PersoSubState[IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_SIM_SERVICE_PROVIDER.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$uicc$IccCardApplicationStatus$PersoSubState[IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_SIM_SIM.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    private String parsePersoType(IccCardApplicationStatus.PersoSubState state) {
        mtkLog("parsePersoType, state = " + state);
        int i = AnonymousClass1.$SwitchMap$com$android$internal$telephony$uicc$IccCardApplicationStatus$PersoSubState[state.ordinal()];
        if (i == 1) {
            return "NETWORK";
        }
        if (i == 2) {
            return "NETWORK_SUBSET";
        }
        if (i == 3) {
            return "CORPORATE";
        }
        if (i == 4) {
            return "SERVICE_PROVIDER";
        }
        if (i != 5) {
            return "UNKNOWN";
        }
        return "SIM";
    }

    public void repollIccStateForModemSmlChangeFeatrue(int slotId, boolean needIntent) {
        mtkLog("repollIccStateForModemSmlChangeFeatrue, needIntent = " + needIntent);
        int arg1 = 1;
        if (!needIntent) {
            arg1 = 0;
        }
        this.mCis[slotId].getIccCardStatus(obtainMessage(110, arg1, 0, Integer.valueOf(slotId)));
    }

    public boolean ignoreGetSimStatus() {
        int airplaneMode = Settings.Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0);
        mtkLog("ignoreGetSimStatus(): airplaneMode - " + airplaneMode);
        if (!RadioManager.isFlightModePowerOffModemEnabled() || airplaneMode != 1) {
            return false;
        }
        mtkLog("ignoreGetSimStatus(): return true");
        return true;
    }

    /* access modifiers changed from: protected */
    public void mtkLog(String string) {
        Rlog.d(LOG_TAG_EX, string);
    }

    public boolean isAllRadioAvailable() {
        boolean isRadioReady = true;
        for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++) {
            if (2 == this.mCis[i].getRadioState()) {
                isRadioReady = false;
            }
        }
        mtkLog("isAllRadioAvailable = " + isRadioReady);
        return isRadioReady;
    }

    public void resetRadioForVsim() {
        mtkLog("resetRadioForVsim...resetRadio");
        this.mCis[RadioCapabilitySwitchUtil.getMainCapabilityPhoneId()].restartRILD(null);
    }

    public static MtkIccCardConstants.VsimType getVsimCardType(int slotId) {
        int rSim = SystemProperties.getInt("vendor.gsm.prefered.rsim.slot", -1);
        int akaSim = SystemProperties.getInt("vendor.gsm.prefered.aka.sim.slot", -1);
        boolean isVsim = false;
        TelephonyManager.getDefault();
        String inserted = TelephonyManager.getTelephonyProperty(slotId, "vendor.gsm.external.sim.inserted", "0");
        if (inserted != null && inserted.length() > 0 && !"0".equals(inserted)) {
            isVsim = true;
        }
        if (slotId == rSim && isVsim) {
            return MtkIccCardConstants.VsimType.REMOTE_SIM;
        }
        if (slotId == akaSim) {
            if (isVsim) {
                return MtkIccCardConstants.VsimType.SOFT_AKA_SIM;
            }
            return MtkIccCardConstants.VsimType.PHYSICAL_AKA_SIM;
        } else if (rSim == -1 && akaSim == -1 && isVsim) {
            return MtkIccCardConstants.VsimType.LOCAL_SIM;
        } else {
            return MtkIccCardConstants.VsimType.PHYSICAL_SIM;
        }
    }

    private class ModemStateChangedReceiver extends BroadcastReceiver {
        private ModemStateChangedReceiver() {
        }

        /* synthetic */ ModemStateChangedReceiver(MtkUiccController x0, AnonymousClass1 x1) {
            this();
        }

        public void onReceive(Context content, Intent intent) {
            if (intent.getAction().equals(RadioManager.ACTION_MODEM_POWER_NO_CHANGE)) {
                for (int i = 0; i < MtkUiccController.this.mCis.length; i++) {
                    MtkUiccController.this.sendMessage(MtkUiccController.this.obtainMessage(1, new Integer(i)));
                    MtkUiccController mtkUiccController = MtkUiccController.this;
                    mtkUiccController.mtkLog("Trigger GET_SIM_STATUS due to modem state changed for slot " + i);
                }
            }
        }
    }

    public void setSimPower(int slotId, int state, Message onComplete) {
        this.mCis[slotId].setSimPower(state, onComplete);
    }

    public int getSimOnOffState(int slotId) {
        if (slotId < 0 || slotId >= this.mSimPower.length) {
            mtkLog("getSimOnOffState: invalid slotId " + slotId);
            return -1;
        }
        boolean onoffAPSupport = SystemProperties.get(PROPERTY_SIM_CARD_ONOFF).equals(MtkGsmCdmaPhone.ACT_TYPE_UTRAN);
        boolean onoffMDSupport = SystemProperties.get(PROPERTY_SIM_ONOFF_SUPPORT).equals("1");
        int onoffState = SystemProperties.getInt(PROPERTY_SIM_ONOFF_STATE[slotId], -1);
        mtkLog("getSimOnOffState slotId = " + slotId + " onoffAPSupport = " + onoffAPSupport + " onoffMDSupport = " + onoffMDSupport + " mSimPower = " + this.mSimPower[slotId] + " onoffState = " + onoffState);
        if (!onoffAPSupport || !onoffMDSupport) {
            return 11;
        }
        int[] iArr = this.mSimPower;
        if (iArr[slotId] == -1) {
            return onoffState;
        }
        return iArr[slotId];
    }

    public void setSimOnOffExecutingState(int slotId, int state) {
        this.mSimPowerExecutingState[slotId] = state;
    }

    public int getSimOnOffExecutingState(int slotId) {
        if (slotId < 0 || slotId >= this.mSimPowerExecutingState.length) {
            mtkLog("getSimOnOffExecutingState: invalid slotId " + slotId);
            return -1;
        }
        mtkLog("getSimOnOffExecutingState slotId = " + slotId + " mSimPowerExecutingState = " + this.mSimPowerExecutingState[slotId]);
        return this.mSimPowerExecutingState[slotId];
    }
}
