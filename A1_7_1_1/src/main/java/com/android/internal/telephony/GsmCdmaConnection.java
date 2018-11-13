package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Registrant;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.provider.Settings.Global;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.internal.telephony.Connection.PostDialState;
import com.android.internal.telephony.DriverCall.State;
import com.android.internal.telephony.cdma.CdmaCallWaitingNotification;
import com.android.internal.telephony.regionlock.RegionLockConstant;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppState;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.mediatek.common.MPlugin;
import com.mediatek.common.telephony.IGsmConnectionExt;
import com.mediatek.internal.telephony.ConferenceCallMessageHandler;
import com.mediatek.internal.telephony.ITelephonyEx;
import com.mediatek.internal.telephony.ITelephonyEx.Stub;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class GsmCdmaConnection extends Connection {
    /* renamed from: -com-android-internal-telephony-DriverCall$StateSwitchesValues */
    private static final /* synthetic */ int[] f8-com-android-internal-telephony-DriverCall$StateSwitchesValues = null;
    private static final boolean DBG = true;
    private static final int EVENT_CALL_DISCONNECTED = 100;
    static final int EVENT_DTMF_DELAY_DONE = 5;
    static final int EVENT_DTMF_DONE = 1;
    static final int EVENT_NEXT_POST_DIAL = 3;
    static final int EVENT_PAUSE_DONE = 2;
    static final int EVENT_WAKE_LOCK_TIMEOUT = 4;
    private static final String LOG_TAG = "GsmCdmaConnection";
    private static final int MO_CALL_VIBRATE_TIME = 200;
    static final int PAUSE_DELAY_FIRST_MILLIS_GSM = 500;
    static final int PAUSE_DELAY_MILLIS_CDMA = 2000;
    static final int PAUSE_DELAY_MILLIS_GSM = 3000;
    private static final String PROP_LOG_TAG = "GsmCdmaConn";
    private static final boolean VDBG = false;
    static final int WAKE_LOCK_TIMEOUT_MILLIS = 60000;
    private Context mContext;
    long mDisconnectTime;
    boolean mDisconnected;
    private int mDtmfToneDelay;
    Handler mHandler;
    public int mIndex;
    private boolean mIsEmergencyCall;
    private boolean mIsRealConnected;
    boolean mIsVideo;
    Connection mOrigConnection;
    GsmCdmaCallTracker mOwner;
    GsmCdmaCall mParent;
    private WakeLock mPartialWakeLock;
    int mPreciseCause;
    private boolean mReceivedAccepted;
    TelephonyDevController mTelDevController;
    UUSInfo mUusInfo;
    String mVendorCause;

    class MyHandler extends Handler {
        final /* synthetic */ GsmCdmaConnection this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.GsmCdmaConnection.MyHandler.<init>(com.android.internal.telephony.GsmCdmaConnection, android.os.Looper):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        MyHandler(com.android.internal.telephony.GsmCdmaConnection r1, android.os.Looper r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.GsmCdmaConnection.MyHandler.<init>(com.android.internal.telephony.GsmCdmaConnection, android.os.Looper):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.GsmCdmaConnection.MyHandler.<init>(com.android.internal.telephony.GsmCdmaConnection, android.os.Looper):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.internal.telephony.GsmCdmaConnection.MyHandler.handleMessage(android.os.Message):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public void handleMessage(android.os.Message r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.internal.telephony.GsmCdmaConnection.MyHandler.handleMessage(android.os.Message):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.GsmCdmaConnection.MyHandler.handleMessage(android.os.Message):void");
        }
    }

    /* renamed from: -getcom-android-internal-telephony-DriverCall$StateSwitchesValues */
    private static /* synthetic */ int[] m16xd9c92f69() {
        if (f8-com-android-internal-telephony-DriverCall$StateSwitchesValues != null) {
            return f8-com-android-internal-telephony-DriverCall$StateSwitchesValues;
        }
        int[] iArr = new int[State.values().length];
        try {
            iArr[State.ACTIVE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[State.ALERTING.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[State.DIALING.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[State.HOLDING.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[State.INCOMING.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[State.WAITING.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        f8-com-android-internal-telephony-DriverCall$StateSwitchesValues = iArr;
        return iArr;
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.GsmCdmaConnection.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.GsmCdmaConnection.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.GsmCdmaConnection.<clinit>():void");
    }

    private boolean hasC2kOverImsModem() {
        if (this.mTelDevController == null || this.mTelDevController.getModem(0) == null || !this.mTelDevController.getModem(0).hasC2kOverImsModem()) {
            return false;
        }
        return true;
    }

    public GsmCdmaConnection(GsmCdmaPhone phone, DriverCall dc, GsmCdmaCallTracker ct, int index) {
        super(phone.getPhoneType());
        this.mPreciseCause = 0;
        this.mIsEmergencyCall = false;
        this.mDtmfToneDelay = 0;
        this.mTelDevController = TelephonyDevController.getInstance();
        createWakeLock(phone.getContext());
        acquireWakeLock();
        this.mOwner = ct;
        this.mHandler = new MyHandler(this, this.mOwner.getLooper());
        this.mAddress = dc.number;
        if (!hasC2kOverImsModem() || TelephonyManager.getDefault().hasIccCard(phone.getPhoneId())) {
            this.mIsEmergencyCall = PhoneNumberUtils.isLocalEmergencyNumber(phone.getContext(), phone.getSubId(), this.mAddress);
        } else {
            this.mIsEmergencyCall = PhoneNumberUtils.isLocalEmergencyNumber(phone.getContext(), this.mAddress);
        }
        this.mIsIncoming = dc.isMT;
        this.mCreateTime = System.currentTimeMillis();
        this.mCnapName = dc.name;
        this.mCnapNamePresentation = dc.namePresentation;
        this.mNumberPresentation = dc.numberPresentation;
        this.mUusInfo = dc.uusInfo;
        this.mIndex = index;
        this.mIsVideo = dc.isVideo;
        this.mParent = parentFromDCState(dc.state);
        this.mParent.attach(this, dc);
        fetchDtmfToneDelay(phone);
        this.mContext = phone.getContext();
    }

    public GsmCdmaConnection(GsmCdmaPhone phone, String dialString, GsmCdmaCallTracker ct, GsmCdmaCall parent, boolean isEmergencyCall) {
        super(phone.getPhoneType());
        this.mPreciseCause = 0;
        this.mIsEmergencyCall = false;
        this.mDtmfToneDelay = 0;
        this.mTelDevController = TelephonyDevController.getInstance();
        createWakeLock(phone.getContext());
        acquireWakeLock();
        this.mOwner = ct;
        this.mHandler = new MyHandler(this, this.mOwner.getLooper());
        if (isPhoneTypeGsm()) {
            this.mDialString = dialString;
        } else {
            Rlog.d(LOG_TAG, "[GsmCdmaConn] GsmCdmaConnection: dialString=" + maskDialString(dialString));
            dialString = formatDialString(dialString);
            Rlog.d(LOG_TAG, "[GsmCdmaConn] GsmCdmaConnection:formated dialString=" + maskDialString(dialString));
        }
        this.mAddress = PhoneNumberUtils.extractNetworkPortionAlt(dialString);
        this.mIsEmergencyCall = isEmergencyCall;
        this.mPostDialString = PhoneNumberUtils.extractPostDialPortion(dialString);
        this.mIndex = -1;
        this.mIsIncoming = false;
        this.mCnapName = null;
        this.mCnapNamePresentation = 1;
        this.mNumberPresentation = 1;
        this.mCreateTime = System.currentTimeMillis();
        if (parent != null) {
            this.mParent = parent;
            if (isPhoneTypeGsm()) {
                parent.attachFake(this, Call.State.DIALING);
            } else if (parent.mState == Call.State.ACTIVE) {
                parent.attachFake(this, Call.State.ACTIVE);
            } else {
                parent.attachFake(this, Call.State.DIALING);
            }
        }
        fetchDtmfToneDelay(phone);
        this.mIsRealConnected = false;
        this.mReceivedAccepted = false;
        if (this.mContext == null) {
            this.mContext = phone.getContext();
        }
    }

    public GsmCdmaConnection(Context context, CdmaCallWaitingNotification cw, GsmCdmaCallTracker ct, GsmCdmaCall parent) {
        super(parent.getPhone().getPhoneType());
        this.mPreciseCause = 0;
        this.mIsEmergencyCall = false;
        this.mDtmfToneDelay = 0;
        this.mTelDevController = TelephonyDevController.getInstance();
        createWakeLock(context);
        acquireWakeLock();
        this.mOwner = ct;
        this.mHandler = new MyHandler(this, this.mOwner.getLooper());
        this.mAddress = cw.number;
        this.mNumberPresentation = cw.numberPresentation;
        this.mCnapName = cw.name;
        this.mCnapNamePresentation = cw.namePresentation;
        this.mIndex = -1;
        this.mIsIncoming = true;
        this.mCreateTime = System.currentTimeMillis();
        this.mConnectTime = 0;
        this.mParent = parent;
        parent.attachFake(this, Call.State.WAITING);
    }

    public void dispose() {
        clearPostDialListeners();
        if (this.mParent != null) {
            this.mParent.detach(this);
        }
        releaseAllWakeLocks();
    }

    static boolean equalsHandlesNulls(Object a, Object b) {
        if (a == null) {
            return b == null;
        } else {
            return a.equals(b);
        }
    }

    public static String formatDialString(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        int length = phoneNumber.length();
        StringBuilder ret = new StringBuilder();
        int currIndex = 0;
        while (currIndex < length) {
            char c = phoneNumber.charAt(currIndex);
            if (!isPause(c) && !isWait(c)) {
                ret.append(c);
            } else if (currIndex < length - 1) {
                int nextIndex = findNextPCharOrNonPOrNonWCharIndex(phoneNumber, currIndex);
                if (nextIndex < length) {
                    ret.append(findPOrWCharToAppend(phoneNumber, currIndex, nextIndex));
                    if (nextIndex > currIndex + 1) {
                        currIndex = nextIndex - 1;
                    }
                } else if (nextIndex == length) {
                    currIndex = length - 1;
                }
            }
            currIndex++;
        }
        return PhoneNumberUtils.cdmaCheckAndProcessPlusCode(ret.toString());
    }

    boolean compareTo(DriverCall c) {
        if (!(!this.mIsIncoming ? c.isMT : true)) {
            return true;
        }
        if (isPhoneTypeGsm() && this.mOrigConnection != null) {
            return true;
        }
        return this.mIsIncoming == c.isMT ? equalsHandlesNulls(this.mAddress, PhoneNumberUtils.stringFromStringAndTOA(c.number, c.TOA)) : false;
    }

    public String getOrigDialString() {
        return this.mDialString;
    }

    public /* bridge */ /* synthetic */ Call getCall() {
        return getCall();
    }

    public GsmCdmaCall getCall() {
        return this.mParent;
    }

    public long getDisconnectTime() {
        return this.mDisconnectTime;
    }

    public long getHoldDurationMillis() {
        if (getState() != Call.State.HOLDING) {
            return 0;
        }
        return SystemClock.elapsedRealtime() - this.mHoldingStartTime;
    }

    public Call.State getState() {
        if (this.mDisconnected) {
            return Call.State.DISCONNECTED;
        }
        return super.getState();
    }

    public void hangup() throws CallStateException {
        if (this.mDisconnected) {
            throw new CallStateException(ConferenceCallMessageHandler.STATUS_DISCONNECTED);
        }
        this.mOwner.hangup(this);
    }

    public void separate() throws CallStateException {
        if (this.mDisconnected) {
            throw new CallStateException(ConferenceCallMessageHandler.STATUS_DISCONNECTED);
        }
        this.mOwner.separate(this);
    }

    public void proceedAfterWaitChar() {
        if (this.mPostDialState != PostDialState.WAIT) {
            Rlog.w(LOG_TAG, "GsmCdmaConnection.proceedAfterWaitChar(): Expected getPostDialState() to be WAIT but was " + this.mPostDialState);
            return;
        }
        setPostDialState(PostDialState.STARTED);
        processNextPostDialChar();
    }

    public void proceedAfterWildChar(String str) {
        if (this.mPostDialState != PostDialState.WILD) {
            Rlog.w(LOG_TAG, "GsmCdmaConnection.proceedAfterWaitChar(): Expected getPostDialState() to be WILD but was " + this.mPostDialState);
            return;
        }
        setPostDialState(PostDialState.STARTED);
        StringBuilder buf = new StringBuilder(str);
        buf.append(this.mPostDialString.substring(this.mNextPostDialChar));
        this.mPostDialString = buf.toString();
        this.mNextPostDialChar = 0;
        log("proceedAfterWildChar: new postDialString is " + this.mPostDialString);
        processNextPostDialChar();
    }

    public void cancelPostDial() {
        setPostDialState(PostDialState.CANCELLED);
    }

    void onHangupLocal() {
        this.mCause = 3;
        this.mPreciseCause = 0;
        this.mVendorCause = null;
    }

    int disconnectCauseFromCode(int causeCode) {
        AppState uiccAppState;
        switch (causeCode) {
            case 1:
                return 25;
            case 3:
                return 57;
            case 6:
                return 75;
            case 8:
                return 76;
            case 17:
                return 4;
            case 18:
                return 58;
            case 25:
                return 77;
            case 26:
                return 78;
            case 27:
                return 79;
            case 28:
                return 61;
            case 29:
                return 62;
            case 34:
            case 41:
            case 42:
            case 44:
            case 49:
            case 58:
                return 5;
            case 38:
            case 63:
                return 69;
            case 43:
                return 80;
            case 47:
                return 66;
            case 50:
                return 81;
            case 55:
                return 82;
            case 57:
                return 67;
            case 65:
                return 70;
            case CallFailCause.ACM_LIMIT_EXCEEDED /*68*/:
                return 15;
            case CallFailCause.FACILITY_NOT_IMPLEMENT /*69*/:
                return 71;
            case 70:
                return 72;
            case 81:
                return 83;
            case 87:
                return 84;
            case CallFailCause.INCOMPATIBLE_DESTINATION /*88*/:
                return 74;
            case CallFailCause.INVALID_TRANSIT_NETWORK_SELECTION /*91*/:
                return 85;
            case 95:
                return 86;
            case 96:
                return 87;
            case 97:
                return 88;
            case 98:
                return 89;
            case 99:
                return 90;
            case 100:
                return 91;
            case 101:
                return 92;
            case 102:
                return 93;
            case 111:
                return 94;
            case CallFailCause.CALL_BARRED /*240*/:
                return 20;
            case CallFailCause.FDN_BLOCKED /*241*/:
                return 21;
            case CallFailCause.IMEI_NOT_ACCEPTED /*243*/:
                if (PhoneNumberUtils.isEmergencyNumber(getAddress())) {
                    return 2;
                }
                break;
            case 244:
                return 46;
            case 245:
                return 47;
            case 246:
                return 48;
            case 1000:
                return 26;
            case 1001:
                return 27;
            case 1002:
                return 28;
            case 1003:
                return 29;
            case 1004:
                return 30;
            case 1005:
                return 31;
            case 1006:
                return 32;
            case 1007:
                return 33;
            case 1008:
                return 34;
            case 1009:
                return 35;
            case CallFailCause.CM_MM_RR_CONNECTION_RELEASE /*2165*/:
                return 96;
        }
        GsmCdmaPhone phone = this.mOwner.getPhone();
        int serviceState = phone.getServiceState().getState();
        UiccCardApplication cardApp = phone.getUiccCardApplication();
        if (cardApp != null) {
            uiccAppState = cardApp.getState();
        } else {
            uiccAppState = AppState.APPSTATE_UNKNOWN;
        }
        proprietaryLog("disconnectCauseFromCode, causeCode:" + causeCode + ", cardApp:" + cardApp + ", serviceState:" + serviceState + ", uiccAppState:" + uiccAppState);
        if (serviceState == 3) {
            return 17;
        }
        if (hasC2kOverImsModem() && !this.mIsEmergencyCall) {
            ITelephonyEx telEx = Stub.asInterface(ServiceManager.getService("phoneEx"));
            if (telEx != null) {
                try {
                    this.mIsEmergencyCall = telEx.isEccInProgress();
                } catch (RemoteException e) {
                    Rlog.e(PROP_LOG_TAG, "Exception of isEccInProgress");
                }
            }
        }
        if (!this.mIsEmergencyCall) {
            if (serviceState == 1 || serviceState == 2) {
                return 18;
            }
            if (uiccAppState != AppState.APPSTATE_READY && (isPhoneTypeGsm() || phone.mCdmaSubscriptionSource == 0)) {
                return 19;
            }
        }
        if (isPhoneTypeGsm() && causeCode == 65535) {
            if (phone.mSST.mRestrictedState.isCsRestricted()) {
                return 22;
            }
            if (phone.mSST.mRestrictedState.isCsEmergencyRestricted()) {
                return 24;
            }
            if (phone.mSST.mRestrictedState.isCsNormalRestricted()) {
                return 23;
            }
        }
        if (causeCode == 16) {
            return 2;
        }
        if (this.mIsEmergencyCall && (causeCode == 31 || causeCode == 79)) {
            return 2;
        }
        return 36;
    }

    void onRemoteDisconnect(int causeCode, String vendorCause) {
        this.mPreciseCause = causeCode;
        this.mVendorCause = vendorCause;
        onDisconnect(disconnectCauseFromCode(causeCode));
    }

    public boolean onDisconnect(int cause) {
        boolean changed = false;
        this.mCause = cause;
        if (!this.mDisconnected) {
            doDisconnect();
            Rlog.d(LOG_TAG, "onDisconnect: cause=" + cause);
            this.mOwner.getPhone().notifyDisconnect(this);
            if (this.mParent != null) {
                changed = this.mParent.connectionDisconnected(this);
            }
            this.mOrigConnection = null;
        }
        clearPostDialListeners();
        releaseWakeLock();
        if (OemConstant.EXP_VERSION) {
            oppoNotifyDisconnected();
        }
        return changed;
    }

    void onLocalDisconnect() {
        if (!this.mDisconnected) {
            doDisconnect();
            if (VDBG) {
                Rlog.d(LOG_TAG, "onLoalDisconnect");
            }
            if (this.mParent != null) {
                this.mParent.detach(this);
            }
        }
        releaseWakeLock();
    }

    public boolean update(DriverCall dc) {
        boolean z;
        boolean changed = false;
        boolean wasConnectingInOrOut = isConnectingInOrOut();
        boolean wasHolding = getState() == Call.State.HOLDING;
        GsmCdmaCall newParent = parentFromDCState(dc.state);
        log("parent= " + this.mParent + ", newParent= " + newParent);
        if (!isPhoneTypeGsm() || this.mOrigConnection == null) {
            log(" mNumberConverted " + this.mNumberConverted);
            if (!(equalsHandlesNulls(this.mAddress, dc.number) || (this.mNumberConverted && equalsHandlesNulls(this.mConvertedNumber, dc.number)))) {
                log("update: phone # changed!");
                this.mAddress = dc.number;
                changed = true;
            }
        } else {
            log("update: mOrigConnection is not null");
        }
        if (!(TextUtils.isEmpty(dc.name) || dc.name.equals(this.mCnapName))) {
            changed = true;
            this.mCnapName = dc.name;
        }
        log("--dssds----" + this.mCnapName);
        this.mCnapNamePresentation = dc.namePresentation;
        this.mNumberPresentation = dc.numberPresentation;
        if (this.mIsVideo != dc.isVideo) {
            this.mIsVideo = dc.isVideo;
            changed = true;
        }
        if (newParent != this.mParent) {
            if (this.mParent != null) {
                this.mParent.detach(this);
            }
            newParent.attach(this, dc);
            this.mParent = newParent;
            changed = true;
        } else {
            changed = !changed ? this.mParent.update(this, dc) : true;
        }
        StringBuilder append = new StringBuilder().append("update: parent=").append(this.mParent).append(", hasNewParent=");
        if (newParent != this.mParent) {
            z = true;
        } else {
            z = false;
        }
        log(append.append(z).append(", wasConnectingInOrOut=").append(wasConnectingInOrOut).append(", wasHolding=").append(wasHolding).append(", isConnectingInOrOut=").append(isConnectingInOrOut()).append(", isVideo=").append(this.mIsVideo).append(", changed=").append(changed).toString());
        if (wasConnectingInOrOut && !isConnectingInOrOut()) {
            onConnectedInOrOut();
        }
        if (changed && !wasHolding && getState() == Call.State.HOLDING) {
            onStartedHolding();
        }
        if (!isPhoneTypeGsm()) {
            proprietaryLog("state:" + getState() + ", mReceivedAccepted:" + this.mReceivedAccepted);
            if (getState() == Call.State.ACTIVE && this.mReceivedAccepted) {
                if (onCdmaCallAccept()) {
                    this.mOwner.mPhone.notifyCdmaCallAccepted();
                }
                this.mReceivedAccepted = false;
            }
        }
        return changed;
    }

    void fakeHoldBeforeDial() {
        if (this.mParent != null) {
            this.mParent.detach(this);
        }
        this.mParent = this.mOwner.mBackgroundCall;
        this.mParent.attachFake(this, Call.State.HOLDING);
        onStartedHolding();
    }

    void resumeHoldAfterDialFailed() {
        if (this.mParent != null) {
            this.mParent.detach(this);
        }
        this.mParent = this.mOwner.mForegroundCall;
        this.mParent.attachFake(this, Call.State.ACTIVE);
    }

    public int getGsmCdmaIndex() throws CallStateException {
        if (this.mIndex >= 0) {
            return this.mIndex + 1;
        }
        throw new CallStateException("GsmCdma index not yet assigned");
    }

    void onConnectedInOrOut() {
        this.mConnectTime = System.currentTimeMillis();
        this.mConnectTimeReal = SystemClock.elapsedRealtime();
        this.mDuration = 0;
        log("onConnectedInOrOut: connectTime=" + this.mConnectTime);
        if (this.mIsIncoming) {
            releaseWakeLock();
        } else if (isPhoneTypeGsm()) {
            processNextPostDialChar();
        } else {
            int count = this.mParent.mConnections.size();
            proprietaryLog("mParent.mConnections.size():" + count);
            if (!(isInChina() || this.mIsRealConnected || count != 1)) {
                this.mIsRealConnected = true;
                processNextPostDialChar();
                this.mOwner.mPhone.notifyCdmaCallAccepted();
            }
            if (count > 1) {
                this.mIsRealConnected = true;
                processNextPostDialChar();
            }
        }
    }

    private void doDisconnect() {
        this.mIndex = -1;
        this.mDisconnectTime = System.currentTimeMillis();
        this.mDuration = SystemClock.elapsedRealtime() - this.mConnectTimeReal;
        this.mDisconnected = true;
        clearPostDialListeners();
    }

    void onStartedHolding() {
        this.mHoldingStartTime = SystemClock.elapsedRealtime();
    }

    private boolean processPostDialChar(char c) {
        if (PhoneNumberUtils.is12Key(c)) {
            this.mOwner.mCi.sendBurstDtmf(Character.toString(c), 0, 0, this.mHandler.obtainMessage(1));
        } else if (isPause(c)) {
            if (!isPhoneTypeGsm()) {
                setPostDialState(PostDialState.PAUSE);
            }
            if (!isPhoneTypeGsm()) {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2), 2000);
            } else if (this.mNextPostDialChar != 1 || SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2), 3000);
            } else {
                try {
                    IGsmConnectionExt mGsmConnectionExt = (IGsmConnectionExt) MPlugin.createInstance(IGsmConnectionExt.class.getName(), this.mOwner.mPhone.getContext());
                    if (mGsmConnectionExt != null) {
                        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2), (long) mGsmConnectionExt.getFirstPauseDelayMSeconds(PAUSE_DELAY_FIRST_MILLIS_GSM));
                    } else {
                        Rlog.e(PROP_LOG_TAG, "Fail to initialize IGsmConnectionExt");
                    }
                } catch (Exception e) {
                    Rlog.e(PROP_LOG_TAG, "Fail to create plug-in");
                    e.printStackTrace();
                }
            }
        } else if (isWait(c)) {
            setPostDialState(PostDialState.WAIT);
        } else if (!isWild(c)) {
            return false;
        } else {
            setPostDialState(PostDialState.WILD);
        }
        return true;
    }

    public String getRemainingPostDialString() {
        String subStr = super.getRemainingPostDialString();
        if (isPhoneTypeGsm() || TextUtils.isEmpty(subStr)) {
            return subStr;
        }
        int wIndex = subStr.indexOf(59);
        int pIndex = subStr.indexOf(44);
        if (wIndex > 0 && (wIndex < pIndex || pIndex <= 0)) {
            return subStr.substring(0, wIndex);
        }
        if (pIndex > 0) {
            return subStr.substring(0, pIndex);
        }
        return subStr;
    }

    public void updateParent(GsmCdmaCall oldParent, GsmCdmaCall newParent) {
        if (newParent != oldParent) {
            if (oldParent != null) {
                oldParent.detach(this);
            }
            newParent.attachFake(this, Call.State.ACTIVE);
            this.mParent = newParent;
        }
    }

    protected void finalize() {
        if (this.mPartialWakeLock.isHeld()) {
            Rlog.e(LOG_TAG, "[GsmCdmaConn] UNEXPECTED; mPartialWakeLock is held when finalizing.");
        }
        clearPostDialListeners();
        releaseWakeLock();
    }

    private void processNextPostDialChar() {
        if (this.mPostDialState == PostDialState.CANCELLED) {
            releaseWakeLock();
            return;
        }
        char c;
        if (this.mPostDialString == null || this.mPostDialString.length() <= this.mNextPostDialChar || this.mDisconnected) {
            setPostDialState(PostDialState.COMPLETE);
            releaseWakeLock();
            c = 0;
        } else {
            setPostDialState(PostDialState.STARTED);
            String str = this.mPostDialString;
            int i = this.mNextPostDialChar;
            this.mNextPostDialChar = i + 1;
            c = str.charAt(i);
            if (!processPostDialChar(c)) {
                this.mHandler.obtainMessage(3).sendToTarget();
                Rlog.e(LOG_TAG, "processNextPostDialChar: c=" + c + " isn't valid!");
                return;
            }
        }
        notifyPostDialListenersNextChar(c);
        Registrant postDialHandler = this.mOwner.getPhone().getPostDialHandler();
        if (postDialHandler != null) {
            Message notifyMessage = postDialHandler.messageForRegistrant();
            if (notifyMessage != null) {
                PostDialState state = this.mPostDialState;
                AsyncResult ar = AsyncResult.forMessage(notifyMessage);
                ar.result = this;
                ar.userObj = state;
                notifyMessage.arg1 = c;
                notifyMessage.sendToTarget();
            }
        }
    }

    private boolean isConnectingInOrOut() {
        if (this.mParent == null || this.mParent == this.mOwner.mRingingCall || this.mParent.mState == Call.State.DIALING || this.mParent.mState == Call.State.ALERTING) {
            return true;
        }
        return false;
    }

    private GsmCdmaCall parentFromDCState(State state) {
        switch (m16xd9c92f69()[state.ordinal()]) {
            case 1:
            case 2:
            case 3:
                return this.mOwner.mForegroundCall;
            case 4:
                return this.mOwner.mBackgroundCall;
            case 5:
            case 6:
                return this.mOwner.mRingingCall;
            default:
                throw new RuntimeException("illegal call state: " + state);
        }
    }

    private void setPostDialState(PostDialState s) {
        if (s == PostDialState.STARTED || s == PostDialState.PAUSE) {
            synchronized (this.mPartialWakeLock) {
                if (this.mPartialWakeLock.isHeld()) {
                    this.mHandler.removeMessages(4);
                } else {
                    acquireWakeLock();
                }
                this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(4), 60000);
            }
        } else {
            this.mHandler.removeMessages(4);
            releaseWakeLock();
        }
        this.mPostDialState = s;
        notifyPostDialListeners();
    }

    private void createWakeLock(Context context) {
        this.mPartialWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, LOG_TAG);
    }

    private void acquireWakeLock() {
        log("acquireWakeLock, " + hashCode());
        this.mPartialWakeLock.acquire();
    }

    private void releaseWakeLock() {
        synchronized (this.mPartialWakeLock) {
            if (this.mPartialWakeLock.isHeld()) {
                log("releaseWakeLock, " + hashCode());
                this.mPartialWakeLock.release();
            }
        }
    }

    private void releaseAllWakeLocks() {
        synchronized (this.mPartialWakeLock) {
            while (this.mPartialWakeLock.isHeld()) {
                this.mPartialWakeLock.release();
            }
        }
    }

    private static boolean isPause(char c) {
        return c == ',';
    }

    private static boolean isWait(char c) {
        return c == ';';
    }

    private static boolean isWild(char c) {
        return c == 'N';
    }

    private static int findNextPCharOrNonPOrNonWCharIndex(String phoneNumber, int currIndex) {
        boolean wMatched = isWait(phoneNumber.charAt(currIndex));
        int index = currIndex + 1;
        int length = phoneNumber.length();
        while (index < length) {
            char cNext = phoneNumber.charAt(index);
            if (isWait(cNext)) {
                wMatched = true;
            }
            if (!isWait(cNext) && !isPause(cNext)) {
                break;
            }
            index++;
        }
        if (index >= length || index <= currIndex + 1 || wMatched || !isPause(phoneNumber.charAt(currIndex))) {
            return index;
        }
        return currIndex + 1;
    }

    private static char findPOrWCharToAppend(String phoneNumber, int currPwIndex, int nextNonPwCharIndex) {
        char ret = isPause(phoneNumber.charAt(currPwIndex)) ? ',' : ';';
        if (nextNonPwCharIndex > currPwIndex + 1) {
            return ';';
        }
        return ret;
    }

    private String maskDialString(String dialString) {
        if (VDBG) {
            return dialString;
        }
        return "<MASKED>";
    }

    private void fetchDtmfToneDelay(GsmCdmaPhone phone) {
        PersistableBundle b = ((CarrierConfigManager) phone.getContext().getSystemService("carrier_config")).getConfigForSubId(phone.getSubId());
        if (b != null) {
            this.mDtmfToneDelay = b.getInt(phone.getDtmfToneDelayKey());
        }
    }

    private boolean isPhoneTypeGsm() {
        return this.mOwner.getPhone().getPhoneType() == 1;
    }

    private void log(String msg) {
        Rlog.d(LOG_TAG, "[GsmCdmaConn] " + msg);
    }

    public int getNumberPresentation() {
        return this.mNumberPresentation;
    }

    public UUSInfo getUUSInfo() {
        return this.mUusInfo;
    }

    public int getPreciseDisconnectCause() {
        return this.mPreciseCause;
    }

    public String getVendorDisconnectCause() {
        return this.mVendorCause;
    }

    public void migrateFrom(Connection c) {
        if (c != null) {
            super.migrateFrom(c);
            this.mUusInfo = c.getUUSInfo();
            setUserData(c.getUserData());
        }
    }

    public Connection getOrigConnection() {
        return this.mOrigConnection;
    }

    public boolean isMultiparty() {
        if (this.mParent != null) {
            return this.mParent.isMultiparty();
        }
        return false;
    }

    public void onReplaceDisconnect(int cause) {
        this.mCause = cause;
        if (!this.mDisconnected) {
            this.mIndex = -1;
            this.mDisconnectTime = System.currentTimeMillis();
            this.mDuration = SystemClock.elapsedRealtime() - this.mConnectTimeReal;
            this.mDisconnected = true;
            log("onReplaceDisconnect: cause=" + cause);
            if (this.mParent != null) {
                this.mParent.connectionDisconnected(this);
            }
        }
        releaseWakeLock();
    }

    public boolean isRealConnected() {
        return this.mIsRealConnected;
    }

    boolean onCdmaCallAccept() {
        proprietaryLog("onCdmaCallAccept, mIsRealConnected:" + this.mIsRealConnected + ", state:" + getState());
        if (getState() != Call.State.ACTIVE) {
            this.mReceivedAccepted = true;
            return false;
        }
        this.mConnectTimeReal = SystemClock.elapsedRealtime();
        this.mDuration = 0;
        this.mConnectTime = System.currentTimeMillis();
        if (!this.mIsRealConnected) {
            this.mIsRealConnected = true;
            processNextPostDialChar();
        }
        return true;
    }

    private boolean isInChina() {
        return true;
    }

    private void vibrateForAccepted() {
        ((Vibrator) this.mOwner.mPhone.getContext().getSystemService("vibrator")).vibrate(200);
    }

    public boolean isVideo() {
        proprietaryLog("GsmConnection: isVideo = " + this.mIsVideo);
        return this.mIsVideo;
    }

    void updateConferenceParticipantAddress(String address) {
        this.mAddress = address;
    }

    void proprietaryLog(String s) {
        Rlog.d(PROP_LOG_TAG, s);
    }

    private void oppoNotifyDisconnected() {
        if (RegionLockConstant.IS_REGION_LOCK && RegionLockConstant.getRegionLockStatus()) {
            if (Global.getInt(this.mContext.getContentResolver(), "oppo_emergency_call_on", 0) == 1) {
                this.mHandler.sendMessage(this.mHandler.obtainMessage(100));
            }
            Global.putInt(this.mContext.getContentResolver(), "oppo_emergency_call_on", 0);
        }
    }

    private void sendBroadCastUpdateRadioStatus() {
        this.mContext.sendBroadcast(new Intent(RegionLockConstant.ACTION_CALLING_DISCONNECTED));
    }
}
