package com.mediatek.internal.telephony.uicc;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.RuimRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccProfile;
import com.mediatek.internal.telephony.uicc.IccServiceInfo;

public class MtkUiccCardApplication extends UiccCardApplication {
    public static final int CAT_CORPORATE = 3;
    public static final int CAT_NETOWRK_SUBSET = 1;
    public static final int CAT_NETWOEK = 0;
    public static final int CAT_SERVICE_PROVIDER = 2;
    public static final int CAT_SIM = 4;
    private static final boolean DBG = true;
    private static final int EVENT_CHANGE_NETWORK_LOCK_DONE = 102;
    private static final int EVENT_PUK1_CHANGE_PIN1_DONE = 104;
    private static final int EVENT_PUK2_CHANGE_PIN2_DONE = 105;
    private static final int EVENT_QUERY_NETWORK_LOCK_DONE = 101;
    private static final int EVENT_RADIO_NOTAVAILABLE = 103;
    private static final String LOG_TAG_EX = "MtkUiccCardApp";
    public static final int OP_ADD = 2;
    public static final int OP_LOCK = 1;
    public static final int OP_PERMANENT_UNLOCK = 4;
    public static final int OP_REMOVE = 3;
    public static final int OP_UNLOCK = 0;
    private static final String[] PROPERTY_PIN1_RETRY = {"vendor.gsm.sim.retry.pin1", "vendor.gsm.sim.retry.pin1.2", "vendor.gsm.sim.retry.pin1.3", "vendor.gsm.sim.retry.pin1.4"};
    private static final String[] PROPERTY_PIN2_RETRY = {"vendor.gsm.sim.retry.pin2", "vendor.gsm.sim.retry.pin2.2", "vendor.gsm.sim.retry.pin2.3", "vendor.gsm.sim.retry.pin2.4"};
    static final String[] UICCCARDAPPLICATION_PROPERTY_RIL_UICC_TYPE = {"vendor.gsm.ril.uicctype", "vendor.gsm.ril.uicctype.2", "vendor.gsm.ril.uicctype.3", "vendor.gsm.ril.uicctype.4"};
    private RegistrantList mFdnChangedRegistrants = new RegistrantList();
    private Handler mHandlerEx = new Handler() {
        /* class com.mediatek.internal.telephony.uicc.MtkUiccCardApplication.AnonymousClass1 */

        public void handleMessage(Message msg) {
            if (!MtkUiccCardApplication.this.mDestroyed) {
                int i = msg.what;
                if (i == 1) {
                    int attemptsRemaining = -1;
                    AsyncResult ar = (AsyncResult) msg.obj;
                    if (!(ar.exception == null || ar.result == null)) {
                        attemptsRemaining = MtkUiccCardApplication.this.parsePinPukErrorResult(ar);
                    }
                    Message response = (Message) ar.userObj;
                    AsyncResult.forMessage(response).exception = ar.exception;
                    response.arg1 = attemptsRemaining;
                    response.sendToTarget();
                } else if (i == 101) {
                    MtkUiccCardApplication.this.mtkLog("handleMessage (EVENT_QUERY_NETWORK_LOCK)");
                    AsyncResult ar2 = (AsyncResult) msg.obj;
                    if (ar2.exception != null) {
                        Rlog.e(MtkUiccCardApplication.LOG_TAG_EX, "Error query network lock with exception " + ar2.exception);
                    }
                    AsyncResult.forMessage((Message) ar2.userObj, ar2.result, ar2.exception);
                    ((Message) ar2.userObj).sendToTarget();
                } else if (i == 102) {
                    MtkUiccCardApplication.this.mtkLog("handleMessage (EVENT_CHANGE_NETWORK_LOCK)");
                    AsyncResult ar3 = (AsyncResult) msg.obj;
                    if (ar3.exception != null) {
                        Rlog.e(MtkUiccCardApplication.LOG_TAG_EX, "Error change network lock with exception " + ar3.exception);
                    }
                    AsyncResult.forMessage((Message) ar3.userObj).exception = ar3.exception;
                    ((Message) ar3.userObj).sendToTarget();
                } else if (i == 104) {
                    MtkUiccCardApplication.this.mtkLog("EVENT_PUK1_CHANGE_PIN1_DONE");
                    int attemptsRemainingPuk = -1;
                    AsyncResult ar4 = (AsyncResult) msg.obj;
                    if (!(ar4.exception == null || ar4.result == null)) {
                        attemptsRemainingPuk = MtkUiccCardApplication.this.parsePinPukErrorResult(ar4);
                    }
                    Message responsePuk = (Message) ar4.userObj;
                    AsyncResult.forMessage(responsePuk).exception = ar4.exception;
                    responsePuk.arg1 = attemptsRemainingPuk;
                    responsePuk.sendToTarget();
                    MtkUiccCardApplication.this.queryPin1State();
                } else if (i != 105) {
                    MtkUiccCardApplication mtkUiccCardApplication = MtkUiccCardApplication.this;
                    mtkUiccCardApplication.mtkLoge("Unknown Event " + msg.what);
                } else {
                    int attemptsRemainingPuk2 = -1;
                    AsyncResult ar5 = (AsyncResult) msg.obj;
                    if (!(ar5.exception == null || ar5.result == null)) {
                        attemptsRemainingPuk2 = MtkUiccCardApplication.this.parsePinPukErrorResult(ar5);
                    }
                    Message responsePuk2 = (Message) ar5.userObj;
                    AsyncResult.forMessage(responsePuk2).exception = ar5.exception;
                    responsePuk2.arg1 = attemptsRemainingPuk2;
                    responsePuk2.sendToTarget();
                    MtkUiccCardApplication.this.queryFdn();
                }
            } else if (1 == msg.what || 101 == msg.what) {
                Message response2 = (Message) ((AsyncResult) msg.obj).userObj;
                AsyncResult.forMessage(response2).exception = CommandException.fromRilErrno(1);
                MtkUiccCardApplication mtkUiccCardApplication2 = MtkUiccCardApplication.this;
                mtkUiccCardApplication2.mtkLoge("Received message " + msg + "[" + msg.what + "] while being destroyed. return exception.");
                response2.arg1 = -1;
                response2.sendToTarget();
            } else {
                MtkUiccCardApplication mtkUiccCardApplication3 = MtkUiccCardApplication.this;
                mtkUiccCardApplication3.mtkLoge("Received message " + msg + "[" + msg.what + "] while being destroyed. Ignoring.");
            }
        }
    };
    protected String mIccType = null;
    protected int mPhoneId;

