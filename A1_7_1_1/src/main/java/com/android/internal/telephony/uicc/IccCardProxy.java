package com.android.internal.telephony.uicc;

import android.app.ActivityManagerNative;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.CommandsInterface.RadioState;
import com.android.internal.telephony.IccCard;
import com.android.internal.telephony.IccCardConstants.State;
import com.android.internal.telephony.MccTable;
import com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppState;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.PersoSubState;
import com.android.internal.telephony.uicc.IccCardStatus.CardState;
import com.android.internal.telephony.uicc.IccCardStatus.PinState;
import com.mediatek.internal.telephony.dataconnection.DataSubSelector;
import com.mediatek.internal.telephony.worldphone.IWorldPhone;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

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
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
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
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
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
public class IccCardProxy extends Handler implements IccCard {
    /* renamed from: -com-android-internal-telephony-IccCardConstants$StateSwitchesValues */
    private static final /* synthetic */ int[] f22x8dbfd0b5 = null;
    /* renamed from: -com-android-internal-telephony-uicc-IccCardApplicationStatus$AppStateSwitchesValues */
    private static final /* synthetic */ int[] f23x3dee1264 = null;
    /* renamed from: -com-android-internal-telephony-uicc-IccCardApplicationStatus$PersoSubStateSwitchesValues */
    private static final /* synthetic */ int[] f24x16bf601e = null;
    public static final String ACTION_INTERNAL_SIM_STATE_CHANGED = "android.intent.action.internal_sim_state_changed";
    private static final String COMMON_SLOT_PROPERTY = "";
    private static final boolean DBG = true;
    private static final int EVENT_APP_READY = 6;
    private static final int EVENT_CARRIER_PRIVILIGES_LOADED = 503;
    private static final int EVENT_CDMA_SUBSCRIPTION_SOURCE_CHANGED = 11;
    private static final int EVENT_ICC_ABSENT = 4;
    private static final int EVENT_ICC_CHANGED = 3;
    private static final int EVENT_ICC_FDN_CHANGED = 101;
    private static final int EVENT_ICC_LOCKED = 5;
    private static final int EVENT_ICC_RECORD_EVENTS = 500;
    private static final int EVENT_ICC_RECOVERY = 100;
    private static final int EVENT_IMSI_READY = 8;
    private static final int EVENT_NETWORK_LOCKED = 9;
    private static final int EVENT_RADIO_OFF_OR_UNAVAILABLE = 1;
    private static final int EVENT_RADIO_ON = 2;
    private static final int EVENT_RECORDS_LOADED = 7;
    private static final int EVENT_SUBSCRIPTION_ACTIVATED = 501;
    private static final int EVENT_SUBSCRIPTION_DEACTIVATED = 502;
    private static final String ICCID_STRING_FOR_NO_SIM = "N/A";
    private static final String LOG_TAG = "IccCardProxy";
    private static final String[] PROPERTY_RIL_FULL_UICC_TYPE = null;
    private static Intent sInternalIntent;
    static List<SimStateListener> sSimStateListeners;
    private String[] PROPERTY_ICCID_SIM;
    private RegistrantList mAbsentRegistrants;
    private CdmaSubscriptionSourceManager mCdmaSSM;
    private CommandsInterface mCi;
    private Context mContext;
    private int mCurrentAppType;
    private State mExternalState;
    private RegistrantList mFdnChangedRegistrants;
    private IccRecords mIccRecords;
    private boolean mInitialized;
    private final Object mLock;
    private PersoSubState mNetworkLockState;
    private RegistrantList mNetworkLockedRegistrants;
    private Integer mPhoneId;
    private RegistrantList mPinLockedRegistrants;
    private boolean mQuietMode;
    private boolean mRadioOn;
    private RegistrantList mRecoveryRegistrants;
    private TelephonyManager mTelephonyManager;
    private UiccCardApplication mUiccApplication;
    private UiccCard mUiccCard;
    private UiccController mUiccController;
    private final BroadcastReceiver sReceiver;

    public static abstract class SimStateListener {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.uicc.IccCardProxy.SimStateListener.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        public SimStateListener() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.uicc.IccCardProxy.SimStateListener.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.IccCardProxy.SimStateListener.<init>():void");
        }

