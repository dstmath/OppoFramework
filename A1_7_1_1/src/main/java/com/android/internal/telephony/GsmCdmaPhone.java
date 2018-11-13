package com.android.internal.telephony;

import android.app.ActivityManagerNative;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.SQLException;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Telephony.Carriers;
import android.telecom.VideoProfile;
import android.telephony.CarrierConfigManager;
import android.telephony.CellLocation;
import android.telephony.PhoneNumberUtils;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.text.TextUtils;
import android.util.Log;
import com.android.ims.ImsException;
import com.android.ims.ImsManager;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.CommandsInterface.RadioState;
import com.android.internal.telephony.DctConstants.Activity;
import com.android.internal.telephony.DctConstants.State;
import com.android.internal.telephony.PhoneConstants.DataState;
import com.android.internal.telephony.PhoneInternalInterface.DataActivityState;
import com.android.internal.telephony.PhoneInternalInterface.SuppService;
import com.android.internal.telephony.cdma.CdmaMmiCode;
import com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager;
import com.android.internal.telephony.cdma.EriManager;
import com.android.internal.telephony.gsm.GsmMmiCode;
import com.android.internal.telephony.gsm.SuppCrssNotification;
import com.android.internal.telephony.gsm.SuppServiceNotification;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.imsphone.ImsPhoneMmiCode;
import com.android.internal.telephony.test.SimulatedRadioControl;
import com.android.internal.telephony.uicc.IccCardProxy;
import com.android.internal.telephony.uicc.IccException;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IccVmNotSupportedException;
import com.android.internal.telephony.uicc.IsimRecords;
import com.android.internal.telephony.uicc.IsimUiccRecords;
import com.android.internal.telephony.uicc.RuimRecords;
import com.android.internal.telephony.uicc.SIMRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.google.android.mms.pdu.CharacterSets;
import com.mediatek.common.MPlugin;
import com.mediatek.common.telephony.ISupplementaryServiceExt;
import com.mediatek.internal.telephony.ITelephonyEx;
import com.mediatek.internal.telephony.ITelephonyEx.Stub;
import com.mediatek.internal.telephony.ImsSwitchController;
import com.mediatek.internal.telephony.OperatorUtils;
import com.mediatek.internal.telephony.OperatorUtils.OPID;
import com.mediatek.internal.telephony.uicc.CsimPhbStorageInfo;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class GsmCdmaPhone extends Phone {
    /* renamed from: -com-android-internal-telephony-DctConstants$ActivitySwitchesValues */
    private static final /* synthetic */ int[] f9xfa7940f = null;
    /* renamed from: -com-android-internal-telephony-DctConstants$StateSwitchesValues */
    private static final /* synthetic */ int[] f10-com-android-internal-telephony-DctConstants$StateSwitchesValues = null;
    public static final int CANCEL_ECM_TIMER = 1;
    private static final String CFB_KEY = "CFB";
    private static final String CFNRC_KEY = "CFNRC";
    private static final String CFNR_KEY = "CFNR";
    private static final String CFU_QUERY_ICCID_PROP = "persist.radio.cfu.iccid.";
    private static final int CFU_QUERY_MAX_COUNT = 60;
    private static final String CFU_QUERY_PROPERTY_NAME = "gsm.poweron.cfu.query.";
    private static final String CFU_QUERY_SIM_CHANGED_PROP = "persist.radio.cfu.change.";
    private static final boolean DBG = true;
    private static final int DEFAULT_ECM_EXIT_TIMER_VALUE = 300000;
    public static final String IMS_DEREG_OFF = "0";
    public static final String IMS_DEREG_ON = "1";
    public static final String IMS_DEREG_PROP = "gsm.radio.ss.imsdereg";
    private static final int INVALID_SYSTEM_SELECTION_CODE = -1;
    private static final String IS683A_FEATURE_CODE = "*228";
    private static final int IS683A_FEATURE_CODE_NUM_DIGITS = 4;
    private static final int IS683A_SYS_SEL_CODE_NUM_DIGITS = 2;
    private static final int IS683A_SYS_SEL_CODE_OFFSET = 4;
    private static final int IS683_CONST_1900MHZ_A_BLOCK = 2;
    private static final int IS683_CONST_1900MHZ_B_BLOCK = 3;
    private static final int IS683_CONST_1900MHZ_C_BLOCK = 4;
    private static final int IS683_CONST_1900MHZ_D_BLOCK = 5;
    private static final int IS683_CONST_1900MHZ_E_BLOCK = 6;
    private static final int IS683_CONST_1900MHZ_F_BLOCK = 7;
    private static final int IS683_CONST_800MHZ_A_BAND = 0;
    private static final int IS683_CONST_800MHZ_B_BAND = 1;
    public static final String LOG_TAG = "GsmCdmaPhone";
    public static final int MESSAGE_SET_CF = 1;
    private static final boolean MTK_SVLTE_SUPPORT = false;
    public static final String PROPERTY_CDMA_HOME_OPERATOR_NUMERIC = "ro.cdma.home.operator.numeric";
    private static final String[] PROPERTY_RIL_FULL_UICC_TYPE = null;
    private static final String PROP_MTK_CDMA_LTE_MODE = "ro.boot.opt_c2k_lte_mode";
    public static final int RESTART_ECM_TIMER = 0;
    private static final boolean SDBG = false;
    public static final String SS_CW_TBCW_EVER_ENABLE_PROP = "gsm.radio.ss.tbcweverenable";
    private static final String SS_SERVICE_CLASS_PROP = "gsm.radio.ss.sc";
    public static final int TBCW_NOT_OPTBCW = 1;
    public static final int TBCW_OPTBCW_NOT_VOLTE_USER = 3;
    public static final int TBCW_OPTBCW_VOLTE_USER = 2;
    public static final int TBCW_OPTBCW_WITH_CS = 4;
    public static final int TBCW_UNKNOWN = 0;
    private static final boolean VDBG = false;
    private static final String VM_NUMBER = "vm_number_key";
    private static final String VM_NUMBER_CDMA = "vm_number_key_cdma";
    private static final String VM_SIM_IMSI = "vm_sim_imsi_key";
    private static final int cfuQueryWaitTime = 1000;
    private static Pattern pOtaSpNumSchema;
    private boolean mBroadcastEmergencyCallStateChanges;
    private BroadcastReceiver mBroadcastReceiver;
    public GsmCdmaCallTracker mCT;
    private AsyncResult mCachedCrssn;
    private AsyncResult mCachedSsn;
    RegistrantList mCallRelatedSuppSvcRegistrants;
    private String mCarrierOtaSpNumSchema;
    private CdmaSubscriptionSourceManager mCdmaSSM;
    public int mCdmaSubscriptionSource;
    private int mCfuQueryRetryCount;
    private int mDeviceIdAbnormal;
    private Registrant mEcmExitRespRegistrant;
    private final RegistrantList mEcmTimerResetRegistrants;
    private final RegistrantList mEriFileLoadedRegistrants;
    public EriManager mEriManager;
    private String mEsn;
    private Runnable mExitEcmRunnable;
    private IccCardProxy mIccCardProxy;
    private IccPhoneBookInterfaceManager mIccPhoneBookIntManager;
    private IccSmsInterfaceManager mIccSmsInterfaceManager;
    private String mImei;
    private String mImeiSv;
    public boolean mIsNetworkInitiatedUssr;
    private boolean mIsPhoneInEcmState;
    private IsimUiccRecords mIsimUiccRecords;
    private String mMeid;
    private int mNewVoiceTech;
    private ArrayList<MmiCode> mPendingMMIs;
    private int mPrecisePhoneType;
    private boolean mResetModemOnRadioTechnologyChange;
    private int mRilVersion;
    SSRequestDecisionMaker mSSReqDecisionMaker;
    public ServiceStateTracker mSST;
    private SIMRecords mSimRecords;
    private RegistrantList mSsnRegistrants;
    ISupplementaryServiceExt mSupplementaryServiceExt;
    private int mTbcwMode;
    TelephonyDevController mTelDevController;
    private String mVmNumber;
    private WakeLock mWakeLock;
    private boolean needQueryCfu;

    private static class Cfu {
        final Message mOnComplete;
        final String mSetCfNumber;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.GsmCdmaPhone.Cfu.<init>(java.lang.String, android.os.Message):void, dex: 
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
        Cfu(java.lang.String r1, android.os.Message r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.GsmCdmaPhone.Cfu.<init>(java.lang.String, android.os.Message):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.GsmCdmaPhone.Cfu.<init>(java.lang.String, android.os.Message):void");
        }
    }

    private static class CfuEx {
        final Message mOnComplete;
        final String mSetCfNumber;
        final long[] mSetTimeSlot;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.GsmCdmaPhone.CfuEx.<init>(java.lang.String, long[], android.os.Message):void, dex: 
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
        CfuEx(java.lang.String r1, long[] r2, android.os.Message r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.GsmCdmaPhone.CfuEx.<init>(java.lang.String, long[], android.os.Message):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.GsmCdmaPhone.CfuEx.<init>(java.lang.String, long[], android.os.Message):void");
        }
    }

    /* renamed from: -getcom-android-internal-telephony-DctConstants$ActivitySwitchesValues */
    private static /* synthetic */ int[] m17xd0f730eb() {
        if (f9xfa7940f != null) {
            return f9xfa7940f;
        }
        int[] iArr = new int[Activity.values().length];
        try {
            iArr[Activity.DATAIN.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Activity.DATAINANDOUT.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Activity.DATAOUT.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Activity.DORMANT.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Activity.NONE.ordinal()] = 12;
        } catch (NoSuchFieldError e5) {
        }
        f9xfa7940f = iArr;
        return iArr;
    }

    /* renamed from: -getcom-android-internal-telephony-DctConstants$StateSwitchesValues */
    private static /* synthetic */ int[] m18xf0fbc33d() {
        if (f10-com-android-internal-telephony-DctConstants$StateSwitchesValues != null) {
            return f10-com-android-internal-telephony-DctConstants$StateSwitchesValues;
        }
        int[] iArr = new int[State.values().length];
        try {
            iArr[State.CONNECTED.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[State.CONNECTING.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[State.DISCONNECTING.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[State.FAILED.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[State.IDLE.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[State.RETRYING.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[State.SCANNING.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        f10-com-android-internal-telephony-DctConstants$StateSwitchesValues = iArr;
        return iArr;
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.GsmCdmaPhone.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.GsmCdmaPhone.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.GsmCdmaPhone.<clinit>():void");
    }

    private boolean hasC2kOverImsModem() {
        if (this.mTelDevController == null || this.mTelDevController.getModem(0) == null || !this.mTelDevController.getModem(0).hasC2kOverImsModem()) {
            return false;
        }
        return true;
    }

    public GsmCdmaPhone(Context context, CommandsInterface ci, PhoneNotifier notifier, int phoneId, int precisePhoneType, TelephonyComponentFactory telephonyComponentFactory) {
        this(context, ci, notifier, false, phoneId, precisePhoneType, telephonyComponentFactory);
    }

    public GsmCdmaPhone(Context context, CommandsInterface ci, PhoneNotifier notifier, boolean unitTestMode, int phoneId, int precisePhoneType, TelephonyComponentFactory telephonyComponentFactory) {
        String str;
        if (precisePhoneType == 1) {
            str = "GSM";
        } else {
            str = "CDMA";
        }
        super(str, notifier, context, ci, unitTestMode, phoneId, telephonyComponentFactory);
        this.mSsnRegistrants = new RegistrantList();
        this.mCdmaSubscriptionSource = -1;
        this.mEriFileLoadedRegistrants = new RegistrantList();
        this.needQueryCfu = false;
        this.mCfuQueryRetryCount = 0;
        this.mTbcwMode = 0;
        this.mIsNetworkInitiatedUssr = false;
        this.mExitEcmRunnable = new Runnable() {
            public void run() {
                GsmCdmaPhone.this.exitEmergencyCallbackMode();
            }
        };
        this.mPendingMMIs = new ArrayList();
        this.mEcmTimerResetRegistrants = new RegistrantList();
        this.mDeviceIdAbnormal = 0;
        this.mResetModemOnRadioTechnologyChange = false;
        this.mBroadcastEmergencyCallStateChanges = false;
        this.mCallRelatedSuppSvcRegistrants = new RegistrantList();
        this.mCachedSsn = null;
        this.mCachedCrssn = null;
        this.mNewVoiceTech = -1;
        this.mTelDevController = TelephonyDevController.getInstance();
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (intent.getAction().equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                    GsmCdmaPhone.this.sendMessage(GsmCdmaPhone.this.obtainMessage(43));
                } else if ("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED".equals(action)) {
                    GsmCdmaPhone.this.handleSubInfoChange();
                } else if (action.equals("com.android.ims.IMS_STATE_CHANGED")) {
                    int reg = intent.getIntExtra("android:regState", -1);
                    int slotId = intent.getIntExtra("android:phone_id", -1);
                    Rlog.d(GsmCdmaPhone.LOG_TAG, "onReceive ACTION_IMS_STATE_CHANGED: reg=" + reg + ", SimID=" + slotId);
                    if (slotId == GsmCdmaPhone.this.getPhoneId() && reg == 0) {
                        if (GsmCdmaPhone.this.isOpTbcwWithCS(GsmCdmaPhone.this.getPhoneId())) {
                            GsmCdmaPhone.this.setTbcwMode(4);
                            GsmCdmaPhone.this.setTbcwToEnabledOnIfDisabled();
                        } else {
                            GsmCdmaPhone.this.setTbcwMode(2);
                            GsmCdmaPhone.this.setTbcwToEnabledOnIfDisabled();
                        }
                        Rlog.d(GsmCdmaPhone.LOG_TAG, "set needQueryCfu true, due to ACTION_IMS_STATE_CHANGED, phoneId = " + GsmCdmaPhone.this.getPhoneId());
                        GsmCdmaPhone.this.needQueryCfu = true;
                        GsmCdmaPhone.this.sendMessage(GsmCdmaPhone.this.obtainMessage(2002));
                    }
                    if (GsmCdmaPhone.this.mSST == null || (GsmCdmaPhone.this.mSST.mSS.getState() != 0 && GsmCdmaPhone.this.mSST.mSS.getDataRegState() == 0)) {
                        GsmCdmaPhone.this.notifyServiceStateChanged(GsmCdmaPhone.this.mSST.mSS);
                    }
                } else if (action.equals("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE")) {
                    Rlog.d(GsmCdmaPhone.LOG_TAG, "set needQueryCfu true, due to ACTION_SET_RADIO_CAPABILITY_DONE");
                    GsmCdmaPhone.this.needQueryCfu = true;
                } else if (action.equals("android.intent.action.AIRPLANE_MODE")) {
                    boolean bAirplaneModeOn = intent.getBooleanExtra("state", false);
                    Rlog.d(GsmCdmaPhone.LOG_TAG, "ACTION_AIRPLANE_MODE_CHANGED, bAirplaneModeOn = " + bAirplaneModeOn);
                    if (bAirplaneModeOn) {
                        Rlog.d(GsmCdmaPhone.LOG_TAG, "Set needQueryCfu true, due to ACTION_AIRPLANE_MODE_CHANGED");
                        GsmCdmaPhone.this.needQueryCfu = true;
                        if (GsmCdmaPhone.this.isOp(OPID.OP02)) {
                            Rlog.d(GsmCdmaPhone.LOG_TAG, "isOp02IccCard, setCsFallbackStatus 0");
                            GsmCdmaPhone.this.setCsFallbackStatus(0);
                        }
                    }
                }
            }
        };
        this.mPrecisePhoneType = precisePhoneType;
        initOnce(ci);
        initRatSpecific(precisePhoneType);
        this.mSST = this.mTelephonyComponentFactory.makeServiceStateTracker(this, this.mCi);
        this.mDcTracker = this.mTelephonyComponentFactory.makeDcTracker(this);
        this.mSST.registerForNetworkAttached(this, 19, null);
        logd("GsmCdmaPhone: constructor: sub = " + this.mPhoneId);
    }

    private void initOnce(CommandsInterface ci) {
        if (ci instanceof SimulatedRadioControl) {
            this.mSimulatedRadioControl = (SimulatedRadioControl) ci;
        }
        this.mCT = this.mTelephonyComponentFactory.makeGsmCdmaCallTracker(this);
        this.mIccPhoneBookIntManager = this.mTelephonyComponentFactory.makeIccPhoneBookInterfaceManager(this);
        this.mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, LOG_TAG);
        this.mIccSmsInterfaceManager = this.mTelephonyComponentFactory.makeIccSmsInterfaceManager(this);
        this.mIccCardProxy = this.mTelephonyComponentFactory.makeIccCardProxy(this.mContext, this.mCi, this.mPhoneId);
        this.mSSReqDecisionMaker = new SSRequestDecisionMaker(this.mContext, this);
        if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
            try {
                this.mSupplementaryServiceExt = (ISupplementaryServiceExt) MPlugin.createInstance(ISupplementaryServiceExt.class.getName(), this.mContext);
                if (this.mSupplementaryServiceExt != null) {
                    this.mSupplementaryServiceExt.registerReceiver(this.mContext, this.mPhoneId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.mCi.registerForAvailable(this, 1, null);
        this.mCi.registerForOffOrNotAvailable(this, 8, null);
        this.mCi.registerForOn(this, 5, null);
        this.mCi.setOnSuppServiceNotification(this, 2, null);
        this.mCi.setOnUSSD(this, 7, null);
        this.mCi.setOnSs(this, 36, null);
        this.mCi.setOnCallRelatedSuppSvc(this, 1002, null);
        this.mCdmaSSM = this.mTelephonyComponentFactory.getCdmaSubscriptionSourceManagerInstance(this.mContext, this.mCi, this, 27, null);
        this.mEriManager = this.mTelephonyComponentFactory.makeEriManager(this, this.mContext, 0);
        this.mCi.setEmergencyCallbackMode(this, 25, null);
        this.mCi.registerForExitEmergencyCallbackMode(this, 26, null);
        this.mCarrierOtaSpNumSchema = TelephonyManager.from(this.mContext).getOtaSpNumberSchemaForPhone(getPhoneId(), UsimPBMemInfo.STRING_NOT_SET);
        this.mResetModemOnRadioTechnologyChange = SystemProperties.getBoolean("persist.radio.reset_on_switch", false);
        this.mCi.registerForRilConnected(this, 41, null);
        this.mCi.registerForVoiceRadioTechChanged(this, 39, null);
        TelephonyDevController telephonyDevController = this.mTelDevController;
        TelephonyDevController.registerRIL(this.mCi);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        filter.addAction("android.intent.action.ACTION_SUBINFO_RECORD_UPDATED");
        filter.addAction("com.android.ims.IMS_STATE_CHANGED");
        filter.addAction("android.intent.action.ACTION_SET_RADIO_CAPABILITY_DONE");
        filter.addAction("android.intent.action.AIRPLANE_MODE");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
    }

    private void initRatSpecific(int precisePhoneType) {
        this.mPendingMMIs.clear();
        this.mVmCount = 0;
        this.mEsn = null;
        this.mPrecisePhoneType = precisePhoneType;
        TelephonyManager tm = TelephonyManager.from(this.mContext);
        if (isPhoneTypeGsm()) {
            this.mCi.setPhoneType(1);
            tm.setPhoneType(getPhoneId(), 1);
            this.mIccCardProxy.setVoiceRadioTech(3);
            return;
        }
        this.mCdmaSubscriptionSource = -1;
        this.mIsPhoneInEcmState = TelephonyManager.getTelephonyProperty(this.mPhoneId, "ril.cdma.inecmmode", "false").equals("true");
        if (this.mIsPhoneInEcmState) {
            this.mCi.exitEmergencyCallbackMode(obtainMessage(26));
        }
        this.mCi.setPhoneType(2);
        tm.setPhoneType(getPhoneId(), 2);
        this.mIccCardProxy.setVoiceRadioTech(6);
        String operatorAlpha = SystemProperties.get("ro.cdma.home.operator.alpha");
        String operatorNumeric = SystemProperties.get(PROPERTY_CDMA_HOME_OPERATOR_NUMERIC);
        logd("init: operatorAlpha='" + operatorAlpha + "' operatorNumeric='" + operatorNumeric + "'");
        if (this.mUiccController.getUiccCardApplication(this.mPhoneId, 1) == null || isPhoneTypeCdmaLte()) {
            if (!TextUtils.isEmpty(operatorAlpha)) {
                logd("init: set 'gsm.sim.operator.alpha' to operator='" + operatorAlpha + "'");
                tm.setSimOperatorNameForPhone(this.mPhoneId, operatorAlpha);
            }
            if (!TextUtils.isEmpty(operatorNumeric)) {
                logd("init: set 'gsm.sim.operator.numeric' to operator='" + operatorNumeric + "'");
                logd("update icc_operator_numeric=" + operatorNumeric);
                tm.setSimOperatorNumericForPhone(this.mPhoneId, operatorNumeric);
                SubscriptionController.getInstance().setMccMnc(operatorNumeric, getSubId());
                setIsoCountryProperty(operatorNumeric);
                logd("update mccmnc=" + operatorNumeric);
                MccTable.updateMccMncConfiguration(this.mContext, operatorNumeric, false);
            }
        }
        updateCurrentCarrierInProvider(operatorNumeric);
    }

    private void setIsoCountryProperty(String operatorNumeric) {
        TelephonyManager tm = TelephonyManager.from(this.mContext);
        if (TextUtils.isEmpty(operatorNumeric)) {
            logd("setIsoCountryProperty: clear 'gsm.sim.operator.iso-country'");
            tm.setSimCountryIsoForPhone(this.mPhoneId, UsimPBMemInfo.STRING_NOT_SET);
            return;
        }
        String iso = UsimPBMemInfo.STRING_NOT_SET;
        try {
            iso = MccTable.countryCodeForMcc(Integer.parseInt(operatorNumeric.substring(0, 3)));
        } catch (NumberFormatException ex) {
            Rlog.e(LOG_TAG, "setIsoCountryProperty: countryCodeForMcc error", ex);
        } catch (StringIndexOutOfBoundsException ex2) {
            Rlog.e(LOG_TAG, "setIsoCountryProperty: countryCodeForMcc error", ex2);
        }
        logd("setIsoCountryProperty: set 'gsm.sim.operator.iso-country' to iso=" + iso);
        tm.setSimCountryIsoForPhone(this.mPhoneId, iso);
    }

    public boolean isPhoneTypeGsm() {
        return this.mPrecisePhoneType == 1;
    }

    public boolean isPhoneTypeCdma() {
        return this.mPrecisePhoneType == 2;
    }

    public boolean isPhoneTypeCdmaLte() {
        return this.mPrecisePhoneType == 6;
    }

    private void switchPhoneType(int precisePhoneType) {
        removeCallbacks(this.mExitEcmRunnable);
        initRatSpecific(precisePhoneType);
        this.mSST.updatePhoneType();
        setPhoneName(precisePhoneType == 1 ? "GSM" : "CDMA");
        onUpdateIccAvailability();
        this.mCT.updatePhoneType();
        RadioState radioState = this.mCi.getRadioState();
        if (radioState.isAvailable()) {
            handleRadioAvailable();
            if (radioState.isOn()) {
                handleRadioOn();
            }
        }
        if (!radioState.isAvailable() || !radioState.isOn()) {
            handleRadioOffOrNotAvailable();
        }
    }

    protected void finalize() {
        logd("GsmCdmaPhone finalized");
        if (this.mWakeLock.isHeld()) {
            Rlog.e(LOG_TAG, "UNEXPECTED; mWakeLock is held when finalizing.");
            this.mWakeLock.release();
        }
    }

    public ServiceState getServiceState() {
        if (this.mSST == null || (this.mSST.mSS.getState() != 0 && this.mSST.mSS.getDataRegState() == 0)) {
            Phone phone = this.mImsPhone;
            if (phone != null) {
                return ServiceState.mergeServiceStates(this.mSST == null ? new ServiceState() : this.mSST.mSS, phone.getServiceState());
            }
        }
        if (this.mSST != null) {
            return this.mSST.mSS;
        }
        return new ServiceState();
    }

    public CellLocation getCellLocation() {
        if (isPhoneTypeGsm()) {
            return this.mSST.getCellLocation();
        }
        CdmaCellLocation loc = this.mSST.mCellLoc;
        if (loc != null) {
            try {
                if (!(!loc.isEmpty() || this.mSST == null || this.mSST.mNewCellLoc == null)) {
                    CdmaCellLocation locNew = this.mSST.mNewCellLoc;
                    if (!(locNew == null || locNew.isEmpty())) {
                        loc = (CdmaCellLocation) this.mSST.mNewCellLoc;
                    }
                }
            } catch (Exception e) {
                Rlog.d("sms", "getCellLocation--error");
            }
        }
        if (Secure.getInt(getContext().getContentResolver(), "location_mode", 0) == 0) {
            CdmaCellLocation privateLoc = new CdmaCellLocation();
            privateLoc.setCellLocationData(loc.getBaseStationId(), Integer.MAX_VALUE, Integer.MAX_VALUE, loc.getSystemId(), loc.getNetworkId());
            loc = privateLoc;
        }
        return loc;
    }

    public PhoneConstants.State getState() {
        if (this.mImsPhone != null) {
            PhoneConstants.State imsState = this.mImsPhone.getState();
            if (imsState != PhoneConstants.State.IDLE) {
                return imsState;
            }
        }
        return this.mCT.mState;
    }

    public int getPhoneType() {
        if (this.mPrecisePhoneType == 1) {
            return 1;
        }
        return 2;
    }

    public ServiceStateTracker getServiceStateTracker() {
        return this.mSST;
    }

    public CallTracker getCallTracker() {
        return this.mCT;
    }

    public void updateVoiceMail() {
        if (isPhoneTypeGsm()) {
            int countVoiceMessages = 0;
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r != null) {
                countVoiceMessages = r.getVoiceMessageCount();
            }
            int countVoiceMessagesStored = getStoredVoiceMessageCount();
            if (countVoiceMessages == -1 && countVoiceMessagesStored != 0) {
                countVoiceMessages = countVoiceMessagesStored;
            }
            logd("updateVoiceMail countVoiceMessages = " + countVoiceMessages + " subId " + getSubId());
            setVoiceMessageCount(countVoiceMessages);
            return;
        }
        setVoiceMessageCount(getStoredVoiceMessageCount());
    }

    public List<? extends MmiCode> getPendingMmiCodes() {
        Rlog.d(LOG_TAG, "getPendingMmiCodes");
        dumpPendingMmi();
        ImsPhone imsPhone = this.mImsPhone;
        ArrayList<MmiCode> imsphonePendingMMIs = new ArrayList();
        if (imsPhone != null && imsPhone.getServiceState().getState() == 0) {
            for (ImsPhoneMmiCode mmi : imsPhone.getPendingMmiCodes()) {
                imsphonePendingMMIs.add(mmi);
            }
        }
        ArrayList<MmiCode> allPendingMMIs = new ArrayList(this.mPendingMMIs);
        allPendingMMIs.addAll(imsphonePendingMMIs);
        Rlog.d(LOG_TAG, "allPendingMMIs.size() = " + allPendingMMIs.size());
        int s = allPendingMMIs.size();
        for (int i = 0; i < s; i++) {
            Rlog.d(LOG_TAG, "dump allPendingMMIs: " + allPendingMMIs.get(i));
        }
        return allPendingMMIs;
    }

    public DataState getDataConnectionState(String apnType) {
        DataState ret = DataState.DISCONNECTED;
        if (this.mSST != null) {
            if (this.mSST.getCurrentDataConnectionState() != 0 && (isPhoneTypeCdma() || (isPhoneTypeGsm() && !apnType.equals("emergency")))) {
                logd("getDataConnectionState: dataConnectionState is not in service");
                if (SystemProperties.get("persist.mtk_ims_support").equals("1") && apnType.equals(ImsSwitchController.IMS_SERVICE)) {
                    switch (m18xf0fbc33d()[this.mDcTracker.getState(apnType).ordinal()]) {
                        case 1:
                            ret = DataState.CONNECTED;
                            break;
                        case 2:
                        case 7:
                            ret = DataState.CONNECTING;
                            break;
                        case 6:
                            logd("getDataConnectionState: apnType: " + apnType + " is in retrying state!! return connecting state");
                            ret = DataState.CONNECTING;
                            break;
                        default:
                            ret = DataState.DISCONNECTED;
                            break;
                    }
                }
                ret = DataState.DISCONNECTED;
            } else {
                switch (m18xf0fbc33d()[this.mDcTracker.getState(apnType).ordinal()]) {
                    case 1:
                    case 3:
                        if (this.mCT.mState == PhoneConstants.State.IDLE || this.mSST.isConcurrentVoiceAndDataAllowed()) {
                            ret = DataState.CONNECTED;
                        } else {
                            ret = DataState.SUSPENDED;
                        }
                        int phoneCount = TelephonyManager.getDefault().getPhoneCount();
                        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
                            int i = 0;
                            while (i < phoneCount) {
                                Phone pf = PhoneFactory.getPhone(i);
                                if (pf == null || i == getPhoneId() || pf.getState() == PhoneConstants.State.IDLE) {
                                    i++;
                                } else {
                                    logd("getDataConnectionState: Phone[" + getPhoneId() + "] Phone" + i + " is in call.");
                                    if (MTK_SVLTE_SUPPORT) {
                                        int phoneType = pf.getPhoneType();
                                        int rilRat = getServiceState().getRilDataRadioTechnology();
                                        logd("getDataConnectionState: SVLTE, phoneType: " + phoneType + " rilRat: " + rilRat);
                                        if (phoneType == 1 && ServiceState.isGsm(rilRat)) {
                                            ret = DataState.SUSPENDED;
                                        }
                                    } else {
                                        logd("getDataConnectionState: set Data state as SUSPENDED");
                                        ret = DataState.SUSPENDED;
                                    }
                                }
                            }
                        }
                        if (ret == DataState.CONNECTED && apnType == "default" && this.mDcTracker.getState(apnType) == State.DISCONNECTING && !this.mDcTracker.haveVsimIgnoreUserDataSetting() && !this.mDcTracker.getDataEnabled()) {
                            logd("getDataConnectionState: Connected but default data is not open.");
                            ret = DataState.DISCONNECTED;
                            break;
                        }
                        break;
                    case 2:
                    case 7:
                        ret = DataState.CONNECTING;
                        break;
                    case 4:
                    case 5:
                    case 6:
                        ret = DataState.DISCONNECTED;
                        break;
                }
            }
        }
        ret = DataState.DISCONNECTED;
        logd("getDataConnectionState apnType=" + apnType + " ret=" + ret);
        return ret;
    }

    public DataActivityState getDataActivityState() {
        DataActivityState ret = DataActivityState.NONE;
        if (this.mSST.getCurrentDataConnectionState() != 0) {
            return ret;
        }
        switch (m17xd0f730eb()[this.mDcTracker.getActivity().ordinal()]) {
            case 1:
                return DataActivityState.DATAIN;
            case 2:
                return DataActivityState.DATAINANDOUT;
            case 3:
                return DataActivityState.DATAOUT;
            case 4:
                return DataActivityState.DORMANT;
            default:
                return DataActivityState.NONE;
        }
    }

    public void notifyPhoneStateChanged() {
        this.mNotifier.notifyPhoneState(this);
    }

    public void notifyPreciseCallStateChanged() {
        super.notifyPreciseCallStateChangedP();
    }

    public void notifyNewRingingConnection(Connection c) {
        super.notifyNewRingingConnectionP(c);
    }

    public void notifyDisconnect(Connection cn) {
        this.mDisconnectRegistrants.notifyResult(cn);
        this.mNotifier.notifyDisconnectCause(cn.getDisconnectCause(), cn.getPreciseDisconnectCause());
    }

    public void notifyUnknownConnection(Connection cn) {
        super.notifyUnknownConnectionP(cn);
    }

    public boolean isInEmergencyCall() {
        if (isPhoneTypeGsm()) {
            return false;
        }
        return this.mCT.isInEmergencyCall();
    }

    protected void setIsInEmergencyCall() {
        if (!isPhoneTypeGsm()) {
            this.mCT.setIsInEmergencyCall();
        }
    }

    public boolean isInEcm() {
        if (isPhoneTypeGsm()) {
            return false;
        }
        return this.mIsPhoneInEcmState;
    }

    public void queryPhbStorageInfo(int type, Message response) {
        if (isPhoneTypeGsm()) {
            this.mCi.queryPhbStorageInfo(type, response);
            return;
        }
        IccFileHandler fh = getIccFileHandler();
        if (CsimPhbStorageInfo.hasModemPhbEnhanceCapability(fh)) {
            this.mCi.queryPhbStorageInfo(type, response);
        } else {
            CsimPhbStorageInfo.checkPhbRecordInfo(response);
        }
        Rlog.d(LOG_TAG, "queryPhbStorageInfo IccFileHandler" + fh);
    }

    private void sendEmergencyCallbackModeChange() {
        Intent intent = new Intent("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        intent.putExtra("phoneinECMState", this.mIsPhoneInEcmState);
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, getPhoneId());
        ActivityManagerNative.broadcastStickyIntent(intent, null, -1);
        logd("sendEmergencyCallbackModeChange");
    }

    public void sendEmergencyCallStateChange(boolean callActive) {
        if (this.mBroadcastEmergencyCallStateChanges) {
            Intent intent = new Intent("android.intent.action.EMERGENCY_CALL_STATE_CHANGED");
            intent.putExtra("phoneInEmergencyCall", callActive);
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, getPhoneId());
            ActivityManagerNative.broadcastStickyIntent(intent, null, -1);
            Rlog.d(LOG_TAG, "sendEmergencyCallStateChange: callActive " + callActive);
        }
    }

    public void setBroadcastEmergencyCallStateChanges(boolean broadcast) {
        this.mBroadcastEmergencyCallStateChanges = broadcast;
    }

    public void notifySuppServiceFailed(SuppService code) {
        this.mSuppServiceFailedRegistrants.notifyResult(code);
    }

    public void notifyServiceStateChanged(ServiceState ss) {
        super.notifyServiceStateChangedP(ss);
    }

    public void notifyLocationChanged() {
        this.mNotifier.notifyCellLocation(this);
    }

    public void notifyCallForwardingIndicator() {
        int simState = TelephonyManager.from(this.mContext).getSimState(this.mPhoneId);
        Rlog.d(LOG_TAG, "notifyCallForwardingIndicator: " + simState);
        if (simState == 5) {
            this.mNotifier.notifyCallForwardingChanged(this);
        }
    }

    public void setSystemProperty(String property, String value) {
        if (!getUnitTestMode()) {
            if (isPhoneTypeGsm() || isPhoneTypeCdmaLte()) {
                TelephonyManager.setTelephonyProperty(this.mPhoneId, property, value);
            } else {
                super.setSystemProperty(property, value);
            }
        }
    }

    public void registerForSuppServiceNotification(Handler h, int what, Object obj) {
        this.mSsnRegistrants.addUnique(h, what, obj);
        if (this.mCachedSsn != null) {
            this.mSsnRegistrants.notifyRegistrants(this.mCachedSsn);
            this.mCachedSsn = null;
        }
    }

    public void unregisterForSuppServiceNotification(Handler h) {
        this.mSsnRegistrants.remove(h);
        this.mCachedSsn = null;
    }

    public void registerForSimRecordsLoaded(Handler h, int what, Object obj) {
        this.mSimRecordsLoadedRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForSimRecordsLoaded(Handler h) {
        this.mSimRecordsLoadedRegistrants.remove(h);
    }

    public void acceptCall(int videoState) throws CallStateException {
        Phone imsPhone = this.mImsPhone;
        if (imsPhone == null || !imsPhone.getRingingCall().isRinging()) {
            this.mCT.acceptCall(videoState);
        } else {
            imsPhone.acceptCall(videoState);
        }
    }

    public void rejectCall() throws CallStateException {
        this.mCT.rejectCall();
    }

    public void switchHoldingAndActive() throws CallStateException {
        this.mCT.switchWaitingOrHoldingAndActive();
    }

    public String getIccSerialNumber() {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (!isPhoneTypeGsm() && r == null) {
            r = this.mUiccController.getIccRecords(this.mPhoneId, 1);
        }
        if (r != null) {
            return r.getIccId();
        }
        return null;
    }

    public String getFullIccSerialNumber() {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (!isPhoneTypeGsm() && r == null) {
            r = this.mUiccController.getIccRecords(this.mPhoneId, 1);
        }
        if (r != null) {
            return r.getFullIccId();
        }
        return null;
    }

    public boolean canConference() {
        if (this.mImsPhone != null && this.mImsPhone.canConference()) {
            return true;
        }
        if (isPhoneTypeGsm()) {
            return this.mCT.canConference();
        }
        loge("canConference: not possible in CDMA");
        return false;
    }

    public void conference() {
        if (this.mImsPhone == null || !this.mImsPhone.canConference()) {
            if (isPhoneTypeGsm()) {
                this.mCT.conference();
            } else {
                loge("conference: not possible in CDMA");
            }
            return;
        }
        logd("conference() - delegated to IMS phone");
        try {
            this.mImsPhone.conference();
        } catch (CallStateException e) {
            loge(e.toString());
        }
    }

    public void enableEnhancedVoicePrivacy(boolean enable, Message onComplete) {
        if (isPhoneTypeGsm()) {
            loge("enableEnhancedVoicePrivacy: not expected on GSM");
        } else {
            this.mCi.setPreferredVoicePrivacy(enable, onComplete);
        }
    }

    public void getEnhancedVoicePrivacy(Message onComplete) {
        if (isPhoneTypeGsm()) {
            loge("getEnhancedVoicePrivacy: not expected on GSM");
        } else {
            this.mCi.getPreferredVoicePrivacy(onComplete);
        }
    }

    public void clearDisconnected() {
        this.mCT.clearDisconnected();
    }

    public boolean canTransfer() {
        if (isPhoneTypeGsm()) {
            return this.mCT.canTransfer();
        }
        loge("canTransfer: not possible in CDMA");
        return false;
    }

    public void explicitCallTransfer() {
        if (isPhoneTypeGsm()) {
            this.mCT.explicitCallTransfer();
        } else {
            loge("explicitCallTransfer: not possible in CDMA");
        }
    }

    public GsmCdmaCall getForegroundCall() {
        return this.mCT.mForegroundCall;
    }

    public GsmCdmaCall getBackgroundCall() {
        return this.mCT.mBackgroundCall;
    }

    public Call getRingingCall() {
        Phone imsPhone = this.mImsPhone;
        if (imsPhone == null || !imsPhone.getRingingCall().isRinging()) {
            return this.mCT.mRingingCall;
        }
        return imsPhone.getRingingCall();
    }

    private boolean handleUdubIncallSupplementaryService(String dialString) {
        if (dialString.length() > 1) {
            return false;
        }
        if (!(getRingingCall().getState() == Call.State.IDLE && getBackgroundCall().getState() == Call.State.IDLE)) {
            Rlog.d(LOG_TAG, "MmiCode 0: hangupWaitingOrBackground");
            this.mCT.hangupWaitingOrBackground();
        }
        return true;
    }

    private boolean handleCallDeflectionIncallSupplementaryService(String dialString) {
        if (dialString.length() > 1) {
            return false;
        }
        if (getRingingCall().getState() != Call.State.IDLE) {
            logd("MmiCode 0: rejectCall");
            try {
                this.mCT.rejectCall();
            } catch (CallStateException e) {
                Rlog.d(LOG_TAG, "reject failed", e);
                notifySuppServiceFailed(SuppService.REJECT);
            }
        } else if (getBackgroundCall().getState() != Call.State.IDLE) {
            logd("MmiCode 0: hangupWaitingOrBackground");
            this.mCT.hangupWaitingOrBackground();
        }
        return true;
    }

    private boolean handleCallWaitingIncallSupplementaryService(String dialString) {
        int len = dialString.length();
        if (len > 2) {
            return false;
        }
        GsmCdmaCall call = getForegroundCall();
        if (len > 1) {
            try {
                int callIndex = dialString.charAt(1) - 48;
                if (callIndex >= 1 && callIndex <= 19) {
                    logd("MmiCode 1: hangupConnectionByIndex " + callIndex);
                    this.mCT.hangupConnectionByIndex(call, callIndex);
                }
            } catch (CallStateException e) {
                Rlog.d(LOG_TAG, "hangup failed", e);
                notifySuppServiceFailed(SuppService.HANGUP);
            }
        } else if (call.getState() != Call.State.IDLE) {
            logd("MmiCode 1: hangup foreground");
            this.mCT.hangup(call);
        } else {
            logd("MmiCode 1: switchWaitingOrHoldingAndActive");
            this.mCT.switchWaitingOrHoldingAndActive();
        }
        return true;
    }

    private boolean handleCallHoldIncallSupplementaryService(String dialString) {
        int len = dialString.length();
        if (len > 2) {
            return false;
        }
        GsmCdmaCall call = getForegroundCall();
        if (len > 1) {
            try {
                int callIndex = dialString.charAt(1) - 48;
                GsmCdmaConnection conn = this.mCT.getConnectionByIndex(call, callIndex);
                if (conn == null || callIndex < 1 || callIndex > 19) {
                    logd("separate: invalid call index " + callIndex);
                    notifySuppServiceFailed(SuppService.SEPARATE);
                } else {
                    logd("MmiCode 2: separate call " + callIndex);
                    this.mCT.separate(conn);
                }
            } catch (CallStateException e) {
                Rlog.d(LOG_TAG, "separate failed", e);
                notifySuppServiceFailed(SuppService.SEPARATE);
            }
        } else {
            try {
                if (getRingingCall().getState() != Call.State.IDLE) {
                    logd("MmiCode 2: accept ringing call");
                    this.mCT.acceptCall();
                } else {
                    logd("MmiCode 2: switchWaitingOrHoldingAndActive");
                    this.mCT.switchWaitingOrHoldingAndActive();
                }
            } catch (CallStateException e2) {
                Rlog.d(LOG_TAG, "switch failed", e2);
                notifySuppServiceFailed(SuppService.SWITCH);
            }
        }
        return true;
    }

    private boolean handleMultipartyIncallSupplementaryService(String dialString) {
        if (dialString.length() > 1) {
            return false;
        }
        logd("MmiCode 3: merge calls");
        conference();
        return true;
    }

    private boolean handleEctIncallSupplementaryService(String dialString) {
        if (dialString.length() != 1) {
            return false;
        }
        logd("MmiCode 4: explicit call transfer");
        explicitCallTransfer();
        return true;
    }

    private boolean handleCcbsIncallSupplementaryService(String dialString) {
        if (dialString.length() > 1) {
            return false;
        }
        Rlog.i(LOG_TAG, "MmiCode 5: CCBS not supported!");
        notifySuppServiceFailed(SuppService.UNKNOWN);
        return true;
    }

    public Call getCSRingingCall() {
        return this.mCT.mRingingCall;
    }

    boolean isInCSCall() {
        Call.State foregroundCallState = getForegroundCall().getState();
        Call.State backgroundCallState = getBackgroundCall().getState();
        Call.State ringingCallState = getCSRingingCall().getState();
        if (foregroundCallState.isAlive() || backgroundCallState.isAlive()) {
            return true;
        }
        return ringingCallState.isAlive();
    }

    public boolean handleInCallMmiCommands(String dialString) throws CallStateException {
        if (isPhoneTypeGsm()) {
            Phone imsPhone = this.mImsPhone;
            if (imsPhone != null && imsPhone.getServiceState().getState() == 0 && !isInCSCall()) {
                return imsPhone.handleInCallMmiCommands(dialString);
            }
            if (!isInCall() || TextUtils.isEmpty(dialString)) {
                return false;
            }
            boolean result = false;
            switch (dialString.charAt(0)) {
                case '0':
                    result = handleUdubIncallSupplementaryService(dialString);
                    break;
                case '1':
                    result = handleCallWaitingIncallSupplementaryService(dialString);
                    break;
                case '2':
                    result = handleCallHoldIncallSupplementaryService(dialString);
                    break;
                case '3':
                    result = handleMultipartyIncallSupplementaryService(dialString);
                    break;
                case '4':
                    result = handleEctIncallSupplementaryService(dialString);
                    break;
                case '5':
                    result = handleCcbsIncallSupplementaryService(dialString);
                    break;
            }
            return result;
        }
        loge("method handleInCallMmiCommands is NOT supported in CDMA!");
        return false;
    }

    public boolean isInCall() {
        Call.State foregroundCallState = getForegroundCall().getState();
        Call.State backgroundCallState = getBackgroundCall().getState();
        Call.State ringingCallState = getRingingCall().getState();
        if (foregroundCallState.isAlive() || backgroundCallState.isAlive()) {
            return true;
        }
        return ringingCallState.isAlive();
    }

    public Connection dial(String dialString, int videoState) throws CallStateException {
        return dial(dialString, null, videoState, null);
    }

    public Connection dial(String dialString, UUSInfo uusInfo, int videoState, Bundle intentExtras) throws CallStateException {
        if (!OemConstant.isCallOutEnable(this)) {
            Rlog.d(LOG_TAG, "ctmm vo block");
            return null;
        } else if (!OemConstant.isCallOutEnableExp(this) && !PhoneNumberUtils.isEmergencyNumber(dialString)) {
            Rlog.d(LOG_TAG, "th device lock block");
            return null;
        } else if (isPhoneTypeGsm() || uusInfo == null) {
            boolean isUt;
            Boolean valueOf;
            Integer valueOf2;
            boolean isEmergency = PhoneNumberUtils.isEmergencyNumber(dialString);
            Phone imsPhone = this.mImsPhone;
            CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
            boolean alwaysTryImsForEmergencyCarrierConfig = configManager.getConfigForSubId(getSubId()).getBoolean("carrier_use_ims_first_for_emergency_bool");
            boolean imsUseEnabled = (isImsUseEnabled() && imsPhone != null && (imsPhone.isVolteEnabled() || imsPhone.isWifiCallingEnabled() || (imsPhone.isVideoEnabled() && VideoProfile.isVideo(videoState)))) ? imsPhone.getServiceState().getState() == 0 : false;
            boolean useImsForEmergency = (imsPhone != null && isEmergency && alwaysTryImsForEmergencyCarrierConfig && ImsManager.isNonTtyOrTtyOnVolteEnabled(this.mContext)) ? imsPhone.getServiceState().getState() != 3 : false;
            SystemProperties.set("gsm.oppo.operator.ringtone", String.valueOf(configManager.getConfigForSubId(getSubId()).getInt("oppo.operator.ringtone", 0)));
            if (hasC2kOverImsModem()) {
                Rlog.d(LOG_TAG, "keep AOSP");
            } else if (!(useVzwLogic() || isPhoneTypeGsm())) {
                useImsForEmergency = false;
            }
            if (this.mPhoneId != getMainCapabilityPhoneId()) {
                useImsForEmergency = false;
            }
            if (OemConstant.EXP_VERSION) {
                int imsState;
                if (imsPhone != null) {
                    imsState = imsPhone.getServiceState().getState();
                } else {
                    imsState = 3;
                }
                useImsForEmergency = useImsForEmergency && imsState == 0;
            }
            String dialPart = PhoneNumberUtils.extractNetworkPortionAlt(PhoneNumberUtils.stripSeparators(dialString));
            if (dialPart.startsWith(CharacterSets.MIMENAME_ANY_CHARSET) || dialPart.startsWith("#")) {
                isUt = dialPart.endsWith("#");
            } else {
                isUt = false;
            }
            boolean useImsForUt = is93MDSupport() ? imsPhone != null ? imsPhone.isUtEnabled() : false : (imsPhone == null || !imsPhone.isUtEnabled()) ? false : !OperatorUtils.isNotSupportXcap(getOperatorNumeric());
            StringBuilder append = new StringBuilder().append("PhoneId = ").append(this.mPhoneId).append(", imsUseEnabled=").append(imsUseEnabled).append(", useImsForEmergency=").append(useImsForEmergency).append(", useImsForUt=").append(useImsForUt).append(", isUt=").append(isUt).append(", imsPhone=").append(imsPhone).append(", imsPhone.isVolteEnabled()=");
            if (imsPhone != null) {
                valueOf = Boolean.valueOf(imsPhone.isVolteEnabled());
            } else {
                valueOf = "N/A";
            }
            append = append.append(valueOf).append(", imsPhone.isVowifiEnabled()=");
            if (imsPhone != null) {
                valueOf = Boolean.valueOf(imsPhone.isWifiCallingEnabled());
            } else {
                valueOf = "N/A";
            }
            append = append.append(valueOf).append(", imsPhone.isVideoEnabled()=");
            if (imsPhone != null) {
                valueOf = Boolean.valueOf(imsPhone.isVideoEnabled());
            } else {
                valueOf = "N/A";
            }
            append = append.append(valueOf).append(", imsPhone.getServiceState().getState()=");
            if (imsPhone != null) {
                valueOf2 = Integer.valueOf(imsPhone.getServiceState().getState());
            } else {
                valueOf2 = "N/A";
            }
            logd(append.append(valueOf2).toString());
            Phone.checkWfcWifiOnlyModeBeforeDial(this.mImsPhone, this.mContext);
            Rlog.w(LOG_TAG, "IMS: imsphone = " + imsPhone + "isEmergencyNumber = " + isEmergency);
            if (imsPhone != null) {
                Rlog.w(LOG_TAG, "service state = " + imsPhone.getServiceState().getState());
            }
            if (!imsUseEnabled) {
                videoState = 0;
                Rlog.d(LOG_TAG, "Change video state to audio_only");
            }
            if (this.mCT.getState() != PhoneConstants.State.IDLE) {
                imsUseEnabled = false;
                useImsForEmergency = false;
                Rlog.d(LOG_TAG, "imsUseEnabled = false, useImsForEmergency = false, for gsm call exist ");
            }
            if ((imsUseEnabled && (!isUt || useImsForUt)) || useImsForEmergency) {
                if (isInCSCall()) {
                    Rlog.d(LOG_TAG, "has CS Call. Don't try IMS PS Call!");
                } else if (videoState == 0) {
                    try {
                        Rlog.d(LOG_TAG, "Trying IMS PS call");
                        return imsPhone.dial(dialString, uusInfo, videoState, intentExtras);
                    } catch (CallStateException e) {
                        logd("IMS PS call exception " + e + "imsUseEnabled =" + imsUseEnabled + ", imsPhone =" + imsPhone);
                        if (!Phone.CS_FALLBACK.equals(e.getMessage())) {
                            CallStateException ce = new CallStateException(e.getMessage());
                            ce.setStackTrace(e.getStackTrace());
                            throw ce;
                        }
                    }
                } else if (SystemProperties.get("persist.mtk_vilte_support").equals("1")) {
                    Rlog.d(LOG_TAG, "Trying IMS PS video call");
                    return imsPhone.dial(dialString, uusInfo, videoState, intentExtras);
                } else {
                    Rlog.d(LOG_TAG, "Trying (non-IMS) CS video call");
                    return dialInternal(dialString, uusInfo, videoState, intentExtras);
                }
            }
            if (SystemProperties.getInt("gsm.gcf.testmode", 0) == 2 || this.mSST == null || this.mSST.mSS.getState() != 1 || this.mSST.mSS.getDataRegState() == 0 || isEmergency) {
                logd("Trying (non-IMS) CS call");
                if (isPhoneTypeGsm()) {
                    return dialInternal(dialString, null, videoState, intentExtras);
                }
                return dialInternal(dialString, null, videoState, intentExtras);
            }
            throw new CallStateException("cannot dial in current state");
        } else {
            throw new CallStateException("Sending UUS information NOT supported in CDMA!");
        }
    }

    protected Connection dialInternal(String dialString, UUSInfo uusInfo, int videoState, Bundle intentExtras) throws CallStateException {
        String newDialString = dialString;
        if (!PhoneNumberUtils.isUriNumber(dialString)) {
            newDialString = PhoneNumberUtils.stripSeparators(dialString);
        }
        if (!isPhoneTypeGsm()) {
            return this.mCT.dial(newDialString);
        }
        if (handleInCallMmiCommands(newDialString)) {
            return null;
        }
        GsmMmiCode mmi = GsmMmiCode.newFromDialString(PhoneNumberUtils.extractNetworkPortionAlt(newDialString), this, (UiccCardApplication) this.mUiccApplication.get());
        logd("dialing w/ mmi '" + mmi + "'...");
        if (mmi == null) {
            if (videoState == 0) {
                return this.mCT.dial(newDialString, uusInfo, intentExtras);
            }
            if (is3GVTEnabled()) {
                return this.mCT.vtDial(newDialString, uusInfo, intentExtras);
            }
            throw new CallStateException("cannot vtDial for non-3GVT-capable device");
        } else if (mmi.isTemporaryModeCLIR()) {
            if (videoState == 0) {
                return this.mCT.dial(mmi.mDialingNumber, mmi.getCLIRMode(), uusInfo, intentExtras);
            }
            if (is3GVTEnabled()) {
                return this.mCT.vtDial(mmi.mDialingNumber, mmi.getCLIRMode(), uusInfo, intentExtras);
            }
            throw new CallStateException("cannot vtDial for non-3GVT-capable device");
        } else if (isDuringVoLteCall() || isDuringImsEccCall()) {
            Rlog.d(LOG_TAG, "Stop CS MMI during IMS Ecc Call or VoLTE call");
            throw new CallStateException("Stop CS MMI during IMS Ecc Call or VoLTE call");
        } else {
            this.mPendingMMIs.add(mmi);
            Rlog.d(LOG_TAG, "dialInternal: " + dialString + ", mmi=" + mmi);
            dumpPendingMmi();
            this.mMmiRegistrants.notifyRegistrants(new AsyncResult(null, mmi, null));
            try {
                mmi.processCode();
            } catch (CallStateException e) {
            }
            return null;
        }
    }

    public boolean handlePinMmi(String dialString) {
        MmiCode mmi;
        if (isPhoneTypeGsm()) {
            mmi = GsmMmiCode.newFromDialString(dialString, this, (UiccCardApplication) this.mUiccApplication.get());
        } else {
            mmi = CdmaMmiCode.newFromDialString(dialString, this, (UiccCardApplication) this.mUiccApplication.get());
        }
        if (mmi == null || !mmi.isPinPukCommand()) {
            loge("Mmi is null or unrecognized!");
            return false;
        }
        this.mPendingMMIs.add(mmi);
        Rlog.d(LOG_TAG, "handlePinMmi: " + dialString + ", mmi=" + mmi);
        dumpPendingMmi();
        this.mMmiRegistrants.notifyRegistrants(new AsyncResult(null, mmi, null));
        try {
            mmi.processCode();
        } catch (CallStateException e) {
        }
        return true;
    }

    public void sendUssdResponse(String ussdMessge) {
        if (isPhoneTypeGsm()) {
            GsmMmiCode mmi = GsmMmiCode.newFromUssdUserInput(ussdMessge, this, (UiccCardApplication) this.mUiccApplication.get());
            this.mPendingMMIs.add(mmi);
            Rlog.d(LOG_TAG, "sendUssdResponse: " + ussdMessge + ", mmi=" + mmi);
            dumpPendingMmi();
            this.mMmiRegistrants.notifyRegistrants(new AsyncResult(null, mmi, null));
            mmi.sendUssd(ussdMessge);
            return;
        }
        loge("sendUssdResponse: not possible in CDMA");
    }

    public void sendDtmf(char c) {
        if (!PhoneNumberUtils.is12Key(c)) {
            loge("sendDtmf called with invalid character '" + c + "'");
        } else if (this.mCT.mState == PhoneConstants.State.OFFHOOK) {
            this.mCi.sendDtmf(c, null);
        }
    }

    public void startDtmf(char c) {
        if (PhoneNumberUtils.is12Key(c)) {
            this.mCi.startDtmf(c, null);
        } else {
            loge("startDtmf called with invalid character '" + c + "'");
        }
    }

    public void stopDtmf() {
        this.mCi.stopDtmf(null);
    }

    public void sendBurstDtmf(String dtmfString, int on, int off, Message onComplete) {
        if (isPhoneTypeGsm()) {
            loge("[GsmCdmaPhone] sendBurstDtmf() is a CDMA method");
            return;
        }
        boolean check = true;
        for (int itr = 0; itr < dtmfString.length(); itr++) {
            if (!PhoneNumberUtils.is12Key(dtmfString.charAt(itr))) {
                Rlog.e(LOG_TAG, "sendDtmf called with invalid character '" + dtmfString.charAt(itr) + "'");
                check = false;
                break;
            }
        }
        if (this.mCT.mState == PhoneConstants.State.OFFHOOK && check) {
            this.mCi.sendBurstDtmf(dtmfString, on, off, onComplete);
        }
    }

    public void setRadioPower(boolean power) {
        this.mSST.setRadioPower(power);
    }

    private void storeVoiceMailNumber(String number) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        if (isPhoneTypeGsm()) {
            editor.putString(VM_NUMBER + getPhoneId(), number);
            editor.apply();
            setVmSimImsi(getSubscriberId());
            return;
        }
        editor.putString(VM_NUMBER_CDMA + getPhoneId(), number);
        editor.apply();
    }

    public String getVoiceMailNumber() {
        String number;
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            number = r != null ? r.getVoiceMailNumber() : UsimPBMemInfo.STRING_NOT_SET;
            if (TextUtils.isEmpty(number)) {
                number = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(VM_NUMBER + getPhoneId(), null);
            }
        } else {
            number = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(VM_NUMBER_CDMA + getPhoneId(), null);
        }
        if (TextUtils.isEmpty(number)) {
            String[] listArray = getContext().getResources().getStringArray(17236037);
            if (listArray != null && listArray.length > 0) {
                for (int i = 0; i < listArray.length; i++) {
                    if (!TextUtils.isEmpty(listArray[i])) {
                        String[] defaultVMNumberArray = listArray[i].split(";");
                        if (defaultVMNumberArray != null && defaultVMNumberArray.length > 0) {
                            if (defaultVMNumberArray.length != 1) {
                                if (defaultVMNumberArray.length == 2 && !TextUtils.isEmpty(defaultVMNumberArray[1]) && isMatchGid(defaultVMNumberArray[1])) {
                                    number = defaultVMNumberArray[0];
                                    break;
                                }
                            }
                            number = defaultVMNumberArray[0];
                        }
                    }
                }
            }
        }
        if (!isPhoneTypeGsm() && TextUtils.isEmpty(number) && getContext().getResources().getBoolean(17956963)) {
            return getLine1Number();
        }
        return number;
    }

    private String getVmSimImsi() {
        return PreferenceManager.getDefaultSharedPreferences(getContext()).getString(VM_SIM_IMSI + getPhoneId(), null);
    }

    private void setVmSimImsi(String imsi) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        editor.putString(VM_SIM_IMSI + getPhoneId(), imsi);
        editor.apply();
    }

    public String getVoiceMailAlphaTag() {
        String ret = UsimPBMemInfo.STRING_NOT_SET;
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            ret = r != null ? r.getVoiceMailAlphaTag() : UsimPBMemInfo.STRING_NOT_SET;
        }
        if (ret == null || ret.length() == 0) {
            return this.mContext.getText(17039364).toString();
        }
        return ret;
    }

    public String getDeviceId() {
        if (isPhoneTypeGsm()) {
            return this.mImei;
        }
        if (((CarrierConfigManager) this.mContext.getSystemService("carrier_config")).getConfigForSubId(getSubId()).getBoolean("force_imei_bool")) {
            return this.mImei;
        }
        if (getLteOnCdmaMode() == 1) {
            Rlog.d(LOG_TAG, "getDeviceId() in LTE_ON_CDMA_TRUE : return Imei");
            return getImei();
        }
        String id = getMeid();
        if (id == null || id.matches("^0*$")) {
            loge("getDeviceId(): MEID is not initialized use ESN");
            id = getEsn();
        }
        return id;
    }

    public int isDeviceIdAbnormal() {
        return this.mDeviceIdAbnormal;
    }

    public void setDeviceIdAbnormal(int abnormal) {
        this.mDeviceIdAbnormal = abnormal;
    }

    public String getDeviceSvn() {
        if (isPhoneTypeGsm() || isPhoneTypeCdmaLte()) {
            return this.mImeiSv;
        }
        loge("getDeviceSvn(): return 0");
        return "0";
    }

    public IsimRecords getIsimRecords() {
        return this.mIsimUiccRecords;
    }

    public String getImei() {
        return this.mImei;
    }

    public String getEsn() {
        if (!isPhoneTypeGsm()) {
            return this.mEsn;
        }
        loge("[GsmCdmaPhone] getEsn() is a CDMA method");
        return "0";
    }

    public String getMeid() {
        return this.mMeid;
    }

    public String getNai() {
        IccRecords r = this.mUiccController.getIccRecords(this.mPhoneId, 2);
        if (Log.isLoggable(LOG_TAG, 2)) {
            Rlog.v(LOG_TAG, "IccRecords is " + r);
        }
        if (r != null) {
            return r.getNAI();
        }
        return null;
    }

    public String getSubscriberId() {
        String str = null;
        IccRecords r;
        if (isPhoneTypeGsm()) {
            r = (IccRecords) this.mIccRecords.get();
            if (r != null) {
                str = r.getIMSI();
            }
            return str;
        } else if (isPhoneTypeCdma()) {
            logd("getSubscriberId, phone type is CDMA Imsi = " + this.mSST.getImsi());
            return this.mSST.getImsi();
        } else {
            r = (IccRecords) this.mIccRecords.get();
            if (this.mSimRecords != null) {
                str = this.mSimRecords.getIMSI();
            } else if (r != null) {
                str = r.getIMSI();
            }
            return str;
        }
    }

    public String getGroupIdLevel1() {
        String str = null;
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r != null) {
                str = r.getGid1();
            }
            return str;
        } else if (isPhoneTypeCdma()) {
            loge("GID1 is not available in CDMA");
            return null;
        } else {
            return this.mSimRecords != null ? this.mSimRecords.getGid1() : UsimPBMemInfo.STRING_NOT_SET;
        }
    }

    public String getGroupIdLevel2() {
        String str = null;
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r != null) {
                str = r.getGid2();
            }
            return str;
        } else if (isPhoneTypeCdma()) {
            loge("GID2 is not available in CDMA");
            return null;
        } else {
            return this.mSimRecords != null ? this.mSimRecords.getGid2() : UsimPBMemInfo.STRING_NOT_SET;
        }
    }

    public String getLine1Number() {
        String str = null;
        if (!isPhoneTypeGsm()) {
            return this.mSST.getMdnNumber();
        }
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r != null) {
            str = r.getMsisdnNumber();
        }
        return str;
    }

    public String getCdmaPrlVersion() {
        return this.mSST.getPrlVersion();
    }

    public String getCdmaMin() {
        return this.mSST.getCdmaMin();
    }

    public boolean isMinInfoReady() {
        return this.mSST.isMinInfoReady();
    }

    public String getMsisdn() {
        String str = null;
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r != null) {
                str = r.getMsisdnNumber();
            }
            return str;
        } else if (isPhoneTypeCdmaLte()) {
            if (this.mSimRecords != null) {
                str = this.mSimRecords.getMsisdnNumber();
            }
            return str;
        } else {
            loge("getMsisdn: not expected on CDMA");
            return null;
        }
    }

    public String getLine1AlphaTag() {
        String str = null;
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r != null) {
                str = r.getMsisdnAlphaTag();
            }
            return str;
        }
        loge("getLine1AlphaTag: not possible in CDMA");
        return null;
    }

    public boolean setLine1Number(String alphaTag, String number, Message onComplete) {
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r == null) {
                return false;
            }
            r.setMsisdnNumber(alphaTag, number, onComplete);
            return true;
        }
        loge("setLine1Number: not possible in CDMA");
        return false;
    }

    public void setVoiceMailNumber(String alphaTag, String voiceMailNumber, Message onComplete) {
        this.mVmNumber = voiceMailNumber;
        Message resp = obtainMessage(20, 0, 0, onComplete);
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r != null) {
            r.setVoiceMailNumber(alphaTag, this.mVmNumber, resp);
        }
    }

    private boolean isValidCommandInterfaceCFReason(int commandInterfaceCFReason) {
        switch (commandInterfaceCFReason) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                return true;
            default:
                return false;
        }
    }

    public String getSystemProperty(String property, String defValue) {
        if (!isPhoneTypeGsm() && !isPhoneTypeCdmaLte()) {
            return super.getSystemProperty(property, defValue);
        }
        if (getUnitTestMode()) {
            return null;
        }
        return TelephonyManager.getTelephonyProperty(this.mPhoneId, property, defValue);
    }

    private boolean isValidCommandInterfaceCFAction(int commandInterfaceCFAction) {
        switch (commandInterfaceCFAction) {
            case 0:
            case 1:
            case 3:
            case 4:
                return true;
            default:
                return false;
        }
    }

    private boolean isCfEnable(int action) {
        return action == 1 || action == 3;
    }

    public void getCallForwardingOption(int commandInterfaceCFReason, Message onComplete) {
        if (isPhoneTypeGsm() || isGsmSsPrefer()) {
            Phone imsPhone = this.mImsPhone;
            Message resp;
            if (is93MDSupport()) {
                Rlog.d(LOG_TAG, "getCallForwardingOption enter, CFReason:" + commandInterfaceCFReason);
                if (imsPhone == null || !(imsPhone.getServiceState().getState() == 0 || imsPhone.isUtEnabled())) {
                    if (isValidCommandInterfaceCFReason(commandInterfaceCFReason)) {
                        logd("requesting call forwarding query.");
                        if (commandInterfaceCFReason == 0) {
                            resp = obtainMessage(13, onComplete);
                        } else {
                            resp = onComplete;
                        }
                        this.mCi.queryCallForwardStatus(commandInterfaceCFReason, 0, null, resp);
                    }
                    return;
                }
                if (isOpReregisterForCF() && onComplete != null && onComplete.arg2 == 1) {
                    Rlog.d(LOG_TAG, "Set ims dereg to ON.");
                    SystemProperties.set(IMS_DEREG_PROP, "1");
                }
                imsPhone.getCallForwardingOption(commandInterfaceCFReason, onComplete);
            } else if (getCsFallbackStatus() == 0 && imsPhone != null && ((imsPhone.getServiceState().getState() == 0 || imsPhone.isUtEnabled()) && (imsPhone.isVolteEnabled() || (imsPhone.isWifiCallingEnabled() && isWFCUtSupport())))) {
                SuppSrvRequest ss = SuppSrvRequest.obtain(12, onComplete);
                ss.mParcel.writeInt(commandInterfaceCFReason);
                ss.mParcel.writeInt(1);
                Message imsUtResult = obtainMessage(ServiceStateTracker.NITZ_UPDATE_DIFF_DEFAULT, ss);
                if (isOpReregisterForCF() && onComplete.arg2 == 1) {
                    Rlog.d(LOG_TAG, "Set ims dereg to ON.");
                    SystemProperties.set(IMS_DEREG_PROP, "1");
                }
                imsPhone.getCallForwardingOption(commandInterfaceCFReason, imsUtResult);
            } else if (isValidCommandInterfaceCFReason(commandInterfaceCFReason)) {
                logd("requesting call forwarding query.");
                if (commandInterfaceCFReason == 0) {
                    resp = obtainMessage(13, onComplete);
                } else {
                    resp = onComplete;
                }
                if (getCsFallbackStatus() == 0 && isGsmUtSupport()) {
                    this.mSSReqDecisionMaker.queryCallForwardStatus(commandInterfaceCFReason, 0, null, resp);
                    return;
                }
                if (getCsFallbackStatus() == 1) {
                    setCsFallbackStatus(0);
                }
                if ((isDuringVoLteCall() || isDuringImsEccCall()) && onComplete != null) {
                    sendErrorResponse(onComplete, Error.GENERIC_FAILURE);
                } else if (isNotSupportUtToCS()) {
                    sendErrorResponse(onComplete, Error.UT_XCAP_403_FORBIDDEN);
                } else {
                    Rlog.d(LOG_TAG, "mCi.queryCallForwardStatus.");
                    this.mCi.queryCallForwardStatus(commandInterfaceCFReason, 0, null, resp);
                }
            }
        } else {
            loge("getCallForwardingOption: not possible in CDMA");
        }
    }

    public void getCallForwardingOptionForServiceClass(int commandInterfaceCFReason, int serviceClass, Message onComplete) {
        ImsPhone imsPhone = this.mImsPhone;
        Message resp;
        if (is93MDSupport()) {
            Rlog.d(LOG_TAG, "getCallForwardingOptionForServiceClass enter, CFReason:" + commandInterfaceCFReason + ", serviceClass:" + serviceClass);
            if (isValidCommandInterfaceCFReason(commandInterfaceCFReason)) {
                logd("requesting call forwarding query for service class.");
                if (commandInterfaceCFReason == 0) {
                    resp = obtainMessage(13, onComplete);
                } else {
                    resp = onComplete;
                }
                this.mCi.queryCallForwardStatus(commandInterfaceCFReason, serviceClass, null, resp);
            }
        } else if (getCsFallbackStatus() == 0 && imsPhone != null && imsPhone.getServiceState().getState() == 0) {
            SuppSrvRequest ss = SuppSrvRequest.obtain(12, onComplete);
            ss.mParcel.writeInt(commandInterfaceCFReason);
            ss.mParcel.writeInt(serviceClass);
            Message imsUtResult = obtainMessage(ServiceStateTracker.NITZ_UPDATE_DIFF_DEFAULT, ss);
            setServiceClass(serviceClass);
            imsPhone.getCallForwardingOption(commandInterfaceCFReason, imsUtResult);
        } else {
            if (isValidCommandInterfaceCFReason(commandInterfaceCFReason)) {
                Rlog.d(LOG_TAG, "requesting call forwarding query.");
                if (commandInterfaceCFReason == 0) {
                    resp = obtainMessage(13, onComplete);
                } else {
                    resp = onComplete;
                }
                if (getCsFallbackStatus() == 0 && isGsmUtSupport()) {
                    this.mSSReqDecisionMaker.queryCallForwardStatus(commandInterfaceCFReason, serviceClass, null, resp);
                    return;
                }
                if (getCsFallbackStatus() == 1) {
                    setCsFallbackStatus(0);
                }
                this.mCi.queryCallForwardStatus(commandInterfaceCFReason, serviceClass, null, resp);
            }
        }
    }

    public void setCallForwardingOption(int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int timerSeconds, Message onComplete) {
        if (isPhoneTypeGsm() || isGsmSsPrefer()) {
            Phone imsPhone = this.mImsPhone;
            int origUtCfuMode;
            String utCfuMode;
            Message resp;
            if (is93MDSupport()) {
                Rlog.d(LOG_TAG, "setCallForwardingOption enter, CFAction:" + commandInterfaceCFAction + ", CFReason:" + commandInterfaceCFReason + ", dialingNumber:" + dialingNumber + ", timerSeconds:" + timerSeconds);
                if (imsPhone == null || !(imsPhone.getServiceState().getState() == 0 || imsPhone.isUtEnabled())) {
                    if (isValidCommandInterfaceCFAction(commandInterfaceCFAction) && isValidCommandInterfaceCFReason(commandInterfaceCFReason)) {
                        if (commandInterfaceCFReason == 0) {
                            origUtCfuMode = 0;
                            utCfuMode = getSystemProperty("persist.radio.ut.cfu.mode", "disabled_ut_cfu_mode");
                            if ("enabled_ut_cfu_mode_on".equals(utCfuMode)) {
                                origUtCfuMode = 1;
                            } else if ("enabled_ut_cfu_mode_off".equals(utCfuMode)) {
                                origUtCfuMode = 2;
                            }
                            setSystemProperty("persist.radio.ut.cfu.mode", "disabled_ut_cfu_mode");
                            resp = obtainMessage(12, isCfEnable(commandInterfaceCFAction) ? 1 : 0, origUtCfuMode, new Cfu(dialingNumber, onComplete));
                        } else {
                            resp = onComplete;
                        }
                        this.mCi.setCallForward(commandInterfaceCFAction, commandInterfaceCFReason, 1, dialingNumber, timerSeconds, resp);
                    }
                    return;
                }
                imsPhone.setCallForwardingOption(commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, timerSeconds, onComplete);
            } else if (getCsFallbackStatus() == 0 && imsPhone != null && ((imsPhone.getServiceState().getState() == 0 || imsPhone.isUtEnabled()) && (imsPhone.isVolteEnabled() || (imsPhone.isWifiCallingEnabled() && isWFCUtSupport())))) {
                SuppSrvRequest ss = SuppSrvRequest.obtain(11, onComplete);
                ss.mParcel.writeInt(commandInterfaceCFAction);
                ss.mParcel.writeInt(commandInterfaceCFReason);
                ss.mParcel.writeString(dialingNumber);
                ss.mParcel.writeInt(timerSeconds);
                ss.mParcel.writeInt(1);
                imsPhone.setCallForwardingOption(commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, timerSeconds, obtainMessage(ServiceStateTracker.NITZ_UPDATE_DIFF_DEFAULT, ss));
            } else if (isValidCommandInterfaceCFAction(commandInterfaceCFAction) && isValidCommandInterfaceCFReason(commandInterfaceCFReason)) {
                if (commandInterfaceCFReason == 0) {
                    origUtCfuMode = 0;
                    utCfuMode = getSystemProperty("persist.radio.ut.cfu.mode", "disabled_ut_cfu_mode");
                    if ("enabled_ut_cfu_mode_on".equals(utCfuMode)) {
                        origUtCfuMode = 1;
                    } else if ("enabled_ut_cfu_mode_off".equals(utCfuMode)) {
                        origUtCfuMode = 2;
                    }
                    setSystemProperty("persist.radio.ut.cfu.mode", "disabled_ut_cfu_mode");
                    resp = obtainMessage(12, isCfEnable(commandInterfaceCFAction) ? 1 : 0, origUtCfuMode, new Cfu(dialingNumber, onComplete));
                } else {
                    resp = onComplete;
                }
                if (getCsFallbackStatus() == 0 && isGsmUtSupport()) {
                    this.mSSReqDecisionMaker.setCallForward(commandInterfaceCFAction, commandInterfaceCFReason, 1, dialingNumber, timerSeconds, resp);
                    return;
                }
                if (getCsFallbackStatus() == 1) {
                    setCsFallbackStatus(0);
                }
                if (isNotSupportUtToCS()) {
                    sendErrorResponse(onComplete, Error.UT_XCAP_403_FORBIDDEN);
                } else if ((isDuringVoLteCall() || isDuringImsEccCall()) && onComplete != null) {
                    sendErrorResponse(onComplete, Error.GENERIC_FAILURE);
                } else {
                    this.mCi.setCallForward(commandInterfaceCFAction, commandInterfaceCFReason, 1, dialingNumber, timerSeconds, resp);
                }
            }
        } else {
            loge("setCallForwardingOption: not possible in CDMA");
        }
    }

    public void setCallForwardingOptionForServiceClass(int commandInterfaceCFReason, int commandInterfaceCFAction, String dialingNumber, int timerSeconds, int serviceClass, Message onComplete) {
        ImsPhone imsPhone = this.mImsPhone;
        int origUtCfuMode;
        String utCfuMode;
        Message resp;
        if (is93MDSupport()) {
            Rlog.d(LOG_TAG, "setCallForwardingOptionForServiceClass enter, CFAction:" + commandInterfaceCFAction + ", CFReason:" + commandInterfaceCFReason + ", dialingNumber:" + dialingNumber + ", timerSeconds:" + timerSeconds + ", serviceClass:" + serviceClass);
            if (isValidCommandInterfaceCFAction(commandInterfaceCFAction) && isValidCommandInterfaceCFReason(commandInterfaceCFReason)) {
                if (commandInterfaceCFReason == 0) {
                    origUtCfuMode = 0;
                    utCfuMode = getSystemProperty("persist.radio.ut.cfu.mode", "disabled_ut_cfu_mode");
                    if ("enabled_ut_cfu_mode_on".equals(utCfuMode)) {
                        origUtCfuMode = 1;
                    } else if ("enabled_ut_cfu_mode_off".equals(utCfuMode)) {
                        origUtCfuMode = 2;
                    }
                    setSystemProperty("persist.radio.ut.cfu.mode", "disabled_ut_cfu_mode");
                    resp = obtainMessage(12, isCfEnable(commandInterfaceCFAction) ? 1 : 0, origUtCfuMode, new Cfu(dialingNumber, onComplete));
                } else {
                    resp = onComplete;
                }
                this.mCi.setCallForward(commandInterfaceCFAction, commandInterfaceCFReason, serviceClass, dialingNumber, timerSeconds, resp);
            }
        } else if (getCsFallbackStatus() == 0 && imsPhone != null && imsPhone.getServiceState().getState() == 0) {
            SuppSrvRequest ss = SuppSrvRequest.obtain(11, onComplete);
            ss.mParcel.writeInt(commandInterfaceCFAction);
            ss.mParcel.writeInt(commandInterfaceCFReason);
            ss.mParcel.writeString(dialingNumber);
            ss.mParcel.writeInt(timerSeconds);
            ss.mParcel.writeInt(serviceClass);
            imsPhone.setCallForwardingOption(commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, serviceClass, timerSeconds, obtainMessage(ServiceStateTracker.NITZ_UPDATE_DIFF_DEFAULT, ss));
        } else {
            if (isValidCommandInterfaceCFAction(commandInterfaceCFAction) && isValidCommandInterfaceCFReason(commandInterfaceCFReason)) {
                if (commandInterfaceCFReason == 0) {
                    origUtCfuMode = 0;
                    utCfuMode = getSystemProperty("persist.radio.ut.cfu.mode", "disabled_ut_cfu_mode");
                    if ("enabled_ut_cfu_mode_on".equals(utCfuMode)) {
                        origUtCfuMode = 1;
                    } else if ("enabled_ut_cfu_mode_off".equals(utCfuMode)) {
                        origUtCfuMode = 2;
                    }
                    setSystemProperty("persist.radio.ut.cfu.mode", "disabled_ut_cfu_mode");
                    resp = obtainMessage(12, isCfEnable(commandInterfaceCFAction) ? 1 : 0, origUtCfuMode, new Cfu(dialingNumber, onComplete));
                } else {
                    resp = onComplete;
                }
                if (getCsFallbackStatus() == 0 && isGsmUtSupport()) {
                    this.mSSReqDecisionMaker.setCallForward(commandInterfaceCFAction, commandInterfaceCFReason, serviceClass, dialingNumber, timerSeconds, resp);
                    return;
                }
                if (getCsFallbackStatus() == 1) {
                    setCsFallbackStatus(0);
                }
                this.mCi.setCallForward(commandInterfaceCFAction, commandInterfaceCFReason, serviceClass, dialingNumber, timerSeconds, resp);
            }
        }
    }

    public int[] getSavedClirSetting() {
        int presentationMode;
        int getClirResult;
        int clirSetting = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(Phone.CLIR_KEY + getPhoneId(), -1);
        if (clirSetting == 0 || clirSetting == -1) {
            presentationMode = 4;
            getClirResult = 0;
        } else if (clirSetting == 1) {
            presentationMode = 3;
            getClirResult = 1;
        } else {
            presentationMode = 4;
            getClirResult = 2;
        }
        int[] getClirResponse = new int[2];
        getClirResponse[0] = getClirResult;
        getClirResponse[1] = presentationMode;
        Rlog.d(LOG_TAG, "getClirResult: " + getClirResult);
        Rlog.d(LOG_TAG, "presentationMode: " + presentationMode);
        return getClirResponse;
    }

    public void getOutgoingCallerIdDisplay(Message onComplete) {
        if (isPhoneTypeGsm() || isGsmSsPrefer()) {
            Phone imsPhone = this.mImsPhone;
            int[] result;
            if (is93MDSupport()) {
                Rlog.d(LOG_TAG, "getOutgoingCallerIdDisplay enter");
                Message resp = obtainMessage(2004, onComplete);
                if (imsPhone == null || imsPhone.getServiceState().getState() != 0) {
                    this.mCi.getCLIR(resp);
                } else {
                    imsPhone.getOutgoingCallerIdDisplay(resp);
                }
            } else if (getCsFallbackStatus() == 0 && imsPhone != null && imsPhone.getServiceState().getState() == 0 && (imsPhone.isVolteEnabled() || (imsPhone.isWifiCallingEnabled() && isWFCUtSupport()))) {
                if (isOpNotSupportCallIdentity()) {
                    sendErrorResponse(onComplete, Error.SPECAIL_UT_COMMAND_NOT_SUPPORTED);
                    return;
                }
                result = getSavedClirSetting();
                if (result[0] == 0) {
                    Rlog.d(LOG_TAG, "CLIR DEFAULT, so return DEFAULT directly.");
                    if (onComplete != null) {
                        AsyncResult.forMessage(onComplete, result, null);
                        onComplete.sendToTarget();
                    }
                } else if (isOpTbClir()) {
                    if (onComplete != null) {
                        AsyncResult.forMessage(onComplete, result, null);
                        onComplete.sendToTarget();
                    }
                } else {
                    imsPhone.getOutgoingCallerIdDisplay(obtainMessage(ServiceStateTracker.NITZ_UPDATE_DIFF_DEFAULT, SuppSrvRequest.obtain(4, onComplete)));
                }
            } else if (getCsFallbackStatus() == 0 && isGsmUtSupport()) {
                result = getSavedClirSetting();
                if (result[0] == 0 && !isOpNotSupportCallIdentity()) {
                    Rlog.d(LOG_TAG, "CLIR DEFAULT, so return DEFAULT directly.");
                    if (onComplete != null) {
                        AsyncResult.forMessage(onComplete, result, null);
                        onComplete.sendToTarget();
                    }
                } else if (isOpTbClir()) {
                    if (onComplete != null) {
                        AsyncResult.forMessage(onComplete, result, null);
                        onComplete.sendToTarget();
                    }
                } else {
                    this.mSSReqDecisionMaker.getCLIR(onComplete);
                }
            } else {
                if (getCsFallbackStatus() == 1) {
                    setCsFallbackStatus(0);
                }
                if (isNotSupportUtToCS()) {
                    sendErrorResponse(onComplete, Error.UT_XCAP_403_FORBIDDEN);
                } else if ((isDuringVoLteCall() || isDuringImsEccCall()) && onComplete != null) {
                    sendErrorResponse(onComplete, Error.GENERIC_FAILURE);
                } else {
                    this.mCi.getCLIR(onComplete);
                }
            }
        } else {
            loge("getOutgoingCallerIdDisplay: not possible in CDMA");
        }
    }

    public void setOutgoingCallerIdDisplay(int commandInterfaceCLIRMode, Message onComplete) {
        if (isPhoneTypeGsm() || isGsmSsPrefer()) {
            Phone imsPhone = this.mImsPhone;
            if (is93MDSupport()) {
                Rlog.d(LOG_TAG, "setOutgoingCallerIdDisplay enter, CLIRmode:" + commandInterfaceCLIRMode);
                Message resp = obtainMessage(18, commandInterfaceCLIRMode, 0, onComplete);
                if (imsPhone == null || imsPhone.getServiceState().getState() != 0) {
                    this.mCi.setCLIR(commandInterfaceCLIRMode, resp);
                    return;
                } else {
                    imsPhone.setOutgoingCallerIdDisplay(commandInterfaceCLIRMode, resp);
                    return;
                }
            } else if (getCsFallbackStatus() == 0 && imsPhone != null && imsPhone.getServiceState().getState() == 0 && (imsPhone.isVolteEnabled() || (imsPhone.isWifiCallingEnabled() && isWFCUtSupport()))) {
                if (isOpNotSupportCallIdentity()) {
                    sendErrorResponse(onComplete, Error.SPECAIL_UT_COMMAND_NOT_SUPPORTED);
                    return;
                } else if (!isOpTbClir()) {
                    SuppSrvRequest ss = SuppSrvRequest.obtain(3, onComplete);
                    ss.mParcel.writeInt(commandInterfaceCLIRMode);
                    imsPhone.setOutgoingCallerIdDisplay(commandInterfaceCLIRMode, obtainMessage(ServiceStateTracker.NITZ_UPDATE_DIFF_DEFAULT, ss));
                    return;
                } else if ((isDuringVoLteCall() || isDuringImsEccCall()) && onComplete != null) {
                    sendErrorResponse(onComplete, Error.GENERIC_FAILURE);
                    return;
                } else {
                    this.mCi.setCLIR(commandInterfaceCLIRMode, obtainMessage(18, commandInterfaceCLIRMode, 0, onComplete));
                    return;
                }
            } else if (getCsFallbackStatus() != 0 || !isGsmUtSupport()) {
                if (getCsFallbackStatus() == 1) {
                    setCsFallbackStatus(0);
                }
                if (isNotSupportUtToCS()) {
                    sendErrorResponse(onComplete, Error.UT_XCAP_403_FORBIDDEN);
                    return;
                } else if ((isDuringVoLteCall() || isDuringImsEccCall()) && onComplete != null) {
                    sendErrorResponse(onComplete, Error.GENERIC_FAILURE);
                    return;
                } else {
                    this.mCi.setCLIR(commandInterfaceCLIRMode, obtainMessage(18, commandInterfaceCLIRMode, 0, onComplete));
                }
            } else if (!isOpTbClir()) {
                this.mSSReqDecisionMaker.setCLIR(commandInterfaceCLIRMode, obtainMessage(18, commandInterfaceCLIRMode, 0, onComplete));
                return;
            } else if ((isDuringVoLteCall() || isDuringImsEccCall()) && onComplete != null) {
                sendErrorResponse(onComplete, Error.GENERIC_FAILURE);
                return;
            } else {
                this.mCi.setCLIR(commandInterfaceCLIRMode, obtainMessage(18, commandInterfaceCLIRMode, 0, onComplete));
                return;
            }
        }
        loge("setOutgoingCallerIdDisplay: not possible in CDMA");
    }

    private void initTbcwMode() {
        if (this.mTbcwMode == 0) {
            if (isOpTbcwWithCS(getPhoneId())) {
                setTbcwMode(4);
                setTbcwToEnabledOnIfDisabled();
            } else if (!isUsimCard()) {
                setTbcwMode(3);
                setSystemProperty("persist.radio.terminal-based.cw", "disabled_tbcw");
            }
        }
        Rlog.d(LOG_TAG, "initTbcwMode: " + this.mTbcwMode);
    }

    public int getTbcwMode() {
        if (this.mTbcwMode == 0) {
            initTbcwMode();
        }
        return this.mTbcwMode;
    }

    public void setTbcwMode(int newMode) {
        Rlog.d(LOG_TAG, "Set tbcwmode: " + newMode);
        this.mTbcwMode = newMode;
    }

    public void setTbcwToEnabledOnIfDisabled() {
        if ("disabled_tbcw".equals(getSystemProperty("persist.radio.terminal-based.cw", "disabled_tbcw"))) {
            setSystemProperty("persist.radio.terminal-based.cw", "enabled_tbcw_on");
        }
    }

    public void getTerminalBasedCallWaiting(Message onComplete) {
        String tbcwMode = getSystemProperty("persist.radio.terminal-based.cw", "disabled_tbcw");
        Rlog.d(LOG_TAG, "getTerminalBasedCallWaiting(): tbcwMode = " + tbcwMode + ", onComplete = " + onComplete);
        int[] cwInfos;
        if ("enabled_tbcw_on".equals(tbcwMode)) {
            if (onComplete != null) {
                cwInfos = new int[2];
                cwInfos[0] = 1;
                cwInfos[1] = 1;
                AsyncResult.forMessage(onComplete, cwInfos, null);
                onComplete.sendToTarget();
            }
        } else if ("enabled_tbcw_off".equals(tbcwMode)) {
            if (onComplete != null) {
                cwInfos = new int[2];
                cwInfos[0] = 0;
                AsyncResult.forMessage(onComplete, cwInfos, null);
                onComplete.sendToTarget();
            }
        } else {
            Rlog.e(LOG_TAG, "getTerminalBasedCallWaiting(): ERROR: tbcwMode = " + tbcwMode);
        }
    }

    public void getCallWaiting(Message onComplete) {
        if (isPhoneTypeGsm() || isGsmSsPrefer()) {
            Phone imsPhone = this.mImsPhone;
            if (is93MDSupport()) {
                Rlog.d(LOG_TAG, "getCallWaiting enter");
                if (getSystemProperty(SS_CW_TBCW_EVER_ENABLE_PROP, "0").equals("1")) {
                    getTerminalBasedCallWaiting(onComplete);
                    return;
                } else if (imsPhone == null || !(imsPhone.getServiceState().getState() == 0 || imsPhone.isUtEnabled())) {
                    this.mCi.queryCallWaiting(0, onComplete);
                    return;
                } else {
                    imsPhone.getCallWaiting(onComplete);
                    return;
                }
            }
            if (!isOpNwCW()) {
                if (this.mTbcwMode == 0) {
                    initTbcwMode();
                }
                Rlog.d(LOG_TAG, "getCallWaiting(): mTbcwMode = " + this.mTbcwMode + ", onComplete = " + onComplete);
                if (this.mTbcwMode == 2) {
                    getTerminalBasedCallWaiting(onComplete);
                    return;
                } else if (this.mTbcwMode == 3) {
                    if ((isDuringVoLteCall() || isDuringImsEccCall()) && onComplete != null) {
                        sendErrorResponse(onComplete, Error.GENERIC_FAILURE);
                        return;
                    }
                    if (isNotSupportUtToCS()) {
                        sendErrorResponse(onComplete, Error.UT_XCAP_403_FORBIDDEN);
                    } else {
                        Rlog.d(LOG_TAG, "mCi.queryCallForwardStatus.");
                        this.mCi.queryCallWaiting(0, onComplete);
                    }
                    return;
                } else if (this.mTbcwMode == 4) {
                    if ((isDuringVoLteCall() || isDuringImsEccCall()) && onComplete != null) {
                        sendErrorResponse(onComplete, Error.GENERIC_FAILURE);
                        return;
                    }
                    this.mCi.queryCallWaiting(0, obtainMessage(301, onComplete));
                    return;
                }
            }
            if (getCsFallbackStatus() == 0 && imsPhone != null && ((imsPhone.getServiceState().getState() == 0 || imsPhone.isUtEnabled()) && (imsPhone.isVolteEnabled() || (imsPhone.isWifiCallingEnabled() && isWFCUtSupport())))) {
                if (isOpNwCW()) {
                    Rlog.d(LOG_TAG, "isOpNwCW(), getCallWaiting() by Ut interface");
                    imsPhone.getCallWaiting(obtainMessage(ServiceStateTracker.NITZ_UPDATE_DIFF_DEFAULT, SuppSrvRequest.obtain(14, onComplete)));
                } else {
                    Rlog.d(LOG_TAG, "isOpTbCW(), getTerminalBasedCallWaiting");
                    setTbcwMode(2);
                    setTbcwToEnabledOnIfDisabled();
                    getTerminalBasedCallWaiting(onComplete);
                }
            } else if (getCsFallbackStatus() == 0 && isGsmUtSupport()) {
                Rlog.d(LOG_TAG, "mSSReqDecisionMaker.queryCallWaiting");
                this.mSSReqDecisionMaker.queryCallWaiting(0, onComplete);
            } else {
                if (getCsFallbackStatus() == 1) {
                    setCsFallbackStatus(0);
                }
                if ((isDuringVoLteCall() || isDuringImsEccCall()) && onComplete != null) {
                    sendErrorResponse(onComplete, Error.GENERIC_FAILURE);
                } else if (isNotSupportUtToCS()) {
                    sendErrorResponse(onComplete, Error.UT_XCAP_403_FORBIDDEN);
                } else {
                    Rlog.d(LOG_TAG, "mCi.queryCallForwardStatus.");
                    this.mCi.queryCallWaiting(0, onComplete);
                }
            }
        } else {
            this.mCi.queryCallWaiting(1, onComplete);
        }
    }

    public void setTerminalBasedCallWaiting(boolean enable, Message onComplete) {
        String tbcwMode = getSystemProperty("persist.radio.terminal-based.cw", "disabled_tbcw");
        Rlog.d(LOG_TAG, "setTerminalBasedCallWaiting(): tbcwMode = " + tbcwMode + ", enable = " + enable);
        if ("enabled_tbcw_on".equals(tbcwMode)) {
            if (!enable) {
                setSystemProperty("persist.radio.terminal-based.cw", "enabled_tbcw_off");
            }
            if (onComplete != null) {
                AsyncResult.forMessage(onComplete, null, null);
                onComplete.sendToTarget();
            }
        } else if ("enabled_tbcw_off".equals(tbcwMode)) {
            if (enable) {
                setSystemProperty("persist.radio.terminal-based.cw", "enabled_tbcw_on");
            }
            if (onComplete != null) {
                AsyncResult.forMessage(onComplete, null, null);
                onComplete.sendToTarget();
            }
        } else {
            Rlog.e(LOG_TAG, "setTerminalBasedCallWaiting(): ERROR: tbcwMode = " + tbcwMode);
        }
    }

    public void setCallWaiting(boolean enable, Message onComplete) {
        if (isPhoneTypeGsm() || isGsmSsPrefer()) {
            Phone imsPhone = this.mImsPhone;
            if (is93MDSupport()) {
                Rlog.d(LOG_TAG, "setCallWaiting enter, enable:" + enable);
                if (getSystemProperty(SS_CW_TBCW_EVER_ENABLE_PROP, "0").equals("1")) {
                    setTerminalBasedCallWaiting(enable, onComplete);
                    return;
                } else if (imsPhone == null || !(imsPhone.getServiceState().getState() == 0 || imsPhone.isUtEnabled())) {
                    this.mCi.setCallWaiting(enable, 1, onComplete);
                    return;
                } else {
                    imsPhone.setCallWaiting(enable, onComplete);
                    return;
                }
            }
            if (!isOpNwCW()) {
                if (this.mTbcwMode == 0) {
                    initTbcwMode();
                }
                Rlog.d(LOG_TAG, "setCallWaiting(): mTbcwMode = " + this.mTbcwMode + ", onComplete = " + onComplete);
                if (this.mTbcwMode == 2) {
                    setTerminalBasedCallWaiting(enable, onComplete);
                    return;
                } else if (this.mTbcwMode == 3) {
                    if ((isDuringVoLteCall() || isDuringImsEccCall()) && onComplete != null) {
                        sendErrorResponse(onComplete, Error.GENERIC_FAILURE);
                        return;
                    }
                    if (isNotSupportUtToCS()) {
                        sendErrorResponse(onComplete, Error.UT_XCAP_403_FORBIDDEN);
                    } else {
                        this.mCi.setCallWaiting(enable, 1, onComplete);
                    }
                    return;
                } else if (this.mTbcwMode == 4) {
                    if ((isDuringVoLteCall() || isDuringImsEccCall()) && onComplete != null) {
                        sendErrorResponse(onComplete, Error.GENERIC_FAILURE);
                        return;
                    }
                    int i;
                    if (enable) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    this.mCi.setCallWaiting(enable, 1, obtainMessage(302, i, 0, onComplete));
                    return;
                }
            }
            if (getCsFallbackStatus() == 0 && imsPhone != null && ((imsPhone.getServiceState().getState() == 0 || imsPhone.isUtEnabled()) && (imsPhone.isVolteEnabled() || (imsPhone.isWifiCallingEnabled() && isWFCUtSupport())))) {
                if (isOpNwCW()) {
                    Rlog.d(LOG_TAG, "isOpNwCW(), setCallWaiting(): IMS in service");
                    SuppSrvRequest ss = SuppSrvRequest.obtain(13, onComplete);
                    ss.mParcel.writeInt(enable ? 1 : 0);
                    imsPhone.setCallWaiting(enable, obtainMessage(ServiceStateTracker.NITZ_UPDATE_DIFF_DEFAULT, ss));
                } else {
                    Rlog.d(LOG_TAG, "isOpTbCW(), setTerminalBasedCallWaiting(): IMS in service");
                    setTbcwMode(2);
                    setTbcwToEnabledOnIfDisabled();
                    setTerminalBasedCallWaiting(enable, onComplete);
                }
            } else if (getCsFallbackStatus() == 0 && isGsmUtSupport()) {
                Rlog.d(LOG_TAG, "mSSReqDecisionMaker.setCallWaiting");
                this.mSSReqDecisionMaker.setCallWaiting(enable, 1, onComplete);
            } else {
                if (getCsFallbackStatus() == 1) {
                    setCsFallbackStatus(0);
                }
                if ((isDuringVoLteCall() || isDuringImsEccCall()) && onComplete != null) {
                    sendErrorResponse(onComplete, Error.GENERIC_FAILURE);
                } else if (isNotSupportUtToCS()) {
                    sendErrorResponse(onComplete, Error.UT_XCAP_403_FORBIDDEN);
                } else {
                    this.mCi.setCallWaiting(enable, 1, onComplete);
                }
            }
        } else {
            loge("method setCallWaiting is NOT supported in CDMA!");
        }
    }

    public void getFacilityLock(String facility, String password, Message onComplete) {
        if (isPhoneTypeGsm() || isGsmSsPrefer()) {
            ImsPhone imsPhone = (ImsPhone) this.mImsPhone;
            CommandException checkError;
            if (is93MDSupport()) {
                Rlog.d(LOG_TAG, "getFacilityLock enter, facility:" + facility + ", password:" + password);
                Message resp = obtainMessage(2006, onComplete);
                if (imsPhone == null || imsPhone.getServiceState().getState() != 0) {
                    checkError = checkUiccApplicationForFacilityLock();
                    if (checkError == null || onComplete == null) {
                        this.mCi.queryFacilityLockForApp(facility, password, 1, ((UiccCardApplication) this.mUiccApplication.get()).getAid(), resp);
                        return;
                    }
                    sendErrorResponse(onComplete, checkError.getCommandError());
                    return;
                }
                imsPhone.getCallBarring(facility, resp);
                return;
            } else if (getCsFallbackStatus() == 0 && imsPhone != null && imsPhone.getServiceState().getState() == 0 && (imsPhone.isVolteEnabled() || (imsPhone.isWifiCallingEnabled() && isWFCUtSupport()))) {
                if (isOpNotSupportOCB(facility)) {
                    sendErrorResponse(onComplete, Error.SPECAIL_UT_COMMAND_NOT_SUPPORTED);
                    return;
                }
                SuppSrvRequest ss = SuppSrvRequest.obtain(10, onComplete);
                ss.mParcel.writeString(facility);
                ss.mParcel.writeString(password);
                ss.mParcel.writeInt(1);
                imsPhone.getCallBarring(facility, obtainMessage(ServiceStateTracker.NITZ_UPDATE_DIFF_DEFAULT, ss));
                return;
            } else if (getCsFallbackStatus() == 0 && isGsmUtSupport()) {
                this.mSSReqDecisionMaker.queryFacilityLock(facility, password, 1, onComplete);
                return;
            } else {
                if (getCsFallbackStatus() == 1) {
                    setCsFallbackStatus(0);
                }
                if (isNotSupportUtToCS()) {
                    sendErrorResponse(onComplete, Error.UT_XCAP_403_FORBIDDEN);
                    return;
                } else if ((isDuringVoLteCall() || isDuringImsEccCall()) && onComplete != null) {
                    sendErrorResponse(onComplete, Error.GENERIC_FAILURE);
                    return;
                } else {
                    checkError = checkUiccApplicationForFacilityLock();
                    if (checkError == null || onComplete == null) {
                        this.mCi.queryFacilityLockForApp(facility, password, 1, ((UiccCardApplication) this.mUiccApplication.get()).getAid(), onComplete);
                    } else {
                        sendErrorResponse(onComplete, checkError.getCommandError());
                        return;
                    }
                }
            }
        }
        loge("method getFacilityLock is NOT supported in CDMA!");
    }

    public void setFacilityLock(String facility, boolean enable, String password, Message onComplete) {
        if (isPhoneTypeGsm() || isGsmSsPrefer()) {
            ImsPhone imsPhone = (ImsPhone) this.mImsPhone;
            CommandException checkError;
            if (is93MDSupport()) {
                Rlog.d(LOG_TAG, "setFacilityLock enter, facility:" + facility + ", enable:" + enable + ", password:" + password);
                Message resp = obtainMessage(2005, onComplete);
                if (imsPhone == null || imsPhone.getServiceState().getState() != 0) {
                    checkError = checkUiccApplicationForFacilityLock();
                    if (checkError == null || onComplete == null) {
                        this.mCi.setFacilityLockForApp(facility, enable, password, 1, ((UiccCardApplication) this.mUiccApplication.get()).getAid(), resp);
                        return;
                    } else {
                        sendErrorResponse(onComplete, checkError.getCommandError());
                        return;
                    }
                }
                imsPhone.setCallBarring(facility, enable, password, resp);
                return;
            } else if (getCsFallbackStatus() == 0 && imsPhone != null && imsPhone.getServiceState().getState() == 0 && (imsPhone.isVolteEnabled() || (imsPhone.isWifiCallingEnabled() && isWFCUtSupport()))) {
                if (isOpNotSupportOCB(facility)) {
                    sendErrorResponse(onComplete, Error.SPECAIL_UT_COMMAND_NOT_SUPPORTED);
                    return;
                }
                SuppSrvRequest ss = SuppSrvRequest.obtain(9, onComplete);
                ss.mParcel.writeString(facility);
                ss.mParcel.writeInt(enable ? 1 : 0);
                ss.mParcel.writeString(password);
                ss.mParcel.writeInt(1);
                imsPhone.setCallBarring(facility, enable, password, obtainMessage(ServiceStateTracker.NITZ_UPDATE_DIFF_DEFAULT, ss));
                return;
            } else if (getCsFallbackStatus() == 0 && isGsmUtSupport()) {
                this.mSSReqDecisionMaker.setFacilityLock(facility, enable, password, 1, onComplete);
                return;
            } else {
                if (getCsFallbackStatus() == 1) {
                    setCsFallbackStatus(0);
                }
                if (isNotSupportUtToCS()) {
                    sendErrorResponse(onComplete, Error.UT_XCAP_403_FORBIDDEN);
                    return;
                } else if ((isDuringVoLteCall() || isDuringImsEccCall()) && onComplete != null) {
                    sendErrorResponse(onComplete, Error.GENERIC_FAILURE);
                    return;
                } else {
                    checkError = checkUiccApplicationForFacilityLock();
                    if (checkError == null || onComplete == null) {
                        this.mCi.setFacilityLockForApp(facility, enable, password, 1, ((UiccCardApplication) this.mUiccApplication.get()).getAid(), onComplete);
                    } else {
                        sendErrorResponse(onComplete, checkError.getCommandError());
                        return;
                    }
                }
            }
        }
        loge("method setFacilityLock is NOT supported in CDMA!");
    }

    public void getFacilityLockForServiceClass(String facility, String password, int serviceClass, Message onComplete) {
        ImsPhone imsPhone = (ImsPhone) this.mImsPhone;
        CommandException checkError;
        if (is93MDSupport()) {
            Rlog.d(LOG_TAG, "getFacilityLockForServiceClass enter, facility:" + facility + ", serviceClass:" + serviceClass + ", password:" + password);
            Message resp = obtainMessage(2006, onComplete);
            if (imsPhone == null || imsPhone.getServiceState().getState() != 0) {
                checkError = checkUiccApplicationForFacilityLock();
                if (checkError == null || onComplete == null) {
                    this.mCi.queryFacilityLockForApp(facility, password, serviceClass, ((UiccCardApplication) this.mUiccApplication.get()).getAid(), resp);
                    return;
                }
                sendErrorResponse(onComplete, checkError.getCommandError());
                return;
            }
            setServiceClass(serviceClass);
            imsPhone.getCallBarring(facility, resp);
        } else if (getCsFallbackStatus() == 0 && imsPhone != null && imsPhone.getServiceState().getState() == 0) {
            if (isOpNotSupportOCB(facility)) {
                sendErrorResponse(onComplete, Error.SPECAIL_UT_COMMAND_NOT_SUPPORTED);
                return;
            }
            SuppSrvRequest ss = SuppSrvRequest.obtain(10, onComplete);
            ss.mParcel.writeString(facility);
            ss.mParcel.writeString(password);
            ss.mParcel.writeInt(serviceClass);
            Message imsUtResult = obtainMessage(ServiceStateTracker.NITZ_UPDATE_DIFF_DEFAULT, ss);
            setServiceClass(serviceClass);
            imsPhone.getCallBarring(facility, imsUtResult);
        } else if (getCsFallbackStatus() == 0 && isGsmUtSupport()) {
            setServiceClass(serviceClass);
            this.mSSReqDecisionMaker.queryFacilityLock(facility, password, serviceClass, onComplete);
        } else {
            if (getCsFallbackStatus() == 1) {
                setCsFallbackStatus(0);
            }
            checkError = checkUiccApplicationForFacilityLock();
            if (checkError == null || onComplete == null) {
                this.mCi.queryFacilityLockForApp(facility, password, serviceClass, ((UiccCardApplication) this.mUiccApplication.get()).getAid(), onComplete);
                return;
            }
            sendErrorResponse(onComplete, checkError.getCommandError());
        }
    }

    public void setFacilityLockForServiceClass(String facility, boolean enable, String password, int serviceClass, Message onComplete) {
        ImsPhone imsPhone = (ImsPhone) this.mImsPhone;
        CommandException checkError;
        if (is93MDSupport()) {
            Rlog.d(LOG_TAG, "setFacilityLockForServiceClass enter, facility:" + facility + ", serviceClass:" + serviceClass + ", password:" + password + ", enable:" + enable);
            Message resp = obtainMessage(2005, onComplete);
            if (imsPhone == null || imsPhone.getServiceState().getState() != 0) {
                checkError = checkUiccApplicationForFacilityLock();
                if (checkError == null || onComplete == null) {
                    this.mCi.setFacilityLockForApp(facility, enable, password, serviceClass, ((UiccCardApplication) this.mUiccApplication.get()).getAid(), resp);
                    return;
                } else {
                    sendErrorResponse(onComplete, checkError.getCommandError());
                    return;
                }
            }
            setServiceClass(serviceClass);
            imsPhone.setCallBarring(facility, enable, password, resp);
        } else if (getCsFallbackStatus() == 0 && imsPhone != null && imsPhone.getServiceState().getState() == 0) {
            if (isOpNotSupportOCB(facility)) {
                sendErrorResponse(onComplete, Error.SPECAIL_UT_COMMAND_NOT_SUPPORTED);
                return;
            }
            SuppSrvRequest ss = SuppSrvRequest.obtain(9, onComplete);
            ss.mParcel.writeString(facility);
            ss.mParcel.writeInt(enable ? 1 : 0);
            ss.mParcel.writeString(password);
            ss.mParcel.writeInt(serviceClass);
            Message imsUtResult = obtainMessage(ServiceStateTracker.NITZ_UPDATE_DIFF_DEFAULT, ss);
            setServiceClass(serviceClass);
            imsPhone.setCallBarring(facility, enable, password, imsUtResult);
        } else if (getCsFallbackStatus() == 0 && isGsmUtSupport()) {
            setServiceClass(serviceClass);
            this.mSSReqDecisionMaker.setFacilityLock(facility, enable, password, serviceClass, onComplete);
        } else {
            if (getCsFallbackStatus() == 1) {
                setCsFallbackStatus(0);
            }
            checkError = checkUiccApplicationForFacilityLock();
            if (checkError == null || onComplete == null) {
                this.mCi.setFacilityLockForApp(facility, enable, password, serviceClass, ((UiccCardApplication) this.mUiccApplication.get()).getAid(), onComplete);
            } else {
                sendErrorResponse(onComplete, checkError.getCommandError());
            }
        }
    }

    public void changeBarringPassword(String facility, String oldPwd, String newPwd, Message onComplete) {
        if (!isPhoneTypeGsm()) {
            loge("method setFacilityLock is NOT supported in CDMA!");
        } else if (!isDuringImsCall()) {
            this.mCi.changeBarringPassword(facility, oldPwd, newPwd, onComplete);
        } else if (onComplete != null) {
            AsyncResult.forMessage(onComplete, null, new CommandException(Error.GENERIC_FAILURE));
            onComplete.sendToTarget();
        }
    }

    public void getCallForwardInTimeSlot(int commandInterfaceCFReason, Message onComplete) {
        if (isPhoneTypeGsm()) {
            ImsPhone imsPhone = this.mImsPhone;
            if (is93MDSupport()) {
                Rlog.d(LOG_TAG, "getCallForwardInTimeSlot enter, CFReason:" + commandInterfaceCFReason);
                if (!isOp(OPID.OP01)) {
                    sendErrorResponse(onComplete, Error.SPECAIL_UT_COMMAND_NOT_SUPPORTED);
                } else if (imsPhone == null || !(imsPhone.getServiceState().getState() == 0 || imsPhone.isUtEnabled())) {
                    if (commandInterfaceCFReason == 0) {
                        Rlog.d(LOG_TAG, "requesting call forwarding in time slot query.");
                        this.mCi.queryCallForwardInTimeSlotStatus(commandInterfaceCFReason, 0, obtainMessage(109, onComplete));
                    }
                } else {
                    imsPhone.getCallForwardInTimeSlot(commandInterfaceCFReason, onComplete);
                }
            } else if (getCsFallbackStatus() == 0 && isOp(OPID.OP01) && imsPhone != null && imsPhone.getServiceState().getState() == 0 && (imsPhone.isVolteEnabled() || (imsPhone.isWifiCallingEnabled() && isWFCUtSupport()))) {
                imsPhone.getCallForwardInTimeSlot(commandInterfaceCFReason, onComplete);
            } else if (commandInterfaceCFReason == 0) {
                Rlog.d(LOG_TAG, "requesting call forwarding in time slot query.");
                Message resp = obtainMessage(109, onComplete);
                if (getCsFallbackStatus() == 0 && isGsmUtSupport()) {
                    setSystemProperty("persist.radio.ut.cfu.mode", "disabled_ut_cfu_mode");
                    this.mSSReqDecisionMaker.queryCallForwardInTimeSlotStatus(commandInterfaceCFReason, 1, resp);
                } else {
                    sendErrorResponse(onComplete, Error.SPECAIL_UT_COMMAND_NOT_SUPPORTED);
                }
            } else if (onComplete != null) {
                sendErrorResponse(onComplete, Error.GENERIC_FAILURE);
            }
        } else {
            loge("method getCallForwardInTimeSlot is NOT supported in CDMA!");
        }
    }

    public void setCallForwardInTimeSlot(int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int timerSeconds, long[] timeSlot, Message onComplete) {
        if (isPhoneTypeGsm()) {
            ImsPhone imsPhone = this.mImsPhone;
            if (is93MDSupport()) {
                Rlog.d(LOG_TAG, "getCallForwardInTimeSlot enter, CFReason:" + commandInterfaceCFReason + ", CFAction:" + commandInterfaceCFAction + ", dialingNumber:" + dialingNumber + ", timerSeconds:" + timerSeconds);
                if (!isOp(OPID.OP01)) {
                    sendErrorResponse(onComplete, Error.SPECAIL_UT_COMMAND_NOT_SUPPORTED);
                    return;
                } else if (imsPhone == null || !(imsPhone.getServiceState().getState() == 0 || imsPhone.isUtEnabled())) {
                    if (isValidCommandInterfaceCFAction(commandInterfaceCFAction) && commandInterfaceCFReason == 0) {
                        this.mCi.setCallForwardInTimeSlot(commandInterfaceCFAction, commandInterfaceCFReason, 1, dialingNumber, timerSeconds, timeSlot, obtainMessage(110, isCfEnable(commandInterfaceCFAction) ? 1 : 0, 0, new CfuEx(dialingNumber, timeSlot, onComplete)));
                    }
                    return;
                } else {
                    imsPhone.setCallForwardInTimeSlot(commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, timerSeconds, timeSlot, onComplete);
                    return;
                }
            }
            if (getCsFallbackStatus() == 0) {
                if (isOp(OPID.OP01) && imsPhone != null && imsPhone.getServiceState().getState() == 0 && (imsPhone.isVolteEnabled() || (imsPhone.isWifiCallingEnabled() && isWFCUtSupport()))) {
                    SuppSrvRequest ss = SuppSrvRequest.obtain(17, onComplete);
                    ss.mParcel.writeInt(commandInterfaceCFAction);
                    ss.mParcel.writeInt(commandInterfaceCFReason);
                    ss.mParcel.writeString(dialingNumber);
                    ss.mParcel.writeInt(timerSeconds);
                    imsPhone.setCallForwardInTimeSlot(commandInterfaceCFAction, commandInterfaceCFReason, dialingNumber, timerSeconds, timeSlot, obtainMessage(ServiceStateTracker.NITZ_UPDATE_DIFF_DEFAULT, ss));
                    return;
                }
            }
            if (isValidCommandInterfaceCFAction(commandInterfaceCFAction) && commandInterfaceCFReason == 0) {
                Message resp = obtainMessage(110, isCfEnable(commandInterfaceCFAction) ? 1 : 0, 0, new CfuEx(dialingNumber, timeSlot, onComplete));
                if (getCsFallbackStatus() == 0 && isGsmUtSupport()) {
                    this.mSSReqDecisionMaker.setCallForwardInTimeSlot(commandInterfaceCFAction, commandInterfaceCFReason, 1, dialingNumber, timerSeconds, timeSlot, resp);
                } else {
                    sendErrorResponse(onComplete, Error.SPECAIL_UT_COMMAND_NOT_SUPPORTED);
                }
            } else {
                sendErrorResponse(onComplete, Error.GENERIC_FAILURE);
            }
        } else {
            loge("method setCallForwardInTimeSlot is NOT supported in CDMA!");
        }
    }

    private void handleCfuInTimeSlotQueryResult(CallForwardInfoEx[] infos) {
        boolean z = false;
        if (((IccRecords) this.mIccRecords.get()) == null) {
            return;
        }
        if (infos == null || infos.length == 0) {
            setVoiceCallForwardingFlag(1, false, null);
            setSystemProperty("persist.radio.ut.cfu.mode", "enabled_ut_cfu_mode_off");
            return;
        }
        int s = infos.length;
        for (int i = 0; i < s; i++) {
            if ((infos[i].serviceClass & 1) != 0) {
                if (infos[i].status == 1) {
                    z = true;
                }
                setVoiceCallForwardingFlag(1, z, infos[i].number);
                setSystemProperty("persist.radio.ut.cfu.mode", infos[i].status == 1 ? "enabled_ut_cfu_mode_on" : "enabled_ut_cfu_mode_off");
                saveTimeSlot(infos[i].timeSlot);
                return;
            }
        }
    }

    void sendErrorResponse(Message onComplete, Error error) {
        Rlog.d(LOG_TAG, "sendErrorResponse" + error);
        if (onComplete != null) {
            AsyncResult.forMessage(onComplete, null, new CommandException(error));
            onComplete.sendToTarget();
        }
    }

    public boolean queryCfuOrWait() {
        String oppositePropertyValue1 = SystemProperties.get(CFU_QUERY_PROPERTY_NAME + 99);
        String oppositePropertyValue2 = SystemProperties.get(CFU_QUERY_PROPERTY_NAME + 99);
        if (oppositePropertyValue1.equals("1") || oppositePropertyValue2.equals("1")) {
            sendMessageDelayed(obtainMessage(102), 1000);
            return false;
        }
        boolean bDataEnable = !getDataEnabled() ? this.mDcTracker.haveVsimIgnoreUserDataSetting() : true;
        Rlog.d(LOG_TAG, "bDataEnable: " + bDataEnable);
        if (isPhoneTypeGsm() || isGsmSsPrefer()) {
            Phone imsPhone;
            if (is93MDSupport()) {
                imsPhone = this.mImsPhone;
                if (imsPhone == null || !(imsPhone.getServiceState().getState() == 0 || imsPhone.isUtEnabled())) {
                    if (isValidCommandInterfaceCFReason(0)) {
                        logd("requesting call forwarding query, queryCfuOrWait().");
                        if (isOp(OPID.OP01)) {
                            this.mCi.queryCallForwardInTimeSlotStatus(0, 1, obtainMessage(109, 1, 0, null));
                        } else {
                            this.mCi.queryCallForwardStatus(0, 1, null, obtainMessage(13));
                        }
                    }
                    return true;
                }
                if (isOp(OPID.OP01)) {
                    imsPhone.getCallForwardInTimeSlot(0, obtainMessage(109, 1, 0, null));
                } else {
                    imsPhone.getCallForwardingOption(0, obtainMessage(13, null));
                }
                return true;
            } else if (!isIccCardMncMccAvailable(getPhoneId())) {
                return false;
            } else {
                if (getCsFallbackStatus() != 0 || !isGsmUtSupport() || !bDataEnable) {
                    imsPhone = this.mImsPhone;
                    if (getCsFallbackStatus() == 0 && imsPhone != null && imsPhone.getServiceState().getState() == 0 && !bDataEnable && isOp(OPID.OP01) && isOp(OPID.OP02)) {
                        Rlog.d(LOG_TAG, "No need query CFU in CS domain!");
                    } else if (!isNotSupportUtToCS()) {
                        if (getCsFallbackStatus() == 1) {
                            setCsFallbackStatus(0);
                        }
                        if (isDuringVoLteCall() || isDuringImsEccCall()) {
                            Rlog.i(LOG_TAG, "No need query CFU in CS domain!");
                        } else {
                            this.mCi.queryCallForwardStatus(0, 1, null, obtainMessage(13));
                        }
                    }
                } else if (isOp(OPID.OP01)) {
                    this.mSSReqDecisionMaker.queryCallForwardInTimeSlotStatus(0, 1, obtainMessage(109, 1, 0, null));
                } else {
                    this.mSSReqDecisionMaker.queryCallForwardStatus(0, 1, null, obtainMessage(13, null));
                }
            }
        }
        return true;
    }

    public SSRequestDecisionMaker getSSRequestDecisionMaker() {
        return this.mSSReqDecisionMaker;
    }

    public boolean isDuringImsCall() {
        if (this.mImsPhone != null) {
            boolean isDuringImsCall;
            Call.State foregroundCallState = this.mImsPhone.getForegroundCall().getState();
            Call.State backgroundCallState = this.mImsPhone.getBackgroundCall().getState();
            Call.State ringingCallState = this.mImsPhone.getRingingCall().getState();
            if (foregroundCallState.isAlive() || backgroundCallState.isAlive()) {
                isDuringImsCall = true;
            } else {
                isDuringImsCall = ringingCallState.isAlive();
            }
            if (isDuringImsCall) {
                Rlog.d(LOG_TAG, "During IMS call.");
                return true;
            }
        }
        return false;
    }

    public boolean isDuringVoLteCall() {
        boolean r = isDuringImsCall() ? this.mImsPhone != null ? this.mImsPhone.isVolteEnabled() : false : false;
        Rlog.d(LOG_TAG, "isDuringVoLteCall: " + r);
        return r;
    }

    public boolean isDuringImsEccCall() {
        boolean isInImsEccCall = this.mImsPhone != null ? this.mImsPhone.isInEmergencyCall() : false;
        Rlog.d(LOG_TAG, "isInImsEccCall: " + isInImsEccCall);
        return isInImsEccCall;
    }

    private void handleImsUtCsfb(Message msg) {
        SuppSrvRequest ss = msg.obj;
        if (ss == null) {
            Rlog.e(LOG_TAG, "handleImsUtCsfb: Error SuppSrvRequest null!");
        } else if (isDuringVoLteCall() || isDuringImsEccCall()) {
            Message resultCallback = ss.getResultCallback();
            if (resultCallback != null) {
                AsyncResult.forMessage(resultCallback, null, new CommandException(Error.GENERIC_FAILURE));
                resultCallback.sendToTarget();
            }
            if (getCsFallbackStatus() == 1) {
                setCsFallbackStatus(0);
            }
            ss.setResultCallback(null);
            ss.mParcel.recycle();
        } else {
            int requestCode = ss.getRequestCode();
            ss.mParcel.setDataPosition(0);
            switch (requestCode) {
                case 3:
                    Rlog.d(LOG_TAG, "handleImsUtCsfb: SUPP_SRV_REQ_SET_CLIR");
                    setOutgoingCallerIdDisplay(ss.mParcel.readInt(), ss.getResultCallback());
                    break;
                case 4:
                    Rlog.d(LOG_TAG, "handleImsUtCsfb: SUPP_SRV_REQ_GET_CLIR");
                    getOutgoingCallerIdDisplay(ss.getResultCallback());
                    break;
                case 9:
                    Rlog.d(LOG_TAG, "handleImsUtCsfb: SUPP_SRV_REQ_SET_CB");
                    setFacilityLockForServiceClass(ss.mParcel.readString(), ss.mParcel.readInt() != 0, ss.mParcel.readString(), ss.mParcel.readInt(), ss.getResultCallback());
                    break;
                case 10:
                    Rlog.d(LOG_TAG, "handleImsUtCsfb: SUPP_SRV_REQ_GET_CB");
                    getFacilityLockForServiceClass(ss.mParcel.readString(), ss.mParcel.readString(), ss.mParcel.readInt(), ss.getResultCallback());
                    break;
                case 11:
                    Rlog.d(LOG_TAG, "handleImsUtCsfb: SUPP_SRV_REQ_SET_CF");
                    setCallForwardingOptionForServiceClass(ss.mParcel.readInt(), ss.mParcel.readInt(), ss.mParcel.readString(), ss.mParcel.readInt(), ss.mParcel.readInt(), ss.getResultCallback());
                    break;
                case 12:
                    Rlog.d(LOG_TAG, "handleImsUtCsfb: SUPP_SRV_REQ_GET_CF");
                    getCallForwardingOptionForServiceClass(ss.mParcel.readInt(), ss.mParcel.readInt(), ss.getResultCallback());
                    break;
                case 13:
                    Rlog.d(LOG_TAG, "handleImsUtCsfb: SUPP_SRV_REQ_SET_CW");
                    setCallWaiting(ss.mParcel.readInt() != 0, ss.getResultCallback());
                    break;
                case 14:
                    Rlog.d(LOG_TAG, "handleImsUtCsfb: SUPP_SRV_REQ_GET_CW");
                    getCallWaiting(ss.getResultCallback());
                    break;
                case 15:
                    String dialString = ss.mParcel.readString();
                    Rlog.d(LOG_TAG, "handleImsUtCsfb: SUPP_SRV_REQ_MMI_CODE: dialString = " + dialString);
                    try {
                        dial(dialString, 0);
                        break;
                    } catch (CallStateException ex) {
                        Rlog.e(LOG_TAG, "handleImsUtCsfb: SUPP_SRV_REQ_MMI_CODE: CallStateException!");
                        ex.printStackTrace();
                        break;
                    }
                default:
                    Rlog.e(LOG_TAG, "handleImsUtCsfb: invalid requestCode = " + requestCode);
                    break;
            }
            ss.setResultCallback(null);
            ss.mParcel.recycle();
        }
    }

    private void handleUssiCsfb(String dialString) {
        Rlog.d(LOG_TAG, "handleUssiCsfb: dialString=" + dialString);
        try {
            dial(dialString, 0);
        } catch (CallStateException ex) {
            Rlog.e(LOG_TAG, "handleUssiCsfb: CallStateException!");
            ex.printStackTrace();
        }
    }

    public void getAvailableNetworks(Message response) {
        if (isPhoneTypeGsm() || (isPhoneTypeCdmaLte() && 11 == this.mCi.oppoGetPreferredNetworkType())) {
            this.mCi.getAvailableNetworks(response);
            return;
        }
        loge("getAvailableNetworks: not possible in CDMA");
        if (response != null) {
            AsyncResult.forMessage(response).exception = new CommandException(Error.REQUEST_NOT_SUPPORTED);
            response.sendToTarget();
        }
    }

    public void getNeighboringCids(Message response) {
        if (isPhoneTypeGsm()) {
            this.mCi.getNeighboringCids(response);
        } else if (response != null) {
            AsyncResult.forMessage(response).exception = new CommandException(Error.REQUEST_NOT_SUPPORTED);
            response.sendToTarget();
        }
    }

    public void setUiTTYMode(int uiTtyMode, Message onComplete) {
        if (this.mImsPhone != null) {
            this.mImsPhone.setUiTTYMode(uiTtyMode, onComplete);
        }
    }

    public void setMute(boolean muted) {
        this.mCT.setMute(muted);
    }

    public boolean getMute() {
        return this.mCT.getMute();
    }

    public void getDataCallList(Message response) {
        this.mCi.getDataCallList(response);
    }

    public void updateServiceLocation() {
        this.mSST.enableSingleLocationUpdate();
    }

    public void enableLocationUpdates() {
        this.mSST.enableLocationUpdates();
    }

    public void disableLocationUpdates() {
        this.mSST.disableLocationUpdates();
    }

    public boolean getDataRoamingEnabled() {
        return this.mDcTracker.getDataOnRoamingEnabled();
    }

    public void setDataRoamingEnabled(boolean enable) {
        this.mDcTracker.setDataOnRoamingEnabled(enable);
    }

    public void registerForCdmaOtaStatusChange(Handler h, int what, Object obj) {
        this.mCi.registerForCdmaOtaProvision(h, what, obj);
    }

    public void unregisterForCdmaOtaStatusChange(Handler h) {
        this.mCi.unregisterForCdmaOtaProvision(h);
    }

    public void registerForSubscriptionInfoReady(Handler h, int what, Object obj) {
        this.mSST.registerForSubscriptionInfoReady(h, what, obj);
    }

    public void unregisterForSubscriptionInfoReady(Handler h) {
        this.mSST.unregisterForSubscriptionInfoReady(h);
    }

    public void setOnEcbModeExitResponse(Handler h, int what, Object obj) {
        this.mEcmExitRespRegistrant = new Registrant(h, what, obj);
    }

    public void unsetOnEcbModeExitResponse(Handler h) {
        this.mEcmExitRespRegistrant.clear();
    }

    public void registerForCallWaiting(Handler h, int what, Object obj) {
        this.mCT.registerForCallWaiting(h, what, obj);
    }

    public void unregisterForCallWaiting(Handler h) {
        this.mCT.unregisterForCallWaiting(h);
    }

    public boolean getDataEnabled() {
        return this.mDcTracker.getDataEnabled();
    }

    public void setDataEnabled(boolean enable) {
        this.mDcTracker.setDataEnabled(enable);
    }

    public void onMMIDone(MmiCode mmi) {
        Rlog.d(LOG_TAG, "onMMIDone: " + mmi);
        dumpPendingMmi();
        if (!this.mPendingMMIs.remove(mmi)) {
            if (!isPhoneTypeGsm()) {
                return;
            }
            if (!(mmi.isUssdRequest() || ((GsmMmiCode) mmi).isSsInfo())) {
                return;
            }
        }
        this.mMmiCompleteRegistrants.notifyRegistrants(new AsyncResult(null, mmi, null));
    }

    public void onMMIDone(GsmMmiCode mmi, Object obj) {
        Rlog.d(LOG_TAG, "onMMIDone: " + mmi + ", obj=" + obj);
        dumpPendingMmi();
        if (this.mPendingMMIs.remove(mmi) || mmi.isUssdRequest() || mmi.isSsInfo()) {
            this.mMmiCompleteRegistrants.notifyRegistrants(new AsyncResult(obj, mmi, null));
        }
    }

    public void dumpPendingMmi() {
        int size = this.mPendingMMIs.size();
        if (size == 0) {
            Rlog.d(LOG_TAG, "dumpPendingMmi: none");
            return;
        }
        for (int i = 0; i < size; i++) {
            Rlog.d(LOG_TAG, "dumpPendingMmi: " + this.mPendingMMIs.get(i));
        }
    }

    private void onNetworkInitiatedUssd(MmiCode mmi) {
        Rlog.e(LOG_TAG, "onNetworkInitiatedUssd ... NetworkInitiatedUssd mmi = " + mmi);
        this.mMmiCompleteRegistrants.notifyRegistrants(new AsyncResult(null, mmi, null));
    }

    private void onIncomingUSSD(int ussdMode, String ussdMessage) {
        Rlog.e(LOG_TAG, "onIncomingUSSD ... ussdMode = " + ussdMode);
        if (ussdMessage != null) {
            Rlog.e(LOG_TAG, "onIncomingUSSD ... ussdMessage = " + ussdMessage);
        }
        if (!isPhoneTypeGsm()) {
            loge("onIncomingUSSD: not expected on GSM");
        }
        boolean isUssdRequest = ussdMode == 1;
        boolean isUssdError = ussdMode != 4 ? ussdMode == 5 : true;
        boolean isUssdhandleByStk = ussdMode == 3;
        boolean isUssdRelease = ussdMode == 2;
        GsmMmiCode found = null;
        Rlog.d(LOG_TAG, "USSD:mPendingMMIs= " + this.mPendingMMIs + " size=" + this.mPendingMMIs.size());
        int s = this.mPendingMMIs.size();
        for (int i = 0; i < s; i++) {
            Rlog.d(LOG_TAG, "i= " + i + " isPending=" + ((GsmMmiCode) this.mPendingMMIs.get(i)).isPendingUSSD());
            if (((GsmMmiCode) this.mPendingMMIs.get(i)).isPendingUSSD()) {
                found = (GsmMmiCode) this.mPendingMMIs.get(i);
                Rlog.d(LOG_TAG, "found = " + found);
                break;
            }
        }
        if (found != null) {
            Rlog.d(LOG_TAG, "setUserInitiatedMMI  TRUE");
            found.setUserInitiatedMMI(true);
            if (isUssdRelease && this.mIsNetworkInitiatedUssr) {
                Rlog.d(LOG_TAG, "onIncomingUSSD(): USSD_MODE_NW_RELEASE.");
                found.onUssdRelease(ussdMessage);
            } else if (isUssdError) {
                found.onUssdFinishedError();
            } else if (isUssdhandleByStk) {
                found.onUssdStkHandling(ussdMessage, isUssdRequest);
            } else {
                found.onUssdFinished(ussdMessage, isUssdRequest);
            }
        } else {
            if (isUssdRequest) {
                Rlog.d(LOG_TAG, "The default value of UserInitiatedMMI is FALSE");
                this.mIsNetworkInitiatedUssr = true;
                Rlog.d(LOG_TAG, "onIncomingUSSD(): Network Initialized USSD");
            }
            if (!isUssdError && ussdMessage != null) {
                onNetworkInitiatedUssd(GsmMmiCode.newNetworkInitiatedUssd(ussdMessage, isUssdRequest, this, (UiccCardApplication) this.mUiccApplication.get()));
            } else if (isUssdError) {
                onNetworkInitiatedUssd(GsmMmiCode.newNetworkInitiatedUssdError(ussdMessage, isUssdRequest, this, (UiccCardApplication) this.mUiccApplication.get()));
            }
        }
        if (isUssdRelease || isUssdError) {
            this.mIsNetworkInitiatedUssr = false;
        }
    }

    private void syncClirSetting() {
        int clirSetting = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(Phone.CLIR_KEY + getPhoneId(), -1);
        if (clirSetting >= 0) {
            this.mCi.setCLIR(clirSetting, null);
        }
    }

    private void handleRadioAvailable() {
        this.mCi.getBasebandVersion(obtainMessage(6));
        this.mCi.getDeviceIdentity(obtainMessage(21));
        this.mCi.getRadioCapability(obtainMessage(35));
        startLceAfterRadioIsAvailable();
    }

    private void handleRadioOn() {
        this.mCi.getVoiceRadioTechnology(obtainMessage(40));
        if (!isPhoneTypeGsm()) {
            this.mCdmaSubscriptionSource = this.mCdmaSSM.getCdmaSubscriptionSource();
        }
        setPreferredNetworkTypeIfSimLoaded();
    }

    private void handleRadioOffOrNotAvailable() {
        if (isPhoneTypeGsm()) {
            for (int i = this.mPendingMMIs.size() - 1; i >= 0; i--) {
                if (((GsmMmiCode) this.mPendingMMIs.get(i)).isPendingUSSD()) {
                    ((GsmMmiCode) this.mPendingMMIs.get(i)).onUssdFinishedError();
                }
            }
        }
        Phone imsPhone = this.mImsPhone;
        if (!(imsPhone == null || imsPhone.isWifiCallingEnabled())) {
            imsPhone.getServiceState().setState(1);
        }
        this.mRadioOffOrNotAvailableRegistrants.notifyRegistrants();
    }

    public void handleMessage(Message msg) {
        AsyncResult ar;
        CallForwardInfo[] cfinfo;
        int i;
        Message onComplete;
        CommandException cmdException;
        switch (msg.what) {
            case 1:
                handleRadioAvailable();
                return;
            case 2:
                logd("Event EVENT_SSN Received");
                if (isPhoneTypeGsm()) {
                    ar = (AsyncResult) msg.obj;
                    SuppServiceNotification not = ar.result;
                    if (this.mSsnRegistrants.size() == 0) {
                        this.mCachedSsn = ar;
                    }
                    this.mSsnRegistrants.notifyRegistrants(ar);
                    return;
                }
                return;
            case 3:
                if (isPhoneTypeGsm()) {
                    updateCurrentCarrierInProvider();
                    String imsi = getVmSimImsi();
                    String imsiFromSIM = getSubscriberId();
                    if (!(imsi == null || imsiFromSIM == null || imsiFromSIM.equals(imsi))) {
                        storeVoiceMailNumber(null);
                        setVmSimImsi(null);
                    }
                    logd("imsi = " + imsi + " imsiFromSIM = " + imsiFromSIM + " imsiFromSIM = " + imsiFromSIM);
                    if (imsi == null && imsiFromSIM != null && imsiFromSIM.startsWith("53024")) {
                        storeVoiceMailNumber("+64222022002");
                        setVmSimImsi(imsiFromSIM);
                    }
                    logd("notify call forward indication, phone id:" + this.mPhoneId);
                    notifyCallForwardingIndicator();
                }
                this.mSimRecordsLoadedRegistrants.notifyRegistrants();
                return;
            case 5:
                logd("Event EVENT_RADIO_ON Received");
                handleRadioOn();
                return;
            case 6:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    logd("Baseband version: " + ar.result);
                    TelephonyManager.from(this.mContext).setBasebandVersionForPhone(getPhoneId(), (String) ar.result);
                    return;
                }
                return;
            case 7:
                String[] ussdResult = (String[]) ((AsyncResult) msg.obj).result;
                if (ussdResult.length > 1) {
                    try {
                        onIncomingUSSD(Integer.parseInt(ussdResult[0]), ussdResult[1]);
                        return;
                    } catch (NumberFormatException e) {
                        Rlog.w(LOG_TAG, "error parsing USSD");
                        return;
                    }
                }
                return;
            case 8:
                logd("Event EVENT_RADIO_OFF_OR_NOT_AVAILABLE Received");
                handleRadioOffOrNotAvailable();
                return;
            case 9:
                ar = (AsyncResult) msg.obj;
                if (ar.exception != null) {
                    Rlog.e(LOG_TAG, "Invalid DeviceId (IMEI)");
                    setDeviceIdAbnormal(1);
                    return;
                }
                this.mImei = (String) ar.result;
                Rlog.d(LOG_TAG, "IMEI: ****" + this.mImei.substring(10));
                try {
                    Long.parseLong(this.mImei);
                    setDeviceIdAbnormal(0);
                    return;
                } catch (NumberFormatException e2) {
                    setDeviceIdAbnormal(1);
                    Rlog.e(LOG_TAG, "Invalid DeviceId (IMEI) Format: " + e2.toString() + ")");
                    return;
                }
            case 10:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    this.mImeiSv = (String) ar.result;
                    return;
                }
                return;
            case 12:
                IccRecords r;
                Cfu cfu;
                if (is93MDSupport()) {
                    ar = (AsyncResult) msg.obj;
                    r = (IccRecords) this.mIccRecords.get();
                    cfu = ar.userObj;
                    if (ar.exception == null && r != null) {
                        setVoiceCallForwardingFlag(1, msg.arg1 == 1, cfu.mSetCfNumber);
                        setSystemProperty("persist.radio.ut.cfu.mode", msg.arg1 == 1 ? "enabled_ut_cfu_mode_on" : "enabled_ut_cfu_mode_off");
                    }
                    if (cfu.mOnComplete != null) {
                        AsyncResult.forMessage(cfu.mOnComplete, ar.result, ar.exception);
                        cfu.mOnComplete.sendToTarget();
                        return;
                    }
                    return;
                }
                ar = (AsyncResult) msg.obj;
                r = (IccRecords) this.mIccRecords.get();
                cfu = (Cfu) ar.userObj;
                if (ar.exception == null && r != null) {
                    if (!queryCFUAgainAfterSet()) {
                        setVoiceCallForwardingFlag(1, msg.arg1 == 1, cfu.mSetCfNumber);
                    } else if (ar.result != null) {
                        cfinfo = ar.result;
                        if (cfinfo != null && cfinfo.length != 0) {
                            Rlog.d(LOG_TAG, "[EVENT_SET_CALL_FORWARD_DONE check cfinfo");
                            i = 0;
                            while (i < cfinfo.length) {
                                if ((cfinfo[i].serviceClass & 1) != 0) {
                                    setVoiceCallForwardingFlag(1, cfinfo[i].status == 1, cfinfo[i].number);
                                } else {
                                    i++;
                                }
                            }
                            break;
                        }
                        Rlog.d(LOG_TAG, "cfinfo is null or length is 0.");
                    } else {
                        Rlog.e(LOG_TAG, "EVENT_SET_CALL_FORWARD_DONE: ar.result is null.");
                    }
                    setSystemProperty("persist.radio.ut.cfu.mode", msg.arg1 == 1 ? "enabled_ut_cfu_mode_on" : "enabled_ut_cfu_mode_off");
                }
                if (!(ar.exception == null || msg.arg2 == 0)) {
                    if (msg.arg2 == 1) {
                        setSystemProperty("persist.radio.ut.cfu.mode", "enabled_ut_cfu_mode_on");
                    } else {
                        setSystemProperty("persist.radio.ut.cfu.mode", "enabled_ut_cfu_mode_off");
                    }
                }
                if (cfu.mOnComplete != null) {
                    AsyncResult.forMessage(cfu.mOnComplete, ar.result, ar.exception);
                    cfu.mOnComplete.sendToTarget();
                    return;
                }
                return;
            case 13:
                Rlog.d(LOG_TAG, "mPhoneId= " + this.mPhoneId + "subId=" + getSubId());
                setSystemProperty(CFU_QUERY_PROPERTY_NAME + this.mPhoneId, "0");
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null && (ar.result == null || (ar.result instanceof CallForwardInfo[]))) {
                    handleCfuQueryResult((CallForwardInfo[]) ar.result);
                }
                onComplete = (Message) ar.userObj;
                if (onComplete != null) {
                    AsyncResult.forMessage(onComplete, ar.result, ar.exception);
                    onComplete.sendToTarget();
                    return;
                }
                return;
            case 18:
                Rlog.d(LOG_TAG, "EVENT_SET_CLIR_COMPLETE");
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    saveClirSetting(msg.arg1);
                }
                if (ar.exception != null && (ar.exception instanceof CommandException)) {
                    cmdException = ar.exception;
                    Rlog.d(LOG_TAG, "EVENT_SET_CLIR_COMPLETE: cmdException error:" + cmdException.getCommandError());
                    if (is93MDSupport() && ((isOp(OPID.OP01) || isOp(OPID.OP02)) && cmdException != null)) {
                        if (isUtError(cmdException.getCommandError())) {
                            Rlog.d(LOG_TAG, "return SPECAIL_UT_COMMAND_NOT_SUPPORTED");
                            ar.exception = new CommandException(Error.SPECAIL_UT_COMMAND_NOT_SUPPORTED);
                        } else {
                            Rlog.d(LOG_TAG, "return Original Error");
                        }
                    }
                }
                onComplete = (Message) ar.userObj;
                if (onComplete != null) {
                    AsyncResult.forMessage(onComplete, ar.result, ar.exception);
                    onComplete.sendToTarget();
                    return;
                }
                return;
            case 19:
                logd("Event EVENT_REGISTERED_TO_NETWORK Received");
                if (isPhoneTypeGsm()) {
                    syncClirSetting();
                }
                if (isNotSupportUtToCS()) {
                    sendMessageDelayed(obtainMessage(2002), 10000);
                    return;
                } else {
                    sendMessage(obtainMessage(2002));
                    return;
                }
            case 20:
                ar = (AsyncResult) msg.obj;
                if ((isPhoneTypeGsm() && IccVmNotSupportedException.class.isInstance(ar.exception)) || (!isPhoneTypeGsm() && IccException.class.isInstance(ar.exception))) {
                    storeVoiceMailNumber(this.mVmNumber);
                    ar.exception = null;
                }
                onComplete = (Message) ar.userObj;
                if (onComplete != null) {
                    AsyncResult.forMessage(onComplete, ar.result, ar.exception);
                    onComplete.sendToTarget();
                    return;
                }
                return;
            case 21:
                ar = msg.obj;
                if (ar.exception != null) {
                    setDeviceIdAbnormal(1);
                    Rlog.e(LOG_TAG, "Invalid Device Id");
                    return;
                }
                String[] respId = (String[]) ar.result;
                this.mImei = respId[0];
                this.mImeiSv = respId[1];
                this.mEsn = respId[2];
                this.mMeid = respId[3];
                setDeviceIdAbnormal(0);
                return;
            case 22:
                logd("Event EVENT_RUIM_RECORDS_LOADED Received");
                updateCurrentCarrierInProvider();
                return;
            case 25:
                handleEnterEmergencyCallbackMode(msg);
                return;
            case 26:
                handleExitEmergencyCallbackMode(msg);
                return;
            case 27:
                logd("EVENT_CDMA_SUBSCRIPTION_SOURCE_CHANGED");
                this.mCdmaSubscriptionSource = this.mCdmaSSM.getCdmaSubscriptionSource();
                return;
            case 28:
                ar = (AsyncResult) msg.obj;
                if (this.mSST.mSS.getIsManualSelection()) {
                    setNetworkSelectionModeAutomatic((Message) ar.result);
                    logd("SET_NETWORK_SELECTION_AUTOMATIC: set to automatic");
                    return;
                }
                logd("SET_NETWORK_SELECTION_AUTOMATIC: already automatic, ignore");
                return;
            case 29:
                Rlog.d(LOG_TAG, "EVENT_ICC_RECORD_EVENTS");
                processIccRecordEvents(((Integer) ((AsyncResult) msg.obj).result).intValue());
                return;
            case 35:
                ar = (AsyncResult) msg.obj;
                RadioCapability rc = ar.result;
                if (ar.exception != null) {
                    Rlog.d(LOG_TAG, "get phone radio capability fail, no need to change mRadioCapability");
                } else {
                    radioCapabilityUpdated(rc);
                }
                Rlog.d(LOG_TAG, "EVENT_GET_RADIO_CAPABILITY: phone rc: " + rc);
                return;
            case 36:
                ar = (AsyncResult) msg.obj;
                logd("Event EVENT_SS received");
                if (isPhoneTypeGsm()) {
                    new GsmMmiCode(this, (UiccCardApplication) this.mUiccApplication.get()).processSsData(ar);
                    return;
                }
                return;
            case 39:
            case 40:
                String what = msg.what == 39 ? "EVENT_VOICE_RADIO_TECH_CHANGED" : "EVENT_REQUEST_VOICE_RADIO_TECH_DONE";
                ar = (AsyncResult) msg.obj;
                if (ar.exception != null) {
                    loge(what + ": exception=" + ar.exception);
                    return;
                } else if (ar.result == null || ((int[]) ar.result).length == 0) {
                    loge(what + ": has no tech!");
                    return;
                } else {
                    int newVoiceTech = ((int[]) ar.result)[0];
                    logd(what + ": newVoiceTech=" + newVoiceTech);
                    phoneObjectUpdater(newVoiceTech);
                    return;
                }
            case 41:
                ar = (AsyncResult) msg.obj;
                if (ar.exception != null || ar.result == null) {
                    logd("Unexpected exception on EVENT_RIL_CONNECTED");
                    this.mRilVersion = -1;
                    return;
                }
                this.mRilVersion = ((Integer) ar.result).intValue();
                return;
            case 42:
                phoneObjectUpdater(msg.arg1);
                return;
            case 43:
                if (!this.mContext.getResources().getBoolean(17957018)) {
                    this.mCi.getVoiceRadioTechnology(obtainMessage(40));
                }
                if (!(this.mContext.getPackageManager().hasSystemFeature("oppo.cmcc.test") || this.mImsPhone == null)) {
                    ImsPhone imsphone = (ImsPhone) this.mImsPhone;
                    imsphone.setUserAgentToMd();
                    imsphone.setRusConfig(true);
                }
                ImsManager.updateImsServiceConfig(this.mContext, this.mPhoneId, true);
                PersistableBundle b = ((CarrierConfigManager) getContext().getSystemService("carrier_config")).getConfigForSubId(getSubId());
                if (b != null) {
                    boolean broadcastEmergencyCallStateChanges = b.getBoolean("broadcast_emergency_call_state_changes_bool");
                    logd("broadcastEmergencyCallStateChanges = " + broadcastEmergencyCallStateChanges);
                    setBroadcastEmergencyCallStateChanges(broadcastEmergencyCallStateChanges);
                } else {
                    loge("didn't get broadcastEmergencyCallStateChanges from carrier config");
                }
                if (b != null) {
                    int config_cdma_roaming_mode = b.getInt("cdma_roaming_mode_int");
                    int current_cdma_roaming_mode = Global.getInt(getContext().getContentResolver(), "roaming_settings", -1);
                    switch (config_cdma_roaming_mode) {
                        case -1:
                            if (current_cdma_roaming_mode != config_cdma_roaming_mode) {
                                logd("cdma_roaming_mode is going to changed to " + current_cdma_roaming_mode);
                                setCdmaRoamingPreference(current_cdma_roaming_mode, obtainMessage(44));
                                break;
                            }
                            break;
                        case 0:
                        case 1:
                        case 2:
                            logd("cdma_roaming_mode is going to changed to " + config_cdma_roaming_mode);
                            setCdmaRoamingPreference(config_cdma_roaming_mode, obtainMessage(44));
                            break;
                    }
                    loge("Invalid cdma_roaming_mode settings: " + config_cdma_roaming_mode);
                } else {
                    loge("didn't get the cdma_roaming_mode changes from the carrier config.");
                }
                prepareEri();
                if (!isPhoneTypeGsm()) {
                    this.mSST.pollState();
                    return;
                }
                return;
            case 44:
                logd("cdma_roaming_mode change is done");
                return;
            case 109:
                Rlog.d(LOG_TAG, "mPhoneId = " + this.mPhoneId + ", subId = " + getSubId());
                setSystemProperty(CFU_QUERY_PROPERTY_NAME + this.mPhoneId, "0");
                ar = (AsyncResult) msg.obj;
                Rlog.d(LOG_TAG, "[EVENT_GET_CALL_FORWARD_TIME_SLOT_DONE]ar.exception = " + ar.exception);
                if (ar.exception == null) {
                    handleCfuInTimeSlotQueryResult((CallForwardInfoEx[]) ar.result);
                }
                Rlog.d(LOG_TAG, "[EVENT_GET_CALL_FORWARD_TIME_SLOT_DONE]msg.arg1 = " + msg.arg1);
                if (ar.exception != null && (ar.exception instanceof CommandException)) {
                    cmdException = (CommandException) ar.exception;
                    Rlog.d(LOG_TAG, "[EVENT_GET_CALL_FORWARD_TIME_SLOT_DONE] cmdException error:" + cmdException.getCommandError());
                    if (msg.arg1 == 1 && cmdException != null && ((cmdException.getCommandError() == Error.SPECAIL_UT_COMMAND_NOT_SUPPORTED || cmdException.getCommandError() == Error.OEM_ERROR_2 || cmdException.getCommandError() == Error.OEM_ERROR_3) && this.mSST != null && this.mSST.mSS != null && this.mSST.mSS.getState() == 0)) {
                        getCallForwardingOption(0, obtainMessage(13));
                    }
                    if (is93MDSupport() && cmdException != null && (cmdException.getCommandError() == Error.OEM_ERROR_2 || cmdException.getCommandError() == Error.OEM_ERROR_3)) {
                        Rlog.d(LOG_TAG, "return SPECAIL_UT_COMMAND_NOT_SUPPORTED");
                        ar.exception = new CommandException(Error.SPECAIL_UT_COMMAND_NOT_SUPPORTED);
                    }
                }
                onComplete = (Message) ar.userObj;
                if (onComplete != null) {
                    AsyncResult.forMessage(onComplete, ar.result, ar.exception);
                    onComplete.sendToTarget();
                    return;
                }
                return;
            case 110:
                ar = (AsyncResult) msg.obj;
                IccRecords records = (IccRecords) this.mIccRecords.get();
                CfuEx cfuEx = ar.userObj;
                if (ar.exception == null && records != null) {
                    records.setVoiceCallForwardingFlag(1, msg.arg1 == 1, cfuEx.mSetCfNumber);
                    saveTimeSlot(cfuEx.mSetTimeSlot);
                    if (msg.arg1 == 1) {
                        setSystemProperty("persist.radio.ut.cfu.mode", "enabled_ut_cfu_mode_on");
                    } else {
                        setSystemProperty("persist.radio.ut.cfu.mode", "enabled_ut_cfu_mode_off");
                    }
                }
                if (cfuEx.mOnComplete != null) {
                    AsyncResult.forMessage(cfuEx.mOnComplete, ar.result, ar.exception);
                    cfuEx.mOnComplete.sendToTarget();
                    return;
                }
                return;
            case 301:
                ar = (AsyncResult) msg.obj;
                Rlog.d(LOG_TAG, "[EVENT_GET_CALL_WAITING_]ar.exception = " + ar.exception);
                onComplete = ar.userObj;
                if (ar.exception == null) {
                    int[] cwArray = (int[]) ar.result;
                    try {
                        Rlog.d(LOG_TAG, "EVENT_GET_CALL_WAITING_DONE cwArray[0]:cwArray[1] = " + cwArray[0] + ":" + cwArray[1]);
                        boolean csEnable = cwArray[0] == 1 ? (cwArray[1] & 1) == 1 : false;
                        setTerminalBasedCallWaiting(csEnable, null);
                        if (onComplete != null) {
                            AsyncResult.forMessage(onComplete, ar.result, null);
                            onComplete.sendToTarget();
                            return;
                        }
                        return;
                    } catch (ArrayIndexOutOfBoundsException e3) {
                        Rlog.e(LOG_TAG, "EVENT_GET_CALL_WAITING_DONE: improper result: err =" + e3.getMessage());
                        if (onComplete != null) {
                            AsyncResult.forMessage(onComplete, ar.result, null);
                            onComplete.sendToTarget();
                            return;
                        }
                        return;
                    }
                } else if (onComplete != null) {
                    AsyncResult.forMessage(onComplete, ar.result, ar.exception);
                    onComplete.sendToTarget();
                    return;
                } else {
                    return;
                }
            case 302:
                ar = (AsyncResult) msg.obj;
                onComplete = (Message) ar.userObj;
                if (ar.exception != null) {
                    Rlog.d(LOG_TAG, "EVENT_SET_CALL_WAITING_DONE: ar.exception=" + ar.exception);
                    if (onComplete != null) {
                        AsyncResult.forMessage(onComplete, ar.result, ar.exception);
                        onComplete.sendToTarget();
                        return;
                    }
                    return;
                }
                setTerminalBasedCallWaiting(msg.arg1 == 1, onComplete);
                return;
            case 1002:
                ar = (AsyncResult) msg.obj;
                SuppCrssNotification noti = ar.result;
                Connection cn;
                if (noti.code == 2) {
                    if (getRingingCall().getState() != Call.State.IDLE) {
                        cn = (Connection) getRingingCall().getConnections().get(0);
                        Rlog.d(LOG_TAG, "set number presentation to connection : " + noti.cli_validity);
                        switch (noti.cli_validity) {
                            case 1:
                                cn.setNumberPresentation(2);
                                break;
                            case 2:
                                cn.setNumberPresentation(3);
                                break;
                            case 3:
                                cn.setNumberPresentation(4);
                                break;
                            default:
                                cn.setNumberPresentation(1);
                                break;
                        }
                    }
                } else if (noti.code == 3) {
                    Rlog.d(LOG_TAG, "[COLP]noti.number = " + Rlog.pii(SDBG, noti.number));
                    if (getForegroundCall().getState() != Call.State.IDLE) {
                        cn = (Connection) getForegroundCall().getConnections().get(0);
                        if (!(cn == null || cn.getAddress() == null || cn.getAddress().equals(noti.number))) {
                            cn.setRedirectingAddress(noti.number);
                            Rlog.d(LOG_TAG, "[COLP]Redirecting address = " + Rlog.pii(SDBG, cn.getRedirectingAddress()));
                        }
                    }
                }
                if (this.mCallRelatedSuppSvcRegistrants.size() == 0) {
                    this.mCachedCrssn = ar;
                }
                this.mCallRelatedSuppSvcRegistrants.notifyRegistrants(ar);
                return;
            case ServiceStateTracker.NITZ_UPDATE_DIFF_DEFAULT /*2000*/:
                Rlog.d(LOG_TAG, "EVENT_IMS_UT_DONE: Enter");
                ar = (AsyncResult) msg.obj;
                if (ar == null) {
                    Rlog.e(LOG_TAG, "EVENT_IMS_UT_DONE: Error AsyncResult null!");
                    return;
                }
                SuppSrvRequest ss = ar.userObj;
                int commandInterfaceCFAction;
                int commandInterfaceCFReason;
                String dialingNumber;
                if (ss == null) {
                    Rlog.e(LOG_TAG, "EVENT_IMS_UT_DONE: Error SuppSrvRequest null!");
                    return;
                } else if (17 == ss.getRequestCode()) {
                    if (ar.exception == null) {
                        ss.mParcel.setDataPosition(0);
                        Rlog.d(LOG_TAG, "EVENT_IMS_UT_DONE: SUPP_SRV_REQ_SET_CF_IN_TIME_SLOT");
                        commandInterfaceCFAction = ss.mParcel.readInt();
                        commandInterfaceCFReason = ss.mParcel.readInt();
                        dialingNumber = ss.mParcel.readString();
                        if (commandInterfaceCFReason == 0) {
                            if (isCfEnable(commandInterfaceCFAction)) {
                                setSystemProperty("persist.radio.ut.cfu.mode", "enabled_ut_cfu_mode_on");
                            } else {
                                setSystemProperty("persist.radio.ut.cfu.mode", "enabled_ut_cfu_mode_off");
                            }
                        }
                    }
                    onComplete = ss.getResultCallback();
                    if (onComplete != null) {
                        AsyncResult.forMessage(onComplete, ar.result, ar.exception);
                        onComplete.sendToTarget();
                    }
                    ss.mParcel.recycle();
                    return;
                } else {
                    cmdException = null;
                    ImsException imsException = null;
                    if (ar.exception != null && (ar.exception instanceof CommandException)) {
                        cmdException = ar.exception;
                        Rlog.d(LOG_TAG, "EVENT_IMS_UT_DONE: cmdException error:" + cmdException.getCommandError());
                    }
                    if (ar.exception != null && (ar.exception instanceof ImsException)) {
                        imsException = (ImsException) ar.exception;
                        Rlog.d(LOG_TAG, "EVENT_IMS_UT_DONE: ImsException code:" + imsException.getCode());
                    }
                    if (cmdException != null && cmdException.getCommandError() == Error.UT_XCAP_403_FORBIDDEN) {
                        setCsFallbackStatus(2);
                        if (isNotSupportUtToCS()) {
                            Rlog.d(LOG_TAG, "UT_XCAP_403_FORBIDDEN.");
                            ar.exception = new CommandException(Error.UT_XCAP_403_FORBIDDEN);
                            onComplete = ss.getResultCallback();
                            if (onComplete != null) {
                                AsyncResult.forMessage(onComplete, ar.result, ar.exception);
                                onComplete.sendToTarget();
                            }
                            ss.mParcel.recycle();
                            return;
                        }
                        Rlog.d(LOG_TAG, "Csfallback next_reboot.");
                        sendMessage(obtainMessage(2001, ss));
                        return;
                    } else if (cmdException == null || cmdException.getCommandError() != Error.UT_UNKNOWN_HOST) {
                        if (imsException != null && imsException.getCode() == 830) {
                            setCsFallbackStatus(2);
                            if (isNotSupportUtToCS()) {
                                Rlog.d(LOG_TAG, "ImsReasonInfo.CODE_UT_XCAP_403_FORBIDDEN.");
                                ar.exception = new CommandException(Error.UT_XCAP_403_FORBIDDEN);
                                onComplete = ss.getResultCallback();
                                if (onComplete != null) {
                                    AsyncResult.forMessage(onComplete, ar.result, ar.exception);
                                    onComplete.sendToTarget();
                                }
                                ss.mParcel.recycle();
                                return;
                            }
                            Rlog.d(LOG_TAG, "Csfallback next_reboot.");
                            sendMessage(obtainMessage(2001, ss));
                            return;
                        } else if (imsException == null || imsException.getCode() != 831) {
                            if (ar.exception == null && 11 == ss.getRequestCode()) {
                                ss.mParcel.setDataPosition(0);
                                Rlog.d(LOG_TAG, "EVENT_IMS_UT_DONE: SUPP_SRV_REQ_SET_CF");
                                commandInterfaceCFAction = ss.mParcel.readInt();
                                commandInterfaceCFReason = ss.mParcel.readInt();
                                dialingNumber = ss.mParcel.readString();
                                if (commandInterfaceCFReason == 0) {
                                    if (queryCFUAgainAfterSet()) {
                                        if (ar.result != null) {
                                            cfinfo = (CallForwardInfo[]) ar.result;
                                            if (cfinfo != null && cfinfo.length != 0) {
                                                i = 0;
                                                while (i < cfinfo.length) {
                                                    if ((cfinfo[i].serviceClass & 1) == 0) {
                                                        i++;
                                                    } else if (cfinfo[i].status == 1) {
                                                        Rlog.d(LOG_TAG, "Set enable, serviceClass: " + cfinfo[i].serviceClass);
                                                        setSystemProperty("persist.radio.ut.cfu.mode", "enabled_ut_cfu_mode_on");
                                                    } else {
                                                        Rlog.d(LOG_TAG, "Set disable, serviceClass: " + cfinfo[i].serviceClass);
                                                        setSystemProperty("persist.radio.ut.cfu.mode", "enabled_ut_cfu_mode_off");
                                                    }
                                                }
                                                break;
                                            }
                                            Rlog.d(LOG_TAG, "cfinfo is null or 0.");
                                        } else {
                                            Rlog.d(LOG_TAG, "ar.result is null.");
                                        }
                                    } else if (isCfEnable(commandInterfaceCFAction)) {
                                        setSystemProperty("persist.radio.ut.cfu.mode", "enabled_ut_cfu_mode_on");
                                    } else {
                                        setSystemProperty("persist.radio.ut.cfu.mode", "enabled_ut_cfu_mode_off");
                                    }
                                }
                            } else if (imsException == null || imsException.getCode() != 832) {
                                if (cmdException == null || cmdException.getCommandError() != Error.UT_XCAP_404_NOT_FOUND) {
                                    if (imsException == null || imsException.getCode() != 833) {
                                        if (cmdException != null && cmdException.getCommandError() == Error.UT_XCAP_409_CONFLICT) {
                                            if (isEnableXcapHttpResponse409()) {
                                                Rlog.d(LOG_TAG, "GSMPhone get UT_XCAP_409_CONFLICT.");
                                            } else {
                                                Rlog.d(LOG_TAG, "GSMPhone get UT_XCAP_409_CONFLICT, return GENERIC_FAILURE");
                                                ar.exception = new CommandException(Error.GENERIC_FAILURE);
                                            }
                                        }
                                    } else if (isEnableXcapHttpResponse409()) {
                                        Rlog.d(LOG_TAG, "GSMPhone get UT_XCAP_409_CONFLICT.");
                                        ar.exception = new CommandException(Error.UT_XCAP_409_CONFLICT);
                                    } else {
                                        Rlog.d(LOG_TAG, "GSMPhone get UT_XCAP_409_CONFLICT, return GENERIC_FAILURE");
                                        ar.exception = new CommandException(Error.GENERIC_FAILURE);
                                    }
                                } else if (isOpTransferXcap404() && (ss.getRequestCode() == 10 || ss.getRequestCode() == 9)) {
                                    Rlog.d(LOG_TAG, "GSMPhone get UT_XCAP_404_NOT_FOUND.");
                                } else {
                                    ar.exception = new CommandException(Error.GENERIC_FAILURE);
                                }
                            } else if (isOpTransferXcap404() && (ss.getRequestCode() == 10 || ss.getRequestCode() == 9)) {
                                ar.exception = new CommandException(Error.UT_XCAP_404_NOT_FOUND);
                            } else {
                                ar.exception = new CommandException(Error.GENERIC_FAILURE);
                            }
                            onComplete = ss.getResultCallback();
                            if (onComplete != null) {
                                AsyncResult.forMessage(onComplete, ar.result, ar.exception);
                                onComplete.sendToTarget();
                            }
                            ss.mParcel.recycle();
                            return;
                        } else if (isNotSupportUtToCS()) {
                            Rlog.d(LOG_TAG, "CommandException.Error.UT_UNKNOWN_HOST.");
                            ar.exception = new CommandException(Error.UT_XCAP_403_FORBIDDEN);
                            onComplete = ss.getResultCallback();
                            if (onComplete != null) {
                                AsyncResult.forMessage(onComplete, ar.result, ar.exception);
                                onComplete.sendToTarget();
                            }
                            ss.mParcel.recycle();
                            return;
                        } else {
                            Rlog.d(LOG_TAG, "Csfallback once.");
                            setCsFallbackStatus(1);
                            sendMessage(obtainMessage(2001, ss));
                            return;
                        }
                    } else if (isNotSupportUtToCS()) {
                        Rlog.d(LOG_TAG, "CommandException.Error.UT_UNKNOWN_HOST.");
                        ar.exception = new CommandException(Error.UT_XCAP_403_FORBIDDEN);
                        onComplete = ss.getResultCallback();
                        if (onComplete != null) {
                            AsyncResult.forMessage(onComplete, ar.result, ar.exception);
                            onComplete.sendToTarget();
                        }
                        ss.mParcel.recycle();
                        return;
                    } else {
                        Rlog.d(LOG_TAG, "Csfallback once.");
                        setCsFallbackStatus(1);
                        sendMessage(obtainMessage(2001, ss));
                        return;
                    }
                }
            case 2001:
                handleImsUtCsfb(msg);
                return;
            case 2002:
                String simType = getSystemProperty("gsm.external.sim.inserted", "0");
                if (simType == null || simType.length() <= 0 || "0".equals(simType)) {
                    boolean checkEQC = checkEventQueryCfuReady();
                    Rlog.d(LOG_TAG, "Receive EVENT_QUERY_CFU phoneid: " + getPhoneId() + " , needQueryCfu: " + this.needQueryCfu + " , checkEventQueryCfuReady: " + checkEQC);
                    if (this.needQueryCfu && checkEQC) {
                        String cfuSetting;
                        String defaultQueryCfuMode = PhoneConstants.CFU_QUERY_TYPE_DEF_VALUE;
                        if (this.mSupplementaryServiceExt != null) {
                            defaultQueryCfuMode = this.mSupplementaryServiceExt.getOpDefaultQueryCfuMode();
                            Rlog.d(LOG_TAG, "defaultQueryCfuMode = " + defaultQueryCfuMode);
                        }
                        if (TelephonyManager.from(this.mContext).isVoiceCapable()) {
                            cfuSetting = SystemProperties.get("persist.radio.cfu.querytype", defaultQueryCfuMode);
                        } else {
                            cfuSetting = SystemProperties.get("persist.radio.cfu.querytype", "1");
                        }
                        String isTestSim = "0";
                        boolean isRRMEnv = false;
                        if (this.mPhoneId == 0) {
                            isTestSim = SystemProperties.get("gsm.sim.ril.testsim", "0");
                        } else if (this.mPhoneId == 1) {
                            isTestSim = SystemProperties.get("gsm.sim.ril.testsim.2", "0");
                        }
                        String operatorNumeric = getServiceState().getOperatorNumeric();
                        if (operatorNumeric != null && operatorNumeric.equals("46602")) {
                            isRRMEnv = true;
                        }
                        Rlog.d(LOG_TAG, "[GSMPhone] CFU_KEY = " + cfuSetting + " isTestSIM : " + isTestSim + " isRRMEnv : " + isRRMEnv + " phoneid: " + getPhoneId());
                        IccRecords record;
                        String utCfuMode;
                        if (!isTestSim.equals("0") || isRRMEnv) {
                            this.needQueryCfu = false;
                            record = (IccRecords) this.mIccRecords.get();
                            if (record != null && (record instanceof SIMRecords) && !((SIMRecords) record).checkEfCfis()) {
                                utCfuMode = getSystemProperty("persist.radio.ut.cfu.mode", "disabled_ut_cfu_mode");
                                Rlog.d(LOG_TAG, "utCfuMode: " + utCfuMode);
                                if ("enabled_ut_cfu_mode_on".equals(utCfuMode)) {
                                    setVoiceCallForwardingFlag(1, true, UsimPBMemInfo.STRING_NOT_SET);
                                    return;
                                } else if ("enabled_ut_cfu_mode_off".equals(utCfuMode)) {
                                    setVoiceCallForwardingFlag(1, false, UsimPBMemInfo.STRING_NOT_SET);
                                    return;
                                } else {
                                    return;
                                }
                            }
                            return;
                        }
                        String isChangedProp = CFU_QUERY_SIM_CHANGED_PROP + getPhoneId();
                        String isChanged = SystemProperties.get(isChangedProp, "0");
                        Rlog.d(LOG_TAG, "[GSMPhone] isChanged " + isChanged);
                        if (cfuSetting.equals("2") || (cfuSetting.equals("0") && isChanged.equals("1"))) {
                            this.mCfuQueryRetryCount = 0;
                            queryCfuOrWait();
                            this.needQueryCfu = false;
                            SystemProperties.set(isChangedProp, "0");
                            return;
                        }
                        this.needQueryCfu = false;
                        record = (IccRecords) this.mIccRecords.get();
                        if (record != null && (record instanceof SIMRecords) && !((SIMRecords) record).checkEfCfis()) {
                            utCfuMode = getSystemProperty("persist.radio.ut.cfu.mode", "disabled_ut_cfu_mode");
                            Rlog.d(LOG_TAG, "utCfuMode: " + utCfuMode);
                            if ("enabled_ut_cfu_mode_on".equals(utCfuMode)) {
                                setVoiceCallForwardingFlag(1, true, UsimPBMemInfo.STRING_NOT_SET);
                                return;
                            } else if ("enabled_ut_cfu_mode_off".equals(utCfuMode)) {
                                setVoiceCallForwardingFlag(1, false, UsimPBMemInfo.STRING_NOT_SET);
                                return;
                            } else {
                                return;
                            }
                        }
                        return;
                    }
                    return;
                }
                Rlog.d(LOG_TAG, "It's external SIM, skip CFU query, phoneId: " + getPhoneId());
                return;
            case 2003:
                handleUssiCsfb((String) msg.obj);
                return;
            case 2004:
                Rlog.d(LOG_TAG, "EVENT_GET_CLIR_COMPLETE");
                ar = (AsyncResult) msg.obj;
                if (ar.exception != null && (ar.exception instanceof CommandException)) {
                    cmdException = (CommandException) ar.exception;
                    Rlog.d(LOG_TAG, "EVENT_GET_CLIR_COMPLETE: cmdException error:" + cmdException.getCommandError());
                    if (is93MDSupport()) {
                        if ((isOp(OPID.OP01) || isOp(OPID.OP02)) && cmdException != null) {
                            if (isUtError(cmdException.getCommandError())) {
                                Rlog.d(LOG_TAG, "return SPECAIL_UT_COMMAND_NOT_SUPPORTED");
                                ar.exception = new CommandException(Error.SPECAIL_UT_COMMAND_NOT_SUPPORTED);
                            } else {
                                Rlog.d(LOG_TAG, "return Original Error");
                            }
                        }
                        if (cmdException.getCommandError() == Error.OEM_ERROR_7) {
                            Object result = getSavedClirSetting();
                            Rlog.d(LOG_TAG, "Terminal based and getSavedClirSetting:" + Arrays.toString(result));
                            onComplete = (Message) ar.userObj;
                            if (onComplete != null) {
                                AsyncResult.forMessage(onComplete, result, null);
                                onComplete.sendToTarget();
                                return;
                            }
                            return;
                        }
                    }
                }
                onComplete = (Message) ar.userObj;
                if (onComplete != null) {
                    AsyncResult.forMessage(onComplete, ar.result, ar.exception);
                    onComplete.sendToTarget();
                    return;
                }
                return;
            case 2005:
                Rlog.d(LOG_TAG, "EVENT_SET_FACILITY_LOCK_COMPLETE");
                ar = (AsyncResult) msg.obj;
                if (ar.exception != null && (ar.exception instanceof CommandException)) {
                    cmdException = (CommandException) ar.exception;
                    Rlog.d(LOG_TAG, "EVENT_SET_FACILITY_LOCK_COMPLETE: cmdException error:" + cmdException.getCommandError());
                    if (is93MDSupport() && isOp(OPID.OP01) && cmdException != null) {
                        if (isUtError(cmdException.getCommandError())) {
                            Rlog.d(LOG_TAG, "return SPECAIL_UT_COMMAND_NOT_SUPPORTED");
                            ar.exception = new CommandException(Error.SPECAIL_UT_COMMAND_NOT_SUPPORTED);
                        } else {
                            Rlog.d(LOG_TAG, "return Original Error");
                        }
                    }
                }
                onComplete = (Message) ar.userObj;
                if (onComplete != null) {
                    AsyncResult.forMessage(onComplete, ar.result, ar.exception);
                    onComplete.sendToTarget();
                    return;
                }
                return;
            case 2006:
                Rlog.d(LOG_TAG, "EVENT_GET_FACILITY_LOCK_COMPLETE");
                ar = (AsyncResult) msg.obj;
                if (ar.exception != null && (ar.exception instanceof CommandException)) {
                    cmdException = (CommandException) ar.exception;
                    Rlog.d(LOG_TAG, "EVENT_GET_FACILITY_LOCK_COMPLETE: cmdException error:" + cmdException.getCommandError());
                    if (is93MDSupport() && ((isOp(OPID.OP01) || isOp(OPID.OP09)) && cmdException != null)) {
                        if (isUtError(cmdException.getCommandError())) {
                            Rlog.d(LOG_TAG, "return SPECAIL_UT_COMMAND_NOT_SUPPORTED");
                            ar.exception = new CommandException(Error.SPECAIL_UT_COMMAND_NOT_SUPPORTED);
                        } else {
                            Rlog.d(LOG_TAG, "return Original Error");
                        }
                    }
                }
                onComplete = (Message) ar.userObj;
                if (onComplete != null) {
                    AsyncResult.forMessage(onComplete, ar.result, ar.exception);
                    onComplete.sendToTarget();
                    return;
                }
                return;
            default:
                super.handleMessage(msg);
                return;
        }
    }

    public UiccCardApplication getUiccCardApplication() {
        if (isPhoneTypeGsm()) {
            return this.mUiccController.getUiccCardApplication(this.mPhoneId, 1);
        }
        return this.mUiccController.getUiccCardApplication(this.mPhoneId, 2);
    }

    protected void onUpdateIccAvailability() {
        if (this.mUiccController != null) {
            UiccCardApplication newUiccApplication;
            if (isPhoneTypeGsm() || isPhoneTypeCdmaLte()) {
                newUiccApplication = this.mUiccController.getUiccCardApplication(this.mPhoneId, 3);
                IsimUiccRecords newIsimUiccRecords = null;
                if (newUiccApplication != null) {
                    newIsimUiccRecords = (IsimUiccRecords) newUiccApplication.getIccRecords();
                    logd("New ISIM application found");
                }
                this.mIsimUiccRecords = newIsimUiccRecords;
            }
            if (this.mSimRecords != null) {
                this.mSimRecords.unregisterForRecordsLoaded(this);
            }
            if (isPhoneTypeCdmaLte()) {
                newUiccApplication = this.mUiccController.getUiccCardApplication(this.mPhoneId, 1);
                SIMRecords sIMRecords = null;
                if (newUiccApplication != null) {
                    sIMRecords = (SIMRecords) newUiccApplication.getIccRecords();
                }
                this.mSimRecords = sIMRecords;
                if (this.mSimRecords != null) {
                    this.mSimRecords.registerForRecordsLoaded(this, 3, null);
                }
            } else {
                this.mSimRecords = null;
            }
            newUiccApplication = getUiccCardApplication();
            if (!isPhoneTypeGsm() && newUiccApplication == null) {
                logd("can't find 3GPP2 application; trying APP_FAM_3GPP");
                newUiccApplication = this.mUiccController.getUiccCardApplication(this.mPhoneId, 1);
            }
            UiccCardApplication app = (UiccCardApplication) this.mUiccApplication.get();
            IccRecords newIccRecord = newUiccApplication != null ? newUiccApplication.getIccRecords() : null;
            if (!(app == newUiccApplication && this.mIccRecords.get() == newIccRecord)) {
                if (app != null) {
                    logd("Removing stale icc objects.");
                    if (this.mIccRecords.get() != null) {
                        unregisterForIccRecordEvents();
                        this.mIccPhoneBookIntManager.updateIccRecords(null);
                    }
                    this.mIccRecords.set(null);
                    this.mUiccApplication.set(null);
                }
                if (newUiccApplication != null) {
                    logd("New Uicc application found. type = " + newUiccApplication.getType());
                    this.mUiccApplication.set(newUiccApplication);
                    this.mIccRecords.set(newUiccApplication.getIccRecords());
                    registerForIccRecordEvents();
                    this.mIccPhoneBookIntManager.updateIccRecords((IccRecords) this.mIccRecords.get());
                    handleSuppServInitIfSimChanged();
                }
            }
            Rlog.d(LOG_TAG, "isPhoneTypeCdmaLte:" + isPhoneTypeCdmaLte() + ", phoneId: " + getPhoneId() + " isCdmaWithoutLteCard: " + isCdmaWithoutLteCard() + " mNewVoiceTech: " + this.mNewVoiceTech);
            if (this.mNewVoiceTech != -1 && ((isPhoneTypeCdmaLte() && isCdmaWithoutLteCard()) || (isPhoneTypeCdma() && !isCdmaWithoutLteCard()))) {
                updatePhoneObject(this.mNewVoiceTech);
            }
        }
    }

    private void processIccRecordEvents(int eventCode) {
        switch (eventCode) {
            case 1:
                Rlog.d(LOG_TAG, "processIccRecordEvents");
                notifyCallForwardingIndicator();
                return;
            default:
                return;
        }
    }

    public boolean updateCurrentCarrierInProvider() {
        if (!isPhoneTypeGsm() && !isPhoneTypeCdmaLte()) {
            return true;
        }
        long currentDds = (long) SubscriptionManager.getDefaultDataSubscriptionId();
        String operatorNumeric = getOperatorNumeric();
        logd("updateCurrentCarrierInProvider: mSubId = " + getSubId() + " currentDds = " + currentDds + " operatorNumeric = " + operatorNumeric);
        if (!TextUtils.isEmpty(operatorNumeric) && ((long) getSubId()) == currentDds) {
            try {
                Uri uri = Uri.withAppendedPath(Carriers.CONTENT_URI, "current");
                ContentValues map = new ContentValues();
                map.put("numeric", operatorNumeric);
                this.mContext.getContentResolver().insert(uri, map);
                return true;
            } catch (SQLException e) {
                Rlog.e(LOG_TAG, "Can't store current operator", e);
            }
        }
        return false;
    }

    private boolean updateCurrentCarrierInProvider(String operatorNumeric) {
        if (isPhoneTypeCdma() || (isPhoneTypeCdmaLte() && this.mUiccController.getUiccCardApplication(this.mPhoneId, 1) == null)) {
            logd("CDMAPhone: updateCurrentCarrierInProvider called");
            if (!TextUtils.isEmpty(operatorNumeric)) {
                try {
                    Uri uri = Uri.withAppendedPath(Carriers.CONTENT_URI, "current");
                    ContentValues map = new ContentValues();
                    map.put("numeric", operatorNumeric);
                    logd("updateCurrentCarrierInProvider from system: numeric=" + operatorNumeric);
                    getContext().getContentResolver().insert(uri, map);
                    logd("update mccmnc=" + operatorNumeric);
                    MccTable.updateMccMncConfiguration(this.mContext, operatorNumeric, false);
                    return true;
                } catch (SQLException e) {
                    Rlog.e(LOG_TAG, "Can't store current operator", e);
                }
            }
            return false;
        }
        logd("updateCurrentCarrierInProvider not updated X retVal=true");
        return true;
    }

    private void handleCfuQueryResult(CallForwardInfo[] infos) {
        boolean z = false;
        if (((IccRecords) this.mIccRecords.get()) == null) {
            return;
        }
        if (infos == null || infos.length == 0) {
            setVoiceCallForwardingFlag(1, false, null);
            setSystemProperty("persist.radio.ut.cfu.mode", "enabled_ut_cfu_mode_off");
            return;
        }
        int s = infos.length;
        for (int i = 0; i < s; i++) {
            if ((infos[i].serviceClass & 512) != 0) {
                boolean z2;
                if (infos[i].status == 1) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                setVideoCallForwardingFlag(z2);
            }
            if ((infos[i].serviceClass & 1) != 0) {
                if (infos[i].status == 1) {
                    z = true;
                }
                setVoiceCallForwardingFlag(1, z, infos[i].number);
                setSystemProperty("persist.radio.ut.cfu.mode", infos[i].status == 1 ? "enabled_ut_cfu_mode_on" : "enabled_ut_cfu_mode_off");
                return;
            }
        }
    }

    public IccPhoneBookInterfaceManager getIccPhoneBookInterfaceManager() {
        return this.mIccPhoneBookIntManager;
    }

    public void registerForEriFileLoaded(Handler h, int what, Object obj) {
        this.mEriFileLoadedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForEriFileLoaded(Handler h) {
        this.mEriFileLoadedRegistrants.remove(h);
    }

    public void prepareEri() {
        if (this.mEriManager == null) {
            Rlog.e(LOG_TAG, "PrepareEri: Trying to access stale objects");
            return;
        }
        this.mEriManager.loadEriFile();
        if (this.mEriManager.isEriFileLoaded()) {
            logd("ERI read, notify registrants");
            this.mEriFileLoadedRegistrants.notifyRegistrants();
        }
    }

    public boolean isEriFileLoaded() {
        return this.mEriManager.isEriFileLoaded();
    }

    public void activateCellBroadcastSms(int activate, Message response) {
        loge("[GsmCdmaPhone] activateCellBroadcastSms() is obsolete; use SmsManager");
        response.sendToTarget();
    }

    public void getCellBroadcastSmsConfig(Message response) {
        loge("[GsmCdmaPhone] getCellBroadcastSmsConfig() is obsolete; use SmsManager");
        response.sendToTarget();
    }

    public void setCellBroadcastSmsConfig(int[] configValuesArray, Message response) {
        loge("[GsmCdmaPhone] setCellBroadcastSmsConfig() is obsolete; use SmsManager");
        response.sendToTarget();
    }

    public boolean needsOtaServiceProvisioning() {
        boolean z = false;
        if (isPhoneTypeGsm()) {
            return false;
        }
        if (this.mSST.getOtasp() != 3) {
            z = true;
        }
        return z;
    }

    public boolean isCspPlmnEnabled() {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        return r != null ? r.isCspPlmnEnabled() : false;
    }

    public boolean isManualNetSelAllowed() {
        int nwMode = Global.getInt(this.mContext.getContentResolver(), "preferred_network_mode" + getSubId(), Phone.PREFERRED_NT_MODE);
        logd("isManualNetSelAllowed in mode = " + nwMode);
        if (isManualSelProhibitedInGlobalMode() && (nwMode == 10 || nwMode == 7)) {
            logd("Manual selection not supported in mode = " + nwMode);
            return false;
        }
        logd("Manual selection is supported in mode = " + nwMode);
        return true;
    }

    private boolean isManualSelProhibitedInGlobalMode() {
        boolean isProhibited = false;
        String configString = getContext().getResources().getString(17039464);
        if (!TextUtils.isEmpty(configString)) {
            String[] configArray = configString.split(";");
            if (configArray != null && ((configArray.length == 1 && configArray[0].equalsIgnoreCase("true")) || (configArray.length == 2 && !TextUtils.isEmpty(configArray[1]) && configArray[0].equalsIgnoreCase("true") && isMatchGid(configArray[1])))) {
                isProhibited = true;
            }
        }
        logd("isManualNetSelAllowedInGlobal in current carrier is " + isProhibited);
        return isProhibited;
    }

    private void registerForIccRecordEvents() {
        Rlog.d(LOG_TAG, "registerForIccRecordEvents, phonetype: " + isPhoneTypeGsm());
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r != null) {
            if (r instanceof SIMRecords) {
                r.registerForNetworkSelectionModeAutomatic(this, 28, null);
                r.registerForRecordsEvents(this, 29, null);
                r.registerForRecordsLoaded(this, 3, null);
            } else {
                r.registerForRecordsLoaded(this, 22, null);
            }
        }
    }

    private void unregisterForIccRecordEvents() {
        Rlog.d(LOG_TAG, "unregisterForIccRecordEvents");
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r != null) {
            r.unregisterForNetworkSelectionModeAutomatic(this);
            r.unregisterForRecordsEvents(this);
            r.unregisterForRecordsLoaded(this);
        }
    }

    public void exitEmergencyCallbackMode() {
        if (!isPhoneTypeGsm()) {
            if (this.mWakeLock.isHeld()) {
                this.mWakeLock.release();
            }
            this.mCi.exitEmergencyCallbackMode(obtainMessage(26));
        } else if (this.mImsPhone != null) {
            this.mImsPhone.exitEmergencyCallbackMode();
        }
    }

    private void handleEnterEmergencyCallbackMode(Message msg) {
        Rlog.d(LOG_TAG, "handleEnterEmergencyCallbackMode,mIsPhoneInEcmState= " + this.mIsPhoneInEcmState);
        if (!this.mIsPhoneInEcmState) {
            TelephonyManager.setTelephonyProperty(this.mPhoneId, "ril.cdma.inecmmode", "true");
            this.mIsPhoneInEcmState = true;
            sendEmergencyCallbackModeChange();
            this.mDcTracker.setInternalDataEnabled(false);
            notifyEmergencyCallRegistrants(true);
            postDelayed(this.mExitEcmRunnable, SystemProperties.getLong("ro.cdma.ecmexittimer", 300000));
            this.mWakeLock.acquire();
        }
    }

    private void handleExitEmergencyCallbackMode(Message msg) {
        AsyncResult ar = msg.obj;
        Rlog.d(LOG_TAG, "handleExitEmergencyCallbackMode,ar.exception , mIsPhoneInEcmState " + ar.exception + this.mIsPhoneInEcmState);
        removeCallbacks(this.mExitEcmRunnable);
        if (this.mEcmExitRespRegistrant != null) {
            this.mEcmExitRespRegistrant.notifyRegistrant(ar);
        }
        if (ar.exception == null) {
            if (this.mIsPhoneInEcmState) {
                TelephonyManager.setTelephonyProperty(this.mPhoneId, "ril.cdma.inecmmode", "false");
                this.mIsPhoneInEcmState = false;
            }
            if (this.mWakeLock.isHeld()) {
                this.mWakeLock.release();
            }
            sendEmergencyCallbackModeChange();
            this.mDcTracker.setInternalDataEnabled(true);
            notifyEmergencyCallRegistrants(false);
        }
    }

    public void notifyEmergencyCallRegistrants(boolean started) {
        this.mEmergencyCallToggledRegistrants.notifyResult(Integer.valueOf(started ? 1 : 0));
    }

    public void handleTimerInEmergencyCallbackMode(int action) {
        switch (action) {
            case 0:
                postDelayed(this.mExitEcmRunnable, SystemProperties.getLong("ro.cdma.ecmexittimer", 300000));
                this.mEcmTimerResetRegistrants.notifyResult(Boolean.FALSE);
                return;
            case 1:
                removeCallbacks(this.mExitEcmRunnable);
                this.mEcmTimerResetRegistrants.notifyResult(Boolean.TRUE);
                return;
            default:
                Rlog.e(LOG_TAG, "handleTimerInEmergencyCallbackMode, unsupported action " + action);
                return;
        }
    }

    private static boolean isIs683OtaSpDialStr(String dialStr) {
        if (dialStr.length() != 4) {
            switch (extractSelCodeFromOtaSpNum(dialStr)) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    return true;
                default:
                    return false;
            }
        } else if (dialStr.equals(IS683A_FEATURE_CODE)) {
            return true;
        } else {
            return false;
        }
    }

    private static int extractSelCodeFromOtaSpNum(String dialStr) {
        int dialStrLen = dialStr.length();
        int sysSelCodeInt = -1;
        if (dialStr.regionMatches(0, IS683A_FEATURE_CODE, 0, 4) && dialStrLen >= 6) {
            sysSelCodeInt = Integer.parseInt(dialStr.substring(4, 6));
        }
        Rlog.d(LOG_TAG, "extractSelCodeFromOtaSpNum " + sysSelCodeInt);
        return sysSelCodeInt;
    }

    private static boolean checkOtaSpNumBasedOnSysSelCode(int sysSelCodeInt, String[] sch) {
        try {
            int selRc = Integer.parseInt(sch[1]);
            int i = 0;
            while (i < selRc) {
                if (!(TextUtils.isEmpty(sch[i + 2]) || TextUtils.isEmpty(sch[i + 3]))) {
                    int selMin = Integer.parseInt(sch[i + 2]);
                    int selMax = Integer.parseInt(sch[i + 3]);
                    if (sysSelCodeInt >= selMin && sysSelCodeInt <= selMax) {
                        return true;
                    }
                }
                i++;
            }
            return false;
        } catch (NumberFormatException ex) {
            Rlog.e(LOG_TAG, "checkOtaSpNumBasedOnSysSelCode, error", ex);
            return false;
        }
    }

    private boolean isCarrierOtaSpNum(String dialStr) {
        boolean isOtaSpNum = false;
        int sysSelCodeInt = extractSelCodeFromOtaSpNum(dialStr);
        if (sysSelCodeInt == -1) {
            return false;
        }
        if (TextUtils.isEmpty(this.mCarrierOtaSpNumSchema)) {
            Rlog.d(LOG_TAG, "isCarrierOtaSpNum,ota schema pattern empty");
        } else {
            Matcher m = pOtaSpNumSchema.matcher(this.mCarrierOtaSpNumSchema);
            Rlog.d(LOG_TAG, "isCarrierOtaSpNum,schema" + this.mCarrierOtaSpNumSchema);
            if (m.find()) {
                String[] sch = pOtaSpNumSchema.split(this.mCarrierOtaSpNumSchema);
                if (TextUtils.isEmpty(sch[0]) || !sch[0].equals("SELC")) {
                    if (TextUtils.isEmpty(sch[0]) || !sch[0].equals("FC")) {
                        Rlog.d(LOG_TAG, "isCarrierOtaSpNum,ota schema not supported" + sch[0]);
                    } else {
                        if (dialStr.regionMatches(0, sch[2], 0, Integer.parseInt(sch[1]))) {
                            isOtaSpNum = true;
                        } else {
                            Rlog.d(LOG_TAG, "isCarrierOtaSpNum,not otasp number");
                        }
                    }
                } else if (sysSelCodeInt != -1) {
                    isOtaSpNum = checkOtaSpNumBasedOnSysSelCode(sysSelCodeInt, sch);
                } else {
                    Rlog.d(LOG_TAG, "isCarrierOtaSpNum,sysSelCodeInt is invalid");
                }
            } else {
                Rlog.d(LOG_TAG, "isCarrierOtaSpNum,ota schema pattern not right" + this.mCarrierOtaSpNumSchema);
            }
        }
        return isOtaSpNum;
    }

    public boolean isOtaSpNumber(String dialStr) {
        if (isPhoneTypeGsm()) {
            return super.isOtaSpNumber(dialStr);
        }
        boolean isOtaSpNum = false;
        String dialableStr = PhoneNumberUtils.extractNetworkPortionAlt(dialStr);
        if (dialableStr != null) {
            isOtaSpNum = isIs683OtaSpDialStr(dialableStr);
            if (!isOtaSpNum) {
                isOtaSpNum = isCarrierOtaSpNum(dialableStr);
            }
        }
        Rlog.d(LOG_TAG, "isOtaSpNumber " + isOtaSpNum);
        return isOtaSpNum;
    }

    public int getCdmaEriIconIndex() {
        if (isPhoneTypeGsm()) {
            return super.getCdmaEriIconIndex();
        }
        return getServiceState().getCdmaEriIconIndex();
    }

    public int getCdmaEriIconMode() {
        if (isPhoneTypeGsm()) {
            return super.getCdmaEriIconMode();
        }
        return getServiceState().getCdmaEriIconMode();
    }

    public String getCdmaEriText() {
        if (isPhoneTypeGsm()) {
            return super.getCdmaEriText();
        }
        return this.mEriManager.getCdmaEriText(getServiceState().getCdmaRoamingIndicator(), getServiceState().getCdmaDefaultRoamingIndicator());
    }

    private void phoneObjectUpdater(int newVoiceRadioTech) {
        boolean z = true;
        logd("phoneObjectUpdater: newVoiceRadioTech=" + newVoiceRadioTech);
        this.mNewVoiceTech = newVoiceRadioTech;
        if (ServiceState.isLte(newVoiceRadioTech) || newVoiceRadioTech == 0) {
            PersistableBundle b = ((CarrierConfigManager) getContext().getSystemService("carrier_config")).getConfigForSubId(getSubId());
            if (b != null) {
                int volteReplacementRat = b.getInt("volte_replacement_rat_int");
                logd("phoneObjectUpdater: volteReplacementRat=" + volteReplacementRat);
                if (volteReplacementRat != 0) {
                    newVoiceRadioTech = volteReplacementRat;
                }
            } else {
                loge("phoneObjectUpdater: didn't get volteReplacementRat from carrier config");
            }
        }
        if (this.mRilVersion == 6 && getLteOnCdmaMode() == 1) {
            if (getPhoneType() == 2) {
                logd("phoneObjectUpdater: LTE ON CDMA property is set. Use CDMA Phone newVoiceRadioTech=" + newVoiceRadioTech + " mActivePhone=" + getPhoneName());
                return;
            } else {
                logd("phoneObjectUpdater: LTE ON CDMA property is set. Switch to CDMALTEPhone newVoiceRadioTech=" + newVoiceRadioTech + " mActivePhone=" + getPhoneName());
                newVoiceRadioTech = 6;
            }
        } else if (isShuttingDown()) {
            logd("Device is shutting down. No need to switch phone now.");
            return;
        } else {
            boolean matchCdma = ServiceState.isCdma(newVoiceRadioTech);
            boolean matchGsm = ServiceState.isGsm(newVoiceRadioTech);
            if ((matchCdma && getPhoneType() == 2) || (matchGsm && getPhoneType() == 1)) {
                if (matchCdma && getPhoneType() == 2) {
                    this.mIccCardProxy.setVoiceRadioTech(newVoiceRadioTech);
                }
                if (!(isPhoneTypeCdmaLte() && isCdmaWithoutLteCard()) && (!isPhoneTypeCdma() || isCdmaWithoutLteCard())) {
                    z = false;
                }
                if (!z) {
                    logd("phoneObjectUpdater: No change ignore, newVoiceRadioTech=" + newVoiceRadioTech + " mActivePhone=" + getPhoneName());
                    return;
                }
            }
            if (!(matchCdma || matchGsm)) {
                loge("phoneObjectUpdater: newVoiceRadioTech=" + newVoiceRadioTech + " doesn't match either CDMA or GSM - error! No phone change");
                return;
            }
        }
        if (newVoiceRadioTech == 0) {
            logd("phoneObjectUpdater: Unknown rat ignore,  newVoiceRadioTech=Unknown. mActivePhone=" + getPhoneName());
            return;
        }
        boolean oldPowerState = false;
        if (this.mResetModemOnRadioTechnologyChange && this.mCi.getRadioState().isOn()) {
            oldPowerState = true;
            logd("phoneObjectUpdater: Setting Radio Power to Off");
            this.mCi.setRadioPower(false, null);
        }
        switchVoiceRadioTech(newVoiceRadioTech);
        if (this.mResetModemOnRadioTechnologyChange && oldPowerState) {
            logd("phoneObjectUpdater: Resetting Radio");
            this.mCi.setRadioPower(oldPowerState, null);
        }
        this.mIccCardProxy.setVoiceRadioTech(newVoiceRadioTech);
        Intent intent = new Intent("android.intent.action.RADIO_TECHNOLOGY");
        intent.addFlags(536870912);
        intent.putExtra("phoneName", getPhoneName());
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhoneId);
        ActivityManagerNative.broadcastStickyIntent(intent, null, -1);
    }

    private void switchVoiceRadioTech(int newVoiceRadioTech) {
        if (OemFeature.FEATURE_CALL_STABILITY && (this.mCT.isOemInEcm() || this.mCT.isOemInEmergencyCall())) {
            Rlog.d(LOG_TAG, "Switching Voice Phone :blocked");
            return;
        }
        logd("Switching Voice Phone : " + getPhoneName() + " >>> " + (ServiceState.isGsm(newVoiceRadioTech) ? "GSM" : "CDMA"));
        if (ServiceState.isCdma(newVoiceRadioTech)) {
            if (isCdmaWithoutLteCard()) {
                switchPhoneType(2);
            } else {
                switchPhoneType(6);
            }
        } else if (ServiceState.isGsm(newVoiceRadioTech)) {
            switchPhoneType(1);
        } else {
            loge("deleteAndCreatePhone: newVoiceRadioTech=" + newVoiceRadioTech + " is not CDMA or GSM (error) - aborting!");
        }
    }

    public IccSmsInterfaceManager getIccSmsInterfaceManager() {
        return this.mIccSmsInterfaceManager;
    }

    public void updatePhoneObject(int voiceRadioTech) {
        logd("updatePhoneObject: radioTechnology=" + voiceRadioTech);
        sendMessage(obtainMessage(42, voiceRadioTech, 0, null));
    }

    public void setImsRegistrationState(boolean registered) {
        this.mSST.setImsRegistrationState(registered);
    }

    public boolean getIccRecordsLoaded() {
        return this.mIccCardProxy.getIccRecordsLoaded();
    }

    public IccCard getIccCard() {
        return this.mIccCardProxy;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("GsmCdmaPhone extends:");
        super.dump(fd, pw, args);
        pw.println(" mPrecisePhoneType=" + this.mPrecisePhoneType);
        pw.println(" mCT=" + this.mCT);
        pw.println(" mSST=" + this.mSST);
        pw.println(" mPendingMMIs=" + this.mPendingMMIs);
        pw.println(" mIccPhoneBookIntManager=" + this.mIccPhoneBookIntManager);
        pw.println(" mCdmaSSM=" + this.mCdmaSSM);
        pw.println(" mCdmaSubscriptionSource=" + this.mCdmaSubscriptionSource);
        pw.println(" mEriManager=" + this.mEriManager);
        pw.println(" mWakeLock=" + this.mWakeLock);
        pw.println(" mIsPhoneInEcmState=" + this.mIsPhoneInEcmState);
        pw.println(" mCarrierOtaSpNumSchema=" + this.mCarrierOtaSpNumSchema);
        if (!isPhoneTypeGsm()) {
            pw.println(" getCdmaEriIconIndex()=" + getCdmaEriIconIndex());
            pw.println(" getCdmaEriIconMode()=" + getCdmaEriIconMode());
            pw.println(" getCdmaEriText()=" + getCdmaEriText());
            pw.println(" isMinInfoReady()=" + isMinInfoReady());
        }
        pw.println(" isCspPlmnEnabled()=" + isCspPlmnEnabled());
        pw.flush();
        pw.println("++++++++++++++++++++++++++++++++");
        try {
            this.mIccCardProxy.dump(fd, pw, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        pw.flush();
        pw.println("++++++++++++++++++++++++++++++++");
    }

    public boolean setOperatorBrandOverride(String brand) {
        if (this.mUiccController == null) {
            return false;
        }
        UiccCard card = this.mUiccController.getUiccCard(getPhoneId());
        if (card == null) {
            return false;
        }
        boolean status = card.setOperatorBrandOverride(brand);
        if (status) {
            IccRecords iccRecords = (IccRecords) this.mIccRecords.get();
            if (iccRecords != null) {
                TelephonyManager.from(this.mContext).setSimOperatorNameForPhone(getPhoneId(), iccRecords.getServiceProviderName());
            }
            if (this.mSST != null) {
                this.mSST.pollState();
            }
        }
        return status;
    }

    private String getOperatorNumeric() {
        Object obj = null;
        String operatorNumeric = null;
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r != null) {
                return r.getOperatorNumeric();
            }
            return null;
        }
        IccRecords curIccRecords = null;
        if (this.mCdmaSubscriptionSource == 1) {
            operatorNumeric = SystemProperties.get(PROPERTY_CDMA_HOME_OPERATOR_NUMERIC);
        } else if (this.mCdmaSubscriptionSource == 0) {
            curIccRecords = this.mSimRecords;
            if (curIccRecords != null) {
                operatorNumeric = curIccRecords.getOperatorNumeric();
            } else {
                curIccRecords = (IccRecords) this.mIccRecords.get();
                if (curIccRecords != null && (curIccRecords instanceof RuimRecords)) {
                    operatorNumeric = ((RuimRecords) curIccRecords).getRUIMOperatorNumeric();
                }
            }
        }
        if (operatorNumeric == null) {
            StringBuilder append = new StringBuilder().append("getOperatorNumeric: Cannot retrieve operatorNumeric: mCdmaSubscriptionSource = ").append(this.mCdmaSubscriptionSource).append(" mIccRecords = ");
            if (curIccRecords != null) {
                obj = Boolean.valueOf(curIccRecords.getRecordsLoaded());
            }
            loge(append.append(obj).toString());
        }
        logd("getOperatorNumeric: mCdmaSubscriptionSource = " + this.mCdmaSubscriptionSource + " operatorNumeric = " + operatorNumeric);
        return operatorNumeric;
    }

    public void notifyEcbmTimerReset(Boolean flag) {
        this.mEcmTimerResetRegistrants.notifyResult(flag);
    }

    public void registerForEcmTimerReset(Handler h, int what, Object obj) {
        this.mEcmTimerResetRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForEcmTimerReset(Handler h) {
        this.mEcmTimerResetRegistrants.remove(h);
    }

    public void setVoiceMessageWaiting(int line, int countWaiting) {
        if (isPhoneTypeGsm()) {
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r != null) {
                r.setVoiceMessageWaiting(line, countWaiting);
                return;
            } else {
                logd("SIM Records not found, MWI not updated");
                return;
            }
        }
        setVoiceMessageCount(countWaiting);
    }

    private void logd(String s) {
        Rlog.d(LOG_TAG, "[GsmCdmaPhone] " + s);
    }

    private void loge(String s) {
        Rlog.e(LOG_TAG, "[GsmCdmaPhone] " + s);
    }

    public boolean isUtEnabled() {
        Phone imsPhone = this.mImsPhone;
        if (imsPhone != null) {
            return imsPhone.isUtEnabled();
        }
        logd("isUtEnabled: called for GsmCdma");
        return false;
    }

    public String getDtmfToneDelayKey() {
        if (isPhoneTypeGsm()) {
            return "gsm_dtmf_tone_delay_int";
        }
        return "cdma_dtmf_tone_delay_int";
    }

    public WakeLock getWakeLock() {
        return this.mWakeLock;
    }

    public void hangupAll() throws CallStateException {
        this.mCT.hangupAll();
    }

    public void registerForCrssSuppServiceNotification(Handler h, int what, Object obj) {
        this.mCallRelatedSuppSvcRegistrants.addUnique(h, what, obj);
        if (this.mCachedCrssn != null) {
            this.mCallRelatedSuppSvcRegistrants.notifyRegistrants(this.mCachedCrssn);
            this.mCachedCrssn = null;
        }
    }

    public void unregisterForCrssSuppServiceNotification(Handler h) {
        this.mCallRelatedSuppSvcRegistrants.remove(h);
        this.mCachedCrssn = null;
    }

    public Connection dial(List<String> numbers, int videoState) throws CallStateException {
        boolean imsUseEnabled = false;
        Phone imsPhone = this.mImsPhone;
        if (ImsManager.isVolteEnabledByPlatform(this.mContext) && ImsManager.isEnhanced4gLteModeSettingEnabledByUser(this.mContext)) {
            imsUseEnabled = ImsManager.isNonTtyOrTtyOnVolteEnabled(this.mContext);
        }
        if (imsUseEnabled) {
            if (imsPhone != null) {
                Rlog.w(LOG_TAG, "service state = " + imsPhone.getServiceState().getState());
            }
            if (imsUseEnabled && imsPhone != null && imsPhone.getServiceState().getState() == 0) {
                try {
                    Rlog.d(LOG_TAG, "Trying IMS PS conference call");
                    return imsPhone.dial(numbers, videoState);
                } catch (CallStateException e) {
                    Rlog.d(LOG_TAG, "IMS PS conference call exception " + e);
                    if (!Phone.CS_FALLBACK.equals(e.getMessage())) {
                        CallStateException ce = new CallStateException(e.getMessage());
                        ce.setStackTrace(e.getStackTrace());
                        throw ce;
                    }
                }
            }
            return null;
        }
        Rlog.w(LOG_TAG, "IMS is disabled and can not dial conference call directly.");
        return null;
    }

    private int getMainCapabilityPhoneId() {
        int phoneId = SystemProperties.getInt("persist.radio.simswitch", 1) - 1;
        if (phoneId < 0 || phoneId >= TelephonyManager.getDefault().getPhoneCount()) {
            return -1;
        }
        return phoneId;
    }

    public void doGeneralSimAuthentication(int sessionId, int mode, int tag, String param1, String param2, Message result) {
        if (isPhoneTypeGsm()) {
            this.mCi.doGeneralSimAuthentication(sessionId, mode, tag, param1, param2, result);
        }
    }

    public String getMvnoMatchType() {
        String type = UsimPBMemInfo.STRING_NOT_SET;
        if (isPhoneTypeGsm()) {
            if (this.mIccRecords.get() != null) {
                type = ((IccRecords) this.mIccRecords.get()).getMvnoMatchType();
            }
            logd("getMvnoMatchType: Type = " + type);
        }
        return type;
    }

    public String getMvnoPattern(String type) {
        String pattern = UsimPBMemInfo.STRING_NOT_SET;
        if (!isPhoneTypeGsm() || this.mIccRecords.get() == null) {
            return pattern;
        }
        if (type.equals("spn")) {
            return ((IccRecords) this.mIccRecords.get()).getSpNameInEfSpn();
        }
        if (type.equals("imsi")) {
            return ((IccRecords) this.mIccRecords.get()).isOperatorMvnoForImsi();
        }
        if (type.equals("pnn")) {
            return ((IccRecords) this.mIccRecords.get()).isOperatorMvnoForEfPnn();
        }
        if (type.equals("gid")) {
            return ((IccRecords) this.mIccRecords.get()).getGid1();
        }
        logd("getMvnoPattern: Wrong type = " + type);
        return pattern;
    }

    public int getCdmaSubscriptionActStatus() {
        return this.mCdmaSSM != null ? this.mCdmaSSM.getActStatus() : 0;
    }

    public boolean isGsmUtSupport() {
        if (!SystemProperties.get("persist.mtk_ims_support").equals("1") || !SystemProperties.get("persist.mtk_volte_support").equals("1") || !OperatorUtils.isGsmUtSupport(getOperatorNumeric()) || !isUsimCard()) {
            return false;
        }
        boolean isWfcEnable = this.mImsPhone != null ? this.mImsPhone.isWifiCallingEnabled() : false;
        boolean isWfcUtSupport = isWFCUtSupport();
        logd("in isGsmUtSupport isWfcEnable -->" + isWfcEnable + "isWfcUtSupport-->" + isWfcUtSupport);
        if (!isWfcEnable || isWfcUtSupport) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Missing block: B:14:0x0045, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isWFCUtSupport() {
        if (SystemProperties.get("ro.mtk_bsp_package").equals("1") || !SystemProperties.get("persist.mtk_ims_support").equals("1") || !SystemProperties.get("persist.mtk_wfc_support").equals("1") || isOp(OPID.OP11) || isOp(OPID.OP15)) {
            return false;
        }
        return true;
    }

    private boolean isUsimCard() {
        if (isPhoneTypeGsm()) {
            boolean r = false;
            String iccCardType = PhoneFactory.getPhone(getPhoneId()).getIccCard().getIccCardType();
            if (iccCardType != null && iccCardType.equals("USIM")) {
                r = true;
            }
            Rlog.d(LOG_TAG, "isUsimCard: " + r + ", " + iccCardType);
            return r;
        }
        Object[] values = null;
        int slotId = SubscriptionManager.getSlotId(SubscriptionManager.getSubIdUsingPhoneId(getPhoneId()));
        if (slotId < 0 || slotId >= PROPERTY_RIL_FULL_UICC_TYPE.length) {
            return false;
        }
        String prop = SystemProperties.get(PROPERTY_RIL_FULL_UICC_TYPE[slotId], UsimPBMemInfo.STRING_NOT_SET);
        if (!prop.equals(UsimPBMemInfo.STRING_NOT_SET) && prop.length() > 0) {
            values = prop.split(",");
        }
        Rlog.d(LOG_TAG, "isUsimCard PhoneId = " + getPhoneId() + " cardType = " + Arrays.toString(values));
        if (values == null) {
            return false;
        }
        for (String s : values) {
            if (s.equals("USIM")) {
                return true;
            }
        }
        return false;
    }

    public boolean isOpNotSupportOCB(String facility) {
        boolean r = false;
        boolean isOcb = false;
        if (facility.equals(CommandsInterface.CB_FACILITY_BAOC) || facility.equals(CommandsInterface.CB_FACILITY_BAOIC) || facility.equals(CommandsInterface.CB_FACILITY_BAOICxH)) {
            isOcb = true;
        }
        if (isOcb && isOp(OPID.OP01)) {
            r = true;
        }
        Rlog.d(LOG_TAG, "isOpNotSupportOCB: " + r + ", facility=" + facility);
        return r;
    }

    private boolean isOp(OPID id) {
        return OperatorUtils.isOperator(getOperatorNumeric(), id);
    }

    private boolean isOpTbcwWithCS(int phoneId) {
        boolean r = false;
        if (OperatorUtils.isNotSupportXcap(getOperatorNumeric()) && !OperatorUtils.isNotSupportXcapButUseTBCW(getOperatorNumeric())) {
            r = true;
        }
        Rlog.d(LOG_TAG, "isOpTbcwWithCS: " + r);
        return r;
    }

    public boolean isOpTbClir() {
        boolean r = false;
        if (OperatorUtils.isTbClir(getOperatorNumeric())) {
            r = true;
        }
        Rlog.d(LOG_TAG, "isOpTbClir: " + r);
        return r;
    }

    public void setServiceClass(int serviceClass) {
        Rlog.d(LOG_TAG, "setServiceClass: " + serviceClass);
        SystemProperties.set(SS_SERVICE_CLASS_PROP, String.valueOf(serviceClass));
    }

    public boolean isOpNwCW() {
        boolean r = false;
        if (isOp(OPID.OP50) || isOp(OPID.OP07) || isOp(OPID.OP137) || isOp(OPID.OP156)) {
            r = true;
        }
        Rlog.d(LOG_TAG, "isOpNwCW():" + r);
        return r;
    }

    public boolean isEnableXcapHttpResponse409() {
        boolean r = false;
        if (isOp(OPID.OP05)) {
            r = true;
        }
        Rlog.d(LOG_TAG, "isEnableXcapHttpResponse409: " + r);
        return r;
    }

    public boolean isOpTransferXcap404() {
        boolean r = false;
        if (isOp(OPID.OP05)) {
            r = true;
        }
        Rlog.d(LOG_TAG, "isOpTransferXcap404: " + r);
        return r;
    }

    public boolean isOpNotSupportCallIdentity() {
        boolean r = false;
        if (isOp(OPID.OP01) || isOp(OPID.OP02)) {
            r = true;
        }
        Rlog.d(LOG_TAG, "isOpNotSupportCallIdentity: " + r);
        return r;
    }

    public boolean isOpReregisterForCF() {
        boolean r = false;
        if (isOp(OPID.OP08)) {
            r = true;
        }
        Rlog.d(LOG_TAG, "isOpReregisterForCF: " + r);
        return r;
    }

    private boolean isIccCardMncMccAvailable(int phoneId) {
        boolean z = true;
        IccRecords iccRecords = UiccController.getInstance().getIccRecords(phoneId, 1);
        if (iccRecords != null) {
            String mccMnc = iccRecords.getOperatorNumeric();
            Rlog.d(LOG_TAG, "isIccCardMncMccAvailable(): phone id : " + phoneId + ", mccMnc: " + mccMnc);
            if (mccMnc == null) {
                z = false;
            }
            return z;
        }
        Rlog.d(LOG_TAG, "isIccCardMncMccAvailable(): phone id : " + phoneId + ", false");
        return false;
    }

    public boolean isSupportSaveCFNumber() {
        boolean r = false;
        if (isOp(OPID.OP07)) {
            r = true;
        }
        Rlog.d(LOG_TAG, "isSupportSaveCFNumber: " + r);
        return r;
    }

    public void clearCFSharePreference(int cfReason) {
        String key;
        switch (cfReason) {
            case 1:
                key = "CFB_" + String.valueOf(this.mPhoneId);
                break;
            case 2:
                key = "CFNR_" + String.valueOf(this.mPhoneId);
                break;
            case 3:
                key = "CFNRC_" + String.valueOf(this.mPhoneId);
                break;
            default:
                Rlog.e(LOG_TAG, "No need to store cfreason: " + cfReason);
                return;
        }
        Rlog.e(LOG_TAG, "Read to clear the key: " + key);
        Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        editor.remove(key);
        if (editor.commit()) {
            Rlog.e(LOG_TAG, "Commit the removal of CF preference: " + key);
        } else {
            Rlog.e(LOG_TAG, "failed to commit the removal of CF preference: " + key);
        }
    }

    public boolean applyCFSharePreference(int cfReason, String setNumber) {
        String key;
        switch (cfReason) {
            case 1:
                key = "CFB_" + String.valueOf(this.mPhoneId);
                break;
            case 2:
                key = "CFNR_" + String.valueOf(this.mPhoneId);
                break;
            case 3:
                key = "CFNRC_" + String.valueOf(this.mPhoneId);
                break;
            default:
                Rlog.d(LOG_TAG, "No need to store cfreason: " + cfReason);
                return false;
        }
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r == null) {
            Rlog.d(LOG_TAG, "No iccRecords");
            return false;
        }
        String currentImsi = r.getIMSI();
        if (currentImsi == null || currentImsi.isEmpty()) {
            Rlog.d(LOG_TAG, "currentImsi is empty");
            return false;
        } else if (setNumber == null || setNumber.isEmpty()) {
            Rlog.d(LOG_TAG, "setNumber is empty");
            return false;
        } else {
            Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
            String content = currentImsi + ";" + setNumber;
            if (content == null || content.isEmpty()) {
                Rlog.e(LOG_TAG, "imsi or content are empty or null.");
                return false;
            }
            Rlog.e(LOG_TAG, "key: " + key);
            Rlog.e(LOG_TAG, "content: " + content);
            editor.putString(key, content);
            editor.apply();
            return true;
        }
    }

    public String getCFPreviousDialNumber(int cfReason) {
        String key;
        switch (cfReason) {
            case 1:
                key = "CFB_" + String.valueOf(this.mPhoneId);
                break;
            case 2:
                key = "CFNR_" + String.valueOf(this.mPhoneId);
                break;
            case 3:
                key = "CFNRC_" + String.valueOf(this.mPhoneId);
                break;
            default:
                Rlog.d(LOG_TAG, "No need to do the reason: " + cfReason);
                return null;
        }
        Rlog.d(LOG_TAG, "key: " + key);
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r == null) {
            Rlog.d(LOG_TAG, "No iccRecords");
            return null;
        }
        String currentImsi = r.getIMSI();
        if (currentImsi == null || currentImsi.isEmpty()) {
            Rlog.d(LOG_TAG, "currentImsi is empty");
            return null;
        }
        Rlog.d(LOG_TAG, "currentImsi: " + currentImsi);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        String info = sp.getString(key, null);
        if (info == null) {
            Rlog.d(LOG_TAG, "Sharedpref not with: " + key);
            return null;
        }
        String[] infoAry = info.split(";");
        if (infoAry == null || infoAry.length < 2) {
            Rlog.d(LOG_TAG, "infoAry.length < 2");
            return null;
        }
        String imsi = infoAry[0];
        String number = infoAry[1];
        if (imsi == null || imsi.isEmpty()) {
            Rlog.d(LOG_TAG, "Sharedpref imsi is empty.");
            return null;
        } else if (number == null || number.isEmpty()) {
            Rlog.d(LOG_TAG, "Sharedpref number is empty.");
            return null;
        } else {
            Rlog.d(LOG_TAG, "Sharedpref imsi: " + imsi);
            Rlog.d(LOG_TAG, "Sharedpref number: " + number);
            if (currentImsi.equals(imsi)) {
                Rlog.d(LOG_TAG, "Get dial number from sharepref: " + number);
                return number;
            }
            Editor editor = sp.edit();
            editor.remove(key);
            if (!editor.commit()) {
                Rlog.e(LOG_TAG, "failed to commit the removal of CF preference: " + key);
            }
            return null;
        }
    }

    public boolean queryCFUAgainAfterSet() {
        boolean r = false;
        if (isOp(OPID.OP05) || isOp(OPID.OP11)) {
            r = true;
        }
        Rlog.d(LOG_TAG, "queryCFUAgainAfterSet: " + r);
        return r;
    }

    public void refreshSpnDisplay() {
        this.mSST.refreshSpnDisplay();
    }

    public int getNetworkHideState() {
        if (this.mSST.dontUpdateNetworkStateFlag) {
            return 1;
        }
        return this.mSST.mSS.getState();
    }

    public String getLocatedPlmn() {
        return this.mSST.getLocatedPlmn();
    }

    private boolean isCdmaWithoutLteCard() {
        ITelephonyEx telephonyEx = Stub.asInterface(ServiceManager.getService("phoneEx"));
        int iccFamily = 1;
        if (telephonyEx != null) {
            try {
                iccFamily = telephonyEx.getIccAppFamily(getPhoneId());
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        } else {
            loge("get telephonyEx failed!!");
        }
        if (iccFamily == 2) {
            return true;
        }
        return false;
    }

    public boolean isNotSupportUtToCS() {
        boolean r = false;
        if (((SystemProperties.get("persist.mtk_ct_volte_support").equals("1") && isOp(OPID.OP09) && isUsimCard()) || isOp(OPID.OP117)) && !getServiceState().getRoaming()) {
            r = true;
        }
        Rlog.d(LOG_TAG, "isNotSupportUtToCS: " + r);
        return r;
    }

    private boolean is93MDSupport() {
        if (SystemProperties.get("ro.md_auto_setup_ims").equals("1")) {
            return true;
        }
        return false;
    }

    private boolean isUtError(Error error) {
        if (error == Error.OEM_ERROR_3 || error == Error.OEM_ERROR_4 || error == Error.OEM_ERROR_5 || error == Error.OEM_ERROR_6 || error == Error.UT_XCAP_409_CONFLICT || error == Error.UT_UNKNOWN_HOST) {
            return true;
        }
        return false;
    }

    private boolean isGsmSsPrefer() {
        if ((SystemProperties.get("persist.mtk_ct_volte_support").equals("1") && isOp(OPID.OP09)) || isOp(OPID.OP117)) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Missing block: B:26:0x0069, code:
            if (r5.mSST.mSS.getState() != 0) goto L_0x0055;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized boolean checkEventQueryCfuReady() {
        boolean checkEQC;
        checkEQC = true;
        SubscriptionManager subMgr = SubscriptionManager.from(this.mContext);
        SubscriptionInfo mySubInfo = null;
        if (subMgr != null) {
            mySubInfo = subMgr.getActiveSubscriptionInfo(getSubId());
        }
        if (!isIccCardMncMccAvailable(getPhoneId())) {
            Rlog.d(LOG_TAG, "checkEventQueryCfu, MCCMNC is not ready");
            checkEQC = false;
        }
        if (mySubInfo == null || mySubInfo.getIccId() == null) {
            Rlog.d(LOG_TAG, "checkEventQueryCfu, IccId is not ready");
            checkEQC = false;
        }
        if (this.mIccRecords.get() == null) {
            Rlog.d(LOG_TAG, "checkEventQueryCfu, IccRecord is not ready");
            checkEQC = false;
        }
        if (!(this.mSST == null || this.mSST.mSS == null)) {
        }
        Rlog.d(LOG_TAG, "checkEventQueryCfu, CS Service is not ready");
        checkEQC = false;
        return checkEQC;
    }

    /* JADX WARNING: Missing block: B:31:0x00d6, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void handleSubInfoChange() {
        SubscriptionManager subMgr = SubscriptionManager.from(this.mContext);
        SubscriptionInfo mySubInfo = null;
        if (subMgr != null) {
            mySubInfo = subMgr.getActiveSubscriptionInfo(getSubId());
        }
        String mySettingName = CFU_QUERY_ICCID_PROP + getPhoneId();
        String oldIccId = SystemProperties.get(mySettingName, UsimPBMemInfo.STRING_NOT_SET);
        String defaultQueryCfuMode = PhoneConstants.CFU_QUERY_TYPE_DEF_VALUE;
        if (this.mSupplementaryServiceExt != null) {
            defaultQueryCfuMode = this.mSupplementaryServiceExt.getOpDefaultQueryCfuMode();
            Rlog.d(LOG_TAG, "defaultQueryCfuMode = " + defaultQueryCfuMode);
        }
        String cfuSetting = SystemProperties.get("persist.radio.cfu.querytype", defaultQueryCfuMode);
        if (!isIccCardMncMccAvailable(getPhoneId())) {
            return;
        }
        if (mySubInfo != null) {
            if (mySubInfo.getIccId() != null) {
                if (cfuSetting.equals("2")) {
                    Rlog.d(LOG_TAG, "Always query CFU.");
                    if (!(this.mSST == null || this.mSST.mSS == null || this.mSST.mSS.getState() != 0)) {
                        this.needQueryCfu = true;
                        sendMessage(obtainMessage(2002));
                    }
                } else if (!mySubInfo.getIccId().equals(oldIccId)) {
                    Rlog.d(LOG_TAG, " mySubId " + getSubId() + " mySettingName " + Rlog.pii(SDBG, mySettingName) + " old iccid : " + Rlog.pii(SDBG, oldIccId) + " new iccid : " + Rlog.pii(SDBG, mySubInfo.getIccId()) + " phoneId : " + getPhoneId());
                    SystemProperties.set(mySettingName, mySubInfo.getIccId());
                    SystemProperties.set(CFU_QUERY_SIM_CHANGED_PROP + getPhoneId(), "1");
                    handleSuppServInitIfSimChanged();
                } else if (mySubInfo.getIccId().equals(oldIccId)) {
                    Rlog.d(LOG_TAG, "oldIccId = " + oldIccId + " , phoneId : " + getPhoneId());
                    if (this.mIccRecords.get() == null) {
                        Rlog.d(LOG_TAG, "skip sending EVENT_QUERY_CFU due to IccRecords null.");
                    } else if (!(this.mSST == null || this.mSST.mSS == null || this.mSST.mSS.getState() != 0)) {
                        this.needQueryCfu = true;
                        Rlog.d(LOG_TAG, "handleSubInfoChange, Send EVENT_QUERY_CFU, phoneId = " + getPhoneId());
                        sendMessage(obtainMessage(2002));
                    }
                }
                Rlog.d(LOG_TAG, "handleSubInfoChange: mTbcwMode = " + this.mTbcwMode);
                if (this.mTbcwMode == 0 && isOpTbcwWithCS(getPhoneId())) {
                    setTbcwMode(4);
                    setTbcwToEnabledOnIfDisabled();
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:24:0x0112, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void handleSuppServInitIfSimChanged() {
        if (this.mIccRecords.get() == null) {
            Rlog.d(LOG_TAG, "skip handleSuppServInitIfSimChanged due to IccRecords null.");
            return;
        }
        String isChanged = SystemProperties.get(CFU_QUERY_SIM_CHANGED_PROP + getPhoneId(), "0");
        Rlog.d(LOG_TAG, "handleSuppServInitIfSimChanged: isChanged = " + isChanged);
        if (isChanged.equals("1")) {
            this.needQueryCfu = true;
            setCsFallbackStatus(0);
            setTbcwMode(0);
            setSystemProperty(SS_CW_TBCW_EVER_ENABLE_PROP, "0");
            setSystemProperty("persist.radio.terminal-based.cw", "disabled_tbcw");
            saveTimeSlot(null);
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
            if (sp.getInt(Phone.CLIR_KEY + getPhoneId(), -1) != -1) {
                Editor editor = sp.edit();
                editor.remove(Phone.CLIR_KEY + getPhoneId());
                if (!editor.commit()) {
                    Rlog.e(LOG_TAG, "failed to commit the removal of CLIR preference");
                }
            }
            setSystemProperty("persist.radio.ut.cfu.mode", "disabled_ut_cfu_mode");
            if (!(this.mSST == null || this.mSST.mSS == null || this.mSST.mSS.getState() != 0)) {
                Rlog.d(LOG_TAG, "handleSuppServInitIfSimChanged, Send EVENT_QUERY_CFU, phoneId = " + getPhoneId());
                sendMessage(obtainMessage(2002));
            }
        }
    }

    public CommandException checkUiccApplicationForFacilityLock() {
        if (this.mUiccApplication.get() != null) {
            return null;
        }
        Rlog.d(LOG_TAG, "FacilityLockForServiceClass: mUiccApplication.get() == null");
        if (this.mCi.getRadioState().isAvailable() && this.mCi.getRadioState().isOn()) {
            return new CommandException(Error.GENERIC_FAILURE);
        }
        Rlog.d(LOG_TAG, "FacilityLockForServiceClass: radio not available");
        return new CommandException(Error.RADIO_NOT_AVAILABLE);
    }

    public AsyncResult getCachedCrss() {
        Rlog.e(LOG_TAG, "getCachedCrss()");
        return this.mCachedCrssn;
    }

    public void resetCachedCrss() {
        Rlog.e(LOG_TAG, "ResetCachedCrss()");
        this.mCachedCrssn = null;
    }
}
