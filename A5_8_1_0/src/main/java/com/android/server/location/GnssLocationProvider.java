package com.android.server.location;

import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.database.Cursor;
import android.hardware.location.GeofenceHardwareImpl;
import android.location.FusedBatchOptions.SourceTechnologies;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.IGnssStatusListener;
import android.location.IGnssStatusProvider;
import android.location.IGnssStatusProvider.Stub;
import android.location.IGpsGeofenceHardware;
import android.location.ILocationManager;
import android.location.INetInitiatedListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest.Builder;
import android.net.Uri;
import android.net.util.NetworkConstants;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.PowerSaveState;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.WorkSource;
import android.provider.Settings.Global;
import android.provider.Telephony.Carriers;
import android.telephony.CarrierConfigManager;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionManager.OnSubscriptionsChangedListener;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;
import android.util.NtpTrustedTime;
import com.android.internal.app.IAppOpsService;
import com.android.internal.app.IBatteryStats;
import com.android.internal.location.GpsNetInitiatedHandler;
import com.android.internal.location.GpsNetInitiatedHandler.GpsNiNotification;
import com.android.internal.location.ProviderProperties;
import com.android.internal.location.ProviderRequest;
import com.android.internal.location.gnssmetrics.GnssMetrics;
import com.android.server.display.OppoBrightUtils;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import libcore.io.IoUtils;

public class GnssLocationProvider implements LocationProviderInterface {
    private static final int ADD_LISTENER = 8;
    private static final int AGPS_DATA_CONNECTION_CLOSED = 0;
    private static final int AGPS_DATA_CONNECTION_OPEN = 2;
    private static final int AGPS_DATA_CONNECTION_OPENING = 1;
    private static final int AGPS_REF_LOCATION_TYPE_GSM_CELLID = 1;
    private static final int AGPS_REF_LOCATION_TYPE_UMTS_CELLID = 2;
    private static final int AGPS_RIL_REQUEST_SETID_IMSI = 1;
    private static final int AGPS_RIL_REQUEST_SETID_MSISDN = 2;
    private static final int AGPS_SETID_TYPE_IMSI = 1;
    private static final int AGPS_SETID_TYPE_MSISDN = 2;
    private static final int AGPS_SETID_TYPE_NONE = 0;
    private static final int AGPS_SUPL_MODE_MSA = 2;
    private static final int AGPS_SUPL_MODE_MSB = 1;
    private static final int AGPS_TYPE_C2K = 2;
    private static final int AGPS_TYPE_SUPL = 1;
    private static final String ALARM_TIMEOUT = "com.android.internal.location.ALARM_TIMEOUT";
    private static final String ALARM_WAKEUP = "com.android.internal.location.ALARM_WAKEUP";
    private static final int APN_INVALID = 0;
    private static final int APN_IPV4 = 1;
    private static final int APN_IPV4V6 = 3;
    private static final int APN_IPV6 = 2;
    private static final int CHECK_LOCATION = 1;
    private static boolean DEBUG = Log.isLoggable("GnssLocationProvider", 3);
    private static final String DEBUG_PROPERTIES_FILE = "/etc/gps_debug.conf";
    private static final String DOWNLOAD_EXTRA_WAKELOCK_KEY = "GnssLocationProviderXtraDownload";
    private static final int DOWNLOAD_XTRA_DATA = 6;
    private static final int DOWNLOAD_XTRA_DATA_FINISHED = 11;
    private static final long DOWNLOAD_XTRA_DATA_TIMEOUT_MS = 60000;
    private static final int ENABLE = 2;
    private static final int GPS_AGPS_DATA_CONNECTED = 3;
    private static final int GPS_AGPS_DATA_CONN_DONE = 4;
    private static final int GPS_AGPS_DATA_CONN_FAILED = 5;
    private static final int GPS_CAPABILITY_GEOFENCING = 32;
    private static final int GPS_CAPABILITY_MEASUREMENTS = 64;
    private static final int GPS_CAPABILITY_MSA = 4;
    private static final int GPS_CAPABILITY_MSB = 2;
    private static final int GPS_CAPABILITY_NAV_MESSAGES = 128;
    private static final int GPS_CAPABILITY_ON_DEMAND_TIME = 16;
    private static final int GPS_CAPABILITY_SCHEDULING = 1;
    private static final int GPS_CAPABILITY_SINGLE_SHOT = 8;
    private static final int GPS_DELETE_ALL = 65535;
    private static final int GPS_DELETE_ALMANAC = 2;
    private static final int GPS_DELETE_CELLDB_INFO = 32768;
    private static final int GPS_DELETE_EPHEMERIS = 1;
    private static final int GPS_DELETE_HEALTH = 64;
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
    public static final String GPS_OPCUSTOM_POWERSAVE_OFF_FEATURE = "oppo.customize.function.gps_powersave_off";
    private static final int GPS_POLLING_THRESHOLD_INTERVAL = 10000;
    private static final int GPS_POSITION_MODE_MS_ASSISTED = 2;
    private static final int GPS_POSITION_MODE_MS_BASED = 1;
    private static final int GPS_POSITION_MODE_STANDALONE = 0;
    private static final int GPS_POSITION_RECURRENCE_PERIODIC = 0;
    private static final int GPS_POSITION_RECURRENCE_SINGLE = 1;
    private static final int GPS_RELEASE_AGPS_DATA_CONN = 2;
    private static final int GPS_REQUEST_AGPS_DATA_CONN = 1;
    private static final int GPS_START_LOG = 2048;
    private static final int GPS_STATUS_ENGINE_OFF = 4;
    private static final int GPS_STATUS_ENGINE_ON = 3;
    private static final int GPS_STATUS_NONE = 0;
    private static final int GPS_STATUS_SESSION_BEGIN = 1;
    private static final int GPS_STATUS_SESSION_END = 2;
    private static final int GPS_STOP_LOG = 4096;
    private static final int INITIALIZE_HANDLER = 13;
    private static final int INJECT_NTP_TIME = 5;
    private static final int INJECT_NTP_TIME_FINISHED = 10;
    private static final float ITAR_SPEED_LIMIT_METERS_PER_SECOND = 400.0f;
    private static final int LOCATION_HAS_ALTITUDE = 2;
    private static final int LOCATION_HAS_BEARING = 8;
    private static final int LOCATION_HAS_BEARING_ACCURACY = 128;
    private static final int LOCATION_HAS_HORIZONTAL_ACCURACY = 16;
    private static final int LOCATION_HAS_LAT_LONG = 1;
    private static final int LOCATION_HAS_SPEED = 4;
    private static final int LOCATION_HAS_SPEED_ACCURACY = 64;
    private static final int LOCATION_HAS_VERTICAL_ACCURACY = 32;
    private static final int LOCATION_INVALID = 0;
    private static final String LPP_PROFILE = "persist.sys.gps.lpp";
    private static final long MAX_RETRY_INTERVAL = 14400000;
    private static final int MAX_SVS = 64;
    private static final int NO_FIX_TIMEOUT = 60000;
    private static final long NTP_INTERVAL = 86400000;
    private static final ProviderProperties PROPERTIES = new ProviderProperties(true, true, false, false, true, true, true, 3, 1);
    private static final long RECENT_FIX_TIMEOUT = 10000;
    private static final int RELEASE_SUPL_CONNECTION = 15;
    private static final int REMOVE_LISTENER = 9;
    private static final int REQUEST_SUPL_CONNECTION = 14;
    private static final long RETRY_INTERVAL = 300000;
    private static final int SET_REQUEST = 3;
    private static final String SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    private static final int STATE_DOWNLOADING = 1;
    private static final int STATE_IDLE = 2;
    private static final int STATE_PENDING_NETWORK = 0;
    private static final int SUBSCRIPTION_OR_SIM_CHANGED = 12;
    private static final String TAG = "GnssLocationProvider";
    private static final int TCP_MAX_PORT = 65535;
    private static final int TCP_MIN_PORT = 0;
    private static final int UPDATE_LOCATION = 7;
    private static final int UPDATE_NETWORK_STATE = 4;
    private static boolean VERBOSE = Log.isLoggable("GnssLocationProvider", 2);
    private static final String WAKELOCK_KEY = "GnssLocationProvider";
    private InetAddress mAGpsDataConnectionIpAddr;
    private int mAGpsDataConnectionState;
    private final AlarmManager mAlarmManager;
    private final IAppOpsService mAppOpsService;
    private final IBatteryStats mBatteryStats;
    private final BroadcastReceiver mBroadcastReceiver;
    private String mC2KServerHost;
    private int mC2KServerPort;
    private WorkSource mClientSource;
    private float[] mCn0s;
    private final ConnectivityManager mConnMgr;
    private final Context mContext;
    private boolean mDisableGps;
    private int mDownloadXtraDataPending;
    private final WakeLock mDownloadXtraWakeLock;
    private boolean mEnabled;
    private int mEngineCapabilities;
    private boolean mEngineOn;
    private int mFixInterval;
    private long mFixRequestTime;
    private GeofenceHardwareImpl mGeofenceHardwareImpl;
    private final GnssMeasurementsProvider mGnssMeasurementsProvider;
    private GnssMetrics mGnssMetrics;
    private final GnssNavigationMessageProvider mGnssNavigationMessageProvider;
    private boolean mGnssPowerEnable;
    private final IGnssStatusProvider mGnssStatusProvider;
    private boolean mGpsAllowDataCollect;
    private IGpsGeofenceHardware mGpsGeofenceBinder;
    private Handler mHandler;
    private final ILocationManager mILocationManager;
    private int mInjectNtpTimePending;
    private boolean mIsFakeReport;
    private boolean mItarSpeedLimitExceeded;
    private long mLastFixTime;
    private final GnssStatusListenerHelper mListenerHelper;
    private Location mLocation;
    private Bundle mLocationExtras;
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;
    private Object mLock;
    private int mMaxCn0;
    private int mMeanCn0;
    private final GpsNetInitiatedHandler mNIHandler;
    private boolean mNavigating;
    private NavigationStatusController mNavigationController;
    private final INetInitiatedListener mNetInitiatedListener;
    private boolean mNetworkConnected;
    private final NetworkCallback mNetworkConnectivityCallback;
    private boolean mNetworkLocationRequestPending;
    private boolean mNetworkLocationStarted;
    private boolean mNetworkProviderEnabled;
    private byte[] mNmeaBuffer;
    private BackOff mNtpBackOff;
    private final NtpTrustedTime mNtpTime;
    private boolean mOnDemandTimeInjection;
    private final OnSubscriptionsChangedListener mOnSubscriptionsChangedListener;
    private OppoLocationBlacklist mOppoBlackList;
    private int mPositionMode;
    private final PowerManager mPowerManager;
    private Properties mProperties;
    private ProviderRequest mProviderRequest;
    private boolean mSingleShot;
    private float mSpeed;
    private boolean mStarted;
    private int mStatus;
    private long mStatusUpdateTime;
    private final NetworkCallback mSuplConnectivityCallback;
    private boolean mSuplEsEnabled;
    private String mSuplServerHost;
    private int mSuplServerPort;
    private boolean mSupportsXtra;
    private float[] mSvAzimuths;
    private float[] mSvCarrierFreqs;
    private int mSvCount;
    private float[] mSvElevations;
    private int[] mSvidWithFlags;
    private int mTimeToFirstFix;
    private final PendingIntent mTimeoutIntent;
    private final WakeLock mWakeLock;
    private final PendingIntent mWakeupIntent;
    private WorkSource mWorkSource;
    private BackOff mXtraBackOff;
    private int mYearOfHardware;

