package com.android.server;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.app.ActivityManagerNative;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.AppOpsManager.OnOpChangedInternalListener;
import android.app.IActivityManager;
import android.app.IProcessObserver;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.PendingIntent.OnFinished;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageManager.OnPermissionsChangedListener;
import android.content.pm.PackageManagerInternal;
import android.content.pm.PackageManagerInternal.PackagesProvider;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.location.ActivityRecognitionHardware;
import android.location.Address;
import android.location.Criteria;
import android.location.GeocoderParams;
import android.location.Geofence;
import android.location.IFusedGeofenceHardware;
import android.location.IGnssMeasurementsListener;
import android.location.IGnssNavigationMessageListener;
import android.location.IGnssStatusListener;
import android.location.IGnssStatusProvider;
import android.location.IGpsGeofenceHardware;
import android.location.ILocationListener;
import android.location.ILocationManager.Stub;
import android.location.INetInitiatedListener;
import android.location.Location;
import android.location.LocationProvider;
import android.location.LocationRequest;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.WorkSource;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.content.PackageMonitor;
import com.android.internal.location.ProviderProperties;
import com.android.internal.location.ProviderRequest;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.ArrayUtils;
import com.android.server.am.OppoCrashClearManager;
import com.android.server.am.OppoMultiAppManager;
import com.android.server.am.OppoPermissionConstants;
import com.android.server.location.ActivityRecognitionProxy;
import com.android.server.location.FlpHardwareProvider;
import com.android.server.location.FusedProxy;
import com.android.server.location.GeocoderProxy;
import com.android.server.location.GeofenceManager;
import com.android.server.location.GeofenceProxy;
import com.android.server.location.GnssLocationProvider;
import com.android.server.location.GnssLocationProvider.GnssSystemInfoProvider;
import com.android.server.location.GnssMeasurementsProvider;
import com.android.server.location.GnssNavigationMessageProvider;
import com.android.server.location.GpsMonitor;
import com.android.server.location.LocationBlacklist;
import com.android.server.location.LocationFudger;
import com.android.server.location.LocationProviderInterface;
import com.android.server.location.LocationProviderProxy;
import com.android.server.location.LocationRequestStatistics;
import com.android.server.location.LocationRequestStatistics.PackageProviderKey;
import com.android.server.location.LocationRequestStatistics.PackageStatistics;
import com.android.server.location.MockProvider;
import com.android.server.location.OppoGnssWhiteListProxy;
import com.android.server.location.OppoLocationBlacklist;
import com.android.server.location.OppoNlpProxy;
import com.android.server.location.PassiveProvider;
import com.mediatek.cta.CtaUtils;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import oppo.util.OppoStatistics;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

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
public class LocationManagerService extends Stub {
    private static final String ACCESS_LOCATION_EXTRA_COMMANDS = "android.permission.ACCESS_LOCATION_EXTRA_COMMANDS";
    private static final String ACCESS_MOCK_LOCATION = "android.permission.ACCESS_MOCK_LOCATION";
    private static final String ACTION_MTKLOGGER_STATE_CHANGED = "com.mediatek.mtklogger.intent.action.LOG_STATE_CHANGED";
    private static final int ALLOW = 1;
    private static final int CHECK_PROVIDER_TIMER = 5000;
    public static boolean D = false;
    private static final LocationRequest DEFAULT_LOCATION_REQUEST = null;
    private static final String EXTRA_GPS_PKGS = "pkgs";
    private static final int FG_ONLY = 2;
    public static final boolean FORCE_DEBUG = false;
    private static final String FUSED_LOCATION_SERVICE_ACTION = "com.android.location.service.FusedLocationProvider";
    public static final long GPS_DURATION_THRESHOLD = 60000;
    public static final String GPS_OPCUSTOM_FEATURE = "persist.sys.gps_disable";
    private static final String GPS_WORKSOURCE_CHANGE_ACTION = "android.location.GPS_WORKSOURCE_CHANGE";
    private static final long HIGH_POWER_INTERVAL_MS = 300000;
    private static final String INSTALL_LOCATION_PROVIDER = "android.permission.INSTALL_LOCATION_PROVIDER";
    private static final boolean IS_USER_BUILD = false;
    public static final String LOGTAG_GPSLOCATION = "30101";
    private static final int MAX_PROVIDER_SCHEDULING_JITTER_MS = 100;
    private static final int MAX_SIZE = 3;
    private static final int MSG_LOCATION_CHANGED = 1;
    private static final String MTKLOGGER_MOBILELOG_DEBUG_PROPERTY = "debug.MB.running";
    private static final long NANOS_PER_MILLI = 1000000;
    private static final String NETWORK_LOCATION_SERVICE_ACTION = "com.android.location.service.v3.NetworkLocationProvider";
    public static final int OPPO_FAKE_LOCATION_DATA_LENGTH = 3;
    public static final String OPPO_FAKE_LOCATION_SPLIT = "_";
    public static final String OPPO_FAKE_LOCATION_TEST = "oppo.locationtest.data";
    public static final String OPPO_FAKE_LOCATOIN_SWITCH_ON = "1";
    public static final String OPPO_FAKE_LOCATOIN_TEST_SWITCH = "oppo.locationtest.switch";
    private static final int PROHIBIT = 3;
    private static final String PROP_FORCE_DEBUG_KEY = "persist.log.tag.tel_dbg";
    private static final int RESOLUTION_LEVEL_COARSE = 1;
    private static final int RESOLUTION_LEVEL_FINE = 2;
    private static final int RESOLUTION_LEVEL_NONE = 0;
    private static final String TAG = "LocationManagerService";
    public static final String USER_ACTION_REQUEST_GPSLOCATION = "requestGpsLocation";
    private static final String WAKELOCK_KEY = "LocationManagerService";
    private static final String mWhitelistPackage = "com.mediatek.ims";
    private static final String mWhitelistProvider = "network";
    private final String ACTION_FOR_TEST_NAVIGATION_MODE;
    private final String CUSTOMIZE_LIST_PATH;
    private boolean isFakeLocationreporting;
    private boolean isSytemPropRooted;
    private AlarmManager mAlarmManager;
    private IActivityManager mAm;
    private final AppOpsManager mAppOps;
    private LocationBlacklist mBlacklist;
    private final Context mContext;
    private int mCurrentUserId;
    private int[] mCurrentUserProfiles;
    private List<String> mCustomizeList;
    private final Set<String> mDisabledProviders;
    private final Set<String> mEnabledProviders;
    private GeocoderProxy mGeocodeProvider;
    private GeofenceManager mGeofenceManager;
    private GnssMeasurementsProvider mGnssMeasurementsProvider;
    private GnssNavigationMessageProvider mGnssNavigationMessageProvider;
    private IGnssStatusProvider mGnssStatusProvider;
    private GnssSystemInfoProvider mGnssSystemInfoProvider;
    private OppoGnssWhiteListProxy mGnssWhiteListProxy;
    private boolean mGpsBackGroundBlockFeatureDisable;
    private IGpsGeofenceHardware mGpsGeofenceProxy;
    private ArrayList<String> mGpsPkgs;
    private boolean mIsRegister;
    private final HashMap<String, Location> mLastLocation;
    private final HashMap<String, Location> mLastLocationCoarseInterval;
    private LocationFudger mLocationFudger;
    private LocationWorkerHandler mLocationHandler;
    private final Object mLock;
    private final ArrayList<String> mMapList;
    private final HashMap<String, MockProvider> mMockProviders;
    private BroadcastReceiver mNavigationTestReceiver;
    private INetInitiatedListener mNetInitiatedListener;
    private boolean mNetworkEnabled;
    private OppoLocationBlacklist mOppoBlackList;
    private OppoNlpProxy mOppoNlpProxy;
    private PackageManager mPackageManager;
    private final PackageMonitor mPackageMonitor;
    private PassiveProvider mPassiveProvider;
    private PowerManager mPowerManager;
    final IProcessObserver mProcessObserver;
    private boolean mProviderCheckAlarm;
    private PendingIntent mProviderCheckTimeoutIntent;
    private final ArrayList<LocationProviderInterface> mProviders;
    private final HashMap<String, LocationProviderInterface> mProvidersByName;
    private final ArrayList<LocationProviderProxy> mProxyProviders;
    private final HashMap<String, LocationProviderInterface> mRealProviders;
    private final HashMap<Object, Receiver> mReceivers;
    private final HashMap<String, ArrayList<UpdateRecord>> mRecordsByProvider;
    private final LocationRequestStatistics mRequestStatistics;
    private UserManager mUserManager;
    private boolean mWhitelistWorkingMode;

