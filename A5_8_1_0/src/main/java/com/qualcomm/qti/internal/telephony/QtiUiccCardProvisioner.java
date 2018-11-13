package com.qualcomm.qti.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccCardStatus.CardState;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.qualcomm.qcrilhook.IQcRilHook;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicBoolean;

public class QtiUiccCardProvisioner extends Handler {
    private static final String ACTION_HOTSWAP_STATE_CHANGE = "oppo.intent.action.SUBINFO_STATE_CHANGE";
    private static final String ACTION_SIM_STATE_CHANGED = "com.dmyk.android.telephony.action.SIM_STATE_CHANGED";
    private static final String ACTION_UICC_MANUAL_PROVISION_STATUS_CHANGED = "org.codeaurora.intent.action.ACTION_UICC_MANUAL_PROVISION_STATUS_CHANGED";
    private static final boolean CMCC_DM_SWITCH = SystemProperties.get("ro.product.oem_dm", "0").equals("1");
    private static final boolean DBG = true;
    private static final int EVENT_GET_ICCID_DONE = 4;
    private static final int EVENT_ICC_CHANGED = 1;
    private static final int EVENT_OEM_HOOK_SERVICE_READY = 3;
    private static final int EVENT_UNSOL_MANUAL_PROVISION_STATUS_CHANGED = 2;
    private static final String EXTRA_NEW_PROVISION_STATE = "newProvisionState";
    private static final String EXTRA_SIM_PHONEID = "com.dmyk.android.telephony.extra.SIM_PHONEID";
    private static final String EXTRA_SIM_STATE = "com.dmyk.android.telephony.extra.SIM_STATE";
    private static final String FEATURE_ENABLE_HOTSWAP = "gsm.enable_hotswap";
    private static final int GENERIC_FAILURE = -1;
    private static final String INTENT_KEY_SIM_STATE = "simstate";
    private static final String INTENT_KEY_SLOT_ID = "slotid";
    private static final String INTENT_KEY_SUB_ID = "subid";
    private static final String INTENT_VALUE_PRIVISION = "PRIVISION";
    private static final String INTENT_VALUE_SIM_CARD_TYPE = "CARDTYPE";
    private static final String INTENT_VALUE_SIM_PLUG_IN = "PLUGIN";
    private static final String INTENT_VALUE_SIM_PLUG_OUT = "PLUGOUT";
    private static final int INVALID_INPUT = -2;
    private static final String LOG_TAG = "QtiUiccCardProvisioner";
    private static final int REQUEST_IN_PROGRESS = -3;
    private static final int SUCCESS = 0;
    private static final boolean VDBG = false;
    private static final int mNumPhones = TelephonyManager.getDefault().getPhoneCount();
    private static AtomicBoolean mRequestInProgress = new AtomicBoolean(CMCC_DM_SWITCH);
    private static UiccController mUiccController = null;
    private static QtiUiccCardProvisioner sInstance;
    private static Object sManualProvLock = new Object();
    protected boolean[] isSimPlugIn = new boolean[TelephonyManager.getDefault().getPhoneCount()];
    private CardState[] mCardState;
    private Context mContext;
    private boolean[] mIsIccIdBootUpQuery = new boolean[mNumPhones];
    private boolean[] mIsIccIdQueryPending = new boolean[mNumPhones];
    private RegistrantList mManualProvisionChangedRegistrants = new RegistrantList();
    private UiccProvisionStatus[] mOldProvisionStatus;
    private UiccProvisionStatus[] mProvisionStatus;
    private QtiRilInterface mQtiRilInterface;
    private String[] mSimIccId;

    public static class UiccProvisionStatus {
        public static final int CARD_NOT_PRESENT = -2;
        public static final int INVALID_STATE = -1;
        public static final int NOT_PROVISIONED = 0;
        public static final int PROVISIONED = 1;
        private int currentState = -1;
        private int userPreference = -1;

        UiccProvisionStatus() {
        }

        boolean equals(UiccProvisionStatus provisionStatus) {
            if (provisionStatus.getUserPreference() == getUserPreference() && provisionStatus.getCurrentState() == getCurrentState()) {
                return QtiUiccCardProvisioner.DBG;
            }
            return QtiUiccCardProvisioner.CMCC_DM_SWITCH;
        }

        int getUserPreference() {
            return this.userPreference;
        }

        void setUserPreference(int pref) {
            this.userPreference = pref;
        }

        int getCurrentState() {
            return this.currentState;
        }

        void setCurrentState(int state) {
            this.currentState = state;
        }

