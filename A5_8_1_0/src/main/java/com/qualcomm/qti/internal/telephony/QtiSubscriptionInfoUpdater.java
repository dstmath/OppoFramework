package com.qualcomm.qti.internal.telephony;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.SubscriptionInfoUpdater;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccCardStatus.CardState;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccController;
import java.util.List;

public class QtiSubscriptionInfoUpdater extends SubscriptionInfoUpdater {
    private static final String ACTION_ALL_ICC_QUERY_DONE = "org.codeaurora.intent.action.ALL_ICC_QUERY_DONE";
    private static final String CARRIER_MODE_CT_CLASS_A = "ct_class_a";
    private static final int EVENT_ADD_SUBINFO_RECORD = 100;
    public static final int EVENT_SIM_READ_DELAY = 30;
    private static final int EVENT_UPDATE_NV_RECORD = 101;
    private static final String ICCID_STRING_FOR_NO_SIM = "";
    private static final String ICCID_STRING_FOR_NV = "DUMMY_NV_ID";
    private static final String LOG_TAG = "QtiSubscriptionInfoUpdater";
    private static final String ROAMING_SETTINGS_CONFIG = "persist.vendor.radio.roamingsettings";
    private static final int mNumPhones = TelephonyManager.getDefault().getPhoneCount();
    private static Phone[] mQtiPhone;
    private static Context sContext = null;
    private static QtiSubscriptionInfoUpdater sInstance = null;
    private boolean isNVSubAvailable = false;
    private String mCarrierMode = SystemProperties.get("persist.radio.carrier_mode", "default");
    private boolean mIsCTClassA = this.mCarrierMode.equals(CARRIER_MODE_CT_CLASS_A);
    private boolean[] mIsRecordUpdateRequired = new boolean[mNumPhones];
    private boolean needEnableRoamingSettings = false;

