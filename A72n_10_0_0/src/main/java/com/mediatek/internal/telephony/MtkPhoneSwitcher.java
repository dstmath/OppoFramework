package com.mediatek.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Looper;
import android.os.Message;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.ITelephonyRegistry;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneSwitcher;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.dataconnection.ApnContext;
import com.android.internal.telephony.dataconnection.DcRequest;
import com.mediatek.internal.telephony.dataconnection.MtkDcHelper;
import com.mediatek.internal.telephony.datasub.SmartDataSwitchAssistant;
import com.mediatek.telephony.MtkTelephonyManagerEx;
import java.util.List;

public class MtkPhoneSwitcher extends PhoneSwitcher {
    private static final int EVENT_CALL_EVALUATE = 1001;
    private static final int EVENT_SIMLOCK_INFO_CHANGED = 1000;
    private static final String LOG_TAG = "MtkPhoneSwitcher";
    static final int SML_SLOT_LOCK_POLICY_LK_SLOTA_RESTRICT_INVALID_ECC_FOR_VALID_NO_SERVICE = 9;
    private static final boolean VDBG = true;
    private static MtkPhoneSwitcher sInstance = null;
    private EndcBearController mEndcBearController;
    private boolean mIsInCall = false;
    private boolean[] mPhoneStateIsSet;
    private final BroadcastReceiver mSimLockChangedReceiver = new BroadcastReceiver() {
        /* class com.mediatek.internal.telephony.MtkPhoneSwitcher.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            MtkPhoneSwitcher.this.obtainMessage(MtkPhoneSwitcher.EVENT_SIMLOCK_INFO_CHANGED).sendToTarget();
        }
    };
    private boolean mTempDataSwitching = false;

    public MtkPhoneSwitcher(int maxActivePhones, int numPhones, Context context, SubscriptionController subscriptionController, Looper looper, ITelephonyRegistry tr, CommandsInterface[] cis, Phone[] phones) {
        super(maxActivePhones, numPhones, context, subscriptionController, looper, tr, cis, phones);
        sInstance = this;
        this.mPhoneStateIsSet = new boolean[numPhones];
        updateHalCommandToUse();
        log("updateHalCommandToUse done");
        for (int i = 0; i < numPhones; i++) {
            this.mPhoneStateIsSet[i] = false;
        }
        if (MtkTelephonyManagerEx.getDefault() != null) {
            log("getSimLockPolicy:" + MtkTelephonyManagerEx.getDefault().getSimLockPolicy());
        }
        this.mEndcBearController = EndcBearController.makeEndcBearController(this.mContext);
    }

    public void onRadioCapChanged(int phoneId) {
        if (this.mHalCommandToUse == 1 || this.mHalCommandToUse == 0) {
            log("onRadioCapChanged: mPhoneStateIsSet[" + phoneId + "] =" + this.mPhoneStateIsSet[phoneId]);
            if (this.mPhoneStateIsSet[phoneId]) {
                MtkPhoneSwitcher.super.onRadioCapChanged(phoneId);
                return;
            }
            return;
        }
        log("onRadioCapChanged: preferred data");
        MtkPhoneSwitcher.super.onRadioCapChanged(phoneId);
    }

    public static boolean acceptRequest(NetworkRequest request, int score) {
        if (ApnContext.getApnTypeFromNetworkRequest(request) != 0) {
            return true;
        }
        log("[acceptRequest] Invalid APN ID request: " + request);
        return false;
    }

    /* access modifiers changed from: protected */
    public NetworkCapabilities makeNetworkFilter() {
        NetworkCapabilities netCap = MtkPhoneSwitcher.super.makeNetworkFilter();
        netCap.addCapability(27);
        return netCap;
    }

    public static MtkPhoneSwitcher getInstance() {
        return sInstance;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r7v0, resolved type: com.mediatek.internal.telephony.MtkPhoneSwitcher */
    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: protected */
    public void updatePreferredDataPhoneId() {
        SmartDataSwitchAssistant mSmartDataSwitchAssistant = SmartDataSwitchAssistant.getInstance();
        Phone voicePhone = findPhoneById(this.mPhoneIdInVoiceCall);
        if (voicePhone != null && voicePhone.isUserDataEnabled() && !this.mIsInCall) {
            log("set mIsInCall to true");
            this.mIsInCall = true;
            mSmartDataSwitchAssistant.registerReEvaluateEvent(this, 1001, null, this.mPhoneIdInVoiceCall);
        }
        if (voicePhone == null && this.mIsInCall) {
            log("set mIsInCall to false");
            this.mIsInCall = false;
            this.mTempDataSwitching = false;
            mSmartDataSwitchAssistant.unregisterReEvaluateEvent(this);
        }
        if (this.mEmergencyOverride != null && findPhoneById(this.mEmergencyOverride.mPhoneId) != null) {
            log("updatePreferredDataPhoneId: preferred data overridden for emergency. phoneId = " + this.mEmergencyOverride.mPhoneId);
            this.mPreferredDataPhoneId = this.mEmergencyOverride.mPhoneId;
        } else if (voicePhone == null || !voicePhone.getDataEnabledSettings().isDataEnabled(17) || !mSmartDataSwitchAssistant.checkIsSwitchAvailable(this.mPhoneIdInVoiceCall)) {
            int subId = getSubIdForDefaultNetworkRequests();
            int phoneId = -1;
            if (SubscriptionManager.isUsableSubIdValue(subId)) {
                int i = 0;
                while (true) {
                    if (i >= this.mNumPhones) {
                        break;
                    } else if (this.mPhoneSubscriptions[i] == subId) {
                        phoneId = i;
                        break;
                    } else {
                        i++;
                    }
                }
            }
            this.mPreferredDataPhoneId = phoneId;
            this.mTempDataSwitching = false;
        } else {
            this.mPreferredDataPhoneId = this.mPhoneIdInVoiceCall;
            this.mTempDataSwitching = true;
        }
        this.mPreferredDataSubId = this.mSubscriptionController.getSubIdUsingPhoneId(this.mPreferredDataPhoneId);
    }

    /* access modifiers changed from: protected */
    public void updateHalCommandToUse() {
        MtkPhoneSwitcher.super.updateHalCommandToUse();
        log("updateHalCommandToUse");
        if (this.mHalCommandToUse == 1 || this.mHalCommandToUse == 0) {
            log("updateHalCommandToUse: The HIDL preferred data not exist, use ALLOW_DATA");
            if (getSimLockMode()) {
                this.mContext.registerReceiver(this.mSimLockChangedReceiver, new IntentFilter("com.mediatek.phone.ACTION_SIM_SLOT_LOCK_POLICY_INFORMATION"));
            }
        }
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == EVENT_SIMLOCK_INFO_CHANGED) {
            StringBuilder sb = new StringBuilder("simLockChange");
            for (int i2 = 0; i2 < this.mNumPhones; i2++) {
                int cap = MtkTelephonyManagerEx.getDefault().getShouldServiceCapability(i2);
                sb.append(" phone[");
                sb.append(i2);
                sb.append("],Capability=");
                sb.append(cap);
            }
            onEvaluate(true, sb.toString());
        } else if (i != 1001) {
            MtkPhoneSwitcher.super.handleMessage(msg);
        } else {
            log("EVENT_CALL_EVALUATE");
            onEvaluate(false, "CALL_EVALUATE");
        }
    }

    /* access modifiers changed from: protected */
    public boolean isCallActive(Phone phone) {
        if (phone == null) {
            return false;
        }
        log("ForegroundCall:" + phone.getForegroundCall().getState() + ", RingingCall:" + phone.getRingingCall().getState() + ", BackgroundCall: " + phone.getBackgroundCall().getState());
        if (phone.getForegroundCall().getState() == Call.State.ACTIVE || phone.getForegroundCall().getState() == Call.State.ALERTING || phone.getForegroundCall().getState() == Call.State.DIALING || phone.getRingingCall().getState().isRinging() || phone.getBackgroundCall().getState().isAlive()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void deactivate(int phoneId) {
        MtkPhoneSwitcher.super.deactivate(phoneId);
        this.mPhoneStateIsSet[phoneId] = true;
    }

    /* access modifiers changed from: protected */
    public void activate(int phoneId) {
        MtkPhoneSwitcher.super.activate(phoneId);
        this.mPhoneStateIsSet[phoneId] = true;
    }

    private boolean isEimsAllowed(NetworkRequest networkRequest) {
        if (networkRequest.networkCapabilities.hasCapability(10)) {
            for (int i = 0; i < this.mNumPhones; i++) {
                if (MtkDcHelper.getInstance().isSimInserted(i)) {
                    loge("isAllowEims, sim is not null");
                    return false;
                }
            }
            return true;
        }
        loge("isAllowEims, NetworkRequest not include EIMS capability");
        return false;
    }

    /* access modifiers changed from: protected */
    public void suggestDefaultActivePhone(List<Integer> newActivePhones) {
        MtkDcHelper dcHelper = MtkDcHelper.getInstance();
        int mainCapPhoneId = RadioCapabilitySwitchUtil.getMainCapabilityPhoneId();
        if (newActivePhones.isEmpty()) {
            log("newActivePhones is empty");
            if (dcHelper.isSimInserted(mainCapPhoneId) && (!getSimLockMode() || getPsAllowedByPhoneId(mainCapPhoneId))) {
                log("newActivePhones mainCapPhoneId=" + mainCapPhoneId);
                newActivePhones.add(Integer.valueOf(mainCapPhoneId));
            }
        }
        logv("mPrioritizedDcRequests" + this.mPrioritizedDcRequests.toString());
        if (newActivePhones.isEmpty()) {
            log("ECC w/o SIM");
            for (DcRequest dcRequest : this.mPrioritizedDcRequests) {
                if (isEimsAllowed(dcRequest.networkRequest)) {
                    log("newActivePhones mainCapPhoneId=" + mainCapPhoneId);
                    newActivePhones.add(Integer.valueOf(mainCapPhoneId));
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public int phoneIdForRequest(NetworkRequest netRequest) {
        int phoneId = MtkPhoneSwitcher.super.phoneIdForRequest(netRequest);
        if ((this.mHalCommandToUse == 1 || this.mHalCommandToUse == 0) && getSimLockMode() && !getPsAllowedByPhoneId(phoneId)) {
            return -1;
        }
        return phoneId;
    }

    public boolean getTempDataSwitchState() {
        return this.mTempDataSwitching && this.mSubscriptionController.getPhoneId(this.mSubscriptionController.getDefaultDataSubId()) != this.mPhoneIdInVoiceCall;
    }

    public boolean getSimLockMode() {
        int policy = MtkTelephonyManagerEx.getDefault().getSimLockPolicy();
        return policy == 1 || policy == 2 || policy == 3 || policy == 4 || policy == 5 || policy == 6 || policy == 7 || policy == 8 || policy == 9;
    }

    public boolean getPsAllowedByPhoneId(int phoneId) {
        int cap = MtkTelephonyManagerEx.getDefault().getShouldServiceCapability(phoneId);
        switch (MtkTelephonyManagerEx.getDefault().getSimLockPolicy()) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
                if (cap == 0 || cap == 2) {
                    return true;
                }
                return false;
            default:
                return true;
        }
    }

    private static void log(String l) {
        Rlog.d(LOG_TAG, l);
    }

    private static void loge(String l) {
        Rlog.e(LOG_TAG, l);
    }

    private static void logv(String l) {
        Rlog.v(LOG_TAG, l);
    }
}
