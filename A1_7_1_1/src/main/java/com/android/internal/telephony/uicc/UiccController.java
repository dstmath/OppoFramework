package com.android.internal.telephony.uicc;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.OppoManager;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.provider.Settings.Global;
import android.provider.oppo.CallLog.Calls;
import android.provider.oppo.Telephony.SimInfo;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import com.android.internal.telephony.CommandsInterface;
import com.android.internal.telephony.CommandsInterface.RadioState;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppState;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.PersoSubState;
import com.mediatek.common.MPlugin;
import com.mediatek.common.telephony.IUiccControllerExt;
import com.mediatek.internal.telephony.RadioManager;
import com.mediatek.internal.telephony.dataconnection.DataSubSelector;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import com.oppo.hypnus.HypnusManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.LinkedList;

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
public class UiccController extends Handler {
    /* renamed from: -com-android-internal-telephony-uicc-IccCardApplicationStatus$PersoSubStateSwitchesValues */
    private static final /* synthetic */ int[] f19x16bf601e = null;
    public static final int APP_FAM_3GPP = 1;
    public static final int APP_FAM_3GPP2 = 2;
    public static final int APP_FAM_IMS = 3;
    private static final String COMMON_SLOT_PROPERTY = "ro.mtk_sim_hot_swap_common_slot";
    private static final boolean DBG = true;
    private static final String DECRYPT_STATE = "trigger_restart_framework";
    protected static final int EVENT_COMMON_SLOT_NO_CHANGED = 116;
    private static final int EVENT_GET_ICC_STATUS_DONE = 2;
    protected static final int EVENT_GET_ICC_STATUS_DONE_FOR_SIM_MISSING = 106;
    protected static final int EVENT_GET_ICC_STATUS_DONE_FOR_SIM_RECOVERY = 107;
    protected static final int EVENT_HOTSWAP_GET_ICC_STATUS_DONE = 111;
    private static final int EVENT_ICC_STATUS_CHANGED = 1;
    protected static final int EVENT_INVALID_SIM_DETECTED = 114;
    protected static final int EVENT_QUERY_ICCID_DONE_FOR_HOT_SWAP = 108;
    protected static final int EVENT_QUERY_SIM_MISSING = 113;
    protected static final int EVENT_QUERY_SIM_MISSING_STATUS = 104;
    protected static final int EVENT_QUERY_SIM_STATUS_FOR_PLUG_IN = 112;
    protected static final int EVENT_RADIO_AVAILABLE = 100;
    private static final int EVENT_RADIO_UNAVAILABLE = 3;
    protected static final int EVENT_REPOLL_SML_STATE = 115;
    protected static final int EVENT_SIM_MISSING = 103;
    protected static final int EVENT_SIM_PLUG_IN = 110;
    protected static final int EVENT_SIM_PLUG_OUT = 109;
    protected static final int EVENT_SIM_RECOVERY = 105;
    private static final int EVENT_SIM_REFRESH = 4;
    protected static final int EVENT_TRAY_PLUG_IN = 203;
    protected static final int EVENT_VIRTUAL_SIM_OFF = 102;
    protected static final int EVENT_VIRTUAL_SIM_ON = 101;
    private static final String FEATURE_ENABLE_HOTSWAP = "gsm.enable_hotswap";
    private static final String LOG_TAG = "UiccController";
    private static final int LOG_TYPE_SIM_PLUG_IN = 162;
    private static final int LOG_TYPE_SIM_PLUG_OUT = 163;
    private static final int LOG_TYPE_SIM_PLUG_RECOVERED = 164;
    private static final int MAX_PROACTIVE_COMMANDS_TO_LOG = 20;
    private static final int SML_FEATURE_NEED_BROADCAST_INTENT = 1;
    private static final int SML_FEATURE_NO_NEED_BROADCAST_INTENT = 0;
    private static boolean bIsHotSwapSimReboot;
    private static HypnusManager mHM;
    private static UiccController mInstance;
    public static volatile int mIsVsimDisable;
    private static final Object mLock = null;
    private static IUiccControllerExt mUiccControllerExt;
    private int[] UICCCONTROLLER_STRING_NOTIFICATION_SIM_MISSING;
    private int[] UICCCONTROLLER_STRING_NOTIFICATION_VIRTUAL_SIM_ON;
    private boolean bIsNeedRecordLog;
    protected boolean[] isSimPlugIn;
    protected boolean[] isTrayPlugIn;
    private RegistrantList mApplicationChangedRegistrants;
    private int mBtSlotId;
    private LinkedList<String> mCardLogs;
    private CommandsInterface[] mCis;
    private Context mContext;
    protected RegistrantList mIccChangedRegistrants;
    private int[] mIsimSessionId;
    private BroadcastReceiver mMdStateReceiver;
    private RegistrantList mRecoveryRegistrants;
    private UiccCard[] mUiccCards;