    static QtiSubscriptionInfoUpdater init(Looper looper, Context context, Phone[] phone, CommandsInterface[] ci) {
        QtiSubscriptionInfoUpdater qtiSubscriptionInfoUpdater;
        synchronized (QtiSubscriptionInfoUpdater.class) {
            if (sInstance == null) {
                sInstance = new QtiSubscriptionInfoUpdater(looper, context, phone, ci);
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            qtiSubscriptionInfoUpdater = sInstance;
        }
        return qtiSubscriptionInfoUpdater;
    }

    public static QtiSubscriptionInfoUpdater getInstance() {
        if (sInstance == null) {
            Log.wtf(LOG_TAG, "getInstance null");
        }
        return sInstance;
    }

    private QtiSubscriptionInfoUpdater(Looper looper, Context context, Phone[] phone, CommandsInterface[] ci) {
        super(looper, context, phone, ci);
        sContext = context;
        for (int index = 0; index < mNumPhones; index++) {
            this.mIsRecordUpdateRequired[index] = false;
        }
        mQtiPhone = phone;
        if (SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("US")) {
            ExpOperatorSwitchUtils.init(sContext);
        }
    }

    public void handleMessage(Message msg) {
        Rlog.d(LOG_TAG, " handleMessage: EVENT:  " + msg.what);
        switch (msg.what) {
            case 1:
                int slotId = msg.obj.userObj.slotId;
                if (mIccId[slotId] == null || mIccId[slotId] == ICCID_STRING_FOR_NO_SIM) {
                    this.mIsRecordUpdateRequired[slotId] = true;
                }
                super.handleMessage(msg);
                return;
            case EVENT_SIM_READ_DELAY /*30*/:
                String operatorVersion = SystemProperties.get("ro.oppo.operator", "OPPO");
                if (ExpOperatorSwitchUtils.isSupportOperatorSwitch(operatorVersion)) {
                    setOperatorConf(operatorVersion);
                    return;
                }
                return;
            case 100:
                handleAddSubInfoRecordEvent(msg.arg1, (String) msg.obj);
                return;
            case 101:
                handleUpdateNVRecord(msg.arg1);
                return;
            default:
                super.handleMessage(msg);
                return;
        }
    }

    public void updateNVRecord(boolean isNVReady, int slotId) {
        Rlog.d(LOG_TAG, "updateNVRecord, isNVReady: " + isNVReady + " slotId: " + slotId);
        this.isNVSubAvailable = isNVReady;
        sendMessage(obtainMessage(101, slotId, -1, null));
    }

    public void handleUpdateNVRecord(int slotId) {
        if (this.isNVSubAvailable) {
            this.mIsRecordUpdateRequired[slotId] = true;
            handleAddSubInfoRecordEvent(slotId, ICCID_STRING_FOR_NV);
            return;
        }
        List<SubscriptionInfo> subInfo = SubscriptionController.getInstance().getSubInfoUsingSlotIndexWithCheck(slotId, false, sContext.getOpPackageName());
        if (subInfo != null) {
            Rlog.d(LOG_TAG, "handleUpdateNVRecord, active IccID: " + ((SubscriptionInfo) subInfo.get(0)).getIccId());
            if (((SubscriptionInfo) subInfo.get(0)).getIccId().equals(ICCID_STRING_FOR_NV)) {
                handleSimAbsentOrError(slotId, "ABSENT");
            }
        }
    }

    void addSubInfoRecord(int slotId, String iccId) {
        if (iccId == null || slotId < 0 || slotId >= mNumPhones) {
            Rlog.e(LOG_TAG, "addSubInfoRecord, invalid input IccId[" + slotId + "] = " + iccId);
        } else {
            sendMessage(obtainMessage(100, slotId, -1, iccId));
        }
    }

    /* JADX WARNING: Missing block: B:32:0x00d0, code:
            return;
     */
    /* JADX WARNING: Missing block: B:35:0x00db, code:
            if ((mIccId[r4].equals(r5) ^ 1) != 0) goto L_0x0063;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void handleAddSubInfoRecordEvent(int slotId, String iccId) {
        if (mIccId[slotId] == null || mIccId[slotId].equals(ICCID_STRING_FOR_NO_SIM) || !mIccId[slotId].equals(iccId)) {
            if (!(mIccId[slotId] == null || mIccId[slotId] == ICCID_STRING_FOR_NO_SIM)) {
            }
            this.mIsRecordUpdateRequired[slotId] = true;
            mIccId[slotId] = iccId;
            Rlog.d(LOG_TAG, " slotId = " + slotId + ", iccId = " + iccId + " needEnableRoamingSettings = " + this.needEnableRoamingSettings);
            if (!TextUtils.isEmpty(iccId) && isCtCard(iccId) && SystemProperties.getBoolean(ROAMING_SETTINGS_CONFIG, false)) {
                setRoamingSettingsState(sContext, true);
                this.needEnableRoamingSettings = true;
            } else if (!this.needEnableRoamingSettings) {
                setRoamingSettingsState(sContext, false);
            }
            if (this.mIsCTClassA && slotId == 0) {
                checkUiccCard(iccId);
            }
            if (isAllIccIdQueryDone()) {
                updateSubscriptionInfoByIccId();
            }
        } else {
            Rlog.d(LOG_TAG, "Record already exists ignore duplicate update, existing IccId = " + mIccId[slotId] + " recvd iccId[" + slotId + "] = " + iccId);
        }
    }

    private void sendBroadCastToApp() {
        Intent intent = new Intent();
        intent.setClassName("com.qualcomm.qti.networksetting", "com.qualcomm.qti.networksetting.SimAlertNotification");
        Rlog.d(LOG_TAG, "Sending broadcast to NetworkSetting" + intent);
        sContext.sendBroadcast(intent);
    }

    private void checkUiccCard(String iccId) {
        if (isCtCard(iccId)) {
            UiccCard uiccCard = UiccController.getInstance().getUiccCard(0);
            if (uiccCard != null && uiccCard.getCardState() == CardState.CARDSTATE_PRESENT) {
                boolean hasUiccApp;
                if (!uiccCard.isApplicationOnIcc(AppType.APPTYPE_USIM)) {
                    hasUiccApp = false;
                } else if (uiccCard.isApplicationOnIcc(AppType.APPTYPE_CSIM)) {
                    hasUiccApp = true;
                } else {
                    hasUiccApp = uiccCard.isApplicationOnIcc(AppType.APPTYPE_RUIM);
                }
                if (!hasUiccApp) {
                    Rlog.d(LOG_TAG, "This is a 3G CT card.");
                    sendBroadCastToApp();
                    return;
                }
                return;
            }
            return;
        }
        Rlog.d(LOG_TAG, "This is a non-CT card.");
        sendBroadCastToApp();
    }

    private boolean isCtCard(String iccId) {
        String subIccId = iccId.substring(0, 6);
        if ("898603".equals(subIccId) || "898611".equals(subIccId) || "8985231".equals(subIccId) || "8985302".equals(subIccId) || "8985307".equals(subIccId)) {
            return true;
        }
        return false;
    }

    private void setRoamingSettingsState(Context context, boolean install) {
        if (context == null) {
            Rlog.d(LOG_TAG, "setRoamingSettingsState, context null");
            return;
        }
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            Rlog.d(LOG_TAG, "setRoamingSettingsState, PackageManager null");
            return;
        }
        int state;
        String packageName = "com.qualcomm.qti.roamingsettings";
        ComponentName cn = new ComponentName(packageName, "com.qualcomm.qti.roamingsettings.RoamingSettingsActivity");
        if (install) {
            state = 1;
        } else {
            state = 2;
        }
        for (PackageInfo pi : pm.getInstalledPackages(8192)) {
            if (!TextUtils.isEmpty(pi.packageName) && packageName.equals(pi.packageName)) {
                Rlog.d(LOG_TAG, "setRoamingSettings state = " + state);
                pm.setComponentEnabledSetting(cn, state, 0);
            }
        }
    }

    protected void handleSimLoaded(int slotId) {
        if (mIccId[slotId] == null || mIccId[slotId] == ICCID_STRING_FOR_NO_SIM) {
            this.mIsRecordUpdateRequired[slotId] = true;
        }
        super.handleSimLoaded(slotId);
    }

    protected void handleSimAbsentOrError(int slotId, String simState) {
        if (!this.isNVSubAvailable) {
            if (mIccId[slotId] == null || mIccId[slotId] != ICCID_STRING_FOR_NO_SIM) {
                this.mIsRecordUpdateRequired[slotId] = true;
            }
            super.handleSimAbsentOrError(slotId, simState);
        }
    }

    protected synchronized void updateSubscriptionInfoByIccId() {
        int index;
        boolean isUpdateRequired = false;
        for (index = 0; index < mNumPhones; index++) {
            if (this.mIsRecordUpdateRequired[index]) {
                isUpdateRequired = true;
                break;
            }
        }
        if (isUpdateRequired) {
            super.updateSubscriptionInfoByIccId();
            Rlog.d(LOG_TAG, "SIM state changed, Updating user preference ");
            if (QtiUiccCardProvisioner.getInstance().isAllCardProvisionInfoReceived()) {
                QtiSubscriptionController.getInstance().updateUserPreferences();
            }
            for (index = 0; index < mNumPhones; index++) {
                this.mIsRecordUpdateRequired[index] = false;
            }
            String operatorVersion = SystemProperties.get("ro.oppo.operator", "OPPO");
            if (ExpOperatorSwitchUtils.isSupportOperatorSwitch(operatorVersion)) {
                setOperatorConf(operatorVersion);
            }
        } else {
            Rlog.d(LOG_TAG, "Ignoring subscription update event");
        }
    }

    private void setOperatorConf(String version) {
        if (ExpOperatorSwitchUtils.isFirstInsertSim()) {
            boolean isSpecOperator = false;
            boolean isInsertSim = false;
            for (int slot = 0; slot < TelephonyManager.getDefault().getPhoneCount(); slot++) {
                if (!mIccId[slot].equals(ICCID_STRING_FOR_NO_SIM)) {
                    IccRecords records = mQtiPhone[slot].getIccCard().getIccRecords();
                    if (records != null) {
                        String operator = records.getOperatorNumeric();
                        if (operator != null) {
                            ExpOperatorSwitchUtils.setFirstInsertSimFlag(1);
                            isInsertSim = true;
                            Rlog.d(LOG_TAG, "setOperatorConf, slot = " + slot + " operator=" + operator);
                            if (ExpOperatorSwitchUtils.oppoIsSpecOperator(operator, slot, version)) {
                                isSpecOperator = true;
                                break;
                            }
                        }
                    }
                    sendMessageDelayed(obtainMessage(30), 3000);
                    return;
                }
            }
            boolean hotStatus = QtiUiccCardProvisioner.getInstance().getSimHotSwapPlugInState();
            if (!isSpecOperator && isInsertSim && hotStatus) {
                ExpOperatorSwitchUtils.oppoBroadCastDelayHotswap();
            }
            return;
        }
        Rlog.d(LOG_TAG, "setOperatorConf, not first insert simcard!!");
    }
}
