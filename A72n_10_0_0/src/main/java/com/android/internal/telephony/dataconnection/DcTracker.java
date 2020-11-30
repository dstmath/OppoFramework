package com.android.internal.telephony.dataconnection;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.NetworkCapabilities;
import android.net.NetworkConfig;
import android.net.NetworkRequest;
import android.net.ProxyInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.OppoManager;
import android.os.PersistableBundle;
import android.os.RegistrantList;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.CarrierConfigManager;
import android.telephony.CellLocation;
import android.telephony.DataFailCause;
import android.telephony.NetworkRegistrationInfo;
import android.telephony.PcoData;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.data.ApnSetting;
import android.telephony.data.DataProfile;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.LocalLog;
import android.util.Pair;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.DctConstants;
import com.android.internal.telephony.EventLogTags;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.HbpcdLookup;
import com.android.internal.telephony.IOppoDataManager;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.InboundSmsTracker;
import com.android.internal.telephony.OppoModemLogManager;
import com.android.internal.telephony.OppoTelephonyFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConfigurationManager;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.PhoneSwitcher;
import com.android.internal.telephony.SettingsObserver;
import com.android.internal.telephony.dataconnection.DataConnectionReasons;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.nano.TelephonyProto;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.util.OemTelephonyUtils;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.AsyncChannel;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class DcTracker extends AbstractDcTracker {
    static final String APN_ID = "apn_id";
    static final String DATA_COMPLETE_MSG_EXTRA_HANDOVER_FAILURE_FALLBACK = "extra_handover_failure_fallback";
    public static final String DATA_COMPLETE_MSG_EXTRA_NETWORK_REQUEST = "extra_network_request";
    static final String DATA_COMPLETE_MSG_EXTRA_REQUEST_TYPE = "extra_request_type";
    static final String DATA_COMPLETE_MSG_EXTRA_SUCCESS = "extra_success";
    static final String DATA_COMPLETE_MSG_EXTRA_TRANSPORT_TYPE = "extra_transport_type";
    private static final DctConstants.State[] DATA_CONNECTION_STATE_PRIORITIES = {DctConstants.State.IDLE, DctConstants.State.DISCONNECTING, DctConstants.State.CONNECTING, DctConstants.State.CONNECTED};
    private static final String DATA_EVENT_ID = "050401";
    private static final int DATA_STALL_ALARM_AGGRESSIVE_DELAY_IN_MS_DEFAULT = 60000;
    private static final int DATA_STALL_ALARM_NON_AGGRESSIVE_DELAY_IN_MS_DEFAULT = 360000;
    protected static final boolean DATA_STALL_NOT_SUSPECTED = false;
    private static final boolean DATA_STALL_SUSPECTED = true;
    private static final boolean DBG = true;
    private static final String DEBUG_PROV_APN_ALARM = "persist.debug.prov_apn_alarm";
    private static final String INTENT_DATA_STALL_ALARM = "com.android.internal.telephony.data-stall";
    private static final String INTENT_DATA_STALL_ALARM_EXTRA_TAG = "data_stall_alarm_extra_tag";
    private static final String INTENT_DATA_STALL_ALARM_EXTRA_TRANSPORT_TYPE = "data_stall_alarm_extra_transport_type";
    private static final String INTENT_PROVISIONING_APN_ALARM = "com.android.internal.telephony.provisioning_apn_alarm";
    private static final String INTENT_RECONNECT_ALARM = "com.android.internal.telephony.data-reconnect";
    private static final String INTENT_RECONNECT_ALARM_EXTRA_REASON = "reconnect_alarm_extra_reason";
    private static final String INTENT_RECONNECT_ALARM_EXTRA_TRANSPORT_TYPE = "reconnect_alarm_extra_transport_type";
    private static final String INTENT_RECONNECT_ALARM_EXTRA_TYPE = "reconnect_alarm_extra_type";
    private static final String LOG_TAG = "DCT";
    private static final int NETWORK_TYPE_CBS = 12;
    private static final int NETWORK_TYPE_DEFAULT = 0;
    private static final int NETWORK_TYPE_DUN = 4;
    private static final int NETWORK_TYPE_EMERGENCY = 15;
    private static final int NETWORK_TYPE_FOTA = 10;
    private static final int NETWORK_TYPE_HIPRI = 5;
    private static final int NETWORK_TYPE_IA = 14;
    private static final int NETWORK_TYPE_IMS = 11;
    private static final int NETWORK_TYPE_MCX = 1001;
    private static final int NETWORK_TYPE_MMS = 2;
    private static final int NETWORK_TYPE_SUPL = 3;
    private static final int NUMBER_SENT_PACKETS_OF_HANG = 10;
    private static final int POLL_NETSTAT_MILLIS = 1000;
    private static final int POLL_NETSTAT_SCREEN_OFF_MILLIS = 600000;
    private static final int POLL_PDP_MILLIS = 5000;
    protected static final Uri PREFERAPN_NO_UPDATE_URI_USING_SUBID = Uri.parse("content://telephony/carriers/preferapn_no_update/subId/");
    private static final int PROVISIONING_APN_ALARM_DELAY_IN_MS_DEFAULT = 900000;
    private static final String PROVISIONING_APN_ALARM_TAG_EXTRA = "provisioning.apn.alarm.tag";
    private static final int PROVISIONING_SPINNER_TIMEOUT_MILLIS = 120000;
    private static final String PUPPET_MASTER_RADIO_STRESS_TEST = "gsm.defaultpdpcontext.active";
    private static final boolean RADIO_TESTS = false;
    private static final int RECOVERY_ACTION_CLEANUP = 1;
    private static final int RECOVERY_ACTION_GET_DATA_CALL_LIST = 0;
    private static final int RECOVERY_ACTION_RADIO_RESTART = 3;
    private static final int RECOVERY_ACTION_REREGISTER = 2;
    public static final int RELEASE_TYPE_DETACH = 2;
    public static final int RELEASE_TYPE_HANDOVER = 3;
    public static final int RELEASE_TYPE_NORMAL = 1;
    public static final int REQUEST_TYPE_HANDOVER = 2;
    public static final int REQUEST_TYPE_NORMAL = 1;
    protected static final int THRESHOLD_TO_RECORD = 10;
    private static final boolean VDBG = false;
    private static final boolean VDBG_STALL = false;
    public static final String[] isSpecialOperator = {"23430", "23433", "20416", "20420"};
    protected static boolean mOppoCtaSupport = false;
    private static int sEnableFailFastRefCounter = 0;
    private static Method sMethodUpdateTxRxSumEx;
    protected static final String[] sTelstraOperaters = {"50501", "50511", "50571", "50572"};
    public AtomicBoolean isCleanupRequired;
    private DctConstants.Activity mActivity;
    private final AlarmManager mAlarmManager;
    protected ArrayList<ApnSetting> mAllApnSettings;
    private RegistrantList mAllDataDisconnectedRegistrants;
    protected final ConcurrentHashMap<String, ApnContext> mApnContexts;
    protected final SparseArray<ApnContext> mApnContextsByType;
    private ApnChangeObserver mApnObserver;
    protected final LocalLog mApnSettingsInitializationLog;
    private HashMap<String, Integer> mApnToDataConnectionId;
    protected AtomicBoolean mAttached;
    protected AtomicBoolean mAutoAttachEnabled;
    protected boolean mAutoAttachOnCreationConfig;
    protected boolean mCanSetPreferApn;
    private final Handler mDataConnectionTracker;
    protected HashMap<Integer, DataConnection> mDataConnections;
    protected final DataEnabledSettings mDataEnabledSettings;
    private final LocalLog mDataRoamingLeakageLog;
    protected boolean mDataServiceBound;
    protected final DataServiceManager mDataServiceManager;
    private PendingIntent mDataStallAlarmIntent;
    private int mDataStallAlarmTag;
    private volatile boolean mDataStallNoRxEnabled;
    protected TxRxSum mDataStallTxRxSum;
    private int mDataType;
    protected DcTesterFailBringUpAll mDcTesterFailBringUpAll;
    protected DcController mDcc;
    protected int mDisconnectPendingCount;
    private DataStallRecoveryHandler mDsRecoveryHandler;
    protected ApnSetting mEmergencyApn;
    private volatile boolean mFailFast;
    protected boolean mFirstcommingModemKeyLog;
    protected boolean mHasDataConnConncted;
    protected boolean mHasInboundData;
    protected final AtomicReference<IccRecords> mIccRecords;
    protected boolean mInVoiceCall;
    private final BroadcastReceiver mIntentReceiver;
    private boolean mIsDisposed;
    private boolean mIsProvisioning;
    protected boolean mIsPsRestricted;
    private boolean mIsScreenOn;
    protected boolean mIsWifiConnected;
    protected ArrayList<DataProfile> mLastDataProfileList;
    protected final String mLogTag;
    protected boolean mModemKeyLogState;
    private boolean mNetStatPollEnabled;
    private int mNetStatPollPeriod;
    private int mNoRecvPollCount;
    private final DctOnSubscriptionsChangedListener mOnSubscriptionsChangedListener;
    protected final Phone mPhone;
    private final Runnable mPollNetStat;
    protected ApnSetting mPreferredApn;
    private final PriorityQueue<ApnContext> mPrioritySortedApnContexts;
    private final String mProvisionActionName;
    private BroadcastReceiver mProvisionBroadcastReceiver;
    private PendingIntent mProvisioningApnAlarmIntent;
    private int mProvisioningApnAlarmTag;
    private ProgressDialog mProvisioningSpinner;
    private String mProvisioningUrl;
    private PendingIntent mReconnectIntent;
    protected final Object mRefCountLock;
    private AsyncChannel mReplyAc;
    private final Map<Integer, List<Message>> mRequestNetworkCompletionMsgs;
    protected int mRequestedApnType;
    protected boolean mReregisterOnReconnectFailure;
    protected ContentResolver mResolver;
    private long mRxPkts;
    private long mSentSinceLastRecv;
    protected final SettingsObserver mSettingsObserver;
    protected int mSetupDataCallFailureCount;
    protected DctConstants.State mState;
    private SubscriptionManager mSubscriptionManager;
    private final TelephonyManager mTelephonyManager;
    protected final int mTransportType;
    private long mTxPkts;
    protected final UiccController mUiccController;
    protected AtomicInteger mUniqueIdGenerator;

    @Retention(RetentionPolicy.SOURCE)
    private @interface RecoveryAction {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ReleaseNetworkType {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface RequestNetworkType {
    }

    /* access modifiers changed from: protected */
    public enum RetryFailures {
        ALWAYS,
        ONLY_ON_CHANGE
    }

    static {
        Class<?> clz = null;
        try {
            clz = Class.forName("com.mediatek.internal.telephony.dataconnection.MtkDcTracker");
        } catch (Exception e) {
            Rlog.d(LOG_TAG, e.toString());
        }
        if (clz != null) {
            try {
                sMethodUpdateTxRxSumEx = clz.getDeclaredMethod("updateTxRxSumEx", new Class[0]);
                sMethodUpdateTxRxSumEx.setAccessible(true);
            } catch (Exception e2) {
                Rlog.d(LOG_TAG, e2.toString());
            }
        }
    }

    /* access modifiers changed from: private */
    public class DctOnSubscriptionsChangedListener extends SubscriptionManager.OnSubscriptionsChangedListener {
        public final AtomicInteger mPreviousSubId;

        private DctOnSubscriptionsChangedListener() {
            this.mPreviousSubId = new AtomicInteger(-1);
        }

        public void onSubscriptionsChanged() {
            DcTracker.this.log("SubscriptionListener.onSubscriptionInfoChanged");
            int subId = DcTracker.this.mPhone.getSubId();
            if (SubscriptionManager.isValidSubscriptionId(subId) && DcTracker.this.mtkIsNeedRegisterSettingsObserver(this.mPreviousSubId.get(), subId)) {
                DcTracker.this.registerSettingsObserver();
            }
            if (SubscriptionManager.isValidSubscriptionId(subId) && this.mPreviousSubId.getAndSet(subId) != subId) {
                DcTracker.this.onRecordsLoadedOrSubIdChanged();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void registerSettingsObserver() {
        this.mSettingsObserver.unobserve();
        String simSuffix = PhoneConfigurationManager.SSSS;
        if (TelephonyManager.getDefault().getSimCount() > 1) {
            simSuffix = Integer.toString(this.mPhone.getSubId());
        }
        SettingsObserver settingsObserver = this.mSettingsObserver;
        settingsObserver.observe(Settings.Global.getUriFor("data_roaming" + simSuffix), 270384);
        this.mSettingsObserver.observe(Settings.Global.getUriFor("device_provisioned"), 270386);
    }

    public static class TxRxSum {
        public long rxPkts;
        public long txPkts;

        public TxRxSum() {
            reset();
        }

        public TxRxSum(long txPkts2, long rxPkts2) {
            this.txPkts = txPkts2;
            this.rxPkts = rxPkts2;
        }

        public TxRxSum(TxRxSum sum) {
            this.txPkts = sum.txPkts;
            this.rxPkts = sum.rxPkts;
        }

        public void reset() {
            this.txPkts = -1;
            this.rxPkts = -1;
        }

        public String toString() {
            return "{txSum=" + this.txPkts + " rxSum=" + this.rxPkts + "}";
        }

        public void updateTcpTxRxSum() {
            this.txPkts = TrafficStats.getMobileTcpTxPackets();
            this.rxPkts = TrafficStats.getMobileTcpRxPackets();
        }

        public void updateTotalTxRxSum() {
            this.txPkts = TrafficStats.getMobileTxPackets();
            this.rxPkts = TrafficStats.getMobileRxPackets();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onActionIntentReconnectAlarm(Intent intent) {
        Message msg = obtainMessage(270383);
        msg.setData(intent.getExtras());
        sendMessage(msg);
    }

    private void onDataReconnect(Bundle bundle) {
        String reason = bundle.getString(INTENT_RECONNECT_ALARM_EXTRA_REASON);
        String apnType = bundle.getString(INTENT_RECONNECT_ALARM_EXTRA_TYPE);
        int phoneSubId = this.mPhone.getSubId();
        int currSubId = bundle.getInt("subscription", -1);
        if (SubscriptionManager.isValidSubscriptionId(currSubId) && currSubId == phoneSubId && bundle.getInt(INTENT_RECONNECT_ALARM_EXTRA_TRANSPORT_TYPE, 0) == this.mTransportType) {
            ApnContext apnContext = this.mApnContexts.get(apnType);
            log("onDataReconnect: mState=" + this.mState + " reason=" + reason + " apnType=" + apnType + " apnContext=" + apnContext);
            if (apnContext != null && apnContext.isEnabled()) {
                apnContext.setReason(reason);
                DctConstants.State apnContextState = apnContext.getState();
                log("onDataReconnect: apnContext state=" + apnContextState);
                if (apnContextState == DctConstants.State.FAILED || apnContextState == DctConstants.State.IDLE) {
                    log("onDataReconnect: state is FAILED|IDLE, disassociate");
                    apnContext.releaseDataConnection(PhoneConfigurationManager.SSSS);
                } else {
                    log("onDataReconnect: keep associated");
                }
                sendMessage(obtainMessage(270339, apnContext));
                apnContext.setReconnectIntent(null);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onActionIntentDataStallAlarm(Intent intent) {
        int subId = intent.getIntExtra("subscription", -1);
        if (SubscriptionManager.isValidSubscriptionId(subId) && subId == this.mPhone.getSubId() && intent.getIntExtra(INTENT_DATA_STALL_ALARM_EXTRA_TRANSPORT_TYPE, 0) == this.mTransportType) {
            Message msg = obtainMessage(270353, intent.getAction());
            msg.arg1 = intent.getIntExtra(INTENT_DATA_STALL_ALARM_EXTRA_TAG, 0);
            sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    public class ApnChangeObserver extends ContentObserver {
        public ApnChangeObserver() {
            super(DcTracker.this.mDataConnectionTracker);
        }

        public void onChange(boolean selfChange) {
            DcTracker dcTracker = DcTracker.this;
            dcTracker.sendMessage(dcTracker.obtainMessage(270355));
        }
    }

    public DcTracker(Phone phone, int transportType) {
        this.isCleanupRequired = new AtomicBoolean(false);
        this.mRequestedApnType = 17;
        this.mSetupDataCallFailureCount = 0;
        this.mHasDataConnConncted = true;
        this.mHasInboundData = true;
        this.mDataType = 0;
        this.mPrioritySortedApnContexts = new PriorityQueue<>(5, new Comparator<ApnContext>() {
            /* class com.android.internal.telephony.dataconnection.DcTracker.AnonymousClass1 */

            public int compare(ApnContext c1, ApnContext c2) {
                return c2.priority - c1.priority;
            }
        });
        this.mAllApnSettings = new ArrayList<>();
        this.mRefCountLock = new Object();
        this.mPreferredApn = null;
        this.mIsPsRestricted = false;
        this.mEmergencyApn = null;
        this.mIsDisposed = false;
        this.mIsProvisioning = false;
        this.mProvisioningUrl = null;
        this.mDataServiceBound = false;
        this.mProvisioningApnAlarmIntent = null;
        this.mProvisioningApnAlarmTag = (int) SystemClock.elapsedRealtime();
        this.mReplyAc = new AsyncChannel();
        this.mDataRoamingLeakageLog = new LocalLog(50);
        this.mApnSettingsInitializationLog = new LocalLog(50);
        this.mIntentReceiver = new BroadcastReceiver() {
            /* class com.android.internal.telephony.dataconnection.DcTracker.AnonymousClass2 */

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.startsWith(DcTracker.INTENT_RECONNECT_ALARM)) {
                    DcTracker.this.onActionIntentReconnectAlarm(intent);
                } else if (action.equals(DcTracker.INTENT_DATA_STALL_ALARM)) {
                    DcTracker.this.onActionIntentDataStallAlarm(intent);
                } else if (action.equals(DcTracker.INTENT_PROVISIONING_APN_ALARM)) {
                    DcTracker.this.log("Provisioning apn alarm");
                    DcTracker.this.onActionIntentProvisioningApnAlarm(intent);
                } else if (action.equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                    DcTracker.this.log("received carrier config change");
                    if (DcTracker.this.mIccRecords.get() != null && DcTracker.this.mIccRecords.get().getRecordsLoaded()) {
                        DcTracker.this.setDefaultDataRoamingEnabled();
                    }
                } else {
                    DcTracker dcTracker = DcTracker.this;
                    dcTracker.log("onReceive: Unknown action=" + action);
                }
            }
        };
        this.mPollNetStat = new Runnable() {
            /* class com.android.internal.telephony.dataconnection.DcTracker.AnonymousClass3 */

            public void run() {
                DcTracker.this.updateDataActivity();
                if (DcTracker.this.mIsScreenOn) {
                    DcTracker dcTracker = DcTracker.this;
                    dcTracker.mNetStatPollPeriod = Settings.Global.getInt(dcTracker.mResolver, "pdp_watchdog_poll_interval_ms", 1000);
                } else {
                    DcTracker dcTracker2 = DcTracker.this;
                    dcTracker2.mNetStatPollPeriod = Settings.Global.getInt(dcTracker2.mResolver, "pdp_watchdog_long_poll_interval_ms", DcTracker.POLL_NETSTAT_SCREEN_OFF_MILLIS);
                }
                if (DcTracker.this.mNetStatPollEnabled) {
                    DcTracker.this.mDataConnectionTracker.postDelayed(this, (long) DcTracker.this.mNetStatPollPeriod);
                }
            }
        };
        this.mOnSubscriptionsChangedListener = new DctOnSubscriptionsChangedListener();
        this.mAllDataDisconnectedRegistrants = new RegistrantList();
        this.mIccRecords = new AtomicReference<>();
        this.mActivity = DctConstants.Activity.NONE;
        this.mState = DctConstants.State.IDLE;
        this.mNetStatPollEnabled = false;
        this.mDataStallTxRxSum = new TxRxSum(0, 0);
        this.mDataStallAlarmTag = (int) SystemClock.elapsedRealtime();
        this.mDataStallAlarmIntent = null;
        this.mNoRecvPollCount = 0;
        this.mDataStallNoRxEnabled = true;
        this.mFailFast = false;
        this.mInVoiceCall = false;
        this.mIsWifiConnected = false;
        this.mReconnectIntent = null;
        this.mAutoAttachOnCreationConfig = false;
        this.mAutoAttachEnabled = new AtomicBoolean(false);
        this.mIsScreenOn = true;
        this.mUniqueIdGenerator = new AtomicInteger(0);
        this.mDataConnections = new HashMap<>();
        this.mApnToDataConnectionId = new HashMap<>();
        this.mApnContexts = new ConcurrentHashMap<>();
        this.mApnContextsByType = new SparseArray<>();
        this.mDisconnectPendingCount = 0;
        this.mLastDataProfileList = new ArrayList<>();
        this.mReregisterOnReconnectFailure = false;
        this.mCanSetPreferApn = false;
        this.mAttached = new AtomicBoolean(false);
        this.mModemKeyLogState = false;
        this.mFirstcommingModemKeyLog = true;
        this.mRequestNetworkCompletionMsgs = new HashMap();
        this.mPhone = phone;
        log("DCT.constructor");
        this.mTelephonyManager = TelephonyManager.from(phone.getContext()).createForSubscriptionId(phone.getSubId());
        StringBuilder sb = new StringBuilder();
        sb.append("-");
        sb.append(transportType == 1 ? "C" : "I");
        String tagSuffix = sb.toString();
        if (this.mTelephonyManager.getPhoneCount() > 1) {
            tagSuffix = tagSuffix + "-" + this.mPhone.getPhoneId();
        }
        this.mLogTag = LOG_TAG + tagSuffix;
        this.mTransportType = transportType;
        this.mDataServiceManager = new DataServiceManager(phone, transportType, tagSuffix);
        this.mResolver = this.mPhone.getContext().getContentResolver();
        this.mUiccController = UiccController.getInstance();
        this.mUiccController.registerForIccChanged(this, 270369, null);
        this.mAlarmManager = (AlarmManager) this.mPhone.getContext().getSystemService("alarm");
        this.mDsRecoveryHandler = new DataStallRecoveryHandler();
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_DATA_STALL_ALARM);
        filter.addAction(INTENT_PROVISIONING_APN_ALARM);
        filter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        this.mDataEnabledSettings = this.mPhone.getDataEnabledSettings();
        this.mDataEnabledSettings.registerForDataEnabledChanged(this, 270382, null);
        this.mDataEnabledSettings.registerForDataEnabledOverrideChanged(this, 270387);
        this.mPhone.getContext().registerReceiver(this.mIntentReceiver, filter, null, this.mPhone);
        this.mAutoAttachEnabled.set(PreferenceManager.getDefaultSharedPreferences(this.mPhone.getContext()).getBoolean(Phone.DATA_DISABLED_ON_BOOT_KEY, false));
        this.mSubscriptionManager = SubscriptionManager.from(this.mPhone.getContext());
        this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        HandlerThread dcHandlerThread = new HandlerThread("DcHandlerThread");
        dcHandlerThread.start();
        Handler dcHandler = new Handler(dcHandlerThread.getLooper());
        this.mDcc = DcController.makeDcc(this.mPhone, this, this.mDataServiceManager, dcHandler, tagSuffix);
        this.mDcTesterFailBringUpAll = new DcTesterFailBringUpAll(this.mPhone, dcHandler);
        mtkCopyHandlerThread(dcHandlerThread);
        this.mDataConnectionTracker = this;
        registerForAllEvents();
        update();
        this.mApnObserver = new ApnChangeObserver();
        phone.getContext().getContentResolver().registerContentObserver(Telephony.Carriers.CONTENT_URI, true, this.mApnObserver);
        initApnContexts();
        Iterator<ApnContext> it = this.mApnContexts.values().iterator();
        while (it.hasNext()) {
            IntentFilter filter2 = new IntentFilter();
            filter2.addAction("com.android.internal.telephony.data-reconnect." + it.next().getApnType());
            this.mPhone.getContext().registerReceiver(this.mIntentReceiver, filter2, null, this.mPhone);
        }
        initEmergencyApnSetting();
        addEmergencyApnSetting();
        this.mProvisionActionName = "com.android.internal.telephony.PROVISION" + phone.getPhoneId();
        this.mSettingsObserver = new SettingsObserver(this.mPhone.getContext(), this);
        registerSettingsObserver();
        this.mDataStallNoRxEnabled = OemTelephonyUtils.isNwLabTest() ^ true;
        Phone phone2 = this.mPhone;
        if (!(phone2 == null || phone2.getContext() == null || this.mPhone.getContext().getPackageManager() == null)) {
            mOppoCtaSupport = this.mPhone.getContext().getPackageManager().hasSystemFeature("oppo.cta.support");
        }
        this.mReference = (IOppoDcTracker) OppoTelephonyFactory.getInstance().getFeature(IOppoDcTracker.DEFAULT, this, this.mPhone);
    }

    @VisibleForTesting
    public DcTracker() {
        this.isCleanupRequired = new AtomicBoolean(false);
        this.mRequestedApnType = 17;
        this.mSetupDataCallFailureCount = 0;
        this.mHasDataConnConncted = true;
        this.mHasInboundData = true;
        this.mDataType = 0;
        this.mPrioritySortedApnContexts = new PriorityQueue<>(5, new Comparator<ApnContext>() {
            /* class com.android.internal.telephony.dataconnection.DcTracker.AnonymousClass1 */

            public int compare(ApnContext c1, ApnContext c2) {
                return c2.priority - c1.priority;
            }
        });
        this.mAllApnSettings = new ArrayList<>();
        this.mRefCountLock = new Object();
        this.mPreferredApn = null;
        this.mIsPsRestricted = false;
        this.mEmergencyApn = null;
        this.mIsDisposed = false;
        this.mIsProvisioning = false;
        this.mProvisioningUrl = null;
        this.mDataServiceBound = false;
        this.mProvisioningApnAlarmIntent = null;
        this.mProvisioningApnAlarmTag = (int) SystemClock.elapsedRealtime();
        this.mReplyAc = new AsyncChannel();
        this.mDataRoamingLeakageLog = new LocalLog(50);
        this.mApnSettingsInitializationLog = new LocalLog(50);
        this.mIntentReceiver = new BroadcastReceiver() {
            /* class com.android.internal.telephony.dataconnection.DcTracker.AnonymousClass2 */

            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.startsWith(DcTracker.INTENT_RECONNECT_ALARM)) {
                    DcTracker.this.onActionIntentReconnectAlarm(intent);
                } else if (action.equals(DcTracker.INTENT_DATA_STALL_ALARM)) {
                    DcTracker.this.onActionIntentDataStallAlarm(intent);
                } else if (action.equals(DcTracker.INTENT_PROVISIONING_APN_ALARM)) {
                    DcTracker.this.log("Provisioning apn alarm");
                    DcTracker.this.onActionIntentProvisioningApnAlarm(intent);
                } else if (action.equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                    DcTracker.this.log("received carrier config change");
                    if (DcTracker.this.mIccRecords.get() != null && DcTracker.this.mIccRecords.get().getRecordsLoaded()) {
                        DcTracker.this.setDefaultDataRoamingEnabled();
                    }
                } else {
                    DcTracker dcTracker = DcTracker.this;
                    dcTracker.log("onReceive: Unknown action=" + action);
                }
            }
        };
        this.mPollNetStat = new Runnable() {
            /* class com.android.internal.telephony.dataconnection.DcTracker.AnonymousClass3 */

            public void run() {
                DcTracker.this.updateDataActivity();
                if (DcTracker.this.mIsScreenOn) {
                    DcTracker dcTracker = DcTracker.this;
                    dcTracker.mNetStatPollPeriod = Settings.Global.getInt(dcTracker.mResolver, "pdp_watchdog_poll_interval_ms", 1000);
                } else {
                    DcTracker dcTracker2 = DcTracker.this;
                    dcTracker2.mNetStatPollPeriod = Settings.Global.getInt(dcTracker2.mResolver, "pdp_watchdog_long_poll_interval_ms", DcTracker.POLL_NETSTAT_SCREEN_OFF_MILLIS);
                }
                if (DcTracker.this.mNetStatPollEnabled) {
                    DcTracker.this.mDataConnectionTracker.postDelayed(this, (long) DcTracker.this.mNetStatPollPeriod);
                }
            }
        };
        this.mOnSubscriptionsChangedListener = new DctOnSubscriptionsChangedListener();
        this.mAllDataDisconnectedRegistrants = new RegistrantList();
        this.mIccRecords = new AtomicReference<>();
        this.mActivity = DctConstants.Activity.NONE;
        this.mState = DctConstants.State.IDLE;
        this.mNetStatPollEnabled = false;
        this.mDataStallTxRxSum = new TxRxSum(0, 0);
        this.mDataStallAlarmTag = (int) SystemClock.elapsedRealtime();
        this.mDataStallAlarmIntent = null;
        this.mNoRecvPollCount = 0;
        this.mDataStallNoRxEnabled = true;
        this.mFailFast = false;
        this.mInVoiceCall = false;
        this.mIsWifiConnected = false;
        this.mReconnectIntent = null;
        this.mAutoAttachOnCreationConfig = false;
        this.mAutoAttachEnabled = new AtomicBoolean(false);
        this.mIsScreenOn = true;
        this.mUniqueIdGenerator = new AtomicInteger(0);
        this.mDataConnections = new HashMap<>();
        this.mApnToDataConnectionId = new HashMap<>();
        this.mApnContexts = new ConcurrentHashMap<>();
        this.mApnContextsByType = new SparseArray<>();
        this.mDisconnectPendingCount = 0;
        this.mLastDataProfileList = new ArrayList<>();
        this.mReregisterOnReconnectFailure = false;
        this.mCanSetPreferApn = false;
        this.mAttached = new AtomicBoolean(false);
        this.mModemKeyLogState = false;
        this.mFirstcommingModemKeyLog = true;
        this.mRequestNetworkCompletionMsgs = new HashMap();
        this.mLogTag = LOG_TAG;
        this.mTelephonyManager = null;
        this.mAlarmManager = null;
        this.mPhone = null;
        this.mUiccController = null;
        this.mDataConnectionTracker = null;
        this.mProvisionActionName = null;
        this.mSettingsObserver = new SettingsObserver(null, this);
        this.mDataEnabledSettings = null;
        this.mTransportType = 0;
        this.mDataServiceManager = null;
    }

    public void registerServiceStateTrackerEvents() {
        this.mPhone.getServiceStateTracker().registerForDataConnectionAttached(this.mTransportType, this, 270352, null);
        this.mPhone.getServiceStateTracker().registerForDataConnectionDetached(this.mTransportType, this, 270345, null);
        this.mPhone.getServiceStateTracker().registerForDataRoamingOn(this, 270347, null);
        this.mPhone.getServiceStateTracker().registerForDataRoamingOff(this, 270348, null, true);
        this.mPhone.getServiceStateTracker().registerForPsRestrictedEnabled(this, 270358, null);
        this.mPhone.getServiceStateTracker().registerForPsRestrictedDisabled(this, 270359, null);
        this.mPhone.getServiceStateTracker().registerForDataRegStateOrRatChanged(this.mTransportType, this, 270377, null);
    }

    public void unregisterServiceStateTrackerEvents() {
        this.mPhone.getServiceStateTracker().unregisterForDataConnectionAttached(this.mTransportType, this);
        this.mPhone.getServiceStateTracker().unregisterForDataConnectionDetached(this.mTransportType, this);
        this.mPhone.getServiceStateTracker().unregisterForDataRoamingOn(this);
        this.mPhone.getServiceStateTracker().unregisterForDataRoamingOff(this);
        this.mPhone.getServiceStateTracker().unregisterForPsRestrictedEnabled(this);
        this.mPhone.getServiceStateTracker().unregisterForPsRestrictedDisabled(this);
        this.mPhone.getServiceStateTracker().unregisterForDataRegStateOrRatChanged(this.mTransportType, this);
    }

    /* access modifiers changed from: protected */
    public void registerForAllEvents() {
        this.mPhone.getDeviceStateMonitor().registerForOemScreenChanged(this, 270388, null);
        if (this.mTransportType == 1) {
            this.mPhone.mCi.registerForAvailable(this, 270337, null);
            this.mPhone.mCi.registerForOffOrNotAvailable(this, 270342, null);
            this.mPhone.mCi.registerForPcoData(this, 270381, null);
        }
        this.mPhone.getCallTracker().registerForVoiceCallEnded(this, 270344, null);
        this.mPhone.getCallTracker().registerForVoiceCallStarted(this, 270343, null);
        registerServiceStateTrackerEvents();
        this.mDataServiceManager.registerForServiceBindingChanged(this, 270385, null);
    }

    public void dispose() {
        log("DCT.dispose");
        if (this.mProvisionBroadcastReceiver != null) {
            this.mPhone.getContext().unregisterReceiver(this.mProvisionBroadcastReceiver);
            this.mProvisionBroadcastReceiver = null;
        }
        ProgressDialog progressDialog = this.mProvisioningSpinner;
        if (progressDialog != null) {
            progressDialog.dismiss();
            this.mProvisioningSpinner = null;
        }
        cleanUpAllConnectionsInternal(true, null);
        this.mIsDisposed = true;
        this.mPhone.getContext().unregisterReceiver(this.mIntentReceiver);
        this.mUiccController.unregisterForIccChanged(this);
        this.mSettingsObserver.unobserve();
        this.mSubscriptionManager.removeOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        this.mDcc.dispose();
        this.mDcTesterFailBringUpAll.dispose();
        this.mPhone.getContext().getContentResolver().unregisterContentObserver(this.mApnObserver);
        this.mApnContexts.clear();
        this.mApnContextsByType.clear();
        this.mPrioritySortedApnContexts.clear();
        unregisterForAllEvents();
        destroyDataConnections();
    }

    /* access modifiers changed from: protected */
    public void unregisterForAllEvents() {
        this.mPhone.getDeviceStateMonitor().unregisterOemScreenChanged(this);
        if (this.mTransportType == 1) {
            this.mPhone.mCi.unregisterForAvailable(this);
            this.mPhone.mCi.unregisterForOffOrNotAvailable(this);
            this.mPhone.mCi.unregisterForPcoData(this);
        }
        IccRecords r = this.mIccRecords.get();
        if (r != null) {
            r.unregisterForRecordsLoaded(this);
            super.oppoUnregisterForImsiReady(r);
            this.mIccRecords.set(null);
        }
        this.mPhone.getCallTracker().unregisterForVoiceCallEnded(this);
        this.mPhone.getCallTracker().unregisterForVoiceCallStarted(this);
        unregisterServiceStateTrackerEvents();
        this.mDataServiceManager.unregisterForServiceBindingChanged(this);
        this.mDataEnabledSettings.unregisterForDataEnabledChanged(this);
        this.mDataEnabledSettings.unregisterForDataEnabledOverrideChanged(this);
    }

    /* access modifiers changed from: protected */
    public void reevaluateDataConnections() {
        for (DataConnection dataConnection : this.mDataConnections.values()) {
            dataConnection.reevaluateRestrictedState();
        }
    }

    public long getSubId() {
        return (long) this.mPhone.getSubId();
    }

    public DctConstants.Activity getActivity() {
        return this.mActivity;
    }

    private void setActivity(DctConstants.Activity activity) {
        log("setActivity = " + activity);
        this.mActivity = activity;
        this.mPhone.notifyDataActivity();
    }

    public void requestNetwork(NetworkRequest networkRequest, int type, Message onCompleteMsg) {
        ApnContext apnContext = this.mApnContextsByType.get(ApnContext.getApnTypeFromNetworkRequest(networkRequest));
        if (apnContext != null) {
            apnContext.requestNetwork(networkRequest, type, onCompleteMsg);
        }
    }

    public void releaseNetwork(NetworkRequest networkRequest, int type) {
        ApnContext apnContext = this.mApnContextsByType.get(ApnContext.getApnTypeFromNetworkRequest(networkRequest));
        if (apnContext != null) {
            apnContext.releaseNetwork(networkRequest, type);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setRadio(boolean on) {
        try {
            ITelephony.Stub.asInterface(ServiceManager.checkService("phone")).setRadio(on);
        } catch (Exception e) {
        }
    }

    /* access modifiers changed from: private */
    public class ProvisionNotificationBroadcastReceiver extends BroadcastReceiver {
        private final String mNetworkOperator;
        private final String mProvisionUrl;

        public ProvisionNotificationBroadcastReceiver(String provisionUrl, String networkOperator) {
            this.mNetworkOperator = networkOperator;
            this.mProvisionUrl = provisionUrl;
        }

        private void setEnableFailFastMobileData(int enabled) {
            DcTracker dcTracker = DcTracker.this;
            dcTracker.sendMessage(dcTracker.obtainMessage(270372, enabled, 0));
        }

        private void enableMobileProvisioning() {
            Message msg = DcTracker.this.obtainMessage(270373);
            msg.setData(Bundle.forPair("provisioningUrl", this.mProvisionUrl));
            DcTracker.this.sendMessage(msg);
        }

        public void onReceive(Context context, Intent intent) {
            DcTracker.this.log("onReceive : ProvisionNotificationBroadcastReceiver");
            DcTracker.this.mProvisioningSpinner = new ProgressDialog(context);
            DcTracker.this.mProvisioningSpinner.setTitle(this.mNetworkOperator);
            DcTracker.this.mProvisioningSpinner.setMessage(context.getText(17040315));
            DcTracker.this.mProvisioningSpinner.setIndeterminate(true);
            DcTracker.this.mProvisioningSpinner.setCancelable(true);
            DcTracker.this.mProvisioningSpinner.getWindow().setType(TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_MIP_FA_VJ_HEADER_COMPRESSION_UNAVAILABLE);
            DcTracker.this.mProvisioningSpinner.show();
            DcTracker dcTracker = DcTracker.this;
            dcTracker.sendMessageDelayed(dcTracker.obtainMessage(270378, dcTracker.mProvisioningSpinner), 120000);
            DcTracker.this.setRadio(true);
            setEnableFailFastMobileData(1);
            enableMobileProvisioning();
        }
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() {
        if (this.mPhone != null) {
            log("finalize");
        }
    }

    /* access modifiers changed from: protected */
    public ApnContext addApnContext(String type, NetworkConfig networkConfig) {
        ApnContext apnContext = new ApnContext(this.mPhone, type, this.mLogTag, networkConfig, this);
        this.mApnContexts.put(type, apnContext);
        this.mApnContextsByType.put(ApnSetting.getApnTypesBitmaskFromString(type), apnContext);
        this.mPrioritySortedApnContexts.add(apnContext);
        return apnContext;
    }

    /* access modifiers changed from: protected */
    public void initApnContexts() {
        ApnContext apnContext;
        log("initApnContexts: E");
        for (String networkConfigString : this.mPhone.getContext().getResources().getStringArray(17236107)) {
            NetworkConfig networkConfig = new NetworkConfig(networkConfigString);
            int i = networkConfig.type;
            if (i == 0) {
                apnContext = addApnContext(TransportManager.IWLAN_OPERATION_MODE_DEFAULT, networkConfig);
            } else if (i == 1001) {
                apnContext = addApnContext("mcx", networkConfig);
            } else if (i == 2) {
                apnContext = addApnContext("mms", networkConfig);
            } else if (i == 3) {
                apnContext = addApnContext("supl", networkConfig);
            } else if (i == 4) {
                apnContext = addApnContext("dun", networkConfig);
            } else if (i == 5) {
                apnContext = addApnContext("hipri", networkConfig);
            } else if (i == 14) {
                apnContext = addApnContext("ia", networkConfig);
            } else if (i != 15) {
                switch (i) {
                    case 10:
                        apnContext = addApnContext("fota", networkConfig);
                        break;
                    case 11:
                        apnContext = addApnContext("ims", networkConfig);
                        break;
                    case 12:
                        apnContext = addApnContext("cbs", networkConfig);
                        break;
                    default:
                        log("initApnContexts: skipping unknown type=" + networkConfig.type);
                        continue;
                }
            } else {
                apnContext = addApnContext("emergency", networkConfig);
            }
            log("initApnContexts: apnContext=" + apnContext);
        }
    }

    public LinkProperties getLinkProperties(String apnType) {
        DataConnection dataConnection;
        ApnContext apnContext = this.mApnContexts.get(apnType);
        if (apnContext == null || (dataConnection = apnContext.getDataConnection()) == null) {
            log("return new LinkProperties");
            return new LinkProperties();
        }
        log("return link properties for " + apnType);
        return dataConnection.getLinkProperties();
    }

    public NetworkCapabilities getNetworkCapabilities(String apnType) {
        DataConnection dataConnection;
        ApnContext apnContext = this.mApnContexts.get(apnType);
        if (apnContext == null || (dataConnection = apnContext.getDataConnection()) == null) {
            log("return new NetworkCapabilities");
            return new NetworkCapabilities();
        }
        log("get active pdp is not null, return NetworkCapabilities for " + apnType);
        return dataConnection.getNetworkCapabilities();
    }

    public String[] getActiveApnTypes() {
        log("get all active apn types");
        ArrayList<String> result = new ArrayList<>();
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (this.mAttached.get() && apnContext.isReady()) {
                result.add(apnContext.getApnType());
            }
        }
        return (String[]) result.toArray(new String[0]);
    }

    public String getActiveApnString(String apnType) {
        ApnSetting apnSetting;
        ApnContext apnContext = this.mApnContexts.get(apnType);
        if (apnContext == null || (apnSetting = apnContext.getApnSetting()) == null) {
            return null;
        }
        return apnSetting.getApnName();
    }

    public DctConstants.State getState(String apnType) {
        DctConstants.State state = DctConstants.State.IDLE;
        int apnTypeBitmask = ApnSetting.getApnTypesBitmaskFromString(apnType);
        for (DataConnection dc : this.mDataConnections.values()) {
            ApnSetting apnSetting = dc.getApnSetting();
            if (apnSetting != null && apnSetting.canHandleType(apnTypeBitmask)) {
                if (dc.isActive()) {
                    state = getBetterConnectionState(state, DctConstants.State.CONNECTED);
                } else if (dc.isActivating()) {
                    state = getBetterConnectionState(state, DctConstants.State.CONNECTING);
                } else if (dc.isInactive()) {
                    state = getBetterConnectionState(state, DctConstants.State.IDLE);
                } else if (dc.isDisconnecting()) {
                    state = getBetterConnectionState(state, DctConstants.State.DISCONNECTING);
                }
            }
        }
        return state;
    }

    private static DctConstants.State getBetterConnectionState(DctConstants.State stateA, DctConstants.State stateB) {
        return ArrayUtils.indexOf(DATA_CONNECTION_STATE_PRIORITIES, stateA) >= ArrayUtils.indexOf(DATA_CONNECTION_STATE_PRIORITIES, stateB) ? stateA : stateB;
    }

    private boolean isProvisioningApn(String apnType) {
        ApnContext apnContext = this.mApnContexts.get(apnType);
        if (apnContext != null) {
            return apnContext.isProvisioningApn();
        }
        return false;
    }

    public DctConstants.State getOverallState() {
        boolean isConnecting = false;
        boolean isFailed = true;
        boolean isAnyEnabled = false;
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (apnContext.isEnabled()) {
                isAnyEnabled = true;
                int i = AnonymousClass6.$SwitchMap$com$android$internal$telephony$DctConstants$State[apnContext.getState().ordinal()];
                if (i == 1 || i == 2) {
                    return DctConstants.State.CONNECTED;
                }
                if (i == 3) {
                    isConnecting = true;
                    isFailed = false;
                } else if (i == 4 || i == 5) {
                    isFailed = false;
                } else {
                    isAnyEnabled = true;
                }
            }
        }
        if (!isAnyEnabled) {
            return DctConstants.State.IDLE;
        }
        if (isConnecting) {
            return DctConstants.State.CONNECTING;
        }
        if (!isFailed) {
            return DctConstants.State.IDLE;
        }
        return DctConstants.State.FAILED;
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.internal.telephony.dataconnection.DcTracker$6  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass6 {
        static final /* synthetic */ int[] $SwitchMap$com$android$internal$telephony$DctConstants$State = new int[DctConstants.State.values().length];

        static {
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.CONNECTED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.DISCONNECTING.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.CONNECTING.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.IDLE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.RETRYING.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$android$internal$telephony$DctConstants$State[DctConstants.State.FAILED.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onDataConnectionDetached() {
        log("onDataConnectionDetached: stop polling and notify detached");
        stopNetStatPoll();
        stopDataStallAlarm();
        this.mPhone.notifyDataConnection();
        this.mAttached.set(false);
    }

    private void onDataConnectionAttached() {
        log("onDataConnectionAttached");
        this.mAttached.set(true);
        if (getOverallState() == DctConstants.State.CONNECTED) {
            log("onDataConnectionAttached: start polling notify attached");
            startNetStatPoll();
            startDataStallAlarm(false);
            this.mPhone.notifyDataConnection();
        }
        if (this.mAutoAttachOnCreationConfig) {
            this.mAutoAttachEnabled.set(true);
        }
        setupDataOnAllConnectableApns(PhoneInternalInterface.REASON_DATA_ATTACHED, RetryFailures.ALWAYS);
    }

    public boolean isDataAllowed(DataConnectionReasons dataConnectionReasons) {
        return isDataAllowed(null, 1, dataConnectionReasons);
    }

    public boolean isDataAllowed(ApnContext apnContext, int requestType, DataConnectionReasons dataConnectionReasons) {
        boolean isDataEnabled;
        if (((IOppoDataManager) OppoTelephonyFactory.getInstance().getFeature(IOppoDataManager.DEFAULT, new Object[0])).isDataAllowByPolicy(this.mPhone)) {
            return false;
        }
        DataConnectionReasons reasons = new DataConnectionReasons();
        boolean internalDataEnabled = this.mDataEnabledSettings.isInternalDataEnabled();
        boolean attachedState = this.mAttached.get();
        boolean desiredPowerState = this.mPhone.getServiceStateTracker().getDesiredPowerState();
        boolean radioStateFromCarrier = this.mPhone.getServiceStateTracker().getPowerStateFromCarrier();
        int dataRat = getDataRat();
        if (dataRat == 18) {
            desiredPowerState = true;
            radioStateFromCarrier = true;
        }
        boolean recordsLoaded = this.mIccRecords.get() != null && this.mIccRecords.get().getRecordsLoaded();
        boolean defaultDataSelected = SubscriptionManager.isValidSubscriptionId(SubscriptionManager.getDefaultDataSubscriptionId());
        boolean isMeteredApnType = apnContext == null || ApnSettingUtils.isMeteredApnType(ApnSetting.getApnTypesBitmaskFromString(apnContext.getApnType()), this.mPhone);
        PhoneConstants.State phoneState = PhoneConstants.State.IDLE;
        if (this.mPhone.getCallTracker() != null) {
            phoneState = this.mPhone.getCallTracker().getState();
        }
        if (apnContext == null || !apnContext.getApnType().equals("emergency") || !apnContext.isConnectable()) {
            if (apnContext != null && !apnContext.isConnectable()) {
                reasons.add(DataConnectionReasons.DataDisallowedReasonType.APN_NOT_CONNECTABLE);
            }
            if (apnContext != null && ((apnContext.getApnType().equals(TransportManager.IWLAN_OPERATION_MODE_DEFAULT) || apnContext.getApnType().equals("ia")) && this.mPhone.getTransportManager().isInLegacyMode() && dataRat == 18)) {
                reasons.add(DataConnectionReasons.DataDisallowedReasonType.ON_IWLAN);
            }
            if (isEmergency()) {
                reasons.add(DataConnectionReasons.DataDisallowedReasonType.IN_ECBM);
            }
            if (!attachedState && !shouldAutoAttach()) {
                if (requestType != 2) {
                    reasons.add(DataConnectionReasons.DataDisallowedReasonType.NOT_ATTACHED);
                }
            }
            if (!recordsLoaded) {
                reasons.add(DataConnectionReasons.DataDisallowedReasonType.RECORD_NOT_LOADED);
            }
            if (phoneState != PhoneConstants.State.IDLE && !this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed()) {
                reasons.add(DataConnectionReasons.DataDisallowedReasonType.INVALID_PHONE_STATE);
                reasons.add(DataConnectionReasons.DataDisallowedReasonType.CONCURRENT_VOICE_DATA_NOT_ALLOWED);
            }
            if (!internalDataEnabled) {
                reasons.add(DataConnectionReasons.DataDisallowedReasonType.INTERNAL_DATA_DISABLED);
            }
            if (!defaultDataSelected) {
                reasons.add(DataConnectionReasons.DataDisallowedReasonType.DEFAULT_DATA_UNSELECTED);
            }
            if (this.mPhone.getServiceState().getDataRoaming() && !getDataRoamingEnabled()) {
                reasons.add(DataConnectionReasons.DataDisallowedReasonType.ROAMING_DISABLED);
            }
            if (this.mIsPsRestricted) {
                reasons.add(DataConnectionReasons.DataDisallowedReasonType.PS_RESTRICTED);
            }
            if (!desiredPowerState) {
                reasons.add(DataConnectionReasons.DataDisallowedReasonType.UNDESIRED_POWER_STATE);
            }
            if (!radioStateFromCarrier) {
                reasons.add(DataConnectionReasons.DataDisallowedReasonType.RADIO_DISABLED_BY_CARRIER);
            }
            if (apnContext == null) {
                isDataEnabled = this.mDataEnabledSettings.isDataEnabled();
            } else {
                isDataEnabled = this.mDataEnabledSettings.isDataEnabled(apnContext.getApnTypeBitmask());
            }
            if (!isDataEnabled) {
                reasons.add(DataConnectionReasons.DataDisallowedReasonType.DATA_DISABLED);
            }
            if (!reasons.containsHardDisallowedReasons()) {
                if (!reasons.allowed()) {
                    if (this.mTransportType == 2 || (this.mPhone.getTransportManager().isInLegacyMode() && dataRat == 18)) {
                        reasons.add(DataConnectionReasons.DataAllowedReasonType.UNMETERED_APN);
                    } else if (this.mTransportType == 1 && !isMeteredApnType) {
                        reasons.add(DataConnectionReasons.DataAllowedReasonType.UNMETERED_APN);
                    }
                    if (apnContext != null && apnContext.hasRestrictedRequests(true) && !reasons.allowed()) {
                        reasons.add(DataConnectionReasons.DataAllowedReasonType.RESTRICTED_REQUEST);
                    }
                } else {
                    reasons.add(DataConnectionReasons.DataAllowedReasonType.NORMAL);
                }
                if (dataConnectionReasons != null) {
                    dataConnectionReasons.copyFrom(reasons);
                }
                return reasons.allowed();
            } else if (dataConnectionReasons == null) {
                return false;
            } else {
                dataConnectionReasons.copyFrom(reasons);
                return false;
            }
        } else if (dataConnectionReasons == null) {
            return true;
        } else {
            dataConnectionReasons.add(DataConnectionReasons.DataAllowedReasonType.EMERGENCY_APN);
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public void setupDataOnAllConnectableApns(String reason, RetryFailures retryFailures) {
        StringBuilder sb = new StringBuilder((int) TelephonyProto.TelephonyEvent.RilSetupDataCallResponse.RilDataCallFailCause.PDP_FAIL_IFACE_AND_POL_FAMILY_MISMATCH);
        Iterator<ApnContext> it = this.mPrioritySortedApnContexts.iterator();
        while (it.hasNext()) {
            ApnContext apnContext = it.next();
            sb.append(apnContext.getApnType());
            sb.append(":[state=");
            sb.append(apnContext.getState());
            sb.append(",enabled=");
            sb.append(apnContext.isEnabled());
            sb.append("] ");
        }
        log("setupDataOnAllConnectableApns: " + reason + " " + ((Object) sb));
        Iterator<ApnContext> it2 = this.mPrioritySortedApnContexts.iterator();
        while (it2.hasNext()) {
            setupDataOnConnectableApn(it2.next(), reason, retryFailures);
        }
    }

    /* access modifiers changed from: protected */
    public void setupDataOnConnectableApn(ApnContext apnContext, String reason, RetryFailures retryFailures) {
        if (apnContext.getState() == DctConstants.State.FAILED || apnContext.getState() == DctConstants.State.RETRYING) {
            if (retryFailures == RetryFailures.ALWAYS) {
                apnContext.releaseDataConnection(reason);
            } else if (!apnContext.isConcurrentVoiceAndDataAllowed() && this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed()) {
                apnContext.releaseDataConnection(reason);
            }
        }
        if (apnContext.isConnectable()) {
            log("isConnectable() call trySetupData");
            if (!super.oppoWlanAssistantBlockTrySetupData(apnContext, reason)) {
                apnContext.setReason(reason);
                trySetupData(apnContext, 1);
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean isEmergency() {
        boolean result = this.mPhone.isInEcm() || this.mPhone.isInEmergencyCall();
        log("isEmergency: result=" + result);
        return result;
    }

    /* access modifiers changed from: protected */
    public boolean trySetupData(ApnContext apnContext, int requestType) {
        if (super.needManualSelectAPN(apnContext.getApnType(), this.mPreferredApn)) {
            log("need manual select APN, trySetupData return false");
            return false;
        } else if (this.mPhone.getSimulatedRadioControl() != null) {
            apnContext.setState(DctConstants.State.CONNECTED);
            this.mPhone.notifyDataConnection(apnContext.getApnType());
            log("trySetupData: X We're on the simulator; assuming connected retValue=true");
            return true;
        } else {
            DataConnectionReasons dataConnectionReasons = new DataConnectionReasons();
            boolean isDataAllowed = isDataAllowed(apnContext, requestType, dataConnectionReasons);
            String logStr = "trySetupData for APN type " + apnContext.getApnType() + ", reason: " + apnContext.getReason() + ", requestType=" + requestTypeToString(requestType) + ". " + dataConnectionReasons.toString();
            log(logStr);
            apnContext.requestLog(logStr);
            if (isDataAllowed) {
                if (apnContext.getState() == DctConstants.State.FAILED) {
                    log("trySetupData: make a FAILED ApnContext IDLE so its reusable");
                    apnContext.requestLog("trySetupData: make a FAILED ApnContext IDLE so its reusable");
                    apnContext.setState(DctConstants.State.IDLE);
                }
                int radioTech = getDataRat();
                if (radioTech == 0) {
                    radioTech = getVoiceRat();
                }
                log("service state=" + this.mPhone.getServiceState());
                apnContext.setConcurrentVoiceAndDataAllowed(this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed());
                if (apnContext.getState() == DctConstants.State.IDLE) {
                    ArrayList<ApnSetting> waitingApns = buildWaitingApns(apnContext.getApnType(), radioTech);
                    if (waitingApns.isEmpty()) {
                        notifyNoData(27, apnContext);
                        log("trySetupData: X No APN found retValue=false");
                        apnContext.requestLog("trySetupData: X No APN found retValue=false");
                        int log_type = -1;
                        String log_desc = PhoneConfigurationManager.SSSS;
                        try {
                            String[] log_array = OemTelephonyUtils.getOemRes(this.mPhone.getContext(), "zz_oppo_critical_log_111", PhoneConfigurationManager.SSSS).split(",");
                            log_type = Integer.valueOf(log_array[0]).intValue();
                            log_desc = log_array[1];
                        } catch (Exception e) {
                        }
                        OppoManager.writeLogToPartition(log_type, getCellLocation() + ", trySetupData: X No APN found", "NETWORK", "data_no_available_apn", log_desc);
                        return false;
                    }
                    apnContext.setWaitingApns(waitingApns);
                    log("trySetupData: Create from mAllApnSettings : " + apnListToString(this.mAllApnSettings));
                }
                boolean retValue = setupData(apnContext, radioTech, requestType);
                log("trySetupData: X retValue=" + retValue);
                return retValue;
            }
            StringBuilder str = new StringBuilder();
            str.append("trySetupData failed. apnContext = [type=" + apnContext.getApnType() + ", mState=" + apnContext.getState() + ", apnEnabled=" + apnContext.isEnabled() + ", mDependencyMet=" + apnContext.isDependencyMet() + "] ");
            if (!this.mDataEnabledSettings.isDataEnabled()) {
                str.append("isDataEnabled() = false. " + this.mDataEnabledSettings);
            }
            if (apnContext.getState() == DctConstants.State.RETRYING) {
                apnContext.setState(DctConstants.State.FAILED);
                str.append(" Stop retrying.");
            }
            log(str.toString());
            apnContext.requestLog(str.toString());
            return false;
        }
    }

    public void cleanUpAllConnections(String reason) {
        log("cleanUpAllConnections");
        Message msg = obtainMessage(270365);
        msg.obj = reason;
        sendMessage(msg);
    }

    /* access modifiers changed from: protected */
    public boolean cleanUpAllConnectionsInternal(boolean detach, String reason) {
        log("cleanUpAllConnectionsInternal: detach=" + detach + " reason=" + reason);
        boolean didDisconnect = false;
        boolean disableMeteredOnly = false;
        if (!TextUtils.isEmpty(reason)) {
            disableMeteredOnly = reason.equals(PhoneInternalInterface.REASON_DATA_SPECIFIC_DISABLED) || reason.equals(PhoneInternalInterface.REASON_ROAMING_ON) || reason.equals(PhoneInternalInterface.REASON_CARRIER_ACTION_DISABLE_METERED_APN);
        }
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (!reason.equals(PhoneInternalInterface.REASON_SINGLE_PDN_ARBITRATION) || !apnContext.getApnType().equals("ims")) {
                if (shouldCleanUpConnection(apnContext, disableMeteredOnly)) {
                    if (!apnContext.isDisconnected()) {
                        didDisconnect = true;
                    }
                    apnContext.setReason(reason);
                    cleanUpConnectionInternal(detach, 2, apnContext);
                } else {
                    log("cleanUpAllConnectionsInternal: APN type " + apnContext.getApnType() + " shouldn't be cleaned up.");
                }
            }
        }
        stopNetStatPoll();
        stopDataStallAlarm();
        this.mRequestedApnType = 17;
        log("cleanUpAllConnectionsInternal: mDisconnectPendingCount = " + this.mDisconnectPendingCount);
        if (detach && this.mDisconnectPendingCount == 0) {
            notifyAllDataDisconnected();
        }
        return didDisconnect;
    }

    /* access modifiers changed from: protected */
    public boolean shouldCleanUpConnection(ApnContext apnContext, boolean disableMeteredOnly) {
        if (apnContext == null) {
            return false;
        }
        if (!disableMeteredOnly) {
            return true;
        }
        ApnSetting apnSetting = apnContext.getApnSetting();
        if (apnSetting == null || !ApnSettingUtils.isMetered(apnSetting, this.mPhone)) {
            return false;
        }
        boolean isRoaming = this.mPhone.getServiceState().getDataRoaming();
        boolean isDataRoamingDisabled = !getDataRoamingEnabled();
        if ((!this.mDataEnabledSettings.isDataEnabled(apnSetting.getApnTypeBitmask())) || (isRoaming && isDataRoamingDisabled)) {
            return true;
        }
        return false;
    }

    public void cleanUpConnection(ApnContext apnContext) {
        log("cleanUpConnection: apnContext=" + apnContext);
        Message msg = obtainMessage(270360);
        msg.arg2 = 0;
        msg.obj = apnContext;
        sendMessage(msg);
    }

    /* access modifiers changed from: protected */
    public void cleanUpConnectionInternal(boolean detach, int releaseType, ApnContext apnContext) {
        if (apnContext == null) {
            log("cleanUpConnectionInternal: apn context is null");
            return;
        }
        DataConnection dataConnection = apnContext.getDataConnection();
        apnContext.requestLog("cleanUpConnectionInternal: detach=" + detach + " reason=" + apnContext.getReason());
        if (detach) {
            boolean isDisconnected = apnContext.isDisconnected();
            String str = PhoneConfigurationManager.SSSS;
            if (isDisconnected) {
                apnContext.releaseDataConnection(str);
            } else if (dataConnection == null) {
                apnContext.setState(DctConstants.State.IDLE);
                apnContext.requestLog("cleanUpConnectionInternal: connected, bug no dc");
                if (mtkIsNeedNotify(apnContext)) {
                    this.mPhone.notifyDataConnection(apnContext.getApnType());
                }
            } else if (apnContext.getState() != DctConstants.State.DISCONNECTING) {
                boolean disconnectAll = false;
                if ("dun".equals(apnContext.getApnType()) && ServiceState.isCdma(getDataRat())) {
                    log("cleanUpConnectionInternal: disconnectAll DUN connection");
                    disconnectAll = true;
                }
                int generation = apnContext.getConnectionGeneration();
                StringBuilder sb = new StringBuilder();
                sb.append("cleanUpConnectionInternal: tearing down");
                if (disconnectAll) {
                    str = " all";
                }
                sb.append(str);
                sb.append(" using gen#");
                sb.append(generation);
                String str2 = sb.toString();
                log(str2 + "apnContext=" + apnContext);
                apnContext.requestLog(str2);
                Message msg = obtainMessage(270351, new Pair<>(apnContext, Integer.valueOf(generation)));
                if (disconnectAll || releaseType == 3) {
                    dataConnection.tearDownAll(apnContext.getReason(), releaseType, msg);
                } else {
                    dataConnection.tearDown(apnContext, apnContext.getReason(), msg);
                }
                mtkTearDown(apnContext, msg);
                apnContext.setState(DctConstants.State.DISCONNECTING);
                this.mDisconnectPendingCount++;
            }
        } else {
            if (dataConnection != null) {
                dataConnection.reset();
            }
            apnContext.setState(DctConstants.State.IDLE);
            if (mtkIsNeedNotify(apnContext)) {
                this.mPhone.notifyDataConnection(apnContext.getApnType());
            }
            apnContext.setDataConnection(null);
        }
        if (dataConnection != null) {
            cancelReconnectAlarm(apnContext);
        }
        String str3 = "cleanUpConnectionInternal: X detach=" + detach + " reason=" + apnContext.getReason();
        if (mtkIsNeedNotify(apnContext)) {
            log(str3 + " apnContext=" + apnContext + " dc=" + apnContext.getDataConnection());
        }
    }

    @VisibleForTesting
    public ArrayList<ApnSetting> fetchDunApns() {
        if (SystemProperties.getBoolean("net.tethering.noprovisioning", false)) {
            log("fetchDunApns: net.tethering.noprovisioning=true ret: empty list");
            return new ArrayList<>(0);
        }
        ArrayList<ApnSetting> dunList = super.getDunApnList(this.mAllApnSettings);
        if (dunList != null) {
            return dunList;
        }
        int bearer = getDataRat();
        ArrayList<ApnSetting> dunCandidates = new ArrayList<>();
        ArrayList<ApnSetting> retDunSettings = new ArrayList<>();
        String apnData = Settings.Global.getString(this.mResolver, "tether_dun_apn");
        if (!TextUtils.isEmpty(apnData)) {
            dunCandidates.addAll(ApnSetting.arrayFromString(apnData));
        }
        if (dunCandidates.isEmpty()) {
            synchronized (this.mRefCountLock) {
                if (!ArrayUtils.isEmpty(this.mAllApnSettings)) {
                    Iterator<ApnSetting> it = this.mAllApnSettings.iterator();
                    while (it.hasNext()) {
                        ApnSetting apn = it.next();
                        if (apn.canHandleType(8) && !apn.canHandleType(17)) {
                            dunCandidates.add(apn);
                        }
                    }
                }
            }
        }
        Iterator<ApnSetting> it2 = dunCandidates.iterator();
        while (it2.hasNext()) {
            ApnSetting dunSetting = it2.next();
            if (dunSetting.canSupportNetworkType(ServiceState.rilRadioTechnologyToNetworkType(bearer))) {
                retDunSettings.add(dunSetting);
            }
        }
        return retDunSettings;
    }

    private int getPreferredApnSetId() {
        int setId;
        ContentResolver contentResolver = this.mPhone.getContext().getContentResolver();
        Uri uri = Telephony.Carriers.CONTENT_URI;
        Cursor c = contentResolver.query(Uri.withAppendedPath(uri, "preferapnset/subId/" + this.mPhone.getSubId()), new String[]{"apn_set_id"}, null, null, null);
        if (c == null) {
            loge("getPreferredApnSetId: cursor is null");
            return 0;
        }
        if (c.getCount() < 1) {
            loge("getPreferredApnSetId: no APNs found");
            setId = 0;
        } else {
            c.moveToFirst();
            setId = c.getInt(0);
        }
        if (!c.isClosed()) {
            c.close();
        }
        return setId;
    }

    public boolean hasMatchedTetherApnSetting() {
        ArrayList<ApnSetting> matches = fetchDunApns();
        log("hasMatchedTetherApnSetting: APNs=" + matches);
        return matches.size() > 0;
    }

    public DataConnection getDataConnectionByContextId(int cid) {
        return this.mDcc.getActiveDcByCid(cid);
    }

    public DataConnection getDataConnectionByApnType(String apnType) {
        ApnContext apnContext = this.mApnContexts.get(apnType);
        if (apnContext != null) {
            return apnContext.getDataConnection();
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void cancelReconnectAlarm(ApnContext apnContext) {
        PendingIntent intent;
        if (apnContext != null && (intent = apnContext.getReconnectIntent()) != null) {
            ((AlarmManager) this.mPhone.getContext().getSystemService("alarm")).cancel(intent);
            apnContext.setReconnectIntent(null);
        }
    }

    public boolean isPermanentFailure(int dcFailCause) {
        return DataFailCause.isPermanentFailure(this.mPhone.getContext(), dcFailCause, this.mPhone.getSubId()) && (!this.mAttached.get() || dcFailCause != -3);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x003d A[EDGE_INSN: B:19:0x003d->B:11:0x003d ?: BREAK  , SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:6:0x0027  */
    public DataConnection findFreeDataConnection() {
        for (DataConnection dataConnection : this.mDataConnections.values()) {
            boolean inUse = false;
            Iterator<ApnContext> it = this.mApnContexts.values().iterator();
            while (true) {
                if (!it.hasNext()) {
                    ApnContext apnContext = it.next();
                    if (apnContext.getDataConnection() == dataConnection || mtkIsInUse(apnContext, dataConnection)) {
                        inUse = true;
                    }
                    if (!it.hasNext()) {
                        break;
                    }
                }
            }
            inUse = true;
            continue;
            if (!inUse) {
                log("findFreeDataConnection: found free DataConnection=" + dataConnection);
                return dataConnection;
            }
        }
        log("findFreeDataConnection: NO free DataConnection");
        return null;
    }

    /* access modifiers changed from: protected */
    public boolean setupData(ApnContext apnContext, int radioTech, int requestType) {
        int profileId;
        ApnSetting apnSetting;
        DataConnection dataConnection;
        ApnSetting dataConnectionApnSetting;
        log("setupData: apnContext=" + apnContext + ", requestType=" + requestTypeToString(requestType));
        StringBuilder sb = new StringBuilder();
        sb.append("setupData. requestType=");
        sb.append(requestTypeToString(requestType));
        apnContext.requestLog(sb.toString());
        DataConnection dataConnection2 = null;
        ApnSetting apnSetting2 = apnContext.getNextApnSetting();
        if (apnSetting2 == null) {
            log("setupData: return for no apn found!");
            return false;
        }
        if (apnSetting2.isPersistent()) {
            int profileId2 = apnSetting2.getProfileId();
            if (profileId2 == 0) {
                profileId = getApnProfileID(apnContext.getApnType());
            } else {
                profileId = profileId2;
            }
        } else {
            profileId = -1;
        }
        if (!apnContext.getApnType().equals("dun") || ServiceState.isGsm(getDataRat())) {
            dataConnection2 = checkForCompatibleConnectedApnContext(apnContext);
            if (dataConnection2 == null || (dataConnectionApnSetting = dataConnection2.getApnSetting()) == null) {
                apnSetting = apnSetting2;
            } else {
                apnSetting = dataConnectionApnSetting;
            }
        } else {
            apnSetting = apnSetting2;
        }
        if (dataConnection2 == null) {
            if (isOnlySingleDcAllowed(radioTech)) {
                if (isHigherPriorityApnContextActive(apnContext)) {
                    log("setupData: Higher priority ApnContext active.  Ignoring call");
                    return false;
                } else if (apnContext.getApnType().equals("ims") || !cleanUpAllConnectionsInternal(true, PhoneInternalInterface.REASON_SINGLE_PDN_ARBITRATION)) {
                    log("setupData: Single pdp. Continue setting up data call.");
                } else {
                    log("setupData: Some calls are disconnecting first. Wait and retry");
                    return false;
                }
            }
            DataConnection dataConnection3 = findFreeDataConnection();
            if (dataConnection3 == null) {
                dataConnection3 = createDataConnection();
            }
            if (dataConnection3 == null) {
                log("setupData: No free DataConnection and couldn't create one, WEIRD");
                return false;
            }
            dataConnection = dataConnection3;
        } else {
            dataConnection = dataConnection2;
        }
        int generation = apnContext.incAndGetConnectionGeneration();
        log("setupData: dc=" + dataConnection + " apnSetting=" + apnSetting + " gen#=" + generation);
        apnContext.setDataConnection(dataConnection);
        apnContext.setApnSetting(apnSetting);
        apnContext.setState(DctConstants.State.CONNECTING);
        this.mPhone.notifyDataConnection(apnContext.getApnType());
        Message msg = obtainMessage();
        msg.what = 270336;
        msg.obj = new Pair(apnContext, Integer.valueOf(generation));
        dataConnection.bringUp(apnContext, profileId, radioTech, msg, generation, requestType, this.mPhone.getSubId());
        log("setupData: initing!");
        return true;
    }

    /* access modifiers changed from: protected */
    public void setInitialAttachApn() {
        ApnSetting iaApnSetting = null;
        ApnSetting defaultApnSetting = null;
        ApnSetting firstNonEmergencyApnSetting = null;
        if (super.needManualSelectAPN(this.mPreferredApn)) {
            log("need manual select APN, setInitialAttachApn return");
            return;
        }
        log("setInitialApn: E mPreferredApn=" + this.mPreferredApn);
        ApnSetting apnSetting = this.mPreferredApn;
        if (apnSetting != null && apnSetting.canHandleType(256)) {
            iaApnSetting = this.mPreferredApn;
        } else if (!this.mAllApnSettings.isEmpty()) {
            Iterator<ApnSetting> it = this.mAllApnSettings.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                ApnSetting apn = it.next();
                if (firstNonEmergencyApnSetting == null && !apn.canHandleType(512)) {
                    firstNonEmergencyApnSetting = apn;
                    log("setInitialApn: firstNonEmergencyApnSetting=" + firstNonEmergencyApnSetting);
                }
                if (apn.canHandleType(256)) {
                    log("setInitialApn: iaApnSetting=" + apn);
                    iaApnSetting = apn;
                    break;
                } else if (defaultApnSetting == null && apn.canHandleType(17)) {
                    log("setInitialApn: defaultApnSetting=" + apn);
                    defaultApnSetting = apn;
                }
            }
        }
        ApnSetting initialAttachApnSetting = null;
        if (iaApnSetting != null) {
            log("setInitialAttachApn: using iaApnSetting");
            initialAttachApnSetting = iaApnSetting;
        } else if (this.mPreferredApn != null) {
            log("setInitialAttachApn: using mPreferredApn");
            initialAttachApnSetting = this.mPreferredApn;
        } else if (defaultApnSetting != null) {
            log("setInitialAttachApn: using defaultApnSetting");
            initialAttachApnSetting = defaultApnSetting;
        } else if (firstNonEmergencyApnSetting != null) {
            log("setInitialAttachApn: using firstNonEmergencyApnSetting");
            initialAttachApnSetting = firstNonEmergencyApnSetting;
        }
        if (initialAttachApnSetting == null) {
            log("setInitialAttachApn: X There in no available apn");
            return;
        }
        log("setInitialAttachApn: X selected Apn=" + initialAttachApnSetting);
        this.mDataServiceManager.setInitialAttachApn(createDataProfile(initialAttachApnSetting, initialAttachApnSetting.equals(getPreferredApn())), this.mPhone.getServiceState().getDataRoamingFromRegistration(), null);
    }

    /* access modifiers changed from: protected */
    public void onApnChanged() {
        DctConstants.State overallState = getOverallState();
        boolean z = false;
        boolean isDisconnected = overallState == DctConstants.State.IDLE || overallState == DctConstants.State.FAILED;
        Phone phone = this.mPhone;
        if (phone instanceof GsmCdmaPhone) {
            ((GsmCdmaPhone) phone).updateCurrentCarrierInProvider();
        }
        log("onApnChanged: createAllApnList and cleanUpAllConnections");
        createAllApnList();
        setDataProfilesAsNeeded();
        setInitialAttachApn();
        if (!isDisconnected) {
            z = true;
        }
        cleanUpConnectionsOnUpdatedApns(z, PhoneInternalInterface.REASON_APN_CHANGED);
        if (this.mPhone.getSubId() == SubscriptionManager.getDefaultDataSubscriptionId()) {
            setupDataOnAllConnectableApns(PhoneInternalInterface.REASON_APN_CHANGED, RetryFailures.ALWAYS);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isHigherPriorityApnContextActive(ApnContext apnContext) {
        if (apnContext.getApnType().equals("ims")) {
            return false;
        }
        Iterator<ApnContext> it = this.mPrioritySortedApnContexts.iterator();
        while (it.hasNext()) {
            ApnContext otherContext = it.next();
            if (!otherContext.getApnType().equals("ims")) {
                if (apnContext.getApnType().equalsIgnoreCase(otherContext.getApnType())) {
                    return false;
                }
                if (otherContext.isEnabled() && otherContext.getState() != DctConstants.State.FAILED) {
                    return true;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isOnlySingleDcAllowed(int rilRadioTech) {
        PersistableBundle bundle;
        int[] singleDcRats = null;
        CarrierConfigManager configManager = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        if (!(configManager == null || (bundle = configManager.getConfigForSubId(this.mPhone.getSubId())) == null)) {
            singleDcRats = bundle.getIntArray("only_single_dc_allowed_int_array");
        }
        boolean onlySingleDcAllowed = false;
        if (Build.IS_DEBUGGABLE && SystemProperties.getBoolean("persist.telephony.test.singleDc", false)) {
            onlySingleDcAllowed = true;
        }
        if (singleDcRats != null) {
            for (int i = 0; i < singleDcRats.length && !onlySingleDcAllowed; i++) {
                if (rilRadioTech == singleDcRats[i]) {
                    onlySingleDcAllowed = true;
                }
            }
        }
        log("isOnlySingleDcAllowed(" + rilRadioTech + "): " + onlySingleDcAllowed);
        return onlySingleDcAllowed;
    }

    public void sendRestartRadio() {
        log("sendRestartRadio:");
        sendMessage(obtainMessage(270362));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void restartRadio() {
        log("restartRadio: ************TURN OFF RADIO**************");
        cleanUpAllConnectionsInternal(true, PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
        this.mPhone.getServiceStateTracker().powerOffRadioSafely();
        try {
            SystemProperties.set("net.ppp.reset-by-timeout", String.valueOf(Integer.parseInt(SystemProperties.get("net.ppp.reset-by-timeout", OppoModemLogManager.DEFAULT_MODEMDUMP_POSTBACK)) + 1));
        } catch (RuntimeException e) {
            log("Failed to set net.ppp.reset-by-timeout");
        }
    }

    /* access modifiers changed from: protected */
    public boolean retryAfterDisconnected(ApnContext apnContext) {
        boolean retry = true;
        if (PhoneInternalInterface.REASON_RADIO_TURNED_OFF.equals(apnContext.getReason()) || (isOnlySingleDcAllowed(getDataRat()) && isHigherPriorityApnContextActive(apnContext))) {
            retry = false;
        }
        super.checkIfRetryAfterDisconnected(apnContext, retry);
        return retry;
    }

    private void startAlarmForReconnect(long delay, ApnContext apnContext) {
        String apnType = apnContext.getApnType();
        Intent intent = new Intent("com.android.internal.telephony.data-reconnect." + apnType);
        intent.putExtra(INTENT_RECONNECT_ALARM_EXTRA_REASON, apnContext.getReason());
        intent.putExtra(INTENT_RECONNECT_ALARM_EXTRA_TYPE, apnType);
        intent.putExtra(INTENT_RECONNECT_ALARM_EXTRA_TRANSPORT_TYPE, this.mTransportType);
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
        intent.addFlags(268435456);
        log("startAlarmForReconnect: delay=" + delay + " action=" + intent.getAction() + " apn=" + apnContext);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this.mPhone.getContext(), this.mPhone.getPhoneId() + 1, intent, 201326592);
        apnContext.setReconnectIntent(alarmIntent);
        this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + delay, alarmIntent);
    }

    /* access modifiers changed from: protected */
    public void notifyNoData(int lastFailCauseCode, ApnContext apnContext) {
        log("notifyNoData: type=" + apnContext.getApnType());
        if (isPermanentFailure(lastFailCauseCode) && !apnContext.getApnType().equals(TransportManager.IWLAN_OPERATION_MODE_DEFAULT)) {
            this.mPhone.notifyDataConnectionFailed(apnContext.getApnType());
        }
    }

    /* access modifiers changed from: protected */
    public void onRecordsLoadedOrSubIdChanged() {
        log("onRecordsLoadedOrSubIdChanged: createAllApnList");
        if (this.mTransportType == 1) {
            this.mAutoAttachOnCreationConfig = this.mPhone.getContext().getResources().getBoolean(17891366);
        }
        createAllApnList();
        setDataProfilesAsNeeded();
        setInitialAttachApn();
        this.mPhone.notifyDataConnection();
        setupDataOnAllConnectableApns(PhoneInternalInterface.REASON_SIM_LOADED, RetryFailures.ALWAYS);
    }

    /* access modifiers changed from: protected */
    public void onSimNotReady() {
        log("onSimNotReady");
        cleanUpAllConnectionsInternal(true, PhoneInternalInterface.REASON_SIM_NOT_READY);
        synchronized (this.mRefCountLock) {
            this.mAllApnSettings.clear();
        }
        this.mAutoAttachOnCreationConfig = false;
        this.mAutoAttachEnabled.set(false);
        this.mOnSubscriptionsChangedListener.mPreviousSubId.set(-1);
        createAllApnList();
        setDataProfilesAsNeeded();
    }

    private DataConnection checkForCompatibleConnectedApnContext(ApnContext apnContext) {
        if (mtkSkipCheckForCompatibleConnectedApnContext(apnContext)) {
            return null;
        }
        int apnType = apnContext.getApnTypeBitmask();
        ArrayList<ApnSetting> dunSettings = null;
        if (8 == apnType) {
            dunSettings = sortApnListByPreferred(fetchDunApns());
        }
        log("checkForCompatibleConnectedApnContext: apnContext=" + apnContext);
        DataConnection potentialDc = null;
        ApnContext potentialApnCtx = null;
        for (ApnContext curApnCtx : this.mApnContexts.values()) {
            DataConnection curDc = curApnCtx.getDataConnection();
            if (curDc != null) {
                ApnSetting apnSetting = curApnCtx.getApnSetting();
                log("apnSetting: " + apnSetting);
                if (dunSettings != null && dunSettings.size() > 0) {
                    Iterator<ApnSetting> it = dunSettings.iterator();
                    while (it.hasNext()) {
                        if (it.next().equals(apnSetting)) {
                            int i = AnonymousClass6.$SwitchMap$com$android$internal$telephony$DctConstants$State[curApnCtx.getState().ordinal()];
                            if (i == 1) {
                                log("checkForCompatibleConnectedApnContext: found dun conn=" + curDc + " curApnCtx=" + curApnCtx);
                                return curDc;
                            } else if (i == 3) {
                                potentialApnCtx = curApnCtx;
                                potentialDc = curDc;
                            }
                        }
                    }
                    continue;
                } else if (apnSetting != null && apnSetting.canHandleType(apnType)) {
                    int i2 = AnonymousClass6.$SwitchMap$com$android$internal$telephony$DctConstants$State[curApnCtx.getState().ordinal()];
                    if (i2 == 1) {
                        log("checkForCompatibleConnectedApnContext: found canHandle conn=" + curDc + " curApnCtx=" + curApnCtx);
                        return curDc;
                    } else if (i2 == 3) {
                        potentialApnCtx = curApnCtx;
                        potentialDc = curDc;
                    }
                }
            }
        }
        if (potentialDc != null) {
            log("checkForCompatibleConnectedApnContext: found potential conn=" + potentialDc + " curApnCtx=" + potentialApnCtx);
            return potentialDc;
        }
        log("checkForCompatibleConnectedApnContext: NO conn apnContext=" + apnContext);
        return null;
    }

    /* access modifiers changed from: protected */
    public void addRequestNetworkCompleteMsg(Message onCompleteMsg, int apnType) {
        if (onCompleteMsg != null) {
            List<Message> messageList = this.mRequestNetworkCompletionMsgs.get(Integer.valueOf(apnType));
            if (messageList == null) {
                messageList = new ArrayList();
            }
            messageList.add(onCompleteMsg);
            this.mRequestNetworkCompletionMsgs.put(Integer.valueOf(apnType), messageList);
        }
    }

    /* access modifiers changed from: protected */
    public void sendRequestNetworkCompleteMsg(Message message, boolean success, int transport, int requestType, int cause) {
        if (message != null) {
            Bundle b = message.getData();
            b.putBoolean(DATA_COMPLETE_MSG_EXTRA_SUCCESS, success);
            b.putInt(DATA_COMPLETE_MSG_EXTRA_REQUEST_TYPE, requestType);
            b.putInt(DATA_COMPLETE_MSG_EXTRA_TRANSPORT_TYPE, transport);
            b.putBoolean(DATA_COMPLETE_MSG_EXTRA_HANDOVER_FAILURE_FALLBACK, requestType == 2 && cause == 2251);
            message.sendToTarget();
        }
    }

    public void enableApn(int apnType, int requestType, Message onCompleteMsg) {
        sendMessage(obtainMessage(270349, apnType, requestType, onCompleteMsg));
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private void onEnableApn(int apnType, int requestType, Message onCompleteMsg) {
        ApnContext apnContext = this.mApnContextsByType.get(apnType);
        if (apnContext == null) {
            loge("onEnableApn(" + apnType + "): NO ApnContext");
            sendRequestNetworkCompleteMsg(onCompleteMsg, false, this.mTransportType, requestType, 0);
            return;
        }
        String str = "onEnableApn: apnType=" + ApnSetting.getApnTypeString(apnType) + ", request type=" + requestTypeToString(requestType);
        log(str);
        apnContext.requestLog(str);
        if (!apnContext.isDependencyMet()) {
            apnContext.setReason(PhoneInternalInterface.REASON_DATA_DEPENDENCY_UNMET);
            apnContext.setEnabled(true);
            log("onEnableApn: dependency is not met.");
            apnContext.requestLog("onEnableApn: dependency is not met.");
            sendRequestNetworkCompleteMsg(onCompleteMsg, false, this.mTransportType, requestType, 0);
            return;
        }
        if (apnContext.isReady()) {
            switch (AnonymousClass6.$SwitchMap$com$android$internal$telephony$DctConstants$State[apnContext.getState().ordinal()]) {
                case 1:
                    log("onEnableApn: 'CONNECTED' so return");
                    apnContext.requestLog("onEnableApn state=CONNECTED, so return");
                    sendRequestNetworkCompleteMsg(onCompleteMsg, true, this.mTransportType, requestType, 0);
                    return;
                case 2:
                    log("onEnableApn: 'DISCONNECTING' so return");
                    apnContext.requestLog("onEnableApn state=DISCONNECTING, so return");
                    sendRequestNetworkCompleteMsg(onCompleteMsg, false, this.mTransportType, requestType, 0);
                    return;
                case 3:
                    log("onEnableApn: 'CONNECTING' so return");
                    apnContext.requestLog("onEnableApn state=CONNECTING, so return");
                    addRequestNetworkCompleteMsg(onCompleteMsg, apnType);
                    return;
                case 4:
                case 5:
                case 6:
                    apnContext.setReason(PhoneInternalInterface.REASON_DATA_ENABLED);
                    break;
            }
        } else {
            if (apnContext.isEnabled()) {
                apnContext.setReason(PhoneInternalInterface.REASON_DATA_DEPENDENCY_MET);
            } else {
                apnContext.setReason(PhoneInternalInterface.REASON_DATA_ENABLED);
            }
            if (apnContext.getState() == DctConstants.State.FAILED) {
                apnContext.setState(DctConstants.State.IDLE);
            }
        }
        apnContext.setEnabled(true);
        apnContext.resetErrorCodeRetries();
        if (mtkAddOrSendRequestNetworkCompleteMsg(apnType, requestType, onCompleteMsg)) {
            log("onEnableApn: not trySetupData, just add or send complete msg");
        } else if (trySetupData(apnContext, requestType)) {
            addRequestNetworkCompleteMsg(onCompleteMsg, apnType);
        } else {
            sendRequestNetworkCompleteMsg(onCompleteMsg, false, this.mTransportType, requestType, 0);
        }
    }

    public void disableApn(int apnType, int releaseType) {
        sendMessage(obtainMessage(270350, apnType, releaseType));
    }

    private void onDisableApn(int apnType, int releaseType) {
        ApnContext apnContext = this.mApnContextsByType.get(apnType);
        if (apnContext == null) {
            loge("disableApn(" + apnType + "): NO ApnContext");
            return;
        }
        boolean cleanup = false;
        String str = "onDisableApn: apnType=" + ApnSetting.getApnTypeString(apnType) + ", release type=" + releaseTypeToString(releaseType);
        log(str);
        apnContext.requestLog(str);
        mtkSyncApnContextDisableState(apnContext, releaseType);
        if (apnContext.isReady()) {
            cleanup = releaseType == 2 || releaseType == 3;
            if (apnContext.isDependencyMet()) {
                apnContext.setReason(PhoneInternalInterface.REASON_DATA_DISABLED_INTERNAL);
                if ("dun".equals(apnContext.getApnType()) || apnContext.getState() != DctConstants.State.CONNECTED) {
                    String str2 = "Clean up the connection. Apn type = " + apnContext.getApnType() + ", state = " + apnContext.getState();
                    log(str2);
                    apnContext.requestLog(str2);
                    cleanup = true;
                }
            } else {
                apnContext.setReason(PhoneInternalInterface.REASON_DATA_DEPENDENCY_UNMET);
            }
        }
        apnContext.setEnabled(false);
        if (cleanup) {
            cleanUpConnectionInternal(true, releaseType, apnContext);
        }
        if (isOnlySingleDcAllowed(getDataRat()) && !isHigherPriorityApnContextActive(apnContext)) {
            log("disableApn:isOnlySingleDcAllowed true & higher priority APN disabled");
            setupDataOnAllConnectableApns(PhoneInternalInterface.REASON_SINGLE_PDN_ARBITRATION, RetryFailures.ALWAYS);
        }
    }

    public void setDataRoamingEnabledByUser(boolean enabled) {
        this.mDataEnabledSettings.setDataRoamingEnabled(enabled);
        setDataRoamingFromUserAction(true);
        log("setDataRoamingEnabledByUser: set phoneSubId=" + this.mPhone.getSubId() + " isRoaming=" + enabled);
    }

    public boolean getDataRoamingEnabled() {
        return this.mDataEnabledSettings.getDataRoamingEnabled();
    }

    /* access modifiers changed from: protected */
    public void setDefaultDataRoamingEnabled() {
        boolean useCarrierSpecificDefault = false;
        if (this.mTelephonyManager.getSimCount() != 1) {
            try {
                Settings.Global.getInt(this.mResolver, "data_roaming" + this.mPhone.getSubId());
            } catch (Settings.SettingNotFoundException e) {
                useCarrierSpecificDefault = true;
            }
        } else if (!isDataRoamingFromUserAction() && mtkIsUseCarrierRoamingData()) {
            useCarrierSpecificDefault = true;
        }
        log("setDefaultDataRoamingEnabled: useCarrierSpecificDefault " + useCarrierSpecificDefault);
        if (useCarrierSpecificDefault) {
            this.mDataEnabledSettings.setDataRoamingEnabled(this.mDataEnabledSettings.getDefaultDataRoamingEnabled());
        }
    }

    /* access modifiers changed from: protected */
    public boolean isDataRoamingFromUserAction() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mPhone.getContext());
        if (!sp.contains(Phone.DATA_ROAMING_IS_USER_SETTING_KEY) && (Settings.Global.getInt(this.mResolver, "device_provisioned", 0) == 0 || mtkIsSetFalseForUserAction())) {
            sp.edit().putBoolean(Phone.DATA_ROAMING_IS_USER_SETTING_KEY, false).commit();
        }
        return sp.getBoolean(Phone.DATA_ROAMING_IS_USER_SETTING_KEY, true);
    }

    private void setDataRoamingFromUserAction(boolean isUserAction) {
        PreferenceManager.getDefaultSharedPreferences(this.mPhone.getContext()).edit().putBoolean(Phone.DATA_ROAMING_IS_USER_SETTING_KEY, isUserAction).commit();
    }

    /* access modifiers changed from: protected */
    public void onDataRoamingOff() {
        log("onDataRoamingOff");
        reevaluateDataConnections();
        if (!getDataRoamingEnabled()) {
            setDataProfilesAsNeeded();
            setInitialAttachApn();
            setupDataOnAllConnectableApns(PhoneInternalInterface.REASON_ROAMING_OFF, RetryFailures.ALWAYS);
            return;
        }
        this.mPhone.notifyDataConnection();
    }

    /* access modifiers changed from: protected */
    public void onDataRoamingOnOrSettingsChanged(int messageType) {
        log("onDataRoamingOnOrSettingsChanged");
        boolean settingChanged = messageType == 270384;
        if (!this.mPhone.getServiceState().getDataRoaming()) {
            log("device is not roaming. ignored the request.");
            return;
        }
        checkDataRoamingStatus(settingChanged);
        if (getDataRoamingEnabled()) {
            if (settingChanged) {
                reevaluateDataConnections();
            }
            log("onDataRoamingOnOrSettingsChanged: setup data on roaming");
            setupDataOnAllConnectableApns(PhoneInternalInterface.REASON_ROAMING_ON, RetryFailures.ALWAYS);
            this.mPhone.notifyDataConnection();
            return;
        }
        log("onDataRoamingOnOrSettingsChanged: Tear down data connection on roaming.");
        cleanUpAllConnectionsInternal(true, PhoneInternalInterface.REASON_ROAMING_ON);
    }

    /* access modifiers changed from: protected */
    public void checkDataRoamingStatus(boolean settingChanged) {
        if (!(settingChanged || getDataRoamingEnabled() || !this.mPhone.getServiceState().getDataRoaming())) {
            for (ApnContext apnContext : this.mApnContexts.values()) {
                if (apnContext.getState() == DctConstants.State.CONNECTED) {
                    LocalLog localLog = this.mDataRoamingLeakageLog;
                    StringBuilder sb = new StringBuilder();
                    sb.append("PossibleRoamingLeakage  connection params: ");
                    sb.append(apnContext.getDataConnection() != null ? apnContext.getDataConnection().getConnectionParams() : PhoneConfigurationManager.SSSS);
                    localLog.log(sb.toString());
                }
            }
        }
    }

    private void onRadioAvailable() {
        log("onRadioAvailable");
        if (this.mPhone.getSimulatedRadioControl() != null) {
            this.mPhone.notifyDataConnection();
            log("onRadioAvailable: We're on the simulator; assuming data is connected");
        }
        if (getOverallState() != DctConstants.State.IDLE) {
            cleanUpConnectionInternal(true, 2, null);
        }
    }

    private void onRadioOffOrNotAvailable() {
        this.mReregisterOnReconnectFailure = false;
        this.mAutoAttachEnabled.set(false);
        if (this.mPhone.getSimulatedRadioControl() != null) {
            log("We're on the simulator; assuming radio off is meaningless");
            return;
        }
        log("onRadioOffOrNotAvailable: is off and clean up all connections");
        cleanUpAllConnectionsInternal(false, PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
    }

    private void completeConnection(ApnContext apnContext, int type) {
        log("completeConnection: successful, notify the world apnContext=" + apnContext);
        if (this.mIsProvisioning && !TextUtils.isEmpty(this.mProvisioningUrl)) {
            log("completeConnection: MOBILE_PROVISIONING_ACTION url=" + this.mProvisioningUrl);
            Intent newIntent = Intent.makeMainSelectorActivity("android.intent.action.MAIN", "android.intent.category.APP_BROWSER");
            newIntent.setData(Uri.parse(this.mProvisioningUrl));
            newIntent.setFlags(272629760);
            try {
                this.mPhone.getContext().startActivity(newIntent);
            } catch (ActivityNotFoundException e) {
                loge("completeConnection: startActivityAsUser failed" + e);
            }
        }
        this.mIsProvisioning = false;
        this.mProvisioningUrl = null;
        ProgressDialog progressDialog = this.mProvisioningSpinner;
        if (progressDialog != null) {
            sendMessage(obtainMessage(270378, progressDialog));
        }
        if (type != 2 && mtkIsNeedNotify(apnContext)) {
            this.mPhone.notifyDataConnection(apnContext.getApnType());
        }
        startNetStatPoll();
        startDataStallAlarm(false);
    }

    private void onDataSetupComplete(ApnContext apnContext, boolean success, int cause, int requestType) {
        String str;
        if (!mtkCanHandleOnDataSetupComplete(apnContext, success, cause, requestType)) {
            List<Message> messageList = this.mRequestNetworkCompletionMsgs.get(Integer.valueOf(ApnSetting.getApnTypesBitmaskFromString(apnContext.getApnType())));
            if (messageList != null) {
                for (Message msg : messageList) {
                    sendRequestNetworkCompleteMsg(msg, success, this.mTransportType, requestType, cause);
                }
                messageList.clear();
            }
            String str2 = null;
            if (success) {
                DataConnection dataConnection = apnContext.getDataConnection();
                if (dataConnection == null) {
                    log("onDataSetupComplete: no connection to DC, handle as error");
                    onDataSetupCompleteError(apnContext, requestType);
                    return;
                }
                ApnSetting apn = apnContext.getApnSetting();
                StringBuilder sb = new StringBuilder();
                sb.append("onDataSetupComplete: success apn=");
                if (apn == null) {
                    str = "unknown";
                } else {
                    str = apn.getApnName();
                }
                sb.append(str);
                log(sb.toString());
                if (apn != null && !TextUtils.isEmpty(apn.getProxyAddressAsString())) {
                    try {
                        int port = apn.getProxyPort();
                        if (port == -1) {
                            port = 8080;
                        }
                        dataConnection.setLinkPropertiesHttpProxy(new ProxyInfo(apn.getProxyAddressAsString(), port, null));
                    } catch (NumberFormatException e) {
                        loge("onDataSetupComplete: NumberFormatException making ProxyProperties (" + apn.getProxyPort() + "): " + e);
                    } catch (Exception e2) {
                        Rlog.d(LOG_TAG, e2.toString());
                    }
                }
                if (TextUtils.equals(apnContext.getApnType(), TransportManager.IWLAN_OPERATION_MODE_DEFAULT)) {
                    try {
                        SystemProperties.set(PUPPET_MASTER_RADIO_STRESS_TEST, "true");
                    } catch (RuntimeException e3) {
                        log("Failed to set PUPPET_MASTER_RADIO_STRESS_TEST to true");
                    }
                    if (this.mCanSetPreferApn && this.mPreferredApn == null) {
                        log("onDataSetupComplete: PREFERRED APN is null");
                        this.mPreferredApn = apn;
                        ApnSetting apnSetting = this.mPreferredApn;
                        if (apnSetting != null) {
                            setPreferredApn(apnSetting.getId());
                            updateWaitingApns(this.mPreferredApn, apnContext);
                        }
                    }
                    super.setNeedDataStallFlag(apn);
                } else {
                    try {
                        SystemProperties.set(PUPPET_MASTER_RADIO_STRESS_TEST, "false");
                    } catch (RuntimeException e4) {
                        log("Failed to set PUPPET_MASTER_RADIO_STRESS_TEST to false");
                    }
                }
                this.mHasDataConnConncted = true;
                this.mSetupDataCallFailureCount = 0;
                apnContext.setState(DctConstants.State.CONNECTED);
                checkDataRoamingStatus(false);
                boolean isProvApn = apnContext.isProvisioningApn();
                ConnectivityManager cm = ConnectivityManager.from(this.mPhone.getContext());
                if (this.mProvisionBroadcastReceiver != null) {
                    this.mPhone.getContext().unregisterReceiver(this.mProvisionBroadcastReceiver);
                    this.mProvisionBroadcastReceiver = null;
                }
                if (!isProvApn || this.mIsProvisioning) {
                    cm.setProvisioningNotificationVisible(false, 0, this.mProvisionActionName);
                    completeConnection(apnContext, requestType);
                } else {
                    log("onDataSetupComplete: successful, BUT send connected to prov apn as mIsProvisioning:" + this.mIsProvisioning + " == false && (isProvisioningApn:" + isProvApn + " == true");
                    this.mProvisionBroadcastReceiver = new ProvisionNotificationBroadcastReceiver(cm.getMobileProvisioningUrl(), this.mTelephonyManager.getNetworkOperatorName());
                    this.mPhone.getContext().registerReceiver(this.mProvisionBroadcastReceiver, new IntentFilter(this.mProvisionActionName));
                    cm.setProvisioningNotificationVisible(true, 0, this.mProvisionActionName);
                    setRadio(false);
                }
                log("onDataSetupComplete: SETUP complete type=" + apnContext.getApnType());
                if (Build.IS_DEBUGGABLE) {
                    int pcoVal = SystemProperties.getInt("persist.radio.test.pco", -1);
                    if (pcoVal != -1) {
                        log("PCO testing: read pco value from persist.radio.test.pco " + pcoVal);
                        Intent intent = new Intent("com.android.internal.telephony.CARRIER_SIGNAL_PCO_VALUE");
                        intent.putExtra("apnType", TransportManager.IWLAN_OPERATION_MODE_DEFAULT);
                        intent.putExtra("apnProto", "IPV4V6");
                        intent.putExtra("pcoId", 65280);
                        intent.putExtra("pcoValue", new byte[]{(byte) pcoVal});
                        this.mPhone.getCarrierSignalAgent().notifyCarrierSignalReceivers(intent);
                        return;
                    }
                    return;
                }
                return;
            }
            ApnSetting apn2 = apnContext.getApnSetting();
            log(("onDataSetupComplete: error apn=" + apn2) != null ? apn2.getApnName() : ((Object) null) + ", cause=" + cause + ", requestType=" + requestTypeToString(requestType));
            if (DataFailCause.isEventLoggable(cause)) {
                EventLog.writeEvent((int) EventLogTags.PDP_SETUP_FAIL, Integer.valueOf(cause), Integer.valueOf(getCellLocationId()), Integer.valueOf(this.mTelephonyManager.getNetworkType()));
            }
            ApnSetting apn3 = apnContext.getApnSetting();
            Phone phone = this.mPhone;
            String apnType = apnContext.getApnType();
            if (apn3 != null) {
                str2 = apn3.getApnName();
            }
            phone.notifyPreciseDataConnectionFailed(apnType, str2, cause);
            Intent intent2 = new Intent("com.android.internal.telephony.CARRIER_SIGNAL_REQUEST_NETWORK_FAILED");
            intent2.putExtra("errorCode", cause);
            intent2.putExtra("apnType", apnContext.getApnType());
            this.mPhone.getCarrierSignalAgent().notifyCarrierSignalReceivers(intent2);
            if (DataFailCause.isRadioRestartFailure(this.mPhone.getContext(), cause, this.mPhone.getSubId()) || apnContext.restartOnError(cause)) {
                log("Modem restarted.");
                sendRestartRadio();
            }
            if (isPermanentFailure(cause) || mtkIsPermanentFailure(cause)) {
                log("cause = " + cause + ", mark apn as permanent failed. apn = " + apn3);
                apnContext.markApnPermanentFailed(apn3);
            }
            onDataSetupCompleteError(apnContext, requestType);
        }
    }

    private void onDataSetupCompleteError(ApnContext apnContext, int requestType) {
        if (this.mPhone.getServiceState().getDataRegState() == 0) {
            int i = this.mSetupDataCallFailureCount + 1;
            this.mSetupDataCallFailureCount = i;
            if (i == 10 && this.mHasDataConnConncted) {
                this.mHasDataConnConncted = false;
                ApnSetting apn = apnContext.getApnSetting();
                getCellLocationId();
                int nwType = TelephonyManager.getDefault().getNetworkType();
                StringBuilder sb = new StringBuilder();
                sb.append(getCellLocation());
                sb.append(", onDataSetupComplete: error apn=");
                sb.append(apn == null ? "unknown" : apn.getApnName());
                sb.append(",nwType=");
                sb.append(nwType);
                String error_info = sb.toString();
                int log_type = -1;
                String log_desc = PhoneConfigurationManager.SSSS;
                try {
                    String[] log_array = OemTelephonyUtils.getOemRes(this.mPhone.getContext(), "zz_oppo_critical_log_110", PhoneConfigurationManager.SSSS).split(",");
                    log_type = Integer.valueOf(log_array[0]).intValue();
                    log_desc = log_array[1];
                } catch (Exception e) {
                }
                OppoManager.writeLogToPartition(log_type, error_info, "NETWORK", "data_setup_data_error", log_desc);
                HashMap<String, String> mDataSetupCompleteErrorMap = new HashMap<>();
                mDataSetupCompleteErrorMap.put(String.valueOf("data_setup_data_error"), "onDataSetupComplete error");
                OppoManager.onStamp(DATA_EVENT_ID, mDataSetupCompleteErrorMap);
            }
        }
        long delay = apnContext.getDelayForNextApn(this.mFailFast);
        if (delay >= 0) {
            log("onDataSetupCompleteError: Try next APN. delay = " + delay);
            apnContext.setState(DctConstants.State.RETRYING);
            startAlarmForReconnect(delay, apnContext);
            return;
        }
        apnContext.setState(DctConstants.State.FAILED);
        this.mPhone.notifyDataConnection(apnContext.getApnType());
        apnContext.setDataConnection(null);
        log("onDataSetupCompleteError: Stop retrying APNs. delay=" + delay + ", requestType=" + requestTypeToString(requestType));
        mtkFakeDataConnection(apnContext);
    }

    private void onNetworkStatusChanged(int status, String redirectUrl) {
        if (!TextUtils.isEmpty(redirectUrl)) {
            Intent intent = new Intent("com.android.internal.telephony.CARRIER_SIGNAL_REDIRECTED");
            intent.putExtra("redirectionUrl", redirectUrl);
            this.mPhone.getCarrierSignalAgent().notifyCarrierSignalReceivers(intent);
            log("Notify carrier signal receivers with redirectUrl: " + redirectUrl);
            return;
        }
        boolean isValid = status == 1;
        if (!this.mDsRecoveryHandler.isRecoveryOnBadNetworkEnabled()) {
            log("Skip data stall recovery on network status change with in threshold");
        } else if (this.mTransportType != 1) {
            log("Skip data stall recovery on non WWAN");
        } else {
            this.mDsRecoveryHandler.processNetworkStatusChanged(isValid);
        }
    }

    /* access modifiers changed from: protected */
    public void onDisconnectDone(ApnContext apnContext) {
        log("onDisconnectDone: EVENT_DISCONNECT_DONE apnContext=" + apnContext);
        apnContext.setState(DctConstants.State.IDLE);
        DataConnection dc = apnContext.getDataConnection();
        if (dc != null && dc.isInactive() && !dc.hasBeenTransferred()) {
            for (String type : ApnSetting.getApnTypesStringFromBitmask(apnContext.getApnSetting().getApnTypeBitmask()).split(",")) {
                this.mPhone.notifyDataConnection(type);
            }
        }
        if (!isDisconnected() || !this.mPhone.getServiceStateTracker().processPendingRadioPowerOffAfterDataOff()) {
            if (!this.mAttached.get() || !apnContext.isReady() || !retryAfterDisconnected(apnContext)) {
                boolean restartRadioAfterProvisioning = this.mPhone.getContext().getResources().getBoolean(17891501);
                if (apnContext.isProvisioningApn() && restartRadioAfterProvisioning) {
                    log("onDisconnectDone: restartRadio after provisioning");
                    restartRadio();
                }
                apnContext.setApnSetting(null);
                apnContext.setDataConnection(null);
                if (isOnlySingleDcAllowed(getDataRat())) {
                    log("onDisconnectDone: isOnlySigneDcAllowed true so setup single apn");
                    if (!TransportManager.IWLAN_OPERATION_MODE_DEFAULT.equals(apnContext.getApnType()) || !this.mIsWifiConnected) {
                        setupDataOnAllConnectableApns(PhoneInternalInterface.REASON_SINGLE_PDN_ARBITRATION, RetryFailures.ALWAYS);
                    } else {
                        log("wifi have been conneted, set default apn type retry false--do nothing!");
                    }
                } else {
                    log("onDisconnectDone: not retrying");
                }
            } else {
                try {
                    SystemProperties.set(PUPPET_MASTER_RADIO_STRESS_TEST, "false");
                } catch (RuntimeException e) {
                    log("Failed to set PUPPET_MASTER_RADIO_STRESS_TEST to false");
                }
                log("onDisconnectDone: attached, ready and retry after disconnect");
                long delay = mtkModifyInterApnDelay(apnContext.getRetryAfterDisconnectDelay(), apnContext);
                if (delay > 0) {
                    startAlarmForReconnect(delay, apnContext);
                }
            }
            int i = this.mDisconnectPendingCount;
            if (i > 0) {
                this.mDisconnectPendingCount = i - 1;
            }
            if (this.mDisconnectPendingCount == 0) {
                apnContext.setConcurrentVoiceAndDataAllowed(this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed());
                notifyAllDataDisconnected();
                return;
            }
            return;
        }
        log("onDisconnectDone: radio will be turned off, no retries");
        apnContext.setApnSetting(null);
        apnContext.setDataConnection(null);
        int i2 = this.mDisconnectPendingCount;
        if (i2 > 0) {
            this.mDisconnectPendingCount = i2 - 1;
        }
        if (this.mDisconnectPendingCount == 0) {
            notifyAllDataDisconnected();
        }
    }

    private void onVoiceCallStarted() {
        log("onVoiceCallStarted");
        this.mInVoiceCall = true;
        if (isConnected() && !this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed()) {
            log("onVoiceCallStarted stop polling");
            stopNetStatPoll();
            stopDataStallAlarm();
            this.mPhone.notifyDataConnection();
        }
    }

    private void onVoiceCallEnded() {
        log("onVoiceCallEnded");
        this.mInVoiceCall = false;
        if (!isConnected()) {
            return;
        }
        if (!this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed()) {
            startNetStatPoll();
            startDataStallAlarm(false);
            this.mPhone.notifyDataConnection();
            return;
        }
        resetPollStats();
    }

    /* access modifiers changed from: protected */
    public boolean isConnected() {
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (apnContext.getState() == DctConstants.State.CONNECTED) {
                return true;
            }
        }
        return false;
    }

    public boolean isDisconnected() {
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (!apnContext.isDisconnected()) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void setDataProfilesAsNeeded() {
        log("setDataProfilesAsNeeded");
        ArrayList<DataProfile> dataProfileList = new ArrayList<>();
        Iterator<ApnSetting> it = this.mAllApnSettings.iterator();
        while (it.hasNext()) {
            ApnSetting apn = it.next();
            DataProfile dp = createDataProfile(apn, apn.equals(getPreferredApn()));
            if (!dataProfileList.contains(dp)) {
                dataProfileList.add(dp);
            }
        }
        if (dataProfileList.isEmpty()) {
            return;
        }
        if (dataProfileList.size() != this.mLastDataProfileList.size() || !this.mLastDataProfileList.containsAll(dataProfileList)) {
            this.mDataServiceManager.setDataProfile(dataProfileList, this.mPhone.getServiceState().getDataRoamingFromRegistration(), null);
        }
    }

    /* access modifiers changed from: protected */
    public void createAllApnList() {
        this.mAllApnSettings.clear();
        IccRecords r = this.mIccRecords.get();
        String operator = r != null ? r.getOperatorNumeric() : PhoneConfigurationManager.SSSS;
        ContentResolver contentResolver = this.mPhone.getContext().getContentResolver();
        Uri uri = Telephony.Carriers.SIM_APN_URI;
        Cursor cursor = contentResolver.query(Uri.withAppendedPath(uri, "filtered/subId/" + this.mPhone.getSubId()), null, null, null, HbpcdLookup.ID);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                ApnSetting apn = ApnSetting.makeApnSetting(cursor);
                if (apn != null) {
                    this.mAllApnSettings.add(apn);
                }
            }
            cursor.close();
        } else {
            log("createAllApnList: cursor is null");
            LocalLog localLog = this.mApnSettingsInitializationLog;
            localLog.log("cursor is null for carrier, operator: " + operator);
            recordNoOperatorError();
        }
        addEmergencyApnSetting();
        dedupeApnSettings();
        if (this.mAllApnSettings.isEmpty()) {
            log("createAllApnList: No APN found for carrier, operator: " + operator);
            LocalLog localLog2 = this.mApnSettingsInitializationLog;
            localLog2.log("no APN found for carrier, operator: " + operator);
            this.mPreferredApn = null;
            recordNoApnAvailable();
        } else {
            this.mPreferredApn = getPreferredApn();
            ApnSetting apnSetting = this.mPreferredApn;
            if (apnSetting != null && !apnSetting.getOperatorNumeric().equals(operator)) {
                this.mPreferredApn = null;
                setPreferredApn(-1);
            }
            log("createAllApnList: mPreferredApn=" + this.mPreferredApn);
        }
        log("createAllApnList: X mAllApnSettings=" + this.mAllApnSettings);
    }

    /* access modifiers changed from: protected */
    public void dedupeApnSettings() {
        new ArrayList();
        synchronized (this.mRefCountLock) {
            for (int i = 0; i < this.mAllApnSettings.size() - 1; i++) {
                ApnSetting first = this.mAllApnSettings.get(i);
                int j = i + 1;
                while (j < this.mAllApnSettings.size()) {
                    ApnSetting second = this.mAllApnSettings.get(j);
                    if (first.similar(second)) {
                        ApnSetting newApn = mergeApns(first, second);
                        this.mAllApnSettings.set(i, newApn);
                        first = newApn;
                        this.mAllApnSettings.remove(j);
                    } else {
                        j++;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public ApnSetting mergeApns(ApnSetting dest, ApnSetting src) {
        int protocol;
        int id = dest.getId();
        if ((src.getApnTypeBitmask() & 17) == 17) {
            id = src.getId();
        }
        int resultApnType = src.getApnTypeBitmask() | dest.getApnTypeBitmask();
        Uri mmsc = dest.getMmsc() == null ? src.getMmsc() : dest.getMmsc();
        String mmsProxy = TextUtils.isEmpty(dest.getMmsProxyAddressAsString()) ? src.getMmsProxyAddressAsString() : dest.getMmsProxyAddressAsString();
        int mmsPort = dest.getMmsProxyPort() == -1 ? src.getMmsProxyPort() : dest.getMmsProxyPort();
        String proxy = TextUtils.isEmpty(dest.getProxyAddressAsString()) ? src.getProxyAddressAsString() : dest.getProxyAddressAsString();
        int port = dest.getProxyPort() == -1 ? src.getProxyPort() : dest.getProxyPort();
        if (src.getProtocol() == 2) {
            protocol = src.getProtocol();
        } else {
            protocol = dest.getProtocol();
        }
        return ApnSetting.makeApnSetting(id, dest.getOperatorNumeric(), dest.getEntryName(), dest.getApnName(), proxy, port, mmsc, mmsProxy, mmsPort, dest.getUser(), dest.getPassword(), dest.getAuthType(), resultApnType, protocol, src.getRoamingProtocol() == 2 ? src.getRoamingProtocol() : dest.getRoamingProtocol(), dest.isEnabled(), (dest.getNetworkTypeBitmask() == 0 || src.getNetworkTypeBitmask() == 0) ? 0 : dest.getNetworkTypeBitmask() | src.getNetworkTypeBitmask(), dest.getProfileId(), dest.isPersistent() || src.isPersistent(), dest.getMaxConns(), dest.getWaitTime(), dest.getMaxConnsTime(), dest.getMtu(), dest.getMvnoType(), dest.getMvnoMatchData(), dest.getApnSetId(), dest.getCarrierId(), dest.getSkip464Xlat());
    }

    private DataConnection createDataConnection() {
        log("createDataConnection E");
        int id = this.mUniqueIdGenerator.getAndIncrement();
        DataConnection dataConnection = DataConnection.makeDataConnection(this.mPhone, id, this, this.mDataServiceManager, this.mDcTesterFailBringUpAll, this.mDcc);
        this.mDataConnections.put(Integer.valueOf(id), dataConnection);
        log("createDataConnection() X id=" + id + " dc=" + dataConnection);
        return dataConnection;
    }

    private void destroyDataConnections() {
        if (this.mDataConnections != null) {
            log("destroyDataConnections: clear mDataConnectionList");
            this.mDataConnections.clear();
            return;
        }
        log("destroyDataConnections: mDataConnecitonList is empty, ignore");
    }

    /* access modifiers changed from: protected */
    public ArrayList<ApnSetting> buildWaitingApns(String requestedApnType, int radioTech) {
        boolean usePreferred;
        ApnSetting apnSetting;
        log("buildWaitingApns: E requestedApnType=" + requestedApnType);
        ArrayList<ApnSetting> apnList = new ArrayList<>();
        int requestedApnTypeBitmask = ApnSetting.getApnTypesBitmaskFromString(requestedApnType);
        if (requestedApnTypeBitmask == 8) {
            ArrayList<ApnSetting> dunApns = fetchDunApns();
            if (dunApns.size() > 0) {
                Iterator<ApnSetting> it = dunApns.iterator();
                while (it.hasNext()) {
                    apnList.add(it.next());
                    log("buildWaitingApns: X added APN_TYPE_DUN apnList=" + apnList);
                }
                return sortApnListByPreferred(apnList);
            }
        }
        IccRecords r = this.mIccRecords.get();
        String operator = mtkGetOperatorNumeric(r);
        try {
            usePreferred = !this.mPhone.getContext().getResources().getBoolean(17891415);
        } catch (Resources.NotFoundException e) {
            log("buildWaitingApns: usePreferred NotFoundException set to true");
            usePreferred = true;
        }
        if (usePreferred) {
            this.mPreferredApn = getPreferredApn();
        }
        log("buildWaitingApns: usePreferred=" + usePreferred + " canSetPreferApn=" + this.mCanSetPreferApn + " mPreferredApn=" + this.mPreferredApn + " operator=" + operator + " radioTech=" + radioTech + " IccRecords r=" + r);
        if (usePreferred && this.mCanSetPreferApn && (apnSetting = this.mPreferredApn) != null && apnSetting.canHandleType(requestedApnTypeBitmask)) {
            log("buildWaitingApns: Preferred APN:" + operator + ":" + this.mPreferredApn.getOperatorNumeric() + ":" + this.mPreferredApn);
            if (!this.mPreferredApn.getOperatorNumeric().equals(operator)) {
                log("buildWaitingApns: no preferred APN");
                setPreferredApn(-1);
                this.mPreferredApn = null;
            } else if (this.mPreferredApn.canSupportNetworkType(ServiceState.rilRadioTechnologyToNetworkType(radioTech))) {
                apnList.add(this.mPreferredApn);
                ArrayList<ApnSetting> apnList2 = sortApnListByPreferred(apnList);
                log("buildWaitingApns: X added preferred apnList=" + apnList2);
                return apnList2;
            } else {
                log("buildWaitingApns: no preferred APN");
                setPreferredApn(-1);
                this.mPreferredApn = null;
            }
        }
        log("buildWaitingApns: mAllApnSettings=" + this.mAllApnSettings);
        Iterator<ApnSetting> it2 = this.mAllApnSettings.iterator();
        while (it2.hasNext()) {
            ApnSetting apn = it2.next();
            if (apn.canHandleType(requestedApnTypeBitmask)) {
                if (!apn.canSupportNetworkType(ServiceState.rilRadioTechnologyToNetworkType(radioTech)) || !mtkIsApnCanSupportNetworkType(apn, radioTech)) {
                    log("buildWaitingApns: networkTypeBitmask:" + apn.getNetworkTypeBitmask() + " does not include radioTech:" + ServiceState.rilRadioTechnologyToString(radioTech));
                } else if (!requestedApnType.equals("mms") || !isSamePaidApn(this.mPreferredApn, apn)) {
                    apnList.add(apn);
                } else {
                    apnList.add(0, apn);
                }
            }
        }
        if (super.isTargetVersion()) {
            log("buildWaitingApns isTargetVersion");
            apnList = super.sortApnList(apnList);
        }
        ArrayList<ApnSetting> apnList3 = sortApnListByPreferred(apnList);
        log("buildWaitingApns: " + apnList3.size() + " APNs in the list: " + apnList3);
        return apnList3;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:3:0x000a, code lost:
        r0 = getPreferredApnSetId();
     */
    @VisibleForTesting
    public ArrayList<ApnSetting> sortApnListByPreferred(ArrayList<ApnSetting> list) {
        final int preferredApnSetId;
        if (!(list == null || list.size() <= 1 || preferredApnSetId == 0)) {
            list.sort(new Comparator<ApnSetting>() {
                /* class com.android.internal.telephony.dataconnection.DcTracker.AnonymousClass4 */

                public int compare(ApnSetting apn1, ApnSetting apn2) {
                    if (apn1.getApnSetId() == preferredApnSetId) {
                        return -1;
                    }
                    if (apn2.getApnSetId() == preferredApnSetId) {
                        return 1;
                    }
                    return 0;
                }
            });
        }
        return list;
    }

    /* access modifiers changed from: protected */
    public String apnListToString(ArrayList<ApnSetting> apns) {
        StringBuilder result = new StringBuilder();
        int size = apns.size();
        for (int i = 0; i < size; i++) {
            result.append('[');
            result.append(apns.get(i).toString());
            result.append(']');
        }
        return result.toString();
    }

    /* access modifiers changed from: protected */
    public void setPreferredApn(int pos) {
        if (!this.mCanSetPreferApn) {
            log("setPreferredApn: X !canSEtPreferApn");
            return;
        }
        Uri uri = Uri.withAppendedPath(PREFERAPN_NO_UPDATE_URI_USING_SUBID, Long.toString((long) this.mPhone.getSubId()));
        log("setPreferredApn: delete");
        ContentResolver resolver = this.mPhone.getContext().getContentResolver();
        resolver.delete(uri, null, null);
        if (pos >= 0) {
            log("setPreferredApn: insert");
            ContentValues values = new ContentValues();
            values.put(APN_ID, Integer.valueOf(pos));
            resolver.insert(uri, values);
        }
    }

    public ApnSetting getPreferredApn() {
        ArrayList<ApnSetting> arrayList = this.mAllApnSettings;
        if (arrayList == null || arrayList.isEmpty()) {
            log("getPreferredApn: mAllApnSettings is empty");
            return null;
        }
        Cursor cursor = this.mPhone.getContext().getContentResolver().query(Uri.withAppendedPath(PREFERAPN_NO_UPDATE_URI_USING_SUBID, Long.toString((long) this.mPhone.getSubId())), new String[]{HbpcdLookup.ID, "name", "apn"}, null, null, "name ASC");
        if (cursor != null) {
            this.mCanSetPreferApn = true;
        } else {
            this.mCanSetPreferApn = false;
        }
        if (this.mCanSetPreferApn && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int pos = cursor.getInt(cursor.getColumnIndexOrThrow(HbpcdLookup.ID));
            synchronized (this.mRefCountLock) {
                Iterator<ApnSetting> it = this.mAllApnSettings.iterator();
                while (it.hasNext()) {
                    ApnSetting p = it.next();
                    if (p != null && p.getId() == pos && p.canHandleType(this.mRequestedApnType)) {
                        log("getPreferredApn: For APN type " + ApnSetting.getApnTypeString(this.mRequestedApnType) + " found apnSetting " + p);
                        cursor.close();
                        return p;
                    }
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        log("getPreferredApn: X not found");
        return null;
    }

    public void handleMessage(Message msg) {
        boolean isProvApn;
        int i = 0;
        boolean z = true;
        switch (msg.what) {
            case 270336:
                AsyncResult ar = (AsyncResult) msg.obj;
                Pair<ApnContext, Integer> pair = (Pair) ar.userObj;
                ApnContext apnContext = (ApnContext) pair.first;
                int generation = ((Integer) pair.second).intValue();
                int requestType = msg.arg2;
                if (apnContext.getConnectionGeneration() == generation) {
                    boolean success = true;
                    int cause = InboundSmsTracker.DEST_PORT_FLAG_NO_PORT;
                    if (ar.exception != null) {
                        success = false;
                        cause = ((Integer) ar.result).intValue();
                    }
                    onDataSetupComplete(apnContext, success, cause, requestType);
                    return;
                }
                loge("EVENT_DATA_SETUP_COMPLETE: Dropped the event because generation did not match.");
                return;
            case 270337:
                onRadioAvailable();
                return;
            case 270338:
                int subId = this.mPhone.getSubId();
                if (SubscriptionManager.isValidSubscriptionId(subId)) {
                    onRecordsLoadedOrSubIdChanged();
                    return;
                }
                log("Ignoring EVENT_RECORDS_LOADED as subId is not valid: " + subId);
                return;
            case 270339:
                trySetupData((ApnContext) msg.obj, 1);
                return;
            case 270340:
            case 270341:
            case 270346:
            case 270356:
            case 270357:
            case 270361:
            case 270363:
            case 270364:
            case 270366:
            case 270367:
            case 270368:
            case 270370:
            case 270379:
            default:
                Rlog.e("DcTracker", "Unhandled event=" + msg);
                return;
            case 270342:
                onRadioOffOrNotAvailable();
                return;
            case 270343:
                onVoiceCallStarted();
                return;
            case 270344:
                onVoiceCallEnded();
                return;
            case 270345:
                onDataConnectionDetached();
                return;
            case 270347:
            case 270384:
                onDataRoamingOnOrSettingsChanged(msg.what);
                return;
            case 270348:
                onDataRoamingOff();
                return;
            case 270349:
                onEnableApn(msg.arg1, msg.arg2, (Message) msg.obj);
                return;
            case 270350:
                onDisableApn(msg.arg1, msg.arg2);
                return;
            case 270351:
                log("EVENT_DISCONNECT_DONE msg=" + msg);
                Pair<ApnContext, Integer> pair2 = (Pair) ((AsyncResult) msg.obj).userObj;
                ApnContext apnContext2 = (ApnContext) pair2.first;
                if (apnContext2.getConnectionGeneration() == ((Integer) pair2.second).intValue()) {
                    onDisconnectDone(apnContext2);
                    return;
                } else {
                    loge("EVENT_DISCONNECT_DONE: Dropped the event because generation did not match.");
                    return;
                }
            case 270352:
                onDataConnectionAttached();
                return;
            case 270353:
                onDataStallAlarm(msg.arg1);
                return;
            case 270354:
                this.mDsRecoveryHandler.doRecovery();
                return;
            case 270355:
                onApnChanged();
                return;
            case 270358:
                log("EVENT_PS_RESTRICT_ENABLED " + this.mIsPsRestricted);
                stopNetStatPoll();
                stopDataStallAlarm();
                this.mIsPsRestricted = true;
                return;
            case 270359:
                log("EVENT_PS_RESTRICT_DISABLED " + this.mIsPsRestricted);
                this.mIsPsRestricted = false;
                if (isConnected()) {
                    startNetStatPoll();
                    startDataStallAlarm(false);
                    return;
                }
                if (this.mState == DctConstants.State.FAILED) {
                    cleanUpAllConnectionsInternal(false, PhoneInternalInterface.REASON_PS_RESTRICT_ENABLED);
                    this.mReregisterOnReconnectFailure = false;
                }
                ApnContext apnContext3 = this.mApnContextsByType.get(17);
                if (apnContext3 != null) {
                    apnContext3.setReason(PhoneInternalInterface.REASON_PS_RESTRICT_ENABLED);
                    trySetupData(apnContext3, 1);
                    return;
                }
                loge("**** Default ApnContext not found ****");
                loge("Default ApnContext not found");
                return;
            case 270360:
                log("EVENT_CLEAN_UP_CONNECTION");
                cleanUpConnectionInternal(true, 2, (ApnContext) msg.obj);
                return;
            case 270362:
                restartRadio();
                return;
            case 270365:
                if (msg.obj != null && !(msg.obj instanceof String)) {
                    msg.obj = null;
                }
                cleanUpAllConnectionsInternal(true, (String) msg.obj);
                return;
            case 270369:
                onUpdateIcc();
                return;
            case 270371:
                Pair<ApnContext, Integer> pair3 = (Pair) ((AsyncResult) msg.obj).userObj;
                ApnContext apnContext4 = (ApnContext) pair3.first;
                int generation2 = ((Integer) pair3.second).intValue();
                int requestType2 = msg.arg2;
                if (apnContext4.getConnectionGeneration() == generation2) {
                    onDataSetupCompleteError(apnContext4, requestType2);
                    return;
                } else {
                    loge("EVENT_DATA_SETUP_COMPLETE_ERROR: Dropped the event because generation did not match.");
                    return;
                }
            case 270372:
                sEnableFailFastRefCounter += msg.arg1 == 1 ? 1 : -1;
                log("CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA:  sEnableFailFastRefCounter=" + sEnableFailFastRefCounter);
                if (sEnableFailFastRefCounter < 0) {
                    loge("CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA: sEnableFailFastRefCounter:" + sEnableFailFastRefCounter + " < 0");
                    sEnableFailFastRefCounter = 0;
                }
                boolean enabled = sEnableFailFastRefCounter > 0;
                log("CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA: enabled=" + enabled + " sEnableFailFastRefCounter=" + sEnableFailFastRefCounter);
                if (this.mFailFast != enabled) {
                    this.mFailFast = enabled;
                    if (enabled) {
                        z = false;
                    }
                    this.mDataStallNoRxEnabled = z;
                    if (!this.mDsRecoveryHandler.isNoRxDataStallDetectionEnabled() || getOverallState() != DctConstants.State.CONNECTED || (this.mInVoiceCall && !this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed())) {
                        log("CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA: stop data stall");
                        stopDataStallAlarm();
                        return;
                    }
                    log("CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA: start data stall");
                    stopDataStallAlarm();
                    startDataStallAlarm(false);
                    return;
                }
                return;
            case 270373:
                Bundle bundle = msg.getData();
                if (bundle != null) {
                    try {
                        this.mProvisioningUrl = (String) bundle.get("provisioningUrl");
                    } catch (ClassCastException e) {
                        loge("CMD_ENABLE_MOBILE_PROVISIONING: provisioning url not a string" + e);
                        this.mProvisioningUrl = null;
                    }
                }
                if (TextUtils.isEmpty(this.mProvisioningUrl)) {
                    loge("CMD_ENABLE_MOBILE_PROVISIONING: provisioning url is empty, ignoring");
                    this.mIsProvisioning = false;
                    this.mProvisioningUrl = null;
                    return;
                }
                loge("CMD_ENABLE_MOBILE_PROVISIONING: provisioningUrl=" + this.mProvisioningUrl);
                this.mIsProvisioning = true;
                startProvisioningApnAlarm();
                return;
            case 270374:
                log("CMD_IS_PROVISIONING_APN");
                String apnType = null;
                try {
                    Bundle bundle2 = msg.getData();
                    if (bundle2 != null) {
                        apnType = (String) bundle2.get("apnType");
                    }
                    if (TextUtils.isEmpty(apnType)) {
                        loge("CMD_IS_PROVISIONING_APN: apnType is empty");
                        isProvApn = false;
                    } else {
                        isProvApn = isProvisioningApn(apnType);
                    }
                } catch (ClassCastException e2) {
                    loge("CMD_IS_PROVISIONING_APN: NO provisioning url ignoring");
                    isProvApn = false;
                }
                log("CMD_IS_PROVISIONING_APN: ret=" + isProvApn);
                AsyncChannel asyncChannel = this.mReplyAc;
                if (isProvApn) {
                    i = 1;
                }
                asyncChannel.replyToMessage(msg, 270374, i);
                return;
            case 270375:
                log("EVENT_PROVISIONING_APN_ALARM");
                ApnContext apnCtx = this.mApnContextsByType.get(17);
                if (!apnCtx.isProvisioningApn() || !apnCtx.isConnectedOrConnecting()) {
                    log("EVENT_PROVISIONING_APN_ALARM: Not connected ignore");
                    return;
                } else if (this.mProvisioningApnAlarmTag == msg.arg1) {
                    log("EVENT_PROVISIONING_APN_ALARM: Disconnecting");
                    this.mIsProvisioning = false;
                    this.mProvisioningUrl = null;
                    stopProvisioningApnAlarm();
                    cleanUpConnectionInternal(true, 2, apnCtx);
                    return;
                } else {
                    log("EVENT_PROVISIONING_APN_ALARM: ignore stale tag, mProvisioningApnAlarmTag:" + this.mProvisioningApnAlarmTag + " != arg1:" + msg.arg1);
                    return;
                }
            case 270376:
                if (msg.arg1 == 1) {
                    handleStartNetStatPoll((DctConstants.Activity) msg.obj);
                    return;
                } else if (msg.arg1 == 0) {
                    handleStopNetStatPoll((DctConstants.Activity) msg.obj);
                    return;
                } else {
                    return;
                }
            case 270377:
                if (getDataRat() != 0 && !super.isTelstraSimAndNetworkClassNotChange()) {
                    cleanUpConnectionsOnUpdatedApns(false, PhoneInternalInterface.REASON_NW_TYPE_CHANGED);
                    setupDataOnAllConnectableApns(PhoneInternalInterface.REASON_NW_TYPE_CHANGED, RetryFailures.ONLY_ON_CHANGE);
                    super.oppoWlanAssistantMeasureForRatChange();
                    return;
                }
                return;
            case 270378:
                if (this.mProvisioningSpinner == msg.obj) {
                    this.mProvisioningSpinner.dismiss();
                    this.mProvisioningSpinner = null;
                    return;
                }
                return;
            case 270380:
                onNetworkStatusChanged(msg.arg1, (String) msg.obj);
                return;
            case 270381:
                handlePcoData((AsyncResult) msg.obj);
                return;
            case 270382:
                AsyncResult ar2 = (AsyncResult) msg.obj;
                if (ar2.result instanceof Pair) {
                    Pair<Boolean, Integer> p = (Pair) ar2.result;
                    onDataEnabledChanged(((Boolean) p.first).booleanValue(), ((Integer) p.second).intValue());
                    return;
                }
                return;
            case 270383:
                onDataReconnect(msg.getData());
                return;
            case 270385:
                onDataServiceBindingChanged(((Boolean) ((AsyncResult) msg.obj).result).booleanValue());
                return;
            case 270386:
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mPhone.getContext());
                if (!sp.contains(Phone.DATA_ROAMING_IS_USER_SETTING_KEY)) {
                    sp.edit().putBoolean(Phone.DATA_ROAMING_IS_USER_SETTING_KEY, false).commit();
                    return;
                }
                return;
            case 270387:
                onDataEnabledOverrideRulesChanged();
                return;
            case 270388:
                AsyncResult arscreen = (AsyncResult) msg.obj;
                if (arscreen != null) {
                    this.mIsScreenOn = ((Boolean) arscreen.result).booleanValue();
                    post(new Runnable() {
                        /* class com.android.internal.telephony.dataconnection.DcTracker.AnonymousClass5 */

                        public void run() {
                            DcTracker.this.stopNetStatPoll();
                            DcTracker.this.startNetStatPoll();
                            DcTracker.this.restartDataStallAlarm();
                        }
                    });
                    return;
                }
                log("leon EVENT_OEM_SCREEN_CHANGED error");
                return;
        }
    }

    /* access modifiers changed from: protected */
    public int getApnProfileID(String apnType) {
        if (TextUtils.equals(apnType, "ims")) {
            return 2;
        }
        if (TextUtils.equals(apnType, "fota")) {
            return 3;
        }
        if (TextUtils.equals(apnType, "cbs")) {
            return 4;
        }
        if (!TextUtils.equals(apnType, "ia") && TextUtils.equals(apnType, "dun")) {
            return 1;
        }
        return 0;
    }

    private int getCellLocationId() {
        CellLocation loc = this.mPhone.getCellLocation();
        if (loc == null) {
            return -1;
        }
        if (loc instanceof GsmCellLocation) {
            return ((GsmCellLocation) loc).getCid();
        }
        if (loc instanceof CdmaCellLocation) {
            return ((CdmaCellLocation) loc).getBaseStationId();
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    public IccRecords getUiccRecords(int appFamily) {
        return this.mUiccController.getIccRecords(this.mPhone.getPhoneId(), appFamily);
    }

    /* access modifiers changed from: protected */
    public void onUpdateIcc() {
        IccRecords newIccRecords;
        IccRecords r;
        if (this.mUiccController != null && (r = this.mIccRecords.get()) != (newIccRecords = getUiccRecords(1))) {
            if (r != null) {
                log("Removing stale icc objects.");
                r.unregisterForRecordsLoaded(this);
                this.mIccRecords.set(null);
            }
            if (newIccRecords == null) {
                onSimNotReady();
            } else if (SubscriptionManager.isValidSubscriptionId(this.mPhone.getSubId())) {
                log("New records found.");
                this.mIccRecords.set(newIccRecords);
                newIccRecords.registerForRecordsLoaded(this, 270338, null);
            }
        }
    }

    public void update() {
        log("update sub = " + this.mPhone.getSubId());
        log("update(): Active DDS, register for all events now!");
        onUpdateIcc();
        this.mAutoAttachEnabled.set(false);
        this.mPhone.updateCurrentCarrierInProvider();
    }

    @VisibleForTesting
    public boolean shouldAutoAttach() {
        if (this.mAutoAttachEnabled.get()) {
            return true;
        }
        PhoneSwitcher phoneSwitcher = PhoneSwitcher.getInstance();
        ServiceState serviceState = this.mPhone.getServiceState();
        if (phoneSwitcher == null || serviceState == null || this.mPhone.getPhoneId() == phoneSwitcher.getPreferredDataPhoneId() || serviceState.getVoiceRegState() != 0 || serviceState.getVoiceNetworkType() == 13 || serviceState.getVoiceNetworkType() == 20) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void notifyAllDataDisconnected() {
        sEnableFailFastRefCounter = 0;
        this.mFailFast = false;
        this.mAllDataDisconnectedRegistrants.notifyRegistrants();
    }

    public void registerForAllDataDisconnected(Handler h, int what) {
        this.mAllDataDisconnectedRegistrants.addUnique(h, what, (Object) null);
        if (isDisconnected()) {
            log("notify All Data Disconnected");
            notifyAllDataDisconnected();
        }
    }

    public void unregisterForAllDataDisconnected(Handler h) {
        this.mAllDataDisconnectedRegistrants.remove(h);
    }

    /* access modifiers changed from: protected */
    public void onDataEnabledChanged(boolean enable, int enabledChangedReason) {
        String cleanupReason;
        log("onDataEnabledChanged: enable=" + enable + ", enabledChangedReason=" + enabledChangedReason);
        if (enable) {
            reevaluateDataConnections();
            setupDataOnAllConnectableApns(PhoneInternalInterface.REASON_DATA_ENABLED, RetryFailures.ALWAYS);
            super.oppoWlanAssistantMeasureForDataEnabled(enable);
            return;
        }
        if (enabledChangedReason == 1) {
            cleanupReason = PhoneInternalInterface.REASON_DATA_DISABLED_INTERNAL;
        } else if (enabledChangedReason != 4) {
            cleanupReason = PhoneInternalInterface.REASON_DATA_SPECIFIC_DISABLED;
        } else {
            cleanupReason = PhoneInternalInterface.REASON_CARRIER_ACTION_DISABLE_METERED_APN;
        }
        cleanUpAllConnectionsInternal(true, cleanupReason);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void log(String s) {
        Rlog.d(this.mLogTag, s);
    }

    private void loge(String s) {
        Rlog.e(this.mLogTag, s);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("DcTracker:");
        pw.println(" RADIO_TESTS=false");
        pw.println(" mDataEnabledSettings=" + this.mDataEnabledSettings);
        pw.println(" isDataAllowed=" + isDataAllowed(null));
        pw.flush();
        pw.println(" mRequestedApnType=" + this.mRequestedApnType);
        pw.println(" mPhone=" + this.mPhone.getPhoneName());
        pw.println(" mActivity=" + this.mActivity);
        pw.println(" mState=" + this.mState);
        pw.println(" mTxPkts=" + this.mTxPkts);
        pw.println(" mRxPkts=" + this.mRxPkts);
        pw.println(" mNetStatPollPeriod=" + this.mNetStatPollPeriod);
        pw.println(" mNetStatPollEnabled=" + this.mNetStatPollEnabled);
        pw.println(" mDataStallTxRxSum=" + this.mDataStallTxRxSum);
        pw.println(" mDataStallAlarmTag=" + this.mDataStallAlarmTag);
        pw.println(" mDataStallNoRxEnabled=" + this.mDataStallNoRxEnabled);
        pw.println(" mEmergencyApn=" + this.mEmergencyApn);
        pw.println(" mSentSinceLastRecv=" + this.mSentSinceLastRecv);
        pw.println(" mNoRecvPollCount=" + this.mNoRecvPollCount);
        pw.println(" mResolver=" + this.mResolver);
        pw.println(" mReconnectIntent=" + this.mReconnectIntent);
        pw.println(" mAutoAttachEnabled=" + this.mAutoAttachEnabled.get());
        pw.println(" mIsScreenOn=" + this.mIsScreenOn);
        pw.println(" mUniqueIdGenerator=" + this.mUniqueIdGenerator);
        pw.println(" mDataServiceBound=" + this.mDataServiceBound);
        pw.println(" mDataRoamingLeakageLog= ");
        this.mDataRoamingLeakageLog.dump(fd, pw, args);
        pw.println(" mApnSettingsInitializationLog= ");
        this.mApnSettingsInitializationLog.dump(fd, pw, args);
        pw.flush();
        pw.println(" ***************************************");
        DcController dcc = this.mDcc;
        if (dcc == null) {
            pw.println(" mDcc=null");
        } else if (this.mDataServiceBound) {
            dcc.dump(fd, pw, args);
        } else {
            pw.println(" Can't dump mDcc because data service is not bound.");
        }
        pw.println(" ***************************************");
        if (this.mDataConnections != null) {
            Set<Map.Entry<Integer, DataConnection>> mDcSet = this.mDataConnections.entrySet();
            pw.println(" mDataConnections: count=" + mDcSet.size());
            for (Map.Entry<Integer, DataConnection> entry : mDcSet) {
                pw.printf(" *** mDataConnection[%d] \n", entry.getKey());
                entry.getValue().dump(fd, pw, args);
            }
        } else {
            pw.println("mDataConnections=null");
        }
        pw.println(" ***************************************");
        pw.flush();
        HashMap<String, Integer> apnToDcId = this.mApnToDataConnectionId;
        if (apnToDcId != null) {
            Set<Map.Entry<String, Integer>> apnToDcIdSet = apnToDcId.entrySet();
            pw.println(" mApnToDataConnectonId size=" + apnToDcIdSet.size());
            for (Map.Entry<String, Integer> entry2 : apnToDcIdSet) {
                pw.printf(" mApnToDataConnectonId[%s]=%d\n", entry2.getKey(), entry2.getValue());
            }
        } else {
            pw.println("mApnToDataConnectionId=null");
        }
        pw.println(" ***************************************");
        pw.flush();
        ConcurrentHashMap<String, ApnContext> apnCtxs = this.mApnContexts;
        if (apnCtxs != null) {
            Set<Map.Entry<String, ApnContext>> apnCtxsSet = apnCtxs.entrySet();
            pw.println(" mApnContexts size=" + apnCtxsSet.size());
            for (Map.Entry<String, ApnContext> entry3 : apnCtxsSet) {
                entry3.getValue().dump(fd, pw, args);
            }
            pw.println(" ***************************************");
        } else {
            pw.println(" mApnContexts=null");
        }
        pw.flush();
        pw.println(" mAllApnSettings size=" + this.mAllApnSettings.size());
        for (int i = 0; i < this.mAllApnSettings.size(); i++) {
            pw.printf(" mAllApnSettings[%d]: %s\n", Integer.valueOf(i), this.mAllApnSettings.get(i));
        }
        pw.flush();
        pw.println(" mPreferredApn=" + this.mPreferredApn);
        pw.println(" mIsPsRestricted=" + this.mIsPsRestricted);
        pw.println(" mIsDisposed=" + this.mIsDisposed);
        pw.println(" mIntentReceiver=" + this.mIntentReceiver);
        pw.println(" mReregisterOnReconnectFailure=" + this.mReregisterOnReconnectFailure);
        pw.println(" canSetPreferApn=" + this.mCanSetPreferApn);
        pw.println(" mApnObserver=" + this.mApnObserver);
        pw.println(" getOverallState=" + getOverallState());
        pw.println(" mAttached=" + this.mAttached.get());
        this.mDataEnabledSettings.dump(fd, pw, args);
        pw.flush();
    }

    public String[] getPcscfAddress(String apnType) {
        ApnContext apnContext;
        log("getPcscfAddress()");
        if (apnType == null) {
            log("apnType is null, return null");
            return null;
        }
        if (TextUtils.equals(apnType, "emergency")) {
            apnContext = this.mApnContextsByType.get(512);
        } else if (TextUtils.equals(apnType, "ims")) {
            apnContext = this.mApnContextsByType.get(64);
        } else {
            log("apnType is invalid, return null");
            return null;
        }
        if (apnContext == null) {
            log("apnContext is null, return null");
            return null;
        }
        DataConnection dataConnection = apnContext.getDataConnection();
        if (dataConnection == null) {
            return null;
        }
        String[] result = dataConnection.getPcscfAddresses();
        if (result != null) {
            for (int i = 0; i < result.length; i++) {
                log("Pcscf[" + i + "]: " + result[i]);
            }
        }
        return result;
    }

    private void initEmergencyApnSetting() {
        Cursor cursor = this.mPhone.getContext().getContentResolver().query(Uri.withAppendedPath(Telephony.Carriers.CONTENT_URI, "filtered"), null, mtkGetEmergencyApnSelection("type=\"emergency\""), null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                this.mEmergencyApn = ApnSetting.makeApnSetting(cursor);
            }
            cursor.close();
        }
        if (this.mEmergencyApn == null) {
            this.mEmergencyApn = new ApnSetting.Builder().setEntryName("Emergency").setProtocol(2).setApnName("sos").setApnTypeBitmask(512).build();
        }
    }

    /* access modifiers changed from: protected */
    public void addEmergencyApnSetting() {
        if (this.mEmergencyApn != null) {
            synchronized (this.mRefCountLock) {
                Iterator<ApnSetting> it = this.mAllApnSettings.iterator();
                while (it.hasNext()) {
                    if (it.next().canHandleType(512)) {
                        log("addEmergencyApnSetting - E-APN setting is already present");
                        return;
                    }
                }
                if (!this.mAllApnSettings.contains(this.mEmergencyApn)) {
                    this.mAllApnSettings.add(this.mEmergencyApn);
                    log("Adding emergency APN : " + this.mEmergencyApn);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean containsAllApns(ArrayList<ApnSetting> oldApnList, ArrayList<ApnSetting> newApnList) {
        Iterator<ApnSetting> it = newApnList.iterator();
        while (it.hasNext()) {
            ApnSetting newApnSetting = it.next();
            boolean canHandle = false;
            Iterator<ApnSetting> it2 = oldApnList.iterator();
            while (true) {
                if (it2.hasNext()) {
                    if (it2.next().equals(newApnSetting, this.mPhone.getServiceState().getDataRoamingFromRegistration())) {
                        canHandle = true;
                        continue;
                        break;
                    }
                } else {
                    break;
                }
            }
            if (!canHandle) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void cleanUpConnectionsOnUpdatedApns(boolean detach, String reason) {
        log("cleanUpConnectionsOnUpdatedApns: detach=" + detach);
        if (this.mAllApnSettings.isEmpty()) {
            cleanUpAllConnectionsInternal(detach, PhoneInternalInterface.REASON_APN_CHANGED);
        } else if (getDataRat() != 0) {
            for (ApnContext apnContext : this.mApnContexts.values()) {
                ArrayList<ApnSetting> currentWaitingApns = apnContext.getWaitingApns();
                ArrayList<ApnSetting> waitingApns = buildWaitingApns(apnContext.getApnType(), getDataRat());
                if (currentWaitingApns != null && (waitingApns.size() != currentWaitingApns.size() || !containsAllApns(currentWaitingApns, waitingApns))) {
                    apnContext.setWaitingApns(waitingApns);
                    if (!apnContext.isDisconnected()) {
                        apnContext.setReason(reason);
                        cleanUpConnectionInternal(true, 2, apnContext);
                    }
                }
            }
        } else {
            return;
        }
        if (!isConnected()) {
            stopNetStatPoll();
            stopDataStallAlarm();
        }
        this.mRequestedApnType = 17;
        log("mDisconnectPendingCount = " + this.mDisconnectPendingCount);
        if (detach && this.mDisconnectPendingCount == 0) {
            notifyAllDataDisconnected();
        }
    }

    /* access modifiers changed from: protected */
    public void resetPollStats() {
        this.mTxPkts = -1;
        this.mRxPkts = -1;
        this.mNetStatPollPeriod = 1000;
    }

    /* access modifiers changed from: protected */
    public void startNetStatPoll() {
        if (getOverallState() == DctConstants.State.CONNECTED && !this.mNetStatPollEnabled) {
            log("startNetStatPoll");
            resetPollStats();
            this.mNetStatPollEnabled = true;
            this.mPollNetStat.run();
        }
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.notifyDataActivity();
        }
    }

    /* access modifiers changed from: protected */
    public void stopNetStatPoll() {
        this.mNetStatPollEnabled = false;
        removeCallbacks(this.mPollNetStat);
        log("stopNetStatPoll");
        Phone phone = this.mPhone;
        if (phone != null) {
            phone.notifyDataActivity();
        }
    }

    public void sendStartNetStatPoll(DctConstants.Activity activity) {
        Message msg = obtainMessage(270376);
        msg.arg1 = 1;
        msg.obj = activity;
        sendMessage(msg);
    }

    private void handleStartNetStatPoll(DctConstants.Activity activity) {
        startNetStatPoll();
        startDataStallAlarm(false);
        setActivity(activity);
    }

    public void sendStopNetStatPoll(DctConstants.Activity activity) {
        Message msg = obtainMessage(270376);
        msg.arg1 = 0;
        msg.obj = activity;
        sendMessage(msg);
    }

    private void handleStopNetStatPoll(DctConstants.Activity activity) {
        stopNetStatPoll();
        stopDataStallAlarm();
        setActivity(activity);
    }

    private void onDataEnabledOverrideRulesChanged() {
        log("onDataEnabledOverrideRulesChanged");
        Iterator<ApnContext> it = this.mPrioritySortedApnContexts.iterator();
        while (it.hasNext()) {
            ApnContext apnContext = it.next();
            if (isDataAllowed(apnContext, 1, null)) {
                if (apnContext.getDataConnection() != null) {
                    apnContext.getDataConnection().reevaluateRestrictedState();
                }
                setupDataOnConnectableApn(apnContext, PhoneInternalInterface.REASON_DATA_ENABLED_OVERRIDE, RetryFailures.ALWAYS);
            } else if (shouldCleanUpConnection(apnContext, true)) {
                apnContext.setReason(PhoneInternalInterface.REASON_DATA_ENABLED_OVERRIDE);
                cleanUpConnectionInternal(true, 2, apnContext);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateDataActivity() {
        DctConstants.Activity newActivity;
        TxRxSum preTxRxSum = new TxRxSum(this.mTxPkts, this.mRxPkts);
        TxRxSum curTxRxSum = new TxRxSum();
        curTxRxSum.updateTotalTxRxSum();
        this.mTxPkts = curTxRxSum.txPkts;
        this.mRxPkts = curTxRxSum.rxPkts;
        if (!this.mNetStatPollEnabled) {
            return;
        }
        if (preTxRxSum.txPkts > 0 || preTxRxSum.rxPkts > 0) {
            long sent = this.mTxPkts - preTxRxSum.txPkts;
            long received = this.mRxPkts - preTxRxSum.rxPkts;
            if (sent > 0 && received > 0) {
                newActivity = DctConstants.Activity.DATAINANDOUT;
            } else if (sent > 0 && received == 0) {
                newActivity = DctConstants.Activity.DATAOUT;
            } else if (sent != 0 || received <= 0) {
                newActivity = this.mActivity == DctConstants.Activity.DORMANT ? this.mActivity : DctConstants.Activity.NONE;
            } else {
                newActivity = DctConstants.Activity.DATAIN;
            }
            if (this.mActivity != newActivity && this.mIsScreenOn) {
                this.mActivity = newActivity;
                this.mPhone.notifyDataActivity();
            }
        }
    }

    private void handlePcoData(AsyncResult ar) {
        if (ar.exception != null) {
            loge("PCO_DATA exception: " + ar.exception);
            return;
        }
        PcoData pcoData = (PcoData) ar.result;
        ArrayList<DataConnection> dcList = new ArrayList<>();
        DataConnection temp = this.mDcc.getActiveDcByCid(pcoData.cid);
        if (temp != null) {
            dcList.add(temp);
        }
        if (dcList.size() == 0) {
            loge("PCO_DATA for unknown cid: " + pcoData.cid + ", inferring");
            Iterator<DataConnection> it = this.mDataConnections.values().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                DataConnection dc = it.next();
                int cid = dc.getCid();
                if (cid == pcoData.cid) {
                    dcList.clear();
                    dcList.add(dc);
                    break;
                } else if (cid == -1) {
                    Iterator<ApnContext> it2 = dc.getApnContexts().iterator();
                    while (true) {
                        if (it2.hasNext()) {
                            if (it2.next().getState() == DctConstants.State.CONNECTING) {
                                dcList.add(dc);
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                }
            }
        }
        if (dcList.size() == 0) {
            loge("PCO_DATA - couldn't infer cid");
            return;
        }
        Iterator<DataConnection> it3 = dcList.iterator();
        while (it3.hasNext()) {
            List<ApnContext> apnContextList = it3.next().getApnContexts();
            if (apnContextList.size() != 0) {
                for (ApnContext apnContext : apnContextList) {
                    String apnType = apnContext.getApnType();
                    Intent intent = new Intent("com.android.internal.telephony.CARRIER_SIGNAL_PCO_VALUE");
                    intent.putExtra("apnType", apnType);
                    intent.putExtra("apnProto", pcoData.bearerProto);
                    intent.putExtra("pcoId", pcoData.pcoId);
                    intent.putExtra("pcoValue", pcoData.contents);
                    this.mPhone.getCarrierSignalAgent().notifyCarrierSignalReceivers(intent);
                    mtkHandlePcoByOp(apnContext, pcoData);
                }
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: private */
    public class DataStallRecoveryHandler {
        private static final int DEFAULT_MIN_DURATION_BETWEEN_RECOVERY_STEPS_IN_MS = 180000;
        private boolean mIsValidNetwork;
        private long mTimeLastRecoveryStartMs;

        public DataStallRecoveryHandler() {
            reset();
        }

        public void reset() {
            this.mTimeLastRecoveryStartMs = 0;
            putRecoveryAction(0);
        }

        public boolean isAggressiveRecovery() {
            int action = getRecoveryAction();
            return action == 1 || action == 2 || action == 3;
        }

        private long getMinDurationBetweenRecovery() {
            return Settings.Global.getLong(DcTracker.this.mResolver, "min_duration_between_recovery_steps", 180000);
        }

        private long getElapsedTimeSinceRecoveryMs() {
            return SystemClock.elapsedRealtime() - this.mTimeLastRecoveryStartMs;
        }

        /* access modifiers changed from: protected */
        public int getRecoveryAction() {
            return Settings.System.getInt(DcTracker.this.mResolver, "radio.data.stall.recovery.action", 0);
        }

        /* access modifiers changed from: protected */
        public void putRecoveryAction(int action) {
            Settings.System.putInt(DcTracker.this.mResolver, "radio.data.stall.recovery.action", action);
        }

        private void broadcastDataStallDetected(int recoveryAction) {
            Intent intent = new Intent("android.intent.action.DATA_STALL_DETECTED");
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, DcTracker.this.mPhone.getPhoneId());
            intent.putExtra("recoveryAction", recoveryAction);
            DcTracker.this.mPhone.getContext().sendBroadcast(intent, "android.permission.READ_PRIVILEGED_PHONE_STATE");
        }

        private boolean isRecoveryAlreadyStarted() {
            return getRecoveryAction() != 0;
        }

        private boolean checkRecovery() {
            if (getElapsedTimeSinceRecoveryMs() >= getMinDurationBetweenRecovery() && DcTracker.this.mAttached.get() && DcTracker.this.isDataAllowed(null)) {
                return true;
            }
            return false;
        }

        private void triggerRecovery() {
            DcTracker dcTracker = DcTracker.this;
            dcTracker.sendMessage(dcTracker.obtainMessage(270354));
        }

        public void doRecovery() {
            if (DcTracker.this.getOverallState() == DctConstants.State.CONNECTED) {
                int recoveryAction = getRecoveryAction();
                TelephonyMetrics.getInstance().writeDataStallEvent(DcTracker.this.mPhone.getPhoneId(), recoveryAction);
                broadcastDataStallDetected(recoveryAction);
                if (recoveryAction == 0) {
                    DcTracker dcTracker = DcTracker.this;
                    dcTracker.oemCloseNrCase0(dcTracker.mPhone);
                    EventLog.writeEvent((int) EventLogTags.DATA_STALL_RECOVERY_GET_DATA_CALL_LIST, DcTracker.this.mSentSinceLastRecv);
                    DcTracker.this.log("doRecovery() get data call list");
                    DcTracker.this.mDataServiceManager.requestDataCallList(DcTracker.this.obtainMessage());
                    putRecoveryAction(1);
                } else if (recoveryAction == 1) {
                    EventLog.writeEvent((int) EventLogTags.DATA_STALL_RECOVERY_CLEANUP, DcTracker.this.mSentSinceLastRecv);
                    DcTracker.this.log("doRecovery() cleanup all connections");
                    DcTracker dcTracker2 = DcTracker.this;
                    dcTracker2.cleanUpConnection(dcTracker2.mApnContexts.get(ApnSetting.getApnTypeString(17)));
                    putRecoveryAction(2);
                } else if (recoveryAction == 2) {
                    EventLog.writeEvent((int) EventLogTags.DATA_STALL_RECOVERY_REREGISTER, DcTracker.this.mSentSinceLastRecv);
                    DcTracker.this.log("doRecovery() re-register");
                    DcTracker.this.mPhone.getServiceStateTracker().reRegisterNetwork(null);
                    putRecoveryAction(3);
                    DcTracker dcTracker3 = DcTracker.this;
                    dcTracker3.mHasInboundData = dcTracker3.recordDataStallInfo(dcTracker3.mHasInboundData, DcTracker.this.mSentSinceLastRecv);
                } else if (recoveryAction != 3) {
                    DcTracker dcTracker4 = DcTracker.this;
                    dcTracker4.log("doRecovery: Invalid recoveryAction=" + recoveryAction);
                } else {
                    DcTracker dcTracker5 = DcTracker.this;
                    dcTracker5.oemCloseNrCase1(dcTracker5.mPhone);
                    EventLog.writeEvent((int) EventLogTags.DATA_STALL_RECOVERY_RADIO_RESTART, DcTracker.this.mSentSinceLastRecv);
                    DcTracker.this.log("restarting radio");
                    DcTracker.this.restartRadio();
                    reset();
                }
                DcTracker.this.mSentSinceLastRecv = 0;
                this.mTimeLastRecoveryStartMs = SystemClock.elapsedRealtime();
            }
        }

        public void processNetworkStatusChanged(boolean isValid) {
            if (isValid) {
                this.mIsValidNetwork = true;
                reset();
            } else if (this.mIsValidNetwork || isRecoveryAlreadyStarted()) {
                this.mIsValidNetwork = false;
                if (checkRecovery() && !DcTracker.this.mtkSkipDataStallAlarm()) {
                    DcTracker.this.log("trigger data stall recovery");
                    triggerRecovery();
                }
            }
        }

        public boolean isRecoveryOnBadNetworkEnabled() {
            return Settings.Global.getInt(DcTracker.this.mResolver, "data_stall_recovery_on_bad_network", 1) == 1;
        }

        public boolean isNoRxDataStallDetectionEnabled() {
            return DcTracker.this.mDataStallNoRxEnabled && !isRecoveryOnBadNetworkEnabled();
        }
    }

    private void updateDataStallInfo() {
        TxRxSum preTxRxSum = new TxRxSum(this.mDataStallTxRxSum);
        this.mDataStallTxRxSum.updateTcpTxRxSum();
        mtkUpdateTotalTxRxSum();
        long sent = this.mDataStallTxRxSum.txPkts - preTxRxSum.txPkts;
        long received = this.mDataStallTxRxSum.rxPkts - preTxRxSum.rxPkts;
        if (sent > 0 && received > 0) {
            this.mSentSinceLastRecv = 0;
            this.mDsRecoveryHandler.reset();
            this.mHasInboundData = true;
        } else if (sent > 0 && received == 0) {
            if (isPhoneStateIdle()) {
                this.mSentSinceLastRecv += sent;
            } else {
                this.mSentSinceLastRecv = 0;
            }
            log("updateDataStallInfo: OUT sent=" + sent + " mSentSinceLastRecv=" + this.mSentSinceLastRecv);
        } else if (sent == 0 && received > 0) {
            this.mSentSinceLastRecv = 0;
            this.mDsRecoveryHandler.reset();
            this.mHasInboundData = true;
        }
    }

    private boolean isPhoneStateIdle() {
        for (int i = 0; i < this.mTelephonyManager.getPhoneCount(); i++) {
            Phone phone = PhoneFactory.getPhone(i);
            if (phone != null && phone.getState() != PhoneConstants.State.IDLE) {
                log("isPhoneStateIdle false: Voice call active on phone " + i);
                return false;
            }
        }
        return true;
    }

    private void onDataStallAlarm(int tag) {
        if (this.mDataStallAlarmTag != tag) {
            log("onDataStallAlarm: ignore, tag=" + tag + " expecting " + this.mDataStallAlarmTag);
            return;
        }
        log("Data stall alarm");
        updateDataStallInfo();
        boolean suspectedStall = false;
        if (this.mSentSinceLastRecv >= ((long) Settings.Global.getInt(this.mResolver, "pdp_watchdog_trigger_packet_count", 10))) {
            log("onDataStallAlarm: tag=" + tag + " do recovery action=" + this.mDsRecoveryHandler.getRecoveryAction());
            suspectedStall = true;
            sendMessage(obtainMessage(270354));
        }
        startDataStallAlarm(suspectedStall);
    }

    /* access modifiers changed from: protected */
    public void startDataStallAlarm(boolean suspectedStall) {
        int delayInMs;
        if (super.checkIfNeedDataStall() && this.mDsRecoveryHandler.isNoRxDataStallDetectionEnabled() && getOverallState() == DctConstants.State.CONNECTED) {
            if (this.mIsScreenOn || suspectedStall || this.mDsRecoveryHandler.isAggressiveRecovery()) {
                delayInMs = Settings.Global.getInt(this.mResolver, "data_stall_alarm_aggressive_delay_in_ms", 60000);
            } else {
                delayInMs = Settings.Global.getInt(this.mResolver, "data_stall_alarm_non_aggressive_delay_in_ms", DATA_STALL_ALARM_NON_AGGRESSIVE_DELAY_IN_MS_DEFAULT);
            }
            this.mDataStallAlarmTag++;
            Intent intent = new Intent(INTENT_DATA_STALL_ALARM);
            intent.putExtra(INTENT_DATA_STALL_ALARM_EXTRA_TAG, this.mDataStallAlarmTag);
            intent.putExtra(INTENT_DATA_STALL_ALARM_EXTRA_TRANSPORT_TYPE, this.mTransportType);
            SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
            this.mDataStallAlarmIntent = PendingIntent.getBroadcast(this.mPhone.getContext(), this.mPhone.getPhoneId() + 1, intent, 201326592);
            this.mAlarmManager.set(3, SystemClock.elapsedRealtime() + ((long) delayInMs), this.mDataStallAlarmIntent);
        }
    }

    /* access modifiers changed from: protected */
    public void stopDataStallAlarm() {
        this.mDataStallAlarmTag++;
        PendingIntent pendingIntent = this.mDataStallAlarmIntent;
        if (pendingIntent != null) {
            this.mAlarmManager.cancel(pendingIntent);
            this.mDataStallAlarmIntent = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void restartDataStallAlarm() {
        if (isConnected()) {
            if (this.mDsRecoveryHandler.isAggressiveRecovery()) {
                log("restartDataStallAlarm: action is pending. not resetting the alarm.");
                return;
            }
            stopDataStallAlarm();
            startDataStallAlarm(false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onActionIntentProvisioningApnAlarm(Intent intent) {
        log("onActionIntentProvisioningApnAlarm: action=" + intent.getAction());
        Message msg = obtainMessage(270375, intent.getAction());
        msg.arg1 = intent.getIntExtra(PROVISIONING_APN_ALARM_TAG_EXTRA, 0);
        sendMessage(msg);
    }

    private void startProvisioningApnAlarm() {
        int delayInMs = Settings.Global.getInt(this.mResolver, "provisioning_apn_alarm_delay_in_ms", PROVISIONING_APN_ALARM_DELAY_IN_MS_DEFAULT);
        if (Build.IS_DEBUGGABLE) {
            try {
                delayInMs = Integer.parseInt(System.getProperty(DEBUG_PROV_APN_ALARM, Integer.toString(delayInMs)));
            } catch (NumberFormatException e) {
                loge("startProvisioningApnAlarm: e=" + e);
            } catch (Exception e2) {
                Rlog.d(LOG_TAG, e2.toString());
            }
        }
        this.mProvisioningApnAlarmTag++;
        log("startProvisioningApnAlarm: tag=" + this.mProvisioningApnAlarmTag + " delay=" + (delayInMs / 1000) + "s");
        Intent intent = new Intent(INTENT_PROVISIONING_APN_ALARM);
        intent.putExtra(PROVISIONING_APN_ALARM_TAG_EXTRA, this.mProvisioningApnAlarmTag);
        this.mProvisioningApnAlarmIntent = PendingIntent.getBroadcast(this.mPhone.getContext(), this.mPhone.getPhoneId() + 1, intent, 201326592);
        this.mAlarmManager.set(2, SystemClock.elapsedRealtime() + ((long) delayInMs), this.mProvisioningApnAlarmIntent);
    }

    private void stopProvisioningApnAlarm() {
        log("stopProvisioningApnAlarm: current tag=" + this.mProvisioningApnAlarmTag + " mProvsioningApnAlarmIntent=" + this.mProvisioningApnAlarmIntent);
        this.mProvisioningApnAlarmTag = this.mProvisioningApnAlarmTag + 1;
        PendingIntent pendingIntent = this.mProvisioningApnAlarmIntent;
        if (pendingIntent != null) {
            this.mAlarmManager.cancel(pendingIntent);
            this.mProvisioningApnAlarmIntent = null;
        }
    }

    protected static DataProfile createDataProfile(ApnSetting apn, boolean isPreferred) {
        return createDataProfile(apn, apn.getProfileId(), isPreferred);
    }

    @VisibleForTesting
    public static DataProfile createDataProfile(ApnSetting apn, int profileId, boolean isPreferred) {
        int profileType;
        int networkTypeBitmask = apn.getNetworkTypeBitmask();
        if (networkTypeBitmask == 0) {
            profileType = 0;
        } else if (ServiceState.bearerBitmapHasCdma(networkTypeBitmask)) {
            profileType = 2;
        } else {
            profileType = 1;
        }
        return new DataProfile.Builder().setProfileId(profileId).setApn(apn.getApnName()).setProtocolType(apn.getProtocol()).setAuthType(apn.getAuthType()).setUserName(apn.getUser()).setPassword(apn.getPassword()).setType(profileType).setMaxConnectionsTime(apn.getMaxConnsTime()).setMaxConnections(apn.getMaxConns()).setWaitTime(apn.getWaitTime()).enable(apn.isEnabled()).setSupportedApnTypesBitmask(apn.getApnTypeBitmask()).setRoamingProtocolType(apn.getRoamingProtocol()).setBearerBitmask(networkTypeBitmask).setMtu(apn.getMtu()).setPersistent(apn.isPersistent()).setPreferred(isPreferred).build();
    }

    private void onDataServiceBindingChanged(boolean bound) {
        if (bound) {
            this.mDcc.start();
        } else {
            this.mDcc.dispose();
        }
        this.mDataServiceBound = bound;
    }

    public static String requestTypeToString(int type) {
        if (type == 1) {
            return "NORMAL";
        }
        if (type != 2) {
            return "UNKNOWN";
        }
        return "HANDOVER";
    }

    public static String releaseTypeToString(int type) {
        if (type == 1) {
            return "NORMAL";
        }
        if (type == 2) {
            return "DETACH";
        }
        if (type != 3) {
            return "UNKNOWN";
        }
        return "HANDOVER";
    }

    /* access modifiers changed from: protected */
    public int getDataRat() {
        NetworkRegistrationInfo nrs = this.mPhone.getServiceState().getNetworkRegistrationInfo(2, this.mTransportType);
        if (nrs != null) {
            return ServiceState.networkTypeToRilRadioTechnology(nrs.getAccessNetworkTechnology());
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    public int getVoiceRat() {
        NetworkRegistrationInfo nrs = this.mPhone.getServiceState().getNetworkRegistrationInfo(1, this.mTransportType);
        if (nrs != null) {
            return ServiceState.networkTypeToRilRadioTechnology(nrs.getAccessNetworkTechnology());
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    public void mtkCopyHandlerThread(HandlerThread t) {
    }

    /* access modifiers changed from: protected */
    public long mtkModifyInterApnDelay(long delay, ApnContext apnContext) {
        return delay;
    }

    /* access modifiers changed from: protected */
    public boolean mtkIsNeedNotify(ApnContext apnContext) {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean mtkIsPermanentFailure(int dcFailCause) {
        return false;
    }

    /* access modifiers changed from: protected */
    public String mtkGetEmergencyApnSelection(String selection) {
        return selection;
    }

    /* access modifiers changed from: protected */
    public boolean mtkIsNeedRegisterSettingsObserver(int pSubId, int subId) {
        return true;
    }

    /* access modifiers changed from: protected */
    public String mtkGetOperatorNumeric(IccRecords r) {
        return r != null ? r.getOperatorNumeric() : PhoneConfigurationManager.SSSS;
    }

    /* access modifiers changed from: protected */
    public void mtkUpdateTotalTxRxSum() {
    }

    /* access modifiers changed from: protected */
    public void mtkSyncApnContextDisableState(ApnContext apnContext, int releaseType) {
    }

    /* access modifiers changed from: protected */
    public void mtkHandlePcoByOp(ApnContext apnContext, PcoData pcoData) {
    }

    /* access modifiers changed from: protected */
    public void mtkTearDown(ApnContext apnContext, Message msg) {
    }

    /* access modifiers changed from: protected */
    public boolean mtkSkipCheckForCompatibleConnectedApnContext(ApnContext apnContext) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean mtkCanHandleOnDataSetupComplete(ApnContext apnContext, boolean success, int cause, int requestType) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean mtkIsInUse(ApnContext apnContext, DataConnection dc) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean mtkAddOrSendRequestNetworkCompleteMsg(int apnType, int requestType, Message onCompleteMsg) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void mtkFakeDataConnection(ApnContext apnContext) {
    }

    /* access modifiers changed from: protected */
    public boolean mtkSkipDataStallAlarm() {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean mtkIsUseCarrierRoamingData() {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean mtkIsSetFalseForUserAction() {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean mtkIsApnCanSupportNetworkType(ApnSetting apn, int radioTech) {
        return true;
    }

    protected static int getUiccFamilyByRat(int dataRat) {
        if (ServiceState.isGsm(dataRat) || dataRat == 13 || !ServiceState.isCdma(dataRat)) {
            return 1;
        }
        return 2;
    }

    @Override // com.android.internal.telephony.dataconnection.AbstractDcTracker
    public String getOperatorNumeric() {
        String result = mtkGetOperatorNumeric(this.mIccRecords.get());
        log("getOperatorNumberic - returning from card: " + result);
        return result;
    }

    /* access modifiers changed from: protected */
    public boolean isSamePaidApn(ApnSetting first, ApnSetting second) {
        return false;
    }

    @Override // com.android.internal.telephony.dataconnection.AbstractDcTracker
    public AtomicReference<IccRecords> getIccRecords() {
        return this.mIccRecords;
    }

    @Override // com.android.internal.telephony.dataconnection.AbstractDcTracker
    public void setupDataOnAllConnectableApns(String reason) {
        setupDataOnAllConnectableApns(reason, RetryFailures.ALWAYS);
    }
}
