package com.android.internal.telephony;

import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.radio.V1_0.CellIdentityCdma;
import android.hardware.radio.V1_0.CellIdentityGsm;
import android.hardware.radio.V1_0.CellIdentityLte;
import android.hardware.radio.V1_0.CellIdentityTdscdma;
import android.hardware.radio.V1_0.CellIdentityWcdma;
import android.hardware.radio.V1_0.DataRegStateResult;
import android.hardware.radio.V1_0.VoiceRegStateResult;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.NetworkRequest;
import android.os.AsyncResult;
import android.os.BaseBundle;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.WorkSource;
import android.preference.PreferenceManager;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Telephony.ServiceStateTable;
import android.telephony.CarrierConfigManager;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.OppoTelephonyConstant;
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
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.regionlock.RegionLockConstant;
import com.android.internal.telephony.regionlock.RegionLockPlmnListService;
import com.android.internal.telephony.test.SimulatedCommands;
import com.android.internal.telephony.uicc.IccCardApplicationStatus.AppState;
import com.android.internal.telephony.uicc.IccCardStatus.PinState;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.RuimRecords;
import com.android.internal.telephony.uicc.SIMRecords;
import com.android.internal.telephony.uicc.SpnOverride;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.PatternSyntaxException;

public class ServiceStateTracker extends Handler {
    /* renamed from: -com-android-internal-telephony-CommandsInterface$RadioStateSwitchesValues */
    private static final /* synthetic */ int[] f8xba025bb = null;
    private static final String ACTION_QTI_MCFG_CONFIG_CHANGE = "android.telephony.action.mcfg_change";
    private static final String ACTION_RADIO_OFF = "android.intent.action.ACTION_RADIO_OFF";
    protected static final int ALLOWED_NO_SERVICE_INTERVAL = 23000;
    public static final int CS_DISABLED = 1004;
    public static final int CS_EMERGENCY_ENABLED = 1006;
    public static final int CS_ENABLED = 1003;
    public static final int CS_NORMAL_ENABLED = 1005;
    public static final int CS_NOTIFICATION = 999;
    public static final int CS_REJECT_CAUSE_ENABLED = 2001;
    public static final int CS_REJECT_CAUSE_NOTIFICATION = 111;
    protected static final boolean DBG = OemConstant.SWITCH_LOG;
    public static final int DEFAULT_GPRS_CHECK_PERIOD_MILLIS = 60000;
    public static final String DEFAULT_MNC = "00";
    protected static final int EVENT_ALL_DATA_DISCONNECTED = 49;
    protected static final int EVENT_CDMA_PRL_VERSION_CHANGED = 40;
    protected static final int EVENT_CDMA_SUBSCRIPTION_SOURCE_CHANGED = 39;
    protected static final int EVENT_CHANGE_IMS_STATE = 45;
    protected static final int EVENT_CHECK_REPORT_GPRS = 22;
    protected static final int EVENT_ERI_FILE_LOADED = 36;
    protected static final int EVENT_GET_CELL_INFO_LIST = 43;
    protected static final int EVENT_GET_LOC_DONE = 15;
    protected static final int EVENT_GET_PREFERRED_NETWORK_TYPE = 19;
    protected static final int EVENT_GET_SIGNAL_STRENGTH = 3;
    protected static final int EVENT_GET_SIGNAL_STRENGTH_ONCE = 3000;
    public static final int EVENT_ICC_CHANGED = 42;
    protected static final int EVENT_IMS_CAPABILITY_CHANGED = 48;
    protected static final int EVENT_IMS_STATE_CHANGED = 46;
    protected static final int EVENT_IMS_STATE_DONE = 47;
    protected static final int EVENT_LOCATION_UPDATES_ENABLED = 18;
    protected static final int EVENT_MCFG_CONFIG_CHANGE = 3001;
    protected static final int EVENT_NETWORK_STATE_CHANGED = 2;
    protected static final int EVENT_NITZ_TIME = 11;
    protected static final int EVENT_NV_READY = 35;
    protected static final int EVENT_OEM_SCREEN_CHANGED = 150;
    private static final int EVENT_OEM_SMOOTH_0 = 101;
    private static final int EVENT_OEM_SMOOTH_1 = 102;
    protected static final int EVENT_OTA_PROVISION_STATUS_CHANGE = 37;
    protected static final int EVENT_PHONE_TYPE_SWITCHED = 50;
    protected static final int EVENT_POLL_SIGNAL_STRENGTH = 10;
    protected static final int EVENT_POLL_STATE_CDMA_SUBSCRIPTION = 34;
    protected static final int EVENT_POLL_STATE_GPRS = 5;
    protected static final int EVENT_POLL_STATE_NETWORK_SELECTION_MODE = 14;
    protected static final int EVENT_POLL_STATE_OPERATOR = 6;
    protected static final int EVENT_POLL_STATE_REGISTRATION = 4;
    protected static final int EVENT_RADIO_ON = 41;
    protected static final int EVENT_RADIO_POWER_FROM_CARRIER = 51;
    protected static final int EVENT_RADIO_POWER_OFF_DONE = 53;
    protected static final int EVENT_RADIO_STATE_CHANGED = 1;
    static final int EVENT_REGION_CHANGED = 4000;
    static final int EVENT_REGION_CHANGED_DONE = 4001;
    protected static final int EVENT_RESET_PREFERRED_NETWORK_TYPE = 21;
    protected static final int EVENT_RESTRICTED_STATE_CHANGED = 23;
    protected static final int EVENT_RUIM_READY = 26;
    protected static final int EVENT_RUIM_RECORDS_LOADED = 27;
    protected static final int EVENT_SET_PREFERRED_NETWORK_TYPE = 20;
    protected static final int EVENT_SET_RADIO_POWER_OFF = 38;
    protected static final int EVENT_SIGNAL_STRENGTH_UPDATE = 12;
    protected static final int EVENT_SIM_NOT_INSERTED = 52;
    protected static final int EVENT_SIM_READY = 17;
    protected static final int EVENT_SIM_RECORDS_LOADED = 16;
    protected static final int EVENT_UNSOL_CELL_INFO_LIST = 44;
    private static final String GAME_PACKAGE_NAME = "com.tencent.tmgp.sgame";
    protected static final String[] GMT_COUNTRY_CODES = new String[]{"bf", "ci", "eh", "fo", "gb", "gh", "gm", "gn", "gw", "ie", "lr", "is", "ma", "ml", "mr", "pt", "sl", "sn", "st", "tg"};
    private static final int INVALID_LTE_EARFCN = -1;
    public static final String INVALID_MCC = "000";
    protected static boolean IS_OEM_SMOOTH = OemConstant.SWITCH__SMOOTH;
    private static final long LAST_CELL_INFO_LIST_MAX_AGE_MS = 2000;
    private static final String LOG_TAG = "SST";
    private static final int MAX_NITZ_YEAR = 2037;
    protected static final int MCFG_CONFIG_CHANGE_DELAY = 500;
    public static final int MS_PER_HOUR = 3600000;
    private static final long NITZ_NTP_INTERVAL_OEM = 86400000;
    private static final long NITZ_NTP_INTERVAL_OEM_SECOND = 1000;
    public static final int NITZ_UPDATE_DIFF_DEFAULT = 2000;
    public static final int NITZ_UPDATE_SPACING_DEFAULT = 600000;
    protected static final int OOS_DELAY_NONE = 0;
    protected static final int OOS_DELAY_TIMEOUT = 2;
    protected static final int OOS_DELAY_TIMING = 1;
    protected static final int OPPOEVENT_CHECK_NO_SERVICE = 666;
    private static final int POLL_PERIOD_MILLIS = 20000;
    private static final String PROPERTY_USENTP_INTERVAL = "persist.sys.usentpinterval";
    private static final String PROPERTY_USENTP_TYPE = "persist.sys.usentptype";
    private static final String PROP_FORCE_ROAMING = "telephony.test.forceRoaming";
    public static final int PS_DISABLED = 1002;
    public static final int PS_ENABLED = 1001;
    public static final int PS_NOTIFICATION = 888;
    static final String REGION_CHANGED_ACTION = "android.settings.OPPO_REGION_CHANGED";
    protected static final String REGISTRATION_DENIED_AUTH = "Authentication Failure";
    protected static final String REGISTRATION_DENIED_GEN = "General";
    private static final int SIGNAL_SMOOTH_TIMER = 20000;
    private static final String TAG_ENABLE = "use_ntp_enable";
    private static final String TAG_ENABLE_VALUE = "1";
    private static final String TAG_INTERVAL = "use_ntp_interval";
    protected static final String TIMEZONE_PROPERTY = "persist.sys.timezone";
    private static final int TYPE_USENTP_CLOSE = 0;
    private static final int TYPE_USENTP_OPEN = 1;
    public static final String UNACTIVATED_MIN2_VALUE = "000000";
    public static final String UNACTIVATED_MIN_VALUE = "1111110111";
    private static final int USE_NTP_TIME_INTERVAL_LESS = 2;
    private static final int USE_NTP_TIME_INTERVAL_MORE = 1;
    private static final int USE_NTP_TIME_NONE = 0;
    private static final boolean VDBG = false;
    public static final String WAKELOCK_TAG = "ServiceStateTracker";
    public static final String[][] customEhplmn;
    public static boolean mAlreadyUpdated = false;
    private static long[] mBeginNoServiceTime = new long[]{0, 0};
    private static long mCidChangeCount = 0;
    public static int mDataCallCount = 0;
    private static boolean mIsUseNtpTime = false;
    public static int mNITZCount = 0;
    public static long mNoServiceTime = 0;
    public static double mReselectCount = 0.0d;
    public static double mReselectCountPerMin = 0.0d;
    public static int mSMSSendCount = 0;
    private static long mUseNtpInterval = 0;
    protected long OosStartTime = -1;
    public long SAMPLE_TIME;
    protected boolean bCheckNoServiceAgain = true;
    protected boolean isCardLocked = false;
    private volatile boolean isInCall = false;
    protected boolean isRemoveCard = false;
    protected long lastTime = 0;
    private boolean mAlarmSwitch = false;
    private final LocalLog mAttachLog = new LocalLog(10);
    protected RegistrantList mAttachedRegistrants = new RegistrantList();
    private ContentObserver mAutoTimeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            Rlog.i(ServiceStateTracker.LOG_TAG, "Auto time state changed");
            ServiceStateTracker.this.revertToNitzTime();
        }
    };
    private ContentObserver mAutoTimeZoneObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            Rlog.i(ServiceStateTracker.LOG_TAG, "Auto time zone state changed");
            ServiceStateTracker.this.revertToNitzTimeZone();
        }
    };
    private CarrierServiceStateTracker mCSST;
    private RegistrantList mCdmaForSubscriptionInfoReadyRegistrants = new RegistrantList();
    private CdmaSubscriptionSourceManager mCdmaSSM;
    public CellLocation mCellLoc;
    private CommandsInterface mCi;
    private ConnectivityManager mConnectivityManager;
    private ContentResolver mCr;
    private String mCurDataSpn = null;
    private String mCurPlmn = null;
    private boolean mCurShowPlmn = false;
    private boolean mCurShowSpn = false;
    private String mCurSpn = null;
    private String mCurrentCarrier = null;
    private int mCurrentOtaspMode = 0;
    private boolean mCurrentUpdateOOSTime = false;
    private RegistrantList mDataRegStateOrRatChangedRegistrants = new RegistrantList();
    private boolean mDataRoaming = false;
    private RegistrantList mDataRoamingOffRegistrants = new RegistrantList();
    private RegistrantList mDataRoamingOnRegistrants = new RegistrantList();
    private int mDefaultRoamingIndicator;
    private boolean mDesiredPowerState;
    protected RegistrantList mDetachedRegistrants = new RegistrantList();
    private boolean mDeviceShuttingDown = false;
    private boolean mDontPollSignalStrength = false;
    private ArrayList<Pair<Integer, Integer>> mEarfcnPairListForRsrpBoost = null;
    private boolean mEmergencyOnly = false;
    private boolean mGotCountryCode = false;
    private boolean mGsmRoaming = false;
    private HbpcdUtils mHbpcdUtils = null;
    private int[] mHomeNetworkId = null;
    private int[] mHomeSystemId = null;
    private IccRecords mIccRecords = null;
    private boolean mImsRegistered = false;
    private boolean mImsRegistrationOnOff = false;
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                ServiceStateTracker.this.updateLteEarfcnLists();
                return;
            }
            if (ServiceStateTracker.DBG) {
                ServiceStateTracker.this.log("onReceive intent: " + intent);
            }
            if (intent.getAction().equals("android.intent.action.LOCALE_CHANGED")) {
                ServiceStateTracker.this.updateSpnDisplay();
            } else if (intent.getAction().equals(ServiceStateTracker.ACTION_RADIO_OFF)) {
                ServiceStateTracker.this.mAlarmSwitch = false;
                ServiceStateTracker.this.powerOffRadioSafely(ServiceStateTracker.this.mPhone.mDcTracker);
            } else if (intent.getAction().equals(ServiceStateTracker.REGION_CHANGED_ACTION)) {
                ServiceStateTracker.this.log("[oppo exp] REGION_CHANGED to:" + SystemProperties.get("persist.sys.oppo.region", SpnOverride.MVNO_TYPE_NONE));
                if (ServiceStateTracker.this.sendMessage(ServiceStateTracker.this.obtainMessage(ServiceStateTracker.EVENT_REGION_CHANGED))) {
                    ServiceStateTracker.this.log("[oppo exp] send REGION_CHANGED");
                } else {
                    ServiceStateTracker.this.log("[oppo exp] Cannot send Msg!");
                }
            } else if (intent.getAction().equals(RegionLockConstant.ACTION_UNLOCK_NETWORK_SIM1) || intent.getAction().equals(RegionLockConstant.ACTION_UNLOCK_NETWORK_SIM2)) {
                ServiceStateTracker.this.log("ACTION_UNLOCK_NETWORK,phoneId==" + intent.getIntExtra("phoneId", 0) + ",getPhoneId()==" + ServiceStateTracker.this.getPhoneId());
                if (intent.getIntExtra("phoneId", 0) == ServiceStateTracker.this.getPhoneId()) {
                    ServiceStateTracker.this.sendMessage(ServiceStateTracker.this.obtainMessage(RegionLockConstant.EVENT_NETWORK_LOCK_STATUS));
                }
            } else if (intent.getAction().equals(ServiceStateTracker.ACTION_QTI_MCFG_CONFIG_CHANGE)) {
                ServiceStateTracker.this.log("yipeng.sun get android.telephony.action.mcfg_change");
                if (ServiceStateTracker.this.hasMessages(ServiceStateTracker.EVENT_MCFG_CONFIG_CHANGE)) {
                    ServiceStateTracker.this.removeMessages(ServiceStateTracker.EVENT_MCFG_CONFIG_CHANGE);
                }
                ServiceStateTracker.this.log("yipeng.sun send EVENT_MCFG_CONFIG_CHANGE");
                ServiceStateTracker.this.sendEmptyMessageDelayed(ServiceStateTracker.EVENT_MCFG_CONFIG_CHANGE, 500);
            }
        }
    };
    private boolean mIsEriTextLoaded = false;
    private boolean mIsInPrl;
    private boolean mIsMinInfoReady = false;
    private boolean mIsModemTriggeredPollingPending = false;
    private boolean mIsPendingNotify_0 = false;
    private boolean mIsPendingNotify_1 = false;
    private boolean mIsPsReg;
    protected boolean mIsScreenOn = true;
    private boolean mIsSubscriptionFromRuim = false;
    private List<CellInfo> mLastCellInfoList = null;
    private long mLastCellInfoListTime;
    private SignalStrength mLastSignalStrength = null;
    private boolean mLastUpdateOOSTime = false;
    private int mLteRsrpBoost = 0;
    private final Object mLteRsrpBoostLock = new Object();
    private int mMaxDataCalls = 1;
    private String mMdn;
    private String mMin;
    private boolean mNeedFixZoneAfterNitz = false;
    private RegistrantList mNetworkAttachedRegistrants = new RegistrantList();
    private RegistrantList mNetworkDetachedRegistrants = new RegistrantList();
    public CellLocation mNewCellLoc;
    private int mNewMaxDataCalls = 1;
    protected String mNewPlmn = null;
    private int mNewReasonDataDenied = -1;
    private int mNewRejectCode;
    private ServiceState mNewSS;
    private int mNitzUpdateDiff = SystemProperties.getInt("ro.nitz_update_diff", NITZ_UPDATE_DIFF_DEFAULT);
    private int mNitzUpdateSpacing = SystemProperties.getInt("ro.nitz_update_spacing", NITZ_UPDATE_SPACING_DEFAULT);
    private boolean mNitzUpdatedTime = false;
    private Notification mNotification;
    private int mOEMCurLevel_0 = -1;
    private int mOEMCurLevel_1 = -1;
    private int mOEMLastLevel_0 = -1;
    private int mOEMLastLevel_1 = -1;
    protected String mOemSpn = SpnOverride.MVNO_TYPE_NONE;
    private final SstSubscriptionsChangedListener mOnSubscriptionsChangedListener = new SstSubscriptionsChangedListener(this, null);
    protected int mOosDelayState = 0;
    protected boolean mOppoNeedNotify = false;
    private boolean mPendingRadioPowerOffAfterDataOff = false;
    private int mPendingRadioPowerOffAfterDataOffTag = 0;
    private double mPerDataCallPowerLost;
    private double mPerNITZLost;
    private double mPerNoServicePowerLost;
    private double mPerReselectLost;
    private double mPerSMSSendLost;
    protected GsmCdmaPhone mPhone;
    private final LocalLog mPhoneTypeLog = new LocalLog(10);
    protected int[] mPollingContext;
    private boolean mPowerOffDelayNeed = true;
    private int mPreferredNetworkType;
    private String mPrlVersion;
    private RegistrantList mPsRestrictDisabledRegistrants = new RegistrantList();
    private RegistrantList mPsRestrictEnabledRegistrants = new RegistrantList();
    private boolean mRadioDisabledByCarrier = false;
    private PendingIntent mRadioOffIntent = null;
    private final LocalLog mRadioPowerLog = new LocalLog(20);
    private final LocalLog mRatLog = new LocalLog(20);
    private final RatRatcheter mRatRatcheter;
    private int mReasonDataDenied = -1;
    private String mRegistrationDeniedReason;
    private int mRegistrationState = -1;
    private int mRejectCode;
    private boolean mReportedGprsNoReg;
    private PendingIntent mResetIntentSlot1 = null;
    private PendingIntent mResetIntentSlot2 = null;
    public RestrictedState mRestrictedState;
    private int mRoamingIndicator;
    private final LocalLog mRoamingLog = new LocalLog(10);
    public ServiceState mSS;
    private long mSavedAtTime;
    private long mSavedTime;
    private String mSavedTimeZone;
    public long mScreenOffTime;
    public long mScreenOnTime;
    protected boolean mShowPlmn;
    protected boolean mShowSPn;
    private SignalStrength mSignalStrength;
    private boolean mSpnUpdatePending = false;
    private boolean mStartedGprsRegCheck;
    private int mSubId = -1;
    private SubscriptionController mSubscriptionController;
    private SubscriptionManager mSubscriptionManager;
    protected int mTimeCount = 0;
    private final LocalLog mTimeLog = new LocalLog(15);
    protected String[][] mTimeZoneIdOfCapitalCity;
    private final LocalLog mTimeZoneLog = new LocalLog(15);
    private UiccCardApplication mUiccApplcation = null;
    private UiccController mUiccController = null;
    private boolean mVoiceCapable;
    private RegistrantList mVoiceRoamingOffRegistrants = new RegistrantList();
    private RegistrantList mVoiceRoamingOnRegistrants = new RegistrantList();
    private WakeLock mWakeLock;
    private boolean mWantContinuousLocationUpdates;
    private boolean mWantSingleLocationUpdate;
    private boolean mZoneDst;
    private int mZoneOffset;
    private long mZoneTime;
    protected int mphoneid = -1;
    private int oosFlag = -1;
    private boolean oppoNeedSetAlarm = true;
    private boolean oppoNeedSetRadio = true;
    private boolean oppoSignalUpdate = false;
    private RegionLockPlmnListService regionLockPlmnList;

    private class CellInfoResult {
        List<CellInfo> list;
        Object lockObj;

        /* synthetic */ CellInfoResult(ServiceStateTracker this$0, CellInfoResult -this1) {
            this();
        }

        private CellInfoResult() {
            this.lockObj = new Object();
        }
    }

    private class SstSubscriptionsChangedListener extends OnSubscriptionsChangedListener {
        public final AtomicInteger mPreviousSubId;

        /* synthetic */ SstSubscriptionsChangedListener(ServiceStateTracker this$0, SstSubscriptionsChangedListener -this1) {
            this();
        }

        private SstSubscriptionsChangedListener() {
            this.mPreviousSubId = new AtomicInteger(-1);
        }

        public void onSubscriptionsChanged() {
            if (ServiceStateTracker.DBG) {
                ServiceStateTracker.this.log("SubscriptionListener.onSubscriptionInfoChanged");
            }
            int subId = ServiceStateTracker.this.mPhone.getSubId();
            if (this.mPreviousSubId.getAndSet(subId) != subId) {
                if (ServiceStateTracker.this.mSubscriptionController.isActiveSubId(subId)) {
                    Context context = ServiceStateTracker.this.mPhone.getContext();
                    ServiceStateTracker.this.mPhone.notifyPhoneStateChanged();
                    ServiceStateTracker.this.mPhone.notifyCallForwardingIndicator();
                    ServiceStateTracker.this.mPhone.sendSubscriptionSettings(context.getResources().getBoolean(17957095) ^ 1);
                    ServiceStateTracker.this.mPhone.setSystemProperty("gsm.network.type", ServiceState.rilRadioTechnologyToString(ServiceStateTracker.this.mSS.getRilDataRadioTechnology()));
                    if (ServiceStateTracker.this.mSpnUpdatePending) {
                        ServiceStateTracker.this.mSubscriptionController.setPlmnSpn(ServiceStateTracker.this.mPhone.getPhoneId(), ServiceStateTracker.this.mCurShowPlmn, ServiceStateTracker.this.mCurPlmn, ServiceStateTracker.this.mCurShowSpn, ServiceStateTracker.this.mCurSpn);
                        ServiceStateTracker.this.mSpnUpdatePending = false;
                    }
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                    String oldNetworkSelection = sp.getString(Phone.NETWORK_SELECTION_KEY, SpnOverride.MVNO_TYPE_NONE);
                    String oldNetworkSelectionName = sp.getString(Phone.NETWORK_SELECTION_NAME_KEY, SpnOverride.MVNO_TYPE_NONE);
                    String oldNetworkSelectionShort = sp.getString(Phone.NETWORK_SELECTION_SHORT_KEY, SpnOverride.MVNO_TYPE_NONE);
                    if (!(TextUtils.isEmpty(oldNetworkSelection) && (TextUtils.isEmpty(oldNetworkSelectionName) ^ 1) == 0 && (TextUtils.isEmpty(oldNetworkSelectionShort) ^ 1) == 0)) {
                        Editor editor = sp.edit();
                        editor.putString(Phone.NETWORK_SELECTION_KEY + subId, oldNetworkSelection);
                        editor.putString(Phone.NETWORK_SELECTION_NAME_KEY + subId, oldNetworkSelectionName);
                        editor.putString(Phone.NETWORK_SELECTION_SHORT_KEY + subId, oldNetworkSelectionShort);
                        editor.remove(Phone.NETWORK_SELECTION_KEY);
                        editor.remove(Phone.NETWORK_SELECTION_NAME_KEY);
                        editor.remove(Phone.NETWORK_SELECTION_SHORT_KEY);
                        editor.commit();
                    }
                    ServiceStateTracker.this.updateSpnDisplay();
                }
                ServiceStateTracker.this.mPhone.updateVoiceMail();
                if (ServiceStateTracker.this.mSubscriptionController.getSlotIndex(subId) == -1) {
                    ServiceStateTracker.this.sendMessage(ServiceStateTracker.this.obtainMessage(52));
                }
            }
        }
    }

    /* renamed from: -getcom-android-internal-telephony-CommandsInterface$RadioStateSwitchesValues */
    private static /* synthetic */ int[] m9x804b995f() {
        if (f8xba025bb != null) {
            return f8xba025bb;
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
        f8xba025bb = iArr;
        return iArr;
    }

    static {
        String[][] strArr = new String[28][];
        strArr[0] = new String[]{"46000", "46002", "46007", "46008"};
        strArr[1] = new String[]{"45400", "45402", "45418"};
        strArr[2] = new String[]{"46001", "46009"};
        strArr[3] = new String[]{"45403", "45404"};
        strArr[4] = new String[]{"45412", "45413"};
        strArr[5] = new String[]{"45416", "45419"};
        strArr[6] = new String[]{"45501", "45504"};
        strArr[7] = new String[]{"45503", "45505"};
        strArr[8] = new String[]{"45002", "45008"};
        strArr[9] = new String[]{"52501", "52502"};
        strArr[10] = new String[]{"43602", "43612"};
        strArr[11] = new String[]{"50212", "50218"};
        strArr[12] = new String[]{"40404", "40411"};
        strArr[13] = new String[]{"52000", "52004", "52099"};
        strArr[14] = new String[]{"52001", "52003"};
        strArr[15] = new String[]{"52010", "52099"};
        strArr[16] = new String[]{"52005", "52018"};
        strArr[17] = new String[]{"52010", "52099"};
        strArr[18] = new String[]{"24001", "24005"};
        strArr[19] = new String[]{"26207", "26208"};
        strArr[20] = new String[]{"23430", "23431", "23432", "23433", "23434"};
        strArr[21] = new String[]{"72402", "72403", "72404"};
        strArr[22] = new String[]{"72406", "72410", "72411", "72423"};
        strArr[23] = new String[]{"72432", "72433", "72434"};
        strArr[24] = new String[]{"31026", "31031", "310160", "310200", "310210", "310220", "310230", "310240", "310250", SimulatedCommands.FAKE_MCC_MNC, "310270", "310280", "311290", "310300", "310310", "310320", "311330", "310660", "310800"};
        strArr[25] = new String[]{"310150", "310170", "310380", "310410"};
        strArr[26] = new String[]{"31033", "310330"};
        strArr[27] = new String[]{"21401", "21402", "21403", "21404", "21405", "21406", "21407", "21408", "21409", "21410", "21411", "21412", "21413", "21414", "21415", "21416", "21417", "21418", "21419", "21420", "21421"};
        customEhplmn = strArr;
    }

    public ServiceStateTracker(GsmCdmaPhone phone, CommandsInterface ci) {
        boolean z;
        String[][] strArr = new String[1][];
        strArr[0] = new String[]{"cn", "Asia/Shanghai"};
        this.mTimeZoneIdOfCapitalCity = strArr;
        this.mScreenOffTime = 0;
        this.mScreenOnTime = 0;
        this.SAMPLE_TIME = 5;
        this.mPerDataCallPowerLost = 0.6d;
        this.mPerNoServicePowerLost = 0.017d;
        this.mPerReselectLost = 0.008d;
        this.mPerSMSSendLost = 0.3d;
        this.mPerNITZLost = 0.05d;
        this.mIsPsReg = false;
        this.mConnectivityManager = null;
        this.mPhone = phone;
        this.mCi = ci;
        this.mRatRatcheter = new RatRatcheter(this.mPhone);
        this.mVoiceCapable = this.mPhone.getContext().getResources().getBoolean(17957059);
        this.mUiccController = UiccController.getInstance();
        this.mUiccController.registerForIccChanged(this, 42, null);
        this.mCi.setOnSignalStrengthUpdate(this, 12, null);
        this.mCi.registerForCellInfoList(this, 44, null);
        this.mSubscriptionController = SubscriptionController.getInstance();
        this.mSubscriptionManager = SubscriptionManager.from(phone.getContext());
        this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        this.mRestrictedState = new RestrictedState();
        this.mCi.registerForImsNetworkStateChanged(this, 46, null);
        this.mWakeLock = ((PowerManager) phone.getContext().getSystemService("power")).newWakeLock(1, WAKELOCK_TAG);
        this.mCi.registerForRadioStateChanged(this, 1, null);
        this.mCi.registerForNetworkStateChanged(this, 2, null);
        this.mCi.setOnNITZTime(this, 11, null);
        this.mCr = phone.getContext().getContentResolver();
        int airplaneMode = Global.getInt(this.mCr, "airplane_mode_on", 0);
        int enableCellularOnBoot = Global.getInt(this.mCr, "enable_cellular_on_boot", 1);
        if (enableCellularOnBoot <= 0 || airplaneMode > 0) {
            z = false;
        } else {
            z = true;
        }
        this.mDesiredPowerState = z;
        this.mRadioPowerLog.log("init : airplane mode = " + airplaneMode + " enableCellularOnBoot = " + enableCellularOnBoot);
        this.mCr.registerContentObserver(Global.getUriFor("auto_time"), true, this.mAutoTimeObserver);
        this.mCr.registerContentObserver(Global.getUriFor("auto_time_zone"), true, this.mAutoTimeZoneObserver);
        setSignalStrengthDefaultValues();
        this.mPhone.getCarrierActionAgent().registerForCarrierAction(1, this, 51, null, false);
        Context context = this.mPhone.getContext();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.LOCALE_CHANGED");
        context.registerReceiver(this.mIntentReceiver, filter);
        filter = new IntentFilter();
        filter.addAction(ACTION_RADIO_OFF);
        context.registerReceiver(this.mIntentReceiver, filter);
        filter = new IntentFilter();
        filter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        context.registerReceiver(this.mIntentReceiver, filter);
        filter = new IntentFilter();
        filter.addAction(REGION_CHANGED_ACTION);
        if (OemConstant.EXP_VERSION && RegionLockConstant.IS_REGION_LOCK) {
            filter.addAction(RegionLockConstant.ACTION_UNLOCK_NETWORK_SIM1);
            filter.addAction(RegionLockConstant.ACTION_UNLOCK_NETWORK_SIM2);
            filter.addAction(RegionLockConstant.ACTION_CALLING_DISCONNECTED);
            this.regionLockPlmnList = RegionLockPlmnListService.getInstance(phone.getContext());
        }
        context.registerReceiver(this.mIntentReceiver, filter);
        filter = new IntentFilter();
        filter.addAction(ACTION_QTI_MCFG_CONFIG_CHANGE);
        context.registerReceiver(this.mIntentReceiver, filter);
        this.mPhone.notifyOtaspChanged(0);
        this.mCi.setOnRestrictedStateChanged(this, 23, null);
        updatePhoneType();
        this.mCSST = new CarrierServiceStateTracker(phone, this);
        registerForNetworkAttached(this.mCSST, 101, null);
        registerForNetworkDetached(this.mCSST, 102, null);
        registerForDataConnectionAttached(this.mCSST, 103, null);
        registerForDataConnectionDetached(this.mCSST, 104, null);
        OppoGsmServiceStateTracker.oppoNvCheckAndRestore(phone.getContext());
        this.mPhone.getDeviceStateMonitor().registerForOemScreenChanged(this, 150, null);
        if (getUseNtpType() == 0) {
            mIsUseNtpTime = false;
        } else {
            mIsUseNtpTime = true;
        }
        mUseNtpInterval = getUseNtpInterval();
    }

    public void updatePhoneType() {
        if (this.mSS != null && this.mSS.getVoiceRoaming()) {
            this.mVoiceRoamingOffRegistrants.notifyRegistrants();
        }
        if (this.mSS != null && this.mSS.getDataRoaming()) {
            this.mDataRoamingOffRegistrants.notifyRegistrants();
        }
        if (this.mSS != null && this.mSS.getDataRegState() == 0) {
            this.mDetachedRegistrants.notifyRegistrants();
        }
        this.mSS = new ServiceState();
        this.mNewSS = new ServiceState();
        this.mSS.setCdmaEriIconIndex(-1);
        this.mSS.setCdmaEriIconMode(-1);
        this.mNewSS.setCdmaEriIconIndex(-1);
        this.mNewSS.setCdmaEriIconMode(-1);
        this.mLastCellInfoListTime = 0;
        this.mLastCellInfoList = null;
        this.mSignalStrength = new SignalStrength();
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
        } else {
            this.mPhone.registerForSimRecordsLoaded(this, 16, null);
            this.mCellLoc = new CdmaCellLocation();
            this.mNewCellLoc = new CdmaCellLocation();
            this.mCdmaSSM = CdmaSubscriptionSourceManager.getInstance(this.mPhone.getContext(), this.mCi, this, 39, null);
            this.mIsSubscriptionFromRuim = this.mCdmaSSM.getCdmaSubscriptionSource() == 0;
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
        notifyDataRegStateRilRadioTechnologyChanged();
    }

    public void requestShutdown() {
        if (!this.mDeviceShuttingDown) {
            this.mDeviceShuttingDown = true;
            this.mDesiredPowerState = false;
            setPowerStateToDesired();
        }
    }

    public void dispose() {
        this.mPhone.getDeviceStateMonitor().unregisterOemScreenChanged(this);
        this.mCi.unSetOnSignalStrengthUpdate(this);
        this.mUiccController.unregisterForIccChanged(this);
        this.mCi.unregisterForCellInfoList(this);
        this.mSubscriptionManager.removeOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        this.mCi.unregisterForImsNetworkStateChanged(this);
        this.mPhone.getCarrierActionAgent().unregisterForCarrierAction(this, 1);
    }

    public boolean getDesiredPowerState() {
        return this.mDesiredPowerState;
    }

    public boolean getPowerStateFromCarrier() {
        return this.mRadioDisabledByCarrier ^ 1;
    }

    protected boolean notifySignalStrength() {
        if (this.mSignalStrength.equals(this.mLastSignalStrength)) {
            return false;
        }
        try {
            this.mPhone.notifySignalStrength();
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
        if (!this.mVoiceCapable) {
            if (DBG) {
                log("useDataRegStateForDataOnlyDevice: VoiceRegState=" + this.mNewSS.getVoiceRegState() + " DataRegState=" + this.mNewSS.getDataRegState());
            }
            this.mNewSS.setVoiceRegState(this.mNewSS.getDataRegState());
        }
    }

    protected void updatePhoneObject() {
        if (this.mPhone.getContext().getResources().getBoolean(17957040)) {
            boolean isRegistered = this.mSS.getVoiceRegState() != 0 ? this.mSS.getVoiceRegState() == 2 : true;
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

    public void registerForDataRoamingOff(Handler h, int what, Object obj, boolean notifyNow) {
        Registrant r = new Registrant(h, what, obj);
        this.mDataRoamingOffRegistrants.add(r);
        if (notifyNow && (this.mSS.getDataRoaming() ^ 1) != 0) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForDataRoamingOff(Handler h) {
        this.mDataRoamingOffRegistrants.remove(h);
    }

    public void reRegisterNetwork(Message onComplete) {
        this.mCi.getPreferredNetworkType(obtainMessage(19, onComplete));
    }

    public void setRadioPower(boolean power) {
        this.mDesiredPowerState = power;
        setPowerStateToDesired();
    }

    public void setRadioPowerFromCarrier(boolean enable) {
        this.mRadioDisabledByCarrier = enable ^ 1;
        setPowerStateToDesired();
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
        if (!this.mWantSingleLocationUpdate && (this.mWantContinuousLocationUpdates ^ 1) != 0) {
            this.mCi.setLocationUpdates(false, null);
        }
    }

    public void disableLocationUpdates() {
        this.mWantContinuousLocationUpdates = false;
        if (!this.mWantSingleLocationUpdate && (this.mWantContinuousLocationUpdates ^ 1) != 0) {
            this.mCi.setLocationUpdates(false, null);
        }
    }

    private void processCellLocationInfo(CellLocation cellLocation, VoiceRegStateResult voiceRegStateResult) {
        if (this.mPhone.isPhoneTypeGsm()) {
            int psc = -1;
            int cid = -1;
            int lac = -1;
            switch (voiceRegStateResult.cellIdentity.cellInfoType) {
                case 1:
                    if (voiceRegStateResult.cellIdentity.cellIdentityGsm.size() == 1) {
                        CellIdentityGsm cellIdentityGsm = (CellIdentityGsm) voiceRegStateResult.cellIdentity.cellIdentityGsm.get(0);
                        cid = cellIdentityGsm.cid;
                        lac = cellIdentityGsm.lac;
                        break;
                    }
                    break;
                case 3:
                    if (voiceRegStateResult.cellIdentity.cellIdentityLte.size() == 1) {
                        CellIdentityLte cellIdentityLte = (CellIdentityLte) voiceRegStateResult.cellIdentity.cellIdentityLte.get(0);
                        cid = cellIdentityLte.ci;
                        lac = cellIdentityLte.tac;
                        break;
                    }
                    break;
                case 4:
                    if (voiceRegStateResult.cellIdentity.cellIdentityWcdma.size() == 1) {
                        CellIdentityWcdma cellIdentityWcdma = (CellIdentityWcdma) voiceRegStateResult.cellIdentity.cellIdentityWcdma.get(0);
                        cid = cellIdentityWcdma.cid;
                        lac = cellIdentityWcdma.lac;
                        psc = cellIdentityWcdma.psc;
                        break;
                    }
                    break;
                case 5:
                    if (voiceRegStateResult.cellIdentity.cellIdentityTdscdma.size() == 1) {
                        CellIdentityTdscdma cellIdentityTdscdma = (CellIdentityTdscdma) voiceRegStateResult.cellIdentity.cellIdentityTdscdma.get(0);
                        cid = cellIdentityTdscdma.cid;
                        lac = cellIdentityTdscdma.lac;
                        break;
                    }
                    break;
            }
            if (lac > 0 && cid == Integer.MAX_VALUE) {
                try {
                    if (!(this.mCellLoc == null || ((GsmCellLocation) this.mCellLoc).getCid() == Integer.MAX_VALUE)) {
                        cid = ((GsmCellLocation) this.mCellLoc).getCid();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            ((GsmCellLocation) cellLocation).setLacAndCid(lac, cid);
            ((GsmCellLocation) cellLocation).setPsc(psc);
            return;
        }
        int baseStationId = -1;
        int baseStationLatitude = Integer.MAX_VALUE;
        int baseStationLongitude = Integer.MAX_VALUE;
        int systemId = 0;
        int networkId = 0;
        switch (voiceRegStateResult.cellIdentity.cellInfoType) {
            case 2:
                if (voiceRegStateResult.cellIdentity.cellIdentityCdma.size() == 1) {
                    CellIdentityCdma cellIdentityCdma = (CellIdentityCdma) voiceRegStateResult.cellIdentity.cellIdentityCdma.get(0);
                    baseStationId = cellIdentityCdma.baseStationId;
                    baseStationLatitude = cellIdentityCdma.latitude;
                    baseStationLongitude = cellIdentityCdma.longitude;
                    systemId = cellIdentityCdma.systemId;
                    networkId = cellIdentityCdma.networkId;
                    break;
                }
                break;
        }
        if (baseStationLatitude == 0 && baseStationLongitude == 0) {
            baseStationLatitude = Integer.MAX_VALUE;
            baseStationLongitude = Integer.MAX_VALUE;
        }
        ((CdmaCellLocation) cellLocation).setCellLocationData(baseStationId, baseStationLatitude, baseStationLongitude, systemId, networkId);
    }

    private int getLteEarfcn(DataRegStateResult dataRegStateResult) {
        switch (dataRegStateResult.cellIdentity.cellInfoType) {
            case 3:
                if (dataRegStateResult.cellIdentity.cellIdentityLte.size() == 1) {
                    return ((CellIdentityLte) dataRegStateResult.cellIdentity.cellIdentityLte.get(0)).earfcn;
                }
                return -1;
            default:
                return -1;
        }
    }

    /* JADX WARNING: Missing block: B:3:0x002a, code:
            return;
     */
    /* JADX WARNING: Missing block: B:75:0x02b7, code:
            handlePollStateResult(r33.what, (android.os.AsyncResult) r33.obj);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void handleMessage(Message msg) {
        AsyncResult ar;
        switch (msg.what) {
            case 1:
            case 50:
                if (!this.mPhone.isPhoneTypeGsm() && this.mCi.getRadioState() == RadioState.RADIO_ON) {
                    handleCdmaSubscriptionSource(this.mCdmaSSM.getCdmaSubscriptionSource());
                    queueNextSignalStrengthPoll();
                }
                setPowerStateToDesired();
                modemTriggeredPollState();
                break;
            case 2:
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
            case 6:
                break;
            case 5:
                onPsNetworkStateChangeResult((AsyncResult) msg.obj);
                break;
            case 10:
                this.mCi.getSignalStrength(obtainMessage(3));
                break;
            case 11:
                ar = (AsyncResult) msg.obj;
                setTimeFromNITZString(((Object[]) ar.result)[0], ((Long) ((Object[]) ar.result)[1]).longValue());
                break;
            case 12:
                ar = (AsyncResult) msg.obj;
                this.mDontPollSignalStrength = true;
                onSignalStrengthResult(ar);
                break;
            case 14:
                if (DBG) {
                    log("EVENT_POLL_STATE_NETWORK_SELECTION_MODE");
                }
                ar = (AsyncResult) msg.obj;
                if (!this.mPhone.isPhoneTypeGsm()) {
                    if (ar.exception == null && ar.result != null) {
                        if (ar.result[0] == 1) {
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
                    processCellLocationInfo(this.mCellLoc, (VoiceRegStateResult) ar.result);
                    this.mPhone.notifyLocationChanged();
                }
                disableSingleLocationUpdate();
                break;
            case 16:
                log("EVENT_SIM_RECORDS_LOADED: what=" + msg.what);
                updatePhoneObject();
                updateOtaspState();
                if (this.mPhone.isPhoneTypeGsm()) {
                    updateSpnDisplay();
                    break;
                }
                break;
            case 17:
                this.mOnSubscriptionsChangedListener.mPreviousSubId.set(-1);
                pollState();
                queueNextSignalStrengthPoll();
                break;
            case 18:
                if (((AsyncResult) msg.obj).exception == null) {
                    this.mCi.getVoiceRegistrationState(obtainMessage(15, null));
                    break;
                }
                break;
            case 19:
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    this.mPreferredNetworkType = ((int[]) ar.result)[0];
                } else {
                    this.mPreferredNetworkType = 7;
                }
                this.mCi.setPreferredNetworkType(7, obtainMessage(20, ar.userObj));
                break;
            case 20:
                this.mCi.setPreferredNetworkType(this.mPreferredNetworkType, obtainMessage(21, ((AsyncResult) msg.obj).userObj));
                break;
            case 21:
                ar = (AsyncResult) msg.obj;
                if (ar.userObj != null) {
                    AsyncResult.forMessage((Message) ar.userObj).exception = ar.exception;
                    ((Message) ar.userObj).sendToTarget();
                    break;
                }
                break;
            case 22:
                if (this.mPhone.isPhoneTypeGsm() && this.mSS != null) {
                    if ((isGprsConsistent(this.mSS.getDataRegState(), this.mSS.getVoiceRegState()) ^ 1) != 0) {
                        GsmCellLocation loc = (GsmCellLocation) this.mPhone.getCellLocation();
                        Object[] objArr = new Object[2];
                        objArr[0] = this.mSS.getOperatorNumeric();
                        objArr[1] = Integer.valueOf(loc != null ? loc.getCid() : -1);
                        EventLog.writeEvent(EventLogTags.DATA_NETWORK_REGISTRATION_FAIL, objArr);
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
                if (this.mPhone.getLteOnCdmaMode() == 1) {
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
                ar = msg.obj;
                CellInfoResult result = ar.userObj;
                synchronized (result.lockObj) {
                    if (ar.exception != null) {
                        log("EVENT_GET_CELL_INFO_LIST: error ret null, e=" + ar.exception);
                        result.list = null;
                    } else {
                        result.list = (List) ar.result;
                    }
                    this.mLastCellInfoListTime = SystemClock.elapsedRealtime();
                    this.mLastCellInfoList = result.list;
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
            case 48:
                if (DBG) {
                    log("EVENT_IMS_CAPABILITY_CHANGED");
                    break;
                }
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
                ar = (AsyncResult) msg.obj;
                if (ar.exception == null) {
                    boolean enable = ((Boolean) ar.result).booleanValue();
                    if (DBG) {
                        log("EVENT_RADIO_POWER_FROM_CARRIER: " + enable);
                    }
                    setRadioPowerFromCarrier(enable);
                    break;
                }
                break;
            case 52:
                if (DBG) {
                    log("EVENT_SIM_NOT_INSERTED");
                }
                cancelAllNotifications();
                this.mMdn = null;
                this.mMin = null;
                this.mIsMinInfoReady = false;
                break;
            case 53:
                if (DBG) {
                    log("EVENT_RADIO_POWER_OFF_DONE");
                }
                if (this.mDeviceShuttingDown && this.mCi.getRadioState().isAvailable()) {
                    this.mCi.requestShutdown(null);
                    break;
                }
            case 101:
            case 102:
                int index = msg.what - 101;
                if (index == 0) {
                    if (this.mOEMLastLevel_0 <= this.mOEMCurLevel_0) {
                        this.mOEMLastLevel_0 = this.mOEMCurLevel_0;
                        this.mIsPendingNotify_0 = false;
                        return;
                    }
                    if (this.mOEMLastLevel_0 > this.mOEMCurLevel_0) {
                        this.mOEMLastLevel_0--;
                    }
                    this.mSignalStrength.mOEMLevel_0 = this.mOEMLastLevel_0;
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
                    this.mSignalStrength.mOEMLevel_1 = this.mOEMLastLevel_1;
                }
                oppoNotifySignalStrength();
                nofifySmoothLevel(msg.what);
                if (DBG) {
                    log("leon EVENT_OEM_SMOOTH(last:(" + this.mOEMLastLevel_0 + "," + this.mOEMLastLevel_1 + ") current:(" + this.mOEMCurLevel_0 + "," + this.mOEMCurLevel_1 + ") ):" + msg.what);
                    break;
                }
                break;
            case 150:
                AsyncResult arscreen = msg.obj;
                if (arscreen != null) {
                    this.mIsScreenOn = ((Boolean) arscreen.result).booleanValue();
                } else {
                    log("leon EVENT_OEM_SCREEN_CHANGED error");
                }
                this.mTimeCount = 0;
                if (!this.mIsScreenOn) {
                    removeSmoothMessage();
                    if (OemConstant.getPowerCenterEnableFromProp(this.mPhone.getContext())) {
                        mNoServiceTime = 0;
                        mAlreadyUpdated = false;
                        mReselectCount = 0.0d;
                        mDataCallCount = 0;
                        mSMSSendCount = 0;
                        mNITZCount = 0;
                        this.mCi.oppoResetRilCount();
                        this.mScreenOffTime = System.currentTimeMillis() / NITZ_NTP_INTERVAL_OEM_SECOND;
                        if (this.mScreenOnTime == 0) {
                            this.mScreenOnTime = this.mScreenOffTime - (SystemClock.elapsedRealtime() / NITZ_NTP_INTERVAL_OEM_SECOND);
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
                } else if (OemConstant.getPowerCenterEnable(this.mPhone.getContext())) {
                    this.mScreenOnTime = System.currentTimeMillis() / NITZ_NTP_INTERVAL_OEM_SECOND;
                    if (this.mScreenOnTime - this.mScreenOffTime > this.SAMPLE_TIME) {
                        SubscriptionController s = SubscriptionController.getInstance();
                        SubscriptionManager.from(this.mPhone.getContext());
                        boolean isActivePhone = SubscriptionManager.getSubState(this.mPhone.getSubId()) == 1;
                        if (this.oppoSignalUpdate) {
                            if (DBG) {
                                log("screen on update signal strength when necessary");
                            }
                            this.oppoSignalUpdate = false;
                            updateOEMSmooth(this.mSS);
                        }
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
                }
                log("leon EVENT_OEM_SCREEN_CHANGED:" + this.mIsScreenOn);
                break;
            case OPPOEVENT_CHECK_NO_SERVICE /*666*/:
                this.bCheckNoServiceAgain = false;
                this.lastTime = 0;
                pollState();
                break;
            case EVENT_GET_SIGNAL_STRENGTH_ONCE /*3000*/:
                if (this.mCi.getRadioState().isOn()) {
                    onSignalStrengthResult((AsyncResult) msg.obj);
                    break;
                }
                return;
            case EVENT_MCFG_CONFIG_CHANGE /*3001*/:
                this.mPhone.sendSubscriptionSettings(true);
                log("yipeng.sun sendSubscriptionSettings, slotid:" + this.mPhone.getPhoneId());
                break;
            case EVENT_REGION_CHANGED /*4000*/:
                if (DBG) {
                    log("EVENT_REGION_CHANGED:");
                }
                this.mCi.OppoExpSetRegionForRilEcclist(obtainMessage(EVENT_REGION_CHANGED_DONE));
                break;
            case EVENT_REGION_CHANGED_DONE /*4001*/:
                ar = (AsyncResult) msg.obj;
                if (ar.exception != null) {
                    if (DBG) {
                        log("EVENT_REGION_CHANGED Failed:" + ar.exception);
                        break;
                    }
                } else if (DBG) {
                    log("EVENT_REGION_CHANGED Succeeded:");
                    break;
                }
                break;
            case RegionLockConstant.EVENT_NETWORK_LOCK_STATUS /*5000*/:
                log("EVENT_OPPO_CHANGED_NETWORK_LOCK_STATUS:");
                Intent intent = new Intent(RegionLockConstant.ACTION_NETWORK_LOCK);
                intent.putExtra(RegionLockConstant.NETLOCK_STATUS, "0");
                intent.putExtra(RegionLockConstant.UNLOCK_TYPE, "1");
                sendBroadCastChangedNetlockStatus(intent);
                break;
            default:
                log("Unhandled message with number: " + msg.what);
                break;
        }
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
        if (!TextUtils.isEmpty(operatorNumeric) && (TextUtils.isEmpty(getCdmaMin()) ^ 1) != 0) {
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
        if (ar.userObj == this.mPollingContext) {
            if (ar.exception != null) {
                Error err = null;
                if (ar.exception instanceof CommandException) {
                    err = ((CommandException) ar.exception).getCommandError();
                }
                if (err == Error.RADIO_NOT_AVAILABLE) {
                    cancelPollState();
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
                if (this.mPhone.isPhoneTypeGsm()) {
                    updateRoamingState();
                    this.mNewSS.setEmergencyOnly(this.mEmergencyOnly);
                } else {
                    boolean namMatch = false;
                    if (!isSidsAllZeros()) {
                        if (isHomeSid(this.mNewSS.getSystemId())) {
                            namMatch = true;
                        }
                    }
                    if (this.mIsSubscriptionFromRuim) {
                        boolean isRoamingBetweenOperators = isRoamingBetweenOperators(this.mNewSS.getVoiceRoaming(), this.mNewSS);
                        if (isRoamingBetweenOperators != this.mNewSS.getVoiceRoaming()) {
                            log("isRoamingBetweenOperators=" + isRoamingBetweenOperators + ". Override CDMA voice roaming to " + isRoamingBetweenOperators);
                            this.mNewSS.setVoiceRoaming(isRoamingBetweenOperators);
                        }
                    }
                    if (ServiceState.isCdma(this.mNewSS.getRilDataRadioTechnology())) {
                        if (this.mNewSS.getVoiceRegState() == 0) {
                            boolean isVoiceRoaming = this.mNewSS.getVoiceRoaming();
                            if (this.mNewSS.getDataRoaming() != isVoiceRoaming) {
                                log("Data roaming != Voice roaming. Override data roaming to " + isVoiceRoaming);
                                this.mNewSS.setDataRoaming(isVoiceRoaming);
                            }
                        } else {
                            boolean isRoamIndForHomeSystem = isRoamIndForHomeSystem(Integer.toString(this.mRoamingIndicator));
                            boolean dataRoamingState = this.mNewSS.getDataRoaming();
                            if (this.mNewSS.getDataRoaming() == isRoamIndForHomeSystem) {
                                log("isRoamIndForHomeSystem=" + isRoamIndForHomeSystem + ", override data roaming to " + (isRoamIndForHomeSystem ^ 1));
                                this.mNewSS.setDataRoaming(isRoamIndForHomeSystem ^ 1);
                            }
                            String[] homeRoamIndicators = Resources.getSystem().getStringArray(17235992);
                            if (!(dataRoamingState || (isRoamIndForHomeSystem ^ 1) == 0 || (homeRoamIndicators != null && (homeRoamIndicators == null || homeRoamIndicators.length != 0)))) {
                                log("isRoamIndForHomeSystem=" + isRoamIndForHomeSystem + ", override data roaming to false");
                                this.mNewSS.setDataRoaming(false);
                            }
                        }
                    }
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
                        if (!namMatch && (this.mIsInPrl ^ 1) != 0) {
                            this.mNewSS.setCdmaRoamingIndicator(this.mDefaultRoamingIndicator);
                        } else if (!namMatch || (this.mIsInPrl ^ 1) == 0) {
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
    }

    private boolean isRoamingBetweenOperators(boolean cdmaRoaming, ServiceState s) {
        return cdmaRoaming ? isSameOperatorNameFromSimAndSS(s) ^ 1 : false;
    }

    private int getRegStateFromHalRegState(int regState) {
        switch (regState) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            case 3:
                return 3;
            case 4:
                return 4;
            case 5:
                return 5;
            case 10:
                return 10;
            case 12:
                return 12;
            case 13:
                return 13;
            case 14:
                return 14;
            default:
                return 0;
        }
    }

    protected void handlePollStateResultMessage(int what, AsyncResult ar) {
        switch (what) {
            case 4:
                VoiceRegStateResult voiceRegStateResult = ar.result;
                int registrationState = getRegStateFromHalRegState(voiceRegStateResult.regState);
                int cssIndicator = voiceRegStateResult.cssSupported ? 1 : 0;
                this.mNewSS.setVoiceRegState(regCodeToServiceState(registrationState));
                this.mNewSS.setRilVoiceRadioTechnology(voiceRegStateResult.rat);
                int reasonForDenial = voiceRegStateResult.reasonForDenial;
                if (this.mPhone.isPhoneTypeGsm()) {
                    this.mGsmRoaming = regCodeIsRoaming(registrationState);
                    this.mNewRejectCode = reasonForDenial;
                    this.mNewSS.setCssIndicator(cssIndicator);
                    boolean isVoiceCapable = this.mPhone.getContext().getResources().getBoolean(17957059);
                    if ((registrationState == 13 || registrationState == 10 || registrationState == 12 || registrationState == 14) && isVoiceCapable) {
                        this.mEmergencyOnly = true;
                    } else {
                        this.mEmergencyOnly = false;
                    }
                } else {
                    boolean cdmaRoaming;
                    int roamingIndicator = voiceRegStateResult.roamingIndicator;
                    int systemIsInPrl = voiceRegStateResult.systemIsInPrl;
                    int defaultRoamingIndicator = voiceRegStateResult.defaultRoamingIndicator;
                    this.mRegistrationState = registrationState;
                    if (regCodeIsRoaming(registrationState)) {
                        cdmaRoaming = isRoamIndForHomeSystem(Integer.toString(roamingIndicator)) ^ 1;
                    } else {
                        cdmaRoaming = false;
                    }
                    this.mNewSS.setVoiceRoaming(cdmaRoaming);
                    this.mNewSS.setCssIndicator(cssIndicator);
                    this.mRoamingIndicator = roamingIndicator;
                    this.mIsInPrl = systemIsInPrl != 0;
                    this.mDefaultRoamingIndicator = defaultRoamingIndicator;
                    int systemId = 0;
                    int networkId = 0;
                    if (voiceRegStateResult.cellIdentity.cellInfoType == 2 && voiceRegStateResult.cellIdentity.cellIdentityCdma.size() == 1) {
                        CellIdentityCdma cellIdentityCdma = (CellIdentityCdma) voiceRegStateResult.cellIdentity.cellIdentityCdma.get(0);
                        systemId = cellIdentityCdma.systemId;
                        networkId = cellIdentityCdma.networkId;
                    }
                    this.mNewSS.setSystemAndNetworkId(systemId, networkId);
                    if (reasonForDenial == 0) {
                        this.mRegistrationDeniedReason = REGISTRATION_DENIED_GEN;
                    } else if (reasonForDenial == 1) {
                        this.mRegistrationDeniedReason = REGISTRATION_DENIED_AUTH;
                    } else {
                        this.mRegistrationDeniedReason = SpnOverride.MVNO_TYPE_NONE;
                    }
                    if (this.mRegistrationState == 3 && DBG) {
                        log("Registration denied, " + this.mRegistrationDeniedReason);
                    }
                }
                processCellLocationInfo(this.mNewCellLoc, voiceRegStateResult);
                if (DBG) {
                    log("handlPollVoiceRegResultMessage: regState=" + registrationState + " radioTechnology=" + voiceRegStateResult.rat);
                    return;
                }
                return;
            case 5:
                DataRegStateResult dataRegStateResult = ar.result;
                int regState = getRegStateFromHalRegState(dataRegStateResult.regState);
                int dataRegState = regCodeToServiceState(regState);
                int newDataRat = dataRegStateResult.rat;
                if ((SystemProperties.get("ro.oppo.operator", "ex").equals("TELSTRA")) && newDataRat == 19) {
                    newDataRat = 14;
                    log(" modify type from CA to LTE for telstra version");
                }
                this.mNewSS.setDataRegState(dataRegState);
                this.mNewSS.setRilDataRadioTechnology(newDataRat);
                boolean isDataRoaming;
                if (this.mPhone.isPhoneTypeGsm()) {
                    this.mNewReasonDataDenied = dataRegStateResult.reasonDataDenied;
                    this.mNewMaxDataCalls = dataRegStateResult.maxDataCalls;
                    this.mDataRoaming = regCodeIsRoaming(regState);
                    this.mNewSS.setDataRoamingFromRegistration(this.mDataRoaming);
                    if (DBG) {
                        log("handlPollStateResultMessage: GsmSST setDataRegState=" + dataRegState + " regState=" + regState + " dataRadioTechnology=" + newDataRat);
                    }
                } else if (this.mPhone.isPhoneTypeCdma()) {
                    isDataRoaming = regCodeIsRoaming(regState);
                    this.mNewSS.setDataRoaming(isDataRoaming);
                    this.mNewSS.setDataRoamingFromRegistration(isDataRoaming);
                    if (DBG) {
                        log("handlPollStateResultMessage: cdma setDataRegState=" + dataRegState + " regState=" + regState + " dataRadioTechnology=" + newDataRat);
                    }
                } else {
                    int oldDataRAT = this.mSS.getRilDataRadioTechnology();
                    if ((oldDataRAT == 0 && newDataRat != 0) || ((ServiceState.isCdma(oldDataRAT) && ServiceState.isLte(newDataRat)) || (ServiceState.isLte(oldDataRAT) && ServiceState.isCdma(newDataRat)))) {
                        this.mCi.getSignalStrength(obtainMessage(3));
                    }
                    isDataRoaming = regCodeIsRoaming(regState);
                    this.mNewSS.setDataRoaming(isDataRoaming);
                    this.mNewSS.setDataRoamingFromRegistration(isDataRoaming);
                    if (DBG) {
                        log("handlPollStateResultMessage: CdmaLteSST setDataRegState=" + dataRegState + " regState=" + regState + " dataRadioTechnology=" + newDataRat);
                    }
                }
                updateServiceStateLteEarfcnBoost(this.mNewSS, getLteEarfcn(dataRegStateResult));
                return;
            case 6:
                String[] opNames;
                String brandOverride;
                if (this.mPhone.isPhoneTypeGsm()) {
                    opNames = (String[]) ar.result;
                    if (opNames != null && opNames.length >= 3) {
                        brandOverride = this.mUiccController.getUiccCard(getPhoneId()) != null ? this.mUiccController.getUiccCard(getPhoneId()).getOperatorBrandOverride() : null;
                        if (brandOverride != null) {
                            log("EVENT_POLL_STATE_OPERATOR: use brandOverride=" + brandOverride);
                            this.mNewSS.setOperatorName(brandOverride, brandOverride, opNames[2]);
                            return;
                        }
                        this.mNewSS.setOperatorName(opNames[0], opNames[1], opNames[2]);
                        return;
                    }
                    return;
                }
                opNames = (String[]) ar.result;
                if (opNames != null && opNames.length >= 3) {
                    if (opNames[2] == null || opNames[2].length() < 5 || "00000".equals(opNames[2])) {
                        opNames[2] = SystemProperties.get(GsmCdmaPhone.PROPERTY_CDMA_HOME_OPERATOR_NUMERIC, "00000");
                        if (DBG) {
                            log("RIL_REQUEST_OPERATOR.response[2], the numeric,  is bad. Using SystemProperties 'ro.cdma.home.operator.numeric'= " + opNames[2]);
                        }
                    }
                    if (this.mIsSubscriptionFromRuim) {
                        brandOverride = this.mUiccController.getUiccCard(getPhoneId()) != null ? this.mUiccController.getUiccCard(getPhoneId()).getOperatorBrandOverride() : null;
                        if (brandOverride != null) {
                            this.mNewSS.setOperatorName(brandOverride, brandOverride, opNames[2]);
                            return;
                        } else {
                            this.mNewSS.setOperatorName(opNames[0], opNames[1], opNames[2]);
                            return;
                        }
                    }
                    this.mNewSS.setOperatorName(opNames[0], opNames[1], opNames[2]);
                    return;
                } else if (DBG) {
                    log("EVENT_POLL_STATE_OPERATOR_CDMA: error parsing opNames");
                    return;
                } else {
                    return;
                }
            case 14:
                int[] ints = ar.result;
                this.mNewSS.setIsManualSelection(ints[0] == 1);
                if (ints[0] == 1 && this.mPhone.shouldForceAutoNetworkSelect()) {
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
        String[] homeRoamIndicators = Resources.getSystem().getStringArray(17235992);
        log("isRoamIndForHomeSystem: homeRoamIndicators=" + Arrays.toString(homeRoamIndicators));
        if (homeRoamIndicators != null) {
            for (String homeRoamInd : homeRoamIndicators) {
                if (homeRoamInd.equals(roamInd)) {
                    return true;
                }
            }
            log("isRoamIndForHomeSystem: No match found against list for roamInd=" + roamInd);
            return false;
        }
        log("isRoamIndForHomeSystem: No list found");
        return false;
    }

    protected void updateRoamingState() {
        CarrierConfigManager configLoader;
        PersistableBundle b;
        if (this.mPhone.isPhoneTypeGsm()) {
            boolean roaming = !this.mGsmRoaming ? this.mDataRoaming : true;
            if (this.mGsmRoaming && (isOperatorConsideredRoaming(this.mNewSS) ^ 1) != 0 && (isSameNamedOperators(this.mNewSS) || isOperatorConsideredNonRoaming(this.mNewSS))) {
                log("updateRoamingState: resource override set non roaming.isSameNamedOperators=" + isSameNamedOperators(this.mNewSS) + ",isOperatorConsideredNonRoaming=" + isOperatorConsideredNonRoaming(this.mNewSS));
                roaming = false;
            }
            if (SubscriptionManager.isVsimEnabled(this.mPhone.getSubId())) {
                log("Vsim is Enabled, set roaming = false.");
                roaming = false;
            }
            configLoader = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
            if (configLoader != null) {
                try {
                    b = configLoader.getConfigForSubId(this.mPhone.getSubId());
                    if (alwaysOnHomeNetwork(b)) {
                        log("updateRoamingState: carrier config override always on home network");
                        roaming = false;
                    } else if (isNonRoamingInGsmNetwork(b, this.mNewSS.getOperatorNumeric())) {
                        log("updateRoamingState: carrier config override set non roaming:" + this.mNewSS.getOperatorNumeric());
                        roaming = false;
                    } else if (isRoamingInGsmNetwork(b, this.mNewSS.getOperatorNumeric())) {
                        log("updateRoamingState: carrier config override set roaming:" + this.mNewSS.getOperatorNumeric());
                        roaming = true;
                    }
                } catch (Exception e) {
                    loge("updateRoamingState: unable to access carrier config service");
                }
            } else {
                log("updateRoamingState: no carrier config service available");
            }
            if (roaming && OppoGsmServiceStateTracker.isVodafoneNationalRoaming(this.mNewSS.getOperatorNumeric(), getSIMOperatorNumeric())) {
                roaming = false;
            }
            this.mNewSS.setVoiceRoaming(roaming);
            this.mNewSS.setDataRoaming(roaming);
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
            } catch (Exception e2) {
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

    private String getSIMOperatorNumeric() {
        return ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getSimOperatorNumericForPhone(this.mPhone.getPhoneId());
    }

    /* JADX WARNING: Missing block: B:203:0x06d1, code:
            if ((android.text.TextUtils.equals(r4, r47.mCurPlmn) ^ 1) == 0) goto L_0x032a;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void updateSpnDisplay() {
        updateOperatorNameFromEri();
        CharSequence wfcVoiceSpnFormat = null;
        CharSequence wfcDataSpnFormat = null;
        int combinedRegState = getCombinedRegState();
        if (this.mPhone.getImsPhone() != null && this.mPhone.getImsPhone().isWifiCallingEnabled() && combinedRegState == 0) {
            String[] wfcSpnFormats = this.mPhone.getContext().getResources().getStringArray(17236078);
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
            wfcVoiceSpnFormat = wfcSpnFormats[voiceIdx];
            wfcDataSpnFormat = wfcSpnFormats[dataIdx];
        }
        IccRecords iccRecords;
        String plmn;
        boolean showPlmn;
        String spn;
        boolean showSpn;
        int subId;
        int[] subIds;
        Intent intent;
        if (this.mPhone.isPhoneTypeGsm()) {
            iccRecords = this.mIccRecords;
            plmn = null;
            IccRecords r = this.mPhone != null ? (IccRecords) this.mPhone.mIccRecords.get() : null;
            String strNumPlmn = this.mSS != null ? this.mSS.getOperatorNumeric() : null;
            String simOperatorNumeric = r != null ? r.getOperatorNumeric() : SpnOverride.MVNO_TYPE_NONE;
            int rule = OppoGsmServiceStateTracker.oppoGetDisplayRule(iccRecords, this.mSS);
            if (combinedRegState == 1 || combinedRegState == 2) {
                showPlmn = true;
                if (this.mEmergencyOnly) {
                    plmn = Resources.getSystem().getText(17039832).toString();
                } else {
                    plmn = Resources.getSystem().getText(17040164).toString();
                }
                plmn = Resources.getSystem().getText(17040409).toString();
                if (DBG) {
                    log("updateSpnDisplay: radio is on but out of service, set plmn='" + plmn + "'");
                }
            } else if (combinedRegState == 0) {
                String[] pnnEons = this.mSS != null ? getPnnFromEons(this.mSS.getOperatorNumeric(), true) : null;
                if (pnnEons != null && (TextUtils.isEmpty(pnnEons[0]) ^ 1) != 0 && (OppoGsmServiceStateTracker.isGT4GSimCardCheck(simOperatorNumeric) || OppoGsmServiceStateTracker.isVodafoneNationalRoaming(strNumPlmn, simOperatorNumeric) || "50501".equals(strNumPlmn) || "50502".equals(strNumPlmn))) {
                    log("TW GT or NationalRoaming case, don't process language name pnnEons[0] =" + pnnEons[0]);
                    plmn = pnnEons[0];
                } else if (!(this.mPhone == null || this.mSS == null)) {
                    plmn = OppoGsmServiceStateTracker.oppoGetPlmnOverride(this.mPhone.getContext(), this.mSS.getOperatorNumeric(), this.mSS);
                }
                showPlmn = !TextUtils.isEmpty(plmn) ? (rule & 2) == 2 : false;
            } else {
                showPlmn = true;
                plmn = Resources.getSystem().getText(17040409).toString();
                if (DBG) {
                    log("updateSpnDisplay: radio is off w/ showPlmn=" + true + " plmn=" + plmn);
                }
            }
            String simNumeric = getSystemProperty("gsm.sim.operator.numeric", SpnOverride.MVNO_TYPE_NONE);
            String pnnHomeName = getSystemProperty(OppoTelephonyConstant.PROPERTY_ICC_OPERATOR_PNN_NAME, SpnOverride.MVNO_TYPE_NONE);
            if (DBG) {
                log("updateSpnDisplay pnnHomeName = " + pnnHomeName);
            }
            plmn = OppoGsmServiceStateTracker.oppoGetExPlmn(this.mSS, plmn, simNumeric, pnnHomeName);
            spn = iccRecords != null ? iccRecords.getServiceProviderName() : SpnOverride.MVNO_TYPE_NONE;
            if (this.mSubscriptionController.isHasSoftSimCard() && this.mSubscriptionController.getSoftSimCardSlotId() == this.mPhone.getPhoneId()) {
                spn = getReadTeaServiceProviderName(this.mPhone.getContext(), spn);
            }
            spn = OppoGsmServiceStateTracker.oppoGetExPlmn(this.mSS, spn, simNumeric, pnnHomeName);
            String dataSpn = spn;
            showSpn = !TextUtils.isEmpty(spn) ? (rule & 1) == 1 : false;
            if (!TextUtils.isEmpty(spn) && (TextUtils.isEmpty(wfcVoiceSpnFormat) ^ 1) != 0 && (TextUtils.isEmpty(wfcDataSpnFormat) ^ 1) != 0) {
                String originalSpn = spn.trim();
                spn = String.format(wfcVoiceSpnFormat, new Object[]{originalSpn});
                dataSpn = String.format(wfcDataSpnFormat, new Object[]{originalSpn});
                showSpn = true;
                showPlmn = false;
            } else if (!TextUtils.isEmpty(plmn) && (TextUtils.isEmpty(wfcVoiceSpnFormat) ^ 1) != 0) {
                plmn = String.format(wfcVoiceSpnFormat, new Object[]{plmn.trim()});
            } else if (this.mSS.getVoiceRegState() == 3 || (showPlmn && TextUtils.equals(spn, plmn))) {
                spn = null;
                showSpn = false;
            }
            subId = -1;
            subIds = SubscriptionManager.getSubId(this.mPhone.getPhoneId());
            if (subIds != null && subIds.length > 0) {
                subId = subIds[0];
            }
            if (this.mSubId == subId && showPlmn == this.mCurShowPlmn && showSpn == this.mCurShowSpn && (TextUtils.equals(spn, this.mCurSpn) ^ 1) == 0) {
                if ((TextUtils.equals(dataSpn, this.mCurDataSpn) ^ 1) == 0) {
                }
            }
            if (showPlmn && showSpn && (TextUtils.isEmpty(spn) ^ 1) != 0 && combinedRegState == 0 && (this.mSS.getRoaming() ^ 1) != 0) {
                log("display spn first for non-roaming case, spn = " + spn);
                showPlmn = false;
                showSpn = true;
            }
            boolean[] showTemp = OppoGsmServiceStateTracker.oppoShowSpnOrPlmn(this.mSS, plmn, spn, simNumeric);
            if (showTemp[0] || showTemp[1]) {
                showPlmn = showTemp[0];
                showSpn = showTemp[1];
            }
            if (DBG) {
                log(String.format("updateSpnDisplay: changed sending intent rule=" + rule + " showPlmn='%b' plmn='%s' showSpn='%b' spn='%s' dataSpn='%s' " + "subId='%d'", new Object[]{Boolean.valueOf(showPlmn), plmn, Boolean.valueOf(showSpn), spn, dataSpn, Integer.valueOf(subId)}));
            }
            if (TextUtils.isEmpty(plmn) && TextUtils.isEmpty(spn) && simNumeric != null && OppoGsmServiceStateTracker.isOperatorCheck(simNumeric)) {
                SIMRecords vSimRecords = (SIMRecords) iccRecords;
                if (!(vSimRecords == null || vSimRecords.getEFpnnNetworkNames(0) == null)) {
                    plmn = vSimRecords.getEFpnnNetworkNames(0).sFullName;
                    showPlmn = true;
                    showSpn = false;
                    if (DBG) {
                        log("updateSpnDisplay: vodafone display EFpnn name =" + plmn);
                    }
                }
            }
            String iwlanName = OppoGsmServiceStateTracker.getNameForIwlanOnly(iccRecords, this.mSS, simNumeric);
            if (!TextUtils.isEmpty(iwlanName)) {
                log("updateSpnDisplay: iwlanName =" + iwlanName);
                plmn = iwlanName;
                oppoSetOperatorAlpha(iwlanName);
                showPlmn = true;
                showSpn = false;
            }
            if (showPlmn && (plmn == null || SpnOverride.MVNO_TYPE_NONE.equals(plmn))) {
                log(String.format("[Oppo] Ignore null plmn", new Object[0]));
                this.mSubId = subId;
                this.mCurShowSpn = showSpn;
                this.mCurShowPlmn = showPlmn;
                this.mCurSpn = spn;
                this.mCurDataSpn = dataSpn;
                this.mCurPlmn = plmn;
            } else if (showPlmn || !(spn == null || SpnOverride.MVNO_TYPE_NONE.equals(spn))) {
                oppoVirtualSimCheck(this.mSS.getOperatorNumeric(), plmn, spn, showPlmn, showSpn);
                showPlmn = this.mShowPlmn;
                showSpn = this.mShowSPn;
                if (this.mNewPlmn != null) {
                    plmn = this.mNewPlmn;
                }
                if (OppoGsmServiceStateTracker.isOperatorCheck(getSIMOperatorNumeric())) {
                    SIMRecords mSIMRecords = (SIMRecords) iccRecords;
                    if (mSIMRecords != null) {
                        String tempSpn = mSIMRecords.getServiceProviderName();
                        if (OppoGsmServiceStateTracker.isVodafoneHomePlmn(this.mSS.getOperatorNumeric(), getSIMOperatorNumeric())) {
                            if (!TextUtils.isEmpty(tempSpn)) {
                                spn = tempSpn;
                                oppoSetOperatorAlpha(tempSpn);
                                showPlmn = false;
                                showSpn = true;
                                if (DBG) {
                                    log("updateSpnDisplay:vodafone_display spn=" + tempSpn);
                                }
                            } else if (mSIMRecords.getEFpnnNetworkNames(0) != null) {
                                plmn = mSIMRecords.getEFpnnNetworkNames(0).sFullName;
                                oppoSetOperatorAlpha(plmn);
                                showPlmn = true;
                                showSpn = false;
                                if (DBG) {
                                    log("updateSpnDisplay:vodafone_display plmn=" + plmn);
                                }
                            }
                        } else if (OppoGsmServiceStateTracker.isVodafoneNationalRoaming(this.mSS.getOperatorNumeric(), getSIMOperatorNumeric())) {
                            if (mSIMRecords.getEFpnnNetworkNames(1) != null) {
                                plmn = mSIMRecords.getEFpnnNetworkNames(1).sFullName;
                                oppoSetOperatorAlpha(plmn);
                                showPlmn = true;
                                showSpn = false;
                                if (DBG) {
                                    log("updateSpnDisplay:vodafone_display roaming plmn=" + plmn);
                                }
                            }
                        } else if (this.mSS.getVoiceRegState() == 3 && 18 == this.mSS.getRilDataRadioTechnology() && mSIMRecords.getEFpnnNetworkNames(0) != null) {
                            plmn = mSIMRecords.getEFpnnNetworkNames(0).sFullName;
                            oppoSetOperatorAlpha(plmn);
                            showPlmn = true;
                            showSpn = false;
                            if (DBG) {
                                log("updateSpnDisplay:vodafone_display iwlan plmn=" + plmn);
                            }
                        }
                        if (!mSIMRecords.isSimLoadedCompleted()) {
                            showPlmn = false;
                            showSpn = false;
                            log(String.format("[Oppo] sim is not load ok,ignore plmn", new Object[0]));
                        }
                    }
                    if (DBG) {
                        log(String.format("updateSpnDisplay:vodafone_displayrule=" + rule + "showPlmn='%b' plmn='%s' showSpn='%b' spn='%s' dataSpn='%s' subId='%d'", new Object[]{Boolean.valueOf(showPlmn), plmn, Boolean.valueOf(showSpn), spn, dataSpn, Integer.valueOf(subId)}));
                    }
                }
                if (OppoGsmServiceStateTracker.isNZOperatorCheck(getSIMOperatorNumeric())) {
                    if (!(this.mSS.getOperatorNumeric() == null || !this.mSS.getOperatorNumeric().startsWith("530") || TextUtils.isEmpty(spn))) {
                        showPlmn = false;
                        showSpn = true;
                        oppoSetOperatorAlpha(spn);
                    }
                    if (DBG) {
                        log(String.format("updateSpnDisplay:2degrees_displayrule=" + rule + "showPlmn='%b' plmn='%s' showSpn='%b' spn='%s' dataSpn='%s' subId='%d'", new Object[]{Boolean.valueOf(showPlmn), plmn, Boolean.valueOf(showSpn), spn, dataSpn, Integer.valueOf(subId)}));
                    }
                }
                intent = new Intent("android.provider.Telephony.SPN_STRINGS_UPDATED");
                intent.putExtra("showSpn", showSpn);
                intent.putExtra(SpnOverride.MVNO_TYPE_SPN, spn);
                intent.putExtra("spnData", dataSpn);
                intent.putExtra("showPlmn", showPlmn);
                intent.putExtra("plmn", plmn);
                SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
                intent.putExtra("serviceState", combinedRegState);
                this.mPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
                if (!this.mSubscriptionController.setPlmnSpn(this.mPhone.getPhoneId(), showPlmn, plmn, showSpn, spn)) {
                    this.mSpnUpdatePending = true;
                }
                this.mSubId = subId;
                this.mCurShowSpn = showSpn;
                this.mCurShowPlmn = showPlmn;
                this.mCurSpn = spn;
                this.mCurDataSpn = dataSpn;
                this.mCurPlmn = plmn;
            } else {
                log(String.format("[Oppo] can't show spn", new Object[0]));
                showSpn = false;
                if (!TextUtils.isEmpty(plmn)) {
                    oppoSetOperatorAlpha(plmn);
                    showPlmn = true;
                }
                this.mSubId = subId;
                this.mCurShowSpn = showSpn;
                this.mCurShowPlmn = showPlmn;
                this.mCurSpn = spn;
                this.mCurDataSpn = dataSpn;
                this.mCurPlmn = plmn;
            }
        } else {
            plmn = OppoGsmServiceStateTracker.oppoGetPlmnOverride(this.mPhone.getContext(), this.mSS.getOperatorNumeric(), this.mSS);
            showSpn = false;
            spn = SpnOverride.MVNO_TYPE_NONE;
            showPlmn = plmn != null;
            subId = -1;
            subIds = SubscriptionManager.getSubId(this.mPhone.getPhoneId());
            if (subIds != null && subIds.length > 0) {
                subId = subIds[0];
            }
            if (!TextUtils.isEmpty(plmn) && (TextUtils.isEmpty(wfcVoiceSpnFormat) ^ 1) != 0) {
                plmn = String.format(wfcVoiceSpnFormat, new Object[]{plmn.trim()});
            } else if (this.mCi.getRadioState() == RadioState.RADIO_OFF) {
                log("updateSpnDisplay: overwriting plmn from " + plmn + " to null as radio " + "state is off");
                plmn = null;
            }
            if (combinedRegState == 1) {
                plmn = Resources.getSystem().getText(17040409).toString();
                if (DBG) {
                    log("updateSpnDisplay: radio is on but out of svc, set plmn='" + plmn + "'");
                }
            }
            if (!(this.mSubId == subId && (TextUtils.equals(plmn, this.mCurPlmn) ^ 1) == 0)) {
                if (DBG) {
                    log(String.format("updateSpnDisplay: changed sending intent showPlmn='%b' plmn='%s' subId='%d'", new Object[]{Boolean.valueOf(showPlmn), plmn, Integer.valueOf(subId)}));
                }
                if (TextUtils.equals(plmn, SpnOverride.MVNO_TYPE_NONE)) {
                    plmn = this.mCurPlmn;
                }
                oppoVirtualSimCheck(SpnOverride.MVNO_TYPE_NONE, plmn, SpnOverride.MVNO_TYPE_NONE, showPlmn, false);
                showPlmn = this.mShowPlmn;
                intent = new Intent("android.provider.Telephony.SPN_STRINGS_UPDATED");
                if (this.mSubscriptionController.isHasSoftSimCard() && this.mSubscriptionController.getSoftSimCardSlotId() == this.mPhone.getPhoneId()) {
                    iccRecords = this.mIccRecords;
                    spn = getReadTeaServiceProviderName(this.mPhone.getContext(), iccRecords != null ? iccRecords.getServiceProviderName() : SpnOverride.MVNO_TYPE_NONE);
                    if (!TextUtils.isEmpty(spn)) {
                        showSpn = true;
                        showPlmn = false;
                        oppoSetOperatorAlpha(spn);
                    }
                }
                intent.putExtra("showSpn", showSpn);
                intent.putExtra(SpnOverride.MVNO_TYPE_SPN, spn);
                intent.putExtra("showPlmn", showPlmn);
                intent.putExtra("plmn", plmn);
                SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
                intent.putExtra("serviceState", combinedRegState);
                this.mPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
                if (!this.mSubscriptionController.setPlmnSpn(this.mPhone.getPhoneId(), showPlmn, plmn, showSpn, spn)) {
                    this.mSpnUpdatePending = true;
                }
            }
            this.mSubId = subId;
            this.mCurShowSpn = showSpn;
            this.mCurShowPlmn = showPlmn;
            this.mCurSpn = spn;
            this.mCurPlmn = plmn;
        }
        if (OemConstant.getPowerCenterEnable(this.mPhone.getContext())) {
            SubscriptionController s = SubscriptionController.getInstance();
            boolean isInService = this.mSS.getState() == 0;
            SubscriptionManager.from(this.mPhone.getContext());
            boolean isActivePhone = SubscriptionManager.getSubState(this.mPhone.getSubId()) == 1;
            long now = System.currentTimeMillis() / NITZ_NTP_INTERVAL_OEM_SECOND;
            if (DBG) {
                log("[POWERSTATE]mBeginNoServiceTime:" + mBeginNoServiceTime[this.mPhone.getPhoneId()] + ",mNoServiceTime:" + mNoServiceTime + ",isInService:" + isInService + ",isActivePhone:" + isActivePhone);
            }
            if (this.mPhone.getPhoneId() < 2 && isActivePhone && isInService && mBeginNoServiceTime[this.mPhone.getPhoneId()] != 0) {
                if (!(this.mScreenOffTime == 0 || (this.mIsScreenOn ^ 1) == 0)) {
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
    }

    protected void setPowerStateToDesired() {
        if (DBG) {
            String tmpLog = "mDeviceShuttingDown=" + this.mDeviceShuttingDown + ", mDesiredPowerState=" + this.mDesiredPowerState + ", getRadioState=" + this.mCi.getRadioState() + ", mPowerOffDelayNeed=" + this.mPowerOffDelayNeed + ", mAlarmSwitch=" + this.mAlarmSwitch + ", mRadioDisabledByCarrier=" + this.mRadioDisabledByCarrier;
            log(tmpLog);
            this.mRadioPowerLog.log(tmpLog);
        }
        if (this.mPhone.isPhoneTypeGsm() && this.mAlarmSwitch) {
            if (DBG) {
                log("mAlarmSwitch == true");
            }
            ((AlarmManager) this.mPhone.getContext().getSystemService("alarm")).cancel(this.mRadioOffIntent);
            this.mAlarmSwitch = false;
        }
        if (this.mDesiredPowerState && (this.mRadioDisabledByCarrier ^ 1) != 0 && this.mCi.getRadioState() == RadioState.RADIO_OFF) {
            this.mCi.setRadioPower(true, null);
        } else if ((!this.mDesiredPowerState || this.mRadioDisabledByCarrier) && this.mCi.getRadioState().isOn()) {
            if (!this.mPhone.isPhoneTypeGsm() || !this.mPowerOffDelayNeed) {
                powerOffRadioSafely(this.mPhone.mDcTracker);
            } else if (!this.mImsRegistrationOnOff || (this.mAlarmSwitch ^ 1) == 0) {
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
            UiccCardApplication newUiccApplication = getUiccCardApplication();
            if (this.mUiccApplcation != newUiccApplication) {
                if (this.mUiccApplcation != null) {
                    log("Removing stale icc objects.");
                    this.mUiccApplcation.unregisterForReady(this);
                    if (this.mIccRecords != null) {
                        this.mIccRecords.unregisterForRecordsLoaded(this);
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
                        }
                    } else if (this.mIsSubscriptionFromRuim) {
                        this.mUiccApplcation.registerForReady(this, 26, null);
                        if (this.mIccRecords != null) {
                            this.mIccRecords.registerForRecordsLoaded(this, 27, null);
                        }
                    }
                    this.isRemoveCard = false;
                }
                this.isCardLocked = false;
            } else if (newUiccApplication != null) {
                AppState tAppState = newUiccApplication.getState();
                PinState tPinState = newUiccApplication.getPin1State();
                if (AppState.APPSTATE_PIN != tAppState && AppState.APPSTATE_PUK != tAppState) {
                    this.isCardLocked = false;
                } else if (!(PinState.PINSTATE_ENABLED_VERIFIED == tPinState || PinState.PINSTATE_DISABLED == tPinState)) {
                    log("onUpdateIccAvailability PIN or PUK, need show no service now!");
                    this.lastTime = 0;
                    this.isCardLocked = true;
                    this.mNewSS.setStateOutOfService();
                    pollState();
                }
            }
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
        Rlog.d(LOG_TAG, "phoneId " + this.mPhone.getPhoneId() + " " + s);
    }

    protected void loge(String s) {
        Rlog.e(LOG_TAG, "phoneId " + this.mPhone.getPhoneId() + " " + s);
    }

    public int getCurrentDataConnectionState() {
        return this.mSS.getDataRegState();
    }

    public boolean isConcurrentVoiceAndDataAllowed() {
        boolean z = true;
        if (this.mPhone.isPhoneTypeGsm()) {
            if (this.mSS.getRilDataRadioTechnology() >= 3) {
                return true;
            }
            if (this.mSS.getCssIndicator() != 1) {
                z = false;
            }
            return z;
        } else if (this.mPhone.isPhoneTypeCdma()) {
            return false;
        } else {
            if (this.mSS.getCssIndicator() != 1) {
                z = false;
            }
            return z;
        }
    }

    public void setImsRegistrationState(boolean registered) {
        log("ImsRegistrationState - registered : " + registered);
        if (this.mImsRegistrationOnOff && (registered ^ 1) != 0 && this.mAlarmSwitch) {
            this.mImsRegistrationOnOff = registered;
            ((AlarmManager) this.mPhone.getContext().getSystemService("alarm")).cancel(this.mRadioOffIntent);
            this.mAlarmSwitch = false;
            sendMessage(obtainMessage(45));
            return;
        }
        this.mImsRegistrationOnOff = registered;
    }

    public void onImsCapabilityChanged() {
        sendMessage(obtainMessage(48));
    }

    public boolean isRadioOn() {
        return this.mCi.getRadioState() == RadioState.RADIO_ON;
    }

    public void pollState() {
        pollState(false);
    }

    private void modemTriggeredPollState() {
        pollState(true);
    }

    public void pollState(boolean modemTriggered) {
        this.mPollingContext = new int[1];
        this.mPollingContext[0] = 0;
        log("pollState: modemTriggered=" + modemTriggered);
        switch (m9x804b995f()[this.mCi.getRadioState().ordinal()]) {
            case 1:
                this.mNewSS.setStateOff();
                this.mNewCellLoc.setStateInvalid();
                setSignalStrengthDefaultValues();
                this.mGotCountryCode = false;
                this.mNitzUpdatedTime = false;
                if (this.mDeviceShuttingDown || !(modemTriggered || 18 == this.mSS.getRilDataRadioTechnology() || (this.mIsModemTriggeredPollingPending ^ 1) == 0)) {
                    pollStateDone();
                    return;
                }
            case 2:
                this.mNewSS.setStateOutOfService();
                this.mNewCellLoc.setStateInvalid();
                setSignalStrengthDefaultValues();
                this.mGotCountryCode = false;
                this.mNitzUpdatedTime = false;
                pollStateDone();
                return;
        }
        if (modemTriggered) {
            this.mIsModemTriggeredPollingPending = true;
        }
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
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:350:0x0e44 A:{Splitter: B:322:0x0cfa, ExcHandler: java.lang.NumberFormatException (r9_0 'ex' java.lang.RuntimeException), PHI: r32 } */
    /* JADX WARNING: Missing block: B:350:0x0e44, code:
            r9 = move-exception;
     */
    /* JADX WARNING: Missing block: B:351:0x0e45, code:
            loge("pollStateDone: countryCodeForMcc error: " + r9);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void pollStateDone() {
        boolean hasRilDataRadioTechnologyChanged;
        if (OemConstant.SWITCH__SMOOTH) {
            int checkResult = oppoOosDelayState();
            if (DBG) {
                log("oppoDelayOosState checkResult = " + checkResult);
            }
            if (checkResult != 1) {
                if (checkResult == 2) {
                    oppoSetAutoNetworkSelect();
                }
            } else {
                return;
            }
        }
        this.mIsModemTriggeredPollingPending = false;
        if (!this.mPhone.isPhoneTypeGsm()) {
            updateRoamingState();
        }
        if (Build.IS_DEBUGGABLE && SystemProperties.getBoolean(PROP_FORCE_ROAMING, false)) {
            this.mNewSS.setVoiceRoaming(true);
            this.mNewSS.setDataRoaming(true);
        }
        useDataRegStateForDataOnlyDevices();
        resetServiceStateInIwlanMode();
        if (DBG) {
            log("Poll ServiceState done:  oldSS=[" + this.mSS + "] newSS=[" + this.mNewSS + "]" + " oldMaxDataCalls=" + this.mMaxDataCalls + " mNewMaxDataCalls=" + this.mNewMaxDataCalls + " oldReasonDataDenied=" + this.mReasonDataDenied + " mNewReasonDataDenied=" + this.mNewReasonDataDenied);
        }
        boolean hasRegistered = this.mSS.getVoiceRegState() != 0 ? this.mNewSS.getVoiceRegState() == 0 : false;
        boolean hasDeregistered = this.mSS.getVoiceRegState() == 0 ? this.mNewSS.getVoiceRegState() != 0 : false;
        boolean hasDataAttached = this.mSS.getDataRegState() != 0 ? this.mNewSS.getDataRegState() == 0 : false;
        boolean hasDataDetached = this.mSS.getDataRegState() == 0 ? this.mNewSS.getDataRegState() != 0 : false;
        boolean hasDataRegStateChanged = this.mSS.getDataRegState() != this.mNewSS.getDataRegState();
        boolean hasVoiceRegStateChanged = this.mSS.getVoiceRegState() != this.mNewSS.getVoiceRegState();
        boolean hasLocationChanged = this.mNewCellLoc.equals(this.mCellLoc) ^ 1;
        boolean hasRilVoiceRadioTechnologyChanged = this.mSS.getRilVoiceRadioTechnology() != this.mNewSS.getRilVoiceRadioTechnology();
        if (this.mSS.getRilDataRadioTechnology() == this.mNewSS.getRilDataRadioTechnology()) {
            hasRilDataRadioTechnologyChanged = this.mSS.isUsingCarrierAggregation() != this.mNewSS.isUsingCarrierAggregation();
        } else {
            hasRilDataRadioTechnologyChanged = true;
        }
        boolean hasChanged = this.mNewSS.equals(this.mSS) ^ 1;
        boolean hasVoiceRoamingOn = !this.mSS.getVoiceRoaming() ? this.mNewSS.getVoiceRoaming() : false;
        boolean hasVoiceRoamingOff = this.mSS.getVoiceRoaming() ? this.mNewSS.getVoiceRoaming() ^ 1 : false;
        boolean hasDataRoamingOn = !this.mSS.getDataRoaming() ? this.mNewSS.getDataRoaming() : false;
        boolean hasDataRoamingOff = this.mSS.getDataRoaming() ? this.mNewSS.getDataRoaming() ^ 1 : false;
        boolean hasRejectCauseChanged = this.mRejectCode != this.mNewRejectCode;
        boolean hasCssIndicatorChanged = this.mSS.getCssIndicator() != this.mNewSS.getCssIndicator();
        boolean has4gHandoff = false;
        boolean hasMultiApnSupport = false;
        boolean hasLostMultiApnSupport = false;
        boolean hasLteDataDetach = false;
        if (this.mPhone.isPhoneTypeCdmaLte()) {
            if (this.mNewSS.getDataRegState() != 0) {
                has4gHandoff = false;
            } else if (ServiceState.isLte(this.mSS.getRilDataRadioTechnology()) && this.mNewSS.getRilDataRadioTechnology() == 13) {
                has4gHandoff = true;
            } else if (this.mSS.getRilDataRadioTechnology() == 13) {
                has4gHandoff = ServiceState.isLte(this.mNewSS.getRilDataRadioTechnology());
            } else {
                has4gHandoff = false;
            }
            hasMultiApnSupport = (ServiceState.isLte(this.mNewSS.getRilDataRadioTechnology()) || this.mNewSS.getRilDataRadioTechnology() == 13) ? !ServiceState.isLte(this.mSS.getRilDataRadioTechnology()) ? this.mSS.getRilDataRadioTechnology() != 13 : false : false;
            hasLostMultiApnSupport = this.mNewSS.getRilDataRadioTechnology() >= 4 ? this.mNewSS.getRilDataRadioTechnology() <= 8 : false;
            if (ServiceState.isLte(this.mSS.getRilDataRadioTechnology())) {
                hasLteDataDetach = ServiceState.isLte(this.mNewSS.getRilDataRadioTechnology()) ^ 1;
            } else {
                hasLteDataDetach = false;
            }
        }
        if (DBG) {
            log("pollStateDone: hasRegistered=" + hasRegistered + " hasDeregistered=" + hasDeregistered + " hasDataAttached=" + hasDataAttached + " hasDataDetached=" + hasDataDetached + " hasDataRegStateChanged=" + hasDataRegStateChanged + " hasRilVoiceRadioTechnologyChanged= " + hasRilVoiceRadioTechnologyChanged + " hasRilDataRadioTechnologyChanged=" + hasRilDataRadioTechnologyChanged + " hasChanged=" + hasChanged + " hasVoiceRoamingOn=" + hasVoiceRoamingOn + " hasVoiceRoamingOff=" + hasVoiceRoamingOff + " hasDataRoamingOn=" + hasDataRoamingOn + " hasDataRoamingOff=" + hasDataRoamingOff + " hasLocationChanged=" + hasLocationChanged + " has4gHandoff = " + has4gHandoff + " hasMultiApnSupport=" + hasMultiApnSupport + " hasLostMultiApnSupport=" + hasLostMultiApnSupport);
        }
        if (hasVoiceRegStateChanged || hasDataRegStateChanged) {
            int i;
            if (this.mPhone.isPhoneTypeGsm()) {
                i = EventLogTags.GSM_SERVICE_STATE_CHANGE;
            } else {
                i = EventLogTags.CDMA_SERVICE_STATE_CHANGE;
            }
            EventLog.writeEvent(i, new Object[]{Integer.valueOf(this.mSS.getVoiceRegState()), Integer.valueOf(this.mSS.getDataRegState()), Integer.valueOf(this.mNewSS.getVoiceRegState()), Integer.valueOf(this.mNewSS.getDataRegState())});
        }
        int newOosFlag = 0;
        if (this.mNewSS.getVoiceRegState() == 1 && (this.mNewSS.isEmergencyOnly() ^ 1) != 0) {
            newOosFlag = 1;
        }
        if (newOosFlag != this.oosFlag) {
            SystemProperties.set("gsm.oppo.oos" + this.mPhone.getPhoneId(), newOosFlag == 1 ? "1" : "0");
            this.oosFlag = newOosFlag;
            log("newOosFlag " + newOosFlag);
        }
        if (OemConstant.EXP_VERSION && RegionLockConstant.IS_REGION_LOCK && RegionLockConstant.getRegionLockStatus() && (oppoIsTestCard() ^ 1) != 0) {
            log("Davis oppoNeedSetRadio==" + this.oppoNeedSetRadio + ",oppoNeedSetAlarm==" + this.oppoNeedSetAlarm);
            CharSequence plmn = null;
            if (getOemRegState(this.mNewSS) == 0 && !TextUtils.isEmpty(this.mNewSS.getOperatorNumeric())) {
                plmn = this.mNewSS.getOperatorNumeric();
            }
            if (TextUtils.isEmpty(plmn)) {
                if (!(this.mNewSS.getVoiceRegState() == 0 || (this.oppoNeedSetAlarm ^ 1) == 0)) {
                    this.oppoNeedSetAlarm = true;
                    cancelNetworkStatusAlarm(this.mPhone.getPhoneId());
                    log("Davis cancel alarm");
                }
                if (!(this.mNewSS.getVoiceRegState() == 0 || (this.oppoNeedSetRadio ^ 1) == 0)) {
                    this.oppoNeedSetRadio = true;
                    log("Davis reset radio flag");
                }
            } else {
                if (this.regionLockPlmnList.oppoIsWhiteListNetwork(RegionLockConstant.VERSION, plmn)) {
                    if (this.oppoNeedSetAlarm) {
                        startResetNetworkStatusAlarm(getPhoneId());
                        this.oppoNeedSetAlarm = false;
                        log("Davis start alarm");
                    }
                    SystemProperties.set(RegionLockConstant.NOTIFY_NETLOCK_FLAG, "0");
                } else {
                    if (this.regionLockPlmnList.oppoIsBlackListNetwork(RegionLockConstant.VERSION, plmn)) {
                        oppoSetPowerRadioOff(this.mPhone.getPhoneId());
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
        if (this.mPhone.isPhoneTypeGsm()) {
            if (hasRilVoiceRadioTechnologyChanged) {
                int cid = -1;
                GsmCellLocation loc = (GsmCellLocation) this.mNewCellLoc;
                if (loc != null) {
                    cid = loc.getCid();
                }
                EventLog.writeEvent(EventLogTags.GSM_RAT_SWITCHED_NEW, new Object[]{Integer.valueOf(cid), Integer.valueOf(this.mSS.getRilVoiceRadioTechnology()), Integer.valueOf(this.mNewSS.getRilVoiceRadioTechnology())});
                if (DBG) {
                    log("RAT switched " + ServiceState.rilRadioTechnologyToString(this.mSS.getRilVoiceRadioTechnology()) + " -> " + ServiceState.rilRadioTechnologyToString(this.mNewSS.getRilVoiceRadioTechnology()) + " at cell " + cid);
                }
            }
            this.mReasonDataDenied = this.mNewReasonDataDenied;
            this.mMaxDataCalls = this.mNewMaxDataCalls;
            this.mRejectCode = this.mNewRejectCode;
        }
        ServiceState tss = this.mSS;
        this.mSS = this.mNewSS;
        this.mNewSS = tss;
        this.mNewSS.setStateOutOfService();
        CellLocation tcl = this.mCellLoc;
        this.mCellLoc = this.mNewCellLoc;
        this.mNewCellLoc = tcl;
        if (hasRilVoiceRadioTechnologyChanged) {
            updatePhoneObject();
        }
        TelephonyManager tm = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
        if (hasRilDataRadioTechnologyChanged) {
            tm.setDataNetworkTypeForPhone(this.mPhone.getPhoneId(), this.mSS.getRilDataRadioTechnology());
            if (18 == this.mSS.getRilDataRadioTechnology()) {
                log("pollStateDone: IWLAN enabled");
            }
        }
        if (hasRegistered) {
            this.mNetworkAttachedRegistrants.notifyRegistrants();
            if (DBG) {
                log("pollStateDone: registering current mNitzUpdatedTime=" + this.mNitzUpdatedTime + " changing to false");
            }
            this.mNitzUpdatedTime = false;
        }
        if (hasDeregistered) {
            this.mNetworkDetachedRegistrants.notifyRegistrants();
        }
        if (hasRejectCauseChanged) {
            setNotification(CS_REJECT_CAUSE_ENABLED);
        }
        this.mOppoNeedNotify = this.mPhone.getOppoNeedNotifyStatus();
        if (hasChanged || this.mOppoNeedNotify) {
            this.mPhone.setOppoNeedNotifyStatus(false);
            updateSpnDisplay();
            tm.setNetworkOperatorNameForPhone(this.mPhone.getPhoneId(), this.mSS.getOperatorAlpha());
            String prevOperatorNumeric = tm.getNetworkOperatorForPhone(this.mPhone.getPhoneId());
            String operatorNumeric = this.mSS.getOperatorNumeric();
            if (!this.mPhone.isPhoneTypeGsm() && isInvalidOperatorNumeric(operatorNumeric)) {
                operatorNumeric = fixUnknownMcc(operatorNumeric, this.mSS.getSystemId());
            }
            tm.setNetworkOperatorNumericForPhone(this.mPhone.getPhoneId(), operatorNumeric);
            updateCarrierMccMncConfiguration(operatorNumeric, prevOperatorNumeric, this.mPhone.getContext());
            if (isInvalidOperatorNumeric(operatorNumeric)) {
                if (DBG) {
                    log("operatorNumeric " + operatorNumeric + " is invalid");
                }
                tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), SpnOverride.MVNO_TYPE_NONE);
                this.mGotCountryCode = false;
                this.mNitzUpdatedTime = false;
            } else if (this.mSS.getRilDataRadioTechnology() != 18) {
                String iso = SpnOverride.MVNO_TYPE_NONE;
                String mcc = SpnOverride.MVNO_TYPE_NONE;
                try {
                    mcc = operatorNumeric.substring(0, 3);
                    iso = MccTable.countryCodeForMcc(Integer.parseInt(mcc));
                } catch (RuntimeException ex) {
                }
                tm.setNetworkCountryIsoForPhone(this.mPhone.getPhoneId(), iso);
                this.mGotCountryCode = true;
                if (!(this.mNitzUpdatedTime || (mcc.equals(INVALID_MCC) ^ 1) == 0 || (TextUtils.isEmpty(iso) ^ 1) == 0 || !getAutoTimeZone())) {
                    boolean testOneUniqueOffsetPath = SystemProperties.getBoolean("telephony.test.ignore.nitz", false) ? (SystemClock.uptimeMillis() & 1) == 0 : false;
                    List<String> uniqueZoneIds = TimeUtils.getTimeZoneIdsWithUniqueOffsets(iso);
                    if (uniqueZoneIds.size() == 1 || testOneUniqueOffsetPath) {
                        String zoneId = (String) uniqueZoneIds.get(0);
                        if (DBG) {
                            log("pollStateDone: no nitz but one TZ for iso-cc=" + iso + " with zone.getID=" + zoneId + " testOneUniqueOffsetPath=" + testOneUniqueOffsetPath);
                        }
                        this.mTimeZoneLog.log("pollStateDone: set time zone=" + zoneId + " mcc=" + mcc + " iso=" + iso);
                        setAndBroadcastNetworkSetTimeZone(zoneId);
                    } else if (DBG) {
                        log("pollStateDone: there are " + uniqueZoneIds.size() + " unique offsets for iso-cc='" + iso + " testOneUniqueOffsetPath=" + testOneUniqueOffsetPath + "', do nothing");
                    }
                }
                if (!this.mPhone.isPhoneTypeGsm()) {
                    setOperatorIdd(operatorNumeric);
                }
                if (shouldFixTimeZoneNow(this.mPhone, operatorNumeric, prevOperatorNumeric, this.mNeedFixZoneAfterNitz)) {
                    fixTimeZone(iso);
                }
            }
            int phoneId = this.mPhone.getPhoneId();
            boolean voiceRoaming = this.mPhone.isPhoneTypeGsm() ? this.mSS.getVoiceRoaming() : !this.mSS.getVoiceRoaming() ? this.mSS.getDataRoaming() : true;
            tm.setNetworkRoamingForPhone(phoneId, voiceRoaming);
            setRoamingType(this.mSS);
            log("Broadcasting ServiceState : " + this.mSS);
            this.mPhone.notifyServiceStateChanged(this.mSS);
            this.mPhone.getContext().getContentResolver().insert(ServiceStateTable.getUriForSubscriptionId(this.mPhone.getSubId()), ServiceStateTable.getContentValuesForServiceState(this.mSS));
            TelephonyMetrics.getInstance().writeServiceStateChanged(this.mPhone.getPhoneId(), this.mSS);
            if (hasRegistered || hasDataAttached || hasRilVoiceRadioTechnologyChanged || hasRilDataRadioTechnologyChanged) {
                if (DBG) {
                    log("service state rat changed should update and notify signal");
                }
                updateOEMSmooth(this.mSS);
            }
            if (hasLteDataDetach) {
                if (DBG) {
                    log("CdmaLte has4gHandoff so the value of isGSM will change");
                }
                this.mCi.getSignalStrength(obtainMessage(EVENT_GET_SIGNAL_STRENGTH_ONCE));
            }
        }
        if (hasDataAttached || has4gHandoff || hasDataDetached || hasRegistered || hasDeregistered) {
            logAttachChange();
        }
        if (hasDataAttached || has4gHandoff) {
            this.mAttachedRegistrants.notifyRegistrants();
        }
        if (hasDataDetached) {
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
        }
        if (hasLocationChanged) {
            this.mPhone.notifyLocationChanged();
        }
        if (hasCssIndicatorChanged) {
            this.mPhone.notifyDataConnection(PhoneInternalInterface.REASON_CSS_INDICATOR_CHANGED);
        }
        if (this.mPhone.isPhoneTypeGsm()) {
            if (isGprsConsistent(this.mSS.getDataRegState(), this.mSS.getVoiceRegState())) {
                this.mReportedGprsNoReg = false;
            } else if (!(this.mStartedGprsRegCheck || (this.mReportedGprsNoReg ^ 1) == 0)) {
                this.mStartedGprsRegCheck = true;
                sendMessageDelayed(obtainMessage(22), (long) Global.getInt(this.mPhone.getContext().getContentResolver(), "gprs_register_check_period_ms", DEFAULT_GPRS_CHECK_PERIOD_MILLIS));
            }
        }
    }

    private void updateOperatorNameFromEri() {
        String eriText;
        if (this.mPhone.isPhoneTypeCdma()) {
            if (this.mCi.getRadioState().isOn() && (this.mIsSubscriptionFromRuim ^ 1) != 0) {
                if (this.mSS.getVoiceRegState() == 0) {
                    eriText = this.mPhone.getCdmaEriText();
                } else {
                    eriText = this.mPhone.getContext().getText(17040801).toString();
                }
                this.mSS.setOperatorAlphaLong(eriText);
            }
        } else if (this.mPhone.isPhoneTypeCdmaLte()) {
            boolean hasBrandOverride = this.mUiccController.getUiccCard(getPhoneId()) != null ? this.mUiccController.getUiccCard(getPhoneId()).getOperatorBrandOverride() != null : false;
            if (!hasBrandOverride && this.mCi.getRadioState().isOn() && this.mPhone.isEriFileLoaded() && ((!ServiceState.isLte(this.mSS.getRilVoiceRadioTechnology()) || this.mPhone.getContext().getResources().getBoolean(17956867)) && (this.mIsSubscriptionFromRuim ^ 1) != 0)) {
                eriText = this.mSS.getOperatorAlpha();
                if (this.mSS.getVoiceRegState() == 0) {
                    eriText = this.mPhone.getCdmaEriText();
                } else if (this.mSS.getVoiceRegState() == 3) {
                    eriText = this.mIccRecords != null ? this.mIccRecords.getServiceProviderName() : null;
                    if (TextUtils.isEmpty(eriText)) {
                        eriText = SystemProperties.get("ro.cdma.home.operator.alpha");
                    }
                } else if (this.mSS.getDataRegState() != 0) {
                    eriText = this.mPhone.getContext().getText(17040801).toString();
                }
                this.mSS.setOperatorAlphaLong(eriText);
            }
            if (this.mUiccApplcation != null && this.mUiccApplcation.getState() == AppState.APPSTATE_READY && this.mIccRecords != null && getCombinedRegState() == 0 && (ServiceState.isLte(this.mSS.getRilVoiceRadioTechnology()) ^ 1) != 0) {
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
        if (!this.mPhone.getUnitTestMode()) {
            String idd = this.mHbpcdUtils.getIddByMcc(Integer.parseInt(operatorNumeric.substring(0, 3)));
            if (idd == null || (idd.isEmpty() ^ 1) == 0) {
                SystemProperties.set("gsm.operator.idpstring", "+");
            } else {
                SystemProperties.set("gsm.operator.idpstring", idd);
            }
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
        int simState = SubscriptionManager.getSimStateForSlotIndex(SubscriptionManager.getSlotIndex(this.mPhone.getSubId()));
        log("fixUnknownMcc simState == " + simState + " slotid == " + SubscriptionManager.getSlotIndex(this.mPhone.getSubId()));
        if (sid <= 0 || simState != 5) {
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
        if (SpnOverride.MVNO_TYPE_NONE.equals(isoCountryCode) && this.mNeedFixZoneAfterNitz) {
            zone = getNitzTimeZone(this.mZoneOffset, this.mZoneDst, this.mZoneTime);
            if (DBG) {
                log("pollStateDone: using NITZ TimeZone");
            }
        } else if (this.mZoneOffset != 0 || this.mZoneDst || zoneName == null || zoneName.length() <= 0 || Arrays.binarySearch(GMT_COUNTRY_CODES, isoCountryCode) >= 0) {
            zone = TimeUtils.getTimeZone(this.mZoneOffset, this.mZoneDst, this.mZoneTime, isoCountryCode);
            if (DBG) {
                log("fixTimeZone: using getTimeZone(off, dst, time, iso)");
            }
        } else if (this.mNitzUpdatedTime || !fixTimeZoneOem()) {
            zone = TimeZone.getDefault();
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
        } else {
            this.mNeedFixZoneAfterNitz = false;
            return;
        }
        this.mTimeZoneLog.log("fixTimeZone zoneName=" + zoneName + " mZoneOffset=" + this.mZoneOffset + " mZoneDst=" + this.mZoneDst + " iso-cc=" + isoCountryCode + " mNeedFixZoneAfterNitz=" + this.mNeedFixZoneAfterNitz + " zone=" + (zone != null ? zone.getID() : "NULL"));
        if (zone != null) {
            log("fixTimeZone: zone != null zone.getID=" + zone.getID());
            if (getAutoTimeZone()) {
                setAndBroadcastNetworkSetTimeZone(zone.getID());
            } else {
                log("fixTimeZone: skip changing zone as getAutoTimeZone was false");
            }
            if (this.mNeedFixZoneAfterNitz || this.mSavedTimeZone == null) {
                saveNitzTimeZone(zone.getID());
            }
        } else {
            log("fixTimeZone: zone == null, do nothing for zone");
        }
        this.mNeedFixZoneAfterNitz = false;
    }

    private boolean isGprsConsistent(int dataRegState, int voiceRegState) {
        return voiceRegState != 0 || dataRegState == 0;
    }

    private TimeZone getNitzTimeZone(int offset, boolean dst, long when) {
        TimeZone guess = findTimeZone(offset, dst, when);
        if (guess == null) {
            guess = findTimeZone(offset, dst ^ 1, when);
        }
        if (DBG) {
            log("getNitzTimeZone returning " + (guess == null ? guess : guess.getID()));
        }
        return guess;
    }

    private TimeZone findTimeZone(int offset, boolean dst, long when) {
        int rawOffset = offset;
        if (dst) {
            rawOffset = offset - 3600000;
        }
        String[] zones = TimeZone.getAvailableIDs(rawOffset);
        Date d = new Date(when);
        for (String zone : zones) {
            TimeZone tz = TimeZone.getTimeZone(zone);
            if (tz.getOffset(when) == offset && tz.inDaylightTime(d) == dst) {
                return tz;
            }
        }
        return null;
    }

    private int regCodeToServiceState(int code) {
        switch (code) {
            case 1:
            case 5:
                return 0;
            default:
                return 1;
        }
    }

    private boolean regCodeIsRoaming(int code) {
        return 5 == code;
    }

    private boolean isSameOperatorNameFromSimAndSS(ServiceState s) {
        String spn = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getSimOperatorNameForPhone(getPhoneId());
        String onsl = s.getOperatorAlphaLong();
        String onss = s.getOperatorAlphaShort();
        boolean equalsOnsl = !TextUtils.isEmpty(spn) ? spn.equalsIgnoreCase(onsl) : false;
        boolean equalsOnss = !TextUtils.isEmpty(spn) ? spn.equalsIgnoreCase(onss) : false;
        String simNumeric = getSystemProperty("gsm.sim.operator.numeric", SpnOverride.MVNO_TYPE_NONE);
        String operatorNumeric = s.getOperatorNumeric();
        if (simNumeric.equals("23211") && operatorNumeric.equals("23201")) {
            equalsOnsl = true;
        }
        if (equalsOnsl) {
            return true;
        }
        return equalsOnss;
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
        CarrierConfigManager configManager = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        Object[] numericArray = null;
        if (configManager != null) {
            PersistableBundle config = configManager.getConfigForSubId(this.mPhone.getSubId());
            if (config != null) {
                numericArray = config.getStringArray("non_roaming_operator_string_array");
            }
        }
        if (ArrayUtils.isEmpty(numericArray) || operatorNumeric == null) {
            return false;
        }
        for (String numeric : numericArray) {
            if (!TextUtils.isEmpty(numeric) && operatorNumeric.startsWith(numeric)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOperatorConsideredRoaming(ServiceState s) {
        String operatorNumeric = s.getOperatorNumeric();
        CarrierConfigManager configManager = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        Object[] numericArray = null;
        if (configManager != null) {
            PersistableBundle config = configManager.getConfigForSubId(this.mPhone.getSubId());
            if (config != null) {
                numericArray = config.getStringArray("roaming_operator_string_array");
            }
        }
        if (ArrayUtils.isEmpty(numericArray) || operatorNumeric == null) {
            return false;
        }
        for (String numeric : numericArray) {
            if (!TextUtils.isEmpty(numeric) && operatorNumeric.startsWith(numeric)) {
                return true;
            }
        }
        return false;
    }

    private void onRestrictedStateChanged(AsyncResult ar) {
        boolean z = true;
        RestrictedState newRs = new RestrictedState();
        if (DBG) {
            log("onRestrictedStateChanged: E rs " + this.mRestrictedState);
        }
        if (ar.exception == null && ar.result != null) {
            boolean z2;
            int state = ((Integer) ar.result).intValue();
            if ((state & 1) != 0) {
                z2 = true;
            } else if ((state & 4) != 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            newRs.setCsEmergencyRestricted(z2);
            if (this.mUiccApplcation != null && this.mUiccApplcation.getState() == AppState.APPSTATE_READY) {
                if ((state & 2) != 0) {
                    z2 = true;
                } else if ((state & 4) != 0) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                newRs.setCsNormalRestricted(z2);
                if ((state & 16) == 0) {
                    z = false;
                }
                newRs.setPsRestricted(z);
            }
            if (DBG) {
                log("onRestrictedStateChanged: new rs " + newRs);
            }
            if (!this.mRestrictedState.isPsRestricted() && newRs.isPsRestricted()) {
                this.mPsRestrictEnabledRegistrants.notifyRegistrants();
                setNotification(1001);
            } else if (this.mRestrictedState.isPsRestricted() && (newRs.isPsRestricted() ^ 1) != 0) {
                this.mPsRestrictDisabledRegistrants.notifyRegistrants();
                setNotification(1002);
            }
            if (this.mRestrictedState.isCsRestricted()) {
                if (!newRs.isAnyCsRestricted()) {
                    setNotification(1004);
                } else if (!newRs.isCsNormalRestricted()) {
                    setNotification(1006);
                } else if (!newRs.isCsEmergencyRestricted()) {
                    setNotification(1005);
                }
            } else if (!this.mRestrictedState.isCsEmergencyRestricted() || (this.mRestrictedState.isCsNormalRestricted() ^ 1) == 0) {
                if (this.mRestrictedState.isCsEmergencyRestricted() || !this.mRestrictedState.isCsNormalRestricted()) {
                    if (newRs.isCsRestricted()) {
                        setNotification(1003);
                    } else if (newRs.isCsEmergencyRestricted()) {
                        setNotification(1006);
                    } else if (newRs.isCsNormalRestricted()) {
                        setNotification(1005);
                    }
                } else if (!newRs.isAnyCsRestricted()) {
                    setNotification(1004);
                } else if (newRs.isCsRestricted()) {
                    setNotification(1003);
                } else if (newRs.isCsEmergencyRestricted()) {
                    setNotification(1006);
                }
            } else if (!newRs.isAnyCsRestricted()) {
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

    public CellLocation getCellLocation(WorkSource workSource) {
        if (((GsmCellLocation) this.mCellLoc).getLac() >= 0 && ((GsmCellLocation) this.mCellLoc).getCid() >= 0) {
            return this.mCellLoc;
        }
        List<CellInfo> result = getAllCellInfo(workSource);
        if (result == null) {
            return this.mCellLoc;
        }
        GsmCellLocation cellLocOther = new GsmCellLocation();
        for (CellInfo ci : result) {
            if (ci instanceof CellInfoGsm) {
                android.telephony.CellIdentityGsm cellIdentityGsm = ((CellInfoGsm) ci).getCellIdentity();
                cellLocOther.setLacAndCid(cellIdentityGsm.getLac(), cellIdentityGsm.getCid());
                cellLocOther.setPsc(cellIdentityGsm.getPsc());
                return cellLocOther;
            } else if (ci instanceof CellInfoWcdma) {
                android.telephony.CellIdentityWcdma cellIdentityWcdma = ((CellInfoWcdma) ci).getCellIdentity();
                cellLocOther.setLacAndCid(cellIdentityWcdma.getLac(), cellIdentityWcdma.getCid());
                cellLocOther.setPsc(cellIdentityWcdma.getPsc());
                return cellLocOther;
            } else if ((ci instanceof CellInfoLte) && (cellLocOther.getLac() < 0 || cellLocOther.getCid() < 0)) {
                android.telephony.CellIdentityLte cellIdentityLte = ((CellInfoLte) ci).getCellIdentity();
                if (!(cellIdentityLte.getTac() == Integer.MAX_VALUE || cellIdentityLte.getCi() == Integer.MAX_VALUE)) {
                    cellLocOther.setLacAndCid(cellIdentityLte.getTac(), cellIdentityLte.getCi());
                    cellLocOther.setPsc(0);
                }
            }
        }
        return cellLocOther;
    }

    /* JADX WARNING: Missing block: B:73:0x0348, code:
            if (r42.mZoneDst != (r8 != 0)) goto L_0x0203;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setTimeFromNITZString(String nitz, long nitzReceiveTime) {
        if (OemConstant.getPowerCenterEnable(this.mPhone.getContext())) {
            mNITZCount++;
            if (DBG) {
                log("[POWERSTATE]mNITZCount:" + mNITZCount);
            }
        }
        long start = SystemClock.elapsedRealtime();
        if (DBG) {
            log("NITZ: " + nitz + "," + nitzReceiveTime + " start=" + start + " delay=" + (start - nitzReceiveTime));
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
            c.set(2, Integer.parseInt(nitzSubs[1]) - 1);
            c.set(5, Integer.parseInt(nitzSubs[2]));
            c.set(10, Integer.parseInt(nitzSubs[3]));
            c.set(12, Integer.parseInt(nitzSubs[4]));
            c.set(13, Integer.parseInt(nitzSubs[5]));
            boolean sign = nitz.indexOf(45) == -1;
            int tzOffset = Integer.parseInt(nitzSubs[6]);
            int dst = nitzSubs.length >= 8 ? Integer.parseInt(nitzSubs[7]) : 0;
            tzOffset = ((((sign ? 1 : -1) * tzOffset) * 15) * 60) * 1000;
            TimeZone zone = null;
            if (nitzSubs.length >= 9) {
                zone = TimeZone.getTimeZone(nitzSubs[8].replace('!', '/'));
            }
            String iso = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getNetworkCountryIsoForPhone(this.mPhone.getPhoneId());
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
            String tmpLog = "NITZ: nitz=" + nitz + " nitzReceiveTime=" + nitzReceiveTime + " tzOffset=" + tzOffset + " dst=" + dst + " zone=" + (zone != null ? zone.getID() : "NULL") + " iso=" + iso + " mGotCountryCode=" + this.mGotCountryCode + " mNeedFixZoneAfterNitz=" + this.mNeedFixZoneAfterNitz + " getAutoTimeZone()=" + getAutoTimeZone();
            if (DBG) {
                log(tmpLog);
            }
            this.mTimeZoneLog.log(tmpLog);
            if (zone != null) {
                if (getAutoTimeZone()) {
                    setAndBroadcastNetworkSetTimeZone(zone.getID());
                }
                saveNitzTimeZone(zone.getID());
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
                            log("NITZ: not setting time, processing has taken " + (millisSinceNitzReceived / NITZ_NTP_INTERVAL_OEM) + " days");
                        }
                        if (DBG) {
                            end = SystemClock.elapsedRealtime();
                            log("NITZ: end=" + end + " dur=" + (end - start));
                        }
                        this.mWakeLock.release();
                        return;
                    } else {
                        c.add(14, (int) millisSinceNitzReceived);
                        tmpLog = "NITZ: nitz=" + nitz + " nitzReceiveTime=" + nitzReceiveTime + " Setting time of day to " + c.getTime() + " NITZ receive delay(ms): " + millisSinceNitzReceived + " gained(ms): " + (c.getTimeInMillis() - System.currentTimeMillis()) + " from " + nitz;
                        if (DBG) {
                            log(tmpLog);
                        }
                        this.mTimeLog.log(tmpLog);
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
                            int isUseNtp = isUseNtptime(c.getTimeInMillis());
                            if (isUseNtp == 2) {
                                log("skip NITZ time if ntp time has been set when Unsol response received for UNSOL_NITZ_TIME_RECEIVED");
                                if (DBG) {
                                    end = SystemClock.elapsedRealtime();
                                    log("NITZ: end=" + end + " dur=" + (end - start));
                                }
                                this.mWakeLock.release();
                                return;
                            } else if (isUseNtp == 0) {
                                setAndBroadcastNetworkSetTime(c.getTimeInMillis());
                            }
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
        this.mSavedTimeZone = zoneId;
    }

    private void saveNitzTime(long time) {
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
        if (this.mSS != null && "51503".equals(this.mSS.getOperatorNumeric())) {
            if (DBG) {
                log("setAndBroadcastNetworkSetTime plmn is 51503, sometimes, the time of nitz is not exact, so we don`t set time with nitz, immediate return");
            }
        } else if (shouldSkipThisTime()) {
            if (DBG) {
                log("setAndBroadcastNetworkSetTime, skip this time");
            }
        } else {
            SystemClock.setCurrentTimeMillis(time);
            Intent intent = new Intent("android.intent.action.NETWORK_SET_TIME");
            intent.addFlags(536870912);
            intent.putExtra("time", time);
            this.mPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            TelephonyMetrics.getInstance().writeNITZEvent(this.mPhone.getPhoneId(), time);
        }
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
        if (Global.getInt(this.mCr, "auto_time", 0) != 0) {
            if (DBG) {
                log("Reverting to NITZ Time: mSavedTime=" + this.mSavedTime + " mSavedAtTime=" + this.mSavedAtTime);
            }
            if (!(this.mSavedTime == 0 || this.mSavedAtTime == 0)) {
                long currTime = SystemClock.elapsedRealtime();
                this.mTimeLog.log("Reverting to NITZ time, currTime=" + currTime + " mSavedAtTime=" + this.mSavedAtTime + " mSavedTime=" + this.mSavedTime);
                if (isUseNtptime(this.mSavedTime + (currTime - this.mSavedAtTime)) == 2) {
                    log("skip NITZ time if ntp time has been set when AUTO_TIME is enabled");
                    return;
                }
                setAndBroadcastNetworkSetTime(this.mSavedTime + (currTime - this.mSavedAtTime));
            }
        }
    }

    private void revertToNitzTimeZone() {
        if (Global.getInt(this.mCr, "auto_time_zone", 0) != 0) {
            if (this.mNitzUpdatedTime || !fixTimeZoneOem()) {
                String tmpLog = "Reverting to NITZ TimeZone: tz=" + this.mSavedTimeZone;
                if (DBG) {
                    log(tmpLog);
                }
                this.mTimeZoneLog.log(tmpLog);
                if (this.mSavedTimeZone != null) {
                    setAndBroadcastNetworkSetTimeZone(this.mSavedTimeZone);
                }
            }
        }
    }

    private void cancelAllNotifications() {
        if (DBG) {
            log("setNotification: cancelAllNotifications");
        }
        ((NotificationManager) this.mPhone.getContext().getSystemService("notification")).cancelAll();
    }

    private boolean fixTimeZoneOem() {
        try {
            boolean testOneUniqueOffsetPath = SystemProperties.getBoolean("telephony.test.ignore.nitz", false) ? (SystemClock.uptimeMillis() & 1) == 0 : false;
            String operatorNumeric = this.mSS == null ? null : this.mSS.getOperatorNumeric();
            String iso = SpnOverride.MVNO_TYPE_NONE;
            if (DBG) {
                log("setTimeZone operatorNumeric=" + operatorNumeric);
            }
            if (operatorNumeric != null) {
                String mcc = SpnOverride.MVNO_TYPE_NONE;
                try {
                    String[] operatorNumericArray = operatorNumeric.split(",");
                    int i = 0;
                    while (i < operatorNumericArray.length) {
                        if (operatorNumericArray[i] == null || operatorNumericArray[i].length() <= 0) {
                            i++;
                        } else {
                            mcc = operatorNumericArray[i].substring(0, 3);
                            if (!(mcc == null || (mcc.equals(INVALID_MCC) ^ 1) == 0)) {
                                iso = MccTable.countryCodeForMcc(Integer.parseInt(mcc));
                            }
                        }
                    }
                } catch (Exception ex) {
                    loge("setTimeZone: countryCodeForMcc error" + ex);
                }
            }
            if (DBG) {
                log("setTimeZone iso 1=" + iso);
            }
            if (iso.equals(SpnOverride.MVNO_TYPE_NONE)) {
                if (DBG) {
                    log("setTimeZone: get iso from operatorNumeric fail");
                }
                if (!this.mPhone.isPhoneTypeCdma()) {
                    iso = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getNetworkCountryIsoForPhone(this.mPhone.getPhoneId());
                }
            }
            if (DBG) {
                log("setTimeZone iso 2=" + iso);
            }
            List<String> uniqueZoneIds = TimeUtils.getTimeZoneIdsWithUniqueOffsets(iso);
            if (uniqueZoneIds == null) {
                log("---null pointer exception---");
                return false;
            } else if (uniqueZoneIds.size() == 1 || testOneUniqueOffsetPath) {
                String zoneId = (String) uniqueZoneIds.get(0);
                if (zoneId == null) {
                    return false;
                }
                if (DBG) {
                    log("setTimeZone: no nitz but one TZ for iso-cc=" + iso + " with zone.getID=" + zoneId + " testOneUniqueOffsetPath=" + testOneUniqueOffsetPath);
                }
                setAndBroadcastNetworkSetTimeZone(zoneId);
                if (this.mSavedTimeZone != null) {
                    saveNitzTimeZone(zoneId);
                }
                return true;
            } else if (uniqueZoneIds.size() > 1) {
                if (DBG) {
                    log("uniqueZones.size=" + uniqueZoneIds.size());
                }
                TimeZone zone = getTimeZonesWithCapitalCity(iso);
                if (zone == null) {
                    return false;
                }
                setAndBroadcastNetworkSetTimeZone(zone.getID());
                if (this.mSavedTimeZone != null) {
                    saveNitzTimeZone(zone.getID());
                }
                return true;
            } else {
                if (DBG) {
                    log("setTimeZone: there are " + uniqueZoneIds.size() + " unique offsets for iso-cc='" + iso + " testOneUniqueOffsetPath=" + testOneUniqueOffsetPath + "', do nothing");
                }
                return false;
            }
        } catch (Exception ex2) {
            ex2.printStackTrace();
            return false;
        }
    }

    public void setNotification(int notifyType) {
        if (DBG) {
            log("Ignore all the notifications");
        }
    }

    private int selectResourceForRejectCode(int rejCode) {
        switch (rejCode) {
            case 1:
                return 17040328;
            case 2:
                return 17040331;
            case 3:
                return 17040330;
            case 6:
                return 17040329;
            default:
                return 0;
        }
    }

    private UiccCardApplication getUiccCardApplication() {
        if (this.mPhone.isPhoneTypeGsm()) {
            return this.mUiccController.getUiccCardApplication(this.mPhone.getPhoneId(), 1);
        }
        return this.mUiccController.getUiccCardApplication(this.mPhone.getPhoneId(), 2);
    }

    private void queueNextSignalStrengthPoll() {
        if (!this.mDontPollSignalStrength) {
            if (DBG) {
                log("queueNextSignalStrengthPoll");
            }
            removeMessages(10);
            Message msg = obtainMessage();
            msg.what = 10;
            sendMessageDelayed(msg, 20000);
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

    public void registerForNetworkDetached(Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        this.mNetworkDetachedRegistrants.add(r);
        if (this.mSS.getVoiceRegState() != 0) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForNetworkDetached(Handler h) {
        this.mNetworkDetachedRegistrants.remove(h);
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

    /* JADX WARNING: Missing block: B:23:0x0055, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void powerOffRadioSafely(DcTracker dcTracker) {
        synchronized (this) {
            if (!this.mPendingRadioPowerOffAfterDataOff) {
                Message msg;
                int i;
                if (this.mPhone.isPhoneTypeGsm() || this.mPhone.isPhoneTypeCdma() || this.mPhone.isPhoneTypeCdmaLte()) {
                    int dds = SubscriptionManager.getDefaultDataSubscriptionId();
                    if (!dcTracker.isDisconnected() || (dds != this.mPhone.getSubId() && (dds == this.mPhone.getSubId() || !ProxyController.getInstance().isDataDisconnected(dds)))) {
                        if (this.mPhone.isPhoneTypeGsm() && this.mPhone.isInCall()) {
                            this.mPhone.mCT.mRingingCall.hangupIfAlive();
                            this.mPhone.mCT.mBackgroundCall.hangupIfAlive();
                            this.mPhone.mCT.mForegroundCall.hangupIfAlive();
                        }
                        dcTracker.cleanUpAllConnections(PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
                        if (!(dds == this.mPhone.getSubId() || (ProxyController.getInstance().isDataDisconnected(dds) ^ 1) == 0)) {
                            if (DBG) {
                                log("Data is active on DDS.  Wait for all data disconnect");
                            }
                            ProxyController.getInstance().registerForAllDataDisconnected(dds, this, 49, null);
                            this.mPendingRadioPowerOffAfterDataOff = true;
                        }
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
                    String[] networkNotClearData = this.mPhone.getContext().getResources().getStringArray(17236062);
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

    private boolean containsEarfcnInEarfcnRange(ArrayList<Pair<Integer, Integer>> earfcnPairList, int earfcn) {
        if (earfcnPairList != null) {
            for (Pair<Integer, Integer> earfcnPair : earfcnPairList) {
                if (earfcn >= ((Integer) earfcnPair.first).intValue() && earfcn <= ((Integer) earfcnPair.second).intValue()) {
                    return true;
                }
            }
        }
        return false;
    }

    ArrayList<Pair<Integer, Integer>> convertEarfcnStringArrayToPairList(String[] earfcnsList) {
        ArrayList<Pair<Integer, Integer>> earfcnPairList = new ArrayList();
        if (earfcnsList != null) {
            int i = 0;
            while (i < earfcnsList.length) {
                try {
                    String[] earfcns = earfcnsList[i].split("-");
                    if (earfcns.length != 2) {
                        return null;
                    }
                    int earfcnStart = Integer.parseInt(earfcns[0]);
                    int earfcnEnd = Integer.parseInt(earfcns[1]);
                    if (earfcnStart > earfcnEnd) {
                        return null;
                    }
                    earfcnPairList.add(new Pair(Integer.valueOf(earfcnStart), Integer.valueOf(earfcnEnd)));
                    i++;
                } catch (PatternSyntaxException e) {
                    return null;
                } catch (NumberFormatException e2) {
                    return null;
                }
            }
        }
        return earfcnPairList;
    }

    private void updateLteEarfcnLists() {
        PersistableBundle b = ((CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config")).getConfigForSubId(this.mPhone.getSubId());
        synchronized (this.mLteRsrpBoostLock) {
            this.mLteRsrpBoost = b.getInt("lte_earfcns_rsrp_boost_int", 0);
            this.mEarfcnPairListForRsrpBoost = convertEarfcnStringArrayToPairList(b.getStringArray("boosted_lte_earfcns_string_array"));
        }
    }

    private void updateServiceStateLteEarfcnBoost(ServiceState serviceState, int lteEarfcn) {
        synchronized (this.mLteRsrpBoostLock) {
            if (lteEarfcn != -1) {
                if (containsEarfcnInEarfcnRange(this.mEarfcnPairListForRsrpBoost, lteEarfcn)) {
                    serviceState.setLteEarfcnRsrpBoost(this.mLteRsrpBoost);
                }
            }
            serviceState.setLteEarfcnRsrpBoost(0);
        }
    }

    protected boolean onSignalStrengthResult(AsyncResult ar) {
        boolean isGsm = this.mPhone.isPhoneTypeGsm();
        int dataRat = this.mSS.getRilDataRadioTechnology();
        int voiceRat = this.mSS.getRilVoiceRadioTechnology();
        if ((dataRat != 18 && ServiceState.isGsm(dataRat)) || (voiceRat != 18 && ServiceState.isGsm(voiceRat))) {
            isGsm = true;
        }
        if (ar.exception != null || ar.result == null) {
            log("onSignalStrengthResult() Exception from RIL : " + ar.exception);
            this.mSignalStrength = new SignalStrength(isGsm);
        } else {
            this.mSignalStrength = (SignalStrength) ar.result;
            this.mSignalStrength.validateInput();
            this.mSignalStrength.setGsm(isGsm);
            this.mSignalStrength.setLteRsrpBoost(this.mSS.getLteEarfcnRsrpBoost());
        }
        if (this.mIsScreenOn) {
            return updateOEMSmooth(this.mSS);
        }
        log("DQL because screen is off return ");
        this.oppoSignalUpdate = true;
        return true;
    }

    protected void hangupAndPowerOff() {
        if (!this.mPhone.isPhoneTypeGsm() || this.mPhone.isInCall()) {
            this.mPhone.mCT.mRingingCall.hangupIfAlive();
            this.mPhone.mCT.mBackgroundCall.hangupIfAlive();
            this.mPhone.mCT.mForegroundCall.hangupIfAlive();
        }
        this.mCi.setRadioPower(false, obtainMessage(53));
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

    /* JADX WARNING: Missing block: B:45:0x0085, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<CellInfo> getAllCellInfo(WorkSource workSource) {
        CellInfoResult result = new CellInfoResult(this, null);
        if (this.mCi.getRilVersion() < 8) {
            if (DBG) {
                log("SST.getAllCellInfo(): not implemented");
            }
            result.list = null;
        } else if (!isCallerOnDifferentThread()) {
            if (DBG) {
                log("SST.getAllCellInfo(): return last, same thread can't block");
            }
            result.list = this.mLastCellInfoList;
        } else if (SystemClock.elapsedRealtime() - this.mLastCellInfoListTime > LAST_CELL_INFO_LIST_MAX_AGE_MS) {
            Message msg = obtainMessage(43, result);
            synchronized (result.lockObj) {
                result.list = null;
                this.mCi.getCellInfoList(msg, workSource);
                try {
                    result.lockObj.wait(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (DBG) {
                log("SST.getAllCellInfo(): return last, back to back calls");
            }
            result.list = this.mLastCellInfoList;
        }
        synchronized (result.lockObj) {
            if (result.list != null) {
                List<CellInfo> list = result.list;
                return list;
            } else if (DBG) {
                log("SST.getAllCellInfo(): X size=0 list=null");
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
        log("Storing cdma subscription source: " + source);
        Global.putInt(this.mPhone.getContext().getContentResolver(), "subscription_mode", source);
        log("Read from settings: " + Global.getInt(this.mPhone.getContext().getContentResolver(), "subscription_mode", -1));
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
        log("isFromRuim: " + this.mIsSubscriptionFromRuim);
        saveCdmaSubscriptionSource(newSubscriptionSource);
        if (!this.mIsSubscriptionFromRuim) {
            sendMessage(obtainMessage(35));
        }
    }

    private void dumpEarfcnPairList(PrintWriter pw) {
        pw.print(" mEarfcnPairListForRsrpBoost={");
        if (this.mEarfcnPairListForRsrpBoost != null) {
            int i = this.mEarfcnPairListForRsrpBoost.size();
            for (Pair<Integer, Integer> earfcnPair : this.mEarfcnPairListForRsrpBoost) {
                pw.print("(");
                pw.print(earfcnPair.first);
                pw.print(",");
                pw.print(earfcnPair.second);
                pw.print(")");
                i--;
                if (i != 0) {
                    pw.print(",");
                }
            }
        }
        pw.println("}");
    }

    private void dumpCellInfoList(PrintWriter pw) {
        pw.print(" mLastCellInfoList={");
        if (this.mLastCellInfoList != null) {
            boolean first = true;
            for (CellInfo info : this.mLastCellInfoList) {
                if (!first) {
                    pw.print(",");
                }
                first = false;
                pw.print(info.toString());
            }
        }
        pw.println("}");
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("ServiceStateTracker:");
        pw.println(" mSubId=" + this.mSubId);
        pw.println(" mSS=" + this.mSS);
        pw.println(" mNewSS=" + this.mNewSS);
        pw.println(" mVoiceCapable=" + this.mVoiceCapable);
        pw.println(" mRestrictedState=" + this.mRestrictedState);
        pw.println(" mPollingContext=" + this.mPollingContext + " - " + (this.mPollingContext != null ? Integer.valueOf(this.mPollingContext[0]) : SpnOverride.MVNO_TYPE_NONE));
        pw.println(" mDesiredPowerState=" + this.mDesiredPowerState);
        pw.println(" mDontPollSignalStrength=" + this.mDontPollSignalStrength);
        pw.println(" mSignalStrength=" + this.mSignalStrength);
        pw.println(" mLastSignalStrength=" + this.mLastSignalStrength);
        pw.println(" mRestrictedState=" + this.mRestrictedState);
        pw.println(" mPendingRadioPowerOffAfterDataOff=" + this.mPendingRadioPowerOffAfterDataOff);
        pw.println(" mPendingRadioPowerOffAfterDataOffTag=" + this.mPendingRadioPowerOffAfterDataOffTag);
        pw.println(" mCellLoc=" + Rlog.pii(false, this.mCellLoc));
        pw.println(" mNewCellLoc=" + Rlog.pii(false, this.mNewCellLoc));
        pw.println(" mLastCellInfoListTime=" + this.mLastCellInfoListTime);
        dumpCellInfoList(pw);
        pw.flush();
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
        pw.println(" mLteRsrpBoost=" + this.mLteRsrpBoost);
        dumpEarfcnPairList(pw);
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
        ipw.println(" Radio power Log:");
        ipw.increaseIndent();
        this.mRadioPowerLog.dump(fd, ipw, args);
        ipw.println(" Time Logs:");
        ipw.increaseIndent();
        this.mTimeLog.dump(fd, ipw, args);
        ipw.decreaseIndent();
        ipw.println(" Time zone Logs:");
        ipw.increaseIndent();
        this.mTimeZoneLog.dump(fd, ipw, args);
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
            log("update mccmnc=" + newOp + " fromServiceState=true");
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
        boolean isVoiceInService = currentServiceState.getVoiceRegState() == 0;
        if (isVoiceInService) {
            if (!currentServiceState.getVoiceRoaming()) {
                currentServiceState.setVoiceRoamingType(0);
            } else if (!this.mPhone.isPhoneTypeGsm()) {
                int[] intRoamingIndicators = this.mPhone.getContext().getResources().getIntArray(17235993);
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
            } else if (inSameCountry(currentServiceState.getVoiceOperatorNumeric())) {
                currentServiceState.setVoiceRoamingType(2);
            } else {
                currentServiceState.setVoiceRoamingType(3);
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

    protected String getHomeOperatorNumeric() {
        String numeric = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getSimOperatorNumericForPhone(this.mPhone.getPhoneId());
        if (this.mPhone.isPhoneTypeGsm() || !TextUtils.isEmpty(numeric)) {
            return numeric;
        }
        return SystemProperties.get(GsmCdmaPhone.PROPERTY_CDMA_HOME_OPERATOR_NUMERIC, SpnOverride.MVNO_TYPE_NONE);
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

    public boolean isDeviceShuttingDown() {
        return this.mDeviceShuttingDown;
    }

    protected int getCombinedRegState() {
        int regState = this.mSS.getVoiceRegState();
        int dataRegState = this.mSS.getDataRegState();
        if ((regState != 1 && regState != 3) || dataRegState != 0) {
            return regState;
        }
        log("getCombinedRegState: return STATE_IN_SERVICE as Data is in service");
        return dataRegState;
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

    protected void removeSmoothMessage() {
        this.mIsPendingNotify_0 = false;
        this.mIsPendingNotify_1 = false;
        removeMessages(101);
        removeMessages(102);
    }

    protected boolean updateOEMSmooth(ServiceState st) {
        SignalStrength oem_ss = getSignalStrength();
        int[] levels = getOEMLevel(st, oem_ss);
        this.mOEMCurLevel_0 = levels[0];
        this.mOEMCurLevel_1 = levels[1];
        if (this.mIsScreenOn || (!(this.mOEMCurLevel_0 == 0 && this.mOEMCurLevel_1 == 0) && (this.mOEMCurLevel_0 >= this.mOEMLastLevel_0 || this.mOEMCurLevel_1 >= this.mOEMLastLevel_1))) {
            if (OemConstant.getWlanAssistantEnable(this.mPhone.getContext())) {
                SubscriptionManager s = SubscriptionManager.from(this.mPhone.getContext());
                boolean isDefaultDataPhone = this.mPhone.getSubId() == SubscriptionManager.getDefaultDataSubId();
                boolean signalLevelChanged = this.mOEMCurLevel_1 != this.mOEMLastLevel_1;
                if (signalLevelChanged && isDefaultDataPhone) {
                    if (DBG) {
                        log("WLAN+ EVENT_SIGNAL_UPDATE_CHANGED:signalLevelChanged = " + signalLevelChanged + " mMeasureDataState=" + DcTracker.mMeasureDataState + " Roaming=" + this.mSS.getRoaming() + " DataEnabled=" + (!this.mPhone.getDataEnabled() ? this.mPhone.mDcTracker.haveVsimIgnoreUserDataSetting() : true) + " isDefaultDataPhone" + isDefaultDataPhone);
                    }
                    boolean myMeasureDataState = (!DcTracker.mMeasureDataState || (DcTracker.mDelayMeasure ^ 1) == 0 || (this.mSS.getRoaming() ^ 1) == 0) ? false : !this.mPhone.getDataEnabled() ? this.mPhone.mDcTracker.haveVsimIgnoreUserDataSetting() : true;
                    if (myMeasureDataState) {
                        new Thread() {
                            public void run() {
                                ConnectivityManager connectivityManager = (ConnectivityManager) ServiceStateTracker.this.mPhone.getContext().getSystemService("connectivity");
                                if (!connectivityManager.measureDataState(ServiceStateTracker.this.mOEMCurLevel_1)) {
                                    NetworkRequest request = connectivityManager.getCelluarNetworkRequest();
                                    if (request != null) {
                                        if (DcTracker.mMeasureDCCallback != null) {
                                            if (ServiceStateTracker.DBG) {
                                                ServiceStateTracker.this.log("WLAN+ EVENT_SIGNAL_UPDATE_CHANGED release DC befor request: mMeasureDataState=" + DcTracker.mMeasureDataState);
                                            }
                                            try {
                                                connectivityManager.unregisterNetworkCallback(DcTracker.mMeasureDCCallback);
                                            } catch (IllegalArgumentException e) {
                                                ServiceStateTracker.this.log("WLAN+ " + e.toString());
                                            } catch (Exception e2) {
                                                ServiceStateTracker.this.log("WLAN+ Exception:" + e2.toString());
                                            }
                                        }
                                        DcTracker.mMeasureDCCallback = new NetworkCallback();
                                        connectivityManager.requestNetwork(request, DcTracker.mMeasureDCCallback);
                                        connectivityManager.measureDataState(ServiceStateTracker.this.mOEMCurLevel_1);
                                    }
                                }
                            }
                        }.start();
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
            boolean isTelstraVersion = SystemProperties.get("ro.oppo.operator", "ex").equals("TELSTRA");
            if (IS_OEM_SMOOTH && this.mOEMCurLevel_1 == 0 && this.mOEMCurLevel_0 == 0 && (isTelstraVersion ^ 1) != 0) {
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
                removeMessages(101);
            }
            if (!this.mIsPendingNotify_0) {
                nofifySmoothLevel(101);
            }
            if (this.mOEMLastLevel_1 <= this.mOEMCurLevel_1) {
                this.mIsPendingNotify_1 = false;
                this.mOEMLastLevel_1 = this.mOEMCurLevel_1;
                removeMessages(102);
            }
            if (!this.mIsPendingNotify_1) {
                nofifySmoothLevel(102);
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
        if (nt == 14 || nt == 19) {
            return 3;
        }
        return 0;
    }

    protected int[] getOEMLevel(ServiceState st, SignalStrength s) {
        if (OemConstant.EXP_VERSION) {
            int csLevel = s.getOEMLevel(getNetworkModeBySS(st.getRilVoiceRadioTechnology()));
            int psLevel = s.getOEMLevel(getNetworkModeBySS(st.getRilDataRadioTechnology()));
            if (st.getRilVoiceRadioTechnology() == 0 && psLevel > csLevel) {
                csLevel = psLevel;
            }
            return new int[]{csLevel, psLevel};
        }
        return new int[]{s.getOEMLevel(getNetworkModeBySS(st.getRilVoiceRadioTechnology())), s.getOEMLevel(getNetworkModeBySS(st.getRilDataRadioTechnology()))};
    }

    protected void nofifySmoothLevel(int msg_type) {
        if (msg_type - 101 == 0) {
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

    protected int getOemRegState(ServiceState ss) {
        int regState = ss.getVoiceRegState();
        int dataRegState = ss.getDataRegState();
        if (regState == 1 && dataRegState == 0) {
            return dataRegState;
        }
        return regState;
    }

    protected void oppoSetOperatorAlpha(String val) {
        this.mOemSpn = val;
        if (DBG) {
            log("leon OemSpn=" + this.mOemSpn);
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
        if (this.mSS.getRoaming()) {
            this.mShowPlmn = true;
            this.mShowSPn = false;
            if ((this.mIccRecords != null ? this.mIccRecords.isInCmccList(spn) : true) && ("45412".equals(operator) || "45413".equals(operator))) {
                plmn = "CMHK";
                this.mNewPlmn = plmn;
            }
        } else if (isinCnlist && (TextUtils.isEmpty(plmn) ^ 1) != 0) {
            this.mShowPlmn = true;
            this.mShowSPn = false;
        }
        if (!(this.mShowPlmn || this.mShowSPn)) {
            boolean z;
            this.mShowPlmn = TextUtils.isEmpty(plmn) ^ 1;
            if (this.mShowPlmn) {
                z = false;
            } else {
                z = TextUtils.isEmpty(spn) ^ 1;
            }
            this.mShowSPn = z;
        }
        if (this.mSubscriptionController.isHasSoftSimCard() && this.mSubscriptionController.getSoftSimCardSlotId() == getPhoneId() && (TextUtils.isEmpty(spn) ^ 1) != 0) {
            this.mShowPlmn = false;
            this.mShowSPn = true;
        }
        if (this.mShowPlmn) {
            oppoSetOperatorAlpha(plmn);
            this.mShowSPn = false;
        } else if (!this.mShowSPn || (TextUtils.isEmpty(spn) ^ 1) == 0) {
            oppoSetOperatorAlpha(plmn);
            this.mShowSPn = false;
        } else {
            oppoSetOperatorAlpha(spn);
        }
    }

    public boolean isCmcc() {
        String plmn = this.mSS.getOperatorNumeric();
        if (TextUtils.isEmpty(plmn)) {
            return false;
        }
        boolean equals = ("46000".equals(plmn) || "46002".equals(plmn)) ? true : "46007".equals(plmn);
        return equals;
    }

    protected boolean oppoCheckNoService() {
        if (!(this.isRemoveCard || (this.isCardLocked ^ 1) == 0)) {
            if (this.isInCall) {
                if (DBG) {
                    log("oppoCheckNoService--in call");
                }
                return true;
            }
            boolean chgNoService = (getOemRegState(this.mSS) != 0 || getOemRegState(this.mNewSS) == 0) ? false : (IS_OEM_SMOOTH || this.mPhone.getState() != State.IDLE) ? IS_OEM_SMOOTH : true;
            if (chgNoService && this.bCheckNoServiceAgain && this.mCi.getRadioState().isOn()) {
                long currentTime = System.currentTimeMillis();
                boolean isDelayOk = currentTime >= this.lastTime + 23000 || this.lastTime == 0;
                if (DBG) {
                    log("oppoCheckNoService currentTime: " + currentTime + "  lastTime:" + this.lastTime + "  isDelayOk:" + isDelayOk);
                }
                if (isDelayOk) {
                    this.lastTime = System.currentTimeMillis();
                    log("oppoCheckNoService : send OPPOEVENT_CHECK_NO_SERVICE event");
                    sendEmptyMessageDelayed(OPPOEVENT_CHECK_NO_SERVICE, 23000);
                    return true;
                }
                if (DBG) {
                    log(" oppoCheckNoService isDelayOk--not-ok");
                }
                return true;
            }
        }
        this.bCheckNoServiceAgain = true;
        removeMessages(OPPOEVENT_CHECK_NO_SERVICE);
        this.lastTime = 0;
        return false;
    }

    private boolean twoCTcardNeedOosDelay() {
        if (TelephonyManager.getDefault().getPhoneCount() != 2) {
            return true;
        }
        if (!OemConstant.isCtCard(PhoneFactory.getPhone(1 - this.mPhone.getPhoneId())) || (OemConstant.isCtCard(this.mPhone) ^ 1) != 0) {
            log("ct simcard check:non-ct, need smooth ");
            return true;
        } else if (this.mPhone.getServiceState().getDataRoaming()) {
            log("ct simcard check:roaming, need smooth ");
            return true;
        } else if (this.mPhone.getSubId() == SubscriptionManager.getDefaultDataSubscriptionId() || this.mImsRegistered) {
            log("ct simcard check: need smooth");
            return true;
        } else {
            log("ct simcard check:non-DDS id ,not registered ims,  DONT need smooth ");
            return false;
        }
    }

    public void oemNofiyNoService(boolean isIdle) {
        if (DBG) {
            log(" oppoCheckNoService isIdle:" + isIdle);
        }
        this.isInCall = isIdle ^ 1;
        if (isIdle) {
            pollState();
            return;
        }
        this.mSS.setVoiceRegState(1);
        this.mSS.setDataRegState(1);
        updateSpnDisplay();
        getPhone().notifyServiceStateChangedP(this.mSS);
    }

    public void oppoResetOosDelayState() {
        Phone oPhone = PhoneFactory.getPhone(1 - this.mPhone.getPhoneId());
        if (oPhone != null && oPhone.getServiceStateTracker() != null) {
            oPhone.getServiceStateTracker().mOosDelayState = 0;
        }
    }

    protected int oppoOosDelayState() {
        boolean isOos = ((getOemRegState(this.mNewSS) == 1 || getOemRegState(this.mNewSS) == 2) && this.mCi.getRadioState().isOn()) ? this.mSS.getState() != 3 : false;
        boolean inSwitchingDss = this.mSubscriptionController.getSwitchingDssState(this.mPhone.getPhoneId());
        int mSimState = SubscriptionManager.getSimStateForSlotIndex(this.mPhone.getPhoneId());
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
        } else if (inSwitchingDss || mSimState != 5 || (twoCTcardNeedOosDelay() ^ 1) != 0) {
            if (DBG) {
                log("oppoOosDelayState should not delay oos");
            }
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
        return (oPhone == null || oPhone.getState() == State.IDLE) ? false : true;
    }

    protected boolean oemPowerOffRadioSafely(Message msg) {
        int delay;
        try {
            delay = CallManager.getInstance().getState() == State.IDLE ? RegionLockConstant.EVENT_NETWORK_LOCK_STATUS : 20000;
        } catch (Exception e) {
            delay = RegionLockConstant.EVENT_NETWORK_LOCK_STATUS;
        }
        boolean ret = sendMessageDelayed(msg, (long) delay);
        if (ret && DBG) {
            log("oem Wait upto " + (delay / 1000) + "s for data to disconnect, then turn off radio.");
        }
        return ret;
    }

    protected TimeZone getTimeZonesWithCapitalCity(String iso) {
        for (int i = 0; i < this.mTimeZoneIdOfCapitalCity.length; i++) {
            if (iso.equals(this.mTimeZoneIdOfCapitalCity[i][0])) {
                TimeZone tz = TimeZone.getTimeZone(this.mTimeZoneIdOfCapitalCity[i][1]);
                log("uses TimeZone of Capital City:" + this.mTimeZoneIdOfCapitalCity[i][1]);
                return tz;
            }
        }
        return null;
    }

    public String getTelephonyPowerState() {
        long escapeTime = this.mScreenOnTime - this.mScreenOffTime;
        String[] wakeupReson = new String[]{"DATA_CALL_COUNT", "NO_SERVICE_TIME", "RESELECT_PER_MIN", "SMS_SEND_COUNT", "NITZ_COUNT"};
        double[] wakeupSrcPowerLost = new double[]{0.0d, 0.0d, 0.0d, 0.0d, 0.0d};
        wakeupSrcPowerLost[0] = ((double) mDataCallCount) * this.mPerDataCallPowerLost;
        if (escapeTime <= 0 || mNoServiceTime <= escapeTime) {
            wakeupSrcPowerLost[1] = ((double) mNoServiceTime) * this.mPerNoServicePowerLost;
        } else {
            wakeupSrcPowerLost[1] = ((double) escapeTime) * this.mPerNoServicePowerLost;
        }
        wakeupSrcPowerLost[2] = mReselectCount * this.mPerReselectLost;
        wakeupSrcPowerLost[3] = ((double) mSMSSendCount) * this.mPerSMSSendLost;
        wakeupSrcPowerLost[4] = ((double) mNITZCount) * this.mPerNITZLost;
        int topIndex = 0;
        double max = 0.0d;
        for (int i = 0; i < wakeupSrcPowerLost.length; i++) {
            if (wakeupSrcPowerLost[i] > max) {
                max = wakeupSrcPowerLost[i];
                topIndex = i;
            }
        }
        if (DBG) {
            log("[POWERSTATE]DATA_CALL_COUNT:" + mDataCallCount + " NO_SERVICE_TIME:" + mNoServiceTime + " RESELECT_PER_MIN:" + mReselectCountPerMin + " SMS_SEND_COUNT:" + mSMSSendCount + " NITZ_COUNT:" + mNITZCount);
        }
        return wakeupReson[topIndex] + ":" + max + " RILJ_TOP:" + this.mCi.oppoGetRilTopMsg();
    }

    public boolean isAlreadyUpdated() {
        return mAlreadyUpdated;
    }

    public double getTelephonyPowerLost() {
        long escapeTime = this.mScreenOnTime - this.mScreenOffTime;
        double ret = (((((double) mDataCallCount) * this.mPerDataCallPowerLost) + (mReselectCount * this.mPerReselectLost)) + (((double) mSMSSendCount) * this.mPerSMSSendLost)) + (((double) mNITZCount) * this.mPerNITZLost);
        if (escapeTime <= 0 || mNoServiceTime <= escapeTime) {
            return (((double) mNoServiceTime) * this.mPerNoServicePowerLost) + ret;
        }
        return (((double) escapeTime) * this.mPerNoServicePowerLost) + ret;
    }

    public int getSignalLevel() {
        return this.mOEMCurLevel_1;
    }

    protected String getReadTeaServiceProviderName(Context context, String providerName) {
        String spn = SpnOverride.MVNO_TYPE_NONE;
        if (TextUtils.isEmpty(providerName)) {
            return spn;
        }
        return OemConstant.getOemRes(context, "redtea_virtul_card", SpnOverride.MVNO_TYPE_NONE);
    }

    protected void oppoSetAutoNetworkSelect() {
        if (!this.isRemoveCard && this.mNewSS.getIsManualSelection()) {
            if (DBG) {
                log("WXK : manual select network ---> automatic select network for oos > 23 secounds");
            }
            this.mPhone.setNetworkSelectionModeAutomatic(null);
        }
    }

    public String[] getPnnFromEons(String plmn, boolean longName) {
        log("getPnnFromEons in");
        String[] pnnEons = new String[2];
        if (this.mPhone != null && this.mPhone.isPhoneTypeGsm()) {
            String simPlmn = null;
            SIMRecords simRecords = null;
            IccRecords r = this.mPhone != null ? (IccRecords) this.mPhone.mIccRecords.get() : null;
            if (r != null) {
                simRecords = (SIMRecords) r;
            }
            String eons = null;
            Object strNumPlmn = this.mSS != null ? this.mSS.getOperatorNumeric() : null;
            String simOperatorNumeric = r != null ? r.getOperatorNumeric() : null;
            String simCardMccMnc = r != null ? r.getOperatorNumeric() : null;
            CharSequence spn = r != null ? r.getServiceProviderName() : null;
            if (DBG) {
                log("getPnnFromEons spn = " + spn);
            }
            if (plmn == null || !(plmn.equals("50503") || plmn.equals("50502") || plmn.equals("50501") || plmn.equals("50506"))) {
                if (plmn != null && ((plmn.equals("46605") || plmn.equals("46697")) && OppoGsmServiceStateTracker.isGT4GSimCardCheck(simCardMccMnc))) {
                    try {
                        if (this.mCellLoc != null) {
                            eons = simRecords != null ? simRecords.getEonsIfExist(plmn, ((GsmCellLocation) this.mCellLoc).getLac(), longName) : null;
                        }
                    } catch (RuntimeException ex) {
                        loge("Exception while getEonsIfExist. " + ex);
                    }
                    if (eons != null) {
                        simPlmn = eons;
                    }
                    if (TextUtils.isEmpty(simPlmn)) {
                        log("No matched eons and No CPHS ONS");
                        if (plmn.equals(simOperatorNumeric)) {
                            log("Home PLMN, get CPHS ons");
                            simPlmn = simRecords != null ? simRecords.getSIMCPHSOns() : SpnOverride.MVNO_TYPE_NONE;
                        }
                        if (TextUtils.isEmpty(simPlmn) && plmn.equals(strNumPlmn)) {
                            simPlmn = getOemSpn();
                        }
                    }
                    pnnEons[0] = simPlmn;
                    pnnEons[1] = eons;
                }
                if ("46605".equals(plmn) && "46692".equals(simCardMccMnc)) {
                    pnnEons[0] = "GT 4G";
                    pnnEons[1] = "GT 4G";
                }
                if ("502153".equals(plmn) || "50211".equals(plmn)) {
                    pnnEons[0] = "unifi";
                    pnnEons[1] = "unifi";
                } else if (!(plmn == null || !plmn.equals("42403") || simOperatorNumeric == null || !simOperatorNumeric.equals("42403") || TextUtils.isEmpty(spn))) {
                    pnnEons[0] = spn;
                    pnnEons[1] = spn;
                }
            } else if (OppoGsmServiceStateTracker.isOperatorCheck(simCardMccMnc)) {
                if (DBG) {
                    log("getPnnFromEons isOperatorCheck");
                }
                if (OppoGsmServiceStateTracker.isVodafoneHomePlmn(plmn, simCardMccMnc)) {
                    if (!TextUtils.isEmpty(spn)) {
                        pnnEons[0] = spn;
                        pnnEons[1] = spn;
                        if (DBG) {
                            log("getPnnFromEons 1 pnnEons[0]=" + pnnEons[0] + "; pnnEons[1]=" + pnnEons[1]);
                        }
                    } else if (!(simRecords == null || simRecords.getEFpnnNetworkNames(0) == null)) {
                        pnnEons[0] = simRecords.getEFpnnNetworkNames(0).sFullName;
                        pnnEons[1] = simRecords.getEFpnnNetworkNames(0).sFullName;
                        if (DBG) {
                            log("getPnnFromEons 2 pnnEons[0]=" + pnnEons[0] + "; pnnEons[1]=" + pnnEons[1]);
                        }
                    }
                } else if (!(simRecords == null || simRecords.getEFpnnNetworkNames(1) == null || !plmn.equals("50502"))) {
                    pnnEons[0] = simRecords.getEFpnnNetworkNames(1).sFullName;
                    pnnEons[1] = simRecords.getEFpnnNetworkNames(1).sFullName;
                    if (DBG) {
                        log("getPnnFromEons 3 pnnEons[0]=" + pnnEons[0] + "; pnnEons[1]=" + pnnEons[1]);
                    }
                }
            } else if (!TextUtils.isEmpty(spn) && (((spn.equalsIgnoreCase("Virgin") || spn.equalsIgnoreCase("YES OPTUS")) && plmn.equals("50502")) || ((spn.equalsIgnoreCase("TeleChoice") || spn.equalsIgnoreCase("Telstra")) && plmn.equals("50501")))) {
                pnnEons[0] = spn;
                pnnEons[1] = spn;
                if (DBG) {
                    log("getPnnFromEons 4 pnnEons[0]=" + pnnEons[0] + "; pnnEons[1]=" + pnnEons[1]);
                }
            }
            log("getPnnFromEons simPlmn = " + simPlmn + "; eons = " + eons);
        }
        return pnnEons;
    }

    public boolean isHPlmn(String plmn) {
        String mccmnc = getSIMOperatorNumeric();
        if (plmn == null) {
            return false;
        }
        if (mccmnc == null || mccmnc.equals(SpnOverride.MVNO_TYPE_NONE)) {
            log("isHPlmn getSIMOperatorNumeric error: " + mccmnc);
            return false;
        } else if (plmn.equals(mccmnc)) {
            return true;
        } else {
            return plmn.length() == 5 && mccmnc.length() == 6 && plmn.equals(mccmnc.substring(0, 5));
        }
    }

    private void startResetNetworkStatusAlarm(int phoneId) {
        log("start Reset Alarm Exact start");
        int delayInMs = SystemProperties.getInt(RegionLockConstant.PERSIST_LOCK_TIME, 3600000);
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
        log("Davis,oppoSetPowerRadioOff");
        if (this.oppoNeedSetRadio) {
            this.oppoNeedSetRadio = false;
            Intent intent = new Intent(RegionLockConstant.ACTION_NETWORK_LOCK);
            intent.putExtra(RegionLockConstant.NETLOCK_STATUS, "1");
            sendBroadCastChangedNetlockStatus(intent);
        }
        SubscriptionManager.deactivateSubId(this.mPhone.getSubId());
    }

    private void sendBroadCastChangedNetlockStatus(Intent intent) {
        this.mPhone.getContext().sendBroadcast(intent);
    }

    private boolean oppoIsTestCard() {
        if (!this.mPhone.is_test_card()) {
            return false;
        }
        log("oppoIsSpecialStatus-->testing simcard");
        return true;
    }

    private void onPsNetworkStateChangeResult(AsyncResult ar) {
        if (ar != null) {
            int dataRegState = -1;
            if (ar.exception != null || ar.result == null) {
                loge("isPsRegistered exception");
            } else {
                dataRegState = getRegStateFromHalRegState(ar.result.regState);
            }
            if (dataRegState == 0) {
                this.mIsPsReg = true;
            } else {
                this.mIsPsReg = false;
            }
        }
    }

    public boolean isPsRegistered() {
        if (DBG) {
            log("isPsRegistered: " + this.mIsPsReg);
        }
        return this.mIsPsReg;
    }

    public CellLocation oppoGetCTLteCellLocation() {
        List<CellInfo> result = getAllCellInfo(null);
        log("guix oppoGetCTLteCellLocation(): result =" + result);
        if (result != null) {
            GsmCellLocation cellLocOther = new GsmCellLocation();
            for (CellInfo ci : result) {
                if (ci instanceof CellInfoGsm) {
                    android.telephony.CellIdentityGsm cellIdentityGsm = ((CellInfoGsm) ci).getCellIdentity();
                    cellLocOther.setLacAndCid(cellIdentityGsm.getLac(), cellIdentityGsm.getCid());
                    cellLocOther.setPsc(cellIdentityGsm.getPsc());
                    log("guix getCellLocation(): X ret GSM info=" + cellLocOther);
                    return cellLocOther;
                } else if (ci instanceof CellInfoWcdma) {
                    android.telephony.CellIdentityWcdma cellIdentityWcdma = ((CellInfoWcdma) ci).getCellIdentity();
                    cellLocOther.setLacAndCid(cellIdentityWcdma.getLac(), cellIdentityWcdma.getCid());
                    cellLocOther.setPsc(cellIdentityWcdma.getPsc());
                    log("guix getCellLocation(): X ret WCDMA info=" + cellLocOther);
                    return cellLocOther;
                } else if ((ci instanceof CellInfoLte) && (cellLocOther.getLac() < 0 || cellLocOther.getCid() < 0)) {
                    android.telephony.CellIdentityLte cellIdentityLte = ((CellInfoLte) ci).getCellIdentity();
                    if (!(cellIdentityLte.getTac() == Integer.MAX_VALUE || cellIdentityLte.getCi() == Integer.MAX_VALUE)) {
                        cellLocOther.setLacAndCid(cellIdentityLte.getTac(), cellIdentityLte.getCi());
                        cellLocOther.setPsc(0);
                        log("guix getCellLocation(): possible LTE cellLocOther=" + cellLocOther);
                        return cellLocOther;
                    }
                }
            }
            if (DBG) {
                log("guix getCellLocation(): X ret best answer cellLocOther=" + cellLocOther);
            }
            return cellLocOther;
        }
        if (DBG) {
            log("guix getCellLocation(): X empty mCellLoc and CellInfo mCellLoc=" + this.mCellLoc);
        }
        return this.mCellLoc;
    }

    public static int getUseNtpType() {
        return SystemProperties.getInt(PROPERTY_USENTP_TYPE, 1);
    }

    public static long getUseNtpInterval() {
        return SystemProperties.getLong(PROPERTY_USENTP_INTERVAL, NITZ_NTP_INTERVAL_OEM);
    }

    public int isUseNtptime(long time) {
        try {
            if (!mIsUseNtpTime || this.mPhone.getContext().getPackageManager().hasSystemFeature("oppo.cmcc.test") || this.mPhone.getContext().getPackageManager().hasSystemFeature("oppo.cu.test") || this.mPhone.getContext().getPackageManager().hasSystemFeature("oppo.ct.test")) {
                return 0;
            }
            if (this.mConnectivityManager == null) {
                this.mConnectivityManager = (ConnectivityManager) this.mPhone.getContext().getSystemService("connectivity");
            }
            if (this.mConnectivityManager != null && this.mConnectivityManager.hasCache()) {
                long currentNtpTime = this.mConnectivityManager.getCurrentTimeMillis();
                if (DBG) {
                    log("time = " + time + ", mConnectivityManager.getCurrentTimeMillis() = " + currentNtpTime);
                    log("mUseNtpInterval = " + mUseNtpInterval);
                }
                if (mUseNtpInterval == 0 || Math.abs(time - currentNtpTime) < mUseNtpInterval) {
                    return 2;
                }
                return 1;
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUseNtpTime(String name, String value) {
        if (value != null && name != null) {
            if (TAG_ENABLE.equals(name)) {
                if (value.equals("1")) {
                    mIsUseNtpTime = true;
                    SystemProperties.set(PROPERTY_USENTP_TYPE, "1");
                } else {
                    mIsUseNtpTime = false;
                    SystemProperties.set(PROPERTY_USENTP_TYPE, "0");
                }
            } else if (TAG_INTERVAL.equals(name)) {
                mUseNtpInterval = NITZ_NTP_INTERVAL_OEM_SECOND * Long.parseLong(value);
                SystemProperties.set(PROPERTY_USENTP_INTERVAL, SpnOverride.MVNO_TYPE_NONE + mUseNtpInterval);
            }
        }
    }
}
