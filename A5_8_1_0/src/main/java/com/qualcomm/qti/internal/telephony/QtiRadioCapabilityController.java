package com.qualcomm.qti.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.RadioAccessFamily;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants.State;
import com.android.internal.telephony.ProxyController;
import com.android.internal.telephony.RadioCapability;
import java.util.HashMap;

public class QtiRadioCapabilityController extends Handler {
    static final String ALLOW_FLEX_MAPPING_ON_INACTIVE_SUB_PROPERTY = "persist.radio.flex_map_inactive";
    private static final boolean DBG = true;
    private static final int EVENT_RADIO_CAPS_AVAILABLE = 2;
    private static final int EVENT_RADIO_NOT_AVAILABLE = 1;
    private static final int EVENT_UPDATE_BINDING_DONE = 3;
    private static final int FAILURE = 0;
    private static final String LOG_TAG = "QtiRadioCapabilityController";
    private static final int SUCCESS = 1;
    private static final boolean VDBG = false;
    private static final int mNumPhones = TelephonyManager.getDefault().getPhoneCount();
    private static QtiRadioCapabilityController sInstance;
    private static Object sSetNwModeLock = new Object();
    private boolean bothPhonesMappedToSameStack = false;
    private CommandsInterface[] mCi;
    private Context mContext;
    private int[] mCurrentStackId = new int[mNumPhones];
    private boolean mIsSetPrefNwModeInProgress = false;
    private boolean mNeedSetDds = false;
    private Phone[] mPhone;
    private int[] mPrefNwMode = new int[mNumPhones];
    private int[] mPreferredStackId = new int[mNumPhones];
    private QtiSubscriptionController mQtiSubscriptionController = null;
    private int[] mRadioAccessFamily = new int[mNumPhones];
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Rlog.d(QtiRadioCapabilityController.LOG_TAG, "mReceiver: action " + action);
            if (action.equals("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE")) {
                QtiRadioCapabilityController.this.sendMessage(QtiRadioCapabilityController.this.obtainMessage(3, 1, -1));
            } else if (action.equals("android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED")) {
                QtiRadioCapabilityController.this.sendMessage(QtiRadioCapabilityController.this.obtainMessage(3, 0, -1));
            }
        }
    };
    private HashMap<Integer, Message> mStoredResponse = new HashMap();

    public static QtiRadioCapabilityController make(Context context, Phone[] phone, CommandsInterface[] ci) {
        Rlog.d(LOG_TAG, "getInstance");
        if (sInstance == null) {
            sInstance = new QtiRadioCapabilityController(context, phone, ci);
        } else {
            Log.wtf(LOG_TAG, "QtiRadioCapabilityController.make() should be called once");
        }
        return sInstance;
    }

    public static QtiRadioCapabilityController getInstance() {
        if (sInstance == null) {
            Log.e(LOG_TAG, "QtiRadioCapabilityController.getInstance called before make");
        }
        return sInstance;
    }

    private QtiRadioCapabilityController(Context context, Phone[] phone, CommandsInterface[] ci) {
        this.mCi = ci;
        this.mContext = context;
        this.mPhone = phone;
        this.mQtiSubscriptionController = QtiSubscriptionController.getInstance();
        for (int i = 0; i < this.mCi.length; i++) {
            this.mCi[i].registerForNotAvailable(this, 1, new Integer(i));
            this.mStoredResponse.put(Integer.valueOf(i), null);
        }
        IntentFilter filter = new IntentFilter("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE");
        filter.addAction("android.intent.action.ACTION_SET_RADIO_CAPABILITY_FAILED");
        context.registerReceiver(this.mReceiver, filter);
        logd("Constructor - Exit");
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                if (msg.obj != null) {
                    AsyncResult ar = msg.obj;
                    if (ar.userObj != null) {
                        Integer phoneId = ar.userObj;
                        logd("EVENT_RADIO_NOT_AVAILABLE, phoneId = " + phoneId);
                        processRadioNotAvailable(ar, phoneId.intValue());
                        return;
                    }
                    loge("Invalid user obj");
                    return;
                }
                loge("Invalid msg obj");
                return;
            case 2:
                handleRadioCapsAvailable();
                return;
            case 3:
                logv(" EVENT_UPDATE_BINDING_DONE ");
                handleUpdateBindingDone(msg.arg1);
                return;
            default:
                return;
        }
    }

    private boolean areAllModemCapInfoReceived() {
        for (int i = 0; i < mNumPhones; i++) {
            if (this.mPhone[i].getRadioCapability() == null) {
                return false;
            }
        }
        return DBG;
    }

    private boolean isFlexMappingAllowedOnInactiveSub() {
        return SystemProperties.getBoolean(ALLOW_FLEX_MAPPING_ON_INACTIVE_SUB_PROPERTY, false);
    }

    private void handleUpdateBindingDone(int result) {
        int i;
        if (this.bothPhonesMappedToSameStack && result == 1) {
            this.bothPhonesMappedToSameStack = false;
            if (SystemProperties.get("persist.radio.flexmap_type", "nw_mode").equals("dds")) {
                logd("handleUpdateBindingDone: set dds ");
                this.mQtiSubscriptionController.setDefaultDataSubId(this.mQtiSubscriptionController.getDefaultDataSubId());
            } else {
                for (i = 0; i < mNumPhones; i++) {
                    if (((Message) this.mStoredResponse.get(Integer.valueOf(i))) != null) {
                        logd("handleUpdateBindingDone: try initiate pending flex map req ");
                        if (updateStackBindingIfRequired(DBG)) {
                            return;
                        }
                    }
                }
            }
        }
        if (result == 1) {
            updateNewNwModeToDB();
            for (i = 0; i < mNumPhones; i++) {
                ((QtiGsmCdmaPhone) this.mPhone[i]).fetchIMEI();
            }
        }
        for (i = 0; i < mNumPhones; i++) {
            sendSubscriptionSettings(i);
        }
        setDdsIfRequired(DBG);
        setNWModeInProgressFlag(false);
        notifyRadioCapsUpdated(result == 1 ? DBG : false);
        for (i = 0; i < mNumPhones; i++) {
            int errorCode = 0;
            Message resp = (Message) this.mStoredResponse.get(Integer.valueOf(i));
            if (resp != null) {
                if (result != 1) {
                    errorCode = 2;
                }
                sendResponseToTarget(resp, errorCode);
                this.mStoredResponse.put(Integer.valueOf(i), null);
            }
        }
    }

    private void handleRadioCapsAvailable() {
        logd("handleRadioCapsAvailable... ");
        if (updateStackBindingIfRequired(false)) {
            setNWModeInProgressFlag(DBG);
        } else {
            notifyRadioCapsUpdated(false);
        }
    }

    private void processRadioNotAvailable(AsyncResult ar, int phoneId) {
        logd("processRadioNotAvailable on phoneId = " + phoneId);
        this.mNeedSetDds = DBG;
    }

    private void syncCurrentStackInfo() {
        for (int i = 0; i < mNumPhones; i++) {
            int i2;
            this.mCurrentStackId[i] = Integer.valueOf(this.mPhone[i].getModemUuId()).intValue();
            this.mRadioAccessFamily[this.mCurrentStackId[i]] = this.mPhone[i].getRadioAccessFamily();
            int[] iArr = this.mPreferredStackId;
            if (this.mCurrentStackId[i] >= 0) {
                i2 = this.mCurrentStackId[i];
            } else {
                i2 = i;
            }
            iArr[i] = i2;
            logv("syncCurrentStackInfo, current stackId[" + i + "] = " + this.mCurrentStackId[i] + " raf = " + this.mRadioAccessFamily[this.mCurrentStackId[i]]);
        }
    }

    /* JADX WARNING: Missing block: B:57:0x0163, code:
            return r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized boolean updateStackBindingIfRequired(boolean isNwModeRequest) {
        boolean isUpdateStackBindingRequired = false;
        boolean response = false;
        boolean callInProgress = isAnyCallsInProgress();
        boolean isInEcmState = isAnyPhoneInEcmState();
        String flexMapSupportType = SystemProperties.get("persist.radio.flexmap_type", "nw_mode");
        logd("updateStackBindingIfRequired");
        if (mNumPhones == 1 || (flexMapSupportType.equals("nw_mode") ^ 1) != 0) {
            loge("No need to update Stack Bindingm prop = " + flexMapSupportType + " ph count = " + mNumPhones);
            return false;
        }
        if (!(callInProgress || isInEcmState)) {
            if ((areAllModemCapInfoReceived() ^ 1) == 0) {
                int i;
                if (!isNwModeRequest) {
                    for (i = 0; i < mNumPhones; i++) {
                        int[] subId = this.mQtiSubscriptionController.getSubId(i);
                        if (!isCardAbsent(i)) {
                            if (subId != null && subId.length > 0) {
                                if ((this.mQtiSubscriptionController.isActiveSubId(subId[0]) ^ 1) == 0) {
                                }
                            }
                            loge("Error: subId not generated yet " + i);
                            return false;
                        }
                    }
                }
                if (isBothPhonesMappedToSameStack()) {
                    return initNormalMappingRequest();
                }
                updatePreferredStackIds(isNwModeRequest);
                for (i = 0; i < mNumPhones; i++) {
                    logv(" pref stack[" + i + "] = " + this.mPreferredStackId[i] + " current stack[" + i + "] = " + this.mCurrentStackId[i]);
                    if (this.mPreferredStackId[i] != this.mCurrentStackId[i]) {
                        isUpdateStackBindingRequired = DBG;
                        break;
                    }
                }
                logd(" updateStackBindingIfRequired, required =  " + isUpdateStackBindingRequired);
                if (isUpdateStackBindingRequired) {
                    RadioAccessFamily[] rafs = new RadioAccessFamily[mNumPhones];
                    for (i = 0; i < mNumPhones; i++) {
                        rafs[i] = new RadioAccessFamily(i, this.mRadioAccessFamily[this.mPreferredStackId[i]]);
                    }
                    response = ProxyController.getInstance().setRadioCapability(rafs);
                }
            }
        }
        loge("Error: Call state = " + callInProgress + ", ecm state = " + isInEcmState);
        return false;
    }

    private void updatePreferredStackIds(boolean isNwModeRequest) {
        if (areAllModemCapInfoReceived()) {
            if (!isNwModeRequest) {
                syncPreferredNwModeFromDB();
            }
            syncCurrentStackInfo();
            int curPhoneId = 0;
            while (curPhoneId < mNumPhones) {
                if (isNwModeSupportedOnStack(this.mPrefNwMode[curPhoneId], this.mCurrentStackId[curPhoneId])) {
                    logd("updatePreferredStackIds: current stack[" + this.mCurrentStackId[curPhoneId] + "]supports NwMode[" + this.mPrefNwMode[curPhoneId] + "] on phoneId[" + curPhoneId + "]");
                } else {
                    logd("updatePreferredStackIds:  current stack[" + this.mCurrentStackId[curPhoneId] + "],  NwMode[" + this.mPrefNwMode[curPhoneId] + "] on phoneId[" + curPhoneId + "]");
                    int otherPhoneId = 0;
                    while (otherPhoneId < mNumPhones) {
                        if (otherPhoneId != curPhoneId) {
                            logd("updatePreferredStackIds:  other stack[" + this.mCurrentStackId[otherPhoneId] + "],  NwMode[" + this.mPrefNwMode[curPhoneId] + "] on phoneId[" + curPhoneId + "]");
                            if (isNwModeSupportedOnStack(this.mPrefNwMode[curPhoneId], this.mCurrentStackId[otherPhoneId]) && ((isCardAbsent(otherPhoneId) && (isCardAbsent(curPhoneId) ^ 1) != 0) || isNwModeSupportedOnStack(this.mPrefNwMode[otherPhoneId], this.mCurrentStackId[curPhoneId]))) {
                                logd("updatePreferredStackIds: Cross Binding is possible between phoneId[" + curPhoneId + "] and phoneId[" + otherPhoneId + "]");
                                this.mPreferredStackId[curPhoneId] = this.mCurrentStackId[otherPhoneId];
                                this.mPreferredStackId[otherPhoneId] = this.mCurrentStackId[curPhoneId];
                            }
                        }
                        otherPhoneId++;
                    }
                }
                curPhoneId++;
            }
            return;
        }
        loge("updatePreferredStackIds: Modem Caps not Available, request =" + isNwModeRequest);
    }

    private boolean isNwModeSupportedOnStack(int nwMode, int stackId) {
        int[] numRafSupported = new int[mNumPhones];
        int maxNumRafSupported = 0;
        boolean isSupported = false;
        for (int i = 0; i < mNumPhones; i++) {
            numRafSupported[i] = getNumOfRafSupportedForNwMode(nwMode, this.mRadioAccessFamily[i]);
            if (maxNumRafSupported < numRafSupported[i]) {
                maxNumRafSupported = numRafSupported[i];
            }
        }
        if (numRafSupported[stackId] == maxNumRafSupported) {
            isSupported = DBG;
        }
        logd("nwMode:" + nwMode + ", on stack:" + stackId + " is " + (isSupported ? "Supported" : "Not Supported"));
        return isSupported;
    }

    private void syncPreferredNwModeFromDB() {
        for (int i = 0; i < mNumPhones; i++) {
            this.mPrefNwMode[i] = getNetworkModeFromDB(i);
        }
    }

    private int getNetworkModeFromDB(int phoneId) {
        int networkMode;
        int[] subId = this.mQtiSubscriptionController.getSubId(phoneId);
        try {
            networkMode = TelephonyManager.getIntAtIndex(this.mContext.getContentResolver(), "preferred_network_mode", phoneId);
        } catch (SettingNotFoundException e) {
            loge("getNwMode: " + phoneId + " ,Could not find PREFERRED_NETWORK_MODE!!!");
            networkMode = Phone.PREFERRED_NT_MODE;
        }
        if (subId == null || subId.length <= 0 || !this.mQtiSubscriptionController.isActiveSubId(subId[0])) {
            logi(" get slotId based N/W mode, val[" + phoneId + "] = " + networkMode);
            return networkMode;
        }
        networkMode = Global.getInt(this.mContext.getContentResolver(), "preferred_network_mode" + subId[0], networkMode);
        logi(" get sub based N/W mode, val[" + phoneId + "] = " + networkMode);
        return networkMode;
    }

    private void updateNewNwModeToDB() {
        for (int i = 0; i < mNumPhones; i++) {
            int nwModeFromDB = getNetworkModeFromDB(i);
            if (this.mPrefNwMode[i] != nwModeFromDB) {
                int[] subId = this.mQtiSubscriptionController.getSubId(i);
                logi("updateNewNwModeToDB: subId[" + i + "] = " + subId + " new Nw mode = " + this.mPrefNwMode[i] + " old n/w mode = " + nwModeFromDB);
                if (this.mQtiSubscriptionController.isActiveSubId(subId[0])) {
                    Global.putInt(this.mContext.getContentResolver(), "preferred_network_mode" + subId[0], this.mPrefNwMode[i]);
                }
                TelephonyManager.putIntAtIndex(this.mContext.getContentResolver(), "preferred_network_mode", i, this.mPrefNwMode[i]);
            }
        }
    }

    /* JADX WARNING: Missing block: B:22:0x00bd, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void setPreferredNetworkType(int phoneId, int networkType, Message response) {
        if (isSetNWModeInProgress() || isUiccProvisionInProgress()) {
            loge("setPreferredNetworkType: In Progress, nwmode[" + phoneId + "] = " + networkType);
            sendResponseToTarget(response, 2);
            return;
        }
        int[] subId = this.mQtiSubscriptionController.getSubId(phoneId);
        boolean isSubActive = false;
        if (subId != null && subId.length > 0) {
            isSubActive = this.mQtiSubscriptionController.isActiveSubId(subId[0]);
        }
        logd("setPreferredNetworkType: nwMode[" + phoneId + "] = " + networkType + " isActive = " + isSubActive);
        setNWModeInProgressFlag(DBG);
        syncPreferredNwModeFromDB();
        this.mPrefNwMode[phoneId] = networkType;
        if ((isFlexMappingAllowedOnInactiveSub() || isSubActive) && updateStackBindingIfRequired(DBG)) {
            logv("setPreferredNetworkType: store msg, nwMode[" + phoneId + "] = " + networkType);
            this.mStoredResponse.put(Integer.valueOf(phoneId), response);
        } else {
            logv("setPreferredNetworkType: sending nwMode[" + phoneId + "] = " + networkType);
            this.mCi[phoneId].setPreferredNetworkType(networkType, response);
            setNWModeInProgressFlag(false);
        }
    }

    private int getNumOfRafSupportedForNwMode(int nwMode, int radioAccessFamily) {
        if (radioAccessFamily == 1) {
            loge(" Modem Capabilites are null. Return!!, N/W mode " + nwMode);
            return 0;
        }
        int nwModeRaf = RadioAccessFamily.getRafFromNetworkType(nwMode);
        int supportedRafMaskForNwMode = radioAccessFamily & nwModeRaf;
        logv("getNumOfRATsSupportedForNwMode: nwMode[" + nwMode + " nwModeRaf = " + nwModeRaf + "] raf = " + radioAccessFamily + " supportedRafMaskForNwMode:" + supportedRafMaskForNwMode);
        return Integer.bitCount(supportedRafMaskForNwMode);
    }

    private void sendSubscriptionSettings(int phoneId) {
        Phone phone = this.mPhone[phoneId];
        this.mCi[phoneId].setPreferredNetworkType(getNetworkModeFromDB(phoneId), null);
        int[] subId = this.mQtiSubscriptionController.getSubId(phoneId);
        if (subId != null && subId.length > 0 && this.mQtiSubscriptionController.isActiveSubId(subId[0])) {
            phone.mDcTracker.setDataEnabled(phone.mDcTracker.getDataEnabled());
        }
    }

    private void notifyRadioCapsUpdated(boolean isCrossMapDone) {
        logd("notifyRadioCapsUpdated: radio caps updated " + isCrossMapDone);
        if (isCrossMapDone) {
            for (int i = 0; i < mNumPhones; i++) {
                this.mCurrentStackId[i] = this.mPreferredStackId[i];
            }
        }
        this.mContext.sendStickyBroadcastAsUser(new Intent("org.codeaurora.intent.action.ACTION_RADIO_CAPABILITY_UPDATED"), UserHandle.ALL);
    }

    private void sendResponseToTarget(Message response, int responseCode) {
        if (response != null) {
            AsyncResult.forMessage(response, null, CommandException.fromRilErrno(responseCode));
            response.sendToTarget();
        }
    }

    private boolean isAnyCallsInProgress() {
        for (int i = 0; i < mNumPhones; i++) {
            if (this.mPhone[i].getState() != State.IDLE) {
                return DBG;
            }
        }
        return false;
    }

    private boolean isAnyPhoneInEcmState() {
        for (int i = 0; i < mNumPhones; i++) {
            if (this.mPhone[i].isInEcm()) {
                return DBG;
            }
        }
        return false;
    }

    private boolean isUiccProvisionInProgress() {
        QtiUiccCardProvisioner uiccProvisioner = QtiUiccCardProvisioner.getInstance();
        if (uiccProvisioner == null) {
            return false;
        }
        boolean retVal = uiccProvisioner.isAnyProvisionRequestInProgress();
        logd("isUiccProvisionInProgress: retVal =  " + retVal);
        return retVal;
    }

    private boolean isCardAbsent(int phoneId) {
        int provisionStatus = -1;
        QtiUiccCardProvisioner uiccProvisioner = QtiUiccCardProvisioner.getInstance();
        if (uiccProvisioner != null) {
            provisionStatus = uiccProvisioner.getCurrentUiccCardProvisioningStatus(phoneId);
            logd("provisionStatus[" + phoneId + "] : " + provisionStatus);
        }
        return provisionStatus == -2 ? DBG : false;
    }

    private void setNWModeInProgressFlag(boolean newStatus) {
        synchronized (sSetNwModeLock) {
            this.mIsSetPrefNwModeInProgress = newStatus;
        }
    }

    public boolean isSetNWModeInProgress() {
        boolean retVal;
        synchronized (sSetNwModeLock) {
            retVal = this.mIsSetPrefNwModeInProgress;
        }
        return retVal;
    }

    public void radioCapabilityUpdated(int phoneId, RadioCapability rc) {
        if (!SubscriptionManager.isValidPhoneId(phoneId) || (isSetNWModeInProgress() ^ 1) == 0) {
            loge("radioCapabilityUpdated: Invalid phoneId=" + phoneId + " or SetNWModeInProgress");
            return;
        }
        logd(" radioCapabilityUpdated phoneId[" + phoneId + "] rc = " + rc);
        if (areAllModemCapInfoReceived()) {
            sendMessage(obtainMessage(2));
        }
    }

    public void setDdsIfRequired(boolean forceSetDds) {
        int ddsSubId = this.mQtiSubscriptionController.getDefaultDataSubId();
        int ddsPhoneId = this.mQtiSubscriptionController.getPhoneId(ddsSubId);
        logd("setDdsIfRequired: ddsSub = " + ddsSubId + " ddsPhone = " + ddsPhoneId + " force = " + forceSetDds + " needSetDds = " + this.mNeedSetDds);
        if (!SubscriptionManager.isValidPhoneId(ddsPhoneId)) {
            return;
        }
        if (forceSetDds || this.mNeedSetDds) {
            this.mCi[ddsPhoneId].setDataAllowed(DBG, null);
            if (this.mNeedSetDds) {
                this.mNeedSetDds = false;
            }
        }
    }

    boolean isBothPhonesMappedToSameStack() {
        if (mNumPhones <= 1 || !areAllModemCapInfoReceived() || Integer.valueOf(this.mPhone[0].getModemUuId()) != Integer.valueOf(this.mPhone[1].getModemUuId())) {
            return false;
        }
        loge("Error: both Phones mapped same stackId: " + this.mPhone[0].getModemUuId() + " raf = " + this.mPhone[0].getRadioAccessFamily());
        this.bothPhonesMappedToSameStack = DBG;
        return DBG;
    }

    boolean initNormalMappingRequest() {
        RadioCapability[] oldRadioCapability = new RadioCapability[mNumPhones];
        int maxRaf = RadioAccessFamily.getRafFromNetworkType(22);
        int minRaf = RadioAccessFamily.getRafFromNetworkType(1);
        logd(" initNormalMappingRequest  ");
        setNWModeInProgressFlag(DBG);
        int i = 0;
        while (i < mNumPhones) {
            int i2;
            oldRadioCapability[i] = this.mPhone[i].getRadioCapability();
            if (i == 0) {
                i2 = minRaf;
            } else {
                i2 = maxRaf;
            }
            this.mPhone[i].radioCapabilityUpdated(new RadioCapability(i, 0, 0, i2, i == 0 ? "1" : "0", 1));
            i++;
        }
        RadioAccessFamily[] rafs = new RadioAccessFamily[mNumPhones];
        for (i = 0; i < mNumPhones; i++) {
            int i3;
            if (i == 0) {
                i3 = maxRaf;
            } else {
                i3 = minRaf;
            }
            rafs[i] = new RadioAccessFamily(i, i3);
        }
        if (ProxyController.getInstance().setRadioCapability(rafs)) {
            return DBG;
        }
        for (i = 0; i < mNumPhones; i++) {
            this.mPhone[i].radioCapabilityUpdated(oldRadioCapability[i]);
        }
        logd(" initNormalMappingRequest:  Fail, request in progress ");
        setNWModeInProgressFlag(false);
        return false;
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

    private void logv(String string) {
    }
}