        public abstract void onSimStateChange(int i, Intent intent);
    }

    /* renamed from: com.android.internal.telephony.uicc.IccCardProxy$1 */
    class AnonymousClass1 extends BroadcastReceiver {
        final /* synthetic */ IccCardProxy this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.uicc.IccCardProxy.1.<init>(com.android.internal.telephony.uicc.IccCardProxy):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass1(com.android.internal.telephony.uicc.IccCardProxy r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.uicc.IccCardProxy.1.<init>(com.android.internal.telephony.uicc.IccCardProxy):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.IccCardProxy.1.<init>(com.android.internal.telephony.uicc.IccCardProxy):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.uicc.IccCardProxy.1.onReceive(android.content.Context, android.content.Intent):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onReceive(android.content.Context r1, android.content.Intent r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.uicc.IccCardProxy.1.onReceive(android.content.Context, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.IccCardProxy.1.onReceive(android.content.Context, android.content.Intent):void");
        }
    }

    /* renamed from: -getcom-android-internal-telephony-IccCardConstants$StateSwitchesValues */
    private static /* synthetic */ int[] m49xf663cf59() {
        if (f22x8dbfd0b5 != null) {
            return f22x8dbfd0b5;
        }
        int[] iArr = new int[State.values().length];
        try {
            iArr[State.ABSENT.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[State.CARD_IO_ERROR.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[State.CARD_RESTRICTED.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[State.NETWORK_LOCKED.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[State.NOT_READY.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[State.PERM_DISABLED.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[State.PIN_REQUIRED.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[State.PUK_REQUIRED.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[State.READY.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[State.UNKNOWN.ordinal()] = 21;
        } catch (NoSuchFieldError e10) {
        }
        f22x8dbfd0b5 = iArr;
        return iArr;
    }

    /* renamed from: -getcom-android-internal-telephony-uicc-IccCardApplicationStatus$AppStateSwitchesValues */
    private static /* synthetic */ int[] m50x37a84908() {
        if (f23x3dee1264 != null) {
            return f23x3dee1264;
        }
        int[] iArr = new int[AppState.values().length];
        try {
            iArr[AppState.APPSTATE_DETECTED.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[AppState.APPSTATE_PIN.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[AppState.APPSTATE_PUK.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[AppState.APPSTATE_READY.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[AppState.APPSTATE_SUBSCRIPTION_PERSO.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[AppState.APPSTATE_UNKNOWN.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        f23x3dee1264 = iArr;
        return iArr;
    }

    /* renamed from: -getcom-android-internal-telephony-uicc-IccCardApplicationStatus$PersoSubStateSwitchesValues */
    private static /* synthetic */ int[] m51x5ed1affa() {
        if (f24x16bf601e != null) {
            return f24x16bf601e;
        }
        int[] iArr = new int[PersoSubState.values().length];
        try {
            iArr[PersoSubState.PERSOSUBSTATE_IN_PROGRESS.ordinal()] = 21;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_READY.ordinal()] = 22;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_CORPORATE.ordinal()] = 23;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_CORPORATE_PUK.ordinal()] = 24;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_HRPD.ordinal()] = 25;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_HRPD_PUK.ordinal()] = 26;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_NETWORK1.ordinal()] = 27;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_NETWORK1_PUK.ordinal()] = 28;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_NETWORK2.ordinal()] = 29;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_NETWORK2_PUK.ordinal()] = 30;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_RUIM.ordinal()] = 31;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_RUIM_PUK.ordinal()] = 32;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_SERVICE_PROVIDER.ordinal()] = 33;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_SERVICE_PROVIDER_PUK.ordinal()] = 34;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_CORPORATE.ordinal()] = 1;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_CORPORATE_PUK.ordinal()] = 35;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_NETWORK.ordinal()] = 2;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_NETWORK_PUK.ordinal()] = 36;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_NETWORK_SUBSET.ordinal()] = 3;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_NETWORK_SUBSET_PUK.ordinal()] = 37;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_SERVICE_PROVIDER.ordinal()] = 4;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_SERVICE_PROVIDER_PUK.ordinal()] = 38;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_SIM.ordinal()] = 5;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_SIM_PUK.ordinal()] = 39;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_UNKNOWN.ordinal()] = 40;
        } catch (NoSuchFieldError e25) {
        }
        f24x16bf601e = iArr;
        return iArr;
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.uicc.IccCardProxy.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.uicc.IccCardProxy.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.IccCardProxy.<clinit>():void");
    }

    public IccCardProxy(Context context, CommandsInterface ci, int phoneId) {
        this.mPhoneId = null;
        this.mRecoveryRegistrants = new RegistrantList();
        this.mFdnChangedRegistrants = new RegistrantList();
        this.mNetworkLockState = PersoSubState.PERSOSUBSTATE_UNKNOWN;
        String[] strArr = new String[4];
        strArr[0] = "ril.iccid.sim1";
        strArr[1] = "ril.iccid.sim2";
        strArr[2] = "ril.iccid.sim3";
        strArr[3] = "ril.iccid.sim4";
        this.PROPERTY_ICCID_SIM = strArr;
        this.mLock = new Object();
        this.mAbsentRegistrants = new RegistrantList();
        this.mPinLockedRegistrants = new RegistrantList();
        this.mNetworkLockedRegistrants = new RegistrantList();
        this.mCurrentAppType = 1;
        this.mUiccController = null;
        this.mUiccCard = null;
        this.mUiccApplication = null;
        this.mIccRecords = null;
        this.mCdmaSSM = null;
        this.mRadioOn = false;
        this.mQuietMode = false;
        this.mInitialized = false;
        this.mExternalState = State.UNKNOWN;
        this.sReceiver = new AnonymousClass1(this);
        log("ctor: ci=" + ci + " phoneId=" + phoneId);
        this.mContext = context;
        this.mCi = ci;
        this.mPhoneId = Integer.valueOf(phoneId);
        this.mTelephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        this.mCdmaSSM = CdmaSubscriptionSourceManager.getInstance(context, ci, this, 11, null);
        this.mUiccController = UiccController.getInstance();
        this.mUiccController.registerForIccChanged(this, 3, null);
        this.mUiccController.registerForIccRecovery(this, 100, null);
        ci.registerForOn(this, 2, null);
        ci.registerForOffOrNotAvailable(this, 1, null);
        setExternalState(State.NOT_READY);
        this.mContext.registerReceiver(this.sReceiver, new IntentFilter(IWorldPhone.ACTION_SHUTDOWN_IPO));
        resetProperties();
        setExternalState(State.NOT_READY, false);
    }

    public void dispose() {
        synchronized (this.mLock) {
            log("Disposing");
            this.mUiccController.unregisterForIccChanged(this);
            this.mUiccController.unregisterForIccRecovery(this);
            this.mUiccController = null;
            this.mCi.unregisterForOn(this);
            this.mCi.unregisterForOffOrNotAvailable(this);
            if (this.mCdmaSSM != null) {
                this.mCdmaSSM.dispose(this);
                this.mCdmaSSM = null;
            }
        }
    }

    public void setVoiceRadioTech(int radioTech) {
        synchronized (this.mLock) {
            log("Setting radio tech " + ServiceState.rilRadioTechnologyToString(radioTech));
            if (ServiceState.isGsm(radioTech)) {
                this.mCurrentAppType = 1;
            } else {
                this.mCurrentAppType = 2;
            }
            updateQuietMode();
        }
    }

    private void updateQuietMode() {
        synchronized (this.mLock) {
            boolean newQuietMode;
            int cdmaSource = -1;
            if (this.mCurrentAppType == 1) {
                newQuietMode = false;
                log("updateQuietMode: 3GPP subscription -> newQuietMode=" + false);
            } else {
                if (this.mPhoneId.intValue() == 0 && SystemProperties.get("persist.sys.forcttddtest").equals("1")) {
                    log("updateQuietMode: force IccCardProxy into 3gpp for cttdd test");
                    this.mCurrentAppType = 1;
                }
                cdmaSource = this.mCdmaSSM != null ? this.mCdmaSSM.getCdmaSubscriptionSource() : -1;
                newQuietMode = cdmaSource == 1 ? this.mCurrentAppType == 2 : false;
            }
            if (!this.mQuietMode && newQuietMode) {
                log("Switching to QuietMode.");
                setExternalState(State.READY);
                this.mQuietMode = newQuietMode;
            } else if (!this.mQuietMode || newQuietMode) {
                log("updateQuietMode: no changes don't setExternalState");
            } else {
                log("updateQuietMode: Switching out from QuietMode. Force broadcast of current state=" + this.mExternalState);
                this.mQuietMode = newQuietMode;
                setExternalState(this.mExternalState, true);
            }
            log("updateQuietMode: QuietMode is " + this.mQuietMode + " (app_type=" + this.mCurrentAppType + " cdmaSource=" + cdmaSource + ")");
            this.mInitialized = true;
            sendMessage(obtainMessage(3));
        }
    }

    public void handleMessage(Message msg) {
        log("receive message " + msg.what);
        switch (msg.what) {
            case 1:
                this.mRadioOn = false;
                if (RadioState.RADIO_UNAVAILABLE == this.mCi.getRadioState()) {
                    setExternalState(State.NOT_READY);
                    break;
                }
                break;
            case 2:
                this.mRadioOn = true;
                if (!this.mInitialized) {
                    updateQuietMode();
                    break;
                }
                break;
            case 3:
                if (this.mInitialized) {
                    AsyncResult ar = msg.obj;
                    int index = this.mPhoneId.intValue();
                    if (ar != null && (ar.result instanceof Integer)) {
                        index = ((Integer) ar.result).intValue();
                        log("handleMessage (EVENT_ICC_CHANGED) , index = " + index);
                    }
                    if (index == this.mPhoneId.intValue()) {
                        updateIccAvailability();
                        break;
                    }
                }
                break;
            case 4:
                this.mAbsentRegistrants.notifyRegistrants();
                setExternalState(State.ABSENT);
                break;
            case 5:
                processLockedState();
                break;
            case 6:
                setExternalState(State.READY);
                break;
            case 7:
                if (this.mIccRecords != null) {
                    String operator = this.mIccRecords.getOperatorNumeric();
                    log("operator=" + operator + " mPhoneId=" + this.mPhoneId);
                    if (operator != null) {
                        String countryCode = operator.substring(0, 3);
                        if (countryCode != null) {
                            try {
                                this.mTelephonyManager.setSimCountryIsoForPhone(this.mPhoneId.intValue(), MccTable.countryCodeForMcc(Integer.parseInt(countryCode)));
                            } catch (NumberFormatException e) {
                                loge("Not number format: " + countryCode);
                            }
                        } else {
                            loge("EVENT_RECORDS_LOADED Country code is null");
                        }
                    } else {
                        loge("EVENT_RECORDS_LOADED Operator name is null");
                    }
                }
                if (this.mUiccCard != null && !this.mUiccCard.areCarrierPriviligeRulesLoaded()) {
                    this.mUiccCard.registerForCarrierPrivilegeRulesLoaded(this, EVENT_CARRIER_PRIVILIGES_LOADED, null);
                    break;
                } else {
                    onRecordsLoaded();
                    break;
                }
                break;
            case 8:
                broadcastIccStateChangedIntent("IMSI", null);
                break;
            case 9:
                if (this.mUiccApplication != null) {
                    this.mNetworkLockedRegistrants.notifyRegistrants();
                    setExternalState(State.NETWORK_LOCKED);
                    break;
                }
                loge("getIccStateReason: NETWORK_LOCKED but mUiccApplication is null!");
                return;
            case 11:
                updateQuietMode();
                break;
            case 100:
                Integer index2 = msg.obj.result;
                log("handleMessage (EVENT_ICC_RECOVERY) , index = " + index2);
                if (index2 == this.mPhoneId) {
                    log("mRecoveryRegistrants notify");
                    this.mRecoveryRegistrants.notifyRegistrants();
                    break;
                }
                break;
            case 101:
                this.mFdnChangedRegistrants.notifyRegistrants();
                break;
            case EVENT_ICC_RECORD_EVENTS /*500*/:
                if (this.mCurrentAppType == 1 && this.mIccRecords != null && ((Integer) msg.obj.result).intValue() == 2) {
                    this.mTelephonyManager.setSimOperatorNameForPhone(this.mPhoneId.intValue(), this.mIccRecords.getServiceProviderName());
                    break;
                }
            case EVENT_SUBSCRIPTION_ACTIVATED /*501*/:
                log("EVENT_SUBSCRIPTION_ACTIVATED");
                onSubscriptionActivated();
                break;
            case EVENT_SUBSCRIPTION_DEACTIVATED /*502*/:
                log("EVENT_SUBSCRIPTION_DEACTIVATED");
                onSubscriptionDeactivated();
                break;
            case EVENT_CARRIER_PRIVILIGES_LOADED /*503*/:
                log("EVENT_CARRIER_PRIVILEGES_LOADED");
                if (this.mUiccCard != null) {
                    this.mUiccCard.unregisterForCarrierPrivilegeRulesLoaded(this);
                }
                onRecordsLoaded();
                break;
            default:
                loge("Unhandled message with number: " + msg.what);
                break;
        }
    }

    private void onSubscriptionActivated() {
        updateIccAvailability();
        updateStateProperty();
    }

    private void onSubscriptionDeactivated() {
        resetProperties();
        updateIccAvailability();
        updateStateProperty();
    }

    private void onRecordsLoaded() {
        broadcastInternalIccStateChangedIntent("LOADED", null);
    }

    /* JADX WARNING: Missing block: B:18:0x0046, code:
            if (r7.mUiccCard != r1) goto L_0x002d;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateIccAvailability() {
        synchronized (this.mLock) {
            UiccCard newCard = this.mUiccController.getUiccCard(this.mPhoneId.intValue());
            CardState state = CardState.CARDSTATE_ABSENT;
            UiccCardApplication newApp = null;
            IccRecords newRecords = null;
            if (newCard != null) {
                state = newCard.getCardState();
                newApp = newCard.getApplication(this.mCurrentAppType);
                if (newApp != null) {
                    newRecords = newApp.getIccRecords();
                }
            }
            if (this.mIccRecords == newRecords && this.mUiccApplication == newApp) {
            }
            log("Icc changed. Reregestering.");
            unregisterUiccCardEvents();
            this.mUiccCard = newCard;
            this.mUiccApplication = newApp;
            this.mIccRecords = newRecords;
            registerUiccCardEvents();
            updateExternalState();
        }
    }

    void resetProperties() {
        if (this.mCurrentAppType == 1) {
            log("update icc_operator_numeric=");
            this.mTelephonyManager.setSimOperatorNumericForPhone(this.mPhoneId.intValue(), "");
            this.mTelephonyManager.setSimCountryIsoForPhone(this.mPhoneId.intValue(), "");
            this.mTelephonyManager.setSimOperatorNameForPhone(this.mPhoneId.intValue(), "");
        }
    }

    private void HandleDetectedState() {
    }

    private void updateExternalState() {
        if (this.mUiccCard == null) {
            log("updateExternalState, broadcast NOT_READY because UiccCard is null!");
            setExternalState(State.NOT_READY);
        } else if (this.mUiccCard.getCardState() == CardState.CARDSTATE_ABSENT) {
            log("updateExternalState, broadcast ABSENT because card state is absent!");
            setExternalState(State.ABSENT);
        } else if (this.mUiccCard.getCardState() == CardState.CARDSTATE_ERROR) {
            setExternalState(State.CARD_IO_ERROR);
        } else if (this.mUiccCard.getCardState() == CardState.CARDSTATE_RESTRICTED) {
            setExternalState(State.CARD_RESTRICTED);
        } else if (this.mUiccApplication == null) {
            log("updateExternalState, broadcast NOT_READY because mUiccApplication is null!");
            setExternalState(State.NOT_READY);
        } else {
            switch (m50x37a84908()[this.mUiccApplication.getState().ordinal()]) {
                case 1:
                    HandleDetectedState();
                    break;
                case 2:
                    setExternalState(State.PIN_REQUIRED);
                    break;
                case 3:
                    setExternalState(State.PUK_REQUIRED);
                    break;
                case 4:
                    setExternalState(State.READY);
                    break;
                case 5:
                    setExternalState(State.NETWORK_LOCKED);
                    break;
                case 6:
                    setExternalState(State.UNKNOWN);
                    break;
                default:
                    setExternalState(State.UNKNOWN);
                    break;
            }
        }
    }

    private void registerUiccCardEvents() {
        if (this.mUiccCard != null) {
            this.mUiccCard.registerForAbsent(this, 4, null);
        }
        if (this.mUiccApplication != null) {
            this.mUiccApplication.registerForReady(this, 6, null);
            this.mUiccApplication.registerForLocked(this, 5, null);
            this.mUiccApplication.registerForNetworkLocked(this, 9, null);
            this.mUiccApplication.registerForFdnChanged(this, 101, null);
        }
        if (this.mIccRecords != null) {
            this.mIccRecords.registerForImsiReady(this, 8, null);
            this.mIccRecords.registerForRecordsLoaded(this, 7, null);
            this.mIccRecords.registerForRecordsEvents(this, EVENT_ICC_RECORD_EVENTS, null);
        }
    }

    private void unregisterUiccCardEvents() {
        if (this.mUiccCard != null) {
            this.mUiccCard.unregisterForAbsent(this);
        }
        if (this.mUiccApplication != null) {
            this.mUiccApplication.unregisterForReady(this);
        }
        if (this.mUiccApplication != null) {
            this.mUiccApplication.unregisterForLocked(this);
        }
        if (this.mUiccApplication != null) {
            this.mUiccApplication.unregisterForNetworkLocked(this);
        }
        if (this.mIccRecords != null) {
            this.mIccRecords.unregisterForImsiReady(this);
        }
        if (this.mIccRecords != null) {
            this.mIccRecords.unregisterForRecordsLoaded(this);
        }
        if (this.mIccRecords != null) {
            this.mIccRecords.unregisterForRecordsEvents(this);
        }
    }

    private void updateStateProperty() {
        this.mTelephonyManager.setSimStateForPhone(this.mPhoneId.intValue(), getState().toString());
    }

    private void broadcastIccStateChangedIntent(String value, String reason) {
        synchronized (this.mLock) {
            if (this.mPhoneId == null || !SubscriptionManager.isValidSlotId(this.mPhoneId.intValue())) {
                loge("broadcastIccStateChangedIntent: mPhoneId=" + this.mPhoneId + " is invalid; Return!!");
            } else if (this.mQuietMode) {
                log("broadcastIccStateChangedIntent: QuietMode NOT Broadcasting intent ACTION_SIM_STATE_CHANGED  value=" + value + " reason=" + reason);
            } else {
                Intent intent = new Intent("android.intent.action.SIM_STATE_CHANGED");
                intent.addFlags(67108864);
                intent.putExtra("phoneName", "Phone");
                intent.putExtra("ss", value);
                intent.putExtra(DataSubSelector.EXTRA_MOBILE_DATA_ENABLE_REASON, reason);
                SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhoneId.intValue());
                log("broadcastIccStateChangedIntent intent ACTION_SIM_STATE_CHANGED value=" + value + " reason=" + reason + " for mPhoneId=" + this.mPhoneId);
                for (SimStateListener listener : sSimStateListeners) {
                    listener.onSimStateChange(this.mPhoneId.intValue(), intent);
                }
                ActivityManagerNative.broadcastStickyIntent(intent, "android.permission.READ_PHONE_STATE", -1);
            }
        }
    }

    private void broadcastInternalIccStateChangedIntent(String value, String reason) {
        Intent intent = new Intent(ACTION_INTERNAL_SIM_STATE_CHANGED);
        synchronized (this.mLock) {
            if (this.mPhoneId == null) {
                loge("broadcastInternalIccStateChangedIntent: Card Index is not set; Return!!");
                return;
            }
            intent.addFlags(67108864);
            intent.putExtra("phoneName", "Phone");
            intent.putExtra("ss", value);
            intent.putExtra(DataSubSelector.EXTRA_MOBILE_DATA_ENABLE_REASON, reason);
            intent.putExtra("phone", this.mPhoneId);
            log("Sending intent ACTION_INTERNAL_SIM_STATE_CHANGED for mPhoneId : " + this.mPhoneId);
            sInternalIntent = intent;
            ActivityManagerNative.broadcastStickyIntent(intent, null, -1);
        }
    }

    /* JADX WARNING: Missing block: B:28:0x00d5, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setExternalState(State newState, boolean override) {
        synchronized (this.mLock) {
            if (this.mPhoneId == null || !SubscriptionManager.isValidSlotId(this.mPhoneId.intValue())) {
                loge("setExternalState: mPhoneId=" + this.mPhoneId + " is invalid; Return!!");
                return;
            }
            log("setExternalState(): mExternalState = " + this.mExternalState + " newState =  " + newState + " override = " + override);
            if (!override && newState == this.mExternalState) {
                if (newState != State.NETWORK_LOCKED || this.mNetworkLockState == getNetworkPersoType()) {
                    loge("setExternalState: !override and newstate unchanged from " + newState);
                    return;
                }
                log("NetworkLockState =  " + this.mNetworkLockState);
            }
            this.mExternalState = newState;
            this.mNetworkLockState = getNetworkPersoType();
            loge("setExternalState: set mPhoneId=" + this.mPhoneId + " mExternalState=" + this.mExternalState);
            if (this.mExternalState == State.ABSENT && newState == State.NOT_READY) {
                loge("setExternalState: Do not change the state");
            } else {
                this.mTelephonyManager.setSimStateForPhone(this.mPhoneId.intValue(), getState().toString());
            }
            if ("LOCKED".equals(getIccStateIntentString(this.mExternalState))) {
                broadcastInternalIccStateChangedIntent(getIccStateIntentString(this.mExternalState), getIccStateReason(this.mExternalState));
            } else {
                broadcastIccStateChangedIntent(getIccStateIntentString(this.mExternalState), getIccStateReason(this.mExternalState));
            }
            if (State.ABSENT == this.mExternalState) {
                this.mAbsentRegistrants.notifyRegistrants();
            }
        }
    }

    /* JADX WARNING: Missing block: B:17:0x002e, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void processLockedState() {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                if (this.mUiccApplication.getPin1State() != PinState.PINSTATE_ENABLED_PERM_BLOCKED) {
                    switch (m50x37a84908()[this.mUiccApplication.getState().ordinal()]) {
                        case 2:
                            this.mPinLockedRegistrants.notifyRegistrants();
                            setExternalState(State.PIN_REQUIRED);
                            break;
                        case 3:
                            setExternalState(State.PUK_REQUIRED);
                            break;
                    }
                }
                setExternalState(State.PERM_DISABLED);
                return;
            }
        }
    }

    private void setExternalState(State newState) {
        if (newState == State.PIN_REQUIRED && this.mUiccApplication != null && this.mUiccApplication.getPin1State() == PinState.PINSTATE_ENABLED_PERM_BLOCKED) {
            log("setExternalState(): PERM_DISABLED");
            setExternalState(State.PERM_DISABLED);
            return;
        }
        setExternalState(newState, false);
    }

    public boolean getIccRecordsLoaded() {
        synchronized (this.mLock) {
            if (this.mIccRecords != null) {
                boolean recordsLoaded = this.mIccRecords.getRecordsLoaded();
                return recordsLoaded;
            }
            return false;
        }
    }

    private String getIccStateIntentString(State state) {
        switch (m49xf663cf59()[state.ordinal()]) {
            case 1:
                return "ABSENT";
            case 2:
                return "CARD_IO_ERROR";
            case 3:
                return "CARD_RESTRICTED";
            case 4:
                return "LOCKED";
            case 5:
                return "NOT_READY";
            case 6:
                return "LOCKED";
            case 7:
                return "LOCKED";
            case 8:
                return "LOCKED";
            case 9:
                return "READY";
            default:
                return "UNKNOWN";
        }
    }

    private String getIccStateReason(State state) {
        switch (m49xf663cf59()[state.ordinal()]) {
            case 2:
                return "CARD_IO_ERROR";
            case 3:
                return "CARD_RESTRICTED";
            case 4:
                switch (m51x5ed1affa()[this.mUiccApplication.getPersoSubState().ordinal()]) {
                    case 1:
                        return "CORPORATE";
                    case 2:
                        return "NETWORK";
                    case 3:
                        return "NETWORK_SUBSET";
                    case 4:
                        return "SERVICE_PROVIDER";
                    case 5:
                        return "SIM";
                    default:
                        return null;
                }
            case 6:
                return "PERM_DISABLED";
            case 7:
                return "PIN";
            case 8:
                return "PUK";
            default:
                return null;
        }
    }

    public State getState() {
        State state;
        synchronized (this.mLock) {
            state = this.mExternalState;
        }
        return state;
    }

    public IccRecords getIccRecords() {
        IccRecords iccRecords;
        synchronized (this.mLock) {
            iccRecords = this.mIccRecords;
        }
        return iccRecords;
    }

    public IccFileHandler getIccFileHandler() {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                IccFileHandler iccFileHandler = this.mUiccApplication.getIccFileHandler();
                return iccFileHandler;
            }
            return null;
        }
    }

    public void registerForAbsent(Handler h, int what, Object obj) {
        synchronized (this.mLock) {
            Registrant r = new Registrant(h, what, obj);
            this.mAbsentRegistrants.add(r);
            if (getState() == State.ABSENT) {
                r.notifyRegistrant();
            }
        }
    }

    public void unregisterForAbsent(Handler h) {
        synchronized (this.mLock) {
            this.mAbsentRegistrants.remove(h);
        }
    }

    public void registerForNetworkLocked(Handler h, int what, Object obj) {
        synchronized (this.mLock) {
            Registrant r = new Registrant(h, what, obj);
            this.mNetworkLockedRegistrants.add(r);
            if (getState() == State.NETWORK_LOCKED) {
                r.notifyRegistrant();
            }
        }
    }

    public void unregisterForNetworkLocked(Handler h) {
        synchronized (this.mLock) {
            this.mNetworkLockedRegistrants.remove(h);
        }
    }

    public void registerForLocked(Handler h, int what, Object obj) {
        synchronized (this.mLock) {
            Registrant r = new Registrant(h, what, obj);
            this.mPinLockedRegistrants.add(r);
            if (getState().isPinLocked()) {
                r.notifyRegistrant();
            }
        }
    }

    public void unregisterForLocked(Handler h) {
        synchronized (this.mLock) {
            this.mPinLockedRegistrants.remove(h);
        }
    }

    /* JADX WARNING: Missing block: B:10:0x001a, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void supplyPin(String pin, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication == null) {
                log("zhanghziran mUiccApplication == null");
                updateIccAvailability();
            }
            if (this.mUiccApplication != null) {
                this.mUiccApplication.supplyPin(pin, onComplete);
            } else if (onComplete != null) {
                Exception e = CommandException.fromRilErrno(1);
                log("Fail to supplyPin, hasIccCard = " + hasIccCard());
                AsyncResult.forMessage(onComplete).exception = e;
                onComplete.sendToTarget();
            }
        }
    }

    /* JADX WARNING: Missing block: B:7:0x000d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void supplyPuk(String puk, String newPin, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.supplyPuk(puk, newPin, onComplete);
            } else if (onComplete != null) {
                Exception e = CommandException.fromRilErrno(1);
                log("Fail to supplyPuk, hasIccCard = " + hasIccCard());
                AsyncResult.forMessage(onComplete).exception = e;
                onComplete.sendToTarget();
            }
        }
    }

    /* JADX WARNING: Missing block: B:7:0x000d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void supplyPin2(String pin2, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.supplyPin2(pin2, onComplete);
            } else if (onComplete != null) {
                Exception e = CommandException.fromRilErrno(1);
                log("Fail to supplyPin2, hasIccCard = " + hasIccCard());
                AsyncResult.forMessage(onComplete).exception = e;
                onComplete.sendToTarget();
            }
        }
    }

    /* JADX WARNING: Missing block: B:7:0x000d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void supplyPuk2(String puk2, String newPin2, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.supplyPuk2(puk2, newPin2, onComplete);
            } else if (onComplete != null) {
                Exception e = CommandException.fromRilErrno(1);
                log("Fail to supplyPuk2, hasIccCard = " + hasIccCard());
                AsyncResult.forMessage(onComplete).exception = e;
                onComplete.sendToTarget();
            }
        }
    }

    /* JADX WARNING: Missing block: B:7:0x000d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void supplyNetworkDepersonalization(String pin, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.supplyNetworkDepersonalization(pin, onComplete);
            } else if (onComplete != null) {
                AsyncResult.forMessage(onComplete).exception = new RuntimeException("CommandsInterface is not set.");
                onComplete.sendToTarget();
            }
        }
    }

    public boolean getIccLockEnabled() {
        boolean booleanValue;
        synchronized (this.mLock) {
            booleanValue = Boolean.valueOf(this.mUiccApplication != null ? this.mUiccApplication.getIccLockEnabled() : false).booleanValue();
        }
        return booleanValue;
    }

    public boolean getIccFdnEnabled() {
        boolean booleanValue;
        synchronized (this.mLock) {
            booleanValue = Boolean.valueOf(this.mUiccApplication != null ? this.mUiccApplication.getIccFdnEnabled() : false).booleanValue();
        }
        return booleanValue;
    }

    public boolean getIccFdnAvailable() {
        return this.mUiccApplication != null ? this.mUiccApplication.getIccFdnAvailable() : false;
    }

    public boolean getIccPin2Blocked() {
        return Boolean.valueOf(this.mUiccApplication != null ? this.mUiccApplication.getIccPin2Blocked() : false).booleanValue();
    }

    public boolean getIccPuk2Blocked() {
        return Boolean.valueOf(this.mUiccApplication != null ? this.mUiccApplication.getIccPuk2Blocked() : false).booleanValue();
    }

    /* JADX WARNING: Missing block: B:7:0x000d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setIccLockEnabled(boolean enabled, String password, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.setIccLockEnabled(enabled, password, onComplete);
            } else if (onComplete != null) {
                Exception e = CommandException.fromRilErrno(1);
                log("Fail to setIccLockEnabled, hasIccCard = " + hasIccCard());
                AsyncResult.forMessage(onComplete).exception = e;
                onComplete.sendToTarget();
            }
        }
    }

    /* JADX WARNING: Missing block: B:7:0x000d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setIccFdnEnabled(boolean enabled, String password, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.setIccFdnEnabled(enabled, password, onComplete);
            } else if (onComplete != null) {
                Exception e = CommandException.fromRilErrno(1);
                log("Fail to setIccFdnEnabled, hasIccCard = " + hasIccCard());
                AsyncResult.forMessage(onComplete).exception = e;
                onComplete.sendToTarget();
            }
        }
    }

    /* JADX WARNING: Missing block: B:7:0x000d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void changeIccLockPassword(String oldPassword, String newPassword, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.changeIccLockPassword(oldPassword, newPassword, onComplete);
            } else if (onComplete != null) {
                Exception e = CommandException.fromRilErrno(1);
                log("Fail to changeIccLockPassword, hasIccCard = " + hasIccCard());
                AsyncResult.forMessage(onComplete).exception = e;
                onComplete.sendToTarget();
            }
        }
    }

    /* JADX WARNING: Missing block: B:7:0x000d, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void changeIccFdnPassword(String oldPassword, String newPassword, Message onComplete) {
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.changeIccFdnPassword(oldPassword, newPassword, onComplete);
            } else if (onComplete != null) {
                Exception e = CommandException.fromRilErrno(1);
                log("Fail to changeIccFdnPassword, hasIccCard = " + hasIccCard());
                AsyncResult.forMessage(onComplete).exception = e;
                onComplete.sendToTarget();
            }
        }
    }

    public String getServiceProviderName() {
        synchronized (this.mLock) {
            if (this.mIccRecords != null) {
                String serviceProviderName = this.mIccRecords.getServiceProviderName();
                return serviceProviderName;
            }
            return null;
        }
    }

    public boolean isApplicationOnIcc(AppType type) {
        boolean booleanValue;
        synchronized (this.mLock) {
            booleanValue = Boolean.valueOf(this.mUiccCard != null ? this.mUiccCard.isApplicationOnIcc(type) : false).booleanValue();
        }
        return booleanValue;
    }

    public boolean hasIccCard() {
        boolean isSimInsert;
        synchronized (this.mLock) {
            isSimInsert = false;
            String iccId = SystemProperties.get(this.PROPERTY_ICCID_SIM[this.mPhoneId.intValue()]);
            if (!(iccId == null || iccId.equals(""))) {
                if (!iccId.equals(ICCID_STRING_FOR_NO_SIM)) {
                    isSimInsert = true;
                }
            }
            if (!(isSimInsert || this.mUiccCard == null || this.mUiccCard.getCardState() == CardState.CARDSTATE_ABSENT)) {
                isSimInsert = true;
            }
            log("hasIccCard(): isSimInsert =  " + isSimInsert + " ,CardState = " + (this.mUiccCard != null ? this.mUiccCard.getCardState() : "") + ", iccId = " + SubscriptionInfo.givePrintableIccid(iccId));
        }
        return isSimInsert;
    }

    private void setSystemProperty(String property, String value) {
        TelephonyManager.setTelephonyProperty(this.mPhoneId.intValue(), property, value);
    }

    public IccRecords getIccRecord() {
        return this.mIccRecords;
    }

    private void log(String s) {
        Rlog.d(LOG_TAG, s + " (slot " + this.mPhoneId + ")");
    }

    private void loge(String msg) {
        Rlog.e(LOG_TAG, msg + " (slot " + this.mPhoneId + ")");
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        int i;
        pw.println("IccCardProxy: " + this);
        pw.println(" mContext=" + this.mContext);
        pw.println(" mCi=" + this.mCi);
        pw.println(" mAbsentRegistrants: size=" + this.mAbsentRegistrants.size());
        for (i = 0; i < this.mAbsentRegistrants.size(); i++) {
            pw.println("  mAbsentRegistrants[" + i + "]=" + ((Registrant) this.mAbsentRegistrants.get(i)).getHandler());
        }
        pw.println(" mPinLockedRegistrants: size=" + this.mPinLockedRegistrants.size());
        for (i = 0; i < this.mPinLockedRegistrants.size(); i++) {
            pw.println("  mPinLockedRegistrants[" + i + "]=" + ((Registrant) this.mPinLockedRegistrants.get(i)).getHandler());
        }
        pw.println(" mNetworkLockedRegistrants: size=" + this.mNetworkLockedRegistrants.size());
        for (i = 0; i < this.mNetworkLockedRegistrants.size(); i++) {
            pw.println("  mNetworkLockedRegistrants[" + i + "]=" + ((Registrant) this.mNetworkLockedRegistrants.get(i)).getHandler());
        }
        pw.println(" mCurrentAppType=" + this.mCurrentAppType);
        pw.println(" mUiccController=" + this.mUiccController);
        pw.println(" mUiccCard=" + this.mUiccCard);
        pw.println(" mUiccApplication=" + this.mUiccApplication);
        pw.println(" mIccRecords=" + this.mIccRecords);
        pw.println(" mCdmaSSM=" + this.mCdmaSSM);
        pw.println(" mRadioOn=" + this.mRadioOn);
        pw.println(" mQuietMode=" + this.mQuietMode);
        pw.println(" mInitialized=" + this.mInitialized);
        pw.println(" mExternalState=" + this.mExternalState);
        pw.flush();
    }

    public PersoSubState getNetworkPersoType() {
        synchronized (this.mLock) {
            PersoSubState persoSubState;
            if (this.mUiccApplication != null) {
                persoSubState = this.mUiccApplication.getPersoSubState();
                return persoSubState;
            }
            persoSubState = PersoSubState.PERSOSUBSTATE_UNKNOWN;
            return persoSubState;
        }
    }

    /* JADX WARNING: Missing block: B:7:0x0024, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
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

    /* JADX WARNING: Missing block: B:7:0x0069, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setIccNetworkLockEnabled(int category, int lockop, String password, String data_imsi, String gid1, String gid2, Message onComplete) {
        log("SetIccNetworkEnabled(): category = " + category + " lockop = " + lockop + " password = " + password + " data_imsi = " + data_imsi + " gid1 = " + gid1 + " gid2 = " + gid2);
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.setIccNetworkLockEnabled(category, lockop, password, data_imsi, gid1, gid2, onComplete);
            } else if (onComplete != null) {
                Exception e = CommandException.fromRilErrno(1);
                log("Fail to setIccNetworkLockEnabled, hasIccCard = " + hasIccCard());
                AsyncResult.forMessage(onComplete).exception = e;
                onComplete.sendToTarget();
            }
        }
    }

    public void repollIccStateForModemSmlChangeFeatrue(boolean needIntent) {
        log("repollIccStateForModemSmlChangeFeatrue, needIntent = " + needIntent);
        synchronized (this.mLock) {
            this.mUiccController.repollIccStateForModemSmlChangeFeatrue(this.mPhoneId.intValue(), needIntent);
        }
    }

    public String getIccCardType() {
        synchronized (this.mLock) {
            String str;
            if (this.mUiccCard == null || this.mUiccCard.getCardState() == CardState.CARDSTATE_ABSENT) {
                str = "";
                return str;
            }
            str = this.mUiccCard.getIccCardType();
            return str;
        }
    }

    public void registerForRecovery(Handler h, int what, Object obj) {
        synchronized (this.mLock) {
            Registrant r = new Registrant(h, what, obj);
            this.mRecoveryRegistrants.add(r);
            if (getState() == State.READY) {
                r.notifyRegistrant();
            }
        }
    }

    public void unregisterForRecovery(Handler h) {
        synchronized (this.mLock) {
            this.mRecoveryRegistrants.remove(h);
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

    public boolean isCdmaOnly() {
        boolean z = false;
        Object[] values = null;
        if (this.mPhoneId.intValue() < 0 || this.mPhoneId.intValue() >= PROPERTY_RIL_FULL_UICC_TYPE.length) {
            log("isCdmaOnly: invalid PhoneId " + this.mPhoneId);
            return false;
        }
        int length;
        String prop = SystemProperties.get(PROPERTY_RIL_FULL_UICC_TYPE[this.mPhoneId.intValue()]);
        if (prop != null && prop.length() > 0) {
            values = prop.split(",");
        }
        StringBuilder append = new StringBuilder().append("isCdmaOnly PhoneId ").append(this.mPhoneId).append(", prop value= ").append(prop).append(", size= ");
        if (values != null) {
            length = values.length;
        } else {
            length = 0;
        }
        log(append.append(length).toString());
        if (values == null) {
            return false;
        }
        if (!(Arrays.asList(values).contains("USIM") || Arrays.asList(values).contains("SIM"))) {
            z = true;
        }
        return z;
    }

    /* JADX WARNING: Missing block: B:7:0x002f, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setCarrierRestrictionState(int state, String password, Message onComplete) {
        log("setCarrierRestrictionState(): state: = " + state + "password = " + password);
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.setCarrierRestrictionState(state, password, onComplete);
            } else if (onComplete != null) {
                Exception e = CommandException.fromRilErrno(1);
                log("Fail to setCarrierRestrictionState, hasIccCard = " + hasIccCard());
                AsyncResult.forMessage(onComplete).exception = e;
                onComplete.sendToTarget();
            }
        }
    }

    /* JADX WARNING: Missing block: B:7:0x0013, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void getCarrierRestrictionState(Message onComplete) {
        log("getCarrierRestrictionState()");
        synchronized (this.mLock) {
            if (this.mUiccApplication != null) {
                this.mUiccApplication.getCarrierRestrictionState(onComplete);
            } else if (onComplete != null) {
                Exception e = CommandException.fromRilErrno(1);
                log("Fail to getCarrierRestrictionState, hasIccCard = " + hasIccCard());
                AsyncResult.forMessage(onComplete).exception = e;
                onComplete.sendToTarget();
            }
        }
    }

    public static void registerSimStateListener(SimStateListener ssl) {
        sSimStateListeners.add(ssl);
    }

    public static void unRegisterSimStateListener(SimStateListener ssl) {
        sSimStateListeners.remove(ssl);
    }
}