    private class ModemStateChangedReceiver extends BroadcastReceiver {
        final /* synthetic */ UiccController this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.uicc.UiccController.ModemStateChangedReceiver.<init>(com.android.internal.telephony.uicc.UiccController):void, dex: 
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
        private ModemStateChangedReceiver(com.android.internal.telephony.uicc.UiccController r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.uicc.UiccController.ModemStateChangedReceiver.<init>(com.android.internal.telephony.uicc.UiccController):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.UiccController.ModemStateChangedReceiver.<init>(com.android.internal.telephony.uicc.UiccController):void");
        }

        /* synthetic */ ModemStateChangedReceiver(UiccController this$0, ModemStateChangedReceiver modemStateChangedReceiver) {
            this(this$0);
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.uicc.UiccController.ModemStateChangedReceiver.onReceive(android.content.Context, android.content.Intent):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onReceive(android.content.Context r1, android.content.Intent r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.uicc.UiccController.ModemStateChangedReceiver.onReceive(android.content.Context, android.content.Intent):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.UiccController.ModemStateChangedReceiver.onReceive(android.content.Context, android.content.Intent):void");
        }
    }

    /* renamed from: -getcom-android-internal-telephony-uicc-IccCardApplicationStatus$PersoSubStateSwitchesValues */
    private static /* synthetic */ int[] m47x5ed1affa() {
        if (f19x16bf601e != null) {
            return f19x16bf601e;
        }
        int[] iArr = new int[PersoSubState.values().length];
        try {
            iArr[PersoSubState.PERSOSUBSTATE_IN_PROGRESS.ordinal()] = 6;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_READY.ordinal()] = 7;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_CORPORATE.ordinal()] = 8;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_CORPORATE_PUK.ordinal()] = 9;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_HRPD.ordinal()] = 10;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_HRPD_PUK.ordinal()] = 11;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_NETWORK1.ordinal()] = 12;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_NETWORK1_PUK.ordinal()] = 13;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_NETWORK2.ordinal()] = 14;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_NETWORK2_PUK.ordinal()] = 15;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_RUIM.ordinal()] = 16;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_RUIM_PUK.ordinal()] = 17;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_SERVICE_PROVIDER.ordinal()] = 18;
        } catch (NoSuchFieldError e13) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_RUIM_SERVICE_PROVIDER_PUK.ordinal()] = 19;
        } catch (NoSuchFieldError e14) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_CORPORATE.ordinal()] = 1;
        } catch (NoSuchFieldError e15) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_CORPORATE_PUK.ordinal()] = 20;
        } catch (NoSuchFieldError e16) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_NETWORK.ordinal()] = 2;
        } catch (NoSuchFieldError e17) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_NETWORK_PUK.ordinal()] = 21;
        } catch (NoSuchFieldError e18) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_NETWORK_SUBSET.ordinal()] = 3;
        } catch (NoSuchFieldError e19) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_NETWORK_SUBSET_PUK.ordinal()] = 22;
        } catch (NoSuchFieldError e20) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_SERVICE_PROVIDER.ordinal()] = 4;
        } catch (NoSuchFieldError e21) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_SERVICE_PROVIDER_PUK.ordinal()] = 23;
        } catch (NoSuchFieldError e22) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_SIM.ordinal()] = 5;
        } catch (NoSuchFieldError e23) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_SIM_SIM_PUK.ordinal()] = 24;
        } catch (NoSuchFieldError e24) {
        }
        try {
            iArr[PersoSubState.PERSOSUBSTATE_UNKNOWN.ordinal()] = 25;
        } catch (NoSuchFieldError e25) {
        }
        f19x16bf601e = iArr;
        return iArr;
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.uicc.UiccController.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.uicc.UiccController.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.uicc.UiccController.<clinit>():void");
    }

    public static UiccController make(Context c, CommandsInterface[] ci) {
        UiccController uiccController;
        synchronized (mLock) {
            if (mInstance != null) {
                throw new RuntimeException("MSimUiccController.make() should only be called once");
            }
            mInstance = new UiccController(c, ci);
            uiccController = mInstance;
        }
        return uiccController;
    }

    private UiccController(Context c, CommandsInterface[] ci) {
        this.mUiccCards = new UiccCard[TelephonyManager.getDefault().getPhoneCount()];
        this.isTrayPlugIn = new boolean[TelephonyManager.getDefault().getPhoneCount()];
        this.isSimPlugIn = new boolean[TelephonyManager.getDefault().getPhoneCount()];
        this.bIsNeedRecordLog = false;
        this.mIccChangedRegistrants = new RegistrantList();
        this.mRecoveryRegistrants = new RegistrantList();
        this.mIsimSessionId = new int[TelephonyManager.getDefault().getPhoneCount()];
        this.mApplicationChangedRegistrants = new RegistrantList();
        this.UICCCONTROLLER_STRING_NOTIFICATION_SIM_MISSING = new int[]{134545522, 134545530, 134545531, 134545532};
        this.UICCCONTROLLER_STRING_NOTIFICATION_VIRTUAL_SIM_ON = new int[]{134545515, 134545516, 134545517, 134545518};
        this.mCardLogs = new LinkedList();
        this.mBtSlotId = -1;
        log("Creating UiccController");
        this.mContext = c;
        this.mCis = ci;
        for (int i = 0; i < this.mCis.length; i++) {
            Integer index = new Integer(i);
            this.mCis[i].registerForIccStatusChanged(this, 1, index);
            log("crypto.state:" + SystemProperties.get("ro.crypto.state") + ", crypto.type:" + SystemProperties.get("ro.crypto.state") + ", vold.decrypt:" + SystemProperties.get("vold.decrypt"));
            if (SystemProperties.get("ro.crypto.state").equals("unencrypted") || SystemProperties.get("ro.crypto.state").equals("unsupported") || SystemProperties.get("ro.crypto.type").equals("file") || DECRYPT_STATE.equals(SystemProperties.get("vold.decrypt")) || StorageManager.isFileEncryptedNativeOrEmulated()) {
                this.mCis[i].registerForAvailable(this, 1, index);
            } else {
                this.mCis[i].registerForOn(this, 1, index);
            }
            this.mCis[i].registerForNotAvailable(this, 3, index);
            this.mCis[i].registerForIccRefresh(this, 4, index);
            this.mCis[i].registerForVirtualSimOn(this, 101, index);
            this.mCis[i].registerForVirtualSimOff(this, 102, index);
            this.mCis[i].registerForSimMissing(this, 103, index);
            this.mCis[i].registerForSimRecovery(this, 105, index);
            this.mCis[i].registerForSimPlugOut(this, 109, index);
            this.mCis[i].registerForSimPlugIn(this, 110, index);
            this.mCis[i].registerForCommonSlotNoChanged(this, 116, index);
            this.mCis[i].registerForTrayPlugIn(this, EVENT_TRAY_PLUG_IN, index);
            this.isTrayPlugIn[i] = false;
            this.isSimPlugIn[i] = false;
        }
        try {
            mUiccControllerExt = (IUiccControllerExt) MPlugin.createInstance(IUiccControllerExt.class.getName(), this.mContext);
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "Fail to create plug-in");
            e.printStackTrace();
        }
        this.mMdStateReceiver = new ModemStateChangedReceiver(this, null);
        IntentFilter filter = new IntentFilter();
        filter.addAction(RadioManager.ACTION_MODEM_POWER_NO_CHANGE);
        this.mContext.registerReceiver(this.mMdStateReceiver, filter);
        this.bIsNeedRecordLog = this.mContext.getPackageManager().hasSystemFeature("oppo.criticaldata.hardware.statistics.support");
        bIsHotSwapSimReboot = this.mContext.getPackageManager().hasSystemFeature("oppo.commcenter.reboot.dialog");
        setHotSwapSimRebootFlag(bIsHotSwapSimReboot);
    }

    public static UiccController getInstance() {
        UiccController uiccController;
        synchronized (mLock) {
            if (mInstance == null) {
                throw new RuntimeException("UiccController.getInstance can't be called before make()");
            }
            uiccController = mInstance;
        }
        return uiccController;
    }

    public UiccCard getUiccCard(int phoneId) {
        synchronized (mLock) {
            if (isValidCardIndex(phoneId)) {
                UiccCard uiccCard = this.mUiccCards[phoneId];
                return uiccCard;
            }
            return null;
        }
    }

    public UiccCard[] getUiccCards() {
        UiccCard[] uiccCardArr;
        synchronized (mLock) {
            uiccCardArr = (UiccCard[]) this.mUiccCards.clone();
        }
        return uiccCardArr;
    }

    public UiccCardApplication getUiccCardApplication(int family) {
        return getUiccCardApplication(SubscriptionController.getInstance().getPhoneId(SubscriptionController.getInstance().getDefaultSubId()), family);
    }

    public IccRecords getIccRecords(int phoneId, int family) {
        synchronized (mLock) {
            UiccCardApplication app = getUiccCardApplication(phoneId, family);
            if (app != null) {
                IccRecords iccRecords = app.getIccRecords();
                return iccRecords;
            }
            return null;
        }
    }

    public IccFileHandler getIccFileHandler(int phoneId, int family) {
        synchronized (mLock) {
            UiccCardApplication app = getUiccCardApplication(phoneId, family);
            if (app != null) {
                IccFileHandler iccFileHandler = app.getIccFileHandler();
                return iccFileHandler;
            }
            return null;
        }
    }

    public int getIccApplicationChannel(int slotId, int family) {
        int index;
        synchronized (mLock) {
            index = 0;
            switch (family) {
                case 3:
                    index = this.mIsimSessionId[slotId];
                    if (index == 0) {
                        if (getUiccCardApplication(slotId, family) == null) {
                            index = 0;
                            break;
                        }
                        index = 1;
                        break;
                    }
                    break;
                default:
                    log("unknown application");
                    break;
            }
        }
        return index;
    }

    public void registerForIccChanged(Handler h, int what, Object obj) {
        synchronized (mLock) {
            Registrant r = new Registrant(h, what, obj);
            this.mIccChangedRegistrants.add(r);
            int simNumber = TelephonyManager.getDefault().getPhoneCount();
            for (int i = 0; i < simNumber; i++) {
                if (this.mUiccCards[i] != null) {
                    r.notifyRegistrant(new AsyncResult(null, Integer.valueOf(i), null));
                    log("registerForIccChanged notify for slot:" + i);
                }
            }
        }
    }

    public void unregisterForIccChanged(Handler h) {
        synchronized (mLock) {
            this.mIccChangedRegistrants.remove(h);
        }
    }

    public void registerForIccRecovery(Handler h, int what, Object obj) {
        synchronized (mLock) {
            Registrant r = new Registrant(h, what, obj);
            this.mRecoveryRegistrants.add(r);
            r.notifyRegistrant();
        }
    }

    public void unregisterForIccRecovery(Handler h) {
        synchronized (mLock) {
            this.mRecoveryRegistrants.remove(h);
        }
    }

    /* JADX WARNING: Missing block: B:21:0x0090, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleMessage(Message msg) {
        synchronized (mLock) {
            Integer index = getCiIndex(msg);
            if (index.intValue() >= 0 && index.intValue() < this.mCis.length) {
                AsyncResult asyncResult = null;
                if (msg.obj != null && (msg.obj instanceof AsyncResult)) {
                    asyncResult = msg.obj;
                }
                switch (msg.what) {
                    case 1:
                        log("Received EVENT_ICC_STATUS_CHANGED, calling getIccCardStatus, index: " + index);
                        if (mHM == null) {
                            mHM = new HypnusManager();
                        }
                        if (mHM != null) {
                            mHM.hypnusSetAction(12, 30000);
                            log("hypnusSetAction()");
                        }
                        if (getVsimState() != 1) {
                            if (!ignoreGetSimStatus()) {
                                this.mCis[index.intValue()].getIccCardStatus(obtainMessage(2, index));
                                break;
                            } else {
                                log("FlightMode ON, Modem OFF: ignore get sim status");
                                break;
                            }
                        }
                        log("mIsVsimDisable is true, break");
                        break;
                    case 2:
                        log("Received EVENT_GET_ICC_STATUS_DONE");
                        onGetIccCardStatusDone(asyncResult, index);
                        break;
                    case 3:
                        log("EVENT_RADIO_UNAVAILABLE, dispose card");
                        if (this.mUiccCards[index.intValue()] != null) {
                            this.mUiccCards[index.intValue()].dispose();
                        }
                        this.mUiccCards[index.intValue()] = null;
                        this.mIccChangedRegistrants.notifyRegistrants(new AsyncResult(null, index, null));
                        break;
                    case 4:
                        log("Received EVENT_SIM_REFRESH");
                        onSimRefresh(asyncResult, index);
                        break;
                    case 101:
                        log("handleMessage (EVENT_VIRTUAL_SIM_ON)");
                        setNotificationVirtual(index.intValue(), 101);
                        Editor editorOn = this.mContext.getSharedPreferences("AutoAnswer", 1).edit();
                        editorOn.putBoolean("flag", true);
                        editorOn.commit();
                        break;
                    case 102:
                        log("handleMessage (EVENT_VIRTUAL_SIM_OFF)");
                        removeNotificationVirtual(index.intValue(), 101);
                        Editor editorOff = this.mContext.getSharedPreferences("AutoAnswer", 1).edit();
                        editorOff.putBoolean("flag", false);
                        editorOff.commit();
                        break;
                    case 103:
                        log("handleMessage (EVENT_SIM_MISSING)");
                        notifyIccIdForSimPlugOut(index.intValue());
                        this.mCis[index.intValue()].getIccCardStatus(obtainMessage(106, index));
                        break;
                    case 105:
                        log("handleMessage (EVENT_SIM_RECOVERY)");
                        this.mCis[index.intValue()].getIccCardStatus(obtainMessage(107, index));
                        this.mRecoveryRegistrants.notifyRegistrants(new AsyncResult(null, index, null));
                        Intent intent = new Intent();
                        intent.setAction("com.android.phone.ACTION_SIM_RECOVERY_DONE");
                        this.mContext.sendBroadcast(intent);
                        break;
                    case 106:
                        log("Received EVENT_GET_ICC_STATUS_DONE_FOR_SIM_MISSING");
                        onGetIccCardStatusDone((AsyncResult) msg.obj, index, false);
                        break;
                    case 107:
                        log("Received EVENT_GET_ICC_STATUS_DONE_FOR_SIM_RECOVERY");
                        onGetIccCardStatusDone((AsyncResult) msg.obj, index, false);
                        break;
                    case 109:
                        log("EVENT_SIM_PLUG_OUT, index=" + index);
                        notifyIccIdForSimPlugOut(index.intValue());
                        this.isSimPlugIn[index.intValue()] = false;
                        this.isTrayPlugIn[index.intValue()] = false;
                        saveSimPlugState(index.intValue(), 163);
                        break;
                    case 110:
                        log("Received EVENT_SIM_PLUG_IN, index=" + index);
                        log("EVENT_SIM_PLUG_IN,isTrayPlugIn[" + index + "]=" + this.isTrayPlugIn[index.intValue()]);
                        if (!this.isTrayPlugIn[index.intValue()]) {
                            log("not call notifyIccIdForSimPlugIn, index=" + index);
                            saveSimPlugState(index.intValue(), 164);
                            break;
                        }
                        log("notifyIccIdForSimPlugIn, index=" + index);
                        notifyIccIdForSimPlugIn(index.intValue());
                        this.isSimPlugIn[index.intValue()] = true;
                        this.isTrayPlugIn[index.intValue()] = false;
                        break;
                    case 115:
                        log("Received EVENT_REPOLL_SML_STATE");
                        asyncResult = msg.obj;
                        boolean needIntent = msg.arg1 == 1;
                        onGetIccCardStatusDone(asyncResult, index, false);
                        if (this.mUiccCards[index.intValue()] != null && needIntent) {
                            UiccCardApplication app = this.mUiccCards[index.intValue()].getApplication(1);
                            if (app != null) {
                                if (app.getState() == AppState.APPSTATE_SUBSCRIPTION_PERSO) {
                                    Intent lockIntent = new Intent();
                                    if (lockIntent != null) {
                                        log("Broadcast ACTION_UNLOCK_SIM_LOCK");
                                        lockIntent.setAction("mediatek.intent.action.ACTION_UNLOCK_SIM_LOCK");
                                        lockIntent.putExtra("ss", "LOCKED");
                                        lockIntent.putExtra(DataSubSelector.EXTRA_MOBILE_DATA_ENABLE_REASON, parsePersoType(app.getPersoSubState()));
                                        SubscriptionManager.putPhoneIdAndSubIdExtra(lockIntent, index.intValue());
                                        this.mContext.sendBroadcast(lockIntent);
                                        break;
                                    }
                                    log("New intent failed");
                                    return;
                                }
                            }
                            log("UiccCardApplication = null");
                            break;
                        }
                        break;
                    case 116:
                        log("handleMessage (EVENT_COMMON_SLOT_NO_CHANGED)");
                        Intent intentNoChanged = new Intent("com.mediatek.phone.ACTION_COMMON_SLOT_NO_CHANGED");
                        int slotId = index.intValue();
                        SubscriptionManager.putPhoneIdAndSubIdExtra(intentNoChanged, slotId);
                        log("Broadcasting intent ACTION_COMMON_SLOT_NO_CHANGED for mSlotId : " + slotId);
                        this.mContext.sendBroadcast(intentNoChanged);
                        break;
                    case EVENT_TRAY_PLUG_IN /*203*/:
                        log("EVENT_TRAY_PLUG_IN, isTrayPlugIn[" + index + "]=" + this.isTrayPlugIn[index.intValue()] + ",will set to true");
                        this.isTrayPlugIn[index.intValue()] = true;
                        notifyIccIdForTrayPlugIn(index.intValue());
                        saveSimPlugState(index.intValue(), 162);
                        break;
                    default:
                        Rlog.e(LOG_TAG, " Unknown Event " + msg.what);
                        break;
                }
            }
            Rlog.e(LOG_TAG, "Invalid index : " + index + " received with event " + msg.what);
        }
    }

    private Integer getCiIndex(Message msg) {
        Integer index = new Integer(0);
        if (msg == null) {
            return index;
        }
        if (msg.obj != null && (msg.obj instanceof Integer)) {
            return msg.obj;
        }
        if (msg.obj == null || !(msg.obj instanceof AsyncResult)) {
            return index;
        }
        AsyncResult ar = msg.obj;
        if (ar.userObj == null || !(ar.userObj instanceof Integer)) {
            return index;
        }
        return ar.userObj;
    }

    /* JADX WARNING: Missing block: B:11:0x001b, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public UiccCardApplication getUiccCardApplication(int phoneId, int family) {
        synchronized (mLock) {
            if (!isValidCardIndex(phoneId) || this.mUiccCards[phoneId] == null) {
            } else {
                UiccCardApplication application = this.mUiccCards[phoneId].getApplication(family);
                return application;
            }
        }
    }

    private synchronized void onGetIccCardStatusDone(AsyncResult ar, Integer index) {
        if (ar.exception != null) {
            Rlog.e(LOG_TAG, "Error getting ICC status. RIL_REQUEST_GET_ICC_STATUS should never return an error", ar.exception);
        } else if (isValidCardIndex(index.intValue())) {
            log("onGetIccCardStatusDone, index " + index);
            IccCardStatus status = ar.result;
            if (this.mUiccCards[index.intValue()] == null) {
                this.mUiccCards[index.intValue()] = new UiccCard(this.mContext, this.mCis[index.intValue()], status, index.intValue());
            } else {
                this.mUiccCards[index.intValue()].update(this.mContext, this.mCis[index.intValue()], status);
            }
            log("Notifying IccChangedRegistrants");
            this.mIccChangedRegistrants.notifyRegistrants(new AsyncResult(null, index, null));
        } else {
            Rlog.e(LOG_TAG, "onGetIccCardStatusDone: invalid index : " + index);
        }
    }

    private void onSimRefresh(AsyncResult ar, Integer index) {
        if (ar.exception != null) {
            Rlog.e(LOG_TAG, "Sim REFRESH with exception: " + ar.exception);
        } else if (isValidCardIndex(index.intValue())) {
            IccRefreshResponse resp = ar.result;
            Rlog.d(LOG_TAG, "onSimRefresh: " + resp);
            if (this.mUiccCards[index.intValue()] == null) {
                Rlog.e(LOG_TAG, "onSimRefresh: refresh on null card : " + index);
            } else if (resp.refreshResult != 2 || resp.aid == null) {
                Rlog.d(LOG_TAG, "Ignoring reset: " + resp);
            } else {
                Rlog.d(LOG_TAG, "Handling refresh reset: " + resp);
                if (this.mUiccCards[index.intValue()].resetAppWithAid(resp.aid)) {
                    boolean requirePowerOffOnSimRefreshReset = this.mContext.getResources().getBoolean(17956992);
                    if (SystemProperties.get("ro.sim_refresh_reset_by_modem").equals("1")) {
                        this.mCis[index.intValue()].getIccCardStatus(obtainMessage(2));
                    } else {
                        this.mCis[index.intValue()].resetRadio(null);
                    }
                    this.mIccChangedRegistrants.notifyRegistrants(new AsyncResult(null, index, null));
                }
            }
        } else {
            Rlog.e(LOG_TAG, "onSimRefresh: invalid index : " + index);
        }
    }

    private boolean isValidCardIndex(int index) {
        return index >= 0 && index < this.mUiccCards.length;
    }

    private void log(String string) {
        Rlog.d(LOG_TAG, string);
    }

    public void addCardLog(String data) {
        Time t = new Time();
        t.setToNow();
        this.mCardLogs.addLast(t.format("%m-%d %H:%M:%S") + " " + data);
        if (this.mCardLogs.size() > 20) {
            this.mCardLogs.removeFirst();
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        int i;
        pw.println("UiccController: " + this);
        pw.println(" mContext=" + this.mContext);
        pw.println(" mInstance=" + mInstance);
        pw.println(" mIccChangedRegistrants: size=" + this.mIccChangedRegistrants.size());
        for (i = 0; i < this.mIccChangedRegistrants.size(); i++) {
            pw.println("  mIccChangedRegistrants[" + i + "]=" + ((Registrant) this.mIccChangedRegistrants.get(i)).getHandler());
        }
        pw.println();
        pw.flush();
        pw.println(" mUiccCards: size=" + this.mUiccCards.length);
        for (i = 0; i < this.mUiccCards.length; i++) {
            if (this.mUiccCards[i] == null) {
                pw.println("  mUiccCards[" + i + "]=null");
            } else {
                pw.println("  mUiccCards[" + i + "]=" + this.mUiccCards[i]);
                this.mUiccCards[i].dump(fd, pw, args);
            }
        }
        pw.println("mCardLogs: ");
        for (i = 0; i < this.mCardLogs.size(); i++) {
            pw.println("  " + ((String) this.mCardLogs.get(i)));
        }
    }

    /* JADX WARNING: Missing block: B:21:0x00a7, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void onGetIccCardStatusDone(AsyncResult ar, Integer index, boolean isUpdate) {
        if (ar.exception != null) {
            Rlog.e(LOG_TAG, "Error getting ICC status. RIL_REQUEST_GET_ICC_STATUS should never return an error", ar.exception);
        } else if (isValidCardIndex(index.intValue())) {
            log("onGetIccCardStatusDone, index " + index + "isUpdateSiminfo " + isUpdate);
            IccCardStatus status = ar.result;
            if (this.mUiccCards[index.intValue()] == null) {
                this.mUiccCards[index.intValue()] = new UiccCard(this.mContext, this.mCis[index.intValue()], status, index.intValue(), isUpdate);
            } else {
                this.mUiccCards[index.intValue()].update(this.mContext, this.mCis[index.intValue()], status, isUpdate);
            }
            log("Notifying IccChangedRegistrants");
            if (SystemProperties.get(COMMON_SLOT_PROPERTY).equals("1")) {
                Bundle result = new Bundle();
                result.putInt("Index", index.intValue());
                result.putBoolean("ForceUpdate", isUpdate);
                this.mIccChangedRegistrants.notifyRegistrants(new AsyncResult(null, result, null));
            } else {
                this.mIccChangedRegistrants.notifyRegistrants(new AsyncResult(null, index, null));
            }
        } else {
            Rlog.e(LOG_TAG, "onGetIccCardStatusDone: invalid index : " + index);
        }
    }

    private void setNotification(int slot, int notifyType) {
        log("setNotification(): notifyType = " + notifyType);
        Notification notification = new Notification();
        notification.when = System.currentTimeMillis();
        notification.flags = 16;
        notification.icon = 17301642;
        notification.contentIntent = PendingIntent.getActivity(this.mContext, 0, new Intent(), 134217728);
        String title = mUiccControllerExt.getMissingTitle(this.mContext, slot);
        CharSequence detail = mUiccControllerExt.getMissingDetail(this.mContext);
        notification.tickerText = title;
        notification.setLatestEventInfo(this.mContext, title, detail, notification.contentIntent);
        ((NotificationManager) this.mContext.getSystemService("notification")).notify(notifyType + slot, notification);
    }

    public void disableSimMissingNotification(int slot) {
        ((NotificationManager) this.mContext.getSystemService("notification")).cancel(slot + 103);
    }

    private void setNotificationVirtual(int slot, int notifyType) {
        String title;
        log("setNotificationVirtual(): notifyType = " + notifyType);
        Notification notification = new Notification();
        notification.when = System.currentTimeMillis();
        notification.flags = 16;
        notification.icon = 17301642;
        notification.contentIntent = PendingIntent.getActivity(this.mContext, 0, new Intent(), 134217728);
        if (TelephonyManager.getDefault().getSimCount() > 1) {
            title = Resources.getSystem().getText(this.UICCCONTROLLER_STRING_NOTIFICATION_VIRTUAL_SIM_ON[slot]).toString();
        } else {
            title = Resources.getSystem().getText(134545519).toString();
        }
        CharSequence detail = this.mContext.getText(134545519).toString();
        notification.tickerText = this.mContext.getText(134545519).toString();
        notification.setLatestEventInfo(this.mContext, title, detail, notification.contentIntent);
        ((NotificationManager) this.mContext.getSystemService("notification")).notify(notifyType + slot, notification);
    }

    private void removeNotificationVirtual(int slot, int notifyType) {
        ((NotificationManager) this.mContext.getSystemService("notification")).cancel(notifyType + slot);
    }

    public int getBtConnectedSimId() {
        log("getBtConnectedSimId, slot " + this.mBtSlotId);
        return this.mBtSlotId;
    }

    public void setBtConnectedSimId(int simId) {
        this.mBtSlotId = simId;
        log("setBtConnectedSimId, slot " + this.mBtSlotId);
    }

    private String parsePersoType(PersoSubState state) {
        log("parsePersoType, state = " + state);
        switch (m47x5ed1affa()[state.ordinal()]) {
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
                return "UNKNOWN";
        }
    }

    public void repollIccStateForModemSmlChangeFeatrue(int slotId, boolean needIntent) {
        log("repollIccStateForModemSmlChangeFeatrue, needIntent = " + needIntent);
        this.mCis[slotId].getIccCardStatus(obtainMessage(115, needIntent ? 1 : 0, 0, Integer.valueOf(slotId)));
    }

    public void registerForApplicationChanged(Handler h, int what, Object obj) {
        synchronized (mLock) {
            Registrant r = new Registrant(h, what, obj);
            this.mApplicationChangedRegistrants.add(r);
            r.notifyRegistrant();
        }
    }

    public void unregisterForApplicationChanged(Handler h) {
        synchronized (mLock) {
            this.mApplicationChangedRegistrants.remove(h);
        }
    }

    public boolean ignoreGetSimStatus() {
        int airplaneMode = Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0);
        log("ignoreGetSimStatus(): airplaneMode - " + airplaneMode);
        if (!RadioManager.isFlightModePowerOffModemEnabled() || airplaneMode != 1) {
            return false;
        }
        log("ignoreGetSimStatus(): return true");
        return true;
    }

    public boolean isAllRadioAvailable() {
        boolean isRadioReady = true;
        for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++) {
            if (RadioState.RADIO_UNAVAILABLE == this.mCis[i].getRadioState()) {
                isRadioReady = false;
            }
        }
        log("isAllRadioAvailable = " + isRadioReady);
        return isRadioReady;
    }

    public void resetRadioForVsim() {
        int allPhoneIdBitMask = (1 << TelephonyManager.getDefault().getPhoneCount()) - 1;
        log("resetRadioForVsim...false");
        RadioManager.getInstance().setModemPower(false, allPhoneIdBitMask);
        log("resetRadioForVsim...true");
        RadioManager.getInstance().setModemPower(true, allPhoneIdBitMask);
    }

    private void notifyIccIdForSimPlugOut(int slotid) {
        log("notifyIccIdForSimPlugOut plug out sim slotid = " + slotid);
        SystemProperties.set("com.oppo.sim_plug_in", "false");
        broadcastIccStateChangedIntent("ABSENT", "PLUGOUT", slotid);
    }

    private void notifyIccIdForSimPlugIn(int slotid) {
        if (slotid < 0 || slotid > 1) {
            log("notifyIccIdForSimPlugIn failed slotid = " + slotid);
            return;
        }
        log("notifyIccIdForSimPlugIn slotid = " + slotid);
        SystemProperties.set("com.oppo.sim_plug_in", "true");
        boolean hotswapSimReboot = isHotSwapSimReboot();
        log("hotswapSimReboot:" + hotswapSimReboot);
        if (hotswapSimReboot) {
            broadcastIccStateChangedIntent("NOT_READY", "PLUGIN", slotid);
        }
    }

    private void broadcastIccStateChangedIntent(String value, String reason, int slotid) {
        Intent intent = new Intent("android.intent.action.SIM_STATE_CHANGED");
        intent.putExtra("phoneName", "Phone");
        intent.putExtra("ss", value);
        intent.putExtra(DataSubSelector.EXTRA_MOBILE_DATA_ENABLE_REASON, reason);
        intent.putExtra(SimInfo.SLOT, slotid);
        intent.putExtra(Calls.SIM_ID, slotid);
        int[] subIds = SubscriptionManager.getSubId(slotid);
        if (subIds != null && subIds.length > 0) {
            intent.putExtra("subscription", (long) subIds[0]);
        }
        log("Broadcasting intent ACTION_SIM_STATE_CHANGED " + value + " reason " + reason + " sim id " + slotid);
        this.mContext.sendBroadcast(intent);
    }

    private void notifyIccIdForTrayPlugIn(int slotid) {
        if (slotid < 0 || slotid > 1) {
            log("notifyIccIdForTrayPlugIn failed slotid = " + slotid);
            return;
        }
        log("notifyIccIdForTrayPlugIn slotid = " + slotid);
        broadcastIccStateChangedIntent("NOT_READY", "TRAYPLUGIN", slotid);
    }

    private void saveSimPlugState(int index, int type) {
        log("saveSimPlugState(), bIsNeedRecordLog = " + this.bIsNeedRecordLog);
        if (this.bIsNeedRecordLog) {
            String issue;
            int logType;
            String log_desc;
            String logString = "old cardState is: absent";
            if (!(this.mUiccCards == null || this.mUiccCards[index] == null)) {
                logString = "old cardState is:" + this.mUiccCards[index].getCardState();
            }
            switch (type) {
                case 162:
                    issue = "Sim PlugIn";
                    break;
                case 163:
                    issue = "Sim PlugOut";
                    break;
                case 164:
                    issue = "Sim Recovered";
                    break;
                default:
                    issue = "unknow";
                    break;
            }
            try {
                String[] logArray = this.mContext.getString(this.mContext.getResources().getIdentifier("zz_oppo_critical_log_" + type, "string", "android")).split(",");
                logType = Integer.valueOf(logArray[0]).intValue();
                log_desc = logArray[1];
            } catch (Exception e) {
                log_desc = UsimPBMemInfo.STRING_NOT_SET;
                logType = type + 261;
            }
            OppoManager.writeLogToPartition(logType, logString, "NETWORK", issue, log_desc);
        }
    }

    public static void setVsimState(int status) {
        mIsVsimDisable = status;
        Rlog.d(LOG_TAG, "setVsimState(), mIsVsimDisable = " + mIsVsimDisable);
    }

    public static int getVsimState() {
        Rlog.d(LOG_TAG, "getVsimState(), mIsVsimDisable = " + mIsVsimDisable);
        return mIsVsimDisable;
    }

    public static boolean isHotSwapSimReboot() {
        return SystemProperties.get(FEATURE_ENABLE_HOTSWAP, "true").equals("false");
    }

    private void setHotSwapSimRebootFlag(boolean Reboot) {
        SystemProperties.set(FEATURE_ENABLE_HOTSWAP, Reboot ? "false" : "true");
    }
}