    private class LocationWorkerHandler extends Handler {
        public LocationWorkerHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            boolean z = true;
            switch (msg.what) {
                case 1:
                    LocationManagerService locationManagerService = LocationManagerService.this;
                    Location location = (Location) msg.obj;
                    if (msg.arg1 != 1) {
                        z = false;
                    }
                    locationManagerService.handleLocationChanged(location, z);
                    return;
                default:
                    return;
            }
        }
    }

    private final class Receiver implements DeathRecipient, OnFinished {
        final int mAllowedResolutionLevel;
        final boolean mHideFromAppOps;
        final Object mKey;
        final ILocationListener mListener;
        boolean mOpHighPowerMonitoring;
        boolean mOpMonitoring;
        final String mPackageName;
        int mPendingBroadcasts;
        final PendingIntent mPendingIntent;
        final int mPid;
        final int mUid;
        final HashMap<String, UpdateRecord> mUpdateRecords = new HashMap();
        WakeLock mWakeLock;
        final WorkSource mWorkSource;

        Receiver(ILocationListener listener, PendingIntent intent, int pid, int uid, String packageName, WorkSource workSource, boolean hideFromAppOps) {
            this.mListener = listener;
            this.mPendingIntent = intent;
            if (listener != null) {
                this.mKey = listener.asBinder();
            } else {
                this.mKey = intent;
            }
            this.mAllowedResolutionLevel = LocationManagerService.this.getAllowedResolutionLevel(pid, uid);
            this.mUid = uid;
            this.mPid = pid;
            this.mPackageName = packageName;
            if (workSource != null && workSource.size() <= 0) {
                workSource = null;
            }
            this.mWorkSource = workSource;
            this.mHideFromAppOps = hideFromAppOps;
            updateMonitoring(true);
            this.mWakeLock = LocationManagerService.this.mPowerManager.newWakeLock(1, "LocationManagerService");
            if (workSource == null) {
                workSource = new WorkSource(this.mUid, this.mPackageName);
            }
            this.mWakeLock.setWorkSource(workSource);
        }

        public boolean equals(Object otherObj) {
            if (otherObj instanceof Receiver) {
                return this.mKey.equals(((Receiver) otherObj).mKey);
            }
            return false;
        }

        public int hashCode() {
            return this.mKey.hashCode();
        }

        public String toString() {
            StringBuilder s = new StringBuilder();
            s.append("Reciever[");
            s.append(Integer.toHexString(System.identityHashCode(this)));
            if (this.mListener != null) {
                s.append(" listener");
            } else {
                s.append(" intent");
            }
            for (String p : this.mUpdateRecords.keySet()) {
                s.append(" ").append(((UpdateRecord) this.mUpdateRecords.get(p)).toString());
            }
            s.append("]");
            return s.toString();
        }

        public void updateMonitoring(boolean allow) {
            if (!this.mHideFromAppOps) {
                boolean requestingLocation = false;
                boolean requestingHighPowerLocation = false;
                if (allow) {
                    for (UpdateRecord updateRecord : this.mUpdateRecords.values()) {
                        if (LocationManagerService.this.isAllowedByCurrentUserSettingsLocked(updateRecord.mProvider)) {
                            ProviderProperties properties;
                            requestingLocation = true;
                            LocationProviderInterface locationProvider = (LocationProviderInterface) LocationManagerService.this.mProvidersByName.get(updateRecord.mProvider);
                            if (locationProvider != null) {
                                properties = locationProvider.getProperties();
                            } else {
                                properties = null;
                            }
                            if (properties != null && properties.mPowerRequirement == 3 && updateRecord.mRequest.getInterval() < 300000) {
                                requestingHighPowerLocation = true;
                                break;
                            }
                        }
                    }
                }
                this.mOpMonitoring = updateMonitoring(requestingLocation, this.mOpMonitoring, 41);
                boolean wasHighPowerMonitoring = this.mOpHighPowerMonitoring;
                this.mOpHighPowerMonitoring = updateMonitoring(requestingHighPowerLocation, this.mOpHighPowerMonitoring, 42);
                if (this.mOpHighPowerMonitoring != wasHighPowerMonitoring) {
                    LocationManagerService.this.mContext.sendBroadcastAsUser(new Intent("android.location.HIGH_POWER_REQUEST_CHANGE"), UserHandle.ALL);
                }
            }
        }

        private boolean updateMonitoring(boolean allowMonitoring, boolean currentlyMonitoring, int op) {
            boolean z = false;
            if (currentlyMonitoring) {
                if (!(allowMonitoring && LocationManagerService.this.mAppOps.checkOpNoThrow(op, this.mUid, this.mPackageName) == 0)) {
                    LocationManagerService.this.mAppOps.finishOp(op, this.mUid, this.mPackageName);
                    return false;
                }
            } else if (allowMonitoring) {
                if (LocationManagerService.this.mAppOps.startOpNoThrow(op, this.mUid, this.mPackageName) == 0) {
                    z = true;
                }
                return z;
            }
            return currentlyMonitoring;
        }

        public boolean isListener() {
            return this.mListener != null;
        }

        public boolean isPendingIntent() {
            return this.mPendingIntent != null;
        }

        public ILocationListener getListener() {
            if (this.mListener != null) {
                return this.mListener;
            }
            throw new IllegalStateException("Request for non-existent listener");
        }

        public boolean callStatusChangedLocked(String provider, int status, Bundle extras) {
            if (this.mListener != null) {
                try {
                    synchronized (this) {
                        this.mListener.onStatusChanged(provider, status, extras);
                        incrementPendingBroadcastsLocked();
                    }
                } catch (RemoteException e) {
                    return false;
                }
            }
            Intent statusChanged = new Intent();
            statusChanged.putExtras(new Bundle(extras));
            statusChanged.putExtra("status", status);
            try {
                synchronized (this) {
                    this.mPendingIntent.send(LocationManagerService.this.mContext, 0, statusChanged, this, LocationManagerService.this.mLocationHandler, LocationManagerService.this.getResolutionPermission(this.mAllowedResolutionLevel));
                    incrementPendingBroadcastsLocked();
                }
            } catch (CanceledException e2) {
                return false;
            }
            return true;
        }

        public boolean callLocationChangedLocked(Location location) {
            if (this.mListener != null) {
                try {
                    synchronized (this) {
                        this.mListener.onLocationChanged(new Location(location));
                        incrementPendingBroadcastsLocked();
                    }
                } catch (RemoteException e) {
                    return false;
                }
            }
            Intent locationChanged = new Intent();
            locationChanged.putExtra("location", new Location(location));
            try {
                synchronized (this) {
                    this.mPendingIntent.send(LocationManagerService.this.mContext, 0, locationChanged, this, LocationManagerService.this.mLocationHandler, LocationManagerService.this.getResolutionPermission(this.mAllowedResolutionLevel));
                    incrementPendingBroadcastsLocked();
                }
            } catch (CanceledException e2) {
                return false;
            }
            return true;
        }

        public boolean callProviderEnabledLocked(String provider, boolean enabled) {
            updateMonitoring(true);
            if (this.mListener != null) {
                try {
                    synchronized (this) {
                        if (enabled) {
                            this.mListener.onProviderEnabled(provider);
                        } else {
                            this.mListener.onProviderDisabled(provider);
                        }
                        incrementPendingBroadcastsLocked();
                    }
                } catch (RemoteException e) {
                    return false;
                }
            }
            Intent providerIntent = new Intent();
            providerIntent.putExtra("providerEnabled", enabled);
            try {
                synchronized (this) {
                    this.mPendingIntent.send(LocationManagerService.this.mContext, 0, providerIntent, this, LocationManagerService.this.mLocationHandler, LocationManagerService.this.getResolutionPermission(this.mAllowedResolutionLevel));
                    incrementPendingBroadcastsLocked();
                }
            } catch (CanceledException e2) {
                return false;
            }
            return true;
        }

        public void binderDied() {
            if (LocationManagerService.D) {
                Log.d("LocationManagerService", "Location listener died");
            }
            synchronized (LocationManagerService.this.mLock) {
                LocationManagerService.this.removeUpdatesLocked(this);
            }
            synchronized (this) {
                clearPendingBroadcastsLocked();
            }
        }

        public void onSendFinished(PendingIntent pendingIntent, Intent intent, int resultCode, String resultData, Bundle resultExtras) {
            synchronized (this) {
                decrementPendingBroadcastsLocked();
            }
        }

        private void incrementPendingBroadcastsLocked() {
            int i = this.mPendingBroadcasts;
            this.mPendingBroadcasts = i + 1;
            if (i == 0) {
                this.mWakeLock.acquire();
            }
        }

        private void decrementPendingBroadcastsLocked() {
            int i = this.mPendingBroadcasts - 1;
            this.mPendingBroadcasts = i;
            if (i == 0 && this.mWakeLock.isHeld()) {
                this.mWakeLock.release();
            }
        }

        public void clearPendingBroadcastsLocked() {
            if (this.mPendingBroadcasts > 0) {
                this.mPendingBroadcasts = 0;
                if (this.mWakeLock.isHeld()) {
                    this.mWakeLock.release();
                }
            }
        }
    }

    private class UpdateRecord {
        Location mLastFixBroadcast;
        long mLastStatusBroadcast;
        int mOp;
        final String mProvider;
        final Receiver mReceiver;
        final LocationRequest mRequest;

        UpdateRecord(String provider, LocationRequest request, Receiver receiver) {
            this.mProvider = provider;
            this.mRequest = request;
            this.mReceiver = receiver;
            if ("gps".equals(request.getProvider())) {
                String packageName = this.mReceiver.mPackageName;
                if (LocationManagerService.this.isSystemApp(packageName)) {
                    this.mOp = 1;
                } else if (LocationManagerService.this.mOppoBlackList.inBlacklist(packageName)) {
                    this.mOp = 3;
                } else if (LocationManagerService.this.mOppoBlackList.inBGBlacklist(packageName)) {
                    this.mOp = 2;
                } else {
                    this.mOp = 1;
                }
                if (LocationManagerService.D) {
                    Log.d("LocationManagerService", "pkg:" + packageName + ",Op:" + this.mOp);
                }
            }
            ArrayList<UpdateRecord> records = (ArrayList) LocationManagerService.this.mRecordsByProvider.get(provider);
            if (records == null) {
                records = new ArrayList();
                LocationManagerService.this.mRecordsByProvider.put(provider, records);
            }
            if (!records.contains(this)) {
                records.add(this);
            }
            LocationManagerService.this.mRequestStatistics.startRequesting(this.mReceiver.mPackageName, provider, request.getInterval());
        }

        boolean isBlock() {
            boolean z = false;
            if (LocationManagerService.this.mGpsBackGroundBlockFeatureDisable && LocationManagerService.this.checkWhiteList(this.mReceiver.mPackageName)) {
                if (LocationManagerService.D) {
                    Log.d("LocationManagerService", "checkWhiteList direct return mReceiver.mPackageName = " + this.mReceiver.mPackageName);
                }
                return false;
            }
            if ("gps".equals(this.mRequest.getProvider())) {
                switch (this.mOp) {
                    case 2:
                        if (!LocationManagerService.this.mIsRegister) {
                            return false;
                        }
                        try {
                            if (!this.mReceiver.mPackageName.equals(LocationManagerService.this.mAm.getTopAppName().getPackageName())) {
                                z = true;
                            }
                            return z;
                        } catch (Exception e) {
                            return false;
                        }
                    case 3:
                        return true;
                }
            }
            return false;
        }

        void disposeLocked(boolean removeReceiver) {
            LocationManagerService.this.mRequestStatistics.stopRequesting(this.mReceiver.mPackageName, this.mProvider);
            if (LocationManagerService.isExpROM() && !LocationManagerService.this.mMapList.contains(this.mReceiver.mPackageName) && "gps".equals(this.mProvider)) {
                PackageStatistics stats = (PackageStatistics) LocationManagerService.this.mRequestStatistics.statistics.get(new PackageProviderKey(this.mReceiver.mPackageName, this.mProvider));
                if (stats != null) {
                    long gpsDuration = stats.getLastDurationMs();
                    if (!stats.isRecord() && gpsDuration >= 60000) {
                        HashMap<String, String> map = new HashMap();
                        map.put("duration", Long.toString(gpsDuration / 1000));
                        map.put("pack_name", this.mReceiver.mPackageName);
                        OppoStatistics.onCommon(LocationManagerService.this.mContext, LocationManagerService.LOGTAG_GPSLOCATION, LocationManagerService.USER_ACTION_REQUEST_GPSLOCATION, map, false);
                        stats.setRecord(true);
                        if (LocationManagerService.D) {
                            Log.d("LocationManagerService", "ST pkg:" + this.mReceiver.mPackageName + ",duration:" + gpsDuration);
                        }
                    }
                }
            }
            ArrayList<UpdateRecord> globalRecords = (ArrayList) LocationManagerService.this.mRecordsByProvider.get(this.mProvider);
            if (globalRecords != null) {
                globalRecords.remove(this);
            }
            if (removeReceiver) {
                HashMap<String, UpdateRecord> receiverRecords = this.mReceiver.mUpdateRecords;
                if (receiverRecords != null) {
                    receiverRecords.remove(this.mProvider);
                    if (removeReceiver && receiverRecords.size() == 0) {
                        LocationManagerService.this.removeUpdatesLocked(this.mReceiver);
                    }
                }
            }
        }

        public String toString() {
            StringBuilder s = new StringBuilder();
            s.append("UpdateRecord[");
            s.append(this.mProvider);
            s.append(' ').append(this.mReceiver.mPackageName).append('(');
            s.append(this.mReceiver.mUid).append(')');
            s.append(' ').append(this.mRequest);
            s.append(']');
            return s.toString();
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.LocationManagerService.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.LocationManagerService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.LocationManagerService.<clinit>():void");
    }

    public LocationManagerService(Context context) {
        this.mLock = new Object();
        this.mEnabledProviders = new HashSet();
        this.mDisabledProviders = new HashSet();
        this.mMockProviders = new HashMap();
        this.mReceivers = new HashMap();
        this.mProviders = new ArrayList();
        this.mRealProviders = new HashMap();
        this.mProvidersByName = new HashMap();
        this.mRecordsByProvider = new HashMap();
        this.mRequestStatistics = new LocationRequestStatistics();
        this.mLastLocation = new HashMap();
        this.mLastLocationCoarseInterval = new HashMap();
        this.mProxyProviders = new ArrayList();
        this.mCurrentUserId = 0;
        int[] iArr = new int[1];
        iArr[0] = 0;
        this.mCurrentUserProfiles = iArr;
        this.mProviderCheckAlarm = false;
        this.mWhitelistWorkingMode = false;
        this.mNetworkEnabled = true;
        this.CUSTOMIZE_LIST_PATH = "/system/etc/oppo_customize_whitelist.xml";
        this.mCustomizeList = new ArrayList();
        this.mGpsBackGroundBlockFeatureDisable = false;
        this.isSytemPropRooted = false;
        this.isFakeLocationreporting = false;
        this.mMapList = new ArrayList();
        this.ACTION_FOR_TEST_NAVIGATION_MODE = "action.test.navigation.mode";
        this.mNavigationTestReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (LocationManagerService.D) {
                    if ("action.test.navigation.mode".equals(intent.getAction())) {
                        LocationManagerService.this.getNavigationPackagesList();
                    }
                }
            }
        };
        this.mGpsPkgs = new ArrayList(3);
        this.mIsRegister = false;
        this.mProcessObserver = new IProcessObserver.Stub() {
            /* JADX WARNING: Missing block: B:13:0x005e, code:
            return;
     */
            /* JADX WARNING: Missing block: B:27:0x0087, code:
            return;
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
                if (LocationManagerService.D) {
                    Log.d("LocationManagerService", "pid:" + pid + ",uid:" + uid + ",fg:" + foregroundActivities);
                }
                synchronized (LocationManagerService.this.mLock) {
                    if (LocationManagerService.this.isAllowedByCurrentUserSettingsLocked("gps")) {
                        ArrayList<UpdateRecord> records = (ArrayList) LocationManagerService.this.mRecordsByProvider.get("gps");
                        if (records == null || records.size() == 0) {
                        } else {
                            boolean needUpdate = false;
                            for (UpdateRecord record : records) {
                                if (record.mOp == 2 && record.mReceiver.mUid == uid) {
                                    needUpdate = true;
                                    break;
                                }
                            }
                            if (needUpdate) {
                                LocationManagerService.this.applyRequirementsLocked("gps");
                            }
                        }
                    }
                }
            }

            public void onProcessStateChanged(int pid, int uid, int procState) {
            }

            public void onProcessDied(int pid, int uid) {
            }
        };
        this.mPackageMonitor = new PackageMonitor() {
            /* JADX WARNING: Missing block: B:15:0x0037, code:
            if (r1 == null) goto L_0x0053;
     */
            /* JADX WARNING: Missing block: B:17:?, code:
            r3 = r1.iterator();
     */
            /* JADX WARNING: Missing block: B:19:0x0041, code:
            if (r3.hasNext() == false) goto L_0x0053;
     */
            /* JADX WARNING: Missing block: B:20:0x0043, code:
            com.android.server.LocationManagerService.-wrap12(r6.this$0, (com.android.server.LocationManagerService.Receiver) r3.next());
     */
            /* JADX WARNING: Missing block: B:27:0x0054, code:
            return;
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onPackageDisappeared(String packageName, int reason) {
                Throwable th;
                synchronized (LocationManagerService.this.mLock) {
                    ArrayList<Receiver> deadReceivers = null;
                    try {
                        Iterator receiver$iterator = LocationManagerService.this.mReceivers.values().iterator();
                        while (true) {
                            ArrayList<Receiver> deadReceivers2;
                            try {
                                deadReceivers2 = deadReceivers;
                                if (!receiver$iterator.hasNext()) {
                                    break;
                                }
                                Receiver receiver = (Receiver) receiver$iterator.next();
                                if (receiver.mPackageName.equals(packageName)) {
                                    if (deadReceivers2 == null) {
                                        deadReceivers = new ArrayList();
                                    } else {
                                        deadReceivers = deadReceivers2;
                                    }
                                    deadReceivers.add(receiver);
                                } else {
                                    deadReceivers = deadReceivers2;
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                deadReceivers = deadReceivers2;
                                throw th;
                            }
                        }
                    } catch (Throwable th3) {
                        th = th3;
                    }
                }
            }
        };
        this.mContext = context;
        this.mAppOps = (AppOpsManager) context.getSystemService("appops");
        ((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)).setLocationPackagesProvider(new PackagesProvider() {
            public String[] getPackages(int userId) {
                return LocationManagerService.this.mContext.getResources().getStringArray(17236016);
            }
        });
        if (getVerboseLoggingLevel() > 0) {
            enableVerboseLogging(1);
        } else {
            enableVerboseLogging(0);
        }
        if (D) {
            Log.d("LocationManagerService", "Constructed");
        }
        this.mGnssWhiteListProxy = OppoGnssWhiteListProxy.getInstall(this.mContext);
        this.mGnssWhiteListProxy.setIsDebug(D);
        if (this.mContext.getPackageManager().hasSystemFeature("oppo.business.custom") && this.mContext.getPackageManager().hasSystemFeature("oppo.customize.function.allow_gps_background")) {
            this.mGpsBackGroundBlockFeatureDisable = true;
        }
        if (this.mGpsBackGroundBlockFeatureDisable) {
            this.mCustomizeList = loadCustomizeWhiteList("/system/etc/oppo_customize_whitelist.xml");
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:74:0x0114  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0084 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0084 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0114  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0114  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0084 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0084 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0114  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0114  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0084 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x00fe A:{SYNTHETIC, Splitter: B:63:0x00fe} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0084 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0114  */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x00e5 A:{SYNTHETIC, Splitter: B:55:0x00e5} */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0114  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0084 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00cc A:{SYNTHETIC, Splitter: B:47:0x00cc} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0084 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0114  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00b3 A:{SYNTHETIC, Splitter: B:39:0x00b3} */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0114  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0084 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x009a A:{SYNTHETIC, Splitter: B:31:0x009a} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0084 A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x0114  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x010b A:{SYNTHETIC, Splitter: B:69:0x010b} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private List<String> loadCustomizeWhiteList(String path) {
        IOException e;
        NullPointerException e2;
        NumberFormatException e3;
        XmlPullParserException e4;
        IndexOutOfBoundsException e5;
        Throwable th;
        ArrayList<String> emptyList = new ArrayList();
        File file = new File(path);
        if (file.exists()) {
            ArrayList<String> ret = new ArrayList();
            FileInputStream stream = null;
            boolean success = false;
            try {
                FileInputStream stream2 = new FileInputStream(file);
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(stream2, null);
                    int type;
                    do {
                        type = parser.next();
                        if (type == 2) {
                            if (OppoCrashClearManager.CRASH_CLEAR_NAME.equals(parser.getName())) {
                                String value = parser.getAttributeValue(null, "att");
                                if (value != null) {
                                    ret.add(value);
                                }
                            }
                        }
                    } while (type != 1);
                    success = true;
                    if (stream2 != null) {
                        try {
                            stream2.close();
                        } catch (IOException e6) {
                            e6.printStackTrace();
                        }
                    }
                    stream = stream2;
                } catch (NullPointerException e7) {
                    e2 = e7;
                    stream = stream2;
                    Slog.w("LocationManagerService", "failed parsing ", e2);
                    if (stream != null) {
                    }
                    if (success) {
                    }
                } catch (NumberFormatException e8) {
                    e3 = e8;
                    stream = stream2;
                    Slog.w("LocationManagerService", "failed parsing ", e3);
                    if (stream != null) {
                    }
                    if (success) {
                    }
                } catch (XmlPullParserException e9) {
                    e4 = e9;
                    stream = stream2;
                    Slog.w("LocationManagerService", "failed parsing ", e4);
                    if (stream != null) {
                    }
                    if (success) {
                    }
                } catch (IOException e10) {
                    e6 = e10;
                    stream = stream2;
                    Slog.w("LocationManagerService", "failed parsing ", e6);
                    if (stream != null) {
                    }
                    if (success) {
                    }
                } catch (IndexOutOfBoundsException e11) {
                    e5 = e11;
                    stream = stream2;
                    try {
                        Slog.w("LocationManagerService", "failed parsing ", e5);
                        if (stream != null) {
                        }
                        if (success) {
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (stream != null) {
                            try {
                                stream.close();
                            } catch (IOException e62) {
                                e62.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    stream = stream2;
                    if (stream != null) {
                    }
                    throw th;
                }
            } catch (NullPointerException e12) {
                e2 = e12;
                Slog.w("LocationManagerService", "failed parsing ", e2);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e622) {
                        e622.printStackTrace();
                    }
                }
                if (success) {
                }
            } catch (NumberFormatException e13) {
                e3 = e13;
                Slog.w("LocationManagerService", "failed parsing ", e3);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e6222) {
                        e6222.printStackTrace();
                    }
                }
                if (success) {
                }
            } catch (XmlPullParserException e14) {
                e4 = e14;
                Slog.w("LocationManagerService", "failed parsing ", e4);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e62222) {
                        e62222.printStackTrace();
                    }
                }
                if (success) {
                }
            } catch (IOException e15) {
                e62222 = e15;
                Slog.w("LocationManagerService", "failed parsing ", e62222);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e622222) {
                        e622222.printStackTrace();
                    }
                }
                if (success) {
                }
            } catch (IndexOutOfBoundsException e16) {
                e5 = e16;
                Slog.w("LocationManagerService", "failed parsing ", e5);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e6222222) {
                        e6222222.printStackTrace();
                    }
                }
                if (success) {
                }
            }
            if (success) {
                return ret;
            }
            Slog.w("LocationManagerService", path + " file failed parsing!");
            return emptyList;
        }
        Slog.w("LocationManagerService", path + " file don't exist!");
        return emptyList;
    }

    private boolean checkWhiteList(String packageName) {
        if (this.mCustomizeList == null || this.mCustomizeList.size() <= 0 || packageName == null) {
            return false;
        }
        try {
            for (String pkg : this.mCustomizeList) {
                if (pkg.equalsIgnoreCase(packageName)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Slog.w("LocationManagerService", "check white list has exception! ", e);
            return false;
        }
    }

    public void systemRunning() {
        synchronized (this.mLock) {
            if (D) {
                Log.d("LocationManagerService", "systemRunning()");
            }
            this.mPackageManager = this.mContext.getPackageManager();
            this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
            this.mLocationHandler = new LocationWorkerHandler(BackgroundThread.get().getLooper());
            this.mLocationFudger = new LocationFudger(this.mContext, this.mLocationHandler);
            this.mBlacklist = new LocationBlacklist(this.mContext, this.mLocationHandler);
            this.mBlacklist.init();
            this.mGeofenceManager = new GeofenceManager(this.mContext, this.mBlacklist);
            this.mAm = ActivityManagerNative.getDefault();
            this.mOppoBlackList = new OppoLocationBlacklist(this.mContext, this.mLocationHandler);
            this.mOppoBlackList.init();
            this.mAppOps.startWatchingMode(0, null, new OnOpChangedInternalListener() {
                public void onOpChanged(int op, String packageName) {
                    synchronized (LocationManagerService.this.mLock) {
                        for (Receiver receiver : LocationManagerService.this.mReceivers.values()) {
                            receiver.updateMonitoring(true);
                        }
                        LocationManagerService.this.applyAllProviderRequirementsLocked();
                    }
                }
            });
            this.mPackageManager.addOnPermissionsChangeListener(new OnPermissionsChangedListener() {
                public void onPermissionsChanged(int uid) {
                    synchronized (LocationManagerService.this.mLock) {
                        LocationManagerService.this.applyAllProviderRequirementsLocked();
                    }
                }
            });
            this.mUserManager = (UserManager) this.mContext.getSystemService("user");
            updateUserProfiles(this.mCurrentUserId);
            loadProvidersLocked();
            updateProvidersLocked();
            Log.d("LocationManagerService", "SystemProperties.getInt : " + SystemProperties.getInt("ro.secure", 1));
            this.isSytemPropRooted = SystemProperties.getInt("ro.secure", 1) == 0;
            Log.d("LocationManagerService", "SystemProperties.isSytemPropRooted: " + this.isSytemPropRooted);
        }
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor("location_providers_allowed"), true, new ContentObserver(this.mLocationHandler) {
            public void onChange(boolean selfChange) {
                synchronized (LocationManagerService.this.mLock) {
                    LocationManagerService.this.triggerProviderCheck();
                    LocationManagerService.this.updateProvidersLocked();
                }
            }
        }, -1);
        this.mPackageMonitor.register(this.mContext, this.mLocationHandler.getLooper(), true);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_ADDED");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_REMOVED");
        intentFilter.addAction("android.intent.action.ACTION_SHUTDOWN");
        intentFilter.addAction("com.mediatek.mtklogger.intent.action.LOG_STATE_CHANGED");
        this.mContext.registerReceiverAsUser(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int i = 0;
                String action = intent.getAction();
                if ("android.intent.action.USER_SWITCHED".equals(action)) {
                    LocationManagerService.this.switchUser(intent.getIntExtra("android.intent.extra.user_handle", 0));
                } else if ("android.intent.action.MANAGED_PROFILE_ADDED".equals(action) || "android.intent.action.MANAGED_PROFILE_REMOVED".equals(action)) {
                    LocationManagerService.this.updateUserProfiles(LocationManagerService.this.mCurrentUserId);
                } else if ("android.intent.action.ACTION_SHUTDOWN".equals(action)) {
                    if (LocationManagerService.D) {
                        Log.d("LocationManagerService", "Shutdown received with UserId: " + getSendingUserId());
                    }
                    if (getSendingUserId() == -1) {
                        LocationManagerService.this.shutdownComponents();
                    }
                } else if ("com.mediatek.mtklogger.intent.action.LOG_STATE_CHANGED".equals(action)) {
                    LocationManagerService locationManagerService = LocationManagerService.this;
                    if (LocationManagerService.this.getVerboseLoggingLevel() > 0) {
                        i = 1;
                    }
                    locationManagerService.enableVerboseLogging(i);
                }
            }
        }, UserHandle.ALL, intentFilter, null, this.mLocationHandler);
        String ACTION_BOOT_IPO = "android.intent.action.ACTION_BOOT_IPO";
        String ACTION_SHUTDOWN_IPO = "android.intent.action.ACTION_SHUTDOWN_IPO";
        IntentFilter intentIPOFilter = new IntentFilter();
        if (SystemProperties.get("ro.mtk_ipo_support").equals(OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
            intentIPOFilter.addAction("android.intent.action.ACTION_BOOT_IPO");
            intentIPOFilter.addAction("android.intent.action.ACTION_SHUTDOWN_IPO");
            this.mContext.registerReceiver(new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    synchronized (LocationManagerService.this.mLock) {
                        String action = intent.getAction();
                        if ("android.intent.action.ACTION_SHUTDOWN_IPO".equals(action)) {
                            LocationManagerService.this.locationIPOremoveProviderLocked();
                        } else if ("android.intent.action.ACTION_BOOT_IPO".equals(action)) {
                            LocationManagerService.this.locationIPOcreateProviderLocked();
                            LocationManagerService.this.updateProvidersLocked();
                        }
                    }
                }
            }, intentIPOFilter);
        }
        String ALARM_PVDCHECK = "com.mediatek.location.providercheck";
        IntentFilter intentPvdChkFilter = new IntentFilter();
        intentPvdChkFilter.addAction("com.mediatek.location.providercheck");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("com.mediatek.location.providercheck".equals(intent.getAction())) {
                    synchronized (LocationManagerService.this.mLock) {
                        LocationManagerService.this.removeProviderCheck();
                        LocationManagerService.this.updateProvidersLocked();
                    }
                }
            }
        }, intentPvdChkFilter);
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        this.mProviderCheckTimeoutIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent("com.mediatek.location.providercheck"), 0);
    }

    private void shutdownComponents() {
        if (D) {
            Log.d("LocationManagerService", "Shutting down components...");
        }
        LocationProviderInterface gpsProvider = (LocationProviderInterface) this.mProvidersByName.get("gps");
        if (gpsProvider != null && gpsProvider.isEnabled()) {
            gpsProvider.disable();
        }
        if (FlpHardwareProvider.isSupported()) {
            FlpHardwareProvider.getInstance(this.mContext).cleanup();
        }
        this.mOppoBlackList.shutdown();
    }

    void updateUserProfiles(int currentUserId) {
        int[] profileIds = this.mUserManager.getProfileIdsWithDisabled(currentUserId);
        synchronized (this.mLock) {
            this.mCurrentUserProfiles = profileIds;
        }
    }

    private boolean isCurrentProfile(int userId) {
        if (OppoMultiAppManager.getInstance().isCurrentProfile(userId)) {
            return true;
        }
        boolean contains;
        synchronized (this.mLock) {
            contains = ArrayUtils.contains(this.mCurrentUserProfiles, userId);
        }
        return contains;
    }

    private void ensureFallbackFusedProviderPresentLocked(ArrayList<String> pkgs) {
        PackageManager pm = this.mContext.getPackageManager();
        String systemPackageName = this.mContext.getPackageName();
        ArrayList<HashSet<Signature>> sigSets = ServiceWatcher.getSignatureSets(this.mContext, pkgs);
        for (ResolveInfo rInfo : pm.queryIntentServicesAsUser(new Intent(FUSED_LOCATION_SERVICE_ACTION), 128, this.mCurrentUserId)) {
            String packageName = rInfo.serviceInfo.packageName;
            try {
                if (!ServiceWatcher.isSignatureMatch(pm.getPackageInfo(packageName, 64).signatures, sigSets)) {
                    Log.w("LocationManagerService", packageName + " resolves service " + FUSED_LOCATION_SERVICE_ACTION + ", but has wrong signature, ignoring");
                } else if (rInfo.serviceInfo.metaData == null) {
                    Log.w("LocationManagerService", "Found fused provider without metadata: " + packageName);
                } else if (rInfo.serviceInfo.metaData.getInt(ServiceWatcher.EXTRA_SERVICE_VERSION, -1) == 0) {
                    if ((rInfo.serviceInfo.applicationInfo.flags & 1) == 0) {
                        if (D) {
                            Log.d("LocationManagerService", "Fallback candidate not in /system: " + packageName);
                        }
                    } else if (pm.checkSignatures(systemPackageName, packageName) == 0) {
                        if (D) {
                            Log.d("LocationManagerService", "Found fallback provider: " + packageName);
                        }
                        return;
                    } else if (D) {
                        Log.d("LocationManagerService", "Fallback candidate not signed the same as system: " + packageName);
                    }
                } else if (D) {
                    Log.d("LocationManagerService", "Fallback candidate not version 0: " + packageName);
                }
            } catch (NameNotFoundException e) {
                Log.e("LocationManagerService", "missing package: " + packageName);
            }
        }
    }

    public int getVerboseLoggingLevel() {
        return SystemProperties.getInt(MTKLOGGER_MOBILELOG_DEBUG_PROPERTY, 0);
    }

    public void enableVerboseLogging(int verbose) {
        boolean z = false;
        if (verbose > 0) {
            z = true;
        }
        D = z;
        Log.d("LocationManagerService", "debug enable:" + D);
    }

    private void loadProvidersLocked() {
        FlpHardwareProvider flpHardwareProvider;
        IFusedGeofenceHardware geofenceHardware;
        PassiveProvider passiveProvider = new PassiveProvider(this);
        addProviderLocked(passiveProvider);
        this.mEnabledProviders.add(passiveProvider.getName());
        this.mPassiveProvider = passiveProvider;
        if (GnssLocationProvider.isSupported()) {
            GnssLocationProvider gnssLocationProvider = new GnssLocationProvider(this.mContext, this, this.mOppoBlackList, this.mLocationHandler.getLooper());
            this.mGnssSystemInfoProvider = gnssLocationProvider.getGnssSystemInfoProvider();
            this.mGnssStatusProvider = gnssLocationProvider.getGnssStatusProvider();
            this.mNetInitiatedListener = gnssLocationProvider.getNetInitiatedListener();
            addProviderLocked(gnssLocationProvider);
            this.mRealProviders.put("gps", gnssLocationProvider);
            this.mGnssMeasurementsProvider = gnssLocationProvider.getGnssMeasurementsProvider();
            this.mGnssNavigationMessageProvider = gnssLocationProvider.getGnssNavigationMessageProvider();
            this.mGpsGeofenceProxy = gnssLocationProvider.getGpsGeofenceProxy();
        }
        Resources resources = this.mContext.getResources();
        ArrayList<String> providerPackageNames = new ArrayList();
        String[] pkgs = resources.getStringArray(17236016);
        if (D) {
            Log.d("LocationManagerService", "certificates for location providers pulled from: " + Arrays.toString(pkgs));
        }
        if (pkgs != null) {
            providerPackageNames.addAll(Arrays.asList(pkgs));
        }
        ensureFallbackFusedProviderPresentLocked(providerPackageNames);
        LocationProviderInterface networkProvider = OppoNlpProxy.createAndBind(this.mContext, mWhitelistProvider, NETWORK_LOCATION_SERVICE_ACTION, isExpROM(), this.mLocationHandler);
        this.mOppoNlpProxy = (OppoNlpProxy) networkProvider;
        if (networkProvider != null) {
            this.mRealProviders.put(mWhitelistProvider, networkProvider);
            addProviderLocked(networkProvider);
        } else {
            Slog.w("LocationManagerService", "no network location provider found");
        }
        LocationProviderProxy fusedLocationProvider = LocationProviderProxy.createAndBind(this.mContext, "fused", FUSED_LOCATION_SERVICE_ACTION, 17956944, 17039425, 17236016, this.mLocationHandler);
        if (fusedLocationProvider != null) {
            addProviderLocked(fusedLocationProvider);
            this.mProxyProviders.add(fusedLocationProvider);
            this.mEnabledProviders.add(fusedLocationProvider.getName());
            this.mRealProviders.put("fused", fusedLocationProvider);
        } else {
            Slog.e("LocationManagerService", "no fused location provider found", new IllegalStateException("Location service needs a fused location provider"));
        }
        this.mGeocodeProvider = GeocoderProxy.createAndBind(this.mContext, 17956946, 17039427, 17236016, 134479876, 0, this.mLocationHandler);
        if (this.mGeocodeProvider == null) {
            Slog.e("LocationManagerService", "no geocoder provider found");
        }
        if (this.mOppoNlpProxy != null) {
            this.mOppoNlpProxy.setGeocodeProvider(this.mGeocodeProvider);
        }
        if (FlpHardwareProvider.isSupported()) {
            flpHardwareProvider = FlpHardwareProvider.getInstance(this.mContext);
            if (FusedProxy.createAndBind(this.mContext, this.mLocationHandler, flpHardwareProvider.getLocationHardware(), 17956945, 17039426, 17236016) == null) {
                Slog.d("LocationManagerService", "Unable to bind FusedProxy.");
            }
        } else {
            flpHardwareProvider = null;
            Slog.d("LocationManagerService", "FLP HAL not supported");
        }
        Context context = this.mContext;
        Handler handler = this.mLocationHandler;
        IGpsGeofenceHardware iGpsGeofenceHardware = this.mGpsGeofenceProxy;
        if (flpHardwareProvider != null) {
            geofenceHardware = flpHardwareProvider.getGeofenceHardware();
        } else {
            geofenceHardware = null;
        }
        if (GeofenceProxy.createAndBind(context, 17956947, 17039428, 17236016, handler, iGpsGeofenceHardware, geofenceHardware) == null) {
            Slog.d("LocationManagerService", "Unable to bind FLP Geofence proxy.");
        }
        boolean activityRecognitionHardwareIsSupported = ActivityRecognitionHardware.isSupported();
        ActivityRecognitionHardware activityRecognitionHardware = null;
        if (activityRecognitionHardwareIsSupported) {
            activityRecognitionHardware = ActivityRecognitionHardware.getInstance(this.mContext);
        } else {
            Slog.d("LocationManagerService", "Hardware Activity-Recognition not supported.");
        }
        if (ActivityRecognitionProxy.createAndBind(this.mContext, this.mLocationHandler, activityRecognitionHardwareIsSupported, activityRecognitionHardware, 17956948, 17039429, 17236016) == null) {
            Slog.d("LocationManagerService", "Unable to bind ActivityRecognitionProxy.");
        }
        for (String split : resources.getStringArray(17236017)) {
            String[] fragments = split.split(",");
            String name = fragments[0].trim();
            if (this.mProvidersByName.get(name) != null) {
                throw new IllegalArgumentException("Provider \"" + name + "\" already exists");
            }
            addTestProviderLocked(name, new ProviderProperties(Boolean.parseBoolean(fragments[1]), Boolean.parseBoolean(fragments[2]), Boolean.parseBoolean(fragments[3]), Boolean.parseBoolean(fragments[4]), Boolean.parseBoolean(fragments[5]), Boolean.parseBoolean(fragments[6]), Boolean.parseBoolean(fragments[7]), Integer.parseInt(fragments[8]), Integer.parseInt(fragments[9])));
        }
    }

    private void locationIPOremoveProviderLocked() {
        Log.d("LocationManagerService", "IPO shutdown for location");
        LocationProviderInterface realProvider = (LocationProviderInterface) this.mRealProviders.get(mWhitelistProvider);
        if (realProvider != null) {
            LocationProviderProxy networkProvider = (LocationProviderProxy) realProvider;
            networkProvider.unbind();
            this.mRealProviders.remove(mWhitelistProvider);
            this.mProxyProviders.remove(networkProvider);
            removeProviderLocked(realProvider);
        }
        realProvider = (LocationProviderInterface) this.mRealProviders.get("fused");
        if (realProvider != null) {
            LocationProviderProxy fusedProvider = (LocationProviderProxy) realProvider;
            fusedProvider.unbind();
            this.mRealProviders.remove("fused");
            this.mProxyProviders.remove(fusedProvider);
            removeProviderLocked(realProvider);
        }
        if (this.mGeocodeProvider != null) {
            this.mGeocodeProvider.unbind();
            this.mGeocodeProvider = null;
        }
        this.mLastLocation.clear();
        this.mLastLocationCoarseInterval.clear();
        updateProviderListenersLocked("gps", false);
    }

    private void locationIPOcreateProviderLocked() {
        Log.d("LocationManagerService", "IPO powerup for location");
        Resources resources = this.mContext.getResources();
        ArrayList<String> providerPackageNames = new ArrayList();
        String[] pkgs = resources.getStringArray(17236016);
        if (D) {
            Log.d("LocationManagerService", "certificates for location providers pulled from: " + Arrays.toString(pkgs));
        }
        if (pkgs != null) {
            providerPackageNames.addAll(Arrays.asList(pkgs));
        }
        ensureFallbackFusedProviderPresentLocked(providerPackageNames);
        LocationProviderProxy networkProvider = LocationProviderProxy.createAndBind(this.mContext, mWhitelistProvider, NETWORK_LOCATION_SERVICE_ACTION, 17956943, 17039424, 17236016, 134479876, 0, this.mLocationHandler);
        if (networkProvider != null) {
            this.mRealProviders.put(mWhitelistProvider, networkProvider);
            this.mProxyProviders.add(networkProvider);
            addProviderLocked(networkProvider);
        } else {
            Slog.w("LocationManagerService", "no network location provider found");
        }
        LocationProviderProxy fusedLocationProvider = LocationProviderProxy.createAndBind(this.mContext, "fused", FUSED_LOCATION_SERVICE_ACTION, 17956944, 17039425, 17236016, this.mLocationHandler);
        if (fusedLocationProvider != null) {
            addProviderLocked(fusedLocationProvider);
            this.mProxyProviders.add(fusedLocationProvider);
            this.mEnabledProviders.add(fusedLocationProvider.getName());
            this.mRealProviders.put("fused", fusedLocationProvider);
        } else {
            Slog.e("LocationManagerService", "no fused location provider found", new IllegalStateException("Location service needs a fused location provider"));
        }
        this.mGeocodeProvider = GeocoderProxy.createAndBind(this.mContext, 17956946, 17039427, 17236016, 134479876, 0, this.mLocationHandler);
        if (this.mGeocodeProvider == null) {
            Slog.e("LocationManagerService", "no geocoder provider found");
        }
    }

    private void triggerProviderCheck() {
        if (D) {
            Log.d("LocationManagerService", "triggerProviderCheck before: " + this.mProviderCheckAlarm + " to:true");
        }
        this.mProviderCheckAlarm = true;
        this.mAlarmManager.cancel(this.mProviderCheckTimeoutIntent);
        this.mAlarmManager.set(2, SystemClock.elapsedRealtime() + 5000, this.mProviderCheckTimeoutIntent);
    }

    private void removeProviderCheck() {
        this.mProviderCheckAlarm = false;
        if (D) {
            Log.d("LocationManagerService", "removeProviderCheck : " + this.mProviderCheckAlarm);
        }
        this.mAlarmManager.cancel(this.mProviderCheckTimeoutIntent);
    }

    private void switchUser(int userId) {
        if (this.mCurrentUserId != userId) {
            this.mBlacklist.switchUser(userId);
            this.mLocationHandler.removeMessages(1);
            synchronized (this.mLock) {
                this.mLastLocation.clear();
                this.mLastLocationCoarseInterval.clear();
                for (LocationProviderInterface p : this.mProviders) {
                    updateProviderListenersLocked(p.getName(), false);
                }
                this.mCurrentUserId = userId;
                updateUserProfiles(userId);
                updateProvidersLocked();
            }
        }
    }

    public void locationCallbackFinished(ILocationListener listener) {
        synchronized (this.mLock) {
            Receiver receiver = (Receiver) this.mReceivers.get(listener.asBinder());
            if (receiver != null) {
                synchronized (receiver) {
                    long identity = Binder.clearCallingIdentity();
                    receiver.decrementPendingBroadcastsLocked();
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }
    }

    public int getGnssYearOfHardware() {
        if (this.mGnssNavigationMessageProvider != null) {
            return this.mGnssSystemInfoProvider.getGnssYearOfHardware();
        }
        return 0;
    }

    private void initMapList() {
        this.mMapList.add("com.baidu.BaiduMap");
        this.mMapList.add("com.autonavi.minimap");
        this.mMapList.add("com.amap.android.ams");
        this.mMapList.add("com.google.android.apps.maps");
    }

    public List<String> getNavigationPackagesList() {
        Throwable th;
        List<String> list = null;
        int mode = GpsMonitor.getNavigateMode();
        if (!(2 == mode || -1 == mode)) {
            synchronized (this.mLock) {
                try {
                    ArrayList<UpdateRecord> records = (ArrayList) this.mRecordsByProvider.get("gps");
                    if (records != null) {
                        ArrayList<String> pkgList = new ArrayList();
                        try {
                            for (UpdateRecord r : records) {
                                pkgList.add(r.mReceiver.mPackageName);
                            }
                            list = pkgList;
                        } catch (Throwable th2) {
                            th = th2;
                            ArrayList<String> arrayList = pkgList;
                            throw th;
                        }
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
        }
        if (D) {
            Log.d("LocationManagerService", "PF03:NAV Mode:" + mode + ",pkg:" + list);
        }
        return list;
    }

    private void registTestBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("action.test.navigation.mode");
        this.mContext.registerReceiver(this.mNavigationTestReceiver, intentFilter);
    }

    private void addProviderLocked(LocationProviderInterface provider) {
        this.mProviders.add(provider);
        this.mProvidersByName.put(provider.getName(), provider);
    }

    private void removeProviderLocked(LocationProviderInterface provider) {
        provider.disable();
        this.mProviders.remove(provider);
        this.mProvidersByName.remove(provider.getName());
    }

    private boolean isAllowedByCurrentUserSettingsLocked(String provider) {
        if (this.mEnabledProviders.contains(provider)) {
            return true;
        }
        if (this.mDisabledProviders.contains(provider)) {
            return false;
        }
        return Secure.isLocationProviderEnabledForUser(this.mContext.getContentResolver(), provider, this.mCurrentUserId);
    }

    private boolean isAllowedByCurrentUserSettingsLockedForOppo(String provider) {
        if (this.mEnabledProviders.contains(provider)) {
            return true;
        }
        if (this.mDisabledProviders.contains(provider)) {
            return false;
        }
        boolean isEnabled = Secure.isLocationProviderEnabledForUser(this.mContext.getContentResolver(), provider, this.mCurrentUserId);
        if (this.mGnssWhiteListProxy != null && this.mGnssWhiteListProxy.isNetworkLocationAlwayOn() && provider.equals(mWhitelistProvider)) {
            this.mNetworkEnabled = isEnabled;
            isEnabled = true;
            if (!this.mNetworkEnabled) {
                Log.d("LocationManagerService", "Network location allow starting!!");
            }
        }
        return isEnabled;
    }

    private boolean isAllowedByUserSettingsLockedForOppo(String provider, int uid) {
        if (isCurrentProfile(UserHandle.getUserId(uid)) || isUidALocationProvider(uid)) {
            return isAllowedByCurrentUserSettingsLockedForOppo(provider);
        }
        return false;
    }

    private boolean isAllowedLocationRequested(String provider, int uid, String packageName) {
        boolean isAllowed = isAllowedByUserSettingsLockedForOppo(provider, uid);
        if (provider.equals(mWhitelistProvider)) {
            isAllowed = !this.mNetworkEnabled ? isNetworkWhiteList(packageName) : true;
            if (!this.mNetworkEnabled && D) {
                Log.d("LocationManagerService", "allow network request--" + packageName + ", " + isNetworkWhiteList(packageName));
            }
        }
        return isAllowed;
    }

    private boolean isAllowedLocationChanged(String provider, String packageName) {
        boolean z = true;
        if (!provider.equals(mWhitelistProvider)) {
            return true;
        }
        if (!this.mNetworkEnabled && D) {
            Log.d("LocationManagerService", "allow network location--" + packageName + ", " + isNetworkWhiteList(packageName));
        }
        if (!this.mNetworkEnabled) {
            z = isNetworkWhiteList(packageName);
        }
        return z;
    }

    private boolean isNetworkWhiteList(String packageName) {
        if (packageName == null || this.mGnssWhiteListProxy == null) {
            return false;
        }
        return this.mGnssWhiteListProxy.inNetworkLocationWhiteList(packageName);
    }

    private boolean isAllowedByUserSettingsLocked(String provider, int uid) {
        if (isCurrentProfile(UserHandle.getUserId(uid)) || isUidALocationProvider(uid)) {
            return isAllowedByCurrentUserSettingsLocked(provider);
        }
        return false;
    }

    private String getResolutionPermission(int resolutionLevel) {
        switch (resolutionLevel) {
            case 1:
                return "android.permission.ACCESS_COARSE_LOCATION";
            case 2:
                return OppoPermissionConstants.PERMISSION_ACCESS;
            default:
                return null;
        }
    }

    private int getAllowedResolutionLevel(int pid, int uid) {
        if (this.mContext.checkPermission(OppoPermissionConstants.PERMISSION_ACCESS, pid, uid) == 0 && checkOp(pid, uid, 1)) {
            return 2;
        }
        return (this.mContext.checkPermission("android.permission.ACCESS_COARSE_LOCATION", pid, uid) == 0 && checkOp(pid, uid, 0)) ? 1 : 0;
    }

    private boolean checkOp(int pid, int uid, int op) {
        if (!CtaUtils.isCtaSupported()) {
            return true;
        }
        boolean granted = false;
        String[] pkgs = this.mContext.getPackageManager().getPackagesForUid(uid);
        if (pkgs == null) {
            if (D) {
                Log.d("LocationManagerService", "checkOp(pid = " + pid + ", uid = " + uid + ") pkg == null, return false");
            }
            return false;
        }
        for (String pkg : pkgs) {
            if (this.mAppOps.checkOpNoThrow(op, uid, pkg) == 0) {
                granted = true;
                break;
            }
        }
        return granted;
    }

    private int getCallerAllowedResolutionLevel() {
        return getAllowedResolutionLevel(Binder.getCallingPid(), Binder.getCallingUid());
    }

    private void checkResolutionLevelIsSufficientForGeofenceUse(int allowedResolutionLevel) {
        if (allowedResolutionLevel < 2) {
            throw new SecurityException("Geofence usage requires ACCESS_FINE_LOCATION permission");
        }
    }

    private int getMinimumResolutionLevelForProviderUse(String provider) {
        if ("gps".equals(provider) || "passive".equals(provider)) {
            return 2;
        }
        if (mWhitelistProvider.equals(provider) || "fused".equals(provider)) {
            return 1;
        }
        LocationProviderInterface lp = (LocationProviderInterface) this.mMockProviders.get(provider);
        if (lp != null) {
            ProviderProperties properties = lp.getProperties();
            if (properties == null || properties.mRequiresSatellite) {
                return 2;
            }
            if (properties.mRequiresNetwork || properties.mRequiresCell) {
                return 1;
            }
        }
        return 2;
    }

    private void checkResolutionLevelIsSufficientForProviderUse(int allowedResolutionLevel, String providerName) {
        int requiredResolutionLevel = getMinimumResolutionLevelForProviderUse(providerName);
        if (allowedResolutionLevel < requiredResolutionLevel) {
            switch (requiredResolutionLevel) {
                case 1:
                    throw new SecurityException("\"" + providerName + "\" location provider " + "requires ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission.");
                case 2:
                    throw new SecurityException("\"" + providerName + "\" location provider " + "requires ACCESS_FINE_LOCATION permission.");
                default:
                    throw new SecurityException("Insufficient permission for \"" + providerName + "\" location provider.");
            }
        }
    }

    private void checkDeviceStatsAllowed() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.UPDATE_DEVICE_STATS", null);
    }

    private void checkUpdateAppOpsAllowed() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.UPDATE_APP_OPS_STATS", null);
    }

    public static int resolutionLevelToOp(int allowedResolutionLevel) {
        if (allowedResolutionLevel != 0) {
            return allowedResolutionLevel == 1 ? 0 : 1;
        } else {
            return -1;
        }
    }

    boolean reportLocationAccessNoThrow(int pid, int uid, String packageName, int allowedResolutionLevel) {
        int op = resolutionLevelToOp(allowedResolutionLevel);
        if (op >= 0 && this.mAppOps.noteOpNoThrow(op, uid, packageName) != 0) {
            if (!CtaUtils.isCtaSupported()) {
                return false;
            }
            if (op != 1) {
                if (D) {
                    Log.d("LocationManagerService", "reportLocationAccessNoThrow(op!=OP_COARSE_LOCATION) returns false");
                }
                return false;
            } else if (this.mAppOps.noteOpNoThrow(0, uid, packageName) != 0) {
                if (D) {
                    Log.d("LocationManagerService", "reportLocationAccessNoThrow(op = OP_COARSE_LOCATION, uid = " + uid + ", pkg = " + packageName + ") != ALLOWED");
                }
                return false;
            }
        }
        if (getAllowedResolutionLevel(pid, uid) >= allowedResolutionLevel) {
            return true;
        }
        if (D) {
            Log.d("LocationManagerService", "reportLocationAccessNoThrow() - getAllowedResolutionLevel(pid=," + pid + "uid = " + uid + ") = " + getAllowedResolutionLevel(pid, uid) + " < allowedResolutionLevel = " + allowedResolutionLevel);
        }
        return false;
    }

    boolean checkLocationAccess(int pid, int uid, String packageName, int allowedResolutionLevel) {
        int op = resolutionLevelToOp(allowedResolutionLevel);
        if (op >= 0 && this.mAppOps.checkOp(op, uid, packageName) != 0) {
            if (!CtaUtils.isCtaSupported()) {
                return false;
            }
            if (op != 1) {
                if (D) {
                    Log.d("LocationManagerService", "checkLocationAccess(op!=OP_COARSE_LOCATION) returns false");
                }
                return false;
            } else if (this.mAppOps.checkOp(0, uid, packageName) != 0) {
                if (D) {
                    Log.d("LocationManagerService", "checkLocationAccess(op = OP_COARSE_LOCATION , uid = " + uid + ", pkg = " + packageName + ") != ALLOWED");
                }
                return false;
            }
        }
        if (getAllowedResolutionLevel(pid, uid) >= allowedResolutionLevel) {
            return true;
        }
        if (D) {
            Log.d("LocationManagerService", "checkLocationAccess() - getAllowedResolutionLevel(pid=," + pid + "uid = " + uid + ") = " + getAllowedResolutionLevel(pid, uid) + " < allowedResolutionLevel = " + allowedResolutionLevel);
        }
        return false;
    }

    public List<String> getAllProviders() {
        ArrayList<String> out;
        synchronized (this.mLock) {
            out = new ArrayList(this.mProviders.size());
            for (LocationProviderInterface provider : this.mProviders) {
                String name = provider.getName();
                if (!"fused".equals(name)) {
                    out.add(name);
                }
            }
        }
        if (D) {
            Log.d("LocationManagerService", "getAllProviders()=" + out);
        }
        return out;
    }

    public List<String> getProviders(Criteria criteria, boolean enabledOnly) {
        int allowedResolutionLevel = getCallerAllowedResolutionLevel();
        int uid = Binder.getCallingUid();
        long identity = Binder.clearCallingIdentity();
        try {
            ArrayList<String> out;
            synchronized (this.mLock) {
                out = new ArrayList(this.mProviders.size());
                for (LocationProviderInterface provider : this.mProviders) {
                    String name = provider.getName();
                    if (!"fused".equals(name) && allowedResolutionLevel >= getMinimumResolutionLevelForProviderUse(name)) {
                        if ((!enabledOnly || isAllowedByUserSettingsLocked(name, uid)) && (criteria == null || LocationProvider.propertiesMeetCriteria(name, provider.getProperties(), criteria))) {
                            out.add(name);
                        }
                    }
                }
            }
            if (D) {
                Log.d("LocationManagerService", "getProviders()=" + out);
            }
            return out;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public String getBestProvider(Criteria criteria, boolean enabledOnly) {
        List<String> providers = getProviders(criteria, enabledOnly);
        String result;
        if (providers.isEmpty()) {
            providers = getProviders(null, enabledOnly);
            if (providers.isEmpty()) {
                if (D) {
                    Log.d("LocationManagerService", "getBestProvider(" + criteria + ", " + enabledOnly + ")=" + null);
                }
                return null;
            }
            result = pickBest(providers);
            if (D) {
                Log.d("LocationManagerService", "getBestProvider(" + criteria + ", " + enabledOnly + ")=" + result);
            }
            return result;
        }
        result = pickBest(providers);
        if (D) {
            Log.d("LocationManagerService", "getBestProvider(" + criteria + ", " + enabledOnly + ")=" + result);
        }
        return result;
    }

    private String pickBest(List<String> providers) {
        if (providers.contains("gps")) {
            return "gps";
        }
        if (providers.contains(mWhitelistProvider)) {
            return mWhitelistProvider;
        }
        return (String) providers.get(0);
    }

    public boolean providerMeetsCriteria(String provider, Criteria criteria) {
        LocationProviderInterface p = (LocationProviderInterface) this.mProvidersByName.get(provider);
        if (p == null) {
            throw new IllegalArgumentException("provider=" + provider);
        }
        boolean result = LocationProvider.propertiesMeetCriteria(p.getName(), p.getProperties(), criteria);
        if (D) {
            Log.d("LocationManagerService", "providerMeetsCriteria(" + provider + ", " + criteria + ")=" + result);
        }
        return result;
    }

    private void updateProvidersLocked() {
        boolean changesMade = false;
        for (int i = this.mProviders.size() - 1; i >= 0; i--) {
            LocationProviderInterface p = (LocationProviderInterface) this.mProviders.get(i);
            boolean isEnabled = p.isEnabled();
            String name = p.getName();
            boolean shouldBeEnabled = isAllowedByCurrentUserSettingsLockedForOppo(name);
            if (isEnabled && !shouldBeEnabled) {
                updateProviderListenersLocked(name, false);
                this.mLastLocation.clear();
                this.mLastLocationCoarseInterval.clear();
                changesMade = true;
            } else if (!isEnabled && shouldBeEnabled) {
                updateProviderListenersLocked(name, true);
                changesMade = true;
            } else if (shouldBeEnabled && isWhitelistWorkingMode(name)) {
                this.mWhitelistWorkingMode = false;
                Log.d("LocationManagerService", "Exit whitelist only mode, skip provider enabling");
            }
            Log.d("LocationManagerService", "updateProvidersLocked provider:" + name + " changesMade: " + changesMade + " isEnabled:" + isEnabled + " shouldBeEnabled:" + shouldBeEnabled);
        }
        if (changesMade) {
            this.mContext.sendBroadcastAsUser(new Intent("android.location.PROVIDERS_CHANGED"), UserHandle.ALL);
            this.mContext.sendBroadcastAsUser(new Intent("android.location.MODE_CHANGED"), UserHandle.ALL);
        }
    }

    private static boolean isExpROM() {
        return !SystemProperties.get("persist.sys.oppo.region", "CN").equalsIgnoreCase("CN");
    }

    private void updateProviderListenersLocked(String provider, boolean enabled) {
        int listeners = 0;
        LocationProviderInterface p = (LocationProviderInterface) this.mProvidersByName.get(provider);
        if (p != null) {
            int i;
            ArrayList arrayList = null;
            ArrayList<UpdateRecord> records = (ArrayList) this.mRecordsByProvider.get(provider);
            if (records != null) {
                int N = records.size();
                for (i = 0; i < N; i++) {
                    UpdateRecord record = (UpdateRecord) records.get(i);
                    if (isCurrentProfile(UserHandle.getUserId(record.mReceiver.mUid))) {
                        if (!record.mReceiver.callProviderEnabledLocked(provider, enabled)) {
                            if (arrayList == null) {
                                arrayList = new ArrayList();
                            }
                            arrayList.add(record.mReceiver);
                        }
                        listeners++;
                    }
                }
            }
            if (arrayList != null) {
                for (i = arrayList.size() - 1; i >= 0; i--) {
                    removeUpdatesLocked((Receiver) arrayList.get(i));
                }
            }
            if (enabled) {
                p.enable();
                if (listeners > 0) {
                    applyRequirementsLocked(provider);
                }
            } else if (isWhitelistFeatureSupport() && mWhitelistProvider.equals(provider) && isProviderRecordsContainsWhilelistPackage(provider)) {
                Log.d("LocationManagerService", "Enter white list only mode, skip provider disabling");
                this.mWhitelistWorkingMode = true;
            } else {
                p.disable();
            }
        }
    }

    private void broadcastGpsWorkSourceChange() {
        ArrayList<String> gpsPkgs = getLastWorkSource();
        if (D) {
            Log.d("LocationManagerService", "BF10 new:" + gpsPkgs);
        }
        if (!this.mGpsPkgs.equals(gpsPkgs)) {
            this.mGpsPkgs = gpsPkgs;
            Intent intent = new Intent(GPS_WORKSOURCE_CHANGE_ACTION);
            if (this.mGpsPkgs.size() > 0) {
                intent.putStringArrayListExtra(EXTRA_GPS_PKGS, this.mGpsPkgs);
            }
            this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        }
    }

    private ArrayList<String> getLastWorkSource() {
        ArrayList<UpdateRecord> records = (ArrayList) this.mRecordsByProvider.get("gps");
        ArrayList<String> gpsPkgs = new ArrayList(3);
        if (records != null) {
            for (int index = records.size() - 1; index >= 0; index--) {
                Receiver receiver = ((UpdateRecord) records.get(index)).mReceiver;
                if (1000 != receiver.mUid && isCurrentProfile(UserHandle.getUserId(receiver.mUid))) {
                    WorkSource worksource = receiver.mWorkSource;
                    if (worksource != null && worksource.size() > 0 && worksource.getName(0) != null) {
                        for (int k = 0; k < worksource.size(); k++) {
                            String pkg = worksource.getName(k);
                            if (!gpsPkgs.contains(pkg)) {
                                gpsPkgs.add(pkg);
                            }
                            if (gpsPkgs.size() > 3) {
                                break;
                            }
                        }
                    } else if (!gpsPkgs.contains(receiver.mPackageName)) {
                        gpsPkgs.add(receiver.mPackageName);
                    }
                    if (gpsPkgs.size() > 3) {
                        break;
                    }
                }
            }
        }
        return gpsPkgs;
    }

    private void applyRequirementsLocked(String provider) {
        LocationProviderInterface p = (LocationProviderInterface) this.mProvidersByName.get(provider);
        if (p != null) {
            ArrayList<UpdateRecord> records = (ArrayList) this.mRecordsByProvider.get(provider);
            WorkSource worksource = new WorkSource();
            ProviderRequest providerRequest = new ProviderRequest();
            if (records != null) {
                LocationRequest locationRequest;
                for (UpdateRecord record : records) {
                    if (isCurrentProfile(UserHandle.getUserId(record.mReceiver.mUid)) && checkLocationAccess(record.mReceiver.mPid, record.mReceiver.mUid, record.mReceiver.mPackageName, record.mReceiver.mAllowedResolutionLevel)) {
                        if (!record.isBlock()) {
                            locationRequest = record.mRequest;
                            providerRequest.locationRequests.add(locationRequest);
                            if (locationRequest.getInterval() < providerRequest.interval) {
                                providerRequest.reportLocation = true;
                                providerRequest.interval = locationRequest.getInterval();
                            }
                        } else if (D) {
                            Log.d("LocationManagerService", "block:" + record.mReceiver.mPackageName);
                        }
                    }
                }
                if (providerRequest.reportLocation) {
                    long thresholdInterval = ((providerRequest.interval + 1000) * 3) / 2;
                    for (UpdateRecord record2 : records) {
                        if (isCurrentProfile(UserHandle.getUserId(record2.mReceiver.mUid))) {
                            locationRequest = record2.mRequest;
                            if (providerRequest.locationRequests.contains(locationRequest) && locationRequest.getInterval() <= thresholdInterval) {
                                if (record2.mReceiver.mWorkSource == null || record2.mReceiver.mWorkSource.size() <= 0 || record2.mReceiver.mWorkSource.getName(0) == null) {
                                    worksource.add(record2.mReceiver.mUid, record2.mReceiver.mPackageName);
                                } else {
                                    worksource.add(record2.mReceiver.mWorkSource);
                                }
                            }
                        }
                    }
                }
            }
            if (D) {
                Log.d("LocationManagerService", "provider request: " + provider + " " + providerRequest);
            }
            if (this.isFakeLocationreporting) {
                fakeReportLocation(provider);
            } else {
                p.setRequest(providerRequest, worksource);
            }
            if ("gps".equals(provider)) {
                broadcastGpsWorkSourceChange();
            }
        }
    }

    private boolean isSystemApp(String packageName) {
        try {
            ApplicationInfo info = this.mContext.getPackageManager().getPackageInfo(packageName, 0).applicationInfo;
            if (info == null || (info.flags & 1) == 0) {
                return false;
            }
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    private Receiver getReceiverLocked(ILocationListener listener, int pid, int uid, String packageName, WorkSource workSource, boolean hideFromAppOps) {
        IBinder binder = listener.asBinder();
        Receiver receiver = (Receiver) this.mReceivers.get(binder);
        if (receiver == null) {
            receiver = new Receiver(listener, null, pid, uid, packageName, workSource, hideFromAppOps);
            try {
                receiver.getListener().asBinder().linkToDeath(receiver, 0);
                this.mReceivers.put(binder, receiver);
            } catch (RemoteException e) {
                Slog.e("LocationManagerService", "linkToDeath failed:", e);
                return null;
            }
        }
        return receiver;
    }

    private Receiver getReceiverLocked(PendingIntent intent, int pid, int uid, String packageName, WorkSource workSource, boolean hideFromAppOps) {
        Receiver receiver = (Receiver) this.mReceivers.get(intent);
        if (receiver != null) {
            return receiver;
        }
        receiver = new Receiver(null, intent, pid, uid, packageName, workSource, hideFromAppOps);
        this.mReceivers.put(intent, receiver);
        return receiver;
    }

    private LocationRequest createSanitizedRequest(LocationRequest request, int resolutionLevel) {
        LocationRequest sanitizedRequest = new LocationRequest(request);
        if (resolutionLevel < 2) {
            switch (sanitizedRequest.getQuality()) {
                case 100:
                    sanitizedRequest.setQuality(102);
                    break;
                case 203:
                    sanitizedRequest.setQuality(201);
                    break;
            }
            if (sanitizedRequest.getInterval() < LocationFudger.FASTEST_INTERVAL_MS) {
                sanitizedRequest.setInterval(LocationFudger.FASTEST_INTERVAL_MS);
            }
            if (sanitizedRequest.getFastestInterval() < LocationFudger.FASTEST_INTERVAL_MS) {
                sanitizedRequest.setFastestInterval(LocationFudger.FASTEST_INTERVAL_MS);
            }
        }
        if (sanitizedRequest.getFastestInterval() > sanitizedRequest.getInterval()) {
            request.setFastestInterval(request.getInterval());
        }
        return sanitizedRequest;
    }

    private void checkPackageName(String packageName) {
        if (packageName == null) {
            throw new SecurityException("invalid package name: " + packageName);
        }
        int uid = Binder.getCallingUid();
        String[] packages = this.mPackageManager.getPackagesForUid(uid);
        if (packages == null) {
            throw new SecurityException("invalid UID " + uid);
        }
        int i = 0;
        int length = packages.length;
        while (i < length) {
            if (!packageName.equals(packages[i])) {
                i++;
            } else {
                return;
            }
        }
        throw new SecurityException("invalid package name: " + packageName);
    }

    private void checkPendingIntent(PendingIntent intent) {
        if (intent == null) {
            throw new IllegalArgumentException("invalid pending intent: " + intent);
        }
    }

    private Receiver checkListenerOrIntentLocked(ILocationListener listener, PendingIntent intent, int pid, int uid, String packageName, WorkSource workSource, boolean hideFromAppOps) {
        if (intent == null && listener == null) {
            throw new IllegalArgumentException("need either listener or intent");
        } else if (intent != null && listener != null) {
            throw new IllegalArgumentException("cannot register both listener and intent");
        } else if (intent == null) {
            return getReceiverLocked(listener, pid, uid, packageName, workSource, hideFromAppOps);
        } else {
            checkPendingIntent(intent);
            return getReceiverLocked(intent, pid, uid, packageName, workSource, hideFromAppOps);
        }
    }

    public void requestLocationUpdates(LocationRequest request, ILocationListener listener, PendingIntent intent, String packageName) {
        if (request == null) {
            request = DEFAULT_LOCATION_REQUEST;
        }
        checkPackageName(packageName);
        if (SystemProperties.getInt(GPS_OPCUSTOM_FEATURE, 0) != 1) {
            int allowedResolutionLevel = getCallerAllowedResolutionLevel();
            checkResolutionLevelIsSufficientForProviderUse(allowedResolutionLevel, request.getProvider());
            WorkSource workSource = request.getWorkSource();
            if (workSource != null && workSource.size() > 0) {
                checkDeviceStatsAllowed();
            }
            boolean hideFromAppOps = request.getHideFromAppOps();
            if (hideFromAppOps) {
                checkUpdateAppOpsAllowed();
            }
            LocationRequest sanitizedRequest = createSanitizedRequest(request, allowedResolutionLevel);
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long identity = Binder.clearCallingIdentity();
            try {
                checkLocationAccess(pid, uid, packageName, allowedResolutionLevel);
                synchronized (this.mLock) {
                    Receiver recevier = checkListenerOrIntentLocked(listener, intent, pid, uid, packageName, workSource, hideFromAppOps);
                    if (recevier != null) {
                        requestLocationUpdatesLocked(sanitizedRequest, recevier, pid, uid, packageName);
                        if ("gps".equals(request.getProvider()) && !this.mIsRegister) {
                            try {
                                this.mAm.registerProcessObserver(this.mProcessObserver);
                                this.mIsRegister = true;
                            } catch (RemoteException e) {
                            }
                        }
                    } else {
                        Log.e("LocationManagerService", "request from " + packageName + " failed.");
                    }
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    private void requestLocationUpdatesLocked(LocationRequest request, Receiver receiver, int pid, int uid, String packageName) {
        if (request == null) {
            request = DEFAULT_LOCATION_REQUEST;
        }
        String name = request.getProvider();
        if (name == null) {
            throw new IllegalArgumentException("provider name must not be null");
        }
        Log.d("LocationManagerService", "request " + Integer.toHexString(System.identityHashCode(receiver)) + " " + name + " " + request + " from " + packageName + "(" + uid + ")");
        LocationProviderInterface provider = (LocationProviderInterface) this.mProvidersByName.get(name);
        if (provider == null) {
            throw new IllegalArgumentException("provider doesn't exist: " + name);
        }
        boolean isProviderEnabled;
        UpdateRecord oldRecord = (UpdateRecord) receiver.mUpdateRecords.put(name, new UpdateRecord(name, request, receiver));
        if (oldRecord != null) {
            oldRecord.disposeLocked(false);
        }
        if (this.mGnssWhiteListProxy == null || !this.mGnssWhiteListProxy.isNetworkLocationAlwayOn()) {
            isProviderEnabled = isAllowedByUserSettingsLocked(name, uid);
        } else {
            isProviderEnabled = isAllowedLocationRequested(name, uid, packageName);
        }
        if (fakeLocationFeatureEnable()) {
            this.isFakeLocationreporting = true;
        }
        if (isProviderEnabled) {
            applyRequirementsLocked(name);
        } else if (isWhitelistFeatureSupport() && mWhitelistPackage.equals(packageName) && mWhitelistProvider.equals(name)) {
            if (!this.mWhitelistWorkingMode) {
                Log.d("LocationManagerService", "Enable provider first when first whitlist package requested");
                provider.enable();
                this.mContext.sendBroadcastAsUser(new Intent("android.location.PROVIDERS_CHANGED"), UserHandle.ALL);
                this.mContext.sendBroadcastAsUser(new Intent("android.location.MODE_CHANGED"), UserHandle.ALL);
            }
            this.mWhitelistWorkingMode = true;
            applyRequirementsLocked(name);
        } else {
            receiver.callProviderEnabledLocked(name, false);
        }
        receiver.updateMonitoring(true);
    }

    public void removeUpdates(ILocationListener listener, PendingIntent intent, String packageName) {
        checkPackageName(packageName);
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        synchronized (this.mLock) {
            Receiver receiver = checkListenerOrIntentLocked(listener, intent, pid, uid, packageName, null, false);
            long identity = Binder.clearCallingIdentity();
            if (receiver != null) {
                try {
                    removeUpdatesLocked(receiver);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                }
            }
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void removeUpdatesLocked(Receiver receiver) {
        Log.i("LocationManagerService", "remove " + Integer.toHexString(System.identityHashCode(receiver)));
        if (fakeLocationFeatureEnable()) {
            this.isFakeLocationreporting = false;
        }
        if (this.mReceivers.remove(receiver.mKey) != null && receiver.isListener()) {
            receiver.getListener().asBinder().unlinkToDeath(receiver, 0);
            synchronized (receiver) {
                receiver.clearPendingBroadcastsLocked();
            }
        }
        receiver.updateMonitoring(false);
        HashSet<String> providers = new HashSet();
        HashMap<String, UpdateRecord> oldRecords = receiver.mUpdateRecords;
        if (oldRecords != null) {
            for (UpdateRecord record : oldRecords.values()) {
                record.disposeLocked(false);
            }
            providers.addAll(oldRecords.keySet());
        }
        for (String provider : providers) {
            if (isAllowedByCurrentUserSettingsLockedForOppo(provider) && (isAllowedByCurrentUserSettingsLocked(provider) || (isWhitelistWorkingMode(provider) && !disableProviderWhenNoWhitelistPackageRegistered(provider)))) {
                applyRequirementsLocked(provider);
            }
        }
    }

    private void applyAllProviderRequirementsLocked() {
        for (LocationProviderInterface p : this.mProviders) {
            if (isAllowedByCurrentUserSettingsLocked(p.getName())) {
                applyRequirementsLocked(p.getName());
            }
        }
    }

    public Location getLastLocation(LocationRequest request, String packageName) {
        if (D) {
            Log.d("LocationManagerService", "getLastLocation: " + request);
        }
        if (request == null) {
            request = DEFAULT_LOCATION_REQUEST;
        }
        int allowedResolutionLevel = getCallerAllowedResolutionLevel();
        checkPackageName(packageName);
        checkResolutionLevelIsSufficientForProviderUse(allowedResolutionLevel, request.getProvider());
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        long identity = Binder.clearCallingIdentity();
        try {
            if (this.mBlacklist.isBlacklisted(packageName)) {
                if (D) {
                    Log.d("LocationManagerService", "not returning last loc for blacklisted app: " + packageName);
                }
                Binder.restoreCallingIdentity(identity);
                return null;
            } else if (reportLocationAccessNoThrow(pid, uid, packageName, allowedResolutionLevel)) {
                synchronized (this.mLock) {
                    String name = request.getProvider();
                    if (name == null) {
                        name = "fused";
                    }
                    if (((LocationProviderInterface) this.mProvidersByName.get(name)) == null) {
                        Binder.restoreCallingIdentity(identity);
                        return null;
                    } else if (isAllowedByUserSettingsLocked(name, uid)) {
                        Location location;
                        if (allowedResolutionLevel < 2) {
                            location = (Location) this.mLastLocationCoarseInterval.get(name);
                        } else {
                            location = (Location) this.mLastLocation.get(name);
                        }
                        Location location2;
                        if (location == null) {
                            Binder.restoreCallingIdentity(identity);
                            return null;
                        } else if (allowedResolutionLevel < 2) {
                            Location noGPSLocation = location.getExtraLocation("noGPSLocation");
                            if (noGPSLocation != null) {
                                location2 = new Location(this.mLocationFudger.getOrCreate(noGPSLocation));
                                Binder.restoreCallingIdentity(identity);
                                return location2;
                            }
                            Binder.restoreCallingIdentity(identity);
                            return null;
                        } else {
                            location2 = new Location(location);
                            Binder.restoreCallingIdentity(identity);
                            return location2;
                        }
                    } else {
                        Binder.restoreCallingIdentity(identity);
                        return null;
                    }
                }
            } else {
                if (D) {
                    Log.d("LocationManagerService", "not returning last loc for no op app: " + packageName);
                }
                Binder.restoreCallingIdentity(identity);
                return null;
            }
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void requestGeofence(LocationRequest request, Geofence geofence, PendingIntent intent, String packageName) {
        if (request == null) {
            request = DEFAULT_LOCATION_REQUEST;
        }
        int allowedResolutionLevel = getCallerAllowedResolutionLevel();
        checkResolutionLevelIsSufficientForGeofenceUse(allowedResolutionLevel);
        checkPendingIntent(intent);
        checkPackageName(packageName);
        checkResolutionLevelIsSufficientForProviderUse(allowedResolutionLevel, request.getProvider());
        LocationRequest sanitizedRequest = createSanitizedRequest(request, allowedResolutionLevel);
        if (D) {
            Log.d("LocationManagerService", "requestGeofence: " + sanitizedRequest + " " + geofence + " " + intent);
        }
        int uid = Binder.getCallingUid();
        if (UserHandle.getUserId(uid) != 0) {
            Log.w("LocationManagerService", "proximity alerts are currently available only to the primary user");
            return;
        }
        long identity = Binder.clearCallingIdentity();
        try {
            this.mGeofenceManager.addFence(sanitizedRequest, geofence, intent, allowedResolutionLevel, uid, packageName);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void removeGeofence(Geofence geofence, PendingIntent intent, String packageName) {
        checkPendingIntent(intent);
        checkPackageName(packageName);
        if (D) {
            Log.d("LocationManagerService", "removeGeofence: " + geofence + " " + intent);
        }
        long identity = Binder.clearCallingIdentity();
        try {
            this.mGeofenceManager.removeFence(geofence, intent);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public boolean registerGnssStatusCallback(IGnssStatusListener callback, String packageName) {
        int allowedResolutionLevel = getCallerAllowedResolutionLevel();
        checkResolutionLevelIsSufficientForProviderUse(allowedResolutionLevel, "gps");
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        long ident = Binder.clearCallingIdentity();
        try {
            if (!checkLocationAccess(pid, uid, packageName, allowedResolutionLevel)) {
                return false;
            }
            Binder.restoreCallingIdentity(ident);
            if (this.mGnssStatusProvider == null) {
                return false;
            }
            try {
                this.mGnssStatusProvider.registerGnssStatusCallback(callback);
                Log.d("LocationManagerService", "registerGnssStatusCallback by package: " + packageName);
                return true;
            } catch (RemoteException e) {
                Slog.e("LocationManagerService", "mGpsStatusProvider.registerGnssStatusCallback failed", e);
                return false;
            }
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void unregisterGnssStatusCallback(IGnssStatusListener callback) {
        synchronized (this.mLock) {
            try {
                this.mGnssStatusProvider.unregisterGnssStatusCallback(callback);
                Log.d("LocationManagerService", "unregisterGnssStatusCallback");
            } catch (Exception e) {
                Slog.e("LocationManagerService", "mGpsStatusProvider.unregisterGnssStatusCallback failed", e);
            }
        }
        return;
    }

    public boolean addGnssMeasurementsListener(IGnssMeasurementsListener listener, String packageName) {
        int allowedResolutionLevel = getCallerAllowedResolutionLevel();
        checkResolutionLevelIsSufficientForProviderUse(allowedResolutionLevel, "gps");
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        long identity = Binder.clearCallingIdentity();
        try {
            boolean hasLocationAccess = checkLocationAccess(pid, uid, packageName, allowedResolutionLevel);
            if (!hasLocationAccess || this.mGnssMeasurementsProvider == null) {
                return false;
            }
            return this.mGnssMeasurementsProvider.addListener(listener);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void removeGnssMeasurementsListener(IGnssMeasurementsListener listener) {
        if (this.mGnssMeasurementsProvider != null) {
            this.mGnssMeasurementsProvider.removeListener(listener);
        }
    }

    public boolean addGnssNavigationMessageListener(IGnssNavigationMessageListener listener, String packageName) {
        int allowedResolutionLevel = getCallerAllowedResolutionLevel();
        checkResolutionLevelIsSufficientForProviderUse(allowedResolutionLevel, "gps");
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        long identity = Binder.clearCallingIdentity();
        try {
            boolean hasLocationAccess = checkLocationAccess(pid, uid, packageName, allowedResolutionLevel);
            if (!hasLocationAccess || this.mGnssNavigationMessageProvider == null) {
                return false;
            }
            return this.mGnssNavigationMessageProvider.addListener(listener);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public void removeGnssNavigationMessageListener(IGnssNavigationMessageListener listener) {
        if (this.mGnssNavigationMessageProvider != null) {
            this.mGnssNavigationMessageProvider.removeListener(listener);
        }
    }

    public boolean sendExtraCommand(String provider, String command, Bundle extras) {
        if (provider == null) {
            throw new NullPointerException();
        }
        checkResolutionLevelIsSufficientForProviderUse(getCallerAllowedResolutionLevel(), provider);
        if (this.mContext.checkCallingOrSelfPermission(ACCESS_LOCATION_EXTRA_COMMANDS) != 0) {
            throw new SecurityException("Requires ACCESS_LOCATION_EXTRA_COMMANDS permission");
        }
        synchronized (this.mLock) {
            LocationProviderInterface p = (LocationProviderInterface) this.mProvidersByName.get(provider);
            if (p == null) {
                return false;
            }
            boolean sendExtraCommand = p.sendExtraCommand(command, extras);
            return sendExtraCommand;
        }
    }

    public boolean sendNiResponse(int notifId, int userResponse) {
        if (Binder.getCallingUid() != Process.myUid()) {
            throw new SecurityException("calling sendNiResponse from outside of the system is not allowed");
        }
        try {
            return this.mNetInitiatedListener.sendNiResponse(notifId, userResponse);
        } catch (RemoteException e) {
            Slog.e("LocationManagerService", "RemoteException in LocationManagerService.sendNiResponse");
            return false;
        }
    }

    public ProviderProperties getProviderProperties(String provider) {
        if (this.mProvidersByName.get(provider) == null) {
            return null;
        }
        LocationProviderInterface p;
        checkResolutionLevelIsSufficientForProviderUse(getCallerAllowedResolutionLevel(), provider);
        synchronized (this.mLock) {
            p = (LocationProviderInterface) this.mProvidersByName.get(provider);
        }
        if (p == null) {
            return null;
        }
        return p.getProperties();
    }

    /* JADX WARNING: Missing block: B:11:0x001f, code:
            if ((r0 instanceof com.android.server.location.LocationProviderProxy) == false) goto L_0x002b;
     */
    /* JADX WARNING: Missing block: B:13:0x0027, code:
            return ((com.android.server.location.LocationProviderProxy) r0).getConnectedPackageName();
     */
    /* JADX WARNING: Missing block: B:17:0x002b, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public String getNetworkProviderPackage() {
        synchronized (this.mLock) {
            if (this.mProvidersByName.get(mWhitelistProvider) == null) {
                return null;
            }
            LocationProviderInterface p = (LocationProviderInterface) this.mProvidersByName.get(mWhitelistProvider);
        }
    }

    public boolean isProviderEnabled(String provider) {
        if ("fused".equals(provider)) {
            return false;
        }
        int uid = Binder.getCallingUid();
        long identity = Binder.clearCallingIdentity();
        try {
            synchronized (this.mLock) {
                if (((LocationProviderInterface) this.mProvidersByName.get(provider)) != null) {
                    if (isWhitelistWorkingMode(provider) && isUidALocationProvider(uid)) {
                        Binder.restoreCallingIdentity(identity);
                        return true;
                    }
                    boolean isAllowedByUserSettingsLocked = isAllowedByUserSettingsLocked(provider, uid);
                    Binder.restoreCallingIdentity(identity);
                    return isAllowedByUserSettingsLocked;
                }
            }
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
        return false;
    }

    private boolean isUidALocationProvider(int uid) {
        if (uid == 1000 || doesUidHavePackage(uid, "com.google.android.gms")) {
            return true;
        }
        if (this.mGeocodeProvider != null && doesUidHavePackage(uid, this.mGeocodeProvider.getConnectedPackageName())) {
            return true;
        }
        for (LocationProviderProxy proxy : this.mProxyProviders) {
            if (doesUidHavePackage(uid, proxy.getConnectedPackageName())) {
                return true;
            }
        }
        return false;
    }

    private void checkCallerIsProvider() {
        if (this.mContext.checkCallingOrSelfPermission(INSTALL_LOCATION_PROVIDER) != 0 && !isUidALocationProvider(Binder.getCallingUid())) {
            throw new SecurityException("need INSTALL_LOCATION_PROVIDER permission, or UID of a currently bound location provider");
        }
    }

    private boolean doesUidHavePackage(int uid, String packageName) {
        if (packageName == null) {
            return false;
        }
        String[] packageNames = this.mPackageManager.getPackagesForUid(uid);
        if (packageNames == null) {
            return false;
        }
        for (String name : packageNames) {
            if (packageName.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void reportLocation(Location location, boolean passive) {
        int i = 1;
        checkCallerIsProvider();
        if (location.isComplete()) {
            this.mLocationHandler.removeMessages(1, location);
            Message m = Message.obtain(this.mLocationHandler, 1, location);
            if (!passive) {
                i = 0;
            }
            m.arg1 = i;
            this.mLocationHandler.sendMessageAtFrontOfQueue(m);
            return;
        }
        Log.w("LocationManagerService", "Dropping incomplete location: " + location);
    }

    private void fakeReportLocation(String provider) {
        String location_prop = SystemProperties.get(OPPO_FAKE_LOCATION_TEST);
        if (location_prop != null) {
            String[] split = location_prop.split(OPPO_FAKE_LOCATION_SPLIT);
            if (split.length <= 0 || split.length != 3) {
                Log.e("LocationManagerService", "fakeReportLocation parameter is abnormal ");
                return;
            }
            Location fakeLoation = new Location(provider);
            try {
                fakeLoation.setLatitude(Double.parseDouble(split[1]));
                fakeLoation.setLongitude(Double.parseDouble(split[0]));
            } catch (NumberFormatException e) {
            }
            this.mLocationHandler.removeMessages(1, fakeLoation);
            Message m = Message.obtain(this.mLocationHandler, 1, fakeLoation);
            m.arg1 = 0;
            Log.d("LocationManagerService", "fakeReportLocation Longitude = " + split[0] + " Altitude = " + split[1] + " inchina = " + split[2]);
            this.mLocationHandler.sendMessageDelayed(m, 500);
        }
    }

    private boolean fakeLocationFeatureEnable() {
        String location_prop = SystemProperties.get(OPPO_FAKE_LOCATOIN_TEST_SWITCH);
        boolean result = false;
        if (location_prop != null && location_prop.equals(OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
            result = true;
        }
        Log.e("LocationManagerService", "fakeLocationFeatureEnable result =  " + result);
        return result;
    }

    private static boolean shouldBroadcastSafe(Location loc, Location lastLoc, UpdateRecord record, long now) {
        if (lastLoc == null) {
            return true;
        }
        if ((loc.getElapsedRealtimeNanos() - lastLoc.getElapsedRealtimeNanos()) / NANOS_PER_MILLI < record.mRequest.getFastestInterval() - 100) {
            return false;
        }
        double minDistance = (double) record.mRequest.getSmallestDisplacement();
        if (minDistance > 0.0d && ((double) loc.distanceTo(lastLoc)) <= minDistance) {
            return false;
        }
        if (record.mRequest.getNumUpdates() <= 0) {
            return false;
        }
        if (record.mRequest.getExpireAt() < now) {
            return false;
        }
        return true;
    }

    private void changeGpsLastLocation(String provider, Location location) {
        Location lastGpsLocation = (Location) this.mLastLocation.get("gps");
        if (lastGpsLocation == null) {
            if (D) {
                Log.d("LocationManagerService", "Adding the first gps last location!!!");
            }
            lastGpsLocation = new Location("gps");
            this.mLastLocation.put("gps", lastGpsLocation);
            lastGpsLocation.set(location);
        } else if (!provider.equals(lastGpsLocation.getProvider())) {
            if (provider.equals("gps") || (provider.equals(mWhitelistProvider) && !lastGpsLocation.getProvider().equals("gps"))) {
                if (D) {
                    Log.d("LocationManagerService", "changing the gps last location!!!!");
                }
                lastGpsLocation.set(location);
            }
        }
    }

    private void handleLocationChangedLocked(Location location, boolean passive) {
        if (D) {
            Log.d("LocationManagerService", "incoming location: " + location);
        } else {
            Log.d("LocationManagerService", "incoming location from: " + location.getProvider());
        }
        long now = SystemClock.elapsedRealtime();
        String provider = passive ? "passive" : location.getProvider();
        LocationProviderInterface p = (LocationProviderInterface) this.mProvidersByName.get(provider);
        if (p != null) {
            Location noGPSLocation = location.getExtraLocation("noGPSLocation");
            Location lastLocation = (Location) this.mLastLocation.get(provider);
            if (lastLocation == null) {
                lastLocation = new Location(provider);
                this.mLastLocation.put(provider, lastLocation);
            } else {
                Location lastNoGPSLocation = lastLocation.getExtraLocation("noGPSLocation");
                if (noGPSLocation == null && lastNoGPSLocation != null) {
                    location.setExtraLocation("noGPSLocation", lastNoGPSLocation);
                }
            }
            lastLocation.set(location);
            changeGpsLastLocation(provider, location);
            Location lastLocationCoarseInterval = (Location) this.mLastLocationCoarseInterval.get(provider);
            if (lastLocationCoarseInterval == null) {
                lastLocationCoarseInterval = new Location(location);
                this.mLastLocationCoarseInterval.put(provider, lastLocationCoarseInterval);
            }
            if (location.getElapsedRealtimeNanos() - lastLocationCoarseInterval.getElapsedRealtimeNanos() > 600000000000L) {
                lastLocationCoarseInterval.set(location);
            }
            noGPSLocation = lastLocationCoarseInterval.getExtraLocation("noGPSLocation");
            ArrayList<UpdateRecord> records = (ArrayList) this.mRecordsByProvider.get(provider);
            if (records != null && records.size() != 0) {
                Receiver receiver;
                Location coarseLocation = null;
                if (noGPSLocation != null) {
                    coarseLocation = this.mLocationFudger.getOrCreate(noGPSLocation);
                } else if (CtaUtils.isCtaSupported()) {
                    coarseLocation = this.mLocationFudger.getOrCreate(lastLocationCoarseInterval);
                }
                long newStatusUpdateTime = p.getStatusUpdateTime();
                Bundle extras = new Bundle();
                int status = p.getStatus(extras);
                Iterable deadReceivers = null;
                Iterable deadUpdateRecords = null;
                for (UpdateRecord r : records) {
                    receiver = r.mReceiver;
                    boolean receiverDead = false;
                    int receiverUserId = UserHandle.getUserId(receiver.mUid);
                    if (!isCurrentProfile(receiverUserId)) {
                        if (!isUidALocationProvider(receiver.mUid)) {
                            if (D) {
                                Log.d("LocationManagerService", "skipping loc update for background user " + receiverUserId + " (current user: " + this.mCurrentUserId + ", app: " + receiver.mPackageName + ")");
                            }
                        }
                    }
                    if (!this.mBlacklist.isBlacklisted(receiver.mPackageName)) {
                        if (this.mGnssWhiteListProxy != null && this.mGnssWhiteListProxy.isNetworkLocationAlwayOn()) {
                            if (!isAllowedLocationChanged(provider, receiver.mPackageName)) {
                            }
                        }
                        if (reportLocationAccessNoThrow(receiver.mPid, receiver.mUid, receiver.mPackageName, receiver.mAllowedResolutionLevel)) {
                            if (!isWhitelistWorkingMode(provider) || mWhitelistPackage.equals(receiver.mPackageName)) {
                                Location notifyLocation;
                                if (receiver.mAllowedResolutionLevel < 2) {
                                    notifyLocation = coarseLocation;
                                } else {
                                    notifyLocation = lastLocation;
                                }
                                if (notifyLocation != null) {
                                    Location lastLoc = r.mLastFixBroadcast;
                                    if (lastLoc == null || shouldBroadcastSafe(notifyLocation, lastLoc, r, now)) {
                                        if (lastLoc == null) {
                                            r.mLastFixBroadcast = new Location(notifyLocation);
                                        } else {
                                            lastLoc.set(notifyLocation);
                                        }
                                        if (!receiver.callLocationChangedLocked(notifyLocation)) {
                                            Slog.w("LocationManagerService", "RemoteException calling onLocationChanged on " + receiver);
                                            receiverDead = true;
                                        }
                                        r.mRequest.decrementNumUpdates();
                                    }
                                }
                                long prevStatusUpdateTime = r.mLastStatusBroadcast;
                                if (newStatusUpdateTime > prevStatusUpdateTime && !(prevStatusUpdateTime == 0 && status == 2)) {
                                    r.mLastStatusBroadcast = newStatusUpdateTime;
                                    if (!receiver.callStatusChangedLocked(provider, status, extras)) {
                                        receiverDead = true;
                                        Slog.w("LocationManagerService", "RemoteException calling onStatusChanged on " + receiver);
                                    }
                                }
                                if (r.mRequest.getNumUpdates() <= 0 || r.mRequest.getExpireAt() < now) {
                                    if (deadUpdateRecords == null) {
                                        deadUpdateRecords = new ArrayList();
                                    }
                                    deadUpdateRecords.add(r);
                                }
                                if (receiverDead) {
                                    if (deadReceivers == null) {
                                        deadReceivers = new ArrayList();
                                    }
                                    if (!deadReceivers.contains(receiver)) {
                                        deadReceivers.add(receiver);
                                    }
                                }
                            }
                        } else if (D) {
                            Log.d("LocationManagerService", "skipping loc update for no op app: " + receiver.mPackageName);
                        }
                    } else if (D) {
                        Log.d("LocationManagerService", "skipping loc update for blacklisted app: " + receiver.mPackageName);
                    }
                }
                if (deadReceivers != null) {
                    for (Receiver receiver2 : deadReceivers) {
                        removeUpdatesLocked(receiver2);
                    }
                }
                if (deadUpdateRecords != null) {
                    for (UpdateRecord r2 : deadUpdateRecords) {
                        r2.disposeLocked(true);
                    }
                    applyRequirementsLocked(provider);
                }
                if (this.isFakeLocationreporting) {
                    fakeReportLocation(location.getProvider());
                }
            }
        }
    }

    private boolean isMockProvider(String provider) {
        boolean containsKey;
        synchronized (this.mLock) {
            containsKey = this.mMockProviders.containsKey(provider);
        }
        return containsKey;
    }

    private void handleLocationChanged(Location location, boolean passive) {
        Location myLocation = new Location(location);
        String provider = myLocation.getProvider();
        if (!myLocation.isFromMockProvider() && isMockProvider(provider)) {
            myLocation.setIsFromMockProvider(true);
        }
        synchronized (this.mLock) {
            if (isAllowedByCurrentUserSettingsLocked(provider)) {
                if (!passive) {
                    this.mPassiveProvider.updateLocation(myLocation);
                }
                handleLocationChangedLocked(myLocation, passive);
            } else if (isWhitelistWorkingMode(provider)) {
                handleLocationChangedLocked(myLocation, passive);
            }
        }
    }

    public boolean geocoderIsPresent() {
        return this.mGeocodeProvider != null ? this.mGeocodeProvider.isServiceBinded() : false;
    }

    public String getFromLocation(double latitude, double longitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        boolean isDisableGeocoder = SystemProperties.get("persist.sys.mtk.disable.moms", "0").equals(OPPO_FAKE_LOCATOIN_SWITCH_ON);
        if (this.mGeocodeProvider == null || isDisableGeocoder) {
            return null;
        }
        return this.mGeocodeProvider.getFromLocation(latitude, longitude, maxResults, params, addrs);
    }

    public String getFromLocationName(String locationName, double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude, double upperRightLongitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        boolean isDisableGeocoder = SystemProperties.get("persist.sys.mtk.disable.moms", "0").equals(OPPO_FAKE_LOCATOIN_SWITCH_ON);
        if (this.mGeocodeProvider == null || isDisableGeocoder) {
            return null;
        }
        return this.mGeocodeProvider.getFromLocationName(locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, maxResults, params, addrs);
    }

    private boolean canCallerAccessMockLocation(String opPackageName) {
        return this.mAppOps.noteOp(58, Binder.getCallingUid(), opPackageName) == 0;
    }

    public void addTestProvider(String name, ProviderProperties properties, String opPackageName) {
        if (!canCallerAccessMockLocation(opPackageName)) {
            return;
        }
        if ("passive".equals(name)) {
            throw new IllegalArgumentException("Cannot mock the passive location provider");
        }
        long identity = Binder.clearCallingIdentity();
        synchronized (this.mLock) {
            if ("gps".equals(name) || mWhitelistProvider.equals(name) || "fused".equals(name)) {
                LocationProviderInterface p = (LocationProviderInterface) this.mProvidersByName.get(name);
                if (p != null) {
                    removeProviderLocked(p);
                }
            }
            addTestProviderLocked(name, properties);
            updateProvidersLocked();
        }
        Binder.restoreCallingIdentity(identity);
    }

    private void addTestProviderLocked(String name, ProviderProperties properties) {
        if (this.mProvidersByName.get(name) != null) {
            throw new IllegalArgumentException("Provider \"" + name + "\" already exists");
        }
        MockProvider provider = new MockProvider(name, this, properties);
        addProviderLocked(provider);
        this.mMockProviders.put(name, provider);
        this.mLastLocation.put(name, null);
        this.mLastLocationCoarseInterval.put(name, null);
    }

    public void removeTestProvider(String provider, String opPackageName) {
        if (canCallerAccessMockLocation(opPackageName)) {
            synchronized (this.mLock) {
                clearTestProviderEnabled(provider, opPackageName);
                clearTestProviderLocation(provider, opPackageName);
                clearTestProviderStatus(provider, opPackageName);
                if (((MockProvider) this.mMockProviders.remove(provider)) == null) {
                    throw new IllegalArgumentException("Provider \"" + provider + "\" unknown");
                }
                long identity = Binder.clearCallingIdentity();
                removeProviderLocked((LocationProviderInterface) this.mProvidersByName.get(provider));
                LocationProviderInterface realProvider = (LocationProviderInterface) this.mRealProviders.get(provider);
                if (realProvider != null) {
                    addProviderLocked(realProvider);
                }
                this.mLastLocation.put(provider, null);
                this.mLastLocationCoarseInterval.put(provider, null);
                updateProvidersLocked();
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public void setTestProviderLocation(String provider, Location loc, String opPackageName) {
        if (canCallerAccessMockLocation(opPackageName)) {
            synchronized (this.mLock) {
                MockProvider mockProvider = (MockProvider) this.mMockProviders.get(provider);
                if (mockProvider == null) {
                    throw new IllegalArgumentException("Provider \"" + provider + "\" unknown");
                }
                Location mock = new Location(loc);
                mock.setIsFromMockProvider(true);
                if (!(TextUtils.isEmpty(loc.getProvider()) || provider.equals(loc.getProvider()))) {
                    Object[] objArr = new Object[3];
                    objArr[0] = "33091107";
                    objArr[1] = Integer.valueOf(Binder.getCallingUid());
                    objArr[2] = provider + "!=" + loc.getProvider();
                    EventLog.writeEvent(1397638484, objArr);
                }
                long identity = Binder.clearCallingIdentity();
                mockProvider.setLocation(mock);
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public void clearTestProviderLocation(String provider, String opPackageName) {
        if (canCallerAccessMockLocation(opPackageName)) {
            synchronized (this.mLock) {
                MockProvider mockProvider = (MockProvider) this.mMockProviders.get(provider);
                if (mockProvider == null) {
                    throw new IllegalArgumentException("Provider \"" + provider + "\" unknown");
                }
                mockProvider.clearLocation();
            }
        }
    }

    public void setTestProviderEnabled(String provider, boolean enabled, String opPackageName) {
        if (canCallerAccessMockLocation(opPackageName)) {
            synchronized (this.mLock) {
                MockProvider mockProvider = (MockProvider) this.mMockProviders.get(provider);
                if (mockProvider == null) {
                    throw new IllegalArgumentException("Provider \"" + provider + "\" unknown");
                }
                long identity = Binder.clearCallingIdentity();
                if (enabled) {
                    mockProvider.enable();
                    this.mEnabledProviders.add(provider);
                    this.mDisabledProviders.remove(provider);
                } else {
                    mockProvider.disable();
                    this.mEnabledProviders.remove(provider);
                    this.mDisabledProviders.add(provider);
                }
                updateProvidersLocked();
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public void clearTestProviderEnabled(String provider, String opPackageName) {
        if (canCallerAccessMockLocation(opPackageName)) {
            synchronized (this.mLock) {
                if (((MockProvider) this.mMockProviders.get(provider)) == null) {
                    throw new IllegalArgumentException("Provider \"" + provider + "\" unknown");
                }
                long identity = Binder.clearCallingIdentity();
                this.mEnabledProviders.remove(provider);
                this.mDisabledProviders.remove(provider);
                updateProvidersLocked();
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    public void setTestProviderStatus(String provider, int status, Bundle extras, long updateTime, String opPackageName) {
        if (canCallerAccessMockLocation(opPackageName)) {
            synchronized (this.mLock) {
                MockProvider mockProvider = (MockProvider) this.mMockProviders.get(provider);
                if (mockProvider == null) {
                    throw new IllegalArgumentException("Provider \"" + provider + "\" unknown");
                }
                mockProvider.setStatus(status, extras, updateTime);
            }
        }
    }

    public void clearTestProviderStatus(String provider, String opPackageName) {
        if (canCallerAccessMockLocation(opPackageName)) {
            synchronized (this.mLock) {
                MockProvider mockProvider = (MockProvider) this.mMockProviders.get(provider);
                if (mockProvider == null) {
                    throw new IllegalArgumentException("Provider \"" + provider + "\" unknown");
                }
                mockProvider.clearStatus();
            }
        }
    }

    private void log(String log) {
        if (Log.isLoggable("LocationManagerService", 2)) {
            Slog.d("LocationManagerService", log);
        }
    }

    /* JADX WARNING: Missing block: B:74:0x0490, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump LocationManagerService from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            return;
        }
        synchronized (this.mLock) {
            Location location;
            pw.println("Current Location Manager state:");
            pw.println("  Location Listeners:");
            for (Receiver receiver : this.mReceivers.values()) {
                pw.println("    " + receiver);
            }
            pw.println("  Active Records by Provider:");
            for (Entry<String, ArrayList<UpdateRecord>> entry : this.mRecordsByProvider.entrySet()) {
                pw.println("    " + ((String) entry.getKey()) + ":");
                for (UpdateRecord record : (ArrayList) entry.getValue()) {
                    pw.println("      " + record);
                }
            }
            pw.println("  Historical Records by Provider:");
            for (Entry<PackageProviderKey, PackageStatistics> entry2 : this.mRequestStatistics.statistics.entrySet()) {
                PackageProviderKey key = (PackageProviderKey) entry2.getKey();
                pw.println("    " + key.packageName + ": " + key.providerName + ": " + ((PackageStatistics) entry2.getValue()));
            }
            pw.println("  Last Known Locations:");
            for (Entry<String, Location> entry3 : this.mLastLocation.entrySet()) {
                location = (Location) entry3.getValue();
                pw.println("    " + ((String) entry3.getKey()) + ": " + location);
            }
            pw.println("  Last Known Locations Coarse Intervals:");
            for (Entry<String, Location> entry32 : this.mLastLocationCoarseInterval.entrySet()) {
                location = (Location) entry32.getValue();
                pw.println("    " + ((String) entry32.getKey()) + ": " + location);
            }
            this.mGeofenceManager.dump(pw);
            if (this.mEnabledProviders.size() > 0) {
                pw.println("  Enabled Providers:");
                for (String i : this.mEnabledProviders) {
                    pw.println("    " + i);
                }
            }
            if (this.mDisabledProviders.size() > 0) {
                pw.println("  Disabled Providers:");
                for (String i2 : this.mDisabledProviders) {
                    pw.println("    " + i2);
                }
            }
            pw.append("  ");
            this.mBlacklist.dump(pw);
            if (this.mMockProviders.size() > 0) {
                pw.println("  Mock Providers:");
                for (Entry<String, MockProvider> i3 : this.mMockProviders.entrySet()) {
                    ((MockProvider) i3.getValue()).dump(pw, "      ");
                }
            }
            pw.append("  fudger: ");
            this.mLocationFudger.dump(fd, pw, args);
            if (args.length <= 0 || !"short".equals(args[0])) {
                for (LocationProviderInterface provider : this.mProviders) {
                    pw.print(provider.getName() + " Internal State");
                    if (provider instanceof LocationProviderProxy) {
                        LocationProviderProxy proxy = (LocationProviderProxy) provider;
                        pw.print(" (" + proxy.getConnectedPackageName() + ")");
                    }
                    if (provider instanceof OppoNlpProxy) {
                        OppoNlpProxy proxy2 = (OppoNlpProxy) provider;
                        pw.print(" (" + proxy2.getConnectedPackageName() + ")");
                    }
                    pw.println(":");
                    provider.dump(fd, pw, args);
                }
                if (D) {
                    this.mOppoBlackList.dump(pw);
                }
            }
        }
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "ZhiYong.Lin@Plf.Framework, add for BPM", property = OppoRomType.ROM)
    public int[] getLocationListenersUid() {
        synchronized (this.mLock) {
            ArrayList<Integer> tmp = new ArrayList();
            for (Receiver i : this.mReceivers.values()) {
                for (Entry<String, UpdateRecord> j : i.mUpdateRecords.entrySet()) {
                    tmp.add(Integer.valueOf(((UpdateRecord) j.getValue()).mReceiver.mUid));
                }
            }
            int size = tmp.size();
            if (size <= 0) {
                return null;
            }
            int[] res = new int[size];
            for (int i2 = 0; i2 < size; i2++) {
                res[i2] = ((Integer) tmp.get(i2)).intValue();
            }
            return res;
        }
    }

    private void reBindNetworkProviderLock(boolean bindGmsPackage) {
        Log.d("LocationManagerService", "reBindNetworkProviderLock bindGmsPackage: " + bindGmsPackage);
        LocationProviderProxy previousNlp = (LocationProviderProxy) this.mRealProviders.get(mWhitelistProvider);
        if (previousNlp != null) {
            int preferPackageNamesId;
            Log.d("LocationManagerService", "binded NLP package name: " + previousNlp.getConnectedPackageName());
            if (bindGmsPackage) {
                preferPackageNamesId = 0;
            } else {
                preferPackageNamesId = 134479876;
            }
            LocationProviderProxy networkProvider = LocationProviderProxy.createAndBind(this.mContext, mWhitelistProvider, NETWORK_LOCATION_SERVICE_ACTION, 17956943, 17039424, 17236016, 134479876, preferPackageNamesId, this.mLocationHandler);
            if (networkProvider != null) {
                Log.d("LocationManagerService", "Successfully bind package:" + networkProvider.getConnectedPackageName());
                if (previousNlp != null) {
                    previousNlp.unbind();
                    this.mRealProviders.remove(mWhitelistProvider);
                    this.mProxyProviders.remove(previousNlp);
                    removeProviderLocked(previousNlp);
                }
                this.mRealProviders.put(mWhitelistProvider, networkProvider);
                this.mProxyProviders.add(networkProvider);
                addProviderLocked(networkProvider);
                updateProvidersLocked();
                if (this.mGeocodeProvider != null) {
                    this.mGeocodeProvider.unbind();
                }
                this.mGeocodeProvider = GeocoderProxy.createAndBind(this.mContext, 17956946, 17039427, 17236016, 134479876, preferPackageNamesId, this.mLocationHandler);
            } else {
                Log.d("LocationManagerService", "Failed to bind specified package service");
            }
            return;
        }
        Log.d("LocationManagerService", "currently there is no NLP provided.");
    }

    private boolean isWhitelistFeatureSupport() {
        String optr = SystemProperties.get("persist.operator.optr");
        if (optr == null || !optr.equals("OP08")) {
            return false;
        }
        return true;
    }

    private boolean isWhitelistWorkingMode(String provider) {
        if (this.mWhitelistWorkingMode && mWhitelistProvider.equals(provider)) {
            return true;
        }
        return false;
    }

    private boolean isProviderRecordsContainsWhilelistPackage(String provider) {
        ArrayList<UpdateRecord> records = (ArrayList) this.mRecordsByProvider.get(provider);
        boolean contained = false;
        if (records != null) {
            for (UpdateRecord record : records) {
                if (mWhitelistPackage.equals(record.mReceiver.mPackageName)) {
                    contained = true;
                    break;
                }
            }
        }
        Log.d("LocationManagerService", "isProviderRecordsContainsWhilelistPackage: " + contained);
        return contained;
    }

    private boolean disableProviderWhenNoWhitelistPackageRegistered(String provider) {
        if (isProviderRecordsContainsWhilelistPackage(provider)) {
            return false;
        }
        this.mWhitelistWorkingMode = false;
        LocationProviderInterface p = (LocationProviderInterface) this.mProvidersByName.get(provider);
        if (p == null) {
            throw new IllegalArgumentException("provider doesn't exist: " + provider);
        }
        Log.d("LocationManagerService", "disable provider when no whitelist package registered");
        p.disable();
        this.mContext.sendBroadcastAsUser(new Intent("android.location.PROVIDERS_CHANGED"), UserHandle.ALL);
        this.mContext.sendBroadcastAsUser(new Intent("android.location.MODE_CHANGED"), UserHandle.ALL);
        return true;
    }
}
