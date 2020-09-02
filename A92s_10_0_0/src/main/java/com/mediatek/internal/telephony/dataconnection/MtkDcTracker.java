package com.mediatek.internal.telephony.dataconnection;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.NetworkConfig;
import android.net.NetworkInfo;
import android.net.NetworkStats;
import android.net.Uri;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.OppoManager;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.CarrierConfigManager;
import android.telephony.PcoData;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.data.ApnSetting;
import android.telephony.data.DataProfile;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.DctConstants;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.IOppoDataManager;
import com.android.internal.telephony.OemConstant;
import com.android.internal.telephony.OppoTelephonyFactory;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.SubscriptionController;
import com.android.internal.telephony.TelephonyDevController;
import com.android.internal.telephony.dataconnection.ApnContext;
import com.android.internal.telephony.dataconnection.ApnSettingUtils;
import com.android.internal.telephony.dataconnection.DataConnection;
import com.android.internal.telephony.dataconnection.DataConnectionReasons;
import com.android.internal.telephony.dataconnection.DcTesterFailBringUpAll;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IccUtils;
import com.android.internal.telephony.uicc.RuimRecords;
import com.android.internal.telephony.uicc.SIMRecords;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.util.ArrayUtils;
import com.mediatek.android.mms.pdu.MtkCharacterSets;
import com.mediatek.internal.telephony.IMtkTelephonyEx;
import com.mediatek.internal.telephony.MtkGsmCdmaPhone;
import com.mediatek.internal.telephony.MtkIccCardConstants;
import com.mediatek.internal.telephony.MtkServiceStateTracker;
import com.mediatek.internal.telephony.MtkSubscriptionController;
import com.mediatek.internal.telephony.OpTelephonyCustomizationFactoryBase;
import com.mediatek.internal.telephony.OpTelephonyCustomizationUtils;
import com.mediatek.internal.telephony.RadioManager;
import com.mediatek.internal.telephony.dataconnection.MtkDcHelper;
import com.mediatek.internal.telephony.datasub.DataSubConstants;
import com.mediatek.internal.telephony.ims.MtkDedicateDataCallResponse;
import com.mediatek.internal.telephony.ppl.PplMessageManager;
import com.mediatek.internal.telephony.uicc.MtkIccUtilsEx;
import com.mediatek.internal.telephony.uicc.MtkUiccCardApplication;
import com.mediatek.internal.telephony.uicc.MtkUiccController;
import com.mediatek.internal.telephony.worldphone.WorldPhoneUtil;
import com.mediatek.telephony.internal.telephony.vsim.ExternalSimConstants;
import com.mediatek.telephony.internal.telephony.vsim.ExternalSimManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;
import mediatek.telephony.MtkServiceState;
import mediatek.telephony.data.MtkApnSetting;