        public String toString() {
            return "User pref " + this.userPreference + " Current pref " + this.currentState;
        }
    }

    public static QtiUiccCardProvisioner make(Context context) {
        if (sInstance == null) {
            sInstance = new QtiUiccCardProvisioner(context);
        } else {
            Log.wtf(LOG_TAG, "QtiUiccCardProvisioner.make() should be called once");
        }
        return sInstance;
    }

    public void dispose() {
        logd(" disposing... ");
        mUiccController.unregisterForIccChanged(this);
        mUiccController = null;
        this.mQtiRilInterface.unRegisterForServiceReadyEvent(this);
        this.mQtiRilInterface.unRegisterForUnsol(this);
        this.mQtiRilInterface = null;
    }

    public static QtiUiccCardProvisioner getInstance() {
        if (sInstance == null) {
            Log.e(LOG_TAG, "QtiUiccCardProvisioner.getInstance called before make");
        }
        return sInstance;
    }

    private QtiUiccCardProvisioner(Context context) {
        logd(" Invoking constructor, no of phones = " + mNumPhones);
        this.mContext = context;
        this.mProvisionStatus = new UiccProvisionStatus[mNumPhones];
        this.mOldProvisionStatus = new UiccProvisionStatus[mNumPhones];
        this.mSimIccId = new String[mNumPhones];
        this.mCardState = new CardState[mNumPhones];
        for (int index = 0; index < mNumPhones; index++) {
            this.mSimIccId[index] = null;
            this.mProvisionStatus[index] = new UiccProvisionStatus();
            this.mCardState[index] = CardState.CARDSTATE_ABSENT;
            this.mIsIccIdQueryPending[index] = CMCC_DM_SWITCH;
            this.mIsIccIdBootUpQuery[index] = DBG;
            this.mOldProvisionStatus[index] = new UiccProvisionStatus();
            this.isSimPlugIn[index] = CMCC_DM_SWITCH;
        }
        mUiccController = UiccController.getInstance();
        mUiccController.registerForIccChanged(this, 1, null);
        this.mQtiRilInterface = QtiRilInterface.getInstance(context);
        this.mQtiRilInterface.registerForServiceReadyEvent(this, 3, null);
        this.mQtiRilInterface.registerForUnsol(this, 2, null);
    }

    public void registerForManualProvisionChanged(Handler handler, int what, Object obj) {
        Registrant r = new Registrant(handler, what, obj);
        synchronized (this.mManualProvisionChangedRegistrants) {
            this.mManualProvisionChangedRegistrants.add(r);
            r.notifyRegistrant();
        }
    }

    public void unregisterForManualProvisionChanged(Handler handler) {
        synchronized (this.mManualProvisionChangedRegistrants) {
            this.mManualProvisionChangedRegistrants.remove(handler);
        }
    }

    public void handleMessage(Message msg) {
        AsyncResult ar;
        switch (msg.what) {
            case 1:
                ar = msg.obj;
                if (ar == null || ar.result == null) {
                    loge("Error: Invalid card index EVENT_ICC_CHANGED ");
                    return;
                } else {
                    updateIccAvailability(((Integer) ar.result).intValue());
                    return;
                }
            case 2:
                ar = (AsyncResult) msg.obj;
                if (ar == null || ar.result == null) {
                    loge("Error: empty result, UNSOL_MANUAL_PROVISION_STATUS_CHANGED");
                    return;
                } else {
                    handleUnsolManualProvisionEvent((Message) ar.result);
                    return;
                }
            case 3:
                ar = (AsyncResult) msg.obj;
                if (ar == null || ar.result == null) {
                    loge("Error: empty result, EVENT_OEM_HOOK_SERVICE_READY");
                    return;
                } else if (((Boolean) ar.result).booleanValue()) {
                    queryAllUiccProvisionInfo();
                    return;
                } else {
                    return;
                }
            case 4:
                ar = (AsyncResult) msg.obj;
                String iccId = null;
                int phoneId = -1;
                if (ar != null) {
                    phoneId = ((Integer) ar.userObj).intValue();
                    if (ar.result != null) {
                        byte[] data = ar.result;
                        iccId = IccUtils.bcdToString(data, 0, data.length);
                    } else {
                        logd("Exception in GET iccId[" + phoneId + "] " + ar.exception);
                    }
                }
                if (phoneId >= 0 && phoneId < mNumPhones) {
                    this.mIsIccIdQueryPending[phoneId] = CMCC_DM_SWITCH;
                    if (!TextUtils.isEmpty(iccId)) {
                        logi("SIM_IO add subInfo record, iccId[" + phoneId + "] = " + iccId);
                        QtiSubscriptionInfoUpdater.getInstance().addSubInfoRecord(phoneId, iccId);
                        this.mSimIccId[phoneId] = iccId;
                        if (this.mSimIccId[phoneId] != null && isAllCardProvisionInfoReceived()) {
                            int[] subIds = QtiSubscriptionController.getInstance().getSubId(phoneId);
                            if (!(subIds == null || subIds.length == 0 || !QtiSubscriptionController.getInstance().isActiveSubId(subIds[0]))) {
                                QtiSubscriptionController.getInstance().updateUserPreferences();
                            }
                        }
                        if (this.mOldProvisionStatus != null && (this.mOldProvisionStatus[phoneId].equals(this.mProvisionStatus[phoneId]) ^ 1) != 0) {
                            logd(" broadcasting ProvisionInfo, phoneId = " + phoneId);
                            broadcastManualProvisionStatusChanged(phoneId, getCurrentProvisioningStatus(phoneId));
                            this.mOldProvisionStatus[phoneId] = this.mProvisionStatus[phoneId];
                            return;
                        }
                        return;
                    }
                    return;
                }
                return;
            default:
                loge("Error: hit default case " + msg.what);
                return;
        }
    }

