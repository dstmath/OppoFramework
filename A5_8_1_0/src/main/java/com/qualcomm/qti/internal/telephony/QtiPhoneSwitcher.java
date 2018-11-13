package com.qualcomm.qti.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkRequest;
import android.net.NetworkSpecifier;
import android.net.StringNetworkSpecifier;
import android.os.AsyncResult;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.text.TextUtils;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.ITelephonyRegistry;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants.State;
import com.android.internal.telephony.PhoneSwitcher;
import com.android.internal.telephony.PhoneSwitcher.PhoneState;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.dataconnection.DcRequest;
import com.qualcomm.qcrilhook.IQcRilHook;
import com.qualcomm.qti.internal.telephony.primarycard.SubsidyLockSettingsObserver;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class QtiPhoneSwitcher extends PhoneSwitcher {
    public static boolean isLplusLSupported = false;
    private final int EVENT_ALLOW_DATA_FALSE_RESPONSE = 112;
    private final int EVENT_ALLOW_DATA_TRUE_RESPONSE = 113;
    private final int MAX_CONNECT_FAILURE_COUNT = 5;
    private final int NONUSER_INITIATED_SWITCH = 1;
    private final String PROPERTY_TEMP_DDSSWITCH = "persist.radio.enable_temp_dds";
    private final int USER_INITIATED_SWITCH = 0;
    private int[] mAllowDataFailure;
    private CallManager mCm;
    private boolean mDdsSwithBlocked = false;
    private int mDefaultDataPhoneId = -1;
    private boolean mManualDdsSwitch = false;
    private List<Integer> mNewActivePhones;
    private QtiDdsSwitchController mQtiDdsSwitchController;
    private QtiRilInterface mQtiRilInterface;
    private final int[] mRetryArray = new int[]{5, 10, 20, 40, 60};
    private boolean mSendDdsSwitchDoneIntent = false;
    private BroadcastReceiver mSimStateIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.SIM_STATE_CHANGED")) {
                String value = intent.getStringExtra("ss");
                int phoneId = intent.getIntExtra("phone", -1);
                QtiPhoneSwitcher.this.log("mSimStateIntentReceiver: phoneId = " + phoneId + " value = " + value);
                if (phoneId != -1) {
                    QtiPhoneSwitcher.this.mSimStates[phoneId] = value;
                }
                if (QtiPhoneSwitcher.this.isSimReady(phoneId) && QtiPhoneSwitcher.this.getConnectFailureCount(phoneId) > 0) {
                    QtiPhoneSwitcher.this.resendDataAllowed(phoneId);
                }
            }
        }
    };
    private String[] mSimStates;
    private boolean mWaitForDetachResponse = false;

    public QtiPhoneSwitcher(int maxActivePhones, int numPhones, Context context, SubscriptionController subscriptionController, Looper looper, ITelephonyRegistry tr, CommandsInterface[] cis, Phone[] phones) {
        super(maxActivePhones, numPhones, context, subscriptionController, looper, tr, cis, phones);
        this.mAllowDataFailure = new int[numPhones];
        this.mSimStates = new String[numPhones];
        this.mCm = CallManager.getInstance();
        this.mCm.registerForDisconnect(this, 108, null);
        this.mQtiRilInterface = QtiRilInterface.getInstance(context);
        if (this.mQtiRilInterface.isServiceReady()) {
            queryMaxDataAllowed();
            isLplusLSupported = this.mQtiRilInterface.getLpluslSupportStatus();
        } else {
            this.mQtiRilInterface.registerForServiceReadyEvent(this, 111, null);
        }
        this.mQtiRilInterface.registerForUnsol(this, 110, null);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        this.mContext.registerReceiver(this.mSimStateIntentReceiver, filter);
        this.mQtiDdsSwitchController = new QtiDdsSwitchController(numPhones, context, subscriptionController, looper, phones, isLplusLSupported);
    }

    private void queryMaxDataAllowed() {
        this.mMaxActivePhones = this.mQtiRilInterface.getMaxDataAllowed();
    }

    private void handleUnsolMaxDataAllowedChange(Message msg) {
        if (msg == null || msg.obj == null) {
            log("Null data received in handleUnsolMaxDataAllowedChange");
            return;
        }
        ByteBuffer payload = ByteBuffer.wrap((byte[]) msg.obj);
        payload.order(ByteOrder.nativeOrder());
        if (payload.getInt() == IQcRilHook.QCRILHOOK_UNSOL_MAX_DATA_ALLOWED_CHANGED) {
            int response_size = payload.getInt();
            if (response_size < 0) {
                log("Response size is Invalid " + response_size);
            } else {
                this.mMaxActivePhones = payload.get();
                log(" Unsol Max Data Changed to: " + this.mMaxActivePhones);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:36:0x00dc  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleMessage(Message msg) {
        int ddsSubId = this.mSubscriptionController.getDefaultDataSubId();
        int ddsPhoneId = this.mSubscriptionController.getPhoneId(ddsSubId);
        log("handle event - " + msg.what);
        AsyncResult ar;
        switch (msg.what) {
            case SubsidyLockSettingsObserver.SUBSIDY_LOCKED /*101*/:
                boolean isLplTempSwitch = SystemProperties.getBoolean("persist.radio.enable_temp_dds", false);
                if (!isAnyVoiceCallActiveOnDevice() || (isLplTempSwitch ^ 1) == 0) {
                    onEvaluate(false, "defaultChanged");
                    break;
                }
                this.mDdsSwithBlocked = true;
                log("Voice call active. Waiting for call end");
                return;
                break;
            case SubsidyLockSettingsObserver.AP_LOCKED /*102*/:
                broadcastNetworkSpecifier();
                onEvaluate(false, "subChanged");
                break;
            case 108:
                log("EVENT_VOICE_CALL_ENDED");
                sendEmptyMessageDelayed(120, 800);
                break;
            case 110:
                ar = msg.obj;
                if (ar.result == null) {
                    log("Error: empty result, EVENT_UNSOL_MAX_DATA_ALLOWED_CHANGED");
                    break;
                } else {
                    handleUnsolMaxDataAllowedChange((Message) ar.result);
                    break;
                }
            case 111:
                ar = msg.obj;
                if (ar.result != null) {
                    if (((Boolean) ar.result).booleanValue()) {
                        queryMaxDataAllowed();
                        isLplusLSupported = this.mQtiRilInterface.getLpluslSupportStatus();
                        this.mQtiDdsSwitchController.updateLplusLStatus(isLplusLSupported);
                        break;
                    }
                }
                log("Error: empty result, EVENT_OEM_HOOK_SERVICE_READY");
                break;
                break;
            case 112:
                log("EVENT_ALLOW_DATA_FALSE_RESPONSE");
                this.mWaitForDetachResponse = false;
                informDdsToRil(ddsSubId);
                for (Integer intValue : this.mNewActivePhones) {
                    activate(intValue.intValue());
                }
                if (this.mNewActivePhones.contains(Integer.valueOf(ddsPhoneId))) {
                    this.mManualDdsSwitch = false;
                    break;
                }
                break;
            case 113:
                onAllowDataResponse(msg.arg1, (AsyncResult) msg.obj);
                break;
            case 120:
                log("EVENT_VOICE_CALL_ENDED_DELAYED");
                if (!isAnyVoiceCallActiveOnDevice()) {
                    int i = 0;
                    while (i < this.mNumPhones) {
                        if (getConnectFailureCount(i) > 0 && isPhoneIdValidForRetry(i)) {
                            resendDataAllowed(i);
                            if (this.mDdsSwithBlocked) {
                                sendEmptyMessageDelayed(SubsidyLockSettingsObserver.SUBSIDY_LOCKED, 5000);
                                this.mDdsSwithBlocked = false;
                                break;
                            }
                        }
                        i++;
                    }
                    if (this.mDdsSwithBlocked) {
                    }
                }
                break;
            default:
                super.handleMessage(msg);
                break;
        }
    }

    private void broadcastNetworkSpecifier() {
        ArrayList<Integer> subIdList = new ArrayList();
        int i = 0;
        while (i < this.mNumPhones) {
            int[] subId = this.mSubscriptionController.getSubId(i);
            if (subId != null && subId.length > 0 && this.mSubscriptionController.isActiveSubId(subId[0]) && isUiccProvisioned(i)) {
                subIdList.add(Integer.valueOf(subId[0]));
            }
            i++;
        }
        if (subIdList.size() > 0) {
            Intent intent = new Intent("org.codeaurora.intent.action.ACTION_NETWORK_SPECIFIER_SET");
            intent.putIntegerArrayListExtra("SubIdList", subIdList);
            log("Broadcast network specifier set intent");
            this.mContext.sendBroadcast(intent);
        }
    }

    private boolean isSimReady(int phoneId) {
        if (phoneId == -1) {
            return false;
        }
        if (!"READY".equals(this.mSimStates[phoneId]) && !"LOADED".equals(this.mSimStates[phoneId]) && !"IMSI".equals(this.mSimStates[phoneId])) {
            return false;
        }
        log("SIM READY for phoneId: " + phoneId);
        return true;
    }

    protected void onEvaluate(boolean requestsChanged, String reason) {
        StringBuilder stringBuilder = new StringBuilder(reason);
        if (isEmergency()) {
            log("onEvalute aborted due to Emergency");
            return;
        }
        int i;
        boolean diffDetected = requestsChanged;
        int dataSubId = this.mSubscriptionController.getDefaultDataSubId();
        int ddsPhoneId = this.mSubscriptionController.getPhoneId(dataSubId);
        if ((this.mSubscriptionController.isActiveSubId(dataSubId) && dataSubId != this.mDefaultDataSubscription) || !(ddsPhoneId == -1 || ddsPhoneId == this.mDefaultDataPhoneId)) {
            stringBuilder.append(" default ").append(this.mDefaultDataSubscription).append("->").append(dataSubId);
            this.mManualDdsSwitch = true;
            this.mSendDdsSwitchDoneIntent = true;
            this.mDefaultDataSubscription = dataSubId;
            this.mDefaultDataPhoneId = ddsPhoneId;
            diffDetected = true;
        }
        for (i = 0; i < this.mNumPhones; i++) {
            int sub = this.mSubscriptionController.getSubIdUsingPhoneId(i);
            if (sub != this.mPhoneSubscriptions[i]) {
                stringBuilder.append(" phone[").append(i).append("] ").append(this.mPhoneSubscriptions[i]);
                stringBuilder.append("->").append(sub);
                this.mPhoneSubscriptions[i] = sub;
                diffDetected = true;
            }
        }
        log("diffDetected=" + diffDetected);
        if (diffDetected) {
            log("evaluating due to " + stringBuilder.toString());
            List<Integer> newActivePhones = new ArrayList();
            for (DcRequest dcRequest : this.mPrioritizedDcRequests) {
                int phoneIdForRequest = phoneIdForRequest(dcRequest.networkRequest, dcRequest.apnId);
                if (!(phoneIdForRequest == -1 || newActivePhones.contains(Integer.valueOf(phoneIdForRequest)))) {
                    newActivePhones.add(Integer.valueOf(phoneIdForRequest));
                    if (newActivePhones.size() < this.mMaxActivePhones) {
                    }
                }
            }
            try {
                log("ds = " + this.mDefaultDataSubscription + " num=" + this.mNumPhones);
                for (i = 0; i < this.mNumPhones; i++) {
                    log(" phone[" + i + "] using sub[" + this.mPhoneSubscriptions[i] + "]");
                }
                for (Integer i2 : newActivePhones) {
                    log("phone:" + i2);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            this.mNewActivePhones = newActivePhones;
            for (int phoneId = 0; phoneId < this.mNumPhones; phoneId++) {
                if (!newActivePhones.contains(Integer.valueOf(phoneId))) {
                    deactivate(phoneId);
                }
            }
            if (!this.mWaitForDetachResponse) {
                informDdsToRil(dataSubId);
                for (Integer intValue : newActivePhones) {
                    activate(intValue.intValue());
                }
                if (this.mNewActivePhones.contains(Integer.valueOf(ddsPhoneId))) {
                    this.mManualDdsSwitch = false;
                }
            }
        }
    }

    protected int phoneIdForRequest(NetworkRequest netRequest, int apnid) {
        int subId;
        String specifier = null;
        NetworkSpecifier networkSpecifierObj = netRequest.networkCapabilities.getNetworkSpecifier();
        if (networkSpecifierObj != null && (networkSpecifierObj instanceof StringNetworkSpecifier)) {
            specifier = ((StringNetworkSpecifier) networkSpecifierObj).specifier;
        }
        if (TextUtils.isEmpty(specifier)) {
            subId = this.mDefaultDataSubscription;
        } else if (5 == apnid && this.mManualDdsSwitch) {
            subId = this.mDefaultDataSubscription;
        } else {
            try {
                subId = Integer.parseInt(specifier);
            } catch (NumberFormatException e) {
                Rlog.e("PhoneSwitcher", "NumberFormatException on " + specifier);
                subId = -1;
            }
        }
        int phoneId = -1;
        if (subId == -1) {
            return -1;
        }
        for (int i = 0; i < this.mNumPhones; i++) {
            if (this.mPhoneSubscriptions[i] == subId) {
                phoneId = i;
                break;
            }
        }
        return phoneId;
    }

    private boolean isUiccProvisioned(int phoneId) {
        boolean status = QtiUiccCardProvisioner.getInstance().getCurrentUiccCardProvisioningStatus(phoneId) > 0;
        log("isUiccProvisioned = " + status);
        return status;
    }

    protected void deactivate(int phoneId) {
        log("--deactivate--");
        PhoneState state = this.mPhoneStates[phoneId];
        if (state.active) {
            state.active = false;
            log("deactivate " + phoneId);
            state.lastRequested = System.currentTimeMillis();
            if (this.mSubscriptionController.isActiveSubId(this.mPhoneSubscriptions[phoneId])) {
                this.mCommandsInterfaces[phoneId].setDataAllowed(false, obtainMessage(112));
                this.mWaitForDetachResponse = true;
            }
            this.mActivePhoneRegistrants[phoneId].notifyRegistrants();
        }
    }

    protected void activate(int phoneId) {
        log("--activate--");
        PhoneState state = this.mPhoneStates[phoneId];
        if (!state.active || (this.mManualDdsSwitch ^ 1) == 0 || getConnectFailureCount(phoneId) != 0) {
            state.active = true;
            log("activate " + phoneId);
            state.lastRequested = System.currentTimeMillis();
            this.mCommandsInterfaces[phoneId].setDataAllowed(true, obtainMessage(113, phoneId, 0));
        }
    }

    protected void onResendDataAllowed(Message msg) {
        int phoneId = msg.arg1;
        this.mCommandsInterfaces[phoneId].setDataAllowed(this.mPhoneStates[phoneId].active, obtainMessage(113, phoneId, 0));
    }

    private void resetConnectFailureCount(int phoneId) {
        this.mAllowDataFailure[phoneId] = 0;
    }

    private void incConnectFailureCount(int phoneId) {
        int[] iArr = this.mAllowDataFailure;
        iArr[phoneId] = iArr[phoneId] + 1;
    }

    private int getConnectFailureCount(int phoneId) {
        return this.mAllowDataFailure[phoneId];
    }

    private void handleConnectMaxFailure(int phoneId) {
        resetConnectFailureCount(phoneId);
        int ddsPhoneId = this.mSubscriptionController.getPhoneId(this.mSubscriptionController.getDefaultDataSubId());
        if (ddsPhoneId > 0 && ddsPhoneId < this.mNumPhones && phoneId != ddsPhoneId) {
            log("ALLOW_DATA retries exhausted on phoneId = " + phoneId);
            enforceDds(ddsPhoneId);
        }
    }

    private void enforceDds(int phoneId) {
        int[] subId = this.mSubscriptionController.getSubId(phoneId);
        log("enforceDds: subId = " + subId[0]);
        this.mSubscriptionController.setDefaultDataSubId(subId[0]);
    }

    private boolean isAnyVoiceCallActiveOnDevice() {
        boolean ret = this.mCm.getState() != State.IDLE;
        log("isAnyVoiceCallActiveOnDevice: " + ret);
        return ret;
    }

    private void onAllowDataResponse(final int phoneId, AsyncResult ar) {
        if (ar.exception != null) {
            incConnectFailureCount(phoneId);
            log("Allow_data failed on phoneId = " + phoneId + ", failureCount = " + getConnectFailureCount(phoneId));
            if (isAnyVoiceCallActiveOnDevice()) {
                log("Wait for call end indication");
                return;
            } else if (isSimReady(phoneId)) {
                int allowDataFailureCount = getConnectFailureCount(phoneId);
                if (allowDataFailureCount > 5) {
                    handleConnectMaxFailure(phoneId);
                } else {
                    int retryDelay = this.mRetryArray[allowDataFailureCount - 1] * 1000;
                    log("Scheduling retry connect/allow_data after: " + retryDelay);
                    postDelayed(new Runnable() {
                        public void run() {
                            QtiPhoneSwitcher.this.log("Running retry connect/allow_data");
                            if (QtiPhoneSwitcher.this.isPhoneIdValidForRetry(phoneId)) {
                                QtiPhoneSwitcher.this.resendDataAllowed(phoneId);
                                return;
                            }
                            QtiPhoneSwitcher.this.log("Abandon Retry");
                            QtiPhoneSwitcher.this.resetConnectFailureCount(phoneId);
                        }
                    }, (long) retryDelay);
                }
            } else {
                log("Wait for SIM to get READY");
                return;
            }
        }
        log("Allow_data success on phoneId = " + phoneId);
        if (this.mSendDdsSwitchDoneIntent) {
            this.mSendDdsSwitchDoneIntent = false;
            Intent intent = new Intent("org.codeaurora.intent.action.ACTION_DDS_SWITCH_DONE");
            intent.putExtra("subscription", this.mSubscriptionController.getDefaultDataSubId());
            intent.addFlags(16777216);
            log("Broadcast dds switch done intent");
            this.mContext.sendBroadcast(intent);
        }
        resetConnectFailureCount(phoneId);
        this.mActivePhoneRegistrants[phoneId].notifyRegistrants();
        QtiDdsSwitchController qtiDdsSwitchController = this.mQtiDdsSwitchController;
        if (QtiDdsSwitchController.isTempDdsSwitchRequired()) {
            this.mQtiDdsSwitchController.resetTempDdsSwitchRequired();
        }
    }

    private boolean isPhoneIdValidForRetry(int phoneId) {
        if (this.mPrioritizedDcRequests.size() > 0) {
            for (int i = 0; i < this.mMaxActivePhones; i++) {
                DcRequest dcRequest = (DcRequest) this.mPrioritizedDcRequests.get(i);
                if (dcRequest != null) {
                    int phoneIdForRequest = phoneIdForRequest(dcRequest.networkRequest, dcRequest.apnId);
                    log("isPhoneIdValidForRetry phoneIdForRequest= " + phoneIdForRequest);
                    if (phoneIdForRequest == phoneId) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void informDdsToRil(int ddsSubId) {
        int ddsPhoneId = this.mSubscriptionController.getPhoneId(ddsSubId);
        if (this.mQtiRilInterface.isServiceReady()) {
            for (int i = 0; i < this.mNumPhones; i++) {
                log("InformDdsToRil rild= " + i + ", DDS=" + ddsPhoneId);
                QtiDdsSwitchController qtiDdsSwitchController = this.mQtiDdsSwitchController;
                if (QtiDdsSwitchController.isTempDdsSwitchRequired()) {
                    this.mQtiRilInterface.qcRilSendDDSInfo(ddsPhoneId, 1, i);
                } else {
                    this.mQtiRilInterface.qcRilSendDDSInfo(ddsPhoneId, 0, i);
                }
            }
            return;
        }
        log("Oem hook service is not ready yet");
    }
}
