package com.android.server.location;

import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.location.GeofenceHardwareImpl;
import android.location.FusedBatchOptions;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.IGpsGeofenceHardware;
import android.location.INetInitiatedListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.PowerSaveState;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.WorkSource;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;
import android.util.StatsLog;
import android.util.TimeUtils;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.IBatteryStats;
import com.android.internal.location.GpsNetInitiatedHandler;
import com.android.internal.location.ProviderProperties;
import com.android.internal.location.ProviderRequest;
import com.android.internal.location.gnssmetrics.GnssMetrics;
import com.android.server.PswServiceFactory;
import com.android.server.UiModeManagerService;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.display.OppoBrightUtils;
import com.android.server.location.AbstractLocationProvider;
import com.android.server.location.GnssLocationProvider;
import com.android.server.location.GnssNetworkConnectivityHandler;
import com.android.server.location.GnssSatelliteBlacklistHelper;
import com.android.server.location.NtpTimeHelper;
import com.android.server.location.interfaces.IOppoGnssLocationProvider;
import com.android.server.location.interfaces.IPswGnssDiagnosticTool;
import com.android.server.location.interfaces.IPswLocationStatistics;
import com.android.server.location.interfaces.IPswNavigationStatusController;
import com.android.server.location.interfaces.IPswOppoGnssWhiteListProxy;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GnssLocationProvider extends AbstractLocationProvider implements IOppoGnssLocationProvider, NtpTimeHelper.InjectNtpTimeCallback, GnssSatelliteBlacklistHelper.GnssSatelliteBlacklistCallback {
    private static final String ACTION_MTKLOGGER_STATE_CHANGED = "com.debug.loggerui.intent.action.LOG_STATE_CHANGED";
    private static final int AGPS_REF_LOCATION_TYPE_GSM_CELLID = 1;
    private static final int AGPS_REF_LOCATION_TYPE_UMTS_CELLID = 2;
    private static final int AGPS_RIL_REQUEST_SETID_IMSI = 1;
    private static final int AGPS_RIL_REQUEST_SETID_MSISDN = 2;
    private static final int AGPS_SETID_TYPE_IMSI = 1;
    private static final int AGPS_SETID_TYPE_MSISDN = 2;
    private static final int AGPS_SETID_TYPE_NONE = 0;
    private static final int AGPS_SUPL_MODE_MSA = 2;
    private static final int AGPS_SUPL_MODE_MSB = 1;
    private static final String ALARM_TIMEOUT = "com.android.internal.location.ALARM_TIMEOUT";
    private static final String ALARM_WAKEUP = "com.android.internal.location.ALARM_WAKEUP";
    /* access modifiers changed from: private */
    public static boolean DEBUG = (!IS_USER_BUILD || Log.isLoggable("GnssLocationProvider", 3) || FORCE_DEBUG);
    private static final String DOWNLOAD_EXTRA_WAKELOCK_KEY = "GnssLocationProviderPsdsDownload";
    private static final int DOWNLOAD_PSDS_DATA = 6;
    private static final int DOWNLOAD_PSDS_DATA_FINISHED = 11;
    private static final long DOWNLOAD_PSDS_DATA_TIMEOUT_MS = 60000;
    private static final int DUMP_DISABLE_GNSS_POWER_SAVE = 1;
    private static final int DUMP_ENABLE_GNSS_POWER_SAVE = 2;
    private static final int ELAPSED_REALTIME_HAS_TIMESTAMP_NS = 1;
    private static final int ELAPSED_REALTIME_HAS_TIME_UNCERTAINTY_NS = 2;
    private static final int EMERGENCY_LOCATION_UPDATE_DURATION_MULTIPLIER = 3;
    public static final boolean FORCE_DEBUG = (SystemProperties.getInt(PROP_FORCE_DEBUG_KEY, 0) == 1);
    public static final int GPS_CAPABILITY_GEOFENCING = 32;
    public static final int GPS_CAPABILITY_LOW_POWER_MODE = 256;
    public static final int GPS_CAPABILITY_MEASUREMENTS = 64;
    public static final int GPS_CAPABILITY_MEASUREMENT_CORRECTIONS = 1024;
    private static final int GPS_CAPABILITY_MSA = 4;
    private static final int GPS_CAPABILITY_MSB = 2;
    public static final int GPS_CAPABILITY_NAV_MESSAGES = 128;
    private static final int GPS_CAPABILITY_ON_DEMAND_TIME = 16;
    public static final int GPS_CAPABILITY_SATELLITE_BLACKLIST = 512;
    private static final int GPS_CAPABILITY_SCHEDULING = 1;
    private static final int GPS_CAPABILITY_SINGLE_SHOT = 8;
    private static final int GPS_DELETE_ALL = 65535;
    private static final int GPS_DELETE_ALMANAC = 2;
    private static final int GPS_DELETE_CELLDB_INFO = 32768;
    private static final int GPS_DELETE_EPHEMERIS = 1;
    private static final int GPS_DELETE_EPO = 16384;
    private static final int GPS_DELETE_HEALTH = 64;
    private static final int GPS_DELETE_HOT_STILL = 8192;
    private static final int GPS_DELETE_IONO = 16;
    private static final int GPS_DELETE_POSITION = 4;
    private static final int GPS_DELETE_RTI = 1024;
    private static final int GPS_DELETE_SADATA = 512;
    private static final int GPS_DELETE_SVDIR = 128;
    private static final int GPS_DELETE_SVSTEER = 256;
    private static final int GPS_DELETE_TIME = 8;
    private static final int GPS_DELETE_UTC = 32;
    private static final int GPS_GEOFENCE_AVAILABLE = 2;
    private static final int GPS_GEOFENCE_ERROR_GENERIC = -149;
    private static final int GPS_GEOFENCE_ERROR_ID_EXISTS = -101;
    private static final int GPS_GEOFENCE_ERROR_ID_UNKNOWN = -102;
    private static final int GPS_GEOFENCE_ERROR_INVALID_TRANSITION = -103;
    private static final int GPS_GEOFENCE_ERROR_TOO_MANY_GEOFENCES = 100;
    private static final int GPS_GEOFENCE_OPERATION_SUCCESS = 0;
    private static final int GPS_GEOFENCE_UNAVAILABLE = 1;
    private static final int GPS_POLLING_THRESHOLD_INTERVAL = 10000;
    private static final int GPS_POSITION_MODE_MS_ASSISTED = 2;
    private static final int GPS_POSITION_MODE_MS_BASED = 1;
    private static final int GPS_POSITION_MODE_STANDALONE = 0;
    private static final int GPS_POSITION_RECURRENCE_PERIODIC = 0;
    private static final int GPS_POSITION_RECURRENCE_SINGLE = 1;
    private static final int GPS_START_LOG = 2048;
    private static final int GPS_STATUS_ENGINE_OFF = 4;
    private static final int GPS_STATUS_ENGINE_ON = 3;
    private static final int GPS_STATUS_NONE = 0;
    private static final int GPS_STATUS_SESSION_BEGIN = 1;
    private static final int GPS_STATUS_SESSION_END = 2;
    private static final int GPS_STOP_LOG = 4096;
    private static final int INITIALIZE_HANDLER = 13;
    private static final int INJECT_NTP_TIME = 5;
    private static final boolean IS_USER_BUILD = ("user".equals(Build.TYPE) || "userdebug".equals(Build.TYPE));
    private static final float ITAR_SPEED_LIMIT_METERS_PER_SECOND = 400.0f;
    private static final int LAST_LOCATION_EXPIRED_TIMEOUT = 600000;
    private static final int LOCATION_HAS_ALTITUDE = 2;
    private static final int LOCATION_HAS_BEARING = 8;
    private static final int LOCATION_HAS_BEARING_ACCURACY = 128;
    private static final int LOCATION_HAS_HORIZONTAL_ACCURACY = 16;
    private static final int LOCATION_HAS_LAT_LONG = 1;
    private static final int LOCATION_HAS_SPEED = 4;
    private static final int LOCATION_HAS_SPEED_ACCURACY = 64;
    private static final int LOCATION_HAS_VERTICAL_ACCURACY = 32;
    private static final int LOCATION_INVALID = 0;
    private static final int LOCATION_MIN_SV_COUNT = 2;
    private static final long LOCATION_OFF_DELAY_THRESHOLD_ERROR_MILLIS = 15000;
    private static final long LOCATION_OFF_DELAY_THRESHOLD_WARN_MILLIS = 2000;
    private static final long LOCATION_UPDATE_DURATION_MILLIS = 10000;
    private static final long LOCATION_UPDATE_MIN_TIME_INTERVAL_MILLIS = 1000;
    private static final long MAX_RETRY_INTERVAL = 14400000;
    private static final String MTKLOGGER_MOBILELOG_DEBUG_PROPERTY = "vendor.MB.running";
    private static final String MTK_DEBUG_GPSDBLOG_ENABLE_PROPERTY = "vendor.gpsdbglog.enable";
    private static final int NO_FIX_TIMEOUT = 60000;
    private static final ProviderProperties PROPERTIES = new ProviderProperties(true, true, false, false, true, true, true, 3, 1);
    private static final String PROP_FORCE_DEBUG_KEY = "persist.vendor.log.tel_dbg";
    private static final long RECENT_FIX_TIMEOUT = 10000;
    private static final int REPORT_LOCATION = 17;
    private static final int REPORT_SV_STATUS = 18;
    private static final int REQUEST_LOCATION = 16;
    private static final long RETRY_INTERVAL = 300000;
    private static final int SET_REQUEST = 3;
    private static final int STATE_DOWNLOADING = 1;
    private static final int STATE_IDLE = 2;
    private static final int STATE_PENDING_NETWORK = 0;
    private static final String TAG = "GnssLocationProvider";
    private static final int TCP_MAX_PORT = 65535;
    private static final int TCP_MIN_PORT = 0;
    private static final int UPDATE_LOCATION = 7;
    private static boolean VERBOSE = false;
    private static final String WAKELOCK_KEY = "GnssLocationProvider";
    private final AlarmManager mAlarmManager;
    private final AppOpsManager mAppOps;
    private final IBatteryStats mBatteryStats;
    /* access modifiers changed from: private */
    public final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.location.GnssLocationProvider.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (GnssLocationProvider.DEBUG) {
                Log.d("GnssLocationProvider", "receive broadcast intent, action: " + action);
            }
            if (action != null) {
                char c = 65535;
                switch (action.hashCode()) {
                    case -2128145023:
                        if (action.equals("android.intent.action.SCREEN_OFF")) {
                            c = 4;
                            break;
                        }
                        break;
                    case -1992416737:
                        if (action.equals(GnssLocationProvider.ALARM_TIMEOUT)) {
                            c = 1;
                            break;
                        }
                        break;
                    case -1454123155:
                        if (action.equals("android.intent.action.SCREEN_ON")) {
                            c = 5;
                            break;
                        }
                        break;
                    case -1138588223:
                        if (action.equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                            c = 6;
                            break;
                        }
                        break;
                    case -678568287:
                        if (action.equals(GnssLocationProvider.ALARM_WAKEUP)) {
                            c = 0;
                            break;
                        }
                        break;
                    case -320665661:
                        if (action.equals(GnssLocationProvider.ACTION_MTKLOGGER_STATE_CHANGED)) {
                            c = 8;
                            break;
                        }
                        break;
                    case -25388475:
                        if (action.equals("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED")) {
                            c = 7;
                            break;
                        }
                        break;
                    case 870701415:
                        if (action.equals("android.os.action.DEVICE_IDLE_MODE_CHANGED")) {
                            c = 3;
                            break;
                        }
                        break;
                    case 1779291251:
                        if (action.equals("android.os.action.POWER_SAVE_MODE_CHANGED")) {
                            c = 2;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                        GnssLocationProvider.this.startNavigating();
                        return;
                    case 1:
                        GnssLocationProvider.this.hibernate();
                        return;
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        GnssLocationProvider.this.updateLowPowerMode();
                        return;
                    case 6:
                    case 7:
                        GnssLocationProvider.this.subscriptionOrCarrierConfigChanged(context);
                        return;
                    case 8:
                        Log.d("GnssLocationProvider", "GNSS location provider get the action : com.debug.loggerui.intent.action.LOG_STATE_CHANGED");
                        GnssLocationProvider.this.enableVerboseLogging();
                        return;
                    default:
                        return;
                }
            }
        }
    };
    private String mC2KServerHost;
    private int mC2KServerPort;
    private WorkSource mClientSource = new WorkSource();
    private boolean mDisableGpsForPowerManager = false;
    /* access modifiers changed from: private */
    public int mDownloadPsdsDataPending = 0;
    @GuardedBy({"mLock"})
    private final PowerManager.WakeLock mDownloadPsdsWakeLock;
    private int mFixInterval = 1000;
    private long mFixRequestTime = 0;
    private final LocationChangeListener mFusedLocationListener = new FusedLocationListener();
    private GeofenceHardwareImpl mGeofenceHardwareImpl;
    private final GnssBatchingProvider mGnssBatchingProvider;
    private final GnssCapabilitiesProvider mGnssCapabilitiesProvider;
    private GnssConfiguration mGnssConfiguration;
    private final GnssGeofenceProvider mGnssGeofenceProvider;
    private final GnssMeasurementCorrectionsProvider mGnssMeasurementCorrectionsProvider;
    private final GnssMeasurementsProvider mGnssMeasurementsProvider;
    private GnssMetrics mGnssMetrics;
    private final GnssNavigationMessageProvider mGnssNavigationMessageProvider;
    private final GnssStatusListenerHelper mGnssStatusListenerHelper;
    /* access modifiers changed from: private */
    public GnssVisibilityControl mGnssVisibilityControl;
    @GuardedBy({"mLock"})
    private boolean mGpsEnabled;
    private Handler mHandler;
    /* access modifiers changed from: private */
    public volatile String mHardwareModelName;
    /* access modifiers changed from: private */
    public volatile int mHardwareYear = 0;
    private volatile boolean mItarSpeedLimitExceeded = false;
    private long mLastFixTime;
    private GnssPositionMode mLastPositionMode;
    private final LocationExtras mLocationExtras = new LocationExtras();
    private final Object mLock = new Object();
    /* access modifiers changed from: private */
    public final Looper mLooper;
    private boolean mLowPowerMode = false;
    private Object mMtkGnssProvider = null;
    private Class<?> mMtkGnssProviderClass = null;
    /* access modifiers changed from: private */
    public final GpsNetInitiatedHandler mNIHandler;
    private boolean mNavigating;
    /* access modifiers changed from: private */
    public IPswNavigationStatusController mNavigationController = null;
    private final INetInitiatedListener mNetInitiatedListener = new INetInitiatedListener.Stub() {
        /* class com.android.server.location.GnssLocationProvider.AnonymousClass8 */

        public boolean sendNiResponse(int notificationId, int userResponse) {
            if (GnssLocationProvider.DEBUG) {
                Log.d("GnssLocationProvider", "sendNiResponse, notifId: " + notificationId + ", response: " + userResponse);
            }
            GnssLocationProvider.this.native_send_ni_response(notificationId, userResponse);
            StatsLog.write(124, 2, notificationId, 0, false, false, false, 0, 0, null, null, 0, 0, GnssLocationProvider.this.mSuplEsEnabled, GnssLocationProvider.this.isGpsEnabled(), userResponse);
            return true;
        }
    };
    /* access modifiers changed from: private */
    public final GnssNetworkConnectivityHandler mNetworkConnectivityHandler;
    private final LocationChangeListener mNetworkLocationListener = new NetworkLocationListener();
    private byte[] mNmeaBuffer = new byte[120];
    /* access modifiers changed from: private */
    public final NtpTimeHelper mNtpTimeHelper;
    /* access modifiers changed from: private */
    public IPswLocationStatistics mOppoLocationStatistics = null;
    private int mPositionMode;
    private final PowerManager mPowerManager;
    private ProviderRequest mProviderRequest;
    private final ExponentialBackOff mPsdsBackOff = new ExponentialBackOff(300000, 14400000);
    /* access modifiers changed from: private */
    public boolean mShutdown;
    private boolean mStarted;
    private long mStartedChangedElapsedRealtime;
    private int mStatus = 1;
    private long mStatusUpdateTime = SystemClock.elapsedRealtime();
    /* access modifiers changed from: private */
    public boolean mSuplEsEnabled = false;
    private String mSuplServerHost;
    private int mSuplServerPort = 0;
    private boolean mSupportsPsds;
    private int mTimeToFirstFix = 0;
    private final PendingIntent mTimeoutIntent;
    private volatile int mTopHalCapabilities;
    /* access modifiers changed from: private */
    public final PowerManager.WakeLock mWakeLock;
    private final PendingIntent mWakeupIntent;
    private WorkSource mWorkSource = null;

    public interface GnssMetricsProvider {
        String getGnssMetricsAsProtoString();
    }

    public interface GnssSystemInfoProvider {
        String getGnssHardwareModelName();

        int getGnssYearOfHardware();
    }

    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.SOURCE)
    private @interface NativeEntryPoint {
    }

    private static native void class_init_native();

    private native void native_agps_ni_message(byte[] bArr, int i);

    private native void native_agps_set_id(int i, String str);

    private native void native_agps_set_ref_location_cellid(int i, int i2, int i3, int i4, int i5);

    private native void native_cleanup();

    private native void native_delete_aiding_data(int i);

    private native String native_get_internal_state();

    private native boolean native_init();

    private static native void native_init_once(boolean z);

    private native void native_inject_best_location(int i, double d, double d2, double d3, float f, float f2, float f3, float f4, float f5, float f6, long j, int i2, long j2, double d4);

    private native void native_inject_location(double d, double d2, float f);

    private native void native_inject_psds_data(byte[] bArr, int i);

    private native void native_inject_time(long j, long j2, int i);

    /* access modifiers changed from: private */
    public static native boolean native_is_gnss_visibility_control_supported();

    private static native boolean native_is_supported();

    private native int native_read_nmea(byte[] bArr, int i);

    /* access modifiers changed from: private */
    public native void native_send_ni_response(int i, int i2);

    private native void native_set_agps_server(int i, String str, int i2);

    private native boolean native_set_position_mode(int i, int i2, int i3, int i4, int i5, boolean z);

    private native boolean native_start();

    private native boolean native_stop();

    private native boolean native_supports_psds();

    static {
        boolean z = true;
        if (IS_USER_BUILD && !Log.isLoggable("GnssLocationProvider", 2) && !FORCE_DEBUG) {
            z = false;
        }
        VERBOSE = z;
        class_init_native();
    }

    private static class GpsRequest {
        public ProviderRequest request;
        public WorkSource source;

        public GpsRequest(ProviderRequest request2, WorkSource source2) {
            this.request = request2;
            this.source = source2;
        }
    }

    private static class LocationExtras {
        private final Bundle mBundle = new Bundle();
        private int mMaxCn0;
        private int mMeanCn0;
        private int mSvCount;

        public void set(int svCount, int meanCn0, int maxCn0) {
            synchronized (this) {
                this.mSvCount = svCount;
                this.mMeanCn0 = meanCn0;
                this.mMaxCn0 = maxCn0;
            }
            setBundle(this.mBundle);
        }

        public void reset() {
            set(0, 0, 0);
        }

        public void setBundle(Bundle extras) {
            if (extras != null) {
                synchronized (this) {
                    extras.putInt("satellites", this.mSvCount);
                    extras.putInt("meanCn0", this.mMeanCn0);
                    extras.putInt("maxCn0", this.mMaxCn0);
                }
            }
        }

        public Bundle getBundle() {
            Bundle bundle;
            synchronized (this) {
                bundle = new Bundle(this.mBundle);
            }
            return bundle;
        }
    }

    public GnssStatusListenerHelper getGnssStatusProvider() {
        return this.mGnssStatusListenerHelper;
    }

    public IGpsGeofenceHardware getGpsGeofenceProxy() {
        return this.mGnssGeofenceProvider;
    }

    public GnssMeasurementsProvider getGnssMeasurementsProvider() {
        return this.mGnssMeasurementsProvider;
    }

    public GnssMeasurementCorrectionsProvider getGnssMeasurementCorrectionsProvider() {
        return this.mGnssMeasurementCorrectionsProvider;
    }

    public GnssNavigationMessageProvider getGnssNavigationMessageProvider() {
        return this.mGnssNavigationMessageProvider;
    }

    public /* synthetic */ void lambda$onUpdateSatelliteBlacklist$0$GnssLocationProvider(int[] constellations, int[] svids) {
        this.mGnssConfiguration.setSatelliteBlacklist(constellations, svids);
    }

    @Override // com.android.server.location.GnssSatelliteBlacklistHelper.GnssSatelliteBlacklistCallback
    public void onUpdateSatelliteBlacklist(int[] constellations, int[] svids) {
        this.mHandler.post(new Runnable(constellations, svids) {
            /* class com.android.server.location.$$Lambda$GnssLocationProvider$WbIUWqWbiKrZx6NHwSpsFU1pHKI */
            private final /* synthetic */ int[] f$1;
            private final /* synthetic */ int[] f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                GnssLocationProvider.this.lambda$onUpdateSatelliteBlacklist$0$GnssLocationProvider(this.f$1, this.f$2);
            }
        });
        this.mGnssMetrics.resetConstellationTypes();
    }

    /* access modifiers changed from: private */
    public void subscriptionOrCarrierConfigChanged(Context context) {
        if (DEBUG) {
            Log.d("GnssLocationProvider", "received SIM related action: ");
        }
        TelephonyManager phone = (TelephonyManager) this.mContext.getSystemService("phone");
        CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        int ddSubId = SubscriptionManager.getDefaultDataSubscriptionId();
        String mccMnc = SubscriptionManager.isValidSubscriptionId(ddSubId) ? phone.getSimOperator(ddSubId) : phone.getSimOperator();
        boolean isKeepLppProfile = false;
        if (!TextUtils.isEmpty(mccMnc)) {
            if (DEBUG) {
                Log.d("GnssLocationProvider", "SIM MCC/MNC is available: " + mccMnc);
            }
            if (configManager != null) {
                PersistableBundle b = SubscriptionManager.isValidSubscriptionId(ddSubId) ? configManager.getConfigForSubId(ddSubId) : null;
                if (b != null) {
                    isKeepLppProfile = b.getBoolean("gps.persist_lpp_mode_bool");
                }
            }
            if (isKeepLppProfile) {
                this.mGnssConfiguration.loadPropertiesFromCarrierConfig();
                String lpp_profile = this.mGnssConfiguration.getLppProfile();
                if (lpp_profile != null) {
                    SystemProperties.set("persist.sys.gps.lpp", lpp_profile);
                }
            } else {
                SystemProperties.set("persist.sys.gps.lpp", "");
            }
            reloadGpsProperties();
        } else if (DEBUG) {
            Log.d("GnssLocationProvider", "SIM MCC/MNC is still not available");
        }
    }

    /* access modifiers changed from: private */
    public void updateLowPowerMode() {
        boolean disableGpsForPowerManager = this.mPowerManager.isDeviceIdleMode();
        PowerSaveState result = this.mPowerManager.getPowerSaveState(1);
        int i = result.locationMode;
        if (i == 1 || i == 2) {
            disableGpsForPowerManager |= result.batterySaverEnabled && !this.mPowerManager.isInteractive();
        }
        if (disableGpsForPowerManager) {
            disableGpsForPowerManager = true ^ PswServiceFactory.getInstance().getFeature(IPswOppoGnssWhiteListProxy.DEFAULT, new Object[]{this.mContext}).isLocationInteractive();
        }
        if (disableGpsForPowerManager != this.mDisableGpsForPowerManager) {
            this.mDisableGpsForPowerManager = disableGpsForPowerManager;
            Log.d("GnssLocationProvider", "updateLowPowerMode triggered gps status update mDisableGpsForPowerManager=" + this.mDisableGpsForPowerManager);
            updateEnabled();
            updateRequirements();
        }
    }

    public static boolean isSupported() {
        return native_is_supported();
    }

    /* access modifiers changed from: private */
    public void reloadGpsProperties() {
        this.mGnssConfiguration.reloadGpsProperties();
        setSuplHostPort();
        this.mC2KServerHost = this.mGnssConfiguration.getC2KHost();
        boolean z = false;
        this.mC2KServerPort = this.mGnssConfiguration.getC2KPort(0);
        this.mNIHandler.setEmergencyExtensionSeconds(this.mGnssConfiguration.getEsExtensionSec());
        if (this.mGnssConfiguration.getSuplEs(0) == 1) {
            z = true;
        }
        this.mSuplEsEnabled = z;
        this.mNIHandler.setSuplEsEnabled(this.mSuplEsEnabled);
        GnssVisibilityControl gnssVisibilityControl = this.mGnssVisibilityControl;
        if (gnssVisibilityControl != null) {
            gnssVisibilityControl.onConfigurationUpdated(this.mGnssConfiguration);
        }
    }

    public GnssLocationProvider(Context context, AbstractLocationProvider.LocationProviderManager locationProviderManager, Looper looper) {
        super(context, locationProviderManager);
        this.mLooper = looper;
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mWakeLock = this.mPowerManager.newWakeLock(1, "GnssLocationProvider");
        this.mWakeLock.setReferenceCounted(true);
        this.mDownloadPsdsWakeLock = this.mPowerManager.newWakeLock(1, DOWNLOAD_EXTRA_WAKELOCK_KEY);
        this.mDownloadPsdsWakeLock.setReferenceCounted(true);
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mWakeupIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ALARM_WAKEUP), 0);
        this.mTimeoutIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ALARM_TIMEOUT), 0);
        this.mNetworkConnectivityHandler = new GnssNetworkConnectivityHandler(context, new GnssNetworkConnectivityHandler.GnssNetworkListener() {
            /* class com.android.server.location.$$Lambda$GnssLocationProvider$Q6M8z_ZBiD7BNs3kvNmVrqoHSng */

            @Override // com.android.server.location.GnssNetworkConnectivityHandler.GnssNetworkListener
            public final void onNetworkAvailable() {
                GnssLocationProvider.this.onNetworkAvailable();
            }
        }, looper);
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
        this.mBatteryStats = IBatteryStats.Stub.asInterface(ServiceManager.getService("batterystats"));
        this.mHandler = new ProviderHandler(looper);
        this.mGnssConfiguration = new GnssConfiguration(this.mContext);
        this.mGnssCapabilitiesProvider = new GnssCapabilitiesProvider();
        this.mNIHandler = new GpsNetInitiatedHandler(context, this.mNetInitiatedListener, this.mSuplEsEnabled);
        this.mOppoLocationStatistics = PswServiceFactory.getInstance().getFeature(IPswLocationStatistics.DEFAULT, new Object[0]);
        sendMessage(13, 0, null);
        this.mGnssStatusListenerHelper = new GnssStatusListenerHelper(this.mContext, this.mHandler) {
            /* class com.android.server.location.GnssLocationProvider.AnonymousClass2 */

            /* access modifiers changed from: protected */
            @Override // com.android.server.location.RemoteListenerHelper
            public boolean isAvailableInPlatform() {
                return GnssLocationProvider.isSupported();
            }

            /* access modifiers changed from: protected */
            @Override // com.android.server.location.RemoteListenerHelper
            public boolean isGpsEnabled() {
                return GnssLocationProvider.this.isGpsEnabled();
            }
        };
        this.mGnssMeasurementsProvider = new GnssMeasurementsProvider(this.mContext, this.mHandler) {
            /* class com.android.server.location.GnssLocationProvider.AnonymousClass3 */

            /* access modifiers changed from: protected */
            @Override // com.android.server.location.RemoteListenerHelper
            public boolean isGpsEnabled() {
                return GnssLocationProvider.this.isGpsEnabled();
            }
        };
        this.mGnssMeasurementCorrectionsProvider = new GnssMeasurementCorrectionsProvider(this.mHandler);
        this.mGnssNavigationMessageProvider = new GnssNavigationMessageProvider(this.mContext, this.mHandler) {
            /* class com.android.server.location.GnssLocationProvider.AnonymousClass4 */

            /* access modifiers changed from: protected */
            @Override // com.android.server.location.RemoteListenerHelper
            public boolean isGpsEnabled() {
                return GnssLocationProvider.this.isGpsEnabled();
            }
        };
        this.mGnssMetrics = new GnssMetrics(this.mBatteryStats);
        this.mNtpTimeHelper = new NtpTimeHelper(this.mContext, looper, this);
        GnssSatelliteBlacklistHelper gnssSatelliteBlacklistHelper = new GnssSatelliteBlacklistHelper(this.mContext, looper, this);
        Handler handler = this.mHandler;
        Objects.requireNonNull(gnssSatelliteBlacklistHelper);
        handler.post(new Runnable() {
            /* class com.android.server.location.$$Lambda$5U_NhZgxqnYDZhpyacq4qBxh8k */

            public final void run() {
                GnssSatelliteBlacklistHelper.this.updateSatelliteBlacklist();
            }
        });
        this.mGnssBatchingProvider = new GnssBatchingProvider();
        this.mGnssGeofenceProvider = new GnssGeofenceProvider();
        this.mContext.registerReceiverAsUser(new BroadcastReceiver() {
            /* class com.android.server.location.GnssLocationProvider.AnonymousClass5 */

            public void onReceive(Context context, Intent intent) {
                if (getSendingUserId() == -1) {
                    boolean unused = GnssLocationProvider.this.mShutdown = true;
                    GnssLocationProvider.this.updateEnabled();
                }
            }
        }, UserHandle.ALL, new IntentFilter("android.intent.action.ACTION_SHUTDOWN"), null, this.mHandler);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("location_mode"), true, new ContentObserver(this.mHandler) {
            /* class com.android.server.location.GnssLocationProvider.AnonymousClass6 */

            public void onChange(boolean selfChange) {
                GnssLocationProvider.this.updateEnabled();
            }
        }, -1);
        setProperties(PROPERTIES);
        setEnabled(true);
        enableVerboseLogging();
        initMtkGnssLocProvider();
        this.mNavigationController = PswServiceFactory.getInstance().getFeature(IPswNavigationStatusController.DEFAULT, new Object[]{this.mContext, this});
        this.mNavigationController.setDebug(DEBUG);
    }

    @Override // com.android.server.location.NtpTimeHelper.InjectNtpTimeCallback
    public void injectTime(long time, long timeReference, int uncertainty) {
        native_inject_time(time, timeReference, uncertainty);
    }

    /* access modifiers changed from: private */
    public void onNetworkAvailable() {
        this.mNtpTimeHelper.onNetworkAvailable();
        if (this.mDownloadPsdsDataPending == 0 && this.mSupportsPsds) {
            psdsDownloadRequest();
        }
    }

    /* access modifiers changed from: private */
    public void handleRequestLocation(boolean independentFromGnss, boolean isUserEmergency) {
        LocationChangeListener locationListener;
        String provider;
        if (!isRequestLocationRateLimited()) {
            long durationMillis = Settings.Global.getLong(this.mContext.getContentResolver(), "gnss_hal_location_request_duration_millis", 10000);
            if (durationMillis == 0) {
                Log.i("GnssLocationProvider", "GNSS HAL location request is disabled by Settings.");
                return;
            }
            LocationManager locationManager = (LocationManager) this.mContext.getSystemService("location");
            LocationRequest locationRequest = new LocationRequest().setInterval(1000).setFastestInterval(1000);
            if (independentFromGnss) {
                provider = "network";
                locationListener = this.mNetworkLocationListener;
                locationRequest.setQuality(201);
                mtkInjectLastKnownLocation();
            } else {
                provider = "fused";
                locationListener = this.mFusedLocationListener;
                locationRequest.setQuality(100);
            }
            locationRequest.setProvider(provider);
            if (isUserEmergency && this.mNIHandler.getInEmergency()) {
                locationRequest.setLocationSettingsIgnored(true);
                durationMillis *= 3;
            }
            Log.i("GnssLocationProvider", String.format("GNSS HAL Requesting location updates from %s provider for %d millis.", provider, Long.valueOf(durationMillis)));
            try {
                locationManager.requestLocationUpdates(locationRequest, locationListener, this.mHandler.getLooper());
                LocationChangeListener.access$1108(locationListener);
                this.mHandler.postDelayed(new Runnable(provider, locationManager) {
                    /* class com.android.server.location.$$Lambda$GnssLocationProvider$oV78CWPlpzb195CgVgv_YipNWw */
                    private final /* synthetic */ String f$1;
                    private final /* synthetic */ LocationManager f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void run() {
                        GnssLocationProvider.lambda$handleRequestLocation$1(GnssLocationProvider.LocationChangeListener.this, this.f$1, this.f$2);
                    }
                }, durationMillis);
            } catch (IllegalArgumentException e) {
                Log.w("GnssLocationProvider", "Unable to request location.", e);
            }
        } else if (DEBUG) {
            Log.d("GnssLocationProvider", "RequestLocation is denied due to too frequent requests.");
        }
    }

    static /* synthetic */ void lambda$handleRequestLocation$1(LocationChangeListener locationListener, String provider, LocationManager locationManager) {
        if (LocationChangeListener.access$1106(locationListener) == 0) {
            Log.i("GnssLocationProvider", String.format("Removing location updates from %s provider.", provider));
            locationManager.removeUpdates(locationListener);
        }
    }

    /* access modifiers changed from: private */
    public void injectBestLocation(Location location) {
        if (DEBUG) {
            Log.d("GnssLocationProvider", "injectBestLocation: " + location);
        }
        int i = 2;
        int gnssLocationFlags = (location.hasAltitude() ? 2 : 0) | 1 | (location.hasSpeed() ? 4 : 0) | (location.hasBearing() ? 8 : 0) | (location.hasAccuracy() ? 16 : 0) | (location.hasVerticalAccuracy() ? 32 : 0) | (location.hasSpeedAccuracy() ? 64 : 0) | (location.hasBearingAccuracy() ? 128 : 0);
        double latitudeDegrees = location.getLatitude();
        double longitudeDegrees = location.getLongitude();
        double altitudeMeters = location.getAltitude();
        float speedMetersPerSec = location.getSpeed();
        float bearingDegrees = location.getBearing();
        float horizontalAccuracyMeters = location.getAccuracy();
        float verticalAccuracyMeters = location.getVerticalAccuracyMeters();
        float speedAccuracyMetersPerSecond = location.getSpeedAccuracyMetersPerSecond();
        float bearingAccuracyDegrees = location.getBearingAccuracyDegrees();
        long timestamp = location.getTime();
        if (!location.hasElapsedRealtimeUncertaintyNanos()) {
            i = 0;
        }
        native_inject_best_location(gnssLocationFlags, latitudeDegrees, longitudeDegrees, altitudeMeters, speedMetersPerSec, bearingDegrees, horizontalAccuracyMeters, verticalAccuracyMeters, speedAccuracyMetersPerSecond, bearingAccuracyDegrees, timestamp, i | 1, location.getElapsedRealtimeNanos(), location.getElapsedRealtimeUncertaintyNanos());
    }

    private boolean isRequestLocationRateLimited() {
        return false;
    }

    /* access modifiers changed from: private */
    public void handleDownloadPsdsData() {
        if (!this.mSupportsPsds) {
            Log.d("GnssLocationProvider", "handleDownloadPsdsData() called when PSDS not supported");
        } else if (this.mDownloadPsdsDataPending != 1) {
            if (!this.mNetworkConnectivityHandler.isDataNetworkConnected()) {
                this.mDownloadPsdsDataPending = 0;
                return;
            }
            this.mDownloadPsdsDataPending = 1;
            synchronized (this.mLock) {
                this.mDownloadPsdsWakeLock.acquire(60000);
            }
            Log.i("GnssLocationProvider", "WakeLock acquired by handleDownloadPsdsData()");
            AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                /* class com.android.server.location.$$Lambda$GnssLocationProvider$psQkGhDUF5E1xdXdW4u299tSPsA */

                public final void run() {
                    GnssLocationProvider.this.lambda$handleDownloadPsdsData$2$GnssLocationProvider();
                }
            });
        }
    }

    public /* synthetic */ void lambda$handleDownloadPsdsData$2$GnssLocationProvider() {
        byte[] data = new GpsPsdsDownloader(this.mGnssConfiguration.getProperties()).downloadPsdsData();
        if (data != null) {
            if (DEBUG) {
                Log.d("GnssLocationProvider", "calling native_inject_psds_data");
            }
            native_inject_psds_data(data, data.length);
            this.mPsdsBackOff.reset();
        }
        sendMessage(11, 0, null);
        if (data == null) {
            this.mHandler.sendEmptyMessageDelayed(6, this.mPsdsBackOff.nextBackoffMillis());
        }
        synchronized (this.mLock) {
            if (this.mDownloadPsdsWakeLock.isHeld()) {
                try {
                    this.mDownloadPsdsWakeLock.release();
                    if (DEBUG) {
                        Log.d("GnssLocationProvider", "WakeLock released by handleDownloadPsdsData()");
                    }
                } catch (Exception e) {
                    Log.i("GnssLocationProvider", "Wakelock timeout & release race exception in handleDownloadPsdsData()", e);
                }
            } else {
                Log.e("GnssLocationProvider", "WakeLock expired before release in handleDownloadPsdsData()");
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleUpdateLocation(Location location) {
        if (location.hasAccuracy()) {
            if (DEBUG) {
                Log.d("GnssLocationProvider", "injectLocation: " + location);
            }
            native_inject_location(location.getLatitude(), location.getLongitude(), location.getAccuracy());
        }
    }

    private void setSuplHostPort() {
        int i;
        this.mSuplServerHost = this.mGnssConfiguration.getSuplHost();
        this.mSuplServerPort = this.mGnssConfiguration.getSuplPort(0);
        String str = this.mSuplServerHost;
        if (str != null && (i = this.mSuplServerPort) > 0 && i <= 65535) {
            native_set_agps_server(1, str, i);
        }
    }

    private int getSuplMode(boolean agpsEnabled) {
        int suplMode;
        if (!agpsEnabled || (suplMode = this.mGnssConfiguration.getSuplMode(0)) == 0 || !hasCapability(2) || (suplMode & 1) == 0) {
            return 0;
        }
        return 1;
    }

    private void setGpsEnabled(boolean enabled) {
        synchronized (this.mLock) {
            this.mGpsEnabled = enabled;
        }
    }

    private void handleEnable() {
        if (DEBUG) {
            Log.d("GnssLocationProvider", "handleEnable");
        }
        if (native_init()) {
            setGpsEnabled(true);
            this.mSupportsPsds = native_supports_psds();
            String str = this.mSuplServerHost;
            if (str != null) {
                native_set_agps_server(1, str, this.mSuplServerPort);
            }
            String str2 = this.mC2KServerHost;
            if (str2 != null) {
                native_set_agps_server(2, str2, this.mC2KServerPort);
            }
            this.mGnssMeasurementsProvider.onGpsEnabledChanged();
            this.mGnssNavigationMessageProvider.onGpsEnabledChanged();
            this.mGnssBatchingProvider.enable();
            GnssVisibilityControl gnssVisibilityControl = this.mGnssVisibilityControl;
            if (gnssVisibilityControl != null) {
                gnssVisibilityControl.onGpsEnabledChanged(true);
                return;
            }
            return;
        }
        setGpsEnabled(false);
        Log.w("GnssLocationProvider", "Failed to enable location provider");
    }

    private void handleDisable() {
        if (DEBUG) {
            Log.d("GnssLocationProvider", "handleDisable");
        }
        this.mNavigationController.stopController();
        setGpsEnabled(false);
        updateClientUids(new WorkSource());
        stopNavigating();
        this.mAlarmManager.cancel(this.mWakeupIntent);
        this.mAlarmManager.cancel(this.mTimeoutIntent);
        GnssVisibilityControl gnssVisibilityControl = this.mGnssVisibilityControl;
        if (gnssVisibilityControl != null) {
            gnssVisibilityControl.onGpsEnabledChanged(false);
        }
        this.mGnssBatchingProvider.disable();
        native_cleanup();
        this.mGnssMeasurementsProvider.onGpsEnabledChanged();
        this.mGnssNavigationMessageProvider.onGpsEnabledChanged();
    }

    /* access modifiers changed from: private */
    public void updateEnabled() {
        boolean enabled = ((LocationManager) this.mContext.getSystemService(LocationManager.class)).isLocationEnabledForUser(UserHandle.CURRENT) & (!this.mDisableGpsForPowerManager);
        ProviderRequest providerRequest = this.mProviderRequest;
        boolean enabled2 = (enabled | (providerRequest != null && providerRequest.reportLocation && this.mProviderRequest.locationSettingsIgnored)) & (!this.mShutdown);
        if (enabled2 != isGpsEnabled()) {
            if (enabled2) {
                handleEnable();
            } else {
                handleDisable();
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isGpsEnabled() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mGpsEnabled;
        }
        return z;
    }

    @Override // com.android.server.location.AbstractLocationProvider
    public int getStatus(Bundle extras) {
        this.mLocationExtras.setBundle(extras);
        return this.mStatus;
    }

    private void updateStatus(int status) {
        if (status != this.mStatus) {
            this.mStatus = status;
            this.mStatusUpdateTime = SystemClock.elapsedRealtime();
        }
    }

    @Override // com.android.server.location.AbstractLocationProvider
    public long getStatusUpdateTime() {
        return this.mStatusUpdateTime;
    }

    @Override // com.android.server.location.AbstractLocationProvider
    public void setRequest(ProviderRequest request, WorkSource source) {
        sendMessage(3, 0, new GpsRequest(request, source));
    }

    /* access modifiers changed from: private */
    public void handleSetRequest(ProviderRequest request, WorkSource source) {
        this.mProviderRequest = request;
        this.mWorkSource = source;
        updateEnabled();
        updateRequirements();
    }

    private void updateRequirements() {
        if (this.mProviderRequest != null && this.mWorkSource != null) {
            Log.d("GnssLocationProvider", "setRequest " + this.mProviderRequest);
            if (!this.mProviderRequest.reportLocation || !isGpsEnabled()) {
                updateClientUids(new WorkSource());
                this.mNavigationController.stopController();
                stopNavigating();
                this.mAlarmManager.cancel(this.mWakeupIntent);
                this.mAlarmManager.cancel(this.mTimeoutIntent);
                return;
            }
            updateClientUids(this.mWorkSource);
            this.mFixInterval = (int) this.mProviderRequest.interval;
            this.mLowPowerMode = this.mProviderRequest.lowPowerMode;
            if (((long) this.mFixInterval) != this.mProviderRequest.interval) {
                Log.w("GnssLocationProvider", "interval overflow: " + this.mProviderRequest.interval);
                this.mFixInterval = Integer.MAX_VALUE;
            }
            if (!this.mStarted || !hasCapability(1)) {
                if (this.mStarted) {
                    this.mAlarmManager.cancel(this.mTimeoutIntent);
                    if (this.mFixInterval >= 60000) {
                        this.mAlarmManager.set(2, SystemClock.elapsedRealtime() + 60000, this.mTimeoutIntent);
                    }
                } else if (!this.mNavigationController.resistStartGps()) {
                    this.mNavigationController.setUp();
                    startNavigating();
                }
            } else if (!setPositionMode(this.mPositionMode, 0, this.mFixInterval, 0, 0, this.mLowPowerMode)) {
                Log.e("GnssLocationProvider", "set_position_mode failed in updateRequirements");
            } else {
                this.mOppoLocationStatistics.recordGnssNavigatingStarted((long) this.mFixInterval);
            }
        }
    }

    private boolean setPositionMode(int mode, int recurrence, int minInterval, int preferredAccuracy, int preferredTime, boolean lowPowerMode) {
        GnssPositionMode positionMode = new GnssPositionMode(mode, recurrence, minInterval, preferredAccuracy, preferredTime, lowPowerMode);
        GnssPositionMode gnssPositionMode = this.mLastPositionMode;
        if (gnssPositionMode != null && gnssPositionMode.equals(positionMode)) {
            return true;
        }
        boolean result = native_set_position_mode(mode, recurrence, minInterval, preferredAccuracy, preferredTime, lowPowerMode);
        if (result) {
            this.mLastPositionMode = positionMode;
        } else {
            this.mLastPositionMode = null;
        }
        return result;
    }

    private void updateClientUids(WorkSource source) {
        if (!source.equals(this.mClientSource)) {
            try {
                this.mBatteryStats.noteGpsChanged(this.mClientSource, source);
            } catch (RemoteException e) {
                Log.w("GnssLocationProvider", "RemoteException", e);
            }
            List<WorkSource.WorkChain>[] diffs = WorkSource.diffChains(this.mClientSource, source);
            if (diffs != null) {
                List<WorkSource.WorkChain> newChains = diffs[0];
                List<WorkSource.WorkChain> goneChains = diffs[1];
                if (newChains != null) {
                    for (WorkSource.WorkChain newChain : newChains) {
                        this.mAppOps.startOpNoThrow(2, newChain.getAttributionUid(), newChain.getAttributionTag());
                    }
                }
                if (goneChains != null) {
                    for (WorkSource.WorkChain goneChain : goneChains) {
                        this.mAppOps.finishOp(2, goneChain.getAttributionUid(), goneChain.getAttributionTag());
                    }
                }
                this.mClientSource.transferWorkChains(source);
            }
            WorkSource[] changes = this.mClientSource.setReturningDiffs(source);
            if (changes != null) {
                WorkSource newWork = changes[0];
                WorkSource goneWork = changes[1];
                if (newWork != null) {
                    for (int i = 0; i < newWork.size(); i++) {
                        this.mAppOps.startOpNoThrow(2, newWork.get(i), newWork.getName(i));
                    }
                }
                if (goneWork != null) {
                    for (int i2 = 0; i2 < goneWork.size(); i2++) {
                        this.mAppOps.finishOp(2, goneWork.get(i2), goneWork.getName(i2));
                    }
                }
            }
        }
    }

    @Override // com.android.server.location.AbstractLocationProvider
    public void sendExtraCommand(String command, Bundle extras) {
        long identity = Binder.clearCallingIdentity();
        try {
            if ("delete_aiding_data".equals(command)) {
                deleteAidingData(extras);
            } else if ("force_time_injection".equals(command)) {
                requestUtcTime();
            } else if (!"force_psds_injection".equals(command)) {
                Log.w("GnssLocationProvider", "sendExtraCommand: unknown command " + command);
            } else if (this.mSupportsPsds) {
                psdsDownloadRequest();
            }
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void deleteAidingData(Bundle extras) {
        int flags;
        if (extras == null) {
            flags = 65535;
        } else {
            flags = 0;
            if (extras.getBoolean("ephemeris")) {
                flags = 0 | 1;
            }
            if (extras.getBoolean("almanac")) {
                flags |= 2;
            }
            if (extras.getBoolean("position")) {
                flags |= 4;
            }
            if (extras.getBoolean("time")) {
                flags |= 8;
            }
            if (extras.getBoolean("iono")) {
                flags |= 16;
            }
            if (extras.getBoolean("utc")) {
                flags |= 32;
            }
            if (extras.getBoolean("health")) {
                flags |= 64;
            }
            if (extras.getBoolean("svdir")) {
                flags |= 128;
            }
            if (extras.getBoolean("svsteer")) {
                flags |= 256;
            }
            if (extras.getBoolean("sadata")) {
                flags |= 512;
            }
            if (extras.getBoolean("rti")) {
                flags |= 1024;
            }
            if (extras.getBoolean("celldb-info")) {
                flags |= 32768;
            }
            if (extras.getBoolean("all")) {
                flags |= 65535;
            }
        }
        int flags2 = mtkDeleteAidingData(extras, flags);
        if (flags2 != 0) {
            native_delete_aiding_data(flags2);
        }
    }

    /* access modifiers changed from: private */
    public void startNavigating() {
        String mode;
        if (!this.mStarted) {
            Log.d("GnssLocationProvider", "startNavigating");
            PswServiceFactory.getInstance().getFeature(IPswGnssDiagnosticTool.DEFAULT, new Object[]{this.mContext}).refreshRequestTimer();
            this.mTimeToFirstFix = 0;
            this.mLastFixTime = 0;
            setStarted(true);
            this.mPositionMode = 0;
            if (this.mItarSpeedLimitExceeded) {
                Log.i("GnssLocationProvider", "startNavigating with ITAR limit in place. Output limited  until slow enough speed reported.");
            }
            this.mPositionMode = getSuplMode(Settings.Global.getInt(this.mContext.getContentResolver(), "assisted_gps_enabled", 1) != 0);
            if (DEBUG) {
                int i = this.mPositionMode;
                if (i == 0) {
                    mode = "standalone";
                } else if (i == 1) {
                    mode = "MS_BASED";
                } else if (i != 2) {
                    mode = UiModeManagerService.Shell.NIGHT_MODE_STR_UNKNOWN;
                } else {
                    mode = "MS_ASSISTED";
                }
                Log.d("GnssLocationProvider", "setting position_mode to " + mode);
            }
            int interval = hasCapability(1) ? this.mFixInterval : 1000;
            this.mLowPowerMode = this.mProviderRequest.lowPowerMode;
            if (!setPositionMode(this.mPositionMode, 0, interval, 0, 0, this.mLowPowerMode)) {
                setStarted(false);
                Log.e("GnssLocationProvider", "set_position_mode failed in startNavigating()");
            } else if (!native_start()) {
                setStarted(false);
                Log.e("GnssLocationProvider", "native_start failed in startNavigating()");
            } else {
                this.mOppoLocationStatistics.recordGnssNavigatingStarted((long) interval);
                this.mNavigationController.startController();
                updateStatus(1);
                this.mLocationExtras.reset();
                this.mFixRequestTime = SystemClock.elapsedRealtime();
                if (!hasCapability(1) && this.mFixInterval >= 60000) {
                    this.mAlarmManager.set(2, SystemClock.elapsedRealtime() + 60000, this.mTimeoutIntent);
                }
            }
        }
    }

    private void stopNavigating() {
        if (DEBUG) {
            Log.d("GnssLocationProvider", "stopNavigating");
        }
        if (this.mStarted) {
            setStarted(false);
            native_stop();
            this.mLastFixTime = 0;
            this.mLastPositionMode = null;
            updateStatus(1);
            this.mLocationExtras.reset();
            this.mOppoLocationStatistics.recordGnssNavigatingStopped();
        }
    }

    private void setStarted(boolean started) {
        if (this.mStarted != started) {
            this.mStarted = started;
            this.mStartedChangedElapsedRealtime = SystemClock.elapsedRealtime();
        }
    }

    /* access modifiers changed from: private */
    public void hibernate() {
        stopNavigating();
        this.mAlarmManager.cancel(this.mTimeoutIntent);
        this.mAlarmManager.cancel(this.mWakeupIntent);
        this.mAlarmManager.set(2, ((long) this.mFixInterval) + SystemClock.elapsedRealtime(), this.mWakeupIntent);
    }

    private boolean hasCapability(int capability) {
        return (this.mTopHalCapabilities & capability) != 0;
    }

    private void reportLocation(boolean hasLatLong, Location location) {
        sendMessage(17, hasLatLong ? 1 : 0, location);
    }

    /* access modifiers changed from: private */
    public void handleReportLocation(boolean hasLatLong, Location location) {
        if (location.hasSpeed()) {
            this.mItarSpeedLimitExceeded = location.getSpeed() > ITAR_SPEED_LIMIT_METERS_PER_SECOND;
        }
        if (this.mItarSpeedLimitExceeded) {
            Log.i("GnssLocationProvider", "Hal reported a speed in excess of ITAR limit.  GPS/GNSS Navigation output blocked.");
            if (this.mStarted) {
                this.mGnssMetrics.logReceivedLocationStatus(false);
                return;
            }
            return;
        }
        if (VERBOSE) {
            Log.v("GnssLocationProvider", "reportLocation " + location.toString());
        }
        location.setExtras(this.mLocationExtras.getBundle());
        reportLocation(location);
        if (this.mStarted) {
            this.mGnssMetrics.logReceivedLocationStatus(hasLatLong);
            if (hasLatLong) {
                if (location.hasAccuracy()) {
                    this.mGnssMetrics.logPositionAccuracyMeters(location.getAccuracy());
                }
                if (this.mTimeToFirstFix > 0) {
                    this.mGnssMetrics.logMissedReports(this.mFixInterval, (int) (SystemClock.elapsedRealtime() - this.mLastFixTime));
                }
            }
        } else {
            long locationAfterStartedFalseMillis = SystemClock.elapsedRealtime() - this.mStartedChangedElapsedRealtime;
            if (locationAfterStartedFalseMillis > LOCATION_OFF_DELAY_THRESHOLD_WARN_MILLIS) {
                String logMessage = "Unexpected GNSS Location report " + TimeUtils.formatDuration(locationAfterStartedFalseMillis) + " after location turned off";
                if (locationAfterStartedFalseMillis > LOCATION_OFF_DELAY_THRESHOLD_ERROR_MILLIS) {
                    Log.e("GnssLocationProvider", logMessage);
                } else {
                    Log.w("GnssLocationProvider", logMessage);
                }
            }
        }
        this.mLastFixTime = SystemClock.elapsedRealtime();
        if (this.mTimeToFirstFix == 0 && hasLatLong) {
            this.mTimeToFirstFix = (int) (this.mLastFixTime - this.mFixRequestTime);
            Log.d("GnssLocationProvider", "TTFF: " + this.mTimeToFirstFix);
            if (this.mStarted) {
                this.mGnssMetrics.logTimeToFirstFixMilliSecs(this.mTimeToFirstFix);
            }
            this.mGnssStatusListenerHelper.onFirstFix(this.mTimeToFirstFix);
        }
        int svCount = this.mLocationExtras.getBundle().getInt("satellites");
        if (this.mStarted && this.mStatus != 2 && svCount > 2) {
            if (!hasCapability(1) && this.mFixInterval < 60000) {
                this.mAlarmManager.cancel(this.mTimeoutIntent);
            }
            updateStatus(2);
        }
        if (!hasCapability(1) && this.mStarted && this.mFixInterval > 10000) {
            if (DEBUG) {
                Log.d("GnssLocationProvider", "got fix, hibernating");
            }
            hibernate();
        }
    }

    private void reportStatus(int status) {
        if (DEBUG) {
            Log.v("GnssLocationProvider", "reportStatus status: " + status);
        }
        boolean wasNavigating = this.mNavigating;
        if (status == 1) {
            this.mNavigating = true;
        } else if (status == 2) {
            this.mNavigating = false;
        } else if (status != 3 && status == 4) {
            this.mNavigating = false;
        }
        boolean z = this.mNavigating;
        if (wasNavigating != z) {
            this.mGnssStatusListenerHelper.onStatusChanged(z);
        }
    }

    /* access modifiers changed from: private */
    public static class SvStatusInfo {
        /* access modifiers changed from: private */
        public float[] mCn0s;
        /* access modifiers changed from: private */
        public float[] mSvAzimuths;
        /* access modifiers changed from: private */
        public float[] mSvCarrierFreqs;
        /* access modifiers changed from: private */
        public int mSvCount;
        /* access modifiers changed from: private */
        public float[] mSvElevations;
        /* access modifiers changed from: private */
        public int[] mSvidWithFlags;

        private SvStatusInfo() {
        }
    }

    private void reportSvStatus(int svCount, int[] svidWithFlags, float[] cn0s, float[] svElevations, float[] svAzimuths, float[] svCarrierFreqs) {
        SvStatusInfo svStatusInfo = new SvStatusInfo();
        int unused = svStatusInfo.mSvCount = svCount;
        int[] unused2 = svStatusInfo.mSvidWithFlags = svidWithFlags;
        float[] unused3 = svStatusInfo.mCn0s = cn0s;
        float[] unused4 = svStatusInfo.mSvElevations = svElevations;
        float[] unused5 = svStatusInfo.mSvAzimuths = svAzimuths;
        float[] unused6 = svStatusInfo.mSvCarrierFreqs = svCarrierFreqs;
        sendMessage(18, 0, svStatusInfo);
    }

    /* access modifiers changed from: private */
    public void handleReportSvStatus(SvStatusInfo info) {
        this.mGnssStatusListenerHelper.onSvStatusChanged(info.mSvCount, info.mSvidWithFlags, info.mCn0s, info.mSvElevations, info.mSvAzimuths, info.mSvCarrierFreqs);
        this.mGnssMetrics.logCn0(info.mCn0s, info.mSvCount);
        if (VERBOSE) {
            Log.v("GnssLocationProvider", "SV count: " + info.mSvCount);
        }
        int usedInFixCount = 0;
        int maxCn0 = 0;
        int meanCn0 = 0;
        for (int i = 0; i < info.mSvCount; i++) {
            if ((info.mSvidWithFlags[i] & 4) != 0) {
                usedInFixCount++;
                if (info.mCn0s[i] > ((float) maxCn0)) {
                    maxCn0 = (int) info.mCn0s[i];
                }
                meanCn0 = (int) (((float) meanCn0) + info.mCn0s[i]);
            }
            if (VERBOSE) {
                StringBuilder sb = new StringBuilder();
                sb.append("svid: ");
                sb.append(info.mSvidWithFlags[i] >> 8);
                sb.append(" cn0: ");
                sb.append(info.mCn0s[i]);
                sb.append(" elev: ");
                sb.append(info.mSvElevations[i]);
                sb.append(" azimuth: ");
                sb.append(info.mSvAzimuths[i]);
                sb.append(" carrier frequency: ");
                sb.append(info.mSvCarrierFreqs[i]);
                String str = "  ";
                sb.append((1 & info.mSvidWithFlags[i]) == 0 ? str : " E");
                if ((2 & info.mSvidWithFlags[i]) != 0) {
                    str = " A";
                }
                sb.append(str);
                String str2 = "";
                sb.append((info.mSvidWithFlags[i] & 4) == 0 ? str2 : "U");
                if ((info.mSvidWithFlags[i] & 8) != 0) {
                    str2 = "F";
                }
                sb.append(str2);
                Log.v("GnssLocationProvider", sb.toString());
            }
            if ((info.mSvidWithFlags[i] & 4) != 0) {
                this.mGnssMetrics.logConstellationType((info.mSvidWithFlags[i] >> 4) & 15);
            }
        }
        if (usedInFixCount > 0) {
            meanCn0 /= usedInFixCount;
        }
        this.mLocationExtras.set(usedInFixCount, meanCn0, maxCn0);
        PswServiceFactory.getInstance().getFeature(IPswGnssDiagnosticTool.DEFAULT, new Object[]{this.mContext}).storeSatellitesInfo(info.mSvCount, usedInFixCount, maxCn0);
        if (this.mNavigating && this.mStatus == 2 && this.mLastFixTime > 0 && SystemClock.elapsedRealtime() - this.mLastFixTime > 10000) {
            updateStatus(1);
        }
    }

    private void reportAGpsStatus(int agpsType, int agpsStatus, byte[] suplIpAddr) {
        this.mNetworkConnectivityHandler.onReportAGpsStatus(agpsType, agpsStatus, suplIpAddr);
    }

    private void reportNmea(long timestamp) {
        if (!this.mItarSpeedLimitExceeded) {
            byte[] bArr = this.mNmeaBuffer;
            this.mGnssStatusListenerHelper.onNmeaReceived(timestamp, new String(this.mNmeaBuffer, 0, native_read_nmea(bArr, bArr.length)));
        }
    }

    private void reportMeasurementData(GnssMeasurementsEvent event) {
        if (!this.mItarSpeedLimitExceeded) {
            this.mHandler.post(new Runnable(event) {
                /* class com.android.server.location.$$Lambda$GnssLocationProvider$7an4_QLRsGpd_GYEEX9o8MWsh5g */
                private final /* synthetic */ GnssMeasurementsEvent f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    GnssLocationProvider.this.lambda$reportMeasurementData$3$GnssLocationProvider(this.f$1);
                }
            });
        }
    }

    public /* synthetic */ void lambda$reportMeasurementData$3$GnssLocationProvider(GnssMeasurementsEvent event) {
        this.mGnssMeasurementsProvider.onMeasurementsAvailable(event);
    }

    private void reportNavigationMessage(GnssNavigationMessage event) {
        if (!this.mItarSpeedLimitExceeded) {
            this.mHandler.post(new Runnable(event) {
                /* class com.android.server.location.$$Lambda$GnssLocationProvider$xOqKbJvkF9q308HLNoriIqVzOc */
                private final /* synthetic */ GnssNavigationMessage f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    GnssLocationProvider.this.lambda$reportNavigationMessage$4$GnssLocationProvider(this.f$1);
                }
            });
        }
    }

    public /* synthetic */ void lambda$reportNavigationMessage$4$GnssLocationProvider(GnssNavigationMessage event) {
        this.mGnssNavigationMessageProvider.onNavigationMessageAvailable(event);
    }

    private void setTopHalCapabilities(int topHalCapabilities) {
        this.mHandler.post(new Runnable(topHalCapabilities) {
            /* class com.android.server.location.$$Lambda$GnssLocationProvider$FgVpMm9HUsK34prF193aseSjf8 */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                GnssLocationProvider.this.lambda$setTopHalCapabilities$5$GnssLocationProvider(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$setTopHalCapabilities$5$GnssLocationProvider(int topHalCapabilities) {
        this.mTopHalCapabilities = topHalCapabilities;
        if (hasCapability(16)) {
            this.mNtpTimeHelper.enablePeriodicTimeInjection();
            requestUtcTime();
        }
        this.mGnssMeasurementsProvider.onCapabilitiesUpdated(hasCapability(64));
        this.mGnssNavigationMessageProvider.onCapabilitiesUpdated(hasCapability(128));
        restartRequests();
        this.mGnssCapabilitiesProvider.setTopHalCapabilities(this.mTopHalCapabilities);
    }

    private void setSubHalMeasurementCorrectionsCapabilities(int subHalCapabilities) {
        this.mHandler.post(new Runnable(subHalCapabilities) {
            /* class com.android.server.location.$$Lambda$GnssLocationProvider$9jdOLj83ArhY9j3s3_Ubo4wma44 */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                GnssLocationProvider.this.lambda$setSubHalMeasurementCorrectionsCapabilities$6$GnssLocationProvider(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$setSubHalMeasurementCorrectionsCapabilities$6$GnssLocationProvider(int subHalCapabilities) {
        if (this.mGnssMeasurementCorrectionsProvider.onCapabilitiesUpdated(subHalCapabilities)) {
            this.mGnssCapabilitiesProvider.setSubHalMeasurementCorrectionsCapabilities(subHalCapabilities);
        }
    }

    private void restartRequests() {
        Log.i("GnssLocationProvider", "restartRequests");
        restartLocationRequest();
        this.mGnssMeasurementsProvider.resumeIfStarted();
        this.mGnssNavigationMessageProvider.resumeIfStarted();
        this.mGnssBatchingProvider.resumeIfStarted();
        this.mGnssGeofenceProvider.resumeIfStarted();
    }

    private void restartLocationRequest() {
        if (DEBUG) {
            Log.d("GnssLocationProvider", "restartLocationRequest");
        }
        setStarted(false);
        updateRequirements();
    }

    private void setGnssYearOfHardware(int yearOfHardware) {
        if (DEBUG) {
            Log.d("GnssLocationProvider", "setGnssYearOfHardware called with " + yearOfHardware);
        }
        this.mHardwareYear = yearOfHardware;
    }

    private void setGnssHardwareModelName(String modelName) {
        if (DEBUG) {
            Log.d("GnssLocationProvider", "setGnssModelName called with " + modelName);
        }
        this.mHardwareModelName = modelName;
    }

    private void reportGnssServiceDied() {
        if (DEBUG) {
            Log.d("GnssLocationProvider", "reportGnssServiceDied");
        }
        this.mHandler.post(new Runnable() {
            /* class com.android.server.location.$$Lambda$GnssLocationProvider$_u_h1wukFYajzrvRlCDdiTE0DwU */

            public final void run() {
                GnssLocationProvider.this.lambda$reportGnssServiceDied$7$GnssLocationProvider();
            }
        });
    }

    public /* synthetic */ void lambda$reportGnssServiceDied$7$GnssLocationProvider() {
        setupNativeGnssService(true);
        if (isGpsEnabled()) {
            setGpsEnabled(false);
            updateEnabled();
            reloadGpsProperties();
        }
    }

    public GnssSystemInfoProvider getGnssSystemInfoProvider() {
        return new GnssSystemInfoProvider() {
            /* class com.android.server.location.GnssLocationProvider.AnonymousClass7 */

            @Override // com.android.server.location.GnssLocationProvider.GnssSystemInfoProvider
            public int getGnssYearOfHardware() {
                return GnssLocationProvider.this.mHardwareYear;
            }

            @Override // com.android.server.location.GnssLocationProvider.GnssSystemInfoProvider
            public String getGnssHardwareModelName() {
                return GnssLocationProvider.this.mHardwareModelName;
            }
        };
    }

    public GnssBatchingProvider getGnssBatchingProvider() {
        return this.mGnssBatchingProvider;
    }

    public GnssMetricsProvider getGnssMetricsProvider() {
        return new GnssMetricsProvider() {
            /* class com.android.server.location.$$Lambda$GnssLocationProvider$rE3aXybVXWDfHPxCjEXzxG9bPmo */

            @Override // com.android.server.location.GnssLocationProvider.GnssMetricsProvider
            public final String getGnssMetricsAsProtoString() {
                return GnssLocationProvider.this.lambda$getGnssMetricsProvider$8$GnssLocationProvider();
            }
        };
    }

    public /* synthetic */ String lambda$getGnssMetricsProvider$8$GnssLocationProvider() {
        return this.mGnssMetrics.dumpGnssMetricsAsProtoString();
    }

    public GnssCapabilitiesProvider getGnssCapabilitiesProvider() {
        return this.mGnssCapabilitiesProvider;
    }

    private void reportLocationBatch(Location[] locationArray) {
        List<Location> locations = new ArrayList<>(Arrays.asList(locationArray));
        if (DEBUG) {
            Log.d("GnssLocationProvider", "Location batch of size " + locationArray.length + " reported");
        }
        reportLocation(locations);
    }

    private void psdsDownloadRequest() {
        if (DEBUG) {
            Log.d("GnssLocationProvider", "psdsDownloadRequest");
        }
        sendMessage(6, 0, null);
    }

    private static int getGeofenceStatus(int status) {
        if (status == GPS_GEOFENCE_ERROR_GENERIC) {
            return 5;
        }
        if (status == 0) {
            return 0;
        }
        if (status == 100) {
            return 1;
        }
        switch (status) {
            case GPS_GEOFENCE_ERROR_INVALID_TRANSITION /*{ENCODED_INT: -103}*/:
                return 4;
            case GPS_GEOFENCE_ERROR_ID_UNKNOWN /*{ENCODED_INT: -102}*/:
                return 3;
            case GPS_GEOFENCE_ERROR_ID_EXISTS /*{ENCODED_INT: -101}*/:
                return 2;
            default:
                return -1;
        }
    }

    private void reportGeofenceTransition(int geofenceId, Location location, int transition, long transitionTimestamp) {
        this.mHandler.post(new Runnable(geofenceId, location, transition, transitionTimestamp) {
            /* class com.android.server.location.$$Lambda$GnssLocationProvider$OlaPfB60MVaXRIneVwZiybyWF4 */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ Location f$2;
            private final /* synthetic */ int f$3;
            private final /* synthetic */ long f$4;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
            }

            public final void run() {
                GnssLocationProvider.this.lambda$reportGeofenceTransition$9$GnssLocationProvider(this.f$1, this.f$2, this.f$3, this.f$4);
            }
        });
    }

    public /* synthetic */ void lambda$reportGeofenceTransition$9$GnssLocationProvider(int geofenceId, Location location, int transition, long transitionTimestamp) {
        if (this.mGeofenceHardwareImpl == null) {
            this.mGeofenceHardwareImpl = GeofenceHardwareImpl.getInstance(this.mContext);
        }
        this.mGeofenceHardwareImpl.reportGeofenceTransition(geofenceId, location, transition, transitionTimestamp, 0, FusedBatchOptions.SourceTechnologies.GNSS);
    }

    private void reportGeofenceStatus(int status, Location location) {
        this.mHandler.post(new Runnable(status, location) {
            /* class com.android.server.location.$$Lambda$GnssLocationProvider$ii5CDUWWmfq57JzZZBF3Nxnic */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ Location f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                GnssLocationProvider.this.lambda$reportGeofenceStatus$10$GnssLocationProvider(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$reportGeofenceStatus$10$GnssLocationProvider(int status, Location location) {
        if (this.mGeofenceHardwareImpl == null) {
            this.mGeofenceHardwareImpl = GeofenceHardwareImpl.getInstance(this.mContext);
        }
        int monitorStatus = 1;
        if (status == 2) {
            monitorStatus = 0;
        }
        this.mGeofenceHardwareImpl.reportGeofenceMonitorStatus(0, monitorStatus, location, FusedBatchOptions.SourceTechnologies.GNSS);
    }

    private void reportGeofenceAddStatus(int geofenceId, int status) {
        this.mHandler.post(new Runnable(geofenceId, status) {
            /* class com.android.server.location.$$Lambda$GnssLocationProvider$UbVMf2XkqNujf6ZZYbD3ITfhy98 */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                GnssLocationProvider.this.lambda$reportGeofenceAddStatus$11$GnssLocationProvider(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$reportGeofenceAddStatus$11$GnssLocationProvider(int geofenceId, int status) {
        if (this.mGeofenceHardwareImpl == null) {
            this.mGeofenceHardwareImpl = GeofenceHardwareImpl.getInstance(this.mContext);
        }
        this.mGeofenceHardwareImpl.reportGeofenceAddStatus(geofenceId, getGeofenceStatus(status));
    }

    private void reportGeofenceRemoveStatus(int geofenceId, int status) {
        this.mHandler.post(new Runnable(geofenceId, status) {
            /* class com.android.server.location.$$Lambda$GnssLocationProvider$AqYK7fn42KqHmtzfEEHCId_ucqc */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                GnssLocationProvider.this.lambda$reportGeofenceRemoveStatus$12$GnssLocationProvider(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$reportGeofenceRemoveStatus$12$GnssLocationProvider(int geofenceId, int status) {
        if (this.mGeofenceHardwareImpl == null) {
            this.mGeofenceHardwareImpl = GeofenceHardwareImpl.getInstance(this.mContext);
        }
        this.mGeofenceHardwareImpl.reportGeofenceRemoveStatus(geofenceId, getGeofenceStatus(status));
    }

    private void reportGeofencePauseStatus(int geofenceId, int status) {
        this.mHandler.post(new Runnable(geofenceId, status) {
            /* class com.android.server.location.$$Lambda$GnssLocationProvider$YioQmt5_4rwC3kkzEgeymB15HhA */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                GnssLocationProvider.this.lambda$reportGeofencePauseStatus$13$GnssLocationProvider(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$reportGeofencePauseStatus$13$GnssLocationProvider(int geofenceId, int status) {
        if (this.mGeofenceHardwareImpl == null) {
            this.mGeofenceHardwareImpl = GeofenceHardwareImpl.getInstance(this.mContext);
        }
        this.mGeofenceHardwareImpl.reportGeofencePauseStatus(geofenceId, getGeofenceStatus(status));
    }

    private void reportGeofenceResumeStatus(int geofenceId, int status) {
        this.mHandler.post(new Runnable(geofenceId, status) {
            /* class com.android.server.location.$$Lambda$GnssLocationProvider$WHLwhMXdOptyG8XPk2vIU0pgmL8 */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                GnssLocationProvider.this.lambda$reportGeofenceResumeStatus$14$GnssLocationProvider(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$reportGeofenceResumeStatus$14$GnssLocationProvider(int geofenceId, int status) {
        if (this.mGeofenceHardwareImpl == null) {
            this.mGeofenceHardwareImpl = GeofenceHardwareImpl.getInstance(this.mContext);
        }
        this.mGeofenceHardwareImpl.reportGeofenceResumeStatus(geofenceId, getGeofenceStatus(status));
    }

    public INetInitiatedListener getNetInitiatedListener() {
        return this.mNetInitiatedListener;
    }

    public void reportNiNotification(int notificationId, int niType, int notifyFlags, int timeout, int defaultResponse, String requestorId, String text, int requestorIdEncoding, int textEncoding) {
        Log.i("GnssLocationProvider", "reportNiNotification: entered");
        Log.i("GnssLocationProvider", "notificationId: " + notificationId + ", niType: " + niType + ", notifyFlags: " + notifyFlags + ", timeout: " + timeout + ", defaultResponse: " + defaultResponse);
        StringBuilder sb = new StringBuilder();
        sb.append("requestorId: ");
        sb.append(requestorId);
        sb.append(", text: ");
        sb.append(text);
        sb.append(", requestorIdEncoding: ");
        sb.append(requestorIdEncoding);
        sb.append(", textEncoding: ");
        sb.append(textEncoding);
        Log.i("GnssLocationProvider", sb.toString());
        GpsNetInitiatedHandler.GpsNiNotification notification = new GpsNetInitiatedHandler.GpsNiNotification();
        notification.notificationId = notificationId;
        notification.niType = niType;
        boolean z = false;
        notification.needNotify = (notifyFlags & 1) != 0;
        notification.needVerify = (notifyFlags & 2) != 0;
        if ((notifyFlags & 4) != 0) {
            z = true;
        }
        notification.privacyOverride = z;
        notification.timeout = timeout;
        notification.defaultResponse = defaultResponse;
        notification.requestorId = requestorId;
        notification.text = text;
        notification.requestorIdEncoding = requestorIdEncoding;
        notification.textEncoding = textEncoding;
        this.mNIHandler.handleNiNotification(notification);
        StatsLog.write(124, 1, notification.notificationId, notification.niType, notification.needNotify, notification.needVerify, notification.privacyOverride, notification.timeout, notification.defaultResponse, notification.requestorId, notification.text, notification.requestorIdEncoding, notification.textEncoding, this.mSuplEsEnabled, isGpsEnabled(), 0);
    }

    private void requestSetID(int flags) {
        TelephonyManager phone = (TelephonyManager) this.mContext.getSystemService("phone");
        int type = 0;
        String setId = null;
        int ddSubId = SubscriptionManager.getDefaultDataSubscriptionId();
        if ((flags & 1) == 1) {
            if (SubscriptionManager.isValidSubscriptionId(ddSubId)) {
                setId = phone.getSubscriberId(ddSubId);
            }
            if (setId == null) {
                setId = phone.getSubscriberId();
            }
            if (setId != null) {
                type = 1;
            }
        } else if ((flags & 2) == 2) {
            if (SubscriptionManager.isValidSubscriptionId(ddSubId)) {
                setId = phone.getLine1Number(ddSubId);
            }
            if (setId == null) {
                setId = phone.getLine1Number();
            }
            if (setId != null) {
                type = 2;
            }
        }
        native_agps_set_id(type, setId == null ? "" : setId);
    }

    private void requestLocation(boolean independentFromGnss, boolean isUserEmergency) {
        if (DEBUG) {
            Log.d("GnssLocationProvider", "requestLocation. independentFromGnss: " + independentFromGnss + ", isUserEmergency: " + isUserEmergency);
        }
        sendMessage(16, independentFromGnss ? 1 : 0, Boolean.valueOf(isUserEmergency));
    }

    private void requestUtcTime() {
        if (DEBUG) {
            Log.d("GnssLocationProvider", "utcTimeRequest");
        }
        sendMessage(5, 0, null);
    }

    private void requestRefLocation() {
        int type;
        TelephonyManager phone = (TelephonyManager) this.mContext.getSystemService("phone");
        int phoneType = phone.getPhoneType();
        if (phoneType == 1) {
            GsmCellLocation gsm_cell = (GsmCellLocation) phone.getCellLocation();
            if (gsm_cell == null || phone.getNetworkOperator() == null || phone.getNetworkOperator().length() <= 3) {
                Log.e("GnssLocationProvider", "Error getting cell location info.");
                return;
            }
            int mcc = Integer.parseInt(phone.getNetworkOperator().substring(0, 3));
            int mnc = Integer.parseInt(phone.getNetworkOperator().substring(3));
            int networkType = phone.getNetworkType();
            if (networkType == 3 || networkType == 8 || networkType == 9 || networkType == 10 || networkType == 15) {
                type = 2;
            } else {
                type = 1;
            }
            native_agps_set_ref_location_cellid(type, mcc, mnc, gsm_cell.getLac(), gsm_cell.getCid());
        } else if (phoneType == 2) {
            Log.e("GnssLocationProvider", "CDMA not supported.");
        }
    }

    private void reportNfwNotification(String proxyAppPackageName, byte protocolStack, String otherProtocolStackName, byte requestor, String requestorId, byte responseType, boolean inEmergencyMode, boolean isCachedLocation) {
        GnssVisibilityControl gnssVisibilityControl = this.mGnssVisibilityControl;
        if (gnssVisibilityControl == null) {
            Log.e("GnssLocationProvider", "reportNfwNotification: mGnssVisibilityControl is not initialized.");
        } else {
            gnssVisibilityControl.reportNfwNotification(proxyAppPackageName, protocolStack, otherProtocolStackName, requestor, requestorId, responseType, inEmergencyMode, isCachedLocation);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isInEmergencySession() {
        return this.mNIHandler.getInEmergency();
    }

    private void sendMessage(int message, int arg, Object obj) {
        this.mWakeLock.acquire();
        this.mOppoLocationStatistics.recordHeldWakelock("GnssLocationProvider");
        if (DEBUG) {
            Log.d("GnssLocationProvider", "WakeLock acquired by sendMessage(" + messageIdAsString(message) + ", " + arg + ", " + obj + ")");
        }
        this.mHandler.obtainMessage(message, arg, 1, obj).sendToTarget();
    }

    private final class ProviderHandler extends Handler {
        public ProviderHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            int message = msg.what;
            if (message == 3) {
                GpsRequest gpsRequest = (GpsRequest) msg.obj;
                GnssLocationProvider.this.handleSetRequest(gpsRequest.request, gpsRequest.source);
            } else if (message == 11) {
                int unused = GnssLocationProvider.this.mDownloadPsdsDataPending = 2;
            } else if (message == 13) {
                handleInitialize();
            } else if (message == 5) {
                GnssLocationProvider.this.mNtpTimeHelper.retrieveAndInjectNtpTime();
            } else if (message == 6) {
                GnssLocationProvider.this.handleDownloadPsdsData();
            } else if (message != 7) {
                boolean z = false;
                switch (message) {
                    case 16:
                        GnssLocationProvider gnssLocationProvider = GnssLocationProvider.this;
                        if (msg.arg1 == 1) {
                            z = true;
                        }
                        gnssLocationProvider.handleRequestLocation(z, ((Boolean) msg.obj).booleanValue());
                        break;
                    case 17:
                        GnssLocationProvider gnssLocationProvider2 = GnssLocationProvider.this;
                        if (msg.arg1 == 1) {
                            z = true;
                        }
                        gnssLocationProvider2.handleReportLocation(z, (Location) msg.obj);
                        break;
                    case 18:
                        GnssLocationProvider.this.handleReportSvStatus((SvStatusInfo) msg.obj);
                        break;
                }
            } else {
                GnssLocationProvider.this.handleUpdateLocation((Location) msg.obj);
            }
            if (msg.arg2 == 1) {
                GnssLocationProvider.this.mWakeLock.release();
                GnssLocationProvider.this.mOppoLocationStatistics.recordReleaseWakelock("GnssLocationProvider");
                if (GnssLocationProvider.DEBUG) {
                    Log.d("GnssLocationProvider", "WakeLock released by handleMessage(" + GnssLocationProvider.this.messageIdAsString(message) + ", " + msg.arg1 + ", " + msg.obj + ")");
                }
            }
        }

        private void handleInitialize() {
            GnssLocationProvider.this.setupNativeGnssService(false);
            if (GnssLocationProvider.native_is_gnss_visibility_control_supported()) {
                GnssLocationProvider gnssLocationProvider = GnssLocationProvider.this;
                GnssVisibilityControl unused = gnssLocationProvider.mGnssVisibilityControl = new GnssVisibilityControl(gnssLocationProvider.mContext, GnssLocationProvider.this.mLooper, GnssLocationProvider.this.mNIHandler);
                GnssLocationProvider.this.mGnssVisibilityControl.setDebug(GnssLocationProvider.DEBUG);
            }
            GnssLocationProvider.this.reloadGpsProperties();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(GnssLocationProvider.ALARM_WAKEUP);
            intentFilter.addAction(GnssLocationProvider.ALARM_TIMEOUT);
            intentFilter.addAction("android.os.action.POWER_SAVE_MODE_CHANGED");
            intentFilter.addAction("android.os.action.DEVICE_IDLE_MODE_CHANGED");
            intentFilter.addAction("android.intent.action.SCREEN_OFF");
            intentFilter.addAction("android.intent.action.SCREEN_ON");
            intentFilter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
            intentFilter.addAction("android.intent.action.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED");
            intentFilter.addAction(GnssLocationProvider.ACTION_MTKLOGGER_STATE_CHANGED);
            GnssLocationProvider.this.mContext.registerReceiver(GnssLocationProvider.this.mBroadcastReceiver, intentFilter, null, this);
            GnssLocationProvider.this.mNetworkConnectivityHandler.registerNetworkCallbacks();
            LocationRequest request = LocationRequest.createFromDeprecatedProvider("passive", 0, (float) OppoBrightUtils.MIN_LUX_LIMITI, false);
            request.setHideFromAppOps(true);
            ((LocationManager) GnssLocationProvider.this.mContext.getSystemService("location")).requestLocationUpdates(request, new NetworkLocationListener(), getLooper());
            GnssLocationProvider.this.updateEnabled();
            GnssLocationProvider.this.mNavigationController.init();
        }
    }

    /* access modifiers changed from: private */
    public abstract class LocationChangeListener implements LocationListener {
        private int mNumLocationUpdateRequest;

        private LocationChangeListener() {
        }

        static /* synthetic */ int access$1106(LocationChangeListener x0) {
            int i = x0.mNumLocationUpdateRequest - 1;
            x0.mNumLocationUpdateRequest = i;
            return i;
        }

        static /* synthetic */ int access$1108(LocationChangeListener x0) {
            int i = x0.mNumLocationUpdateRequest;
            x0.mNumLocationUpdateRequest = i + 1;
            return i;
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    }

    private final class NetworkLocationListener extends LocationChangeListener {
        private NetworkLocationListener() {
            super();
        }

        public void onLocationChanged(Location location) {
            if ("network".equals(location.getProvider())) {
                GnssLocationProvider.this.handleUpdateLocation(location);
            }
        }
    }

    private final class FusedLocationListener extends LocationChangeListener {
        private FusedLocationListener() {
            super();
        }

        public void onLocationChanged(Location location) {
            if ("fused".equals(location.getProvider())) {
                GnssLocationProvider.this.injectBestLocation(location);
            }
        }
    }

    /* access modifiers changed from: private */
    public String messageIdAsString(int message) {
        if (message == 3) {
            return "SET_REQUEST";
        }
        if (message == 11) {
            return "DOWNLOAD_PSDS_DATA_FINISHED";
        }
        if (message == 13) {
            return "INITIALIZE_HANDLER";
        }
        if (message == 5) {
            return "INJECT_NTP_TIME";
        }
        if (message == 6) {
            return "DOWNLOAD_PSDS_DATA";
        }
        if (message == 7) {
            return "UPDATE_LOCATION";
        }
        switch (message) {
            case 16:
                return "REQUEST_LOCATION";
            case 17:
                return "REPORT_LOCATION";
            case 18:
                return "REPORT_SV_STATUS";
            default:
                return "<Unknown>";
        }
    }

    @Override // com.android.server.location.AbstractLocationProvider
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        StringBuilder s = new StringBuilder();
        boolean z = false;
        if (args.length > 0 && this.mNavigationController != null) {
            if (args[0].equals("--disableGnssPowerSave")) {
                this.mNavigationController.setPowerSaveForDump(1);
            } else if (args[0].equals("--enableGnssPowerSave")) {
                this.mNavigationController.setPowerSaveForDump(2);
            } else if (args[0].equals("--getGnssPowerSave")) {
                s.append("  GnssPowerSaveFeature=");
                s.append(this.mNavigationController.powerSaveEnabled());
                s.append(10);
            }
        }
        s.append("  mStarted=");
        s.append(this.mStarted);
        s.append("   (changed ");
        TimeUtils.formatDuration(SystemClock.elapsedRealtime() - this.mStartedChangedElapsedRealtime, s);
        s.append(" ago)");
        s.append(10);
        s.append("  mFixInterval=");
        s.append(this.mFixInterval);
        s.append(10);
        s.append("  mLowPowerMode=");
        s.append(this.mLowPowerMode);
        s.append(10);
        s.append("  isInPowerSavingMode=");
        StringBuilder sb = new StringBuilder();
        sb.append("");
        if (this.mNavigationController.getNavigateMode() == 2) {
            z = true;
        }
        sb.append(z);
        s.append(sb.toString());
        s.append(10);
        s.append("  mGnssMeasurementsProvider.isRegistered()=");
        s.append(this.mGnssMeasurementsProvider.isRegistered());
        s.append(10);
        s.append("  mGnssNavigationMessageProvider.isRegistered()=");
        s.append(this.mGnssNavigationMessageProvider.isRegistered());
        s.append(10);
        s.append("  mDisableGpsForPowerManager=");
        s.append(this.mDisableGpsForPowerManager);
        s.append(10);
        s.append("  mTopHalCapabilities=0x");
        s.append(Integer.toHexString(this.mTopHalCapabilities));
        s.append(" ( ");
        if (hasCapability(1)) {
            s.append("SCHEDULING ");
        }
        if (hasCapability(2)) {
            s.append("MSB ");
        }
        if (hasCapability(4)) {
            s.append("MSA ");
        }
        if (hasCapability(8)) {
            s.append("SINGLE_SHOT ");
        }
        if (hasCapability(16)) {
            s.append("ON_DEMAND_TIME ");
        }
        if (hasCapability(32)) {
            s.append("GEOFENCING ");
        }
        if (hasCapability(64)) {
            s.append("MEASUREMENTS ");
        }
        if (hasCapability(128)) {
            s.append("NAV_MESSAGES ");
        }
        if (hasCapability(256)) {
            s.append("LOW_POWER_MODE ");
        }
        if (hasCapability(512)) {
            s.append("SATELLITE_BLACKLIST ");
        }
        if (hasCapability(1024)) {
            s.append("MEASUREMENT_CORRECTIONS ");
        }
        s.append(")\n");
        if (hasCapability(1024)) {
            s.append("  SubHal=MEASUREMENT_CORRECTIONS[");
            s.append(this.mGnssMeasurementCorrectionsProvider.toStringCapabilities());
            s.append("]\n");
        }
        s.append(this.mGnssMetrics.dumpGnssMetricsAsText());
        s.append("  native internal state: ");
        s.append(native_get_internal_state());
        s.append(StringUtils.LF);
        pw.append((CharSequence) s);
    }

    /* access modifiers changed from: private */
    public void setupNativeGnssService(boolean reinitializeGnssServiceHandle) {
        native_init_once(reinitializeGnssServiceHandle);
        if (!native_init()) {
            Log.w("GnssLocationProvider", "Native initialization failed.");
        } else {
            native_cleanup();
        }
    }

    private void initMtkGnssLocProvider() {
        Constructor constructor;
        try {
            this.mMtkGnssProviderClass = Class.forName("com.mediatek.location.MtkLocationExt$GnssLocationProvider");
            if (DEBUG) {
                Log.d("GnssLocationProvider", "class = " + this.mMtkGnssProviderClass);
            }
            if (!(this.mMtkGnssProviderClass == null || (constructor = this.mMtkGnssProviderClass.getConstructor(Context.class, Handler.class)) == null)) {
                this.mMtkGnssProvider = constructor.newInstance(this.mContext, this.mHandler);
            }
            Log.d("GnssLocationProvider", "mMtkGnssProvider = " + this.mMtkGnssProvider);
            this.mDownloadPsdsDataPending = 2;
            this.mNtpTimeHelper.setNtpTimeStateIdle();
        } catch (Exception e) {
            Log.w("GnssLocationProvider", "Failed to init mMtkGnssProvider!");
        }
    }

    private int mtkDeleteAidingData(Bundle extras, int flags) {
        if (this.mMtkGnssProvider != null) {
            if (extras != null) {
                if (extras.getBoolean("hot-still")) {
                    flags |= 8192;
                }
                if (extras.getBoolean("epo")) {
                    flags |= 16384;
                }
            }
            Log.d("GnssLocationProvider", "mtkDeleteAidingData extras:" + extras + "flags:" + flags);
        }
        return flags;
    }

    private void mtkInjectLastKnownLocation() {
        Location lastLocation;
        if (this.mMtkGnssProvider != null && (lastLocation = ((LocationManager) this.mContext.getSystemService("location")).getLastKnownLocation("network")) != null && System.currentTimeMillis() - lastLocation.getTime() < 600000) {
            this.mHandler.obtainMessage(7, 0, 0, lastLocation).sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    public void enableVerboseLogging() {
        boolean z = false;
        if (SystemProperties.getInt(MTK_DEBUG_GPSDBLOG_ENABLE_PROPERTY, 0) > 0) {
            z = true;
        }
        DEBUG = z;
        boolean z2 = DEBUG;
        VERBOSE = z2;
        native_delete_aiding_data(z2 ? 2048 : 4096);
        IPswNavigationStatusController iPswNavigationStatusController = this.mNavigationController;
        if (iPswNavigationStatusController != null) {
            iPswNavigationStatusController.setDebug(DEBUG);
        }
    }

    @Override // com.android.server.location.interfaces.IOppoGnssLocationProvider
    public void wakeGps() {
        startNavigating();
    }

    @Override // com.android.server.location.interfaces.IOppoGnssLocationProvider
    public void enterPSMode() {
        stopNavigating();
    }
}
