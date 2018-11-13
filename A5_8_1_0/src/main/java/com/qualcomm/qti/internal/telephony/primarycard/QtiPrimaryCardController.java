package com.qualcomm.qti.internal.telephony.primarycard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings.Global;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.SubscriptionController;
import com.qualcomm.qti.internal.telephony.QtiRilInterface;
import com.qualcomm.qti.internal.telephony.QtiSubscriptionController;
import com.qualcomm.qti.internal.telephony.QtiUiccCardProvisioner;

public class QtiPrimaryCardController extends Handler {
    private static final String ACTION_PRIMARY_CARD_CHANGED_IN_SERVICE = "org.codeaurora.intent.action.PRIMARY_CARD_CHANGED_IN_SERVICE";
    private static final String ACTION_SET_PRIMARY_CARD_DONE = "org.codeaurora.intent.action.ACTION_SET_PRIMARY_CARD_DONE";
    private static final String CARRIER_MODE_CMCC = "cmcc";
    private static final boolean DBG = true;
    private static final int EVENT_ALL_CARDS_INFO_AVAILABLE = 1;
    private static final int EVENT_GET_NWMODE_DONE = 4;
    private static final int EVENT_OEM_HOOK_SERVICE_READY = 8;
    private static final int EVENT_PRIMARY_CARD_SET_DONE = 5;
    private static final int EVENT_SERVICE_STATE_CHANGED = 7;
    private static final int EVENT_SET_NWMODE_DONE = 3;
    private static final int EVENT_SET_PRIMARY_SUB = 6;
    private static final int EVENT_SET_RADIO_CAPABILITY_DONE = 2;
    private static final int FWK_PRIMARY_CARD_REQUEST = 1000;
    private static final String LOG_TAG = "QtiPcController";
    private static final String PRIMARYCARD_SUBSCRIPTION_KEY = "primarycard_sub";
    private static final String PRIMARY_CARD_RESULT = "result";
    private static final String SETTING_USER_PREF_DATA_SUB = "user_preferred_data_sub";
    private static final int USER_PRIMARY_CARD_REQUEST = 1001;
    private static final boolean VDBG = false;
    private static final int[] sCmccIins = new int[]{898600, 898602, 898607, 898608, 898521, 898212};
    private static final int[] sCtIins = new int[]{898603, 898611};
    private static QtiPrimaryCardController sInstance;
    private boolean mCardChanged = false;
    QtiCardInfoManager mCardInfoMgr;
    private String mCarrierMode = SystemProperties.get("persist.radio.carrier_mode", "default");
    private CommandsInterface[] mCi;
    private Message mCmdMessage;
    private final Context mContext;
    private String[] mCurrentIccIds;
    private boolean mIsCMCC = this.mCarrierMode.equals(CARRIER_MODE_CMCC);
    private boolean mPcTriggeredFlexMapDone = false;
    QtiPrimaryCardUtils mPcUtils;
    private Phone[] mPhone;
    private int[] mPrefNwModes;
    private int mPrefPrimarySlot = -1;
    private PrimaryCardState mPrimaryCardState = PrimaryCardState.IDLE;
    QtiPrimaryCardPriorityHandler mPriorityHandler;
    private boolean mPriorityMatch = false;
    private QtiRilInterface mQtiRilInterface;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            QtiPrimaryCardController.this.logd("Recieved intent " + action);
            if ("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE".equals(action)) {
                QtiPrimaryCardController.this.sendMessage(QtiPrimaryCardController.this.obtainMessage(2));
            }
        }
    };
    private int[] mRetryArray = new int[]{2, 5, 10, 20, 30};
    private int mRetryCount;
    private int mRetryPrimarySlot;
    SubsidyLockSettingsObserver mSubsidyLockSettingsObserver;

    public enum PrimaryCardState {
        IDLE,
        IN_PROGRESS,
        PENDING_DUE_TO_PC_IN_PROGRESS,
        PENDING_DUE_TO_FLEXMAP_IN_PROGRESS
    }

    public static void init(Context context, Phone[] phones, CommandsInterface[] ci) {
        synchronized (QtiPrimaryCardController.class) {
            if (sInstance == null && QtiPrimaryCardUtils.isPrimaryCardFeatureEnabled()) {
                sInstance = new QtiPrimaryCardController(context, phones, ci);
            }
        }
    }

    public static QtiPrimaryCardController getInstance() {
        QtiPrimaryCardController qtiPrimaryCardController;
        synchronized (QtiPrimaryCardController.class) {
            if (sInstance == null) {
                throw new RuntimeException("QtiPrimaryCardController was not initialized!");
            }
            qtiPrimaryCardController = sInstance;
        }
        return qtiPrimaryCardController;
    }

    private QtiPrimaryCardController(Context context, Phone[] phones, CommandsInterface[] ci) {
        this.mContext = context;
        this.mPhone = phones;
        this.mCi = ci;
        this.mPcUtils = QtiPrimaryCardUtils.init(this.mContext);
        this.mPriorityHandler = new QtiPrimaryCardPriorityHandler(this.mContext);
        this.mCardInfoMgr = QtiCardInfoManager.init(this.mContext, ci);
        this.mCardInfoMgr.registerAllCardsInfoAvailable(this, 1, null);
        this.mPrefNwModes = new int[QtiPrimaryCardUtils.PHONE_COUNT];
        this.mCurrentIccIds = new String[QtiPrimaryCardUtils.PHONE_COUNT];
        resetPrimaryCardParams();
        IntentFilter intentFilter = new IntentFilter("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE");
        if (SubsidyLockSettingsObserver.isSubsidyLockFeatureEnabled()) {
            this.mSubsidyLockSettingsObserver = new SubsidyLockSettingsObserver(this.mContext);
            this.mSubsidyLockSettingsObserver.observe(this.mCardInfoMgr, this.mPriorityHandler);
        }
        this.mContext.registerReceiver(this.mReceiver, intentFilter);
        this.mQtiRilInterface = QtiRilInterface.getInstance(context);
        this.mQtiRilInterface.registerForServiceReadyEvent(this, 8, null);
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                logd("on EVENT_ALL_CARDS_INFO_AVAILABLE");
                setPrimaryCardIfRequired(false);
                return;
            case 2:
                logd("on EVENT_SET_RADIO_CAPABILITY_DONE");
                handleSetRadioCapsDone();
                return;
            case 3:
                logd("on EVENT_SET_NWMODE_DONE");
                handleSetNwModeDone(msg);
                return;
            case 4:
                logd("on EVENT_GET_NWMODE_DONE");
                handleGetNwModeDone(msg);
                return;
            case 5:
                logd("on EVENT_PRIMARY_CARD_SET_DONE");
                handleOnSetPrimaryCardDone(msg);
                return;
            case 6:
                logd("on EVENT_SET_PRIMARY_SUB ");
                setPrimaryCardIfRequired(DBG);
                break;
            case 7:
                break;
            case 8:
                logd("on EVENT_OEM_HOOK_SERVICE_READY");
                this.mPriorityHandler.reloadPriorityConfig();
                return;
            default:
                return;
        }
        logd("on EVENT_SERVICE_STATE_CHANGED ");
        handleServiceStateChanged(msg);
    }

    private void handleSetRadioCapsDone() {
        if (this.mPrimaryCardState == PrimaryCardState.PENDING_DUE_TO_FLEXMAP_IN_PROGRESS) {
            this.mPrimaryCardState = PrimaryCardState.IDLE;
            logd("Flex mapping completed, try setting primary card now");
            setPrimaryCardIfRequired(false);
        } else if (this.mPrimaryCardState == PrimaryCardState.IN_PROGRESS || this.mPrimaryCardState == PrimaryCardState.PENDING_DUE_TO_PC_IN_PROGRESS) {
            logd("Primary card trigerred Flex Mapping completed.");
            this.mPcTriggeredFlexMapDone = DBG;
        }
    }

    private void handleSetNwModeDone(Message msg) {
        AsyncResult ar = msg.obj;
        int index = Integer.valueOf(msg.arg1).intValue();
        int requestType = Integer.valueOf(msg.arg2).intValue();
        logd("set " + this.mPrefNwModes[index] + " for slot " + index + " done, " + ar.exception);
        if (ar.exception != null) {
            int i = this.mRetryCount + 1;
            this.mRetryCount = i;
            if (i > this.mRetryArray.length || requestType != FWK_PRIMARY_CARD_REQUEST) {
                resetSetNwModeFailureCount();
                for (int i2 = 0; i2 < QtiPrimaryCardUtils.PHONE_COUNT; i2++) {
                    this.mPhone[i2].getPreferredNetworkType(obtainMessage(4, Integer.valueOf(i2)));
                }
                sendSetPrimaryCardResult(2);
            } else {
                int delay = this.mRetryArray[this.mRetryCount - 1] * FWK_PRIMARY_CARD_REQUEST;
                logd("Scheduling retry for failed set primary card request: " + delay + " ms");
                this.mRetryPrimarySlot = this.mPrefPrimarySlot;
                postDelayed(new Runnable() {
                    public void run() {
                        if (QtiPrimaryCardController.this.mRetryPrimarySlot == QtiPrimaryCardController.this.mPrefPrimarySlot) {
                            QtiPrimaryCardController.this.logd("Retrying setPrimaryCardIfRequired request");
                            QtiPrimaryCardController.this.setPrimaryCardIfRequired(false, QtiPrimaryCardController.DBG);
                            return;
                        }
                        QtiPrimaryCardController.this.logd("Primary card slot changed, skip retry");
                        QtiPrimaryCardController.this.resetSetNwModeFailureCount();
                    }
                }, (long) delay);
            }
            return;
        }
        if (this.mRetryCount > 0) {
            resetSetNwModeFailureCount();
        }
        if (this.mPcTriggeredFlexMapDone || index == this.mPrefPrimarySlot) {
            setDdsOnPrimaryCardIfRequired();
            sendSetPrimaryCardResult(0);
        } else {
            logd("set NwMode[" + this.mPrefNwModes[this.mPrefPrimarySlot] + "] on Primarycard:" + this.mPrefPrimarySlot);
            this.mPhone[this.mPrefPrimarySlot].setPreferredNetworkType(this.mPrefNwModes[this.mPrefPrimarySlot], obtainMessage(3, this.mPrefPrimarySlot, requestType));
        }
    }

    private void resetSetNwModeFailureCount() {
        this.mRetryCount = 0;
    }

    private void handleGetNwModeDone(Message msg) {
        int modemNwMode = -1;
        AsyncResult ar = msg.obj;
        int index = ((Integer) ar.userObj).intValue();
        if (ar.exception == null) {
            modemNwMode = ((int[]) ar.result)[0];
            saveNwModesToDB(modemNwMode, index);
        }
        logd("got nwMode:" + modemNwMode + " on slot" + index + ", saved to DB, " + ar.exception);
    }

    private void setDdsOnPrimaryCardIfRequired() {
        SubscriptionController subCtrlr = SubscriptionController.getInstance();
        int subId = subCtrlr.getSubIdUsingPhoneId(this.mPrefPrimarySlot);
        subCtrlr.setDefaultDataSubId(subId);
        Global.putInt(this.mContext.getContentResolver(), SETTING_USER_PREF_DATA_SUB, subId);
        logd("Cofigure DDS on " + subId);
    }

    private void sendSetPrimaryCardResult(int responseCode) {
        if (this.mCmdMessage != null) {
            AsyncResult.forMessage(this.mCmdMessage, null, CommandException.fromRilErrno(responseCode));
            this.mCmdMessage.sendToTarget();
            this.mCmdMessage = null;
        }
        if (responseCode == 0) {
            saveCardIccIdInfoInSp();
            notifySetPrimaryCardDone(DBG);
            QtiPrimaryCardUtils.savePrimarySlotToDB(this.mPrefPrimarySlot);
            broadcastPrimarySlotServiceChanged(this.mPrefPrimarySlot);
        } else {
            notifySetPrimaryCardDone(false);
        }
        if (this.mPrimaryCardState == PrimaryCardState.PENDING_DUE_TO_PC_IN_PROGRESS) {
            this.mPrimaryCardState = PrimaryCardState.IDLE;
            logi("Primary Card request completed, check for pending reqeusts");
            setPrimaryCardIfRequired(false);
        } else if (this.mPrimaryCardState == PrimaryCardState.IN_PROGRESS) {
            this.mPrimaryCardState = PrimaryCardState.IDLE;
        }
    }

    void broadcastPrimarySlotServiceChanged(int slotId) {
        if (SubscriptionManager.isValidSlotIndex(slotId)) {
            this.mPhone[slotId].unregisterForServiceStateChanged(this);
            ServiceState ss = this.mPhone[slotId].getServiceState();
            if (ss == null || (ss.getState() != 0 && (ss.getDataRegState() != 0 || ss.getDataNetworkType() == 18))) {
                this.mPhone[slotId].registerForServiceStateChanged(this, 7, new Integer(slotId));
            } else {
                logd(" broadcastPrimarySlotServiceChanged, slotId " + slotId);
                Intent intent = new Intent(ACTION_PRIMARY_CARD_CHANGED_IN_SERVICE);
                intent.putExtra("phone", slotId);
                this.mContext.sendBroadcast(intent);
            }
            return;
        }
        logd(" Error!!! Invalid slotId " + slotId);
    }

    private void handleServiceStateChanged(Message msg) {
        AsyncResult ar = msg.obj;
        if (ar != null) {
            int currentPrimarySlot = QtiPrimaryCardUtils.getCurrentPrimarySlotFromDB(this.mContext);
            int slotId = ((Integer) ar.userObj).intValue();
            if (SubscriptionManager.isValidSlotIndex(slotId)) {
                this.mPhone[slotId].unregisterForServiceStateChanged(this);
            } else {
                logd(" Error, Invalid slotId " + slotId);
            }
            broadcastPrimarySlotServiceChanged(currentPrimarySlot);
        }
    }

    private void notifySetPrimaryCardDone(boolean isPass) {
        logd("notifySetPrimaryCardDone: Set Primary Card SUCCESS: " + isPass);
        Intent intent = new Intent(ACTION_SET_PRIMARY_CARD_DONE);
        intent.putExtra(PRIMARY_CARD_RESULT, isPass ? 1 : 0);
        intent.putExtra("phone", this.mPrefPrimarySlot);
        this.mContext.sendBroadcast(intent);
    }

    private void handleOnSetPrimaryCardDone(Message msg) {
        AsyncResult ar = msg.obj;
        int index = ((Integer) ar.userObj).intValue();
        if (ar.exception == null) {
            QtiPrimaryCardUtils.savePrimarySlotToDB(index);
            broadcastPrimarySlotServiceChanged(index);
            int userSelectionMode = QtiPrimaryCardUtils.getUserSelectionMode();
            boolean enableUserSelection = false;
            int numCmccCards = 0;
            for (int i = 0; i < QtiPrimaryCardUtils.PHONE_COUNT; i++) {
                if (isCardMatchesIins(this.mCardInfoMgr.getCardInfo(i).getIccId(), sCmccIins)) {
                    numCmccCards++;
                }
            }
            logd("userSelectionMode = " + userSelectionMode + ", mPriorityMatch" + this.mPriorityMatch + ", numCmccCards = " + numCmccCards);
            if (userSelectionMode == 3 || (userSelectionMode == 2 && this.mPriorityMatch)) {
                enableUserSelection = DBG;
            }
            if (this.mIsCMCC) {
                if (numCmccCards == 0) {
                    enableUserSelection = DBG;
                }
                QtiPrimaryCardUtils.saveEnableUserSelectioninDB(enableUserSelection);
                logd("handleOnSetPrimaryCardDone: enableUserSelection =" + enableUserSelection + ", mCardChanged:" + this.mCardChanged + ", numCardsValid:" + numCardsValid());
                if (enableUserSelection && this.mCardChanged && numCardsValid() > 1 && this.mPriorityMatch) {
                    startLTEConifgActivity();
                }
                return;
            }
            QtiPrimaryCardUtils.saveEnableUserSelectioninDB(enableUserSelection);
            boolean subsidyLockFeatureEnabled = SubsidyLockSettingsObserver.isSubsidyLockFeatureEnabled();
            boolean isPermanentlyUnlocked = DBG;
            if (subsidyLockFeatureEnabled) {
                SubsidyLockSettingsObserver subsidyLockSettingsObserver = this.mSubsidyLockSettingsObserver;
                isPermanentlyUnlocked = SubsidyLockSettingsObserver.isPermanentlyUnlocked(this.mContext);
            }
            if (enableUserSelection && this.mCardChanged && numCardsValid() > 1 && isPermanentlyUnlocked) {
                startLTEConifgActivity();
            }
        }
    }

    private void startLTEConifgActivity() {
        Intent intent = new Intent("codeaurora.intent.action.ACTION_LTE_CONFIGURE");
        intent.setFlags(813694976);
        this.mContext.startActivity(intent);
    }

    private boolean haveCMCCSimCard() {
        for (int i = 0; i < numCardsValid(); i++) {
            String iccId = this.mCardInfoMgr.getCardInfo(i).getIccId();
            if (!TextUtils.isEmpty(iccId)) {
                String subIccId = iccId.substring(0, 6);
                logd("iccId is: " + subIccId + " on slot" + i);
                if ("898600".equals(subIccId) || "898602".equals(subIccId) || "898607".equals(subIccId) || "898608".equals(subIccId)) {
                    return DBG;
                }
            }
        }
        return false;
    }

    public void saveUserSelectionMode() {
        int userSelectionMode = QtiPrimaryCardUtils.getUserSelectionMode();
        boolean enableUserSelection = false;
        if (userSelectionMode == 3 || (userSelectionMode == 2 && this.mPriorityMatch)) {
            enableUserSelection = DBG;
        }
        logd("saveUserSelectionMode: enableUserSelection =" + enableUserSelection);
        QtiPrimaryCardUtils.saveEnableUserSelectioninDB(enableUserSelection);
    }

    private int numCardsValid() {
        int numCount = 0;
        for (int i = 0; i < QtiPrimaryCardUtils.PHONE_COUNT; i++) {
            if (this.mCardInfoMgr.getCardInfo(i).getIccId() != null) {
                numCount++;
            }
        }
        return numCount;
    }

    private void updateDdsPreferenceInDb() {
        boolean disableDds = false;
        if (QtiPrimaryCardUtils.isPrimaryCardFeatureEnabled() && QtiPrimaryCardUtils.isPrimary7Plus5Enabled()) {
            int numCmccCards = 0;
            for (int i = 0; i < QtiPrimaryCardUtils.PHONE_COUNT; i++) {
                if (isCardMatchesIins(this.mCardInfoMgr.getCardInfo(i).getIccId(), sCmccIins)) {
                    numCmccCards++;
                }
            }
            logi("numCmccCards: " + numCmccCards);
            if (numCmccCards == 1 && numCmccCards != 2) {
                logi("updateDdsPreferenceInDb: Disable DDS in UI.");
                disableDds = DBG;
            }
        }
        if (QtiPrimaryCardUtils.disableDds()) {
            disableDds = DBG;
        }
        QtiPrimaryCardUtils.saveDisableDdsPreferenceInDB(disableDds);
    }

    private boolean isCardMatchesIins(String iccId, int[] iins) {
        if (iccId == null || iccId.length() < 6) {
            return false;
        }
        int cardIin = Integer.parseInt(iccId.substring(0, 6));
        for (int iin : iins) {
            if (iin == cardIin) {
                return DBG;
            }
        }
        return false;
    }

    private void resetPrimaryCardParams() {
        this.mPriorityMatch = false;
        this.mCmdMessage = null;
        this.mPcTriggeredFlexMapDone = false;
        for (int i = 0; i < QtiPrimaryCardUtils.PHONE_COUNT; i++) {
            this.mPrefNwModes[i] = QtiPrimaryCardUtils.getDefaultNwMode();
        }
    }

    public void trySetPrimarySub() {
        sendMessage(obtainMessage(6));
    }

    private void setPrimaryCardIfRequired(boolean force) {
        setPrimaryCardIfRequired(force, false);
    }

    private void setPrimaryCardIfRequired(boolean force, boolean isRetryRequest) {
        logd("setPrimaryCardIfRequired: force: " + force);
        if ((this.mPrimaryCardState == PrimaryCardState.IN_PROGRESS || this.mPrimaryCardState == PrimaryCardState.PENDING_DUE_TO_PC_IN_PROGRESS) && (isRetryRequest ^ 1) != 0) {
            this.mPrimaryCardState = PrimaryCardState.PENDING_DUE_TO_PC_IN_PROGRESS;
            logi("Primary Card setting in progress. WAIT!");
        } else if (QtiUiccCardProvisioner.getInstance().isFlexMapInProgress() || this.mPrimaryCardState == PrimaryCardState.PENDING_DUE_TO_FLEXMAP_IN_PROGRESS) {
            this.mPrimaryCardState = PrimaryCardState.PENDING_DUE_TO_FLEXMAP_IN_PROGRESS;
            logi("Flex Map in progress. WAIT!");
        } else if (QtiUiccCardProvisioner.getInstance().isAnyProvisionRequestInProgress()) {
            logi("Manual provisioning in progress. EXIT!");
        } else {
            boolean isCardChanged = isCardsInfoChanged();
            this.mPriorityHandler.loadCurrentPriorityConfigs(!(!SubsidyLockSettingsObserver.isSubsidyLockFeatureEnabled() ? force : DBG) ? isCardChanged : DBG);
            this.mPrefPrimarySlot = this.mPriorityHandler.getPrefPrimarySlot();
            loge("mPrefPrimarySlot: setPrimaryCardIfRequired: " + this.mPrefPrimarySlot);
            boolean isSetable = this.mQtiRilInterface.getLpluslSupportStatus() ? false : this.mPrefPrimarySlot != -1 ? DBG : false;
            QtiPrimaryCardUtils.savePrimarySetable(isSetable);
            if (isCardChanged || (isRetryRequest ^ 1) == 0 || (force ^ 1) == 0) {
                this.mCardChanged = isCardChanged;
                resetPrimaryCardParams();
                updateDdsPreferenceInDb();
                if (this.mPrefPrimarySlot == -2) {
                    this.mPrefPrimarySlot = QtiPrimaryCardUtils.getDefaultPrimarySlot();
                    this.mPriorityMatch = DBG;
                } else if (this.mPrefPrimarySlot < 0) {
                    logi("Both slots do not have cards with priority config defined. EXIT!");
                    if (isRetryRequest) {
                        sendSetPrimaryCardResult(2);
                        resetSetNwModeFailureCount();
                    }
                    return;
                }
                setPrimaryCardOnSlot(this.mPrefPrimarySlot, obtainMessage(5, Integer.valueOf(this.mPrefPrimarySlot)), isRetryRequest);
                return;
            }
            logd("primary card " + QtiPrimaryCardUtils.getCurrentPrimarySlotFromDB(this.mContext) + " ,Cards not changed, IGNORE!!");
        }
    }

    public void setPrimaryCardOnSlot(int slotId) {
        if (QtiSubscriptionController.getInstance().isRadioInValidState()) {
            setPrimaryCardOnSlot(slotId, null);
            return;
        }
        loge("setPrimaryCardOnSlot[" + slotId + "]: Radio is in Invalid State, EXIT!!!");
        sendSetPrimaryCardResult(2);
    }

    private synchronized void setPrimaryCardOnSlot(int slotId, Message msg) {
        setPrimaryCardOnSlot(slotId, msg, false);
    }

    private synchronized void setPrimaryCardOnSlot(int primarySlotId, Message msg, boolean isRetryRequest) {
        SubscriptionController subCtrlr = SubscriptionController.getInstance();
        int subId = subCtrlr.getSubIdUsingPhoneId(primarySlotId);
        logd("setPrimaryCardOnSlot: for slotId:" + primarySlotId + ", Start.");
        if ((this.mPrimaryCardState == PrimaryCardState.IDLE || (isRetryRequest ^ 1) == 0) && (this.mPriorityHandler.isConfigLoadDone() ^ 1) == 0 && (SubscriptionManager.isValidSlotIndex(primarySlotId) ^ 1) == 0 && (subCtrlr.isActiveSubId(subId) ^ 1) == 0) {
            int i;
            if (msg == null) {
                for (i = 0; i < QtiPrimaryCardUtils.PHONE_COUNT; i++) {
                    this.mCurrentIccIds[i] = this.mCardInfoMgr.getCardInfo(i).getIccId();
                }
            }
            this.mPrimaryCardState = PrimaryCardState.IN_PROGRESS;
            this.mPrefNwModes = this.mPriorityHandler.getNwModesFromConfig(primarySlotId);
            this.mPrefPrimarySlot = primarySlotId;
            this.mCmdMessage = msg;
            int isFwkRequest = this.mCmdMessage != null ? FWK_PRIMARY_CARD_REQUEST : USER_PRIMARY_CARD_REQUEST;
            this.mPcTriggeredFlexMapDone = false;
            for (i = 0; i < QtiPrimaryCardUtils.PHONE_COUNT; i++) {
                saveNwModesToDB(this.mPrefNwModes[i], i);
            }
            for (int index = 0; index < QtiPrimaryCardUtils.PHONE_COUNT; index++) {
                if (index != primarySlotId) {
                    logd("set NwMode[" + this.mPrefNwModes[index] + "]  on Secondary card:" + index);
                    this.mPhone[index].setPreferredNetworkType(this.mPrefNwModes[index], obtainMessage(3, index, isFwkRequest));
                }
            }
            return;
        }
        loge("Primary Card State is not IDLE, mPrimaryCardState:" + this.mPrimaryCardState + " subId: " + subId + ", or configs not yet loaded EXIT!");
        sendSetPrimaryCardResult(2);
    }

    private void saveNwModesToDB(int nwMode, int slotId) {
        int[] subId = QtiSubscriptionController.getInstance().getSubId(slotId);
        if (subId != null) {
            logi("saveNwModesToDB: subId[" + slotId + "] = " + subId[0] + ", new Nw mode = " + nwMode);
            if (QtiSubscriptionController.getInstance().isActiveSubId(subId[0])) {
                Global.putInt(this.mContext.getContentResolver(), "preferred_network_mode" + subId[0], nwMode);
            }
        } else {
            loge("saveNwModesToDB: subId is null, do not save nwMode in subId based DB");
        }
        TelephonyManager.putIntAtIndex(this.mContext.getContentResolver(), "preferred_network_mode", slotId, nwMode);
    }

    private boolean isCardsInfoChanged() {
        boolean cardChanged = false;
        for (int index = 0; index < QtiPrimaryCardUtils.PHONE_COUNT; index++) {
            if (isCardsInfoChanged(index)) {
                cardChanged = DBG;
            }
        }
        return cardChanged;
    }

    protected boolean isCardsInfoChanged(int phoneId) {
        String iccId = this.mCardInfoMgr.getCardInfo(phoneId).getIccId();
        this.mCurrentIccIds[phoneId] = iccId;
        String iccIdInSP = PreferenceManager.getDefaultSharedPreferences(this.mContext).getString(PRIMARYCARD_SUBSCRIPTION_KEY + phoneId, null);
        logd(" phoneId " + phoneId + " icc id = " + iccId + ", icc id in sp=" + iccIdInSP);
        return TextUtils.equals(iccId, iccIdInSP) ^ 1;
    }

    private void saveCardIccIdInfoInSp() {
        for (int i = 0; i < QtiPrimaryCardUtils.PHONE_COUNT; i++) {
            String iccId = this.mCurrentIccIds[i];
            logd("save IccId: " + iccId + ", on slotId:" + i + ", in SP.");
            PreferenceManager.getDefaultSharedPreferences(this.mContext).edit().putString(PRIMARYCARD_SUBSCRIPTION_KEY + i, iccId).commit();
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
}
