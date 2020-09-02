package com.mediatek.internal.telephony.uicc;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.MccTable;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.uicc.UiccProfile;
import com.mediatek.internal.telephony.MtkSubscriptionInfo;

public class MtkUiccProfile extends UiccProfile {
    protected static final int EVENT_BASE_ID = 100;
    private static final int EVENT_GET_ATR_DONE = 102;
    private static final int EVENT_ICC_FDN_CHANGED = 104;
    private static final int EVENT_OPEN_CHANNEL_WITH_SW_DONE = 103;
    private static final int EVENT_SIM_IO_EX_DONE = 101;
    private static final String ICCID_STRING_FOR_NO_SIM = "N/A";
    private static final String[] PROPERTY_RIL_FULL_UICC_TYPE = {"vendor.gsm.ril.fulluicctype", "vendor.gsm.ril.fulluicctype.2", "vendor.gsm.ril.fulluicctype.3", "vendor.gsm.ril.fulluicctype.4"};
    static final String[] UICCCARD_PROPERTY_RIL_UICC_TYPE = {"vendor.gsm.ril.uicctype", "vendor.gsm.ril.uicctype.2", "vendor.gsm.ril.uicctype.3", "vendor.gsm.ril.uicctype.4"};
    private String[] PROPERTY_ICCID_SIM = {"vendor.ril.iccid.sim1", "vendor.ril.iccid.sim2", "vendor.ril.iccid.sim3", "vendor.ril.iccid.sim4"};
    /* access modifiers changed from: private */
    public RegistrantList mFdnChangedRegistrants = new RegistrantList();
    private int mLastAppType = 1;
    public final Handler mMtkHandler = new Handler() {
        /* class com.mediatek.internal.telephony.uicc.MtkUiccProfile.AnonymousClass1 */

        public void handleMessage(Message msg) {
            if (MtkUiccProfile.this.mDisposed) {
                MtkUiccProfile mtkUiccProfile = MtkUiccProfile.this;
                mtkUiccProfile.loge("handleMessage: Received " + msg.what + " after dispose(); ignoring the message");
                return;
            }
            MtkUiccProfile mtkUiccProfile2 = MtkUiccProfile.this;
            mtkUiccProfile2.log("mHandlerEx Received message " + msg + "[" + msg.what + "]");
            switch (msg.what) {
                case 101:
                case 102:
                case MtkUiccProfile.EVENT_OPEN_CHANNEL_WITH_SW_DONE /*{ENCODED_INT: 103}*/:
                    AsyncResult ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        MtkUiccProfile mtkUiccProfile3 = MtkUiccProfile.this;
                        mtkUiccProfile3.loge("Error in SIM access with exception" + ar.exception);
                    }
                    AsyncResult.forMessage((Message) ar.userObj, ar.result, ar.exception);
                    ((Message) ar.userObj).sendToTarget();
                    return;
                case 104:
                    MtkUiccProfile.this.mFdnChangedRegistrants.notifyRegistrants();
                    return;
                default:
                    MtkUiccProfile.this.mHandler.handleMessage(msg);
                    return;
            }
        }
    };
    private IccCardApplicationStatus.PersoSubState mNetworkLockState = IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_UNKNOWN;

    public MtkUiccProfile(Context c, CommandsInterface ci, IccCardStatus ics, int phoneId, UiccCard uiccCard, Object lock) {
        super(c, ci, ics, phoneId, uiccCard, lock);
        log("MtkUiccProfile Creating");
    }

    /* access modifiers changed from: protected */
    public UiccCardApplication makeUiccApplication(UiccProfile uiccProfile, IccCardApplicationStatus as, Context c, CommandsInterface ci) {
        return new MtkUiccCardApplication(uiccProfile, as, c, ci);
    }

    /* access modifiers changed from: protected */
    public boolean isSupportAllNetworkLockCategory() {
        return true;
    }

    /* access modifiers changed from: protected */
    public void registerCurrAppEvents() {
        MtkUiccProfile.super.registerCurrAppEvents();
        if (this.mUiccApplication != null && (this.mUiccApplication instanceof MtkUiccCardApplication)) {
            this.mUiccApplication.registerForFdnChanged(this.mMtkHandler, 104, null);
        }
    }

    /* access modifiers changed from: protected */
    public void unregisterCurrAppEvents() {
        MtkUiccProfile.super.unregisterCurrAppEvents();
        if (this.mUiccApplication != null && (this.mUiccApplication instanceof MtkUiccCardApplication)) {
            this.mUiccApplication.unregisterForFdnChanged(this.mMtkHandler);
        }
    }

    /* access modifiers changed from: protected */
    public void setCurrentAppType(boolean isGsm) {
        this.mLastAppType = this.mCurrentAppType;
        MtkUiccProfile.super.setCurrentAppType(isGsm);
    }

    /* access modifiers changed from: protected */
    public void setExternalState(IccCardConstants.State newState, boolean override) {
        synchronized (this.mLock) {
            if (!SubscriptionManager.isValidSlotIndex(getPhoneId())) {
                loge("setExternalState: mPhoneId=" + getPhoneId() + " is invalid; Return!!");
                return;
            }
            log("setExternalState(): mExternalState = " + this.mExternalState + " newState =  " + newState + " override = " + override);
            if (!override && newState == this.mExternalState) {
                if (newState == IccCardConstants.State.NETWORK_LOCKED && this.mNetworkLockState != getNetworkPersoType()) {
                    this.mNetworkLockState = getNetworkPersoType();
                } else if (this.mExternalState == IccCardConstants.State.LOADED && this.mLastAppType != this.mCurrentAppType && MtkIccUtilsEx.checkCdma3gCard(getPhoneId()) == 2) {
                    log("Update operatorNumeric for CDMA 3G dual mode card");
                } else {
                    log("setExternalState: !override and newstate unchanged from " + newState);
                    return;
                }
            }
            this.mExternalState = newState;
            if (newState == IccCardConstants.State.NETWORK_LOCKED) {
                this.mNetworkLockState = getNetworkPersoType();
                log("NetworkLockState =  " + this.mNetworkLockState);
            }
            if (this.mExternalState == IccCardConstants.State.LOADED && this.mIccRecords != null) {
                String operator = this.mIccRecords.getOperatorNumeric();
                log("operator=" + operator + " mPhoneId=" + getPhoneId());
                if (!TextUtils.isEmpty(operator)) {
                    this.mTelephonyManager.setSimOperatorNumericForPhone(getPhoneId(), operator);
                    String countryCode = operator.substring(0, 3);
                    if (countryCode != null) {
                        this.mTelephonyManager.setSimCountryIsoForPhone(getPhoneId(), MccTable.countryCodeForMcc(Integer.parseInt(countryCode)));
                    } else {
                        loge("EVENT_RECORDS_LOADED Country code is null");
                    }
                } else {
                    loge("EVENT_RECORDS_LOADED Operator name is null");
                }
            }
            log("setExternalState: set mPhoneId=" + getPhoneId() + " mExternalState=" + this.mExternalState);
            this.mTelephonyManager.setSimStateForPhone(getPhoneId(), getState().toString());
            UiccController.updateInternalIccState(this.mContext, this.mExternalState, getIccStateReason(this.mExternalState), getPhoneId());
        }
    }

    /* access modifiers changed from: protected */
    public void setExternalState(IccCardConstants.State newState) {
        if (newState == IccCardConstants.State.PIN_REQUIRED && this.mUiccApplication != null && this.mUiccApplication.getPin1State() == IccCardStatus.PinState.PINSTATE_ENABLED_PERM_BLOCKED) {
            log("setExternalState(): PERM_DISABLED");
            setExternalState(IccCardConstants.State.PERM_DISABLED);
            return;
        }
        MtkUiccProfile.super.setExternalState(newState);
    }

    /* access modifiers changed from: protected */
    public String getIccStateReason(IccCardConstants.State state) {
        log("getIccStateReason E");
        if (IccCardConstants.State.NETWORK_LOCKED != state || this.mUiccApplication == null) {
            return MtkUiccProfile.super.getIccStateReason(state);
        }
        switch (AnonymousClass2.$SwitchMap$com$android$internal$telephony$uicc$IccCardApplicationStatus$PersoSubState[this.mUiccApplication.getPersoSubState().ordinal()]) {
            case 1:
                return "NETWORK";
            case 2:
                return "NETWORK_SUBSET";
            case 3:
                return "CORPORATE";
            case 4:
                return "SERVICE_PROVIDER";
            case 5:
                return "SIM";
            case 6:
                return "NETWORK_PUK";
            case 7:
                return "NETWORK_SUBSET_PUK";
            case 8:
                return "CORPORATE_PUK";
            case 9:
                return "SERVICE_PROVIDER_PUK";
            case 10:
                return "SIM_PUK";
            default:
                return null;
        }
    }

    /* renamed from: com.mediatek.internal.telephony.uicc.MtkUiccProfile$2  reason: invalid class name */
    static /* synthetic */ class AnonymousClass2 {
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
            try {
                $SwitchMap$com$android$internal$telephony$uicc$IccCardApplicationStatus$PersoSubState[IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_SIM_NETWORK_PUK.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$uicc$IccCardApplicationStatus$PersoSubState[IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_SIM_NETWORK_SUBSET_PUK.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$uicc$IccCardApplicationStatus$PersoSubState[IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_SIM_CORPORATE_PUK.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$uicc$IccCardApplicationStatus$PersoSubState[IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_SIM_SERVICE_PROVIDER_PUK.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$uicc$IccCardApplicationStatus$PersoSubState[IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_SIM_SIM_PUK.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
        }
    }

    public boolean hasIccCard() {
        boolean isSimInsert = false;
        String iccId = SystemProperties.get(this.PROPERTY_ICCID_SIM[getPhoneId()]);
        IccCardStatus.CardState cardState = "";
        if (iccId != null && !iccId.equals(cardState) && !iccId.equals("N/A")) {
            isSimInsert = true;
        }
        if (!isSimInsert && this.mUiccCard != null && this.mUiccCard.getCardState() != IccCardStatus.CardState.CARDSTATE_ABSENT && !"N/A".equals(iccId)) {
            isSimInsert = true;
        }
        if (this.mUiccCard != null && this.mUiccCard.getCardState() == IccCardStatus.CardState.CARDSTATE_ERROR) {
            log("hasIccCard: CARDSTATE_ERROR,return false");
            return false;
        } else if (this.mUiccCard != null && this.mUiccCard.getCardState() != IccCardStatus.CardState.CARDSTATE_ABSENT) {
            return true;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("hasIccCard(): isSimInsert =  ");
            sb.append(isSimInsert);
            sb.append(" ,CardState = ");
            if (this.mUiccCard != null) {
                cardState = this.mUiccCard.getCardState();
            }
            sb.append(cardState);
            sb.append(", iccId = ");
            sb.append(MtkSubscriptionInfo.givePrintableIccid(iccId));
            log(sb.toString());
            return isSimInsert;
        }
    }

    /* access modifiers changed from: protected */
    public String getSubscriptionDisplayName(int subId, Context context) {
        String simNumeric = this.mTelephonyManager.getSimOperatorNumeric(subId);
        String simMvnoName = MtkSpnOverride.getInstance().lookupOperatorNameForDisplayName(subId, simNumeric, true, context);
        String simCarrierName = this.mTelephonyManager.getSimOperatorName(subId);
        log("getSubscriptionDisplayName- simNumeric: " + simNumeric + ", simMvnoName: " + simMvnoName + ", simCarrierName: " + simCarrierName);
        if (this.mExternalState == IccCardConstants.State.LOADED) {
            return !TextUtils.isEmpty(simMvnoName) ? simMvnoName : simCarrierName;
        }
        return "";
    }

    /* access modifiers changed from: protected */
    public boolean isUdpateCarrierName(String newCarrierName) {
        return !TextUtils.isEmpty(newCarrierName) && this.mExternalState == IccCardConstants.State.LOADED;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x004e, code lost:
        return;
     */
    public void queryIccNetworkLock(int category, Message onComplete) {
        log("queryIccNetworkLock(): category =  " + category);
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.queryIccNetworkLock(category, onComplete);
            } else if (onComplete != null) {
                Exception e = CommandException.fromRilErrno(1);
                log("Fail to queryIccNetworkLock, hasIccCard = " + hasIccCard());
                AsyncResult.forMessage(onComplete).exception = e;
                onComplete.sendToTarget();
            }
        }
    }

    public void setIccNetworkLockEnabled(int category, int lockop, String password, String data_imsi, String gid1, String gid2, Message onComplete) {
        Object obj;
        log("SetIccNetworkEnabled(): category = " + category + " lockop = " + lockop + " password = " + password + " data_imsi = " + data_imsi + " gid1 = " + gid1 + " gid2 = " + gid2);
        Object obj2 = this.mLock;
        synchronized (obj2) {
            try {
                if (this.mUiccApplication != null) {
                    obj = obj2;
                    this.mUiccApplication.setIccNetworkLockEnabled(category, lockop, password, data_imsi, gid1, gid2, onComplete);
                } else {
                    obj = obj2;
                    if (onComplete != null) {
                        Exception e = CommandException.fromRilErrno(1);
                        log("Fail to setIccNetworkLockEnabled, hasIccCard = " + hasIccCard());
                        AsyncResult.forMessage(onComplete).exception = e;
                        onComplete.sendToTarget();
                        return;
                    }
                }
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
    }

    public void registerForFdnChanged(Handler h, int what, Object obj) {
        synchronized (this.mLock) {
            synchronized (this.mLock) {
                Registrant r = new Registrant(h, what, obj);
                this.mFdnChangedRegistrants.add(r);
                if (getIccFdnEnabled()) {
                    r.notifyRegistrant();
                }
            }
        }
    }

    public void unregisterForFdnChanged(Handler h) {
        synchronized (this.mLock) {
            this.mFdnChangedRegistrants.remove(h);
        }
    }

    public IccCardApplicationStatus.PersoSubState getNetworkPersoType() {
        log("getNetworkPersoType E");
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                IccCardApplicationStatus.PersoSubState persoSubState = this.mUiccApplication.getPersoSubState();
                return persoSubState;
            }
            IccCardApplicationStatus.PersoSubState persoSubState2 = IccCardApplicationStatus.PersoSubState.PERSOSUBSTATE_UNKNOWN;
            return persoSubState2;
        }
    }

    public void repollIccStateForModemSmlChangeFeatrue(boolean needIntent) {
        log("repollIccStateForModemSmlChangeFeatrue, needIntent = " + needIntent);
        synchronized (this.mLock) {
            MtkUiccController ctrl = UiccController.getInstance();
            if (ctrl != null) {
                ctrl.repollIccStateForModemSmlChangeFeatrue(getPhoneId(), needIntent);
            }
        }
    }

    /* access modifiers changed from: protected */
    public Exception covertException(String operation) {
        log("Fail to " + operation + ", hasIccCard = " + hasIccCard());
        return CommandException.fromRilErrno(1);
    }

    public void iccExchangeSimIOEx(int fileID, int command, int p1, int p2, int p3, String pathID, String data, String pin2, Message onComplete) {
        this.mCi.iccIO(command, fileID, pathID, p1, p2, p3, data, pin2, this.mMtkHandler.obtainMessage(101, onComplete));
    }

    public void iccGetAtr(Message onComplete) {
        this.mCi.getATR(this.mMtkHandler.obtainMessage(102, onComplete));
    }

    public String getIccCardType() {
        return SystemProperties.get(UICCCARD_PROPERTY_RIL_UICC_TYPE[getPhoneId()]);
    }

    public String[] getFullIccCardType() {
        return SystemProperties.get(PROPERTY_RIL_FULL_UICC_TYPE[getPhoneId()]).split(",");
    }

    /* access modifiers changed from: protected */
    public void log(String msg) {
        Rlog.d("UiccProfile", msg + " (phoneId " + getPhoneId() + ")");
    }

    /* access modifiers changed from: protected */
    public void loge(String msg) {
        Rlog.e("UiccProfile", msg + " (phoneId " + getPhoneId() + ")");
    }
}
