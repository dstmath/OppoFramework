package com.android.internal.telephony;

import android.annotation.UnsupportedAppUsage;
import android.app.BroadcastOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.LinkProperties;
import android.net.NetworkCapabilities;
import android.net.NetworkStats;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.Registrant;
import android.os.RegistrantList;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.CarrierRestrictionRules;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.ClientRequestStats;
import android.telephony.ImsiEncryptionInfo;
import android.telephony.PhysicalChannelConfig;
import android.telephony.RadioAccessFamily;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.data.ApnSetting;
import android.text.TextUtils;
import android.util.LocalLog;
import android.util.SparseArray;
import com.android.ims.ImsCall;
import com.android.ims.ImsManager;
import com.android.internal.telephony.Call;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneInternalInterface;
import com.android.internal.telephony.dataconnection.DataConnectionReasons;
import com.android.internal.telephony.dataconnection.DataEnabledSettings;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.dataconnection.KeepaliveStatus;
import com.android.internal.telephony.dataconnection.TransportManager;
import com.android.internal.telephony.emergency.EmergencyNumberTracker;
import com.android.internal.telephony.imsphone.ImsPhoneCall;
import com.android.internal.telephony.test.SimulatedRadioControl;
import com.android.internal.telephony.uicc.IccCardApplicationStatus;
import com.android.internal.telephony.uicc.IccFileHandler;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.IsimRecords;
import com.android.internal.telephony.uicc.UiccCard;
import com.android.internal.telephony.uicc.UiccCardApplication;
import com.android.internal.telephony.uicc.UiccController;
import com.android.internal.telephony.uicc.UsimServiceTable;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public abstract class Phone extends AbstractPhone implements PhoneInternalInterface {
    private static final int ALREADY_IN_AUTO_SELECTION = 1;
    private static final String CDMA_NON_ROAMING_LIST_OVERRIDE_PREFIX = "cdma_non_roaming_list_";
    private static final String CDMA_ROAMING_LIST_OVERRIDE_PREFIX = "cdma_roaming_list_";
    public static final String CF_ID = "cf_id_key";
    public static final String CF_STATUS = "cf_status_key";
    public static final String CLIR_KEY = "clir_key";
    public static final String CS_FALLBACK = "cs_fallback";
    public static final String DATA_DISABLED_ON_BOOT_KEY = "disabled_on_boot_key";
    public static final String DATA_ROAMING_IS_USER_SETTING_KEY = "data_roaming_is_user_setting_key";
    private static final int DEFAULT_REPORT_INTERVAL_MS = 200;
    private static final String DNS_SERVER_CHECK_DISABLED_KEY = "dns_server_check_disabled_key";
    private static final int EMERGENCY_SMS_NO_TIME_RECORDED = -1;
    private static final int EMERGENCY_SMS_TIMER_MAX_MS = 300000;
    private static final int EVENT_ALL_DATA_DISCONNECTED = 52;
    protected static final int EVENT_CALL_RING = 14;
    private static final int EVENT_CALL_RING_CONTINUE = 15;
    protected static final int EVENT_CARRIER_CONFIG_CHANGED = 43;
    protected static final int EVENT_CDMA_SUBSCRIPTION_SOURCE_CHANGED = 27;
    private static final int EVENT_CHECK_FOR_NETWORK_AUTOMATIC = 38;
    private static final int EVENT_CONFIG_LCE = 37;
    protected static final int EVENT_DEVICE_PROVISIONED_CHANGE = 49;
    protected static final int EVENT_DEVICE_PROVISIONING_DATA_SETTING_CHANGE = 50;
    protected static final int EVENT_EMERGENCY_CALLBACK_MODE_ENTER = 25;
    protected static final int EVENT_EXIT_EMERGENCY_CALLBACK_RESPONSE = 26;
    protected static final int EVENT_GET_AVAILABLE_NETWORKS_DONE = 51;
    protected static final int EVENT_GET_BASEBAND_VERSION_DONE = 6;
    protected static final int EVENT_GET_CALL_FORWARD_DONE = 13;
    protected static final int EVENT_GET_DEVICE_IDENTITY_DONE = 21;
    protected static final int EVENT_GET_IMEISV_DONE = 10;
    protected static final int EVENT_GET_IMEI_DONE = 9;
    protected static final int EVENT_GET_RADIO_CAPABILITY = 35;
    private static final int EVENT_GET_SIM_STATUS_DONE = 11;
    private static final int EVENT_ICC_CHANGED = 30;
    protected static final int EVENT_ICC_RECORD_EVENTS = 29;
    private static final int EVENT_INITIATE_SILENT_REDIAL = 32;
    protected static final int EVENT_LAST = 52;
    private static final int EVENT_MMI_DONE = 4;
    protected static final int EVENT_MODEM_RESET = 45;
    protected static final int EVENT_NV_READY = 23;
    protected static final int EVENT_RADIO_AVAILABLE = 1;
    private static final int EVENT_RADIO_NOT_AVAILABLE = 33;
    protected static final int EVENT_RADIO_OFF_OR_NOT_AVAILABLE = 8;
    protected static final int EVENT_RADIO_ON = 5;
    protected static final int EVENT_RADIO_STATE_CHANGED = 47;
    protected static final int EVENT_REGISTERED_TO_NETWORK = 19;
    protected static final int EVENT_REQUEST_VOICE_RADIO_TECH_DONE = 40;
    protected static final int EVENT_RIL_CONNECTED = 41;
    protected static final int EVENT_RUIM_RECORDS_LOADED = 22;
    protected static final int EVENT_SET_CALL_FORWARD_DONE = 12;
    protected static final int EVENT_SET_CARRIER_DATA_ENABLED = 48;
    protected static final int EVENT_SET_CLIR_COMPLETE = 18;
    private static final int EVENT_SET_ENHANCED_VP = 24;
    protected static final int EVENT_SET_NETWORK_AUTOMATIC = 28;
    protected static final int EVENT_SET_NETWORK_AUTOMATIC_COMPLETE = 17;
    protected static final int EVENT_SET_NETWORK_MANUAL_COMPLETE = 16;
    protected static final int EVENT_SET_ROAMING_PREFERENCE_DONE = 44;
    protected static final int EVENT_SET_VM_NUMBER_DONE = 20;
    protected static final int EVENT_SIM_RECORDS_LOADED = 3;
    private static final int EVENT_SRVCC_STATE_CHANGED = 31;
    protected static final int EVENT_SS = 36;
    protected static final int EVENT_SSN = 2;
    private static final int EVENT_UNSOL_OEM_HOOK_RAW = 34;
    protected static final int EVENT_UPDATE_PHONE_OBJECT = 42;
    protected static final int EVENT_USSD = 7;
    protected static final int EVENT_VOICE_RADIO_TECH_CHANGED = 39;
    protected static final int EVENT_VRS_OR_RAT_CHANGED = 46;
    public static final String EXTRA_KEY_ALERT_MESSAGE = "alertMessage";
    public static final String EXTRA_KEY_ALERT_SHOW = "alertShow";
    public static final String EXTRA_KEY_ALERT_TITLE = "alertTitle";
    public static final String EXTRA_KEY_NOTIFICATION_MESSAGE = "notificationMessage";
    private static final String GSM_NON_ROAMING_LIST_OVERRIDE_PREFIX = "gsm_non_roaming_list_";
    private static final String GSM_ROAMING_LIST_OVERRIDE_PREFIX = "gsm_roaming_list_";
    private static final boolean LCE_PULL_MODE = true;
    private static final String LOG_TAG = "Phone";
    public static final String NETWORK_SELECTION_KEY = "network_selection_key";
    public static final String NETWORK_SELECTION_NAME_KEY = "network_selection_name_key";
    public static final String NETWORK_SELECTION_SHORT_KEY = "network_selection_short_key";
    private static final String VM_COUNT = "vm_count_key";
    private static final String VM_ID = "vm_id_key";
    protected static final Object lockForRadioTechnologyChange = new Object();
    protected final int USSD_MAX_QUEUE;
    private final String mActionAttached;
    private final String mActionDetached;
    private final RegistrantList mAllDataDisconnectedRegistrants;
    private final AppSmsManager mAppSmsManager;
    private int mCallRingContinueToken;
    private int mCallRingDelay;
    protected CarrierActionAgent mCarrierActionAgent;
    protected CarrierResolver mCarrierResolver;
    protected CarrierSignalAgent mCarrierSignalAgent;
    private final RegistrantList mCellInfoRegistrants;
    @UnsupportedAppUsage
    public CommandsInterface mCi;
    @UnsupportedAppUsage
    protected final Context mContext;
    protected DataEnabledSettings mDataEnabledSettings;
    protected final SparseArray<DcTracker> mDcTrackers;
    protected DeviceStateMonitor mDeviceStateMonitor;
    protected final RegistrantList mDisconnectRegistrants;
    private boolean mDnsCheckDisabled;
    private boolean mDoesRilSendMultipleCallRing;
    protected final RegistrantList mEmergencyCallToggledRegistrants;
    private final RegistrantList mHandoverRegistrants;
    @UnsupportedAppUsage
    protected final AtomicReference<IccRecords> mIccRecords;
    private BroadcastReceiver mImsIntentReceiver;
    @UnsupportedAppUsage
    protected Phone mImsPhone;
    protected boolean mImsServiceReady;
    private final RegistrantList mIncomingRingRegistrants;
    protected boolean mIsPhoneInEcmState;
    protected boolean mIsVideoCapable;
    private boolean mIsVoiceCapable;
    private int mLceStatus;
    protected final LocalLog mLocalLog;
    private Looper mLooper;
    protected final RegistrantList mMmiCompleteRegistrants;
    @UnsupportedAppUsage
    protected final RegistrantList mMmiRegistrants;
    private String mName;
    private final RegistrantList mNewRingingConnectionRegistrants;
    @UnsupportedAppUsage
    protected PhoneNotifier mNotifier;
    @UnsupportedAppUsage
    protected int mPhoneId;
    protected Registrant mPostDialHandler;
    private final RegistrantList mPreciseCallStateRegistrants;
    private final AtomicReference<RadioCapability> mRadioCapability;
    protected final RegistrantList mRadioOffOrNotAvailableRegistrants;
    private final RegistrantList mServiceStateRegistrants;
    private SimActivationTracker mSimActivationTracker;
    protected final RegistrantList mSimRecordsLoadedRegistrants;
    protected SimulatedRadioControl mSimulatedRadioControl;
    @UnsupportedAppUsage
    public SmsStorageMonitor mSmsStorageMonitor;
    public SmsUsageMonitor mSmsUsageMonitor;
    protected final RegistrantList mSuppServiceFailedRegistrants;
    protected TelephonyComponentFactory mTelephonyComponentFactory;
    protected TelephonyTester mTelephonyTester;
    private volatile long mTimeLastEmergencySmsSentMs;
    protected TransportManager mTransportManager;
    @UnsupportedAppUsage
    protected AtomicReference<UiccCardApplication> mUiccApplication;
    @UnsupportedAppUsage
    protected UiccController mUiccController;
    private boolean mUnitTestMode;
    protected final RegistrantList mUnknownConnectionRegistrants;
    private final RegistrantList mVideoCapabilityChangedRegistrants;
    protected int mVmCount;

    public static class NetworkSelectMessage {
        public Message message;
        public String operatorAlphaLong;
        public String operatorAlphaShort;
        public String operatorNumeric;
    }

    @UnsupportedAppUsage
    public abstract int getPhoneType();

    @UnsupportedAppUsage
    public abstract PhoneConstants.State getState();

    /* access modifiers changed from: protected */
    public abstract void onUpdateIccAvailability();

    public abstract void sendEmergencyCallStateChange(boolean z);

    public abstract void setBroadcastEmergencyCallStateChanges(boolean z);

    /* access modifiers changed from: protected */
    public void handleExitEmergencyCallbackMode() {
    }

    public IccRecords getIccRecords() {
        return this.mIccRecords.get();
    }

    @UnsupportedAppUsage
    public String getPhoneName() {
        return this.mName;
    }

    /* access modifiers changed from: protected */
    public void setPhoneName(String name) {
        this.mName = name;
    }

    @UnsupportedAppUsage
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
            TelephonyManager.setTelephonyProperty(this.mPhoneId, property, value);
        }
    }

    public void setGlobalSystemProperty(String property, String value) {
        if (!getUnitTestMode()) {
            TelephonyManager.setTelephonyProperty(property, value);
        }
    }

    @UnsupportedAppUsage
    public String getSystemProperty(String property, String defValue) {
        if (getUnitTestMode()) {
            return null;
        }
        return SystemProperties.get(property, defValue);
    }

    protected Phone(String name, PhoneNotifier notifier, Context context, CommandsInterface ci, boolean unitTestMode) {
        this(name, notifier, context, ci, unitTestMode, KeepaliveStatus.INVALID_HANDLE, TelephonyComponentFactory.getInstance());
    }

    protected Phone(String name, PhoneNotifier notifier, Context context, CommandsInterface ci, boolean unitTestMode, int phoneId, TelephonyComponentFactory telephonyComponentFactory) {
        this.USSD_MAX_QUEUE = 10;
        this.mImsIntentReceiver = new BroadcastReceiver() {
            /* class com.android.internal.telephony.Phone.AnonymousClass1 */

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
                    if (intent.getAction().equals("com.android.ims.IMS_SERVICE_UP")) {
                        Phone.this.mImsServiceReady = true;
                        Phone.this.updateImsPhone();
                        ImsManager.getInstance(Phone.this.mContext, Phone.this.mPhoneId).updateImsServiceConfig(false);
                    } else if (intent.getAction().equals("com.android.ims.IMS_SERVICE_DOWN")) {
                        Phone.this.mImsServiceReady = false;
                        Phone.this.updateImsPhone();
                    }
                }
            }
        };
        this.mVmCount = 0;
        this.mDcTrackers = new SparseArray<>();
        this.mIsVoiceCapable = true;
        this.mIsPhoneInEcmState = false;
        this.mTimeLastEmergencySmsSentMs = -1;
        this.mIsVideoCapable = false;
        this.mUiccController = null;
        this.mIccRecords = new AtomicReference<>();
        this.mUiccApplication = new AtomicReference<>();
        this.mImsServiceReady = false;
        this.mImsPhone = null;
        this.mRadioCapability = new AtomicReference<>();
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
        this.mAllDataDisconnectedRegistrants = new RegistrantList();
        this.mCellInfoRegistrants = new RegistrantList();
        this.mPhoneId = phoneId;
        this.mName = name;
        this.mNotifier = notifier;
        this.mContext = context;
        this.mLooper = Looper.myLooper();
        this.mCi = ci;
        this.mActionDetached = getClass().getPackage().getName() + ".action_detached";
        this.mActionAttached = getClass().getPackage().getName() + ".action_attached";
        this.mAppSmsManager = telephonyComponentFactory.inject(AppSmsManager.class.getName()).makeAppSmsManager(context);
        this.mLocalLog = new LocalLog(64);
        if (Build.IS_DEBUGGABLE) {
            this.mTelephonyTester = new TelephonyTester(this);
        }
        setUnitTestMode(unitTestMode);
        this.mDnsCheckDisabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(DNS_SERVER_CHECK_DISABLED_KEY, false);
        this.mCi.setOnCallRing(this, 14, null);
        this.mIsVoiceCapable = this.mContext.getResources().getBoolean(17891571);
        this.mDoesRilSendMultipleCallRing = SystemProperties.getBoolean("ro.telephony.call_ring.multiple", true);
        Rlog.d(LOG_TAG, "mDoesRilSendMultipleCallRing=" + this.mDoesRilSendMultipleCallRing);
        this.mCallRingDelay = SystemProperties.getInt("ro.telephony.call_ring.delay", 3000);
        Rlog.d(LOG_TAG, "mCallRingDelay=" + this.mCallRingDelay);
        if (getPhoneType() != 5) {
            Locale carrierLocale = getLocaleFromCarrierProperties(this.mContext);
            if (carrierLocale != null && !TextUtils.isEmpty(carrierLocale.getCountry())) {
                String country = carrierLocale.getCountry();
                try {
                    Settings.Global.getInt(this.mContext.getContentResolver(), "wifi_country_code");
                } catch (Settings.SettingNotFoundException e) {
                    ((WifiManager) this.mContext.getSystemService("wifi")).setCountryCode(country);
                }
            }
            this.mTelephonyComponentFactory = telephonyComponentFactory;
            this.mSmsStorageMonitor = this.mTelephonyComponentFactory.inject(SmsStorageMonitor.class.getName()).makeSmsStorageMonitor(this);
            this.mSmsUsageMonitor = this.mTelephonyComponentFactory.inject(SmsUsageMonitor.class.getName()).makeSmsUsageMonitor(context);
            this.mUiccController = UiccController.getInstance();
            this.mUiccController.registerForIccChanged(this, 30, null);
            this.mSimActivationTracker = this.mTelephonyComponentFactory.inject(SimActivationTracker.class.getName()).makeSimActivationTracker(this);
            if (getPhoneType() != 3) {
                this.mCi.registerForSrvccStateChanged(this, 31, null);
            }
            this.mCi.setOnUnsolOemHookRaw(this, 34, null);
            this.mCi.startLceService(200, true, obtainMessage(37));
            this.mDeviceStateMonitor = this.mTelephonyComponentFactory.makeDeviceStateMonitor(this);
            this.mReference = (IOppoPhone) OppoTelephonyFactory.getInstance().getFeature(IOppoPhone.DEFAULT, this);
        }
    }

    public void startMonitoringImsService() {
        if (getPhoneType() != 3) {
            synchronized (lockForRadioTechnologyChange) {
                IntentFilter filter = new IntentFilter();
                ImsManager imsManager = ImsManager.getInstance(this.mContext, getPhoneId());
                if (imsManager != null && !imsManager.isDynamicBinding()) {
                    filter.addAction("com.android.ims.IMS_SERVICE_UP");
                    filter.addAction("com.android.ims.IMS_SERVICE_DOWN");
                    this.mContext.registerReceiver(this.mImsIntentReceiver, filter);
                }
                if (imsManager != null && (imsManager.isDynamicBinding() || imsManager.isServiceAvailable())) {
                    this.mImsServiceReady = true;
                    updateImsPhone();
                }
            }
        }
    }

    public boolean supportsConversionOfCdmaCallerIdMmiCodesWhileRoaming() {
        PersistableBundle b = ((CarrierConfigManager) getContext().getSystemService("carrier_config")).getConfigForSubId(getSubId());
        if (b != null) {
            return b.getBoolean("convert_cdma_caller_id_mmi_codes_while_roaming_on_3gpp_bool", false);
        }
        return false;
    }

    public void handleMessage(Message msg) {
        int i = msg.what;
        if (i == 16 || i == 17) {
            handleSetSelectNetwork((AsyncResult) msg.obj);
            return;
        }
        int i2 = msg.what;
        if (i2 == 14) {
            Rlog.d(LOG_TAG, "Event EVENT_CALL_RING Received state=" + getState());
            if (((AsyncResult) msg.obj).exception == null) {
                PhoneConstants.State state = getState();
                if (this.mDoesRilSendMultipleCallRing || !(state == PhoneConstants.State.RINGING || state == PhoneConstants.State.IDLE)) {
                    notifyIncomingRing();
                    return;
                }
                this.mCallRingContinueToken++;
                sendIncomingCallRingNotification(this.mCallRingContinueToken);
            }
        } else if (i2 == 15) {
            Rlog.d(LOG_TAG, "Event EVENT_CALL_RING_CONTINUE Received state=" + getState());
            if (getState() == PhoneConstants.State.RINGING) {
                sendIncomingCallRingNotification(msg.arg1);
            }
        } else if (i2 == 34) {
            AsyncResult ar = (AsyncResult) msg.obj;
            if (ar.exception == null) {
                this.mNotifier.notifyOemHookRawEventForSubscriber(this, (byte[]) ar.result);
                return;
            }
            Rlog.e(LOG_TAG, "OEM hook raw exception: " + ar.exception);
        } else if (i2 != 52) {
            if (i2 == 37) {
                AsyncResult ar2 = (AsyncResult) msg.obj;
                if (ar2.exception != null) {
                    Rlog.d(LOG_TAG, "config LCE service failed: " + ar2.exception);
                    return;
                }
                this.mLceStatus = ((ArrayList) ar2.result).get(0).intValue();
            } else if (i2 != 38) {
                switch (i2) {
                    case 30:
                        onUpdateIccAvailability();
                        return;
                    case 31:
                        AsyncResult ar3 = (AsyncResult) msg.obj;
                        if (ar3.exception == null) {
                            handleSrvccStateChanged((int[]) ar3.result);
                            return;
                        }
                        Rlog.e(LOG_TAG, "Srvcc exception: " + ar3.exception);
                        return;
                    case 32:
                        Rlog.d(LOG_TAG, "Event EVENT_INITIATE_SILENT_REDIAL Received");
                        AsyncResult ar4 = (AsyncResult) msg.obj;
                        if (ar4.exception == null && ar4.result != null) {
                            String dialString = (String) ar4.result;
                            if (!TextUtils.isEmpty(dialString)) {
                                try {
                                    dialInternal(dialString, new PhoneInternalInterface.DialArgs.Builder().build());
                                    return;
                                } catch (CallStateException e) {
                                    Rlog.e(LOG_TAG, "silent redial failed: " + e);
                                    return;
                                }
                            } else {
                                return;
                            }
                        } else {
                            return;
                        }
                    default:
                        Rlog.w(LOG_TAG, "unexpected event not handled:" + msg.what);
                        return;
                }
            } else {
                onCheckForNetworkSelectionModeAutomatic(msg);
            }
        } else if (areAllDataDisconnected()) {
            this.mAllDataDisconnectedRegistrants.notifyRegistrants();
        }
    }

    public ArrayList<Connection> getHandoverConnection() {
        return null;
    }

    public void notifySrvccState(Call.SrvccState state) {
    }

    public void registerForSilentRedial(Handler h, int what, Object obj) {
    }

    public void unregisterForSilentRedial(Handler h) {
    }

    /* access modifiers changed from: protected */
    public boolean needNotifySrvccState() {
        return false;
    }

    private void handleSrvccStateChanged(int[] ret) {
        Call.SrvccState srvccState;
        Rlog.d(LOG_TAG, "handleSrvccStateChanged");
        ArrayList<Connection> conn = null;
        Phone imsPhone = this.mImsPhone;
        Call.SrvccState srvccState2 = Call.SrvccState.NONE;
        if (ret != null && ret.length != 0) {
            int state = ret[0];
            if (state == 0) {
                srvccState = Call.SrvccState.STARTED;
                if (imsPhone != null) {
                    ArrayList<Connection> conn2 = imsPhone.getHandoverConnection();
                    migrateFrom(imsPhone);
                    if (imsPhone.needNotifySrvccState()) {
                        imsPhone.notifySrvccState(srvccState);
                    }
                    conn = conn2;
                } else {
                    Rlog.d(LOG_TAG, "HANDOVER_STARTED: mImsPhone null");
                }
            } else if (state == 1) {
                srvccState = Call.SrvccState.COMPLETED;
                if (imsPhone != null) {
                    imsPhone.notifySrvccState(srvccState);
                } else {
                    Rlog.d(LOG_TAG, "HANDOVER_COMPLETED: mImsPhone null");
                }
            } else if (state == 2 || state == 3) {
                srvccState = Call.SrvccState.FAILED;
                if (imsPhone == null) {
                    Rlog.d(LOG_TAG, "HANDOVER_FAILED: mImsPhone null");
                } else if (imsPhone.needNotifySrvccState()) {
                    imsPhone.notifySrvccState(srvccState);
                }
            } else {
                return;
            }
            getCallTracker().notifySrvccState(srvccState, conn);
            notifySrvccStateChanged(state);
            updateSrvccState(srvccState);
        }
    }

    @UnsupportedAppUsage
    public Context getContext() {
        return this.mContext;
    }

    public void disableDnsCheck(boolean b) {
        this.mDnsCheckDisabled = b;
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        editor.putBoolean(DNS_SERVER_CHECK_DISABLED_KEY, b);
        editor.apply();
    }

    public boolean isDnsCheckDisabled() {
        return this.mDnsCheckDisabled;
    }

    @UnsupportedAppUsage
    public void registerForPreciseCallStateChanged(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        this.mPreciseCallStateRegistrants.addUnique(h, what, obj);
    }

    @UnsupportedAppUsage
    public void unregisterForPreciseCallStateChanged(Handler h) {
        this.mPreciseCallStateRegistrants.remove(h);
    }

    /* access modifiers changed from: protected */
    public void notifyPreciseCallStateChangedP() {
        this.mPreciseCallStateRegistrants.notifyRegistrants(new AsyncResult((Object) null, this, (Throwable) null));
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
        this.mHandoverRegistrants.notifyRegistrants(new AsyncResult((Object) null, cn, (Throwable) null));
    }

    /* access modifiers changed from: protected */
    public void setIsInEmergencyCall() {
    }

    public void notifySmsSent(String destinationAddress) {
        TelephonyManager m = (TelephonyManager) getContext().getSystemService("phone");
        if (m != null && m.isEmergencyNumber(destinationAddress)) {
            this.mLocalLog.log("Emergency SMS detected, recording time.");
            this.mTimeLastEmergencySmsSentMs = SystemClock.elapsedRealtime();
        }
    }

    public boolean isInEmergencySmsMode() {
        PersistableBundle b;
        int eSmsTimerMs;
        long lastSmsTimeMs = this.mTimeLastEmergencySmsSentMs;
        boolean isInEmergencySmsMode = false;
        if (lastSmsTimeMs == -1 || (b = ((CarrierConfigManager) getContext().getSystemService("carrier_config")).getConfigForSubId(getSubId())) == null || (eSmsTimerMs = b.getInt("emergency_sms_mode_timer_ms_int", 0)) == 0) {
            return false;
        }
        if (eSmsTimerMs > EMERGENCY_SMS_TIMER_MAX_MS) {
            eSmsTimerMs = EMERGENCY_SMS_TIMER_MAX_MS;
        }
        if (SystemClock.elapsedRealtime() <= ((long) eSmsTimerMs) + lastSmsTimeMs) {
            isInEmergencySmsMode = true;
        }
        if (!isInEmergencySmsMode) {
            this.mTimeLastEmergencySmsSentMs = -1;
        } else {
            this.mLocalLog.log("isInEmergencySmsMode: queried while eSMS mode is active.");
        }
        return isInEmergencySmsMode;
    }

    /* access modifiers changed from: protected */
    public void migrateFrom(Phone from) {
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
        migrate(this.mCellInfoRegistrants, from.mCellInfoRegistrants);
        if (from.isInEmergencyCall()) {
            setIsInEmergencyCall();
        }
    }

    /* access modifiers changed from: protected */
    public void migrate(RegistrantList to, RegistrantList from) {
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

    @UnsupportedAppUsage
    public void registerForUnknownConnection(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        this.mUnknownConnectionRegistrants.addUnique(h, what, obj);
    }

    @UnsupportedAppUsage
    public void unregisterForUnknownConnection(Handler h) {
        this.mUnknownConnectionRegistrants.remove(h);
    }

    @UnsupportedAppUsage
    public void registerForNewRingingConnection(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        this.mNewRingingConnectionRegistrants.addUnique(h, what, obj);
    }

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    public void registerForIncomingRing(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        this.mIncomingRingRegistrants.addUnique(h, what, obj);
    }

    @UnsupportedAppUsage
    public void unregisterForIncomingRing(Handler h) {
        this.mIncomingRingRegistrants.remove(h);
    }

    @UnsupportedAppUsage
    public void registerForDisconnect(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        this.mDisconnectRegistrants.addUnique(h, what, obj);
    }

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    public void registerForMmiInitiate(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        this.mMmiRegistrants.addUnique(h, what, obj);
    }

    @UnsupportedAppUsage
    public void unregisterForMmiInitiate(Handler h) {
        this.mMmiRegistrants.remove(h);
    }

    @UnsupportedAppUsage
    public void registerForMmiComplete(Handler h, int what, Object obj) {
        checkCorrectThread(h);
        this.mMmiCompleteRegistrants.addUnique(h, what, obj);
    }

    @UnsupportedAppUsage
    public void unregisterForMmiComplete(Handler h) {
        checkCorrectThread(h);
        this.mMmiCompleteRegistrants.remove(h);
    }

    @UnsupportedAppUsage
    public void registerForSimRecordsLoaded(Handler h, int what, Object obj) {
    }

    @UnsupportedAppUsage
    public void unregisterForSimRecordsLoaded(Handler h) {
    }

    public void registerForTtyModeReceived(Handler h, int what, Object obj) {
    }

    public void unregisterForTtyModeReceived(Handler h) {
    }

    @UnsupportedAppUsage
    public void setNetworkSelectionModeAutomatic(Message response) {
        Rlog.d(LOG_TAG, "setNetworkSelectionModeAutomatic, querying current mode");
        Message msg = obtainMessage(38);
        msg.obj = response;
        this.mCi.getNetworkSelectionMode(msg);
    }

    /* access modifiers changed from: protected */
    public void onCheckForNetworkSelectionModeAutomatic(Message fromRil) {
        AsyncResult ar = (AsyncResult) fromRil.obj;
        Message response = (Message) ar.userObj;
        boolean doAutomatic = true;
        if (ar.exception == null && ar.result != null) {
            try {
                if (((int[]) ar.result)[0] == 0) {
                    doAutomatic = false;
                }
            } catch (Exception e) {
            }
        }
        NetworkSelectMessage nsm = new NetworkSelectMessage();
        nsm.message = response;
        nsm.operatorNumeric = PhoneConfigurationManager.SSSS;
        nsm.operatorAlphaLong = PhoneConfigurationManager.SSSS;
        nsm.operatorAlphaShort = PhoneConfigurationManager.SSSS;
        if (doAutomatic) {
            this.mCi.setNetworkSelectionModeAutomatic(obtainMessage(17, nsm));
        } else {
            Rlog.d(LOG_TAG, "setNetworkSelectionModeAutomatic - already auto, ignoring");
            if (nsm.message != null) {
                nsm.message.arg1 = 1;
            }
            ar.userObj = nsm;
            handleSetSelectNetwork(ar);
        }
        updateSavedNetworkOperator(nsm);
    }

    public void getNetworkSelectionMode(Message message) {
        this.mCi.getNetworkSelectionMode(message);
    }

    public List<ClientRequestStats> getClientRequestStats() {
        return this.mCi.getClientRequestStats();
    }

    @UnsupportedAppUsage
    public void selectNetworkManually(OperatorInfo network, boolean persistSelection, Message response) {
        NetworkSelectMessage nsm = new NetworkSelectMessage();
        nsm.message = response;
        nsm.operatorNumeric = network.getOperatorNumeric();
        nsm.operatorAlphaLong = network.getOperatorAlphaLong();
        nsm.operatorAlphaShort = network.getOperatorAlphaShort();
        this.mCi.setNetworkSelectionModeManual(network.getOperatorNumeric(), obtainMessage(16, nsm));
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

    /* access modifiers changed from: protected */
    public void updateSavedNetworkOperator(NetworkSelectMessage nsm) {
        int subId = getSubId();
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
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

    /* access modifiers changed from: protected */
    public void handleSetSelectNetwork(AsyncResult ar) {
        if (!(ar.userObj instanceof NetworkSelectMessage)) {
            Rlog.e(LOG_TAG, "unexpected result from user object.");
            return;
        }
        NetworkSelectMessage nsm = (NetworkSelectMessage) ar.userObj;
        if (nsm.message != null) {
            AsyncResult.forMessage(nsm.message, ar.result, ar.exception);
            nsm.message.sendToTarget();
        }
    }

    private OperatorInfo getSavedNetworkSelection() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        String numeric = sp.getString(NETWORK_SELECTION_KEY + getSubId(), PhoneConfigurationManager.SSSS);
        String name = sp.getString(NETWORK_SELECTION_NAME_KEY + getSubId(), PhoneConfigurationManager.SSSS);
        return new OperatorInfo(name, sp.getString(NETWORK_SELECTION_SHORT_KEY + getSubId(), PhoneConfigurationManager.SSSS), numeric);
    }

    /* access modifiers changed from: protected */
    public void clearSavedNetworkSelection() {
        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        SharedPreferences.Editor remove = edit.remove(NETWORK_SELECTION_KEY + getSubId());
        SharedPreferences.Editor remove2 = remove.remove(NETWORK_SELECTION_NAME_KEY + getSubId());
        remove2.remove(NETWORK_SELECTION_SHORT_KEY + getSubId()).commit();
    }

    /* access modifiers changed from: protected */
    public void restoreSavedNetworkSelection(Message response) {
        OperatorInfo networkSelection = getSavedNetworkSelection();
        if (networkSelection == null || TextUtils.isEmpty(networkSelection.getOperatorNumeric())) {
            setNetworkSelectionModeAutomatic(response);
        } else {
            selectNetworkManually(networkSelection, true, response);
        }
    }

    public void saveClirSetting(int commandInterfaceCLIRMode) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        editor.putInt(CLIR_KEY + getPhoneId(), commandInterfaceCLIRMode);
        Rlog.i(LOG_TAG, "saveClirSetting: clir_key" + getPhoneId() + "=" + commandInterfaceCLIRMode);
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

    /* access modifiers changed from: protected */
    public void notifyDisconnectP(Connection cn) {
        this.mDisconnectRegistrants.notifyRegistrants(new AsyncResult((Object) null, cn, (Throwable) null));
    }

    @UnsupportedAppUsage
    public void registerForServiceStateChanged(Handler h, int what, Object obj) {
        this.mServiceStateRegistrants.add(h, what, obj);
    }

    @UnsupportedAppUsage
    public void unregisterForServiceStateChanged(Handler h) {
        this.mServiceStateRegistrants.remove(h);
    }

    @UnsupportedAppUsage
    public void registerForRingbackTone(Handler h, int what, Object obj) {
        this.mCi.registerForRingbackTone(h, what, obj);
    }

    @UnsupportedAppUsage
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

    public void registerForCellInfo(Handler h, int what, Object obj) {
        this.mCellInfoRegistrants.add(h, what, obj);
    }

    public void unregisterForCellInfo(Handler h) {
        this.mCellInfoRegistrants.remove(h);
    }

    public void setEchoSuppressionEnabled() {
    }

    /* access modifiers changed from: protected */
    public void notifyServiceStateChangedP(ServiceState ss) {
        this.mServiceStateRegistrants.notifyRegistrants(new AsyncResult((Object) null, ss, (Throwable) null));
        this.mNotifier.notifyServiceState(this);
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
        CharSequence[] carrierLocales = ctx.getResources().getTextArray(17235974);
        for (int i = 0; i < carrierLocales.length; i += 3) {
            if (carrier.equals(carrierLocales[i].toString())) {
                return Locale.forLanguageTag(carrierLocales[i + 1].toString().replace('_', '-'));
            }
        }
        return null;
    }

    @UnsupportedAppUsage
    public IccFileHandler getIccFileHandler() {
        IccFileHandler fh;
        UiccCardApplication uiccApplication = this.mUiccApplication.get();
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

    @UnsupportedAppUsage
    public ServiceStateTracker getServiceStateTracker() {
        return null;
    }

    public EmergencyNumberTracker getEmergencyNumberTracker() {
        return null;
    }

    @UnsupportedAppUsage
    public CallTracker getCallTracker() {
        return null;
    }

    public TransportManager getTransportManager() {
        return null;
    }

    public void setVoiceActivationState(int state) {
        this.mSimActivationTracker.setVoiceActivationState(state);
    }

    public void setDataActivationState(int state) {
        this.mSimActivationTracker.setDataActivationState(state);
    }

    public int getVoiceActivationState() {
        return this.mSimActivationTracker.getVoiceActivationState();
    }

    public int getDataActivationState() {
        return this.mSimActivationTracker.getDataActivationState();
    }

    public void updateVoiceMail() {
        Rlog.e(LOG_TAG, "updateVoiceMail() should be overridden");
    }

    public IccCardApplicationStatus.AppType getCurrentUiccAppType() {
        UiccCardApplication currentApp = this.mUiccApplication.get();
        if (currentApp != null) {
            return currentApp.getType();
        }
        return IccCardApplicationStatus.AppType.APPTYPE_UNKNOWN;
    }

    @UnsupportedAppUsage
    public IccCard getIccCard() {
        return null;
    }

    @UnsupportedAppUsage
    public String getIccSerialNumber() {
        IccRecords r = this.mIccRecords.get();
        if (r != null) {
            return r.getIccId();
        }
        return null;
    }

    public String getFullIccSerialNumber() {
        IccRecords r = this.mIccRecords.get();
        if (r != null) {
            return r.getFullIccId();
        }
        return null;
    }

    public boolean getIccRecordsLoaded() {
        IccRecords r = this.mIccRecords.get();
        if (r != null) {
            return r.getRecordsLoaded();
        }
        return false;
    }

    public void setCellInfoMinInterval(int interval) {
        getServiceStateTracker().setCellInfoMinInterval(interval);
    }

    public List<CellInfo> getAllCellInfo() {
        return getServiceStateTracker().getAllCellInfo();
    }

    public void requestCellInfoUpdate(WorkSource workSource, Message rspMsg) {
        getServiceStateTracker().requestAllCellInfo(workSource, rspMsg);
    }

    @UnsupportedAppUsage
    public CellLocation getCellLocation() {
        return getServiceStateTracker().getCellLocation();
    }

    public void getCellLocation(WorkSource workSource, Message rspMsg) {
        getServiceStateTracker().requestCellLocation(workSource, rspMsg);
    }

    public void setCellInfoListRate(int rateInMillis, WorkSource workSource) {
        this.mCi.setCellInfoListRate(rateInMillis, null, workSource);
    }

    public boolean getMessageWaitingIndicator() {
        return this.mVmCount != 0;
    }

    /* access modifiers changed from: protected */
    public int getCallForwardingIndicatorFromSharedPref() {
        String subscriberId;
        int status = 0;
        int subId = getSubId();
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
            status = sp.getInt(CF_STATUS + subId, -1);
            Rlog.d(LOG_TAG, "getCallForwardingIndicatorFromSharedPref: for subId " + subId + "= " + status);
            if (status == -1 && (subscriberId = sp.getString(CF_ID, null)) != null) {
                if (subscriberId.equals(getSubscriberId())) {
                    boolean z = false;
                    status = sp.getInt(CF_STATUS, 0);
                    if (status == 1) {
                        z = true;
                    }
                    setCallForwardingIndicatorInSharedPref(z);
                    Rlog.d(LOG_TAG, "getCallForwardingIndicatorFromSharedPref: " + status);
                } else {
                    Rlog.d(LOG_TAG, "getCallForwardingIndicatorFromSharedPref: returning DISABLED as status for matching subscriberId not found");
                }
                SharedPreferences.Editor editor = sp.edit();
                editor.remove(CF_ID);
                editor.remove(CF_STATUS);
                editor.apply();
            }
        } else {
            Rlog.e(LOG_TAG, "getCallForwardingIndicatorFromSharedPref: invalid subId " + subId);
        }
        return status;
    }

    /* access modifiers changed from: protected */
    public void setCallForwardingIndicatorInSharedPref(boolean enable) {
        int status;
        if (enable) {
            status = 1;
        } else {
            status = 0;
        }
        int subId = getSubId();
        Rlog.i(LOG_TAG, "setCallForwardingIndicatorInSharedPref: Storing status = " + status + " in pref " + CF_STATUS + subId);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
        StringBuilder sb = new StringBuilder();
        sb.append(CF_STATUS);
        sb.append(subId);
        editor.putInt(sb.toString(), status);
        editor.apply();
    }

    public void setVoiceCallForwardingFlag(int line, boolean enable, String number) {
        setCallForwardingIndicatorInSharedPref(enable);
        IccRecords r = this.mIccRecords.get();
        if (r != null) {
            r.setVoiceCallForwardingFlag(line, enable, number);
        }
    }

    /* access modifiers changed from: protected */
    public void setVoiceCallForwardingFlag(IccRecords r, int line, boolean enable, String number) {
        setCallForwardingIndicatorInSharedPref(enable);
        r.setVoiceCallForwardingFlag(line, enable, number);
    }

    public boolean getCallForwardingIndicator() {
        if (getPhoneType() == 2) {
            Rlog.e(LOG_TAG, "getCallForwardingIndicator: not possible in CDMA");
            return false;
        }
        IccRecords r = this.mIccRecords.get();
        int callForwardingIndicator = -1;
        if (r != null) {
            callForwardingIndicator = r.getVoiceCallForwardingFlag();
        }
        if (callForwardingIndicator == -1) {
            callForwardingIndicator = getCallForwardingIndicatorFromSharedPref();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("getCallForwardingIndicator: iccForwardingFlag=");
        sb.append(r != null ? Integer.valueOf(r.getVoiceCallForwardingFlag()) : "null");
        sb.append(", sharedPrefFlag=");
        sb.append(getCallForwardingIndicatorFromSharedPref());
        Rlog.v(LOG_TAG, sb.toString());
        if (callForwardingIndicator == 1 || getVideoCallForwardingFlag()) {
            return true;
        }
        return false;
    }

    public CarrierSignalAgent getCarrierSignalAgent() {
        return this.mCarrierSignalAgent;
    }

    public CarrierActionAgent getCarrierActionAgent() {
        return this.mCarrierActionAgent;
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

    public boolean isConcurrentVoiceAndDataAllowed() {
        ServiceStateTracker sst = getServiceStateTracker();
        if (sst == null) {
            return false;
        }
        return sst.isConcurrentVoiceAndDataAllowed();
    }

    public void setCdmaRoamingPreference(int cdmaRoamingType, Message response) {
        this.mCi.setCdmaRoamingPreference(cdmaRoamingType, response);
    }

    public void setCdmaSubscription(int cdmaSubscriptionType, Message response) {
        this.mCi.setCdmaSubscriptionSource(cdmaSubscriptionType, response);
    }

    @UnsupportedAppUsage
    public void setPreferredNetworkType(int networkType, Message response) {
        int modemRaf = getRadioAccessFamily();
        int rafFromType = RadioAccessFamily.getRafFromNetworkType(networkType);
        if (modemRaf == 0 || rafFromType == 0) {
            Rlog.d(LOG_TAG, "setPreferredNetworkType: Abort, unknown RAF: " + modemRaf + " " + rafFromType);
            if (response != null) {
                AsyncResult.forMessage(response, (Object) null, new CommandException(CommandException.Error.GENERIC_FAILURE));
                response.sendToTarget();
                return;
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

    @UnsupportedAppUsage
    public void getSmscAddress(Message result) {
        this.mCi.getSmscAddress(result);
    }

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    @Deprecated
    public void invokeOemRilRequestRaw(byte[] data, Message response) {
        this.mCi.invokeOemRilRequestRaw(data, response);
    }

    @UnsupportedAppUsage
    @Deprecated
    public void invokeOemRilRequestStrings(String[] strings, Message response) {
        this.mCi.invokeOemRilRequestStrings(strings, response);
    }

    public void nvReadItem(int itemID, Message response, WorkSource workSource) {
        this.mCi.nvReadItem(itemID, response, workSource);
    }

    public void nvWriteItem(int itemID, String itemValue, Message response, WorkSource workSource) {
        this.mCi.nvWriteItem(itemID, itemValue, response, workSource);
    }

    public void nvWriteCdmaPrl(byte[] preferredRoamingList, Message response) {
        this.mCi.nvWriteCdmaPrl(preferredRoamingList, response);
    }

    public void rebootModem(Message response) {
        this.mCi.nvResetConfig(1, response);
    }

    public void resetModemConfig(Message response) {
        this.mCi.nvResetConfig(3, response);
    }

    public void notifyDataActivity() {
        this.mNotifier.notifyDataActivity(this);
    }

    private void notifyMessageWaitingIndicator() {
        if (this.mIsVoiceCapable) {
            this.mNotifier.notifyMessageWaitingChanged(this);
        }
    }

    public void notifyDataConnection(String apnType) {
        this.mNotifier.notifyDataConnection(this, apnType, getDataConnectionState(apnType));
    }

    public void notifyDataConnection() {
        String[] types = getActiveApnTypes();
        if (types != null) {
            for (String apnType : types) {
                this.mNotifier.notifyDataConnection(this, apnType, getDataConnectionState(apnType));
            }
        }
    }

    @UnsupportedAppUsage
    public void notifyOtaspChanged(int otaspMode) {
        this.mNotifier.notifyOtaspChanged(this, otaspMode);
    }

    public void notifyVoiceActivationStateChanged(int state) {
        this.mNotifier.notifyVoiceActivationStateChanged(this, state);
    }

    public void notifyDataActivationStateChanged(int state) {
        this.mNotifier.notifyDataActivationStateChanged(this, state);
    }

    public void notifyUserMobileDataStateChanged(boolean state) {
        this.mNotifier.notifyUserMobileDataStateChanged(this, state);
    }

    public void notifySignalStrength() {
        this.mNotifier.notifySignalStrength(this);
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public PhoneConstants.DataState getDataConnectionState(String apnType) {
        return PhoneConstants.DataState.DISCONNECTED;
    }

    public void notifyCellInfo(List<CellInfo> cellInfo) {
        this.mCellInfoRegistrants.notifyRegistrants(new AsyncResult((Object) null, cellInfo, (Throwable) null));
        this.mNotifier.notifyCellInfo(this, cellInfo);
    }

    public void notifyPhysicalChannelConfiguration(List<PhysicalChannelConfig> configs) {
        this.mNotifier.notifyPhysicalChannelConfiguration(this, configs);
    }

    public void notifySrvccStateChanged(int state) {
        this.mNotifier.notifySrvccStateChanged(this, state);
    }

    public void notifyEmergencyNumberList() {
        this.mNotifier.notifyEmergencyNumberList(this);
    }

    public boolean isInEmergencyCall() {
        return false;
    }

    protected static boolean getInEcmMode() {
        return SystemProperties.getBoolean("ril.cdma.inecmmode", false);
    }

    public boolean isInEcm() {
        return this.mIsPhoneInEcmState;
    }

    public void setIsInEcm(boolean isInEcm) {
        setGlobalSystemProperty("ril.cdma.inecmmode", String.valueOf(isInEcm));
        this.mIsPhoneInEcmState = isInEcm;
    }

    @UnsupportedAppUsage
    private static int getVideoState(Call call) {
        Connection conn = call.getEarliestConnection();
        if (conn != null) {
            return conn.getVideoState();
        }
        return 0;
    }

    private boolean isVideoCallOrConference(Call call) {
        ImsCall imsCall;
        if (call.isMultiparty()) {
            return true;
        }
        if (!(call instanceof ImsPhoneCall) || (imsCall = ((ImsPhoneCall) call).getImsCall()) == null || (!imsCall.isVideoCall() && !imsCall.wasVideoCall())) {
            return false;
        }
        return true;
    }

    public boolean isImsVideoCallOrConferencePresent() {
        boolean isPresent = false;
        Phone phone = this.mImsPhone;
        if (phone != null) {
            isPresent = isVideoCallOrConference(phone.getForegroundCall()) || isVideoCallOrConference(this.mImsPhone.getBackgroundCall()) || isVideoCallOrConference(this.mImsPhone.getRingingCall());
        }
        Rlog.d(LOG_TAG, "isImsVideoCallOrConferencePresent: " + isPresent);
        return isPresent;
    }

    public int getVoiceMessageCount() {
        return this.mVmCount;
    }

    public void setVoiceMessageCount(int countWaiting) {
        this.mVmCount = countWaiting;
        int subId = getSubId();
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            Rlog.d(LOG_TAG, "setVoiceMessageCount: Storing Voice Mail Count = " + countWaiting + " for mVmCountKey = " + VM_COUNT + subId + " in preferences.");
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
            StringBuilder sb = new StringBuilder();
            sb.append(VM_COUNT);
            sb.append(subId);
            editor.putInt(sb.toString(), countWaiting);
            editor.apply();
        } else {
            Rlog.e(LOG_TAG, "setVoiceMessageCount in sharedPreference: invalid subId " + subId);
        }
        IccRecords records = UiccController.getInstance().getIccRecords(this.mPhoneId, 1);
        if (records != null) {
            Rlog.d(LOG_TAG, "setVoiceMessageCount: updating SIM Records");
            records.setVoiceMessageWaiting(1, countWaiting);
        } else {
            Rlog.d(LOG_TAG, "setVoiceMessageCount: SIM Records not found");
        }
        notifyMessageWaitingIndicator();
    }

    /* access modifiers changed from: protected */
    public int getStoredVoiceMessageCount() {
        int countVoiceMessages = 0;
        int subId = getSubId();
        if (SubscriptionManager.isValidSubscriptionId(subId)) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
            int countFromSP = sp.getInt(VM_COUNT + subId, -2);
            if (countFromSP != -2) {
                Rlog.d(LOG_TAG, "getStoredVoiceMessageCount: from preference for subId " + subId + "= " + countFromSP);
                return countFromSP;
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
            SharedPreferences.Editor editor = sp.edit();
            editor.remove(VM_ID);
            editor.remove(VM_COUNT);
            editor.apply();
            return countVoiceMessages;
        }
        Rlog.e(LOG_TAG, "getStoredVoiceMessageCount: invalid subId " + subId);
        return 0;
    }

    public void sendDialerSpecialCode(String code) {
        handleCustomizedMMICodes(code);
        if (!TextUtils.isEmpty(code)) {
            int len = code.length();
            if (len > 8 && code.startsWith("*#*#") && code.endsWith("#*#*")) {
                code = code.substring(4, len - 4);
            }
            BroadcastOptions options = BroadcastOptions.makeBasic();
            options.setBackgroundActivityStartsAllowed(true);
            Intent intent = new Intent("android.provider.Telephony.SECRET_CODE", Uri.parse("android_secret_code://" + code));
            intent.addFlags(16777216);
            this.mContext.sendBroadcast(intent, null, options.toBundle());
            Intent secrectCodeIntent = new Intent("android.telephony.action.SECRET_CODE", Uri.parse("android_secret_code://" + code));
            secrectCodeIntent.addFlags(16777216);
            this.mContext.sendBroadcast(secrectCodeIntent, null, options.toBundle());
        }
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

    @UnsupportedAppUsage
    public void setOnPostDialCharacter(Handler h, int what, Object obj) {
        this.mPostDialHandler = new Registrant(h, what, obj);
    }

    public Registrant getPostDialHandler() {
        return this.mPostDialHandler;
    }

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    public void registerForEcmTimerReset(Handler h, int what, Object obj) {
    }

    @UnsupportedAppUsage
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

    @UnsupportedAppUsage
    public void setOnEcbModeExitResponse(Handler h, int what, Object obj) {
    }

    @UnsupportedAppUsage
    public void unsetOnEcbModeExitResponse(Handler h) {
    }

    public void registerForRadioOffOrNotAvailable(Handler h, int what, Object obj) {
        this.mRadioOffOrNotAvailableRegistrants.addUnique(h, what, obj);
    }

    public void unregisterForRadioOffOrNotAvailable(Handler h) {
        this.mRadioOffOrNotAvailableRegistrants.remove(h);
    }

    @UnsupportedAppUsage
    public String[] getActiveApnTypes() {
        if (this.mTransportManager == null) {
            return null;
        }
        List<String> typesList = new ArrayList<>();
        int[] availableTransports = this.mTransportManager.getAvailableTransports();
        for (int transportType : availableTransports) {
            if (getDcTracker(transportType) != null) {
                typesList.addAll(Arrays.asList(getDcTracker(transportType).getActiveApnTypes()));
            }
        }
        return (String[]) typesList.toArray(new String[typesList.size()]);
    }

    public boolean hasMatchedTetherApnSetting() {
        if (getDcTracker(1) != null) {
            return getDcTracker(1).hasMatchedTetherApnSetting();
        }
        return false;
    }

    public String getActiveApnHost(String apnType) {
        TransportManager transportManager = this.mTransportManager;
        if (transportManager == null) {
            return null;
        }
        int transportType = transportManager.getCurrentTransport(ApnSetting.getApnTypesBitmaskFromString(apnType));
        if (getDcTracker(transportType) != null) {
            return getDcTracker(transportType).getActiveApnString(apnType);
        }
        return null;
    }

    public LinkProperties getLinkProperties(String apnType) {
        TransportManager transportManager = this.mTransportManager;
        if (transportManager == null) {
            return null;
        }
        int transport = transportManager.getCurrentTransport(ApnSetting.getApnTypesBitmaskFromString(apnType));
        if (getDcTracker(transport) != null) {
            return getDcTracker(transport).getLinkProperties(apnType);
        }
        return null;
    }

    public NetworkCapabilities getNetworkCapabilities(String apnType) {
        TransportManager transportManager = this.mTransportManager;
        if (transportManager == null) {
            return null;
        }
        int transportType = transportManager.getCurrentTransport(ApnSetting.getApnTypesBitmaskFromString(apnType));
        if (getDcTracker(transportType) != null) {
            return getDcTracker(transportType).getNetworkCapabilities(apnType);
        }
        return null;
    }

    public boolean isDataAllowed(int apnType) {
        return isDataAllowed(apnType, null);
    }

    public boolean isDataAllowed(int apnType, DataConnectionReasons reasons) {
        TransportManager transportManager = this.mTransportManager;
        if (transportManager == null) {
            return false;
        }
        int transport = transportManager.getCurrentTransport(apnType);
        if (getDcTracker(transport) != null) {
            return getDcTracker(transport).isDataAllowed(reasons);
        }
        return false;
    }

    public void carrierActionSetMeteredApnsEnabled(boolean enabled) {
        this.mCarrierActionAgent.carrierActionSetMeteredApnsEnabled(enabled);
    }

    public void carrierActionSetRadioEnabled(boolean enabled) {
        this.mCarrierActionAgent.carrierActionSetRadioEnabled(enabled);
    }

    public void carrierActionReportDefaultNetworkStatus(boolean report) {
        this.mCarrierActionAgent.carrierActionReportDefaultNetworkStatus(report);
    }

    public void carrierActionResetAll() {
        this.mCarrierActionAgent.carrierActionReset();
    }

    public void notifyNewRingingConnectionP(Connection cn) {
        if (this.mIsVoiceCapable) {
            this.mNewRingingConnectionRegistrants.notifyRegistrants(new AsyncResult((Object) null, cn, (Throwable) null));
        }
    }

    public void notifyUnknownConnectionP(Connection cn) {
        this.mUnknownConnectionRegistrants.notifyResult(cn);
    }

    public void notifyForVideoCapabilityChanged(boolean isVideoCallCapable) {
        this.mIsVideoCapable = isVideoCallCapable;
        this.mVideoCapabilityChangedRegistrants.notifyRegistrants(new AsyncResult((Object) null, Boolean.valueOf(isVideoCallCapable), (Throwable) null));
    }

    private void notifyIncomingRing() {
        if (this.mIsVoiceCapable) {
            this.mIncomingRingRegistrants.notifyRegistrants(new AsyncResult((Object) null, this, (Throwable) null));
        }
    }

    private void sendIncomingCallRingNotification(int token) {
        if (!this.mIsVoiceCapable || this.mDoesRilSendMultipleCallRing || token != this.mCallRingContinueToken) {
            Rlog.d(LOG_TAG, "Ignoring ring notification request, mDoesRilSendMultipleCallRing=" + this.mDoesRilSendMultipleCallRing + " token=" + token + " mCallRingContinueToken=" + this.mCallRingContinueToken + " mIsVoiceCapable=" + this.mIsVoiceCapable);
            return;
        }
        Rlog.d(LOG_TAG, "Sending notifyIncomingRing");
        notifyIncomingRing();
        sendMessageDelayed(obtainMessage(15, token, 0), (long) this.mCallRingDelay);
    }

    @UnsupportedAppUsage
    public boolean isCspPlmnEnabled() {
        return false;
    }

    @UnsupportedAppUsage
    public IsimRecords getIsimRecords() {
        Rlog.e(LOG_TAG, "getIsimRecords() is only supported on LTE devices");
        return null;
    }

    @UnsupportedAppUsage
    public String getMsisdn() {
        return null;
    }

    public String getPlmn() {
        return null;
    }

    @UnsupportedAppUsage
    public PhoneConstants.DataState getDataConnectionState() {
        return getDataConnectionState(TransportManager.IWLAN_OPERATION_MODE_DEFAULT);
    }

    public void notifyCallForwardingIndicator() {
    }

    public void notifyDataConnectionFailed(String apnType) {
        this.mNotifier.notifyDataConnectionFailed(this, apnType);
    }

    public void notifyPreciseDataConnectionFailed(String apnType, String apn, int failCause) {
        this.mNotifier.notifyPreciseDataConnectionFailed(this, apnType, apn, failCause);
    }

    public int getLteOnCdmaMode() {
        return this.mCi.getLteOnCdmaMode();
    }

    public void setVoiceMessageWaiting(int line, int countWaiting) {
        Rlog.e(LOG_TAG, "Error! This function should never be executed, inactive Phone.");
    }

    public UsimServiceTable getUsimServiceTable() {
        IccRecords r = this.mIccRecords.get();
        if (r != null) {
            return r.getUsimServiceTable();
        }
        return null;
    }

    @UnsupportedAppUsage
    public UiccCard getUiccCard() {
        return this.mUiccController.getUiccCard(this.mPhoneId);
    }

    public String[] getPcscfAddress(String apnType) {
        TransportManager transportManager = this.mTransportManager;
        if (transportManager == null) {
            return null;
        }
        int transportType = transportManager.getCurrentTransport(ApnSetting.getApnTypesBitmaskFromString(apnType));
        if (getDcTracker(transportType) != null) {
            return getDcTracker(transportType).getPcscfAddress(apnType);
        }
        return null;
    }

    public void setImsRegistrationState(boolean registered) {
    }

    @UnsupportedAppUsage
    public Phone getImsPhone() {
        return this.mImsPhone;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public ImsiEncryptionInfo getCarrierInfoForImsiEncryption(int keyType) {
        return null;
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void setCarrierInfoForImsiEncryption(ImsiEncryptionInfo imsiEncryptionInfo) {
    }

    public int getCarrierId() {
        return -1;
    }

    public String getCarrierName() {
        return null;
    }

    public int getMNOCarrierId() {
        return -1;
    }

    public int getSpecificCarrierId() {
        return -1;
    }

    public String getSpecificCarrierName() {
        return null;
    }

    public int getCarrierIdListVersion() {
        return -1;
    }

    public void resolveSubscriptionCarrierId(String simState) {
    }

    @Override // com.android.internal.telephony.PhoneInternalInterface
    public void resetCarrierKeysForImsiEncryption() {
    }

    @UnsupportedAppUsage
    public boolean isUtEnabled() {
        Phone phone = this.mImsPhone;
        if (phone != null) {
            return phone.isUtEnabled();
        }
        return false;
    }

    @UnsupportedAppUsage
    public void dispose() {
        unregister();
    }

    /* access modifiers changed from: protected */
    public void updateImsPhone() {
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

    /* access modifiers changed from: protected */
    public Connection dialInternal(String dialString, PhoneInternalInterface.DialArgs dialArgs) throws CallStateException {
        return null;
    }

    @UnsupportedAppUsage
    public int getSubId() {
        if (SubscriptionController.getInstance() != null) {
            return SubscriptionController.getInstance().getSubIdUsingPhoneId(this.mPhoneId);
        }
        Rlog.e(LOG_TAG, "SubscriptionController.getInstance = null! Returning default subId");
        return KeepaliveStatus.INVALID_HANDLE;
    }

    @UnsupportedAppUsage
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
        if (tracker == null) {
            return true;
        }
        tracker.pollState();
        return true;
    }

    private void setRoamingOverrideHelper(List<String> list, String prefix, String iccId) {
        SharedPreferences.Editor spEditor = PreferenceManager.getDefaultSharedPreferences(this.mContext).edit();
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
            if (!(sst == null || !(isImsRegistered = sst.isImsRegistered()) || sst.getCombinedRegState(getServiceState()) == 0)) {
                isImsRegistered = false;
            }
        }
        Rlog.d(LOG_TAG, "isImsRegistered =" + isImsRegistered);
        return isImsRegistered;
    }

    @UnsupportedAppUsage
    public boolean isWifiCallingEnabled() {
        Phone imsPhone = this.mImsPhone;
        boolean isWifiCallingEnabled = false;
        if (imsPhone != null) {
            isWifiCallingEnabled = imsPhone.isWifiCallingEnabled();
        }
        Rlog.d(LOG_TAG, "isWifiCallingEnabled =" + isWifiCallingEnabled);
        return isWifiCallingEnabled;
    }

    public boolean isImsCapabilityAvailable(int capability, int regTech) {
        Phone imsPhone = this.mImsPhone;
        boolean isAvailable = false;
        if (imsPhone != null) {
            isAvailable = imsPhone.isImsCapabilityAvailable(capability, regTech);
        }
        Rlog.d(LOG_TAG, "isImsRegistered =" + isAvailable);
        return isAvailable;
    }

    @UnsupportedAppUsage
    public boolean isVolteEnabled() {
        Phone imsPhone = this.mImsPhone;
        boolean isVolteEnabled = false;
        if (imsPhone != null) {
            isVolteEnabled = imsPhone.isVolteEnabled();
        }
        Rlog.d(LOG_TAG, "isImsRegistered =" + isVolteEnabled);
        return isVolteEnabled;
    }

    public int getImsRegistrationTech() {
        Phone imsPhone = this.mImsPhone;
        int regTech = -1;
        if (imsPhone != null) {
            regTech = imsPhone.getImsRegistrationTech();
        }
        Rlog.d(LOG_TAG, "getImsRegistrationTechnology =" + regTech);
        return regTech;
    }

    private boolean getRoamingOverrideHelper(String prefix, String key) {
        String iccId = getIccSerialNumber();
        if (TextUtils.isEmpty(iccId) || TextUtils.isEmpty(key)) {
            return false;
        }
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        Set<String> value = sp.getStringSet(prefix + iccId, null);
        if (value == null) {
            return false;
        }
        return value.contains(key);
    }

    public int getRadioPowerState() {
        return this.mCi.getRadioState();
    }

    public boolean isRadioAvailable() {
        return this.mCi.getRadioState() != 2;
    }

    public boolean isRadioOn() {
        return this.mCi.getRadioState() == 1;
    }

    public void shutdownRadio() {
        notifyForShutDownChanged();
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
        if (rc == null) {
            return 0;
        }
        return rc.getRadioAccessFamily();
    }

    public String getModemUuId() {
        RadioCapability rc = getRadioCapability();
        return rc == null ? PhoneConfigurationManager.SSSS : rc.getLogicalModemUuid();
    }

    public RadioCapability getRadioCapability() {
        return this.mRadioCapability.get();
    }

    public void radioCapabilityUpdated(RadioCapability rc) {
        this.mRadioCapability.set(rc);
        if (SubscriptionManager.isValidSubscriptionId(getSubId())) {
            sendSubscriptionSettings(!this.mContext.getResources().getBoolean(17891616));
        }
    }

    public void sendSubscriptionSettings(boolean restoreNetworkSelection) {
        setPreferredNetworkType(PhoneFactory.calculatePreferredNetworkType(this.mContext, getSubId()), null);
        if (restoreNetworkSelection) {
            restoreSavedNetworkSelection(null);
        }
    }

    /* access modifiers changed from: protected */
    public void setPreferredNetworkTypeIfSimLoaded() {
        if (SubscriptionManager.isValidSubscriptionId(getSubId())) {
            setPreferredNetworkType(PhoneFactory.calculatePreferredNetworkType(this.mContext, getSubId()), null);
        }
    }

    public void registerForRadioCapabilityChanged(Handler h, int what, Object obj) {
        this.mCi.registerForRadioCapabilityChanged(h, what, obj);
    }

    public void unregisterForRadioCapabilityChanged(Handler h) {
        this.mCi.unregisterForRadioCapabilityChanged(this);
    }

    public boolean isImsUseEnabled() {
        ImsManager imsManager = ImsManager.getInstance(this.mContext, this.mPhoneId);
        return (imsManager.isVolteEnabledByPlatform() && imsManager.isEnhanced4gLteModeSettingEnabledByUser()) || (imsManager.isWfcEnabledByPlatform() && imsManager.isWfcEnabledByUser() && imsManager.isNonTtyOrTtyOnVolteEnabled());
    }

    public boolean isImsAvailable() {
        Phone phone = this.mImsPhone;
        if (phone == null) {
            return false;
        }
        return phone.isImsAvailable();
    }

    @UnsupportedAppUsage
    public boolean isVideoEnabled() {
        Phone imsPhone = this.mImsPhone;
        if (imsPhone != null) {
            return imsPhone.isVideoEnabled();
        }
        return false;
    }

    public int getLceStatus() {
        return this.mLceStatus;
    }

    public void getModemActivityInfo(Message response, WorkSource workSource) {
        this.mCi.getModemActivityInfo(response, workSource);
    }

    public void startLceAfterRadioIsAvailable() {
        this.mCi.startLceService(200, true, obtainMessage(37));
    }

    public void setAllowedCarriers(CarrierRestrictionRules carrierRestrictionRules, Message response, WorkSource workSource) {
        this.mCi.setAllowedCarriers(carrierRestrictionRules, response, workSource);
    }

    public void setSignalStrengthReportingCriteria(int[] thresholds, int ran) {
    }

    public void setLinkCapacityReportingCriteria(int[] dlThresholds, int[] ulThresholds, int ran) {
    }

    public void getAllowedCarriers(Message response, WorkSource workSource) {
        this.mCi.getAllowedCarriers(response, workSource);
    }

    public Locale getLocaleFromSimAndCarrierPrefs() {
        IccRecords records = this.mIccRecords.get();
        if (records == null || records.getSimLanguage() == null) {
            return getLocaleFromCarrierProperties(this.mContext);
        }
        return new Locale(records.getSimLanguage());
    }

    public void updateDataConnectionTracker() {
        TransportManager transportManager = this.mTransportManager;
        if (transportManager != null) {
            int[] availableTransports = transportManager.getAvailableTransports();
            for (int transport : availableTransports) {
                if (getDcTracker(transport) != null) {
                    getDcTracker(transport).update();
                }
            }
        }
    }

    public boolean updateCurrentCarrierInProvider() {
        return false;
    }

    public boolean areAllDataDisconnected() {
        TransportManager transportManager = this.mTransportManager;
        if (transportManager == null) {
            return true;
        }
        int[] availableTransports = transportManager.getAvailableTransports();
        for (int transport : availableTransports) {
            if (!(getDcTracker(transport) == null || getDcTracker(transport).isDisconnected())) {
                return false;
            }
        }
        return true;
    }

    public void registerForAllDataDisconnected(Handler h, int what) {
        this.mAllDataDisconnectedRegistrants.addUnique(h, what, (Object) null);
        TransportManager transportManager = this.mTransportManager;
        if (transportManager != null) {
            int[] availableTransports = transportManager.getAvailableTransports();
            for (int transport : availableTransports) {
                if (getDcTracker(transport) != null && !getDcTracker(transport).isDisconnected()) {
                    getDcTracker(transport).registerForAllDataDisconnected(this, 52);
                }
            }
        }
    }

    public void unregisterForAllDataDisconnected(Handler h) {
        this.mAllDataDisconnectedRegistrants.remove(h);
    }

    public DataEnabledSettings getDataEnabledSettings() {
        return this.mDataEnabledSettings;
    }

    @UnsupportedAppUsage
    public IccSmsInterfaceManager getIccSmsInterfaceManager() {
        return null;
    }

    /* access modifiers changed from: protected */
    public boolean isMatchGid(String gid) {
        String gid1 = getGroupIdLevel1();
        int gidLength = gid.length();
        if (TextUtils.isEmpty(gid1) || gid1.length() < gidLength || !gid1.substring(0, gidLength).equalsIgnoreCase(gid)) {
            return false;
        }
        return true;
    }

    public static void checkWfcWifiOnlyModeBeforeDial(Phone imsPhone, int phoneId, Context context) throws CallStateException {
        if (imsPhone == null || !imsPhone.isWifiCallingEnabled()) {
            ImsManager imsManager = ImsManager.getInstance(context, phoneId);
            if (imsManager.isWfcEnabledByPlatform() && imsManager.isWfcEnabledByUser() && imsManager.getWfcMode() == 0) {
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

    public void cancelUSSD(Message msg) {
    }

    public Phone getDefaultPhone() {
        return this;
    }

    public NetworkStats getVtDataUsage(boolean perUidStats) {
        Phone phone = this.mImsPhone;
        if (phone == null) {
            return null;
        }
        return phone.getVtDataUsage(perUidStats);
    }

    public Uri[] getCurrentSubscriberUris() {
        return null;
    }

    public AppSmsManager getAppSmsManager() {
        return this.mAppSmsManager;
    }

    public void setSimPowerState(int state, WorkSource workSource) {
        this.mCi.setSimCardPower(state, null, workSource);
    }

    public void setRadioIndicationUpdateMode(int filters, int mode) {
        DeviceStateMonitor deviceStateMonitor = this.mDeviceStateMonitor;
        if (deviceStateMonitor != null) {
            deviceStateMonitor.setIndicationUpdateMode(filters, mode);
        }
    }

    public void setCarrierTestOverride(String mccmnc, String imsi, String iccid, String gid1, String gid2, String pnn, String spn, String carrierPrivilegeRules, String apn) {
    }

    public DcTracker getDcTracker(int transportType) {
        return this.mDcTrackers.get(transportType);
    }

    public boolean isCdmaSubscriptionAppPresent() {
        return false;
    }

    public HalVersion getHalVersion() {
        CommandsInterface commandsInterface = this.mCi;
        if (commandsInterface == null || !(commandsInterface instanceof RIL)) {
            return RIL.RADIO_HAL_VERSION_UNKNOWN;
        }
        return ((RIL) commandsInterface).getHalVersion();
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("Phone: subId=" + getSubId());
        pw.println(" mPhoneId=" + this.mPhoneId);
        pw.println(" mCi=" + this.mCi);
        pw.println(" mDnsCheckDisabled=" + this.mDnsCheckDisabled);
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
        pw.println(" needsOtaServiceProvisioning=" + needsOtaServiceProvisioning());
        pw.println(" isInEmergencySmsMode=" + isInEmergencySmsMode());
        pw.flush();
        pw.println("++++++++++++++++++++++++++++++++");
        Phone phone = this.mImsPhone;
        if (phone != null) {
            try {
                phone.dump(fd, pw, args);
            } catch (Exception e) {
                e.printStackTrace();
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
        }
        TransportManager transportManager = this.mTransportManager;
        if (transportManager != null) {
            int[] availableTransports = transportManager.getAvailableTransports();
            for (int transport : availableTransports) {
                if (getDcTracker(transport) != null) {
                    getDcTracker(transport).dump(fd, pw, args);
                    pw.flush();
                    pw.println("++++++++++++++++++++++++++++++++");
                }
            }
        }
        if (getServiceStateTracker() != null) {
            try {
                getServiceStateTracker().dump(fd, pw, args);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
        }
        if (getEmergencyNumberTracker() != null) {
            try {
                getEmergencyNumberTracker().dump(fd, pw, args);
            } catch (Exception e3) {
                e3.printStackTrace();
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
        }
        CarrierResolver carrierResolver = this.mCarrierResolver;
        if (carrierResolver != null) {
            try {
                carrierResolver.dump(fd, pw, args);
            } catch (Exception e4) {
                e4.printStackTrace();
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
        }
        CarrierActionAgent carrierActionAgent = this.mCarrierActionAgent;
        if (carrierActionAgent != null) {
            try {
                carrierActionAgent.dump(fd, pw, args);
            } catch (Exception e5) {
                e5.printStackTrace();
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
        }
        CarrierSignalAgent carrierSignalAgent = this.mCarrierSignalAgent;
        if (carrierSignalAgent != null) {
            try {
                carrierSignalAgent.dump(fd, pw, args);
            } catch (Exception e6) {
                e6.printStackTrace();
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
        }
        if (getCallTracker() != null) {
            try {
                getCallTracker().dump(fd, pw, args);
            } catch (Exception e7) {
                e7.printStackTrace();
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
        }
        SimActivationTracker simActivationTracker = this.mSimActivationTracker;
        if (simActivationTracker != null) {
            try {
                simActivationTracker.dump(fd, pw, args);
            } catch (Exception e8) {
                e8.printStackTrace();
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
        }
        if (this.mDeviceStateMonitor != null) {
            pw.println("DeviceStateMonitor:");
            this.mDeviceStateMonitor.dump(fd, pw, args);
            pw.println("++++++++++++++++++++++++++++++++");
        }
        TransportManager transportManager2 = this.mTransportManager;
        if (transportManager2 != null) {
            transportManager2.dump(fd, pw, args);
        }
        CommandsInterface commandsInterface = this.mCi;
        if (commandsInterface != null && (commandsInterface instanceof RIL)) {
            try {
                ((RIL) commandsInterface).dump(fd, pw, args);
            } catch (Exception e9) {
                e9.printStackTrace();
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
        }
        pw.println("Phone Local Log: ");
        LocalLog localLog = this.mLocalLog;
        if (localLog != null) {
            try {
                localLog.dump(fd, pw, args);
            } catch (Exception e10) {
                e10.printStackTrace();
            }
            pw.flush();
            pw.println("++++++++++++++++++++++++++++++++");
        }
    }

    public String getOemSpn() {
        return getServiceStateTracker().getOemSpn();
    }

    public void oemMigrateFrom() {
        Phone phone = this.mImsPhone;
        if (phone != null) {
            oemMigrate(this.mHandoverRegistrants, phone.mHandoverRegistrants);
        }
    }

    public boolean hasHoRegistrants() {
        return this.mHandoverRegistrants.size() > 0;
    }

    @Override // com.android.internal.telephony.AbstractPhone
    public DeviceStateMonitor getDeviceStateMonitor() {
        return this.mDeviceStateMonitor;
    }

    public boolean oppoIsInDelayOOSState() {
        ServiceStateTracker sst = getServiceStateTracker();
        if (sst != null) {
            return sst.oppoIsInDelayOOSState();
        }
        return false;
    }

    @Override // com.android.internal.telephony.AbstractPhone
    public void oppoSimlockReq(String[] AtCmd, Message response) {
        invokeOemRilRequestStrings(AtCmd, response);
    }

    @Override // com.android.internal.telephony.AbstractPhone
    public void registerForSimlockState(Handler h, int what, Object obj) {
        this.mCi.registerForSmlDeviceLockInfoChanged(h, what, obj);
    }

    @Override // com.android.internal.telephony.AbstractPhone
    public void unregisterForSimlockState(Handler h) {
        this.mCi.unregisterForSmlDeviceLockInfoChanged(h);
    }
}
