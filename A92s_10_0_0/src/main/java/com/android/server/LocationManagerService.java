package com.android.server;

import android.annotation.OppoHook;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.common.OppoFeatureCache;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.location.ActivityRecognitionHardware;
import android.location.Address;
import android.location.Criteria;
import android.location.GeocoderParams;
import android.location.Geofence;
import android.location.GnssMeasurementCorrections;
import android.location.IBatchedLocationCallback;
import android.location.IGnssMeasurementsListener;
import android.location.IGnssNavigationMessageListener;
import android.location.IGnssStatusListener;
import android.location.IGpsGeofenceHardware;
import android.location.ILocationListener;
import android.location.ILocationManager;
import android.location.INetInitiatedListener;
import android.location.LocAppsOp;
import android.location.Location;
import android.location.LocationRequest;
import android.location.LocationTime;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManagerInternal;
import android.os.PowerSaveState;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.WorkSource;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;
import android.util.TimeUtils;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.PackageMonitor;
import com.android.internal.location.ProviderProperties;
import com.android.internal.location.ProviderRequest;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.Preconditions;
import com.android.server.LocationManagerService;
import com.android.server.am.IColorAppStartupManager;
import com.android.server.am.IColorMultiAppManager;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.location.AbstractLocationProvider;
import com.android.server.location.ActivityRecognitionProxy;
import com.android.server.location.CallerIdentity;
import com.android.server.location.GeocoderProxy;
import com.android.server.location.GeofenceManager;
import com.android.server.location.GeofenceProxy;
import com.android.server.location.GnssBatchingProvider;
import com.android.server.location.GnssCapabilitiesProvider;
import com.android.server.location.GnssLocationProvider;
import com.android.server.location.GnssMeasurementCorrectionsProvider;
import com.android.server.location.GnssMeasurementsProvider;
import com.android.server.location.GnssNavigationMessageProvider;
import com.android.server.location.GnssStatusListenerHelper;
import com.android.server.location.LocationBlacklist;
import com.android.server.location.LocationFudger;
import com.android.server.location.LocationProviderProxy;
import com.android.server.location.LocationRequestStatistics;
import com.android.server.location.MockProvider;
import com.android.server.location.OppoBaseLocationProvider;
import com.android.server.location.PassiveProvider;
import com.android.server.location.RemoteListenerHelper;
import com.android.server.location.interfaces.IPswCoarseToFine;
import com.android.server.location.interfaces.IPswFastNetworkLocation;
import com.android.server.location.interfaces.IPswGnssDuration;
import com.android.server.location.interfaces.IPswLbsCustomize;
import com.android.server.location.interfaces.IPswLbsRepairer;
import com.android.server.location.interfaces.IPswLocationBlacklistUtil;
import com.android.server.location.interfaces.IPswLocationStatistics;
import com.android.server.location.interfaces.IPswNavigationStatusController;
import com.android.server.location.interfaces.IPswNlpProxy;
import com.android.server.location.interfaces.IPswOppoGnssWhiteListProxy;
import com.android.server.pm.PackageManagerService;
import com.color.antivirus.IColorAntiVirusBehaviorManager;
import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;

public class LocationManagerService extends ILocationManager.Stub {
    private static final String ACCESS_LOCATION_EXTRA_COMMANDS = "android.permission.ACCESS_LOCATION_EXTRA_COMMANDS";
    private static final String ACTION_MTKLOGGER_STATE_CHANGED = "com.debug.loggerui.intent.action.LOG_STATE_CHANGED";
    public static boolean D = false;
    private static final long DEFAULT_BACKGROUND_THROTTLE_INTERVAL_MS = 1800000;
    private static final long DEFAULT_LAST_LOCATION_MAX_AGE_MS = 1200000;
    private static final LocationRequest DEFAULT_LOCATION_REQUEST = new LocationRequest();
    public static final boolean FORCE_DEBUG = (SystemProperties.getInt(PROP_FORCE_DEBUG_KEY, 0) == 1);
    private static final int FOREGROUND_IMPORTANCE_CUTOFF = 125;
    private static final String FUSED_LOCATION_SERVICE_ACTION = "com.android.location.service.FusedLocationProvider";
    public static final String GPS_DEBUG_SWITCH = "gps_debug_switch";
    private static final long HIGH_POWER_INTERVAL_MS = 300000;
    private static final boolean IS_USER_BUILD = ("user".equals(Build.TYPE) || "userdebug".equals(Build.TYPE));
    private static final int MAX_PROVIDER_SCHEDULING_JITTER_MS = 100;
    private static final int MSG_PACKAGE_OP_HAVE_CHANGED = 101;
    private static final String MTKLOGGER_MOBILELOG_DEBUG_PROPERTY = "vendor.MB.running";
    private static final long NANOS_PER_MILLI = 1000000;
    private static final String NETWORK_LOCATION_SERVICE_ACTION = "com.android.location.service.v3.NetworkLocationProvider";
    private static final String PROP_FORCE_DEBUG_KEY = "persist.vendor.log.tel_dbg";
    private static final int RESOLUTION_LEVEL_COARSE = 1;
    private static final int RESOLUTION_LEVEL_FINE = 2;
    private static final int RESOLUTION_LEVEL_NONE = 0;
    private static final int SYSTEM_LOCATION_PKG_UID = 1000;
    private static final String TAG = "LocationManagerService";
    private static final String WAKELOCK_KEY = "*location*";
    /* access modifiers changed from: private */
    public ActivityManager mActivityManager;
    /* access modifiers changed from: private */
    public AppOpsManager mAppOps;
    private final ArraySet<String> mBackgroundThrottlePackageWhitelist = new ArraySet<>();
    @GuardedBy({"mLock"})
    private int mBatterySaverMode;
    private LocationBlacklist mBlacklist;
    /* access modifiers changed from: private */
    public final Context mContext;
    private boolean mCtaSupported = false;
    /* access modifiers changed from: private */
    public int mCurrentUserId = 0;
    private int[] mCurrentUserProfiles = {0};
    @GuardedBy({"mLock"})
    private String mExtraLocationControllerPackage;
    private boolean mExtraLocationControllerPackageEnabled;
    /* access modifiers changed from: private */
    public IPswFastNetworkLocation mFastNetworkLocation = null;
    private GeocoderProxy mGeocodeProvider;
    private GeofenceManager mGeofenceManager;
    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public IBatchedLocationCallback mGnssBatchingCallback;
    @GuardedBy({"mLock"})
    private LinkedListener<IBatchedLocationCallback> mGnssBatchingDeathCallback;
    @GuardedBy({"mLock"})
    private boolean mGnssBatchingInProgress = false;
    private GnssBatchingProvider mGnssBatchingProvider;
    private GnssCapabilitiesProvider mGnssCapabilitiesProvider;
    private GnssMeasurementCorrectionsProvider mGnssMeasurementCorrectionsProvider;
    @GuardedBy({"mLock"})
    private final ArrayMap<IBinder, LinkedListener<IGnssMeasurementsListener>> mGnssMeasurementsListeners = new ArrayMap<>();
    private GnssMeasurementsProvider mGnssMeasurementsProvider;
    private GnssLocationProvider.GnssMetricsProvider mGnssMetricsProvider;
    @GuardedBy({"mLock"})
    private final ArrayMap<IBinder, LinkedListener<IGnssNavigationMessageListener>> mGnssNavigationMessageListeners = new ArrayMap<>();
    private GnssNavigationMessageProvider mGnssNavigationMessageProvider;
    private GnssLocationProvider mGnssProvider;
    @GuardedBy({"mLock"})
    private final ArrayMap<IBinder, LinkedListener<IGnssStatusListener>> mGnssStatusListeners = new ArrayMap<>();
    private GnssStatusListenerHelper mGnssStatusProvider;
    private GnssLocationProvider.GnssSystemInfoProvider mGnssSystemInfoProvider;
    /* access modifiers changed from: private */
    public IPswOppoGnssWhiteListProxy mGnssWhiteListProxy;
    private IGpsGeofenceHardware mGpsGeofenceProxy;
    /* access modifiers changed from: private */
    public final Handler mHandler;
    private final ArraySet<String> mIgnoreSettingsPackageWhitelist = new ArraySet<>();
    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public final HashMap<String, Location> mLastLocation = new HashMap<>();
    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public final HashMap<String, Location> mLastLocationCoarseInterval = new HashMap<>();
    private LocationFudger mLocationFudger;
    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public final LocationUsageLogger mLocationUsageLogger;
    /* access modifiers changed from: private */
    public final LocationWorkHandler mLocationWorkHandler;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private Object mMtkLocationManagerService = null;
    private Class<?> mMtkLocationManagerServiceClass = null;
    private IPswNavigationStatusController mNavigationStatusController = null;
    private INetInitiatedListener mNetInitiatedListener;
    /* access modifiers changed from: private */
    public IPswNlpProxy mNlpProxy = null;
    /* access modifiers changed from: private */
    public IPswGnssDuration mOppoGnssDuration;
    /* access modifiers changed from: private */
    public IPswLbsRepairer mOppoLbsRepairer = null;
    /* access modifiers changed from: private */
    public IPswLocationBlacklistUtil mOppoLocationBlacklistUtil = null;
    /* access modifiers changed from: private */
    public IPswLocationStatistics mOppoLocationStatistics = null;
    private PackageManager mPackageManager;
    /* access modifiers changed from: private */
    public PassiveProvider mPassiveProvider;
    /* access modifiers changed from: private */
    public PowerManager mPowerManager;
    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public final ArrayList<LocationProvider> mProviders = new ArrayList<>();
    @GuardedBy({"mLock"})
    private final ArrayList<LocationProvider> mRealProviders = new ArrayList<>();
    @GuardedBy({"mLock"})
    private final HashMap<Object, Receiver> mReceivers = new HashMap<>();
    /* access modifiers changed from: private */
    public final HashMap<String, ArrayList<UpdateRecord>> mRecordsByProvider = new HashMap<>();
    /* access modifiers changed from: private */
    public final LocationRequestStatistics mRequestStatistics = new LocationRequestStatistics();
    private UserManager mUserManager;

    static {
        boolean z = true;
        if (IS_USER_BUILD && !Log.isLoggable(TAG, 3) && !FORCE_DEBUG) {
            z = false;
        }
        D = z;
    }

    public LocationManagerService(Context context) {
        this.mContext = context;
        this.mHandler = FgThread.getHandler();
        this.mLocationWorkHandler = new LocationWorkHandler(this.mHandler.getLooper());
        this.mLocationUsageLogger = new LocationUsageLogger();
        PackageManagerInternal packageManagerInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        packageManagerInternal.setLocationPackagesProvider(new PackageManagerInternal.PackagesProvider() {
            /* class com.android.server.$$Lambda$LocationManagerService$bojY6dMaI07zh6_sF7ERxgmk6U0 */

            public final String[] getPackages(int i) {
                return LocationManagerService.this.lambda$new$0$LocationManagerService(i);
            }
        });
        packageManagerInternal.setLocationExtraPackagesProvider(new PackageManagerInternal.PackagesProvider() {
            /* class com.android.server.$$Lambda$LocationManagerService$pUnNobtfzLC9eAlVqCMKySwbo3U */

            public final String[] getPackages(int i) {
                return LocationManagerService.this.lambda$new$1$LocationManagerService(i);
            }
        });
        if (getVerboseLoggingLevel() > 0) {
            enableVerboseLogging(1);
        } else {
            enableVerboseLogging(0);
        }
    }

    public /* synthetic */ String[] lambda$new$0$LocationManagerService(int userId) {
        return this.mContext.getResources().getStringArray(17236041);
    }

    public /* synthetic */ String[] lambda$new$1$LocationManagerService(int userId) {
        return this.mContext.getResources().getStringArray(17236040);
    }