    interface SetCarrierProperty {
        boolean set(int i);
    }

    public interface GnssSystemInfoProvider {
        int getGnssYearOfHardware();
    }

    public interface GnssBatchingProvider {
        void flush();

        int getSize();

        boolean start(long j, boolean z);

        boolean stop();
    }

    public interface GnssMetricsProvider {
        String getGnssMetricsAsProtoString();
    }

    private static final class BackOff {
        private static final int MULTIPLIER = 2;
        private long mCurrentIntervalMillis = (this.mInitIntervalMillis / 2);
        private final long mInitIntervalMillis;
        private final long mMaxIntervalMillis;

        public BackOff(long initIntervalMillis, long maxIntervalMillis) {
            this.mInitIntervalMillis = initIntervalMillis;
            this.mMaxIntervalMillis = maxIntervalMillis;
        }

        public long nextBackoffMillis() {
            if (this.mCurrentIntervalMillis > this.mMaxIntervalMillis) {
                return this.mMaxIntervalMillis;
            }
            this.mCurrentIntervalMillis *= 2;
            return this.mCurrentIntervalMillis;
        }

        public void reset() {
            this.mCurrentIntervalMillis = this.mInitIntervalMillis / 2;
        }
    }

    private static class GpsRequest {
        public ProviderRequest request;
        public WorkSource source;

        public GpsRequest(ProviderRequest request, WorkSource source) {
            this.request = request;
            this.source = source;
        }
    }

    private final class NetworkLocationListener implements LocationListener {
        /* synthetic */ NetworkLocationListener(GnssLocationProvider this$0, NetworkLocationListener -this1) {
            this();
        }

        private NetworkLocationListener() {
        }

        public void onLocationChanged(Location location) {
            if ("network".equals(location.getProvider())) {
                GnssLocationProvider.this.handleUpdateLocation(location);
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    }

    private final class ProviderHandler extends Handler {
        public ProviderHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            int message = msg.what;
            switch (message) {
                case 2:
                    if (msg.arg1 != 1) {
                        GnssLocationProvider.this.handleDisable();
                        break;
                    } else {
                        GnssLocationProvider.this.handleEnable();
                        break;
                    }
                case 3:
                    GpsRequest gpsRequest = msg.obj;
                    GnssLocationProvider.this.handleSetRequest(gpsRequest.request, gpsRequest.source);
                    break;
                case 4:
                    GnssLocationProvider.this.handleUpdateNetworkState((Network) msg.obj);
                    break;
                case 5:
                    GnssLocationProvider.this.handleInjectNtpTime();
                    break;
                case 6:
                    GnssLocationProvider.this.handleDownloadXtraData();
                    break;
                case 7:
                    GnssLocationProvider.this.handleUpdateLocation((Location) msg.obj);
                    break;
                case 10:
                    GnssLocationProvider.this.mInjectNtpTimePending = 2;
                    break;
                case 11:
                    GnssLocationProvider.this.mDownloadXtraDataPending = 2;
                    break;
                case 12:
                    GnssLocationProvider.this.subscriptionOrSimChanged(GnssLocationProvider.this.mContext);
                    break;
                case 13:
                    handleInitialize();
                    break;
                case 14:
                    GnssLocationProvider.this.handleRequestSuplConnection((InetAddress) msg.obj);
                    break;
                case 15:
                    GnssLocationProvider.this.handleReleaseSuplConnection(msg.arg1);
                    break;
            }
            if (msg.arg2 == 1) {
                GnssLocationProvider.this.mWakeLock.release();
                if (GnssLocationProvider.DEBUG) {
                    Log.i("GnssLocationProvider", "WakeLock released by handleMessage(" + GnssLocationProvider.this.messageIdAsString(message) + ", " + msg.arg1 + ", " + msg.obj + ")");
                }
            }
        }

        private void handleInitialize() {
            IntentFilter intentFilter;
            GnssLocationProvider.this.reloadGpsProperties(GnssLocationProvider.this.mContext, GnssLocationProvider.this.mProperties);
            SubscriptionManager.from(GnssLocationProvider.this.mContext).addOnSubscriptionsChangedListener(GnssLocationProvider.this.mOnSubscriptionsChangedListener);
            if (GnssLocationProvider.native_is_agps_ril_supported()) {
                intentFilter = new IntentFilter();
                intentFilter.addAction("android.intent.action.DATA_SMS_RECEIVED");
                intentFilter.addDataScheme("sms");
                intentFilter.addDataAuthority("localhost", "7275");
                GnssLocationProvider.this.mContext.registerReceiver(GnssLocationProvider.this.mBroadcastReceiver, intentFilter, null, this);
                intentFilter = new IntentFilter();
                intentFilter.addAction("android.provider.Telephony.WAP_PUSH_RECEIVED");
                try {
                    intentFilter.addDataType("application/vnd.omaloc-supl-init");
                } catch (MalformedMimeTypeException e) {
                    Log.w("GnssLocationProvider", "Malformed SUPL init mime type");
                }
                GnssLocationProvider.this.mContext.registerReceiver(GnssLocationProvider.this.mBroadcastReceiver, intentFilter, null, this);
            } else if (GnssLocationProvider.DEBUG) {
                Log.d("GnssLocationProvider", "Skipped registration for SMS/WAP-PUSH messages because AGPS Ril in GPS HAL is not supported");
            }
            intentFilter = new IntentFilter();
            intentFilter.addAction(GnssLocationProvider.ALARM_WAKEUP);
            intentFilter.addAction(GnssLocationProvider.ALARM_TIMEOUT);
            intentFilter.addAction("android.os.action.POWER_SAVE_MODE_CHANGED");
            intentFilter.addAction("android.os.action.DEVICE_IDLE_MODE_CHANGED");
            intentFilter.addAction("android.intent.action.SCREEN_OFF");
            intentFilter.addAction("android.intent.action.SCREEN_ON");
            intentFilter.addAction(GnssLocationProvider.SIM_STATE_CHANGED);
            GnssLocationProvider.this.mContext.registerReceiver(GnssLocationProvider.this.mBroadcastReceiver, intentFilter, null, this);
            Builder networkRequestBuilder = new Builder();
            networkRequestBuilder.addTransportType(0);
            networkRequestBuilder.addTransportType(1);
            if (GnssLocationProvider.this.mContext.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                networkRequestBuilder.addTransportType(2);
            }
            GnssLocationProvider.this.mConnMgr.registerNetworkCallback(networkRequestBuilder.build(), GnssLocationProvider.this.mNetworkConnectivityCallback);
            LocationManager locManager = (LocationManager) GnssLocationProvider.this.mContext.getSystemService("location");
            LocationRequest request = LocationRequest.createFromDeprecatedProvider("passive", 0, OppoBrightUtils.MIN_LUX_LIMITI, false);
            request.setHideFromAppOps(true);
            locManager.requestLocationUpdates(request, new NetworkLocationListener(GnssLocationProvider.this, null), getLooper());
            GnssLocationProvider.this.mNavigationController.init();
        }
    }

    private static native void class_init_native();

    private static native boolean native_add_geofence(int i, double d, double d2, double d3, int i2, int i3, int i4, int i5);

    private native void native_agps_data_conn_closed();

    private native void native_agps_data_conn_failed();

    private native void native_agps_data_conn_open(String str, int i);

    private native void native_agps_ni_message(byte[] bArr, int i);

    private native void native_agps_set_id(int i, String str);

    private native void native_agps_set_ref_location_cellid(int i, int i2, int i3, int i4, int i5);

    private native void native_cleanup();

    private static native void native_cleanup_batching();

    private native void native_delete_aiding_data(int i);

    private static native void native_flush_batch();

    private static native int native_get_batch_size();

    private native String native_get_internal_state();

    private native boolean native_init();

    private static native boolean native_init_batching();

    private native void native_inject_location(double d, double d2, float f);

    private native void native_inject_time(long j, long j2, int i);

    private native void native_inject_xtra_data(byte[] bArr, int i);

    private static native boolean native_is_agps_ril_supported();

    private static native boolean native_is_geofence_supported();

    private static native boolean native_is_gnss_configuration_supported();

    private static native boolean native_is_measurement_supported();

    private static native boolean native_is_navigation_message_supported();

    private static native boolean native_is_supported();

    private static native boolean native_pause_geofence(int i);

    private native int native_read_nmea(byte[] bArr, int i);

    private native int native_read_sv_status(int[] iArr, float[] fArr, float[] fArr2, float[] fArr3, float[] fArr4);

    private static native boolean native_remove_geofence(int i);

    private static native boolean native_resume_geofence(int i, int i2);

    private native void native_send_ni_response(int i, int i2);

    private native void native_set_agps_server(int i, String str, int i2);

    private static native boolean native_set_emergency_supl_pdn(int i);

    private static native boolean native_set_gnss_pos_protocol_select(int i);

    private static native boolean native_set_gps_lock(int i);

    private static native boolean native_set_lpp_profile(int i);

    private native boolean native_set_position_mode(int i, int i2, int i3, int i4, int i5);

    private static native boolean native_set_supl_es(int i);

    private static native boolean native_set_supl_mode(int i);

    private static native boolean native_set_supl_version(int i);

    private native boolean native_start();

    private static native boolean native_start_batch(long j, boolean z);

    private native boolean native_start_measurement_collection();

    private native boolean native_start_navigation_message_collection();

    private native boolean native_stop();

    private static native boolean native_stop_batch();

    private native boolean native_stop_measurement_collection();

    private native boolean native_stop_navigation_message_collection();

    private native boolean native_supports_xtra();

    private native void native_update_network_state(boolean z, int i, boolean z2, boolean z3, String str, String str2);