    private void SendbroadcastSimInfoContentChanged() {
        Intent intent = new Intent("android.intent.action.ACTION_SUBINFO_CONTENT_CHANGE");
        intent.putExtra(INTENT_VALUE_PRIVISION, INTENT_VALUE_PRIVISION);
        this.mContext.sendBroadcast(intent);
        logi("SendbroadcastSimInfoContentChanged");
    }

    private void handleUnsolManualProvisionEvent(Message msg) {
        if (msg == null || msg.obj == null) {
            loge("Null data received in handleUnsolManualProvisionEvent");
            return;
        }
        ByteBuffer payload = ByteBuffer.wrap((byte[]) msg.obj);
        payload.order(ByteOrder.nativeOrder());
        int rspId = payload.getInt();
        int slotId = msg.arg1;
        if (isValidSlotId(slotId) && rspId == IQcRilHook.QCRILHOOK_UNSOL_UICC_PROVISION_STATUS_CHANGED) {
            logi(" Unsol: rspId " + rspId + " slotId " + msg.arg1);
            SendbroadcastSimInfoContentChanged();
            if (!(this.mCardState == null || this.mCardState[slotId] == CardState.CARDSTATE_ABSENT)) {
                queryUiccProvisionInfo(slotId, CMCC_DM_SWITCH);
            }
            int dataSlotId = SubscriptionManager.getSlotIndex(SubscriptionManager.getDefaultDataSubscriptionId());
            if (slotId == dataSlotId && getCurrentProvisioningStatus(dataSlotId) == 1) {
                logd("Set dds after SSR");
                QtiRadioCapabilityController.getInstance().setDdsIfRequired(CMCC_DM_SWITCH);
            }
        }
    }

    private void queryAllUiccProvisionInfo() {
        int index = 0;
        while (index < mNumPhones) {
            logd(" query  provision info, card state[" + index + "] = " + this.mCardState[index]);
            if (this.mCardState[index] == CardState.CARDSTATE_PRESENT && !this.mIsIccIdQueryPending[index]) {
                queryUiccProvisionInfo(index, DBG);
            }
            index++;
        }
    }

    public String getUiccIccId(int slotId) {
        return this.mSimIccId[slotId];
    }

