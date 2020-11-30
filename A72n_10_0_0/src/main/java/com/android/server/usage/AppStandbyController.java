package com.android.server.usage;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.usage.AppStandbyInfo;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManagerInternal;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManagerInternal;
import android.content.pm.ParceledListSlice;
import android.database.ContentObserver;
import android.hardware.display.DisplayManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.NetworkScoreManager;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.Handler;
import android.os.IDeviceIdleController;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.KeyValueListParser;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.util.TimeUtils;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.IBatteryStats;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.ConcurrentUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.LocalServices;
import com.android.server.biometrics.fingerprint.dcs.DcsFingerprintStatisticsUtil;
import com.android.server.job.JobPackageTracker;
import com.android.server.pm.DumpState;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.Settings;
import com.android.server.policy.PhoneWindowManager;
import com.android.server.slice.SliceClientPermissions;
import com.android.server.usage.AppIdleHistory;
import com.android.server.usb.descriptors.UsbTerminalTypes;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import oppo.util.OppoStatistics;
import org.xmlpull.v1.XmlPullParser;

public class AppStandbyController {
    static final boolean COMPRESS_TIME = false;
    static final boolean DEBUG = false;
    static final boolean DEBUG_PANIC = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final long DEFAULT_PREDICTION_TIMEOUT = 43200000;
    private static final String DOZE_CONFIG_XML_PATH = "/data/system/doze_config_local.xml";
    static final long[] ELAPSED_TIME_THRESHOLDS = {0, 43200000, 86400000, 172800000};
    static final int MSG_CHECK_IDLE_STATES = 5;
    static final int MSG_CHECK_PACKAGE_IDLE_STATE = 11;
    static final int MSG_CHECK_PAROLE_TIMEOUT = 6;
    static final int MSG_FORCE_IDLE_STATE = 4;
    static final int MSG_INFORM_LISTENERS = 3;
    static final int MSG_ONE_TIME_CHECK_IDLE_STATES = 10;
    static final int MSG_PAROLE_END_TIMEOUT = 7;
    static final int MSG_PAROLE_STATE_CHANGED = 9;
    static final int MSG_REPORT_CONTENT_PROVIDER_USAGE = 8;
    static final int MSG_REPORT_EXEMPTED_SYNC_START = 13;
    static final int MSG_REPORT_SYNC_SCHEDULED = 12;
    static final int MSG_UPDATE_STABLE_CHARGING = 14;
    private static final int MULTIAPP_USER_ID = 999;
    private static final long ONE_DAY = 86400000;
    private static final long ONE_HOUR = 3600000;
    private static final long ONE_MINUTE = 60000;
    private static boolean OPPODEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    static final long[] SCREEN_TIME_THRESHOLDS = {0, 0, 3600000, SettingsObserver.DEFAULT_SYSTEM_UPDATE_TIMEOUT};
    private static final String TAG = "AppStandbyController";
    private static final String TAG_AUTO_POWER_SAVE_MODES_ENABLED = "auto_power_save_enable";
    static final int[] THRESHOLD_BUCKETS = {10, 20, 30, 40};
    private static final long WAIT_FOR_ADMIN_DATA_TIMEOUT_MS = 10000;
    static final ArrayList<StandbyUpdateRecord> sStandbyUpdatePool = new ArrayList<>(4);
    @GuardedBy({"mActiveAdminApps"})
    private final SparseArray<Set<String>> mActiveAdminApps;
    private final CountDownLatch mAdminDataAvailableLatch;
    volatile boolean mAppIdleEnabled;
    @GuardedBy({"mAppIdleLock"})
    private AppIdleHistory mAppIdleHistory;
    private final Object mAppIdleLock;
    long mAppIdleParoleDurationMillis;
    long mAppIdleParoleIntervalMillis;
    long mAppIdleParoleWindowMillis;
    boolean mAppIdleTempParoled;
    long[] mAppStandbyElapsedThresholds;
    long[] mAppStandbyScreenThresholds;
    private AppWidgetManager mAppWidgetManager;
    @GuardedBy({"mAppIdleLock"})
    private List<String> mCarrierPrivilegedApps;
    boolean mCharging;
    boolean mChargingStable;
    long mCheckIdleIntervalMillis;
    private ConnectivityManager mConnectivityManager;
    private final Context mContext;
    private final DeviceStateReceiver mDeviceStateReceiver;
    private final DisplayManager.DisplayListener mDisplayListener;
    long mExemptedSyncScheduledDozeTimeoutMillis;
    long mExemptedSyncScheduledNonDozeTimeoutMillis;
    long mExemptedSyncStartTimeoutMillis;
    private ArrayList<String> mGoogleRestrictList;
    private ArrayMap<Pair<String, Integer>, Runnable> mGoogleRestrictRunnables;
    private boolean mGoogleRestricted;
    private BroadcastReceiver mGoogleRestriction;
    private final AppStandbyHandler mHandler;
    @GuardedBy({"mAppIdleLock"})
    private boolean mHaveCarrierPrivilegedApps;
    long mInitialForegroundServiceStartTimeoutMillis;
    Injector mInjector;
    private boolean mIsExpVersion;
    private long mLastAppIdleParoledTime;
    private Runnable mListRunnable;
    private int mMultiUserid;
    private final ConnectivityManager.NetworkCallback mNetworkCallback;
    private final NetworkRequest mNetworkRequest;
    long mNotificationSeenTimeoutMillis;
    @GuardedBy({"mPackageAccessListeners"})
    private ArrayList<UsageStatsManagerInternal.AppIdleStateChangeListener> mPackageAccessListeners;
    private PackageManager mPackageManager;
    private boolean mPendingInitializeDefaults;
    private volatile boolean mPendingOneTimeCheckIdleStates;
    private PowerManager mPowerManager;
    long mPredictionTimeoutMillis;
    private Runnable mRestrictRunnable;
    private volatile boolean mRusAutoPowerSaveEnable;
    long mStableChargingThresholdMillis;
    long mStrongUsageTimeoutMillis;
    long mSyncAdapterTimeoutMillis;
    long mSystemInteractionTimeoutMillis;
    private boolean mSystemServicesReady;
    long mSystemUpdateUsageTimeoutMillis;
    long mUnexemptedSyncScheduledTimeoutMillis;
    private final BroadcastReceiver mUserSwitchReceiver;
    private Runnable mUseridRunnable;

    static class Lock {
        Lock() {
        }
    }

    public static class StandbyUpdateRecord {
        int bucket;
        boolean isUserInteraction;
        String packageName;
        int reason;
        int userId;

        StandbyUpdateRecord(String pkgName, int userId2, int bucket2, int reason2, boolean isInteraction) {
            this.packageName = pkgName;
            this.userId = userId2;
            this.bucket = bucket2;
            this.reason = reason2;
            this.isUserInteraction = isInteraction;
        }

        public static StandbyUpdateRecord obtain(String pkgName, int userId2, int bucket2, int reason2, boolean isInteraction) {
            synchronized (AppStandbyController.sStandbyUpdatePool) {
                int size = AppStandbyController.sStandbyUpdatePool.size();
                if (size < 1) {
                    return new StandbyUpdateRecord(pkgName, userId2, bucket2, reason2, isInteraction);
                }
                StandbyUpdateRecord r = AppStandbyController.sStandbyUpdatePool.remove(size - 1);
                r.packageName = pkgName;
                r.userId = userId2;
                r.bucket = bucket2;
                r.reason = reason2;
                r.isUserInteraction = isInteraction;
                return r;
            }
        }

        public void recycle() {
            synchronized (AppStandbyController.sStandbyUpdatePool) {
                AppStandbyController.sStandbyUpdatePool.add(this);
            }
        }
    }

    AppStandbyController(Context context, Looper looper) {
        this(new Injector(context, looper));
    }