    static {
        class_init_native();
    }

    public IGnssStatusProvider getGnssStatusProvider() {
        return this.mGnssStatusProvider;
    }

    public IGpsGeofenceHardware getGpsGeofenceProxy() {
        return this.mGpsGeofenceBinder;
    }

    public GnssMeasurementsProvider getGnssMeasurementsProvider() {
        return this.mGnssMeasurementsProvider;
    }

    public GnssNavigationMessageProvider getGnssNavigationMessageProvider() {
        return this.mGnssNavigationMessageProvider;
    }

    private void subscriptionOrSimChanged(Context context) {
        if (DEBUG) {
            Log.d("GnssLocationProvider", "received SIM related action: ");
        }
        CarrierConfigManager configManager = (CarrierConfigManager) this.mContext.getSystemService("carrier_config");
        String mccMnc = ((TelephonyManager) this.mContext.getSystemService("phone")).getSimOperator();
        boolean isKeepLppProfile = false;
        if (!TextUtils.isEmpty(mccMnc)) {
            if (DEBUG) {
                Log.d("GnssLocationProvider", "SIM MCC/MNC is available: " + mccMnc);
            }
            this.mGpsAllowDataCollect = !mccMnc.startsWith("404") ? mccMnc.startsWith("405") : true;
            synchronized (this.mLock) {
                if (configManager != null) {
                    PersistableBundle bundle = configManager.getConfig();
                    if (bundle != null) {
                        isKeepLppProfile = bundle.getBoolean("persist_lpp_mode_bool");
                    }
                }
                if (isKeepLppProfile) {
                    loadPropertiesFromResource(context, this.mProperties);
                    String lpp_profile = this.mProperties.getProperty("LPP_PROFILE");
                    if (lpp_profile != null) {
                        SystemProperties.set(LPP_PROFILE, lpp_profile);
                    }
                } else {
                    SystemProperties.set(LPP_PROFILE, "");
                }
                reloadGpsProperties(context, this.mProperties);
                this.mNIHandler.setSuplEsEnabled(this.mSuplEsEnabled);
            }
        } else if (DEBUG) {
            Log.d("GnssLocationProvider", "SIM MCC/MNC is still not available");
        }
    }

    private void updateLowPowerMode() {
        boolean disableGps = this.mPowerManager.isDeviceIdleMode();
        PowerSaveState result = this.mPowerManager.getPowerSaveState(1);
        switch (result.gpsMode) {
            case 1:
                disableGps |= result.batterySaverEnabled ? this.mPowerManager.isInteractive() ^ 1 : 0;
                break;
        }
        if (disableGps != this.mDisableGps) {
            this.mDisableGps = disableGps;
            updateRequirements();
        }
    }

    public static boolean isSupported() {
        return native_is_supported();
    }

    private void reloadGpsProperties(Context context, Properties properties) {
        if (DEBUG) {
            Log.d("GnssLocationProvider", "Reset GPS properties, previous size = " + properties.size());
        }
        loadPropertiesFromResource(context, properties);
        String lpp_prof = SystemProperties.get(LPP_PROFILE);
        if (!TextUtils.isEmpty(lpp_prof)) {
            properties.setProperty("LPP_PROFILE", lpp_prof);
        }
        loadPropertiesFromFile(DEBUG_PROPERTIES_FILE, properties);
        String strEnabled = properties.getProperty("POWER_SAVER_ENABLED");
        if (strEnabled != null) {
            this.mGnssPowerEnable = strEnabled.equals("enabled");
        } else {
            this.mGnssPowerEnable = context.getPackageManager().hasSystemFeature(GPS_OPCUSTOM_POWERSAVE_OFF_FEATURE) ^ 1;
        }
        if (DEBUG) {
            Log.d("GnssLocationProvider", "PSE:" + this.mGnssPowerEnable);
        }
        setSuplHostPort(properties.getProperty("SUPL_HOST"), properties.getProperty("SUPL_PORT"));
        this.mC2KServerHost = properties.getProperty("C2K_HOST");
        String portString = properties.getProperty("C2K_PORT");
        if (!(this.mC2KServerHost == null || portString == null)) {
            try {
                this.mC2KServerPort = Integer.parseInt(portString);
            } catch (NumberFormatException e) {
                Log.e("GnssLocationProvider", "unable to parse C2K_PORT: " + portString);
            }
        }
        if (native_is_gnss_configuration_supported()) {
            for (Entry<String, SetCarrierProperty> entry : new HashMap<String, SetCarrierProperty>() {
                {
                    put("SUPL_VER", -$Lambda$LbPzwzo3JyvLa845qcqGRfVQJq4.$INST$0);
                    put("SUPL_MODE", -$Lambda$LbPzwzo3JyvLa845qcqGRfVQJq4.$INST$1);
                    put("SUPL_ES", -$Lambda$LbPzwzo3JyvLa845qcqGRfVQJq4.$INST$2);
                    put("LPP_PROFILE", -$Lambda$LbPzwzo3JyvLa845qcqGRfVQJq4.$INST$3);
                    put("A_GLONASS_POS_PROTOCOL_SELECT", -$Lambda$LbPzwzo3JyvLa845qcqGRfVQJq4.$INST$4);
                    put("USE_EMERGENCY_PDN_FOR_EMERGENCY_SUPL", -$Lambda$LbPzwzo3JyvLa845qcqGRfVQJq4.$INST$5);
                    put("GPS_LOCK", -$Lambda$LbPzwzo3JyvLa845qcqGRfVQJq4.$INST$6);
                }
            }.entrySet()) {
                String propertyName = (String) entry.getKey();
                String propertyValueString = properties.getProperty(propertyName);
                if (propertyValueString != null) {
                    try {
                        if (!((SetCarrierProperty) entry.getValue()).set(Integer.decode(propertyValueString).intValue())) {
                            Log.e("GnssLocationProvider", "Unable to set " + propertyName);
                        }
                    } catch (NumberFormatException e2) {
                        Log.e("GnssLocationProvider", "unable to parse propertyName: " + propertyValueString);
                    }
                }
            }
        } else if (DEBUG) {
            Log.d("GnssLocationProvider", "Skipped configuration update because GNSS configuration in GPS HAL is not supported");
        }
        String suplESProperty = this.mProperties.getProperty("SUPL_ES");
        if (suplESProperty != null) {
            try {
                this.mSuplEsEnabled = Integer.parseInt(suplESProperty) == 1;
            } catch (NumberFormatException e3) {
                Log.e("GnssLocationProvider", "unable to parse SUPL_ES: " + suplESProperty);
            }
        }
    }

    private void loadPropertiesFromResource(Context context, Properties properties) {
        for (String item : context.getResources().getStringArray(17236013)) {
            if (DEBUG) {
                Log.d("GnssLocationProvider", "GpsParamsResource: " + item);
            }
            String[] split = item.split("=");
            if (split.length == 2) {
                properties.setProperty(split[0].trim().toUpperCase(), split[1]);
            } else {
                Log.w("GnssLocationProvider", "malformed contents: " + item);
            }
        }
    }

    private boolean loadPropertiesFromFile(String filename, Properties properties) {
        Throwable th;
        try {
            FileInputStream stream = null;
            try {
                FileInputStream stream2 = new FileInputStream(new File(filename));
                try {
                    properties.load(stream2);
                    IoUtils.closeQuietly(stream2);
                    return true;
                } catch (Throwable th2) {
                    th = th2;
                    stream = stream2;
                    IoUtils.closeQuietly(stream);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                IoUtils.closeQuietly(stream);
                throw th;
            }
        } catch (IOException e) {
            if (DEBUG) {
                Log.d("GnssLocationProvider", "Could not open GPS configuration file " + filename);
            }
            return false;
        }
    }

    public GnssLocationProvider(Context context, ILocationManager ilocationManager, Looper looper) {
        this.mGnssPowerEnable = true;
        this.mGpsAllowDataCollect = false;
        this.mLock = new Object();
        this.mStatus = 1;
        this.mStatusUpdateTime = SystemClock.elapsedRealtime();
        this.mNtpBackOff = new BackOff(300000, MAX_RETRY_INTERVAL);
        this.mXtraBackOff = new BackOff(300000, MAX_RETRY_INTERVAL);
        this.mInjectNtpTimePending = 0;
        this.mDownloadXtraDataPending = 0;
        this.mFixInterval = 1000;
        this.mFixRequestTime = 0;
        this.mTimeToFirstFix = 0;
        this.mProviderRequest = null;
        this.mWorkSource = null;
        this.mDisableGps = false;
        this.mSuplServerPort = 0;
        this.mSuplEsEnabled = false;
        this.mLocation = new Location("gps");
        this.mLocationExtras = new Bundle();
        this.mClientSource = new WorkSource();
        this.mYearOfHardware = 0;
        this.mItarSpeedLimitExceeded = false;
        this.mGnssStatusProvider = new Stub() {
            public void registerGnssStatusCallback(IGnssStatusListener callback) {
                GnssLocationProvider.this.mListenerHelper.addListener(callback);
            }

            public void unregisterGnssStatusCallback(IGnssStatusListener callback) {
                GnssLocationProvider.this.mListenerHelper.removeListener(callback);
            }
        };
        this.mNetworkConnectivityCallback = new NetworkCallback() {
            public void onAvailable(Network network) {
                if (GnssLocationProvider.this.mInjectNtpTimePending == 0) {
                    GnssLocationProvider.this.requestUtcTime();
                }
                if (GnssLocationProvider.this.mDownloadXtraDataPending == 0) {
                    GnssLocationProvider.this.xtraDownloadRequest();
                }
                GnssLocationProvider.this.sendMessage(4, 0, network);
            }

            public void onLost(Network network) {
                GnssLocationProvider.this.sendMessage(4, 0, network);
            }
        };
        this.mSuplConnectivityCallback = new NetworkCallback() {
            public void onAvailable(Network network) {
                GnssLocationProvider.this.sendMessage(4, 0, network);
            }

            public void onLost(Network network) {
                GnssLocationProvider.this.releaseSuplConnection(2);
            }
        };
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (GnssLocationProvider.DEBUG) {
                    Log.d("GnssLocationProvider", "receive broadcast intent, action: " + action);
                }
                if (action != null) {
                    if (action.equals(GnssLocationProvider.ALARM_WAKEUP)) {
                        GnssLocationProvider.this.startNavigating(false);
                    } else if (action.equals(GnssLocationProvider.ALARM_TIMEOUT)) {
                        GnssLocationProvider.this.hibernate();
                    } else if ("android.os.action.POWER_SAVE_MODE_CHANGED".equals(action) || "android.os.action.DEVICE_IDLE_MODE_CHANGED".equals(action) || "android.intent.action.SCREEN_OFF".equals(action) || "android.intent.action.SCREEN_ON".equals(action)) {
                        GnssLocationProvider.this.updateLowPowerMode();
                    } else if (action.equals(GnssLocationProvider.SIM_STATE_CHANGED)) {
                        GnssLocationProvider.this.subscriptionOrSimChanged(context);
                    }
                }
            }
        };
        this.mOnSubscriptionsChangedListener = new OnSubscriptionsChangedListener() {
            public void onSubscriptionsChanged() {
                GnssLocationProvider.this.sendMessage(12, 0, null);
            }
        };
        this.mOppoBlackList = null;
        this.mGpsGeofenceBinder = new IGpsGeofenceHardware.Stub() {
            public boolean isHardwareGeofenceSupported() {
                return GnssLocationProvider.native_is_geofence_supported();
            }

            public boolean addCircularHardwareGeofence(int geofenceId, double latitude, double longitude, double radius, int lastTransition, int monitorTransitions, int notificationResponsiveness, int unknownTimer) {
                return GnssLocationProvider.native_add_geofence(geofenceId, latitude, longitude, radius, lastTransition, monitorTransitions, notificationResponsiveness, unknownTimer);
            }

            public boolean removeHardwareGeofence(int geofenceId) {
                return GnssLocationProvider.native_remove_geofence(geofenceId);
            }

            public boolean pauseHardwareGeofence(int geofenceId) {
                return GnssLocationProvider.native_pause_geofence(geofenceId);
            }

            public boolean resumeHardwareGeofence(int geofenceId, int monitorTransition) {
                return GnssLocationProvider.native_resume_geofence(geofenceId, monitorTransition);
            }
        };
        this.mNetInitiatedListener = new INetInitiatedListener.Stub() {
            public boolean sendNiResponse(int notificationId, int userResponse) {
                if (GnssLocationProvider.DEBUG) {
                    Log.d("GnssLocationProvider", "sendNiResponse, notifId: " + notificationId + ", response: " + userResponse);
                }
                GnssLocationProvider.this.native_send_ni_response(notificationId, userResponse);
                return true;
            }
        };
        this.mSpeed = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mIsFakeReport = true;
        this.mSvidWithFlags = new int[64];
        this.mCn0s = new float[64];
        this.mSvElevations = new float[64];
        this.mSvAzimuths = new float[64];
        this.mSvCarrierFreqs = new float[64];
        this.mNmeaBuffer = new byte[120];
        this.mNetworkLocationStarted = false;
        this.mNetworkProviderEnabled = true;
        this.mNetworkConnected = false;
        this.mNetworkLocationRequestPending = false;
        this.mLocationManager = null;
        this.mLocationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                if (GnssLocationProvider.DEBUG) {
                    Log.d("GnssLocationProvider", "isFakeReport:" + GnssLocationProvider.this.mIsFakeReport + " acc:" + location.getAccuracy());
                }
                if (GnssLocationProvider.this.mIsFakeReport && location.hasAccuracy() && location.getAccuracy() < 100.0f) {
                    Location mLoc = new Location(location);
                    mLoc.setProvider("gps");
                    mLoc.makeComplete();
                    try {
                        GnssLocationProvider.this.mILocationManager.reportLocation(mLoc, false);
                    } catch (RemoteException e) {
                        Log.e("GnssLocationProvider", "RemoteException calling reportLocation");
                    }
                }
            }