public class MtkDcTracker extends DcTracker {
    private static final int APN_CLASS_0 = 0;
    private static final int APN_CLASS_1 = 1;
    private static final int APN_CLASS_2 = 2;
    private static final int APN_CLASS_3 = 3;
    private static final int APN_CLASS_4 = 4;
    private static final int APN_CLASS_5 = 5;
    private static final boolean DBG = true;
    private static final int DEFAULT_DATA_SIM_IDX = 2;
    private static final int DOMESTIC_DATA_ROAMING_IDX = 3;
    private static final String FDN_CONTENT_URI = "content://icc/fdn";
    private static final String FDN_CONTENT_URI_WITH_SUB_ID = "content://icc/fdn/subId/";
    private static final String FDN_FOR_ALLOW_DATA = "*99#";
    private static final String GID1_DEFAULT = "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff";
    private static final int INTERNATIONAL_DATA_ROAMING_IDX = 4;
    private static final String[] KDDI_OPERATOR = {"44007", "44008", "44050", "44051", "44052", "44053", "44054", "44055", "44056", "44070", "44071", "44072", "44073", "44074", "44075", "44076", "44077", "44078", "44079", "44088", "44089", "44170"};
    private static final String LOG_TAG = "MtkDCT";
    private static final int LTE_AS_CONNECTED = 1;
    protected static final String[] MCC_TABLE_DOMESTIC = {"440"};
    protected static final String[] MCC_TABLE_TEST = {"001"};
    private static final int MOBILE_DATA_IDX = 0;
    protected static final boolean MTK_IMS_TESTMODE_SUPPORT;
    private static final String NETWORK_TYPE_MOBILE_IMS = "MOBILEIMS";
    private static final String NETWORK_TYPE_WIFI = "WIFI";
    private static final String OPPO_PAID_SELECT = "oppo_paid_select";
    private static final String[] PRIVATE_APN_OPERATOR = {"732101", "330110", "334020", "71610", "74001", "71403", "73003", "72405", "722310", "37002", "71203", "70401", "70601", "708001", "71021", "74810", "74402"};
    private static final String PROP_APN_CLASS = "vendor.ril.md_changed_apn_class.";
    private static final String PROP_APN_CLASS_ICCID = "vendor.ril.md_changed_apn_class.iccid.";
    private static final String PROP_RIL_DATA_CDMA_IMSI = "vendor.ril.data.cdma_imsi";
    private static final String PROP_RIL_DATA_CDMA_MCC_MNC = "vendor.ril.data.cdma_mcc_mnc";
    private static final String PROP_RIL_DATA_CDMA_SPN = "vendor.ril.data.cdma_spn";
    private static final String PROP_RIL_DATA_GID1 = "vendor.ril.data.gid1-";
    private static final String PROP_RIL_DATA_GSM_IMSI = "vendor.ril.data.gsm_imsi";
    private static final String PROP_RIL_DATA_GSM_MCC_MNC = "vendor.ril.data.gsm_mcc_mnc";
    private static final String PROP_RIL_DATA_GSM_SPN = "vendor.ril.data.gsm_spn";
    private static final String PROP_RIL_DATA_ICCID = "vendor.ril.iccid.sim";
    private static final String PROP_RIL_DATA_PNN = "vendor.ril.data.pnn";
    protected static final int REGION_DOMESTIC = 1;
    protected static final int REGION_FOREIGN = 2;
    protected static final int REGION_UNKNOWN = 0;
    private static final int ROAMING_DATA_IDX = 1;
    private static final int SKIP_DATA_SETTINGS = -2;
    private static final String SKIP_DATA_STALL_ALARM = "persist.vendor.skip.data.stall.alarm";
    private static final String SPRINT_IA_NI = "otasn";
    private static final int THROTTLING_MAX_PDP_SIZE = 8;
    private static final boolean VDBG = Log.isLoggable(LOG_TAG, 3);
    private static final boolean VDBG_STALL = Log.isLoggable(LOG_TAG, 3);
    private static final String VZW_800_NI = "VZW800";
    private static final String VZW_ADMIN_NI = "VZWADMIN";
    private static final String VZW_APP_NI = "VZWAPP";
    private static final String VZW_EMERGENCY_NI = "VZWEMERGENCY";
    private static final String VZW_IMS_NI = "VZWIMS";
    private static final String VZW_INTERNET_NI = "VZWINTERNET";
    private String[] MCCMNC_EE = {"23430"};
    private String[] MCCMNC_OP18 = {"405840", "405854", "405855", "405856", "405857", "405858", "405859", "405860", "405861", "405862", "405863", "405864", "405865", "405866", "405867", "405868", "405869", "405870", "405871", "405872", "405873", "405874"};
    private String[] MCCMNC_TELCEL = {"33402", "334020"};
    private String[] MCCMNC_TELSTRA = {"50501"};
    private String[] PLMN_EMPTY_APN_PCSCF_SET = {"26201", "44010"};
    private String[] PROPERTY_ICCID = {"vendor.ril.iccid.sim1", "vendor.ril.iccid.sim2", "vendor.ril.iccid.sim3", "vendor.ril.iccid.sim4"};
    private boolean mAllowConfig = false;
    private boolean mCcDomesticRoamingEnabled = false;
    private String[] mCcDomesticRoamingSpecifiedNw = null;
    private boolean mCcIntlRoamingEnabled = false;
    private boolean mCcOneSettingForRoaming = false;
    private boolean mCcUniqueSettingsForRoaming = false;
    private IDataConnectionExt mDataConnectionExt = null;
    private DataRetryOperator mDataRetryOperator = DataRetryOperator.UNKNOWN;
    private HandlerThread mDcHandlerThread;
    private int mDedicatedBearerCount = 0;
    private int mDefaultRefCount = 0;
    private int mHandoverApnType = 0;
    private boolean mHasFetchMdAutoSetupImsCapability = false;
    private boolean mHasFetchModemDeactPdnCapabilityForMultiPS = false;
    private ContentObserver mImsSwitchChangeObserver = new ContentObserver(new Handler()) {
        /* class com.mediatek.internal.telephony.dataconnection.MtkDcTracker.AnonymousClass1 */

        public void onChange(boolean selfChange) {
            MtkDcTracker mtkDcTracker = MtkDcTracker.this;
            mtkDcTracker.log("mImsSwitchChangeObserver: onChange=" + selfChange);
            if (MtkDcTracker.this.isOp17IaSupport()) {
                MtkDcTracker.this.log("IA : OP17, set IA");
                MtkDcTracker.this.setInitialAttachApn();
            }
        }
    };
    private String mImsiCdma = null;
    private String mImsiGsm = null;
    protected ApnSetting mInitialAttachApnSetting;
    private final BroadcastReceiver mIntentReceiverEx = new BroadcastReceiver() {
        /* class com.mediatek.internal.telephony.dataconnection.MtkDcTracker.AnonymousClass2 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            MtkDcTracker.this.log("mIntentReceiverEx onReceive: action=" + action);
            if (action.equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                int subId = intent.getIntExtra("subscription", -1);
                MtkDcTracker.this.log("mIntentReceiverEx ACTION_CARRIER_CONFIG_CHANGED: subId=" + subId + ", mPhone.getSubId()=" + MtkDcTracker.this.mPhone.getSubId());
                if (subId == MtkDcTracker.this.mPhone.getSubId()) {
                    MtkDcTracker.this.log("CarrierConfigLoader is loading complete!");
                    MtkDcTracker mtkDcTracker = MtkDcTracker.this;
                    mtkDcTracker.sendMessage(mtkDcTracker.obtainMessage(270854));
                    MtkDcTracker.this.loadCarrierConfig(subId);
                    return;
                }
                return;
            }
            boolean enabled = true;
            if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
                if (!MtkDcTracker.this.hasOperatorIaCapability()) {
                    NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    int apnType = networkInfo.getType();
                    String typeName = networkInfo.getTypeName();
                    MtkDcTracker.this.logd("onReceive: ConnectivityService action change apnType = " + apnType + " typename =" + typeName);
                    if (apnType == 11 && typeName.equals(MtkDcTracker.NETWORK_TYPE_WIFI)) {
                        MtkDcTracker.this.onAttachApnChangedByHandover(true);
                    } else if (apnType == 11 && typeName.equals(MtkDcTracker.NETWORK_TYPE_MOBILE_IMS)) {
                        MtkDcTracker.this.onAttachApnChangedByHandover(false);
                    }
                }
            } else if (action.equals("com.mediatek.common.carrierexpress.operator_config_changed")) {
                MtkDcTracker.this.reloadOpCustomizationFactory();
            } else if (action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
                MtkDcTracker.this.log("Wifi state changed");
                if (intent.getIntExtra("wifi_state", 4) != 3) {
                    enabled = false;
                }
                MtkDcTracker.this.onWifiStateChanged(enabled);
            } else {
                MtkDcTracker.this.log("onReceive: Unknown action=" + action);
            }
        }
    };
    private boolean mIsAddMnoApnsIntoAllApnList = false;
    private boolean mIsFdnChecked = false;
    private boolean mIsImsHandover = false;
    private boolean mIsLte = false;
    private boolean mIsMatchFdnForAllowData = false;
    private boolean mIsModemReset = false;
    private boolean mIsNotifyDataAttached = false;
    private boolean mIsOperatorNumericEmpty = false;
    private boolean mIsPhbStateChangedIntentRegistered = false;
    private boolean mIsRecordsOverride = false;
    private boolean mIsSharedDefaultApn = false;
    private boolean mIsSimNotReady = false;
    private boolean mIsSimRefresh = false;
    private boolean mIsSupportConcurrent = false;
    private String mLteAccessStratumDataState = "unknown";
    private boolean mMdAutoSetupImsCapability = false;
    private ApnSetting mMdChangedAttachApn = null;
    private volatile NetworkStats mMobileDataUsage = null;
    private boolean mModemDeactPdnCapabilityForMultiPS = false;
    protected boolean mNeedsResumeModem = false;
    protected Object mNeedsResumeModemLock = new Object();
    private int mNetworkType = -1;
    private boolean mPendingDataCall = false;
    private BroadcastReceiver mPhbStateChangedIntentReceiver = new BroadcastReceiver() {
        /* class com.mediatek.internal.telephony.dataconnection.MtkDcTracker.AnonymousClass4 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            MtkDcTracker mtkDcTracker = MtkDcTracker.this;
            mtkDcTracker.log("onReceive: action=" + action);
            if (action.equals("mediatek.intent.action.PHB_STATE_CHANGED")) {
                boolean bPhbReady = intent.getBooleanExtra("ready", false);
                MtkDcTracker mtkDcTracker2 = MtkDcTracker.this;
                mtkDcTracker2.log("bPhbReady: " + bPhbReady);
                if (bPhbReady) {
                    MtkDcTracker.this.onFdnChanged();
                }
            }
        }
    };
    private int mPhoneType = 0;
    public ArrayList<ApnContext> mPrioritySortedApnContextsEx;
    protected int mRegion = 0;
    private int mRilRat = 0;
    protected int mSuspendId = 0;
    private TelephonyDevController mTelDevController = TelephonyDevController.getInstance();
    private OpTelephonyCustomizationFactoryBase mTelephonyCustomizationFactory = null;
    private MtkServiceState mTurboSS = null;
    private AtomicReference<UiccCardApplication> mUiccCardApplication;
    /* access modifiers changed from: private */
    public Handler mWorkerHandler;

    private enum DataRetryOperator {
        UNKNOWN,
        TELCEL,
        TELSTRA,
        EE
    }

    static {
        boolean z = false;
        if (SystemProperties.getInt("persist.vendor.radio.imstestmode", 0) == 1) {
            z = true;
        }
        MTK_IMS_TESTMODE_SUPPORT = z;
    }

    public MtkDcTracker(Phone phone, int transportType) {
        super(phone, transportType);
        this.mDataServiceManager.setIwlanDataServiceClassName("com.mediatek.internal.telephony.dataconnection.IwlanDataService");
        reloadOpCustomizationFactory();
        if (!hasOperatorIaCapability()) {
            phone.getContext().getContentResolver().registerContentObserver(Settings.Global.getUriFor("volte_vt_enabled"), true, this.mImsSwitchChangeObserver);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("com.mediatek.common.carrierexpress.operator_config_changed");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        this.mPhone.getContext().registerReceiver(this.mIntentReceiverEx, filter, null, this.mPhone);
        if (TelephonyManager.getDefault().getPhoneCount() == 1) {
            this.mAllowConfig = true;
        }
        this.mMobileDataUsage = new NetworkStats(SystemClock.elapsedRealtime(), 1);
        createWorkerHandler();
        setSscMode();
    }

    public void registerServiceStateTrackerEvents() {
        MtkDcTracker.super.registerServiceStateTrackerEvents();
        this.mPhone.getServiceStateTracker().registerForDataRoamingTypeChange(this, 270850, null);
    }

    public void unregisterServiceStateTrackerEvents() {
        MtkDcTracker.super.unregisterServiceStateTrackerEvents();
        this.mPhone.getServiceStateTracker().unregisterForDataRoamingTypeChange(this);
    }

    /* access modifiers changed from: protected */
    public void registerForAllEvents() {
        MtkDcTracker.super.registerForAllEvents();
        this.mPhone.getCallTracker().unregisterForVoiceCallEnded(this);
        this.mPhone.getCallTracker().unregisterForVoiceCallStarted(this);
        logd("registerForAllEvents: mPhone = " + this.mPhone);
        this.mPhone.mCi.registerForRemoveRestrictEutran(this, 270852, null);
        this.mPhone.mCi.registerForMdDataRetryCountReset(this, 270851, null);
        if (!hasOperatorIaCapability()) {
            if (!WorldPhoneUtil.isWorldPhoneSupport() && !DataSubConstants.OPERATOR_OP01.equals(SystemProperties.get(DataSubConstants.PROPERTY_OPERATOR_OPTR))) {
                this.mPhone.mCi.setOnPlmnChangeNotification(this, 270844, null);
                this.mPhone.mCi.setOnRegistrationSuspended(this, 270845, null);
            }
            this.mPhone.mCi.registerForResetAttachApn(this, 270847, null);
            this.mPhone.mCi.registerForAttachApnChanged(this, 270843, null);
        }
        this.mPhone.mCi.registerForRilConnected(this, 270861, (Object) null);
        this.mPhone.mCi.registerForLteAccessStratumState(this, 270849, null);
        this.mPhone.mCi.registerForDataAllowed(this, 270853, null);
        this.mPhone.mCi.registerForPcoDataAfterAttached(this, 270856, null);
        this.mPhone.mCi.registerForDedicatedBearerActivated(this, 270857, null);
        this.mPhone.mCi.registerForDedicatedBearerModified(this, 270858, null);
        this.mPhone.mCi.registerForDedicatedBearerDeactivationed(this, 270859, null);
        this.mPhone.mCi.registerForIccRefresh(this, 270870, null);
        this.mPhone.mCi.registerForNetworkReject(this, 270862, null);
        this.mPhone.mCi.registerForModemReset(this, 270865, null);
        this.mPhone.mCi.registerForMobileDataUsage(this, 270867, null);
        if (this.mTransportType == 1) {
            this.mPhone.mCi.registerForNwLimitState(this, 270869, null);
        }
    }

    /* access modifiers changed from: protected */
    public void registerSettingsObserver() {
        MtkDcTracker.super.registerSettingsObserver();
        registerFdnContentObserver();
    }

    public void dispose() {
        MtkDcTracker.super.dispose();
        IDataConnectionExt iDataConnectionExt = this.mDataConnectionExt;
        if (iDataConnectionExt != null) {
            iDataConnectionExt.stopDataRoamingStrategy();
        }
        this.mPrioritySortedApnContextsEx.clear();
        this.mPhone.getContext().getContentResolver().unregisterContentObserver(this.mImsSwitchChangeObserver);
        this.mPhone.getContext().unregisterReceiver(this.mIntentReceiverEx);
        HandlerThread handlerThread = this.mDcHandlerThread;
        if (handlerThread != null) {
            handlerThread.quitSafely();
            this.mDcHandlerThread = null;
        }
        this.mIsFdnChecked = false;
        this.mIsMatchFdnForAllowData = false;
        this.mIsPhbStateChangedIntentRegistered = false;
        this.mPhone.getContext().unregisterReceiver(this.mPhbStateChangedIntentReceiver);
        Handler handler = this.mWorkerHandler;
        if (handler != null) {
            handler.getLooper().quit();
        }
    }

    /* access modifiers changed from: protected */
    public void unregisterForAllEvents() {
        MtkDcTracker.super.unregisterForAllEvents();
        logd("unregisterForAllEvents: mPhone = " + this.mPhone);
        this.mPhone.mCi.unregisterForRemoveRestrictEutran(this);
        this.mPhone.mCi.unregisterForMdDataRetryCountReset(this);
        if (!hasOperatorIaCapability()) {
            if (!WorldPhoneUtil.isWorldPhoneSupport() && !DataSubConstants.OPERATOR_OP01.equals(SystemProperties.get(DataSubConstants.PROPERTY_OPERATOR_OPTR))) {
                this.mPhone.mCi.unSetOnPlmnChangeNotification(this);
                this.mPhone.mCi.unSetOnRegistrationSuspended(this);
            }
            this.mPhone.mCi.unregisterForResetAttachApn(this);
            this.mPhone.mCi.unregisterForAttachApnChanged(this);
        }
        this.mPhone.mCi.unregisterForRilConnected(this);
        this.mPhone.mCi.unregisterForLteAccessStratumState(this);
        this.mPhone.mCi.unregisterForDataAllowed(this);
        this.mPhone.mCi.unregisterForPcoDataAfterAttached(this);
        this.mPhone.mCi.unregisterForDedicatedBearerActivated(this);
        this.mPhone.mCi.unregisterForDedicatedBearerModified(this);
        this.mPhone.mCi.unregisterForDedicatedBearerDeactivationed(this);
        this.mPhone.mCi.unregisterForIccRefresh(this);
        this.mPhone.mCi.unregisterForNetworkReject(this);
        this.mPhone.mCi.unregisterForModemReset(this);
        this.mPhone.mCi.unregisterForMobileDataUsage(this);
        this.mPhone.mCi.unregisterForNwLimitState(this);
    }

    /* access modifiers changed from: protected */
    public ApnContext addApnContext(String type, NetworkConfig networkConfig) {
        ApnContext apnContext = new MtkApnContext(this.mPhone, type, this.mLogTag, networkConfig, this);
        this.mApnContexts.put(type, apnContext);
        this.mApnContextsByType.put(ApnSetting.getApnTypesBitmaskFromString(type), apnContext);
        this.mPrioritySortedApnContextsEx.add(apnContext);
        return apnContext;
    }

    /* access modifiers changed from: protected */
    public void initApnContexts() {
        ApnContext apnContext;
        log("initApnContexts: E");
        if (this.mPrioritySortedApnContextsEx == null) {
            this.mPrioritySortedApnContextsEx = new ArrayList<>();
        }
        MtkDcTracker.super.initApnContexts();
        for (String networkConfigString : this.mPhone.getContext().getResources().getStringArray(17236103)) {
            NetworkConfig networkConfig = new NetworkConfig(networkConfigString);
            int i = networkConfig.type;
            if (i != 21) {
                switch (i) {
                    case 25:
                        apnContext = addApnContext("xcap", networkConfig);
                        break;
                    case 26:
                        apnContext = addApnContext("rcs", networkConfig);
                        break;
                    case 27:
                        apnContext = addApnContext("bip", networkConfig);
                        break;
                    case 28:
                        apnContext = addApnContext("vsim", networkConfig);
                        break;
                    default:
                        log("initApnContexts: skipping unknown type=" + networkConfig.type);
                        continue;
                }
            } else {
                apnContext = addApnContext("wap", networkConfig);
            }
            log("initApnContexts: apnContext=" + apnContext);
        }
        Collections.sort(this.mPrioritySortedApnContextsEx, new Comparator<ApnContext>() {
            /* class com.mediatek.internal.telephony.dataconnection.MtkDcTracker.AnonymousClass3 */

            public int compare(ApnContext c1, ApnContext c2) {
                return c2.priority - c1.priority;
            }
        });
        logd("initApnContexts: mPrioritySortedApnContextsEx=" + this.mPrioritySortedApnContextsEx);
        if (VDBG) {
            log("initApnContexts: X mApnContexts=" + this.mApnContexts);
        }
    }

    /* access modifiers changed from: protected */
    public void onDataConnectionDetached() {
        MtkDcTracker.super.onDataConnectionDetached();
        this.mPhone.notifyDataConnection("default");
    }

    public boolean isDataAllowed(ApnContext apnContext, int requestType, DataConnectionReasons dataConnectionReasons) {
        boolean recordsLoaded;
        boolean isDataEnabled;
        ApnSetting apnSetting;
        if (OppoTelephonyFactory.getInstance().getFeature(IOppoDataManager.DEFAULT, new Object[0]).isDataAllowByPolicy(this.mPhone)) {
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
        if (apnContext != null && (TextUtils.equals(apnContext.getApnType(), "default") || TextUtils.equals(apnContext.getApnType(), "ims"))) {
            recordsLoaded = true;
        } else {
            recordsLoaded = this.mIccRecords.get() != null && ((IccRecords) this.mIccRecords.get()).getRecordsLoaded();
        }
        if (recordsLoaded && MtkDcHelper.isCdma3GDualModeCard(this.mPhone.getPhoneId())) {
            if (this.mIccRecords == null) {
                logd("isDataAllowed: icc records is null.");
                recordsLoaded = false;
            }
            IccRecords curIccRecords = (IccRecords) this.mIccRecords.get();
            boolean isRoaming = this.mPhone.getServiceStateTracker().mSS.getRoaming();
            int defaultSubId = SubscriptionController.getInstance().getDefaultDataSubId();
            log("isDataAllowed: , current sub=" + this.mPhone.getSubId() + ", default sub=" + defaultSubId + ", isRoaming=" + isRoaming + ", icc records=" + this.mIccRecords);
            if (this.mPhone.getSubId() == defaultSubId && !isRoaming && curIccRecords != null && ((curIccRecords instanceof SIMRecords) || ((curIccRecords instanceof RuimRecords) && !curIccRecords.isLoaded()))) {
                recordsLoaded = false;
            }
        }
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
            if (apnContext != null && ((apnContext.getApnType().equals("default") || apnContext.getApnType().equals("ia")) && this.mPhone.getTransportManager().isInLegacyMode() && dataRat == 18)) {
                reasons.add(DataConnectionReasons.DataDisallowedReasonType.ON_IWLAN);
            }
            if (isEmergency()) {
                reasons.add(DataConnectionReasons.DataDisallowedReasonType.IN_ECBM);
            }
            boolean dataRegStatus = this.mPhone.getServiceState().getDataRegState() == 0;
            if (!attachedState && !shouldAutoAttach()) {
                if (requestType != 2 && !dataRegStatus) {
                    reasons.add(DataConnectionReasons.DataDisallowedReasonType.NOT_ATTACHED);
                }
            }
            if (!recordsLoaded) {
                reasons.add(DataConnectionReasons.DataDisallowedReasonType.RECORD_NOT_LOADED);
            }
            MtkDcHelper dcHelper = MtkDcHelper.getInstance();
            if (dcHelper != null && !dcHelper.isDataAllowedForConcurrent(this.mPhone.getPhoneId())) {
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
                if (OemConstant.isVersionMOVISTAR() && apnContext != null) {
                    if (apnContext.getApnType() != null) {
                        if (apnContext.getApnType().equals("mms")) {
                            Rlog.d("sms", "allow mms in roaming, ism=" + isMeteredApnType);
                            if (apnContext != null && apnContext.getApnType().equals("default")) {
                                apnContext.setReason("roamingOn");
                            }
                        }
                    }
                }
                reasons.add(DataConnectionReasons.DataDisallowedReasonType.ROAMING_DISABLED);
                apnContext.setReason("roamingOn");
            }
            if (MtkDcTracker.super.isOppoRoamingAllowed() != null) {
                reasons.add(MtkDcTracker.super.isOppoRoamingAllowed());
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
                if (apnContext == null || !isDataAllowedAsOff(apnContext.getApnType()) || !this.mDataEnabledSettings.isPolicyDataEnabled()) {
                    boolean isMms = false;
                    if (this.mDataEnabledSettings.isInternalDataEnabled() && apnContext != null && apnContext.getApnType() != null && apnContext.getApnType().equals("mms")) {
                        Rlog.d("sms", "isMeteredApnType=" + isMeteredApnType);
                        isMms = true;
                    }
                    if (!isMms) {
                        reasons.add(DataConnectionReasons.DataDisallowedReasonType.DATA_DISABLED);
                    }
                }
            }
            isDataAllowedForRoamingFeature(reasons);
            if (dcHelper != null && !dcHelper.isSimMeLockAllowed(this.mPhone.getPhoneId())) {
                reasons.add(DataConnectionReasons.DataDisallowedReasonType.MTK_SIM_ME_LOCK_NOT_ALLOWED);
            }
            if (isFdnEnabled()) {
                reasons.add(DataConnectionReasons.DataDisallowedReasonType.MTK_FDN_ENABLED);
            }
            if (!getAllowConfig()) {
                reasons.add(DataConnectionReasons.DataDisallowedReasonType.MTK_NOT_ALLOWED);
            }
            if (MtkUiccController.getVsimCardType(this.mPhone.getPhoneId()).isAllowOnlyVsimNetwork()) {
                reasons.add(DataConnectionReasons.DataDisallowedReasonType.MTK_NON_VSIM_PDN_NOT_ALLOWED);
            }
            if (isDataRetryRestrictEnabled() && apnContext != null && ((apnContext.getApnType().equals("default") || apnContext.getApnType().equals("dun")) && (apnSetting = apnContext.getApnSetting()) != null && apnSetting.getPermanentFailed())) {
                reasons.add(DataConnectionReasons.DataDisallowedReasonType.MTK_DATA_RETRY_NOT_ALLOWED);
            }
            if (apnContext != null && apnContext.getApnType().equals("default") && TextUtils.equals("2GVoiceCallEnded", apnContext.getReason()) && this.mPhone.getSubId() != SubscriptionManager.getDefaultDataSubscriptionId()) {
                reasons.add(DataConnectionReasons.DataDisallowedReasonType.MTK_TEMP_DATA_SWITCH_NOT_ALLOWED);
            }
            if (apnContext != null && apnContext.getApnType().equals("default") && !getIsPcoAllowedDefault()) {
                reasons.add(DataConnectionReasons.DataDisallowedReasonType.MTK_PCO_NOT_ALLOWED);
            }
            if (apnContext != null && reasons.allowed() && isLocatedPlmnChanged()) {
                reasons.add(DataConnectionReasons.DataDisallowedReasonType.MTK_LOCATED_PLMN_CHANGED);
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
                    if (haveVsimIgnoreUserDataSetting() && !reasons.allowed()) {
                        reasons.add(DataConnectionReasons.DataAllowedReasonType.NORMAL);
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
    public void setupDataOnAllConnectableApns(String reason, DcTracker.RetryFailures retryFailures) {
        if (VDBG) {
            log("setupDataOnAllConnectableApns: " + reason);
        }
        if (!VDBG) {
            StringBuilder sb = new StringBuilder(120);
            Iterator<ApnContext> it = this.mPrioritySortedApnContextsEx.iterator();
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
        }
        Iterator<ApnContext> it2 = this.mPrioritySortedApnContextsEx.iterator();
        while (it2.hasNext()) {
            ApnContext apnContext2 = it2.next();
            if (isDataRetryRestrictEnabled() && (apnContext2.getApnType().equals("default") || apnContext2.getApnType().equals("dun"))) {
                if (TextUtils.equals(reason, MtkGsmCdmaPhone.REASON_CARRIER_CONFIG_LOADED) || TextUtils.equals(reason, "2GVoiceCallEnded") || TextUtils.equals(reason, "nwTypeChanged")) {
                    log("ignore setup data call of default/dun apn for cc33 operators");
                } else {
                    cancelReconnectAlarm(apnContext2);
                    ApnSetting apnSetting = apnContext2.getApnSetting();
                    if (apnSetting != null) {
                        apnSetting.setPermanentFailed(false);
                        log("set permanentFailed as false for default apn type");
                    }
                }
            }
            setupDataOnConnectableApn(apnContext2, reason, retryFailures);
        }
    }

    /* access modifiers changed from: protected */
    public void setupDataOnConnectableApn(ApnContext apnContext, String reason, DcTracker.RetryFailures retryFailures) {
        if (VDBG) {
            log("setupDataOnConnectableApn: apnContext " + apnContext);
        }
        if (apnContext.getState() == DctConstants.State.FAILED || apnContext.getState() == DctConstants.State.RETRYING) {
            if (retryFailures == DcTracker.RetryFailures.ALWAYS) {
                apnContext.releaseDataConnection(reason);
            } else if (!apnContext.isConcurrentVoiceAndDataAllowed() && this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed()) {
                apnContext.releaseDataConnection(reason);
            }
        }
        if (apnContext.isConnectable()) {
            if (isOnlySingleDcAllowed(this.mPhone.getServiceState().getRilDataRadioTechnology()) && isHigherPriorityApnContextActive(apnContext)) {
                log("No need to trysetupdata as higher priority apncontext exists");
            } else if (!MtkDcTracker.super.oppoWlanAssistantBlockTrySetupData(apnContext, reason)) {
                log("isConnectable() call trySetupData");
                apnContext.setReason(reason);
                if (this.mHandoverApnType == apnContext.getApnTypeBitmask()) {
                    this.mHandoverApnType = 0;
                    trySetupData(apnContext, 2);
                } else {
                    trySetupData(apnContext, 1);
                }
            } else {
                return;
            }
        }
        if (apnContext.getState() == DctConstants.State.CONNECTED && TextUtils.equals(reason, MtkGsmCdmaPhone.REASON_DATA_SETUP_SSC_MODE3) && TextUtils.equals(reason, apnContext.getReason())) {
            log("SSC mode3 call trySetupData for APN type" + apnContext.getApnType());
            trySetupData(apnContext, 1);
        }
        if (TextUtils.equals(apnContext.getApnType(), "default") && apnContext.getState() == DctConstants.State.CONNECTED && TextUtils.equals(reason, "apnChanged") && this.mCanSetPreferApn && this.mPreferredApn == null) {
            log("setupDataOnConnectableApns: PREFERRED APN is null");
            this.mPreferredApn = apnContext.getApnSetting();
            if (this.mPreferredApn != null) {
                setPreferredApn(this.mPreferredApn.getId());
                this.mPhone.notifyDataConnection(apnContext.getApnType());
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean trySetupData(ApnContext apnContext, int requestType) {
        int radioTech;
        if (MtkDcTracker.super.needManualSelectAPN(apnContext.getApnType(), this.mPreferredApn)) {
            log("need manual select APN, trySetupData return false");
            return false;
        } else if (this.mPhone.getSimulatedRadioControl() != null) {
            apnContext.setState(DctConstants.State.CONNECTED);
            this.mPhone.notifyDataConnection(apnContext.getApnType());
            log("trySetupData: X We're on the simulator; assuming connected retValue=true");
            return true;
        } else {
            DataConnectionReasons dataConnectionReasons = new DataConnectionReasons();
            boolean isDataAllowed = isDataAllowed(apnContext, requestType, dataConnectionReasons) || isDataAllowedExt(dataConnectionReasons, apnContext);
            boolean isEmergencyApn = apnContext.getApnType().equals("emergency");
            if (!hasMdAutoSetupImsCapability() && isEmergencyApn) {
                int defaultBearerCount = this.mDcc.getActiveDcCount();
                log("defaultBearerCount: " + defaultBearerCount + ", mDedicatedBearerCount: " + this.mDedicatedBearerCount);
                if (this.mDedicatedBearerCount + defaultBearerCount >= 7) {
                    teardownDataByEmergencyPolicy();
                    return false;
                }
            }
            String logStr = "trySetupData for APN type " + apnContext.getApnType() + ", reason: " + apnContext.getReason() + ", requestType=" + requestTypeToString(requestType) + ". " + dataConnectionReasons.toString();
            log(logStr);
            apnContext.requestLog(logStr);
            if (isDataAllowed) {
                if (apnContext.getState() == DctConstants.State.FAILED) {
                    log("trySetupData: make a FAILED ApnContext IDLE so its reusable");
                    apnContext.requestLog("trySetupData: make a FAILED ApnContext IDLE so its reusable");
                    apnContext.setState(DctConstants.State.IDLE);
                }
                int radioTech2 = getDataRat();
                if (radioTech2 == 0) {
                    radioTech = getVoiceRat();
                } else {
                    radioTech = radioTech2;
                }
                log("service state=" + this.mPhone.getServiceState());
                apnContext.setConcurrentVoiceAndDataAllowed(this.mPhone.getServiceStateTracker().isConcurrentVoiceAndDataAllowed());
                if (apnContext.getState() == DctConstants.State.IDLE) {
                    if (TextUtils.equals(apnContext.getApnType(), "emergency") && this.mAllApnSettings.isEmpty()) {
                        log("add mEmergencyApn: " + this.mEmergencyApn + " to mAllApnSettings");
                        addEmergencyApnSetting();
                    }
                    ArrayList<ApnSetting> waitingApns = buildWaitingApns(apnContext.getApnType(), radioTech);
                    if (waitingApns.isEmpty()) {
                        notifyNoData(27, apnContext);
                        log("trySetupData: X No APN found retValue=false");
                        apnContext.requestLog("trySetupData: X No APN found retValue=false");
                        mtkFakeDataConnection(apnContext);
                        int log_type = -1;
                        String log_desc = "";
                        try {
                            try {
                                String[] log_array = this.mPhone.getContext().getString(this.mPhone.getContext().getResources().getIdentifier("zz_oppo_critical_log_111", "string", "android")).split(",");
                                log_type = Integer.valueOf(log_array[0]).intValue();
                                log_desc = log_array[1];
                            } catch (Exception e) {
                            }
                        } catch (Exception e2) {
                        }
                        OppoManager.writeLogToPartition(log_type, "trySetupData: X No APN found", "NETWORK", "data_no_acailable_apn", log_desc);
                        return false;
                    }
                    apnContext.setWaitingApns(waitingApns);
                    ((MtkApnContext) apnContext).setWifiApns(buildWifiApns(apnContext.getApnType()));
                    log("trySetupData: Create from mAllApnSettings : " + apnListToString(this.mAllApnSettings));
                }
                logd("trySetupData: call setupData, waitingApns : " + apnListToString(apnContext.getWaitingApns()) + ", wifiApns : " + apnListToString(((MtkApnContext) apnContext).getWifiApns()));
                boolean retValue = setupData(apnContext, radioTech, requestType);
                StringBuilder sb = new StringBuilder();
                sb.append("trySetupData: X retValue=");
                sb.append(retValue);
                log(sb.toString());
                return retValue;
            }
            if (apnContext.getApnType().equals("default") || !apnContext.isConnectable()) {
                if (apnContext.getReason().equals("roamingOn") && apnContext.getApnType().equals("default")) {
                    log("Notify data disconnect reason to UI side");
                    this.mPhone.notifyDataConnection(apnContext.getApnType());
                }
            } else if (!apnContext.getApnType().equals("mms") || !TelephonyManager.getDefault().isMultiSimEnabled() || this.mAttached.get()) {
                this.mPhone.notifyDataConnectionFailed(apnContext.getApnType());
            } else {
                log("Wait for attach");
                return true;
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

    /* access modifiers changed from: protected */
    public boolean cleanUpAllConnectionsInternal(boolean detach, String reason) {
        log("cleanUpAllConnectionsInternal: detach=" + detach + " reason=" + reason);
        boolean didDisconnect = false;
        boolean disableMeteredOnly = false;
        if (!TextUtils.isEmpty(reason)) {
            disableMeteredOnly = reason.equals("specificDisabled") || reason.equals("roamingOn") || reason.equals("carrierActionDisableMeteredApn");
            if (reason.equals("simNotReady")) {
                log("cleanUpAllConnectionsInternal: not cleanup connections for sim not ready");
                return false;
            }
        }
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (reason.equals("roamingOn") && ignoreDataRoaming(apnContext.getApnType())) {
                log("cleanUpAllConnectionsInternal: Ignore Data Roaming for apnType = " + apnContext.getApnType());
            } else if (!reason.equals("SinglePdnArbitration") || !apnContext.getApnType().equals("ims")) {
                if (reason.equals("radioTurnedOff") && !hasMdAutoSetupImsCapability() && apnContext.getApnType().equals("ims")) {
                    log("cleanUpAllConnectionsInternal: Ignore legacy ims for apnType = " + apnContext.getApnType() + " reason = " + reason);
                } else if (shouldCleanUpConnection(apnContext, disableMeteredOnly)) {
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
    /* JADX WARNING: Code restructure failed: missing block: B:100:0x029e, code lost:
        log("setInitialAttachApn: using defaultApnSetting");
        r43.mInitialAttachApnSetting = r12;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:101:0x02a6, code lost:
        if (r13 == null) goto L_0x02af;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:102:0x02a8, code lost:
        log("setInitialAttachApn: using firstApnSetting");
        r43.mInitialAttachApnSetting = r13;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:104:0x02b1, code lost:
        if (r43.mInitialAttachApnSetting != null) goto L_0x030e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:105:0x02b3, code lost:
        log("setInitialAttachApn: X There in no available apn, use empty");
        r4 = android.telephony.data.ApnSetting.makeApnSetting(0, "", "", "", "", 0, null, "", 0, "", "", 0, 256, 2, 2, true, 0, 0, false, 0, 0, 0, 0, 0, "");
        r43.mPhone.mCi.setInitialAttachApn(createDataProfile(r4, r4.equals(getPreferredApn())), r43.mPhone.getServiceState().getDataRoamingFromRegistration(), null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:106:0x030e, code lost:
        log("setInitialAttachApn: X selected Apn=" + r43.mInitialAttachApnSetting);
        r2 = r43.mInitialAttachApnSetting.getApnName();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:107:0x032a, code lost:
        if (r16 == false) goto L_0x0333;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:108:0x032c, code lost:
        log("setInitialAttachApn: ESM flag false, change IA APN to empty");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:109:0x0333, code lost:
        r4 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:110:0x0334, code lost:
        if (r3 == false) goto L_0x0342;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:111:0x0336, code lost:
        log("setInitialAttachApn: DCM IA support");
        r4 = obtainMessage(270846);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:112:0x0342, code lost:
        r9 = r43.mInitialAttachApnSetting;
        r43.mPhone.mCi.setInitialAttachApn(createDataProfile(r9, r9.equals(getPreferredApn())), r43.mPhone.getServiceState().getDataRoamingFromRegistration(), r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:113:0x0363, code lost:
        log("setInitialAttachApn: new attach Apn [" + r43.mInitialAttachApnSetting + "]");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:114:0x037e, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x004f, code lost:
        r8 = r7.substring(0, 3);
        log("setInitialApn: currentMcc = " + r8 + ", needsResumeModem = " + r3);
        r11 = new java.lang.StringBuilder();
        r11.append("setInitialAttachApn: current attach Apn [");
        r11.append(r43.mInitialAttachApnSetting);
        r11.append("]");
        log(r11.toString());
        r11 = null;
        r12 = null;
        r13 = null;
        r14 = null;
        log("setInitialApn: E mPreferredApn=" + r43.mPreferredApn);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00a8, code lost:
        if (r43.mIsImsHandover != false) goto L_0x00ae;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00ac, code lost:
        if (com.mediatek.internal.telephony.dataconnection.MtkDcTracker.MTK_IMS_TESTMODE_SUPPORT == false) goto L_0x00c8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00ae, code lost:
        r14 = getClassTypeApn(3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00b2, code lost:
        if (r14 == null) goto L_0x00c8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00b4, code lost:
        log("setInitialAttachApn: manualChangedAttachApn = " + r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00ca, code lost:
        if (r43.mMdChangedAttachApn != null) goto L_0x017a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00cc, code lost:
        r0 = r43.mPhone.getPhoneId();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00d6, code lost:
        if (android.telephony.SubscriptionManager.isValidPhoneId(r0) == false) goto L_0x0175;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00d8, code lost:
        r9 = android.os.SystemProperties.getInt(com.mediatek.internal.telephony.dataconnection.MtkDcTracker.PROP_APN_CLASS + r0, -1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00ee, code lost:
        if (r9 < 0) goto L_0x0170;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00f0, code lost:
        r2 = android.os.SystemProperties.get(r43.PROPERTY_ICCID[r0], "");
        r15 = new java.lang.StringBuilder();
        r16 = false;
        r15.append(com.mediatek.internal.telephony.dataconnection.MtkDcTracker.PROP_APN_CLASS_ICCID);
        r15.append(r0);
        r4 = android.os.SystemProperties.get(r15.toString(), "");
        log("setInitialAttachApn: " + r2 + " , " + r4 + ", " + r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x013d, code lost:
        if (android.text.TextUtils.equals(r2, r4) == false) goto L_0x0143;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x013f, code lost:
        updateMdChangedAttachApn(r9);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0143, code lost:
        android.os.SystemProperties.set(com.mediatek.internal.telephony.dataconnection.MtkDcTracker.PROP_APN_CLASS_ICCID + r0, "");
        android.os.SystemProperties.set(com.mediatek.internal.telephony.dataconnection.MtkDcTracker.PROP_APN_CLASS + r0, "");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0170, code lost:
        r16 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0175, code lost:
        r16 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x017a, code lost:
        r16 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x017e, code lost:
        r0 = r43.mMdChangedAttachApn;
        r2 = r43.mMdChangedAttachApn;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0182, code lost:
        if (r2 == null) goto L_0x0192;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0189, code lost:
        if (getClassType(r2) != 1) goto L_0x0192;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x018f, code lost:
        if (isMdChangedAttachApnEnabled() != false) goto L_0x0192;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0191, code lost:
        r0 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0192, code lost:
        if (r0 != null) goto L_0x026d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0194, code lost:
        if (r14 != null) goto L_0x026d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x019a, code lost:
        if (r43.mPreferredApn == null) goto L_0x01a8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x01a2, code lost:
        if (r43.mPreferredApn.canHandleType(256) == false) goto L_0x01a8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x01a4, code lost:
        r11 = r43.mPreferredApn;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x01aa, code lost:
        if (r43.mAllApnSettings == null) goto L_0x026d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x01b2, code lost:
        if (r43.mAllApnSettings.isEmpty() != false) goto L_0x026d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x01b4, code lost:
        r13 = (android.telephony.data.ApnSetting) r43.mAllApnSettings.get(0);
        log("setInitialApn: firstApnSetting=" + r13);
        r2 = false;
        r5 = r43.mAllApnSettings.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x01dd, code lost:
        if (r5.hasNext() == false) goto L_0x026d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x01df, code lost:
        r9 = (android.telephony.data.ApnSetting) r5.next();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x01e7, code lost:
        if (r43.mIsAddMnoApnsIntoAllApnList == false) goto L_0x01fe;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x01ed, code lost:
        if (isSimActivated() != false) goto L_0x01fd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x01f9, code lost:
        if (com.mediatek.internal.telephony.dataconnection.MtkDcTracker.SPRINT_IA_NI.compareToIgnoreCase(r9.getApnName()) != 0) goto L_0x01fd;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x01fb, code lost:
        r2 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:73:0x01fd, code lost:
        r2 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x01fe, code lost:
        log("setInitialApn: isSelectOpIa=" + r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x0218, code lost:
        if (r9.canHandleType(256) != false) goto L_0x021c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x021a, code lost:
        if (r2 == false) goto L_0x0249;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x0220, code lost:
        if (r9.isEnabled() == false) goto L_0x0249;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x0226, code lost:
        if (checkIfDomesticInitialAttachApn(r8) == false) goto L_0x0249;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x0228, code lost:
        log("setInitialApn: iaApnSetting=" + r9);
        r11 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x0243, code lost:
        if (com.android.internal.util.ArrayUtils.contains(r43.PLMN_EMPTY_APN_PCSCF_SET, r7) == false) goto L_0x026d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x0245, code lost:
        r16 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x0249, code lost:
        if (r12 != null) goto L_0x0269;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x0251, code lost:
        if (r9.canHandleType(17) == false) goto L_0x0269;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x0253, code lost:
        log("setInitialApn: defaultApnSetting=" + r9);
        r12 = r9;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x026d, code lost:
        r43.mInitialAttachApnSetting = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x0270, code lost:
        if (r14 == null) goto L_0x027a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x0272, code lost:
        log("setInitialAttachApn: using manualChangedAttachApn");
        r43.mInitialAttachApnSetting = r14;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:0x027a, code lost:
        if (r0 == null) goto L_0x0284;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x027c, code lost:
        log("setInitialAttachApn: using mMdChangedAttachApn");
        r43.mInitialAttachApnSetting = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x0284, code lost:
        if (r11 == null) goto L_0x028e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x0286, code lost:
        log("setInitialAttachApn: using iaApnSetting");
        r43.mInitialAttachApnSetting = r11;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:97:0x0290, code lost:
        if (r43.mPreferredApn == null) goto L_0x029c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:98:0x0292, code lost:
        log("setInitialAttachApn: using mPreferredApn");
        r43.mInitialAttachApnSetting = r43.mPreferredApn;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:0x029c, code lost:
        if (r12 == null) goto L_0x02a6;
     */
    public void setInitialAttachApn() {
        if (MtkDcTracker.super.needManualSelectAPN(this.mPreferredApn)) {
            log("need manual select APN, setInitialAttachApn return");
        } else if (hasOperatorIaCapability()) {
            MtkDcTracker.super.setInitialAttachApn();
        } else {
            boolean needsResumeModem = false;
            ApnSetting apnSetting = this.mInitialAttachApnSetting;
            String operatorNumeric = mtkGetOperatorNumeric((IccRecords) this.mIccRecords.get());
            if (operatorNumeric != null) {
                if (operatorNumeric.length() != 0) {
                    synchronized (this.mNeedsResumeModemLock) {
                        try {
                            if (this.mNeedsResumeModem) {
                                try {
                                    this.mNeedsResumeModem = false;
                                    needsResumeModem = true;
                                } catch (Throwable th) {
                                    th = th;
                                    while (true) {
                                        try {
                                            break;
                                        } catch (Throwable th2) {
                                            th = th2;
                                        }
                                    }
                                    throw th;
                                }
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            while (true) {
                                break;
                            }
                            throw th;
                        }
                    }
                }
            }
            log("setInitialApn: but no operator numeric");
        }
    }

    /* access modifiers changed from: protected */
    public void onApnChanged() {
        DctConstants.State overallState = getOverallState();
        boolean z = true;
        boolean isDisconnected = overallState == DctConstants.State.IDLE || overallState == DctConstants.State.FAILED;
        if (this.mPhone instanceof GsmCdmaPhone) {
            this.mPhone.updateCurrentCarrierInProvider();
        }
        log("onApnChanged: createAllApnList and cleanUpAllConnections");
        createAllApnList();
        setDataProfilesAsNeeded();
        setInitialAttachApn();
        if (isDisconnected) {
            z = false;
        }
        cleanUpConnectionsOnUpdatedApns(z, "apnChanged");
        sendOnApnChangedDone(false);
    }

    /* access modifiers changed from: protected */
    public boolean isHigherPriorityApnContextActive(ApnContext apnContext) {
        if (apnContext.getApnType().equals("ims")) {
            return false;
        }
        Iterator<ApnContext> it = this.mPrioritySortedApnContextsEx.iterator();
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
        IDataConnectionExt iDataConnectionExt = this.mDataConnectionExt;
        if (iDataConnectionExt != null) {
            try {
                boolean onlySingleDcAllowed = iDataConnectionExt.isOnlySingleDcAllowed();
                if (onlySingleDcAllowed) {
                    log("isOnlySingleDcAllowed: " + onlySingleDcAllowed);
                    return true;
                }
            } catch (Exception ex) {
                loge("Fail to create or use plug-in");
                ex.printStackTrace();
            }
        }
        return MtkDcTracker.super.isOnlySingleDcAllowed(rilRadioTech);
    }

    /* access modifiers changed from: protected */
    public boolean retryAfterDisconnected(ApnContext apnContext) {
        boolean retry = true;
        String reason = apnContext.getReason();
        if ("radioTurnedOff".equals(reason) || MtkGsmCdmaPhone.REASON_FDN_ENABLED.equals(reason) || (isOnlySingleDcAllowed(getDataRat()) && isHigherPriorityApnContextActive(apnContext))) {
            retry = false;
        }
        MtkDcTracker.super.checkIfRetryAfterDisconnected(apnContext, retry);
        return retry;
    }

    /* access modifiers changed from: protected */
    public void onRecordsLoadedOrSubIdChanged() {
        String operatorNumeric = mtkGetOperatorNumeric((IccRecords) this.mIccRecords.get());
        if (TextUtils.isEmpty(operatorNumeric)) {
            logd("onRecordsLoadedOrSubIdChanged: empty operator numeric, return");
            this.mIsOperatorNumericEmpty = true;
            return;
        }
        if (MtkDcHelper.isCdma3GDualModeCard(this.mPhone.getPhoneId())) {
            DctConstants.State overallState = getOverallState();
            if (!(overallState == DctConstants.State.IDLE || overallState == DctConstants.State.FAILED) && this.mAllApnSettings != null && !this.mAllApnSettings.isEmpty()) {
                String numeric = ((ApnSetting) this.mAllApnSettings.get(0)).getOperatorNumeric();
                if (numeric.length() > 0 && !numeric.equals(operatorNumeric)) {
                    logd("CDMA 3G dual mode card numeric change, clean up.");
                    cleanUpAllConnectionsInternal(true, "apnChanged");
                }
            }
        }
        MtkSubscriptionController.getMtkInstance().getSlotIndex(SubscriptionController.getInstance().getDefaultDataSubId());
        this.mIsFdnChecked = false;
        this.mDataRetryOperator = getDataRetryOperator();
        if (isDataRetryRestrictEnabled()) {
            this.mPhone.mCi.setRemoveRestrictEutranMode(true, null);
        }
        getImsiFromRil();
        MtkDcTracker.super.onRecordsLoadedOrSubIdChanged();
    }

    /* access modifiers changed from: protected */
    public void onSimNotReady() {
        MtkDcTracker.super.onSimNotReady();
        setIsPcoAllowedDefault(true);
        this.mImsiGsm = null;
        this.mImsiCdma = null;
        this.mPhoneType = 0;
    }

    public void enableApn(int apnType, int requestType, Message onCompleteMsg) {
        ApnContext apnContext;
        if (apnType != 17 || requestType != 1 || (apnContext = (ApnContext) this.mApnContextsByType.get(apnType)) == null || !apnContext.isDependencyMet() || apnContext.isReady() || apnContext.getState() != DctConstants.State.IDLE) {
            MtkDcTracker.super.enableApn(apnType, requestType, onCompleteMsg);
            return;
        }
        log("Enable 'default' apn type in advance");
        apnContext.setReason("dataEnabled");
        apnContext.setEnabled(true);
        apnContext.resetErrorCodeRetries();
        if (trySetupData(apnContext, requestType)) {
            addRequestNetworkCompleteMsg(onCompleteMsg, apnType);
        } else {
            sendRequestNetworkCompleteMsg(onCompleteMsg, false, this.mTransportType, requestType);
        }
    }

    /* access modifiers changed from: protected */
    public void onDataRoamingOff() {
        log("onDataRoamingOff getDataRoamingEnabled=" + getDataRoamingEnabled() + ", mUserDataEnabled=" + this.mDataEnabledSettings.isUserDataEnabled());
        setRoamingDataWithRoamingType(0);
        reevaluateDataConnections();
        if (isUniqueRoamingFeatureEnabled()) {
            boolean bDomDataOnRoamingEnabled = getDomesticDataRoamingEnabledFromSettings();
            boolean bIntDataOnRoamingEnabled = getInternationalDataRoamingEnabledFromSettings();
            log("onDomOrIntRoamingOn bDomDataOnRoamingEnabled=" + bDomDataOnRoamingEnabled + ", bIntDataOnRoamingEnabled=" + bIntDataOnRoamingEnabled + ", currentRoamingType=" + this.mPhone.getServiceState().getDataRoamingType());
            if (!bDomDataOnRoamingEnabled || !bIntDataOnRoamingEnabled) {
                log("onDomOrIntRoamingOn: setup data for HOME.");
                setDataProfilesAsNeeded();
                setInitialAttachApn();
                setupDataOnAllConnectableApns("roamingOff", DcTracker.RetryFailures.ALWAYS);
            } else {
                this.mPhone.notifyDataConnection();
            }
        } else if (!getDataRoamingEnabled()) {
            boolean bHasOperatorIaCapability = hasOperatorIaCapability();
            log("onDataRoamingOff: bHasOperatorIaCapability=" + bHasOperatorIaCapability);
            if (!bHasOperatorIaCapability) {
                setDataProfilesAsNeeded();
                setInitialAttachApn();
            }
            setupDataOnAllConnectableApns("roamingOff", DcTracker.RetryFailures.ALWAYS);
        } else {
            this.mPhone.notifyDataConnection();
        }
        if (!hasOperatorIaCapability() && isOp18Sim()) {
            setInitialAttachApn();
        }
    }

    /* access modifiers changed from: protected */
    public void onDataRoamingOnOrSettingsChanged(int messageType) {
        int currentRoamingType = this.mPhone.getServiceState().getDataRoamingType();
        boolean settingChanged = messageType == 270384;
        log("onDataRoamingOnOrSettingsChanged getDataRoamingEnabled = " + getDataRoamingEnabled() + ", mUserDataEnabled = " + this.mDataEnabledSettings.isUserDataEnabled() + ", settingChanged = " + settingChanged + ", currentRoamingType = " + currentRoamingType);
        if (!this.mDataEnabledSettings.isUserDataEnabled()) {
            log("data not enabled by user");
        } else if (!this.mPhone.getServiceState().getDataRoaming()) {
            log("Device is not roaming, ignore the request.");
        } else if (settingChanged || !setRoamingDataWithRoamingType(currentRoamingType)) {
            checkDataRoamingStatus(settingChanged);
            if (!hasOperatorIaCapability() && isOp18Sim()) {
                setInitialAttachApn();
            }
            if (isUniqueRoamingFeatureEnabled()) {
                if (checkDomesticDataRoamingEnabled() || checkInternationalDataRoamingEnabled()) {
                    log("onDataRoamingOnOrSettingsChanged: setup data on roaming");
                    setupDataOnAllConnectableApns("roamingOn", DcTracker.RetryFailures.ALWAYS);
                    this.mPhone.notifyDataConnection();
                    return;
                }
                log("onDataRoamingOnOrSettingsChanged: Tear down data connection on roaming.");
                cleanUpAllConnectionsInternal(true, "roamingOn");
            } else if (getDataRoamingEnabled() || getDomesticRoamingEnabled()) {
                if (settingChanged) {
                    reevaluateDataConnections();
                }
                log("onDataRoamingOnOrSettingsChanged: setup data on roaming");
                MtkDcTracker.super.OppoSetupDataOnAllConnectableApns("roamingOn");
            } else {
                log("onDataRoamingOnOrSettingsChanged: Tear down data connection on roaming.");
                if (MtkUiccController.getVsimCardType(this.mPhone.getPhoneId()) == MtkIccCardConstants.VsimType.REMOTE_SIM) {
                    log("RSim, not tear down any data connection since ignore data roaming");
                } else {
                    cleanUpAllConnectionsInternal(true, "roamingOn");
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onDisconnectDone(ApnContext apnContext) {
        if (!onSkipDisconnectDone(apnContext)) {
            MtkDcTracker.super.onDisconnectDone(apnContext);
            if (apnContext != null && !hasMdAutoSetupImsCapability() && "pdnOccupied".equals(apnContext.getReason())) {
                log("try setup emergency PDN");
                trySetupData((ApnContext) this.mApnContextsByType.get(512), 1);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setDataProfilesAsNeeded() {
        log("setDataProfilesAsNeeded");
        String operator = mtkGetOperatorNumeric((IccRecords) this.mIccRecords.get());
        if (!this.mIsSimNotReady || !(operator == null || operator.length() == 0)) {
            ArrayList<DataProfile> dataProfileList = new ArrayList<>();
            Iterator it = this.mAllApnSettings.iterator();
            while (it.hasNext()) {
                ApnSetting apn = (ApnSetting) it.next();
                DataProfile dp = createDataProfile(encodeInactiveTimer(apn), apn.equals(getPreferredApn()));
                if (!dataProfileList.contains(dp)) {
                    dataProfileList.add(dp);
                }
            }
            ArrayList<ApnSetting> dunApns = fetchDunApns();
            if (dunApns != null && dunApns.size() > 0) {
                Iterator<ApnSetting> it2 = dunApns.iterator();
                while (it2.hasNext()) {
                    ApnSetting dun = it2.next();
                    DataProfile dpDun = createDataProfile(dun, dun.equals(getPreferredApn()));
                    if (!dataProfileList.contains(dpDun)) {
                        dataProfileList.add(dpDun);
                        log("setDataProfilesAsNeeded: add DUN apn setting: " + dun);
                    }
                }
            }
            if (dataProfileList.isEmpty() || (dataProfileList.size() == this.mLastDataProfileList.size() && this.mLastDataProfileList.containsAll(dataProfileList) && !RadioManager.isFlightModePowerOffModemEnabled() && !this.mIsModemReset && !this.mIsSimRefresh && !DataSubConstants.OPERATOR_OP01.equals(SystemProperties.get(DataSubConstants.PROPERTY_OPERATOR_OPTR)) && !DataSubConstants.OPERATOR_OP09.equals(SystemProperties.get(DataSubConstants.PROPERTY_OPERATOR_OPTR)))) {
                log("setDataProfilesAsNeeded: ignore the empty or same data profile list");
                return;
            }
            this.mPhone.mCi.setDataProfile((DataProfile[]) dataProfileList.toArray(new DataProfile[dataProfileList.size()]), this.mPhone.getServiceState().getDataRoamingFromRegistration(), null);
            this.mLastDataProfileList.clear();
            this.mLastDataProfileList.addAll(dataProfileList);
            if (this.mIsModemReset) {
                this.mIsModemReset = false;
            }
            if (this.mIsSimRefresh) {
                this.mIsSimRefresh = false;
                return;
            }
            return;
        }
        log("setDataProfilesAsNeeded: ignore, sim not ready and no operator numeric");
    }

    private ArrayList<ApnSetting> createApnList(Cursor cursor) {
        ArrayList<ApnSetting> result;
        ArrayList<ApnSetting> mnoApns = new ArrayList<>();
        ArrayList<ApnSetting> mvnoApns = new ArrayList<>();
        IccRecords r = (IccRecords) this.mIccRecords.get();
        if (cursor.moveToFirst()) {
            do {
                ApnSetting apn = makeApnSetting(cursor);
                if (apn != null) {
                    if (!apn.hasMvnoParams()) {
                        mnoApns.add(apn);
                    } else if (isMvnoMatches(apn.getMvnoType(), apn.getMvnoMatchData()) || (r != null && ApnSettingUtils.mvnoMatches(r, apn.getMvnoType(), apn.getMvnoMatchData()))) {
                        mvnoApns.add(apn);
                    }
                }
            } while (cursor.moveToNext());
        }
        if (mvnoApns.isEmpty()) {
            result = mnoApns;
        } else {
            result = mvnoApns;
            if (this.mIsAddMnoApnsIntoAllApnList) {
                log("mnoApns=" + mnoApns);
                result.addAll(mnoApns);
            }
        }
        log("createApnList: X result=" + result);
        return result;
    }

    /* access modifiers changed from: protected */
    public void createAllApnList() {
        String operator = mtkGetOperatorNumeric((IccRecords) this.mIccRecords.get());
        if (!this.mIsSimNotReady || !(operator == null || operator.length() == 0)) {
            synchronized (this.mRefCountLock) {
                this.mAllApnSettings.clear();
                if (operator != null) {
                    String selection = "numeric = '" + operator + "'";
                    log("createAllApnList: selection=" + selection);
                    Cursor cursor = this.mPhone.getContext().getContentResolver().query(Uri.withAppendedPath(Telephony.Carriers.CONTENT_URI, "filtered"), null, selection, null, "_id");
                    if (cursor != null) {
                        if (cursor.getCount() > 0) {
                            this.mAllApnSettings = createApnList(cursor);
                        }
                        cursor.close();
                    } else {
                        log("createAllApnList: cursor is null");
                        this.mApnSettingsInitializationLog.log("cursor is null for carrier, operator: " + operator);
                    }
                }
                addVsimApnTypeToDefaultApnSetting();
            }
            addEmergencyApnSetting();
            dedupeApnSettings();
            if (this.mAllApnSettings.isEmpty()) {
                log("createAllApnList: No APN found for carrier, operator: " + operator);
                this.mApnSettingsInitializationLog.log("no APN found for carrier, operator: " + operator);
                this.mPreferredApn = null;
            } else {
                this.mPreferredApn = getPreferredApn();
                if (this.mPreferredApn != null && !this.mPreferredApn.getOperatorNumeric().equals(operator)) {
                    this.mPreferredApn = null;
                    setPreferredApn(-1);
                }
                log("createAllApnList: mPreferredApn=" + this.mPreferredApn);
            }
            log("createAllApnList: X mAllApnSettings=" + this.mAllApnSettings);
            return;
        }
        log("createAllApnList: ignore, sim not ready and no operator numeric");
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
        return new MtkApnSetting(id, dest.getOperatorNumeric(), dest.getEntryName(), dest.getApnName(), proxy, port, mmsc, mmsProxy, mmsPort, dest.getUser(), dest.getPassword(), dest.getAuthType(), resultApnType, protocol, src.getRoamingProtocol() == 2 ? src.getRoamingProtocol() : dest.getRoamingProtocol(), dest.isEnabled(), (dest.getNetworkTypeBitmask() == 0 || src.getNetworkTypeBitmask() == 0) ? 0 : dest.getNetworkTypeBitmask() | src.getNetworkTypeBitmask(), dest.getProfileId(), dest.isPersistent() || src.isPersistent(), dest.getMaxConns(), dest.getWaitTime(), dest.getMaxConnsTime(), dest.getMtu(), dest.getMvnoType(), dest.getMvnoMatchData(), dest.getApnSetId(), dest.getCarrierId(), dest.getSkip464Xlat(), ((MtkApnSetting) dest).inactiveTimer);
    }

    /* access modifiers changed from: protected */
    public String apnListToString(ArrayList<ApnSetting> apns) {
        try {
            return MtkDcTracker.super.apnListToString(apns);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public void setPreferredApn(int pos) {
        if (this.mCanSetPreferApn) {
            log("setPreferredApn: insert pos=" + pos + ", subId=" + this.mPhone.getSubId());
        }
        MtkDcTracker.super.setPreferredApn(pos);
    }

    /* JADX INFO: Multiple debug info for r0v38 int: [D('ar' android.os.AsyncResult), D('newDefaultRefCount' int)] */
    public void handleMessage(Message msg) {
        int lteAccessStratumDataState;
        if (VDBG) {
            log("handleMessage msg=" + msg);
        }
        int i = msg.what;
        boolean allowed = false;
        if (i == 270359) {
            ConnectivityManager cnnm = (ConnectivityManager) this.mPhone.getContext().getSystemService("connectivity");
            log("EVENT_PS_RESTRICT_DISABLED " + this.mIsPsRestricted);
            this.mIsPsRestricted = false;
            if (isConnected()) {
                startNetStatPoll();
                startDataStallAlarm(false);
                return;
            }
            if (this.mState == DctConstants.State.FAILED) {
                cleanUpAllConnectionsInternal(false, "psRestrictEnabled");
                this.mReregisterOnReconnectFailure = false;
            }
            ApnContext apnContext = (ApnContext) this.mApnContextsByType.get(17);
            if (apnContext == null) {
                loge("**** Default ApnContext not found ****");
                if (Build.IS_DEBUGGABLE && cnnm.isNetworkSupported(0)) {
                    throw new RuntimeException("Default ApnContext not found");
                }
            } else if (this.mPhone.getServiceStateTracker().getCurrentDataConnectionState() == 0) {
                apnContext.setReason("psRestrictEnabled");
                trySetupData(apnContext, 1);
            } else {
                log("EVENT_PS_RESTRICT_DISABLED, data not attached, skip.");
            }
            ApnContext apnContext2 = (ApnContext) this.mApnContextsByType.get(2);
            if (apnContext2 == null || !apnContext2.isConnectable()) {
                loge("**** MMS ApnContext not found ****");
                return;
            }
            apnContext2.setReason("psRestrictEnabled");
            trySetupData(apnContext2, 1);
        } else if (i != 270377) {
            int nwLimitState = -1;
            switch (i) {
                case 270839:
                    logd("EVENT_APN_CHANGED_DONE");
                    onApnChangedDone();
                    return;
                case 270840:
                    onFdnChanged();
                    return;
                case 270841:
                    logd("EVENT_RESET_PDP_DONE cid=" + msg.arg1);
                    this.mPhone.notifyDataConnection("default");
                    return;
                case 270842:
                    onProcessPendingSetupData();
                    return;
                case 270843:
                    onMdChangedAttachApn((AsyncResult) msg.obj);
                    return;
                case 270844:
                    log("handleMessage : <EVENT_REG_PLMN_CHANGED>");
                    if (isOp129IaSupport() || isOp17IaSupport()) {
                        handlePlmnChange((AsyncResult) msg.obj);
                        return;
                    }
                    return;
                case 270845:
                    log("handleMessage : <EVENT_REG_SUSPENDED>");
                    if ((isOp129IaSupport() || isOp17IaSupport()) && isNeedToResumeMd()) {
                        handleRegistrationSuspend((AsyncResult) msg.obj);
                        return;
                    }
                    return;
                case 270846:
                    log("handleMessage : <EVENT_SET_RESUME>");
                    if (isOp129IaSupport() || isOp17IaSupport()) {
                        handleSetResume();
                        return;
                    }
                    return;
                case 270847:
                    if (this.mAllApnSettings == null || this.mAllApnSettings.isEmpty()) {
                        log("EVENT_RESET_ATTACH_APN: Ignore due to null APN list");
                        return;
                    } else {
                        setInitialAttachApn();
                        return;
                    }
                case 270848:
                    onSharedDefaultApnState(msg.arg1);
                    return;
                case 270849:
                    AsyncResult ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        int[] ints = (int[]) ar.result;
                        if (ints.length > 0) {
                            lteAccessStratumDataState = ints[0];
                        } else {
                            lteAccessStratumDataState = -1;
                        }
                        if (ints.length > 1) {
                            nwLimitState = ints[1];
                        }
                        if (lteAccessStratumDataState != 1) {
                            notifyPsNetworkTypeChanged(nwLimitState);
                        } else {
                            broadcastPsNetworkTypeChanged(13);
                        }
                        logd("EVENT_LTE_ACCESS_STRATUM_STATE lteAccessStratumDataState = " + lteAccessStratumDataState + ", networkType = " + nwLimitState);
                        notifyLteAccessStratumChanged(lteAccessStratumDataState);
                        return;
                    }
                    loge("LteAccessStratumState exception: " + ar.exception);
                    return;
                case 270850:
                    MtkDcTracker.super.oppoHandleRoamingTypeChange((IccRecords) this.mIccRecords.get());
                    return;
                case 270851:
                    logd("EVENT_MD_DATA_RETRY_COUNT_RESET");
                    if (this.mIsOperatorNumericEmpty) {
                        this.mIsOperatorNumericEmpty = false;
                        if (SubscriptionManager.isValidSubscriptionId(this.mPhone.getSubId())) {
                            onRecordsLoadedOrSubIdChanged();
                            return;
                        }
                        return;
                    }
                    setupDataOnAllConnectableApns(MtkGsmCdmaPhone.REASON_MD_DATA_RETRY_COUNT_RESET, DcTracker.RetryFailures.ALWAYS);
                    return;
                case 270852:
                    if (isDataRetryRestrictEnabled()) {
                        logd("EVENT_REMOVE_RESTRICT_EUTRAN");
                        this.mReregisterOnReconnectFailure = false;
                        setupDataOnAllConnectableApns("psRestrictDisabled", DcTracker.RetryFailures.ALWAYS);
                        return;
                    }
                    return;
                case 270853:
                    AsyncResult ar2 = (AsyncResult) msg.obj;
                    if (ar2 == null || ar2.result == null) {
                        loge("Parameter error: ret should not be NULL");
                        return;
                    }
                    if (((int[]) ar2.result)[0] == 1) {
                        allowed = true;
                    }
                    onAllowChanged(allowed);
                    return;
                case 270854:
                    setupDataOnAllConnectableApns(MtkGsmCdmaPhone.REASON_CARRIER_CONFIG_LOADED, DcTracker.RetryFailures.ALWAYS);
                    return;
                default:
                    switch (i) {
                        case 270856:
                            handlePcoDataAfterAttached((AsyncResult) msg.obj);
                            return;
                        case 270857:
                            this.mDedicatedBearerCount++;
                            AsyncResult ar3 = (AsyncResult) msg.obj;
                            if (ar3.result instanceof MtkDedicateDataCallResponse) {
                                onDedecatedBearerActivated((MtkDedicateDataCallResponse) ar3.result);
                                return;
                            }
                            return;
                        case 270858:
                            AsyncResult ar4 = (AsyncResult) msg.obj;
                            if (ar4.result instanceof MtkDedicateDataCallResponse) {
                                onDedecatedBearerModified((MtkDedicateDataCallResponse) ar4.result);
                                return;
                            }
                            return;
                        case 270859:
                            int i2 = this.mDedicatedBearerCount;
                            if (i2 > 0) {
                                this.mDedicatedBearerCount = i2 - 1;
                            }
                            onDedecatedBearerDeactivated(((Integer) ((AsyncResult) msg.obj).result).intValue());
                            return;
                        default:
                            switch (i) {
                                case 270861:
                                    logd("EVENT_RIL_CONNECTED");
                                    this.mIsModemReset = true;
                                    SystemProperties.set(PROP_APN_CLASS_ICCID + this.mPhone.getPhoneId(), "");
                                    SystemProperties.set(PROP_APN_CLASS + this.mPhone.getPhoneId(), "");
                                    getImsiFromRil();
                                    return;
                                case 270862:
                                    onNetworkRejectReceived((AsyncResult) msg.obj);
                                    return;
                                case 270863:
                                    onTearDownPdnByApnId(msg.arg1);
                                    return;
                                case 270864:
                                    onDataSetupSscMode3(msg.arg1, msg.arg2);
                                    return;
                                case 270865:
                                    logd("EVENT_MODEM_RESET");
                                    this.mIsModemReset = true;
                                    return;
                                case 270866:
                                    logd("EVENT_IMSI_QUERY_DONE");
                                    AsyncResult ar5 = (AsyncResult) msg.obj;
                                    if (ar5.exception != null) {
                                        loge("Exception querying IMSI, Exception:" + ar5.exception);
                                        return;
                                    } else if (msg.arg1 == 2) {
                                        this.mImsiCdma = IccUtils.stripTrailingFs((String) ar5.result);
                                        return;
                                    } else {
                                        this.mImsiGsm = IccUtils.stripTrailingFs((String) ar5.result);
                                        return;
                                    }
                                case 270867:
                                    AsyncResult ar6 = (AsyncResult) msg.obj;
                                    if (ar6.exception != null || ar6.result == null) {
                                        loge("EVENT_MOBILE_DATA_USAGE, exception: " + ar6.exception);
                                        return;
                                    }
                                    updateMobileDataUsage(ar6);
                                    return;
                                case 270868:
                                    logd("EVENT_RECORDS_OVERRIDE");
                                    this.mIsRecordsOverride = true;
                                    return;
                                case 270869:
                                    AsyncResult ar7 = (AsyncResult) msg.obj;
                                    if (ar7.exception == null) {
                                        int[] ints2 = (int[]) ar7.result;
                                        if (ints2.length > 0) {
                                            nwLimitState = ints2[0];
                                        }
                                        logd("nwLimitState = " + nwLimitState);
                                        MtkDcTracker.super.writeLogToPartionForLteLimit(nwLimitState, getCellLocation());
                                        return;
                                    }
                                    loge("EVENT_NW_LIMIT_STATE, exception: " + ar7.exception);
                                    return;
                                case 270870:
                                    logd("EVENT_ICC_REFRESH");
                                    this.mIsSimRefresh = true;
                                    return;
                                default:
                                    MtkDcTracker.super.handleMessage(msg);
                                    return;
                            }
                    }
            }
        } else if (this.mRilRat != this.mPhone.getServiceState().getRilDataRadioTechnology()) {
            this.mTurboSS = null;
            this.mRilRat = this.mPhone.getServiceState().getRilDataRadioTechnology();
            if (!MtkDcHelper.isPreferredDataPhone(this.mPhone)) {
                this.mPhone.notifyDataConnection("default");
            }
            MtkDcTracker.super.handleMessage(msg);
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
        if (TextUtils.equals(apnType, "ia")) {
            return 0;
        }
        if (TextUtils.equals(apnType, "dun")) {
            return 1;
        }
        if (TextUtils.equals(apnType, "mms")) {
            return ExternalSimConstants.MSG_ID_INITIALIZATION_RESPONSE;
        }
        if (TextUtils.equals(apnType, "supl")) {
            return ExternalSimConstants.MSG_ID_GET_PLATFORM_CAPABILITY_RESPONSE;
        }
        if (TextUtils.equals(apnType, "hipri")) {
            return ExternalSimConstants.MSG_ID_EVENT_RESPONSE;
        }
        if (TextUtils.equals(apnType, "wap")) {
            return ExternalSimConstants.MSG_ID_UICC_RESET_REQUEST;
        }
        if (TextUtils.equals(apnType, "emergency")) {
            return ExternalSimConstants.MSG_ID_UICC_APDU_REQUEST;
        }
        if (TextUtils.equals(apnType, "xcap")) {
            return ExternalSimConstants.MSG_ID_UICC_POWER_DOWN_REQUEST;
        }
        if (TextUtils.equals(apnType, "rcs")) {
            return ExternalSimConstants.MSG_ID_GET_SERVICE_STATE_RESPONSE;
        }
        if (TextUtils.equals(apnType, "default")) {
            return 0;
        }
        if (TextUtils.equals(apnType, "bip")) {
            return ExternalSimConstants.MSG_ID_FINALIZATION_RESPONSE;
        }
        if (TextUtils.equals(apnType, "*")) {
            return MtkCharacterSets.SCSU;
        }
        if (TextUtils.equals(apnType, "vsim")) {
            return ExternalSimConstants.MSG_ID_UICC_AUTHENTICATION_DONE_IND;
        }
        if (TextUtils.equals(apnType, "mcx")) {
            return ExternalSimConstants.MSG_ID_UICC_AUTHENTICATION_ABORT_IND;
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    public void onUpdateIcc() {
        int phoneType;
        IccRecords newIccRecords;
        if (this.mUiccController != null) {
            int i = 1;
            if (MtkDcHelper.isCdma3GDualModeCard(this.mPhone.getPhoneId()) || MtkDcHelper.isCdma3GCard(this.mPhone.getPhoneId())) {
                newIccRecords = this.mPhone.getIccRecords();
                phoneType = this.mPhone.getPhoneType();
            } else {
                newIccRecords = getUiccRecords(1);
                phoneType = 1;
            }
            IccRecords r = (IccRecords) this.mIccRecords.get();
            logd("onUpdateIcc: newIccRecords=" + newIccRecords + ", r=" + r);
            if (r != newIccRecords) {
                if (r != null) {
                    log("Removing stale icc objects.");
                    r.unregisterForRecordsLoaded(this);
                    r.unregisterForRecordsOverride(this);
                    MtkDcTracker.super.oppoUnregisterForImsiReady(r);
                    this.mIccRecords.set(null);
                }
                if (newIccRecords == null) {
                    this.mIsSimNotReady = true;
                    this.mIsRecordsOverride = false;
                    onSimNotReady();
                } else if (SubscriptionManager.isValidSubscriptionId(this.mPhone.getSubId())) {
                    log("New records found.");
                    this.mPhoneType = phoneType;
                    this.mIsSimNotReady = false;
                    this.mIsRecordsOverride = false;
                    this.mIccRecords.set(newIccRecords);
                    newIccRecords.registerForRecordsLoaded(this, 270338, (Object) null);
                    newIccRecords.registerForRecordsOverride(this, 270868, (Object) null);
                    MtkDcTracker.super.oppoRegisterForImsiReady(newIccRecords);
                }
            }
            if (this.mUiccCardApplication == null) {
                this.mUiccCardApplication = new AtomicReference<>();
            }
            UiccCardApplication app = this.mUiccCardApplication.get();
            MtkUiccController mtkUiccController = this.mUiccController;
            if (this.mPhone.getPhoneType() == 2) {
                i = 2;
            }
            UiccCardApplication newUiccCardApp = mtkUiccController.getUiccCardApplication(i);
            if (app != newUiccCardApp) {
                if (app != null) {
                    log("Removing stale UiccCardApplication objects.");
                    ((MtkUiccCardApplication) app).unregisterForFdnChanged(this);
                    this.mUiccCardApplication.set(null);
                }
                if (newUiccCardApp != null) {
                    log("New UiccCardApplication found");
                    ((MtkUiccCardApplication) newUiccCardApp).registerForFdnChanged(this, 270840, null);
                    this.mUiccCardApplication.set(newUiccCardApp);
                }
            }
        }
    }

    public void update() {
        synchronized (this.mDataEnabledSettings) {
            MtkDcTracker.super.update();
        }
    }

    /* access modifiers changed from: protected */
    public void onDataEnabledChanged(boolean enable, int enabledChangedReason) {
        String cleanupReason;
        log("onDataEnabledChanged: enable=" + enable + ", enabledChangedReason=" + enabledChangedReason);
        if (enable) {
            reevaluateDataConnections();
            setupDataOnAllConnectableApns("dataEnabled", DcTracker.RetryFailures.ALWAYS);
            return;
        }
        if (enabledChangedReason == 1) {
            cleanupReason = "dataDisabledInternal";
        } else if (enabledChangedReason != 4) {
            cleanupReason = "specificDisabled";
        } else {
            cleanupReason = "carrierActionDisableMeteredApn";
        }
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if (!isDataAllowedAsOff(apnContext.getApnType())) {
                apnContext.setReason(cleanupReason);
                cleanUpConnectionInternal(true, 2, apnContext);
            }
        }
    }

    public void log(String s) {
        logd(s);
    }

    private void loge(String s) {
        String str = this.mLogTag;
        Rlog.e(str, "[Mtk] " + s);
    }

    private void logw(String s) {
        String str = this.mLogTag;
        Rlog.w(str, "[Mtk] " + s);
    }

    private void logi(String s) {
        String str = this.mLogTag;
        Rlog.i(str, "[Mtk] " + s);
    }

    /* access modifiers changed from: private */
    public void logd(String s) {
        String str = this.mLogTag;
        Rlog.d(str, "[Mtk] " + s);
    }

    private void logv(String s) {
        String str = this.mLogTag;
        Rlog.v(str, "[Mtk] " + s);
    }

    public String[] getPcscfAddress(String apnType) {
        MtkDcTracker.super.getPcscfAddress(apnType);
        log("getPcscfAddress() for RCS, apnType=" + apnType);
        ApnContext apnContext = null;
        if (TextUtils.equals(apnType, "default")) {
            apnContext = (ApnContext) this.mApnContextsByType.get(17);
        }
        if (apnContext == null) {
            log("apnContext is null for RCS, return null");
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

    /* access modifiers changed from: protected */
    public void cleanUpConnectionsOnUpdatedApns(boolean detach, String reason) {
        log("cleanUpConnectionsOnUpdatedApns: detach=" + detach);
        if (this.mAllApnSettings.isEmpty()) {
            cleanUpAllConnectionsInternal(detach, "apnChanged");
        } else if (getDataRat() != 0) {
            for (ApnContext apnContext : this.mApnContexts.values()) {
                if (!hasMdAutoSetupImsCapability() || !"nwTypeChanged".equals(reason) || !"emergency".equals(apnContext.getApnType())) {
                    ApnSetting currentApnSetting = apnContext.getApnSetting();
                    if (currentApnSetting == null) {
                        log("cleanUpConnectionsOnUpdatedApns(): currentApnSetting is null");
                    } else {
                        ArrayList<ApnSetting> waitingApns = buildWaitingApns(apnContext.getApnType(), getDataRat());
                        if (VDBG) {
                            log("new waitingApns:" + waitingApns);
                        }
                        boolean canHandle = false;
                        Iterator<ApnSetting> it = waitingApns.iterator();
                        while (true) {
                            if (it.hasNext()) {
                                if (currentApnSetting.equals(it.next(), this.mPhone.getServiceState().getDataRoamingFromRegistration())) {
                                    canHandle = true;
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                        if (!canHandle) {
                            if (VDBG) {
                                log("new waiting apn is different for " + apnContext);
                            }
                            apnContext.setWaitingApns(waitingApns);
                            if (!apnContext.isDisconnected()) {
                                if (VDBG) {
                                    log("cleanUpConnectionsOnUpdatedApns for " + apnContext);
                                }
                                apnContext.setReason(reason);
                                cleanUpConnectionInternal(true, 2, apnContext);
                            }
                        }
                    }
                } else {
                    log("cleanUpConnectionsOnUpdatedApns(): skip emergency due to " + reason);
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
    public void startDataStallAlarm(boolean suspectedStall) {
        if (mtkSkipDataStallAlarm()) {
            log("onDataStallAlarm: switch data-stall off, skip it!");
        } else {
            MtkDcTracker.super.startDataStallAlarm(suspectedStall);
        }
    }

    private void handlePcoDataAfterAttached(AsyncResult ar) {
        IDataConnectionExt iDataConnectionExt = this.mDataConnectionExt;
        if (iDataConnectionExt != null) {
            iDataConnectionExt.handlePcoDataAfterAttached(ar, this.mPhone, this.mAllApnSettings);
        }
    }

    private boolean isDataAllowedExt(DataConnectionReasons dataConnectionReasons, ApnContext apnContext) {
        if (apnContext == null) {
            log("isDataAllowedExt: apnContext is null, return false");
            return false;
        }
        String apnType = apnContext.getApnType();
        if (dataConnectionReasons.contains(DataConnectionReasons.DataDisallowedReasonType.MTK_LOCATED_PLMN_CHANGED)) {
            log("isDataAllowedExt: located plmn changed, setSetupDataPendingFlag");
            this.mPendingDataCall = true;
            return false;
        }
        if (dataConnectionReasons.contains(DataConnectionReasons.DataDisallowedReasonType.DEFAULT_DATA_UNSELECTED)) {
            if (!ignoreDefaultDataUnselected(apnType)) {
                return false;
            }
            dataConnectionReasons.mDataDisallowedReasonSet.remove(DataConnectionReasons.DataDisallowedReasonType.DEFAULT_DATA_UNSELECTED);
        }
        if (dataConnectionReasons.contains(DataConnectionReasons.DataDisallowedReasonType.ROAMING_DISABLED)) {
            if (!ignoreDataRoaming(apnType) && !getDomesticRoamingEnabled()) {
                return false;
            }
            dataConnectionReasons.mDataDisallowedReasonSet.remove(DataConnectionReasons.DataDisallowedReasonType.ROAMING_DISABLED);
        }
        if (dataConnectionReasons.contains(DataConnectionReasons.DataDisallowedReasonType.MTK_NOT_ALLOWED)) {
            if (!ignoreDataAllow(apnType)) {
                return false;
            }
            dataConnectionReasons.mDataDisallowedReasonSet.remove(DataConnectionReasons.DataDisallowedReasonType.MTK_NOT_ALLOWED);
        }
        if (dataConnectionReasons.contains(DataConnectionReasons.DataDisallowedReasonType.MTK_NON_VSIM_PDN_NOT_ALLOWED)) {
            if (!TextUtils.equals(apnType, "vsim")) {
                return false;
            }
            dataConnectionReasons.mDataDisallowedReasonSet.remove(DataConnectionReasons.DataDisallowedReasonType.MTK_NON_VSIM_PDN_NOT_ALLOWED);
        }
        if (dataConnectionReasons.contains(DataConnectionReasons.DataDisallowedReasonType.MTK_FDN_ENABLED)) {
            if (!"emergency".equals(apnType) && !"ims".equals(apnType)) {
                return false;
            }
            log("isDataAllowedExt allow IMS/EIMS for reason FDN_ENABLED");
            dataConnectionReasons.mDataDisallowedReasonSet.remove(DataConnectionReasons.DataDisallowedReasonType.MTK_FDN_ENABLED);
        }
        if (dataConnectionReasons.contains(DataConnectionReasons.DataDisallowedReasonType.APN_NOT_CONNECTABLE)) {
            if (apnContext.getState() != DctConstants.State.CONNECTED || !MtkGsmCdmaPhone.REASON_DATA_SETUP_SSC_MODE3.equals(apnContext.getReason())) {
                return false;
            }
            log("isDataAllowedExt allow SSC mode3 for reason APN_NOT_CONNECTABLE");
            dataConnectionReasons.mDataDisallowedReasonSet.remove(DataConnectionReasons.DataDisallowedReasonType.APN_NOT_CONNECTABLE);
        }
        if (VDBG) {
            log("isDataAllowedExt: " + dataConnectionReasons.allowed());
        }
        return dataConnectionReasons.allowed();
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0085  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x00b8  */
    private int getDefaultMtuConfig(Context context) {
        String operator = mtkGetOperatorNumeric((IccRecords) this.mIccRecords.get());
        int mcc = 0;
        int mnc = 0;
        if (operator != null && operator.length() > 3) {
            try {
                mcc = Integer.parseInt(operator.substring(0, 3));
                mnc = Integer.parseInt(operator.substring(3, operator.length()));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                loge("operator numeric is invalid");
            }
        }
        Resources sysResource = context.getResources();
        int sysMcc = sysResource.getConfiguration().mcc;
        int sysMnc = sysResource.getConfiguration().mnc;
        Resources resource = null;
        try {
            new Configuration();
            Configuration configuration = context.getResources().getConfiguration();
            configuration.mcc = mcc;
            configuration.mnc = mnc;
            try {
                resource = context.createConfigurationContext(configuration).getResources();
            } catch (Exception e2) {
                e = e2;
                e.printStackTrace();
                loge("getResourcesUsingMccMnc fail");
                if (resource == null) {
                }
            }
        } catch (Exception e3) {
            e = e3;
            e.printStackTrace();
            loge("getResourcesUsingMccMnc fail");
            if (resource == null) {
            }
        }
        if (resource == null) {
            int mtu = sysResource.getInteger(17694846);
            logd("getDefaultMtuConfig: get sysResource sysMcc = " + sysMcc + ", sysMnc = " + sysMnc + ", mcc = " + mcc + ", mnc = " + mnc + ", mtu = " + mtu);
            return mtu;
        }
        int mtu2 = resource.getInteger(17694846);
        logd("getDefaultMtuConfig: get resource sysMcc = " + sysMcc + ", sysMnc = " + sysMnc + ", mcc = " + mcc + ", mnc = " + mnc + ", mtu = " + mtu2);
        return mtu2;
    }

    private void onMdChangedAttachApn(AsyncResult ar) {
        logv("onMdChangedAttachApn");
        int apnId = ((Integer) ar.result).intValue();
        if (apnId == 1 || apnId == 3) {
            int phoneId = this.mPhone.getPhoneId();
            if (SubscriptionManager.isValidPhoneId(phoneId)) {
                String iccId = SystemProperties.get(this.PROPERTY_ICCID[phoneId], "");
                SystemProperties.set(PROP_APN_CLASS_ICCID + phoneId, iccId);
                SystemProperties.set(PROP_APN_CLASS + phoneId, String.valueOf(apnId));
                log("onMdChangedAttachApn, set " + iccId + ", " + apnId);
            }
            updateMdChangedAttachApn(apnId);
            if (this.mMdChangedAttachApn != null) {
                setInitialAttachApn();
            } else {
                logw("onMdChangedAttachApn: MdChangedAttachApn is null, not found APN");
            }
        } else {
            logw("onMdChangedAttachApn: Not handle APN Class:" + apnId);
        }
    }

    private void updateMdChangedAttachApn(int apnId) {
        if (this.mAllApnSettings != null && !this.mAllApnSettings.isEmpty()) {
            Iterator it = this.mAllApnSettings.iterator();
            while (it.hasNext()) {
                ApnSetting apn = (ApnSetting) it.next();
                if (apnId == 1 && apn.canHandleType(64)) {
                    this.mMdChangedAttachApn = apn;
                    log("updateMdChangedAttachApn: MdChangedAttachApn=" + apn);
                    return;
                } else if (apnId == 3 && apn.canHandleType(17)) {
                    this.mMdChangedAttachApn = apn;
                    log("updateMdChangedAttachApn: MdChangedAttachApn=" + apn);
                    return;
                }
            }
        }
    }

    private boolean isMdChangedAttachApnEnabled() {
        if (this.mMdChangedAttachApn == null || this.mAllApnSettings == null || this.mAllApnSettings.isEmpty()) {
            return false;
        }
        Iterator it = this.mAllApnSettings.iterator();
        while (it.hasNext()) {
            ApnSetting apn = (ApnSetting) it.next();
            if (TextUtils.equals(this.mMdChangedAttachApn.getApnName(), apn.getApnName())) {
                log("isMdChangedAttachApnEnabled: " + apn);
                return apn.isEnabled();
            }
        }
        return false;
    }

    private void sendOnApnChangedDone(boolean bImsApnChanged) {
        Message msg = obtainMessage(270839);
        msg.arg1 = bImsApnChanged ? 1 : 0;
        sendMessage(msg);
    }

    private void onApnChangedDone() {
        logd("onApnChangedDone: subId = " + this.mPhone.getSubId() + ", default data subId = " + SubscriptionManager.getDefaultDataSubscriptionId());
        if (this.mPhone.getSubId() == SubscriptionManager.getDefaultDataSubscriptionId()) {
            setupDataOnAllConnectableApns("apnChanged", DcTracker.RetryFailures.ALWAYS);
            return;
        }
        ApnContext apnContextDefault = (ApnContext) this.mApnContexts.get("default");
        if (apnContextDefault != null && apnContextDefault.isConnectable()) {
            log("Temp data switch is active , call setupDataOnAllConnectableApns()");
            setupDataOnAllConnectableApns("apnChanged", DcTracker.RetryFailures.ALWAYS);
        }
    }

    private void registerFdnContentObserver() {
        Uri fdnContentUri;
        if (isFdnEnableSupport()) {
            if (SubscriptionManager.isValidSubscriptionId(this.mPhone.getSubId())) {
                fdnContentUri = Uri.parse(FDN_CONTENT_URI_WITH_SUB_ID + this.mPhone.getSubId());
            } else {
                fdnContentUri = Uri.parse(FDN_CONTENT_URI);
            }
            this.mSettingsObserver.observe(fdnContentUri, 270840);
        }
    }

    private boolean isFdnEnableSupport() {
        IDataConnectionExt iDataConnectionExt = this.mDataConnectionExt;
        if (iDataConnectionExt != null) {
            return iDataConnectionExt.isFdnEnableSupport();
        }
        return false;
    }

    private boolean isFdnEnabled() {
        boolean bFdnEnabled = false;
        if (isFdnEnableSupport()) {
            IMtkTelephonyEx telephonyEx = IMtkTelephonyEx.Stub.asInterface(ServiceManager.getService("phoneEx"));
            if (telephonyEx != null) {
                try {
                    bFdnEnabled = telephonyEx.isFdnEnabled(this.mPhone.getSubId());
                    log("isFdnEnabled(), bFdnEnabled = " + bFdnEnabled);
                    if (bFdnEnabled) {
                        if (this.mIsFdnChecked) {
                            log("isFdnEnabled(), match FDN for allow data = " + this.mIsMatchFdnForAllowData);
                            return !this.mIsMatchFdnForAllowData;
                        }
                        boolean bPhbReady = telephonyEx.isPhbReady(this.mPhone.getSubId());
                        log("isFdnEnabled(), bPhbReady = " + bPhbReady);
                        if (bPhbReady) {
                            this.mWorkerHandler.sendEmptyMessage(270860);
                        } else if (!this.mIsPhbStateChangedIntentRegistered) {
                            IntentFilter filter = new IntentFilter();
                            filter.addAction("mediatek.intent.action.PHB_STATE_CHANGED");
                            this.mPhone.getContext().registerReceiver(this.mPhbStateChangedIntentReceiver, filter);
                            this.mIsPhbStateChangedIntentRegistered = true;
                        }
                    } else if (this.mIsPhbStateChangedIntentRegistered) {
                        this.mIsPhbStateChangedIntentRegistered = false;
                        this.mPhone.getContext().unregisterReceiver(this.mPhbStateChangedIntentReceiver);
                    }
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            } else {
                loge("isFdnEnabled(), get telephonyEx failed!!");
            }
        }
        return bFdnEnabled;
    }

    /* access modifiers changed from: private */
    public void onFdnChanged() {
        if (isFdnEnableSupport()) {
            log("onFdnChanged()");
            boolean bFdnEnabled = false;
            boolean bPhbReady = false;
            IMtkTelephonyEx telephonyEx = IMtkTelephonyEx.Stub.asInterface(ServiceManager.getService("phoneEx"));
            if (telephonyEx != null) {
                try {
                    bFdnEnabled = telephonyEx.isFdnEnabled(this.mPhone.getSubId());
                    bPhbReady = telephonyEx.isPhbReady(this.mPhone.getSubId());
                    log("onFdnChanged(), bFdnEnabled = " + bFdnEnabled + ", bPhbReady = " + bPhbReady);
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            } else {
                loge("onFdnChanged(), get telephonyEx failed!!");
            }
            if (bPhbReady) {
                if (bFdnEnabled) {
                    log("fdn enabled, check fdn list");
                    this.mWorkerHandler.sendEmptyMessage(270860);
                    return;
                }
                log("fdn disabled, call setupDataOnAllConnectableApns()");
                setupDataOnAllConnectableApns(MtkGsmCdmaPhone.REASON_FDN_DISABLED, DcTracker.RetryFailures.ALWAYS);
            } else if (!this.mIsPhbStateChangedIntentRegistered) {
                IntentFilter filter = new IntentFilter();
                filter.addAction("mediatek.intent.action.PHB_STATE_CHANGED");
                this.mPhone.getContext().registerReceiver(this.mPhbStateChangedIntentReceiver, filter);
                this.mIsPhbStateChangedIntentRegistered = true;
            }
        } else {
            log("not support fdn enabled, skip onFdnChanged");
        }
    }

    /* access modifiers changed from: private */
    public void cleanOrSetupDataConnByCheckFdn() {
        Uri uriFdn;
        log("cleanOrSetupDataConnByCheckFdn()");
        if (SubscriptionManager.isValidSubscriptionId(this.mPhone.getSubId())) {
            uriFdn = Uri.parse(FDN_CONTENT_URI_WITH_SUB_ID + this.mPhone.getSubId());
        } else {
            uriFdn = Uri.parse(FDN_CONTENT_URI);
        }
        Cursor cursor = this.mPhone.getContext().getContentResolver().query(uriFdn, new String[]{PplMessageManager.PendingMessage.KEY_NUMBER}, null, null, null);
        this.mIsMatchFdnForAllowData = false;
        if (cursor != null) {
            this.mIsFdnChecked = true;
            if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                while (true) {
                    String strFdnNumber = cursor.getString(cursor.getColumnIndexOrThrow(PplMessageManager.PendingMessage.KEY_NUMBER));
                    log("strFdnNumber = " + strFdnNumber);
                    if (!strFdnNumber.equals(FDN_FOR_ALLOW_DATA)) {
                        if (!cursor.moveToNext()) {
                            break;
                        }
                    } else {
                        this.mIsMatchFdnForAllowData = true;
                        break;
                    }
                }
            }
            cursor.close();
        }
        if (this.mIsMatchFdnForAllowData) {
            log("match FDN for allow data, call setupDataOnAllConnectableApns()");
            setupDataOnAllConnectableApns(MtkGsmCdmaPhone.REASON_FDN_DISABLED, DcTracker.RetryFailures.ALWAYS);
            return;
        }
        log("not match FDN for allow data, call cleanUpAllConnections(REASON_FDN_ENABLED)");
        cleanUpAllConnectionsInternal(true, MtkGsmCdmaPhone.REASON_FDN_ENABLED);
    }

    private void createWorkerHandler() {
        if (this.mWorkerHandler == null) {
            new Thread() {
                /* class com.mediatek.internal.telephony.dataconnection.MtkDcTracker.AnonymousClass5 */

                public void run() {
                    Looper.prepare();
                    MtkDcTracker mtkDcTracker = MtkDcTracker.this;
                    Handler unused = mtkDcTracker.mWorkerHandler = new WorkerHandler();
                    Looper.loop();
                }
            }.start();
        }
    }

    private class WorkerHandler extends Handler {
        private WorkerHandler() {
        }

        public void handleMessage(Message msg) {
            if (msg.what == 270860) {
                MtkDcTracker.this.cleanOrSetupDataConnByCheckFdn();
            }
        }
    }

    private boolean ignoreDataRoaming(String apnType) {
        boolean ignoreDataRoaming = false;
        MtkDcHelper dcHelper = MtkDcHelper.getInstance();
        try {
            ignoreDataRoaming = this.mDataConnectionExt.ignoreDataRoaming(apnType);
        } catch (Exception e) {
            loge("get ignoreDataRoaming fail!");
            e.printStackTrace();
        }
        if (dcHelper.isOperatorMccMnc(MtkDcHelper.Operator.OP156, this.mPhone.getPhoneId())) {
            boolean isOverEpdg = this.mPhone.getServiceState().getIwlanRegState() == 0;
            log("ignoreDataRoaming: OP156 check apnType = " + apnType + ", Epdg=" + isOverEpdg);
            if (isOverEpdg && (apnType.equals("mms") || apnType.equals("xcap"))) {
                ignoreDataRoaming = true;
            }
        }
        if (ignoreDataRoaming) {
            logd("ignoreDataRoaming: " + ignoreDataRoaming + ", apnType = " + apnType);
            return ignoreDataRoaming;
        }
        MtkIccCardConstants.VsimType type = MtkUiccController.getVsimCardType(this.mPhone.getPhoneId());
        if (type == MtkIccCardConstants.VsimType.REMOTE_SIM) {
            log("RSim, set ignoreDataRoaming as true for any apn type");
            return true;
        } else if (!TextUtils.equals(apnType, "vsim") || type != MtkIccCardConstants.VsimType.SOFT_AKA_SIM) {
            return ignoreDataRoaming;
        } else {
            log("Aka sim and soft sim, set ignoreDataRoaming as true for vsim type");
            return true;
        }
    }

    private boolean ignoreDefaultDataUnselected(String apnType) {
        boolean ignoreDefaultDataUnselected = false;
        try {
            ignoreDefaultDataUnselected = this.mDataConnectionExt.ignoreDefaultDataUnselected(apnType);
        } catch (Exception e) {
            loge("get ignoreDefaultDataUnselected fail!");
            e.printStackTrace();
        }
        if (ignoreDefaultDataUnselected) {
            logd("ignoreDefaultDataUnselected: " + ignoreDefaultDataUnselected + ", apnType = " + apnType);
        }
        if (ignoreDefaultDataUnselected || !TextUtils.equals(apnType, "vsim")) {
            return ignoreDefaultDataUnselected;
        }
        log("Vsim is enabled, set ignoreDefaultDataUnselected as true");
        return true;
    }

    private boolean ignoreDataAllow(String apnType) {
        boolean ignoreDataAllow = false;
        if ("ims".equals(apnType)) {
            ignoreDataAllow = true;
        }
        if (ignoreDataAllow || !TextUtils.equals(apnType, "vsim")) {
            return ignoreDataAllow;
        }
        log("Vsim is enabled, set ignoreDataAllow as true");
        return true;
    }

    private boolean getDomesticRoamingEnabled() {
        log("getDomesticRoamingEnabled: isDomesticRoaming=" + isDomesticRoaming() + ", bDomesticRoamingEnabled=" + getDomesticRoamingEnabledBySim());
        return isDomesticRoaming() && getDomesticRoamingEnabledBySim();
    }

    private boolean getIntlRoamingEnabled() {
        log("getIntlRoamingEnabled: isIntlRoaming=" + isIntlRoaming() + ", bIntlRoamingEnabled=" + this.mCcIntlRoamingEnabled);
        return isIntlRoaming() && this.mCcIntlRoamingEnabled;
    }

    private boolean isDomesticRoaming() {
        return this.mPhone.getServiceState().getDataRoamingType() == 2;
    }

    private boolean isIntlRoaming() {
        return this.mPhone.getServiceState().getDataRoamingType() == 3;
    }

    public void onRoamingTypeChanged() {
        boolean bDataOnRoamingEnabled = getDataRoamingEnabled();
        boolean bUserDataEnabled = this.mDataEnabledSettings.isUserDataEnabled();
        boolean bDomesticSpecialSim = getDomesticRoamingEnabledBySim();
        boolean bIntlSpecialSim = this.mCcIntlRoamingEnabled;
        boolean bDomAndIntRoamingFeatureEnabled = isUniqueRoamingFeatureEnabled();
        boolean trySetup = false;
        log("onRoamingTypeChanged: bDataOnRoamingEnabled = " + bDataOnRoamingEnabled + ", bUserDataEnabled = " + bUserDataEnabled + ", bDomesticSpecialSim = " + bDomesticSpecialSim + ", bIntlSpecialSim = " + bIntlSpecialSim + ", bDomAndIntRoamingFeatureEnabled = " + bDomAndIntRoamingFeatureEnabled + ", bOneSettingForRoamingFeatureEnabled = " + this.mCcOneSettingForRoaming + ", roamingType = " + this.mPhone.getServiceState().getDataRoamingType());
        if (!this.mPhone.getServiceState().getDataRoaming()) {
            log("onRoamingTypeChanged: device is not roaming. ignored the request.");
        } else if (this.mCcOneSettingForRoaming && TelephonyManager.getDefault().getSimCount() == 1) {
            onDataRoamingOnOrSettingsChanged(270850);
        } else if (bDomesticSpecialSim || bIntlSpecialSim || bDomAndIntRoamingFeatureEnabled) {
            if (!bDomAndIntRoamingFeatureEnabled) {
                boolean z = false;
                if (isDomesticRoaming()) {
                    if (!bDomesticSpecialSim) {
                        if (bUserDataEnabled && bDataOnRoamingEnabled) {
                            z = true;
                        }
                        trySetup = z;
                    } else if (bUserDataEnabled) {
                        trySetup = true;
                    } else {
                        trySetup = false;
                    }
                } else if (!isIntlRoaming()) {
                    loge("onRoamingTypeChanged error: unexpected roaming type");
                } else if (!bIntlSpecialSim) {
                    if (bUserDataEnabled && bDataOnRoamingEnabled) {
                        z = true;
                    }
                    trySetup = z;
                } else if (bDataOnRoamingEnabled) {
                    trySetup = true;
                } else {
                    trySetup = false;
                }
            } else if (checkDomesticDataRoamingEnabled() || checkInternationalDataRoamingEnabled()) {
                trySetup = true;
            } else {
                trySetup = false;
            }
            if (trySetup) {
                log("onRoamingTypeChanged: setup data on roaming");
                setupDataOnAllConnectableApns("roamingOn", DcTracker.RetryFailures.ALWAYS);
                this.mPhone.notifyDataConnection();
                return;
            }
            log("onRoamingTypeChanged: Tear down data connection on roaming.");
            cleanUpAllConnectionsInternal(true, "roamingOn");
        } else {
            log("onRoamingTypeChanged: is not specific SIM. ignored the request.");
        }
    }

    private long getDisconnectDoneRetryTimer(String reason, long delay) {
        if ("apnChanged".equals(reason)) {
            return 3000;
        }
        IDataConnectionExt iDataConnectionExt = this.mDataConnectionExt;
        if (iDataConnectionExt == null) {
            return delay;
        }
        try {
            return iDataConnectionExt.getDisconnectDoneRetryTimer(reason, delay);
        } catch (Exception e) {
            loge("DataConnectionExt.getDisconnectDoneRetryTimer fail!");
            e.printStackTrace();
            return delay;
        }
    }

    private ArrayList<ApnSetting> buildWifiApns(String requestedApnType) {
        log("buildWifiApns: E requestedApnType=" + requestedApnType);
        ArrayList<ApnSetting> apnList = new ArrayList<>();
        if (this.mAllApnSettings != null) {
            log("buildWaitingApns: mAllApnSettings=" + this.mAllApnSettings);
            int requestedApnTypeBitmask = ApnSetting.getApnTypesBitmaskFromString(requestedApnType);
            Iterator it = this.mAllApnSettings.iterator();
            while (it.hasNext()) {
                ApnSetting apn = (ApnSetting) it.next();
                if (apn.canHandleType(requestedApnTypeBitmask) && isWifiOnlyApn(apn.getNetworkTypeBitmask())) {
                    apnList.add(apn);
                }
            }
        }
        log("buildWifiApns: X apnList=" + apnList);
        return apnList;
    }

    private boolean isWifiOnlyApn(int networkTypeBitmask) {
        if (networkTypeBitmask != 0 && (networkTypeBitmask & 16646143) == 0) {
            return true;
        }
        return false;
    }

    public void deactivatePdpByCid(int cid) {
        this.mDataServiceManager.deactivateDataCall(cid, 1, obtainMessage(270841, cid, 0));
    }

    private void onSharedDefaultApnState(int newDefaultRefCount) {
        logd("onSharedDefaultApnState: newDefaultRefCount = " + newDefaultRefCount + ", curDefaultRefCount = " + this.mDefaultRefCount);
        if (newDefaultRefCount != this.mDefaultRefCount) {
            if (newDefaultRefCount > 1) {
                this.mIsSharedDefaultApn = true;
            } else {
                this.mIsSharedDefaultApn = false;
            }
            this.mDefaultRefCount = newDefaultRefCount;
            logd("onSharedDefaultApnState: mIsSharedDefaultApn = " + this.mIsSharedDefaultApn);
            broadcastSharedDefaultApnStateChanged(this.mIsSharedDefaultApn);
        }
    }

    public void onSetLteAccessStratumReport(boolean enabled, Message response) {
        this.mPhone.mCi.setLteAccessStratumReport(enabled, response);
    }

    public void onSetLteUplinkDataTransfer(int timeMillis, Message response) {
        for (ApnContext apnContext : this.mApnContexts.values()) {
            if ("default".equals(apnContext.getApnType())) {
                try {
                    DataConnection dataConnection = apnContext.getDataConnection();
                    if (dataConnection != null) {
                        this.mPhone.mCi.setLteUplinkDataTransfer(timeMillis, dataConnection.getCid(), response);
                    }
                } catch (Exception e) {
                    loge("getDcAc fail!");
                    e.printStackTrace();
                    if (response != null) {
                        AsyncResult.forMessage(response, (Object) null, new CommandException(CommandException.Error.GENERIC_FAILURE));
                        response.sendToTarget();
                    }
                }
            }
        }
    }

    private void notifyLteAccessStratumChanged(int lteAccessStratumDataState) {
        String str;
        if (lteAccessStratumDataState == 1) {
            str = "connected";
        } else {
            str = "idle";
        }
        this.mLteAccessStratumDataState = str;
        logd("notifyLteAccessStratumChanged mLteAccessStratumDataState = " + this.mLteAccessStratumDataState);
        broadcastLteAccessStratumChanged(this.mLteAccessStratumDataState);
    }

    private void notifyPsNetworkTypeChanged(int newRilNwType) {
        this.mPhone.getServiceState();
        int newNwType = ServiceState.rilRadioTechnologyToNetworkType(newRilNwType);
        logd("notifyPsNetworkTypeChanged mNetworkType = " + this.mNetworkType + ", newNwType = " + newNwType + ", newRilNwType = " + newRilNwType);
        if (newNwType != this.mNetworkType) {
            this.mNetworkType = newNwType;
            broadcastPsNetworkTypeChanged(this.mNetworkType);
        }
    }

    public String getLteAccessStratumState() {
        return this.mLteAccessStratumDataState;
    }

    public boolean isSharedDefaultApn() {
        return this.mIsSharedDefaultApn;
    }

    private void broadcastLteAccessStratumChanged(String state) {
        Intent intent = new Intent("com.mediatek.intent.action.LTE_ACCESS_STRATUM_STATE_CHANGED");
        intent.putExtra("lteAccessStratumState", state);
        this.mPhone.getContext().sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.READ_PHONE_STATE");
    }

    private void broadcastPsNetworkTypeChanged(int nwType) {
        Intent intent = new Intent("com.mediatek.intent.action.PS_NETWORK_TYPE_CHANGED");
        intent.putExtra("psNetworkType", nwType);
        this.mPhone.getContext().sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.READ_PHONE_STATE");
    }

    private void broadcastSharedDefaultApnStateChanged(boolean isSharedDefaultApn) {
        Intent intent = new Intent("com.mediatek.intent.action.SHARED_DEFAULT_APN_STATE_CHANGED");
        intent.putExtra("sharedDefaultApn", isSharedDefaultApn);
        this.mPhone.getContext().sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.READ_PHONE_STATE");
    }

    private boolean isDataAllowedAsOff(String apnType) {
        boolean isDataAllowedAsOff = false;
        MtkDcHelper.getInstance();
        IDataConnectionExt iDataConnectionExt = this.mDataConnectionExt;
        if (iDataConnectionExt != null) {
            isDataAllowedAsOff = iDataConnectionExt.isDataAllowedAsOff(apnType);
        }
        if (this.mCcIntlRoamingEnabled) {
            log("isDataAllowedAsOff: getDataRoamingEnabled=" + getDataRoamingEnabled() + ", bIsInternationalRoaming=" + isIntlRoaming());
            if (getDataRoamingEnabled() && isIntlRoaming()) {
                isDataAllowedAsOff = true;
            }
        }
        if (isDataAllowedAsOff || !TextUtils.equals(apnType, "vsim") || !MtkUiccController.getVsimCardType(this.mPhone.getPhoneId()).isUserDataAllowed()) {
            return isDataAllowedAsOff;
        }
        log("Vsim is enabled, set isDataAllowedAsOff true");
        return true;
    }

    private boolean getDomesticDataRoamingEnabledFromSettings() {
        int phoneId = this.mPhone.getPhoneId();
        boolean isDomDataRoamingEnabled = false;
        try {
            ContentResolver contentResolver = this.mResolver;
            StringBuilder sb = new StringBuilder();
            sb.append("domestic_data_roaming");
            sb.append(this.mPhone.getSubId());
            isDomDataRoamingEnabled = Settings.Global.getInt(contentResolver, sb.toString()) != 0;
        } catch (Settings.SettingNotFoundException snfe) {
            log("getDomesticDataRoamingEnabled: SettingNofFoundException snfe=" + snfe);
        }
        if (VDBG) {
            log("getDomesticDataRoamingEnabled: phoneId=" + phoneId + " isDomDataRoamingEnabled=" + isDomDataRoamingEnabled);
        }
        return isDomDataRoamingEnabled;
    }

    private boolean getInternationalDataRoamingEnabledFromSettings() {
        int phoneId = this.mPhone.getPhoneId();
        boolean isIntDataRoamingEnabled = true;
        try {
            ContentResolver contentResolver = this.mResolver;
            StringBuilder sb = new StringBuilder();
            sb.append("international_data_roaming");
            sb.append(this.mPhone.getSubId());
            isIntDataRoamingEnabled = Settings.Global.getInt(contentResolver, sb.toString()) != 0;
        } catch (Settings.SettingNotFoundException snfe) {
            log("getInternationalDataRoamingEnabled: SettingNofFoundException snfe=" + snfe);
        }
        if (VDBG) {
            log("getInternationalDataRoamingEnabled: phoneId=" + phoneId + " isIntDataRoamingEnabled=" + isIntDataRoamingEnabled);
        }
        return isIntDataRoamingEnabled;
    }

    private boolean isDataRoamingTypeAllowed() {
        boolean isDataRoamingTypeAllowed = false;
        if (isUniqueRoamingFeatureEnabled()) {
            boolean bDomDataOnRoamingEnabled = getDomesticDataRoamingEnabledFromSettings();
            boolean bIntDataOnRoamingEnabled = getInternationalDataRoamingEnabledFromSettings();
            log("isDataRoamingTypeAllowed bDomDataOnRoamingEnabled=" + bDomDataOnRoamingEnabled + ", bIntDataOnRoamingEnabled=" + bIntDataOnRoamingEnabled + ", getDataRoaming=" + this.mPhone.getServiceState().getDataRoaming() + ", currentRoamingType=" + this.mPhone.getServiceState().getDataRoamingType() + ", mUserDataEnabled=" + this.mDataEnabledSettings.isUserDataEnabled());
            if (!this.mPhone.getServiceState().getDataRoaming() || ((bDomDataOnRoamingEnabled && isDomesticRoaming()) || (bIntDataOnRoamingEnabled && isIntlRoaming()))) {
                isDataRoamingTypeAllowed = true;
            } else {
                isDataRoamingTypeAllowed = false;
            }
        }
        log("isDataRoamingTypeAllowed : " + isDataRoamingTypeAllowed);
        return isDataRoamingTypeAllowed;
    }

    public boolean getPendingDataCallFlag() {
        return this.mPendingDataCall;
    }

    private boolean isLocatedPlmnChanged() {
        if (this.mPhone.getPhoneType() == 2) {
            return false;
        }
        return this.mPhone.getServiceStateTracker().willLocatedPlmnChange();
    }

    private void onProcessPendingSetupData() {
        setupDataOnAllConnectableApns(MtkGsmCdmaPhone.REASON_RESUME_PENDING_DATA, DcTracker.RetryFailures.ALWAYS);
    }

    public void processPendingSetupData(MtkServiceStateTracker sst) {
        this.mPendingDataCall = false;
        sendMessage(obtainMessage(270842));
    }

    public int getClassType(ApnSetting apn) {
        int classType = 3;
        if (apn.canHandleType(512) || VZW_EMERGENCY_NI.compareToIgnoreCase(apn.getApnName()) == 0) {
            classType = 0;
        } else if (apn.canHandleType(64) || VZW_IMS_NI.compareToIgnoreCase(apn.getApnName()) == 0) {
            classType = 1;
        } else if (VZW_ADMIN_NI.compareToIgnoreCase(apn.getApnName()) == 0) {
            classType = 2;
        } else if (VZW_APP_NI.compareToIgnoreCase(apn.getApnName()) == 0) {
            classType = 4;
        } else if (VZW_800_NI.compareToIgnoreCase(apn.getApnName()) == 0) {
            classType = 5;
        } else if (apn.canHandleType(17)) {
            classType = 3;
        } else {
            log("getClassType: set to default class 3");
        }
        logd("getClassType:" + classType);
        return classType;
    }

    public ApnSetting getClassTypeApn(int classType) {
        String apnName;
        ApnSetting classTypeApn = null;
        if (classType == 0) {
            apnName = VZW_EMERGENCY_NI;
        } else if (1 == classType) {
            apnName = VZW_IMS_NI;
        } else if (2 == classType) {
            apnName = VZW_ADMIN_NI;
        } else if (3 == classType) {
            apnName = VZW_INTERNET_NI;
        } else if (4 == classType) {
            apnName = VZW_APP_NI;
        } else if (5 == classType) {
            apnName = VZW_800_NI;
        } else {
            log("getClassTypeApn: can't handle class:" + classType);
            return null;
        }
        if (this.mAllApnSettings != null) {
            Iterator it = this.mAllApnSettings.iterator();
            while (it.hasNext()) {
                ApnSetting apn = (ApnSetting) it.next();
                if (apnName.compareToIgnoreCase(apn.getApnName()) == 0) {
                    classTypeApn = apn;
                }
            }
        }
        logd("getClassTypeApn:" + classTypeApn + ", class:" + classType);
        return classTypeApn;
    }

    private void handleSetResume() {
        if (SubscriptionManager.isValidPhoneId(this.mPhone.getPhoneId())) {
            this.mPhone.mCi.setResumeRegistration(this.mSuspendId, null);
        }
    }

    private void handleRegistrationSuspend(AsyncResult ar) {
        if (ar.exception != null || ar.result == null) {
            log("handleRegistrationSuspend: AsyncResult is wrong " + ar.exception);
            return;
        }
        log("handleRegistrationSuspend: createAllApnList and set initial attach APN");
        this.mSuspendId = ((int[]) ar.result)[0];
        log("handleRegistrationSuspend: suspending with Id=" + this.mSuspendId);
        synchronized (this.mNeedsResumeModemLock) {
            this.mNeedsResumeModem = true;
        }
        createAllApnList();
        setInitialAttachApn();
    }

    private void handlePlmnChange(AsyncResult ar) {
        if (ar.exception != null || ar.result == null) {
            log("AsyncResult is wrong " + ar.exception);
            return;
        }
        String[] plmnString = (String[]) ar.result;
        for (int i = 0; i < plmnString.length; i++) {
            logd("plmnString[" + i + "]=" + plmnString[i]);
        }
        this.mRegion = getRegion(plmnString[0]);
        if (TextUtils.isEmpty(mtkGetOperatorNumeric((IccRecords) this.mIccRecords.get())) || isNeedToResumeMd() || this.mPhone.getPhoneId() != SubscriptionManager.getPhoneId(SubscriptionController.getInstance().getDefaultDataSubId())) {
            logd("No need to update APN for Operator");
            return;
        }
        logd("handlePlmnChange: createAllApnList and set initial attach APN");
        createAllApnList();
        setInitialAttachApn();
    }

    private int getRegion(String plmn) {
        if (plmn == null || plmn.equals("") || plmn.length() < 5) {
            logd("[getRegion] Invalid PLMN");
            return 0;
        }
        String currentMcc = plmn.substring(0, 3);
        for (String mcc : MCC_TABLE_TEST) {
            if (currentMcc.equals(mcc)) {
                logd("[getRegion] Test PLMN");
                return 0;
            }
        }
        String[] strArr = MCC_TABLE_DOMESTIC;
        if (strArr.length <= 0) {
            logd("[getRegion] REGION_UNKNOWN");
            return 0;
        } else if (currentMcc.equals(strArr[0])) {
            logd("[getRegion] REGION_DOMESTIC");
            return 1;
        } else {
            logd("[getRegion] REGION_FOREIGN");
            return 2;
        }
    }

    public boolean getImsEnabled() {
        return false;
    }

    public boolean checkIfDomesticInitialAttachApn(String currentMcc) {
        boolean isMccDomestic = false;
        String[] strArr = MCC_TABLE_DOMESTIC;
        int length = strArr.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            } else if (currentMcc.equals(strArr[i])) {
                isMccDomestic = true;
                break;
            } else {
                i++;
            }
        }
        if (!isOp17IaSupport() || !isMccDomestic) {
            if (!enableOpIA()) {
                log("checkIfDomesticInitialAttachApn: Not OP129 or MCC is not in domestic for OP129");
                return true;
            } else if (this.mRegion == 1) {
                return true;
            } else {
                return false;
            }
        } else if (!getImsEnabled()) {
            return false;
        } else {
            if (this.mRegion == 1) {
                return true;
            }
            return false;
        }
    }

    public boolean enableOpIA() {
        String operatorNumeric = mtkGetOperatorNumeric((IccRecords) this.mIccRecords.get());
        if (TextUtils.isEmpty(operatorNumeric)) {
            return false;
        }
        String simOperator = operatorNumeric.substring(0, 3);
        log("enableOpIA: currentMcc = " + simOperator);
        String[] strArr = MCC_TABLE_DOMESTIC;
        int length = strArr.length;
        for (int i = 0; i < length; i++) {
            if (simOperator.equals(strArr[i])) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void onAttachApnChangedByHandover(boolean isImsHandover) {
        this.mIsImsHandover = isImsHandover;
        log("onAttachApnChangedByHandover: mIsImsHandover = " + this.mIsImsHandover);
        setInitialAttachApn();
    }

    /* access modifiers changed from: private */
    public boolean isOp17IaSupport() {
        return TelephonyManager.getTelephonyProperty(this.mPhone.getPhoneId(), "vendor.gsm.ril.sim.op17", "0").equals("1");
    }

    private boolean isOp129IaSupport() {
        return SystemProperties.get("vendor.gsm.ril.sim.op129").equals("1");
    }

    private boolean isNeedToResumeMd() {
        return SystemProperties.get("vendor.gsm.ril.data.op.suspendmd").equals("1");
    }

    private boolean isOp18Sim() {
        String operator = mtkGetOperatorNumeric((IccRecords) this.mIccRecords.get());
        if (operator == null) {
            return false;
        }
        int i = 0;
        while (true) {
            String[] strArr = this.MCCMNC_OP18;
            if (i >= strArr.length) {
                return false;
            }
            if (operator.startsWith(strArr[i])) {
                return true;
            }
            i++;
        }
    }

    /* access modifiers changed from: private */
    public boolean hasOperatorIaCapability() {
        TelephonyDevController telephonyDevController = this.mTelDevController;
        if (telephonyDevController == null || telephonyDevController.getModem(0) == null || !this.mTelDevController.getModem(0).hasOperatorIaCapability()) {
            return false;
        }
        log("hasOpIaCapability: true");
        return true;
    }

    private void onAllowChanged(boolean allow) {
        log("onAllowChanged: Allow = " + allow);
        this.mAllowConfig = allow;
        if (allow) {
            setupDataOnAllConnectableApns(MtkGsmCdmaPhone.REASON_DATA_ALLOWED, DcTracker.RetryFailures.ALWAYS);
        }
    }

    /* access modifiers changed from: protected */
    public boolean getAllowConfig() {
        if (MtkDcHelper.getInstance().isMultiPsAttachSupport() && !hasModemDeactPdnCapabilityForMultiPS()) {
            return this.mAllowConfig;
        }
        return true;
    }

    public void onVoiceCallStartedEx() {
        boolean z;
        log("onVoiceCallStartedEx");
        this.mInVoiceCall = true;
        MtkDcHelper dcHelper = MtkDcHelper.getInstance();
        if (dcHelper == null) {
            z = false;
        } else {
            z = dcHelper.isDataAllowedForConcurrent(this.mPhone.getPhoneId());
        }
        this.mIsSupportConcurrent = z;
        if (isConnected() && !this.mIsSupportConcurrent) {
            log("onVoiceCallStarted stop polling");
            stopNetStatPoll();
            stopDataStallAlarm();
            this.mPhone.notifyDataConnection();
        }
        notifyVoiceCallEventToDataConnection(this.mInVoiceCall, this.mIsSupportConcurrent);
    }

    public void onDsdaStateChanged() {
        boolean z;
        log("onDsdaStateChanged");
        MtkDcHelper dcHelper = MtkDcHelper.getInstance();
        if (dcHelper == null) {
            z = false;
        } else {
            z = dcHelper.isDataAllowedForConcurrent(this.mPhone.getPhoneId());
        }
        this.mIsSupportConcurrent = z;
        if (isConnected()) {
            if (!this.mIsSupportConcurrent) {
                stopNetStatPoll();
                stopDataStallAlarm();
            } else {
                startNetStatPoll();
                startDataStallAlarm(false);
            }
            this.mPhone.notifyDataConnection();
            notifyVoiceCallEventToDataConnection(this.mInVoiceCall, this.mIsSupportConcurrent);
        }
    }

    /* access modifiers changed from: private */
    public void onWifiStateChanged(boolean enabled) {
        boolean z;
        MtkDcHelper dcHelper = MtkDcHelper.getInstance();
        if (dcHelper == null) {
            z = false;
        } else {
            z = dcHelper.isDataAllowedForConcurrent(this.mPhone.getPhoneId());
        }
        this.mIsSupportConcurrent = z;
        log("onWifiStateChanged, wifi enabled = " + enabled + ", mInVoiceCall = " + this.mInVoiceCall + ", mIsSupportConcurrent = " + this.mIsSupportConcurrent);
        if (this.mInVoiceCall && isConnected()) {
            if (!enabled && !this.mIsSupportConcurrent) {
                log("onWifiStateChanged: wifi disabled and not support concurrent");
                stopNetStatPoll();
                stopDataStallAlarm();
                this.mPhone.notifyDataConnection();
                notifyVoiceCallEventToDataConnection(this.mInVoiceCall, this.mIsSupportConcurrent);
            } else if (enabled && this.mIsSupportConcurrent) {
                log("onWifiStateChanged: wifi enabled and support concurrent");
                startNetStatPoll();
                startDataStallAlarm(false);
                this.mPhone.notifyDataConnection();
                notifyVoiceCallEventToDataConnection(this.mInVoiceCall, this.mIsSupportConcurrent);
            }
        }
    }

    public void onVoiceCallEndedEx() {
        boolean z;
        log("onVoiceCallEndedEx");
        boolean z2 = false;
        this.mInVoiceCall = false;
        boolean prevIsSupportConcurrent = this.mIsSupportConcurrent;
        MtkDcHelper dcHelper = MtkDcHelper.getInstance();
        if (dcHelper == null) {
            z = false;
        } else {
            z = dcHelper.isDataAllowedForConcurrent(this.mPhone.getPhoneId());
        }
        this.mIsSupportConcurrent = z;
        if (isConnected()) {
            if (!prevIsSupportConcurrent || this.mIsSupportConcurrent) {
                startNetStatPoll();
                startDataStallAlarm(false);
                this.mPhone.notifyDataConnection();
            } else {
                resetPollStats();
            }
        }
        if (MtkDcHelper.MTK_SVLTE_SUPPORT) {
            if (dcHelper != null) {
                z2 = dcHelper.isDataSupportConcurrent(this.mPhone.getPhoneId());
            }
            this.mIsSupportConcurrent = z2;
            if (dcHelper != null && !dcHelper.isAllCallingStateIdle()) {
                this.mInVoiceCall = true;
                log("SVLTE denali dual call one end, left one call.");
            }
        }
        setupDataOnAllConnectableApns("2GVoiceCallEnded", DcTracker.RetryFailures.ALWAYS);
        notifyVoiceCallEventToDataConnection(this.mInVoiceCall, this.mIsSupportConcurrent);
    }

    private void notifyVoiceCallEventToDataConnection(boolean bInVoiceCall, boolean bSupportConcurrent) {
        logd("notifyVoiceCallEventToDataConnection: bInVoiceCall = " + bInVoiceCall + ", bSupportConcurrent = " + bSupportConcurrent);
        for (DataConnection dc : this.mDataConnections.values()) {
            ((MtkDataConnection) dc).notifyVoiceCallEvent(bInVoiceCall, bSupportConcurrent);
        }
    }

    private boolean getDomesticRoamingEnabledBySim() {
        if (!this.mCcDomesticRoamingEnabled) {
            return false;
        }
        String[] strArr = this.mCcDomesticRoamingSpecifiedNw;
        if (strArr != null) {
            return ArrayUtils.contains(strArr, TelephonyManager.getDefault().getNetworkOperatorForPhone(this.mPhone.getPhoneId()));
        }
        return true;
    }

    private boolean isImsApnSettingChanged(ArrayList<ApnSetting> prevApnList, ArrayList<ApnSetting> currApnList) {
        String prevImsApn = getImsApnSetting(prevApnList);
        String currImsApn = getImsApnSetting(currApnList);
        if (prevImsApn.isEmpty() || TextUtils.equals(prevImsApn, currImsApn)) {
            return false;
        }
        return true;
    }

    private String getImsApnSetting(ArrayList<ApnSetting> apnSettings) {
        if (apnSettings == null || apnSettings.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Iterator<ApnSetting> it = apnSettings.iterator();
        while (it.hasNext()) {
            ApnSetting apnSetting = it.next();
            if (apnSetting.canHandleType(64)) {
                sb.append(((MtkApnSetting) apnSetting).toStringIgnoreName(true));
            }
        }
        log("getImsApnSetting, apnsToStringIgnoreName: sb = " + sb.toString());
        return sb.toString();
    }

    private boolean checkDomesticDataRoamingEnabled() {
        log("checkDomesticDataRoamingEnabled: getDomesticDataRoamingFromSettings=" + getDomesticDataRoamingEnabledFromSettings() + ", isDomesticRoaming=" + isDomesticRoaming());
        return getDomesticDataRoamingEnabledFromSettings() && isDomesticRoaming();
    }

    private boolean checkInternationalDataRoamingEnabled() {
        log("checkInternationalDataRoamingEnabled: getInternationalDataRoamingFromSettings=" + getInternationalDataRoamingEnabledFromSettings() + ", isIntlRoaming=" + isIntlRoaming());
        return getInternationalDataRoamingEnabledFromSettings() && isIntlRoaming();
    }

    private boolean hasModemDeactPdnCapabilityForMultiPS() {
        if (!this.mHasFetchModemDeactPdnCapabilityForMultiPS) {
            TelephonyDevController telephonyDevController = this.mTelDevController;
            if (telephonyDevController == null || telephonyDevController.getModem(0) == null || !this.mTelDevController.getModem(0).hasModemDeactPdnCapabilityForMultiPS()) {
                this.mModemDeactPdnCapabilityForMultiPS = false;
            } else {
                this.mModemDeactPdnCapabilityForMultiPS = true;
            }
            this.mHasFetchModemDeactPdnCapabilityForMultiPS = true;
            log("hasModemDeactPdnCapabilityForMultiPS: " + this.mModemDeactPdnCapabilityForMultiPS);
        }
        return this.mModemDeactPdnCapabilityForMultiPS;
    }

    private void teardownDataByEmergencyPolicy() {
        CarrierConfigManager configManager = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        PersistableBundle b = null;
        if (configManager != null) {
            b = configManager.getConfigForSubId(this.mPhone.getSubId());
        }
        if (b != null) {
            String[] disConnectApns = b.getStringArray("emergency_bearer_management_policy");
            for (String name : disConnectApns) {
                Iterator it = this.mApnContexts.values().iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    ApnContext apnContext = (ApnContext) it.next();
                    if (!apnContext.isDisconnected()) {
                        ApnSetting apnSetting = apnContext.getApnSetting();
                        log("compare apn: " + apnSetting.getApnName() + " by filter: " + name);
                        if (apnSetting.getApnName().equalsIgnoreCase(name)) {
                            apnContext.setReason("pdnOccupied");
                            cleanUpConnection(apnContext);
                            break;
                        }
                    }
                }
            }
            return;
        }
        loge("Couldn't find CarrierConfigService.");
    }

    private boolean isApnSettingExist(ApnSetting apnSetting) {
        if (!(apnSetting == null || this.mAllApnSettings == null || this.mAllApnSettings.isEmpty())) {
            Iterator it = this.mAllApnSettings.iterator();
            while (it.hasNext()) {
                ApnSetting apn = (ApnSetting) it.next();
                if (TextUtils.equals(((MtkApnSetting) apnSetting).toStringIgnoreName(false), ((MtkApnSetting) apn).toStringIgnoreName(false))) {
                    log("isApnSettingExist: " + apn);
                    return true;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void mtkCopyHandlerThread(HandlerThread t) {
        this.mDcHandlerThread = t;
    }

    /* access modifiers changed from: protected */
    public void mtkUpdateTotalTxRxSum() {
        String strOperatorNumeric = mtkGetOperatorNumeric((IccRecords) this.mIccRecords.get());
        if (strOperatorNumeric != null) {
            int i = 0;
            while (true) {
                String[] strArr = PRIVATE_APN_OPERATOR;
                if (i >= strArr.length) {
                    return;
                }
                if (strOperatorNumeric.startsWith(strArr[i])) {
                    this.mDataStallTxRxSum.updateTotalTxRxSum();
                    return;
                }
                i++;
            }
        }
    }

    /* access modifiers changed from: protected */
    public long mtkModifyInterApnDelay(long delay, ApnContext apnContext) {
        if ("vsim".equals(apnContext.getApnType()) || ("default".equals(apnContext.getApnType()) && "apnChanged".equals(apnContext.getReason()))) {
            return 1000;
        }
        return delay;
    }

    private void addVsimApnTypeToDefaultApnSetting() {
        int i;
        if (ExternalSimManager.isNonDsdaRemoteSimSupport() && this.mAllApnSettings != null) {
            int i2 = 0;
            while (i2 < this.mAllApnSettings.size()) {
                ApnSetting apnSetting = (ApnSetting) this.mAllApnSettings.get(i2);
                if (apnSetting.canHandleType(17)) {
                    i = i2;
                    this.mAllApnSettings.set(i, new MtkApnSetting(apnSetting.getId(), apnSetting.getOperatorNumeric(), apnSetting.getEntryName(), apnSetting.getApnName(), apnSetting.getProxyAddressAsString(), apnSetting.getProxyPort(), apnSetting.getMmsc(), apnSetting.getMmsProxyAddressAsString(), apnSetting.getMmsProxyPort(), apnSetting.getUser(), apnSetting.getPassword(), apnSetting.getAuthType(), apnSetting.getApnTypeBitmask() | 32768, apnSetting.getProtocol(), apnSetting.getRoamingProtocol(), apnSetting.isEnabled(), apnSetting.getNetworkTypeBitmask(), apnSetting.getProfileId(), apnSetting.isPersistent(), apnSetting.getMaxConns(), apnSetting.getWaitTime(), apnSetting.getMaxConnsTime(), apnSetting.getMtu(), apnSetting.getMvnoType(), apnSetting.getMvnoMatchData(), apnSetting.getApnSetId(), apnSetting.getCarrierId(), apnSetting.getSkip464Xlat(), ((MtkApnSetting) apnSetting).inactiveTimer));
                } else {
                    i = i2;
                }
                i2 = i + 1;
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean mtkIsNeedNotify(ApnContext apnContext) {
        if (!TextUtils.equals(apnContext.getApnType(), "default") || apnContext.getState() != DctConstants.State.CONNECTED) {
            return ((MtkApnContext) apnContext).isNeedNotify();
        }
        logd("mtkIsNeedNotify: do not notify state for default apn");
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean mtkIsPermanentFailure(int dcFailCause) {
        String strOperatorNumeric;
        boolean bPermanent = false;
        IDataConnectionExt iDataConnectionExt = this.mDataConnectionExt;
        if (iDataConnectionExt != null) {
            try {
                bPermanent = iDataConnectionExt.isPermanentCause(dcFailCause);
            } catch (Exception e) {
                logd("mDataConnectionExt.isPermanentCause exception");
                e.printStackTrace();
            }
        }
        if (dcFailCause != 3910 || (strOperatorNumeric = mtkGetOperatorNumeric((IccRecords) this.mIccRecords.get())) == null) {
            return bPermanent;
        }
        int i = 0;
        while (true) {
            String[] strArr = KDDI_OPERATOR;
            if (i >= strArr.length) {
                return bPermanent;
            }
            if (strOperatorNumeric.startsWith(strArr[i])) {
                return true;
            }
            i++;
        }
    }

    /* access modifiers changed from: protected */
    public String mtkGetEmergencyApnSelection(String selection) {
        return selection + " and numeric=''";
    }

    private void isDataAllowedForRoamingFeature(DataConnectionReasons dataConnectionReasons) {
        if (isUniqueRoamingFeatureEnabled()) {
            if (dataConnectionReasons.contains(DataConnectionReasons.DataDisallowedReasonType.ROAMING_DISABLED)) {
                dataConnectionReasons.mDataDisallowedReasonSet.remove(DataConnectionReasons.DataDisallowedReasonType.ROAMING_DISABLED);
            }
            if (!isDataRoamingTypeAllowed()) {
                dataConnectionReasons.add(DataConnectionReasons.DataDisallowedReasonType.ROAMING_DISABLED);
            }
        }
    }

    private void onDedecatedBearerActivated(MtkDedicateDataCallResponse dataResponse) {
        log("onDedecatedBearerActivated, dataInfo: " + dataResponse);
        notifyDedicateDataConnection(dataResponse.mCid, DctConstants.State.CONNECTED, dataResponse, 0, MtkDedicateDataCallResponse.REASON_BEARER_ACTIVATION);
    }

    private void onDedecatedBearerModified(MtkDedicateDataCallResponse dataResponse) {
        log("onDedecatedBearerModified, dataInfo: " + dataResponse);
        notifyDedicateDataConnection(dataResponse.mCid, DctConstants.State.CONNECTED, dataResponse, 0, MtkDedicateDataCallResponse.REASON_BEARER_MODIFICATION);
    }

    private void onDedecatedBearerDeactivated(int cid) {
        log("onDedecatedBearerDeactivated, Cid: " + cid);
        notifyDedicateDataConnection(cid, DctConstants.State.IDLE, null, 0, MtkDedicateDataCallResponse.REASON_BEARER_DEACTIVATION);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{android.content.Intent.putExtra(java.lang.String, java.io.Serializable):android.content.Intent}
     arg types: [java.lang.String, com.android.internal.telephony.DctConstants$State]
     candidates:
      ClspMth{android.content.Intent.putExtra(java.lang.String, int):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, int[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, double):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, char):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, boolean[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, byte):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Bundle):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, float):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, long[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, long):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, short):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, double[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, float[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, byte[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, short[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, char[]):android.content.Intent}
      ClspMth{android.content.Intent.putExtra(java.lang.String, java.io.Serializable):android.content.Intent} */
    private void notifyDedicateDataConnection(int ddcId, DctConstants.State state, MtkDedicateDataCallResponse dataInfo, int failCause, String reason) {
        log("notifyDedicateDataConnection ddcId=" + ddcId + ", state=" + state + ", failCause=" + failCause + ", reason=" + reason + ", dataInfo=" + dataInfo);
        Intent intent = new Intent("com.mediatek.intent.action.ACTION_ANY_DEDICATE_DATA_CONNECTION_STATE_CHANGED");
        intent.putExtra("DdcId", ddcId);
        if (dataInfo != null && dataInfo.mCid >= 0) {
            intent.putExtra("linkProperties", dataInfo);
        }
        intent.putExtra("state", (Serializable) state);
        intent.putExtra("cause", failCause);
        intent.putExtra("phone", this.mPhone.getPhoneId());
        this.mPhone.getContext().sendBroadcastAsUser(intent, UserHandle.ALL, "android.permission.READ_PRECISE_PHONE_STATE");
    }

    /* access modifiers changed from: private */
    public void loadCarrierConfig(int subId) {
        CarrierConfigManager configManager = (CarrierConfigManager) this.mPhone.getContext().getSystemService("carrier_config");
        PersistableBundle b = null;
        if (configManager != null) {
            b = configManager.getConfigForSubId(subId);
        }
        if (b != null) {
            this.mCcDomesticRoamingEnabled = b.getBoolean("mtk_domestic_roaming_enabled_only_by_mobile_data_setting");
            this.mCcDomesticRoamingSpecifiedNw = b.getStringArray("mtk_domestic_roaming_enabled_only_by_mobile_data_setting_check_nw_plmn");
            this.mCcIntlRoamingEnabled = b.getBoolean("mtk_intl_roaming_enabled_only_by_roaming_data_setting");
            this.mCcUniqueSettingsForRoaming = b.getBoolean("mtk_unique_settings_for_domestic_and_intl_roaming");
            this.mCcOneSettingForRoaming = b.getBoolean("mtk_one_setting_for_domestic_and_intl_roaming_data");
            this.mIsAddMnoApnsIntoAllApnList = b.getBoolean("mtk_key_add_mnoapns_into_allapnlist");
            StringBuilder sb = new StringBuilder();
            sb.append("loadCarrierConfig: DomesticRoamingEnabled ");
            sb.append(this.mCcDomesticRoamingEnabled);
            sb.append(", SpecifiedNw ");
            sb.append(this.mCcDomesticRoamingSpecifiedNw != null);
            sb.append(", IntlRoamingEnabled ");
            sb.append(this.mCcIntlRoamingEnabled);
            sb.append(", UniqueSettingsForRoaming ");
            sb.append(this.mCcUniqueSettingsForRoaming);
            sb.append(", OneSettingForRoaming ");
            sb.append(this.mCcOneSettingForRoaming);
            sb.append(", IsAddMnoApnsIntoAllApnList ");
            sb.append(this.mIsAddMnoApnsIntoAllApnList);
            log(sb.toString());
            if (this.mCcOneSettingForRoaming && TelephonyManager.getDefault().getSimCount() == 1) {
                onDataRoamingOnOrSettingsChanged(270854);
            }
        }
    }

    private boolean hasMdAutoSetupImsCapability() {
        if (!this.mHasFetchMdAutoSetupImsCapability) {
            TelephonyDevController telephonyDevController = this.mTelDevController;
            if (!(telephonyDevController == null || telephonyDevController.getModem(0) == null || !this.mTelDevController.getModem(0).hasMdAutoSetupImsCapability())) {
                this.mMdAutoSetupImsCapability = true;
            }
            this.mHasFetchMdAutoSetupImsCapability = true;
            logd("hasMdAutoSetupImsCapability: " + this.mMdAutoSetupImsCapability);
        }
        return this.mMdAutoSetupImsCapability;
    }

    /* access modifiers changed from: private */
    public void reloadOpCustomizationFactory() {
        try {
            if (this.mDataConnectionExt != null) {
                this.mDataConnectionExt.stopDataRoamingStrategy();
            }
            this.mTelephonyCustomizationFactory = OpTelephonyCustomizationUtils.getOpFactory(this.mPhone.getContext());
            this.mDataConnectionExt = this.mTelephonyCustomizationFactory.makeDataConnectionExt(this.mPhone.getContext());
            if (this.mTransportType == 1) {
                this.mDataConnectionExt.startDataRoamingStrategy(this.mPhone);
            }
        } catch (Exception e) {
            log("mDataConnectionExt init fail");
            e.printStackTrace();
        }
    }

    private boolean isSimActivated() {
        String gid1 = SystemProperties.get(PROP_RIL_DATA_GID1 + this.mPhone.getPhoneId(), "");
        log("gid1: " + gid1);
        if (GID1_DEFAULT.compareToIgnoreCase(gid1) == 0) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean mtkIsNeedRegisterSettingsObserver(int pSubId, int subId) {
        log("mtkIsNeedRegisterSettingsObserver: pSubId=" + pSubId + ", subId=" + subId);
        return pSubId != subId;
    }

    private ApnSetting encodeInactiveTimer(ApnSetting apn) {
        int inactTimer;
        if (apn == null) {
            loge("encodeInactiveTimer apn is null");
            return null;
        }
        if (apn.getAuthType() > 7 || apn.getAuthType() < -1) {
            loge("encodeInactiveTimer invalid authType: " + apn.getAuthType());
        } else if (apn instanceof MtkApnSetting) {
            int i = 0;
            if (((MtkApnSetting) apn).inactiveTimer < 0) {
                inactTimer = 0;
            } else {
                inactTimer = ((MtkApnSetting) apn).inactiveTimer > 536870911 ? 536870911 : ((MtkApnSetting) apn).inactiveTimer;
            }
            if (apn.getAuthType() != -1) {
                i = apn.getAuthType();
            } else if (!TextUtils.isEmpty(apn.getUser())) {
                i = 3;
            }
            return ApnSetting.makeApnSetting(apn.getId(), apn.getOperatorNumeric(), apn.getEntryName(), apn.getApnName(), apn.getProxyAddressAsString(), apn.getProxyPort(), apn.getMmsc(), apn.getMmsProxyAddressAsString(), apn.getMmsProxyPort(), apn.getUser(), apn.getPassword(), (inactTimer << 3) + i, apn.getApnTypeBitmask(), apn.getProtocol(), apn.getRoamingProtocol(), apn.isEnabled(), apn.getNetworkTypeBitmask(), apn.getProfileId(), apn.isPersistent(), apn.getMaxConns(), apn.getWaitTime(), apn.getMaxConnsTime(), apn.getMtu(), apn.getMvnoType(), apn.getMvnoMatchData(), apn.getApnSetId(), apn.getCarrierId(), apn.getSkip464Xlat());
        }
        return apn;
    }

    public DctConstants.State getState(String apnType) {
        ApnContext apnContext = (ApnContext) this.mApnContexts.get(apnType);
        if (apnContext != null) {
            return apnContext.getState();
        }
        return DctConstants.State.IDLE;
    }

    /* access modifiers changed from: protected */
    public String mtkGetOperatorNumeric(IccRecords r) {
        String operatorNumeric;
        if (this.mPhoneType == 0) {
            if (MtkDcHelper.isCdma4GDualModeCard(this.mPhone.getPhoneId())) {
                this.mPhoneType = 1;
            } else {
                this.mPhoneType = this.mPhone.getPhoneType();
            }
        }
        if (this.mPhoneType == 2) {
            operatorNumeric = SystemProperties.get(PROP_RIL_DATA_CDMA_MCC_MNC + this.mPhone.getPhoneId());
        } else {
            operatorNumeric = SystemProperties.get(PROP_RIL_DATA_GSM_MCC_MNC + this.mPhone.getPhoneId());
        }
        StringBuilder sb = new StringBuilder();
        sb.append("mtkGetOperatorNumeric: operator from IccRecords = ");
        String str = "";
        sb.append(r != null ? r.getOperatorNumeric() : str);
        sb.append(", operator from RIL = ");
        sb.append(operatorNumeric);
        log(sb.toString());
        if (TextUtils.isEmpty(operatorNumeric)) {
            if (r != null) {
                str = r.getOperatorNumeric();
            }
            operatorNumeric = str;
        }
        IDataConnectionExt iDataConnectionExt = this.mDataConnectionExt;
        if (iDataConnectionExt != null) {
            return iDataConnectionExt.getOperatorNumericFromImpi(operatorNumeric, this.mPhone.getPhoneId());
        }
        return operatorNumeric;
    }

    /* JADX WARNING: Removed duplicated region for block: B:52:0x01b0 A[RETURN] */
    private boolean isMvnoMatches(int mvnoType, String mvnoMatchData) {
        String strImsi;
        String strHexSpn;
        String strSpn;
        log("mvnoMatchData=" + mvnoMatchData);
        if (mvnoType == 0) {
            if (this.mPhoneType == 2) {
                strHexSpn = SystemProperties.get(PROP_RIL_DATA_CDMA_SPN + this.mPhone.getPhoneId(), "");
            } else {
                strHexSpn = SystemProperties.get(PROP_RIL_DATA_GSM_SPN + this.mPhone.getPhoneId(), "");
            }
            if (strHexSpn.length() == 0) {
                return false;
            }
            if (this.mPhoneType == 2) {
                strSpn = MtkIccUtilsEx.parseSpnToString(2, IccUtils.hexStringToBytes(strHexSpn));
            } else {
                strSpn = MtkIccUtilsEx.parseSpnToString(1, IccUtils.hexStringToBytes(strHexSpn));
            }
            log("strSpn=" + strSpn);
            return strSpn != null && strSpn.equalsIgnoreCase(mvnoMatchData);
        } else if (mvnoType == 1) {
            if (this.mPhoneType == 2) {
                strImsi = SystemProperties.get(PROP_RIL_DATA_CDMA_IMSI + this.mPhone.getPhoneId(), "");
            } else {
                strImsi = SystemProperties.get(PROP_RIL_DATA_GSM_IMSI + this.mPhone.getPhoneId(), "");
            }
            if (strImsi != null && ApnSettingUtils.imsiMatches(mvnoMatchData, strImsi)) {
                return true;
            }
        } else if (mvnoType == 2) {
            String gid1 = SystemProperties.get(PROP_RIL_DATA_GID1 + this.mPhone.getPhoneId(), "");
            log("gid1=" + gid1);
            int mvno_match_data_length = mvnoMatchData.length();
            if (gid1 != null && gid1.length() >= mvno_match_data_length && gid1.substring(0, mvno_match_data_length).equalsIgnoreCase(mvnoMatchData)) {
                return true;
            }
        } else if (mvnoType == 3) {
            String iccId = SystemProperties.get(PROP_RIL_DATA_ICCID + (this.mPhone.getPhoneId() + 1), "");
            log("iccId=" + iccId);
            if (iccId != null && ApnSettingUtils.iccidMatches(mvnoMatchData, iccId)) {
                return true;
            }
        } else if (mvnoType == 4) {
            String strHexPnn = SystemProperties.get(PROP_RIL_DATA_PNN + this.mPhone.getPhoneId(), "");
            if (strHexPnn.length() == 0) {
                return false;
            }
            String strPnn = MtkIccUtilsEx.parsePnnToString(IccUtils.hexStringToBytes(strHexPnn));
            log("strPnn=" + strPnn);
            if (strPnn != null && strPnn.equalsIgnoreCase(mvnoMatchData)) {
                return true;
            }
        }
    }

    private DataRetryOperator getDataRetryOperator() {
        String operator = mtkGetOperatorNumeric((IccRecords) this.mIccRecords.get());
        if (TextUtils.isEmpty(operator)) {
            operator = TelephonyManager.getDefault().getSimOperatorNumeric(this.mPhone.getSubId());
        }
        if (operator != null) {
            int i = 0;
            while (true) {
                String[] strArr = this.MCCMNC_TELCEL;
                if (i >= strArr.length) {
                    int i2 = 0;
                    while (true) {
                        String[] strArr2 = this.MCCMNC_TELSTRA;
                        if (i2 >= strArr2.length) {
                            int i3 = 0;
                            while (true) {
                                String[] strArr3 = this.MCCMNC_EE;
                                if (i3 >= strArr3.length) {
                                    break;
                                } else if (operator.startsWith(strArr3[i3])) {
                                    return DataRetryOperator.EE;
                                } else {
                                    i3++;
                                }
                            }
                        } else if (operator.startsWith(strArr2[i2])) {
                            return DataRetryOperator.TELSTRA;
                        } else {
                            i2++;
                        }
                    }
                } else if (operator.startsWith(strArr[i])) {
                    return DataRetryOperator.TELCEL;
                } else {
                    i++;
                }
            }
        }
        return DataRetryOperator.UNKNOWN;
    }

    private boolean isDataRetryRestrictEnabled() {
        return this.mDataRetryOperator == DataRetryOperator.TELCEL || this.mDataRetryOperator == DataRetryOperator.TELSTRA || this.mDataRetryOperator == DataRetryOperator.EE;
    }

    private void onNetworkRejectReceived(AsyncResult ar) {
        if (ar.exception != null || ar.result == null) {
            loge("onNetworkRejectReceived exception");
            return;
        }
        int[] ints = (int[]) ar.result;
        if (ints.length < 3) {
            loge("onNetworkRejectReceived urc format error");
            return;
        }
        int emm_cause = ints[0];
        int esm_cause = ints[1];
        int event = ints[2];
        log("onNetworkRejectReceived emm_cause:" + emm_cause + ", esm_cause:" + esm_cause + ", event_type:" + event);
        Intent intent = new Intent("com.mediatek.intent.action.ACTION_NETWORK_REJECT_CAUSE");
        SubscriptionManager.putPhoneIdAndSubIdExtra(intent, this.mPhone.getPhoneId());
        intent.addFlags(536870912);
        intent.putExtra("emmCause", emm_cause);
        intent.putExtra("esmCause", esm_cause);
        intent.putExtra("rejectEventType", event);
        this.mPhone.getContext().sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    private ApnSetting makeApnSetting(Cursor cursor) {
        int inactiveTimer = 0;
        try {
            inactiveTimer = cursor.getInt(cursor.getColumnIndexOrThrow("inactive_timer"));
            if (inactiveTimer != 0) {
                logd("makeApnSetting: inactive_timer=" + inactiveTimer);
            }
        } catch (IllegalArgumentException e) {
        }
        int mtu = 0;
        try {
            mtu = cursor.getInt(cursor.getColumnIndexOrThrow("mtu"));
            if (mtu != 0) {
                logd("makeApnSetting: mtu=" + mtu);
            } else {
                mtu = getDefaultMtuConfig(this.mPhone.getContext());
            }
        } catch (IllegalArgumentException e2) {
        }
        return MtkApnSetting.makeApnSetting(cursor, mtu, inactiveTimer);
    }

    /* access modifiers changed from: protected */
    public void mtkSyncApnContextDisableState(ApnContext apnContext, int releaseType) {
        ApnSetting apnSetting;
        if (apnContext != null && apnContext.isReady()) {
            if (apnContext.getState() != DctConstants.State.CONNECTED) {
                log("Apn type '" + apnContext.getApnType() + "' is not connected, we need to sync the disable state with RIL");
                this.mDataServiceManager.deactivateDataCall(getApnProfileID(apnContext.getApnType()) * -1, 1, (Message) null);
            } else if (!"dun".equals(apnContext.getApnType()) && releaseType == 1 && (apnSetting = apnContext.getApnSetting()) != null && apnSetting.canHandleType(17)) {
                log("This apn context has INTERNET capability and CS will not tear it down");
                apnContext.setEnabled(false);
                cleanUpConnectionInternal(true, releaseType, apnContext);
            }
        }
    }

    private void setIsPcoAllowedDefault(boolean allowed) {
        IDataConnectionExt iDataConnectionExt = this.mDataConnectionExt;
        if (iDataConnectionExt != null) {
            iDataConnectionExt.setIsPcoAllowedDefault(allowed);
        }
    }

    private boolean getIsPcoAllowedDefault() {
        IDataConnectionExt iDataConnectionExt = this.mDataConnectionExt;
        if (iDataConnectionExt != null) {
            return iDataConnectionExt.getIsPcoAllowedDefault();
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void mtkHandlePcoByOp(ApnContext apnContext, PcoData pcoData) {
        IDataConnectionExt iDataConnectionExt = this.mDataConnectionExt;
        if (iDataConnectionExt != null && iDataConnectionExt.getPcoActionByApnType(apnContext, pcoData) == 1) {
            log("mtkHandlePcoByOp action1: teardown default apn");
            cleanUpConnectionInternal(true, 2, apnContext);
        }
    }

    public boolean tearDownPdnByType(String type) {
        Message msg = obtainMessage(270863);
        msg.arg1 = ApnSetting.getApnTypesBitmaskFromString(type);
        log("tearDownPdnByType: sendMessage: type=" + msg.arg1);
        sendMessage(msg);
        return true;
    }

    public boolean setupPdnByType(String type) {
        log("setupPdnByType: sendMessage: type=" + type);
        sendMessage(obtainMessage(270339, this.mApnContexts.get(type)));
        return true;
    }

    private void onTearDownPdnByApnId(int apnId) {
        cleanUpConnectionInternal(true, 2, (ApnContext) this.mApnContextsByType.get(apnId));
    }

    private void setSscMode() {
        String mode = Integer.toString(3);
        log("setSscMode: " + mode);
        this.mPhone.mCi.setVendorSetting(15, mode, null);
    }

    public void trySetupDataOnEvent(int event, int cid, int lifetime) {
        Message msg = obtainMessage(event);
        msg.arg1 = cid;
        msg.arg2 = lifetime;
        sendMessage(msg);
    }

    private void onDataSetupSscMode3(int cid, int lifetime) {
        ApnContext apnContext = null;
        Iterator it = this.mApnContexts.values().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            ApnContext apnCtx = (ApnContext) it.next();
            if (apnCtx.getDataConnection() != null) {
                if (apnCtx.getDataConnection().getCid() == cid) {
                    apnContext = apnCtx;
                    log("onDataSetupSscMode3: found apnContext=" + apnContext);
                    break;
                }
                loge("onDataSetupSscMode3: cid does not match! (APN type=" + apnCtx.getApnType() + ", cid=" + apnCtx.getDataConnection().getCid() + ")");
            }
        }
        if (apnContext == null) {
            loge("onDataSetupSscMode3: couldn't find corresponding apnContext");
            return;
        }
        DataConnection dc = apnContext.getDataConnection();
        ((MtkDataConnection) dc).updateNetworkAgentSscMode3(lifetime, 49);
        apnContext.setReason(MtkGsmCdmaPhone.REASON_DATA_SETUP_SSC_MODE3);
        ((MtkApnContext) apnContext).setDataConnectionSscMode3(dc);
        setupDataOnAllConnectableApns(MtkGsmCdmaPhone.REASON_DATA_SETUP_SSC_MODE3, DcTracker.RetryFailures.ALWAYS);
    }

    private boolean onSkipDisconnectDone(ApnContext apnContext) {
        if (apnContext == null) {
            return false;
        }
        DataConnection dc = apnContext.getDataConnection();
        DataConnection dc3 = ((MtkApnContext) apnContext).getDataConnectionSscMode3();
        boolean skipDisconnect = true;
        if (dc == null || dc3 == null) {
            skipDisconnect = false;
        } else if (dc.isInactive() && !dc3.isInactive()) {
            apnContext.setDataConnection(dc3);
            ((MtkDataConnection) dc3).updateNetworkAgentSscMode3(0, 50);
            ((MtkApnContext) apnContext).setDataConnectionSscMode3(null);
        } else if (dc.isInactive() || !dc3.isInactive()) {
            skipDisconnect = false;
        } else {
            ((MtkApnContext) apnContext).setDataConnectionSscMode3(null);
        }
        if (MtkGsmCdmaPhone.REASON_DATA_SETUP_SSC_MODE3.equals(apnContext.getReason())) {
            apnContext.setReason("connected");
        }
        if (skipDisconnect) {
            log("onDisconnectDone: skip due to SSC mode3, dc=(" + dc + "), dc3=(" + dc3 + ")");
        }
        return skipDisconnect;
    }

    /* access modifiers changed from: protected */
    public void mtkTearDown(ApnContext apnContext, Message msg) {
        if (((MtkApnContext) apnContext).getDataConnectionSscMode3() != null) {
            log("cleanUpConnectionInternal: tearing down SSC mode3 PDU, apnContext=" + apnContext);
            ((MtkApnContext) apnContext).getDataConnectionSscMode3().tearDown(apnContext, MtkGsmCdmaPhone.REASON_DATA_SETUP_SSC_MODE3, msg);
        }
    }

    /* access modifiers changed from: protected */
    public boolean mtkSkipCheckForCompatibleConnectedApnContext(ApnContext apnContext) {
        if (apnContext == null || !MtkGsmCdmaPhone.REASON_DATA_SETUP_SSC_MODE3.equals(apnContext.getReason()) || apnContext.getState() != DctConstants.State.CONNECTED) {
            return false;
        }
        log("checkForCompatibleConnectedApnContext: skip check for SSC mode3 PDU, apnContext=" + apnContext);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean mtkCanHandleOnDataSetupComplete(ApnContext apnContext, boolean success, int cause, int requestType) {
        if (success || !MtkGsmCdmaPhone.REASON_DATA_SETUP_SSC_MODE3.equals(apnContext.getReason())) {
            return false;
        }
        ApnSetting apn = apnContext.getApnSetting();
        log("onDataSetupComplete: SSC mode3 PDU, error apn=" + apn.getApnName() + ", cause=" + cause + ", requestType=" + requestTypeToString(requestType));
        DataConnection dc3 = ((MtkApnContext) apnContext).getDataConnectionSscMode3();
        apnContext.setDataConnection(dc3);
        ((MtkApnContext) apnContext).setDataConnectionSscMode3(null);
        if (dc3 == null) {
            log("onDataSetupComplete: SSC mode3 PDU, no connection to original DC, it's wired");
            return true;
        }
        ((MtkDataConnection) dc3).updateNetworkAgentSscMode3(0, 50);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean mtkIsInUse(ApnContext apnContext, DataConnection dc) {
        if (((MtkApnContext) apnContext).getDataConnectionSscMode3() != dc) {
            return false;
        }
        log("findFreeDataConnection: APN type " + apnContext.getApnType() + ", dc=(" + dc + ") is inuse for SSC mode3 PDU");
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean mtkIsAddRequestNetworkCompleteMsg(int apnType, int requestType) {
        if (requestType != 2 || getDataRat() != 0) {
            return false;
        }
        log("Remember the handover apn type when data rat is unknown");
        this.mHandoverApnType = apnType;
        return true;
    }

    public void notifyMtkServiceStateChanged(MtkServiceState ss) {
        if (!this.mIsNotifyDataAttached && ss.getDataRegState() == 0 && !ss.getDataRoaming()) {
            log("notifyMtkServiceStateChanged: send EVENT_DATA_CONNECTION_ATTACHED");
            this.mIsNotifyDataAttached = true;
            this.mTurboSS = ss;
            sendMessage(obtainMessage(270352));
        }
    }

    public MtkServiceState getTurboSS() {
        return this.mTurboSS;
    }

    /* access modifiers changed from: protected */
    public void mtkFakeDataConnection(ApnContext apnContext) {
        log("mtkFakeDataConnection: apn type = " + apnContext.getApnType());
        if (TextUtils.equals(apnContext.getApnType(), "mms")) {
            DataConnection.makeDataConnection(this.mPhone, 0, this, this.mDataServiceManager, (DcTesterFailBringUpAll) null, this.mDcc).fakeNetworkAgent(apnContext);
        }
    }

    public String getImsi() {
        if (this.mIsRecordsOverride) {
            return null;
        }
        if (this.mPhoneType == 2) {
            return this.mImsiCdma;
        }
        return this.mImsiGsm;
    }

    private void getImsiFromRil() {
        if (this.mTransportType == 2) {
            log("skip getImsiFromRil() for DCT-I-x");
            return;
        }
        int i = this.mPhoneType;
        if (i == 2) {
            this.mPhone.mCi.getIMSIForApp("A0000000000C2K", obtainMessage(270866, 2, 0));
        } else if (i == 1) {
            this.mPhone.mCi.getIMSIForApp("A0000000000GSM", obtainMessage(270866, 1, 0));
        } else {
            this.mPhone.mCi.getIMSIForApp("A0000000000GSM", obtainMessage(270866, 1, 0));
            if ("1".equals(SystemProperties.get("ro.vendor.mtk_c2k_support", ""))) {
                this.mPhone.mCi.getIMSIForApp("A0000000000C2K", obtainMessage(270866, 2, 0));
            }
        }
    }

    public boolean isDataServiceBound() {
        return this.mDataServiceBound;
    }

    /* access modifiers changed from: protected */
    public boolean mtkSkipDataStallAlarm() {
        boolean isTestSim = false;
        int phoneId = this.mPhone.getPhoneId();
        MtkDcHelper dcHelper = MtkDcHelper.getInstance();
        if (SubscriptionManager.isValidPhoneId(phoneId) && dcHelper != null && dcHelper.isTestIccCard(phoneId)) {
            isTestSim = true;
        }
        if (isTestSim) {
            if (SystemProperties.get(SKIP_DATA_STALL_ALARM).equals("0")) {
                return false;
            }
            return true;
        } else if (SystemProperties.get(SKIP_DATA_STALL_ALARM).equals("1")) {
            return true;
        } else {
            return false;
        }
    }

    private boolean setRoamingDataWithRoamingType(int roamingType) {
        if (roamingType == 2) {
            return setDataRoamingEnabledByDefault(true);
        }
        if (roamingType == 3 || roamingType == 0 || roamingType == 1) {
            return setDataRoamingEnabledByDefault(false);
        }
        return false;
    }

    private boolean setDataRoamingEnabledByDefault(boolean enabled) {
        if (TelephonyManager.getDefault().getSimCount() != 1 || !this.mCcOneSettingForRoaming || isDataRoamingFromUserAction()) {
            return false;
        }
        boolean oldEnabled = getDataRoamingEnabled();
        log("setDataRoamingEnabledByDefault: oldEnabled = " + oldEnabled + " enabled = " + enabled);
        if (oldEnabled == enabled) {
            return false;
        }
        this.mDataEnabledSettings.setDataRoamingEnabled(enabled);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean mtkIsUseCarrierRoamingData() {
        return !getDataRoamingEnabled();
    }

    /* access modifiers changed from: protected */
    public boolean mtkIsSetFalseForUserAction() {
        return this.mCcOneSettingForRoaming;
    }

    private boolean isUniqueRoamingFeatureEnabled() {
        return this.mCcUniqueSettingsForRoaming && "OP20".equals(SystemProperties.get(DataSubConstants.PROPERTY_OPERATOR_OPTR, ""));
    }

    private void updateMobileDataUsage(AsyncResult ar) {
        int[] ints = (int[]) ar.result;
        int rxPkts = 0;
        int txBytes = ints.length > 0 ? ints[0] : 0;
        int txPkts = ints.length > 1 ? ints[1] : 0;
        int rxBytes = ints.length > 2 ? ints[2] : 0;
        if (ints.length > 3) {
            rxPkts = ints[3];
        }
        long currentTime = SystemClock.elapsedRealtime();
        boolean dataRoaming = this.mPhone.getServiceState().getDataRoaming();
        LinkProperties linkProperties = getLinkProperties("default");
        String ifacename = linkProperties == null ? "" : linkProperties.getInterfaceName();
        NetworkStats mobileDataUsage = new NetworkStats(currentTime, 1);
        mobileDataUsage.combineAllValues(this.mMobileDataUsage);
        mobileDataUsage.combineValues(new NetworkStats.Entry(ifacename, -10, 0, 0, 1, dataRoaming ? 1 : 0, 1, (long) rxBytes, (long) rxPkts, (long) txBytes, (long) txPkts, 0));
        log("updateMobileDataUsage ifacename:" + ifacename + ", txBytes:" + txBytes + ", txPkts:" + txPkts + ", rxBytes:" + rxBytes + ", rxPkts:" + rxPkts);
        this.mMobileDataUsage = mobileDataUsage;
        this.mPhone.getContext().sendBroadcastAsUser(new Intent("com.mediatek.intent.action.ACTION_BACKGROUND_MOBILE_DATA_USAGE"), UserHandle.ALL);
    }

    public NetworkStats getMobileDataUsage() {
        return this.mMobileDataUsage;
    }

    public void setMobileDataUsageSum(long txBytes, long txPkts, long rxBytes, long rxPkts) {
        long currentTime = SystemClock.elapsedRealtime();
        boolean dataRoaming = this.mPhone.getServiceState().getDataRoaming();
        LinkProperties linkProperties = getLinkProperties("default");
        String ifacename = linkProperties == null ? "" : linkProperties.getInterfaceName();
        NetworkStats mobileDataUsage = new NetworkStats(currentTime, 1);
        mobileDataUsage.combineAllValues(this.mMobileDataUsage);
        mobileDataUsage.combineValues(new NetworkStats.Entry(ifacename, -10, 0, 0, 1, dataRoaming ? 1 : 0, 1, rxBytes, rxPkts, txBytes, txPkts, 0));
        this.mMobileDataUsage = mobileDataUsage;
    }

    /* access modifiers changed from: protected */
    public boolean mtkIsApnCanSupportNetworkType(ApnSetting apn, int radioTech) {
        boolean canHandle = true;
        int networkType = ServiceState.rilRadioTechnologyToNetworkType(radioTech);
        if (apn.getApnTypeBitmask() == 512 && networkType == 18) {
            if (apn.getNetworkTypeBitmask() == 0) {
                canHandle = false;
            }
            logi("[mtkIsApnCanSupportNetworkType] return:" + canHandle);
        }
        return canHandle;
    }

    /* access modifiers changed from: protected */
    public boolean isSamePaidApn(ApnSetting first, ApnSetting second) {
        if (first == null || second == null) {
            log("apn is null");
            return false;
        } else if (!(first instanceof MtkApnSetting) || !(second instanceof MtkApnSetting)) {
            log("apn is not MtkApnSetting");
            return false;
        } else {
            String firstOppoPaidSelect = ((MtkApnSetting) first).oppoPaidSelect;
            String secondOppoPaidSelect = ((MtkApnSetting) second).oppoPaidSelect;
            if (firstOppoPaidSelect != null && !TextUtils.isEmpty(firstOppoPaidSelect) && secondOppoPaidSelect != null && !TextUtils.isEmpty(secondOppoPaidSelect)) {
                log("first.oppoPaidSelect:" + firstOppoPaidSelect + " second.oppoPaidSelect:" + secondOppoPaidSelect);
                if (firstOppoPaidSelect.equals(secondOppoPaidSelect)) {
                    return true;
                }
            }
            return false;
        }
    }
}