    public MtkUiccCardApplication(UiccProfile uiccProfile, IccCardApplicationStatus as, Context c, CommandsInterface ci) {
        super(uiccProfile, as, c, ci);
        this.mAuthContext = getAuthContextEx(this.mAppType);
        this.mPhoneId = getPhoneId();
    }

    public void update(IccCardApplicationStatus as, Context c, CommandsInterface ci) {
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                mtkLoge("Application updated after destroyed! Fix me!");
                return;
            }
            mtkLog(this.mAppType + " update. New " + as);
            this.mContext = c;
            this.mCi = ci;
            IccCardApplicationStatus.AppType oldAppType = this.mAppType;
            IccCardApplicationStatus.AppState oldAppState = this.mAppState;
            IccCardApplicationStatus.PersoSubState oldPersoSubState = this.mPersoSubState;
            this.mAppType = as.app_type;
            this.mAuthContext = getAuthContextEx(this.mAppType);
            this.mAppState = as.app_state;
            this.mPersoSubState = as.perso_substate;
            this.mAid = as.aid;
            this.mAppLabel = as.app_label;
            this.mPin1Replaced = as.pin1_replaced != 0;
            this.mPin1State = as.pin1;
            this.mPin2State = as.pin2;
            if (this.mAppType != oldAppType) {
                if (this.mIccFh != null) {
                    this.mIccFh.dispose();
                }
                if (this.mIccRecords != null) {
                    this.mIccRecords.dispose();
                }
                this.mIccFh = createIccFileHandler(as.app_type);
                this.mIccRecords = createIccRecords(as.app_type, c, ci);
            }
            mtkLog("mPersoSubState: " + this.mPersoSubState + " oldPersoSubState: " + oldPersoSubState);
            if (this.mPersoSubState != oldPersoSubState) {
                notifyNetworkLockedRegistrantsIfNeeded(null);
            }
            mtkLog("update,  mAppState=" + this.mAppState + "  oldAppState=" + oldAppState);
            if (this.mAppState != oldAppState) {
                mtkLog(oldAppType + " changed state: " + oldAppState + " -> " + this.mAppState);
                if (this.mAppState == IccCardApplicationStatus.AppState.APPSTATE_READY && this.mAppType != IccCardApplicationStatus.AppType.APPTYPE_ISIM) {
                    queryFdn();
                    queryPin1State();
                }
                notifyPinLockedRegistrantsIfNeeded(null);
                notifyReadyRegistrantsIfNeeded(null);
            } else if (this.mAppState == IccCardApplicationStatus.AppState.APPSTATE_READY && ((this.mAppType == IccCardApplicationStatus.AppType.APPTYPE_SIM && oldAppType == IccCardApplicationStatus.AppType.APPTYPE_RUIM) || (this.mAppType == IccCardApplicationStatus.AppType.APPTYPE_RUIM && oldAppType == IccCardApplicationStatus.AppType.APPTYPE_SIM))) {
                queryFdn();
                queryPin1State();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void notifyNetworkLockedRegistrantsIfNeeded(Registrant r) {
        if (this.mDestroyed || this.mAppState != IccCardApplicationStatus.AppState.APPSTATE_SUBSCRIPTION_PERSO) {
            return;
        }
        if (r == null) {
            mtkLog("Notifying registrants: NETWORK_LOCKED");
            this.mNetworkLockedRegistrants.notifyRegistrants();
            return;
        }
        mtkLog("Notifying 1 registrant: NETWORK_LOCED");
        r.notifyRegistrant(new AsyncResult((Object) null, (Object) null, (Throwable) null));
    }

    /* access modifiers changed from: protected */
    public IccRecords createIccRecords(IccCardApplicationStatus.AppType type, Context c, CommandsInterface ci) {
        mtkLog("UiccCardAppEx createIccRecords, AppType = " + type);
        if (type == IccCardApplicationStatus.AppType.APPTYPE_USIM || type == IccCardApplicationStatus.AppType.APPTYPE_SIM) {
            return new MtkSIMRecords(this, c, ci);
        }
        if (type == IccCardApplicationStatus.AppType.APPTYPE_RUIM || type == IccCardApplicationStatus.AppType.APPTYPE_CSIM) {
            return new MtkRuimRecords(this, c, ci);
        }
        if (type == IccCardApplicationStatus.AppType.APPTYPE_ISIM) {
            return new MtkIsimUiccRecords(this, c, ci);
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.mediatek.internal.telephony.uicc.MtkUiccCardApplication$2  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$uicc$IccCardApplicationStatus$AppType = new int[IccCardApplicationStatus.AppType.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$uicc$IccCardApplicationStatus$AppType[IccCardApplicationStatus.AppType.APPTYPE_SIM.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$uicc$IccCardApplicationStatus$AppType[IccCardApplicationStatus.AppType.APPTYPE_RUIM.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$uicc$IccCardApplicationStatus$AppType[IccCardApplicationStatus.AppType.APPTYPE_USIM.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$uicc$IccCardApplicationStatus$AppType[IccCardApplicationStatus.AppType.APPTYPE_CSIM.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$uicc$IccCardApplicationStatus$AppType[IccCardApplicationStatus.AppType.APPTYPE_ISIM.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    /* access modifiers changed from: protected */
    public IccFileHandler createIccFileHandler(IccCardApplicationStatus.AppType type) {
        int i = AnonymousClass2.$SwitchMap$com$android$internal$telephony$uicc$IccCardApplicationStatus$AppType[type.ordinal()];
        if (i == 1) {
            return new MtkSIMFileHandler(this, this.mAid, this.mCi);
        }
        if (i == 2) {
            return new MtkRuimFileHandler(this, this.mAid, this.mCi);
        }
        if (i == 3) {
            return new MtkUsimFileHandler(this, this.mAid, this.mCi);
        }
        if (i == 4) {
            return new MtkCsimFileHandler(this, this.mAid, this.mCi);
        }
        if (i != 5) {
            return null;
        }
        return new MtkIsimFileHandler(this, this.mAid, this.mCi);
    }

    /* access modifiers changed from: protected */
    public void onChangeFdnDone(AsyncResult ar) {
        MtkUiccCardApplication.super.onChangeFdnDone(ar);
        if (ar.exception == null) {
            mtkLog("notifyFdnChangedRegistrants");
            notifyFdnChangedRegistrants();
        }
    }

    private static int getAuthContextEx(IccCardApplicationStatus.AppType appType) {
        int i = AnonymousClass2.$SwitchMap$com$android$internal$telephony$uicc$IccCardApplicationStatus$AppType[appType.ordinal()];
        if (i == 1) {
            return 128;
        }
        if (i == 3 || i == 5) {
            return 129;
        }
        return -1;
    }

    public void supplyPin(String pin, Message onComplete) {
        synchronized (this.mLock) {
            this.mCi.supplyIccPinForApp(pin, this.mAid, this.mHandlerEx.obtainMessage(1, onComplete));
        }
    }

    public void supplyPuk(String puk, String newPin, Message onComplete) {
        synchronized (this.mLock) {
            mtkLog("supplyPuk");
            this.mCi.supplyIccPukForApp(puk, newPin, this.mAid, this.mHandlerEx.obtainMessage(104, onComplete));
        }
    }

    public void supplyPuk2(String puk2, String newPin2, Message onComplete) {
        synchronized (this.mLock) {
            this.mCi.supplyIccPuk2ForApp(puk2, newPin2, this.mAid, this.mHandlerEx.obtainMessage(105, onComplete));
        }
    }

    public void queryIccNetworkLock(int category, Message onComplete) {
        mtkLog("queryIccNetworkLock(): category =  " + category);
        if (category == 0 || category == 1 || category == 2 || category == 3 || category == 4) {
            this.mCi.queryNetworkLock(category, this.mHandlerEx.obtainMessage(101, onComplete));
            return;
        }
        Rlog.e(LOG_TAG_EX, "queryIccNetworkLock unknown category = " + category);
    }

    public void setIccNetworkLockEnabled(int category, int lockop, String password, String data_imsi, String gid1, String gid2, Message onComplete) {
        mtkLog("SetIccNetworkEnabled(): category = " + category + " lockop = " + lockop);
        if (lockop == 0 || lockop == 1 || lockop == 2 || lockop == 3 || lockop == 4) {
            this.mCi.setNetworkLock(category, lockop, password, data_imsi, gid1, gid2, this.mHandlerEx.obtainMessage(102, onComplete));
            return;
        }
        Rlog.e(LOG_TAG_EX, "SetIccNetworkEnabled unknown operation" + lockop);
    }

    public void registerForFdnChanged(Handler h, int what, Object obj) {
        synchronized (this.mLock) {
            this.mFdnChangedRegistrants.add(new Registrant(h, what, obj));
        }
    }

    public void unregisterForFdnChanged(Handler h) {
        synchronized (this.mLock) {
            this.mFdnChangedRegistrants.remove(h);
        }
    }

    private void notifyFdnChangedRegistrants() {
        if (!this.mDestroyed) {
            this.mFdnChangedRegistrants.notifyRegistrants();
        }
    }

    public String getIccCardType() {
        String str = this.mIccType;
        if (str == null || str.equals("")) {
            this.mIccType = SystemProperties.get(UICCCARDAPPLICATION_PROPERTY_RIL_UICC_TYPE[this.mPhoneId]);
        }
        mtkLog("getIccCardType(): mIccType = " + this.mIccType);
        return this.mIccType;
    }

    public void queryFdn() {
        if (getType() == IccCardApplicationStatus.AppType.APPTYPE_ISIM) {
            mtkLog("queryFdn(): do nothing for ISIM.");
        } else {
            this.mCi.queryFacilityLockForApp("FD", "", 7, this.mAid, this.mHandler.obtainMessage(4));
        }
    }

    /* access modifiers changed from: protected */
    public void queryPin1State() {
        if (getType() == IccCardApplicationStatus.AppType.APPTYPE_ISIM) {
            mtkLog("queryPin1State(): do nothing for ISIM.");
        } else {
            this.mCi.queryFacilityLockForApp("SC", "", 7, this.mAid, this.mHandler.obtainMessage(6));
        }
    }

    public boolean getIccFdnAvailable() {
        IccServiceInfo.IccServiceStatus iccSerStatus;
        if (this.mIccRecords == null) {
            mtkLoge("isFdnExist mIccRecords == null");
            return false;
        }
        IccServiceInfo.IccServiceStatus iccServiceStatus = IccServiceInfo.IccServiceStatus.NOT_EXIST_IN_USIM;
        boolean isPhbReady = false;
        if (this.mIccRecords instanceof MtkSIMRecords) {
            iccSerStatus = this.mIccRecords.getSIMServiceStatus(IccServiceInfo.IccService.FDN);
            isPhbReady = this.mIccRecords.isPhbReady();
        } else if (this.mIccRecords instanceof RuimRecords) {
            iccSerStatus = this.mIccRecords.getSIMServiceStatus(IccServiceInfo.IccService.FDN);
            isPhbReady = this.mIccRecords.isPhbReady();
        } else {
            iccSerStatus = IccServiceInfo.IccServiceStatus.NOT_EXIST_IN_USIM;
        }
        log("getIccFdnAvailable status iccSerStatus:" + iccSerStatus);
        if (iccSerStatus != IccServiceInfo.IccServiceStatus.ACTIVATED || !isPhbReady) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void log(String msg) {
        Rlog.d("UiccCardApplication", msg + " (slot " + this.mPhoneId + ")");
    }

    /* access modifiers changed from: protected */
    public void loge(String msg) {
        Rlog.e("UiccCardApplication", msg + " (slot " + this.mPhoneId + ")");
    }

    /* access modifiers changed from: protected */
    public void mtkLog(String msg) {
        Rlog.d(LOG_TAG_EX, msg + " (slot " + this.mPhoneId + ")");
    }

    /* access modifiers changed from: protected */
    public void mtkLoge(String msg) {
        Rlog.e(LOG_TAG_EX, msg + " (slot " + this.mPhoneId + ")");
    }
}