    AppStandbyController(Injector injector) {
        this.mAppIdleLock = new Lock();
        this.mPackageAccessListeners = new ArrayList<>();
        this.mActiveAdminApps = new SparseArray<>();
        this.mAdminDataAvailableLatch = new CountDownLatch(1);
        this.mAppStandbyScreenThresholds = SCREEN_TIME_THRESHOLDS;
        this.mAppStandbyElapsedThresholds = ELAPSED_TIME_THRESHOLDS;
        this.mSystemServicesReady = false;
        this.mNetworkRequest = new NetworkRequest.Builder().build();
        this.mNetworkCallback = new ConnectivityManager.NetworkCallback() {
            /* class com.android.server.usage.AppStandbyController.AnonymousClass1 */

            public void onAvailable(Network network) {
                AppStandbyController.this.mConnectivityManager.unregisterNetworkCallback(this);
                AppStandbyController.this.checkParoleTimeout();
            }
        };
        this.mDisplayListener = new DisplayManager.DisplayListener() {
            /* class com.android.server.usage.AppStandbyController.AnonymousClass2 */

            public void onDisplayAdded(int displayId) {
            }

            public void onDisplayRemoved(int displayId) {
            }

            public void onDisplayChanged(int displayId) {
                if (displayId == 0) {
                    boolean displayOn = AppStandbyController.this.isDisplayOn();
                    synchronized (AppStandbyController.this.mAppIdleLock) {
                        AppStandbyController.this.mAppIdleHistory.updateDisplay(displayOn, AppStandbyController.this.mInjector.elapsedRealtime());
                    }
                }
            }
        };
        this.mIsExpVersion = false;
        this.mGoogleRestricted = false;
        this.mGoogleRestrictList = new ArrayList<>();
        this.mGoogleRestrictRunnables = new ArrayMap<>();
        this.mGoogleRestriction = new BroadcastReceiver() {
            /* class com.android.server.usage.AppStandbyController.AnonymousClass3 */

            public void onReceive(Context context, Intent intent) {
                boolean restrictlistChange = intent.getBooleanExtra("restrict_list_change", false);
                boolean restrict = intent.getBooleanExtra("restrict_enable", AppStandbyController.this.mGoogleRestricted);
                ArrayList<String> restrictList = intent.getStringArrayListExtra("restrict_list");
                if (restrict != AppStandbyController.this.mGoogleRestricted) {
                    if (!restrictlistChange && AppStandbyController.this.mGoogleRestrictList.size() == 0 && restrictList != null && restrictList.size() > 0) {
                        AppStandbyController.this.mGoogleRestrictList.addAll(restrictList);
                    }
                    AppStandbyController.this.mGoogleRestricted = restrict;
                    AppStandbyController.this.handleRestrictChange();
                }
                if (restrictlistChange) {
                    AppStandbyController.this.mGoogleRestrictList.clear();
                    if (restrictList != null && restrictList.size() > 0) {
                        AppStandbyController.this.mGoogleRestrictList.addAll(restrictList);
                    }
                    if (AppStandbyController.this.mGoogleRestricted) {
                        AppStandbyController.this.handleWhitelistChange();
                    }
                }
            }
        };
        this.mListRunnable = new Runnable() {
            /* class com.android.server.usage.AppStandbyController.AnonymousClass4 */

            public void run() {
                PackageManager pm = AppStandbyController.this.mContext.getPackageManager();
                long elapsedRealtime = SystemClock.elapsedRealtime();
                Iterator it = AppStandbyController.this.mGoogleRestrictList.iterator();
                while (it.hasNext()) {
                    try {
                        PackageInfo info = pm.getPackageInfo((String) it.next(), 0);
                        AppStandbyController.this.setAppStandbyBucket(info.packageName, UserHandle.getUserId(info.applicationInfo.uid), 40, 1024, elapsedRealtime, false);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                for (PackageInfo info2 : pm.getInstalledPackages(512)) {
                    if ((info2.packageName.contains("google") || info2.packageName.equals("com.android.vending")) && !AppStandbyController.this.mGoogleRestrictList.contains(info2.packageName)) {
                        AppStandbyController.this.setAppStandbyBucket(info2.packageName, UserHandle.getUserId(info2.applicationInfo.uid), 10, 256, elapsedRealtime, false);
                    }
                }
            }
        };
        this.mRestrictRunnable = new Runnable() {
            /* class com.android.server.usage.AppStandbyController.AnonymousClass5 */

            public void run() {
                PackageManager.NameNotFoundException e;
                PackageManager pm = AppStandbyController.this.mContext.getPackageManager();
                long elapsedRealtime = SystemClock.elapsedRealtime();
                int bucket = AppStandbyController.this.mGoogleRestricted ? 40 : 10;
                int reason = AppStandbyController.this.mGoogleRestricted ? 1024 : 256;
                Iterator it = AppStandbyController.this.mGoogleRestrictList.iterator();
                while (it.hasNext()) {
                    try {
                        PackageInfo info = pm.getPackageInfo((String) it.next(), 0);
                        int userId = UserHandle.getUserId(info.applicationInfo.uid);
                        AppStandbyController.this.setAppStandbyBucket(info.packageName, userId, bucket, reason, elapsedRealtime, false);
                        if (userId != 999) {
                            try {
                                AppStandbyController.this.setAppStandbyBucket(info.packageName, 999, bucket, reason, elapsedRealtime, false);
                            } catch (PackageManager.NameNotFoundException e2) {
                                e = e2;
                                e.printStackTrace();
                            }
                        }
                        if (AppStandbyController.this.mMultiUserid != 0) {
                            AppStandbyController.this.setAppStandbyBucket(info.packageName, AppStandbyController.this.mMultiUserid, bucket, reason, elapsedRealtime, false);
                        }
                    } catch (PackageManager.NameNotFoundException e3) {
                        e = e3;
                        e.printStackTrace();
                    }
                }
            }
        };
        this.mMultiUserid = 0;
        this.mUserSwitchReceiver = new BroadcastReceiver() {
            /* class com.android.server.usage.AppStandbyController.AnonymousClass6 */

            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                    int userid = intent.getIntExtra("android.intent.extra.user_handle", 0);
                    if (userid != 0) {
                        AppStandbyController.this.mMultiUserid = userid;
                    }
                    Slog.d(AppStandbyController.TAG, "mUserSwitchReceiver: mMultiUserid=" + AppStandbyController.this.mMultiUserid);
                    AppStandbyController.this.handleUseridChange();
                }
            }
        };
        this.mUseridRunnable = new Runnable() {
            /* class com.android.server.usage.AppStandbyController.AnonymousClass7 */

            public void run() {
                PackageManager pm = AppStandbyController.this.mContext.getPackageManager();
                long elapsedRealtime = SystemClock.elapsedRealtime();
                int bucket = AppStandbyController.this.mGoogleRestricted ? 40 : 10;
                int reason = AppStandbyController.this.mGoogleRestricted ? 1024 : 256;
                Iterator it = AppStandbyController.this.mGoogleRestrictList.iterator();
                while (it.hasNext()) {
                    try {
                        AppStandbyController.this.setAppStandbyBucket(pm.getPackageInfo((String) it.next(), 0).packageName, AppStandbyController.this.mMultiUserid, bucket, reason, elapsedRealtime, false);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        this.mInjector = injector;
        this.mContext = this.mInjector.getContext();
        this.mHandler = new AppStandbyHandler(this.mInjector.getLooper());
        this.mPackageManager = this.mContext.getPackageManager();
        this.mDeviceStateReceiver = new DeviceStateReceiver();
        IntentFilter deviceStates = new IntentFilter("android.os.action.CHARGING");
        deviceStates.addAction("android.os.action.DISCHARGING");
        deviceStates.addAction("android.os.action.DEVICE_IDLE_MODE_CHANGED");
        this.mContext.registerReceiver(this.mDeviceStateReceiver, deviceStates);
        synchronized (this.mAppIdleLock) {
            this.mAppIdleHistory = new AppIdleHistory(this.mInjector.getDataSystemDirectory(), this.mInjector.elapsedRealtime());
        }
        IntentFilter packageFilter = new IntentFilter();
        packageFilter.addAction("android.intent.action.PACKAGE_ADDED");
        packageFilter.addAction("android.intent.action.PACKAGE_CHANGED");
        packageFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        packageFilter.addDataScheme(Settings.ATTR_PACKAGE);
        this.mContext.registerReceiverAsUser(new PackageReceiver(), UserHandle.ALL, packageFilter, null, this.mHandler);
        googleRestrictInit(this.mContext);
        int dozeConfigLocal = getLocalDozeCofigLocked();
        if (dozeConfigLocal == 1) {
            this.mRusAutoPowerSaveEnable = true;
        } else if (dozeConfigLocal == 0) {
            this.mRusAutoPowerSaveEnable = false;
        } else {
            this.mRusAutoPowerSaveEnable = this.mContext.getResources().getBoolean(17891433);
        }
        Slog.d(TAG, "onStart: mRusAutoPowerSaveEnable=" + this.mRusAutoPowerSaveEnable + ", mAppIdleEnabled=" + this.mAppIdleEnabled);
        this.mIsExpVersion = this.mContext.getPackageManager().hasSystemFeature("oppo.version.exp");
    }

    /* access modifiers changed from: package-private */
    public void setAppIdleEnabled(boolean enabled) {
        synchronized (this.mAppIdleLock) {
            if (this.mAppIdleEnabled != enabled) {
                boolean oldParoleState = isParoledOrCharging();
                this.mAppIdleEnabled = enabled;
                if (isParoledOrCharging() != oldParoleState) {
                    postParoleStateChanged();
                }
            }
        }
    }

    public void onBootPhase(int phase) {
        boolean userFileExists;
        this.mInjector.onBootPhase(phase);
        if (phase == 500) {
            Slog.d(TAG, "Setting app idle enabled state");
            SettingsObserver settingsObserver = new SettingsObserver(this.mHandler);
            settingsObserver.registerObserver();
            settingsObserver.updateSettings();
            this.mAppWidgetManager = (AppWidgetManager) this.mContext.getSystemService(AppWidgetManager.class);
            this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService(ConnectivityManager.class);
            this.mPowerManager = (PowerManager) this.mContext.getSystemService(PowerManager.class);
            this.mInjector.registerDisplayListener(this.mDisplayListener, this.mHandler);
            synchronized (this.mAppIdleLock) {
                this.mAppIdleHistory.updateDisplay(isDisplayOn(), this.mInjector.elapsedRealtime());
            }
            this.mSystemServicesReady = true;
            synchronized (this.mAppIdleLock) {
                userFileExists = this.mAppIdleHistory.userFileExists(0);
            }
            if (this.mPendingInitializeDefaults || !userFileExists) {
                initializeDefaultsForSystemApps(0);
            }
            if (this.mPendingOneTimeCheckIdleStates) {
                postOneTimeCheckIdleStates();
            }
        } else if (phase == 1000) {
            setChargingState(this.mInjector.isCharging());
        }
    }

    /* access modifiers changed from: package-private */
    public void reportContentProviderUsage(String authority, String providerPkgName, int userId) {
        String[] packages;
        int i;
        int i2;
        int i3 = userId;
        if (this.mAppIdleEnabled) {
            String[] packages2 = ContentResolver.getSyncAdapterPackagesForAuthorityAsUser(authority, i3);
            long elapsedRealtime = this.mInjector.elapsedRealtime();
            int length = packages2.length;
            int i4 = 0;
            while (i4 < length) {
                String packageName = packages2[i4];
                try {
                    PackageInfo pi = this.mPackageManager.getPackageInfoAsUser(packageName, DumpState.DUMP_DEXOPT, i3);
                    if (pi == null) {
                        i2 = i4;
                        i = length;
                        packages = packages2;
                    } else if (pi.applicationInfo == null) {
                        i2 = i4;
                        i = length;
                        packages = packages2;
                    } else if (!matchGoogleRestrictRule(packageName)) {
                        if (!packageName.equals(providerPkgName)) {
                            synchronized (this.mAppIdleLock) {
                                try {
                                    uploadAABPredictInfoWhenReportEvent(null, packageName, 10, 8, userId);
                                    i2 = i4;
                                    i = length;
                                    packages = packages2;
                                    AppIdleHistory.AppUsageHistory appUsage = this.mAppIdleHistory.reportUsage(packageName, userId, 10, 8, 0, elapsedRealtime + this.mSyncAdapterTimeoutMillis);
                                    maybeInformListeners(packageName, userId, elapsedRealtime, appUsage.currentBucket, appUsage.bucketingReason, false);
                                } catch (Throwable th) {
                                    th = th;
                                    throw th;
                                }
                            }
                        } else {
                            i2 = i4;
                            i = length;
                            packages = packages2;
                        }
                    } else {
                        return;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    i2 = i4;
                    i = length;
                    packages = packages2;
                }
                i4 = i2 + 1;
                i3 = userId;
                length = i;
                packages2 = packages;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void reportExemptedSyncScheduled(String packageName, int userId) {
        long durationMillis;
        int usageReason;
        int bucketToPromote;
        Object obj;
        if (this.mAppIdleEnabled && !matchGoogleRestrictRule(packageName)) {
            if (!this.mInjector.isDeviceIdleMode()) {
                bucketToPromote = 10;
                usageReason = 11;
                durationMillis = this.mExemptedSyncScheduledNonDozeTimeoutMillis;
            } else {
                bucketToPromote = 20;
                usageReason = 12;
                durationMillis = this.mExemptedSyncScheduledDozeTimeoutMillis;
            }
            long elapsedRealtime = this.mInjector.elapsedRealtime();
            Object obj2 = this.mAppIdleLock;
            synchronized (obj2) {
                try {
                    uploadAABPredictInfoWhenReportEvent(null, packageName, bucketToPromote, usageReason, userId);
                    AppIdleHistory.AppUsageHistory appUsage = this.mAppIdleHistory.reportUsage(packageName, userId, bucketToPromote, usageReason, 0, elapsedRealtime + durationMillis);
                    obj = obj2;
                    maybeInformListeners(packageName, userId, elapsedRealtime, appUsage.currentBucket, appUsage.bucketingReason, false);
                } catch (Throwable th) {
                    th = th;
                    throw th;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void reportUnexemptedSyncScheduled(String packageName, int userId) {
        if (this.mAppIdleEnabled) {
            long elapsedRealtime = this.mInjector.elapsedRealtime();
            synchronized (this.mAppIdleLock) {
                if (this.mAppIdleHistory.getAppStandbyBucket(packageName, userId, elapsedRealtime) == 50) {
                    AppIdleHistory.AppUsageHistory appUsage = this.mAppIdleHistory.reportUsage(packageName, userId, 20, 14, 0, elapsedRealtime + this.mUnexemptedSyncScheduledTimeoutMillis);
                    maybeInformListeners(packageName, userId, elapsedRealtime, appUsage.currentBucket, appUsage.bucketingReason, false);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void reportExemptedSyncStart(String packageName, int userId) {
        if (this.mAppIdleEnabled && !matchGoogleRestrictRule(packageName)) {
            long elapsedRealtime = this.mInjector.elapsedRealtime();
            synchronized (this.mAppIdleLock) {
                uploadAABPredictInfoWhenReportEvent(null, packageName, 10, 13, userId);
                AppIdleHistory.AppUsageHistory appUsage = this.mAppIdleHistory.reportUsage(packageName, userId, 10, 13, 0, elapsedRealtime + this.mExemptedSyncStartTimeoutMillis);
                maybeInformListeners(packageName, userId, elapsedRealtime, appUsage.currentBucket, appUsage.bucketingReason, false);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setChargingState(boolean charging) {
        synchronized (this.mAppIdleLock) {
            if (this.mCharging != charging) {
                this.mCharging = charging;
                if (charging) {
                    this.mHandler.sendEmptyMessageDelayed(14, this.mStableChargingThresholdMillis);
                } else {
                    this.mHandler.removeMessages(14);
                    updateChargingStableState();
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateChargingStableState() {
        synchronized (this.mAppIdleLock) {
            if (this.mChargingStable != this.mCharging) {
                this.mChargingStable = this.mCharging;
                postParoleStateChanged();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setAppIdleParoled(boolean paroled) {
        synchronized (this.mAppIdleLock) {
            long now = this.mInjector.currentTimeMillis();
            if (this.mAppIdleTempParoled != paroled) {
                this.mAppIdleTempParoled = paroled;
                if (paroled) {
                    postParoleEndTimeout();
                } else {
                    this.mLastAppIdleParoledTime = now;
                    postNextParoleTimeout(now, false);
                }
                postParoleStateChanged();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isParoledOrCharging() {
        boolean z = true;
        if (!this.mAppIdleEnabled) {
            return true;
        }
        synchronized (this.mAppIdleLock) {
            if (!this.mAppIdleTempParoled) {
                if (!this.mChargingStable) {
                    z = false;
                }
            }
        }
        return z;
    }

    private void postNextParoleTimeout(long now, boolean forced) {
        this.mHandler.removeMessages(6);
        long timeLeft = (this.mLastAppIdleParoledTime + this.mAppIdleParoleIntervalMillis) - now;
        if (forced) {
            timeLeft += this.mAppIdleParoleWindowMillis;
        }
        if (timeLeft < 0) {
            timeLeft = 0;
        }
        this.mHandler.sendEmptyMessageDelayed(6, timeLeft);
    }

    private void postParoleEndTimeout() {
        this.mHandler.removeMessages(7);
        this.mHandler.sendEmptyMessageDelayed(7, this.mAppIdleParoleDurationMillis);
    }

    private void postParoleStateChanged() {
        this.mHandler.removeMessages(9);
        this.mHandler.sendEmptyMessage(9);
    }

    /* access modifiers changed from: package-private */
    public void postCheckIdleStates(int userId) {
        AppStandbyHandler appStandbyHandler = this.mHandler;
        appStandbyHandler.sendMessage(appStandbyHandler.obtainMessage(5, userId, 0));
    }

    /* access modifiers changed from: package-private */
    public void postOneTimeCheckIdleStates() {
        if (this.mInjector.getBootPhase() < 500) {
            this.mPendingOneTimeCheckIdleStates = true;
            return;
        }
        this.mHandler.sendEmptyMessage(10);
        this.mPendingOneTimeCheckIdleStates = false;
    }

    /* access modifiers changed from: package-private */
    public boolean checkIdleStates(int checkUserId) {
        if (!this.mAppIdleEnabled) {
            return false;
        }
        try {
            int[] runningUserIds = this.mInjector.getRunningUserIds();
            if (!(checkUserId == -1 || ArrayUtils.contains(runningUserIds, checkUserId))) {
                return false;
            }
            long elapsedRealtime = this.mInjector.elapsedRealtime();
            for (int userId : runningUserIds) {
                if (checkUserId == -1 || checkUserId == userId) {
                    List<PackageInfo> packages = this.mPackageManager.getInstalledPackagesAsUser(512, userId);
                    int packageCount = packages.size();
                    for (int p = 0; p < packageCount; p++) {
                        PackageInfo pi = packages.get(p);
                        checkAndUpdateStandbyState(pi.packageName, userId, pi.applicationInfo.uid, elapsedRealtime);
                    }
                }
            }
            return true;
        } catch (RemoteException re) {
            throw re.rethrowFromSystemServer();
        }
    }

    /* JADX INFO: Multiple debug info for r3v7 int: [D('newBucket' int), D('reason' int)] */
    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkAndUpdateStandbyState(String packageName, int userId, int uid, long elapsedRealtime) {
        int uid2;
        Object obj;
        int reason;
        int newBucket;
        int reason2;
        if (uid <= 0) {
            try {
                uid2 = this.mPackageManager.getPackageUidAsUser(packageName, userId);
            } catch (PackageManager.NameNotFoundException e) {
                return;
            }
        } else {
            uid2 = uid;
        }
        if (isAppSpecial(packageName, UserHandle.getAppId(uid2), userId)) {
            synchronized (this.mAppIdleLock) {
                this.mAppIdleHistory.setAppStandbyBucket(packageName, userId, elapsedRealtime, 5, 256);
            }
            maybeInformListeners(packageName, userId, elapsedRealtime, 5, 256, false);
            return;
        }
        Object obj2 = this.mAppIdleLock;
        synchronized (obj2) {
            try {
                AppIdleHistory.AppUsageHistory app = this.mAppIdleHistory.getAppUsageHistory(packageName, userId, elapsedRealtime);
                int reason3 = app.bucketingReason;
                int oldMainReason = reason3 & JobPackageTracker.EVENT_STOP_REASON_MASK;
                if (oldMainReason != 1024) {
                    int oldBucket = app.currentBucket;
                    int newBucket2 = Math.max(oldBucket, 10);
                    boolean predictionLate = predictionTimedOut(app, elapsedRealtime);
                    if (oldMainReason == 256 || oldMainReason == 768 || oldMainReason == 512 || predictionLate) {
                        if (predictionLate || app.lastPredictedBucket < 10 || app.lastPredictedBucket > 40) {
                            newBucket2 = getBucketForLocked(packageName, userId, elapsedRealtime);
                            reason3 = 512;
                        } else {
                            newBucket2 = app.lastPredictedBucket;
                            reason3 = UsbTerminalTypes.TERMINAL_TELE_PHONELINE;
                        }
                    }
                    long elapsedTimeAdjusted = this.mAppIdleHistory.getElapsedTime(elapsedRealtime);
                    if (newBucket2 >= 10 && app.bucketActiveTimeoutTime > elapsedTimeAdjusted) {
                        reason = app.bucketingReason;
                        newBucket = 10;
                    } else if (newBucket2 < 20 || app.bucketWorkingSetTimeoutTime <= elapsedTimeAdjusted) {
                        reason = reason3;
                        newBucket = newBucket2;
                    } else {
                        if (20 == oldBucket) {
                            reason2 = app.bucketingReason;
                        } else {
                            reason2 = UsbTerminalTypes.TERMINAL_OUT_LFSPEAKER;
                        }
                        reason = reason2;
                        newBucket = 20;
                    }
                    if (oldBucket >= newBucket) {
                        if (!predictionLate) {
                            obj = obj2;
                        }
                    }
                    this.mAppIdleHistory.setAppStandbyBucket(packageName, userId, elapsedRealtime, newBucket, reason);
                    obj = obj2;
                    maybeInformListeners(packageName, userId, elapsedRealtime, newBucket, reason, false);
                }
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
    }

    private boolean predictionTimedOut(AppIdleHistory.AppUsageHistory app, long elapsedRealtime) {
        return app.lastPredictedTime > 0 && this.mAppIdleHistory.getElapsedTime(elapsedRealtime) - app.lastPredictedTime > this.mPredictionTimeoutMillis;
    }

    private void maybeInformListeners(String packageName, int userId, long elapsedRealtime, int bucket, int reason, boolean userStartedInteracting) {
        synchronized (this.mAppIdleLock) {
            if (this.mAppIdleHistory.shouldInformListeners(packageName, userId, elapsedRealtime, bucket)) {
                this.mHandler.sendMessage(this.mHandler.obtainMessage(3, StandbyUpdateRecord.obtain(packageName, userId, bucket, reason, userStartedInteracting)));
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mAppIdleLock"})
    public int getBucketForLocked(String packageName, int userId, long elapsedRealtime) {
        return THRESHOLD_BUCKETS[this.mAppIdleHistory.getThresholdIndex(packageName, userId, elapsedRealtime, this.mAppStandbyScreenThresholds, this.mAppStandbyElapsedThresholds)];
    }

    /* access modifiers changed from: package-private */
    public void checkParoleTimeout() {
        boolean setParoled = false;
        boolean waitForNetwork = false;
        NetworkInfo activeNetwork = this.mConnectivityManager.getActiveNetworkInfo();
        boolean networkActive = activeNetwork != null && activeNetwork.isConnected();
        synchronized (this.mAppIdleLock) {
            long now = this.mInjector.currentTimeMillis();
            if (!this.mAppIdleTempParoled) {
                long timeSinceLastParole = now - this.mLastAppIdleParoledTime;
                if (timeSinceLastParole <= this.mAppIdleParoleIntervalMillis) {
                    postNextParoleTimeout(now, false);
                } else if (networkActive) {
                    setParoled = true;
                } else if (timeSinceLastParole > this.mAppIdleParoleIntervalMillis + this.mAppIdleParoleWindowMillis) {
                    setParoled = true;
                } else {
                    waitForNetwork = true;
                    postNextParoleTimeout(now, true);
                }
            }
        }
        if (waitForNetwork) {
            this.mConnectivityManager.registerNetworkCallback(this.mNetworkRequest, this.mNetworkCallback);
        }
        if (setParoled) {
            setAppIdleParoled(true);
        }
    }

    private void notifyBatteryStats(String packageName, int userId, boolean idle) {
        try {
            int uid = this.mPackageManager.getPackageUidAsUser(packageName, 8192, userId);
            if (idle) {
                this.mInjector.noteEvent(15, packageName, uid);
            } else {
                this.mInjector.noteEvent(16, packageName, uid);
            }
        } catch (PackageManager.NameNotFoundException | RemoteException e) {
        }
    }

    /* access modifiers changed from: package-private */
    public void onDeviceIdleModeChanged() {
        boolean paroled;
        boolean deviceIdle = this.mPowerManager.isDeviceIdleMode();
        synchronized (this.mAppIdleLock) {
            long timeSinceLastParole = this.mInjector.currentTimeMillis() - this.mLastAppIdleParoledTime;
            if (!deviceIdle && timeSinceLastParole >= this.mAppIdleParoleIntervalMillis) {
                paroled = true;
            } else if (deviceIdle) {
                paroled = false;
            } else {
                return;
            }
            setAppIdleParoled(paroled);
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x01b8  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x01ca  */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x01e6  */
    public void reportEvent(UsageEvents.Event event, long elapsedRealtime, int userId) {
        boolean z;
        boolean previouslyIdle;
        AppIdleHistory.AppUsageHistory appHistory;
        int reason;
        int prevBucket;
        long nextCheckTime;
        int i;
        boolean userStartedInteracting;
        int prevBucket2;
        int subReason;
        if (this.mAppIdleEnabled) {
            synchronized (this.mAppIdleLock) {
                boolean previouslyIdle2 = this.mAppIdleHistory.isIdle(event.mPackage, userId, elapsedRealtime);
                if (event.mEventType == 1 || event.mEventType == 2 || event.mEventType == 6 || event.mEventType == 7 || event.mEventType == 10 || event.mEventType == 14 || event.mEventType == 13 || event.mEventType == 19) {
                    if (matchGoogleRestrictRule(event.mPackage)) {
                        if (event.mEventType == 2) {
                            Runnable runnable = new RestrictRunnable(event.mPackage, userId, 40, 1024, elapsedRealtime, false);
                            this.mGoogleRestrictRunnables.put(Pair.create(event.mPackage, Integer.valueOf(userId)), runnable);
                            this.mHandler.postDelayed(runnable, 60000);
                            return;
                        } else if (event.mEventType == 1) {
                            Runnable runnable2 = this.mGoogleRestrictRunnables.remove(Pair.create(event.mPackage, Integer.valueOf(userId)));
                            if (runnable2 != null) {
                                this.mHandler.removeCallbacks(runnable2);
                            }
                        } else {
                            return;
                        }
                    }
                    AppIdleHistory.AppUsageHistory appHistory2 = this.mAppIdleHistory.getAppUsageHistory(event.mPackage, userId, elapsedRealtime);
                    int prevBucket3 = appHistory2.currentBucket;
                    int prevBucketReason = appHistory2.bucketingReason;
                    int subReason2 = usageEventToSubReason(event.mEventType);
                    int reason2 = subReason2 | 768;
                    if (event.mEventType == 10) {
                        previouslyIdle = previouslyIdle2;
                        reason = reason2;
                        subReason = subReason2;
                        prevBucket = prevBucket3;
                        appHistory = appHistory2;
                        i = 10;
                        z = true;
                    } else if (event.mEventType == 14) {
                        previouslyIdle = previouslyIdle2;
                        reason = reason2;
                        subReason = subReason2;
                        prevBucket = prevBucket3;
                        appHistory = appHistory2;
                        i = 10;
                        z = true;
                    } else {
                        if (event.mEventType == 6) {
                            reason = reason2;
                            uploadAABPredictInfoWhenReportEvent(appHistory2, event.mPackage, 10, subReason2, userId);
                            this.mAppIdleHistory.reportUsage(appHistory2, event.mPackage, 10, subReason2, 0, elapsedRealtime + this.mSystemInteractionTimeoutMillis);
                            previouslyIdle = previouslyIdle2;
                            appHistory = appHistory2;
                            i = 10;
                            prevBucket = prevBucket3;
                            z = true;
                            nextCheckTime = this.mSystemInteractionTimeoutMillis;
                        } else {
                            reason = reason2;
                            if (event.mEventType != 19) {
                                uploadAABPredictInfoWhenReportEvent(appHistory2, event.mPackage, 10, subReason2, userId);
                                appHistory = appHistory2;
                                previouslyIdle = previouslyIdle2;
                                i = 10;
                                prevBucket = prevBucket3;
                                z = true;
                                this.mAppIdleHistory.reportUsage(appHistory2, event.mPackage, 10, subReason2, elapsedRealtime, elapsedRealtime + this.mStrongUsageTimeoutMillis);
                                nextCheckTime = this.mStrongUsageTimeoutMillis;
                            } else if (prevBucket3 == 50) {
                                this.mAppIdleHistory.reportUsage(appHistory2, event.mPackage, 10, subReason2, 0, elapsedRealtime + this.mInitialForegroundServiceStartTimeoutMillis);
                                previouslyIdle = previouslyIdle2;
                                appHistory = appHistory2;
                                i = 10;
                                prevBucket = prevBucket3;
                                z = true;
                                nextCheckTime = this.mInitialForegroundServiceStartTimeoutMillis;
                            } else {
                                return;
                            }
                        }
                        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(11, userId, -1, event.mPackage), nextCheckTime);
                        if (appHistory.currentBucket != i) {
                            prevBucket2 = prevBucket;
                            if (!(prevBucket2 == appHistory.currentBucket || (prevBucketReason & JobPackageTracker.EVENT_STOP_REASON_MASK) == 768)) {
                                userStartedInteracting = z;
                                maybeInformListeners(event.mPackage, userId, elapsedRealtime, appHistory.currentBucket, reason, userStartedInteracting);
                                if (previouslyIdle) {
                                    notifyBatteryStats(event.mPackage, userId, false);
                                }
                            }
                        } else {
                            prevBucket2 = prevBucket;
                        }
                        userStartedInteracting = false;
                        maybeInformListeners(event.mPackage, userId, elapsedRealtime, appHistory.currentBucket, reason, userStartedInteracting);
                        if (previouslyIdle) {
                        }
                    }
                    uploadAABPredictInfoWhenReportEvent(appHistory, event.mPackage, 20, subReason, userId);
                    this.mAppIdleHistory.reportUsage(appHistory, event.mPackage, 20, subReason, 0, elapsedRealtime + this.mNotificationSeenTimeoutMillis);
                    nextCheckTime = this.mNotificationSeenTimeoutMillis;
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(11, userId, -1, event.mPackage), nextCheckTime);
                    if (appHistory.currentBucket != i) {
                    }
                    userStartedInteracting = false;
                    maybeInformListeners(event.mPackage, userId, elapsedRealtime, appHistory.currentBucket, reason, userStartedInteracting);
                    if (previouslyIdle) {
                    }
                }
            }
        }
    }

    private int usageEventToSubReason(int eventType) {
        if (eventType == 1) {
            return 4;
        }
        if (eventType == 2) {
            return 5;
        }
        if (eventType == 6) {
            return 1;
        }
        if (eventType == 7) {
            return 3;
        }
        if (eventType == 10) {
            return 2;
        }
        if (eventType == 19) {
            return 15;
        }
        if (eventType == 13) {
            return 10;
        }
        if (eventType != 14) {
            return 0;
        }
        return 9;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0060, code lost:
        r0 = th;
     */
    public void forceIdleState(String packageName, int userId, boolean idle) {
        int appId;
        int standbyBucket;
        if (this.mAppIdleEnabled && (appId = getAppId(packageName)) >= 0) {
            long elapsedRealtime = this.mInjector.elapsedRealtime();
            boolean previouslyIdle = isAppIdleFiltered(packageName, appId, userId, elapsedRealtime);
            synchronized (this.mAppIdleLock) {
                standbyBucket = this.mAppIdleHistory.setIdle(packageName, userId, idle, elapsedRealtime);
            }
            boolean stillIdle = isAppIdleFiltered(packageName, appId, userId, elapsedRealtime);
            if (previouslyIdle != stillIdle) {
                maybeInformListeners(packageName, userId, elapsedRealtime, standbyBucket, 1024, false);
                if (!stillIdle) {
                    notifyBatteryStats(packageName, userId, idle);
                    return;
                }
                return;
            }
            return;
        }
        return;
        while (true) {
        }
    }

    public void setLastJobRunTime(String packageName, int userId, long elapsedRealtime) {
        synchronized (this.mAppIdleLock) {
            this.mAppIdleHistory.setLastJobRunTime(packageName, userId, elapsedRealtime);
        }
    }

    public long getTimeSinceLastJobRun(String packageName, int userId) {
        long timeSinceLastJobRun;
        long elapsedRealtime = this.mInjector.elapsedRealtime();
        synchronized (this.mAppIdleLock) {
            timeSinceLastJobRun = this.mAppIdleHistory.getTimeSinceLastJobRun(packageName, userId, elapsedRealtime);
        }
        return timeSinceLastJobRun;
    }

    public void onUserRemoved(int userId) {
        synchronized (this.mAppIdleLock) {
            this.mAppIdleHistory.onUserRemoved(userId);
            synchronized (this.mActiveAdminApps) {
                this.mActiveAdminApps.remove(userId);
            }
        }
    }

    private boolean isAppIdleUnfiltered(String packageName, int userId, long elapsedRealtime) {
        boolean isIdle;
        synchronized (this.mAppIdleLock) {
            isIdle = this.mAppIdleHistory.isIdle(packageName, userId, elapsedRealtime);
        }
        return isIdle;
    }

    /* access modifiers changed from: package-private */
    public void addListener(UsageStatsManagerInternal.AppIdleStateChangeListener listener) {
        synchronized (this.mPackageAccessListeners) {
            if (!this.mPackageAccessListeners.contains(listener)) {
                this.mPackageAccessListeners.add(listener);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeListener(UsageStatsManagerInternal.AppIdleStateChangeListener listener) {
        synchronized (this.mPackageAccessListeners) {
            this.mPackageAccessListeners.remove(listener);
        }
    }

    /* access modifiers changed from: package-private */
    public int getAppId(String packageName) {
        try {
            return this.mPackageManager.getApplicationInfo(packageName, 4194816).uid;
        } catch (PackageManager.NameNotFoundException e) {
            return -1;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isAppIdleFilteredOrParoled(String packageName, int userId, long elapsedRealtime, boolean shouldObfuscateInstantApps) {
        if (isParoledOrCharging()) {
            return false;
        }
        if (!shouldObfuscateInstantApps || !this.mInjector.isPackageEphemeral(userId, packageName)) {
            return isAppIdleFiltered(packageName, getAppId(packageName), userId, elapsedRealtime);
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isAppSpecial(String packageName, int appId, int userId) {
        if (packageName == null) {
            return false;
        }
        if (!this.mAppIdleEnabled || appId < 10000 || packageName.equals(PackageManagerService.PLATFORM_PACKAGE_NAME) || !this.mRusAutoPowerSaveEnable) {
            return true;
        }
        if (this.mSystemServicesReady) {
            if (matchGoogleRestrictRule(packageName)) {
                return false;
            }
            try {
                if (this.mInjector.isPowerSaveWhitelistExceptIdleApp(packageName) || isActiveDeviceAdmin(packageName, userId) || isActiveNetworkScorer(packageName)) {
                    return true;
                }
                AppWidgetManager appWidgetManager = this.mAppWidgetManager;
                if ((appWidgetManager != null && this.mInjector.isBoundWidgetPackage(appWidgetManager, packageName, userId)) || isDeviceProvisioningPackage(packageName)) {
                    return true;
                }
            } catch (RemoteException re) {
                throw re.rethrowFromSystemServer();
            }
        }
        if (isCarrierApp(packageName)) {
            return true;
        }
        if (!this.mSystemServicesReady || !isSystemApp(packageName)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean isAppIdleFiltered(String packageName, int appId, int userId, long elapsedRealtime) {
        if (isAppSpecial(packageName, appId, userId)) {
            return false;
        }
        return isAppIdleUnfiltered(packageName, userId, elapsedRealtime);
    }

    /* JADX INFO: Multiple debug info for r2v7 int[]: [D('i' int), D('res' int[])] */
    /* access modifiers changed from: package-private */
    public int[] getIdleUidsForUser(int userId) {
        if (!this.mAppIdleEnabled) {
            return new int[0];
        }
        long elapsedRealtime = this.mInjector.elapsedRealtime();
        try {
            ParceledListSlice<ApplicationInfo> slice = AppGlobals.getPackageManager().getInstalledApplications(0, userId);
            if (slice == null) {
                return new int[0];
            }
            List<ApplicationInfo> apps = slice.getList();
            SparseIntArray uidStates = new SparseIntArray();
            for (int i = apps.size() - 1; i >= 0; i--) {
                ApplicationInfo ai = apps.get(i);
                boolean idle = isAppIdleFiltered(ai.packageName, UserHandle.getAppId(ai.uid), userId, elapsedRealtime);
                int index = uidStates.indexOfKey(ai.uid);
                int i2 = 65536;
                if (index < 0) {
                    int i3 = ai.uid;
                    if (!idle) {
                        i2 = 0;
                    }
                    uidStates.put(i3, i2 + 1);
                } else {
                    int valueAt = uidStates.valueAt(index) + 1;
                    if (!idle) {
                        i2 = 0;
                    }
                    uidStates.setValueAt(index, valueAt + i2);
                }
            }
            int numIdle = 0;
            for (int i4 = uidStates.size() - 1; i4 >= 0; i4--) {
                int value = uidStates.valueAt(i4);
                if ((value & 32767) == (value >> 16)) {
                    numIdle++;
                }
            }
            int[] res = new int[numIdle];
            int numIdle2 = 0;
            for (int i5 = uidStates.size() - 1; i5 >= 0; i5--) {
                int value2 = uidStates.valueAt(i5);
                if ((value2 & 32767) == (value2 >> 16)) {
                    res[numIdle2] = uidStates.keyAt(i5);
                    numIdle2++;
                }
            }
            return res;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /* access modifiers changed from: package-private */
    public void setAppIdleAsync(String packageName, boolean idle, int userId) {
        if (packageName != null && this.mAppIdleEnabled) {
            this.mHandler.obtainMessage(4, userId, idle ? 1 : 0, packageName).sendToTarget();
        }
    }

    public int getAppStandbyBucket(String packageName, int userId, long elapsedRealtime, boolean shouldObfuscateInstantApps) {
        int appStandbyBucket;
        if (!this.mAppIdleEnabled) {
            return 10;
        }
        if (shouldObfuscateInstantApps && this.mInjector.isPackageEphemeral(userId, packageName)) {
            return 10;
        }
        synchronized (this.mAppIdleLock) {
            appStandbyBucket = this.mAppIdleHistory.getAppStandbyBucket(packageName, userId, elapsedRealtime);
        }
        return appStandbyBucket;
    }

    public List<AppStandbyInfo> getAppStandbyBuckets(int userId) {
        ArrayList<AppStandbyInfo> appStandbyBuckets;
        synchronized (this.mAppIdleLock) {
            appStandbyBuckets = this.mAppIdleHistory.getAppStandbyBuckets(userId, this.mAppIdleEnabled);
        }
        return appStandbyBuckets;
    }

    /* access modifiers changed from: package-private */
    public void setAppStandbyBucket(String packageName, int userId, int newBucket, int reason, long elapsedRealtime) {
        setAppStandbyBucket(packageName, userId, newBucket, reason, elapsedRealtime, false);
    }

    /* access modifiers changed from: package-private */
    public void setAppStandbyBucket(String packageName, int userId, int newBucket, int reason, long elapsedRealtime, boolean resetTimeout) {
        Throwable th;
        int reason2;
        int newBucket2;
        synchronized (this.mAppIdleLock) {
            try {
                boolean predicted = false;
                if (this.mInjector.isPackageInstalled(packageName, 0, userId)) {
                    AppIdleHistory.AppUsageHistory app = this.mAppIdleHistory.getAppUsageHistory(packageName, userId, elapsedRealtime);
                    if ((reason & JobPackageTracker.EVENT_STOP_REASON_MASK) == 1280) {
                        predicted = true;
                    }
                    if (app.currentBucket < 10 && !matchGoogleRestrictRule(packageName)) {
                        return;
                    }
                    if ((app.currentBucket != 50 && newBucket != 50) || !predicted) {
                        if ((app.bucketingReason & JobPackageTracker.EVENT_STOP_REASON_MASK) != 1024 || !predicted) {
                            if (!this.mIsExpVersion && (app.bucketingReason & JobPackageTracker.EVENT_STOP_REASON_MASK) != 1280 && app.currentBucket > newBucket && predicted && DEBUG_PANIC) {
                                Slog.d(TAG, "setAppStandbyBucket: predicted. pkg=" + packageName + ", currentBucket=" + app.currentBucket + ", newBucket=" + newBucket);
                            }
                            if (predicted) {
                                long elapsedTimeAdjusted = this.mAppIdleHistory.getElapsedTime(elapsedRealtime);
                                this.mAppIdleHistory.updateLastPrediction(app, elapsedTimeAdjusted, newBucket);
                                if (newBucket > 10 && app.bucketActiveTimeoutTime > elapsedTimeAdjusted) {
                                    uploadAABPredictInfoWhenSet(packageName, 10, newBucket, app.bucketActiveTimeoutTime - elapsedTimeAdjusted);
                                    try {
                                        newBucket2 = 10;
                                        reason2 = app.bucketingReason;
                                        this.mAppIdleHistory.setAppStandbyBucket(packageName, userId, elapsedRealtime, newBucket2, reason2, resetTimeout);
                                        maybeInformListeners(packageName, userId, elapsedRealtime, newBucket2, reason2, false);
                                    } catch (Throwable th2) {
                                        th = th2;
                                        while (true) {
                                            try {
                                                break;
                                            } catch (Throwable th3) {
                                                th = th3;
                                            }
                                        }
                                        throw th;
                                    }
                                } else if (newBucket > 20 && app.bucketWorkingSetTimeoutTime > elapsedTimeAdjusted) {
                                    uploadAABPredictInfoWhenSet(packageName, 20, newBucket, app.bucketWorkingSetTimeoutTime - elapsedTimeAdjusted);
                                    if (app.currentBucket != 20) {
                                        newBucket2 = 20;
                                        reason2 = 775;
                                    } else {
                                        newBucket2 = 20;
                                        reason2 = app.bucketingReason;
                                    }
                                    this.mAppIdleHistory.setAppStandbyBucket(packageName, userId, elapsedRealtime, newBucket2, reason2, resetTimeout);
                                    maybeInformListeners(packageName, userId, elapsedRealtime, newBucket2, reason2, false);
                                }
                            }
                            reason2 = reason;
                            newBucket2 = newBucket;
                            try {
                                this.mAppIdleHistory.setAppStandbyBucket(packageName, userId, elapsedRealtime, newBucket2, reason2, resetTimeout);
                                maybeInformListeners(packageName, userId, elapsedRealtime, newBucket2, reason2, false);
                            } catch (Throwable th4) {
                                th = th4;
                                while (true) {
                                    break;
                                }
                                throw th;
                            }
                        }
                    }
                }
            } catch (Throwable th5) {
                th = th5;
                while (true) {
                    break;
                }
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean isActiveDeviceAdmin(String packageName, int userId) {
        boolean z;
        synchronized (this.mActiveAdminApps) {
            Set<String> adminPkgs = this.mActiveAdminApps.get(userId);
            z = adminPkgs != null && adminPkgs.contains(packageName);
        }
        return z;
    }

    public void addActiveDeviceAdmin(String adminPkg, int userId) {
        synchronized (this.mActiveAdminApps) {
            Set<String> adminPkgs = this.mActiveAdminApps.get(userId);
            if (adminPkgs == null) {
                adminPkgs = new ArraySet();
                this.mActiveAdminApps.put(userId, adminPkgs);
            }
            adminPkgs.add(adminPkg);
        }
    }

    public void setActiveAdminApps(Set<String> adminPkgs, int userId) {
        synchronized (this.mActiveAdminApps) {
            if (adminPkgs == null) {
                this.mActiveAdminApps.remove(userId);
            } else {
                this.mActiveAdminApps.put(userId, adminPkgs);
            }
        }
    }

    public void onAdminDataAvailable() {
        this.mAdminDataAvailableLatch.countDown();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void waitForAdminData() {
        if (this.mContext.getPackageManager().hasSystemFeature("android.software.device_admin")) {
            ConcurrentUtils.waitForCountDownNoInterrupt(this.mAdminDataAvailableLatch, 10000, "Wait for admin data");
        }
    }

    /* access modifiers changed from: package-private */
    public Set<String> getActiveAdminAppsForTest(int userId) {
        Set<String> set;
        synchronized (this.mActiveAdminApps) {
            set = this.mActiveAdminApps.get(userId);
        }
        return set;
    }

    private boolean isDeviceProvisioningPackage(String packageName) {
        String deviceProvisioningPackage = this.mContext.getResources().getString(17039717);
        return deviceProvisioningPackage != null && deviceProvisioningPackage.equals(packageName);
    }

    private boolean isCarrierApp(String packageName) {
        synchronized (this.mAppIdleLock) {
            if (!this.mHaveCarrierPrivilegedApps) {
                fetchCarrierPrivilegedAppsLocked();
            }
            if (this.mCarrierPrivilegedApps == null) {
                return false;
            }
            return this.mCarrierPrivilegedApps.contains(packageName);
        }
    }

    /* access modifiers changed from: package-private */
    public void clearCarrierPrivilegedApps() {
        synchronized (this.mAppIdleLock) {
            this.mHaveCarrierPrivilegedApps = false;
            this.mCarrierPrivilegedApps = null;
        }
    }

    @GuardedBy({"mAppIdleLock"})
    private void fetchCarrierPrivilegedAppsLocked() {
        this.mCarrierPrivilegedApps = ((TelephonyManager) this.mContext.getSystemService(TelephonyManager.class)).getPackagesWithCarrierPrivilegesForAllPhones();
        this.mHaveCarrierPrivilegedApps = true;
    }

    private boolean isActiveNetworkScorer(String packageName) {
        return packageName != null && packageName.equals(this.mInjector.getActiveNetworkScorer());
    }

    /* access modifiers changed from: package-private */
    public void informListeners(String packageName, int userId, int bucket, int reason, boolean userInteraction) {
        boolean idle = bucket >= 40;
        synchronized (this.mPackageAccessListeners) {
            Iterator<UsageStatsManagerInternal.AppIdleStateChangeListener> it = this.mPackageAccessListeners.iterator();
            while (it.hasNext()) {
                UsageStatsManagerInternal.AppIdleStateChangeListener listener = it.next();
                listener.onAppIdleStateChanged(packageName, userId, idle, bucket, reason);
                if (userInteraction) {
                    listener.onUserInteractionStarted(packageName, userId);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void informParoleStateChanged() {
        boolean paroled = isParoledOrCharging();
        synchronized (this.mPackageAccessListeners) {
            Iterator<UsageStatsManagerInternal.AppIdleStateChangeListener> it = this.mPackageAccessListeners.iterator();
            while (it.hasNext()) {
                it.next().onParoleStateChanged(paroled);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void flushToDisk(int userId) {
        synchronized (this.mAppIdleLock) {
            this.mAppIdleHistory.writeAppIdleTimes(userId);
        }
    }

    /* access modifiers changed from: package-private */
    public void flushDurationsToDisk() {
        synchronized (this.mAppIdleLock) {
            this.mAppIdleHistory.writeAppIdleDurations();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isDisplayOn() {
        return this.mInjector.isDefaultDisplayOn();
    }

    /* access modifiers changed from: package-private */
    public void clearAppIdleForPackage(String packageName, int userId) {
        synchronized (this.mAppIdleLock) {
            this.mAppIdleHistory.clearUsage(packageName, userId);
        }
    }

    private class PackageReceiver extends BroadcastReceiver {
        private PackageReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.PACKAGE_ADDED".equals(action) || "android.intent.action.PACKAGE_CHANGED".equals(action)) {
                AppStandbyController.this.clearCarrierPrivilegedApps();
            }
            if (("android.intent.action.PACKAGE_REMOVED".equals(action) || "android.intent.action.PACKAGE_ADDED".equals(action)) && !intent.getBooleanExtra("android.intent.extra.REPLACING", false)) {
                AppStandbyController.this.clearAppIdleForPackage(intent.getData().getSchemeSpecificPart(), getSendingUserId());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void initializeDefaultsForSystemApps(int userId) {
        Object obj;
        Throwable th;
        if (!this.mSystemServicesReady) {
            this.mPendingInitializeDefaults = true;
            return;
        }
        Slog.d(TAG, "Initializing defaults for system apps on user " + userId + ", appIdleEnabled=" + this.mAppIdleEnabled);
        long elapsedRealtime = this.mInjector.elapsedRealtime();
        List<PackageInfo> packages = this.mPackageManager.getInstalledPackagesAsUser(512, userId);
        int packageCount = packages.size();
        Object obj2 = this.mAppIdleLock;
        synchronized (obj2) {
            int i = 0;
            while (i < packageCount) {
                try {
                    PackageInfo pi = packages.get(i);
                    String packageName = pi.packageName;
                    if (pi.applicationInfo == null || !pi.applicationInfo.isSystemApp()) {
                        obj = obj2;
                    } else {
                        obj = obj2;
                        this.mAppIdleHistory.reportUsage(packageName, userId, 10, 6, 0, elapsedRealtime + this.mSystemUpdateUsageTimeoutMillis);
                    }
                    i++;
                    obj2 = obj;
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            }
            this.mAppIdleHistory.writeAppIdleTimes(userId);
        }
    }

    /* access modifiers changed from: package-private */
    public void postReportContentProviderUsage(String name, String packageName, int userId) {
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = name;
        args.arg2 = packageName;
        args.arg3 = Integer.valueOf(userId);
        this.mHandler.obtainMessage(8, args).sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public void postReportSyncScheduled(String packageName, int userId, boolean exempted) {
        this.mHandler.obtainMessage(12, userId, exempted ? 1 : 0, packageName).sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public void postReportExemptedSyncStart(String packageName, int userId) {
        this.mHandler.obtainMessage(13, userId, 0, packageName).sendToTarget();
    }

    /* access modifiers changed from: package-private */
    public void dumpUser(IndentingPrintWriter idpw, int userId, String pkg) {
        synchronized (this.mAppIdleLock) {
            this.mAppIdleHistory.dump(idpw, userId, pkg);
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpState(String[] args, PrintWriter pw) {
        synchronized (this.mAppIdleLock) {
            pw.println("Carrier privileged apps (have=" + this.mHaveCarrierPrivilegedApps + "): " + this.mCarrierPrivilegedApps);
        }
        long now = System.currentTimeMillis();
        pw.println();
        pw.println("Settings:");
        pw.print("  mCheckIdleIntervalMillis=");
        TimeUtils.formatDuration(this.mCheckIdleIntervalMillis, pw);
        pw.println();
        pw.print("  mAppIdleParoleIntervalMillis=");
        TimeUtils.formatDuration(this.mAppIdleParoleIntervalMillis, pw);
        pw.println();
        pw.print("  mAppIdleParoleWindowMillis=");
        TimeUtils.formatDuration(this.mAppIdleParoleWindowMillis, pw);
        pw.println();
        pw.print("  mAppIdleParoleDurationMillis=");
        TimeUtils.formatDuration(this.mAppIdleParoleDurationMillis, pw);
        pw.println();
        pw.print("  mStrongUsageTimeoutMillis=");
        TimeUtils.formatDuration(this.mStrongUsageTimeoutMillis, pw);
        pw.println();
        pw.print("  mNotificationSeenTimeoutMillis=");
        TimeUtils.formatDuration(this.mNotificationSeenTimeoutMillis, pw);
        pw.println();
        pw.print("  mSyncAdapterTimeoutMillis=");
        TimeUtils.formatDuration(this.mSyncAdapterTimeoutMillis, pw);
        pw.println();
        pw.print("  mSystemInteractionTimeoutMillis=");
        TimeUtils.formatDuration(this.mSystemInteractionTimeoutMillis, pw);
        pw.println();
        pw.print("  mInitialForegroundServiceStartTimeoutMillis=");
        TimeUtils.formatDuration(this.mInitialForegroundServiceStartTimeoutMillis, pw);
        pw.println();
        pw.print("  mPredictionTimeoutMillis=");
        TimeUtils.formatDuration(this.mPredictionTimeoutMillis, pw);
        pw.println();
        pw.print("  mExemptedSyncScheduledNonDozeTimeoutMillis=");
        TimeUtils.formatDuration(this.mExemptedSyncScheduledNonDozeTimeoutMillis, pw);
        pw.println();
        pw.print("  mExemptedSyncScheduledDozeTimeoutMillis=");
        TimeUtils.formatDuration(this.mExemptedSyncScheduledDozeTimeoutMillis, pw);
        pw.println();
        pw.print("  mExemptedSyncStartTimeoutMillis=");
        TimeUtils.formatDuration(this.mExemptedSyncStartTimeoutMillis, pw);
        pw.println();
        pw.print("  mUnexemptedSyncScheduledTimeoutMillis=");
        TimeUtils.formatDuration(this.mUnexemptedSyncScheduledTimeoutMillis, pw);
        pw.println();
        pw.print("  mSystemUpdateUsageTimeoutMillis=");
        TimeUtils.formatDuration(this.mSystemUpdateUsageTimeoutMillis, pw);
        pw.println();
        pw.print("  mStableChargingThresholdMillis=");
        TimeUtils.formatDuration(this.mStableChargingThresholdMillis, pw);
        pw.println();
        pw.print("  mRusAutoPowerSaveEnable=");
        pw.print(this.mRusAutoPowerSaveEnable);
        pw.println();
        pw.println();
        pw.print("mAppIdleEnabled=");
        pw.print(this.mAppIdleEnabled);
        pw.print(" mAppIdleTempParoled=");
        pw.print(this.mAppIdleTempParoled);
        pw.print(" mCharging=");
        pw.print(this.mCharging);
        pw.print(" mChargingStable=");
        pw.print(this.mChargingStable);
        pw.print(" mLastAppIdleParoledTime=");
        TimeUtils.formatDuration(now - this.mLastAppIdleParoledTime, pw);
        pw.println();
        pw.print("mScreenThresholds=");
        pw.println(Arrays.toString(this.mAppStandbyScreenThresholds));
        pw.print("mElapsedThresholds=");
        pw.println(Arrays.toString(this.mAppStandbyElapsedThresholds));
        pw.print("mStableChargingThresholdMillis=");
        TimeUtils.formatDuration(this.mStableChargingThresholdMillis, pw);
        pw.println();
    }

    /* access modifiers changed from: package-private */
    public static class Injector {
        private IBatteryStats mBatteryStats;
        int mBootPhase;
        private final Context mContext;
        private IDeviceIdleController mDeviceIdleController;
        private DisplayManager mDisplayManager;
        private final Looper mLooper;
        private PackageManagerInternal mPackageManagerInternal;
        private PowerManager mPowerManager;

        Injector(Context context, Looper looper) {
            this.mContext = context;
            this.mLooper = looper;
        }

        /* access modifiers changed from: package-private */
        public Context getContext() {
            return this.mContext;
        }

        /* access modifiers changed from: package-private */
        public Looper getLooper() {
            return this.mLooper;
        }

        /* access modifiers changed from: package-private */
        public void onBootPhase(int phase) {
            if (phase == 500) {
                this.mDeviceIdleController = IDeviceIdleController.Stub.asInterface(ServiceManager.getService("deviceidle"));
                this.mBatteryStats = IBatteryStats.Stub.asInterface(ServiceManager.getService("batterystats"));
                this.mPackageManagerInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
                this.mDisplayManager = (DisplayManager) this.mContext.getSystemService("display");
                this.mPowerManager = (PowerManager) this.mContext.getSystemService(PowerManager.class);
            }
            this.mBootPhase = phase;
        }

        /* access modifiers changed from: package-private */
        public int getBootPhase() {
            return this.mBootPhase;
        }

        /* access modifiers changed from: package-private */
        public long elapsedRealtime() {
            return SystemClock.elapsedRealtime();
        }

        /* access modifiers changed from: package-private */
        public long currentTimeMillis() {
            return System.currentTimeMillis();
        }

        /* access modifiers changed from: package-private */
        public boolean isAppIdleEnabled() {
            return this.mContext.getResources().getBoolean(17891433) && (Settings.Global.getInt(this.mContext.getContentResolver(), "app_standby_enabled", 1) == 1 && Settings.Global.getInt(this.mContext.getContentResolver(), "adaptive_battery_management_enabled", 1) == 1);
        }

        /* access modifiers changed from: package-private */
        public boolean isCharging() {
            return ((BatteryManager) this.mContext.getSystemService(BatteryManager.class)).isCharging();
        }

        /* access modifiers changed from: package-private */
        public boolean isPowerSaveWhitelistExceptIdleApp(String packageName) throws RemoteException {
            return this.mDeviceIdleController.isPowerSaveWhitelistExceptIdleApp(packageName);
        }

        /* access modifiers changed from: package-private */
        public File getDataSystemDirectory() {
            return Environment.getDataSystemDirectory();
        }

        /* access modifiers changed from: package-private */
        public void noteEvent(int event, String packageName, int uid) throws RemoteException {
            this.mBatteryStats.noteEvent(event, packageName, uid);
        }

        /* access modifiers changed from: package-private */
        public boolean isPackageEphemeral(int userId, String packageName) {
            return this.mPackageManagerInternal.isPackageEphemeral(userId, packageName);
        }

        /* access modifiers changed from: package-private */
        public boolean isPackageInstalled(String packageName, int flags, int userId) {
            return this.mPackageManagerInternal.getPackageUid(packageName, flags, userId) >= 0;
        }

        /* access modifiers changed from: package-private */
        public int[] getRunningUserIds() throws RemoteException {
            return ActivityManager.getService().getRunningUserIds();
        }

        /* access modifiers changed from: package-private */
        public boolean isDefaultDisplayOn() {
            return this.mDisplayManager.getDisplay(0).getState() == 2;
        }

        /* access modifiers changed from: package-private */
        public void registerDisplayListener(DisplayManager.DisplayListener listener, Handler handler) {
            this.mDisplayManager.registerDisplayListener(listener, handler);
        }

        /* access modifiers changed from: package-private */
        public String getActiveNetworkScorer() {
            return ((NetworkScoreManager) this.mContext.getSystemService("network_score")).getActiveScorerPackage();
        }

        public boolean isBoundWidgetPackage(AppWidgetManager appWidgetManager, String packageName, int userId) {
            return appWidgetManager.isBoundWidgetPackage(packageName, userId);
        }

        /* access modifiers changed from: package-private */
        public String getAppIdleSettings() {
            return Settings.Global.getString(this.mContext.getContentResolver(), "app_idle_constants");
        }

        public boolean isDeviceIdleMode() {
            return this.mPowerManager.isDeviceIdleMode();
        }
    }

    /* access modifiers changed from: package-private */
    public class AppStandbyHandler extends Handler {
        AppStandbyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            boolean exempted = true;
            switch (msg.what) {
                case 3:
                    StandbyUpdateRecord r = (StandbyUpdateRecord) msg.obj;
                    AppStandbyController.this.informListeners(r.packageName, r.userId, r.bucket, r.reason, r.isUserInteraction);
                    r.recycle();
                    return;
                case 4:
                    AppStandbyController appStandbyController = AppStandbyController.this;
                    String str = (String) msg.obj;
                    int i = msg.arg1;
                    if (msg.arg2 != 1) {
                        exempted = false;
                    }
                    appStandbyController.forceIdleState(str, i, exempted);
                    return;
                case 5:
                    if (AppStandbyController.this.checkIdleStates(msg.arg1) && AppStandbyController.this.mAppIdleEnabled) {
                        AppStandbyController.this.mHandler.sendMessageDelayed(AppStandbyController.this.mHandler.obtainMessage(5, msg.arg1, 0), AppStandbyController.this.mCheckIdleIntervalMillis);
                        return;
                    }
                    return;
                case 6:
                    AppStandbyController.this.checkParoleTimeout();
                    return;
                case 7:
                    AppStandbyController.this.setAppIdleParoled(false);
                    return;
                case 8:
                    SomeArgs args = (SomeArgs) msg.obj;
                    AppStandbyController.this.reportContentProviderUsage((String) args.arg1, (String) args.arg2, ((Integer) args.arg3).intValue());
                    args.recycle();
                    return;
                case 9:
                    AppStandbyController.this.informParoleStateChanged();
                    return;
                case 10:
                    AppStandbyController.this.mHandler.removeMessages(10);
                    AppStandbyController.this.waitForAdminData();
                    AppStandbyController.this.checkIdleStates(-1);
                    return;
                case 11:
                    AppStandbyController.this.checkAndUpdateStandbyState((String) msg.obj, msg.arg1, msg.arg2, AppStandbyController.this.mInjector.elapsedRealtime());
                    return;
                case 12:
                    if (msg.arg1 <= 0) {
                        exempted = false;
                    }
                    if (exempted) {
                        AppStandbyController.this.reportExemptedSyncScheduled((String) msg.obj, msg.arg1);
                        return;
                    } else {
                        AppStandbyController.this.reportUnexemptedSyncScheduled((String) msg.obj, msg.arg1);
                        return;
                    }
                case 13:
                    AppStandbyController.this.reportExemptedSyncStart((String) msg.obj, msg.arg1);
                    return;
                case 14:
                    AppStandbyController.this.updateChargingStableState();
                    return;
                default:
                    super.handleMessage(msg);
                    return;
            }
        }
    }

    private class DeviceStateReceiver extends BroadcastReceiver {
        private DeviceStateReceiver() {
        }

        /* JADX WARNING: Removed duplicated region for block: B:17:0x003c  */
        /* JADX WARNING: Removed duplicated region for block: B:21:0x004d  */
        public void onReceive(Context context, Intent intent) {
            char c;
            String action = intent.getAction();
            int hashCode = action.hashCode();
            if (hashCode != -54942926) {
                if (hashCode != 870701415) {
                    if (hashCode == 948344062 && action.equals("android.os.action.CHARGING")) {
                        c = 0;
                        if (c != 0) {
                            AppStandbyController.this.setChargingState(true);
                            return;
                        } else if (c == 1) {
                            AppStandbyController.this.setChargingState(false);
                            return;
                        } else if (c == 2) {
                            AppStandbyController.this.onDeviceIdleModeChanged();
                            return;
                        } else {
                            return;
                        }
                    }
                } else if (action.equals("android.os.action.DEVICE_IDLE_MODE_CHANGED")) {
                    c = 2;
                    if (c != 0) {
                    }
                }
            } else if (action.equals("android.os.action.DISCHARGING")) {
                c = 1;
                if (c != 0) {
                }
            }
            c = 65535;
            if (c != 0) {
            }
        }
    }

    /* access modifiers changed from: private */
    public class SettingsObserver extends ContentObserver {
        public static final long DEFAULT_EXEMPTED_SYNC_SCHEDULED_DOZE_TIMEOUT = 14400000;
        public static final long DEFAULT_EXEMPTED_SYNC_SCHEDULED_NON_DOZE_TIMEOUT = 600000;
        public static final long DEFAULT_EXEMPTED_SYNC_START_TIMEOUT = 600000;
        public static final long DEFAULT_INITIAL_FOREGROUND_SERVICE_START_TIMEOUT = 1800000;
        public static final long DEFAULT_NOTIFICATION_TIMEOUT = 43200000;
        public static final long DEFAULT_STABLE_CHARGING_THRESHOLD = 600000;
        public static final long DEFAULT_STRONG_USAGE_TIMEOUT = 3600000;
        public static final long DEFAULT_SYNC_ADAPTER_TIMEOUT = 600000;
        public static final long DEFAULT_SYSTEM_INTERACTION_TIMEOUT = 600000;
        public static final long DEFAULT_SYSTEM_UPDATE_TIMEOUT = 7200000;
        public static final long DEFAULT_UNEXEMPTED_SYNC_SCHEDULED_TIMEOUT = 600000;
        private static final String KEY_ELAPSED_TIME_THRESHOLDS = "elapsed_thresholds";
        private static final String KEY_EXEMPTED_SYNC_SCHEDULED_DOZE_HOLD_DURATION = "exempted_sync_scheduled_d_duration";
        private static final String KEY_EXEMPTED_SYNC_SCHEDULED_NON_DOZE_HOLD_DURATION = "exempted_sync_scheduled_nd_duration";
        private static final String KEY_EXEMPTED_SYNC_START_HOLD_DURATION = "exempted_sync_start_duration";
        @Deprecated
        private static final String KEY_IDLE_DURATION = "idle_duration2";
        @Deprecated
        private static final String KEY_IDLE_DURATION_OLD = "idle_duration";
        private static final String KEY_INITIAL_FOREGROUND_SERVICE_START_HOLD_DURATION = "initial_foreground_service_start_duration";
        private static final String KEY_NOTIFICATION_SEEN_HOLD_DURATION = "notification_seen_duration";
        private static final String KEY_PAROLE_DURATION = "parole_duration";
        private static final String KEY_PAROLE_INTERVAL = "parole_interval";
        private static final String KEY_PAROLE_WINDOW = "parole_window";
        private static final String KEY_PREDICTION_TIMEOUT = "prediction_timeout";
        private static final String KEY_SCREEN_TIME_THRESHOLDS = "screen_thresholds";
        private static final String KEY_STABLE_CHARGING_THRESHOLD = "stable_charging_threshold";
        private static final String KEY_STRONG_USAGE_HOLD_DURATION = "strong_usage_duration";
        private static final String KEY_SYNC_ADAPTER_HOLD_DURATION = "sync_adapter_duration";
        private static final String KEY_SYSTEM_INTERACTION_HOLD_DURATION = "system_interaction_duration";
        private static final String KEY_SYSTEM_UPDATE_HOLD_DURATION = "system_update_usage_duration";
        private static final String KEY_UNEXEMPTED_SYNC_SCHEDULED_HOLD_DURATION = "unexempted_sync_scheduled_duration";
        @Deprecated
        private static final String KEY_WALLCLOCK_THRESHOLD = "wallclock_threshold";
        private final KeyValueListParser mParser = new KeyValueListParser(',');

        SettingsObserver(Handler handler) {
            super(handler);
        }

        /* access modifiers changed from: package-private */
        public void registerObserver() {
            ContentResolver cr = AppStandbyController.this.mContext.getContentResolver();
            cr.registerContentObserver(Settings.Global.getUriFor("app_idle_constants"), false, this);
            cr.registerContentObserver(Settings.Global.getUriFor("app_standby_enabled"), false, this);
            cr.registerContentObserver(Settings.Global.getUriFor("adaptive_battery_management_enabled"), false, this);
        }

        public void onChange(boolean selfChange) {
            updateSettings();
            AppStandbyController.this.postOneTimeCheckIdleStates();
        }

        /* access modifiers changed from: package-private */
        public void updateSettings() {
            try {
                this.mParser.setString(AppStandbyController.this.mInjector.getAppIdleSettings());
            } catch (IllegalArgumentException e) {
                Slog.e(AppStandbyController.TAG, "Bad value for app idle settings: " + e.getMessage());
            }
            synchronized (AppStandbyController.this.mAppIdleLock) {
                AppStandbyController.this.mAppIdleParoleIntervalMillis = this.mParser.getDurationMillis(KEY_PAROLE_INTERVAL, 86400000);
                AppStandbyController.this.mAppIdleParoleWindowMillis = this.mParser.getDurationMillis(KEY_PAROLE_WINDOW, (long) DEFAULT_SYSTEM_UPDATE_TIMEOUT);
                AppStandbyController.this.mAppIdleParoleDurationMillis = this.mParser.getDurationMillis(KEY_PAROLE_DURATION, 600000);
                String screenThresholdsValue = this.mParser.getString(KEY_SCREEN_TIME_THRESHOLDS, (String) null);
                AppStandbyController.this.mAppStandbyScreenThresholds = parseLongArray(screenThresholdsValue, AppStandbyController.SCREEN_TIME_THRESHOLDS);
                String elapsedThresholdsValue = this.mParser.getString(KEY_ELAPSED_TIME_THRESHOLDS, (String) null);
                AppStandbyController.this.mAppStandbyElapsedThresholds = parseLongArray(elapsedThresholdsValue, AppStandbyController.ELAPSED_TIME_THRESHOLDS);
                AppStandbyController.this.mCheckIdleIntervalMillis = Math.min(AppStandbyController.this.mAppStandbyElapsedThresholds[1] / 4, 14400000L);
                AppStandbyController.this.mStrongUsageTimeoutMillis = this.mParser.getDurationMillis(KEY_STRONG_USAGE_HOLD_DURATION, 3600000);
                AppStandbyController.this.mNotificationSeenTimeoutMillis = this.mParser.getDurationMillis(KEY_NOTIFICATION_SEEN_HOLD_DURATION, 43200000);
                AppStandbyController.this.mSystemUpdateUsageTimeoutMillis = this.mParser.getDurationMillis(KEY_SYSTEM_UPDATE_HOLD_DURATION, (long) DEFAULT_SYSTEM_UPDATE_TIMEOUT);
                AppStandbyController.this.mPredictionTimeoutMillis = this.mParser.getDurationMillis(KEY_PREDICTION_TIMEOUT, 43200000);
                AppStandbyController.this.mSyncAdapterTimeoutMillis = this.mParser.getDurationMillis(KEY_SYNC_ADAPTER_HOLD_DURATION, 600000);
                AppStandbyController.this.mExemptedSyncScheduledNonDozeTimeoutMillis = this.mParser.getDurationMillis(KEY_EXEMPTED_SYNC_SCHEDULED_NON_DOZE_HOLD_DURATION, 600000);
                AppStandbyController.this.mExemptedSyncScheduledDozeTimeoutMillis = this.mParser.getDurationMillis(KEY_EXEMPTED_SYNC_SCHEDULED_DOZE_HOLD_DURATION, 14400000);
                AppStandbyController.this.mExemptedSyncStartTimeoutMillis = this.mParser.getDurationMillis(KEY_EXEMPTED_SYNC_START_HOLD_DURATION, 600000);
                AppStandbyController.this.mUnexemptedSyncScheduledTimeoutMillis = this.mParser.getDurationMillis(KEY_EXEMPTED_SYNC_SCHEDULED_DOZE_HOLD_DURATION, 600000);
                AppStandbyController.this.mSystemInteractionTimeoutMillis = this.mParser.getDurationMillis(KEY_SYSTEM_INTERACTION_HOLD_DURATION, 600000);
                AppStandbyController.this.mInitialForegroundServiceStartTimeoutMillis = this.mParser.getDurationMillis(KEY_INITIAL_FOREGROUND_SERVICE_START_HOLD_DURATION, 1800000);
                AppStandbyController.this.mStableChargingThresholdMillis = this.mParser.getDurationMillis(KEY_STABLE_CHARGING_THRESHOLD, 600000);
            }
            AppStandbyController appStandbyController = AppStandbyController.this;
            appStandbyController.setAppIdleEnabled(appStandbyController.mInjector.isAppIdleEnabled());
        }

        /* access modifiers changed from: package-private */
        public long[] parseLongArray(String values, long[] defaults) {
            if (values == null || values.isEmpty()) {
                return defaults;
            }
            String[] thresholds = values.split(SliceClientPermissions.SliceAuthority.DELIMITER);
            if (thresholds.length != AppStandbyController.THRESHOLD_BUCKETS.length) {
                return defaults;
            }
            long[] array = new long[AppStandbyController.THRESHOLD_BUCKETS.length];
            for (int i = 0; i < AppStandbyController.THRESHOLD_BUCKETS.length; i++) {
                try {
                    if (!thresholds[i].startsWith("P")) {
                        if (!thresholds[i].startsWith("p")) {
                            array[i] = Long.parseLong(thresholds[i]);
                        }
                    }
                    array[i] = Duration.parse(thresholds[i]).toMillis();
                } catch (NumberFormatException | DateTimeParseException e) {
                    return defaults;
                }
            }
            return array;
        }
    }

    public boolean getRunAutoPowerSaveValue() {
        return this.mRusAutoPowerSaveEnable;
    }

    public void setRunAutoPowerSaveValue(boolean enable) {
        this.mRusAutoPowerSaveEnable = enable;
    }

    private int getLocalDozeCofigLocked() {
        int res = -1;
        File file = new File(DOZE_CONFIG_XML_PATH);
        if (!file.exists()) {
            return -1;
        }
        FileReader xmlReader = null;
        try {
            XmlPullParser parser = Xml.newPullParser();
            xmlReader = new FileReader(file);
            parser.setInput(xmlReader);
            res = parseDozeCofigXml(parser);
            try {
                xmlReader.close();
            } catch (IOException e) {
                Slog.w(TAG, "getDozeCofigLocked: Got execption close xmlReader. ", e);
            }
        } catch (Exception e2) {
            Slog.w(TAG, "getDozeCofigLocked: Got execption. ", e2);
            if (xmlReader != null) {
                xmlReader.close();
            }
        } catch (Throwable th) {
            if (xmlReader != null) {
                try {
                    xmlReader.close();
                } catch (IOException e3) {
                    Slog.w(TAG, "getDozeCofigLocked: Got execption close xmlReader. ", e3);
                }
            }
            throw th;
        }
        return res;
    }

    private int parseDozeCofigXml(XmlPullParser parser) {
        int res = -1;
        try {
            int eventType = parser.getEventType();
            while (eventType != 1) {
                if (eventType != 0) {
                    if (eventType == 2) {
                        String strName = parser.getName();
                        parser.next();
                        String strText = parser.getText();
                        if (TAG_AUTO_POWER_SAVE_MODES_ENABLED.equals(strName)) {
                            try {
                                if (Boolean.parseBoolean(strText)) {
                                    res = 1;
                                } else {
                                    res = 0;
                                }
                            } catch (NumberFormatException e) {
                                Slog.w(TAG, "parseDozeCofigXml NumberFormatException.", e);
                            }
                            if (OPPODEBUG) {
                                Slog.d(TAG, "parseDozeCofigXml: res=" + res);
                            }
                        }
                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e2) {
            Slog.w(TAG, "parseDozeCofigXml: Got execption. ", e2);
        }
        if (OPPODEBUG) {
            Slog.d(TAG, "parseDozeCofigXml: res=" + res);
        }
        return res;
    }

    /* access modifiers changed from: private */
    public class RestrictRunnable implements Runnable {
        int bucket;
        long elapsedRealtime;
        String pkg;
        int reason;
        boolean resetTimeout;
        int userId;

        public RestrictRunnable(String pkg2, int userId2, int bucket2, int reason2, long elapsedRealtime2, boolean resetTimeout2) {
            this.pkg = pkg2;
            this.userId = userId2;
            this.bucket = bucket2;
            this.reason = reason2;
            this.elapsedRealtime = elapsedRealtime2;
            this.resetTimeout = resetTimeout2;
        }

        public void run() {
            AppStandbyController.this.mGoogleRestrictRunnables.remove(this);
            if (AppStandbyController.this.matchGoogleRestrictRule(this.pkg) || 40 != this.bucket) {
                AppStandbyController.this.setAppStandbyBucket(this.pkg, this.userId, this.bucket, this.reason, this.elapsedRealtime, this.resetTimeout);
            } else if (AppStandbyController.OPPODEBUG) {
                Slog.d(AppStandbyController.TAG, "RestrictRunnable: not match restrict. pkg=" + this.pkg);
            }
        }
    }

    private void googleRestrictInit(Context context) {
        context.registerReceiver(this.mGoogleRestriction, new IntentFilter("oppo.intent.action.google_restrict_change"), "oppo.permission.OPPO_COMPONENT_SAFE", this.mHandler);
        this.mContext.registerReceiverAsUser(this.mUserSwitchReceiver, UserHandle.ALL, new IntentFilter("android.intent.action.USER_SWITCHED"), null, this.mHandler);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean matchGoogleRestrictRule(String pkg) {
        return this.mGoogleRestricted && this.mGoogleRestrictList.contains(pkg);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWhitelistChange() {
        this.mHandler.removeCallbacks(this.mListRunnable);
        this.mHandler.postDelayed(this.mListRunnable, 10000);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRestrictChange() {
        this.mHandler.removeCallbacks(this.mRestrictRunnable);
        this.mHandler.postDelayed(this.mRestrictRunnable, 10000);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUseridChange() {
        this.mHandler.removeCallbacks(this.mUseridRunnable);
        this.mHandler.postDelayed(this.mUseridRunnable, 2000);
    }

    private void uploadAABPredictInfoWhenReportEvent(AppIdleHistory.AppUsageHistory appUsageHistory, String pkgname, int newBucket, int reason, int userId) {
        long elapsedRealtime = this.mInjector.elapsedRealtime();
        if (appUsageHistory == null) {
            appUsageHistory = this.mAppIdleHistory.getAppUsageHistory(pkgname, userId, elapsedRealtime);
        }
        if ((appUsageHistory.bucketingReason & JobPackageTracker.EVENT_STOP_REASON_MASK) == 1280 && (appUsageHistory.bucketingReason & 255) != 1) {
            long timeAfterPredict = this.mAppIdleHistory.getElapsedTime(elapsedRealtime) - appUsageHistory.lastPredictedTime;
            if (isBucketInExpectedPrediction(appUsageHistory.currentBucket, timeAfterPredict)) {
                HashMap<String, String> map = new HashMap<>();
                map.put("pkgname", pkgname);
                map.put("predictBucket", String.valueOf(appUsageHistory.currentBucket));
                map.put("timeAfterPredict", String.valueOf(timeAfterPredict));
                map.put("newBucket", String.valueOf(newBucket));
                map.put(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY, String.valueOf(reason));
                map.put("version", "2.0");
                OppoStatistics.onCommon(this.mContext, DcsFingerprintStatisticsUtil.SYSTEM_APP_TAG, "aab_predict_info", map, false);
            }
        }
    }

    private void uploadAABPredictInfoWhenSet(String pkgname, int activeBucket, int predictBucket, long timeRemainder) {
        this.mInjector.elapsedRealtime();
        HashMap<String, String> map = new HashMap<>();
        map.put("pkgname", pkgname);
        map.put("predictBucket", String.valueOf(predictBucket));
        map.put("activeBucketRemainder", String.valueOf(timeRemainder));
        map.put("activeBucket", String.valueOf(activeBucket));
        map.put(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY, "set_app_standby");
        map.put("version", "2.0");
        OppoStatistics.onCommon(this.mContext, DcsFingerprintStatisticsUtil.SYSTEM_APP_TAG, "aab_predict_info", map, false);
    }

    private boolean isBucketInExpectedPrediction(int predictedBucket, long timeAfterPredict) {
        if (predictedBucket <= 10) {
            return true;
        }
        boolean result = true;
        if (predictedBucket <= 20) {
            if (timeAfterPredict > 10800000) {
                result = false;
            }
            return result;
        } else if (predictedBucket <= 30) {
            if (timeAfterPredict > 21600000) {
                result = false;
            }
            return result;
        } else if (predictedBucket > 40) {
            return false;
        } else {
            if (timeAfterPredict > 86400000) {
                result = false;
            }
            return result;
        }
    }

    private boolean isSystemApp(String pkgName) {
        if (pkgName.contains(".coloros.") || pkgName.contains(".oppo.") || pkgName.contains(".nearme.")) {
            return true;
        }
        PackageManager packageManager = this.mContext.getPackageManager();
        if (packageManager == null) {
            return false;
        }
        ApplicationInfo appInfo = null;
        try {
            appInfo = packageManager.getApplicationInfo(pkgName, 8192);
        } catch (PackageManager.NameNotFoundException e) {
        }
        if (appInfo == null || (appInfo.flags & 1) == 0) {
            return false;
        }
        return true;
    }
}