            public void onProviderDisabled(String provider) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };
        this.mContext = context;
        this.mNtpTime = NtpTrustedTime.getInstance(context);
        this.mILocationManager = ilocationManager;
        this.mLocation.setExtras(this.mLocationExtras);
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mWakeLock = this.mPowerManager.newWakeLock(1, "GnssLocationProvider");
        this.mWakeLock.setReferenceCounted(true);
        this.mDownloadXtraWakeLock = this.mPowerManager.newWakeLock(1, DOWNLOAD_EXTRA_WAKELOCK_KEY);
        this.mDownloadXtraWakeLock.setReferenceCounted(true);
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mWakeupIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ALARM_WAKEUP), 0);
        this.mTimeoutIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ALARM_TIMEOUT), 0);
        this.mConnMgr = (ConnectivityManager) context.getSystemService("connectivity");
        this.mAppOpsService = IAppOpsService.Stub.asInterface(ServiceManager.getService("appops"));
        this.mBatteryStats = IBatteryStats.Stub.asInterface(ServiceManager.getService("batterystats"));
        this.mHandler = new ProviderHandler(looper);
        this.mNavigationController = new NavigationStatusController(this.mContext, this, this.mILocationManager);
        this.mNavigationController.setDebug(DEBUG);
        this.mProperties = new Properties();
        sendMessage(13, 0, null);
        this.mNIHandler = new GpsNetInitiatedHandler(context, this.mNetInitiatedListener, this.mSuplEsEnabled);
        this.mListenerHelper = new GnssStatusListenerHelper(this.mHandler) {
            protected boolean isAvailableInPlatform() {
                return GnssLocationProvider.isSupported();
            }

            protected boolean isGpsEnabled() {
                return GnssLocationProvider.this.isEnabled();
            }
        };
        this.mGnssMeasurementsProvider = new GnssMeasurementsProvider(this.mHandler) {
            public boolean isAvailableInPlatform() {
                return GnssLocationProvider.native_is_measurement_supported();
            }

            protected boolean registerWithService() {
                return GnssLocationProvider.this.native_start_measurement_collection();
            }

            protected void unregisterFromService() {
                GnssLocationProvider.this.native_stop_measurement_collection();
            }

            protected boolean isGpsEnabled() {
                return GnssLocationProvider.this.isEnabled();
            }
        };
        this.mGnssNavigationMessageProvider = new GnssNavigationMessageProvider(this.mHandler) {
            protected boolean isAvailableInPlatform() {
                return GnssLocationProvider.native_is_navigation_message_supported();
            }

            protected boolean registerWithService() {
                return GnssLocationProvider.this.native_start_navigation_message_collection();
            }

            protected void unregisterFromService() {
                GnssLocationProvider.this.native_stop_navigation_message_collection();
            }

            protected boolean isGpsEnabled() {
                return GnssLocationProvider.this.isEnabled();
            }
        };
        this.mGnssMetrics = new GnssMetrics();
        if (native_init()) {
            native_cleanup();
        } else {
            Log.d("GnssLocationProvider", "Failed to initialize at bootup");
        }
        this.mGnssPowerEnable = context.getPackageManager().hasSystemFeature(GPS_OPCUSTOM_POWERSAVE_OFF_FEATURE) ^ 1;
        if (DEBUG) {
            Log.d("GnssLocationProvider", " mGpsPowerSaveFeatureDisable : " + this.mGnssPowerEnable);
        }
    }

    public GnssLocationProvider(Context context, ILocationManager ilocationManager, OppoLocationBlacklist oppoBlackList, Looper looper) {
        this(context, ilocationManager, looper);
        this.mOppoBlackList = oppoBlackList;
    }

    public String getName() {
        return "gps";
    }

    public ProviderProperties getProperties() {
        return PROPERTIES;
    }

    private void handleUpdateNetworkState(Network network) {
        NetworkInfo info = this.mConnMgr.getNetworkInfo(network);
        boolean networkAvailable = false;
        boolean isConnected = false;
        int type = -1;
        boolean isRoaming = false;
        String apnName = null;
        if (info != null) {
            networkAvailable = info.isAvailable() ? TelephonyManager.getDefault().getDataEnabled() : false;
            isConnected = info.isConnected();
            type = info.getType();
            isRoaming = info.isRoaming();
            apnName = info.getExtraInfo();
            networkLocationHandleByNetworkState(info);
        }
        if (DEBUG) {
            Log.d("GnssLocationProvider", String.format("UpdateNetworkState, state=%s, connected=%s, info=%s, capabilities=%S", new Object[]{agpsDataConnStateAsString(), Boolean.valueOf(isConnected), info, this.mConnMgr.getNetworkCapabilities(network)}));
        }
        if (native_is_agps_ril_supported()) {
            String defaultApn = getSelectedApn();
            if (defaultApn == null) {
                defaultApn = "dummy-apn";
            }
            native_update_network_state(isConnected, type, isRoaming, networkAvailable, apnName, defaultApn);
        } else if (DEBUG) {
            Log.d("GnssLocationProvider", "Skipped network state update because GPS HAL AGPS-RIL is not  supported");
        }
        if (this.mAGpsDataConnectionState != 1) {
            return;
        }
        if (isConnected) {
            if (apnName == null) {
                apnName = "dummy-apn";
            }
            int apnIpType = getApnIpType(apnName);
            setRouting();
            if (DEBUG) {
                Log.d("GnssLocationProvider", String.format("native_agps_data_conn_open: mAgpsApn=%s, mApnIpType=%s", new Object[]{apnName, Integer.valueOf(apnIpType)}));
            }
            native_agps_data_conn_open(apnName, apnIpType);
            this.mAGpsDataConnectionState = 2;
            return;
        }
        handleReleaseSuplConnection(5);
    }

    private void handleRequestSuplConnection(InetAddress address) {
        if (DEBUG) {
            Log.d("GnssLocationProvider", String.format("requestSuplConnection, state=%s, address=%s", new Object[]{agpsDataConnStateAsString(), address}));
        }
        if (this.mAGpsDataConnectionState == 0) {
            this.mAGpsDataConnectionIpAddr = address;
            this.mAGpsDataConnectionState = 1;
            Builder requestBuilder = new Builder();
            requestBuilder.addTransportType(0);
            requestBuilder.addCapability(1);
            this.mConnMgr.requestNetwork(requestBuilder.build(), this.mSuplConnectivityCallback);
        }
    }

    private void handleReleaseSuplConnection(int agpsDataConnStatus) {
        if (DEBUG) {
            Log.d("GnssLocationProvider", String.format("releaseSuplConnection, state=%s, status=%s", new Object[]{agpsDataConnStateAsString(), agpsDataConnStatusAsString(agpsDataConnStatus)}));
        }
        if (this.mAGpsDataConnectionState != 0) {
            this.mAGpsDataConnectionState = 0;
            this.mConnMgr.unregisterNetworkCallback(this.mSuplConnectivityCallback);
            switch (agpsDataConnStatus) {
                case 2:
                    native_agps_data_conn_closed();
                    break;
                case 5:
                    native_agps_data_conn_failed();
                    break;
                default:
                    Log.e("GnssLocationProvider", "Invalid status to release SUPL connection: " + agpsDataConnStatus);
                    break;
            }
        }
    }

    private void handleInjectNtpTime() {
        if (this.mInjectNtpTimePending != 1) {
            if (isDataNetworkConnected()) {
                this.mInjectNtpTimePending = 1;
                this.mWakeLock.acquire();
                Log.i("GnssLocationProvider", "WakeLock acquired by handleInjectNtpTime()");
                AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                    public void run() {
                        long delay;
                        boolean refreshSuccess = true;
                        if (GnssLocationProvider.this.mNtpTime.getCacheAge() >= 86400000) {
                            refreshSuccess = GnssLocationProvider.this.mNtpTime.forceRefresh();
                        }
                        if (GnssLocationProvider.this.mNtpTime.getCacheAge() < 86400000) {
                            long time = GnssLocationProvider.this.mNtpTime.getCachedNtpTime();
                            long timeReference = GnssLocationProvider.this.mNtpTime.getCachedNtpTimeReference();
                            long certainty = GnssLocationProvider.this.mNtpTime.getCacheCertainty();
                            if (GnssLocationProvider.DEBUG) {
                                Log.d("GnssLocationProvider", "NTP server returned: " + time + " (" + new Date(time) + ") reference: " + timeReference + " certainty: " + certainty + " system time offset: " + (time - System.currentTimeMillis()));
                            }
                            GnssLocationProvider.this.native_inject_time(time, timeReference, (int) certainty);
                            delay = 86400000;
                            GnssLocationProvider.this.mNtpBackOff.reset();
                        } else {
                            Log.e("GnssLocationProvider", "requestTime failed");
                            delay = GnssLocationProvider.this.mNtpBackOff.nextBackoffMillis();
                        }
                        GnssLocationProvider.this.sendMessage(10, 0, null);
                        if (GnssLocationProvider.DEBUG) {
                            Log.d("GnssLocationProvider", String.format("onDemandTimeInjection=%s, refreshSuccess=%s, delay=%s", new Object[]{Boolean.valueOf(GnssLocationProvider.this.mOnDemandTimeInjection), Boolean.valueOf(refreshSuccess), Long.valueOf(delay)}));
                        }
                        if (GnssLocationProvider.this.mOnDemandTimeInjection || (refreshSuccess ^ 1) != 0) {
                            GnssLocationProvider.this.mHandler.sendEmptyMessageDelayed(5, delay);
                        }
                        GnssLocationProvider.this.mWakeLock.release();
                        Log.i("GnssLocationProvider", "WakeLock released by handleInjectNtpTime()");
                    }
                });
                return;
            }
            this.mInjectNtpTimePending = 0;
        }
    }

    private void handleDownloadXtraData() {
        if (!this.mSupportsXtra) {
            Log.d("GnssLocationProvider", "handleDownloadXtraData() called when Xtra not supported");
        } else if (this.mDownloadXtraDataPending != 1) {
            if (isDataNetworkConnected()) {
                this.mDownloadXtraDataPending = 1;
                this.mDownloadXtraWakeLock.acquire(60000);
                Log.i("GnssLocationProvider", "WakeLock acquired by handleDownloadXtraData()");
                AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                    public void run() {
                        byte[] data = new GpsXtraDownloader(GnssLocationProvider.this.mProperties).downloadXtraData();
                        if (data != null) {
                            if (GnssLocationProvider.DEBUG) {
                                Log.d("GnssLocationProvider", "calling native_inject_xtra_data");
                            }
                            GnssLocationProvider.this.native_inject_xtra_data(data, data.length);
                            GnssLocationProvider.this.mXtraBackOff.reset();
                        }
                        GnssLocationProvider.this.sendMessage(11, 0, null);
                        if (data == null) {
                            GnssLocationProvider.this.mHandler.sendEmptyMessageDelayed(6, GnssLocationProvider.this.mXtraBackOff.nextBackoffMillis());
                        }
                        synchronized (GnssLocationProvider.this.mLock) {
                            if (GnssLocationProvider.this.mDownloadXtraWakeLock.isHeld()) {
                                try {
                                    GnssLocationProvider.this.mDownloadXtraWakeLock.release();
                                    if (GnssLocationProvider.DEBUG) {
                                        Log.d("GnssLocationProvider", "WakeLock released by handleDownloadXtraData()");
                                    }
                                } catch (Exception e) {
                                    Log.i("GnssLocationProvider", "Wakelock timeout & release race exception in handleDownloadXtraData()", e);
                                }
                            } else {
                                Log.e("GnssLocationProvider", "WakeLock expired before release in handleDownloadXtraData()");
                            }
                        }
                        return;
                    }
                });
                return;
            }
            this.mDownloadXtraDataPending = 0;
        }
    }

    private void handleUpdateLocation(Location location) {
        if (location.hasAccuracy()) {
            native_inject_location(location.getLatitude(), location.getLongitude(), location.getAccuracy());
        }
    }

    public void enable() {
        synchronized (this.mLock) {
            if (this.mEnabled) {
                return;
            }
            this.mEnabled = true;
            sendMessage(2, 1, null);
        }
    }

    private void setSuplHostPort(String hostString, String portString) {
        if (hostString != null) {
            this.mSuplServerHost = hostString;
        }
        if (portString != null) {
            try {
                this.mSuplServerPort = Integer.parseInt(portString);
            } catch (NumberFormatException e) {
                Log.e("GnssLocationProvider", "unable to parse SUPL_PORT: " + portString);
            }
        }
        if (this.mSuplServerHost != null && this.mSuplServerPort > 0 && this.mSuplServerPort <= NetworkConstants.ARP_HWTYPE_RESERVED_HI) {
            native_set_agps_server(1, this.mSuplServerHost, this.mSuplServerPort);
        }
    }

    private int getSuplMode(Properties properties, boolean agpsEnabled, boolean singleShot) {
        if (agpsEnabled) {
            String modeString = properties.getProperty("SUPL_MODE");
            int suplMode = 0;
            if (!TextUtils.isEmpty(modeString)) {
                try {
                    suplMode = Integer.parseInt(modeString);
                } catch (NumberFormatException e) {
                    Log.e("GnssLocationProvider", "unable to parse SUPL_MODE: " + modeString);
                    return 0;
                }
            }
            if (!hasCapability(2) || (suplMode & 1) == 0) {
                return (singleShot && hasCapability(4) && (suplMode & 2) != 0) ? 2 : 0;
            } else {
                return 1;
            }
        }
    }

    private void handleEnable() {
        if (DEBUG) {
            Log.d("GnssLocationProvider", "handleEnable");
        }
        if (native_init()) {
            this.mSupportsXtra = native_supports_xtra();
            if (this.mSuplServerHost != null) {
                native_set_agps_server(1, this.mSuplServerHost, this.mSuplServerPort);
            }
            if (this.mC2KServerHost != null) {
                native_set_agps_server(2, this.mC2KServerHost, this.mC2KServerPort);
            }
            this.mGnssMeasurementsProvider.onGpsEnabledChanged();
            this.mGnssNavigationMessageProvider.onGpsEnabledChanged();
            enableBatching();
            return;
        }
        synchronized (this.mLock) {
            this.mEnabled = false;
        }
        Log.w("GnssLocationProvider", "Failed to enable location provider");
    }

    public void disable() {
        synchronized (this.mLock) {
            if (this.mEnabled) {
                this.mEnabled = false;
                sendMessage(2, 0, null);
                return;
            }
        }
    }

    private void handleDisable() {
        if (DEBUG) {
            Log.d("GnssLocationProvider", "handleDisable");
        }
        updateClientUids(new WorkSource());
        this.mNavigationController.stopController();
        stopNavigating();
        this.mAlarmManager.cancel(this.mWakeupIntent);
        this.mAlarmManager.cancel(this.mTimeoutIntent);
        disableBatching();
        native_cleanup();
        this.mGnssMeasurementsProvider.onGpsEnabledChanged();
        this.mGnssNavigationMessageProvider.onGpsEnabledChanged();
    }

    public boolean isEnabled() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mEnabled;
        }
        return z;
    }

    public int getStatus(Bundle extras) {
        setLocationExtras(extras);
        return this.mStatus;
    }

    private void updateStatus(int status, int svCount, int meanCn0, int maxCn0) {
        if (status != this.mStatus || svCount != this.mSvCount || meanCn0 != this.mMeanCn0 || maxCn0 != this.mMaxCn0) {
            this.mStatus = status;
            this.mSvCount = svCount;
            this.mMeanCn0 = meanCn0;
            this.mMaxCn0 = maxCn0;
            setLocationExtras(this.mLocationExtras);
            this.mStatusUpdateTime = SystemClock.elapsedRealtime();
        }
    }

    private void setLocationExtras(Bundle extras) {
        if (extras != null) {
            extras.putInt("satellites", this.mSvCount);
            extras.putInt("meanCn0", this.mMeanCn0);
            extras.putInt("maxCn0", this.mMaxCn0);
        }
    }

    public long getStatusUpdateTime() {
        return this.mStatusUpdateTime;
    }

    public void setRequest(ProviderRequest request, WorkSource source) {
        sendMessage(3, 0, new GpsRequest(request, source));
    }

    private void handleSetRequest(ProviderRequest request, WorkSource source) {
        this.mProviderRequest = request;
        this.mWorkSource = source;
        updateRequirements();
    }

    public boolean getGpsAllowDataCollect() {
        return this.mGpsAllowDataCollect;
    }

    private void updateRequirements() {
        if (this.mProviderRequest != null && this.mWorkSource != null) {
            boolean singleShot = false;
            if (this.mProviderRequest.locationRequests != null && this.mProviderRequest.locationRequests.size() > 0) {
                singleShot = true;
                for (LocationRequest lr : this.mProviderRequest.locationRequests) {
                    if (lr.getNumUpdates() != 1) {
                        singleShot = false;
                    }
                }
            }
            if (DEBUG) {
                Log.d("GnssLocationProvider", "setRequest " + this.mProviderRequest);
            }
            if (this.mProviderRequest.reportLocation && (this.mDisableGps ^ 1) != 0 && isEnabled()) {
                this.mIsFakeReport = isAllowedFakeReport(this.mWorkSource);
                updateClientUids(this.mWorkSource);
                this.mFixInterval = (int) this.mProviderRequest.interval;
                if (((long) this.mFixInterval) != this.mProviderRequest.interval) {
                    Log.w("GnssLocationProvider", "interval overflow: " + this.mProviderRequest.interval);
                    this.mFixInterval = Integer.MAX_VALUE;
                }
                if (this.mStarted && hasCapability(1)) {
                    if (!native_set_position_mode(this.mPositionMode, 0, this.mFixInterval, 0, 0)) {
                        Log.e("GnssLocationProvider", "set_position_mode failed in setMinTime()");
                    }
                } else if (!this.mStarted) {
                    if (this.mNavigationController.resistStartGps()) {
                        this.mSingleShot = singleShot;
                    } else {
                        this.mNavigationController.setUp();
                        startNavigating(singleShot);
                    }
                }
            } else {
                updateClientUids(new WorkSource());
                this.mNavigationController.stopController();
                stopNavigating();
                this.mAlarmManager.cancel(this.mWakeupIntent);
                this.mAlarmManager.cancel(this.mTimeoutIntent);
            }
        }
    }

    private void updateClientUids(WorkSource source) {
        WorkSource[] changes = this.mClientSource.setReturningDiffs(source);
        if (changes != null) {
            int lastuid;
            int i;
            int uid;
            WorkSource newWork = changes[0];
            WorkSource goneWork = changes[1];
            if (newWork != null) {
                lastuid = -1;
                for (i = 0; i < newWork.size(); i++) {
                    try {
                        uid = newWork.get(i);
                        this.mAppOpsService.startOperation(AppOpsManager.getToken(this.mAppOpsService), 2, uid, newWork.getName(i));
                        if (uid != lastuid) {
                            lastuid = uid;
                            this.mBatteryStats.noteStartGps(uid);
                        }
                    } catch (RemoteException e) {
                        Log.w("GnssLocationProvider", "RemoteException", e);
                    }
                }
            }
            if (goneWork != null) {
                lastuid = -1;
                for (i = 0; i < goneWork.size(); i++) {
                    try {
                        uid = goneWork.get(i);
                        this.mAppOpsService.finishOperation(AppOpsManager.getToken(this.mAppOpsService), 2, uid, goneWork.getName(i));
                        if (uid != lastuid) {
                            lastuid = uid;
                            this.mBatteryStats.noteStopGps(uid);
                        }
                    } catch (RemoteException e2) {
                        Log.w("GnssLocationProvider", "RemoteException", e2);
                    }
                }
            }
        }
    }

    public boolean sendExtraCommand(String command, Bundle extras) {
        long identity = Binder.clearCallingIdentity();
        boolean result = false;
        if ("delete_aiding_data".equals(command)) {
            result = deleteAidingData(extras);
        } else if ("force_time_injection".equals(command)) {
            requestUtcTime();
            result = true;
        } else if (!"force_xtra_injection".equals(command)) {
            Log.w("GnssLocationProvider", "sendExtraCommand: unknown command " + command);
        } else if (this.mSupportsXtra) {
            xtraDownloadRequest();
            result = true;
        }
        Binder.restoreCallingIdentity(identity);
        return result;
    }

    private boolean deleteAidingData(Bundle extras) {
        int flags;
        if (extras == null) {
            flags = NetworkConstants.ARP_HWTYPE_RESERVED_HI;
        } else {
            flags = 0;
            if (extras.getBoolean("ephemeris")) {
                flags = 1;
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
                flags |= NetworkConstants.ARP_HWTYPE_RESERVED_HI;
            }
        }
        if (flags == 0) {
            return false;
        }
        native_delete_aiding_data(flags);
        return true;
    }

    private void startNavigating(boolean singleShot) {
        if (!this.mNetworkLocationStarted) {
            networkLocationStart();
        }
        if (!this.mStarted) {
            if (DEBUG) {
                Log.d("GnssLocationProvider", "startNavigating, singleShot is " + singleShot);
            }
            this.mTimeToFirstFix = 0;
            this.mLastFixTime = 0;
            this.mStarted = true;
            this.mSingleShot = singleShot;
            this.mPositionMode = 0;
            if (this.mItarSpeedLimitExceeded) {
                Log.i("GnssLocationProvider", "startNavigating with ITAR limit in place. Output limited  until slow enough speed reported.");
            }
            this.mPositionMode = getSuplMode(this.mProperties, Global.getInt(this.mContext.getContentResolver(), "assisted_gps_enabled", 1) != 0, singleShot);
            if (DEBUG) {
                String mode;
                switch (this.mPositionMode) {
                    case 0:
                        mode = "standalone";
                        break;
                    case 1:
                        mode = "MS_BASED";
                        break;
                    case 2:
                        mode = "MS_ASSISTED";
                        break;
                    default:
                        mode = Shell.NIGHT_MODE_STR_UNKNOWN;
                        break;
                }
                Log.d("GnssLocationProvider", "setting position_mode to " + mode);
            }
            if (!native_set_position_mode(this.mPositionMode, 0, hasCapability(1) ? this.mFixInterval : 1000, 0, 0)) {
                this.mStarted = false;
                Log.e("GnssLocationProvider", "set_position_mode failed in startNavigating()");
            } else if (native_start()) {
                if (this.mGnssPowerEnable) {
                    this.mNavigationController.startController();
                } else {
                    this.mNavigationController.stopController();
                }
                updateStatus(1, 0, 0, 0);
                this.mFixRequestTime = SystemClock.elapsedRealtime();
                if (!hasCapability(1) && this.mFixInterval >= 60000) {
                    this.mAlarmManager.set(2, SystemClock.elapsedRealtime() + 60000, this.mTimeoutIntent);
                }
            } else {
                this.mStarted = false;
                Log.e("GnssLocationProvider", "native_start failed in startNavigating()");
            }
        }
    }

    private void stopNavigating() {
        if (DEBUG) {
            Log.d("GnssLocationProvider", "stopNavigating");
        }
        networkLocationStop();
        if (this.mStarted) {
            this.mStarted = false;
            this.mSingleShot = false;
            native_stop();
            this.mTimeToFirstFix = 0;
            this.mLastFixTime = 0;
            updateStatus(1, 0, 0, 0);
        }
    }

    private void hibernate() {
        stopNavigating();
        this.mAlarmManager.cancel(this.mTimeoutIntent);
        this.mAlarmManager.cancel(this.mWakeupIntent);
        this.mAlarmManager.set(2, ((long) this.mFixInterval) + SystemClock.elapsedRealtime(), this.mWakeupIntent);
    }

    private boolean hasCapability(int capability) {
        return (this.mEngineCapabilities & capability) != 0;
    }

    private void reportLocation(boolean hasLatLong, Location location) {
        int hasAltitude;
        if (location.hasSpeed()) {
            hasAltitude = location.hasAltitude();
        } else {
            hasAltitude = 0;
        }
        boolean isProgressPos = hasAltitude ^ 1;
        if (!isProgressPos || (this.mIsFakeReport ^ 1) == 0) {
            if (location.hasSpeed()) {
                this.mItarSpeedLimitExceeded = location.getSpeed() > ITAR_SPEED_LIMIT_METERS_PER_SECOND;
            }
            if (this.mItarSpeedLimitExceeded) {
                Log.i("GnssLocationProvider", "Hal reported a speed in excess of ITAR limit.  GPS/GNSS Navigation output blocked.");
                this.mGnssMetrics.logReceivedLocationStatus(false);
                return;
            }
            this.mSpeed = location.getSpeed();
            networkLocationStop();
            if (VERBOSE) {
                Log.v("GnssLocationProvider", "reportLocation " + location.toString());
            }
            synchronized (this.mLocation) {
                this.mLocation = location;
                this.mLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                this.mLocation.setExtras(this.mLocationExtras);
                if (isProgressPos) {
                    this.mLocation.setSpeed(-1.0f);
                }
                try {
                    this.mILocationManager.reportLocation(this.mLocation, false);
                } catch (RemoteException e) {
                    Log.e("GnssLocationProvider", "RemoteException calling reportLocation");
                }
            }
            this.mGnssMetrics.logReceivedLocationStatus(hasLatLong);
            if (hasLatLong) {
                if (location.hasAccuracy()) {
                    this.mGnssMetrics.logPositionAccuracyMeters(location.getAccuracy());
                }
                if (this.mTimeToFirstFix > 0) {
                    this.mGnssMetrics.logMissedReports(this.mFixInterval, (int) (SystemClock.elapsedRealtime() - this.mLastFixTime));
                }
            }
            this.mLastFixTime = SystemClock.elapsedRealtime();
            if (this.mTimeToFirstFix == 0 && hasLatLong) {
                this.mTimeToFirstFix = (int) (this.mLastFixTime - this.mFixRequestTime);
                if (DEBUG) {
                    Log.d("GnssLocationProvider", "TTFF: " + this.mTimeToFirstFix);
                }
                this.mGnssMetrics.logTimeToFirstFixMilliSecs(this.mTimeToFirstFix);
                this.mListenerHelper.onFirstFix(this.mTimeToFirstFix);
            }
            if (this.mSingleShot) {
                stopNavigating();
            }
            if (this.mStarted && this.mStatus != 2) {
                if (isProgressPos) {
                    if (DEBUG) {
                        Log.d("GnssLocationProvider", "ingore the location!");
                    }
                    return;
                }
                if (!hasCapability(1) && this.mFixInterval < 60000) {
                    this.mAlarmManager.cancel(this.mTimeoutIntent);
                }
                Intent intent = new Intent("android.location.GPS_FIX_CHANGE");
                intent.putExtra("enabled", true);
                this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                updateStatus(2, this.mSvCount, this.mMeanCn0, this.mMaxCn0);
            }
            if (!hasCapability(1) && this.mStarted && this.mFixInterval > 10000) {
                if (DEBUG) {
                    Log.d("GnssLocationProvider", "got fix, hibernating");
                }
                hibernate();
            }
            return;
        }
        if (DEBUG) {
            Log.d("GnssLocationProvider", "Don't report fake location!");
        }
        return;
    }

    private void reportStatus(int status) {
        if (DEBUG) {
            Log.v("GnssLocationProvider", "reportStatus status: " + status);
        }
        boolean wasNavigating = this.mNavigating;
        switch (status) {
            case 1:
                this.mNavigating = true;
                this.mEngineOn = true;
                break;
            case 2:
                this.mNavigating = false;
                break;
            case 3:
                this.mEngineOn = true;
                break;
            case 4:
                this.mEngineOn = false;
                this.mNavigating = false;
                break;
        }
        if (wasNavigating != this.mNavigating) {
            this.mListenerHelper.onStatusChanged(this.mNavigating);
            Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
            intent.putExtra("enabled", this.mNavigating);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    private void reportSvStatus() {
        int svCount = native_read_sv_status(this.mSvidWithFlags, this.mCn0s, this.mSvElevations, this.mSvAzimuths, this.mSvCarrierFreqs);
        this.mListenerHelper.onSvStatusChanged(svCount, this.mSvidWithFlags, this.mCn0s, this.mSvElevations, this.mSvAzimuths, this.mSvCarrierFreqs);
        this.mGnssMetrics.logCn0(this.mCn0s, svCount);
        if (VERBOSE) {
            Log.v("GnssLocationProvider", "SV count: " + svCount);
        }
        int usedInFixCount = 0;
        int maxCn0 = 0;
        int meanCn0 = 0;
        for (int i = 0; i < svCount; i++) {
            if ((this.mSvidWithFlags[i] & 4) != 0) {
                usedInFixCount++;
                if (this.mCn0s[i] > ((float) maxCn0)) {
                    maxCn0 = (int) this.mCn0s[i];
                }
                meanCn0 = (int) (((float) meanCn0) + this.mCn0s[i]);
            }
        }
        if (usedInFixCount > 0) {
            meanCn0 /= usedInFixCount;
        }
        updateStatus(this.mStatus, usedInFixCount, meanCn0, maxCn0);
        if (this.mNavigating && this.mStatus == 2 && this.mLastFixTime > 0 && SystemClock.elapsedRealtime() - this.mLastFixTime > 10000) {
            Intent intent = new Intent("android.location.GPS_FIX_CHANGE");
            intent.putExtra("enabled", false);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            updateStatus(1, this.mSvCount, this.mMeanCn0, this.mMaxCn0);
        }
    }

    private void reportAGpsStatus(int type, int status, byte[] ipaddr) {
        switch (status) {
            case 1:
                if (DEBUG) {
                    Log.d("GnssLocationProvider", "GPS_REQUEST_AGPS_DATA_CONN");
                }
                Log.v("GnssLocationProvider", "Received SUPL IP addr[]: " + Arrays.toString(ipaddr));
                Object connectionIpAddress = null;
                if (ipaddr != null) {
                    try {
                        connectionIpAddress = InetAddress.getByAddress(ipaddr);
                        if (DEBUG) {
                            Log.d("GnssLocationProvider", "IP address converted to: " + connectionIpAddress);
                        }
                    } catch (UnknownHostException e) {
                        Log.e("GnssLocationProvider", "Bad IP Address: " + ipaddr, e);
                    }
                }
                sendMessage(14, 0, connectionIpAddress);
                return;
            case 2:
                if (DEBUG) {
                    Log.d("GnssLocationProvider", "GPS_RELEASE_AGPS_DATA_CONN");
                }
                releaseSuplConnection(2);
                return;
            case 3:
                if (DEBUG) {
                    Log.d("GnssLocationProvider", "GPS_AGPS_DATA_CONNECTED");
                    return;
                }
                return;
            case 4:
                if (DEBUG) {
                    Log.d("GnssLocationProvider", "GPS_AGPS_DATA_CONN_DONE");
                    return;
                }
                return;
            case 5:
                if (DEBUG) {
                    Log.d("GnssLocationProvider", "GPS_AGPS_DATA_CONN_FAILED");
                    return;
                }
                return;
            default:
                if (DEBUG) {
                    Log.d("GnssLocationProvider", "Received Unknown AGPS status: " + status);
                    return;
                }
                return;
        }
    }

    private void releaseSuplConnection(int connStatus) {
        sendMessage(15, connStatus, null);
    }

    private void reportNmea(long timestamp) {
        if (!this.mItarSpeedLimitExceeded) {
            String nmea = new String(this.mNmeaBuffer, 0, native_read_nmea(this.mNmeaBuffer, this.mNmeaBuffer.length));
            if (DEBUG) {
                Log.d("GnssLocationProvider", "NMEA <" + nmea);
            }
            this.mListenerHelper.onNmeaReceived(timestamp, nmea);
        }
    }

    private void reportMeasurementData(final GnssMeasurementsEvent event) {
        if (!this.mItarSpeedLimitExceeded) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    GnssLocationProvider.this.mGnssMeasurementsProvider.onMeasurementsAvailable(event);
                }
            });
        }
    }

    private void reportNavigationMessage(final GnssNavigationMessage event) {
        if (!this.mItarSpeedLimitExceeded) {
            this.mHandler.post(new Runnable() {
                public void run() {
                    GnssLocationProvider.this.mGnssNavigationMessageProvider.onNavigationMessageAvailable(event);
                }
            });
        }
    }

    private void setEngineCapabilities(int capabilities) {
        boolean z;
        boolean z2 = true;
        this.mEngineCapabilities = capabilities;
        if (hasCapability(16)) {
            this.mOnDemandTimeInjection = true;
            requestUtcTime();
        }
        GnssMeasurementsProvider gnssMeasurementsProvider = this.mGnssMeasurementsProvider;
        if ((capabilities & 64) == 64) {
            z = true;
        } else {
            z = false;
        }
        gnssMeasurementsProvider.onCapabilitiesUpdated(z);
        GnssNavigationMessageProvider gnssNavigationMessageProvider = this.mGnssNavigationMessageProvider;
        if ((capabilities & 128) != 128) {
            z2 = false;
        }
        gnssNavigationMessageProvider.onCapabilitiesUpdated(z2);
    }

    private void setGnssYearOfHardware(int yearOfHardware) {
        if (DEBUG) {
            Log.d("GnssLocationProvider", "setGnssYearOfHardware called with " + yearOfHardware);
        }
        this.mYearOfHardware = yearOfHardware;
    }

    public GnssSystemInfoProvider getGnssSystemInfoProvider() {
        return new GnssSystemInfoProvider() {
            public int getGnssYearOfHardware() {
                return GnssLocationProvider.this.mYearOfHardware;
            }
        };
    }

    public GnssBatchingProvider getGnssBatchingProvider() {
        return new GnssBatchingProvider() {
            public int getSize() {
                return GnssLocationProvider.native_get_batch_size();
            }

            public boolean start(long periodNanos, boolean wakeOnFifoFull) {
                if (periodNanos > 0) {
                    return GnssLocationProvider.native_start_batch(periodNanos, wakeOnFifoFull);
                }
                Log.e("GnssLocationProvider", "Invalid periodNanos " + periodNanos + "in batching request, not started");
                return false;
            }

            public void flush() {
                GnssLocationProvider.native_flush_batch();
            }

            public boolean stop() {
                return GnssLocationProvider.native_stop_batch();
            }
        };
    }

    public GnssMetricsProvider getGnssMetricsProvider() {
        return new GnssMetricsProvider() {
            public String getGnssMetricsAsProtoString() {
                return GnssLocationProvider.this.mGnssMetrics.dumpGnssMetricsAsProtoString();
            }
        };
    }

    private void enableBatching() {
        if (!native_init_batching()) {
            Log.e("GnssLocationProvider", "Failed to initialize GNSS batching");
        }
    }

    private void disableBatching() {
        native_stop_batch();
        native_cleanup_batching();
    }

    private void reportLocationBatch(Location[] locationArray) {
        List<Location> locations = new ArrayList(Arrays.asList(locationArray));
        if (DEBUG) {
            Log.d("GnssLocationProvider", "Location batch of size " + locationArray.length + "reported");
        }
        try {
            this.mILocationManager.reportLocationBatch(locations);
        } catch (RemoteException e) {
            Log.e("GnssLocationProvider", "RemoteException calling reportLocationBatch");
        }
    }

    private void xtraDownloadRequest() {
        if (DEBUG) {
            Log.d("GnssLocationProvider", "xtraDownloadRequest");
        }
        sendMessage(6, 0, null);
    }

    private int getGeofenceStatus(int status) {
        switch (status) {
            case GPS_GEOFENCE_ERROR_GENERIC /*-149*/:
                return 5;
            case GPS_GEOFENCE_ERROR_INVALID_TRANSITION /*-103*/:
                return 4;
            case GPS_GEOFENCE_ERROR_ID_UNKNOWN /*-102*/:
                return 3;
            case GPS_GEOFENCE_ERROR_ID_EXISTS /*-101*/:
                return 2;
            case 0:
                return 0;
            case 100:
                return 1;
            default:
                return -1;
        }
    }

    private void reportGeofenceTransition(int geofenceId, Location location, int transition, long transitionTimestamp) {
        if (this.mGeofenceHardwareImpl == null) {
            this.mGeofenceHardwareImpl = GeofenceHardwareImpl.getInstance(this.mContext);
        }
        this.mGeofenceHardwareImpl.reportGeofenceTransition(geofenceId, location, transition, transitionTimestamp, 0, SourceTechnologies.GNSS);
    }

    private void reportGeofenceStatus(int status, Location location) {
        if (this.mGeofenceHardwareImpl == null) {
            this.mGeofenceHardwareImpl = GeofenceHardwareImpl.getInstance(this.mContext);
        }
        int monitorStatus = 1;
        if (status == 2) {
            monitorStatus = 0;
        }
        this.mGeofenceHardwareImpl.reportGeofenceMonitorStatus(0, monitorStatus, location, SourceTechnologies.GNSS);
    }

    private void reportGeofenceAddStatus(int geofenceId, int status) {
        if (this.mGeofenceHardwareImpl == null) {
            this.mGeofenceHardwareImpl = GeofenceHardwareImpl.getInstance(this.mContext);
        }
        this.mGeofenceHardwareImpl.reportGeofenceAddStatus(geofenceId, getGeofenceStatus(status));
    }

    private void reportGeofenceRemoveStatus(int geofenceId, int status) {
        if (this.mGeofenceHardwareImpl == null) {
            this.mGeofenceHardwareImpl = GeofenceHardwareImpl.getInstance(this.mContext);
        }
        this.mGeofenceHardwareImpl.reportGeofenceRemoveStatus(geofenceId, getGeofenceStatus(status));
    }

    private void reportGeofencePauseStatus(int geofenceId, int status) {
        if (this.mGeofenceHardwareImpl == null) {
            this.mGeofenceHardwareImpl = GeofenceHardwareImpl.getInstance(this.mContext);
        }
        this.mGeofenceHardwareImpl.reportGeofencePauseStatus(geofenceId, getGeofenceStatus(status));
    }

    private void reportGeofenceResumeStatus(int geofenceId, int status) {
        if (this.mGeofenceHardwareImpl == null) {
            this.mGeofenceHardwareImpl = GeofenceHardwareImpl.getInstance(this.mContext);
        }
        this.mGeofenceHardwareImpl.reportGeofenceResumeStatus(geofenceId, getGeofenceStatus(status));
    }

    public INetInitiatedListener getNetInitiatedListener() {
        return this.mNetInitiatedListener;
    }

    public void reportNiNotification(int notificationId, int niType, int notifyFlags, int timeout, int defaultResponse, String requestorId, String text, int requestorIdEncoding, int textEncoding) {
        boolean z;
        boolean z2 = true;
        Log.i("GnssLocationProvider", "reportNiNotification: entered");
        Log.i("GnssLocationProvider", "notificationId: " + notificationId + ", niType: " + niType + ", notifyFlags: " + notifyFlags + ", timeout: " + timeout + ", defaultResponse: " + defaultResponse);
        Log.i("GnssLocationProvider", "requestorId: " + requestorId + ", text: " + text + ", requestorIdEncoding: " + requestorIdEncoding + ", textEncoding: " + textEncoding);
        GpsNiNotification notification = new GpsNiNotification();
        notification.notificationId = notificationId;
        notification.niType = niType;
        notification.needNotify = (notifyFlags & 1) != 0;
        if ((notifyFlags & 2) != 0) {
            z = true;
        } else {
            z = false;
        }
        notification.needVerify = z;
        if ((notifyFlags & 4) == 0) {
            z2 = false;
        }
        notification.privacyOverride = z2;
        notification.timeout = timeout;
        notification.defaultResponse = defaultResponse;
        notification.requestorId = requestorId;
        notification.text = text;
        notification.requestorIdEncoding = requestorIdEncoding;
        notification.textEncoding = textEncoding;
        this.mNIHandler.handleNiNotification(notification);
    }

    private void requestSetID(int flags) {
        TelephonyManager phone = (TelephonyManager) this.mContext.getSystemService("phone");
        int type = 0;
        String data = "";
        String data_temp;
        if ((flags & 1) == 1) {
            data_temp = phone.getSubscriberId();
            if (data_temp != null) {
                data = data_temp;
                type = 1;
            }
        } else if ((flags & 2) == 2) {
            data_temp = phone.getLine1Number();
            if (data_temp != null) {
                data = data_temp;
                type = 2;
            }
        }
        native_agps_set_id(type, data);
    }

    private void requestUtcTime() {
        if (DEBUG) {
            Log.d("GnssLocationProvider", "utcTimeRequest");
        }
        sendMessage(5, 0, null);
    }

    private void requestRefLocation() {
        TelephonyManager phone = (TelephonyManager) this.mContext.getSystemService("phone");
        int phoneType = phone.getPhoneType();
        if (phoneType == 1) {
            GsmCellLocation gsm_cell = (GsmCellLocation) phone.getCellLocation();
            if (gsm_cell == null || phone.getNetworkOperator() == null || phone.getNetworkOperator().length() <= 3) {
                Log.e("GnssLocationProvider", "Error getting cell location info.");
                return;
            }
            int type;
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

    private void sendMessage(int message, int arg, Object obj) {
        this.mWakeLock.acquire();
        if (DEBUG) {
            Log.i("GnssLocationProvider", "WakeLock acquired by sendMessage(" + messageIdAsString(message) + ", " + arg + ", " + obj + ")");
        }
        this.mHandler.obtainMessage(message, arg, 1, obj).sendToTarget();
    }

    private String getSelectedApn() {
        Uri uri = Uri.parse("content://telephony/carriers/preferapn");
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(uri, new String[]{"apn"}, null, null, "name ASC");
            if (cursor == null || !cursor.moveToFirst()) {
                Log.e("GnssLocationProvider", "No APN found to select.");
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            String string = cursor.getString(0);
            if (cursor != null) {
                cursor.close();
            }
            return string;
        } catch (Exception e) {
            Log.e("GnssLocationProvider", "Error encountered on selecting the APN.", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private int getApnIpType(String apn) {
        ensureInHandlerThread();
        if (apn == null) {
            return 0;
        }
        String selection = String.format("current = 1 and apn = '%s' and carrier_enabled = 1", new Object[]{apn});
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(Carriers.CONTENT_URI, new String[]{"protocol"}, selection, null, "name ASC");
            if (cursor == null || !cursor.moveToFirst()) {
                Log.e("GnssLocationProvider", "No entry found in query for APN: " + apn);
                if (cursor != null) {
                    cursor.close();
                }
                return 0;
            }
            int translateToApnIpType = translateToApnIpType(cursor.getString(0), apn);
            if (cursor != null) {
                cursor.close();
            }
            return translateToApnIpType;
        } catch (Exception e) {
            Log.e("GnssLocationProvider", "Error encountered on APN query for: " + apn, e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private int translateToApnIpType(String ipProtocol, String apn) {
        if ("IP".equals(ipProtocol)) {
            return 1;
        }
        if ("IPV6".equals(ipProtocol)) {
            return 2;
        }
        if ("IPV4V6".equals(ipProtocol)) {
            return 3;
        }
        Log.e("GnssLocationProvider", String.format("Unknown IP Protocol: %s, for APN: %s", new Object[]{ipProtocol, apn}));
        return 0;
    }

    private void setRouting() {
        if (this.mAGpsDataConnectionIpAddr != null) {
            if (!this.mConnMgr.requestRouteToHostAddress(3, this.mAGpsDataConnectionIpAddr)) {
                Log.e("GnssLocationProvider", "Error requesting route to host: " + this.mAGpsDataConnectionIpAddr);
            } else if (DEBUG) {
                Log.d("GnssLocationProvider", "Successfully requested route to host: " + this.mAGpsDataConnectionIpAddr);
            }
        }
    }

    private boolean isDataNetworkConnected() {
        NetworkInfo activeNetworkInfo = this.mConnMgr.getActiveNetworkInfo();
        return activeNetworkInfo != null ? activeNetworkInfo.isConnected() : false;
    }

    private void ensureInHandlerThread() {
        if (this.mHandler == null || Looper.myLooper() != this.mHandler.getLooper()) {
            throw new RuntimeException("This method must run on the Handler thread.");
        }
    }

    private String agpsDataConnStateAsString() {
        switch (this.mAGpsDataConnectionState) {
            case 0:
                return "CLOSED";
            case 1:
                return "OPENING";
            case 2:
                return "OPEN";
            default:
                return "<Unknown>";
        }
    }

    private String agpsDataConnStatusAsString(int agpsDataConnStatus) {
        switch (agpsDataConnStatus) {
            case 1:
                return "REQUEST";
            case 2:
                return "RELEASE";
            case 3:
                return "CONNECTED";
            case 4:
                return "DONE";
            case 5:
                return "FAILED";
            default:
                return "<Unknown>";
        }
    }

    private String messageIdAsString(int message) {
        switch (message) {
            case 2:
                return "ENABLE";
            case 3:
                return "SET_REQUEST";
            case 4:
                return "UPDATE_NETWORK_STATE";
            case 5:
                return "INJECT_NTP_TIME";
            case 6:
                return "DOWNLOAD_XTRA_DATA";
            case 7:
                return "UPDATE_LOCATION";
            case 10:
                return "INJECT_NTP_TIME_FINISHED";
            case 11:
                return "DOWNLOAD_XTRA_DATA_FINISHED";
            case 12:
                return "SUBSCRIPTION_OR_SIM_CHANGED";
            case 13:
                return "INITIALIZE_HANDLER";
            case 14:
                return "REQUEST_SUPL_CONNECTION";
            case 15:
                return "RELEASE_SUPL_CONNECTION";
            default:
                return "<Unknown>";
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        StringBuilder s = new StringBuilder();
        s.append("  mStarted=").append(this.mStarted).append(10);
        s.append("  mFixInterval=").append(this.mFixInterval).append(10);
        s.append("  mDisableGps (battery saver mode)=").append(this.mDisableGps).append(10);
        s.append("  mEngineCapabilities=0x").append(Integer.toHexString(this.mEngineCapabilities));
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
        s.append(")\n");
        s.append(this.mGnssMetrics.dumpGnssMetricsAsText());
        s.append("  native internal state: ").append(native_get_internal_state());
        s.append("\n");
        pw.append(s);
    }

    protected void wakeGps() {
        startNavigating(this.mSingleShot);
    }

    protected void enterPSMode() {
        stopNavigating();
    }

    protected float getSpeed() {
        return this.mSpeed * 3.6f;
    }

    protected boolean isFakeReport() {
        return this.mIsFakeReport;
    }

    private boolean isAllowedFakeReport(WorkSource worksource) {
        if (DEBUG) {
            Log.d("GnssLocationProvider", "isFakeReport worksource:" + worksource);
        }
        for (int i = 0; i < worksource.size(); i++) {
            String name = worksource.getName(i);
            if (name != null && this.mOppoBlackList.inFRBlacklist(name)) {
                return false;
            }
        }
        return true;
    }

    public void enableVerboseLogging(int verbose) {
        boolean z = true;
        DEBUG = verbose > 0;
        if (verbose <= 0) {
            z = false;
        }
        VERBOSE = z;
        native_delete_aiding_data(DEBUG ? 2048 : 4096);
        if (this.mNavigationController != null) {
            this.mNavigationController.setDebug(DEBUG);
        }
    }

    private void networkLocationHandleByNetworkState(NetworkInfo info) {
        if (info != null) {
            if (info.isConnected()) {
                this.mNetworkConnected = true;
                if (this.mNetworkLocationRequestPending) {
                    Log.d("GnssLocationProvider", "Will start a network location request!!!");
                    networkLocationStart();
                }
            } else {
                this.mNetworkConnected = false;
                this.mNetworkLocationRequestPending = true;
                networkLocationStop();
            }
        }
    }

    private void networkLocationStart() {
        if (this.mNetworkConnected) {
            if (this.mLocationManager == null) {
                this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
                this.mNetworkProviderEnabled = this.mLocationManager.isProviderEnabled("network");
            }
            if (this.mNetworkProviderEnabled) {
                try {
                    this.mLocationManager.requestLocationUpdates("network", 1000, OppoBrightUtils.MIN_LUX_LIMITI, this.mLocationListener);
                    this.mNetworkLocationStarted = true;
                    this.mNetworkLocationRequestPending = false;
                    return;
                } catch (SecurityException e) {
                    Log.e("GnssLocationProvider", "fail to request location update, ignore");
                    return;
                } catch (IllegalArgumentException e2) {
                    Log.e("GnssLocationProvider", "network provider does not exist!");
                    return;
                }
            }
            Log.e("GnssLocationProvider", "Network Location Provider is enable!!!");
            return;
        }
        Log.e("GnssLocationProvider", "network is invalid, pending network request");
        this.mNetworkLocationRequestPending = true;
    }

    private void networkLocationStop() {
        if (this.mNetworkProviderEnabled && this.mNetworkLocationStarted) {
            this.mLocationManager.removeUpdates(this.mLocationListener);
            this.mNetworkLocationStarted = false;
            Log.d("GnssLocationProvider", "Network location has been stopped!!");
        }
    }
}
