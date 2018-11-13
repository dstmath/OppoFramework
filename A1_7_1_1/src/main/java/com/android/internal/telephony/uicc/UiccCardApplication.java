package com.android.internal.telephony.uicc;

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
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppState;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.PersoSubState;
import com.android.internal.telephony.uicc.IccCardStatus.PinState;
import com.android.internal.telephony.uicc.IccConstants.IccService;
import com.android.internal.telephony.uicc.IccConstants.IccServiceStatus;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import java.io.FileDescriptor;
import java.io.PrintWriter;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class UiccCardApplication {
    /* renamed from: -com-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues */
    private static final /* synthetic */ int[] f12x1911c1cf = null;
    /* renamed from: -com-android-internal-telephony-uicc-IccCardStatus$PinStateSwitchesValues */
    private static final /* synthetic */ int[] f13xb5f5d084 = null;
    public static final int AUTH_CONTEXT_EAP_AKA = 129;
    public static final int AUTH_CONTEXT_EAP_SIM = 128;
    public static final int AUTH_CONTEXT_UNDEFINED = -1;
    public static final int CAT_CORPORATE = 3;
    public static final int CAT_NETOWRK_SUBSET = 1;
    public static final int CAT_NETWOEK = 0;
    public static final int CAT_SERVICE_PROVIDER = 2;
    public static final int CAT_SIM = 4;
    private static final boolean DBG = true;
    private static final int EVENT_CHANGE_FACILITY_FDN_DONE = 5;
    private static final int EVENT_CHANGE_FACILITY_LOCK_DONE = 7;
    private static final int EVENT_CHANGE_NETWORK_LOCK_DONE = 102;
    private static final int EVENT_CHANGE_PIN1_DONE = 2;
    private static final int EVENT_CHANGE_PIN2_DONE = 3;
    private static final int EVENT_PIN1_PUK1_DONE = 1;
    private static final int EVENT_PIN2_PUK2_DONE = 8;
    private static final int EVENT_PUK1_CHANGE_PIN1_DONE = 104;
    private static final int EVENT_PUK2_CHANGE_PIN2_DONE = 105;
    private static final int EVENT_QUERY_FACILITY_FDN_DONE = 4;
    private static final int EVENT_QUERY_FACILITY_LOCK_DONE = 6;
    private static final int EVENT_QUERY_NETWORK_LOCK_DONE = 101;
    private static final int EVENT_RADIO_NOTAVAILABLE = 103;
    private static final int EVENT_RADIO_UNAVAILABLE = 9;
    private static final String LOG_TAG = "UiccCardApplication";
    public static final int OP_ADD = 2;
    public static final int OP_LOCK = 1;
    public static final int OP_PERMANENT_UNLOCK = 4;
    public static final int OP_REMOVE = 3;
    public static final int OP_UNLOCK = 0;
    private static final String[] PROPERTY_PIN1_RETRY = null;
    private static final String[] PROPERTY_PIN2_RETRY = null;
    static final String[] UICCCARDAPPLICATION_PROPERTY_RIL_UICC_TYPE = null;
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
    private RegistrantList mFdnChangedRegistrants;
    private Handler mHandler;
    private boolean mIccFdnAvailable;
    private boolean mIccFdnEnabled;
    private IccFileHandler mIccFh;
    private boolean mIccLockEnabled;
    private IccRecords mIccRecords;
    protected String mIccType;
    private final Object mLock;
    private RegistrantList mNetworkLockedRegistrants;
    private PersoSubState mPersoSubState;
    private int mPhoneId;
    private boolean mPin1Replaced;
    private PinState mPin1State;
    private PinState mPin2State;
    private RegistrantList mPinLockedRegistrants;
    private RegistrantList mReadyRegistrants;
    private UiccCard mUiccCard;

    /* renamed from: -getcom-android-internal-telephony-uicc-IccCardApplicationStatus$AppTypeSwitchesValues */
    private static /* synthetic */ int[] m20x31a426ab() {
        if (f12x1911c1cf != null) {
            return f12x1911c1cf;
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
            iArr[AppType.APPTYPE_UNKNOWN.ordinal()] = 12;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[AppType.APPTYPE_USIM.ordinal()] = 5;
        } catch (NoSuchFieldError e6) {
        }
        f12x1911c1cf = iArr;
        return iArr;
    }

    /* renamed from: -getcom-android-internal-telephony-uicc-IccCardStatus$PinStateSwitchesValues */
    private static /* synthetic */ int[] m21xc1fb5860() {
        if (f13xb5f5d084 != null) {
            return f13xb5f5d084;
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
        f13xb5f5d084 = iArr;
        return iArr;
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.uicc.UiccCardApplication.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.uicc.UiccCardApplication.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.UiccCardApplication.<clinit>():void");
    }

    public UiccCardApplication(UiccCard uiccCard, IccCardApplicationStatus as, Context c, CommandsInterface ci) {
        boolean z = true;
        this.mLock = new Object();
        this.mIccFdnAvailable = true;
        this.mReadyRegistrants = new RegistrantList();
        this.mPinLockedRegistrants = new RegistrantList();
        this.mNetworkLockedRegistrants = new RegistrantList();
        this.mIccType = null;
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                Message response;
                if (UiccCardApplication.this.mDestroyed) {
                    if (1 == msg.what || 101 == msg.what) {
                        response = msg.obj.userObj;
                        AsyncResult.forMessage(response).exception = CommandException.fromRilErrno(1);
                        UiccCardApplication.this.loge("Received message " + msg + "[" + msg.what + "] while being destroyed. return exception.");
                        response.arg1 = -1;
                        response.sendToTarget();
                    } else {
                        UiccCardApplication.this.loge("Received message " + msg + "[" + msg.what + "] while being destroyed. Ignoring.");
                    }
                    return;
                }
                AsyncResult ar;
                switch (msg.what) {
                    case 1:
                    case 2:
                    case 3:
                    case 8:
                        int attemptsRemaining = -1;
                        ar = (AsyncResult) msg.obj;
                        if (!(ar.exception == null || ar.result == null)) {
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
                    case 101:
                        UiccCardApplication.this.log("handleMessage (EVENT_QUERY_NETWORK_LOCK)");
                        ar = (AsyncResult) msg.obj;
                        if (ar.exception != null) {
                            Rlog.e(UiccCardApplication.LOG_TAG, "Error query network lock with exception " + ar.exception);
                        }
                        AsyncResult.forMessage((Message) ar.userObj, ar.result, ar.exception);
                        ((Message) ar.userObj).sendToTarget();
                        break;
                    case 102:
                        UiccCardApplication.this.log("handleMessage (EVENT_CHANGE_NETWORK_LOCK)");
                        ar = (AsyncResult) msg.obj;
                        if (ar.exception != null) {
                            Rlog.e(UiccCardApplication.LOG_TAG, "Error change network lock with exception " + ar.exception);
                        }
                        AsyncResult.forMessage((Message) ar.userObj).exception = ar.exception;
                        ((Message) ar.userObj).sendToTarget();
                        break;
                    case 104:
                        UiccCardApplication.this.log("EVENT_PUK1_CHANGE_PIN1_DONE");
                        int attemptsRemainingPuk = -1;
                        ar = (AsyncResult) msg.obj;
                        if (!(ar.exception == null || ar.result == null)) {
                            attemptsRemainingPuk = UiccCardApplication.this.parsePinPukErrorResult(ar);
                        }
                        Message responsePuk = ar.userObj;
                        AsyncResult.forMessage(responsePuk).exception = ar.exception;
                        responsePuk.arg1 = attemptsRemainingPuk;
                        responsePuk.sendToTarget();
                        UiccCardApplication.this.queryPin1State();
                        break;
                    case 105:
                        int attemptsRemainingPuk2 = -1;
                        ar = (AsyncResult) msg.obj;
                        if (!(ar.exception == null || ar.result == null)) {
                            attemptsRemainingPuk2 = UiccCardApplication.this.parsePinPukErrorResult(ar);
                        }
                        Message responsePuk2 = ar.userObj;
                        AsyncResult.forMessage(responsePuk2).exception = ar.exception;
                        responsePuk2.arg1 = attemptsRemainingPuk2;
                        responsePuk2.sendToTarget();
                        UiccCardApplication.this.queryFdn();
                        break;
                    default:
                        UiccCardApplication.this.loge("Unknown Event " + msg.what);
                        break;
                }
            }
        };
        this.mFdnChangedRegistrants = new RegistrantList();
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
        this.mPhoneId = this.mUiccCard.getPhoneId();
        this.mIccFh = createIccFileHandler(as.app_type);
        this.mIccRecords = createIccRecords(as.app_type, this.mContext, this.mCi);
        if (this.mAppState == AppState.APPSTATE_READY && this.mAppType != AppType.APPTYPE_ISIM) {
            queryFdn();
            queryPin1State();
        }
        this.mCi.registerForNotAvailable(this.mHandler, 9, null);
    }

    /* JADX WARNING: Missing block: B:33:0x011f, code:
            return;
     */
    /* JADX WARNING: Missing block: B:49:0x0142, code:
            if (r1 == com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType.APPTYPE_SIM) goto L_0x0130;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void update(IccCardApplicationStatus as, Context c, CommandsInterface ci) {
        boolean z = false;
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
            if (as.pin1_replaced != 0) {
                z = true;
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
            log("mPersoSubState: " + this.mPersoSubState + " oldPersoSubState: " + oldPersoSubState);
            if (this.mPersoSubState != oldPersoSubState) {
                notifyNetworkLockedRegistrantsIfNeeded(null);
            }
            log("update,  mAppState=" + this.mAppState + "  oldAppState=" + oldAppState);
            if (this.mAppState != oldAppState) {
                log(oldAppType + " changed state: " + oldAppState + " -> " + this.mAppState);
                if (this.mAppState == AppState.APPSTATE_READY && this.mAppType != AppType.APPTYPE_ISIM) {
                    queryFdn();
                    queryPin1State();
                }
                notifyPinLockedRegistrantsIfNeeded(null);
                notifyReadyRegistrantsIfNeeded(null);
            } else if (this.mAppState == AppState.APPSTATE_READY) {
                if (!(this.mAppType == AppType.APPTYPE_SIM && oldAppType == AppType.APPTYPE_RUIM)) {
                    if (this.mAppType == AppType.APPTYPE_RUIM) {
                    }
                }
                queryFdn();
                queryPin1State();
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
        log("createIccRecords, AppType = " + type);
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
        switch (m20x31a426ab()[type.ordinal()]) {
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
        this.mCi.queryFacilityLockForApp(CommandsInterface.CB_FACILITY_BA_FD, UsimPBMemInfo.STRING_NOT_SET, 7, this.mAid, this.mHandler.obtainMessage(4));
    }

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
            notifyFdnChangedRegistrants();
        }
    }

    private void onChangeFdnDone(AsyncResult ar) {
        synchronized (this.mLock) {
            int attemptsRemaining = -1;
            boolean bNotifyFdnChanged = false;
            if (ar.exception == null) {
                this.mIccFdnEnabled = this.mDesiredFdnEnabled;
                log("EVENT_CHANGE_FACILITY_FDN_DONE: mIccFdnEnabled=" + this.mIccFdnEnabled);
                bNotifyFdnChanged = true;
            } else {
                attemptsRemaining = parsePinPukErrorResult(ar);
                loge("Error change facility fdn with exception " + ar.exception);
            }
            Message response = ar.userObj;
            response.arg1 = attemptsRemaining;
            AsyncResult.forMessage(response).exception = ar.exception;
            response.sendToTarget();
            if (bNotifyFdnChanged) {
                notifyFdnChangedRegistrants();
            }
        }
    }

    private void queryPin1State() {
        this.mCi.queryFacilityLockForApp(CommandsInterface.CB_FACILITY_BA_SIM, UsimPBMemInfo.STRING_NOT_SET, 7, this.mAid, this.mHandler.obtainMessage(6));
    }

    /* JADX WARNING: Missing block: B:21:0x007e, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onQueryFacilityLock(AsyncResult ar) {
        boolean z = false;
        synchronized (this.mLock) {
            if (ar.exception != null) {
                loge("Error in querying facility lock:" + ar.exception);
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
                switch (m21xc1fb5860()[this.mPin1State.ordinal()]) {
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
        int[] result = ar.result;
        if (result == null) {
            return -1;
        }
        int attemptsRemaining = -1;
        if (result.length > 0) {
            attemptsRemaining = result[0];
        }
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
        if (!this.mDestroyed && this.mAppState == AppState.APPSTATE_SUBSCRIPTION_PERSO) {
            if (r == null) {
                log("Notifying registrants: NETWORK_LOCKED");
                this.mNetworkLockedRegistrants.notifyRegistrants();
            } else {
                log("Notifying 1 registrant: NETWORK_LOCED");
                r.notifyRegistrant(new AsyncResult(null, null, null));
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
        switch (m20x31a426ab()[appType.ordinal()]) {
            case 2:
            case 5:
                return 129;
            case 4:
                return 128;
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

    public void supplyPin(String pin, Message onComplete) {
        synchronized (this.mLock) {
            this.mCi.supplyIccPinForApp(pin, this.mAid, this.mHandler.obtainMessage(1, onComplete));
        }
    }

    public void supplyPuk(String puk, String newPin, Message onComplete) {
        synchronized (this.mLock) {
            log("supplyPuk");
            this.mCi.supplyIccPukForApp(puk, newPin, this.mAid, this.mHandler.obtainMessage(104, onComplete));
        }
    }

    public void supplyPin2(String pin2, Message onComplete) {
        synchronized (this.mLock) {
            this.mCi.supplyIccPin2ForApp(pin2, this.mAid, this.mHandler.obtainMessage(8, onComplete));
        }
    }

    public void supplyPuk2(String puk2, String newPin2, Message onComplete) {
        synchronized (this.mLock) {
            this.mCi.supplyIccPuk2ForApp(puk2, newPin2, this.mAid, this.mHandler.obtainMessage(105, onComplete));
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
        boolean z = false;
        if (this.mIccRecords == null) {
            log("isFdnExist mIccRecords == null");
            return false;
        }
        IccServiceStatus iccSerStatus = this.mIccRecords.getSIMServiceStatus(IccService.FDN);
        log("getIccFdnAvailable status: iccSerStatus");
        if (iccSerStatus == IccServiceStatus.ACTIVATED) {
            z = this.mIccRecords.isPhbReady();
        }
        return z;
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
        return this.mUiccCard.getPhoneId();
    }

    protected UiccCard getUiccCard() {
        return this.mUiccCard;
    }

    private void log(String msg) {
        Rlog.d(LOG_TAG, msg + " (slot " + this.mPhoneId + ")");
    }

    private void loge(String msg) {
        Rlog.e(LOG_TAG, msg + " (slot " + this.mPhoneId + ")");
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

    public int getSlotId() {
        return this.mPhoneId;
    }

    private void notifyFdnChangedRegistrants() {
        if (!this.mDestroyed) {
            log("notifyFdnChangedRegistrants");
            this.mFdnChangedRegistrants.notifyRegistrants();
        }
    }

    public String getIccCardType() {
        if (this.mIccType == null || this.mIccType.equals(UsimPBMemInfo.STRING_NOT_SET)) {
            this.mIccType = SystemProperties.get(UICCCARDAPPLICATION_PROPERTY_RIL_UICC_TYPE[this.mPhoneId]);
        }
        log("getIccCardType(): mIccType = " + this.mIccType);
        return this.mIccType;
    }

    public void queryIccNetworkLock(int category, Message onComplete) {
        log("queryIccNetworkLock(): category =  " + category);
        switch (category) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
                this.mCi.queryNetworkLock(category, this.mHandler.obtainMessage(101, onComplete));
                return;
            default:
                Rlog.e(LOG_TAG, "queryIccNetworkLock unknown category = " + category);
                return;
        }
    }

    public void setIccNetworkLockEnabled(int category, int lockop, String password, String data_imsi, String gid1, String gid2, Message onComplete) {
        log("SetIccNetworkEnabled(): category = " + category + " lockop = " + lockop + " password = " + password + " data_imsi = " + data_imsi + " gid1 = " + gid1 + " gid2 = " + gid2);
        switch (lockop) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
                this.mCi.setNetworkLock(category, lockop, password, data_imsi, gid1, gid2, this.mHandler.obtainMessage(102, onComplete));
                return;
            default:
                Rlog.e(LOG_TAG, "SetIccNetworkEnabled unknown operation" + lockop);
                return;
        }
    }

    public void setCarrierRestrictionState(int state, String password, Message onComplete) {
        log("setCarrierRestrictionState(): state: " + state + "password = " + password);
        if (this.mCi != null) {
            this.mCi.setCarrierRestrictionState(state, password, onComplete);
        }
    }

    public void getCarrierRestrictionState(Message onComplete) {
        log("getCarrierRestrictionState()");
        if (this.mCi != null) {
            this.mCi.getCarrierRestrictionState(onComplete);
        }
    }
}
