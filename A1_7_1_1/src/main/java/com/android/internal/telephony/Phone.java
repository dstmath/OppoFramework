package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.LinkProperties;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.service.carrier.CarrierIdentifier;
import android.telecom.VideoProfile;
import android.telephony.CarrierConfigManager;
import android.telephony.CellIdentityCdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.RadioAccessFamily;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.VoLteServiceState;
import android.text.TextUtils;
import com.android.ims.ImsManager;
import com.android.internal.telephony.Call.SrvccState;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.PhoneConstants.DataState;
import com.android.internal.telephony.PhoneConstants.State;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.test.SimulatedRadioControl;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppType;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IsimRecords;
import com.android.internal.telephony.uicc.RuimRecords;
import com.android.internal.telephony.uicc.SIMRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.uicc.UsimServiceTable;
import com.android.internal.telephony.uicc.UsimServiceTable.UsimService;
import com.mediatek.internal.telephony.FemtoCellInfo;
import com.mediatek.internal.telephony.NetworkInfoWithAcT;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import com.mediatek.internal.telephony.worldphone.WorldPhoneUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;

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
    	at jadx.core.utils.BlockUtils.collectAllInsns(BlockUtils.java:556)
    	at jadx.core.dex.visitors.ClassModifier.removeBridgeMethod(ClassModifier.java:197)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticMethods(ClassModifier.java:135)
    	at jadx.core.dex.visitors.ClassModifier.lambda$visit$0(ClassModifier.java:49)
    	at java.util.ArrayList.forEach(ArrayList.java:1251)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:49)
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
public abstract class Phone extends Handler implements PhoneInternalInterface {
    public static final String ACT_TYPE_GSM = "0";
    public static final String ACT_TYPE_LTE = "7";
    public static final String ACT_TYPE_UTRAN = "2";
    private static final String CDMA_NON_ROAMING_LIST_OVERRIDE_PREFIX = "cdma_non_roaming_list_";
    private static final String CDMA_ROAMING_LIST_OVERRIDE_PREFIX = "cdma_roaming_list_";
    private static final String CFU_TIME_SLOT = "persist.radio.cfu.timeslot.";
    public static final String CF_ENABLED_VIDEO = "video_cf_enabled";
    public static final String CF_ID = "cf_id_key";
    public static final String CF_STATUS = "cf_status_key";
    public static final String CLIR_KEY = "clir_key";
    protected static final int CMD_OPPO_SET_SAR_RF_STATE = 5;
    public static final String CS_FALLBACK = "cs_fallback";
    public static final String DATA_DISABLED_ON_BOOT_KEY = "disabled_on_boot_key";
    protected static final boolean DBG = false;
    private static final int DEFAULT_REPORT_INTERVAL_MS = 200;
    private static final String DNS_SERVER_CHECK_DISABLED_KEY = "dns_server_check_disabled_key";
    protected static final int EVENT_CALL_RING = 14;
    private static final int EVENT_CALL_RING_CONTINUE = 15;
    protected static final int EVENT_CARRIER_CONFIG_CHANGED = 43;
    protected static final int EVENT_CDMA_SUBSCRIPTION_SOURCE_CHANGED = 27;
    protected static final int EVENT_CFU_IND = 101;
    protected static final int EVENT_CFU_QUERY_TIMEOUT = 102;
    protected static final int EVENT_CHARGING_STOP = 103;
    private static final int EVENT_CHECK_FOR_NETWORK_AUTOMATIC = 38;
    protected static final int EVENT_CIPHER_INDICATION = 1000;
    private static final int EVENT_CONFIG_LCE = 37;
    protected static final int EVENT_CRSS_IND = 1002;
    protected static final int EVENT_EMERGENCY_CALLBACK_MODE_ENTER = 25;
    protected static final int EVENT_EXIT_EMERGENCY_CALLBACK_RESPONSE = 26;
    protected static final int EVENT_GET_BASEBAND_VERSION_DONE = 6;
    protected static final int EVENT_GET_CALL_FORWARD_DONE = 13;
    protected static final int EVENT_GET_CALL_FORWARD_TIME_SLOT_DONE = 109;
    protected static final int EVENT_GET_CALL_WAITING_DONE = 301;
    protected static final int EVENT_GET_CLIR_COMPLETE = 2004;
    protected static final int EVENT_GET_DEVICE_IDENTITY_DONE = 21;
    protected static final int EVENT_GET_FACILITY_LOCK_COMPLETE = 2006;
    protected static final int EVENT_GET_IMEISV_DONE = 10;
    protected static final int EVENT_GET_IMEI_DONE = 9;
    protected static final int EVENT_GET_RADIO_CAPABILITY = 35;
    private static final int EVENT_GET_SIM_STATUS_DONE = 11;
    private static final int EVENT_ICC_CHANGED = 30;
    protected static final int EVENT_ICC_RECORD_EVENTS = 29;
    protected static final int EVENT_IMS_UT_CSFB = 2001;
    protected static final int EVENT_IMS_UT_DONE = 2000;
    private static final int EVENT_INITIATE_SILENT_REDIAL = 32;
    protected static final int EVENT_LAST = 44;
    private static final int EVENT_MMI_DONE = 4;
    protected static final int EVENT_MTK_BASE = 1000;
    protected static final int EVENT_NV_READY = 23;
    protected static final int EVENT_QUERY_CFU = 2002;
    protected static final int EVENT_RADIO_AVAILABLE = 1;
    private static final int EVENT_RADIO_NOT_AVAILABLE = 33;
    protected static final int EVENT_RADIO_OFF_OR_NOT_AVAILABLE = 8;
    protected static final int EVENT_RADIO_ON = 5;
    protected static final int EVENT_REGISTERED_TO_NETWORK = 19;
    protected static final int EVENT_REQUEST_VOICE_RADIO_TECH_DONE = 40;
    protected static final int EVENT_RIL_CONNECTED = 41;
    protected static final int EVENT_RUIM_RECORDS_LOADED = 22;
    protected static final int EVENT_SET_CALL_FORWARD_DONE = 12;
    protected static final int EVENT_SET_CALL_FORWARD_TIME_SLOT_DONE = 110;
    protected static final int EVENT_SET_CALL_WAITING_DONE = 302;
    protected static final int EVENT_SET_CLIR_COMPLETE = 18;
    private static final int EVENT_SET_ENHANCED_VP = 24;
    protected static final int EVENT_SET_FACILITY_LOCK_COMPLETE = 2005;
    protected static final int EVENT_SET_NETWORK_AUTOMATIC = 28;
    private static final int EVENT_SET_NETWORK_AUTOMATIC_COMPLETE = 17;
    private static final int EVENT_SET_NETWORK_MANUAL_COMPLETE = 16;
    protected static final int EVENT_SET_ROAMING_PREFERENCE_DONE = 44;
    protected static final int EVENT_SET_VM_NUMBER_DONE = 20;
    protected static final int EVENT_SIM_RECORDS_LOADED = 3;
    protected static final int EVENT_SPEECH_CODEC_INFO = 1001;
    private static final int EVENT_SRVCC_STATE_CHANGED = 31;
    protected static final int EVENT_SS = 36;
    protected static final int EVENT_SSN = 2;
    private static final int EVENT_UNSOL_OEM_HOOK_RAW = 34;
    protected static final int EVENT_UNSOL_RADIO_CAPABILITY_CHANGED = 111;
    protected static final int EVENT_UPDATE_PHONE_OBJECT = 42;
    protected static final int EVENT_USSD = 7;
    protected static final int EVENT_USSI_CSFB = 2003;
    protected static final int EVENT_VOICE_RADIO_TECH_CHANGED = 39;
    public static final String EXTRA_KEY_ALERT_MESSAGE = "alertMessage";
    public static final String EXTRA_KEY_ALERT_SHOW = "alertShow";
    public static final String EXTRA_KEY_ALERT_TITLE = "alertTitle";
    public static final String EXTRA_KEY_NOTIFICATION_MESSAGE = "notificationMessage";
    private static final String GSM_NON_ROAMING_LIST_OVERRIDE_PREFIX = "gsm_non_roaming_list_";
    private static final String GSM_ROAMING_LIST_OVERRIDE_PREFIX = "gsm_roaming_list_";
    private static final boolean LCE_PULL_MODE = true;
    private static final String LOG_TAG = "Phone";
    public static final String LTE_INDICATOR = "4G";
    private static final boolean MTK_VZW_SUPPORT = false;
    public static final String NETWORK_SELECTION_KEY = "network_selection_key";
    public static final String NETWORK_SELECTION_NAME_KEY = "network_selection_name_key";
    public static final String NETWORK_SELECTION_SHORT_KEY = "network_selection_short_key";
    private static final String OEM_ISAUTO_ANSWER = "oem_is_auto_answer";
    public static final int OEM_PRODUCT_15113 = 3;
    public static final int OEM_PRODUCT_15131 = 1;
    public static final int OEM_PRODUCT_15311 = 100;
    public static final int OEM_PRODUCT_15331 = 101;
    public static final int OEM_PRODUCT_16021 = 4;
    public static final int OEM_PRODUCT_16321 = 102;
    public static final int OEM_PRODUCT_16391 = 103;
    public static final int OEM_PRODUCT_17031 = 5;
    public static final int OEM_PRODUCT_17071 = 6;
    public static final int OEM_PRODUCT_17101 = 7;
    public static final int OEM_PRODUCT_17307 = 109;
    public static final int OEM_PRODUCT_17309 = 110;
    public static final int OEM_PRODUCT_17310 = 111;
    public static final int OEM_PRODUCT_17321 = 105;
    public static final int OEM_PRODUCT_17351 = 104;
    public static final int OEM_PRODUCT_17371 = 106;
    public static final int OEM_PRODUCT_17373 = 107;
    public static final int OEM_PRODUCT_17375 = 108;
    public static final int OEM_PRODUCT_NONE = 0;
    public static final int OEM_PRODUCT_R6031 = 2;
    protected static final int SAR_RF_STATE_DEFAULT = 0;
    protected static final int SAR_RF_STATE_PROXIMITY_ACQUIRED = 1;
    protected static final int SAR_RF_STATE_PROXIMITY_ACQUIRED_CHARGE_CALL = 7;
    protected static final int SAR_RF_STATE_PROXIMITY_ACQUIRED_HEADSET = 5;
    protected static final int SAR_RF_STATE_PROXIMITY_ACQUIRED_USB = 3;
    protected static final int SAR_RF_STATE_PROXIMITY_RELEASE = 2;
    protected static final int SAR_RF_STATE_PROXIMITY_RELEASE_CHARGE_CALL = 8;
    protected static final int SAR_RF_STATE_PROXIMITY_RELEASE_HEADSET = 6;
    protected static final int SAR_RF_STATE_PROXIMITY_RELEASE_USB = 4;
    public static final String UTRAN_INDICATOR = "3G";
    private static final String VM_COUNT = "vm_count_key";
    private static final String VM_ID = "vm_id_key";
    protected static final Object lockForRadioTechnologyChange = null;
    private final String mActionAttached;
    private final String mActionDetached;
    private int mCSFallbackMode;
    private int mCallRingContinueToken;
    private int mCallRingDelay;
    private CarrierSignalAgent mCarrierSignalAgent;
    protected final RegistrantList mCdmaCallAcceptedRegistrants;
    public CommandsInterface mCi;
    protected final RegistrantList mCipherIndicationRegistrants;
    protected final Context mContext;
    public DcTracker mDcTracker;
    protected final RegistrantList mDisconnectRegistrants;
    private boolean mDnsCheckDisabled;
    private boolean mDoesRilSendMultipleCallRing;
    protected final RegistrantList mEmergencyCallToggledRegistrants;
    private final RegistrantList mHandoverRegistrants;
    protected final AtomicReference<IccRecords> mIccRecords;
    private BroadcastReceiver mImsIntentReceiver;
    protected Phone mImsPhone;
    private boolean mImsServiceReady;
    private final RegistrantList mIncomingRingRegistrants;
    private boolean mIsPendingSRVCC;
    protected boolean mIsVideoCapable;
    private boolean mIsVoiceCapable;
    private int mLceStatus;
    private Looper mLooper;
    protected final RegistrantList mMmiCompleteRegistrants;
    protected final RegistrantList mMmiRegistrants;
    private String mName;
    private final RegistrantList mNewRingingConnectionRegistrants;
    protected PhoneNotifier mNotifier;
    protected int mPhoneId;
    protected Registrant mPostDialHandler;
    private final RegistrantList mPreciseCallStateRegistrants;
    private final AtomicReference<RadioCapability> mRadioCapability;
    private int mRadioCapabilityRef;
    protected final RegistrantList mRadioOffOrNotAvailableRegistrants;
    private final RegistrantList mServiceStateRegistrants;
    protected final RegistrantList mSimRecordsLoadedRegistrants;
    protected SimulatedRadioControl mSimulatedRadioControl;
    public SmsStorageMonitor mSmsStorageMonitor;
    public SmsUsageMonitor mSmsUsageMonitor;
    private final RegistrantList mSpeechCodecInfoRegistrants;
    private SrvccState mSrvccState;
    protected final RegistrantList mSuppServiceFailedRegistrants;
    private int mSwitchState;
    protected TelephonyComponentFactory mTelephonyComponentFactory;
    private TelephonyTester mTelephonyTester;
    protected AtomicReference<UiccCardApplication> mUiccApplication;
    protected UiccController mUiccController;
    private boolean mUnitTestMode;
    protected final RegistrantList mUnknownConnectionRegistrants;
    private final RegistrantList mVideoCapabilityChangedRegistrants;
    protected int mVmCount;

    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public enum FeatureType {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.Phone.FeatureType.<clinit>():void, dex: 
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
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.Phone.FeatureType.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.Phone.FeatureType.<clinit>():void");
        }
    }

    private static class NetworkSelectMessage {
        public Message message;
        public String operatorAlphaLong;
        public String operatorAlphaShort;
        public String operatorNumeric;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.Phone.NetworkSelectMessage.<init>():void, dex: 
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
        private NetworkSelectMessage() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.Phone.NetworkSelectMessage.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.Phone.NetworkSelectMessage.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.Phone.NetworkSelectMessage.<init>(com.android.internal.telephony.Phone$NetworkSelectMessage):void, dex: 
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
        /* synthetic */ NetworkSelectMessage(com.android.internal.telephony.Phone.NetworkSelectMessage r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.telephony.Phone.NetworkSelectMessage.<init>(com.android.internal.telephony.Phone$NetworkSelectMessage):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.Phone.NetworkSelectMessage.<init>(com.android.internal.telephony.Phone$NetworkSelectMessage):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.Phone.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.Phone.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.Phone.<clinit>():void");
    }

    public abstract int getPhoneType();

    public abstract State getState();

    protected abstract void onUpdateIccAvailability();

    public abstract void sendEmergencyCallStateChange(boolean z);

    public abstract void setBroadcastEmergencyCallStateChanges(boolean z);

    public IccRecords getIccRecords() {
        return (IccRecords) this.mIccRecords.get();
    }

    public String getPhoneName() {
        return this.mName;
    }

    protected void setPhoneName(String name) {
        this.mName = name;
    }

    public String getNai() {
        return null;
    }

    public String getActionDetached() {
        return this.mActionDetached;
    }

    public String getActionAttached() {
        return this.mActionAttached;
    }

    public void setSystemProperty(String property, String value) {
        if (!getUnitTestMode()) {
            SystemProperties.set(property, value);
        }
    }

    public String getSystemProperty(String property, String defValue) {
        if (getUnitTestMode()) {
            return null;
        }
        return SystemProperties.get(property, defValue);
    }

    protected Phone(String name, PhoneNotifier notifier, Context context, CommandsInterface ci, boolean unitTestMode) {
        this(name, notifier, context, ci, unitTestMode, Integer.MAX_VALUE, TelephonyComponentFactory.getInstance());
    }

    protected Phone(String name, PhoneNotifier notifier, Context context, CommandsInterface ci, boolean unitTestMode, int phoneId, TelephonyComponentFactory telephonyComponentFactory) {
        this.mImsIntentReceiver = new BroadcastReceiver() {
            /* JADX WARNING: Missing block: B:21:0x00d2, code:
            return;
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onReceive(Context context, Intent intent) {
                Rlog.d(Phone.LOG_TAG, "mImsIntentReceiver: action " + intent.getAction());
                if (intent.hasExtra("android:phone_id")) {
                    int extraPhoneId = intent.getIntExtra("android:phone_id", -1);
                    Rlog.d(Phone.LOG_TAG, "mImsIntentReceiver: extraPhoneId = " + extraPhoneId);
                    if (extraPhoneId == -1 || extraPhoneId != Phone.this.getPhoneId()) {
                        return;
                    }
                }
                synchronized (Phone.lockForRadioTechnologyChange) {
                    Rlog.w(Phone.LOG_TAG, intent.getAction() + ", getSubId=" + Phone.this.getSubId() + ", getPhoneId=" + Phone.this.getPhoneId());
                    if (intent.getAction().equals("com.android.ims.IMS_SERVICE_UP")) {
                        if (SystemProperties.getInt("persist.ims.simulate", 0) != 1 || SystemProperties.getInt("persist.ims.phoneid", 0) == Phone.this.getPhoneId()) {
                            Phone.this.mImsServiceReady = true;
                            Phone.this.updateImsPhone();
                            ImsManager.updateImsServiceConfig(Phone.this.mContext, Phone.this.mPhoneId, false);
                        }
                    } else if (intent.getAction().equals("com.android.ims.IMS_SERVICE_DOWN")) {
                        Phone.this.mImsServiceReady = false;
                        Phone.this.updateImsPhone();
                    } else if (intent.getAction().equals("com.android.intent.action.IMS_CONFIG_CHANGED")) {
                        ImsManager.onProvisionedValueChanged(context, intent.getIntExtra("item", -1), intent.getStringExtra(ColorOSHolidayMode.STATES_VALUE));
                    }
                }
            }
        };
        this.mCSFallbackMode = 0;
        this.mRadioCapabilityRef = 0;
        this.mSwitchState = 0;
        this.mVmCount = 0;
        this.mIsVoiceCapable = true;
        this.mIsVideoCapable = false;
        this.mUiccController = null;
        this.mIccRecords = new AtomicReference();
        this.mUiccApplication = new AtomicReference();
        this.mImsServiceReady = false;
        this.mImsPhone = null;
        this.mRadioCapability = new AtomicReference();
        this.mLceStatus = -1;
        this.mPreciseCallStateRegistrants = new RegistrantList();
        this.mHandoverRegistrants = new RegistrantList();
        this.mNewRingingConnectionRegistrants = new RegistrantList();
        this.mIncomingRingRegistrants = new RegistrantList();
        this.mDisconnectRegistrants = new RegistrantList();
        this.mServiceStateRegistrants = new RegistrantList();
        this.mMmiCompleteRegistrants = new RegistrantList();
        this.mMmiRegistrants = new RegistrantList();
        this.mUnknownConnectionRegistrants = new RegistrantList();
        this.mSuppServiceFailedRegistrants = new RegistrantList();
        this.mRadioOffOrNotAvailableRegistrants = new RegistrantList();
        this.mSimRecordsLoadedRegistrants = new RegistrantList();
        this.mVideoCapabilityChangedRegistrants = new RegistrantList();
        this.mEmergencyCallToggledRegistrants = new RegistrantList();
        this.mCipherIndicationRegistrants = new RegistrantList();
        this.mSpeechCodecInfoRegistrants = new RegistrantList();
        this.mCdmaCallAcceptedRegistrants = new RegistrantList();
        this.mSrvccState = SrvccState.NONE;
        this.mIsPendingSRVCC = false;
        this.mPhoneId = phoneId;
        this.mName = name;
        this.mNotifier = notifier;
        this.mContext = context;
        this.mLooper = Looper.myLooper();
        this.mCi = ci;
        this.mCarrierSignalAgent = new CarrierSignalAgent(this);
        this.mActionDetached = getClass().getPackage().getName() + ".action_detached";
        this.mActionAttached = getClass().getPackage().getName() + ".action_attached";
        if (Build.IS_DEBUGGABLE) {
            this.mTelephonyTester = new TelephonyTester(this);
        }
        setUnitTestMode(unitTestMode);
        this.mDnsCheckDisabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(DNS_SERVER_CHECK_DISABLED_KEY, false);
        this.mCi.setOnCallRing(this, 14, null);
        this.mIsVoiceCapable = this.mContext.getResources().getBoolean(17956954);
        this.mDoesRilSendMultipleCallRing = SystemProperties.getBoolean("ro.telephony.call_ring.multiple", true);
        Rlog.d(LOG_TAG, "mDoesRilSendMultipleCallRing=" + this.mDoesRilSendMultipleCallRing);
        this.mCallRingDelay = SystemProperties.getInt("ro.telephony.call_ring.delay", 3000);
        Rlog.d(LOG_TAG, "mCallRingDelay=" + this.mCallRingDelay);
        if (getPhoneType() != 5) {
            Locale carrierLocale = getLocaleFromCarrierProperties(this.mContext);
            if (!(carrierLocale == null || TextUtils.isEmpty(carrierLocale.getCountry()))) {
                String country = carrierLocale.getCountry();
                try {
                    Global.getInt(this.mContext.getContentResolver(), "wifi_country_code");
                } catch (SettingNotFoundException e) {
                    ((WifiManager) this.mContext.getSystemService("wifi")).setCountryCode(country, false);
                }
            }
            this.mTelephonyComponentFactory = telephonyComponentFactory;
            this.mSmsStorageMonitor = this.mTelephonyComponentFactory.makeSmsStorageMonitor(this);
            this.mSmsUsageMonitor = this.mTelephonyComponentFactory.makeSmsUsageMonitor(context);
            this.mUiccController = UiccController.getInstance();
            this.mUiccController.registerForIccChanged(this, 30, null);
            if (getPhoneType() != 3) {
                this.mCi.registerForSrvccStateChanged(this, 31, null);
            }
            this.mCi.setOnUnsolOemHookRaw(this, 34, null);
            this.mCi.startLceService(200, true, obtainMessage(37));
            this.mCi.registerForCipherIndication(this, 1000, null);
            this.mCi.setOnSpeechCodecInfo(this, 1001, null);
            this.mRadioCapability.set(this.mCi.getBootupRadioCapability());
            this.mCi.registerForRadioCapabilityChanged(this, 111, null);
        }
    }

    public void startMonitoringImsService() {
        if (getPhoneType() != 3) {
            synchronized (lockForRadioTechnologyChange) {
                IntentFilter filter = new IntentFilter();
                filter.addAction("com.android.ims.IMS_SERVICE_UP");
                filter.addAction("com.android.ims.IMS_SERVICE_DOWN");
                filter.addAction("com.android.intent.action.IMS_CONFIG_CHANGED");
                this.mContext.registerReceiver(this.mImsIntentReceiver, filter);
                ImsManager imsManager = ImsManager.getInstance(this.mContext, getPhoneId());
                if (imsManager != null && imsManager.isServiceAvailable()) {
                    this.mImsServiceReady = true;
                    updateImsPhone();
                    ImsManager.updateImsServiceConfig(this.mContext, this.mPhoneId, false);
                }
            }
        }
    }

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case 16:
                if (msg.obj.exception != null) {
                    clearSavedNetworkSelection();
                    break;
                }
                break;
            case 17:
                break;
            default:
                AsyncResult ar;
                switch (msg.what) {
                    case 14:
                        Rlog.d(LOG_TAG, "Event EVENT_CALL_RING Received state=" + getState());
                        if (((AsyncResult) msg.obj).exception == null) {
                            State state = getState();
                            if (!this.mDoesRilSendMultipleCallRing && (state == State.RINGING || state == State.IDLE)) {
                                this.mCallRingContinueToken++;
                                sendIncomingCallRingNotification(this.mCallRingContinueToken);
                                break;
                            }
                            notifyIncomingRing();
                            break;
                        }
                        break;
                    case 15:
                        Rlog.d(LOG_TAG, "Event EVENT_CALL_RING_CONTINUE Received state=" + getState());
                        if (getState() == State.RINGING) {
                            sendIncomingCallRingNotification(msg.arg1);
                            break;
                        }
                        break;
                    case 30:
                        onUpdateIccAvailability();
                        break;
                    case 31:
                        ar = (AsyncResult) msg.obj;
                        if (ar.exception != null) {
                            Rlog.e(LOG_TAG, "Srvcc exception: " + ar.exception);
                            break;
                        } else {
                            handleSrvccStateChanged((int[]) ar.result);
                            break;
                        }
                    case 32:
                        Rlog.d(LOG_TAG, "Event EVENT_INITIATE_SILENT_REDIAL Received");
                        ar = (AsyncResult) msg.obj;
                        if (ar.exception == null && ar.result != null) {
                            String dialString = ar.result;
                            if (!TextUtils.isEmpty(dialString)) {
                                try {
                                    dialInternal(dialString, null, 0, null);
                                    break;
                                } catch (CallStateException e) {
                                    Rlog.e(LOG_TAG, "silent redial failed: " + e);
                                    break;
                                }
                            }
                            return;
                        }
                    case 34:
                        ar = (AsyncResult) msg.obj;
                        if (ar.exception != null) {
                            Rlog.e(LOG_TAG, "OEM hook raw exception: " + ar.exception);
                            break;
                        }
                        this.mNotifier.notifyOemHookRawEventForSubscriber(getSubId(), ar.result);
                        break;
                    case 37:
                        ar = (AsyncResult) msg.obj;
                        if (ar.exception == null) {
                            this.mLceStatus = ((Integer) ar.result.get(0)).intValue();
                            break;
                        } else {
                            Rlog.d(LOG_TAG, "config LCE service failed: " + ar.exception);
                            break;
                        }
                    case 38:
                        onCheckForNetworkSelectionModeAutomatic(msg);
                        break;
                    case 103:
                        String[] sFun = new String[2];
                        String[] sRat = new String[2];
                        String[] s = new String[2];
                        Rlog.d(LOG_TAG, "send special AT cmd to MD");
                        sFun[0] = "AT+EFUN=1";
                        sFun[1] = UsimPBMemInfo.STRING_NOT_SET;
                        invokeOemRilRequestStrings(sFun, null);
                        s[0] = "AT+ERFTX=1,0,0," + SystemProperties.get("persist.radio.charging_stop", "40");
                        s[1] = UsimPBMemInfo.STRING_NOT_SET;
                        invokeOemRilRequestStrings(s, null);
                        break;
                    case 111:
                        ar = (AsyncResult) msg.obj;
                        RadioCapability rc_unsol = ar.result;
                        if (ar.exception != null) {
                            Rlog.d(LOG_TAG, "RIL_UNSOL_RADIO_CAPABILITY fail,no need to change mRadioCapability");
                        } else {
                            this.mRadioCapability.set(rc_unsol);
                            this.mRadioCapabilityRef++;
                        }
                        Rlog.d(LOG_TAG, "EVENT_UNSOL_RADIO_CAPABILITY_CHANGED :phone rc : " + rc_unsol);
                        break;
                    case 1001:
                        ar = (AsyncResult) msg.obj;
                        Rlog.d(LOG_TAG, "handle EVENT_SPEECH_CODEC_INFO : " + ((int[]) ar.result)[0]);
                        notifySpeechCodecInfo(((int[]) ar.result)[0]);
                        break;
                    default:
                        Rlog.w(LOG_TAG, "unexpected event not handled:" + msg.what);
                        break;
                }
                return;
        }
        handleSetSelectNetwork((AsyncResult) msg.obj);
    }

    public ArrayList<Connection> getHandoverConnection() {
        return null;
    }

    public void notifySrvccState(SrvccState state) {
    }

    public void registerForSilentRedial(Handler h, int what, Object obj) {
    }

    public void unregisterForSilentRedial(Handler h) {
    }

    private void handleSrvccStateChanged(int[] ret) {
        Rlog.d(LOG_TAG, "handleSrvccStateChanged");
        ArrayList<Connection> conn = null;
        Phone imsPhone = this.mImsPhone;
        SrvccState srvccState = SrvccState.NONE;
        if (!(ret == null || ret.length == 0)) {
            switch (ret[0]) {
                case 0:
                    srvccState = SrvccState.STARTED;
                    if (imsPhone == null) {
                        Rlog.d(LOG_TAG, "HANDOVER_STARTED: mImsPhone null");
                        break;
                    }
                    conn = imsPhone.getHandoverConnection();
                    migrateFrom(imsPhone);
                    break;
                case 1:
                    srvccState = SrvccState.COMPLETED;
                    if (imsPhone == null) {
                        Rlog.d(LOG_TAG, "HANDOVER_COMPLETED: mImsPhone null");
                        break;
                    } else {
                        imsPhone.notifySrvccState(srvccState);
                        break;
                    }
                case 2:
                case 3:
                    srvccState = SrvccState.FAILED;
                    break;
                default:
                    return;
            }
            getCallTracker().notifySrvccState(srvccState, conn);
            this.mSrvccState = srvccState;
            if (this.mIsPendingSRVCC && this.mSrvccState == SrvccState.COMPLETED) {
                getCallTracker().pollCallsAfterDelay();
            }
            this.mIsPendingSRVCC = false;
        }
    }

    public Context getContext() {
        return this.mContext;
    }

    public void disableDnsCheck(boolean b) {
        this.mDnsCheckDisabled = b;
        Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        editor.putBoolean(DNS_SERVER_CHECK_DISABLED_KEY, b);
        editor.apply();
    }

    public boolean isDnsCheckDisabled() {
        return this.mDnsCheckDisabled;
    }

    public void registerForPreciseCallStateChanged(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        this.mPreciseCallStateRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForPreciseCallStateChanged(Handler h) {
        this.mPreciseCallStateRegistrants.remove(h);
    }

    protected void notifyPreciseCallStateChangedP() {
        this.mPreciseCallStateRegistrants.notifyRegistrants(new AsyncResult(null, this, null));
        this.mNotifier.notifyPreciseCallState(this);
    }

    public void registerForHandoverStateChanged(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        this.mHandoverRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForHandoverStateChanged(Handler h) {
        this.mHandoverRegistrants.remove(h);
    }

    public void notifyHandoverStateChanged(Connection cn) {
        this.mHandoverRegistrants.notifyRegistrants(new AsyncResult(null, cn, null));
    }

    protected void setIsInEmergencyCall() {
    }

    protected void migrateFrom(Phone from) {
        migrate(this.mHandoverRegistrants, from.mHandoverRegistrants);
        migrate(this.mPreciseCallStateRegistrants, from.mPreciseCallStateRegistrants);
        migrate(this.mNewRingingConnectionRegistrants, from.mNewRingingConnectionRegistrants);
        migrate(this.mIncomingRingRegistrants, from.mIncomingRingRegistrants);
        migrate(this.mDisconnectRegistrants, from.mDisconnectRegistrants);
        migrate(this.mServiceStateRegistrants, from.mServiceStateRegistrants);
        migrate(this.mMmiCompleteRegistrants, from.mMmiCompleteRegistrants);
        migrate(this.mMmiRegistrants, from.mMmiRegistrants);
        migrate(this.mUnknownConnectionRegistrants, from.mUnknownConnectionRegistrants);
        migrate(this.mSuppServiceFailedRegistrants, from.mSuppServiceFailedRegistrants);
        if (from.isInEmergencyCall()) {
            setIsInEmergencyCall();
        }
    }

    protected void migrate(RegistrantList to, RegistrantList from) {
        from.removeCleared();
        int n = from.size();
        for (int i = 0; i < n; i++) {
            Message msg = ((Registrant) from.get(i)).messageForRegistrant();
            if (msg == null) {
                Rlog.d(LOG_TAG, "msg is null");
            } else if (msg.obj != CallManager.getInstance().getRegistrantIdentifier()) {
                to.add((Registrant) from.get(i));
            }
        }
    }

    public void registerForUnknownConnection(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        this.mUnknownConnectionRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForUnknownConnection(Handler h) {
        this.mUnknownConnectionRegistrants.remove(h);
    }

    public void registerForNewRingingConnection(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        this.mNewRingingConnectionRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForNewRingingConnection(Handler h) {
        this.mNewRingingConnectionRegistrants.remove(h);
    }

    public void registerForVideoCapabilityChanged(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        this.mVideoCapabilityChangedRegistrants.addUnique(h, what, obj);
        notifyForVideoCapabilityChanged(this.mIsVideoCapable);
    }

    public void unregisterForVideoCapabilityChanged(Handler h) {
        this.mVideoCapabilityChangedRegistrants.remove(h);
    }

    public void registerForInCallVoicePrivacyOn(Handler h, int what, Object obj) {
        this.mCi.registerForInCallVoicePrivacyOn(h, what, obj);
    }

    public void unregisterForInCallVoicePrivacyOn(Handler h) {
        this.mCi.unregisterForInCallVoicePrivacyOn(h);
    }

    public void registerForInCallVoicePrivacyOff(Handler h, int what, Object obj) {
        this.mCi.registerForInCallVoicePrivacyOff(h, what, obj);
    }

    public void unregisterForInCallVoicePrivacyOff(Handler h) {
        this.mCi.unregisterForInCallVoicePrivacyOff(h);
    }

    public void registerForIncomingRing(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        this.mIncomingRingRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForIncomingRing(Handler h) {
        this.mIncomingRingRegistrants.remove(h);
    }

    public void registerForDisconnect(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        this.mDisconnectRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForDisconnect(Handler h) {
        this.mDisconnectRegistrants.remove(h);
    }

    public void registerForSuppServiceFailed(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        this.mSuppServiceFailedRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForSuppServiceFailed(Handler h) {
        this.mSuppServiceFailedRegistrants.remove(h);
    }

    public void registerForMmiInitiate(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        this.mMmiRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForMmiInitiate(Handler h) {
        this.mMmiRegistrants.remove(h);
    }

    public void registerForMmiComplete(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        this.mMmiCompleteRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForMmiComplete(Handler h) {
        checkCorrectThread(h);
        this.mMmiCompleteRegistrants.remove(h);
    }

    public void registerForSimRecordsLoaded(Handler h, int what, Object obj) {
    }

    public void unregisterForSimRecordsLoaded(Handler h) {
    }

    public void registerForTtyModeReceived(Handler h, int what, Object obj) {
    }

    public void unregisterForTtyModeReceived(Handler h) {
    }

    public void setNetworkSelectionModeAutomatic(Message response) {
        Rlog.d(LOG_TAG, "setNetworkSelectionModeAutomatic, querying current mode");
        Message msg = obtainMessage(38);
        msg.obj = response;
        this.mCi.getNetworkSelectionMode(msg);
    }

    private void onCheckForNetworkSelectionModeAutomatic(Message fromRil) {
        AsyncResult ar = fromRil.obj;
        Message response = ar.userObj;
        boolean doAutomatic = true;
        if (ar.exception == null && ar.result != null) {
            try {
                if (ar.result[0] == 0) {
                    doAutomatic = false;
                    ServiceState mss = getServiceState();
                    if (!(mss.getVoiceRegState() == 0 || mss.getDataRegState() == 0)) {
                        ar.exception = new CommandException(Error.ABORTED);
                    }
                }
            } catch (Exception e) {
            }
        }
        NetworkSelectMessage nsm = new NetworkSelectMessage();
        nsm.message = response;
        nsm.operatorNumeric = UsimPBMemInfo.STRING_NOT_SET;
        nsm.operatorAlphaLong = UsimPBMemInfo.STRING_NOT_SET;
        nsm.operatorAlphaShort = UsimPBMemInfo.STRING_NOT_SET;
        if (doAutomatic) {
            this.mCi.setNetworkSelectionModeAutomatic(obtainMessage(17, nsm));
        } else {
            Rlog.d(LOG_TAG, "setNetworkSelectionModeAutomatic - already auto, ignoring");
            ar.userObj = nsm;
            handleSetSelectNetwork(ar);
        }
        updateSavedNetworkOperator(nsm);
    }

    public void getNetworkSelectionMode(Message message) {
        this.mCi.getNetworkSelectionMode(message);
    }

    public void selectNetworkManually(OperatorInfo network, boolean persistSelection, Message response) {
        NetworkSelectMessage nsm = new NetworkSelectMessage();
        nsm.message = response;
        nsm.operatorNumeric = network.getOperatorNumeric();
        nsm.operatorAlphaLong = network.getOperatorAlphaLong();
        nsm.operatorAlphaShort = network.getOperatorAlphaShort();
        Message msg = obtainMessage(16, nsm);
        if (getPhoneName().equals("GSM")) {
            Rlog.d(LOG_TAG, "GSMPhone selectNetworkManuallyWithAct:" + network);
            String actype = "0";
            if (network.getOperatorAlphaLong() != null && network.getOperatorAlphaLong().endsWith(UTRAN_INDICATOR)) {
                actype = "2";
            } else if (network.getOperatorAlphaLong() != null && network.getOperatorAlphaLong().endsWith(LTE_INDICATOR)) {
                actype = ACT_TYPE_LTE;
            }
            this.mCi.setNetworkSelectionModeManualWithAct(network.getOperatorNumeric(), actype, msg);
        } else {
            this.mCi.setNetworkSelectionModeManual(network.getOperatorNumeric(), msg);
        }
        if (persistSelection) {
            updateSavedNetworkOperator(nsm);
        } else {
            clearSavedNetworkSelection();
        }
    }

    public void registerForEmergencyCallToggle(Handler h, int what, Object obj) {
        this.mEmergencyCallToggledRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForEmergencyCallToggle(Handler h) {
        this.mEmergencyCallToggledRegistrants.remove(h);
    }

    private void updateSavedNetworkOperator(NetworkSelectMessage nsm) {
        int subId = getSubId();
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
            editor.putString(NETWORK_SELECTION_KEY + subId, nsm.operatorNumeric);
            editor.putString(NETWORK_SELECTION_NAME_KEY + subId, nsm.operatorAlphaLong);
            editor.putString(NETWORK_SELECTION_SHORT_KEY + subId, nsm.operatorAlphaShort);
            if (!editor.commit()) {
                Rlog.e(LOG_TAG, "failed to commit network selection preference");
                return;
            }
            return;
        }
        Rlog.e(LOG_TAG, "Cannot update network selection preference due to invalid subId " + subId);
    }

    private void handleSetSelectNetwork(AsyncResult ar) {
        if (ar.userObj instanceof NetworkSelectMessage) {
            NetworkSelectMessage nsm = ar.userObj;
            if (nsm.message != null) {
                AsyncResult.forMessage(nsm.message, ar.result, ar.exception);
                nsm.message.sendToTarget();
            }
            return;
        }
        Rlog.e(LOG_TAG, "unexpected result from user object.");
    }

    private OperatorInfo getSavedNetworkSelection() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        return new OperatorInfo(sp.getString(NETWORK_SELECTION_NAME_KEY + getSubId(), UsimPBMemInfo.STRING_NOT_SET), sp.getString(NETWORK_SELECTION_SHORT_KEY + getSubId(), UsimPBMemInfo.STRING_NOT_SET), sp.getString(NETWORK_SELECTION_KEY + getSubId(), UsimPBMemInfo.STRING_NOT_SET));
    }

    private void clearSavedNetworkSelection() {
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().remove(NETWORK_SELECTION_KEY + getSubId()).remove(NETWORK_SELECTION_NAME_KEY + getSubId()).remove(NETWORK_SELECTION_SHORT_KEY + getSubId()).commit();
    }

    public void restoreSavedNetworkSelection(Message response) {
        OperatorInfo networkSelection = getSavedNetworkSelection();
        if (networkSelection == null || TextUtils.isEmpty(networkSelection.getOperatorNumeric())) {
            setNetworkSelectionModeAutomatic(response);
        } else {
            selectNetworkManually(networkSelection, true, response);
        }
    }

    public void saveClirSetting(int commandInterfaceCLIRMode) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        editor.putInt(CLIR_KEY + getPhoneId(), commandInterfaceCLIRMode);
        if (!editor.commit()) {
            Rlog.e(LOG_TAG, "Failed to commit CLIR preference");
        }
    }

    private void setUnitTestMode(boolean f) {
        this.mUnitTestMode = f;
    }

    public boolean getUnitTestMode() {
        return this.mUnitTestMode;
    }

    protected void notifyDisconnectP(Connection cn) {
        this.mDisconnectRegistrants.notifyRegistrants(new AsyncResult(null, cn, null));
    }

    public void registerForServiceStateChanged(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        this.mServiceStateRegistrants.add(h, what, obj);
    }

    public void unregisterForServiceStateChanged(Handler h) {
        this.mServiceStateRegistrants.remove(h);
    }

    public void registerForRingbackTone(Handler h, int what, Object obj) {
        this.mCi.registerForRingbackTone(h, what, obj);
    }

    public void unregisterForRingbackTone(Handler h) {
        this.mCi.unregisterForRingbackTone(h);
    }

    public void registerForOnHoldTone(Handler h, int what, Object obj) {
    }

    public void unregisterForOnHoldTone(Handler h) {
    }

    public void registerForResendIncallMute(Handler h, int what, Object obj) {
        this.mCi.registerForResendIncallMute(h, what, obj);
    }

    public void unregisterForResendIncallMute(Handler h) {
        this.mCi.unregisterForResendIncallMute(h);
    }

    public void setEchoSuppressionEnabled() {
    }

    protected void notifyServiceStateChangedP(ServiceState ss) {
        this.mServiceStateRegistrants.notifyRegistrants(new AsyncResult(null, ss, null));
        boolean isVideoCallCapable = isVideoEnabled();
        if (this.mIsVideoCapable != isVideoCallCapable) {
            notifyForVideoCapabilityChanged(isVideoCallCapable);
        }
        this.mNotifier.notifyServiceState(this);
        if (ss.getState() != 3 && SystemProperties.get("ril.charging_stop_enable", "0").equals("1")) {
            sendMessageDelayed(obtainMessage(103, 0, 0), 60000);
        }
    }

    public SimulatedRadioControl getSimulatedRadioControl() {
        return this.mSimulatedRadioControl;
    }

    private void checkCorrectThread(Handler h) {
        if (h.getLooper() != this.mLooper) {
            throw new RuntimeException("com.android.internal.telephony.Phone must be used from within one thread");
        }
    }

    private static Locale getLocaleFromCarrierProperties(Context ctx) {
        String carrier = SystemProperties.get("ro.carrier");
        if (carrier == null || carrier.length() == 0 || "unknown".equals(carrier)) {
            return null;
        }
        CharSequence[] carrierLocales = ctx.getResources().getTextArray(17236074);
        for (int i = 0; i < carrierLocales.length; i += 3) {
            if (carrier.equals(carrierLocales[i].toString())) {
                return Locale.forLanguageTag(carrierLocales[i + 1].toString().replace('_', '-'));
            }
        }
        return null;
    }

    public IccFileHandler getIccFileHandler() {
        IccFileHandler fh;
        UiccCardApplication uiccApplication = (UiccCardApplication) this.mUiccApplication.get();
        if (uiccApplication == null) {
            Rlog.d(LOG_TAG, "getIccFileHandler: uiccApplication == null, return null");
            fh = null;
        } else {
            fh = uiccApplication.getIccFileHandler();
        }
        Rlog.d(LOG_TAG, "getIccFileHandler: fh=" + fh);
        return fh;
    }

    public Handler getHandler() {
        return this;
    }

    public void updatePhoneObject(int voiceRadioTech) {
    }

    public ServiceStateTracker getServiceStateTracker() {
        return null;
    }

    public CallTracker getCallTracker() {
        return null;
    }

    public void updateVoiceMail() {
        Rlog.e(LOG_TAG, "updateVoiceMail() should be overridden");
    }

    public AppType getCurrentUiccAppType() {
        UiccCardApplication currentApp = (UiccCardApplication) this.mUiccApplication.get();
        if (currentApp != null) {
            return currentApp.getType();
        }
        return AppType.APPTYPE_UNKNOWN;
    }

    public IccCard getIccCard() {
        return null;
    }

    public String getIccSerialNumber() {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r != null) {
            return r.getIccId();
        }
        return null;
    }

    public String getFullIccSerialNumber() {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r != null) {
            return r.getFullIccId();
        }
        return null;
    }

    public boolean getIccRecordsLoaded() {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        return r != null ? r.getRecordsLoaded() : false;
    }

    public List<CellInfo> getAllCellInfo() {
        return privatizeCellInfoList(getServiceStateTracker().getAllCellInfo());
    }

    private List<CellInfo> privatizeCellInfoList(List<CellInfo> cellInfoList) {
        if (cellInfoList == null) {
            return null;
        }
        if (Secure.getInt(getContext().getContentResolver(), "location_mode", 0) == 0) {
            ArrayList<CellInfo> privateCellInfoList = new ArrayList(cellInfoList.size());
            for (CellInfo c : cellInfoList) {
                if (c instanceof CellInfoCdma) {
                    CellInfoCdma cellInfoCdma = (CellInfoCdma) c;
                    CellIdentityCdma cellIdentity = cellInfoCdma.getCellIdentity();
                    CellIdentityCdma maskedCellIdentity = new CellIdentityCdma(cellIdentity.getNetworkId(), cellIdentity.getSystemId(), cellIdentity.getBasestationId(), Integer.MAX_VALUE, Integer.MAX_VALUE);
                    CellInfoCdma privateCellInfoCdma = new CellInfoCdma(cellInfoCdma);
                    privateCellInfoCdma.setCellIdentity(maskedCellIdentity);
                    privateCellInfoList.add(privateCellInfoCdma);
                } else {
                    privateCellInfoList.add(c);
                }
            }
            cellInfoList = privateCellInfoList;
        }
        return cellInfoList;
    }

    public void setCellInfoListRate(int rateInMillis) {
        this.mCi.setCellInfoListRate(rateInMillis, null);
    }

    public boolean getMessageWaitingIndicator() {
        return this.mVmCount != 0;
    }

    private int getCallForwardingIndicatorFromSharedPref() {
        boolean z = true;
        int status = 0;
        int subId = getSubId();
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
            status = sp.getInt(CF_STATUS + subId, -1);
            Rlog.d(LOG_TAG, "getCallForwardingIndicatorFromSharedPref: for subId " + subId + "= " + status);
            if (status == -1) {
                String subscriberId = sp.getString(CF_ID, null);
                if (subscriberId != null) {
                    if (subscriberId.equals(getSubscriberId())) {
                        status = sp.getInt(CF_STATUS, 0);
                        if (status != 1) {
                            z = false;
                        }
                        setCallForwardingIndicatorInSharedPref(z);
                        Rlog.d(LOG_TAG, "getCallForwardingIndicatorFromSharedPref: " + status);
                    } else {
                        Rlog.d(LOG_TAG, "getCallForwardingIndicatorFromSharedPref: returning DISABLED as status for matching subscriberId not found");
                    }
                    Editor editor = sp.edit();
                    editor.remove(CF_ID);
                    editor.remove(CF_STATUS);
                    editor.apply();
                }
            }
        } else {
            Rlog.e(LOG_TAG, "getCallForwardingIndicatorFromSharedPref: invalid subId " + subId);
        }
        return status;
    }

    public void clearCallForwardingIndicatorFromSharedPref() {
        setCallForwardingIndicatorInSharedPref(false);
    }

    private void setCallForwardingIndicatorInSharedPref(boolean enable) {
        int status;
        if (enable) {
            status = 1;
        } else {
            status = 0;
        }
        int subId = getSubId();
        Rlog.d(LOG_TAG, "setCallForwardingIndicatorInSharedPref: Storing status = " + status + " in pref " + CF_STATUS + subId);
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        editor.putInt(CF_STATUS + subId, status);
        editor.apply();
    }

    public void setVoiceCallForwardingFlag(int line, boolean enable, String number) {
        setCallForwardingIndicatorInSharedPref(enable);
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r != null) {
            r.setVoiceCallForwardingFlag(line, enable, number);
        } else {
            Rlog.d(LOG_TAG, "IccRecords is null, skip set CFU icon.");
        }
    }

    public void setVideoCallForwardingFlag(boolean enable) {
        boolean supportVideoCf = false;
        CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        if (configManager != null) {
            PersistableBundle config = configManager.getConfigForSubId(getSubId());
            if (config != null) {
                supportVideoCf = config.getBoolean("oppo_support_video_cf_bool");
            }
        }
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        int subId = getSubId();
        Rlog.d(LOG_TAG, "setVideoCallForwardingFlag InSharedPref: Storing enable = " + enable + "  video_cf_enabled:" + subId + ", supportVideoCf " + supportVideoCf);
        if (sp != null && supportVideoCf) {
            Editor editor = sp.edit();
            editor.putBoolean(CF_ENABLED_VIDEO + subId, enable);
            editor.apply();
        }
    }

    public boolean getVideoCallForwardingFlag() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        boolean enabled = false;
        int subId = getSubId();
        if (sp != null) {
            enabled = sp.getBoolean(CF_ENABLED_VIDEO + subId, false);
        }
        Rlog.d(LOG_TAG, "getVideoCallForwardingFlag enabled = " + enabled + " for subid " + subId);
        return enabled;
    }

    protected void setVoiceCallForwardingFlag(IccRecords r, int line, boolean enable, String number) {
        setCallForwardingIndicatorInSharedPref(enable);
        r.setVoiceCallForwardingFlag(line, enable, number);
    }

    public boolean getCallForwardingIndicator() {
        boolean z = true;
        if (getPhoneType() == 2) {
            Rlog.e(LOG_TAG, "getCallForwardingIndicator: not possible in CDMA");
            return false;
        }
        IccRecords r = (IccRecords) this.mIccRecords.get();
        int callForwardingIndicator = -1;
        if (r != null) {
            callForwardingIndicator = r.getVoiceCallForwardingFlag();
        }
        if (callForwardingIndicator == -1) {
            callForwardingIndicator = getCallForwardingIndicatorFromSharedPref();
        }
        if (callForwardingIndicator != 1) {
            z = getVideoCallForwardingFlag();
        }
        return z;
    }

    public CarrierSignalAgent getCarrierSignalAgent() {
        return this.mCarrierSignalAgent;
    }

    public void queryCdmaRoamingPreference(Message response) {
        this.mCi.queryCdmaRoamingPreference(response);
    }

    public SignalStrength getSignalStrength() {
        ServiceStateTracker sst = getServiceStateTracker();
        if (sst == null) {
            return new SignalStrength();
        }
        return sst.getSignalStrength();
    }

    public void setCdmaRoamingPreference(int cdmaRoamingType, Message response) {
        this.mCi.setCdmaRoamingPreference(cdmaRoamingType, response);
    }

    public void setCdmaSubscription(int cdmaSubscriptionType, Message response) {
        this.mCi.setCdmaSubscriptionSource(cdmaSubscriptionType, response);
    }

    public void setPreferredNetworkType(int networkType, Message response) {
        int modemRaf = getRadioAccessFamily();
        int rafFromType = RadioAccessFamily.getRafFromNetworkType(networkType);
        if (modemRaf == 1 || rafFromType == 1) {
            Rlog.d(LOG_TAG, "setPreferredNetworkType: Abort, unknown RAF: " + modemRaf + " " + rafFromType);
            if (response != null) {
                AsyncResult.forMessage(response, null, new CommandException(Error.GENERIC_FAILURE));
                response.sendToTarget();
            }
            return;
        }
        int filteredType = RadioAccessFamily.getNetworkTypeFromRaf(rafFromType & modemRaf);
        Rlog.d(LOG_TAG, "setPreferredNetworkType: networkType = " + networkType + " modemRaf = " + modemRaf + " rafFromType = " + rafFromType + " filteredType = " + filteredType);
        this.mCi.setPreferredNetworkType(filteredType, response);
    }

    public void getPreferredNetworkType(Message response) {
        this.mCi.getPreferredNetworkType(response);
    }

    public void getSmscAddress(Message result) {
        this.mCi.getSmscAddress(result);
    }

    public void setSmscAddress(String address, Message result) {
        this.mCi.setSmscAddress(address, result);
    }

    public void setTTYMode(int ttyMode, Message onComplete) {
        this.mCi.setTTYMode(ttyMode, onComplete);
    }

    public void setUiTTYMode(int uiTtyMode, Message onComplete) {
        Rlog.d(LOG_TAG, "unexpected setUiTTYMode method call");
    }

    public void queryTTYMode(Message onComplete) {
        this.mCi.queryTTYMode(onComplete);
    }

    public void enableEnhancedVoicePrivacy(boolean enable, Message onComplete) {
    }

    public void getEnhancedVoicePrivacy(Message onComplete) {
    }

    public void setBandMode(int bandMode, Message response) {
        this.mCi.setBandMode(bandMode, response);
    }

    public void queryAvailableBandMode(Message response) {
        this.mCi.queryAvailableBandMode(response);
    }

    public void invokeOemRilRequestRaw(byte[] data, Message response) {
        this.mCi.invokeOemRilRequestRaw(data, response);
    }

    public void invokeOemRilRequestStrings(String[] strings, Message response) {
        this.mCi.invokeOemRilRequestStrings(strings, response);
    }

    public void nvReadItem(int itemID, Message response) {
        this.mCi.nvReadItem(itemID, response);
    }

    public void nvWriteItem(int itemID, String itemValue, Message response) {
        this.mCi.nvWriteItem(itemID, itemValue, response);
    }

    public void nvWriteCdmaPrl(byte[] preferredRoamingList, Message response) {
        this.mCi.nvWriteCdmaPrl(preferredRoamingList, response);
    }

    public void nvResetConfig(int resetType, Message response) {
        this.mCi.nvResetConfig(resetType, response);
    }

    public void notifyDataActivity() {
        this.mNotifier.notifyDataActivity(this);
    }

    private void notifyMessageWaitingIndicator() {
        if (this.mIsVoiceCapable) {
            this.mNotifier.notifyMessageWaitingChanged(this);
        }
    }

    public void notifyDataConnection(String reason, String apnType, DataState state) {
        this.mNotifier.notifyDataConnection(this, reason, apnType, state);
    }

    public void notifyDataConnection(String reason, String apnType) {
        this.mNotifier.notifyDataConnection(this, reason, apnType, getDataConnectionState(apnType));
    }

    public void notifyDataConnection(String reason) {
        for (String apnType : getActiveApnTypes()) {
            this.mNotifier.notifyDataConnection(this, reason, apnType, getDataConnectionState(apnType));
        }
    }

    public void notifyOtaspChanged(int otaspMode) {
        this.mNotifier.notifyOtaspChanged(this, otaspMode);
    }

    public void notifySignalStrength() {
        this.mNotifier.notifySignalStrength(this);
    }

    public void notifyCellInfo(List<CellInfo> cellInfo) {
        this.mNotifier.notifyCellInfo(this, privatizeCellInfoList(cellInfo));
    }

    public void notifyVoLteServiceStateChanged(VoLteServiceState lteState) {
        this.mNotifier.notifyVoLteServiceStateChanged(this, lteState);
    }

    public boolean isInEmergencyCall() {
        return false;
    }

    public boolean isInEcm() {
        return false;
    }

    private static int getVideoState(Call call) {
        Connection conn = call.getEarliestConnection();
        if (conn != null) {
            return conn.getVideoState();
        }
        return 0;
    }

    private boolean isVideoCall(Call call) {
        return VideoProfile.isVideo(getVideoState(call));
    }

    public boolean isVideoCallPresent() {
        boolean isVideoCallActive = false;
        if (this.mImsPhone != null) {
            if (isVideoCall(this.mImsPhone.getForegroundCall()) || isVideoCall(this.mImsPhone.getBackgroundCall())) {
                isVideoCallActive = true;
            } else {
                isVideoCallActive = isVideoCall(this.mImsPhone.getRingingCall());
            }
        }
        Rlog.d(LOG_TAG, "isVideoCallActive: " + isVideoCallActive);
        return isVideoCallActive;
    }

    public int getVoiceMessageCount() {
        return this.mVmCount;
    }

    public void setVoiceMessageCount(int countWaiting) {
        this.mVmCount = countWaiting;
        int subId = getSubId();
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            Rlog.d(LOG_TAG, "setVoiceMessageCount: Storing Voice Mail Count = " + countWaiting + " for mVmCountKey = " + VM_COUNT + subId + " in preferences.");
            Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
            editor.putInt(VM_COUNT + subId, countWaiting);
            editor.apply();
        } else {
            Rlog.e(LOG_TAG, "setVoiceMessageCount in sharedPreference: invalid subId " + subId);
        }
        notifyMessageWaitingIndicator();
    }

    protected int getStoredVoiceMessageCount() {
        int countVoiceMessages = 0;
        int subId = getSubId();
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
            int countFromSP = sp.getInt(VM_COUNT + subId, -2);
            if (countFromSP != -2) {
                countVoiceMessages = countFromSP;
                Rlog.d(LOG_TAG, "getStoredVoiceMessageCount: from preference for subId " + subId + "= " + countFromSP);
                return countVoiceMessages;
            }
            String subscriberId = sp.getString(VM_ID, null);
            if (subscriberId == null) {
                return 0;
            }
            String currentSubscriberId = getSubscriberId();
            if (currentSubscriberId == null || !currentSubscriberId.equals(subscriberId)) {
                Rlog.d(LOG_TAG, "getStoredVoiceMessageCount: returning 0 as count for matching subscriberId not found");
            } else {
                countVoiceMessages = sp.getInt(VM_COUNT, 0);
                setVoiceMessageCount(countVoiceMessages);
                Rlog.d(LOG_TAG, "getStoredVoiceMessageCount: from preference = " + countVoiceMessages);
            }
            Editor editor = sp.edit();
            editor.remove(VM_ID);
            editor.remove(VM_COUNT);
            editor.apply();
            return countVoiceMessages;
        }
        Rlog.e(LOG_TAG, "getStoredVoiceMessageCount: invalid subId " + subId);
        return 0;
    }

    public int getCdmaEriIconIndex() {
        return -1;
    }

    public int getCdmaEriIconMode() {
        return -1;
    }

    public String getCdmaEriText() {
        return "GSM nw, no ERI";
    }

    public String getCdmaMin() {
        return null;
    }

    public boolean isMinInfoReady() {
        return false;
    }

    public String getCdmaPrlVersion() {
        return null;
    }

    public void sendBurstDtmf(String dtmfString, int on, int off, Message onComplete) {
    }

    public void setOnPostDialCharacter(Handler h, int what, Object obj) {
        this.mPostDialHandler = new Registrant(h, what, obj);
    }

    public Registrant getPostDialHandler() {
        return this.mPostDialHandler;
    }

    public void exitEmergencyCallbackMode() {
    }

    public void registerForCdmaOtaStatusChange(Handler h, int what, Object obj) {
    }

    public void unregisterForCdmaOtaStatusChange(Handler h) {
    }

    public void registerForSubscriptionInfoReady(Handler h, int what, Object obj) {
    }

    public void unregisterForSubscriptionInfoReady(Handler h) {
    }

    public boolean needsOtaServiceProvisioning() {
        return false;
    }

    public boolean isOtaSpNumber(String dialStr) {
        return false;
    }

    public void registerForCallWaiting(Handler h, int what, Object obj) {
    }

    public void unregisterForCallWaiting(Handler h) {
    }

    public void registerForEcmTimerReset(Handler h, int what, Object obj) {
    }

    public void unregisterForEcmTimerReset(Handler h) {
    }

    public void registerForSignalInfo(Handler h, int what, Object obj) {
        this.mCi.registerForSignalInfo(h, what, obj);
    }

    public void unregisterForSignalInfo(Handler h) {
        this.mCi.unregisterForSignalInfo(h);
    }

    public void registerForDisplayInfo(Handler h, int what, Object obj) {
        this.mCi.registerForDisplayInfo(h, what, obj);
    }

    public void unregisterForDisplayInfo(Handler h) {
        this.mCi.unregisterForDisplayInfo(h);
    }

    public void registerForNumberInfo(Handler h, int what, Object obj) {
        this.mCi.registerForNumberInfo(h, what, obj);
    }

    public void unregisterForNumberInfo(Handler h) {
        this.mCi.unregisterForNumberInfo(h);
    }

    public void registerForRedirectedNumberInfo(Handler h, int what, Object obj) {
        this.mCi.registerForRedirectedNumberInfo(h, what, obj);
    }

    public void unregisterForRedirectedNumberInfo(Handler h) {
        this.mCi.unregisterForRedirectedNumberInfo(h);
    }

    public void registerForLineControlInfo(Handler h, int what, Object obj) {
        this.mCi.registerForLineControlInfo(h, what, obj);
    }

    public void unregisterForLineControlInfo(Handler h) {
        this.mCi.unregisterForLineControlInfo(h);
    }

    public void registerFoT53ClirlInfo(Handler h, int what, Object obj) {
        this.mCi.registerFoT53ClirlInfo(h, what, obj);
    }

    public void unregisterForT53ClirInfo(Handler h) {
        this.mCi.unregisterForT53ClirInfo(h);
    }

    public void registerForT53AudioControlInfo(Handler h, int what, Object obj) {
        this.mCi.registerForT53AudioControlInfo(h, what, obj);
    }

    public void unregisterForT53AudioControlInfo(Handler h) {
        this.mCi.unregisterForT53AudioControlInfo(h);
    }

    public void setOnEcbModeExitResponse(Handler h, int what, Object obj) {
    }

    public void unsetOnEcbModeExitResponse(Handler h) {
    }

    public void registerForRadioOffOrNotAvailable(Handler h, int what, Object obj) {
        this.mRadioOffOrNotAvailableRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForRadioOffOrNotAvailable(Handler h) {
        this.mRadioOffOrNotAvailableRegistrants.remove(h);
    }

    public String[] getActiveApnTypes() {
        if (this.mDcTracker == null) {
            return null;
        }
        return this.mDcTracker.getActiveApnTypes();
    }

    public boolean hasMatchedTetherApnSetting() {
        return this.mDcTracker.hasMatchedTetherApnSetting();
    }

    public String getActiveApnHost(String apnType) {
        return this.mDcTracker.getActiveApnString(apnType);
    }

    public LinkProperties getLinkProperties(String apnType) {
        return this.mDcTracker.getLinkProperties(apnType);
    }

    public NetworkCapabilities getNetworkCapabilities(String apnType) {
        return this.mDcTracker.getNetworkCapabilities(apnType);
    }

    public boolean isDataConnectivityPossible() {
        return isDataConnectivityPossible("default");
    }

    public boolean isDataConnectivityPossible(String apnType) {
        if (this.mDcTracker != null) {
            return this.mDcTracker.isDataPossible(apnType);
        }
        return false;
    }

    public void carrierActionSetMeteredApnsEnabled(boolean enabled) {
        if (this.mDcTracker != null) {
            this.mDcTracker.setApnsEnabledByCarrier(enabled);
        }
    }

    public void carrierActionSetRadioEnabled(boolean enabled) {
        if (this.mDcTracker != null) {
            this.mDcTracker.carrierActionSetRadioEnabled(enabled);
        }
    }

    public void notifyNewRingingConnectionP(Connection cn) {
        if (this.mIsVoiceCapable) {
            this.mNewRingingConnectionRegistrants.notifyRegistrants(new AsyncResult(null, cn, null));
        }
    }

    public void notifyUnknownConnectionP(Connection cn) {
        this.mUnknownConnectionRegistrants.notifyResult(cn);
    }

    public void notifyForVideoCapabilityChanged(boolean isVideoCallCapable) {
        this.mIsVideoCapable = isVideoCallCapable;
        this.mVideoCapabilityChangedRegistrants.notifyRegistrants(new AsyncResult(null, Boolean.valueOf(isVideoCallCapable), null));
    }

    private void notifyIncomingRing() {
        if (this.mIsVoiceCapable) {
            this.mIncomingRingRegistrants.notifyRegistrants(new AsyncResult(null, this, null));
        }
    }

    private void sendIncomingCallRingNotification(int token) {
        if (this.mIsVoiceCapable && !this.mDoesRilSendMultipleCallRing && token == this.mCallRingContinueToken) {
            Rlog.d(LOG_TAG, "Sending notifyIncomingRing");
            notifyIncomingRing();
            sendMessageDelayed(obtainMessage(15, token, 0), (long) this.mCallRingDelay);
            return;
        }
        Rlog.d(LOG_TAG, "Ignoring ring notification request, mDoesRilSendMultipleCallRing=" + this.mDoesRilSendMultipleCallRing + " token=" + token + " mCallRingContinueToken=" + this.mCallRingContinueToken + " mIsVoiceCapable=" + this.mIsVoiceCapable);
    }

    public boolean isCspPlmnEnabled() {
        return false;
    }

    public IsimRecords getIsimRecords() {
        Rlog.e(LOG_TAG, "getIsimRecords() is only supported on LTE devices");
        return null;
    }

    public String getMsisdn() {
        return null;
    }

    public DataState getDataConnectionState() {
        return getDataConnectionState("default");
    }

    public void notifyCallForwardingIndicator() {
    }

    public void notifyDataConnectionFailed(String reason, String apnType) {
        this.mNotifier.notifyDataConnectionFailed(this, reason, apnType);
    }

    public void notifyPreciseDataConnectionFailed(String reason, String apnType, String apn, String failCause) {
        this.mNotifier.notifyPreciseDataConnectionFailed(this, reason, apnType, apn, failCause);
    }

    public int getLteOnCdmaMode() {
        return this.mCi.getLteOnCdmaMode();
    }

    public void setVoiceMessageWaiting(int line, int countWaiting) {
        Rlog.e(LOG_TAG, "Error! This function should never be executed, inactive Phone.");
    }

    public UsimServiceTable getUsimServiceTable() {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r != null) {
            return r.getUsimServiceTable();
        }
        return null;
    }

    public UiccCard getUiccCard() {
        return this.mUiccController.getUiccCard(this.mPhoneId);
    }

    public String[] getPcscfAddress(String apnType) {
        return this.mDcTracker.getPcscfAddress(apnType);
    }

    public void setImsRegistrationState(boolean registered) {
    }

    public Phone getImsPhone() {
        return this.mImsPhone;
    }

    public boolean isUtEnabled() {
        return false;
    }

    public void dispose() {
    }

    private void updateImsPhone() {
        Rlog.d(LOG_TAG, "updateImsPhone mImsServiceReady=" + this.mImsServiceReady);
        if (this.mImsServiceReady && this.mImsPhone == null) {
            this.mImsPhone = PhoneFactory.makeImsPhone(this.mNotifier, this);
            CallManager.getInstance().registerPhone(this.mImsPhone);
            this.mImsPhone.registerForSilentRedial(this, 32, null);
        } else if (!this.mImsServiceReady && this.mImsPhone != null) {
            CallManager.getInstance().unregisterPhone(this.mImsPhone);
            this.mImsPhone.unregisterForSilentRedial(this);
            this.mImsPhone.dispose();
            this.mImsPhone = null;
        }
    }

    protected Connection dialInternal(String dialString, UUSInfo uusInfo, int videoState, Bundle intentExtras) throws CallStateException {
        return null;
    }

    public int getSubId() {
        return SubscriptionController.getInstance().getSubIdUsingPhoneId(this.mPhoneId);
    }

    public int getPhoneId() {
        return this.mPhoneId;
    }

    public int getVoicePhoneServiceState() {
        Phone imsPhone = this.mImsPhone;
        if (imsPhone == null || imsPhone.getServiceState().getState() != 0) {
            return getServiceState().getState();
        }
        return 0;
    }

    public boolean setOperatorBrandOverride(String brand) {
        return false;
    }

    public boolean setRoamingOverride(List<String> gsmRoamingList, List<String> gsmNonRoamingList, List<String> cdmaRoamingList, List<String> cdmaNonRoamingList) {
        String iccId = getIccSerialNumber();
        if (TextUtils.isEmpty(iccId)) {
            return false;
        }
        setRoamingOverrideHelper(gsmRoamingList, GSM_ROAMING_LIST_OVERRIDE_PREFIX, iccId);
        setRoamingOverrideHelper(gsmNonRoamingList, GSM_NON_ROAMING_LIST_OVERRIDE_PREFIX, iccId);
        setRoamingOverrideHelper(cdmaRoamingList, CDMA_ROAMING_LIST_OVERRIDE_PREFIX, iccId);
        setRoamingOverrideHelper(cdmaNonRoamingList, CDMA_NON_ROAMING_LIST_OVERRIDE_PREFIX, iccId);
        ServiceStateTracker tracker = getServiceStateTracker();
        if (tracker != null) {
            tracker.pollState();
        }
        return true;
    }

    private void setRoamingOverrideHelper(List<String> list, String prefix, String iccId) {
        Editor spEditor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        String key = prefix + iccId;
        if (list == null || list.isEmpty()) {
            spEditor.remove(key).commit();
        } else {
            spEditor.putStringSet(key, new HashSet(list)).commit();
        }
    }

    public boolean isMccMncMarkedAsRoaming(String mccMnc) {
        return getRoamingOverrideHelper(GSM_ROAMING_LIST_OVERRIDE_PREFIX, mccMnc);
    }

    public boolean isMccMncMarkedAsNonRoaming(String mccMnc) {
        return getRoamingOverrideHelper(GSM_NON_ROAMING_LIST_OVERRIDE_PREFIX, mccMnc);
    }

    public boolean isSidMarkedAsRoaming(int SID) {
        return getRoamingOverrideHelper(CDMA_ROAMING_LIST_OVERRIDE_PREFIX, Integer.toString(SID));
    }

    public boolean isSidMarkedAsNonRoaming(int SID) {
        return getRoamingOverrideHelper(CDMA_NON_ROAMING_LIST_OVERRIDE_PREFIX, Integer.toString(SID));
    }

    public boolean isImsRegistered() {
        Phone imsPhone = this.mImsPhone;
        boolean isImsRegistered = false;
        if (imsPhone != null) {
            isImsRegistered = imsPhone.isImsRegistered();
        } else {
            ServiceStateTracker sst = getServiceStateTracker();
            if (sst != null) {
                isImsRegistered = sst.isImsRegistered();
                if (isImsRegistered && sst.getCombinedRegState() != 0) {
                    isImsRegistered = false;
                }
            }
        }
        Rlog.d(LOG_TAG, "isImsRegistered =" + isImsRegistered);
        return isImsRegistered;
    }

    public boolean isWifiCallingEnabled() {
        Phone imsPhone = this.mImsPhone;
        boolean isWifiCallingEnabled = false;
        if (imsPhone != null) {
            isWifiCallingEnabled = imsPhone.isWifiCallingEnabled();
        }
        Rlog.d(LOG_TAG, "isWifiCallingEnabled =" + isWifiCallingEnabled);
        return isWifiCallingEnabled;
    }

    public boolean isVolteEnabled() {
        Phone imsPhone = this.mImsPhone;
        boolean isVolteEnabled = false;
        if (imsPhone != null) {
            isVolteEnabled = imsPhone.isVolteEnabled();
        }
        Rlog.d(LOG_TAG, "isImsRegistered =" + isVolteEnabled);
        return isVolteEnabled;
    }

    private boolean getRoamingOverrideHelper(String prefix, String key) {
        String iccId = getIccSerialNumber();
        if (TextUtils.isEmpty(iccId) || TextUtils.isEmpty(key)) {
            return false;
        }
        Set<String> value = PreferenceManager.getDefaultSharedPreferences(this.mContext).getStringSet(prefix + iccId, null);
        if (value == null) {
            return false;
        }
        return value.contains(key);
    }

    public boolean isRadioAvailable() {
        return this.mCi.getRadioState().isAvailable();
    }

    public boolean isRadioOn() {
        return this.mCi.getRadioState().isOn();
    }

    public void shutdownRadio() {
        getServiceStateTracker().requestShutdown();
    }

    public boolean isShuttingDown() {
        return getServiceStateTracker().isDeviceShuttingDown();
    }

    public void setRadioCapability(RadioCapability rc, Message response) {
        this.mCi.setRadioCapability(rc, response);
    }

    public int getRadioAccessFamily() {
        RadioCapability rc = getRadioCapability();
        return rc == null ? 1 : rc.getRadioAccessFamily();
    }

    public String getModemUuId() {
        RadioCapability rc = getRadioCapability();
        return rc == null ? UsimPBMemInfo.STRING_NOT_SET : rc.getLogicalModemUuid();
    }

    public RadioCapability getRadioCapability() {
        if (this.mRadioCapability.get() == null && this.mRadioCapabilityRef == 0) {
            this.mRadioCapability.set(this.mCi.getBootupRadioCapability());
            Rlog.d(LOG_TAG, "getRadioCapability re-get radio capability from bootupRadioCapability!!");
        }
        return (RadioCapability) this.mRadioCapability.get();
    }

    public void radioCapabilityUpdated(RadioCapability rc) {
        this.mRadioCapability.set(rc);
        this.mRadioCapabilityRef++;
        if (SubscriptionManager.isValidSubscriptionId(getSubId())) {
            boolean z;
            if (this.mContext.getResources().getBoolean(17956961)) {
                z = false;
            } else {
                z = true;
            }
            sendSubscriptionSettings(z);
        }
    }

    public void sendSubscriptionSettings(boolean restoreNetworkSelection) {
        ServiceStateTracker sst = getServiceStateTracker();
        if (sst != null) {
            sst.setDeviceRatMode(this.mPhoneId);
        }
        if (restoreNetworkSelection) {
            restoreSavedNetworkSelection(null);
        }
    }

    protected void setPreferredNetworkTypeIfSimLoaded() {
        ServiceStateTracker sst = getServiceStateTracker();
        if (sst != null) {
            sst.setDeviceRatMode(this.mPhoneId);
        }
    }

    public void registerForRadioCapabilityChanged(Handler h, int what, Object obj) {
        this.mCi.registerForRadioCapabilityChanged(h, what, obj);
    }

    public void unregisterForRadioCapabilityChanged(Handler h) {
        this.mCi.unregisterForRadioCapabilityChanged(this);
    }

    public boolean isImsUseEnabled() {
        if (ImsManager.isVolteEnabledByPlatform(this.mContext) && ImsManager.isEnhanced4gLteModeSettingEnabledByUser(this.mContext, this.mPhoneId)) {
            return true;
        }
        if (ImsManager.isWfcEnabledByPlatform(this.mContext) && ImsManager.isWfcEnabledByUser(this.mContext)) {
            return ImsManager.isNonTtyOrTtyOnVolteEnabled(this.mContext);
        }
        return false;
    }

    public boolean isVideoEnabled() {
        Phone imsPhone = this.mImsPhone;
        boolean ret = false;
        if (imsPhone != null && imsPhone.getServiceState().getState() == 0) {
            ret = imsPhone.isVideoEnabled();
        }
        if (!ret) {
            ret = is3GVTEnabled();
        }
        Rlog.d(LOG_TAG, "isVideoEnabled: " + ret);
        return ret;
    }

    public boolean is3GVTEnabled() {
        int networkType = getServiceState().getVoiceNetworkType();
        Rlog.d(LOG_TAG, "networkType=" + TelephonyManager.getNetworkTypeName(networkType));
        boolean is3GVTNetworkAvailable = (networkType == 3 || networkType == 8 || networkType == 9 || networkType == 10 || networkType == 15) ? true : networkType == 13;
        return SystemProperties.get("ro.mtk_vt3g324m_support").equals("1") ? is3GVTNetworkAvailable : false;
    }

    public int getLceStatus() {
        return this.mLceStatus;
    }

    public void getModemActivityInfo(Message response) {
        this.mCi.getModemActivityInfo(response);
    }

    public void startLceAfterRadioIsAvailable() {
        this.mCi.startLceService(200, true, obtainMessage(37));
    }

    public void setAllowedCarriers(List<CarrierIdentifier> carriers, Message response) {
        this.mCi.setAllowedCarriers(carriers, response);
    }

    public void getAllowedCarriers(Message response) {
        this.mCi.getAllowedCarriers(response);
    }

    public Locale getLocaleFromSimAndCarrierPrefs() {
        IccRecords records = (IccRecords) this.mIccRecords.get();
        if (records == null || records.getSimLanguage() == null) {
            return getLocaleFromCarrierProperties(this.mContext);
        }
        return new Locale(records.getSimLanguage());
    }

    public void updateDataConnectionTracker() {
        this.mDcTracker.update();
    }

    public void setInternalDataEnabled(boolean enable, Message onCompleteMsg) {
        this.mDcTracker.setInternalDataEnabled(enable, onCompleteMsg);
    }

    public boolean updateCurrentCarrierInProvider() {
        return false;
    }

    public void registerForAllDataDisconnected(Handler h, int what, Object obj) {
        this.mDcTracker.registerForAllDataDisconnected(h, what, obj);
    }

    public void unregisterForAllDataDisconnected(Handler h) {
        this.mDcTracker.unregisterForAllDataDisconnected(h);
    }

    public void registerForDataEnabledChanged(Handler h, int what, Object obj) {
        this.mDcTracker.registerForDataEnabledChanged(h, what, obj);
    }

    public void unregisterForDataEnabledChanged(Handler h) {
        this.mDcTracker.unregisterForDataEnabledChanged(h);
    }

    public IccSmsInterfaceManager getIccSmsInterfaceManager() {
        return null;
    }

    protected boolean isMatchGid(String gid) {
        String gid1 = getGroupIdLevel1();
        int gidLength = gid.length();
        if (TextUtils.isEmpty(gid1) || gid1.length() < gidLength || !gid1.substring(0, gidLength).equalsIgnoreCase(gid)) {
            return false;
        }
        return true;
    }

    public static void checkWfcWifiOnlyModeBeforeDial(Phone imsPhone, Context context) throws CallStateException {
        boolean wfcWiFiOnly = false;
        if (imsPhone == null || !imsPhone.isWifiCallingEnabled()) {
            if (ImsManager.isWfcEnabledByPlatform(context) && ImsManager.isWfcEnabledByUser(context) && ImsManager.getWfcMode(context) == 0) {
                wfcWiFiOnly = true;
            }
            if (wfcWiFiOnly) {
                throw new CallStateException(1, "WFC Wi-Fi Only Mode: IMS not registered");
            }
        }
    }

    public void startRingbackTone() {
    }

    public void stopRingbackTone() {
    }

    public void callEndCleanupHandOverCallIfAny() {
    }

    public void cancelUSSD() {
    }

    public Phone getDefaultPhone() {
        return this;
    }

    public long getVtDataUsage() {
        if (this.mImsPhone == null) {
            return 0;
        }
        return this.mImsPhone.getVtDataUsage();
    }

    public void setPolicyDataEnabled(boolean enabled) {
        this.mDcTracker.setPolicyDataEnabled(enabled);
    }

    public Uri[] getCurrentSubscriberUris() {
        return null;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("Phone: subId=" + getSubId());
        pw.println(" mPhoneId=" + this.mPhoneId);
        pw.println(" mCi=" + this.mCi);
        pw.println(" mDnsCheckDisabled=" + this.mDnsCheckDisabled);
        pw.println(" mDcTracker=" + this.mDcTracker);
        pw.println(" mDoesRilSendMultipleCallRing=" + this.mDoesRilSendMultipleCallRing);
        pw.println(" mCallRingContinueToken=" + this.mCallRingContinueToken);
        pw.println(" mCallRingDelay=" + this.mCallRingDelay);
        pw.println(" mIsVoiceCapable=" + this.mIsVoiceCapable);
        pw.println(" mIccRecords=" + this.mIccRecords.get());
        pw.println(" mUiccApplication=" + this.mUiccApplication.get());
        pw.println(" mSmsStorageMonitor=" + this.mSmsStorageMonitor);
        pw.println(" mSmsUsageMonitor=" + this.mSmsUsageMonitor);
        pw.flush();
        pw.println(" mLooper=" + this.mLooper);
        pw.println(" mContext=" + this.mContext);
        pw.println(" mNotifier=" + this.mNotifier);
        pw.println(" mSimulatedRadioControl=" + this.mSimulatedRadioControl);
        pw.println(" mUnitTestMode=" + this.mUnitTestMode);
        pw.println(" isDnsCheckDisabled()=" + isDnsCheckDisabled());
        pw.println(" getUnitTestMode()=" + getUnitTestMode());
        pw.println(" getState()=" + getState());
        pw.println(" getIccSerialNumber()=" + getIccSerialNumber());
        pw.println(" getIccRecordsLoaded()=" + getIccRecordsLoaded());
        pw.println(" getMessageWaitingIndicator()=" + getMessageWaitingIndicator());
        pw.println(" getCallForwardingIndicator()=" + getCallForwardingIndicator());
        pw.println(" isInEmergencyCall()=" + isInEmergencyCall());
        pw.flush();
        pw.println(" isInEcm()=" + isInEcm());
        pw.println(" getPhoneName()=" + getPhoneName());
        pw.println(" getPhoneType()=" + getPhoneType());
        pw.println(" getVoiceMessageCount()=" + getVoiceMessageCount());
        pw.println(" getActiveApnTypes()=" + getActiveApnTypes());
        pw.println(" isDataConnectivityPossible()=" + isDataConnectivityPossible());
        pw.println(" needsOtaServiceProvisioning=" + needsOtaServiceProvisioning());
        pw.flush();
        pw.println("++++++++++++++++++++++++++++++++");
        if (this.mImsPhone != null) {
            try {
                this.mImsPhone.dump(fd, pw, args);
            } catch (Exception e) {
                e.printStackTrace();
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
        }
        if (this.mDcTracker != null) {
            try {
                this.mDcTracker.dump(fd, pw, args);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
        }
        if (getServiceStateTracker() != null) {
            try {
                getServiceStateTracker().dump(fd, pw, args);
            } catch (Exception e22) {
                e22.printStackTrace();
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
        }
        if (getCallTracker() != null) {
            try {
                getCallTracker().dump(fd, pw, args);
            } catch (Exception e222) {
                e222.printStackTrace();
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
        }
        if (this.mCi != null && (this.mCi instanceof RIL)) {
            try {
                ((RIL) this.mCi).dump(fd, pw, args);
            } catch (Exception e2222) {
                e2222.printStackTrace();
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
        }
    }

    public synchronized void cancelAvailableNetworks(Message response) {
        Rlog.d(LOG_TAG, "cancelAvailableNetworks");
        this.mCi.unregisterForGetAvailableNetworksDone(this);
        this.mCi.cancelAvailableNetworks(response);
    }

    public void setNetworkSelectionModeSemiAutomatic(OperatorInfo network, Message response) {
        NetworkSelectMessage nsm = new NetworkSelectMessage();
        nsm.message = response;
        nsm.operatorNumeric = UsimPBMemInfo.STRING_NOT_SET;
        nsm.operatorAlphaLong = UsimPBMemInfo.STRING_NOT_SET;
        Message msg = obtainMessage(17, nsm);
        String actype = "0";
        if (network.getOperatorAlphaLong() != null && network.getOperatorAlphaLong().endsWith(UTRAN_INDICATOR)) {
            actype = "2";
        } else if (network.getOperatorAlphaLong() != null && network.getOperatorAlphaLong().endsWith(LTE_INDICATOR)) {
            actype = ACT_TYPE_LTE;
        }
        this.mCi.setNetworkSelectionModeSemiAutomatic(network.getOperatorNumeric(), actype, msg);
    }

    public void registerForNeighboringInfo(Handler h, int what, Object obj) {
    }

    public void unregisterForNeighboringInfo(Handler h) {
    }

    public void registerForNetworkInfo(Handler h, int what, Object obj) {
        this.mCi.registerForNetworkInfo(h, what, obj);
    }

    public void unregisterForNetworkInfo(Handler h) {
        this.mCi.unregisterForNetworkInfo(h);
    }

    public void refreshSpnDisplay() {
    }

    public int getNetworkHideState() {
        return 0;
    }

    public String getLocatedPlmn() {
        return null;
    }

    public void getFemtoCellList(String operatorNumeric, int rat, Message response) {
        Rlog.d(LOG_TAG, "getFemtoCellList(),operatorNumeric=" + operatorNumeric + ",rat=" + rat);
        this.mCi.getFemtoCellList(operatorNumeric, rat, response);
    }

    public void abortFemtoCellList(Message response) {
        Rlog.d(LOG_TAG, "abortFemtoCellList()");
        this.mCi.abortFemtoCellList(response);
    }

    public void selectFemtoCell(FemtoCellInfo femtocell, Message response) {
        Rlog.d(LOG_TAG, "selectFemtoCell(): " + femtocell);
        this.mCi.selectFemtoCell(femtocell, response);
    }

    public void queryFemtoCellSystemSelectionMode(Message response) {
        Rlog.d(LOG_TAG, "queryFemtoCellSystemSelectionMode()");
        this.mCi.queryFemtoCellSystemSelectionMode(response);
    }

    public void setFemtoCellSystemSelectionMode(int mode, Message response) {
        Rlog.d(LOG_TAG, "setFemtoCellSystemSelectionMode(), mode=" + mode);
        this.mCi.setFemtoCellSystemSelectionMode(mode, response);
    }

    public void getPolCapability(Message onComplete) {
        this.mCi.getPOLCapabilty(onComplete);
    }

    public void getPol(Message onComplete) {
        this.mCi.getCurrentPOLList(onComplete);
    }

    public void setPolEntry(NetworkInfoWithAcT networkWithAct, Message onComplete) {
        this.mCi.setPOLEntry(networkWithAct.getPriority(), networkWithAct.getOperatorNumeric(), networkWithAct.getAccessTechnology(), onComplete);
    }

    public void hangupAll() throws CallStateException {
    }

    public void registerForCipherIndication(Handler h, int what, Object obj) {
        this.mCipherIndicationRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForCipherIndication(Handler h) {
        this.mCipherIndicationRegistrants.remove(h);
    }

    public void getFacilityLock(String facility, String password, Message onComplete) {
    }

    public void setFacilityLock(String facility, boolean enable, String password, Message onComplete) {
    }

    public void getFacilityLockForServiceClass(String facility, String password, int serviceClass, Message onComplete) {
    }

    public void setFacilityLockForServiceClass(String facility, boolean enable, String password, int serviceClass, Message onComplete) {
    }

    public void changeBarringPassword(String facility, String oldPwd, String newPwd, Message onComplete) {
    }

    public Connection dial(List<String> list, int videoState) throws CallStateException {
        return null;
    }

    public boolean isFeatureSupported(FeatureType feature) {
        if (this.mImsPhone != null) {
            return this.mImsPhone.isFeatureSupported(feature);
        }
        Rlog.d(LOG_TAG, "isFeatureSupported = False with " + feature);
        return false;
    }

    public void explicitCallTransfer(String number, int type) {
    }

    public void registerForCrssSuppServiceNotification(Handler h, int what, Object obj) {
    }

    public void unregisterForCrssSuppServiceNotification(Handler h) {
    }

    public void registerForSpeechCodecInfo(Handler h, int what, Object obj) {
        this.mSpeechCodecInfoRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForSpeechCodecInfo(Handler h) {
        this.mSpeechCodecInfoRegistrants.remove(h);
    }

    void notifySpeechCodecInfo(int type) {
        this.mSpeechCodecInfoRegistrants.notifyResult(Integer.valueOf(type));
    }

    public void registerForVtStatusInfo(Handler h, int what, Object obj) {
        this.mCi.registerForVtStatusInfo(h, what, obj);
    }

    public void unregisterForVtStatusInfo(Handler h) {
        this.mCi.unregisterForVtStatusInfo(h);
    }

    public void registerForCdmaCallAccepted(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        this.mCdmaCallAcceptedRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForCdmaCallAccepted(Handler h) {
        this.mCdmaCallAcceptedRegistrants.remove(h);
    }

    public void notifyCdmaCallAccepted() {
        this.mCdmaCallAcceptedRegistrants.notifyRegistrants(new AsyncResult(null, this, null));
    }

    public int getCsFallbackStatus() {
        Rlog.d(LOG_TAG, "getCsFallbackStatus is " + this.mCSFallbackMode);
        return this.mCSFallbackMode;
    }

    public void setCsFallbackStatus(int newStatus) {
        Rlog.d(LOG_TAG, "setCsFallbackStatus to " + newStatus);
        this.mCSFallbackMode = newStatus;
    }

    public void getCallForwardInTimeSlot(int commandInterfaceCFReason, Message onComplete) {
        Rlog.d(LOG_TAG, "whk, need implement this method ");
    }

    public void setCallForwardInTimeSlot(int commandInterfaceCFAction, int commandInterfaceCFReason, String dialingNumber, int timerSeconds, long[] timeSlot, Message onComplete) {
        Rlog.d(LOG_TAG, "whk, need implement this method ");
    }

    public void getCallForwardingOptionForServiceClass(int commandInterfaceCFReason, int serviceClass, Message onComplete) {
    }

    public void setCallForwardingOptionForServiceClass(int commandInterfaceCFReason, int commandInterfaceCFAction, String dialingNumber, int timerSeconds, int serviceClass, Message onComplete) {
    }

    public void saveTimeSlot(long[] timeSlot) {
        String timeSlotKey = CFU_TIME_SLOT + this.mPhoneId;
        String timeSlotString = UsimPBMemInfo.STRING_NOT_SET;
        if (timeSlot != null && timeSlot.length == 2) {
            timeSlotString = Long.toString(timeSlot[0]) + "," + Long.toString(timeSlot[1]);
        }
        SystemProperties.set(timeSlotKey, timeSlotString);
        Rlog.d(LOG_TAG, "timeSlotString = " + timeSlotString);
    }

    public long[] getTimeSlot() {
        String timeSlotString = SystemProperties.get(CFU_TIME_SLOT + this.mPhoneId, UsimPBMemInfo.STRING_NOT_SET);
        long[] timeSlot = null;
        if (!(timeSlotString == null || timeSlotString.equals(UsimPBMemInfo.STRING_NOT_SET))) {
            String[] timeArray = timeSlotString.split(",");
            if (timeArray.length == 2) {
                timeSlot = new long[2];
                for (int i = 0; i < 2; i++) {
                    timeSlot[i] = Long.parseLong(timeArray[i]);
                    Calendar calenar = Calendar.getInstance(TimeZone.getDefault());
                    calenar.setTimeInMillis(timeSlot[i]);
                    int hour = calenar.get(11);
                    int min = calenar.get(12);
                    Calendar calenar2 = Calendar.getInstance(TimeZone.getDefault());
                    calenar2.set(11, hour);
                    calenar2.set(12, min);
                    timeSlot[i] = calenar2.getTimeInMillis();
                }
            }
        }
        Rlog.d(LOG_TAG, "timeSlot = " + Arrays.toString(timeSlot));
        return timeSlot;
    }

    public String getMvnoMatchType() {
        return null;
    }

    public String getMvnoPattern(String type) {
        return null;
    }

    public int getCdmaSubscriptionActStatus() {
        return -1;
    }

    public void doGeneralSimAuthentication(int sessionId, int mode, int tag, String param1, String param2, Message result) {
    }

    public void triggerModeSwitchByEcc(int mode, Message response) {
        this.mCi.triggerModeSwitchByEcc(mode, response);
    }

    public void queryPhbStorageInfo(int type, Message response) {
    }

    public void notifyLteAccessStratumChanged(String state) {
        this.mNotifier.notifyLteAccessStratumChanged(this, state);
    }

    public void notifyPsNetworkTypeChanged(int nwType) {
        this.mNotifier.notifyPsNetworkTypeChanged(this, nwType);
    }

    public void notifySharedDefaultApnStateChanged(boolean isSharedDefaultApn) {
        this.mNotifier.notifySharedDefaultApnStateChanged(this, isSharedDefaultApn);
    }

    void setRxTestConfig(int AntType, Message result) {
        Rlog.d(LOG_TAG, "set Rx Test Config)");
        this.mCi.setRxTestConfig(AntType, result);
    }

    void getRxTestResult(Message result) {
        Rlog.d(LOG_TAG, "get Rx Test Result");
        this.mCi.getRxTestResult(result);
    }

    public void setCurrentStatus(int airplaneMode, int imsReg, Message response) {
        Rlog.d(LOG_TAG, "setCurrentStatus(), airplaneMode=" + airplaneMode + ", imsReg=" + imsReg);
        this.mCi.setCurrentStatus(airplaneMode, imsReg, response);
    }

    public void setEccPreferredRat(int phoneType, Message response) {
        Rlog.d(LOG_TAG, "setEccPreferredRat(), phoneType=" + phoneType);
        this.mCi.setEccPreferredRat(phoneType, response);
    }

    public boolean useVzwLogic() {
        String optr;
        boolean ret = false;
        if (SystemProperties.get("persist.radio.mtk_dsbp_support", "0").equals("1")) {
            optr = SystemProperties.get("persist.radio.sim.sbp", "0");
        } else {
            optr = SystemProperties.get("ro.mtk_md_sbp_custom_value", "0");
        }
        if (optr.equals("12") || optr.equals("20")) {
            ret = true;
        }
        Rlog.d(LOG_TAG, "optr = " + optr + ", useVzwLogic = " + ret);
        return ret;
    }

    public boolean is_test_card() {
        if (this.mIccRecords == null) {
            return false;
        }
        IccRecords mRecords = (IccRecords) this.mIccRecords.get();
        if (mRecords != null) {
            return mRecords.is_test_card();
        }
        return false;
    }

    public String[] getLteCdmaImsi(int phoneid) {
        String[] ImsiList = new String[2];
        ImsiList[0] = UsimPBMemInfo.STRING_NOT_SET;
        ImsiList[1] = UsimPBMemInfo.STRING_NOT_SET;
        SIMRecords sIMRecords = null;
        RuimRecords ruimRecords = null;
        if (this.mUiccController == null) {
            Rlog.d(LOG_TAG, "getLteCdmaImsi mUiccController == null");
            return ImsiList;
        }
        UiccCardApplication newUiccApplication = this.mUiccController.getUiccCardApplication(phoneid, 1);
        UiccCardApplication newUiccApplication2 = this.mUiccController.getUiccCardApplication(phoneid, 2);
        if (newUiccApplication != null) {
            sIMRecords = (SIMRecords) newUiccApplication.getIccRecords();
        }
        if (newUiccApplication2 != null) {
            ruimRecords = (RuimRecords) newUiccApplication2.getIccRecords();
        }
        if (!(ruimRecords == null || ruimRecords.getIMSI() == null)) {
            ImsiList[0] = ruimRecords.getIMSI();
            Rlog.d(LOG_TAG, "getLteCdmaImsi ruim imsi = " + ImsiList[0]);
        }
        if (!(sIMRecords == null || sIMRecords.getIMSI() == null)) {
            ImsiList[1] = sIMRecords.getIMSI();
            Rlog.d(LOG_TAG, "getLteCdmaImsi sim imsi = " + ImsiList[1]);
        }
        Rlog.d(LOG_TAG, "getLteCdmaImsi ImsiList = " + ImsiList);
        return ImsiList;
    }

    public boolean OppoCheckUsimIs4G() {
        UsimServiceTable st = getUsimServiceTable();
        if (st == null) {
            return false;
        }
        return st.isAvailable(UsimService.EPS_MOBILITY_MANAGEMENT_INFO);
    }

    public boolean getOemAutoAnswer() {
        boolean defaultVal = OemConstant.isNwLabTest();
        boolean isAuto = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(OEM_ISAUTO_ANSWER, !defaultVal);
        Rlog.d("oem", "getOemAutoAnswer isAuto:" + isAuto + ", defaultVal " + defaultVal);
        return isAuto;
    }

    public void setOemAutoAnswer(boolean isAuto) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        editor.putBoolean(OEM_ISAUTO_ANSWER, isAuto);
        editor.apply();
    }

    public String getOemSpn() {
        return getServiceStateTracker().getOemSpn();
    }

    public void oppoSetSarRfState(int state) {
        Rlog.d(LOG_TAG, "TBD:oppoSetSarRfState() state : " + state);
    }

    public int oemGetTxPowerBackoffIndex() {
        String product = SystemProperties.get("ro.build.product");
        String product_name = SystemProperties.get("ro.product.name");
        String productHW = SystemProperties.get("ro.product.hw");
        Rlog.d(LOG_TAG, "product:" + product + ", product_name : " + product_name + ", productHW : " + productHW);
        if ("oppo6750_15131".equals(product)) {
            return 1;
        }
        if ("R9k".equals(product_name) || "R6031".equals(product_name)) {
            return 2;
        }
        if ("oppo6755_15111".equals(product)) {
            return 3;
        }
        if ("oppo6750_16021".equals(product)) {
            return 4;
        }
        if ("DC052".equals(productHW) || "DC053".equals(productHW)) {
            return 5;
        }
        if ("BC151".equals(productHW) || "BC153".equals(productHW)) {
            return 6;
        }
        if ("BD011".equals(productHW) || "BD012".equals(productHW)) {
            return 7;
        }
        if ("oppo6750_15311".equals(product)) {
            return 100;
        }
        if ("oppo6750_15331".equals(product)) {
            return 101;
        }
        if ("oppo6750_16321".equals(product)) {
            return 102;
        }
        if ("oppo6750_16391".equals(product)) {
            return 103;
        }
        if ("oppo6750_17351".equals(product)) {
            return 104;
        }
        if ("oppo6763_17321".equals(product) || "DC018".equals(productHW) || "DC020".equals(productHW) || "DC022".equals(productHW)) {
            return 105;
        }
        if ("BC155".equals(productHW)) {
            return 106;
        }
        if ("BC157".equals(productHW)) {
            return OEM_PRODUCT_17373;
        }
        if ("BC159".equals(productHW)) {
            return OEM_PRODUCT_17375;
        }
        if ("BD053".equals(productHW)) {
            return 109;
        }
        if ("BD055".equals(productHW)) {
            return 110;
        }
        if ("BD057".equals(productHW)) {
            return 111;
        }
        return 0;
    }

    /* JADX WARNING: Removed duplicated region for block: B:26:0x0046  */
    /* JADX WARNING: Removed duplicated region for block: B:10:0x0022  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0040 A:{SYNTHETIC, Splitter: B:22:0x0040} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String getOperatorName() {
        Throwable th;
        File file = new File("/proc/oppoVersion/operatorName");
        BufferedReader br = null;
        String operator = UsimPBMemInfo.STRING_NOT_SET;
        try {
            BufferedReader br2 = new BufferedReader(new FileReader(file));
            try {
                operator = br2.readLine();
                if (br2 != null) {
                    try {
                        br2.close();
                    } catch (IOException e) {
                    }
                }
                br = br2;
            } catch (IOException e2) {
                br = br2;
            } catch (Throwable th2) {
                th = th2;
                br = br2;
                if (br != null) {
                }
                throw th;
            }
        } catch (IOException e3) {
            try {
                operator = UsimPBMemInfo.STRING_NOT_SET;
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e4) {
                    }
                }
                if (operator != null) {
                }
            } catch (Throwable th3) {
                th = th3;
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e5) {
                    }
                }
                throw th;
            }
        }
        if (operator != null) {
            Rlog.d(LOG_TAG, "operator is null");
            return UsimPBMemInfo.STRING_NOT_SET;
        }
        Rlog.d(LOG_TAG, "operator:" + operator);
        return operator.trim();
    }

    public void oppoSetSarRfStateV2(int state) {
        Rlog.d(LOG_TAG, "TBD:oppoSetSarRfStateV2() state : " + state);
        this.mSwitchState = state;
        int index = oemGetTxPowerBackoffIndex();
        if (index > 0) {
            switch (state) {
                case 0:
                    txPowerBackOff(false, index, state);
                    return;
                case 1:
                case 3:
                case 5:
                case 7:
                    if (3 == index || 2 == index) {
                        txPowerBackOff(false, index, state);
                        return;
                    } else {
                        txPowerBackOff(true, index, state);
                        return;
                    }
                case 2:
                case 4:
                case 6:
                case 8:
                    if (3 == index || 2 == index) {
                        txPowerBackOff(true, index, state);
                        return;
                    } else {
                        txPowerBackOff(false, index, state);
                        return;
                    }
                default:
                    return;
            }
        }
    }

    public int oppoGetSwitchState() {
        return this.mSwitchState;
    }

    public void txPowerBackOff(boolean enable, int index, int state) {
        Rlog.d(LOG_TAG, "Set tx power back off enable : " + enable + ", index : " + index);
        String[] atCmdGsm = new String[2];
        String[] atCmdWcdma = new String[2];
        String[] atCmdLteB1 = new String[2];
        String[] atCmdLteB2 = new String[2];
        String[] atCmdLteB3 = new String[2];
        String[] atCmdLteB4 = new String[2];
        String[] atCmdLteB5 = new String[2];
        String[] atCmdLteB7 = new String[2];
        String[] atCmdLteB8 = new String[2];
        String[] atCmdLteB38 = new String[2];
        String[] atCmdLteB39 = new String[2];
        String[] atCmdLteB40 = new String[2];
        String[] atCmdLteB41 = new String[2];
        atCmdGsm[0] = UsimPBMemInfo.STRING_NOT_SET;
        atCmdGsm[1] = UsimPBMemInfo.STRING_NOT_SET;
        atCmdWcdma[0] = UsimPBMemInfo.STRING_NOT_SET;
        atCmdWcdma[1] = UsimPBMemInfo.STRING_NOT_SET;
        atCmdLteB1[0] = UsimPBMemInfo.STRING_NOT_SET;
        atCmdLteB1[1] = UsimPBMemInfo.STRING_NOT_SET;
        atCmdLteB2[0] = UsimPBMemInfo.STRING_NOT_SET;
        atCmdLteB2[1] = UsimPBMemInfo.STRING_NOT_SET;
        atCmdLteB3[0] = UsimPBMemInfo.STRING_NOT_SET;
        atCmdLteB3[1] = UsimPBMemInfo.STRING_NOT_SET;
        atCmdLteB4[0] = UsimPBMemInfo.STRING_NOT_SET;
        atCmdLteB4[1] = UsimPBMemInfo.STRING_NOT_SET;
        atCmdLteB5[0] = UsimPBMemInfo.STRING_NOT_SET;
        atCmdLteB5[1] = UsimPBMemInfo.STRING_NOT_SET;
        atCmdLteB7[0] = UsimPBMemInfo.STRING_NOT_SET;
        atCmdLteB7[1] = UsimPBMemInfo.STRING_NOT_SET;
        atCmdLteB8[0] = UsimPBMemInfo.STRING_NOT_SET;
        atCmdLteB8[1] = UsimPBMemInfo.STRING_NOT_SET;
        atCmdLteB38[0] = UsimPBMemInfo.STRING_NOT_SET;
        atCmdLteB38[1] = UsimPBMemInfo.STRING_NOT_SET;
        atCmdLteB39[0] = UsimPBMemInfo.STRING_NOT_SET;
        atCmdLteB39[1] = UsimPBMemInfo.STRING_NOT_SET;
        atCmdLteB40[0] = UsimPBMemInfo.STRING_NOT_SET;
        atCmdLteB40[1] = UsimPBMemInfo.STRING_NOT_SET;
        atCmdLteB41[0] = UsimPBMemInfo.STRING_NOT_SET;
        atCmdLteB41[1] = UsimPBMemInfo.STRING_NOT_SET;
        int b_sim = 0;
        String operatorName = getOperatorName();
        String isTestSim1 = SystemProperties.get("gsm.sim.ril.testsim", "2");
        String isTestSim2 = SystemProperties.get("gsm.sim.ril.testsim.1", "2");
        String SimStatus = SystemProperties.get("gsm.sim.state", "null");
        int ant_pos = Integer.valueOf(SystemProperties.get("nw.ant.pos", "0")).intValue();
        Rlog.d(LOG_TAG, "ant_pos:" + ant_pos);
        if (!(isTestSim1.equals("2") && isTestSim2.equals("2"))) {
            b_sim = 1;
        }
        if (isTestSim1.equals("1") || isTestSim2.equals("1")) {
            b_sim = 2;
        }
        Rlog.d(LOG_TAG, "txPowerBackoff(): SimStatus %d " + b_sim);
        String[] gsmFixDownCmd = new String[2];
        String[] gsmNotFixCmd = new String[2];
        gsmFixDownCmd[0] = "AT+ETXANT=4,1,0,2";
        gsmFixDownCmd[1] = "+ETXANT:";
        gsmNotFixCmd[0] = "AT+ETXANT=3,1,,2";
        gsmNotFixCmd[1] = "+ETXANT:";
        if (enable) {
            switch (index) {
                case 2:
                    atCmdGsm[0] = "AT+ERFTX=3,1,0,0,0,0,0,0,0,0,10,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0";
                    break;
                case 3:
                    atCmdGsm[0] = "AT+ERFTX=3,1,0,0,0,0,0,0,0,0,10,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0";
                    break;
                case 4:
                    atCmdWcdma[0] = "AT+ERFTX=3,2,12,12";
                    atCmdLteB1[0] = "AT+ERFTX=3,3,1,12";
                    atCmdLteB3[0] = "AT+ERFTX=3,3,3,8";
                    break;
                case 5:
                    if (3 == state) {
                        atCmdGsm[0] = "AT+ERFTX=10,1,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0";
                    } else {
                        atCmdGsm[0] = "AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,16,16,16,16,16,16,16,16";
                    }
                    if (1 == WorldPhoneUtil.get3GDivisionDuplexMode()) {
                        atCmdWcdma[0] = "AT+ERFTX=10,2,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,24,24,24,24,,,24,24";
                    }
                    atCmdLteB1[0] = "AT+ERFTX=10,3,1,0,24";
                    atCmdLteB2[0] = "AT+ERFTX=10,3,2,0,24";
                    atCmdLteB3[0] = "AT+ERFTX=10,3,3,0,24";
                    atCmdLteB4[0] = "AT+ERFTX=10,3,4,0,24";
                    atCmdLteB7[0] = "AT+ERFTX=10,3,7,0,24";
                    break;
                case 6:
                    if (1 == WorldPhoneUtil.get3GDivisionDuplexMode()) {
                        atCmdWcdma[0] = "AT+ERFTX=10,2,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,28,28,29,29,,,21,21";
                    }
                    atCmdGsm[0] = "AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12,12";
                    atCmdLteB1[0] = "AT+ERFTX=10,3,1,0,24";
                    atCmdLteB3[0] = "AT+ERFTX=10,3,3,0,24";
                    atCmdLteB4[0] = "AT+ERFTX=10,3,4,0,17";
                    atCmdLteB7[0] = "AT+ERFTX=10,3,7,0,11";
                    break;
                case 7:
                    if (1 == WorldPhoneUtil.get3GDivisionDuplexMode()) {
                        atCmdWcdma[0] = "AT+ERFTX=10,2,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,16,16,12,12";
                    }
                    atCmdLteB1[0] = "AT+ERFTX=10,3,1,0,16";
                    atCmdLteB3[0] = "AT+ERFTX=10,3,3,0,12";
                    atCmdLteB7[0] = "AT+ERFTX=10,3,7,0,12";
                    break;
                case 104:
                    if (!operatorName.equals("5")) {
                        if (!operatorName.equals("3") && !operatorName.equals(ACT_TYPE_LTE)) {
                            Rlog.d(LOG_TAG, "17351 SAR 3 real sim :" + b_sim);
                            break;
                        } else {
                            Rlog.d(LOG_TAG, "17351 SAR 2 real sim :" + b_sim);
                            break;
                        }
                    } else if (!SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("IN")) {
                        Rlog.d(LOG_TAG, "17351 SAR 1 real sim :" + b_sim);
                        break;
                    } else {
                        Rlog.d(LOG_TAG, "17351 SAR 0 real sim :" + b_sim);
                        break;
                    }
                case 105:
                    if (operatorName.equals("11")) {
                        if (SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("IN")) {
                            if ((b_sim == 2 || b_sim == 0) && ant_pos == 1) {
                                atCmdGsm[0] = "AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,39,39,39,39,39,39,39,39,39,39,39,39,39,39,39,39";
                                atCmdWcdma[0] = "AT+ERFTX=10,2,58,58,,,,,,,,,,,,,8,8,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,";
                                atCmdLteB1[0] = "AT+ERFTX=10,3,1,56,0";
                                atCmdLteB3[0] = "AT+ERFTX=10,3,3,58,0";
                                atCmdLteB7[0] = "AT+ERFTX=10,3,7,52,0";
                                atCmdLteB8[0] = "AT+ERFTX=10,3,8,8,0";
                                atCmdLteB38[0] = "AT+ERFTX=10,3,38,0,36";
                                atCmdLteB39[0] = "AT+ERFTX=10,3,39,0,40";
                                atCmdLteB40[0] = "AT+ERFTX=10,3,40,0,22";
                                atCmdLteB41[0] = "AT+ERFTX=10,3,41,0,36";
                            } else if (b_sim == 1) {
                                atCmdGsm[0] = "AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,39,39,39,39,39,39,39,39,39,39,39,39,39,39,39,39";
                                atCmdWcdma[0] = "AT+ERFTX=10,2,,,,,,,,,,,,,,,8,8,,,,,,,,,,,,,,,,,,,,,,,,,58,58,,,,,,,,,";
                                atCmdLteB1[0] = "AT+ERFTX=10,3,1,0,56";
                                atCmdLteB3[0] = "AT+ERFTX=10,3,3,0,58";
                                atCmdLteB7[0] = "AT+ERFTX=10,3,7,0,52";
                                atCmdLteB8[0] = "AT+ERFTX=10,3,8,8,0";
                                atCmdLteB38[0] = "AT+ERFTX=10,3,38,0,36";
                                atCmdLteB39[0] = "AT+ERFTX=10,3,39,0,40";
                                atCmdLteB40[0] = "AT+ERFTX=10,3,40,0,22";
                                atCmdLteB41[0] = "AT+ERFTX=10,3,41,0,36";
                            }
                            if (7 == state && b_sim == 2 && ant_pos != 2) {
                                atCmdGsm[0] = "AT+ERFTX=10,1,0,0,0,0,0,0,0,0,64,64,64,64,64,64,64,64,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,39,39,39,39,39,39,39,39,39,39,39,39,39,39,39,39";
                            }
                        } else {
                            if ((b_sim == 2 || b_sim == 0) && ant_pos == 1) {
                                atCmdGsm[0] = "AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4";
                                atCmdWcdma[0] = "AT+ERFTX=10,2,24,24,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,";
                                atCmdLteB1[0] = "AT+ERFTX=10,3,1,20,0";
                                atCmdLteB3[0] = "AT+ERFTX=10,3,3,24,0";
                                atCmdLteB7[0] = "AT+ERFTX=10,3,7,12,0";
                                atCmdLteB39[0] = "AT+ERFTX=10,3,39,0,6";
                            } else if (b_sim == 1) {
                                atCmdGsm[0] = "AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4";
                                atCmdWcdma[0] = "AT+ERFTX=10,2,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,24,24,,,,,,,,,";
                                atCmdLteB1[0] = "AT+ERFTX=10,3,1,0,20";
                                atCmdLteB3[0] = "AT+ERFTX=10,3,3,0,24";
                                atCmdLteB7[0] = "AT+ERFTX=10,3,7,0,12";
                                atCmdLteB39[0] = "AT+ERFTX=10,3,39,0,6";
                            }
                            if (7 == state && b_sim == 2 && ant_pos != 2) {
                                atCmdGsm[0] = "AT+ERFTX=10,1,0,0,0,0,0,0,0,0,64,64,64,64,64,64,64,64,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4";
                            }
                        }
                    } else if (operatorName.equals("10")) {
                        if ((b_sim == 2 || b_sim == 0) && ant_pos == 1) {
                            atCmdGsm[0] = "AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,7,7,7,7,7,7,7,7";
                            atCmdWcdma[0] = "AT+ERFTX=10,2,22,22,26,26,,,8,8,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,";
                            atCmdLteB1[0] = "AT+ERFTX=10,3,1,18,0";
                            atCmdLteB2[0] = "AT+ERFTX=10,3,2,20,0";
                            atCmdLteB3[0] = "AT+ERFTX=10,3,3,14,0";
                            atCmdLteB4[0] = "AT+ERFTX=10,3,4,14,0";
                            atCmdLteB7[0] = "AT+ERFTX=10,3,7,14,0";
                        } else if (b_sim == 1) {
                            atCmdGsm[0] = "AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,7,7,7,7,7,7,7,7";
                            atCmdWcdma[0] = "AT+ERFTX=10,2,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,22,22,26,26,,,8,8,,,";
                            atCmdLteB1[0] = "AT+ERFTX=10,3,1,0,18";
                            atCmdLteB2[0] = "AT+ERFTX=10,3,2,0,20";
                            atCmdLteB3[0] = "AT+ERFTX=10,3,3,0,14";
                            atCmdLteB4[0] = "AT+ERFTX=10,3,4,0,14";
                            atCmdLteB7[0] = "AT+ERFTX=10,3,7,0,14";
                        }
                        if (7 == state && b_sim == 2 && ant_pos != 2) {
                            atCmdGsm[0] = "AT+ERFTX=10,1,0,0,0,0,0,0,0,0,64,64,64,64,64,64,64,64,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,7,7,7,7,7,7,7,7";
                        }
                    } else if (!operatorName.equals("12")) {
                        Rlog.d(LOG_TAG, "17321: operator is not EU or TW");
                    } else if (SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("IN")) {
                        if ((b_sim == 2 || b_sim == 0) && ant_pos == 1) {
                            atCmdGsm[0] = "AT+ERFTX=10,1,0,0,0,0,0,0,0,0,11,11,11,11,11,11,11,11,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,39,39,39,39,39,39,39,39,39,39,39,39,39,39,39,39";
                            atCmdWcdma[0] = "AT+ERFTX=10,2,58,58,58,58,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,12,12,,,,";
                            atCmdLteB1[0] = "AT+ERFTX=10,3,1,56,0";
                            atCmdLteB2[0] = "AT+ERFTX=10,3,2,52,0";
                            atCmdLteB3[0] = "AT+ERFTX=10,3,3,58,0";
                            atCmdLteB7[0] = "AT+ERFTX=10,3,7,52,0";
                            atCmdLteB38[0] = "AT+ERFTX=10,3,38,0,36";
                            atCmdLteB39[0] = "AT+ERFTX=10,3,39,0,40";
                            atCmdLteB40[0] = "AT+ERFTX=10,3,40,0,22";
                            atCmdLteB41[0] = "AT+ERFTX=10,3,41,0,36";
                        } else if (b_sim == 1) {
                            atCmdGsm[0] = "AT+ERFTX=10,1,0,0,0,0,0,0,0,0,11,11,11,11,11,11,11,11,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,39,39,39,39,39,39,39,39,39,39,39,39,39,39,39,39";
                            atCmdWcdma[0] = "AT+ERFTX=10,2,,,,,,,,,,,,,,,12,12,,,,,,,,,,,,,,,,,,,,,,,,,58,58,58,58,,,,,,,";
                            atCmdLteB1[0] = "AT+ERFTX=10,3,1,0,56";
                            atCmdLteB2[0] = "AT+ERFTX=10,3,2,0,52";
                            atCmdLteB3[0] = "AT+ERFTX=10,3,3,0,58";
                            atCmdLteB7[0] = "AT+ERFTX=10,3,7,0,52";
                            atCmdLteB38[0] = "AT+ERFTX=10,3,38,0,36";
                            atCmdLteB39[0] = "AT+ERFTX=10,3,39,0,40";
                            atCmdLteB40[0] = "AT+ERFTX=10,3,40,0,22";
                            atCmdLteB41[0] = "AT+ERFTX=10,3,41,0,36";
                        }
                        if (7 == state && b_sim == 2 && ant_pos != 2) {
                            atCmdGsm[0] = "AT+ERFTX=10,1,0,0,0,0,0,0,0,0,64,64,64,64,64,64,64,64,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,39,39,39,39,39,39,39,39,39,39,39,39,39,39,39,39";
                        }
                    } else {
                        if ((b_sim == 2 || b_sim == 0) && ant_pos == 1) {
                            atCmdGsm[0] = "AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,7,7,7,7,7,7,7,7";
                            atCmdWcdma[0] = "AT+ERFTX=10,2,22,22,26,26,,,8,8,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,";
                            atCmdLteB1[0] = "AT+ERFTX=10,3,1,18,0";
                            atCmdLteB2[0] = "AT+ERFTX=10,3,2,20,0";
                            atCmdLteB3[0] = "AT+ERFTX=10,3,3,14,0";
                            atCmdLteB4[0] = "AT+ERFTX=10,3,4,14,0";
                            atCmdLteB7[0] = "AT+ERFTX=10,3,7,14,0";
                        } else if (b_sim == 1) {
                            atCmdGsm[0] = "AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,7,7,7,7,7,7,7,7";
                            atCmdWcdma[0] = "AT+ERFTX=10,2,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,22,22,26,26,,,8,8,,,";
                            atCmdLteB1[0] = "AT+ERFTX=10,3,1,0,18";
                            atCmdLteB2[0] = "AT+ERFTX=10,3,2,0,20";
                            atCmdLteB3[0] = "AT+ERFTX=10,3,3,0,14";
                            atCmdLteB4[0] = "AT+ERFTX=10,3,4,0,14";
                            atCmdLteB7[0] = "AT+ERFTX=10,3,7,0,14";
                        }
                        if (7 == state && b_sim == 2 && ant_pos != 2) {
                            atCmdGsm[0] = "AT+ERFTX=10,1,0,0,0,0,0,0,0,0,64,64,64,64,64,64,64,64,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,7,7,7,7,7,7,7,7";
                        }
                    }
                    if (b_sim == 1) {
                        if (7 != state) {
                            invokeOemRilRequestStrings(gsmNotFixCmd, null);
                            break;
                        } else {
                            invokeOemRilRequestStrings(gsmFixDownCmd, null);
                            break;
                        }
                    }
                    break;
                case 106:
                    if ((b_sim == 2 || b_sim == 0) && ant_pos == 1) {
                        atCmdGsm[0] = "AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,8,8,8,8,8,8,8,8,4,4,4,4,4,4,4,4";
                        atCmdWcdma[0] = "AT+ERFTX=10,2,21,21,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,";
                        atCmdLteB1[0] = "AT+ERFTX=10,3,1,17,0";
                        atCmdLteB3[0] = "AT+ERFTX=10,3,3,17,0";
                        atCmdLteB7[0] = "AT+ERFTX=10,3,7,15,0";
                        atCmdLteB39[0] = "AT+ERFTX=10,3,39,0,5";
                    } else if (b_sim == 1) {
                        atCmdGsm[0] = "AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,8,8,8,8,8,8,8,8,4,4,4,4,4,4,4,4";
                        atCmdWcdma[0] = "AT+ERFTX=10,2,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,21,21,,,,,,,,,";
                        atCmdLteB1[0] = "AT+ERFTX=10,3,1,0,17";
                        atCmdLteB3[0] = "AT+ERFTX=10,3,3,0,17";
                        atCmdLteB7[0] = "AT+ERFTX=10,3,7,0,15";
                        atCmdLteB39[0] = "AT+ERFTX=10,3,39,0,5";
                    }
                    if (7 == state && b_sim == 2 && ant_pos != 2) {
                        atCmdGsm[0] = "AT+ERFTX=10,1,0,0,0,0,0,0,0,0,64,64,64,64,64,64,64,64,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,8,8,8,8,8,8,8,8,4,4,4,4,4,4,4,4";
                    }
                    if (b_sim == 1) {
                        if (7 != state) {
                            invokeOemRilRequestStrings(gsmNotFixCmd, null);
                            break;
                        } else {
                            invokeOemRilRequestStrings(gsmFixDownCmd, null);
                            break;
                        }
                    }
                    break;
                case OEM_PRODUCT_17373 /*107*/:
                    if ((b_sim == 2 || b_sim == 0) && ant_pos == 1) {
                        atCmdWcdma[0] = "AT+ERFTX=10,2,,,17,17,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,17,17,,,,,,,,,";
                        atCmdLteB1[0] = "AT+ERFTX=10,3,1,0,17";
                        atCmdLteB2[0] = "AT+ERFTX=10,3,2,17,0";
                        atCmdLteB3[0] = "AT+ERFTX=10,3,3,0,13";
                        atCmdLteB7[0] = "AT+ERFTX=10,3,7,7,0";
                    } else if (b_sim == 1) {
                        atCmdWcdma[0] = "AT+ERFTX=10,2,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,17,17,17,17,,,,,,,";
                        atCmdLteB1[0] = "AT+ERFTX=10,3,1,0,17";
                        atCmdLteB2[0] = "AT+ERFTX=10,3,2,0,17";
                        atCmdLteB3[0] = "AT+ERFTX=10,3,3,0,13";
                        atCmdLteB7[0] = "AT+ERFTX=10,3,7,0,7";
                    }
                    if (7 == state && b_sim == 2 && ant_pos != 2) {
                        atCmdGsm[0] = "AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,64,64,64,64,64,64,64,64,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0";
                    }
                    if (b_sim == 1) {
                        if (7 != state) {
                            invokeOemRilRequestStrings(gsmNotFixCmd, null);
                            break;
                        } else {
                            invokeOemRilRequestStrings(gsmFixDownCmd, null);
                            break;
                        }
                    }
                    break;
                case OEM_PRODUCT_17375 /*108*/:
                    if ((b_sim == 2 || b_sim == 0) && ant_pos == 1) {
                        atCmdGsm[0] = "AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,27,27,27,27,27,27,27,27,31,31,31,31,31,31,31,31";
                        atCmdWcdma[0] = "AT+ERFTX=10,2,53,53,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,";
                        atCmdLteB1[0] = "AT+ERFTX=10,3,1,53,0";
                        atCmdLteB3[0] = "AT+ERFTX=10,3,3,49,0";
                        atCmdLteB7[0] = "AT+ERFTX=10,3,7,43,0";
                        atCmdLteB8[0] = "AT+ERFTX=10,3,8,5,0";
                        atCmdLteB38[0] = "AT+ERFTX=10,3,38,0,37";
                        atCmdLteB39[0] = "AT+ERFTX=10,3,39,0,37";
                        atCmdLteB40[0] = "AT+ERFTX=10,3,40,0,25";
                        atCmdLteB41[0] = "AT+ERFTX=10,3,41,0,37";
                    } else if (b_sim == 1) {
                        atCmdGsm[0] = "AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,27,27,27,27,27,27,27,27,31,31,31,31,31,31,31,31";
                        atCmdWcdma[0] = "AT+ERFTX=10,2,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,53,53,,,,,,,,,";
                        atCmdLteB1[0] = "AT+ERFTX=10,3,1,0,53";
                        atCmdLteB3[0] = "AT+ERFTX=10,3,3,0,49";
                        atCmdLteB7[0] = "AT+ERFTX=10,3,7,0,43";
                        atCmdLteB8[0] = "AT+ERFTX=10,3,8,5,0";
                        atCmdLteB38[0] = "AT+ERFTX=10,3,38,0,37";
                        atCmdLteB39[0] = "AT+ERFTX=10,3,39,0,37";
                        atCmdLteB40[0] = "AT+ERFTX=10,3,40,0,25";
                        atCmdLteB41[0] = "AT+ERFTX=10,3,41,0,37";
                    }
                    if (7 == state && b_sim == 2 && ant_pos != 2) {
                        atCmdGsm[0] = "AT+ERFTX=10,1,0,0,0,0,0,0,0,0,64,64,64,64,64,64,64,64,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,27,27,27,27,27,27,27,27,31,31,31,31,31,31,31,31";
                    }
                    if (b_sim == 1) {
                        if (7 != state) {
                            invokeOemRilRequestStrings(gsmNotFixCmd, null);
                            break;
                        } else {
                            invokeOemRilRequestStrings(gsmFixDownCmd, null);
                            break;
                        }
                    }
                    break;
                case 109:
                    if ((b_sim != 2 && b_sim != 0) || ant_pos != 1) {
                        if (b_sim == 1) {
                            atCmdGsm[0] = "AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8";
                            atCmdWcdma[0] = "AT+ERFTX=10,2,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,11,11,,,,,,,,,";
                            atCmdLteB1[0] = "AT+ERFTX=10,3,1,0,16";
                            atCmdLteB3[0] = "AT+ERFTX=10,3,3,0,16";
                            atCmdLteB7[0] = "AT+ERFTX=10,3,7,0,20";
                            atCmdLteB38[0] = "AT+ERFTX=10,3,38,0,8";
                            atCmdLteB41[0] = "AT+ERFTX=10,3,41,0,8";
                            break;
                        }
                    }
                    atCmdGsm[0] = "AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0";
                    atCmdWcdma[0] = "AT+ERFTX=10,2,11,11,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,";
                    atCmdLteB1[0] = "AT+ERFTX=10,3,1,16,0";
                    atCmdLteB3[0] = "AT+ERFTX=10,3,3,16,0";
                    atCmdLteB7[0] = "AT+ERFTX=10,3,7,20,0";
                    atCmdLteB38[0] = "AT+ERFTX=10,3,38,0,8";
                    atCmdLteB41[0] = "AT+ERFTX=10,3,41,0,8";
                    break;
                    break;
                case 110:
                    if ((b_sim != 2 && b_sim != 0) || ant_pos != 1) {
                        if (b_sim == 1) {
                            atCmdWcdma[0] = "AT+ERFTX=10,2,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,12,12,,,,,,,,,";
                            atCmdLteB1[0] = "AT+ERFTX=10,3,1,0,12";
                            atCmdLteB3[0] = "AT+ERFTX=10,3,3,0,12";
                            atCmdLteB7[0] = "AT+ERFTX=10,3,7,0,12";
                            atCmdLteB41[0] = "AT+ERFTX=10,3,41,0,4";
                            break;
                        }
                    }
                    atCmdWcdma[0] = "AT+ERFTX=10,2,12,12,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,";
                    atCmdLteB1[0] = "AT+ERFTX=10,3,1,12,0";
                    atCmdLteB3[0] = "AT+ERFTX=10,3,3,12,0";
                    atCmdLteB7[0] = "AT+ERFTX=10,3,7,12,0";
                    atCmdLteB41[0] = "AT+ERFTX=10,3,41,0,4";
                    break;
                    break;
                case 111:
                    if (!SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("IN")) {
                        if ((b_sim != 2 && b_sim != 0) || ant_pos != 1) {
                            if (b_sim == 1) {
                                atCmdGsm[0] = "AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8";
                                atCmdWcdma[0] = "AT+ERFTX=10,2,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,11,11,,,,,,,,,";
                                atCmdLteB1[0] = "AT+ERFTX=10,3,1,0,16";
                                atCmdLteB3[0] = "AT+ERFTX=10,3,3,0,16";
                                atCmdLteB7[0] = "AT+ERFTX=10,3,7,0,20";
                                atCmdLteB38[0] = "AT+ERFTX=10,3,38,0,8";
                                atCmdLteB41[0] = "AT+ERFTX=10,3,41,0,8";
                                break;
                            }
                        }
                        atCmdGsm[0] = "AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0";
                        atCmdWcdma[0] = "AT+ERFTX=10,2,11,11,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,";
                        atCmdLteB1[0] = "AT+ERFTX=10,3,1,16,0";
                        atCmdLteB3[0] = "AT+ERFTX=10,3,3,16,0";
                        atCmdLteB7[0] = "AT+ERFTX=10,3,7,20,0";
                        atCmdLteB38[0] = "AT+ERFTX=10,3,38,0,8";
                        atCmdLteB41[0] = "AT+ERFTX=10,3,41,0,8";
                        break;
                    } else if ((b_sim != 2 && b_sim != 0) || ant_pos != 1) {
                        if (b_sim == 1) {
                            atCmdGsm[0] = "AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14,14,14,14,14,14,14,14,10,10,10,10,10,10,10,10";
                            atCmdWcdma[0] = "AT+ERFTX=10,2,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,30,30,,,,,,,,,";
                            atCmdLteB1[0] = "AT+ERFTX=10,3,1,0,32";
                            atCmdLteB3[0] = "AT+ERFTX=10,3,3,0,32";
                            atCmdLteB38[0] = "AT+ERFTX=10,3,38,0,40";
                            atCmdLteB40[0] = "AT+ERFTX=10,3,40,0,24";
                            atCmdLteB41[0] = "AT+ERFTX=10,3,41,0,40";
                            break;
                        }
                    } else {
                        atCmdGsm[0] = "AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,14,14,14,14,14,14,14,14,10,10,10,10,10,10,10,10,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0";
                        atCmdWcdma[0] = "AT+ERFTX=10,2,30,30,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,";
                        atCmdLteB1[0] = "AT+ERFTX=10,3,1,32,0";
                        atCmdLteB3[0] = "AT+ERFTX=10,3,3,32,0";
                        atCmdLteB38[0] = "AT+ERFTX=10,3,38,0,40";
                        atCmdLteB40[0] = "AT+ERFTX=10,3,40,0,24";
                        atCmdLteB41[0] = "AT+ERFTX=10,3,41,0,40";
                        break;
                    }
                    break;
            }
            if (!(atCmdGsm[0] == null || atCmdGsm[0].equals(UsimPBMemInfo.STRING_NOT_SET))) {
                invokeOemRilRequestStrings(atCmdGsm, null);
            }
            if (!(atCmdWcdma[0] == null || atCmdWcdma[0].equals(UsimPBMemInfo.STRING_NOT_SET))) {
                invokeOemRilRequestStrings(atCmdWcdma, null);
            }
            if (!(atCmdLteB1[0] == null || atCmdLteB1[0].equals(UsimPBMemInfo.STRING_NOT_SET))) {
                invokeOemRilRequestStrings(atCmdLteB1, null);
            }
            if (!(atCmdLteB2[0] == null || atCmdLteB2[0].equals(UsimPBMemInfo.STRING_NOT_SET))) {
                invokeOemRilRequestStrings(atCmdLteB2, null);
            }
            if (!(atCmdLteB3[0] == null || atCmdLteB3[0].equals(UsimPBMemInfo.STRING_NOT_SET))) {
                invokeOemRilRequestStrings(atCmdLteB3, null);
            }
            if (!(atCmdLteB4[0] == null || atCmdLteB4[0].equals(UsimPBMemInfo.STRING_NOT_SET))) {
                invokeOemRilRequestStrings(atCmdLteB4, null);
            }
            if (!(atCmdLteB5[0] == null || atCmdLteB5[0].equals(UsimPBMemInfo.STRING_NOT_SET))) {
                invokeOemRilRequestStrings(atCmdLteB5, null);
            }
            if (!(atCmdLteB7[0] == null || atCmdLteB7[0].equals(UsimPBMemInfo.STRING_NOT_SET))) {
                invokeOemRilRequestStrings(atCmdLteB7, null);
            }
            if (!(atCmdLteB8[0] == null || atCmdLteB8[0].equals(UsimPBMemInfo.STRING_NOT_SET))) {
                invokeOemRilRequestStrings(atCmdLteB8, null);
            }
            if (!(atCmdLteB38[0] == null || atCmdLteB38[0].equals(UsimPBMemInfo.STRING_NOT_SET))) {
                invokeOemRilRequestStrings(atCmdLteB38, null);
            }
            if (!(atCmdLteB39[0] == null || atCmdLteB39[0].equals(UsimPBMemInfo.STRING_NOT_SET))) {
                invokeOemRilRequestStrings(atCmdLteB39, null);
            }
            if (!(atCmdLteB40[0] == null || atCmdLteB40[0].equals(UsimPBMemInfo.STRING_NOT_SET))) {
                invokeOemRilRequestStrings(atCmdLteB40, null);
            }
            if (atCmdLteB41[0] != null && !atCmdLteB41[0].equals(UsimPBMemInfo.STRING_NOT_SET)) {
                invokeOemRilRequestStrings(atCmdLteB41, null);
                return;
            }
            return;
        }
        if (5 == index && 4 == state) {
            atCmdGsm[0] = "AT+ERFTX=10,1,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,32,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0";
            if (1 == WorldPhoneUtil.get3GDivisionDuplexMode()) {
                atCmdWcdma[0] = "AT+ERFTX=10,2,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,0,0,0,0,,,0,0";
            }
            atCmdLteB1[0] = "AT+ERFTX=10,3,1,0,0";
            atCmdLteB2[0] = "AT+ERFTX=10,3,2,0,0";
            atCmdLteB3[0] = "AT+ERFTX=10,3,3,0,0";
            atCmdLteB4[0] = "AT+ERFTX=10,3,4,0,0";
            atCmdLteB7[0] = "AT+ERFTX=10,3,7,0,0";
        } else {
            String[] atCmdResetAll = new String[2];
            atCmdResetAll[0] = "AT+ERFTX=1,0,0,0";
            atCmdResetAll[1] = UsimPBMemInfo.STRING_NOT_SET;
            invokeOemRilRequestStrings(atCmdResetAll, null);
        }
        switch (index) {
            case 105:
                if (operatorName.equals("11")) {
                    if (!SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("IN")) {
                        if ((b_sim == 2 || b_sim == 0) && ant_pos == 1) {
                            atCmdWcdma[0] = "AT+ERFTX=10,2,12,12,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,";
                            atCmdLteB1[0] = "AT+ERFTX=10,3,1,14,0";
                            atCmdLteB3[0] = "AT+ERFTX=10,3,3,16,0";
                            atCmdLteB7[0] = "AT+ERFTX=10,3,7,8,0";
                        } else if (b_sim == 1) {
                            atCmdWcdma[0] = "AT+ERFTX=10,2,12,12,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,";
                            atCmdLteB1[0] = "AT+ERFTX=10,3,1,0,14";
                            atCmdLteB3[0] = "AT+ERFTX=10,3,3,0,16";
                            atCmdLteB7[0] = "AT+ERFTX=10,3,7,0,8";
                        }
                    }
                } else if (operatorName.equals("10")) {
                    if ((b_sim == 2 || b_sim == 0) && ant_pos == 1) {
                        atCmdWcdma[0] = "AT+ERFTX=10,2,14,14,16,16,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,";
                        atCmdLteB1[0] = "AT+ERFTX=10,3,1,12,0";
                        atCmdLteB2[0] = "AT+ERFTX=10,3,2,14,0";
                        atCmdLteB3[0] = "AT+ERFTX=10,3,3,4,0";
                        atCmdLteB4[0] = "AT+ERFTX=10,3,4,4,0";
                        atCmdLteB7[0] = "AT+ERFTX=10,3,7,8,0";
                    } else if (b_sim == 1) {
                        atCmdWcdma[0] = "AT+ERFTX=10,2,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,14,14,16,16,,,,,,,";
                        atCmdLteB1[0] = "AT+ERFTX=10,3,1,0,12";
                        atCmdLteB2[0] = "AT+ERFTX=10,3,2,0,14";
                        atCmdLteB3[0] = "AT+ERFTX=10,3,3,0,4";
                        atCmdLteB4[0] = "AT+ERFTX=10,3,4,0,4";
                        atCmdLteB7[0] = "AT+ERFTX=10,3,7,0,8";
                    }
                } else if (!SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("IN")) {
                    if ((b_sim == 2 || b_sim == 0) && ant_pos == 1) {
                        atCmdWcdma[0] = "AT+ERFTX=10,2,14,14,16,16,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,";
                        atCmdLteB1[0] = "AT+ERFTX=10,3,1,12,0";
                        atCmdLteB2[0] = "AT+ERFTX=10,3,2,14,0";
                        atCmdLteB3[0] = "AT+ERFTX=10,3,3,4,0";
                        atCmdLteB4[0] = "AT+ERFTX=10,3,4,4,0";
                        atCmdLteB7[0] = "AT+ERFTX=10,3,7,8,0";
                    } else if (b_sim == 1) {
                        atCmdWcdma[0] = "AT+ERFTX=10,2,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,14,14,16,16,,,,,,,";
                        atCmdLteB1[0] = "AT+ERFTX=10,3,1,0,12";
                        atCmdLteB2[0] = "AT+ERFTX=10,3,2,0,14";
                        atCmdLteB3[0] = "AT+ERFTX=10,3,3,0,4";
                        atCmdLteB4[0] = "AT+ERFTX=10,3,4,0,4";
                        atCmdLteB7[0] = "AT+ERFTX=10,3,7,0,8";
                    }
                }
                if (8 == state && b_sim == 2 && ant_pos != 2) {
                    atCmdGsm[0] = "AT+ERFTX=10,1,0,0,0,0,0,0,0,0,64,64,64,64,64,64,64,64,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0";
                }
                if (b_sim == 1) {
                    if (8 != state) {
                        invokeOemRilRequestStrings(gsmNotFixCmd, null);
                        break;
                    } else {
                        invokeOemRilRequestStrings(gsmFixDownCmd, null);
                        break;
                    }
                }
                break;
            case 106:
                if ((b_sim == 2 || b_sim == 0) && ant_pos == 1) {
                    atCmdWcdma[0] = "AT+ERFTX=10,2,17,17,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,";
                    atCmdLteB1[0] = "AT+ERFTX=10,3,1,13,0";
                    atCmdLteB3[0] = "AT+ERFTX=10,3,3,13,0";
                    atCmdLteB7[0] = "AT+ERFTX=10,3,7,15,0";
                } else if (b_sim == 1) {
                    atCmdWcdma[0] = "AT+ERFTX=10,2,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,17,17,,,,,,,,,";
                    atCmdLteB1[0] = "AT+ERFTX=10,3,1,0,13";
                    atCmdLteB3[0] = "AT+ERFTX=10,3,3,0,13";
                    atCmdLteB7[0] = "AT+ERFTX=10,3,7,0,15";
                }
                if (8 == state && b_sim == 2 && ant_pos != 2) {
                    atCmdGsm[0] = "AT+ERFTX=10,1,0,0,0,0,0,0,0,0,64,64,64,64,64,64,64,64,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0";
                }
                if (b_sim == 1) {
                    if (8 != state) {
                        invokeOemRilRequestStrings(gsmNotFixCmd, null);
                        break;
                    } else {
                        invokeOemRilRequestStrings(gsmFixDownCmd, null);
                        break;
                    }
                }
                break;
            case OEM_PRODUCT_17373 /*107*/:
                if ((b_sim == 2 || b_sim == 0) && ant_pos == 1) {
                    atCmdWcdma[0] = "AT+ERFTX=10,2,,,17,17,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,17,17,,,,,,,,,";
                    atCmdLteB1[0] = "AT+ERFTX=10,3,1,0,17";
                    atCmdLteB2[0] = "AT+ERFTX=10,3,2,9,0";
                    atCmdLteB3[0] = "AT+ERFTX=10,3,3,0,5";
                    atCmdLteB7[0] = "AT+ERFTX=10,3,7,7,0";
                } else if (b_sim == 1) {
                    atCmdWcdma[0] = "AT+ERFTX=10,2,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,17,17,17,17,,,,,,,";
                    atCmdLteB1[0] = "AT+ERFTX=10,3,1,0,17";
                    atCmdLteB2[0] = "AT+ERFTX=10,3,2,0,9";
                    atCmdLteB3[0] = "AT+ERFTX=10,3,3,0,5";
                    atCmdLteB7[0] = "AT+ERFTX=10,3,7,0,7";
                }
                if (8 == state && b_sim == 2 && ant_pos != 2) {
                    atCmdGsm[0] = "AT+ERFTX=10,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,64,64,64,64,64,64,64,64,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0";
                }
                if (b_sim == 1) {
                    if (8 != state) {
                        invokeOemRilRequestStrings(gsmNotFixCmd, null);
                        break;
                    } else {
                        invokeOemRilRequestStrings(gsmFixDownCmd, null);
                        break;
                    }
                }
                break;
            case OEM_PRODUCT_17375 /*108*/:
                if (8 == state && b_sim == 2 && ant_pos != 2) {
                    atCmdGsm[0] = "AT+ERFTX=10,1,0,0,0,0,0,0,0,0,64,64,64,64,64,64,64,64,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0";
                }
                if (b_sim == 1) {
                    if (8 != state) {
                        invokeOemRilRequestStrings(gsmNotFixCmd, null);
                        break;
                    } else {
                        invokeOemRilRequestStrings(gsmFixDownCmd, null);
                        break;
                    }
                }
                break;
            case 109:
                if ((b_sim != 2 && b_sim != 0) || ant_pos != 1) {
                    if (b_sim == 1) {
                        atCmdLteB3[0] = "AT+ERFTX=10,3,3,0,4";
                        atCmdLteB7[0] = "AT+ERFTX=10,3,7,0,16";
                        break;
                    }
                }
                atCmdLteB3[0] = "AT+ERFTX=10,3,3,4,0";
                atCmdLteB7[0] = "AT+ERFTX=10,3,7,16,0";
                break;
                break;
            case 111:
                if (!SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("IN")) {
                    if ((b_sim != 2 && b_sim != 0) || ant_pos != 1) {
                        if (b_sim == 1) {
                            atCmdLteB3[0] = "AT+ERFTX=10,3,3,0,4";
                            atCmdLteB7[0] = "AT+ERFTX=10,3,7,0,16";
                            break;
                        }
                    }
                    atCmdLteB3[0] = "AT+ERFTX=10,3,3,4,0";
                    atCmdLteB7[0] = "AT+ERFTX=10,3,7,16,0";
                    break;
                }
                break;
        }
        if (!(atCmdGsm[0] == null || atCmdGsm[0].equals(UsimPBMemInfo.STRING_NOT_SET))) {
            invokeOemRilRequestStrings(atCmdGsm, null);
        }
        if (!(atCmdWcdma[0] == null || atCmdWcdma[0].equals(UsimPBMemInfo.STRING_NOT_SET))) {
            invokeOemRilRequestStrings(atCmdWcdma, null);
        }
        if (!(atCmdLteB1[0] == null || atCmdLteB1[0].equals(UsimPBMemInfo.STRING_NOT_SET))) {
            invokeOemRilRequestStrings(atCmdLteB1, null);
        }
        if (!(atCmdLteB2[0] == null || atCmdLteB2[0].equals(UsimPBMemInfo.STRING_NOT_SET))) {
            invokeOemRilRequestStrings(atCmdLteB2, null);
        }
        if (!(atCmdLteB3[0] == null || atCmdLteB3[0].equals(UsimPBMemInfo.STRING_NOT_SET))) {
            invokeOemRilRequestStrings(atCmdLteB3, null);
        }
        if (!(atCmdLteB4[0] == null || atCmdLteB4[0].equals(UsimPBMemInfo.STRING_NOT_SET))) {
            invokeOemRilRequestStrings(atCmdLteB4, null);
        }
        if (atCmdLteB7[0] != null && !atCmdLteB7[0].equals(UsimPBMemInfo.STRING_NOT_SET)) {
            invokeOemRilRequestStrings(atCmdLteB7, null);
        }
    }

    public void oemMigrateFrom() {
        if (this.mImsPhone != null) {
            oemMigrate(this.mHandoverRegistrants, this.mImsPhone.mHandoverRegistrants);
            CallTracker ct = this.mImsPhone.getCallTracker();
            if (ct != null) {
                ct.setOemStates(State.IDLE);
            }
        }
    }

    public void oemMigrate(RegistrantList to, RegistrantList from) {
        from.removeCleared();
        int n = from.size();
        for (int i = 0; i < n; i++) {
            Registrant r = (Registrant) from.get(i);
            Message msg = r.messageForRegistrant();
            if (msg == null) {
                Rlog.d(LOG_TAG, "msg is null");
            } else if (msg.obj != CallManager.getInstance().getRegistrantIdentifier()) {
                boolean issame = false;
                int k = to.size();
                for (int j = 0; j < k; j++) {
                    if (((Registrant) to.get(j)) == r) {
                        issame = true;
                        break;
                    }
                }
                Rlog.d(LOG_TAG, "leon oemMigrate:" + issame);
                if (!issame) {
                    to.add(r);
                }
            }
        }
    }

    public boolean isSRVCC() {
        return this.mSrvccState == SrvccState.STARTED;
    }

    public void setPeningSRVCC(boolean bl) {
        this.mIsPendingSRVCC = bl;
    }

    public boolean hasHoRegistrants() {
        return this.mHandoverRegistrants.size() > 0;
    }

    public boolean getBooleanCarrierConfig(String key) {
        CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        Rlog.d(LOG_TAG, "getBooleanCarrierConfig: subId=" + getSubId());
        PersistableBundle b = null;
        if (configManager != null) {
            b = configManager.getConfigForSubId(getSubId());
        }
        if (b != null) {
            return b.getBoolean(key);
        }
        return CarrierConfigManager.getDefaultConfig().getBoolean(key);
    }
}
