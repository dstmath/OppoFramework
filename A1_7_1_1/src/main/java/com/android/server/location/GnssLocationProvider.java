package com.android.server.location;

import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
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
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.WorkSource;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Telephony.Carriers;
import android.provider.Telephony.Sms.Intents;
import android.telephony.SmsMessage;
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
import com.android.server.LocationManagerService;
import com.android.server.display.OppoBrightUtils;
import com.android.server.oppo.IElsaManager;
import com.mediatek.location.LocationExt;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Properties;
import libcore.io.IoUtils;

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
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
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
public class GnssLocationProvider implements LocationProviderInterface {
    private static final String ACTION_MTKLOGGER_STATE_CHANGED = "com.mediatek.mtklogger.intent.action.LOG_STATE_CHANGED";
    private static final int ADD_LISTENER = 8;
    private static final int AGPS_DATA_CONNECTION_CLOSED = 0;
    private static final int AGPS_DATA_CONNECTION_OPEN = 2;
    private static final int AGPS_DATA_CONNECTION_OPENING = 1;
    private static final int AGPS_REF_LOCATION_TYPE_GSM_CELLID = 1;
    private static final int AGPS_REF_LOCATION_TYPE_UMTS_CELLID = 2;
    private static final int AGPS_REG_LOCATION_TYPE_MAC = 3;
    private static final int AGPS_RIL_REQUEST_REFLOC_CELLID = 1;
    private static final int AGPS_RIL_REQUEST_REFLOC_MAC = 2;
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
    private static final String BATTERY_SAVER_GPS_MODE = "batterySaverGpsMode";
    private static final int BATTERY_SAVER_MODE_DISABLED_WHEN_SCREEN_OFF = 1;
    private static final int BATTERY_SAVER_MODE_NO_CHANGE = 0;
    private static final int CHECK_LOCATION = 1;
    private static boolean DEBUG = false;
    private static final String DEFAULT_PROPERTIES_FILE = "/etc/gps.conf";
    private static final int DOWNLOAD_XTRA_DATA = 6;
    private static final int DOWNLOAD_XTRA_DATA_FINISHED = 11;
    private static final int ENABLE = 2;
    public static final boolean FORCE_DEBUG = false;
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
    private static final int GPS_POLLING_THRESHOLD_INTERVAL = 10000;
    private static final int GPS_POSITION_MODE_MS_ASSISTED = 2;
    private static final int GPS_POSITION_MODE_MS_BASED = 1;
    private static final int GPS_POSITION_MODE_STANDALONE = 0;
    private static final int GPS_POSITION_RECURRENCE_PERIODIC = 0;
    private static final int GPS_POSITION_RECURRENCE_SINGLE = 1;
    private static final int GPS_RELEASE_AGPS_DATA_CONN = 2;
    private static final int GPS_REQUEST_AGPS_DATA_CONN = 1;
    private static final int GPS_STATUS_ENGINE_OFF = 4;
    private static final int GPS_STATUS_ENGINE_ON = 3;
    private static final int GPS_STATUS_NONE = 0;
    private static final int GPS_STATUS_SESSION_BEGIN = 1;
    private static final int GPS_STATUS_SESSION_END = 2;
    private static final int INITIALIZE_HANDLER = 13;
    private static final int INJECT_NTP_TIME = 5;
    private static final int INJECT_NTP_TIME_FINISHED = 10;
    private static final boolean IS_USER_BUILD = false;
    private static final int LOCATION_HAS_ACCURACY = 16;
    private static final int LOCATION_HAS_ALTITUDE = 2;
    private static final int LOCATION_HAS_BEARING = 8;
    private static final int LOCATION_HAS_LAT_LONG = 1;
    private static final int LOCATION_HAS_SPEED = 4;
    private static final int LOCATION_INVALID = 0;
    private static final String LPP_PROFILE = "persist.sys.gps.lpp";
    private static final long MAX_RETRY_INTERVAL = 14400000;
    private static final int MAX_SVS = 64;
    private static final String MTKLOGGER_MOBILELOG_DEBUG_PROPERTY = "debug.MB.running";
    private static final String MTK_DEBUG_GPSDBLOG_ENABLE_PROPERTY = "debug.gpsdbglog.enable";
    private static final int NO_FIX_TIMEOUT = 60000;
    private static final long NTP_INTERVAL = 86400000;
    private static final ProviderProperties PROPERTIES = null;
    private static final String PROPERTIES_FILE_PREFIX = "/etc/gps";
    private static final String PROPERTIES_FILE_SUFFIX = ".conf";
    private static final String PROP_FORCE_DEBUG_KEY = "persist.log.tag.tel_dbg";
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
    private static boolean VERBOSE = false;
    private static final String VZW_DEBUG_MSG = "com.mediatek.location.debug_message";
    private static final String[] VzwGid1List = null;
    private static final String[] VzwMccMncList = null;
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
    private String mDefaultApn;
    private boolean mDisableGps;
    private int mDownloadXtraDataPending;
    private boolean mEnabled;
    private int mEngineCapabilities;
    private boolean mEngineOn;
    private int mFixInterval;
    private long mFixRequestTime;
    private GeofenceHardwareImpl mGeofenceHardwareImpl;
    private final GnssMeasurementsProvider mGnssMeasurementsProvider;
    private final GnssNavigationMessageProvider mGnssNavigationMessageProvider;
    private final IGnssStatusProvider mGnssStatusProvider;
    private final GpsController mGpsController;
    private IGpsGeofenceHardware mGpsGeofenceBinder;
    private Handler mHandler;
    private final ILocationManager mILocationManager;
    private int mInjectNtpTimePending;
    private boolean mIsFakeReport;
    private long mLastFixTime;
    private final GnssStatusListenerHelper mListenerHelper;
    private Location mLocation;
    private Bundle mLocationExtras;
    private int mLocationFlags;
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;
    private Object mLock;
    private final GpsNetInitiatedHandler mNIHandler;
    private boolean mNavigating;
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
    private boolean vzwDbgEanbled;