    private void queryUiccProvisionInfo(int phoneId, boolean useSimIORequest) {
        boolean z = CMCC_DM_SWITCH;
        if (this.mQtiRilInterface.isServiceReady() && (isValidSlotId(phoneId) ^ 1) == 0) {
            UiccProvisionStatus oldStatus = this.mProvisionStatus[phoneId];
            UiccProvisionStatus subStatus = this.mQtiRilInterface.getUiccProvisionPreference(phoneId);
            if (!(subStatus.getCurrentState() == -1 || subStatus.getUserPreference() == -1)) {
                logd("queryUiccProvisionInfo, subStatus = " + subStatus);
                synchronized (sManualProvLock) {
                    this.mProvisionStatus[phoneId] = subStatus;
                    logd("queryUiccProvisionInfo, phoneId[" + phoneId + "] = " + this.mProvisionStatus[phoneId]);
                }
            }
            if (this.mSimIccId[phoneId] == null) {
                logd(" queryUiccProvisionInfo: useSimIORequest=  " + useSimIORequest);
                if (!useSimIORequest || this.mIsIccIdBootUpQuery[phoneId]) {
                    String iccId = this.mQtiRilInterface.getUiccIccId(phoneId);
                    logd(" queryUiccProvisionInfo: getUiccIccId ");
                    if (this.mIsIccIdBootUpQuery[phoneId]) {
                        this.mIsIccIdBootUpQuery[phoneId] = CMCC_DM_SWITCH;
                    }
                    if (iccId != null) {
                        logi("OEM add subInfo record, iccId[" + phoneId + "] = " + iccId);
                        QtiSubscriptionInfoUpdater.getInstance().addSubInfoRecord(phoneId, iccId);
                        this.mSimIccId[phoneId] = iccId;
                    }
                } else {
                    loadIccId(phoneId);
                }
            }
            logd(" queryUiccProvisionInfo, iccId[" + phoneId + "] = " + this.mSimIccId[phoneId] + " " + this.mProvisionStatus[phoneId]);
            if (!oldStatus.equals(this.mProvisionStatus[phoneId])) {
                if (this.mSimIccId[phoneId] != null && isAllCardProvisionInfoReceived()) {
                    int[] subIds = QtiSubscriptionController.getInstance().getSubId(phoneId);
                    if (!(subIds == null || subIds.length == 0 || !QtiSubscriptionController.getInstance().isActiveSubId(subIds[0]))) {
                        QtiSubscriptionController.getInstance().updateUserPreferences();
                    }
                }
                if (useSimIORequest && this.mSimIccId[phoneId] == null) {
                    z = DBG;
                }
                if (!z) {
                    logd(" broadcasting ProvisionInfo, phoneId = " + phoneId);
                    broadcastManualProvisionStatusChanged(phoneId, getCurrentProvisioningStatus(phoneId));
                    this.mOldProvisionStatus[phoneId] = this.mProvisionStatus[phoneId];
                }
            }
            return;
        }
        logi("Oem hook service is not ready yet " + phoneId);
    }

    private void loadIccId(int phoneId) {
        UiccCard uiccCard = mUiccController.getUiccCard(phoneId);
        if (uiccCard != null) {
            UiccCardApplication validApp = null;
            int numApps = uiccCard.getNumApplications();
            for (int i = 0; i < numApps; i++) {
                UiccCardApplication app = uiccCard.getApplicationIndex(i);
                if (app != null && app.getType() != AppType.APPTYPE_UNKNOWN) {
                    validApp = app;
                    break;
                }
            }
            if (validApp != null) {
                IccFileHandler fileHandler = validApp.getIccFileHandler();
                if (fileHandler != null) {
                    this.mIsIccIdQueryPending[phoneId] = DBG;
                    fileHandler.loadEFTransparent(12258, obtainMessage(4, Integer.valueOf(phoneId)));
                }
            }
        }
    }

    public boolean isHotSwapSimReboot() {
        return SystemProperties.get(FEATURE_ENABLE_HOTSWAP, this.mContext.getPackageManager().hasSystemFeature("oppo.commcenter.reboot.dialog") ? "false" : "true").equals("false");
    }

