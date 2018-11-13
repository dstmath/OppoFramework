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
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.LinkProperties;
import android.net.NetworkCapabilities;
import android.net.NetworkConfig;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkRequest;
import android.net.NetworkUtils;
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
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.provider.Telephony.Carriers;
import android.provider.oppo.CallLog.Calls;
import android.telephony.CarrierConfigManager;
import android.telephony.CellLocation;
import android.telephony.PcoData;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionManager.OnSubscriptionsChangedListener;
import android.telephony.TelephonyManager;
import android.telephony.TelephonyManager.MultiSimVariants;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.LocalLog;
import android.util.Pair;
import android.util.SparseArray;
import com.android.internal.telephony.DctConstants.Activity;
import com.android.internal.telephony.DctConstants.State;
import com.android.internal.telephony.EventLogTags;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.ITelephony.Stub;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneConstants.DataState;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.RIL;
import com.android.internal.telephony.SettingsObserver;
import com.android.internal.telephony.cdma.CdmaSubscriptionSourceManager;
import com.android.internal.telephony.dataconnection.DataConnectionReasons.DataDisallowedReasonType;
import com.android.internal.telephony.metrics.TelephonyMetrics;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.RuimRecords;
import com.android.internal.telephony.uicc.SIMRecords;
import com.android.internal.telephony.uicc.SpnOverride;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.util.NotificationChannelController;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.AsyncChannel;
import com.google.android.mms.pdu.CharacterSets;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class DcTracker extends Handler {
    /* renamed from: -com-android-internal-telephony-DctConstants$StateSwitchesValues */
    private static final /* synthetic */ int[] f21-com-android-internal-telephony-DctConstants$StateSwitchesValues = null;
    static final String APN_ID = "apn_id";
    private static final int DATA_STALL_ALARM_AGGRESSIVE_DELAY_IN_MS_DEFAULT = 60000;
    private static final int DATA_STALL_ALARM_NON_AGGRESSIVE_DELAY_IN_MS_DEFAULT = 360000;
    private static final String DATA_STALL_ALARM_TAG_EXTRA = "data.stall.alram.tag";
    private static final boolean DATA_STALL_NOT_SUSPECTED = false;
    private static final boolean DATA_STALL_SUSPECTED = true;
    protected static final boolean DBG = true;
    private static final String DEBUG_PROV_APN_ALARM = "persist.debug.prov_apn_alarm";
    private static final int EVENT_SIM_RECORDS_LOADED = 100;
    private static final String INTENT_DATA_STALL_ALARM = "com.android.internal.telephony.data-stall";
    private static final String INTENT_PROVISIONING_APN_ALARM = "com.android.internal.telephony.provisioning_apn_alarm";
    private static final String INTENT_RECONNECT_ALARM = "com.android.internal.telephony.data-reconnect";
    private static final String INTENT_RECONNECT_ALARM_EXTRA_REASON = "reconnect_alarm_extra_reason";
    private static final String INTENT_RECONNECT_ALARM_EXTRA_TYPE = "reconnect_alarm_extra_type";
    private static final int NUMBER_SENT_PACKETS_OF_HANG = 10;
    private static final int POLL_NETSTAT_MILLIS = 1000;
    private static final int POLL_NETSTAT_SCREEN_OFF_MILLIS = 600000;
    private static final int POLL_PDP_MILLIS = 5000;
    static final Uri PREFERAPN_NO_UPDATE_URI_USING_SUBID = Uri.parse("content://telephony/carriers/preferapn_no_update/subId/");
    private static final int PROVISIONING_APN_ALARM_DELAY_IN_MS_DEFAULT = 900000;
    private static final String PROVISIONING_APN_ALARM_TAG_EXTRA = "provisioning.apn.alarm.tag";
    private static final int PROVISIONING_SPINNER_TIMEOUT_MILLIS = 120000;
    private static final String PUPPET_MASTER_RADIO_STRESS_TEST = "gsm.defaultpdpcontext.active";
    private static final boolean RADIO_TESTS = false;
    protected static final int THRESHOLD_TO_RECORD = 10;
    private static final boolean VDBG = false;
    private static final boolean VDBG_STALL = false;
    private static final String WIFI_SCORE_CHANGE = "android.net.wifi.WIFI_SCORE_CHANGE";
    public static boolean mDelayMeasure = false;
    public static int mLastDataRadioTech = 0;
    public static NetworkCallback mMeasureDCCallback;
    public static boolean mMeasureDataState = false;
    protected static boolean mOppoCtaSupport = false;
    public static boolean mVsimIgnoreUserDataSetting = false;
    private static int sEnableFailFastRefCounter = 0;
    protected static final String[] sTelstraOperaters = new String[]{"50501", "50511", "50571", "50572"};
    private final int LINGER_TIMER;
    protected String LOG_TAG;
    private String RADIO_RESET_PROPERTY;
    private final int RETRY_TIMES;
    private final int WAITTING_TIMEOUT;
    public AtomicBoolean isCleanupRequired;
    private Activity mActivity;
    private final AlarmManager mAlarmManager;
    protected ArrayList<ApnSetting> mAllApnSettings;
    private RegistrantList mAllDataDisconnectedRegistrants;
    private final ConcurrentHashMap<String, ApnContext> mApnContexts;
    private final SparseArray<ApnContext> mApnContextsById;
    private ApnChangeObserver mApnObserver;
    private HashMap<String, Integer> mApnToDataConnectionId;
    private AtomicBoolean mAttached;
    protected AtomicBoolean mAutoAttachOnCreation;
    protected boolean mAutoAttachOnCreationConfig;
    private boolean mCanSetPreferApn;
    private final ConnectivityManager mCm;
    private HashMap<Integer, DcAsyncChannel> mDataConnectionAcHashMap;
    private final Handler mDataConnectionTracker;
    private HashMap<Integer, DataConnection> mDataConnections;
    private final DataEnabledSettings mDataEnabledSettings;
    private final LocalLog mDataRoamingLeakageLog;
    private PendingIntent mDataStallAlarmIntent;
    private int mDataStallAlarmTag;
    private volatile boolean mDataStallDetectionEnabled;
    private TxRxSum mDataStallTxRxSum;
    private int mDataType;
    private DcTesterFailBringUpAll mDcTesterFailBringUpAll;
    private DcController mDcc;
    private ArrayList<Message> mDisconnectAllCompleteMsgList;
    private int mDisconnectPendingCount;
    private ApnSetting mEmergencyApn;
    private volatile boolean mFailFast;
    protected boolean mHasDataConnConncted;
    protected boolean mHasInboundData;
    protected final AtomicReference<IccRecords> mIccRecords;
    public boolean mImsRegistrationState;
    private boolean mInVoiceCall;
    private final BroadcastReceiver mIntentReceiver;
    protected boolean mIsDisposed;
    private boolean mIsProvisioning;
    private boolean mIsPsRestricted;
    private boolean mIsScreenOn;
    private boolean mIsWifiConnected;
    private boolean mMeteredApnDisabled;
    protected boolean mMvnoMatched;
    private boolean mNetStatPollEnabled;
    private int mNetStatPollPeriod;
    private int mNoRecvPollCount;
    private final OnSubscriptionsChangedListener mOnSubscriptionsChangedListener;
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
    private AsyncChannel mReplyAc;
    private String mRequestedApnType;
    private boolean mReregisterOnReconnectFailure;
    private ContentResolver mResolver;
    private long mRxPkts;
    private long mSentSinceLastRecv;
    private int mSetDataProfileStatus;
    private final SettingsObserver mSettingsObserver;
    protected int mSetupDataCallFailureCount;
    private SIMRecords mSimRecords;
    protected State mState;
    private SubscriptionManager mSubscriptionManager;
    private long mTxPkts;
    private final UiccController mUiccController;
    private AtomicInteger mUniqueIdGenerator;
    private long mWifiConnectTimeStamp;
    private DetailedState mWifiOldState;
    private final int[] waitToRetry;

    private class ApnChangeObserver extends ContentObserver {
        public ApnChangeObserver() {
            super(DcTracker.this.mDataConnectionTracker);
        }

        public void onChange(boolean selfChange) {
            DcTracker.this.sendMessage(DcTracker.this.obtainMessage(270355));
        }
    }

    private class ProvisionNotificationBroadcastReceiver extends BroadcastReceiver {
        private final String mNetworkOperator;
        private final String mProvisionUrl;

        public ProvisionNotificationBroadcastReceiver(String provisionUrl, String networkOperator) {
            this.mNetworkOperator = networkOperator;
            this.mProvisionUrl = provisionUrl;
        }

        private void setEnableFailFastMobileData(int enabled) {
            DcTracker.this.sendMessage(DcTracker.this.obtainMessage(270372, enabled, 0));
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
            DcTracker.this.mProvisioningSpinner.setMessage(context.getText(17040231));
            DcTracker.this.mProvisioningSpinner.setIndeterminate(true);
            DcTracker.this.mProvisioningSpinner.setCancelable(true);
            DcTracker.this.mProvisioningSpinner.getWindow().setType(2009);
            DcTracker.this.mProvisioningSpinner.show();
            DcTracker.this.sendMessageDelayed(DcTracker.this.obtainMessage(270378, DcTracker.this.mProvisioningSpinner), 120000);
            DcTracker.this.setRadio(true);
            setEnableFailFastMobileData(1);
            enableMobileProvisioning();
        }
    }

    private static class RecoveryAction {
        public static final int CLEANUP = 1;
        public static final int GET_DATA_CALL_LIST = 0;
        public static final int RADIO_RESTART = 3;
        public static final int RADIO_RESTART_WITH_PROP = 4;
        public static final int REREGISTER = 2;

        private RecoveryAction() {
        }

        private static boolean isAggressiveRecovery(int value) {
            if (value == 1 || value == 2 || value == 3 || value == 4) {
                return true;
            }
            return false;
        }
    }

    private enum RetryFailures {
        ALWAYS,
        ONLY_ON_CHANGE
    }

    public static class TxRxSum {
        public long rxPkts;
        public long txPkts;

        public TxRxSum() {
            reset();
        }

        public TxRxSum(long txPkts, long rxPkts) {
            this.txPkts = txPkts;
            this.rxPkts = rxPkts;
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

        public void updateTxRxSum() {
            if (DcTracker.mOppoCtaSupport) {
                this.txPkts = TrafficStats.getMobileTxPackets();
                this.rxPkts = TrafficStats.getMobileRxPackets();
                return;
            }
            this.txPkts = TrafficStats.getMobileTcpTxPackets();
            this.rxPkts = TrafficStats.getMobileTcpRxPackets();
        }
    }

    /* renamed from: -getcom-android-internal-telephony-DctConstants$StateSwitchesValues */
    private static /* synthetic */ int[] m21xf0fbc33d() {
        if (f21-com-android-internal-telephony-DctConstants$StateSwitchesValues != null) {
            return f21-com-android-internal-telephony-DctConstants$StateSwitchesValues;
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
        f21-com-android-internal-telephony-DctConstants$StateSwitchesValues = iArr;
        return iArr;
    }

    private void registerSettingsObserver() {
        this.mSettingsObserver.unobserve();
        String simSuffix = SpnOverride.MVNO_TYPE_NONE;
        if (TelephonyManager.getDefault().getSimCount() > 1) {
            simSuffix = Integer.toString(this.mPhone.getSubId());
        }
        this.mSettingsObserver.observe(Global.getUriFor("data_roaming" + simSuffix), 270384);
        this.mSettingsObserver.observe(Global.getUriFor("device_provisioned"), 270379);
        this.mSettingsObserver.observe(Global.getUriFor("device_provisioning_mobile_data"), 270379);
    }

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
        log("onDataReconnect: currSubId = " + currSubId + " phoneSubId=" + phoneSubId);
        if (this.mSubscriptionManager.isActiveSubId(currSubId) && currSubId == phoneSubId) {
            ApnContext apnContext = (ApnContext) this.mApnContexts.get(apnType);
            log("onDataReconnect: mState=" + this.mState + " reason=" + reason + " apnType=" + apnType + " apnContext=" + apnContext + " mDataConnectionAsyncChannels=" + this.mDataConnectionAcHashMap);
            if (apnContext != null && apnContext.isEnabled()) {
                apnContext.setReason(reason);
                State apnContextState = apnContext.getState();
                log("onDataReconnect: apnContext state=" + apnContextState);
                if (apnContextState == State.FAILED || apnContextState == State.IDLE) {
                    log("onDataReconnect: state is FAILED|IDLE, disassociate");
                    DcAsyncChannel dcac = apnContext.getDcAc();
                    if (dcac != null) {
                        log("onDataReconnect: tearDown apnContext=" + apnContext);
                        dcac.tearDown(apnContext, SpnOverride.MVNO_TYPE_NONE, null);
                    }
                    apnContext.setDataConnectionAc(null);
                    apnContext.setState(State.IDLE);
                } else {
                    log("onDataReconnect: keep associated");
                }
                sendMessage(obtainMessage(270339, apnContext));
                apnContext.setReconnectIntent(null);
            }
            return;
        }
        log("receive ReconnectAlarm but subId incorrect, ignore");
    }

    private void onActionIntentDataStallAlarm(Intent intent) {
        Message msg = obtainMessage(270353, intent.getAction());
        msg.arg1 = intent.getIntExtra(DATA_STALL_ALARM_TAG_EXTRA, 0);
        sendMessage(msg);
    }

    public DcTracker(Phone phone) {
        this.LOG_TAG = "DCT";
        this.isCleanupRequired = new AtomicBoolean(false);
        this.mRequestedApnType = "default";
        this.mDataEnabledSettings = new DataEnabledSettings();
        this.RADIO_RESET_PROPERTY = "gsm.radioreset";
        this.mWifiOldState = DetailedState.IDLE;
        this.mWifiConnectTimeStamp = 0;
        this.LINGER_TIMER = 3000;
        this.RETRY_TIMES = 3;
        this.waitToRetry = new int[]{3, 30, 60};
        this.WAITTING_TIMEOUT = 60;
        this.mSetupDataCallFailureCount = 0;
        this.mHasDataConnConncted = true;
        this.mHasInboundData = true;
        this.mDataType = 0;
        this.mPrioritySortedApnContexts = new PriorityQueue(5, new Comparator<ApnContext>() {
            public int compare(ApnContext c1, ApnContext c2) {
                return c2.priority - c1.priority;
            }
        });
        this.mAllApnSettings = null;
        this.mPreferredApn = null;
        this.mIsPsRestricted = false;
        this.mEmergencyApn = null;
        this.mIsDisposed = false;
        this.mIsProvisioning = false;
        this.mProvisioningUrl = null;
        this.mProvisioningApnAlarmIntent = null;
        this.mProvisioningApnAlarmTag = (int) SystemClock.elapsedRealtime();
        this.mReplyAc = new AsyncChannel();
        this.mDataRoamingLeakageLog = new LocalLog(50);
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.startsWith(DcTracker.INTENT_RECONNECT_ALARM)) {
                    DcTracker.this.log("Reconnect alarm. Previous state was " + DcTracker.this.mState);
                    DcTracker.this.onActionIntentReconnectAlarm(intent);
                } else if (action.equals(DcTracker.INTENT_DATA_STALL_ALARM)) {
                    DcTracker.this.log("Data stall alarm");
                    DcTracker.this.onActionIntentDataStallAlarm(intent);
                } else if (action.equals(DcTracker.INTENT_PROVISIONING_APN_ALARM)) {
                    DcTracker.this.log("Provisioning apn alarm");
                    DcTracker.this.onActionIntentProvisioningApnAlarm(intent);
                } else if (action.equals("android.net.wifi.STATE_CHANGE")) {
                    boolean isConnected;
                    NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    DcTracker dcTracker = DcTracker.this;
                    if (networkInfo != null) {
                        isConnected = networkInfo.isConnected();
                    } else {
                        isConnected = false;
                    }
                    dcTracker.mIsWifiConnected = isConnected;
                    if (networkInfo != null) {
                        DetailedState state = networkInfo.getDetailedState();
                        DcTracker.this.log("NETWORK_STATE_CHANGED_ACTION: mIsWifiConnected=" + DcTracker.this.mIsWifiConnected + " state:" + state + " mWifiOldState:" + DcTracker.this.mWifiOldState);
                        if (OemConstant.getWlanAssistantEnable(DcTracker.this.mPhone.getContext())) {
                            if (!DcTracker.this.mIsWifiConnected && state == DetailedState.DISCONNECTED) {
                                DcTracker.mMeasureDataState = false;
                                DcTracker.this.mCm.shouldKeepCelluarNetwork(DcTracker.mMeasureDataState);
                            } else if (!(DcTracker.this.mWifiOldState == DetailedState.CONNECTED || state != DetailedState.CONNECTED || DcTracker.mMeasureDCCallback == null)) {
                                DcTracker.this.mWifiConnectTimeStamp = SystemClock.elapsedRealtime();
                                DcTracker.this.log("WLAN+ NETWORK_STATE_CHANGED_ACTION release DC: mMeasureDataState=" + DcTracker.mMeasureDataState);
                                try {
                                    DcTracker.this.mCm.unregisterNetworkCallback(DcTracker.mMeasureDCCallback);
                                } catch (IllegalArgumentException e) {
                                    DcTracker.this.log("WLAN+ " + e.toString());
                                } catch (Exception e2) {
                                    DcTracker.this.log("WLAN+ Exception:" + e2.toString());
                                }
                                DcTracker.mMeasureDCCallback = null;
                            }
                        }
                        if (state == DetailedState.DISCONNECTED && DcTracker.this.mWifiOldState == DetailedState.CONNECTED && (DcTracker.this.getDataEnabled() || DcTracker.this.haveVsimIgnoreUserDataSetting())) {
                            DcTracker.this.setupDataOnConnectableApns("android.net.wifi.STATE_CHANGE");
                        }
                        DcTracker.this.mWifiOldState = state;
                    }
                } else if (action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                    DcTracker.this.log("Wifi state changed");
                    boolean enabled = intent.getIntExtra("wifi_state", 4) == 3;
                    if (!enabled) {
                        DcTracker.this.mIsWifiConnected = false;
                    }
                    DcTracker.this.log("WIFI_STATE_CHANGED_ACTION: enabled=" + enabled + " mIsWifiConnected=" + DcTracker.this.mIsWifiConnected);
                    if (OemConstant.getWlanAssistantEnable(DcTracker.this.mPhone.getContext()) && !enabled) {
                        DcTracker.mMeasureDataState = false;
                        DcTracker.this.mCm.shouldKeepCelluarNetwork(DcTracker.mMeasureDataState);
                    }
                } else if (action.equals(DcTracker.WIFI_SCORE_CHANGE)) {
                    SubscriptionManager su = SubscriptionManager.from(DcTracker.this.mPhone.getContext());
                    boolean isDefaultDataPhone = DcTracker.this.mPhone.getSubId() == SubscriptionManager.getDefaultDataSubId();
                    DcTracker.mMeasureDataState = intent.getBooleanExtra("enableData", false);
                    DcTracker.this.mCm.shouldKeepCelluarNetwork(DcTracker.mMeasureDataState);
                    if (isDefaultDataPhone) {
                        new Thread() {
                            /* renamed from: -com-android-internal-telephony-DctConstants$StateSwitchesValues */
                            private static final /* synthetic */ int[] f22-com-android-internal-telephony-DctConstants$StateSwitchesValues = null;
                            final /* synthetic */ int[] $SWITCH_TABLE$com$android$internal$telephony$DctConstants$State;

                            /* renamed from: -getcom-android-internal-telephony-DctConstants$StateSwitchesValues */
                            private static /* synthetic */ int[] m22xf0fbc33d() {
                                if (f22-com-android-internal-telephony-DctConstants$StateSwitchesValues != null) {
                                    return f22-com-android-internal-telephony-DctConstants$StateSwitchesValues;
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
                                    iArr[State.FAILED.ordinal()] = 7;
                                } catch (NoSuchFieldError e4) {
                                }
                                try {
                                    iArr[State.IDLE.ordinal()] = 4;
                                } catch (NoSuchFieldError e5) {
                                }
                                try {
                                    iArr[State.RETRYING.ordinal()] = 5;
                                } catch (NoSuchFieldError e6) {
                                }
                                try {
                                    iArr[State.SCANNING.ordinal()] = 6;
                                } catch (NoSuchFieldError e7) {
                                }
                                f22-com-android-internal-telephony-DctConstants$StateSwitchesValues = iArr;
                                return iArr;
                            }

                            public void run() {
                                boolean isBusy = true;
                                int wait = 0;
                                long now = SystemClock.elapsedRealtime();
                                while (true) {
                                    if ((isBusy || now - DcTracker.this.mWifiConnectTimeStamp < 3000) && wait < 60) {
                                        ApnContext defaultApnContext = (ApnContext) DcTracker.this.mApnContextsById.get(0);
                                        if (defaultApnContext != null) {
                                            switch (AnonymousClass1.m22xf0fbc33d()[defaultApnContext.getState().ordinal()]) {
                                                case 2:
                                                case 3:
                                                case 5:
                                                case 6:
                                                    isBusy = true;
                                                    break;
                                                default:
                                                    isBusy = false;
                                                    break;
                                            }
                                            long sleepTime = 0;
                                            if (now - DcTracker.this.mWifiConnectTimeStamp < 3000) {
                                                sleepTime = 3000 - (now - DcTracker.this.mWifiConnectTimeStamp);
                                            } else if (isBusy) {
                                                sleepTime = 1000;
                                            }
                                            try {
                                                DcTracker.this.log("WLAN+ WIFI_SCORE_CHANGE waiting " + sleepTime + "ms for last (dis)connect finish!");
                                                Thread.sleep(sleepTime);
                                            } catch (Exception e) {
                                                DcTracker.this.log("WLAN+ " + e.toString());
                                            }
                                            wait++;
                                            now = SystemClock.elapsedRealtime();
                                        }
                                    }
                                }
                                if (DcTracker.mMeasureDataState) {
                                    int i = 0;
                                    while (i < 3) {
                                        boolean myMeasureDataState;
                                        if (DcTracker.this.getDataEnabled() || DcTracker.this.haveVsimIgnoreUserDataSetting()) {
                                            myMeasureDataState = DcTracker.this.mPhone.getServiceState().getRoaming() ^ 1;
                                        } else {
                                            myMeasureDataState = false;
                                        }
                                        DcTracker.this.log("WLAN+ WIFI_SCORE_CHANGE " + i + ": mMeasureDataState=" + DcTracker.mMeasureDataState + " Roaming=" + DcTracker.this.mPhone.getServiceState().getRoaming() + " DataEnabled=" + (!DcTracker.this.getDataEnabled() ? DcTracker.this.haveVsimIgnoreUserDataSetting() : true));
                                        if (myMeasureDataState) {
                                            NetworkRequest request = DcTracker.this.mCm.getCelluarNetworkRequest();
                                            if (request != null) {
                                                if (DcTracker.mMeasureDCCallback != null) {
                                                    DcTracker.this.log("WLAN+ WIFI_SCORE_CHANGE release DC befor request: mMeasureDataState=" + DcTracker.mMeasureDataState);
                                                    try {
                                                        DcTracker.this.mCm.unregisterNetworkCallback(DcTracker.mMeasureDCCallback);
                                                    } catch (IllegalArgumentException e2) {
                                                        DcTracker.this.log("WLAN+ " + e2.toString());
                                                    } catch (Exception e3) {
                                                        DcTracker.this.log("WLAN+ Exception:" + e3.toString());
                                                    }
                                                }
                                                DcTracker.mMeasureDCCallback = new NetworkCallback();
                                                DcTracker.this.mCm.requestNetwork(request, DcTracker.mMeasureDCCallback);
                                                if (DcTracker.this.mCm.measureDataState(DcTracker.this.mPhone.getServiceStateTracker().getSignalLevel())) {
                                                    return;
                                                }
                                            }
                                            try {
                                                Thread.sleep((long) (DcTracker.this.waitToRetry[i] * 1000));
                                                Boolean isConnected = Boolean.valueOf(false);
                                                ApnContext apnContext = (ApnContext) DcTracker.this.mApnContextsById.get(0);
                                                if (apnContext != null && apnContext.getState() == State.CONNECTED) {
                                                    isConnected = Boolean.valueOf(true);
                                                }
                                                if (isConnected.booleanValue() || (DcTracker.mMeasureDataState ^ 1) != 0) {
                                                    DcTracker.this.log("WLAN+ WIFI_SCORE_CHANG retry ignore: mMeasureDataState=" + DcTracker.mMeasureDataState + " conntected:" + DcTracker.this.isConnected());
                                                    return;
                                                }
                                                i++;
                                            } catch (Exception e32) {
                                                DcTracker.this.log("WLAN+ " + e32.toString());
                                            }
                                        } else {
                                            DcTracker.this.log("WLAN+ WIFI_SCORE_CHANGE myMeasureDataState is false. ignore!");
                                            return;
                                        }
                                    }
                                } else if (DcTracker.mMeasureDCCallback != null) {
                                    DcTracker.this.log("WLAN+ WIFI_SCORE_CHANGE release DC: mMeasureDataState=" + DcTracker.mMeasureDataState);
                                    try {
                                        DcTracker.this.mCm.unregisterNetworkCallback(DcTracker.mMeasureDCCallback);
                                    } catch (IllegalArgumentException e22) {
                                        DcTracker.this.log("WLAN+ " + e22.toString());
                                    } catch (Exception e322) {
                                        DcTracker.this.log("WLAN+ Exception:" + e322.toString());
                                    }
                                    DcTracker.mMeasureDCCallback = null;
                                }
                            }
                        }.start();
                    }
                } else if (!action.equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                    DcTracker.this.log("onReceive: Unknown action=" + action);
                } else if (DcTracker.this.mIccRecords.get() != null && ((IccRecords) DcTracker.this.mIccRecords.get()).getRecordsLoaded()) {
                    DcTracker.this.setDefaultDataRoamingEnabled();
                }
            }
        };
        this.mPollNetStat = new Runnable() {
            public void run() {
                DcTracker.this.updateDataActivity();
                if (DcTracker.this.mIsScreenOn) {
                    DcTracker.this.mNetStatPollPeriod = Global.getInt(DcTracker.this.mResolver, "pdp_watchdog_poll_interval_ms", 1000);
                } else {
                    DcTracker.this.mNetStatPollPeriod = Global.getInt(DcTracker.this.mResolver, "pdp_watchdog_long_poll_interval_ms", 600000);
                }
                if (DcTracker.this.mNetStatPollEnabled) {
                    DcTracker.this.mDataConnectionTracker.postDelayed(this, (long) DcTracker.this.mNetStatPollPeriod);
                }
            }
        };
        this.mOnSubscriptionsChangedListener = new OnSubscriptionsChangedListener() {
            public final AtomicInteger mPreviousSubId = new AtomicInteger(-1);

            public void onSubscriptionsChanged() {
                DcTracker.this.log("SubscriptionListener.onSubscriptionInfoChanged");
                int subId = DcTracker.this.mPhone.getSubId();
                if (DcTracker.this.mSubscriptionManager.isActiveSubId(subId)) {
                    DcTracker.this.registerSettingsObserver();
                }
                if (this.mPreviousSubId.getAndSet(subId) != subId && DcTracker.this.mSubscriptionManager.isActiveSubId(subId)) {
                    DcTracker.this.onRecordsLoadedOrSubIdChanged();
                }
            }
        };
        this.mDisconnectAllCompleteMsgList = new ArrayList();
        this.mAllDataDisconnectedRegistrants = new RegistrantList();
        this.mIccRecords = new AtomicReference();
        this.mActivity = Activity.NONE;
        this.mState = State.IDLE;
        this.mNetStatPollEnabled = false;
        this.mDataStallTxRxSum = new TxRxSum(0, 0);
        this.mDataStallAlarmTag = (int) SystemClock.elapsedRealtime();
        this.mDataStallAlarmIntent = null;
        this.mNoRecvPollCount = 0;
        this.mDataStallDetectionEnabled = true;
        this.mFailFast = false;
        this.mInVoiceCall = false;
        this.mIsWifiConnected = false;
        this.mReconnectIntent = null;
        this.mAutoAttachOnCreationConfig = false;
        this.mAutoAttachOnCreation = new AtomicBoolean(false);
        this.mIsScreenOn = true;
        this.mMvnoMatched = false;
        this.mUniqueIdGenerator = new AtomicInteger(0);
        this.mDataConnections = new HashMap();
        this.mDataConnectionAcHashMap = new HashMap();
        this.mApnToDataConnectionId = new HashMap();
        this.mApnContexts = new ConcurrentHashMap();
        this.mApnContextsById = new SparseArray();
        this.mDisconnectPendingCount = 0;
        this.mMeteredApnDisabled = false;
        this.mSetDataProfileStatus = 0;
        this.mReregisterOnReconnectFailure = false;
        this.mCanSetPreferApn = false;
        this.mAttached = new AtomicBoolean(false);
        this.mImsRegistrationState = false;
        this.mPhone = phone;
        log("DCT.constructor");
        this.mResolver = this.mPhone.getContext().getContentResolver();
        this.mUiccController = UiccController.getInstance();
        this.mUiccController.registerForIccChanged(this, 270369, null);
        this.mAlarmManager = (AlarmManager) this.mPhone.getContext().getSystemService("alarm");
        this.mCm = (ConnectivityManager) this.mPhone.getContext().getSystemService("connectivity");
        IntentFilter filter = new IntentFilter();
        if (OemConstant.getWlanAssistantEnable(this.mPhone.getContext())) {
            filter.addAction(WIFI_SCORE_CHANGE);
        }
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction(INTENT_DATA_STALL_ALARM);
        filter.addAction(INTENT_PROVISIONING_APN_ALARM);
        filter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        this.mDataEnabledSettings.setUserDataEnabled(getDataEnabled());
        this.mPhone.getContext().registerReceiver(this.mIntentReceiver, filter, null, this.mPhone);
        this.mAutoAttachOnCreation.set(PreferenceManager.getDefaultSharedPreferences(this.mPhone.getContext()).getBoolean(Phone.DATA_DISABLED_ON_BOOT_KEY, false));
        this.mSubscriptionManager = SubscriptionManager.from(this.mPhone.getContext());
        this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        HandlerThread dcHandlerThread = new HandlerThread("DcHandlerThread");
        dcHandlerThread.start();
        Handler dcHandler = new Handler(dcHandlerThread.getLooper());
        this.mDcc = DcController.makeDcc(this.mPhone, this, dcHandler);
        this.mDcTesterFailBringUpAll = new DcTesterFailBringUpAll(this.mPhone, dcHandler);
        this.mDataConnectionTracker = this;
        registerForAllEvents();
        update();
        this.mApnObserver = new ApnChangeObserver();
        phone.getContext().getContentResolver().registerContentObserver(Carriers.CONTENT_URI, true, this.mApnObserver);
        initApnContexts();
        for (ApnContext apnContext : this.mApnContexts.values()) {
            filter = new IntentFilter();
            filter.addAction("com.android.internal.telephony.data-reconnect." + apnContext.getApnType());
            this.mPhone.getContext().registerReceiver(this.mIntentReceiver, filter, null, this.mPhone);
        }
        initEmergencyApnSetting();
        addEmergencyApnSetting();
        this.mProvisionActionName = "com.android.internal.telephony.PROVISION" + phone.getPhoneId();
        this.mSettingsObserver = new SettingsObserver(this.mPhone.getContext(), this);
        registerSettingsObserver();
        this.mAutoAttachOnCreationConfig = this.mPhone.getContext().getResources().getBoolean(17956893);
        mOppoCtaSupport = this.mPhone.getContext().getPackageManager().hasSystemFeature("oppo.cta.support");
        this.mDataStallDetectionEnabled = OemConstant.isNwLabTest() ^ 1;
        mVsimIgnoreUserDataSetting = OemConstant.isVsimIgnoreUserDataSetting(this.mPhone.getContext());
    }

    public DcTracker() {
        this.LOG_TAG = "DCT";
        this.isCleanupRequired = new AtomicBoolean(false);
        this.mRequestedApnType = "default";
        this.mDataEnabledSettings = new DataEnabledSettings();
        this.RADIO_RESET_PROPERTY = "gsm.radioreset";
        this.mWifiOldState = DetailedState.IDLE;
        this.mWifiConnectTimeStamp = 0;
        this.LINGER_TIMER = 3000;
        this.RETRY_TIMES = 3;
        this.waitToRetry = new int[]{3, 30, 60};
        this.WAITTING_TIMEOUT = 60;
        this.mSetupDataCallFailureCount = 0;
        this.mHasDataConnConncted = true;
        this.mHasInboundData = true;
        this.mDataType = 0;
        this.mPrioritySortedApnContexts = new PriorityQueue(5, /* anonymous class already generated */);
        this.mAllApnSettings = null;
        this.mPreferredApn = null;
        this.mIsPsRestricted = false;
        this.mEmergencyApn = null;
        this.mIsDisposed = false;
        this.mIsProvisioning = false;
        this.mProvisioningUrl = null;
        this.mProvisioningApnAlarmIntent = null;
        this.mProvisioningApnAlarmTag = (int) SystemClock.elapsedRealtime();
        this.mReplyAc = new AsyncChannel();
        this.mDataRoamingLeakageLog = new LocalLog(50);
        this.mIntentReceiver = /* anonymous class already generated */;
        this.mPollNetStat = /* anonymous class already generated */;
        this.mOnSubscriptionsChangedListener = /* anonymous class already generated */;
        this.mDisconnectAllCompleteMsgList = new ArrayList();
        this.mAllDataDisconnectedRegistrants = new RegistrantList();
        this.mIccRecords = new AtomicReference();
        this.mActivity = Activity.NONE;
        this.mState = State.IDLE;
        this.mNetStatPollEnabled = false;
        this.mDataStallTxRxSum = new TxRxSum(0, 0);
        this.mDataStallAlarmTag = (int) SystemClock.elapsedRealtime();
        this.mDataStallAlarmIntent = null;
        this.mNoRecvPollCount = 0;
        this.mDataStallDetectionEnabled = true;
        this.mFailFast = false;
        this.mInVoiceCall = false;
        this.mIsWifiConnected = false;
        this.mReconnectIntent = null;
        this.mAutoAttachOnCreationConfig = false;
        this.mAutoAttachOnCreation = new AtomicBoolean(false);
        this.mIsScreenOn = true;
        this.mMvnoMatched = false;
        this.mUniqueIdGenerator = new AtomicInteger(0);
        this.mDataConnections = new HashMap();
        this.mDataConnectionAcHashMap = new HashMap();
        this.mApnToDataConnectionId = new HashMap();
        this.mApnContexts = new ConcurrentHashMap();
        this.mApnContextsById = new SparseArray();
        this.mDisconnectPendingCount = 0;
        this.mMeteredApnDisabled = false;
        this.mSetDataProfileStatus = 0;
        this.mReregisterOnReconnectFailure = false;
        this.mCanSetPreferApn = false;
        this.mAttached = new AtomicBoolean(false);
        this.mImsRegistrationState = false;
        this.mAlarmManager = null;
        this.mCm = null;
        this.mPhone = null;
        this.mUiccController = null;
        this.mDataConnectionTracker = null;
        this.mProvisionActionName = null;
        this.mSettingsObserver = new SettingsObserver(null, this);
    }

    public void registerServiceStateTrackerEvents() {
        this.mPhone.getServiceStateTracker().registerForDataConnectionAttached(this, 270352, null);
        this.mPhone.getServiceStateTracker().registerForDataConnectionDetached(this, 270345, null);
        this.mPhone.getServiceStateTracker().registerForDataRoamingOn(this, 270347, null);
        this.mPhone.getServiceStateTracker().registerForDataRoamingOff(this, 270348, null, true);
        this.mPhone.getServiceStateTracker().registerForPsRestrictedEnabled(this, 270358, null);
        this.mPhone.getServiceStateTracker().registerForPsRestrictedDisabled(this, 270359, null);
        this.mPhone.getServiceStateTracker().registerForDataRegStateOrRatChanged(this, 270377, null);
    }

    public void unregisterServiceStateTrackerEvents() {
        this.mPhone.getServiceStateTracker().unregisterForDataConnectionAttached(this);
        this.mPhone.getServiceStateTracker().unregisterForDataConnectionDetached(this);
        this.mPhone.getServiceStateTracker().unregisterForDataRoamingOn(this);
        this.mPhone.getServiceStateTracker().unregisterForDataRoamingOff(this);
        this.mPhone.getServiceStateTracker().unregisterForPsRestrictedEnabled(this);
        this.mPhone.getServiceStateTracker().unregisterForPsRestrictedDisabled(this);
        this.mPhone.getServiceStateTracker().unregisterForDataRegStateOrRatChanged(this);
    }

    private void registerForAllEvents() {
        this.mPhone.getDeviceStateMonitor().registerForOemScreenChanged(this, 270437, null);
        this.mPhone.mCi.registerForAvailable(this, 270337, null);
        this.mPhone.mCi.registerForOffOrNotAvailable(this, 270342, null);
        this.mPhone.mCi.registerForDataCallListChanged(this, 270340, null);
        this.mPhone.getCallTracker().registerForVoiceCallEnded(this, 270344, null);
        this.mPhone.getCallTracker().registerForVoiceCallStarted(this, 270343, null);
        registerServiceStateTrackerEvents();
        this.mPhone.mCi.registerForPcoData(this, 270381, null);
        this.mPhone.getCarrierActionAgent().registerForCarrierAction(0, this, 270382, null, false);
    }

    public void dispose() {
        log("DCT.dispose");
        if (this.mProvisionBroadcastReceiver != null) {
            this.mPhone.getContext().unregisterReceiver(this.mProvisionBroadcastReceiver);
            this.mProvisionBroadcastReceiver = null;
        }
        if (this.mProvisioningSpinner != null) {
            this.mProvisioningSpinner.dismiss();
            this.mProvisioningSpinner = null;
        }
        cleanUpAllConnections(true, null);
        for (DcAsyncChannel dcac : this.mDataConnectionAcHashMap.values()) {
            dcac.disconnect();
        }
        this.mDataConnectionAcHashMap.clear();
        this.mIsDisposed = true;
        this.mPhone.getContext().unregisterReceiver(this.mIntentReceiver);
        this.mUiccController.unregisterForIccChanged(this);
        this.mSettingsObserver.unobserve();
        this.mSubscriptionManager.removeOnSubscriptionsChangedListener(this.mOnSubscriptionsChangedListener);
        this.mDcc.dispose();
        this.mDcTesterFailBringUpAll.dispose();
        this.mPhone.getContext().getContentResolver().unregisterContentObserver(this.mApnObserver);
        this.mApnContexts.clear();
        this.mApnContextsById.clear();
        this.mPrioritySortedApnContexts.clear();
        unregisterForAllEvents();
        destroyDataConnections();
    }

    private void unregisterForAllEvents() {
        this.mPhone.getDeviceStateMonitor().unregisterOemScreenChanged(this);
        this.mPhone.mCi.unregisterForAvailable(this);
        this.mPhone.mCi.unregisterForOffOrNotAvailable(this);
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r != null) {
            r.unregisterForRecordsLoaded(this);
            this.mIccRecords.set(null);
        }
        this.mPhone.mCi.unregisterForDataCallListChanged(this);
        this.mPhone.getCallTracker().unregisterForVoiceCallEnded(this);
        this.mPhone.getCallTracker().unregisterForVoiceCallStarted(this);
        unregisterServiceStateTrackerEvents();
        this.mPhone.mCi.unregisterForPcoData(this);
        this.mPhone.getCarrierActionAgent().unregisterForCarrierAction(this, 0);
        if (this.mPhone.getImsPhone() != null && this.mPhone.getImsPhone().getCallTracker() != null) {
            log("unregister ims call state");
            this.mPhone.getImsPhone().getCallTracker().unregisterForVoiceCallEnded(this);
            this.mPhone.getImsPhone().getCallTracker().unregisterForVoiceCallStarted(this);
        }
    }

    private void onResetDone(AsyncResult ar) {
        log("EVENT_RESET_DONE");
        String str = null;
        if (ar.userObj instanceof String) {
            str = ar.userObj;
        }
        gotoIdleAndNotifyDataConnection(str);
    }

    public void setDataEnabled(boolean enable) {
        if (this.mPhone == null || !OemConstant.isPoliceVersion(this.mPhone) || (OemConstant.canSwitchByUser(this.mPhone) ^ 1) == 0 || enable == OemConstant.isDataAllow(this.mPhone)) {
            Message msg = obtainMessage(270366);
            msg.arg1 = enable ? 1 : 0;
            log("setDataEnabled: sendMessage: enable=" + enable);
            sendMessage(msg);
            return;
        }
        log("---data-enable-return---");
    }

    private void onSetUserDataEnabled(boolean enabled) {
        synchronized (this.mDataEnabledSettings) {
            if (this.mDataEnabledSettings.isUserDataEnabled() == enabled && enabled != getDataEnabled()) {
                this.mDataEnabledSettings.setUserDataEnabled(getDataEnabled());
                log("onSetUserDataEnabled mUserDataEnabled = " + this.mDataEnabledSettings.isUserDataEnabled());
            }
            if (this.mDataEnabledSettings.isUserDataEnabled() != enabled) {
                int i;
                this.mDataEnabledSettings.setUserDataEnabled(enabled);
                ContentResolver contentResolver = this.mResolver;
                String str = "mobile_data";
                if (enabled) {
                    i = 1;
                } else {
                    i = 0;
                }
                Global.putInt(contentResolver, str, i);
                for (int slotIdx = 0; slotIdx < TelephonyManager.getDefault().getPhoneCount(); slotIdx++) {
                    Phone phone = PhoneFactory.getPhone(slotIdx);
                    if (phone != null) {
                        contentResolver = this.mResolver;
                        str = "mobile_data" + phone.getSubId();
                        if (enabled) {
                            i = 1;
                        } else {
                            i = 0;
                        }
                        Global.putInt(contentResolver, str, i);
                    }
                }
                log("onSetUserDataEnabled enabled = " + enabled + " PhoneId = " + this.mPhone.getPhoneId());
                if (!getDataRoamingEnabled() && this.mPhone.getServiceState().getDataRoaming()) {
                    if (enabled) {
                        notifyOffApnsOfAvailability(PhoneInternalInterface.REASON_ROAMING_ON);
                    } else {
                        notifyOffApnsOfAvailability(PhoneInternalInterface.REASON_DATA_DISABLED);
                    }
                }
                if (enabled) {
                    reevaluateDataConnections();
                    onTrySetupData(PhoneInternalInterface.REASON_DATA_ENABLED);
                } else {
                    onCleanUpAllConnections(PhoneInternalInterface.REASON_DATA_SPECIFIC_DISABLED);
                }
                if (OemConstant.getWlanAssistantEnable(this.mPhone.getContext())) {
                    SubscriptionManager su = SubscriptionManager.from(this.mPhone.getContext());
                    boolean isDefaultDataPhone = this.mPhone.getSubId() == SubscriptionManager.getDefaultDataSubId();
                    if (isDefaultDataPhone) {
                        boolean myMeasureDataState;
                        boolean isRomming = this.mPhone.getServiceState().getRoaming();
                        log("WLAN+ CMD_SET_USER_DATA_ENABLE: mMeasureDataState=" + mMeasureDataState + " Roaming=" + isRomming + " DataEnabled=" + enabled + " isDefaultDataPhone=" + isDefaultDataPhone);
                        if (mMeasureDataState && this.mIsWifiConnected && (isRomming ^ 1) != 0) {
                            myMeasureDataState = enabled;
                        } else {
                            myMeasureDataState = false;
                        }
                        if (myMeasureDataState) {
                            new Thread() {
                                public void run() {
                                    if (!DcTracker.this.mCm.measureDataState(DcTracker.this.mPhone.getServiceStateTracker().getSignalLevel())) {
                                        NetworkRequest request = DcTracker.this.mCm.getCelluarNetworkRequest();
                                        if (request != null) {
                                            if (DcTracker.mMeasureDCCallback != null) {
                                                DcTracker.this.log("WLAN+ CMD_SET_USER_DATA_ENABLE release DC befor request: mMeasureDataState=" + DcTracker.mMeasureDataState);
                                                try {
                                                    DcTracker.this.mCm.unregisterNetworkCallback(DcTracker.mMeasureDCCallback);
                                                } catch (IllegalArgumentException e) {
                                                    DcTracker.this.log("WLAN+ " + e.toString());
                                                } catch (Exception e2) {
                                                    DcTracker.this.log("WLAN+ Exception:" + e2.toString());
                                                }
                                            }
                                            DcTracker.mMeasureDCCallback = new NetworkCallback();
                                            DcTracker.this.mCm.requestNetwork(request, DcTracker.mMeasureDCCallback);
                                            DcTracker.this.mCm.measureDataState(DcTracker.this.mPhone.getServiceStateTracker().getSignalLevel());
                                        }
                                    }
                                }
                            }.start();
                        }
                    }
                }
            }
        }
    }

    private void reevaluateDataConnections() {
        if (this.mDataEnabledSettings.isDataEnabled()) {
            for (ApnContext apnContext : this.mApnContexts.values()) {
                if (apnContext.isConnectedOrConnecting() && (apnContext.getApnType().equals("ims") ^ 1) != 0) {
                    DcAsyncChannel dcac = apnContext.getDcAc();
                    if (dcac != null) {
                        NetworkCapabilities netCaps = dcac.getNetworkCapabilitiesSync();
                        if (netCaps != null && (netCaps.hasCapability(13) ^ 1) != 0 && (netCaps.hasCapability(11) ^ 1) != 0) {
                            log("Tearing down restricted metered net:" + apnContext);
                            apnContext.setReason(PhoneInternalInterface.REASON_DATA_ENABLED);
                            cleanUpConnection(true, apnContext);
                        } else if (apnContext.getApnSetting().isMetered(this.mPhone) && netCaps != null && netCaps.hasCapability(11)) {
                            log("Tearing down unmetered net:" + apnContext);
                            apnContext.setReason(PhoneInternalInterface.REASON_DATA_ENABLED);
                            cleanUpConnection(true, apnContext);
                        }
                    }
                }
            }
        }
    }

    private void onDeviceProvisionedChange() {
        if (getDataEnabled()) {
            this.mDataEnabledSettings.setUserDataEnabled(true);
            reevaluateDataConnections();
            onTrySetupData(PhoneInternalInterface.REASON_DATA_ENABLED);
            return;
        }
        this.mDataEnabledSettings.setUserDataEnabled(false);
        onCleanUpAllConnections(PhoneInternalInterface.REASON_DATA_SPECIFIC_DISABLED);
    }

    public long getSubId() {
        return (long) this.mPhone.getSubId();
    }

    public Activity getActivity() {
        return this.mActivity;
    }

    private void setActivity(Activity activity) {
        log("setActivity = " + activity);
        this.mActivity = activity;
        this.mPhone.notifyDataActivity();
    }

    public void requestNetwork(NetworkRequest networkRequest, LocalLog log) {
        ApnContext apnContext = (ApnContext) this.mApnContextsById.get(ApnContext.apnIdForNetworkRequest(networkRequest));
        log.log("DcTracker.requestNetwork for " + networkRequest + " found " + apnContext);
        if (apnContext != null) {
            apnContext.requestNetwork(networkRequest, log);
        }
    }

    public void releaseNetwork(NetworkRequest networkRequest, LocalLog log) {
        ApnContext apnContext = (ApnContext) this.mApnContextsById.get(ApnContext.apnIdForNetworkRequest(networkRequest));
        log.log("DcTracker.releaseNetwork for " + networkRequest + " found " + apnContext);
        if (apnContext != null) {
            apnContext.releaseNetwork(networkRequest, log);
        }
    }

    public boolean isApnSupported(String name) {
        if (name == null) {
            loge("isApnSupported: name=null");
            return false;
        } else if (((ApnContext) this.mApnContexts.get(name)) != null) {
            return true;
        } else {
            loge("Request for unsupported mobile name: " + name);
            return false;
        }
    }

    public int getApnPriority(String name) {
        ApnContext apnContext = (ApnContext) this.mApnContexts.get(name);
        if (apnContext == null) {
            loge("Request for unsupported mobile name: " + name);
        }
        return apnContext.priority;
    }

    private void setRadio(boolean on) {
        try {
            Stub.asInterface(ServiceManager.checkService("phone")).setRadio(on);
        } catch (Exception e) {
        }
    }

    protected void finalize() {
        if (this.mPhone != null) {
            log("finalize");
        }
    }

    private ApnContext addApnContext(String type, NetworkConfig networkConfig) {
        ApnContext apnContext = new ApnContext(this.mPhone, type, this.LOG_TAG, networkConfig, this);
        this.mApnContexts.put(type, apnContext);
        this.mApnContextsById.put(ApnContext.apnIdForApnName(type), apnContext);
        this.mPrioritySortedApnContexts.add(apnContext);
        return apnContext;
    }

    private void initApnContexts() {
        log("initApnContexts: E");
        for (String networkConfigString : this.mPhone.getContext().getResources().getStringArray(17236060)) {
            ApnContext apnContext;
            NetworkConfig networkConfig = new NetworkConfig(networkConfigString);
            switch (networkConfig.type) {
                case 0:
                    apnContext = addApnContext("default", networkConfig);
                    break;
                case 2:
                    apnContext = addApnContext("mms", networkConfig);
                    break;
                case 3:
                    apnContext = addApnContext("supl", networkConfig);
                    break;
                case 4:
                    apnContext = addApnContext("dun", networkConfig);
                    break;
                case 5:
                    apnContext = addApnContext("hipri", networkConfig);
                    break;
                case 10:
                    apnContext = addApnContext("fota", networkConfig);
                    break;
                case 11:
                    apnContext = addApnContext("ims", networkConfig);
                    break;
                case 12:
                    apnContext = addApnContext("cbs", networkConfig);
                    break;
                case 14:
                    apnContext = addApnContext("ia", networkConfig);
                    break;
                case 15:
                    apnContext = addApnContext("emergency", networkConfig);
                    break;
                default:
                    log("initApnContexts: skipping unknown type=" + networkConfig.type);
                    continue;
            }
            log("initApnContexts: apnContext=" + apnContext);
        }
    }

    public LinkProperties getLinkProperties(String apnType) {
        ApnContext apnContext = (ApnContext) this.mApnContexts.get(apnType);
        if (apnContext != null) {
            DcAsyncChannel dcac = apnContext.getDcAc();
            if (dcac != null) {
                log("return link properites for " + apnType);
                return dcac.getLinkPropertiesSync();
            }
        }
        log("return new LinkProperties");
        return new LinkProperties();
    }

    public NetworkCapabilities getNetworkCapabilities(String apnType) {
        ApnContext apnContext = (ApnContext) this.mApnContexts.get(apnType);
        if (apnContext != null) {
            DcAsyncChannel dataConnectionAc = apnContext.getDcAc();
            if (dataConnectionAc != null) {
                log("get active pdp is not null, return NetworkCapabilities for " + apnType);
                return dataConnectionAc.getNetworkCapabilitiesSync();
            }
        }
        log("return new NetworkCapabilities");
        return new NetworkCapabilities();
    }

    public String[] getActiveApnTypes() {
        log("get all active apn types");
        ArrayList<String> result = new ArrayList();
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (this.mAttached.get() && apnContext.isReady()) {
                result.add(apnContext.getApnType());
            }
        }
        return (String[]) result.toArray(new String[0]);
    }

    public String getActiveApnString(String apnType) {
        ApnContext apnContext = (ApnContext) this.mApnContexts.get(apnType);
        if (apnContext != null) {
            ApnSetting apnSetting = apnContext.getApnSetting();
            if (apnSetting != null) {
                return apnSetting.apn;
            }
        }
        return null;
    }

    public State getState(String apnType) {
        ApnContext apnContext = (ApnContext) this.mApnContexts.get(apnType);
        if (apnContext != null) {
            return apnContext.getState();
        }
        return State.FAILED;
    }

    private boolean isProvisioningApn(String apnType) {
        ApnContext apnContext = (ApnContext) this.mApnContexts.get(apnType);
        if (apnContext != null) {
            return apnContext.isProvisioningApn();
        }
        return false;
    }

    public State getOverallState() {
        boolean isConnecting = false;
        boolean isFailed = true;
        boolean isAnyEnabled = false;
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (apnContext.isEnabled()) {
                isAnyEnabled = true;
                switch (m21xf0fbc33d()[apnContext.getState().ordinal()]) {
                    case 1:
                    case 3:
                        return State.CONNECTED;
                    case 2:
                    case 6:
                        isConnecting = true;
                        isFailed = false;
                        break;
                    case 5:
                    case 7:
                        isFailed = false;
                        break;
                    default:
                        isAnyEnabled = true;
                        break;
                }
            }
        }
        if (!isAnyEnabled) {
            return State.IDLE;
        }
        if (isConnecting) {
            return State.CONNECTING;
        }
        if (isFailed) {
            return State.FAILED;
        }
        return State.IDLE;
    }

    public boolean isDataEnabled() {
        return this.mDataEnabledSettings.isDataEnabled();
    }

    private void onDataConnectionDetached() {
        log("onDataConnectionDetached: stop polling and notify detached");
        stopNetStatPoll();
        stopDataStallAlarm();
        notifyDataConnection(PhoneInternalInterface.REASON_DATA_DETACHED);
        this.mAttached.set(false);
    }

    private void onDataConnectionAttached() {
        log("onDataConnectionAttached");
        this.mAttached.set(true);
        if (getOverallState() == State.CONNECTED) {
            log("onDataConnectionAttached: start polling notify attached");
            startNetStatPoll();
            startDataStallAlarm(false);
            notifyDataConnection(PhoneInternalInterface.REASON_DATA_ATTACHED);
        } else {
            notifyOffApnsOfAvailability(PhoneInternalInterface.REASON_DATA_ATTACHED);
        }
        if (!this.mAutoAttachOnCreationConfig) {
            this.mAutoAttachOnCreationConfig = this.mPhone.getContext().getResources().getBoolean(17956893);
            log("onDataConnectionAttached: mAutoAttachOnCreationConfig = " + this.mAutoAttachOnCreationConfig);
        }
        if (this.mAutoAttachOnCreationConfig) {
            this.mAutoAttachOnCreation.set(true);
        }
        setupDataOnConnectableApns(PhoneInternalInterface.REASON_DATA_ATTACHED);
    }

    protected boolean isNvSubscription() {
        if (CdmaSubscriptionSourceManager.getDefault(this.mPhone.getContext()) == 1) {
            return true;
        }
        return false;
    }

    protected boolean getAttachedStatus() {
        return this.mAttached.get();
    }

    public boolean isDataAllowed(DataConnectionReasons dataConnectionReasons) {
        return isDataAllowed(null, dataConnectionReasons);
    }

    boolean isDataAllowed(ApnContext apnContext, DataConnectionReasons dataConnectionReasons) {
        boolean isMeteredApnType;
        try {
            if ((OemConstant.isPoliceVersion(this.mPhone) || OemConstant.isDeviceLockVersion()) && !OemConstant.isDataAllow(this.mPhone)) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        DataConnectionReasons reasons = new DataConnectionReasons();
        boolean internalDataEnabled = this.mDataEnabledSettings.isInternalDataEnabled();
        boolean attachedState = getAttachedStatus();
        boolean desiredPowerState = this.mPhone.getServiceStateTracker().getDesiredPowerState();
        boolean radioStateFromCarrier = this.mPhone.getServiceStateTracker().getPowerStateFromCarrier();
        int radioTech = this.mPhone.getServiceState().getRilDataRadioTechnology();
        if (radioTech == 18) {
            desiredPowerState = true;
            radioStateFromCarrier = true;
        }
        boolean recordsLoaded = this.mIccRecords.get() != null ? ((IccRecords) this.mIccRecords.get()).getRecordsLoaded() : false;
        boolean defaultDataSelected = SubscriptionManager.isValidSubscriptionId(SubscriptionManager.getDefaultDataSubscriptionId());
        if (apnContext != null) {
            isMeteredApnType = ApnSetting.isMeteredApnType(apnContext.getApnType(), this.mPhone);
        } else {
            isMeteredApnType = true;
        }
        PhoneConstants.State phoneState = PhoneConstants.State.IDLE;
        if (this.mPhone.getCallTracker() != null) {
            phoneState = this.mPhone.getCallTracker().getState();
        }
        if (apnContext != null && apnContext.getApnType().equals("emergency") && apnContext.isConnectable()) {
            if (dataConnectionReasons != null) {
                dataConnectionReasons.add(DataAllowedReasonType.EMERGENCY_APN);
            }
            return true;
        }
        if (!(apnContext == null || (apnContext.isConnectable() ^ 1) == 0)) {
            reasons.add(DataDisallowedReasonType.APN_NOT_CONNECTABLE);
        }
        if (apnContext != null && ((apnContext.getApnType().equals("default") || apnContext.getApnType().equals("ia")) && radioTech == 18)) {
            reasons.add(DataDisallowedReasonType.ON_IWLAN);
        }
        if (isEmergency()) {
            reasons.add(DataDisallowedReasonType.IN_ECBM);
        }
        if (!(!attachedState ? this.mAutoAttachOnCreation.get() : true)) {
            reasons.add(DataDisallowedReasonType.NOT_ATTACHED);
        }
        if (!recordsLoaded) {
            reasons.add(DataDisallowedReasonType.RECORD_NOT_LOADED);
        }
        if (!(phoneState == PhoneConstants.State.IDLE || (this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed() ^ 1) == 0)) {
            reasons.add(DataDisallowedReasonType.INVALID_PHONE_STATE);
            reasons.add(DataDisallowedReasonType.CONCURRENT_VOICE_DATA_NOT_ALLOWED);
        }
        if (!internalDataEnabled) {
            reasons.add(DataDisallowedReasonType.INTERNAL_DATA_DISABLED);
        }
        if (!defaultDataSelected) {
            reasons.add(DataDisallowedReasonType.DEFAULT_DATA_UNSELECTED);
        }
        if (this.mPhone.getServiceState().getDataRoaming() && (getDataRoamingEnabled() ^ 1) != 0) {
            reasons.add(DataDisallowedReasonType.ROAMING_DISABLED);
        }
        if (this.mIsPsRestricted) {
            reasons.add(DataDisallowedReasonType.PS_RESTRICTED);
        }
        if (!desiredPowerState) {
            reasons.add(DataDisallowedReasonType.UNDESIRED_POWER_STATE);
        }
        if (!radioStateFromCarrier) {
            reasons.add(DataDisallowedReasonType.RADIO_DISABLED_BY_CARRIER);
        }
        if (!this.mDataEnabledSettings.isDataEnabled()) {
            boolean isMms = false;
            if (!(getDataEnabled() || !this.mDataEnabledSettings.isInternalDataEnabled() || apnContext == null || apnContext.getApnType() == null || !apnContext.getApnType().equals("mms"))) {
                Rlog.d(NotificationChannelController.CHANNEL_ID_SMS, "isMeteredApnType=" + isMeteredApnType);
                isMms = true;
            }
            if (!isMms) {
                reasons.add(DataDisallowedReasonType.DATA_DISABLED);
            }
        }
        if (reasons.containsHardDisallowedReasons()) {
            if (dataConnectionReasons != null) {
                dataConnectionReasons.copyFrom(reasons);
            }
            return false;
        }
        if (!(isMeteredApnType || (reasons.allowed() ^ 1) == 0)) {
            reasons.add(DataAllowedReasonType.UNMETERED_APN);
        }
        if (!(apnContext == null || (apnContext.hasNoRestrictedRequests(true) ^ 1) == 0 || (reasons.allowed() ^ 1) == 0)) {
            reasons.add(DataAllowedReasonType.RESTRICTED_REQUEST);
        }
        if (haveVsimIgnoreUserDataSetting() && (reasons.allowed() ^ 1) != 0) {
            reasons.add(DataAllowedReasonType.NORMAL);
        }
        if (reasons.allowed()) {
            reasons.add(DataAllowedReasonType.NORMAL);
        }
        if (dataConnectionReasons != null) {
            dataConnectionReasons.copyFrom(reasons);
        }
        return reasons.allowed();
    }

    protected void setupDataOnConnectableApns(String reason) {
        setupDataOnConnectableApns(reason, RetryFailures.ALWAYS);
    }

    private void setupDataOnConnectableApns(String reason, RetryFailures retryFailures) {
        StringBuilder sb = new StringBuilder(120);
        for (ApnContext apnContext : this.mPrioritySortedApnContexts) {
            sb.append(apnContext.getApnType());
            sb.append(":[state=");
            sb.append(apnContext.getState());
            sb.append(",enabled=");
            sb.append(apnContext.isEnabled());
            sb.append("] ");
        }
        log("setupDataOnConnectableApns: " + reason + " " + sb);
        for (ApnContext apnContext2 : this.mPrioritySortedApnContexts) {
            if (apnContext2.getState() == State.FAILED || apnContext2.getState() == State.SCANNING) {
                if (retryFailures == RetryFailures.ALWAYS) {
                    apnContext2.releaseDataConnection(reason);
                } else if (!apnContext2.isConcurrentVoiceAndDataAllowed() && this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed()) {
                    apnContext2.releaseDataConnection(reason);
                }
            }
            if (apnContext2.isConnectable()) {
                log("isConnectable() call trySetupData");
                String apnType = apnContext2.getApnType();
                if (OemConstant.getWlanAssistantEnable(this.mPhone.getContext()) && mDelayMeasure && mMeasureDataState && this.mIsWifiConnected && "default".equals(apnType)) {
                    log("setupDataOnConnectableApns: " + reason + "ignore! block for WLAN+");
                } else {
                    apnContext2.setReason(reason);
                    trySetupData(apnContext2);
                }
            }
        }
    }

    boolean isEmergency() {
        boolean result = !this.mPhone.isInEcm() ? this.mPhone.isInEmergencyCall() : true;
        log("isEmergency: result=" + result);
        return result;
    }

    private boolean trySetupData(ApnContext apnContext) {
        if ("default".equals(apnContext.getApnType()) && this.mPreferredApn == null) {
            if (needManualSelectAPN(getOperatorNumeric())) {
                log("trySetupData: mPreferredApn == null, need Manual Select APN from UI, can not set up data!");
                return false;
            }
        }
        if (this.mPhone.getSimulatedRadioControl() != null) {
            apnContext.setState(State.CONNECTED);
            this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
            log("trySetupData: X We're on the simulator; assuming connected retValue=true");
            return true;
        }
        DataConnectionReasons dataConnectionReasons = new DataConnectionReasons();
        boolean isDataAllowed = isDataAllowed(apnContext, dataConnectionReasons);
        String logStr = "trySetupData for APN type " + apnContext.getApnType() + ", reason: " + apnContext.getReason() + ". " + dataConnectionReasons.toString();
        log(logStr);
        apnContext.requestLog(logStr);
        synchronized (this.mDataEnabledSettings) {
            if (this.mDataEnabledSettings.isUserDataEnabled() != getDataEnabled()) {
                log("trySetupData before mUserDataEnabled = " + this.mDataEnabledSettings.isUserDataEnabled());
                this.mDataEnabledSettings.setUserDataEnabled(getDataEnabled());
                log("trySetupData after mUserDataEnabled = " + this.mDataEnabledSettings.isUserDataEnabled());
            }
        }
        if (isDataAllowed) {
            String str;
            if (apnContext.getState() == State.FAILED) {
                str = "trySetupData: make a FAILED ApnContext IDLE so its reusable";
                log(str);
                apnContext.requestLog(str);
                apnContext.setState(State.IDLE);
            }
            int radioTech = this.mPhone.getServiceState().getRilDataRadioTechnology();
            apnContext.setConcurrentVoiceAndDataAllowed(this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed());
            if (apnContext.getState() == State.IDLE) {
                ArrayList<ApnSetting> waitingApns = buildWaitingApns(apnContext.getApnType(), radioTech);
                if (waitingApns.isEmpty()) {
                    notifyNoData(DcFailCause.MISSING_UNKNOWN_APN, apnContext);
                    notifyOffApnsOfAvailability(apnContext.getReason());
                    str = "trySetupData: X No APN found retValue=false";
                    log(str);
                    apnContext.requestLog(str);
                    int log_type = -1;
                    String log_desc = SpnOverride.MVNO_TYPE_NONE;
                    try {
                        String[] log_array = this.mPhone.getContext().getString(this.mPhone.getContext().getResources().getIdentifier("zz_oppo_critical_log_111", "string", "android")).split(",");
                        log_type = Integer.valueOf(log_array[0]).intValue();
                        log_desc = log_array[1];
                    } catch (Exception e) {
                    }
                    OppoManager.writeLogToPartition(log_type, getCellLocation() + ", trySetupData: X No APN found", "NETWORK", RIL.ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_NO_AVAILABLE_APN, log_desc);
                    return false;
                }
                apnContext.setWaitingApns(waitingApns);
                log("trySetupData: Create from mAllApnSettings : " + apnListToString(this.mAllApnSettings));
            }
            boolean retValue = setupData(apnContext, radioTech, dataConnectionReasons.contains(DataAllowedReasonType.UNMETERED_APN));
            notifyOffApnsOfAvailability(apnContext.getReason());
            log("trySetupData: X retValue=" + retValue);
            return retValue;
        }
        if (!apnContext.getApnType().equals("default") && apnContext.isConnectable()) {
            this.mPhone.notifyDataConnectionFailed(apnContext.getReason(), apnContext.getApnType());
        }
        notifyOffApnsOfAvailability(apnContext.getReason());
        StringBuilder str2 = new StringBuilder();
        str2.append("trySetupData failed. apnContext = [type=").append(apnContext.getApnType()).append(", mState=").append(apnContext.getState()).append(", apnEnabled=").append(apnContext.isEnabled()).append(", mDependencyMet=").append(apnContext.getDependencyMet()).append("] ");
        if (!this.mDataEnabledSettings.isDataEnabled()) {
            str2.append("isDataEnabled() = false. ").append(this.mDataEnabledSettings);
        }
        if (apnContext.getState() == State.SCANNING) {
            apnContext.setState(State.FAILED);
            str2.append(" Stop retrying.");
        }
        log(str2.toString());
        apnContext.requestLog(str2.toString());
        return false;
    }

    protected void notifyOffApnsOfAvailability(String reason) {
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (!this.mAttached.get() || (apnContext.isReady() ^ 1) != 0) {
                String str;
                Phone phone = this.mPhone;
                if (reason != null) {
                    str = reason;
                } else {
                    str = apnContext.getReason();
                }
                phone.notifyDataConnection(str, apnContext.getApnType(), DataState.DISCONNECTED);
            } else if (apnContext != null && apnContext.getApnType() == "default" && PhoneInternalInterface.REASON_ROAMING_ON.equals(reason) && (getDataRoamingEnabled() ^ 1) != 0) {
                this.mPhone.notifyDataConnection(PhoneInternalInterface.REASON_ROAMING_ON, "default", DataState.DISCONNECTED);
                log("else notifyOffApnOfAvailability: notify only for type default");
            }
        }
    }

    protected boolean cleanUpAllConnections(boolean tearDown, String reason) {
        log("cleanUpAllConnections: tearDown=" + tearDown + " reason=" + reason);
        boolean didDisconnect = false;
        boolean disableMeteredOnly = false;
        if (!TextUtils.isEmpty(reason)) {
            if (reason.equals(PhoneInternalInterface.REASON_DATA_SPECIFIC_DISABLED) || reason.equals(PhoneInternalInterface.REASON_ROAMING_ON) || reason.equals(PhoneInternalInterface.REASON_CARRIER_ACTION_DISABLE_METERED_APN) || reason.equals(PhoneInternalInterface.REASON_SINGLE_PDN_ARBITRATION)) {
                disableMeteredOnly = true;
            } else {
                disableMeteredOnly = reason.equals(PhoneInternalInterface.REASON_PDP_RESET);
            }
        }
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (!disableMeteredOnly) {
                if (!apnContext.isDisconnected()) {
                    didDisconnect = true;
                }
                apnContext.setReason(reason);
                cleanUpConnection(tearDown, apnContext);
            } else if (!apnContext.getApnType().equals("ims")) {
                ApnSetting apnSetting = apnContext.getApnSetting();
                if (apnSetting != null && apnSetting.isMetered(this.mPhone)) {
                    if (!apnContext.isDisconnected()) {
                        didDisconnect = true;
                    }
                    log("clean up metered ApnContext Type: " + apnContext.getApnType());
                    apnContext.setReason(reason);
                    cleanUpConnection(tearDown, apnContext);
                }
            }
        }
        stopNetStatPoll();
        stopDataStallAlarm();
        this.mRequestedApnType = "default";
        log("cleanUpConnection: mDisconnectPendingCount = " + this.mDisconnectPendingCount);
        if (tearDown && this.mDisconnectPendingCount == 0) {
            notifyDataDisconnectComplete();
            notifyAllDataDisconnected();
        }
        return didDisconnect;
    }

    private void onCleanUpAllConnections(String cause) {
        cleanUpAllConnections(true, cause);
    }

    void sendCleanUpConnection(boolean tearDown, ApnContext apnContext) {
        int i;
        log("sendCleanUpConnection: tearDown=" + tearDown + " apnContext=" + apnContext);
        Message msg = obtainMessage(270360);
        if (tearDown) {
            i = 1;
        } else {
            i = 0;
        }
        msg.arg1 = i;
        msg.arg2 = 0;
        msg.obj = apnContext;
        sendMessage(msg);
    }

    protected void cleanUpConnection(boolean tearDown, ApnContext apnContext) {
        if (apnContext == null) {
            log("cleanUpConnection: apn context is null");
            return;
        }
        String str;
        DcAsyncChannel dcac = apnContext.getDcAc();
        apnContext.requestLog("cleanUpConnection: tearDown=" + tearDown + " reason=" + apnContext.getReason());
        if (!tearDown) {
            if (dcac != null) {
                dcac.reqReset();
            }
            apnContext.setState(State.IDLE);
            this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
            apnContext.setDataConnectionAc(null);
        } else if (apnContext.isDisconnected()) {
            apnContext.setState(State.IDLE);
            if (!apnContext.isReady()) {
                if (dcac != null) {
                    str = "cleanUpConnection: teardown, disconnected, !ready";
                    log(str + " apnContext=" + apnContext);
                    apnContext.requestLog(str);
                    dcac.tearDown(apnContext, SpnOverride.MVNO_TYPE_NONE, null);
                }
                apnContext.setDataConnectionAc(null);
            }
        } else if (dcac == null) {
            apnContext.setState(State.IDLE);
            apnContext.requestLog("cleanUpConnection: connected, bug no DCAC");
            this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
        } else if (apnContext.getState() != State.DISCONNECTING) {
            boolean disconnectAll = false;
            if ("dun".equals(apnContext.getApnType()) && teardownForDun()) {
                log("cleanUpConnection: disconnectAll DUN connection");
                disconnectAll = true;
            }
            int generation = apnContext.getConnectionGeneration();
            str = "cleanUpConnection: tearing down" + (disconnectAll ? " all" : SpnOverride.MVNO_TYPE_NONE) + " using gen#" + generation;
            log(str + "apnContext=" + apnContext);
            apnContext.requestLog(str);
            Message msg = obtainMessage(270351, new Pair(apnContext, Integer.valueOf(generation)));
            if (disconnectAll) {
                apnContext.getDcAc().tearDownAll(apnContext.getReason(), msg);
            } else {
                apnContext.getDcAc().tearDown(apnContext, apnContext.getReason(), msg);
            }
            apnContext.setState(State.DISCONNECTING);
            this.mDisconnectPendingCount++;
        }
        if (dcac != null) {
            cancelReconnectAlarm(apnContext);
        }
        str = "cleanUpConnection: X tearDown=" + tearDown + " reason=" + apnContext.getReason();
        log(str + " apnContext=" + apnContext + " dcac=" + apnContext.getDcAc());
        apnContext.requestLog(str);
    }

    public ApnSetting fetchDunApn() {
        if (SystemProperties.getBoolean("net.tethering.noprovisioning", false)) {
            log("fetchDunApn: net.tethering.noprovisioning=true ret: null");
            return null;
        }
        ApnSetting dunApn = getDunApnFromCache();
        if (dunApn != null) {
            log("fetchDunApn: dunApn:" + dunApn);
            return dunApn;
        }
        ApnSetting apn;
        int bearer = this.mPhone.getServiceState().getRilDataRadioTechnology();
        IccRecords r = (IccRecords) this.mIccRecords.get();
        String operator = getOperatorNumeric();
        ArrayList<ApnSetting> dunCandidates = new ArrayList();
        ApnSetting retDunSetting = null;
        String apnData = Global.getString(this.mResolver, "tether_dun_apn");
        if (!TextUtils.isEmpty(apnData)) {
            dunCandidates.addAll(ApnSetting.arrayFromString(apnData));
        }
        if (dunCandidates.isEmpty()) {
            String[] apnArrayData = getDunApnByMccMnc(this.mPhone.getContext());
            if (!ArrayUtils.isEmpty(apnArrayData)) {
                for (String apnString : apnArrayData) {
                    apn = ApnSetting.fromString(apnString);
                    if (apn != null) {
                        dunCandidates.add(apn);
                    }
                }
            }
        }
        if (dunCandidates.isEmpty() && !ArrayUtils.isEmpty(this.mAllApnSettings)) {
            for (ApnSetting apn2 : this.mAllApnSettings) {
                if (apn2.canHandleType("dun") && (apn2.canHandleType("default") ^ 1) != 0) {
                    dunCandidates.add(apn2);
                }
            }
        }
        for (ApnSetting dunSetting : dunCandidates) {
            if (ServiceState.bitmaskHasTech(dunSetting.bearerBitmask, bearer) && dunSetting.numeric.equals(operator)) {
                if (dunSetting.hasMvnoParams()) {
                    if (r != null && ApnSetting.mvnoMatches(r, dunSetting.mvnoType, dunSetting.mvnoMatchData)) {
                        retDunSetting = dunSetting;
                        break;
                    }
                } else if (!this.mMvnoMatched) {
                    retDunSetting = dunSetting;
                    break;
                }
            }
        }
        return retDunSetting;
    }

    public boolean hasMatchedTetherApnSetting() {
        ApnSetting matched = fetchDunApn();
        log("hasMatchedTetherApnSetting: APN=" + matched);
        return matched != null;
    }

    private boolean teardownForDun() {
        boolean z = true;
        if (ServiceState.isCdma(this.mPhone.getServiceState().getRilDataRadioTechnology())) {
            return true;
        }
        if (fetchDunApn() == null) {
            z = false;
        }
        return z;
    }

    private void cancelReconnectAlarm(ApnContext apnContext) {
        if (apnContext != null) {
            PendingIntent intent = apnContext.getReconnectIntent();
            if (intent != null) {
                ((AlarmManager) this.mPhone.getContext().getSystemService("alarm")).cancel(intent);
                apnContext.setReconnectIntent(null);
            }
        }
    }

    private String[] parseTypes(String types) {
        if (types != null && !types.equals(SpnOverride.MVNO_TYPE_NONE)) {
            return types.split(",");
        }
        return new String[]{CharacterSets.MIMENAME_ANY_CHARSET};
    }

    boolean isPermanentFailure(DcFailCause dcFailCause) {
        if (dcFailCause.isPermanentFailure(this.mPhone.getContext(), this.mPhone.getSubId())) {
            return (this.mAttached.get() && dcFailCause == DcFailCause.SIGNAL_LOST) ? false : true;
        } else {
            return false;
        }
    }

    private ApnSetting makeApnSetting(Cursor cursor) {
        return new ApnSetting(cursor.getInt(cursor.getColumnIndexOrThrow("_id")), cursor.getString(cursor.getColumnIndexOrThrow("numeric")), cursor.getString(cursor.getColumnIndexOrThrow(Calls.CACHED_NAME)), cursor.getString(cursor.getColumnIndexOrThrow("apn")), NetworkUtils.trimV4AddrZeros(cursor.getString(cursor.getColumnIndexOrThrow("proxy"))), cursor.getString(cursor.getColumnIndexOrThrow("port")), NetworkUtils.trimV4AddrZeros(cursor.getString(cursor.getColumnIndexOrThrow("mmsc"))), NetworkUtils.trimV4AddrZeros(cursor.getString(cursor.getColumnIndexOrThrow("mmsproxy"))), cursor.getString(cursor.getColumnIndexOrThrow("mmsport")), cursor.getString(cursor.getColumnIndexOrThrow("user")), cursor.getString(cursor.getColumnIndexOrThrow("password")), cursor.getInt(cursor.getColumnIndexOrThrow("authtype")), parseTypes(cursor.getString(cursor.getColumnIndexOrThrow(Calls.TYPE))), cursor.getString(cursor.getColumnIndexOrThrow("protocol")), cursor.getString(cursor.getColumnIndexOrThrow("roaming_protocol")), cursor.getInt(cursor.getColumnIndexOrThrow("carrier_enabled")) == 1, cursor.getInt(cursor.getColumnIndexOrThrow("bearer")), cursor.getInt(cursor.getColumnIndexOrThrow("bearer_bitmask")), cursor.getInt(cursor.getColumnIndexOrThrow("profile_id")), cursor.getInt(cursor.getColumnIndexOrThrow("modem_cognitive")) == 1, cursor.getInt(cursor.getColumnIndexOrThrow("max_conns")), cursor.getInt(cursor.getColumnIndexOrThrow("wait_time")), cursor.getInt(cursor.getColumnIndexOrThrow("max_conns_time")), cursor.getInt(cursor.getColumnIndexOrThrow("mtu")), cursor.getString(cursor.getColumnIndexOrThrow("mvno_type")), cursor.getString(cursor.getColumnIndexOrThrow("mvno_match_data")));
    }

    protected ArrayList<ApnSetting> createApnList(Cursor cursor) {
        ApnSetting apn;
        ArrayList<ApnSetting> result;
        ArrayList<ApnSetting> mnoApns = new ArrayList();
        ArrayList<ApnSetting> mvnoApns = new ArrayList();
        IccRecords r = (IccRecords) this.mIccRecords.get();
        boolean hasMvnoImsApn = false;
        if (cursor.moveToFirst()) {
            do {
                apn = makeApnSetting(cursor);
                if (apn != null) {
                    if (!apn.hasMvnoParams()) {
                        mnoApns.add(apn);
                    } else if (r != null && ApnSetting.mvnoMatches(r, apn.mvnoType, apn.mvnoMatchData)) {
                        mvnoApns.add(apn);
                        if (ArrayUtils.contains(apn.types, "ims")) {
                            hasMvnoImsApn = true;
                        }
                    }
                }
            } while (cursor.moveToNext());
        }
        if (mvnoApns.isEmpty()) {
            result = mnoApns;
            this.mMvnoMatched = false;
        } else {
            result = mvnoApns;
            if (!hasMvnoImsApn) {
                for (ApnSetting apn2 : mnoApns) {
                    if (ArrayUtils.contains(apn2.types, "ims")) {
                        mvnoApns.add(apn2);
                    }
                }
            }
            this.mMvnoMatched = true;
        }
        log("createApnList: X result=" + result);
        return result;
    }

    private boolean dataConnectionNotInUse(DcAsyncChannel dcac) {
        log("dataConnectionNotInUse: check if dcac is inuse dcac=" + dcac);
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (apnContext.getDcAc() == dcac) {
                log("dataConnectionNotInUse: in use by apnContext=" + apnContext);
                return false;
            }
        }
        log("dataConnectionNotInUse: tearDownAll");
        dcac.tearDownAll("No connection", null);
        log("dataConnectionNotInUse: not in use return true");
        return true;
    }

    private DcAsyncChannel findFreeDataConnection() {
        for (DcAsyncChannel dcac : this.mDataConnectionAcHashMap.values()) {
            if (dcac.isInactiveSync() && dataConnectionNotInUse(dcac)) {
                log("findFreeDataConnection: found free DataConnection= dcac=" + dcac);
                return dcac;
            }
        }
        log("findFreeDataConnection: NO free DataConnection");
        return null;
    }

    private boolean setupData(ApnContext apnContext, int radioTech, boolean unmeteredUseOnly) {
        log("setupData: apnContext=" + apnContext);
        apnContext.requestLog("setupData");
        DcAsyncChannel dcac = null;
        ApnSetting apnSetting = apnContext.getNextApnSetting();
        if (apnSetting == null) {
            log("setupData: return for no apn found!");
            return false;
        }
        int profileId = apnSetting.profileId;
        if (profileId == 0) {
            profileId = getApnProfileID(apnContext.getApnType());
        }
        if (!(apnContext.getApnType() == "dun" && teardownForDun())) {
            dcac = checkForCompatibleConnectedApnContext(apnContext);
            if (dcac != null) {
                ApnSetting dcacApnSetting = dcac.getApnSettingSync();
                if (dcacApnSetting != null) {
                    apnSetting = dcacApnSetting;
                }
            }
        }
        if (dcac == null) {
            if (isOnlySingleDcAllowed(radioTech)) {
                if (isHigherPriorityApnContextActive(apnContext)) {
                    log("setupData: Higher priority ApnContext active.  Ignoring call");
                    return false;
                } else if (apnContext.getApnType().equals("ims") || !cleanUpAllConnections(true, PhoneInternalInterface.REASON_SINGLE_PDN_ARBITRATION)) {
                    log("setupData: Single pdp. Continue setting up data call.");
                } else {
                    log("setupData: Some calls are disconnecting first. Wait and retry");
                    return false;
                }
            }
            dcac = findFreeDataConnection();
            if (dcac == null) {
                dcac = createDataConnection();
            }
            if (dcac == null) {
                log("setupData: No free DataConnection and couldn't create one, WEIRD");
                return false;
            }
        }
        int generation = apnContext.incAndGetConnectionGeneration();
        log("setupData: dcac=" + dcac + " apnSetting=" + apnSetting + " gen#=" + generation);
        apnContext.setDataConnectionAc(dcac);
        apnContext.setApnSetting(apnSetting);
        apnContext.setState(State.CONNECTING);
        this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
        Message msg = obtainMessage();
        msg.what = 270336;
        msg.obj = new Pair(apnContext, Integer.valueOf(generation));
        dcac.bringUp(apnContext, profileId, radioTech, unmeteredUseOnly, msg, generation);
        log("setupData: initing!");
        return true;
    }

    protected void setInitialAttachApn() {
        int i = 1;
        ApnSetting iaApnSetting = null;
        ApnSetting defaultApnSetting = null;
        ApnSetting firstApnSetting = null;
        if (this.mPreferredApn == null && needManualSelectAPN(getOperatorNumeric())) {
            log("setInitialAttachApn: mPreferredApn == null, need Manual Select APN from UI, can not set up data!");
            return;
        }
        log("setInitialApn: E mPreferredApn=" + this.mPreferredApn);
        if (this.mPreferredApn != null && this.mPreferredApn.canHandleType("ia")) {
            iaApnSetting = this.mPreferredApn;
        } else if (this.mAllApnSettings != null && (this.mAllApnSettings.isEmpty() ^ 1) != 0) {
            firstApnSetting = (ApnSetting) this.mAllApnSettings.get(0);
            log("setInitialApn: firstApnSetting=" + firstApnSetting);
            for (ApnSetting apn : this.mAllApnSettings) {
                if (apn.canHandleType("ia")) {
                    log("setInitialApn: iaApnSetting=" + apn);
                    iaApnSetting = apn;
                    break;
                } else if (defaultApnSetting == null && apn.canHandleType("default")) {
                    log("setInitialApn: defaultApnSetting=" + apn);
                    defaultApnSetting = apn;
                }
            }
        }
        if (iaApnSetting == null && defaultApnSetting == null && (allowInitialAttachForOperator() ^ 1) != 0) {
            log("Abort Initial attach");
            return;
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
        } else if (firstApnSetting != null) {
            log("setInitialAttachApn: using firstApnSetting");
            initialAttachApnSetting = firstApnSetting;
            if (firstApnSetting.types.length == 1) {
                if (!"default".equals(initialAttachApnSetting.types[0])) {
                    i = "ia".equals(initialAttachApnSetting.types[0]);
                }
                if ((i ^ 1) != 0) {
                    if (SystemProperties.get("ro.oppo.operator", "ex").equals("TELSTRA")) {
                        log("setInitialAttachApn: Telstra operator don't set not default/IA APN as IA APN to avoid failed in IOT test");
                        return;
                    } else if (!"emergency".equals(initialAttachApnSetting.types[0])) {
                        log("setInitialAttachApn: don't set not default/emergency/IA APN as IA APN to avoid rejected by NW");
                        return;
                    }
                }
            }
        }
        if (initialAttachApnSetting == null) {
            log("setInitialAttachApn: X There in no available apn");
        } else {
            log("setInitialAttachApn: X selected Apn=" + initialAttachApnSetting);
            this.mPhone.mCi.setInitialAttachApn(new DataProfile(initialAttachApnSetting), this.mPhone.getServiceState().getDataRoamingFromRegistration(), null);
        }
    }

    protected boolean allowInitialAttachForOperator() {
        return true;
    }

    private void onApnChanged() {
        State overallState = getOverallState();
        boolean isDisconnected = overallState != State.IDLE ? overallState == State.FAILED : true;
        if (this.mPhone instanceof GsmCdmaPhone) {
            ((GsmCdmaPhone) this.mPhone).updateCurrentCarrierInProvider();
        }
        log("onApnChanged: createAllApnList and cleanUpAllConnections");
        ApnSetting oldPreferredApn = this.mPreferredApn;
        createAllApnList();
        ApnSetting newPreferredApn = getPreferredApn();
        log("oldPreferredApn == " + oldPreferredApn + " newPreferredApn == " + newPreferredApn + " overallState == " + overallState);
        if (isDisconnected || (isApnDifferent(newPreferredApn, oldPreferredApn) ^ 1) == 0) {
            setInitialAttachApn();
            cleanUpConnectionsOnUpdatedApns(isDisconnected ^ 1, PhoneInternalInterface.REASON_APN_CHANGED);
            if (this.mPhone.getSubId() == SubscriptionManager.getDefaultDataSubscriptionId()) {
                setupDataOnConnectableApns(PhoneInternalInterface.REASON_APN_CHANGED);
            }
            return;
        }
        log("preferred apn has no changed");
    }

    private boolean isApnDifferent(ApnSetting oldApn, ApnSetting newApn) {
        if (newApn == null || oldApn == null) {
            if (newApn == null && oldApn == null) {
                return false;
            }
            return true;
        } else if (oldApn.equals(newApn)) {
            return false;
        } else {
            return true;
        }
    }

    private DcAsyncChannel findDataConnectionAcByCid(int cid) {
        for (DcAsyncChannel dcac : this.mDataConnectionAcHashMap.values()) {
            if (dcac.getCidSync() == cid) {
                return dcac;
            }
        }
        return null;
    }

    private void gotoIdleAndNotifyDataConnection(String reason) {
        log("gotoIdleAndNotifyDataConnection: reason=" + reason);
        notifyDataConnection(reason);
    }

    private boolean isHigherPriorityApnContextActive(ApnContext apnContext) {
        if (apnContext.getApnType().equals("ims")) {
            return false;
        }
        for (ApnContext otherContext : this.mPrioritySortedApnContexts) {
            if (!otherContext.getApnType().equals("ims")) {
                if (apnContext.getApnType().equalsIgnoreCase(otherContext.getApnType())) {
                    return false;
                }
                if (otherContext.isEnabled() && otherContext.getState() != State.FAILED) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isOnlySingleDcAllowed(int rilRadioTech) {
        int[] singleDcRats = null;
        CarrierConfigManager configManager = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        if (configManager != null) {
            PersistableBundle bundle = configManager.getConfig();
            if (bundle != null) {
                singleDcRats = bundle.getIntArray("only_single_dc_allowed_int_array");
            }
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
        IccRecords r = (IccRecords) this.mIccRecords.get();
        String operator = r != null ? r.getOperatorNumeric() : SpnOverride.MVNO_TYPE_NONE;
        if (!(operator == null || !operator.equals("60202") || onlySingleDcAllowed)) {
            onlySingleDcAllowed = true;
            log("isOnlySingleDcAllowed EG vodafone not support Muti-PDN");
        }
        log("isOnlySingleDcAllowed(" + rilRadioTech + "): " + onlySingleDcAllowed);
        return onlySingleDcAllowed;
    }

    void sendRestartRadio() {
        log("sendRestartRadio:");
        sendMessage(obtainMessage(270362));
    }

    private void restartRadio() {
        log("restartRadio: ************TURN OFF RADIO**************");
        cleanUpAllConnections(true, PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
        this.mPhone.getServiceStateTracker().powerOffRadioSafely(this);
        SystemProperties.set("net.ppp.reset-by-timeout", String.valueOf(Integer.parseInt(SystemProperties.get("net.ppp.reset-by-timeout", "0")) + 1));
    }

    private boolean retryAfterDisconnected(ApnContext apnContext) {
        boolean retry = true;
        if (PhoneInternalInterface.REASON_RADIO_TURNED_OFF.equals(apnContext.getReason()) || (isOnlySingleDcAllowed(this.mPhone.getServiceState().getRilDataRadioTechnology()) && isHigherPriorityApnContextActive(apnContext))) {
            retry = false;
        }
        if ("default".equals(apnContext.getApnType()) && this.mIsWifiConnected && (mMeasureDataState ^ 1) != 0) {
            return false;
        }
        return retry;
    }

    private void startAlarmForReconnect(long delay, ApnContext apnContext) {
        String apnType = apnContext.getApnType();
        Intent intent = new Intent("com.android.internal.telephony.data-reconnect." + apnType);
        intent.addFlags(268435456);
        intent.putExtra(INTENT_RECONNECT_ALARM_EXTRA_REASON, apnContext.getReason());
        intent.putExtra(INTENT_RECONNECT_ALARM_EXTRA_TYPE, apnType);
        intent.addFlags(268435456);
        intent.putExtra("subscription", this.mPhone.getSubId());
        log("startAlarmForReconnect: delay=" + delay + " action=" + intent.getAction() + " apn=" + apnContext);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this.mPhone.getContext(), 0, intent, 134217728);
        apnContext.setReconnectIntent(alarmIntent);
        this.mAlarmManager.setExact(2, SystemClock.elapsedRealtime() + delay, alarmIntent);
    }

    private void notifyNoData(DcFailCause lastFailCauseCode, ApnContext apnContext) {
        log("notifyNoData: type=" + apnContext.getApnType());
        if (isPermanentFailure(lastFailCauseCode) && (apnContext.getApnType().equals("default") ^ 1) != 0) {
            this.mPhone.notifyDataConnectionFailed(apnContext.getReason(), apnContext.getApnType());
        }
    }

    public boolean getAutoAttachOnCreation() {
        return this.mAutoAttachOnCreation.get();
    }

    protected void onRecordsLoadedOrSubIdChanged() {
        log("onRecordsLoadedOrSubIdChanged: createAllApnList");
        this.mAutoAttachOnCreationConfig = this.mPhone.getContext().getResources().getBoolean(17956893);
        createAllApnList();
        setInitialAttachApn();
        if (this.mPhone.mCi.getRadioState().isOn()) {
            log("onRecordsLoadedOrSubIdChanged: notifying data availability");
            notifyOffApnsOfAvailability(PhoneInternalInterface.REASON_SIM_LOADED);
        }
        setupDataOnConnectableApns(PhoneInternalInterface.REASON_SIM_LOADED);
    }

    private void onSetCarrierDataEnabled(AsyncResult ar) {
        if (ar.exception != null) {
            Rlog.e(this.LOG_TAG, "CarrierDataEnable exception: " + ar.exception);
            return;
        }
        synchronized (this.mDataEnabledSettings) {
            boolean enabled = ((Boolean) ar.result).booleanValue();
            if (enabled != this.mDataEnabledSettings.isCarrierDataEnabled()) {
                log("carrier Action: set metered apns enabled: " + enabled);
                this.mDataEnabledSettings.setCarrierDataEnabled(enabled);
                if (enabled) {
                    this.mPhone.notifyOtaspChanged(this.mPhone.getServiceStateTracker().getOtasp());
                    reevaluateDataConnections();
                    setupDataOnConnectableApns(PhoneInternalInterface.REASON_DATA_ENABLED);
                } else {
                    this.mPhone.notifyOtaspChanged(5);
                    cleanUpAllConnections(true, PhoneInternalInterface.REASON_CARRIER_ACTION_DISABLE_METERED_APN);
                }
            }
        }
    }

    private void onSimNotReady() {
        log("onSimNotReady");
        cleanUpAllConnections(true, PhoneInternalInterface.REASON_SIM_NOT_READY);
        this.mAllApnSettings = null;
        this.mAutoAttachOnCreationConfig = false;
        this.mAutoAttachOnCreation.set(false);
    }

    private void onSetDependencyMet(String apnType, boolean met) {
        if (!"hipri".equals(apnType)) {
            ApnContext apnContext = (ApnContext) this.mApnContexts.get(apnType);
            if (apnContext == null) {
                loge("onSetDependencyMet: ApnContext not found in onSetDependencyMet(" + apnType + ", " + met + ")");
                return;
            }
            applyNewState(apnContext, apnContext.isEnabled(), met);
            if ("default".equals(apnType)) {
                apnContext = (ApnContext) this.mApnContexts.get("hipri");
                if (apnContext != null) {
                    applyNewState(apnContext, apnContext.isEnabled(), met);
                }
            }
        }
    }

    public void setPolicyDataEnabled(boolean enabled) {
        log("setPolicyDataEnabled: " + enabled);
        Message msg = obtainMessage(270368);
        msg.arg1 = enabled ? 1 : 0;
        sendMessage(msg);
    }

    private void onSetPolicyDataEnabled(boolean enabled) {
        synchronized (this.mDataEnabledSettings) {
            boolean prevEnabled = isDataEnabled();
            if (this.mDataEnabledSettings.isPolicyDataEnabled() != enabled) {
                this.mDataEnabledSettings.setPolicyDataEnabled(enabled);
                if (prevEnabled != isDataEnabled()) {
                    if (prevEnabled) {
                        onCleanUpAllConnections(PhoneInternalInterface.REASON_DATA_SPECIFIC_DISABLED);
                    } else {
                        reevaluateDataConnections();
                        onTrySetupData(PhoneInternalInterface.REASON_DATA_ENABLED);
                    }
                }
            }
        }
    }

    private void applyNewState(ApnContext apnContext, boolean enabled, boolean met) {
        boolean cleanup = false;
        boolean trySetup = false;
        String str = "applyNewState(" + apnContext.getApnType() + ", " + enabled + "(" + apnContext.isEnabled() + "), " + met + "(" + apnContext.getDependencyMet() + "))";
        log(str);
        apnContext.requestLog(str);
        if (apnContext.isReady()) {
            cleanup = true;
            if (enabled && met) {
                State state = apnContext.getState();
                switch (m21xf0fbc33d()[state.ordinal()]) {
                    case 1:
                    case 2:
                    case 3:
                        log("applyNewState: 'ready' so return");
                        apnContext.requestLog("applyNewState state=" + state + ", so return");
                        return;
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                        trySetup = true;
                        apnContext.setReason(PhoneInternalInterface.REASON_DATA_ENABLED);
                        break;
                }
            } else if (met) {
                apnContext.setReason(PhoneInternalInterface.REASON_DATA_DISABLED);
                if (!(apnContext.getApnType() == "dun" && teardownForDun()) && apnContext.getState() == State.CONNECTED) {
                    cleanup = false;
                } else {
                    str = "Clean up the connection. Apn type = " + apnContext.getApnType() + ", state = " + apnContext.getState();
                    log(str);
                    apnContext.requestLog(str);
                    cleanup = true;
                }
            } else {
                apnContext.setReason(PhoneInternalInterface.REASON_DATA_DEPENDENCY_UNMET);
            }
        } else if (enabled && met) {
            if (apnContext.isEnabled()) {
                apnContext.setReason(PhoneInternalInterface.REASON_DATA_DEPENDENCY_MET);
            } else {
                apnContext.setReason(PhoneInternalInterface.REASON_DATA_ENABLED);
            }
            if (apnContext.getState() == State.FAILED) {
                apnContext.setState(State.IDLE);
            }
            trySetup = true;
        }
        apnContext.setEnabled(enabled);
        apnContext.setDependencyMet(met);
        if (cleanup) {
            cleanUpConnection(true, apnContext);
        }
        if (trySetup) {
            apnContext.resetErrorCodeRetries();
            trySetupData(apnContext);
        }
    }

    private DcAsyncChannel checkForCompatibleConnectedApnContext(ApnContext apnContext) {
        String apnType = apnContext.getApnType();
        ApnSetting dunSetting = null;
        if ("dun".equals(apnType)) {
            dunSetting = fetchDunApn();
        }
        log("checkForCompatibleConnectedApnContext: apnContext=" + apnContext);
        DcAsyncChannel potentialDcac = null;
        Object potentialApnCtx = null;
        for (ApnContext curApnCtx : this.mApnContexts.values()) {
            DcAsyncChannel curDcac = curApnCtx.getDcAc();
            if (curDcac != null) {
                ApnSetting apnSetting = curApnCtx.getApnSetting();
                log("apnSetting: " + apnSetting);
                if (dunSetting == null) {
                    if (apnSetting != null && apnSetting.canHandleType(apnType)) {
                        switch (m21xf0fbc33d()[curApnCtx.getState().ordinal()]) {
                            case 1:
                                log("checkForCompatibleConnectedApnContext: found canHandle conn=" + curDcac + " curApnCtx=" + curApnCtx);
                                return curDcac;
                            case 2:
                            case 6:
                                potentialDcac = curDcac;
                                potentialApnCtx = curApnCtx;
                                break;
                            default:
                                break;
                        }
                    }
                } else if (dunSetting.equals(apnSetting)) {
                    switch (m21xf0fbc33d()[curApnCtx.getState().ordinal()]) {
                        case 1:
                            log("checkForCompatibleConnectedApnContext: found dun conn=" + curDcac + " curApnCtx=" + curApnCtx);
                            return curDcac;
                        case 2:
                        case 6:
                            potentialDcac = curDcac;
                            potentialApnCtx = curApnCtx;
                            break;
                        default:
                            break;
                    }
                } else {
                    continue;
                }
            }
        }
        if (potentialDcac != null) {
            log("checkForCompatibleConnectedApnContext: found potential conn=" + potentialDcac + " curApnCtx=" + potentialApnCtx);
            return potentialDcac;
        }
        log("checkForCompatibleConnectedApnContext: NO conn apnContext=" + apnContext);
        return null;
    }

    public void setEnabled(int id, boolean enable) {
        Message msg = obtainMessage(270349);
        msg.arg1 = id;
        msg.arg2 = enable ? 1 : 0;
        sendMessage(msg);
    }

    private void onEnableApn(int apnId, int enabled) {
        boolean z = true;
        ApnContext apnContext = (ApnContext) this.mApnContextsById.get(apnId);
        if (apnContext == null) {
            loge("onEnableApn(" + apnId + ", " + enabled + "): NO ApnContext");
            return;
        }
        log("onEnableApn: apnContext=" + apnContext + " call applyNewState");
        if (enabled != 1) {
            z = false;
        }
        applyNewState(apnContext, z, apnContext.getDependencyMet());
        if (enabled == 0 && isOnlySingleDcAllowed(this.mPhone.getServiceState().getRilDataRadioTechnology()) && (isHigherPriorityApnContextActive(apnContext) ^ 1) != 0) {
            log("onEnableApn: isOnlySingleDcAllowed true & higher priority APN disabled");
            setupDataOnConnectableApns(PhoneInternalInterface.REASON_SINGLE_PDN_ARBITRATION);
        }
    }

    private boolean onTrySetupData(String reason) {
        log("onTrySetupData: reason=" + reason);
        setupDataOnConnectableApns(reason);
        return true;
    }

    private boolean onTrySetupData(ApnContext apnContext) {
        log("onTrySetupData: apnContext=" + apnContext);
        return trySetupData(apnContext);
    }

    public boolean getDataEnabled() {
        int i = 0;
        int device_provisioned = Global.getInt(this.mResolver, "device_provisioned", 0);
        boolean retVal = "true".equalsIgnoreCase(SystemProperties.get("ro.com.android.mobiledata", "true"));
        ContentResolver contentResolver = this.mResolver;
        String str = "mobile_data";
        if (retVal) {
            i = 1;
        }
        retVal = Global.getInt(contentResolver, str, i) != 0;
        log("getDataEnabled: getIntWithSubId retVal=" + retVal);
        return retVal;
    }

    public void setDataRoamingEnabledByUser(boolean enabled) {
        int phoneSubId = this.mPhone.getSubId();
        if (getDataRoamingEnabled() != enabled) {
            int roaming = enabled ? 1 : 0;
            if (TelephonyManager.getDefault().getSimCount() == 1) {
                Global.putInt(this.mResolver, "data_roaming", roaming);
                setDataRoamingFromUserAction(true);
            } else {
                Global.putInt(this.mResolver, "data_roaming" + phoneSubId, roaming);
            }
            this.mSubscriptionManager.setDataRoaming(roaming, phoneSubId);
            log("setDataRoamingEnabledByUser: set phoneSubId=" + phoneSubId + " isRoaming=" + enabled);
            return;
        }
        log("setDataRoamingEnabledByUser: unchanged phoneSubId=" + phoneSubId + " isRoaming=" + enabled);
    }

    public boolean getDataRoamingEnabled() {
        int i = 1;
        int phoneSubId = this.mPhone.getSubId();
        try {
            if (TelephonyManager.getDefault().getSimCount() != 1) {
                return TelephonyManager.getIntWithSubId(this.mResolver, "data_roaming", phoneSubId) != 0;
            } else {
                ContentResolver contentResolver = this.mResolver;
                String str = "data_roaming";
                if (!getDefaultDataRoamingEnabled()) {
                    i = 0;
                }
                if (Global.getInt(contentResolver, str, i) != 0) {
                    return true;
                }
                return false;
            }
        } catch (SettingNotFoundException snfe) {
            log("getDataRoamingEnabled: SettingNofFoundException snfe=" + snfe);
            return getDefaultDataRoamingEnabled();
        }
    }

    private boolean getDefaultDataRoamingEnabled() {
        return "true".equalsIgnoreCase(SystemProperties.get("ro.com.android.dataroaming", "false")) | ((CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config")).getConfigForSubId(this.mPhone.getSubId()).getBoolean("carrier_default_data_roaming_enabled_bool");
    }

    private void setDefaultDataRoamingEnabled() {
        int i = 1;
        String setting = "data_roaming";
        boolean useCarrierSpecificDefault = false;
        if (TelephonyManager.getDefault().getSimCount() != 1) {
            setting = setting + this.mPhone.getSubId();
            try {
                Global.getInt(this.mResolver, setting);
            } catch (SettingNotFoundException e) {
                useCarrierSpecificDefault = true;
            }
        } else if (!isDataRoamingFromUserAction()) {
            useCarrierSpecificDefault = true;
        }
        if (useCarrierSpecificDefault) {
            int i2;
            boolean defaultVal = getDefaultDataRoamingEnabled();
            log("setDefaultDataRoamingEnabled: " + setting + "default value: " + defaultVal);
            ContentResolver contentResolver = this.mResolver;
            if (defaultVal) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            Global.putInt(contentResolver, setting, i2);
            SubscriptionManager subscriptionManager = this.mSubscriptionManager;
            if (!defaultVal) {
                i = 0;
            }
            subscriptionManager.setDataRoaming(i, this.mPhone.getSubId());
        }
    }

    private boolean isDataRoamingFromUserAction() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mPhone.getContext());
        if (!sp.contains(Phone.DATA_ROAMING_IS_USER_SETTING_KEY) && Global.getInt(this.mResolver, "device_provisioned", 0) == 0) {
            sp.edit().putBoolean(Phone.DATA_ROAMING_IS_USER_SETTING_KEY, false).commit();
        }
        return sp.getBoolean(Phone.DATA_ROAMING_IS_USER_SETTING_KEY, true);
    }

    private void setDataRoamingFromUserAction(boolean isUserAction) {
        PreferenceManager.getDefaultSharedPreferences(this.mPhone.getContext()).edit().putBoolean(Phone.DATA_ROAMING_IS_USER_SETTING_KEY, isUserAction).commit();
    }

    private void onDataRoamingOff() {
        log("onDataRoamingOff");
        if (getDataRoamingEnabled()) {
            notifyDataConnection(PhoneInternalInterface.REASON_ROAMING_OFF);
            return;
        }
        setInitialAttachApn();
        setDataProfilesAsNeeded();
        notifyOffApnsOfAvailability(PhoneInternalInterface.REASON_ROAMING_OFF);
        setupDataOnConnectableApns(PhoneInternalInterface.REASON_ROAMING_OFF);
    }

    private void onDataRoamingOnOrSettingsChanged(int messageType) {
        log("onDataRoamingOnOrSettingsChanged");
        boolean settingChanged = messageType == 270384;
        if (this.mPhone.getServiceState().getDataRoaming()) {
            checkDataRoamingStatus(settingChanged);
            if (getDataRoamingEnabled()) {
                log("onDataRoamingOnOrSettingsChanged: setup data on roaming");
                setupDataOnConnectableApns(PhoneInternalInterface.REASON_ROAMING_ON);
                notifyDataConnection(PhoneInternalInterface.REASON_ROAMING_ON);
            } else {
                log("onDataRoamingOnOrSettingsChanged: Tear down data connection on roaming.");
                cleanUpAllConnections(true, PhoneInternalInterface.REASON_ROAMING_ON);
                notifyOffApnsOfAvailability(PhoneInternalInterface.REASON_ROAMING_ON);
            }
            return;
        }
        log("device is not roaming. ignored the request.");
    }

    private void checkDataRoamingStatus(boolean settingChanged) {
        if (!settingChanged && (getDataRoamingEnabled() ^ 1) != 0 && this.mPhone.getServiceState().getDataRoaming()) {
            for (ApnContext apnContext : this.mApnContexts.values()) {
                if (apnContext.getState() == State.CONNECTED) {
                    this.mDataRoamingLeakageLog.log("PossibleRoamingLeakage  connection params: " + (apnContext.getDcAc() != null ? apnContext.getDcAc().mLastConnectionParams : SpnOverride.MVNO_TYPE_NONE));
                }
            }
        }
    }

    private void onRadioAvailable() {
        log("onRadioAvailable");
        if (this.mPhone.getSimulatedRadioControl() != null) {
            notifyDataConnection(null);
            log("onRadioAvailable: We're on the simulator; assuming data is connected");
        }
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (r != null && r.getRecordsLoaded()) {
            notifyOffApnsOfAvailability(null);
        }
        if (getOverallState() != State.IDLE) {
            cleanUpConnection(true, null);
        }
    }

    private void onRadioOffOrNotAvailable() {
        this.mReregisterOnReconnectFailure = false;
        this.mAutoAttachOnCreation.set(false);
        if (this.mPhone.getSimulatedRadioControl() != null) {
            log("We're on the simulator; assuming radio off is meaningless");
        } else {
            log("onRadioOffOrNotAvailable: is off and clean up all connections");
            cleanUpAllConnections(false, PhoneInternalInterface.REASON_RADIO_TURNED_OFF);
        }
        notifyOffApnsOfAvailability(null);
    }

    private void completeConnection(ApnContext apnContext) {
        log("completeConnection: successful, notify the world apnContext=" + apnContext);
        if (this.mIsProvisioning && (TextUtils.isEmpty(this.mProvisioningUrl) ^ 1) != 0) {
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
        if (this.mProvisioningSpinner != null) {
            sendMessage(obtainMessage(270378, this.mProvisioningSpinner));
        }
        this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
        startNetStatPoll();
        startDataStallAlarm(false);
    }

    private void onDataSetupComplete(AsyncResult ar) {
        DcFailCause cause = DcFailCause.UNKNOWN;
        boolean handleError = false;
        ApnContext apnContext = getValidApnContext(ar, "onDataSetupComplete");
        if (apnContext != null) {
            ApnSetting apn;
            Intent intent;
            if (ar.exception == null) {
                DcAsyncChannel dcac = apnContext.getDcAc();
                if (dcac == null) {
                    log("onDataSetupComplete: no connection to DC, handle as error");
                    cause = DcFailCause.CONNECTION_TO_DATACONNECTIONAC_BROKEN;
                    handleError = true;
                } else {
                    apn = apnContext.getApnSetting();
                    log("onDataSetupComplete: success apn=" + (apn == null ? "unknown" : apn.apn));
                    if (!(apn == null || apn.proxy == null || apn.proxy.length() == 0)) {
                        try {
                            String port = apn.port;
                            if (TextUtils.isEmpty(port)) {
                                port = "8080";
                            }
                            dcac.setLinkPropertiesHttpProxySync(new ProxyInfo(apn.proxy, Integer.parseInt(port), null));
                        } catch (NumberFormatException e) {
                            loge("onDataSetupComplete: NumberFormatException making ProxyProperties (" + apn.port + "): " + e);
                        }
                    }
                    if (TextUtils.equals(apnContext.getApnType(), "default")) {
                        try {
                            SystemProperties.set(PUPPET_MASTER_RADIO_STRESS_TEST, "true");
                        } catch (RuntimeException e2) {
                            log("Failed to set PUPPET_MASTER_RADIO_STRESS_TEST to true");
                        }
                        if (this.mCanSetPreferApn && this.mPreferredApn == null) {
                            log("onDataSetupComplete: PREFERRED APN is null");
                            this.mPreferredApn = apn;
                            if (this.mPreferredApn != null) {
                                setPreferredApn(this.mPreferredApn.id);
                            }
                        }
                    } else {
                        try {
                            SystemProperties.set(PUPPET_MASTER_RADIO_STRESS_TEST, "false");
                        } catch (RuntimeException e3) {
                            log("Failed to set PUPPET_MASTER_RADIO_STRESS_TEST to false");
                        }
                    }
                    this.mHasDataConnConncted = true;
                    this.mSetupDataCallFailureCount = 0;
                    apnContext.setState(State.CONNECTED);
                    checkDataRoamingStatus(false);
                    boolean isProvApn = apnContext.isProvisioningApn();
                    ConnectivityManager cm = ConnectivityManager.from(this.mPhone.getContext());
                    if (this.mProvisionBroadcastReceiver != null) {
                        this.mPhone.getContext().unregisterReceiver(this.mProvisionBroadcastReceiver);
                        this.mProvisionBroadcastReceiver = null;
                    }
                    if (!isProvApn || this.mIsProvisioning) {
                        cm.setProvisioningNotificationVisible(false, 0, this.mProvisionActionName);
                        completeConnection(apnContext);
                    } else {
                        log("onDataSetupComplete: successful, BUT send connected to prov apn as mIsProvisioning:" + this.mIsProvisioning + " == false" + " && (isProvisioningApn:" + isProvApn + " == true");
                        this.mProvisionBroadcastReceiver = new ProvisionNotificationBroadcastReceiver(cm.getMobileProvisioningUrl(), TelephonyManager.getDefault().getNetworkOperatorName());
                        this.mPhone.getContext().registerReceiver(this.mProvisionBroadcastReceiver, new IntentFilter(this.mProvisionActionName));
                        cm.setProvisioningNotificationVisible(true, 0, this.mProvisionActionName);
                        setRadio(false);
                    }
                    log("onDataSetupComplete: SETUP complete type=" + apnContext.getApnType() + ", reason:" + apnContext.getReason());
                    if (Build.IS_DEBUGGABLE) {
                        int pcoVal = SystemProperties.getInt("persist.radio.test.pco", -1);
                        if (pcoVal != -1) {
                            log("PCO testing: read pco value from persist.radio.test.pco " + pcoVal);
                            byte[] value = new byte[]{(byte) pcoVal};
                            intent = new Intent("com.android.internal.telephony.CARRIER_SIGNAL_PCO_VALUE");
                            intent.putExtra("apnType", "default");
                            intent.putExtra("apnProto", "IPV4V6");
                            intent.putExtra("pcoId", 65280);
                            intent.putExtra("pcoValue", value);
                            this.mPhone.getCarrierSignalAgent().notifyCarrierSignalReceivers(intent);
                        }
                    }
                }
            } else {
                cause = ar.result;
                apn = apnContext.getApnSetting();
                String str = "onDataSetupComplete: error apn=%s cause=%s";
                Object[] objArr = new Object[2];
                objArr[0] = apn == null ? "unknown" : apn.apn;
                objArr[1] = cause;
                log(String.format(str, objArr));
                if (cause.isEventLoggable()) {
                    int cid = getCellLocationId();
                    EventLog.writeEvent(EventLogTags.PDP_SETUP_FAIL, new Object[]{Integer.valueOf(cause.ordinal()), Integer.valueOf(cid), Integer.valueOf(TelephonyManager.getDefault().getNetworkType())});
                }
                apn = apnContext.getApnSetting();
                this.mPhone.notifyPreciseDataConnectionFailed(apnContext.getReason(), apnContext.getApnType(), apn != null ? apn.apn : "unknown", cause.toString());
                intent = new Intent("com.android.internal.telephony.CARRIER_SIGNAL_REQUEST_NETWORK_FAILED");
                intent.putExtra("errorCode", cause.getErrorCode());
                intent.putExtra("apnType", apnContext.getApnType());
                this.mPhone.getCarrierSignalAgent().notifyCarrierSignalReceivers(intent);
                if (cause.isRestartRadioFail(this.mPhone.getContext(), this.mPhone.getSubId()) || apnContext.restartOnError(cause.getErrorCode())) {
                    log("Modem restarted.");
                    sendRestartRadio();
                }
                if (isPermanentFailure(cause)) {
                    log("cause = " + cause + ", mark apn as permanent failed. apn = " + apn);
                    apnContext.markApnPermanentFailed(apn);
                }
                handleError = true;
            }
            if (handleError) {
                onDataSetupCompleteError(ar);
            }
            if (!this.mDataEnabledSettings.isInternalDataEnabled()) {
                cleanUpAllConnections(PhoneInternalInterface.REASON_DATA_DISABLED);
            }
        }
    }

    private ApnContext getValidApnContext(AsyncResult ar, String logString) {
        if (ar != null && (ar.userObj instanceof Pair)) {
            Pair<ApnContext, Integer> pair = ar.userObj;
            ApnContext apnContext = pair.first;
            if (apnContext != null) {
                int generation = apnContext.getConnectionGeneration();
                log("getValidApnContext (" + logString + ") on " + apnContext + " got " + generation + " vs " + pair.second);
                if (generation == ((Integer) pair.second).intValue()) {
                    return apnContext;
                }
                log("ignoring obsolete " + logString);
                return null;
            }
        }
        return null;
    }

    private void onDataSetupCompleteError(AsyncResult ar) {
        ApnContext apnContext = getValidApnContext(ar, "onDataSetupCompleteError");
        if (apnContext != null) {
            if (this.mPhone.getServiceState().getDataRegState() == 0) {
                int i = this.mSetupDataCallFailureCount + 1;
                this.mSetupDataCallFailureCount = i;
                if (i == 10 && this.mHasDataConnConncted) {
                    this.mHasDataConnConncted = false;
                    DcFailCause cause = DcFailCause.UNKNOWN;
                    ApnSetting apn = apnContext.getApnSetting();
                    cause = ar.result;
                    int cid = getCellLocationId();
                    String error_info = getCellLocation() + ", onDataSetupComplete error apn:" + (apn == null ? "unknown" : apn.apn) + ", cause:" + cause + ", nwType:" + TelephonyManager.getDefault().getNetworkType();
                    int log_type = -1;
                    String log_desc = SpnOverride.MVNO_TYPE_NONE;
                    try {
                        String[] log_array = this.mPhone.getContext().getString(this.mPhone.getContext().getResources().getIdentifier("zz_oppo_critical_log_110", "string", "android")).split(",");
                        log_type = Integer.valueOf(log_array[0]).intValue();
                        log_desc = log_array[1];
                    } catch (Exception e) {
                    }
                    OppoManager.writeLogToPartition(log_type, error_info, "NETWORK", RIL.ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_SET_UP_DATA_ERROR, log_desc);
                }
            }
            long delay = apnContext.getDelayForNextApn(this.mFailFast);
            if (delay >= 0) {
                log("onDataSetupCompleteError: Try next APN. delay = " + delay);
                apnContext.setState(State.SCANNING);
                startAlarmForReconnect(delay, apnContext);
            } else {
                apnContext.setState(State.FAILED);
                this.mPhone.notifyDataConnection(PhoneInternalInterface.REASON_APN_FAILED, apnContext.getApnType());
                apnContext.setDataConnectionAc(null);
                log("onDataSetupCompleteError: Stop retrying APNs.");
            }
        }
    }

    private void onDataConnectionRedirected(String redirectUrl) {
        if (!TextUtils.isEmpty(redirectUrl)) {
            Intent intent = new Intent("com.android.internal.telephony.CARRIER_SIGNAL_REDIRECTED");
            intent.putExtra("redirectionUrl", redirectUrl);
            this.mPhone.getCarrierSignalAgent().notifyCarrierSignalReceivers(intent);
            log("Notify carrier signal receivers with redirectUrl: " + redirectUrl);
        }
    }

    private void onDisconnectDone(AsyncResult ar) {
        ApnContext apnContext = getValidApnContext(ar, "onDisconnectDone");
        if (apnContext != null) {
            log("onDisconnectDone: EVENT_DISCONNECT_DONE apnContext=" + apnContext);
            apnContext.setState(State.IDLE);
            this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
            if (isDisconnected() && this.mPhone.getServiceStateTracker().processPendingRadioPowerOffAfterDataOff()) {
                log("onDisconnectDone: radio will be turned off, no retries");
                apnContext.setApnSetting(null);
                apnContext.setDataConnectionAc(null);
                if (this.mDisconnectPendingCount > 0) {
                    this.mDisconnectPendingCount--;
                }
                if (this.mDisconnectPendingCount == 0) {
                    notifyDataDisconnectComplete();
                    notifyAllDataDisconnected();
                }
                return;
            }
            if (this.mAttached.get() && apnContext.isReady() && retryAfterDisconnected(apnContext)) {
                try {
                    SystemProperties.set(PUPPET_MASTER_RADIO_STRESS_TEST, "false");
                } catch (RuntimeException e) {
                    log("Failed to set PUPPET_MASTER_RADIO_STRESS_TEST to false");
                }
                log("onDisconnectDone: attached, ready and retry after disconnect");
                long delay = apnContext.getRetryAfterDisconnectDelay();
                if (delay > 0) {
                    startAlarmForReconnect(delay, apnContext);
                }
            } else {
                boolean restartRadioAfterProvisioning = this.mPhone.getContext().getResources().getBoolean(17957000);
                if (apnContext.isProvisioningApn() && restartRadioAfterProvisioning) {
                    log("onDisconnectDone: restartRadio after provisioning");
                    restartRadio();
                }
                apnContext.setApnSetting(null);
                apnContext.setDataConnectionAc(null);
                if (isOnlySingleDcAllowed(this.mPhone.getServiceState().getRilDataRadioTechnology())) {
                    log("onDisconnectDone: isOnlySigneDcAllowed true so setup single apn");
                    if ("default".equals(apnContext.getApnType()) && this.mIsWifiConnected) {
                        log("wifi have been conneted, set default apn type retry false--do nothing!");
                    } else {
                        setupDataOnConnectableApns(PhoneInternalInterface.REASON_SINGLE_PDN_ARBITRATION);
                    }
                } else {
                    log("onDisconnectDone: not retrying");
                }
            }
            if (this.mDisconnectPendingCount > 0) {
                this.mDisconnectPendingCount--;
            }
            if (this.mDisconnectPendingCount == 0) {
                apnContext.setConcurrentVoiceAndDataAllowed(this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed());
                notifyDataDisconnectComplete();
                notifyAllDataDisconnected();
            }
        }
    }

    private void onDisconnectDcRetrying(AsyncResult ar) {
        ApnContext apnContext = getValidApnContext(ar, "onDisconnectDcRetrying");
        if (apnContext != null) {
            apnContext.setState(State.RETRYING);
            log("onDisconnectDcRetrying: apnContext=" + apnContext);
            this.mPhone.notifyDataConnection(apnContext.getReason(), apnContext.getApnType());
        }
    }

    private void onVoiceCallStarted() {
        log("onVoiceCallStarted");
        this.mInVoiceCall = true;
        SubscriptionManager su = SubscriptionManager.from(this.mPhone.getContext());
        boolean isDefaultDataPhone = this.mPhone.getSubId() == SubscriptionManager.getDefaultDataSubId();
        if (isDefaultDataPhone && (this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed() ^ 1) != 0) {
            removeMessages(270438);
            mDelayMeasure = true;
            log("WLAN+ onVoiceCallStarted mDelayMeasure:true");
        }
        if (isConnected() && (this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed() ^ 1) != 0) {
            log("onVoiceCallStarted stop polling");
            stopNetStatPoll();
            stopDataStallAlarm();
            notifyDataConnection(PhoneInternalInterface.REASON_VOICE_CALL_STARTED);
        }
        int defaultDataSubId = SubscriptionManager.getDefaultDataSubId();
        if (!isDefaultDataPhone && SubscriptionManager.isValidSubscriptionId(defaultDataSubId) && TelephonyManager.getDefault().getMultiSimConfiguration() != MultiSimVariants.DSDA) {
            for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++) {
                Phone phone = PhoneFactory.getPhone(i);
                if (phone != null && phone.getSubId() == defaultDataSubId) {
                    phone.mDcTracker.notifyDataConnection(PhoneInternalInterface.REASON_VOICE_CALL_STARTED);
                }
            }
        }
    }

    private void onVoiceCallEnded() {
        log("onVoiceCallEnded");
        this.mInVoiceCall = false;
        if (isConnected()) {
            if (this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed()) {
                resetPollStats();
            } else {
                startNetStatPoll();
                startDataStallAlarm(false);
                notifyDataConnection(PhoneInternalInterface.REASON_VOICE_CALL_ENDED);
            }
        }
        SubscriptionManager su = SubscriptionManager.from(this.mPhone.getContext());
        boolean isDefaultDataPhone = this.mPhone.getSubId() == SubscriptionManager.getDefaultDataSubId();
        if (OemConstant.getWlanAssistantEnable(this.mPhone.getContext()) && mMeasureDataState && ((getDataEnabled() || haveVsimIgnoreUserDataSetting()) && (this.mPhone.getServiceState().getRoaming() ^ 1) != 0 && this.mIsWifiConnected && isDefaultDataPhone && (this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed() ^ 1) != 0)) {
            removeMessages(270438);
            sendMessageDelayed(obtainMessage(270438, null), 60000);
        } else {
            setupDataOnConnectableApns(PhoneInternalInterface.REASON_VOICE_CALL_ENDED);
            mDelayMeasure = false;
            log("WLAN+ onVoiceCallEnded mDelayMeasure:false");
        }
        int defaultDataSubId = SubscriptionManager.getDefaultDataSubId();
        if (!isDefaultDataPhone && SubscriptionManager.isValidSubscriptionId(defaultDataSubId) && TelephonyManager.getDefault().getMultiSimConfiguration() != MultiSimVariants.DSDA) {
            for (int i = 0; i < TelephonyManager.getDefault().getPhoneCount(); i++) {
                Phone phone = PhoneFactory.getPhone(i);
                if (phone != null && phone.getSubId() == defaultDataSubId) {
                    phone.mDcTracker.notifyDataConnection(PhoneInternalInterface.REASON_VOICE_CALL_ENDED);
                }
            }
        }
    }

    private void onCleanUpConnection(boolean tearDown, int apnId, String reason) {
        log("onCleanUpConnection");
        ApnContext apnContext = (ApnContext) this.mApnContextsById.get(apnId);
        if (apnContext != null) {
            apnContext.setReason(reason);
            cleanUpConnection(tearDown, apnContext);
        }
    }

    private boolean isConnected() {
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (apnContext.getState() == State.CONNECTED) {
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

    private void notifyDataConnection(String reason) {
        log("notifyDataConnection: reason=" + reason);
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (this.mAttached.get() && apnContext.isReady()) {
                log("notifyDataConnection: type:" + apnContext.getApnType());
                this.mPhone.notifyDataConnection(reason != null ? reason : apnContext.getReason(), apnContext.getApnType());
            }
        }
        notifyOffApnsOfAvailability(reason);
    }

    protected void setDataProfilesAsNeeded() {
        log("setDataProfilesAsNeeded");
        if (this.mAllApnSettings != null && (this.mAllApnSettings.isEmpty() ^ 1) != 0) {
            ArrayList<DataProfile> dps = new ArrayList();
            for (ApnSetting apn : this.mAllApnSettings) {
                if (apn.modemCognitive) {
                    DataProfile dp = new DataProfile(apn);
                    if (!dps.contains(dp)) {
                        dps.add(dp);
                    }
                }
            }
            if (dps.size() > 0) {
                this.mPhone.mCi.setDataProfile((DataProfile[]) dps.toArray(new DataProfile[0]), this.mPhone.getServiceState().getDataRoamingFromRegistration(), null);
            }
        }
    }

    protected void createAllApnList() {
        this.mMvnoMatched = false;
        this.mAllApnSettings = new ArrayList();
        IccRecords r = (IccRecords) this.mIccRecords.get();
        String operator = getOperatorNumeric();
        if (operator != null) {
            String selection = "numeric = '" + operator + "'";
            log("createAllApnList: selection=" + selection);
            Cursor cursor = this.mPhone.getContext().getContentResolver().query(Carriers.CONTENT_URI, null, selection, null, "_id");
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    this.mAllApnSettings = createApnList(cursor);
                }
                cursor.close();
            }
        }
        addEmergencyApnSetting();
        dedupeApnSettings();
        if (this.mAllApnSettings.isEmpty()) {
            log("createAllApnList: No APN found for carrier: " + operator);
            this.mPreferredApn = null;
        } else {
            this.mPreferredApn = getPreferredApn();
            if (!(this.mPreferredApn == null || (this.mPreferredApn.numeric.equals(operator) ^ 1) == 0)) {
                this.mPreferredApn = null;
                setPreferredApn(-1);
            }
            log("createAllApnList: mPreferredApn=" + this.mPreferredApn);
        }
        log("createAllApnList: X mAllApnSettings=" + this.mAllApnSettings);
        setDataProfilesAsNeeded();
    }

    protected void dedupeApnSettings() {
        ArrayList<ApnSetting> resultApns = new ArrayList();
        for (int i = 0; i < this.mAllApnSettings.size() - 1; i++) {
            ApnSetting first = (ApnSetting) this.mAllApnSettings.get(i);
            int j = i + 1;
            while (j < this.mAllApnSettings.size()) {
                ApnSetting second = (ApnSetting) this.mAllApnSettings.get(j);
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

    private ApnSetting mergeApns(ApnSetting dest, ApnSetting src) {
        String roamingProtocol;
        int id = dest.id;
        ArrayList<String> resultTypes = new ArrayList();
        resultTypes.addAll(Arrays.asList(dest.types));
        for (String srcType : src.types) {
            if (!resultTypes.contains(srcType)) {
                resultTypes.add(srcType);
            }
            if (srcType.equals("default")) {
                id = src.id;
            }
        }
        String mmsc = TextUtils.isEmpty(dest.mmsc) ? src.mmsc : dest.mmsc;
        String mmsProxy = TextUtils.isEmpty(dest.mmsProxy) ? src.mmsProxy : dest.mmsProxy;
        String mmsPort = TextUtils.isEmpty(dest.mmsPort) ? src.mmsPort : dest.mmsPort;
        String proxy = TextUtils.isEmpty(dest.proxy) ? src.proxy : dest.proxy;
        String port = TextUtils.isEmpty(dest.port) ? src.port : dest.port;
        String protocol = src.protocol.equals("IPV4V6") ? src.protocol : dest.protocol;
        if (src.roamingProtocol.equals("IPV4V6")) {
            roamingProtocol = src.roamingProtocol;
        } else {
            roamingProtocol = dest.roamingProtocol;
        }
        int bearerBitmask = (dest.bearerBitmask == 0 || src.bearerBitmask == 0) ? 0 : dest.bearerBitmask | src.bearerBitmask;
        return new ApnSetting(id, dest.numeric, dest.carrier, dest.apn, proxy, port, mmsc, mmsProxy, mmsPort, dest.user, dest.password, dest.authType, (String[]) resultTypes.toArray(new String[0]), protocol, roamingProtocol, dest.carrierEnabled, 0, bearerBitmask, dest.profileId, !dest.modemCognitive ? src.modemCognitive : true, dest.maxConns, dest.waitTime, dest.maxConnsTime, dest.mtu, dest.mvnoType, dest.mvnoMatchData);
    }

    private DcAsyncChannel createDataConnection() {
        log("createDataConnection E");
        int id = this.mUniqueIdGenerator.getAndIncrement();
        DataConnection conn = DataConnection.makeDataConnection(this.mPhone, id, this, this.mDcTesterFailBringUpAll, this.mDcc);
        this.mDataConnections.put(Integer.valueOf(id), conn);
        DcAsyncChannel dcac = new DcAsyncChannel(conn, this.LOG_TAG);
        int status = dcac.fullyConnectSync(this.mPhone.getContext(), this, conn.getHandler());
        if (status == 0) {
            this.mDataConnectionAcHashMap.put(Integer.valueOf(dcac.getDataConnectionIdSync()), dcac);
        } else {
            loge("createDataConnection: Could not connect to dcac=" + dcac + " status=" + status);
        }
        log("createDataConnection() X id=" + id + " dc=" + conn);
        return dcac;
    }

    private void destroyDataConnections() {
        if (this.mDataConnections != null) {
            log("destroyDataConnections: clear mDataConnectionList");
            this.mDataConnections.clear();
            return;
        }
        log("destroyDataConnections: mDataConnecitonList is empty, ignore");
    }

    private ArrayList<ApnSetting> buildWaitingApns(String requestedApnType, int radioTech) {
        boolean usePreferred;
        log("buildWaitingApns: E requestedApnType=" + requestedApnType);
        ArrayList<ApnSetting> apnList = new ArrayList();
        if (requestedApnType.equals("dun")) {
            ApnSetting dun = fetchDunApn();
            if (dun != null) {
                apnList.add(dun);
                log("buildWaitingApns: X added APN_TYPE_DUN apnList=" + apnList);
                return apnList;
            }
        }
        String operator = getOperatorNumeric();
        try {
            usePreferred = this.mPhone.getContext().getResources().getBoolean(17956930) ^ 1;
        } catch (NotFoundException e) {
            log("buildWaitingApns: usePreferred NotFoundException set to true");
            usePreferred = true;
        }
        if (usePreferred) {
            this.mPreferredApn = getPreferredApn();
        }
        log("buildWaitingApns: usePreferred=" + usePreferred + " canSetPreferApn=" + this.mCanSetPreferApn + " mPreferredApn=" + this.mPreferredApn + " operator=" + operator + " radioTech=" + radioTech + " IccRecords r=" + this.mIccRecords);
        if (usePreferred && this.mCanSetPreferApn && this.mPreferredApn != null && this.mPreferredApn.canHandleType(requestedApnType)) {
            log("buildWaitingApns: Preferred APN:" + operator + ":" + this.mPreferredApn.numeric + ":" + this.mPreferredApn);
            if (!this.mPreferredApn.numeric.equals(operator)) {
                log("buildWaitingApns: no preferred APN");
                setPreferredApn(-1);
                this.mPreferredApn = null;
            } else if (ServiceState.bitmaskHasTech(this.mPreferredApn.bearerBitmask, radioTech)) {
                apnList.add(this.mPreferredApn);
                log("buildWaitingApns: X added preferred apnList=" + apnList);
                return apnList;
            } else {
                log("buildWaitingApns: no preferred APN");
                setPreferredApn(-1);
                this.mPreferredApn = null;
            }
        }
        if (this.mAllApnSettings != null) {
            log("buildWaitingApns: mAllApnSettings=" + this.mAllApnSettings);
            for (ApnSetting apn : this.mAllApnSettings) {
                if (!apn.canHandleType(requestedApnType)) {
                    log("buildWaitingApns: couldn't handle requested ApnType=" + requestedApnType);
                } else if (ServiceState.bitmaskHasTech(apn.bearerBitmask, radioTech)) {
                    log("buildWaitingApns: adding apn=" + apn);
                    apnList.add(apn);
                } else {
                    log("buildWaitingApns: bearerBitmask:" + apn.bearerBitmask + " does " + "not include radioTech:" + radioTech);
                }
            }
        } else {
            loge("mAllApnSettings is null!");
        }
        log("buildWaitingApns: " + apnList.size() + " APNs in the list: " + apnList);
        return apnList;
    }

    private String apnListToString(ArrayList<ApnSetting> apns) {
        StringBuilder result = new StringBuilder();
        int size = apns.size();
        for (int i = 0; i < size; i++) {
            result.append('[').append(((ApnSetting) apns.get(i)).toString()).append(']');
        }
        return result.toString();
    }

    protected void setPreferredApn(int pos) {
        if (this.mCanSetPreferApn) {
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
            return;
        }
        log("setPreferredApn: X !canSEtPreferApn");
    }

    protected ApnSetting getPreferredApn() {
        if (this.mAllApnSettings == null || this.mAllApnSettings.isEmpty()) {
            log("getPreferredApn: mAllApnSettings is " + (this.mAllApnSettings == null ? "null" : "empty"));
            return null;
        }
        int count;
        Cursor cursor = this.mPhone.getContext().getContentResolver().query(Uri.withAppendedPath(PREFERAPN_NO_UPDATE_URI_USING_SUBID, Long.toString((long) this.mPhone.getSubId())), new String[]{"_id", Calls.CACHED_NAME, "apn"}, null, null, "name ASC");
        if (cursor != null) {
            this.mCanSetPreferApn = true;
        } else {
            this.mCanSetPreferApn = false;
        }
        StringBuilder append = new StringBuilder().append("getPreferredApn: mRequestedApnType=").append(this.mRequestedApnType).append(" cursor=").append(cursor).append(" cursor.count=");
        if (cursor != null) {
            count = cursor.getCount();
        } else {
            count = 0;
        }
        log(append.append(count).toString());
        if (this.mCanSetPreferApn && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int pos = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
            for (ApnSetting p : this.mAllApnSettings) {
                log("getPreferredApn: apnSetting=" + p);
                if (p.id == pos && p.canHandleType(this.mRequestedApnType)) {
                    log("getPreferredApn: X found apnSetting" + p);
                    cursor.close();
                    return p;
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        log("getPreferredApn: X not found");
        return null;
    }

    void onRecordsLoaded() {
        int subId = this.mPhone.getSubId();
        if (this.mSubscriptionManager.isActiveSubId(subId)) {
            onRecordsLoadedOrSubIdChanged();
        } else {
            log("Ignoring EVENT_RECORDS_LOADED as subId is not valid: " + subId);
        }
    }

    public void handleMessage(Message msg) {
        boolean enabled;
        Bundle bundle;
        switch (msg.what) {
            case 100:
                onRecordsLoaded();
                if (this.mSimRecords != null) {
                    this.mSimRecords.unregisterForRecordsLoaded(this);
                    this.mSimRecords = null;
                    return;
                }
                return;
            case 69636:
                log("DISCONNECTED_CONNECTED: msg=" + msg);
                DcAsyncChannel dcac = msg.obj;
                this.mDataConnectionAcHashMap.remove(Integer.valueOf(dcac.getDataConnectionIdSync()));
                dcac.disconnected();
                return;
            case 270336:
                onDataSetupComplete((AsyncResult) msg.obj);
                return;
            case 270337:
                break;
            case 270338:
                this.mSimRecords = this.mPhone.getSIMRecords();
                if (!(this.mIccRecords.get() instanceof RuimRecords) || this.mSimRecords == null) {
                    onRecordsLoaded();
                    return;
                } else {
                    this.mSimRecords.registerForRecordsLoaded(this, 100, null);
                    return;
                }
            case 270339:
                if (msg.obj instanceof ApnContext) {
                    onTrySetupData((ApnContext) msg.obj);
                    return;
                } else if (msg.obj instanceof String) {
                    onTrySetupData((String) msg.obj);
                    return;
                } else {
                    loge("EVENT_TRY_SETUP request w/o apnContext or String");
                    return;
                }
            case 270340:
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
                onEnableApn(msg.arg1, msg.arg2);
                return;
            case 270351:
                log("DataConnectionTracker.handleMessage: EVENT_DISCONNECT_DONE msg=" + msg);
                onDisconnectDone((AsyncResult) msg.obj);
                return;
            case 270352:
                onDataConnectionAttached();
                return;
            case 270353:
                onDataStallAlarm(msg.arg1);
                return;
            case 270354:
                doRecovery();
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
                if (this.mState == State.FAILED) {
                    cleanUpAllConnections(false, PhoneInternalInterface.REASON_PS_RESTRICT_ENABLED);
                    this.mReregisterOnReconnectFailure = false;
                }
                ApnContext apnContext = (ApnContext) this.mApnContextsById.get(0);
                if (apnContext != null) {
                    apnContext.setReason(PhoneInternalInterface.REASON_PS_RESTRICT_ENABLED);
                    trySetupData(apnContext);
                    return;
                }
                loge("**** Default ApnContext not found ****");
                loge("Default ApnContext not found");
                return;
            case 270360:
                boolean tearDown = msg.arg1 != 0;
                log("EVENT_CLEAN_UP_CONNECTION tearDown=" + tearDown);
                if (msg.obj instanceof ApnContext) {
                    cleanUpConnection(tearDown, (ApnContext) msg.obj);
                    return;
                } else {
                    onCleanUpConnection(tearDown, msg.arg2, (String) msg.obj);
                    return;
                }
            case 270362:
                restartRadio();
                return;
            case 270363:
                onSetInternalDataEnabled(msg.arg1 == 1, (Message) msg.obj);
                return;
            case 270364:
                log("EVENT_RESET_DONE");
                onResetDone((AsyncResult) msg.obj);
                return;
            case 270365:
                if (!(msg.obj == null || (msg.obj instanceof String))) {
                    msg.obj = null;
                }
                onCleanUpAllConnections((String) msg.obj);
                return;
            case 270366:
                enabled = msg.arg1 == 1;
                log("CMD_SET_USER_DATA_ENABLE enabled=" + enabled);
                onSetUserDataEnabled(enabled);
                return;
            case 270367:
                boolean met = msg.arg1 == 1;
                log("CMD_SET_DEPENDENCY_MET met=" + met);
                bundle = msg.getData();
                if (bundle != null) {
                    String apnType = (String) bundle.get("apnType");
                    if (apnType != null) {
                        onSetDependencyMet(apnType, met);
                        return;
                    }
                    return;
                }
                return;
            case 270368:
                onSetPolicyDataEnabled(msg.arg1 == 1);
                return;
            case 270369:
                onUpdateIcc();
                return;
            case 270370:
                log("DataConnectionTracker.handleMessage: EVENT_DISCONNECT_DC_RETRYING msg=" + msg);
                onDisconnectDcRetrying((AsyncResult) msg.obj);
                return;
            case 270371:
                onDataSetupCompleteError((AsyncResult) msg.obj);
                return;
            case 270372:
                sEnableFailFastRefCounter = (msg.arg1 == 1 ? 1 : -1) + sEnableFailFastRefCounter;
                log("CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA:  sEnableFailFastRefCounter=" + sEnableFailFastRefCounter);
                if (sEnableFailFastRefCounter < 0) {
                    loge("CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA: sEnableFailFastRefCounter:" + sEnableFailFastRefCounter + " < 0");
                    sEnableFailFastRefCounter = 0;
                }
                enabled = sEnableFailFastRefCounter > 0;
                log("CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA: enabled=" + enabled + " sEnableFailFastRefCounter=" + sEnableFailFastRefCounter);
                if (this.mFailFast != enabled) {
                    this.mFailFast = enabled;
                    this.mDataStallDetectionEnabled = enabled ^ 1;
                    if (this.mDataStallDetectionEnabled && getOverallState() == State.CONNECTED && (!this.mInVoiceCall || this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed())) {
                        log("CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA: start data stall");
                        stopDataStallAlarm();
                        startDataStallAlarm(false);
                        return;
                    }
                    log("CMD_SET_ENABLE_FAIL_FAST_MOBILE_DATA: stop data stall");
                    stopDataStallAlarm();
                    return;
                }
                return;
            case 270373:
                bundle = msg.getData();
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
                boolean isProvApn;
                log("CMD_IS_PROVISIONING_APN");
                Object apnType2 = null;
                try {
                    bundle = msg.getData();
                    if (bundle != null) {
                        apnType2 = (String) bundle.get("apnType");
                    }
                    if (TextUtils.isEmpty(apnType2)) {
                        loge("CMD_IS_PROVISIONING_APN: apnType is empty");
                        isProvApn = false;
                    } else {
                        isProvApn = isProvisioningApn(apnType2);
                    }
                } catch (ClassCastException e2) {
                    loge("CMD_IS_PROVISIONING_APN: NO provisioning url ignoring");
                    isProvApn = false;
                }
                log("CMD_IS_PROVISIONING_APN: ret=" + isProvApn);
                this.mReplyAc.replyToMessage(msg, 270374, isProvApn ? 1 : 0);
                return;
            case 270375:
                log("EVENT_PROVISIONING_APN_ALARM");
                ApnContext apnCtx = (ApnContext) this.mApnContextsById.get(0);
                if (apnCtx.isProvisioningApn() && apnCtx.isConnectedOrConnecting()) {
                    if (this.mProvisioningApnAlarmTag == msg.arg1) {
                        log("EVENT_PROVISIONING_APN_ALARM: Disconnecting");
                        this.mIsProvisioning = false;
                        this.mProvisioningUrl = null;
                        stopProvisioningApnAlarm();
                        sendCleanUpConnection(true, apnCtx);
                        return;
                    }
                    log("EVENT_PROVISIONING_APN_ALARM: ignore stale tag, mProvisioningApnAlarmTag:" + this.mProvisioningApnAlarmTag + " != arg1:" + msg.arg1);
                    return;
                }
                log("EVENT_PROVISIONING_APN_ALARM: Not connected ignore");
                return;
            case 270376:
                if (msg.arg1 == 1) {
                    handleStartNetStatPoll((Activity) msg.obj);
                    return;
                } else if (msg.arg1 == 0) {
                    handleStopNetStatPoll((Activity) msg.obj);
                    return;
                } else {
                    return;
                }
            case 270377:
                if (this.mPhone.getServiceState().getRilDataRadioTechnology() != 0) {
                    if (isTelstraSim()) {
                        int oldClass = TelephonyManager.getNetworkClass(this.mDataType);
                        this.mDataType = this.mPhone.getServiceState().getDataNetworkType();
                        if (TelephonyManager.getNetworkClass(this.mDataType) == oldClass) {
                            return;
                        }
                    }
                    log("EVENT_DATA_RAT_CHANGED onUpdateIcc()");
                    onUpdateIcc();
                    cleanUpConnectionsOnUpdatedApns(false, PhoneInternalInterface.REASON_NW_TYPE_CHANGED);
                    if (this.mPhone.getServiceState().getVoiceRegState() == 0 || this.mPhone.getServiceState().getDataRegState() == 0) {
                        setupDataOnConnectableApns(PhoneInternalInterface.REASON_NW_TYPE_CHANGED, RetryFailures.ONLY_ON_CHANGE);
                    }
                    if (OemConstant.getWlanAssistantEnable(this.mPhone.getContext())) {
                        int dataRadioTech = this.mPhone.getServiceState().getRilDataRadioTechnology();
                        if ((dataRadioTech == 19 && mLastDataRadioTech == 14) || (dataRadioTech == 14 && mLastDataRadioTech == 19)) {
                            mLastDataRadioTech = dataRadioTech;
                            return;
                        }
                        mLastDataRadioTech = dataRadioTech;
                        SubscriptionManager su = SubscriptionManager.from(this.mPhone.getContext());
                        boolean isDefaultDataPhone = this.mPhone.getSubId() == SubscriptionManager.getDefaultDataSubId();
                        if (isDefaultDataPhone) {
                            boolean isRomming = this.mPhone.getServiceState().getRoaming();
                            log("WLAN+ EVENT_DATA_RAT_CHANGED: mMeasureDataState=" + mMeasureDataState + " Roaming=" + isRomming + " DataEnabled=" + (!getDataEnabled() ? haveVsimIgnoreUserDataSetting() : true) + " isDefaultDataPhone=" + isDefaultDataPhone);
                            boolean myMeasureDataState = (!mMeasureDataState || (mDelayMeasure ^ 1) == 0 || (isRomming ^ 1) == 0) ? false : !getDataEnabled() ? haveVsimIgnoreUserDataSetting() : true;
                            if (myMeasureDataState) {
                                new Thread() {
                                    public void run() {
                                        if (!DcTracker.this.mCm.measureDataState(DcTracker.this.mPhone.getServiceStateTracker().getSignalLevel())) {
                                            NetworkRequest request = DcTracker.this.mCm.getCelluarNetworkRequest();
                                            if (request != null) {
                                                if (DcTracker.mMeasureDCCallback != null) {
                                                    DcTracker.this.log("WLAN+ EVENT_DATA_RAT_CHANGED release DC befor request: mMeasureDataState=" + DcTracker.mMeasureDataState);
                                                    try {
                                                        DcTracker.this.mCm.unregisterNetworkCallback(DcTracker.mMeasureDCCallback);
                                                    } catch (IllegalArgumentException e) {
                                                        DcTracker.this.log("WLAN+ " + e.toString());
                                                    } catch (Exception e2) {
                                                        DcTracker.this.log("WLAN+ Exception:" + e2.toString());
                                                    }
                                                }
                                                DcTracker.mMeasureDCCallback = new NetworkCallback();
                                                DcTracker.this.mCm.requestNetwork(request, DcTracker.mMeasureDCCallback);
                                                DcTracker.this.mCm.measureDataState(DcTracker.this.mPhone.getServiceStateTracker().getSignalLevel());
                                            }
                                        }
                                    }
                                }.start();
                                return;
                            }
                            return;
                        }
                        return;
                    }
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
            case 270379:
                onDeviceProvisionedChange();
                return;
            case 270380:
                String url = msg.obj;
                log("dataConnectionTracker.handleMessage: EVENT_REDIRECTION_DETECTED=" + url);
                onDataConnectionRedirected(url);
                break;
            case 270381:
                handlePcoData((AsyncResult) msg.obj);
                return;
            case 270382:
                onSetCarrierDataEnabled((AsyncResult) msg.obj);
                return;
            case 270383:
                onDataReconnect(msg.getData());
                return;
            case 270437:
                AsyncResult arscreen = msg.obj;
                if (arscreen != null) {
                    this.mIsScreenOn = ((Boolean) arscreen.result).booleanValue();
                } else {
                    log("leon EVENT_OEM_SCREEN_CHANGED error");
                }
                post(new Runnable() {
                    public void run() {
                        DcTracker.this.stopNetStatPoll();
                        DcTracker.this.startNetStatPoll();
                        DcTracker.this.restartDataStallAlarm();
                    }
                });
                return;
            case 270438:
                mDelayMeasure = false;
                log("WLAN+ handlemessage CMD_DELAY_SETUP_DATA mDelayMeasure:false");
                setupDataOnConnectableApns(PhoneInternalInterface.REASON_VOICE_CALL_ENDED);
                return;
            default:
                Rlog.e("DcTracker", "Unhandled event=" + msg);
                return;
        }
        onRadioAvailable();
    }

    private int getApnProfileID(String apnType) {
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

    private IccRecords getUiccRecords(int appFamily) {
        return this.mUiccController.getIccRecords(this.mPhone.getPhoneId(), appFamily);
    }

    private void onUpdateIcc() {
        if (this.mUiccController != null) {
            String name;
            int dataRat = this.mPhone.getServiceState().getRilDataRadioTechnology();
            IccRecords newIccRecords = getUiccRecords(UiccController.getFamilyTypeFromRAT(dataRat));
            if (newIccRecords == null) {
                newIccRecords = this.mPhone.getIccRecords();
            }
            StringBuilder append = new StringBuilder().append("onUpdateIcc: dataRat= ").append(dataRat).append(" newIccRecords ");
            if (newIccRecords != null) {
                name = newIccRecords.getClass().getName();
            } else {
                name = null;
            }
            log(append.append(name).toString());
            IccRecords r = (IccRecords) this.mIccRecords.get();
            if (r != newIccRecords) {
                if (r != null) {
                    log("Removing stale icc objects.");
                    r.unregisterForRecordsLoaded(this);
                    this.mIccRecords.set(null);
                }
                if (newIccRecords == null) {
                    onSimNotReady();
                } else if (this.mSubscriptionManager.isActiveSubId(this.mPhone.getSubId())) {
                    log("New records found.");
                    this.mIccRecords.set(newIccRecords);
                    newIccRecords.registerForRecordsLoaded(this, 270338, null);
                }
            }
        }
    }

    public void update() {
        log("update sub = " + this.mPhone.getSubId());
        log("update(): Active DDS, register for all events now!");
        onUpdateIcc();
        this.mDataEnabledSettings.setUserDataEnabled(getDataEnabled());
        this.mAutoAttachOnCreation.set(false);
        ((GsmCdmaPhone) this.mPhone).updateCurrentCarrierInProvider();
    }

    public void cleanUpAllConnections(String cause) {
        cleanUpAllConnections(cause, null);
    }

    public void updateRecords() {
        onUpdateIcc();
    }

    public void cleanUpAllConnections(String cause, Message disconnectAllCompleteMsg) {
        log("cleanUpAllConnections");
        if (disconnectAllCompleteMsg != null) {
            this.mDisconnectAllCompleteMsgList.add(disconnectAllCompleteMsg);
        }
        Message msg = obtainMessage(270365);
        msg.obj = cause;
        sendMessage(msg);
    }

    private void notifyDataDisconnectComplete() {
        log("notifyDataDisconnectComplete");
        for (Message m : this.mDisconnectAllCompleteMsgList) {
            m.sendToTarget();
        }
        this.mDisconnectAllCompleteMsgList.clear();
    }

    private void notifyAllDataDisconnected() {
        sEnableFailFastRefCounter = 0;
        this.mFailFast = false;
        this.mAllDataDisconnectedRegistrants.notifyRegistrants();
    }

    public void registerForAllDataDisconnected(Handler h, int what, Object obj) {
        this.mAllDataDisconnectedRegistrants.addUnique(h, what, obj);
        if (isDisconnected()) {
            log("notify All Data Disconnected");
            notifyAllDataDisconnected();
        }
    }

    public void unregisterForAllDataDisconnected(Handler h) {
        this.mAllDataDisconnectedRegistrants.remove(h);
    }

    public void registerForDataEnabledChanged(Handler h, int what, Object obj) {
        this.mDataEnabledSettings.registerForDataEnabledChanged(h, what, obj);
    }

    public void unregisterForDataEnabledChanged(Handler h) {
        this.mDataEnabledSettings.unregisterForDataEnabledChanged(h);
    }

    private void onSetInternalDataEnabled(boolean enabled, Message onCompleteMsg) {
        synchronized (this.mDataEnabledSettings) {
            log("onSetInternalDataEnabled: enabled=" + enabled);
            boolean sendOnComplete = true;
            this.mDataEnabledSettings.setInternalDataEnabled(enabled);
            if (enabled) {
                log("onSetInternalDataEnabled: changed to enabled, try to setup data call");
                onTrySetupData(PhoneInternalInterface.REASON_DATA_ENABLED);
            } else {
                sendOnComplete = false;
                log("onSetInternalDataEnabled: changed to disabled, cleanUpAllConnections");
                cleanUpAllConnections(PhoneInternalInterface.REASON_DATA_DISABLED, onCompleteMsg);
            }
            if (sendOnComplete && onCompleteMsg != null) {
                onCompleteMsg.sendToTarget();
            }
        }
    }

    public boolean setInternalDataEnabled(boolean enable) {
        return setInternalDataEnabled(enable, null);
    }

    public boolean setInternalDataEnabled(boolean enable, Message onCompleteMsg) {
        log("setInternalDataEnabled(" + enable + ")");
        Message msg = obtainMessage(270363, onCompleteMsg);
        msg.arg1 = enable ? 1 : 0;
        sendMessage(msg);
        return true;
    }

    protected void log(String s) {
        Rlog.d(this.LOG_TAG, "[" + this.mPhone.getPhoneId() + "]" + s);
    }

    protected void loge(String s) {
        Rlog.e(this.LOG_TAG, "[" + this.mPhone.getPhoneId() + "]" + s);
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
        pw.println(" mDataStallDetectionEnabled=" + this.mDataStallDetectionEnabled);
        pw.println(" mSentSinceLastRecv=" + this.mSentSinceLastRecv);
        pw.println(" mNoRecvPollCount=" + this.mNoRecvPollCount);
        pw.println(" mResolver=" + this.mResolver);
        pw.println(" mIsWifiConnected=" + this.mIsWifiConnected);
        pw.println(" mReconnectIntent=" + this.mReconnectIntent);
        pw.println(" mAutoAttachOnCreation=" + this.mAutoAttachOnCreation.get());
        pw.println(" mIsScreenOn=" + this.mIsScreenOn);
        pw.println(" mUniqueIdGenerator=" + this.mUniqueIdGenerator);
        pw.println(" mDataRoamingLeakageLog= ");
        this.mDataRoamingLeakageLog.dump(fd, pw, args);
        pw.flush();
        pw.println(" ***************************************");
        DcController dcc = this.mDcc;
        if (dcc != null) {
            dcc.dump(fd, pw, args);
        } else {
            pw.println(" mDcc=null");
        }
        pw.println(" ***************************************");
        if (this.mDataConnections != null) {
            Set<Entry<Integer, DataConnection>> mDcSet = this.mDataConnections.entrySet();
            pw.println(" mDataConnections: count=" + mDcSet.size());
            for (Entry<Integer, DataConnection> entry : mDcSet) {
                pw.printf(" *** mDataConnection[%d] \n", new Object[]{entry.getKey()});
                ((DataConnection) entry.getValue()).dump(fd, pw, args);
            }
        } else {
            pw.println("mDataConnections=null");
        }
        pw.println(" ***************************************");
        pw.flush();
        HashMap<String, Integer> apnToDcId = this.mApnToDataConnectionId;
        if (apnToDcId != null) {
            Set<Entry<String, Integer>> apnToDcIdSet = apnToDcId.entrySet();
            pw.println(" mApnToDataConnectonId size=" + apnToDcIdSet.size());
            for (Entry<String, Integer> entry2 : apnToDcIdSet) {
                pw.printf(" mApnToDataConnectonId[%s]=%d\n", new Object[]{entry2.getKey(), entry2.getValue()});
            }
        } else {
            pw.println("mApnToDataConnectionId=null");
        }
        pw.println(" ***************************************");
        pw.flush();
        ConcurrentHashMap<String, ApnContext> apnCtxs = this.mApnContexts;
        if (apnCtxs != null) {
            Set<Entry<String, ApnContext>> apnCtxsSet = apnCtxs.entrySet();
            pw.println(" mApnContexts size=" + apnCtxsSet.size());
            for (Entry<String, ApnContext> entry3 : apnCtxsSet) {
                ((ApnContext) entry3.getValue()).dump(fd, pw, args);
            }
            pw.println(" ***************************************");
        } else {
            pw.println(" mApnContexts=null");
        }
        pw.flush();
        ArrayList<ApnSetting> apnSettings = this.mAllApnSettings;
        if (apnSettings != null) {
            pw.println(" mAllApnSettings size=" + apnSettings.size());
            for (int i = 0; i < apnSettings.size(); i++) {
                pw.printf(" mAllApnSettings[%d]: %s\n", new Object[]{Integer.valueOf(i), apnSettings.get(i)});
            }
            pw.flush();
        } else {
            pw.println(" mAllApnSettings=null");
        }
        pw.println(" mPreferredApn=" + this.mPreferredApn);
        pw.println(" mIsPsRestricted=" + this.mIsPsRestricted);
        pw.println(" mIsDisposed=" + this.mIsDisposed);
        pw.println(" mIntentReceiver=" + this.mIntentReceiver);
        pw.println(" mReregisterOnReconnectFailure=" + this.mReregisterOnReconnectFailure);
        pw.println(" canSetPreferApn=" + this.mCanSetPreferApn);
        pw.println(" mApnObserver=" + this.mApnObserver);
        pw.println(" getOverallState=" + getOverallState());
        pw.println(" mDataConnectionAsyncChannels=%s\n" + this.mDataConnectionAcHashMap);
        pw.println(" mAttached=" + this.mAttached.get());
        pw.flush();
    }

    public String[] getPcscfAddress(String apnType) {
        log("getPcscfAddress()");
        if (apnType == null) {
            log("apnType is null, return null");
            return null;
        }
        ApnContext apnContext;
        if (TextUtils.equals(apnType, "emergency")) {
            apnContext = (ApnContext) this.mApnContextsById.get(9);
        } else if (TextUtils.equals(apnType, "ims")) {
            apnContext = (ApnContext) this.mApnContextsById.get(5);
        } else {
            log("apnType is invalid, return null");
            return null;
        }
        if (apnContext == null) {
            log("apnContext is null, return null");
            return null;
        }
        DcAsyncChannel dcac = apnContext.getDcAc();
        if (dcac == null) {
            return null;
        }
        String[] result = dcac.getPcscfAddr();
        for (int i = 0; i < result.length; i++) {
            log("Pcscf[" + i + "]: " + result[i]);
        }
        return result;
    }

    private void initEmergencyApnSetting() {
        Cursor cursor = this.mPhone.getContext().getContentResolver().query(Carriers.CONTENT_URI, null, "type=\"emergency\"", null, null);
        if (cursor != null) {
            if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                this.mEmergencyApn = makeApnSetting(cursor);
            }
            cursor.close();
        }
    }

    protected void addEmergencyApnSetting() {
        if (this.mEmergencyApn == null) {
            return;
        }
        if (this.mAllApnSettings == null) {
            this.mAllApnSettings = new ArrayList();
            return;
        }
        boolean hasEmergencyApn = false;
        for (ApnSetting apn : this.mAllApnSettings) {
            if (ArrayUtils.contains(apn.types, "emergency")) {
                hasEmergencyApn = true;
                break;
            }
        }
        if (hasEmergencyApn) {
            log("addEmergencyApnSetting - E-APN setting is already present");
        } else {
            this.mAllApnSettings.add(this.mEmergencyApn);
        }
    }

    private boolean containsAllApns(ArrayList<ApnSetting> oldApnList, ArrayList<ApnSetting> newApnList) {
        for (ApnSetting newApnSetting : newApnList) {
            boolean canHandle = false;
            for (ApnSetting oldApnSetting : oldApnList) {
                if (oldApnSetting.equals(newApnSetting, this.mPhone.getServiceState().getDataRoamingFromRegistration())) {
                    canHandle = true;
                    continue;
                    break;
                }
            }
            if (!canHandle) {
                return false;
            }
        }
        return true;
    }

    protected void cleanUpConnectionsOnUpdatedApns(boolean tearDown, String reason) {
        log("cleanUpConnectionsOnUpdatedApns: tearDown=" + tearDown);
        if (this.mAllApnSettings == null || !this.mAllApnSettings.isEmpty()) {
            for (ApnContext apnContext : this.mApnContexts.values()) {
                ArrayList<ApnSetting> currentWaitingApns = apnContext.getWaitingApns();
                ArrayList<ApnSetting> waitingApns = buildWaitingApns(apnContext.getApnType(), this.mPhone.getServiceState().getRilDataRadioTechnology());
                if (!(currentWaitingApns == null || (waitingApns.size() == currentWaitingApns.size() && (containsAllApns(currentWaitingApns, waitingApns) ^ 1) == 0))) {
                    apnContext.setWaitingApns(waitingApns);
                    if (!apnContext.isDisconnected()) {
                        apnContext.setReason(reason);
                        cleanUpConnection(true, apnContext);
                    }
                }
            }
        } else {
            cleanUpAllConnections(tearDown, PhoneInternalInterface.REASON_APN_CHANGED);
        }
        if (!isConnected()) {
            stopNetStatPoll();
            stopDataStallAlarm();
        }
        this.mRequestedApnType = "default";
        log("mDisconnectPendingCount = " + this.mDisconnectPendingCount);
        if (tearDown && this.mDisconnectPendingCount == 0) {
            notifyDataDisconnectComplete();
            notifyAllDataDisconnected();
        }
    }

    private void resetPollStats() {
        this.mTxPkts = -1;
        this.mRxPkts = -1;
        this.mNetStatPollPeriod = 1000;
    }

    private void startNetStatPoll() {
        if (getOverallState() == State.CONNECTED && !this.mNetStatPollEnabled) {
            log("startNetStatPoll");
            resetPollStats();
            this.mNetStatPollEnabled = true;
            this.mPollNetStat.run();
        }
        if (this.mPhone != null) {
            this.mPhone.notifyDataActivity();
        }
    }

    private void stopNetStatPoll() {
        this.mNetStatPollEnabled = false;
        removeCallbacks(this.mPollNetStat);
        log("stopNetStatPoll");
        if (this.mPhone != null) {
            this.mPhone.notifyDataActivity();
        }
    }

    public void sendStartNetStatPoll(Activity activity) {
        Message msg = obtainMessage(270376);
        msg.arg1 = 1;
        msg.obj = activity;
        sendMessage(msg);
    }

    private void handleStartNetStatPoll(Activity activity) {
        startNetStatPoll();
        startDataStallAlarm(false);
        setActivity(activity);
    }

    public void sendStopNetStatPoll(Activity activity) {
        Message msg = obtainMessage(270376);
        msg.arg1 = 0;
        msg.obj = activity;
        sendMessage(msg);
    }

    private void handleStopNetStatPoll(Activity activity) {
        stopNetStatPoll();
        stopDataStallAlarm();
        setActivity(activity);
    }

    private void updateDataActivity() {
        TxRxSum preTxRxSum = new TxRxSum(this.mTxPkts, this.mRxPkts);
        TxRxSum curTxRxSum = new TxRxSum();
        curTxRxSum.updateTxRxSum();
        this.mTxPkts = curTxRxSum.txPkts;
        this.mRxPkts = curTxRxSum.rxPkts;
        if (!this.mNetStatPollEnabled) {
            return;
        }
        if (preTxRxSum.txPkts > 0 || preTxRxSum.rxPkts > 0) {
            long sent = this.mTxPkts - preTxRxSum.txPkts;
            long received = this.mRxPkts - preTxRxSum.rxPkts;
            Activity newActivity = (sent <= 0 || received <= 0) ? (sent <= 0 || received != 0) ? (sent != 0 || received <= 0) ? this.mActivity == Activity.DORMANT ? this.mActivity : Activity.NONE : Activity.DATAIN : Activity.DATAOUT : Activity.DATAINANDOUT;
            if (this.mActivity != newActivity && this.mIsScreenOn) {
                this.mActivity = newActivity;
                this.mPhone.notifyDataActivity();
            }
        }
    }

    private void handlePcoData(AsyncResult ar) {
        if (ar.exception != null) {
            Rlog.e(this.LOG_TAG, "PCO_DATA exception: " + ar.exception);
            return;
        }
        PcoData pcoData = ar.result;
        ArrayList<DataConnection> dcList = new ArrayList();
        DataConnection temp = this.mDcc.getActiveDcByCid(pcoData.cid);
        if (temp != null) {
            dcList.add(temp);
        }
        if (dcList.size() == 0) {
            Rlog.e(this.LOG_TAG, "PCO_DATA for unknown cid: " + pcoData.cid + ", inferring");
            for (DataConnection dc : this.mDataConnections.values()) {
                int cid = dc.getCid();
                if (cid == pcoData.cid) {
                    dcList.clear();
                    dcList.add(dc);
                    break;
                } else if (cid == -1) {
                    for (ApnContext apnContext : dc.mApnContexts.keySet()) {
                        if (apnContext.getState() == State.CONNECTING) {
                            dcList.add(dc);
                            break;
                        }
                    }
                }
            }
        }
        if (dcList.size() == 0) {
            Rlog.e(this.LOG_TAG, "PCO_DATA - couldn't infer cid");
            return;
        }
        for (DataConnection dc2 : dcList) {
            if (dc2.mApnContexts.size() == 0) {
                break;
            }
            for (ApnContext apnContext2 : dc2.mApnContexts.keySet()) {
                String apnType = apnContext2.getApnType();
                Intent intent = new Intent("com.android.internal.telephony.CARRIER_SIGNAL_PCO_VALUE");
                intent.putExtra("apnType", apnType);
                intent.putExtra("apnProto", pcoData.bearerProto);
                intent.putExtra("pcoId", pcoData.pcoId);
                intent.putExtra("pcoValue", pcoData.contents);
                this.mPhone.getCarrierSignalAgent().notifyCarrierSignalReceivers(intent);
            }
        }
    }

    private int getRecoveryAction() {
        return System.getInt(this.mResolver, "radio.data.stall.recovery.action", 0);
    }

    private void putRecoveryAction(int action) {
        System.putInt(this.mResolver, "radio.data.stall.recovery.action", action);
    }

    private void doRecovery() {
        if (getOverallState() == State.CONNECTED) {
            int recoveryAction = getRecoveryAction();
            TelephonyMetrics.getInstance().writeDataStallEvent(this.mPhone.getPhoneId(), recoveryAction);
            switch (recoveryAction) {
                case 0:
                    EventLog.writeEvent(EventLogTags.DATA_STALL_RECOVERY_GET_DATA_CALL_LIST, this.mSentSinceLastRecv);
                    log("doRecovery() get data call list");
                    this.mPhone.mCi.getDataCallList(obtainMessage(270340));
                    putRecoveryAction(1);
                    break;
                case 1:
                    EventLog.writeEvent(EventLogTags.DATA_STALL_RECOVERY_CLEANUP, this.mSentSinceLastRecv);
                    log("doRecovery() cleanup all connections");
                    cleanUpAllConnections(PhoneInternalInterface.REASON_PDP_RESET);
                    putRecoveryAction(2);
                    break;
                case 2:
                    EventLog.writeEvent(EventLogTags.DATA_STALL_RECOVERY_REREGISTER, this.mSentSinceLastRecv);
                    log("doRecovery() re-register");
                    this.mPhone.getServiceStateTracker().reRegisterNetwork(null);
                    putRecoveryAction(3);
                    recordDataStallInfo();
                    break;
                case 3:
                    EventLog.writeEvent(EventLogTags.DATA_STALL_RECOVERY_RADIO_RESTART, this.mSentSinceLastRecv);
                    log("restarting radio");
                    putRecoveryAction(4);
                    restartRadio();
                    break;
                case 4:
                    EventLog.writeEvent(EventLogTags.DATA_STALL_RECOVERY_RADIO_RESTART_WITH_PROP, -1);
                    log("restarting radio with gsm.radioreset to true");
                    SystemProperties.set(this.RADIO_RESET_PROPERTY, "true");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                    restartRadio();
                    putRecoveryAction(0);
                    break;
                default:
                    log("doRecovery: Invalid recoveryAction=" + recoveryAction);
                    break;
            }
            this.mSentSinceLastRecv = 0;
        }
    }

    private void updateDataStallInfo() {
        TxRxSum preTxRxSum = new TxRxSum(this.mDataStallTxRxSum);
        this.mDataStallTxRxSum.updateTxRxSum();
        long sent = this.mDataStallTxRxSum.txPkts - preTxRxSum.txPkts;
        long received = this.mDataStallTxRxSum.rxPkts - preTxRxSum.rxPkts;
        if (sent > 0 && received > 0) {
            this.mSentSinceLastRecv = 0;
            putRecoveryAction(0);
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
            putRecoveryAction(0);
            this.mHasInboundData = true;
        }
    }

    private boolean isPhoneStateIdle() {
        int i = 0;
        while (i < TelephonyManager.getDefault().getPhoneCount()) {
            Phone phone = PhoneFactory.getPhone(i);
            if (phone == null || phone.getState() == PhoneConstants.State.IDLE) {
                i++;
            } else {
                log("isPhoneStateIdle: Voice call active on sub: " + i);
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
        updateDataStallInfo();
        boolean suspectedStall = false;
        if (this.mSentSinceLastRecv >= ((long) Global.getInt(this.mResolver, "pdp_watchdog_trigger_packet_count", 10))) {
            log("onDataStallAlarm: tag=" + tag + " do recovery action=" + getRecoveryAction());
            suspectedStall = true;
            sendMessage(obtainMessage(270354));
        }
        startDataStallAlarm(suspectedStall);
    }

    private void startDataStallAlarm(boolean suspectedStall) {
        int nextAction = getRecoveryAction();
        if (this.mDataStallDetectionEnabled && getOverallState() == State.CONNECTED) {
            int delayInMs;
            if (this.mIsScreenOn || suspectedStall || RecoveryAction.isAggressiveRecovery(nextAction)) {
                delayInMs = Global.getInt(this.mResolver, "data_stall_alarm_aggressive_delay_in_ms", 60000);
            } else {
                delayInMs = Global.getInt(this.mResolver, "data_stall_alarm_non_aggressive_delay_in_ms", DATA_STALL_ALARM_NON_AGGRESSIVE_DELAY_IN_MS_DEFAULT);
            }
            this.mDataStallAlarmTag++;
            Intent intent = new Intent(INTENT_DATA_STALL_ALARM);
            intent.putExtra(DATA_STALL_ALARM_TAG_EXTRA, this.mDataStallAlarmTag);
            this.mDataStallAlarmIntent = PendingIntent.getBroadcast(this.mPhone.getContext(), 0, intent, 134217728);
            this.mAlarmManager.set(3, SystemClock.elapsedRealtime() + ((long) delayInMs), this.mDataStallAlarmIntent);
        }
    }

    private void stopDataStallAlarm() {
        this.mDataStallAlarmTag++;
        if (this.mDataStallAlarmIntent != null) {
            this.mAlarmManager.cancel(this.mDataStallAlarmIntent);
            this.mDataStallAlarmIntent = null;
        }
    }

    private void restartDataStallAlarm() {
        if (!isConnected()) {
            return;
        }
        if (RecoveryAction.isAggressiveRecovery(getRecoveryAction())) {
            log("restartDataStallAlarm: action is pending. not resetting the alarm.");
            return;
        }
        stopDataStallAlarm();
        startDataStallAlarm(false);
    }

    private void onActionIntentProvisioningApnAlarm(Intent intent) {
        log("onActionIntentProvisioningApnAlarm: action=" + intent.getAction());
        Message msg = obtainMessage(270375, intent.getAction());
        msg.arg1 = intent.getIntExtra(PROVISIONING_APN_ALARM_TAG_EXTRA, 0);
        sendMessage(msg);
    }

    private void startProvisioningApnAlarm() {
        int delayInMs = Global.getInt(this.mResolver, "provisioning_apn_alarm_delay_in_ms", PROVISIONING_APN_ALARM_DELAY_IN_MS_DEFAULT);
        if (Build.IS_DEBUGGABLE) {
            try {
                delayInMs = Integer.parseInt(System.getProperty(DEBUG_PROV_APN_ALARM, Integer.toString(delayInMs)));
            } catch (NumberFormatException e) {
                loge("startProvisioningApnAlarm: e=" + e);
            }
        }
        this.mProvisioningApnAlarmTag++;
        log("startProvisioningApnAlarm: tag=" + this.mProvisioningApnAlarmTag + " delay=" + (delayInMs / 1000) + "s");
        Intent intent = new Intent(INTENT_PROVISIONING_APN_ALARM);
        intent.putExtra(PROVISIONING_APN_ALARM_TAG_EXTRA, this.mProvisioningApnAlarmTag);
        this.mProvisioningApnAlarmIntent = PendingIntent.getBroadcast(this.mPhone.getContext(), 0, intent, 134217728);
        this.mAlarmManager.set(2, SystemClock.elapsedRealtime() + ((long) delayInMs), this.mProvisioningApnAlarmIntent);
    }

    private void stopProvisioningApnAlarm() {
        log("stopProvisioningApnAlarm: current tag=" + this.mProvisioningApnAlarmTag + " mProvsioningApnAlarmIntent=" + this.mProvisioningApnAlarmIntent);
        this.mProvisioningApnAlarmTag++;
        if (this.mProvisioningApnAlarmIntent != null) {
            this.mAlarmManager.cancel(this.mProvisioningApnAlarmIntent);
            this.mProvisioningApnAlarmIntent = null;
        }
    }

    protected String getCellLocation() {
        int phoneId = this.mPhone.getPhoneId();
        String mccMnc = SpnOverride.MVNO_TYPE_NONE;
        String prop = SystemProperties.get("gsm.operator.numeric", SpnOverride.MVNO_TYPE_NONE);
        if (prop != null && prop.length() > 0) {
            String[] values = prop.split(",");
            if (phoneId >= 0 && phoneId < values.length && values[phoneId] != null) {
                mccMnc = values[phoneId];
            }
        }
        int mcc = 0;
        int mnc = 0;
        if (mccMnc != null) {
            try {
                if (mccMnc.length() >= 3) {
                    mcc = Integer.parseInt(mccMnc.substring(0, 3));
                    mnc = Integer.parseInt(mccMnc.substring(3));
                }
            } catch (Exception e) {
                log("couldn't parse mcc/mnc: " + mccMnc);
            }
        }
        if (mcc == 460) {
            if (mnc == 2 || mnc == 7 || mnc == 8) {
                mnc = 0;
            }
            if (mnc == 6 || mnc == 9) {
                mnc = 1;
            }
            if (mnc == 3) {
                mnc = 11;
            }
        }
        String loc = "MCC:" + mcc + ", MNC:" + mnc;
        if (this.mPhone != null) {
            CellLocation cell = this.mPhone.getCellLocation();
            if (cell instanceof GsmCellLocation) {
                loc = loc + ", LAC:" + ((GsmCellLocation) cell).getLac() + ", CID:" + ((GsmCellLocation) cell).getCid();
            } else if (cell instanceof CdmaCellLocation) {
                loc = loc + ", SID:" + ((CdmaCellLocation) cell).getSystemId() + ", NID:" + ((CdmaCellLocation) cell).getNetworkId() + ", BID:" + ((CdmaCellLocation) cell).getBaseStationId();
            }
            SignalStrength signal = this.mPhone.getSignalStrength();
            if (signal != null) {
                loc = loc + ", signalstrength:" + signal.getDbm() + ", signallevel:" + signal.getLevel();
            }
        }
        log("getCellLocation:" + loc);
        return loc;
    }

    protected void recordDataStallInfo() {
        if (this.mPhone.getServiceState().getDataRegState() == 0 && this.mHasInboundData) {
            String error_info = getCellLocation() + ", nwType:" + TelephonyManager.getDefault().getNetworkType() + ", mSentSinceLastRecv:" + this.mSentSinceLastRecv;
            int log_type = -1;
            String log_desc = SpnOverride.MVNO_TYPE_NONE;
            try {
                String[] log_array = this.mPhone.getContext().getString(this.mPhone.getContext().getResources().getIdentifier("zz_oppo_critical_log_113", "string", "android")).split(",");
                log_type = Integer.valueOf(log_array[0]).intValue();
                log_desc = log_array[1];
            } catch (Exception e) {
            }
            this.mHasInboundData = false;
            OppoManager.writeLogToPartition(log_type, error_info, "NETWORK", RIL.ISSUE_SYS_OEM_NW_DIAG_CAUSE_DATA_STALL_ERROR, log_desc);
        }
    }

    public String getOperatorNumeric() {
        String result;
        if (isNvSubscription()) {
            result = SystemProperties.get(GsmCdmaPhone.PROPERTY_CDMA_HOME_OPERATOR_NUMERIC);
            log("getOperatorNumberic - returning from NV: " + result);
            return result;
        }
        IccRecords r = (IccRecords) this.mIccRecords.get();
        result = r != null ? r.getOperatorNumeric() : SpnOverride.MVNO_TYPE_NONE;
        log("getOperatorNumberic - returning from card: " + result);
        return result;
    }

    private String[] getDunApnByMccMnc(Context context) {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        String operator = r != null ? r.getOperatorNumeric() : SpnOverride.MVNO_TYPE_NONE;
        int mcc = 0;
        int mnc = 0;
        if (operator != null && operator.length() > 3) {
            mcc = Integer.parseInt(operator.substring(0, 3));
            mnc = Integer.parseInt(operator.substring(3, operator.length()));
        }
        Resources sysResource = context.getResources();
        int sysMcc = sysResource.getConfiguration().mcc;
        int sysMnc = sysResource.getConfiguration().mnc;
        log("fetchDunApn: sys mcc=" + sysMcc + ", mnc=" + sysMnc);
        Resources resource = null;
        try {
            log("getResourcesUsingMccMnc: mcc = " + mcc + ", mnc = " + mnc);
            Configuration configuration = new Configuration();
            configuration = context.getResources().getConfiguration();
            configuration.mcc = mcc;
            configuration.mnc = mnc;
            resource = context.createConfigurationContext(configuration).getResources();
        } catch (Exception e) {
            e.printStackTrace();
            log("getResourcesUsingMccMnc fail");
        }
        if (TelephonyManager.getDefault().getSimCount() == 1 || ((mcc == sysMcc && mnc == sysMnc) || resource == null)) {
            return sysResource.getStringArray(17236042);
        }
        log("fetchDunApn: get resource from mcc=" + mcc + ", mnc=" + mnc);
        return resource.getStringArray(17236042);
    }

    protected ApnSetting getDunApnFromCache() {
        IccRecords r = (IccRecords) this.mIccRecords.get();
        String operator = r != null ? r.getOperatorNumeric() : SpnOverride.MVNO_TYPE_NONE;
        for (String equals : sTelstraOperaters) {
            if (equals.equals(operator)) {
                if (!(this.mAllApnSettings == null || (this.mAllApnSettings.isEmpty() ^ 1) == 0)) {
                    log("getDunApnFromCache: mAllApnSettings=" + this.mAllApnSettings);
                    for (ApnSetting apn : this.mAllApnSettings) {
                        if (apn.canHandleType("dun")) {
                            log("getDunApnFromCache: operator:" + operator + " apn:" + apn);
                            return apn;
                        }
                    }
                }
                log("getDunApnFromCache: get DUN apn from the default config!!! operator:" + operator);
                return null;
            }
        }
        log("getDunApnFromCache: get DUN apn from the default config!!! operator:" + operator);
        return null;
    }

    public boolean haveVsimIgnoreUserDataSetting() {
        return mVsimIgnoreUserDataSetting ? SubscriptionManager.isVsimEnabled(this.mPhone.getSubId()) : false;
    }

    protected boolean needManualSelectAPN(String operator) {
        Object obj = null;
        if (operator != null) {
            Cursor cursor = null;
            try {
                String selection = "numeric = '" + operator + "'";
                log("isOppoManualSelectAPN: selection=" + selection);
                cursor = this.mPhone.getContext().getContentResolver().query(Carriers.CONTENT_URI, null, selection, null, "_id");
                if (cursor != null && cursor.moveToFirst()) {
                    while (true) {
                        obj = cursor.getString(cursor.getColumnIndexOrThrow("oppo_manual_select"));
                        if (!"1".equals(obj)) {
                            if (!cursor.moveToNext()) {
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {
                log("needManualSelectAPNException:" + e);
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return "1".equals(obj);
    }

    public boolean isTelstraSim() {
        boolean isTelstraSim = false;
        IccRecords r = (IccRecords) this.mIccRecords.get();
        String operator = r != null ? r.getOperatorNumeric() : SpnOverride.MVNO_TYPE_NONE;
        for (String equals : sTelstraOperaters) {
            if (equals.equals(operator)) {
                isTelstraSim = true;
                break;
            }
        }
        log("isTelstraSim isTelstraSim:" + isTelstraSim);
        return isTelstraSim;
    }

    public void registerOnImsCallStateChange() {
        if (this.mPhone != null && this.mPhone.getImsPhone() != null && this.mPhone.getImsPhone().getCallTracker() != null) {
            log("register on ims call state");
            this.mPhone.getImsPhone().getCallTracker().registerForVoiceCallEnded(this, 270344, null);
            this.mPhone.getImsPhone().getCallTracker().registerForVoiceCallStarted(this, 270343, null);
        }
    }
}
