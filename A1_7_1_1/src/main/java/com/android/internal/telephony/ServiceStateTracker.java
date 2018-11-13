package com.android.internal.telephony;

import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.os.AsyncResult;
import android.os.BaseBundle;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.PowerManager.WakeLock;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.preference.PreferenceManager;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.provider.oppo.Telephony.WapPush;
import android.telephony.CarrierConfigManager;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionManager.OnSubscriptionsChangedListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.LocalLog;
import android.util.Pair;
import android.util.TimeUtils;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.CommandsInterface.RadioState;
import com.android.internal.telephony.PhoneConstants.State;
import com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.gsm.OppoGsmServiceStateTracker;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.regionlock.RegionLockConstant;
import com.android.internal.telephony.regionlock.RegionLockPlmnListService;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppState;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IccRefreshResponse;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.RuimRecords;
import com.android.internal.telephony.uicc.SIMRecords;
import com.android.internal.telephony.uicc.SpnOverride;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.util.IndentingPrintWriter;
import com.mediatek.common.telephony.IServiceStateExt;
import com.mediatek.internal.telephony.RadioCapabilitySwitchUtil;
import com.mediatek.internal.telephony.RadioManager;
import com.mediatek.internal.telephony.cdma.pluscode.IPlusCodeUtils;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

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
public class ServiceStateTracker extends Handler {
    /* renamed from: -com-android-internal-telephony-CommandsInterface$RadioStateSwitchesValues */
    private static final /* synthetic */ int[] f16xba025bb = null;
    private static final String ACTION_RADIO_OFF = "android.intent.action.ACTION_RADIO_OFF";
    protected static final int ALLOWED_NO_SERVICE_INTERVAL = 23000;
    public static final int CS_DISABLED = 1004;
    public static final int CS_EMERGENCY_ENABLED = 1006;
    public static final int CS_ENABLED = 1003;
    public static final int CS_NORMAL_ENABLED = 1005;
    public static final int CS_NOTIFICATION = 999;
    protected static final boolean DBG = false;
    public static final int DEFAULT_GPRS_CHECK_PERIOD_MILLIS = 60000;
    public static final String DEFAULT_MNC = "00";
    protected static final int EVENT_ALL_DATA_DISCONNECTED = 49;
    protected static final int EVENT_CDMA_PRL_VERSION_CHANGED = 40;
    protected static final int EVENT_CDMA_SUBSCRIPTION_SOURCE_CHANGED = 39;
    protected static final int EVENT_CHANGE_IMS_STATE = 45;
    protected static final int EVENT_CHECK_REPORT_GPRS = 22;
    protected static final int EVENT_CS_NETWORK_STATE_CHANGED = 119;
    protected static final int EVENT_DATA_CONNECTION_DETACHED = 100;
    protected static final int EVENT_DISABLE_EMMRRS_STATUS = 104;
    protected static final int EVENT_ENABLE_EMMRRS_STATUS = 105;
    protected static final int EVENT_ERI_FILE_LOADED = 36;
    protected static final int EVENT_ETS_DEV_CHANGED_LOGGER = 205;
    protected static final int EVENT_FEMTO_CELL_INFO = 107;
    protected static final int EVENT_GET_AVAILABLE_NETWORK_DONE = 51;
    protected static final int EVENT_GET_CELL_INFO_LIST = 43;
    protected static final int EVENT_GET_CELL_INFO_LIST_BY_RATE = 108;
    protected static final int EVENT_GET_LOC_DONE = 15;
    protected static final int EVENT_GET_PREFERRED_NETWORK_TYPE = 19;
    protected static final int EVENT_GET_SIGNAL_STRENGTH = 3;
    public static final int EVENT_ICC_CHANGED = 42;
    protected static final int EVENT_ICC_REFRESH = 106;
    protected static final int EVENT_IMEI_LOCK = 103;
    protected static final int EVENT_IMS_CAPABILITY_CHANGED = 48;
    protected static final int EVENT_IMS_DISABLED_URC = 111;
    protected static final int EVENT_IMS_REGISTRATION_INFO = 112;
    protected static final int EVENT_IMS_STATE_CHANGED = 46;
    protected static final int EVENT_IMS_STATE_DONE = 47;
    protected static final int EVENT_INVALID_SIM_INFO = 101;
    protected static final int EVENT_LOCATION_UPDATES_ENABLED = 18;
    protected static final int EVENT_MODULATION_INFO = 117;
    protected static final int EVENT_NETWORK_EVENT = 118;
    protected static final int EVENT_NETWORK_STATE_CHANGED = 2;
    protected static final int EVENT_NITZ_TIME = 11;
    protected static final int EVENT_NV_READY = 35;
    protected static final int EVENT_OEM_SCREEN_CHANGED = 150;
    private static final int EVENT_OEM_SMOOTH_0 = 301;
    private static final int EVENT_OEM_SMOOTH_1 = 302;
    protected static final int EVENT_OTA_PROVISION_STATUS_CHANGE = 37;
    protected static final int EVENT_PHONE_TYPE_SWITCHED = 50;
    protected static final int EVENT_POLL_SIGNAL_STRENGTH = 10;
    protected static final int EVENT_POLL_STATE_CDMA_SUBSCRIPTION = 34;
    protected static final int EVENT_POLL_STATE_GPRS = 5;
    protected static final int EVENT_POLL_STATE_NETWORK_SELECTION_MODE = 14;
    protected static final int EVENT_POLL_STATE_OPERATOR = 6;
    protected static final int EVENT_POLL_STATE_REGISTRATION = 4;
    protected static final int EVENT_PS_NETWORK_STATE_CHANGED = 102;
    protected static final int EVENT_PS_NETWORK_TYPE_CHANGED = 113;
    protected static final int EVENT_RADIO_AVAILABLE = 13;
    protected static final int EVENT_RADIO_ON = 41;
    protected static final int EVENT_RADIO_STATE_CHANGED = 1;
    protected static final int EVENT_RESET_PREFERRED_NETWORK_TYPE = 21;
    protected static final int EVENT_RESTRICTED_STATE_CHANGED = 23;
    protected static final int EVENT_RUIM_READY = 26;
    protected static final int EVENT_RUIM_RECORDS_LOADED = 27;
    protected static final int EVENT_SET_AUTO_SELECT_NETWORK_DONE = 50;
    protected static final int EVENT_SET_IMS_DISABLE_DONE = 110;
    protected static final int EVENT_SET_IMS_ENABLED_DONE = 109;
    protected static final int EVENT_SET_PREFERRED_NETWORK_TYPE = 20;
    protected static final int EVENT_SET_RADIO_POWER_OFF = 38;
    protected static final int EVENT_SIGNAL_STRENGTH_UPDATE = 12;
    protected static final int EVENT_SIM_OPL_LOADED = 120;
    protected static final int EVENT_SIM_READY = 17;
    protected static final int EVENT_SIM_RECORDS_LOADED = 16;
    protected static final int EVENT_UNSOL_CELL_INFO_LIST = 44;
    private static final String GAME_PACKAGE_NAME = "com.tencent.tmgp.sgame";
    protected static final String[] GMT_COUNTRY_CODES = null;
    public static final String INVALID_MCC = "000";
    protected static boolean IS_OEM_SMOOTH = false;
    private static final long LAST_CELL_INFO_LIST_MAX_AGE_MS = 2000;
    private static final String LOG_TAG = "SST";
    private static final int MAX_NITZ_YEAR = 2037;
    public static final int MS_PER_HOUR = 3600000;
    public static final int NITZ_UPDATE_DIFF_DEFAULT = 2000;
    public static final int NITZ_UPDATE_SPACING_DEFAULT = 600000;
    protected static final int OOS_DELAY_NONE = 0;
    protected static final int OOS_DELAY_TIMEOUT = 2;
    protected static final int OOS_DELAY_TIMING = 1;
    protected static final int OPPOEVENT_CHECK_NO_SERVICE = 666;
    public static final int OTASP_NEEDED = 2;
    public static final int OTASP_NOT_NEEDED = 3;
    public static final int OTASP_SIM_UNPROVISIONED = 5;
    public static final int OTASP_UNINITIALIZED = 0;
    public static final int OTASP_UNKNOWN = 1;
    private static final int POLL_PERIOD_MILLIS = 10000;
    protected static final String PROPERTY_AUTO_RAT_SWITCH = "persist.radio.autoratswitch";
    private static final String PROP_FORCE_ROAMING = "telephony.test.forceRoaming";
    public static final int PS_DISABLED = 1002;
    public static final int PS_ENABLED = 1001;
    public static final int PS_NOTIFICATION = 888;
    protected static final String REGISTRATION_DENIED_AUTH = "Authentication Failure";
    protected static final String REGISTRATION_DENIED_GEN = "General";
    static final int REJECT_NOTIFICATION = 890;
    private static final int SIGNAL_SMOOTH_TIMER = 20000;
    static final int SPECIAL_CARD_TYPE_NOTIFICATION = 8903;
    protected static final String TIMEZONE_PROPERTY = "persist.sys.timezone";
    public static final String UNACTIVATED_MIN2_VALUE = "000000";
    public static final String UNACTIVATED_MIN_VALUE = "1111110111";
    private static final boolean VDBG = false;
    public static final String WAKELOCK_TAG = "ServiceStateTracker";
    public static final String[][] customEhplmn = null;
    public static final String[][] customOperatorConsiderRoamingMcc = null;
    public static boolean mAlreadyUpdated;
    private static long[] mBeginNoServiceTime;
    private static Timer mCellInfoTimer;
    private static long[] mCid;
    private static long mCidChangeCount;
    public static int mDataCallCount;
    private static final boolean mEngLoad = false;
    private static int mLogLv;
    public static long mNoServiceTime;
    public static double mReselectCount;
    public static double mReselectCountPerMin;
    public static int mSMSSendCount;
    private static boolean[] sReceiveNitz;
    protected long OosStartTime;
    public long SAMPLE_TIME;
    protected boolean bCheckNoServiceAgain;
    protected boolean bHasDetachedDuringPolling;
    public boolean dontUpdateNetworkStateFlag;
    private int explict_update_spn;
    private int gprsState;
    public boolean hasPendingPollState;
    private boolean isCsInvalidCard;
    protected boolean isRemoveCard;
    private String iso;
    protected long lastTime;
    private boolean mAlarmSwitch;
    private final LocalLog mAttachLog;
    protected RegistrantList mAttachedRegistrants;
    private ContentObserver mAutoTimeObserver;
    private ContentObserver mAutoTimeZoneObserver;
    private int mCallingPhoneId;
    private RegistrantList mCdmaForSubscriptionInfoReadyRegistrants;
    private CdmaSubscriptionSourceManager mCdmaSSM;
    protected int mCellInfoRate;
    public CellLocation mCellLoc;
    private CommandsInterface mCi;
    private ContentResolver mCr;
    private String mCsgId;
    private String mCurDataSpn;
    private String mCurPlmn;
    private boolean mCurShowPlmn;
    private boolean mCurShowSpn;
    private String mCurSpn;
    private String mCurrentCarrier;
    private int mCurrentOtaspMode;
    private boolean mCurrentUpdateOOSTime;
    private ContentObserver mDataConnectionSettingObserver;
    private RegistrantList mDataRegStateOrRatChangedRegistrants;
    private boolean mDataRoaming;
    private RegistrantList mDataRoamingOffRegistrants;
    private RegistrantList mDataRoamingOnRegistrants;
    private RegistrantList mDataRoamingTypeChangedRegistrants;
    private int mDefaultRoamingIndicator;
    private boolean mDesiredPowerState;
    protected RegistrantList mDetachedRegistrants;
    private boolean mDeviceShuttingDown;
    private boolean mDontPollSignalStrength;
    private boolean mEmergencyOnly;
    protected boolean mEriTriggeredPollState;
    private boolean mEverIVSR;
    private int mFemtocellDomain;
    private boolean mFirstRadioChange;
    private boolean mGotCountryCode;
    private boolean mGsmRoaming;
    private HbpcdUtils mHbpcdUtils;
    private String mHhbName;
    private int[] mHomeNetworkId;
    private int[] mHomeSystemId;
    private IccRecords mIccRecords;
    private boolean mImsRegistered;
    private boolean mImsRegistrationOnOff;
    private BroadcastReceiver mIntentReceiver;
    private boolean mIsEriTextLoaded;
    private int mIsFemtocell;
    private boolean mIsForceSendScreenOnForUpdateNwInfo;
    private boolean mIsImeiLock;
    private boolean mIsInPrl;
    private boolean mIsMinInfoReady;
    private boolean mIsPendingNotify_0;
    private boolean mIsPendingNotify_1;
    protected boolean mIsScreenOn;
    private boolean mIsSubscriptionFromRuim;
    private List<CellInfo> mLastCellInfoList;
    private long mLastCellInfoListTime;
    protected ServiceState mLastCombinedSS;
    private String mLastPSRegisteredPLMN;
    private String mLastRegisteredPLMN;
    protected SignalStrength mLastSignalStrength;
    private boolean mLastUpdateOOSTime;
    private String mLocatedPlmn;
    private int mMaxDataCalls;
    private String mMdn;
    private String mMin;
    private ContentObserver mMsicFeatureConfigObserver;
    private boolean mNeedFixZoneAfterNitz;
    private boolean mNeedNotify;
    private boolean mNeedSetDds1;
    private boolean mNeedSetDds2;
    private RegistrantList mNetworkAttachedRegistrants;
    private boolean mNetworkExsit;
    public CellLocation mNewCellLoc;
    private int mNewMaxDataCalls;
    protected String mNewPlmn;
    private int mNewReasonDataDenied;
    private ServiceState mNewSS;
    private int mNitzUpdateDiff;
    private int mNitzUpdateSpacing;
    private boolean mNitzUpdatedTime;
    private Notification mNotification;
    private Builder mNotificationBuilder;
    private int mOEMCurLevel_0;
    private int mOEMCurLevel_1;
    private int mOEMLastLevel_0;
    private int mOEMLastLevel_1;
    protected String mOemSpn;
    private final SstSubscriptionsChangedListener mOnSubscriptionsChangedListener;
    protected int mOosDelayState;
    protected boolean mOppoNeedNotify;
    private boolean mOppoNeedSetAlarm;
    private boolean mOppoNeedSetRadio;
    protected boolean mPendingPsRestrictDisabledNotify;
    private boolean mPendingRadioPowerOffAfterDataOff;
    private int mPendingRadioPowerOffAfterDataOffTag;
    private GsmCdmaPhone mPhone;
    private final LocalLog mPhoneTypeLog;
    private IPlusCodeUtils mPlusCodeUtils;
    private int[] mPollingContext;
    private boolean mPowerOffDelayNeed;
    private int mPreferredNetworkType;
    private String mPrlVersion;
    private int mPsRegState;
    private int mPsRegStateRaw;
    private RegistrantList mPsRestrictDisabledRegistrants;
    private RegistrantList mPsRestrictEnabledRegistrants;
    private boolean mRadioDisabledByCarrier;
    private PendingIntent mRadioOffIntent;
    private final LocalLog mRatLog;
    private final RatRatcheter mRatRatcheter;
    private int mReasonDataDenied;
    private RegionLockPlmnListService mRegionLockPlmnList;
    private String mRegistrationDeniedReason;
    private int mRegistrationState;
    private boolean mReportedGprsNoReg;
    private PendingIntent mResetIntentSlot1;
    private PendingIntent mResetIntentSlot2;
    public RestrictedState mRestrictedState;
    protected int mRoaming;
    private int mRoamingIndicator;
    private final LocalLog mRoamingLog;
    public ServiceState mSS;
    private long mSavedAtTime;
    private long mSavedTime;
    private String mSavedTimeZone;
    public long mScreenOffTime;
    public long mScreenOnTime;
    private IServiceStateExt mServiceStateExt;
    protected boolean mShowPlmn;
    protected boolean mShowSPn;
    private SignalStrength mSignalStrength;
    protected RegistrantList mSignalStrengthChangedRegistrants;
    private String mSimType;
    protected boolean mSmsCapable;
    private boolean mSpnUpdatePending;
    private boolean mStartedGprsRegCheck;
    private int mSubId;
    private SubscriptionController mSubscriptionController;
    private SubscriptionManager mSubscriptionManager;
    protected int mTimeCount;
    private String[][] mTimeZoneIdByMcc;
    private String[][] mTimeZoneIdOfCapitalCity;
    private UiccCardApplication mUiccApplcation;
    private UiccController mUiccController;
    private boolean mVoiceCapable;
    private RegistrantList mVoiceRoamingOffRegistrants;
    private RegistrantList mVoiceRoamingOnRegistrants;
    private WakeLock mWakeLock;
    private boolean mWantContinuousLocationUpdates;
    private boolean mWantSingleLocationUpdate;
    private boolean mZoneDst;
    private int mZoneOffset;
    private long mZoneTime;
    private String mcc;
    protected int mphoneid;
    private int newGPRSState;
    private int oosFlag;
    int psCid;
    int psLac;
    private boolean voiceUrcWith4G;
    private String wfcDataSpnFormat;
    private String wfcVoiceSpnFormat;

    /* renamed from: com.android.internal.telephony.ServiceStateTracker$4 */
    class AnonymousClass4 extends ContentObserver {
        final /* synthetic */ ServiceStateTracker this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.ServiceStateTracker.4.<init>(com.android.internal.telephony.ServiceStateTracker, android.os.Handler):void, dex: 
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
        AnonymousClass4(com.android.internal.telephony.ServiceStateTracker r1, android.os.Handler r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.ServiceStateTracker.4.<init>(com.android.internal.telephony.ServiceStateTracker, android.os.Handler):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.ServiceStateTracker.4.<init>(com.android.internal.telephony.ServiceStateTracker, android.os.Handler):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.ServiceStateTracker.4.onChange(boolean):void, dex: 
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
        public void onChange(boolean r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.ServiceStateTracker.4.onChange(boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.ServiceStateTracker.4.onChange(boolean):void");
        }
    }

    /* renamed from: com.android.internal.telephony.ServiceStateTracker$5 */
    class AnonymousClass5 extends ContentObserver {
        final /* synthetic */ ServiceStateTracker this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.ServiceStateTracker.5.<init>(com.android.internal.telephony.ServiceStateTracker, android.os.Handler):void, dex: 
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
        AnonymousClass5(com.android.internal.telephony.ServiceStateTracker r1, android.os.Handler r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.ServiceStateTracker.5.<init>(com.android.internal.telephony.ServiceStateTracker, android.os.Handler):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.ServiceStateTracker.5.<init>(com.android.internal.telephony.ServiceStateTracker, android.os.Handler):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.ServiceStateTracker.5.onChange(boolean):void, dex: 
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
        public void onChange(boolean r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.ServiceStateTracker.5.onChange(boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.ServiceStateTracker.5.onChange(boolean):void");
        }
    }

    /* renamed from: com.android.internal.telephony.ServiceStateTracker$6 */
    class AnonymousClass6 extends Thread {
        final /* synthetic */ ServiceStateTracker this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.ServiceStateTracker.6.<init>(com.android.internal.telephony.ServiceStateTracker):void, dex: 
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
        AnonymousClass6(com.android.internal.telephony.ServiceStateTracker r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.ServiceStateTracker.6.<init>(com.android.internal.telephony.ServiceStateTracker):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.ServiceStateTracker.6.<init>(com.android.internal.telephony.ServiceStateTracker):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.ServiceStateTracker.6.run():void, dex: 
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
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.ServiceStateTracker.6.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.ServiceStateTracker.6.run():void");
        }
    }

    private class CellInfoResult {
        List<CellInfo> list;
        Object lockObj;
        final /* synthetic */ ServiceStateTracker this$0;

        /* synthetic */ CellInfoResult(ServiceStateTracker this$0, CellInfoResult cellInfoResult) {
            this(this$0);
        }

        private CellInfoResult(ServiceStateTracker this$0) {
            this.this$0 = this$0;
            this.lockObj = new Object();
        }
    }

    private class SstSubscriptionsChangedListener extends OnSubscriptionsChangedListener {
        public final AtomicInteger mPreviousSubId;
        final /* synthetic */ ServiceStateTracker this$0;

        /* synthetic */ SstSubscriptionsChangedListener(ServiceStateTracker this$0, SstSubscriptionsChangedListener sstSubscriptionsChangedListener) {
            this(this$0);
        }

        private SstSubscriptionsChangedListener(ServiceStateTracker this$0) {
            this.this$0 = this$0;
            this.mPreviousSubId = new AtomicInteger(-1);
        }

        public void onSubscriptionsChanged() {
            int subId = this.this$0.mPhone.getSubId();
            if (ServiceStateTracker.DBG) {
                this.this$0.log("SubscriptionListener.onSubscriptionInfoChanged start " + subId);
            }
            if (this.mPreviousSubId.getAndSet(subId) != subId) {
                if (SubscriptionManager.isValidSubscriptionId(subId)) {
                    Context context = this.this$0.mPhone.getContext();
                    this.this$0.mPhone.notifyPhoneStateChanged();
                    this.this$0.mPhone.notifyCallForwardingIndicator();
                    this.this$0.mPhone.sendSubscriptionSettings(!context.getResources().getBoolean(17956961));
                    this.this$0.mPhone.setSystemProperty("gsm.network.type", ServiceState.rilRadioTechnologyToString(this.this$0.mSS.getRilDataRadioTechnology()));
                    if (this.this$0.mSpnUpdatePending) {
                        this.this$0.mSubscriptionController.setPlmnSpn(this.this$0.mPhone.getPhoneId(), this.this$0.mCurShowPlmn, this.this$0.mCurPlmn, this.this$0.mCurShowSpn, this.this$0.mCurSpn);
                        this.this$0.mSpnUpdatePending = false;
                    }
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                    String oldNetworkSelection = sp.getString(Phone.NETWORK_SELECTION_KEY, UsimPBMemInfo.STRING_NOT_SET);
                    String oldNetworkSelectionName = sp.getString(Phone.NETWORK_SELECTION_NAME_KEY, UsimPBMemInfo.STRING_NOT_SET);
                    String oldNetworkSelectionShort = sp.getString(Phone.NETWORK_SELECTION_SHORT_KEY, UsimPBMemInfo.STRING_NOT_SET);
                    if (this.this$0.mPhone.getContext().getResources().getBoolean(17956961)) {
                        sp.edit().remove(Phone.NETWORK_SELECTION_KEY + subId).remove(Phone.NETWORK_SELECTION_NAME_KEY + subId).remove(Phone.NETWORK_SELECTION_SHORT_KEY + subId).commit();
                    } else if (!(TextUtils.isEmpty(oldNetworkSelection) && TextUtils.isEmpty(oldNetworkSelectionName) && TextUtils.isEmpty(oldNetworkSelectionShort))) {
                        Editor editor = sp.edit();
                        editor.putString(Phone.NETWORK_SELECTION_KEY + subId, oldNetworkSelection);
                        editor.putString(Phone.NETWORK_SELECTION_NAME_KEY + subId, oldNetworkSelectionName);
                        editor.putString(Phone.NETWORK_SELECTION_SHORT_KEY + subId, oldNetworkSelectionShort);
                        editor.remove(Phone.NETWORK_SELECTION_KEY);
                        editor.remove(Phone.NETWORK_SELECTION_NAME_KEY);
                        editor.remove(Phone.NETWORK_SELECTION_SHORT_KEY);
                        editor.commit();
                    }
                    this.this$0.updateSpnDisplay();
                }
                this.this$0.mPhone.updateVoiceMail();
            }
            if (this.this$0.mSubscriptionController.isReady()) {
                int phoneId = this.this$0.mPhone.getPhoneId();
                this.this$0.log("phoneId= " + phoneId + " ,mSpnUpdatePending= " + this.this$0.mSpnUpdatePending);
                if (this.this$0.mSpnUpdatePending) {
                    this.this$0.mSubscriptionController.setPlmnSpn(phoneId, this.this$0.mCurShowPlmn, this.this$0.mCurPlmn, this.this$0.mCurShowSpn, this.this$0.mCurSpn);
                    this.this$0.mSpnUpdatePending = false;
                }
            }
        }
    }

    public class timerTask extends TimerTask {
        final /* synthetic */ ServiceStateTracker this$0;

        /* renamed from: com.android.internal.telephony.ServiceStateTracker$timerTask$1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ timerTask this$1;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.ServiceStateTracker.timerTask.1.<init>(com.android.internal.telephony.ServiceStateTracker$timerTask):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            AnonymousClass1(com.android.internal.telephony.ServiceStateTracker.timerTask r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.ServiceStateTracker.timerTask.1.<init>(com.android.internal.telephony.ServiceStateTracker$timerTask):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.ServiceStateTracker.timerTask.1.<init>(com.android.internal.telephony.ServiceStateTracker$timerTask):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.ServiceStateTracker.timerTask.1.run():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public void run() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.ServiceStateTracker.timerTask.1.run():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.ServiceStateTracker.timerTask.1.run():void");
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.ServiceStateTracker.timerTask.<init>(com.android.internal.telephony.ServiceStateTracker):void, dex: 
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
        public timerTask(com.android.internal.telephony.ServiceStateTracker r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.ServiceStateTracker.timerTask.<init>(com.android.internal.telephony.ServiceStateTracker):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.ServiceStateTracker.timerTask.<init>(com.android.internal.telephony.ServiceStateTracker):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.ServiceStateTracker.timerTask.run():void, dex: 
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
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.ServiceStateTracker.timerTask.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.ServiceStateTracker.timerTask.run():void");
        }
    }

    /* renamed from: -getcom-android-internal-telephony-CommandsInterface$RadioStateSwitchesValues */
    private static /* synthetic */ int[] m42x804b995f() {
        if (f16xba025bb != null) {
            return f16xba025bb;
        }
        int[] iArr = new int[RadioState.values().length];
        try {
            iArr[RadioState.RADIO_OFF.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[RadioState.RADIO_ON.ordinal()] = 3;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[RadioState.RADIO_UNAVAILABLE.ordinal()] = 2;
        } catch (NoSuchFieldError e3) {
        }
        f16xba025bb = iArr;
        return iArr;
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.ServiceStateTracker.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.ServiceStateTracker.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.ServiceStateTracker.<clinit>():void");
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public ServiceStateTracker(com.android.internal.telephony.GsmCdmaPhone r11, com.android.internal.telephony.CommandsInterface r12) {
        /*
        r10 = this;
        r10.<init>();
        r6 = 0;
        r10.mUiccController = r6;
        r6 = 0;
        r10.mUiccApplcation = r6;
        r6 = 0;
        r10.mIccRecords = r6;
        r6 = 0;
        r10.mLastCellInfoList = r6;
        r6 = 2147483647; // 0x7fffffff float:NaN double:1.060997895E-314;
        r10.mCellInfoRate = r6;
        r6 = 0;
        r10.mDontPollSignalStrength = r6;
        r6 = new android.os.RegistrantList;
        r6.<init>();
        r10.mVoiceRoamingOnRegistrants = r6;
        r6 = new android.os.RegistrantList;
        r6.<init>();
        r10.mVoiceRoamingOffRegistrants = r6;
        r6 = new android.os.RegistrantList;
        r6.<init>();
        r10.mDataRoamingOnRegistrants = r6;
        r6 = new android.os.RegistrantList;
        r6.<init>();
        r10.mDataRoamingOffRegistrants = r6;
        r6 = new android.os.RegistrantList;
        r6.<init>();
        r10.mDataRoamingTypeChangedRegistrants = r6;
        r6 = new android.os.RegistrantList;
        r6.<init>();
        r10.mAttachedRegistrants = r6;
        r6 = new android.os.RegistrantList;
        r6.<init>();
        r10.mDetachedRegistrants = r6;
        r6 = new android.os.RegistrantList;
        r6.<init>();
        r10.mDataRegStateOrRatChangedRegistrants = r6;
        r6 = new android.os.RegistrantList;
        r6.<init>();
        r10.mNetworkAttachedRegistrants = r6;
        r6 = new android.os.RegistrantList;
        r6.<init>();
        r10.mPsRestrictEnabledRegistrants = r6;
        r6 = new android.os.RegistrantList;
        r6.<init>();
        r10.mPsRestrictDisabledRegistrants = r6;
        r6 = new android.os.RegistrantList;
        r6.<init>();
        r10.mSignalStrengthChangedRegistrants = r6;
        r6 = 0;
        r10.mPendingRadioPowerOffAfterDataOff = r6;
        r6 = 0;
        r10.mPendingRadioPowerOffAfterDataOffTag = r6;
        r6 = 0;
        r10.mPendingPsRestrictDisabledNotify = r6;
        r6 = 0;
        r10.mEriTriggeredPollState = r6;
        r6 = 0;
        r10.mResetIntentSlot1 = r6;
        r6 = 0;
        r10.mResetIntentSlot2 = r6;
        r6 = 1;
        r10.mOppoNeedSetAlarm = r6;
        r6 = 1;
        r10.mOppoNeedSetRadio = r6;
        r6 = 0;
        r10.mCallingPhoneId = r6;
        r6 = 0;
        r10.mLastUpdateOOSTime = r6;
        r6 = 0;
        r10.mCurrentUpdateOOSTime = r6;
        r6 = -1;
        r10.oosFlag = r6;
        r6 = 0;
        r10.mImsRegistrationOnOff = r6;
        r6 = 0;
        r10.mAlarmSwitch = r6;
        r6 = 0;
        r10.mRadioDisabledByCarrier = r6;
        r6 = 0;
        r10.mRadioOffIntent = r6;
        r6 = 1;
        r10.mPowerOffDelayNeed = r6;
        r6 = 0;
        r10.mDeviceShuttingDown = r6;
        r6 = 0;
        r10.mSpnUpdatePending = r6;
        r6 = 0;
        r10.mCurSpn = r6;
        r6 = 0;
        r10.mCurDataSpn = r6;
        r6 = 0;
        r10.mCurPlmn = r6;
        r6 = 0;
        r10.mCurShowPlmn = r6;
        r6 = 0;
        r10.mCurShowSpn = r6;
        r6 = -1;
        r10.mSubId = r6;
        r6 = 0;
        r10.mImsRegistered = r6;
        r6 = new com.android.internal.telephony.ServiceStateTracker$SstSubscriptionsChangedListener;
        r7 = 0;
        r6.<init>(r10, r7);
        r10.mOnSubscriptionsChangedListener = r6;
        r6 = new android.util.LocalLog;
        r7 = 10;
        r6.<init>(r7);
        r10.mRoamingLog = r6;
        r6 = new android.util.LocalLog;
        r7 = 10;
        r6.<init>(r7);
        r10.mAttachLog = r6;
        r6 = new android.util.LocalLog;
        r7 = 10;
        r6.<init>(r7);
        r10.mPhoneTypeLog = r6;
        r6 = new android.util.LocalLog;
        r7 = 20;
        r6.<init>(r7);
        r10.mRatLog = r6;
        r6 = 0;
        r10.mNeedFixZoneAfterNitz = r6;
        r6 = 0;
        r10.mGotCountryCode = r6;
        r6 = new com.android.internal.telephony.ServiceStateTracker$1;
        r7 = new android.os.Handler;
        r7.<init>();
        r6.<init>(r7);
        r10.mAutoTimeObserver = r6;
        r6 = new com.android.internal.telephony.ServiceStateTracker$2;
        r7 = new android.os.Handler;
        r7.<init>();
        r6.<init>(r7);
        r10.mAutoTimeZoneObserver = r6;
        r6 = 1;
        r10.mMaxDataCalls = r6;
        r6 = 1;
        r10.mNewMaxDataCalls = r6;
        r6 = -1;
        r10.mReasonDataDenied = r6;
        r6 = -1;
        r10.mNewReasonDataDenied = r6;
        r6 = 0;
        r10.mGsmRoaming = r6;
        r6 = 0;
        r10.mDataRoaming = r6;
        r6 = 0;
        r10.mEmergencyOnly = r6;
        r6 = 0;
        r10.mNitzUpdatedTime = r6;
        r6 = 1;
        r10.gprsState = r6;
        r6 = 1;
        r10.newGPRSState = r6;
        r6 = 0;
        r10.mHhbName = r6;
        r6 = 0;
        r10.mCsgId = r6;
        r6 = 0;
        r10.mFemtocellDomain = r6;
        r6 = 0;
        r10.mIsFemtocell = r6;
        r6 = 0;
        r10.dontUpdateNetworkStateFlag = r6;
        r6 = 0;
        r10.hasPendingPollState = r6;
        r6 = 1;
        r10.mFirstRadioChange = r6;
        r6 = 0;
        r10.explict_update_spn = r6;
        r6 = 0;
        r10.mLastRegisteredPLMN = r6;
        r6 = 0;
        r10.mLastPSRegisteredPLMN = r6;
        r6 = 0;
        r10.mEverIVSR = r6;
        r6 = 0;
        r10.isCsInvalidCard = r6;
        r6 = 0;
        r10.mLocatedPlmn = r6;
        r6 = 1;
        r10.mPsRegState = r6;
        r6 = 0;
        r10.mPsRegStateRaw = r6;
        r6 = "";
        r10.mSimType = r6;
        r6 = 14;
        r6 = new java.lang.String[r6][];
        r7 = 2;
        r7 = new java.lang.String[r7];
        r8 = "au";
        r9 = 0;
        r7[r9] = r8;
        r8 = "Australia/Sydney";
        r9 = 1;
        r7[r9] = r8;
        r8 = 0;
        r6[r8] = r7;
        r7 = 2;
        r7 = new java.lang.String[r7];
        r8 = "br";
        r9 = 0;
        r7[r9] = r8;
        r8 = "America/Sao_Paulo";
        r9 = 1;
        r7[r9] = r8;
        r8 = 1;
        r6[r8] = r7;
        r7 = 2;
        r7 = new java.lang.String[r7];
        r8 = "ca";
        r9 = 0;
        r7[r9] = r8;
        r8 = "America/Toronto";
        r9 = 1;
        r7[r9] = r8;
        r8 = 2;
        r6[r8] = r7;
        r7 = 2;
        r7 = new java.lang.String[r7];
        r8 = "cl";
        r9 = 0;
        r7[r9] = r8;
        r8 = "America/Santiago";
        r9 = 1;
        r7[r9] = r8;
        r8 = 3;
        r6[r8] = r7;
        r7 = 2;
        r7 = new java.lang.String[r7];
        r8 = "es";
        r9 = 0;
        r7[r9] = r8;
        r8 = "Europe/Madrid";
        r9 = 1;
        r7[r9] = r8;
        r8 = 4;
        r6[r8] = r7;
        r7 = 2;
        r7 = new java.lang.String[r7];
        r8 = "fm";
        r9 = 0;
        r7[r9] = r8;
        r8 = "Pacific/Ponape";
        r9 = 1;
        r7[r9] = r8;
        r8 = 5;
        r6[r8] = r7;
        r7 = 2;
        r7 = new java.lang.String[r7];
        r8 = "gl";
        r9 = 0;
        r7[r9] = r8;
        r8 = "America/Godthab";
        r9 = 1;
        r7[r9] = r8;
        r8 = 6;
        r6[r8] = r7;
        r7 = 2;
        r7 = new java.lang.String[r7];
        r8 = "kz";
        r9 = 0;
        r7[r9] = r8;
        r8 = "Asia/Almaty";
        r9 = 1;
        r7[r9] = r8;
        r8 = 7;
        r6[r8] = r7;
        r7 = 2;
        r7 = new java.lang.String[r7];
        r8 = "mn";
        r9 = 0;
        r7[r9] = r8;
        r8 = "Asia/Ulaanbaatar";
        r9 = 1;
        r7[r9] = r8;
        r8 = 8;
        r6[r8] = r7;
        r7 = 2;
        r7 = new java.lang.String[r7];
        r8 = "mx";
        r9 = 0;
        r7[r9] = r8;
        r8 = "America/Mexico_City";
        r9 = 1;
        r7[r9] = r8;
        r8 = 9;
        r6[r8] = r7;
        r7 = 2;
        r7 = new java.lang.String[r7];
        r8 = "pf";
        r9 = 0;
        r7[r9] = r8;
        r8 = "Pacific/Tahiti";
        r9 = 1;
        r7[r9] = r8;
        r8 = 10;
        r6[r8] = r7;
        r7 = 2;
        r7 = new java.lang.String[r7];
        r8 = "pt";
        r9 = 0;
        r7[r9] = r8;
        r8 = "Europe/Lisbon";
        r9 = 1;
        r7[r9] = r8;
        r8 = 11;
        r6[r8] = r7;
        r7 = 2;
        r7 = new java.lang.String[r7];
        r8 = "us";
        r9 = 0;
        r7[r9] = r8;
        r8 = "America/New_York";
        r9 = 1;
        r7[r9] = r8;
        r8 = 12;
        r6[r8] = r7;
        r7 = 2;
        r7 = new java.lang.String[r7];
        r8 = "ec";
        r9 = 0;
        r7[r9] = r8;
        r8 = "America/Guayaquil";
        r9 = 1;
        r7[r9] = r8;
        r8 = 13;
        r6[r8] = r7;
        r10.mTimeZoneIdOfCapitalCity = r6;
        r6 = 3;
        r6 = new java.lang.String[r6][];
        r7 = 2;
        r7 = new java.lang.String[r7];
        r8 = "460";
        r9 = 0;
        r7[r9] = r8;
        r8 = "Asia/Shanghai";
        r9 = 1;
        r7[r9] = r8;
        r8 = 0;
        r6[r8] = r7;
        r7 = 2;
        r7 = new java.lang.String[r7];
        r8 = "404";
        r9 = 0;
        r7[r9] = r8;
        r8 = "Asia/Calcutta";
        r9 = 1;
        r7[r9] = r8;
        r8 = 1;
        r6[r8] = r7;
        r7 = 2;
        r7 = new java.lang.String[r7];
        r8 = "454";
        r9 = 0;
        r7[r9] = r8;
        r8 = "Asia/Hong_Kong";
        r9 = 1;
        r7[r9] = r8;
        r8 = 2;
        r6[r8] = r7;
        r10.mTimeZoneIdByMcc = r6;
        r6 = 0;
        r10.mIsImeiLock = r6;
        r6 = 0;
        r10.mIsForceSendScreenOnForUpdateNwInfo = r6;
        r6 = 0;
        r10.bHasDetachedDuringPolling = r6;
        r6 = 0;
        r10.mNeedNotify = r6;
        r6 = 0;
        r10.voiceUrcWith4G = r6;
        r6 = new com.android.internal.telephony.ServiceStateTracker$3;
        r6.<init>();
        r10.mIntentReceiver = r6;
        r6 = new com.android.internal.telephony.ServiceStateTracker$4;
        r7 = new android.os.Handler;
        r7.<init>();
        r6.<init>(r10, r7);
        r10.mDataConnectionSettingObserver = r6;
        r6 = new com.android.internal.telephony.ServiceStateTracker$5;
        r7 = new android.os.Handler;
        r7.<init>();
        r6.<init>(r10, r7);
        r10.mMsicFeatureConfigObserver = r6;
        r6 = 0;
        r10.mCurrentOtaspMode = r6;
        r6 = "ro.nitz_update_spacing";
        r7 = 600000; // 0x927c0 float:8.40779E-40 double:2.964394E-318;
        r6 = android.os.SystemProperties.getInt(r6, r7);
        r10.mNitzUpdateSpacing = r6;
        r6 = "ro.nitz_update_diff";
        r7 = 2000; // 0x7d0 float:2.803E-42 double:9.88E-321;
        r6 = android.os.SystemProperties.getInt(r6, r7);
        r10.mNitzUpdateDiff = r6;
        r6 = -1;
        r10.mRegistrationState = r6;
        r6 = new android.os.RegistrantList;
        r6.<init>();
        r10.mCdmaForSubscriptionInfoReadyRegistrants = r6;
        r6 = 0;
        r10.mHomeSystemId = r6;
        r6 = 0;
        r10.mHomeNetworkId = r6;
        r6 = 0;
        r10.mIsMinInfoReady = r6;
        r6 = 0;
        r10.mIsEriTextLoaded = r6;
        r6 = 0;
        r10.mIsSubscriptionFromRuim = r6;
        r6 = 0;
        r10.mHbpcdUtils = r6;
        r6 = 0;
        r10.mCurrentCarrier = r6;
        r6 = 1;
        r10.mNetworkExsit = r6;
        r6 = com.mediatek.internal.telephony.cdma.pluscode.PlusCodeProcessor.getPlusCodeUtils();
        r10.mPlusCodeUtils = r6;
        r6 = 0;
        r10.mLastSignalStrength = r6;
        r6 = -1;
        r10.psLac = r6;
        r6 = -1;
        r10.psCid = r6;
        r6 = 0;
        r10.wfcVoiceSpnFormat = r6;
        r6 = 0;
        r10.wfcDataSpnFormat = r6;
        r6 = "";
        r10.iso = r6;
        r6 = "";
        r10.mcc = r6;
        r6 = 1;
        r10.mIsScreenOn = r6;
        r6 = 0;
        r10.mTimeCount = r6;
        r6 = 0;
        r10.mIsPendingNotify_0 = r6;
        r6 = 0;
        r10.mIsPendingNotify_1 = r6;
        r6 = -1;
        r10.mOEMLastLevel_0 = r6;
        r6 = -1;
        r10.mOEMLastLevel_1 = r6;
        r6 = -1;
        r10.mOEMCurLevel_0 = r6;
        r6 = -1;
        r10.mOEMCurLevel_1 = r6;
        r6 = 0;
        r10.mNewPlmn = r6;
        r6 = "";
        r10.mOemSpn = r6;
        r6 = 1;
        r10.bCheckNoServiceAgain = r6;
        r6 = new android.telephony.ServiceState;
        r6.<init>();
        r10.mLastCombinedSS = r6;
        r6 = -1;
        r10.mRoaming = r6;
        r6 = 0;
        r10.lastTime = r6;
        r6 = -1;
        r10.mphoneid = r6;
        r6 = 0;
        r10.isRemoveCard = r6;
        r6 = -1;
        r10.OosStartTime = r6;
        r6 = 0;
        r10.mOosDelayState = r6;
        r6 = 0;
        r10.mOppoNeedNotify = r6;
        r6 = 0;
        r10.mNeedSetDds1 = r6;
        r6 = 0;
        r10.mNeedSetDds2 = r6;
        r6 = 0;
        r10.mScreenOffTime = r6;
        r6 = 0;
        r10.mScreenOnTime = r6;
        r6 = 5;
        r10.SAMPLE_TIME = r6;
        r10.mPhone = r11;
        r10.mCi = r12;
        r6 = new com.android.internal.telephony.RatRatcheter;
        r7 = r10.mPhone;
        r6.<init>(r7);
        r10.mRatRatcheter = r6;
        r6 = r10.mPhone;
        r6 = r6.getContext();
        r6 = r6.getResources();
        r7 = 17956954; // 0x112005a float:2.6816217E-38 double:8.871914E-317;
        r6 = r6.getBoolean(r7);
        r10.mVoiceCapable = r6;
        r6 = r10.mPhone;
        r6 = r6.getContext();
        r6 = r6.getResources();
        r7 = 17956957; // 0x112005d float:2.6816226E-38 double:8.8719156E-317;
        r6 = r6.getBoolean(r7);
        r10.mSmsCapable = r6;
        r6 = com.android.internal.telephony.uicc.UiccController.getInstance();
        r10.mUiccController = r6;
        r6 = r10.mUiccController;
        r7 = 42;
        r8 = 0;
        r6.registerForIccChanged(r10, r7, r8);
        r6 = r10.mCi;
        r7 = 12;
        r8 = 0;
        r6.setOnSignalStrengthUpdate(r10, r7, r8);
        r6 = r10.mCi;
        r7 = 44;
        r8 = 0;
        r6.registerForCellInfoList(r10, r7, r8);
        r6 = r10.mCi;
        r7 = 51;
        r8 = 0;
        r6.registerForGetAvailableNetworksDone(r10, r7, r8);
        r6 = r10.mCi;
        r7 = 102; // 0x66 float:1.43E-43 double:5.04E-322;
        r8 = 0;
        r6.registerForPsNetworkStateChanged(r10, r7, r8);
        r6 = com.android.internal.telephony.SubscriptionController.getInstance();
        r10.mSubscriptionController = r6;
        r6 = r11.getContext();
        r6 = android.telephony.SubscriptionManager.from(r6);
        r10.mSubscriptionManager = r6;
        r6 = r10.mSubscriptionManager;
        r7 = r10.mOnSubscriptionsChangedListener;
        r6.addOnSubscriptionsChangedListener(r7);
        r6 = r10.mCi;
        r7 = 46;
        r8 = 0;
        r6.registerForImsNetworkStateChanged(r10, r7, r8);
        r6 = r11.getContext();
        r7 = "power";
        r5 = r6.getSystemService(r7);
        r5 = (android.os.PowerManager) r5;
        r6 = "ServiceStateTracker";
        r7 = 1;
        r6 = r5.newWakeLock(r7, r6);
        r10.mWakeLock = r6;
        r6 = "ro.mtk_bsp_package";
        r6 = android.os.SystemProperties.get(r6);
        r7 = "1";
        r6 = r6.equals(r7);
        if (r6 != 0) goto L_0x042b;
    L_0x0419:
        r6 = com.mediatek.common.telephony.IServiceStateExt.class;	 Catch:{ RuntimeException -> 0x04f5 }
        r6 = r6.getName();	 Catch:{ RuntimeException -> 0x04f5 }
        r7 = r11.getContext();	 Catch:{ RuntimeException -> 0x04f5 }
        r6 = com.mediatek.common.MPlugin.createInstance(r6, r7);	 Catch:{ RuntimeException -> 0x04f5 }
        r6 = (com.mediatek.common.telephony.IServiceStateExt) r6;	 Catch:{ RuntimeException -> 0x04f5 }
        r10.mServiceStateExt = r6;	 Catch:{ RuntimeException -> 0x04f5 }
    L_0x042b:
        r6 = r10.mCi;
        r7 = 1;
        r8 = 0;
        r6.registerForRadioStateChanged(r10, r7, r8);
        r6 = r10.mCi;
        r7 = 2;
        r8 = 0;
        r6.registerForVoiceNetworkStateChanged(r10, r7, r8);
        r6 = r10.mCi;
        r7 = 11;
        r8 = 0;
        r6.setOnNITZTime(r10, r7, r8);
        r6 = r11.getContext();
        r6 = r6.getContentResolver();
        r10.mCr = r6;
        r6 = r10.mCr;
        r7 = "airplane_mode_on";
        r8 = 0;
        r0 = android.provider.Settings.Global.getInt(r6, r7, r8);
        r6 = r10.mCr;
        r7 = "enable_cellular_on_boot";
        r8 = 1;
        r3 = android.provider.Settings.Global.getInt(r6, r7, r8);
        if (r3 <= 0) goto L_0x04fb;
    L_0x0461:
        if (r0 > 0) goto L_0x04fb;
    L_0x0463:
        r6 = 1;
    L_0x0464:
        r10.mDesiredPowerState = r6;
        r6 = r10.mCr;
        r7 = "auto_time";
        r7 = android.provider.Settings.Global.getUriFor(r7);
        r8 = r10.mAutoTimeObserver;
        r9 = 1;
        r6.registerContentObserver(r7, r9, r8);
        r6 = r10.mCr;
        r7 = "auto_time_zone";
        r7 = android.provider.Settings.Global.getUriFor(r7);
        r8 = r10.mAutoTimeZoneObserver;
        r9 = 1;
        r6.registerContentObserver(r7, r9, r8);
        r10.setSignalStrengthDefaultValues();
        r6 = r10.mPhone;
        r1 = r6.getContext();
        r4 = new android.content.IntentFilter;
        r4.<init>();
        r6 = "android.intent.action.LOCALE_CHANGED";
        r4.addAction(r6);
        r6 = r10.mIntentReceiver;
        r1.registerReceiver(r6, r4);
        r4 = new android.content.IntentFilter;
        r4.<init>();
        r6 = "android.intent.action.ACTION_RADIO_OFF";
        r4.addAction(r6);
        r6 = "android.intent.action.SIM_STATE_CHANGED";
        r4.addAction(r6);
        r6 = "android.intent.action.ACTION_SUBINFO_RECORD_UPDATED";
        r4.addAction(r6);
        r6 = "android.intent.action.RADIO_TECHNOLOGY";
        r4.addAction(r6);
        r6 = com.android.internal.telephony.OemConstant.EXP_VERSION;
        if (r6 == 0) goto L_0x04de;
    L_0x04be:
        r6 = com.android.internal.telephony.regionlock.RegionLockConstant.IS_REGION_LOCK;
        if (r6 == 0) goto L_0x04de;
    L_0x04c2:
        r6 = "oppo.action.UNLOCK_NETWORK_SIM1";
        r4.addAction(r6);
        r6 = "oppo.action.UNLOCK_NETWORK_SIM2";
        r4.addAction(r6);
        r6 = "oppo.intent.action.CALLING_DISCONNECTED";
        r4.addAction(r6);
        r6 = r11.getContext();
        r6 = com.android.internal.telephony.regionlock.RegionLockPlmnListService.getInstance(r6);
        r10.mRegionLockPlmnList = r6;
    L_0x04de:
        r6 = r10.mIntentReceiver;
        r1.registerReceiver(r6, r4);
        r6 = r10.mPhone;
        r7 = 0;
        r6.notifyOtaspChanged(r7);
        r10.updatePhoneType();
        r6 = r10.mCi;
        r7 = 150; // 0x96 float:2.1E-43 double:7.4E-322;
        r8 = 0;
        r6.registerForOemScreenChanged(r10, r7, r8);
        return;
    L_0x04f5:
        r2 = move-exception;
        r2.printStackTrace();
        goto L_0x042b;
    L_0x04fb:
        r6 = 0;
        goto L_0x0464;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.ServiceStateTracker.<init>(com.android.internal.telephony.GsmCdmaPhone, com.android.internal.telephony.CommandsInterface):void");
    }

    public void updatePhoneType() {
        boolean z = true;
        this.mSS = new ServiceState();
        this.mNewSS = new ServiceState();
        this.mLastCellInfoListTime = 0;
        this.mLastCellInfoList = null;
        this.mSignalStrength = new SignalStrength();
        this.mRestrictedState = new RestrictedState();
        this.mStartedGprsRegCheck = false;
        this.mReportedGprsNoReg = false;
        this.mMdn = null;
        this.mMin = null;
        this.mPrlVersion = null;
        this.mIsMinInfoReady = false;
        this.mNitzUpdatedTime = false;
        cancelPollState();
        if (this.mPhone.isPhoneTypeGsm()) {
            if (this.mCdmaSSM != null) {
                this.mCdmaSSM.dispose(this);
            }
            this.mCi.unregisterForCdmaPrlChanged(this);
            this.mPhone.unregisterForEriFileLoaded(this);
            this.mCi.unregisterForCdmaOtaProvision(this);
            this.mPhone.unregisterForSimRecordsLoaded(this);
            this.mCellLoc = new GsmCellLocation();
            this.mNewCellLoc = new GsmCellLocation();
            this.mCi.registerForAvailable(this, 13, null);
            this.mCi.setOnRestrictedStateChanged(this, 23, null);
            this.mCi.setInvalidSimInfo(this, 101, null);
            this.mCi.registerForModulation(this, 117, null);
            this.mCi.registerForCsNetworkStateChanged(this, 119, null);
            try {
                if (this.mServiceStateExt.isImeiLocked()) {
                    this.mCi.registerForIMEILock(this, 103, null);
                }
            } catch (RuntimeException e) {
                loge("No isImeiLocked");
            }
            this.mCi.registerForIccRefresh(this, 106, null);
            if (SystemProperties.get("ro.mtk_femto_cell_support").equals("1")) {
                this.mCi.registerForFemtoCellInfo(this, 107, null);
            }
            this.mCi.registerForNetworkEvent(this, 118, null);
            this.mCr.registerContentObserver(Global.getUriFor("telephony_misc_feature_config"), true, this.mMsicFeatureConfigObserver);
            this.mCr.registerContentObserver(System.getUriFor("multi_sim_data_call"), true, this.mDataConnectionSettingObserver);
            this.mCr.registerContentObserver(System.getUriFor("mobile_data"), true, this.mDataConnectionSettingObserver);
            if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
                try {
                    if (this.mServiceStateExt.needEMMRRS()) {
                        if (isCurrentPhoneDataConnectionOn()) {
                            getEINFO(105);
                        } else {
                            getEINFO(104);
                        }
                    }
                } catch (RuntimeException e2) {
                    e2.printStackTrace();
                }
            }
            for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++) {
                setReceivedNitz(i, false);
            }
        } else {
            this.mCi.unregisterForAvailable(this);
            this.mCi.unSetOnRestrictedStateChanged(this);
            this.mPsRestrictDisabledRegistrants.notifyRegistrants();
            this.mCi.unregisterForCdmaPrlChanged(this);
            this.mPhone.unregisterForEriFileLoaded(this);
            this.mCi.unregisterForCdmaOtaProvision(this);
            this.mPhone.unregisterForSimRecordsLoaded(this);
            this.mCi.unSetInvalidSimInfo(this);
            this.mCi.unregisterForModulation(this);
            this.mCi.unregisterForCsNetworkStateChanged(this);
            try {
                if (this.mServiceStateExt.isImeiLocked()) {
                    this.mCi.unregisterForIMEILock(this);
                }
            } catch (RuntimeException e3) {
                loge("No isImeiLocked");
            }
            if (SystemProperties.get("ro.mtk_femto_cell_support").equals("1")) {
                this.mCi.unregisterForFemtoCellInfo(this);
            }
            this.mCi.unregisterForIccRefresh(this);
            if (SystemProperties.get("ro.mtk_femto_cell_support").equals("1")) {
                this.mCi.unregisterForFemtoCellInfo(this);
            }
            if (SystemProperties.get("persist.mtk_ims_support").equals("1")) {
                this.mCi.unregisterForImsDisable(this);
                this.mCi.unregisterForImsRegistrationInfo(this);
            }
            this.mCi.unregisterForNetworkEvent(this);
            this.mCr.unregisterContentObserver(this.mMsicFeatureConfigObserver);
            this.mCr.unregisterContentObserver(this.mDataConnectionSettingObserver);
            if (this.mPhone.isPhoneTypeCdmaLte()) {
                this.mPhone.registerForSimRecordsLoaded(this, 16, null);
            }
            this.mCellLoc = new CdmaCellLocation();
            this.mNewCellLoc = new CdmaCellLocation();
            this.mCdmaSSM = CdmaSubscriptionSourceManager.getInstance(this.mPhone.getContext(), this.mCi, this, 39, null);
            if (this.mCdmaSSM.getCdmaSubscriptionSource() != 0) {
                z = false;
            }
            this.mIsSubscriptionFromRuim = z;
            this.mCi.registerForCdmaPrlChanged(this, 40, null);
            this.mPhone.registerForEriFileLoaded(this, 36, null);
            this.mCi.registerForCdmaOtaProvision(this, 37, null);
            this.mHbpcdUtils = new HbpcdUtils(this.mPhone.getContext());
            updateOtaspState();
        }
        onUpdateIccAvailability();
        this.mPhone.setSystemProperty("gsm.network.type", ServiceState.rilRadioTechnologyToString(0));
        this.mCi.getSignalStrength(obtainMessage(3));
        sendMessage(obtainMessage(50));
        logPhoneTypeChange();
        this.mVoiceRoamingOffRegistrants.notifyRegistrants();
        this.mDataRoamingOffRegistrants.notifyRegistrants();
        this.mDetachedRegistrants.notifyRegistrants();
        notifyDataRegStateRilRadioTechnologyChanged();
    }

    public void requestShutdown() {
        if (!this.mDeviceShuttingDown) {
            this.mDeviceShuttingDown = true;
            this.mDesiredPowerState = false;
            RadioManager.getInstance().setModemPower(false, 1 << getPhone().getPhoneId());
        }
    }

    public void dispose() {
        this.mCi.unregisterOemScreenChanged(this);
        this.mCi.unSetOnSignalStrengthUpdate(this);
        this.mUiccController.unregisterForIccChanged(this);
        this.mCi.unregisterForCellInfoList(this);
        this.mSubscriptionManager.removeOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        this.mCi.unregisterForImsNetworkStateChanged(this);
        this.mCi.unregisterForGetAvailableNetworksDone(this);
        this.mCi.unregisterForPsNetworkStateChanged(this);
        if (this.mPhone.isPhoneTypeGsm()) {
            this.mCi.unregisterForAvailable(this);
            this.mCi.unSetOnRestrictedStateChanged(this);
            this.mCi.unSetInvalidSimInfo(this);
            this.mCi.unregisterForModulation(this);
            this.mCi.unregisterForCsNetworkStateChanged(this);
            try {
                if (this.mServiceStateExt.isImeiLocked()) {
                    this.mCi.unregisterForIMEILock(this);
                }
            } catch (RuntimeException e) {
                loge("No isImeiLocked");
            }
            if (SystemProperties.get("ro.mtk_femto_cell_support").equals("1")) {
                this.mCi.unregisterForFemtoCellInfo(this);
            }
            this.mCi.unregisterForIccRefresh(this);
            if (SystemProperties.get("ro.mtk_femto_cell_support").equals("1")) {
                this.mCi.unregisterForFemtoCellInfo(this);
            }
            this.mCi.unregisterForNetworkEvent(this);
            this.mCr.unregisterContentObserver(this.mMsicFeatureConfigObserver);
            this.mCr.unregisterContentObserver(this.mDataConnectionSettingObserver);
        }
        if (this.mPhone.isPhoneTypeCdma() || this.mPhone.isPhoneTypeCdmaLte()) {
            this.mPhone.getContext().unregisterReceiver(this.mIntentReceiver);
        }
    }

    public boolean getDesiredPowerState() {
        return this.mDesiredPowerState;
    }

    public boolean getPowerStateFromCarrier() {
        return !this.mRadioDisabledByCarrier;
    }

    protected boolean notifySignalStrength() {
        if (this.mSignalStrength.equals(this.mLastSignalStrength)) {
            return false;
        }
        try {
            if (DBG) {
                log("notifySignalStrength: mSignalStrength.getLevel=" + this.mSignalStrength.getLevel());
            }
            this.mPhone.notifySignalStrength();
            this.mLastSignalStrength = new SignalStrength(this.mSignalStrength);
            return true;
        } catch (NullPointerException ex) {
            loge("updateSignalStrength() Phone already destroyed: " + ex + "SignalStrength not notified");
            return false;
        }
    }

    protected void notifyDataRegStateRilRadioTechnologyChanged() {
        int rat = this.mSS.getRilDataRadioTechnology();
        int drs = this.mSS.getDataRegState();
        if (DBG) {
            log("notifyDataRegStateRilRadioTechnologyChanged: drs=" + drs + " rat=" + rat);
        }
        this.mPhone.setSystemProperty("gsm.network.type", ServiceState.rilRadioTechnologyToString(rat));
        this.mDataRegStateOrRatChangedRegistrants.notifyResult(new Pair(Integer.valueOf(drs), Integer.valueOf(rat)));
    }

    protected void useDataRegStateForDataOnlyDevices() {
        if (!this.mSmsCapable) {
            if (DBG) {
                log("useDataRegStateForDataOnlyDevice: VoiceRegState=" + this.mNewSS.getVoiceRegState() + " DataRegState=" + this.mNewSS.getDataRegState());
            }
            this.mNewSS.setVoiceRegState(this.mNewSS.getDataRegState());
            this.mNewSS.setRegState(1);
        }
    }

    protected void updatePhoneObject() {
        boolean isRegistered = true;
        if (this.mPhone.getContext().getResources().getBoolean(17957018)) {
            if (!(this.mSS.getVoiceRegState() == 0 || this.mSS.getVoiceRegState() == 2)) {
                isRegistered = false;
            }
            if (isRegistered) {
                this.mPhone.updatePhoneObject(this.mSS.getRilVoiceRadioTechnology());
            } else {
                log("updatePhoneObject: Ignore update");
            }
        }
    }

    public void registerForVoiceRoamingOn(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mVoiceRoamingOnRegistrants.add(r);
        if (this.mSS.getVoiceRoaming()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForVoiceRoamingOn(Handler h) {
        this.mVoiceRoamingOnRegistrants.remove(h);
    }

    public void registerForVoiceRoamingOff(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mVoiceRoamingOffRegistrants.add(r);
        if (!this.mSS.getVoiceRoaming()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForVoiceRoamingOff(Handler h) {
        this.mVoiceRoamingOffRegistrants.remove(h);
    }

    public void registerForDataRoamingOn(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mDataRoamingOnRegistrants.add(r);
        if (this.mSS.getDataRoaming()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForDataRoamingOn(Handler h) {
        this.mDataRoamingOnRegistrants.remove(h);
    }

    public void registerForDataRoamingOff(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mDataRoamingOffRegistrants.add(r);
        if (!this.mSS.getDataRoaming()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForDataRoamingOff(Handler h) {
        this.mDataRoamingOffRegistrants.remove(h);
    }

    public void registerForDataRoamingTypeChange(Handler h, int what, Object obj) {
        this.mDataRoamingTypeChangedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForDataRoamingTypeChange(Handler h) {
        this.mDataRoamingTypeChangedRegistrants.remove(h);
    }

    public void reRegisterNetwork(Message onComplete) {
        this.mCi.getPreferredNetworkType(obtainMessage(19, onComplete));
    }

    public void setRadioPower(boolean power) {
        int mPhoneId = getPhoneId();
        if (DBG) {
            Rlog.d(LOG_TAG, "setRadioPower: mPhoneId :" + mPhoneId);
        }
        if (mPhoneId >= 0 && mPhoneId < TelephonyManager.getDefault().getPhoneCount()) {
            if (DBG) {
                Rlog.d(LOG_TAG, "setRadioPower: isUiccSlotForbid :" + OemConstant.isUiccSlotForbid(mPhoneId));
            }
            if (OemConstant.isUiccSlotForbid(mPhoneId) && power) {
                if (DBG) {
                    Rlog.d(LOG_TAG, "setRadioPower: isUiccSlotForbid return");
                }
                return;
            }
        }
        this.mDesiredPowerState = power;
        setPowerStateToDesired();
    }

    public void setRadioPowerFromCarrier(boolean enable) {
        this.mRadioDisabledByCarrier = !enable;
        RadioManager.getInstance().setRadioPower(this.mDesiredPowerState, this.mPhone.getPhoneId());
    }

    public void enableSingleLocationUpdate() {
        if (!this.mWantSingleLocationUpdate && !this.mWantContinuousLocationUpdates) {
            this.mWantSingleLocationUpdate = true;
            this.mCi.setLocationUpdates(true, obtainMessage(18));
        }
    }

    public void enableLocationUpdates() {
        if (!this.mWantSingleLocationUpdate && !this.mWantContinuousLocationUpdates) {
            this.mWantContinuousLocationUpdates = true;
            this.mCi.setLocationUpdates(true, obtainMessage(18));
        }
    }

    protected void disableSingleLocationUpdate() {
        this.mWantSingleLocationUpdate = false;
        if (!this.mWantSingleLocationUpdate && !this.mWantContinuousLocationUpdates) {
            this.mCi.setLocationUpdates(false, null);
        }
    }

    public void disableLocationUpdates() {
        this.mWantContinuousLocationUpdates = false;
        if (!this.mWantSingleLocationUpdate && !this.mWantContinuousLocationUpdates) {
            this.mCi.setLocationUpdates(false, null);
        }
    }

    public void handleMessage(Message msg) {
        AsyncResult ar;
        String[] data;
        int oldValue;
        int value;
        switch (msg.what) {
            case 1:
            case 50:
                if (!this.mPhone.isPhoneTypeGsm() && this.mCi.getRadioState() == RadioState.RADIO_ON) {
                    handleCdmaSubscriptionSource(this.mCdmaSSM.getCdmaSubscriptionSource());
                    queueNextSignalStrengthPoll();
                }
                if (RadioManager.isMSimModeSupport()) {
                    logd("handle [msg.what]=" + msg.what + ",MTK propiertary Power on flow, setRadioPower:  mDesiredPowerState=" + this.mDesiredPowerState + "  phoneId=" + this.mPhone.getPhoneId());
                    RadioManager.getInstance().setRadioPower(this.mDesiredPowerState, this.mPhone.getPhoneId());
                } else {
                    log("handle [msg.what]=" + msg.what + ",BSP package but use MTK Power on flow");
                    RadioManager.getInstance().setRadioPower(this.mDesiredPowerState, this.mPhone.getPhoneId());
                }
                modemTriggeredPollState();
                break;
            case 2:
                if (this.mPhone.isPhoneTypeGsm()) {
                    onNetworkStateChangeResult((AsyncResult) msg.obj);
                }
                modemTriggeredPollState();
                break;
            case 3:
                if (this.mCi.getRadioState().isOn()) {
                    onSignalStrengthResult((AsyncResult) msg.obj);
                    queueNextSignalStrengthPoll();
                    break;
                }
                return;
            case 4:
            case 5:
            case 6:
                handlePollStateResult(msg.what, (AsyncResult) msg.obj);
                break;
            case 10:
                if (this.mPhone.isPhoneTypeGsm()) {
                    log("handle EVENT_POLL_SIGNAL_STRENGTH GSM " + this.mDontPollSignalStrength);
                    if (this.mDontPollSignalStrength) {
                        return;
                    }
                }
                this.mCi.getSignalStrength(obtainMessage(3));
                break;
            case 11:
                ar = (AsyncResult) msg.obj;
                setTimeFromNITZString(((Object[]) ar.result)[0], ((Long) ((Object[]) ar.result)[1]).longValue());
                break;
            case 12:
                ar = (AsyncResult) msg.obj;
                this.mDontPollSignalStrength = true;
                if (this.mPhone.isPhoneTypeGsm() && ar.exception == null && ar.result != null) {
                    this.mSignalStrengthChangedRegistrants.notifyResult(new SignalStrength((SignalStrength) ar.result));
                }
                onSignalStrengthResult(ar);
                break;
            case 13:
                if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
                    log("not BSP package, notify!");
                    RadioManager.getInstance().notifyRadioAvailable(this.mPhone.getPhoneId());
                    break;
                }
                break;
            case 14:
                if (DBG) {
                    log("EVENT_POLL_STATE_NETWORK_SELECTION_MODE");
                }
                ar = (AsyncResult) msg.obj;
                if (!this.mPhone.isPhoneTypeGsm()) {
                    if (ar.exception == null && ar.result != null) {
                        if (((int[]) ar.result)[0] == 1) {
                            this.mPhone.setNetworkSelectionModeAutomatic(null);
                            break;
                        }
                    }
                    log("Unable to getNetworkSelectionMode");
                    break;
                }
                handlePollStateResult(msg.what, ar);
                break;
                break;
            case 15:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    String[] states = (String[]) ar.result;
                    if (this.mPhone.isPhoneTypeGsm()) {
                        int lac = -1;
                        int cid = -1;
                        if (states.length >= 3) {
                            try {
                                if (states[1] != null && states[1].length() > 0) {
                                    lac = Integer.parseInt(states[1], 16);
                                }
                                if (states[2] != null && states[2].length() > 0) {
                                    cid = Integer.parseInt(states[2], 16);
                                }
                            } catch (NumberFormatException ex) {
                                Rlog.w(LOG_TAG, "error parsing location: " + ex);
                            }
                        }
                        ((GsmCellLocation) this.mCellLoc).setLacAndCid(lac, cid);
                    } else {
                        int baseStationId = -1;
                        int baseStationLatitude = Integer.MAX_VALUE;
                        int baseStationLongitude = Integer.MAX_VALUE;
                        int systemId = -1;
                        int networkId = -1;
                        if (states.length > 9) {
                            try {
                                if (states[4] != null) {
                                    baseStationId = Integer.parseInt(states[4]);
                                }
                                if (states[5] != null) {
                                    baseStationLatitude = Integer.parseInt(states[5]);
                                }
                                if (states[6] != null) {
                                    baseStationLongitude = Integer.parseInt(states[6]);
                                }
                                if (baseStationLatitude == 0 && baseStationLongitude == 0) {
                                    baseStationLatitude = Integer.MAX_VALUE;
                                    baseStationLongitude = Integer.MAX_VALUE;
                                }
                                if (states[8] != null) {
                                    systemId = Integer.parseInt(states[8]);
                                }
                                if (states[9] != null) {
                                    networkId = Integer.parseInt(states[9]);
                                }
                            } catch (NumberFormatException ex2) {
                                loge("error parsing cell location data: " + ex2);
                            }
                        }
                        ((CdmaCellLocation) this.mCellLoc).setCellLocationData(baseStationId, baseStationLatitude, baseStationLongitude, systemId, networkId);
                    }
                    this.mPhone.notifyLocationChanged();
                }
                disableSingleLocationUpdate();
                break;
            case 16:
                log("EVENT_SIM_RECORDS_LOADED: what=" + msg.what);
                updatePhoneObject();
                updateOtaspState();
                if (this.mPhone.isPhoneTypeGsm()) {
                    refreshSpnDisplay();
                    if (this.mNeedNotify) {
                        pollState();
                        break;
                    }
                }
                break;
            case 17:
                this.mOnSubscriptionsChangedListener.mPreviousSubId.set(-1);
                if (this.mPhone.isPhoneTypeGsm()) {
                    boolean skipRestoringSelection = this.mPhone.getContext().getResources().getBoolean(17956961);
                    if (DBG) {
                        log("skipRestoringSelection=" + skipRestoringSelection);
                    }
                    if (!skipRestoringSelection) {
                        this.mPhone.restoreSavedNetworkSelection(null);
                    }
                }
                pollState();
                queueNextSignalStrengthPoll();
                break;
            case 18:
                log("handle EVENT_LOCATION_UPDATES_ENABLED");
                if (((AsyncResult) msg.obj).exception == null) {
                    this.mCi.getVoiceRegistrationState(obtainMessage(15, null));
                    break;
                }
                break;
            case 19:
                log("handle EVENT_GET_PREFERRED_NETWORK_TYPE");
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    this.mPreferredNetworkType = ((int[]) ar.result)[0];
                } else {
                    this.mPreferredNetworkType = 7;
                }
                this.mCi.setPreferredNetworkType(7, obtainMessage(20, ar.userObj));
                break;
            case 20:
                log("handle EVENT_SET_PREFERRED_NETWORK_TYPE");
                this.mCi.setPreferredNetworkType(this.mPreferredNetworkType, obtainMessage(21, ((AsyncResult) msg.obj).userObj));
                break;
            case 21:
                log("handle EVENT_RESET_PREFERRED_NETWORK_TYPE");
                ar = (AsyncResult) msg.obj;
                if (ar.userObj != null) {
                    AsyncResult.forMessage((Message) ar.userObj).exception = ar.exception;
                    ((Message) ar.userObj).sendToTarget();
                    break;
                }
                break;
            case 22:
                log("handle EVENT_CHECK_REPORT_GPRS");
                if (this.mPhone.isPhoneTypeGsm() && this.mSS != null) {
                    if (!isGprsConsistent(this.mSS.getDataRegState(), this.mSS.getVoiceRegState())) {
                        GsmCellLocation loc = (GsmCellLocation) this.mPhone.getCellLocation();
                        String[] strArr = new Object[2];
                        strArr[0] = this.mSS.getOperatorNumeric();
                        strArr[1] = Integer.valueOf(loc != null ? loc.getCid() : -1);
                        EventLog.writeEvent(EventLogTags.DATA_NETWORK_REGISTRATION_FAIL, strArr);
                        this.mReportedGprsNoReg = true;
                    }
                }
                this.mStartedGprsRegCheck = false;
                break;
            case 23:
                if (this.mPhone.isPhoneTypeGsm()) {
                    if (DBG) {
                        log("EVENT_RESTRICTED_STATE_CHANGED");
                    }
                    onRestrictedStateChanged((AsyncResult) msg.obj);
                    break;
                }
                break;
            case 26:
                if (this.mPhone.isPhoneTypeCdma() || this.mPhone.isPhoneTypeCdmaLte()) {
                    this.mIsSubscriptionFromRuim = true;
                }
                if (this.mPhone.isPhoneTypeCdmaLte()) {
                    if (DBG) {
                        log("Receive EVENT_RUIM_READY");
                    }
                    pollState();
                } else {
                    if (DBG) {
                        log("Receive EVENT_RUIM_READY and Send Request getCDMASubscription.");
                    }
                    getSubscriptionInfoAndStartPollingThreads();
                }
                this.mCi.getNetworkSelectionMode(obtainMessage(14));
                break;
            case 27:
                if (!this.mPhone.isPhoneTypeGsm()) {
                    log("EVENT_RUIM_RECORDS_LOADED: what=" + msg.what);
                    updatePhoneObject();
                    if (!this.mPhone.isPhoneTypeCdma()) {
                        RuimRecords ruim = (RuimRecords) this.mIccRecords;
                        if (ruim != null) {
                            if (ruim.isProvisioned()) {
                                this.mMdn = ruim.getMdn();
                                this.mMin = ruim.getMin();
                                parseSidNid(ruim.getSid(), ruim.getNid());
                                this.mPrlVersion = ruim.getPrlVersion();
                                this.mIsMinInfoReady = true;
                            }
                            updateOtaspState();
                            updateSpnDisplay();
                            notifyCdmaSubscriptionInfoReady();
                        }
                        pollState();
                        break;
                    }
                    updateSpnDisplay();
                    break;
                }
                break;
            case 34:
                if (!this.mPhone.isPhoneTypeGsm()) {
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        String[] cdmaSubscription = ar.result;
                        if (cdmaSubscription == null || cdmaSubscription.length < 5) {
                            if (DBG) {
                                log("GET_CDMA_SUBSCRIPTION: error parsing cdmaSubscription params num=" + cdmaSubscription.length);
                                break;
                            }
                        }
                        this.mMdn = cdmaSubscription[0];
                        parseSidNid(cdmaSubscription[1], cdmaSubscription[2]);
                        this.mMin = cdmaSubscription[3];
                        this.mPrlVersion = cdmaSubscription[4];
                        if (DBG) {
                            log("GET_CDMA_SUBSCRIPTION: MDN=" + this.mMdn);
                        }
                        this.mIsMinInfoReady = true;
                        updateOtaspState();
                        notifyCdmaSubscriptionInfoReady();
                        if (this.mIsSubscriptionFromRuim || this.mIccRecords == null) {
                            if (DBG) {
                                log("GET_CDMA_SUBSCRIPTION either mIccRecords is null or NV type device - not setting Imsi in mIccRecords");
                                break;
                            }
                        }
                        if (DBG) {
                            log("GET_CDMA_SUBSCRIPTION set imsi in mIccRecords");
                        }
                        this.mIccRecords.setImsi(getImsi());
                        break;
                    }
                }
                break;
            case 35:
                updatePhoneObject();
                this.mCi.getNetworkSelectionMode(obtainMessage(14));
                getSubscriptionInfoAndStartPollingThreads();
                break;
            case 36:
                if (DBG) {
                    log("ERI file has been loaded, repolling.");
                }
                this.mEriTriggeredPollState = true;
                pollState();
                break;
            case 37:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    int otaStatus = ((int[]) ar.result)[0];
                    if (otaStatus == 8 || otaStatus == 10) {
                        if (DBG) {
                            log("EVENT_OTA_PROVISION_STATUS_CHANGE: Complete, Reload MDN");
                        }
                        this.mCi.getCDMASubscription(obtainMessage(34));
                        break;
                    }
                }
                break;
            case 38:
                synchronized (this) {
                    if (!this.mPendingRadioPowerOffAfterDataOff || msg.arg1 != this.mPendingRadioPowerOffAfterDataOffTag) {
                        log("EVENT_SET_RADIO_OFF is stale arg1=" + msg.arg1 + "!= tag=" + this.mPendingRadioPowerOffAfterDataOffTag);
                        break;
                    }
                    if (DBG) {
                        log("EVENT_SET_RADIO_OFF, turn radio off now.");
                    }
                    hangupAndPowerOff();
                    this.mPendingRadioPowerOffAfterDataOffTag++;
                    this.mPendingRadioPowerOffAfterDataOff = false;
                    break;
                }
                break;
            case 39:
                handleCdmaSubscriptionSource(this.mCdmaSSM.getCdmaSubscriptionSource());
                break;
            case 40:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    this.mPrlVersion = Integer.toString(((int[]) ar.result)[0]);
                    break;
                }
                break;
            case 42:
                onUpdateIccAvailability();
                break;
            case 43:
            case 108:
                ar = msg.obj;
                CellInfoResult result = ar.userObj;
                synchronized (result.lockObj) {
                    if (ar.exception != null) {
                        log("EVENT_GET_CELL_INFO_LIST: error ret null, e=" + ar.exception);
                        result.list = null;
                    } else {
                        result.list = (List) ar.result;
                        if (DBG) {
                            log("EVENT_GET_CELL_INFO_LIST: size=" + result.list.size() + " list=" + result.list);
                        }
                    }
                    this.mLastCellInfoListTime = SystemClock.elapsedRealtime();
                    this.mLastCellInfoList = result.list;
                    if (msg.what == 108) {
                        log("EVENT_GET_CELL_INFO_LIST_BY_RATE notify result");
                        this.mPhone.notifyCellInfo(result.list);
                    }
                    result.lockObj.notify();
                }
            case 44:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    List<CellInfo> list = ar.result;
                    this.mLastCellInfoListTime = SystemClock.elapsedRealtime();
                    this.mLastCellInfoList = list;
                    this.mPhone.notifyCellInfo(list);
                    break;
                }
                log("EVENT_UNSOL_CELL_INFO_LIST: error ignoring, e=" + ar.exception);
                break;
            case 45:
                if (DBG) {
                    log("EVENT_CHANGE_IMS_STATE:");
                }
                setPowerStateToDesired();
                break;
            case 46:
                this.mCi.getImsRegistrationState(obtainMessage(47));
                break;
            case 47:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    this.mImsRegistered = ((int[]) ar.result)[0] == 1;
                    break;
                }
                break;
            case EVENT_IMS_CAPABILITY_CHANGED /*48*/:
                if (DBG) {
                    log("EVENT_IMS_CAPABILITY_CHANGED");
                }
                updateSpnDisplay();
                break;
            case 49:
                ProxyController.getInstance().unregisterForAllDataDisconnected(SubscriptionManager.getDefaultDataSubscriptionId(), this);
                synchronized (this) {
                    if (!this.mPendingRadioPowerOffAfterDataOff) {
                        log("EVENT_ALL_DATA_DISCONNECTED is stale");
                        break;
                    }
                    if (DBG) {
                        log("EVENT_ALL_DATA_DISCONNECTED, turn radio off now.");
                    }
                    hangupAndPowerOff();
                    this.mPendingRadioPowerOffAfterDataOff = false;
                    break;
                }
            case 51:
                log("EVENT_GET_AVAILABLE_NETWORK_DONE, radio is: " + this.mCi.getRadioState() + ", voiceReg = " + this.mSS.getVoiceRegState() + ", dataReg = " + this.mSS.getDataRegState());
                if (this.mCi.getRadioState().isOn() && !(this.mSS.getVoiceRegState() == 0 && this.mSS.getDataRegState() == 0)) {
                    log("EVENT_GET_AVAILABLE_NETWORK_DONE, need query again.");
                    pollState(true);
                    break;
                }
            case 101:
                if (this.mPhone.isPhoneTypeGsm()) {
                    onInvalidSimInfoReceived((AsyncResult) msg.obj);
                    break;
                }
                break;
            case 102:
                if (this.mPhone.isPhoneTypeGsm()) {
                    onPsNetworkStateChangeResult((AsyncResult) msg.obj);
                }
                modemTriggeredPollState();
                break;
            case 103:
                if (this.mPhone.isPhoneTypeGsm()) {
                    log("handle EVENT_IMEI_LOCK GSM");
                    this.mIsImeiLock = true;
                    break;
                }
                break;
            case 104:
                if (this.mPhone.isPhoneTypeGsm()) {
                    log("handle EVENT_DISABLE_EMMRRS_STATUS GSM");
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        data = (String[]) ar.result;
                        log("EVENT_DISABLE_EMMRRS_STATUS, data[0] is : " + data[0]);
                        log("EVENT_DISABLE_EMMRRS_STATUS, einfo value is : " + data[0].substring(8));
                        try {
                            oldValue = Integer.valueOf(data[0].substring(8)).intValue();
                            value = oldValue & 65407;
                            log("EVENT_DISABLE_EMMRRS_STATUS, einfo value change is : " + value);
                            if (oldValue != value) {
                                setEINFO(value, null);
                                break;
                            }
                        } catch (NumberFormatException ex22) {
                            loge("Unexpected einfo value : " + ex22);
                            break;
                        }
                    }
                }
                break;
            case 105:
                if (this.mPhone.isPhoneTypeGsm()) {
                    log("handle EVENT_ENABLE_EMMRRS_STATUS GSM");
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        data = ar.result;
                        log("EVENT_ENABLE_EMMRRS_STATUS, data[0] is : " + data[0]);
                        log("EVENT_ENABLE_EMMRRS_STATUS, einfo value is : " + data[0].substring(8));
                        oldValue = Integer.valueOf(data[0].substring(8)).intValue();
                        value = oldValue | 128;
                        log("EVENT_ENABLE_EMMRRS_STATUS, einfo value change is : " + value);
                        if (oldValue != value) {
                            setEINFO(value, null);
                            break;
                        }
                    }
                }
                break;
            case 106:
                if (this.mPhone.isPhoneTypeGsm()) {
                    ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        IccRefreshResponse res = ar.result;
                        if (res != null) {
                            switch (res.refreshResult) {
                                case 0:
                                case 5:
                                    if (res.efId == 28423) {
                                        this.mLastRegisteredPLMN = null;
                                        this.mLastPSRegisteredPLMN = null;
                                        log("Reset flag of IVSR for IMSI update");
                                        break;
                                    }
                                    break;
                                case 4:
                                case 6:
                                    this.mLastRegisteredPLMN = null;
                                    this.mLastPSRegisteredPLMN = null;
                                    log("Reset mLastRegisteredPLMN/mLastPSRegisteredPLMNfor ICC refresh");
                                    break;
                                default:
                                    log("GSST EVENT_ICC_REFRESH IccRefreshResponse =" + res);
                                    break;
                            }
                        }
                        log("IccRefreshResponse is null");
                        break;
                    }
                }
                break;
            case 107:
                onFemtoCellInfoResult((AsyncResult) msg.obj);
                break;
            case 117:
                if (this.mPhone.isPhoneTypeGsm()) {
                    onModulationInfoReceived((AsyncResult) msg.obj);
                    break;
                }
                break;
            case 118:
                if (this.mPhone.isPhoneTypeGsm()) {
                    onNetworkEventReceived((AsyncResult) msg.obj);
                    break;
                }
                break;
            case 119:
                if (this.mPhone.isPhoneTypeGsm()) {
                    onNetworkStateChangeResult((AsyncResult) msg.obj);
                    break;
                }
                break;
            case 120:
                ar = (AsyncResult) msg.obj;
                if (!(ar == null || ar.result == null)) {
                    Integer id = ar.result;
                    log("EVENT_SIM_OPL_LOADED: id=" + id);
                    if (id.intValue() == 3) {
                        if (!this.mPhone.isPhoneTypeGsm()) {
                            loge("EVENT_SIM_OPL_LOADED should not be here");
                            break;
                        }
                        refreshSpnDisplay();
                        if (this.mNeedNotify) {
                            this.mPhone.notifyServiceStateChanged(this.mSS);
                            this.mNeedNotify = false;
                            break;
                        }
                    }
                }
                break;
            case 150:
                AsyncResult arscreen = msg.obj;
                if (arscreen != null) {
                    this.mIsScreenOn = ((Boolean) arscreen.result).booleanValue();
                } else {
                    log("leon EVENT_OEM_SCREEN_CHANGED error");
                }
                if (this.mIsScreenOn) {
                    this.explict_update_spn = 1;
                    if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
                        try {
                            if (this.mServiceStateExt.needEMMRRS() && isCurrentPhoneDataConnectionOn()) {
                                getEINFO(105);
                            }
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                    }
                    this.mScreenOnTime = System.currentTimeMillis() / 1000;
                    if (this.mScreenOnTime - this.mScreenOffTime > this.SAMPLE_TIME) {
                        SubscriptionController s = SubscriptionController.getInstance();
                        SubscriptionManager.from(this.mPhone.getContext());
                        boolean isActivePhone = SubscriptionManager.getSubState(this.mPhone.getSubId()) == 1;
                        if (DBG) {
                            log("[POWERSTATE]~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~SCRENN ON~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n[POWERSTATE]mBeginNoServiceTime:" + mBeginNoServiceTime[this.mPhone.getPhoneId()] + " isActivePhone:" + isActivePhone + " mNoServiceTime:" + mNoServiceTime);
                        }
                        if (this.mPhone.getPhoneId() < 2 && isActivePhone && mBeginNoServiceTime[this.mPhone.getPhoneId()] != 0) {
                            if (mBeginNoServiceTime[this.mPhone.getPhoneId()] < this.mScreenOffTime) {
                                mBeginNoServiceTime[this.mPhone.getPhoneId()] = this.mScreenOffTime;
                                if (DBG) {
                                    log("[POWERSTATE]set mBeginNoServiceTime=mScreenOffTime:" + mBeginNoServiceTime[this.mPhone.getPhoneId()]);
                                }
                            }
                            mNoServiceTime += this.mScreenOnTime - mBeginNoServiceTime[this.mPhone.getPhoneId()];
                        }
                        mReselectCount = (double) ((long) ((((double) (this.mScreenOnTime - this.mScreenOffTime)) / 60.0d) * mReselectCountPerMin));
                        mAlreadyUpdated = true;
                        if (DBG) {
                            log("[POWERSTATE]set already updated, mReselectCount:" + mReselectCount + " getTelephonyPowerState:" + getTelephonyPowerState());
                        }
                        ConnectivityManager connectivityManager = (ConnectivityManager) this.mPhone.getContext().getSystemService("connectivity");
                        connectivityManager.setTelephonyPowerState(getTelephonyPowerState());
                        connectivityManager.setTelephonyPowerLost(getTelephonyPowerLost());
                        connectivityManager.setAlreadyUpdated(isAlreadyUpdated());
                    }
                    mCidChangeCount = 0;
                } else {
                    if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
                        try {
                            if (this.mServiceStateExt.needEMMRRS() && isCurrentPhoneDataConnectionOn()) {
                                getEINFO(104);
                            }
                        } catch (RuntimeException e2) {
                            e2.printStackTrace();
                        }
                    }
                    mNoServiceTime = 0;
                    mAlreadyUpdated = false;
                    mReselectCount = 0.0d;
                    mDataCallCount = 0;
                    mSMSSendCount = 0;
                    this.mScreenOffTime = System.currentTimeMillis() / 1000;
                    if (this.mScreenOnTime == 0) {
                        this.mScreenOnTime = this.mScreenOffTime - (SystemClock.elapsedRealtime() / 1000);
                    }
                    if (DBG) {
                        log("[POWERSTATE]-------------------------SCRENN OFF----------------------------------\n[POWERSTATE]mScreenOffTime:" + this.mScreenOffTime + "mScreenOnTime:" + this.mScreenOnTime + " mCidChangeCount:" + mCidChangeCount);
                    }
                    if (this.mScreenOffTime != this.mScreenOnTime && this.mScreenOffTime - this.mScreenOnTime > 5) {
                        mReselectCountPerMin = ((double) mCidChangeCount) / (((double) (this.mScreenOffTime - this.mScreenOnTime)) / 60.0d);
                    }
                    if (DBG) {
                        log("[POWERSTATE]mReselectCountPerMin:" + mReselectCountPerMin);
                    }
                }
                this.mTimeCount = 0;
                if (!this.mIsScreenOn) {
                    removeSmoothMessage();
                }
                log("base leon EVENT_OEM_SCREEN_CHANGED:" + this.mIsScreenOn);
                break;
            case EVENT_OEM_SMOOTH_0 /*301*/:
            case EVENT_OEM_SMOOTH_1 /*302*/:
                int index = msg.what - 301;
                SignalStrength oem_ss = getSignalStrength();
                if (index == 0) {
                    if (this.mOEMLastLevel_0 <= this.mOEMCurLevel_0) {
                        this.mOEMLastLevel_0 = this.mOEMCurLevel_0;
                        this.mIsPendingNotify_0 = false;
                        return;
                    }
                    if (this.mOEMLastLevel_0 > this.mOEMCurLevel_0) {
                        this.mOEMLastLevel_0--;
                    }
                    oem_ss.mOEMLevel_0 = this.mOEMLastLevel_0;
                } else if (index == 1) {
                    if (this.mOEMLastLevel_1 <= this.mOEMCurLevel_1) {
                        this.mOEMLastLevel_1 = this.mOEMCurLevel_1;
                        this.mIsPendingNotify_1 = false;
                        return;
                    }
                    if (this.mOEMLastLevel_1 > this.mOEMCurLevel_1) {
                        this.mOEMLastLevel_1--;
                    }
                    if (this.mOEMCurLevel_1 == 0 && this.mOEMLastLevel_1 > 0) {
                        this.mOEMLastLevel_1 = 0;
                    }
                    oem_ss.mOEMLevel_1 = this.mOEMLastLevel_1;
                }
                oppoNotifySignalStrength();
                nofifySmoothLevel(msg.what);
                if (DBG) {
                    log("leon EVENT_OEM_SMOOTH(last:(" + this.mOEMLastLevel_0 + "," + this.mOEMLastLevel_1 + ") current:(" + this.mOEMCurLevel_0 + "," + this.mOEMCurLevel_1 + ") ):" + msg.what);
                    break;
                }
                break;
            case OPPOEVENT_CHECK_NO_SERVICE /*666*/:
                this.bCheckNoServiceAgain = false;
                this.lastTime = 0;
                pollState();
                break;
            case RegionLockConstant.EVENT_NETWORK_LOCK_STATUS /*5000*/:
                log("EVENT_OPPO_CHANGED_NETWORK_LOCK_STATUS");
                Intent intent = new Intent(RegionLockConstant.ACTION_NETWORK_LOCK);
                intent.putExtra(RegionLockConstant.NETLOCK_STATUS, "0");
                intent.putExtra(RegionLockConstant.UNLOCK_TYPE, "1");
                sendBroadCastChangedNetlockStatus(intent);
                break;
            case RegionLockConstant.EVENT_UPDATE_POWER_RADIO /*50001*/:
                log("EVENT_OPPO_UPDATE_POWER_RADIO");
                updatePowerRadioStatus();
                break;
            default:
                log("Unhandled message with number: " + msg.what);
                break;
        }
    }

    protected int calculateDeviceRatMode(int phoneId) {
        int networkType = -1;
        int restrictedNwMode = -1;
        int capabilityPhoneId = RadioCapabilitySwitchUtil.getMainCapabilityPhoneId();
        if (this.mPhone.isPhoneTypeGsm()) {
            if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
                try {
                    if (this.mServiceStateExt.isSupportRatBalancing()) {
                        logd("networkType is controlled by RAT Blancing, no need to set network type");
                        return -1;
                    }
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
            if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
                try {
                    restrictedNwMode = this.mServiceStateExt.needAutoSwitchRatMode(phoneId, this.mLocatedPlmn);
                } catch (RuntimeException e2) {
                    e2.printStackTrace();
                }
            }
            if (!OemConstant.EXP_VERSION) {
                networkType = getPreferredNetworkModeSettings(phoneId);
            } else if (phoneId == capabilityPhoneId) {
                networkType = getPreferredNetworkModeSettings(phoneId);
            } else if (SystemProperties.get("ro.oppo.prepaid.without.2G").equals("1")) {
                networkType = 12;
            } else {
                networkType = 0;
            }
            if (restrictedNwMode >= 0 && restrictedNwMode != networkType) {
                logd("Revise networkType to " + restrictedNwMode);
                networkType = restrictedNwMode;
            }
        } else {
            if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
                try {
                    networkType = this.mServiceStateExt.getNetworkTypeForMota(phoneId);
                    log("[CDMA], networkType for mota is: " + networkType);
                } catch (RuntimeException e22) {
                    e22.printStackTrace();
                }
            }
            if (networkType == -1) {
                networkType = getPreferredNetworkModeSettings(phoneId);
            }
        }
        logd("calculateDeviceRatMode=" + networkType + ", restrictedNwMode=" + restrictedNwMode + ", phoneId " + phoneId);
        return networkType;
    }

    protected void setDeviceRatMode(int phoneId) {
        int networkType = calculateDeviceRatMode(phoneId);
        if (networkType >= 0) {
            this.mPhone.setPreferredNetworkType(networkType, null);
        }
    }

    public boolean isPsRegStateRoamByUnsol() {
        return regCodeIsRoaming(this.mPsRegStateRaw);
    }

    protected boolean isSidsAllZeros() {
        if (this.mHomeSystemId != null) {
            for (int i : this.mHomeSystemId) {
                if (i != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isHomeSid(int sid) {
        if (this.mHomeSystemId != null) {
            for (int i : this.mHomeSystemId) {
                if (sid == i) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getMdnNumber() {
        return this.mMdn;
    }

    public String getCdmaMin() {
        return this.mMin;
    }

    public String getPrlVersion() {
        return this.mPrlVersion;
    }

    public String getImsi() {
        String operatorNumeric = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getSimOperatorNumericForPhone(this.mPhone.getPhoneId());
        if (!TextUtils.isEmpty(operatorNumeric) && getCdmaMin() != null) {
            return operatorNumeric + getCdmaMin();
        }
        if (this.mIccRecords != null) {
            return this.mIccRecords.getIMSI();
        }
        log("getImsi,imsi is null");
        return null;
    }

    public boolean isMinInfoReady() {
        return this.mIsMinInfoReady;
    }

    public int getOtasp() {
        if (!this.mPhone.getIccRecordsLoaded()) {
            if (DBG) {
                log("getOtasp: otasp uninitialized due to sim not loaded");
            }
            return 0;
        } else if (this.mPhone.isPhoneTypeGsm()) {
            if (DBG) {
                log("getOtasp: otasp not needed for GSM");
            }
            return 3;
        } else if (this.mIsSubscriptionFromRuim && this.mMin == null) {
            return 2;
        } else {
            int provisioningState;
            if (this.mMin == null || this.mMin.length() < 6) {
                if (DBG) {
                    log("getOtasp: bad mMin='" + this.mMin + "'");
                }
                provisioningState = 1;
            } else if (this.mMin.equals(UNACTIVATED_MIN_VALUE) || this.mMin.substring(0, 6).equals(UNACTIVATED_MIN2_VALUE) || SystemProperties.getBoolean("test_cdma_setup", false)) {
                provisioningState = 2;
            } else {
                provisioningState = 3;
            }
            if (DBG) {
                log("getOtasp: state=" + provisioningState);
            }
            return provisioningState;
        }
    }

    protected void parseSidNid(String sidStr, String nidStr) {
        int i;
        if (sidStr != null) {
            String[] sid = sidStr.split(",");
            this.mHomeSystemId = new int[sid.length];
            for (i = 0; i < sid.length; i++) {
                try {
                    this.mHomeSystemId[i] = Integer.parseInt(sid[i]);
                } catch (NumberFormatException ex) {
                    loge("error parsing system id: " + ex);
                }
            }
        }
        if (DBG) {
            log("CDMA_SUBSCRIPTION: SID=" + sidStr);
        }
        if (nidStr != null) {
            String[] nid = nidStr.split(",");
            this.mHomeNetworkId = new int[nid.length];
            for (i = 0; i < nid.length; i++) {
                try {
                    this.mHomeNetworkId[i] = Integer.parseInt(nid[i]);
                } catch (NumberFormatException ex2) {
                    loge("CDMA_SUBSCRIPTION: error parsing network id: " + ex2);
                }
            }
        }
        if (DBG) {
            log("CDMA_SUBSCRIPTION: NID=" + nidStr);
        }
    }

    protected void updateOtaspState() {
        int otaspMode = getOtasp();
        int oldOtaspMode = this.mCurrentOtaspMode;
        this.mCurrentOtaspMode = otaspMode;
        if (oldOtaspMode != this.mCurrentOtaspMode) {
            if (DBG) {
                log("updateOtaspState: call notifyOtaspChanged old otaspMode=" + oldOtaspMode + " new otaspMode=" + this.mCurrentOtaspMode);
            }
            this.mPhone.notifyOtaspChanged(this.mCurrentOtaspMode);
        }
    }

    protected Phone getPhone() {
        return this.mPhone;
    }

    protected void handlePollStateResult(int what, AsyncResult ar) {
        this.psLac = -1;
        this.psCid = -1;
        if (ar.userObj != this.mPollingContext) {
            if (what == 14) {
                logd("EVENT_POLL_STATE_NETWORK_SELECTION_MODE, return due to(ar.userObj != mPollingContext)");
            } else if (what == 4) {
                logd("EVENT_POLL_STATE_REGISTRATION, return due to(ar.userObj != mPollingContext)");
            } else if (what == 5) {
                logd("EVENT_POLL_STATE_GPRS, return due to(ar.userObj != mPollingContext)");
            } else if (what == 6) {
                logd("EVENT_POLL_STATE_OPERATOR, return due to(ar.userObj != mPollingContext)");
            }
            return;
        }
        if (ar.exception != null) {
            Error err = null;
            if (ar.exception instanceof CommandException) {
                err = ((CommandException) ar.exception).getCommandError();
            }
            if (err == Error.RADIO_NOT_AVAILABLE) {
                cancelPollState();
                loge("handlePollStateResult cancelPollState due to RADIO_NOT_AVAILABLE");
                if (this.hasPendingPollState) {
                    this.hasPendingPollState = false;
                    pollState();
                    if (DBG) {
                        loge("handlePollStateResult trigger pending pollState()");
                    }
                } else if (this.mCi.getRadioState() != RadioState.RADIO_ON) {
                    this.mNewSS.setStateOff();
                    this.mNewCellLoc.setStateInvalid();
                    setSignalStrengthDefaultValues();
                    this.mGotCountryCode = false;
                    this.mNitzUpdatedTime = false;
                    setNullState();
                    this.mPsRegStateRaw = 0;
                    pollStateDone();
                    loge("handlePollStateResult pollStateDone to notify RADIO_NOT_AVAILABLE");
                }
                return;
            } else if (err != Error.OP_NOT_ALLOWED_BEFORE_REG_NW) {
                loge("RIL implementation has returned an error where it must succeed" + ar.exception);
            }
        } else {
            try {
                handlePollStateResultMessage(what, ar);
            } catch (RuntimeException ex) {
                loge("Exception while polling service state. Probably malformed RIL response." + ex);
            }
        }
        int[] iArr = this.mPollingContext;
        iArr[0] = iArr[0] - 1;
        if (this.mPollingContext[0] == 0) {
            if (!(this.mNewSS.getVoiceRegState() == 0 || this.mNewSS.getDataRegState() == 0)) {
                this.mNewSS.setOperatorName(null, null, null);
            }
            if (this.mPhone.isPhoneTypeGsm()) {
                if (this.mPendingPsRestrictDisabledNotify) {
                    this.mPsRestrictDisabledRegistrants.notifyRegistrants();
                    setNotification(1002);
                    this.mPendingPsRestrictDisabledNotify = false;
                }
                if (this.mNewSS.getState() != 0 && this.mNewSS.getDataRegState() == 0) {
                    log("update cellLoc by +CGREG");
                    ((GsmCellLocation) this.mNewCellLoc).setLacAndCid(this.psLac, this.psCid);
                }
                updateRoamingState();
                if (this.voiceUrcWith4G) {
                    this.mEmergencyOnly = false;
                    ImsPhone imsPhone = (ImsPhone) this.mPhone.getImsPhone();
                    if (!(!this.mPhone.isImsUseEnabled() || imsPhone == null || imsPhone.getServiceState() == null || !imsPhone.isSupportLteEcc() || this.mSS.getVoiceRegState() == 0 || this.mSS.getDataRegState() == 0)) {
                        log("set mEmergencyOnly = true for Ims ECC");
                        this.mEmergencyOnly = true;
                    }
                } else if (this.mEmergencyOnly && this.mSS.getDataRegState() == 0) {
                    this.mEmergencyOnly = false;
                }
                this.mNewSS.setEmergencyOnly(this.mEmergencyOnly);
            } else {
                boolean namMatch = false;
                if (!isSidsAllZeros() && isHomeSid(this.mNewSS.getSystemId())) {
                    namMatch = true;
                }
                if (this.mIsSubscriptionFromRuim) {
                    this.mNewSS.setVoiceRoaming(isRoamingBetweenOperators(this.mNewSS.getVoiceRoaming(), this.mNewSS));
                }
                boolean isVoiceInService = this.mNewSS.getVoiceRegState() == 0;
                int dataRegType = this.mNewSS.getRilDataRadioTechnology();
                if (isVoiceInService && ServiceState.isCdma(dataRegType)) {
                    this.mNewSS.setDataRoaming(this.mNewSS.getVoiceRoaming());
                }
                this.mEmergencyOnly = false;
                if (this.mCi.getRadioState().isOn() && this.mNewSS.getVoiceRegState() == 1 && this.mNewSS.getDataRegState() == 1 && this.mNetworkExsit) {
                    this.mEmergencyOnly = true;
                }
                if (DBG) {
                    log("[CDMA]handlePollStateResult: set mEmergencyOnly=" + this.mEmergencyOnly + ", mNetworkExsit=" + this.mNetworkExsit);
                }
                this.mNewSS.setEmergencyOnly(this.mEmergencyOnly);
                this.mNewSS.setCdmaDefaultRoamingIndicator(this.mDefaultRoamingIndicator);
                this.mNewSS.setCdmaRoamingIndicator(this.mRoamingIndicator);
                boolean isPrlLoaded = true;
                if (TextUtils.isEmpty(this.mPrlVersion)) {
                    isPrlLoaded = false;
                }
                if (!isPrlLoaded || this.mNewSS.getRilVoiceRadioTechnology() == 0) {
                    log("Turn off roaming indicator if !isPrlLoaded or voice RAT is unknown");
                    this.mNewSS.setCdmaRoamingIndicator(1);
                } else if (!isSidsAllZeros()) {
                    if (!namMatch && !this.mIsInPrl) {
                        this.mNewSS.setCdmaRoamingIndicator(this.mDefaultRoamingIndicator);
                    } else if (!namMatch || this.mIsInPrl) {
                        if (!namMatch && this.mIsInPrl) {
                            this.mNewSS.setCdmaRoamingIndicator(this.mRoamingIndicator);
                        } else if (this.mRoamingIndicator <= 2) {
                            this.mNewSS.setCdmaRoamingIndicator(1);
                        } else {
                            this.mNewSS.setCdmaRoamingIndicator(this.mRoamingIndicator);
                        }
                    } else if (ServiceState.isLte(this.mNewSS.getRilVoiceRadioTechnology())) {
                        log("Turn off roaming indicator as voice is LTE");
                        this.mNewSS.setCdmaRoamingIndicator(1);
                    } else {
                        this.mNewSS.setCdmaRoamingIndicator(2);
                    }
                }
                int roamingIndicator = this.mNewSS.getCdmaRoamingIndicator();
                this.mNewSS.setCdmaEriIconIndex(this.mPhone.mEriManager.getCdmaEriIconIndex(roamingIndicator, this.mDefaultRoamingIndicator));
                this.mNewSS.setCdmaEriIconMode(this.mPhone.mEriManager.getCdmaEriIconMode(roamingIndicator, this.mDefaultRoamingIndicator));
                if (DBG) {
                    log("Set CDMA Roaming Indicator to: " + this.mNewSS.getCdmaRoamingIndicator() + ". voiceRoaming = " + this.mNewSS.getVoiceRoaming() + ". dataRoaming = " + this.mNewSS.getDataRoaming() + ", isPrlLoaded = " + isPrlLoaded + ". namMatch = " + namMatch + " , mIsInPrl = " + this.mIsInPrl + ", mRoamingIndicator = " + this.mRoamingIndicator + ", mDefaultRoamingIndicator= " + this.mDefaultRoamingIndicator);
                }
            }
            pollStateDone();
        }
    }

    private boolean isRoamingBetweenOperators(boolean cdmaRoaming, ServiceState s) {
        return cdmaRoaming && !isSameOperatorNameFromSimAndSS(s);
    }

    /* JADX WARNING: Removed duplicated region for block: B:169:0x0569  */
    /* JADX WARNING: Removed duplicated region for block: B:151:0x0490  */
    /* JADX WARNING: Removed duplicated region for block: B:170:0x0574  */
    /* JADX WARNING: Removed duplicated region for block: B:154:0x04cb  */
    /* JADX WARNING: Removed duplicated region for block: B:171:0x0577  */
    /* JADX WARNING: Removed duplicated region for block: B:157:0x04e1  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void handlePollStateResultMessage(int what, AsyncResult ar) {
        String[] states;
        int regState;
        int tempLac;
        int tempCid;
        switch (what) {
            case 4:
                if (this.mPhone.isPhoneTypeGsm()) {
                    states = (String[]) ar.result;
                    int lac = -1;
                    int cid = -1;
                    regState = 4;
                    int psc = -1;
                    if (states.length > 0) {
                        try {
                            regState = Integer.parseInt(states[0]);
                            if (states.length >= 3) {
                                if (states[1] != null && states[1].length() > 0) {
                                    tempLac = Integer.parseInt(states[1], 16);
                                    if (tempLac < 0) {
                                        log("set Lac to previous value");
                                        tempLac = ((GsmCellLocation) this.mCellLoc).getLac();
                                    }
                                    lac = tempLac;
                                }
                                if (states[2] != null && states[2].length() > 0) {
                                    tempCid = Integer.parseInt(states[2], 16);
                                    if (tempCid < 0) {
                                        log("set Cid to previous value");
                                        tempCid = ((GsmCellLocation) this.mCellLoc).getCid();
                                    }
                                    cid = tempCid;
                                }
                                if (states.length >= 4 && states[3] != null && states[3].length() > 0) {
                                    int mNwType = Integer.parseInt(states[3]);
                                    if (mNwType == -1) {
                                        updateNetworkInfo(regState, 0);
                                    } else {
                                        this.mNewSS.setRilVoiceRadioTechnology(mNwType);
                                    }
                                }
                                if (states.length >= 14 && states[13] != null && states[13].length() > 0) {
                                    int rejCause = Integer.parseInt(states[13]);
                                    this.mNewSS.setVoiceRejectCause(rejCause);
                                    logd("set voice reject cause to " + rejCause);
                                }
                            }
                            if (states.length > 14 && states[14] != null && states[14].length() > 0) {
                                psc = Integer.parseInt(states[14], 16);
                            }
                            log("EVENT_POLL_STATE_REGISTRATION mSS getRilVoiceRadioTechnology:" + this.mSS.getRilVoiceRadioTechnology() + ", regState:" + regState + ", NewSS RilVoiceRadioTechnology:" + this.mNewSS.getRilVoiceRadioTechnology() + ", lac:" + lac + ", cid:" + cid);
                        } catch (NumberFormatException ex) {
                            loge("error parsing RegistrationState: " + ex);
                        }
                    }
                    this.mGsmRoaming = regCodeIsRoaming(regState);
                    this.mNewSS.setVoiceRegState(regCodeToServiceState(regState));
                    if (this.mGsmRoaming) {
                        this.mNewSS.setRilVoiceRegState(5);
                    } else {
                        this.mNewSS.setRilVoiceRegState(regState);
                    }
                    boolean isVoiceCapable = this.mPhone.getContext().getResources().getBoolean(17956954);
                    if ((regState == 13 || regState == 10 || regState == 12 || regState == 14) && isVoiceCapable) {
                        this.mEmergencyOnly = true;
                    } else {
                        this.mEmergencyOnly = false;
                    }
                    log("[states.length] = " + states.length + ", regState = " + regState + ", isVoiceCapable = " + isVoiceCapable + ", mEmergencyOnly = " + this.mEmergencyOnly);
                    if (states.length > 3) {
                        if (lac == 65534 || cid == 268435455) {
                            log("unknown lac:" + lac + " or cid:" + cid);
                        } else if (regCodeToServiceState(regState) != 1) {
                            int index = this.mPhone.getPhoneId() % 2;
                            if (!(mCid[index] == ((long) cid) || cid == -1)) {
                                if (mCid[index] != -1) {
                                    mCidChangeCount++;
                                }
                                if (DBG) {
                                    log("[POWERSTATE]set mCidChangeCount:" + mCidChangeCount + " cid:" + cid);
                                }
                                mCid[index] = (long) cid;
                            }
                            ((GsmCellLocation) this.mNewCellLoc).setLacAndCid(lac, cid);
                        }
                    }
                    ((GsmCellLocation) this.mNewCellLoc).setPsc(psc);
                    hasEonsChanged();
                    return;
                }
                states = (String[]) ar.result;
                int registrationState = 4;
                int radioTechnology = -1;
                int baseStationId = -1;
                int baseStationLatitude = Integer.MAX_VALUE;
                int baseStationLongitude = Integer.MAX_VALUE;
                int cssIndicator = 0;
                int systemId = 0;
                int networkId = 0;
                int roamingIndicator = -1;
                int systemIsInPrl = 0;
                int defaultRoamingIndicator = 0;
                int reasonForDenial = 0;
                if (states.length >= 14) {
                    boolean cdmaRoaming;
                    try {
                        if (states[0] != null) {
                            registrationState = Integer.parseInt(states[0]);
                        }
                        if (states[3] != null) {
                            radioTechnology = Integer.parseInt(states[3]);
                            if (radioTechnology == -1) {
                                radioTechnology = 0;
                            }
                        }
                        if (states[4] != null) {
                            baseStationId = Integer.parseInt(states[4]);
                        }
                        if (states[5] != null) {
                            baseStationLatitude = Integer.parseInt(states[5]);
                        }
                        if (states[6] != null) {
                            baseStationLongitude = Integer.parseInt(states[6]);
                        }
                        if (baseStationLatitude == 0 && baseStationLongitude == 0) {
                            baseStationLatitude = Integer.MAX_VALUE;
                            baseStationLongitude = Integer.MAX_VALUE;
                        }
                        if (states[7] != null) {
                            cssIndicator = Integer.parseInt(states[7]);
                        }
                        if (states[8] != null) {
                            systemId = Integer.parseInt(states[8]);
                        }
                        if (states[9] != null) {
                            networkId = Integer.parseInt(states[9]);
                        }
                        if (states[10] != null) {
                            roamingIndicator = Integer.parseInt(states[10]);
                        }
                        if (states[11] != null) {
                            systemIsInPrl = Integer.parseInt(states[11]);
                        }
                        if (states[12] != null) {
                            defaultRoamingIndicator = Integer.parseInt(states[12]);
                        }
                        if (states[13] != null) {
                            reasonForDenial = Integer.parseInt(states[13]);
                        }
                        if (states.length > 15 && states[15] != null) {
                            boolean z;
                            if (1 == Integer.parseInt(states[15])) {
                                z = true;
                            } else {
                                z = false;
                            }
                            this.mNetworkExsit = z;
                        }
                    } catch (NumberFormatException ex2) {
                        loge("EVENT_POLL_STATE_REGISTRATION_CDMA: error parsing: " + ex2);
                    }
                    this.mRegistrationState = registrationState;
                    if (regCodeIsRoaming(registrationState)) {
                        if (!isRoamIndForHomeSystem(states[10])) {
                            cdmaRoaming = true;
                            this.mNewSS.setVoiceRoaming(cdmaRoaming);
                            if (cdmaRoaming) {
                                this.mNewSS.setRilVoiceRegState(registrationState);
                            } else {
                                this.mNewSS.setRilVoiceRegState(5);
                            }
                            this.mNewSS.setVoiceRegState(regCodeToServiceState(registrationState));
                            this.mNewSS.setRilVoiceRadioTechnology(radioTechnology);
                            this.mNewSS.setCssIndicator(cssIndicator);
                            this.mNewSS.setSystemAndNetworkId(systemId, networkId);
                            this.mRoamingIndicator = roamingIndicator;
                            this.mIsInPrl = systemIsInPrl == 0;
                            this.mDefaultRoamingIndicator = defaultRoamingIndicator;
                            ((CdmaCellLocation) this.mNewCellLoc).setCellLocationData(baseStationId, baseStationLatitude, baseStationLongitude, systemId, networkId);
                            if (reasonForDenial != 0) {
                                this.mRegistrationDeniedReason = REGISTRATION_DENIED_GEN;
                            } else if (reasonForDenial == 1) {
                                this.mRegistrationDeniedReason = REGISTRATION_DENIED_AUTH;
                            } else {
                                this.mRegistrationDeniedReason = UsimPBMemInfo.STRING_NOT_SET;
                            }
                            if (this.mRegistrationState == 3 && DBG) {
                                log("Registration denied, " + this.mRegistrationDeniedReason);
                                return;
                            }
                            return;
                        }
                    }
                    cdmaRoaming = false;
                    this.mNewSS.setVoiceRoaming(cdmaRoaming);
                    if (cdmaRoaming) {
                    }
                    this.mNewSS.setVoiceRegState(regCodeToServiceState(registrationState));
                    this.mNewSS.setRilVoiceRadioTechnology(radioTechnology);
                    this.mNewSS.setCssIndicator(cssIndicator);
                    this.mNewSS.setSystemAndNetworkId(systemId, networkId);
                    this.mRoamingIndicator = roamingIndicator;
                    if (systemIsInPrl == 0) {
                    }
                    this.mIsInPrl = systemIsInPrl == 0;
                    this.mDefaultRoamingIndicator = defaultRoamingIndicator;
                    ((CdmaCellLocation) this.mNewCellLoc).setCellLocationData(baseStationId, baseStationLatitude, baseStationLongitude, systemId, networkId);
                    if (reasonForDenial != 0) {
                    }
                    if (this.mRegistrationState == 3) {
                        return;
                    }
                    return;
                }
                throw new RuntimeException("Warning! Wrong number of parameters returned from RIL_REQUEST_REGISTRATION_STATE: expected 14 or more strings and got " + states.length + " strings");
            case 5:
                int dataRegState;
                Object states2;
                if (this.mPhone.isPhoneTypeGsm()) {
                    states = (String[]) ar.result;
                    int type = 0;
                    regState = 4;
                    this.mNewReasonDataDenied = -1;
                    this.mNewMaxDataCalls = 1;
                    if (states.length > 0) {
                        try {
                            regState = Integer.parseInt(states[0]);
                            if (states.length >= 3) {
                                if (states[1] != null && states[1].length() > 0) {
                                    tempLac = Integer.parseInt(states[1], 16);
                                    if (tempLac < 0) {
                                        logd("set Lac to previous value");
                                        tempLac = ((GsmCellLocation) this.mCellLoc).getLac();
                                    }
                                    this.psLac = tempLac;
                                }
                                if (states[2] != null && states[2].length() > 0) {
                                    tempCid = Integer.parseInt(states[2], 16);
                                    if (tempCid < 0) {
                                        logd("set Cid to previous value");
                                        tempCid = ((GsmCellLocation) this.mCellLoc).getCid();
                                    }
                                    this.psCid = tempCid;
                                }
                            }
                            if (states.length >= 4 && states[3] != null) {
                                type = Integer.parseInt(states[3]);
                            }
                            if (states.length >= 5 && states[4] != null && regState == 3) {
                                this.mNewReasonDataDenied = Integer.parseInt(states[4]);
                                this.mNewSS.setDataRejectCause(this.mNewReasonDataDenied);
                                log("set data reject cause to " + this.mNewReasonDataDenied);
                            }
                            if (states.length >= 6 && states[5] != null) {
                                this.mNewMaxDataCalls = Integer.parseInt(states[5]);
                            }
                        } catch (NumberFormatException ex22) {
                            loge("error parsing GprsRegistrationState: " + ex22);
                        }
                    }
                    dataRegState = regCodeToServiceState(regState);
                    this.mNewSS.setRilDataRegState(regState);
                    this.mNewSS.setDataRegState(dataRegState);
                    this.mDataRoaming = regCodeIsRoaming(regState);
                    this.mNewSS.setRilDataRadioTechnology(type);
                    this.mNewSS.setProprietaryDataRadioTechnology(type);
                    if (DBG) {
                        log("handlPollStateResultMessage: GsmSST setDataRegState=" + dataRegState + " regState=" + regState + " dataRadioTechnology=" + type);
                        return;
                    }
                    return;
                } else if (this.mPhone.isPhoneTypeCdma()) {
                    states2 = (String[]) ar.result;
                    if (DBG) {
                        log("handlePollStateResultMessage: EVENT_POLL_STATE_GPRS states.length=" + states2.length + " states=" + states2);
                    }
                    regState = 4;
                    int dataRadioTechnology = 0;
                    if (states2.length > 0) {
                        try {
                            regState = Integer.parseInt(states2[0]);
                            if (states2.length >= 4 && states2[3] != null) {
                                dataRadioTechnology = Integer.parseInt(states2[3]);
                            }
                        } catch (NumberFormatException ex222) {
                            loge("handlePollStateResultMessage: error parsing GprsRegistrationState: " + ex222);
                        }
                    }
                    dataRegState = regCodeToServiceState(regState);
                    this.mNewSS.setDataRegState(dataRegState);
                    this.mNewSS.setRilDataRegState(regState);
                    this.mNewSS.setRilDataRadioTechnology(dataRadioTechnology);
                    this.mNewSS.setDataRoaming(regCodeIsRoaming(regState));
                    if (DBG) {
                        log("handlPollStateResultMessage: cdma setDataRegState=" + dataRegState + " regState=" + regState + " dataRadioTechnology=" + dataRadioTechnology);
                        return;
                    }
                    return;
                } else {
                    states2 = (String[]) ar.result;
                    if (DBG) {
                        log("handlePollStateResultMessage: EVENT_POLL_STATE_GPRS states.length=" + states2.length + " states=" + states2);
                    }
                    int newDataRAT = 0;
                    regState = -1;
                    if (states2.length > 0) {
                        try {
                            regState = Integer.parseInt(states2[0]);
                            if (states2.length >= 4 && states2[3] != null) {
                                newDataRAT = Integer.parseInt(states2[3]);
                            }
                        } catch (NumberFormatException ex2222) {
                            loge("handlePollStateResultMessage: error parsing GprsRegistrationState: " + ex2222);
                        }
                    }
                    int oldDataRAT = this.mSS.getRilDataRadioTechnology();
                    if ((oldDataRAT == 0 && newDataRAT != 0) || ((ServiceState.isCdma(oldDataRAT) && ServiceState.isLte(newDataRAT)) || (ServiceState.isLte(oldDataRAT) && ServiceState.isCdma(newDataRAT)))) {
                        this.mCi.getSignalStrength(obtainMessage(3));
                    }
                    this.mNewSS.setRilDataRadioTechnology(newDataRAT);
                    dataRegState = regCodeToServiceState(regState);
                    this.mNewSS.setDataRegState(dataRegState);
                    this.mNewSS.setRilDataRegState(regState);
                    this.mNewSS.setProprietaryDataRadioTechnology(newDataRAT);
                    boolean isDateRoaming = regCodeIsRoaming(regState);
                    this.mNewSS.setDataRoaming(isDateRoaming);
                    if (isDateRoaming) {
                        this.mNewSS.setRilDataRegState(5);
                    }
                    if (DBG) {
                        log("handlPollStateResultMessage: CdmaLteSST setDataRegState=" + dataRegState + " regState=" + regState + " dataRadioTechnology=" + newDataRAT);
                        return;
                    }
                    return;
                }
            case 6:
                String[] opNames;
                String brandOverride;
                SpnOverride spnOverride;
                String strOperatorLong;
                String strOperatorShort;
                if (this.mPhone.isPhoneTypeGsm()) {
                    opNames = (String[]) ar.result;
                    if (opNames != null && opNames.length >= 3) {
                        brandOverride = this.mUiccController.getUiccCard(getPhoneId()) != null ? this.mUiccController.getUiccCard(getPhoneId()).getOperatorBrandOverride() : null;
                        if (brandOverride != null) {
                            log("EVENT_POLL_STATE_OPERATOR: use brandOverride=" + brandOverride);
                            this.mNewSS.setOperatorName(brandOverride, brandOverride, opNames[2]);
                        } else {
                            spnOverride = SpnOverride.getInstance();
                            strOperatorLong = this.mCi.lookupOperatorNameFromNetwork((long) SubscriptionManager.getSubIdUsingPhoneId(this.mPhone.getPhoneId()), opNames[2], true);
                            if (TextUtils.isEmpty(strOperatorLong)) {
                                strOperatorLong = spnOverride.lookupOperatorName(SubscriptionManager.getSubIdUsingPhoneId(this.mPhone.getPhoneId()), opNames[2], true, this.mPhone.getContext());
                                if (strOperatorLong != null) {
                                    logd("EVENT_POLL_STATE_OPERATOR: OperatorLong use lookupOperatorName");
                                    strOperatorLong = this.mServiceStateExt.updateOpAlphaLongForHK(strOperatorLong, opNames[2], this.mPhone.getPhoneId());
                                } else {
                                    log("EVENT_POLL_STATE_OPERATOR: OperatorLong use value from ril");
                                    strOperatorLong = opNames[0];
                                }
                            } else {
                                log("EVENT_POLL_STATE_OPERATOR: OperatorLong use lookFromNetwork");
                            }
                            strOperatorShort = this.mCi.lookupOperatorNameFromNetwork((long) SubscriptionManager.getSubIdUsingPhoneId(this.mPhone.getPhoneId()), opNames[2], false);
                            if (TextUtils.isEmpty(strOperatorShort)) {
                                strOperatorShort = spnOverride.lookupOperatorName(SubscriptionManager.getSubIdUsingPhoneId(this.mPhone.getPhoneId()), opNames[2], false, this.mPhone.getContext());
                                if (strOperatorShort != null) {
                                    logd("EVENT_POLL_STATE_OPERATOR: OperatorShort use lookupOperatorName");
                                } else {
                                    log("EVENT_POLL_STATE_OPERATOR: OperatorShort use value from ril");
                                    strOperatorShort = opNames[1];
                                }
                            } else {
                                log("EVENT_POLL_STATE_OPERATOR: OperatorShort use lookupOperatorNameFromNetwork");
                            }
                            log("EVENT_POLL_STATE_OPERATOR: " + strOperatorLong + ", " + strOperatorShort);
                            this.mNewSS.setOperatorName(strOperatorLong, strOperatorShort, opNames[2]);
                        }
                        updateLocatedPlmn(opNames[2]);
                        return;
                    } else if (opNames != null && opNames.length == 1) {
                        log("opNames:" + opNames[0] + " len=" + opNames[0].length());
                        this.mNewSS.setOperatorName(null, null, null);
                        if (opNames[0].length() < 5 || opNames[0].equals(UNACTIVATED_MIN2_VALUE)) {
                            updateLocatedPlmn(null);
                            return;
                        } else {
                            updateLocatedPlmn(opNames[0]);
                            return;
                        }
                    } else {
                        return;
                    }
                }
                opNames = (String[]) ar.result;
                if (opNames != null && opNames.length >= 3) {
                    if (opNames[2] == null || opNames[2].length() < 5 || "00000".equals(opNames[2]) || "N/AN/A".equals(opNames[2])) {
                        opNames[2] = SystemProperties.get(GsmCdmaPhone.PROPERTY_CDMA_HOME_OPERATOR_NUMERIC, UsimPBMemInfo.STRING_NOT_SET);
                        if (DBG) {
                            log("RIL_REQUEST_OPERATOR.response[2], the numeric,  is bad. Using SystemProperties 'ro.cdma.home.operator.numeric'= " + opNames[2]);
                        }
                    }
                    String numeric = opNames[2];
                    boolean plusCode = false;
                    if (numeric.startsWith("2134") && numeric.length() == 7) {
                        String tempStr = this.mPlusCodeUtils.checkMccBySidLtmOff(numeric);
                        if (!tempStr.equals("0")) {
                            opNames[2] = tempStr + numeric.substring(4);
                            numeric = tempStr;
                            log("EVENT_POLL_STATE_OPERATOR_CDMA: checkMccBySidLtmOff: numeric =" + tempStr + ", plmn =" + opNames[2]);
                        }
                        plusCode = true;
                    }
                    if (this.mIsSubscriptionFromRuim) {
                        if (this.mUiccController.getUiccCard(getPhoneId()) != null) {
                            brandOverride = this.mUiccController.getUiccCard(getPhoneId()).getOperatorBrandOverride();
                        } else {
                            brandOverride = null;
                        }
                        if (brandOverride != null) {
                            log("EVENT_POLL_STATE_OPERATOR_CDMA: use brand=" + brandOverride);
                            this.mNewSS.setOperatorName(brandOverride, brandOverride, opNames[2]);
                        } else {
                            spnOverride = SpnOverride.getInstance();
                            strOperatorLong = this.mCi.lookupOperatorNameFromNetwork((long) SubscriptionManager.getSubIdUsingPhoneId(this.mPhone.getPhoneId()), opNames[2], true);
                            if (strOperatorLong != null) {
                                log("EVENT_POLL_STATE_OPERATOR_CDMA: OperatorLong use lookupOperatorNameFromNetwork");
                            } else {
                                strOperatorLong = spnOverride.lookupOperatorName(SubscriptionManager.getSubIdUsingPhoneId(this.mPhone.getPhoneId()), opNames[2], true, this.mPhone.getContext());
                                if (strOperatorLong != null) {
                                    log("EVENT_POLL_STATE_OPERATOR_CDMA: OperatorLong use lookupOperatorName");
                                } else {
                                    log("EVENT_POLL_STATE_OPERATOR_CDMA: OperatorLong use value from ril");
                                    strOperatorLong = opNames[0];
                                }
                            }
                            strOperatorShort = this.mCi.lookupOperatorNameFromNetwork((long) SubscriptionManager.getSubIdUsingPhoneId(this.mPhone.getPhoneId()), opNames[2], false);
                            if (strOperatorShort != null) {
                                log("EVENT_POLL_STATE_OPERATOR_CDMA: OperatorShort use lookupOperatorNameFromNetwork");
                            } else {
                                strOperatorShort = spnOverride.lookupOperatorName(SubscriptionManager.getSubIdUsingPhoneId(this.mPhone.getPhoneId()), opNames[2], false, this.mPhone.getContext());
                                if (strOperatorShort != null) {
                                    log("EVENT_POLL_STATE_OPERATOR_CDMA: OperatorShort use lookupOperatorName");
                                } else {
                                    log("EVENT_POLL_STATE_OPERATOR_CDMA: OperatorShort use value from ril");
                                    strOperatorShort = opNames[1];
                                }
                            }
                            log("EVENT_POLL_STATE_OPERATOR_CDMA: " + strOperatorLong + ", " + strOperatorShort);
                            this.mNewSS.setOperatorName(strOperatorLong, strOperatorShort, opNames[2]);
                        }
                    } else {
                        if (plusCode) {
                            opNames[1] = SpnOverride.getInstance().lookupOperatorName(this.mPhone.getSubId(), opNames[2], false, this.mPhone.getContext());
                        }
                        this.mNewSS.setOperatorName(null, opNames[1], opNames[2]);
                    }
                    if (opNames[2] == null || opNames[2].length() < 5 || opNames[2].equals(UNACTIVATED_MIN2_VALUE)) {
                        updateLocatedPlmn(null);
                        return;
                    } else {
                        updateLocatedPlmn(opNames[2]);
                        return;
                    }
                } else if (DBG) {
                    log("EVENT_POLL_STATE_OPERATOR_CDMA: error parsing opNames");
                    return;
                } else {
                    return;
                }
            case 14:
                int[] ints = (int[]) ar.result;
                this.mNewSS.setIsManualSelection(ints[0] == 1);
                if (ints[0] == 1 && !this.mPhone.isManualNetSelAllowed()) {
                    this.mPhone.setNetworkSelectionModeAutomatic(null);
                    log(" Forcing Automatic Network Selection, manual selection is not allowed");
                    return;
                }
                return;
            default:
                loge("handlePollStateResultMessage: Unexpected RIL response received: " + what);
                return;
        }
    }

    private boolean isRoamIndForHomeSystem(String roamInd) {
        String[] homeRoamIndicators = this.mPhone.getContext().getResources().getStringArray(17236034);
        if (homeRoamIndicators == null) {
            return false;
        }
        for (String homeRoamInd : homeRoamIndicators) {
            if (homeRoamInd.equals(roamInd)) {
                return true;
            }
        }
        return false;
    }

    protected void updateRoamingState() {
        CarrierConfigManager configLoader;
        PersistableBundle b;
        if (this.mPhone.isPhoneTypeGsm()) {
            boolean z = !this.mGsmRoaming ? this.mDataRoaming : true;
            log("set roaming=" + z + ",mGsmRoaming= " + this.mGsmRoaming + ",mDataRoaming= " + this.mDataRoaming);
            boolean isRoamingForSpecialSim = false;
            if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
                String simType = PhoneFactory.getPhone(this.mPhone.getPhoneId()).getIccCard().getIccCardType();
                try {
                    if (!(this.mNewSS.getOperatorNumeric() == null || getSIMOperatorNumeric() == null || simType == null || simType.equals(UsimPBMemInfo.STRING_NOT_SET))) {
                        if (simType.equals("CSIM") && this.mServiceStateExt.isRoamingForSpecialSIM(this.mNewSS.getOperatorNumeric(), getSIMOperatorNumeric())) {
                            isRoamingForSpecialSim = true;
                        }
                    }
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
            if (!isRoamingForSpecialSim) {
                if (z && isSameNamedOperators(this.mNewSS) && !isOperatorConsideredRoamingMtk(this.mNewSS)) {
                    z = false;
                }
                if (this.mPhone.isMccMncMarkedAsNonRoaming(this.mNewSS.getOperatorNumeric())) {
                    z = false;
                } else if (this.mPhone.isMccMncMarkedAsRoaming(this.mNewSS.getOperatorNumeric())) {
                    z = true;
                }
            }
            if (SubscriptionManager.isVsimEnabled(this.mPhone.getSubId())) {
                log("Vsim is Enabled, set roaming = false.");
                z = false;
            }
            if (z && OppoGsmServiceStateTracker.isNationalRoaming(this.mNewSS.getOperatorNumeric(), getSIMOperatorNumeric())) {
                z = false;
                log("vodafone national roaming,operator recognize it as not roaming ");
            }
            if (18 == this.mNewSS.getRilDataRadioTechnology()) {
                this.mNewSS.setDataRoamingFromRegistration(this.mDataRoaming);
            } else {
                this.mNewSS.setDataRoamingFromRegistration(z);
            }
            configLoader = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
            if (configLoader != null) {
                try {
                    b = configLoader.getConfigForSubId(this.mPhone.getSubId());
                    if (alwaysOnHomeNetwork(b)) {
                        log("updateRoamingState: carrier config override always on home network");
                        z = false;
                    } else if (isNonRoamingInGsmNetwork(b, this.mNewSS.getOperatorNumeric())) {
                        log("updateRoamingState: carrier config override set non roaming:" + this.mNewSS.getOperatorNumeric());
                        z = false;
                    } else if (isRoamingInGsmNetwork(b, this.mNewSS.getOperatorNumeric())) {
                        log("updateRoamingState: carrier config override set roaming:" + this.mNewSS.getOperatorNumeric());
                        z = true;
                    }
                } catch (Exception e2) {
                    loge("updateRoamingState: unable to access carrier config service");
                }
            } else {
                log("updateRoamingState: no carrier config service available");
            }
            this.mNewSS.setVoiceRoaming(z);
            if (18 == this.mNewSS.getRilDataRadioTechnology()) {
                this.mNewSS.setDataRoaming(this.mDataRoaming);
            } else {
                this.mNewSS.setDataRoaming(z);
            }
        } else {
            this.mNewSS.setDataRoamingFromRegistration(this.mNewSS.getDataRoaming());
            if (SubscriptionManager.isVsimEnabled(this.mPhone.getSubId())) {
                log("Vsim is Enabled, set roaming = false.");
                setRoamingOff();
                return;
            }
            configLoader = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
            if (configLoader != null) {
                try {
                    b = configLoader.getConfigForSubId(this.mPhone.getSubId());
                    String systemId = Integer.toString(this.mNewSS.getSystemId());
                    if (alwaysOnHomeNetwork(b)) {
                        log("updateRoamingState: carrier config override always on home network");
                        setRoamingOff();
                    } else if (isNonRoamingInGsmNetwork(b, this.mNewSS.getOperatorNumeric()) || isNonRoamingInCdmaNetwork(b, systemId)) {
                        log("updateRoamingState: carrier config override set non-roaming:" + this.mNewSS.getOperatorNumeric() + ", " + systemId);
                        setRoamingOff();
                    } else if (isRoamingInGsmNetwork(b, this.mNewSS.getOperatorNumeric()) || isRoamingInCdmaNetwork(b, systemId)) {
                        log("updateRoamingState: carrier config override set roaming:" + this.mNewSS.getOperatorNumeric() + ", " + systemId);
                        setRoamingOn();
                    }
                } catch (Exception e3) {
                    loge("updateRoamingState: unable to access carrier config service");
                }
            } else {
                log("updateRoamingState: no carrier config service available");
            }
            if (Build.IS_DEBUGGABLE && SystemProperties.getBoolean(PROP_FORCE_ROAMING, false)) {
                this.mNewSS.setVoiceRoaming(true);
                this.mNewSS.setDataRoaming(true);
            }
        }
    }

    private void setRoamingOn() {
        this.mNewSS.setVoiceRoaming(true);
        this.mNewSS.setDataRoaming(true);
        this.mNewSS.setCdmaEriIconIndex(0);
        this.mNewSS.setCdmaEriIconMode(0);
    }

    private void setRoamingOff() {
        this.mNewSS.setVoiceRoaming(false);
        this.mNewSS.setDataRoaming(false);
        this.mNewSS.setCdmaEriIconIndex(1);
    }

    public void refreshSpnDisplay() {
        String numeric = this.mSS.getOperatorNumeric();
        if (!(numeric == null || numeric.equals(UsimPBMemInfo.STRING_NOT_SET))) {
            String newAlphaLong = SpnOverride.getInstance().lookupOperatorName(SubscriptionManager.getSubIdUsingPhoneId(this.mPhone.getPhoneId()), numeric, true, this.mPhone.getContext());
            String newAlphaShort = SpnOverride.getInstance().lookupOperatorName(SubscriptionManager.getSubIdUsingPhoneId(this.mPhone.getPhoneId()), numeric, false, this.mPhone.getContext());
            if (this.mPhone.isPhoneTypeGsm() && newAlphaLong != null) {
                newAlphaLong = this.mServiceStateExt.updateOpAlphaLongForHK(newAlphaLong, numeric, this.mPhone.getPhoneId());
            }
            if (!TextUtils.equals(newAlphaLong, this.mSS.getOperatorAlphaLong())) {
                this.mNeedNotify = true;
            }
            log("refreshSpnDisplay set alpha to " + newAlphaLong + "," + newAlphaShort + "," + numeric + ", mNeedNotify=" + this.mNeedNotify);
            this.mSS.setOperatorName(newAlphaLong, newAlphaShort, numeric);
        }
        updateSpnDisplay();
    }

    protected void updateSpnDisplay() {
        updateOperatorNameFromEri();
        this.wfcVoiceSpnFormat = null;
        this.wfcDataSpnFormat = null;
        if (this.mPhone.getImsPhone() != null && this.mPhone.getImsPhone().isWifiCallingEnabled()) {
            String[] wfcSpnFormats = this.mPhone.getContext().getResources().getStringArray(17236069);
            int voiceIdx = 0;
            int dataIdx = 0;
            CarrierConfigManager configLoader = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
            if (configLoader != null) {
                try {
                    PersistableBundle b = configLoader.getConfigForSubId(this.mPhone.getSubId());
                    if (b != null) {
                        voiceIdx = b.getInt("wfc_spn_format_idx_int");
                        dataIdx = b.getInt("wfc_data_spn_format_idx_int");
                    }
                } catch (Exception e) {
                    loge("updateSpnDisplay: carrier config error: " + e);
                }
            }
            this.wfcVoiceSpnFormat = wfcSpnFormats[voiceIdx];
            this.wfcDataSpnFormat = wfcSpnFormats[dataIdx];
        }
        if (this.mPhone.isPhoneTypeGsm()) {
            updateSpnDisplayGsm(false);
        } else {
            updateSpnDisplayCdma(false);
        }
    }

    /* JADX WARNING: Missing block: B:225:0x0790, code:
            if (r44 == false) goto L_0x044e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void updateSpnDisplayGsm(boolean forceUpdate) {
        int rule;
        String str;
        Object[] objArr;
        SIMRecords simRecords = null;
        IccRecords r = (IccRecords) this.mPhone.mIccRecords.get();
        if (r != null) {
            simRecords = (SIMRecords) r;
        }
        if (simRecords != null) {
            rule = simRecords.getDisplayRule(this.mSS.getOperatorNumeric());
        } else {
            rule = 2;
        }
        String strNumPlmn = this.mSS.getOperatorNumeric();
        String spn = simRecords != null ? simRecords.getServiceProviderName() : UsimPBMemInfo.STRING_NOT_SET;
        String sEons = null;
        boolean showPlmn = false;
        String plmn = null;
        String mSimOperatorNumeric = simRecords != null ? simRecords.getOperatorNumeric() : UsimPBMemInfo.STRING_NOT_SET;
        if (simRecords != null) {
            try {
                sEons = simRecords.getEonsIfExist(this.mSS.getOperatorNumeric(), ((GsmCellLocation) this.mCellLoc).getLac(), true);
            } catch (RuntimeException ex) {
                loge("Exception while getEonsIfExist. " + ex);
            }
        } else {
            sEons = null;
        }
        if (sEons != null) {
            plmn = sEons;
        } else if (strNumPlmn != null && strNumPlmn.equals(mSimOperatorNumeric)) {
            log("Home PLMN, get CPHS ons");
            plmn = simRecords != null ? simRecords.getSIMCPHSOns() : UsimPBMemInfo.STRING_NOT_SET;
        }
        if (TextUtils.isEmpty(plmn)) {
            log("No matched EONS and No CPHS ONS");
            plmn = this.mSS.getOperatorAlphaLong();
            if (TextUtils.isEmpty(plmn) || plmn.equals(this.mSS.getOperatorNumeric())) {
                plmn = this.mSS.getOperatorAlphaShort();
            }
        }
        if (sEons == null || !(OppoGsmServiceStateTracker.isGT4GSimCardCheck(mSimOperatorNumeric) || OppoGsmServiceStateTracker.isNationalRoaming(strNumPlmn, mSimOperatorNumeric) || "50501".equals(strNumPlmn) || "50502".equals(strNumPlmn))) {
            String languageName = OppoGsmServiceStateTracker.oppoGetPlmnOverride(this.mPhone.getContext(), this.mSS.getOperatorNumeric(), this.mSS);
            log("updateSpnDisplay: languageName = " + languageName);
            if (!TextUtils.isEmpty(languageName)) {
                if (!languageName.equals(this.mSS.getOperatorNumeric())) {
                    plmn = languageName;
                }
            }
        } else {
            log("TW GT or NationalRoaming case, don't process language name");
        }
        CharSequence realPlmn = plmn;
        if (!(this.mSS.getVoiceRegState() == 0 || this.mSS.getDataRegState() == 0)) {
            showPlmn = true;
            plmn = Resources.getSystem().getText(17041017).toString();
        }
        if (this.mSS.getVoiceRegState() == 0 || this.mSS.getDataRegState() == 0) {
            if (TextUtils.isEmpty(plmn)) {
                showPlmn = false;
            } else if ((rule & 2) == 2) {
                showPlmn = true;
            } else {
                showPlmn = false;
            }
        }
        String dataSpn = spn;
        boolean showSpn = !TextUtils.isEmpty(spn) ? (rule & 1) == 1 : false;
        if (!TextUtils.isEmpty(spn) && !TextUtils.isEmpty(this.wfcVoiceSpnFormat) && !TextUtils.isEmpty(this.wfcDataSpnFormat)) {
            String originalSpn = spn.trim();
            str = this.wfcVoiceSpnFormat;
            objArr = new Object[1];
            objArr[0] = originalSpn;
            spn = String.format(str, objArr);
            str = this.wfcDataSpnFormat;
            objArr = new Object[1];
            objArr[0] = originalSpn;
            dataSpn = String.format(str, objArr);
            showSpn = true;
            showPlmn = false;
        } else if (this.mSS.getVoiceRegState() == 3 || ((showPlmn && TextUtils.equals(spn, plmn)) || !(this.mSS.getVoiceRegState() == 0 || this.mSS.getDataRegState() == 0))) {
            spn = null;
            showSpn = false;
        }
        if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
            try {
                if (this.mServiceStateExt.needSpnRuleShowPlmnOnly() && !TextUtils.isEmpty(plmn)) {
                    log("origin showSpn:" + showSpn + " showPlmn:" + showPlmn + " rule:" + rule);
                    showSpn = false;
                    showPlmn = true;
                    rule = 2;
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        try {
            plmn = this.mServiceStateExt.onUpdateSpnDisplayForIms(plmn, this.mSS, ((GsmCellLocation) this.mCellLoc).getLac(), this.mPhone.getPhoneId(), simRecords);
        } catch (RuntimeException e2) {
            e2.printStackTrace();
        }
        int subId = -1;
        int[] subIds = SubscriptionManager.getSubId(this.mPhone.getPhoneId());
        if (subIds != null && subIds.length > 0) {
            subId = subIds[0];
        }
        String simNumeric = getSystemProperty("gsm.sim.operator.numeric", UsimPBMemInfo.STRING_NOT_SET);
        plmn = OppoGsmServiceStateTracker.oppoExpDisplayFormatting(this.mSS, plmn, simNumeric);
        spn = OppoGsmServiceStateTracker.oppoExpDisplayFormatting(this.mSS, spn, simNumeric);
        if (OppoGsmServiceStateTracker.isAUOperatorCheck(mSimOperatorNumeric) && !TextUtils.isEmpty(spn)) {
            spn = spn.replaceAll("[\\n\\r]", " ");
        }
        if (this.mSubId == subId && showPlmn == this.mCurShowPlmn && showSpn == this.mCurShowSpn && TextUtils.equals(spn, this.mCurSpn)) {
            if (TextUtils.equals(dataSpn, this.mCurDataSpn)) {
                if (TextUtils.equals(plmn, this.mCurPlmn)) {
                }
            }
        }
        if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
            try {
                if (!this.mServiceStateExt.allowSpnDisplayed()) {
                    log("For CT test case don't show SPN.");
                    if (rule == 3) {
                        showSpn = false;
                        spn = null;
                    }
                }
            } catch (RuntimeException e22) {
                e22.printStackTrace();
            }
        }
        oppoVirtualSimCheck(this.mSS.getOperatorNumeric(), plmn, spn, showPlmn, showSpn);
        showPlmn = this.mShowPlmn;
        showSpn = this.mShowSPn;
        boolean[] showRules = OppoGsmServiceStateTracker.oppoExpDisplayRules(this.mSS, plmn, spn, simNumeric);
        if (showRules[0] || showRules[1]) {
            showPlmn = showRules[0];
            showSpn = showRules[1];
        }
        if (showPlmn && !TextUtils.isEmpty(plmn)) {
            oppoSetOperatorAlpha(plmn);
        } else if (showSpn && !TextUtils.isEmpty(spn)) {
            oppoSetOperatorAlpha(spn);
        }
        if (this.mSubscriptionController.isHasSoftSimCard() && this.mSubscriptionController.getSoftSimCardSlotId() == getPhoneId() && !TextUtils.isEmpty(spn)) {
            log("Softsim should display spn, so set showSpn to true.");
            showPlmn = false;
            showSpn = true;
            spn = this.mPhone.getContext().getString(17041014);
        }
        if (OppoGsmServiceStateTracker.oppoIsNZSimCheck(getSIMOperatorNumeric()) && OppoGsmServiceStateTracker.oppoIsNZOperatorCheck(this.mSS.getOperatorNumeric()) && !TextUtils.isEmpty(spn)) {
            showPlmn = false;
            showSpn = true;
            oppoSetOperatorAlpha(spn);
            log("2Degress shows spn when national roaming");
        }
        if (DBG) {
            str = "updateSpnDisplay: changed sending intent rule=" + rule + " showPlmn='%b' plmn='%s' showSpn='%b' spn='%s' dataSpn='%s' " + "subId='%d'";
            objArr = new Object[6];
            objArr[0] = Boolean.valueOf(showPlmn);
            objArr[1] = plmn;
            objArr[2] = Boolean.valueOf(showSpn);
            objArr[3] = spn;
            objArr[4] = dataSpn;
            objArr[5] = Integer.valueOf(subId);
            log(String.format(str, objArr));
        }
        Intent intent = new Intent(WapPush.SPN_STRINGS_UPDATED_ACTION);
        if (TelephonyManager.getDefault().getPhoneCount() == 1) {
            intent.addFlags(536870912);
        }
        intent.putExtra(WapPush.EXTRA_SHOW_SPN, showSpn);
        intent.putExtra("spn", spn);
        intent.putExtra("spnData", dataSpn);
        intent.putExtra(WapPush.EXTRA_SHOW_PLMN, showPlmn);
        intent.putExtra("plmn", plmn);
        intent.putExtra("hnbName", this.mHhbName);
        intent.putExtra("csgId", this.mCsgId);
        intent.putExtra("domain", this.mFemtocellDomain);
        intent.putExtra("femtocell", this.mIsFemtocell);
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
        this.mPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        int phoneId = this.mPhone.getPhoneId();
        if (SystemProperties.get("ro.mtk_femto_cell_support").equals("1")) {
            if (this.mHhbName != null || this.mCsgId == null) {
                if (this.mHhbName != null) {
                    plmn = (plmn + " - ") + this.mHhbName;
                }
            } else if (SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
                plmn = (plmn + " - ") + this.mCsgId;
            } else {
                try {
                    if (this.mServiceStateExt.needToShowCsgId()) {
                        plmn = (plmn + " - ") + this.mCsgId;
                    }
                } catch (RuntimeException e222) {
                    e222.printStackTrace();
                }
            }
        }
        boolean setResult = this.mSubscriptionController.setPlmnSpn(phoneId, showPlmn, plmn, showSpn, spn);
        if (!setResult) {
            this.mSpnUpdatePending = true;
        }
        log("showSpn:" + showSpn + " spn:" + spn + " showPlmn:" + showPlmn + " plmn:" + plmn + " rule:" + rule + " setResult:" + setResult + " phoneId:" + phoneId);
        String operatorLong = this.mSS.getOperatorAlphaLong();
        if (!showSpn || showPlmn || spn == null) {
            if (operatorLong == null || !operatorLong.equals(realPlmn)) {
                this.mSS.setOperatorAlphaLong(realPlmn);
                this.mNeedNotify = true;
            }
            log("updateAllOpertorInfo with realPlmn:" + realPlmn + ", mNeedNotify=" + this.mNeedNotify);
            updateOperatorAlpha(realPlmn);
        } else {
            if (operatorLong == null || !operatorLong.equals(spn)) {
                this.mSS.setOperatorAlphaLong(spn);
                this.mNeedNotify = true;
            }
            log("updateAllOpertorInfo with spn:" + spn + ", mNeedNotify=" + this.mNeedNotify);
            updateOperatorAlpha(spn);
        }
        this.mSubId = subId;
        this.mCurShowSpn = showSpn;
        this.mCurShowPlmn = showPlmn;
        this.mCurSpn = spn;
        this.mCurDataSpn = dataSpn;
        this.mCurPlmn = plmn;
        SubscriptionController s = SubscriptionController.getInstance();
        boolean isInService = this.mSS.getState() == 0;
        SubscriptionManager.from(this.mPhone.getContext());
        boolean isActivePhone = SubscriptionManager.getSubState(this.mPhone.getSubId()) == 1;
        long now = System.currentTimeMillis() / 1000;
        if (DBG) {
            log("[POWERSTATE]mBeginNoServiceTime:" + mBeginNoServiceTime[this.mPhone.getPhoneId()] + ",mNoServiceTime:" + mNoServiceTime + ",isInService:" + isInService + ",isActivePhone:" + isActivePhone);
        }
        if (this.mPhone.getPhoneId() < 2 && isActivePhone && isInService && mBeginNoServiceTime[this.mPhone.getPhoneId()] != 0) {
            if (!(this.mScreenOffTime == 0 || this.mIsScreenOn)) {
                if (mBeginNoServiceTime[this.mPhone.getPhoneId()] < this.mScreenOffTime) {
                    mBeginNoServiceTime[this.mPhone.getPhoneId()] = this.mScreenOffTime;
                    if (DBG) {
                        log("[POWERSTATE]set mBeginNoServiceTime=mScreenOffTime:" + mBeginNoServiceTime[this.mPhone.getPhoneId()]);
                    }
                }
                mNoServiceTime += now - mBeginNoServiceTime[this.mPhone.getPhoneId()];
            }
            if (DBG) {
                log("[POWERSTATE]set mNoServiceTime:" + mNoServiceTime + ",set mBeginNoServiceTime = 0");
            }
            mBeginNoServiceTime[this.mPhone.getPhoneId()] = 0;
        }
        if (this.mPhone.getPhoneId() < 2 && isActivePhone) {
            if (!isInService && mBeginNoServiceTime[this.mPhone.getPhoneId()] == 0) {
                mBeginNoServiceTime[this.mPhone.getPhoneId()] = now;
            } else if (isInService) {
                mBeginNoServiceTime[this.mPhone.getPhoneId()] = 0;
            }
        }
        if (DBG) {
            log("[POWERSTATE]updateSpnDisplay: showPlmn:" + this.mCurShowPlmn + " plmn:" + this.mCurPlmn + " showSpn:" + this.mCurShowSpn + " spn:" + this.mCurSpn + " subId:" + this.mSubId + ",set mBeginNoServiceTime_phone0:" + mBeginNoServiceTime[0] + "mBeginNoServiceTime_phone1:" + mBeginNoServiceTime[1]);
        }
    }

    private void updateSpnDisplayCdma(boolean forceUpdate) {
        Object[] objArr;
        String plmn = OppoGsmServiceStateTracker.oppoGetPlmnOverride(this.mPhone.getContext(), this.mSS.getOperatorNumeric(), this.mSS);
        boolean showPlmn = plmn != null;
        int subId = -1;
        int[] subIds = SubscriptionManager.getSubId(this.mPhone.getPhoneId());
        if (subIds != null && subIds.length > 0) {
            subId = subIds[0];
        }
        if (!TextUtils.isEmpty(plmn) && !TextUtils.isEmpty(this.wfcVoiceSpnFormat)) {
            String originalPlmn = plmn.trim();
            String str = this.wfcVoiceSpnFormat;
            objArr = new Object[1];
            objArr[0] = originalPlmn;
            plmn = String.format(str, objArr);
        } else if (this.mCi.getRadioState() == RadioState.RADIO_OFF) {
            log("updateSpnDisplay: overwriting plmn from " + plmn + " to null as radio " + "state is off");
            plmn = null;
        }
        if (plmn == null || plmn.equals(UsimPBMemInfo.STRING_NOT_SET)) {
            plmn = this.mSS.getOperatorAlphaLong();
            if (plmn == null || plmn.equals(this.mSS.getOperatorNumeric())) {
                plmn = this.mSS.getOperatorAlphaShort();
            }
        }
        if (plmn != null) {
            showPlmn = true;
            if (plmn.equals(UsimPBMemInfo.STRING_NOT_SET)) {
                plmn = null;
            }
        }
        log("[CDMA]updateSpnDisplay: getOperatorAlphaLong=" + this.mSS.getOperatorAlphaLong() + ", getOperatorAlphaShort=" + this.mSS.getOperatorAlphaShort() + ", plmn=" + plmn + ", forceUpdate=" + forceUpdate);
        if (!(this.mSS.getState() == 0 || this.mSS.getDataRegState() == 0)) {
            log("[CDMA]updateSpnDisplay: Do not display SPN before get normal service");
            showPlmn = true;
            plmn = Resources.getSystem().getText(17041017).toString();
        }
        String spn = UsimPBMemInfo.STRING_NOT_SET;
        boolean showSpn = false;
        if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
            try {
                if (this.mServiceStateExt.allowSpnDisplayed()) {
                    int rule;
                    IccRecords r = (IccRecords) this.mPhone.mIccRecords.get();
                    if (r != null) {
                        rule = r.getDisplayRule(this.mSS.getOperatorNumeric());
                    } else {
                        rule = 2;
                    }
                    spn = r != null ? r.getServiceProviderName() : UsimPBMemInfo.STRING_NOT_SET;
                    showSpn = (TextUtils.isEmpty(spn) || (rule & 1) != 1 || this.mSS.getVoiceRegState() == 3) ? false : !this.mSS.getRoaming();
                    log("[CDMA]updateSpnDisplay: rule=" + rule + ", spn=" + spn + ", showSpn=" + showSpn);
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        if (!(this.mSubId == subId && showPlmn == this.mCurShowPlmn && showSpn == this.mCurShowSpn && TextUtils.equals(spn, this.mCurSpn) && TextUtils.equals(plmn, this.mCurPlmn) && !forceUpdate)) {
            showPlmn = plmn != null;
            if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
                try {
                    if (this.mServiceStateExt.allowSpnDisplayed()) {
                        if (!(this.mSS.getVoiceRegState() == 3 || this.mSS.getVoiceRegState() == 1)) {
                            if (!(this.mSS.getRoaming() || spn == null || spn.equals(UsimPBMemInfo.STRING_NOT_SET))) {
                                showPlmn = false;
                            }
                        }
                        showPlmn = true;
                    }
                } catch (RuntimeException e2) {
                    e2.printStackTrace();
                }
            }
            oppoVirtualSimCheck(UsimPBMemInfo.STRING_NOT_SET, plmn, UsimPBMemInfo.STRING_NOT_SET, showPlmn, false);
            showPlmn = this.mShowPlmn;
            showSpn = this.mShowSPn;
            if (DBG) {
                objArr = new Object[5];
                objArr[0] = Integer.valueOf(subId);
                objArr[1] = Boolean.valueOf(showPlmn);
                objArr[2] = plmn;
                objArr[3] = Boolean.valueOf(showSpn);
                objArr[4] = spn;
                log(String.format("[CDMA]updateSpnDisplay: changed sending intent subId='%d' showPlmn='%b' plmn='%s' showSpn='%b' spn='%s'", objArr));
            }
            Intent intent = new Intent(WapPush.SPN_STRINGS_UPDATED_ACTION);
            if (TelephonyManager.getDefault().getPhoneCount() == 1) {
                intent.addFlags(536870912);
            }
            intent.putExtra(WapPush.EXTRA_SHOW_SPN, showSpn);
            intent.putExtra("spn", spn);
            intent.putExtra(WapPush.EXTRA_SHOW_PLMN, showPlmn);
            intent.putExtra("plmn", plmn);
            intent.putExtra("hnbName", (String) null);
            intent.putExtra("csgId", (String) null);
            intent.putExtra("domain", 0);
            intent.putExtra("femtocell", this.mIsFemtocell);
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
            this.mPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            boolean setResult = this.mSubscriptionController.setPlmnSpn(this.mPhone.getPhoneId(), showPlmn, plmn, showSpn, spn);
            if (!setResult) {
                this.mSpnUpdatePending = true;
            }
            log("[CDMA]updateSpnDisplay: subId=" + subId + ", showPlmn=" + showPlmn + ", plmn=" + plmn + ", showSpn=" + showSpn + ", spn=" + spn + ", setResult=" + setResult + ", mSpnUpdatePending=" + this.mSpnUpdatePending);
        }
        this.mSubId = subId;
        this.mCurShowSpn = showSpn;
        this.mCurShowPlmn = showPlmn;
        this.mCurSpn = spn;
        this.mCurPlmn = plmn;
        SubscriptionController s = SubscriptionController.getInstance();
        boolean isInService = this.mSS.getState() == 0;
        SubscriptionManager.from(this.mPhone.getContext());
        boolean isActivePhone = SubscriptionManager.getSubState(this.mPhone.getSubId()) == 1;
        long now = System.currentTimeMillis() / 1000;
        if (DBG) {
            log("[POWERSTATE]mBeginNoServiceTime:" + mBeginNoServiceTime[this.mPhone.getPhoneId()] + ",mNoServiceTime:" + mNoServiceTime + ",isInService:" + isInService + ",isActivePhone:" + isActivePhone);
        }
        if (this.mPhone.getPhoneId() < 2 && isActivePhone && isInService && mBeginNoServiceTime[this.mPhone.getPhoneId()] != 0) {
            if (!(this.mScreenOffTime == 0 || this.mIsScreenOn)) {
                if (mBeginNoServiceTime[this.mPhone.getPhoneId()] < this.mScreenOffTime) {
                    mBeginNoServiceTime[this.mPhone.getPhoneId()] = this.mScreenOffTime;
                    if (DBG) {
                        log("[POWERSTATE]set mBeginNoServiceTime=mScreenOffTime:" + mBeginNoServiceTime[this.mPhone.getPhoneId()]);
                    }
                }
                mNoServiceTime += now - mBeginNoServiceTime[this.mPhone.getPhoneId()];
            }
            if (DBG) {
                log("[POWERSTATE]set mNoServiceTime:" + mNoServiceTime + ",set mBeginNoServiceTime = 0");
            }
            mBeginNoServiceTime[this.mPhone.getPhoneId()] = 0;
        }
        if (this.mPhone.getPhoneId() < 2 && isActivePhone) {
            if (!isInService && mBeginNoServiceTime[this.mPhone.getPhoneId()] == 0) {
                mBeginNoServiceTime[this.mPhone.getPhoneId()] = now;
            } else if (isInService) {
                mBeginNoServiceTime[this.mPhone.getPhoneId()] = 0;
            }
        }
        if (DBG) {
            log("[POWERSTATE]updateSpnDisplay: showPlmn:" + this.mCurShowPlmn + " plmn:" + this.mCurPlmn + " showSpn:" + this.mCurShowSpn + " spn:" + this.mCurSpn + " subId:" + this.mSubId + ",set mBeginNoServiceTime_phone0:" + mBeginNoServiceTime[0] + "mBeginNoServiceTime_phone1:" + mBeginNoServiceTime[1]);
        }
    }

    protected void setPowerStateToDesired() {
        if (DBG) {
            log("mDeviceShuttingDown=" + this.mDeviceShuttingDown + ", mDesiredPowerState=" + this.mDesiredPowerState + ", getRadioState=" + this.mCi.getRadioState() + ", mPowerOffDelayNeed=" + this.mPowerOffDelayNeed + ", mAlarmSwitch=" + this.mAlarmSwitch + ", mRadioDisabledByCarrier=" + this.mRadioDisabledByCarrier);
        }
        if (this.mPhone.isPhoneTypeGsm() && this.mAlarmSwitch) {
            if (DBG) {
                log("mAlarmSwitch == true");
            }
            ((AlarmManager) this.mPhone.getContext().getSystemService("alarm")).cancel(this.mRadioOffIntent);
            this.mAlarmSwitch = false;
        }
        if (this.mDesiredPowerState && !this.mRadioDisabledByCarrier && this.mCi.getRadioState() == RadioState.RADIO_OFF) {
            if (this.mPhone.isPhoneTypeGsm()) {
                setDeviceRatMode(this.mPhone.getPhoneId());
            }
            RadioManager.getInstance();
            RadioManager.sendRequestBeforeSetRadioPower(true, this.mPhone.getPhoneId());
            this.mCi.setRadioPower(true, null);
            this.mOppoNeedNotify = true;
        } else if ((!this.mDesiredPowerState || this.mRadioDisabledByCarrier) && this.mCi.getRadioState().isOn()) {
            if (!this.mPhone.isPhoneTypeGsm() || !this.mPowerOffDelayNeed) {
                powerOffRadioSafely(this.mPhone.mDcTracker);
            } else if (!this.mImsRegistrationOnOff || this.mAlarmSwitch) {
                powerOffRadioSafely(this.mPhone.mDcTracker);
            } else {
                if (DBG) {
                    log("mImsRegistrationOnOff == true");
                }
                Context context = this.mPhone.getContext();
                AlarmManager am = (AlarmManager) context.getSystemService("alarm");
                this.mRadioOffIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_RADIO_OFF), 0);
                this.mAlarmSwitch = true;
                if (DBG) {
                    log("Alarm setting");
                }
                am.set(2, SystemClock.elapsedRealtime() + 3000, this.mRadioOffIntent);
            }
        } else if (this.mDeviceShuttingDown && this.mCi.getRadioState().isAvailable()) {
            this.mCi.requestShutdown(null);
        }
    }

    protected void onUpdateIccAvailability() {
        if (this.mUiccController != null) {
            log("reset ps_invlaid !=1 when update gsm icccard ");
            SystemProperties.set("gsm.sim.invalidpssim", "1000");
            UiccCardApplication newUiccApplication = getUiccCardApplication();
            if ((this.mPhone.isPhoneTypeCdma() || this.mPhone.isPhoneTypeCdmaLte()) && newUiccApplication != null) {
                AppState appState = newUiccApplication.getState();
                if ((appState == AppState.APPSTATE_PIN || appState == AppState.APPSTATE_PUK) && this.mNetworkExsit) {
                    this.mEmergencyOnly = true;
                } else {
                    this.mEmergencyOnly = false;
                }
                log("[CDMA]onUpdateIccAvailability, appstate=" + appState + ", mNetworkExsit=" + this.mNetworkExsit + ", mEmergencyOnly=" + this.mEmergencyOnly);
            }
            if (this.mUiccApplcation != newUiccApplication) {
                if (this.mUiccApplcation != null) {
                    log("Removing stale icc objects.");
                    this.mUiccApplcation.unregisterForReady(this);
                    if (this.mIccRecords != null) {
                        this.mIccRecords.unregisterForRecordsLoaded(this);
                        if (this.mPhone.isPhoneTypeGsm()) {
                            this.mIccRecords.unregisterForRecordsEvents(this);
                        }
                    }
                    this.mIccRecords = null;
                    this.mUiccApplcation = null;
                    this.isRemoveCard = true;
                }
                if (newUiccApplication != null) {
                    log("New card found");
                    this.mUiccApplcation = newUiccApplication;
                    this.mIccRecords = this.mUiccApplcation.getIccRecords();
                    if (this.mPhone.isPhoneTypeGsm()) {
                        this.mUiccApplcation.registerForReady(this, 17, null);
                        if (this.mIccRecords != null) {
                            this.mIccRecords.registerForRecordsLoaded(this, 16, null);
                            this.mIccRecords.registerForRecordsEvents(this, 120, null);
                        }
                    } else if (this.mIsSubscriptionFromRuim) {
                        this.mUiccApplcation.registerForReady(this, 26, null);
                        if (this.mIccRecords != null) {
                            this.mIccRecords.registerForRecordsLoaded(this, 27, null);
                        }
                    }
                    this.isRemoveCard = false;
                }
            }
        }
    }

    protected void logd(String s) {
        if (!mEngLoad && mLogLv <= 0) {
            return;
        }
        if (this.mPhone.isPhoneTypeGsm()) {
            Rlog.d(LOG_TAG, "[GsmSST" + this.mPhone.getPhoneId() + "] " + s);
        } else if (this.mPhone.isPhoneTypeCdma()) {
            Rlog.d(LOG_TAG, "[CdmaSST" + this.mPhone.getPhoneId() + "] " + s);
        } else {
            Rlog.d(LOG_TAG, "[CdmaLteSST" + this.mPhone.getPhoneId() + "] " + s);
        }
    }

    private void logRoamingChange() {
        this.mRoamingLog.log(this.mSS.toString());
    }

    private void logAttachChange() {
        this.mAttachLog.log(this.mSS.toString());
    }

    private void logPhoneTypeChange() {
        this.mPhoneTypeLog.log(Integer.toString(this.mPhone.getPhoneType()));
    }

    private void logRatChange() {
        this.mRatLog.log(this.mSS.toString());
    }

    protected void log(String s) {
        if (this.mPhone.isPhoneTypeGsm()) {
            Rlog.d(LOG_TAG, "[GsmSST" + this.mPhone.getPhoneId() + "] " + s);
        } else if (this.mPhone.isPhoneTypeCdma()) {
            Rlog.d(LOG_TAG, "[CdmaSST" + this.mPhone.getPhoneId() + "] " + s);
        } else {
            Rlog.d(LOG_TAG, "[CdmaLteSST" + this.mPhone.getPhoneId() + "] " + s);
        }
    }

    protected void loge(String s) {
        if (this.mPhone.isPhoneTypeGsm()) {
            Rlog.e(LOG_TAG, "[GsmSST" + this.mPhone.getPhoneId() + "] " + s);
        } else if (this.mPhone.isPhoneTypeCdma()) {
            Rlog.e(LOG_TAG, "[CdmaSST" + this.mPhone.getPhoneId() + "] " + s);
        } else {
            Rlog.e(LOG_TAG, "[CdmaLteSST" + this.mPhone.getPhoneId() + "] " + s);
        }
    }

    public int getCurrentDataConnectionState() {
        return this.mSS.getDataRegState();
    }

    public boolean isConcurrentVoiceAndDataAllowed() {
        boolean z = true;
        if (this.mPhone.isPhoneTypeGsm()) {
            boolean isAllowed = false;
            if (this.mSS.isVoiceRadioTechnologyHigher(3) || this.mSS.getRilVoiceRadioTechnology() == 3) {
                isAllowed = true;
            } else if (isConcurrentVoiceAndDataAllowedForVolte()) {
                isAllowed = true;
            }
            if (DBG) {
                log("isConcurrentVoiceAndDataAllowed(): " + isAllowed);
            }
            return isAllowed;
        } else if (this.mPhone.isPhoneTypeCdma()) {
            return false;
        } else {
            if ((SystemProperties.getInt("ro.boot.opt_c2k_lte_mode", 0) == 1 && this.mSS.getRilDataRadioTechnology() == 14) || isConcurrentVoiceAndDataAllowedForVolte()) {
                return true;
            }
            if (this.mSS.getCssIndicator() != 1) {
                z = false;
            }
            return z;
        }
    }

    public void setImsRegistrationState(boolean registered) {
        log("ImsRegistrationState - registered : " + registered);
        if (this.mImsRegistrationOnOff && !registered && this.mAlarmSwitch) {
            this.mImsRegistrationOnOff = registered;
            ((AlarmManager) this.mPhone.getContext().getSystemService("alarm")).cancel(this.mRadioOffIntent);
            this.mAlarmSwitch = false;
            sendMessage(obtainMessage(45));
            return;
        }
        this.mImsRegistrationOnOff = registered;
    }

    public void onImsCapabilityChanged() {
        sendMessage(obtainMessage(EVENT_IMS_CAPABILITY_CHANGED));
    }

    public boolean isRadioOn() {
        return this.mCi.getRadioState() == RadioState.RADIO_ON;
    }

    private void onNetworkStateChangeResult(AsyncResult ar) {
        int lac = -1;
        int cid = -1;
        int Act = -1;
        int cause = -1;
        if (ar.exception != null || ar.result == null) {
            loge("onNetworkStateChangeResult exception");
        } else {
            String[] info = ar.result;
            if (info.length > 0) {
                int state = Integer.parseInt(info[0]);
                if (info[1] != null && info[1].length() > 0) {
                    lac = Integer.parseInt(info[1], 16);
                }
                if (info[2] != null && info[2].length() > 0) {
                    if (info[2].equals("FFFFFFFF") || info[2].equals("ffffffff")) {
                        log("Invalid cid:" + info[2]);
                        info[2] = "0000ffff";
                    }
                    cid = Integer.parseInt(info[2], 16);
                }
                if (info[3] != null && info[3].length() > 0) {
                    Act = Integer.parseInt(info[3]);
                }
                if (info[4] != null && info[4].length() > 0) {
                    cause = Integer.parseInt(info[4]);
                }
                log("onNetworkStateChangeResult state:" + state + " lac:" + lac + " cid:" + cid + " Act:" + Act + " cause:" + cause);
                if (Act == 7) {
                    this.voiceUrcWith4G = true;
                } else {
                    this.voiceUrcWith4G = false;
                }
                if (!SystemProperties.get("ro.mtk_bsp_package").equals("1") && SystemProperties.get("ro.mtk_md_world_mode_support").equals("0")) {
                    try {
                        if (this.mServiceStateExt.needIgnoredState(this.mSS.getVoiceRegState(), state, cause)) {
                            log("onNetworkStateChangeResult isCsInvalidCard:" + this.isCsInvalidCard);
                            if (!this.isCsInvalidCard) {
                                if (!this.dontUpdateNetworkStateFlag) {
                                    broadcastHideNetworkState("start", 1);
                                }
                                this.dontUpdateNetworkStateFlag = true;
                            }
                            return;
                        }
                        if (this.dontUpdateNetworkStateFlag) {
                            broadcastHideNetworkState("stop", 1);
                        }
                        this.dontUpdateNetworkStateFlag = false;
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }
                if (!(lac == -1 || cid == -1 || regCodeToServiceState(state) != 1)) {
                    if (lac == 65534 || cid == 268435455) {
                        logd("unknown lac:" + lac + " or cid:" + cid);
                    } else {
                        logd("mNewCellLoc Updated, lac:" + lac + " and cid:" + cid);
                        ((GsmCellLocation) this.mNewCellLoc).setLacAndCid(lac, cid);
                    }
                }
                if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
                    try {
                        if (this.mServiceStateExt.needRejectCauseNotification(cause)) {
                            setRejectCauseNotification(cause);
                        }
                    } catch (RuntimeException e2) {
                        e2.printStackTrace();
                    }
                }
            } else {
                logd("onNetworkStateChangeResult length zero");
            }
        }
    }

    public void setEverIVSR(boolean value) {
        log("setEverIVSR:" + value);
        this.mEverIVSR = value;
        if (value) {
            Intent intent = new Intent("mediatek.intent.action.IVSR_NOTIFY");
            intent.putExtra("action", "start");
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
            if (TelephonyManager.getDefault().getPhoneCount() == 1) {
                intent.addFlags(536870912);
            }
            log("broadcast ACTION_IVSR_NOTIFY intent");
            this.mPhone.getContext().sendBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    public String getLocatedPlmn() {
        return this.mLocatedPlmn;
    }

    private void updateLocatedPlmn(String plmn) {
        logd("updateLocatedPlmn(),previous plmn= " + this.mLocatedPlmn + " ,update to: " + plmn);
        if ((this.mLocatedPlmn == null && plmn != null) || ((this.mLocatedPlmn != null && plmn == null) || !(this.mLocatedPlmn == null || plmn == null || this.mLocatedPlmn.equals(plmn)))) {
            Intent intent = new Intent("mediatek.intent.action.LOCATED_PLMN_CHANGED");
            if (TelephonyManager.getDefault().getPhoneCount() == 1) {
                intent.addFlags(536870912);
            }
            intent.putExtra("plmn", plmn);
            if (plmn != null) {
                try {
                    intent.putExtra("iso", MccTable.countryCodeForMcc(Integer.parseInt(plmn.substring(0, 3))));
                } catch (NumberFormatException ex) {
                    loge("updateLocatedPlmn: countryCodeForMcc error" + ex);
                    intent.putExtra("iso", UsimPBMemInfo.STRING_NOT_SET);
                } catch (StringIndexOutOfBoundsException ex2) {
                    loge("updateLocatedPlmn: countryCodeForMcc error" + ex2);
                    intent.putExtra("iso", UsimPBMemInfo.STRING_NOT_SET);
                }
                if (SystemProperties.get(PROPERTY_AUTO_RAT_SWITCH).equals("0")) {
                    loge("updateLocatedPlmn: framework auto RAT switch disabled");
                } else {
                    this.mLocatedPlmn = plmn;
                    setDeviceRatMode(this.mPhone.getPhoneId());
                }
            } else {
                intent.putExtra("iso", UsimPBMemInfo.STRING_NOT_SET);
            }
            broadcastMccChange(plmn);
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
            this.mPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        }
        this.mLocatedPlmn = plmn;
    }

    private void onFemtoCellInfoResult(AsyncResult ar) {
        int isCsgCell = 0;
        if (ar.exception != null || ar.result == null) {
            loge("onFemtoCellInfo exception");
        } else {
            String[] info = ar.result;
            if (info.length > 0) {
                if (info[0] != null && info[0].length() > 0) {
                    this.mFemtocellDomain = Integer.parseInt(info[0]);
                    log("onFemtoCellInfo: mFemtocellDomain set to " + this.mFemtocellDomain);
                }
                if (info[5] != null && info[5].length() > 0) {
                    isCsgCell = Integer.parseInt(info[5]);
                }
                this.mIsFemtocell = isCsgCell;
                log("onFemtoCellInfo: domain= " + this.mFemtocellDomain + ",isCsgCell= " + isCsgCell);
                if (isCsgCell == 1) {
                    if (info[6] != null && info[6].length() > 0) {
                        this.mCsgId = info[6];
                        log("onFemtoCellInfo: mCsgId set to " + this.mCsgId);
                    }
                    if (info[8] == null || info[8].length() <= 0) {
                        this.mHhbName = null;
                        log("onFemtoCellInfo: mHhbName is not available ,set to null");
                    } else {
                        this.mHhbName = new String(IccUtils.hexStringToBytes(info[8]));
                        log("onFemtoCellInfo: mHhbName set from " + info[8] + " to " + this.mHhbName);
                    }
                } else {
                    this.mCsgId = null;
                    this.mHhbName = null;
                    log("onFemtoCellInfo: csgId and hnbName are cleared");
                }
                if (!(isCsgCell == 2 || info[1] == null || info[1].length() <= 0 || info[9] == null || info[0].length() <= 0)) {
                    int state = Integer.parseInt(info[1]);
                    int cause = Integer.parseInt(info[9]);
                    if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
                        try {
                            if (this.mServiceStateExt.needIgnoreFemtocellUpdate(state, cause)) {
                                log("needIgnoreFemtocellUpdate due to state= " + state + ",cause= " + cause);
                                return;
                            }
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                    }
                }
                Intent intent = new Intent(WapPush.SPN_STRINGS_UPDATED_ACTION);
                SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
                if (TelephonyManager.getDefault().getPhoneCount() == 1) {
                    intent.addFlags(536870912);
                }
                intent.putExtra(WapPush.EXTRA_SHOW_SPN, this.mCurShowSpn);
                intent.putExtra("spn", this.mCurSpn);
                intent.putExtra(WapPush.EXTRA_SHOW_PLMN, this.mCurShowPlmn);
                intent.putExtra("plmn", this.mCurPlmn);
                intent.putExtra("hnbName", this.mHhbName);
                intent.putExtra("csgId", this.mCsgId);
                intent.putExtra("domain", this.mFemtocellDomain);
                intent.putExtra("femtocell", this.mIsFemtocell);
                this.mPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
                int phoneId = this.mPhone.getPhoneId();
                String plmn = this.mCurPlmn;
                if (this.mHhbName != null || this.mCsgId == null) {
                    if (this.mHhbName != null) {
                        plmn = (plmn + " - ") + this.mHhbName;
                    }
                } else if (SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
                    plmn = (plmn + " - ") + this.mCsgId;
                } else {
                    try {
                        if (this.mServiceStateExt.needToShowCsgId()) {
                            plmn = (plmn + " - ") + this.mCsgId;
                        }
                    } catch (RuntimeException e2) {
                        e2.printStackTrace();
                    }
                }
                if (!this.mSubscriptionController.setPlmnSpn(phoneId, this.mCurShowPlmn, plmn, this.mCurShowSpn, this.mCurSpn)) {
                    this.mSpnUpdatePending = true;
                }
            }
        }
    }

    private void broadcastHideNetworkState(String action, int state) {
        if (DBG) {
            log("broadcastHideNetworkUpdate action=" + action + " state=" + state);
        }
        Intent intent = new Intent("mediatek.intent.action.ACTION_HIDE_NETWORK_STATE");
        if (TelephonyManager.getDefault().getPhoneCount() == 1) {
            intent.addFlags(536870912);
        }
        intent.putExtra("action", action);
        intent.putExtra("state", state);
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
        this.mPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private void onInvalidSimInfoReceived(AsyncResult ar) {
        String[] InvalidSimInfo = ar.result;
        String plmn = InvalidSimInfo[0];
        int cs_invalid = Integer.parseInt(InvalidSimInfo[1]);
        int ps_invalid = Integer.parseInt(InvalidSimInfo[2]);
        int cause = Integer.parseInt(InvalidSimInfo[3]);
        if (1 == ps_invalid) {
            log("ps_invlaid==1 check");
            SystemProperties.set("gsm.sim.invalidpssim", UsimPBMemInfo.STRING_NOT_SET + this.mPhone.getPhoneId());
        } else {
            log("ps_invlaid !=1 check ");
            SystemProperties.set("gsm.sim.invalidpssim", "1000");
        }
        int testMode = SystemProperties.getInt("gsm.gcf.testmode", 0);
        log("onInvalidSimInfoReceived testMode:" + testMode + " cause:" + cause + " cs_invalid:" + cs_invalid + " ps_invalid:" + ps_invalid + " plmn:" + plmn + " mEverIVSR:" + this.mEverIVSR);
        if (testMode != 0) {
            log("InvalidSimInfo received during test mode: " + testMode);
        } else if (this.mServiceStateExt.isNeedDisableIVSR()) {
            log("Disable IVSR");
        } else {
            if (cs_invalid == 1) {
                this.isCsInvalidCard = true;
            }
            if (this.mVoiceCapable && cs_invalid == 1 && this.mLastRegisteredPLMN != null && plmn.equals(this.mLastRegisteredPLMN)) {
                log("InvalidSimInfo reset SIM due to CS invalid");
                setEverIVSR(true);
                this.mLastRegisteredPLMN = null;
                this.mLastPSRegisteredPLMN = null;
                this.mCi.setSimPower(2, null);
            } else if (ps_invalid == 1 && isAllowRecoveryOnIvsr(ar) && this.mLastPSRegisteredPLMN != null && plmn.equals(this.mLastPSRegisteredPLMN)) {
                log("InvalidSimInfo reset SIM due to PS invalid ");
                setEverIVSR(true);
                this.mLastRegisteredPLMN = null;
                this.mLastPSRegisteredPLMN = null;
                this.mCi.setSimPower(2, null);
            }
        }
    }

    private void onModulationInfoReceived(AsyncResult ar) {
        if (ar.exception != null || ar.result == null) {
            loge("onModulationInfoReceived exception");
            return;
        }
        int modulation = ar.result[0];
        log("[onModulationInfoReceived] modulation:" + modulation);
        Intent intent = new Intent("mediatek.intent.action.ACTION_NOTIFY_MODULATION_INFO");
        intent.addFlags(536870912);
        intent.putExtra("modulation_info", modulation);
        this.mPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private boolean isAllowRecoveryOnIvsr(AsyncResult ar) {
        if (this.mPhone.isInCall()) {
            log("[isAllowRecoveryOnIvsr] isInCall()=true");
            Message msg = obtainMessage();
            msg.what = 101;
            msg.obj = ar;
            sendMessageDelayed(msg, 10000);
            return false;
        }
        log("isAllowRecoveryOnIvsr() return true");
        return true;
    }

    private void setRejectCauseNotification(int cause) {
        if (DBG) {
            log("setRejectCauseNotification: create notification " + cause);
        }
        Context context = this.mPhone.getContext();
        this.mNotificationBuilder = new Builder(context);
        this.mNotificationBuilder.setWhen(System.currentTimeMillis());
        this.mNotificationBuilder.setAutoCancel(true);
        this.mNotificationBuilder.setSmallIcon(17301642);
        this.mNotificationBuilder.setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), 134217728));
        CharSequence details = UsimPBMemInfo.STRING_NOT_SET;
        CharSequence title = context.getText(134545511);
        switch (cause) {
            case 2:
                details = context.getText(134545512);
                break;
            case 3:
                details = context.getText(134545513);
                break;
            case 5:
                details = context.getText(134545520);
                break;
            case 6:
                details = context.getText(134545521);
                break;
            case 13:
                details = context.getText(134545525);
                break;
        }
        if (DBG) {
            log("setRejectCauseNotification: put notification " + title + " / " + details);
        }
        this.mNotificationBuilder.setContentTitle(title);
        this.mNotificationBuilder.setContentText(details);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
        this.mNotification = this.mNotificationBuilder.build();
        notificationManager.notify(REJECT_NOTIFICATION, this.mNotification);
    }

    private void setSpecialCardTypeNotification(String iccCardType, int titleType, int detailType) {
        if (DBG) {
            log("setSpecialCardTypeNotification: create notification for " + iccCardType);
        }
        Context context = this.mPhone.getContext();
        this.mNotificationBuilder = new Builder(context);
        this.mNotificationBuilder.setWhen(System.currentTimeMillis());
        this.mNotificationBuilder.setAutoCancel(true);
        this.mNotificationBuilder.setSmallIcon(17301642);
        this.mNotificationBuilder.setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), 134217728));
        CharSequence title = UsimPBMemInfo.STRING_NOT_SET;
        switch (titleType) {
            case 0:
                title = context.getText(134545559);
                break;
        }
        CharSequence details = UsimPBMemInfo.STRING_NOT_SET;
        switch (detailType) {
            case 0:
                details = context.getText(134545560);
                break;
        }
        if (DBG) {
            log("setSpecialCardTypeNotification: put notification " + title + " / " + details);
        }
        this.mNotificationBuilder.setContentTitle(title);
        this.mNotificationBuilder.setContentText(details);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
        this.mNotification = this.mNotificationBuilder.build();
        notificationManager.notify(SPECIAL_CARD_TYPE_NOTIFICATION, this.mNotification);
    }

    private int getDstForMcc(int mcc, long when) {
        if (mcc == 0) {
            return 0;
        }
        String tzId = MccTable.defaultTimeZoneForMcc(mcc);
        if (tzId == null || !TimeZone.getTimeZone(tzId).inDaylightTime(new Date(when))) {
            return 0;
        }
        log("[NITZ] getDstForMcc: dst=" + 1);
        return 1;
    }

    private int getMobileCountryCode() {
        int mcc = 0;
        String operatorNumeric = this.mSS.getOperatorNumeric();
        if (operatorNumeric == null) {
            return mcc;
        }
        try {
            return Integer.parseInt(operatorNumeric.substring(0, 3));
        } catch (NumberFormatException ex) {
            loge("countryCodeForMcc error" + ex);
            return mcc;
        } catch (StringIndexOutOfBoundsException ex2) {
            loge("countryCodeForMcc error" + ex2);
            return mcc;
        }
    }

    private TimeZone getTimeZonesWithCapitalCity(String iso) {
        if (this.mZoneOffset != 0 || this.mZoneDst) {
            log("don't udpate with capital city, cause we have received nitz");
            return null;
        }
        for (int i = 0; i < this.mTimeZoneIdOfCapitalCity.length; i++) {
            if (iso.equals(this.mTimeZoneIdOfCapitalCity[i][0])) {
                TimeZone tz = TimeZone.getTimeZone(this.mTimeZoneIdOfCapitalCity[i][1]);
                log("uses TimeZone of Capital City:" + this.mTimeZoneIdOfCapitalCity[i][1]);
                return tz;
            }
        }
        return null;
    }

    private String getTimeZonesByMcc(String mcc) {
        for (int i = 0; i < this.mTimeZoneIdByMcc.length; i++) {
            if (mcc.equals(this.mTimeZoneIdByMcc[i][0])) {
                String tz = this.mTimeZoneIdByMcc[i][1];
                log("uses Timezone of GsmSST by mcc: " + this.mTimeZoneIdByMcc[i][1]);
                return tz;
            }
        }
        return null;
    }

    protected void fixTimeZone() {
        TimeZone zone = null;
        String iso = UsimPBMemInfo.STRING_NOT_SET;
        String operatorNumeric = this.mSS.getOperatorNumeric();
        if (operatorNumeric == null || operatorNumeric.equals(UsimPBMemInfo.STRING_NOT_SET) || !isNumeric(operatorNumeric)) {
            log("fixTimeZone but not registered and operatorNumeric is null or invalid value");
            return;
        }
        String mcc = operatorNumeric.substring(0, 3);
        try {
            iso = MccTable.countryCodeForMcc(Integer.parseInt(mcc));
        } catch (NumberFormatException ex) {
            loge("fixTimeZone countryCodeForMcc error" + ex);
        }
        if (!(mcc.equals(INVALID_MCC) || TextUtils.isEmpty(iso) || !getAutoTimeZone())) {
            boolean testOneUniqueOffsetPath = SystemProperties.getBoolean("telephony.test.ignore.nitz", false) ? (SystemClock.uptimeMillis() & 1) == 0 : false;
            ArrayList<TimeZone> uniqueZones = TimeUtils.getTimeZonesWithUniqueOffsets(iso);
            if (uniqueZones.size() == 1 || testOneUniqueOffsetPath) {
                zone = (TimeZone) uniqueZones.get(0);
                if (DBG) {
                    log("fixTimeZone: no nitz but one TZ for iso-cc=" + iso + " with zone.getID=" + zone.getID() + " testOneUniqueOffsetPath=" + testOneUniqueOffsetPath);
                }
                setAndBroadcastNetworkSetTimeZone(zone.getID());
            } else if (uniqueZones.size() > 1) {
                log("uniqueZones.size=" + uniqueZones.size());
                zone = getTimeZonesWithCapitalCity(iso);
                if (zone != null) {
                    setAndBroadcastNetworkSetTimeZone(zone.getID());
                }
            } else if (DBG) {
                log("fixTimeZone: there are " + uniqueZones.size() + " unique offsets for iso-cc='" + iso + " testOneUniqueOffsetPath=" + testOneUniqueOffsetPath + "', do nothing");
            }
        }
        if (zone != null) {
            log("fixTimeZone: zone != null zone.getID=" + zone.getID());
            if (getAutoTimeZone()) {
                setAndBroadcastNetworkSetTimeZone(zone.getID());
            }
            saveNitzTimeZone(zone.getID());
        } else {
            log("fixTimeZone: zone == null");
        }
    }

    public boolean isNumeric(String str) {
        try {
            int testNum = Integer.parseInt(str);
            return true;
        } catch (NumberFormatException eNFE) {
            log("isNumeric:" + eNFE.toString());
            return false;
        } catch (Exception e) {
            log("isNumeric:" + e.toString());
            return false;
        }
    }

    private void onPsNetworkStateChangeResult(AsyncResult ar) {
        if (ar.exception != null || ar.result == null) {
            loge("onPsNetworkStateChangeResult exception");
            return;
        }
        int[] info = ar.result;
        int newUrcState = regCodeToServiceState(info[0]);
        log("onPsNetworkStateChangeResult, mPsRegState:" + this.mPsRegState + ",new:" + newUrcState + ",result:" + info[0]);
        if (5 == info[0] && SubscriptionManager.isVsimEnabled(this.mPhone.getSubId())) {
            this.mPsRegStateRaw = 1;
        } else {
            this.mPsRegStateRaw = info[0];
        }
        if (this.mPsRegState == 0 && newUrcState != 0) {
            log("set flag for ever detach, may notify attach later");
            this.bHasDetachedDuringPolling = true;
        }
    }

    private void handlePsRegNotification(int oldState, int newState) {
        boolean specificNotify = false;
        log("old:" + oldState + " ,mPsRegState:" + this.mPsRegState + ",new:" + newState);
        boolean hasGprsAttached = oldState != 0 ? this.mPsRegState == 0 : false;
        boolean hasGprsDetached = oldState == 0 ? this.mPsRegState != 0 : false;
        if (hasGprsAttached) {
            this.mAttachedRegistrants.notifyRegistrants();
            this.mLastPSRegisteredPLMN = this.mSS.getOperatorNumeric();
            log("mLastPSRegisteredPLMN= " + this.mLastPSRegisteredPLMN);
            this.bHasDetachedDuringPolling = false;
        }
        if (hasGprsDetached) {
            this.mDetachedRegistrants.notifyRegistrants();
        }
        hasGprsAttached = this.mPsRegState != 0 ? newState == 0 : false;
        hasGprsDetached = this.mPsRegState == 0 ? newState != 0 : false;
        if (!hasGprsAttached && this.bHasDetachedDuringPolling && newState == 0) {
            specificNotify = true;
            log("need to compensate for notifying");
        }
        if (hasGprsAttached || specificNotify) {
            this.mAttachedRegistrants.notifyRegistrants();
            this.mLastPSRegisteredPLMN = this.mSS.getOperatorNumeric();
            log("mLastPSRegisteredPLMN= " + this.mLastPSRegisteredPLMN);
        }
        if (hasGprsDetached) {
            this.mDetachedRegistrants.notifyRegistrants();
        }
        this.mPsRegState = newState;
        this.bHasDetachedDuringPolling = false;
    }

    private void getEINFO(int eventId) {
        GsmCdmaPhone gsmCdmaPhone = this.mPhone;
        String[] strArr = new String[2];
        strArr[0] = "AT+EINFO?";
        strArr[1] = "+EINFO";
        gsmCdmaPhone.invokeOemRilRequestStrings(strArr, obtainMessage(eventId));
        log("getEINFO for EMMRRS");
    }

    private void setEINFO(int value, Message onComplete) {
        String[] Cmd = new String[2];
        Cmd[0] = "AT+EINFO=" + value;
        Cmd[1] = "+EINFO";
        this.mPhone.invokeOemRilRequestStrings(Cmd, onComplete);
        log("setEINFO for EMMRRS, ATCmd[0]=" + Cmd[0]);
    }

    private boolean isCurrentPhoneDataConnectionOn() {
        int defaultDataSubId = SubscriptionManager.getDefaultDataSubscriptionId();
        boolean userDataEnabled = true;
        try {
            userDataEnabled = TelephonyManager.getIntWithSubId(this.mPhone.getContext().getContentResolver(), "mobile_data", defaultDataSubId) == 1;
        } catch (SettingNotFoundException snfe) {
            if (DBG) {
                log("isCurrentPhoneDataConnectionOn: SettingNofFoundException snfe=" + snfe);
            }
        }
        log("userDataEnabled=" + userDataEnabled + ", defaultDataSubId=" + defaultDataSubId);
        if (userDataEnabled && defaultDataSubId == SubscriptionManager.getSubIdUsingPhoneId(this.mPhone.getPhoneId())) {
            return true;
        }
        return false;
    }

    protected int updateOperatorAlpha(String operatorAlphaLong) {
        TelephonyManager.setTelephonyProperty(this.mPhone.getPhoneId(), "gsm.operator.alpha", operatorAlphaLong);
        return 1;
    }

    private void updateNetworkInfo(int newRegState, int newNetworkType) {
        int displayState = this.mCi.getDisplayState();
        boolean isRegisted;
        if (newRegState == 1 || newRegState == 5) {
            isRegisted = true;
        } else {
            isRegisted = false;
        }
        if (displayState != 1 || this.mIsForceSendScreenOnForUpdateNwInfo || (!isRegisted && displayState == 1)) {
            this.mNewSS.setRilVoiceRadioTechnology(newNetworkType);
        } else if (this.mSS.getVoiceRegState() == 1 && isRegisted && displayState == 1) {
            if (!this.mIsForceSendScreenOnForUpdateNwInfo) {
                log("send screen state ON to change format of CREG");
                this.mIsForceSendScreenOnForUpdateNwInfo = true;
                this.mCi.sendScreenState(true);
                pollState();
            }
        } else if (displayState == 1 && isRegisted) {
            this.mNewSS.setRilVoiceRadioTechnology(this.mSS.getRilVoiceRadioTechnology());
            log("set Voice network type=" + this.mNewSS.getRilVoiceRadioTechnology() + " update network type with old type.");
        }
    }

    public boolean isSameRadioTechnologyMode(int nRadioTechnology1, int nRadioTechnology2) {
        if ((nRadioTechnology1 == 14 && nRadioTechnology2 == 14) || (nRadioTechnology1 == 16 && nRadioTechnology2 == 16)) {
            return true;
        }
        if (((nRadioTechnology1 < 3 || nRadioTechnology1 > 13) && nRadioTechnology1 != 15) || ((nRadioTechnology2 < 3 || nRadioTechnology2 > 13) && nRadioTechnology2 != 15)) {
            return false;
        }
        return true;
    }

    private void setReceivedNitz(int phoneId, boolean receivedNitz) {
        log("setReceivedNitz : phoneId = " + phoneId);
        sReceiveNitz[phoneId] = receivedNitz;
    }

    private boolean getReceivedNitz() {
        return sReceiveNitz[this.mPhone.getPhoneId()];
    }

    private void onNetworkEventReceived(AsyncResult ar) {
        if (ar.exception != null || ar.result == null) {
            loge("onNetworkEventReceived exception");
            return;
        }
        int nwEventType = ((int[]) ar.result)[1];
        log("[onNetworkEventReceived] event_type:" + nwEventType);
        Intent intent = new Intent("android.intent.action.ACTION_NETWORK_EVENT");
        intent.addFlags(536870912);
        intent.putExtra("eventType", nwEventType + 1);
        this.mPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    public void pollState() {
        pollState(false);
    }

    private void modemTriggeredPollState() {
        pollState(true);
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void pollState(boolean modemTriggered) {
        log("pollState RadioState is " + this.mCi.getRadioState() + ", currentNetworkMode= " + getPreferredNetworkModeSettings(this.mPhone.getPhoneId()) + ", mPollingContext=" + (this.mPollingContext != null ? this.mPollingContext[0] : -1) + ", modemTriggered=" + modemTriggered + ", hasPendingPollState=" + this.hasPendingPollState);
        if (this.mPollingContext == null || this.mCi.getRadioState() == RadioState.RADIO_UNAVAILABLE || (!(this.mPhone.isPhoneTypeGsm() && this.mPollingContext[0] == 4) && (this.mPhone.isPhoneTypeGsm() || this.mPollingContext[0] != 3))) {
            this.mPollingContext = new int[1];
            this.mPollingContext[0] = 0;
            if (this.mPhone.isPhoneTypeGsm() && this.dontUpdateNetworkStateFlag) {
                log("pollState is ignored!!");
                return;
            }
            switch (m42x804b995f()[this.mCi.getRadioState().ordinal()]) {
                case 1:
                    this.mNewSS.setStateOff();
                    this.mNewCellLoc.setStateInvalid();
                    setSignalStrengthDefaultValues();
                    this.mGotCountryCode = false;
                    this.mNitzUpdatedTime = false;
                    if (this.mPhone.isPhoneTypeGsm()) {
                        setNullState();
                    }
                    if (!modemTriggered && 18 != this.mSS.getRilDataRadioTechnology() && regCodeToServiceState(this.mPsRegStateRaw) != 0) {
                        this.mPsRegStateRaw = 0;
                        pollStateDone();
                        break;
                    }
                    cleanMccProperties(this.mPhone.getPhoneId());
                    break;
                case 2:
                    if (this.mPhone.isPhoneTypeCdmaLte()) {
                        this.mNewSS.setStateOutOfService();
                    } else {
                        this.mNewSS.setStateOff();
                    }
                    this.mNewCellLoc.setStateInvalid();
                    setSignalStrengthDefaultValues();
                    this.mGotCountryCode = false;
                    this.mNitzUpdatedTime = false;
                    if (this.mPhone.isPhoneTypeGsm()) {
                        setNullState();
                        this.mPsRegStateRaw = 0;
                    }
                    pollStateDone();
                    break;
                default:
                    int[] iArr = this.mPollingContext;
                    iArr[0] = iArr[0] + 1;
                    this.mCi.getOperator(obtainMessage(6, this.mPollingContext));
                    iArr = this.mPollingContext;
                    iArr[0] = iArr[0] + 1;
                    this.mCi.getDataRegistrationState(obtainMessage(5, this.mPollingContext));
                    iArr = this.mPollingContext;
                    iArr[0] = iArr[0] + 1;
                    this.mCi.getVoiceRegistrationState(obtainMessage(4, this.mPollingContext));
                    if (this.mPhone.isPhoneTypeGsm()) {
                        iArr = this.mPollingContext;
                        iArr[0] = iArr[0] + 1;
                        this.mCi.getNetworkSelectionMode(obtainMessage(14, this.mPollingContext));
                        break;
                    }
                    break;
            }
            return;
        }
        this.hasPendingPollState = true;
    }

    private void pollStateDone() {
        if (OemConstant.SWITCH__SMOOTH) {
            int checkResult = oppoOosDelayState();
            if (DBG) {
                log("oppoDelayOosState checkResult = " + checkResult);
            }
            if (checkResult == 1) {
                this.mOppoNeedNotify = true;
                if (this.hasPendingPollState) {
                    this.hasPendingPollState = false;
                    pollState();
                }
                return;
            } else if (checkResult == 2) {
                oppoSetAutoNetworkSelect();
            }
        }
        if (this.mPhone.isPhoneTypeGsm()) {
            pollStateDoneGsm();
        } else if (this.mPhone.isPhoneTypeCdma()) {
            pollStateDoneCdma();
        } else {
            pollStateDoneCdmaLte();
        }
        if (this.hasPendingPollState) {
            this.hasPendingPollState = false;
            pollState();
        }
    }

    private void pollStateDoneGsm() {
        boolean hasDeregistered;
        boolean hasDataRoamingTypeChanged;
        Integer[] numArr;
        this.iso = UsimPBMemInfo.STRING_NOT_SET;
        this.mcc = UsimPBMemInfo.STRING_NOT_SET;
        if (Build.IS_DEBUGGABLE && SystemProperties.getBoolean(PROP_FORCE_ROAMING, false)) {
            this.mNewSS.setVoiceRoaming(true);
            this.mNewSS.setDataRoaming(true);
        }
        useDataRegStateForDataOnlyDevices();
        resetServiceStateInIwlanMode();
        log("Poll ServiceState done:  oldSS=[" + this.mSS + "] newSS=[" + this.mNewSS + "]" + " oldMaxDataCalls=" + this.mMaxDataCalls + " mNewMaxDataCalls=" + this.mNewMaxDataCalls + " oldReasonDataDenied=" + this.mReasonDataDenied + " mNewReasonDataDenied=" + this.mNewReasonDataDenied);
        int ddsSubid = SubscriptionManager.getDefaultDataSubscriptionId();
        if (SubscriptionManager.getSlotId(ddsSubid) == this.mPhone.getPhoneId() || !(this.mNewSS.getRilVoiceRadioTechnology() == 14 || this.mNewSS.getRilVoiceRadioTechnology() == 19 || this.mNewSS.getRilDataRadioTechnology() == 14 || this.mNewSS.getRilDataRadioTechnology() == 19)) {
            setNeedSetDss(this.mPhone.getPhoneId(), false);
        } else {
            if (DBG) {
                log("getDefaultDataSubscriptionId == " + ddsSubid + "slotid == " + SubscriptionManager.getSlotId(ddsSubid));
            }
            setNeedSetDss(this.mPhone.getPhoneId(), true);
        }
        if (this.mIsForceSendScreenOnForUpdateNwInfo) {
            log("send screen state OFF to restore format of CREG");
            this.mIsForceSendScreenOnForUpdateNwInfo = false;
            if (this.mCi.getDisplayState() == 1) {
                this.mCi.sendScreenState(false);
            }
        }
        boolean hasRegistered = this.mSS.getVoiceRegState() != 0 ? this.mNewSS.getVoiceRegState() == 0 : false;
        if (this.mSS.getVoiceRegState() != 0) {
            hasDeregistered = false;
        } else if (this.mNewSS.getVoiceRegState() != 0) {
            hasDeregistered = true;
        } else {
            hasDeregistered = false;
        }
        boolean hasGprsAttached = (this.mSS.getDataRegState() != 0 || this.mOppoNeedNotify) ? this.mNewSS.getDataRegState() == 0 : false;
        boolean hasGprsDetached = this.mSS.getDataRegState() == 0 ? this.mNewSS.getDataRegState() != 0 : false;
        boolean hasDataRegStateChanged = this.mSS.getDataRegState() != this.mNewSS.getDataRegState();
        boolean hasVoiceRegStateChanged = this.mSS.getVoiceRegState() != this.mNewSS.getVoiceRegState();
        boolean hasLocationChanged = !this.mNewCellLoc.equals(this.mCellLoc);
        if (!hasLocationChanged) {
        }
        if (this.mSS.getRilVoiceRegState() != this.mNewSS.getRilVoiceRegState()) {
        }
        boolean hasRilVoiceRadioTechnologyChanged = this.mSS.getRilVoiceRadioTechnology() != this.mNewSS.getRilVoiceRadioTechnology();
        boolean hasRilDataRadioTechnologyChanged = this.mSS.getRilDataRadioTechnology() != this.mNewSS.getRilDataRadioTechnology();
        boolean hasChanged = (!this.mNewSS.equals(this.mSS) || this.mNeedNotify) ? true : this.mOppoNeedNotify;
        boolean hasVoiceRoamingOn = !this.mSS.getVoiceRoaming() ? this.mNewSS.getVoiceRoaming() : false;
        boolean hasVoiceRoamingOff = this.mSS.getVoiceRoaming() && !this.mNewSS.getVoiceRoaming();
        boolean hasDataRoamingOn = !this.mSS.getDataRoaming() ? this.mNewSS.getDataRoaming() : false;
        boolean hasDataRoamingOff = this.mSS.getDataRoaming() && !this.mNewSS.getDataRoaming();
        if (this.mSS.getDataRoaming() && this.mNewSS.getDataRoaming()) {
            hasDataRoamingTypeChanged = this.mSS.getDataRoamingType() != this.mNewSS.getDataRoamingType();
        } else {
            hasDataRoamingTypeChanged = false;
        }
        TelephonyManager tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
        log("pollStateDone,hasRegistered:" + hasRegistered + ",hasDeregistered:" + hasDeregistered + ",hasGprsAttached:" + hasGprsAttached + ",hasRilVoiceRadioTechnologyChanged:" + hasRilVoiceRadioTechnologyChanged + ",hasRilDataRadioTechnologyChanged:" + hasRilDataRadioTechnologyChanged + ",hasVoiceRegStateChanged:" + hasVoiceRegStateChanged + ",hasDataRegStateChanged:" + hasDataRegStateChanged + ",hasChanged:" + hasChanged + ",hasVoiceRoamingOn:" + hasVoiceRoamingOn + ",hasVoiceRoamingOff:" + hasVoiceRoamingOff + ",hasDataRoamingOn:" + hasDataRoamingOn + ",hasDataRoamingOff:" + hasDataRoamingOff + ",hasLocationChanged:" + hasLocationChanged + ",hasLacChanged:" + (((GsmCellLocation) this.mNewCellLoc).getLac() != ((GsmCellLocation) this.mCellLoc).getLac()) + ",sReceiveNitz:" + getReceivedNitz() + ",hasDataRoamingTypeChanged:" + hasDataRoamingTypeChanged);
        if (hasVoiceRegStateChanged || hasDataRegStateChanged) {
            numArr = new Object[4];
            numArr[0] = Integer.valueOf(this.mSS.getVoiceRegState());
            numArr[1] = Integer.valueOf(this.mSS.getDataRegState());
            numArr[2] = Integer.valueOf(this.mNewSS.getVoiceRegState());
            numArr[3] = Integer.valueOf(this.mNewSS.getDataRegState());
            EventLog.writeEvent(EventLogTags.GSM_SERVICE_STATE_CHANGE, numArr);
        }
        int newOosFlag = 0;
        if (this.mNewSS.getVoiceRegState() == 1 && !this.mNewSS.isEmergencyOnly()) {
            newOosFlag = 1;
        }
        if (newOosFlag != this.oosFlag) {
            SystemProperties.set("com.oppo.oos" + this.mPhone.getPhoneId(), newOosFlag == 1 ? "1" : "0");
            this.oosFlag = newOosFlag;
            log("newOosFlag " + newOosFlag);
        }
        if (hasRilVoiceRadioTechnologyChanged) {
            int cid = -1;
            GsmCellLocation loc = (GsmCellLocation) this.mNewCellLoc;
            if (loc != null) {
                cid = loc.getCid();
            }
            numArr = new Object[3];
            numArr[0] = Integer.valueOf(cid);
            numArr[1] = Integer.valueOf(this.mSS.getRilVoiceRadioTechnology());
            numArr[2] = Integer.valueOf(this.mNewSS.getRilVoiceRadioTechnology());
            EventLog.writeEvent(EventLogTags.GSM_RAT_SWITCHED_NEW, numArr);
            if (DBG) {
                log("RAT switched " + ServiceState.rilRadioTechnologyToString(this.mSS.getRilVoiceRadioTechnology()) + " -> " + ServiceState.rilRadioTechnologyToString(this.mNewSS.getRilVoiceRadioTechnology()) + " at cell " + cid);
            }
        }
        if (OemConstant.EXP_VERSION && RegionLockConstant.IS_REGION_LOCK && RegionLockConstant.getRegionLockStatus() && !oppoIsTestCard()) {
            if (DBG) {
                log("Davis mOppoNeedSetRadio==" + this.mOppoNeedSetRadio + ",mOppoNeedSetRadio==" + this.mOppoNeedSetRadio);
            }
            CharSequence plmn = null;
            if (getOemRegState(this.mNewSS) == 0 && !TextUtils.isEmpty(this.mNewSS.getOperatorNumeric())) {
                plmn = this.mNewSS.getOperatorNumeric();
            }
            if (TextUtils.isEmpty(plmn)) {
                if (!(this.mNewSS.getVoiceRegState() == 0 || this.mOppoNeedSetAlarm)) {
                    this.mOppoNeedSetAlarm = true;
                    cancelNetworkStatusAlarm(this.mPhone.getPhoneId());
                    if (DBG) {
                        log("Davis cancel alarm");
                    }
                }
                if (this.mCi.getRadioState() == RadioState.RADIO_OFF && !this.mOppoNeedSetRadio) {
                    this.mOppoNeedSetRadio = true;
                    if (DBG) {
                        log("Davis reset radio flag");
                    }
                }
            } else {
                if (this.mRegionLockPlmnList.oppoIsWhiteListNetwork(RegionLockConstant.VERSION, plmn)) {
                    if (this.mOppoNeedSetAlarm) {
                        startResetNetworkStatusAlarm(getPhoneId());
                        this.mOppoNeedSetAlarm = false;
                        if (DBG) {
                            log("Davis start alarm");
                        }
                    }
                    SystemProperties.set(RegionLockConstant.NOTIFY_NETLOCK_FLAG, "0");
                } else {
                    if (this.mRegionLockPlmnList.oppoIsBlackListNetwork(RegionLockConstant.VERSION, plmn)) {
                        if (Global.getInt(this.mPhone.getContext().getContentResolver(), "oppo_emergency_call_on", 0) == 0) {
                            if (DBG) {
                                log("Davis set radio power");
                            }
                            oppoSetPowerRadioOff(this.mPhone.getPhoneId());
                        } else {
                            if (DBG) {
                                log("Davis is dialing ecall");
                            }
                            this.mCallingPhoneId = this.mPhone.getPhoneId();
                        }
                        SystemProperties.set(RegionLockConstant.NOTIFY_NETLOCK_FLAG, "1");
                        return;
                    }
                }
            }
        }
        if (OemConstant.EXP_VERSION && OemDeviceLock.IS_OP_LOCK && OemDeviceLock.getDeviceLockStatus() && OemDeviceLock.getSimBindedStatus()) {
            if (getOemRegState(this.mNewSS) == 1) {
                this.mCurrentUpdateOOSTime = true;
                if (this.mLastUpdateOOSTime != this.mCurrentUpdateOOSTime) {
                    this.mLastUpdateOOSTime = this.mCurrentUpdateOOSTime;
                    OemDeviceLock.updateServiceStatusTime(getPhoneId(), true);
                }
                this.mLastUpdateOOSTime = this.mCurrentUpdateOOSTime;
            } else {
                this.mCurrentUpdateOOSTime = false;
                if (this.mLastUpdateOOSTime != this.mCurrentUpdateOOSTime) {
                    this.mLastUpdateOOSTime = this.mCurrentUpdateOOSTime;
                    OemDeviceLock.updateServiceStatusTime(getPhoneId(), false);
                }
                this.mLastUpdateOOSTime = this.mCurrentUpdateOOSTime;
                OemDeviceLock.updateLockedTime();
            }
        }
        if (OemConstant.EXP_VERSION && OemDeviceLock.IS_OP_LOCK && OemDeviceLock.getDeviceLockStatus() && OemDeviceLock.isNeedShowOutService(this.mPhone.getPhoneId())) {
            log("device lock not allow update servicestate " + this.mPhone.getPhoneId());
            this.mNewSS.setStateOutOfService();
        }
        ServiceState tss = this.mSS;
        this.mSS = this.mNewSS;
        this.mNewSS = tss;
        CellLocation tcl = (GsmCellLocation) this.mCellLoc;
        this.mCellLoc = this.mNewCellLoc;
        this.mNewCellLoc = tcl;
        this.mReasonDataDenied = this.mNewReasonDataDenied;
        this.mMaxDataCalls = this.mNewMaxDataCalls;
        if (hasRilVoiceRadioTechnologyChanged) {
            updatePhoneObject();
        }
        if (hasRilDataRadioTechnologyChanged) {
            tm.setDataNetworkTypeForPhone(this.mPhone.getPhoneId(), this.mSS.getRilDataRadioTechnology());
            if (18 == this.mSS.getRilDataRadioTechnology()) {
                log("pollStateDone: IWLAN enabled");
            }
        }
        if (hasRegistered) {
            this.mNetworkAttachedRegistrants.notifyRegistrants();
            this.mLastRegisteredPLMN = this.mSS.getOperatorNumeric();
            log("mLastRegisteredPLMN= " + this.mLastRegisteredPLMN);
            if (DBG) {
                log("pollStateDone: registering current mNitzUpdatedTime=" + this.mNitzUpdatedTime + " changing to false");
            }
            this.mNitzUpdatedTime = false;
        }
        if (this.explict_update_spn == 1) {
            if (!hasChanged) {
                log("explict_update_spn trigger to refresh SPN");
                updateSpnDisplay();
            }
            this.explict_update_spn = 0;
        }
        if (hasChanged) {
            this.mOppoNeedNotify = false;
            updateSpnDisplay();
            this.mNeedNotify = false;
            String prevOperatorNumeric = tm.getNetworkOperatorForPhone(this.mPhone.getPhoneId());
            String operatorNumeric = this.mSS.getOperatorNumeric();
            tm.setNetworkOperatorNumericForPhone(this.mPhone.getPhoneId(), operatorNumeric);
            updateCarrierMccMncConfiguration(operatorNumeric, prevOperatorNumeric, this.mPhone.getContext());
            if (operatorNumeric == null || isNumeric(operatorNumeric)) {
                if (TextUtils.isEmpty(operatorNumeric)) {
                    if (DBG) {
                        log("operatorNumeric is null");
                    }
                    updateCarrierMccMncConfiguration(operatorNumeric, prevOperatorNumeric, this.mPhone.getContext());
                    tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), UsimPBMemInfo.STRING_NOT_SET);
                    this.mGotCountryCode = false;
                    this.mNitzUpdatedTime = false;
                } else {
                    try {
                        this.mcc = operatorNumeric.substring(0, 3);
                        this.iso = MccTable.countryCodeForMcc(Integer.parseInt(this.mcc));
                    } catch (NumberFormatException ex) {
                        loge("pollStateDone: countryCodeForMcc error" + ex);
                    } catch (StringIndexOutOfBoundsException ex2) {
                        loge("pollStateDone: countryCodeForMcc error" + ex2);
                    }
                    tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), this.iso);
                    this.mGotCountryCode = true;
                    if (!(this.mNitzUpdatedTime || this.mcc.equals(INVALID_MCC) || TextUtils.isEmpty(this.iso) || !getAutoTimeZone())) {
                        boolean testOneUniqueOffsetPath = SystemProperties.getBoolean("telephony.test.ignore.nitz", false) ? (SystemClock.uptimeMillis() & 1) == 0 : false;
                        ArrayList<TimeZone> uniqueZones = TimeUtils.getTimeZonesWithUniqueOffsets(this.iso);
                        TimeZone zone;
                        if (uniqueZones.size() == 1 || testOneUniqueOffsetPath) {
                            zone = (TimeZone) uniqueZones.get(0);
                            if (DBG) {
                                log("pollStateDone: no nitz but one TZ for iso-cc=" + this.iso + " with zone.getID=" + zone.getID() + " testOneUniqueOffsetPath=" + testOneUniqueOffsetPath);
                            }
                            setAndBroadcastNetworkSetTimeZone(zone.getID());
                        } else if (uniqueZones.size() > 1) {
                            log("uniqueZones.size=" + uniqueZones.size() + " iso= " + this.iso);
                            zone = getTimeZonesWithCapitalCity(this.iso);
                            if (zone != null) {
                                setAndBroadcastNetworkSetTimeZone(zone.getID());
                            } else {
                                log("Can't find time zone for capital city");
                            }
                        } else if (DBG) {
                            log("pollStateDone: there are " + uniqueZones.size() + " unique offsets for iso-cc='" + this.iso + " testOneUniqueOffsetPath=" + testOneUniqueOffsetPath + "', do nothing");
                        }
                    }
                    if (shouldFixTimeZoneNow(this.mPhone, operatorNumeric, prevOperatorNumeric, this.mNeedFixZoneAfterNitz)) {
                        fixTimeZone(this.iso);
                    }
                }
            } else if (DBG) {
                log("operatorNumeric is Invalid value, don't update timezone");
            }
            tm.setNetworkRoamingForPhone(this.mPhone.getPhoneId(), this.mSS.getVoiceRoaming());
            setRoamingType(this.mSS);
            log("Broadcasting ServiceState : " + this.mSS);
            this.mPhone.notifyServiceStateChanged(this.mSS);
            TelephonyMetrics.getInstance().writeServiceStateChanged(this.mPhone.getPhoneId(), this.mSS);
            if (hasRegistered || hasGprsAttached || hasRilVoiceRadioTechnologyChanged || hasRilDataRadioTechnologyChanged) {
                if (DBG) {
                    log("service state rat changed should update and notify signal");
                }
                updateOEMSmooth(this.mSS);
            }
        }
        if (hasGprsAttached || hasGprsDetached || hasRegistered || hasDeregistered) {
            logAttachChange();
        }
        if (hasGprsAttached) {
            this.mAttachedRegistrants.notifyRegistrants();
            this.mLastPSRegisteredPLMN = this.mSS.getOperatorNumeric();
            log("mLastPSRegisteredPLMN= " + this.mLastPSRegisteredPLMN);
        }
        if (hasGprsDetached) {
            this.mDetachedRegistrants.notifyRegistrants();
        }
        if (hasRilDataRadioTechnologyChanged || hasRilVoiceRadioTechnologyChanged) {
            logRatChange();
        }
        if (hasDataRegStateChanged || hasRilDataRadioTechnologyChanged) {
            notifyDataRegStateRilRadioTechnologyChanged();
            if (18 == this.mSS.getRilDataRadioTechnology()) {
                this.mPhone.notifyDataConnection(PhoneInternalInterface.REASON_IWLAN_AVAILABLE);
            } else {
                this.mPhone.notifyDataConnection(null);
            }
        }
        if (hasVoiceRoamingOn || hasVoiceRoamingOff || hasDataRoamingOn || hasDataRoamingOff) {
            logRoamingChange();
        }
        if (hasVoiceRoamingOn) {
            this.mVoiceRoamingOnRegistrants.notifyRegistrants();
        }
        if (hasVoiceRoamingOff) {
            this.mVoiceRoamingOffRegistrants.notifyRegistrants();
        }
        if (hasDataRoamingOn) {
            this.mDataRoamingOnRegistrants.notifyRegistrants();
        }
        if (hasDataRoamingOff) {
            this.mDataRoamingOffRegistrants.notifyRegistrants();
        } else if (((this.mNewSS.getRilDataRegState() == 1 && (this.mSS.getRilDataRegState() == 1 || this.mSS.getRilDataRegState() == 5)) || (this.mSS.getRilDataRegState() == 5 && !this.mDataRoaming)) && this.mPsRegStateRaw == 5) {
            log("recover setup data for roaming off. OldDataRegState:" + this.mNewSS.getRilDataRegState() + " NewDataRegState:" + this.mSS.getRilDataRegState() + " NewRoamingState:" + this.mSS.getRoaming() + " NewDataRoamingState:" + this.mDataRoaming + " PsRegState:" + this.mPsRegStateRaw);
            this.mPsRegStateRaw = 1;
            if (!this.mSS.getRoaming()) {
                this.mDataRoamingOffRegistrants.notifyRegistrants();
            }
        }
        if (hasDataRoamingTypeChanged) {
            this.mDataRoamingTypeChangedRegistrants.notifyRegistrants();
        }
        if (hasLocationChanged) {
            this.mPhone.notifyLocationChanged();
        }
        if (isGprsConsistent(this.mSS.getDataRegState(), this.mSS.getVoiceRegState())) {
            this.mReportedGprsNoReg = false;
        } else if (!(this.mStartedGprsRegCheck || this.mReportedGprsNoReg)) {
            this.mStartedGprsRegCheck = true;
            sendMessageDelayed(obtainMessage(22), (long) Global.getInt(this.mPhone.getContext().getContentResolver(), "gprs_register_check_period_ms", DEFAULT_GPRS_CHECK_PERIOD_MILLIS));
        }
    }

    protected void pollStateDoneCdma() {
        boolean hasRegistered;
        boolean hasDataRoamingTypeChanged;
        updateRoamingState();
        useDataRegStateForDataOnlyDevices();
        resetServiceStateInIwlanMode();
        if (DBG) {
            log("pollStateDone: cdma oldSS=[" + this.mSS + "] newSS=[" + this.mNewSS + "]");
        }
        if (this.mSS.getVoiceRegState() == 0) {
            hasRegistered = false;
        } else if (this.mNewSS.getVoiceRegState() == 0) {
            hasRegistered = true;
        } else {
            hasRegistered = false;
        }
        boolean hasCdmaDataConnectionAttached = (this.mSS.getDataRegState() != 0 || this.mOppoNeedNotify) ? this.mNewSS.getDataRegState() == 0 : false;
        boolean hasCdmaDataConnectionDetached = this.mSS.getDataRegState() == 0 ? this.mNewSS.getDataRegState() != 0 : false;
        boolean hasCdmaDataConnectionChanged = this.mSS.getDataRegState() != this.mNewSS.getDataRegState();
        boolean hasLocationChanged = !this.mNewCellLoc.equals(this.mCellLoc);
        if (!hasLocationChanged) {
            this.mRatRatcheter.ratchetRat(this.mSS, this.mNewSS);
        }
        boolean hasRilVoiceRadioTechnologyChanged = this.mSS.getRilVoiceRadioTechnology() != this.mNewSS.getRilVoiceRadioTechnology();
        boolean hasRilDataRadioTechnologyChanged = this.mSS.getRilDataRadioTechnology() != this.mNewSS.getRilDataRadioTechnology();
        boolean hasChanged = this.mNewSS.equals(this.mSS) ? this.mOppoNeedNotify : true;
        boolean hasVoiceRoamingOn = !this.mSS.getVoiceRoaming() ? this.mNewSS.getVoiceRoaming() : false;
        boolean hasVoiceRoamingOff = this.mSS.getVoiceRoaming() && !this.mNewSS.getVoiceRoaming();
        boolean hasDataRoamingOn = !this.mSS.getDataRoaming() ? this.mNewSS.getDataRoaming() : false;
        boolean hasDataRoamingOff = this.mSS.getDataRoaming() && !this.mNewSS.getDataRoaming();
        if (this.mSS.getDataRoaming() && this.mNewSS.getDataRoaming()) {
            hasDataRoamingTypeChanged = this.mSS.getDataRoamingType() != this.mNewSS.getDataRoamingType();
        } else {
            hasDataRoamingTypeChanged = false;
        }
        TelephonyManager tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
        if (!(this.mSS.getVoiceRegState() == this.mNewSS.getVoiceRegState() && this.mSS.getDataRegState() == this.mNewSS.getDataRegState())) {
            Integer[] numArr = new Object[4];
            numArr[0] = Integer.valueOf(this.mSS.getVoiceRegState());
            numArr[1] = Integer.valueOf(this.mSS.getDataRegState());
            numArr[2] = Integer.valueOf(this.mNewSS.getVoiceRegState());
            numArr[3] = Integer.valueOf(this.mNewSS.getDataRegState());
            EventLog.writeEvent(EventLogTags.CDMA_SERVICE_STATE_CHANGE, numArr);
        }
        ServiceState tss = this.mSS;
        this.mSS = this.mNewSS;
        this.mNewSS = tss;
        this.mNewSS.setStateOutOfService();
        CellLocation tcl = (CdmaCellLocation) this.mCellLoc;
        this.mCellLoc = this.mNewCellLoc;
        this.mNewCellLoc = tcl;
        if (hasRilVoiceRadioTechnologyChanged) {
            updatePhoneObject();
        }
        if (hasRilDataRadioTechnologyChanged) {
            tm.setDataNetworkTypeForPhone(this.mPhone.getPhoneId(), this.mSS.getRilDataRadioTechnology());
            if (18 == this.mSS.getRilDataRadioTechnology()) {
                log("pollStateDone: IWLAN enabled");
            }
        }
        if (hasRegistered) {
            this.mNetworkAttachedRegistrants.notifyRegistrants();
        }
        if (hasChanged) {
            this.mOppoNeedNotify = false;
            updateSpnDisplay();
            tm.setNetworkOperatorNameForPhone(this.mPhone.getPhoneId(), this.mSS.getOperatorAlphaLong());
            String prevOperatorNumeric = tm.getNetworkOperatorForPhone(this.mPhone.getPhoneId());
            String operatorNumeric = this.mSS.getOperatorNumeric();
            if (isInvalidOperatorNumeric(operatorNumeric)) {
                operatorNumeric = fixUnknownMcc(operatorNumeric, this.mSS.getSystemId());
            }
            tm.setNetworkOperatorNumericForPhone(this.mPhone.getPhoneId(), operatorNumeric);
            updateCarrierMccMncConfiguration(operatorNumeric, prevOperatorNumeric, this.mPhone.getContext());
            if (isInvalidOperatorNumeric(operatorNumeric)) {
                if (DBG) {
                    log("operatorNumeric " + operatorNumeric + "is invalid");
                }
                tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), UsimPBMemInfo.STRING_NOT_SET);
                this.mGotCountryCode = false;
            } else {
                String isoCountryCode = UsimPBMemInfo.STRING_NOT_SET;
                String mcc = operatorNumeric.substring(0, 3);
                try {
                    isoCountryCode = MccTable.countryCodeForMcc(Integer.parseInt(operatorNumeric.substring(0, 3)));
                } catch (NumberFormatException ex) {
                    loge("pollStateDone: countryCodeForMcc error" + ex);
                } catch (StringIndexOutOfBoundsException ex2) {
                    loge("pollStateDone: countryCodeForMcc error" + ex2);
                }
                tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), isoCountryCode);
                this.mGotCountryCode = true;
                setOperatorIdd(operatorNumeric);
                if (shouldFixTimeZoneNow(this.mPhone, operatorNumeric, prevOperatorNumeric, this.mNeedFixZoneAfterNitz)) {
                    fixTimeZone(isoCountryCode);
                }
            }
            tm.setNetworkRoamingForPhone(this.mPhone.getPhoneId(), !this.mSS.getVoiceRoaming() ? this.mSS.getDataRoaming() : true);
            setRoamingType(this.mSS);
            log("Broadcasting ServiceState : " + this.mSS);
            this.mPhone.notifyServiceStateChanged(this.mSS);
            TelephonyMetrics.getInstance().writeServiceStateChanged(this.mPhone.getPhoneId(), this.mSS);
            if (hasRegistered || hasCdmaDataConnectionAttached || hasRilDataRadioTechnologyChanged || hasRilVoiceRadioTechnologyChanged) {
                if (DBG) {
                    log("Servicestate rat changed should update and notify signal");
                }
                updateOEMSmooth(this.mSS);
            }
        }
        if (hasCdmaDataConnectionAttached || hasCdmaDataConnectionDetached || hasRegistered) {
            logAttachChange();
        }
        if (hasCdmaDataConnectionAttached) {
            this.mAttachedRegistrants.notifyRegistrants();
        }
        if (hasCdmaDataConnectionDetached) {
            this.mDetachedRegistrants.notifyRegistrants();
        }
        if (hasRilDataRadioTechnologyChanged || hasRilVoiceRadioTechnologyChanged) {
            logRatChange();
        }
        if (hasCdmaDataConnectionChanged || hasRilDataRadioTechnologyChanged) {
            notifyDataRegStateRilRadioTechnologyChanged();
            if (18 == this.mSS.getRilDataRadioTechnology()) {
                this.mPhone.notifyDataConnection(PhoneInternalInterface.REASON_IWLAN_AVAILABLE);
            } else {
                this.mPhone.notifyDataConnection(null);
            }
        }
        if (hasVoiceRoamingOn) {
            this.mVoiceRoamingOnRegistrants.notifyRegistrants();
        }
        if (hasVoiceRoamingOff) {
            this.mVoiceRoamingOffRegistrants.notifyRegistrants();
        }
        if (hasVoiceRoamingOn || hasVoiceRoamingOff || hasDataRoamingOn || hasDataRoamingOff) {
            logRoamingChange();
        }
        if (hasDataRoamingOn) {
            this.mDataRoamingOnRegistrants.notifyRegistrants();
        }
        if (hasDataRoamingOff) {
            this.mDataRoamingOffRegistrants.notifyRegistrants();
        }
        if (hasDataRoamingTypeChanged) {
            this.mDataRoamingTypeChangedRegistrants.notifyRegistrants();
        }
        if (hasLocationChanged) {
            this.mPhone.notifyLocationChanged();
        }
    }

    protected void pollStateDoneCdmaLte() {
        boolean hasDeregistered;
        boolean hasDataRoamingTypeChanged;
        boolean has4gHandoff;
        updateRoamingState();
        if (Build.IS_DEBUGGABLE && SystemProperties.getBoolean(PROP_FORCE_ROAMING, false)) {
            this.mNewSS.setVoiceRoaming(true);
            this.mNewSS.setDataRoaming(true);
        }
        useDataRegStateForDataOnlyDevices();
        resetServiceStateInIwlanMode();
        log("pollStateDone: lte 1 ss=[" + this.mSS + "] newSS=[" + this.mNewSS + "]");
        boolean hasRegistered = this.mSS.getVoiceRegState() != 0 ? this.mNewSS.getVoiceRegState() == 0 : false;
        if (this.mSS.getVoiceRegState() != 0) {
            hasDeregistered = false;
        } else if (this.mNewSS.getVoiceRegState() != 0) {
            hasDeregistered = true;
        } else {
            hasDeregistered = false;
        }
        boolean hasCdmaDataConnectionAttached = (this.mSS.getDataRegState() != 0 || this.mOppoNeedNotify) ? this.mNewSS.getDataRegState() == 0 : false;
        boolean hasCdmaDataConnectionDetached = this.mSS.getDataRegState() == 0 ? this.mNewSS.getDataRegState() != 0 : false;
        boolean hasCdmaDataConnectionChanged = this.mSS.getDataRegState() != this.mNewSS.getDataRegState();
        boolean hasLocationChanged = !this.mNewCellLoc.equals(this.mCellLoc);
        if (!hasLocationChanged) {
            this.mRatRatcheter.ratchetRat(this.mSS, this.mNewSS);
        }
        boolean hasVoiceRadioTechnologyChanged = this.mSS.getRilVoiceRadioTechnology() != this.mNewSS.getRilVoiceRadioTechnology();
        boolean hasDataRadioTechnologyChanged = this.mSS.getRilDataRadioTechnology() != this.mNewSS.getRilDataRadioTechnology();
        boolean hasChanged = this.mNewSS.equals(this.mSS) ? this.mOppoNeedNotify : true;
        boolean hasVoiceRoamingOn = !this.mSS.getVoiceRoaming() ? this.mNewSS.getVoiceRoaming() : false;
        boolean hasVoiceRoamingOff = this.mSS.getVoiceRoaming() && !this.mNewSS.getVoiceRoaming();
        boolean hasDataRoamingOn = !this.mSS.getDataRoaming() ? this.mNewSS.getDataRoaming() : false;
        boolean hasDataRoamingOff = this.mSS.getDataRoaming() && !this.mNewSS.getDataRoaming();
        if (this.mSS.getDataRoaming() && this.mNewSS.getDataRoaming()) {
            hasDataRoamingTypeChanged = this.mSS.getDataRoamingType() != this.mNewSS.getDataRoamingType();
        } else {
            hasDataRoamingTypeChanged = false;
        }
        if (this.mNewSS.getDataRegState() != 0) {
            has4gHandoff = false;
        } else if (ServiceState.isLte(this.mSS.getRilDataRadioTechnology()) && this.mNewSS.getRilDataRadioTechnology() == 13) {
            has4gHandoff = true;
        } else if (this.mSS.getRilDataRadioTechnology() == 13) {
            has4gHandoff = ServiceState.isLte(this.mNewSS.getRilDataRadioTechnology());
        } else {
            has4gHandoff = false;
        }
        boolean hasMultiApnSupport = (ServiceState.isLte(this.mNewSS.getRilDataRadioTechnology()) || this.mNewSS.getRilDataRadioTechnology() == 13) ? !ServiceState.isLte(this.mSS.getRilDataRadioTechnology()) ? this.mSS.getRilDataRadioTechnology() != 13 : false : false;
        boolean hasLostMultiApnSupport = this.mNewSS.getRilDataRadioTechnology() >= 4 ? this.mNewSS.getRilDataRadioTechnology() <= 8 : false;
        boolean hasLteDataDetach = ServiceState.isLte(this.mSS.getRilDataRadioTechnology()) ? !ServiceState.isLte(this.mNewSS.getRilDataRadioTechnology()) : false;
        TelephonyManager tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
        if (DBG) {
            log("pollStateDone: hasRegistered=" + hasRegistered + " hasDeegistered=" + hasDeregistered + " hasCdmaDataConnectionAttached=" + hasCdmaDataConnectionAttached + " hasCdmaDataConnectionDetached=" + hasCdmaDataConnectionDetached + " hasCdmaDataConnectionChanged=" + hasCdmaDataConnectionChanged + " hasVoiceRadioTechnologyChanged= " + hasVoiceRadioTechnologyChanged + " hasDataRadioTechnologyChanged=" + hasDataRadioTechnologyChanged + " hasChanged=" + hasChanged + " hasVoiceRoamingOn=" + hasVoiceRoamingOn + " hasVoiceRoamingOff=" + hasVoiceRoamingOff + " hasDataRoamingOn=" + hasDataRoamingOn + " hasDataRoamingOff=" + hasDataRoamingOff + " hasLocationChanged=" + hasLocationChanged + " has4gHandoff = " + has4gHandoff + " hasMultiApnSupport=" + hasMultiApnSupport + " hasLostMultiApnSupport=" + hasLostMultiApnSupport + " hasDataRoamingTypeChanged=" + hasDataRoamingTypeChanged);
        }
        if (!(this.mSS.getVoiceRegState() == this.mNewSS.getVoiceRegState() && this.mSS.getDataRegState() == this.mNewSS.getDataRegState())) {
            Integer[] numArr = new Object[4];
            numArr[0] = Integer.valueOf(this.mSS.getVoiceRegState());
            numArr[1] = Integer.valueOf(this.mSS.getDataRegState());
            numArr[2] = Integer.valueOf(this.mNewSS.getVoiceRegState());
            numArr[3] = Integer.valueOf(this.mNewSS.getDataRegState());
            EventLog.writeEvent(EventLogTags.CDMA_SERVICE_STATE_CHANGE, numArr);
        }
        int oldRilDataRadioTechnology = this.mSS.getRilDataRadioTechnology();
        ServiceState tss = this.mSS;
        this.mSS = this.mNewSS;
        this.mNewSS = tss;
        this.mNewSS.setStateOutOfService();
        CellLocation tcl = (CdmaCellLocation) this.mCellLoc;
        this.mCellLoc = this.mNewCellLoc;
        this.mNewCellLoc = tcl;
        this.mNewSS.setStateOutOfService();
        if (hasVoiceRadioTechnologyChanged) {
            updatePhoneObject();
        }
        if (hasDataRadioTechnologyChanged) {
            tm.setDataNetworkTypeForPhone(this.mPhone.getPhoneId(), this.mSS.getRilDataRadioTechnology());
            if (18 == this.mSS.getRilDataRadioTechnology()) {
                log("pollStateDone: IWLAN enabled");
            }
            if (oldRilDataRadioTechnology == 14 || this.mSS.getRilDataRadioTechnology() == 14) {
                log("[CDMALTE]pollStateDone: update signal for RAT switch between diff group");
                sendMessage(obtainMessage(10));
            }
        }
        if (hasRegistered) {
            this.mNetworkAttachedRegistrants.notifyRegistrants();
        }
        if (hasChanged || this.mEriTriggeredPollState) {
            this.mOppoNeedNotify = false;
            if (this.mEriTriggeredPollState) {
                this.mEriTriggeredPollState = false;
            }
            updateSpnDisplay();
            tm.setNetworkOperatorNameForPhone(this.mPhone.getPhoneId(), this.mSS.getOperatorAlphaLong());
            String prevOperatorNumeric = tm.getNetworkOperatorForPhone(this.mPhone.getPhoneId());
            String operatorNumeric = this.mSS.getOperatorNumeric();
            if (isInvalidOperatorNumeric(operatorNumeric)) {
                operatorNumeric = fixUnknownMcc(operatorNumeric, this.mSS.getSystemId());
            }
            tm.setNetworkOperatorNumericForPhone(this.mPhone.getPhoneId(), operatorNumeric);
            updateCarrierMccMncConfiguration(operatorNumeric, prevOperatorNumeric, this.mPhone.getContext());
            if (isInvalidOperatorNumeric(operatorNumeric)) {
                if (DBG) {
                    log("operatorNumeric is null");
                }
                tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), UsimPBMemInfo.STRING_NOT_SET);
                this.mGotCountryCode = false;
            } else {
                String isoCountryCode = UsimPBMemInfo.STRING_NOT_SET;
                String mcc = operatorNumeric.substring(0, 3);
                try {
                    isoCountryCode = MccTable.countryCodeForMcc(Integer.parseInt(operatorNumeric.substring(0, 3)));
                } catch (NumberFormatException ex) {
                    loge("countryCodeForMcc error" + ex);
                } catch (StringIndexOutOfBoundsException ex2) {
                    loge("countryCodeForMcc error" + ex2);
                }
                tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), isoCountryCode);
                this.mGotCountryCode = true;
                setOperatorIdd(operatorNumeric);
                if (shouldFixTimeZoneNow(this.mPhone, operatorNumeric, prevOperatorNumeric, this.mNeedFixZoneAfterNitz)) {
                    fixTimeZone(isoCountryCode);
                }
            }
            tm.setNetworkRoamingForPhone(this.mPhone.getPhoneId(), !this.mSS.getVoiceRoaming() ? this.mSS.getDataRoaming() : true);
            setRoamingType(this.mSS);
            log("Broadcasting ServiceState : " + this.mSS);
            this.mPhone.notifyServiceStateChanged(this.mSS);
            TelephonyMetrics.getInstance().writeServiceStateChanged(this.mPhone.getPhoneId(), this.mSS);
            if (hasRegistered || hasCdmaDataConnectionAttached || hasDataRadioTechnologyChanged || hasVoiceRadioTechnologyChanged) {
                if (DBG) {
                    log("Servicestate rat changed should update and notify signal");
                }
                updateOEMSmooth(this.mSS);
            }
            if (hasLteDataDetach) {
                if (DBG) {
                    log("CdmaLte hasLteDataDetach so the value of isGSM will change from TRUE to FALSE");
                }
                this.mCi.getSignalStrength(obtainMessage(3));
            }
        }
        if (hasCdmaDataConnectionAttached || has4gHandoff || hasCdmaDataConnectionDetached || hasRegistered || hasDeregistered) {
            logAttachChange();
        }
        if (hasCdmaDataConnectionAttached || has4gHandoff) {
            this.mAttachedRegistrants.notifyRegistrants();
        }
        if (hasCdmaDataConnectionDetached) {
            this.mDetachedRegistrants.notifyRegistrants();
        }
        if (hasDataRadioTechnologyChanged || hasVoiceRadioTechnologyChanged) {
            logRatChange();
        }
        if (hasCdmaDataConnectionChanged || hasDataRadioTechnologyChanged) {
            notifyDataRegStateRilRadioTechnologyChanged();
            if (18 == this.mSS.getRilDataRadioTechnology()) {
                this.mPhone.notifyDataConnection(PhoneInternalInterface.REASON_IWLAN_AVAILABLE);
            } else {
                this.mPhone.notifyDataConnection(null);
            }
        }
        if (hasVoiceRoamingOn || hasVoiceRoamingOff || hasDataRoamingOn || hasDataRoamingOff) {
            logRoamingChange();
        }
        if (hasVoiceRoamingOn) {
            this.mVoiceRoamingOnRegistrants.notifyRegistrants();
        }
        if (hasVoiceRoamingOff) {
            this.mVoiceRoamingOffRegistrants.notifyRegistrants();
        }
        if (hasDataRoamingOn) {
            this.mDataRoamingOnRegistrants.notifyRegistrants();
        }
        if (hasDataRoamingOff) {
            this.mDataRoamingOffRegistrants.notifyRegistrants();
        }
        if (hasDataRoamingTypeChanged) {
            this.mDataRoamingTypeChangedRegistrants.notifyRegistrants();
        }
        if (hasLocationChanged) {
            this.mPhone.notifyLocationChanged();
        }
    }

    private void updateOperatorNameFromEri() {
        boolean hasBrandOverride = false;
        String eriText;
        if (this.mPhone.isPhoneTypeCdma()) {
            if (this.mCi.getRadioState().isOn() && !this.mIsSubscriptionFromRuim) {
                if (this.mSS.getVoiceRegState() == 0) {
                    eriText = this.mPhone.getCdmaEriText();
                } else {
                    eriText = this.mPhone.getContext().getText(17039597).toString();
                }
                this.mSS.setOperatorAlphaLong(eriText);
            }
        } else if (this.mPhone.isPhoneTypeCdmaLte()) {
            if (!(this.mUiccController.getUiccCard(getPhoneId()) == null || this.mUiccController.getUiccCard(getPhoneId()).getOperatorBrandOverride() == null)) {
                hasBrandOverride = true;
            }
            if (!hasBrandOverride && this.mCi.getRadioState().isOn() && this.mPhone.isEriFileLoaded() && ((!ServiceState.isLte(this.mSS.getRilVoiceRadioTechnology()) || this.mPhone.getContext().getResources().getBoolean(17957024)) && !this.mIsSubscriptionFromRuim)) {
                eriText = this.mSS.getOperatorAlphaLong();
                if (this.mSS.getVoiceRegState() == 0) {
                    eriText = this.mPhone.getCdmaEriText();
                } else if (this.mSS.getVoiceRegState() == 3) {
                    if (this.mIccRecords != null) {
                        eriText = this.mIccRecords.getServiceProviderName();
                    } else {
                        eriText = null;
                    }
                    if (TextUtils.isEmpty(eriText)) {
                        eriText = SystemProperties.get("ro.cdma.home.operator.alpha");
                    }
                } else if (this.mSS.getDataRegState() != 0) {
                    eriText = this.mPhone.getContext().getText(17039597).toString();
                }
                this.mSS.setOperatorAlphaLong(eriText);
            }
            if (this.mUiccApplcation != null && this.mUiccApplcation.getState() == AppState.APPSTATE_READY && this.mIccRecords != null && this.mSS.getVoiceRegState() == 0 && !ServiceState.isLte(this.mSS.getRilVoiceRadioTechnology())) {
                boolean showSpn = ((RuimRecords) this.mIccRecords).getCsimSpnDisplayCondition();
                int iconIndex = this.mSS.getCdmaEriIconIndex();
                if (showSpn && iconIndex == 1 && isInHomeSidNid(this.mSS.getSystemId(), this.mSS.getNetworkId()) && this.mIccRecords != null) {
                    this.mSS.setOperatorAlphaLong(this.mIccRecords.getServiceProviderName());
                }
            }
        }
    }

    private boolean isInHomeSidNid(int sid, int nid) {
        if (isSidsAllZeros() || this.mHomeSystemId.length != this.mHomeNetworkId.length || sid == 0) {
            return true;
        }
        int i = 0;
        while (i < this.mHomeSystemId.length) {
            if (this.mHomeSystemId[i] == sid && (this.mHomeNetworkId[i] == 0 || this.mHomeNetworkId[i] == 65535 || nid == 0 || nid == 65535 || this.mHomeNetworkId[i] == nid)) {
                return true;
            }
            i++;
        }
        return false;
    }

    protected void setOperatorIdd(String operatorNumeric) {
        String idd = UsimPBMemInfo.STRING_NOT_SET;
        try {
            idd = this.mHbpcdUtils.getIddByMcc(Integer.parseInt(operatorNumeric.substring(0, 3)));
        } catch (NumberFormatException ex) {
            loge("setOperatorIdd: idd error" + ex);
        } catch (StringIndexOutOfBoundsException ex2) {
            loge("setOperatorIdd: idd error" + ex2);
        }
        if (idd == null || idd.isEmpty()) {
            this.mPhone.setSystemProperty("gsm.operator.idpstring", "+");
        } else {
            this.mPhone.setSystemProperty("gsm.operator.idpstring", idd);
        }
    }

    protected boolean isInvalidOperatorNumeric(String operatorNumeric) {
        if (operatorNumeric == null || operatorNumeric.length() < 5) {
            return true;
        }
        return operatorNumeric.startsWith(INVALID_MCC);
    }

    protected String fixUnknownMcc(String operatorNumeric, int sid) {
        int i = 0;
        if (sid <= 0) {
            return operatorNumeric;
        }
        boolean isNitzTimeZone = false;
        int timeZone = 0;
        if (this.mSavedTimeZone != null) {
            timeZone = TimeZone.getTimeZone(this.mSavedTimeZone).getRawOffset() / 3600000;
            isNitzTimeZone = true;
        } else {
            TimeZone tzone = getNitzTimeZone(this.mZoneOffset, this.mZoneDst, this.mZoneTime);
            if (tzone != null) {
                timeZone = tzone.getRawOffset() / 3600000;
            }
        }
        HbpcdUtils hbpcdUtils = this.mHbpcdUtils;
        if (this.mZoneDst) {
            i = 1;
        }
        int mcc = hbpcdUtils.getMcc(sid, timeZone, i, isNitzTimeZone);
        if (mcc > 0) {
            operatorNumeric = Integer.toString(mcc) + DEFAULT_MNC;
        }
        return operatorNumeric;
    }

    protected void fixTimeZone(String isoCountryCode) {
        TimeZone zone;
        String zoneName = SystemProperties.get(TIMEZONE_PROPERTY);
        if (DBG) {
            log("fixTimeZone zoneName='" + zoneName + "' mZoneOffset=" + this.mZoneOffset + " mZoneDst=" + this.mZoneDst + " iso-cc='" + isoCountryCode + "' iso-cc-idx=" + Arrays.binarySearch(GMT_COUNTRY_CODES, isoCountryCode));
        }
        if (UsimPBMemInfo.STRING_NOT_SET.equals(isoCountryCode) && this.mNeedFixZoneAfterNitz) {
            zone = getNitzTimeZone(this.mZoneOffset, this.mZoneDst, this.mZoneTime);
            if (DBG) {
                log("pollStateDone: using NITZ TimeZone");
            }
        } else if (this.mZoneOffset != 0 || this.mZoneDst || zoneName == null || zoneName.length() <= 0 || Arrays.binarySearch(GMT_COUNTRY_CODES, isoCountryCode) >= 0) {
            zone = TimeUtils.getTimeZone(this.mZoneOffset, this.mZoneDst, this.mZoneTime, isoCountryCode);
            if (DBG) {
                log("fixTimeZone: using getTimeZone(off, dst, time, iso)");
            }
        } else {
            zone = TimeZone.getDefault();
            if (this.mPhone.isPhoneTypeGsm() && isAllowFixTimeZone() && oppoFixZoneWithoutNitz(this.mcc)) {
                try {
                    String mccTz = getTimeZonesByMcc(this.mcc);
                    if (mccTz == null) {
                        mccTz = MccTable.defaultTimeZoneForMcc(Integer.parseInt(this.mcc));
                    }
                    if (mccTz != null) {
                        zone = TimeZone.getTimeZone(mccTz);
                        if (DBG) {
                            log("pollStateDone: try to fixTimeZone mcc:" + this.mcc + " mccTz:" + mccTz + " zone.getID=" + zone.getID());
                        }
                    }
                } catch (Exception e) {
                    log("pollStateDone: parse error: mcc=" + this.mcc);
                }
            }
            if (this.mNeedFixZoneAfterNitz) {
                long ctm = System.currentTimeMillis();
                long tzOffset = (long) zone.getOffset(ctm);
                if (DBG) {
                    log("fixTimeZone: tzOffset=" + tzOffset + " ltod=" + TimeUtils.logTimeOfDay(ctm));
                }
                if (getAutoTime()) {
                    long adj = ctm - tzOffset;
                    if (DBG) {
                        log("fixTimeZone: adj ltod=" + TimeUtils.logTimeOfDay(adj));
                    }
                    setAndBroadcastNetworkSetTime(adj);
                } else {
                    this.mSavedTime -= tzOffset;
                    if (DBG) {
                        log("fixTimeZone: adj mSavedTime=" + this.mSavedTime);
                    }
                }
            }
            if (DBG) {
                log("fixTimeZone: using default TimeZone");
            }
        }
        this.mNeedFixZoneAfterNitz = false;
        if (zone != null) {
            log("fixTimeZone: zone != null zone.getID=" + zone.getID());
            if (getAutoTimeZone()) {
                setAndBroadcastNetworkSetTimeZone(zone.getID());
            } else {
                log("fixTimeZone: skip changing zone as getAutoTimeZone was false");
            }
            saveNitzTimeZone(zone.getID());
            if (this.mPhone.isPhoneTypeCdma() || this.mPhone.isPhoneTypeCdmaLte()) {
                TelephonyManager.setTelephonyProperty(this.mPhone.getPhoneId(), "cdma.operator.nitztimezoneid", zone.getID());
                return;
            }
            return;
        }
        log("fixTimeZone: zone == null, do nothing for zone");
    }

    private boolean isGprsConsistent(int dataRegState, int voiceRegState) {
        return voiceRegState != 0 || dataRegState == 0;
    }

    private TimeZone getNitzTimeZone(int offset, boolean dst, long when) {
        TimeZone guess = findTimeZone(offset, dst, when);
        if (guess == null) {
            guess = findTimeZone(offset, !dst, when);
        }
        if (DBG) {
            log("getNitzTimeZone returning " + (guess == null ? guess : guess.getID()));
        }
        return guess;
    }

    private TimeZone findTimeZone(int offset, boolean dst, long when) {
        log("[NITZ],findTimeZone,offset:" + offset + ",dst:" + dst + ",when:" + when);
        int rawOffset = offset;
        if (dst) {
            rawOffset = offset - 3600000;
        }
        String[] zones = TimeZone.getAvailableIDs(rawOffset);
        Date d = new Date(when);
        for (String zone : zones) {
            TimeZone tz = TimeZone.getTimeZone(zone);
            if (tz.getOffset(when) == offset && tz.inDaylightTime(d) == dst) {
                TimeZone guess = tz;
                log("[NITZ],find time zone.");
                return guess;
            }
        }
        return null;
    }

    private int regCodeToServiceState(int code) {
        switch (code) {
            case 0:
            case 2:
            case 3:
            case 4:
            case 10:
            case 12:
            case 13:
            case 14:
                return 1;
            case 1:
            case 5:
                return 0;
            default:
                loge("regCodeToServiceState: unexpected service state " + code);
                return 1;
        }
    }

    private int regCodeToRegState(int code) {
        switch (code) {
            case 10:
                return 0;
            case 12:
                return 2;
            case 13:
                return 3;
            case 14:
                return 4;
            default:
                return code;
        }
    }

    private String getSIMOperatorNumeric() {
        IccRecords r = this.mIccRecords;
        if (r == null) {
            return null;
        }
        String mccmnc = r.getOperatorNumeric();
        if (mccmnc == null) {
            String imsi = r.getIMSI();
            if (!(imsi == null || imsi.equals(UsimPBMemInfo.STRING_NOT_SET))) {
                mccmnc = imsi.substring(0, 5);
                log("get MCC/MNC from IMSI = " + mccmnc);
            }
        }
        if (this.mPhone.isPhoneTypeGsm() && (mccmnc == null || mccmnc.equals(UsimPBMemInfo.STRING_NOT_SET))) {
            String SimMccMncProp = "gsm.ril.uicc.mccmnc";
            if (this.mPhone.getPhoneId() != 0) {
                SimMccMncProp = SimMccMncProp + "." + this.mPhone.getPhoneId();
            }
            mccmnc = SystemProperties.get(SimMccMncProp, UsimPBMemInfo.STRING_NOT_SET);
            log("get MccMnc from property(" + SimMccMncProp + "): " + mccmnc);
        }
        return mccmnc;
    }

    private boolean regCodeIsRoaming(int code) {
        boolean z = true;
        if (this.mPhone.isPhoneTypeGsm()) {
            boolean isRoaming = false;
            String strHomePlmn = getSIMOperatorNumeric();
            String strServingPlmn = this.mNewSS.getOperatorNumeric();
            if (!SystemProperties.get("ro.mtk_bsp_package").equals("1")) {
                String simType = PhoneFactory.getPhone(this.mPhone.getPhoneId()).getIccCard().getIccCardType();
                if (!(strServingPlmn == null || strHomePlmn == null || simType == null)) {
                    try {
                        if (!simType.equals(UsimPBMemInfo.STRING_NOT_SET)) {
                            if (simType.equals("CSIM") && this.mServiceStateExt.isRoamingForSpecialSIM(strServingPlmn, strHomePlmn)) {
                                return true;
                            }
                        }
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (5 == code) {
                isRoaming = true;
            }
            if (isRoaming && strServingPlmn != null && strHomePlmn != null) {
                log("strServingPlmn = " + strServingPlmn + " strHomePlmn = " + strHomePlmn);
                for (int i = 0; i < customEhplmn.length; i++) {
                    boolean isServingPlmnInGroup = false;
                    boolean isHomePlmnInGroup = false;
                    for (int j = 0; j < customEhplmn[i].length; j++) {
                        if (strServingPlmn.equals(customEhplmn[i][j])) {
                            isServingPlmnInGroup = true;
                        }
                        if (strHomePlmn.equals(customEhplmn[i][j])) {
                            isHomePlmnInGroup = true;
                        }
                    }
                    if (isServingPlmnInGroup && isHomePlmnInGroup) {
                        isRoaming = false;
                        log("Ignore roaming");
                        break;
                    }
                }
            }
            return isRoaming;
        }
        if (5 != code) {
            z = false;
        }
        return z;
    }

    private boolean isSameOperatorNameFromSimAndSS(ServiceState s) {
        String spn = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getSimOperatorNameForPhone(getPhoneId());
        String onsl = s.getOperatorAlphaLong();
        String onss = s.getOperatorAlphaShort();
        boolean equalsOnsl = false;
        boolean equalsOnss = false;
        if (!TextUtils.isEmpty(spn)) {
            if (isIgnoreCaseOperatorName()) {
                equalsOnsl = spn.equalsIgnoreCase(onsl);
                equalsOnss = spn.equalsIgnoreCase(onss);
            } else {
                equalsOnsl = spn.equals(onsl);
                equalsOnss = spn.equals(onss);
            }
        }
        if (equalsOnsl) {
            return true;
        }
        return equalsOnss;
    }

    private boolean isIgnoreCaseOperatorName() {
        String simNumeric = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getSimOperatorNumericForPhone(getPhoneId());
        if (TextUtils.isEmpty(simNumeric) || !"51505".equals(simNumeric)) {
            return true;
        }
        if (DBG) {
            log("Sun operaor not need ingore case");
        }
        return false;
    }

    private boolean isSameNamedOperators(ServiceState s) {
        return currentMccEqualsSimMcc(s) ? isSameOperatorNameFromSimAndSS(s) : false;
    }

    private boolean currentMccEqualsSimMcc(ServiceState s) {
        boolean equalsMcc = true;
        try {
            return ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getSimOperatorNumericForPhone(getPhoneId()).substring(0, 3).equals(s.getOperatorNumeric().substring(0, 3));
        } catch (Exception e) {
            return equalsMcc;
        }
    }

    private boolean isOperatorConsideredNonRoaming(ServiceState s) {
        String operatorNumeric = s.getOperatorNumeric();
        String[] numericArray = this.mPhone.getContext().getResources().getStringArray(17236029);
        if (numericArray.length == 0 || operatorNumeric == null) {
            return false;
        }
        for (String numeric : numericArray) {
            if (operatorNumeric.startsWith(numeric)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOperatorConsideredRoamingMtk(ServiceState s) {
        String operatorNumeric = s.getOperatorNumeric();
        String simOperatorNumeric = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getSimOperatorNumericForPhone(getPhoneId());
        if (customOperatorConsiderRoamingMcc.length == 0 || TextUtils.isEmpty(operatorNumeric) || TextUtils.isEmpty(simOperatorNumeric)) {
            return false;
        }
        for (String[] numerics : customOperatorConsiderRoamingMcc) {
            if (simOperatorNumeric.startsWith(numerics[0])) {
                for (int idx = 1; idx < numerics.length; idx++) {
                    if (operatorNumeric.startsWith(numerics[idx])) {
                        return true;
                    }
                }
                continue;
            }
        }
        return false;
    }

    private boolean isOperatorConsideredRoaming(ServiceState s) {
        String operatorNumeric = s.getOperatorNumeric();
        String[] numericArray = this.mPhone.getContext().getResources().getStringArray(17236030);
        if (numericArray.length == 0 || operatorNumeric == null) {
            return false;
        }
        for (String numeric : numericArray) {
            if (operatorNumeric.startsWith(numeric)) {
                return true;
            }
        }
        return false;
    }

    private void onRestrictedStateChanged(AsyncResult ar) {
        RestrictedState newRs = new RestrictedState();
        if (DBG) {
            log("onRestrictedStateChanged: E rs " + this.mRestrictedState);
        }
        if (ar.exception == null) {
            boolean z;
            int state = ar.result[0];
            if ((state & 1) != 0) {
                z = true;
            } else if ((state & 4) != 0) {
                z = true;
            } else {
                z = false;
            }
            newRs.setCsEmergencyRestricted(z);
            if (this.mUiccApplcation != null && this.mUiccApplcation.getState() == AppState.APPSTATE_READY) {
                if ((state & 2) != 0) {
                    z = true;
                } else if ((state & 4) != 0) {
                    z = true;
                } else {
                    z = false;
                }
                newRs.setCsNormalRestricted(z);
                if ((state & 16) != 0) {
                    z = true;
                } else {
                    z = false;
                }
                newRs.setPsRestricted(z);
            } else if (this.mPhone.isPhoneTypeGsm()) {
                log("IccCard state Not ready ");
                if (this.mRestrictedState.isCsNormalRestricted() && (state & 2) == 0 && (state & 4) == 0) {
                    newRs.setCsNormalRestricted(false);
                }
                if (this.mRestrictedState.isPsRestricted() && (state & 16) == 0) {
                    newRs.setPsRestricted(false);
                }
            }
            if (DBG) {
                log("onRestrictedStateChanged: new rs " + newRs);
            }
            if (!this.mRestrictedState.isPsRestricted() && newRs.isPsRestricted()) {
                this.mPsRestrictEnabledRegistrants.notifyRegistrants();
                setNotification(1001);
            } else if (this.mRestrictedState.isPsRestricted() && !newRs.isPsRestricted()) {
                if (!this.mPhone.isPhoneTypeGsm()) {
                    this.mPsRestrictDisabledRegistrants.notifyRegistrants();
                    setNotification(1002);
                } else if (this.mPollingContext[0] != 0) {
                    this.mPendingPsRestrictDisabledNotify = true;
                } else {
                    this.mPsRestrictDisabledRegistrants.notifyRegistrants();
                    setNotification(1002);
                }
            }
            if (this.mRestrictedState.isCsRestricted()) {
                if (!newRs.isCsRestricted()) {
                    setNotification(1004);
                } else if (!newRs.isCsNormalRestricted()) {
                    setNotification(1006);
                } else if (!newRs.isCsEmergencyRestricted()) {
                    setNotification(1005);
                }
            } else if (!this.mRestrictedState.isCsEmergencyRestricted() || this.mRestrictedState.isCsNormalRestricted()) {
                if (this.mRestrictedState.isCsEmergencyRestricted() || !this.mRestrictedState.isCsNormalRestricted()) {
                    if (newRs.isCsRestricted()) {
                        setNotification(1003);
                    } else if (newRs.isCsEmergencyRestricted()) {
                        setNotification(1006);
                    } else if (newRs.isCsNormalRestricted()) {
                        setNotification(1005);
                    }
                } else if (!newRs.isCsRestricted()) {
                    setNotification(1004);
                } else if (newRs.isCsRestricted()) {
                    setNotification(1003);
                } else if (newRs.isCsEmergencyRestricted()) {
                    setNotification(1006);
                }
            } else if (!newRs.isCsRestricted()) {
                setNotification(1004);
            } else if (newRs.isCsRestricted()) {
                setNotification(1003);
            } else if (newRs.isCsNormalRestricted()) {
                setNotification(1005);
            }
            this.mRestrictedState = newRs;
        }
        log("onRestrictedStateChanged: X rs " + this.mRestrictedState);
    }

    public CellLocation getCellLocation() {
        if (((GsmCellLocation) this.mCellLoc).getLac() < 0 || ((GsmCellLocation) this.mCellLoc).getCid() < 0) {
            List<CellInfo> result = getAllCellInfo();
            if (result != null) {
                GsmCellLocation cellLocOther = new GsmCellLocation();
                for (CellInfo ci : result) {
                    if (ci instanceof CellInfoGsm) {
                        CellIdentityGsm cellIdentityGsm = ((CellInfoGsm) ci).getCellIdentity();
                        cellLocOther.setLacAndCid(cellIdentityGsm.getLac(), cellIdentityGsm.getCid());
                        cellLocOther.setPsc(cellIdentityGsm.getPsc());
                        if (DBG) {
                            log("getCellLocation(): X ret GSM info=" + cellLocOther);
                        }
                        return cellLocOther;
                    } else if (ci instanceof CellInfoWcdma) {
                        CellIdentityWcdma cellIdentityWcdma = ((CellInfoWcdma) ci).getCellIdentity();
                        cellLocOther.setLacAndCid(cellIdentityWcdma.getLac(), cellIdentityWcdma.getCid());
                        cellLocOther.setPsc(cellIdentityWcdma.getPsc());
                        if (DBG) {
                            log("getCellLocation(): X ret WCDMA info=" + cellLocOther);
                        }
                        return cellLocOther;
                    } else if ((ci instanceof CellInfoLte) && (cellLocOther.getLac() < 0 || cellLocOther.getCid() < 0)) {
                        CellIdentityLte cellIdentityLte = ((CellInfoLte) ci).getCellIdentity();
                        if (!(cellIdentityLte.getTac() == Integer.MAX_VALUE || cellIdentityLte.getCi() == Integer.MAX_VALUE)) {
                            cellLocOther.setLacAndCid(cellIdentityLte.getTac(), cellIdentityLte.getCi());
                            cellLocOther.setPsc(0);
                            if (DBG) {
                                log("getCellLocation(): possible LTE cellLocOther=" + cellLocOther);
                            }
                        }
                    }
                }
                if (DBG) {
                    log("getCellLocation(): X ret best answer cellLocOther=" + cellLocOther);
                }
                return cellLocOther;
            }
            if (DBG) {
                log("getCellLocation(): X empty mCellLoc and CellInfo mCellLoc=" + this.mCellLoc);
            }
            return this.mCellLoc;
        }
        if (DBG) {
            log("getCellLocation(): X good mCellLoc=" + this.mCellLoc);
        }
        return this.mCellLoc;
    }

    /* JADX WARNING: Missing block: B:102:0x04cb, code:
            if (r42.mZoneDst != (r8 != 0)) goto L_0x0365;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setTimeFromNITZString(String nitz, long nitzReceiveTime) {
        long start = SystemClock.elapsedRealtime();
        if (DBG) {
            log("NITZ: " + nitz + "," + nitzReceiveTime + " start=" + start + " delay=" + (start - nitzReceiveTime));
        }
        if ((!this.mPhone.isPhoneTypeCdma() && !this.mPhone.isPhoneTypeCdmaLte()) || nitz.length() > 0) {
            try {
                boolean hascard = this.mPhone.getIccCard().hasIccCard();
                log("hascard=" + hascard);
                if (!hascard) {
                    log("NITZ: stop for no card");
                    return;
                }
            } catch (Exception e) {
                log("uknown error");
            }
            long end;
            try {
                Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                c.clear();
                c.set(16, 0);
                String[] nitzSubs = nitz.split("[/:,+-]");
                int year = Integer.parseInt(nitzSubs[0]) + NITZ_UPDATE_DIFF_DEFAULT;
                if (year > MAX_NITZ_YEAR) {
                    if (DBG) {
                        loge("NITZ year: " + year + " exceeds limit, skip NITZ time update");
                    }
                    return;
                }
                c.set(1, year);
                int month = Integer.parseInt(nitzSubs[1]) - 1;
                c.set(2, month);
                int date = Integer.parseInt(nitzSubs[2]);
                c.set(5, date);
                int hour = Integer.parseInt(nitzSubs[3]);
                c.set(10, hour);
                int minute = Integer.parseInt(nitzSubs[4]);
                c.set(12, minute);
                int second = Integer.parseInt(nitzSubs[5]);
                c.set(13, second);
                boolean sign = nitz.indexOf(45) == -1;
                int tzOffset = Integer.parseInt(nitzSubs[6]);
                int dst = nitzSubs.length >= 8 ? Integer.parseInt(nitzSubs[7]) : 0;
                if (this.mPhone.isPhoneTypeCdma() || this.mPhone.isPhoneTypeCdmaLte()) {
                    int ltmoffset = (sign ? 1 : -1) * tzOffset;
                    if (DBG) {
                        log("[CDMA] NITZ: year = " + year + ", month = " + month + ", date = " + date + ", hour = " + hour + ", minute = " + minute + ", second = " + second + ", tzOffset = " + tzOffset + ", ltmoffset = " + ltmoffset + ", dst = " + dst);
                    }
                    TelephonyManager.setTelephonyProperty(this.mPhone.getPhoneId(), "cdma.operator.ltmoffset", Integer.toString(ltmoffset));
                }
                if (this.mPhone.isPhoneTypeGsm()) {
                    dst = nitzSubs.length >= 8 ? Integer.parseInt(nitzSubs[7]) : getDstForMcc(getMobileCountryCode(), c.getTimeInMillis());
                }
                tzOffset = ((((sign ? 1 : -1) * tzOffset) * 15) * 60) * 1000;
                TimeZone zone = null;
                if (nitzSubs.length >= 9) {
                    String tzname = nitzSubs[8].replace('!', '/');
                    zone = TimeZone.getTimeZone(tzname);
                    log("[NITZ] setTimeFromNITZString,tzname:" + tzname + " zone:" + zone);
                }
                String iso = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getNetworkCountryIsoForPhone(this.mPhone.getPhoneId());
                log("[NITZ] setTimeFromNITZString,mGotCountryCode:" + this.mGotCountryCode);
                if (zone == null && this.mGotCountryCode) {
                    if (iso == null || iso.length() <= 0) {
                        zone = getNitzTimeZone(tzOffset, dst != 0, c.getTimeInMillis());
                    } else {
                        zone = TimeUtils.getTimeZone(tzOffset, dst != 0, c.getTimeInMillis(), iso);
                    }
                }
                if (zone != null && this.mZoneOffset == tzOffset) {
                }
                this.mNeedFixZoneAfterNitz = true;
                this.mZoneOffset = tzOffset;
                this.mZoneDst = dst != 0;
                this.mZoneTime = c.getTimeInMillis();
                setReceivedNitz(this.mPhone.getPhoneId(), true);
                if (DBG) {
                    log("NITZ: tzOffset=" + tzOffset + " dst=" + dst + " zone=" + (zone != null ? zone.getID() : "NULL") + " iso=" + iso + " mGotCountryCode=" + this.mGotCountryCode + " mNeedFixZoneAfterNitz=" + this.mNeedFixZoneAfterNitz);
                }
                if (zone != null) {
                    if (getAutoTimeZone()) {
                        setAndBroadcastNetworkSetTimeZone(zone.getID());
                    }
                    saveNitzTimeZone(zone.getID());
                    if (this.mPhone.isPhoneTypeCdma() || this.mPhone.isPhoneTypeCdmaLte()) {
                        TelephonyManager.setTelephonyProperty(this.mPhone.getPhoneId(), "cdma.operator.nitztimezoneid", zone.getID());
                    }
                }
                String ignore = SystemProperties.get("gsm.ignore-nitz");
                if (ignore == null || !ignore.equals("yes")) {
                    this.mWakeLock.acquire();
                    if (!this.mPhone.isPhoneTypeGsm() || getAutoTime()) {
                        long millisSinceNitzReceived = SystemClock.elapsedRealtime() - nitzReceiveTime;
                        if (millisSinceNitzReceived < 0) {
                            if (DBG) {
                                log("NITZ: not setting time, clock has rolled backwards since NITZ time was received, " + nitz);
                            }
                            if (DBG) {
                                end = SystemClock.elapsedRealtime();
                                log("NITZ: end=" + end + " dur=" + (end - start));
                            }
                            this.mWakeLock.release();
                            return;
                        } else if (millisSinceNitzReceived > 2147483647L) {
                            if (DBG) {
                                log("NITZ: not setting time, processing has taken " + (millisSinceNitzReceived / 86400000) + " days");
                            }
                            if (DBG) {
                                end = SystemClock.elapsedRealtime();
                                log("NITZ: end=" + end + " dur=" + (end - start));
                            }
                            this.mWakeLock.release();
                            return;
                        } else {
                            c.add(14, (int) millisSinceNitzReceived);
                            if (DBG) {
                                log("NITZ: Setting time of day to " + c.getTime() + " NITZ receive delay(ms): " + millisSinceNitzReceived + " gained(ms): " + (c.getTimeInMillis() - System.currentTimeMillis()) + " from " + nitz);
                            }
                            if (getAutoTime()) {
                                long gained = c.getTimeInMillis() - System.currentTimeMillis();
                                long timeSinceLastUpdate = SystemClock.elapsedRealtime() - this.mSavedAtTime;
                                int nitzUpdateSpacing = Global.getInt(this.mCr, "nitz_update_spacing", this.mNitzUpdateSpacing);
                                int nitzUpdateDiff = Global.getInt(this.mCr, "nitz_update_diff", this.mNitzUpdateDiff);
                                if (this.mSavedAtTime != 0 && timeSinceLastUpdate <= ((long) nitzUpdateSpacing)) {
                                    if (Math.abs(gained) <= ((long) nitzUpdateDiff)) {
                                        if (DBG) {
                                            log("NITZ: ignore, a previous update was " + timeSinceLastUpdate + "ms ago and gained=" + gained + "ms");
                                        }
                                        if (DBG) {
                                            end = SystemClock.elapsedRealtime();
                                            log("NITZ: end=" + end + " dur=" + (end - start));
                                        }
                                        this.mWakeLock.release();
                                        return;
                                    }
                                }
                                if (DBG) {
                                    log("NITZ: Auto updating time of day to " + c.getTime() + " NITZ receive delay=" + millisSinceNitzReceived + "ms gained=" + gained + "ms from " + nitz);
                                }
                                setAndBroadcastNetworkSetTime(c.getTimeInMillis());
                            }
                        }
                    }
                    SystemProperties.set("gsm.nitz.time", String.valueOf(c.getTimeInMillis()));
                    saveNitzTime(c.getTimeInMillis());
                    this.mNitzUpdatedTime = true;
                    if (OemDeviceLock.IS_OP_LOCK && OemDeviceLock.getDeviceLockStatus()) {
                        OemDeviceLock.updateLockedTime(false, c.getTimeInMillis());
                    }
                    if (DBG) {
                        end = SystemClock.elapsedRealtime();
                        log("NITZ: end=" + end + " dur=" + (end - start));
                    }
                    this.mWakeLock.release();
                    return;
                }
                log("NITZ: Not setting clock because gsm.ignore-nitz is set");
            } catch (RuntimeException ex) {
                loge("NITZ: Parsing NITZ time " + nitz + " ex=" + ex);
            } catch (Throwable th) {
                if (DBG) {
                    end = SystemClock.elapsedRealtime();
                    log("NITZ: end=" + end + " dur=" + (end - start));
                }
                this.mWakeLock.release();
            }
        }
    }

    private boolean isAllowFixTimeZone() {
        for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++) {
            if (sReceiveNitz[i]) {
                log("Phone" + i + " has received NITZ!!");
                return false;
            }
        }
        log("Fix time zone allowed");
        return true;
    }

    private boolean getAutoTime() {
        boolean z = true;
        try {
            if (Global.getInt(this.mCr, "auto_time") <= 0) {
                z = false;
            }
            return z;
        } catch (SettingNotFoundException e) {
            return true;
        }
    }

    private boolean getAutoTimeZone() {
        boolean z = true;
        try {
            if (Global.getInt(this.mCr, "auto_time_zone") <= 0) {
                z = false;
            }
            return z;
        } catch (SettingNotFoundException e) {
            return true;
        }
    }

    private void saveNitzTimeZone(String zoneId) {
        log("saveNitzTimeZone zoneId:" + zoneId);
        this.mSavedTimeZone = zoneId;
    }

    private void saveNitzTime(long time) {
        if (DBG) {
            log("saveNitzTime: time=" + time);
        }
        this.mSavedTime = time;
        this.mSavedAtTime = SystemClock.elapsedRealtime();
    }

    private void setAndBroadcastNetworkSetTimeZone(String zoneId) {
        if (DBG) {
            log("setAndBroadcastNetworkSetTimeZone: setTimeZone=" + zoneId);
        }
        ((AlarmManager) this.mPhone.getContext().getSystemService("alarm")).setTimeZone(zoneId);
        Intent intent = new Intent("android.intent.action.NETWORK_SET_TIMEZONE");
        intent.addFlags(536870912);
        intent.putExtra("time-zone", zoneId);
        this.mPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        if (DBG) {
            log("setAndBroadcastNetworkSetTimeZone: call alarm.setTimeZone and broadcast zoneId=" + zoneId);
        }
    }

    private void setAndBroadcastNetworkSetTime(long time) {
        if (DBG) {
            log("setAndBroadcastNetworkSetTime: time=" + time + "ms");
        }
        if (shouldSkipThisTime()) {
            if (DBG) {
                log("setAndBroadcastNetworkSetTime, skip this time");
            }
            return;
        }
        SystemClock.setCurrentTimeMillis(time);
        Intent intent = new Intent("android.intent.action.NETWORK_SET_TIME");
        intent.addFlags(536870912);
        intent.putExtra("time", time);
        this.mPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
        TelephonyMetrics.getInstance().writeNITZEvent(this.mPhone.getPhoneId(), time);
    }

    private boolean shouldSkipThisTime() {
        Object packageName = null;
        try {
            packageName = ActivityManagerNative.getDefault().getTopAppName().getPackageName();
        } catch (Exception e) {
        }
        if (GAME_PACKAGE_NAME.equals(packageName)) {
            return true;
        }
        return false;
    }

    private void revertToNitzTime() {
        if (Global.getInt(this.mCr, "auto_time", 0) == 0) {
            log("[NITZ]:revertToNitz,AUTO_TIME is 0");
            return;
        }
        if (DBG) {
            log("Reverting to NITZ Time: mSavedTime=" + this.mSavedTime + " mSavedAtTime=" + this.mSavedAtTime + " tz='" + this.mSavedTimeZone + "'");
        }
        if (!(this.mSavedTime == 0 || this.mSavedAtTime == 0)) {
            setAndBroadcastNetworkSetTime(this.mSavedTime + (SystemClock.elapsedRealtime() - this.mSavedAtTime));
        }
    }

    private void revertToNitzTimeZone() {
        if (Global.getInt(this.mCr, "auto_time_zone", 0) != 0) {
            if (getReceivedNitz()) {
                if (DBG) {
                    log("Reverting to NITZ TimeZone: tz='" + this.mSavedTimeZone);
                }
                if (this.mSavedTimeZone != null) {
                    setAndBroadcastNetworkSetTimeZone(this.mSavedTimeZone);
                }
            }
            if (isAllowFixTimeZone() && oppoFixZoneWithoutNitz(this.mcc)) {
                fixTimeZone();
                if (DBG) {
                    log("Reverting to fixed TimeZone: tz='" + this.mSavedTimeZone);
                }
                if (this.mSavedTimeZone != null) {
                    setAndBroadcastNetworkSetTimeZone(this.mSavedTimeZone);
                }
                return;
            }
            if (DBG) {
                log("Do nothing since other phone has received NITZ, but this phone didn't");
            }
        }
    }

    private void setNotification(int notifyType) {
    }

    private UiccCardApplication getUiccCardApplication() {
        if (this.mPhone.isPhoneTypeGsm()) {
            return this.mUiccController.getUiccCardApplication(this.mPhone.getPhoneId(), 1);
        }
        return this.mUiccController.getUiccCardApplication(this.mPhone.getPhoneId(), 2);
    }

    private void queueNextSignalStrengthPoll() {
        if (!this.mDontPollSignalStrength) {
            Message msg = obtainMessage();
            msg.what = 10;
            sendMessageDelayed(msg, 10000);
        }
    }

    private void notifyCdmaSubscriptionInfoReady() {
        if (this.mCdmaForSubscriptionInfoReadyRegistrants != null) {
            if (DBG) {
                log("CDMA_SUBSCRIPTION: call notifyRegistrants()");
            }
            this.mCdmaForSubscriptionInfoReadyRegistrants.notifyRegistrants();
        }
    }

    public void registerForDataConnectionAttached(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mAttachedRegistrants.add(r);
        if (getCurrentDataConnectionState() == 0) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForDataConnectionAttached(Handler h) {
        this.mAttachedRegistrants.remove(h);
    }

    public void registerForDataConnectionDetached(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mDetachedRegistrants.add(r);
        if (getCurrentDataConnectionState() != 0) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForDataConnectionDetached(Handler h) {
        this.mDetachedRegistrants.remove(h);
    }

    public void registerForDataRegStateOrRatChanged(Handler h, int what, Object obj) {
        this.mDataRegStateOrRatChangedRegistrants.add(new Registrant(h, what, obj));
        notifyDataRegStateRilRadioTechnologyChanged();
    }

    public void unregisterForDataRegStateOrRatChanged(Handler h) {
        this.mDataRegStateOrRatChangedRegistrants.remove(h);
    }

    public void registerForNetworkAttached(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mNetworkAttachedRegistrants.add(r);
        if (this.mSS.getVoiceRegState() == 0) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForNetworkAttached(Handler h) {
        this.mNetworkAttachedRegistrants.remove(h);
    }

    public void registerForPsRestrictedEnabled(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mPsRestrictEnabledRegistrants.add(r);
        if (this.mRestrictedState.isPsRestricted()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForPsRestrictedEnabled(Handler h) {
        this.mPsRestrictEnabledRegistrants.remove(h);
    }

    public void registerForPsRestrictedDisabled(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mPsRestrictDisabledRegistrants.add(r);
        if (this.mRestrictedState.isPsRestricted()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForPsRestrictedDisabled(Handler h) {
        this.mPsRestrictDisabledRegistrants.remove(h);
    }

    public void registerForSignalStrengthChanged(Handler h, int what, Object obj) {
        this.mSignalStrengthChangedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForSignalStrengthChanged(Handler h) {
        this.mSignalStrengthChangedRegistrants.remove(h);
    }

    /* JADX WARNING: Missing block: B:19:0x0080, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void powerOffRadioSafely(DcTracker dcTracker) {
        synchronized (this) {
            if (!this.mPendingRadioPowerOffAfterDataOff) {
                Message msg;
                int i;
                if (this.mPhone.isPhoneTypeGsm() || this.mPhone.isPhoneTypeCdmaLte()) {
                    int dds = SubscriptionManager.getDefaultDataSubscriptionId();
                    int phoneSubId = this.mPhone.getSubId();
                    log("powerOffRadioSafely phoneId=" + SubscriptionManager.getPhoneId(dds) + ", dds=" + dds + ", mPhone.getSubId()=" + this.mPhone.getSubId() + ", phoneSubId=" + phoneSubId);
                    if (dds != -1 && (dcTracker.isDisconnected() || dds != phoneSubId)) {
                        dcTracker.cleanUpAllConnections(PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
                        if (DBG) {
                            log("Data disconnected, turn off radio right away.");
                        }
                        hangupAndPowerOff();
                    } else if (this.mPhone.isPhoneTypeGsm() || !dcTracker.isDisconnected() || (dds != this.mPhone.getSubId() && (dds == this.mPhone.getSubId() || !ProxyController.getInstance().isDataDisconnected(dds)))) {
                        if (this.mPhone.isPhoneTypeGsm() && this.mPhone.isInCall()) {
                            this.mPhone.mCT.mRingingCall.hangupIfAlive();
                            this.mPhone.mCT.mBackgroundCall.hangupIfAlive();
                            this.mPhone.mCT.mForegroundCall.hangupIfAlive();
                        }
                        ImsPhone imsPhone = (ImsPhone) this.mPhone.getImsPhone();
                        if (imsPhone != null && imsPhone.isInCall()) {
                            if (DBG) {
                                log("exist volte call, hangup!!!");
                            }
                            imsPhone.getForegroundCall().hangupIfAlive();
                            imsPhone.getBackgroundCall().hangupIfAlive();
                            imsPhone.getRingingCall().hangupIfAlive();
                        }
                        dcTracker.cleanUpAllConnections(PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
                        if (this.mPhone.isPhoneTypeGsm()) {
                            if (dds == -1 || SubscriptionManager.getPhoneId(dds) == Integer.MAX_VALUE) {
                                if (dcTracker.isDisconnected() || dcTracker.isOnlyIMSorEIMSPdnConnected()) {
                                    if (DBG) {
                                        log("Data disconnected (no data sub), turn off radio right away.");
                                    }
                                    hangupAndPowerOff();
                                    return;
                                }
                                if (DBG) {
                                    log("Data is active on.  Wait for all data disconnect");
                                }
                                this.mPhone.registerForAllDataDisconnected(this, 49, null);
                                this.mPendingRadioPowerOffAfterDataOff = true;
                            }
                        } else if (!(dds == this.mPhone.getSubId() || ProxyController.getInstance().isDataDisconnected(dds))) {
                            if (DBG) {
                                log("Data is active on DDS. Wait for all data disconnect");
                            }
                            ProxyController.getInstance().registerForAllDataDisconnected(dds, this, 49, null);
                            this.mPendingRadioPowerOffAfterDataOff = true;
                        }
                        if (dcTracker.isOnlyIMSorEIMSPdnConnected()) {
                            if (DBG) {
                                log("Only IMS or EIMS connected, turn off radio right away.");
                            }
                            hangupAndPowerOff();
                            return;
                        }
                        msg = Message.obtain(this);
                        msg.what = 38;
                        i = this.mPendingRadioPowerOffAfterDataOffTag + 1;
                        this.mPendingRadioPowerOffAfterDataOffTag = i;
                        msg.arg1 = i;
                        if (sendMessageDelayed(msg, 5000)) {
                            if (DBG) {
                                log("Wait upto 5s for data to disconnect, then turn off radio.");
                            }
                            this.mPendingRadioPowerOffAfterDataOff = true;
                        } else {
                            log("Cannot send delayed Msg, turn off radio right away.");
                            hangupAndPowerOff();
                            this.mPendingRadioPowerOffAfterDataOff = false;
                        }
                    } else {
                        dcTracker.cleanUpAllConnections(PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
                        if (DBG) {
                            log("Data disconnected, turn off radio right away.");
                        }
                        hangupAndPowerOff();
                    }
                } else {
                    String[] networkNotClearData = this.mPhone.getContext().getResources().getStringArray(17236039);
                    String currentNetwork = this.mSS.getOperatorNumeric();
                    if (!(networkNotClearData == null || currentNetwork == null)) {
                        for (Object equals : networkNotClearData) {
                            if (currentNetwork.equals(equals)) {
                                if (DBG) {
                                    log("Not disconnecting data for " + currentNetwork);
                                }
                                hangupAndPowerOff();
                                return;
                            }
                        }
                    }
                    if (dcTracker.isDisconnected()) {
                        dcTracker.cleanUpAllConnections(PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
                        if (DBG) {
                            log("Data disconnected, turn off radio right away.");
                        }
                        hangupAndPowerOff();
                    } else {
                        dcTracker.cleanUpAllConnections(PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
                        msg = Message.obtain(this);
                        msg.what = 38;
                        i = this.mPendingRadioPowerOffAfterDataOffTag + 1;
                        this.mPendingRadioPowerOffAfterDataOffTag = i;
                        msg.arg1 = i;
                        if (oemPowerOffRadioSafely(msg)) {
                            this.mPendingRadioPowerOffAfterDataOff = true;
                        } else {
                            log("Cannot send delayed Msg, turn off radio right away.");
                            hangupAndPowerOff();
                        }
                    }
                }
            }
        }
    }

    public boolean processPendingRadioPowerOffAfterDataOff() {
        synchronized (this) {
            if (this.mPendingRadioPowerOffAfterDataOff) {
                if (DBG) {
                    log("Process pending request to turn radio off.");
                }
                this.mPendingRadioPowerOffAfterDataOffTag++;
                hangupAndPowerOff();
                this.mPendingRadioPowerOffAfterDataOff = false;
                return true;
            }
            return false;
        }
    }

    protected boolean onSignalStrengthResult(AsyncResult ar) {
        boolean isGsm = false;
        if (this.mIsScreenOn) {
            if (this.mPhone.isPhoneTypeGsm() || (this.mPhone.isPhoneTypeCdmaLte() && ServiceState.isLte(this.mSS.getRilDataRadioTechnology()))) {
                isGsm = true;
            }
            if (ar.exception != null || ar.result == null) {
                log("onSignalStrengthResult() Exception from RIL : " + ar.exception);
                this.mSignalStrength = new SignalStrength(isGsm);
            } else {
                this.mSignalStrength = (SignalStrength) ar.result;
                this.mSignalStrength.validateInput();
                this.mSignalStrength.setGsm(isGsm);
                if (DBG) {
                    String str;
                    StringBuilder append = new StringBuilder().append("onSignalStrengthResult():");
                    if (this.mLastSignalStrength != null) {
                        str = "LastSignalStrength=" + this.mLastSignalStrength.toString();
                    } else {
                        str = UsimPBMemInfo.STRING_NOT_SET;
                    }
                    log(append.append(str).append("new mSignalStrength=").append(this.mSignalStrength.toString()).toString());
                }
            }
            return updateOEMSmooth(this.mSS);
        }
        log("SignalStrength because screen is off return ");
        return true;
    }

    protected void hangupAndPowerOff() {
        if (!this.mPhone.isPhoneTypeGsm() || this.mPhone.isInCall()) {
            this.mPhone.mCT.mRingingCall.hangupIfAlive();
            this.mPhone.mCT.mBackgroundCall.hangupIfAlive();
            this.mPhone.mCT.mForegroundCall.hangupIfAlive();
        }
        ImsPhone imsPhone = (ImsPhone) this.mPhone.getImsPhone();
        if (imsPhone != null && imsPhone.isInCall()) {
            if (DBG) {
                log("exist volte call, hangup!!!");
            }
            imsPhone.getForegroundCall().hangupIfAlive();
            imsPhone.getBackgroundCall().hangupIfAlive();
            imsPhone.getRingingCall().hangupIfAlive();
        }
        RadioManager.getInstance();
        RadioManager.sendRequestBeforeSetRadioPower(false, this.mPhone.getPhoneId());
        this.mCi.setRadioPower(false, null);
    }

    protected void cancelPollState() {
        this.mPollingContext = new int[1];
    }

    protected boolean shouldFixTimeZoneNow(Phone phone, String operatorNumeric, String prevOperatorNumeric, boolean needToFixTimeZone) {
        try {
            int prevMcc;
            int mcc = Integer.parseInt(operatorNumeric.substring(0, 3));
            try {
                prevMcc = Integer.parseInt(prevOperatorNumeric.substring(0, 3));
            } catch (Exception e) {
                prevMcc = mcc + 1;
            }
            boolean iccCardExist = false;
            if (this.mUiccApplcation != null) {
                iccCardExist = this.mUiccApplcation.getState() != AppState.APPSTATE_UNKNOWN;
            }
            boolean retVal = (!iccCardExist || mcc == prevMcc) ? needToFixTimeZone : true;
            if (DBG) {
                log("shouldFixTimeZoneNow: retVal=" + retVal + " iccCardExist=" + iccCardExist + " operatorNumeric=" + operatorNumeric + " mcc=" + mcc + " prevOperatorNumeric=" + prevOperatorNumeric + " prevMcc=" + prevMcc + " needToFixTimeZone=" + needToFixTimeZone + " ltod=" + TimeUtils.logTimeOfDay(System.currentTimeMillis()));
            }
            return retVal;
        } catch (Exception e2) {
            if (DBG) {
                log("shouldFixTimeZoneNow: no mcc, operatorNumeric=" + operatorNumeric + " retVal=false");
            }
            return false;
        }
    }

    public String getSystemProperty(String property, String defValue) {
        return TelephonyManager.getTelephonyProperty(this.mPhone.getPhoneId(), property, defValue);
    }

    /* JADX WARNING: Missing block: B:36:0x00b7, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<CellInfo> getAllCellInfo() {
        CellInfoResult result = new CellInfoResult(this, null);
        String mLog = "SST.getAllCellInfo(): ";
        if (this.mCi.getRilVersion() < 8) {
            mLog = mLog + "not implemented. ";
            result.list = null;
        } else if (!isCallerOnDifferentThread()) {
            mLog = mLog + "return last, same thread can't block. ";
            result.list = this.mLastCellInfoList;
        } else if (SystemClock.elapsedRealtime() - this.mLastCellInfoListTime > LAST_CELL_INFO_LIST_MAX_AGE_MS) {
            Message msg = obtainMessage(43, result);
            synchronized (result.lockObj) {
                result.list = null;
                this.mCi.getCellInfoList(msg);
                try {
                    result.lockObj.wait(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            mLog = mLog + "return last, back to back calls. ";
            result.list = this.mLastCellInfoList;
        }
        synchronized (result.lockObj) {
            if (result.list != null) {
                List<CellInfo> list = result.list;
                return list;
            }
            mLog = mLog + "X size=0 list=null.";
            if (DBG) {
                log(mLog);
            }
        }
    }

    protected List<CellInfo> getAllCellInfoByRate() {
        CellInfoResult result = new CellInfoResult(this, null);
        if (DBG) {
            log("SST.getAllCellInfoByRate(): enter");
        }
        if (this.mCi.getRilVersion() < 8) {
            if (DBG) {
                log("SST.getAllCellInfoByRate(): not implemented");
            }
            result.list = null;
        } else if (!isCallerOnDifferentThread()) {
            if (DBG) {
                log("SST.getAllCellInfoByRate(): return last, same thread can't block");
            }
            result.list = this.mLastCellInfoList;
        } else if (SystemClock.elapsedRealtime() - this.mLastCellInfoListTime > LAST_CELL_INFO_LIST_MAX_AGE_MS) {
            Message msg = obtainMessage(108, result);
            synchronized (result.lockObj) {
                this.mCi.getCellInfoList(msg);
                try {
                    result.lockObj.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    result.list = null;
                }
            }
        } else {
            if (DBG) {
                log("SST.getAllCellInfoByRate(): return last, back to back calls");
            }
            result.list = this.mLastCellInfoList;
        }
        if (DBG) {
            if (result.list != null) {
                log("SST.getAllCellInfoByRate(): X size=" + result.list.size() + " list=" + result.list);
            } else {
                log("SST.getAllCellInfoByRate(): X size=0 list=null");
            }
        }
        return result.list;
    }

    public void setCellInfoRate(int rateInMillis) {
        log("SST.setCellInfoRate()");
        this.mCellInfoRate = rateInMillis;
        updateCellInfoRate();
    }

    protected void updateCellInfoRate() {
        log("SST.updateCellInfoRate()");
        if (this.mPhone.isPhoneTypeGsm()) {
            log("updateCellInfoRate(),mCellInfoRate= " + this.mCellInfoRate);
            if (this.mCellInfoRate != Integer.MAX_VALUE && this.mCellInfoRate != 0) {
                if (mCellInfoTimer != null) {
                    log("cancel previous timer if any");
                    mCellInfoTimer.cancel();
                    mCellInfoTimer = null;
                }
                mCellInfoTimer = new Timer(true);
                log("schedule timer with period = " + this.mCellInfoRate + " ms");
                mCellInfoTimer.schedule(new timerTask(this), (long) this.mCellInfoRate);
            } else if ((this.mCellInfoRate == 0 || this.mCellInfoRate == Integer.MAX_VALUE) && mCellInfoTimer != null) {
                log("cancel cell info timer if any");
                mCellInfoTimer.cancel();
                mCellInfoTimer = null;
            }
        }
    }

    public SignalStrength getSignalStrength() {
        return this.mSignalStrength;
    }

    public void registerForSubscriptionInfoReady(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mCdmaForSubscriptionInfoReadyRegistrants.add(r);
        if (isMinInfoReady()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForSubscriptionInfoReady(Handler h) {
        this.mCdmaForSubscriptionInfoReadyRegistrants.remove(h);
    }

    private void saveCdmaSubscriptionSource(int source) {
        logd("Storing cdma subscription source: " + source);
        Global.putInt(this.mPhone.getContext().getContentResolver(), "subscription_mode", source);
        logd("Read from settings: " + Global.getInt(this.mPhone.getContext().getContentResolver(), "subscription_mode", -1));
    }

    private void getSubscriptionInfoAndStartPollingThreads() {
        this.mCi.getCDMASubscription(obtainMessage(34));
        pollState();
    }

    private void handleCdmaSubscriptionSource(int newSubscriptionSource) {
        boolean z = false;
        log("Subscription Source : " + newSubscriptionSource);
        if (newSubscriptionSource == 0) {
            z = true;
        }
        this.mIsSubscriptionFromRuim = z;
        logd("isFromRuim: " + this.mIsSubscriptionFromRuim);
        saveCdmaSubscriptionSource(newSubscriptionSource);
        if (!this.mIsSubscriptionFromRuim) {
            sendMessage(obtainMessage(35));
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("ServiceStateTracker:");
        pw.println(" mSubId=" + this.mSubId);
        pw.println(" mSS=" + this.mSS);
        pw.println(" mNewSS=" + this.mNewSS);
        pw.println(" mVoiceCapable=" + this.mVoiceCapable);
        pw.println(" mRestrictedState=" + this.mRestrictedState);
        pw.println(" mPollingContext=" + this.mPollingContext + " - " + (this.mPollingContext != null ? Integer.valueOf(this.mPollingContext[0]) : UsimPBMemInfo.STRING_NOT_SET));
        pw.println(" mDesiredPowerState=" + this.mDesiredPowerState);
        pw.println(" mDontPollSignalStrength=" + this.mDontPollSignalStrength);
        pw.println(" mSignalStrength=" + this.mSignalStrength);
        pw.println(" mLastSignalStrength=" + this.mLastSignalStrength);
        pw.println(" mRestrictedState=" + this.mRestrictedState);
        pw.println(" mPendingRadioPowerOffAfterDataOff=" + this.mPendingRadioPowerOffAfterDataOff);
        pw.println(" mPendingRadioPowerOffAfterDataOffTag=" + this.mPendingRadioPowerOffAfterDataOffTag);
        pw.println(" mCellLoc=" + this.mCellLoc);
        pw.println(" mNewCellLoc=" + this.mNewCellLoc);
        pw.println(" mLastCellInfoListTime=" + this.mLastCellInfoListTime);
        pw.println(" mPreferredNetworkType=" + this.mPreferredNetworkType);
        pw.println(" mMaxDataCalls=" + this.mMaxDataCalls);
        pw.println(" mNewMaxDataCalls=" + this.mNewMaxDataCalls);
        pw.println(" mReasonDataDenied=" + this.mReasonDataDenied);
        pw.println(" mNewReasonDataDenied=" + this.mNewReasonDataDenied);
        pw.println(" mGsmRoaming=" + this.mGsmRoaming);
        pw.println(" mDataRoaming=" + this.mDataRoaming);
        pw.println(" mEmergencyOnly=" + this.mEmergencyOnly);
        pw.println(" mNeedFixZoneAfterNitz=" + this.mNeedFixZoneAfterNitz);
        pw.flush();
        pw.println(" mZoneOffset=" + this.mZoneOffset);
        pw.println(" mZoneDst=" + this.mZoneDst);
        pw.println(" mZoneTime=" + this.mZoneTime);
        pw.println(" mGotCountryCode=" + this.mGotCountryCode);
        pw.println(" mNitzUpdatedTime=" + this.mNitzUpdatedTime);
        pw.println(" mSavedTimeZone=" + this.mSavedTimeZone);
        pw.println(" mSavedTime=" + this.mSavedTime);
        pw.println(" mSavedAtTime=" + this.mSavedAtTime);
        pw.println(" mStartedGprsRegCheck=" + this.mStartedGprsRegCheck);
        pw.println(" mReportedGprsNoReg=" + this.mReportedGprsNoReg);
        pw.println(" mNotification=" + this.mNotification);
        pw.println(" mWakeLock=" + this.mWakeLock);
        pw.println(" mCurSpn=" + this.mCurSpn);
        pw.println(" mCurDataSpn=" + this.mCurDataSpn);
        pw.println(" mCurShowSpn=" + this.mCurShowSpn);
        pw.println(" mCurPlmn=" + this.mCurPlmn);
        pw.println(" mCurShowPlmn=" + this.mCurShowPlmn);
        pw.flush();
        pw.println(" mCurrentOtaspMode=" + this.mCurrentOtaspMode);
        pw.println(" mRoamingIndicator=" + this.mRoamingIndicator);
        pw.println(" mIsInPrl=" + this.mIsInPrl);
        pw.println(" mDefaultRoamingIndicator=" + this.mDefaultRoamingIndicator);
        pw.println(" mRegistrationState=" + this.mRegistrationState);
        pw.println(" mMdn=" + this.mMdn);
        pw.println(" mHomeSystemId=" + this.mHomeSystemId);
        pw.println(" mHomeNetworkId=" + this.mHomeNetworkId);
        pw.println(" mMin=" + this.mMin);
        pw.println(" mPrlVersion=" + this.mPrlVersion);
        pw.println(" mIsMinInfoReady=" + this.mIsMinInfoReady);
        pw.println(" mIsEriTextLoaded=" + this.mIsEriTextLoaded);
        pw.println(" mIsSubscriptionFromRuim=" + this.mIsSubscriptionFromRuim);
        pw.println(" mCdmaSSM=" + this.mCdmaSSM);
        pw.println(" mRegistrationDeniedReason=" + this.mRegistrationDeniedReason);
        pw.println(" mCurrentCarrier=" + this.mCurrentCarrier);
        pw.flush();
        pw.println(" mImsRegistered=" + this.mImsRegistered);
        pw.println(" mImsRegistrationOnOff=" + this.mImsRegistrationOnOff);
        pw.println(" mAlarmSwitch=" + this.mAlarmSwitch);
        pw.println(" mRadioDisabledByCarrier" + this.mRadioDisabledByCarrier);
        pw.println(" mPowerOffDelayNeed=" + this.mPowerOffDelayNeed);
        pw.println(" mDeviceShuttingDown=" + this.mDeviceShuttingDown);
        pw.println(" mSpnUpdatePending=" + this.mSpnUpdatePending);
        pw.println(" Roaming Log:");
        IndentingPrintWriter ipw = new IndentingPrintWriter(pw, "  ");
        ipw.increaseIndent();
        this.mRoamingLog.dump(fd, ipw, args);
        ipw.decreaseIndent();
        ipw.println(" Attach Log:");
        ipw.increaseIndent();
        this.mAttachLog.dump(fd, ipw, args);
        ipw.decreaseIndent();
        ipw.println(" Phone Change Log:");
        ipw.increaseIndent();
        this.mPhoneTypeLog.dump(fd, ipw, args);
        ipw.decreaseIndent();
        ipw.println(" Rat Change Log:");
        ipw.increaseIndent();
        this.mRatLog.dump(fd, ipw, args);
        ipw.decreaseIndent();
    }

    public boolean isImsRegistered() {
        return this.mImsRegistered;
    }

    protected void checkCorrectThread() {
        if (Thread.currentThread() != getLooper().getThread()) {
            throw new RuntimeException("ServiceStateTracker must be used from within one thread");
        }
    }

    protected boolean isCallerOnDifferentThread() {
        return Thread.currentThread() != getLooper().getThread();
    }

    protected void updateCarrierMccMncConfiguration(String newOp, String oldOp, Context context) {
        if ((newOp == null && !TextUtils.isEmpty(oldOp)) || (newOp != null && !newOp.equals(oldOp))) {
            logd("update mccmnc=" + newOp + " fromServiceState=true");
            MccTable.updateMccMncConfiguration(context, newOp, true);
        }
    }

    protected boolean inSameCountry(String operatorNumeric) {
        if (TextUtils.isEmpty(operatorNumeric) || operatorNumeric.length() < 5) {
            return false;
        }
        String homeNumeric = getHomeOperatorNumeric();
        if (TextUtils.isEmpty(homeNumeric) || homeNumeric.length() < 5) {
            return false;
        }
        String networkMCC = operatorNumeric.substring(0, 3);
        String homeMCC = homeNumeric.substring(0, 3);
        String networkCountry = MccTable.countryCodeForMcc(Integer.parseInt(networkMCC));
        String homeCountry = MccTable.countryCodeForMcc(Integer.parseInt(homeMCC));
        if (networkCountry.isEmpty() || homeCountry.isEmpty()) {
            return false;
        }
        boolean inSameCountry = homeCountry.equals(networkCountry);
        if (inSameCountry) {
            return inSameCountry;
        }
        if ("us".equals(homeCountry) && "vi".equals(networkCountry)) {
            inSameCountry = true;
        } else if ("vi".equals(homeCountry) && "us".equals(networkCountry)) {
            inSameCountry = true;
        }
        return inSameCountry;
    }

    protected void setRoamingType(ServiceState currentServiceState) {
        boolean isVoiceInService;
        if (currentServiceState.getVoiceRegState() == 0) {
            isVoiceInService = true;
        } else {
            isVoiceInService = false;
        }
        boolean isInternationalRoaming = false;
        if (isVoiceInService) {
            if (!currentServiceState.getVoiceRoaming()) {
                currentServiceState.setVoiceRoamingType(0);
            } else if (this.mPhone.isPhoneTypeGsm()) {
                if (inSameCountry(currentServiceState.getVoiceOperatorNumeric())) {
                    currentServiceState.setVoiceRoamingType(2);
                } else {
                    currentServiceState.setVoiceRoamingType(3);
                }
                try {
                    isInternationalRoaming = this.mServiceStateExt.operatorDefinedInternationalRoaming(currentServiceState.getVoiceOperatorNumeric());
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
                if (isInternationalRoaming) {
                    log(currentServiceState.getVoiceOperatorNumeric() + " is in operator defined international roaming list");
                    currentServiceState.setVoiceRoamingType(3);
                }
            } else {
                int[] intRoamingIndicators = this.mPhone.getContext().getResources().getIntArray(17236042);
                if (intRoamingIndicators != null && intRoamingIndicators.length > 0) {
                    currentServiceState.setVoiceRoamingType(2);
                    int curRoamingIndicator = currentServiceState.getCdmaRoamingIndicator();
                    for (int i : intRoamingIndicators) {
                        if (curRoamingIndicator == i) {
                            currentServiceState.setVoiceRoamingType(3);
                            break;
                        }
                    }
                } else if (inSameCountry(currentServiceState.getVoiceOperatorNumeric())) {
                    currentServiceState.setVoiceRoamingType(2);
                } else {
                    currentServiceState.setVoiceRoamingType(3);
                }
            }
        }
        boolean isDataInService = currentServiceState.getDataRegState() == 0;
        int dataRegType = currentServiceState.getRilDataRadioTechnology();
        if (!isDataInService) {
            return;
        }
        if (!currentServiceState.getDataRoaming()) {
            currentServiceState.setDataRoamingType(0);
        } else if (this.mPhone.isPhoneTypeGsm()) {
            if (!ServiceState.isGsm(dataRegType)) {
                currentServiceState.setDataRoamingType(1);
            } else if (isVoiceInService) {
                currentServiceState.setDataRoamingType(currentServiceState.getVoiceRoamingType());
            } else {
                currentServiceState.setDataRoamingType(1);
            }
        } else if (ServiceState.isCdma(dataRegType)) {
            if (isVoiceInService) {
                currentServiceState.setDataRoamingType(currentServiceState.getVoiceRoamingType());
            } else {
                currentServiceState.setDataRoamingType(1);
            }
        } else if (inSameCountry(currentServiceState.getDataOperatorNumeric())) {
            currentServiceState.setDataRoamingType(2);
        } else {
            currentServiceState.setDataRoamingType(3);
        }
    }

    private void setSignalStrengthDefaultValues() {
        this.mSignalStrength = new SignalStrength(true);
    }

    private void setNullState() {
        this.mGsmRoaming = false;
        this.mNewReasonDataDenied = -1;
        this.mNewMaxDataCalls = 1;
        this.mDataRoaming = false;
        this.mEmergencyOnly = false;
        updateLocatedPlmn(null);
        this.mDontPollSignalStrength = false;
        this.mLastSignalStrength = new SignalStrength(true);
        this.isCsInvalidCard = false;
        this.mPsRegState = 1;
    }

    protected String getHomeOperatorNumeric() {
        String numeric = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getSimOperatorNumericForPhone(this.mPhone.getPhoneId());
        if (this.mPhone.isPhoneTypeGsm() || !TextUtils.isEmpty(numeric)) {
            return numeric;
        }
        return SystemProperties.get(GsmCdmaPhone.PROPERTY_CDMA_HOME_OPERATOR_NUMERIC, UsimPBMemInfo.STRING_NOT_SET);
    }

    protected int getPhoneId() {
        return this.mPhone.getPhoneId();
    }

    protected void resetServiceStateInIwlanMode() {
        if (this.mCi.getRadioState() == RadioState.RADIO_OFF) {
            boolean resetIwlanRatVal = false;
            log("set service state as POWER_OFF");
            if (18 == this.mNewSS.getRilDataRadioTechnology()) {
                log("pollStateDone: mNewSS = " + this.mNewSS);
                log("pollStateDone: reset iwlan RAT value");
                resetIwlanRatVal = true;
            }
            String operator = this.mNewSS.getOperatorAlphaLong();
            this.mNewSS.setStateOff();
            if (resetIwlanRatVal) {
                this.mNewSS.setRilDataRadioTechnology(18);
                this.mNewSS.setDataRegState(0);
                this.mNewSS.setOperatorAlphaLong(operator);
                log("pollStateDone: mNewSS = " + this.mNewSS);
            }
        }
    }

    protected final boolean alwaysOnHomeNetwork(BaseBundle b) {
        return b.getBoolean("force_home_network_bool");
    }

    private boolean isInNetwork(BaseBundle b, String network, String key) {
        String[] networks = b.getStringArray(key);
        if (networks == null || !Arrays.asList(networks).contains(network)) {
            return false;
        }
        return true;
    }

    protected final boolean isRoamingInGsmNetwork(BaseBundle b, String network) {
        return isInNetwork(b, network, "gsm_roaming_networks_string_array");
    }

    protected final boolean isNonRoamingInGsmNetwork(BaseBundle b, String network) {
        return isInNetwork(b, network, "gsm_nonroaming_networks_string_array");
    }

    protected final boolean isRoamingInCdmaNetwork(BaseBundle b, String network) {
        return isInNetwork(b, network, "cdma_roaming_networks_string_array");
    }

    protected final boolean isNonRoamingInCdmaNetwork(BaseBundle b, String network) {
        return isInNetwork(b, network, "cdma_nonroaming_networks_string_array");
    }

    protected int getPreferredNetworkModeSettings(int phoneId) {
        int[] subId = SubscriptionManager.getSubId(phoneId);
        if (subId != null && SubscriptionManager.isValidSubscriptionId(subId[0])) {
            return PhoneFactory.calculatePreferredNetworkType(this.mPhone.getContext(), subId[0]);
        }
        log("Invalid subId, return invalid networkType");
        return -1;
    }

    protected void setSignalStrength(AsyncResult ar, boolean isGsm) {
        SignalStrength oldSignalStrength = this.mSignalStrength;
        if (DBG && this.mLastSignalStrength != null) {
            log("Before combine Signal Strength, setSignalStrength(): isGsm = " + isGsm + " LastSignalStrength = " + this.mLastSignalStrength.toString());
        }
        if (ar.exception != null || ar.result == null) {
            log("Before combine Signal Strength, setSignalStrength() Exception from RIL : " + ar.exception);
            this.mSignalStrength = new SignalStrength(isGsm);
            return;
        }
        this.mSignalStrength = (SignalStrength) ar.result;
        this.mSignalStrength.validateInput();
        this.mSignalStrength.setGsm(isGsm);
        if (DBG) {
            log("Before combine Signal Strength, setSignalStrength(): isGsm = " + isGsm + "new mSignalStrength = " + this.mSignalStrength.toString());
        }
    }

    private final boolean isConcurrentVoiceAndDataAllowedForVolte() {
        if (this.mSS.getDataRegState() == 0 && ServiceState.isLte(this.mSS.getRilDataRadioTechnology()) && getImsServiceState() == 0) {
            return true;
        }
        return false;
    }

    private final int getImsServiceState() {
        Phone imsPhone = this.mPhone.getImsPhone();
        if (imsPhone != null && imsPhone.isVolteEnabled() && this.mPhone.isImsUseEnabled()) {
            return imsPhone.getServiceState().getState();
        }
        return 1;
    }

    public boolean isHPlmn(String plmn) {
        if (!this.mPhone.isPhoneTypeGsm()) {
            return false;
        }
        String mccmnc = getSIMOperatorNumeric();
        if (plmn == null) {
            return false;
        }
        if (mccmnc == null || mccmnc.equals(UsimPBMemInfo.STRING_NOT_SET)) {
            log("isHPlmn getSIMOperatorNumeric error: " + mccmnc);
            return false;
        } else if (plmn.equals(mccmnc)) {
            return true;
        } else {
            if (plmn.length() == 5 && mccmnc.length() == 6 && plmn.equals(mccmnc.substring(0, 5))) {
                return true;
            }
            if (this.mPhone.getPhoneType() == 1) {
                for (int i = 0; i < customEhplmn.length; i++) {
                    boolean isServingPlmnInGroup = false;
                    boolean isHomePlmnInGroup = false;
                    for (int j = 0; j < customEhplmn[i].length; j++) {
                        if (plmn.equals(customEhplmn[i][j])) {
                            isServingPlmnInGroup = true;
                        }
                        if (mccmnc.equals(customEhplmn[i][j])) {
                            isHomePlmnInGroup = true;
                        }
                    }
                    if (isServingPlmnInGroup && isHomePlmnInGroup) {
                        log("plmn:" + plmn + "is in customized ehplmn table");
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public boolean isDeviceShuttingDown() {
        return this.mDeviceShuttingDown;
    }

    private void hasEonsChanged() {
        logd("hasEonsChanged OperatorNumeric:" + this.mNewSS.getOperatorNumeric() + ", Lac: " + ((GsmCellLocation) this.mNewCellLoc).getLac());
        SIMRecords simRecords = null;
        IccRecords r = (IccRecords) this.mPhone.mIccRecords.get();
        if (r != null) {
            simRecords = (SIMRecords) r;
        }
        String sEons = null;
        String sNewEons = null;
        if (simRecords != null) {
            try {
                sEons = simRecords.getEonsIfExist(this.mSS.getOperatorNumeric(), ((GsmCellLocation) this.mCellLoc).getLac(), true);
            } catch (RuntimeException ex) {
                loge("Exception while getEonsIfExist. " + ex);
            }
        } else {
            sEons = null;
        }
        if (simRecords != null) {
            sNewEons = simRecords.getEonsIfExist(this.mNewSS.getOperatorNumeric(), ((GsmCellLocation) this.mNewCellLoc).getLac(), true);
        } else {
            sNewEons = null;
        }
        if (sEons == null && sNewEons != null) {
            log("sNewEons: " + sNewEons);
            this.explict_update_spn = 1;
        } else if (sEons != null && sNewEons == null) {
            log("sEons: " + sEons);
            this.explict_update_spn = 1;
        } else if (sEons != null && sNewEons != null && !sEons.equals(sNewEons)) {
            log("sEons: " + sEons + ", sNewEons: " + sNewEons);
            this.explict_update_spn = 1;
        }
    }

    protected void removeSmoothMessage() {
        this.mIsPendingNotify_0 = false;
        this.mIsPendingNotify_1 = false;
        removeMessages(EVENT_OEM_SMOOTH_0);
        removeMessages(EVENT_OEM_SMOOTH_1);
    }

    public int getSignalLevel() {
        return this.mOEMCurLevel_1;
    }

    protected boolean updateOEMSmooth(ServiceState st) {
        SignalStrength oem_ss = getSignalStrength();
        int[] levels = getOEMLevel(st, oem_ss);
        this.mOEMCurLevel_0 = levels[0];
        this.mOEMCurLevel_1 = levels[1];
        if (this.mIsScreenOn || (!(this.mOEMCurLevel_0 == 0 && this.mOEMCurLevel_1 == 0) && ((this.mOEMCurLevel_0 <= 0 && this.mOEMCurLevel_1 <= 0) || (this.mOEMLastLevel_0 <= 0 && this.mOEMCurLevel_1 <= 0)))) {
            if (OemConstant.getWlanAssistantEnable(this.mPhone.getContext())) {
                SubscriptionManager s = SubscriptionManager.from(this.mPhone.getContext());
                boolean isDefaultDataPhone = this.mPhone.getSubId() == SubscriptionManager.getDefaultDataSubId();
                boolean signalLevelChanged = this.mOEMCurLevel_1 != this.mOEMLastLevel_1;
                if (signalLevelChanged && isDefaultDataPhone) {
                    boolean myMeasureDataState;
                    if (DBG) {
                        log("WLAN+ EVENT_SIGNAL_UPDATE_CHANGED:signalLevelChanged = " + signalLevelChanged + " mMeasureDataState=" + DcTracker.mMeasureDataState + " Roaming=" + this.mSS.getRoaming() + " DataEnabled=" + (!this.mPhone.getDataEnabled() ? this.mPhone.mDcTracker.haveVsimIgnoreUserDataSetting() : true) + " isDefaultDataPhone" + isDefaultDataPhone);
                    }
                    if (!DcTracker.mMeasureDataState || DcTracker.mDelayMeasure || this.mSS.getRoaming()) {
                        myMeasureDataState = false;
                    } else {
                        myMeasureDataState = !this.mPhone.getDataEnabled() ? this.mPhone.mDcTracker.haveVsimIgnoreUserDataSetting() : true;
                    }
                    if (myMeasureDataState) {
                        new AnonymousClass6(this).start();
                    }
                }
            }
            if (this.mOEMCurLevel_1 == 0 && this.mOEMCurLevel_0 == 0 && ((getNetworkModeBySS(st.getRilVoiceRadioTechnology()) == 3 || getNetworkModeBySS(st.getRilDataRadioTechnology()) == 3) && (oem_ss.getLteDbm() == Integer.MAX_VALUE || oem_ss.getLteDbm() == 255))) {
                if (DBG) {
                    log("leon updateOEMSmooth block");
                }
                return false;
            }
            if (!IS_OEM_SMOOTH && this.mOEMCurLevel_1 == 0 && this.mOEMCurLevel_0 == 0 && (this.mPhone.getContext().getPackageManager().hasSystemFeature("oppo.cta.support") || this.mPhone.getState() != State.IDLE)) {
                for (int lev : new int[]{3, 2, 1}) {
                    this.mOEMCurLevel_0 = oem_ss.getOEMLevel(lev);
                    if (this.mOEMCurLevel_0 > 0) {
                        break;
                    }
                }
            }
            if (IS_OEM_SMOOTH && this.mOEMCurLevel_1 == 0 && this.mOEMCurLevel_0 == 0) {
                this.mOEMCurLevel_0 = 1;
                this.mOEMCurLevel_1 = 1;
            }
            if (DBG) {
                log("leon updateOEMSmooth(last:(" + this.mOEMLastLevel_0 + "," + this.mOEMLastLevel_1 + ") current:(" + this.mOEMCurLevel_0 + "," + this.mOEMCurLevel_1 + ") )");
            }
            if (!IS_OEM_SMOOTH || (this.mOEMLastLevel_0 <= this.mOEMCurLevel_0 && this.mOEMLastLevel_1 <= this.mOEMCurLevel_1)) {
                removeSmoothMessage();
                this.mOEMLastLevel_0 = this.mOEMCurLevel_0;
                this.mOEMLastLevel_1 = this.mOEMCurLevel_1;
                oem_ss.mOEMLevel_0 = this.mOEMCurLevel_0;
                oem_ss.mOEMLevel_1 = this.mOEMCurLevel_1;
                return oppoNotifySignalStrength();
            }
            if (this.mOEMLastLevel_0 <= this.mOEMCurLevel_0) {
                this.mIsPendingNotify_0 = false;
                this.mOEMLastLevel_0 = this.mOEMCurLevel_0;
                removeMessages(EVENT_OEM_SMOOTH_0);
            }
            if (!this.mIsPendingNotify_0) {
                nofifySmoothLevel(EVENT_OEM_SMOOTH_0);
            }
            if (this.mOEMLastLevel_1 <= this.mOEMCurLevel_1) {
                this.mIsPendingNotify_1 = false;
                this.mOEMLastLevel_1 = this.mOEMCurLevel_1;
                removeMessages(EVENT_OEM_SMOOTH_1);
            }
            if (!this.mIsPendingNotify_1) {
                nofifySmoothLevel(EVENT_OEM_SMOOTH_1);
            }
            oem_ss.mOEMLevel_0 = this.mOEMLastLevel_0;
            oem_ss.mOEMLevel_1 = this.mOEMLastLevel_1;
            return oppoNotifySignalStrength();
        }
        log("DQL because screen is off return updateOEMSmooth");
        return true;
    }

    private int getNetworkModeBySS(int nt) {
        if (nt == 1 || nt == 2 || nt == 16) {
            return 1;
        }
        if (nt == 3 || nt == 9 || nt == 10 || nt == 11 || nt == 15) {
            return 4;
        }
        if (nt == 4 || nt == 5 || nt == 6) {
            return 5;
        }
        if (nt == 7 || nt == 8 || nt == 12 || nt == 13) {
            return 6;
        }
        if (nt == 17) {
            return 2;
        }
        if (nt == 14 || nt == 139) {
            return 3;
        }
        return 0;
    }

    protected int[] getOEMLevel(ServiceState st, SignalStrength s) {
        int[] iArr;
        if (OemConstant.EXP_VERSION) {
            int csLevel = s.getOEMLevel(getNetworkModeBySS(st.getRilVoiceRadioTechnology()));
            int psLevel = s.getOEMLevel(getNetworkModeBySS(st.getRilDataRadioTechnology()));
            if (st.getRilVoiceRadioTechnology() == 0 && psLevel > csLevel) {
                csLevel = psLevel;
            }
            iArr = new int[2];
            iArr[0] = csLevel;
            iArr[1] = psLevel;
            return iArr;
        }
        iArr = new int[2];
        iArr[0] = s.getOEMLevel(getNetworkModeBySS(st.getRilVoiceRadioTechnology()));
        iArr[1] = s.getOEMLevel(getNetworkModeBySS(st.getRilDataRadioTechnology()));
        return iArr;
    }

    protected void nofifySmoothLevel(int msg_type) {
        if (msg_type - 301 == 0) {
            if (this.mOEMLastLevel_0 <= this.mOEMCurLevel_0) {
                this.mOEMLastLevel_0 = this.mOEMCurLevel_0;
                this.mIsPendingNotify_0 = false;
                return;
            }
            this.mIsPendingNotify_0 = true;
        } else if (this.mOEMLastLevel_1 <= this.mOEMCurLevel_1) {
            this.mOEMLastLevel_1 = this.mOEMCurLevel_1;
            this.mIsPendingNotify_1 = false;
            return;
        } else {
            this.mIsPendingNotify_1 = true;
        }
        Message msg = Message.obtain();
        msg.what = msg_type;
        sendMessageDelayed(msg, 20000);
        if (DBG) {
            log("leon nofifySmoothLevel(last:(" + this.mOEMLastLevel_0 + "," + this.mOEMLastLevel_1 + ") current:(" + this.mOEMCurLevel_0 + "," + this.mOEMCurLevel_1 + ") ) " + msg_type);
        }
    }

    protected boolean oppoNotifySignalStrength() {
        try {
            this.mPhone.notifySignalStrength();
            if (DBG) {
                log("===oppoNotifySignalStrengthe=mSignalStrength==" + getSignalStrength());
            }
            return true;
        } catch (NullPointerException ex) {
            loge("updateSignalStrength() Phone already destroyed: " + ex + "SignalStrength not notified");
            return false;
        }
    }

    protected void oppoCheckSignal(ServiceState oldss, ServiceState newss) {
        if ((oldss.getVoiceRegState() != 0 && newss.getVoiceRegState() == 0) || (oldss.getDataRegState() != 0 && newss.getDataRegState() == 0)) {
            if (DBG) {
                log("leon oppoCheckSignal smooth: " + IS_OEM_SMOOTH);
            }
            if (IS_OEM_SMOOTH) {
                updateOEMSmooth(newss);
            }
        }
    }

    protected int getOemRegState(ServiceState ss) {
        int regState = ss.getVoiceRegState();
        int dataRegState = ss.getDataRegState();
        if (regState == 0 || dataRegState != 0) {
            return regState;
        }
        return dataRegState;
    }

    protected void oppoSetOperatorAlpha(String val) {
        try {
            TelephonyManager.getDefault();
            TelephonyManager.setTelephonyProperty(getPhone().getPhoneId(), "gsm.sim.operator.spn", val);
        } catch (Exception ex) {
            log("leon gsm.sim.operator.spn= ex." + ex.getMessage());
        }
        this.mOemSpn = val;
        if (DBG) {
            log("leon OemSpn=" + val);
        }
    }

    public String getOemSpn() {
        return this.mOemSpn;
    }

    protected void oppoVirtualSimCheck(String operator, String plmn, String spn, boolean showplmn, boolean showspn) {
        this.mShowPlmn = showplmn;
        this.mShowSPn = showspn;
        this.mNewPlmn = null;
        boolean isinCnlist = this.mIccRecords != null ? this.mIccRecords.isInCnList(spn) : true;
        if (this.mIccRecords != null) {
            boolean isInCmccList = this.mIccRecords.isInCmccList(spn);
        }
        if (this.mSS.getRoaming()) {
            this.mShowPlmn = true;
            this.mShowSPn = false;
        } else if (isinCnlist && !TextUtils.isEmpty(plmn)) {
            this.mShowPlmn = true;
            this.mShowSPn = false;
        } else if (showplmn && TextUtils.isEmpty(plmn)) {
            this.mShowPlmn = false;
            this.mShowSPn = true;
        }
        if (!(this.mShowPlmn || this.mShowSPn)) {
            boolean z;
            if (TextUtils.isEmpty(plmn)) {
                z = false;
            } else {
                z = true;
            }
            this.mShowPlmn = z;
            if (this.mShowPlmn || TextUtils.isEmpty(spn)) {
                z = false;
            } else {
                z = true;
            }
            this.mShowSPn = z;
        }
        if (this.mSubscriptionController.isHasSoftSimCard() && this.mSubscriptionController.getSoftSimCardSlotId() == getPhoneId() && !TextUtils.isEmpty(spn)) {
            this.mShowPlmn = false;
            this.mShowSPn = true;
        }
        if (this.mShowPlmn) {
            oppoSetOperatorAlpha(plmn);
            this.mShowSPn = false;
        } else if (!this.mShowSPn || TextUtils.isEmpty(spn)) {
            oppoSetOperatorAlpha(plmn);
            this.mShowSPn = false;
        } else {
            oppoSetOperatorAlpha(spn);
        }
    }

    public void oppoResetOosDelayState() {
        Phone oPhone = PhoneFactory.getPhone(1 - this.mPhone.getPhoneId());
        if (oPhone != null && oPhone.getServiceStateTracker() != null) {
            oPhone.getServiceStateTracker().mOosDelayState = 0;
        }
    }

    public boolean getNeedSetDss(int phoneId) {
        if (phoneId == 0) {
            return this.mNeedSetDds1;
        }
        return this.mNeedSetDds2;
    }

    public void setNeedSetDss(int phoneId, boolean state) {
        if (phoneId == 0) {
            this.mNeedSetDds1 = state;
        } else {
            this.mNeedSetDds2 = state;
        }
    }

    protected int oppoOosDelayState() {
        boolean isOos = ((getOemRegState(this.mNewSS) == 1 || getOemRegState(this.mNewSS) == 2) && this.mCi.getRadioState().isOn()) ? this.mSS.getState() != 3 : false;
        boolean inSwitchingDss = this.mSubscriptionController.getSwitchingDssState(this.mPhone.getPhoneId());
        int mSimState = SubscriptionManager.getSimStateForSlotIdx(this.mPhone.getPhoneId());
        boolean mNeedSetDdsState = getNeedSetDss(this.mPhone.getPhoneId());
        long currentTime = System.currentTimeMillis();
        if (DBG) {
            log("guix inSwitchingDss = " + inSwitchingDss + "  mSimState = " + mSimState + " PhoneId = " + this.mPhone.getPhoneId());
        }
        if (!isOos) {
            this.mOosDelayState = 0;
            removeMessages(OPPOEVENT_CHECK_NO_SERVICE);
        } else if (oppoIsOPhoneInCall()) {
            if (DBG) {
                log("oppoOosDelayState Other Phone is in call, do delay");
            }
            removeMessages(OPPOEVENT_CHECK_NO_SERVICE);
            sendEmptyMessageDelayed(OPPOEVENT_CHECK_NO_SERVICE, 23000);
            this.OosStartTime = currentTime;
            this.mOosDelayState = 1;
        } else if (inSwitchingDss || mSimState != 5 || mNeedSetDdsState) {
            if (DBG) {
                log("oppoOosDelayState should not delay oos");
            }
            setNeedSetDss(this.mPhone.getPhoneId(), false);
            this.mSubscriptionController.setSwitchingDssState(this.mPhone.getPhoneId(), false);
            this.mOosDelayState = 0;
            removeMessages(OPPOEVENT_CHECK_NO_SERVICE);
        } else if (this.mOosDelayState != 1) {
            if (DBG) {
                log("oppoOosDelayState send OPPOEVENT_CHECK_NO_SERVICE");
            }
            sendEmptyMessageDelayed(OPPOEVENT_CHECK_NO_SERVICE, 23000);
            this.OosStartTime = currentTime;
            this.mOosDelayState = 1;
        } else if (currentTime >= this.OosStartTime + 23000) {
            if (DBG) {
                log("oppoDelayOosState currentTime: " + currentTime + "  OosStartTime:" + this.OosStartTime);
            }
            this.mOosDelayState = 2;
            removeMessages(OPPOEVENT_CHECK_NO_SERVICE);
        }
        return this.mOosDelayState;
    }

    protected boolean oppoIsOPhoneInCall() {
        Phone oPhone = PhoneFactory.getPhone(1 - this.mPhone.getPhoneId());
        if (oPhone == null || oPhone.getState() == State.IDLE) {
            return false;
        }
        return true;
    }

    protected boolean oemNoCheckNIZ() {
        if (!this.mIsScreenOn) {
            if (this.mTimeCount >= 3) {
                if (DBG) {
                    log("oppo.leon don't niz when screen off");
                }
                return true;
            }
            this.mTimeCount++;
        }
        return false;
    }

    public void oemCallStateChange() {
    }

    protected boolean oemIsIVSR() {
        return false;
    }

    public String getTelephonyPowerState() {
        long escapeTime = this.mScreenOnTime - this.mScreenOffTime;
        if (escapeTime <= 0 || mNoServiceTime <= escapeTime) {
            return "DATA_CALL_COUNT:" + mDataCallCount + " NO_SERVICE_TIME:" + mNoServiceTime + " RESELECT_PER_MIN:" + mReselectCountPerMin + " SMS_SEND_COUNT:" + mSMSSendCount;
        }
        return "DATA_CALL_COUNT:" + mDataCallCount + " NO_SERVICE_TIME:" + escapeTime + " RESELECT_PER_MIN:" + mReselectCountPerMin + " SMS_SEND_COUNT:" + mSMSSendCount;
    }

    public boolean isAlreadyUpdated() {
        return mAlreadyUpdated;
    }

    public double getTelephonyPowerLost() {
        long escapeTime = this.mScreenOnTime - this.mScreenOffTime;
        if (escapeTime <= 0 || mNoServiceTime <= escapeTime) {
            return (((((double) mDataCallCount) * 0.4d) + (((double) mNoServiceTime) * 0.017d)) + (mReselectCount * 0.008d)) + (((double) mSMSSendCount) * 0.2d);
        }
        return (((((double) mDataCallCount) * 0.4d) + (((double) escapeTime) * 0.017d)) + (mReselectCount * 0.008d)) + (((double) mSMSSendCount) * 0.2d);
    }

    protected String getReadTeaServiceProviderName(Context context, String providerName) {
        String spn = UsimPBMemInfo.STRING_NOT_SET;
        if (TextUtils.isEmpty(providerName)) {
            return spn;
        }
        return context.getString(17041014);
    }

    protected void oppoSetAutoNetworkSelect() {
        if (!this.isRemoveCard && this.mNewSS.getIsManualSelection()) {
            if (DBG) {
                log("WXK : manual select network ---> automatic select network for oos > 23 secounds");
            }
            this.mPhone.setNetworkSelectionModeAutomatic(null);
        }
    }

    public void broadcastMccChange(String plmn) {
        int phoneId = this.mPhone.getPhoneId();
        if (phoneId >= 10) {
            phoneId -= 10;
        }
        String pMcc = UsimPBMemInfo.STRING_NOT_SET;
        if (plmn != null && !TextUtils.isEmpty(plmn) && isNumeric(plmn)) {
            pMcc = plmn.substring(0, 3);
            setMccProperties(phoneId, pMcc);
            Intent intent = new Intent("android.telephony.action.mcc_change");
            intent.putExtra("mcc", pMcc);
            intent.putExtra("slotid", phoneId);
            this.mPhone.getContext().sendBroadcast(intent);
            String sysMcc = SystemProperties.get("android.telephony.mcc_change", UsimPBMemInfo.STRING_NOT_SET);
            String sysMcc2 = SystemProperties.get("android.telephony.mcc_change2", UsimPBMemInfo.STRING_NOT_SET);
            if (DBG) {
                log("broadcastMccChange  sysMcc:" + sysMcc + "  sysMcc2:" + sysMcc2);
            }
        } else if (DBG) {
            log("broadcastMccChange  plmn is null, do not broadcast");
        }
    }

    public void cleanMccProperties(int phoneId) {
        String sysMcc = SystemProperties.get("android.telephony.mcc_change", UsimPBMemInfo.STRING_NOT_SET);
        String sysMcc2 = SystemProperties.get("android.telephony.mcc_change2", UsimPBMemInfo.STRING_NOT_SET);
        if (!TextUtils.isEmpty(sysMcc) && !TextUtils.isEmpty(sysMcc2)) {
            log("cleanMccProperties phoneId:" + phoneId + " sysMcc:" + sysMcc + " sysMcc2:" + sysMcc2);
            if (phoneId == 1 || phoneId == 11) {
                SystemProperties.set("android.telephony.mcc_change2", UsimPBMemInfo.STRING_NOT_SET);
            } else {
                SystemProperties.set("android.telephony.mcc_change", UsimPBMemInfo.STRING_NOT_SET);
            }
        }
    }

    public void setMccProperties(int phoneId, String mcc) {
        log("setMccProperties: phoneId = " + phoneId + "  mcc:" + mcc);
        if (this.mSubscriptionController.getActiveSubInfoCount(getClass().getPackage().getName()) <= 1) {
            SystemProperties.set("android.telephony.mcc_change", mcc);
            SystemProperties.set("android.telephony.mcc_change2", mcc);
        } else if (phoneId == 1 || phoneId == 11) {
            SystemProperties.set("android.telephony.mcc_change2", mcc);
        } else {
            SystemProperties.set("android.telephony.mcc_change", mcc);
        }
    }

    protected boolean oemPowerOffRadioSafely(Message msg) {
        int delay;
        try {
            delay = CallManager.getInstance().getState() == State.IDLE ? RegionLockConstant.EVENT_NETWORK_LOCK_STATUS : SIGNAL_SMOOTH_TIMER;
        } catch (Exception e) {
            delay = RegionLockConstant.EVENT_NETWORK_LOCK_STATUS;
        }
        boolean ret = sendMessageDelayed(msg, (long) delay);
        if (ret && DBG) {
            log("oem Wait upto " + (delay / 1000) + "s for data to disconnect, then turn off radio.");
        }
        return ret;
    }

    protected int getCombinedRegState() {
        int regState = this.mSS.getVoiceRegState();
        int dataRegState = this.mSS.getDataRegState();
        if (regState == 1 && dataRegState == 0) {
            return dataRegState;
        }
        return regState;
    }

    private void startResetNetworkStatusAlarm(int phoneId) {
        log("start Reset Alarm Exact start");
        int delayInMs = SystemProperties.getInt("persist.sys.oppo.locktime", 3600000);
        log("lock timer, delayInMs = " + delayInMs + "phoneId==" + phoneId);
        AlarmManager alarm = (AlarmManager) this.mPhone.getContext().getSystemService("alarm");
        Intent intent;
        if (phoneId == 1) {
            intent = new Intent(RegionLockConstant.ACTION_UNLOCK_NETWORK_SIM2);
            intent.putExtra("phoneId", phoneId);
            this.mResetIntentSlot2 = PendingIntent.getBroadcast(this.mPhone.getContext(), 1, intent, 134217728);
            alarm.setExact(2, SystemClock.elapsedRealtime() + ((long) delayInMs), this.mResetIntentSlot2);
            return;
        }
        intent = new Intent(RegionLockConstant.ACTION_UNLOCK_NETWORK_SIM1);
        intent.putExtra("phoneId", phoneId);
        this.mResetIntentSlot1 = PendingIntent.getBroadcast(this.mPhone.getContext(), 0, intent, 134217728);
        alarm.setExact(2, SystemClock.elapsedRealtime() + ((long) delayInMs), this.mResetIntentSlot1);
    }

    private void cancelNetworkStatusAlarm(int phoneId) {
        AlarmManager alarm = (AlarmManager) this.mPhone.getContext().getSystemService("alarm");
        if (phoneId == 1) {
            if (this.mResetIntentSlot2 != null) {
                alarm.cancel(this.mResetIntentSlot2);
                this.mResetIntentSlot2 = null;
            }
        } else if (this.mResetIntentSlot1 != null) {
            alarm.cancel(this.mResetIntentSlot1);
            this.mResetIntentSlot1 = null;
        }
    }

    private void oppoSetPowerRadioOff(int phoneId) {
        if (DBG) {
            log("Davis,oppoSetPowerRadioOff");
        }
        if (this.mOppoNeedSetRadio) {
            this.mOppoNeedSetRadio = false;
            Intent intent = new Intent(RegionLockConstant.ACTION_NETWORK_LOCK);
            intent.putExtra(RegionLockConstant.NETLOCK_STATUS, "1");
            sendBroadCastChangedNetlockStatus(intent);
        }
        RadioManager.getInstance().setRadioPower(false, phoneId);
    }

    private void sendBroadCastChangedNetlockStatus(Intent intent) {
        this.mPhone.getContext().sendBroadcast(intent);
    }

    private void updatePowerRadioStatus() {
        if (RegionLockConstant.IS_REGION_LOCK && RegionLockConstant.getRegionLockStatus()) {
            if (DBG) {
                log("Davis,updatePowerRadioStatus,mCallingPhoneId==" + this.mCallingPhoneId + ",phoneId==" + this.mPhone.getPhoneId());
            }
            if (this.mRegionLockPlmnList.oppoIsBlackListNetwork(RegionLockConstant.VERSION, this.mNewSS.getOperatorNumeric()) && this.mCallingPhoneId == this.mPhone.getPhoneId()) {
                RadioManager.getInstance().setRadioPower(false, this.mCallingPhoneId);
            }
        }
    }

    private boolean oppoIsTestCard() {
        if (this.mPhone.is_test_card()) {
            return true;
        }
        return false;
    }

    private boolean oppoFixZoneWithoutNitz(String mcc) {
        if (TextUtils.isEmpty(mcc) || !mcc.equals("250")) {
            return true;
        }
        log("250 not fix zone without nitz");
        return false;
    }
}