    private void updateIccAvailability(int slotId) {
        if (isValidSlotId(slotId)) {
            CardState newState = CardState.CARDSTATE_ABSENT;
            UiccCard newCard = mUiccController.getUiccCard(slotId);
            if (newCard != null) {
                newState = newCard.getCardState();
                logd("updateIccAvailability, card state[" + slotId + "] = " + newState);
                if (isUiccSlotForbid(slotId)) {
                    logd("updateIccAvailability, forbid");
                    deactivateUiccCard(slotId);
                    return;
                }
                if (1 == mUiccController.getSimHotswapState(slotId) && newState == CardState.CARDSTATE_PRESENT && this.mCardState[slotId] != CardState.CARDSTATE_PRESENT) {
                    logd("card[" + slotId + "] inserted");
                    this.isSimPlugIn[slotId] = DBG;
                    broadcastCardhotswap(Integer.toString(slotId), "-1", INTENT_VALUE_SIM_PLUG_IN);
                    broadcastCardHotSwapState(slotId);
                } else if (2 == mUiccController.getSimHotswapState(slotId) && newState == CardState.CARDSTATE_ABSENT && this.mCardState[slotId] != CardState.CARDSTATE_ABSENT) {
                    logd("card[" + slotId + "] plugout");
                    this.isSimPlugIn[slotId] = CMCC_DM_SWITCH;
                    broadcastCardhotswap(Integer.toString(slotId), "-1", INTENT_VALUE_SIM_PLUG_OUT);
                }
                mUiccController.setSimHotswapState(slotId, 0);
                this.mCardState[slotId] = newState;
                int currentState = getCurrentProvisioningStatus(slotId);
                if (this.mCardState[slotId] == CardState.CARDSTATE_PRESENT && ((this.mSimIccId[slotId] == null || currentState == -1 || currentState == -2) && !this.mIsIccIdQueryPending[slotId])) {
                    queryUiccProvisionInfo(slotId, DBG);
                } else if (this.mCardState[slotId] == CardState.CARDSTATE_ABSENT || this.mCardState[slotId] == CardState.CARDSTATE_ERROR) {
                    synchronized (sManualProvLock) {
                        this.mProvisionStatus[slotId].setUserPreference(-2);
                        this.mProvisionStatus[slotId].setCurrentState(-2);
                        this.mSimIccId[slotId] = null;
                        this.mManualProvisionChangedRegistrants.notifyRegistrants(new AsyncResult(null, Integer.valueOf(slotId), null));
                    }
                }
                return;
            }
            logd("updateIccAvailability, uicc card null, ignore " + slotId);
            return;
        }
        loge("Invalid slot Index!!! " + slotId);
    }

    private void broadcastManualProvisionStatusChanged(int phoneId, int newProvisionState) {
        Intent intent = new Intent(ACTION_UICC_MANUAL_PROVISION_STATUS_CHANGED);
        intent.putExtra("phone", phoneId);
        intent.putExtra(EXTRA_NEW_PROVISION_STATE, newProvisionState);
        this.mContext.sendBroadcast(intent);
        this.mManualProvisionChangedRegistrants.notifyRegistrants(new AsyncResult(null, Integer.valueOf(phoneId), null));
    }

    private int getCurrentProvisioningStatus(int slotId) {
        int currentState;
        synchronized (sManualProvLock) {
            currentState = this.mProvisionStatus[slotId].getCurrentState();
        }
        return currentState;
    }

    public int getCurrentUiccCardProvisioningStatus(int slotId) {
        if (mNumPhones == 1 && isValidSlotId(slotId)) {
            return 1;
        }
        if (canProcessRequest(slotId)) {
            return getCurrentProvisioningStatus(slotId);
        }
        return -1;
    }

    public int getUiccCardProvisioningUserPreference(int slotId) {
        if (mNumPhones == 1 && isValidSlotId(slotId)) {
            return 1;
        }
        if (!canProcessRequest(slotId)) {
            return -1;
        }
        int userPref;
        synchronized (sManualProvLock) {
            userPref = this.mProvisionStatus[slotId].getUserPreference();
        }
        return userPref;
    }

    public int activateUiccCard(int slotId) {
        logd(" activateUiccCard: phoneId = " + slotId);
        if (isUiccSlotForbid(slotId)) {
            logd(" activateUiccCard: forbid ");
            return -1;
        }
        enforceModifyPhoneState("activateUiccCard");
        int activateStatus = 0;
        if (!canProcessRequest(slotId)) {
            activateStatus = -2;
        } else if (getCurrentProvisioningStatus(slotId) == 1) {
            logd(" Uicc card in slot[" + slotId + "] already activated ");
        } else if (isFlexMapInProgress() || !mRequestInProgress.compareAndSet(CMCC_DM_SWITCH, DBG)) {
            activateStatus = REQUEST_IN_PROGRESS;
        } else {
            boolean retVal = this.mQtiRilInterface.setUiccProvisionPreference(1, slotId);
            if (retVal) {
                synchronized (sManualProvLock) {
                    this.mProvisionStatus[slotId].setCurrentState(1);
                }
            } else {
                activateStatus = -1;
            }
            logi(" activation result[" + slotId + "] = " + retVal);
            mRequestInProgress.set(CMCC_DM_SWITCH);
        }
        return activateStatus;
    }