    public interface GnssSystemInfoProvider {
        int getGnssYearOfHardware();
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
        /* synthetic */ NetworkLocationListener(GnssLocationProvider this$0, NetworkLocationListener networkLocationListener) {
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
                    Log.i("GnssLocationProvider", "WakeLock released by handleMessage(" + message + ", " + msg.arg1 + ", " + msg.obj + ")");
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
            intentFilter.addAction("com.mediatek.mtklogger.intent.action.LOG_STATE_CHANGED");
            GnssLocationProvider.this.mContext.registerReceiver(GnssLocationProvider.this.mBroadcastReceiver, intentFilter, null, this);
            Builder networkRequestBuilder = new Builder();
            networkRequestBuilder.addTransportType(0);
            networkRequestBuilder.addTransportType(1);
            GnssLocationProvider.this.mConnMgr.registerNetworkCallback(networkRequestBuilder.build(), GnssLocationProvider.this.mNetworkConnectivityCallback);
            LocationManager locManager = (LocationManager) GnssLocationProvider.this.mContext.getSystemService("location");
            LocationRequest request = LocationRequest.createFromDeprecatedProvider("passive", 0, OppoBrightUtils.MIN_LUX_LIMITI, false);
            request.setHideFromAppOps(true);
            locManager.requestLocationUpdates(request, new NetworkLocationListener(GnssLocationProvider.this, null), getLooper());
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.location.GnssLocationProvider.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.location.GnssLocationProvider.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.GnssLocationProvider.<clinit>():void");
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

    private static native void native_configuration_update(String str);

    private native void native_delete_aiding_data(int i);

    private native String native_get_internal_state();

    private native boolean native_init();

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

    private native int native_read_sv_status(int[] iArr, float[] fArr, float[] fArr2, float[] fArr3);

    private static native boolean native_remove_geofence(int i);

    private static native boolean native_resume_geofence(int i, int i2);

    private native void native_send_ni_response(int i, int i2);

    private native void native_set_agps_server(int i, String str, int i2);

    private native void native_set_log_level(int i);

    private native boolean native_set_position_mode(int i, int i2, int i3, int i4, int i5);

    private static native void native_set_vzw_debug_screen(boolean z);

    private native boolean native_start();

    private native boolean native_start_measurement_collection();

    private native boolean native_start_navigation_message_collection();

    private native boolean native_stop();

    private native boolean native_stop_measurement_collection();

    private native boolean native_stop_navigation_message_collection();

    private native boolean native_supports_xtra();

    private native void native_update_network_state(boolean z, int i, boolean z2, boolean z3, String str, String str2);

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

    private final boolean isVerizon(String mccMnc, String imsi, String groupId) {
        if (DEBUG) {
            Log.d("GnssLocationProvider", "simOperator: " + mccMnc);
        }
        if (!(TextUtils.isEmpty(mccMnc) && TextUtils.isEmpty(imsi))) {
            int i = 0;
            while (i < VzwMccMncList.length) {
                if (((TextUtils.isEmpty(mccMnc) || !mccMnc.equals(VzwMccMncList[i])) && (TextUtils.isEmpty(imsi) || !imsi.startsWith(VzwMccMncList[i]))) || !(TextUtils.isEmpty(VzwGid1List[i]) || VzwGid1List[i].equals(groupId))) {
                    i++;
                } else {
                    if (DEBUG) {
                        Log.d("GnssLocationProvider", "Verizon UICC");
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private void subscriptionOrSimChanged(Context context) {
        if (DEBUG) {
            Log.d("GnssLocationProvider", "received SIM related action: ");
        }
        TelephonyManager phone = (TelephonyManager) this.mContext.getSystemService("phone");
        String mccMnc = phone.getSimOperator();
        String imsi = phone.getSubscriberId();
        String groupId = phone.getGroupIdLevel1();
        if (!TextUtils.isEmpty(mccMnc)) {
            if (DEBUG) {
                Log.d("GnssLocationProvider", "SIM MCC/MNC is available: " + mccMnc);
            }
            synchronized (this.mLock) {
                if (isVerizon(mccMnc, imsi, groupId)) {
                    loadPropertiesFromResource(context, this.mProperties);
                    SystemProperties.set(LPP_PROFILE, this.mProperties.getProperty("LPP_PROFILE"));
                } else {
                    SystemProperties.set(LPP_PROFILE, IElsaManager.EMPTY_PACKAGE);
                }
                reloadGpsProperties(context, this.mProperties);
                this.mNIHandler.setSuplEsEnabled(this.mSuplEsEnabled);
            }
        } else if (DEBUG) {
            Log.d("GnssLocationProvider", "SIM MCC/MNC is still not available");
        }
    }

    private void checkSmsSuplInit(Intent intent) {
        SmsMessage[] messages = Intents.getMessagesFromIntent(intent);
        if (messages == null) {
            Log.e("GnssLocationProvider", "Message does not exist in the intent.");
            return;
        }
        for (SmsMessage message : messages) {
            if (!(message == null || message.mWrappedSmsMessage == null)) {
                byte[] suplInit = message.getUserData();
                if (suplInit != null) {
                    native_agps_ni_message(suplInit, suplInit.length);
                }
            }
        }
    }

    private void checkWapSuplInit(Intent intent) {
        if (LocationExt.checkWapSuplInit(intent)) {
            byte[] suplInit = intent.getByteArrayExtra("data");
            if (suplInit != null) {
                native_agps_ni_message(suplInit, suplInit.length);
            }
        }
    }

    private void updateLowPowerMode() {
        int i = 0;
        boolean disableGps = this.mPowerManager.isDeviceIdleMode();
        switch (Secure.getInt(this.mContext.getContentResolver(), BATTERY_SAVER_GPS_MODE, 1)) {
            case 1:
                if (this.mPowerManager.isPowerSaveMode() && !this.mPowerManager.isInteractive()) {
                    i = 1;
                }
                disableGps |= i;
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
        boolean z = true;
        if (DEBUG) {
            Log.d("GnssLocationProvider", "Reset GPS properties, previous size = " + properties.size());
        }
        loadPropertiesFromResource(context, properties);
        boolean isPropertiesLoadedFromFile = false;
        String gpsHardware = SystemProperties.get("ro.hardware.gps");
        if (!TextUtils.isEmpty(gpsHardware)) {
            isPropertiesLoadedFromFile = loadPropertiesFromFile("/etc/gps." + gpsHardware + PROPERTIES_FILE_SUFFIX, properties);
        }
        if (!isPropertiesLoadedFromFile) {
            loadPropertiesFromFile(DEFAULT_PROPERTIES_FILE, properties);
        }
        if (DEBUG) {
            Log.d("GnssLocationProvider", "GPS properties reloaded, size = " + properties.size());
        }
        String lpp_prof = SystemProperties.get(LPP_PROFILE);
        if (!TextUtils.isEmpty(lpp_prof)) {
            properties.setProperty("LPP_PROFILE", lpp_prof);
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
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
                properties.store(baos, null);
                native_configuration_update(baos.toString());
                if (DEBUG) {
                    Log.d("GnssLocationProvider", "final config = " + baos.toString());
                }
            } catch (IOException e2) {
                Log.e("GnssLocationProvider", "failed to dump properties contents");
            }
        } else if (DEBUG) {
            Log.d("GnssLocationProvider", "Skipped configuration update because GNSS configuration in GPS HAL is not supported");
        }
        String suplESProperty = this.mProperties.getProperty("SUPL_ES");
        if (suplESProperty != null) {
            try {
                if (Integer.parseInt(suplESProperty) != 1) {
                    z = false;
                }
                this.mSuplEsEnabled = z;
            } catch (NumberFormatException e3) {
                Log.e("GnssLocationProvider", "unable to parse SUPL_ES: " + suplESProperty);
            }
        }
    }

    private void loadPropertiesFromResource(Context context, Properties properties) {
        for (String item : context.getResources().getStringArray(17236036)) {
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
            Log.w("GnssLocationProvider", "Could not open GPS configuration file " + filename);
            return false;
        }
    }

    public GnssLocationProvider(Context context, ILocationManager ilocationManager, OppoLocationBlacklist oppoBlackList, Looper looper) {
        this(context, ilocationManager, looper);
        this.mOppoBlackList = oppoBlackList;
    }

    public GnssLocationProvider(Context context, ILocationManager ilocationManager, Looper looper) {
        this.mLock = new Object();
        this.mLocationFlags = 0;
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
        this.mDefaultApn = null;
        this.vzwDbgEanbled = false;
        this.mClientSource = new WorkSource();
        this.mYearOfHardware = 0;
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
                if (GnssLocationProvider.this.mInjectNtpTimePending == 0 && GnssLocationProvider.this.isEnabled()) {
                    GnssLocationProvider.this.requestUtcTime();
                }
                if (GnssLocationProvider.this.mDownloadXtraDataPending == 0) {
                    GnssLocationProvider.this.xtraDownloadRequest();
                }
                LocationExt.updateNetworkAvailable(network);
            }
        };
        this.mSuplConnectivityCallback = new NetworkCallback() {
            public void onAvailable(Network network) {
                LocationExt.suplConnectionCallback(2, network);
                GnssLocationProvider.this.sendMessage(4, 0, network);
            }

            public void onLost(Network network) {
                LocationExt.suplConnectionCallback(1, network);
                GnssLocationProvider.this.releaseSuplConnection(2);
            }

            public void onUnavailable() {
                LocationExt.suplConnectionCallback(0, null);
                GnssLocationProvider.this.releaseSuplConnection(5);
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
                    } else if (action.equals("android.intent.action.DATA_SMS_RECEIVED")) {
                        GnssLocationProvider.this.checkSmsSuplInit(intent);
                    } else if (action.equals("android.provider.Telephony.WAP_PUSH_RECEIVED")) {
                        GnssLocationProvider.this.checkWapSuplInit(intent);
                    } else if ("android.os.action.POWER_SAVE_MODE_CHANGED".equals(action) || "android.os.action.DEVICE_IDLE_MODE_CHANGED".equals(action) || "android.intent.action.SCREEN_OFF".equals(action) || "android.intent.action.SCREEN_ON".equals(action)) {
                        GnssLocationProvider.this.updateLowPowerMode();
                    } else if (action.equals(GnssLocationProvider.SIM_STATE_CHANGED)) {
                        GnssLocationProvider.this.subscriptionOrSimChanged(context);
                    } else if (action.equals("com.mediatek.mtklogger.intent.action.LOG_STATE_CHANGED")) {
                        Log.d("GnssLocationProvider", "GNSS location provider get the action : com.mediatek.mtklogger.intent.action.LOG_STATE_CHANGED");
                        GnssLocationProvider.this.enableVerboseLogging();
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
        this.mSvidWithFlags = new int[64];
        this.mCn0s = new float[64];
        this.mSvElevations = new float[64];
        this.mSvAzimuths = new float[64];
        this.mNmeaBuffer = new byte[120];
        this.mSpeed = OppoBrightUtils.MIN_LUX_LIMITI;
        this.mIsFakeReport = true;
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
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mWakeupIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ALARM_WAKEUP), 0);
        this.mTimeoutIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ALARM_TIMEOUT), 0);
        this.mConnMgr = (ConnectivityManager) context.getSystemService("connectivity");
        this.mAppOpsService = IAppOpsService.Stub.asInterface(ServiceManager.getService("appops"));
        this.mBatteryStats = IBatteryStats.Stub.asInterface(ServiceManager.getService("batterystats"));
        this.mHandler = new ProviderHandler(looper);
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
        this.mGpsController = new GpsController(this.mContext, this, this.mILocationManager);
        enableVerboseLogging();
        initLocationExt();
    }

    private void enableVerboseLogging() {
        String str;
        boolean z = false;
        int level = SystemProperties.getInt(MTK_DEBUG_GPSDBLOG_ENABLE_PROPERTY, 0);
        native_set_log_level(level);
        if (level > 0) {
            z = true;
        }
        DEBUG = z;
        VERBOSE = DEBUG;
        if (this.mGpsController != null) {
            this.mGpsController.enableLog(DEBUG);
        }
        String str2 = "persist.sys.oppo.chatty";
        if (DEBUG) {
            str = "0";
        } else {
            str = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON;
        }
        SystemProperties.set(str2, str);
    }

    public String getName() {
        return "gps";
    }

    public ProviderProperties getProperties() {
        return PROPERTIES;
    }

    private void handleUpdateNetworkState(Network network) {
        NetworkInfo info = this.mConnMgr.getNetworkInfo(network);
        if (info != null) {
            Object[] objArr;
            boolean isConnected = info.isConnected();
            if (DEBUG) {
                objArr = new Object[4];
                objArr[0] = agpsDataConnStateAsString();
                objArr[1] = Boolean.valueOf(isConnected);
                objArr[2] = info;
                objArr[3] = this.mConnMgr.getNetworkCapabilities(network);
                Log.d("GnssLocationProvider", String.format("UpdateNetworkState, state=%s, connected=%s, info=%s, capabilities=%S", objArr));
            }
            if (native_is_agps_ril_supported()) {
                boolean networkAvailable = info.isAvailable() ? TelephonyManager.getDefault().getDataEnabled() : false;
                networkLocationHandleByNetworkState(info);
                if (info.getType() != 1) {
                    this.mDefaultApn = getSelectedApn();
                }
                if (this.mDefaultApn == null) {
                    this.mDefaultApn = "dummy-apn";
                }
                native_update_network_state(isConnected, info.getType(), info.isRoaming(), networkAvailable, info.getExtraInfo(), this.mDefaultApn);
            } else if (DEBUG) {
                Log.d("GnssLocationProvider", "Skipped network state update because GPS HAL AGPS-RIL is not  supported");
            }
            if (this.mAGpsDataConnectionState == 1) {
                if (isConnected) {
                    String apnName = info.getExtraInfo();
                    if (apnName == null) {
                        apnName = "dummy-apn";
                    }
                    int apnIpType = getApnIpType(apnName);
                    setRouting();
                    if (DEBUG) {
                        objArr = new Object[2];
                        objArr[0] = apnName;
                        objArr[1] = Integer.valueOf(apnIpType);
                        Log.d("GnssLocationProvider", String.format("native_agps_data_conn_open: mAgpsApn=%s, mApnIpType=%s", objArr));
                    }
                    native_agps_data_conn_open(apnName, apnIpType);
                    this.mAGpsDataConnectionState = 2;
                } else {
                    handleReleaseSuplConnection(5);
                }
            }
        }
    }

    private void handleRequestSuplConnection(InetAddress address) {
        if (DEBUG) {
            Object[] objArr = new Object[2];
            objArr[0] = agpsDataConnStateAsString();
            objArr[1] = address;
            Log.d("GnssLocationProvider", String.format("requestSuplConnection, state=%s, address=%s", objArr));
        }
        if (this.mAGpsDataConnectionState == 0) {
            this.mAGpsDataConnectionIpAddr = address;
            this.mAGpsDataConnectionState = 1;
            Builder requestBuilder = new Builder();
            requestBuilder.addTransportType(0);
            requestBuilder.addCapability(1);
            this.mConnMgr.requestNetwork(requestBuilder.build(), this.mSuplConnectivityCallback, 6000000);
        }
    }

    private void handleReleaseSuplConnection(int agpsDataConnStatus) {
        if (DEBUG) {
            Object[] objArr = new Object[2];
            objArr[0] = agpsDataConnStateAsString();
            objArr[1] = agpsDataConnStatusAsString(agpsDataConnStatus);
            Log.d("GnssLocationProvider", String.format("releaseSuplConnection, state=%s, status=%s", objArr));
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
                            long now = System.currentTimeMillis();
                            if (GnssLocationProvider.DEBUG) {
                                Log.d("GnssLocationProvider", "NTP server returned: " + time + " (" + new Date(time) + ") reference: " + timeReference + " certainty: " + certainty + " system time offset: " + (time - now));
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
                            Object[] objArr = new Object[3];
                            objArr[0] = Boolean.valueOf(GnssLocationProvider.this.mOnDemandTimeInjection);
                            objArr[1] = Boolean.valueOf(refreshSuccess);
                            objArr[2] = Long.valueOf(delay);
                            Log.d("GnssLocationProvider", String.format("onDemandTimeInjection=%s, refreshSuccess=%s, delay=%s", objArr));
                        }
                        if (GnssLocationProvider.this.mOnDemandTimeInjection || !refreshSuccess) {
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
                this.mWakeLock.acquire();
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
                        GnssLocationProvider.this.mWakeLock.release();
                        Log.i("GnssLocationProvider", "WakeLock released by handleDownloadXtraData()");
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
        if (this.mSuplServerHost != null && this.mSuplServerPort > 0 && this.mSuplServerPort <= 65535) {
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
        } else {
            synchronized (this.mLock) {
                this.mEnabled = false;
            }
            Log.w("GnssLocationProvider", "Failed to enable location provider");
        }
        if (this.mInjectNtpTimePending == 0) {
            requestUtcTime();
        }
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
        this.mGpsController.stopController();
        stopNavigating();
        this.mAlarmManager.cancel(this.mWakeupIntent);
        this.mAlarmManager.cancel(this.mTimeoutIntent);
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
        if (extras != null) {
            extras.putInt("satellites", this.mSvCount);
        }
        return this.mStatus;
    }

    private void updateStatus(int status, int svCount) {
        if (status != this.mStatus || svCount != this.mSvCount) {
            this.mStatus = status;
            this.mSvCount = svCount;
            this.mLocationExtras.putInt("satellites", svCount);
            this.mStatusUpdateTime = SystemClock.elapsedRealtime();
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
            if (this.mProviderRequest.reportLocation && !this.mDisableGps && isEnabled()) {
                this.mIsFakeReport = isAllowedFakeReport(this.mWorkSource);
                updateClientUids(this.mWorkSource);
                this.mFixInterval = (int) this.mProviderRequest.interval;
                if (((long) this.mFixInterval) != this.mProviderRequest.interval) {
                    Log.w("GnssLocationProvider", "interval overflow: " + this.mProviderRequest.interval);
                    this.mFixInterval = Integer.MAX_VALUE;
                }
                if (this.mStarted && hasCapability(1)) {
                    Log.d("GnssLocationProvider", "set_position_mode setRequest " + this.mProviderRequest);
                    if (!native_set_position_mode(this.mPositionMode, 0, this.mFixInterval, 0, 0)) {
                        Log.e("GnssLocationProvider", "set_position_mode failed in setMinTime()");
                    }
                } else if (!this.mStarted) {
                    if (this.mGpsController.resistStartGps()) {
                        this.mSingleShot = singleShot;
                    } else {
                        startNavigating(singleShot);
                    }
                }
                LocationExt.setGpsTimeSyncFlag(true);
            } else {
                updateClientUids(new WorkSource());
                this.mGpsController.stopController();
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
        } else if ("force_xtra_injection".equals(command)) {
            if (this.mSupportsXtra) {
                xtraDownloadRequest();
                result = true;
            }
        } else if ("set_vzw_debug_screen".equals(command)) {
            setVzwDebugScreen(extras != null ? extras.getBoolean("enabled") : false);
        } else if ("get_vzw_debug_screen".equals(command)) {
            result = getVzwDebugScreen();
        } else {
            Log.w("GnssLocationProvider", "sendExtraCommand: unknown command " + command);
        }
        Binder.restoreCallingIdentity(identity);
        return result;
    }

    private boolean deleteAidingData(Bundle extras) {
        int flags;
        if (extras == null) {
            flags = 65535;
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
                flags |= 65535;
            }
        }
        flags = LocationExt.getInstance(this, this.mContext, this.mHandler, this.mConnMgr).deleteAidingData(extras, flags);
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
            Log.d("GnssLocationProvider", "startNavigating, singleShot is " + singleShot + " setRequest: " + this.mProviderRequest);
            this.mTimeToFirstFix = 0;
            this.mLastFixTime = 0;
            this.mStarted = true;
            this.mSingleShot = singleShot;
            this.mPositionMode = 0;
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
                        mode = "unknown";
                        break;
                }
                Log.d("GnssLocationProvider", "setting position_mode to " + mode);
            }
            if (!native_set_position_mode(this.mPositionMode, 0, hasCapability(1) ? this.mFixInterval : 1000, 0, 0)) {
                this.mStarted = false;
                Log.e("GnssLocationProvider", "set_position_mode failed in startNavigating()");
            } else if (native_start()) {
                this.mGpsController.startController();
                updateStatus(1, 0);
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
        networkLocationStop();
        if (this.mStarted) {
            Log.d("GnssLocationProvider", "stopNavigating");
            this.mStarted = false;
            this.mSingleShot = false;
            native_stop();
            this.mTimeToFirstFix = 0;
            this.mLastFixTime = 0;
            this.mLocationFlags = 0;
            updateStatus(1, 0);
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

    private void reportLocation(int flags, double latitude, double longitude, double altitude, float speed, float bearing, float accuracy, long timestamp) {
        if (VERBOSE) {
            Log.v("GnssLocationProvider", "reportLocation lat: " + latitude + " long: " + longitude + " timestamp: " + timestamp);
        }
        this.mSpeed = speed;
        networkLocationStop();
        synchronized (this.mLocation) {
            this.mLocationFlags = flags;
            if ((flags & 1) == 1) {
                this.mLocation.setLatitude(latitude);
                this.mLocation.setLongitude(longitude);
                this.mLocation.setTime(timestamp);
                this.mLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
            }
            if ((flags & 2) == 2) {
                this.mLocation.setAltitude(altitude);
            } else {
                this.mLocation.removeAltitude();
            }
            if ((flags & 4) == 4) {
                this.mLocation.setSpeed(speed);
            } else {
                this.mLocation.removeSpeed();
            }
            if ((flags & 8) == 8) {
                this.mLocation.setBearing(bearing);
            } else {
                this.mLocation.removeBearing();
            }
            if ((flags & 16) == 16) {
                this.mLocation.setAccuracy(accuracy);
            } else {
                this.mLocation.removeAccuracy();
            }
            this.mLocation.setExtras(this.mLocationExtras);
            try {
                this.mILocationManager.reportLocation(this.mLocation, false);
            } catch (RemoteException e) {
                Log.e("GnssLocationProvider", "RemoteException calling reportLocation");
            }
        }
        this.mLastFixTime = SystemClock.elapsedRealtime();
        if (this.mTimeToFirstFix == 0 && (flags & 1) == 1) {
            this.mTimeToFirstFix = (int) (this.mLastFixTime - this.mFixRequestTime);
            Log.d("GnssLocationProvider", "TTFF: " + this.mTimeToFirstFix);
            this.mListenerHelper.onFirstFix(this.mTimeToFirstFix);
        }
        LocationExt.doSystemTimeSyncByGps(flags, timestamp);
        if (this.mSingleShot) {
            stopNavigating();
        }
        if (this.mStarted && this.mStatus != 2) {
            if (!hasCapability(1) && this.mFixInterval < 60000) {
                this.mAlarmManager.cancel(this.mTimeoutIntent);
            }
            Intent intent = new Intent("android.location.GPS_FIX_CHANGE");
            intent.putExtra("enabled", true);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            updateStatus(2, this.mSvCount);
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
        int svCount = native_read_sv_status(this.mSvidWithFlags, this.mCn0s, this.mSvElevations, this.mSvAzimuths);
        this.mListenerHelper.onSvStatusChanged(svCount, this.mSvidWithFlags, this.mCn0s, this.mSvElevations, this.mSvAzimuths);
        if (VERBOSE) {
            Log.v("GnssLocationProvider", "SV count: " + svCount);
        }
        int usedInFixCount = 0;
        for (int i = 0; i < svCount; i++) {
            if ((this.mSvidWithFlags[i] & 4) != 0) {
                usedInFixCount++;
            }
            if (VERBOSE) {
                Log.v("GnssLocationProvider", "svid: " + (this.mSvidWithFlags[i] >> 7) + " cn0: " + (this.mCn0s[i] / 10.0f) + " elev: " + this.mSvElevations[i] + " azimuth: " + this.mSvAzimuths[i] + ((this.mSvidWithFlags[i] & 1) == 0 ? "  " : " E") + ((this.mSvidWithFlags[i] & 2) == 0 ? "  " : " A") + ((this.mSvidWithFlags[i] & 4) == 0 ? IElsaManager.EMPTY_PACKAGE : "U"));
            }
        }
        updateStatus(this.mStatus, usedInFixCount);
        if (this.mNavigating && this.mStatus == 2 && this.mLastFixTime > 0 && SystemClock.elapsedRealtime() - this.mLastFixTime > 10000) {
            Intent intent = new Intent("android.location.GPS_FIX_CHANGE");
            intent.putExtra("enabled", false);
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            updateStatus(1, this.mSvCount);
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
        this.mListenerHelper.onNmeaReceived(timestamp, new String(this.mNmeaBuffer, 0, native_read_nmea(this.mNmeaBuffer, this.mNmeaBuffer.length)));
    }

    private void reportMeasurementData(GnssMeasurementsEvent event) {
        this.mGnssMeasurementsProvider.onMeasurementsAvailable(event);
    }

    private void reportNavigationMessage(GnssNavigationMessage event) {
        this.mGnssNavigationMessageProvider.onNavigationMessageAvailable(event);
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

    private void xtraDownloadRequest() {
        if (DEBUG) {
            Log.d("GnssLocationProvider", "xtraDownloadRequest");
        }
        sendMessage(6, 0, null);
    }

    private Location buildLocation(int flags, double latitude, double longitude, double altitude, float speed, float bearing, float accuracy, long timestamp) {
        Location location = new Location("gps");
        if ((flags & 1) == 1) {
            location.setLatitude(latitude);
            location.setLongitude(longitude);
            location.setTime(timestamp);
            location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }
        if ((flags & 2) == 2) {
            location.setAltitude(altitude);
        }
        if ((flags & 4) == 4) {
            location.setSpeed(speed);
        }
        if ((flags & 8) == 8) {
            location.setBearing(bearing);
        }
        if ((flags & 16) == 16) {
            location.setAccuracy(accuracy);
        }
        return location;
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

    private void reportGeofenceTransition(int geofenceId, int flags, double latitude, double longitude, double altitude, float speed, float bearing, float accuracy, long timestamp, int transition, long transitionTimestamp) {
        if (this.mGeofenceHardwareImpl == null) {
            this.mGeofenceHardwareImpl = GeofenceHardwareImpl.getInstance(this.mContext);
        }
        int i = geofenceId;
        this.mGeofenceHardwareImpl.reportGeofenceTransition(i, buildLocation(flags, latitude, longitude, altitude, speed, bearing, accuracy, timestamp), transition, transitionTimestamp, 0, SourceTechnologies.GNSS);
    }

    private void reportGeofenceStatus(int status, int flags, double latitude, double longitude, double altitude, float speed, float bearing, float accuracy, long timestamp) {
        if (this.mGeofenceHardwareImpl == null) {
            this.mGeofenceHardwareImpl = GeofenceHardwareImpl.getInstance(this.mContext);
        }
        Location location = buildLocation(flags, latitude, longitude, altitude, speed, bearing, accuracy, timestamp);
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

    public void reportNiNotification(int notificationId, int niType, int notifyFlags, int timeout, int defaultResponse, String requestorId, String text, int requestorIdEncoding, int textEncoding, String extras) {
        Log.i("GnssLocationProvider", "reportNiNotification: entered");
        Log.i("GnssLocationProvider", "notificationId: " + notificationId + ", niType: " + niType + ", notifyFlags: " + notifyFlags + ", timeout: " + timeout + ", defaultResponse: " + defaultResponse);
        Log.i("GnssLocationProvider", "requestorId: " + requestorId + ", text: " + text + ", requestorIdEncoding: " + requestorIdEncoding + ", textEncoding: " + textEncoding);
        GpsNiNotification notification = new GpsNiNotification();
        notification.notificationId = notificationId;
        notification.niType = niType;
        notification.needNotify = (notifyFlags & 1) != 0;
        notification.needVerify = (notifyFlags & 2) != 0;
        notification.privacyOverride = (notifyFlags & 4) != 0;
        notification.timeout = timeout;
        notification.defaultResponse = defaultResponse;
        notification.requestorId = requestorId;
        notification.text = text;
        notification.requestorIdEncoding = requestorIdEncoding;
        notification.textEncoding = textEncoding;
        Bundle bundle = new Bundle();
        if (extras == null) {
            extras = IElsaManager.EMPTY_PACKAGE;
        }
        Properties extraProp = new Properties();
        try {
            extraProp.load(new StringReader(extras));
        } catch (IOException e) {
            Log.e("GnssLocationProvider", "reportNiNotification cannot parse extras data: " + extras);
        }
        for (Entry<Object, Object> ent : extraProp.entrySet()) {
            bundle.putString((String) ent.getKey(), (String) ent.getValue());
        }
        notification.extras = bundle;
        this.mNIHandler.handleNiNotification(notification);
    }

    private void requestSetID(int flags) {
        TelephonyManager phone = (TelephonyManager) this.mContext.getSystemService("phone");
        int type = 0;
        String data = IElsaManager.EMPTY_PACKAGE;
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

    private void requestRefLocation(int flags) {
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
            Log.i("GnssLocationProvider", "WakeLock acquired by sendMessage(" + message + ", " + arg + ", " + obj + ")");
        }
        this.mHandler.obtainMessage(message, arg, 1, obj).sendToTarget();
    }

    private String getSelectedApn() {
        Uri uri = Uri.parse("content://telephony/carriers/preferapn");
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            String[] strArr = new String[1];
            strArr[0] = "apn";
            cursor = contentResolver.query(uri, strArr, null, null, "name ASC");
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
        Object[] objArr = new Object[1];
        objArr[0] = apn;
        String selection = String.format("current = 1 and apn = '%s' and carrier_enabled = 1", objArr);
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = this.mContext.getContentResolver();
            Uri uri = Carriers.CONTENT_URI;
            String[] strArr = new String[1];
            strArr[0] = "protocol";
            cursor = contentResolver.query(uri, strArr, selection, null, "name ASC");
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
        Object[] objArr = new Object[2];
        objArr[0] = ipProtocol;
        objArr[1] = apn;
        Log.e("GnssLocationProvider", String.format("Unknown IP Protocol: %s, for APN: %s", objArr));
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

    public void initLocationExt() {
        if (SystemProperties.get("ro.mtk_gps_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
            this.mDownloadXtraDataPending = 2;
            this.mInjectNtpTimePending = 2;
            LocationExt.getInstance(this, this.mContext, this.mHandler, this.mConnMgr);
            Log.d("GnssLocationProvider", "LocationExt is created");
        }
    }

    public void setVzwDebugScreen(boolean enabled) {
        Log.d("GnssLocationProvider", "setVzwDebugScreen enabled= " + enabled);
        this.vzwDbgEanbled = enabled;
        native_set_vzw_debug_screen(enabled);
    }

    public boolean getVzwDebugScreen() {
        Log.d("GnssLocationProvider", "getVzwDebugScreen vzwDbgEanbled = " + this.vzwDbgEanbled);
        return this.vzwDbgEanbled;
    }

    public void reportVzwDebugMessage(String vzw_msg) {
        if (DEBUG) {
            Log.d("GnssLocationProvider", "reportVzwDebugMessage vzw_msg: " + vzw_msg);
        }
        Intent intent = new Intent(VZW_DEBUG_MSG);
        intent.putExtra("vzw_dbg", vzw_msg);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        StringBuilder s = new StringBuilder();
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
        s.append(native_get_internal_state());
        pw.append(s);
    }

    protected void wakeGps() {
        startNavigating(this.mSingleShot);
    }

    protected void enterPSMode() {
        stopNavigating();
    }

    protected float getSpeed() {
        return this.mSpeed;
    }

    protected float[] getCn0s() {
        return this.mCn0s;
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

    private void networkLocationHandleByNetworkState(NetworkInfo info) {
        if (info.isConnected()) {
            this.mNetworkConnected = true;
            if (this.mNetworkLocationRequestPending) {
                Log.d("GnssLocationProvider", "Will start a network location request!!!");
                networkLocationStart();
                return;
            }
            return;
        }
        this.mNetworkConnected = false;
        this.mNetworkLocationRequestPending = true;
        networkLocationStop();
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
