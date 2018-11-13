package com.qualcomm.qti.internal.telephony.primarycard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.telephony.Rlog;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.qualcomm.qti.internal.telephony.QtiSubscriptionController;
import com.qualcomm.qti.internal.telephony.QtiUiccCardProvisioner;

public class QtiCardInfoManager extends Handler {
    private static final boolean DBG = true;
    private static final int EVENT_ICC_CHANGED = 3;
    private static final int EVENT_MANUAL_PROVISION_STATE_CHANGED = 1;
    private static final int EVENT_READ_EF_HPLMNWACT_DONE = 2;
    private static final int EVENT_SUBINFO_RECORD_ADDED = 4;
    private static final int HPLMN_SEL_DATA_LEN = 5;
    private static final String LOG_TAG = "QtiPcCardInfoManager";
    static final int PHONE_COUNT = TelephonyManager.getDefault().getPhoneCount();
    private static final int UPDATE_CARDTYPE_COMPLETED = 2;
    private static final int UPDATE_CARDTYPE_INIT = 0;
    private static final int UPDATE_CARDTYPE_IN_PROGRESS = 1;
    private static final int UPDATE_CARDTYPE_NOT_NEEDED = 3;
    private static final boolean VDBG = false;
    private static Context mContext;
    private static QtiCardInfoManager sInstance;
    private RegistrantList mAllCardsInfoAvailableRegistrants = new RegistrantList();
    private CardInfo[] mCardInfos = new CardInfo[PHONE_COUNT];
    private QtiUiccCardProvisioner mQtiCardProvisioner;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
                int slotId = intent.getIntExtra("slot", 0);
                String stateExtra = intent.getStringExtra("ss");
                QtiCardInfoManager.this.logd(" SIM_STATE_CHANGED intent received state is " + stateExtra + " slotId + " + slotId);
                if (!SubscriptionManager.isValidSlotIndex(slotId)) {
                    return;
                }
                if (!"LOADED".equals(stateExtra)) {
                    QtiCardInfoManager.this.mCardInfos[slotId].mMCCMNCLoaded = false;
                } else if (!QtiCardInfoManager.this.mCardInfos[slotId].mMCCMNCLoaded) {
                    QtiCardInfoManager.this.mCardInfos[slotId].mMCCMNCLoaded = QtiCardInfoManager.DBG;
                    QtiCardInfoManager.this.updateCardInfo(slotId);
                }
            }
        }
    };

    static class CardInfo {
        private CardType mCardType;
        private String mIccId;
        private boolean mMCCMNCLoaded;
        private String mMccMnc;
        private int mProvisionState;
        private int mUpdateCardTypeState;

        CardInfo() {
        }

        private void reset() {
            this.mCardType = CardType.UNKNOWN;
            this.mIccId = null;
            this.mMccMnc = null;
            this.mUpdateCardTypeState = 0;
            this.mProvisionState = -1;
        }

        public String getIccId() {
            if (QtiPrimaryCardUtils.setPrimaryCardOnDeAct() && this.mProvisionState == 0) {
                return null;
            }
            return this.mIccId;
        }

        public String getMccMnc() {
            if (QtiPrimaryCardUtils.setPrimaryCardOnDeAct() && this.mProvisionState == 0) {
                return null;
            }
            return this.mMccMnc;
        }

        public int getProvisionState() {
            return this.mProvisionState;
        }

        public boolean isCardTypeSame(String cardType) {
            if (QtiPrimaryCardUtils.setPrimaryCardOnDeAct() && this.mProvisionState == 0) {
                return false;
            }
            return CardType.valueOf(cardType).equals(this.mCardType);
        }

        public boolean isCardInfoAvailable(int slotId) {
            boolean isAvailable = this.mUpdateCardTypeState != 0 ? this.mUpdateCardTypeState != 1 ? QtiCardInfoManager.DBG : false : false;
            if (SubsidyLockSettingsObserver.isSubsidyLockFeatureEnabled() && (SubsidyLockSettingsObserver.isPermanentlyUnlocked(QtiCardInfoManager.mContext) ^ 1) != 0) {
                UiccCard uiccCard = UiccController.getInstance().getUiccCard(slotId);
                if (!(this.mProvisionState != 1 || uiccCard == null || uiccCard.getApplication(1) == null)) {
                    UiccCardApplication app = uiccCard.getApplication(1);
                    if (!(app == null || (app.isPersoLocked() ^ 1) == 0)) {
                        isAvailable &= this.mMCCMNCLoaded;
                    }
                    if (!this.mMCCMNCLoaded) {
                        this.mMccMnc = null;
                    }
                }
            }
            return isAvailable;
        }
    }

    public enum CardType {
        UNKNOWN,
        CARDTYPE_2G,
        CARDTYPE_3G,
        CARDTYPE_4G
    }

    static QtiCardInfoManager init(Context context, CommandsInterface[] ci) {
        synchronized (QtiCardInfoManager.class) {
            if (sInstance == null) {
                sInstance = new QtiCardInfoManager(context, ci);
            }
        }
        return sInstance;
    }

    public static QtiCardInfoManager getInstance() {
        QtiCardInfoManager qtiCardInfoManager;
        synchronized (QtiCardInfoManager.class) {
            if (sInstance == null) {
                throw new RuntimeException("QtiCardInfoManager was not initialized!");
            }
            qtiCardInfoManager = sInstance;
        }
        return qtiCardInfoManager;
    }

    private QtiCardInfoManager(Context context, CommandsInterface[] ci) {
        mContext = context;
        for (int index = 0; index < PHONE_COUNT; index++) {
            this.mCardInfos[index] = new CardInfo();
            ci[index].registerForAvailable(this, 1, new Integer(index));
        }
        this.mQtiCardProvisioner = QtiUiccCardProvisioner.getInstance();
        this.mQtiCardProvisioner.registerForManualProvisionChanged(this, 1, null);
        UiccController.getInstance().registerForIccChanged(this, 3, null);
        QtiSubscriptionController.getInstance().registerForAddSubscriptionRecord(this, 4, null);
        if (SubsidyLockSettingsObserver.isSubsidyLockFeatureEnabled()) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.SIM_STATE_CHANGED");
            context.registerReceiver(this.receiver, filter);
        }
    }

    public void registerAllCardsInfoAvailable(Handler handler, int what, Object obj) {
        Registrant r = new Registrant(handler, what, obj);
        synchronized (this.mAllCardsInfoAvailableRegistrants) {
            this.mAllCardsInfoAvailableRegistrants.add(r);
            int index = 0;
            while (index < PHONE_COUNT) {
                if (this.mCardInfos[index].isCardInfoAvailable(index)) {
                    index++;
                } else {
                    return;
                }
            }
            r.notifyRegistrant();
        }
    }

    public void unregisterAllCardsInfoAvailable(Handler handler) {
        synchronized (this.mAllCardsInfoAvailableRegistrants) {
            this.mAllCardsInfoAvailableRegistrants.remove(handler);
        }
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                logd("on EVENT_MANUAL_PROVISION_STATE_CHANGED");
                onManualProvisionStateChanged((AsyncResult) msg.obj);
                return;
            case 2:
                logd("on EVENT_READ_EF_HPLMNWACT_DONE");
                onEfLoaded((AsyncResult) msg.obj);
                return;
            case 3:
                logd("on EVENT_ICC_CHANGED");
                onIccChanged((AsyncResult) msg.obj);
                return;
            case 4:
                logd("on EVENT_SUBINFO_RECORD_ADDED");
                onSubscriptionInfoChanged((AsyncResult) msg.obj);
                return;
            default:
                return;
        }
    }

    private void onSubscriptionInfoChanged(AsyncResult subInfoChange) {
        if (subInfoChange != null && subInfoChange.result != null) {
            updateCardInfo(((Integer) subInfoChange.result).intValue());
        }
    }

    private void onIccChanged(AsyncResult iccChangedResult) {
        if (iccChangedResult != null && iccChangedResult.result != null) {
            updateCardInfo(((Integer) iccChangedResult.result).intValue());
        }
    }

    private void onManualProvisionStateChanged(AsyncResult manualProvisionResult) {
        if (manualProvisionResult == null || manualProvisionResult.result == null) {
            for (int index = 0; index < PHONE_COUNT; index++) {
                updateCardInfo(index);
            }
            return;
        }
        updateCardInfo(((Integer) manualProvisionResult.result).intValue());
    }

    private void updateCardInfo(int slotId) {
        int currProvState = this.mQtiCardProvisioner.getCurrentUiccCardProvisioningStatus(slotId);
        String currIccId = this.mQtiCardProvisioner.getUiccIccId(slotId);
        String currMccMnc = null;
        logd("updateCardInfo[" + slotId + "]: Start!");
        if (QtiSubscriptionController.getInstance().isRadioInValidState()) {
            if (1 == currProvState) {
                UiccCard uiccCard = UiccController.getInstance().getUiccCard(slotId);
                if (uiccCard == null || uiccCard.getApplication(1) == null) {
                    loge("updateCardInfo[" + slotId + "]: card not READY!! ");
                    return;
                }
                SubscriptionController subCtrlr = SubscriptionController.getInstance();
                if (!subCtrlr.isActiveSubId(subCtrlr.getSubIdUsingPhoneId(slotId))) {
                    loge("updateCardInfo[" + slotId + "]: subId not added yet!! ");
                    return;
                } else if (SubsidyLockSettingsObserver.isSubsidyLockFeatureEnabled()) {
                    SubscriptionInfo sir = SubscriptionManager.from(mContext).getActiveSubscriptionInfoForSimSlotIndex(slotId);
                    if (sir != null) {
                        currMccMnc = "" + sir.getMcc() + sir.getMnc();
                    }
                }
            }
            if (currProvState == -1) {
                this.mCardInfos[slotId].reset();
                logd("updateCardInfo[" + slotId + "]: ProvStatus is Invalid, reset cardInfo!");
            } else if (isUpdateCardInfoRequired(slotId, currIccId, currProvState, currMccMnc)) {
                if (currProvState == -2) {
                    this.mCardInfos[slotId].reset();
                    this.mCardInfos[slotId].mProvisionState = currProvState;
                    this.mCardInfos[slotId].mUpdateCardTypeState = 3;
                    logd("updateCardInfo[" + slotId + "]: CardAbsent!!!");
                    notifyAllCardsInfoAvailableIfNeeded();
                } else {
                    this.mCardInfos[slotId].reset();
                    logd("updateCardInfo[" + slotId + "]: Query current state is required!");
                    this.mCardInfos[slotId].mIccId = currIccId;
                    this.mCardInfos[slotId].mMccMnc = currMccMnc;
                    this.mCardInfos[slotId].mProvisionState = currProvState;
                    if (updateUiccCardType(slotId)) {
                        this.mCardInfos[slotId].mUpdateCardTypeState = 2;
                        notifyAllCardsInfoAvailableIfNeeded();
                    }
                }
            }
            logi("updateCardInfo[" + slotId + "]: Exit! - UpdateCardTypeState: " + this.mCardInfos[slotId].mUpdateCardTypeState + ", mCardType: " + this.mCardInfos[slotId].mCardType);
            return;
        }
        loge("updateCardInfo[" + slotId + "]: Radio is in Invalid State, IGNORE!!!");
    }

    private boolean isSubsidyRestricted() {
        if (SubsidyLockSettingsObserver.isSubsidyLockFeatureEnabled()) {
            return SubsidyLockSettingsObserver.isPermanentlyUnlocked(mContext) ^ 1;
        }
        return false;
    }

    private boolean isUpdateCardInfoRequired(int slotId, String currIccId, int currProvState, String currMccmnc) {
        if (!TextUtils.equals(currIccId, this.mCardInfos[slotId].mIccId) || ((isSubsidyRestricted() && currMccmnc != null && (TextUtils.equals(currMccmnc, this.mCardInfos[slotId].mMccMnc) ^ 1) != 0) || currProvState != this.mCardInfos[slotId].mProvisionState || this.mCardInfos[slotId].mUpdateCardTypeState == 0 || (this.mCardInfos[slotId].mCardType == CardType.UNKNOWN && this.mCardInfos[slotId].mUpdateCardTypeState != 3))) {
            return DBG;
        }
        return false;
    }

    private boolean updateUiccCardType(int slotId) {
        try {
            UiccCardApplication app = UiccController.getInstance().getUiccCard(slotId).getApplication(1);
            if (app.getType() != AppType.APPTYPE_USIM) {
                this.mCardInfos[slotId].mCardType = CardType.CARDTYPE_2G;
            } else {
                boolean read4gEf = QtiPrimaryCardUtils.read4gFlag();
                this.mCardInfos[slotId].mCardType = CardType.CARDTYPE_3G;
                if (read4gEf) {
                    IccFileHandler iccFh = app.getIccFileHandler();
                    if (this.mCardInfos[slotId].mProvisionState == 1) {
                        this.mCardInfos[slotId].mUpdateCardTypeState = 1;
                        iccFh.loadEFTransparent(28514, obtainMessage(2, Integer.valueOf(slotId)));
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            loge("For slot " + slotId + " Exception while updateUiccCardType " + e.getMessage());
        }
        return DBG;
    }

    private void onEfLoaded(AsyncResult ar) {
        int slotId = ((Integer) ar.userObj).intValue();
        logd("onEfLoaded: Started");
        if (ar.exception != null) {
            logd("EF_HPLMNWACT read with exception = " + ar.exception);
        } else {
            byte[] data = ar.result;
            logd("result=" + IccUtils.bytesToHexString(data));
            int numRec = data.length / 5;
            logd("number of Records=" + numRec);
            for (int i = 0; i < numRec; i++) {
                if ((data[(i * 5) + 3] & 64) != 0) {
                    this.mCardInfos[slotId].mCardType = CardType.CARDTYPE_4G;
                    break;
                }
            }
        }
        this.mCardInfos[slotId].mUpdateCardTypeState = 2;
        notifyAllCardsInfoAvailableIfNeeded();
        logd("onEfLoaded(" + slotId + ") : mCardType = " + this.mCardInfos[slotId].mCardType);
    }

    private void notifyAllCardsInfoAvailableIfNeeded() {
        int index = 0;
        while (index < PHONE_COUNT) {
            if (this.mCardInfos[index].isCardInfoAvailable(index)) {
                index++;
            } else {
                logd(" card info not available " + index);
                return;
            }
        }
        this.mAllCardsInfoAvailableRegistrants.notifyRegistrants();
    }

    public CardInfo getCardInfo(int slotId) {
        return this.mCardInfos[slotId];
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