    public void systemRunning() {
        synchronized (this.mLock) {
            initMtkLocationManagerService();
            initializeLocked();
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v41, resolved type: android.app.AppOpsManager} */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r1v16, types: [android.app.AppOpsManager$OnOpChangedListener, com.android.server.LocationManagerService$1] */
    @GuardedBy({"mLock"})
    private void initializeLocked() {
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mPackageManager = this.mContext.getPackageManager();
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService(IColorAppStartupManager.TYPE_ACTIVITY);
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mLocationFudger = new LocationFudger(this.mContext, this.mHandler);
        this.mBlacklist = new LocationBlacklist(this.mContext, this.mHandler);
        this.mBlacklist.init();
        this.mGeofenceManager = new GeofenceManager(this.mContext, this.mBlacklist);
        this.mFastNetworkLocation = PswServiceFactory.getInstance().getFeature(IPswFastNetworkLocation.DEFAULT, new Object[]{this.mContext, null});
        this.mGnssWhiteListProxy = PswServiceFactory.getInstance().getFeature(IPswOppoGnssWhiteListProxy.DEFAULT, new Object[]{this.mContext});
        this.mOppoGnssDuration = PswServiceFactory.getInstance().getFeature(IPswGnssDuration.DEFAULT, new Object[]{this.mContext});
        this.mOppoLbsRepairer = PswServiceFactory.getInstance().getFeature(IPswLbsRepairer.DEFAULT, new Object[]{this.mContext});
        this.mOppoLocationBlacklistUtil = PswServiceFactory.getInstance().getFeature(IPswLocationBlacklistUtil.DEFAULT, new Object[0]);
        this.mOppoLocationStatistics = PswServiceFactory.getInstance().getFeature(IPswLocationStatistics.DEFAULT, new Object[0]);
        this.mOppoLocationBlacklistUtil.init(this.mContext, this.mHandler.getLooper());
        this.mOppoLocationStatistics.init(this.mContext, this.mRequestStatistics);
        initializeProvidersLocked();
        this.mNavigationStatusController = PswServiceFactory.getInstance().getFeature(IPswNavigationStatusController.DEFAULT, new Object[]{this.mContext, this.mGnssProvider});
        this.mAppOps.startWatchingMode(0, null, 1, new AppOpsManager.OnOpChangedInternalListener() {
            /* class com.android.server.LocationManagerService.AnonymousClass1 */

            public void onOpChanged(int op, String packageName) {
                if (LocationManagerService.this.mLocationWorkHandler.hasMessages(101)) {
                    LocationManagerService.this.mLocationWorkHandler.removeMessages(101);
                }
                LocationManagerService.this.mLocationWorkHandler.sendMessage(Message.obtain(LocationManagerService.this.mLocationWorkHandler, 101));
            }
        });
        this.mPackageManager.addOnPermissionsChangeListener(new PackageManager.OnPermissionsChangedListener() {
            /* class com.android.server.$$Lambda$LocationManagerService$2PZQdsle7L3JDh5TZyL5YAyDqTk */

            public final void onPermissionsChanged(int i) {
                LocationManagerService.this.lambda$initializeLocked$3$LocationManagerService(i);
            }
        });
        this.mActivityManager.addOnUidImportanceListener(new ActivityManager.OnUidImportanceListener() {
            /* class com.android.server.$$Lambda$LocationManagerService$tHPgS5c0niUhGntiX8gOnWrZpg8 */

            public final void onUidImportance(int i, int i2) {
                LocationManagerService.this.lambda$initializeLocked$5$LocationManagerService(i, i2);
            }
        }, 125);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("location_mode"), true, new ContentObserver(this.mHandler) {
            /* class com.android.server.LocationManagerService.AnonymousClass2 */

            public void onChange(boolean selfChange) {
                synchronized (LocationManagerService.this.mLock) {
                    LocationManagerService.this.onLocationModeChangedLocked(true);
                }
            }
        }, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("location_providers_allowed"), true, new ContentObserver(this.mHandler) {
            /* class com.android.server.LocationManagerService.AnonymousClass3 */

            public void onChange(boolean selfChange) {
                synchronized (LocationManagerService.this.mLock) {
                    LocationManagerService.this.onProviderAllowedChangedLocked();
                }
            }
        }, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("location_background_throttle_interval_ms"), true, new ContentObserver(this.mHandler) {
            /* class com.android.server.LocationManagerService.AnonymousClass4 */

            public void onChange(boolean selfChange) {
                synchronized (LocationManagerService.this.mLock) {
                    LocationManagerService.this.onBackgroundThrottleIntervalChangedLocked();
                }
            }
        }, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("location_background_throttle_package_whitelist"), true, new ContentObserver(this.mHandler) {
            /* class com.android.server.LocationManagerService.AnonymousClass5 */

            public void onChange(boolean selfChange) {
                synchronized (LocationManagerService.this.mLock) {
                    LocationManagerService.this.onBackgroundThrottleWhitelistChangedLocked();
                }
            }
        }, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("location_ignore_settings_package_whitelist"), true, new ContentObserver(this.mHandler) {
            /* class com.android.server.LocationManagerService.AnonymousClass6 */

            public void onChange(boolean selfChange) {
                synchronized (LocationManagerService.this.mLock) {
                    LocationManagerService.this.onIgnoreSettingsWhitelistChangedLocked();
                }
            }
        }, -1);
        ((PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class)).registerLowPowerModeObserver(1, new Consumer() {
            /* class com.android.server.$$Lambda$LocationManagerService$g2YvHnuXGNr_JWSge7Toq3BS9cY */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                LocationManagerService.this.lambda$initializeLocked$7$LocationManagerService((PowerSaveState) obj);
            }
        });
        new PackageMonitor() {
            /* class com.android.server.LocationManagerService.AnonymousClass7 */

            public void onPackageDisappeared(String packageName, int reason) {
                synchronized (LocationManagerService.this.mLock) {
                    LocationManagerService.this.onPackageDisappearedLocked(packageName);
                }
            }
        }.register(this.mContext, this.mHandler.getLooper(), true);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_ADDED");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_REMOVED");
        intentFilter.addAction("android.intent.action.SCREEN_OFF");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        intentFilter.addAction(ACTION_MTKLOGGER_STATE_CHANGED);
        intentFilter.addAction("oppo.location.blacklist.update.gps.requirements");
        this.mContext.registerReceiverAsUser(new BroadcastReceiver() {
            /* class com.android.server.LocationManagerService.AnonymousClass8 */

            /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null) {
                    synchronized (LocationManagerService.this.mLock) {
                        char c = 65535;
                        int i = 1;
                        switch (action.hashCode()) {
                            case -2128145023:
                                if (action.equals("android.intent.action.SCREEN_OFF")) {
                                    c = 4;
                                    break;
                                }
                                break;
                            case -1454123155:
                                if (action.equals("android.intent.action.SCREEN_ON")) {
                                    c = 3;
                                    break;
                                }
                                break;
                            case -385593787:
                                if (action.equals("android.intent.action.MANAGED_PROFILE_ADDED")) {
                                    c = 1;
                                    break;
                                }
                                break;
                            case -320665661:
                                if (action.equals(LocationManagerService.ACTION_MTKLOGGER_STATE_CHANGED)) {
                                    c = 5;
                                    break;
                                }
                                break;
                            case 98097949:
                                if (action.equals("oppo.location.blacklist.update.gps.requirements")) {
                                    c = 6;
                                    break;
                                }
                                break;
                            case 959232034:
                                if (action.equals("android.intent.action.USER_SWITCHED")) {
                                    c = 0;
                                    break;
                                }
                                break;
                            case 1051477093:
                                if (action.equals("android.intent.action.MANAGED_PROFILE_REMOVED")) {
                                    c = 2;
                                    break;
                                }
                                break;
                        }
                        switch (c) {
                            case 0:
                                LocationManagerService.this.onUserChangedLocked(intent.getIntExtra("android.intent.extra.user_handle", 0));
                                break;
                            case 1:
                            case 2:
                                LocationManagerService.this.onUserProfilesChangedLocked();
                                break;
                            case 3:
                            case 4:
                                LocationManagerService.this.onScreenStateChangedLocked();
                                break;
                            case 5:
                                LocationManagerService locationManagerService = LocationManagerService.this;
                                if (LocationManagerService.this.getVerboseLoggingLevel() <= 0) {
                                    i = 0;
                                }
                                locationManagerService.enableVerboseLogging(i);
                                break;
                            case 6:
                                if (LocationManagerService.D) {
                                    Log.v(LocationManagerService.TAG, "locationmanagerservice receive broadcast:oppo.location.blacklist.update.gps.requirements");
                                }
                                LocationManagerService.this.applyRequirementsLocked("gps");
                                break;
                        }
                    }
                }
            }
        }, UserHandle.ALL, intentFilter, null, this.mHandler);
        this.mCurrentUserId = -10000;
        onUserChangedLocked(0);
        onBackgroundThrottleWhitelistChangedLocked();
        onIgnoreSettingsWhitelistChangedLocked();
        onBatterySaverModeChangedLocked(this.mPowerManager.getLocationPowerSaveMode());
    }

    public /* synthetic */ void lambda$initializeLocked$3$LocationManagerService(int uid) {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.$$Lambda$LocationManagerService$GJjItJofmJkJhbftqezuIe8Sio */

            public final void run() {
                LocationManagerService.this.lambda$initializeLocked$2$LocationManagerService();
            }
        });
    }

    public /* synthetic */ void lambda$initializeLocked$2$LocationManagerService() {
        synchronized (this.mLock) {
            onPermissionsChangedLocked();
        }
    }

    public /* synthetic */ void lambda$initializeLocked$5$LocationManagerService(int uid, int importance) {
        this.mHandler.post(new Runnable(uid, importance) {
            /* class com.android.server.$$Lambda$LocationManagerService$GVLGDgL1Vk3AKozMjRmo3OLpQ */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                LocationManagerService.this.lambda$initializeLocked$4$LocationManagerService(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$initializeLocked$4$LocationManagerService(int uid, int importance) {
        synchronized (this.mLock) {
            onUidImportanceChangedLocked(uid, importance);
        }
    }

    public /* synthetic */ void lambda$initializeLocked$7$LocationManagerService(PowerSaveState state) {
        this.mHandler.post(new Runnable(state) {
            /* class com.android.server.$$Lambda$LocationManagerService$wT7D5HWSJcE1hXhYNGDPH6IVDx0 */
            private final /* synthetic */ PowerSaveState f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                LocationManagerService.this.lambda$initializeLocked$6$LocationManagerService(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$initializeLocked$6$LocationManagerService(PowerSaveState state) {
        synchronized (this.mLock) {
            onBatterySaverModeChangedLocked(state.locationMode);
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public void onAppOpChangedLocked() {
        for (Receiver receiver : this.mReceivers.values()) {
            receiver.updateMonitoring(true);
        }
        Iterator<LocationProvider> it = this.mProviders.iterator();
        while (it.hasNext()) {
            applyRequirementsLocked(it.next());
        }
    }

    @GuardedBy({"mLock"})
    private void onPermissionsChangedLocked() {
        Iterator<LocationProvider> it = this.mProviders.iterator();
        while (it.hasNext()) {
            applyRequirementsLocked(it.next());
        }
    }

    @GuardedBy({"mLock"})
    private void onBatterySaverModeChangedLocked(int newLocationMode) {
        if (D) {
            Slog.d(TAG, "Battery Saver location mode changed from " + PowerManager.locationPowerSaveModeToString(this.mBatterySaverMode) + " to " + PowerManager.locationPowerSaveModeToString(newLocationMode));
        }
        if (this.mBatterySaverMode != newLocationMode) {
            this.mBatterySaverMode = newLocationMode;
            Iterator<LocationProvider> it = this.mProviders.iterator();
            while (it.hasNext()) {
                applyRequirementsLocked(it.next());
            }
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public void onScreenStateChangedLocked() {
        if (this.mBatterySaverMode == 4) {
            Iterator<LocationProvider> it = this.mProviders.iterator();
            while (it.hasNext()) {
                applyRequirementsLocked(it.next());
            }
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public void onLocationModeChangedLocked(boolean broadcast) {
        Log.d(TAG, "onLocationModeChangedLocked broadcast:" + broadcast);
        if (D) {
            Log.d(TAG, "location enabled is now " + isLocationEnabled());
        }
        Iterator<LocationProvider> it = this.mProviders.iterator();
        while (it.hasNext()) {
            it.next().onLocationModeChangedLocked();
        }
        if (broadcast) {
            this.mContext.sendBroadcastAsUser(new Intent("android.location.MODE_CHANGED"), UserHandle.ALL);
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public void onProviderAllowedChangedLocked() {
        Log.d(TAG, "onProviderAllowedChangedLocked");
        Iterator<LocationProvider> it = this.mProviders.iterator();
        while (it.hasNext()) {
            it.next().onAllowedChangedLocked();
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public void onPackageDisappearedLocked(String packageName) {
        ArrayList<Receiver> deadReceivers = null;
        for (Receiver receiver : this.mReceivers.values()) {
            if (receiver.mCallerIdentity.mPackageName.equals(packageName)) {
                if (deadReceivers == null) {
                    deadReceivers = new ArrayList<>();
                }
                deadReceivers.add(receiver);
            }
        }
        if (deadReceivers != null) {
            Iterator<Receiver> it = deadReceivers.iterator();
            while (it.hasNext()) {
                removeUpdatesLocked(it.next());
            }
        }
    }

    @GuardedBy({"mLock"})
    private void onUidImportanceChangedLocked(int uid, int importance) {
        boolean foreground = isImportanceForeground(importance);
        HashSet<String> affectedProviders = new HashSet<>(this.mRecordsByProvider.size());
        for (Map.Entry<String, ArrayList<UpdateRecord>> entry : this.mRecordsByProvider.entrySet()) {
            String provider = entry.getKey();
            Iterator<UpdateRecord> it = entry.getValue().iterator();
            while (it.hasNext()) {
                UpdateRecord record = it.next();
                if (record.mReceiver.mCallerIdentity.mUid == uid && record.mIsForegroundUid != foreground) {
                    Log.d(TAG, "request from uid " + uid + " is now " + foregroundAsString(foreground));
                    record.updateForeground(foreground);
                    if (!isThrottlingExemptLocked(record.mReceiver.mCallerIdentity)) {
                        affectedProviders.add(provider);
                    }
                }
            }
        }
        Iterator<String> it2 = affectedProviders.iterator();
        while (it2.hasNext()) {
            applyRequirementsLocked(it2.next());
        }
        updateGnssDataProviderOnUidImportanceChangedLocked(this.mGnssMeasurementsListeners, this.mGnssMeasurementsProvider, $$Lambda$qoNbXUvSu3yuTPVXPUfZW_HDrTQ.INSTANCE, uid, foreground);
        updateGnssDataProviderOnUidImportanceChangedLocked(this.mGnssNavigationMessageListeners, this.mGnssNavigationMessageProvider, $$Lambda$HALkbmbB2IPr_wdFkPjiIWCzJsY.INSTANCE, uid, foreground);
        updateGnssDataProviderOnUidImportanceChangedLocked(this.mGnssStatusListeners, this.mGnssStatusProvider, $$Lambda$hu4394T6QBT8QyZnspMtXqICWs.INSTANCE, uid, foreground);
    }

    @GuardedBy({"mLock"})
    private <TListener extends IInterface> void updateGnssDataProviderOnUidImportanceChangedLocked(ArrayMap<IBinder, ? extends LinkedListenerBase> gnssDataListeners, RemoteListenerHelper<TListener> gnssDataProvider, Function<IBinder, TListener> mapBinderToListener, int uid, boolean foreground) {
        for (Map.Entry<IBinder, ? extends LinkedListenerBase> entry : gnssDataListeners.entrySet()) {
            LinkedListenerBase linkedListener = (LinkedListenerBase) entry.getValue();
            CallerIdentity callerIdentity = linkedListener.mCallerIdentity;
            if (callerIdentity.mUid == uid) {
                if (D) {
                    Log.d(TAG, linkedListener.mListenerName + " from uid " + uid + " is now " + foregroundAsString(foreground));
                }
                TListener listener = mapBinderToListener.apply(entry.getKey());
                if (foreground || isThrottlingExemptLocked(callerIdentity)) {
                    gnssDataProvider.addListener(listener, callerIdentity);
                } else {
                    gnssDataProvider.removeListener(listener);
                }
            }
        }
    }

    private static String foregroundAsString(boolean foreground) {
        return foreground ? "foreground" : "background";
    }

    /* access modifiers changed from: private */
    public static boolean isImportanceForeground(int importance) {
        return importance <= 125;
    }

    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public void onBackgroundThrottleIntervalChangedLocked() {
        Iterator<LocationProvider> it = this.mProviders.iterator();
        while (it.hasNext()) {
            applyRequirementsLocked(it.next());
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public void onBackgroundThrottleWhitelistChangedLocked() {
        this.mBackgroundThrottlePackageWhitelist.clear();
        this.mBackgroundThrottlePackageWhitelist.addAll(SystemConfig.getInstance().getAllowUnthrottledLocation());
        String setting = Settings.Global.getString(this.mContext.getContentResolver(), "location_background_throttle_package_whitelist");
        if (!TextUtils.isEmpty(setting)) {
            this.mBackgroundThrottlePackageWhitelist.addAll(Arrays.asList(setting.split(",")));
        }
        Iterator<LocationProvider> it = this.mProviders.iterator();
        while (it.hasNext()) {
            applyRequirementsLocked(it.next());
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy({"lock"})
    public void onIgnoreSettingsWhitelistChangedLocked() {
        this.mIgnoreSettingsPackageWhitelist.clear();
        this.mIgnoreSettingsPackageWhitelist.addAll(SystemConfig.getInstance().getAllowIgnoreLocationSettings());
        String setting = Settings.Global.getString(this.mContext.getContentResolver(), "location_ignore_settings_package_whitelist");
        Log.d(TAG, "Ignore whitelist changed as: " + setting);
        if (!TextUtils.isEmpty(setting)) {
            this.mIgnoreSettingsPackageWhitelist.addAll(Arrays.asList(setting.split(",")));
        }
        Iterator<LocationProvider> it = this.mProviders.iterator();
        while (it.hasNext()) {
            applyRequirementsLocked(it.next());
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public void onUserProfilesChangedLocked() {
        this.mCurrentUserProfiles = this.mUserManager.getProfileIdsWithDisabled(this.mCurrentUserId);
    }

    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public boolean isCurrentProfileLocked(int userId) {
        if (!OppoFeatureCache.get(IColorMultiAppManager.DEFAULT).isCurrentProfile(userId) && 1000 != Binder.getCallingUid()) {
            return ArrayUtils.contains(this.mCurrentUserProfiles, userId);
        }
        return true;
    }

    @GuardedBy({"mLock"})
    private void ensureFallbackFusedProviderPresentLocked(String[] pkgs) {
        PackageManager pm = this.mContext.getPackageManager();
        String systemPackageName = this.mContext.getPackageName();
        ArrayList<HashSet<Signature>> sigSets = ServiceWatcher.getSignatureSets(this.mContext, pkgs);
        for (ResolveInfo rInfo : pm.queryIntentServicesAsUser(new Intent(FUSED_LOCATION_SERVICE_ACTION), 128, this.mCurrentUserId)) {
            String packageName = rInfo.serviceInfo.packageName;
            try {
                if (!ServiceWatcher.isSignatureMatch(pm.getPackageInfo(packageName, 64).signatures, sigSets)) {
                    Log.w(TAG, packageName + " resolves service " + FUSED_LOCATION_SERVICE_ACTION + ", but has wrong signature, ignoring");
                } else if (rInfo.serviceInfo.metaData == null) {
                    Log.w(TAG, "Found fused provider without metadata: " + packageName);
                } else if (rInfo.serviceInfo.metaData.getInt(ServiceWatcher.EXTRA_SERVICE_VERSION, -1) == 0) {
                    if ((rInfo.serviceInfo.applicationInfo.flags & 1) == 0) {
                        if (D) {
                            Log.d(TAG, "Fallback candidate not in /system: " + packageName);
                        }
                    } else if (pm.checkSignatures(systemPackageName, packageName) != 0) {
                        if (D) {
                            Log.d(TAG, "Fallback candidate not signed the same as system: " + packageName);
                        }
                    } else if (D) {
                        Log.d(TAG, "Found fallback provider: " + packageName);
                        return;
                    } else {
                        return;
                    }
                } else if (D) {
                    Log.d(TAG, "Fallback candidate not version 0: " + packageName);
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "missing package: " + packageName);
            }
        }
        throw new IllegalStateException("Unable to find a fused location provider that is in the system partition with version 0 and signed with the platform certificate. Such a package is needed to provide a default fused location provider in the event that no other fused location provider has been installed or is currently available. For example, coreOnly boot mode when decrypting the data partition. The fallback must also be marked coreApp=\"true\" in the manifest");
    }

    @GuardedBy({"mLock"})
    private void initializeProvidersLocked() {
        ActivityRecognitionHardware activityRecognitionHardware;
        LocationManagerService locationManagerService = this;
        LocationProvider passiveProviderManager = new LocationProvider("passive");
        locationManagerService.addProviderLocked(passiveProviderManager);
        locationManagerService.mPassiveProvider = new PassiveProvider(locationManagerService.mContext, passiveProviderManager);
        passiveProviderManager.attachLocked(locationManagerService.mPassiveProvider);
        if (GnssLocationProvider.isSupported()) {
            LocationProvider gnssProviderManager = new LocationProvider("gps", true);
            locationManagerService.mRealProviders.add(gnssProviderManager);
            locationManagerService.addProviderLocked(gnssProviderManager);
            GnssLocationProvider gnssProvider = new GnssLocationProvider(locationManagerService.mContext, gnssProviderManager, locationManagerService.mHandler.getLooper());
            gnssProviderManager.attachLocked(gnssProvider);
            locationManagerService.mGnssSystemInfoProvider = gnssProvider.getGnssSystemInfoProvider();
            locationManagerService.mGnssBatchingProvider = gnssProvider.getGnssBatchingProvider();
            locationManagerService.mGnssMetricsProvider = gnssProvider.getGnssMetricsProvider();
            locationManagerService.mGnssCapabilitiesProvider = gnssProvider.getGnssCapabilitiesProvider();
            locationManagerService.mGnssStatusProvider = gnssProvider.getGnssStatusProvider();
            locationManagerService.mNetInitiatedListener = gnssProvider.getNetInitiatedListener();
            locationManagerService.mGnssMeasurementsProvider = gnssProvider.getGnssMeasurementsProvider();
            locationManagerService.mGnssMeasurementCorrectionsProvider = gnssProvider.getGnssMeasurementCorrectionsProvider();
            locationManagerService.mGnssNavigationMessageProvider = gnssProvider.getGnssNavigationMessageProvider();
            locationManagerService.mGpsGeofenceProxy = gnssProvider.getGpsGeofenceProxy();
            locationManagerService.mGnssProvider = gnssProvider;
        }
        Resources resources = locationManagerService.mContext.getResources();
        String[] pkgs = resources.getStringArray(17236041);
        if (D) {
            Log.d(TAG, "certificates for location providers pulled from: " + Arrays.toString(pkgs));
        }
        locationManagerService.ensureFallbackFusedProviderPresentLocked(pkgs);
        LocationProvider networkProviderManager = new LocationProvider("network", true);
        locationManagerService.mNlpProxy = PswServiceFactory.getInstance().getFeature(IPswNlpProxy.DEFAULT, new Object[]{locationManagerService.mContext, networkProviderManager});
        LocationProviderProxy networkProvider = locationManagerService.mNlpProxy.getActiveNpProxy();
        if (networkProvider != null) {
            locationManagerService.mRealProviders.add(networkProviderManager);
            locationManagerService.addProviderLocked(networkProviderManager);
            networkProviderManager.attachLocked(networkProvider);
        } else {
            Slog.w(TAG, "no network location provider found");
        }
        LocationProvider fusedProviderManager = new LocationProvider("fused");
        LocationProviderProxy fusedProvider = LocationProviderProxy.createAndBind(locationManagerService.mContext, fusedProviderManager, FUSED_LOCATION_SERVICE_ACTION, 17891437, 17039733, 17236041);
        if (fusedProvider != null) {
            locationManagerService.mRealProviders.add(fusedProviderManager);
            locationManagerService.addProviderLocked(fusedProviderManager);
            fusedProviderManager.attachLocked(fusedProvider);
        } else {
            Slog.e(TAG, "no fused location provider found", new IllegalStateException("Location service needs a fused location provider"));
        }
        locationManagerService.mGeocodeProvider = locationManagerService.mNlpProxy.getActiveGeocoderProxy();
        if (locationManagerService.mGeocodeProvider == null) {
            Slog.e(TAG, "no geocoder provider found");
        }
        if (GeofenceProxy.createAndBind(locationManagerService.mContext, 17891439, 17039735, 17236041, locationManagerService.mGpsGeofenceProxy, null) == null) {
            Slog.d(TAG, "Unable to bind FLP Geofence proxy.");
        }
        boolean activityRecognitionHardwareIsSupported = ActivityRecognitionHardware.isSupported();
        if (activityRecognitionHardwareIsSupported) {
            activityRecognitionHardware = ActivityRecognitionHardware.getInstance(locationManagerService.mContext);
        } else {
            Slog.d(TAG, "Hardware Activity-Recognition not supported.");
            activityRecognitionHardware = null;
        }
        if (ActivityRecognitionProxy.createAndBind(locationManagerService.mContext, activityRecognitionHardwareIsSupported, activityRecognitionHardware, 17891431, 17039670, 17236041) == null) {
            Slog.d(TAG, "Unable to bind ActivityRecognitionProxy.");
        }
        String[] testProviderStrings = resources.getStringArray(17236079);
        int length = testProviderStrings.length;
        int i = 0;
        while (i < length) {
            String[] fragments = testProviderStrings[i].split(",");
            String name = fragments[0].trim();
            ProviderProperties properties = new ProviderProperties(Boolean.parseBoolean(fragments[1]), Boolean.parseBoolean(fragments[2]), Boolean.parseBoolean(fragments[3]), Boolean.parseBoolean(fragments[4]), Boolean.parseBoolean(fragments[5]), Boolean.parseBoolean(fragments[6]), Boolean.parseBoolean(fragments[7]), Integer.parseInt(fragments[8]), Integer.parseInt(fragments[9]));
            LocationProvider testProviderManager = new LocationProvider(name);
            locationManagerService.addProviderLocked(testProviderManager);
            new MockProvider(locationManagerService.mContext, testProviderManager, properties);
            i++;
            locationManagerService = this;
            passiveProviderManager = passiveProviderManager;
            activityRecognitionHardware = activityRecognitionHardware;
            resources = resources;
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public void onUserChangedLocked(int userId) {
        if (this.mCurrentUserId != userId) {
            if (D) {
                Log.d(TAG, "foreground user is changing to " + userId);
            }
            Iterator<LocationProvider> it = this.mProviders.iterator();
            while (it.hasNext()) {
                it.next().onUserChangingLocked();
            }
            this.mCurrentUserId = userId;
            onUserProfilesChangedLocked();
            this.mBlacklist.switchUser(userId);
            onLocationModeChangedLocked(false);
            onProviderAllowedChangedLocked();
            Iterator<LocationProvider> it2 = this.mProviders.iterator();
            while (it2.hasNext()) {
                it2.next().onUseableChangedLocked(false);
            }
        }
    }

    /* access modifiers changed from: private */
    public class LocationProvider extends OppoBaseLocationProvider implements AbstractLocationProvider.LocationProviderManager {
        @GuardedBy({"mLock"})
        private boolean mAllowed;
        @GuardedBy({"mLock"})
        private boolean mEnabled;
        private final boolean mIsManagedBySettings;
        private final String mName;
        @GuardedBy({"mLock"})
        private ProviderProperties mProperties;
        @GuardedBy({"mLock"})
        protected AbstractLocationProvider mProvider;
        @GuardedBy({"mLock"})
        private boolean mUseable;

        /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
         method: com.android.server.LocationManagerService.LocationProvider.<init>(com.android.server.LocationManagerService, java.lang.String, boolean):void
         arg types: [com.android.server.LocationManagerService, java.lang.String, int]
         candidates:
          com.android.server.LocationManagerService.LocationProvider.<init>(com.android.server.LocationManagerService, java.lang.String, com.android.server.LocationManagerService$1):void
          com.android.server.LocationManagerService.LocationProvider.<init>(com.android.server.LocationManagerService, java.lang.String, boolean):void */
        private LocationProvider(LocationManagerService locationManagerService, String name) {
            this(name, false);
        }

        private LocationProvider(String name, boolean isManagedBySettings) {
            this.mName = name;
            this.mIsManagedBySettings = isManagedBySettings;
            this.mProvider = null;
            this.mUseable = false;
            boolean z = this.mIsManagedBySettings;
            this.mAllowed = !z;
            this.mEnabled = false;
            this.mProperties = null;
            if (z) {
                ContentResolver contentResolver = LocationManagerService.this.mContext.getContentResolver();
                Settings.Secure.putStringForUser(contentResolver, "location_providers_allowed", "-" + this.mName, LocationManagerService.this.mCurrentUserId);
            }
        }

        @GuardedBy({"mLock"})
        public void attachLocked(AbstractLocationProvider provider) {
            Preconditions.checkNotNull(provider);
            Preconditions.checkState(this.mProvider == null);
            if (LocationManagerService.D) {
                Log.d(LocationManagerService.TAG, this.mName + " provider attached");
            }
            this.mProvider = provider;
            onUseableChangedLocked(false);
        }

        public String getName() {
            return this.mName;
        }

        @GuardedBy({"mLock"})
        public List<String> getPackagesLocked() {
            AbstractLocationProvider abstractLocationProvider = this.mProvider;
            if (abstractLocationProvider == null) {
                return Collections.emptyList();
            }
            return abstractLocationProvider.getProviderPackages();
        }

        public boolean isMock() {
            return false;
        }

        @GuardedBy({"mLock"})
        public boolean isPassiveLocked() {
            return this.mProvider == LocationManagerService.this.mPassiveProvider;
        }

        @GuardedBy({"mLock"})
        public ProviderProperties getPropertiesLocked() {
            return this.mProperties;
        }

        @GuardedBy({"mLock"})
        public void setRequestLocked(ProviderRequest request, WorkSource workSource) {
            if (this.mProvider != null) {
                long identity = Binder.clearCallingIdentity();
                try {
                    if (!this.mName.equals("network") || (this.mProvider instanceof MockProvider)) {
                        if (this.mProvider instanceof MockProvider) {
                            Log.e(LocationManagerService.TAG, "mockprovider is in use : " + this.mName);
                        }
                        this.mProvider.setRequest(request, workSource);
                    } else {
                        LocationManagerService.this.mNlpProxy.setRequestTry(request, workSource);
                    }
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        @GuardedBy({"mLock"})
        public void dumpLocked(FileDescriptor fd, PrintWriter pw, String[] args) {
            pw.print("  " + this.mName + " provider");
            if (isMock()) {
                pw.print(" [mock]");
            }
            pw.println(":");
            pw.println("    useable=" + this.mUseable);
            if (!this.mUseable) {
                StringBuilder sb = new StringBuilder();
                sb.append("    attached=");
                sb.append(this.mProvider != null);
                pw.println(sb.toString());
                if (this.mIsManagedBySettings) {
                    pw.println("    allowed=" + this.mAllowed);
                }
                pw.println("    enabled=" + this.mEnabled);
            }
            pw.println("    properties=" + this.mProperties);
            pw.println("    illRec=" + LocationManagerService.this.mOppoLbsRepairer.getRec());
            if (this.mProvider != null) {
                long identity = Binder.clearCallingIdentity();
                try {
                    this.mProvider.dump(fd, pw, args);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        @GuardedBy({"mLock"})
        public long getStatusUpdateTimeLocked() {
            if (this.mProvider == null) {
                return 0;
            }
            long identity = Binder.clearCallingIdentity();
            try {
                return this.mProvider.getStatusUpdateTime();
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        @GuardedBy({"mLock"})
        public int getStatusLocked(Bundle extras) {
            if (this.mProvider == null) {
                return 2;
            }
            long identity = Binder.clearCallingIdentity();
            try {
                return this.mProvider.getStatus(extras);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        @GuardedBy({"mLock"})
        public void sendExtraCommandLocked(String command, Bundle extras) {
            if (LocationManagerService.this.mGnssWhiteListProxy.isAllowedChangeChipData(this.mName, command) && this.mProvider != null) {
                long identity = Binder.clearCallingIdentity();
                try {
                    this.mProvider.sendExtraCommand(command, extras);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        @Override // com.android.server.location.AbstractLocationProvider.LocationProviderManager
        public void onReportLocation(Location location) {
            if (this.mName.equals("network")) {
                LocationManagerService.this.mFastNetworkLocation.setLastLocation(location);
            }
            LocationManagerService.this.mHandler.post(new Runnable(location) {
                /* class com.android.server.$$Lambda$LocationManagerService$LocationProvider$R123rmQLJrCf8yBSKrQD6XPhpZs */
                private final /* synthetic */ Location f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    LocationManagerService.LocationProvider.this.lambda$onReportLocation$0$LocationManagerService$LocationProvider(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$onReportLocation$0$LocationManagerService$LocationProvider(Location location) {
            synchronized (LocationManagerService.this.mLock) {
                LocationManagerService.this.handleLocationChangedLocked(location, this);
            }
        }

        @Override // com.android.server.location.AbstractLocationProvider.LocationProviderManager
        public void onReportLocation(List<Location> locations) {
            LocationManagerService.this.mHandler.post(new Runnable(locations) {
                /* class com.android.server.$$Lambda$LocationManagerService$LocationProvider$UwV519Q998DTiPhy1rbdXyO3Geo */
                private final /* synthetic */ List f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    LocationManagerService.LocationProvider.this.lambda$onReportLocation$1$LocationManagerService$LocationProvider(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$onReportLocation$1$LocationManagerService$LocationProvider(List locations) {
            synchronized (LocationManagerService.this.mLock) {
                LocationProvider gpsProvider = LocationManagerService.this.getLocationProviderLocked("gps");
                if (gpsProvider != null) {
                    if (gpsProvider.isUseableLocked()) {
                        if (LocationManagerService.this.mGnssBatchingCallback == null) {
                            Slog.e(LocationManagerService.TAG, "reportLocationBatch() called without active Callback");
                            return;
                        }
                        try {
                            LocationManagerService.this.mGnssBatchingCallback.onLocationBatch(locations);
                        } catch (RemoteException e) {
                            Slog.e(LocationManagerService.TAG, "mGnssBatchingCallback.onLocationBatch failed", e);
                        }
                    }
                }
                Slog.w(LocationManagerService.TAG, "reportLocationBatch() called without user permission");
            }
        }

        @Override // com.android.server.location.AbstractLocationProvider.LocationProviderManager
        public void onSetEnabled(boolean enabled) {
            if (!LocationManagerService.this.mOppoLbsRepairer.ignoreDisabled(this.mName, enabled)) {
                LocationManagerService.this.mHandler.post(new Runnable(enabled) {
                    /* class com.android.server.$$Lambda$LocationManagerService$LocationProvider$nsL4uwojBLPzs1TzMfpQIBSm7p0 */
                    private final /* synthetic */ boolean f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        LocationManagerService.LocationProvider.this.lambda$onSetEnabled$2$LocationManagerService$LocationProvider(this.f$1);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$onSetEnabled$2$LocationManagerService$LocationProvider(boolean enabled) {
            synchronized (LocationManagerService.this.mLock) {
                if (enabled != this.mEnabled) {
                    Log.d(LocationManagerService.TAG, this.mName + " provider mEnable: " + this.mEnabled + " --> " + enabled);
                    this.mEnabled = enabled;
                    onUseableChangedLocked(false);
                }
            }
        }

        @Override // com.android.server.location.AbstractLocationProvider.LocationProviderManager
        public void onSetProperties(ProviderProperties properties) {
            synchronized (LocationManagerService.this.mLock) {
                this.mProperties = properties;
            }
        }

        @GuardedBy({"mLock"})
        public void onLocationModeChangedLocked() {
            onUseableChangedLocked(false);
            transProviderStatusToMonitor(false);
        }

        @GuardedBy({"mLock"})
        public void onAllowedChangedLocked() {
            boolean allowed;
            if (this.mIsManagedBySettings && (allowed = TextUtils.delimitedStringContains(Settings.Secure.getStringForUser(LocationManagerService.this.mContext.getContentResolver(), "location_providers_allowed", LocationManagerService.this.mCurrentUserId), ',', this.mName)) != this.mAllowed) {
                Log.d(LocationManagerService.TAG, this.mName + " provider mAllowed: " + this.mAllowed + " --> " + allowed);
                this.mAllowed = allowed;
                onUseableChangedLocked(true);
            }
        }

        @Override // com.android.server.location.OppoBaseLocationProvider
        public void transProviderStatusToMonitor(boolean forceShow) {
            if (LocationManagerService.this.mOppoLbsRepairer != null) {
                LocationManagerService.this.mOppoLbsRepairer.getProviderStatus(this.mName, this.mEnabled, this.mUseable, this.mAllowed, forceShow, LocationManagerService.this.mCurrentUserId);
            }
        }

        @Override // com.android.server.location.OppoBaseLocationProvider
        public boolean isNetworkWhiteList(String packageName) {
            if (!this.mName.equals("network") || LocationManagerService.this.mGnssWhiteListProxy == null) {
                return false;
            }
            return LocationManagerService.this.mGnssWhiteListProxy.inNetworkLocationWhiteList(this.mUseable, packageName);
        }

        @GuardedBy({"mLock"})
        public boolean isUseableLocked() {
            return isUseableForUserLocked(LocationManagerService.this.mCurrentUserId);
        }

        @GuardedBy({"mLock"})
        public boolean isUseableForUserLocked(int userId) {
            return LocationManagerService.this.isCurrentProfileLocked(userId) && this.mUseable;
        }

        @GuardedBy({"mLock"})
        private boolean isUseableIgnoringAllowedLocked() {
            return this.mProvider != null && LocationManagerService.this.mProviders.contains(this) && LocationManagerService.this.isLocationEnabled() && this.mEnabled;
        }

        @GuardedBy({"mLock"})
        public void onUseableChangedLocked(boolean isAllowedChanged) {
            boolean useableIgnoringAllowed = isUseableIgnoringAllowedLocked();
            boolean useable = useableIgnoringAllowed && this.mAllowed;
            if (this.mIsManagedBySettings) {
                if (useableIgnoringAllowed && !isAllowedChanged) {
                    ContentResolver contentResolver = LocationManagerService.this.mContext.getContentResolver();
                    Settings.Secure.putStringForUser(contentResolver, "location_providers_allowed", "+" + this.mName, LocationManagerService.this.mCurrentUserId);
                    Log.d(LocationManagerService.TAG, this.mName + " onUseableChangedLocked Add back provider: " + this.mName);
                } else if (!useableIgnoringAllowed) {
                    ContentResolver contentResolver2 = LocationManagerService.this.mContext.getContentResolver();
                    Settings.Secure.putStringForUser(contentResolver2, "location_providers_allowed", "-" + this.mName, LocationManagerService.this.mCurrentUserId);
                    Log.d(LocationManagerService.TAG, this.mName + " onUseableChangedLocked remove provider: " + this.mName);
                }
                LocationManagerService.this.mOppoLbsRepairer.updateSettings(this.mName, LocationManagerService.this.mCurrentUserId);
                Intent intent = new Intent("android.location.PROVIDERS_CHANGED");
                intent.putExtra("android.location.extra.PROVIDER_NAME", this.mName);
                LocationManagerService.this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            }
            if (useable != this.mUseable) {
                Log.d(LocationManagerService.TAG, this.mName + " provider mUseable: " + this.mUseable + " --> " + useable);
                this.mUseable = useable;
                if (!this.mUseable) {
                    LocationManagerService.this.mLastLocation.clear();
                    LocationManagerService.this.mLastLocationCoarseInterval.clear();
                }
                LocationManagerService.this.updateProviderUseableLocked(this);
            }
        }

        @GuardedBy({"mLock"})
        public void onUserChangingLocked() {
            this.mUseable = false;
            LocationManagerService.this.updateProviderUseableLocked(this);
        }
    }

    private class MockLocationProvider extends LocationProvider {
        /* access modifiers changed from: private */
        public ProviderRequest mCurrentRequest;

        private MockLocationProvider(String name) {
            super(name);
        }

        @Override // com.android.server.LocationManagerService.LocationProvider
        public void attachLocked(AbstractLocationProvider provider) {
            Preconditions.checkState(provider instanceof MockProvider);
            super.attachLocked(provider);
        }

        @Override // com.android.server.LocationManagerService.LocationProvider
        public boolean isMock() {
            return true;
        }

        @GuardedBy({"mLock"})
        public void setEnabledLocked(boolean enabled) {
            if (this.mProvider != null) {
                long identity = Binder.clearCallingIdentity();
                try {
                    ((MockProvider) this.mProvider).setEnabled(enabled);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        @GuardedBy({"mLock"})
        public void setLocationLocked(Location location) {
            if (this.mProvider != null) {
                long identity = Binder.clearCallingIdentity();
                try {
                    ((MockProvider) this.mProvider).setLocation(location);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        @Override // com.android.server.LocationManagerService.LocationProvider
        @GuardedBy({"mLock"})
        public void setRequestLocked(ProviderRequest request, WorkSource workSource) {
            super.setRequestLocked(request, workSource);
            this.mCurrentRequest = request;
        }

        @GuardedBy({"mLock"})
        public void setStatusLocked(int status, Bundle extras, long updateTime) {
            if (this.mProvider != null) {
                long identity = Binder.clearCallingIdentity();
                try {
                    ((MockProvider) this.mProvider).setStatus(status, extras, updateTime);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public final class Receiver extends LinkedListenerBase implements PendingIntent.OnFinished {
        private static final long WAKELOCK_TIMEOUT_MILLIS = 60000;
        /* access modifiers changed from: private */
        public final int mAllowedResolutionLevel;
        private final boolean mHideFromAppOps;
        /* access modifiers changed from: private */
        public final Object mKey;
        private final ILocationListener mListener;
        private boolean mOpHighPowerMonitoring;
        private boolean mOpMonitoring;
        private int mPendingBroadcasts;
        final PendingIntent mPendingIntent;
        final HashMap<String, UpdateRecord> mUpdateRecords;
        PowerManager.WakeLock mWakeLock;
        final WorkSource mWorkSource;

        private Receiver(ILocationListener listener, PendingIntent intent, int pid, int uid, String packageName, WorkSource workSource, boolean hideFromAppOps) {
            super(new CallerIdentity(uid, pid, packageName), "LocationListener");
            this.mUpdateRecords = new HashMap<>();
            this.mListener = listener;
            this.mPendingIntent = intent;
            if (listener != null) {
                this.mKey = listener.asBinder();
            } else {
                this.mKey = intent;
            }
            this.mAllowedResolutionLevel = LocationManagerService.this.getAllowedResolutionLevel(pid, uid);
            if (workSource != null && workSource.isEmpty()) {
                workSource = null;
            }
            this.mWorkSource = workSource;
            this.mHideFromAppOps = hideFromAppOps;
            updateMonitoring(true);
            this.mWakeLock = LocationManagerService.this.mPowerManager.newWakeLock(1, LocationManagerService.WAKELOCK_KEY);
            this.mWakeLock.setWorkSource(workSource == null ? new WorkSource(this.mCallerIdentity.mUid, this.mCallerIdentity.mPackageName) : workSource);
            this.mWakeLock.setReferenceCounted(false);
        }

        public boolean equals(Object otherObj) {
            return (otherObj instanceof Receiver) && this.mKey.equals(((Receiver) otherObj).mKey);
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
                s.append(StringUtils.SPACE);
                s.append(this.mUpdateRecords.get(p).toString());
            }
            s.append(" monitoring location: ");
            s.append(this.mOpMonitoring);
            s.append("]");
            return s.toString();
        }

        public void updateMonitoring(boolean allow) {
            if (!this.mHideFromAppOps) {
                boolean requestingLocation = false;
                boolean requestingHighPowerLocation = false;
                if (allow) {
                    Iterator<UpdateRecord> it = this.mUpdateRecords.values().iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        UpdateRecord updateRecord = it.next();
                        LocationProvider provider = LocationManagerService.this.getLocationProviderLocked(updateRecord.mProvider);
                        if (provider != null && (provider.isUseableLocked() || LocationManagerService.this.isSettingsExemptLocked(updateRecord))) {
                            requestingLocation = true;
                            ProviderProperties properties = provider.getPropertiesLocked();
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
            if (!currentlyMonitoring) {
                if (allowMonitoring) {
                    return LocationManagerService.this.mAppOps.startOpNoThrow(op, this.mCallerIdentity.mUid, this.mCallerIdentity.mPackageName) == 0;
                }
            } else if (!allowMonitoring || LocationManagerService.this.mAppOps.checkOpNoThrow(op, this.mCallerIdentity.mUid, this.mCallerIdentity.mPackageName) != 0) {
                if (allowMonitoring && LocationManagerService.this.mGnssWhiteListProxy != null && LocationManagerService.this.mGnssWhiteListProxy.isAllowedPassLocationAccess(this.mCallerIdentity.mPackageName)) {
                    return true;
                }
                LocationManagerService.this.mAppOps.finishOp(op, this.mCallerIdentity.mUid, this.mCallerIdentity.mPackageName);
                return false;
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
            ILocationListener iLocationListener = this.mListener;
            if (iLocationListener != null) {
                return iLocationListener;
            }
            throw new IllegalStateException("Request for non-existent listener");
        }

        public boolean callStatusChangedLocked(String provider, int status, Bundle extras) {
            ILocationListener iLocationListener = this.mListener;
            if (iLocationListener != null) {
                try {
                    iLocationListener.onStatusChanged(provider, status, extras);
                    incrementPendingBroadcastsLocked();
                    return true;
                } catch (RemoteException e) {
                    return false;
                }
            } else {
                Intent statusChanged = new Intent();
                statusChanged.putExtras(new Bundle(extras));
                statusChanged.putExtra("status", status);
                try {
                    this.mPendingIntent.send(LocationManagerService.this.mContext, 0, statusChanged, this, LocationManagerService.this.mHandler, LocationManagerService.this.getResolutionPermission(this.mAllowedResolutionLevel), PendingIntentUtils.createDontSendToRestrictedAppsBundle(null));
                    incrementPendingBroadcastsLocked();
                    return true;
                } catch (PendingIntent.CanceledException e2) {
                    return false;
                }
            }
        }

        public boolean callLocationChangedLocked(Location location) {
            ILocationListener iLocationListener = this.mListener;
            if (iLocationListener != null) {
                try {
                    iLocationListener.onLocationChanged(new Location(location));
                    incrementPendingBroadcastsLocked();
                    return true;
                } catch (RemoteException e) {
                    return false;
                }
            } else {
                Intent locationChanged = new Intent();
                locationChanged.putExtra("location", new Location(location));
                try {
                    this.mPendingIntent.send(LocationManagerService.this.mContext, 0, locationChanged, this, LocationManagerService.this.mHandler, LocationManagerService.this.getResolutionPermission(this.mAllowedResolutionLevel), PendingIntentUtils.createDontSendToRestrictedAppsBundle(null));
                    incrementPendingBroadcastsLocked();
                    return true;
                } catch (PendingIntent.CanceledException e2) {
                    return false;
                }
            }
        }

        /* access modifiers changed from: private */
        public boolean callProviderEnabledLocked(String provider, boolean enabled) {
            updateMonitoring(true);
            ILocationListener iLocationListener = this.mListener;
            if (iLocationListener != null) {
                if (enabled) {
                    try {
                        iLocationListener.onProviderEnabled(provider);
                    } catch (RemoteException e) {
                        return false;
                    }
                } else {
                    iLocationListener.onProviderDisabled(provider);
                }
                incrementPendingBroadcastsLocked();
            } else {
                Intent providerIntent = new Intent();
                providerIntent.putExtra("providerEnabled", enabled);
                try {
                    this.mPendingIntent.send(LocationManagerService.this.mContext, 0, providerIntent, this, LocationManagerService.this.mHandler, LocationManagerService.this.getResolutionPermission(this.mAllowedResolutionLevel), PendingIntentUtils.createDontSendToRestrictedAppsBundle(null));
                    incrementPendingBroadcastsLocked();
                } catch (PendingIntent.CanceledException e2) {
                    return false;
                }
            }
            return true;
        }

        public void binderDied() {
            if (LocationManagerService.D) {
                Log.d(LocationManagerService.TAG, "Remote " + this.mListenerName + " died.");
            }
            synchronized (LocationManagerService.this.mLock) {
                LocationManagerService.this.removeUpdatesLocked(this);
                clearPendingBroadcastsLocked();
            }
        }

        public void onSendFinished(PendingIntent pendingIntent, Intent intent, int resultCode, String resultData, Bundle resultExtras) {
            synchronized (LocationManagerService.this.mLock) {
                decrementPendingBroadcastsLocked();
            }
        }

        private void incrementPendingBroadcastsLocked() {
            this.mPendingBroadcasts++;
            long identity = Binder.clearCallingIdentity();
            try {
                this.mWakeLock.acquire(60000);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        /* access modifiers changed from: private */
        public void decrementPendingBroadcastsLocked() {
            int i = this.mPendingBroadcasts - 1;
            this.mPendingBroadcasts = i;
            if (i == 0) {
                long identity = Binder.clearCallingIdentity();
                try {
                    if (this.mWakeLock.isHeld()) {
                        this.mWakeLock.release();
                    }
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public void clearPendingBroadcastsLocked() {
            if (this.mPendingBroadcasts > 0) {
                this.mPendingBroadcasts = 0;
                long identity = Binder.clearCallingIdentity();
                try {
                    if (this.mWakeLock.isHeld()) {
                        this.mWakeLock.release();
                    }
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }
    }

    public void locationCallbackFinished(ILocationListener listener) {
        synchronized (this.mLock) {
            Receiver receiver = this.mReceivers.get(listener.asBinder());
            if (receiver != null) {
                receiver.decrementPendingBroadcastsLocked();
            }
        }
    }

    public int getGnssYearOfHardware() {
        GnssLocationProvider.GnssSystemInfoProvider gnssSystemInfoProvider = this.mGnssSystemInfoProvider;
        if (gnssSystemInfoProvider != null) {
            return gnssSystemInfoProvider.getGnssYearOfHardware();
        }
        return 0;
    }

    public String getGnssHardwareModelName() {
        GnssLocationProvider.GnssSystemInfoProvider gnssSystemInfoProvider = this.mGnssSystemInfoProvider;
        if (gnssSystemInfoProvider != null) {
            return gnssSystemInfoProvider.getGnssHardwareModelName();
        }
        return null;
    }

    private boolean hasGnssPermissions(String packageName) {
        boolean checkLocationAccess;
        synchronized (this.mLock) {
            int allowedResolutionLevel = getCallerAllowedResolutionLevel();
            checkResolutionLevelIsSufficientForProviderUseLocked(allowedResolutionLevel, "gps");
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long identity = Binder.clearCallingIdentity();
            try {
                checkLocationAccess = checkLocationAccess(pid, uid, packageName, allowedResolutionLevel);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
        return checkLocationAccess;
    }

    public int getGnssBatchSize(String packageName) {
        GnssBatchingProvider gnssBatchingProvider;
        this.mContext.enforceCallingPermission("android.permission.LOCATION_HARDWARE", "Location Hardware permission not granted to access hardware batching");
        if (!hasGnssPermissions(packageName) || (gnssBatchingProvider = this.mGnssBatchingProvider) == null) {
            return 0;
        }
        return gnssBatchingProvider.getBatchSize();
    }

    public boolean addGnssBatchingCallback(IBatchedLocationCallback callback, String packageName) {
        this.mContext.enforceCallingPermission("android.permission.LOCATION_HARDWARE", "Location Hardware permission not granted to access hardware batching");
        if (!hasGnssPermissions(packageName) || this.mGnssBatchingProvider == null) {
            return false;
        }
        CallerIdentity callerIdentity = new CallerIdentity(Binder.getCallingUid(), Binder.getCallingPid(), packageName);
        synchronized (this.mLock) {
            this.mGnssBatchingCallback = callback;
            this.mGnssBatchingDeathCallback = new LinkedListener<>(callback, "BatchedLocationCallback", callerIdentity, new Consumer() {
                /* class com.android.server.$$Lambda$LocationManagerService$ma_5PjwiFAbM39eIaW8jFG89f1w */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    LocationManagerService.this.lambda$addGnssBatchingCallback$8$LocationManagerService((IBatchedLocationCallback) obj);
                }
            });
            if (!linkToListenerDeathNotificationLocked(callback.asBinder(), this.mGnssBatchingDeathCallback)) {
                return false;
            }
            return true;
        }
    }

    public /* synthetic */ void lambda$addGnssBatchingCallback$8$LocationManagerService(IBatchedLocationCallback listener) {
        stopGnssBatch();
        removeGnssBatchingCallback();
    }

    public void removeGnssBatchingCallback() {
        synchronized (this.mLock) {
            unlinkFromListenerDeathNotificationLocked(this.mGnssBatchingCallback.asBinder(), this.mGnssBatchingDeathCallback);
            this.mGnssBatchingCallback = null;
            this.mGnssBatchingDeathCallback = null;
        }
    }

    public boolean startGnssBatch(long periodNanos, boolean wakeOnFifoFull, String packageName) {
        boolean start;
        this.mContext.enforceCallingPermission("android.permission.LOCATION_HARDWARE", "Location Hardware permission not granted to access hardware batching");
        if (!hasGnssPermissions(packageName) || this.mGnssBatchingProvider == null) {
            return false;
        }
        synchronized (this.mLock) {
            if (this.mGnssBatchingInProgress) {
                Log.e(TAG, "startGnssBatch unexpectedly called w/o stopping prior batch");
                stopGnssBatch();
            }
            this.mGnssBatchingInProgress = true;
            start = this.mGnssBatchingProvider.start(periodNanos, wakeOnFifoFull);
        }
        return start;
    }

    public void flushGnssBatch(String packageName) {
        this.mContext.enforceCallingPermission("android.permission.LOCATION_HARDWARE", "Location Hardware permission not granted to access hardware batching");
        if (!hasGnssPermissions(packageName)) {
            Log.e(TAG, "flushGnssBatch called without GNSS permissions");
            return;
        }
        synchronized (this.mLock) {
            if (!this.mGnssBatchingInProgress) {
                Log.w(TAG, "flushGnssBatch called with no batch in progress");
            }
            if (this.mGnssBatchingProvider != null) {
                this.mGnssBatchingProvider.flush();
            }
        }
    }

    public boolean stopGnssBatch() {
        this.mContext.enforceCallingPermission("android.permission.LOCATION_HARDWARE", "Location Hardware permission not granted to access hardware batching");
        synchronized (this.mLock) {
            if (this.mGnssBatchingProvider == null) {
                return false;
            }
            this.mGnssBatchingInProgress = false;
            boolean stop = this.mGnssBatchingProvider.stop();
            return stop;
        }
    }

    public int getVerboseLoggingLevel() {
        return SystemProperties.getInt(MTKLOGGER_MOBILELOG_DEBUG_PROPERTY, 0);
    }

    public void enableVerboseLogging(int verbose) {
        D = verbose > 0;
        Log.d(TAG, "debug enable:" + D);
        PswServiceFactory.getInstance().getFeature(IPswLbsCustomize.DEFAULT, new Object[]{this.mContext}).setDebug(D);
    }

    public List<String> getInUsePackagesList() {
        ArrayList<String> pkgList = null;
        IPswNavigationStatusController iPswNavigationStatusController = this.mNavigationStatusController;
        if (iPswNavigationStatusController != null) {
            int mode = iPswNavigationStatusController.getNavigateMode();
            if (1 == mode) {
                synchronized (this.mLock) {
                    ArrayList<UpdateRecord> records = this.mRecordsByProvider.get("gps");
                    if (records != null) {
                        pkgList = new ArrayList<>();
                        Iterator<UpdateRecord> it = records.iterator();
                        while (it.hasNext()) {
                            pkgList.add(it.next().mReceiver.mCallerIdentity.mPackageName);
                        }
                    }
                }
            }
            if (D) {
                Log.d(TAG, "PF03:NAV Mode:" + mode + ",pkg:" + pkgList);
            }
        }
        return pkgList;
    }

    @GuardedBy({"mLock"})
    private void addProviderLocked(LocationProvider provider) {
        Preconditions.checkState(getLocationProviderLocked(provider.getName()) == null);
        this.mProviders.add(provider);
        provider.onAllowedChangedLocked();
        provider.onUseableChangedLocked(false);
    }

    @GuardedBy({"mLock"})
    private void removeProviderLocked(LocationProvider provider) {
        if (this.mProviders.remove(provider)) {
            provider.onUseableChangedLocked(false);
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public LocationProvider getLocationProviderLocked(String providerName) {
        Iterator<LocationProvider> it = this.mProviders.iterator();
        while (it.hasNext()) {
            LocationProvider provider = it.next();
            if (providerName.equals(provider.getName())) {
                return provider;
            }
        }
        return null;
    }

    /* access modifiers changed from: private */
    public String getResolutionPermission(int resolutionLevel) {
        if (resolutionLevel == 1) {
            return "android.permission.ACCESS_COARSE_LOCATION";
        }
        if (resolutionLevel != 2) {
            return null;
        }
        return "android.permission.ACCESS_FINE_LOCATION";
    }

    /* access modifiers changed from: private */
    public int getAllowedResolutionLevel(int pid, int uid) {
        if (this.mContext.checkPermission("android.permission.ACCESS_FINE_LOCATION", pid, uid) == 0 && mtkCheckCtaOp(pid, uid, 1)) {
            return 2;
        }
        if (this.mContext.checkPermission("android.permission.ACCESS_COARSE_LOCATION", pid, uid) != 0 || !mtkCheckCtaOp(pid, uid, 0)) {
            return 0;
        }
        return 1;
    }

    private int getCallerAllowedResolutionLevel() {
        return getAllowedResolutionLevel(Binder.getCallingPid(), Binder.getCallingUid());
    }

    private void checkResolutionLevelIsSufficientForGeofenceUse(int allowedResolutionLevel) {
        if (allowedResolutionLevel < 2) {
            throw new SecurityException("Geofence usage requires ACCESS_FINE_LOCATION permission");
        }
    }

    @GuardedBy({"mLock"})
    private int getMinimumResolutionLevelForProviderUseLocked(String provider) {
        ProviderProperties properties;
        if ("gps".equals(provider) || "passive".equals(provider)) {
            return 2;
        }
        if ("network".equals(provider) || "fused".equals(provider)) {
            return 1;
        }
        Iterator<LocationProvider> it = this.mProviders.iterator();
        while (it.hasNext()) {
            LocationProvider lp = it.next();
            if (lp.getName().equals(provider) && (properties = lp.getPropertiesLocked()) != null) {
                if (properties.mRequiresSatellite) {
                    return 2;
                }
                if (properties.mRequiresNetwork || properties.mRequiresCell) {
                    return 1;
                }
            }
        }
        return 2;
    }

    @GuardedBy({"mLock"})
    private void checkResolutionLevelIsSufficientForProviderUseLocked(int allowedResolutionLevel, String providerName) {
        int requiredResolutionLevel = getMinimumResolutionLevelForProviderUseLocked(providerName);
        if (allowedResolutionLevel >= requiredResolutionLevel) {
            return;
        }
        if (requiredResolutionLevel == 1) {
            throw new SecurityException("\"" + providerName + "\" location provider requires ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission.");
        } else if (requiredResolutionLevel != 2) {
            throw new SecurityException("Insufficient permission for \"" + providerName + "\" location provider.");
        } else {
            throw new SecurityException("\"" + providerName + "\" location provider requires ACCESS_FINE_LOCATION permission.");
        }
    }

    public static int resolutionLevelToOp(int allowedResolutionLevel) {
        if (allowedResolutionLevel == 0) {
            return -1;
        }
        if (allowedResolutionLevel == 1) {
            return 0;
        }
        return 1;
    }

    private static String resolutionLevelToOpStr(int allowedResolutionLevel) {
        if (allowedResolutionLevel == 0) {
            return "android:fine_location";
        }
        if (allowedResolutionLevel != 1) {
            return allowedResolutionLevel != 2 ? "android:fine_location" : "android:fine_location";
        }
        return "android:coarse_location";
    }

    private boolean reportLocationAccessNoThrow(int pid, int uid, String packageName, int allowedResolutionLevel) {
        IPswOppoGnssWhiteListProxy iPswOppoGnssWhiteListProxy;
        int op = resolutionLevelToOp(allowedResolutionLevel);
        if (op >= 0) {
            if (op == 1 && (iPswOppoGnssWhiteListProxy = this.mGnssWhiteListProxy) != null && iPswOppoGnssWhiteListProxy.isAllowedPassLocationAccess(packageName)) {
                return true;
            }
            if (this.mAppOps.noteOpNoThrow(op, uid, packageName) != 0) {
                if (D) {
                    Log.d(TAG, "noteOpNoThrow not allowed. op:" + op + " uid:" + uid + " pkg:" + packageName + " Cta:" + isCtaSupported());
                }
                if (!mtkNoteOpForCta(op, pid, uid, packageName, allowedResolutionLevel)) {
                    return false;
                }
            }
        }
        return getAllowedResolutionLevel(pid, uid) >= allowedResolutionLevel;
    }

    private boolean checkLocationAccess(int pid, int uid, String packageName, int allowedResolutionLevel) {
        IPswOppoGnssWhiteListProxy iPswOppoGnssWhiteListProxy;
        int op = resolutionLevelToOp(allowedResolutionLevel);
        if (op >= 0) {
            if (op == 1 && (iPswOppoGnssWhiteListProxy = this.mGnssWhiteListProxy) != null && iPswOppoGnssWhiteListProxy.isAllowedPassLocationAccess(packageName)) {
                return true;
            }
            if (this.mAppOps.checkOp(op, uid, packageName) != 0) {
                if (D) {
                    Log.d(TAG, "checkOp not allowed. op:" + op + " uid:" + uid + " pkg:" + packageName + " Cta:" + isCtaSupported());
                }
                if (!mtkCheckOpForCta(op, pid, uid, packageName, allowedResolutionLevel)) {
                    return false;
                }
            }
        }
        return getAllowedResolutionLevel(pid, uid) >= allowedResolutionLevel;
    }

    public List<String> getAllProviders() {
        ArrayList<String> providers;
        synchronized (this.mLock) {
            providers = new ArrayList<>(this.mProviders.size());
            Iterator<LocationProvider> it = this.mProviders.iterator();
            while (it.hasNext()) {
                String name = it.next().getName();
                if (!"fused".equals(name)) {
                    providers.add(name);
                }
            }
        }
        return providers;
    }

    public List<String> getProviders(Criteria criteria, boolean enabledOnly) {
        ArrayList<String> providers;
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(24, Binder.getCallingUid());
        int allowedResolutionLevel = getCallerAllowedResolutionLevel();
        synchronized (this.mLock) {
            providers = new ArrayList<>(this.mProviders.size());
            Iterator<LocationProvider> it = this.mProviders.iterator();
            while (it.hasNext()) {
                LocationProvider provider = it.next();
                String name = provider.getName();
                if (!"fused".equals(name)) {
                    if (allowedResolutionLevel >= getMinimumResolutionLevelForProviderUseLocked(name)) {
                        if (!enabledOnly || provider.isUseableLocked()) {
                            if (criteria == null || android.location.LocationProvider.propertiesMeetCriteria(name, provider.getPropertiesLocked(), criteria)) {
                                providers.add(name);
                            }
                        }
                    }
                }
            }
        }
        return providers;
    }

    public String getBestProvider(Criteria criteria, boolean enabledOnly) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(25, Binder.getCallingUid());
        List<String> providers = getProviders(criteria, enabledOnly);
        if (providers.isEmpty()) {
            providers = getProviders(null, enabledOnly);
        }
        if (providers.isEmpty()) {
            return null;
        }
        if (providers.contains("gps")) {
            return "gps";
        }
        if (providers.contains("network")) {
            return "network";
        }
        return providers.get(0);
    }

    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public void updateProviderUseableLocked(LocationProvider provider) {
        boolean useable = provider.isUseableLocked();
        IPswOppoGnssWhiteListProxy iPswOppoGnssWhiteListProxy = this.mGnssWhiteListProxy;
        if (iPswOppoGnssWhiteListProxy != null) {
            useable = iPswOppoGnssWhiteListProxy.isNetworkUseablechanged(provider.getName(), useable);
        }
        ArrayList<Receiver> deadReceivers = null;
        ArrayList<UpdateRecord> records = this.mRecordsByProvider.get(provider.getName());
        if (records != null) {
            Iterator<UpdateRecord> it = records.iterator();
            while (it.hasNext()) {
                UpdateRecord record = it.next();
                if (isCurrentProfileLocked(UserHandle.getUserId(record.mReceiver.mCallerIdentity.mUid)) && !isSettingsExemptLocked(record) && !record.mReceiver.callProviderEnabledLocked(provider.getName(), useable)) {
                    if (deadReceivers == null) {
                        deadReceivers = new ArrayList<>();
                    }
                    deadReceivers.add(record.mReceiver);
                }
            }
        }
        if (deadReceivers != null) {
            for (int i = deadReceivers.size() - 1; i >= 0; i--) {
                removeUpdatesLocked(deadReceivers.get(i));
            }
        }
        applyRequirementsLocked(provider);
    }

    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public void applyRequirementsLocked(String providerName) {
        LocationProvider provider = getLocationProviderLocked(providerName);
        if (provider != null) {
            applyRequirementsLocked(provider);
        }
    }

    /* JADX INFO: finally extract failed */
    @GuardedBy({"mLock"})
    private void applyRequirementsLocked(LocationProvider provider) {
        WorkSource worksource;
        WorkSource worksource2;
        WorkSource worksource3;
        long identity;
        boolean shouldThrottleRequests;
        boolean isForegroundOnlyMode;
        boolean z;
        WorkSource worksource4;
        ArrayList<UpdateRecord> records = this.mRecordsByProvider.get(provider.getName());
        WorkSource worksource5 = new WorkSource();
        ProviderRequest providerRequest = new ProviderRequest();
        if (!this.mProviders.contains(provider) || records == null || records.isEmpty()) {
            worksource = worksource5;
        } else {
            long identity2 = Binder.clearCallingIdentity();
            try {
                long backgroundThrottleInterval = Settings.Global.getLong(this.mContext.getContentResolver(), "location_background_throttle_interval_ms", 1800000);
                Binder.restoreCallingIdentity(identity2);
                boolean isForegroundOnlyMode2 = this.mBatterySaverMode == 3;
                boolean shouldThrottleRequests2 = this.mBatterySaverMode == 4 && !this.mPowerManager.isInteractive();
                providerRequest.lowPowerMode = true;
                Iterator<UpdateRecord> it = records.iterator();
                while (it.hasNext()) {
                    UpdateRecord record = it.next();
                    if (isCurrentProfileLocked(UserHandle.getUserId(record.mReceiver.mCallerIdentity.mUid))) {
                        if (!checkLocationAccess(record.mReceiver.mCallerIdentity.mPid, record.mReceiver.mCallerIdentity.mUid, record.mReceiver.mCallerIdentity.mPackageName, record.mReceiver.mAllowedResolutionLevel)) {
                            it = it;
                        } else {
                            boolean isBatterySaverDisablingLocation = shouldThrottleRequests2 || (isForegroundOnlyMode2 && !record.mIsForegroundUid);
                            if (!provider.isUseableLocked() || isBatterySaverDisablingLocation) {
                                isForegroundOnlyMode = isForegroundOnlyMode2;
                                shouldThrottleRequests = shouldThrottleRequests2;
                                identity = identity2;
                                if (isSettingsExemptLocked(record)) {
                                    worksource3 = worksource5;
                                } else if (provider.isNetworkWhiteList(record.mReceiver.mCallerIdentity.mPackageName)) {
                                    worksource3 = worksource5;
                                } else {
                                    if ("network".equals(provider.getName())) {
                                        worksource4 = worksource5;
                                        if (this.mOppoLbsRepairer.isForegroundActivity(this.mActivityManager.getPackageImportance(record.mReceiver.mCallerIdentity.mPackageName))) {
                                            provider.transProviderStatusToMonitor(true);
                                        }
                                    } else {
                                        worksource4 = worksource5;
                                    }
                                    Log.d(TAG, "Skip applyRequirementsLocked, provider: " + provider.getName() + " provider.isUseableLocked(): " + provider.isUseableLocked() + " isBatterySaverDisablingLocation: " + isBatterySaverDisablingLocation + " isSettingsExemptLocked(record): " + isSettingsExemptLocked(record) + " record: " + record);
                                    isForegroundOnlyMode2 = isForegroundOnlyMode;
                                    it = it;
                                    shouldThrottleRequests2 = shouldThrottleRequests;
                                    identity2 = identity;
                                    worksource5 = worksource4;
                                }
                                providerRequest.locationSettingsIgnored = true;
                                providerRequest.lowPowerMode = false;
                                Log.d(TAG, "applyRequirementsLocked for SettingsExempt case, provider: " + provider.getName() + " provider.isUseableLocked(): " + provider.isUseableLocked() + " isBatterySaverDisablingLocation: " + isBatterySaverDisablingLocation + " isSettingsExemptLocked(record): " + isSettingsExemptLocked(record) + " record: " + record);
                            } else {
                                isForegroundOnlyMode = isForegroundOnlyMode2;
                                worksource3 = worksource5;
                                identity = identity2;
                                shouldThrottleRequests = shouldThrottleRequests2;
                            }
                            if (provider.getName().equalsIgnoreCase("gps")) {
                                String checkPackageName = record.mReceiver.mCallerIdentity.mPackageName;
                                boolean isBlocked = this.mOppoLocationBlacklistUtil.isPackageBlocked(checkPackageName, provider.getName());
                                if (isBlocked) {
                                    IPswLbsRepairer iPswLbsRepairer = this.mOppoLbsRepairer;
                                    if (iPswLbsRepairer != null && iPswLbsRepairer.isForegroundActivity(this.mActivityManager.getPackageImportance(record.mReceiver.mCallerIdentity.mPackageName))) {
                                        record.mRealRequest.setInterval(2147483647L);
                                        record.mRequest.setInterval(2147483647L);
                                    }
                                } else {
                                    record.mRequest.setInterval(record.mRealRequest.getInterval());
                                }
                                if (this.mOppoLocationBlacklistUtil.needChangeNotifyStatus(checkPackageName, isBlocked)) {
                                    record.mReceiver.updateMonitoring(!isBlocked);
                                }
                                if (isBlocked) {
                                    isForegroundOnlyMode2 = isForegroundOnlyMode;
                                    it = it;
                                    shouldThrottleRequests2 = shouldThrottleRequests;
                                    identity2 = identity;
                                    worksource5 = worksource3;
                                }
                            }
                            LocationRequest locationRequest = record.mRealRequest;
                            long interval = locationRequest.getInterval();
                            if (!providerRequest.locationSettingsIgnored && !isThrottlingExemptLocked(record.mReceiver.mCallerIdentity)) {
                                if (!record.mIsForegroundUid) {
                                    interval = Math.max(interval, backgroundThrottleInterval);
                                }
                                if (interval != locationRequest.getInterval()) {
                                    locationRequest = new LocationRequest(locationRequest);
                                    locationRequest.setInterval(interval);
                                }
                            }
                            record.mRequest = locationRequest;
                            providerRequest.locationRequests.add(locationRequest);
                            if (!locationRequest.isLowPowerMode()) {
                                providerRequest.lowPowerMode = false;
                            }
                            if (interval < providerRequest.interval) {
                                z = true;
                                providerRequest.reportLocation = true;
                                providerRequest.interval = interval;
                            } else {
                                z = true;
                            }
                            isForegroundOnlyMode2 = isForegroundOnlyMode;
                            it = it;
                            shouldThrottleRequests2 = shouldThrottleRequests;
                            identity2 = identity;
                            worksource5 = worksource3;
                        }
                    }
                }
                WorkSource worksource6 = worksource5;
                if (providerRequest.reportLocation) {
                    long thresholdInterval = ((providerRequest.interval + 1000) * 3) / 2;
                    Iterator<UpdateRecord> it2 = records.iterator();
                    while (it2.hasNext()) {
                        UpdateRecord record2 = it2.next();
                        if (isCurrentProfileLocked(UserHandle.getUserId(record2.mReceiver.mCallerIdentity.mUid))) {
                            LocationRequest locationRequest2 = record2.mRequest;
                            if (providerRequest.locationRequests.contains(locationRequest2)) {
                                if (locationRequest2.getInterval() <= thresholdInterval) {
                                    if (record2.mReceiver.mWorkSource == null) {
                                        worksource2 = worksource6;
                                    } else if (isValidWorkSource(record2.mReceiver.mWorkSource)) {
                                        worksource2 = worksource6;
                                        worksource2.add(record2.mReceiver.mWorkSource);
                                    } else {
                                        worksource2 = worksource6;
                                    }
                                    worksource2.add(record2.mReceiver.mCallerIdentity.mUid, record2.mReceiver.mCallerIdentity.mPackageName);
                                } else {
                                    worksource2 = worksource6;
                                }
                            }
                        } else {
                            worksource2 = worksource6;
                        }
                        worksource6 = worksource2;
                    }
                    worksource = worksource6;
                } else {
                    worksource = worksource6;
                }
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(identity2);
                throw th;
            }
        }
        provider.setRequestLocked(providerRequest, worksource);
    }

    private static boolean isValidWorkSource(WorkSource workSource) {
        if (workSource.size() > 0) {
            return workSource.getName(0) != null;
        }
        ArrayList<WorkSource.WorkChain> workChains = workSource.getWorkChains();
        return (workChains == null || workChains.isEmpty() || workChains.get(0).getAttributionTag() == null) ? false : true;
    }

    public String[] getBackgroundThrottlingWhitelist() {
        String[] strArr;
        synchronized (this.mLock) {
            strArr = (String[]) this.mBackgroundThrottlePackageWhitelist.toArray(new String[0]);
        }
        return strArr;
    }

    public String[] getIgnoreSettingsWhitelist() {
        String[] strArr;
        synchronized (this.mLock) {
            strArr = (String[]) this.mIgnoreSettingsPackageWhitelist.toArray(new String[0]);
        }
        return strArr;
    }

    @GuardedBy({"mLock"})
    private boolean isThrottlingExemptLocked(CallerIdentity callerIdentity) {
        if (callerIdentity.mUid != 1000 && !this.mBackgroundThrottlePackageWhitelist.contains(callerIdentity.mPackageName)) {
            return isProviderPackage(callerIdentity.mPackageName);
        }
        return true;
    }

    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public boolean isSettingsExemptLocked(UpdateRecord record) {
        if (!record.mRealRequest.isLocationSettingsIgnored()) {
            return false;
        }
        if (this.mIgnoreSettingsPackageWhitelist.contains(record.mReceiver.mCallerIdentity.mPackageName)) {
            return true;
        }
        return isProviderPackage(record.mReceiver.mCallerIdentity.mPackageName);
    }

    /* access modifiers changed from: private */
    public class UpdateRecord {
        /* access modifiers changed from: private */
        public boolean mIsForegroundUid;
        /* access modifiers changed from: private */
        public Location mLastFixBroadcast;
        /* access modifiers changed from: private */
        public long mLastStatusBroadcast;
        final String mProvider;
        /* access modifiers changed from: private */
        public final LocationRequest mRealRequest;
        /* access modifiers changed from: private */
        public final Receiver mReceiver;
        LocationRequest mRequest;
        private Throwable mStackTrace;

        private UpdateRecord(String provider, LocationRequest request, Receiver receiver) {
            ArrayList<UpdateRecord> records;
            this.mProvider = provider;
            this.mRealRequest = request;
            this.mRequest = request;
            this.mReceiver = receiver;
            this.mIsForegroundUid = LocationManagerService.isImportanceForeground(LocationManagerService.this.mActivityManager.getPackageImportance(this.mReceiver.mCallerIdentity.mPackageName));
            if (LocationManagerService.D && receiver.mCallerIdentity.mPid == Process.myPid()) {
                this.mStackTrace = new Throwable();
            }
            ArrayList<UpdateRecord> records2 = (ArrayList) LocationManagerService.this.mRecordsByProvider.get(provider);
            if (records2 == null) {
                ArrayList<UpdateRecord> records3 = new ArrayList<>();
                LocationManagerService.this.mRecordsByProvider.put(provider, records3);
                records = records3;
            } else {
                records = records2;
            }
            if (!records.contains(this)) {
                records.add(this);
            }
            LocationManagerService.this.mRequestStatistics.startRequesting(this.mReceiver.mCallerIdentity.mPackageName, provider, request.getInterval(), this.mIsForegroundUid);
            LocationManagerService.this.mOppoLocationStatistics.startRequesting(this.mReceiver.mCallerIdentity.mPackageName, provider, request.getInterval(), this.mIsForegroundUid, Integer.toHexString(System.identityHashCode(this.mReceiver)));
            if (provider.equals("gps")) {
                LocationManagerService.this.mOppoLocationBlacklistUtil.recordPackagesLocationStatus(this.mReceiver.mCallerIdentity.mPackageName, this.mReceiver.mCallerIdentity.mUid, this.mReceiver.mCallerIdentity.mPid, provider);
            }
        }

        /* access modifiers changed from: private */
        public void updateForeground(boolean isForeground) {
            this.mIsForegroundUid = isForeground;
            LocationManagerService.this.mRequestStatistics.updateForeground(this.mReceiver.mCallerIdentity.mPackageName, this.mProvider, isForeground);
        }

        /* access modifiers changed from: private */
        public void disposeLocked(boolean removeReceiver) {
            LocationRequestStatistics.PackageStatistics stats;
            String packageName = this.mReceiver.mCallerIdentity.mPackageName;
            LocationManagerService.this.mRequestStatistics.stopRequesting(packageName, this.mProvider);
            LocationManagerService.this.mOppoLocationStatistics.stopRequesting(packageName, this.mProvider, Integer.toHexString(System.identityHashCode(this.mReceiver)));
            if (LocationManagerService.this.mOppoGnssDuration != null && "gps".equals(this.mProvider) && (stats = LocationManagerService.this.mRequestStatistics.statistics.get(new LocationRequestStatistics.PackageProviderKey(this.mReceiver.mCallerIdentity.mPackageName, this.mProvider))) != null && LocationManagerService.this.mOppoGnssDuration.isFeedBackGnssDuration(stats.getLastDurationMs(), stats.isRecord(), this.mReceiver.mCallerIdentity.mPackageName)) {
                stats.setRecord(true);
            }
            LocationManagerService.this.mLocationUsageLogger.logLocationApiUsage(1, 1, packageName, this.mRealRequest, this.mReceiver.isListener(), this.mReceiver.isPendingIntent(), null, LocationManagerService.this.mActivityManager.getPackageImportance(packageName));
            if ("gps".equals(this.mProvider)) {
                LocationManagerService.this.mOppoLocationBlacklistUtil.removePackagesLocationStatus(this.mReceiver.mCallerIdentity.mPackageName, this.mReceiver.mCallerIdentity.mUid, this.mReceiver.mCallerIdentity.mPid, this.mProvider);
            }
            ArrayList<UpdateRecord> globalRecords = (ArrayList) LocationManagerService.this.mRecordsByProvider.get(this.mProvider);
            if (globalRecords != null) {
                globalRecords.remove(this);
            }
            if (removeReceiver) {
                HashMap<String, UpdateRecord> receiverRecords = this.mReceiver.mUpdateRecords;
                receiverRecords.remove(this.mProvider);
                if (receiverRecords.size() == 0) {
                    LocationManagerService.this.removeUpdatesLocked(this.mReceiver);
                }
            }
        }

        public String toString() {
            StringBuilder b = new StringBuilder("UpdateRecord[");
            b.append(this.mProvider);
            b.append(StringUtils.SPACE);
            b.append(this.mReceiver.mCallerIdentity.mPackageName);
            b.append("(");
            b.append(this.mReceiver.mCallerIdentity.mUid);
            if (this.mIsForegroundUid) {
                b.append(" foreground");
            } else {
                b.append(" background");
            }
            b.append(") ");
            b.append(this.mRealRequest);
            b.append(StringUtils.SPACE);
            b.append(this.mReceiver.mWorkSource);
            if (this.mStackTrace != null) {
                ByteArrayOutputStream tmp = new ByteArrayOutputStream();
                this.mStackTrace.printStackTrace(new PrintStream(tmp));
                b.append("\n\n");
                b.append(tmp.toString());
                b.append(StringUtils.LF);
            }
            b.append("]");
            return b.toString();
        }
    }

    @GuardedBy({"mLock"})
    private Receiver getReceiverLocked(ILocationListener listener, int pid, int uid, String packageName, WorkSource workSource, boolean hideFromAppOps) {
        IBinder binder = listener.asBinder();
        Receiver receiver = this.mReceivers.get(binder);
        if (receiver == null) {
            receiver = new Receiver(listener, null, pid, uid, packageName, workSource, hideFromAppOps);
            if (!linkToListenerDeathNotificationLocked(receiver.getListener().asBinder(), receiver)) {
                return null;
            }
            this.mReceivers.put(binder, receiver);
        }
        return receiver;
    }

    @GuardedBy({"mLock"})
    private Receiver getReceiverLocked(PendingIntent intent, int pid, int uid, String packageName, WorkSource workSource, boolean hideFromAppOps) {
        Receiver receiver = this.mReceivers.get(intent);
        if (receiver != null) {
            return receiver;
        }
        Receiver receiver2 = new Receiver(null, intent, pid, uid, packageName, workSource, hideFromAppOps);
        this.mReceivers.put(intent, receiver2);
        return receiver2;
    }

    private LocationRequest createSanitizedRequest(LocationRequest request, int resolutionLevel, boolean callerHasLocationHardwarePermission) {
        LocationRequest sanitizedRequest = new LocationRequest(request);
        if (!callerHasLocationHardwarePermission) {
            sanitizedRequest.setLowPowerMode(false);
        }
        if (resolutionLevel < 2) {
            int quality = sanitizedRequest.getQuality();
            if (quality == 100) {
                sanitizedRequest.setQuality(102);
            } else if (quality == 203) {
                sanitizedRequest.setQuality(201);
            }
            if (sanitizedRequest.getInterval() < 600000) {
                sanitizedRequest.setInterval(600000);
            }
            if (sanitizedRequest.getFastestInterval() < 600000) {
                sanitizedRequest.setFastestInterval(600000);
            }
        }
        if (sanitizedRequest.getFastestInterval() > sanitizedRequest.getInterval()) {
            sanitizedRequest.setFastestInterval(request.getInterval());
        }
        return sanitizedRequest;
    }

    private void checkPackageName(String packageName) {
        if (packageName != null) {
            int uid = Binder.getCallingUid();
            String[] packages = this.mPackageManager.getPackagesForUid(uid);
            if (packages != null) {
                int length = packages.length;
                int i = 0;
                while (i < length) {
                    if (!packageName.equals(packages[i])) {
                        i++;
                    } else {
                        return;
                    }
                }
                throw new SecurityException("invalid package name: " + packageName);
            }
            throw new SecurityException("invalid UID " + uid);
        }
        throw new SecurityException("invalid package name: " + ((Object) null));
    }

    /* JADX WARN: Failed to insert an additional move for type inference into block B:14:0x0045 */
    /* JADX WARN: Failed to insert an additional move for type inference into block B:35:0x00cc */
    /* JADX WARN: Failed to insert an additional move for type inference into block B:71:0x017e */
    /* JADX WARN: Failed to insert an additional move for type inference into block B:68:0x0159 */
    /* JADX WARN: Failed to insert an additional move for type inference into block B:64:0x0146 */
    /* JADX DEBUG: Additional 1 move instruction added to help type inference */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v6, resolved type: com.android.server.PswServiceFactory} */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r4v0 */
    /* JADX WARN: Type inference failed for: r4v3, types: [java.lang.Object[]] */
    /* JADX WARN: Type inference failed for: r4v7, types: [long] */
    /* JADX WARN: Type inference failed for: r4v9 */
    /* JADX WARN: Type inference failed for: r4v10 */
    /* JADX WARN: Type inference failed for: r4v11 */
    /* JADX WARN: Type inference failed for: r4v12, types: [int] */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x0184, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x01a6, code lost:
        r0 = th;
        r4 = r4;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    public void requestLocationUpdates(LocationRequest request, ILocationListener listener, PendingIntent intent, String packageName) {
        Object obj;
        int pid;
        int uid;
        Receiver receiver;
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(26, Binder.getCallingUid());
        Object obj2 = this.mLock;
        synchronized (obj2) {
            try {
                PswServiceFactory instance = PswServiceFactory.getInstance();
                IPswLbsCustomize iPswLbsCustomize = IPswLbsCustomize.DEFAULT;
                ? r4 = {this.mContext};
                if (instance.getFeature(iPswLbsCustomize, r4).isForceGnssDisabled()) {
                    try {
                    } catch (Throwable th) {
                        th = th;
                        obj = obj2;
                        throw th;
                    }
                } else {
                    LocationRequest request2 = request == null ? DEFAULT_LOCATION_REQUEST : request;
                    try {
                        checkPackageName(packageName);
                        int allowedResolutionLevel = getCallerAllowedResolutionLevel();
                        checkResolutionLevelIsSufficientForProviderUseLocked(allowedResolutionLevel, request2.getProvider());
                        WorkSource workSource = request2.getWorkSource();
                        if (workSource != null) {
                            try {
                                if (!workSource.isEmpty()) {
                                    this.mContext.enforceCallingOrSelfPermission("android.permission.UPDATE_DEVICE_STATS", null);
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                obj = obj2;
                                throw th;
                            }
                        }
                        boolean hideFromAppOps = request2.getHideFromAppOps();
                        if (hideFromAppOps) {
                            this.mContext.enforceCallingOrSelfPermission("android.permission.UPDATE_APP_OPS_STATS", null);
                        }
                        if (request2.isLocationSettingsIgnored()) {
                            this.mContext.enforceCallingOrSelfPermission("android.permission.WRITE_SECURE_SETTINGS", null);
                        }
                        LocationRequest sanitizedRequest = createSanitizedRequest(request2, allowedResolutionLevel, this.mContext.checkCallingPermission("android.permission.LOCATION_HARDWARE") == 0);
                        int pid2 = Binder.getCallingPid();
                        int uid2 = Binder.getCallingUid();
                        PswServiceFactory.getInstance().getFeature(IPswLbsCustomize.DEFAULT, new Object[]{this.mContext}).getAppInfoForTr("requestLocationUpdates()", request2.getProvider(), pid2, packageName);
                        r4 = Binder.clearCallingIdentity();
                        try {
                            checkLocationAccess(pid2, uid2, packageName, allowedResolutionLevel);
                            if (intent == null && listener == null) {
                                try {
                                    throw new IllegalArgumentException("need either listener or intent");
                                } catch (Throwable th3) {
                                    th = th3;
                                    Binder.restoreCallingIdentity(r4);
                                    throw th;
                                }
                            } else {
                                if (intent != null) {
                                    if (listener != null) {
                                        throw new IllegalArgumentException("cannot register both listener and intent");
                                    }
                                }
                                this.mLocationUsageLogger.logLocationApiUsage(0, 1, packageName, request2, listener != null, intent != null, null, this.mActivityManager.getPackageImportance(packageName));
                                if (intent != null) {
                                    uid = uid2;
                                    pid = pid2;
                                    try {
                                        receiver = getReceiverLocked(intent, pid2, uid2, packageName, workSource, hideFromAppOps);
                                    } catch (Throwable th4) {
                                        th = th4;
                                    }
                                } else {
                                    uid = uid2;
                                    pid = pid2;
                                    r4 = uid;
                                    try {
                                        receiver = getReceiverLocked(listener, pid, (int) r4, packageName, workSource, hideFromAppOps);
                                    } catch (Throwable th5) {
                                        th = th5;
                                        Binder.restoreCallingIdentity(r4);
                                        throw th;
                                    }
                                }
                                if (receiver != null) {
                                    r4 = obj2;
                                    requestLocationUpdatesLocked(sanitizedRequest, receiver, pid, uid, packageName);
                                } else {
                                    r4 = obj2;
                                    Log.e(TAG, "request from " + packageName + " failed.");
                                }
                                Binder.restoreCallingIdentity(r4);
                            }
                        } catch (Throwable th6) {
                            th = th6;
                            Binder.restoreCallingIdentity(r4);
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        obj = obj2;
                        throw th;
                    }
                }
            } catch (Throwable th8) {
                th = th8;
                obj = obj2;
                throw th;
            }
        }
    }

    @GuardedBy({"mLock"})
    private void requestLocationUpdatesLocked(LocationRequest request, Receiver receiver, int pid, int uid, String packageName) {
        Location loc;
        LocationRequest request2 = request == null ? DEFAULT_LOCATION_REQUEST : request;
        String name = request2.getProvider();
        if (name != null) {
            LocationProvider provider = getLocationProviderLocked(name);
            if (provider == null) {
                this.mHandler.post(new Runnable(name) {
                    /* class com.android.server.$$Lambda$LocationManagerService$WXzFz_q0NY_AzxN3Ug3cWlIDZek */
                    private final /* synthetic */ String f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        LocationManagerService.this.lambda$requestLocationUpdatesLocked$9$LocationManagerService(this.f$1);
                    }
                });
                if ("network".equals(name) && this.mNlpProxy != null) {
                    LocationProvider networkProviderManager = new LocationProvider("network", true);
                    LocationProviderProxy networkProvider = this.mNlpProxy.getActiveNpProxy();
                    if (networkProvider != null) {
                        this.mRealProviders.add(networkProviderManager);
                        addProviderLocked(networkProviderManager);
                        networkProviderManager.attachLocked(networkProvider);
                    }
                }
                throw new IllegalArgumentException("provider doesn't exist: " + name);
            }
            UpdateRecord record = new UpdateRecord(name, request2, receiver);
            StringBuilder sb = new StringBuilder();
            sb.append("request ");
            sb.append(Integer.toHexString(System.identityHashCode(receiver)));
            sb.append(StringUtils.SPACE);
            sb.append(name);
            sb.append(StringUtils.SPACE);
            sb.append(request2);
            sb.append(" from ");
            sb.append(packageName);
            sb.append("(");
            sb.append(uid);
            sb.append(StringUtils.SPACE);
            sb.append(record.mIsForegroundUid ? "foreground" : "background");
            sb.append(isThrottlingExemptLocked(receiver.mCallerIdentity) ? " [whitelisted]" : "");
            sb.append(")");
            Log.d(TAG, sb.toString());
            Log.d(TAG, "Location Settings Mode isEnabled = " + isLocationEnabled());
            UpdateRecord oldRecord = receiver.mUpdateRecords.put(name, record);
            if (oldRecord != null) {
                oldRecord.disposeLocked(false);
            }
            if (!provider.isUseableLocked() && !isSettingsExemptLocked(record) && !provider.isNetworkWhiteList(packageName)) {
                boolean unused = receiver.callProviderEnabledLocked(name, false);
            }
            if (provider.isUseableLocked() && name.equals("network") && (loc = this.mFastNetworkLocation.getValidLocation()) != null) {
                provider.onReportLocation(loc);
            }
            mtkPrintCtaLog(pid, uid, "requestLocationUpdates", "USE_LOCATION", name);
            applyRequirementsLocked(name);
            receiver.updateMonitoring(true);
            return;
        }
        throw new IllegalArgumentException("provider name must not be null");
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.android.server.LocationManagerService.getReceiverLocked(android.app.PendingIntent, int, int, java.lang.String, android.os.WorkSource, boolean):com.android.server.LocationManagerService$Receiver
     arg types: [android.app.PendingIntent, int, int, java.lang.String, ?[OBJECT, ARRAY], int]
     candidates:
      com.android.server.LocationManagerService.getReceiverLocked(android.location.ILocationListener, int, int, java.lang.String, android.os.WorkSource, boolean):com.android.server.LocationManagerService$Receiver
      com.android.server.LocationManagerService.getReceiverLocked(android.app.PendingIntent, int, int, java.lang.String, android.os.WorkSource, boolean):com.android.server.LocationManagerService$Receiver */
    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.android.server.LocationManagerService.getReceiverLocked(android.location.ILocationListener, int, int, java.lang.String, android.os.WorkSource, boolean):com.android.server.LocationManagerService$Receiver
     arg types: [android.location.ILocationListener, int, int, java.lang.String, ?[OBJECT, ARRAY], int]
     candidates:
      com.android.server.LocationManagerService.getReceiverLocked(android.app.PendingIntent, int, int, java.lang.String, android.os.WorkSource, boolean):com.android.server.LocationManagerService$Receiver
      com.android.server.LocationManagerService.getReceiverLocked(android.location.ILocationListener, int, int, java.lang.String, android.os.WorkSource, boolean):com.android.server.LocationManagerService$Receiver */
    public void removeUpdates(ILocationListener listener, PendingIntent intent, String packageName) {
        Receiver receiver;
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(27, Binder.getCallingUid());
        checkPackageName(packageName);
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        if (intent == null && listener == null) {
            throw new IllegalArgumentException("need either listener or intent");
        } else if (intent == null || listener == null) {
            synchronized (this.mLock) {
                if (intent != null) {
                    receiver = getReceiverLocked(intent, pid, uid, packageName, (WorkSource) null, false);
                } else {
                    receiver = getReceiverLocked(listener, pid, uid, packageName, (WorkSource) null, false);
                }
                long identity = Binder.clearCallingIdentity();
                try {
                    removeUpdatesLocked(receiver);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        } else {
            throw new IllegalArgumentException("cannot register both listener and intent");
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public void removeUpdatesLocked(Receiver receiver) {
        Log.i(TAG, "remove " + Integer.toHexString(System.identityHashCode(receiver)));
        if (this.mReceivers.remove(receiver.mKey) != null && receiver.isListener()) {
            unlinkFromListenerDeathNotificationLocked(receiver.getListener().asBinder(), receiver);
            receiver.clearPendingBroadcastsLocked();
        }
        receiver.updateMonitoring(false);
        HashSet<String> providers = new HashSet<>();
        HashMap<String, UpdateRecord> oldRecords = receiver.mUpdateRecords;
        if (oldRecords != null) {
            for (UpdateRecord record : oldRecords.values()) {
                record.disposeLocked(false);
            }
            providers.addAll(oldRecords.keySet());
        }
        Iterator<String> it = providers.iterator();
        while (it.hasNext()) {
            applyRequirementsLocked(it.next());
        }
    }

    public Location getLastLocation(LocationRequest r, String packageName) {
        Location location;
        synchronized (this.mLock) {
            LocationRequest request = r != null ? r : DEFAULT_LOCATION_REQUEST;
            int allowedResolutionLevel = getCallerAllowedResolutionLevel();
            checkPackageName(packageName);
            checkResolutionLevelIsSufficientForProviderUseLocked(allowedResolutionLevel, request.getProvider());
            int pid = Binder.getCallingPid();
            int uid = Binder.getCallingUid();
            long identity = Binder.clearCallingIdentity();
            try {
                if (this.mBlacklist.isBlacklisted(packageName)) {
                    try {
                        if (D) {
                            Log.d(TAG, "not returning last loc for blacklisted app: " + packageName);
                        }
                        Binder.restoreCallingIdentity(identity);
                        return null;
                    } catch (Throwable th) {
                        th = th;
                        Binder.restoreCallingIdentity(identity);
                        throw th;
                    }
                } else {
                    String name = request.getProvider();
                    if (name == null) {
                        name = "fused";
                    }
                    LocationProvider provider = getLocationProviderLocked(name);
                    if (provider == null) {
                        Binder.restoreCallingIdentity(identity);
                        return null;
                    } else if (!isCurrentProfileLocked(UserHandle.getUserId(uid)) && !isProviderPackage(packageName)) {
                        Binder.restoreCallingIdentity(identity);
                        return null;
                    } else if (!provider.isUseableLocked()) {
                        Binder.restoreCallingIdentity(identity);
                        return null;
                    } else {
                        if (allowedResolutionLevel < 2) {
                            location = this.mLastLocationCoarseInterval.get(name);
                        } else {
                            location = this.mLastLocation.get(name);
                        }
                        if (location == null) {
                            Binder.restoreCallingIdentity(identity);
                            return null;
                        }
                        try {
                            mtkPrintCtaLog(pid, uid, "getLastLocation", "READ_LOCATION_INFO", name);
                            String op = resolutionLevelToOpStr(allowedResolutionLevel);
                            try {
                                if (SystemClock.elapsedRealtime() - (location.getElapsedRealtimeNanos() / NANOS_PER_MILLI) <= Settings.Global.getLong(this.mContext.getContentResolver(), "location_last_location_max_age_millis", DEFAULT_LAST_LOCATION_MAX_AGE_MS) || this.mAppOps.unsafeCheckOp(op, uid, packageName) != 4) {
                                    Location lastLocation = null;
                                    if (allowedResolutionLevel < 2) {
                                        Location noGPSLocation = location.getExtraLocation("noGPSLocation");
                                        if (noGPSLocation != null) {
                                            lastLocation = new Location(this.mLocationFudger.getOrCreate(noGPSLocation));
                                        }
                                    } else {
                                        lastLocation = new Location(location);
                                    }
                                    if (lastLocation != null && !reportLocationAccessNoThrow(pid, uid, packageName, allowedResolutionLevel)) {
                                        if (D) {
                                            Log.d(TAG, "not returning last loc for no op app: " + packageName);
                                        }
                                        lastLocation = null;
                                    }
                                    Binder.restoreCallingIdentity(identity);
                                    return lastLocation;
                                }
                                Binder.restoreCallingIdentity(identity);
                                return null;
                            } catch (Throwable th2) {
                                th = th2;
                                Binder.restoreCallingIdentity(identity);
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            Binder.restoreCallingIdentity(identity);
                            throw th;
                        }
                    }
                }
            } catch (Throwable th4) {
                th = th4;
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        }
    }

    public LocationTime getGnssTimeMillis() {
        synchronized (this.mLock) {
            Location location = this.mLastLocation.get("gps");
            if (location == null) {
                return null;
            }
            long currentNanos = SystemClock.elapsedRealtimeNanos();
            LocationTime locationTime = new LocationTime(location.getTime() + ((currentNanos - location.getElapsedRealtimeNanos()) / NANOS_PER_MILLI), currentNanos);
            return locationTime;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x004f, code lost:
        return false;
     */
    public boolean injectLocation(Location location) {
        this.mContext.enforceCallingPermission("android.permission.LOCATION_HARDWARE", "Location Hardware permission not granted to inject location");
        this.mContext.enforceCallingPermission("android.permission.ACCESS_FINE_LOCATION", "Access Fine Location permission not granted to inject Location");
        if (location == null) {
            if (D) {
                Log.d(TAG, "injectLocation(): called with null location");
            }
            return false;
        }
        synchronized (this.mLock) {
            LocationProvider provider = getLocationProviderLocked(location.getProvider());
            if (provider != null) {
                if (provider.isUseableLocked()) {
                    if (this.mLastLocation.get(provider.getName()) != null) {
                        return false;
                    }
                    updateLastLocationLocked(location, provider.getName());
                    return true;
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00b8, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00c8, code lost:
        r0 = th;
     */
    public void requestGeofence(LocationRequest request, Geofence geofence, PendingIntent intent, String packageName) {
        LocationRequest request2 = request == null ? DEFAULT_LOCATION_REQUEST : request;
        int allowedResolutionLevel = getCallerAllowedResolutionLevel();
        checkResolutionLevelIsSufficientForGeofenceUse(allowedResolutionLevel);
        if (intent != null) {
            checkPackageName(packageName);
            synchronized (this.mLock) {
                checkResolutionLevelIsSufficientForProviderUseLocked(allowedResolutionLevel, request2.getProvider());
            }
            LocationRequest sanitizedRequest = createSanitizedRequest(request2, allowedResolutionLevel, this.mContext.checkCallingPermission("android.permission.LOCATION_HARDWARE") == 0);
            if (D) {
                Log.d(TAG, "requestGeofence: " + sanitizedRequest + StringUtils.SPACE + geofence + StringUtils.SPACE + intent);
            }
            int uid = Binder.getCallingUid();
            if (UserHandle.getUserId(uid) != 0) {
                Log.w(TAG, "proximity alerts are currently available only to the primary user");
                return;
            }
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (this.mLock) {
                    this.mLocationUsageLogger.logLocationApiUsage(0, 4, packageName, request2, false, true, geofence, this.mActivityManager.getPackageImportance(packageName));
                }
                try {
                    this.mGeofenceManager.addFence(sanitizedRequest, geofence, intent, allowedResolutionLevel, uid, packageName);
                    Binder.restoreCallingIdentity(identity);
                    return;
                } catch (Throwable th) {
                    th = th;
                    Binder.restoreCallingIdentity(identity);
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        } else {
            throw new IllegalArgumentException("invalid pending intent: " + ((Object) null));
        }
        while (true) {
        }
        while (true) {
        }
    }

    public void removeGeofence(Geofence geofence, PendingIntent intent, String packageName) {
        if (intent != null) {
            checkPackageName(packageName);
            if (D) {
                Log.d(TAG, "removeGeofence: " + geofence + StringUtils.SPACE + intent);
            }
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (this.mLock) {
                    this.mLocationUsageLogger.logLocationApiUsage(1, 4, packageName, null, false, true, geofence, this.mActivityManager.getPackageImportance(packageName));
                }
                this.mGeofenceManager.removeFence(geofence, intent);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        } else {
            throw new IllegalArgumentException("invalid pending intent: " + ((Object) null));
        }
    }

    public boolean registerGnssStatusCallback(IGnssStatusListener listener, String packageName) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(23, Binder.getCallingUid());
        IBinder binder = listener != null ? listener.asBinder() : null;
        Log.d(TAG, "registerGnssStatusCallback by package: " + packageName + " ,listener binder: " + binder);
        return addGnssDataListener(listener, packageName, "GnssStatusListener", this.mGnssStatusProvider, this.mGnssStatusListeners, new Consumer() {
            /* class com.android.server.$$Lambda$1kw1pGRY14l4iRI8vioJeswbbZ0 */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                LocationManagerService.this.unregisterGnssStatusCallback((IGnssStatusListener) obj);
            }
        });
    }

    public void unregisterGnssStatusCallback(IGnssStatusListener listener) {
        IBinder binder = listener != null ? listener.asBinder() : null;
        Log.d(TAG, "unregisterGnssStatusCallback, callback binder: " + binder);
        removeGnssDataListener(listener, this.mGnssStatusProvider, this.mGnssStatusListeners);
    }

    public boolean addGnssMeasurementsListener(IGnssMeasurementsListener listener, String packageName) {
        return addGnssDataListener(listener, packageName, "GnssMeasurementsListener", this.mGnssMeasurementsProvider, this.mGnssMeasurementsListeners, new Consumer() {
            /* class com.android.server.$$Lambda$XnEj1qgrS2tLlw6uNlntfcuKl88 */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                LocationManagerService.this.removeGnssMeasurementsListener((IGnssMeasurementsListener) obj);
            }
        });
    }

    public void removeGnssMeasurementsListener(IGnssMeasurementsListener listener) {
        removeGnssDataListener(listener, this.mGnssMeasurementsProvider, this.mGnssMeasurementsListeners);
    }

    private static abstract class LinkedListenerBase implements IBinder.DeathRecipient {
        protected final CallerIdentity mCallerIdentity;
        protected final String mListenerName;

        private LinkedListenerBase(CallerIdentity callerIdentity, String listenerName) {
            this.mCallerIdentity = callerIdentity;
            this.mListenerName = listenerName;
        }
    }

    private static class LinkedListener<TListener> extends LinkedListenerBase {
        private final Consumer<TListener> mBinderDeathCallback;
        private final TListener mListener;

        private LinkedListener(TListener listener, String listenerName, CallerIdentity callerIdentity, Consumer<TListener> binderDeathCallback) {
            super(callerIdentity, listenerName);
            this.mListener = listener;
            this.mBinderDeathCallback = binderDeathCallback;
        }

        public void binderDied() {
            if (LocationManagerService.D) {
                Log.d(LocationManagerService.TAG, "Remote " + this.mListenerName + " died.");
            }
            this.mBinderDeathCallback.accept(this.mListener);
        }
    }

    private <TListener extends IInterface> boolean addGnssDataListener(TListener listener, String packageName, String listenerName, RemoteListenerHelper<TListener> gnssDataProvider, ArrayMap<IBinder, LinkedListener<TListener>> gnssDataListeners, Consumer<TListener> binderDeathCallback) {
        Object obj;
        int i;
        if (hasGnssPermissions(packageName)) {
            if (gnssDataProvider != null) {
                CallerIdentity callerIdentity = new CallerIdentity(Binder.getCallingUid(), Binder.getCallingPid(), packageName);
                LinkedListener<TListener> linkedListener = new LinkedListener<>(listener, listenerName, callerIdentity, binderDeathCallback);
                IBinder binder = listener.asBinder();
                Object obj2 = this.mLock;
                synchronized (obj2) {
                    try {
                        if (linkToListenerDeathNotificationLocked(binder, linkedListener)) {
                            gnssDataListeners.put(binder, linkedListener);
                            long identity = Binder.clearCallingIdentity();
                            try {
                                if (gnssDataProvider != this.mGnssMeasurementsProvider) {
                                    if (gnssDataProvider != this.mGnssStatusProvider) {
                                        obj = obj2;
                                        if (!isThrottlingExemptLocked(callerIdentity) || isImportanceForeground(this.mActivityManager.getPackageImportance(packageName))) {
                                            try {
                                                gnssDataProvider.addListener(listener, callerIdentity);
                                            } catch (Throwable th) {
                                                th = th;
                                                throw th;
                                            }
                                        }
                                        Binder.restoreCallingIdentity(identity);
                                        return true;
                                    }
                                }
                                LocationUsageLogger locationUsageLogger = this.mLocationUsageLogger;
                                if (gnssDataProvider == this.mGnssMeasurementsProvider) {
                                    i = 2;
                                } else {
                                    i = 3;
                                }
                                obj = obj2;
                                try {
                                    locationUsageLogger.logLocationApiUsage(0, i, packageName, null, true, false, null, this.mActivityManager.getPackageImportance(packageName));
                                    if (!isThrottlingExemptLocked(callerIdentity)) {
                                    }
                                    gnssDataProvider.addListener(listener, callerIdentity);
                                    Binder.restoreCallingIdentity(identity);
                                    return true;
                                } catch (Throwable th2) {
                                    th = th2;
                                    Binder.restoreCallingIdentity(identity);
                                    throw th;
                                }
                            } catch (Throwable th3) {
                                th = th3;
                                Binder.restoreCallingIdentity(identity);
                                throw th;
                            }
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        obj = obj2;
                        throw th;
                    }
                }
                return false;
            }
        }
        return false;
    }

    private <TListener extends IInterface> void removeGnssDataListener(TListener listener, RemoteListenerHelper<TListener> gnssDataProvider, ArrayMap<IBinder, LinkedListener<TListener>> gnssDataListeners) {
        int i;
        if (gnssDataProvider != null) {
            IBinder binder = listener.asBinder();
            synchronized (this.mLock) {
                try {
                    LinkedListener<TListener> linkedListener = gnssDataListeners.remove(binder);
                    if (linkedListener != null) {
                        long identity = Binder.clearCallingIdentity();
                        try {
                            if (gnssDataProvider == this.mGnssMeasurementsProvider || gnssDataProvider == this.mGnssStatusProvider) {
                                LocationUsageLogger locationUsageLogger = this.mLocationUsageLogger;
                                if (gnssDataProvider == this.mGnssMeasurementsProvider) {
                                    i = 2;
                                } else {
                                    i = 3;
                                }
                                locationUsageLogger.logLocationApiUsage(1, i, linkedListener.mCallerIdentity.mPackageName, null, true, false, null, this.mActivityManager.getPackageImportance(linkedListener.mCallerIdentity.mPackageName));
                            }
                            Binder.restoreCallingIdentity(identity);
                            unlinkFromListenerDeathNotificationLocked(binder, linkedListener);
                            gnssDataProvider.removeListener(listener);
                        } catch (Throwable th) {
                            th = th;
                            throw th;
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            }
        }
    }

    private boolean linkToListenerDeathNotificationLocked(IBinder binder, LinkedListenerBase linkedListener) {
        try {
            binder.linkToDeath(linkedListener, 0);
            return true;
        } catch (RemoteException e) {
            Log.w(TAG, "Could not link " + linkedListener.mListenerName + " death callback.", e);
            return false;
        }
    }

    private boolean unlinkFromListenerDeathNotificationLocked(IBinder binder, LinkedListenerBase linkedListener) {
        try {
            binder.unlinkToDeath(linkedListener, 0);
            return true;
        } catch (NoSuchElementException e) {
            Log.w(TAG, "Could not unlink " + linkedListener.mListenerName + " death callback.", e);
            return false;
        }
    }

    public void injectGnssMeasurementCorrections(GnssMeasurementCorrections measurementCorrections, String packageName) {
        this.mContext.enforceCallingPermission("android.permission.LOCATION_HARDWARE", "Location Hardware permission not granted to inject GNSS measurement corrections.");
        if (!hasGnssPermissions(packageName)) {
            Slog.e(TAG, "Can not inject GNSS corrections due to no permission.");
            return;
        }
        GnssMeasurementCorrectionsProvider gnssMeasurementCorrectionsProvider = this.mGnssMeasurementCorrectionsProvider;
        if (gnssMeasurementCorrectionsProvider == null) {
            Slog.e(TAG, "Can not inject GNSS corrections. GNSS measurement corrections provider not available.");
        } else {
            gnssMeasurementCorrectionsProvider.injectGnssMeasurementCorrections(measurementCorrections);
        }
    }

    public long getGnssCapabilities(String packageName) {
        GnssCapabilitiesProvider gnssCapabilitiesProvider;
        this.mContext.enforceCallingPermission("android.permission.LOCATION_HARDWARE", "Location Hardware permission not granted to obtain GNSS chipset capabilities.");
        if (!hasGnssPermissions(packageName) || (gnssCapabilitiesProvider = this.mGnssCapabilitiesProvider) == null) {
            return -1;
        }
        return gnssCapabilitiesProvider.getGnssCapabilities();
    }

    public boolean addGnssNavigationMessageListener(IGnssNavigationMessageListener listener, String packageName) {
        return addGnssDataListener(listener, packageName, "GnssNavigationMessageListener", this.mGnssNavigationMessageProvider, this.mGnssNavigationMessageListeners, new Consumer() {
            /* class com.android.server.$$Lambda$wg7j1ZorSDGIu2L17I_NmjcwgzQ */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                LocationManagerService.this.removeGnssNavigationMessageListener((IGnssNavigationMessageListener) obj);
            }
        });
    }

    public void removeGnssNavigationMessageListener(IGnssNavigationMessageListener listener) {
        removeGnssDataListener(listener, this.mGnssNavigationMessageProvider, this.mGnssNavigationMessageListeners);
    }

    public boolean sendExtraCommand(String providerName, String command, Bundle extras) {
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(28, Binder.getCallingUid());
        if (providerName != null) {
            synchronized (this.mLock) {
                checkResolutionLevelIsSufficientForProviderUseLocked(getCallerAllowedResolutionLevel(), providerName);
                this.mLocationUsageLogger.logLocationApiUsage(0, 5, providerName);
                if (this.mContext.checkCallingOrSelfPermission(ACCESS_LOCATION_EXTRA_COMMANDS) == 0) {
                    LocationProvider provider = getLocationProviderLocked(providerName);
                    if (provider != null) {
                        Log.e(TAG, "UID:" + Binder.getCallingUid() + "will sendExtraCommand:" + command + " extras:" + extras + " providerName" + providerName);
                        provider.sendExtraCommandLocked(command, extras);
                    }
                    this.mOppoLocationStatistics.handleCommand(providerName, command, extras);
                    this.mLocationUsageLogger.logLocationApiUsage(1, 5, providerName);
                } else {
                    throw new SecurityException("Requires ACCESS_LOCATION_EXTRA_COMMANDS permission");
                }
            }
            return true;
        }
        throw new NullPointerException();
    }

    public boolean sendNiResponse(int notifId, int userResponse) {
        if (Binder.getCallingUid() == Process.myUid()) {
            try {
                return this.mNetInitiatedListener.sendNiResponse(notifId, userResponse);
            } catch (RemoteException e) {
                Slog.e(TAG, "RemoteException in LocationManagerService.sendNiResponse");
                return false;
            }
        } else {
            throw new SecurityException("calling sendNiResponse from outside of the system is not allowed");
        }
    }

    public ProviderProperties getProviderProperties(String providerName) {
        synchronized (this.mLock) {
            checkResolutionLevelIsSufficientForProviderUseLocked(getCallerAllowedResolutionLevel(), providerName);
            LocationProvider provider = getLocationProviderLocked(providerName);
            if (provider == null) {
                return null;
            }
            ProviderProperties propertiesLocked = provider.getPropertiesLocked();
            return propertiesLocked;
        }
    }

    public boolean isProviderPackage(String packageName) {
        synchronized (this.mLock) {
            Iterator<LocationProvider> it = this.mProviders.iterator();
            while (it.hasNext()) {
                if (it.next().getPackagesLocked().contains(packageName)) {
                    return true;
                }
            }
            if (packageName.equals("com.google.android.gms")) {
                return true;
            }
            return false;
        }
    }

    public void setExtraLocationControllerPackage(String packageName) {
        this.mContext.enforceCallingPermission("android.permission.LOCATION_HARDWARE", "android.permission.LOCATION_HARDWARE permission required");
        synchronized (this.mLock) {
            this.mExtraLocationControllerPackage = packageName;
        }
    }

    public String getExtraLocationControllerPackage() {
        String str;
        synchronized (this.mLock) {
            str = this.mExtraLocationControllerPackage;
        }
        return str;
    }

    public void setExtraLocationControllerPackageEnabled(boolean enabled) {
        this.mContext.enforceCallingPermission("android.permission.LOCATION_HARDWARE", "android.permission.LOCATION_HARDWARE permission required");
        synchronized (this.mLock) {
            this.mExtraLocationControllerPackageEnabled = enabled;
        }
    }

    public boolean isExtraLocationControllerPackageEnabled() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mExtraLocationControllerPackageEnabled && this.mExtraLocationControllerPackage != null;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public boolean isLocationEnabled() {
        return isLocationEnabledForUser(this.mCurrentUserId);
    }

    public boolean isLocationEnabledForUser(int userId) {
        if (userId == 999) {
            userId = 0;
        } else if (UserHandle.getCallingUserId() != userId) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS", "Requires INTERACT_ACROSS_USERS permission");
        }
        long identity = Binder.clearCallingIdentity();
        try {
            boolean z = false;
            if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "location_mode", 0, userId) != 0) {
                z = true;
            }
            return z;
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    public boolean isProviderEnabledForUser(String providerName, int userId) {
        boolean z = false;
        OppoFeatureCache.getOrCreate(IColorAntiVirusBehaviorManager.DEFAULT, new Object[0]).setAction(74, Binder.getCallingUid());
        if (userId == 999) {
            userId = 0;
        } else if (UserHandle.getCallingUserId() != userId) {
            this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS", "Requires INTERACT_ACROSS_USERS permission");
        }
        if ("fused".equals(providerName)) {
            return false;
        }
        synchronized (this.mLock) {
            LocationProvider provider = getLocationProviderLocked(providerName);
            if (provider != null && provider.isUseableForUserLocked(userId)) {
                z = true;
            }
        }
        return z;
    }

    @GuardedBy({"mLock"})
    private static boolean shouldBroadcastSafeLocked(Location loc, Location lastLoc, UpdateRecord record, long now) {
        if (lastLoc == null) {
            return true;
        }
        if ((loc.getElapsedRealtimeNanos() - lastLoc.getElapsedRealtimeNanos()) / NANOS_PER_MILLI < record.mRealRequest.getFastestInterval() - 100) {
            return false;
        }
        double minDistance = (double) record.mRealRequest.getSmallestDisplacement();
        if ((minDistance > 0.0d && ((double) loc.distanceTo(lastLoc)) <= minDistance) || record.mRealRequest.getNumUpdates() <= 0) {
            return false;
        }
        if (record.mRealRequest.getExpireAt() >= now) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:96:0x02a5, code lost:
        if (r10 != 2) goto L_0x02aa;
     */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x0205  */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x020f  */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x022a  */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x0253  */
    @GuardedBy({"mLock"})
    public void handleLocationChangedLocked(Location location, LocationProvider provider) {
        Location lastLocationCoarseInterval;
        ArrayList<UpdateRecord> records;
        Location noGPSLocation;
        long timeDiffNanos;
        Location lastLocationCoarseInterval2;
        Location notifyLocation;
        Location lastLoc;
        if (this.mProviders.contains(provider)) {
            if (!location.isComplete()) {
                Log.w(TAG, "Dropping incomplete location: " + location);
                return;
            }
            if (provider.isUseableLocked() && !provider.isPassiveLocked()) {
                this.mPassiveProvider.updateLocation(location);
            }
            if (D) {
                Log.d(TAG, "incoming location: " + location);
            } else {
                Log.d(TAG, "incoming location from: " + location.getProvider());
            }
            long now = SystemClock.elapsedRealtime();
            if (provider.isUseableLocked()) {
                updateLastLocationLocked(location, provider.getName());
            }
            Location lastLocationCoarseInterval3 = this.mLastLocationCoarseInterval.get(provider.getName());
            if (lastLocationCoarseInterval3 == null) {
                lastLocationCoarseInterval3 = new Location(location);
                if (provider.isUseableLocked()) {
                    this.mLastLocationCoarseInterval.put(provider.getName(), lastLocationCoarseInterval3);
                }
            }
            long timeDiffNanos2 = location.getElapsedRealtimeNanos() - lastLocationCoarseInterval.getElapsedRealtimeNanos();
            if (timeDiffNanos2 > 600000000000L) {
                lastLocationCoarseInterval.set(location);
            }
            Location noGPSLocation2 = lastLocationCoarseInterval.getExtraLocation("noGPSLocation");
            ArrayList<UpdateRecord> records2 = this.mRecordsByProvider.get(provider.getName());
            if (records2 == null) {
                return;
            }
            if (records2.size() != 0) {
                Location coarseLocation = null;
                if (noGPSLocation2 != null) {
                    coarseLocation = this.mLocationFudger.getOrCreate(noGPSLocation2);
                } else if (isCtaSupported()) {
                    coarseLocation = this.mLocationFudger.getOrCreate(lastLocationCoarseInterval);
                }
                ArrayList<Receiver> deadReceivers = null;
                ArrayList<UpdateRecord> deadUpdateRecords = null;
                Iterator<UpdateRecord> it = records2.iterator();
                while (it.hasNext()) {
                    UpdateRecord r = it.next();
                    Receiver receiver = r.mReceiver;
                    boolean receiverDead = false;
                    if (provider.isUseableLocked() || isSettingsExemptLocked(r)) {
                        lastLocationCoarseInterval2 = lastLocationCoarseInterval;
                    } else {
                        lastLocationCoarseInterval2 = lastLocationCoarseInterval;
                        if (!provider.isNetworkWhiteList(receiver.mCallerIdentity.mPackageName)) {
                            timeDiffNanos = timeDiffNanos2;
                            noGPSLocation = noGPSLocation2;
                            records = records2;
                            lastLocationCoarseInterval = lastLocationCoarseInterval2;
                            timeDiffNanos2 = timeDiffNanos;
                            noGPSLocation2 = noGPSLocation;
                            records2 = records;
                        }
                    }
                    int receiverUserId = UserHandle.getUserId(receiver.mCallerIdentity.mUid);
                    if (!isCurrentProfileLocked(receiverUserId)) {
                        timeDiffNanos = timeDiffNanos2;
                        if (!isProviderPackage(receiver.mCallerIdentity.mPackageName)) {
                            if (D) {
                                Log.d(TAG, "skipping loc update for background user " + receiverUserId + " (current user: " + this.mCurrentUserId + ", app: " + receiver.mCallerIdentity.mPackageName + ")");
                                noGPSLocation = noGPSLocation2;
                                records = records2;
                            } else {
                                noGPSLocation = noGPSLocation2;
                                records = records2;
                            }
                            lastLocationCoarseInterval = lastLocationCoarseInterval2;
                            timeDiffNanos2 = timeDiffNanos;
                            noGPSLocation2 = noGPSLocation;
                            records2 = records;
                        }
                    } else {
                        timeDiffNanos = timeDiffNanos2;
                    }
                    if (!this.mBlacklist.isBlacklisted(receiver.mCallerIdentity.mPackageName)) {
                        if (receiver.mAllowedResolutionLevel < 2) {
                            noGPSLocation = noGPSLocation2;
                            records = records2;
                            if (!PswServiceFactory.getInstance().getFeature(IPswCoarseToFine.DEFAULT, new Object[]{this.mContext}).isAllowCoarseToFine(receiver.mCallerIdentity.mPackageName)) {
                                notifyLocation = coarseLocation;
                                if (notifyLocation != null && ((lastLoc = r.mLastFixBroadcast) == null || shouldBroadcastSafeLocked(notifyLocation, lastLoc, r, now))) {
                                    if (lastLoc != null) {
                                        lastLoc = new Location(notifyLocation);
                                        Location unused = r.mLastFixBroadcast = lastLoc;
                                    } else {
                                        lastLoc.set(notifyLocation);
                                    }
                                    if (!reportLocationAccessNoThrow(receiver.mCallerIdentity.mPid, receiver.mCallerIdentity.mUid, receiver.mCallerIdentity.mPackageName, receiver.mAllowedResolutionLevel)) {
                                        if (!receiver.callLocationChangedLocked(notifyLocation)) {
                                            Slog.w(TAG, "RemoteException calling onLocationChanged on " + receiver);
                                            receiverDead = true;
                                        }
                                        r.mRealRequest.decrementNumUpdates();
                                    } else if (D) {
                                        Log.d(TAG, "skipping loc update for no op app: " + receiver.mCallerIdentity.mPackageName);
                                    }
                                }
                                if (Settings.Global.getInt(this.mContext.getContentResolver(), "location_disable_status_callbacks", 1) == 0) {
                                    long newStatusUpdateTime = provider.getStatusUpdateTimeLocked();
                                    Bundle extras = new Bundle();
                                    int status = provider.getStatusLocked(extras);
                                    long prevStatusUpdateTime = r.mLastStatusBroadcast;
                                    if (newStatusUpdateTime > prevStatusUpdateTime) {
                                        if (prevStatusUpdateTime == 0) {
                                        }
                                        long unused2 = r.mLastStatusBroadcast = newStatusUpdateTime;
                                        if (!receiver.callStatusChangedLocked(provider.getName(), status, extras)) {
                                            receiverDead = true;
                                            Slog.w(TAG, "RemoteException calling onStatusChanged on " + receiver);
                                        }
                                    }
                                }
                                if (r.mRealRequest.getNumUpdates() <= 0 || r.mRealRequest.getExpireAt() < now) {
                                    if (deadUpdateRecords == null) {
                                        deadUpdateRecords = new ArrayList<>();
                                    }
                                    deadUpdateRecords.add(r);
                                }
                                if (receiverDead) {
                                    if (deadReceivers == null) {
                                        deadReceivers = new ArrayList<>();
                                    }
                                    if (!deadReceivers.contains(receiver)) {
                                        deadReceivers.add(receiver);
                                    }
                                }
                                lastLocationCoarseInterval = lastLocationCoarseInterval2;
                                timeDiffNanos2 = timeDiffNanos;
                                noGPSLocation2 = noGPSLocation;
                                records2 = records;
                            }
                        } else {
                            noGPSLocation = noGPSLocation2;
                            records = records2;
                        }
                        notifyLocation = location;
                        if (lastLoc != null) {
                        }
                        if (!reportLocationAccessNoThrow(receiver.mCallerIdentity.mPid, receiver.mCallerIdentity.mUid, receiver.mCallerIdentity.mPackageName, receiver.mAllowedResolutionLevel)) {
                        }
                    } else if (D) {
                        Log.d(TAG, "skipping loc update for blacklisted app: " + receiver.mCallerIdentity.mPackageName);
                        noGPSLocation = noGPSLocation2;
                        records = records2;
                    } else {
                        noGPSLocation = noGPSLocation2;
                        records = records2;
                    }
                    lastLocationCoarseInterval = lastLocationCoarseInterval2;
                    timeDiffNanos2 = timeDiffNanos;
                    noGPSLocation2 = noGPSLocation;
                    records2 = records;
                }
                if (deadReceivers != null) {
                    Iterator<Receiver> it2 = deadReceivers.iterator();
                    while (it2.hasNext()) {
                        removeUpdatesLocked(it2.next());
                    }
                }
                if (deadUpdateRecords != null) {
                    Iterator<UpdateRecord> it3 = deadUpdateRecords.iterator();
                    while (it3.hasNext()) {
                        it3.next().disposeLocked(true);
                    }
                    applyRequirementsLocked(provider);
                }
            }
        }
    }

    @GuardedBy({"mLock"})
    private void updateLastLocationLocked(Location location, String provider) {
        Location noGPSLocation = location.getExtraLocation("noGPSLocation");
        Location lastLocation = this.mLastLocation.get(provider);
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
    }

    public boolean geocoderIsPresent() {
        return this.mGeocodeProvider != null;
    }

    public String getFromLocation(double latitude, double longitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        if (this.mGeocodeProvider != null) {
            return this.mNlpProxy.getFromLocation(latitude, longitude, maxResults, params, addrs);
        }
        return null;
    }

    public String getFromLocationName(String locationName, double lowerLeftLatitude, double lowerLeftLongitude, double upperRightLatitude, double upperRightLongitude, int maxResults, GeocoderParams params, List<Address> addrs) {
        if (this.mGeocodeProvider != null) {
            return this.mNlpProxy.getFromLocationName(locationName, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude, maxResults, params, addrs);
        }
        return null;
    }

    private boolean canCallerAccessMockLocation(String opPackageName) {
        if (!opPackageName.equalsIgnoreCase(PackageManagerService.PLATFORM_PACKAGE_NAME) && this.mAppOps.checkOp(58, Binder.getCallingUid(), opPackageName) != 0) {
            return false;
        }
        return true;
    }

    public void getLocAppsOp(int flag, LocAppsOp locAppsOp) {
        this.mOppoLocationBlacklistUtil.getLocAppsOp(flag, locAppsOp);
    }

    public void setLocAppsOp(int cmd, LocAppsOp locAppsOp) {
        this.mOppoLocationBlacklistUtil.setLocAppsOp(cmd, locAppsOp);
    }

    /* access modifiers changed from: private */
    public class LocationWorkHandler extends Handler {
        public LocationWorkHandler(Looper looper) {
            super(looper, null);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 101) {
                synchronized (LocationManagerService.this.mLock) {
                    LocationManagerService.this.onAppOpChangedLocked();
                }
            }
        }
    }

    public void addTestProvider(String name, ProviderProperties properties, String opPackageName) {
        if (canCallerAccessMockLocation(opPackageName)) {
            if (!"passive".equals(name)) {
                synchronized (this.mLock) {
                    long identity = Binder.clearCallingIdentity();
                    try {
                        LocationProvider oldProvider = getLocationProviderLocked(name);
                        if (oldProvider != null) {
                            if (!oldProvider.isMock()) {
                                removeProviderLocked(oldProvider);
                            } else {
                                throw new IllegalArgumentException("Provider \"" + name + "\" already exists");
                            }
                        }
                        MockLocationProvider mockProviderManager = new MockLocationProvider(name);
                        addProviderLocked(mockProviderManager);
                        this.mOppoLbsRepairer.onAddMockProvider(opPackageName, name);
                        mockProviderManager.attachLocked(new MockProvider(this.mContext, mockProviderManager, properties));
                    } finally {
                        Binder.restoreCallingIdentity(identity);
                    }
                }
                return;
            }
            throw new IllegalArgumentException("Cannot mock the passive location provider");
        }
    }

    public void removeTestProvider(String name, String opPackageName) {
        if (canCallerAccessMockLocation(opPackageName)) {
            synchronized (this.mLock) {
                long identity = Binder.clearCallingIdentity();
                try {
                    LocationProvider testProvider = getLocationProviderLocked(name);
                    if (testProvider == null || !testProvider.isMock()) {
                        throw new IllegalArgumentException("Provider \"" + name + "\" unknown");
                    }
                    removeProviderLocked(testProvider);
                    this.mOppoLbsRepairer.onRemoveMockProvider(opPackageName, name);
                    LocationProvider realProvider = null;
                    Iterator<LocationProvider> it = this.mRealProviders.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        LocationProvider provider = it.next();
                        if (name.equals(provider.getName())) {
                            realProvider = provider;
                            break;
                        }
                    }
                    if (realProvider != null) {
                        addProviderLocked(realProvider);
                    }
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }
    }

    public void setTestProviderLocation(String providerName, Location location, String opPackageName) {
        if (canCallerAccessMockLocation(opPackageName)) {
            synchronized (this.mLock) {
                LocationProvider testProvider = getLocationProviderLocked(providerName);
                if (testProvider == null || !testProvider.isMock()) {
                    throw new IllegalArgumentException("Provider \"" + providerName + "\" unknown");
                }
                String locationProvider = location.getProvider();
                if (!TextUtils.isEmpty(locationProvider) && !providerName.equals(locationProvider)) {
                    EventLog.writeEvent(1397638484, "33091107", Integer.valueOf(Binder.getCallingUid()), providerName + "!=" + location.getProvider());
                }
                ((MockLocationProvider) testProvider).setLocationLocked(location);
            }
        }
    }

    public void setTestProviderEnabled(String providerName, boolean enabled, String opPackageName) {
        if (canCallerAccessMockLocation(opPackageName)) {
            synchronized (this.mLock) {
                LocationProvider testProvider = getLocationProviderLocked(providerName);
                if (testProvider == null || !testProvider.isMock()) {
                    throw new IllegalArgumentException("Provider \"" + providerName + "\" unknown");
                }
                ((MockLocationProvider) testProvider).setEnabledLocked(enabled);
            }
        }
    }

    public void setTestProviderStatus(String providerName, int status, Bundle extras, long updateTime, String opPackageName) {
        if (canCallerAccessMockLocation(opPackageName)) {
            synchronized (this.mLock) {
                LocationProvider testProvider = getLocationProviderLocked(providerName);
                if (testProvider == null || !testProvider.isMock()) {
                    throw new IllegalArgumentException("Provider \"" + providerName + "\" unknown");
                }
                ((MockLocationProvider) testProvider).setStatusLocked(status, extras, updateTime);
            }
        }
    }

    public List<LocationRequest> getTestProviderCurrentRequests(String providerName, String opPackageName) {
        if (!canCallerAccessMockLocation(opPackageName)) {
            return Collections.emptyList();
        }
        synchronized (this.mLock) {
            LocationProvider testProvider = getLocationProviderLocked(providerName);
            if (testProvider == null || !testProvider.isMock()) {
                throw new IllegalArgumentException("Provider \"" + providerName + "\" unknown");
            }
            MockLocationProvider provider = (MockLocationProvider) testProvider;
            if (provider.mCurrentRequest == null) {
                List<LocationRequest> emptyList = Collections.emptyList();
                return emptyList;
            }
            List<LocationRequest> requests = new ArrayList<>();
            for (LocationRequest request : provider.mCurrentRequest.locationRequests) {
                requests.add(new LocationRequest(request));
            }
            return requests;
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x002a, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:80:0x033f, code lost:
        return;
     */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            synchronized (this.mLock) {
                if (args.length <= 0 || !args[0].equals("--gnssmetrics")) {
                    pw.println("Current Location Manager state:");
                    pw.print("  Current System Time: " + TimeUtils.logTimeOfDay(System.currentTimeMillis()));
                    pw.println(", Current Elapsed Time: " + TimeUtils.formatDuration(SystemClock.elapsedRealtime()));
                    pw.println("  Current user: " + this.mCurrentUserId + StringUtils.SPACE + Arrays.toString(this.mCurrentUserProfiles));
                    StringBuilder sb = new StringBuilder();
                    sb.append("  Location mode: ");
                    sb.append(isLocationEnabled());
                    pw.println(sb.toString());
                    pw.println("  Battery Saver Location Mode: " + PowerManager.locationPowerSaveModeToString(this.mBatterySaverMode));
                    pw.println("  Location Listeners:");
                    Iterator<Receiver> it = this.mReceivers.values().iterator();
                    while (it.hasNext()) {
                        pw.println("    " + it.next());
                    }
                    pw.println("  Active Records by Provider:");
                    for (Map.Entry<String, ArrayList<UpdateRecord>> entry : this.mRecordsByProvider.entrySet()) {
                        pw.println("    " + entry.getKey() + ":");
                        Iterator<UpdateRecord> it2 = entry.getValue().iterator();
                        while (it2.hasNext()) {
                            pw.println("      " + it2.next());
                        }
                    }
                    pw.println("  Active GnssMeasurement Listeners:");
                    dumpGnssDataListenersLocked(pw, this.mGnssMeasurementsListeners);
                    pw.println("  Active GnssNavigationMessage Listeners:");
                    dumpGnssDataListenersLocked(pw, this.mGnssNavigationMessageListeners);
                    pw.println("  Active GnssStatus Listeners:");
                    dumpGnssDataListenersLocked(pw, this.mGnssStatusListeners);
                    pw.println("  Historical Records by Provider:");
                    for (Map.Entry<LocationRequestStatistics.PackageProviderKey, LocationRequestStatistics.PackageStatistics> entry2 : this.mRequestStatistics.statistics.entrySet()) {
                        LocationRequestStatistics.PackageProviderKey key = entry2.getKey();
                        pw.println("    " + key.packageName + ": " + key.providerName + ": " + entry2.getValue());
                    }
                    pw.println("  Last Known Locations:");
                    for (Map.Entry<String, Location> entry3 : this.mLastLocation.entrySet()) {
                        pw.println("    " + entry3.getKey() + ": " + entry3.getValue());
                    }
                    pw.println("  Last Known Locations Coarse Intervals:");
                    for (Map.Entry<String, Location> entry4 : this.mLastLocationCoarseInterval.entrySet()) {
                        pw.println("    " + entry4.getKey() + ": " + entry4.getValue());
                    }
                    if (this.mGeofenceManager != null) {
                        this.mGeofenceManager.dump(pw);
                    } else {
                        pw.println("  Geofences: null");
                    }
                    if (this.mBlacklist != null) {
                        pw.append((CharSequence) "  ");
                        this.mBlacklist.dump(pw);
                    } else {
                        pw.println("  mBlacklist=null");
                    }
                    if (this.mExtraLocationControllerPackage != null) {
                        pw.println(" Location controller extra package: " + this.mExtraLocationControllerPackage + " enabled: " + this.mExtraLocationControllerPackageEnabled);
                    }
                    if (!this.mBackgroundThrottlePackageWhitelist.isEmpty()) {
                        pw.println("  Throttling Whitelisted Packages:");
                        Iterator<String> it3 = this.mBackgroundThrottlePackageWhitelist.iterator();
                        while (it3.hasNext()) {
                            pw.println("    " + it3.next());
                        }
                    }
                    if (!this.mIgnoreSettingsPackageWhitelist.isEmpty()) {
                        pw.println("  Bypass Whitelisted Packages:");
                        Iterator<String> it4 = this.mIgnoreSettingsPackageWhitelist.iterator();
                        while (it4.hasNext()) {
                            pw.println("    " + it4.next());
                        }
                    }
                    if (this.mLocationFudger != null) {
                        pw.append((CharSequence) "  fudger: ");
                        this.mLocationFudger.dump(fd, pw, args);
                    } else {
                        pw.println("  fudger: null");
                    }
                    if (args.length <= 0 || !"short".equals(args[0])) {
                        Iterator<LocationProvider> it5 = this.mProviders.iterator();
                        while (it5.hasNext()) {
                            it5.next().dumpLocked(fd, pw, args);
                        }
                        if (this.mGnssBatchingInProgress) {
                            pw.println("  GNSS batching in progress");
                        }
                    }
                } else if (this.mGnssMetricsProvider != null) {
                    pw.append((CharSequence) this.mGnssMetricsProvider.getGnssMetricsAsProtoString());
                }
            }
        }
    }

    @GuardedBy({"mLock"})
    private void dumpGnssDataListenersLocked(PrintWriter pw, ArrayMap<IBinder, ? extends LinkedListenerBase> gnssDataListeners) {
        for (LinkedListenerBase listener : gnssDataListeners.values()) {
            CallerIdentity callerIdentity = listener.mCallerIdentity;
            pw.println("    " + callerIdentity.mPid + StringUtils.SPACE + callerIdentity.mUid + StringUtils.SPACE + callerIdentity.mPackageName + ": " + isThrottlingExemptLocked(callerIdentity));
        }
    }

    private void initMtkLocationManagerService() {
        Constructor constructor;
        try {
            this.mMtkLocationManagerServiceClass = Class.forName("com.mediatek.location.MtkLocationExt$LocationManagerService");
            if (D) {
                Log.d(TAG, "class = " + this.mMtkLocationManagerServiceClass);
            }
            if (!(this.mMtkLocationManagerServiceClass == null || (constructor = this.mMtkLocationManagerServiceClass.getConstructor(Context.class, Handler.class)) == null)) {
                this.mMtkLocationManagerService = constructor.newInstance(this.mContext, this.mHandler);
            }
            this.mCtaSupported = checkCtaSuport();
            Log.d(TAG, "mMtkLocationManagerService = " + this.mMtkLocationManagerService + " mCtaSupported = " + this.mCtaSupported);
        } catch (Exception e) {
            Log.w(TAG, "Failed to init mMtkLocationManagerService!");
        }
    }

    private boolean isCtaSupported() {
        return this.mCtaSupported;
    }

    private boolean checkCtaSuport() {
        Boolean ret = false;
        try {
            if (this.mMtkLocationManagerService != null) {
                ret = (Boolean) this.mMtkLocationManagerServiceClass.getMethod("isCtaFeatureSupport", new Class[0]).invoke(this.mMtkLocationManagerService, new Object[0]);
                Log.d(TAG, "checkCtaSupport = " + ret);
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to call isCtaFeatureSupport!");
        }
        return ret.booleanValue();
    }

    private void mtkPrintCtaLog(int callingPid, int callingUid, String functionName, String actionType, String parameter) {
        try {
            if (isCtaSupported()) {
                this.mMtkLocationManagerServiceClass.getMethod("printCtaLog", Integer.TYPE, Integer.TYPE, String.class, String.class, String.class).invoke(this.mMtkLocationManagerService, Integer.valueOf(callingPid), Integer.valueOf(callingUid), functionName, actionType, parameter);
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to call printCtaLog!");
        }
    }

    private boolean mtkCheckCtaOp(int pid, int uid, int op) {
        if (!isCtaSupported()) {
            return true;
        }
        String[] pkgs = this.mContext.getPackageManager().getPackagesForUid(uid);
        if (pkgs == null) {
            Log.d(TAG, "checkOp(pid = " + pid + ", uid = " + uid + ") pkg == null, return false");
            return false;
        }
        for (String pkg : pkgs) {
            int mode = this.mAppOps.checkOpNoThrow(op, uid, pkg);
            if (mode == 0 || mode == 1) {
                return true;
            }
            Log.d(TAG, "checkOp(pid = " + pid + ", uid = " + uid + ", op = " + op + ", pkg = " + pkg + ", mode = " + mode + ") NOT Allowed or Ignored");
        }
        return false;
    }

    private boolean mtkNoteOpForCta(int op, int pid, int uid, String packageName, int allowedResolutionLevel) {
        if (!isCtaSupported()) {
            return false;
        }
        if (op != 1) {
            Log.d(TAG, "noteOpNoThrow(op = OP_COARSE_LOCATION) returns false");
            return false;
        } else if (this.mAppOps.noteOpNoThrow(0, uid, packageName) == 0) {
            return true;
        } else {
            Log.d(TAG, "noteOpNoThrow(op = OP_COARSE_LOCATION, uid = " + uid + ", pkg = " + packageName + ") != ALLOWED");
            return false;
        }
    }

    private boolean mtkCheckOpForCta(int op, int pid, int uid, String packageName, int allowedResolutionLevel) {
        if (!isCtaSupported()) {
            return false;
        }
        if (op != 1) {
            Log.d(TAG, "checkOp(op = OP_COARSE_LOCATION) returns false");
            return false;
        } else if (this.mAppOps.checkOp(0, uid, packageName) == 0) {
            return true;
        } else {
            Log.d(TAG, "checkOp(op = OP_COARSE_LOCATION , uid = " + uid + ", pkg = " + packageName + ") != ALLOWED");
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: mtkShowNlpNotInstalledToast */
    public void lambda$requestLocationUpdatesLocked$9$LocationManagerService(String provider) {
        try {
            if (this.mMtkLocationManagerService != null) {
                this.mMtkLocationManagerServiceClass.getMethod("showNlpNotInstalledToast", String.class).invoke(this.mMtkLocationManagerService, provider);
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to call showNlpNotInstalledToast ", e);
        }
    }

    @OppoHook(level = OppoHook.OppoHookType.NEW_METHOD, note = "ZhiYong.Lin@Plf.Framework, add for BPM", property = OppoHook.OppoRomType.ROM)
    public int[] getLocationListenersUid() {
        synchronized (this.mLock) {
            ArrayList<Integer> tmp = new ArrayList<>();
            for (Receiver i : this.mReceivers.values()) {
                for (Map.Entry<String, UpdateRecord> j : i.mUpdateRecords.entrySet()) {
                    tmp.add(Integer.valueOf(j.getValue().mReceiver.mCallerIdentity.mUid));
                }
            }
            int size = tmp.size();
            if (size <= 0) {
                return null;
            }
            int[] res = new int[size];
            for (int i2 = 0; i2 < size; i2++) {
                res[i2] = tmp.get(i2).intValue();
            }
            return res;
        }
    }
}
