package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
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
import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.BaseBundle;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.WorkSource;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.CarrierConfigManager;
import android.telephony.CellIdentity;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityTdscdma;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.DataSpecificRegistrationInfo;
import android.telephony.NetworkRegistrationInfo;
import android.telephony.PhysicalChannelConfig;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.VoiceSpecificRegistrationInfo;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.LocalLog;
import android.util.Pair;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.StatsLog;
import android.util.TimestampedValue;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager;
import com.android.internal.telephony.cdma.EriManager;
import com.android.internal.telephony.cdnr.CarrierDisplayNameData;
import com.android.internal.telephony.cdnr.CarrierDisplayNameResolver;
import com.android.internal.telephony.dataconnection.DataConnection;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.dataconnection.TransportManager;
import com.android.internal.telephony.imsphone.ImsPhone;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.nano.TelephonyProto;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccCardStatus;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.RuimRecords;
import com.android.internal.telephony.uicc.SIMRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.uicc.UiccProfile;
import com.android.internal.telephony.util.NotificationChannelController;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.IndentingPrintWriter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ServiceStateTracker extends AbstractServiceStateTracker {
    protected static final String ACTION_RADIO_OFF = "android.intent.action.ACTION_RADIO_OFF";
    public static final int CARRIER_NAME_DISPLAY_BITMASK_SHOW_PLMN = 2;
    public static final int CARRIER_NAME_DISPLAY_BITMASK_SHOW_SPN = 1;
    private static final long CELL_INFO_LIST_QUERY_TIMEOUT = 2000;
    public static final int CS_DISABLED = 1004;
    public static final int CS_EMERGENCY_ENABLED = 1006;
    public static final int CS_ENABLED = 1003;
    public static final int CS_NORMAL_ENABLED = 1005;
    public static final int CS_NOTIFICATION = 999;
    public static final int CS_REJECT_CAUSE_DISABLED = 2002;
    public static final int CS_REJECT_CAUSE_ENABLED = 2001;
    public static final int CS_REJECT_CAUSE_NOTIFICATION = 111;
    static final boolean DBG = true;
    public static final int DEFAULT_GPRS_CHECK_PERIOD_MILLIS = 60000;
    public static final String DEFAULT_MNC = "00";
    protected static final int EVENT_ALL_DATA_DISCONNECTED = 49;
    protected static final int EVENT_CARRIER_CONFIG_CHANGED = 57;
    protected static final int EVENT_CDMA_PRL_VERSION_CHANGED = 40;
    protected static final int EVENT_CDMA_SUBSCRIPTION_SOURCE_CHANGED = 39;
    protected static final int EVENT_CELL_LOCATION_RESPONSE = 56;
    protected static final int EVENT_CHANGE_IMS_STATE = 45;
    protected static final int EVENT_CHECK_REPORT_GPRS = 22;
    protected static final int EVENT_GET_CELL_INFO_LIST = 43;
    protected static final int EVENT_GET_LOC_DONE = 15;
    protected static final int EVENT_GET_PREFERRED_NETWORK_TYPE = 19;
    protected static final int EVENT_GET_SIGNAL_STRENGTH = 3;
    public static final int EVENT_ICC_CHANGED = 42;
    protected static final int EVENT_IMS_CAPABILITY_CHANGED = 48;
    protected static final int EVENT_IMS_SERVICE_STATE_CHANGED = 53;
    protected static final int EVENT_IMS_STATE_CHANGED = 46;
    protected static final int EVENT_IMS_STATE_DONE = 47;
    protected static final int EVENT_LOCATION_UPDATES_ENABLED = 18;
    protected static final int EVENT_NETWORK_STATE_CHANGED = 2;
    protected static final int EVENT_NITZ_TIME = 11;
    protected static final int EVENT_NV_READY = 35;
    protected static final int EVENT_OTA_PROVISION_STATUS_CHANGE = 37;
    protected static final int EVENT_PHONE_TYPE_SWITCHED = 50;
    protected static final int EVENT_PHYSICAL_CHANNEL_CONFIG = 55;
    protected static final int EVENT_POLL_SIGNAL_STRENGTH = 10;
    protected static final int EVENT_POLL_STATE_CDMA_SUBSCRIPTION = 34;
    protected static final int EVENT_POLL_STATE_CS_CELLULAR_REGISTRATION = 4;
    protected static final int EVENT_POLL_STATE_NETWORK_SELECTION_MODE = 14;
    protected static final int EVENT_POLL_STATE_OPERATOR = 7;
    protected static final int EVENT_POLL_STATE_PS_CELLULAR_REGISTRATION = 5;
    protected static final int EVENT_POLL_STATE_PS_IWLAN_REGISTRATION = 6;
    protected static final int EVENT_RADIO_ON = 41;
    protected static final int EVENT_RADIO_POWER_FROM_CARRIER = 51;
    protected static final int EVENT_RADIO_POWER_OFF_DONE = 54;
    protected static final int EVENT_RADIO_STATE_CHANGED = 1;
    protected static final int EVENT_RESET_PREFERRED_NETWORK_TYPE = 21;
    protected static final int EVENT_RESTRICTED_STATE_CHANGED = 23;
    protected static final int EVENT_RUIM_READY = 26;
    protected static final int EVENT_RUIM_RECORDS_LOADED = 27;
    protected static final int EVENT_SET_PREFERRED_NETWORK_TYPE = 20;
    protected static final int EVENT_SET_RADIO_POWER_OFF = 38;
    protected static final int EVENT_SIGNAL_STRENGTH_UPDATE = 12;
    protected static final int EVENT_SIM_READY = 17;
    protected static final int EVENT_SIM_RECORDS_LOADED = 16;
    protected static final int EVENT_UNSOL_CELL_INFO_LIST = 44;
    private static final int INVALID_LTE_EARFCN = -1;
    public static final String INVALID_MCC = "000";
    static final String LOG_TAG = "SST";
    public static final int MS_PER_HOUR = 3600000;
    private static final int POLL_PERIOD_MILLIS = 20000;
    protected static final String PROP_FORCE_ROAMING = "telephony.test.forceRoaming";
    public static final int PS_DISABLED = 1002;
    public static final int PS_ENABLED = 1001;
    public static final int PS_NOTIFICATION = 888;
    protected static final String REGISTRATION_DENIED_AUTH = "Authentication Failure";
    protected static final String REGISTRATION_DENIED_GEN = "General";
    public static final String UNACTIVATED_MIN2_VALUE = "000000";
    public static final String UNACTIVATED_MIN_VALUE = "1111110111";
    private static final boolean VDBG = false;
    protected boolean mAlarmSwitch = false;
    private final LocalLog mAttachLog = new LocalLog(10);
    protected SparseArray<RegistrantList> mAttachedRegistrants = new SparseArray<>();
    private CarrierServiceStateTracker mCSST;
    private RegistrantList mCdmaForSubscriptionInfoReadyRegistrants = new RegistrantList();
    protected CdmaSubscriptionSourceManager mCdmaSSM;
    protected CarrierDisplayNameResolver mCdnr;
    private final LocalLog mCdnrLogs = new LocalLog(64);
    protected CellIdentity mCellIdentity;
    private int mCellInfoMinIntervalMs = TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_MIP_FA_REASON_UNSPECIFIED;
    @UnsupportedAppUsage
    protected CommandsInterface mCi;
    @UnsupportedAppUsage
    private final ContentResolver mCr;
    @UnsupportedAppUsage
    protected String mCurDataSpn = null;
    @UnsupportedAppUsage
    protected String mCurPlmn = null;
    @UnsupportedAppUsage
    protected boolean mCurShowPlmn = false;
    @UnsupportedAppUsage
    protected boolean mCurShowSpn = false;
    @UnsupportedAppUsage
    protected String mCurSpn = null;
    private String mCurrentCarrier = null;
    private int mCurrentOtaspMode = 0;
    private SparseArray<RegistrantList> mDataRegStateOrRatChangedRegistrants = new SparseArray<>();
    protected boolean mDataRoaming = false;
    @UnsupportedAppUsage
    protected RegistrantList mDataRoamingOffRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    protected RegistrantList mDataRoamingOnRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    protected int mDefaultRoamingIndicator;
    @UnsupportedAppUsage
    protected boolean mDesiredPowerState;
    protected SparseArray<RegistrantList> mDetachedRegistrants = new SparseArray<>();
    @UnsupportedAppUsage
    protected boolean mDeviceShuttingDown = false;
    private boolean mDontPollSignalStrength = false;
    private ArrayList<Pair<Integer, Integer>> mEarfcnPairListForRsrpBoost = null;
    @UnsupportedAppUsage
    protected boolean mEmergencyOnly = false;
    protected final EriManager mEriManager;
    protected boolean mGsmRoaming = false;
    protected HbpcdUtils mHbpcdUtils = null;
    private int[] mHomeNetworkId = null;
    private int[] mHomeSystemId = null;
    @UnsupportedAppUsage
    protected IccRecords mIccRecords = null;
    private RegistrantList mImsCapabilityChangedRegistrants = new RegistrantList();
    private boolean mImsRegistered = false;
    protected boolean mImsRegistrationOnOff = false;
    @UnsupportedAppUsage
    protected BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.ServiceStateTracker.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if (!intent.getAction().equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                ServiceStateTracker serviceStateTracker = ServiceStateTracker.this;
                serviceStateTracker.log("onReceive intent: " + intent);
                if (intent.getAction().equals("android.intent.action.LOCALE_CHANGED")) {
                    ServiceStateTracker.this.updateSpnDisplay();
                } else if (intent.getAction().equals(ServiceStateTracker.ACTION_RADIO_OFF)) {
                    ServiceStateTracker serviceStateTracker2 = ServiceStateTracker.this;
                    serviceStateTracker2.mAlarmSwitch = false;
                    serviceStateTracker2.powerOffRadioSafely();
                }
            } else if (intent.getExtras().getInt("android.telephony.extra.SLOT_INDEX") == ServiceStateTracker.this.mPhone.getPhoneId()) {
                ServiceStateTracker.this.sendEmptyMessage(57);
            }
        }
    };
    private boolean mIsEriTextLoaded = false;
    protected boolean mIsInPrl;
    protected boolean mIsMinInfoReady = false;
    private boolean mIsPendingCellInfoRequest = false;
    protected boolean mIsSimReady = false;
    @UnsupportedAppUsage
    protected boolean mIsSubscriptionFromRuim = false;
    protected List<CellInfo> mLastCellInfoList = null;
    protected long mLastCellInfoReqTime;
    protected List<PhysicalChannelConfig> mLastPhysicalChannelConfigList = null;
    private SignalStrength mLastSignalStrength = null;
    protected final LocaleTracker mLocaleTracker;
    private int mLteRsrpBoost = 0;
    private final Object mLteRsrpBoostLock = new Object();
    @UnsupportedAppUsage
    protected int mMaxDataCalls = 1;
    protected String mMdn;
    protected String mMin;
    @UnsupportedAppUsage
    protected RegistrantList mNetworkAttachedRegistrants = new RegistrantList();
    protected RegistrantList mNetworkDetachedRegistrants = new RegistrantList();
    protected CellIdentity mNewCellIdentity;
    @UnsupportedAppUsage
    protected int mNewMaxDataCalls = 1;
    @UnsupportedAppUsage
    protected int mNewReasonDataDenied = -1;
    protected int mNewRejectCode;
    @UnsupportedAppUsage
    protected ServiceState mNewSS;
    protected final NitzStateMachine mNitzState;
    private Notification mNotification;
    @UnsupportedAppUsage
    private final SstSubscriptionsChangedListener mOnSubscriptionsChangedListener = new SstSubscriptionsChangedListener();
    protected Pattern mOperatorNameStringPattern;
    private List<Message> mPendingCellInfoRequests = new LinkedList();
    protected boolean mPendingRadioPowerOffAfterDataOff = false;
    protected int mPendingRadioPowerOffAfterDataOffTag = 0;
    @UnsupportedAppUsage
    protected final GsmCdmaPhone mPhone;
    private final LocalLog mPhoneTypeLog = new LocalLog(10);
    @VisibleForTesting
    public int[] mPollingContext;
    protected boolean mPowerOffDelayNeed = true;
    @UnsupportedAppUsage
    private int mPreferredNetworkType;
    protected int mPrevSubId = -1;
    protected String mPrlVersion;
    protected RegistrantList mPsRestrictDisabledRegistrants = new RegistrantList();
    private RegistrantList mPsRestrictEnabledRegistrants = new RegistrantList();
    public boolean mRadioDisabledByCarrier = false;
    protected PendingIntent mRadioOffIntent = null;
    public final LocalLog mRadioPowerLog = new LocalLog(20);
    private final LocalLog mRatLog = new LocalLog(20);
    protected final RatRatcheter mRatRatcheter;
    @UnsupportedAppUsage
    protected int mReasonDataDenied = -1;
    protected final SparseArray<NetworkRegistrationManager> mRegStateManagers = new SparseArray<>();
    protected String mRegistrationDeniedReason;
    protected int mRegistrationState = -1;
    protected int mRejectCode;
    @UnsupportedAppUsage
    protected boolean mReportedGprsNoReg;
    public RestrictedState mRestrictedState;
    @UnsupportedAppUsage
    protected int mRoamingIndicator;
    private final LocalLog mRoamingLog = new LocalLog(10);
    @UnsupportedAppUsage
    public ServiceState mSS;
    @UnsupportedAppUsage
    protected SignalStrength mSignalStrength = new SignalStrength();
    @UnsupportedAppUsage
    protected boolean mSpnUpdatePending = false;
    @UnsupportedAppUsage
    protected boolean mStartedGprsRegCheck;
    @UnsupportedAppUsage
    @VisibleForTesting
    public int mSubId = -1;
    @UnsupportedAppUsage
    protected SubscriptionController mSubscriptionController;
    @UnsupportedAppUsage
    private SubscriptionManager mSubscriptionManager;
    protected final TransportManager mTransportManager;
    @UnsupportedAppUsage
    protected UiccCardApplication mUiccApplcation = null;
    @UnsupportedAppUsage
    protected UiccController mUiccController = null;
    private boolean mVoiceCapable;
    private RegistrantList mVoiceRegStateOrRatChangedRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    protected RegistrantList mVoiceRoamingOffRegistrants = new RegistrantList();
    @UnsupportedAppUsage
    protected RegistrantList mVoiceRoamingOnRegistrants = new RegistrantList();
    private boolean mWantContinuousLocationUpdates;
    private boolean mWantSingleLocationUpdate;

    @Retention(RetentionPolicy.SOURCE)
    public @interface CarrierNameDisplayBitmask {
    }

    private class SstSubscriptionsChangedListener extends SubscriptionManager.OnSubscriptionsChangedListener {
        public final AtomicInteger mPreviousSubId;

        private SstSubscriptionsChangedListener() {
            this.mPreviousSubId = new AtomicInteger(-1);
        }

        public void onSubscriptionsChanged() {
            ServiceStateTracker.this.log("SubscriptionListener.onSubscriptionInfoChanged");
            int subId = ServiceStateTracker.this.mPhone.getSubId();
            ServiceStateTracker.this.mPrevSubId = this.mPreviousSubId.get();
            if (this.mPreviousSubId.getAndSet(subId) != subId) {
                if (SubscriptionManager.isValidSubscriptionId(subId)) {
                    Context context = ServiceStateTracker.this.mPhone.getContext();
                    ServiceStateTracker.this.mPhone.notifyPhoneStateChanged();
                    ServiceStateTracker.this.mPhone.notifyCallForwardingIndicator();
                    ServiceStateTracker.this.mPhone.sendSubscriptionSettings(!context.getResources().getBoolean(17891616));
                    ServiceStateTracker.this.mPhone.setSystemProperty("gsm.network.type", ServiceState.rilRadioTechnologyToString(ServiceStateTracker.this.mSS.getRilDataRadioTechnology()));
                    if (ServiceStateTracker.this.mSpnUpdatePending) {
                        ServiceStateTracker.this.mSubscriptionController.setPlmnSpn(ServiceStateTracker.this.mPhone.getPhoneId(), ServiceStateTracker.this.mCurShowPlmn, ServiceStateTracker.this.mCurPlmn, ServiceStateTracker.this.mCurShowSpn, ServiceStateTracker.this.mCurSpn);
                        ServiceStateTracker.this.mSpnUpdatePending = false;
                    }
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
                    String oldNetworkSelection = sp.getString(Phone.NETWORK_SELECTION_KEY, PhoneConfigurationManager.SSSS);
                    String oldNetworkSelectionName = sp.getString(Phone.NETWORK_SELECTION_NAME_KEY, PhoneConfigurationManager.SSSS);
                    String oldNetworkSelectionShort = sp.getString(Phone.NETWORK_SELECTION_SHORT_KEY, PhoneConfigurationManager.SSSS);
                    if (!TextUtils.isEmpty(oldNetworkSelection) || !TextUtils.isEmpty(oldNetworkSelectionName) || !TextUtils.isEmpty(oldNetworkSelectionShort)) {
                        SharedPreferences.Editor editor = sp.edit();
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
                ServiceStateTracker.this.onSubscriptionsChangedForOppo(subId);
            }
        }
    }

    public ServiceStateTracker(GsmCdmaPhone phone, CommandsInterface ci) {
        this.mNitzState = TelephonyComponentFactory.getInstance().inject(NitzStateMachine.class.getName()).makeNitzStateMachine(phone);
        this.mPhone = phone;
        this.mCi = ci;
        this.mCdnr = new CarrierDisplayNameResolver(this.mPhone);
        this.mEriManager = TelephonyComponentFactory.getInstance().inject(EriManager.class.getName()).makeEriManager(this.mPhone, 0);
        this.mRatRatcheter = new RatRatcheter(this.mPhone);
        this.mVoiceCapable = this.mPhone.getContext().getResources().getBoolean(17891571);
        this.mUiccController = UiccController.getInstance();
        this.mUiccController.registerForIccChanged(this, 42, null);
        this.mCi.setOnSignalStrengthUpdate(this, 12, null);
        this.mCi.registerForCellInfoList(this, 44, null);
        this.mCi.registerForPhysicalChannelConfiguration(this, 55, null);
        this.mSubscriptionController = SubscriptionController.getInstance();
        this.mSubscriptionManager = SubscriptionManager.from(phone.getContext());
        this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        this.mRestrictedState = new RestrictedState();
        this.mTransportManager = this.mPhone.getTransportManager();
        int[] availableTransports = this.mTransportManager.getAvailableTransports();
        for (int transportType : availableTransports) {
            this.mRegStateManagers.append(transportType, new NetworkRegistrationManager(transportType, phone));
            this.mRegStateManagers.get(transportType).registerForNetworkRegistrationInfoChanged(this, 2, null);
        }
        this.mLocaleTracker = TelephonyComponentFactory.getInstance().inject(LocaleTracker.class.getName()).makeLocaleTracker(this.mPhone, this.mNitzState, getLooper());
        this.mCi.registerForImsNetworkStateChanged(this, 46, null);
        this.mCi.registerForRadioStateChanged(this, 1, null);
        this.mCi.setOnNITZTime(this, 11, null);
        this.mCr = phone.getContext().getContentResolver();
        int airplaneMode = Settings.Global.getInt(this.mCr, "airplane_mode_on", 0);
        int enableCellularOnBoot = Settings.Global.getInt(this.mCr, "enable_cellular_on_boot", 1);
        this.mDesiredPowerState = enableCellularOnBoot > 0 && airplaneMode <= 0;
        this.mRadioPowerLog.log("init : airplane mode = " + airplaneMode + " enableCellularOnBoot = " + enableCellularOnBoot);
        setSignalStrengthDefaultValues();
        this.mPhone.getCarrierActionAgent().registerForCarrierAction(1, this, 51, null, false);
        Context context = this.mPhone.getContext();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.LOCALE_CHANGED");
        context.registerReceiver(this.mIntentReceiver, filter);
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(ACTION_RADIO_OFF);
        context.registerReceiver(this.mIntentReceiver, filter2);
        IntentFilter filter3 = new IntentFilter();
        filter3.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        context.registerReceiver(this.mIntentReceiver, filter3);
        this.mPhone.notifyOtaspChanged(0);
        this.mCi.setOnRestrictedStateChanged(this, 23, null);
        updatePhoneType();
        this.mCSST = new CarrierServiceStateTracker(phone, this);
        registerForNetworkAttached(this.mCSST, 101, null);
        registerForNetworkDetached(this.mCSST, 102, null);
        registerForDataConnectionAttached(1, this.mCSST, AbstractCallTracker.EVENT_ACCEPT_COMPLETE, null);
        registerForDataConnectionDetached(1, this.mCSST, 104, null);
        registerForImsCapabilityChanged(this.mCSST, 105, null);
        this.mReference = (IOppoServiceStateTracker) OppoTelephonyFactory.getInstance().getFeature(IOppoServiceStateTracker.DEFAULT, this, phone);
    }

    @VisibleForTesting
    public void updatePhoneType() {
        NetworkRegistrationInfo nrs;
        ServiceState serviceState = this.mSS;
        if (serviceState != null && serviceState.getVoiceRoaming()) {
            this.mVoiceRoamingOffRegistrants.notifyRegistrants();
        }
        ServiceState serviceState2 = this.mSS;
        if (serviceState2 != null && serviceState2.getDataRoaming()) {
            this.mDataRoamingOffRegistrants.notifyRegistrants();
        }
        ServiceState serviceState3 = this.mSS;
        if (serviceState3 != null && serviceState3.getVoiceRegState() == 0) {
            this.mNetworkDetachedRegistrants.notifyRegistrants();
        }
        int[] availableTransports = this.mTransportManager.getAvailableTransports();
        for (int transport : availableTransports) {
            ServiceState serviceState4 = this.mSS;
            if (!(serviceState4 == null || (nrs = serviceState4.getNetworkRegistrationInfo(2, transport)) == null || !nrs.isInService() || this.mDetachedRegistrants.get(transport) == null)) {
                this.mDetachedRegistrants.get(transport).notifyRegistrants();
            }
        }
        this.mSS = new ServiceState();
        this.mSS.setStateOutOfService();
        this.mNewSS = new ServiceState();
        this.mNewSS.setStateOutOfService();
        this.mLastCellInfoReqTime = 0;
        this.mLastCellInfoList = null;
        this.mSignalStrength = new SignalStrength();
        this.mStartedGprsRegCheck = false;
        this.mReportedGprsNoReg = false;
        this.mMdn = null;
        this.mMin = null;
        this.mPrlVersion = null;
        this.mIsMinInfoReady = false;
        this.mNitzState.handleNetworkCountryCodeUnavailable();
        this.mCellIdentity = null;
        this.mNewCellIdentity = null;
        cancelPollState();
        if (this.mPhone.isPhoneTypeGsm()) {
            CdmaSubscriptionSourceManager cdmaSubscriptionSourceManager = this.mCdmaSSM;
            if (cdmaSubscriptionSourceManager != null) {
                cdmaSubscriptionSourceManager.dispose(this);
            }
            this.mCi.unregisterForCdmaPrlChanged(this);
            this.mCi.unregisterForCdmaOtaProvision(this);
            this.mPhone.unregisterForSimRecordsLoaded(this);
        } else {
            this.mPhone.registerForSimRecordsLoaded(this, 16, null);
            this.mCdmaSSM = CdmaSubscriptionSourceManager.getInstance(this.mPhone.getContext(), this.mCi, this, 39, null);
            this.mIsSubscriptionFromRuim = this.mCdmaSSM.getCdmaSubscriptionSource() == 0;
            this.mCi.registerForCdmaPrlChanged(this, 40, null);
            this.mCi.registerForCdmaOtaProvision(this, 37, null);
            this.mHbpcdUtils = new HbpcdUtils(this.mPhone.getContext());
            updateOtaspState();
        }
        onUpdateIccAvailability();
        this.mPhone.setSystemProperty("gsm.network.type", ServiceState.rilRadioTechnologyToString(0));
        this.mCi.getSignalStrength(obtainMessage(3));
        sendMessage(obtainMessage(50));
        logPhoneTypeChange();
        notifyVoiceRegStateRilRadioTechnologyChanged();
        for (int transport2 : this.mTransportManager.getAvailableTransports()) {
            notifyDataRegStateRilRadioTechnologyChanged(transport2);
        }
    }

    @VisibleForTesting
    public void requestShutdown() {
        if (!this.mDeviceShuttingDown) {
            this.mDeviceShuttingDown = true;
            this.mDesiredPowerState = false;
            setPowerStateToDesired();
        }
    }

    public void dispose() {
        this.mCi.unSetOnSignalStrengthUpdate(this);
        this.mUiccController.unregisterForIccChanged(this);
        this.mCi.unregisterForCellInfoList(this);
        this.mCi.unregisterForPhysicalChannelConfiguration(this);
        this.mSubscriptionManager.removeOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        this.mCi.unregisterForImsNetworkStateChanged(this);
        this.mPhone.getCarrierActionAgent().unregisterForCarrierAction(this, 1);
        CarrierServiceStateTracker carrierServiceStateTracker = this.mCSST;
        if (carrierServiceStateTracker != null) {
            carrierServiceStateTracker.dispose();
            this.mCSST = null;
        }
    }

    @UnsupportedAppUsage
    public boolean getDesiredPowerState() {
        return this.mDesiredPowerState;
    }

    public boolean getPowerStateFromCarrier() {
        return !this.mRadioDisabledByCarrier;
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public boolean notifySignalStrength() {
        boolean notified = false;
        if (this.mSignalStrength.equals(this.mLastSignalStrength)) {
            return false;
        }
        try {
            this.mPhone.notifySignalStrength();
            notified = true;
            this.mLastSignalStrength = this.mSignalStrength;
            return true;
        } catch (NullPointerException ex) {
            loge("updateSignalStrength() Phone already destroyed: " + ex + "SignalStrength not notified");
            return notified;
        }
    }

    /* access modifiers changed from: protected */
    public void notifyVoiceRegStateRilRadioTechnologyChanged() {
        int rat = this.mSS.getRilVoiceRadioTechnology();
        int vrs = this.mSS.getVoiceRegState();
        log("notifyVoiceRegStateRilRadioTechnologyChanged: vrs=" + vrs + " rat=" + rat);
        this.mVoiceRegStateOrRatChangedRegistrants.notifyResult(new Pair(Integer.valueOf(vrs), Integer.valueOf(rat)));
    }

    /* access modifiers changed from: protected */
    public void notifyDataRegStateRilRadioTechnologyChanged(int transport) {
        NetworkRegistrationInfo nrs = this.mSS.getNetworkRegistrationInfo(2, transport);
        if (nrs != null) {
            int rat = ServiceState.networkTypeToRilRadioTechnology(nrs.getAccessNetworkTechnology());
            int drs = regCodeToServiceState(nrs.getRegistrationState());
            log("notifyDataRegStateRilRadioTechnologyChanged: drs=" + drs + " rat=" + rat);
            RegistrantList registrantList = this.mDataRegStateOrRatChangedRegistrants.get(transport);
            if (registrantList != null) {
                registrantList.notifyResult(new Pair(Integer.valueOf(drs), Integer.valueOf(rat)));
            }
        }
        this.mPhone.setSystemProperty("gsm.network.type", ServiceState.rilRadioTechnologyToString(this.mSS.getRilDataRadioTechnology()));
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void useDataRegStateForDataOnlyDevices() {
        if (!this.mVoiceCapable) {
            log("useDataRegStateForDataOnlyDevice: VoiceRegState=" + this.mNewSS.getVoiceRegState() + " DataRegState=" + this.mNewSS.getDataRegState());
            ServiceState serviceState = this.mNewSS;
            serviceState.setVoiceRegState(serviceState.getDataRegState());
        }
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void updatePhoneObject() {
        if (this.mPhone.getContext().getResources().getBoolean(17891548)) {
            if (!(this.mSS.getVoiceRegState() == 0 || this.mSS.getVoiceRegState() == 2)) {
                log("updatePhoneObject: Ignore update");
            } else {
                this.mPhone.updatePhoneObject(this.mSS.getRilVoiceRadioTechnology());
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
        if (notifyNow && !this.mSS.getDataRoaming()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForDataRoamingOff(Handler h) {
        this.mDataRoamingOffRegistrants.remove(h);
    }

    @UnsupportedAppUsage
    public void reRegisterNetwork(Message onComplete) {
        this.mCi.getPreferredNetworkType(obtainMessage(19, onComplete));
    }

    public void setRadioPower(boolean power) {
        int mPhoneId = getPhoneId();
        Rlog.d(LOG_TAG, "setRadioPower: mPhoneId :" + mPhoneId);
        if (mPhoneId >= 0 && mPhoneId < TelephonyManager.getDefault().getPhoneCount()) {
            Rlog.d(LOG_TAG, "setRadioPower: isUiccSlotForbid :" + OemConstant.isUiccSlotForbid(mPhoneId));
            if (OemConstant.isUiccSlotForbid(mPhoneId) && power) {
                Rlog.d(LOG_TAG, "setRadioPower: isUiccSlotForbid return");
                return;
            }
        }
        this.mDesiredPowerState = power;
        setPowerStateToDesired();
    }

    public void setRadioPowerFromCarrier(boolean enable) {
        this.mRadioDisabledByCarrier = !enable;
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

    /* access modifiers changed from: protected */
    public void disableSingleLocationUpdate() {
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

    /* access modifiers changed from: protected */
    public int getLteEarfcn(CellIdentity cellIdentity) {
        if (cellIdentity == null || cellIdentity.getType() != 3) {
            return -1;
        }
        return ((CellIdentityLte) cellIdentity).getEarfcn();
    }

    public void handleMessage(Message msg) {
        ServiceState serviceState;
        boolean z = false;
        switch (msg.what) {
            case 1:
            case 50:
                if (!this.mPhone.isPhoneTypeGsm() && this.mCi.getRadioState() == 1) {
                    handleCdmaSubscriptionSource(this.mCdmaSSM.getCdmaSubscriptionSource());
                    queueNextSignalStrengthPoll();
                }
                setPowerStateToDesired();
                modemTriggeredPollState();
                return;
            case 2:
                modemTriggeredPollState();
                return;
            case 3:
                if (this.mCi.getRadioState() == 1) {
                    onSignalStrengthResult((AsyncResult) msg.obj);
                    queueNextSignalStrengthPoll();
                    return;
                }
                return;
            case 4:
            case 5:
            case 6:
            case 7:
                handlePollStateResult(msg.what, (AsyncResult) msg.obj);
                return;
            case 8:
            case 9:
            case 13:
            case 24:
            case 25:
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
            case 33:
            case 36:
            case 41:
            case 52:
            default:
                log("Unhandled message with number: " + msg.what);
                return;
            case 10:
                this.mCi.getSignalStrength(obtainMessage(3));
                return;
            case 11:
                AsyncResult ar = (AsyncResult) msg.obj;
                setTimeFromNITZString((String) ((Object[]) ar.result)[0], ((Long) ((Object[]) ar.result)[1]).longValue());
                return;
            case 12:
                this.mDontPollSignalStrength = true;
                onSignalStrengthResult((AsyncResult) msg.obj);
                return;
            case 14:
                log("EVENT_POLL_STATE_NETWORK_SELECTION_MODE");
                AsyncResult ar2 = (AsyncResult) msg.obj;
                if (this.mPhone.isPhoneTypeGsm()) {
                    handlePollStateResult(msg.what, ar2);
                    return;
                } else if (ar2.exception != null || ar2.result == null) {
                    log("Unable to getNetworkSelectionMode");
                    return;
                } else if (((int[]) ar2.result)[0] == 1) {
                    this.mPhone.setNetworkSelectionModeAutomatic(null);
                    return;
                } else {
                    return;
                }
            case 15:
                AsyncResult ar3 = (AsyncResult) msg.obj;
                if (ar3.exception == null) {
                    CellIdentity cellIdentity = ((NetworkRegistrationInfo) ar3.result).getCellIdentity();
                    updateOperatorNameForCellIdentity(cellIdentity);
                    this.mCellIdentity = cellIdentity;
                    this.mPhone.notifyLocationChanged(getCellLocation());
                }
                disableSingleLocationUpdate();
                return;
            case 16:
                log("EVENT_SIM_RECORDS_LOADED: what=" + msg.what);
                updatePhoneObject();
                updateOtaspState();
                if (this.mPhone.isPhoneTypeGsm()) {
                    this.mCdnr.updateEfFromUsim((SIMRecords) this.mIccRecords);
                    updateSpnDisplay();
                    return;
                }
                return;
            case 17:
                ((SstSubscriptionsChangedListener) this.mOnSubscriptionsChangedListener).mPreviousSubId.set(-1);
                this.mPrevSubId = -1;
                this.mIsSimReady = true;
                pollState();
                queueNextSignalStrengthPoll();
                return;
            case 18:
                if (((AsyncResult) msg.obj).exception == null) {
                    this.mRegStateManagers.get(1).requestNetworkRegistrationInfo(1, obtainMessage(15, null));
                    return;
                }
                return;
            case 19:
                AsyncResult ar4 = (AsyncResult) msg.obj;
                if (ar4.exception == null) {
                    this.mPreferredNetworkType = ((int[]) ar4.result)[0];
                } else {
                    this.mPreferredNetworkType = 7;
                }
                this.mCi.setPreferredNetworkType(7, obtainMessage(20, ar4.userObj));
                return;
            case 20:
                this.mCi.setPreferredNetworkType(this.mPreferredNetworkType, obtainMessage(21, ((AsyncResult) msg.obj).userObj));
                return;
            case 21:
                AsyncResult ar5 = (AsyncResult) msg.obj;
                if (ar5.userObj != null) {
                    AsyncResult.forMessage((Message) ar5.userObj).exception = ar5.exception;
                    ((Message) ar5.userObj).sendToTarget();
                    return;
                }
                return;
            case 22:
                if (this.mPhone.isPhoneTypeGsm() && (serviceState = this.mSS) != null && !isGprsConsistent(serviceState.getDataRegState(), this.mSS.getVoiceRegState())) {
                    EventLog.writeEvent((int) EventLogTags.DATA_NETWORK_REGISTRATION_FAIL, this.mSS.getOperatorNumeric(), Integer.valueOf(getCidFromCellIdentity(this.mCellIdentity)));
                    this.mReportedGprsNoReg = true;
                }
                this.mStartedGprsRegCheck = false;
                return;
            case 23:
                if (this.mPhone.isPhoneTypeGsm()) {
                    log("EVENT_RESTRICTED_STATE_CHANGED");
                    onRestrictedStateChanged((AsyncResult) msg.obj);
                    return;
                }
                return;
            case 26:
                if (this.mPhone.getLteOnCdmaMode() == 1) {
                    log("Receive EVENT_RUIM_READY");
                    pollState();
                } else {
                    log("Receive EVENT_RUIM_READY and Send Request getCDMASubscription.");
                    getSubscriptionInfoAndStartPollingThreads();
                }
                this.mCi.getNetworkSelectionMode(obtainMessage(14));
                return;
            case 27:
                if (!this.mPhone.isPhoneTypeGsm()) {
                    log("EVENT_RUIM_RECORDS_LOADED: what=" + msg.what);
                    this.mCdnr.updateEfFromRuim((RuimRecords) this.mIccRecords);
                    updatePhoneObject();
                    if (this.mPhone.isPhoneTypeCdma()) {
                        updateSpnDisplay();
                        return;
                    }
                    RuimRecords ruim = (RuimRecords) this.mIccRecords;
                    if (ruim != null) {
                        this.mMdn = ruim.getMdn();
                        if (ruim.isProvisioned()) {
                            this.mMin = ruim.getMin();
                            parseSidNid(ruim.getSid(), ruim.getNid());
                            this.mPrlVersion = ruim.getPrlVersion();
                            this.mIsMinInfoReady = true;
                        }
                        updateOtaspState();
                        notifyCdmaSubscriptionInfoReady();
                    }
                    pollState();
                    return;
                }
                return;
            case 34:
                if (!this.mPhone.isPhoneTypeGsm()) {
                    AsyncResult ar6 = (AsyncResult) msg.obj;
                    if (ar6.exception == null) {
                        String[] cdmaSubscription = (String[]) ar6.result;
                        if (cdmaSubscription == null || cdmaSubscription.length < 5) {
                            log("GET_CDMA_SUBSCRIPTION: error parsing cdmaSubscription params num=" + cdmaSubscription.length);
                            return;
                        }
                        this.mMdn = cdmaSubscription[0];
                        parseSidNid(cdmaSubscription[1], cdmaSubscription[2]);
                        this.mMin = cdmaSubscription[3];
                        this.mPrlVersion = cdmaSubscription[4];
                        log("GET_CDMA_SUBSCRIPTION: MDN=" + this.mMdn);
                        this.mIsMinInfoReady = true;
                        updateOtaspState();
                        notifyCdmaSubscriptionInfoReady();
                        if (this.mIsSubscriptionFromRuim || this.mIccRecords == null) {
                            log("GET_CDMA_SUBSCRIPTION either mIccRecords is null or NV type device - not setting Imsi in mIccRecords");
                            return;
                        }
                        log("GET_CDMA_SUBSCRIPTION set imsi in mIccRecords");
                        this.mIccRecords.setImsi(getImsi());
                        return;
                    }
                    return;
                }
                return;
            case 35:
                updatePhoneObject();
                this.mCi.getNetworkSelectionMode(obtainMessage(14));
                getSubscriptionInfoAndStartPollingThreads();
                return;
            case 37:
                AsyncResult ar7 = (AsyncResult) msg.obj;
                if (ar7.exception == null) {
                    int otaStatus = ((int[]) ar7.result)[0];
                    if (otaStatus == 8 || otaStatus == 10) {
                        log("EVENT_OTA_PROVISION_STATUS_CHANGE: Complete, Reload MDN");
                        this.mCi.getCDMASubscription(obtainMessage(34));
                        return;
                    }
                    return;
                }
                return;
            case 38:
                synchronized (this) {
                    if (!this.mPendingRadioPowerOffAfterDataOff || msg.arg1 != this.mPendingRadioPowerOffAfterDataOffTag) {
                        log("EVENT_SET_RADIO_OFF is stale arg1=" + msg.arg1 + "!= tag=" + this.mPendingRadioPowerOffAfterDataOffTag);
                    } else {
                        log("EVENT_SET_RADIO_OFF, turn radio off now.");
                        hangupAndPowerOff();
                        this.mPendingRadioPowerOffAfterDataOffTag++;
                        this.mPendingRadioPowerOffAfterDataOff = false;
                    }
                }
                return;
            case 39:
                handleCdmaSubscriptionSource(this.mCdmaSSM.getCdmaSubscriptionSource());
                return;
            case 40:
                AsyncResult ar8 = (AsyncResult) msg.obj;
                if (ar8.exception == null) {
                    this.mPrlVersion = Integer.toString(((int[]) ar8.result)[0]);
                    return;
                }
                return;
            case 42:
                if (isSimAbsent()) {
                    log("EVENT_ICC_CHANGED: SIM absent");
                    cancelAllNotifications();
                    this.mMdn = null;
                    this.mMin = null;
                    this.mIsMinInfoReady = false;
                    this.mCdnr.updateEfFromRuim(null);
                    this.mCdnr.updateEfFromUsim(null);
                }
                onUpdateIccAvailability();
                UiccCardApplication uiccCardApplication = this.mUiccApplcation;
                if (uiccCardApplication != null && uiccCardApplication.getState() != IccCardApplicationStatus.AppState.APPSTATE_READY) {
                    this.mIsSimReady = false;
                    updateSpnDisplay();
                    return;
                }
                return;
            case 43:
            case 44:
                List<CellInfo> cellInfo = null;
                Throwable ex = null;
                if (msg.obj != null) {
                    AsyncResult ar9 = (AsyncResult) msg.obj;
                    if (ar9.exception != null) {
                        log("EVENT_GET_CELL_INFO_LIST: error ret null, e=" + ar9.exception);
                        ex = ar9.exception;
                    } else if (ar9.result == null) {
                        loge("Invalid CellInfo result");
                    } else {
                        cellInfo = (List) ar9.result;
                        updateOperatorNameForCellInfo(cellInfo);
                        this.mLastCellInfoList = cellInfo;
                        this.mPhone.notifyCellInfo(cellInfo);
                    }
                } else if (this.mIsPendingCellInfoRequest && SystemClock.elapsedRealtime() - this.mLastCellInfoReqTime >= CELL_INFO_LIST_QUERY_TIMEOUT) {
                    loge("Timeout waiting for CellInfo; (everybody panic)!");
                    this.mLastCellInfoList = null;
                } else {
                    return;
                }
                synchronized (this.mPendingCellInfoRequests) {
                    if (this.mIsPendingCellInfoRequest) {
                        this.mIsPendingCellInfoRequest = false;
                        for (Message m : this.mPendingCellInfoRequests) {
                            AsyncResult.forMessage(m, cellInfo, ex);
                            m.sendToTarget();
                        }
                        this.mPendingCellInfoRequests.clear();
                    }
                }
                return;
            case 45:
                log("EVENT_CHANGE_IMS_STATE:");
                setPowerStateToDesired();
                return;
            case 46:
                this.mCi.getImsRegistrationState(obtainMessage(47));
                return;
            case 47:
                AsyncResult ar10 = (AsyncResult) msg.obj;
                if (ar10.exception == null) {
                    if (((int[]) ar10.result)[0] == 1) {
                        z = true;
                    }
                    this.mImsRegistered = z;
                    return;
                }
                return;
            case 48:
                log("EVENT_IMS_CAPABILITY_CHANGED");
                updateSpnDisplay();
                this.mImsCapabilityChangedRegistrants.notifyRegistrants();
                return;
            case 49:
                ProxyController.getInstance().unregisterForAllDataDisconnected(SubscriptionManager.getDefaultDataSubscriptionId(), this);
                synchronized (this) {
                    if (this.mPendingRadioPowerOffAfterDataOff) {
                        log("EVENT_ALL_DATA_DISCONNECTED, turn radio off now.");
                        hangupAndPowerOff();
                        this.mPendingRadioPowerOffAfterDataOff = false;
                    } else {
                        log("EVENT_ALL_DATA_DISCONNECTED is stale");
                    }
                }
                return;
            case 51:
                AsyncResult ar11 = (AsyncResult) msg.obj;
                if (ar11.exception == null) {
                    boolean enable = ((Boolean) ar11.result).booleanValue();
                    log("EVENT_RADIO_POWER_FROM_CARRIER: " + enable);
                    setRadioPowerFromCarrier(enable);
                    return;
                }
                return;
            case 53:
                log("EVENT_IMS_SERVICE_STATE_CHANGED");
                if (this.mSS.getState() != 0) {
                    GsmCdmaPhone gsmCdmaPhone = this.mPhone;
                    gsmCdmaPhone.notifyServiceStateChanged(gsmCdmaPhone.getServiceState());
                    return;
                }
                return;
            case 54:
                log("EVENT_RADIO_POWER_OFF_DONE");
                if (this.mDeviceShuttingDown && this.mCi.getRadioState() != 2) {
                    this.mCi.requestShutdown(null);
                    return;
                }
                return;
            case 55:
                AsyncResult ar12 = (AsyncResult) msg.obj;
                if (ar12.exception == null) {
                    List<PhysicalChannelConfig> list = (List) ar12.result;
                    this.mPhone.notifyPhysicalChannelConfiguration(list);
                    this.mLastPhysicalChannelConfigList = list;
                    if ((updateNrFrequencyRangeFromPhysicalChannelConfigs(list, this.mSS) || updateNrStateFromPhysicalChannelConfigs(list, this.mSS)) || RatRatcheter.updateBandwidths(getBandwidthsFromConfigs(list), this.mSS)) {
                        this.mPhone.notifyServiceStateChanged(this.mSS);
                        return;
                    }
                    return;
                }
                return;
            case 56:
                AsyncResult ar13 = (AsyncResult) msg.obj;
                if (ar13 == null) {
                    loge("Invalid null response to getCellLocation!");
                    return;
                }
                Message rspRspMsg = (Message) ar13.userObj;
                AsyncResult.forMessage(rspRspMsg, getCellLocation(), ar13.exception);
                rspRspMsg.sendToTarget();
                return;
            case 57:
                onCarrierConfigChanged();
                return;
        }
    }

    private boolean isSimAbsent() {
        UiccController uiccController = this.mUiccController;
        if (uiccController == null) {
            return true;
        }
        UiccCard uiccCard = uiccController.getUiccCard(this.mPhone.getPhoneId());
        if (uiccCard == null) {
            return true;
        }
        return uiccCard.getCardState() == IccCardStatus.CardState.CARDSTATE_ABSENT;
    }

    private int[] getBandwidthsFromConfigs(List<PhysicalChannelConfig> list) {
        return list.stream().map($$Lambda$WWHOcG5P4jgjzPPgLwmwN15OM.INSTANCE).mapToInt($$Lambda$UV1wDVoVlbcxpr8zevj_aMFtUGw.INSTANCE).toArray();
    }

    /* access modifiers changed from: protected */
    public boolean isSidsAllZeros() {
        if (this.mHomeSystemId == null) {
            return true;
        }
        int i = 0;
        while (true) {
            int[] iArr = this.mHomeSystemId;
            if (i >= iArr.length) {
                return true;
            }
            if (iArr[i] != 0) {
                return false;
            }
            i++;
        }
    }

    public ServiceState getServiceState() {
        return new ServiceState(this.mSS);
    }

    /* access modifiers changed from: protected */
    public boolean isHomeSid(int sid) {
        if (this.mHomeSystemId == null) {
            return false;
        }
        int i = 0;
        while (true) {
            int[] iArr = this.mHomeSystemId;
            if (i >= iArr.length) {
                return false;
            }
            if (sid == iArr[i]) {
                return true;
            }
            i++;
        }
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
        if (TextUtils.isEmpty(operatorNumeric) || getCdmaMin() == null) {
            IccRecords iccRecords = this.mIccRecords;
            if (iccRecords != null) {
                return iccRecords.getIMSI();
            }
            log("getImsi,imsi is null");
            return null;
        }
        return operatorNumeric + getCdmaMin();
    }

    public boolean isMinInfoReady() {
        return this.mIsMinInfoReady;
    }

    public int getOtasp() {
        int provisioningState;
        if (!this.mPhone.getIccRecordsLoaded()) {
            log("getOtasp: otasp uninitialized due to sim not loaded");
            return 0;
        } else if (this.mPhone.isPhoneTypeGsm()) {
            log("getOtasp: otasp not needed for GSM");
            return 3;
        } else if (this.mIsSubscriptionFromRuim && this.mMin == null) {
            return 2;
        } else {
            String str = this.mMin;
            if (str == null || str.length() < 6) {
                log("getOtasp: bad mMin='" + this.mMin + "'");
                provisioningState = 1;
            } else if (this.mMin.equals(UNACTIVATED_MIN_VALUE) || this.mMin.substring(0, 6).equals(UNACTIVATED_MIN2_VALUE) || SystemProperties.getBoolean("test_cdma_setup", false)) {
                provisioningState = 2;
            } else {
                provisioningState = 3;
            }
            log("getOtasp: state=" + provisioningState);
            return provisioningState;
        }
    }

    /* access modifiers changed from: protected */
    public void parseSidNid(String sidStr, String nidStr) {
        if (sidStr != null) {
            String[] sid = sidStr.split(",");
            this.mHomeSystemId = new int[sid.length];
            for (int i = 0; i < sid.length; i++) {
                try {
                    this.mHomeSystemId[i] = Integer.parseInt(sid[i]);
                } catch (NumberFormatException ex) {
                    loge("error parsing system id: " + ex);
                } catch (Exception e) {
                    Rlog.d(LOG_TAG, e.toString());
                }
            }
        }
        log("CDMA_SUBSCRIPTION: SID=" + sidStr);
        if (nidStr != null) {
            String[] nid = nidStr.split(",");
            this.mHomeNetworkId = new int[nid.length];
            for (int i2 = 0; i2 < nid.length; i2++) {
                try {
                    this.mHomeNetworkId[i2] = Integer.parseInt(nid[i2]);
                } catch (NumberFormatException ex2) {
                    loge("CDMA_SUBSCRIPTION: error parsing network id: " + ex2);
                } catch (Exception e2) {
                    Rlog.d(LOG_TAG, e2.toString());
                }
            }
        }
        log("CDMA_SUBSCRIPTION: NID=" + nidStr);
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void updateOtaspState() {
        int otaspMode = getOtasp();
        int oldOtaspMode = this.mCurrentOtaspMode;
        this.mCurrentOtaspMode = otaspMode;
        if (oldOtaspMode != this.mCurrentOtaspMode) {
            log("updateOtaspState: call notifyOtaspChanged old otaspMode=" + oldOtaspMode + " new otaspMode=" + this.mCurrentOtaspMode);
            this.mPhone.notifyOtaspChanged(this.mCurrentOtaspMode);
        }
    }

    /* access modifiers changed from: protected */
    public Phone getPhone() {
        return this.mPhone;
    }

    /* access modifiers changed from: protected */
    public void handlePollStateResult(int what, AsyncResult ar) {
        boolean isRoamingBetweenOperators;
        if (ar.userObj == this.mPollingContext) {
            if (ar.exception != null) {
                CommandException.Error err = null;
                if (ar.exception instanceof IllegalStateException) {
                    log("handlePollStateResult exception " + ar.exception);
                }
                if (ar.exception instanceof CommandException) {
                    err = ((CommandException) ar.exception).getCommandError();
                }
                if (err == CommandException.Error.RADIO_NOT_AVAILABLE) {
                    cancelPollState();
                    return;
                } else if (err != CommandException.Error.OP_NOT_ALLOWED_BEFORE_REG_NW) {
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
            boolean isVoiceInService = false;
            iArr[0] = iArr[0] - 1;
            if (iArr[0] == 0) {
                this.mNewSS.setEmergencyOnly(this.mEmergencyOnly);
                combinePsRegistrationStates(this.mNewSS);
                updateOperatorNameForServiceState(this.mNewSS);
                if (this.mPhone.isPhoneTypeGsm()) {
                    updateRoamingState();
                } else {
                    boolean namMatch = false;
                    if (!isSidsAllZeros() && isHomeSid(this.mNewSS.getCdmaSystemId())) {
                        namMatch = true;
                    }
                    if (this.mIsSubscriptionFromRuim && (isRoamingBetweenOperators = isRoamingBetweenOperators(this.mNewSS.getVoiceRoaming(), this.mNewSS)) != this.mNewSS.getVoiceRoaming()) {
                        log("isRoamingBetweenOperators=" + isRoamingBetweenOperators + ". Override CDMA voice roaming to " + isRoamingBetweenOperators);
                        this.mNewSS.setVoiceRoaming(isRoamingBetweenOperators);
                    }
                    if (ServiceState.isCdma(this.mNewSS.getRilDataRadioTechnology())) {
                        if (this.mNewSS.getVoiceRegState() == 0) {
                            isVoiceInService = true;
                        }
                        if (isVoiceInService) {
                            boolean isVoiceRoaming = this.mNewSS.getVoiceRoaming();
                            if (this.mNewSS.getDataRoaming() != isVoiceRoaming) {
                                log("Data roaming != Voice roaming. Override data roaming to " + isVoiceRoaming);
                                this.mNewSS.setDataRoaming(isVoiceRoaming);
                            }
                        } else {
                            boolean isRoamIndForHomeSystem = isRoamIndForHomeSystem(this.mRoamingIndicator);
                            if (this.mNewSS.getDataRoaming() == isRoamIndForHomeSystem) {
                                StringBuilder sb = new StringBuilder();
                                sb.append("isRoamIndForHomeSystem=");
                                sb.append(isRoamIndForHomeSystem);
                                sb.append(", override data roaming to ");
                                sb.append(!isRoamIndForHomeSystem);
                                log(sb.toString());
                                this.mNewSS.setDataRoaming(!isRoamIndForHomeSystem);
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
                        if (!namMatch && !this.mIsInPrl) {
                            this.mNewSS.setCdmaRoamingIndicator(this.mDefaultRoamingIndicator);
                        } else if (!namMatch || this.mIsInPrl) {
                            if (namMatch || !this.mIsInPrl) {
                                int i = this.mRoamingIndicator;
                                if (i <= 2) {
                                    this.mNewSS.setCdmaRoamingIndicator(1);
                                } else {
                                    this.mNewSS.setCdmaRoamingIndicator(i);
                                }
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
                    this.mNewSS.setCdmaEriIconIndex(this.mEriManager.getCdmaEriIconIndex(roamingIndicator, this.mDefaultRoamingIndicator));
                    this.mNewSS.setCdmaEriIconMode(this.mEriManager.getCdmaEriIconMode(roamingIndicator, this.mDefaultRoamingIndicator));
                    log("Set CDMA Roaming Indicator to: " + this.mNewSS.getCdmaRoamingIndicator() + ". voiceRoaming = " + this.mNewSS.getVoiceRoaming() + ". dataRoaming = " + this.mNewSS.getDataRoaming() + ", isPrlLoaded = " + isPrlLoaded + ". namMatch = " + namMatch + " , mIsInPrl = " + this.mIsInPrl + ", mRoamingIndicator = " + this.mRoamingIndicator + ", mDefaultRoamingIndicator= " + this.mDefaultRoamingIndicator);
                }
                pollStateDone();
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isRoamingBetweenOperators(boolean cdmaRoaming, ServiceState s) {
        return cdmaRoaming && !isSameOperatorNameFromSimAndSS(s);
    }

    /* access modifiers changed from: protected */
    public boolean isNrStateChanged(NetworkRegistrationInfo oldRegState, NetworkRegistrationInfo newRegState) {
        return (oldRegState == null || newRegState == null) ? oldRegState != newRegState : oldRegState.getNrState() != newRegState.getNrState();
    }

    /* access modifiers changed from: protected */
    public boolean updateNrFrequencyRangeFromPhysicalChannelConfigs(List<PhysicalChannelConfig> physicalChannelConfigs, ServiceState ss) {
        int newFrequencyRange = -1;
        boolean hasChanged = false;
        if (physicalChannelConfigs != null) {
            DcTracker dcTracker = this.mPhone.getDcTracker(1);
            for (PhysicalChannelConfig config : physicalChannelConfigs) {
                if (isNrPhysicalChannelConfig(config)) {
                    int[] contextIds = config.getContextIds();
                    int length = contextIds.length;
                    int i = 0;
                    while (true) {
                        if (i < length) {
                            DataConnection dc = dcTracker.getDataConnectionByContextId(contextIds[i]);
                            if (dc != null && dc.getNetworkCapabilities().hasCapability(12)) {
                                newFrequencyRange = ServiceState.getBetterNRFrequencyRange(newFrequencyRange, config.getFrequencyRange());
                                break;
                            }
                            i++;
                        } else {
                            break;
                        }
                    }
                }
            }
        }
        if (newFrequencyRange != ss.getNrFrequencyRange()) {
            hasChanged = true;
        }
        ss.setNrFrequencyRange(newFrequencyRange);
        return hasChanged;
    }

    /* access modifiers changed from: protected */
    public boolean updateNrStateFromPhysicalChannelConfigs(List<PhysicalChannelConfig> configs, ServiceState ss) {
        boolean hasChanged = true;
        NetworkRegistrationInfo regInfo = ss.getNetworkRegistrationInfo(2, 1);
        if (regInfo != null && configs == null) {
            regInfo.setNrState(super.oppoUpdateNrState(regInfo.getNrState(), false, ss));
            ss.addNetworkRegistrationInfo(regInfo);
            return false;
        } else if (regInfo == null || configs == null) {
            return false;
        } else {
            boolean hasNrSecondaryServingCell = false;
            Iterator<PhysicalChannelConfig> it = configs.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                PhysicalChannelConfig config = it.next();
                if (isNrPhysicalChannelConfig(config) && config.getConnectionStatus() == 2) {
                    hasNrSecondaryServingCell = true;
                    break;
                }
            }
            int newNrState = regInfo.getNrState();
            if (hasNrSecondaryServingCell) {
                if (regInfo.getNrState() == 2) {
                    newNrState = 3;
                }
            } else if (regInfo.getNrState() == 3) {
                newNrState = 2;
            }
            int newNrState2 = super.oppoUpdateNrState(newNrState, hasNrSecondaryServingCell, ss);
            if (newNrState2 == regInfo.getNrState()) {
                hasChanged = false;
            }
            regInfo.setNrState(newNrState2);
            ss.addNetworkRegistrationInfo(regInfo);
            return hasChanged;
        }
    }

    private boolean isNrPhysicalChannelConfig(PhysicalChannelConfig config) {
        return config.getRat() == 20;
    }

    /* access modifiers changed from: protected */
    public void combinePsRegistrationStates(ServiceState serviceState) {
        NetworkRegistrationInfo wlanPsRegState = serviceState.getNetworkRegistrationInfo(2, 2);
        NetworkRegistrationInfo wwanPsRegState = serviceState.getNetworkRegistrationInfo(2, 1);
        boolean isIwlanPreferred = this.mTransportManager.isAnyApnPreferredOnIwlan();
        serviceState.setIwlanPreferred(isIwlanPreferred);
        if (wlanPsRegState != null && wlanPsRegState.getAccessNetworkTechnology() == 18 && wlanPsRegState.getRegistrationState() == 1 && isIwlanPreferred) {
            serviceState.setDataRegState(0);
        } else if (wwanPsRegState != null) {
            serviceState.setDataRegState(regCodeToServiceState(wwanPsRegState.getRegistrationState()));
        }
        log("combinePsRegistrationStates: " + serviceState);
    }

    /* access modifiers changed from: protected */
    public void handlePollStateResultMessage(int what, AsyncResult ar) {
        int networkId;
        if (what == 4) {
            NetworkRegistrationInfo networkRegState = (NetworkRegistrationInfo) ar.result;
            VoiceSpecificRegistrationInfo voiceSpecificStates = networkRegState.getVoiceSpecificInfo();
            int registrationState = networkRegState.getRegistrationState();
            boolean z = voiceSpecificStates.cssSupported;
            ServiceState.networkTypeToRilRadioTechnology(networkRegState.getAccessNetworkTechnology());
            this.mNewSS.setVoiceRegState(regCodeToServiceState(registrationState));
            super.oppoUpdateVoiceRegState(regCodeToServiceState(registrationState));
            this.mNewSS.setCssIndicator(z ? 1 : 0);
            this.mNewSS.addNetworkRegistrationInfo(networkRegState);
            setPhyCellInfoFromCellIdentity(this.mNewSS, networkRegState.getCellIdentity());
            int reasonForDenial = networkRegState.getRejectCause();
            this.mEmergencyOnly = networkRegState.isEmergencyEnabled();
            if (this.mPhone.isPhoneTypeGsm()) {
                this.mGsmRoaming = regCodeIsRoaming(registrationState);
                this.mNewRejectCode = reasonForDenial;
                this.mPhone.getContext().getResources().getBoolean(17891571);
            } else {
                int roamingIndicator = voiceSpecificStates.roamingIndicator;
                int systemIsInPrl = voiceSpecificStates.systemIsInPrl;
                int defaultRoamingIndicator = voiceSpecificStates.defaultRoamingIndicator;
                this.mRegistrationState = registrationState;
                this.mNewSS.setVoiceRoaming(regCodeIsRoaming(registrationState) && !isRoamIndForHomeSystem(roamingIndicator));
                this.mRoamingIndicator = roamingIndicator;
                this.mIsInPrl = systemIsInPrl != 0;
                this.mDefaultRoamingIndicator = defaultRoamingIndicator;
                int systemId = 0;
                CellIdentity cellIdentity = networkRegState.getCellIdentity();
                if (cellIdentity == null || cellIdentity.getType() != 2) {
                    networkId = 0;
                } else {
                    systemId = ((CellIdentityCdma) cellIdentity).getSystemId();
                    networkId = ((CellIdentityCdma) cellIdentity).getNetworkId();
                }
                this.mNewSS.setCdmaSystemAndNetworkId(systemId, networkId);
                if (reasonForDenial == 0) {
                    this.mRegistrationDeniedReason = REGISTRATION_DENIED_GEN;
                } else if (reasonForDenial == 1) {
                    this.mRegistrationDeniedReason = REGISTRATION_DENIED_AUTH;
                } else {
                    this.mRegistrationDeniedReason = PhoneConfigurationManager.SSSS;
                }
                if (this.mRegistrationState == 3) {
                    log("Registration denied, " + this.mRegistrationDeniedReason);
                }
            }
            this.mNewCellIdentity = networkRegState.getCellIdentity();
            log("handlePollStateResultMessage: CS cellular. " + networkRegState);
        } else if (what == 5) {
            NetworkRegistrationInfo networkRegState2 = (NetworkRegistrationInfo) ar.result;
            this.mNewSS.addNetworkRegistrationInfo(networkRegState2);
            DataSpecificRegistrationInfo dataSpecificStates = networkRegState2.getDataSpecificInfo();
            int registrationState2 = networkRegState2.getRegistrationState();
            int serviceState = regCodeToServiceState(registrationState2);
            int newDataRat = ServiceState.networkTypeToRilRadioTechnology(networkRegState2.getAccessNetworkTechnology());
            log("handlePollStateResultMessage: PS cellular. " + networkRegState2);
            if (serviceState == 1) {
                this.mLastPhysicalChannelConfigList = null;
                updateNrFrequencyRangeFromPhysicalChannelConfigs(null, this.mNewSS);
            }
            setPhyCellInfoFromCellIdentity(this.mNewSS, networkRegState2.getCellIdentity());
            if (super.isTelstraVersion() && newDataRat == 19) {
                newDataRat = 14;
                log(" modify type from CA to LTE for telstra version");
            }
            if (this.mPhone.isPhoneTypeGsm()) {
                this.mNewReasonDataDenied = networkRegState2.getRejectCause();
                this.mNewMaxDataCalls = dataSpecificStates.maxDataCalls;
                this.mDataRoaming = regCodeIsRoaming(registrationState2);
            } else if (this.mPhone.isPhoneTypeCdma()) {
                this.mNewSS.setDataRoaming(regCodeIsRoaming(registrationState2));
            } else {
                int oldDataRAT = this.mSS.getRilDataRadioTechnology();
                if ((oldDataRAT == 0 && newDataRat != 0) || ((ServiceState.isCdma(oldDataRAT) && ServiceState.isLte(newDataRat)) || (ServiceState.isLte(oldDataRAT) && ServiceState.isCdma(newDataRat)))) {
                    this.mCi.getSignalStrength(obtainMessage(3));
                }
                this.mNewSS.setDataRoaming(regCodeIsRoaming(registrationState2));
            }
            updateServiceStateLteEarfcnBoost(this.mNewSS, getLteEarfcn(networkRegState2.getCellIdentity()));
        } else if (what == 6) {
            NetworkRegistrationInfo networkRegState3 = (NetworkRegistrationInfo) ar.result;
            this.mNewSS.addNetworkRegistrationInfo(networkRegState3);
            log("handlePollStateResultMessage: PS IWLAN. " + networkRegState3);
        } else if (what == 7) {
            String brandOverride = getOperatorBrandOverride();
            this.mCdnr.updateEfForBrandOverride(brandOverride);
            if (this.mPhone.isPhoneTypeGsm()) {
                String[] opNames = (String[]) ar.result;
                if (opNames != null && opNames.length >= 3) {
                    this.mNewSS.setOperatorAlphaLongRaw(opNames[0]);
                    this.mNewSS.setOperatorAlphaShortRaw(opNames[1]);
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
            String[] opNames2 = (String[]) ar.result;
            if (opNames2 == null || opNames2.length < 3) {
                log("EVENT_POLL_STATE_OPERATOR_CDMA: error parsing opNames");
                return;
            }
            if (opNames2[2] == null || opNames2[2].length() < 5 || "00000".equals(opNames2[2])) {
                opNames2[2] = SystemProperties.get(GsmCdmaPhone.PROPERTY_CDMA_HOME_OPERATOR_NUMERIC, "00000");
                log("RIL_REQUEST_OPERATOR.response[2], the numeric,  is bad. Using SystemProperties 'ro.cdma.home.operator.numeric'= " + opNames2[2]);
            }
            if (!this.mIsSubscriptionFromRuim) {
                this.mNewSS.setOperatorName(opNames2[0], opNames2[1], opNames2[2]);
            } else if (brandOverride != null) {
                this.mNewSS.setOperatorName(brandOverride, brandOverride, opNames2[2]);
            } else {
                this.mNewSS.setOperatorName(opNames2[0], opNames2[1], opNames2[2]);
            }
        } else if (what != 14) {
            loge("handlePollStateResultMessage: Unexpected RIL response received: " + what);
        } else {
            int[] ints = (int[]) ar.result;
            this.mNewSS.setIsManualSelection(ints[0] == 1);
            if (ints[0] == 1 && this.mPhone.shouldForceAutoNetworkSelect()) {
                this.mPhone.setNetworkSelectionModeAutomatic(null);
                log(" Forcing Automatic Network Selection, manual selection is not allowed");
            }
        }
    }

    private static boolean isValidLteBandwidthKhz(int bandwidth) {
        if (bandwidth == 1400 || bandwidth == 3000 || bandwidth == 5000 || bandwidth == 10000 || bandwidth == 15000 || bandwidth == POLL_PERIOD_MILLIS) {
            return true;
        }
        return false;
    }

    protected static int getCidFromCellIdentity(CellIdentity id) {
        if (id == null) {
            return -1;
        }
        int cid = -1;
        int type = id.getType();
        if (type == 1) {
            cid = ((CellIdentityGsm) id).getCid();
        } else if (type == 3) {
            cid = ((CellIdentityLte) id).getCi();
        } else if (type == 4) {
            cid = ((CellIdentityWcdma) id).getCid();
        } else if (type == 5) {
            cid = ((CellIdentityTdscdma) id).getCid();
        }
        if (cid == Integer.MAX_VALUE) {
            return -1;
        }
        return cid;
    }

    /* access modifiers changed from: protected */
    public void setPhyCellInfoFromCellIdentity(ServiceState ss, CellIdentity cellIdentity) {
        if (cellIdentity == null) {
            log("Could not set ServiceState channel number. CellIdentity null");
            return;
        }
        ss.setChannelNumber(cellIdentity.getChannelNumber());
        if (cellIdentity instanceof CellIdentityLte) {
            CellIdentityLte cl = (CellIdentityLte) cellIdentity;
            int[] bandwidths = null;
            if (!ArrayUtils.isEmpty(this.mLastPhysicalChannelConfigList)) {
                bandwidths = getBandwidthsFromConfigs(this.mLastPhysicalChannelConfigList);
                int length = bandwidths.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    }
                    int bw = bandwidths[i];
                    if (!isValidLteBandwidthKhz(bw)) {
                        loge("Invalid LTE Bandwidth in RegistrationState, " + bw);
                        bandwidths = null;
                        break;
                    }
                    i++;
                }
            }
            if (bandwidths == null || bandwidths.length == 1) {
                int cbw = cl.getBandwidth();
                if (isValidLteBandwidthKhz(cbw)) {
                    bandwidths = new int[]{cbw};
                } else if (cbw != Integer.MAX_VALUE) {
                    loge("Invalid LTE Bandwidth in RegistrationState, " + cbw);
                }
            }
            if (bandwidths != null) {
                ss.setCellBandwidths(bandwidths);
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isRoamIndForHomeSystem(int roamInd) {
        int[] homeRoamIndicators = getCarrierConfig().getIntArray("cdma_enhanced_roaming_indicator_for_home_network_int_array");
        log("isRoamIndForHomeSystem: homeRoamIndicators=" + Arrays.toString(homeRoamIndicators));
        if (homeRoamIndicators != null) {
            for (int homeRoamInd : homeRoamIndicators) {
                if (homeRoamInd == roamInd) {
                    return true;
                }
            }
            log("isRoamIndForHomeSystem: No match found against list for roamInd=" + roamInd);
            return false;
        }
        log("isRoamIndForHomeSystem: No list found");
        return false;
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void updateRoamingState() {
        PersistableBundle bundle = getCarrierConfig();
        boolean z = false;
        if (this.mPhone.isPhoneTypeGsm()) {
            if (this.mGsmRoaming || this.mDataRoaming) {
                z = true;
            }
            boolean roaming = z;
            if (this.mGsmRoaming && !isOperatorConsideredRoaming(this.mNewSS) && (isSameNamedOperators(this.mNewSS) || isOperatorConsideredNonRoaming(this.mNewSS))) {
                log("updateRoamingState: resource override set non roaming.isSameNamedOperators=" + isSameNamedOperators(this.mNewSS) + ",isOperatorConsideredNonRoaming=" + isOperatorConsideredNonRoaming(this.mNewSS));
                roaming = false;
            }
            if (alwaysOnHomeNetwork(bundle)) {
                log("updateRoamingState: carrier config override always on home network");
                roaming = false;
            } else if (isNonRoamingInGsmNetwork(bundle, this.mNewSS.getOperatorNumeric())) {
                log("updateRoamingState: carrier config override set non roaming:" + this.mNewSS.getOperatorNumeric());
                roaming = false;
            } else if (isRoamingInGsmNetwork(bundle, this.mNewSS.getOperatorNumeric())) {
                log("updateRoamingState: carrier config override set roaming:" + this.mNewSS.getOperatorNumeric());
                roaming = true;
            }
            if (SubscriptionManager.isVsimEnabled(this.mPhone.getSubId())) {
                log("Vsim is Enabled, set roaming = false.");
                roaming = false;
            }
            this.mNewSS.setVoiceRoaming(roaming);
            this.mNewSS.setDataRoaming(roaming);
            return;
        }
        String systemId = Integer.toString(this.mNewSS.getCdmaSystemId());
        if (alwaysOnHomeNetwork(bundle)) {
            log("updateRoamingState: carrier config override always on home network");
            setRoamingOff();
        } else if (isNonRoamingInGsmNetwork(bundle, this.mNewSS.getOperatorNumeric()) || isNonRoamingInCdmaNetwork(bundle, systemId)) {
            log("updateRoamingState: carrier config override set non-roaming:" + this.mNewSS.getOperatorNumeric() + ", " + systemId);
            setRoamingOff();
        } else if (isRoamingInGsmNetwork(bundle, this.mNewSS.getOperatorNumeric()) || isRoamingInCdmaNetwork(bundle, systemId)) {
            log("updateRoamingState: carrier config override set roaming:" + this.mNewSS.getOperatorNumeric() + ", " + systemId);
            setRoamingOn();
        }
        if (Build.IS_DEBUGGABLE && SystemProperties.getBoolean(PROP_FORCE_ROAMING, false)) {
            this.mNewSS.setVoiceRoaming(true);
            this.mNewSS.setDataRoaming(true);
        }
    }

    /* access modifiers changed from: protected */
    public void setRoamingOn() {
        this.mNewSS.setVoiceRoaming(true);
        this.mNewSS.setDataRoaming(true);
        this.mNewSS.setCdmaEriIconIndex(0);
        this.mNewSS.setCdmaEriIconMode(0);
    }

    /* access modifiers changed from: protected */
    public void setRoamingOff() {
        this.mNewSS.setVoiceRoaming(false);
        this.mNewSS.setDataRoaming(false);
        this.mNewSS.setCdmaEriIconIndex(1);
    }

    /* access modifiers changed from: protected */
    public void updateOperatorNameFromCarrierConfig() {
        if (!this.mPhone.isPhoneTypeGsm() && !this.mSS.getRoaming()) {
            if (!((this.mUiccController.getUiccCard(getPhoneId()) == null || this.mUiccController.getUiccCard(getPhoneId()).getOperatorBrandOverride() == null) ? false : true)) {
                PersistableBundle config = getCarrierConfig();
                if (config.getBoolean("cdma_home_registered_plmn_name_override_bool")) {
                    String operator = config.getString("cdma_home_registered_plmn_name_string");
                    log("updateOperatorNameFromCarrierConfig: changing from " + this.mSS.getOperatorAlpha() + " to " + operator);
                    ServiceState serviceState = this.mSS;
                    serviceState.setOperatorName(operator, operator, serviceState.getOperatorNumeric());
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void notifySpnDisplayUpdate(CarrierDisplayNameData data) {
        int subId = this.mPhone.getSubId();
        if (this.mSubId != subId || data.shouldShowPlmn() != this.mCurShowPlmn || data.shouldShowSpn() != this.mCurShowSpn || !TextUtils.equals(data.getSpn(), this.mCurSpn) || !TextUtils.equals(data.getDataSpn(), this.mCurDataSpn) || !TextUtils.equals(data.getPlmn(), this.mCurPlmn)) {
            String log = String.format("updateSpnDisplay: changed sending intent, rule=%d, showPlmn='%b', plmn='%s', showSpn='%b', spn='%s', dataSpn='%s', subId='%d'", Integer.valueOf(getCarrierNameDisplayBitmask(this.mSS)), Boolean.valueOf(data.shouldShowPlmn()), data.getPlmn(), Boolean.valueOf(data.shouldShowSpn()), data.getSpn(), data.getDataSpn(), Integer.valueOf(subId));
            this.mCdnrLogs.log(log);
            log("updateSpnDisplay: " + log);
            Intent intent = new Intent("android.provider.Telephony.SPN_STRINGS_UPDATED");
            intent.putExtra("showSpn", data.shouldShowSpn());
            intent.putExtra("spn", data.getSpn());
            intent.putExtra("spnData", data.getDataSpn());
            intent.putExtra("showPlmn", data.shouldShowPlmn());
            intent.putExtra("plmn", data.getPlmn());
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
            this.mPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            if (!this.mSubscriptionController.setPlmnSpn(this.mPhone.getPhoneId(), data.shouldShowPlmn(), data.getPlmn(), data.shouldShowSpn(), data.getSpn())) {
                this.mSpnUpdatePending = true;
            }
        }
        this.mSubId = subId;
        this.mCurShowSpn = data.shouldShowSpn();
        this.mCurShowPlmn = data.shouldShowPlmn();
        this.mCurSpn = data.getSpn();
        this.mCurDataSpn = data.getDataSpn();
        this.mCurPlmn = data.getPlmn();
    }

    private void updateSpnDisplayCdnr() {
        log("updateSpnDisplayCdnr+");
        notifySpnDisplayUpdate(this.mCdnr.getCarrierDisplayNameData());
        log("updateSpnDisplayCdnr-");
    }

    @UnsupportedAppUsage
    @VisibleForTesting
    public void updateSpnDisplay() {
        if (getCarrierConfig().getBoolean("enable_carrier_display_name_resolver_bool")) {
            updateSpnDisplayCdnr();
        } else {
            updateSpnDisplayLegacy();
        }
    }

    /* access modifiers changed from: protected */
    public void updateSpnDisplayLegacy() {
        String dataSpn;
        String spn;
        boolean showPlmn;
        String plmn;
        String dataSpn2;
        String eriText;
        boolean showPlmn2;
        log("updateSpnDisplayLegacy+");
        boolean showSpn = false;
        String wfcVoiceSpnFormat = null;
        String wfcDataSpnFormat = null;
        String wfcFlightSpnFormat = null;
        int combinedRegState = getCombinedRegState(this.mSS);
        if (this.mPhone.getImsPhone() == null || !this.mPhone.getImsPhone().isWifiCallingEnabled() || combinedRegState != 0) {
            spn = null;
            dataSpn = null;
        } else {
            PersistableBundle bundle = getCarrierConfig();
            int voiceIdx = bundle.getInt("wfc_spn_format_idx_int");
            int dataIdx = bundle.getInt("wfc_data_spn_format_idx_int");
            int flightModeIdx = bundle.getInt("wfc_flight_mode_spn_format_idx_int");
            spn = null;
            String[] wfcSpnFormats = SubscriptionManager.getResourcesForSubId(this.mPhone.getContext(), this.mPhone.getSubId(), bundle.getBoolean("wfc_spn_use_root_locale")).getStringArray(17236129);
            if (voiceIdx < 0 || voiceIdx >= wfcSpnFormats.length) {
                StringBuilder sb = new StringBuilder();
                dataSpn = null;
                sb.append("updateSpnDisplay: KEY_WFC_SPN_FORMAT_IDX_INT out of bounds: ");
                sb.append(voiceIdx);
                loge(sb.toString());
                voiceIdx = 0;
            } else {
                dataSpn = null;
            }
            if (dataIdx < 0 || dataIdx >= wfcSpnFormats.length) {
                loge("updateSpnDisplay: KEY_WFC_DATA_SPN_FORMAT_IDX_INT out of bounds: " + dataIdx);
                dataIdx = 0;
            }
            if (flightModeIdx < 0 || flightModeIdx >= wfcSpnFormats.length) {
                flightModeIdx = voiceIdx;
            }
            wfcVoiceSpnFormat = wfcSpnFormats[voiceIdx];
            wfcDataSpnFormat = wfcSpnFormats[dataIdx];
            wfcFlightSpnFormat = wfcSpnFormats[flightModeIdx];
        }
        if (this.mPhone.isPhoneTypeGsm()) {
            IccRecords iccRecords = this.mIccRecords;
            int rule = getCarrierNameDisplayBitmask(this.mSS);
            boolean noService = false;
            if (combinedRegState == 1 || combinedRegState == 2) {
                boolean forceDisplayNoService = this.mPhone.getContext().getResources().getBoolean(17891414) && !this.mIsSimReady;
                if (!this.mEmergencyOnly || forceDisplayNoService) {
                    plmn = Resources.getSystem().getText(17040247).toString();
                    noService = true;
                } else {
                    plmn = Resources.getSystem().getText(17039896).toString();
                }
                log("updateSpnDisplay: radio is on but out of service, set plmn='" + plmn + "'");
                showPlmn2 = true;
            } else if (combinedRegState == 0) {
                String plmn2 = this.mSS.getOperatorAlpha();
                boolean showPlmn3 = !TextUtils.isEmpty(plmn2) && (rule & 2) == 2;
                log("updateSpnDisplay: rawPlmn = " + plmn2);
                plmn = plmn2;
                showPlmn2 = showPlmn3;
            } else {
                showPlmn2 = true;
                plmn = Resources.getSystem().getText(17040247).toString();
                log("updateSpnDisplay: radio is off w/ showPlmn=" + true + " plmn=" + plmn);
            }
            String spn2 = getServiceProviderName();
            if (OemTelephonyUtils.isInCnList(this.mPhone.getContext(), spn2)) {
                IccRecords iccRecords2 = this.mIccRecords;
                spn2 = iccRecords2 != null ? iccRecords2.getServiceProviderName() : PhoneConfigurationManager.SSSS;
                log("updateSpnDisplay: Cnlist sim spn = " + spn2);
            }
            showSpn = !noService && !TextUtils.isEmpty(spn2) && (rule & 1) == 1;
            log("updateSpnDisplay: rawSpn = " + spn2);
            if (!TextUtils.isEmpty(spn2) && !TextUtils.isEmpty(wfcVoiceSpnFormat)) {
                if (!TextUtils.isEmpty(wfcDataSpnFormat)) {
                    if (this.mSS.getVoiceRegState() == 3) {
                        wfcVoiceSpnFormat = wfcFlightSpnFormat;
                    }
                    String originalSpn = spn2.trim();
                    eriText = String.format(wfcVoiceSpnFormat, originalSpn);
                    showSpn = true;
                    showPlmn = false;
                    dataSpn2 = String.format(wfcDataSpnFormat, originalSpn);
                }
            }
            if (!TextUtils.isEmpty(plmn) && !TextUtils.isEmpty(wfcVoiceSpnFormat)) {
                plmn = String.format(wfcVoiceSpnFormat, plmn.trim());
                eriText = spn2;
                showPlmn = showPlmn2;
                dataSpn2 = spn2;
            } else if (this.mSS.getVoiceRegState() == 3 || (showPlmn2 && TextUtils.equals(spn2, plmn))) {
                eriText = null;
                showSpn = false;
                showPlmn = showPlmn2;
                dataSpn2 = spn2;
            } else {
                eriText = spn2;
                showPlmn = showPlmn2;
                dataSpn2 = spn2;
            }
        } else {
            String eriText2 = getOperatorNameFromEri();
            if (eriText2 != null) {
                this.mSS.setOperatorAlphaLong(eriText2);
            }
            updateOperatorNameFromCarrierConfig();
            String plmn3 = this.mSS.getOperatorAlpha();
            log("updateSpnDisplay: cdma rawPlmn = " + plmn3);
            showPlmn = plmn3 != null;
            if (!TextUtils.isEmpty(plmn3) && !TextUtils.isEmpty(wfcVoiceSpnFormat)) {
                plmn3 = String.format(wfcVoiceSpnFormat, plmn3.trim());
            } else if (this.mCi.getRadioState() == 0) {
                log("updateSpnDisplay: overwriting plmn from " + plmn3 + " to null as radio state is off");
                plmn3 = null;
            }
            if (combinedRegState == 1) {
                plmn = Resources.getSystem().getText(17040247).toString();
                log("updateSpnDisplay: radio is on but out of svc, set plmn='" + plmn + "'");
                eriText = spn;
                dataSpn2 = dataSpn;
            } else {
                eriText = spn;
                dataSpn2 = dataSpn;
            }
        }
        notifySpnDisplayUpdate(new CarrierDisplayNameData.Builder().setSpn(eriText).setDataSpn(dataSpn2).setShowSpn(showSpn).setPlmn(plmn).setShowPlmn(showPlmn).build());
        log("updateSpnDisplayLegacy-");
    }

    /* access modifiers changed from: protected */
    public void setPowerStateToDesired() {
        String tmpLog = "mDeviceShuttingDown=" + this.mDeviceShuttingDown + ", mDesiredPowerState=" + this.mDesiredPowerState + ", getRadioState=" + this.mCi.getRadioState() + ", mPowerOffDelayNeed=" + this.mPowerOffDelayNeed + ", mAlarmSwitch=" + this.mAlarmSwitch + ", mRadioDisabledByCarrier=" + this.mRadioDisabledByCarrier;
        log(tmpLog);
        this.mRadioPowerLog.log(tmpLog);
        if (this.mPhone.isPhoneTypeGsm() && this.mAlarmSwitch) {
            log("mAlarmSwitch == true");
            ((AlarmManager) this.mPhone.getContext().getSystemService("alarm")).cancel(this.mRadioOffIntent);
            this.mAlarmSwitch = false;
        }
        if (this.mDesiredPowerState && !this.mRadioDisabledByCarrier && this.mCi.getRadioState() == 0) {
            this.mCi.setRadioPower(true, null);
        } else if ((!this.mDesiredPowerState || this.mRadioDisabledByCarrier) && this.mCi.getRadioState() == 1) {
            if (!this.mPhone.isPhoneTypeGsm() || !this.mPowerOffDelayNeed) {
                powerOffRadioSafely();
            } else if (!this.mImsRegistrationOnOff || this.mAlarmSwitch) {
                powerOffRadioSafely();
            } else {
                log("mImsRegistrationOnOff == true");
                Context context = this.mPhone.getContext();
                this.mRadioOffIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_RADIO_OFF), 0);
                this.mAlarmSwitch = true;
                log("Alarm setting");
                ((AlarmManager) context.getSystemService("alarm")).set(2, SystemClock.elapsedRealtime() + 3000, this.mRadioOffIntent);
            }
        } else if (this.mDeviceShuttingDown && this.mCi.getRadioState() != 2) {
            this.mCi.requestShutdown(null);
        }
    }

    /* access modifiers changed from: protected */
    public void onUpdateIccAvailability() {
        UiccCardApplication newUiccApplication;
        if (this.mUiccController != null && this.mUiccApplcation != (newUiccApplication = getUiccCardApplication())) {
            IccRecords iccRecords = this.mIccRecords;
            if (iccRecords instanceof SIMRecords) {
                this.mCdnr.updateEfFromUsim(null);
            } else if (iccRecords instanceof RuimRecords) {
                this.mCdnr.updateEfFromRuim(null);
            }
            if (this.mUiccApplcation != null) {
                log("Removing stale icc objects.");
                this.mUiccApplcation.unregisterForReady(this);
                IccRecords iccRecords2 = this.mIccRecords;
                if (iccRecords2 != null) {
                    iccRecords2.unregisterForRecordsLoaded(this);
                }
                this.mIccRecords = null;
                this.mUiccApplcation = null;
            }
            if (newUiccApplication != null) {
                log("New card found");
                this.mUiccApplcation = newUiccApplication;
                this.mIccRecords = this.mUiccApplcation.getIccRecords();
                if (this.mPhone.isPhoneTypeGsm()) {
                    this.mUiccApplcation.registerForReady(this, 17, null);
                    IccRecords iccRecords3 = this.mIccRecords;
                    if (iccRecords3 != null) {
                        iccRecords3.registerForRecordsLoaded(this, 16, null);
                    }
                } else if (this.mIsSubscriptionFromRuim) {
                    this.mUiccApplcation.registerForReady(this, 26, null);
                    IccRecords iccRecords4 = this.mIccRecords;
                    if (iccRecords4 != null) {
                        iccRecords4.registerForRecordsLoaded(this, 27, null);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void logRoamingChange() {
        this.mRoamingLog.log(this.mSS.toString());
    }

    /* access modifiers changed from: protected */
    public void logAttachChange() {
        this.mAttachLog.log(this.mSS.toString());
    }

    /* access modifiers changed from: protected */
    public void logPhoneTypeChange() {
        this.mPhoneTypeLog.log(Integer.toString(this.mPhone.getPhoneType()));
    }

    /* access modifiers changed from: protected */
    public void logRatChange() {
        this.mRatLog.log(this.mSS.toString());
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void log(String s) {
        Rlog.d(LOG_TAG, "[" + this.mPhone.getPhoneId() + "] " + s);
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void loge(String s) {
        Rlog.e(LOG_TAG, "[" + this.mPhone.getPhoneId() + "] " + s);
    }

    @UnsupportedAppUsage
    public int getCurrentDataConnectionState() {
        return this.mSS.getDataRegState();
    }

    @UnsupportedAppUsage
    public boolean isConcurrentVoiceAndDataAllowed() {
        if (this.mSS.getCssIndicator() == 1) {
            return true;
        }
        if (!this.mPhone.isPhoneTypeGsm()) {
            return false;
        }
        if (this.mSS.getRilDataRadioTechnology() >= 3) {
            return true;
        }
        return false;
    }

    public void onImsServiceStateChanged() {
        sendMessage(obtainMessage(53));
    }

    public void setImsRegistrationState(boolean registered) {
        log("ImsRegistrationState - registered : " + registered);
        if (!this.mImsRegistrationOnOff || registered || !this.mAlarmSwitch) {
            this.mImsRegistrationOnOff = registered;
            return;
        }
        this.mImsRegistrationOnOff = registered;
        ((AlarmManager) this.mPhone.getContext().getSystemService("alarm")).cancel(this.mRadioOffIntent);
        this.mAlarmSwitch = false;
        sendMessage(obtainMessage(45));
    }

    public void onImsCapabilityChanged() {
        sendMessage(obtainMessage(48));
    }

    public boolean isRadioOn() {
        return this.mCi.getRadioState() == 1;
    }

    @UnsupportedAppUsage
    public void pollState() {
        pollState(false);
    }

    /* access modifiers changed from: protected */
    public void modemTriggeredPollState() {
        pollState(true);
    }

    public void pollState(boolean modemTriggered) {
        this.mPollingContext = new int[1];
        this.mPollingContext[0] = 0;
        log("pollState: modemTriggered=" + modemTriggered);
        int radioState = this.mCi.getRadioState();
        if (radioState == 0) {
            this.mNewSS.setStateOff();
            this.mNewCellIdentity = null;
            setSignalStrengthDefaultValues();
            this.mNitzState.handleNetworkCountryCodeUnavailable();
            if (this.mDeviceShuttingDown || (!modemTriggered && 18 != this.mSS.getRilDataRadioTechnology())) {
                pollStateDone();
                return;
            }
        } else if (radioState == 2) {
            this.mNewSS.setStateOutOfService();
            this.mNewCellIdentity = null;
            setSignalStrengthDefaultValues();
            this.mNitzState.handleNetworkCountryCodeUnavailable();
            pollStateDone();
            return;
        }
        int[] iArr = this.mPollingContext;
        iArr[0] = iArr[0] + 1;
        this.mCi.getOperator(obtainMessage(7, iArr));
        int[] iArr2 = this.mPollingContext;
        iArr2[0] = iArr2[0] + 1;
        this.mRegStateManagers.get(1).requestNetworkRegistrationInfo(2, obtainMessage(5, this.mPollingContext));
        int[] iArr3 = this.mPollingContext;
        iArr3[0] = iArr3[0] + 1;
        this.mRegStateManagers.get(1).requestNetworkRegistrationInfo(1, obtainMessage(4, this.mPollingContext));
        if (this.mRegStateManagers.get(2) != null) {
            int[] iArr4 = this.mPollingContext;
            iArr4[0] = iArr4[0] + 1;
            this.mRegStateManagers.get(2).requestNetworkRegistrationInfo(2, obtainMessage(6, this.mPollingContext));
        }
        if (this.mPhone.isPhoneTypeGsm()) {
            int[] iArr5 = this.mPollingContext;
            iArr5[0] = iArr5[0] + 1;
            this.mCi.getNetworkSelectionMode(obtainMessage(14, iArr5));
        }
    }

    /* JADX INFO: Multiple debug info for r1v16 int: [D('hasRegistered' boolean), D('transport' int)] */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:152:0x02ea, code lost:
        if (r39.mNewSS.getRilDataRadioTechnology() == 13) goto L_0x02ef;
     */
    public void pollStateDone() {
        boolean hasCssIndicatorChanged;
        boolean hasMultiApnSupport;
        boolean has4gHandoff;
        boolean hasLostMultiApnSupport;
        boolean hasDataRoamingOff;
        boolean hasDataRoamingOn;
        boolean hasVoiceRoamingOff;
        TelephonyManager tm;
        boolean z;
        int i;
        boolean has4gHandoff2;
        boolean hasMultiApnSupport2;
        int oldRAT;
        int newRAT;
        boolean anyDataRatChanged;
        int oldRegState;
        int newRegState;
        if (!this.mPhone.isPhoneTypeGsm()) {
            updateRoamingState();
        }
        if (Build.IS_DEBUGGABLE && SystemProperties.getBoolean(PROP_FORCE_ROAMING, false)) {
            this.mNewSS.setVoiceRoaming(true);
            this.mNewSS.setDataRoaming(true);
        }
        useDataRegStateForDataOnlyDevices();
        processIwlanRegistrationInfo();
        if (Build.IS_DEBUGGABLE && this.mPhone.mTelephonyTester != null) {
            this.mPhone.mTelephonyTester.overrideServiceState(this.mNewSS);
        }
        log("Poll ServiceState done:  oldSS=[" + this.mSS + "] newSS=[" + this.mNewSS + "] oldMaxDataCalls=" + this.mMaxDataCalls + " mNewMaxDataCalls=" + this.mNewMaxDataCalls + " oldReasonDataDenied=" + this.mReasonDataDenied + " mNewReasonDataDenied=" + this.mNewReasonDataDenied);
        boolean hasRegistered = this.mSS.getVoiceRegState() != 0 && this.mNewSS.getVoiceRegState() == 0;
        boolean hasDeregistered = this.mSS.getVoiceRegState() == 0 && this.mNewSS.getVoiceRegState() != 0;
        boolean hasAirplaneModeOnChanged = this.mSS.getVoiceRegState() != 3 && this.mNewSS.getVoiceRegState() == 3;
        SparseBooleanArray hasDataAttached = new SparseBooleanArray(this.mTransportManager.getAvailableTransports().length);
        SparseBooleanArray hasDataDetached = new SparseBooleanArray(this.mTransportManager.getAvailableTransports().length);
        SparseBooleanArray hasRilDataRadioTechnologyChanged = new SparseBooleanArray(this.mTransportManager.getAvailableTransports().length);
        SparseBooleanArray hasDataRegStateChanged = new SparseBooleanArray(this.mTransportManager.getAvailableTransports().length);
        boolean anyDataRatChanged2 = false;
        int[] availableTransports = this.mTransportManager.getAvailableTransports();
        int length = availableTransports.length;
        boolean anyDataRegChanged = false;
        int i2 = 0;
        while (i2 < length) {
            int transport = availableTransports[i2];
            NetworkRegistrationInfo oldNrs = this.mSS.getNetworkRegistrationInfo(2, transport);
            NetworkRegistrationInfo newNrs = this.mNewSS.getNetworkRegistrationInfo(2, transport);
            hasDataAttached.put(transport, (oldNrs == null || !oldNrs.isInService() || hasAirplaneModeOnChanged) && newNrs != null && newNrs.isInService());
            hasDataDetached.put(transport, oldNrs != null && oldNrs.isInService() && (newNrs == null || !newNrs.isInService()));
            if (oldNrs != null) {
                oldRAT = oldNrs.getAccessNetworkTechnology();
            } else {
                oldRAT = 0;
            }
            if (newNrs != null) {
                newRAT = newNrs.getAccessNetworkTechnology();
            } else {
                newRAT = 0;
            }
            hasRilDataRadioTechnologyChanged.put(transport, oldRAT != newRAT);
            if (oldRAT != newRAT) {
                anyDataRatChanged = true;
            } else {
                anyDataRatChanged = anyDataRatChanged2;
            }
            if (oldNrs != null) {
                oldRegState = oldNrs.getRegistrationState();
            } else {
                oldRegState = 4;
            }
            if (newNrs != null) {
                newRegState = newNrs.getRegistrationState();
            } else {
                newRegState = 4;
            }
            hasDataRegStateChanged.put(transport, oldRegState != newRegState);
            if (oldRegState != newRegState) {
                anyDataRegChanged = true;
            }
            i2++;
            availableTransports = availableTransports;
            length = length;
            anyDataRatChanged2 = anyDataRatChanged;
        }
        boolean hasVoiceRegStateChanged = this.mSS.getVoiceRegState() != this.mNewSS.getVoiceRegState();
        boolean hasNrFrequencyRangeChanged = this.mSS.getNrFrequencyRange() != this.mNewSS.getNrFrequencyRange();
        boolean hasNrStateChanged = isNrStateChanged(this.mSS.getNetworkRegistrationInfo(2, 3), this.mNewSS.getNetworkRegistrationInfo(2, 3));
        boolean hasLocationChanged = !Objects.equals(this.mNewCellIdentity, this.mCellIdentity);
        if (this.mNewSS.getDataRegState() == 0) {
            this.mRatRatcheter.ratchet(this.mSS, this.mNewSS, hasLocationChanged);
        }
        boolean hasRilVoiceRadioTechnologyChanged = this.mSS.getRilVoiceRadioTechnology() != this.mNewSS.getRilVoiceRadioTechnology();
        boolean hasChanged = !this.mNewSS.equals(this.mSS);
        boolean hasVoiceRoamingOn = !this.mSS.getVoiceRoaming() && this.mNewSS.getVoiceRoaming();
        boolean hasVoiceRoamingOff2 = this.mSS.getVoiceRoaming() && !this.mNewSS.getVoiceRoaming();
        boolean hasDataRoamingOn2 = !this.mSS.getDataRoaming() && this.mNewSS.getDataRoaming();
        boolean hasDataRoamingOff2 = this.mSS.getDataRoaming() && !this.mNewSS.getDataRoaming();
        boolean hasRejectCauseChanged = this.mRejectCode != this.mNewRejectCode;
        boolean hasCssIndicatorChanged2 = this.mSS.getCssIndicator() != this.mNewSS.getCssIndicator();
        if (this.mPhone.isPhoneTypeCdmaLte()) {
            hasCssIndicatorChanged = hasCssIndicatorChanged2;
            boolean has4gHandoff3 = this.mNewSS.getDataRegState() == 0 && ((ServiceState.isLte(this.mSS.getRilDataRadioTechnology()) && this.mNewSS.getRilDataRadioTechnology() == 13) || (this.mSS.getRilDataRadioTechnology() == 13 && ServiceState.isLte(this.mNewSS.getRilDataRadioTechnology())));
            if (!ServiceState.isLte(this.mNewSS.getRilDataRadioTechnology())) {
                has4gHandoff2 = has4gHandoff3;
            } else {
                has4gHandoff2 = has4gHandoff3;
            }
            if (!ServiceState.isLte(this.mSS.getRilDataRadioTechnology()) && this.mSS.getRilDataRadioTechnology() != 13) {
                hasMultiApnSupport2 = true;
                hasMultiApnSupport = hasMultiApnSupport2;
                hasLostMultiApnSupport = this.mNewSS.getRilDataRadioTechnology() < 4 && this.mNewSS.getRilDataRadioTechnology() <= 8;
                has4gHandoff = has4gHandoff2;
            }
            hasMultiApnSupport2 = false;
            hasMultiApnSupport = hasMultiApnSupport2;
            hasLostMultiApnSupport = this.mNewSS.getRilDataRadioTechnology() < 4 && this.mNewSS.getRilDataRadioTechnology() <= 8;
            has4gHandoff = has4gHandoff2;
        } else {
            hasCssIndicatorChanged = hasCssIndicatorChanged2;
            hasMultiApnSupport = false;
            hasLostMultiApnSupport = false;
            has4gHandoff = false;
        }
        log("pollStateDone: hasRegistered = " + hasRegistered + " hasDeregistered = " + hasDeregistered + " hasDataAttached = " + hasDataAttached + " hasDataDetached = " + hasDataDetached + " hasDataRegStateChanged = " + hasDataRegStateChanged + " hasRilVoiceRadioTechnologyChanged = " + hasRilVoiceRadioTechnologyChanged + " hasRilDataRadioTechnologyChanged = " + hasRilDataRadioTechnologyChanged + " hasChanged = " + hasChanged + " hasVoiceRoamingOn = " + hasVoiceRoamingOn + " hasVoiceRoamingOff = " + hasVoiceRoamingOff2 + " hasDataRoamingOn =" + hasDataRoamingOn2 + " hasDataRoamingOff = " + hasDataRoamingOff2 + " hasLocationChanged = " + hasLocationChanged + " has4gHandoff = " + has4gHandoff + " hasMultiApnSupport = " + hasMultiApnSupport + " hasLostMultiApnSupport = " + hasLostMultiApnSupport + " hasCssIndicatorChanged = " + hasCssIndicatorChanged + " hasNrFrequencyRangeChanged = " + hasNrFrequencyRangeChanged + " hasNrStateChanged = " + hasNrStateChanged + " hasAirplaneModeOnlChanged = " + hasAirplaneModeOnChanged);
        if (hasVoiceRegStateChanged || anyDataRegChanged) {
            if (this.mPhone.isPhoneTypeGsm()) {
                i = EventLogTags.GSM_SERVICE_STATE_CHANGE;
            } else {
                i = EventLogTags.CDMA_SERVICE_STATE_CHANGE;
            }
            hasDataRoamingOff = hasDataRoamingOff2;
            hasDataRoamingOn = hasDataRoamingOn2;
            EventLog.writeEvent(i, Integer.valueOf(this.mSS.getVoiceRegState()), Integer.valueOf(this.mSS.getDataRegState()), Integer.valueOf(this.mNewSS.getVoiceRegState()), Integer.valueOf(this.mNewSS.getDataRegState()));
        } else {
            hasDataRoamingOn = hasDataRoamingOn2;
            hasDataRoamingOff = hasDataRoamingOff2;
        }
        if (this.mPhone.isPhoneTypeGsm()) {
            if (hasRilVoiceRadioTechnologyChanged) {
                int cid = getCidFromCellIdentity(this.mNewCellIdentity);
                EventLog.writeEvent((int) EventLogTags.GSM_RAT_SWITCHED_NEW, Integer.valueOf(cid), Integer.valueOf(this.mSS.getRilVoiceRadioTechnology()), Integer.valueOf(this.mNewSS.getRilVoiceRadioTechnology()));
                log("RAT switched " + ServiceState.rilRadioTechnologyToString(this.mSS.getRilVoiceRadioTechnology()) + " -> " + ServiceState.rilRadioTechnologyToString(this.mNewSS.getRilVoiceRadioTechnology()) + " at cell " + cid);
            }
            if (hasCssIndicatorChanged) {
                this.mPhone.notifyDataConnection();
            }
            this.mReasonDataDenied = this.mNewReasonDataDenied;
            this.mMaxDataCalls = this.mNewMaxDataCalls;
            this.mRejectCode = this.mNewRejectCode;
        }
        ServiceState oldMergedSS = new ServiceState(this.mPhone.getServiceState());
        ServiceState tss = this.mSS;
        this.mSS = this.mNewSS;
        this.mNewSS = tss;
        this.mNewSS.setStateOutOfService();
        CellIdentity tempCellId = this.mCellIdentity;
        this.mCellIdentity = this.mNewCellIdentity;
        this.mNewCellIdentity = tempCellId;
        if (hasRilVoiceRadioTechnologyChanged) {
            updatePhoneObject();
        }
        TelephonyManager tm2 = (TelephonyManager) this.mPhone.getContext().getSystemService("phone");
        if (anyDataRatChanged2) {
            tm2.setDataNetworkTypeForPhone(this.mPhone.getPhoneId(), this.mSS.getRilDataRadioTechnology());
            hasVoiceRoamingOff = hasVoiceRoamingOff2;
            StatsLog.write(76, ServiceState.rilRadioTechnologyToNetworkType(this.mSS.getRilDataRadioTechnology()), this.mPhone.getPhoneId());
        } else {
            hasVoiceRoamingOff = hasVoiceRoamingOff2;
        }
        if (hasRegistered) {
            this.mNetworkAttachedRegistrants.notifyRegistrants();
            mtkIvsrUpdateCsPlmn();
            this.mNitzState.handleNetworkAvailable();
        }
        if (hasDeregistered) {
            this.mNetworkDetachedRegistrants.notifyRegistrants();
        }
        if (hasRejectCauseChanged) {
            setNotification(2001);
        }
        if (hasChanged) {
            updateSpnDisplay();
            tm2.setNetworkOperatorNameForPhone(this.mPhone.getPhoneId(), this.mSS.getOperatorAlpha());
            String operatorNumeric = this.mSS.getOperatorNumeric();
            if (!this.mPhone.isPhoneTypeGsm() && isInvalidOperatorNumeric(operatorNumeric)) {
                operatorNumeric = fixUnknownMcc(operatorNumeric, this.mSS.getCdmaSystemId());
            }
            tm2.setNetworkOperatorNumericForPhone(this.mPhone.getPhoneId(), operatorNumeric);
            if (isInvalidOperatorNumeric(operatorNumeric)) {
                log("operatorNumeric " + operatorNumeric + " is invalid");
                this.mLocaleTracker.updateOperatorNumeric(PhoneConfigurationManager.SSSS);
            } else if (this.mSS.getRilDataRadioTechnology() != 18) {
                if (!this.mPhone.isPhoneTypeGsm()) {
                    setOperatorIdd(operatorNumeric);
                }
                this.mLocaleTracker.updateOperatorNumeric(operatorNumeric);
            }
            int phoneId = this.mPhone.getPhoneId();
            if (this.mPhone.isPhoneTypeGsm()) {
                z = this.mSS.getVoiceRoaming();
            } else {
                z = this.mSS.getVoiceRoaming() || this.mSS.getDataRoaming();
            }
            tm2.setNetworkRoamingForPhone(phoneId, z);
            setRoamingType(this.mSS);
            log("Broadcasting ServiceState : " + this.mSS);
            if (!oldMergedSS.equals(this.mPhone.getServiceState())) {
                GsmCdmaPhone gsmCdmaPhone = this.mPhone;
                gsmCdmaPhone.notifyServiceStateChanged(gsmCdmaPhone.getServiceState());
            }
            this.mPhone.getContext().getContentResolver().insert(Telephony.ServiceStateTable.getUriForSubscriptionId(this.mPhone.getSubId()), Telephony.ServiceStateTable.getContentValuesForServiceState(this.mSS));
            TelephonyMetrics.getInstance().writeServiceStateChanged(this.mPhone.getPhoneId(), this.mSS);
        }
        boolean shouldLogAttachedChange = false;
        boolean shouldLogRatChange = false;
        if (hasRegistered || hasDeregistered) {
            shouldLogAttachedChange = true;
        }
        if (has4gHandoff) {
            mtkIvsrUpdatePsPlmn();
            this.mAttachedRegistrants.get(1).notifyRegistrants();
            shouldLogAttachedChange = true;
        }
        if (hasRilVoiceRadioTechnologyChanged) {
            shouldLogRatChange = true;
            notifySignalStrength();
        }
        int[] availableTransports2 = this.mTransportManager.getAvailableTransports();
        int length2 = availableTransports2.length;
        boolean shouldLogRatChange2 = shouldLogRatChange;
        boolean shouldLogAttachedChange2 = shouldLogAttachedChange;
        int i3 = 0;
        while (i3 < length2) {
            int transport2 = availableTransports2[i3];
            if (hasRilDataRadioTechnologyChanged.get(transport2)) {
                shouldLogRatChange2 = true;
                notifySignalStrength();
            }
            if (hasDataRegStateChanged.get(transport2) || hasRilDataRadioTechnologyChanged.get(transport2)) {
                notifyDataRegStateRilRadioTechnologyChanged(transport2);
                tm = tm2;
                this.mPhone.notifyDataConnection();
            } else {
                tm = tm2;
            }
            if (hasDataAttached.get(transport2)) {
                shouldLogAttachedChange2 = true;
                if (this.mAttachedRegistrants.get(transport2) != null) {
                    this.mAttachedRegistrants.get(transport2).notifyRegistrants();
                }
            }
            if (hasDataDetached.get(transport2)) {
                if (this.mDetachedRegistrants.get(transport2) != null) {
                    this.mDetachedRegistrants.get(transport2).notifyRegistrants();
                }
                shouldLogAttachedChange2 = true;
            }
            i3++;
            hasRegistered = hasRegistered;
            tm2 = tm;
        }
        if (shouldLogAttachedChange2) {
            logAttachChange();
        }
        if (shouldLogRatChange2) {
            logRatChange();
        }
        if (hasVoiceRegStateChanged || hasRilVoiceRadioTechnologyChanged) {
            notifyVoiceRegStateRilRadioTechnologyChanged();
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
            this.mPhone.notifyLocationChanged(getCellLocation());
        }
        if (!this.mPhone.isPhoneTypeGsm()) {
            return;
        }
        if (isGprsConsistent(this.mSS.getDataRegState(), this.mSS.getVoiceRegState())) {
            this.mReportedGprsNoReg = false;
        } else if (!this.mStartedGprsRegCheck && !this.mReportedGprsNoReg) {
            this.mStartedGprsRegCheck = true;
            sendMessageDelayed(obtainMessage(22), (long) Settings.Global.getInt(this.mPhone.getContext().getContentResolver(), "gprs_register_check_period_ms", DEFAULT_GPRS_CHECK_PERIOD_MILLIS));
        }
    }

    /* access modifiers changed from: protected */
    public String getOperatorNameFromEri() {
        String eriText = null;
        if (this.mPhone.isPhoneTypeCdma()) {
            if (this.mCi.getRadioState() != 1 || this.mIsSubscriptionFromRuim) {
                return null;
            }
            if (this.mSS.getVoiceRegState() == 0) {
                return this.mPhone.getCdmaEriText();
            }
            return this.mPhone.getContext().getText(17040956).toString();
        } else if (!this.mPhone.isPhoneTypeCdmaLte()) {
            return null;
        } else {
            if (!((this.mUiccController.getUiccCard(getPhoneId()) == null || this.mUiccController.getUiccCard(getPhoneId()).getOperatorBrandOverride() == null) ? false : true) && this.mCi.getRadioState() == 1 && this.mEriManager.isEriFileLoaded() && (!ServiceState.isLte(this.mSS.getRilVoiceRadioTechnology()) || this.mPhone.getContext().getResources().getBoolean(17891336))) {
                eriText = this.mSS.getOperatorAlpha();
                if (this.mSS.getVoiceRegState() == 0) {
                    eriText = this.mPhone.getCdmaEriText();
                } else if (this.mSS.getVoiceRegState() == 3) {
                    eriText = getServiceProviderName();
                    if (TextUtils.isEmpty(eriText)) {
                        eriText = SystemProperties.get("ro.cdma.home.operator.alpha");
                    }
                } else if (this.mSS.getDataRegState() != 0) {
                    eriText = this.mPhone.getContext().getText(17040956).toString();
                }
            }
            UiccCardApplication uiccCardApplication = this.mUiccApplcation;
            if (uiccCardApplication == null || uiccCardApplication.getState() != IccCardApplicationStatus.AppState.APPSTATE_READY || this.mIccRecords == null || getCombinedRegState(this.mSS) != 0 || ServiceState.isLte(this.mSS.getRilVoiceRadioTechnology())) {
                return eriText;
            }
            boolean showSpn = ((RuimRecords) this.mIccRecords).getCsimSpnDisplayCondition();
            int iconIndex = this.mSS.getCdmaEriIconIndex();
            if (!showSpn || iconIndex != 1 || !isInHomeSidNid(this.mSS.getCdmaSystemId(), this.mSS.getCdmaNetworkId()) || this.mIccRecords == null) {
                return eriText;
            }
            return getServiceProviderName();
        }
    }

    public String getServiceProviderName() {
        String operatorBrandOverride = getOperatorBrandOverride();
        if (!TextUtils.isEmpty(operatorBrandOverride)) {
            return operatorBrandOverride;
        }
        IccRecords iccRecords = this.mIccRecords;
        String carrierName = iccRecords != null ? iccRecords.getServiceProviderName() : PhoneConfigurationManager.SSSS;
        PersistableBundle config = getCarrierConfig();
        if (config.getBoolean("carrier_name_override_bool") || TextUtils.isEmpty(carrierName)) {
            return config.getString("carrier_name_string");
        }
        return carrierName;
    }

    public int getCarrierNameDisplayBitmask(ServiceState ss) {
        boolean isRoaming;
        PersistableBundle config = getCarrierConfig();
        if (!TextUtils.isEmpty(getOperatorBrandOverride())) {
            return 1;
        }
        if (TextUtils.isEmpty(getServiceProviderName())) {
            return 2;
        }
        boolean useRoamingFromServiceState = config.getBoolean("spn_display_rule_use_roaming_from_service_state_bool");
        IccRecords iccRecords = this.mIccRecords;
        int carrierDisplayNameConditionFromSim = iccRecords == null ? 0 : iccRecords.getCarrierNameDisplayCondition();
        if (useRoamingFromServiceState) {
            isRoaming = ss.getRoaming();
        } else {
            IccRecords iccRecords2 = this.mIccRecords;
            isRoaming = !ArrayUtils.contains(iccRecords2 != null ? iccRecords2.getHomePlmns() : null, ss.getOperatorNumeric());
        }
        if (isRoaming) {
            if ((carrierDisplayNameConditionFromSim & 2) == 2) {
                return 2 | 1;
            }
            return 2;
        } else if ((carrierDisplayNameConditionFromSim & 1) == 1) {
            return 1 | 2;
        } else {
            return 1;
        }
    }

    /* access modifiers changed from: protected */
    public String getOperatorBrandOverride() {
        UiccProfile profile;
        UiccCard card = this.mPhone.getUiccCard();
        if (card == null || (profile = card.getUiccProfile()) == null) {
            return null;
        }
        return profile.getOperatorBrandOverride();
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public boolean isInHomeSidNid(int sid, int nid) {
        if (isSidsAllZeros() || this.mHomeSystemId.length != this.mHomeNetworkId.length || sid == 0) {
            return true;
        }
        int i = 0;
        while (true) {
            int[] iArr = this.mHomeSystemId;
            if (i >= iArr.length) {
                return false;
            }
            if (iArr[i] == sid) {
                int[] iArr2 = this.mHomeNetworkId;
                if (iArr2[i] == 0 || iArr2[i] == 65535 || nid == 0 || nid == 65535 || iArr2[i] == nid) {
                    return true;
                }
            }
            i++;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void setOperatorIdd(String operatorNumeric) {
        try {
            String idd = this.mHbpcdUtils.getIddByMcc(Integer.parseInt(operatorNumeric.substring(0, 3)));
            if (idd == null || idd.isEmpty()) {
                this.mPhone.setGlobalSystemProperty("gsm.operator.idpstring", "+");
            } else {
                this.mPhone.setGlobalSystemProperty("gsm.operator.idpstring", idd);
            }
        } catch (NumberFormatException e) {
            Rlog.d(LOG_TAG, e.toString());
        } catch (Exception e2) {
            Rlog.d(LOG_TAG, e2.toString());
        }
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public boolean isInvalidOperatorNumeric(String operatorNumeric) {
        return operatorNumeric == null || operatorNumeric.length() < 5 || operatorNumeric.startsWith(INVALID_MCC);
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public String fixUnknownMcc(String operatorNumeric, int sid) {
        boolean isNitzTimeZone;
        TimeZone tzone;
        TimeZone tzone2;
        int simState = TelephonyManager.getDefault().getSimState(SubscriptionManager.getSlotIndex(this.mPhone.getSubId()));
        log("fixUnknownMcc simState == " + simState + " slotid == " + SubscriptionManager.getSlotIndex(this.mPhone.getSubId()));
        if (sid <= 0 || simState != 5) {
            return operatorNumeric;
        }
        if (this.mNitzState.getSavedTimeZoneId() != null) {
            tzone = TimeZone.getTimeZone(this.mNitzState.getSavedTimeZoneId());
            isNitzTimeZone = true;
        } else {
            NitzData lastNitzData = this.mNitzState.getCachedNitzData();
            if (lastNitzData == null) {
                tzone2 = null;
            } else {
                tzone2 = TimeZoneLookupHelper.guessZoneByNitzStatic(lastNitzData);
                StringBuilder sb = new StringBuilder();
                sb.append("fixUnknownMcc(): guessNitzTimeZone returned ");
                sb.append((Object) (tzone2 == null ? tzone2 : tzone2.getID()));
                log(sb.toString());
            }
            isNitzTimeZone = false;
            tzone = tzone2;
        }
        int utcOffsetHours = 0;
        if (tzone != null) {
            utcOffsetHours = tzone.getRawOffset() / MS_PER_HOUR;
        }
        NitzData nitzData = this.mNitzState.getCachedNitzData();
        int i = 1;
        boolean isDst = nitzData != null && nitzData.isDst();
        HbpcdUtils hbpcdUtils = this.mHbpcdUtils;
        if (!isDst) {
            i = 0;
        }
        int mcc = hbpcdUtils.getMcc(sid, utcOffsetHours, i, isNitzTimeZone);
        if (mcc <= 0) {
            return operatorNumeric;
        }
        return Integer.toString(mcc) + DEFAULT_MNC;
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public boolean isGprsConsistent(int dataRegState, int voiceRegState) {
        return voiceRegState != 0 || dataRegState == 0;
    }

    /* access modifiers changed from: protected */
    public int regCodeToServiceState(int code) {
        if (code == 1 || code == 5) {
            return 0;
        }
        return 1;
    }

    /* access modifiers changed from: protected */
    public boolean regCodeIsRoaming(int code) {
        return 5 == code;
    }

    private boolean isSameOperatorNameFromSimAndSS(ServiceState s) {
        String spn = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getSimOperatorNameForPhone(getPhoneId());
        return (!TextUtils.isEmpty(spn) && spn.equalsIgnoreCase(s.getOperatorAlphaLong())) || (!TextUtils.isEmpty(spn) && spn.equalsIgnoreCase(s.getOperatorAlphaShort()));
    }

    /* access modifiers changed from: protected */
    public boolean isSameNamedOperators(ServiceState s) {
        return currentMccEqualsSimMcc(s) && isSameOperatorNameFromSimAndSS(s);
    }

    /* access modifiers changed from: protected */
    public boolean currentMccEqualsSimMcc(ServiceState s) {
        try {
            return ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getSimOperatorNumericForPhone(getPhoneId()).substring(0, 3).equals(s.getOperatorNumeric().substring(0, 3));
        } catch (Exception e) {
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public boolean isOperatorConsideredNonRoaming(ServiceState s) {
        String operatorNumeric = s.getOperatorNumeric();
        String[] numericArray = getCarrierConfig().getStringArray("non_roaming_operator_string_array");
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

    /* access modifiers changed from: protected */
    public boolean isOperatorConsideredRoaming(ServiceState s) {
        String simNumeric;
        String simNumeric2;
        String operatorNumeric = s.getOperatorNumeric();
        String[] numericArray = getCarrierConfig().getStringArray("roaming_operator_string_array");
        try {
            if (!(this.mIccRecords == null || (simNumeric2 = this.mIccRecords.getOperatorNumeric()) == null || !"60506".equals(simNumeric2.substring(0, 5)))) {
                log("for Lycamobile sim, Considered as Roaming");
                return true;
            }
        } catch (Exception e) {
            loge("getOperatorNumeric Exception in isOperatorConsideredRoaming " + e);
        }
        if (ArrayUtils.isEmpty(numericArray) || operatorNumeric == null) {
            try {
                if (!(this.mIccRecords == null || (simNumeric = this.mIccRecords.getOperatorNumeric()) == null || (!"404".equals(simNumeric.substring(0, 3)) && !"405".equals(simNumeric.substring(0, 3))))) {
                    log("the config is null, for India sim, Considered as Roaming");
                    return true;
                }
            } catch (Exception e2) {
                loge("getOperatorNumeric Exception in isOperatorConsideredRoaming " + e2);
            }
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
        RestrictedState newRs = new RestrictedState();
        log("onRestrictedStateChanged: E rs " + this.mRestrictedState);
        if (ar.exception == null && ar.result != null) {
            int state = ((Integer) ar.result).intValue();
            boolean z = false;
            newRs.setCsEmergencyRestricted(((state & 1) == 0 && (state & 4) == 0) ? false : true);
            UiccCardApplication uiccCardApplication = this.mUiccApplcation;
            if (uiccCardApplication != null && uiccCardApplication.getState() == IccCardApplicationStatus.AppState.APPSTATE_READY) {
                newRs.setCsNormalRestricted(((state & 2) == 0 && (state & 4) == 0) ? false : true);
                if ((state & 16) != 0) {
                    z = true;
                }
                newRs.setPsRestricted(z);
            }
            log("onRestrictedStateChanged: new rs " + newRs);
            if (!this.mRestrictedState.isPsRestricted() && newRs.isPsRestricted()) {
                this.mPsRestrictEnabledRegistrants.notifyRegistrants();
                setNotification(1001);
            } else if (this.mRestrictedState.isPsRestricted() && !newRs.isPsRestricted()) {
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
            } else if (!this.mRestrictedState.isCsEmergencyRestricted() || this.mRestrictedState.isCsNormalRestricted()) {
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

    public CellLocation getCellLocation() {
        CellIdentity cellIdentity = this.mCellIdentity;
        if (cellIdentity != null) {
            return cellIdentity.asCellLocation();
        }
        CellLocation cl = getCellLocationFromCellInfo(getAllCellInfo());
        if (cl != null) {
            return cl;
        }
        return this.mPhone.getPhoneType() == 2 ? new CdmaCellLocation() : new GsmCellLocation();
    }

    public void requestCellLocation(WorkSource workSource, Message rspMsg) {
        CellIdentity cellIdentity = this.mCellIdentity;
        if (cellIdentity != null) {
            AsyncResult.forMessage(rspMsg, cellIdentity.asCellLocation(), (Throwable) null);
            rspMsg.sendToTarget();
            return;
        }
        requestAllCellInfo(workSource, obtainMessage(56, rspMsg));
    }

    private static CellLocation getCellLocationFromCellInfo(List<CellInfo> info) {
        CellLocation cl = null;
        if (info == null || info.size() <= 0) {
            return null;
        }
        CellIdentity fallbackLteCid = null;
        Iterator<CellInfo> it = info.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            CellIdentity c = it.next().getCellIdentity();
            if (!(c instanceof CellIdentityLte) || fallbackLteCid != null) {
                if (getCidFromCellIdentity(c) != -1) {
                    cl = c.asCellLocation();
                    break;
                }
            } else if (getCidFromCellIdentity(c) != -1) {
                fallbackLteCid = c;
            }
        }
        if (cl != null || fallbackLteCid == null) {
            return cl;
        }
        return fallbackLteCid.asCellLocation();
    }

    @Override // com.android.internal.telephony.AbstractServiceStateTracker
    public void oppoAddDataCallCount() {
        super.oppoAddDataCallCount();
    }

    @Override // com.android.internal.telephony.AbstractServiceStateTracker
    public void oppoAddSmsSendCount() {
        super.oppoAddSmsSendCount();
    }

    /* access modifiers changed from: protected */
    public void setTimeFromNITZString(String nitzString, long nitzReceiveTime) {
        if (super.isNeedupdateNitzTime()) {
            super.oppoAddNitzCount();
            long start = SystemClock.elapsedRealtime();
            Rlog.d(LOG_TAG, "NITZ: " + nitzString + "," + nitzReceiveTime + " start=" + start + " delay=" + (start - nitzReceiveTime));
            NitzData newNitzData = NitzData.parse(nitzString);
            if (newNitzData != null) {
                try {
                    this.mNitzState.handleNitzReceived(new TimestampedValue<>(nitzReceiveTime, newNitzData));
                } finally {
                    long end = SystemClock.elapsedRealtime();
                    Rlog.d(LOG_TAG, "NITZ: end=" + end + " dur=" + (end - start));
                }
            }
        }
    }

    private void cancelAllNotifications() {
        log("cancelAllNotifications: mPrevSubId=" + this.mPrevSubId);
        NotificationManager notificationManager = (NotificationManager) this.mPhone.getContext().getSystemService("notification");
        if (SubscriptionManager.isValidSubscriptionId(this.mPrevSubId)) {
            notificationManager.cancel(Integer.toString(this.mPrevSubId), PS_NOTIFICATION);
            notificationManager.cancel(Integer.toString(this.mPrevSubId), CS_NOTIFICATION);
            notificationManager.cancel(Integer.toString(this.mPrevSubId), 111);
        }
    }

    /* JADX INFO: Multiple debug info for r3v23 long: [D('info' android.telephony.SubscriptionInfo), D('dataSubId' long)] */
    @VisibleForTesting
    public void setNotification(int notifyType) {
        CharSequence charSequence;
        CharSequence charSequence2;
        CharSequence charSequence3;
        CharSequence charSequence4;
        int notifyType2 = notifyType;
        log("setNotification: create notification " + notifyType2);
        if (!SubscriptionManager.isValidSubscriptionId(this.mSubId)) {
            loge("cannot setNotification on invalid subid mSubId=" + this.mSubId);
            return;
        }
        Context context = this.mPhone.getContext();
        SubscriptionInfo info = this.mSubscriptionController.getActiveSubscriptionInfo(this.mPhone.getSubId(), context.getOpPackageName());
        if (info != null) {
            if (!info.isOpportunistic() || info.getGroupUuid() == null) {
                if (!context.getResources().getBoolean(17891570)) {
                    log("Ignore all the notifications");
                    return;
                }
                PersistableBundle bundle = getCarrierConfig();
                if (!bundle.getBoolean("disable_voice_barring_notification_bool", false) || !(notifyType2 == 1003 || notifyType2 == 1005 || notifyType2 == 1006)) {
                    boolean autoCancelCsRejectNotification = bundle.getBoolean("carrier_auto_cancel_cs_notification", false);
                    CharSequence details = PhoneConfigurationManager.SSSS;
                    CharSequence title = PhoneConfigurationManager.SSSS;
                    int notificationId = CS_NOTIFICATION;
                    int icon = 17301642;
                    boolean multipleSubscriptions = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getPhoneCount() > 1;
                    int simNumber = this.mSubscriptionController.getSlotIndex(this.mSubId) + 1;
                    if (notifyType2 != 2001) {
                        switch (notifyType2) {
                            case 1001:
                                if (((long) SubscriptionManager.getDefaultDataSubscriptionId()) == ((long) this.mPhone.getSubId())) {
                                    notificationId = PS_NOTIFICATION;
                                    title = context.getText(17039422);
                                    if (multipleSubscriptions) {
                                        charSequence = context.getString(17039426, Integer.valueOf(simNumber));
                                    } else {
                                        charSequence = context.getText(17039425);
                                    }
                                    details = charSequence;
                                    break;
                                } else {
                                    return;
                                }
                            case 1002:
                                notificationId = PS_NOTIFICATION;
                                break;
                            case 1003:
                                title = context.getText(17039421);
                                if (multipleSubscriptions) {
                                    charSequence2 = context.getString(17039426, Integer.valueOf(simNumber));
                                } else {
                                    charSequence2 = context.getText(17039425);
                                }
                                details = charSequence2;
                                break;
                            case 1005:
                                title = context.getText(17039424);
                                if (multipleSubscriptions) {
                                    charSequence3 = context.getString(17039426, Integer.valueOf(simNumber));
                                } else {
                                    charSequence3 = context.getText(17039425);
                                }
                                details = charSequence3;
                                break;
                            case 1006:
                                title = context.getText(17039423);
                                if (multipleSubscriptions) {
                                    charSequence4 = context.getString(17039426, Integer.valueOf(simNumber));
                                } else {
                                    charSequence4 = context.getText(17039425);
                                }
                                details = charSequence4;
                                break;
                        }
                    } else {
                        notificationId = 111;
                        int resId = selectResourceForRejectCode(this.mRejectCode, multipleSubscriptions);
                        if (resId != 0) {
                            icon = 17303514;
                            title = context.getString(resId, Integer.valueOf(simNumber));
                            details = null;
                        } else if (autoCancelCsRejectNotification) {
                            notifyType2 = 2002;
                        } else {
                            loge("setNotification: mRejectCode=" + this.mRejectCode + " is not handled.");
                            return;
                        }
                    }
                    log("setNotification, create notification, notifyType: " + notifyType2 + ", title: " + ((Object) title) + ", details: " + ((Object) details) + ", subId: " + this.mSubId);
                    this.mNotification = new Notification.Builder(context).setWhen(System.currentTimeMillis()).setAutoCancel(true).setSmallIcon(icon).setTicker(title).setColor(context.getResources().getColor(17170460)).setContentTitle(title).setStyle(new Notification.BigTextStyle().bigText(details)).setContentText(details).setChannel(NotificationChannelController.CHANNEL_ID_ALERT).build();
                    NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
                    if (notifyType2 == 1002 || notifyType2 == 1004 || notifyType2 == 2002) {
                        notificationManager.cancel(Integer.toString(this.mSubId), notificationId);
                        return;
                    }
                    boolean show = false;
                    if (this.mSS.isEmergencyOnly() && notifyType2 == 1006) {
                        show = true;
                    } else if (notifyType2 == 2001) {
                        show = true;
                    } else if (this.mSS.getState() == 0) {
                        show = true;
                    }
                    if (show) {
                        notificationManager.notify(Integer.toString(this.mSubId), notificationId, this.mNotification);
                        return;
                    }
                    return;
                }
                log("Voice/emergency call barred notification disabled");
                return;
            }
        }
        log("cannot setNotification on invisible subid mSubId=" + this.mSubId);
    }

    private int selectResourceForRejectCode(int rejCode, boolean multipleSubscriptions) {
        int rejResourceId;
        int rejResourceId2;
        int rejResourceId3;
        int rejResourceId4;
        if (rejCode == 1) {
            if (multipleSubscriptions) {
                rejResourceId = 17040437;
            } else {
                rejResourceId = 17040436;
            }
            return rejResourceId;
        } else if (rejCode == 2) {
            if (multipleSubscriptions) {
                rejResourceId2 = 17040443;
            } else {
                rejResourceId2 = 17040442;
            }
            return rejResourceId2;
        } else if (rejCode == 3) {
            if (multipleSubscriptions) {
                rejResourceId3 = 17040441;
            } else {
                rejResourceId3 = 17040440;
            }
            return rejResourceId3;
        } else if (rejCode != 6) {
            return 0;
        } else {
            if (multipleSubscriptions) {
                rejResourceId4 = 17040439;
            } else {
                rejResourceId4 = 17040438;
            }
            return rejResourceId4;
        }
    }

    /* access modifiers changed from: protected */
    public UiccCardApplication getUiccCardApplication() {
        if (this.mPhone.isPhoneTypeGsm()) {
            return this.mUiccController.getUiccCardApplication(this.mPhone.getPhoneId(), 1);
        }
        return this.mUiccController.getUiccCardApplication(this.mPhone.getPhoneId(), 2);
    }

    /* access modifiers changed from: protected */
    public void queueNextSignalStrengthPoll() {
        if (!this.mDontPollSignalStrength) {
            UiccCard uiccCard = UiccController.getInstance().getUiccCard(getPhoneId());
            if (uiccCard == null || uiccCard.getCardState() == IccCardStatus.CardState.CARDSTATE_ABSENT) {
                log("Not polling signal strength due to absence of SIM");
                return;
            }
            Message msg = obtainMessage();
            msg.what = 10;
            sendMessageDelayed(msg, 20000);
        }
    }

    private void notifyCdmaSubscriptionInfoReady() {
        if (this.mCdmaForSubscriptionInfoReadyRegistrants != null) {
            log("CDMA_SUBSCRIPTION: call notifyRegistrants()");
            this.mCdmaForSubscriptionInfoReadyRegistrants.notifyRegistrants();
        }
    }

    public void registerForDataConnectionAttached(int transport, Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        if (this.mAttachedRegistrants.get(transport) == null) {
            this.mAttachedRegistrants.put(transport, new RegistrantList());
        }
        this.mAttachedRegistrants.get(transport).add(r);
        ServiceState serviceState = this.mSS;
        if (serviceState != null) {
            NetworkRegistrationInfo netRegState = serviceState.getNetworkRegistrationInfo(2, transport);
            if (netRegState == null || netRegState.isInService()) {
                r.notifyRegistrant();
            }
        }
    }

    public void unregisterForDataConnectionAttached(int transport, Handler h) {
        if (this.mAttachedRegistrants.get(transport) != null) {
            this.mAttachedRegistrants.get(transport).remove(h);
        }
    }

    public void registerForDataConnectionDetached(int transport, Handler h, int what, Object obj) {
        NetworkRegistrationInfo netRegState;
        Registrant r = new Registrant(h, what, obj);
        if (this.mDetachedRegistrants.get(transport) == null) {
            this.mDetachedRegistrants.put(transport, new RegistrantList());
        }
        this.mDetachedRegistrants.get(transport).add(r);
        ServiceState serviceState = this.mSS;
        if (serviceState != null && (netRegState = serviceState.getNetworkRegistrationInfo(2, transport)) != null && !netRegState.isInService()) {
            r.notifyRegistrant();
        }
    }

    public void unregisterForDataConnectionDetached(int transport, Handler h) {
        if (this.mDetachedRegistrants.get(transport) != null) {
            this.mDetachedRegistrants.get(transport).remove(h);
        }
    }

    public void registerForVoiceRegStateOrRatChanged(Handler h, int what, Object obj) {
        this.mVoiceRegStateOrRatChangedRegistrants.add(new Registrant(h, what, obj));
        notifyVoiceRegStateRilRadioTechnologyChanged();
    }

    public void unregisterForVoiceRegStateOrRatChanged(Handler h) {
        this.mVoiceRegStateOrRatChangedRegistrants.remove(h);
    }

    public void registerForDataRegStateOrRatChanged(int transport, Handler h, int what, Object obj) {
        Registrant r = new Registrant(h, what, obj);
        if (this.mDataRegStateOrRatChangedRegistrants.get(transport) == null) {
            this.mDataRegStateOrRatChangedRegistrants.put(transport, new RegistrantList());
        }
        this.mDataRegStateOrRatChangedRegistrants.get(transport).add(r);
        notifyDataRegStateRilRadioTechnologyChanged(transport);
    }

    public void unregisterForDataRegStateOrRatChanged(int transport, Handler h) {
        if (this.mDataRegStateOrRatChangedRegistrants.get(transport) != null) {
            this.mDataRegStateOrRatChangedRegistrants.get(transport).remove(h);
        }
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

    public void registerForImsCapabilityChanged(Handler h, int what, Object obj) {
        this.mImsCapabilityChangedRegistrants.add(new Registrant(h, what, obj));
    }

    public void unregisterForImsCapabilityChanged(Handler h) {
        this.mImsCapabilityChangedRegistrants.remove(h);
    }

    public void powerOffRadioSafely() {
        synchronized (this) {
            if (!this.mPendingRadioPowerOffAfterDataOff) {
                int dds = mtkReplaceDdsIfUnset(SubscriptionManager.getDefaultDataSubscriptionId());
                if (!this.mPhone.areAllDataDisconnected() || (dds != this.mPhone.getSubId() && (dds == this.mPhone.getSubId() || !ProxyController.getInstance().areAllDataDisconnected(dds)))) {
                    if (this.mPhone.isPhoneTypeGsm() && this.mPhone.isInCall()) {
                        this.mPhone.mCT.mRingingCall.hangupIfAlive();
                        this.mPhone.mCT.mBackgroundCall.hangupIfAlive();
                        this.mPhone.mCT.mForegroundCall.hangupIfAlive();
                    }
                    hangupAllImsCall();
                    int[] availableTransports = this.mTransportManager.getAvailableTransports();
                    for (int transport : availableTransports) {
                        if (this.mPhone.getDcTracker(transport) != null) {
                            this.mPhone.getDcTracker(transport).cleanUpAllConnections(PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
                        }
                    }
                    if (dds != this.mPhone.getSubId() && !ProxyController.getInstance().areAllDataDisconnected(dds)) {
                        log("Data is active on DDS.  Wait for all data disconnect");
                        ProxyController.getInstance().registerForAllDataDisconnected(dds, this, 49);
                        this.mPendingRadioPowerOffAfterDataOff = true;
                        mtkRegisterAllDataDisconnected();
                    }
                    Message msg = Message.obtain(this);
                    msg.what = 38;
                    int i = this.mPendingRadioPowerOffAfterDataOffTag + 1;
                    this.mPendingRadioPowerOffAfterDataOffTag = i;
                    msg.arg1 = i;
                    if (sendMessageDelayed(msg, (long) mtkReplaceDisconnectTimer())) {
                        log("Wait upto 30s for data to disconnect, then turn off radio.");
                        this.mPendingRadioPowerOffAfterDataOff = true;
                    } else {
                        log("Cannot send delayed Msg, turn off radio right away.");
                        hangupAndPowerOff();
                        this.mPendingRadioPowerOffAfterDataOff = false;
                    }
                } else {
                    int[] availableTransports2 = this.mTransportManager.getAvailableTransports();
                    for (int transport2 : availableTransports2) {
                        if (this.mPhone.getDcTracker(transport2) != null) {
                            this.mPhone.getDcTracker(transport2).cleanUpAllConnections(PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
                        }
                    }
                    log("Data disconnected, turn off radio right away.");
                    hangupAndPowerOff();
                }
            }
        }
    }

    public boolean processPendingRadioPowerOffAfterDataOff() {
        synchronized (this) {
            if (!this.mPendingRadioPowerOffAfterDataOff) {
                return false;
            }
            log("Process pending request to turn radio off.");
            this.mPendingRadioPowerOffAfterDataOffTag++;
            hangupAndPowerOff();
            this.mPendingRadioPowerOffAfterDataOff = false;
            return true;
        }
    }

    private boolean containsEarfcnInEarfcnRange(ArrayList<Pair<Integer, Integer>> earfcnPairList, int earfcn) {
        if (earfcnPairList == null) {
            return false;
        }
        Iterator<Pair<Integer, Integer>> it = earfcnPairList.iterator();
        while (it.hasNext()) {
            Pair<Integer, Integer> earfcnPair = it.next();
            if (earfcn >= ((Integer) earfcnPair.first).intValue() && earfcn <= ((Integer) earfcnPair.second).intValue()) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public ArrayList<Pair<Integer, Integer>> convertEarfcnStringArrayToPairList(String[] earfcnsList) {
        int earfcnStart;
        int earfcnEnd;
        ArrayList<Pair<Integer, Integer>> earfcnPairList = new ArrayList<>();
        if (earfcnsList != null) {
            int i = 0;
            while (i < earfcnsList.length) {
                try {
                    String[] earfcns = earfcnsList[i].split("-");
                    if (earfcns.length != 2 || (earfcnStart = Integer.parseInt(earfcns[0])) > (earfcnEnd = Integer.parseInt(earfcns[1]))) {
                        return null;
                    }
                    earfcnPairList.add(new Pair<>(Integer.valueOf(earfcnStart), Integer.valueOf(earfcnEnd)));
                    i++;
                } catch (PatternSyntaxException e) {
                    return null;
                } catch (NumberFormatException e2) {
                    return null;
                } catch (Exception e3) {
                    Rlog.d(LOG_TAG, e3.toString());
                    return null;
                }
            }
        }
        return earfcnPairList;
    }

    /* access modifiers changed from: protected */
    public void onCarrierConfigChanged() {
        PersistableBundle config = getCarrierConfig();
        log("CarrierConfigChange " + config);
        this.mEriManager.loadEriFile();
        this.mCdnr.updateEfForEri(getOperatorNameFromEri());
        updateLteEarfcnLists(config);
        updateReportingCriteria(config);
        updateOperatorNamePattern(config);
        this.mCdnr.updateEfFromCarrierConfig(config);
        pollState();
    }

    /* access modifiers changed from: protected */
    public void updateLteEarfcnLists(PersistableBundle config) {
        synchronized (this.mLteRsrpBoostLock) {
            this.mLteRsrpBoost = config.getInt("lte_earfcns_rsrp_boost_int", 0);
            this.mEarfcnPairListForRsrpBoost = convertEarfcnStringArrayToPairList(config.getStringArray("boosted_lte_earfcns_string_array"));
        }
    }

    /* access modifiers changed from: protected */
    public void updateReportingCriteria(PersistableBundle config) {
        this.mPhone.setSignalStrengthReportingCriteria(config.getIntArray("lte_rsrp_thresholds_int_array"), 3);
        this.mPhone.setSignalStrengthReportingCriteria(config.getIntArray("wcdma_rscp_thresholds_int_array"), 2);
    }

    /* access modifiers changed from: protected */
    public void updateServiceStateLteEarfcnBoost(ServiceState serviceState, int lteEarfcn) {
        synchronized (this.mLteRsrpBoostLock) {
            if (lteEarfcn != -1) {
                if (containsEarfcnInEarfcnRange(this.mEarfcnPairListForRsrpBoost, lteEarfcn)) {
                    serviceState.setLteEarfcnRsrpBoost(this.mLteRsrpBoost);
                }
            }
            serviceState.setLteEarfcnRsrpBoost(0);
        }
    }

    /* access modifiers changed from: protected */
    public boolean onSignalStrengthResult(AsyncResult ar) {
        if (ar.exception != null || ar.result == null) {
            log("onSignalStrengthResult() Exception from RIL : " + ar.exception);
            this.mSignalStrength = new SignalStrength();
        } else {
            this.mSignalStrength = (SignalStrength) ar.result;
            this.mSignalStrength.updateLevel(getCarrierConfig(), this.mSS);
        }
        return notifySignalStrength();
    }

    /* access modifiers changed from: protected */
    public void hangupAndPowerOff() {
        if (!this.mPhone.isPhoneTypeGsm() || this.mPhone.isInCall()) {
            this.mPhone.mCT.mRingingCall.hangupIfAlive();
            this.mPhone.mCT.mBackgroundCall.hangupIfAlive();
            this.mPhone.mCT.mForegroundCall.hangupIfAlive();
        }
        ImsPhone imsPhone = (ImsPhone) this.mPhone.getImsPhone();
        if (imsPhone != null && imsPhone.isInCall()) {
            log("exist volte call, hangup!!!");
            imsPhone.getForegroundCall().hangupIfAlive();
            imsPhone.getBackgroundCall().hangupIfAlive();
            imsPhone.getRingingCall().hangupIfAlive();
        }
        this.mCi.setRadioPower(false, obtainMessage(54));
    }

    /* access modifiers changed from: protected */
    public void cancelPollState() {
        this.mPollingContext = new int[1];
    }

    /* access modifiers changed from: protected */
    public boolean networkCountryIsoChanged(String newCountryIsoCode, String prevCountryIsoCode) {
        if (TextUtils.isEmpty(newCountryIsoCode)) {
            log("countryIsoChanged: no new country ISO code");
            return false;
        } else if (!TextUtils.isEmpty(prevCountryIsoCode)) {
            return !newCountryIsoCode.equals(prevCountryIsoCode);
        } else {
            log("countryIsoChanged: no previous country ISO code");
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public boolean iccCardExists() {
        UiccCardApplication uiccCardApplication = this.mUiccApplcation;
        if (uiccCardApplication == null) {
            return false;
        }
        return uiccCardApplication.getState() != IccCardApplicationStatus.AppState.APPSTATE_UNKNOWN;
    }

    @UnsupportedAppUsage
    public String getSystemProperty(String property, String defValue) {
        return TelephonyManager.getTelephonyProperty(this.mPhone.getPhoneId(), property, defValue);
    }

    public List<CellInfo> getAllCellInfo() {
        return this.mLastCellInfoList;
    }

    public void setCellInfoMinInterval(int interval) {
        this.mCellInfoMinIntervalMs = interval;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x004f, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0070, code lost:
        return;
     */
    public void requestAllCellInfo(WorkSource workSource, Message rspMsg) {
        if (this.mCi.getRilVersion() < 8) {
            AsyncResult.forMessage(rspMsg);
            rspMsg.sendToTarget();
            log("SST.requestAllCellInfo(): not implemented");
        } else if (super.checkDeepSleepStatus(this.mPhone.getContext(), this.mLastCellInfoList, rspMsg)) {
            log("SST.requestAllCellInfo(): return when in deep sleep state");
        } else {
            synchronized (this.mPendingCellInfoRequests) {
                if (this.mPendingCellInfoRequests.size() >= 6) {
                    log("SST.requestAllCellInfo(): exceed allowance of request, return");
                    AsyncResult.forMessage(rspMsg);
                    rspMsg.sendToTarget();
                } else if (!this.mIsPendingCellInfoRequest) {
                    long curTime = SystemClock.elapsedRealtime();
                    if (curTime - this.mLastCellInfoReqTime >= ((long) this.mCellInfoMinIntervalMs)) {
                        if (rspMsg != null) {
                            this.mPendingCellInfoRequests.add(rspMsg);
                        }
                        this.mLastCellInfoReqTime = curTime;
                        this.mIsPendingCellInfoRequest = true;
                        this.mCi.getCellInfoList(obtainMessage(43), workSource);
                        sendMessageDelayed(obtainMessage(43), CELL_INFO_LIST_QUERY_TIMEOUT);
                    } else if (rspMsg != null) {
                        log("SST.requestAllCellInfo(): return last, back to back calls");
                        AsyncResult.forMessage(rspMsg, this.mLastCellInfoList, (Throwable) null);
                        rspMsg.sendToTarget();
                    }
                } else if (rspMsg != null) {
                    this.mPendingCellInfoRequests.add(rspMsg);
                }
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
        Settings.Global.putInt(this.mPhone.getContext().getContentResolver(), "subscription_mode", source);
        log("Read from settings: " + Settings.Global.getInt(this.mPhone.getContext().getContentResolver(), "subscription_mode", -1));
    }

    /* access modifiers changed from: protected */
    public void getSubscriptionInfoAndStartPollingThreads() {
        this.mCi.getCDMASubscription(obtainMessage(34));
        pollState();
    }

    /* access modifiers changed from: protected */
    public void handleCdmaSubscriptionSource(int newSubscriptionSource) {
        log("Subscription Source : " + newSubscriptionSource);
        this.mIsSubscriptionFromRuim = newSubscriptionSource == 0;
        log("isFromRuim: " + this.mIsSubscriptionFromRuim);
        saveCdmaSubscriptionSource(newSubscriptionSource);
        if (!this.mIsSubscriptionFromRuim) {
            sendMessage(obtainMessage(35));
        }
    }

    private void dumpEarfcnPairList(PrintWriter pw) {
        pw.print(" mEarfcnPairListForRsrpBoost={");
        ArrayList<Pair<Integer, Integer>> arrayList = this.mEarfcnPairListForRsrpBoost;
        if (arrayList != null) {
            int i = arrayList.size();
            Iterator<Pair<Integer, Integer>> it = this.mEarfcnPairListForRsrpBoost.iterator();
            while (it.hasNext()) {
                Pair<Integer, Integer> earfcnPair = it.next();
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
        List<CellInfo> list = this.mLastCellInfoList;
        if (list != null) {
            boolean first = true;
            for (CellInfo info : list) {
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
        StringBuilder sb = new StringBuilder();
        sb.append(" mPollingContext=");
        sb.append(this.mPollingContext);
        sb.append(" - ");
        int[] iArr = this.mPollingContext;
        sb.append(iArr != null ? Integer.valueOf(iArr[0]) : PhoneConfigurationManager.SSSS);
        pw.println(sb.toString());
        pw.println(" mDesiredPowerState=" + this.mDesiredPowerState);
        pw.println(" mDontPollSignalStrength=" + this.mDontPollSignalStrength);
        pw.println(" mSignalStrength=" + this.mSignalStrength);
        pw.println(" mLastSignalStrength=" + this.mLastSignalStrength);
        pw.println(" mRestrictedState=" + this.mRestrictedState);
        pw.println(" mPendingRadioPowerOffAfterDataOff=" + this.mPendingRadioPowerOffAfterDataOff);
        pw.println(" mPendingRadioPowerOffAfterDataOffTag=" + this.mPendingRadioPowerOffAfterDataOffTag);
        pw.println(" mCellIdentity=" + Rlog.pii(false, this.mCellIdentity));
        pw.println(" mNewCellIdentity=" + Rlog.pii(false, this.mNewCellIdentity));
        pw.println(" mLastCellInfoReqTime=" + this.mLastCellInfoReqTime);
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
        pw.flush();
        this.mNitzState.dumpState(pw);
        pw.flush();
        pw.println(" mStartedGprsRegCheck=" + this.mStartedGprsRegCheck);
        pw.println(" mReportedGprsNoReg=" + this.mReportedGprsNoReg);
        pw.println(" mNotification=" + this.mNotification);
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
        pw.println(" mCellInfoMinIntervalMs=" + this.mCellInfoMinIntervalMs);
        pw.println(" mEriManager=" + this.mEriManager);
        dumpEarfcnPairList(pw);
        this.mLocaleTracker.dump(fd, pw, args);
        IndentingPrintWriter ipw = new IndentingPrintWriter(pw, "    ");
        this.mCdnr.dump(ipw);
        ipw.println(" Carrier Display Name update records:");
        ipw.increaseIndent();
        this.mCdnrLogs.dump(fd, ipw, args);
        ipw.decreaseIndent();
        ipw.println(" Roaming Log:");
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
        ipw.decreaseIndent();
        this.mNitzState.dumpLogs(fd, ipw, args);
        ipw.flush();
    }

    @UnsupportedAppUsage
    public boolean isImsRegistered() {
        return this.mImsRegistered;
    }

    /* access modifiers changed from: protected */
    public void checkCorrectThread() {
        if (Thread.currentThread() != getLooper().getThread()) {
            throw new RuntimeException("ServiceStateTracker must be used from within one thread");
        }
    }

    /* access modifiers changed from: protected */
    public boolean isCallerOnDifferentThread() {
        return Thread.currentThread() != getLooper().getThread();
    }

    /* access modifiers changed from: protected */
    public boolean inSameCountry(String operatorNumeric) {
        try {
            if (!TextUtils.isEmpty(operatorNumeric)) {
                if (operatorNumeric.length() >= 5) {
                    String homeNumeric = getHomeOperatorNumeric();
                    if (!TextUtils.isEmpty(homeNumeric)) {
                        if (homeNumeric.length() >= 5) {
                            String networkMCC = operatorNumeric.substring(0, 3);
                            String homeMCC = homeNumeric.substring(0, 3);
                            String networkCountry = MccTable.countryCodeForMcc(networkMCC);
                            String homeCountry = MccTable.countryCodeForMcc(homeMCC);
                            if (!networkCountry.isEmpty()) {
                                if (!homeCountry.isEmpty()) {
                                    boolean inSameCountry = homeCountry.equals(networkCountry);
                                    if (inSameCountry) {
                                        return inSameCountry;
                                    }
                                    if ("us".equals(homeCountry) && "vi".equals(networkCountry)) {
                                        return true;
                                    }
                                    if (!"vi".equals(homeCountry) || !"us".equals(networkCountry)) {
                                        return inSameCountry;
                                    }
                                    return true;
                                }
                            }
                            return false;
                        }
                    }
                    return false;
                }
            }
            return false;
        } catch (NumberFormatException e) {
            Rlog.d(LOG_TAG, e.toString());
            return false;
        } catch (Exception e2) {
            Rlog.d(LOG_TAG, e2.toString());
            return false;
        }
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void setRoamingType(ServiceState currentServiceState) {
        boolean isVoiceInService = currentServiceState.getVoiceRegState() == 0;
        if (isVoiceInService) {
            if (!currentServiceState.getVoiceRoaming()) {
                currentServiceState.setVoiceRoamingType(0);
            } else if (!this.mPhone.isPhoneTypeGsm()) {
                int[] intRoamingIndicators = this.mPhone.getContext().getResources().getIntArray(17236002);
                if (intRoamingIndicators != null && intRoamingIndicators.length > 0) {
                    currentServiceState.setVoiceRoamingType(2);
                    int curRoamingIndicator = currentServiceState.getCdmaRoamingIndicator();
                    int i = 0;
                    while (true) {
                        if (i >= intRoamingIndicators.length) {
                            break;
                        } else if (curRoamingIndicator == intRoamingIndicators[i]) {
                            currentServiceState.setVoiceRoamingType(3);
                            break;
                        } else {
                            i++;
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

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public void setSignalStrengthDefaultValues() {
        this.mSignalStrength = new SignalStrength();
    }

    /* access modifiers changed from: protected */
    public String getHomeOperatorNumeric() {
        String numeric = ((TelephonyManager) this.mPhone.getContext().getSystemService("phone")).getSimOperatorNumericForPhone(this.mPhone.getPhoneId());
        if (this.mPhone.isPhoneTypeGsm() || !TextUtils.isEmpty(numeric)) {
            return numeric;
        }
        return SystemProperties.get(GsmCdmaPhone.PROPERTY_CDMA_HOME_OPERATOR_NUMERIC, PhoneConfigurationManager.SSSS);
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public int getPhoneId() {
        return this.mPhone.getPhoneId();
    }

    /* access modifiers changed from: protected */
    public void processIwlanRegistrationInfo() {
        NetworkRegistrationInfo wwanNri;
        if (this.mCi.getRadioState() == 0) {
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
                this.mNewSS.setDataRegState(0);
                this.mNewSS.addNetworkRegistrationInfo(new NetworkRegistrationInfo.Builder().setTransportType(2).setDomain(2).setAccessNetworkTechnology(18).setRegistrationState(1).build());
                if (this.mTransportManager.isInLegacyMode()) {
                    this.mNewSS.addNetworkRegistrationInfo(new NetworkRegistrationInfo.Builder().setTransportType(1).setDomain(2).setAccessNetworkTechnology(18).setRegistrationState(1).build());
                }
                this.mNewSS.setOperatorAlphaLong(operator);
                log("pollStateDone: mNewSS = " + this.mNewSS);
            }
        } else if (this.mTransportManager.isInLegacyMode() && (wwanNri = this.mNewSS.getNetworkRegistrationInfo(2, 1)) != null && wwanNri.getAccessNetworkTechnology() == 18) {
            this.mNewSS.addNetworkRegistrationInfo(new NetworkRegistrationInfo.Builder().setTransportType(2).setDomain(2).setRegistrationState(wwanNri.getRegistrationState()).setAccessNetworkTechnology(18).setRejectCause(wwanNri.getRejectCause()).setEmergencyOnly(wwanNri.isEmergencyEnabled()).setAvailableServices(wwanNri.getAvailableServices()).build());
        }
    }

    /* access modifiers changed from: protected */
    public final boolean alwaysOnHomeNetwork(BaseBundle b) {
        return b.getBoolean("force_home_network_bool");
    }

    private boolean isInNetwork(BaseBundle b, String network, String key) {
        String[] networks = b.getStringArray(key);
        if (networks == null || !Arrays.asList(networks).contains(network)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public final boolean isRoamingInGsmNetwork(BaseBundle b, String network) {
        return isInNetwork(b, network, "gsm_roaming_networks_string_array");
    }

    /* access modifiers changed from: protected */
    public final boolean isNonRoamingInGsmNetwork(BaseBundle b, String network) {
        return isInNetwork(b, network, "gsm_nonroaming_networks_string_array");
    }

    /* access modifiers changed from: protected */
    public final boolean isRoamingInCdmaNetwork(BaseBundle b, String network) {
        return isInNetwork(b, network, "cdma_roaming_networks_string_array");
    }

    /* access modifiers changed from: protected */
    public final boolean isNonRoamingInCdmaNetwork(BaseBundle b, String network) {
        return isInNetwork(b, network, "cdma_nonroaming_networks_string_array");
    }

    public boolean isDeviceShuttingDown() {
        return this.mDeviceShuttingDown;
    }

    /* access modifiers changed from: protected */
    public int getCombinedRegState(ServiceState ss) {
        int regState = ss.getVoiceRegState();
        int dataRegState = ss.getDataRegState();
        if ((regState != 1 && regState != 3) || dataRegState != 0) {
            return regState;
        }
        log("getCombinedRegState: return STATE_IN_SERVICE as Data is in service");
        return dataRegState;
    }

    /* access modifiers changed from: protected */
    public PersistableBundle getCarrierConfig() {
        PersistableBundle config;
        CarrierConfigManager configManager = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        if (configManager == null || (config = configManager.getConfigForSubId(this.mPhone.getSubId())) == null) {
            return CarrierConfigManager.getDefaultConfig();
        }
        return config;
    }

    public LocaleTracker getLocaleTracker() {
        return this.mLocaleTracker;
    }

    /* access modifiers changed from: package-private */
    public String getCdmaEriText(int roamInd, int defRoamInd) {
        return this.mEriManager.getCdmaEriText(roamInd, defRoamInd);
    }

    /* access modifiers changed from: protected */
    public void updateOperatorNamePattern(PersistableBundle config) {
        String operatorNamePattern = config.getString("operator_name_filter_pattern_string");
        if (!TextUtils.isEmpty(operatorNamePattern)) {
            this.mOperatorNameStringPattern = Pattern.compile(operatorNamePattern);
            log("mOperatorNameStringPattern: " + this.mOperatorNameStringPattern.toString());
        }
    }

    /* access modifiers changed from: protected */
    public void updateOperatorNameForServiceState(ServiceState servicestate) {
        if (servicestate != null) {
            servicestate.setOperatorName(filterOperatorNameByPattern(servicestate.getOperatorAlphaLong()), filterOperatorNameByPattern(servicestate.getOperatorAlphaShort()), servicestate.getOperatorNumeric());
            List<NetworkRegistrationInfo> networkRegistrationInfos = servicestate.getNetworkRegistrationInfoList();
            for (int i = 0; i < networkRegistrationInfos.size(); i++) {
                if (networkRegistrationInfos.get(i) != null) {
                    updateOperatorNameForCellIdentity(networkRegistrationInfos.get(i).getCellIdentity());
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updateOperatorNameForCellIdentity(CellIdentity cellIdentity) {
        if (cellIdentity != null) {
            cellIdentity.setOperatorAlphaLong(filterOperatorNameByPattern((String) cellIdentity.getOperatorAlphaLong()));
            cellIdentity.setOperatorAlphaShort(filterOperatorNameByPattern((String) cellIdentity.getOperatorAlphaShort()));
        }
    }

    public void updateOperatorNameForCellInfo(List<CellInfo> cellInfos) {
        if (cellInfos != null && !cellInfos.isEmpty()) {
            for (CellInfo cellInfo : cellInfos) {
                if (cellInfo.isRegistered()) {
                    updateOperatorNameForCellIdentity(cellInfo.getCellIdentity());
                }
            }
        }
    }

    public String filterOperatorNameByPattern(String operatorName) {
        if (this.mOperatorNameStringPattern == null || TextUtils.isEmpty(operatorName)) {
            return operatorName;
        }
        Matcher matcher = this.mOperatorNameStringPattern.matcher(operatorName);
        if (!matcher.find()) {
            return operatorName;
        }
        if (matcher.groupCount() > 0) {
            return matcher.group(1);
        }
        log("filterOperatorNameByPattern: pattern no group");
        return operatorName;
    }

    /* access modifiers changed from: protected */
    public void mtkIvsrUpdateCsPlmn() {
    }

    /* access modifiers changed from: protected */
    public void mtkIvsrUpdatePsPlmn() {
    }

    /* access modifiers changed from: protected */
    public int mtkReplaceDdsIfUnset(int dds) {
        return dds;
    }

    /* access modifiers changed from: protected */
    public void mtkRegisterAllDataDisconnected() {
    }

    /* access modifiers changed from: protected */
    public int mtkReplaceDisconnectTimer() {
        return CarrierServicesSmsFilter.FILTER_COMPLETE_TIMEOUT_MS;
    }

    /* access modifiers changed from: protected */
    public void hangupAllImsCall() {
    }
}
