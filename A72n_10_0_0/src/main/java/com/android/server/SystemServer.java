package com.android.server;

import android.app.ActivityThread;
import android.app.INotificationManager;
import android.app.usage.UsageStatsManagerInternal;
import android.common.OppoFeatureCache;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.sqlite.SQLiteCompatibilityWalFlags;
import android.database.sqlite.SQLiteGlobal;
import android.hardware.display.DisplayManagerInternal;
import android.net.IConnectivityManager;
import android.net.INetd;
import android.net.NetworkStackClient;
import android.operator.OppoOperatorManager;
import android.os.BaseBundle;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.FactoryTest;
import android.os.FileUtils;
import android.os.IBinder;
import android.os.IIncidentManager;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.Process;
import android.os.ServiceManager;
import android.os.StrictMode;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.storage.IStorageManager;
import android.provider.DeviceConfig;
import android.provider.Settings;
import android.sysprop.VoldProperties;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.util.Slog;
import android.util.TimingsTraceLog;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodSystemProperty;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.os.BinderInternal;
import com.android.internal.util.ConcurrentUtils;
import com.android.internal.widget.ILockSettings;
import com.android.server.BatteryService;
import com.android.server.BinderCallsStatsService;
import com.android.server.LooperStatsService;
import com.android.server.NetworkScoreService;
import com.android.server.alipay.AlipayService;
import com.android.server.am.ActivityManagerService;
import com.android.server.appbinding.AppBindingService;
import com.android.server.attention.AttentionManagerService;
import com.android.server.audio.AudioService;
import com.android.server.biometrics.BiometricService;
import com.android.server.biometrics.face.FaceService;
import com.android.server.biometrics.fingerprint.FingerprintService;
import com.android.server.biometrics.iris.IrisService;
import com.android.server.broadcastradio.BroadcastRadioService;
import com.android.server.camera.CameraServiceProxy;
import com.android.server.clipboard.ClipboardService;
import com.android.server.connectivity.IpConnectivityMetrics;
import com.android.server.contentcapture.ContentCaptureManagerInternal;
import com.android.server.coverage.CoverageService;
import com.android.server.devicepolicy.DevicePolicyManagerService;
import com.android.server.display.DisplayManagerService;
import com.android.server.display.color.ColorDisplayService;
import com.android.server.dreams.DreamManagerService;
import com.android.server.emergency.EmergencyAffordanceService;
import com.android.server.engineer.OppoEngineerService;
import com.android.server.gpu.GpuService;
import com.android.server.hdmi.HdmiControlService;
import com.android.server.incident.IncidentCompanionService;
import com.android.server.input.InputManagerService;
import com.android.server.inputmethod.InputMethodManagerService;
import com.android.server.inputmethod.MultiClientInputMethodManagerService;
import com.android.server.job.JobSchedulerService;
import com.android.server.lights.OppoLightsService;
import com.android.server.mdmcrsh.OppomodemService;
import com.android.server.media.MediaResourceMonitorService;
import com.android.server.media.MediaRouterService;
import com.android.server.media.MediaSessionService;
import com.android.server.media.projection.MediaProjectionManagerService;
import com.android.server.net.NetworkPolicyManagerService;
import com.android.server.net.NetworkStatsService;
import com.android.server.net.watchlist.NetworkWatchlistService;
import com.android.server.notification.NotificationManagerService;
import com.android.server.oemlock.OemLockService;
import com.android.server.om.OverlayManagerService;
import com.android.server.operator.OppoOperatorManagerService;
import com.android.server.oppo.CabcService;
import com.android.server.oppo.HypnusService;
import com.android.server.oppo.OppoCustomizeService;
import com.android.server.oppo.TorchManagerService;
import com.android.server.os.BugreportManagerService;
import com.android.server.os.DeviceIdentifiersPolicyService;
import com.android.server.os.SchedulingPolicyService;
import com.android.server.pm.BackgroundDexOptService;
import com.android.server.pm.CrossProfileAppsService;
import com.android.server.pm.DynamicCodeLoggingService;
import com.android.server.pm.Installer;
import com.android.server.pm.LauncherAppsService;
import com.android.server.pm.OtaDexoptService;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.ShortcutService;
import com.android.server.pm.UserManagerService;
import com.android.server.policy.PermissionPolicyService;
import com.android.server.policy.role.LegacyRoleResolutionPolicy;
import com.android.server.power.PowerManagerService;
import com.android.server.power.ShutdownThread;
import com.android.server.power.ThermalManagerService;
import com.android.server.restrictions.RestrictionsManagerService;
import com.android.server.role.RoleManagerService;
import com.android.server.rollback.RollbackManagerService;
import com.android.server.security.KeyAttestationApplicationIdProviderService;
import com.android.server.security.KeyChainSystemService;
import com.android.server.signedconfig.SignedConfigService;
import com.android.server.soundtrigger.SoundTriggerService;
import com.android.server.stats.StatsCompanionService;
import com.android.server.statusbar.ColorStatusBarManagerService;
import com.android.server.storage.DeviceStorageMonitorService;
import com.android.server.telecom.TelecomLoaderService;
import com.android.server.testharness.TestHarnessModeService;
import com.android.server.textclassifier.TextClassificationManagerService;
import com.android.server.textservices.TextServicesManagerService;
import com.android.server.trust.TrustManagerService;
import com.android.server.tv.TvInputManagerService;
import com.android.server.tv.TvRemoteService;
import com.android.server.twilight.TwilightService;
import com.android.server.uri.UriGrantsManagerService;
import com.android.server.usage.UsageStatsService;
import com.android.server.utils.PriorityDump;
import com.android.server.vr.VrManagerService;
import com.android.server.webkit.WebViewUpdateService;
import com.android.server.wm.ActivityTaskManagerService;
import com.android.server.wm.WindowManagerGlobalLock;
import com.android.server.wm.WindowManagerService;
import com.mediatek.server.MtkSystemServer;
import com.oppo.phoenix.Phoenix;
import com.oppo.theme.IColorThemeStyle;
import dalvik.system.PathClassLoader;
import dalvik.system.VMRuntime;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Timer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

public final class SystemServer {
    private static final String ACCESSIBILITY_MANAGER_SERVICE_CLASS = "com.android.server.accessibility.AccessibilityManagerService$Lifecycle";
    private static final String ACCOUNT_SERVICE_CLASS = "com.android.server.accounts.AccountManagerService$Lifecycle";
    private static final String ADB_SERVICE_CLASS = "com.android.server.adb.AdbService$Lifecycle";
    private static final String APPWIDGET_SERVICE_CLASS = "com.android.server.appwidget.AppWidgetService";
    private static final String APP_PREDICTION_MANAGER_SERVICE_CLASS = "com.android.server.appprediction.AppPredictionManagerService";
    private static final String AUTO_FILL_MANAGER_SERVICE_CLASS = "com.android.server.autofill.AutofillManagerService";
    private static final String BACKUP_MANAGER_SERVICE_CLASS = "com.android.server.backup.BackupManagerService$Lifecycle";
    private static final String BLOCK_MAP_FILE = "/cache/recovery/block.map";
    private static final TimingsTraceLog BOOT_TIMINGS_TRACE_LOG = new TimingsTraceLog(SYSTEM_SERVER_TIMING_TAG, 524288);
    private static final String CAR_SERVICE_HELPER_SERVICE_CLASS = "com.android.internal.car.CarServiceHelperService";
    private static final String COMPANION_DEVICE_MANAGER_SERVICE_CLASS = "com.android.server.companion.CompanionDeviceManagerService";
    private static final String CONTENT_CAPTURE_MANAGER_SERVICE_CLASS = "com.android.server.contentcapture.ContentCaptureManagerService";
    private static final String CONTENT_SERVICE_CLASS = "com.android.server.content.ContentService$Lifecycle";
    private static final String CONTENT_SUGGESTIONS_SERVICE_CLASS = "com.android.server.contentsuggestions.ContentSuggestionsManagerService";
    private static final int DEFAULT_SYSTEM_THEME = OppoFeatureCache.getOrCreate(IColorThemeStyle.DEFAULT, new Object[0]).getSystemThemeStyle(16974847);
    private static final long EARLIEST_SUPPORTED_TIME = 86400000;
    private static final String ENCRYPTED_STATE = "1";
    private static final String ENCRYPTING_STATE = "trigger_restart_min_framework";
    private static final String ETHERNET_SERVICE_CLASS = "com.android.server.ethernet.EthernetService";
    private static final String GSI_RUNNING_PROP = "ro.gsid.image_running";
    private static final String IOT_SERVICE_CLASS = "com.android.things.server.IoTSystemService";
    private static final String JOB_SCHEDULER_SERVICE_CLASS = "com.android.server.job.JobSchedulerService";
    private static final String LOCK_SETTINGS_SERVICE_CLASS = "com.android.server.locksettings.LockSettingsService$Lifecycle";
    private static final String LOWPAN_SERVICE_CLASS = "com.android.server.lowpan.LowpanService";
    private static final String MIDI_SERVICE_CLASS = "com.android.server.midi.MidiService$Lifecycle";
    private static final String PERSISTENT_DATA_BLOCK_PROP = "ro.frp.pst";
    private static final String PRINT_MANAGER_SERVICE_CLASS = "com.android.server.print.PrintManagerService";
    private static final String SEARCH_MANAGER_SERVICE_CLASS = "com.android.server.search.SearchManagerService$Lifecycle";
    private static final String SLICE_MANAGER_SERVICE_CLASS = "com.android.server.slice.SliceManagerService$Lifecycle";
    private static final long SLOW_DELIVERY_THRESHOLD_MS = 200;
    private static final long SLOW_DISPATCH_THRESHOLD_MS = 100;
    private static final long SNAPSHOT_INTERVAL = 3600000;
    private static final String START_HIDL_SERVICES = "StartHidlServices";
    private static final String START_SENSOR_SERVICE = "StartSensorService";
    private static final String STORAGE_MANAGER_SERVICE_CLASS = "com.android.server.StorageManagerService$Lifecycle";
    private static final String STORAGE_STATS_SERVICE_CLASS = "com.android.server.usage.StorageStatsService$Lifecycle";
    private static final String SYSPROP_START_COUNT = "sys.system_server.start_count";
    private static final String SYSPROP_START_ELAPSED = "sys.system_server.start_elapsed";
    private static final String SYSPROP_START_UPTIME = "sys.system_server.start_uptime";
    private static final String SYSTEM_CAPTIONS_MANAGER_SERVICE_CLASS = "com.android.server.systemcaptions.SystemCaptionsManagerService";
    private static final String SYSTEM_SERVER_TIMING_ASYNC_TAG = "SystemServerTimingAsync";
    private static final String SYSTEM_SERVER_TIMING_TAG = "SystemServerTiming";
    private static final String TAG = "SystemServer";
    private static final String THERMAL_OBSERVER_CLASS = "com.google.android.clockwork.ThermalObserver";
    private static final String TIME_DETECTOR_SERVICE_CLASS = "com.android.server.timedetector.TimeDetectorService$Lifecycle";
    private static final String TIME_ZONE_RULES_MANAGER_SERVICE_CLASS = "com.android.server.timezone.RulesManagerService$Lifecycle";
    private static final String UNCRYPT_PACKAGE_FILE = "/cache/recovery/uncrypt_file";
    private static final String USB_SERVICE_CLASS = "com.android.server.usb.UsbService$Lifecycle";
    private static final String VOICE_RECOGNITION_MANAGER_SERVICE_CLASS = "com.android.server.voiceinteraction.VoiceInteractionManagerService";
    private static final String WALLPAPER_SERVICE_CLASS = "com.android.server.wallpaper.WallpaperManagerService$Lifecycle";
    private static final String WEAR_CONNECTIVITY_SERVICE_CLASS = "com.android.clockwork.connectivity.WearConnectivityService";
    private static final String WEAR_DISPLAY_SERVICE_CLASS = "com.google.android.clockwork.display.WearDisplayService";
    private static final String WEAR_GLOBAL_ACTIONS_SERVICE_CLASS = "com.android.clockwork.globalactions.GlobalActionsService";
    private static final String WEAR_LEFTY_SERVICE_CLASS = "com.google.android.clockwork.lefty.WearLeftyService";
    private static final String WEAR_POWER_SERVICE_CLASS = "com.android.clockwork.power.WearPowerService";
    private static final String WEAR_SIDEKICK_SERVICE_CLASS = "com.google.android.clockwork.sidekick.SidekickService";
    private static final String WEAR_TIME_SERVICE_CLASS = "com.google.android.clockwork.time.WearTimeService";
    private static final String WIFI_AWARE_SERVICE_CLASS = "com.android.server.wifi.aware.WifiAwareService";
    private static final String WIFI_P2P_SERVICE_CLASS = "com.android.server.wifi.p2p.WifiP2pService";
    private static final String WIFI_SERVICE_CLASS = "com.android.server.wifi.WifiService";
    private static final int sMaxBinderThreads = 31;
    private static Class<?> sMtkSystemServerClass = getMtkSystemServer();
    private static MtkSystemServer sMtkSystemServerIns = MtkSystemServer.getInstance();
    HypnusService hypnusService = null;
    private ActivityManagerService mActivityManagerService;
    private AlipayService mAlipayService = null;
    private BiometricService mBiometricService = null;
    private ContentResolver mContentResolver;
    private DisplayManagerService mDisplayManagerService;
    private EntropyMixer mEntropyMixer;
    private FaceService mFaceService = null;
    private final int mFactoryTestMode = FactoryTest.getMode();
    private FingerprintService mFingerprintService = null;
    private boolean mFirstBoot;
    private OppoSystemServerHelper mHelper = null;
    private Object mMtkSystemServerInstance = null;
    private boolean mOnlyCore;
    private OppoEngineerService mOppoEngineerService = null;
    private OppoLightsService mOppoLightsService;
    private OppoOperatorManagerService mOppoOperatorManagerService = null;
    private PackageManager mPackageManager;
    private PackageManagerService mPackageManagerService;
    private PowerManagerService mPowerManagerService;
    private Timer mProfilerSnapshotTimer;
    private final boolean mRuntimeRestart = "1".equals(SystemProperties.get("sys.boot_completed"));
    private final long mRuntimeStartElapsedTime = SystemClock.elapsedRealtime();
    private final long mRuntimeStartUptime = SystemClock.uptimeMillis();
    private Future<?> mSensorServiceStart;
    private final int mStartCount = (SystemProperties.getInt(SYSPROP_START_COUNT, 0) + 1);
    private Context mSystemContext;
    private SystemServiceManager mSystemServiceManager;
    private WebViewUpdateService mWebViewUpdateService;
    private WindowManagerGlobalLock mWindowManagerGlobalLock;
    private Future<?> mZygotePreload;
    private OppoCustomizeService oppoCustomize = null;

    private static native void initZygoteChildHeapProfiling();

    private static native void startHidlServices();

    private static native void startSensorService();

    public static void main(String[] args) {
        new SystemServer().run();
    }

    /* JADX INFO: finally extract failed */
    private void run() {
        try {
            traceBeginAndSlog("InitBeforeStartServices");
            SystemProperties.set(SYSPROP_START_COUNT, String.valueOf(this.mStartCount));
            SystemProperties.set(SYSPROP_START_ELAPSED, String.valueOf(this.mRuntimeStartElapsedTime));
            SystemProperties.set(SYSPROP_START_UPTIME, String.valueOf(this.mRuntimeStartUptime));
            EventLog.writeEvent((int) EventLogTags.SYSTEM_SERVER_START, Integer.valueOf(this.mStartCount), Long.valueOf(this.mRuntimeStartUptime), Long.valueOf(this.mRuntimeStartElapsedTime));
            if (System.currentTimeMillis() < 86400000) {
                Slog.w(TAG, "System clock is before 1970; setting to 1970.");
                SystemClock.setCurrentTimeMillis(86400000);
            }
            String timezoneProperty = SystemProperties.get("persist.sys.timezone");
            if (timezoneProperty == null || timezoneProperty.isEmpty()) {
                Slog.w(TAG, "Timezone not set; setting to GMT.");
                SystemProperties.set("persist.sys.timezone", "GMT");
            }
            if (!SystemProperties.get("persist.sys.language").isEmpty()) {
                SystemProperties.set("persist.sys.locale", Locale.getDefault().toLanguageTag());
                SystemProperties.set("persist.sys.language", "");
                SystemProperties.set("persist.sys.country", "");
                SystemProperties.set("persist.sys.localevar", "");
            }
            Binder.setWarnOnBlocking(true);
            PackageItemInfo.forceSafeLabels();
            SQLiteGlobal.sDefaultSyncMode = PriorityDump.PRIORITY_ARG_NORMAL;
            SQLiteCompatibilityWalFlags.init((String) null);
            Slog.i(TAG, "Entered the Android system server!");
            int uptimeMillis = (int) SystemClock.elapsedRealtime();
            EventLog.writeEvent((int) EventLogTags.BOOT_PROGRESS_SYSTEM_RUN, uptimeMillis);
            if (!this.mRuntimeRestart) {
                MetricsLogger.histogram((Context) null, "boot_system_server_init", uptimeMillis);
            }
            Phoenix.setBootstage("ANDROID_SYSTEMSERVER_INIT_START");
            sMtkSystemServerIns.addBootEvent("Android:SysServerInit_START");
            SystemProperties.set("persist.sys.dalvik.vm.lib.2", VMRuntime.getRuntime().vmLibrary());
            VMRuntime.getRuntime().clearGrowthLimit();
            VMRuntime.getRuntime().setTargetHeapUtilization(0.8f);
            Build.ensureFingerprintProperty();
            Environment.setUserRequired(true);
            BaseBundle.setShouldDefuse(true);
            Parcel.setStackTraceParceling(true);
            BinderInternal.disableBackgroundScheduling(true);
            BinderInternal.setMaxThreads(31);
            Process.setCanSelfBackground(false);
            Looper.prepareMainLooper();
            Looper.getMainLooper().setSlowLogThresholdMs(SLOW_DISPATCH_THRESHOLD_MS, SLOW_DELIVERY_THRESHOLD_MS);
            System.loadLibrary("android_servers");
            if (Build.IS_DEBUGGABLE) {
                initZygoteChildHeapProfiling();
            }
            performPendingShutdown();
            createSystemContext();
            this.mSystemServiceManager = new SystemServiceManager(this.mSystemContext);
            this.mSystemServiceManager.setStartInfo(this.mRuntimeRestart, this.mRuntimeStartElapsedTime, this.mRuntimeStartUptime);
            LocalServices.addService(SystemServiceManager.class, this.mSystemServiceManager);
            SystemServerInitThreadPool.get();
            traceEnd();
            sMtkSystemServerIns.setPrameters(BOOT_TIMINGS_TRACE_LOG, this.mSystemServiceManager, this.mSystemContext);
            SystemProperties.set("sys.oppo.boot_completed", "0");
            this.mHelper = new OppoSystemServerHelper(this.mSystemContext);
            OppoCustomizeService.initCustomizeListPath();
            try {
                traceBeginAndSlog("StartServices");
                startBootstrapServices();
                sMtkSystemServerIns.startMtkBootstrapServices();
                startCoreServices();
                sMtkSystemServerIns.startMtkCoreServices();
                startOtherServices();
                SystemServerInitThreadPool.shutdown();
                traceEnd();
                StrictMode.initVmDefaults(null);
                if ("user".equals(Build.TYPE) && !this.mRuntimeRestart && !isFirstBootOrUpgrade()) {
                    int uptimeMillis2 = (int) SystemClock.elapsedRealtime();
                    MetricsLogger.histogram((Context) null, "boot_system_server_ready", uptimeMillis2);
                    if (uptimeMillis2 > 60000) {
                        Slog.wtf(SYSTEM_SERVER_TIMING_TAG, "SystemServer init took too long. uptimeMillis=" + uptimeMillis2);
                    }
                }
                Phoenix.setBootstage("ANDROID_SYSTEMSERVER_READY");
                if (!VMRuntime.hasBootImageSpaces()) {
                    Slog.wtf(TAG, "Runtime is not running with a boot image!");
                }
                sMtkSystemServerIns.addBootEvent("Android:SysServerInit_END");
                Process.setThreadPriority(-2);
                Looper.loop();
                throw new RuntimeException("Main thread loop unexpectedly exited");
            } catch (Throwable th) {
                traceEnd();
                throw th;
            }
        } catch (Throwable th2) {
            traceEnd();
            throw th2;
        }
    }

    private boolean isFirstBootOrUpgrade() {
        return this.mPackageManagerService.isFirstBoot() || this.mPackageManagerService.isDeviceUpgrading();
    }

    private void reportWtf(String msg, Throwable e) {
        Slog.w(TAG, "***********************************************");
        Slog.wtf(TAG, "BOOT FAILURE " + msg, e);
    }

    private void performPendingShutdown() {
        final String reason;
        String shutdownAction = SystemProperties.get(ShutdownThread.SHUTDOWN_ACTION_PROPERTY, "");
        if (shutdownAction != null && shutdownAction.length() > 0) {
            final boolean reboot = shutdownAction.charAt(0) == '1';
            if (shutdownAction.length() > 1) {
                reason = shutdownAction.substring(1, shutdownAction.length());
            } else {
                reason = null;
            }
            if (reason != null && reason.startsWith("recovery-update")) {
                File packageFile = new File(UNCRYPT_PACKAGE_FILE);
                if (packageFile.exists()) {
                    String filename = null;
                    try {
                        filename = FileUtils.readTextFile(packageFile, 0, null);
                    } catch (IOException e) {
                        Slog.e(TAG, "Error reading uncrypt package file", e);
                    }
                    if (filename != null && filename.startsWith("/data") && !new File(BLOCK_MAP_FILE).exists()) {
                        Slog.e(TAG, "Can't find block map file, uncrypt failed or unexpected runtime restart?");
                        return;
                    }
                }
            }
            Message msg = Message.obtain(UiThread.getHandler(), new Runnable() {
                /* class com.android.server.SystemServer.AnonymousClass1 */

                public void run() {
                    synchronized (this) {
                        ShutdownThread.rebootOrShutdown(null, reboot, reason);
                    }
                }
            });
            msg.setAsynchronous(true);
            UiThread.getHandler().sendMessage(msg);
        }
    }

    private void createSystemContext() {
        ActivityThread activityThread = ActivityThread.systemMain();
        this.mSystemContext = activityThread.getSystemContext();
        this.mSystemContext.setTheme(DEFAULT_SYSTEM_THEME);
        activityThread.getSystemUiContext().setTheme(DEFAULT_SYSTEM_THEME);
    }

    /* JADX INFO: finally extract failed */
    private void startBootstrapServices() {
        traceBeginAndSlog("StartWatchdog");
        Watchdog watchdog = Watchdog.getInstance();
        watchdog.start();
        traceEnd();
        Slog.i(TAG, "Reading configuration...");
        traceBeginAndSlog("ReadingSystemConfig");
        SystemServerInitThreadPool.get().submit($$Lambda$YWiwiKm_Qgqb55C6tTuq_n2JzdY.INSTANCE, "ReadingSystemConfig");
        traceEnd();
        traceBeginAndSlog("StartInstaller");
        Installer installer = (Installer) this.mSystemServiceManager.startService(Installer.class);
        traceEnd();
        traceBeginAndSlog("DeviceIdentifiersPolicyService");
        this.mSystemServiceManager.startService(DeviceIdentifiersPolicyService.class);
        traceEnd();
        traceBeginAndSlog("UriGrantsManagerService");
        this.mSystemServiceManager.startService(UriGrantsManagerService.Lifecycle.class);
        traceEnd();
        traceBeginAndSlog("StartActivityManager");
        ActivityTaskManagerService atm = ((ActivityTaskManagerService.Lifecycle) this.mSystemServiceManager.startService(ActivityTaskManagerService.Lifecycle.class)).getService();
        Slog.i(TAG, "Ams Service");
        this.mActivityManagerService = ActivityManagerService.Lifecycle.startService(this.mSystemServiceManager, atm);
        this.mActivityManagerService.setSystemServiceManager(this.mSystemServiceManager);
        this.mActivityManagerService.setInstaller(installer);
        this.mWindowManagerGlobalLock = atm.getGlobalLock();
        traceEnd();
        traceBeginAndSlog("StartPowerManager");
        Slog.i(TAG, "Power Service");
        this.mPowerManagerService = (PowerManagerService) this.mSystemServiceManager.startService(PowerManagerService.class);
        traceEnd();
        traceBeginAndSlog("StartThermalManager");
        this.mSystemServiceManager.startService(ThermalManagerService.class);
        traceEnd();
        traceBeginAndSlog("InitPowerManagement");
        this.mActivityManagerService.initPowerManagement();
        traceEnd();
        traceBeginAndSlog("StartRecoverySystemService");
        this.mSystemServiceManager.startService(RecoverySystemService.class);
        traceEnd();
        RescueParty.noteBoot(this.mSystemContext);
        traceBeginAndSlog("StartLightsService");
        Slog.i(TAG, "Light Service");
        this.mOppoLightsService = (OppoLightsService) this.mSystemServiceManager.startService(OppoLightsService.class);
        traceEnd();
        traceBeginAndSlog("StartSidekickService");
        if (SystemProperties.getBoolean("config.enable_sidekick_graphics", false)) {
            this.mSystemServiceManager.startService(WEAR_SIDEKICK_SERVICE_CLASS);
        }
        traceEnd();
        traceBeginAndSlog("StartDisplayManager");
        Slog.i(TAG, "DisplayManager Service");
        this.mDisplayManagerService = this.mHelper.startColorDisplayManagerService();
        if (this.mDisplayManagerService == null) {
            Slog.i(TAG, "DisplayManager Service");
            this.mDisplayManagerService = (DisplayManagerService) this.mSystemServiceManager.startService(DisplayManagerService.class);
        }
        traceEnd();
        traceBeginAndSlog("WaitForDisplay");
        this.mSystemServiceManager.startBootPhase(100);
        traceEnd();
        String cryptState = (String) VoldProperties.decrypt().orElse("");
        boolean z = true;
        if (ENCRYPTING_STATE.equals(cryptState)) {
            Slog.w(TAG, "Detected encryption in progress - only parsing core apps");
            this.mOnlyCore = true;
        } else if ("1".equals(cryptState)) {
            Slog.w(TAG, "Device encrypted - only parsing core apps");
            this.mOnlyCore = true;
        }
        if (!this.mRuntimeRestart) {
            MetricsLogger.histogram((Context) null, "boot_package_manager_init_start", (int) SystemClock.elapsedRealtime());
        }
        traceBeginAndSlog("StartPackageManagerService");
        try {
            Watchdog.getInstance().pauseWatchingCurrentThread("packagemanagermain");
            Context context = this.mSystemContext;
            if (this.mFactoryTestMode == 0) {
                z = false;
            }
            this.mPackageManagerService = PackageManagerService.main(context, installer, z, this.mOnlyCore);
            Watchdog.getInstance().resumeWatchingCurrentThread("packagemanagermain");
            this.mFirstBoot = this.mPackageManagerService.isFirstBoot();
            this.mPackageManager = this.mSystemContext.getPackageManager();
            traceEnd();
            if (!this.mRuntimeRestart && !isFirstBootOrUpgrade()) {
                MetricsLogger.histogram((Context) null, "boot_package_manager_init_ready", (int) SystemClock.elapsedRealtime());
            }
            if (!this.mOnlyCore && !SystemProperties.getBoolean("config.disable_otadexopt", false)) {
                traceBeginAndSlog("StartOtaDexOptService");
                try {
                    Watchdog.getInstance().pauseWatchingCurrentThread("moveab");
                    OtaDexoptService.main(this.mSystemContext, this.mPackageManagerService);
                } catch (Throwable th) {
                    Watchdog.getInstance().resumeWatchingCurrentThread("moveab");
                    traceEnd();
                    throw th;
                }
                Watchdog.getInstance().resumeWatchingCurrentThread("moveab");
                traceEnd();
            }
            traceBeginAndSlog("StartUserManagerService");
            this.mSystemServiceManager.startService(UserManagerService.LifeCycle.class);
            traceEnd();
            traceBeginAndSlog("InitAttributerCache");
            AttributeCache.init(this.mSystemContext);
            traceEnd();
            traceBeginAndSlog("SetSystemProcess");
            this.mActivityManagerService.setSystemProcess();
            traceEnd();
            traceBeginAndSlog("InitWatchdog");
            watchdog.init(this.mSystemContext, this.mActivityManagerService);
            traceEnd();
            this.mDisplayManagerService.setupSchedulerPolicies();
            this.mPackageManagerService.onAmsAddedtoServiceMgr();
            traceBeginAndSlog("StartOverlayManagerService");
            this.mSystemServiceManager.startService(new OverlayManagerService(this.mSystemContext, installer));
            traceEnd();
            traceBeginAndSlog("StartSensorPrivacyService");
            this.mSystemServiceManager.startService(new SensorPrivacyService(this.mSystemContext));
            traceEnd();
            Slog.i(TAG, "Sensor Service");
            if (SystemProperties.getInt("persist.sys.displayinset.top", 0) > 0) {
                this.mActivityManagerService.updateSystemUiContext();
                ((DisplayManagerInternal) LocalServices.getService(DisplayManagerInternal.class)).onOverlayChanged();
            }
            this.mSensorServiceStart = SystemServerInitThreadPool.get().submit($$Lambda$SystemServer$UyrPns7R814gZEylCbDKhe8It4.INSTANCE, START_SENSOR_SERVICE);
            this.mHelper.startBootstrapServices();
        } catch (Throwable th2) {
            Watchdog.getInstance().resumeWatchingCurrentThread("packagemanagermain");
            throw th2;
        }
    }

    static /* synthetic */ void lambda$startBootstrapServices$0() {
        TimingsTraceLog traceLog = new TimingsTraceLog(SYSTEM_SERVER_TIMING_ASYNC_TAG, 524288);
        traceLog.traceBegin(START_SENSOR_SERVICE);
        startSensorService();
        traceLog.traceEnd();
    }

    private void startCoreServices() {
        traceBeginAndSlog("StartBatteryService");
        Slog.i(TAG, "Battery Service");
        this.mSystemServiceManager.startService(BatteryService.class);
        traceEnd();
        traceBeginAndSlog("StartUsageService");
        Slog.i(TAG, "UsageStats Service");
        this.mSystemServiceManager.startService(UsageStatsService.class);
        this.mActivityManagerService.setUsageStatsManager((UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class));
        traceEnd();
        if (this.mPackageManager.hasSystemFeature("android.software.webview")) {
            traceBeginAndSlog("StartWebViewUpdateService");
            this.mWebViewUpdateService = (WebViewUpdateService) this.mSystemServiceManager.startService(WebViewUpdateService.class);
            traceEnd();
        }
        traceBeginAndSlog("StartCachedDeviceStateService");
        this.mSystemServiceManager.startService(CachedDeviceStateService.class);
        traceEnd();
        traceBeginAndSlog("StartBinderCallsStatsService");
        this.mSystemServiceManager.startService(BinderCallsStatsService.LifeCycle.class);
        traceEnd();
        traceBeginAndSlog("StartLooperStatsService");
        this.mSystemServiceManager.startService(LooperStatsService.Lifecycle.class);
        traceEnd();
        traceBeginAndSlog("StartRollbackManagerService");
        this.mSystemServiceManager.startService(RollbackManagerService.class);
        traceEnd();
        traceBeginAndSlog("StartBugreportManagerService");
        this.mSystemServiceManager.startService(BugreportManagerService.class);
        traceEnd();
        traceBeginAndSlog(GpuService.TAG);
        this.mSystemServiceManager.startService(GpuService.class);
        traceEnd();
        this.mHelper.startCoreServices();
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:210:0x0628 */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r19v2, types: [com.android.server.IpSecService] */
    /* JADX WARN: Type inference failed for: r19v3 */
    /* JADX WARN: Type inference failed for: r15v3, types: [com.android.server.net.NetworkStatsService, android.net.INetworkStatsService] */
    /* JADX WARN: Type inference failed for: r0v161, types: [com.mediatek.server.MtkSystemServer] */
    /* JADX WARN: Type inference failed for: r0v238, types: [com.android.server.GraphicsStatsService, android.os.IBinder] */
    /* JADX WARN: Type inference failed for: r19v4 */
    /* JADX WARN: Type inference failed for: r6v88, types: [android.os.IBinder] */
    /* JADX WARN: Type inference failed for: r6v100, types: [com.android.server.oppo.OppoCustomizeService, android.os.IBinder] */
    /* JADX WARN: Type inference failed for: r6v102, types: [com.android.server.oppo.HypnusService, android.os.IBinder] */
    /* JADX WARN: Type inference failed for: r0v353, types: [com.android.server.oppo.CabcService] */
    /* JADX WARN: Type inference failed for: r10v32, types: [android.os.IBinder] */
    /* JADX WARN: Type inference failed for: r0v373, types: [com.android.server.HardwarePropertiesManagerService] */
    /* JADX WARN: Type inference failed for: r0v379, types: [com.android.server.SerialService] */
    /* JADX WARN: Type inference failed for: r6v141, types: [android.os.IBinder] */
    /* JADX WARN: Type inference failed for: r13v12, types: [android.os.IBinder] */
    /* JADX WARN: Type inference failed for: r6v146, types: [com.android.server.UpdateLockService, android.os.IBinder] */
    /* JADX WARN: Type inference failed for: r6v148, types: [com.android.server.SystemUpdateManagerService, android.os.IBinder] */
    /* JADX WARN: Type inference failed for: r0v430, types: [com.android.server.NsdService] */
    /* JADX WARN: Type inference failed for: r7v30, types: [android.os.IBinder] */
    /* JADX WARN: Type inference failed for: r0v460, types: [com.android.server.net.NetworkStatsService] */
    /* JADX WARN: Type inference failed for: r0v465, types: [com.android.server.IpSecService] */
    /* JADX WARN: Type inference failed for: r6v158, types: [android.os.IBinder] */
    /* JADX WARN: Type inference failed for: r0v479, types: [com.android.server.statusbar.ColorStatusBarManagerService] */
    /* JADX WARN: Type inference failed for: r6v173, types: [com.android.server.security.KeyAttestationApplicationIdProviderService, android.os.IBinder] */
    /* JADX WARN: Type inference failed for: r6v175, types: [com.android.server.os.SchedulingPolicyService, android.os.IBinder] */
    /* JADX WARN: Type inference failed for: r0v541, types: [com.android.server.TelephonyRegistry, android.os.IBinder] */
    /* JADX WARN: Type inference failed for: r9v7, types: [android.os.IBinder] */
    /* JADX WARN: Type inference failed for: r12v6, types: [com.android.server.input.InputManagerService, android.os.IBinder] */
    /* JADX WARN: Type inference failed for: r0v576, types: [android.os.IBinder, com.android.server.wm.WindowManagerService] */
    /* JADX WARN: Type inference failed for: r0v581, types: [com.android.server.am.ActivityManagerService] */
    /* JADX WARN: Type inference failed for: r0v608, types: [com.android.server.ConsumerIrService, android.os.IBinder] */
    /* JADX WARN: Type inference failed for: r0v612, types: [com.android.server.DynamicSystemService, android.os.IBinder] */
    /* JADX WARN: Type inference failed for: r15v6 */
    /* JADX WARN: Type inference failed for: r15v7 */
    /* JADX WARNING: Removed duplicated region for block: B:117:0x03f8 A[Catch:{ all -> 0x0419 }] */
    /* JADX WARNING: Removed duplicated region for block: B:130:0x044d  */
    /* JADX WARNING: Removed duplicated region for block: B:148:0x049e  */
    /* JADX WARNING: Removed duplicated region for block: B:208:0x0605  */
    /* JADX WARNING: Removed duplicated region for block: B:226:0x0672  */
    /* JADX WARNING: Removed duplicated region for block: B:229:0x069c  */
    /* JADX WARNING: Removed duplicated region for block: B:232:0x06b7  */
    /* JADX WARNING: Removed duplicated region for block: B:235:0x06d2  */
    /* JADX WARNING: Removed duplicated region for block: B:238:0x06ed  */
    /* JADX WARNING: Removed duplicated region for block: B:241:0x0708  */
    /* JADX WARNING: Removed duplicated region for block: B:247:0x0735 A[SYNTHETIC, Splitter:B:247:0x0735] */
    /* JADX WARNING: Removed duplicated region for block: B:282:0x07c1 A[Catch:{ all -> 0x07cc }] */
    /* JADX WARNING: Removed duplicated region for block: B:287:0x07e0  */
    /* JADX WARNING: Removed duplicated region for block: B:290:0x080e  */
    /* JADX WARNING: Removed duplicated region for block: B:325:0x087f  */
    /* JADX WARNING: Removed duplicated region for block: B:333:0x08cd  */
    /* JADX WARNING: Removed duplicated region for block: B:336:0x08e3  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x08eb  */
    /* JADX WARNING: Removed duplicated region for block: B:344:0x092d  */
    /* JADX WARNING: Removed duplicated region for block: B:347:0x0952  */
    /* JADX WARNING: Removed duplicated region for block: B:355:0x0983  */
    /* JADX WARNING: Removed duplicated region for block: B:368:0x09cd  */
    /* JADX WARNING: Removed duplicated region for block: B:392:0x0a3c  */
    /* JADX WARNING: Removed duplicated region for block: B:395:0x0a6e  */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0ad5  */
    /* JADX WARNING: Removed duplicated region for block: B:451:0x0bd3  */
    /* JADX WARNING: Removed duplicated region for block: B:457:0x0be9  */
    /* JADX WARNING: Removed duplicated region for block: B:459:0x0bfa A[ADDED_TO_REGION] */
    /* JADX WARNING: Removed duplicated region for block: B:485:0x0c8d  */
    /* JADX WARNING: Removed duplicated region for block: B:488:0x0ca9  */
    /* JADX WARNING: Removed duplicated region for block: B:491:0x0cc2  */
    /* JADX WARNING: Removed duplicated region for block: B:494:0x0cfe  */
    /* JADX WARNING: Removed duplicated region for block: B:502:0x0d3a  */
    /* JADX WARNING: Removed duplicated region for block: B:505:0x0d51  */
    /* JADX WARNING: Removed duplicated region for block: B:518:0x0d9e  */
    /* JADX WARNING: Removed duplicated region for block: B:519:0x0dba  */
    /* JADX WARNING: Removed duplicated region for block: B:521:0x0dbe  */
    /* JADX WARNING: Removed duplicated region for block: B:523:0x0dcf  */
    /* JADX WARNING: Removed duplicated region for block: B:540:0x0e34  */
    /* JADX WARNING: Removed duplicated region for block: B:546:0x0e4e  */
    /* JADX WARNING: Removed duplicated region for block: B:554:0x0e76  */
    /* JADX WARNING: Removed duplicated region for block: B:561:0x0ece  */
    /* JADX WARNING: Removed duplicated region for block: B:563:0x0eee  */
    /* JADX WARNING: Removed duplicated region for block: B:565:0x0eff  */
    /* JADX WARNING: Removed duplicated region for block: B:570:0x0f5d  */
    /* JADX WARNING: Removed duplicated region for block: B:572:0x0f6e  */
    /* JADX WARNING: Removed duplicated region for block: B:575:0x0f89  */
    /* JADX WARNING: Removed duplicated region for block: B:578:0x0fb8  */
    /* JADX WARNING: Removed duplicated region for block: B:581:0x0fdb  */
    /* JADX WARNING: Removed duplicated region for block: B:589:0x102e A[SYNTHETIC, Splitter:B:589:0x102e] */
    /* JADX WARNING: Removed duplicated region for block: B:600:0x1077  */
    /* JADX WARNING: Removed duplicated region for block: B:603:0x10a7  */
    /* JADX WARNING: Removed duplicated region for block: B:613:0x10cf A[Catch:{ all -> 0x10da }] */
    /* JADX WARNING: Removed duplicated region for block: B:619:0x10e5 A[Catch:{ all -> 0x10f0 }] */
    /* JADX WARNING: Removed duplicated region for block: B:625:0x10fb A[Catch:{ all -> 0x1106 }] */
    /* JADX WARNING: Removed duplicated region for block: B:631:0x1111 A[Catch:{ all -> 0x111c }] */
    /* JADX WARNING: Removed duplicated region for block: B:638:0x1136 A[Catch:{ all -> 0x1141 }] */
    /* JADX WARNING: Removed duplicated region for block: B:645:0x1163 A[Catch:{ all -> 0x1169 }] */
    /* JADX WARNING: Removed duplicated region for block: B:655:0x11a9  */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x0372  */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x038d  */
    /* JADX WARNING: Unknown variable types count: 30 */
    private void startOtherServices() {
        boolean z;
        VibratorService vibrator;
        NetworkManagementService networkManagement;
        TelephonyRegistry telephonyRegistry;
        InputManagerService inputManager;
        WindowManagerService wm;
        boolean safeMode;
        ILockSettings lockSettings;
        MediaRouterService mediaRouter;
        IConnectivityManager iConnectivityManager;
        CountryDetectorService countryDetector;
        OppomodemService oppomodemService;
        LocationManagerService location;
        NetworkPolicyManagerService networkPolicy;
        NetworkTimeUpdateService networkTimeUpdater;
        NetworkStatsService networkStats;
        ?? r19;
        Resources.Theme systemTheme;
        IBinder iBinder;
        NetworkManagementService networkManagement2;
        ?? r15;
        NetworkPolicyManagerService networkPolicy2;
        IBinder iBinder2;
        OppomodemService oppomodemService2;
        IConnectivityManager iConnectivityManager2;
        IConnectivityManager iConnectivityManager3;
        IBinder iBinder3;
        INotificationManager notification;
        INotificationManager notification2;
        LocationManagerService location2;
        CountryDetectorService countryDetector2;
        IBinder iBinder4;
        OppomodemService oppomodemService3;
        IBinder iBinder5;
        MediaRouterService mediaRouter2;
        boolean hasFeatureFace;
        boolean hasFeatureIris;
        boolean hasFeatureFingerprint;
        MediaRouterService mediaRouter3;
        Throwable e;
        ?? mediaRouterService;
        NetworkTimeUpdateService networkTimeUpdater2;
        Throwable e2;
        Throwable e3;
        OppomodemService oppomodemService4;
        Throwable e4;
        ?? oppomodemService5;
        IBinder iBinder6;
        Throwable e5;
        IBinder iBinder7;
        Throwable e6;
        Throwable e7;
        CountryDetectorService countryDetector3;
        Throwable e8;
        ?? countryDetectorService;
        LocationManagerService location3;
        Throwable e9;
        ?? locationManagerService;
        IBinder iBinder8;
        Throwable e10;
        IConnectivityManager iConnectivityManager4;
        Throwable e11;
        IConnectivityManager iConnectivityManager5;
        NetworkPolicyManagerService networkPolicy3;
        Throwable e12;
        ?? networkPolicyManagerService;
        NetworkManagementService networkManagement3;
        Throwable e13;
        ?? create;
        Throwable e14;
        VibratorService vibrator2;
        RuntimeException e15;
        ?? vibratorService;
        InputManagerService inputManager2;
        ?? r12;
        Context context = this.mSystemContext;
        IBinder iBinder9 = null;
        IBinder iBinder10 = null;
        WindowManagerService wm2 = null;
        IBinder iBinder11 = null;
        NetworkTimeUpdateService networkTimeUpdater3 = null;
        InputManagerService inputManager3 = null;
        boolean disableSystemTextClassifier = SystemProperties.getBoolean("config.disable_systemtextclassifier", false);
        boolean disableNetworkTime = SystemProperties.getBoolean("config.disable_networktime", false);
        boolean disableCameraService = SystemProperties.getBoolean("config.disable_cameraservice", false);
        boolean disableSlices = SystemProperties.getBoolean("config.disable_slices", false);
        boolean enableLeftyService = SystemProperties.getBoolean("config.enable_lefty", false);
        boolean isEmulator = SystemProperties.get("ro.kernel.qemu").equals("1");
        boolean isWatch = context.getPackageManager().hasSystemFeature("android.hardware.type.watch");
        boolean isArc = context.getPackageManager().hasSystemFeature("org.chromium.arc");
        boolean enableVrService = context.getPackageManager().hasSystemFeature("android.hardware.vr.high_performance");
        if (Build.IS_DEBUGGABLE) {
            z = false;
            if (SystemProperties.getBoolean("debug.crash_system", false)) {
                throw new RuntimeException();
            }
        } else {
            z = false;
        }
        try {
            try {
                this.mZygotePreload = SystemServerInitThreadPool.get().submit($$Lambda$SystemServer$VBGb9VpEls6bUcVBPwYLtX7qDTs.INSTANCE, "SecondaryZygotePreload");
                traceBeginAndSlog("StartKeyAttestationApplicationIdProviderService");
                ServiceManager.addService("sec_key_att_app_id_provider", (IBinder) new KeyAttestationApplicationIdProviderService(context));
                traceEnd();
                traceBeginAndSlog("StartKeyChainSystemService");
                this.mSystemServiceManager.startService(KeyChainSystemService.class);
                traceEnd();
                traceBeginAndSlog("StartSchedulingPolicyService");
                ServiceManager.addService("scheduling_policy", (IBinder) new SchedulingPolicyService());
                traceEnd();
                traceBeginAndSlog("StartTelecomLoaderService");
                this.mSystemServiceManager.startService(TelecomLoaderService.class);
                traceEnd();
                traceBeginAndSlog("StartTelephonyRegistry");
                ?? telephonyRegistry2 = new TelephonyRegistry(context);
                try {
                    ServiceManager.addService("telephony.registry", (IBinder) telephonyRegistry2);
                    traceEnd();
                    traceBeginAndSlog("StartEntropyMixer");
                    this.mEntropyMixer = new EntropyMixer(context);
                    traceEnd();
                    this.mContentResolver = context.getContentResolver();
                    traceBeginAndSlog("StartAccountManagerService");
                    this.mSystemServiceManager.startService(ACCOUNT_SERVICE_CLASS);
                    traceEnd();
                    traceBeginAndSlog("StartContentService");
                    this.mSystemServiceManager.startService(CONTENT_SERVICE_CLASS);
                    traceEnd();
                    traceBeginAndSlog("InstallSystemProviders");
                    this.mActivityManagerService.installSystemProviders();
                    SQLiteCompatibilityWalFlags.reset();
                    traceEnd();
                    traceBeginAndSlog("StartDropBoxManager");
                    this.mSystemServiceManager.startService(DropBoxManagerService.class);
                    traceEnd();
                    traceBeginAndSlog("StartVibratorService");
                    vibratorService = new VibratorService(context);
                } catch (RuntimeException e16) {
                    e15 = e16;
                    telephonyRegistry = telephonyRegistry2;
                    networkManagement = null;
                    vibrator2 = null;
                    Slog.e("System", "******************************************");
                    Slog.e("System", "************ Failure starting core service", e15);
                    inputManager = inputManager3;
                    vibrator = vibrator2;
                    wm = wm2;
                    safeMode = wm.detectSafeMode();
                    if (safeMode) {
                    }
                    lockSettings = null;
                    if (this.mFactoryTestMode != 1) {
                    }
                    traceBeginAndSlog("MakeDisplayReady");
                    Slog.i(TAG, "displayReady");
                    wm.displayReady();
                    traceEnd();
                    traceBeginAndSlog("StartStorageManagerService");
                    try {
                        if (!sMtkSystemServerIns.startMtkStorageManagerService()) {
                        }
                        Slog.i(TAG, "mount service");
                        IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
                    } catch (Throwable e17) {
                        reportWtf("starting StorageManagerService", e17);
                    }
                    traceEnd();
                    traceBeginAndSlog("StartStorageStatsService");
                    try {
                        this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
                    } catch (Throwable e18) {
                        reportWtf("starting StorageStatsService", e18);
                    }
                    traceEnd();
                    traceBeginAndSlog("StartUiModeManager");
                    this.mSystemServiceManager.startService(UiModeManagerService.class);
                    traceEnd();
                    if (!this.mOnlyCore) {
                    }
                    traceBeginAndSlog("PerformFstrimIfNeeded");
                    Slog.i(TAG, "performFstrimIfNeeded");
                    this.mPackageManagerService.performFstrimIfNeeded();
                    traceEnd();
                    if (this.mFactoryTestMode == 1) {
                    }
                    if (!isWatch) {
                    }
                    if (isWatch) {
                    }
                    if (!disableSlices) {
                    }
                    if (!disableCameraService) {
                    }
                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                    }
                    traceBeginAndSlog("StartStatsCompanionService");
                    this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                    traceEnd();
                    traceBeginAndSlog("StartIncidentCompanionService");
                    this.mSystemServiceManager.startService(IncidentCompanionService.class);
                    traceEnd();
                    if (safeMode) {
                    }
                    traceBeginAndSlog("StartMmsService");
                    MmsServiceBroker mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                    }
                    traceBeginAndSlog("StartClipboardService");
                    this.mSystemServiceManager.startService(ClipboardService.class);
                    traceEnd();
                    traceBeginAndSlog("AppServiceManager");
                    this.mSystemServiceManager.startService(AppBindingService.Lifecycle.class);
                    traceEnd();
                    sMtkSystemServerIns.startMtkOtherServices();
                    this.mHelper.startOtherServices();
                    traceBeginAndSlog("MakeVibratorServiceReady");
                    vibrator.systemReady();
                    traceEnd();
                    traceBeginAndSlog("MakeLockSettingsServiceReady");
                    if (lockSettings != null) {
                    }
                    traceEnd();
                    traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                    traceEnd();
                    traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                    this.mSystemServiceManager.startBootPhase(500);
                    traceEnd();
                    traceBeginAndSlog("MakeWindowManagerServiceReady");
                    Slog.i(TAG, "wms systemReady");
                    wm.systemReady();
                    traceEnd();
                    if (safeMode) {
                    }
                    Configuration config = wm.computeNewConfiguration(0);
                    DisplayMetrics metrics = new DisplayMetrics();
                    ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                    context.getResources().updateConfiguration(config, metrics);
                    systemTheme = context.getTheme();
                    if (systemTheme.getChangingConfigurations() != 0) {
                    }
                    traceBeginAndSlog("MakePowerManagerServiceReady");
                    Slog.i(TAG, "power manager systemReady");
                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                    traceEnd();
                    if (this.mFingerprintService != null) {
                    }
                    if (this.mFaceService != null) {
                    }
                    if (this.mBiometricService != null) {
                    }
                    if (this.mAlipayService != null) {
                    }
                    traceBeginAndSlog("StartPermissionPolicyService");
                    this.mSystemServiceManager.startService(PermissionPolicyService.class);
                    traceEnd();
                    if (this.mOppoOperatorManagerService != null) {
                    }
                    traceBeginAndSlog("MakePackageManagerServiceReady");
                    Slog.i(TAG, "Package systemReady");
                    this.mPackageManagerService.systemReady();
                    traceEnd();
                    traceBeginAndSlog("MakeOppoCustomizeServiceReady");
                    if (this.oppoCustomize != null) {
                    }
                    traceEnd();
                    traceBeginAndSlog("MakeDisplayManagerServiceReady");
                    Slog.i(TAG, "DisplayManager systemReady");
                    this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                    traceEnd();
                    this.mSystemServiceManager.setSafeMode(safeMode);
                    traceBeginAndSlog("StartDeviceSpecificServices");
                    String[] classes = this.mSystemContext.getResources().getStringArray(17236011);
                    while (r6 < r7) {
                    }
                    traceEnd();
                    traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                    traceEnd();
                    this.mHelper.systemReady();
                    Slog.i(TAG, "Ams systemReady");
                    this.mActivityManagerService.systemReady(new Runnable(context, wm, safeMode, iConnectivityManager, networkManagement, networkPolicy, r19, networkStats, location, countryDetector, networkTimeUpdater, inputManager, telephonyRegistry, mediaRouter, oppomodemService, mmsService) {
                        /* class com.android.server.$$Lambda$SystemServer$Veuf5e15txqWe0TdLPFg29kfanQ */
                        private final /* synthetic */ Context f$1;
                        private final /* synthetic */ CountryDetectorService f$10;
                        private final /* synthetic */ NetworkTimeUpdateService f$11;
                        private final /* synthetic */ InputManagerService f$12;
                        private final /* synthetic */ TelephonyRegistry f$13;
                        private final /* synthetic */ MediaRouterService f$14;
                        private final /* synthetic */ OppomodemService f$15;
                        private final /* synthetic */ MmsServiceBroker f$16;
                        private final /* synthetic */ WindowManagerService f$2;
                        private final /* synthetic */ boolean f$3;
                        private final /* synthetic */ ConnectivityService f$4;
                        private final /* synthetic */ NetworkManagementService f$5;
                        private final /* synthetic */ NetworkPolicyManagerService f$6;
                        private final /* synthetic */ IpSecService f$7;
                        private final /* synthetic */ NetworkStatsService f$8;
                        private final /* synthetic */ LocationManagerService f$9;

                        {
                            this.f$1 = r4;
                            this.f$2 = r5;
                            this.f$3 = r6;
                            this.f$4 = r7;
                            this.f$5 = r8;
                            this.f$6 = r9;
                            this.f$7 = r10;
                            this.f$8 = r11;
                            this.f$9 = r12;
                            this.f$10 = r13;
                            this.f$11 = r14;
                            this.f$12 = r15;
                            this.f$13 = r16;
                            this.f$14 = r17;
                            this.f$15 = r18;
                            this.f$16 = r19;
                        }

                        public final void run() {
                            SystemServer.this.lambda$startOtherServices$4$SystemServer(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15, this.f$16);
                        }
                    }, BOOT_TIMINGS_TRACE_LOG);
                }
                try {
                    ServiceManager.addService("vibrator", (IBinder) vibratorService);
                    traceEnd();
                    try {
                        if (!"user".equals(SystemProperties.get("ro.build.type", "userdebug"))) {
                            try {
                                traceBeginAndSlog("StartDynamicSystemService");
                                try {
                                    ServiceManager.addService("dynamic_system", (IBinder) new DynamicSystemService(context));
                                    traceEnd();
                                } catch (RuntimeException e19) {
                                    e15 = e19;
                                    telephonyRegistry = telephonyRegistry2;
                                    networkManagement = null;
                                    vibrator2 = vibratorService;
                                    Slog.e("System", "******************************************");
                                    Slog.e("System", "************ Failure starting core service", e15);
                                    inputManager = inputManager3;
                                    vibrator = vibrator2;
                                    wm = wm2;
                                    safeMode = wm.detectSafeMode();
                                    if (safeMode) {
                                    }
                                    lockSettings = null;
                                    if (this.mFactoryTestMode != 1) {
                                    }
                                    traceBeginAndSlog("MakeDisplayReady");
                                    Slog.i(TAG, "displayReady");
                                    wm.displayReady();
                                    traceEnd();
                                    traceBeginAndSlog("StartStorageManagerService");
                                    if (!sMtkSystemServerIns.startMtkStorageManagerService()) {
                                    }
                                    Slog.i(TAG, "mount service");
                                    IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
                                    traceEnd();
                                    traceBeginAndSlog("StartStorageStatsService");
                                    this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
                                    traceEnd();
                                    traceBeginAndSlog("StartUiModeManager");
                                    this.mSystemServiceManager.startService(UiModeManagerService.class);
                                    traceEnd();
                                    if (!this.mOnlyCore) {
                                    }
                                    traceBeginAndSlog("PerformFstrimIfNeeded");
                                    Slog.i(TAG, "performFstrimIfNeeded");
                                    this.mPackageManagerService.performFstrimIfNeeded();
                                    traceEnd();
                                    if (this.mFactoryTestMode == 1) {
                                    }
                                    if (!isWatch) {
                                    }
                                    if (isWatch) {
                                    }
                                    if (!disableSlices) {
                                    }
                                    if (!disableCameraService) {
                                    }
                                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                                    }
                                    traceBeginAndSlog("StartStatsCompanionService");
                                    this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                                    traceEnd();
                                    traceBeginAndSlog("StartIncidentCompanionService");
                                    this.mSystemServiceManager.startService(IncidentCompanionService.class);
                                    traceEnd();
                                    if (safeMode) {
                                    }
                                    traceBeginAndSlog("StartMmsService");
                                    MmsServiceBroker mmsService2 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                    traceEnd();
                                    if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                                    }
                                    traceBeginAndSlog("StartClipboardService");
                                    this.mSystemServiceManager.startService(ClipboardService.class);
                                    traceEnd();
                                    traceBeginAndSlog("AppServiceManager");
                                    this.mSystemServiceManager.startService(AppBindingService.Lifecycle.class);
                                    traceEnd();
                                    sMtkSystemServerIns.startMtkOtherServices();
                                    this.mHelper.startOtherServices();
                                    traceBeginAndSlog("MakeVibratorServiceReady");
                                    vibrator.systemReady();
                                    traceEnd();
                                    traceBeginAndSlog("MakeLockSettingsServiceReady");
                                    if (lockSettings != null) {
                                    }
                                    traceEnd();
                                    traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                                    traceEnd();
                                    traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                                    this.mSystemServiceManager.startBootPhase(500);
                                    traceEnd();
                                    traceBeginAndSlog("MakeWindowManagerServiceReady");
                                    Slog.i(TAG, "wms systemReady");
                                    wm.systemReady();
                                    traceEnd();
                                    if (safeMode) {
                                    }
                                    Configuration config2 = wm.computeNewConfiguration(0);
                                    DisplayMetrics metrics2 = new DisplayMetrics();
                                    ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics2);
                                    context.getResources().updateConfiguration(config2, metrics2);
                                    systemTheme = context.getTheme();
                                    if (systemTheme.getChangingConfigurations() != 0) {
                                    }
                                    traceBeginAndSlog("MakePowerManagerServiceReady");
                                    Slog.i(TAG, "power manager systemReady");
                                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                    traceEnd();
                                    if (this.mFingerprintService != null) {
                                    }
                                    if (this.mFaceService != null) {
                                    }
                                    if (this.mBiometricService != null) {
                                    }
                                    if (this.mAlipayService != null) {
                                    }
                                    traceBeginAndSlog("StartPermissionPolicyService");
                                    this.mSystemServiceManager.startService(PermissionPolicyService.class);
                                    traceEnd();
                                    if (this.mOppoOperatorManagerService != null) {
                                    }
                                    traceBeginAndSlog("MakePackageManagerServiceReady");
                                    Slog.i(TAG, "Package systemReady");
                                    this.mPackageManagerService.systemReady();
                                    traceEnd();
                                    traceBeginAndSlog("MakeOppoCustomizeServiceReady");
                                    if (this.oppoCustomize != null) {
                                    }
                                    traceEnd();
                                    traceBeginAndSlog("MakeDisplayManagerServiceReady");
                                    Slog.i(TAG, "DisplayManager systemReady");
                                    this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                    traceEnd();
                                    this.mSystemServiceManager.setSafeMode(safeMode);
                                    traceBeginAndSlog("StartDeviceSpecificServices");
                                    String[] classes2 = this.mSystemContext.getResources().getStringArray(17236011);
                                    while (r6 < r7) {
                                    }
                                    traceEnd();
                                    traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                                    traceEnd();
                                    this.mHelper.systemReady();
                                    Slog.i(TAG, "Ams systemReady");
                                    this.mActivityManagerService.systemReady(new Runnable(context, wm, safeMode, iConnectivityManager, networkManagement, networkPolicy, r19, networkStats, location, countryDetector, networkTimeUpdater, inputManager, telephonyRegistry, mediaRouter, oppomodemService, mmsService2) {
                                        /* class com.android.server.$$Lambda$SystemServer$Veuf5e15txqWe0TdLPFg29kfanQ */
                                        private final /* synthetic */ Context f$1;
                                        private final /* synthetic */ CountryDetectorService f$10;
                                        private final /* synthetic */ NetworkTimeUpdateService f$11;
                                        private final /* synthetic */ InputManagerService f$12;
                                        private final /* synthetic */ TelephonyRegistry f$13;
                                        private final /* synthetic */ MediaRouterService f$14;
                                        private final /* synthetic */ OppomodemService f$15;
                                        private final /* synthetic */ MmsServiceBroker f$16;
                                        private final /* synthetic */ WindowManagerService f$2;
                                        private final /* synthetic */ boolean f$3;
                                        private final /* synthetic */ ConnectivityService f$4;
                                        private final /* synthetic */ NetworkManagementService f$5;
                                        private final /* synthetic */ NetworkPolicyManagerService f$6;
                                        private final /* synthetic */ IpSecService f$7;
                                        private final /* synthetic */ NetworkStatsService f$8;
                                        private final /* synthetic */ LocationManagerService f$9;

                                        {
                                            this.f$1 = r4;
                                            this.f$2 = r5;
                                            this.f$3 = r6;
                                            this.f$4 = r7;
                                            this.f$5 = r8;
                                            this.f$6 = r9;
                                            this.f$7 = r10;
                                            this.f$8 = r11;
                                            this.f$9 = r12;
                                            this.f$10 = r13;
                                            this.f$11 = r14;
                                            this.f$12 = r15;
                                            this.f$13 = r16;
                                            this.f$14 = r17;
                                            this.f$15 = r18;
                                            this.f$16 = r19;
                                        }

                                        public final void run() {
                                            SystemServer.this.lambda$startOtherServices$4$SystemServer(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15, this.f$16);
                                        }
                                    }, BOOT_TIMINGS_TRACE_LOG);
                                }
                            } catch (RuntimeException e20) {
                                e15 = e20;
                                telephonyRegistry = telephonyRegistry2;
                                networkManagement = null;
                                vibrator2 = vibratorService;
                                Slog.e("System", "******************************************");
                                Slog.e("System", "************ Failure starting core service", e15);
                                inputManager = inputManager3;
                                vibrator = vibrator2;
                                wm = wm2;
                                safeMode = wm.detectSafeMode();
                                if (safeMode) {
                                }
                                lockSettings = null;
                                if (this.mFactoryTestMode != 1) {
                                }
                                traceBeginAndSlog("MakeDisplayReady");
                                Slog.i(TAG, "displayReady");
                                wm.displayReady();
                                traceEnd();
                                traceBeginAndSlog("StartStorageManagerService");
                                if (!sMtkSystemServerIns.startMtkStorageManagerService()) {
                                }
                                Slog.i(TAG, "mount service");
                                IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
                                traceEnd();
                                traceBeginAndSlog("StartStorageStatsService");
                                this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
                                traceEnd();
                                traceBeginAndSlog("StartUiModeManager");
                                this.mSystemServiceManager.startService(UiModeManagerService.class);
                                traceEnd();
                                if (!this.mOnlyCore) {
                                }
                                traceBeginAndSlog("PerformFstrimIfNeeded");
                                Slog.i(TAG, "performFstrimIfNeeded");
                                this.mPackageManagerService.performFstrimIfNeeded();
                                traceEnd();
                                if (this.mFactoryTestMode == 1) {
                                }
                                if (!isWatch) {
                                }
                                if (isWatch) {
                                }
                                if (!disableSlices) {
                                }
                                if (!disableCameraService) {
                                }
                                if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                                }
                                traceBeginAndSlog("StartStatsCompanionService");
                                this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                                traceEnd();
                                traceBeginAndSlog("StartIncidentCompanionService");
                                this.mSystemServiceManager.startService(IncidentCompanionService.class);
                                traceEnd();
                                if (safeMode) {
                                }
                                traceBeginAndSlog("StartMmsService");
                                MmsServiceBroker mmsService22 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                                }
                                traceBeginAndSlog("StartClipboardService");
                                this.mSystemServiceManager.startService(ClipboardService.class);
                                traceEnd();
                                traceBeginAndSlog("AppServiceManager");
                                this.mSystemServiceManager.startService(AppBindingService.Lifecycle.class);
                                traceEnd();
                                sMtkSystemServerIns.startMtkOtherServices();
                                this.mHelper.startOtherServices();
                                traceBeginAndSlog("MakeVibratorServiceReady");
                                vibrator.systemReady();
                                traceEnd();
                                traceBeginAndSlog("MakeLockSettingsServiceReady");
                                if (lockSettings != null) {
                                }
                                traceEnd();
                                traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                                traceEnd();
                                traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                                this.mSystemServiceManager.startBootPhase(500);
                                traceEnd();
                                traceBeginAndSlog("MakeWindowManagerServiceReady");
                                Slog.i(TAG, "wms systemReady");
                                wm.systemReady();
                                traceEnd();
                                if (safeMode) {
                                }
                                Configuration config22 = wm.computeNewConfiguration(0);
                                DisplayMetrics metrics22 = new DisplayMetrics();
                                ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics22);
                                context.getResources().updateConfiguration(config22, metrics22);
                                systemTheme = context.getTheme();
                                if (systemTheme.getChangingConfigurations() != 0) {
                                }
                                traceBeginAndSlog("MakePowerManagerServiceReady");
                                Slog.i(TAG, "power manager systemReady");
                                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                traceEnd();
                                if (this.mFingerprintService != null) {
                                }
                                if (this.mFaceService != null) {
                                }
                                if (this.mBiometricService != null) {
                                }
                                if (this.mAlipayService != null) {
                                }
                                traceBeginAndSlog("StartPermissionPolicyService");
                                this.mSystemServiceManager.startService(PermissionPolicyService.class);
                                traceEnd();
                                if (this.mOppoOperatorManagerService != null) {
                                }
                                traceBeginAndSlog("MakePackageManagerServiceReady");
                                Slog.i(TAG, "Package systemReady");
                                this.mPackageManagerService.systemReady();
                                traceEnd();
                                traceBeginAndSlog("MakeOppoCustomizeServiceReady");
                                if (this.oppoCustomize != null) {
                                }
                                traceEnd();
                                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                                Slog.i(TAG, "DisplayManager systemReady");
                                this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                                traceEnd();
                                this.mSystemServiceManager.setSafeMode(safeMode);
                                traceBeginAndSlog("StartDeviceSpecificServices");
                                String[] classes22 = this.mSystemContext.getResources().getStringArray(17236011);
                                while (r6 < r7) {
                                }
                                traceEnd();
                                traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                                traceEnd();
                                this.mHelper.systemReady();
                                Slog.i(TAG, "Ams systemReady");
                                this.mActivityManagerService.systemReady(new Runnable(context, wm, safeMode, iConnectivityManager, networkManagement, networkPolicy, r19, networkStats, location, countryDetector, networkTimeUpdater, inputManager, telephonyRegistry, mediaRouter, oppomodemService, mmsService22) {
                                    /* class com.android.server.$$Lambda$SystemServer$Veuf5e15txqWe0TdLPFg29kfanQ */
                                    private final /* synthetic */ Context f$1;
                                    private final /* synthetic */ CountryDetectorService f$10;
                                    private final /* synthetic */ NetworkTimeUpdateService f$11;
                                    private final /* synthetic */ InputManagerService f$12;
                                    private final /* synthetic */ TelephonyRegistry f$13;
                                    private final /* synthetic */ MediaRouterService f$14;
                                    private final /* synthetic */ OppomodemService f$15;
                                    private final /* synthetic */ MmsServiceBroker f$16;
                                    private final /* synthetic */ WindowManagerService f$2;
                                    private final /* synthetic */ boolean f$3;
                                    private final /* synthetic */ ConnectivityService f$4;
                                    private final /* synthetic */ NetworkManagementService f$5;
                                    private final /* synthetic */ NetworkPolicyManagerService f$6;
                                    private final /* synthetic */ IpSecService f$7;
                                    private final /* synthetic */ NetworkStatsService f$8;
                                    private final /* synthetic */ LocationManagerService f$9;

                                    {
                                        this.f$1 = r4;
                                        this.f$2 = r5;
                                        this.f$3 = r6;
                                        this.f$4 = r7;
                                        this.f$5 = r8;
                                        this.f$6 = r9;
                                        this.f$7 = r10;
                                        this.f$8 = r11;
                                        this.f$9 = r12;
                                        this.f$10 = r13;
                                        this.f$11 = r14;
                                        this.f$12 = r15;
                                        this.f$13 = r16;
                                        this.f$14 = r17;
                                        this.f$15 = r18;
                                        this.f$16 = r19;
                                    }

                                    public final void run() {
                                        SystemServer.this.lambda$startOtherServices$4$SystemServer(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15, this.f$16);
                                    }
                                }, BOOT_TIMINGS_TRACE_LOG);
                            }
                        }
                        if (!isWatch) {
                            traceBeginAndSlog("StartConsumerIrService");
                            ServiceManager.addService("consumer_ir", (IBinder) new ConsumerIrService(context));
                            traceEnd();
                        }
                    } catch (RuntimeException e21) {
                        e15 = e21;
                        telephonyRegistry = telephonyRegistry2;
                        networkManagement = null;
                        vibrator2 = vibratorService;
                        Slog.e("System", "******************************************");
                        Slog.e("System", "************ Failure starting core service", e15);
                        inputManager = inputManager3;
                        vibrator = vibrator2;
                        wm = wm2;
                        safeMode = wm.detectSafeMode();
                        if (safeMode) {
                        }
                        lockSettings = null;
                        if (this.mFactoryTestMode != 1) {
                        }
                        traceBeginAndSlog("MakeDisplayReady");
                        Slog.i(TAG, "displayReady");
                        wm.displayReady();
                        traceEnd();
                        traceBeginAndSlog("StartStorageManagerService");
                        if (!sMtkSystemServerIns.startMtkStorageManagerService()) {
                        }
                        Slog.i(TAG, "mount service");
                        IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
                        traceEnd();
                        traceBeginAndSlog("StartStorageStatsService");
                        this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
                        traceEnd();
                        traceBeginAndSlog("StartUiModeManager");
                        this.mSystemServiceManager.startService(UiModeManagerService.class);
                        traceEnd();
                        if (!this.mOnlyCore) {
                        }
                        traceBeginAndSlog("PerformFstrimIfNeeded");
                        Slog.i(TAG, "performFstrimIfNeeded");
                        this.mPackageManagerService.performFstrimIfNeeded();
                        traceEnd();
                        if (this.mFactoryTestMode == 1) {
                        }
                        if (!isWatch) {
                        }
                        if (isWatch) {
                        }
                        if (!disableSlices) {
                        }
                        if (!disableCameraService) {
                        }
                        if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                        }
                        traceBeginAndSlog("StartStatsCompanionService");
                        this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                        traceEnd();
                        traceBeginAndSlog("StartIncidentCompanionService");
                        this.mSystemServiceManager.startService(IncidentCompanionService.class);
                        traceEnd();
                        if (safeMode) {
                        }
                        traceBeginAndSlog("StartMmsService");
                        MmsServiceBroker mmsService222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                        }
                        traceBeginAndSlog("StartClipboardService");
                        this.mSystemServiceManager.startService(ClipboardService.class);
                        traceEnd();
                        traceBeginAndSlog("AppServiceManager");
                        this.mSystemServiceManager.startService(AppBindingService.Lifecycle.class);
                        traceEnd();
                        sMtkSystemServerIns.startMtkOtherServices();
                        this.mHelper.startOtherServices();
                        traceBeginAndSlog("MakeVibratorServiceReady");
                        vibrator.systemReady();
                        traceEnd();
                        traceBeginAndSlog("MakeLockSettingsServiceReady");
                        if (lockSettings != null) {
                        }
                        traceEnd();
                        traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                        traceEnd();
                        traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                        this.mSystemServiceManager.startBootPhase(500);
                        traceEnd();
                        traceBeginAndSlog("MakeWindowManagerServiceReady");
                        Slog.i(TAG, "wms systemReady");
                        wm.systemReady();
                        traceEnd();
                        if (safeMode) {
                        }
                        Configuration config222 = wm.computeNewConfiguration(0);
                        DisplayMetrics metrics222 = new DisplayMetrics();
                        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics222);
                        context.getResources().updateConfiguration(config222, metrics222);
                        systemTheme = context.getTheme();
                        if (systemTheme.getChangingConfigurations() != 0) {
                        }
                        traceBeginAndSlog("MakePowerManagerServiceReady");
                        Slog.i(TAG, "power manager systemReady");
                        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                        traceEnd();
                        if (this.mFingerprintService != null) {
                        }
                        if (this.mFaceService != null) {
                        }
                        if (this.mBiometricService != null) {
                        }
                        if (this.mAlipayService != null) {
                        }
                        traceBeginAndSlog("StartPermissionPolicyService");
                        this.mSystemServiceManager.startService(PermissionPolicyService.class);
                        traceEnd();
                        if (this.mOppoOperatorManagerService != null) {
                        }
                        traceBeginAndSlog("MakePackageManagerServiceReady");
                        Slog.i(TAG, "Package systemReady");
                        this.mPackageManagerService.systemReady();
                        traceEnd();
                        traceBeginAndSlog("MakeOppoCustomizeServiceReady");
                        if (this.oppoCustomize != null) {
                        }
                        traceEnd();
                        traceBeginAndSlog("MakeDisplayManagerServiceReady");
                        Slog.i(TAG, "DisplayManager systemReady");
                        this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                        traceEnd();
                        this.mSystemServiceManager.setSafeMode(safeMode);
                        traceBeginAndSlog("StartDeviceSpecificServices");
                        String[] classes222 = this.mSystemContext.getResources().getStringArray(17236011);
                        while (r6 < r7) {
                        }
                        traceEnd();
                        traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                        traceEnd();
                        this.mHelper.systemReady();
                        Slog.i(TAG, "Ams systemReady");
                        this.mActivityManagerService.systemReady(new Runnable(context, wm, safeMode, iConnectivityManager, networkManagement, networkPolicy, r19, networkStats, location, countryDetector, networkTimeUpdater, inputManager, telephonyRegistry, mediaRouter, oppomodemService, mmsService222) {
                            /* class com.android.server.$$Lambda$SystemServer$Veuf5e15txqWe0TdLPFg29kfanQ */
                            private final /* synthetic */ Context f$1;
                            private final /* synthetic */ CountryDetectorService f$10;
                            private final /* synthetic */ NetworkTimeUpdateService f$11;
                            private final /* synthetic */ InputManagerService f$12;
                            private final /* synthetic */ TelephonyRegistry f$13;
                            private final /* synthetic */ MediaRouterService f$14;
                            private final /* synthetic */ OppomodemService f$15;
                            private final /* synthetic */ MmsServiceBroker f$16;
                            private final /* synthetic */ WindowManagerService f$2;
                            private final /* synthetic */ boolean f$3;
                            private final /* synthetic */ ConnectivityService f$4;
                            private final /* synthetic */ NetworkManagementService f$5;
                            private final /* synthetic */ NetworkPolicyManagerService f$6;
                            private final /* synthetic */ IpSecService f$7;
                            private final /* synthetic */ NetworkStatsService f$8;
                            private final /* synthetic */ LocationManagerService f$9;

                            {
                                this.f$1 = r4;
                                this.f$2 = r5;
                                this.f$3 = r6;
                                this.f$4 = r7;
                                this.f$5 = r8;
                                this.f$6 = r9;
                                this.f$7 = r10;
                                this.f$8 = r11;
                                this.f$9 = r12;
                                this.f$10 = r13;
                                this.f$11 = r14;
                                this.f$12 = r15;
                                this.f$13 = r16;
                                this.f$14 = r17;
                                this.f$15 = r18;
                                this.f$16 = r19;
                            }

                            public final void run() {
                                SystemServer.this.lambda$startOtherServices$4$SystemServer(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15, this.f$16);
                            }
                        }, BOOT_TIMINGS_TRACE_LOG);
                    }
                } catch (RuntimeException e22) {
                    e15 = e22;
                    telephonyRegistry = telephonyRegistry2;
                    networkManagement = null;
                    vibrator2 = vibratorService;
                    Slog.e("System", "******************************************");
                    Slog.e("System", "************ Failure starting core service", e15);
                    inputManager = inputManager3;
                    vibrator = vibrator2;
                    wm = wm2;
                    safeMode = wm.detectSafeMode();
                    if (safeMode) {
                    }
                    lockSettings = null;
                    if (this.mFactoryTestMode != 1) {
                    }
                    traceBeginAndSlog("MakeDisplayReady");
                    Slog.i(TAG, "displayReady");
                    wm.displayReady();
                    traceEnd();
                    traceBeginAndSlog("StartStorageManagerService");
                    if (!sMtkSystemServerIns.startMtkStorageManagerService()) {
                    }
                    Slog.i(TAG, "mount service");
                    IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
                    traceEnd();
                    traceBeginAndSlog("StartStorageStatsService");
                    this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
                    traceEnd();
                    traceBeginAndSlog("StartUiModeManager");
                    this.mSystemServiceManager.startService(UiModeManagerService.class);
                    traceEnd();
                    if (!this.mOnlyCore) {
                    }
                    traceBeginAndSlog("PerformFstrimIfNeeded");
                    Slog.i(TAG, "performFstrimIfNeeded");
                    this.mPackageManagerService.performFstrimIfNeeded();
                    traceEnd();
                    if (this.mFactoryTestMode == 1) {
                    }
                    if (!isWatch) {
                    }
                    if (isWatch) {
                    }
                    if (!disableSlices) {
                    }
                    if (!disableCameraService) {
                    }
                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                    }
                    traceBeginAndSlog("StartStatsCompanionService");
                    this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                    traceEnd();
                    traceBeginAndSlog("StartIncidentCompanionService");
                    this.mSystemServiceManager.startService(IncidentCompanionService.class);
                    traceEnd();
                    if (safeMode) {
                    }
                    traceBeginAndSlog("StartMmsService");
                    MmsServiceBroker mmsService2222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                    }
                    traceBeginAndSlog("StartClipboardService");
                    this.mSystemServiceManager.startService(ClipboardService.class);
                    traceEnd();
                    traceBeginAndSlog("AppServiceManager");
                    this.mSystemServiceManager.startService(AppBindingService.Lifecycle.class);
                    traceEnd();
                    sMtkSystemServerIns.startMtkOtherServices();
                    this.mHelper.startOtherServices();
                    traceBeginAndSlog("MakeVibratorServiceReady");
                    vibrator.systemReady();
                    traceEnd();
                    traceBeginAndSlog("MakeLockSettingsServiceReady");
                    if (lockSettings != null) {
                    }
                    traceEnd();
                    traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                    traceEnd();
                    traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                    this.mSystemServiceManager.startBootPhase(500);
                    traceEnd();
                    traceBeginAndSlog("MakeWindowManagerServiceReady");
                    Slog.i(TAG, "wms systemReady");
                    wm.systemReady();
                    traceEnd();
                    if (safeMode) {
                    }
                    Configuration config2222 = wm.computeNewConfiguration(0);
                    DisplayMetrics metrics2222 = new DisplayMetrics();
                    ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics2222);
                    context.getResources().updateConfiguration(config2222, metrics2222);
                    systemTheme = context.getTheme();
                    if (systemTheme.getChangingConfigurations() != 0) {
                    }
                    traceBeginAndSlog("MakePowerManagerServiceReady");
                    Slog.i(TAG, "power manager systemReady");
                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                    traceEnd();
                    if (this.mFingerprintService != null) {
                    }
                    if (this.mFaceService != null) {
                    }
                    if (this.mBiometricService != null) {
                    }
                    if (this.mAlipayService != null) {
                    }
                    traceBeginAndSlog("StartPermissionPolicyService");
                    this.mSystemServiceManager.startService(PermissionPolicyService.class);
                    traceEnd();
                    if (this.mOppoOperatorManagerService != null) {
                    }
                    traceBeginAndSlog("MakePackageManagerServiceReady");
                    Slog.i(TAG, "Package systemReady");
                    this.mPackageManagerService.systemReady();
                    traceEnd();
                    traceBeginAndSlog("MakeOppoCustomizeServiceReady");
                    if (this.oppoCustomize != null) {
                    }
                    traceEnd();
                    traceBeginAndSlog("MakeDisplayManagerServiceReady");
                    Slog.i(TAG, "DisplayManager systemReady");
                    this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                    traceEnd();
                    this.mSystemServiceManager.setSafeMode(safeMode);
                    traceBeginAndSlog("StartDeviceSpecificServices");
                    String[] classes2222 = this.mSystemContext.getResources().getStringArray(17236011);
                    while (r6 < r7) {
                    }
                    traceEnd();
                    traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                    traceEnd();
                    this.mHelper.systemReady();
                    Slog.i(TAG, "Ams systemReady");
                    this.mActivityManagerService.systemReady(new Runnable(context, wm, safeMode, iConnectivityManager, networkManagement, networkPolicy, r19, networkStats, location, countryDetector, networkTimeUpdater, inputManager, telephonyRegistry, mediaRouter, oppomodemService, mmsService2222) {
                        /* class com.android.server.$$Lambda$SystemServer$Veuf5e15txqWe0TdLPFg29kfanQ */
                        private final /* synthetic */ Context f$1;
                        private final /* synthetic */ CountryDetectorService f$10;
                        private final /* synthetic */ NetworkTimeUpdateService f$11;
                        private final /* synthetic */ InputManagerService f$12;
                        private final /* synthetic */ TelephonyRegistry f$13;
                        private final /* synthetic */ MediaRouterService f$14;
                        private final /* synthetic */ OppomodemService f$15;
                        private final /* synthetic */ MmsServiceBroker f$16;
                        private final /* synthetic */ WindowManagerService f$2;
                        private final /* synthetic */ boolean f$3;
                        private final /* synthetic */ ConnectivityService f$4;
                        private final /* synthetic */ NetworkManagementService f$5;
                        private final /* synthetic */ NetworkPolicyManagerService f$6;
                        private final /* synthetic */ IpSecService f$7;
                        private final /* synthetic */ NetworkStatsService f$8;
                        private final /* synthetic */ LocationManagerService f$9;

                        {
                            this.f$1 = r4;
                            this.f$2 = r5;
                            this.f$3 = r6;
                            this.f$4 = r7;
                            this.f$5 = r8;
                            this.f$6 = r9;
                            this.f$7 = r10;
                            this.f$8 = r11;
                            this.f$9 = r12;
                            this.f$10 = r13;
                            this.f$11 = r14;
                            this.f$12 = r15;
                            this.f$13 = r16;
                            this.f$14 = r17;
                            this.f$15 = r18;
                            this.f$16 = r19;
                        }

                        public final void run() {
                            SystemServer.this.lambda$startOtherServices$4$SystemServer(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15, this.f$16);
                        }
                    }, BOOT_TIMINGS_TRACE_LOG);
                }
                try {
                    traceBeginAndSlog("StartAlarmManagerService");
                    if (!sMtkSystemServerIns.startMtkAlarmManagerService()) {
                        try {
                            this.mSystemServiceManager.startService(new AlarmManagerService(context));
                        } catch (RuntimeException e23) {
                            e15 = e23;
                            telephonyRegistry = telephonyRegistry2;
                            networkManagement = null;
                            vibrator2 = vibratorService;
                        }
                    }
                    Slog.i(TAG, "AlarmManager Service");
                    traceEnd();
                    traceBeginAndSlog("StartInputManagerService");
                    inputManager2 = new InputManagerService(context);
                    try {
                        traceEnd();
                        traceBeginAndSlog("StartWindowManagerService");
                        ConcurrentUtils.waitForFutureNoInterrupt(this.mSensorServiceStart, START_SENSOR_SERVICE);
                        this.mSensorServiceStart = null;
                        vibrator = vibratorService;
                        r12 = inputManager2;
                        telephonyRegistry = telephonyRegistry2;
                        networkManagement = null;
                    } catch (RuntimeException e24) {
                        e15 = e24;
                        telephonyRegistry = telephonyRegistry2;
                        networkManagement = null;
                        inputManager3 = inputManager2;
                        vibrator2 = vibratorService;
                        Slog.e("System", "******************************************");
                        Slog.e("System", "************ Failure starting core service", e15);
                        inputManager = inputManager3;
                        vibrator = vibrator2;
                        wm = wm2;
                        safeMode = wm.detectSafeMode();
                        if (safeMode) {
                        }
                        lockSettings = null;
                        if (this.mFactoryTestMode != 1) {
                        }
                        traceBeginAndSlog("MakeDisplayReady");
                        Slog.i(TAG, "displayReady");
                        wm.displayReady();
                        traceEnd();
                        traceBeginAndSlog("StartStorageManagerService");
                        if (!sMtkSystemServerIns.startMtkStorageManagerService()) {
                        }
                        Slog.i(TAG, "mount service");
                        IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
                        traceEnd();
                        traceBeginAndSlog("StartStorageStatsService");
                        this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
                        traceEnd();
                        traceBeginAndSlog("StartUiModeManager");
                        this.mSystemServiceManager.startService(UiModeManagerService.class);
                        traceEnd();
                        if (!this.mOnlyCore) {
                        }
                        traceBeginAndSlog("PerformFstrimIfNeeded");
                        Slog.i(TAG, "performFstrimIfNeeded");
                        this.mPackageManagerService.performFstrimIfNeeded();
                        traceEnd();
                        if (this.mFactoryTestMode == 1) {
                        }
                        if (!isWatch) {
                        }
                        if (isWatch) {
                        }
                        if (!disableSlices) {
                        }
                        if (!disableCameraService) {
                        }
                        if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                        }
                        traceBeginAndSlog("StartStatsCompanionService");
                        this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                        traceEnd();
                        traceBeginAndSlog("StartIncidentCompanionService");
                        this.mSystemServiceManager.startService(IncidentCompanionService.class);
                        traceEnd();
                        if (safeMode) {
                        }
                        traceBeginAndSlog("StartMmsService");
                        MmsServiceBroker mmsService22222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                        }
                        traceBeginAndSlog("StartClipboardService");
                        this.mSystemServiceManager.startService(ClipboardService.class);
                        traceEnd();
                        traceBeginAndSlog("AppServiceManager");
                        this.mSystemServiceManager.startService(AppBindingService.Lifecycle.class);
                        traceEnd();
                        sMtkSystemServerIns.startMtkOtherServices();
                        this.mHelper.startOtherServices();
                        traceBeginAndSlog("MakeVibratorServiceReady");
                        vibrator.systemReady();
                        traceEnd();
                        traceBeginAndSlog("MakeLockSettingsServiceReady");
                        if (lockSettings != null) {
                        }
                        traceEnd();
                        traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                        traceEnd();
                        traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                        this.mSystemServiceManager.startBootPhase(500);
                        traceEnd();
                        traceBeginAndSlog("MakeWindowManagerServiceReady");
                        Slog.i(TAG, "wms systemReady");
                        wm.systemReady();
                        traceEnd();
                        if (safeMode) {
                        }
                        Configuration config22222 = wm.computeNewConfiguration(0);
                        DisplayMetrics metrics22222 = new DisplayMetrics();
                        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics22222);
                        context.getResources().updateConfiguration(config22222, metrics22222);
                        systemTheme = context.getTheme();
                        if (systemTheme.getChangingConfigurations() != 0) {
                        }
                        traceBeginAndSlog("MakePowerManagerServiceReady");
                        Slog.i(TAG, "power manager systemReady");
                        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                        traceEnd();
                        if (this.mFingerprintService != null) {
                        }
                        if (this.mFaceService != null) {
                        }
                        if (this.mBiometricService != null) {
                        }
                        if (this.mAlipayService != null) {
                        }
                        traceBeginAndSlog("StartPermissionPolicyService");
                        this.mSystemServiceManager.startService(PermissionPolicyService.class);
                        traceEnd();
                        if (this.mOppoOperatorManagerService != null) {
                        }
                        traceBeginAndSlog("MakePackageManagerServiceReady");
                        Slog.i(TAG, "Package systemReady");
                        this.mPackageManagerService.systemReady();
                        traceEnd();
                        traceBeginAndSlog("MakeOppoCustomizeServiceReady");
                        if (this.oppoCustomize != null) {
                        }
                        traceEnd();
                        traceBeginAndSlog("MakeDisplayManagerServiceReady");
                        Slog.i(TAG, "DisplayManager systemReady");
                        this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                        traceEnd();
                        this.mSystemServiceManager.setSafeMode(safeMode);
                        traceBeginAndSlog("StartDeviceSpecificServices");
                        String[] classes22222 = this.mSystemContext.getResources().getStringArray(17236011);
                        while (r6 < r7) {
                        }
                        traceEnd();
                        traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                        traceEnd();
                        this.mHelper.systemReady();
                        Slog.i(TAG, "Ams systemReady");
                        this.mActivityManagerService.systemReady(new Runnable(context, wm, safeMode, iConnectivityManager, networkManagement, networkPolicy, r19, networkStats, location, countryDetector, networkTimeUpdater, inputManager, telephonyRegistry, mediaRouter, oppomodemService, mmsService22222) {
                            /* class com.android.server.$$Lambda$SystemServer$Veuf5e15txqWe0TdLPFg29kfanQ */
                            private final /* synthetic */ Context f$1;
                            private final /* synthetic */ CountryDetectorService f$10;
                            private final /* synthetic */ NetworkTimeUpdateService f$11;
                            private final /* synthetic */ InputManagerService f$12;
                            private final /* synthetic */ TelephonyRegistry f$13;
                            private final /* synthetic */ MediaRouterService f$14;
                            private final /* synthetic */ OppomodemService f$15;
                            private final /* synthetic */ MmsServiceBroker f$16;
                            private final /* synthetic */ WindowManagerService f$2;
                            private final /* synthetic */ boolean f$3;
                            private final /* synthetic */ ConnectivityService f$4;
                            private final /* synthetic */ NetworkManagementService f$5;
                            private final /* synthetic */ NetworkPolicyManagerService f$6;
                            private final /* synthetic */ IpSecService f$7;
                            private final /* synthetic */ NetworkStatsService f$8;
                            private final /* synthetic */ LocationManagerService f$9;

                            {
                                this.f$1 = r4;
                                this.f$2 = r5;
                                this.f$3 = r6;
                                this.f$4 = r7;
                                this.f$5 = r8;
                                this.f$6 = r9;
                                this.f$7 = r10;
                                this.f$8 = r11;
                                this.f$9 = r12;
                                this.f$10 = r13;
                                this.f$11 = r14;
                                this.f$12 = r15;
                                this.f$13 = r16;
                                this.f$14 = r17;
                                this.f$15 = r18;
                                this.f$16 = r19;
                            }

                            public final void run() {
                                SystemServer.this.lambda$startOtherServices$4$SystemServer(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15, this.f$16);
                            }
                        }, BOOT_TIMINGS_TRACE_LOG);
                    }
                } catch (RuntimeException e25) {
                    e15 = e25;
                    telephonyRegistry = telephonyRegistry2;
                    networkManagement = null;
                    vibrator2 = vibratorService;
                    Slog.e("System", "******************************************");
                    Slog.e("System", "************ Failure starting core service", e15);
                    inputManager = inputManager3;
                    vibrator = vibrator2;
                    wm = wm2;
                    safeMode = wm.detectSafeMode();
                    if (safeMode) {
                    }
                    lockSettings = null;
                    if (this.mFactoryTestMode != 1) {
                    }
                    traceBeginAndSlog("MakeDisplayReady");
                    Slog.i(TAG, "displayReady");
                    wm.displayReady();
                    traceEnd();
                    traceBeginAndSlog("StartStorageManagerService");
                    if (!sMtkSystemServerIns.startMtkStorageManagerService()) {
                    }
                    Slog.i(TAG, "mount service");
                    IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
                    traceEnd();
                    traceBeginAndSlog("StartStorageStatsService");
                    this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
                    traceEnd();
                    traceBeginAndSlog("StartUiModeManager");
                    this.mSystemServiceManager.startService(UiModeManagerService.class);
                    traceEnd();
                    if (!this.mOnlyCore) {
                    }
                    traceBeginAndSlog("PerformFstrimIfNeeded");
                    Slog.i(TAG, "performFstrimIfNeeded");
                    this.mPackageManagerService.performFstrimIfNeeded();
                    traceEnd();
                    if (this.mFactoryTestMode == 1) {
                    }
                    if (!isWatch) {
                    }
                    if (isWatch) {
                    }
                    if (!disableSlices) {
                    }
                    if (!disableCameraService) {
                    }
                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                    }
                    traceBeginAndSlog("StartStatsCompanionService");
                    this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                    traceEnd();
                    traceBeginAndSlog("StartIncidentCompanionService");
                    this.mSystemServiceManager.startService(IncidentCompanionService.class);
                    traceEnd();
                    if (safeMode) {
                    }
                    traceBeginAndSlog("StartMmsService");
                    MmsServiceBroker mmsService222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                    }
                    traceBeginAndSlog("StartClipboardService");
                    this.mSystemServiceManager.startService(ClipboardService.class);
                    traceEnd();
                    traceBeginAndSlog("AppServiceManager");
                    this.mSystemServiceManager.startService(AppBindingService.Lifecycle.class);
                    traceEnd();
                    sMtkSystemServerIns.startMtkOtherServices();
                    this.mHelper.startOtherServices();
                    traceBeginAndSlog("MakeVibratorServiceReady");
                    vibrator.systemReady();
                    traceEnd();
                    traceBeginAndSlog("MakeLockSettingsServiceReady");
                    if (lockSettings != null) {
                    }
                    traceEnd();
                    traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                    traceEnd();
                    traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                    this.mSystemServiceManager.startBootPhase(500);
                    traceEnd();
                    traceBeginAndSlog("MakeWindowManagerServiceReady");
                    Slog.i(TAG, "wms systemReady");
                    wm.systemReady();
                    traceEnd();
                    if (safeMode) {
                    }
                    Configuration config222222 = wm.computeNewConfiguration(0);
                    DisplayMetrics metrics222222 = new DisplayMetrics();
                    ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics222222);
                    context.getResources().updateConfiguration(config222222, metrics222222);
                    systemTheme = context.getTheme();
                    if (systemTheme.getChangingConfigurations() != 0) {
                    }
                    traceBeginAndSlog("MakePowerManagerServiceReady");
                    Slog.i(TAG, "power manager systemReady");
                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                    traceEnd();
                    if (this.mFingerprintService != null) {
                    }
                    if (this.mFaceService != null) {
                    }
                    if (this.mBiometricService != null) {
                    }
                    if (this.mAlipayService != null) {
                    }
                    traceBeginAndSlog("StartPermissionPolicyService");
                    this.mSystemServiceManager.startService(PermissionPolicyService.class);
                    traceEnd();
                    if (this.mOppoOperatorManagerService != null) {
                    }
                    traceBeginAndSlog("MakePackageManagerServiceReady");
                    Slog.i(TAG, "Package systemReady");
                    this.mPackageManagerService.systemReady();
                    traceEnd();
                    traceBeginAndSlog("MakeOppoCustomizeServiceReady");
                    if (this.oppoCustomize != null) {
                    }
                    traceEnd();
                    traceBeginAndSlog("MakeDisplayManagerServiceReady");
                    Slog.i(TAG, "DisplayManager systemReady");
                    this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                    traceEnd();
                    this.mSystemServiceManager.setSafeMode(safeMode);
                    traceBeginAndSlog("StartDeviceSpecificServices");
                    String[] classes222222 = this.mSystemContext.getResources().getStringArray(17236011);
                    while (r6 < r7) {
                    }
                    traceEnd();
                    traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                    traceEnd();
                    this.mHelper.systemReady();
                    Slog.i(TAG, "Ams systemReady");
                    this.mActivityManagerService.systemReady(new Runnable(context, wm, safeMode, iConnectivityManager, networkManagement, networkPolicy, r19, networkStats, location, countryDetector, networkTimeUpdater, inputManager, telephonyRegistry, mediaRouter, oppomodemService, mmsService222222) {
                        /* class com.android.server.$$Lambda$SystemServer$Veuf5e15txqWe0TdLPFg29kfanQ */
                        private final /* synthetic */ Context f$1;
                        private final /* synthetic */ CountryDetectorService f$10;
                        private final /* synthetic */ NetworkTimeUpdateService f$11;
                        private final /* synthetic */ InputManagerService f$12;
                        private final /* synthetic */ TelephonyRegistry f$13;
                        private final /* synthetic */ MediaRouterService f$14;
                        private final /* synthetic */ OppomodemService f$15;
                        private final /* synthetic */ MmsServiceBroker f$16;
                        private final /* synthetic */ WindowManagerService f$2;
                        private final /* synthetic */ boolean f$3;
                        private final /* synthetic */ ConnectivityService f$4;
                        private final /* synthetic */ NetworkManagementService f$5;
                        private final /* synthetic */ NetworkPolicyManagerService f$6;
                        private final /* synthetic */ IpSecService f$7;
                        private final /* synthetic */ NetworkStatsService f$8;
                        private final /* synthetic */ LocationManagerService f$9;

                        {
                            this.f$1 = r4;
                            this.f$2 = r5;
                            this.f$3 = r6;
                            this.f$4 = r7;
                            this.f$5 = r8;
                            this.f$6 = r9;
                            this.f$7 = r10;
                            this.f$8 = r11;
                            this.f$9 = r12;
                            this.f$10 = r13;
                            this.f$11 = r14;
                            this.f$12 = r15;
                            this.f$13 = r16;
                            this.f$14 = r17;
                            this.f$15 = r18;
                            this.f$16 = r19;
                        }

                        public final void run() {
                            SystemServer.this.lambda$startOtherServices$4$SystemServer(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15, this.f$16);
                        }
                    }, BOOT_TIMINGS_TRACE_LOG);
                }
                try {
                    ?? main = WindowManagerService.main(context, inputManager2, !this.mFirstBoot, this.mOnlyCore, this.mHelper.startPhoneWindowManager(), this.mActivityManagerService.mActivityTaskManager);
                    try {
                        ServiceManager.addService("window", (IBinder) main, false, 17);
                        ServiceManager.addService("input", (IBinder) r12, false, 1);
                        traceEnd();
                        traceBeginAndSlog("SetWindowManagerService");
                        this.mActivityManagerService.setWindowManager(main);
                        traceEnd();
                        traceBeginAndSlog("WindowManagerServiceOnInitReady");
                        main.onInitReady();
                        traceEnd();
                        SystemServerInitThreadPool.get().submit($$Lambda$SystemServer$NlJmG18aPrQduhRqASIdcn7G0z8.INSTANCE, START_HIDL_SERVICES);
                        if (!isWatch && enableVrService) {
                            traceBeginAndSlog("StartVrManagerService");
                            this.mSystemServiceManager.startService(VrManagerService.class);
                            traceEnd();
                        }
                        traceBeginAndSlog("StartInputManager");
                        r12.setWindowManagerCallbacks(main.getInputManagerCallback());
                        r12.start();
                        traceEnd();
                        traceBeginAndSlog("DisplayManagerWindowManagerAndInputReady");
                        this.mDisplayManagerService.windowManagerAndInputReady();
                        traceEnd();
                        if (this.mFactoryTestMode == 1) {
                            Slog.i(TAG, "No Bluetooth Service (factory test)");
                        } else if (!context.getPackageManager().hasSystemFeature("android.hardware.bluetooth")) {
                            Slog.i(TAG, "No Bluetooth Service (Bluetooth Hardware Not Present)");
                        } else {
                            traceBeginAndSlog("StartBluetoothService");
                            this.mSystemServiceManager.startService(BluetoothService.class);
                            traceEnd();
                        }
                        traceBeginAndSlog("IpConnectivityMetrics");
                        this.mSystemServiceManager.startService(IpConnectivityMetrics.class);
                        traceEnd();
                        traceBeginAndSlog("NetworkWatchlistService");
                        this.mSystemServiceManager.startService(NetworkWatchlistService.Lifecycle.class);
                        traceEnd();
                        traceBeginAndSlog("PinnerService");
                        this.mSystemServiceManager.startService(PinnerService.class);
                        traceEnd();
                        traceBeginAndSlog("SignedConfigService");
                        SignedConfigService.registerUpdateReceiver(this.mSystemContext);
                        traceEnd();
                        wm = main;
                        inputManager = r12;
                    } catch (RuntimeException e26) {
                        e15 = e26;
                        wm2 = main;
                        inputManager3 = r12;
                        vibrator2 = vibrator;
                        Slog.e("System", "******************************************");
                        Slog.e("System", "************ Failure starting core service", e15);
                        inputManager = inputManager3;
                        vibrator = vibrator2;
                        wm = wm2;
                        safeMode = wm.detectSafeMode();
                        if (safeMode) {
                        }
                        lockSettings = null;
                        if (this.mFactoryTestMode != 1) {
                        }
                        traceBeginAndSlog("MakeDisplayReady");
                        Slog.i(TAG, "displayReady");
                        wm.displayReady();
                        traceEnd();
                        traceBeginAndSlog("StartStorageManagerService");
                        if (!sMtkSystemServerIns.startMtkStorageManagerService()) {
                        }
                        Slog.i(TAG, "mount service");
                        IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
                        traceEnd();
                        traceBeginAndSlog("StartStorageStatsService");
                        this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
                        traceEnd();
                        traceBeginAndSlog("StartUiModeManager");
                        this.mSystemServiceManager.startService(UiModeManagerService.class);
                        traceEnd();
                        if (!this.mOnlyCore) {
                        }
                        traceBeginAndSlog("PerformFstrimIfNeeded");
                        Slog.i(TAG, "performFstrimIfNeeded");
                        this.mPackageManagerService.performFstrimIfNeeded();
                        traceEnd();
                        if (this.mFactoryTestMode == 1) {
                        }
                        if (!isWatch) {
                        }
                        if (isWatch) {
                        }
                        if (!disableSlices) {
                        }
                        if (!disableCameraService) {
                        }
                        if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                        }
                        traceBeginAndSlog("StartStatsCompanionService");
                        this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                        traceEnd();
                        traceBeginAndSlog("StartIncidentCompanionService");
                        this.mSystemServiceManager.startService(IncidentCompanionService.class);
                        traceEnd();
                        if (safeMode) {
                        }
                        traceBeginAndSlog("StartMmsService");
                        MmsServiceBroker mmsService2222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                        }
                        traceBeginAndSlog("StartClipboardService");
                        this.mSystemServiceManager.startService(ClipboardService.class);
                        traceEnd();
                        traceBeginAndSlog("AppServiceManager");
                        this.mSystemServiceManager.startService(AppBindingService.Lifecycle.class);
                        traceEnd();
                        sMtkSystemServerIns.startMtkOtherServices();
                        this.mHelper.startOtherServices();
                        traceBeginAndSlog("MakeVibratorServiceReady");
                        vibrator.systemReady();
                        traceEnd();
                        traceBeginAndSlog("MakeLockSettingsServiceReady");
                        if (lockSettings != null) {
                        }
                        traceEnd();
                        traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                        traceEnd();
                        traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                        this.mSystemServiceManager.startBootPhase(500);
                        traceEnd();
                        traceBeginAndSlog("MakeWindowManagerServiceReady");
                        Slog.i(TAG, "wms systemReady");
                        wm.systemReady();
                        traceEnd();
                        if (safeMode) {
                        }
                        Configuration config2222222 = wm.computeNewConfiguration(0);
                        DisplayMetrics metrics2222222 = new DisplayMetrics();
                        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics2222222);
                        context.getResources().updateConfiguration(config2222222, metrics2222222);
                        systemTheme = context.getTheme();
                        if (systemTheme.getChangingConfigurations() != 0) {
                        }
                        traceBeginAndSlog("MakePowerManagerServiceReady");
                        Slog.i(TAG, "power manager systemReady");
                        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                        traceEnd();
                        if (this.mFingerprintService != null) {
                        }
                        if (this.mFaceService != null) {
                        }
                        if (this.mBiometricService != null) {
                        }
                        if (this.mAlipayService != null) {
                        }
                        traceBeginAndSlog("StartPermissionPolicyService");
                        this.mSystemServiceManager.startService(PermissionPolicyService.class);
                        traceEnd();
                        if (this.mOppoOperatorManagerService != null) {
                        }
                        traceBeginAndSlog("MakePackageManagerServiceReady");
                        Slog.i(TAG, "Package systemReady");
                        this.mPackageManagerService.systemReady();
                        traceEnd();
                        traceBeginAndSlog("MakeOppoCustomizeServiceReady");
                        if (this.oppoCustomize != null) {
                        }
                        traceEnd();
                        traceBeginAndSlog("MakeDisplayManagerServiceReady");
                        Slog.i(TAG, "DisplayManager systemReady");
                        this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                        traceEnd();
                        this.mSystemServiceManager.setSafeMode(safeMode);
                        traceBeginAndSlog("StartDeviceSpecificServices");
                        String[] classes2222222 = this.mSystemContext.getResources().getStringArray(17236011);
                        while (r6 < r7) {
                        }
                        traceEnd();
                        traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                        traceEnd();
                        this.mHelper.systemReady();
                        Slog.i(TAG, "Ams systemReady");
                        this.mActivityManagerService.systemReady(new Runnable(context, wm, safeMode, iConnectivityManager, networkManagement, networkPolicy, r19, networkStats, location, countryDetector, networkTimeUpdater, inputManager, telephonyRegistry, mediaRouter, oppomodemService, mmsService2222222) {
                            /* class com.android.server.$$Lambda$SystemServer$Veuf5e15txqWe0TdLPFg29kfanQ */
                            private final /* synthetic */ Context f$1;
                            private final /* synthetic */ CountryDetectorService f$10;
                            private final /* synthetic */ NetworkTimeUpdateService f$11;
                            private final /* synthetic */ InputManagerService f$12;
                            private final /* synthetic */ TelephonyRegistry f$13;
                            private final /* synthetic */ MediaRouterService f$14;
                            private final /* synthetic */ OppomodemService f$15;
                            private final /* synthetic */ MmsServiceBroker f$16;
                            private final /* synthetic */ WindowManagerService f$2;
                            private final /* synthetic */ boolean f$3;
                            private final /* synthetic */ ConnectivityService f$4;
                            private final /* synthetic */ NetworkManagementService f$5;
                            private final /* synthetic */ NetworkPolicyManagerService f$6;
                            private final /* synthetic */ IpSecService f$7;
                            private final /* synthetic */ NetworkStatsService f$8;
                            private final /* synthetic */ LocationManagerService f$9;

                            {
                                this.f$1 = r4;
                                this.f$2 = r5;
                                this.f$3 = r6;
                                this.f$4 = r7;
                                this.f$5 = r8;
                                this.f$6 = r9;
                                this.f$7 = r10;
                                this.f$8 = r11;
                                this.f$9 = r12;
                                this.f$10 = r13;
                                this.f$11 = r14;
                                this.f$12 = r15;
                                this.f$13 = r16;
                                this.f$14 = r17;
                                this.f$15 = r18;
                                this.f$16 = r19;
                            }

                            public final void run() {
                                SystemServer.this.lambda$startOtherServices$4$SystemServer(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15, this.f$16);
                            }
                        }, BOOT_TIMINGS_TRACE_LOG);
                    }
                } catch (RuntimeException e27) {
                    e15 = e27;
                    inputManager3 = r12;
                    vibrator2 = vibrator;
                    Slog.e("System", "******************************************");
                    Slog.e("System", "************ Failure starting core service", e15);
                    inputManager = inputManager3;
                    vibrator = vibrator2;
                    wm = wm2;
                    safeMode = wm.detectSafeMode();
                    if (safeMode) {
                    }
                    lockSettings = null;
                    if (this.mFactoryTestMode != 1) {
                    }
                    traceBeginAndSlog("MakeDisplayReady");
                    Slog.i(TAG, "displayReady");
                    wm.displayReady();
                    traceEnd();
                    traceBeginAndSlog("StartStorageManagerService");
                    if (!sMtkSystemServerIns.startMtkStorageManagerService()) {
                    }
                    Slog.i(TAG, "mount service");
                    IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
                    traceEnd();
                    traceBeginAndSlog("StartStorageStatsService");
                    this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
                    traceEnd();
                    traceBeginAndSlog("StartUiModeManager");
                    this.mSystemServiceManager.startService(UiModeManagerService.class);
                    traceEnd();
                    if (!this.mOnlyCore) {
                    }
                    traceBeginAndSlog("PerformFstrimIfNeeded");
                    Slog.i(TAG, "performFstrimIfNeeded");
                    this.mPackageManagerService.performFstrimIfNeeded();
                    traceEnd();
                    if (this.mFactoryTestMode == 1) {
                    }
                    if (!isWatch) {
                    }
                    if (isWatch) {
                    }
                    if (!disableSlices) {
                    }
                    if (!disableCameraService) {
                    }
                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                    }
                    traceBeginAndSlog("StartStatsCompanionService");
                    this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                    traceEnd();
                    traceBeginAndSlog("StartIncidentCompanionService");
                    this.mSystemServiceManager.startService(IncidentCompanionService.class);
                    traceEnd();
                    if (safeMode) {
                    }
                    traceBeginAndSlog("StartMmsService");
                    MmsServiceBroker mmsService22222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                    }
                    traceBeginAndSlog("StartClipboardService");
                    this.mSystemServiceManager.startService(ClipboardService.class);
                    traceEnd();
                    traceBeginAndSlog("AppServiceManager");
                    this.mSystemServiceManager.startService(AppBindingService.Lifecycle.class);
                    traceEnd();
                    sMtkSystemServerIns.startMtkOtherServices();
                    this.mHelper.startOtherServices();
                    traceBeginAndSlog("MakeVibratorServiceReady");
                    vibrator.systemReady();
                    traceEnd();
                    traceBeginAndSlog("MakeLockSettingsServiceReady");
                    if (lockSettings != null) {
                    }
                    traceEnd();
                    traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                    traceEnd();
                    traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                    this.mSystemServiceManager.startBootPhase(500);
                    traceEnd();
                    traceBeginAndSlog("MakeWindowManagerServiceReady");
                    Slog.i(TAG, "wms systemReady");
                    wm.systemReady();
                    traceEnd();
                    if (safeMode) {
                    }
                    Configuration config22222222 = wm.computeNewConfiguration(0);
                    DisplayMetrics metrics22222222 = new DisplayMetrics();
                    ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics22222222);
                    context.getResources().updateConfiguration(config22222222, metrics22222222);
                    systemTheme = context.getTheme();
                    if (systemTheme.getChangingConfigurations() != 0) {
                    }
                    traceBeginAndSlog("MakePowerManagerServiceReady");
                    Slog.i(TAG, "power manager systemReady");
                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                    traceEnd();
                    if (this.mFingerprintService != null) {
                    }
                    if (this.mFaceService != null) {
                    }
                    if (this.mBiometricService != null) {
                    }
                    if (this.mAlipayService != null) {
                    }
                    traceBeginAndSlog("StartPermissionPolicyService");
                    this.mSystemServiceManager.startService(PermissionPolicyService.class);
                    traceEnd();
                    if (this.mOppoOperatorManagerService != null) {
                    }
                    traceBeginAndSlog("MakePackageManagerServiceReady");
                    Slog.i(TAG, "Package systemReady");
                    this.mPackageManagerService.systemReady();
                    traceEnd();
                    traceBeginAndSlog("MakeOppoCustomizeServiceReady");
                    if (this.oppoCustomize != null) {
                    }
                    traceEnd();
                    traceBeginAndSlog("MakeDisplayManagerServiceReady");
                    Slog.i(TAG, "DisplayManager systemReady");
                    this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                    traceEnd();
                    this.mSystemServiceManager.setSafeMode(safeMode);
                    traceBeginAndSlog("StartDeviceSpecificServices");
                    String[] classes22222222 = this.mSystemContext.getResources().getStringArray(17236011);
                    while (r6 < r7) {
                    }
                    traceEnd();
                    traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                    traceEnd();
                    this.mHelper.systemReady();
                    Slog.i(TAG, "Ams systemReady");
                    this.mActivityManagerService.systemReady(new Runnable(context, wm, safeMode, iConnectivityManager, networkManagement, networkPolicy, r19, networkStats, location, countryDetector, networkTimeUpdater, inputManager, telephonyRegistry, mediaRouter, oppomodemService, mmsService22222222) {
                        /* class com.android.server.$$Lambda$SystemServer$Veuf5e15txqWe0TdLPFg29kfanQ */
                        private final /* synthetic */ Context f$1;
                        private final /* synthetic */ CountryDetectorService f$10;
                        private final /* synthetic */ NetworkTimeUpdateService f$11;
                        private final /* synthetic */ InputManagerService f$12;
                        private final /* synthetic */ TelephonyRegistry f$13;
                        private final /* synthetic */ MediaRouterService f$14;
                        private final /* synthetic */ OppomodemService f$15;
                        private final /* synthetic */ MmsServiceBroker f$16;
                        private final /* synthetic */ WindowManagerService f$2;
                        private final /* synthetic */ boolean f$3;
                        private final /* synthetic */ ConnectivityService f$4;
                        private final /* synthetic */ NetworkManagementService f$5;
                        private final /* synthetic */ NetworkPolicyManagerService f$6;
                        private final /* synthetic */ IpSecService f$7;
                        private final /* synthetic */ NetworkStatsService f$8;
                        private final /* synthetic */ LocationManagerService f$9;

                        {
                            this.f$1 = r4;
                            this.f$2 = r5;
                            this.f$3 = r6;
                            this.f$4 = r7;
                            this.f$5 = r8;
                            this.f$6 = r9;
                            this.f$7 = r10;
                            this.f$8 = r11;
                            this.f$9 = r12;
                            this.f$10 = r13;
                            this.f$11 = r14;
                            this.f$12 = r15;
                            this.f$13 = r16;
                            this.f$14 = r17;
                            this.f$15 = r18;
                            this.f$16 = r19;
                        }

                        public final void run() {
                            SystemServer.this.lambda$startOtherServices$4$SystemServer(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15, this.f$16);
                        }
                    }, BOOT_TIMINGS_TRACE_LOG);
                }
            } catch (RuntimeException e28) {
                e15 = e28;
                networkManagement = null;
                telephonyRegistry = null;
                vibrator2 = null;
                Slog.e("System", "******************************************");
                Slog.e("System", "************ Failure starting core service", e15);
                inputManager = inputManager3;
                vibrator = vibrator2;
                wm = wm2;
                safeMode = wm.detectSafeMode();
                if (safeMode) {
                }
                lockSettings = null;
                if (this.mFactoryTestMode != 1) {
                }
                traceBeginAndSlog("MakeDisplayReady");
                Slog.i(TAG, "displayReady");
                wm.displayReady();
                traceEnd();
                traceBeginAndSlog("StartStorageManagerService");
                if (!sMtkSystemServerIns.startMtkStorageManagerService()) {
                }
                Slog.i(TAG, "mount service");
                IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
                traceEnd();
                traceBeginAndSlog("StartStorageStatsService");
                this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
                traceEnd();
                traceBeginAndSlog("StartUiModeManager");
                this.mSystemServiceManager.startService(UiModeManagerService.class);
                traceEnd();
                if (!this.mOnlyCore) {
                }
                traceBeginAndSlog("PerformFstrimIfNeeded");
                Slog.i(TAG, "performFstrimIfNeeded");
                this.mPackageManagerService.performFstrimIfNeeded();
                traceEnd();
                if (this.mFactoryTestMode == 1) {
                }
                if (!isWatch) {
                }
                if (isWatch) {
                }
                if (!disableSlices) {
                }
                if (!disableCameraService) {
                }
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                }
                traceBeginAndSlog("StartStatsCompanionService");
                this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                traceEnd();
                traceBeginAndSlog("StartIncidentCompanionService");
                this.mSystemServiceManager.startService(IncidentCompanionService.class);
                traceEnd();
                if (safeMode) {
                }
                traceBeginAndSlog("StartMmsService");
                MmsServiceBroker mmsService222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                }
                traceBeginAndSlog("StartClipboardService");
                this.mSystemServiceManager.startService(ClipboardService.class);
                traceEnd();
                traceBeginAndSlog("AppServiceManager");
                this.mSystemServiceManager.startService(AppBindingService.Lifecycle.class);
                traceEnd();
                sMtkSystemServerIns.startMtkOtherServices();
                this.mHelper.startOtherServices();
                traceBeginAndSlog("MakeVibratorServiceReady");
                vibrator.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeLockSettingsServiceReady");
                if (lockSettings != null) {
                }
                traceEnd();
                traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                traceEnd();
                traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                this.mSystemServiceManager.startBootPhase(500);
                traceEnd();
                traceBeginAndSlog("MakeWindowManagerServiceReady");
                Slog.i(TAG, "wms systemReady");
                wm.systemReady();
                traceEnd();
                if (safeMode) {
                }
                Configuration config222222222 = wm.computeNewConfiguration(0);
                DisplayMetrics metrics222222222 = new DisplayMetrics();
                ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics222222222);
                context.getResources().updateConfiguration(config222222222, metrics222222222);
                systemTheme = context.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                }
                traceBeginAndSlog("MakePowerManagerServiceReady");
                Slog.i(TAG, "power manager systemReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                traceEnd();
                if (this.mFingerprintService != null) {
                }
                if (this.mFaceService != null) {
                }
                if (this.mBiometricService != null) {
                }
                if (this.mAlipayService != null) {
                }
                traceBeginAndSlog("StartPermissionPolicyService");
                this.mSystemServiceManager.startService(PermissionPolicyService.class);
                traceEnd();
                if (this.mOppoOperatorManagerService != null) {
                }
                traceBeginAndSlog("MakePackageManagerServiceReady");
                Slog.i(TAG, "Package systemReady");
                this.mPackageManagerService.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeOppoCustomizeServiceReady");
                if (this.oppoCustomize != null) {
                }
                traceEnd();
                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                Slog.i(TAG, "DisplayManager systemReady");
                this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                traceEnd();
                this.mSystemServiceManager.setSafeMode(safeMode);
                traceBeginAndSlog("StartDeviceSpecificServices");
                String[] classes222222222 = this.mSystemContext.getResources().getStringArray(17236011);
                while (r6 < r7) {
                }
                traceEnd();
                traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                traceEnd();
                this.mHelper.systemReady();
                Slog.i(TAG, "Ams systemReady");
                this.mActivityManagerService.systemReady(new Runnable(context, wm, safeMode, iConnectivityManager, networkManagement, networkPolicy, r19, networkStats, location, countryDetector, networkTimeUpdater, inputManager, telephonyRegistry, mediaRouter, oppomodemService, mmsService222222222) {
                    /* class com.android.server.$$Lambda$SystemServer$Veuf5e15txqWe0TdLPFg29kfanQ */
                    private final /* synthetic */ Context f$1;
                    private final /* synthetic */ CountryDetectorService f$10;
                    private final /* synthetic */ NetworkTimeUpdateService f$11;
                    private final /* synthetic */ InputManagerService f$12;
                    private final /* synthetic */ TelephonyRegistry f$13;
                    private final /* synthetic */ MediaRouterService f$14;
                    private final /* synthetic */ OppomodemService f$15;
                    private final /* synthetic */ MmsServiceBroker f$16;
                    private final /* synthetic */ WindowManagerService f$2;
                    private final /* synthetic */ boolean f$3;
                    private final /* synthetic */ ConnectivityService f$4;
                    private final /* synthetic */ NetworkManagementService f$5;
                    private final /* synthetic */ NetworkPolicyManagerService f$6;
                    private final /* synthetic */ IpSecService f$7;
                    private final /* synthetic */ NetworkStatsService f$8;
                    private final /* synthetic */ LocationManagerService f$9;

                    {
                        this.f$1 = r4;
                        this.f$2 = r5;
                        this.f$3 = r6;
                        this.f$4 = r7;
                        this.f$5 = r8;
                        this.f$6 = r9;
                        this.f$7 = r10;
                        this.f$8 = r11;
                        this.f$9 = r12;
                        this.f$10 = r13;
                        this.f$11 = r14;
                        this.f$12 = r15;
                        this.f$13 = r16;
                        this.f$14 = r17;
                        this.f$15 = r18;
                        this.f$16 = r19;
                    }

                    public final void run() {
                        SystemServer.this.lambda$startOtherServices$4$SystemServer(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15, this.f$16);
                    }
                }, BOOT_TIMINGS_TRACE_LOG);
            }
        } catch (RuntimeException e29) {
            e15 = e29;
            networkManagement = null;
            telephonyRegistry = null;
            vibrator2 = null;
            Slog.e("System", "******************************************");
            Slog.e("System", "************ Failure starting core service", e15);
            inputManager = inputManager3;
            vibrator = vibrator2;
            wm = wm2;
            safeMode = wm.detectSafeMode();
            if (safeMode) {
            }
            lockSettings = null;
            if (this.mFactoryTestMode != 1) {
            }
            traceBeginAndSlog("MakeDisplayReady");
            Slog.i(TAG, "displayReady");
            wm.displayReady();
            traceEnd();
            traceBeginAndSlog("StartStorageManagerService");
            if (!sMtkSystemServerIns.startMtkStorageManagerService()) {
            }
            Slog.i(TAG, "mount service");
            IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
            traceEnd();
            traceBeginAndSlog("StartStorageStatsService");
            this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
            traceEnd();
            traceBeginAndSlog("StartUiModeManager");
            this.mSystemServiceManager.startService(UiModeManagerService.class);
            traceEnd();
            if (!this.mOnlyCore) {
            }
            traceBeginAndSlog("PerformFstrimIfNeeded");
            Slog.i(TAG, "performFstrimIfNeeded");
            this.mPackageManagerService.performFstrimIfNeeded();
            traceEnd();
            if (this.mFactoryTestMode == 1) {
            }
            if (!isWatch) {
            }
            if (isWatch) {
            }
            if (!disableSlices) {
            }
            if (!disableCameraService) {
            }
            if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
            }
            traceBeginAndSlog("StartStatsCompanionService");
            this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
            traceEnd();
            traceBeginAndSlog("StartIncidentCompanionService");
            this.mSystemServiceManager.startService(IncidentCompanionService.class);
            traceEnd();
            if (safeMode) {
            }
            traceBeginAndSlog("StartMmsService");
            MmsServiceBroker mmsService2222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
            traceEnd();
            if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
            }
            traceBeginAndSlog("StartClipboardService");
            this.mSystemServiceManager.startService(ClipboardService.class);
            traceEnd();
            traceBeginAndSlog("AppServiceManager");
            this.mSystemServiceManager.startService(AppBindingService.Lifecycle.class);
            traceEnd();
            sMtkSystemServerIns.startMtkOtherServices();
            this.mHelper.startOtherServices();
            traceBeginAndSlog("MakeVibratorServiceReady");
            vibrator.systemReady();
            traceEnd();
            traceBeginAndSlog("MakeLockSettingsServiceReady");
            if (lockSettings != null) {
            }
            traceEnd();
            traceBeginAndSlog("StartBootPhaseLockSettingsReady");
            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
            traceEnd();
            traceBeginAndSlog("StartBootPhaseSystemServicesReady");
            this.mSystemServiceManager.startBootPhase(500);
            traceEnd();
            traceBeginAndSlog("MakeWindowManagerServiceReady");
            Slog.i(TAG, "wms systemReady");
            wm.systemReady();
            traceEnd();
            if (safeMode) {
            }
            Configuration config2222222222 = wm.computeNewConfiguration(0);
            DisplayMetrics metrics2222222222 = new DisplayMetrics();
            ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics2222222222);
            context.getResources().updateConfiguration(config2222222222, metrics2222222222);
            systemTheme = context.getTheme();
            if (systemTheme.getChangingConfigurations() != 0) {
            }
            traceBeginAndSlog("MakePowerManagerServiceReady");
            Slog.i(TAG, "power manager systemReady");
            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
            traceEnd();
            if (this.mFingerprintService != null) {
            }
            if (this.mFaceService != null) {
            }
            if (this.mBiometricService != null) {
            }
            if (this.mAlipayService != null) {
            }
            traceBeginAndSlog("StartPermissionPolicyService");
            this.mSystemServiceManager.startService(PermissionPolicyService.class);
            traceEnd();
            if (this.mOppoOperatorManagerService != null) {
            }
            traceBeginAndSlog("MakePackageManagerServiceReady");
            Slog.i(TAG, "Package systemReady");
            this.mPackageManagerService.systemReady();
            traceEnd();
            traceBeginAndSlog("MakeOppoCustomizeServiceReady");
            if (this.oppoCustomize != null) {
            }
            traceEnd();
            traceBeginAndSlog("MakeDisplayManagerServiceReady");
            Slog.i(TAG, "DisplayManager systemReady");
            this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
            traceEnd();
            this.mSystemServiceManager.setSafeMode(safeMode);
            traceBeginAndSlog("StartDeviceSpecificServices");
            String[] classes2222222222 = this.mSystemContext.getResources().getStringArray(17236011);
            while (r6 < r7) {
            }
            traceEnd();
            traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
            traceEnd();
            this.mHelper.systemReady();
            Slog.i(TAG, "Ams systemReady");
            this.mActivityManagerService.systemReady(new Runnable(context, wm, safeMode, iConnectivityManager, networkManagement, networkPolicy, r19, networkStats, location, countryDetector, networkTimeUpdater, inputManager, telephonyRegistry, mediaRouter, oppomodemService, mmsService2222222222) {
                /* class com.android.server.$$Lambda$SystemServer$Veuf5e15txqWe0TdLPFg29kfanQ */
                private final /* synthetic */ Context f$1;
                private final /* synthetic */ CountryDetectorService f$10;
                private final /* synthetic */ NetworkTimeUpdateService f$11;
                private final /* synthetic */ InputManagerService f$12;
                private final /* synthetic */ TelephonyRegistry f$13;
                private final /* synthetic */ MediaRouterService f$14;
                private final /* synthetic */ OppomodemService f$15;
                private final /* synthetic */ MmsServiceBroker f$16;
                private final /* synthetic */ WindowManagerService f$2;
                private final /* synthetic */ boolean f$3;
                private final /* synthetic */ ConnectivityService f$4;
                private final /* synthetic */ NetworkManagementService f$5;
                private final /* synthetic */ NetworkPolicyManagerService f$6;
                private final /* synthetic */ IpSecService f$7;
                private final /* synthetic */ NetworkStatsService f$8;
                private final /* synthetic */ LocationManagerService f$9;

                {
                    this.f$1 = r4;
                    this.f$2 = r5;
                    this.f$3 = r6;
                    this.f$4 = r7;
                    this.f$5 = r8;
                    this.f$6 = r9;
                    this.f$7 = r10;
                    this.f$8 = r11;
                    this.f$9 = r12;
                    this.f$10 = r13;
                    this.f$11 = r14;
                    this.f$12 = r15;
                    this.f$13 = r16;
                    this.f$14 = r17;
                    this.f$15 = r18;
                    this.f$16 = r19;
                }

                public final void run() {
                    SystemServer.this.lambda$startOtherServices$4$SystemServer(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15, this.f$16);
                }
            }, BOOT_TIMINGS_TRACE_LOG);
        }
        safeMode = wm.detectSafeMode();
        if (safeMode) {
            Settings.Global.putInt(context.getContentResolver(), "airplane_mode_on", 1);
        }
        lockSettings = null;
        if (this.mFactoryTestMode != 1) {
            traceBeginAndSlog("StartInputMethodManagerLifecycle");
            if (InputMethodSystemProperty.MULTI_CLIENT_IME_ENABLED) {
                this.mSystemServiceManager.startService(MultiClientInputMethodManagerService.Lifecycle.class);
            } else {
                this.mSystemServiceManager.startService(InputMethodManagerService.Lifecycle.class);
            }
            traceEnd();
            traceBeginAndSlog("StartAccessibilityManagerService");
            try {
                this.mSystemServiceManager.startService(ACCESSIBILITY_MANAGER_SERVICE_CLASS);
            } catch (Throwable e30) {
                reportWtf("starting Accessibility Manager", e30);
            }
            traceEnd();
        }
        traceBeginAndSlog("MakeDisplayReady");
        try {
            Slog.i(TAG, "displayReady");
            wm.displayReady();
        } catch (Throwable e31) {
            reportWtf("making display ready", e31);
        }
        traceEnd();
        if (this.mFactoryTestMode != 1 && !"0".equals(SystemProperties.get("system_init.startmountservice"))) {
            traceBeginAndSlog("StartStorageManagerService");
            if (!sMtkSystemServerIns.startMtkStorageManagerService()) {
                Slog.i(TAG, "startMtkStorageManagerService seccuss");
                this.mSystemServiceManager.startService(STORAGE_MANAGER_SERVICE_CLASS);
            }
            Slog.i(TAG, "mount service");
            IStorageManager.Stub.asInterface(ServiceManager.getService("mount"));
            traceEnd();
            traceBeginAndSlog("StartStorageStatsService");
            this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
            traceEnd();
        }
        traceBeginAndSlog("StartUiModeManager");
        this.mSystemServiceManager.startService(UiModeManagerService.class);
        traceEnd();
        if (!this.mOnlyCore) {
            traceBeginAndSlog("UpdatePackagesIfNeeded");
            try {
                Watchdog.getInstance().pauseWatchingCurrentThread("dexopt");
                this.mPackageManagerService.updatePackagesIfNeeded();
            } catch (Throwable th) {
                Watchdog.getInstance().resumeWatchingCurrentThread("dexopt");
                throw th;
            }
            Watchdog.getInstance().resumeWatchingCurrentThread("dexopt");
            traceEnd();
        }
        traceBeginAndSlog("PerformFstrimIfNeeded");
        try {
            Slog.i(TAG, "performFstrimIfNeeded");
            this.mPackageManagerService.performFstrimIfNeeded();
        } catch (Throwable e32) {
            reportWtf("performing fstrim", e32);
        }
        traceEnd();
        if (this.mFactoryTestMode == 1) {
            traceBeginAndSlog("StartLockSettingsService");
            try {
                this.mSystemServiceManager.startService(LOCK_SETTINGS_SERVICE_CLASS);
                lockSettings = ILockSettings.Stub.asInterface(ServiceManager.getService("lock_settings"));
            } catch (Throwable e33) {
                reportWtf("starting LockSettingsService service", e33);
            }
            traceEnd();
            boolean hasPdb = !SystemProperties.get(PERSISTENT_DATA_BLOCK_PROP).equals("");
            boolean hasGsi = SystemProperties.getInt(GSI_RUNNING_PROP, 0) > 0;
            if (hasPdb && !hasGsi) {
                traceBeginAndSlog("StartPersistentDataBlock");
                this.mSystemServiceManager.startService(PersistentDataBlockService.class);
                traceEnd();
            }
            traceBeginAndSlog("StartTestHarnessMode");
            this.mSystemServiceManager.startService(TestHarnessModeService.class);
            traceEnd();
            if (hasPdb || OemLockService.isHalPresent()) {
                traceBeginAndSlog("StartOemLockService");
                this.mSystemServiceManager.startService(OemLockService.class);
                traceEnd();
            }
            traceBeginAndSlog("StartDeviceIdleController");
            this.mSystemServiceManager.startService(DeviceIdleController.class);
            traceEnd();
            traceBeginAndSlog("StartDevicePolicyManager");
            this.mSystemServiceManager.startService(DevicePolicyManagerService.Lifecycle.class);
            traceEnd();
            if (!isWatch) {
                traceBeginAndSlog("StartStatusBarManagerService");
                try {
                    if (!this.mHelper.startColorStatusBarService(wm)) {
                        iBinder = new ColorStatusBarManagerService(context, wm);
                        try {
                            ServiceManager.addService("statusbar", iBinder);
                        } catch (Throwable th2) {
                            e14 = th2;
                        }
                    } else {
                        iBinder = null;
                    }
                } catch (Throwable th3) {
                    e14 = th3;
                    iBinder = null;
                    reportWtf("starting StatusBarManagerService", e14);
                    iBinder = iBinder;
                    traceEnd();
                    startContentCaptureService(context);
                    startAttentionService(context);
                    startSystemCaptionsManagerService(context);
                    traceBeginAndSlog("StartAppPredictionService");
                    this.mSystemServiceManager.startService(APP_PREDICTION_MANAGER_SERVICE_CLASS);
                    traceEnd();
                    traceBeginAndSlog("StartContentSuggestionsService");
                    this.mSystemServiceManager.startService(CONTENT_SUGGESTIONS_SERVICE_CLASS);
                    traceEnd();
                    traceBeginAndSlog("InitNetworkStackClient");
                    NetworkStackClient.getInstance().init();
                    traceEnd();
                    traceBeginAndSlog("StartNetworkManagementService");
                    create = NetworkManagementService.create(context);
                    try {
                        ServiceManager.addService("network_management", (IBinder) create);
                        networkManagement2 = create;
                    } catch (Throwable th4) {
                        e13 = th4;
                        networkManagement3 = create;
                    }
                    traceEnd();
                    traceBeginAndSlog("StartIpSecService");
                    iBinder9 = IpSecService.create(context);
                    ServiceManager.addService(INetd.IPSEC_INTERFACE_PREFIX, iBinder9);
                    traceEnd();
                    traceBeginAndSlog("StartTextServicesManager");
                    this.mSystemServiceManager.startService(TextServicesManagerService.Lifecycle.class);
                    traceEnd();
                    if (!disableSystemTextClassifier) {
                    }
                    traceBeginAndSlog("StartNetworkScoreService");
                    this.mSystemServiceManager.startService(NetworkScoreService.Lifecycle.class);
                    traceEnd();
                    traceBeginAndSlog("StartNetworkStatsService");
                    iBinder10 = NetworkStatsService.create(context, networkManagement2);
                    ServiceManager.addService("netstats", iBinder10);
                    r15 = iBinder10;
                    traceEnd();
                    traceBeginAndSlog("StartNetworkPolicyManagerService");
                    networkPolicyManagerService = new NetworkPolicyManagerService(context, this.mActivityManagerService, networkManagement2);
                    try {
                        ServiceManager.addService("netpolicy", (IBinder) networkPolicyManagerService);
                        networkPolicy2 = networkPolicyManagerService;
                    } catch (Throwable th5) {
                        e12 = th5;
                        networkPolicy3 = networkPolicyManagerService;
                    }
                    traceEnd();
                    iBinder2 = null;
                    if (context.getPackageManager().hasSystemFeature("android.hardware.wifi")) {
                    }
                    if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.rtt")) {
                    }
                    if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.aware")) {
                    }
                    if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.direct")) {
                    }
                    if (context.getPackageManager().hasSystemFeature("android.hardware.lowpan")) {
                    }
                    if (!this.mPackageManager.hasSystemFeature("android.hardware.ethernet")) {
                    }
                    traceBeginAndSlog("StartEthernet");
                    oppomodemService2 = null;
                    this.mSystemServiceManager.startService(ETHERNET_SERVICE_CLASS);
                    traceEnd();
                    traceBeginAndSlog("StartConnectivityService");
                    iConnectivityManager2 = (ConnectivityService) sMtkSystemServerIns.getMtkConnectivityService(networkManagement2, r15, networkPolicy2);
                    iConnectivityManager5 = iConnectivityManager2;
                    if (iConnectivityManager2 == null) {
                    }
                    networkManagement = networkManagement2;
                    ServiceManager.addService("connectivity", iConnectivityManager5, false, 6);
                    r15.bindConnectivityManager(iConnectivityManager5);
                    networkPolicy2.bindConnectivityManager(iConnectivityManager5);
                    iConnectivityManager3 = iConnectivityManager5;
                    traceEnd();
                    traceBeginAndSlog("StartNsdService");
                    iBinder8 = NsdService.create(context);
                    try {
                        ServiceManager.addService("servicediscovery", iBinder8);
                        iBinder3 = iBinder8;
                    } catch (Throwable th6) {
                        e10 = th6;
                        reportWtf("starting Service Discovery Service", e10);
                        iBinder3 = iBinder8;
                        traceEnd();
                        traceBeginAndSlog("StartSystemUpdateManagerService");
                        ServiceManager.addService("system_update", (IBinder) new SystemUpdateManagerService(context));
                        traceEnd();
                        traceBeginAndSlog("StartUpdateLockService");
                        ServiceManager.addService("updatelock", (IBinder) new UpdateLockService(context));
                        traceEnd();
                        if (this.mOppoLightsService != null) {
                        }
                        traceBeginAndSlog("StartNotificationManager");
                        if (!this.mHelper.startColorNotificationManagerService()) {
                        }
                        SystemNotificationChannels.removeDeprecated(context);
                        SystemNotificationChannels.createAll(context);
                        notification = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
                        traceEnd();
                        traceBeginAndSlog("StartDeviceMonitor");
                        if (!this.mHelper.startColorDeviceStorageMonitorService()) {
                        }
                        traceEnd();
                        traceBeginAndSlog("StartLocationManagerService");
                        locationManagerService = new LocationManagerService(context);
                        ServiceManager.addService("location", (IBinder) locationManagerService);
                        notification2 = notification;
                        location2 = locationManagerService;
                        traceEnd();
                        traceBeginAndSlog("StartCountryDetectorService");
                        countryDetectorService = new CountryDetectorService(context);
                        ServiceManager.addService("country_detector", (IBinder) countryDetectorService);
                        countryDetector2 = countryDetectorService;
                        traceEnd();
                        traceBeginAndSlog("StartTimeDetectorService");
                        this.mSystemServiceManager.startService(TIME_DETECTOR_SERVICE_CLASS);
                        traceEnd();
                        if (!isWatch) {
                        }
                        AgingCriticalEvent instance = AgingCriticalEvent.getInstance();
                        networkPolicy = networkPolicy2;
                        StringBuilder sb = new StringBuilder();
                        iConnectivityManager = iConnectivityManager3;
                        sb.append("systemserver pid:");
                        sb.append(Process.myPid());
                        instance.writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, sb.toString());
                        if (context.getResources().getBoolean(17891451)) {
                        }
                        traceBeginAndSlog("StartAudioService");
                        if (isArc) {
                        }
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.hardware.broadcastradio")) {
                        }
                        traceBeginAndSlog("StartDockObserver");
                        Slog.i(TAG, "DockObserver");
                        this.mSystemServiceManager.startService(DockObserver.class);
                        traceEnd();
                        if (isWatch) {
                        }
                        traceBeginAndSlog("StartWiredAccessoryManager");
                        inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.midi")) {
                        }
                        traceBeginAndSlog("StartAdbService");
                        this.mSystemServiceManager.startService(ADB_SERVICE_CLASS);
                        traceEnd();
                        traceBeginAndSlog("StartUsbService");
                        this.mSystemServiceManager.startService(USB_SERVICE_CLASS);
                        traceEnd();
                        if (!isWatch) {
                        }
                        traceBeginAndSlog("StartHardwarePropertiesManagerService");
                        iBinder6 = new HardwarePropertiesManagerService(context);
                        ServiceManager.addService("hardware_properties", iBinder6);
                        iBinder4 = iBinder6;
                        traceEnd();
                        traceBeginAndSlog("StartTwilightService");
                        this.mSystemServiceManager.startService(TwilightService.class);
                        traceEnd();
                        traceBeginAndSlog("StartColorDisplay");
                        this.mSystemServiceManager.startService(ColorDisplayService.class);
                        traceEnd();
                        traceBeginAndSlog("StartJobScheduler");
                        if (!this.mHelper.startColorJobSchedulerService()) {
                        }
                        traceEnd();
                        traceBeginAndSlog("StartSoundTrigger");
                        this.mSystemServiceManager.startService(SoundTriggerService.class);
                        traceEnd();
                        traceBeginAndSlog("StartTrustManager");
                        this.mSystemServiceManager.startService(TrustManagerService.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                        }
                        traceBeginAndSlog("StartAppWidgetService");
                        this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                        traceEnd();
                        traceBeginAndSlog("StartRoleManagerService");
                        SystemServiceManager systemServiceManager = this.mSystemServiceManager;
                        Context context2 = this.mSystemContext;
                        systemServiceManager.startService(new RoleManagerService(context2, new LegacyRoleResolutionPolicy(context2)));
                        traceEnd();
                        traceBeginAndSlog("StartVoiceRecognitionManager");
                        this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                        traceEnd();
                        if (GestureLauncherService.isGestureLauncherEnabled(context.getResources())) {
                        }
                        traceBeginAndSlog("StartSensorNotification");
                        this.mSystemServiceManager.startService(SensorNotificationService.class);
                        traceEnd();
                        traceBeginAndSlog("StartContextHubSystemService");
                        this.mSystemServiceManager.startService(ContextHubSystemService.class);
                        traceEnd();
                        traceBeginAndSlog("StartDiskStatsService");
                        ServiceManager.addService("diskstats", new DiskStatsService(context));
                        traceEnd();
                        Slog.i(TAG, "Oppo modem nimidump Service");
                        oppomodemService5 = new OppomodemService(context);
                        ServiceManager.addService("modem_crash_up", (IBinder) oppomodemService5);
                        oppomodemService3 = oppomodemService5;
                        traceBeginAndSlog("CabcService");
                        Slog.i(TAG, "Cabc Service");
                        iBinder5 = new CabcService(context);
                        ServiceManager.addService("cabc", iBinder5);
                        Trace.traceEnd(524288);
                        traceBeginAndSlog("RuntimeService");
                        ServiceManager.addService("runtime", new RuntimeService(context));
                        traceEnd();
                        Slog.i(TAG, "Torch Service");
                        TorchManagerService.getInstance(context).systemReady();
                        traceBeginAndSlog("StartHypnusService");
                        Slog.i(TAG, "Hypnus Service");
                        this.hypnusService = new HypnusService(context);
                        ServiceManager.addService("hypnus", (IBinder) this.hypnusService);
                        traceEnd();
                        Slog.i(TAG, "Oppo Customize Service");
                        this.oppoCustomize = new OppoCustomizeService(context);
                        ServiceManager.addService("oppocustomize", (IBinder) this.oppoCustomize);
                        if (this.mOnlyCore && context.getResources().getBoolean(17891450)) {
                        }
                        if (isWatch) {
                        }
                        traceBeginAndSlog("CertBlacklister");
                        new CertBlacklister(context);
                        traceEnd();
                        traceBeginAndSlog("StartEmergencyAffordanceService");
                        this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                        traceEnd();
                        traceBeginAndSlog("StartDreamManager");
                        this.mSystemServiceManager.startService(DreamManagerService.class);
                        traceEnd();
                        traceBeginAndSlog("AddGraphicsStatsService");
                        ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, (IBinder) new GraphicsStatsService(context));
                        traceEnd();
                        if (CoverageService.ENABLED) {
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.companion_device_setup")) {
                        }
                        traceBeginAndSlog("StartRestrictionManager");
                        this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                        traceEnd();
                        traceBeginAndSlog("StartMediaSessionService");
                        Slog.i(TAG, "Media Session Service");
                        this.mSystemServiceManager.startService(MediaSessionService.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                        }
                        traceBeginAndSlog("StartTvInputManager");
                        this.mSystemServiceManager.startService(TvInputManagerService.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                        }
                        traceBeginAndSlog("StartMediaRouterService");
                        mediaRouterService = new MediaRouterService(context);
                        ServiceManager.addService("media_router", (IBinder) mediaRouterService);
                        mediaRouter2 = mediaRouterService;
                        traceEnd();
                        hasFeatureFace = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.face");
                        hasFeatureIris = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.iris");
                        hasFeatureFingerprint = this.mPackageManager.hasSystemFeature("android.hardware.fingerprint");
                        if (hasFeatureFace) {
                        }
                        if (hasFeatureIris) {
                        }
                        if (hasFeatureFingerprint) {
                        }
                        traceBeginAndSlog("StartBiometricService");
                        Slog.i(TAG, "BiometricService Service");
                        this.mBiometricService = (BiometricService) this.mSystemServiceManager.startService(BiometricService.class);
                        this.mAlipayService = (AlipayService) this.mSystemServiceManager.startService(AlipayService.class);
                        traceEnd();
                        traceBeginAndSlog("StartBackgroundDexOptService");
                        BackgroundDexOptService.schedule(context);
                        traceEnd();
                        if (OppoOperatorManager.SERVICE_ENABLED) {
                        }
                        if (!isWatch) {
                        }
                        Slog.i(TAG, "Oppo Engineer Service");
                        this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                        if (!isWatch) {
                        }
                        traceBeginAndSlog("StartShortcutServiceLifecycle");
                        this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                        traceEnd();
                        traceBeginAndSlog("StartLauncherAppsService");
                        this.mSystemServiceManager.startService(LauncherAppsService.class);
                        traceEnd();
                        traceBeginAndSlog("StartCrossProfileAppsService");
                        this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                        traceEnd();
                        location = location2;
                        r19 = iBinder9;
                        countryDetector = countryDetector2;
                        networkTimeUpdater = networkTimeUpdater3;
                        mediaRouter = mediaRouter2;
                        networkStats = r15;
                        if (!isWatch) {
                        }
                        if (isWatch) {
                        }
                        if (!disableSlices) {
                        }
                        if (!disableCameraService) {
                        }
                        if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                        }
                        traceBeginAndSlog("StartStatsCompanionService");
                        this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                        traceEnd();
                        traceBeginAndSlog("StartIncidentCompanionService");
                        this.mSystemServiceManager.startService(IncidentCompanionService.class);
                        traceEnd();
                        if (safeMode) {
                        }
                        traceBeginAndSlog("StartMmsService");
                        MmsServiceBroker mmsService22222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                        }
                        traceBeginAndSlog("StartClipboardService");
                        this.mSystemServiceManager.startService(ClipboardService.class);
                        traceEnd();
                        traceBeginAndSlog("AppServiceManager");
                        this.mSystemServiceManager.startService(AppBindingService.Lifecycle.class);
                        traceEnd();
                        sMtkSystemServerIns.startMtkOtherServices();
                        this.mHelper.startOtherServices();
                        traceBeginAndSlog("MakeVibratorServiceReady");
                        vibrator.systemReady();
                        traceEnd();
                        traceBeginAndSlog("MakeLockSettingsServiceReady");
                        if (lockSettings != null) {
                        }
                        traceEnd();
                        traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                        traceEnd();
                        traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                        this.mSystemServiceManager.startBootPhase(500);
                        traceEnd();
                        traceBeginAndSlog("MakeWindowManagerServiceReady");
                        Slog.i(TAG, "wms systemReady");
                        wm.systemReady();
                        traceEnd();
                        if (safeMode) {
                        }
                        Configuration config22222222222 = wm.computeNewConfiguration(0);
                        DisplayMetrics metrics22222222222 = new DisplayMetrics();
                        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics22222222222);
                        context.getResources().updateConfiguration(config22222222222, metrics22222222222);
                        systemTheme = context.getTheme();
                        if (systemTheme.getChangingConfigurations() != 0) {
                        }
                        traceBeginAndSlog("MakePowerManagerServiceReady");
                        Slog.i(TAG, "power manager systemReady");
                        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                        traceEnd();
                        if (this.mFingerprintService != null) {
                        }
                        if (this.mFaceService != null) {
                        }
                        if (this.mBiometricService != null) {
                        }
                        if (this.mAlipayService != null) {
                        }
                        traceBeginAndSlog("StartPermissionPolicyService");
                        this.mSystemServiceManager.startService(PermissionPolicyService.class);
                        traceEnd();
                        if (this.mOppoOperatorManagerService != null) {
                        }
                        traceBeginAndSlog("MakePackageManagerServiceReady");
                        Slog.i(TAG, "Package systemReady");
                        this.mPackageManagerService.systemReady();
                        traceEnd();
                        traceBeginAndSlog("MakeOppoCustomizeServiceReady");
                        if (this.oppoCustomize != null) {
                        }
                        traceEnd();
                        traceBeginAndSlog("MakeDisplayManagerServiceReady");
                        Slog.i(TAG, "DisplayManager systemReady");
                        this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                        traceEnd();
                        this.mSystemServiceManager.setSafeMode(safeMode);
                        traceBeginAndSlog("StartDeviceSpecificServices");
                        String[] classes22222222222 = this.mSystemContext.getResources().getStringArray(17236011);
                        while (r6 < r7) {
                        }
                        traceEnd();
                        traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                        traceEnd();
                        this.mHelper.systemReady();
                        Slog.i(TAG, "Ams systemReady");
                        this.mActivityManagerService.systemReady(new Runnable(context, wm, safeMode, iConnectivityManager, networkManagement, networkPolicy, r19, networkStats, location, countryDetector, networkTimeUpdater, inputManager, telephonyRegistry, mediaRouter, oppomodemService, mmsService22222222222) {
                            /* class com.android.server.$$Lambda$SystemServer$Veuf5e15txqWe0TdLPFg29kfanQ */
                            private final /* synthetic */ Context f$1;
                            private final /* synthetic */ CountryDetectorService f$10;
                            private final /* synthetic */ NetworkTimeUpdateService f$11;
                            private final /* synthetic */ InputManagerService f$12;
                            private final /* synthetic */ TelephonyRegistry f$13;
                            private final /* synthetic */ MediaRouterService f$14;
                            private final /* synthetic */ OppomodemService f$15;
                            private final /* synthetic */ MmsServiceBroker f$16;
                            private final /* synthetic */ WindowManagerService f$2;
                            private final /* synthetic */ boolean f$3;
                            private final /* synthetic */ ConnectivityService f$4;
                            private final /* synthetic */ NetworkManagementService f$5;
                            private final /* synthetic */ NetworkPolicyManagerService f$6;
                            private final /* synthetic */ IpSecService f$7;
                            private final /* synthetic */ NetworkStatsService f$8;
                            private final /* synthetic */ LocationManagerService f$9;

                            {
                                this.f$1 = r4;
                                this.f$2 = r5;
                                this.f$3 = r6;
                                this.f$4 = r7;
                                this.f$5 = r8;
                                this.f$6 = r9;
                                this.f$7 = r10;
                                this.f$8 = r11;
                                this.f$9 = r12;
                                this.f$10 = r13;
                                this.f$11 = r14;
                                this.f$12 = r15;
                                this.f$13 = r16;
                                this.f$14 = r17;
                                this.f$15 = r18;
                                this.f$16 = r19;
                            }

                            public final void run() {
                                SystemServer.this.lambda$startOtherServices$4$SystemServer(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15, this.f$16);
                            }
                        }, BOOT_TIMINGS_TRACE_LOG);
                    }
                    traceEnd();
                    traceBeginAndSlog("StartSystemUpdateManagerService");
                    ServiceManager.addService("system_update", (IBinder) new SystemUpdateManagerService(context));
                    traceEnd();
                    traceBeginAndSlog("StartUpdateLockService");
                    ServiceManager.addService("updatelock", (IBinder) new UpdateLockService(context));
                    traceEnd();
                    if (this.mOppoLightsService != null) {
                    }
                    traceBeginAndSlog("StartNotificationManager");
                    if (!this.mHelper.startColorNotificationManagerService()) {
                    }
                    SystemNotificationChannels.removeDeprecated(context);
                    SystemNotificationChannels.createAll(context);
                    notification = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
                    traceEnd();
                    traceBeginAndSlog("StartDeviceMonitor");
                    if (!this.mHelper.startColorDeviceStorageMonitorService()) {
                    }
                    traceEnd();
                    traceBeginAndSlog("StartLocationManagerService");
                    locationManagerService = new LocationManagerService(context);
                    try {
                        ServiceManager.addService("location", (IBinder) locationManagerService);
                        notification2 = notification;
                        location2 = locationManagerService;
                    } catch (Throwable th7) {
                        e9 = th7;
                        location3 = locationManagerService;
                        notification2 = notification;
                        reportWtf("starting Location Manager", e9);
                        location2 = location3;
                        traceEnd();
                        traceBeginAndSlog("StartCountryDetectorService");
                        countryDetectorService = new CountryDetectorService(context);
                        ServiceManager.addService("country_detector", (IBinder) countryDetectorService);
                        countryDetector2 = countryDetectorService;
                        traceEnd();
                        traceBeginAndSlog("StartTimeDetectorService");
                        this.mSystemServiceManager.startService(TIME_DETECTOR_SERVICE_CLASS);
                        traceEnd();
                        if (!isWatch) {
                        }
                        AgingCriticalEvent instance2 = AgingCriticalEvent.getInstance();
                        networkPolicy = networkPolicy2;
                        StringBuilder sb2 = new StringBuilder();
                        iConnectivityManager = iConnectivityManager3;
                        sb2.append("systemserver pid:");
                        sb2.append(Process.myPid());
                        instance2.writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, sb2.toString());
                        if (context.getResources().getBoolean(17891451)) {
                        }
                        traceBeginAndSlog("StartAudioService");
                        if (isArc) {
                        }
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.hardware.broadcastradio")) {
                        }
                        traceBeginAndSlog("StartDockObserver");
                        Slog.i(TAG, "DockObserver");
                        this.mSystemServiceManager.startService(DockObserver.class);
                        traceEnd();
                        if (isWatch) {
                        }
                        traceBeginAndSlog("StartWiredAccessoryManager");
                        inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.midi")) {
                        }
                        traceBeginAndSlog("StartAdbService");
                        this.mSystemServiceManager.startService(ADB_SERVICE_CLASS);
                        traceEnd();
                        traceBeginAndSlog("StartUsbService");
                        this.mSystemServiceManager.startService(USB_SERVICE_CLASS);
                        traceEnd();
                        if (!isWatch) {
                        }
                        traceBeginAndSlog("StartHardwarePropertiesManagerService");
                        iBinder6 = new HardwarePropertiesManagerService(context);
                        ServiceManager.addService("hardware_properties", iBinder6);
                        iBinder4 = iBinder6;
                        traceEnd();
                        traceBeginAndSlog("StartTwilightService");
                        this.mSystemServiceManager.startService(TwilightService.class);
                        traceEnd();
                        traceBeginAndSlog("StartColorDisplay");
                        this.mSystemServiceManager.startService(ColorDisplayService.class);
                        traceEnd();
                        traceBeginAndSlog("StartJobScheduler");
                        if (!this.mHelper.startColorJobSchedulerService()) {
                        }
                        traceEnd();
                        traceBeginAndSlog("StartSoundTrigger");
                        this.mSystemServiceManager.startService(SoundTriggerService.class);
                        traceEnd();
                        traceBeginAndSlog("StartTrustManager");
                        this.mSystemServiceManager.startService(TrustManagerService.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                        }
                        traceBeginAndSlog("StartAppWidgetService");
                        this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                        traceEnd();
                        traceBeginAndSlog("StartRoleManagerService");
                        SystemServiceManager systemServiceManager2 = this.mSystemServiceManager;
                        Context context22 = this.mSystemContext;
                        systemServiceManager2.startService(new RoleManagerService(context22, new LegacyRoleResolutionPolicy(context22)));
                        traceEnd();
                        traceBeginAndSlog("StartVoiceRecognitionManager");
                        this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                        traceEnd();
                        if (GestureLauncherService.isGestureLauncherEnabled(context.getResources())) {
                        }
                        traceBeginAndSlog("StartSensorNotification");
                        this.mSystemServiceManager.startService(SensorNotificationService.class);
                        traceEnd();
                        traceBeginAndSlog("StartContextHubSystemService");
                        this.mSystemServiceManager.startService(ContextHubSystemService.class);
                        traceEnd();
                        traceBeginAndSlog("StartDiskStatsService");
                        ServiceManager.addService("diskstats", new DiskStatsService(context));
                        traceEnd();
                        Slog.i(TAG, "Oppo modem nimidump Service");
                        oppomodemService5 = new OppomodemService(context);
                        ServiceManager.addService("modem_crash_up", (IBinder) oppomodemService5);
                        oppomodemService3 = oppomodemService5;
                        traceBeginAndSlog("CabcService");
                        Slog.i(TAG, "Cabc Service");
                        iBinder5 = new CabcService(context);
                        ServiceManager.addService("cabc", iBinder5);
                        Trace.traceEnd(524288);
                        traceBeginAndSlog("RuntimeService");
                        ServiceManager.addService("runtime", new RuntimeService(context));
                        traceEnd();
                        Slog.i(TAG, "Torch Service");
                        TorchManagerService.getInstance(context).systemReady();
                        traceBeginAndSlog("StartHypnusService");
                        Slog.i(TAG, "Hypnus Service");
                        this.hypnusService = new HypnusService(context);
                        ServiceManager.addService("hypnus", (IBinder) this.hypnusService);
                        traceEnd();
                        Slog.i(TAG, "Oppo Customize Service");
                        this.oppoCustomize = new OppoCustomizeService(context);
                        ServiceManager.addService("oppocustomize", (IBinder) this.oppoCustomize);
                        if (this.mOnlyCore && context.getResources().getBoolean(17891450)) {
                        }
                        if (isWatch) {
                        }
                        traceBeginAndSlog("CertBlacklister");
                        new CertBlacklister(context);
                        traceEnd();
                        traceBeginAndSlog("StartEmergencyAffordanceService");
                        this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                        traceEnd();
                        traceBeginAndSlog("StartDreamManager");
                        this.mSystemServiceManager.startService(DreamManagerService.class);
                        traceEnd();
                        traceBeginAndSlog("AddGraphicsStatsService");
                        ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, (IBinder) new GraphicsStatsService(context));
                        traceEnd();
                        if (CoverageService.ENABLED) {
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.companion_device_setup")) {
                        }
                        traceBeginAndSlog("StartRestrictionManager");
                        this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                        traceEnd();
                        traceBeginAndSlog("StartMediaSessionService");
                        Slog.i(TAG, "Media Session Service");
                        this.mSystemServiceManager.startService(MediaSessionService.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                        }
                        traceBeginAndSlog("StartTvInputManager");
                        this.mSystemServiceManager.startService(TvInputManagerService.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                        }
                        traceBeginAndSlog("StartMediaRouterService");
                        mediaRouterService = new MediaRouterService(context);
                        ServiceManager.addService("media_router", (IBinder) mediaRouterService);
                        mediaRouter2 = mediaRouterService;
                        traceEnd();
                        hasFeatureFace = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.face");
                        hasFeatureIris = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.iris");
                        hasFeatureFingerprint = this.mPackageManager.hasSystemFeature("android.hardware.fingerprint");
                        if (hasFeatureFace) {
                        }
                        if (hasFeatureIris) {
                        }
                        if (hasFeatureFingerprint) {
                        }
                        traceBeginAndSlog("StartBiometricService");
                        Slog.i(TAG, "BiometricService Service");
                        this.mBiometricService = (BiometricService) this.mSystemServiceManager.startService(BiometricService.class);
                        this.mAlipayService = (AlipayService) this.mSystemServiceManager.startService(AlipayService.class);
                        traceEnd();
                        traceBeginAndSlog("StartBackgroundDexOptService");
                        BackgroundDexOptService.schedule(context);
                        traceEnd();
                        if (OppoOperatorManager.SERVICE_ENABLED) {
                        }
                        if (!isWatch) {
                        }
                        Slog.i(TAG, "Oppo Engineer Service");
                        this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                        if (!isWatch) {
                        }
                        traceBeginAndSlog("StartShortcutServiceLifecycle");
                        this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                        traceEnd();
                        traceBeginAndSlog("StartLauncherAppsService");
                        this.mSystemServiceManager.startService(LauncherAppsService.class);
                        traceEnd();
                        traceBeginAndSlog("StartCrossProfileAppsService");
                        this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                        traceEnd();
                        location = location2;
                        r19 = iBinder9;
                        countryDetector = countryDetector2;
                        networkTimeUpdater = networkTimeUpdater3;
                        mediaRouter = mediaRouter2;
                        networkStats = r15;
                        if (!isWatch) {
                        }
                        if (isWatch) {
                        }
                        if (!disableSlices) {
                        }
                        if (!disableCameraService) {
                        }
                        if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                        }
                        traceBeginAndSlog("StartStatsCompanionService");
                        this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                        traceEnd();
                        traceBeginAndSlog("StartIncidentCompanionService");
                        this.mSystemServiceManager.startService(IncidentCompanionService.class);
                        traceEnd();
                        if (safeMode) {
                        }
                        traceBeginAndSlog("StartMmsService");
                        MmsServiceBroker mmsService222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                        }
                        traceBeginAndSlog("StartClipboardService");
                        this.mSystemServiceManager.startService(ClipboardService.class);
                        traceEnd();
                        traceBeginAndSlog("AppServiceManager");
                        this.mSystemServiceManager.startService(AppBindingService.Lifecycle.class);
                        traceEnd();
                        sMtkSystemServerIns.startMtkOtherServices();
                        this.mHelper.startOtherServices();
                        traceBeginAndSlog("MakeVibratorServiceReady");
                        vibrator.systemReady();
                        traceEnd();
                        traceBeginAndSlog("MakeLockSettingsServiceReady");
                        if (lockSettings != null) {
                        }
                        traceEnd();
                        traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                        traceEnd();
                        traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                        this.mSystemServiceManager.startBootPhase(500);
                        traceEnd();
                        traceBeginAndSlog("MakeWindowManagerServiceReady");
                        Slog.i(TAG, "wms systemReady");
                        wm.systemReady();
                        traceEnd();
                        if (safeMode) {
                        }
                        Configuration config222222222222 = wm.computeNewConfiguration(0);
                        DisplayMetrics metrics222222222222 = new DisplayMetrics();
                        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics222222222222);
                        context.getResources().updateConfiguration(config222222222222, metrics222222222222);
                        systemTheme = context.getTheme();
                        if (systemTheme.getChangingConfigurations() != 0) {
                        }
                        traceBeginAndSlog("MakePowerManagerServiceReady");
                        Slog.i(TAG, "power manager systemReady");
                        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                        traceEnd();
                        if (this.mFingerprintService != null) {
                        }
                        if (this.mFaceService != null) {
                        }
                        if (this.mBiometricService != null) {
                        }
                        if (this.mAlipayService != null) {
                        }
                        traceBeginAndSlog("StartPermissionPolicyService");
                        this.mSystemServiceManager.startService(PermissionPolicyService.class);
                        traceEnd();
                        if (this.mOppoOperatorManagerService != null) {
                        }
                        traceBeginAndSlog("MakePackageManagerServiceReady");
                        Slog.i(TAG, "Package systemReady");
                        this.mPackageManagerService.systemReady();
                        traceEnd();
                        traceBeginAndSlog("MakeOppoCustomizeServiceReady");
                        if (this.oppoCustomize != null) {
                        }
                        traceEnd();
                        traceBeginAndSlog("MakeDisplayManagerServiceReady");
                        Slog.i(TAG, "DisplayManager systemReady");
                        this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                        traceEnd();
                        this.mSystemServiceManager.setSafeMode(safeMode);
                        traceBeginAndSlog("StartDeviceSpecificServices");
                        String[] classes222222222222 = this.mSystemContext.getResources().getStringArray(17236011);
                        while (r6 < r7) {
                        }
                        traceEnd();
                        traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                        traceEnd();
                        this.mHelper.systemReady();
                        Slog.i(TAG, "Ams systemReady");
                        this.mActivityManagerService.systemReady(new Runnable(context, wm, safeMode, iConnectivityManager, networkManagement, networkPolicy, r19, networkStats, location, countryDetector, networkTimeUpdater, inputManager, telephonyRegistry, mediaRouter, oppomodemService, mmsService222222222222) {
                            /* class com.android.server.$$Lambda$SystemServer$Veuf5e15txqWe0TdLPFg29kfanQ */
                            private final /* synthetic */ Context f$1;
                            private final /* synthetic */ CountryDetectorService f$10;
                            private final /* synthetic */ NetworkTimeUpdateService f$11;
                            private final /* synthetic */ InputManagerService f$12;
                            private final /* synthetic */ TelephonyRegistry f$13;
                            private final /* synthetic */ MediaRouterService f$14;
                            private final /* synthetic */ OppomodemService f$15;
                            private final /* synthetic */ MmsServiceBroker f$16;
                            private final /* synthetic */ WindowManagerService f$2;
                            private final /* synthetic */ boolean f$3;
                            private final /* synthetic */ ConnectivityService f$4;
                            private final /* synthetic */ NetworkManagementService f$5;
                            private final /* synthetic */ NetworkPolicyManagerService f$6;
                            private final /* synthetic */ IpSecService f$7;
                            private final /* synthetic */ NetworkStatsService f$8;
                            private final /* synthetic */ LocationManagerService f$9;

                            {
                                this.f$1 = r4;
                                this.f$2 = r5;
                                this.f$3 = r6;
                                this.f$4 = r7;
                                this.f$5 = r8;
                                this.f$6 = r9;
                                this.f$7 = r10;
                                this.f$8 = r11;
                                this.f$9 = r12;
                                this.f$10 = r13;
                                this.f$11 = r14;
                                this.f$12 = r15;
                                this.f$13 = r16;
                                this.f$14 = r17;
                                this.f$15 = r18;
                                this.f$16 = r19;
                            }

                            public final void run() {
                                SystemServer.this.lambda$startOtherServices$4$SystemServer(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15, this.f$16);
                            }
                        }, BOOT_TIMINGS_TRACE_LOG);
                    }
                    traceEnd();
                    traceBeginAndSlog("StartCountryDetectorService");
                    countryDetectorService = new CountryDetectorService(context);
                    try {
                        ServiceManager.addService("country_detector", (IBinder) countryDetectorService);
                        countryDetector2 = countryDetectorService;
                    } catch (Throwable th8) {
                        e8 = th8;
                        countryDetector3 = countryDetectorService;
                        countryDetector2 = countryDetector3;
                        reportWtf("starting Country Detector", e8);
                        traceEnd();
                        traceBeginAndSlog("StartTimeDetectorService");
                        this.mSystemServiceManager.startService(TIME_DETECTOR_SERVICE_CLASS);
                        traceEnd();
                        if (!isWatch) {
                        }
                        AgingCriticalEvent instance22 = AgingCriticalEvent.getInstance();
                        networkPolicy = networkPolicy2;
                        StringBuilder sb22 = new StringBuilder();
                        iConnectivityManager = iConnectivityManager3;
                        sb22.append("systemserver pid:");
                        sb22.append(Process.myPid());
                        instance22.writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, sb22.toString());
                        if (context.getResources().getBoolean(17891451)) {
                        }
                        traceBeginAndSlog("StartAudioService");
                        if (isArc) {
                        }
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.hardware.broadcastradio")) {
                        }
                        traceBeginAndSlog("StartDockObserver");
                        Slog.i(TAG, "DockObserver");
                        this.mSystemServiceManager.startService(DockObserver.class);
                        traceEnd();
                        if (isWatch) {
                        }
                        traceBeginAndSlog("StartWiredAccessoryManager");
                        inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.midi")) {
                        }
                        traceBeginAndSlog("StartAdbService");
                        this.mSystemServiceManager.startService(ADB_SERVICE_CLASS);
                        traceEnd();
                        traceBeginAndSlog("StartUsbService");
                        this.mSystemServiceManager.startService(USB_SERVICE_CLASS);
                        traceEnd();
                        if (!isWatch) {
                        }
                        traceBeginAndSlog("StartHardwarePropertiesManagerService");
                        iBinder6 = new HardwarePropertiesManagerService(context);
                        ServiceManager.addService("hardware_properties", iBinder6);
                        iBinder4 = iBinder6;
                        traceEnd();
                        traceBeginAndSlog("StartTwilightService");
                        this.mSystemServiceManager.startService(TwilightService.class);
                        traceEnd();
                        traceBeginAndSlog("StartColorDisplay");
                        this.mSystemServiceManager.startService(ColorDisplayService.class);
                        traceEnd();
                        traceBeginAndSlog("StartJobScheduler");
                        if (!this.mHelper.startColorJobSchedulerService()) {
                        }
                        traceEnd();
                        traceBeginAndSlog("StartSoundTrigger");
                        this.mSystemServiceManager.startService(SoundTriggerService.class);
                        traceEnd();
                        traceBeginAndSlog("StartTrustManager");
                        this.mSystemServiceManager.startService(TrustManagerService.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                        }
                        traceBeginAndSlog("StartAppWidgetService");
                        this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                        traceEnd();
                        traceBeginAndSlog("StartRoleManagerService");
                        SystemServiceManager systemServiceManager22 = this.mSystemServiceManager;
                        Context context222 = this.mSystemContext;
                        systemServiceManager22.startService(new RoleManagerService(context222, new LegacyRoleResolutionPolicy(context222)));
                        traceEnd();
                        traceBeginAndSlog("StartVoiceRecognitionManager");
                        this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                        traceEnd();
                        if (GestureLauncherService.isGestureLauncherEnabled(context.getResources())) {
                        }
                        traceBeginAndSlog("StartSensorNotification");
                        this.mSystemServiceManager.startService(SensorNotificationService.class);
                        traceEnd();
                        traceBeginAndSlog("StartContextHubSystemService");
                        this.mSystemServiceManager.startService(ContextHubSystemService.class);
                        traceEnd();
                        traceBeginAndSlog("StartDiskStatsService");
                        ServiceManager.addService("diskstats", new DiskStatsService(context));
                        traceEnd();
                        Slog.i(TAG, "Oppo modem nimidump Service");
                        oppomodemService5 = new OppomodemService(context);
                        ServiceManager.addService("modem_crash_up", (IBinder) oppomodemService5);
                        oppomodemService3 = oppomodemService5;
                        traceBeginAndSlog("CabcService");
                        Slog.i(TAG, "Cabc Service");
                        iBinder5 = new CabcService(context);
                        ServiceManager.addService("cabc", iBinder5);
                        Trace.traceEnd(524288);
                        traceBeginAndSlog("RuntimeService");
                        ServiceManager.addService("runtime", new RuntimeService(context));
                        traceEnd();
                        Slog.i(TAG, "Torch Service");
                        TorchManagerService.getInstance(context).systemReady();
                        traceBeginAndSlog("StartHypnusService");
                        Slog.i(TAG, "Hypnus Service");
                        this.hypnusService = new HypnusService(context);
                        ServiceManager.addService("hypnus", (IBinder) this.hypnusService);
                        traceEnd();
                        Slog.i(TAG, "Oppo Customize Service");
                        this.oppoCustomize = new OppoCustomizeService(context);
                        ServiceManager.addService("oppocustomize", (IBinder) this.oppoCustomize);
                        if (this.mOnlyCore && context.getResources().getBoolean(17891450)) {
                        }
                        if (isWatch) {
                        }
                        traceBeginAndSlog("CertBlacklister");
                        new CertBlacklister(context);
                        traceEnd();
                        traceBeginAndSlog("StartEmergencyAffordanceService");
                        this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                        traceEnd();
                        traceBeginAndSlog("StartDreamManager");
                        this.mSystemServiceManager.startService(DreamManagerService.class);
                        traceEnd();
                        traceBeginAndSlog("AddGraphicsStatsService");
                        ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, (IBinder) new GraphicsStatsService(context));
                        traceEnd();
                        if (CoverageService.ENABLED) {
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.companion_device_setup")) {
                        }
                        traceBeginAndSlog("StartRestrictionManager");
                        this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                        traceEnd();
                        traceBeginAndSlog("StartMediaSessionService");
                        Slog.i(TAG, "Media Session Service");
                        this.mSystemServiceManager.startService(MediaSessionService.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                        }
                        traceBeginAndSlog("StartTvInputManager");
                        this.mSystemServiceManager.startService(TvInputManagerService.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                        }
                        traceBeginAndSlog("StartMediaRouterService");
                        mediaRouterService = new MediaRouterService(context);
                        ServiceManager.addService("media_router", (IBinder) mediaRouterService);
                        mediaRouter2 = mediaRouterService;
                        traceEnd();
                        hasFeatureFace = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.face");
                        hasFeatureIris = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.iris");
                        hasFeatureFingerprint = this.mPackageManager.hasSystemFeature("android.hardware.fingerprint");
                        if (hasFeatureFace) {
                        }
                        if (hasFeatureIris) {
                        }
                        if (hasFeatureFingerprint) {
                        }
                        traceBeginAndSlog("StartBiometricService");
                        Slog.i(TAG, "BiometricService Service");
                        this.mBiometricService = (BiometricService) this.mSystemServiceManager.startService(BiometricService.class);
                        this.mAlipayService = (AlipayService) this.mSystemServiceManager.startService(AlipayService.class);
                        traceEnd();
                        traceBeginAndSlog("StartBackgroundDexOptService");
                        BackgroundDexOptService.schedule(context);
                        traceEnd();
                        if (OppoOperatorManager.SERVICE_ENABLED) {
                        }
                        if (!isWatch) {
                        }
                        Slog.i(TAG, "Oppo Engineer Service");
                        this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                        if (!isWatch) {
                        }
                        traceBeginAndSlog("StartShortcutServiceLifecycle");
                        this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                        traceEnd();
                        traceBeginAndSlog("StartLauncherAppsService");
                        this.mSystemServiceManager.startService(LauncherAppsService.class);
                        traceEnd();
                        traceBeginAndSlog("StartCrossProfileAppsService");
                        this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                        traceEnd();
                        location = location2;
                        r19 = iBinder9;
                        countryDetector = countryDetector2;
                        networkTimeUpdater = networkTimeUpdater3;
                        mediaRouter = mediaRouter2;
                        networkStats = r15;
                        if (!isWatch) {
                        }
                        if (isWatch) {
                        }
                        if (!disableSlices) {
                        }
                        if (!disableCameraService) {
                        }
                        if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                        }
                        traceBeginAndSlog("StartStatsCompanionService");
                        this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                        traceEnd();
                        traceBeginAndSlog("StartIncidentCompanionService");
                        this.mSystemServiceManager.startService(IncidentCompanionService.class);
                        traceEnd();
                        if (safeMode) {
                        }
                        traceBeginAndSlog("StartMmsService");
                        MmsServiceBroker mmsService2222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                        }
                        traceBeginAndSlog("StartClipboardService");
                        this.mSystemServiceManager.startService(ClipboardService.class);
                        traceEnd();
                        traceBeginAndSlog("AppServiceManager");
                        this.mSystemServiceManager.startService(AppBindingService.Lifecycle.class);
                        traceEnd();
                        sMtkSystemServerIns.startMtkOtherServices();
                        this.mHelper.startOtherServices();
                        traceBeginAndSlog("MakeVibratorServiceReady");
                        vibrator.systemReady();
                        traceEnd();
                        traceBeginAndSlog("MakeLockSettingsServiceReady");
                        if (lockSettings != null) {
                        }
                        traceEnd();
                        traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                        traceEnd();
                        traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                        this.mSystemServiceManager.startBootPhase(500);
                        traceEnd();
                        traceBeginAndSlog("MakeWindowManagerServiceReady");
                        Slog.i(TAG, "wms systemReady");
                        wm.systemReady();
                        traceEnd();
                        if (safeMode) {
                        }
                        Configuration config2222222222222 = wm.computeNewConfiguration(0);
                        DisplayMetrics metrics2222222222222 = new DisplayMetrics();
                        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics2222222222222);
                        context.getResources().updateConfiguration(config2222222222222, metrics2222222222222);
                        systemTheme = context.getTheme();
                        if (systemTheme.getChangingConfigurations() != 0) {
                        }
                        traceBeginAndSlog("MakePowerManagerServiceReady");
                        Slog.i(TAG, "power manager systemReady");
                        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                        traceEnd();
                        if (this.mFingerprintService != null) {
                        }
                        if (this.mFaceService != null) {
                        }
                        if (this.mBiometricService != null) {
                        }
                        if (this.mAlipayService != null) {
                        }
                        traceBeginAndSlog("StartPermissionPolicyService");
                        this.mSystemServiceManager.startService(PermissionPolicyService.class);
                        traceEnd();
                        if (this.mOppoOperatorManagerService != null) {
                        }
                        traceBeginAndSlog("MakePackageManagerServiceReady");
                        Slog.i(TAG, "Package systemReady");
                        this.mPackageManagerService.systemReady();
                        traceEnd();
                        traceBeginAndSlog("MakeOppoCustomizeServiceReady");
                        if (this.oppoCustomize != null) {
                        }
                        traceEnd();
                        traceBeginAndSlog("MakeDisplayManagerServiceReady");
                        Slog.i(TAG, "DisplayManager systemReady");
                        this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                        traceEnd();
                        this.mSystemServiceManager.setSafeMode(safeMode);
                        traceBeginAndSlog("StartDeviceSpecificServices");
                        String[] classes2222222222222 = this.mSystemContext.getResources().getStringArray(17236011);
                        while (r6 < r7) {
                        }
                        traceEnd();
                        traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                        traceEnd();
                        this.mHelper.systemReady();
                        Slog.i(TAG, "Ams systemReady");
                        this.mActivityManagerService.systemReady(new Runnable(context, wm, safeMode, iConnectivityManager, networkManagement, networkPolicy, r19, networkStats, location, countryDetector, networkTimeUpdater, inputManager, telephonyRegistry, mediaRouter, oppomodemService, mmsService2222222222222) {
                            /* class com.android.server.$$Lambda$SystemServer$Veuf5e15txqWe0TdLPFg29kfanQ */
                            private final /* synthetic */ Context f$1;
                            private final /* synthetic */ CountryDetectorService f$10;
                            private final /* synthetic */ NetworkTimeUpdateService f$11;
                            private final /* synthetic */ InputManagerService f$12;
                            private final /* synthetic */ TelephonyRegistry f$13;
                            private final /* synthetic */ MediaRouterService f$14;
                            private final /* synthetic */ OppomodemService f$15;
                            private final /* synthetic */ MmsServiceBroker f$16;
                            private final /* synthetic */ WindowManagerService f$2;
                            private final /* synthetic */ boolean f$3;
                            private final /* synthetic */ ConnectivityService f$4;
                            private final /* synthetic */ NetworkManagementService f$5;
                            private final /* synthetic */ NetworkPolicyManagerService f$6;
                            private final /* synthetic */ IpSecService f$7;
                            private final /* synthetic */ NetworkStatsService f$8;
                            private final /* synthetic */ LocationManagerService f$9;

                            {
                                this.f$1 = r4;
                                this.f$2 = r5;
                                this.f$3 = r6;
                                this.f$4 = r7;
                                this.f$5 = r8;
                                this.f$6 = r9;
                                this.f$7 = r10;
                                this.f$8 = r11;
                                this.f$9 = r12;
                                this.f$10 = r13;
                                this.f$11 = r14;
                                this.f$12 = r15;
                                this.f$13 = r16;
                                this.f$14 = r17;
                                this.f$15 = r18;
                                this.f$16 = r19;
                            }

                            public final void run() {
                                SystemServer.this.lambda$startOtherServices$4$SystemServer(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15, this.f$16);
                            }
                        }, BOOT_TIMINGS_TRACE_LOG);
                    }
                    traceEnd();
                    traceBeginAndSlog("StartTimeDetectorService");
                    try {
                        this.mSystemServiceManager.startService(TIME_DETECTOR_SERVICE_CLASS);
                    } catch (Throwable th9) {
                        e7 = th9;
                    }
                    traceEnd();
                    if (!isWatch) {
                    }
                    AgingCriticalEvent instance222 = AgingCriticalEvent.getInstance();
                    networkPolicy = networkPolicy2;
                    StringBuilder sb222 = new StringBuilder();
                    iConnectivityManager = iConnectivityManager3;
                    sb222.append("systemserver pid:");
                    sb222.append(Process.myPid());
                    instance222.writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, sb222.toString());
                    if (context.getResources().getBoolean(17891451)) {
                    }
                    traceBeginAndSlog("StartAudioService");
                    if (isArc) {
                    }
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.hardware.broadcastradio")) {
                    }
                    traceBeginAndSlog("StartDockObserver");
                    Slog.i(TAG, "DockObserver");
                    this.mSystemServiceManager.startService(DockObserver.class);
                    traceEnd();
                    if (isWatch) {
                    }
                    traceBeginAndSlog("StartWiredAccessoryManager");
                    inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.software.midi")) {
                    }
                    traceBeginAndSlog("StartAdbService");
                    this.mSystemServiceManager.startService(ADB_SERVICE_CLASS);
                    traceEnd();
                    traceBeginAndSlog("StartUsbService");
                    this.mSystemServiceManager.startService(USB_SERVICE_CLASS);
                    traceEnd();
                    if (!isWatch) {
                    }
                    traceBeginAndSlog("StartHardwarePropertiesManagerService");
                    iBinder6 = new HardwarePropertiesManagerService(context);
                    try {
                        ServiceManager.addService("hardware_properties", iBinder6);
                        iBinder4 = iBinder6;
                    } catch (Throwable th10) {
                        e5 = th10;
                        Slog.e(TAG, "Failure starting HardwarePropertiesManagerService", e5);
                        iBinder4 = iBinder6;
                        traceEnd();
                        traceBeginAndSlog("StartTwilightService");
                        this.mSystemServiceManager.startService(TwilightService.class);
                        traceEnd();
                        traceBeginAndSlog("StartColorDisplay");
                        this.mSystemServiceManager.startService(ColorDisplayService.class);
                        traceEnd();
                        traceBeginAndSlog("StartJobScheduler");
                        if (!this.mHelper.startColorJobSchedulerService()) {
                        }
                        traceEnd();
                        traceBeginAndSlog("StartSoundTrigger");
                        this.mSystemServiceManager.startService(SoundTriggerService.class);
                        traceEnd();
                        traceBeginAndSlog("StartTrustManager");
                        this.mSystemServiceManager.startService(TrustManagerService.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                        }
                        traceBeginAndSlog("StartAppWidgetService");
                        this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                        traceEnd();
                        traceBeginAndSlog("StartRoleManagerService");
                        SystemServiceManager systemServiceManager222 = this.mSystemServiceManager;
                        Context context2222 = this.mSystemContext;
                        systemServiceManager222.startService(new RoleManagerService(context2222, new LegacyRoleResolutionPolicy(context2222)));
                        traceEnd();
                        traceBeginAndSlog("StartVoiceRecognitionManager");
                        this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                        traceEnd();
                        if (GestureLauncherService.isGestureLauncherEnabled(context.getResources())) {
                        }
                        traceBeginAndSlog("StartSensorNotification");
                        this.mSystemServiceManager.startService(SensorNotificationService.class);
                        traceEnd();
                        traceBeginAndSlog("StartContextHubSystemService");
                        this.mSystemServiceManager.startService(ContextHubSystemService.class);
                        traceEnd();
                        traceBeginAndSlog("StartDiskStatsService");
                        ServiceManager.addService("diskstats", new DiskStatsService(context));
                        traceEnd();
                        Slog.i(TAG, "Oppo modem nimidump Service");
                        oppomodemService5 = new OppomodemService(context);
                        ServiceManager.addService("modem_crash_up", (IBinder) oppomodemService5);
                        oppomodemService3 = oppomodemService5;
                        traceBeginAndSlog("CabcService");
                        Slog.i(TAG, "Cabc Service");
                        iBinder5 = new CabcService(context);
                        ServiceManager.addService("cabc", iBinder5);
                        Trace.traceEnd(524288);
                        traceBeginAndSlog("RuntimeService");
                        ServiceManager.addService("runtime", new RuntimeService(context));
                        traceEnd();
                        Slog.i(TAG, "Torch Service");
                        TorchManagerService.getInstance(context).systemReady();
                        traceBeginAndSlog("StartHypnusService");
                        Slog.i(TAG, "Hypnus Service");
                        this.hypnusService = new HypnusService(context);
                        ServiceManager.addService("hypnus", (IBinder) this.hypnusService);
                        traceEnd();
                        Slog.i(TAG, "Oppo Customize Service");
                        this.oppoCustomize = new OppoCustomizeService(context);
                        ServiceManager.addService("oppocustomize", (IBinder) this.oppoCustomize);
                        if (this.mOnlyCore && context.getResources().getBoolean(17891450)) {
                        }
                        if (isWatch) {
                        }
                        traceBeginAndSlog("CertBlacklister");
                        new CertBlacklister(context);
                        traceEnd();
                        traceBeginAndSlog("StartEmergencyAffordanceService");
                        this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                        traceEnd();
                        traceBeginAndSlog("StartDreamManager");
                        this.mSystemServiceManager.startService(DreamManagerService.class);
                        traceEnd();
                        traceBeginAndSlog("AddGraphicsStatsService");
                        ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, (IBinder) new GraphicsStatsService(context));
                        traceEnd();
                        if (CoverageService.ENABLED) {
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.companion_device_setup")) {
                        }
                        traceBeginAndSlog("StartRestrictionManager");
                        this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                        traceEnd();
                        traceBeginAndSlog("StartMediaSessionService");
                        Slog.i(TAG, "Media Session Service");
                        this.mSystemServiceManager.startService(MediaSessionService.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                        }
                        traceBeginAndSlog("StartTvInputManager");
                        this.mSystemServiceManager.startService(TvInputManagerService.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                        }
                        traceBeginAndSlog("StartMediaRouterService");
                        mediaRouterService = new MediaRouterService(context);
                        ServiceManager.addService("media_router", (IBinder) mediaRouterService);
                        mediaRouter2 = mediaRouterService;
                        traceEnd();
                        hasFeatureFace = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.face");
                        hasFeatureIris = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.iris");
                        hasFeatureFingerprint = this.mPackageManager.hasSystemFeature("android.hardware.fingerprint");
                        if (hasFeatureFace) {
                        }
                        if (hasFeatureIris) {
                        }
                        if (hasFeatureFingerprint) {
                        }
                        traceBeginAndSlog("StartBiometricService");
                        Slog.i(TAG, "BiometricService Service");
                        this.mBiometricService = (BiometricService) this.mSystemServiceManager.startService(BiometricService.class);
                        this.mAlipayService = (AlipayService) this.mSystemServiceManager.startService(AlipayService.class);
                        traceEnd();
                        traceBeginAndSlog("StartBackgroundDexOptService");
                        BackgroundDexOptService.schedule(context);
                        traceEnd();
                        if (OppoOperatorManager.SERVICE_ENABLED) {
                        }
                        if (!isWatch) {
                        }
                        Slog.i(TAG, "Oppo Engineer Service");
                        this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                        if (!isWatch) {
                        }
                        traceBeginAndSlog("StartShortcutServiceLifecycle");
                        this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                        traceEnd();
                        traceBeginAndSlog("StartLauncherAppsService");
                        this.mSystemServiceManager.startService(LauncherAppsService.class);
                        traceEnd();
                        traceBeginAndSlog("StartCrossProfileAppsService");
                        this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                        traceEnd();
                        location = location2;
                        r19 = iBinder9;
                        countryDetector = countryDetector2;
                        networkTimeUpdater = networkTimeUpdater3;
                        mediaRouter = mediaRouter2;
                        networkStats = r15;
                        if (!isWatch) {
                        }
                        if (isWatch) {
                        }
                        if (!disableSlices) {
                        }
                        if (!disableCameraService) {
                        }
                        if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                        }
                        traceBeginAndSlog("StartStatsCompanionService");
                        this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                        traceEnd();
                        traceBeginAndSlog("StartIncidentCompanionService");
                        this.mSystemServiceManager.startService(IncidentCompanionService.class);
                        traceEnd();
                        if (safeMode) {
                        }
                        traceBeginAndSlog("StartMmsService");
                        MmsServiceBroker mmsService22222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                        }
                        traceBeginAndSlog("StartClipboardService");
                        this.mSystemServiceManager.startService(ClipboardService.class);
                        traceEnd();
                        traceBeginAndSlog("AppServiceManager");
                        this.mSystemServiceManager.startService(AppBindingService.Lifecycle.class);
                        traceEnd();
                        sMtkSystemServerIns.startMtkOtherServices();
                        this.mHelper.startOtherServices();
                        traceBeginAndSlog("MakeVibratorServiceReady");
                        vibrator.systemReady();
                        traceEnd();
                        traceBeginAndSlog("MakeLockSettingsServiceReady");
                        if (lockSettings != null) {
                        }
                        traceEnd();
                        traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                        traceEnd();
                        traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                        this.mSystemServiceManager.startBootPhase(500);
                        traceEnd();
                        traceBeginAndSlog("MakeWindowManagerServiceReady");
                        Slog.i(TAG, "wms systemReady");
                        wm.systemReady();
                        traceEnd();
                        if (safeMode) {
                        }
                        Configuration config22222222222222 = wm.computeNewConfiguration(0);
                        DisplayMetrics metrics22222222222222 = new DisplayMetrics();
                        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics22222222222222);
                        context.getResources().updateConfiguration(config22222222222222, metrics22222222222222);
                        systemTheme = context.getTheme();
                        if (systemTheme.getChangingConfigurations() != 0) {
                        }
                        traceBeginAndSlog("MakePowerManagerServiceReady");
                        Slog.i(TAG, "power manager systemReady");
                        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                        traceEnd();
                        if (this.mFingerprintService != null) {
                        }
                        if (this.mFaceService != null) {
                        }
                        if (this.mBiometricService != null) {
                        }
                        if (this.mAlipayService != null) {
                        }
                        traceBeginAndSlog("StartPermissionPolicyService");
                        this.mSystemServiceManager.startService(PermissionPolicyService.class);
                        traceEnd();
                        if (this.mOppoOperatorManagerService != null) {
                        }
                        traceBeginAndSlog("MakePackageManagerServiceReady");
                        Slog.i(TAG, "Package systemReady");
                        this.mPackageManagerService.systemReady();
                        traceEnd();
                        traceBeginAndSlog("MakeOppoCustomizeServiceReady");
                        if (this.oppoCustomize != null) {
                        }
                        traceEnd();
                        traceBeginAndSlog("MakeDisplayManagerServiceReady");
                        Slog.i(TAG, "DisplayManager systemReady");
                        this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                        traceEnd();
                        this.mSystemServiceManager.setSafeMode(safeMode);
                        traceBeginAndSlog("StartDeviceSpecificServices");
                        String[] classes22222222222222 = this.mSystemContext.getResources().getStringArray(17236011);
                        while (r6 < r7) {
                        }
                        traceEnd();
                        traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                        traceEnd();
                        this.mHelper.systemReady();
                        Slog.i(TAG, "Ams systemReady");
                        this.mActivityManagerService.systemReady(new Runnable(context, wm, safeMode, iConnectivityManager, networkManagement, networkPolicy, r19, networkStats, location, countryDetector, networkTimeUpdater, inputManager, telephonyRegistry, mediaRouter, oppomodemService, mmsService22222222222222) {
                            /* class com.android.server.$$Lambda$SystemServer$Veuf5e15txqWe0TdLPFg29kfanQ */
                            private final /* synthetic */ Context f$1;
                            private final /* synthetic */ CountryDetectorService f$10;
                            private final /* synthetic */ NetworkTimeUpdateService f$11;
                            private final /* synthetic */ InputManagerService f$12;
                            private final /* synthetic */ TelephonyRegistry f$13;
                            private final /* synthetic */ MediaRouterService f$14;
                            private final /* synthetic */ OppomodemService f$15;
                            private final /* synthetic */ MmsServiceBroker f$16;
                            private final /* synthetic */ WindowManagerService f$2;
                            private final /* synthetic */ boolean f$3;
                            private final /* synthetic */ ConnectivityService f$4;
                            private final /* synthetic */ NetworkManagementService f$5;
                            private final /* synthetic */ NetworkPolicyManagerService f$6;
                            private final /* synthetic */ IpSecService f$7;
                            private final /* synthetic */ NetworkStatsService f$8;
                            private final /* synthetic */ LocationManagerService f$9;

                            {
                                this.f$1 = r4;
                                this.f$2 = r5;
                                this.f$3 = r6;
                                this.f$4 = r7;
                                this.f$5 = r8;
                                this.f$6 = r9;
                                this.f$7 = r10;
                                this.f$8 = r11;
                                this.f$9 = r12;
                                this.f$10 = r13;
                                this.f$11 = r14;
                                this.f$12 = r15;
                                this.f$13 = r16;
                                this.f$14 = r17;
                                this.f$15 = r18;
                                this.f$16 = r19;
                            }

                            public final void run() {
                                SystemServer.this.lambda$startOtherServices$4$SystemServer(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15, this.f$16);
                            }
                        }, BOOT_TIMINGS_TRACE_LOG);
                    }
                    traceEnd();
                    traceBeginAndSlog("StartTwilightService");
                    this.mSystemServiceManager.startService(TwilightService.class);
                    traceEnd();
                    traceBeginAndSlog("StartColorDisplay");
                    this.mSystemServiceManager.startService(ColorDisplayService.class);
                    traceEnd();
                    traceBeginAndSlog("StartJobScheduler");
                    if (!this.mHelper.startColorJobSchedulerService()) {
                    }
                    traceEnd();
                    traceBeginAndSlog("StartSoundTrigger");
                    this.mSystemServiceManager.startService(SoundTriggerService.class);
                    traceEnd();
                    traceBeginAndSlog("StartTrustManager");
                    this.mSystemServiceManager.startService(TrustManagerService.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                    }
                    traceBeginAndSlog("StartAppWidgetService");
                    this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                    traceEnd();
                    traceBeginAndSlog("StartRoleManagerService");
                    SystemServiceManager systemServiceManager2222 = this.mSystemServiceManager;
                    Context context22222 = this.mSystemContext;
                    systemServiceManager2222.startService(new RoleManagerService(context22222, new LegacyRoleResolutionPolicy(context22222)));
                    traceEnd();
                    traceBeginAndSlog("StartVoiceRecognitionManager");
                    this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                    traceEnd();
                    if (GestureLauncherService.isGestureLauncherEnabled(context.getResources())) {
                    }
                    traceBeginAndSlog("StartSensorNotification");
                    this.mSystemServiceManager.startService(SensorNotificationService.class);
                    traceEnd();
                    traceBeginAndSlog("StartContextHubSystemService");
                    this.mSystemServiceManager.startService(ContextHubSystemService.class);
                    traceEnd();
                    traceBeginAndSlog("StartDiskStatsService");
                    ServiceManager.addService("diskstats", new DiskStatsService(context));
                    traceEnd();
                    Slog.i(TAG, "Oppo modem nimidump Service");
                    oppomodemService5 = new OppomodemService(context);
                    try {
                        ServiceManager.addService("modem_crash_up", (IBinder) oppomodemService5);
                        oppomodemService3 = oppomodemService5;
                    } catch (Throwable th11) {
                        e4 = th11;
                        oppomodemService4 = oppomodemService5;
                    }
                    traceBeginAndSlog("CabcService");
                    Slog.i(TAG, "Cabc Service");
                    iBinder5 = new CabcService(context);
                    try {
                        ServiceManager.addService("cabc", iBinder5);
                    } catch (Throwable th12) {
                        e3 = th12;
                        iBinder2 = iBinder5;
                    }
                    Trace.traceEnd(524288);
                    traceBeginAndSlog("RuntimeService");
                    ServiceManager.addService("runtime", new RuntimeService(context));
                    traceEnd();
                    Slog.i(TAG, "Torch Service");
                    TorchManagerService.getInstance(context).systemReady();
                    traceBeginAndSlog("StartHypnusService");
                    Slog.i(TAG, "Hypnus Service");
                    this.hypnusService = new HypnusService(context);
                    ServiceManager.addService("hypnus", (IBinder) this.hypnusService);
                    traceEnd();
                    Slog.i(TAG, "Oppo Customize Service");
                    this.oppoCustomize = new OppoCustomizeService(context);
                    ServiceManager.addService("oppocustomize", (IBinder) this.oppoCustomize);
                    if (this.mOnlyCore && context.getResources().getBoolean(17891450)) {
                    }
                    if (isWatch) {
                    }
                    traceBeginAndSlog("CertBlacklister");
                    new CertBlacklister(context);
                    traceEnd();
                    traceBeginAndSlog("StartEmergencyAffordanceService");
                    this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                    traceEnd();
                    traceBeginAndSlog("StartDreamManager");
                    this.mSystemServiceManager.startService(DreamManagerService.class);
                    traceEnd();
                    traceBeginAndSlog("AddGraphicsStatsService");
                    ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, (IBinder) new GraphicsStatsService(context));
                    traceEnd();
                    if (CoverageService.ENABLED) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.companion_device_setup")) {
                    }
                    traceBeginAndSlog("StartRestrictionManager");
                    this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                    traceEnd();
                    traceBeginAndSlog("StartMediaSessionService");
                    Slog.i(TAG, "Media Session Service");
                    this.mSystemServiceManager.startService(MediaSessionService.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                    }
                    traceBeginAndSlog("StartTvInputManager");
                    this.mSystemServiceManager.startService(TvInputManagerService.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                    }
                    traceBeginAndSlog("StartMediaRouterService");
                    mediaRouterService = new MediaRouterService(context);
                    try {
                        ServiceManager.addService("media_router", (IBinder) mediaRouterService);
                        mediaRouter2 = mediaRouterService;
                    } catch (Throwable th13) {
                        e = th13;
                        mediaRouter3 = mediaRouterService;
                    }
                    traceEnd();
                    hasFeatureFace = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.face");
                    hasFeatureIris = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.iris");
                    hasFeatureFingerprint = this.mPackageManager.hasSystemFeature("android.hardware.fingerprint");
                    if (hasFeatureFace) {
                    }
                    if (hasFeatureIris) {
                    }
                    if (hasFeatureFingerprint) {
                    }
                    traceBeginAndSlog("StartBiometricService");
                    Slog.i(TAG, "BiometricService Service");
                    this.mBiometricService = (BiometricService) this.mSystemServiceManager.startService(BiometricService.class);
                    try {
                        this.mAlipayService = (AlipayService) this.mSystemServiceManager.startService(AlipayService.class);
                    } catch (Throwable e34) {
                        Slog.e(TAG, "start mAlipayService Failed:", e34);
                    }
                    traceEnd();
                    traceBeginAndSlog("StartBackgroundDexOptService");
                    BackgroundDexOptService.schedule(context);
                    traceEnd();
                    if (OppoOperatorManager.SERVICE_ENABLED) {
                    }
                    if (!isWatch) {
                    }
                    Slog.i(TAG, "Oppo Engineer Service");
                    this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                    if (!isWatch) {
                    }
                    traceBeginAndSlog("StartShortcutServiceLifecycle");
                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                    traceEnd();
                    traceBeginAndSlog("StartLauncherAppsService");
                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                    traceEnd();
                    traceBeginAndSlog("StartCrossProfileAppsService");
                    this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                    traceEnd();
                    location = location2;
                    r19 = iBinder9;
                    countryDetector = countryDetector2;
                    networkTimeUpdater = networkTimeUpdater3;
                    mediaRouter = mediaRouter2;
                    networkStats = r15;
                    if (!isWatch) {
                    }
                    if (isWatch) {
                    }
                    if (!disableSlices) {
                    }
                    if (!disableCameraService) {
                    }
                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                    }
                    traceBeginAndSlog("StartStatsCompanionService");
                    this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                    traceEnd();
                    traceBeginAndSlog("StartIncidentCompanionService");
                    this.mSystemServiceManager.startService(IncidentCompanionService.class);
                    traceEnd();
                    if (safeMode) {
                    }
                    traceBeginAndSlog("StartMmsService");
                    MmsServiceBroker mmsService222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                    }
                    traceBeginAndSlog("StartClipboardService");
                    this.mSystemServiceManager.startService(ClipboardService.class);
                    traceEnd();
                    traceBeginAndSlog("AppServiceManager");
                    this.mSystemServiceManager.startService(AppBindingService.Lifecycle.class);
                    traceEnd();
                    sMtkSystemServerIns.startMtkOtherServices();
                    this.mHelper.startOtherServices();
                    traceBeginAndSlog("MakeVibratorServiceReady");
                    vibrator.systemReady();
                    traceEnd();
                    traceBeginAndSlog("MakeLockSettingsServiceReady");
                    if (lockSettings != null) {
                    }
                    traceEnd();
                    traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                    traceEnd();
                    traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                    this.mSystemServiceManager.startBootPhase(500);
                    traceEnd();
                    traceBeginAndSlog("MakeWindowManagerServiceReady");
                    Slog.i(TAG, "wms systemReady");
                    wm.systemReady();
                    traceEnd();
                    if (safeMode) {
                    }
                    Configuration config222222222222222 = wm.computeNewConfiguration(0);
                    DisplayMetrics metrics222222222222222 = new DisplayMetrics();
                    ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics222222222222222);
                    context.getResources().updateConfiguration(config222222222222222, metrics222222222222222);
                    systemTheme = context.getTheme();
                    if (systemTheme.getChangingConfigurations() != 0) {
                    }
                    traceBeginAndSlog("MakePowerManagerServiceReady");
                    Slog.i(TAG, "power manager systemReady");
                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                    traceEnd();
                    if (this.mFingerprintService != null) {
                    }
                    if (this.mFaceService != null) {
                    }
                    if (this.mBiometricService != null) {
                    }
                    if (this.mAlipayService != null) {
                    }
                    traceBeginAndSlog("StartPermissionPolicyService");
                    this.mSystemServiceManager.startService(PermissionPolicyService.class);
                    traceEnd();
                    if (this.mOppoOperatorManagerService != null) {
                    }
                    traceBeginAndSlog("MakePackageManagerServiceReady");
                    Slog.i(TAG, "Package systemReady");
                    this.mPackageManagerService.systemReady();
                    traceEnd();
                    traceBeginAndSlog("MakeOppoCustomizeServiceReady");
                    if (this.oppoCustomize != null) {
                    }
                    traceEnd();
                    traceBeginAndSlog("MakeDisplayManagerServiceReady");
                    Slog.i(TAG, "DisplayManager systemReady");
                    this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                    traceEnd();
                    this.mSystemServiceManager.setSafeMode(safeMode);
                    traceBeginAndSlog("StartDeviceSpecificServices");
                    String[] classes222222222222222 = this.mSystemContext.getResources().getStringArray(17236011);
                    while (r6 < r7) {
                    }
                    traceEnd();
                    traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                    traceEnd();
                    this.mHelper.systemReady();
                    Slog.i(TAG, "Ams systemReady");
                    this.mActivityManagerService.systemReady(new Runnable(context, wm, safeMode, iConnectivityManager, networkManagement, networkPolicy, r19, networkStats, location, countryDetector, networkTimeUpdater, inputManager, telephonyRegistry, mediaRouter, oppomodemService, mmsService222222222222222) {
                        /* class com.android.server.$$Lambda$SystemServer$Veuf5e15txqWe0TdLPFg29kfanQ */
                        private final /* synthetic */ Context f$1;
                        private final /* synthetic */ CountryDetectorService f$10;
                        private final /* synthetic */ NetworkTimeUpdateService f$11;
                        private final /* synthetic */ InputManagerService f$12;
                        private final /* synthetic */ TelephonyRegistry f$13;
                        private final /* synthetic */ MediaRouterService f$14;
                        private final /* synthetic */ OppomodemService f$15;
                        private final /* synthetic */ MmsServiceBroker f$16;
                        private final /* synthetic */ WindowManagerService f$2;
                        private final /* synthetic */ boolean f$3;
                        private final /* synthetic */ ConnectivityService f$4;
                        private final /* synthetic */ NetworkManagementService f$5;
                        private final /* synthetic */ NetworkPolicyManagerService f$6;
                        private final /* synthetic */ IpSecService f$7;
                        private final /* synthetic */ NetworkStatsService f$8;
                        private final /* synthetic */ LocationManagerService f$9;

                        {
                            this.f$1 = r4;
                            this.f$2 = r5;
                            this.f$3 = r6;
                            this.f$4 = r7;
                            this.f$5 = r8;
                            this.f$6 = r9;
                            this.f$7 = r10;
                            this.f$8 = r11;
                            this.f$9 = r12;
                            this.f$10 = r13;
                            this.f$11 = r14;
                            this.f$12 = r15;
                            this.f$13 = r16;
                            this.f$14 = r17;
                            this.f$15 = r18;
                            this.f$16 = r19;
                        }

                        public final void run() {
                            SystemServer.this.lambda$startOtherServices$4$SystemServer(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15, this.f$16);
                        }
                    }, BOOT_TIMINGS_TRACE_LOG);
                }
                traceEnd();
            } else {
                iBinder = null;
            }
            startContentCaptureService(context);
            startAttentionService(context);
            startSystemCaptionsManagerService(context);
            traceBeginAndSlog("StartAppPredictionService");
            this.mSystemServiceManager.startService(APP_PREDICTION_MANAGER_SERVICE_CLASS);
            traceEnd();
            traceBeginAndSlog("StartContentSuggestionsService");
            this.mSystemServiceManager.startService(CONTENT_SUGGESTIONS_SERVICE_CLASS);
            traceEnd();
            traceBeginAndSlog("InitNetworkStackClient");
            try {
                NetworkStackClient.getInstance().init();
            } catch (Throwable e35) {
                reportWtf("initializing NetworkStackClient", e35);
            }
            traceEnd();
            traceBeginAndSlog("StartNetworkManagementService");
            try {
                create = NetworkManagementService.create(context);
                ServiceManager.addService("network_management", (IBinder) create);
                networkManagement2 = create;
            } catch (Throwable th14) {
                e13 = th14;
                networkManagement3 = networkManagement;
                reportWtf("starting NetworkManagement Service", e13);
                networkManagement2 = networkManagement3;
                traceEnd();
                traceBeginAndSlog("StartIpSecService");
                iBinder9 = IpSecService.create(context);
                ServiceManager.addService(INetd.IPSEC_INTERFACE_PREFIX, iBinder9);
                traceEnd();
                traceBeginAndSlog("StartTextServicesManager");
                this.mSystemServiceManager.startService(TextServicesManagerService.Lifecycle.class);
                traceEnd();
                if (!disableSystemTextClassifier) {
                }
                traceBeginAndSlog("StartNetworkScoreService");
                this.mSystemServiceManager.startService(NetworkScoreService.Lifecycle.class);
                traceEnd();
                traceBeginAndSlog("StartNetworkStatsService");
                iBinder10 = NetworkStatsService.create(context, networkManagement2);
                ServiceManager.addService("netstats", iBinder10);
                r15 = iBinder10;
                traceEnd();
                traceBeginAndSlog("StartNetworkPolicyManagerService");
                networkPolicyManagerService = new NetworkPolicyManagerService(context, this.mActivityManagerService, networkManagement2);
                ServiceManager.addService("netpolicy", (IBinder) networkPolicyManagerService);
                networkPolicy2 = networkPolicyManagerService;
                traceEnd();
                iBinder2 = null;
                if (context.getPackageManager().hasSystemFeature("android.hardware.wifi")) {
                }
                if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.rtt")) {
                }
                if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.aware")) {
                }
                if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.direct")) {
                }
                if (context.getPackageManager().hasSystemFeature("android.hardware.lowpan")) {
                }
                if (!this.mPackageManager.hasSystemFeature("android.hardware.ethernet")) {
                }
                traceBeginAndSlog("StartEthernet");
                oppomodemService2 = null;
                this.mSystemServiceManager.startService(ETHERNET_SERVICE_CLASS);
                traceEnd();
                traceBeginAndSlog("StartConnectivityService");
                iConnectivityManager2 = (ConnectivityService) sMtkSystemServerIns.getMtkConnectivityService(networkManagement2, r15, networkPolicy2);
                iConnectivityManager5 = iConnectivityManager2;
                if (iConnectivityManager2 == null) {
                }
                networkManagement = networkManagement2;
                ServiceManager.addService("connectivity", iConnectivityManager5, false, 6);
                r15.bindConnectivityManager(iConnectivityManager5);
                networkPolicy2.bindConnectivityManager(iConnectivityManager5);
                iConnectivityManager3 = iConnectivityManager5;
                traceEnd();
                traceBeginAndSlog("StartNsdService");
                iBinder8 = NsdService.create(context);
                ServiceManager.addService("servicediscovery", iBinder8);
                iBinder3 = iBinder8;
                traceEnd();
                traceBeginAndSlog("StartSystemUpdateManagerService");
                ServiceManager.addService("system_update", (IBinder) new SystemUpdateManagerService(context));
                traceEnd();
                traceBeginAndSlog("StartUpdateLockService");
                ServiceManager.addService("updatelock", (IBinder) new UpdateLockService(context));
                traceEnd();
                if (this.mOppoLightsService != null) {
                }
                traceBeginAndSlog("StartNotificationManager");
                if (!this.mHelper.startColorNotificationManagerService()) {
                }
                SystemNotificationChannels.removeDeprecated(context);
                SystemNotificationChannels.createAll(context);
                notification = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
                traceEnd();
                traceBeginAndSlog("StartDeviceMonitor");
                if (!this.mHelper.startColorDeviceStorageMonitorService()) {
                }
                traceEnd();
                traceBeginAndSlog("StartLocationManagerService");
                locationManagerService = new LocationManagerService(context);
                ServiceManager.addService("location", (IBinder) locationManagerService);
                notification2 = notification;
                location2 = locationManagerService;
                traceEnd();
                traceBeginAndSlog("StartCountryDetectorService");
                countryDetectorService = new CountryDetectorService(context);
                ServiceManager.addService("country_detector", (IBinder) countryDetectorService);
                countryDetector2 = countryDetectorService;
                traceEnd();
                traceBeginAndSlog("StartTimeDetectorService");
                this.mSystemServiceManager.startService(TIME_DETECTOR_SERVICE_CLASS);
                traceEnd();
                if (!isWatch) {
                }
                AgingCriticalEvent instance2222 = AgingCriticalEvent.getInstance();
                networkPolicy = networkPolicy2;
                StringBuilder sb2222 = new StringBuilder();
                iConnectivityManager = iConnectivityManager3;
                sb2222.append("systemserver pid:");
                sb2222.append(Process.myPid());
                instance2222.writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, sb2222.toString());
                if (context.getResources().getBoolean(17891451)) {
                }
                traceBeginAndSlog("StartAudioService");
                if (isArc) {
                }
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.hardware.broadcastradio")) {
                }
                traceBeginAndSlog("StartDockObserver");
                Slog.i(TAG, "DockObserver");
                this.mSystemServiceManager.startService(DockObserver.class);
                traceEnd();
                if (isWatch) {
                }
                traceBeginAndSlog("StartWiredAccessoryManager");
                inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.midi")) {
                }
                traceBeginAndSlog("StartAdbService");
                this.mSystemServiceManager.startService(ADB_SERVICE_CLASS);
                traceEnd();
                traceBeginAndSlog("StartUsbService");
                this.mSystemServiceManager.startService(USB_SERVICE_CLASS);
                traceEnd();
                if (!isWatch) {
                }
                traceBeginAndSlog("StartHardwarePropertiesManagerService");
                iBinder6 = new HardwarePropertiesManagerService(context);
                ServiceManager.addService("hardware_properties", iBinder6);
                iBinder4 = iBinder6;
                traceEnd();
                traceBeginAndSlog("StartTwilightService");
                this.mSystemServiceManager.startService(TwilightService.class);
                traceEnd();
                traceBeginAndSlog("StartColorDisplay");
                this.mSystemServiceManager.startService(ColorDisplayService.class);
                traceEnd();
                traceBeginAndSlog("StartJobScheduler");
                if (!this.mHelper.startColorJobSchedulerService()) {
                }
                traceEnd();
                traceBeginAndSlog("StartSoundTrigger");
                this.mSystemServiceManager.startService(SoundTriggerService.class);
                traceEnd();
                traceBeginAndSlog("StartTrustManager");
                this.mSystemServiceManager.startService(TrustManagerService.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                }
                traceBeginAndSlog("StartAppWidgetService");
                this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                traceEnd();
                traceBeginAndSlog("StartRoleManagerService");
                SystemServiceManager systemServiceManager22222 = this.mSystemServiceManager;
                Context context222222 = this.mSystemContext;
                systemServiceManager22222.startService(new RoleManagerService(context222222, new LegacyRoleResolutionPolicy(context222222)));
                traceEnd();
                traceBeginAndSlog("StartVoiceRecognitionManager");
                this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                traceEnd();
                if (GestureLauncherService.isGestureLauncherEnabled(context.getResources())) {
                }
                traceBeginAndSlog("StartSensorNotification");
                this.mSystemServiceManager.startService(SensorNotificationService.class);
                traceEnd();
                traceBeginAndSlog("StartContextHubSystemService");
                this.mSystemServiceManager.startService(ContextHubSystemService.class);
                traceEnd();
                traceBeginAndSlog("StartDiskStatsService");
                ServiceManager.addService("diskstats", new DiskStatsService(context));
                traceEnd();
                Slog.i(TAG, "Oppo modem nimidump Service");
                oppomodemService5 = new OppomodemService(context);
                ServiceManager.addService("modem_crash_up", (IBinder) oppomodemService5);
                oppomodemService3 = oppomodemService5;
                traceBeginAndSlog("CabcService");
                Slog.i(TAG, "Cabc Service");
                iBinder5 = new CabcService(context);
                ServiceManager.addService("cabc", iBinder5);
                Trace.traceEnd(524288);
                traceBeginAndSlog("RuntimeService");
                ServiceManager.addService("runtime", new RuntimeService(context));
                traceEnd();
                Slog.i(TAG, "Torch Service");
                TorchManagerService.getInstance(context).systemReady();
                traceBeginAndSlog("StartHypnusService");
                Slog.i(TAG, "Hypnus Service");
                this.hypnusService = new HypnusService(context);
                ServiceManager.addService("hypnus", (IBinder) this.hypnusService);
                traceEnd();
                Slog.i(TAG, "Oppo Customize Service");
                this.oppoCustomize = new OppoCustomizeService(context);
                ServiceManager.addService("oppocustomize", (IBinder) this.oppoCustomize);
                if (this.mOnlyCore && context.getResources().getBoolean(17891450)) {
                }
                if (isWatch) {
                }
                traceBeginAndSlog("CertBlacklister");
                new CertBlacklister(context);
                traceEnd();
                traceBeginAndSlog("StartEmergencyAffordanceService");
                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                traceEnd();
                traceBeginAndSlog("StartDreamManager");
                this.mSystemServiceManager.startService(DreamManagerService.class);
                traceEnd();
                traceBeginAndSlog("AddGraphicsStatsService");
                ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, (IBinder) new GraphicsStatsService(context));
                traceEnd();
                if (CoverageService.ENABLED) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.companion_device_setup")) {
                }
                traceBeginAndSlog("StartRestrictionManager");
                this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                traceEnd();
                traceBeginAndSlog("StartMediaSessionService");
                Slog.i(TAG, "Media Session Service");
                this.mSystemServiceManager.startService(MediaSessionService.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                }
                traceBeginAndSlog("StartTvInputManager");
                this.mSystemServiceManager.startService(TvInputManagerService.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                }
                traceBeginAndSlog("StartMediaRouterService");
                mediaRouterService = new MediaRouterService(context);
                ServiceManager.addService("media_router", (IBinder) mediaRouterService);
                mediaRouter2 = mediaRouterService;
                traceEnd();
                hasFeatureFace = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.face");
                hasFeatureIris = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.iris");
                hasFeatureFingerprint = this.mPackageManager.hasSystemFeature("android.hardware.fingerprint");
                if (hasFeatureFace) {
                }
                if (hasFeatureIris) {
                }
                if (hasFeatureFingerprint) {
                }
                traceBeginAndSlog("StartBiometricService");
                Slog.i(TAG, "BiometricService Service");
                this.mBiometricService = (BiometricService) this.mSystemServiceManager.startService(BiometricService.class);
                this.mAlipayService = (AlipayService) this.mSystemServiceManager.startService(AlipayService.class);
                traceEnd();
                traceBeginAndSlog("StartBackgroundDexOptService");
                BackgroundDexOptService.schedule(context);
                traceEnd();
                if (OppoOperatorManager.SERVICE_ENABLED) {
                }
                if (!isWatch) {
                }
                Slog.i(TAG, "Oppo Engineer Service");
                this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                if (!isWatch) {
                }
                traceBeginAndSlog("StartShortcutServiceLifecycle");
                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                traceEnd();
                traceBeginAndSlog("StartLauncherAppsService");
                this.mSystemServiceManager.startService(LauncherAppsService.class);
                traceEnd();
                traceBeginAndSlog("StartCrossProfileAppsService");
                this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                traceEnd();
                location = location2;
                r19 = iBinder9;
                countryDetector = countryDetector2;
                networkTimeUpdater = networkTimeUpdater3;
                mediaRouter = mediaRouter2;
                networkStats = r15;
                if (!isWatch) {
                }
                if (isWatch) {
                }
                if (!disableSlices) {
                }
                if (!disableCameraService) {
                }
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                }
                traceBeginAndSlog("StartStatsCompanionService");
                this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                traceEnd();
                traceBeginAndSlog("StartIncidentCompanionService");
                this.mSystemServiceManager.startService(IncidentCompanionService.class);
                traceEnd();
                if (safeMode) {
                }
                traceBeginAndSlog("StartMmsService");
                MmsServiceBroker mmsService2222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                }
                traceBeginAndSlog("StartClipboardService");
                this.mSystemServiceManager.startService(ClipboardService.class);
                traceEnd();
                traceBeginAndSlog("AppServiceManager");
                this.mSystemServiceManager.startService(AppBindingService.Lifecycle.class);
                traceEnd();
                sMtkSystemServerIns.startMtkOtherServices();
                this.mHelper.startOtherServices();
                traceBeginAndSlog("MakeVibratorServiceReady");
                vibrator.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeLockSettingsServiceReady");
                if (lockSettings != null) {
                }
                traceEnd();
                traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                traceEnd();
                traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                this.mSystemServiceManager.startBootPhase(500);
                traceEnd();
                traceBeginAndSlog("MakeWindowManagerServiceReady");
                Slog.i(TAG, "wms systemReady");
                wm.systemReady();
                traceEnd();
                if (safeMode) {
                }
                Configuration config2222222222222222 = wm.computeNewConfiguration(0);
                DisplayMetrics metrics2222222222222222 = new DisplayMetrics();
                ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics2222222222222222);
                context.getResources().updateConfiguration(config2222222222222222, metrics2222222222222222);
                systemTheme = context.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                }
                traceBeginAndSlog("MakePowerManagerServiceReady");
                Slog.i(TAG, "power manager systemReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                traceEnd();
                if (this.mFingerprintService != null) {
                }
                if (this.mFaceService != null) {
                }
                if (this.mBiometricService != null) {
                }
                if (this.mAlipayService != null) {
                }
                traceBeginAndSlog("StartPermissionPolicyService");
                this.mSystemServiceManager.startService(PermissionPolicyService.class);
                traceEnd();
                if (this.mOppoOperatorManagerService != null) {
                }
                traceBeginAndSlog("MakePackageManagerServiceReady");
                Slog.i(TAG, "Package systemReady");
                this.mPackageManagerService.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeOppoCustomizeServiceReady");
                if (this.oppoCustomize != null) {
                }
                traceEnd();
                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                Slog.i(TAG, "DisplayManager systemReady");
                this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                traceEnd();
                this.mSystemServiceManager.setSafeMode(safeMode);
                traceBeginAndSlog("StartDeviceSpecificServices");
                String[] classes2222222222222222 = this.mSystemContext.getResources().getStringArray(17236011);
                while (r6 < r7) {
                }
                traceEnd();
                traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                traceEnd();
                this.mHelper.systemReady();
                Slog.i(TAG, "Ams systemReady");
                this.mActivityManagerService.systemReady(new Runnable(context, wm, safeMode, iConnectivityManager, networkManagement, networkPolicy, r19, networkStats, location, countryDetector, networkTimeUpdater, inputManager, telephonyRegistry, mediaRouter, oppomodemService, mmsService2222222222222222) {
                    /* class com.android.server.$$Lambda$SystemServer$Veuf5e15txqWe0TdLPFg29kfanQ */
                    private final /* synthetic */ Context f$1;
                    private final /* synthetic */ CountryDetectorService f$10;
                    private final /* synthetic */ NetworkTimeUpdateService f$11;
                    private final /* synthetic */ InputManagerService f$12;
                    private final /* synthetic */ TelephonyRegistry f$13;
                    private final /* synthetic */ MediaRouterService f$14;
                    private final /* synthetic */ OppomodemService f$15;
                    private final /* synthetic */ MmsServiceBroker f$16;
                    private final /* synthetic */ WindowManagerService f$2;
                    private final /* synthetic */ boolean f$3;
                    private final /* synthetic */ ConnectivityService f$4;
                    private final /* synthetic */ NetworkManagementService f$5;
                    private final /* synthetic */ NetworkPolicyManagerService f$6;
                    private final /* synthetic */ IpSecService f$7;
                    private final /* synthetic */ NetworkStatsService f$8;
                    private final /* synthetic */ LocationManagerService f$9;

                    {
                        this.f$1 = r4;
                        this.f$2 = r5;
                        this.f$3 = r6;
                        this.f$4 = r7;
                        this.f$5 = r8;
                        this.f$6 = r9;
                        this.f$7 = r10;
                        this.f$8 = r11;
                        this.f$9 = r12;
                        this.f$10 = r13;
                        this.f$11 = r14;
                        this.f$12 = r15;
                        this.f$13 = r16;
                        this.f$14 = r17;
                        this.f$15 = r18;
                        this.f$16 = r19;
                    }

                    public final void run() {
                        SystemServer.this.lambda$startOtherServices$4$SystemServer(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15, this.f$16);
                    }
                }, BOOT_TIMINGS_TRACE_LOG);
            }
            traceEnd();
            traceBeginAndSlog("StartIpSecService");
            try {
                iBinder9 = IpSecService.create(context);
                ServiceManager.addService(INetd.IPSEC_INTERFACE_PREFIX, iBinder9);
            } catch (Throwable e36) {
                reportWtf("starting IpSec Service", e36);
            }
            traceEnd();
            traceBeginAndSlog("StartTextServicesManager");
            this.mSystemServiceManager.startService(TextServicesManagerService.Lifecycle.class);
            traceEnd();
            if (!disableSystemTextClassifier) {
                traceBeginAndSlog("StartTextClassificationManagerService");
                this.mSystemServiceManager.startService(TextClassificationManagerService.Lifecycle.class);
                traceEnd();
            }
            traceBeginAndSlog("StartNetworkScoreService");
            this.mSystemServiceManager.startService(NetworkScoreService.Lifecycle.class);
            traceEnd();
            traceBeginAndSlog("StartNetworkStatsService");
            try {
                iBinder10 = NetworkStatsService.create(context, networkManagement2);
                ServiceManager.addService("netstats", iBinder10);
                r15 = iBinder10;
            } catch (Throwable e37) {
                reportWtf("starting NetworkStats Service", e37);
                r15 = iBinder10;
            }
            traceEnd();
            traceBeginAndSlog("StartNetworkPolicyManagerService");
            try {
                networkPolicyManagerService = new NetworkPolicyManagerService(context, this.mActivityManagerService, networkManagement2);
                ServiceManager.addService("netpolicy", (IBinder) networkPolicyManagerService);
                networkPolicy2 = networkPolicyManagerService;
            } catch (Throwable th15) {
                e12 = th15;
                networkPolicy3 = null;
                reportWtf("starting NetworkPolicy Service", e12);
                networkPolicy2 = networkPolicy3;
                traceEnd();
                iBinder2 = null;
                if (context.getPackageManager().hasSystemFeature("android.hardware.wifi")) {
                }
                if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.rtt")) {
                }
                if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.aware")) {
                }
                if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.direct")) {
                }
                if (context.getPackageManager().hasSystemFeature("android.hardware.lowpan")) {
                }
                if (!this.mPackageManager.hasSystemFeature("android.hardware.ethernet")) {
                }
                traceBeginAndSlog("StartEthernet");
                oppomodemService2 = null;
                this.mSystemServiceManager.startService(ETHERNET_SERVICE_CLASS);
                traceEnd();
                traceBeginAndSlog("StartConnectivityService");
                iConnectivityManager2 = (ConnectivityService) sMtkSystemServerIns.getMtkConnectivityService(networkManagement2, r15, networkPolicy2);
                iConnectivityManager5 = iConnectivityManager2;
                if (iConnectivityManager2 == null) {
                }
                networkManagement = networkManagement2;
                ServiceManager.addService("connectivity", iConnectivityManager5, false, 6);
                r15.bindConnectivityManager(iConnectivityManager5);
                networkPolicy2.bindConnectivityManager(iConnectivityManager5);
                iConnectivityManager3 = iConnectivityManager5;
                traceEnd();
                traceBeginAndSlog("StartNsdService");
                iBinder8 = NsdService.create(context);
                ServiceManager.addService("servicediscovery", iBinder8);
                iBinder3 = iBinder8;
                traceEnd();
                traceBeginAndSlog("StartSystemUpdateManagerService");
                ServiceManager.addService("system_update", (IBinder) new SystemUpdateManagerService(context));
                traceEnd();
                traceBeginAndSlog("StartUpdateLockService");
                ServiceManager.addService("updatelock", (IBinder) new UpdateLockService(context));
                traceEnd();
                if (this.mOppoLightsService != null) {
                }
                traceBeginAndSlog("StartNotificationManager");
                if (!this.mHelper.startColorNotificationManagerService()) {
                }
                SystemNotificationChannels.removeDeprecated(context);
                SystemNotificationChannels.createAll(context);
                notification = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
                traceEnd();
                traceBeginAndSlog("StartDeviceMonitor");
                if (!this.mHelper.startColorDeviceStorageMonitorService()) {
                }
                traceEnd();
                traceBeginAndSlog("StartLocationManagerService");
                locationManagerService = new LocationManagerService(context);
                ServiceManager.addService("location", (IBinder) locationManagerService);
                notification2 = notification;
                location2 = locationManagerService;
                traceEnd();
                traceBeginAndSlog("StartCountryDetectorService");
                countryDetectorService = new CountryDetectorService(context);
                ServiceManager.addService("country_detector", (IBinder) countryDetectorService);
                countryDetector2 = countryDetectorService;
                traceEnd();
                traceBeginAndSlog("StartTimeDetectorService");
                this.mSystemServiceManager.startService(TIME_DETECTOR_SERVICE_CLASS);
                traceEnd();
                if (!isWatch) {
                }
                AgingCriticalEvent instance22222 = AgingCriticalEvent.getInstance();
                networkPolicy = networkPolicy2;
                StringBuilder sb22222 = new StringBuilder();
                iConnectivityManager = iConnectivityManager3;
                sb22222.append("systemserver pid:");
                sb22222.append(Process.myPid());
                instance22222.writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, sb22222.toString());
                if (context.getResources().getBoolean(17891451)) {
                }
                traceBeginAndSlog("StartAudioService");
                if (isArc) {
                }
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.hardware.broadcastradio")) {
                }
                traceBeginAndSlog("StartDockObserver");
                Slog.i(TAG, "DockObserver");
                this.mSystemServiceManager.startService(DockObserver.class);
                traceEnd();
                if (isWatch) {
                }
                traceBeginAndSlog("StartWiredAccessoryManager");
                inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.midi")) {
                }
                traceBeginAndSlog("StartAdbService");
                this.mSystemServiceManager.startService(ADB_SERVICE_CLASS);
                traceEnd();
                traceBeginAndSlog("StartUsbService");
                this.mSystemServiceManager.startService(USB_SERVICE_CLASS);
                traceEnd();
                if (!isWatch) {
                }
                traceBeginAndSlog("StartHardwarePropertiesManagerService");
                iBinder6 = new HardwarePropertiesManagerService(context);
                ServiceManager.addService("hardware_properties", iBinder6);
                iBinder4 = iBinder6;
                traceEnd();
                traceBeginAndSlog("StartTwilightService");
                this.mSystemServiceManager.startService(TwilightService.class);
                traceEnd();
                traceBeginAndSlog("StartColorDisplay");
                this.mSystemServiceManager.startService(ColorDisplayService.class);
                traceEnd();
                traceBeginAndSlog("StartJobScheduler");
                if (!this.mHelper.startColorJobSchedulerService()) {
                }
                traceEnd();
                traceBeginAndSlog("StartSoundTrigger");
                this.mSystemServiceManager.startService(SoundTriggerService.class);
                traceEnd();
                traceBeginAndSlog("StartTrustManager");
                this.mSystemServiceManager.startService(TrustManagerService.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                }
                traceBeginAndSlog("StartAppWidgetService");
                this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                traceEnd();
                traceBeginAndSlog("StartRoleManagerService");
                SystemServiceManager systemServiceManager222222 = this.mSystemServiceManager;
                Context context2222222 = this.mSystemContext;
                systemServiceManager222222.startService(new RoleManagerService(context2222222, new LegacyRoleResolutionPolicy(context2222222)));
                traceEnd();
                traceBeginAndSlog("StartVoiceRecognitionManager");
                this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                traceEnd();
                if (GestureLauncherService.isGestureLauncherEnabled(context.getResources())) {
                }
                traceBeginAndSlog("StartSensorNotification");
                this.mSystemServiceManager.startService(SensorNotificationService.class);
                traceEnd();
                traceBeginAndSlog("StartContextHubSystemService");
                this.mSystemServiceManager.startService(ContextHubSystemService.class);
                traceEnd();
                traceBeginAndSlog("StartDiskStatsService");
                ServiceManager.addService("diskstats", new DiskStatsService(context));
                traceEnd();
                Slog.i(TAG, "Oppo modem nimidump Service");
                oppomodemService5 = new OppomodemService(context);
                ServiceManager.addService("modem_crash_up", (IBinder) oppomodemService5);
                oppomodemService3 = oppomodemService5;
                traceBeginAndSlog("CabcService");
                Slog.i(TAG, "Cabc Service");
                iBinder5 = new CabcService(context);
                ServiceManager.addService("cabc", iBinder5);
                Trace.traceEnd(524288);
                traceBeginAndSlog("RuntimeService");
                ServiceManager.addService("runtime", new RuntimeService(context));
                traceEnd();
                Slog.i(TAG, "Torch Service");
                TorchManagerService.getInstance(context).systemReady();
                traceBeginAndSlog("StartHypnusService");
                Slog.i(TAG, "Hypnus Service");
                this.hypnusService = new HypnusService(context);
                ServiceManager.addService("hypnus", (IBinder) this.hypnusService);
                traceEnd();
                Slog.i(TAG, "Oppo Customize Service");
                this.oppoCustomize = new OppoCustomizeService(context);
                ServiceManager.addService("oppocustomize", (IBinder) this.oppoCustomize);
                if (this.mOnlyCore && context.getResources().getBoolean(17891450)) {
                }
                if (isWatch) {
                }
                traceBeginAndSlog("CertBlacklister");
                new CertBlacklister(context);
                traceEnd();
                traceBeginAndSlog("StartEmergencyAffordanceService");
                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                traceEnd();
                traceBeginAndSlog("StartDreamManager");
                this.mSystemServiceManager.startService(DreamManagerService.class);
                traceEnd();
                traceBeginAndSlog("AddGraphicsStatsService");
                ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, (IBinder) new GraphicsStatsService(context));
                traceEnd();
                if (CoverageService.ENABLED) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.companion_device_setup")) {
                }
                traceBeginAndSlog("StartRestrictionManager");
                this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                traceEnd();
                traceBeginAndSlog("StartMediaSessionService");
                Slog.i(TAG, "Media Session Service");
                this.mSystemServiceManager.startService(MediaSessionService.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                }
                traceBeginAndSlog("StartTvInputManager");
                this.mSystemServiceManager.startService(TvInputManagerService.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                }
                traceBeginAndSlog("StartMediaRouterService");
                mediaRouterService = new MediaRouterService(context);
                ServiceManager.addService("media_router", (IBinder) mediaRouterService);
                mediaRouter2 = mediaRouterService;
                traceEnd();
                hasFeatureFace = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.face");
                hasFeatureIris = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.iris");
                hasFeatureFingerprint = this.mPackageManager.hasSystemFeature("android.hardware.fingerprint");
                if (hasFeatureFace) {
                }
                if (hasFeatureIris) {
                }
                if (hasFeatureFingerprint) {
                }
                traceBeginAndSlog("StartBiometricService");
                Slog.i(TAG, "BiometricService Service");
                this.mBiometricService = (BiometricService) this.mSystemServiceManager.startService(BiometricService.class);
                this.mAlipayService = (AlipayService) this.mSystemServiceManager.startService(AlipayService.class);
                traceEnd();
                traceBeginAndSlog("StartBackgroundDexOptService");
                BackgroundDexOptService.schedule(context);
                traceEnd();
                if (OppoOperatorManager.SERVICE_ENABLED) {
                }
                if (!isWatch) {
                }
                Slog.i(TAG, "Oppo Engineer Service");
                this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                if (!isWatch) {
                }
                traceBeginAndSlog("StartShortcutServiceLifecycle");
                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                traceEnd();
                traceBeginAndSlog("StartLauncherAppsService");
                this.mSystemServiceManager.startService(LauncherAppsService.class);
                traceEnd();
                traceBeginAndSlog("StartCrossProfileAppsService");
                this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                traceEnd();
                location = location2;
                r19 = iBinder9;
                countryDetector = countryDetector2;
                networkTimeUpdater = networkTimeUpdater3;
                mediaRouter = mediaRouter2;
                networkStats = r15;
                if (!isWatch) {
                }
                if (isWatch) {
                }
                if (!disableSlices) {
                }
                if (!disableCameraService) {
                }
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                }
                traceBeginAndSlog("StartStatsCompanionService");
                this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                traceEnd();
                traceBeginAndSlog("StartIncidentCompanionService");
                this.mSystemServiceManager.startService(IncidentCompanionService.class);
                traceEnd();
                if (safeMode) {
                }
                traceBeginAndSlog("StartMmsService");
                MmsServiceBroker mmsService22222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                }
                traceBeginAndSlog("StartClipboardService");
                this.mSystemServiceManager.startService(ClipboardService.class);
                traceEnd();
                traceBeginAndSlog("AppServiceManager");
                this.mSystemServiceManager.startService(AppBindingService.Lifecycle.class);
                traceEnd();
                sMtkSystemServerIns.startMtkOtherServices();
                this.mHelper.startOtherServices();
                traceBeginAndSlog("MakeVibratorServiceReady");
                vibrator.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeLockSettingsServiceReady");
                if (lockSettings != null) {
                }
                traceEnd();
                traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                traceEnd();
                traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                this.mSystemServiceManager.startBootPhase(500);
                traceEnd();
                traceBeginAndSlog("MakeWindowManagerServiceReady");
                Slog.i(TAG, "wms systemReady");
                wm.systemReady();
                traceEnd();
                if (safeMode) {
                }
                Configuration config22222222222222222 = wm.computeNewConfiguration(0);
                DisplayMetrics metrics22222222222222222 = new DisplayMetrics();
                ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics22222222222222222);
                context.getResources().updateConfiguration(config22222222222222222, metrics22222222222222222);
                systemTheme = context.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                }
                traceBeginAndSlog("MakePowerManagerServiceReady");
                Slog.i(TAG, "power manager systemReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                traceEnd();
                if (this.mFingerprintService != null) {
                }
                if (this.mFaceService != null) {
                }
                if (this.mBiometricService != null) {
                }
                if (this.mAlipayService != null) {
                }
                traceBeginAndSlog("StartPermissionPolicyService");
                this.mSystemServiceManager.startService(PermissionPolicyService.class);
                traceEnd();
                if (this.mOppoOperatorManagerService != null) {
                }
                traceBeginAndSlog("MakePackageManagerServiceReady");
                Slog.i(TAG, "Package systemReady");
                this.mPackageManagerService.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeOppoCustomizeServiceReady");
                if (this.oppoCustomize != null) {
                }
                traceEnd();
                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                Slog.i(TAG, "DisplayManager systemReady");
                this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                traceEnd();
                this.mSystemServiceManager.setSafeMode(safeMode);
                traceBeginAndSlog("StartDeviceSpecificServices");
                String[] classes22222222222222222 = this.mSystemContext.getResources().getStringArray(17236011);
                while (r6 < r7) {
                }
                traceEnd();
                traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                traceEnd();
                this.mHelper.systemReady();
                Slog.i(TAG, "Ams systemReady");
                this.mActivityManagerService.systemReady(new Runnable(context, wm, safeMode, iConnectivityManager, networkManagement, networkPolicy, r19, networkStats, location, countryDetector, networkTimeUpdater, inputManager, telephonyRegistry, mediaRouter, oppomodemService, mmsService22222222222222222) {
                    /* class com.android.server.$$Lambda$SystemServer$Veuf5e15txqWe0TdLPFg29kfanQ */
                    private final /* synthetic */ Context f$1;
                    private final /* synthetic */ CountryDetectorService f$10;
                    private final /* synthetic */ NetworkTimeUpdateService f$11;
                    private final /* synthetic */ InputManagerService f$12;
                    private final /* synthetic */ TelephonyRegistry f$13;
                    private final /* synthetic */ MediaRouterService f$14;
                    private final /* synthetic */ OppomodemService f$15;
                    private final /* synthetic */ MmsServiceBroker f$16;
                    private final /* synthetic */ WindowManagerService f$2;
                    private final /* synthetic */ boolean f$3;
                    private final /* synthetic */ ConnectivityService f$4;
                    private final /* synthetic */ NetworkManagementService f$5;
                    private final /* synthetic */ NetworkPolicyManagerService f$6;
                    private final /* synthetic */ IpSecService f$7;
                    private final /* synthetic */ NetworkStatsService f$8;
                    private final /* synthetic */ LocationManagerService f$9;

                    {
                        this.f$1 = r4;
                        this.f$2 = r5;
                        this.f$3 = r6;
                        this.f$4 = r7;
                        this.f$5 = r8;
                        this.f$6 = r9;
                        this.f$7 = r10;
                        this.f$8 = r11;
                        this.f$9 = r12;
                        this.f$10 = r13;
                        this.f$11 = r14;
                        this.f$12 = r15;
                        this.f$13 = r16;
                        this.f$14 = r17;
                        this.f$15 = r18;
                        this.f$16 = r19;
                    }

                    public final void run() {
                        SystemServer.this.lambda$startOtherServices$4$SystemServer(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15, this.f$16);
                    }
                }, BOOT_TIMINGS_TRACE_LOG);
            }
            traceEnd();
            iBinder2 = null;
            if (context.getPackageManager().hasSystemFeature("android.hardware.wifi")) {
                traceBeginAndSlog("StartWifi");
                this.mSystemServiceManager.startService(WIFI_SERVICE_CLASS);
                traceEnd();
                traceBeginAndSlog("StartWifiScanning");
                this.mSystemServiceManager.startService("com.android.server.wifi.scanner.WifiScanningService");
                traceEnd();
            }
            if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.rtt")) {
                traceBeginAndSlog("StartRttService");
                this.mSystemServiceManager.startService("com.android.server.wifi.rtt.RttService");
                traceEnd();
            }
            if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.aware")) {
                traceBeginAndSlog("StartWifiAware");
                this.mSystemServiceManager.startService(WIFI_AWARE_SERVICE_CLASS);
                traceEnd();
            }
            if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.direct")) {
                traceBeginAndSlog("StartWifiP2P");
                this.mSystemServiceManager.startService(WIFI_P2P_SERVICE_CLASS);
                traceEnd();
            }
            if (context.getPackageManager().hasSystemFeature("android.hardware.lowpan")) {
                traceBeginAndSlog("StartLowpan");
                this.mSystemServiceManager.startService(LOWPAN_SERVICE_CLASS);
                traceEnd();
            }
            if (!this.mPackageManager.hasSystemFeature("android.hardware.ethernet") || this.mPackageManager.hasSystemFeature("android.hardware.usb.host")) {
                traceBeginAndSlog("StartEthernet");
                oppomodemService2 = null;
                this.mSystemServiceManager.startService(ETHERNET_SERVICE_CLASS);
                traceEnd();
            } else {
                oppomodemService2 = null;
            }
            traceBeginAndSlog("StartConnectivityService");
            iConnectivityManager2 = (ConnectivityService) sMtkSystemServerIns.getMtkConnectivityService(networkManagement2, r15, networkPolicy2);
            iConnectivityManager5 = iConnectivityManager2;
            if (iConnectivityManager2 == null) {
                try {
                    iConnectivityManager5 = new ConnectivityService(context, networkManagement2, r15, networkPolicy2);
                } catch (Throwable th16) {
                    e11 = th16;
                    networkManagement = networkManagement2;
                    iConnectivityManager4 = iConnectivityManager2;
                    reportWtf("starting Connectivity Service", e11);
                    iConnectivityManager3 = iConnectivityManager4;
                    traceEnd();
                    traceBeginAndSlog("StartNsdService");
                    iBinder8 = NsdService.create(context);
                    ServiceManager.addService("servicediscovery", iBinder8);
                    iBinder3 = iBinder8;
                    traceEnd();
                    traceBeginAndSlog("StartSystemUpdateManagerService");
                    ServiceManager.addService("system_update", (IBinder) new SystemUpdateManagerService(context));
                    traceEnd();
                    traceBeginAndSlog("StartUpdateLockService");
                    ServiceManager.addService("updatelock", (IBinder) new UpdateLockService(context));
                    traceEnd();
                    if (this.mOppoLightsService != null) {
                    }
                    traceBeginAndSlog("StartNotificationManager");
                    if (!this.mHelper.startColorNotificationManagerService()) {
                    }
                    SystemNotificationChannels.removeDeprecated(context);
                    SystemNotificationChannels.createAll(context);
                    notification = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
                    traceEnd();
                    traceBeginAndSlog("StartDeviceMonitor");
                    if (!this.mHelper.startColorDeviceStorageMonitorService()) {
                    }
                    traceEnd();
                    traceBeginAndSlog("StartLocationManagerService");
                    locationManagerService = new LocationManagerService(context);
                    ServiceManager.addService("location", (IBinder) locationManagerService);
                    notification2 = notification;
                    location2 = locationManagerService;
                    traceEnd();
                    traceBeginAndSlog("StartCountryDetectorService");
                    countryDetectorService = new CountryDetectorService(context);
                    ServiceManager.addService("country_detector", (IBinder) countryDetectorService);
                    countryDetector2 = countryDetectorService;
                    traceEnd();
                    traceBeginAndSlog("StartTimeDetectorService");
                    this.mSystemServiceManager.startService(TIME_DETECTOR_SERVICE_CLASS);
                    traceEnd();
                    if (!isWatch) {
                    }
                    AgingCriticalEvent instance222222 = AgingCriticalEvent.getInstance();
                    networkPolicy = networkPolicy2;
                    StringBuilder sb222222 = new StringBuilder();
                    iConnectivityManager = iConnectivityManager3;
                    sb222222.append("systemserver pid:");
                    sb222222.append(Process.myPid());
                    instance222222.writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, sb222222.toString());
                    if (context.getResources().getBoolean(17891451)) {
                    }
                    traceBeginAndSlog("StartAudioService");
                    if (isArc) {
                    }
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.hardware.broadcastradio")) {
                    }
                    traceBeginAndSlog("StartDockObserver");
                    Slog.i(TAG, "DockObserver");
                    this.mSystemServiceManager.startService(DockObserver.class);
                    traceEnd();
                    if (isWatch) {
                    }
                    traceBeginAndSlog("StartWiredAccessoryManager");
                    inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.software.midi")) {
                    }
                    traceBeginAndSlog("StartAdbService");
                    this.mSystemServiceManager.startService(ADB_SERVICE_CLASS);
                    traceEnd();
                    traceBeginAndSlog("StartUsbService");
                    this.mSystemServiceManager.startService(USB_SERVICE_CLASS);
                    traceEnd();
                    if (!isWatch) {
                    }
                    traceBeginAndSlog("StartHardwarePropertiesManagerService");
                    iBinder6 = new HardwarePropertiesManagerService(context);
                    ServiceManager.addService("hardware_properties", iBinder6);
                    iBinder4 = iBinder6;
                    traceEnd();
                    traceBeginAndSlog("StartTwilightService");
                    this.mSystemServiceManager.startService(TwilightService.class);
                    traceEnd();
                    traceBeginAndSlog("StartColorDisplay");
                    this.mSystemServiceManager.startService(ColorDisplayService.class);
                    traceEnd();
                    traceBeginAndSlog("StartJobScheduler");
                    if (!this.mHelper.startColorJobSchedulerService()) {
                    }
                    traceEnd();
                    traceBeginAndSlog("StartSoundTrigger");
                    this.mSystemServiceManager.startService(SoundTriggerService.class);
                    traceEnd();
                    traceBeginAndSlog("StartTrustManager");
                    this.mSystemServiceManager.startService(TrustManagerService.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                    }
                    traceBeginAndSlog("StartAppWidgetService");
                    this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                    traceEnd();
                    traceBeginAndSlog("StartRoleManagerService");
                    SystemServiceManager systemServiceManager2222222 = this.mSystemServiceManager;
                    Context context22222222 = this.mSystemContext;
                    systemServiceManager2222222.startService(new RoleManagerService(context22222222, new LegacyRoleResolutionPolicy(context22222222)));
                    traceEnd();
                    traceBeginAndSlog("StartVoiceRecognitionManager");
                    this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                    traceEnd();
                    if (GestureLauncherService.isGestureLauncherEnabled(context.getResources())) {
                    }
                    traceBeginAndSlog("StartSensorNotification");
                    this.mSystemServiceManager.startService(SensorNotificationService.class);
                    traceEnd();
                    traceBeginAndSlog("StartContextHubSystemService");
                    this.mSystemServiceManager.startService(ContextHubSystemService.class);
                    traceEnd();
                    traceBeginAndSlog("StartDiskStatsService");
                    ServiceManager.addService("diskstats", new DiskStatsService(context));
                    traceEnd();
                    Slog.i(TAG, "Oppo modem nimidump Service");
                    oppomodemService5 = new OppomodemService(context);
                    ServiceManager.addService("modem_crash_up", (IBinder) oppomodemService5);
                    oppomodemService3 = oppomodemService5;
                    traceBeginAndSlog("CabcService");
                    Slog.i(TAG, "Cabc Service");
                    iBinder5 = new CabcService(context);
                    ServiceManager.addService("cabc", iBinder5);
                    Trace.traceEnd(524288);
                    traceBeginAndSlog("RuntimeService");
                    ServiceManager.addService("runtime", new RuntimeService(context));
                    traceEnd();
                    Slog.i(TAG, "Torch Service");
                    TorchManagerService.getInstance(context).systemReady();
                    traceBeginAndSlog("StartHypnusService");
                    Slog.i(TAG, "Hypnus Service");
                    this.hypnusService = new HypnusService(context);
                    ServiceManager.addService("hypnus", (IBinder) this.hypnusService);
                    traceEnd();
                    Slog.i(TAG, "Oppo Customize Service");
                    this.oppoCustomize = new OppoCustomizeService(context);
                    ServiceManager.addService("oppocustomize", (IBinder) this.oppoCustomize);
                    if (this.mOnlyCore && context.getResources().getBoolean(17891450)) {
                    }
                    if (isWatch) {
                    }
                    traceBeginAndSlog("CertBlacklister");
                    new CertBlacklister(context);
                    traceEnd();
                    traceBeginAndSlog("StartEmergencyAffordanceService");
                    this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                    traceEnd();
                    traceBeginAndSlog("StartDreamManager");
                    this.mSystemServiceManager.startService(DreamManagerService.class);
                    traceEnd();
                    traceBeginAndSlog("AddGraphicsStatsService");
                    ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, (IBinder) new GraphicsStatsService(context));
                    traceEnd();
                    if (CoverageService.ENABLED) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.companion_device_setup")) {
                    }
                    traceBeginAndSlog("StartRestrictionManager");
                    this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                    traceEnd();
                    traceBeginAndSlog("StartMediaSessionService");
                    Slog.i(TAG, "Media Session Service");
                    this.mSystemServiceManager.startService(MediaSessionService.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                    }
                    traceBeginAndSlog("StartTvInputManager");
                    this.mSystemServiceManager.startService(TvInputManagerService.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                    }
                    traceBeginAndSlog("StartMediaRouterService");
                    mediaRouterService = new MediaRouterService(context);
                    ServiceManager.addService("media_router", (IBinder) mediaRouterService);
                    mediaRouter2 = mediaRouterService;
                    traceEnd();
                    hasFeatureFace = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.face");
                    hasFeatureIris = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.iris");
                    hasFeatureFingerprint = this.mPackageManager.hasSystemFeature("android.hardware.fingerprint");
                    if (hasFeatureFace) {
                    }
                    if (hasFeatureIris) {
                    }
                    if (hasFeatureFingerprint) {
                    }
                    traceBeginAndSlog("StartBiometricService");
                    Slog.i(TAG, "BiometricService Service");
                    this.mBiometricService = (BiometricService) this.mSystemServiceManager.startService(BiometricService.class);
                    this.mAlipayService = (AlipayService) this.mSystemServiceManager.startService(AlipayService.class);
                    traceEnd();
                    traceBeginAndSlog("StartBackgroundDexOptService");
                    BackgroundDexOptService.schedule(context);
                    traceEnd();
                    if (OppoOperatorManager.SERVICE_ENABLED) {
                    }
                    if (!isWatch) {
                    }
                    Slog.i(TAG, "Oppo Engineer Service");
                    this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                    if (!isWatch) {
                    }
                    traceBeginAndSlog("StartShortcutServiceLifecycle");
                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                    traceEnd();
                    traceBeginAndSlog("StartLauncherAppsService");
                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                    traceEnd();
                    traceBeginAndSlog("StartCrossProfileAppsService");
                    this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                    traceEnd();
                    location = location2;
                    r19 = iBinder9;
                    countryDetector = countryDetector2;
                    networkTimeUpdater = networkTimeUpdater3;
                    mediaRouter = mediaRouter2;
                    networkStats = r15;
                    if (!isWatch) {
                    }
                    if (isWatch) {
                    }
                    if (!disableSlices) {
                    }
                    if (!disableCameraService) {
                    }
                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                    }
                    traceBeginAndSlog("StartStatsCompanionService");
                    this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                    traceEnd();
                    traceBeginAndSlog("StartIncidentCompanionService");
                    this.mSystemServiceManager.startService(IncidentCompanionService.class);
                    traceEnd();
                    if (safeMode) {
                    }
                    traceBeginAndSlog("StartMmsService");
                    MmsServiceBroker mmsService222222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                    }
                    traceBeginAndSlog("StartClipboardService");
                    this.mSystemServiceManager.startService(ClipboardService.class);
                    traceEnd();
                    traceBeginAndSlog("AppServiceManager");
                    this.mSystemServiceManager.startService(AppBindingService.Lifecycle.class);
                    traceEnd();
                    sMtkSystemServerIns.startMtkOtherServices();
                    this.mHelper.startOtherServices();
                    traceBeginAndSlog("MakeVibratorServiceReady");
                    vibrator.systemReady();
                    traceEnd();
                    traceBeginAndSlog("MakeLockSettingsServiceReady");
                    if (lockSettings != null) {
                    }
                    traceEnd();
                    traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                    traceEnd();
                    traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                    this.mSystemServiceManager.startBootPhase(500);
                    traceEnd();
                    traceBeginAndSlog("MakeWindowManagerServiceReady");
                    Slog.i(TAG, "wms systemReady");
                    wm.systemReady();
                    traceEnd();
                    if (safeMode) {
                    }
                    Configuration config222222222222222222 = wm.computeNewConfiguration(0);
                    DisplayMetrics metrics222222222222222222 = new DisplayMetrics();
                    ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics222222222222222222);
                    context.getResources().updateConfiguration(config222222222222222222, metrics222222222222222222);
                    systemTheme = context.getTheme();
                    if (systemTheme.getChangingConfigurations() != 0) {
                    }
                    traceBeginAndSlog("MakePowerManagerServiceReady");
                    Slog.i(TAG, "power manager systemReady");
                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                    traceEnd();
                    if (this.mFingerprintService != null) {
                    }
                    if (this.mFaceService != null) {
                    }
                    if (this.mBiometricService != null) {
                    }
                    if (this.mAlipayService != null) {
                    }
                    traceBeginAndSlog("StartPermissionPolicyService");
                    this.mSystemServiceManager.startService(PermissionPolicyService.class);
                    traceEnd();
                    if (this.mOppoOperatorManagerService != null) {
                    }
                    traceBeginAndSlog("MakePackageManagerServiceReady");
                    Slog.i(TAG, "Package systemReady");
                    this.mPackageManagerService.systemReady();
                    traceEnd();
                    traceBeginAndSlog("MakeOppoCustomizeServiceReady");
                    if (this.oppoCustomize != null) {
                    }
                    traceEnd();
                    traceBeginAndSlog("MakeDisplayManagerServiceReady");
                    Slog.i(TAG, "DisplayManager systemReady");
                    this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                    traceEnd();
                    this.mSystemServiceManager.setSafeMode(safeMode);
                    traceBeginAndSlog("StartDeviceSpecificServices");
                    String[] classes222222222222222222 = this.mSystemContext.getResources().getStringArray(17236011);
                    while (r6 < r7) {
                    }
                    traceEnd();
                    traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                    traceEnd();
                    this.mHelper.systemReady();
                    Slog.i(TAG, "Ams systemReady");
                    this.mActivityManagerService.systemReady(new Runnable(context, wm, safeMode, iConnectivityManager, networkManagement, networkPolicy, r19, networkStats, location, countryDetector, networkTimeUpdater, inputManager, telephonyRegistry, mediaRouter, oppomodemService, mmsService222222222222222222) {
                        /* class com.android.server.$$Lambda$SystemServer$Veuf5e15txqWe0TdLPFg29kfanQ */
                        private final /* synthetic */ Context f$1;
                        private final /* synthetic */ CountryDetectorService f$10;
                        private final /* synthetic */ NetworkTimeUpdateService f$11;
                        private final /* synthetic */ InputManagerService f$12;
                        private final /* synthetic */ TelephonyRegistry f$13;
                        private final /* synthetic */ MediaRouterService f$14;
                        private final /* synthetic */ OppomodemService f$15;
                        private final /* synthetic */ MmsServiceBroker f$16;
                        private final /* synthetic */ WindowManagerService f$2;
                        private final /* synthetic */ boolean f$3;
                        private final /* synthetic */ ConnectivityService f$4;
                        private final /* synthetic */ NetworkManagementService f$5;
                        private final /* synthetic */ NetworkPolicyManagerService f$6;
                        private final /* synthetic */ IpSecService f$7;
                        private final /* synthetic */ NetworkStatsService f$8;
                        private final /* synthetic */ LocationManagerService f$9;

                        {
                            this.f$1 = r4;
                            this.f$2 = r5;
                            this.f$3 = r6;
                            this.f$4 = r7;
                            this.f$5 = r8;
                            this.f$6 = r9;
                            this.f$7 = r10;
                            this.f$8 = r11;
                            this.f$9 = r12;
                            this.f$10 = r13;
                            this.f$11 = r14;
                            this.f$12 = r15;
                            this.f$13 = r16;
                            this.f$14 = r17;
                            this.f$15 = r18;
                            this.f$16 = r19;
                        }

                        public final void run() {
                            SystemServer.this.lambda$startOtherServices$4$SystemServer(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15, this.f$16);
                        }
                    }, BOOT_TIMINGS_TRACE_LOG);
                }
            }
            networkManagement = networkManagement2;
            try {
                ServiceManager.addService("connectivity", iConnectivityManager5, false, 6);
                r15.bindConnectivityManager(iConnectivityManager5);
                networkPolicy2.bindConnectivityManager(iConnectivityManager5);
                iConnectivityManager3 = iConnectivityManager5;
            } catch (Throwable th17) {
                e11 = th17;
                iConnectivityManager4 = iConnectivityManager5;
            }
            traceEnd();
            traceBeginAndSlog("StartNsdService");
            try {
                iBinder8 = NsdService.create(context);
                ServiceManager.addService("servicediscovery", iBinder8);
                iBinder3 = iBinder8;
            } catch (Throwable th18) {
                e10 = th18;
                iBinder8 = null;
                reportWtf("starting Service Discovery Service", e10);
                iBinder3 = iBinder8;
                traceEnd();
                traceBeginAndSlog("StartSystemUpdateManagerService");
                ServiceManager.addService("system_update", (IBinder) new SystemUpdateManagerService(context));
                traceEnd();
                traceBeginAndSlog("StartUpdateLockService");
                ServiceManager.addService("updatelock", (IBinder) new UpdateLockService(context));
                traceEnd();
                if (this.mOppoLightsService != null) {
                }
                traceBeginAndSlog("StartNotificationManager");
                if (!this.mHelper.startColorNotificationManagerService()) {
                }
                SystemNotificationChannels.removeDeprecated(context);
                SystemNotificationChannels.createAll(context);
                notification = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
                traceEnd();
                traceBeginAndSlog("StartDeviceMonitor");
                if (!this.mHelper.startColorDeviceStorageMonitorService()) {
                }
                traceEnd();
                traceBeginAndSlog("StartLocationManagerService");
                locationManagerService = new LocationManagerService(context);
                ServiceManager.addService("location", (IBinder) locationManagerService);
                notification2 = notification;
                location2 = locationManagerService;
                traceEnd();
                traceBeginAndSlog("StartCountryDetectorService");
                countryDetectorService = new CountryDetectorService(context);
                ServiceManager.addService("country_detector", (IBinder) countryDetectorService);
                countryDetector2 = countryDetectorService;
                traceEnd();
                traceBeginAndSlog("StartTimeDetectorService");
                this.mSystemServiceManager.startService(TIME_DETECTOR_SERVICE_CLASS);
                traceEnd();
                if (!isWatch) {
                }
                AgingCriticalEvent instance2222222 = AgingCriticalEvent.getInstance();
                networkPolicy = networkPolicy2;
                StringBuilder sb2222222 = new StringBuilder();
                iConnectivityManager = iConnectivityManager3;
                sb2222222.append("systemserver pid:");
                sb2222222.append(Process.myPid());
                instance2222222.writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, sb2222222.toString());
                if (context.getResources().getBoolean(17891451)) {
                }
                traceBeginAndSlog("StartAudioService");
                if (isArc) {
                }
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.hardware.broadcastradio")) {
                }
                traceBeginAndSlog("StartDockObserver");
                Slog.i(TAG, "DockObserver");
                this.mSystemServiceManager.startService(DockObserver.class);
                traceEnd();
                if (isWatch) {
                }
                traceBeginAndSlog("StartWiredAccessoryManager");
                inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.midi")) {
                }
                traceBeginAndSlog("StartAdbService");
                this.mSystemServiceManager.startService(ADB_SERVICE_CLASS);
                traceEnd();
                traceBeginAndSlog("StartUsbService");
                this.mSystemServiceManager.startService(USB_SERVICE_CLASS);
                traceEnd();
                if (!isWatch) {
                }
                traceBeginAndSlog("StartHardwarePropertiesManagerService");
                iBinder6 = new HardwarePropertiesManagerService(context);
                ServiceManager.addService("hardware_properties", iBinder6);
                iBinder4 = iBinder6;
                traceEnd();
                traceBeginAndSlog("StartTwilightService");
                this.mSystemServiceManager.startService(TwilightService.class);
                traceEnd();
                traceBeginAndSlog("StartColorDisplay");
                this.mSystemServiceManager.startService(ColorDisplayService.class);
                traceEnd();
                traceBeginAndSlog("StartJobScheduler");
                if (!this.mHelper.startColorJobSchedulerService()) {
                }
                traceEnd();
                traceBeginAndSlog("StartSoundTrigger");
                this.mSystemServiceManager.startService(SoundTriggerService.class);
                traceEnd();
                traceBeginAndSlog("StartTrustManager");
                this.mSystemServiceManager.startService(TrustManagerService.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                }
                traceBeginAndSlog("StartAppWidgetService");
                this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                traceEnd();
                traceBeginAndSlog("StartRoleManagerService");
                SystemServiceManager systemServiceManager22222222 = this.mSystemServiceManager;
                Context context222222222 = this.mSystemContext;
                systemServiceManager22222222.startService(new RoleManagerService(context222222222, new LegacyRoleResolutionPolicy(context222222222)));
                traceEnd();
                traceBeginAndSlog("StartVoiceRecognitionManager");
                this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                traceEnd();
                if (GestureLauncherService.isGestureLauncherEnabled(context.getResources())) {
                }
                traceBeginAndSlog("StartSensorNotification");
                this.mSystemServiceManager.startService(SensorNotificationService.class);
                traceEnd();
                traceBeginAndSlog("StartContextHubSystemService");
                this.mSystemServiceManager.startService(ContextHubSystemService.class);
                traceEnd();
                traceBeginAndSlog("StartDiskStatsService");
                ServiceManager.addService("diskstats", new DiskStatsService(context));
                traceEnd();
                Slog.i(TAG, "Oppo modem nimidump Service");
                oppomodemService5 = new OppomodemService(context);
                ServiceManager.addService("modem_crash_up", (IBinder) oppomodemService5);
                oppomodemService3 = oppomodemService5;
                traceBeginAndSlog("CabcService");
                Slog.i(TAG, "Cabc Service");
                iBinder5 = new CabcService(context);
                ServiceManager.addService("cabc", iBinder5);
                Trace.traceEnd(524288);
                traceBeginAndSlog("RuntimeService");
                ServiceManager.addService("runtime", new RuntimeService(context));
                traceEnd();
                Slog.i(TAG, "Torch Service");
                TorchManagerService.getInstance(context).systemReady();
                traceBeginAndSlog("StartHypnusService");
                Slog.i(TAG, "Hypnus Service");
                this.hypnusService = new HypnusService(context);
                ServiceManager.addService("hypnus", (IBinder) this.hypnusService);
                traceEnd();
                Slog.i(TAG, "Oppo Customize Service");
                this.oppoCustomize = new OppoCustomizeService(context);
                ServiceManager.addService("oppocustomize", (IBinder) this.oppoCustomize);
                if (this.mOnlyCore && context.getResources().getBoolean(17891450)) {
                }
                if (isWatch) {
                }
                traceBeginAndSlog("CertBlacklister");
                new CertBlacklister(context);
                traceEnd();
                traceBeginAndSlog("StartEmergencyAffordanceService");
                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                traceEnd();
                traceBeginAndSlog("StartDreamManager");
                this.mSystemServiceManager.startService(DreamManagerService.class);
                traceEnd();
                traceBeginAndSlog("AddGraphicsStatsService");
                ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, (IBinder) new GraphicsStatsService(context));
                traceEnd();
                if (CoverageService.ENABLED) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.companion_device_setup")) {
                }
                traceBeginAndSlog("StartRestrictionManager");
                this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                traceEnd();
                traceBeginAndSlog("StartMediaSessionService");
                Slog.i(TAG, "Media Session Service");
                this.mSystemServiceManager.startService(MediaSessionService.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                }
                traceBeginAndSlog("StartTvInputManager");
                this.mSystemServiceManager.startService(TvInputManagerService.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                }
                traceBeginAndSlog("StartMediaRouterService");
                mediaRouterService = new MediaRouterService(context);
                ServiceManager.addService("media_router", (IBinder) mediaRouterService);
                mediaRouter2 = mediaRouterService;
                traceEnd();
                hasFeatureFace = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.face");
                hasFeatureIris = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.iris");
                hasFeatureFingerprint = this.mPackageManager.hasSystemFeature("android.hardware.fingerprint");
                if (hasFeatureFace) {
                }
                if (hasFeatureIris) {
                }
                if (hasFeatureFingerprint) {
                }
                traceBeginAndSlog("StartBiometricService");
                Slog.i(TAG, "BiometricService Service");
                this.mBiometricService = (BiometricService) this.mSystemServiceManager.startService(BiometricService.class);
                this.mAlipayService = (AlipayService) this.mSystemServiceManager.startService(AlipayService.class);
                traceEnd();
                traceBeginAndSlog("StartBackgroundDexOptService");
                BackgroundDexOptService.schedule(context);
                traceEnd();
                if (OppoOperatorManager.SERVICE_ENABLED) {
                }
                if (!isWatch) {
                }
                Slog.i(TAG, "Oppo Engineer Service");
                this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                if (!isWatch) {
                }
                traceBeginAndSlog("StartShortcutServiceLifecycle");
                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                traceEnd();
                traceBeginAndSlog("StartLauncherAppsService");
                this.mSystemServiceManager.startService(LauncherAppsService.class);
                traceEnd();
                traceBeginAndSlog("StartCrossProfileAppsService");
                this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                traceEnd();
                location = location2;
                r19 = iBinder9;
                countryDetector = countryDetector2;
                networkTimeUpdater = networkTimeUpdater3;
                mediaRouter = mediaRouter2;
                networkStats = r15;
                if (!isWatch) {
                }
                if (isWatch) {
                }
                if (!disableSlices) {
                }
                if (!disableCameraService) {
                }
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                }
                traceBeginAndSlog("StartStatsCompanionService");
                this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                traceEnd();
                traceBeginAndSlog("StartIncidentCompanionService");
                this.mSystemServiceManager.startService(IncidentCompanionService.class);
                traceEnd();
                if (safeMode) {
                }
                traceBeginAndSlog("StartMmsService");
                MmsServiceBroker mmsService2222222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                }
                traceBeginAndSlog("StartClipboardService");
                this.mSystemServiceManager.startService(ClipboardService.class);
                traceEnd();
                traceBeginAndSlog("AppServiceManager");
                this.mSystemServiceManager.startService(AppBindingService.Lifecycle.class);
                traceEnd();
                sMtkSystemServerIns.startMtkOtherServices();
                this.mHelper.startOtherServices();
                traceBeginAndSlog("MakeVibratorServiceReady");
                vibrator.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeLockSettingsServiceReady");
                if (lockSettings != null) {
                }
                traceEnd();
                traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                traceEnd();
                traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                this.mSystemServiceManager.startBootPhase(500);
                traceEnd();
                traceBeginAndSlog("MakeWindowManagerServiceReady");
                Slog.i(TAG, "wms systemReady");
                wm.systemReady();
                traceEnd();
                if (safeMode) {
                }
                Configuration config2222222222222222222 = wm.computeNewConfiguration(0);
                DisplayMetrics metrics2222222222222222222 = new DisplayMetrics();
                ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics2222222222222222222);
                context.getResources().updateConfiguration(config2222222222222222222, metrics2222222222222222222);
                systemTheme = context.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                }
                traceBeginAndSlog("MakePowerManagerServiceReady");
                Slog.i(TAG, "power manager systemReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                traceEnd();
                if (this.mFingerprintService != null) {
                }
                if (this.mFaceService != null) {
                }
                if (this.mBiometricService != null) {
                }
                if (this.mAlipayService != null) {
                }
                traceBeginAndSlog("StartPermissionPolicyService");
                this.mSystemServiceManager.startService(PermissionPolicyService.class);
                traceEnd();
                if (this.mOppoOperatorManagerService != null) {
                }
                traceBeginAndSlog("MakePackageManagerServiceReady");
                Slog.i(TAG, "Package systemReady");
                this.mPackageManagerService.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeOppoCustomizeServiceReady");
                if (this.oppoCustomize != null) {
                }
                traceEnd();
                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                Slog.i(TAG, "DisplayManager systemReady");
                this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                traceEnd();
                this.mSystemServiceManager.setSafeMode(safeMode);
                traceBeginAndSlog("StartDeviceSpecificServices");
                String[] classes2222222222222222222 = this.mSystemContext.getResources().getStringArray(17236011);
                while (r6 < r7) {
                }
                traceEnd();
                traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                traceEnd();
                this.mHelper.systemReady();
                Slog.i(TAG, "Ams systemReady");
                this.mActivityManagerService.systemReady(new Runnable(context, wm, safeMode, iConnectivityManager, networkManagement, networkPolicy, r19, networkStats, location, countryDetector, networkTimeUpdater, inputManager, telephonyRegistry, mediaRouter, oppomodemService, mmsService2222222222222222222) {
                    /* class com.android.server.$$Lambda$SystemServer$Veuf5e15txqWe0TdLPFg29kfanQ */
                    private final /* synthetic */ Context f$1;
                    private final /* synthetic */ CountryDetectorService f$10;
                    private final /* synthetic */ NetworkTimeUpdateService f$11;
                    private final /* synthetic */ InputManagerService f$12;
                    private final /* synthetic */ TelephonyRegistry f$13;
                    private final /* synthetic */ MediaRouterService f$14;
                    private final /* synthetic */ OppomodemService f$15;
                    private final /* synthetic */ MmsServiceBroker f$16;
                    private final /* synthetic */ WindowManagerService f$2;
                    private final /* synthetic */ boolean f$3;
                    private final /* synthetic */ ConnectivityService f$4;
                    private final /* synthetic */ NetworkManagementService f$5;
                    private final /* synthetic */ NetworkPolicyManagerService f$6;
                    private final /* synthetic */ IpSecService f$7;
                    private final /* synthetic */ NetworkStatsService f$8;
                    private final /* synthetic */ LocationManagerService f$9;

                    {
                        this.f$1 = r4;
                        this.f$2 = r5;
                        this.f$3 = r6;
                        this.f$4 = r7;
                        this.f$5 = r8;
                        this.f$6 = r9;
                        this.f$7 = r10;
                        this.f$8 = r11;
                        this.f$9 = r12;
                        this.f$10 = r13;
                        this.f$11 = r14;
                        this.f$12 = r15;
                        this.f$13 = r16;
                        this.f$14 = r17;
                        this.f$15 = r18;
                        this.f$16 = r19;
                    }

                    public final void run() {
                        SystemServer.this.lambda$startOtherServices$4$SystemServer(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15, this.f$16);
                    }
                }, BOOT_TIMINGS_TRACE_LOG);
            }
            traceEnd();
            traceBeginAndSlog("StartSystemUpdateManagerService");
            try {
                ServiceManager.addService("system_update", (IBinder) new SystemUpdateManagerService(context));
            } catch (Throwable e38) {
                reportWtf("starting SystemUpdateManagerService", e38);
            }
            traceEnd();
            traceBeginAndSlog("StartUpdateLockService");
            try {
                ServiceManager.addService("updatelock", (IBinder) new UpdateLockService(context));
            } catch (Throwable e39) {
                reportWtf("starting UpdateLockService", e39);
            }
            traceEnd();
            try {
                if (this.mOppoLightsService != null) {
                    Slog.i(TAG, "OppoLightsService.systemReady");
                    this.mOppoLightsService.systemReady();
                }
            } catch (Throwable e40) {
                reportWtf("making OppoLightsService ready", e40);
            }
            traceBeginAndSlog("StartNotificationManager");
            if (!this.mHelper.startColorNotificationManagerService()) {
                Slog.i(TAG, "NotificationManagerService");
                this.mSystemServiceManager.startService(NotificationManagerService.class);
            }
            SystemNotificationChannels.removeDeprecated(context);
            SystemNotificationChannels.createAll(context);
            notification = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
            traceEnd();
            traceBeginAndSlog("StartDeviceMonitor");
            if (!this.mHelper.startColorDeviceStorageMonitorService()) {
                this.mSystemServiceManager.startService(DeviceStorageMonitorService.class);
            }
            traceEnd();
            traceBeginAndSlog("StartLocationManagerService");
            try {
                locationManagerService = new LocationManagerService(context);
                ServiceManager.addService("location", (IBinder) locationManagerService);
                notification2 = notification;
                location2 = locationManagerService;
            } catch (Throwable th19) {
                e9 = th19;
                location3 = null;
                notification2 = notification;
                reportWtf("starting Location Manager", e9);
                location2 = location3;
                traceEnd();
                traceBeginAndSlog("StartCountryDetectorService");
                countryDetectorService = new CountryDetectorService(context);
                ServiceManager.addService("country_detector", (IBinder) countryDetectorService);
                countryDetector2 = countryDetectorService;
                traceEnd();
                traceBeginAndSlog("StartTimeDetectorService");
                this.mSystemServiceManager.startService(TIME_DETECTOR_SERVICE_CLASS);
                traceEnd();
                if (!isWatch) {
                }
                AgingCriticalEvent instance22222222 = AgingCriticalEvent.getInstance();
                networkPolicy = networkPolicy2;
                StringBuilder sb22222222 = new StringBuilder();
                iConnectivityManager = iConnectivityManager3;
                sb22222222.append("systemserver pid:");
                sb22222222.append(Process.myPid());
                instance22222222.writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, sb22222222.toString());
                if (context.getResources().getBoolean(17891451)) {
                }
                traceBeginAndSlog("StartAudioService");
                if (isArc) {
                }
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.hardware.broadcastradio")) {
                }
                traceBeginAndSlog("StartDockObserver");
                Slog.i(TAG, "DockObserver");
                this.mSystemServiceManager.startService(DockObserver.class);
                traceEnd();
                if (isWatch) {
                }
                traceBeginAndSlog("StartWiredAccessoryManager");
                inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.midi")) {
                }
                traceBeginAndSlog("StartAdbService");
                this.mSystemServiceManager.startService(ADB_SERVICE_CLASS);
                traceEnd();
                traceBeginAndSlog("StartUsbService");
                this.mSystemServiceManager.startService(USB_SERVICE_CLASS);
                traceEnd();
                if (!isWatch) {
                }
                traceBeginAndSlog("StartHardwarePropertiesManagerService");
                iBinder6 = new HardwarePropertiesManagerService(context);
                ServiceManager.addService("hardware_properties", iBinder6);
                iBinder4 = iBinder6;
                traceEnd();
                traceBeginAndSlog("StartTwilightService");
                this.mSystemServiceManager.startService(TwilightService.class);
                traceEnd();
                traceBeginAndSlog("StartColorDisplay");
                this.mSystemServiceManager.startService(ColorDisplayService.class);
                traceEnd();
                traceBeginAndSlog("StartJobScheduler");
                if (!this.mHelper.startColorJobSchedulerService()) {
                }
                traceEnd();
                traceBeginAndSlog("StartSoundTrigger");
                this.mSystemServiceManager.startService(SoundTriggerService.class);
                traceEnd();
                traceBeginAndSlog("StartTrustManager");
                this.mSystemServiceManager.startService(TrustManagerService.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                }
                traceBeginAndSlog("StartAppWidgetService");
                this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                traceEnd();
                traceBeginAndSlog("StartRoleManagerService");
                SystemServiceManager systemServiceManager222222222 = this.mSystemServiceManager;
                Context context2222222222 = this.mSystemContext;
                systemServiceManager222222222.startService(new RoleManagerService(context2222222222, new LegacyRoleResolutionPolicy(context2222222222)));
                traceEnd();
                traceBeginAndSlog("StartVoiceRecognitionManager");
                this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                traceEnd();
                if (GestureLauncherService.isGestureLauncherEnabled(context.getResources())) {
                }
                traceBeginAndSlog("StartSensorNotification");
                this.mSystemServiceManager.startService(SensorNotificationService.class);
                traceEnd();
                traceBeginAndSlog("StartContextHubSystemService");
                this.mSystemServiceManager.startService(ContextHubSystemService.class);
                traceEnd();
                traceBeginAndSlog("StartDiskStatsService");
                ServiceManager.addService("diskstats", new DiskStatsService(context));
                traceEnd();
                Slog.i(TAG, "Oppo modem nimidump Service");
                oppomodemService5 = new OppomodemService(context);
                ServiceManager.addService("modem_crash_up", (IBinder) oppomodemService5);
                oppomodemService3 = oppomodemService5;
                traceBeginAndSlog("CabcService");
                Slog.i(TAG, "Cabc Service");
                iBinder5 = new CabcService(context);
                ServiceManager.addService("cabc", iBinder5);
                Trace.traceEnd(524288);
                traceBeginAndSlog("RuntimeService");
                ServiceManager.addService("runtime", new RuntimeService(context));
                traceEnd();
                Slog.i(TAG, "Torch Service");
                TorchManagerService.getInstance(context).systemReady();
                traceBeginAndSlog("StartHypnusService");
                Slog.i(TAG, "Hypnus Service");
                this.hypnusService = new HypnusService(context);
                ServiceManager.addService("hypnus", (IBinder) this.hypnusService);
                traceEnd();
                Slog.i(TAG, "Oppo Customize Service");
                this.oppoCustomize = new OppoCustomizeService(context);
                ServiceManager.addService("oppocustomize", (IBinder) this.oppoCustomize);
                if (this.mOnlyCore && context.getResources().getBoolean(17891450)) {
                }
                if (isWatch) {
                }
                traceBeginAndSlog("CertBlacklister");
                new CertBlacklister(context);
                traceEnd();
                traceBeginAndSlog("StartEmergencyAffordanceService");
                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                traceEnd();
                traceBeginAndSlog("StartDreamManager");
                this.mSystemServiceManager.startService(DreamManagerService.class);
                traceEnd();
                traceBeginAndSlog("AddGraphicsStatsService");
                ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, (IBinder) new GraphicsStatsService(context));
                traceEnd();
                if (CoverageService.ENABLED) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.companion_device_setup")) {
                }
                traceBeginAndSlog("StartRestrictionManager");
                this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                traceEnd();
                traceBeginAndSlog("StartMediaSessionService");
                Slog.i(TAG, "Media Session Service");
                this.mSystemServiceManager.startService(MediaSessionService.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                }
                traceBeginAndSlog("StartTvInputManager");
                this.mSystemServiceManager.startService(TvInputManagerService.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                }
                traceBeginAndSlog("StartMediaRouterService");
                mediaRouterService = new MediaRouterService(context);
                ServiceManager.addService("media_router", (IBinder) mediaRouterService);
                mediaRouter2 = mediaRouterService;
                traceEnd();
                hasFeatureFace = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.face");
                hasFeatureIris = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.iris");
                hasFeatureFingerprint = this.mPackageManager.hasSystemFeature("android.hardware.fingerprint");
                if (hasFeatureFace) {
                }
                if (hasFeatureIris) {
                }
                if (hasFeatureFingerprint) {
                }
                traceBeginAndSlog("StartBiometricService");
                Slog.i(TAG, "BiometricService Service");
                this.mBiometricService = (BiometricService) this.mSystemServiceManager.startService(BiometricService.class);
                this.mAlipayService = (AlipayService) this.mSystemServiceManager.startService(AlipayService.class);
                traceEnd();
                traceBeginAndSlog("StartBackgroundDexOptService");
                BackgroundDexOptService.schedule(context);
                traceEnd();
                if (OppoOperatorManager.SERVICE_ENABLED) {
                }
                if (!isWatch) {
                }
                Slog.i(TAG, "Oppo Engineer Service");
                this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                if (!isWatch) {
                }
                traceBeginAndSlog("StartShortcutServiceLifecycle");
                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                traceEnd();
                traceBeginAndSlog("StartLauncherAppsService");
                this.mSystemServiceManager.startService(LauncherAppsService.class);
                traceEnd();
                traceBeginAndSlog("StartCrossProfileAppsService");
                this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                traceEnd();
                location = location2;
                r19 = iBinder9;
                countryDetector = countryDetector2;
                networkTimeUpdater = networkTimeUpdater3;
                mediaRouter = mediaRouter2;
                networkStats = r15;
                if (!isWatch) {
                }
                if (isWatch) {
                }
                if (!disableSlices) {
                }
                if (!disableCameraService) {
                }
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                }
                traceBeginAndSlog("StartStatsCompanionService");
                this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                traceEnd();
                traceBeginAndSlog("StartIncidentCompanionService");
                this.mSystemServiceManager.startService(IncidentCompanionService.class);
                traceEnd();
                if (safeMode) {
                }
                traceBeginAndSlog("StartMmsService");
                MmsServiceBroker mmsService22222222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                }
                traceBeginAndSlog("StartClipboardService");
                this.mSystemServiceManager.startService(ClipboardService.class);
                traceEnd();
                traceBeginAndSlog("AppServiceManager");
                this.mSystemServiceManager.startService(AppBindingService.Lifecycle.class);
                traceEnd();
                sMtkSystemServerIns.startMtkOtherServices();
                this.mHelper.startOtherServices();
                traceBeginAndSlog("MakeVibratorServiceReady");
                vibrator.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeLockSettingsServiceReady");
                if (lockSettings != null) {
                }
                traceEnd();
                traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                traceEnd();
                traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                this.mSystemServiceManager.startBootPhase(500);
                traceEnd();
                traceBeginAndSlog("MakeWindowManagerServiceReady");
                Slog.i(TAG, "wms systemReady");
                wm.systemReady();
                traceEnd();
                if (safeMode) {
                }
                Configuration config22222222222222222222 = wm.computeNewConfiguration(0);
                DisplayMetrics metrics22222222222222222222 = new DisplayMetrics();
                ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics22222222222222222222);
                context.getResources().updateConfiguration(config22222222222222222222, metrics22222222222222222222);
                systemTheme = context.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                }
                traceBeginAndSlog("MakePowerManagerServiceReady");
                Slog.i(TAG, "power manager systemReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                traceEnd();
                if (this.mFingerprintService != null) {
                }
                if (this.mFaceService != null) {
                }
                if (this.mBiometricService != null) {
                }
                if (this.mAlipayService != null) {
                }
                traceBeginAndSlog("StartPermissionPolicyService");
                this.mSystemServiceManager.startService(PermissionPolicyService.class);
                traceEnd();
                if (this.mOppoOperatorManagerService != null) {
                }
                traceBeginAndSlog("MakePackageManagerServiceReady");
                Slog.i(TAG, "Package systemReady");
                this.mPackageManagerService.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeOppoCustomizeServiceReady");
                if (this.oppoCustomize != null) {
                }
                traceEnd();
                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                Slog.i(TAG, "DisplayManager systemReady");
                this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                traceEnd();
                this.mSystemServiceManager.setSafeMode(safeMode);
                traceBeginAndSlog("StartDeviceSpecificServices");
                String[] classes22222222222222222222 = this.mSystemContext.getResources().getStringArray(17236011);
                while (r6 < r7) {
                }
                traceEnd();
                traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                traceEnd();
                this.mHelper.systemReady();
                Slog.i(TAG, "Ams systemReady");
                this.mActivityManagerService.systemReady(new Runnable(context, wm, safeMode, iConnectivityManager, networkManagement, networkPolicy, r19, networkStats, location, countryDetector, networkTimeUpdater, inputManager, telephonyRegistry, mediaRouter, oppomodemService, mmsService22222222222222222222) {
                    /* class com.android.server.$$Lambda$SystemServer$Veuf5e15txqWe0TdLPFg29kfanQ */
                    private final /* synthetic */ Context f$1;
                    private final /* synthetic */ CountryDetectorService f$10;
                    private final /* synthetic */ NetworkTimeUpdateService f$11;
                    private final /* synthetic */ InputManagerService f$12;
                    private final /* synthetic */ TelephonyRegistry f$13;
                    private final /* synthetic */ MediaRouterService f$14;
                    private final /* synthetic */ OppomodemService f$15;
                    private final /* synthetic */ MmsServiceBroker f$16;
                    private final /* synthetic */ WindowManagerService f$2;
                    private final /* synthetic */ boolean f$3;
                    private final /* synthetic */ ConnectivityService f$4;
                    private final /* synthetic */ NetworkManagementService f$5;
                    private final /* synthetic */ NetworkPolicyManagerService f$6;
                    private final /* synthetic */ IpSecService f$7;
                    private final /* synthetic */ NetworkStatsService f$8;
                    private final /* synthetic */ LocationManagerService f$9;

                    {
                        this.f$1 = r4;
                        this.f$2 = r5;
                        this.f$3 = r6;
                        this.f$4 = r7;
                        this.f$5 = r8;
                        this.f$6 = r9;
                        this.f$7 = r10;
                        this.f$8 = r11;
                        this.f$9 = r12;
                        this.f$10 = r13;
                        this.f$11 = r14;
                        this.f$12 = r15;
                        this.f$13 = r16;
                        this.f$14 = r17;
                        this.f$15 = r18;
                        this.f$16 = r19;
                    }

                    public final void run() {
                        SystemServer.this.lambda$startOtherServices$4$SystemServer(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15, this.f$16);
                    }
                }, BOOT_TIMINGS_TRACE_LOG);
            }
            traceEnd();
            traceBeginAndSlog("StartCountryDetectorService");
            try {
                countryDetectorService = new CountryDetectorService(context);
                ServiceManager.addService("country_detector", (IBinder) countryDetectorService);
                countryDetector2 = countryDetectorService;
            } catch (Throwable th20) {
                e8 = th20;
                countryDetector3 = null;
                countryDetector2 = countryDetector3;
                reportWtf("starting Country Detector", e8);
                traceEnd();
                traceBeginAndSlog("StartTimeDetectorService");
                this.mSystemServiceManager.startService(TIME_DETECTOR_SERVICE_CLASS);
                traceEnd();
                if (!isWatch) {
                }
                AgingCriticalEvent instance222222222 = AgingCriticalEvent.getInstance();
                networkPolicy = networkPolicy2;
                StringBuilder sb222222222 = new StringBuilder();
                iConnectivityManager = iConnectivityManager3;
                sb222222222.append("systemserver pid:");
                sb222222222.append(Process.myPid());
                instance222222222.writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, sb222222222.toString());
                if (context.getResources().getBoolean(17891451)) {
                }
                traceBeginAndSlog("StartAudioService");
                if (isArc) {
                }
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.hardware.broadcastradio")) {
                }
                traceBeginAndSlog("StartDockObserver");
                Slog.i(TAG, "DockObserver");
                this.mSystemServiceManager.startService(DockObserver.class);
                traceEnd();
                if (isWatch) {
                }
                traceBeginAndSlog("StartWiredAccessoryManager");
                inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.midi")) {
                }
                traceBeginAndSlog("StartAdbService");
                this.mSystemServiceManager.startService(ADB_SERVICE_CLASS);
                traceEnd();
                traceBeginAndSlog("StartUsbService");
                this.mSystemServiceManager.startService(USB_SERVICE_CLASS);
                traceEnd();
                if (!isWatch) {
                }
                traceBeginAndSlog("StartHardwarePropertiesManagerService");
                iBinder6 = new HardwarePropertiesManagerService(context);
                ServiceManager.addService("hardware_properties", iBinder6);
                iBinder4 = iBinder6;
                traceEnd();
                traceBeginAndSlog("StartTwilightService");
                this.mSystemServiceManager.startService(TwilightService.class);
                traceEnd();
                traceBeginAndSlog("StartColorDisplay");
                this.mSystemServiceManager.startService(ColorDisplayService.class);
                traceEnd();
                traceBeginAndSlog("StartJobScheduler");
                if (!this.mHelper.startColorJobSchedulerService()) {
                }
                traceEnd();
                traceBeginAndSlog("StartSoundTrigger");
                this.mSystemServiceManager.startService(SoundTriggerService.class);
                traceEnd();
                traceBeginAndSlog("StartTrustManager");
                this.mSystemServiceManager.startService(TrustManagerService.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                }
                traceBeginAndSlog("StartAppWidgetService");
                this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                traceEnd();
                traceBeginAndSlog("StartRoleManagerService");
                SystemServiceManager systemServiceManager2222222222 = this.mSystemServiceManager;
                Context context22222222222 = this.mSystemContext;
                systemServiceManager2222222222.startService(new RoleManagerService(context22222222222, new LegacyRoleResolutionPolicy(context22222222222)));
                traceEnd();
                traceBeginAndSlog("StartVoiceRecognitionManager");
                this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                traceEnd();
                if (GestureLauncherService.isGestureLauncherEnabled(context.getResources())) {
                }
                traceBeginAndSlog("StartSensorNotification");
                this.mSystemServiceManager.startService(SensorNotificationService.class);
                traceEnd();
                traceBeginAndSlog("StartContextHubSystemService");
                this.mSystemServiceManager.startService(ContextHubSystemService.class);
                traceEnd();
                traceBeginAndSlog("StartDiskStatsService");
                ServiceManager.addService("diskstats", new DiskStatsService(context));
                traceEnd();
                Slog.i(TAG, "Oppo modem nimidump Service");
                oppomodemService5 = new OppomodemService(context);
                ServiceManager.addService("modem_crash_up", (IBinder) oppomodemService5);
                oppomodemService3 = oppomodemService5;
                traceBeginAndSlog("CabcService");
                Slog.i(TAG, "Cabc Service");
                iBinder5 = new CabcService(context);
                ServiceManager.addService("cabc", iBinder5);
                Trace.traceEnd(524288);
                traceBeginAndSlog("RuntimeService");
                ServiceManager.addService("runtime", new RuntimeService(context));
                traceEnd();
                Slog.i(TAG, "Torch Service");
                TorchManagerService.getInstance(context).systemReady();
                traceBeginAndSlog("StartHypnusService");
                Slog.i(TAG, "Hypnus Service");
                this.hypnusService = new HypnusService(context);
                ServiceManager.addService("hypnus", (IBinder) this.hypnusService);
                traceEnd();
                Slog.i(TAG, "Oppo Customize Service");
                this.oppoCustomize = new OppoCustomizeService(context);
                ServiceManager.addService("oppocustomize", (IBinder) this.oppoCustomize);
                if (this.mOnlyCore && context.getResources().getBoolean(17891450)) {
                }
                if (isWatch) {
                }
                traceBeginAndSlog("CertBlacklister");
                new CertBlacklister(context);
                traceEnd();
                traceBeginAndSlog("StartEmergencyAffordanceService");
                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                traceEnd();
                traceBeginAndSlog("StartDreamManager");
                this.mSystemServiceManager.startService(DreamManagerService.class);
                traceEnd();
                traceBeginAndSlog("AddGraphicsStatsService");
                ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, (IBinder) new GraphicsStatsService(context));
                traceEnd();
                if (CoverageService.ENABLED) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.companion_device_setup")) {
                }
                traceBeginAndSlog("StartRestrictionManager");
                this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                traceEnd();
                traceBeginAndSlog("StartMediaSessionService");
                Slog.i(TAG, "Media Session Service");
                this.mSystemServiceManager.startService(MediaSessionService.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                }
                traceBeginAndSlog("StartTvInputManager");
                this.mSystemServiceManager.startService(TvInputManagerService.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                }
                traceBeginAndSlog("StartMediaRouterService");
                mediaRouterService = new MediaRouterService(context);
                ServiceManager.addService("media_router", (IBinder) mediaRouterService);
                mediaRouter2 = mediaRouterService;
                traceEnd();
                hasFeatureFace = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.face");
                hasFeatureIris = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.iris");
                hasFeatureFingerprint = this.mPackageManager.hasSystemFeature("android.hardware.fingerprint");
                if (hasFeatureFace) {
                }
                if (hasFeatureIris) {
                }
                if (hasFeatureFingerprint) {
                }
                traceBeginAndSlog("StartBiometricService");
                Slog.i(TAG, "BiometricService Service");
                this.mBiometricService = (BiometricService) this.mSystemServiceManager.startService(BiometricService.class);
                this.mAlipayService = (AlipayService) this.mSystemServiceManager.startService(AlipayService.class);
                traceEnd();
                traceBeginAndSlog("StartBackgroundDexOptService");
                BackgroundDexOptService.schedule(context);
                traceEnd();
                if (OppoOperatorManager.SERVICE_ENABLED) {
                }
                if (!isWatch) {
                }
                Slog.i(TAG, "Oppo Engineer Service");
                this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                if (!isWatch) {
                }
                traceBeginAndSlog("StartShortcutServiceLifecycle");
                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                traceEnd();
                traceBeginAndSlog("StartLauncherAppsService");
                this.mSystemServiceManager.startService(LauncherAppsService.class);
                traceEnd();
                traceBeginAndSlog("StartCrossProfileAppsService");
                this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                traceEnd();
                location = location2;
                r19 = iBinder9;
                countryDetector = countryDetector2;
                networkTimeUpdater = networkTimeUpdater3;
                mediaRouter = mediaRouter2;
                networkStats = r15;
                if (!isWatch) {
                }
                if (isWatch) {
                }
                if (!disableSlices) {
                }
                if (!disableCameraService) {
                }
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                }
                traceBeginAndSlog("StartStatsCompanionService");
                this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                traceEnd();
                traceBeginAndSlog("StartIncidentCompanionService");
                this.mSystemServiceManager.startService(IncidentCompanionService.class);
                traceEnd();
                if (safeMode) {
                }
                traceBeginAndSlog("StartMmsService");
                MmsServiceBroker mmsService222222222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                }
                traceBeginAndSlog("StartClipboardService");
                this.mSystemServiceManager.startService(ClipboardService.class);
                traceEnd();
                traceBeginAndSlog("AppServiceManager");
                this.mSystemServiceManager.startService(AppBindingService.Lifecycle.class);
                traceEnd();
                sMtkSystemServerIns.startMtkOtherServices();
                this.mHelper.startOtherServices();
                traceBeginAndSlog("MakeVibratorServiceReady");
                vibrator.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeLockSettingsServiceReady");
                if (lockSettings != null) {
                }
                traceEnd();
                traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                traceEnd();
                traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                this.mSystemServiceManager.startBootPhase(500);
                traceEnd();
                traceBeginAndSlog("MakeWindowManagerServiceReady");
                Slog.i(TAG, "wms systemReady");
                wm.systemReady();
                traceEnd();
                if (safeMode) {
                }
                Configuration config222222222222222222222 = wm.computeNewConfiguration(0);
                DisplayMetrics metrics222222222222222222222 = new DisplayMetrics();
                ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics222222222222222222222);
                context.getResources().updateConfiguration(config222222222222222222222, metrics222222222222222222222);
                systemTheme = context.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                }
                traceBeginAndSlog("MakePowerManagerServiceReady");
                Slog.i(TAG, "power manager systemReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                traceEnd();
                if (this.mFingerprintService != null) {
                }
                if (this.mFaceService != null) {
                }
                if (this.mBiometricService != null) {
                }
                if (this.mAlipayService != null) {
                }
                traceBeginAndSlog("StartPermissionPolicyService");
                this.mSystemServiceManager.startService(PermissionPolicyService.class);
                traceEnd();
                if (this.mOppoOperatorManagerService != null) {
                }
                traceBeginAndSlog("MakePackageManagerServiceReady");
                Slog.i(TAG, "Package systemReady");
                this.mPackageManagerService.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeOppoCustomizeServiceReady");
                if (this.oppoCustomize != null) {
                }
                traceEnd();
                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                Slog.i(TAG, "DisplayManager systemReady");
                this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                traceEnd();
                this.mSystemServiceManager.setSafeMode(safeMode);
                traceBeginAndSlog("StartDeviceSpecificServices");
                String[] classes222222222222222222222 = this.mSystemContext.getResources().getStringArray(17236011);
                while (r6 < r7) {
                }
                traceEnd();
                traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                traceEnd();
                this.mHelper.systemReady();
                Slog.i(TAG, "Ams systemReady");
                this.mActivityManagerService.systemReady(new Runnable(context, wm, safeMode, iConnectivityManager, networkManagement, networkPolicy, r19, networkStats, location, countryDetector, networkTimeUpdater, inputManager, telephonyRegistry, mediaRouter, oppomodemService, mmsService222222222222222222222) {
                    /* class com.android.server.$$Lambda$SystemServer$Veuf5e15txqWe0TdLPFg29kfanQ */
                    private final /* synthetic */ Context f$1;
                    private final /* synthetic */ CountryDetectorService f$10;
                    private final /* synthetic */ NetworkTimeUpdateService f$11;
                    private final /* synthetic */ InputManagerService f$12;
                    private final /* synthetic */ TelephonyRegistry f$13;
                    private final /* synthetic */ MediaRouterService f$14;
                    private final /* synthetic */ OppomodemService f$15;
                    private final /* synthetic */ MmsServiceBroker f$16;
                    private final /* synthetic */ WindowManagerService f$2;
                    private final /* synthetic */ boolean f$3;
                    private final /* synthetic */ ConnectivityService f$4;
                    private final /* synthetic */ NetworkManagementService f$5;
                    private final /* synthetic */ NetworkPolicyManagerService f$6;
                    private final /* synthetic */ IpSecService f$7;
                    private final /* synthetic */ NetworkStatsService f$8;
                    private final /* synthetic */ LocationManagerService f$9;

                    {
                        this.f$1 = r4;
                        this.f$2 = r5;
                        this.f$3 = r6;
                        this.f$4 = r7;
                        this.f$5 = r8;
                        this.f$6 = r9;
                        this.f$7 = r10;
                        this.f$8 = r11;
                        this.f$9 = r12;
                        this.f$10 = r13;
                        this.f$11 = r14;
                        this.f$12 = r15;
                        this.f$13 = r16;
                        this.f$14 = r17;
                        this.f$15 = r18;
                        this.f$16 = r19;
                    }

                    public final void run() {
                        SystemServer.this.lambda$startOtherServices$4$SystemServer(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15, this.f$16);
                    }
                }, BOOT_TIMINGS_TRACE_LOG);
            }
            traceEnd();
            traceBeginAndSlog("StartTimeDetectorService");
            try {
                this.mSystemServiceManager.startService(TIME_DETECTOR_SERVICE_CLASS);
            } catch (Throwable th21) {
                e7 = th21;
                reportWtf("starting StartTimeDetectorService service", e7);
                traceEnd();
                if (!isWatch) {
                }
                AgingCriticalEvent instance2222222222 = AgingCriticalEvent.getInstance();
                networkPolicy = networkPolicy2;
                StringBuilder sb2222222222 = new StringBuilder();
                iConnectivityManager = iConnectivityManager3;
                sb2222222222.append("systemserver pid:");
                sb2222222222.append(Process.myPid());
                instance2222222222.writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, sb2222222222.toString());
                if (context.getResources().getBoolean(17891451)) {
                }
                traceBeginAndSlog("StartAudioService");
                if (isArc) {
                }
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.hardware.broadcastradio")) {
                }
                traceBeginAndSlog("StartDockObserver");
                Slog.i(TAG, "DockObserver");
                this.mSystemServiceManager.startService(DockObserver.class);
                traceEnd();
                if (isWatch) {
                }
                traceBeginAndSlog("StartWiredAccessoryManager");
                inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.midi")) {
                }
                traceBeginAndSlog("StartAdbService");
                this.mSystemServiceManager.startService(ADB_SERVICE_CLASS);
                traceEnd();
                traceBeginAndSlog("StartUsbService");
                this.mSystemServiceManager.startService(USB_SERVICE_CLASS);
                traceEnd();
                if (!isWatch) {
                }
                traceBeginAndSlog("StartHardwarePropertiesManagerService");
                iBinder6 = new HardwarePropertiesManagerService(context);
                ServiceManager.addService("hardware_properties", iBinder6);
                iBinder4 = iBinder6;
                traceEnd();
                traceBeginAndSlog("StartTwilightService");
                this.mSystemServiceManager.startService(TwilightService.class);
                traceEnd();
                traceBeginAndSlog("StartColorDisplay");
                this.mSystemServiceManager.startService(ColorDisplayService.class);
                traceEnd();
                traceBeginAndSlog("StartJobScheduler");
                if (!this.mHelper.startColorJobSchedulerService()) {
                }
                traceEnd();
                traceBeginAndSlog("StartSoundTrigger");
                this.mSystemServiceManager.startService(SoundTriggerService.class);
                traceEnd();
                traceBeginAndSlog("StartTrustManager");
                this.mSystemServiceManager.startService(TrustManagerService.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                }
                traceBeginAndSlog("StartAppWidgetService");
                this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                traceEnd();
                traceBeginAndSlog("StartRoleManagerService");
                SystemServiceManager systemServiceManager22222222222 = this.mSystemServiceManager;
                Context context222222222222 = this.mSystemContext;
                systemServiceManager22222222222.startService(new RoleManagerService(context222222222222, new LegacyRoleResolutionPolicy(context222222222222)));
                traceEnd();
                traceBeginAndSlog("StartVoiceRecognitionManager");
                this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                traceEnd();
                if (GestureLauncherService.isGestureLauncherEnabled(context.getResources())) {
                }
                traceBeginAndSlog("StartSensorNotification");
                this.mSystemServiceManager.startService(SensorNotificationService.class);
                traceEnd();
                traceBeginAndSlog("StartContextHubSystemService");
                this.mSystemServiceManager.startService(ContextHubSystemService.class);
                traceEnd();
                traceBeginAndSlog("StartDiskStatsService");
                ServiceManager.addService("diskstats", new DiskStatsService(context));
                traceEnd();
                Slog.i(TAG, "Oppo modem nimidump Service");
                oppomodemService5 = new OppomodemService(context);
                ServiceManager.addService("modem_crash_up", (IBinder) oppomodemService5);
                oppomodemService3 = oppomodemService5;
                traceBeginAndSlog("CabcService");
                Slog.i(TAG, "Cabc Service");
                iBinder5 = new CabcService(context);
                ServiceManager.addService("cabc", iBinder5);
                Trace.traceEnd(524288);
                traceBeginAndSlog("RuntimeService");
                ServiceManager.addService("runtime", new RuntimeService(context));
                traceEnd();
                Slog.i(TAG, "Torch Service");
                TorchManagerService.getInstance(context).systemReady();
                traceBeginAndSlog("StartHypnusService");
                Slog.i(TAG, "Hypnus Service");
                this.hypnusService = new HypnusService(context);
                ServiceManager.addService("hypnus", (IBinder) this.hypnusService);
                traceEnd();
                Slog.i(TAG, "Oppo Customize Service");
                this.oppoCustomize = new OppoCustomizeService(context);
                ServiceManager.addService("oppocustomize", (IBinder) this.oppoCustomize);
                if (this.mOnlyCore && context.getResources().getBoolean(17891450)) {
                }
                if (isWatch) {
                }
                traceBeginAndSlog("CertBlacklister");
                new CertBlacklister(context);
                traceEnd();
                traceBeginAndSlog("StartEmergencyAffordanceService");
                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                traceEnd();
                traceBeginAndSlog("StartDreamManager");
                this.mSystemServiceManager.startService(DreamManagerService.class);
                traceEnd();
                traceBeginAndSlog("AddGraphicsStatsService");
                ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, (IBinder) new GraphicsStatsService(context));
                traceEnd();
                if (CoverageService.ENABLED) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.companion_device_setup")) {
                }
                traceBeginAndSlog("StartRestrictionManager");
                this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                traceEnd();
                traceBeginAndSlog("StartMediaSessionService");
                Slog.i(TAG, "Media Session Service");
                this.mSystemServiceManager.startService(MediaSessionService.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                }
                traceBeginAndSlog("StartTvInputManager");
                this.mSystemServiceManager.startService(TvInputManagerService.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                }
                traceBeginAndSlog("StartMediaRouterService");
                mediaRouterService = new MediaRouterService(context);
                ServiceManager.addService("media_router", (IBinder) mediaRouterService);
                mediaRouter2 = mediaRouterService;
                traceEnd();
                hasFeatureFace = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.face");
                hasFeatureIris = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.iris");
                hasFeatureFingerprint = this.mPackageManager.hasSystemFeature("android.hardware.fingerprint");
                if (hasFeatureFace) {
                }
                if (hasFeatureIris) {
                }
                if (hasFeatureFingerprint) {
                }
                traceBeginAndSlog("StartBiometricService");
                Slog.i(TAG, "BiometricService Service");
                this.mBiometricService = (BiometricService) this.mSystemServiceManager.startService(BiometricService.class);
                this.mAlipayService = (AlipayService) this.mSystemServiceManager.startService(AlipayService.class);
                traceEnd();
                traceBeginAndSlog("StartBackgroundDexOptService");
                BackgroundDexOptService.schedule(context);
                traceEnd();
                if (OppoOperatorManager.SERVICE_ENABLED) {
                }
                if (!isWatch) {
                }
                Slog.i(TAG, "Oppo Engineer Service");
                this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                if (!isWatch) {
                }
                traceBeginAndSlog("StartShortcutServiceLifecycle");
                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                traceEnd();
                traceBeginAndSlog("StartLauncherAppsService");
                this.mSystemServiceManager.startService(LauncherAppsService.class);
                traceEnd();
                traceBeginAndSlog("StartCrossProfileAppsService");
                this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                traceEnd();
                location = location2;
                r19 = iBinder9;
                countryDetector = countryDetector2;
                networkTimeUpdater = networkTimeUpdater3;
                mediaRouter = mediaRouter2;
                networkStats = r15;
                if (!isWatch) {
                }
                if (isWatch) {
                }
                if (!disableSlices) {
                }
                if (!disableCameraService) {
                }
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                }
                traceBeginAndSlog("StartStatsCompanionService");
                this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                traceEnd();
                traceBeginAndSlog("StartIncidentCompanionService");
                this.mSystemServiceManager.startService(IncidentCompanionService.class);
                traceEnd();
                if (safeMode) {
                }
                traceBeginAndSlog("StartMmsService");
                MmsServiceBroker mmsService2222222222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                }
                traceBeginAndSlog("StartClipboardService");
                this.mSystemServiceManager.startService(ClipboardService.class);
                traceEnd();
                traceBeginAndSlog("AppServiceManager");
                this.mSystemServiceManager.startService(AppBindingService.Lifecycle.class);
                traceEnd();
                sMtkSystemServerIns.startMtkOtherServices();
                this.mHelper.startOtherServices();
                traceBeginAndSlog("MakeVibratorServiceReady");
                vibrator.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeLockSettingsServiceReady");
                if (lockSettings != null) {
                }
                traceEnd();
                traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                traceEnd();
                traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                this.mSystemServiceManager.startBootPhase(500);
                traceEnd();
                traceBeginAndSlog("MakeWindowManagerServiceReady");
                Slog.i(TAG, "wms systemReady");
                wm.systemReady();
                traceEnd();
                if (safeMode) {
                }
                Configuration config2222222222222222222222 = wm.computeNewConfiguration(0);
                DisplayMetrics metrics2222222222222222222222 = new DisplayMetrics();
                ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics2222222222222222222222);
                context.getResources().updateConfiguration(config2222222222222222222222, metrics2222222222222222222222);
                systemTheme = context.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                }
                traceBeginAndSlog("MakePowerManagerServiceReady");
                Slog.i(TAG, "power manager systemReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                traceEnd();
                if (this.mFingerprintService != null) {
                }
                if (this.mFaceService != null) {
                }
                if (this.mBiometricService != null) {
                }
                if (this.mAlipayService != null) {
                }
                traceBeginAndSlog("StartPermissionPolicyService");
                this.mSystemServiceManager.startService(PermissionPolicyService.class);
                traceEnd();
                if (this.mOppoOperatorManagerService != null) {
                }
                traceBeginAndSlog("MakePackageManagerServiceReady");
                Slog.i(TAG, "Package systemReady");
                this.mPackageManagerService.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeOppoCustomizeServiceReady");
                if (this.oppoCustomize != null) {
                }
                traceEnd();
                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                Slog.i(TAG, "DisplayManager systemReady");
                this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                traceEnd();
                this.mSystemServiceManager.setSafeMode(safeMode);
                traceBeginAndSlog("StartDeviceSpecificServices");
                String[] classes2222222222222222222222 = this.mSystemContext.getResources().getStringArray(17236011);
                while (r6 < r7) {
                }
                traceEnd();
                traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                traceEnd();
                this.mHelper.systemReady();
                Slog.i(TAG, "Ams systemReady");
                this.mActivityManagerService.systemReady(new Runnable(context, wm, safeMode, iConnectivityManager, networkManagement, networkPolicy, r19, networkStats, location, countryDetector, networkTimeUpdater, inputManager, telephonyRegistry, mediaRouter, oppomodemService, mmsService2222222222222222222222) {
                    /* class com.android.server.$$Lambda$SystemServer$Veuf5e15txqWe0TdLPFg29kfanQ */
                    private final /* synthetic */ Context f$1;
                    private final /* synthetic */ CountryDetectorService f$10;
                    private final /* synthetic */ NetworkTimeUpdateService f$11;
                    private final /* synthetic */ InputManagerService f$12;
                    private final /* synthetic */ TelephonyRegistry f$13;
                    private final /* synthetic */ MediaRouterService f$14;
                    private final /* synthetic */ OppomodemService f$15;
                    private final /* synthetic */ MmsServiceBroker f$16;
                    private final /* synthetic */ WindowManagerService f$2;
                    private final /* synthetic */ boolean f$3;
                    private final /* synthetic */ ConnectivityService f$4;
                    private final /* synthetic */ NetworkManagementService f$5;
                    private final /* synthetic */ NetworkPolicyManagerService f$6;
                    private final /* synthetic */ IpSecService f$7;
                    private final /* synthetic */ NetworkStatsService f$8;
                    private final /* synthetic */ LocationManagerService f$9;

                    {
                        this.f$1 = r4;
                        this.f$2 = r5;
                        this.f$3 = r6;
                        this.f$4 = r7;
                        this.f$5 = r8;
                        this.f$6 = r9;
                        this.f$7 = r10;
                        this.f$8 = r11;
                        this.f$9 = r12;
                        this.f$10 = r13;
                        this.f$11 = r14;
                        this.f$12 = r15;
                        this.f$13 = r16;
                        this.f$14 = r17;
                        this.f$15 = r18;
                        this.f$16 = r19;
                    }

                    public final void run() {
                        SystemServer.this.lambda$startOtherServices$4$SystemServer(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15, this.f$16);
                    }
                }, BOOT_TIMINGS_TRACE_LOG);
            }
            traceEnd();
            if (!isWatch) {
                traceBeginAndSlog("StartSearchManagerService");
                try {
                    this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                } catch (Throwable e41) {
                    reportWtf("starting Search Service", e41);
                }
                traceEnd();
            }
            AgingCriticalEvent instance22222222222 = AgingCriticalEvent.getInstance();
            networkPolicy = networkPolicy2;
            StringBuilder sb22222222222 = new StringBuilder();
            iConnectivityManager = iConnectivityManager3;
            sb22222222222.append("systemserver pid:");
            sb22222222222.append(Process.myPid());
            instance22222222222.writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, sb22222222222.toString());
            if (context.getResources().getBoolean(17891451)) {
                traceBeginAndSlog("StartWallpaperManagerService");
                this.mSystemServiceManager.startService(WALLPAPER_SERVICE_CLASS);
                traceEnd();
            }
            traceBeginAndSlog("StartAudioService");
            if (isArc) {
                this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
            } else {
                String className = context.getResources().getString(17039718);
                try {
                    this.mSystemServiceManager.startService(className + "$Lifecycle");
                } catch (Throwable e42) {
                    reportWtf("starting " + className, e42);
                }
            }
            traceEnd();
            if (this.mPackageManager.hasSystemFeature("android.hardware.broadcastradio")) {
                traceBeginAndSlog("StartBroadcastRadioService");
                this.mSystemServiceManager.startService(BroadcastRadioService.class);
                traceEnd();
            }
            traceBeginAndSlog("StartDockObserver");
            Slog.i(TAG, "DockObserver");
            this.mSystemServiceManager.startService(DockObserver.class);
            traceEnd();
            if (isWatch) {
                traceBeginAndSlog("StartThermalObserver");
                this.mSystemServiceManager.startService(THERMAL_OBSERVER_CLASS);
                traceEnd();
            }
            traceBeginAndSlog("StartWiredAccessoryManager");
            try {
                inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
            } catch (Throwable e43) {
                reportWtf("starting WiredAccessoryManager", e43);
            }
            traceEnd();
            if (this.mPackageManager.hasSystemFeature("android.software.midi")) {
                traceBeginAndSlog("StartMidiManager");
                this.mSystemServiceManager.startService(MIDI_SERVICE_CLASS);
                traceEnd();
            }
            traceBeginAndSlog("StartAdbService");
            try {
                this.mSystemServiceManager.startService(ADB_SERVICE_CLASS);
            } catch (Throwable th22) {
                Slog.e(TAG, "Failure starting AdbService");
            }
            traceEnd();
            if (this.mPackageManager.hasSystemFeature("android.hardware.usb.host") || this.mPackageManager.hasSystemFeature("android.hardware.usb.accessory") || isEmulator) {
                traceBeginAndSlog("StartUsbService");
                this.mSystemServiceManager.startService(USB_SERVICE_CLASS);
                traceEnd();
            }
            if (!isWatch) {
                traceBeginAndSlog("StartSerialService");
                try {
                    iBinder7 = new SerialService(context);
                    try {
                        ServiceManager.addService("serial", iBinder7);
                    } catch (Throwable th23) {
                        e6 = th23;
                    }
                } catch (Throwable th24) {
                    e6 = th24;
                    iBinder7 = null;
                    Slog.e(TAG, "Failure starting SerialService", e6);
                    traceEnd();
                    iBinder11 = iBinder7;
                    traceBeginAndSlog("StartHardwarePropertiesManagerService");
                    iBinder6 = new HardwarePropertiesManagerService(context);
                    ServiceManager.addService("hardware_properties", iBinder6);
                    iBinder4 = iBinder6;
                    traceEnd();
                    traceBeginAndSlog("StartTwilightService");
                    this.mSystemServiceManager.startService(TwilightService.class);
                    traceEnd();
                    traceBeginAndSlog("StartColorDisplay");
                    this.mSystemServiceManager.startService(ColorDisplayService.class);
                    traceEnd();
                    traceBeginAndSlog("StartJobScheduler");
                    if (!this.mHelper.startColorJobSchedulerService()) {
                    }
                    traceEnd();
                    traceBeginAndSlog("StartSoundTrigger");
                    this.mSystemServiceManager.startService(SoundTriggerService.class);
                    traceEnd();
                    traceBeginAndSlog("StartTrustManager");
                    this.mSystemServiceManager.startService(TrustManagerService.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                    }
                    traceBeginAndSlog("StartAppWidgetService");
                    this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                    traceEnd();
                    traceBeginAndSlog("StartRoleManagerService");
                    SystemServiceManager systemServiceManager222222222222 = this.mSystemServiceManager;
                    Context context2222222222222 = this.mSystemContext;
                    systemServiceManager222222222222.startService(new RoleManagerService(context2222222222222, new LegacyRoleResolutionPolicy(context2222222222222)));
                    traceEnd();
                    traceBeginAndSlog("StartVoiceRecognitionManager");
                    this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                    traceEnd();
                    if (GestureLauncherService.isGestureLauncherEnabled(context.getResources())) {
                    }
                    traceBeginAndSlog("StartSensorNotification");
                    this.mSystemServiceManager.startService(SensorNotificationService.class);
                    traceEnd();
                    traceBeginAndSlog("StartContextHubSystemService");
                    this.mSystemServiceManager.startService(ContextHubSystemService.class);
                    traceEnd();
                    traceBeginAndSlog("StartDiskStatsService");
                    ServiceManager.addService("diskstats", new DiskStatsService(context));
                    traceEnd();
                    Slog.i(TAG, "Oppo modem nimidump Service");
                    oppomodemService5 = new OppomodemService(context);
                    ServiceManager.addService("modem_crash_up", (IBinder) oppomodemService5);
                    oppomodemService3 = oppomodemService5;
                    traceBeginAndSlog("CabcService");
                    Slog.i(TAG, "Cabc Service");
                    iBinder5 = new CabcService(context);
                    ServiceManager.addService("cabc", iBinder5);
                    Trace.traceEnd(524288);
                    traceBeginAndSlog("RuntimeService");
                    ServiceManager.addService("runtime", new RuntimeService(context));
                    traceEnd();
                    Slog.i(TAG, "Torch Service");
                    TorchManagerService.getInstance(context).systemReady();
                    traceBeginAndSlog("StartHypnusService");
                    Slog.i(TAG, "Hypnus Service");
                    this.hypnusService = new HypnusService(context);
                    ServiceManager.addService("hypnus", (IBinder) this.hypnusService);
                    traceEnd();
                    Slog.i(TAG, "Oppo Customize Service");
                    this.oppoCustomize = new OppoCustomizeService(context);
                    ServiceManager.addService("oppocustomize", (IBinder) this.oppoCustomize);
                    if (this.mOnlyCore && context.getResources().getBoolean(17891450)) {
                    }
                    if (isWatch) {
                    }
                    traceBeginAndSlog("CertBlacklister");
                    new CertBlacklister(context);
                    traceEnd();
                    traceBeginAndSlog("StartEmergencyAffordanceService");
                    this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                    traceEnd();
                    traceBeginAndSlog("StartDreamManager");
                    this.mSystemServiceManager.startService(DreamManagerService.class);
                    traceEnd();
                    traceBeginAndSlog("AddGraphicsStatsService");
                    ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, (IBinder) new GraphicsStatsService(context));
                    traceEnd();
                    if (CoverageService.ENABLED) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.companion_device_setup")) {
                    }
                    traceBeginAndSlog("StartRestrictionManager");
                    this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                    traceEnd();
                    traceBeginAndSlog("StartMediaSessionService");
                    Slog.i(TAG, "Media Session Service");
                    this.mSystemServiceManager.startService(MediaSessionService.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                    }
                    traceBeginAndSlog("StartTvInputManager");
                    this.mSystemServiceManager.startService(TvInputManagerService.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                    }
                    traceBeginAndSlog("StartMediaRouterService");
                    mediaRouterService = new MediaRouterService(context);
                    ServiceManager.addService("media_router", (IBinder) mediaRouterService);
                    mediaRouter2 = mediaRouterService;
                    traceEnd();
                    hasFeatureFace = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.face");
                    hasFeatureIris = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.iris");
                    hasFeatureFingerprint = this.mPackageManager.hasSystemFeature("android.hardware.fingerprint");
                    if (hasFeatureFace) {
                    }
                    if (hasFeatureIris) {
                    }
                    if (hasFeatureFingerprint) {
                    }
                    traceBeginAndSlog("StartBiometricService");
                    Slog.i(TAG, "BiometricService Service");
                    this.mBiometricService = (BiometricService) this.mSystemServiceManager.startService(BiometricService.class);
                    this.mAlipayService = (AlipayService) this.mSystemServiceManager.startService(AlipayService.class);
                    traceEnd();
                    traceBeginAndSlog("StartBackgroundDexOptService");
                    BackgroundDexOptService.schedule(context);
                    traceEnd();
                    if (OppoOperatorManager.SERVICE_ENABLED) {
                    }
                    if (!isWatch) {
                    }
                    Slog.i(TAG, "Oppo Engineer Service");
                    this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                    if (!isWatch) {
                    }
                    traceBeginAndSlog("StartShortcutServiceLifecycle");
                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                    traceEnd();
                    traceBeginAndSlog("StartLauncherAppsService");
                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                    traceEnd();
                    traceBeginAndSlog("StartCrossProfileAppsService");
                    this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                    traceEnd();
                    location = location2;
                    r19 = iBinder9;
                    countryDetector = countryDetector2;
                    networkTimeUpdater = networkTimeUpdater3;
                    mediaRouter = mediaRouter2;
                    networkStats = r15;
                    if (!isWatch) {
                    }
                    if (isWatch) {
                    }
                    if (!disableSlices) {
                    }
                    if (!disableCameraService) {
                    }
                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                    }
                    traceBeginAndSlog("StartStatsCompanionService");
                    this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                    traceEnd();
                    traceBeginAndSlog("StartIncidentCompanionService");
                    this.mSystemServiceManager.startService(IncidentCompanionService.class);
                    traceEnd();
                    if (safeMode) {
                    }
                    traceBeginAndSlog("StartMmsService");
                    MmsServiceBroker mmsService22222222222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                    }
                    traceBeginAndSlog("StartClipboardService");
                    this.mSystemServiceManager.startService(ClipboardService.class);
                    traceEnd();
                    traceBeginAndSlog("AppServiceManager");
                    this.mSystemServiceManager.startService(AppBindingService.Lifecycle.class);
                    traceEnd();
                    sMtkSystemServerIns.startMtkOtherServices();
                    this.mHelper.startOtherServices();
                    traceBeginAndSlog("MakeVibratorServiceReady");
                    vibrator.systemReady();
                    traceEnd();
                    traceBeginAndSlog("MakeLockSettingsServiceReady");
                    if (lockSettings != null) {
                    }
                    traceEnd();
                    traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                    traceEnd();
                    traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                    this.mSystemServiceManager.startBootPhase(500);
                    traceEnd();
                    traceBeginAndSlog("MakeWindowManagerServiceReady");
                    Slog.i(TAG, "wms systemReady");
                    wm.systemReady();
                    traceEnd();
                    if (safeMode) {
                    }
                    Configuration config22222222222222222222222 = wm.computeNewConfiguration(0);
                    DisplayMetrics metrics22222222222222222222222 = new DisplayMetrics();
                    ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics22222222222222222222222);
                    context.getResources().updateConfiguration(config22222222222222222222222, metrics22222222222222222222222);
                    systemTheme = context.getTheme();
                    if (systemTheme.getChangingConfigurations() != 0) {
                    }
                    traceBeginAndSlog("MakePowerManagerServiceReady");
                    Slog.i(TAG, "power manager systemReady");
                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                    traceEnd();
                    if (this.mFingerprintService != null) {
                    }
                    if (this.mFaceService != null) {
                    }
                    if (this.mBiometricService != null) {
                    }
                    if (this.mAlipayService != null) {
                    }
                    traceBeginAndSlog("StartPermissionPolicyService");
                    this.mSystemServiceManager.startService(PermissionPolicyService.class);
                    traceEnd();
                    if (this.mOppoOperatorManagerService != null) {
                    }
                    traceBeginAndSlog("MakePackageManagerServiceReady");
                    Slog.i(TAG, "Package systemReady");
                    this.mPackageManagerService.systemReady();
                    traceEnd();
                    traceBeginAndSlog("MakeOppoCustomizeServiceReady");
                    if (this.oppoCustomize != null) {
                    }
                    traceEnd();
                    traceBeginAndSlog("MakeDisplayManagerServiceReady");
                    Slog.i(TAG, "DisplayManager systemReady");
                    this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                    traceEnd();
                    this.mSystemServiceManager.setSafeMode(safeMode);
                    traceBeginAndSlog("StartDeviceSpecificServices");
                    String[] classes22222222222222222222222 = this.mSystemContext.getResources().getStringArray(17236011);
                    while (r6 < r7) {
                    }
                    traceEnd();
                    traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                    traceEnd();
                    this.mHelper.systemReady();
                    Slog.i(TAG, "Ams systemReady");
                    this.mActivityManagerService.systemReady(new Runnable(context, wm, safeMode, iConnectivityManager, networkManagement, networkPolicy, r19, networkStats, location, countryDetector, networkTimeUpdater, inputManager, telephonyRegistry, mediaRouter, oppomodemService, mmsService22222222222222222222222) {
                        /* class com.android.server.$$Lambda$SystemServer$Veuf5e15txqWe0TdLPFg29kfanQ */
                        private final /* synthetic */ Context f$1;
                        private final /* synthetic */ CountryDetectorService f$10;
                        private final /* synthetic */ NetworkTimeUpdateService f$11;
                        private final /* synthetic */ InputManagerService f$12;
                        private final /* synthetic */ TelephonyRegistry f$13;
                        private final /* synthetic */ MediaRouterService f$14;
                        private final /* synthetic */ OppomodemService f$15;
                        private final /* synthetic */ MmsServiceBroker f$16;
                        private final /* synthetic */ WindowManagerService f$2;
                        private final /* synthetic */ boolean f$3;
                        private final /* synthetic */ ConnectivityService f$4;
                        private final /* synthetic */ NetworkManagementService f$5;
                        private final /* synthetic */ NetworkPolicyManagerService f$6;
                        private final /* synthetic */ IpSecService f$7;
                        private final /* synthetic */ NetworkStatsService f$8;
                        private final /* synthetic */ LocationManagerService f$9;

                        {
                            this.f$1 = r4;
                            this.f$2 = r5;
                            this.f$3 = r6;
                            this.f$4 = r7;
                            this.f$5 = r8;
                            this.f$6 = r9;
                            this.f$7 = r10;
                            this.f$8 = r11;
                            this.f$9 = r12;
                            this.f$10 = r13;
                            this.f$11 = r14;
                            this.f$12 = r15;
                            this.f$13 = r16;
                            this.f$14 = r17;
                            this.f$15 = r18;
                            this.f$16 = r19;
                        }

                        public final void run() {
                            SystemServer.this.lambda$startOtherServices$4$SystemServer(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15, this.f$16);
                        }
                    }, BOOT_TIMINGS_TRACE_LOG);
                }
                traceEnd();
                iBinder11 = iBinder7;
            }
            traceBeginAndSlog("StartHardwarePropertiesManagerService");
            try {
                iBinder6 = new HardwarePropertiesManagerService(context);
                ServiceManager.addService("hardware_properties", iBinder6);
                iBinder4 = iBinder6;
            } catch (Throwable th25) {
                e5 = th25;
                iBinder6 = null;
                Slog.e(TAG, "Failure starting HardwarePropertiesManagerService", e5);
                iBinder4 = iBinder6;
                traceEnd();
                traceBeginAndSlog("StartTwilightService");
                this.mSystemServiceManager.startService(TwilightService.class);
                traceEnd();
                traceBeginAndSlog("StartColorDisplay");
                this.mSystemServiceManager.startService(ColorDisplayService.class);
                traceEnd();
                traceBeginAndSlog("StartJobScheduler");
                if (!this.mHelper.startColorJobSchedulerService()) {
                }
                traceEnd();
                traceBeginAndSlog("StartSoundTrigger");
                this.mSystemServiceManager.startService(SoundTriggerService.class);
                traceEnd();
                traceBeginAndSlog("StartTrustManager");
                this.mSystemServiceManager.startService(TrustManagerService.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                }
                traceBeginAndSlog("StartAppWidgetService");
                this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                traceEnd();
                traceBeginAndSlog("StartRoleManagerService");
                SystemServiceManager systemServiceManager2222222222222 = this.mSystemServiceManager;
                Context context22222222222222 = this.mSystemContext;
                systemServiceManager2222222222222.startService(new RoleManagerService(context22222222222222, new LegacyRoleResolutionPolicy(context22222222222222)));
                traceEnd();
                traceBeginAndSlog("StartVoiceRecognitionManager");
                this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                traceEnd();
                if (GestureLauncherService.isGestureLauncherEnabled(context.getResources())) {
                }
                traceBeginAndSlog("StartSensorNotification");
                this.mSystemServiceManager.startService(SensorNotificationService.class);
                traceEnd();
                traceBeginAndSlog("StartContextHubSystemService");
                this.mSystemServiceManager.startService(ContextHubSystemService.class);
                traceEnd();
                traceBeginAndSlog("StartDiskStatsService");
                ServiceManager.addService("diskstats", new DiskStatsService(context));
                traceEnd();
                Slog.i(TAG, "Oppo modem nimidump Service");
                oppomodemService5 = new OppomodemService(context);
                ServiceManager.addService("modem_crash_up", (IBinder) oppomodemService5);
                oppomodemService3 = oppomodemService5;
                traceBeginAndSlog("CabcService");
                Slog.i(TAG, "Cabc Service");
                iBinder5 = new CabcService(context);
                ServiceManager.addService("cabc", iBinder5);
                Trace.traceEnd(524288);
                traceBeginAndSlog("RuntimeService");
                ServiceManager.addService("runtime", new RuntimeService(context));
                traceEnd();
                Slog.i(TAG, "Torch Service");
                TorchManagerService.getInstance(context).systemReady();
                traceBeginAndSlog("StartHypnusService");
                Slog.i(TAG, "Hypnus Service");
                this.hypnusService = new HypnusService(context);
                ServiceManager.addService("hypnus", (IBinder) this.hypnusService);
                traceEnd();
                Slog.i(TAG, "Oppo Customize Service");
                this.oppoCustomize = new OppoCustomizeService(context);
                ServiceManager.addService("oppocustomize", (IBinder) this.oppoCustomize);
                if (this.mOnlyCore && context.getResources().getBoolean(17891450)) {
                }
                if (isWatch) {
                }
                traceBeginAndSlog("CertBlacklister");
                new CertBlacklister(context);
                traceEnd();
                traceBeginAndSlog("StartEmergencyAffordanceService");
                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                traceEnd();
                traceBeginAndSlog("StartDreamManager");
                this.mSystemServiceManager.startService(DreamManagerService.class);
                traceEnd();
                traceBeginAndSlog("AddGraphicsStatsService");
                ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, (IBinder) new GraphicsStatsService(context));
                traceEnd();
                if (CoverageService.ENABLED) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.companion_device_setup")) {
                }
                traceBeginAndSlog("StartRestrictionManager");
                this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                traceEnd();
                traceBeginAndSlog("StartMediaSessionService");
                Slog.i(TAG, "Media Session Service");
                this.mSystemServiceManager.startService(MediaSessionService.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                }
                traceBeginAndSlog("StartTvInputManager");
                this.mSystemServiceManager.startService(TvInputManagerService.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                }
                traceBeginAndSlog("StartMediaRouterService");
                mediaRouterService = new MediaRouterService(context);
                ServiceManager.addService("media_router", (IBinder) mediaRouterService);
                mediaRouter2 = mediaRouterService;
                traceEnd();
                hasFeatureFace = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.face");
                hasFeatureIris = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.iris");
                hasFeatureFingerprint = this.mPackageManager.hasSystemFeature("android.hardware.fingerprint");
                if (hasFeatureFace) {
                }
                if (hasFeatureIris) {
                }
                if (hasFeatureFingerprint) {
                }
                traceBeginAndSlog("StartBiometricService");
                Slog.i(TAG, "BiometricService Service");
                this.mBiometricService = (BiometricService) this.mSystemServiceManager.startService(BiometricService.class);
                this.mAlipayService = (AlipayService) this.mSystemServiceManager.startService(AlipayService.class);
                traceEnd();
                traceBeginAndSlog("StartBackgroundDexOptService");
                BackgroundDexOptService.schedule(context);
                traceEnd();
                if (OppoOperatorManager.SERVICE_ENABLED) {
                }
                if (!isWatch) {
                }
                Slog.i(TAG, "Oppo Engineer Service");
                this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                if (!isWatch) {
                }
                traceBeginAndSlog("StartShortcutServiceLifecycle");
                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                traceEnd();
                traceBeginAndSlog("StartLauncherAppsService");
                this.mSystemServiceManager.startService(LauncherAppsService.class);
                traceEnd();
                traceBeginAndSlog("StartCrossProfileAppsService");
                this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                traceEnd();
                location = location2;
                r19 = iBinder9;
                countryDetector = countryDetector2;
                networkTimeUpdater = networkTimeUpdater3;
                mediaRouter = mediaRouter2;
                networkStats = r15;
                if (!isWatch) {
                }
                if (isWatch) {
                }
                if (!disableSlices) {
                }
                if (!disableCameraService) {
                }
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                }
                traceBeginAndSlog("StartStatsCompanionService");
                this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                traceEnd();
                traceBeginAndSlog("StartIncidentCompanionService");
                this.mSystemServiceManager.startService(IncidentCompanionService.class);
                traceEnd();
                if (safeMode) {
                }
                traceBeginAndSlog("StartMmsService");
                MmsServiceBroker mmsService222222222222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                }
                traceBeginAndSlog("StartClipboardService");
                this.mSystemServiceManager.startService(ClipboardService.class);
                traceEnd();
                traceBeginAndSlog("AppServiceManager");
                this.mSystemServiceManager.startService(AppBindingService.Lifecycle.class);
                traceEnd();
                sMtkSystemServerIns.startMtkOtherServices();
                this.mHelper.startOtherServices();
                traceBeginAndSlog("MakeVibratorServiceReady");
                vibrator.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeLockSettingsServiceReady");
                if (lockSettings != null) {
                }
                traceEnd();
                traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                traceEnd();
                traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                this.mSystemServiceManager.startBootPhase(500);
                traceEnd();
                traceBeginAndSlog("MakeWindowManagerServiceReady");
                Slog.i(TAG, "wms systemReady");
                wm.systemReady();
                traceEnd();
                if (safeMode) {
                }
                Configuration config222222222222222222222222 = wm.computeNewConfiguration(0);
                DisplayMetrics metrics222222222222222222222222 = new DisplayMetrics();
                ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics222222222222222222222222);
                context.getResources().updateConfiguration(config222222222222222222222222, metrics222222222222222222222222);
                systemTheme = context.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                }
                traceBeginAndSlog("MakePowerManagerServiceReady");
                Slog.i(TAG, "power manager systemReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                traceEnd();
                if (this.mFingerprintService != null) {
                }
                if (this.mFaceService != null) {
                }
                if (this.mBiometricService != null) {
                }
                if (this.mAlipayService != null) {
                }
                traceBeginAndSlog("StartPermissionPolicyService");
                this.mSystemServiceManager.startService(PermissionPolicyService.class);
                traceEnd();
                if (this.mOppoOperatorManagerService != null) {
                }
                traceBeginAndSlog("MakePackageManagerServiceReady");
                Slog.i(TAG, "Package systemReady");
                this.mPackageManagerService.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeOppoCustomizeServiceReady");
                if (this.oppoCustomize != null) {
                }
                traceEnd();
                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                Slog.i(TAG, "DisplayManager systemReady");
                this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                traceEnd();
                this.mSystemServiceManager.setSafeMode(safeMode);
                traceBeginAndSlog("StartDeviceSpecificServices");
                String[] classes222222222222222222222222 = this.mSystemContext.getResources().getStringArray(17236011);
                while (r6 < r7) {
                }
                traceEnd();
                traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                traceEnd();
                this.mHelper.systemReady();
                Slog.i(TAG, "Ams systemReady");
                this.mActivityManagerService.systemReady(new Runnable(context, wm, safeMode, iConnectivityManager, networkManagement, networkPolicy, r19, networkStats, location, countryDetector, networkTimeUpdater, inputManager, telephonyRegistry, mediaRouter, oppomodemService, mmsService222222222222222222222222) {
                    /* class com.android.server.$$Lambda$SystemServer$Veuf5e15txqWe0TdLPFg29kfanQ */
                    private final /* synthetic */ Context f$1;
                    private final /* synthetic */ CountryDetectorService f$10;
                    private final /* synthetic */ NetworkTimeUpdateService f$11;
                    private final /* synthetic */ InputManagerService f$12;
                    private final /* synthetic */ TelephonyRegistry f$13;
                    private final /* synthetic */ MediaRouterService f$14;
                    private final /* synthetic */ OppomodemService f$15;
                    private final /* synthetic */ MmsServiceBroker f$16;
                    private final /* synthetic */ WindowManagerService f$2;
                    private final /* synthetic */ boolean f$3;
                    private final /* synthetic */ ConnectivityService f$4;
                    private final /* synthetic */ NetworkManagementService f$5;
                    private final /* synthetic */ NetworkPolicyManagerService f$6;
                    private final /* synthetic */ IpSecService f$7;
                    private final /* synthetic */ NetworkStatsService f$8;
                    private final /* synthetic */ LocationManagerService f$9;

                    {
                        this.f$1 = r4;
                        this.f$2 = r5;
                        this.f$3 = r6;
                        this.f$4 = r7;
                        this.f$5 = r8;
                        this.f$6 = r9;
                        this.f$7 = r10;
                        this.f$8 = r11;
                        this.f$9 = r12;
                        this.f$10 = r13;
                        this.f$11 = r14;
                        this.f$12 = r15;
                        this.f$13 = r16;
                        this.f$14 = r17;
                        this.f$15 = r18;
                        this.f$16 = r19;
                    }

                    public final void run() {
                        SystemServer.this.lambda$startOtherServices$4$SystemServer(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15, this.f$16);
                    }
                }, BOOT_TIMINGS_TRACE_LOG);
            }
            traceEnd();
            traceBeginAndSlog("StartTwilightService");
            this.mSystemServiceManager.startService(TwilightService.class);
            traceEnd();
            traceBeginAndSlog("StartColorDisplay");
            this.mSystemServiceManager.startService(ColorDisplayService.class);
            traceEnd();
            traceBeginAndSlog("StartJobScheduler");
            if (!this.mHelper.startColorJobSchedulerService()) {
                this.mSystemServiceManager.startService(JobSchedulerService.class);
            }
            traceEnd();
            traceBeginAndSlog("StartSoundTrigger");
            this.mSystemServiceManager.startService(SoundTriggerService.class);
            traceEnd();
            traceBeginAndSlog("StartTrustManager");
            this.mSystemServiceManager.startService(TrustManagerService.class);
            traceEnd();
            if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                traceBeginAndSlog("StartBackupManager");
                this.mSystemServiceManager.startService(BACKUP_MANAGER_SERVICE_CLASS);
                traceEnd();
            }
            if (this.mPackageManager.hasSystemFeature("android.software.app_widgets") || context.getResources().getBoolean(17891432)) {
                traceBeginAndSlog("StartAppWidgetService");
                this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                traceEnd();
            }
            traceBeginAndSlog("StartRoleManagerService");
            SystemServiceManager systemServiceManager22222222222222 = this.mSystemServiceManager;
            Context context222222222222222 = this.mSystemContext;
            systemServiceManager22222222222222.startService(new RoleManagerService(context222222222222222, new LegacyRoleResolutionPolicy(context222222222222222)));
            traceEnd();
            traceBeginAndSlog("StartVoiceRecognitionManager");
            this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
            traceEnd();
            if (GestureLauncherService.isGestureLauncherEnabled(context.getResources())) {
                traceBeginAndSlog("StartGestureLauncher");
                this.mSystemServiceManager.startService(GestureLauncherService.class);
                traceEnd();
            }
            traceBeginAndSlog("StartSensorNotification");
            this.mSystemServiceManager.startService(SensorNotificationService.class);
            traceEnd();
            traceBeginAndSlog("StartContextHubSystemService");
            this.mSystemServiceManager.startService(ContextHubSystemService.class);
            traceEnd();
            traceBeginAndSlog("StartDiskStatsService");
            try {
                ServiceManager.addService("diskstats", new DiskStatsService(context));
            } catch (Throwable e44) {
                reportWtf("starting DiskStats Service", e44);
            }
            traceEnd();
            try {
                Slog.i(TAG, "Oppo modem nimidump Service");
                oppomodemService5 = new OppomodemService(context);
                ServiceManager.addService("modem_crash_up", (IBinder) oppomodemService5);
                oppomodemService3 = oppomodemService5;
            } catch (Throwable th26) {
                e4 = th26;
                oppomodemService4 = oppomodemService2;
                Slog.e(TAG, "Failure starting Oppo Service", e4);
                oppomodemService3 = oppomodemService4;
                traceBeginAndSlog("CabcService");
                Slog.i(TAG, "Cabc Service");
                iBinder5 = new CabcService(context);
                ServiceManager.addService("cabc", iBinder5);
                Trace.traceEnd(524288);
                traceBeginAndSlog("RuntimeService");
                ServiceManager.addService("runtime", new RuntimeService(context));
                traceEnd();
                Slog.i(TAG, "Torch Service");
                TorchManagerService.getInstance(context).systemReady();
                traceBeginAndSlog("StartHypnusService");
                Slog.i(TAG, "Hypnus Service");
                this.hypnusService = new HypnusService(context);
                ServiceManager.addService("hypnus", (IBinder) this.hypnusService);
                traceEnd();
                Slog.i(TAG, "Oppo Customize Service");
                this.oppoCustomize = new OppoCustomizeService(context);
                ServiceManager.addService("oppocustomize", (IBinder) this.oppoCustomize);
                if (this.mOnlyCore && context.getResources().getBoolean(17891450)) {
                }
                if (isWatch) {
                }
                traceBeginAndSlog("CertBlacklister");
                new CertBlacklister(context);
                traceEnd();
                traceBeginAndSlog("StartEmergencyAffordanceService");
                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                traceEnd();
                traceBeginAndSlog("StartDreamManager");
                this.mSystemServiceManager.startService(DreamManagerService.class);
                traceEnd();
                traceBeginAndSlog("AddGraphicsStatsService");
                ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, (IBinder) new GraphicsStatsService(context));
                traceEnd();
                if (CoverageService.ENABLED) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.companion_device_setup")) {
                }
                traceBeginAndSlog("StartRestrictionManager");
                this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                traceEnd();
                traceBeginAndSlog("StartMediaSessionService");
                Slog.i(TAG, "Media Session Service");
                this.mSystemServiceManager.startService(MediaSessionService.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                }
                traceBeginAndSlog("StartTvInputManager");
                this.mSystemServiceManager.startService(TvInputManagerService.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                }
                traceBeginAndSlog("StartMediaRouterService");
                mediaRouterService = new MediaRouterService(context);
                ServiceManager.addService("media_router", (IBinder) mediaRouterService);
                mediaRouter2 = mediaRouterService;
                traceEnd();
                hasFeatureFace = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.face");
                hasFeatureIris = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.iris");
                hasFeatureFingerprint = this.mPackageManager.hasSystemFeature("android.hardware.fingerprint");
                if (hasFeatureFace) {
                }
                if (hasFeatureIris) {
                }
                if (hasFeatureFingerprint) {
                }
                traceBeginAndSlog("StartBiometricService");
                Slog.i(TAG, "BiometricService Service");
                this.mBiometricService = (BiometricService) this.mSystemServiceManager.startService(BiometricService.class);
                this.mAlipayService = (AlipayService) this.mSystemServiceManager.startService(AlipayService.class);
                traceEnd();
                traceBeginAndSlog("StartBackgroundDexOptService");
                BackgroundDexOptService.schedule(context);
                traceEnd();
                if (OppoOperatorManager.SERVICE_ENABLED) {
                }
                if (!isWatch) {
                }
                Slog.i(TAG, "Oppo Engineer Service");
                this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                if (!isWatch) {
                }
                traceBeginAndSlog("StartShortcutServiceLifecycle");
                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                traceEnd();
                traceBeginAndSlog("StartLauncherAppsService");
                this.mSystemServiceManager.startService(LauncherAppsService.class);
                traceEnd();
                traceBeginAndSlog("StartCrossProfileAppsService");
                this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                traceEnd();
                location = location2;
                r19 = iBinder9;
                countryDetector = countryDetector2;
                networkTimeUpdater = networkTimeUpdater3;
                mediaRouter = mediaRouter2;
                networkStats = r15;
                if (!isWatch) {
                }
                if (isWatch) {
                }
                if (!disableSlices) {
                }
                if (!disableCameraService) {
                }
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                }
                traceBeginAndSlog("StartStatsCompanionService");
                this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                traceEnd();
                traceBeginAndSlog("StartIncidentCompanionService");
                this.mSystemServiceManager.startService(IncidentCompanionService.class);
                traceEnd();
                if (safeMode) {
                }
                traceBeginAndSlog("StartMmsService");
                MmsServiceBroker mmsService2222222222222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                }
                traceBeginAndSlog("StartClipboardService");
                this.mSystemServiceManager.startService(ClipboardService.class);
                traceEnd();
                traceBeginAndSlog("AppServiceManager");
                this.mSystemServiceManager.startService(AppBindingService.Lifecycle.class);
                traceEnd();
                sMtkSystemServerIns.startMtkOtherServices();
                this.mHelper.startOtherServices();
                traceBeginAndSlog("MakeVibratorServiceReady");
                vibrator.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeLockSettingsServiceReady");
                if (lockSettings != null) {
                }
                traceEnd();
                traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                traceEnd();
                traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                this.mSystemServiceManager.startBootPhase(500);
                traceEnd();
                traceBeginAndSlog("MakeWindowManagerServiceReady");
                Slog.i(TAG, "wms systemReady");
                wm.systemReady();
                traceEnd();
                if (safeMode) {
                }
                Configuration config2222222222222222222222222 = wm.computeNewConfiguration(0);
                DisplayMetrics metrics2222222222222222222222222 = new DisplayMetrics();
                ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics2222222222222222222222222);
                context.getResources().updateConfiguration(config2222222222222222222222222, metrics2222222222222222222222222);
                systemTheme = context.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                }
                traceBeginAndSlog("MakePowerManagerServiceReady");
                Slog.i(TAG, "power manager systemReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                traceEnd();
                if (this.mFingerprintService != null) {
                }
                if (this.mFaceService != null) {
                }
                if (this.mBiometricService != null) {
                }
                if (this.mAlipayService != null) {
                }
                traceBeginAndSlog("StartPermissionPolicyService");
                this.mSystemServiceManager.startService(PermissionPolicyService.class);
                traceEnd();
                if (this.mOppoOperatorManagerService != null) {
                }
                traceBeginAndSlog("MakePackageManagerServiceReady");
                Slog.i(TAG, "Package systemReady");
                this.mPackageManagerService.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeOppoCustomizeServiceReady");
                if (this.oppoCustomize != null) {
                }
                traceEnd();
                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                Slog.i(TAG, "DisplayManager systemReady");
                this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                traceEnd();
                this.mSystemServiceManager.setSafeMode(safeMode);
                traceBeginAndSlog("StartDeviceSpecificServices");
                String[] classes2222222222222222222222222 = this.mSystemContext.getResources().getStringArray(17236011);
                while (r6 < r7) {
                }
                traceEnd();
                traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                traceEnd();
                this.mHelper.systemReady();
                Slog.i(TAG, "Ams systemReady");
                this.mActivityManagerService.systemReady(new Runnable(context, wm, safeMode, iConnectivityManager, networkManagement, networkPolicy, r19, networkStats, location, countryDetector, networkTimeUpdater, inputManager, telephonyRegistry, mediaRouter, oppomodemService, mmsService2222222222222222222222222) {
                    /* class com.android.server.$$Lambda$SystemServer$Veuf5e15txqWe0TdLPFg29kfanQ */
                    private final /* synthetic */ Context f$1;
                    private final /* synthetic */ CountryDetectorService f$10;
                    private final /* synthetic */ NetworkTimeUpdateService f$11;
                    private final /* synthetic */ InputManagerService f$12;
                    private final /* synthetic */ TelephonyRegistry f$13;
                    private final /* synthetic */ MediaRouterService f$14;
                    private final /* synthetic */ OppomodemService f$15;
                    private final /* synthetic */ MmsServiceBroker f$16;
                    private final /* synthetic */ WindowManagerService f$2;
                    private final /* synthetic */ boolean f$3;
                    private final /* synthetic */ ConnectivityService f$4;
                    private final /* synthetic */ NetworkManagementService f$5;
                    private final /* synthetic */ NetworkPolicyManagerService f$6;
                    private final /* synthetic */ IpSecService f$7;
                    private final /* synthetic */ NetworkStatsService f$8;
                    private final /* synthetic */ LocationManagerService f$9;

                    {
                        this.f$1 = r4;
                        this.f$2 = r5;
                        this.f$3 = r6;
                        this.f$4 = r7;
                        this.f$5 = r8;
                        this.f$6 = r9;
                        this.f$7 = r10;
                        this.f$8 = r11;
                        this.f$9 = r12;
                        this.f$10 = r13;
                        this.f$11 = r14;
                        this.f$12 = r15;
                        this.f$13 = r16;
                        this.f$14 = r17;
                        this.f$15 = r18;
                        this.f$16 = r19;
                    }

                    public final void run() {
                        SystemServer.this.lambda$startOtherServices$4$SystemServer(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15, this.f$16);
                    }
                }, BOOT_TIMINGS_TRACE_LOG);
            }
            traceBeginAndSlog("CabcService");
            try {
                Slog.i(TAG, "Cabc Service");
                iBinder5 = new CabcService(context);
                ServiceManager.addService("cabc", iBinder5);
            } catch (Throwable th27) {
                e3 = th27;
                reportWtf("starting Cabc Service", e3);
                iBinder5 = iBinder2;
                Trace.traceEnd(524288);
                traceBeginAndSlog("RuntimeService");
                ServiceManager.addService("runtime", new RuntimeService(context));
                traceEnd();
                Slog.i(TAG, "Torch Service");
                TorchManagerService.getInstance(context).systemReady();
                traceBeginAndSlog("StartHypnusService");
                Slog.i(TAG, "Hypnus Service");
                this.hypnusService = new HypnusService(context);
                ServiceManager.addService("hypnus", (IBinder) this.hypnusService);
                traceEnd();
                Slog.i(TAG, "Oppo Customize Service");
                this.oppoCustomize = new OppoCustomizeService(context);
                ServiceManager.addService("oppocustomize", (IBinder) this.oppoCustomize);
                if (this.mOnlyCore && context.getResources().getBoolean(17891450)) {
                }
                if (isWatch) {
                }
                traceBeginAndSlog("CertBlacklister");
                new CertBlacklister(context);
                traceEnd();
                traceBeginAndSlog("StartEmergencyAffordanceService");
                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                traceEnd();
                traceBeginAndSlog("StartDreamManager");
                this.mSystemServiceManager.startService(DreamManagerService.class);
                traceEnd();
                traceBeginAndSlog("AddGraphicsStatsService");
                ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, (IBinder) new GraphicsStatsService(context));
                traceEnd();
                if (CoverageService.ENABLED) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.companion_device_setup")) {
                }
                traceBeginAndSlog("StartRestrictionManager");
                this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                traceEnd();
                traceBeginAndSlog("StartMediaSessionService");
                Slog.i(TAG, "Media Session Service");
                this.mSystemServiceManager.startService(MediaSessionService.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                }
                traceBeginAndSlog("StartTvInputManager");
                this.mSystemServiceManager.startService(TvInputManagerService.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                }
                traceBeginAndSlog("StartMediaRouterService");
                mediaRouterService = new MediaRouterService(context);
                ServiceManager.addService("media_router", (IBinder) mediaRouterService);
                mediaRouter2 = mediaRouterService;
                traceEnd();
                hasFeatureFace = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.face");
                hasFeatureIris = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.iris");
                hasFeatureFingerprint = this.mPackageManager.hasSystemFeature("android.hardware.fingerprint");
                if (hasFeatureFace) {
                }
                if (hasFeatureIris) {
                }
                if (hasFeatureFingerprint) {
                }
                traceBeginAndSlog("StartBiometricService");
                Slog.i(TAG, "BiometricService Service");
                this.mBiometricService = (BiometricService) this.mSystemServiceManager.startService(BiometricService.class);
                this.mAlipayService = (AlipayService) this.mSystemServiceManager.startService(AlipayService.class);
                traceEnd();
                traceBeginAndSlog("StartBackgroundDexOptService");
                BackgroundDexOptService.schedule(context);
                traceEnd();
                if (OppoOperatorManager.SERVICE_ENABLED) {
                }
                if (!isWatch) {
                }
                Slog.i(TAG, "Oppo Engineer Service");
                this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                if (!isWatch) {
                }
                traceBeginAndSlog("StartShortcutServiceLifecycle");
                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                traceEnd();
                traceBeginAndSlog("StartLauncherAppsService");
                this.mSystemServiceManager.startService(LauncherAppsService.class);
                traceEnd();
                traceBeginAndSlog("StartCrossProfileAppsService");
                this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                traceEnd();
                location = location2;
                r19 = iBinder9;
                countryDetector = countryDetector2;
                networkTimeUpdater = networkTimeUpdater3;
                mediaRouter = mediaRouter2;
                networkStats = r15;
                if (!isWatch) {
                }
                if (isWatch) {
                }
                if (!disableSlices) {
                }
                if (!disableCameraService) {
                }
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                }
                traceBeginAndSlog("StartStatsCompanionService");
                this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                traceEnd();
                traceBeginAndSlog("StartIncidentCompanionService");
                this.mSystemServiceManager.startService(IncidentCompanionService.class);
                traceEnd();
                if (safeMode) {
                }
                traceBeginAndSlog("StartMmsService");
                MmsServiceBroker mmsService22222222222222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                }
                traceBeginAndSlog("StartClipboardService");
                this.mSystemServiceManager.startService(ClipboardService.class);
                traceEnd();
                traceBeginAndSlog("AppServiceManager");
                this.mSystemServiceManager.startService(AppBindingService.Lifecycle.class);
                traceEnd();
                sMtkSystemServerIns.startMtkOtherServices();
                this.mHelper.startOtherServices();
                traceBeginAndSlog("MakeVibratorServiceReady");
                vibrator.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeLockSettingsServiceReady");
                if (lockSettings != null) {
                }
                traceEnd();
                traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                traceEnd();
                traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                this.mSystemServiceManager.startBootPhase(500);
                traceEnd();
                traceBeginAndSlog("MakeWindowManagerServiceReady");
                Slog.i(TAG, "wms systemReady");
                wm.systemReady();
                traceEnd();
                if (safeMode) {
                }
                Configuration config22222222222222222222222222 = wm.computeNewConfiguration(0);
                DisplayMetrics metrics22222222222222222222222222 = new DisplayMetrics();
                ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics22222222222222222222222222);
                context.getResources().updateConfiguration(config22222222222222222222222222, metrics22222222222222222222222222);
                systemTheme = context.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                }
                traceBeginAndSlog("MakePowerManagerServiceReady");
                Slog.i(TAG, "power manager systemReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                traceEnd();
                if (this.mFingerprintService != null) {
                }
                if (this.mFaceService != null) {
                }
                if (this.mBiometricService != null) {
                }
                if (this.mAlipayService != null) {
                }
                traceBeginAndSlog("StartPermissionPolicyService");
                this.mSystemServiceManager.startService(PermissionPolicyService.class);
                traceEnd();
                if (this.mOppoOperatorManagerService != null) {
                }
                traceBeginAndSlog("MakePackageManagerServiceReady");
                Slog.i(TAG, "Package systemReady");
                this.mPackageManagerService.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeOppoCustomizeServiceReady");
                if (this.oppoCustomize != null) {
                }
                traceEnd();
                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                Slog.i(TAG, "DisplayManager systemReady");
                this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                traceEnd();
                this.mSystemServiceManager.setSafeMode(safeMode);
                traceBeginAndSlog("StartDeviceSpecificServices");
                String[] classes22222222222222222222222222 = this.mSystemContext.getResources().getStringArray(17236011);
                while (r6 < r7) {
                }
                traceEnd();
                traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                traceEnd();
                this.mHelper.systemReady();
                Slog.i(TAG, "Ams systemReady");
                this.mActivityManagerService.systemReady(new Runnable(context, wm, safeMode, iConnectivityManager, networkManagement, networkPolicy, r19, networkStats, location, countryDetector, networkTimeUpdater, inputManager, telephonyRegistry, mediaRouter, oppomodemService, mmsService22222222222222222222222222) {
                    /* class com.android.server.$$Lambda$SystemServer$Veuf5e15txqWe0TdLPFg29kfanQ */
                    private final /* synthetic */ Context f$1;
                    private final /* synthetic */ CountryDetectorService f$10;
                    private final /* synthetic */ NetworkTimeUpdateService f$11;
                    private final /* synthetic */ InputManagerService f$12;
                    private final /* synthetic */ TelephonyRegistry f$13;
                    private final /* synthetic */ MediaRouterService f$14;
                    private final /* synthetic */ OppomodemService f$15;
                    private final /* synthetic */ MmsServiceBroker f$16;
                    private final /* synthetic */ WindowManagerService f$2;
                    private final /* synthetic */ boolean f$3;
                    private final /* synthetic */ ConnectivityService f$4;
                    private final /* synthetic */ NetworkManagementService f$5;
                    private final /* synthetic */ NetworkPolicyManagerService f$6;
                    private final /* synthetic */ IpSecService f$7;
                    private final /* synthetic */ NetworkStatsService f$8;
                    private final /* synthetic */ LocationManagerService f$9;

                    {
                        this.f$1 = r4;
                        this.f$2 = r5;
                        this.f$3 = r6;
                        this.f$4 = r7;
                        this.f$5 = r8;
                        this.f$6 = r9;
                        this.f$7 = r10;
                        this.f$8 = r11;
                        this.f$9 = r12;
                        this.f$10 = r13;
                        this.f$11 = r14;
                        this.f$12 = r15;
                        this.f$13 = r16;
                        this.f$14 = r17;
                        this.f$15 = r18;
                        this.f$16 = r19;
                    }

                    public final void run() {
                        SystemServer.this.lambda$startOtherServices$4$SystemServer(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15, this.f$16);
                    }
                }, BOOT_TIMINGS_TRACE_LOG);
            }
            Trace.traceEnd(524288);
            traceBeginAndSlog("RuntimeService");
            try {
                ServiceManager.addService("runtime", new RuntimeService(context));
            } catch (Throwable e45) {
                reportWtf("starting RuntimeService", e45);
            }
            traceEnd();
            try {
                Slog.i(TAG, "Torch Service");
                TorchManagerService.getInstance(context).systemReady();
            } catch (Throwable e46) {
                reportWtf("starting Torch Service", e46);
            }
            traceBeginAndSlog("StartHypnusService");
            try {
                Slog.i(TAG, "Hypnus Service");
                this.hypnusService = new HypnusService(context);
                ServiceManager.addService("hypnus", (IBinder) this.hypnusService);
            } catch (Throwable e47) {
                reportWtf("starting Hypnus Service", e47);
            }
            traceEnd();
            try {
                Slog.i(TAG, "Oppo Customize Service");
                this.oppoCustomize = new OppoCustomizeService(context);
                ServiceManager.addService("oppocustomize", (IBinder) this.oppoCustomize);
            } catch (Throwable e48) {
                Slog.e(TAG, "Failure starting Oppo Customize Service", e48);
            }
            if (this.mOnlyCore && context.getResources().getBoolean(17891450)) {
                traceBeginAndSlog("StartTimeZoneRulesManagerService");
                this.mSystemServiceManager.startService(TIME_ZONE_RULES_MANAGER_SERVICE_CLASS);
                traceEnd();
            }
            if (isWatch && !disableNetworkTime) {
                traceBeginAndSlog("StartNetworkTimeUpdateService");
                try {
                    networkTimeUpdater2 = new NewNetworkTimeUpdateService(context);
                    try {
                        StringBuilder sb3 = new StringBuilder();
                        try {
                            sb3.append("Using networkTimeUpdater class=");
                            sb3.append(networkTimeUpdater2.getClass());
                            Slog.d(TAG, sb3.toString());
                            ServiceManager.addService("network_time_update_service", networkTimeUpdater2);
                            networkTimeUpdater3 = networkTimeUpdater2;
                        } catch (Throwable th28) {
                            e2 = th28;
                            reportWtf("starting NetworkTimeUpdate service", e2);
                            networkTimeUpdater3 = networkTimeUpdater2;
                            traceEnd();
                            traceBeginAndSlog("CertBlacklister");
                            new CertBlacklister(context);
                            traceEnd();
                            traceBeginAndSlog("StartEmergencyAffordanceService");
                            this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                            traceEnd();
                            traceBeginAndSlog("StartDreamManager");
                            this.mSystemServiceManager.startService(DreamManagerService.class);
                            traceEnd();
                            traceBeginAndSlog("AddGraphicsStatsService");
                            ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, (IBinder) new GraphicsStatsService(context));
                            traceEnd();
                            if (CoverageService.ENABLED) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.companion_device_setup")) {
                            }
                            traceBeginAndSlog("StartRestrictionManager");
                            this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                            traceEnd();
                            traceBeginAndSlog("StartMediaSessionService");
                            Slog.i(TAG, "Media Session Service");
                            this.mSystemServiceManager.startService(MediaSessionService.class);
                            traceEnd();
                            if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                            }
                            traceBeginAndSlog("StartTvInputManager");
                            this.mSystemServiceManager.startService(TvInputManagerService.class);
                            traceEnd();
                            if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                            }
                            traceBeginAndSlog("StartMediaRouterService");
                            mediaRouterService = new MediaRouterService(context);
                            ServiceManager.addService("media_router", (IBinder) mediaRouterService);
                            mediaRouter2 = mediaRouterService;
                            traceEnd();
                            hasFeatureFace = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.face");
                            hasFeatureIris = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.iris");
                            hasFeatureFingerprint = this.mPackageManager.hasSystemFeature("android.hardware.fingerprint");
                            if (hasFeatureFace) {
                            }
                            if (hasFeatureIris) {
                            }
                            if (hasFeatureFingerprint) {
                            }
                            traceBeginAndSlog("StartBiometricService");
                            Slog.i(TAG, "BiometricService Service");
                            this.mBiometricService = (BiometricService) this.mSystemServiceManager.startService(BiometricService.class);
                            this.mAlipayService = (AlipayService) this.mSystemServiceManager.startService(AlipayService.class);
                            traceEnd();
                            traceBeginAndSlog("StartBackgroundDexOptService");
                            BackgroundDexOptService.schedule(context);
                            traceEnd();
                            if (OppoOperatorManager.SERVICE_ENABLED) {
                            }
                            if (!isWatch) {
                            }
                            Slog.i(TAG, "Oppo Engineer Service");
                            this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                            if (!isWatch) {
                            }
                            traceBeginAndSlog("StartShortcutServiceLifecycle");
                            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                            traceEnd();
                            traceBeginAndSlog("StartLauncherAppsService");
                            this.mSystemServiceManager.startService(LauncherAppsService.class);
                            traceEnd();
                            traceBeginAndSlog("StartCrossProfileAppsService");
                            this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                            traceEnd();
                            location = location2;
                            r19 = iBinder9;
                            countryDetector = countryDetector2;
                            networkTimeUpdater = networkTimeUpdater3;
                            mediaRouter = mediaRouter2;
                            networkStats = r15;
                            if (!isWatch) {
                            }
                            if (isWatch) {
                            }
                            if (!disableSlices) {
                            }
                            if (!disableCameraService) {
                            }
                            if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                            }
                            traceBeginAndSlog("StartStatsCompanionService");
                            this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                            traceEnd();
                            traceBeginAndSlog("StartIncidentCompanionService");
                            this.mSystemServiceManager.startService(IncidentCompanionService.class);
                            traceEnd();
                            if (safeMode) {
                            }
                            traceBeginAndSlog("StartMmsService");
                            MmsServiceBroker mmsService222222222222222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                            traceEnd();
                            if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                            }
                            traceBeginAndSlog("StartClipboardService");
                            this.mSystemServiceManager.startService(ClipboardService.class);
                            traceEnd();
                            traceBeginAndSlog("AppServiceManager");
                            this.mSystemServiceManager.startService(AppBindingService.Lifecycle.class);
                            traceEnd();
                            sMtkSystemServerIns.startMtkOtherServices();
                            this.mHelper.startOtherServices();
                            traceBeginAndSlog("MakeVibratorServiceReady");
                            vibrator.systemReady();
                            traceEnd();
                            traceBeginAndSlog("MakeLockSettingsServiceReady");
                            if (lockSettings != null) {
                            }
                            traceEnd();
                            traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                            traceEnd();
                            traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                            this.mSystemServiceManager.startBootPhase(500);
                            traceEnd();
                            traceBeginAndSlog("MakeWindowManagerServiceReady");
                            Slog.i(TAG, "wms systemReady");
                            wm.systemReady();
                            traceEnd();
                            if (safeMode) {
                            }
                            Configuration config222222222222222222222222222 = wm.computeNewConfiguration(0);
                            DisplayMetrics metrics222222222222222222222222222 = new DisplayMetrics();
                            ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics222222222222222222222222222);
                            context.getResources().updateConfiguration(config222222222222222222222222222, metrics222222222222222222222222222);
                            systemTheme = context.getTheme();
                            if (systemTheme.getChangingConfigurations() != 0) {
                            }
                            traceBeginAndSlog("MakePowerManagerServiceReady");
                            Slog.i(TAG, "power manager systemReady");
                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                            traceEnd();
                            if (this.mFingerprintService != null) {
                            }
                            if (this.mFaceService != null) {
                            }
                            if (this.mBiometricService != null) {
                            }
                            if (this.mAlipayService != null) {
                            }
                            traceBeginAndSlog("StartPermissionPolicyService");
                            this.mSystemServiceManager.startService(PermissionPolicyService.class);
                            traceEnd();
                            if (this.mOppoOperatorManagerService != null) {
                            }
                            traceBeginAndSlog("MakePackageManagerServiceReady");
                            Slog.i(TAG, "Package systemReady");
                            this.mPackageManagerService.systemReady();
                            traceEnd();
                            traceBeginAndSlog("MakeOppoCustomizeServiceReady");
                            if (this.oppoCustomize != null) {
                            }
                            traceEnd();
                            traceBeginAndSlog("MakeDisplayManagerServiceReady");
                            Slog.i(TAG, "DisplayManager systemReady");
                            this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                            traceEnd();
                            this.mSystemServiceManager.setSafeMode(safeMode);
                            traceBeginAndSlog("StartDeviceSpecificServices");
                            String[] classes222222222222222222222222222 = this.mSystemContext.getResources().getStringArray(17236011);
                            while (r6 < r7) {
                            }
                            traceEnd();
                            traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                            traceEnd();
                            this.mHelper.systemReady();
                            Slog.i(TAG, "Ams systemReady");
                            this.mActivityManagerService.systemReady(new Runnable(context, wm, safeMode, iConnectivityManager, networkManagement, networkPolicy, r19, networkStats, location, countryDetector, networkTimeUpdater, inputManager, telephonyRegistry, mediaRouter, oppomodemService, mmsService222222222222222222222222222) {
                                /* class com.android.server.$$Lambda$SystemServer$Veuf5e15txqWe0TdLPFg29kfanQ */
                                private final /* synthetic */ Context f$1;
                                private final /* synthetic */ CountryDetectorService f$10;
                                private final /* synthetic */ NetworkTimeUpdateService f$11;
                                private final /* synthetic */ InputManagerService f$12;
                                private final /* synthetic */ TelephonyRegistry f$13;
                                private final /* synthetic */ MediaRouterService f$14;
                                private final /* synthetic */ OppomodemService f$15;
                                private final /* synthetic */ MmsServiceBroker f$16;
                                private final /* synthetic */ WindowManagerService f$2;
                                private final /* synthetic */ boolean f$3;
                                private final /* synthetic */ ConnectivityService f$4;
                                private final /* synthetic */ NetworkManagementService f$5;
                                private final /* synthetic */ NetworkPolicyManagerService f$6;
                                private final /* synthetic */ IpSecService f$7;
                                private final /* synthetic */ NetworkStatsService f$8;
                                private final /* synthetic */ LocationManagerService f$9;

                                {
                                    this.f$1 = r4;
                                    this.f$2 = r5;
                                    this.f$3 = r6;
                                    this.f$4 = r7;
                                    this.f$5 = r8;
                                    this.f$6 = r9;
                                    this.f$7 = r10;
                                    this.f$8 = r11;
                                    this.f$9 = r12;
                                    this.f$10 = r13;
                                    this.f$11 = r14;
                                    this.f$12 = r15;
                                    this.f$13 = r16;
                                    this.f$14 = r17;
                                    this.f$15 = r18;
                                    this.f$16 = r19;
                                }

                                public final void run() {
                                    SystemServer.this.lambda$startOtherServices$4$SystemServer(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15, this.f$16);
                                }
                            }, BOOT_TIMINGS_TRACE_LOG);
                        }
                    } catch (Throwable th29) {
                        e2 = th29;
                        reportWtf("starting NetworkTimeUpdate service", e2);
                        networkTimeUpdater3 = networkTimeUpdater2;
                        traceEnd();
                        traceBeginAndSlog("CertBlacklister");
                        new CertBlacklister(context);
                        traceEnd();
                        traceBeginAndSlog("StartEmergencyAffordanceService");
                        this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                        traceEnd();
                        traceBeginAndSlog("StartDreamManager");
                        this.mSystemServiceManager.startService(DreamManagerService.class);
                        traceEnd();
                        traceBeginAndSlog("AddGraphicsStatsService");
                        ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, (IBinder) new GraphicsStatsService(context));
                        traceEnd();
                        if (CoverageService.ENABLED) {
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.companion_device_setup")) {
                        }
                        traceBeginAndSlog("StartRestrictionManager");
                        this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                        traceEnd();
                        traceBeginAndSlog("StartMediaSessionService");
                        Slog.i(TAG, "Media Session Service");
                        this.mSystemServiceManager.startService(MediaSessionService.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                        }
                        traceBeginAndSlog("StartTvInputManager");
                        this.mSystemServiceManager.startService(TvInputManagerService.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                        }
                        traceBeginAndSlog("StartMediaRouterService");
                        mediaRouterService = new MediaRouterService(context);
                        ServiceManager.addService("media_router", (IBinder) mediaRouterService);
                        mediaRouter2 = mediaRouterService;
                        traceEnd();
                        hasFeatureFace = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.face");
                        hasFeatureIris = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.iris");
                        hasFeatureFingerprint = this.mPackageManager.hasSystemFeature("android.hardware.fingerprint");
                        if (hasFeatureFace) {
                        }
                        if (hasFeatureIris) {
                        }
                        if (hasFeatureFingerprint) {
                        }
                        traceBeginAndSlog("StartBiometricService");
                        Slog.i(TAG, "BiometricService Service");
                        this.mBiometricService = (BiometricService) this.mSystemServiceManager.startService(BiometricService.class);
                        this.mAlipayService = (AlipayService) this.mSystemServiceManager.startService(AlipayService.class);
                        traceEnd();
                        traceBeginAndSlog("StartBackgroundDexOptService");
                        BackgroundDexOptService.schedule(context);
                        traceEnd();
                        if (OppoOperatorManager.SERVICE_ENABLED) {
                        }
                        if (!isWatch) {
                        }
                        Slog.i(TAG, "Oppo Engineer Service");
                        this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                        if (!isWatch) {
                        }
                        traceBeginAndSlog("StartShortcutServiceLifecycle");
                        this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                        traceEnd();
                        traceBeginAndSlog("StartLauncherAppsService");
                        this.mSystemServiceManager.startService(LauncherAppsService.class);
                        traceEnd();
                        traceBeginAndSlog("StartCrossProfileAppsService");
                        this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                        traceEnd();
                        location = location2;
                        r19 = iBinder9;
                        countryDetector = countryDetector2;
                        networkTimeUpdater = networkTimeUpdater3;
                        mediaRouter = mediaRouter2;
                        networkStats = r15;
                        if (!isWatch) {
                        }
                        if (isWatch) {
                        }
                        if (!disableSlices) {
                        }
                        if (!disableCameraService) {
                        }
                        if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                        }
                        traceBeginAndSlog("StartStatsCompanionService");
                        this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                        traceEnd();
                        traceBeginAndSlog("StartIncidentCompanionService");
                        this.mSystemServiceManager.startService(IncidentCompanionService.class);
                        traceEnd();
                        if (safeMode) {
                        }
                        traceBeginAndSlog("StartMmsService");
                        MmsServiceBroker mmsService2222222222222222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                        }
                        traceBeginAndSlog("StartClipboardService");
                        this.mSystemServiceManager.startService(ClipboardService.class);
                        traceEnd();
                        traceBeginAndSlog("AppServiceManager");
                        this.mSystemServiceManager.startService(AppBindingService.Lifecycle.class);
                        traceEnd();
                        sMtkSystemServerIns.startMtkOtherServices();
                        this.mHelper.startOtherServices();
                        traceBeginAndSlog("MakeVibratorServiceReady");
                        vibrator.systemReady();
                        traceEnd();
                        traceBeginAndSlog("MakeLockSettingsServiceReady");
                        if (lockSettings != null) {
                        }
                        traceEnd();
                        traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                        traceEnd();
                        traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                        this.mSystemServiceManager.startBootPhase(500);
                        traceEnd();
                        traceBeginAndSlog("MakeWindowManagerServiceReady");
                        Slog.i(TAG, "wms systemReady");
                        wm.systemReady();
                        traceEnd();
                        if (safeMode) {
                        }
                        Configuration config2222222222222222222222222222 = wm.computeNewConfiguration(0);
                        DisplayMetrics metrics2222222222222222222222222222 = new DisplayMetrics();
                        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics2222222222222222222222222222);
                        context.getResources().updateConfiguration(config2222222222222222222222222222, metrics2222222222222222222222222222);
                        systemTheme = context.getTheme();
                        if (systemTheme.getChangingConfigurations() != 0) {
                        }
                        traceBeginAndSlog("MakePowerManagerServiceReady");
                        Slog.i(TAG, "power manager systemReady");
                        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                        traceEnd();
                        if (this.mFingerprintService != null) {
                        }
                        if (this.mFaceService != null) {
                        }
                        if (this.mBiometricService != null) {
                        }
                        if (this.mAlipayService != null) {
                        }
                        traceBeginAndSlog("StartPermissionPolicyService");
                        this.mSystemServiceManager.startService(PermissionPolicyService.class);
                        traceEnd();
                        if (this.mOppoOperatorManagerService != null) {
                        }
                        traceBeginAndSlog("MakePackageManagerServiceReady");
                        Slog.i(TAG, "Package systemReady");
                        this.mPackageManagerService.systemReady();
                        traceEnd();
                        traceBeginAndSlog("MakeOppoCustomizeServiceReady");
                        if (this.oppoCustomize != null) {
                        }
                        traceEnd();
                        traceBeginAndSlog("MakeDisplayManagerServiceReady");
                        Slog.i(TAG, "DisplayManager systemReady");
                        this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                        traceEnd();
                        this.mSystemServiceManager.setSafeMode(safeMode);
                        traceBeginAndSlog("StartDeviceSpecificServices");
                        String[] classes2222222222222222222222222222 = this.mSystemContext.getResources().getStringArray(17236011);
                        while (r6 < r7) {
                        }
                        traceEnd();
                        traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                        traceEnd();
                        this.mHelper.systemReady();
                        Slog.i(TAG, "Ams systemReady");
                        this.mActivityManagerService.systemReady(new Runnable(context, wm, safeMode, iConnectivityManager, networkManagement, networkPolicy, r19, networkStats, location, countryDetector, networkTimeUpdater, inputManager, telephonyRegistry, mediaRouter, oppomodemService, mmsService2222222222222222222222222222) {
                            /* class com.android.server.$$Lambda$SystemServer$Veuf5e15txqWe0TdLPFg29kfanQ */
                            private final /* synthetic */ Context f$1;
                            private final /* synthetic */ CountryDetectorService f$10;
                            private final /* synthetic */ NetworkTimeUpdateService f$11;
                            private final /* synthetic */ InputManagerService f$12;
                            private final /* synthetic */ TelephonyRegistry f$13;
                            private final /* synthetic */ MediaRouterService f$14;
                            private final /* synthetic */ OppomodemService f$15;
                            private final /* synthetic */ MmsServiceBroker f$16;
                            private final /* synthetic */ WindowManagerService f$2;
                            private final /* synthetic */ boolean f$3;
                            private final /* synthetic */ ConnectivityService f$4;
                            private final /* synthetic */ NetworkManagementService f$5;
                            private final /* synthetic */ NetworkPolicyManagerService f$6;
                            private final /* synthetic */ IpSecService f$7;
                            private final /* synthetic */ NetworkStatsService f$8;
                            private final /* synthetic */ LocationManagerService f$9;

                            {
                                this.f$1 = r4;
                                this.f$2 = r5;
                                this.f$3 = r6;
                                this.f$4 = r7;
                                this.f$5 = r8;
                                this.f$6 = r9;
                                this.f$7 = r10;
                                this.f$8 = r11;
                                this.f$9 = r12;
                                this.f$10 = r13;
                                this.f$11 = r14;
                                this.f$12 = r15;
                                this.f$13 = r16;
                                this.f$14 = r17;
                                this.f$15 = r18;
                                this.f$16 = r19;
                            }

                            public final void run() {
                                SystemServer.this.lambda$startOtherServices$4$SystemServer(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15, this.f$16);
                            }
                        }, BOOT_TIMINGS_TRACE_LOG);
                    }
                } catch (Throwable th30) {
                    e2 = th30;
                    networkTimeUpdater2 = null;
                    reportWtf("starting NetworkTimeUpdate service", e2);
                    networkTimeUpdater3 = networkTimeUpdater2;
                    traceEnd();
                    traceBeginAndSlog("CertBlacklister");
                    new CertBlacklister(context);
                    traceEnd();
                    traceBeginAndSlog("StartEmergencyAffordanceService");
                    this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                    traceEnd();
                    traceBeginAndSlog("StartDreamManager");
                    this.mSystemServiceManager.startService(DreamManagerService.class);
                    traceEnd();
                    traceBeginAndSlog("AddGraphicsStatsService");
                    ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, (IBinder) new GraphicsStatsService(context));
                    traceEnd();
                    if (CoverageService.ENABLED) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.companion_device_setup")) {
                    }
                    traceBeginAndSlog("StartRestrictionManager");
                    this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                    traceEnd();
                    traceBeginAndSlog("StartMediaSessionService");
                    Slog.i(TAG, "Media Session Service");
                    this.mSystemServiceManager.startService(MediaSessionService.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                    }
                    traceBeginAndSlog("StartTvInputManager");
                    this.mSystemServiceManager.startService(TvInputManagerService.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                    }
                    traceBeginAndSlog("StartMediaRouterService");
                    mediaRouterService = new MediaRouterService(context);
                    ServiceManager.addService("media_router", (IBinder) mediaRouterService);
                    mediaRouter2 = mediaRouterService;
                    traceEnd();
                    hasFeatureFace = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.face");
                    hasFeatureIris = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.iris");
                    hasFeatureFingerprint = this.mPackageManager.hasSystemFeature("android.hardware.fingerprint");
                    if (hasFeatureFace) {
                    }
                    if (hasFeatureIris) {
                    }
                    if (hasFeatureFingerprint) {
                    }
                    traceBeginAndSlog("StartBiometricService");
                    Slog.i(TAG, "BiometricService Service");
                    this.mBiometricService = (BiometricService) this.mSystemServiceManager.startService(BiometricService.class);
                    this.mAlipayService = (AlipayService) this.mSystemServiceManager.startService(AlipayService.class);
                    traceEnd();
                    traceBeginAndSlog("StartBackgroundDexOptService");
                    BackgroundDexOptService.schedule(context);
                    traceEnd();
                    if (OppoOperatorManager.SERVICE_ENABLED) {
                    }
                    if (!isWatch) {
                    }
                    Slog.i(TAG, "Oppo Engineer Service");
                    this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                    if (!isWatch) {
                    }
                    traceBeginAndSlog("StartShortcutServiceLifecycle");
                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                    traceEnd();
                    traceBeginAndSlog("StartLauncherAppsService");
                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                    traceEnd();
                    traceBeginAndSlog("StartCrossProfileAppsService");
                    this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                    traceEnd();
                    location = location2;
                    r19 = iBinder9;
                    countryDetector = countryDetector2;
                    networkTimeUpdater = networkTimeUpdater3;
                    mediaRouter = mediaRouter2;
                    networkStats = r15;
                    if (!isWatch) {
                    }
                    if (isWatch) {
                    }
                    if (!disableSlices) {
                    }
                    if (!disableCameraService) {
                    }
                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                    }
                    traceBeginAndSlog("StartStatsCompanionService");
                    this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                    traceEnd();
                    traceBeginAndSlog("StartIncidentCompanionService");
                    this.mSystemServiceManager.startService(IncidentCompanionService.class);
                    traceEnd();
                    if (safeMode) {
                    }
                    traceBeginAndSlog("StartMmsService");
                    MmsServiceBroker mmsService22222222222222222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                    }
                    traceBeginAndSlog("StartClipboardService");
                    this.mSystemServiceManager.startService(ClipboardService.class);
                    traceEnd();
                    traceBeginAndSlog("AppServiceManager");
                    this.mSystemServiceManager.startService(AppBindingService.Lifecycle.class);
                    traceEnd();
                    sMtkSystemServerIns.startMtkOtherServices();
                    this.mHelper.startOtherServices();
                    traceBeginAndSlog("MakeVibratorServiceReady");
                    vibrator.systemReady();
                    traceEnd();
                    traceBeginAndSlog("MakeLockSettingsServiceReady");
                    if (lockSettings != null) {
                    }
                    traceEnd();
                    traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                    traceEnd();
                    traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                    this.mSystemServiceManager.startBootPhase(500);
                    traceEnd();
                    traceBeginAndSlog("MakeWindowManagerServiceReady");
                    Slog.i(TAG, "wms systemReady");
                    wm.systemReady();
                    traceEnd();
                    if (safeMode) {
                    }
                    Configuration config22222222222222222222222222222 = wm.computeNewConfiguration(0);
                    DisplayMetrics metrics22222222222222222222222222222 = new DisplayMetrics();
                    ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics22222222222222222222222222222);
                    context.getResources().updateConfiguration(config22222222222222222222222222222, metrics22222222222222222222222222222);
                    systemTheme = context.getTheme();
                    if (systemTheme.getChangingConfigurations() != 0) {
                    }
                    traceBeginAndSlog("MakePowerManagerServiceReady");
                    Slog.i(TAG, "power manager systemReady");
                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                    traceEnd();
                    if (this.mFingerprintService != null) {
                    }
                    if (this.mFaceService != null) {
                    }
                    if (this.mBiometricService != null) {
                    }
                    if (this.mAlipayService != null) {
                    }
                    traceBeginAndSlog("StartPermissionPolicyService");
                    this.mSystemServiceManager.startService(PermissionPolicyService.class);
                    traceEnd();
                    if (this.mOppoOperatorManagerService != null) {
                    }
                    traceBeginAndSlog("MakePackageManagerServiceReady");
                    Slog.i(TAG, "Package systemReady");
                    this.mPackageManagerService.systemReady();
                    traceEnd();
                    traceBeginAndSlog("MakeOppoCustomizeServiceReady");
                    if (this.oppoCustomize != null) {
                    }
                    traceEnd();
                    traceBeginAndSlog("MakeDisplayManagerServiceReady");
                    Slog.i(TAG, "DisplayManager systemReady");
                    this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                    traceEnd();
                    this.mSystemServiceManager.setSafeMode(safeMode);
                    traceBeginAndSlog("StartDeviceSpecificServices");
                    String[] classes22222222222222222222222222222 = this.mSystemContext.getResources().getStringArray(17236011);
                    while (r6 < r7) {
                    }
                    traceEnd();
                    traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                    traceEnd();
                    this.mHelper.systemReady();
                    Slog.i(TAG, "Ams systemReady");
                    this.mActivityManagerService.systemReady(new Runnable(context, wm, safeMode, iConnectivityManager, networkManagement, networkPolicy, r19, networkStats, location, countryDetector, networkTimeUpdater, inputManager, telephonyRegistry, mediaRouter, oppomodemService, mmsService22222222222222222222222222222) {
                        /* class com.android.server.$$Lambda$SystemServer$Veuf5e15txqWe0TdLPFg29kfanQ */
                        private final /* synthetic */ Context f$1;
                        private final /* synthetic */ CountryDetectorService f$10;
                        private final /* synthetic */ NetworkTimeUpdateService f$11;
                        private final /* synthetic */ InputManagerService f$12;
                        private final /* synthetic */ TelephonyRegistry f$13;
                        private final /* synthetic */ MediaRouterService f$14;
                        private final /* synthetic */ OppomodemService f$15;
                        private final /* synthetic */ MmsServiceBroker f$16;
                        private final /* synthetic */ WindowManagerService f$2;
                        private final /* synthetic */ boolean f$3;
                        private final /* synthetic */ ConnectivityService f$4;
                        private final /* synthetic */ NetworkManagementService f$5;
                        private final /* synthetic */ NetworkPolicyManagerService f$6;
                        private final /* synthetic */ IpSecService f$7;
                        private final /* synthetic */ NetworkStatsService f$8;
                        private final /* synthetic */ LocationManagerService f$9;

                        {
                            this.f$1 = r4;
                            this.f$2 = r5;
                            this.f$3 = r6;
                            this.f$4 = r7;
                            this.f$5 = r8;
                            this.f$6 = r9;
                            this.f$7 = r10;
                            this.f$8 = r11;
                            this.f$9 = r12;
                            this.f$10 = r13;
                            this.f$11 = r14;
                            this.f$12 = r15;
                            this.f$13 = r16;
                            this.f$14 = r17;
                            this.f$15 = r18;
                            this.f$16 = r19;
                        }

                        public final void run() {
                            SystemServer.this.lambda$startOtherServices$4$SystemServer(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15, this.f$16);
                        }
                    }, BOOT_TIMINGS_TRACE_LOG);
                }
                traceEnd();
            }
            traceBeginAndSlog("CertBlacklister");
            try {
                new CertBlacklister(context);
            } catch (Throwable e49) {
                reportWtf("starting CertBlacklister", e49);
            }
            traceEnd();
            traceBeginAndSlog("StartEmergencyAffordanceService");
            this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
            traceEnd();
            traceBeginAndSlog("StartDreamManager");
            this.mSystemServiceManager.startService(DreamManagerService.class);
            traceEnd();
            traceBeginAndSlog("AddGraphicsStatsService");
            ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, (IBinder) new GraphicsStatsService(context));
            traceEnd();
            if (CoverageService.ENABLED) {
                traceBeginAndSlog("AddCoverageService");
                ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                traceEnd();
            }
            if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                traceBeginAndSlog("StartPrintManager");
                this.mSystemServiceManager.startService(PRINT_MANAGER_SERVICE_CLASS);
                traceEnd();
            }
            if (this.mPackageManager.hasSystemFeature("android.software.companion_device_setup")) {
                traceBeginAndSlog("StartCompanionDeviceManager");
                this.mSystemServiceManager.startService(COMPANION_DEVICE_MANAGER_SERVICE_CLASS);
                traceEnd();
            }
            traceBeginAndSlog("StartRestrictionManager");
            this.mSystemServiceManager.startService(RestrictionsManagerService.class);
            traceEnd();
            traceBeginAndSlog("StartMediaSessionService");
            Slog.i(TAG, "Media Session Service");
            this.mSystemServiceManager.startService(MediaSessionService.class);
            traceEnd();
            if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                traceBeginAndSlog("StartHdmiControlService");
                this.mSystemServiceManager.startService(HdmiControlService.class);
                traceEnd();
            }
            if (this.mPackageManager.hasSystemFeature("android.software.live_tv") || this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                traceBeginAndSlog("StartTvInputManager");
                this.mSystemServiceManager.startService(TvInputManagerService.class);
                traceEnd();
            }
            if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                traceBeginAndSlog("StartMediaResourceMonitor");
                this.mSystemServiceManager.startService(MediaResourceMonitorService.class);
                traceEnd();
            }
            if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                traceBeginAndSlog("StartTvRemoteService");
                this.mSystemServiceManager.startService(TvRemoteService.class);
                traceEnd();
            }
            traceBeginAndSlog("StartMediaRouterService");
            try {
                mediaRouterService = new MediaRouterService(context);
                ServiceManager.addService("media_router", (IBinder) mediaRouterService);
                mediaRouter2 = mediaRouterService;
            } catch (Throwable th31) {
                e = th31;
                mediaRouter3 = null;
                reportWtf("starting MediaRouterService", e);
                mediaRouter2 = mediaRouter3;
                traceEnd();
                hasFeatureFace = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.face");
                hasFeatureIris = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.iris");
                hasFeatureFingerprint = this.mPackageManager.hasSystemFeature("android.hardware.fingerprint");
                if (hasFeatureFace) {
                }
                if (hasFeatureIris) {
                }
                if (hasFeatureFingerprint) {
                }
                traceBeginAndSlog("StartBiometricService");
                Slog.i(TAG, "BiometricService Service");
                this.mBiometricService = (BiometricService) this.mSystemServiceManager.startService(BiometricService.class);
                this.mAlipayService = (AlipayService) this.mSystemServiceManager.startService(AlipayService.class);
                traceEnd();
                traceBeginAndSlog("StartBackgroundDexOptService");
                BackgroundDexOptService.schedule(context);
                traceEnd();
                if (OppoOperatorManager.SERVICE_ENABLED) {
                }
                if (!isWatch) {
                }
                Slog.i(TAG, "Oppo Engineer Service");
                this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                if (!isWatch) {
                }
                traceBeginAndSlog("StartShortcutServiceLifecycle");
                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                traceEnd();
                traceBeginAndSlog("StartLauncherAppsService");
                this.mSystemServiceManager.startService(LauncherAppsService.class);
                traceEnd();
                traceBeginAndSlog("StartCrossProfileAppsService");
                this.mSystemServiceManager.startService(CrossProfileAppsService.class);
                traceEnd();
                location = location2;
                r19 = iBinder9;
                countryDetector = countryDetector2;
                networkTimeUpdater = networkTimeUpdater3;
                mediaRouter = mediaRouter2;
                networkStats = r15;
                if (!isWatch) {
                }
                if (isWatch) {
                }
                if (!disableSlices) {
                }
                if (!disableCameraService) {
                }
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
                }
                traceBeginAndSlog("StartStatsCompanionService");
                this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
                traceEnd();
                traceBeginAndSlog("StartIncidentCompanionService");
                this.mSystemServiceManager.startService(IncidentCompanionService.class);
                traceEnd();
                if (safeMode) {
                }
                traceBeginAndSlog("StartMmsService");
                MmsServiceBroker mmsService222222222222222222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                }
                traceBeginAndSlog("StartClipboardService");
                this.mSystemServiceManager.startService(ClipboardService.class);
                traceEnd();
                traceBeginAndSlog("AppServiceManager");
                this.mSystemServiceManager.startService(AppBindingService.Lifecycle.class);
                traceEnd();
                sMtkSystemServerIns.startMtkOtherServices();
                this.mHelper.startOtherServices();
                traceBeginAndSlog("MakeVibratorServiceReady");
                vibrator.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeLockSettingsServiceReady");
                if (lockSettings != null) {
                }
                traceEnd();
                traceBeginAndSlog("StartBootPhaseLockSettingsReady");
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                traceEnd();
                traceBeginAndSlog("StartBootPhaseSystemServicesReady");
                this.mSystemServiceManager.startBootPhase(500);
                traceEnd();
                traceBeginAndSlog("MakeWindowManagerServiceReady");
                Slog.i(TAG, "wms systemReady");
                wm.systemReady();
                traceEnd();
                if (safeMode) {
                }
                Configuration config222222222222222222222222222222 = wm.computeNewConfiguration(0);
                DisplayMetrics metrics222222222222222222222222222222 = new DisplayMetrics();
                ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics222222222222222222222222222222);
                context.getResources().updateConfiguration(config222222222222222222222222222222, metrics222222222222222222222222222222);
                systemTheme = context.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                }
                traceBeginAndSlog("MakePowerManagerServiceReady");
                Slog.i(TAG, "power manager systemReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                traceEnd();
                if (this.mFingerprintService != null) {
                }
                if (this.mFaceService != null) {
                }
                if (this.mBiometricService != null) {
                }
                if (this.mAlipayService != null) {
                }
                traceBeginAndSlog("StartPermissionPolicyService");
                this.mSystemServiceManager.startService(PermissionPolicyService.class);
                traceEnd();
                if (this.mOppoOperatorManagerService != null) {
                }
                traceBeginAndSlog("MakePackageManagerServiceReady");
                Slog.i(TAG, "Package systemReady");
                this.mPackageManagerService.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeOppoCustomizeServiceReady");
                if (this.oppoCustomize != null) {
                }
                traceEnd();
                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                Slog.i(TAG, "DisplayManager systemReady");
                this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
                traceEnd();
                this.mSystemServiceManager.setSafeMode(safeMode);
                traceBeginAndSlog("StartDeviceSpecificServices");
                String[] classes222222222222222222222222222222 = this.mSystemContext.getResources().getStringArray(17236011);
                while (r6 < r7) {
                }
                traceEnd();
                traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
                traceEnd();
                this.mHelper.systemReady();
                Slog.i(TAG, "Ams systemReady");
                this.mActivityManagerService.systemReady(new Runnable(context, wm, safeMode, iConnectivityManager, networkManagement, networkPolicy, r19, networkStats, location, countryDetector, networkTimeUpdater, inputManager, telephonyRegistry, mediaRouter, oppomodemService, mmsService222222222222222222222222222222) {
                    /* class com.android.server.$$Lambda$SystemServer$Veuf5e15txqWe0TdLPFg29kfanQ */
                    private final /* synthetic */ Context f$1;
                    private final /* synthetic */ CountryDetectorService f$10;
                    private final /* synthetic */ NetworkTimeUpdateService f$11;
                    private final /* synthetic */ InputManagerService f$12;
                    private final /* synthetic */ TelephonyRegistry f$13;
                    private final /* synthetic */ MediaRouterService f$14;
                    private final /* synthetic */ OppomodemService f$15;
                    private final /* synthetic */ MmsServiceBroker f$16;
                    private final /* synthetic */ WindowManagerService f$2;
                    private final /* synthetic */ boolean f$3;
                    private final /* synthetic */ ConnectivityService f$4;
                    private final /* synthetic */ NetworkManagementService f$5;
                    private final /* synthetic */ NetworkPolicyManagerService f$6;
                    private final /* synthetic */ IpSecService f$7;
                    private final /* synthetic */ NetworkStatsService f$8;
                    private final /* synthetic */ LocationManagerService f$9;

                    {
                        this.f$1 = r4;
                        this.f$2 = r5;
                        this.f$3 = r6;
                        this.f$4 = r7;
                        this.f$5 = r8;
                        this.f$6 = r9;
                        this.f$7 = r10;
                        this.f$8 = r11;
                        this.f$9 = r12;
                        this.f$10 = r13;
                        this.f$11 = r14;
                        this.f$12 = r15;
                        this.f$13 = r16;
                        this.f$14 = r17;
                        this.f$15 = r18;
                        this.f$16 = r19;
                    }

                    public final void run() {
                        SystemServer.this.lambda$startOtherServices$4$SystemServer(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15, this.f$16);
                    }
                }, BOOT_TIMINGS_TRACE_LOG);
            }
            traceEnd();
            hasFeatureFace = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.face");
            hasFeatureIris = this.mPackageManager.hasSystemFeature("android.hardware.biometrics.iris");
            hasFeatureFingerprint = this.mPackageManager.hasSystemFeature("android.hardware.fingerprint");
            if (hasFeatureFace) {
                traceBeginAndSlog("StartFaceSensor");
                Slog.i(TAG, "Face Service");
                oppomodemService = oppomodemService3;
                this.mFaceService = (FaceService) this.mSystemServiceManager.startService(FaceService.class);
                traceEnd();
            } else {
                oppomodemService = oppomodemService3;
            }
            if (hasFeatureIris) {
                traceBeginAndSlog("StartIrisSensor");
                this.mSystemServiceManager.startService(IrisService.class);
                traceEnd();
            }
            if (hasFeatureFingerprint) {
                traceBeginAndSlog("StartFingerprintSensor");
                Slog.i(TAG, "Fingerprint Service");
                this.mFingerprintService = (FingerprintService) this.mSystemServiceManager.startService(FingerprintService.class);
                traceEnd();
            }
            if (hasFeatureFace || hasFeatureIris || hasFeatureFingerprint) {
                traceBeginAndSlog("StartBiometricService");
                Slog.i(TAG, "BiometricService Service");
                this.mBiometricService = (BiometricService) this.mSystemServiceManager.startService(BiometricService.class);
                this.mAlipayService = (AlipayService) this.mSystemServiceManager.startService(AlipayService.class);
                traceEnd();
            }
            traceBeginAndSlog("StartBackgroundDexOptService");
            try {
                BackgroundDexOptService.schedule(context);
            } catch (Throwable e50) {
                reportWtf("starting StartBackgroundDexOptService", e50);
            }
            traceEnd();
            if (OppoOperatorManager.SERVICE_ENABLED) {
                try {
                    Slog.i(TAG, "OppoOperatorManagerService start");
                    this.mOppoOperatorManagerService = (OppoOperatorManagerService) this.mSystemServiceManager.startService(OppoOperatorManagerService.class);
                } catch (Throwable e51) {
                    Slog.e(TAG, "Failure starting OppoOperatorManagerService ", e51);
                }
            }
            if (!isWatch) {
                traceBeginAndSlog("StartDynamicCodeLoggingService");
                try {
                    DynamicCodeLoggingService.schedule(context);
                } catch (Throwable e52) {
                    reportWtf("starting DynamicCodeLoggingService", e52);
                }
                traceEnd();
            }
            Slog.i(TAG, "Oppo Engineer Service");
            this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
            if (!isWatch) {
                traceBeginAndSlog("StartPruneInstantAppsJobService");
                try {
                    PruneInstantAppsJobService.schedule(context);
                } catch (Throwable e53) {
                    reportWtf("StartPruneInstantAppsJobService", e53);
                }
                traceEnd();
            }
            traceBeginAndSlog("StartShortcutServiceLifecycle");
            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
            traceEnd();
            traceBeginAndSlog("StartLauncherAppsService");
            this.mSystemServiceManager.startService(LauncherAppsService.class);
            traceEnd();
            traceBeginAndSlog("StartCrossProfileAppsService");
            this.mSystemServiceManager.startService(CrossProfileAppsService.class);
            traceEnd();
            location = location2;
            r19 = iBinder9;
            countryDetector = countryDetector2;
            networkTimeUpdater = networkTimeUpdater3;
            mediaRouter = mediaRouter2;
            networkStats = r15;
        } else {
            oppomodemService = null;
            networkPolicy = null;
            iConnectivityManager = null;
            location = null;
            countryDetector = null;
            mediaRouter = null;
            r19 = 0;
            networkTimeUpdater = null;
            networkStats = null;
        }
        if (!isWatch) {
            traceBeginAndSlog("StartMediaProjectionManager");
            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
            traceEnd();
        }
        if (isWatch) {
            traceBeginAndSlog("StartWearPowerService");
            this.mSystemServiceManager.startService(WEAR_POWER_SERVICE_CLASS);
            traceEnd();
            traceBeginAndSlog("StartWearConnectivityService");
            this.mSystemServiceManager.startService(WEAR_CONNECTIVITY_SERVICE_CLASS);
            traceEnd();
            traceBeginAndSlog("StartWearDisplayService");
            this.mSystemServiceManager.startService(WEAR_DISPLAY_SERVICE_CLASS);
            traceEnd();
            traceBeginAndSlog("StartWearTimeService");
            this.mSystemServiceManager.startService(WEAR_TIME_SERVICE_CLASS);
            traceEnd();
            if (enableLeftyService) {
                traceBeginAndSlog("StartWearLeftyService");
                this.mSystemServiceManager.startService(WEAR_LEFTY_SERVICE_CLASS);
                traceEnd();
            }
            traceBeginAndSlog("StartWearGlobalActionsService");
            this.mSystemServiceManager.startService(WEAR_GLOBAL_ACTIONS_SERVICE_CLASS);
            traceEnd();
        }
        if (!disableSlices) {
            traceBeginAndSlog("StartSliceManagerService");
            this.mSystemServiceManager.startService(SLICE_MANAGER_SERVICE_CLASS);
            traceEnd();
        }
        if (!disableCameraService) {
            traceBeginAndSlog("StartCameraServiceProxy");
            this.mSystemServiceManager.startService(CameraServiceProxy.class);
            traceEnd();
        }
        if (context.getPackageManager().hasSystemFeature("android.hardware.type.embedded")) {
            traceBeginAndSlog("StartIoTSystemService");
            this.mSystemServiceManager.startService(IOT_SERVICE_CLASS);
            traceEnd();
        }
        traceBeginAndSlog("StartStatsCompanionService");
        this.mSystemServiceManager.startService(StatsCompanionService.Lifecycle.class);
        traceEnd();
        traceBeginAndSlog("StartIncidentCompanionService");
        this.mSystemServiceManager.startService(IncidentCompanionService.class);
        traceEnd();
        if (safeMode) {
            this.mActivityManagerService.enterSafeMode();
        }
        traceBeginAndSlog("StartMmsService");
        MmsServiceBroker mmsService2222222222222222222222222222222 = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
        traceEnd();
        if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
            traceBeginAndSlog("StartAutoFillService");
            this.mSystemServiceManager.startService(AUTO_FILL_MANAGER_SERVICE_CLASS);
            traceEnd();
        }
        traceBeginAndSlog("StartClipboardService");
        this.mSystemServiceManager.startService(ClipboardService.class);
        traceEnd();
        traceBeginAndSlog("AppServiceManager");
        this.mSystemServiceManager.startService(AppBindingService.Lifecycle.class);
        traceEnd();
        sMtkSystemServerIns.startMtkOtherServices();
        this.mHelper.startOtherServices();
        traceBeginAndSlog("MakeVibratorServiceReady");
        try {
            vibrator.systemReady();
        } catch (Throwable e54) {
            reportWtf("making Vibrator Service ready", e54);
        }
        traceEnd();
        traceBeginAndSlog("MakeLockSettingsServiceReady");
        if (lockSettings != null) {
            try {
                lockSettings.systemReady();
            } catch (Throwable e55) {
                reportWtf("making Lock Settings Service ready", e55);
            }
        }
        traceEnd();
        traceBeginAndSlog("StartBootPhaseLockSettingsReady");
        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
        traceEnd();
        traceBeginAndSlog("StartBootPhaseSystemServicesReady");
        this.mSystemServiceManager.startBootPhase(500);
        traceEnd();
        traceBeginAndSlog("MakeWindowManagerServiceReady");
        try {
            Slog.i(TAG, "wms systemReady");
            wm.systemReady();
        } catch (Throwable e56) {
            reportWtf("making Window Manager Service ready", e56);
        }
        traceEnd();
        if (safeMode) {
            this.mActivityManagerService.showSafeModeOverlay();
        }
        Configuration config2222222222222222222222222222222 = wm.computeNewConfiguration(0);
        DisplayMetrics metrics2222222222222222222222222222222 = new DisplayMetrics();
        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics2222222222222222222222222222222);
        context.getResources().updateConfiguration(config2222222222222222222222222222222, metrics2222222222222222222222222222222);
        systemTheme = context.getTheme();
        if (systemTheme.getChangingConfigurations() != 0) {
            systemTheme.rebase();
        }
        traceBeginAndSlog("MakePowerManagerServiceReady");
        try {
            Slog.i(TAG, "power manager systemReady");
            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
        } catch (Throwable e57) {
            reportWtf("making Power Manager Service ready", e57);
        }
        traceEnd();
        try {
            if (this.mFingerprintService != null) {
                Slog.i(TAG, "Fingerprint systemReady");
                this.mFingerprintService.systemReady();
            }
        } catch (Throwable e58) {
            reportWtf("making Fingerprint Service ready", e58);
        }
        try {
            if (this.mFaceService != null) {
                Slog.i(TAG, "Face Service systemReady");
                this.mFaceService.systemReady();
            }
        } catch (Throwable e59) {
            reportWtf("making Face Service ready", e59);
        }
        try {
            if (this.mBiometricService != null) {
                Slog.i(TAG, "Biometrics systemReady");
                this.mBiometricService.systemReady();
            }
        } catch (Throwable e60) {
            reportWtf("making Biometrics Service ready", e60);
        }
        try {
            if (this.mAlipayService != null) {
                Slog.i(TAG, "AlipayService systemReady");
                this.mAlipayService.systemReady();
            }
        } catch (Throwable e61) {
            reportWtf("making AlipayService Service ready", e61);
        }
        traceBeginAndSlog("StartPermissionPolicyService");
        this.mSystemServiceManager.startService(PermissionPolicyService.class);
        traceEnd();
        try {
            if (this.mOppoOperatorManagerService != null) {
                Slog.i(TAG, "OppoOperatorManagerService systemReady");
                this.mOppoOperatorManagerService.systemReady();
            }
        } catch (Throwable e62) {
            reportWtf("making Operator Service ready", e62);
        }
        traceBeginAndSlog("MakePackageManagerServiceReady");
        Slog.i(TAG, "Package systemReady");
        this.mPackageManagerService.systemReady();
        traceEnd();
        traceBeginAndSlog("MakeOppoCustomizeServiceReady");
        try {
            if (this.oppoCustomize != null) {
                this.oppoCustomize.systemReady();
            }
        } catch (Throwable e63) {
            reportWtf("making OppoCustomizeService ready", e63);
        }
        traceEnd();
        traceBeginAndSlog("MakeDisplayManagerServiceReady");
        try {
            Slog.i(TAG, "DisplayManager systemReady");
            this.mDisplayManagerService.systemReady(safeMode, this.mOnlyCore);
        } catch (Throwable e64) {
            reportWtf("making Display Manager Service ready", e64);
        }
        traceEnd();
        this.mSystemServiceManager.setSafeMode(safeMode);
        traceBeginAndSlog("StartDeviceSpecificServices");
        String[] classes2222222222222222222222222222222 = this.mSystemContext.getResources().getStringArray(17236011);
        for (String className2 : classes2222222222222222222222222222222) {
            traceBeginAndSlog("StartDeviceSpecificServices " + className2);
            try {
                this.mSystemServiceManager.startService(className2);
            } catch (Throwable e65) {
                reportWtf("starting " + className2, e65);
            }
            traceEnd();
        }
        traceEnd();
        traceBeginAndSlog("StartBootPhaseDeviceSpecificServicesReady");
        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_DEVICE_SPECIFIC_SERVICES_READY);
        traceEnd();
        this.mHelper.systemReady();
        Slog.i(TAG, "Ams systemReady");
        this.mActivityManagerService.systemReady(new Runnable(context, wm, safeMode, iConnectivityManager, networkManagement, networkPolicy, r19, networkStats, location, countryDetector, networkTimeUpdater, inputManager, telephonyRegistry, mediaRouter, oppomodemService, mmsService2222222222222222222222222222222) {
            /* class com.android.server.$$Lambda$SystemServer$Veuf5e15txqWe0TdLPFg29kfanQ */
            private final /* synthetic */ Context f$1;
            private final /* synthetic */ CountryDetectorService f$10;
            private final /* synthetic */ NetworkTimeUpdateService f$11;
            private final /* synthetic */ InputManagerService f$12;
            private final /* synthetic */ TelephonyRegistry f$13;
            private final /* synthetic */ MediaRouterService f$14;
            private final /* synthetic */ OppomodemService f$15;
            private final /* synthetic */ MmsServiceBroker f$16;
            private final /* synthetic */ WindowManagerService f$2;
            private final /* synthetic */ boolean f$3;
            private final /* synthetic */ ConnectivityService f$4;
            private final /* synthetic */ NetworkManagementService f$5;
            private final /* synthetic */ NetworkPolicyManagerService f$6;
            private final /* synthetic */ IpSecService f$7;
            private final /* synthetic */ NetworkStatsService f$8;
            private final /* synthetic */ LocationManagerService f$9;

            {
                this.f$1 = r4;
                this.f$2 = r5;
                this.f$3 = r6;
                this.f$4 = r7;
                this.f$5 = r8;
                this.f$6 = r9;
                this.f$7 = r10;
                this.f$8 = r11;
                this.f$9 = r12;
                this.f$10 = r13;
                this.f$11 = r14;
                this.f$12 = r15;
                this.f$13 = r16;
                this.f$14 = r17;
                this.f$15 = r18;
                this.f$16 = r19;
            }

            public final void run() {
                SystemServer.this.lambda$startOtherServices$4$SystemServer(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8, this.f$9, this.f$10, this.f$11, this.f$12, this.f$13, this.f$14, this.f$15, this.f$16);
            }
        }, BOOT_TIMINGS_TRACE_LOG);
    }

    static /* synthetic */ void lambda$startOtherServices$1() {
        try {
            Slog.i(TAG, "SecondaryZygotePreload");
            TimingsTraceLog traceLog = new TimingsTraceLog(SYSTEM_SERVER_TIMING_ASYNC_TAG, 524288);
            traceLog.traceBegin("SecondaryZygotePreload");
            if (!Process.ZYGOTE_PROCESS.preloadDefault(Build.SUPPORTED_32_BIT_ABIS[0])) {
                Slog.e(TAG, "Unable to preload default resources");
            }
            traceLog.traceEnd();
        } catch (Exception ex) {
            Slog.e(TAG, "Exception preloading default resources", ex);
        }
    }

    static /* synthetic */ void lambda$startOtherServices$2() {
        TimingsTraceLog traceLog = new TimingsTraceLog(SYSTEM_SERVER_TIMING_ASYNC_TAG, 524288);
        traceLog.traceBegin(START_HIDL_SERVICES);
        startHidlServices();
        traceLog.traceEnd();
    }

    /* JADX WARNING: Removed duplicated region for block: B:100:0x01cd A[SYNTHETIC, Splitter:B:100:0x01cd] */
    /* JADX WARNING: Removed duplicated region for block: B:106:0x01e5 A[SYNTHETIC, Splitter:B:106:0x01e5] */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x01f8 A[SYNTHETIC, Splitter:B:112:0x01f8] */
    /* JADX WARNING: Removed duplicated region for block: B:118:0x020d A[SYNTHETIC, Splitter:B:118:0x020d] */
    /* JADX WARNING: Removed duplicated region for block: B:126:0x0230 A[Catch:{ all -> 0x0234 }] */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x016d A[SYNTHETIC, Splitter:B:76:0x016d] */
    /* JADX WARNING: Removed duplicated region for block: B:82:0x0185 A[SYNTHETIC, Splitter:B:82:0x0185] */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x019d A[SYNTHETIC, Splitter:B:88:0x019d] */
    /* JADX WARNING: Removed duplicated region for block: B:94:0x01b5 A[SYNTHETIC, Splitter:B:94:0x01b5] */
    public /* synthetic */ void lambda$startOtherServices$4$SystemServer(Context context, WindowManagerService windowManagerF, boolean safeMode, ConnectivityService connectivityF, NetworkManagementService networkManagementF, NetworkPolicyManagerService networkPolicyF, IpSecService ipSecServiceF, NetworkStatsService networkStatsF, LocationManagerService locationF, CountryDetectorService countryDetectorF, NetworkTimeUpdateService networkTimeUpdaterF, InputManagerService inputManagerF, TelephonyRegistry telephonyRegistryF, MediaRouterService mediaRouterF, OppomodemService mOppomodemService, MmsServiceBroker mmsServiceF) {
        CountDownLatch networkPolicyInitReadySignal;
        IIncidentManager incident;
        Throwable e;
        Slog.i(TAG, "Making services ready");
        traceBeginAndSlog("StartActivityManagerReadyPhase");
        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_ACTIVITY_MANAGER_READY);
        traceEnd();
        this.mHelper.systemRunning();
        traceBeginAndSlog("StartObservingNativeCrashes");
        try {
            this.mActivityManagerService.startObservingNativeCrashes();
        } catch (Throwable e2) {
            reportWtf("observing native crashes", e2);
        }
        traceEnd();
        Future<?> webviewPrep = (this.mOnlyCore || this.mWebViewUpdateService == null) ? null : SystemServerInitThreadPool.get().submit(new Runnable() {
            /* class com.android.server.$$Lambda$SystemServer$Y1gEdKr_Hb7K7cbTDAo_WOJSYI */

            public final void run() {
                SystemServer.this.lambda$startOtherServices$3$SystemServer();
            }
        }, "WebViewFactoryPreparation");
        if (this.mPackageManager.hasSystemFeature("android.hardware.type.automotive")) {
            traceBeginAndSlog("StartCarServiceHelperService");
            this.mSystemServiceManager.startService(CAR_SERVICE_HELPER_SERVICE_CLASS);
            traceEnd();
        }
        traceBeginAndSlog("StartSystemUI");
        try {
            startSystemUi(context, windowManagerF);
        } catch (Throwable e3) {
            reportWtf("starting System UI", e3);
        }
        traceEnd();
        if (safeMode) {
            traceBeginAndSlog("EnableAirplaneModeInSafeMode");
            try {
                connectivityF.setAirplaneMode(true);
            } catch (Throwable e4) {
                reportWtf("enabling Airplane Mode during Safe Mode bootup", e4);
            }
            traceEnd();
        }
        traceBeginAndSlog("MakeNetworkManagementServiceReady");
        if (networkManagementF != null) {
            try {
                networkManagementF.systemReady();
            } catch (Throwable e5) {
                reportWtf("making Network Managment Service ready", e5);
            }
        }
        if (networkPolicyF != null) {
            networkPolicyInitReadySignal = networkPolicyF.networkScoreAndNetworkManagementServiceReady();
        } else {
            networkPolicyInitReadySignal = null;
        }
        traceEnd();
        traceBeginAndSlog("MakeIpSecServiceReady");
        if (ipSecServiceF != null) {
            try {
                ipSecServiceF.systemReady();
            } catch (Throwable e6) {
                reportWtf("making IpSec Service ready", e6);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeNetworkStatsServiceReady");
        if (networkStatsF != null) {
            try {
                networkStatsF.systemReady();
            } catch (Throwable e7) {
                reportWtf("making Network Stats Service ready", e7);
            }
        }
        traceEnd();
        sMtkSystemServerIns.addBootEvent("SystemServer:NetworkStatsService systemReady");
        traceBeginAndSlog("MakeConnectivityServiceReady");
        if (connectivityF != null) {
            try {
                connectivityF.systemReady();
            } catch (Throwable e8) {
                reportWtf("making Connectivity Service ready", e8);
            }
        }
        traceEnd();
        sMtkSystemServerIns.addBootEvent("SystemServer:ConnectivityService systemReady");
        traceBeginAndSlog("MakeNetworkPolicyServiceReady");
        if (networkPolicyF != null) {
            try {
                networkPolicyF.systemReady(networkPolicyInitReadySignal);
            } catch (Throwable e9) {
                reportWtf("making Network Policy Service ready", e9);
            }
        }
        traceEnd();
        sMtkSystemServerIns.addBootEvent("SystemServer:NetworkPolicyManagerServ systemReady");
        this.mPackageManagerService.waitForAppDataPrepared();
        traceBeginAndSlog("PhaseThirdPartyAppsCanStart");
        if (webviewPrep != null) {
            ConcurrentUtils.waitForFutureNoInterrupt(webviewPrep, "WebViewFactoryPreparation");
        }
        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_THIRD_PARTY_APPS_CAN_START);
        traceEnd();
        traceBeginAndSlog("StartNetworkStack");
        try {
            try {
                NetworkStackClient.getInstance().start(context);
            } catch (Throwable th) {
                e = th;
            }
        } catch (Throwable th2) {
            e = th2;
            reportWtf("starting Network Stack", e);
            traceEnd();
            traceBeginAndSlog("MakeLocationServiceReady");
            if (locationF != null) {
            }
            traceEnd();
            traceBeginAndSlog("MakeCountryDetectionServiceReady");
            if (countryDetectorF != null) {
            }
            traceEnd();
            traceBeginAndSlog("MakeNetworkTimeUpdateReady");
            if (networkTimeUpdaterF != null) {
            }
            traceEnd();
            traceBeginAndSlog("MakeInputManagerServiceReady");
            if (inputManagerF != null) {
            }
            traceEnd();
            traceBeginAndSlog("MakeTelephonyRegistryReady");
            if (telephonyRegistryF != null) {
            }
            traceEnd();
            traceBeginAndSlog("MakeMediaRouterServiceReady");
            if (mediaRouterF != null) {
            }
            traceEnd();
            if (mOppomodemService != null) {
            }
            traceBeginAndSlog("MakeMmsServiceReady");
            if (mmsServiceF != null) {
            }
            traceEnd();
            traceBeginAndSlog("IncidentDaemonReady");
            incident = IIncidentManager.Stub.asInterface(ServiceManager.getService("incident"));
            if (incident != null) {
            }
            traceEnd();
            traceBeginAndSlog("NetworkDataControllerService");
            startNetworkDataControllerService(context);
            traceEnd();
            traceBeginAndSlog("PermissionAnnouncementService");
            startPermissionAnnouncementService(context);
            traceEnd();
            sMtkSystemServerIns.addBootEvent("SystemServer:PhaseThirdPartyAppsCanStart");
        }
        traceEnd();
        traceBeginAndSlog("MakeLocationServiceReady");
        if (locationF != null) {
            try {
                locationF.systemRunning();
            } catch (Throwable e10) {
                reportWtf("Notifying Location Service running", e10);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeCountryDetectionServiceReady");
        if (countryDetectorF != null) {
            try {
                countryDetectorF.systemRunning();
            } catch (Throwable e11) {
                reportWtf("Notifying CountryDetectorService running", e11);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeNetworkTimeUpdateReady");
        if (networkTimeUpdaterF != null) {
            try {
                networkTimeUpdaterF.systemRunning();
            } catch (Throwable e12) {
                reportWtf("Notifying NetworkTimeService running", e12);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeInputManagerServiceReady");
        if (inputManagerF != null) {
            try {
                inputManagerF.systemRunning();
            } catch (Throwable e13) {
                reportWtf("Notifying InputManagerService running", e13);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeTelephonyRegistryReady");
        if (telephonyRegistryF != null) {
            try {
                telephonyRegistryF.systemRunning();
            } catch (Throwable e14) {
                reportWtf("Notifying TelephonyRegistry running", e14);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeMediaRouterServiceReady");
        if (mediaRouterF != null) {
            try {
                mediaRouterF.systemRunning();
            } catch (Throwable e15) {
                reportWtf("Notifying MediaRouterService running", e15);
            }
        }
        traceEnd();
        if (mOppomodemService != null) {
            try {
                mOppomodemService.systemReady();
            } catch (Throwable e16) {
                reportWtf("Notifying mOppomodemService running", e16);
            }
        }
        traceBeginAndSlog("MakeMmsServiceReady");
        if (mmsServiceF != null) {
            try {
                mmsServiceF.systemRunning();
            } catch (Throwable e17) {
                reportWtf("Notifying MmsService running", e17);
            }
        }
        traceEnd();
        traceBeginAndSlog("IncidentDaemonReady");
        try {
            incident = IIncidentManager.Stub.asInterface(ServiceManager.getService("incident"));
            if (incident != null) {
                incident.systemRunning();
            }
        } catch (Throwable e18) {
            reportWtf("Notifying incident daemon running", e18);
        }
        traceEnd();
        traceBeginAndSlog("NetworkDataControllerService");
        try {
            startNetworkDataControllerService(context);
        } catch (Throwable e19) {
            reportWtf("starting NetworkDataControllerService:", e19);
        }
        traceEnd();
        traceBeginAndSlog("PermissionAnnouncementService");
        try {
            startPermissionAnnouncementService(context);
        } catch (Throwable e20) {
            reportWtf("starting PermissionAnnouncementService:", e20);
        }
        traceEnd();
        sMtkSystemServerIns.addBootEvent("SystemServer:PhaseThirdPartyAppsCanStart");
    }

    public /* synthetic */ void lambda$startOtherServices$3$SystemServer() {
        Slog.i(TAG, "WebViewFactoryPreparation");
        TimingsTraceLog traceLog = new TimingsTraceLog(SYSTEM_SERVER_TIMING_ASYNC_TAG, 524288);
        traceLog.traceBegin("WebViewFactoryPreparation");
        ConcurrentUtils.waitForFutureNoInterrupt(this.mZygotePreload, "Zygote preload");
        this.mZygotePreload = null;
        this.mWebViewUpdateService.prepareWebViewInSystemServer();
        traceLog.traceEnd();
    }

    private void startSystemCaptionsManagerService(Context context) {
        if (TextUtils.isEmpty(context.getString(17039710))) {
            Slog.d(TAG, "SystemCaptionsManagerService disabled because resource is not overlaid");
            return;
        }
        traceBeginAndSlog("StartSystemCaptionsManagerService");
        this.mSystemServiceManager.startService(SYSTEM_CAPTIONS_MANAGER_SERVICE_CLASS);
        traceEnd();
    }

    private void startContentCaptureService(Context context) {
        ActivityManagerService activityManagerService;
        boolean explicitlyEnabled = false;
        String settings = DeviceConfig.getProperty("content_capture", "service_explicitly_enabled");
        if (settings != null && !settings.equalsIgnoreCase(BatteryService.HealthServiceWrapper.INSTANCE_VENDOR)) {
            explicitlyEnabled = Boolean.parseBoolean(settings);
            if (explicitlyEnabled) {
                Slog.d(TAG, "ContentCaptureService explicitly enabled by DeviceConfig");
            } else {
                Slog.d(TAG, "ContentCaptureService explicitly disabled by DeviceConfig");
                return;
            }
        }
        if (explicitlyEnabled || !TextUtils.isEmpty(context.getString(17039701))) {
            traceBeginAndSlog("StartContentCaptureService");
            this.mSystemServiceManager.startService(CONTENT_CAPTURE_MANAGER_SERVICE_CLASS);
            ContentCaptureManagerInternal ccmi = (ContentCaptureManagerInternal) LocalServices.getService(ContentCaptureManagerInternal.class);
            if (!(ccmi == null || (activityManagerService = this.mActivityManagerService) == null)) {
                activityManagerService.setContentCaptureManager(ccmi);
            }
            traceEnd();
            return;
        }
        Slog.d(TAG, "ContentCaptureService disabled because resource is not overlaid");
    }

    private void startAttentionService(Context context) {
        if (!AttentionManagerService.isServiceConfigured(context)) {
            Slog.d(TAG, "AttentionService is not configured on this device");
            return;
        }
        traceBeginAndSlog("StartAttentionManagerService");
        this.mSystemServiceManager.startService(AttentionManagerService.class);
        traceEnd();
    }

    private static void startSystemUi(Context context, WindowManagerService windowManager) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.android.systemui", "com.android.systemui.SystemUIService"));
        intent.addFlags(256);
        context.startServiceAsUser(intent, UserHandle.SYSTEM);
        windowManager.onSystemUiStarted();
    }

    private final void startNetworkDataControllerService(Context context) {
        if (SystemProperties.getInt("persist.vendor.sys.disable.moms", 0) != 1 && SystemProperties.getInt("ro.vendor.mtk_mobile_management", 0) == 1) {
            Intent serviceIntent = new Intent("com.mediatek.security.START_SERVICE");
            serviceIntent.setClassName("com.mediatek.security.service", "com.mediatek.security.service.NetworkDataControllerService");
            context.startServiceAsUser(serviceIntent, UserHandle.SYSTEM);
        }
    }

    private final void startPermissionAnnouncementService(Context context) {
        if (SystemProperties.getInt("persist.vendor.sys.disable.moms", 0) != 1 && SystemProperties.getInt("ro.vendor.mtk_mobile_management", 0) == 1) {
            Intent intent = new Intent();
            intent.setClassName("com.android.permissioncontroller", "com.mediatek.packageinstaller.PermissionsAnnouncementService");
            context.startForegroundServiceAsUser(intent, UserHandle.SYSTEM);
        }
    }

    private static void traceBeginAndSlog(String name) {
        Slog.i(TAG, name);
        BOOT_TIMINGS_TRACE_LOG.traceBegin(name);
    }

    private static void traceEnd() {
        BOOT_TIMINGS_TRACE_LOG.traceEnd();
    }

    private static Class<?> getMtkSystemServer() {
        try {
            return Class.forName("com.mediatek.server.MtkSystemServer", false, new PathClassLoader("system/framework/mediatek-services.jar", SystemServer.class.getClassLoader()));
        } catch (Exception e) {
            Slog.e(TAG, "getMtkSystemServer:" + e.toString());
            return null;
        }
    }

    private void createMtkSystemServer() {
        Slog.i(TAG, "startMtkSystemServer start");
        try {
            if (sMtkSystemServerClass != null) {
                this.mMtkSystemServerInstance = sMtkSystemServerClass.getDeclaredMethod("getInstance", TimingsTraceLog.class, SystemServiceManager.class).invoke(sMtkSystemServerClass, BOOT_TIMINGS_TRACE_LOG, this.mSystemServiceManager);
            }
        } catch (Exception e) {
            Slog.e(TAG, "createMtkSystemServer" + e.toString());
        }
    }

    private void startMtkBootstrapServices() {
        Slog.i(TAG, "startMtkBootstrapServices start");
        try {
            if (this.mMtkSystemServerInstance != null) {
                sMtkSystemServerClass.getMethod("startMtkBootstrapServices", new Class[0]).invoke(this.mMtkSystemServerInstance, new Object[0]);
            }
        } catch (Exception e) {
            Slog.e(TAG, "reflect  startDataShappingService error" + e.toString());
        }
    }

    private void startMtkCoreServices() {
        Slog.i(TAG, "startMtkCoreServices start");
        try {
            if (this.mMtkSystemServerInstance != null) {
                sMtkSystemServerClass.getMethod("startMtkCoreServices", new Class[0]).invoke(this.mMtkSystemServerInstance, new Object[0]);
            }
        } catch (Exception e) {
            Slog.e(TAG, "reflect  startMtkCoreServices error" + e.toString());
        }
    }

    private void startMtkOtherServices() {
        Slog.i(TAG, "startMtkOtherServices start");
        try {
            if (this.mMtkSystemServerInstance != null) {
                sMtkSystemServerClass.getMethod("startMtkOtherServices", new Class[0]).invoke(this.mMtkSystemServerInstance, new Object[0]);
            }
        } catch (Exception e) {
            Slog.e(TAG, "reflect  startMtkOtherServices error" + e.toString());
        }
    }
}