    public int deactivateUiccCard(int slotId) {
        logd(" deactivateUiccCard: phoneId = " + slotId);
        enforceModifyPhoneState("deactivateUiccCard");
        int deactivateState = 0;
        if (!canProcessRequest(slotId)) {
            return -2;
        }
        if (getCurrentProvisioningStatus(slotId) == 0) {
            logd(" Uicc card in slot[" + slotId + "] already in deactive state ");
            return 0;
        } else if (isFlexMapInProgress() || !mRequestInProgress.compareAndSet(CMCC_DM_SWITCH, DBG)) {
            return REQUEST_IN_PROGRESS;
        } else {
            boolean retVal = this.mQtiRilInterface.setUiccProvisionPreference(0, slotId);
            if (retVal) {
                synchronized (sManualProvLock) {
                    this.mProvisionStatus[slotId].setCurrentState(0);
                }
            } else {
                deactivateState = -1;
            }
            logi(" deactivation result[" + slotId + "] = " + retVal);
            mRequestInProgress.set(CMCC_DM_SWITCH);
            return deactivateState;
        }
    }

    private void enforceModifyPhoneState(String message) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.MODIFY_PHONE_STATE", message);
    }

    private boolean canProcessRequest(int slotId) {
        if (mNumPhones > 1 && isValidSlotId(slotId)) {
            return DBG;
        }
        loge("Request can't be processed, slotId " + slotId + " numPhones " + mNumPhones);
        return CMCC_DM_SWITCH;
    }

    private boolean isValidSlotId(int slotId) {
        if (slotId < 0 || slotId >= mNumPhones) {
            return CMCC_DM_SWITCH;
        }
        return DBG;
    }

    public boolean isFlexMapInProgress() {
        QtiRadioCapabilityController rcController = QtiRadioCapabilityController.getInstance();
        if (rcController == null) {
            return CMCC_DM_SWITCH;
        }
        boolean retVal = rcController.isSetNWModeInProgress();
        logd("isFlexMapInProgress: = " + retVal);
        return retVal;
    }

    public boolean isAnyProvisionRequestInProgress() {
        return mRequestInProgress.get();
    }

    public boolean isAllCardProvisionInfoReceived() {
        int index = 0;
        while (index < mNumPhones) {
            int provPref = getCurrentProvisioningStatus(index);
            if (provPref == -1 || (this.mSimIccId[index] != null && provPref == -2)) {
                logd("isAllCardProvisionInfoReceived, prov pref[" + index + "] = " + provPref);
                return CMCC_DM_SWITCH;
            }
            index++;
        }
        return DBG;
    }

    public void broadcastCardhotswap(String slotid, String subid, String simstate) {
        Intent intent = new Intent(ACTION_HOTSWAP_STATE_CHANGE);
        intent.putExtra(INTENT_KEY_SLOT_ID, slotid);
        intent.putExtra(INTENT_KEY_SUB_ID, subid);
        intent.putExtra(INTENT_KEY_SIM_STATE, simstate);
        logd("broadcastCardhotswap slotid:" + slotid + " simstate:" + simstate + " subid:" + subid);
        this.mContext.sendBroadcast(intent);
    }

    public void broadcastCardHotSwapState(int slotId) {
        if (CMCC_DM_SWITCH) {
            TelephonyManager tm = TelephonyManager.from(this.mContext);
            int simState = 0;
            if (tm != null) {
                simState = tm.getSimState(slotId);
            } else {
                logd("broadcastCardHotSwapState, tm is null for slotid:" + slotId);
            }
            Intent intent = new Intent(ACTION_SIM_STATE_CHANGED);
            intent.addFlags(268435456);
            intent.putExtra(EXTRA_SIM_PHONEID, slotId);
            intent.putExtra(EXTRA_SIM_STATE, simState);
            logd("Broadcasting intent ACTION_SIM_STATE_CHANGED slotid:" + slotId + " simState:" + simState + " for CmccAutoReg");
            this.mContext.sendBroadcast(intent);
        }
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

    public boolean getSimHotSwapPlugInState() {
        boolean SimPlugInState = CMCC_DM_SWITCH;
        for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++) {
            if (this.isSimPlugIn[i]) {
                SimPlugInState = DBG;
                break;
            }
        }
        logd("getSimHotSwapPlugInState, SimPlugInState:" + SimPlugInState);
        return SimPlugInState;
    }

    protected static boolean isUiccSlotForbid(int slotid) {
        return "1".equals(TelephonyManager.getTelephonyProperty(slotid, "persist.sys.oem_forbid_slots", "0"));
    }

    public void updateUiccProvisionIfProvisioned(int phoneId) {
        if (this.mProvisionStatus[phoneId].getCurrentState() == -1) {
            queryUiccProvisionInfo(phoneId, CMCC_DM_SWITCH);
            logd("updateUiccProvisionInfo, phoneId[" + phoneId + "] = " + this.mProvisionStatus[phoneId]);
        }
    }
}
