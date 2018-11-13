package com.android.internal.telephony.uicc;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppState;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.PersoSubState;
import com.android.internal.telephony.uicc.IccCardStatus.PinState;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class UiccCardApplication {
    /* renamed from: -com-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues */
    private static final /* synthetic */ int[] f13x1911c1cf = null;
    /* renamed from: -com-android-internal-telephony-uicc-IccCardApplicationStatus$PersoSubStateSwitchesValues */
    private static final /* synthetic */ int[] f14x16bf601e = null;
    /* renamed from: -com-android-internal-telephony-uicc-IccCardStatus$PinStateSwitchesValues */
    private static final /* synthetic */ int[] f15xb5f5d084 = null;
    public static final int AUTH_CONTEXT_EAP_AKA = 129;
    public static final int AUTH_CONTEXT_EAP_SIM = 128;
    public static final int AUTH_CONTEXT_UNDEFINED = -1;
    private static final boolean DBG = true;
    private static final int EVENT_CHANGE_FACILITY_FDN_DONE = 5;
    private static final int EVENT_CHANGE_FACILITY_LOCK_DONE = 7;
    private static final int EVENT_CHANGE_PIN1_DONE = 2;
    private static final int EVENT_CHANGE_PIN2_DONE = 3;
    private static final int EVENT_CLOSE_CHANNEL_DONE = 21;
    private static final int EVENT_EXCHANGE_APDU_DONE = 19;
    private static final int EVENT_OPEN_CHANNEL_DONE = 20;
    private static final int EVENT_PIN1_PUK1_DONE = 1;
    private static final int EVENT_PIN2_PUK2_DONE = 8;
    private static final int EVENT_QUERY_FACILITY_FDN_DONE = 4;
    private static final int EVENT_QUERY_FACILITY_LOCK_DONE = 6;
    private static final int EVENT_RADIO_UNAVAILABLE = 9;
    private static final int EVENT_SIM_IO_DONE = 22;
    private static final String LOG_TAG = "UiccCardApplication";
    private String mAid;
    private String mAppLabel;
    private AppState mAppState;
    private AppType mAppType;
    private int mAuthContext;
    private CommandsInterface mCi;
    private Context mContext;
    private boolean mDesiredFdnEnabled;
    private boolean mDesiredPinLocked;
    private boolean mDestroyed;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            AsyncResult ar;
            Message response;
            int attemptsRemaining;
            if (UiccCardApplication.this.mDestroyed) {
                UiccCardApplication.this.loge("Received message " + msg + "[" + msg.what + "] while being destroyed. Ignoring.");
                try {
                    ar = msg.obj;
                    response = ar.userObj;
                    if (response != null) {
                        switch (msg.what) {
                            case 1:
                            case 2:
                            case 3:
                            case 8:
                                UiccCardApplication.this.loge("Receive and process Pin event:[" + msg.what + "] after destroyed.");
                                attemptsRemaining = -1;
                                if (ar.result != null) {
                                    attemptsRemaining = UiccCardApplication.this.parsePinPukErrorResult(ar);
                                }
                                if (ar.exception != null) {
                                    AsyncResult.forMessage(response).exception = ar.exception;
                                } else {
                                    AsyncResult.forMessage(response).exception = new Exception("Card destroyed");
                                }
                                response.arg1 = attemptsRemaining;
                                response.sendToTarget();
                                return;
                        }
                    }
                } catch (Exception e) {
                    UiccCardApplication.this.loge("exception happen when process message " + msg + "[" + msg.what + "] while being destroyed");
                }
                return;
            }
            switch (msg.what) {
                case 1:
                case 2:
                case 3:
                case 8:
                    attemptsRemaining = -1;
                    ar = (AsyncResult) msg.obj;
                    if (ar.result != null) {
                        attemptsRemaining = UiccCardApplication.this.parsePinPukErrorResult(ar);
                    }
                    response = (Message) ar.userObj;
                    AsyncResult.forMessage(response).exception = ar.exception;
                    response.arg1 = attemptsRemaining;
                    response.sendToTarget();
                    break;
                case 4:
                    UiccCardApplication.this.onQueryFdnEnabled((AsyncResult) msg.obj);
                    break;
                case 5:
                    UiccCardApplication.this.onChangeFdnDone((AsyncResult) msg.obj);
                    break;
                case 6:
                    UiccCardApplication.this.onQueryFacilityLock((AsyncResult) msg.obj);
                    break;
                case 7:
                    UiccCardApplication.this.onChangeFacilityLock((AsyncResult) msg.obj);
                    break;
                case 9:
                    UiccCardApplication.this.log("handleMessage (EVENT_RADIO_UNAVAILABLE)");
                    UiccCardApplication.this.mAppState = AppState.APPSTATE_UNKNOWN;
                    break;
                case 19:
                case 20:
                case 21:
                case 22:
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception != null) {
                        UiccCardApplication.this.log("Error in SIM access with exception" + ar.exception);
                    }
                    AsyncResult.forMessage((Message) ar.userObj, ar.result, ar.exception);
                    ((Message) ar.userObj).sendToTarget();
                    break;
                default:
                    UiccCardApplication.this.loge("Unknown Event " + msg.what);
                    break;
            }
        }
    };
    private boolean mIccFdnAvailable = true;
    private boolean mIccFdnEnabled;
    private IccFileHandler mIccFh;
    private boolean mIccLockEnabled;
    private IccRecords mIccRecords;
    private final Object mLock = new Object();
    private RegistrantList mNetworkLockedRegistrants = new RegistrantList();
    private PersoSubState mPersoSubState;
    private boolean mPin1Replaced;
    private PinState mPin1State;
    private PinState mPin2State;
    private RegistrantList mPinLockedRegistrants = new RegistrantList();
    private RegistrantList mReadyRegistrants = new RegistrantList();
    private UiccCard mUiccCard;

    /* renamed from: -getcom-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues */
    private static /* synthetic */ int[] m14x31a426ab() {
        if (f13x1911c1cf != null) {
            return f13x1911c1cf;
        }
        int[] iArr = new int[AppType.values().length];
        try {
            iArr[AppType.APPTYPE_CSIM.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[AppType.APPTYPE_ISIM.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[AppType.APPTYPE_RUIM.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[AppType.APPTYPE_SIM.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[AppType.APPTYPE_UNKNOWN.ordinal()] = 15;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[AppType.APPTYPE_USIM.ordinal()] = 5;
        } catch (NoSuchFieldError e6) {
        }
        f13x1911c1cf = iArr;
        return iArr;
    }

    /* renamed from: -getcom-android-internal-telephony-uicc-IccCardApplicationStatus$PersoSubStateSwitchesValues */
    private static /* synthetic */ int[] m15x5ed1affa() {
        if (f14x16bf601e != null) {
            return f14x16bf601e;
        }
        int[] iArr = new int[PersoSubState.values().length];
        try {
            iArr[PersoSubState.PERSOSUBSTATE_IN_PROGRESS.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_READY.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_CORPORATE.ordinal()] = 15;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_CORPORATE_PUK.ordinal()] = 16;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_HRPD.ordinal()] = 17;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_HRPD_PUK.ordinal()] = 18;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_NETWORK1.ordinal()] = 19;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_NETWORK1_PUK.ordinal()] = 20;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_NETWORK2.ordinal()] = 21;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_NETWORK2_PUK.ordinal()] = 22;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_RUIM.ordinal()] = 23;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_RUIM_PUK.ordinal()] = 24;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_SERVICE_PROVIDER.ordinal()] = 25;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_SERVICE_PROVIDER_PUK.ordinal()] = 26;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_CORPORATE.ordinal()] = 27;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_CORPORATE_PUK.ordinal()] = 28;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_ICCID.ordinal()] = 29;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_ICCID_PUK.ordinal()] = 30;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_IMPI.ordinal()] = 31;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_IMPI_PUK.ordinal()] = 32;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_NETWORK.ordinal()] = 33;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_NETWORK_PUK.ordinal()] = 34;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_NETWORK_SUBSET.ordinal()] = 35;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_NETWORK_SUBSET_PUK.ordinal()] = 36;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_SERVICE_PROVIDER.ordinal()] = 37;
        } catch (NoSuchFieldError e25) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_SERVICE_PROVIDER_PUK.ordinal()] = 38;
        } catch (NoSuchFieldError e26) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_SIM.ordinal()] = 39;
        } catch (NoSuchFieldError e27) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_SIM_PUK.ordinal()] = 40;
        } catch (NoSuchFieldError e28) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_SPN.ordinal()] = 41;
        } catch (NoSuchFieldError e29) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_SPN_PUK.ordinal()] = 42;
        } catch (NoSuchFieldError e30) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_SP_EHPLMN.ordinal()] = 43;
        } catch (NoSuchFieldError e31) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_SP_EHPLMN_PUK.ordinal()] = 44;
        } catch (NoSuchFieldError e32) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_UNKNOWN.ordinal()] = 3;
        } catch (NoSuchFieldError e33) {
        }
        f14x16bf601e = iArr;
        return iArr;
    }

    /* renamed from: -getcom-android-internal-telephony-uicc-IccCardStatus$PinStateSwitchesValues */
    private static /* synthetic */ int[] m16xc1fb5860() {
        if (f15xb5f5d084 != null) {
            return f15xb5f5d084;
        }
        int[] iArr = new int[PinState.values().length];
        try {
            iArr[PinState.PINSTATE_DISABLED.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[PinState.PINSTATE_ENABLED_BLOCKED.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[PinState.PINSTATE_ENABLED_NOT_VERIFIED.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[PinState.PINSTATE_ENABLED_PERM_BLOCKED.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[PinState.PINSTATE_ENABLED_VERIFIED.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[PinState.PINSTATE_UNKNOWN.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        f15xb5f5d084 = iArr;
        return iArr;
    }

    public UiccCardApplication(UiccCard uiccCard, IccCardApplicationStatus as, Context c, CommandsInterface ci) {
        boolean z = true;
        log("Creating UiccApp: " + as);
        this.mUiccCard = uiccCard;
        this.mAppState = as.app_state;
        this.mAppType = as.app_type;
        this.mAuthContext = getAuthContext(this.mAppType);
        this.mPersoSubState = as.perso_substate;
        this.mAid = as.aid;
        this.mAppLabel = as.app_label;
        if (as.pin1_replaced == 0) {
            z = false;
        }
        this.mPin1Replaced = z;
        this.mPin1State = as.pin1;
        this.mPin2State = as.pin2;
        this.mContext = c;
        this.mCi = ci;
        this.mIccFh = createIccFileHandler(as.app_type);
        this.mIccRecords = createIccRecords(as.app_type, this.mContext, this.mCi);
        if (this.mAppState == AppState.APPSTATE_READY) {
            queryFdn();
            queryPin1State();
        }
        this.mCi.registerForNotAvailable(this.mHandler, 9, null);
    }

    /* JADX WARNING: Missing block: B:39:0x0109, code:
            return;
     */
    /* JADX WARNING: Missing block: B:45:0x0117, code:
            if (r8.mPin1State != com.android.internal.telephony.uicc.IccCardStatus.PinState.PINSTATE_ENABLED_PERM_BLOCKED) goto L_0x0108;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void update(IccCardApplicationStatus as, Context c, CommandsInterface ci) {
        boolean z = true;
        synchronized (this.mLock) {
            if (this.mDestroyed) {
                loge("Application updated after destroyed! Fix me!");
                return;
            }
            log(this.mAppType + " update. New " + as);
            this.mContext = c;
            this.mCi = ci;
            AppType oldAppType = this.mAppType;
            AppState oldAppState = this.mAppState;
            PersoSubState oldPersoSubState = this.mPersoSubState;
            this.mAppType = as.app_type;
            this.mAuthContext = getAuthContext(this.mAppType);
            this.mAppState = as.app_state;
            this.mPersoSubState = as.perso_substate;
            this.mAid = as.aid;
            this.mAppLabel = as.app_label;
            if (as.pin1_replaced == 0) {
                z = false;
            }
            this.mPin1Replaced = z;
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
            if (this.mPersoSubState != oldPersoSubState && isPersoLocked()) {
                notifyNetworkLockedRegistrantsIfNeeded(null);
            }
            if (this.mAppState != oldAppState) {
                log(oldAppType + " changed state: " + oldAppState + " -> " + this.mAppState);
                if (this.mAppState == AppState.APPSTATE_READY) {
                    queryFdn();
                    queryPin1State();
                }
                notifyPinLockedRegistrantsIfNeeded(null);
                notifyReadyRegistrantsIfNeeded(null);
            }
            if (!this.mIccLockEnabled) {
                log("update usim app pin1 state: " + this.mPin1State);
                if (!(this.mPin1State == PinState.PINSTATE_ENABLED_NOT_VERIFIED || this.mPin1State == PinState.PINSTATE_ENABLED_VERIFIED)) {
                    if (this.mPin1State != PinState.PINSTATE_ENABLED_BLOCKED) {
                    }
                }
                log("set lock Enable");
                this.mIccLockEnabled = true;
            }
        }
    }

    void dispose() {
        synchronized (this.mLock) {
            log(this.mAppType + " being Disposed");
            this.mDestroyed = true;
            if (this.mIccRecords != null) {
                this.mIccRecords.dispose();
            }
            if (this.mIccFh != null) {
                this.mIccFh.dispose();
            }
            this.mIccRecords = null;
            this.mIccFh = null;
            this.mCi.unregisterForNotAvailable(this.mHandler);
        }
    }

    private IccRecords createIccRecords(AppType type, Context c, CommandsInterface ci) {
        if (type == AppType.APPTYPE_USIM || type == AppType.APPTYPE_SIM) {
            return new SIMRecords(this, c, ci);
        }
        if (type == AppType.APPTYPE_RUIM || type == AppType.APPTYPE_CSIM) {
            return new RuimRecords(this, c, ci);
        }
        if (type == AppType.APPTYPE_ISIM) {
            return new IsimUiccRecords(this, c, ci);
        }
        return null;
    }

    private IccFileHandler createIccFileHandler(AppType type) {
        switch (m14x31a426ab()[type.ordinal()]) {
            case 1:
                return new CsimFileHandler(this, this.mAid, this.mCi);
            case 2:
                return new IsimFileHandler(this, this.mAid, this.mCi);
            case 3:
                return new RuimFileHandler(this, this.mAid, this.mCi);
            case 4:
                return new SIMFileHandler(this, this.mAid, this.mCi);
            case 5:
                return new UsimFileHandler(this, this.mAid, this.mCi);
            default:
                return null;
        }
    }

    public void queryFdn() {
        this.mCi.queryFacilityLockForApp(CommandsInterface.CB_FACILITY_BA_FD, SpnOverride.MVNO_TYPE_NONE, 7, this.mAid, this.mHandler.obtainMessage(4));
    }

    /* JADX WARNING: Missing block: B:16:0x005e, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onQueryFdnEnabled(AsyncResult ar) {
        boolean z = true;
        synchronized (this.mLock) {
            if (ar.exception != null) {
                log("Error in querying facility lock:" + ar.exception);
                return;
            }
            int[] result = ar.result;
            if (result.length != 0) {
                if (result[0] == 2) {
                    this.mIccFdnEnabled = false;
                    this.mIccFdnAvailable = false;
                } else {
                    if (result[0] != 1) {
                        z = false;
                    }
                    this.mIccFdnEnabled = z;
                    this.mIccFdnAvailable = true;
                }
                log("Query facility FDN : FDN service available: " + this.mIccFdnAvailable + " enabled: " + this.mIccFdnEnabled);
            } else {
                loge("Bogus facility lock response");
            }
        }
    }

    private void onChangeFdnDone(AsyncResult ar) {
        synchronized (this.mLock) {
            int attemptsRemaining = -1;
            if (ar.exception == null) {
                this.mIccFdnEnabled = this.mDesiredFdnEnabled;
                log("EVENT_CHANGE_FACILITY_FDN_DONE: mIccFdnEnabled=" + this.mIccFdnEnabled);
            } else {
                attemptsRemaining = parsePinPukErrorResult(ar);
                loge("Error change facility fdn with exception " + ar.exception);
            }
            Message response = ar.userObj;
            response.arg1 = attemptsRemaining;
            AsyncResult.forMessage(response).exception = ar.exception;
            response.sendToTarget();
        }
    }

    private void queryPin1State() {
        this.mCi.queryFacilityLockForApp(CommandsInterface.CB_FACILITY_BA_SIM, SpnOverride.MVNO_TYPE_NONE, 7, this.mAid, this.mHandler.obtainMessage(6));
    }

    /* JADX WARNING: Missing block: B:21:0x007e, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onQueryFacilityLock(AsyncResult ar) {
        boolean z = false;
        synchronized (this.mLock) {
            if (ar.exception != null) {
                log("Error in querying facility lock:" + ar.exception);
                return;
            }
            int[] ints = ar.result;
            if (ints.length != 0) {
                log("Query facility lock : " + ints[0]);
                if (ints[0] != 0) {
                    z = true;
                }
                this.mIccLockEnabled = z;
                if (this.mIccLockEnabled) {
                    this.mPinLockedRegistrants.notifyRegistrants();
                }
                switch (m16xc1fb5860()[this.mPin1State.ordinal()]) {
                    case 1:
                        if (this.mIccLockEnabled) {
                            loge("QUERY_FACILITY_LOCK:enabled GET_SIM_STATUS.Pin1:disabled. Fixme");
                            break;
                        }
                        break;
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        if (!this.mIccLockEnabled) {
                            loge("QUERY_FACILITY_LOCK:disabled GET_SIM_STATUS.Pin1:enabled. Fixme");
                            break;
                        }
                        break;
                }
                log("Ignoring: pin1state=" + this.mPin1State);
            } else {
                loge("Bogus facility lock response");
            }
        }
    }

    private void onChangeFacilityLock(AsyncResult ar) {
        synchronized (this.mLock) {
            int attemptsRemaining = -1;
            if (ar.exception == null) {
                this.mIccLockEnabled = this.mDesiredPinLocked;
                log("EVENT_CHANGE_FACILITY_LOCK_DONE: mIccLockEnabled= " + this.mIccLockEnabled);
            } else {
                attemptsRemaining = parsePinPukErrorResult(ar);
                loge("Error change facility lock with exception " + ar.exception);
            }
            Message response = ar.userObj;
            AsyncResult.forMessage(response).exception = ar.exception;
            response.arg1 = attemptsRemaining;
            response.sendToTarget();
        }
    }

    private int parsePinPukErrorResult(AsyncResult ar) {
        Integer result = ar.result;
        if (result == null) {
            return -1;
        }
        int attemptsRemaining = result.intValue();
        log("parsePinPukErrorResult: attemptsRemaining=" + attemptsRemaining);
        return attemptsRemaining;
    }

    public void registerForReady(Handler h, int what, Object obj) {
        synchronized (this.mLock) {
            Registrant r = new Registrant(h, what, obj);
            this.mReadyRegistrants.add(r);
            notifyReadyRegistrantsIfNeeded(r);
        }
    }

    public void unregisterForReady(Handler h) {
        synchronized (this.mLock) {
            this.mReadyRegistrants.remove(h);
        }
    }

    public void registerForLocked(Handler h, int what, Object obj) {
        synchronized (this.mLock) {
            Registrant r = new Registrant(h, what, obj);
            this.mPinLockedRegistrants.add(r);
            notifyPinLockedRegistrantsIfNeeded(r);
        }
    }

    public void unregisterForLocked(Handler h) {
        synchronized (this.mLock) {
            this.mPinLockedRegistrants.remove(h);
        }
    }

    public void registerForNetworkLocked(Handler h, int what, Object obj) {
        synchronized (this.mLock) {
            Registrant r = new Registrant(h, what, obj);
            this.mNetworkLockedRegistrants.add(r);
            notifyNetworkLockedRegistrantsIfNeeded(r);
        }
    }

    public void unregisterForNetworkLocked(Handler h) {
        synchronized (this.mLock) {
            this.mNetworkLockedRegistrants.remove(h);
        }
    }

    private void notifyReadyRegistrantsIfNeeded(Registrant r) {
        if (!this.mDestroyed && this.mAppState == AppState.APPSTATE_READY) {
            if (this.mPin1State == PinState.PINSTATE_ENABLED_NOT_VERIFIED || this.mPin1State == PinState.PINSTATE_ENABLED_BLOCKED || this.mPin1State == PinState.PINSTATE_ENABLED_PERM_BLOCKED) {
                loge("Sanity check failed! APPSTATE is ready while PIN1 is not verified!!!");
            } else if (r == null) {
                log("Notifying registrants: READY");
                this.mReadyRegistrants.notifyRegistrants();
            } else {
                log("Notifying 1 registrant: READY");
                r.notifyRegistrant(new AsyncResult(null, null, null));
            }
        }
    }

    private void notifyPinLockedRegistrantsIfNeeded(Registrant r) {
        if (!this.mDestroyed) {
            if (this.mAppState == AppState.APPSTATE_PIN || this.mAppState == AppState.APPSTATE_PUK) {
                if (this.mPin1State == PinState.PINSTATE_ENABLED_VERIFIED || this.mPin1State == PinState.PINSTATE_DISABLED) {
                    loge("Sanity check failed! APPSTATE is locked while PIN1 is not!!!");
                } else if (r == null) {
                    log("Notifying registrants: LOCKED");
                    this.mPinLockedRegistrants.notifyRegistrants();
                } else {
                    log("Notifying 1 registrant: LOCKED");
                    r.notifyRegistrant(new AsyncResult(null, null, null));
                }
            }
        }
    }

    private void notifyNetworkLockedRegistrantsIfNeeded(Registrant r) {
        if (!this.mDestroyed && this.mAppState == AppState.APPSTATE_SUBSCRIPTION_PERSO && isPersoLocked()) {
            AsyncResult ar = new AsyncResult(null, Integer.valueOf(this.mPersoSubState.ordinal()), null);
            if (r == null) {
                log("Notifying registrants: NETWORK_LOCKED");
                this.mNetworkLockedRegistrants.notifyRegistrants(ar);
            } else {
                log("Notifying 1 registrant: NETWORK_LOCED");
                r.notifyRegistrant(ar);
            }
        }
    }

    public AppState getState() {
        AppState appState;
        synchronized (this.mLock) {
            appState = this.mAppState;
        }
        return appState;
    }

    public AppType getType() {
        AppType appType;
        synchronized (this.mLock) {
            appType = this.mAppType;
        }
        return appType;
    }

    public int getAuthContext() {
        int i;
        synchronized (this.mLock) {
            i = this.mAuthContext;
        }
        return i;
    }

    private static int getAuthContext(AppType appType) {
        switch (m14x31a426ab()[appType.ordinal()]) {
            case 4:
                return 128;
            case 5:
                return 129;
            default:
                return -1;
        }
    }

    public PersoSubState getPersoSubState() {
        PersoSubState persoSubState;
        synchronized (this.mLock) {
            persoSubState = this.mPersoSubState;
        }
        return persoSubState;
    }

    public String getAid() {
        String str;
        synchronized (this.mLock) {
            str = this.mAid;
        }
        return str;
    }

    public String getAppLabel() {
        return this.mAppLabel;
    }

    public PinState getPin1State() {
        synchronized (this.mLock) {
            PinState universalPinState;
            if (this.mPin1Replaced) {
                universalPinState = this.mUiccCard.getUniversalPinState();
                return universalPinState;
            }
            universalPinState = this.mPin1State;
            return universalPinState;
        }
    }

    public IccFileHandler getIccFileHandler() {
        IccFileHandler iccFileHandler;
        synchronized (this.mLock) {
            iccFileHandler = this.mIccFh;
        }
        return iccFileHandler;
    }

    public IccRecords getIccRecords() {
        IccRecords iccRecords;
        synchronized (this.mLock) {
            iccRecords = this.mIccRecords;
        }
        return iccRecords;
    }

    public boolean isPersoLocked() {
        switch (m15x5ed1affa()[this.mPersoSubState.ordinal()]) {
            case 1:
            case 2:
            case 3:
                return false;
            default:
                return true;
        }
    }

    public void supplyPin(String pin, Message onComplete) {
        synchronized (this.mLock) {
            this.mCi.supplyIccPinForApp(pin, this.mAid, this.mHandler.obtainMessage(1, onComplete));
        }
    }

    public void supplyPuk(String puk, String newPin, Message onComplete) {
        synchronized (this.mLock) {
            this.mCi.supplyIccPukForApp(puk, newPin, this.mAid, this.mHandler.obtainMessage(1, onComplete));
        }
    }

    public void supplyPin2(String pin2, Message onComplete) {
        synchronized (this.mLock) {
            this.mCi.supplyIccPin2ForApp(pin2, this.mAid, this.mHandler.obtainMessage(8, onComplete));
        }
    }

    public void supplyPuk2(String puk2, String newPin2, Message onComplete) {
        synchronized (this.mLock) {
            this.mCi.supplyIccPuk2ForApp(puk2, newPin2, this.mAid, this.mHandler.obtainMessage(8, onComplete));
        }
    }

    public void supplyNetworkDepersonalization(String pin, Message onComplete) {
        synchronized (this.mLock) {
            log("supplyNetworkDepersonalization");
            this.mCi.supplyNetworkDepersonalization(pin, onComplete);
        }
    }

    public boolean getIccLockEnabled() {
        return this.mIccLockEnabled;
    }

    public boolean getIccFdnEnabled() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mIccFdnEnabled;
        }
        return z;
    }

    public boolean getIccFdnAvailable() {
        return this.mIccFdnAvailable;
    }

    public void setIccLockEnabled(boolean enabled, String password, Message onComplete) {
        synchronized (this.mLock) {
            this.mDesiredPinLocked = enabled;
            this.mCi.setFacilityLockForApp(CommandsInterface.CB_FACILITY_BA_SIM, enabled, password, 7, this.mAid, this.mHandler.obtainMessage(7, onComplete));
        }
    }

    public void setIccFdnEnabled(boolean enabled, String password, Message onComplete) {
        synchronized (this.mLock) {
            this.mDesiredFdnEnabled = enabled;
            this.mCi.setFacilityLockForApp(CommandsInterface.CB_FACILITY_BA_FD, enabled, password, 15, this.mAid, this.mHandler.obtainMessage(5, onComplete));
        }
    }

    public void changeIccLockPassword(String oldPassword, String newPassword, Message onComplete) {
        synchronized (this.mLock) {
            log("changeIccLockPassword");
            this.mCi.changeIccPinForApp(oldPassword, newPassword, this.mAid, this.mHandler.obtainMessage(2, onComplete));
        }
    }

    public void changeIccFdnPassword(String oldPassword, String newPassword, Message onComplete) {
        synchronized (this.mLock) {
            log("changeIccFdnPassword");
            this.mCi.changeIccPin2ForApp(oldPassword, newPassword, this.mAid, this.mHandler.obtainMessage(3, onComplete));
        }
    }

    public boolean getIccPin2Blocked() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mPin2State == PinState.PINSTATE_ENABLED_BLOCKED;
        }
        return z;
    }

    public boolean getIccPuk2Blocked() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mPin2State == PinState.PINSTATE_ENABLED_PERM_BLOCKED;
        }
        return z;
    }

    public int getPhoneId() {
        return this.mUiccCard != null ? this.mUiccCard.getPhoneId() : -1;
    }

    protected UiccCard getUiccCard() {
        return this.mUiccCard;
    }

    private void log(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    private void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        int i;
        pw.println("UiccCardApplication: " + this);
        pw.println(" mUiccCard=" + this.mUiccCard);
        pw.println(" mAppState=" + this.mAppState);
        pw.println(" mAppType=" + this.mAppType);
        pw.println(" mPersoSubState=" + this.mPersoSubState);
        pw.println(" mAid=" + this.mAid);
        pw.println(" mAppLabel=" + this.mAppLabel);
        pw.println(" mPin1Replaced=" + this.mPin1Replaced);
        pw.println(" mPin1State=" + this.mPin1State);
        pw.println(" mPin2State=" + this.mPin2State);
        pw.println(" mIccFdnEnabled=" + this.mIccFdnEnabled);
        pw.println(" mDesiredFdnEnabled=" + this.mDesiredFdnEnabled);
        pw.println(" mIccLockEnabled=" + this.mIccLockEnabled);
        pw.println(" mDesiredPinLocked=" + this.mDesiredPinLocked);
        pw.println(" mCi=" + this.mCi);
        pw.println(" mIccRecords=" + this.mIccRecords);
        pw.println(" mIccFh=" + this.mIccFh);
        pw.println(" mDestroyed=" + this.mDestroyed);
        pw.println(" mReadyRegistrants: size=" + this.mReadyRegistrants.size());
        for (i = 0; i < this.mReadyRegistrants.size(); i++) {
            pw.println("  mReadyRegistrants[" + i + "]=" + ((Registrant) this.mReadyRegistrants.get(i)).getHandler());
        }
        pw.println(" mPinLockedRegistrants: size=" + this.mPinLockedRegistrants.size());
        for (i = 0; i < this.mPinLockedRegistrants.size(); i++) {
            pw.println("  mPinLockedRegistrants[" + i + "]=" + ((Registrant) this.mPinLockedRegistrants.get(i)).getHandler());
        }
        pw.println(" mNetworkLockedRegistrants: size=" + this.mNetworkLockedRegistrants.size());
        for (i = 0; i < this.mNetworkLockedRegistrants.size(); i++) {
            pw.println("  mNetworkLockedRegistrants[" + i + "]=" + ((Registrant) this.mNetworkLockedRegistrants.get(i)).getHandler());
        }
        pw.flush();
    }
}
