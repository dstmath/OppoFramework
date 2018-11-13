package com.qualcomm.qti.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncResult;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.PhoneConstants.State;
import com.android.internal.telephony.SubscriptionController;
import java.util.Iterator;
import java.util.List;

public class QtiSubscriptionController extends SubscriptionController {
    private static final String ACTION_SUBSCRIPTION_RECORD_ADDED = "org.codeaurora.intent.action.SUBSCRIPTION_INFO_RECORD_ADDED";
    private static final String APM_SIM_NOT_PWDN_PROPERTY = "persist.vendor.radio.apm_sim_not_pwdn";
    private static final String CARRIER_MODE_CT_CLASS_A = "ct_class_a";
    private static final int DUMMY_SUB_ID_BASE = 2147483643;
    private static final int EVENT_CALL_ENDED = 101;
    static final String LOG_TAG = "QtiSubscriptionController";
    private static final int NOT_PROVISIONED = 0;
    private static final int PROVISIONED = 1;
    private static final String SETTING_USER_PREF_DATA_SUB = "user_preferred_data_sub";
    private static CommandsInterface[] sCi = null;
    private static int sNumPhones;
    private RegistrantList mAddSubscriptionRecordRegistrants = new RegistrantList();
    private CallManager mCallManager;
    private String mCarrierMode = SystemProperties.get("persist.radio.carrier_mode", "default");
    private int mCurrentDdsSubId = DUMMY_SUB_ID_BASE;
    private boolean mIsCTClassA = this.mCarrierMode.equals(CARRIER_MODE_CT_CLASS_A);
    private Handler mSubscriptionHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 101:
                    QtiSubscriptionController.this.logd("EVENT_CALL_ENDED");
                    if (!(QtiSubscriptionController.this.isActiveSubId(QtiSubscriptionController.this.mCurrentDdsSubId) && (QtiSubscriptionController.this.isSubProvisioned(QtiSubscriptionController.this.mCurrentDdsSubId) ^ 1) == 0)) {
                        QtiSubscriptionController.this.logd("Current dds sub is inactive");
                        QtiSubscriptionController.this.mCurrentDdsSubId = QtiSubscriptionController.mDefaultFallbackSubId;
                    }
                    QtiSubscriptionController.this.logd("Set DDS to : " + QtiSubscriptionController.this.mCurrentDdsSubId);
                    QtiSubscriptionController.this.setDefaultDataSubId(QtiSubscriptionController.this.mCurrentDdsSubId);
                    CallManager.getInstance().unregisterForDisconnect(QtiSubscriptionController.this.mSubscriptionHandler);
                    return;
                default:
                    return;
            }
        }
    };
    private TelecomManager mTelecomManager;
    private TelephonyManager mTelephonyManager;

    public static QtiSubscriptionController init(Context c, CommandsInterface[] ci) {
        QtiSubscriptionController qtiSubscriptionController;
        synchronized (QtiSubscriptionController.class) {
            if (sInstance == null) {
                sInstance = new QtiSubscriptionController(c);
                sCi = ci;
                sNumPhones = TelephonyManager.getDefault().getPhoneCount();
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            qtiSubscriptionController = (QtiSubscriptionController) sInstance;
        }
        return qtiSubscriptionController;
    }

    public static QtiSubscriptionController getInstance() {
        if (sInstance == null) {
            Log.wtf(LOG_TAG, "getInstance null");
        }
        return (QtiSubscriptionController) sInstance;
    }

    private QtiSubscriptionController(Context c) {
        super(c);
        logd(" init by Context");
        mDefaultPhoneId = 0;
        mDefaultFallbackSubId = DUMMY_SUB_ID_BASE;
        this.mTelecomManager = TelecomManager.from(this.mContext);
        this.mTelephonyManager = TelephonyManager.from(this.mContext);
        this.mCallManager = CallManager.getInstance();
    }

    private void clearVoiceSubId() {
        List<SubscriptionInfo> records = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
        logdl("[clearVoiceSubId] records: " + records);
        if (shouldDefaultBeCleared(records, getDefaultVoiceSubId())) {
            logdl("[clearVoiceSubId] clear voice sub id");
            setDefaultVoiceSubId(DUMMY_SUB_ID_BASE);
        }
    }

    public int getSlotIndex(int subId) {
        if (VDBG) {
            printStackTrace("[getSlotIndex] subId=" + subId);
        }
        if (subId == Integer.MAX_VALUE) {
            subId = getDefaultSubId();
        }
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            logd("[getSlotIndex]- subId invalid");
            return -1;
        } else if (subId < DUMMY_SUB_ID_BASE) {
            return super.getSlotIndex(subId);
        } else {
            logd("getPhoneId, received dummy subId " + subId);
            return getPhoneIdFromDummySubId(subId);
        }
    }

    public int getPhoneId(int subId) {
        if (VDBG) {
            printStackTrace("[getPhoneId] subId=" + subId);
        }
        if (subId == Integer.MAX_VALUE) {
            subId = getDefaultSubId();
            logdl("[getPhoneId] asked for default subId=" + subId);
        }
        if (!SubscriptionManager.isValidSubscriptionId(subId)) {
            logdl("[getPhoneId]- invalid subId return=-1");
            return -1;
        } else if (subId < DUMMY_SUB_ID_BASE) {
            return super.getPhoneId(subId);
        } else {
            logd("getPhoneId, received dummy subId " + subId);
            return getPhoneIdFromDummySubId(subId);
        }
    }

    private int getPhoneIdFromDummySubId(int subId) {
        return subId - DUMMY_SUB_ID_BASE;
    }

    protected int[] getDummySubIds(int slotIdx) {
        int numSubs = getActiveSubInfoCountMax();
        if (numSubs <= 0) {
            return null;
        }
        int[] dummyValues = new int[numSubs];
        for (int i = 0; i < numSubs; i++) {
            dummyValues[i] = DUMMY_SUB_ID_BASE + slotIdx;
        }
        if (VDBG) {
            logd("getDummySubIds: slotIdx=" + slotIdx + " return " + numSubs + " DummySubIds with each subId=" + dummyValues[0]);
        }
        return dummyValues;
    }

    public void registerForAddSubscriptionRecord(Handler handler, int what, Object obj) {
        Registrant r = new Registrant(handler, what, obj);
        synchronized (this.mAddSubscriptionRecordRegistrants) {
            this.mAddSubscriptionRecordRegistrants.add(r);
            if (getActiveSubscriptionInfoList(this.mContext.getOpPackageName()) != null) {
                r.notifyRegistrant();
            }
        }
    }

    public void unregisterForAddSubscriptionRecord(Handler handler) {
        synchronized (this.mAddSubscriptionRecordRegistrants) {
            this.mAddSubscriptionRecordRegistrants.remove(handler);
        }
    }

    public int addSubInfoRecord(String iccId, int slotId) {
        int retVal = super.addSubInfoRecord(iccId, slotId);
        int[] subId = getSubId(slotId);
        if (subId != null && subId.length > 0) {
            logd("addSubInfoRecord: broadcast intent subId[" + slotId + "] = " + subId[0]);
            this.mAddSubscriptionRecordRegistrants.notifyRegistrants(new AsyncResult(null, Integer.valueOf(slotId), null));
            Intent intent = new Intent(ACTION_SUBSCRIPTION_RECORD_ADDED);
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, slotId, subId[0]);
            this.mContext.sendBroadcast(intent);
        }
        return retVal;
    }

    public void setDefaultDataSubId(int subId) {
        enforceModifyPhoneState("setDefaultDataSubId");
        String flexMapSupportType = SystemProperties.get("persist.radio.flexmap_type", "nw_mode");
        if (QtiPhoneSwitcher.isLplusLSupported && isVoiceCallActive() && (QtiDdsSwitchController.isTempDdsSwitchRequired() ^ 1) != 0) {
            logd("Active call, cannot set Dds to : " + subId);
            this.mCurrentDdsSubId = subId;
            this.mCallManager.registerForDisconnect(this.mSubscriptionHandler, 101, null);
            return;
        }
        if (SubscriptionManager.isValidSubscriptionId(subId) && flexMapSupportType.equals("dds")) {
            QtiRadioCapabilityController radioCapController = QtiRadioCapabilityController.getInstance();
            if (radioCapController.isBothPhonesMappedToSameStack()) {
                radioCapController.initNormalMappingRequest();
                logd(" setDefaultDataSubId init normal mapping: " + subId);
            }
            super.setDefaultDataSubId(subId);
        } else {
            int oldDdsSubId = getDefaultDataSubId();
            logd("qti oldDdsSubId = " + oldDdsSubId + " , to set current dds subId = " + subId);
            if (oldDdsSubId != subId) {
                setSwitchingDssState(0, true);
                setSwitchingDssState(1, true);
            }
            updateAllDataConnectionTrackers();
            Global.putInt(this.mContext.getContentResolver(), "multi_sim_data_call", subId);
            broadcastDefaultDataSubIdChanged(subId);
        }
    }

    public void clearDefaultsForInactiveSubIds() {
        enforceModifyPhoneState("clearDefaultsForInactiveSubIds");
        long identity = Binder.clearCallingIdentity();
        try {
            List<SubscriptionInfo> records = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            logdl("[clearDefaultsForInactiveSubIds] records: " + records);
            if (shouldDefaultBeCleared(records, getDefaultDataSubId())) {
                logd("[clearDefaultsForInactiveSubIds] clearing default data sub id");
                setDefaultDataSubId(-1);
            }
            if (shouldDefaultBeCleared(records, getDefaultSmsSubId())) {
                logdl("[clearDefaultsForInactiveSubIds] clearing default sms sub id");
                setDefaultSmsSubId(-1);
            }
            if (shouldDefaultBeCleared(records, getDefaultVoiceSubId())) {
                logdl("[clearDefaultsForInactiveSubIds] clearing default voice sub id");
                setDefaultVoiceSubId(DUMMY_SUB_ID_BASE);
            }
            Binder.restoreCallingIdentity(identity);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    protected boolean shouldDefaultBeCleared(List<SubscriptionInfo> records, int subId) {
        logdl("[shouldDefaultBeCleared: subId] " + subId);
        if (records == null) {
            logdl("[shouldDefaultBeCleared] return true no records subId=" + subId);
            return true;
        } else if (SubscriptionManager.isValidSubscriptionId(subId)) {
            for (SubscriptionInfo record : records) {
                int id = record.getSubscriptionId();
                logdl("[shouldDefaultBeCleared] Record.id: " + id);
                if (id == subId) {
                    logdl("[shouldDefaultBeCleared] return false subId is active, subId=" + subId);
                    return false;
                }
            }
            if (getUiccProvisionStatus(getSlotIndex(subId)) == 1) {
                logdl("[shouldDefaultBeCleared] return false subId is provisioned, subId=" + subId);
                return false;
            }
            logdl("[shouldDefaultBeCleared] return true not active subId=" + subId);
            return true;
        } else {
            logdl("[shouldDefaultBeCleared] return false only one subId, subId=" + subId);
            return false;
        }
    }

    private boolean isRadioAvailableOnAllSubs() {
        int i = 0;
        while (i < sNumPhones) {
            if (sCi != null && (sCi[i].getRadioState().isAvailable() ^ 1) != 0) {
                return false;
            }
            i++;
        }
        return true;
    }

    private boolean isShuttingDown() {
        int i = 0;
        while (i < sNumPhones) {
            if (sPhones[i] != null && sPhones[i].isShuttingDown()) {
                return true;
            }
            i++;
        }
        return false;
    }

    public boolean isRadioInValidState() {
        boolean isApmSimNotPwrDown = SystemProperties.getInt(APM_SIM_NOT_PWDN_PROPERTY, 0) == 1;
        int isAPMOn = Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0);
        if (isAPMOn == 1 && (isApmSimNotPwrDown ^ 1) != 0) {
            logd("isRadioInValidState, isApmSimNotPwrDown = " + isApmSimNotPwrDown + ", isAPMOn:" + isAPMOn);
            return false;
        } else if (!isRadioAvailableOnAllSubs()) {
            logd(" isRadioInValidState, radio not available");
            return false;
        } else if (!isShuttingDown()) {
            return true;
        } else {
            logd(" isRadioInValidState: device shutdown in progress ");
            return false;
        }
    }

    /* JADX WARNING: Missing block: B:39:0x00ba, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    synchronized void updateUserPreferences() {
        SubscriptionInfo mNextActivatedSub = null;
        int activeCount = 0;
        if (isRadioInValidState()) {
            List<SubscriptionInfo> sil = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
            if (sil == null || sil.size() < 1) {
                logi("updateUserPreferences: Subscription list is empty");
                clearVoiceSubId();
                setDefaultFallbackSubId(DUMMY_SUB_ID_BASE);
            } else if (SystemProperties.getBoolean("persist.radio.aosp_usr_pref_sel", false)) {
                logi("updateUserPreferences: AOSP user preference option enabled ");
            } else {
                for (SubscriptionInfo subInfo : sil) {
                    if (getUiccProvisionStatus(subInfo.getSimSlotIndex()) == 1) {
                        activeCount++;
                        if (mNextActivatedSub == null) {
                            mNextActivatedSub = subInfo;
                        }
                    }
                }
                logd("updateUserPreferences:: active sub count = " + activeCount + " dds = " + getDefaultDataSubId() + " voice = " + getDefaultVoiceSubId() + " sms = " + getDefaultSmsSubId());
                if (activeCount == 1) {
                    setSMSPromptEnabled(false);
                }
                if (mNextActivatedSub != null && getActiveSubInfoCountMax() != 1) {
                    if (!isSubProvisioned(getDefaultSmsSubId())) {
                        setDefaultSmsSubId(mNextActivatedSub.getSubscriptionId());
                    }
                    if (!isSubProvisioned(getDefaultVoiceSubId())) {
                        setDefaultVoiceSubId(mNextActivatedSub.getSubscriptionId());
                    }
                    if (!isNonSimAccountFound() && activeCount == 1) {
                        int subId = mNextActivatedSub.getSubscriptionId();
                        PhoneAccountHandle phoneAccountHandle = subscriptionIdToPhoneAccountHandle(subId);
                        logi("set default phoneaccount to  " + subId);
                        this.mTelecomManager.setUserSelectedOutgoingPhoneAccount(phoneAccountHandle);
                    }
                    if (!isSubProvisioned(mDefaultFallbackSubId)) {
                        setDefaultFallbackSubId(mNextActivatedSub.getSubscriptionId());
                    }
                    notifySubscriptionInfoChanged();
                    logd("updateUserPreferences: after currentDds = " + getDefaultDataSubId() + " voice = " + getDefaultVoiceSubId() + " sms = " + getDefaultSmsSubId());
                }
            }
        } else {
            logd("Radio is in Invalid state, Ignore Updating User Preference!!!");
        }
    }

    private void handleDataPreference(int nextActiveSubId) {
        int userPrefDataSubId = getUserPrefDataSubIdFromDB();
        int currentDataSubId = getDefaultDataSubId();
        List<SubscriptionInfo> subInfoList = getActiveSubscriptionInfoList(this.mContext.getOpPackageName());
        if (subInfoList != null) {
            boolean userPrefSubValid = false;
            for (SubscriptionInfo subInfo : subInfoList) {
                if (subInfo.getSubscriptionId() == userPrefDataSubId) {
                    userPrefSubValid = true;
                }
            }
            logd("havePrefSub = " + userPrefSubValid + " user pref subId = " + userPrefDataSubId + " current dds " + currentDataSubId + " next active subId " + nextActiveSubId);
            if (this.mIsCTClassA && isActiveSubId(getSubId(0)[0])) {
                setDefaultDataSubId(getSubId(0)[0]);
            } else if (userPrefSubValid && isSubProvisioned(userPrefDataSubId) && currentDataSubId != userPrefDataSubId) {
                setDefaultDataSubId(userPrefDataSubId);
            } else if (!isSubProvisioned(currentDataSubId)) {
                setDefaultDataSubId(nextActiveSubId);
            }
            if (subInfoList.size() > 1 && (userPrefSubValid ^ 1) != 0) {
                saveUserPrefDataSubIdIntoDB(getDefaultDataSubId());
            }
            QtiRadioCapabilityController.getInstance().setDdsIfRequired(false);
        }
    }

    private int getUiccProvisionStatus(int slotId) {
        QtiUiccCardProvisioner uiccCardProvisioner = QtiUiccCardProvisioner.getInstance();
        if (uiccCardProvisioner != null) {
            return uiccCardProvisioner.getCurrentUiccCardProvisioningStatus(slotId);
        }
        return 0;
    }

    private boolean isSubProvisioned(int subId) {
        boolean isSubIdUsable = SubscriptionManager.isUsableSubIdValue(subId);
        if (!isSubIdUsable) {
            return isSubIdUsable;
        }
        int slotId = getSlotIndex(subId);
        if (!SubscriptionManager.isValidSlotIndex(slotId) || subId >= DUMMY_SUB_ID_BASE) {
            loge(" Invalid slotId " + slotId + " or subId = " + subId);
            return false;
        }
        if (getUiccProvisionStatus(slotId) != 1) {
            isSubIdUsable = false;
        }
        loge("isSubProvisioned, state = " + isSubIdUsable + " subId = " + subId);
        return isSubIdUsable;
    }

    public boolean isSMSPromptEnabled() {
        int value = 0;
        try {
            value = Global.getInt(this.mContext.getContentResolver(), "multi_sim_sms_prompt");
        } catch (SettingNotFoundException e) {
            loge("Settings Exception Reading Dual Sim SMS Prompt Values");
        }
        boolean prompt = value != 0;
        if (VDBG) {
            logd("SMS Prompt option:" + prompt);
        }
        return prompt;
    }

    public void setSMSPromptEnabled(boolean enabled) {
        enforceModifyPhoneState("setSMSPromptEnabled");
        Global.putInt(this.mContext.getContentResolver(), "multi_sim_sms_prompt", !enabled ? 0 : 1);
        logi("setSMSPromptOption to " + enabled);
    }

    private boolean isNonSimAccountFound() {
        Iterator<PhoneAccountHandle> phoneAccounts = this.mTelecomManager.getCallCapablePhoneAccounts().listIterator();
        while (phoneAccounts.hasNext()) {
            if (this.mTelephonyManager.getSubIdForPhoneAccount(this.mTelecomManager.getPhoneAccount((PhoneAccountHandle) phoneAccounts.next())) == -1) {
                logi("Other than SIM account found. ");
                return true;
            }
        }
        logi("Other than SIM account not found ");
        return false;
    }

    private PhoneAccountHandle subscriptionIdToPhoneAccountHandle(int subId) {
        Iterator<PhoneAccountHandle> phoneAccounts = this.mTelecomManager.getCallCapablePhoneAccounts().listIterator();
        while (phoneAccounts.hasNext()) {
            PhoneAccountHandle phoneAccountHandle = (PhoneAccountHandle) phoneAccounts.next();
            if (subId == this.mTelephonyManager.getSubIdForPhoneAccount(this.mTelecomManager.getPhoneAccount(phoneAccountHandle))) {
                return phoneAccountHandle;
            }
        }
        return null;
    }

    private int getUserPrefDataSubIdFromDB() {
        return Global.getInt(this.mContext.getContentResolver(), SETTING_USER_PREF_DATA_SUB, -1);
    }

    private void saveUserPrefDataSubIdIntoDB(int subId) {
        Global.putInt(this.mContext.getContentResolver(), SETTING_USER_PREF_DATA_SUB, subId);
    }

    private boolean isVoiceCallActive() {
        boolean ret = this.mCallManager.getState() != State.IDLE;
        logd("isVoiceCallActive: " + ret);
        return ret;
    }

    private void logd(String string) {
        Rlog.d(LOG_TAG, string);
    }

    private void logi(String string) {
        Rlog.i(LOG_TAG, string);
    }

    private void loge(String string) {
        Rlog.e(LOG_TAG, string);
    }
}
