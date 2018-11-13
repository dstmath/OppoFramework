package com.android.server;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.app.ActivityThread;
import android.app.INotificationManager;
import android.app.usage.UsageStatsManagerInternal;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources.Theme;
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
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StrictMode;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.storage.IStorageManager;
import android.os.storage.IStorageManager.Stub;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.util.Slog;
import android.util.TimingsTraceLog;
import android.view.WindowManager;
import com.android.internal.app.NightDisplayController;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.os.BinderInternal;
import com.android.internal.os.RegionalizationEnvironment;
import com.android.internal.util.ConcurrentUtils;
import com.android.internal.widget.ILockSettings;
import com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass2;
import com.android.server.accessibility.AccessibilityManagerService;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.ActivityManagerService.Lifecycle;
import com.android.server.am.OppoFreeFormManagerService;
import com.android.server.am.OppoProcessManager;
import com.android.server.audio.AudioService;
import com.android.server.biometrics.BiometricsService;
import com.android.server.broadcastradio.BroadcastRadioService;
import com.android.server.camera.CameraServiceProxy;
import com.android.server.car.CarServiceHelperService;
import com.android.server.clipboard.ClipboardService;
import com.android.server.connectivity.IpConnectivityMetrics;
import com.android.server.coverage.CoverageService;
import com.android.server.devicepolicy.DevicePolicyManagerService;
import com.android.server.display.DisplayManagerService;
import com.android.server.display.NightDisplayService;
import com.android.server.display.OppoBrightUtils;
import com.android.server.dreams.DreamManagerService;
import com.android.server.emergency.EmergencyAffordanceService;
import com.android.server.engineer.OppoEngineerService;
import com.android.server.face.FaceService;
import com.android.server.fingerprint.FingerprintService;
import com.android.server.hdmi.HdmiControlService;
import com.android.server.input.InputManagerService;
import com.android.server.job.JobSchedulerService;
import com.android.server.lights.OppoLightsService;
import com.android.server.media.MediaResourceMonitorService;
import com.android.server.media.MediaRouterService;
import com.android.server.media.MediaSessionService;
import com.android.server.media.projection.MediaProjectionManagerService;
import com.android.server.net.NetworkPolicyManagerService;
import com.android.server.net.NetworkStatsService;
import com.android.server.notification.NotificationManagerService;
import com.android.server.oemlock.OemLockService;
import com.android.server.om.OverlayManagerService;
import com.android.server.oppo.CabcService;
import com.android.server.oppo.ChattyManagerService;
import com.android.server.oppo.HypnusService;
import com.android.server.oppo.OppoAutoInstallService;
import com.android.server.oppo.OppoCustomizeService;
import com.android.server.oppo.OppoExService;
import com.android.server.oppo.OppoService;
import com.android.server.oppo.OppoUsageService;
import com.android.server.os.DeviceIdentifiersPolicyService;
import com.android.server.os.RegionalizationService;
import com.android.server.os.SchedulingPolicyService;
import com.android.server.pm.BackgroundDexOptService;
import com.android.server.pm.Installer;
import com.android.server.pm.LauncherAppsService;
import com.android.server.pm.OtaDexoptService;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.ShortcutService;
import com.android.server.pm.UserManagerService.LifeCycle;
import com.android.server.policy.OppoPhoneWindowManager;
import com.android.server.power.PowerManagerService;
import com.android.server.power.ShutdownThread;
import com.android.server.restrictions.RestrictionsManagerService;
import com.android.server.secrecy.SecrecyService;
import com.android.server.security.KeyAttestationApplicationIdProviderService;
import com.android.server.security.KeyChainSystemService;
import com.android.server.soundtrigger.SoundTriggerService;
import com.android.server.statusbar.StatusBarManagerService;
import com.android.server.storage.OppoDeviceStorageMonitorService;
import com.android.server.telecom.TelecomLoaderService;
import com.android.server.trust.TrustManagerService;
import com.android.server.tv.TvInputManagerService;
import com.android.server.tv.TvRemoteService;
import com.android.server.twilight.TwilightService;
import com.android.server.usage.UsageStatsService;
import com.android.server.vr.VrManagerService;
import com.android.server.webkit.WebViewUpdateService;
import com.android.server.wm.WindowManagerService;
import com.oppo.media.OppoMultimediaService;
import com.oppo.roundcorner.OppoRoundCornerService;
import dalvik.system.PathClassLoader;
import dalvik.system.VMRuntime;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Timer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

public final class SystemServer {
    private static final String ACCOUNT_SERVICE_CLASS = "com.android.server.accounts.AccountManagerService$Lifecycle";
    private static final String APPWIDGET_SERVICE_CLASS = "com.android.server.appwidget.AppWidgetService";
    private static final String AUTO_FILL_MANAGER_SERVICE_CLASS = "com.android.server.autofill.AutofillManagerService";
    private static final String BACKUP_MANAGER_SERVICE_CLASS = "com.android.server.backup.BackupManagerService$Lifecycle";
    private static final String BLOCK_MAP_FILE = "/cache/recovery/block.map";
    private static final TimingsTraceLog BOOT_TIMINGS_TRACE_LOG = new TimingsTraceLog(SYSTEM_SERVER_TIMING_TAG, 524288);
    private static final String COMPANION_DEVICE_MANAGER_SERVICE_CLASS = "com.android.server.companion.CompanionDeviceManagerService";
    private static final String CONTENT_SERVICE_CLASS = "com.android.server.content.ContentService$Lifecycle";
    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "JianHui.Yu@Plf.SDK, 2016-12-24 : Modify for use oppo style", property = OppoRomType.ROM)
    private static final int DEFAULT_SYSTEM_THEME = 201523202;
    private static final long EARLIEST_SUPPORTED_TIME = 86400000;
    private static final String ENCRYPTED_STATE = "1";
    private static final String ENCRYPTING_STATE = "trigger_restart_min_framework";
    private static final String ETHERNET_SERVICE_CLASS = "com.android.server.ethernet.EthernetService";
    private static final String JOB_SCHEDULER_SERVICE_CLASS = "com.android.server.job.JobSchedulerService";
    private static final String LOCK_SETTINGS_SERVICE_CLASS = "com.android.server.locksettings.LockSettingsService$Lifecycle";
    private static final String LOWPAN_SERVICE_CLASS = "com.android.server.lowpan.LowpanService";
    private static final String MIDI_SERVICE_CLASS = "com.android.server.midi.MidiService$Lifecycle";
    private static final String PERSISTENT_DATA_BLOCK_PROP = "ro.frp.pst";
    private static final String PRINT_MANAGER_SERVICE_CLASS = "com.android.server.print.PrintManagerService";
    private static final String SEARCH_MANAGER_SERVICE_CLASS = "com.android.server.search.SearchManagerService$Lifecycle";
    private static final long SNAPSHOT_INTERVAL = 3600000;
    private static final String START_HIDL_SERVICES = "StartHidlServices";
    private static final String START_SENSOR_SERVICE = "StartSensorService";
    private static final String STORAGE_MANAGER_SERVICE_CLASS = "com.android.server.StorageManagerService$Lifecycle";
    private static final String STORAGE_STATS_SERVICE_CLASS = "com.android.server.usage.StorageStatsService$Lifecycle";
    private static final String SYSTEM_SERVER_TIMING_ASYNC_TAG = "SystemServerTimingAsync";
    private static final String SYSTEM_SERVER_TIMING_TAG = "SystemServerTiming";
    private static final String TAG = "SystemServer";
    private static final String THERMAL_OBSERVER_CLASS = "com.google.android.clockwork.ThermalObserver";
    private static final String TIME_ZONE_RULES_MANAGER_SERVICE_CLASS = "com.android.server.timezone.RulesManagerService$Lifecycle";
    private static final String UNCRYPT_PACKAGE_FILE = "/cache/recovery/uncrypt_file";
    private static final String USB_SERVICE_CLASS = "com.android.server.usb.UsbService$Lifecycle";
    private static final String VOICE_RECOGNITION_MANAGER_SERVICE_CLASS = "com.android.server.voiceinteraction.VoiceInteractionManagerService";
    private static final String WALLPAPER_SERVICE_CLASS = "com.android.server.wallpaper.WallpaperManagerService$Lifecycle";
    private static final String WEAR_CONNECTIVITY_SERVICE_CLASS = "com.google.android.clockwork.connectivity.WearConnectivityService";
    private static final String WEAR_DISPLAY_SERVICE_CLASS = "com.google.android.clockwork.display.WearDisplayService";
    private static final String WEAR_LEFTY_SERVICE_CLASS = "com.google.android.clockwork.lefty.WearLeftyService";
    private static final String WEAR_TIME_SERVICE_CLASS = "com.google.android.clockwork.time.WearTimeService";
    private static final String WIFI_AWARE_SERVICE_CLASS = "com.android.server.wifi.aware.WifiAwareService";
    private static final String WIFI_P2P_SERVICE_CLASS = "com.android.server.wifi.p2p.WifiP2pService";
    private static final String WIFI_SERVICE_CLASS = "com.android.server.wifi.WifiService";
    private static final int sMaxBinderThreads = 31;
    private ActivityManagerService mActivityManagerService;
    private BiometricsService mBiometricsService = null;
    private ContentResolver mContentResolver;
    private DisplayManagerService mDisplayManagerService;
    private EntropyMixer mEntropyMixer;
    private FaceService mFaceService = null;
    private final int mFactoryTestMode = FactoryTest.getMode();
    private FingerprintService mFingerprintService = null;
    private boolean mFirstBoot;
    private boolean mOnlyCore;
    private OppoEngineerService mOppoEngineerService = null;
    private OppoLightsService mOppoLightsService;
    private PackageManager mPackageManager;
    private PackageManagerService mPackageManagerService;
    private PowerManagerService mPowerManagerService;
    private Timer mProfilerSnapshotTimer;
    private final boolean mRuntimeRestart = "1".equals(SystemProperties.get("sys.boot_completed"));
    private SecrecyService mSecrecyService;
    private Future<?> mSensorServiceStart;
    private Context mSystemContext;
    private SystemServiceManager mSystemServiceManager;
    private WebViewUpdateService mWebViewUpdateService;
    private Future<?> mZygotePreload;

    private static native void startHidlServices();

    private static native void startSensorService();

    public static void main(String[] args) {
        new SystemServer().run();
    }

    private void run() {
        try {
            traceBeginAndSlog("InitBeforeStartServices");
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
            Slog.i(TAG, "Entered the Android system server!");
            int uptimeMillis = (int) SystemClock.elapsedRealtime();
            EventLog.writeEvent(EventLogTags.BOOT_PROGRESS_SYSTEM_RUN, uptimeMillis);
            if (!this.mRuntimeRestart) {
                MetricsLogger.histogram(null, "boot_system_server_init", uptimeMillis);
            }
            SystemProperties.set("persist.sys.dalvik.vm.lib.2", VMRuntime.getRuntime().vmLibrary());
            VMRuntime.getRuntime().clearGrowthLimit();
            VMRuntime.getRuntime().setTargetHeapUtilization(0.8f);
            Build.ensureFingerprintProperty();
            Environment.setUserRequired(true);
            BaseBundle.setShouldDefuse(true);
            BinderInternal.disableBackgroundScheduling(true);
            BinderInternal.setMaxThreads(31);
            Process.setThreadPriority(-2);
            Process.setCanSelfBackground(false);
            Looper.prepareMainLooper();
            System.loadLibrary("android_servers");
            performPendingShutdown();
            createSystemContext();
            this.mSystemServiceManager = new SystemServiceManager(this.mSystemContext);
            this.mSystemServiceManager.setRuntimeRestarted(this.mRuntimeRestart);
            LocalServices.addService(SystemServiceManager.class, this.mSystemServiceManager);
            SystemServerInitThreadPool.get();
            SystemProperties.set("sys.oppo.boot_completed", "0");
            try {
                traceBeginAndSlog("StartServices");
                startBootstrapServices();
                startCoreServices();
                startOtherServices();
                SystemServerInitThreadPool.shutdown();
                traceEnd();
                if (StrictMode.conditionallyEnableDebugLogging()) {
                    Slog.i(TAG, "Enabled StrictMode for system server main thread.");
                }
                if (!(this.mRuntimeRestart || (isFirstBootOrUpgrade() ^ 1) == 0)) {
                    uptimeMillis = (int) SystemClock.elapsedRealtime();
                    MetricsLogger.histogram(null, "boot_system_server_ready", uptimeMillis);
                    if (uptimeMillis > OppoBrightUtils.SPECIAL_AMBIENT_LIGHT_HORIZON) {
                        Slog.wtf(SYSTEM_SERVER_TIMING_TAG, "SystemServer init took too long. uptimeMillis=" + uptimeMillis);
                    }
                }
                Looper.loop();
                throw new RuntimeException("Main thread loop unexpectedly exited");
            } catch (Throwable th) {
                traceEnd();
            }
        } finally {
            traceEnd();
        }
    }

    private boolean isFirstBootOrUpgrade() {
        return !this.mPackageManagerService.isFirstBoot() ? this.mPackageManagerService.isUpgrade() : true;
    }

    private void reportWtf(String msg, Throwable e) {
        Slog.w(TAG, "***********************************************");
        Slog.wtf(TAG, "BOOT FAILURE " + msg, e);
    }

    private void performPendingShutdown() {
        String shutdownAction = SystemProperties.get(ShutdownThread.SHUTDOWN_ACTION_PROPERTY, "");
        if (shutdownAction != null && shutdownAction.length() > 0) {
            String reason;
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
                    if (!(filename == null || !filename.startsWith("/data") || new File(BLOCK_MAP_FILE).exists())) {
                        Slog.e(TAG, "Can't find block map file, uncrypt failed or unexpected runtime restart?");
                        return;
                    }
                }
            }
            Message msg = Message.obtain(UiThread.getHandler(), new Runnable() {
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

    private void startBootstrapServices() {
        Slog.i(TAG, "Reading configuration...");
        String TAG_SYSTEM_CONFIG = "ReadingSystemConfig";
        traceBeginAndSlog("ReadingSystemConfig");
        SystemServerInitThreadPool.get().submit(-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.$INST$0, "ReadingSystemConfig");
        traceEnd();
        traceBeginAndSlog("StartInstaller");
        Installer installer = (Installer) this.mSystemServiceManager.startService(Installer.class);
        traceEnd();
        traceBeginAndSlog("DeviceIdentifiersPolicyService");
        this.mSystemServiceManager.startService(DeviceIdentifiersPolicyService.class);
        traceEnd();
        traceBeginAndSlog("StartActivityManager");
        Slog.i(TAG, "Ams Service");
        this.mActivityManagerService = ((Lifecycle) this.mSystemServiceManager.startService(Lifecycle.class)).getService();
        this.mActivityManagerService.setSystemServiceManager(this.mSystemServiceManager);
        this.mActivityManagerService.setInstaller(installer);
        traceEnd();
        traceBeginAndSlog("StartPowerManager");
        Slog.i(TAG, "Power Service");
        this.mPowerManagerService = (PowerManagerService) this.mSystemServiceManager.startService(PowerManagerService.class);
        traceEnd();
        traceBeginAndSlog("InitPowerManagement");
        this.mActivityManagerService.initPowerManagement();
        traceEnd();
        if (!SystemProperties.getBoolean("config.disable_noncore", false)) {
            traceBeginAndSlog("StartRecoverySystemService");
            this.mSystemServiceManager.startService(RecoverySystemService.class);
            traceEnd();
        }
        RescueParty.noteBoot(this.mSystemContext);
        traceBeginAndSlog("StartLightsService");
        Slog.i(TAG, "Light Service");
        this.mOppoLightsService = (OppoLightsService) this.mSystemServiceManager.startService(OppoLightsService.class);
        traceEnd();
        traceBeginAndSlog("StartDisplayManager");
        Slog.i(TAG, "DisplayManager Service");
        this.mDisplayManagerService = (DisplayManagerService) this.mSystemServiceManager.startService(DisplayManagerService.class);
        traceEnd();
        traceBeginAndSlog("WaitForDisplay");
        this.mSystemServiceManager.startBootPhase(100);
        traceEnd();
        String cryptState = SystemProperties.get("vold.decrypt");
        if (ENCRYPTING_STATE.equals(cryptState)) {
            Slog.w(TAG, "Detected encryption in progress - only parsing core apps");
            this.mOnlyCore = true;
        } else if ("1".equals(cryptState)) {
            Slog.w(TAG, "Device encrypted - only parsing core apps");
            this.mOnlyCore = true;
        }
        if (RegionalizationEnvironment.isSupported()) {
            Slog.i(TAG, "Regionalization Service");
            ServiceManager.addService("regionalization", new RegionalizationService());
        }
        if (!this.mRuntimeRestart) {
            MetricsLogger.histogram(null, "boot_package_manager_init_start", (int) SystemClock.elapsedRealtime());
        }
        traceBeginAndSlog("StartPackageManagerService");
        this.mPackageManagerService = PackageManagerService.main(this.mSystemContext, installer, this.mFactoryTestMode != 0, this.mOnlyCore);
        this.mFirstBoot = this.mPackageManagerService.isFirstBoot();
        this.mPackageManager = this.mSystemContext.getPackageManager();
        traceEnd();
        if (!(this.mRuntimeRestart || (isFirstBootOrUpgrade() ^ 1) == 0)) {
            MetricsLogger.histogram(null, "boot_package_manager_init_ready", (int) SystemClock.elapsedRealtime());
        }
        if (!(this.mOnlyCore || SystemProperties.getBoolean("config.disable_otadexopt", false))) {
            traceBeginAndSlog("StartOtaDexOptService");
            try {
                OtaDexoptService.main(this.mSystemContext, this.mPackageManagerService);
            } catch (Throwable e) {
                reportWtf("starting OtaDexOptService", e);
            } finally {
                traceEnd();
            }
        }
        traceBeginAndSlog("StartUserManagerService");
        this.mSystemServiceManager.startService(LifeCycle.class);
        traceEnd();
        traceBeginAndSlog("InitAttributerCache");
        AttributeCache.init(this.mSystemContext);
        traceEnd();
        traceBeginAndSlog("SetSystemProcess");
        this.mActivityManagerService.setSystemProcess();
        traceEnd();
        this.mDisplayManagerService.setupSchedulerPolicies();
        traceBeginAndSlog("StartOverlayManagerService");
        this.mSystemServiceManager.startService(new OverlayManagerService(this.mSystemContext, installer));
        traceEnd();
        Slog.i(TAG, "Sensor Service");
        this.mSensorServiceStart = SystemServerInitThreadPool.get().submit(-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.$INST$1, START_SENSOR_SERVICE);
    }

    /* renamed from: lambda$-com_android_server_SystemServer_37107 */
    static /* synthetic */ void m7lambda$-com_android_server_SystemServer_37107() {
        TimingsTraceLog traceLog = new TimingsTraceLog(SYSTEM_SERVER_TIMING_ASYNC_TAG, 524288);
        traceLog.traceBegin(START_SENSOR_SERVICE);
        startSensorService();
        traceLog.traceEnd();
    }

    private void startCoreServices() {
        traceBeginAndSlog("StartDropBoxManager");
        this.mSystemServiceManager.startService(DropBoxManagerService.class);
        traceEnd();
        traceBeginAndSlog("StartBatteryService");
        Slog.i(TAG, "Battery Service");
        this.mSystemServiceManager.startService(BatteryService.class);
        traceEnd();
        traceBeginAndSlog("StartUsageService");
        Slog.i(TAG, "UsageStats Service");
        this.mSystemServiceManager.startService(UsageStatsService.class);
        this.mActivityManagerService.setUsageStatsManager((UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class));
        traceEnd();
        traceBeginAndSlog("StartWebViewUpdateService");
        this.mWebViewUpdateService = (WebViewUpdateService) this.mSystemServiceManager.startService(WebViewUpdateService.class);
        traceEnd();
    }

    /* JADX WARNING: Removed duplicated region for block: B:258:0x0a9e A:{Catch:{ Throwable -> 0x12c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x0b4e A:{SYNTHETIC, Splitter: B:288:0x0b4e} */
    /* JADX WARNING: Removed duplicated region for block: B:523:0x1328  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0b64  */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x0b71  */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0bc0  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0bea  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0bfe  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0c39  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c59  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0cf1  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0d10  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0d24  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:258:0x0a9e A:{Catch:{ Throwable -> 0x12c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x0b4e A:{SYNTHETIC, Splitter: B:288:0x0b4e} */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0b64  */
    /* JADX WARNING: Removed duplicated region for block: B:523:0x1328  */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x0b71  */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0bc0  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0bea  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0bfe  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0c39  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c59  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0cf1  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0d10  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0d24  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0304  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x03ac  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x03dc  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0304  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x03ac  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x03dc  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x0b4e A:{SYNTHETIC, Splitter: B:288:0x0b4e} */
    /* JADX WARNING: Removed duplicated region for block: B:523:0x1328  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0b64  */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x0b71  */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0bc0  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0bea  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0bfe  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0c39  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c59  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0cf1  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0d10  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0d24  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:222:0x0942  */
    /* JADX WARNING: Removed duplicated region for block: B:225:0x097a  */
    /* JADX WARNING: Removed duplicated region for block: B:258:0x0a9e A:{Catch:{ Throwable -> 0x12c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x0b4e A:{SYNTHETIC, Splitter: B:288:0x0b4e} */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0b64  */
    /* JADX WARNING: Removed duplicated region for block: B:523:0x1328  */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x0b71  */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0bc0  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0bea  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0bfe  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0c39  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c59  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0cf1  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0d10  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0d24  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x0b4e A:{SYNTHETIC, Splitter: B:288:0x0b4e} */
    /* JADX WARNING: Removed duplicated region for block: B:523:0x1328  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0b64  */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x0b71  */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0bc0  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0bea  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0bfe  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0c39  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c59  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0cf1  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0d10  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0d24  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:258:0x0a9e A:{Catch:{ Throwable -> 0x12c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x0b4e A:{SYNTHETIC, Splitter: B:288:0x0b4e} */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0b64  */
    /* JADX WARNING: Removed duplicated region for block: B:523:0x1328  */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x0b71  */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0bc0  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0bea  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0bfe  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0c39  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c59  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0cf1  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0d10  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0d24  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0304  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x03ac  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x03dc  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x0487  */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x049b  */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x04ca  */
    /* JADX WARNING: Removed duplicated region for block: B:141:0x06be  */
    /* JADX WARNING: Removed duplicated region for block: B:157:0x0708 A:{Catch:{ Throwable -> 0x1245 }} */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x0765  */
    /* JADX WARNING: Removed duplicated region for block: B:187:0x0845  */
    /* JADX WARNING: Removed duplicated region for block: B:189:0x0859  */
    /* JADX WARNING: Removed duplicated region for block: B:197:0x08a7  */
    /* JADX WARNING: Removed duplicated region for block: B:222:0x0942  */
    /* JADX WARNING: Removed duplicated region for block: B:225:0x097a  */
    /* JADX WARNING: Removed duplicated region for block: B:258:0x0a9e A:{Catch:{ Throwable -> 0x12c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x0b4e A:{SYNTHETIC, Splitter: B:288:0x0b4e} */
    /* JADX WARNING: Removed duplicated region for block: B:523:0x1328  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0b64  */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x0b71  */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0bc0  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0bea  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0bfe  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0c39  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c59  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0cf1  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0d10  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0d24  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0304  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x03ac  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x03dc  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x0b4e A:{SYNTHETIC, Splitter: B:288:0x0b4e} */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0b64  */
    /* JADX WARNING: Removed duplicated region for block: B:523:0x1328  */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x0b71  */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0bc0  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0bea  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0bfe  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0c39  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c59  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0cf1  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0d10  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0d24  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x0b4e A:{SYNTHETIC, Splitter: B:288:0x0b4e} */
    /* JADX WARNING: Removed duplicated region for block: B:523:0x1328  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0b64  */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x0b71  */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0bc0  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0bea  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0bfe  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0c39  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c59  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0cf1  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0d10  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0d24  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0304  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x03ac  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x03dc  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0bc0  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0bea  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0bfe  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0c39  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c59  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0cf1  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0d10  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0d24  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:258:0x0a9e A:{Catch:{ Throwable -> 0x12c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x0b4e A:{SYNTHETIC, Splitter: B:288:0x0b4e} */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0b64  */
    /* JADX WARNING: Removed duplicated region for block: B:523:0x1328  */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x0b71  */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0bc0  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0bea  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0bfe  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0c39  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c59  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0cf1  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0d10  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0d24  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0bc0  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0bea  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0bfe  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0c39  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c59  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0cf1  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0d10  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0d24  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x0b4e A:{SYNTHETIC, Splitter: B:288:0x0b4e} */
    /* JADX WARNING: Removed duplicated region for block: B:523:0x1328  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0b64  */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x0b71  */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0bc0  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0bea  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0bfe  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0c39  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c59  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0cf1  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0d10  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0d24  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0bc0  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0bea  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0bfe  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0c39  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c59  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0cf1  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0d10  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0d24  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x0b4e A:{SYNTHETIC, Splitter: B:288:0x0b4e} */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0b64  */
    /* JADX WARNING: Removed duplicated region for block: B:523:0x1328  */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x0b71  */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0bc0  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0bea  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0bfe  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0c39  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c59  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0cf1  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0d10  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0d24  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:141:0x06be  */
    /* JADX WARNING: Removed duplicated region for block: B:157:0x0708 A:{Catch:{ Throwable -> 0x1245 }} */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x0765  */
    /* JADX WARNING: Removed duplicated region for block: B:187:0x0845  */
    /* JADX WARNING: Removed duplicated region for block: B:189:0x0859  */
    /* JADX WARNING: Removed duplicated region for block: B:197:0x08a7  */
    /* JADX WARNING: Removed duplicated region for block: B:222:0x0942  */
    /* JADX WARNING: Removed duplicated region for block: B:225:0x097a  */
    /* JADX WARNING: Removed duplicated region for block: B:258:0x0a9e A:{Catch:{ Throwable -> 0x12c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x0b4e A:{SYNTHETIC, Splitter: B:288:0x0b4e} */
    /* JADX WARNING: Removed duplicated region for block: B:523:0x1328  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0b64  */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x0b71  */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0bc0  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0bea  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0bfe  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0c39  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c59  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0cf1  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0d10  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0d24  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x0b4e A:{SYNTHETIC, Splitter: B:288:0x0b4e} */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0b64  */
    /* JADX WARNING: Removed duplicated region for block: B:523:0x1328  */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x0b71  */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0bc0  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0bea  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0bfe  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0c39  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c59  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0cf1  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0d10  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0d24  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:222:0x0942  */
    /* JADX WARNING: Removed duplicated region for block: B:225:0x097a  */
    /* JADX WARNING: Removed duplicated region for block: B:258:0x0a9e A:{Catch:{ Throwable -> 0x12c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x0b4e A:{SYNTHETIC, Splitter: B:288:0x0b4e} */
    /* JADX WARNING: Removed duplicated region for block: B:523:0x1328  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0b64  */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x0b71  */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0bc0  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0bea  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0bfe  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0c39  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c59  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0cf1  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0d10  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0d24  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:363:0x0d7e  */
    /* JADX WARNING: Removed duplicated region for block: B:366:0x0dad  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0bc0  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0bea  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0bfe  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0c39  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c59  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0cf1  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0d10  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0d24  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x0b4e A:{SYNTHETIC, Splitter: B:288:0x0b4e} */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0b64  */
    /* JADX WARNING: Removed duplicated region for block: B:523:0x1328  */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x0b71  */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0bc0  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0bea  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0bfe  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0c39  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c59  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0cf1  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0d10  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0d24  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:222:0x0942  */
    /* JADX WARNING: Removed duplicated region for block: B:225:0x097a  */
    /* JADX WARNING: Removed duplicated region for block: B:258:0x0a9e A:{Catch:{ Throwable -> 0x12c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x0b4e A:{SYNTHETIC, Splitter: B:288:0x0b4e} */
    /* JADX WARNING: Removed duplicated region for block: B:523:0x1328  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0b64  */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x0b71  */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0bc0  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0bea  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0bfe  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0c39  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c59  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0cf1  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0d10  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0d24  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x0b4e A:{SYNTHETIC, Splitter: B:288:0x0b4e} */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0b64  */
    /* JADX WARNING: Removed duplicated region for block: B:523:0x1328  */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x0b71  */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0bc0  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0bea  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0bfe  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0c39  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c59  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0cf1  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0d10  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0d24  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:141:0x06be  */
    /* JADX WARNING: Removed duplicated region for block: B:157:0x0708 A:{Catch:{ Throwable -> 0x1245 }} */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x0765  */
    /* JADX WARNING: Removed duplicated region for block: B:187:0x0845  */
    /* JADX WARNING: Removed duplicated region for block: B:189:0x0859  */
    /* JADX WARNING: Removed duplicated region for block: B:197:0x08a7  */
    /* JADX WARNING: Removed duplicated region for block: B:222:0x0942  */
    /* JADX WARNING: Removed duplicated region for block: B:225:0x097a  */
    /* JADX WARNING: Removed duplicated region for block: B:258:0x0a9e A:{Catch:{ Throwable -> 0x12c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x0b4e A:{SYNTHETIC, Splitter: B:288:0x0b4e} */
    /* JADX WARNING: Removed duplicated region for block: B:523:0x1328  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0b64  */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x0b71  */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0bc0  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0bea  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0bfe  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0c39  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c59  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0cf1  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0d10  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0d24  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:187:0x0845  */
    /* JADX WARNING: Removed duplicated region for block: B:189:0x0859  */
    /* JADX WARNING: Removed duplicated region for block: B:197:0x08a7  */
    /* JADX WARNING: Removed duplicated region for block: B:222:0x0942  */
    /* JADX WARNING: Removed duplicated region for block: B:225:0x097a  */
    /* JADX WARNING: Removed duplicated region for block: B:258:0x0a9e A:{Catch:{ Throwable -> 0x12c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x0b4e A:{SYNTHETIC, Splitter: B:288:0x0b4e} */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0b64  */
    /* JADX WARNING: Removed duplicated region for block: B:523:0x1328  */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x0b71  */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0bc0  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0bea  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0bfe  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0c39  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c59  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0cf1  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0d10  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0d24  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x054e  */
    /* JADX WARNING: Removed duplicated region for block: B:477:0x11f5  */
    /* JADX WARNING: Removed duplicated region for block: B:115:0x056e  */
    /* JADX WARNING: Removed duplicated region for block: B:118:0x058e  */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x05ae  */
    /* JADX WARNING: Removed duplicated region for block: B:123:0x05c3 A:{SYNTHETIC, Splitter: B:123:0x05c3} */
    /* JADX WARNING: Removed duplicated region for block: B:141:0x06be  */
    /* JADX WARNING: Removed duplicated region for block: B:157:0x0708 A:{Catch:{ Throwable -> 0x1245 }} */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x0765  */
    /* JADX WARNING: Removed duplicated region for block: B:187:0x0845  */
    /* JADX WARNING: Removed duplicated region for block: B:189:0x0859  */
    /* JADX WARNING: Removed duplicated region for block: B:197:0x08a7  */
    /* JADX WARNING: Removed duplicated region for block: B:222:0x0942  */
    /* JADX WARNING: Removed duplicated region for block: B:225:0x097a  */
    /* JADX WARNING: Removed duplicated region for block: B:258:0x0a9e A:{Catch:{ Throwable -> 0x12c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x0b4e A:{SYNTHETIC, Splitter: B:288:0x0b4e} */
    /* JADX WARNING: Removed duplicated region for block: B:523:0x1328  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0b64  */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x0b71  */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0bc0  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0bea  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0bfe  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0c39  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c59  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0cf1  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0d10  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0d24  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x054e  */
    /* JADX WARNING: Removed duplicated region for block: B:115:0x056e  */
    /* JADX WARNING: Removed duplicated region for block: B:477:0x11f5  */
    /* JADX WARNING: Removed duplicated region for block: B:118:0x058e  */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x05ae  */
    /* JADX WARNING: Removed duplicated region for block: B:123:0x05c3 A:{SYNTHETIC, Splitter: B:123:0x05c3} */
    /* JADX WARNING: Removed duplicated region for block: B:141:0x06be  */
    /* JADX WARNING: Removed duplicated region for block: B:157:0x0708 A:{Catch:{ Throwable -> 0x1245 }} */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x0765  */
    /* JADX WARNING: Removed duplicated region for block: B:187:0x0845  */
    /* JADX WARNING: Removed duplicated region for block: B:189:0x0859  */
    /* JADX WARNING: Removed duplicated region for block: B:197:0x08a7  */
    /* JADX WARNING: Removed duplicated region for block: B:222:0x0942  */
    /* JADX WARNING: Removed duplicated region for block: B:225:0x097a  */
    /* JADX WARNING: Removed duplicated region for block: B:258:0x0a9e A:{Catch:{ Throwable -> 0x12c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x0b4e A:{SYNTHETIC, Splitter: B:288:0x0b4e} */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0b64  */
    /* JADX WARNING: Removed duplicated region for block: B:523:0x1328  */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x0b71  */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0bc0  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0bea  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0bfe  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0c39  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c59  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0cf1  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0d10  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0d24  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x054e  */
    /* JADX WARNING: Removed duplicated region for block: B:477:0x11f5  */
    /* JADX WARNING: Removed duplicated region for block: B:115:0x056e  */
    /* JADX WARNING: Removed duplicated region for block: B:118:0x058e  */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x05ae  */
    /* JADX WARNING: Removed duplicated region for block: B:123:0x05c3 A:{SYNTHETIC, Splitter: B:123:0x05c3} */
    /* JADX WARNING: Removed duplicated region for block: B:141:0x06be  */
    /* JADX WARNING: Removed duplicated region for block: B:157:0x0708 A:{Catch:{ Throwable -> 0x1245 }} */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x0765  */
    /* JADX WARNING: Removed duplicated region for block: B:187:0x0845  */
    /* JADX WARNING: Removed duplicated region for block: B:189:0x0859  */
    /* JADX WARNING: Removed duplicated region for block: B:197:0x08a7  */
    /* JADX WARNING: Removed duplicated region for block: B:222:0x0942  */
    /* JADX WARNING: Removed duplicated region for block: B:225:0x097a  */
    /* JADX WARNING: Removed duplicated region for block: B:258:0x0a9e A:{Catch:{ Throwable -> 0x12c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x0b4e A:{SYNTHETIC, Splitter: B:288:0x0b4e} */
    /* JADX WARNING: Removed duplicated region for block: B:523:0x1328  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0b64  */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x0b71  */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0bc0  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0bea  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0bfe  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0c39  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c59  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0cf1  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0d10  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0d24  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x0487  */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x049b  */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x04ca  */
    /* JADX WARNING: Removed duplicated region for block: B:141:0x06be  */
    /* JADX WARNING: Removed duplicated region for block: B:157:0x0708 A:{Catch:{ Throwable -> 0x1245 }} */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x0765  */
    /* JADX WARNING: Removed duplicated region for block: B:187:0x0845  */
    /* JADX WARNING: Removed duplicated region for block: B:189:0x0859  */
    /* JADX WARNING: Removed duplicated region for block: B:197:0x08a7  */
    /* JADX WARNING: Removed duplicated region for block: B:222:0x0942  */
    /* JADX WARNING: Removed duplicated region for block: B:225:0x097a  */
    /* JADX WARNING: Removed duplicated region for block: B:258:0x0a9e A:{Catch:{ Throwable -> 0x12c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x0b4e A:{SYNTHETIC, Splitter: B:288:0x0b4e} */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0b64  */
    /* JADX WARNING: Removed duplicated region for block: B:523:0x1328  */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x0b71  */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0bc0  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0bea  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0bfe  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0c39  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c59  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0cf1  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0d10  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0d24  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x054e  */
    /* JADX WARNING: Removed duplicated region for block: B:115:0x056e  */
    /* JADX WARNING: Removed duplicated region for block: B:477:0x11f5  */
    /* JADX WARNING: Removed duplicated region for block: B:118:0x058e  */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x05ae  */
    /* JADX WARNING: Removed duplicated region for block: B:123:0x05c3 A:{SYNTHETIC, Splitter: B:123:0x05c3} */
    /* JADX WARNING: Removed duplicated region for block: B:141:0x06be  */
    /* JADX WARNING: Removed duplicated region for block: B:157:0x0708 A:{Catch:{ Throwable -> 0x1245 }} */
    /* JADX WARNING: Removed duplicated region for block: B:160:0x0765  */
    /* JADX WARNING: Removed duplicated region for block: B:187:0x0845  */
    /* JADX WARNING: Removed duplicated region for block: B:189:0x0859  */
    /* JADX WARNING: Removed duplicated region for block: B:197:0x08a7  */
    /* JADX WARNING: Removed duplicated region for block: B:222:0x0942  */
    /* JADX WARNING: Removed duplicated region for block: B:225:0x097a  */
    /* JADX WARNING: Removed duplicated region for block: B:258:0x0a9e A:{Catch:{ Throwable -> 0x12c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x0b4e A:{SYNTHETIC, Splitter: B:288:0x0b4e} */
    /* JADX WARNING: Removed duplicated region for block: B:523:0x1328  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0b64  */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x0b71  */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0bc0  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0bea  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0bfe  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0c39  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c59  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0cf1  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0d10  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0d24  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:222:0x0942  */
    /* JADX WARNING: Removed duplicated region for block: B:225:0x097a  */
    /* JADX WARNING: Removed duplicated region for block: B:258:0x0a9e A:{Catch:{ Throwable -> 0x12c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x0b4e A:{SYNTHETIC, Splitter: B:288:0x0b4e} */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0b64  */
    /* JADX WARNING: Removed duplicated region for block: B:523:0x1328  */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x0b71  */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0bc0  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0bea  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0bfe  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0c39  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c59  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0cf1  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0d10  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0d24  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:187:0x0845  */
    /* JADX WARNING: Removed duplicated region for block: B:189:0x0859  */
    /* JADX WARNING: Removed duplicated region for block: B:197:0x08a7  */
    /* JADX WARNING: Removed duplicated region for block: B:222:0x0942  */
    /* JADX WARNING: Removed duplicated region for block: B:225:0x097a  */
    /* JADX WARNING: Removed duplicated region for block: B:258:0x0a9e A:{Catch:{ Throwable -> 0x12c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x0b4e A:{SYNTHETIC, Splitter: B:288:0x0b4e} */
    /* JADX WARNING: Removed duplicated region for block: B:523:0x1328  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0b64  */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x0b71  */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0bc0  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0bea  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0bfe  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0c39  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c59  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0cf1  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0d10  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0d24  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:363:0x0d7e  */
    /* JADX WARNING: Removed duplicated region for block: B:366:0x0dad  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:187:0x0845  */
    /* JADX WARNING: Removed duplicated region for block: B:189:0x0859  */
    /* JADX WARNING: Removed duplicated region for block: B:197:0x08a7  */
    /* JADX WARNING: Removed duplicated region for block: B:222:0x0942  */
    /* JADX WARNING: Removed duplicated region for block: B:225:0x097a  */
    /* JADX WARNING: Removed duplicated region for block: B:258:0x0a9e A:{Catch:{ Throwable -> 0x12c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x0b4e A:{SYNTHETIC, Splitter: B:288:0x0b4e} */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0b64  */
    /* JADX WARNING: Removed duplicated region for block: B:523:0x1328  */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x0b71  */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0bc0  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0bea  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0bfe  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0c39  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c59  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0cf1  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0d10  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0d24  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x0b4e A:{SYNTHETIC, Splitter: B:288:0x0b4e} */
    /* JADX WARNING: Removed duplicated region for block: B:523:0x1328  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0b64  */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x0b71  */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0bc0  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0bea  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0bfe  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0c39  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c59  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0cf1  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0d10  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0d24  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* JADX WARNING: Removed duplicated region for block: B:187:0x0845  */
    /* JADX WARNING: Removed duplicated region for block: B:189:0x0859  */
    /* JADX WARNING: Removed duplicated region for block: B:197:0x08a7  */
    /* JADX WARNING: Removed duplicated region for block: B:222:0x0942  */
    /* JADX WARNING: Removed duplicated region for block: B:225:0x097a  */
    /* JADX WARNING: Removed duplicated region for block: B:258:0x0a9e A:{Catch:{ Throwable -> 0x12c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:288:0x0b4e A:{SYNTHETIC, Splitter: B:288:0x0b4e} */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0b64  */
    /* JADX WARNING: Removed duplicated region for block: B:523:0x1328  */
    /* JADX WARNING: Removed duplicated region for block: B:294:0x0b71  */
    /* JADX WARNING: Removed duplicated region for block: B:313:0x0bc0  */
    /* JADX WARNING: Removed duplicated region for block: B:322:0x0bea  */
    /* JADX WARNING: Removed duplicated region for block: B:324:0x0bfe  */
    /* JADX WARNING: Removed duplicated region for block: B:331:0x0c39  */
    /* JADX WARNING: Removed duplicated region for block: B:334:0x0c59  */
    /* JADX WARNING: Removed duplicated region for block: B:337:0x0ca6  */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0cf1  */
    /* JADX WARNING: Removed duplicated region for block: B:348:0x0d10  */
    /* JADX WARNING: Removed duplicated region for block: B:350:0x0d24  */
    /* JADX WARNING: Removed duplicated region for block: B:384:0x0e6b  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x0eb4  */
    /* JADX WARNING: Removed duplicated region for block: B:393:0x0efa  */
    /* JADX WARNING: Removed duplicated region for block: B:399:0x0f21 A:{SYNTHETIC, Splitter: B:399:0x0f21} */
    /* JADX WARNING: Removed duplicated region for block: B:403:0x0f53 A:{SYNTHETIC, Splitter: B:403:0x0f53} */
    /* JADX WARNING: Removed duplicated region for block: B:409:0x0ff2  */
    /* JADX WARNING: Removed duplicated region for block: B:417:0x101c A:{Catch:{ Throwable -> 0x13c9 }} */
    /* JADX WARNING: Removed duplicated region for block: B:421:0x1032 A:{Catch:{ Throwable -> 0x13d6 }} */
    /* JADX WARNING: Removed duplicated region for block: B:425:0x1048 A:{Catch:{ Throwable -> 0x13e3 }} */
    /* JADX WARNING: Removed duplicated region for block: B:429:0x105e A:{Catch:{ Throwable -> 0x13f0 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "JianHui.Yu@Plf.SDK, 2016-05-20 : Modify for Longshot", property = OppoRomType.ROM)
    private void startOtherServices() {
        Throwable e;
        InputManagerService inputManager;
        OppoMultimediaService multimediaService;
        OppoRoundCornerService oppoRoundCornerService;
        OppoUsageService usageService;
        LocationManagerService location;
        CountryDetectorService countryDetector;
        ILockSettings lockSettings;
        OppoService oppoService;
        MediaRouterService mediaRouter;
        Throwable e2;
        MmsServiceBroker mmsService;
        Configuration config;
        DisplayMetrics metrics;
        Theme systemTheme;
        NetworkManagementService networkManagementF;
        NetworkStatsService networkStatsF;
        NetworkPolicyManagerService networkPolicyF;
        ConnectivityService connectivityF;
        NetworkScoreService networkScoreF;
        LocationManagerService locationF;
        CountryDetectorService countryDetectorF;
        NetworkTimeUpdateService networkTimeUpdaterF;
        CommonTimeManagementService commonTimeMgmtServiceF;
        InputManagerService inputManagerF;
        TelephonyRegistry telephonyRegistryF;
        MediaRouterService mediaRouterF;
        MmsServiceBroker mmsServiceF;
        WindowManagerService windowManagerF;
        OppoMultimediaService multimediaServiceF;
        OppoRoundCornerService oppoRoundCornerServiceF;
        OppoUsageService usageServiceF;
        OppoService mOppoService;
        OppoMultimediaService oppoMultimediaService;
        CabcService cabcService;
        CabcService cabcService2;
        HypnusService hypnusService;
        HypnusService hypnusService2;
        OppoService oppoService2;
        OppoAutoInstallService oppoAutoInstallService;
        OppoAutoInstallService oppoAutoInstallService2;
        OppoUsageService oppoUsageService;
        NetworkTimeUpdateService networkTimeUpdateService;
        CommonTimeManagementService commonTimeManagementService;
        NetworkPolicyManagerService networkPolicyManagerService;
        ConnectivityService connectivityService;
        CountryDetectorService countryDetectorService;
        HardwarePropertiesManagerService hardwarePropertiesManagerService;
        HardwarePropertiesManagerService hardwarePropertiesManagerService2;
        Context context = this.mSystemContext;
        VibratorService vibrator = null;
        IStorageManager storageManager = null;
        NetworkManagementService networkManagement = null;
        NetworkStatsService networkStats = null;
        NetworkPolicyManagerService networkPolicy = null;
        ConnectivityService connectivity = null;
        NetworkScoreService networkScore = null;
        WindowManagerService wm = null;
        NetworkTimeUpdateService networkTimeUpdater = null;
        CommonTimeManagementService commonTimeMgmtService = null;
        TelephonyRegistry telephonyRegistry = null;
        Object obj = null;
        Object obj2 = null;
        boolean disableStorage = SystemProperties.getBoolean("config.disable_storage", false);
        boolean disableBluetooth = SystemProperties.getBoolean("config.disable_bluetooth", false);
        boolean disableLocation = SystemProperties.getBoolean("config.disable_location", false);
        boolean disableSystemUI = SystemProperties.getBoolean("config.disable_systemui", false);
        boolean disableNonCoreServices = SystemProperties.getBoolean("config.disable_noncore", false);
        boolean disableNetwork = SystemProperties.getBoolean("config.disable_network", false);
        boolean disableNetworkTime = SystemProperties.getBoolean("config.disable_networktime", false);
        boolean disableRtt = SystemProperties.getBoolean("config.disable_rtt", false);
        boolean disableMediaProjection = SystemProperties.getBoolean("config.disable_mediaproj", false);
        boolean disableSerial = SystemProperties.getBoolean("config.disable_serial", false);
        boolean disableSearchManager = SystemProperties.getBoolean("config.disable_searchmanager", false);
        boolean disableTrustManager = SystemProperties.getBoolean("config.disable_trustmanager", false);
        boolean disableTextServices = SystemProperties.getBoolean("config.disable_textservices", false);
        boolean disableConsumerIr = SystemProperties.getBoolean("config.disable_consumerir", false);
        boolean disableVrManager = SystemProperties.getBoolean("config.disable_vrmanager", false);
        boolean disableCameraService = SystemProperties.getBoolean("config.disable_cameraservice", false);
        boolean enableLeftyService = SystemProperties.getBoolean("config.enable_lefty", false);
        boolean isEmulator = SystemProperties.get("ro.kernel.qemu").equals("1");
        boolean enableWigig = SystemProperties.getBoolean("persist.vendor.wigig.enable", false);
        if (Build.IS_DEBUGGABLE && SystemProperties.getBoolean("debug.crash_system", false)) {
            throw new RuntimeException();
        }
        try {
            VibratorService vibratorService;
            String SECONDARY_ZYGOTE_PRELOAD = "SecondaryZygotePreload";
            this.mZygotePreload = SystemServerInitThreadPool.get().submit(-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.$INST$2, "SecondaryZygotePreload");
            traceBeginAndSlog("StartKeyAttestationApplicationIdProviderService");
            ServiceManager.addService("sec_key_att_app_id_provider", new KeyAttestationApplicationIdProviderService(context));
            traceEnd();
            traceBeginAndSlog("StartKeyChainSystemService");
            this.mSystemServiceManager.startService(KeyChainSystemService.class);
            traceEnd();
            traceBeginAndSlog("StartSchedulingPolicyService");
            ServiceManager.addService("scheduling_policy", new SchedulingPolicyService());
            traceEnd();
            traceBeginAndSlog("StartTelecomLoaderService");
            this.mSystemServiceManager.startService(TelecomLoaderService.class);
            traceEnd();
            traceBeginAndSlog("StartTelephonyRegistry");
            TelephonyRegistry telephonyRegistry2 = new TelephonyRegistry(context);
            try {
                ServiceManager.addService("telephony.registry", telephonyRegistry2);
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
                traceEnd();
                traceBeginAndSlog("StartVibratorService");
                vibratorService = new VibratorService(context);
            } catch (RuntimeException e3) {
                e = e3;
                telephonyRegistry = telephonyRegistry2;
                inputManager = null;
                Slog.e("System", "******************************************");
                Slog.e("System", "************ Failure starting core service", e);
                multimediaService = null;
                oppoRoundCornerService = null;
                usageService = null;
                location = null;
                countryDetector = null;
                lockSettings = null;
                oppoService = null;
                mediaRouter = null;
                if (this.mFactoryTestMode != 1) {
                }
                traceBeginAndSlog("MakeDisplayReady");
                Slog.i(TAG, "displayReady");
                wm.displayReady();
                traceEnd();
                traceBeginAndSlog("StartStorageManagerService");
                try {
                    Slog.i(TAG, "mount service");
                    this.mSystemServiceManager.startService(STORAGE_MANAGER_SERVICE_CLASS);
                    storageManager = Stub.asInterface(ServiceManager.getService(OppoProcessManager.RESUME_REASON_MOUNT_STR));
                } catch (Throwable e22) {
                    reportWtf("starting StorageManagerService", e22);
                }
                traceEnd();
                traceBeginAndSlog("StartStorageStatsService");
                try {
                    this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
                } catch (Throwable e222) {
                    reportWtf("starting StorageStatsService", e222);
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
                if (this.mFactoryTestMode != 1) {
                }
                traceBeginAndSlog("StartMediaProjectionManager");
                this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                traceEnd();
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                }
                if (!disableCameraService) {
                }
                traceBeginAndSlog("StartJitCompilation");
                VMRuntime.getRuntime().startJitCompilation();
                traceEnd();
                traceBeginAndSlog("StartMmsService");
                mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                }
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
                if (enableWigig) {
                }
                Slog.i(TAG, "wms systemReady");
                wm.systemReady();
                traceEnd();
                config = wm.computeNewConfiguration(0);
                metrics = new DisplayMetrics();
                ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                context.getResources().updateConfiguration(config, metrics);
                systemTheme = context.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                }
                traceBeginAndSlog("MakePowerManagerServiceReady");
                Slog.i(TAG, "power manager systemReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                traceEnd();
                if (this.mSecrecyService != null) {
                }
                if (this.mFingerprintService != null) {
                }
                if (this.mFaceService != null) {
                }
                if (this.mBiometricsService != null) {
                }
                traceBeginAndSlog("MakePackageManagerServiceReady");
                Slog.i(TAG, "Package systemReady");
                this.mPackageManagerService.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                Slog.i(TAG, "DisplayManager systemReady");
                this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                traceEnd();
                this.mSystemServiceManager.setSafeMode(false);
                networkManagementF = networkManagement;
                networkStatsF = networkStats;
                networkPolicyF = networkPolicy;
                connectivityF = connectivity;
                networkScoreF = networkScore;
                locationF = location;
                countryDetectorF = countryDetector;
                networkTimeUpdaterF = networkTimeUpdater;
                commonTimeMgmtServiceF = commonTimeMgmtService;
                inputManagerF = inputManager;
                telephonyRegistryF = telephonyRegistry;
                mediaRouterF = mediaRouter;
                mmsServiceF = mmsService;
                windowManagerF = wm;
                multimediaServiceF = multimediaService;
                oppoRoundCornerServiceF = oppoRoundCornerService;
                usageServiceF = usageService;
                mOppoService = oppoService;
                Slog.i(TAG, "Ams systemReady");
                this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
            }
            try {
                ServiceManager.addService("vibrator", vibratorService);
                traceEnd();
                if (!disableConsumerIr) {
                    traceBeginAndSlog("StartConsumerIrService");
                    ConsumerIrService consumerIrService = new ConsumerIrService(context);
                    ConsumerIrService consumerIrService2;
                    try {
                        ServiceManager.addService("consumer_ir", consumerIrService);
                        traceEnd();
                        consumerIrService2 = consumerIrService;
                    } catch (RuntimeException e4) {
                        e = e4;
                        consumerIrService2 = consumerIrService;
                        telephonyRegistry = telephonyRegistry2;
                        inputManager = null;
                        vibrator = vibratorService;
                        Slog.e("System", "******************************************");
                        Slog.e("System", "************ Failure starting core service", e);
                        multimediaService = null;
                        oppoRoundCornerService = null;
                        usageService = null;
                        location = null;
                        countryDetector = null;
                        lockSettings = null;
                        oppoService = null;
                        mediaRouter = null;
                        if (this.mFactoryTestMode != 1) {
                        }
                        traceBeginAndSlog("MakeDisplayReady");
                        Slog.i(TAG, "displayReady");
                        wm.displayReady();
                        traceEnd();
                        traceBeginAndSlog("StartStorageManagerService");
                        Slog.i(TAG, "mount service");
                        this.mSystemServiceManager.startService(STORAGE_MANAGER_SERVICE_CLASS);
                        storageManager = Stub.asInterface(ServiceManager.getService(OppoProcessManager.RESUME_REASON_MOUNT_STR));
                        traceEnd();
                        traceBeginAndSlog("StartStorageStatsService");
                        this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
                        traceEnd();
                        traceBeginAndSlog("StartUiModeManager");
                        this.mSystemServiceManager.startService(UiModeManagerService.class);
                        traceEnd();
                        if (this.mOnlyCore) {
                        }
                        traceBeginAndSlog("PerformFstrimIfNeeded");
                        Slog.i(TAG, "performFstrimIfNeeded");
                        this.mPackageManagerService.performFstrimIfNeeded();
                        traceEnd();
                        if (this.mFactoryTestMode != 1) {
                        }
                        traceBeginAndSlog("StartMediaProjectionManager");
                        this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                        traceEnd();
                        if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                        }
                        if (disableCameraService) {
                        }
                        traceBeginAndSlog("StartJitCompilation");
                        VMRuntime.getRuntime().startJitCompilation();
                        traceEnd();
                        traceBeginAndSlog("StartMmsService");
                        mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                        }
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
                        if (enableWigig) {
                        }
                        Slog.i(TAG, "wms systemReady");
                        wm.systemReady();
                        traceEnd();
                        config = wm.computeNewConfiguration(0);
                        metrics = new DisplayMetrics();
                        ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                        context.getResources().updateConfiguration(config, metrics);
                        systemTheme = context.getTheme();
                        if (systemTheme.getChangingConfigurations() != 0) {
                        }
                        traceBeginAndSlog("MakePowerManagerServiceReady");
                        Slog.i(TAG, "power manager systemReady");
                        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                        traceEnd();
                        if (this.mSecrecyService != null) {
                        }
                        if (this.mFingerprintService != null) {
                        }
                        if (this.mFaceService != null) {
                        }
                        if (this.mBiometricsService != null) {
                        }
                        traceBeginAndSlog("MakePackageManagerServiceReady");
                        Slog.i(TAG, "Package systemReady");
                        this.mPackageManagerService.systemReady();
                        traceEnd();
                        traceBeginAndSlog("MakeDisplayManagerServiceReady");
                        Slog.i(TAG, "DisplayManager systemReady");
                        this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                        traceEnd();
                        this.mSystemServiceManager.setSafeMode(false);
                        networkManagementF = networkManagement;
                        networkStatsF = networkStats;
                        networkPolicyF = networkPolicy;
                        connectivityF = connectivity;
                        networkScoreF = networkScore;
                        locationF = location;
                        countryDetectorF = countryDetector;
                        networkTimeUpdaterF = networkTimeUpdater;
                        commonTimeMgmtServiceF = commonTimeMgmtService;
                        inputManagerF = inputManager;
                        telephonyRegistryF = telephonyRegistry;
                        mediaRouterF = mediaRouter;
                        mmsServiceF = mmsService;
                        windowManagerF = wm;
                        multimediaServiceF = multimediaService;
                        oppoRoundCornerServiceF = oppoRoundCornerService;
                        usageServiceF = usageService;
                        mOppoService = oppoService;
                        Slog.i(TAG, "Ams systemReady");
                        this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
                    }
                }
                traceBeginAndSlog("StartAlarmManagerService");
                Slog.i(TAG, "AlarmManager Service");
                this.mSystemServiceManager.startService(AlarmManagerService.class);
                traceEnd();
                traceBeginAndSlog("InitWatchdog");
                Watchdog.getInstance().init(context, this.mActivityManagerService);
                traceEnd();
                traceBeginAndSlog("StartInputManagerService");
                inputManager = new InputManagerService(context);
            } catch (RuntimeException e5) {
                e = e5;
                telephonyRegistry = telephonyRegistry2;
                inputManager = null;
                vibrator = vibratorService;
                Slog.e("System", "******************************************");
                Slog.e("System", "************ Failure starting core service", e);
                multimediaService = null;
                oppoRoundCornerService = null;
                usageService = null;
                location = null;
                countryDetector = null;
                lockSettings = null;
                oppoService = null;
                mediaRouter = null;
                if (this.mFactoryTestMode != 1) {
                }
                traceBeginAndSlog("MakeDisplayReady");
                Slog.i(TAG, "displayReady");
                wm.displayReady();
                traceEnd();
                traceBeginAndSlog("StartStorageManagerService");
                Slog.i(TAG, "mount service");
                this.mSystemServiceManager.startService(STORAGE_MANAGER_SERVICE_CLASS);
                storageManager = Stub.asInterface(ServiceManager.getService(OppoProcessManager.RESUME_REASON_MOUNT_STR));
                traceEnd();
                traceBeginAndSlog("StartStorageStatsService");
                this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
                traceEnd();
                traceBeginAndSlog("StartUiModeManager");
                this.mSystemServiceManager.startService(UiModeManagerService.class);
                traceEnd();
                if (this.mOnlyCore) {
                }
                traceBeginAndSlog("PerformFstrimIfNeeded");
                Slog.i(TAG, "performFstrimIfNeeded");
                this.mPackageManagerService.performFstrimIfNeeded();
                traceEnd();
                if (this.mFactoryTestMode != 1) {
                }
                traceBeginAndSlog("StartMediaProjectionManager");
                this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                traceEnd();
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                }
                if (disableCameraService) {
                }
                traceBeginAndSlog("StartJitCompilation");
                VMRuntime.getRuntime().startJitCompilation();
                traceEnd();
                traceBeginAndSlog("StartMmsService");
                mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                }
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
                if (enableWigig) {
                }
                Slog.i(TAG, "wms systemReady");
                wm.systemReady();
                traceEnd();
                config = wm.computeNewConfiguration(0);
                metrics = new DisplayMetrics();
                ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                context.getResources().updateConfiguration(config, metrics);
                systemTheme = context.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                }
                traceBeginAndSlog("MakePowerManagerServiceReady");
                Slog.i(TAG, "power manager systemReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                traceEnd();
                if (this.mSecrecyService != null) {
                }
                if (this.mFingerprintService != null) {
                }
                if (this.mFaceService != null) {
                }
                if (this.mBiometricsService != null) {
                }
                traceBeginAndSlog("MakePackageManagerServiceReady");
                Slog.i(TAG, "Package systemReady");
                this.mPackageManagerService.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                Slog.i(TAG, "DisplayManager systemReady");
                this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                traceEnd();
                this.mSystemServiceManager.setSafeMode(false);
                networkManagementF = networkManagement;
                networkStatsF = networkStats;
                networkPolicyF = networkPolicy;
                connectivityF = connectivity;
                networkScoreF = networkScore;
                locationF = location;
                countryDetectorF = countryDetector;
                networkTimeUpdaterF = networkTimeUpdater;
                commonTimeMgmtServiceF = commonTimeMgmtService;
                inputManagerF = inputManager;
                telephonyRegistryF = telephonyRegistry;
                mediaRouterF = mediaRouter;
                mmsServiceF = mmsService;
                windowManagerF = wm;
                multimediaServiceF = multimediaService;
                oppoRoundCornerServiceF = oppoRoundCornerService;
                usageServiceF = usageService;
                mOppoService = oppoService;
                Slog.i(TAG, "Ams systemReady");
                this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
            }
            try {
                traceEnd();
                traceBeginAndSlog("StartWindowManagerService");
                ConcurrentUtils.waitForFutureNoInterrupt(this.mSensorServiceStart, START_SENSOR_SERVICE);
                this.mSensorServiceStart = null;
                wm = WindowManagerService.main(context, inputManager, this.mFactoryTestMode != 1, this.mFirstBoot ^ 1, this.mOnlyCore, new OppoPhoneWindowManager());
                ServiceManager.addService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR, wm);
                ServiceManager.addService("input", inputManager);
                traceEnd();
                SystemServerInitThreadPool.get().submit(-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.$INST$3, START_HIDL_SERVICES);
                if (!disableVrManager) {
                    traceBeginAndSlog("StartVrManagerService");
                    this.mSystemServiceManager.startService(VrManagerService.class);
                    traceEnd();
                }
                traceBeginAndSlog("SetWindowManagerService");
                this.mActivityManagerService.setWindowManager(wm);
                traceEnd();
                traceBeginAndSlog("StartInputManager");
                inputManager.setWindowManagerCallbacks(wm.getInputMonitor());
                inputManager.start();
                traceEnd();
                traceBeginAndSlog("DisplayManagerWindowManagerAndInputReady");
                this.mDisplayManagerService.windowManagerAndInputReady();
                traceEnd();
                if (isEmulator) {
                    Slog.i(TAG, "No Bluetooth Service (emulator)");
                } else if (this.mFactoryTestMode == 1) {
                    Slog.i(TAG, "No Bluetooth Service (factory test)");
                } else if (!context.getPackageManager().hasSystemFeature("android.hardware.bluetooth")) {
                    Slog.i(TAG, "No Bluetooth Service (Bluetooth Hardware Not Present)");
                } else if (disableBluetooth) {
                    Slog.i(TAG, "Bluetooth Service disabled by config");
                } else {
                    traceBeginAndSlog("StartBluetoothService");
                    this.mSystemServiceManager.startService(BluetoothService.class);
                    traceEnd();
                }
                traceBeginAndSlog("IpConnectivityMetrics");
                this.mSystemServiceManager.startService(IpConnectivityMetrics.class);
                traceEnd();
                traceBeginAndSlog("PinnerService");
                this.mSystemServiceManager.startService(PinnerService.class);
                traceEnd();
                SystemServer.addService(context);
                telephonyRegistry = telephonyRegistry2;
                vibrator = vibratorService;
            } catch (RuntimeException e6) {
                e = e6;
                telephonyRegistry = telephonyRegistry2;
                vibrator = vibratorService;
                Slog.e("System", "******************************************");
                Slog.e("System", "************ Failure starting core service", e);
                multimediaService = null;
                oppoRoundCornerService = null;
                usageService = null;
                location = null;
                countryDetector = null;
                lockSettings = null;
                oppoService = null;
                mediaRouter = null;
                if (this.mFactoryTestMode != 1) {
                }
                traceBeginAndSlog("MakeDisplayReady");
                Slog.i(TAG, "displayReady");
                wm.displayReady();
                traceEnd();
                traceBeginAndSlog("StartStorageManagerService");
                Slog.i(TAG, "mount service");
                this.mSystemServiceManager.startService(STORAGE_MANAGER_SERVICE_CLASS);
                storageManager = Stub.asInterface(ServiceManager.getService(OppoProcessManager.RESUME_REASON_MOUNT_STR));
                traceEnd();
                traceBeginAndSlog("StartStorageStatsService");
                this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
                traceEnd();
                traceBeginAndSlog("StartUiModeManager");
                this.mSystemServiceManager.startService(UiModeManagerService.class);
                traceEnd();
                if (this.mOnlyCore) {
                }
                traceBeginAndSlog("PerformFstrimIfNeeded");
                Slog.i(TAG, "performFstrimIfNeeded");
                this.mPackageManagerService.performFstrimIfNeeded();
                traceEnd();
                if (this.mFactoryTestMode != 1) {
                }
                traceBeginAndSlog("StartMediaProjectionManager");
                this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                traceEnd();
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                }
                if (disableCameraService) {
                }
                traceBeginAndSlog("StartJitCompilation");
                VMRuntime.getRuntime().startJitCompilation();
                traceEnd();
                traceBeginAndSlog("StartMmsService");
                mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                }
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
                if (enableWigig) {
                }
                Slog.i(TAG, "wms systemReady");
                wm.systemReady();
                traceEnd();
                config = wm.computeNewConfiguration(0);
                metrics = new DisplayMetrics();
                ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                context.getResources().updateConfiguration(config, metrics);
                systemTheme = context.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                }
                traceBeginAndSlog("MakePowerManagerServiceReady");
                Slog.i(TAG, "power manager systemReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                traceEnd();
                if (this.mSecrecyService != null) {
                }
                if (this.mFingerprintService != null) {
                }
                if (this.mFaceService != null) {
                }
                if (this.mBiometricsService != null) {
                }
                traceBeginAndSlog("MakePackageManagerServiceReady");
                Slog.i(TAG, "Package systemReady");
                this.mPackageManagerService.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                Slog.i(TAG, "DisplayManager systemReady");
                this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                traceEnd();
                this.mSystemServiceManager.setSafeMode(false);
                networkManagementF = networkManagement;
                networkStatsF = networkStats;
                networkPolicyF = networkPolicy;
                connectivityF = connectivity;
                networkScoreF = networkScore;
                locationF = location;
                countryDetectorF = countryDetector;
                networkTimeUpdaterF = networkTimeUpdater;
                commonTimeMgmtServiceF = commonTimeMgmtService;
                inputManagerF = inputManager;
                telephonyRegistryF = telephonyRegistry;
                mediaRouterF = mediaRouter;
                mmsServiceF = mmsService;
                windowManagerF = wm;
                multimediaServiceF = multimediaService;
                oppoRoundCornerServiceF = oppoRoundCornerService;
                usageServiceF = usageService;
                mOppoService = oppoService;
                Slog.i(TAG, "Ams systemReady");
                this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
            }
        } catch (RuntimeException e7) {
            e = e7;
            inputManager = null;
            Slog.e("System", "******************************************");
            Slog.e("System", "************ Failure starting core service", e);
            multimediaService = null;
            oppoRoundCornerService = null;
            usageService = null;
            location = null;
            countryDetector = null;
            lockSettings = null;
            oppoService = null;
            mediaRouter = null;
            if (this.mFactoryTestMode != 1) {
            }
            traceBeginAndSlog("MakeDisplayReady");
            Slog.i(TAG, "displayReady");
            wm.displayReady();
            traceEnd();
            traceBeginAndSlog("StartStorageManagerService");
            Slog.i(TAG, "mount service");
            this.mSystemServiceManager.startService(STORAGE_MANAGER_SERVICE_CLASS);
            storageManager = Stub.asInterface(ServiceManager.getService(OppoProcessManager.RESUME_REASON_MOUNT_STR));
            traceEnd();
            traceBeginAndSlog("StartStorageStatsService");
            this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
            traceEnd();
            traceBeginAndSlog("StartUiModeManager");
            this.mSystemServiceManager.startService(UiModeManagerService.class);
            traceEnd();
            if (this.mOnlyCore) {
            }
            traceBeginAndSlog("PerformFstrimIfNeeded");
            Slog.i(TAG, "performFstrimIfNeeded");
            this.mPackageManagerService.performFstrimIfNeeded();
            traceEnd();
            if (this.mFactoryTestMode != 1) {
            }
            traceBeginAndSlog("StartMediaProjectionManager");
            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
            traceEnd();
            if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
            }
            if (disableCameraService) {
            }
            traceBeginAndSlog("StartJitCompilation");
            VMRuntime.getRuntime().startJitCompilation();
            traceEnd();
            traceBeginAndSlog("StartMmsService");
            mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
            traceEnd();
            if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
            }
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
            if (enableWigig) {
            }
            Slog.i(TAG, "wms systemReady");
            wm.systemReady();
            traceEnd();
            config = wm.computeNewConfiguration(0);
            metrics = new DisplayMetrics();
            ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
            context.getResources().updateConfiguration(config, metrics);
            systemTheme = context.getTheme();
            if (systemTheme.getChangingConfigurations() != 0) {
            }
            traceBeginAndSlog("MakePowerManagerServiceReady");
            Slog.i(TAG, "power manager systemReady");
            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
            traceEnd();
            if (this.mSecrecyService != null) {
            }
            if (this.mFingerprintService != null) {
            }
            if (this.mFaceService != null) {
            }
            if (this.mBiometricsService != null) {
            }
            traceBeginAndSlog("MakePackageManagerServiceReady");
            Slog.i(TAG, "Package systemReady");
            this.mPackageManagerService.systemReady();
            traceEnd();
            traceBeginAndSlog("MakeDisplayManagerServiceReady");
            Slog.i(TAG, "DisplayManager systemReady");
            this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
            traceEnd();
            this.mSystemServiceManager.setSafeMode(false);
            networkManagementF = networkManagement;
            networkStatsF = networkStats;
            networkPolicyF = networkPolicy;
            connectivityF = connectivity;
            networkScoreF = networkScore;
            locationF = location;
            countryDetectorF = countryDetector;
            networkTimeUpdaterF = networkTimeUpdater;
            commonTimeMgmtServiceF = commonTimeMgmtService;
            inputManagerF = inputManager;
            telephonyRegistryF = telephonyRegistry;
            mediaRouterF = mediaRouter;
            mmsServiceF = mmsService;
            windowManagerF = wm;
            multimediaServiceF = multimediaService;
            oppoRoundCornerServiceF = oppoRoundCornerService;
            usageServiceF = usageService;
            mOppoService = oppoService;
            Slog.i(TAG, "Ams systemReady");
            this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
        }
        multimediaService = null;
        oppoRoundCornerService = null;
        usageService = null;
        location = null;
        countryDetector = null;
        lockSettings = null;
        oppoService = null;
        mediaRouter = null;
        if (this.mFactoryTestMode != 1) {
            traceBeginAndSlog("StartInputMethodManagerLifecycle");
            this.mSystemServiceManager.startService(InputMethodManagerService.Lifecycle.class);
            traceEnd();
            traceBeginAndSlog("StartAccessibilityManagerService");
            try {
                ServiceManager.addService("accessibility", new AccessibilityManagerService(context));
            } catch (Throwable e2222) {
                reportWtf("starting Accessibility Manager", e2222);
            }
            traceEnd();
        }
        traceBeginAndSlog("MakeDisplayReady");
        try {
            Slog.i(TAG, "displayReady");
            wm.displayReady();
        } catch (Throwable e22222) {
            reportWtf("making display ready", e22222);
        }
        traceEnd();
        if (!(this.mFactoryTestMode == 1 || disableStorage || ("0".equals(SystemProperties.get("system_init.startmountservice")) ^ 1) == 0)) {
            traceBeginAndSlog("StartStorageManagerService");
            Slog.i(TAG, "mount service");
            this.mSystemServiceManager.startService(STORAGE_MANAGER_SERVICE_CLASS);
            storageManager = Stub.asInterface(ServiceManager.getService(OppoProcessManager.RESUME_REASON_MOUNT_STR));
            traceEnd();
            traceBeginAndSlog("StartStorageStatsService");
            this.mSystemServiceManager.startService(STORAGE_STATS_SERVICE_CLASS);
            traceEnd();
        }
        traceBeginAndSlog("StartUiModeManager");
        this.mSystemServiceManager.startService(UiModeManagerService.class);
        traceEnd();
        if (this.mOnlyCore) {
            traceBeginAndSlog("UpdatePackagesIfNeeded");
            try {
                this.mPackageManagerService.updatePackagesIfNeeded();
            } catch (Throwable e222222) {
                reportWtf("update packages", e222222);
            }
            traceEnd();
        }
        traceBeginAndSlog("PerformFstrimIfNeeded");
        try {
            Slog.i(TAG, "performFstrimIfNeeded");
            this.mPackageManagerService.performFstrimIfNeeded();
        } catch (Throwable e2222222) {
            reportWtf("performing fstrim", e2222222);
        }
        traceEnd();
        if (this.mFactoryTestMode != 1) {
            if (!disableNonCoreServices) {
                traceBeginAndSlog("StartLockSettingsService");
                try {
                    this.mSystemServiceManager.startService(LOCK_SETTINGS_SERVICE_CLASS);
                    lockSettings = ILockSettings.Stub.asInterface(ServiceManager.getService("lock_settings"));
                } catch (Throwable e22222222) {
                    reportWtf("starting LockSettingsService service", e22222222);
                }
                traceEnd();
                boolean hasPdb = SystemProperties.get(PERSISTENT_DATA_BLOCK_PROP).equals("") ^ 1;
                if (hasPdb) {
                    traceBeginAndSlog("StartPersistentDataBlock");
                    this.mSystemServiceManager.startService(PersistentDataBlockService.class);
                    traceEnd();
                }
                if (hasPdb || OemLockService.isHalPresent()) {
                    traceBeginAndSlog("StartOemLockService");
                    this.mSystemServiceManager.startService(OemLockService.class);
                    traceEnd();
                }
                traceBeginAndSlog("StartDeviceIdleController");
                this.mSystemServiceManager.startService(DeviceIdleController.class);
                traceEnd();
                traceBeginAndSlog("StartDevicePolicyManager");
                Slog.i(TAG, "DevicePolicyManagerService");
                this.mSystemServiceManager.startService(DevicePolicyManagerService.Lifecycle.class);
                traceEnd();
            }
            if (!disableSystemUI) {
                traceBeginAndSlog("StartStatusBarManagerService");
                try {
                    StatusBarManagerService colorStatusBarManagerService = new ColorStatusBarManagerService(context, wm);
                    StatusBarManagerService statusBarManagerService;
                    try {
                        ServiceManager.addService("statusbar", colorStatusBarManagerService);
                        statusBarManagerService = colorStatusBarManagerService;
                    } catch (Throwable th) {
                        e22222222 = th;
                        statusBarManagerService = colorStatusBarManagerService;
                        reportWtf("starting StatusBarManagerService", e22222222);
                        traceEnd();
                        if (!disableNonCoreServices) {
                        }
                        if (!disableNetwork) {
                        }
                        traceBeginAndSlog("StartTextServicesManager");
                        this.mSystemServiceManager.startService(TextServicesManagerService.Lifecycle.class);
                        traceEnd();
                        if (!disableNetwork) {
                        }
                        if (!disableNonCoreServices) {
                        }
                        traceBeginAndSlog("WaitForAsecScan");
                        try {
                            storageManager.waitForAsecScan();
                        } catch (RemoteException e8) {
                        }
                        traceEnd();
                        Slog.i(TAG, "Oppo Expand Service");
                        ServiceManager.addService("OPPOExService", new OppoExService(context, wm));
                        if (this.mOppoLightsService != null) {
                        }
                        traceBeginAndSlog("StartNotificationManager");
                        Slog.i(TAG, "NotificationManagerService");
                        this.mSystemServiceManager.startService(NotificationManagerService.class);
                        SystemNotificationChannels.createAll(context);
                        networkPolicy.bindNotificationManager(INotificationManager.Stub.asInterface(ServiceManager.getService("notification")));
                        traceEnd();
                        traceBeginAndSlog("StartDeviceMonitor");
                        this.mSystemServiceManager.startService(OppoDeviceStorageMonitorService.class);
                        Slog.i(TAG, "add OppoDeviceStorageMonitorService");
                        traceEnd();
                        if (!disableLocation) {
                        }
                        traceBeginAndSlog("StartSearchManagerService");
                        try {
                            this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                        } catch (Throwable e222222222) {
                            reportWtf("starting Search Service", e222222222);
                        }
                        traceEnd();
                        AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, "systemserver pid:" + Process.myPid());
                        AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, "systemserver pid:" + Process.myPid());
                        traceBeginAndSlog("StartWallpaperManagerService");
                        this.mSystemServiceManager.startService(WALLPAPER_SERVICE_CLASS);
                        traceEnd();
                        traceBeginAndSlog("StartAudioService");
                        this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.hardware.broadcastradio")) {
                        }
                        if (!disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartWiredAccessoryManager");
                        inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                        traceEnd();
                        if (!disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartTwilightService");
                        this.mSystemServiceManager.startService(TwilightService.class);
                        traceEnd();
                        if (NightDisplayController.isAvailable(context)) {
                        }
                        traceBeginAndSlog("StartJobScheduler");
                        this.mSystemServiceManager.startService(JobSchedulerService.class);
                        traceEnd();
                        traceBeginAndSlog("StartSoundTrigger");
                        this.mSystemServiceManager.startService(SoundTriggerService.class);
                        traceEnd();
                        if (!disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartDiskStatsService");
                        ServiceManager.addService("diskstats", new DiskStatsService(context));
                        traceEnd();
                        Slog.i(TAG, "+OppoMultimediaService");
                        oppoMultimediaService = new OppoMultimediaService(context);
                        try {
                            ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                            Slog.i(TAG, "-OppoMultimediaService");
                            multimediaService = oppoMultimediaService;
                        } catch (Throwable th2) {
                            e222222222 = th2;
                            multimediaService = oppoMultimediaService;
                            reportWtf("starting OppoMultimediaService", e222222222);
                            traceBeginAndSlog("CabcService");
                            Slog.i(TAG, "Cabc Service");
                            cabcService = new CabcService(context);
                            ServiceManager.addService("cabc", cabcService);
                            cabcService2 = cabcService;
                            Trace.traceEnd(524288);
                            traceBeginAndSlog("OppoRoundCornerService");
                            oppoRoundCornerService = null;
                            if (SystemProperties.getInt("persist.sys.enable.rc", 0) == 1) {
                            }
                            traceBeginAndSlog("StartHypnusService");
                            Slog.i(TAG, "Hypnus Service");
                            hypnusService = new HypnusService(context);
                            ServiceManager.addService("hypnus", hypnusService);
                            hypnusService2 = hypnusService;
                            traceEnd();
                            Slog.i(TAG, "Oppo Customize Service");
                            ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                            Slog.i(TAG, "Oppo Service");
                            oppoService2 = new OppoService(context);
                            ServiceManager.addService("OPPO", oppoService2);
                            oppoService = oppoService2;
                            Slog.i(TAG, "OppoAutoInstallService");
                            oppoAutoInstallService = new OppoAutoInstallService(context);
                            ServiceManager.addService("oppoautoinstall", oppoAutoInstallService);
                            oppoAutoInstallService2 = oppoAutoInstallService;
                            Slog.i(TAG, "OPPO Usage Service");
                            oppoUsageService = new OppoUsageService(context);
                            ServiceManager.addService("usage", oppoUsageService);
                            usageService = oppoUsageService;
                            if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                            }
                            if (this.mOnlyCore) {
                            }
                            if (this.mOnlyCore ? context.getResources().getBoolean(17956963) : false) {
                            }
                            traceBeginAndSlog("StartNetworkTimeUpdateService");
                            networkTimeUpdateService = new NetworkTimeUpdateService(context);
                            try {
                                ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                                networkTimeUpdater = networkTimeUpdateService;
                            } catch (Throwable th3) {
                                e222222222 = th3;
                                networkTimeUpdater = networkTimeUpdateService;
                                reportWtf("starting NetworkTimeUpdate service", e222222222);
                                traceEnd();
                                traceBeginAndSlog("StartCommonTimeManagementService");
                                commonTimeManagementService = new CommonTimeManagementService(context);
                                ServiceManager.addService("commontime_management", commonTimeManagementService);
                                commonTimeMgmtService = commonTimeManagementService;
                                traceEnd();
                                if (!disableNetwork) {
                                }
                                traceBeginAndSlog("StartEmergencyAffordanceService");
                                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                                traceEnd();
                                if (!disableNonCoreServices) {
                                }
                                if (!disableNonCoreServices) {
                                }
                                traceBeginAndSlog("AddCoverageService");
                                ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                                traceEnd();
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
                                if (!disableNonCoreServices) {
                                }
                                traceBeginAndSlog("StartShortcutServiceLifecycle");
                                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                traceEnd();
                                traceBeginAndSlog("StartLauncherAppsService");
                                this.mSystemServiceManager.startService(LauncherAppsService.class);
                                traceEnd();
                                Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                                this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                                Slog.i(TAG, "Secrecy Service");
                                this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                                traceBeginAndSlog("StartMediaProjectionManager");
                                this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                                traceEnd();
                                if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                                }
                                if (disableCameraService) {
                                }
                                traceBeginAndSlog("StartJitCompilation");
                                VMRuntime.getRuntime().startJitCompilation();
                                traceEnd();
                                traceBeginAndSlog("StartMmsService");
                                mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                                }
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
                                if (enableWigig) {
                                }
                                Slog.i(TAG, "wms systemReady");
                                wm.systemReady();
                                traceEnd();
                                config = wm.computeNewConfiguration(0);
                                metrics = new DisplayMetrics();
                                ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                                context.getResources().updateConfiguration(config, metrics);
                                systemTheme = context.getTheme();
                                if (systemTheme.getChangingConfigurations() != 0) {
                                }
                                traceBeginAndSlog("MakePowerManagerServiceReady");
                                Slog.i(TAG, "power manager systemReady");
                                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                traceEnd();
                                if (this.mSecrecyService != null) {
                                }
                                if (this.mFingerprintService != null) {
                                }
                                if (this.mFaceService != null) {
                                }
                                if (this.mBiometricsService != null) {
                                }
                                traceBeginAndSlog("MakePackageManagerServiceReady");
                                Slog.i(TAG, "Package systemReady");
                                this.mPackageManagerService.systemReady();
                                traceEnd();
                                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                                Slog.i(TAG, "DisplayManager systemReady");
                                this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                                traceEnd();
                                this.mSystemServiceManager.setSafeMode(false);
                                networkManagementF = networkManagement;
                                networkStatsF = networkStats;
                                networkPolicyF = networkPolicy;
                                connectivityF = connectivity;
                                networkScoreF = networkScore;
                                locationF = location;
                                countryDetectorF = countryDetector;
                                networkTimeUpdaterF = networkTimeUpdater;
                                commonTimeMgmtServiceF = commonTimeMgmtService;
                                inputManagerF = inputManager;
                                telephonyRegistryF = telephonyRegistry;
                                mediaRouterF = mediaRouter;
                                mmsServiceF = mmsService;
                                windowManagerF = wm;
                                multimediaServiceF = multimediaService;
                                oppoRoundCornerServiceF = oppoRoundCornerService;
                                usageServiceF = usageService;
                                mOppoService = oppoService;
                                Slog.i(TAG, "Ams systemReady");
                                this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
                            }
                            traceEnd();
                            traceBeginAndSlog("StartCommonTimeManagementService");
                            commonTimeManagementService = new CommonTimeManagementService(context);
                            ServiceManager.addService("commontime_management", commonTimeManagementService);
                            commonTimeMgmtService = commonTimeManagementService;
                            traceEnd();
                            if (disableNetwork) {
                            }
                            traceBeginAndSlog("StartEmergencyAffordanceService");
                            this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                            traceEnd();
                            if (disableNonCoreServices) {
                            }
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("AddCoverageService");
                            ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                            traceEnd();
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
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartShortcutServiceLifecycle");
                            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                            traceEnd();
                            traceBeginAndSlog("StartLauncherAppsService");
                            this.mSystemServiceManager.startService(LauncherAppsService.class);
                            traceEnd();
                            Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                            this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                            Slog.i(TAG, "Secrecy Service");
                            this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                            traceBeginAndSlog("StartMediaProjectionManager");
                            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                            traceEnd();
                            if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                            }
                            if (disableCameraService) {
                            }
                            traceBeginAndSlog("StartJitCompilation");
                            VMRuntime.getRuntime().startJitCompilation();
                            traceEnd();
                            traceBeginAndSlog("StartMmsService");
                            mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                            traceEnd();
                            if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                            }
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
                            if (enableWigig) {
                            }
                            Slog.i(TAG, "wms systemReady");
                            wm.systemReady();
                            traceEnd();
                            config = wm.computeNewConfiguration(0);
                            metrics = new DisplayMetrics();
                            ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                            context.getResources().updateConfiguration(config, metrics);
                            systemTheme = context.getTheme();
                            if (systemTheme.getChangingConfigurations() != 0) {
                            }
                            traceBeginAndSlog("MakePowerManagerServiceReady");
                            Slog.i(TAG, "power manager systemReady");
                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                            traceEnd();
                            if (this.mSecrecyService != null) {
                            }
                            if (this.mFingerprintService != null) {
                            }
                            if (this.mFaceService != null) {
                            }
                            if (this.mBiometricsService != null) {
                            }
                            traceBeginAndSlog("MakePackageManagerServiceReady");
                            Slog.i(TAG, "Package systemReady");
                            this.mPackageManagerService.systemReady();
                            traceEnd();
                            traceBeginAndSlog("MakeDisplayManagerServiceReady");
                            Slog.i(TAG, "DisplayManager systemReady");
                            this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                            traceEnd();
                            this.mSystemServiceManager.setSafeMode(false);
                            networkManagementF = networkManagement;
                            networkStatsF = networkStats;
                            networkPolicyF = networkPolicy;
                            connectivityF = connectivity;
                            networkScoreF = networkScore;
                            locationF = location;
                            countryDetectorF = countryDetector;
                            networkTimeUpdaterF = networkTimeUpdater;
                            commonTimeMgmtServiceF = commonTimeMgmtService;
                            inputManagerF = inputManager;
                            telephonyRegistryF = telephonyRegistry;
                            mediaRouterF = mediaRouter;
                            mmsServiceF = mmsService;
                            windowManagerF = wm;
                            multimediaServiceF = multimediaService;
                            oppoRoundCornerServiceF = oppoRoundCornerService;
                            usageServiceF = usageService;
                            mOppoService = oppoService;
                            Slog.i(TAG, "Ams systemReady");
                            this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
                        }
                        traceBeginAndSlog("CabcService");
                        Slog.i(TAG, "Cabc Service");
                        cabcService = new CabcService(context);
                        try {
                            ServiceManager.addService("cabc", cabcService);
                            cabcService2 = cabcService;
                        } catch (Throwable th4) {
                            e222222222 = th4;
                            cabcService2 = cabcService;
                            reportWtf("starting Cabc Service", e222222222);
                            Trace.traceEnd(524288);
                            traceBeginAndSlog("OppoRoundCornerService");
                            oppoRoundCornerService = null;
                            if (SystemProperties.getInt("persist.sys.enable.rc", 0) == 1) {
                            }
                            traceBeginAndSlog("StartHypnusService");
                            Slog.i(TAG, "Hypnus Service");
                            hypnusService = new HypnusService(context);
                            ServiceManager.addService("hypnus", hypnusService);
                            hypnusService2 = hypnusService;
                            traceEnd();
                            Slog.i(TAG, "Oppo Customize Service");
                            ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                            Slog.i(TAG, "Oppo Service");
                            oppoService2 = new OppoService(context);
                            ServiceManager.addService("OPPO", oppoService2);
                            oppoService = oppoService2;
                            Slog.i(TAG, "OppoAutoInstallService");
                            oppoAutoInstallService = new OppoAutoInstallService(context);
                            ServiceManager.addService("oppoautoinstall", oppoAutoInstallService);
                            oppoAutoInstallService2 = oppoAutoInstallService;
                            Slog.i(TAG, "OPPO Usage Service");
                            oppoUsageService = new OppoUsageService(context);
                            ServiceManager.addService("usage", oppoUsageService);
                            usageService = oppoUsageService;
                            if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                            }
                            if (this.mOnlyCore) {
                            }
                            if (this.mOnlyCore ? context.getResources().getBoolean(17956963) : false) {
                            }
                            traceBeginAndSlog("StartNetworkTimeUpdateService");
                            networkTimeUpdateService = new NetworkTimeUpdateService(context);
                            ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                            networkTimeUpdater = networkTimeUpdateService;
                            traceEnd();
                            traceBeginAndSlog("StartCommonTimeManagementService");
                            commonTimeManagementService = new CommonTimeManagementService(context);
                            ServiceManager.addService("commontime_management", commonTimeManagementService);
                            commonTimeMgmtService = commonTimeManagementService;
                            traceEnd();
                            if (disableNetwork) {
                            }
                            traceBeginAndSlog("StartEmergencyAffordanceService");
                            this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                            traceEnd();
                            if (disableNonCoreServices) {
                            }
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("AddCoverageService");
                            ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                            traceEnd();
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
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartShortcutServiceLifecycle");
                            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                            traceEnd();
                            traceBeginAndSlog("StartLauncherAppsService");
                            this.mSystemServiceManager.startService(LauncherAppsService.class);
                            traceEnd();
                            Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                            this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                            Slog.i(TAG, "Secrecy Service");
                            this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                            traceBeginAndSlog("StartMediaProjectionManager");
                            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                            traceEnd();
                            if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                            }
                            if (disableCameraService) {
                            }
                            traceBeginAndSlog("StartJitCompilation");
                            VMRuntime.getRuntime().startJitCompilation();
                            traceEnd();
                            traceBeginAndSlog("StartMmsService");
                            mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                            traceEnd();
                            if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                            }
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
                            if (enableWigig) {
                            }
                            Slog.i(TAG, "wms systemReady");
                            wm.systemReady();
                            traceEnd();
                            config = wm.computeNewConfiguration(0);
                            metrics = new DisplayMetrics();
                            ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                            context.getResources().updateConfiguration(config, metrics);
                            systemTheme = context.getTheme();
                            if (systemTheme.getChangingConfigurations() != 0) {
                            }
                            traceBeginAndSlog("MakePowerManagerServiceReady");
                            Slog.i(TAG, "power manager systemReady");
                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                            traceEnd();
                            if (this.mSecrecyService != null) {
                            }
                            if (this.mFingerprintService != null) {
                            }
                            if (this.mFaceService != null) {
                            }
                            if (this.mBiometricsService != null) {
                            }
                            traceBeginAndSlog("MakePackageManagerServiceReady");
                            Slog.i(TAG, "Package systemReady");
                            this.mPackageManagerService.systemReady();
                            traceEnd();
                            traceBeginAndSlog("MakeDisplayManagerServiceReady");
                            Slog.i(TAG, "DisplayManager systemReady");
                            this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                            traceEnd();
                            this.mSystemServiceManager.setSafeMode(false);
                            networkManagementF = networkManagement;
                            networkStatsF = networkStats;
                            networkPolicyF = networkPolicy;
                            connectivityF = connectivity;
                            networkScoreF = networkScore;
                            locationF = location;
                            countryDetectorF = countryDetector;
                            networkTimeUpdaterF = networkTimeUpdater;
                            commonTimeMgmtServiceF = commonTimeMgmtService;
                            inputManagerF = inputManager;
                            telephonyRegistryF = telephonyRegistry;
                            mediaRouterF = mediaRouter;
                            mmsServiceF = mmsService;
                            windowManagerF = wm;
                            multimediaServiceF = multimediaService;
                            oppoRoundCornerServiceF = oppoRoundCornerService;
                            usageServiceF = usageService;
                            mOppoService = oppoService;
                            Slog.i(TAG, "Ams systemReady");
                            this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
                        }
                        Trace.traceEnd(524288);
                        traceBeginAndSlog("OppoRoundCornerService");
                        oppoRoundCornerService = null;
                        if (SystemProperties.getInt("persist.sys.enable.rc", 0) == 1) {
                        }
                        traceBeginAndSlog("StartHypnusService");
                        Slog.i(TAG, "Hypnus Service");
                        hypnusService = new HypnusService(context);
                        try {
                            ServiceManager.addService("hypnus", hypnusService);
                            hypnusService2 = hypnusService;
                        } catch (Throwable th5) {
                            e222222222 = th5;
                            hypnusService2 = hypnusService;
                            reportWtf("starting Hypnus Service", e222222222);
                            traceEnd();
                            Slog.i(TAG, "Oppo Customize Service");
                            ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                            Slog.i(TAG, "Oppo Service");
                            oppoService2 = new OppoService(context);
                            ServiceManager.addService("OPPO", oppoService2);
                            oppoService = oppoService2;
                            Slog.i(TAG, "OppoAutoInstallService");
                            oppoAutoInstallService = new OppoAutoInstallService(context);
                            ServiceManager.addService("oppoautoinstall", oppoAutoInstallService);
                            oppoAutoInstallService2 = oppoAutoInstallService;
                            Slog.i(TAG, "OPPO Usage Service");
                            oppoUsageService = new OppoUsageService(context);
                            ServiceManager.addService("usage", oppoUsageService);
                            usageService = oppoUsageService;
                            if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                            }
                            if (this.mOnlyCore) {
                            }
                            if (this.mOnlyCore ? context.getResources().getBoolean(17956963) : false) {
                            }
                            traceBeginAndSlog("StartNetworkTimeUpdateService");
                            networkTimeUpdateService = new NetworkTimeUpdateService(context);
                            ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                            networkTimeUpdater = networkTimeUpdateService;
                            traceEnd();
                            traceBeginAndSlog("StartCommonTimeManagementService");
                            commonTimeManagementService = new CommonTimeManagementService(context);
                            ServiceManager.addService("commontime_management", commonTimeManagementService);
                            commonTimeMgmtService = commonTimeManagementService;
                            traceEnd();
                            if (disableNetwork) {
                            }
                            traceBeginAndSlog("StartEmergencyAffordanceService");
                            this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                            traceEnd();
                            if (disableNonCoreServices) {
                            }
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("AddCoverageService");
                            ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                            traceEnd();
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
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartShortcutServiceLifecycle");
                            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                            traceEnd();
                            traceBeginAndSlog("StartLauncherAppsService");
                            this.mSystemServiceManager.startService(LauncherAppsService.class);
                            traceEnd();
                            Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                            this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                            Slog.i(TAG, "Secrecy Service");
                            this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                            traceBeginAndSlog("StartMediaProjectionManager");
                            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                            traceEnd();
                            if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                            }
                            if (disableCameraService) {
                            }
                            traceBeginAndSlog("StartJitCompilation");
                            VMRuntime.getRuntime().startJitCompilation();
                            traceEnd();
                            traceBeginAndSlog("StartMmsService");
                            mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                            traceEnd();
                            if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                            }
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
                            if (enableWigig) {
                            }
                            Slog.i(TAG, "wms systemReady");
                            wm.systemReady();
                            traceEnd();
                            config = wm.computeNewConfiguration(0);
                            metrics = new DisplayMetrics();
                            ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                            context.getResources().updateConfiguration(config, metrics);
                            systemTheme = context.getTheme();
                            if (systemTheme.getChangingConfigurations() != 0) {
                            }
                            traceBeginAndSlog("MakePowerManagerServiceReady");
                            Slog.i(TAG, "power manager systemReady");
                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                            traceEnd();
                            if (this.mSecrecyService != null) {
                            }
                            if (this.mFingerprintService != null) {
                            }
                            if (this.mFaceService != null) {
                            }
                            if (this.mBiometricsService != null) {
                            }
                            traceBeginAndSlog("MakePackageManagerServiceReady");
                            Slog.i(TAG, "Package systemReady");
                            this.mPackageManagerService.systemReady();
                            traceEnd();
                            traceBeginAndSlog("MakeDisplayManagerServiceReady");
                            Slog.i(TAG, "DisplayManager systemReady");
                            this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                            traceEnd();
                            this.mSystemServiceManager.setSafeMode(false);
                            networkManagementF = networkManagement;
                            networkStatsF = networkStats;
                            networkPolicyF = networkPolicy;
                            connectivityF = connectivity;
                            networkScoreF = networkScore;
                            locationF = location;
                            countryDetectorF = countryDetector;
                            networkTimeUpdaterF = networkTimeUpdater;
                            commonTimeMgmtServiceF = commonTimeMgmtService;
                            inputManagerF = inputManager;
                            telephonyRegistryF = telephonyRegistry;
                            mediaRouterF = mediaRouter;
                            mmsServiceF = mmsService;
                            windowManagerF = wm;
                            multimediaServiceF = multimediaService;
                            oppoRoundCornerServiceF = oppoRoundCornerService;
                            usageServiceF = usageService;
                            mOppoService = oppoService;
                            Slog.i(TAG, "Ams systemReady");
                            this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
                        }
                        traceEnd();
                        Slog.i(TAG, "Oppo Customize Service");
                        ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                        Slog.i(TAG, "Oppo Service");
                        oppoService2 = new OppoService(context);
                        ServiceManager.addService("OPPO", oppoService2);
                        oppoService = oppoService2;
                        Slog.i(TAG, "OppoAutoInstallService");
                        oppoAutoInstallService = new OppoAutoInstallService(context);
                        try {
                            ServiceManager.addService("oppoautoinstall", oppoAutoInstallService);
                            oppoAutoInstallService2 = oppoAutoInstallService;
                        } catch (Throwable th6) {
                            e222222222 = th6;
                            oppoAutoInstallService2 = oppoAutoInstallService;
                            Slog.e(TAG, "Start OppoAutoInstallService failed for:", e222222222);
                            Slog.i(TAG, "OPPO Usage Service");
                            oppoUsageService = new OppoUsageService(context);
                            ServiceManager.addService("usage", oppoUsageService);
                            usageService = oppoUsageService;
                            if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                            }
                            if (this.mOnlyCore) {
                            }
                            if (this.mOnlyCore ? context.getResources().getBoolean(17956963) : false) {
                            }
                            traceBeginAndSlog("StartNetworkTimeUpdateService");
                            networkTimeUpdateService = new NetworkTimeUpdateService(context);
                            ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                            networkTimeUpdater = networkTimeUpdateService;
                            traceEnd();
                            traceBeginAndSlog("StartCommonTimeManagementService");
                            commonTimeManagementService = new CommonTimeManagementService(context);
                            ServiceManager.addService("commontime_management", commonTimeManagementService);
                            commonTimeMgmtService = commonTimeManagementService;
                            traceEnd();
                            if (disableNetwork) {
                            }
                            traceBeginAndSlog("StartEmergencyAffordanceService");
                            this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                            traceEnd();
                            if (disableNonCoreServices) {
                            }
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("AddCoverageService");
                            ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                            traceEnd();
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
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartShortcutServiceLifecycle");
                            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                            traceEnd();
                            traceBeginAndSlog("StartLauncherAppsService");
                            this.mSystemServiceManager.startService(LauncherAppsService.class);
                            traceEnd();
                            Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                            this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                            Slog.i(TAG, "Secrecy Service");
                            this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                            traceBeginAndSlog("StartMediaProjectionManager");
                            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                            traceEnd();
                            if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                            }
                            if (disableCameraService) {
                            }
                            traceBeginAndSlog("StartJitCompilation");
                            VMRuntime.getRuntime().startJitCompilation();
                            traceEnd();
                            traceBeginAndSlog("StartMmsService");
                            mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                            traceEnd();
                            if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                            }
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
                            if (enableWigig) {
                            }
                            Slog.i(TAG, "wms systemReady");
                            wm.systemReady();
                            traceEnd();
                            config = wm.computeNewConfiguration(0);
                            metrics = new DisplayMetrics();
                            ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                            context.getResources().updateConfiguration(config, metrics);
                            systemTheme = context.getTheme();
                            if (systemTheme.getChangingConfigurations() != 0) {
                            }
                            traceBeginAndSlog("MakePowerManagerServiceReady");
                            Slog.i(TAG, "power manager systemReady");
                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                            traceEnd();
                            if (this.mSecrecyService != null) {
                            }
                            if (this.mFingerprintService != null) {
                            }
                            if (this.mFaceService != null) {
                            }
                            if (this.mBiometricsService != null) {
                            }
                            traceBeginAndSlog("MakePackageManagerServiceReady");
                            Slog.i(TAG, "Package systemReady");
                            this.mPackageManagerService.systemReady();
                            traceEnd();
                            traceBeginAndSlog("MakeDisplayManagerServiceReady");
                            Slog.i(TAG, "DisplayManager systemReady");
                            this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                            traceEnd();
                            this.mSystemServiceManager.setSafeMode(false);
                            networkManagementF = networkManagement;
                            networkStatsF = networkStats;
                            networkPolicyF = networkPolicy;
                            connectivityF = connectivity;
                            networkScoreF = networkScore;
                            locationF = location;
                            countryDetectorF = countryDetector;
                            networkTimeUpdaterF = networkTimeUpdater;
                            commonTimeMgmtServiceF = commonTimeMgmtService;
                            inputManagerF = inputManager;
                            telephonyRegistryF = telephonyRegistry;
                            mediaRouterF = mediaRouter;
                            mmsServiceF = mmsService;
                            windowManagerF = wm;
                            multimediaServiceF = multimediaService;
                            oppoRoundCornerServiceF = oppoRoundCornerService;
                            usageServiceF = usageService;
                            mOppoService = oppoService;
                            Slog.i(TAG, "Ams systemReady");
                            this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
                        }
                        Slog.i(TAG, "OPPO Usage Service");
                        oppoUsageService = new OppoUsageService(context);
                        try {
                            ServiceManager.addService("usage", oppoUsageService);
                            usageService = oppoUsageService;
                        } catch (Throwable th7) {
                            e222222222 = th7;
                            usageService = oppoUsageService;
                            Slog.e(TAG, "Start OppoUsageService failed for:", e222222222);
                            if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                            }
                            if (this.mOnlyCore) {
                            }
                            if (this.mOnlyCore ? context.getResources().getBoolean(17956963) : false) {
                            }
                            traceBeginAndSlog("StartNetworkTimeUpdateService");
                            networkTimeUpdateService = new NetworkTimeUpdateService(context);
                            ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                            networkTimeUpdater = networkTimeUpdateService;
                            traceEnd();
                            traceBeginAndSlog("StartCommonTimeManagementService");
                            commonTimeManagementService = new CommonTimeManagementService(context);
                            ServiceManager.addService("commontime_management", commonTimeManagementService);
                            commonTimeMgmtService = commonTimeManagementService;
                            traceEnd();
                            if (disableNetwork) {
                            }
                            traceBeginAndSlog("StartEmergencyAffordanceService");
                            this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                            traceEnd();
                            if (disableNonCoreServices) {
                            }
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("AddCoverageService");
                            ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                            traceEnd();
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
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartShortcutServiceLifecycle");
                            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                            traceEnd();
                            traceBeginAndSlog("StartLauncherAppsService");
                            this.mSystemServiceManager.startService(LauncherAppsService.class);
                            traceEnd();
                            Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                            this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                            Slog.i(TAG, "Secrecy Service");
                            this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                            traceBeginAndSlog("StartMediaProjectionManager");
                            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                            traceEnd();
                            if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                            }
                            if (disableCameraService) {
                            }
                            traceBeginAndSlog("StartJitCompilation");
                            VMRuntime.getRuntime().startJitCompilation();
                            traceEnd();
                            traceBeginAndSlog("StartMmsService");
                            mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                            traceEnd();
                            if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                            }
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
                            if (enableWigig) {
                            }
                            Slog.i(TAG, "wms systemReady");
                            wm.systemReady();
                            traceEnd();
                            config = wm.computeNewConfiguration(0);
                            metrics = new DisplayMetrics();
                            ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                            context.getResources().updateConfiguration(config, metrics);
                            systemTheme = context.getTheme();
                            if (systemTheme.getChangingConfigurations() != 0) {
                            }
                            traceBeginAndSlog("MakePowerManagerServiceReady");
                            Slog.i(TAG, "power manager systemReady");
                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                            traceEnd();
                            if (this.mSecrecyService != null) {
                            }
                            if (this.mFingerprintService != null) {
                            }
                            if (this.mFaceService != null) {
                            }
                            if (this.mBiometricsService != null) {
                            }
                            traceBeginAndSlog("MakePackageManagerServiceReady");
                            Slog.i(TAG, "Package systemReady");
                            this.mPackageManagerService.systemReady();
                            traceEnd();
                            traceBeginAndSlog("MakeDisplayManagerServiceReady");
                            Slog.i(TAG, "DisplayManager systemReady");
                            this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                            traceEnd();
                            this.mSystemServiceManager.setSafeMode(false);
                            networkManagementF = networkManagement;
                            networkStatsF = networkStats;
                            networkPolicyF = networkPolicy;
                            connectivityF = connectivity;
                            networkScoreF = networkScore;
                            locationF = location;
                            countryDetectorF = countryDetector;
                            networkTimeUpdaterF = networkTimeUpdater;
                            commonTimeMgmtServiceF = commonTimeMgmtService;
                            inputManagerF = inputManager;
                            telephonyRegistryF = telephonyRegistry;
                            mediaRouterF = mediaRouter;
                            mmsServiceF = mmsService;
                            windowManagerF = wm;
                            multimediaServiceF = multimediaService;
                            oppoRoundCornerServiceF = oppoRoundCornerService;
                            usageServiceF = usageService;
                            mOppoService = oppoService;
                            Slog.i(TAG, "Ams systemReady");
                            this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
                        }
                        if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                        }
                        if (this.mOnlyCore) {
                        }
                        if (this.mOnlyCore ? context.getResources().getBoolean(17956963) : false) {
                        }
                        traceBeginAndSlog("StartNetworkTimeUpdateService");
                        try {
                            networkTimeUpdateService = new NetworkTimeUpdateService(context);
                            ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                            networkTimeUpdater = networkTimeUpdateService;
                        } catch (Throwable th8) {
                            e222222222 = th8;
                            reportWtf("starting NetworkTimeUpdate service", e222222222);
                            traceEnd();
                            traceBeginAndSlog("StartCommonTimeManagementService");
                            commonTimeManagementService = new CommonTimeManagementService(context);
                            ServiceManager.addService("commontime_management", commonTimeManagementService);
                            commonTimeMgmtService = commonTimeManagementService;
                            traceEnd();
                            if (disableNetwork) {
                            }
                            traceBeginAndSlog("StartEmergencyAffordanceService");
                            this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                            traceEnd();
                            if (disableNonCoreServices) {
                            }
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("AddCoverageService");
                            ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                            traceEnd();
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
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartShortcutServiceLifecycle");
                            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                            traceEnd();
                            traceBeginAndSlog("StartLauncherAppsService");
                            this.mSystemServiceManager.startService(LauncherAppsService.class);
                            traceEnd();
                            Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                            this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                            Slog.i(TAG, "Secrecy Service");
                            this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                            traceBeginAndSlog("StartMediaProjectionManager");
                            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                            traceEnd();
                            if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                            }
                            if (disableCameraService) {
                            }
                            traceBeginAndSlog("StartJitCompilation");
                            VMRuntime.getRuntime().startJitCompilation();
                            traceEnd();
                            traceBeginAndSlog("StartMmsService");
                            mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                            traceEnd();
                            if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                            }
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
                            if (enableWigig) {
                            }
                            Slog.i(TAG, "wms systemReady");
                            wm.systemReady();
                            traceEnd();
                            config = wm.computeNewConfiguration(0);
                            metrics = new DisplayMetrics();
                            ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                            context.getResources().updateConfiguration(config, metrics);
                            systemTheme = context.getTheme();
                            if (systemTheme.getChangingConfigurations() != 0) {
                            }
                            traceBeginAndSlog("MakePowerManagerServiceReady");
                            Slog.i(TAG, "power manager systemReady");
                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                            traceEnd();
                            if (this.mSecrecyService != null) {
                            }
                            if (this.mFingerprintService != null) {
                            }
                            if (this.mFaceService != null) {
                            }
                            if (this.mBiometricsService != null) {
                            }
                            traceBeginAndSlog("MakePackageManagerServiceReady");
                            Slog.i(TAG, "Package systemReady");
                            this.mPackageManagerService.systemReady();
                            traceEnd();
                            traceBeginAndSlog("MakeDisplayManagerServiceReady");
                            Slog.i(TAG, "DisplayManager systemReady");
                            this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                            traceEnd();
                            this.mSystemServiceManager.setSafeMode(false);
                            networkManagementF = networkManagement;
                            networkStatsF = networkStats;
                            networkPolicyF = networkPolicy;
                            connectivityF = connectivity;
                            networkScoreF = networkScore;
                            locationF = location;
                            countryDetectorF = countryDetector;
                            networkTimeUpdaterF = networkTimeUpdater;
                            commonTimeMgmtServiceF = commonTimeMgmtService;
                            inputManagerF = inputManager;
                            telephonyRegistryF = telephonyRegistry;
                            mediaRouterF = mediaRouter;
                            mmsServiceF = mmsService;
                            windowManagerF = wm;
                            multimediaServiceF = multimediaService;
                            oppoRoundCornerServiceF = oppoRoundCornerService;
                            usageServiceF = usageService;
                            mOppoService = oppoService;
                            Slog.i(TAG, "Ams systemReady");
                            this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
                        }
                        traceEnd();
                        traceBeginAndSlog("StartCommonTimeManagementService");
                        commonTimeManagementService = new CommonTimeManagementService(context);
                        try {
                            ServiceManager.addService("commontime_management", commonTimeManagementService);
                            commonTimeMgmtService = commonTimeManagementService;
                        } catch (Throwable th9) {
                            e222222222 = th9;
                            commonTimeMgmtService = commonTimeManagementService;
                            reportWtf("starting CommonTimeManagementService service", e222222222);
                            traceEnd();
                            if (disableNetwork) {
                            }
                            traceBeginAndSlog("StartEmergencyAffordanceService");
                            this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                            traceEnd();
                            if (disableNonCoreServices) {
                            }
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("AddCoverageService");
                            ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                            traceEnd();
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
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartShortcutServiceLifecycle");
                            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                            traceEnd();
                            traceBeginAndSlog("StartLauncherAppsService");
                            this.mSystemServiceManager.startService(LauncherAppsService.class);
                            traceEnd();
                            Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                            this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                            Slog.i(TAG, "Secrecy Service");
                            this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                            traceBeginAndSlog("StartMediaProjectionManager");
                            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                            traceEnd();
                            if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                            }
                            if (disableCameraService) {
                            }
                            traceBeginAndSlog("StartJitCompilation");
                            VMRuntime.getRuntime().startJitCompilation();
                            traceEnd();
                            traceBeginAndSlog("StartMmsService");
                            mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                            traceEnd();
                            if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                            }
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
                            if (enableWigig) {
                            }
                            Slog.i(TAG, "wms systemReady");
                            wm.systemReady();
                            traceEnd();
                            config = wm.computeNewConfiguration(0);
                            metrics = new DisplayMetrics();
                            ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                            context.getResources().updateConfiguration(config, metrics);
                            systemTheme = context.getTheme();
                            if (systemTheme.getChangingConfigurations() != 0) {
                            }
                            traceBeginAndSlog("MakePowerManagerServiceReady");
                            Slog.i(TAG, "power manager systemReady");
                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                            traceEnd();
                            if (this.mSecrecyService != null) {
                            }
                            if (this.mFingerprintService != null) {
                            }
                            if (this.mFaceService != null) {
                            }
                            if (this.mBiometricsService != null) {
                            }
                            traceBeginAndSlog("MakePackageManagerServiceReady");
                            Slog.i(TAG, "Package systemReady");
                            this.mPackageManagerService.systemReady();
                            traceEnd();
                            traceBeginAndSlog("MakeDisplayManagerServiceReady");
                            Slog.i(TAG, "DisplayManager systemReady");
                            this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                            traceEnd();
                            this.mSystemServiceManager.setSafeMode(false);
                            networkManagementF = networkManagement;
                            networkStatsF = networkStats;
                            networkPolicyF = networkPolicy;
                            connectivityF = connectivity;
                            networkScoreF = networkScore;
                            locationF = location;
                            countryDetectorF = countryDetector;
                            networkTimeUpdaterF = networkTimeUpdater;
                            commonTimeMgmtServiceF = commonTimeMgmtService;
                            inputManagerF = inputManager;
                            telephonyRegistryF = telephonyRegistry;
                            mediaRouterF = mediaRouter;
                            mmsServiceF = mmsService;
                            windowManagerF = wm;
                            multimediaServiceF = multimediaService;
                            oppoRoundCornerServiceF = oppoRoundCornerService;
                            usageServiceF = usageService;
                            mOppoService = oppoService;
                            Slog.i(TAG, "Ams systemReady");
                            this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
                        }
                        traceEnd();
                        if (disableNetwork) {
                        }
                        traceBeginAndSlog("StartEmergencyAffordanceService");
                        this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                        traceEnd();
                        if (disableNonCoreServices) {
                        }
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("AddCoverageService");
                        ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                        traceEnd();
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
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartShortcutServiceLifecycle");
                        this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                        traceEnd();
                        traceBeginAndSlog("StartLauncherAppsService");
                        this.mSystemServiceManager.startService(LauncherAppsService.class);
                        traceEnd();
                        Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                        this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                        Slog.i(TAG, "Secrecy Service");
                        this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                        traceBeginAndSlog("StartMediaProjectionManager");
                        this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                        traceEnd();
                        if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                        }
                        if (disableCameraService) {
                        }
                        traceBeginAndSlog("StartJitCompilation");
                        VMRuntime.getRuntime().startJitCompilation();
                        traceEnd();
                        traceBeginAndSlog("StartMmsService");
                        mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                        }
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
                        if (enableWigig) {
                        }
                        Slog.i(TAG, "wms systemReady");
                        wm.systemReady();
                        traceEnd();
                        config = wm.computeNewConfiguration(0);
                        metrics = new DisplayMetrics();
                        ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                        context.getResources().updateConfiguration(config, metrics);
                        systemTheme = context.getTheme();
                        if (systemTheme.getChangingConfigurations() != 0) {
                        }
                        traceBeginAndSlog("MakePowerManagerServiceReady");
                        Slog.i(TAG, "power manager systemReady");
                        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                        traceEnd();
                        if (this.mSecrecyService != null) {
                        }
                        if (this.mFingerprintService != null) {
                        }
                        if (this.mFaceService != null) {
                        }
                        if (this.mBiometricsService != null) {
                        }
                        traceBeginAndSlog("MakePackageManagerServiceReady");
                        Slog.i(TAG, "Package systemReady");
                        this.mPackageManagerService.systemReady();
                        traceEnd();
                        traceBeginAndSlog("MakeDisplayManagerServiceReady");
                        Slog.i(TAG, "DisplayManager systemReady");
                        this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                        traceEnd();
                        this.mSystemServiceManager.setSafeMode(false);
                        networkManagementF = networkManagement;
                        networkStatsF = networkStats;
                        networkPolicyF = networkPolicy;
                        connectivityF = connectivity;
                        networkScoreF = networkScore;
                        locationF = location;
                        countryDetectorF = countryDetector;
                        networkTimeUpdaterF = networkTimeUpdater;
                        commonTimeMgmtServiceF = commonTimeMgmtService;
                        inputManagerF = inputManager;
                        telephonyRegistryF = telephonyRegistry;
                        mediaRouterF = mediaRouter;
                        mmsServiceF = mmsService;
                        windowManagerF = wm;
                        multimediaServiceF = multimediaService;
                        oppoRoundCornerServiceF = oppoRoundCornerService;
                        usageServiceF = usageService;
                        mOppoService = oppoService;
                        Slog.i(TAG, "Ams systemReady");
                        this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
                    }
                } catch (Throwable th10) {
                    e222222222 = th10;
                    reportWtf("starting StatusBarManagerService", e222222222);
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    if (disableNetwork) {
                    }
                    traceBeginAndSlog("StartTextServicesManager");
                    this.mSystemServiceManager.startService(TextServicesManagerService.Lifecycle.class);
                    traceEnd();
                    if (disableNetwork) {
                    }
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("WaitForAsecScan");
                    storageManager.waitForAsecScan();
                    traceEnd();
                    Slog.i(TAG, "Oppo Expand Service");
                    ServiceManager.addService("OPPOExService", new OppoExService(context, wm));
                    if (this.mOppoLightsService != null) {
                    }
                    traceBeginAndSlog("StartNotificationManager");
                    Slog.i(TAG, "NotificationManagerService");
                    this.mSystemServiceManager.startService(NotificationManagerService.class);
                    SystemNotificationChannels.createAll(context);
                    networkPolicy.bindNotificationManager(INotificationManager.Stub.asInterface(ServiceManager.getService("notification")));
                    traceEnd();
                    traceBeginAndSlog("StartDeviceMonitor");
                    this.mSystemServiceManager.startService(OppoDeviceStorageMonitorService.class);
                    Slog.i(TAG, "add OppoDeviceStorageMonitorService");
                    traceEnd();
                    if (disableLocation) {
                    }
                    traceBeginAndSlog("StartSearchManagerService");
                    this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                    traceEnd();
                    AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, "systemserver pid:" + Process.myPid());
                    AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, "systemserver pid:" + Process.myPid());
                    traceBeginAndSlog("StartWallpaperManagerService");
                    this.mSystemServiceManager.startService(WALLPAPER_SERVICE_CLASS);
                    traceEnd();
                    traceBeginAndSlog("StartAudioService");
                    this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.hardware.broadcastradio")) {
                    }
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartWiredAccessoryManager");
                    inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartTwilightService");
                    this.mSystemServiceManager.startService(TwilightService.class);
                    traceEnd();
                    if (NightDisplayController.isAvailable(context)) {
                    }
                    traceBeginAndSlog("StartJobScheduler");
                    this.mSystemServiceManager.startService(JobSchedulerService.class);
                    traceEnd();
                    traceBeginAndSlog("StartSoundTrigger");
                    this.mSystemServiceManager.startService(SoundTriggerService.class);
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartDiskStatsService");
                    ServiceManager.addService("diskstats", new DiskStatsService(context));
                    traceEnd();
                    Slog.i(TAG, "+OppoMultimediaService");
                    oppoMultimediaService = new OppoMultimediaService(context);
                    ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                    Slog.i(TAG, "-OppoMultimediaService");
                    multimediaService = oppoMultimediaService;
                    traceBeginAndSlog("CabcService");
                    Slog.i(TAG, "Cabc Service");
                    cabcService = new CabcService(context);
                    ServiceManager.addService("cabc", cabcService);
                    cabcService2 = cabcService;
                    Trace.traceEnd(524288);
                    traceBeginAndSlog("OppoRoundCornerService");
                    oppoRoundCornerService = null;
                    if (SystemProperties.getInt("persist.sys.enable.rc", 0) == 1) {
                    }
                    traceBeginAndSlog("StartHypnusService");
                    Slog.i(TAG, "Hypnus Service");
                    hypnusService = new HypnusService(context);
                    ServiceManager.addService("hypnus", hypnusService);
                    hypnusService2 = hypnusService;
                    traceEnd();
                    Slog.i(TAG, "Oppo Customize Service");
                    ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                    Slog.i(TAG, "Oppo Service");
                    oppoService2 = new OppoService(context);
                    ServiceManager.addService("OPPO", oppoService2);
                    oppoService = oppoService2;
                    Slog.i(TAG, "OppoAutoInstallService");
                    oppoAutoInstallService = new OppoAutoInstallService(context);
                    ServiceManager.addService("oppoautoinstall", oppoAutoInstallService);
                    oppoAutoInstallService2 = oppoAutoInstallService;
                    Slog.i(TAG, "OPPO Usage Service");
                    oppoUsageService = new OppoUsageService(context);
                    ServiceManager.addService("usage", oppoUsageService);
                    usageService = oppoUsageService;
                    if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                    }
                    if (this.mOnlyCore) {
                    }
                    if (this.mOnlyCore ? context.getResources().getBoolean(17956963) : false) {
                    }
                    traceBeginAndSlog("StartNetworkTimeUpdateService");
                    networkTimeUpdateService = new NetworkTimeUpdateService(context);
                    ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                    networkTimeUpdater = networkTimeUpdateService;
                    traceEnd();
                    traceBeginAndSlog("StartCommonTimeManagementService");
                    commonTimeManagementService = new CommonTimeManagementService(context);
                    ServiceManager.addService("commontime_management", commonTimeManagementService);
                    commonTimeMgmtService = commonTimeManagementService;
                    traceEnd();
                    if (disableNetwork) {
                    }
                    traceBeginAndSlog("StartEmergencyAffordanceService");
                    this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("AddCoverageService");
                    ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                    traceEnd();
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
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartShortcutServiceLifecycle");
                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                    traceEnd();
                    traceBeginAndSlog("StartLauncherAppsService");
                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                    traceEnd();
                    Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                    this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                    Slog.i(TAG, "Secrecy Service");
                    this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                    traceBeginAndSlog("StartMediaProjectionManager");
                    this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                    traceEnd();
                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                    }
                    if (disableCameraService) {
                    }
                    traceBeginAndSlog("StartJitCompilation");
                    VMRuntime.getRuntime().startJitCompilation();
                    traceEnd();
                    traceBeginAndSlog("StartMmsService");
                    mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                    }
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
                    if (enableWigig) {
                    }
                    Slog.i(TAG, "wms systemReady");
                    wm.systemReady();
                    traceEnd();
                    config = wm.computeNewConfiguration(0);
                    metrics = new DisplayMetrics();
                    ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                    context.getResources().updateConfiguration(config, metrics);
                    systemTheme = context.getTheme();
                    if (systemTheme.getChangingConfigurations() != 0) {
                    }
                    traceBeginAndSlog("MakePowerManagerServiceReady");
                    Slog.i(TAG, "power manager systemReady");
                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                    traceEnd();
                    if (this.mSecrecyService != null) {
                    }
                    if (this.mFingerprintService != null) {
                    }
                    if (this.mFaceService != null) {
                    }
                    if (this.mBiometricsService != null) {
                    }
                    traceBeginAndSlog("MakePackageManagerServiceReady");
                    Slog.i(TAG, "Package systemReady");
                    this.mPackageManagerService.systemReady();
                    traceEnd();
                    traceBeginAndSlog("MakeDisplayManagerServiceReady");
                    Slog.i(TAG, "DisplayManager systemReady");
                    this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                    traceEnd();
                    this.mSystemServiceManager.setSafeMode(false);
                    networkManagementF = networkManagement;
                    networkStatsF = networkStats;
                    networkPolicyF = networkPolicy;
                    connectivityF = connectivity;
                    networkScoreF = networkScore;
                    locationF = location;
                    countryDetectorF = countryDetector;
                    networkTimeUpdaterF = networkTimeUpdater;
                    commonTimeMgmtServiceF = commonTimeMgmtService;
                    inputManagerF = inputManager;
                    telephonyRegistryF = telephonyRegistry;
                    mediaRouterF = mediaRouter;
                    mmsServiceF = mmsService;
                    windowManagerF = wm;
                    multimediaServiceF = multimediaService;
                    oppoRoundCornerServiceF = oppoRoundCornerService;
                    usageServiceF = usageService;
                    mOppoService = oppoService;
                    Slog.i(TAG, "Ams systemReady");
                    this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
                }
                traceEnd();
            }
            if (disableNonCoreServices) {
                traceBeginAndSlog("StartClipboardService");
                this.mSystemServiceManager.startService(ClipboardService.class);
                traceEnd();
            }
            if (disableNetwork) {
                traceBeginAndSlog("StartNetworkManagementService");
                try {
                    networkManagement = NetworkManagementService.create(context);
                    ServiceManager.addService("network_management", networkManagement);
                } catch (Throwable e2222222222) {
                    reportWtf("starting NetworkManagement Service", e2222222222);
                }
                traceEnd();
            }
            if (!(disableNonCoreServices || (disableTextServices ^ 1) == 0)) {
                traceBeginAndSlog("StartTextServicesManager");
                this.mSystemServiceManager.startService(TextServicesManagerService.Lifecycle.class);
                traceEnd();
            }
            if (disableNetwork) {
                traceBeginAndSlog("StartNetworkScoreService");
                try {
                    NetworkScoreService networkScoreService = new NetworkScoreService(context);
                    try {
                        ServiceManager.addService("network_score", networkScoreService);
                        networkScore = networkScoreService;
                    } catch (Throwable th11) {
                        e2222222222 = th11;
                        networkScore = networkScoreService;
                        reportWtf("starting Network Score Service", e2222222222);
                        traceEnd();
                        traceBeginAndSlog("StartNetworkStatsService");
                        networkStats = NetworkStatsService.create(context, networkManagement);
                        ServiceManager.addService("netstats", networkStats);
                        traceEnd();
                        traceBeginAndSlog("StartNetworkPolicyManagerService");
                        networkPolicyManagerService = new NetworkPolicyManagerService(context, this.mActivityManagerService, networkStats, networkManagement);
                        try {
                            ServiceManager.addService("netpolicy", networkPolicyManagerService);
                            networkPolicy = networkPolicyManagerService;
                        } catch (Throwable th12) {
                            e2222222222 = th12;
                            networkPolicy = networkPolicyManagerService;
                            reportWtf("starting NetworkPolicy Service", e2222222222);
                            traceEnd();
                            traceBeginAndSlog("StartWifi");
                            Slog.i(TAG, "wifi service");
                            this.mSystemServiceManager.startService(WIFI_SERVICE_CLASS);
                            traceEnd();
                            traceBeginAndSlog("StartWifiScanning");
                            this.mSystemServiceManager.startService("com.android.server.wifi.scanner.WifiScanningService");
                            traceEnd();
                            if (!disableRtt) {
                            }
                            if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.aware")) {
                            }
                            if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.direct")) {
                            }
                            if (context.getPackageManager().hasSystemFeature("android.hardware.lowpan")) {
                            }
                            if (enableWigig) {
                            }
                            traceBeginAndSlog("StartEthernet");
                            this.mSystemServiceManager.startService(ETHERNET_SERVICE_CLASS);
                            traceEnd();
                            traceBeginAndSlog("StartConnectivityService");
                            connectivityService = new ConnectivityService(context, networkManagement, networkStats, networkPolicy);
                            ServiceManager.addService("connectivity", connectivityService);
                            networkStats.bindConnectivityManager(connectivityService);
                            networkPolicy.bindConnectivityManager(connectivityService);
                            connectivity = connectivityService;
                            traceEnd();
                            traceBeginAndSlog("StartNsdService");
                            ServiceManager.addService("servicediscovery", NsdService.create(context));
                            traceEnd();
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("WaitForAsecScan");
                            storageManager.waitForAsecScan();
                            traceEnd();
                            Slog.i(TAG, "Oppo Expand Service");
                            ServiceManager.addService("OPPOExService", new OppoExService(context, wm));
                            if (this.mOppoLightsService != null) {
                            }
                            traceBeginAndSlog("StartNotificationManager");
                            Slog.i(TAG, "NotificationManagerService");
                            this.mSystemServiceManager.startService(NotificationManagerService.class);
                            SystemNotificationChannels.createAll(context);
                            networkPolicy.bindNotificationManager(INotificationManager.Stub.asInterface(ServiceManager.getService("notification")));
                            traceEnd();
                            traceBeginAndSlog("StartDeviceMonitor");
                            this.mSystemServiceManager.startService(OppoDeviceStorageMonitorService.class);
                            Slog.i(TAG, "add OppoDeviceStorageMonitorService");
                            traceEnd();
                            if (disableLocation) {
                            }
                            traceBeginAndSlog("StartSearchManagerService");
                            this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                            traceEnd();
                            AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, "systemserver pid:" + Process.myPid());
                            AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, "systemserver pid:" + Process.myPid());
                            traceBeginAndSlog("StartWallpaperManagerService");
                            this.mSystemServiceManager.startService(WALLPAPER_SERVICE_CLASS);
                            traceEnd();
                            traceBeginAndSlog("StartAudioService");
                            this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                            traceEnd();
                            if (this.mPackageManager.hasSystemFeature("android.hardware.broadcastradio")) {
                            }
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartWiredAccessoryManager");
                            inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                            traceEnd();
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartTwilightService");
                            this.mSystemServiceManager.startService(TwilightService.class);
                            traceEnd();
                            if (NightDisplayController.isAvailable(context)) {
                            }
                            traceBeginAndSlog("StartJobScheduler");
                            this.mSystemServiceManager.startService(JobSchedulerService.class);
                            traceEnd();
                            traceBeginAndSlog("StartSoundTrigger");
                            this.mSystemServiceManager.startService(SoundTriggerService.class);
                            traceEnd();
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartDiskStatsService");
                            ServiceManager.addService("diskstats", new DiskStatsService(context));
                            traceEnd();
                            Slog.i(TAG, "+OppoMultimediaService");
                            oppoMultimediaService = new OppoMultimediaService(context);
                            ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                            Slog.i(TAG, "-OppoMultimediaService");
                            multimediaService = oppoMultimediaService;
                            traceBeginAndSlog("CabcService");
                            Slog.i(TAG, "Cabc Service");
                            cabcService = new CabcService(context);
                            ServiceManager.addService("cabc", cabcService);
                            cabcService2 = cabcService;
                            Trace.traceEnd(524288);
                            traceBeginAndSlog("OppoRoundCornerService");
                            oppoRoundCornerService = null;
                            if (SystemProperties.getInt("persist.sys.enable.rc", 0) == 1) {
                            }
                            traceBeginAndSlog("StartHypnusService");
                            Slog.i(TAG, "Hypnus Service");
                            hypnusService = new HypnusService(context);
                            ServiceManager.addService("hypnus", hypnusService);
                            hypnusService2 = hypnusService;
                            traceEnd();
                            Slog.i(TAG, "Oppo Customize Service");
                            ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                            Slog.i(TAG, "Oppo Service");
                            oppoService2 = new OppoService(context);
                            ServiceManager.addService("OPPO", oppoService2);
                            oppoService = oppoService2;
                            Slog.i(TAG, "OppoAutoInstallService");
                            oppoAutoInstallService = new OppoAutoInstallService(context);
                            ServiceManager.addService("oppoautoinstall", oppoAutoInstallService);
                            oppoAutoInstallService2 = oppoAutoInstallService;
                            Slog.i(TAG, "OPPO Usage Service");
                            oppoUsageService = new OppoUsageService(context);
                            ServiceManager.addService("usage", oppoUsageService);
                            usageService = oppoUsageService;
                            if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                            }
                            if (this.mOnlyCore) {
                            }
                            if (this.mOnlyCore ? context.getResources().getBoolean(17956963) : false) {
                            }
                            traceBeginAndSlog("StartNetworkTimeUpdateService");
                            networkTimeUpdateService = new NetworkTimeUpdateService(context);
                            ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                            networkTimeUpdater = networkTimeUpdateService;
                            traceEnd();
                            traceBeginAndSlog("StartCommonTimeManagementService");
                            commonTimeManagementService = new CommonTimeManagementService(context);
                            ServiceManager.addService("commontime_management", commonTimeManagementService);
                            commonTimeMgmtService = commonTimeManagementService;
                            traceEnd();
                            if (disableNetwork) {
                            }
                            traceBeginAndSlog("StartEmergencyAffordanceService");
                            this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                            traceEnd();
                            if (disableNonCoreServices) {
                            }
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("AddCoverageService");
                            ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                            traceEnd();
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
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartShortcutServiceLifecycle");
                            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                            traceEnd();
                            traceBeginAndSlog("StartLauncherAppsService");
                            this.mSystemServiceManager.startService(LauncherAppsService.class);
                            traceEnd();
                            Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                            this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                            Slog.i(TAG, "Secrecy Service");
                            this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                            traceBeginAndSlog("StartMediaProjectionManager");
                            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                            traceEnd();
                            if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                            }
                            if (disableCameraService) {
                            }
                            traceBeginAndSlog("StartJitCompilation");
                            VMRuntime.getRuntime().startJitCompilation();
                            traceEnd();
                            traceBeginAndSlog("StartMmsService");
                            mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                            traceEnd();
                            if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                            }
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
                            if (enableWigig) {
                            }
                            Slog.i(TAG, "wms systemReady");
                            wm.systemReady();
                            traceEnd();
                            config = wm.computeNewConfiguration(0);
                            metrics = new DisplayMetrics();
                            ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                            context.getResources().updateConfiguration(config, metrics);
                            systemTheme = context.getTheme();
                            if (systemTheme.getChangingConfigurations() != 0) {
                            }
                            traceBeginAndSlog("MakePowerManagerServiceReady");
                            Slog.i(TAG, "power manager systemReady");
                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                            traceEnd();
                            if (this.mSecrecyService != null) {
                            }
                            if (this.mFingerprintService != null) {
                            }
                            if (this.mFaceService != null) {
                            }
                            if (this.mBiometricsService != null) {
                            }
                            traceBeginAndSlog("MakePackageManagerServiceReady");
                            Slog.i(TAG, "Package systemReady");
                            this.mPackageManagerService.systemReady();
                            traceEnd();
                            traceBeginAndSlog("MakeDisplayManagerServiceReady");
                            Slog.i(TAG, "DisplayManager systemReady");
                            this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                            traceEnd();
                            this.mSystemServiceManager.setSafeMode(false);
                            networkManagementF = networkManagement;
                            networkStatsF = networkStats;
                            networkPolicyF = networkPolicy;
                            connectivityF = connectivity;
                            networkScoreF = networkScore;
                            locationF = location;
                            countryDetectorF = countryDetector;
                            networkTimeUpdaterF = networkTimeUpdater;
                            commonTimeMgmtServiceF = commonTimeMgmtService;
                            inputManagerF = inputManager;
                            telephonyRegistryF = telephonyRegistry;
                            mediaRouterF = mediaRouter;
                            mmsServiceF = mmsService;
                            windowManagerF = wm;
                            multimediaServiceF = multimediaService;
                            oppoRoundCornerServiceF = oppoRoundCornerService;
                            usageServiceF = usageService;
                            mOppoService = oppoService;
                            Slog.i(TAG, "Ams systemReady");
                            this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
                        }
                        traceEnd();
                        traceBeginAndSlog("StartWifi");
                        Slog.i(TAG, "wifi service");
                        this.mSystemServiceManager.startService(WIFI_SERVICE_CLASS);
                        traceEnd();
                        traceBeginAndSlog("StartWifiScanning");
                        this.mSystemServiceManager.startService("com.android.server.wifi.scanner.WifiScanningService");
                        traceEnd();
                        if (disableRtt) {
                        }
                        if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.aware")) {
                        }
                        if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.direct")) {
                        }
                        if (context.getPackageManager().hasSystemFeature("android.hardware.lowpan")) {
                        }
                        if (enableWigig) {
                        }
                        traceBeginAndSlog("StartEthernet");
                        this.mSystemServiceManager.startService(ETHERNET_SERVICE_CLASS);
                        traceEnd();
                        traceBeginAndSlog("StartConnectivityService");
                        connectivityService = new ConnectivityService(context, networkManagement, networkStats, networkPolicy);
                        ServiceManager.addService("connectivity", connectivityService);
                        networkStats.bindConnectivityManager(connectivityService);
                        networkPolicy.bindConnectivityManager(connectivityService);
                        connectivity = connectivityService;
                        traceEnd();
                        traceBeginAndSlog("StartNsdService");
                        ServiceManager.addService("servicediscovery", NsdService.create(context));
                        traceEnd();
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("WaitForAsecScan");
                        storageManager.waitForAsecScan();
                        traceEnd();
                        Slog.i(TAG, "Oppo Expand Service");
                        ServiceManager.addService("OPPOExService", new OppoExService(context, wm));
                        if (this.mOppoLightsService != null) {
                        }
                        traceBeginAndSlog("StartNotificationManager");
                        Slog.i(TAG, "NotificationManagerService");
                        this.mSystemServiceManager.startService(NotificationManagerService.class);
                        SystemNotificationChannels.createAll(context);
                        networkPolicy.bindNotificationManager(INotificationManager.Stub.asInterface(ServiceManager.getService("notification")));
                        traceEnd();
                        traceBeginAndSlog("StartDeviceMonitor");
                        this.mSystemServiceManager.startService(OppoDeviceStorageMonitorService.class);
                        Slog.i(TAG, "add OppoDeviceStorageMonitorService");
                        traceEnd();
                        if (disableLocation) {
                        }
                        traceBeginAndSlog("StartSearchManagerService");
                        this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                        traceEnd();
                        AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, "systemserver pid:" + Process.myPid());
                        AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, "systemserver pid:" + Process.myPid());
                        traceBeginAndSlog("StartWallpaperManagerService");
                        this.mSystemServiceManager.startService(WALLPAPER_SERVICE_CLASS);
                        traceEnd();
                        traceBeginAndSlog("StartAudioService");
                        this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.hardware.broadcastradio")) {
                        }
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartWiredAccessoryManager");
                        inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                        traceEnd();
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartTwilightService");
                        this.mSystemServiceManager.startService(TwilightService.class);
                        traceEnd();
                        if (NightDisplayController.isAvailable(context)) {
                        }
                        traceBeginAndSlog("StartJobScheduler");
                        this.mSystemServiceManager.startService(JobSchedulerService.class);
                        traceEnd();
                        traceBeginAndSlog("StartSoundTrigger");
                        this.mSystemServiceManager.startService(SoundTriggerService.class);
                        traceEnd();
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartDiskStatsService");
                        ServiceManager.addService("diskstats", new DiskStatsService(context));
                        traceEnd();
                        Slog.i(TAG, "+OppoMultimediaService");
                        oppoMultimediaService = new OppoMultimediaService(context);
                        ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                        Slog.i(TAG, "-OppoMultimediaService");
                        multimediaService = oppoMultimediaService;
                        traceBeginAndSlog("CabcService");
                        Slog.i(TAG, "Cabc Service");
                        cabcService = new CabcService(context);
                        ServiceManager.addService("cabc", cabcService);
                        cabcService2 = cabcService;
                        Trace.traceEnd(524288);
                        traceBeginAndSlog("OppoRoundCornerService");
                        oppoRoundCornerService = null;
                        if (SystemProperties.getInt("persist.sys.enable.rc", 0) == 1) {
                        }
                        traceBeginAndSlog("StartHypnusService");
                        Slog.i(TAG, "Hypnus Service");
                        hypnusService = new HypnusService(context);
                        ServiceManager.addService("hypnus", hypnusService);
                        hypnusService2 = hypnusService;
                        traceEnd();
                        Slog.i(TAG, "Oppo Customize Service");
                        ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                        Slog.i(TAG, "Oppo Service");
                        oppoService2 = new OppoService(context);
                        ServiceManager.addService("OPPO", oppoService2);
                        oppoService = oppoService2;
                        Slog.i(TAG, "OppoAutoInstallService");
                        oppoAutoInstallService = new OppoAutoInstallService(context);
                        ServiceManager.addService("oppoautoinstall", oppoAutoInstallService);
                        oppoAutoInstallService2 = oppoAutoInstallService;
                        Slog.i(TAG, "OPPO Usage Service");
                        oppoUsageService = new OppoUsageService(context);
                        ServiceManager.addService("usage", oppoUsageService);
                        usageService = oppoUsageService;
                        if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                        }
                        if (this.mOnlyCore) {
                        }
                        if (this.mOnlyCore ? context.getResources().getBoolean(17956963) : false) {
                        }
                        traceBeginAndSlog("StartNetworkTimeUpdateService");
                        networkTimeUpdateService = new NetworkTimeUpdateService(context);
                        ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                        networkTimeUpdater = networkTimeUpdateService;
                        traceEnd();
                        traceBeginAndSlog("StartCommonTimeManagementService");
                        commonTimeManagementService = new CommonTimeManagementService(context);
                        ServiceManager.addService("commontime_management", commonTimeManagementService);
                        commonTimeMgmtService = commonTimeManagementService;
                        traceEnd();
                        if (disableNetwork) {
                        }
                        traceBeginAndSlog("StartEmergencyAffordanceService");
                        this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                        traceEnd();
                        if (disableNonCoreServices) {
                        }
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("AddCoverageService");
                        ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                        traceEnd();
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
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartShortcutServiceLifecycle");
                        this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                        traceEnd();
                        traceBeginAndSlog("StartLauncherAppsService");
                        this.mSystemServiceManager.startService(LauncherAppsService.class);
                        traceEnd();
                        Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                        this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                        Slog.i(TAG, "Secrecy Service");
                        this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                        traceBeginAndSlog("StartMediaProjectionManager");
                        this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                        traceEnd();
                        if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                        }
                        if (disableCameraService) {
                        }
                        traceBeginAndSlog("StartJitCompilation");
                        VMRuntime.getRuntime().startJitCompilation();
                        traceEnd();
                        traceBeginAndSlog("StartMmsService");
                        mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                        }
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
                        if (enableWigig) {
                        }
                        Slog.i(TAG, "wms systemReady");
                        wm.systemReady();
                        traceEnd();
                        config = wm.computeNewConfiguration(0);
                        metrics = new DisplayMetrics();
                        ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                        context.getResources().updateConfiguration(config, metrics);
                        systemTheme = context.getTheme();
                        if (systemTheme.getChangingConfigurations() != 0) {
                        }
                        traceBeginAndSlog("MakePowerManagerServiceReady");
                        Slog.i(TAG, "power manager systemReady");
                        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                        traceEnd();
                        if (this.mSecrecyService != null) {
                        }
                        if (this.mFingerprintService != null) {
                        }
                        if (this.mFaceService != null) {
                        }
                        if (this.mBiometricsService != null) {
                        }
                        traceBeginAndSlog("MakePackageManagerServiceReady");
                        Slog.i(TAG, "Package systemReady");
                        this.mPackageManagerService.systemReady();
                        traceEnd();
                        traceBeginAndSlog("MakeDisplayManagerServiceReady");
                        Slog.i(TAG, "DisplayManager systemReady");
                        this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                        traceEnd();
                        this.mSystemServiceManager.setSafeMode(false);
                        networkManagementF = networkManagement;
                        networkStatsF = networkStats;
                        networkPolicyF = networkPolicy;
                        connectivityF = connectivity;
                        networkScoreF = networkScore;
                        locationF = location;
                        countryDetectorF = countryDetector;
                        networkTimeUpdaterF = networkTimeUpdater;
                        commonTimeMgmtServiceF = commonTimeMgmtService;
                        inputManagerF = inputManager;
                        telephonyRegistryF = telephonyRegistry;
                        mediaRouterF = mediaRouter;
                        mmsServiceF = mmsService;
                        windowManagerF = wm;
                        multimediaServiceF = multimediaService;
                        oppoRoundCornerServiceF = oppoRoundCornerService;
                        usageServiceF = usageService;
                        mOppoService = oppoService;
                        Slog.i(TAG, "Ams systemReady");
                        this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
                    }
                } catch (Throwable th13) {
                    e2222222222 = th13;
                    reportWtf("starting Network Score Service", e2222222222);
                    traceEnd();
                    traceBeginAndSlog("StartNetworkStatsService");
                    networkStats = NetworkStatsService.create(context, networkManagement);
                    ServiceManager.addService("netstats", networkStats);
                    traceEnd();
                    traceBeginAndSlog("StartNetworkPolicyManagerService");
                    networkPolicyManagerService = new NetworkPolicyManagerService(context, this.mActivityManagerService, networkStats, networkManagement);
                    ServiceManager.addService("netpolicy", networkPolicyManagerService);
                    networkPolicy = networkPolicyManagerService;
                    traceEnd();
                    traceBeginAndSlog("StartWifi");
                    Slog.i(TAG, "wifi service");
                    this.mSystemServiceManager.startService(WIFI_SERVICE_CLASS);
                    traceEnd();
                    traceBeginAndSlog("StartWifiScanning");
                    this.mSystemServiceManager.startService("com.android.server.wifi.scanner.WifiScanningService");
                    traceEnd();
                    if (disableRtt) {
                    }
                    if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.aware")) {
                    }
                    if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.direct")) {
                    }
                    if (context.getPackageManager().hasSystemFeature("android.hardware.lowpan")) {
                    }
                    if (enableWigig) {
                    }
                    traceBeginAndSlog("StartEthernet");
                    this.mSystemServiceManager.startService(ETHERNET_SERVICE_CLASS);
                    traceEnd();
                    traceBeginAndSlog("StartConnectivityService");
                    connectivityService = new ConnectivityService(context, networkManagement, networkStats, networkPolicy);
                    ServiceManager.addService("connectivity", connectivityService);
                    networkStats.bindConnectivityManager(connectivityService);
                    networkPolicy.bindConnectivityManager(connectivityService);
                    connectivity = connectivityService;
                    traceEnd();
                    traceBeginAndSlog("StartNsdService");
                    ServiceManager.addService("servicediscovery", NsdService.create(context));
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("WaitForAsecScan");
                    storageManager.waitForAsecScan();
                    traceEnd();
                    Slog.i(TAG, "Oppo Expand Service");
                    ServiceManager.addService("OPPOExService", new OppoExService(context, wm));
                    if (this.mOppoLightsService != null) {
                    }
                    traceBeginAndSlog("StartNotificationManager");
                    Slog.i(TAG, "NotificationManagerService");
                    this.mSystemServiceManager.startService(NotificationManagerService.class);
                    SystemNotificationChannels.createAll(context);
                    networkPolicy.bindNotificationManager(INotificationManager.Stub.asInterface(ServiceManager.getService("notification")));
                    traceEnd();
                    traceBeginAndSlog("StartDeviceMonitor");
                    this.mSystemServiceManager.startService(OppoDeviceStorageMonitorService.class);
                    Slog.i(TAG, "add OppoDeviceStorageMonitorService");
                    traceEnd();
                    if (disableLocation) {
                    }
                    traceBeginAndSlog("StartSearchManagerService");
                    this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                    traceEnd();
                    AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, "systemserver pid:" + Process.myPid());
                    AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, "systemserver pid:" + Process.myPid());
                    traceBeginAndSlog("StartWallpaperManagerService");
                    this.mSystemServiceManager.startService(WALLPAPER_SERVICE_CLASS);
                    traceEnd();
                    traceBeginAndSlog("StartAudioService");
                    this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.hardware.broadcastradio")) {
                    }
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartWiredAccessoryManager");
                    inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartTwilightService");
                    this.mSystemServiceManager.startService(TwilightService.class);
                    traceEnd();
                    if (NightDisplayController.isAvailable(context)) {
                    }
                    traceBeginAndSlog("StartJobScheduler");
                    this.mSystemServiceManager.startService(JobSchedulerService.class);
                    traceEnd();
                    traceBeginAndSlog("StartSoundTrigger");
                    this.mSystemServiceManager.startService(SoundTriggerService.class);
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartDiskStatsService");
                    ServiceManager.addService("diskstats", new DiskStatsService(context));
                    traceEnd();
                    Slog.i(TAG, "+OppoMultimediaService");
                    oppoMultimediaService = new OppoMultimediaService(context);
                    ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                    Slog.i(TAG, "-OppoMultimediaService");
                    multimediaService = oppoMultimediaService;
                    traceBeginAndSlog("CabcService");
                    Slog.i(TAG, "Cabc Service");
                    cabcService = new CabcService(context);
                    ServiceManager.addService("cabc", cabcService);
                    cabcService2 = cabcService;
                    Trace.traceEnd(524288);
                    traceBeginAndSlog("OppoRoundCornerService");
                    oppoRoundCornerService = null;
                    if (SystemProperties.getInt("persist.sys.enable.rc", 0) == 1) {
                    }
                    traceBeginAndSlog("StartHypnusService");
                    Slog.i(TAG, "Hypnus Service");
                    hypnusService = new HypnusService(context);
                    ServiceManager.addService("hypnus", hypnusService);
                    hypnusService2 = hypnusService;
                    traceEnd();
                    Slog.i(TAG, "Oppo Customize Service");
                    ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                    Slog.i(TAG, "Oppo Service");
                    oppoService2 = new OppoService(context);
                    ServiceManager.addService("OPPO", oppoService2);
                    oppoService = oppoService2;
                    Slog.i(TAG, "OppoAutoInstallService");
                    oppoAutoInstallService = new OppoAutoInstallService(context);
                    ServiceManager.addService("oppoautoinstall", oppoAutoInstallService);
                    oppoAutoInstallService2 = oppoAutoInstallService;
                    Slog.i(TAG, "OPPO Usage Service");
                    oppoUsageService = new OppoUsageService(context);
                    ServiceManager.addService("usage", oppoUsageService);
                    usageService = oppoUsageService;
                    if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                    }
                    if (this.mOnlyCore) {
                    }
                    if (this.mOnlyCore ? context.getResources().getBoolean(17956963) : false) {
                    }
                    traceBeginAndSlog("StartNetworkTimeUpdateService");
                    networkTimeUpdateService = new NetworkTimeUpdateService(context);
                    ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                    networkTimeUpdater = networkTimeUpdateService;
                    traceEnd();
                    traceBeginAndSlog("StartCommonTimeManagementService");
                    commonTimeManagementService = new CommonTimeManagementService(context);
                    ServiceManager.addService("commontime_management", commonTimeManagementService);
                    commonTimeMgmtService = commonTimeManagementService;
                    traceEnd();
                    if (disableNetwork) {
                    }
                    traceBeginAndSlog("StartEmergencyAffordanceService");
                    this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("AddCoverageService");
                    ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                    traceEnd();
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
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartShortcutServiceLifecycle");
                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                    traceEnd();
                    traceBeginAndSlog("StartLauncherAppsService");
                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                    traceEnd();
                    Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                    this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                    Slog.i(TAG, "Secrecy Service");
                    this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                    traceBeginAndSlog("StartMediaProjectionManager");
                    this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                    traceEnd();
                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                    }
                    if (disableCameraService) {
                    }
                    traceBeginAndSlog("StartJitCompilation");
                    VMRuntime.getRuntime().startJitCompilation();
                    traceEnd();
                    traceBeginAndSlog("StartMmsService");
                    mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                    }
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
                    if (enableWigig) {
                    }
                    Slog.i(TAG, "wms systemReady");
                    wm.systemReady();
                    traceEnd();
                    config = wm.computeNewConfiguration(0);
                    metrics = new DisplayMetrics();
                    ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                    context.getResources().updateConfiguration(config, metrics);
                    systemTheme = context.getTheme();
                    if (systemTheme.getChangingConfigurations() != 0) {
                    }
                    traceBeginAndSlog("MakePowerManagerServiceReady");
                    Slog.i(TAG, "power manager systemReady");
                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                    traceEnd();
                    if (this.mSecrecyService != null) {
                    }
                    if (this.mFingerprintService != null) {
                    }
                    if (this.mFaceService != null) {
                    }
                    if (this.mBiometricsService != null) {
                    }
                    traceBeginAndSlog("MakePackageManagerServiceReady");
                    Slog.i(TAG, "Package systemReady");
                    this.mPackageManagerService.systemReady();
                    traceEnd();
                    traceBeginAndSlog("MakeDisplayManagerServiceReady");
                    Slog.i(TAG, "DisplayManager systemReady");
                    this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                    traceEnd();
                    this.mSystemServiceManager.setSafeMode(false);
                    networkManagementF = networkManagement;
                    networkStatsF = networkStats;
                    networkPolicyF = networkPolicy;
                    connectivityF = connectivity;
                    networkScoreF = networkScore;
                    locationF = location;
                    countryDetectorF = countryDetector;
                    networkTimeUpdaterF = networkTimeUpdater;
                    commonTimeMgmtServiceF = commonTimeMgmtService;
                    inputManagerF = inputManager;
                    telephonyRegistryF = telephonyRegistry;
                    mediaRouterF = mediaRouter;
                    mmsServiceF = mmsService;
                    windowManagerF = wm;
                    multimediaServiceF = multimediaService;
                    oppoRoundCornerServiceF = oppoRoundCornerService;
                    usageServiceF = usageService;
                    mOppoService = oppoService;
                    Slog.i(TAG, "Ams systemReady");
                    this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
                }
                traceEnd();
                traceBeginAndSlog("StartNetworkStatsService");
                try {
                    networkStats = NetworkStatsService.create(context, networkManagement);
                    ServiceManager.addService("netstats", networkStats);
                } catch (Throwable e22222222222) {
                    reportWtf("starting NetworkStats Service", e22222222222);
                }
                traceEnd();
                traceBeginAndSlog("StartNetworkPolicyManagerService");
                try {
                    networkPolicyManagerService = new NetworkPolicyManagerService(context, this.mActivityManagerService, networkStats, networkManagement);
                    ServiceManager.addService("netpolicy", networkPolicyManagerService);
                    networkPolicy = networkPolicyManagerService;
                } catch (Throwable th14) {
                    e22222222222 = th14;
                    reportWtf("starting NetworkPolicy Service", e22222222222);
                    traceEnd();
                    traceBeginAndSlog("StartWifi");
                    Slog.i(TAG, "wifi service");
                    this.mSystemServiceManager.startService(WIFI_SERVICE_CLASS);
                    traceEnd();
                    traceBeginAndSlog("StartWifiScanning");
                    this.mSystemServiceManager.startService("com.android.server.wifi.scanner.WifiScanningService");
                    traceEnd();
                    if (disableRtt) {
                    }
                    if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.aware")) {
                    }
                    if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.direct")) {
                    }
                    if (context.getPackageManager().hasSystemFeature("android.hardware.lowpan")) {
                    }
                    if (enableWigig) {
                    }
                    traceBeginAndSlog("StartEthernet");
                    this.mSystemServiceManager.startService(ETHERNET_SERVICE_CLASS);
                    traceEnd();
                    traceBeginAndSlog("StartConnectivityService");
                    connectivityService = new ConnectivityService(context, networkManagement, networkStats, networkPolicy);
                    ServiceManager.addService("connectivity", connectivityService);
                    networkStats.bindConnectivityManager(connectivityService);
                    networkPolicy.bindConnectivityManager(connectivityService);
                    connectivity = connectivityService;
                    traceEnd();
                    traceBeginAndSlog("StartNsdService");
                    ServiceManager.addService("servicediscovery", NsdService.create(context));
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("WaitForAsecScan");
                    storageManager.waitForAsecScan();
                    traceEnd();
                    Slog.i(TAG, "Oppo Expand Service");
                    ServiceManager.addService("OPPOExService", new OppoExService(context, wm));
                    if (this.mOppoLightsService != null) {
                    }
                    traceBeginAndSlog("StartNotificationManager");
                    Slog.i(TAG, "NotificationManagerService");
                    this.mSystemServiceManager.startService(NotificationManagerService.class);
                    SystemNotificationChannels.createAll(context);
                    networkPolicy.bindNotificationManager(INotificationManager.Stub.asInterface(ServiceManager.getService("notification")));
                    traceEnd();
                    traceBeginAndSlog("StartDeviceMonitor");
                    this.mSystemServiceManager.startService(OppoDeviceStorageMonitorService.class);
                    Slog.i(TAG, "add OppoDeviceStorageMonitorService");
                    traceEnd();
                    if (disableLocation) {
                    }
                    traceBeginAndSlog("StartSearchManagerService");
                    this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                    traceEnd();
                    AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, "systemserver pid:" + Process.myPid());
                    AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, "systemserver pid:" + Process.myPid());
                    traceBeginAndSlog("StartWallpaperManagerService");
                    this.mSystemServiceManager.startService(WALLPAPER_SERVICE_CLASS);
                    traceEnd();
                    traceBeginAndSlog("StartAudioService");
                    this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.hardware.broadcastradio")) {
                    }
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartWiredAccessoryManager");
                    inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartTwilightService");
                    this.mSystemServiceManager.startService(TwilightService.class);
                    traceEnd();
                    if (NightDisplayController.isAvailable(context)) {
                    }
                    traceBeginAndSlog("StartJobScheduler");
                    this.mSystemServiceManager.startService(JobSchedulerService.class);
                    traceEnd();
                    traceBeginAndSlog("StartSoundTrigger");
                    this.mSystemServiceManager.startService(SoundTriggerService.class);
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartDiskStatsService");
                    ServiceManager.addService("diskstats", new DiskStatsService(context));
                    traceEnd();
                    Slog.i(TAG, "+OppoMultimediaService");
                    oppoMultimediaService = new OppoMultimediaService(context);
                    ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                    Slog.i(TAG, "-OppoMultimediaService");
                    multimediaService = oppoMultimediaService;
                    traceBeginAndSlog("CabcService");
                    Slog.i(TAG, "Cabc Service");
                    cabcService = new CabcService(context);
                    ServiceManager.addService("cabc", cabcService);
                    cabcService2 = cabcService;
                    Trace.traceEnd(524288);
                    traceBeginAndSlog("OppoRoundCornerService");
                    oppoRoundCornerService = null;
                    if (SystemProperties.getInt("persist.sys.enable.rc", 0) == 1) {
                    }
                    traceBeginAndSlog("StartHypnusService");
                    Slog.i(TAG, "Hypnus Service");
                    hypnusService = new HypnusService(context);
                    ServiceManager.addService("hypnus", hypnusService);
                    hypnusService2 = hypnusService;
                    traceEnd();
                    Slog.i(TAG, "Oppo Customize Service");
                    ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                    Slog.i(TAG, "Oppo Service");
                    oppoService2 = new OppoService(context);
                    ServiceManager.addService("OPPO", oppoService2);
                    oppoService = oppoService2;
                    Slog.i(TAG, "OppoAutoInstallService");
                    oppoAutoInstallService = new OppoAutoInstallService(context);
                    ServiceManager.addService("oppoautoinstall", oppoAutoInstallService);
                    oppoAutoInstallService2 = oppoAutoInstallService;
                    Slog.i(TAG, "OPPO Usage Service");
                    oppoUsageService = new OppoUsageService(context);
                    ServiceManager.addService("usage", oppoUsageService);
                    usageService = oppoUsageService;
                    if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                    }
                    if (this.mOnlyCore) {
                    }
                    if (this.mOnlyCore ? context.getResources().getBoolean(17956963) : false) {
                    }
                    traceBeginAndSlog("StartNetworkTimeUpdateService");
                    networkTimeUpdateService = new NetworkTimeUpdateService(context);
                    ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                    networkTimeUpdater = networkTimeUpdateService;
                    traceEnd();
                    traceBeginAndSlog("StartCommonTimeManagementService");
                    commonTimeManagementService = new CommonTimeManagementService(context);
                    ServiceManager.addService("commontime_management", commonTimeManagementService);
                    commonTimeMgmtService = commonTimeManagementService;
                    traceEnd();
                    if (disableNetwork) {
                    }
                    traceBeginAndSlog("StartEmergencyAffordanceService");
                    this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("AddCoverageService");
                    ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                    traceEnd();
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
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartShortcutServiceLifecycle");
                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                    traceEnd();
                    traceBeginAndSlog("StartLauncherAppsService");
                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                    traceEnd();
                    Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                    this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                    Slog.i(TAG, "Secrecy Service");
                    this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                    traceBeginAndSlog("StartMediaProjectionManager");
                    this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                    traceEnd();
                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                    }
                    if (disableCameraService) {
                    }
                    traceBeginAndSlog("StartJitCompilation");
                    VMRuntime.getRuntime().startJitCompilation();
                    traceEnd();
                    traceBeginAndSlog("StartMmsService");
                    mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                    }
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
                    if (enableWigig) {
                    }
                    Slog.i(TAG, "wms systemReady");
                    wm.systemReady();
                    traceEnd();
                    config = wm.computeNewConfiguration(0);
                    metrics = new DisplayMetrics();
                    ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                    context.getResources().updateConfiguration(config, metrics);
                    systemTheme = context.getTheme();
                    if (systemTheme.getChangingConfigurations() != 0) {
                    }
                    traceBeginAndSlog("MakePowerManagerServiceReady");
                    Slog.i(TAG, "power manager systemReady");
                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                    traceEnd();
                    if (this.mSecrecyService != null) {
                    }
                    if (this.mFingerprintService != null) {
                    }
                    if (this.mFaceService != null) {
                    }
                    if (this.mBiometricsService != null) {
                    }
                    traceBeginAndSlog("MakePackageManagerServiceReady");
                    Slog.i(TAG, "Package systemReady");
                    this.mPackageManagerService.systemReady();
                    traceEnd();
                    traceBeginAndSlog("MakeDisplayManagerServiceReady");
                    Slog.i(TAG, "DisplayManager systemReady");
                    this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                    traceEnd();
                    this.mSystemServiceManager.setSafeMode(false);
                    networkManagementF = networkManagement;
                    networkStatsF = networkStats;
                    networkPolicyF = networkPolicy;
                    connectivityF = connectivity;
                    networkScoreF = networkScore;
                    locationF = location;
                    countryDetectorF = countryDetector;
                    networkTimeUpdaterF = networkTimeUpdater;
                    commonTimeMgmtServiceF = commonTimeMgmtService;
                    inputManagerF = inputManager;
                    telephonyRegistryF = telephonyRegistry;
                    mediaRouterF = mediaRouter;
                    mmsServiceF = mmsService;
                    windowManagerF = wm;
                    multimediaServiceF = multimediaService;
                    oppoRoundCornerServiceF = oppoRoundCornerService;
                    usageServiceF = usageService;
                    mOppoService = oppoService;
                    Slog.i(TAG, "Ams systemReady");
                    this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
                }
                traceEnd();
                traceBeginAndSlog("StartWifi");
                Slog.i(TAG, "wifi service");
                this.mSystemServiceManager.startService(WIFI_SERVICE_CLASS);
                traceEnd();
                traceBeginAndSlog("StartWifiScanning");
                this.mSystemServiceManager.startService("com.android.server.wifi.scanner.WifiScanningService");
                traceEnd();
                if (disableRtt) {
                    traceBeginAndSlog("StartWifiRtt");
                    this.mSystemServiceManager.startService("com.android.server.wifi.RttService");
                    traceEnd();
                }
                if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.aware")) {
                    traceBeginAndSlog("StartWifiAware");
                    this.mSystemServiceManager.startService(WIFI_AWARE_SERVICE_CLASS);
                    traceEnd();
                } else {
                    Slog.i(TAG, "No Wi-Fi Aware Service (Aware support Not Present)");
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
                if (enableWigig) {
                    try {
                        Slog.i(TAG, "Wigig Service");
                        PathClassLoader pathClassLoader = new PathClassLoader("/system/framework/wigig-service.jar", getClass().getClassLoader());
                        obj = pathClassLoader.loadClass("com.qualcomm.qti.server.wigig.p2p.WigigP2pServiceImpl").getConstructor(new Class[]{Context.class}).newInstance(new Object[]{context});
                        Slog.i(TAG, "Successfully loaded WigigP2pServiceImpl class");
                        ServiceManager.addService("wigigp2p", (IBinder) obj);
                        obj2 = pathClassLoader.loadClass("com.qualcomm.qti.server.wigig.WigigService").getConstructor(new Class[]{Context.class}).newInstance(new Object[]{context});
                        Slog.i(TAG, "Successfully loaded WigigService class");
                        ServiceManager.addService("wigig", (IBinder) obj2);
                    } catch (Throwable e222222222222) {
                        reportWtf("starting WigigService", e222222222222);
                    }
                }
                if (this.mPackageManager.hasSystemFeature("android.hardware.ethernet") || this.mPackageManager.hasSystemFeature("android.hardware.usb.host")) {
                    traceBeginAndSlog("StartEthernet");
                    this.mSystemServiceManager.startService(ETHERNET_SERVICE_CLASS);
                    traceEnd();
                }
                traceBeginAndSlog("StartConnectivityService");
                try {
                    connectivityService = new ConnectivityService(context, networkManagement, networkStats, networkPolicy);
                    try {
                        ServiceManager.addService("connectivity", connectivityService);
                        networkStats.bindConnectivityManager(connectivityService);
                        networkPolicy.bindConnectivityManager(connectivityService);
                        connectivity = connectivityService;
                    } catch (Throwable th15) {
                        e222222222222 = th15;
                        connectivity = connectivityService;
                        reportWtf("starting Connectivity Service", e222222222222);
                        traceEnd();
                        traceBeginAndSlog("StartNsdService");
                        ServiceManager.addService("servicediscovery", NsdService.create(context));
                        traceEnd();
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("WaitForAsecScan");
                        storageManager.waitForAsecScan();
                        traceEnd();
                        Slog.i(TAG, "Oppo Expand Service");
                        ServiceManager.addService("OPPOExService", new OppoExService(context, wm));
                        if (this.mOppoLightsService != null) {
                        }
                        traceBeginAndSlog("StartNotificationManager");
                        Slog.i(TAG, "NotificationManagerService");
                        this.mSystemServiceManager.startService(NotificationManagerService.class);
                        SystemNotificationChannels.createAll(context);
                        networkPolicy.bindNotificationManager(INotificationManager.Stub.asInterface(ServiceManager.getService("notification")));
                        traceEnd();
                        traceBeginAndSlog("StartDeviceMonitor");
                        this.mSystemServiceManager.startService(OppoDeviceStorageMonitorService.class);
                        Slog.i(TAG, "add OppoDeviceStorageMonitorService");
                        traceEnd();
                        if (disableLocation) {
                        }
                        traceBeginAndSlog("StartSearchManagerService");
                        this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                        traceEnd();
                        AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, "systemserver pid:" + Process.myPid());
                        AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, "systemserver pid:" + Process.myPid());
                        traceBeginAndSlog("StartWallpaperManagerService");
                        this.mSystemServiceManager.startService(WALLPAPER_SERVICE_CLASS);
                        traceEnd();
                        traceBeginAndSlog("StartAudioService");
                        this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.hardware.broadcastradio")) {
                        }
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartWiredAccessoryManager");
                        inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                        traceEnd();
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartTwilightService");
                        this.mSystemServiceManager.startService(TwilightService.class);
                        traceEnd();
                        if (NightDisplayController.isAvailable(context)) {
                        }
                        traceBeginAndSlog("StartJobScheduler");
                        this.mSystemServiceManager.startService(JobSchedulerService.class);
                        traceEnd();
                        traceBeginAndSlog("StartSoundTrigger");
                        this.mSystemServiceManager.startService(SoundTriggerService.class);
                        traceEnd();
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartDiskStatsService");
                        ServiceManager.addService("diskstats", new DiskStatsService(context));
                        traceEnd();
                        Slog.i(TAG, "+OppoMultimediaService");
                        oppoMultimediaService = new OppoMultimediaService(context);
                        ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                        Slog.i(TAG, "-OppoMultimediaService");
                        multimediaService = oppoMultimediaService;
                        traceBeginAndSlog("CabcService");
                        Slog.i(TAG, "Cabc Service");
                        cabcService = new CabcService(context);
                        ServiceManager.addService("cabc", cabcService);
                        cabcService2 = cabcService;
                        Trace.traceEnd(524288);
                        traceBeginAndSlog("OppoRoundCornerService");
                        oppoRoundCornerService = null;
                        if (SystemProperties.getInt("persist.sys.enable.rc", 0) == 1) {
                        }
                        traceBeginAndSlog("StartHypnusService");
                        Slog.i(TAG, "Hypnus Service");
                        hypnusService = new HypnusService(context);
                        ServiceManager.addService("hypnus", hypnusService);
                        hypnusService2 = hypnusService;
                        traceEnd();
                        Slog.i(TAG, "Oppo Customize Service");
                        ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                        Slog.i(TAG, "Oppo Service");
                        oppoService2 = new OppoService(context);
                        ServiceManager.addService("OPPO", oppoService2);
                        oppoService = oppoService2;
                        Slog.i(TAG, "OppoAutoInstallService");
                        oppoAutoInstallService = new OppoAutoInstallService(context);
                        ServiceManager.addService("oppoautoinstall", oppoAutoInstallService);
                        oppoAutoInstallService2 = oppoAutoInstallService;
                        Slog.i(TAG, "OPPO Usage Service");
                        oppoUsageService = new OppoUsageService(context);
                        ServiceManager.addService("usage", oppoUsageService);
                        usageService = oppoUsageService;
                        if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                        }
                        if (this.mOnlyCore) {
                        }
                        if (this.mOnlyCore ? context.getResources().getBoolean(17956963) : false) {
                        }
                        traceBeginAndSlog("StartNetworkTimeUpdateService");
                        networkTimeUpdateService = new NetworkTimeUpdateService(context);
                        ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                        networkTimeUpdater = networkTimeUpdateService;
                        traceEnd();
                        traceBeginAndSlog("StartCommonTimeManagementService");
                        commonTimeManagementService = new CommonTimeManagementService(context);
                        ServiceManager.addService("commontime_management", commonTimeManagementService);
                        commonTimeMgmtService = commonTimeManagementService;
                        traceEnd();
                        if (disableNetwork) {
                        }
                        traceBeginAndSlog("StartEmergencyAffordanceService");
                        this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                        traceEnd();
                        if (disableNonCoreServices) {
                        }
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("AddCoverageService");
                        ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                        traceEnd();
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
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartShortcutServiceLifecycle");
                        this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                        traceEnd();
                        traceBeginAndSlog("StartLauncherAppsService");
                        this.mSystemServiceManager.startService(LauncherAppsService.class);
                        traceEnd();
                        Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                        this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                        Slog.i(TAG, "Secrecy Service");
                        this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                        traceBeginAndSlog("StartMediaProjectionManager");
                        this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                        traceEnd();
                        if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                        }
                        if (disableCameraService) {
                        }
                        traceBeginAndSlog("StartJitCompilation");
                        VMRuntime.getRuntime().startJitCompilation();
                        traceEnd();
                        traceBeginAndSlog("StartMmsService");
                        mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                        }
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
                        if (enableWigig) {
                        }
                        Slog.i(TAG, "wms systemReady");
                        wm.systemReady();
                        traceEnd();
                        config = wm.computeNewConfiguration(0);
                        metrics = new DisplayMetrics();
                        ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                        context.getResources().updateConfiguration(config, metrics);
                        systemTheme = context.getTheme();
                        if (systemTheme.getChangingConfigurations() != 0) {
                        }
                        traceBeginAndSlog("MakePowerManagerServiceReady");
                        Slog.i(TAG, "power manager systemReady");
                        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                        traceEnd();
                        if (this.mSecrecyService != null) {
                        }
                        if (this.mFingerprintService != null) {
                        }
                        if (this.mFaceService != null) {
                        }
                        if (this.mBiometricsService != null) {
                        }
                        traceBeginAndSlog("MakePackageManagerServiceReady");
                        Slog.i(TAG, "Package systemReady");
                        this.mPackageManagerService.systemReady();
                        traceEnd();
                        traceBeginAndSlog("MakeDisplayManagerServiceReady");
                        Slog.i(TAG, "DisplayManager systemReady");
                        this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                        traceEnd();
                        this.mSystemServiceManager.setSafeMode(false);
                        networkManagementF = networkManagement;
                        networkStatsF = networkStats;
                        networkPolicyF = networkPolicy;
                        connectivityF = connectivity;
                        networkScoreF = networkScore;
                        locationF = location;
                        countryDetectorF = countryDetector;
                        networkTimeUpdaterF = networkTimeUpdater;
                        commonTimeMgmtServiceF = commonTimeMgmtService;
                        inputManagerF = inputManager;
                        telephonyRegistryF = telephonyRegistry;
                        mediaRouterF = mediaRouter;
                        mmsServiceF = mmsService;
                        windowManagerF = wm;
                        multimediaServiceF = multimediaService;
                        oppoRoundCornerServiceF = oppoRoundCornerService;
                        usageServiceF = usageService;
                        mOppoService = oppoService;
                        Slog.i(TAG, "Ams systemReady");
                        this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
                    }
                } catch (Throwable th16) {
                    e222222222222 = th16;
                    reportWtf("starting Connectivity Service", e222222222222);
                    traceEnd();
                    traceBeginAndSlog("StartNsdService");
                    ServiceManager.addService("servicediscovery", NsdService.create(context));
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("WaitForAsecScan");
                    storageManager.waitForAsecScan();
                    traceEnd();
                    Slog.i(TAG, "Oppo Expand Service");
                    ServiceManager.addService("OPPOExService", new OppoExService(context, wm));
                    if (this.mOppoLightsService != null) {
                    }
                    traceBeginAndSlog("StartNotificationManager");
                    Slog.i(TAG, "NotificationManagerService");
                    this.mSystemServiceManager.startService(NotificationManagerService.class);
                    SystemNotificationChannels.createAll(context);
                    networkPolicy.bindNotificationManager(INotificationManager.Stub.asInterface(ServiceManager.getService("notification")));
                    traceEnd();
                    traceBeginAndSlog("StartDeviceMonitor");
                    this.mSystemServiceManager.startService(OppoDeviceStorageMonitorService.class);
                    Slog.i(TAG, "add OppoDeviceStorageMonitorService");
                    traceEnd();
                    if (disableLocation) {
                    }
                    traceBeginAndSlog("StartSearchManagerService");
                    this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                    traceEnd();
                    AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, "systemserver pid:" + Process.myPid());
                    AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, "systemserver pid:" + Process.myPid());
                    traceBeginAndSlog("StartWallpaperManagerService");
                    this.mSystemServiceManager.startService(WALLPAPER_SERVICE_CLASS);
                    traceEnd();
                    traceBeginAndSlog("StartAudioService");
                    this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.hardware.broadcastradio")) {
                    }
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartWiredAccessoryManager");
                    inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartTwilightService");
                    this.mSystemServiceManager.startService(TwilightService.class);
                    traceEnd();
                    if (NightDisplayController.isAvailable(context)) {
                    }
                    traceBeginAndSlog("StartJobScheduler");
                    this.mSystemServiceManager.startService(JobSchedulerService.class);
                    traceEnd();
                    traceBeginAndSlog("StartSoundTrigger");
                    this.mSystemServiceManager.startService(SoundTriggerService.class);
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartDiskStatsService");
                    ServiceManager.addService("diskstats", new DiskStatsService(context));
                    traceEnd();
                    Slog.i(TAG, "+OppoMultimediaService");
                    oppoMultimediaService = new OppoMultimediaService(context);
                    ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                    Slog.i(TAG, "-OppoMultimediaService");
                    multimediaService = oppoMultimediaService;
                    traceBeginAndSlog("CabcService");
                    Slog.i(TAG, "Cabc Service");
                    cabcService = new CabcService(context);
                    ServiceManager.addService("cabc", cabcService);
                    cabcService2 = cabcService;
                    Trace.traceEnd(524288);
                    traceBeginAndSlog("OppoRoundCornerService");
                    oppoRoundCornerService = null;
                    if (SystemProperties.getInt("persist.sys.enable.rc", 0) == 1) {
                    }
                    traceBeginAndSlog("StartHypnusService");
                    Slog.i(TAG, "Hypnus Service");
                    hypnusService = new HypnusService(context);
                    ServiceManager.addService("hypnus", hypnusService);
                    hypnusService2 = hypnusService;
                    traceEnd();
                    Slog.i(TAG, "Oppo Customize Service");
                    ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                    Slog.i(TAG, "Oppo Service");
                    oppoService2 = new OppoService(context);
                    ServiceManager.addService("OPPO", oppoService2);
                    oppoService = oppoService2;
                    Slog.i(TAG, "OppoAutoInstallService");
                    oppoAutoInstallService = new OppoAutoInstallService(context);
                    ServiceManager.addService("oppoautoinstall", oppoAutoInstallService);
                    oppoAutoInstallService2 = oppoAutoInstallService;
                    Slog.i(TAG, "OPPO Usage Service");
                    oppoUsageService = new OppoUsageService(context);
                    ServiceManager.addService("usage", oppoUsageService);
                    usageService = oppoUsageService;
                    if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                    }
                    if (this.mOnlyCore) {
                    }
                    if (this.mOnlyCore ? context.getResources().getBoolean(17956963) : false) {
                    }
                    traceBeginAndSlog("StartNetworkTimeUpdateService");
                    networkTimeUpdateService = new NetworkTimeUpdateService(context);
                    ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                    networkTimeUpdater = networkTimeUpdateService;
                    traceEnd();
                    traceBeginAndSlog("StartCommonTimeManagementService");
                    commonTimeManagementService = new CommonTimeManagementService(context);
                    ServiceManager.addService("commontime_management", commonTimeManagementService);
                    commonTimeMgmtService = commonTimeManagementService;
                    traceEnd();
                    if (disableNetwork) {
                    }
                    traceBeginAndSlog("StartEmergencyAffordanceService");
                    this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("AddCoverageService");
                    ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                    traceEnd();
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
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartShortcutServiceLifecycle");
                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                    traceEnd();
                    traceBeginAndSlog("StartLauncherAppsService");
                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                    traceEnd();
                    Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                    this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                    Slog.i(TAG, "Secrecy Service");
                    this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                    traceBeginAndSlog("StartMediaProjectionManager");
                    this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                    traceEnd();
                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                    }
                    if (disableCameraService) {
                    }
                    traceBeginAndSlog("StartJitCompilation");
                    VMRuntime.getRuntime().startJitCompilation();
                    traceEnd();
                    traceBeginAndSlog("StartMmsService");
                    mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                    }
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
                    if (enableWigig) {
                    }
                    Slog.i(TAG, "wms systemReady");
                    wm.systemReady();
                    traceEnd();
                    config = wm.computeNewConfiguration(0);
                    metrics = new DisplayMetrics();
                    ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                    context.getResources().updateConfiguration(config, metrics);
                    systemTheme = context.getTheme();
                    if (systemTheme.getChangingConfigurations() != 0) {
                    }
                    traceBeginAndSlog("MakePowerManagerServiceReady");
                    Slog.i(TAG, "power manager systemReady");
                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                    traceEnd();
                    if (this.mSecrecyService != null) {
                    }
                    if (this.mFingerprintService != null) {
                    }
                    if (this.mFaceService != null) {
                    }
                    if (this.mBiometricsService != null) {
                    }
                    traceBeginAndSlog("MakePackageManagerServiceReady");
                    Slog.i(TAG, "Package systemReady");
                    this.mPackageManagerService.systemReady();
                    traceEnd();
                    traceBeginAndSlog("MakeDisplayManagerServiceReady");
                    Slog.i(TAG, "DisplayManager systemReady");
                    this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                    traceEnd();
                    this.mSystemServiceManager.setSafeMode(false);
                    networkManagementF = networkManagement;
                    networkStatsF = networkStats;
                    networkPolicyF = networkPolicy;
                    connectivityF = connectivity;
                    networkScoreF = networkScore;
                    locationF = location;
                    countryDetectorF = countryDetector;
                    networkTimeUpdaterF = networkTimeUpdater;
                    commonTimeMgmtServiceF = commonTimeMgmtService;
                    inputManagerF = inputManager;
                    telephonyRegistryF = telephonyRegistry;
                    mediaRouterF = mediaRouter;
                    mmsServiceF = mmsService;
                    windowManagerF = wm;
                    multimediaServiceF = multimediaService;
                    oppoRoundCornerServiceF = oppoRoundCornerService;
                    usageServiceF = usageService;
                    mOppoService = oppoService;
                    Slog.i(TAG, "Ams systemReady");
                    this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
                }
                traceEnd();
                traceBeginAndSlog("StartNsdService");
                try {
                    ServiceManager.addService("servicediscovery", NsdService.create(context));
                } catch (Throwable e2222222222222) {
                    reportWtf("starting Service Discovery Service", e2222222222222);
                }
                traceEnd();
            }
            if (disableNonCoreServices) {
                traceBeginAndSlog("StartUpdateLockService");
                try {
                    ServiceManager.addService("updatelock", new UpdateLockService(context));
                } catch (Throwable e22222222222222) {
                    reportWtf("starting UpdateLockService", e22222222222222);
                }
                traceEnd();
            }
            if (!(storageManager == null || (this.mOnlyCore ^ 1) == 0)) {
                traceBeginAndSlog("WaitForAsecScan");
                storageManager.waitForAsecScan();
                traceEnd();
            }
            try {
                Slog.i(TAG, "Oppo Expand Service");
                ServiceManager.addService("OPPOExService", new OppoExService(context, wm));
            } catch (Throwable e222222222222222) {
                Slog.e(TAG, "Failure starting Oppo Service", e222222222222222);
            }
            try {
                if (this.mOppoLightsService != null) {
                    Slog.i(TAG, "OppoLightsService.systemReady");
                    this.mOppoLightsService.systemReady();
                }
            } catch (Throwable e2222222222222222) {
                reportWtf("making OppoLightsService ready", e2222222222222222);
            }
            traceBeginAndSlog("StartNotificationManager");
            Slog.i(TAG, "NotificationManagerService");
            this.mSystemServiceManager.startService(NotificationManagerService.class);
            SystemNotificationChannels.createAll(context);
            networkPolicy.bindNotificationManager(INotificationManager.Stub.asInterface(ServiceManager.getService("notification")));
            traceEnd();
            traceBeginAndSlog("StartDeviceMonitor");
            this.mSystemServiceManager.startService(OppoDeviceStorageMonitorService.class);
            Slog.i(TAG, "add OppoDeviceStorageMonitorService");
            traceEnd();
            if (disableLocation) {
                traceBeginAndSlog("StartLocationManagerService");
                try {
                    LocationManagerService locationManagerService = new LocationManagerService(context);
                    try {
                        ServiceManager.addService("location", locationManagerService);
                        location = locationManagerService;
                    } catch (Throwable th17) {
                        e2222222222222222 = th17;
                        location = locationManagerService;
                        reportWtf("starting Location Manager", e2222222222222222);
                        traceEnd();
                        traceBeginAndSlog("StartCountryDetectorService");
                        countryDetectorService = new CountryDetectorService(context);
                        try {
                            ServiceManager.addService("country_detector", countryDetectorService);
                            countryDetector = countryDetectorService;
                        } catch (Throwable th18) {
                            e2222222222222222 = th18;
                            countryDetector = countryDetectorService;
                            reportWtf("starting Country Detector", e2222222222222222);
                            traceEnd();
                            traceBeginAndSlog("StartSearchManagerService");
                            this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                            traceEnd();
                            AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, "systemserver pid:" + Process.myPid());
                            AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, "systemserver pid:" + Process.myPid());
                            traceBeginAndSlog("StartWallpaperManagerService");
                            this.mSystemServiceManager.startService(WALLPAPER_SERVICE_CLASS);
                            traceEnd();
                            traceBeginAndSlog("StartAudioService");
                            this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                            traceEnd();
                            if (this.mPackageManager.hasSystemFeature("android.hardware.broadcastradio")) {
                            }
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartWiredAccessoryManager");
                            inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                            traceEnd();
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartTwilightService");
                            this.mSystemServiceManager.startService(TwilightService.class);
                            traceEnd();
                            if (NightDisplayController.isAvailable(context)) {
                            }
                            traceBeginAndSlog("StartJobScheduler");
                            this.mSystemServiceManager.startService(JobSchedulerService.class);
                            traceEnd();
                            traceBeginAndSlog("StartSoundTrigger");
                            this.mSystemServiceManager.startService(SoundTriggerService.class);
                            traceEnd();
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartDiskStatsService");
                            ServiceManager.addService("diskstats", new DiskStatsService(context));
                            traceEnd();
                            Slog.i(TAG, "+OppoMultimediaService");
                            oppoMultimediaService = new OppoMultimediaService(context);
                            ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                            Slog.i(TAG, "-OppoMultimediaService");
                            multimediaService = oppoMultimediaService;
                            traceBeginAndSlog("CabcService");
                            Slog.i(TAG, "Cabc Service");
                            cabcService = new CabcService(context);
                            ServiceManager.addService("cabc", cabcService);
                            cabcService2 = cabcService;
                            Trace.traceEnd(524288);
                            traceBeginAndSlog("OppoRoundCornerService");
                            oppoRoundCornerService = null;
                            if (SystemProperties.getInt("persist.sys.enable.rc", 0) == 1) {
                            }
                            traceBeginAndSlog("StartHypnusService");
                            Slog.i(TAG, "Hypnus Service");
                            hypnusService = new HypnusService(context);
                            ServiceManager.addService("hypnus", hypnusService);
                            hypnusService2 = hypnusService;
                            traceEnd();
                            Slog.i(TAG, "Oppo Customize Service");
                            ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                            Slog.i(TAG, "Oppo Service");
                            oppoService2 = new OppoService(context);
                            ServiceManager.addService("OPPO", oppoService2);
                            oppoService = oppoService2;
                            Slog.i(TAG, "OppoAutoInstallService");
                            oppoAutoInstallService = new OppoAutoInstallService(context);
                            ServiceManager.addService("oppoautoinstall", oppoAutoInstallService);
                            oppoAutoInstallService2 = oppoAutoInstallService;
                            Slog.i(TAG, "OPPO Usage Service");
                            oppoUsageService = new OppoUsageService(context);
                            ServiceManager.addService("usage", oppoUsageService);
                            usageService = oppoUsageService;
                            if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                            }
                            if (this.mOnlyCore) {
                            }
                            if (this.mOnlyCore ? context.getResources().getBoolean(17956963) : false) {
                            }
                            traceBeginAndSlog("StartNetworkTimeUpdateService");
                            networkTimeUpdateService = new NetworkTimeUpdateService(context);
                            ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                            networkTimeUpdater = networkTimeUpdateService;
                            traceEnd();
                            traceBeginAndSlog("StartCommonTimeManagementService");
                            commonTimeManagementService = new CommonTimeManagementService(context);
                            ServiceManager.addService("commontime_management", commonTimeManagementService);
                            commonTimeMgmtService = commonTimeManagementService;
                            traceEnd();
                            if (disableNetwork) {
                            }
                            traceBeginAndSlog("StartEmergencyAffordanceService");
                            this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                            traceEnd();
                            if (disableNonCoreServices) {
                            }
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("AddCoverageService");
                            ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                            traceEnd();
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
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartShortcutServiceLifecycle");
                            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                            traceEnd();
                            traceBeginAndSlog("StartLauncherAppsService");
                            this.mSystemServiceManager.startService(LauncherAppsService.class);
                            traceEnd();
                            Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                            this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                            Slog.i(TAG, "Secrecy Service");
                            this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                            traceBeginAndSlog("StartMediaProjectionManager");
                            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                            traceEnd();
                            if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                            }
                            if (disableCameraService) {
                            }
                            traceBeginAndSlog("StartJitCompilation");
                            VMRuntime.getRuntime().startJitCompilation();
                            traceEnd();
                            traceBeginAndSlog("StartMmsService");
                            mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                            traceEnd();
                            if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                            }
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
                            if (enableWigig) {
                            }
                            Slog.i(TAG, "wms systemReady");
                            wm.systemReady();
                            traceEnd();
                            config = wm.computeNewConfiguration(0);
                            metrics = new DisplayMetrics();
                            ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                            context.getResources().updateConfiguration(config, metrics);
                            systemTheme = context.getTheme();
                            if (systemTheme.getChangingConfigurations() != 0) {
                            }
                            traceBeginAndSlog("MakePowerManagerServiceReady");
                            Slog.i(TAG, "power manager systemReady");
                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                            traceEnd();
                            if (this.mSecrecyService != null) {
                            }
                            if (this.mFingerprintService != null) {
                            }
                            if (this.mFaceService != null) {
                            }
                            if (this.mBiometricsService != null) {
                            }
                            traceBeginAndSlog("MakePackageManagerServiceReady");
                            Slog.i(TAG, "Package systemReady");
                            this.mPackageManagerService.systemReady();
                            traceEnd();
                            traceBeginAndSlog("MakeDisplayManagerServiceReady");
                            Slog.i(TAG, "DisplayManager systemReady");
                            this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                            traceEnd();
                            this.mSystemServiceManager.setSafeMode(false);
                            networkManagementF = networkManagement;
                            networkStatsF = networkStats;
                            networkPolicyF = networkPolicy;
                            connectivityF = connectivity;
                            networkScoreF = networkScore;
                            locationF = location;
                            countryDetectorF = countryDetector;
                            networkTimeUpdaterF = networkTimeUpdater;
                            commonTimeMgmtServiceF = commonTimeMgmtService;
                            inputManagerF = inputManager;
                            telephonyRegistryF = telephonyRegistry;
                            mediaRouterF = mediaRouter;
                            mmsServiceF = mmsService;
                            windowManagerF = wm;
                            multimediaServiceF = multimediaService;
                            oppoRoundCornerServiceF = oppoRoundCornerService;
                            usageServiceF = usageService;
                            mOppoService = oppoService;
                            Slog.i(TAG, "Ams systemReady");
                            this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
                        }
                        traceEnd();
                        traceBeginAndSlog("StartSearchManagerService");
                        this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                        traceEnd();
                        AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, "systemserver pid:" + Process.myPid());
                        AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, "systemserver pid:" + Process.myPid());
                        traceBeginAndSlog("StartWallpaperManagerService");
                        this.mSystemServiceManager.startService(WALLPAPER_SERVICE_CLASS);
                        traceEnd();
                        traceBeginAndSlog("StartAudioService");
                        this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.hardware.broadcastradio")) {
                        }
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartWiredAccessoryManager");
                        inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                        traceEnd();
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartTwilightService");
                        this.mSystemServiceManager.startService(TwilightService.class);
                        traceEnd();
                        if (NightDisplayController.isAvailable(context)) {
                        }
                        traceBeginAndSlog("StartJobScheduler");
                        this.mSystemServiceManager.startService(JobSchedulerService.class);
                        traceEnd();
                        traceBeginAndSlog("StartSoundTrigger");
                        this.mSystemServiceManager.startService(SoundTriggerService.class);
                        traceEnd();
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartDiskStatsService");
                        ServiceManager.addService("diskstats", new DiskStatsService(context));
                        traceEnd();
                        Slog.i(TAG, "+OppoMultimediaService");
                        oppoMultimediaService = new OppoMultimediaService(context);
                        ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                        Slog.i(TAG, "-OppoMultimediaService");
                        multimediaService = oppoMultimediaService;
                        traceBeginAndSlog("CabcService");
                        Slog.i(TAG, "Cabc Service");
                        cabcService = new CabcService(context);
                        ServiceManager.addService("cabc", cabcService);
                        cabcService2 = cabcService;
                        Trace.traceEnd(524288);
                        traceBeginAndSlog("OppoRoundCornerService");
                        oppoRoundCornerService = null;
                        if (SystemProperties.getInt("persist.sys.enable.rc", 0) == 1) {
                        }
                        traceBeginAndSlog("StartHypnusService");
                        Slog.i(TAG, "Hypnus Service");
                        hypnusService = new HypnusService(context);
                        ServiceManager.addService("hypnus", hypnusService);
                        hypnusService2 = hypnusService;
                        traceEnd();
                        Slog.i(TAG, "Oppo Customize Service");
                        ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                        Slog.i(TAG, "Oppo Service");
                        oppoService2 = new OppoService(context);
                        ServiceManager.addService("OPPO", oppoService2);
                        oppoService = oppoService2;
                        Slog.i(TAG, "OppoAutoInstallService");
                        oppoAutoInstallService = new OppoAutoInstallService(context);
                        ServiceManager.addService("oppoautoinstall", oppoAutoInstallService);
                        oppoAutoInstallService2 = oppoAutoInstallService;
                        Slog.i(TAG, "OPPO Usage Service");
                        oppoUsageService = new OppoUsageService(context);
                        ServiceManager.addService("usage", oppoUsageService);
                        usageService = oppoUsageService;
                        if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                        }
                        if (this.mOnlyCore) {
                        }
                        if (this.mOnlyCore ? context.getResources().getBoolean(17956963) : false) {
                        }
                        traceBeginAndSlog("StartNetworkTimeUpdateService");
                        networkTimeUpdateService = new NetworkTimeUpdateService(context);
                        ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                        networkTimeUpdater = networkTimeUpdateService;
                        traceEnd();
                        traceBeginAndSlog("StartCommonTimeManagementService");
                        commonTimeManagementService = new CommonTimeManagementService(context);
                        ServiceManager.addService("commontime_management", commonTimeManagementService);
                        commonTimeMgmtService = commonTimeManagementService;
                        traceEnd();
                        if (disableNetwork) {
                        }
                        traceBeginAndSlog("StartEmergencyAffordanceService");
                        this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                        traceEnd();
                        if (disableNonCoreServices) {
                        }
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("AddCoverageService");
                        ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                        traceEnd();
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
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartShortcutServiceLifecycle");
                        this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                        traceEnd();
                        traceBeginAndSlog("StartLauncherAppsService");
                        this.mSystemServiceManager.startService(LauncherAppsService.class);
                        traceEnd();
                        Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                        this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                        Slog.i(TAG, "Secrecy Service");
                        this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                        traceBeginAndSlog("StartMediaProjectionManager");
                        this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                        traceEnd();
                        if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                        }
                        if (disableCameraService) {
                        }
                        traceBeginAndSlog("StartJitCompilation");
                        VMRuntime.getRuntime().startJitCompilation();
                        traceEnd();
                        traceBeginAndSlog("StartMmsService");
                        mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                        }
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
                        if (enableWigig) {
                        }
                        Slog.i(TAG, "wms systemReady");
                        wm.systemReady();
                        traceEnd();
                        config = wm.computeNewConfiguration(0);
                        metrics = new DisplayMetrics();
                        ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                        context.getResources().updateConfiguration(config, metrics);
                        systemTheme = context.getTheme();
                        if (systemTheme.getChangingConfigurations() != 0) {
                        }
                        traceBeginAndSlog("MakePowerManagerServiceReady");
                        Slog.i(TAG, "power manager systemReady");
                        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                        traceEnd();
                        if (this.mSecrecyService != null) {
                        }
                        if (this.mFingerprintService != null) {
                        }
                        if (this.mFaceService != null) {
                        }
                        if (this.mBiometricsService != null) {
                        }
                        traceBeginAndSlog("MakePackageManagerServiceReady");
                        Slog.i(TAG, "Package systemReady");
                        this.mPackageManagerService.systemReady();
                        traceEnd();
                        traceBeginAndSlog("MakeDisplayManagerServiceReady");
                        Slog.i(TAG, "DisplayManager systemReady");
                        this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                        traceEnd();
                        this.mSystemServiceManager.setSafeMode(false);
                        networkManagementF = networkManagement;
                        networkStatsF = networkStats;
                        networkPolicyF = networkPolicy;
                        connectivityF = connectivity;
                        networkScoreF = networkScore;
                        locationF = location;
                        countryDetectorF = countryDetector;
                        networkTimeUpdaterF = networkTimeUpdater;
                        commonTimeMgmtServiceF = commonTimeMgmtService;
                        inputManagerF = inputManager;
                        telephonyRegistryF = telephonyRegistry;
                        mediaRouterF = mediaRouter;
                        mmsServiceF = mmsService;
                        windowManagerF = wm;
                        multimediaServiceF = multimediaService;
                        oppoRoundCornerServiceF = oppoRoundCornerService;
                        usageServiceF = usageService;
                        mOppoService = oppoService;
                        Slog.i(TAG, "Ams systemReady");
                        this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
                    }
                } catch (Throwable th19) {
                    e2222222222222222 = th19;
                    reportWtf("starting Location Manager", e2222222222222222);
                    traceEnd();
                    traceBeginAndSlog("StartCountryDetectorService");
                    countryDetectorService = new CountryDetectorService(context);
                    ServiceManager.addService("country_detector", countryDetectorService);
                    countryDetector = countryDetectorService;
                    traceEnd();
                    traceBeginAndSlog("StartSearchManagerService");
                    this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                    traceEnd();
                    AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, "systemserver pid:" + Process.myPid());
                    AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, "systemserver pid:" + Process.myPid());
                    traceBeginAndSlog("StartWallpaperManagerService");
                    this.mSystemServiceManager.startService(WALLPAPER_SERVICE_CLASS);
                    traceEnd();
                    traceBeginAndSlog("StartAudioService");
                    this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.hardware.broadcastradio")) {
                    }
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartWiredAccessoryManager");
                    inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartTwilightService");
                    this.mSystemServiceManager.startService(TwilightService.class);
                    traceEnd();
                    if (NightDisplayController.isAvailable(context)) {
                    }
                    traceBeginAndSlog("StartJobScheduler");
                    this.mSystemServiceManager.startService(JobSchedulerService.class);
                    traceEnd();
                    traceBeginAndSlog("StartSoundTrigger");
                    this.mSystemServiceManager.startService(SoundTriggerService.class);
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartDiskStatsService");
                    ServiceManager.addService("diskstats", new DiskStatsService(context));
                    traceEnd();
                    Slog.i(TAG, "+OppoMultimediaService");
                    oppoMultimediaService = new OppoMultimediaService(context);
                    ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                    Slog.i(TAG, "-OppoMultimediaService");
                    multimediaService = oppoMultimediaService;
                    traceBeginAndSlog("CabcService");
                    Slog.i(TAG, "Cabc Service");
                    cabcService = new CabcService(context);
                    ServiceManager.addService("cabc", cabcService);
                    cabcService2 = cabcService;
                    Trace.traceEnd(524288);
                    traceBeginAndSlog("OppoRoundCornerService");
                    oppoRoundCornerService = null;
                    if (SystemProperties.getInt("persist.sys.enable.rc", 0) == 1) {
                    }
                    traceBeginAndSlog("StartHypnusService");
                    Slog.i(TAG, "Hypnus Service");
                    hypnusService = new HypnusService(context);
                    ServiceManager.addService("hypnus", hypnusService);
                    hypnusService2 = hypnusService;
                    traceEnd();
                    Slog.i(TAG, "Oppo Customize Service");
                    ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                    Slog.i(TAG, "Oppo Service");
                    oppoService2 = new OppoService(context);
                    ServiceManager.addService("OPPO", oppoService2);
                    oppoService = oppoService2;
                    Slog.i(TAG, "OppoAutoInstallService");
                    oppoAutoInstallService = new OppoAutoInstallService(context);
                    ServiceManager.addService("oppoautoinstall", oppoAutoInstallService);
                    oppoAutoInstallService2 = oppoAutoInstallService;
                    Slog.i(TAG, "OPPO Usage Service");
                    oppoUsageService = new OppoUsageService(context);
                    ServiceManager.addService("usage", oppoUsageService);
                    usageService = oppoUsageService;
                    if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                    }
                    if (this.mOnlyCore) {
                    }
                    if (this.mOnlyCore ? context.getResources().getBoolean(17956963) : false) {
                    }
                    traceBeginAndSlog("StartNetworkTimeUpdateService");
                    networkTimeUpdateService = new NetworkTimeUpdateService(context);
                    ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                    networkTimeUpdater = networkTimeUpdateService;
                    traceEnd();
                    traceBeginAndSlog("StartCommonTimeManagementService");
                    commonTimeManagementService = new CommonTimeManagementService(context);
                    ServiceManager.addService("commontime_management", commonTimeManagementService);
                    commonTimeMgmtService = commonTimeManagementService;
                    traceEnd();
                    if (disableNetwork) {
                    }
                    traceBeginAndSlog("StartEmergencyAffordanceService");
                    this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("AddCoverageService");
                    ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                    traceEnd();
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
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartShortcutServiceLifecycle");
                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                    traceEnd();
                    traceBeginAndSlog("StartLauncherAppsService");
                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                    traceEnd();
                    Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                    this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                    Slog.i(TAG, "Secrecy Service");
                    this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                    traceBeginAndSlog("StartMediaProjectionManager");
                    this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                    traceEnd();
                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                    }
                    if (disableCameraService) {
                    }
                    traceBeginAndSlog("StartJitCompilation");
                    VMRuntime.getRuntime().startJitCompilation();
                    traceEnd();
                    traceBeginAndSlog("StartMmsService");
                    mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                    }
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
                    if (enableWigig) {
                    }
                    Slog.i(TAG, "wms systemReady");
                    wm.systemReady();
                    traceEnd();
                    config = wm.computeNewConfiguration(0);
                    metrics = new DisplayMetrics();
                    ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                    context.getResources().updateConfiguration(config, metrics);
                    systemTheme = context.getTheme();
                    if (systemTheme.getChangingConfigurations() != 0) {
                    }
                    traceBeginAndSlog("MakePowerManagerServiceReady");
                    Slog.i(TAG, "power manager systemReady");
                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                    traceEnd();
                    if (this.mSecrecyService != null) {
                    }
                    if (this.mFingerprintService != null) {
                    }
                    if (this.mFaceService != null) {
                    }
                    if (this.mBiometricsService != null) {
                    }
                    traceBeginAndSlog("MakePackageManagerServiceReady");
                    Slog.i(TAG, "Package systemReady");
                    this.mPackageManagerService.systemReady();
                    traceEnd();
                    traceBeginAndSlog("MakeDisplayManagerServiceReady");
                    Slog.i(TAG, "DisplayManager systemReady");
                    this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                    traceEnd();
                    this.mSystemServiceManager.setSafeMode(false);
                    networkManagementF = networkManagement;
                    networkStatsF = networkStats;
                    networkPolicyF = networkPolicy;
                    connectivityF = connectivity;
                    networkScoreF = networkScore;
                    locationF = location;
                    countryDetectorF = countryDetector;
                    networkTimeUpdaterF = networkTimeUpdater;
                    commonTimeMgmtServiceF = commonTimeMgmtService;
                    inputManagerF = inputManager;
                    telephonyRegistryF = telephonyRegistry;
                    mediaRouterF = mediaRouter;
                    mmsServiceF = mmsService;
                    windowManagerF = wm;
                    multimediaServiceF = multimediaService;
                    oppoRoundCornerServiceF = oppoRoundCornerService;
                    usageServiceF = usageService;
                    mOppoService = oppoService;
                    Slog.i(TAG, "Ams systemReady");
                    this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
                }
                traceEnd();
                traceBeginAndSlog("StartCountryDetectorService");
                try {
                    countryDetectorService = new CountryDetectorService(context);
                    ServiceManager.addService("country_detector", countryDetectorService);
                    countryDetector = countryDetectorService;
                } catch (Throwable th20) {
                    e2222222222222222 = th20;
                    reportWtf("starting Country Detector", e2222222222222222);
                    traceEnd();
                    traceBeginAndSlog("StartSearchManagerService");
                    this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                    traceEnd();
                    AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, "systemserver pid:" + Process.myPid());
                    AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, "systemserver pid:" + Process.myPid());
                    traceBeginAndSlog("StartWallpaperManagerService");
                    this.mSystemServiceManager.startService(WALLPAPER_SERVICE_CLASS);
                    traceEnd();
                    traceBeginAndSlog("StartAudioService");
                    this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.hardware.broadcastradio")) {
                    }
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartWiredAccessoryManager");
                    inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartTwilightService");
                    this.mSystemServiceManager.startService(TwilightService.class);
                    traceEnd();
                    if (NightDisplayController.isAvailable(context)) {
                    }
                    traceBeginAndSlog("StartJobScheduler");
                    this.mSystemServiceManager.startService(JobSchedulerService.class);
                    traceEnd();
                    traceBeginAndSlog("StartSoundTrigger");
                    this.mSystemServiceManager.startService(SoundTriggerService.class);
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartDiskStatsService");
                    ServiceManager.addService("diskstats", new DiskStatsService(context));
                    traceEnd();
                    Slog.i(TAG, "+OppoMultimediaService");
                    oppoMultimediaService = new OppoMultimediaService(context);
                    ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                    Slog.i(TAG, "-OppoMultimediaService");
                    multimediaService = oppoMultimediaService;
                    traceBeginAndSlog("CabcService");
                    Slog.i(TAG, "Cabc Service");
                    cabcService = new CabcService(context);
                    ServiceManager.addService("cabc", cabcService);
                    cabcService2 = cabcService;
                    Trace.traceEnd(524288);
                    traceBeginAndSlog("OppoRoundCornerService");
                    oppoRoundCornerService = null;
                    if (SystemProperties.getInt("persist.sys.enable.rc", 0) == 1) {
                    }
                    traceBeginAndSlog("StartHypnusService");
                    Slog.i(TAG, "Hypnus Service");
                    hypnusService = new HypnusService(context);
                    ServiceManager.addService("hypnus", hypnusService);
                    hypnusService2 = hypnusService;
                    traceEnd();
                    Slog.i(TAG, "Oppo Customize Service");
                    ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                    Slog.i(TAG, "Oppo Service");
                    oppoService2 = new OppoService(context);
                    ServiceManager.addService("OPPO", oppoService2);
                    oppoService = oppoService2;
                    Slog.i(TAG, "OppoAutoInstallService");
                    oppoAutoInstallService = new OppoAutoInstallService(context);
                    ServiceManager.addService("oppoautoinstall", oppoAutoInstallService);
                    oppoAutoInstallService2 = oppoAutoInstallService;
                    Slog.i(TAG, "OPPO Usage Service");
                    oppoUsageService = new OppoUsageService(context);
                    ServiceManager.addService("usage", oppoUsageService);
                    usageService = oppoUsageService;
                    if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                    }
                    if (this.mOnlyCore) {
                    }
                    if (this.mOnlyCore ? context.getResources().getBoolean(17956963) : false) {
                    }
                    traceBeginAndSlog("StartNetworkTimeUpdateService");
                    networkTimeUpdateService = new NetworkTimeUpdateService(context);
                    ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                    networkTimeUpdater = networkTimeUpdateService;
                    traceEnd();
                    traceBeginAndSlog("StartCommonTimeManagementService");
                    commonTimeManagementService = new CommonTimeManagementService(context);
                    ServiceManager.addService("commontime_management", commonTimeManagementService);
                    commonTimeMgmtService = commonTimeManagementService;
                    traceEnd();
                    if (disableNetwork) {
                    }
                    traceBeginAndSlog("StartEmergencyAffordanceService");
                    this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("AddCoverageService");
                    ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                    traceEnd();
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
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartShortcutServiceLifecycle");
                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                    traceEnd();
                    traceBeginAndSlog("StartLauncherAppsService");
                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                    traceEnd();
                    Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                    this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                    Slog.i(TAG, "Secrecy Service");
                    this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                    traceBeginAndSlog("StartMediaProjectionManager");
                    this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                    traceEnd();
                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                    }
                    if (disableCameraService) {
                    }
                    traceBeginAndSlog("StartJitCompilation");
                    VMRuntime.getRuntime().startJitCompilation();
                    traceEnd();
                    traceBeginAndSlog("StartMmsService");
                    mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                    }
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
                    if (enableWigig) {
                    }
                    Slog.i(TAG, "wms systemReady");
                    wm.systemReady();
                    traceEnd();
                    config = wm.computeNewConfiguration(0);
                    metrics = new DisplayMetrics();
                    ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                    context.getResources().updateConfiguration(config, metrics);
                    systemTheme = context.getTheme();
                    if (systemTheme.getChangingConfigurations() != 0) {
                    }
                    traceBeginAndSlog("MakePowerManagerServiceReady");
                    Slog.i(TAG, "power manager systemReady");
                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                    traceEnd();
                    if (this.mSecrecyService != null) {
                    }
                    if (this.mFingerprintService != null) {
                    }
                    if (this.mFaceService != null) {
                    }
                    if (this.mBiometricsService != null) {
                    }
                    traceBeginAndSlog("MakePackageManagerServiceReady");
                    Slog.i(TAG, "Package systemReady");
                    this.mPackageManagerService.systemReady();
                    traceEnd();
                    traceBeginAndSlog("MakeDisplayManagerServiceReady");
                    Slog.i(TAG, "DisplayManager systemReady");
                    this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                    traceEnd();
                    this.mSystemServiceManager.setSafeMode(false);
                    networkManagementF = networkManagement;
                    networkStatsF = networkStats;
                    networkPolicyF = networkPolicy;
                    connectivityF = connectivity;
                    networkScoreF = networkScore;
                    locationF = location;
                    countryDetectorF = countryDetector;
                    networkTimeUpdaterF = networkTimeUpdater;
                    commonTimeMgmtServiceF = commonTimeMgmtService;
                    inputManagerF = inputManager;
                    telephonyRegistryF = telephonyRegistry;
                    mediaRouterF = mediaRouter;
                    mmsServiceF = mmsService;
                    windowManagerF = wm;
                    multimediaServiceF = multimediaService;
                    oppoRoundCornerServiceF = oppoRoundCornerService;
                    usageServiceF = usageService;
                    mOppoService = oppoService;
                    Slog.i(TAG, "Ams systemReady");
                    this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
                }
                traceEnd();
            }
            if (!(disableNonCoreServices || (disableSearchManager ^ 1) == 0)) {
                traceBeginAndSlog("StartSearchManagerService");
                this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                traceEnd();
            }
            AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, "systemserver pid:" + Process.myPid());
            AgingCriticalEvent.getInstance().writeEvent(AgingCriticalEvent.EVENT_SYSTEM_BOOTUP, "systemserver pid:" + Process.myPid());
            if (!disableNonCoreServices && context.getResources().getBoolean(17956964)) {
                traceBeginAndSlog("StartWallpaperManagerService");
                this.mSystemServiceManager.startService(WALLPAPER_SERVICE_CLASS);
                traceEnd();
            }
            traceBeginAndSlog("StartAudioService");
            this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
            traceEnd();
            if (this.mPackageManager.hasSystemFeature("android.hardware.broadcastradio")) {
                traceBeginAndSlog("StartBroadcastRadioService");
                this.mSystemServiceManager.startService(BroadcastRadioService.class);
                traceEnd();
            }
            if (disableNonCoreServices) {
                traceBeginAndSlog("StartDockObserver");
                Slog.i(TAG, "DockObserver");
                this.mSystemServiceManager.startService(DockObserver.class);
                traceEnd();
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                    traceBeginAndSlog("StartThermalObserver");
                    this.mSystemServiceManager.startService(THERMAL_OBSERVER_CLASS);
                    traceEnd();
                }
            }
            traceBeginAndSlog("StartWiredAccessoryManager");
            try {
                inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
            } catch (Throwable e22222222222222222) {
                reportWtf("starting WiredAccessoryManager", e22222222222222222);
            }
            traceEnd();
            if (disableNonCoreServices) {
                if (this.mPackageManager.hasSystemFeature("android.software.midi")) {
                    traceBeginAndSlog("StartMidiManager");
                    this.mSystemServiceManager.startService(MIDI_SERVICE_CLASS);
                    traceEnd();
                }
                if (this.mPackageManager.hasSystemFeature("android.hardware.usb.host") || this.mPackageManager.hasSystemFeature("android.hardware.usb.accessory")) {
                    traceBeginAndSlog("StartUsbService");
                    this.mSystemServiceManager.startService(USB_SERVICE_CLASS);
                    traceEnd();
                }
                if (!disableSerial) {
                    traceBeginAndSlog("StartSerialService");
                    try {
                        SerialService serialService = new SerialService(context);
                        SerialService serialService2;
                        try {
                            ServiceManager.addService("serial", serialService);
                            serialService2 = serialService;
                        } catch (Throwable th21) {
                            e22222222222222222 = th21;
                            serialService2 = serialService;
                            Slog.e(TAG, "Failure starting SerialService", e22222222222222222);
                            traceEnd();
                            traceBeginAndSlog("StartHardwarePropertiesManagerService");
                            hardwarePropertiesManagerService = new HardwarePropertiesManagerService(context);
                            try {
                                ServiceManager.addService("hardware_properties", hardwarePropertiesManagerService);
                                hardwarePropertiesManagerService2 = hardwarePropertiesManagerService;
                            } catch (Throwable th22) {
                                e22222222222222222 = th22;
                                hardwarePropertiesManagerService2 = hardwarePropertiesManagerService;
                                Slog.e(TAG, "Failure starting HardwarePropertiesManagerService", e22222222222222222);
                                traceEnd();
                                traceBeginAndSlog("StartTwilightService");
                                this.mSystemServiceManager.startService(TwilightService.class);
                                traceEnd();
                                if (NightDisplayController.isAvailable(context)) {
                                }
                                traceBeginAndSlog("StartJobScheduler");
                                this.mSystemServiceManager.startService(JobSchedulerService.class);
                                traceEnd();
                                traceBeginAndSlog("StartSoundTrigger");
                                this.mSystemServiceManager.startService(SoundTriggerService.class);
                                traceEnd();
                                if (disableNonCoreServices) {
                                }
                                traceBeginAndSlog("StartDiskStatsService");
                                ServiceManager.addService("diskstats", new DiskStatsService(context));
                                traceEnd();
                                Slog.i(TAG, "+OppoMultimediaService");
                                oppoMultimediaService = new OppoMultimediaService(context);
                                ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                                Slog.i(TAG, "-OppoMultimediaService");
                                multimediaService = oppoMultimediaService;
                                traceBeginAndSlog("CabcService");
                                Slog.i(TAG, "Cabc Service");
                                cabcService = new CabcService(context);
                                ServiceManager.addService("cabc", cabcService);
                                cabcService2 = cabcService;
                                Trace.traceEnd(524288);
                                traceBeginAndSlog("OppoRoundCornerService");
                                oppoRoundCornerService = null;
                                if (SystemProperties.getInt("persist.sys.enable.rc", 0) == 1) {
                                }
                                traceBeginAndSlog("StartHypnusService");
                                Slog.i(TAG, "Hypnus Service");
                                hypnusService = new HypnusService(context);
                                ServiceManager.addService("hypnus", hypnusService);
                                hypnusService2 = hypnusService;
                                traceEnd();
                                Slog.i(TAG, "Oppo Customize Service");
                                ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                                Slog.i(TAG, "Oppo Service");
                                oppoService2 = new OppoService(context);
                                ServiceManager.addService("OPPO", oppoService2);
                                oppoService = oppoService2;
                                Slog.i(TAG, "OppoAutoInstallService");
                                oppoAutoInstallService = new OppoAutoInstallService(context);
                                ServiceManager.addService("oppoautoinstall", oppoAutoInstallService);
                                oppoAutoInstallService2 = oppoAutoInstallService;
                                Slog.i(TAG, "OPPO Usage Service");
                                oppoUsageService = new OppoUsageService(context);
                                ServiceManager.addService("usage", oppoUsageService);
                                usageService = oppoUsageService;
                                if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                                }
                                if (this.mOnlyCore) {
                                }
                                if (this.mOnlyCore ? context.getResources().getBoolean(17956963) : false) {
                                }
                                traceBeginAndSlog("StartNetworkTimeUpdateService");
                                networkTimeUpdateService = new NetworkTimeUpdateService(context);
                                ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                                networkTimeUpdater = networkTimeUpdateService;
                                traceEnd();
                                traceBeginAndSlog("StartCommonTimeManagementService");
                                commonTimeManagementService = new CommonTimeManagementService(context);
                                ServiceManager.addService("commontime_management", commonTimeManagementService);
                                commonTimeMgmtService = commonTimeManagementService;
                                traceEnd();
                                if (disableNetwork) {
                                }
                                traceBeginAndSlog("StartEmergencyAffordanceService");
                                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                                traceEnd();
                                if (disableNonCoreServices) {
                                }
                                if (disableNonCoreServices) {
                                }
                                traceBeginAndSlog("AddCoverageService");
                                ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                                traceEnd();
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
                                if (disableNonCoreServices) {
                                }
                                traceBeginAndSlog("StartShortcutServiceLifecycle");
                                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                traceEnd();
                                traceBeginAndSlog("StartLauncherAppsService");
                                this.mSystemServiceManager.startService(LauncherAppsService.class);
                                traceEnd();
                                Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                                this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                                Slog.i(TAG, "Secrecy Service");
                                this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                                traceBeginAndSlog("StartMediaProjectionManager");
                                this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                                traceEnd();
                                if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                                }
                                if (disableCameraService) {
                                }
                                traceBeginAndSlog("StartJitCompilation");
                                VMRuntime.getRuntime().startJitCompilation();
                                traceEnd();
                                traceBeginAndSlog("StartMmsService");
                                mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                traceEnd();
                                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                                }
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
                                if (enableWigig) {
                                }
                                Slog.i(TAG, "wms systemReady");
                                wm.systemReady();
                                traceEnd();
                                config = wm.computeNewConfiguration(0);
                                metrics = new DisplayMetrics();
                                ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                                context.getResources().updateConfiguration(config, metrics);
                                systemTheme = context.getTheme();
                                if (systemTheme.getChangingConfigurations() != 0) {
                                }
                                traceBeginAndSlog("MakePowerManagerServiceReady");
                                Slog.i(TAG, "power manager systemReady");
                                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                traceEnd();
                                if (this.mSecrecyService != null) {
                                }
                                if (this.mFingerprintService != null) {
                                }
                                if (this.mFaceService != null) {
                                }
                                if (this.mBiometricsService != null) {
                                }
                                traceBeginAndSlog("MakePackageManagerServiceReady");
                                Slog.i(TAG, "Package systemReady");
                                this.mPackageManagerService.systemReady();
                                traceEnd();
                                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                                Slog.i(TAG, "DisplayManager systemReady");
                                this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                                traceEnd();
                                this.mSystemServiceManager.setSafeMode(false);
                                networkManagementF = networkManagement;
                                networkStatsF = networkStats;
                                networkPolicyF = networkPolicy;
                                connectivityF = connectivity;
                                networkScoreF = networkScore;
                                locationF = location;
                                countryDetectorF = countryDetector;
                                networkTimeUpdaterF = networkTimeUpdater;
                                commonTimeMgmtServiceF = commonTimeMgmtService;
                                inputManagerF = inputManager;
                                telephonyRegistryF = telephonyRegistry;
                                mediaRouterF = mediaRouter;
                                mmsServiceF = mmsService;
                                windowManagerF = wm;
                                multimediaServiceF = multimediaService;
                                oppoRoundCornerServiceF = oppoRoundCornerService;
                                usageServiceF = usageService;
                                mOppoService = oppoService;
                                Slog.i(TAG, "Ams systemReady");
                                this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
                            }
                            traceEnd();
                            traceBeginAndSlog("StartTwilightService");
                            this.mSystemServiceManager.startService(TwilightService.class);
                            traceEnd();
                            if (NightDisplayController.isAvailable(context)) {
                            }
                            traceBeginAndSlog("StartJobScheduler");
                            this.mSystemServiceManager.startService(JobSchedulerService.class);
                            traceEnd();
                            traceBeginAndSlog("StartSoundTrigger");
                            this.mSystemServiceManager.startService(SoundTriggerService.class);
                            traceEnd();
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartDiskStatsService");
                            ServiceManager.addService("diskstats", new DiskStatsService(context));
                            traceEnd();
                            Slog.i(TAG, "+OppoMultimediaService");
                            oppoMultimediaService = new OppoMultimediaService(context);
                            ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                            Slog.i(TAG, "-OppoMultimediaService");
                            multimediaService = oppoMultimediaService;
                            traceBeginAndSlog("CabcService");
                            Slog.i(TAG, "Cabc Service");
                            cabcService = new CabcService(context);
                            ServiceManager.addService("cabc", cabcService);
                            cabcService2 = cabcService;
                            Trace.traceEnd(524288);
                            traceBeginAndSlog("OppoRoundCornerService");
                            oppoRoundCornerService = null;
                            if (SystemProperties.getInt("persist.sys.enable.rc", 0) == 1) {
                            }
                            traceBeginAndSlog("StartHypnusService");
                            Slog.i(TAG, "Hypnus Service");
                            hypnusService = new HypnusService(context);
                            ServiceManager.addService("hypnus", hypnusService);
                            hypnusService2 = hypnusService;
                            traceEnd();
                            Slog.i(TAG, "Oppo Customize Service");
                            ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                            Slog.i(TAG, "Oppo Service");
                            oppoService2 = new OppoService(context);
                            ServiceManager.addService("OPPO", oppoService2);
                            oppoService = oppoService2;
                            Slog.i(TAG, "OppoAutoInstallService");
                            oppoAutoInstallService = new OppoAutoInstallService(context);
                            ServiceManager.addService("oppoautoinstall", oppoAutoInstallService);
                            oppoAutoInstallService2 = oppoAutoInstallService;
                            Slog.i(TAG, "OPPO Usage Service");
                            oppoUsageService = new OppoUsageService(context);
                            ServiceManager.addService("usage", oppoUsageService);
                            usageService = oppoUsageService;
                            if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                            }
                            if (this.mOnlyCore) {
                            }
                            if (this.mOnlyCore ? context.getResources().getBoolean(17956963) : false) {
                            }
                            traceBeginAndSlog("StartNetworkTimeUpdateService");
                            networkTimeUpdateService = new NetworkTimeUpdateService(context);
                            ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                            networkTimeUpdater = networkTimeUpdateService;
                            traceEnd();
                            traceBeginAndSlog("StartCommonTimeManagementService");
                            commonTimeManagementService = new CommonTimeManagementService(context);
                            ServiceManager.addService("commontime_management", commonTimeManagementService);
                            commonTimeMgmtService = commonTimeManagementService;
                            traceEnd();
                            if (disableNetwork) {
                            }
                            traceBeginAndSlog("StartEmergencyAffordanceService");
                            this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                            traceEnd();
                            if (disableNonCoreServices) {
                            }
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("AddCoverageService");
                            ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                            traceEnd();
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
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartShortcutServiceLifecycle");
                            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                            traceEnd();
                            traceBeginAndSlog("StartLauncherAppsService");
                            this.mSystemServiceManager.startService(LauncherAppsService.class);
                            traceEnd();
                            Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                            this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                            Slog.i(TAG, "Secrecy Service");
                            this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                            traceBeginAndSlog("StartMediaProjectionManager");
                            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                            traceEnd();
                            if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                            }
                            if (disableCameraService) {
                            }
                            traceBeginAndSlog("StartJitCompilation");
                            VMRuntime.getRuntime().startJitCompilation();
                            traceEnd();
                            traceBeginAndSlog("StartMmsService");
                            mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                            traceEnd();
                            if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                            }
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
                            if (enableWigig) {
                            }
                            Slog.i(TAG, "wms systemReady");
                            wm.systemReady();
                            traceEnd();
                            config = wm.computeNewConfiguration(0);
                            metrics = new DisplayMetrics();
                            ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                            context.getResources().updateConfiguration(config, metrics);
                            systemTheme = context.getTheme();
                            if (systemTheme.getChangingConfigurations() != 0) {
                            }
                            traceBeginAndSlog("MakePowerManagerServiceReady");
                            Slog.i(TAG, "power manager systemReady");
                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                            traceEnd();
                            if (this.mSecrecyService != null) {
                            }
                            if (this.mFingerprintService != null) {
                            }
                            if (this.mFaceService != null) {
                            }
                            if (this.mBiometricsService != null) {
                            }
                            traceBeginAndSlog("MakePackageManagerServiceReady");
                            Slog.i(TAG, "Package systemReady");
                            this.mPackageManagerService.systemReady();
                            traceEnd();
                            traceBeginAndSlog("MakeDisplayManagerServiceReady");
                            Slog.i(TAG, "DisplayManager systemReady");
                            this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                            traceEnd();
                            this.mSystemServiceManager.setSafeMode(false);
                            networkManagementF = networkManagement;
                            networkStatsF = networkStats;
                            networkPolicyF = networkPolicy;
                            connectivityF = connectivity;
                            networkScoreF = networkScore;
                            locationF = location;
                            countryDetectorF = countryDetector;
                            networkTimeUpdaterF = networkTimeUpdater;
                            commonTimeMgmtServiceF = commonTimeMgmtService;
                            inputManagerF = inputManager;
                            telephonyRegistryF = telephonyRegistry;
                            mediaRouterF = mediaRouter;
                            mmsServiceF = mmsService;
                            windowManagerF = wm;
                            multimediaServiceF = multimediaService;
                            oppoRoundCornerServiceF = oppoRoundCornerService;
                            usageServiceF = usageService;
                            mOppoService = oppoService;
                            Slog.i(TAG, "Ams systemReady");
                            this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
                        }
                    } catch (Throwable th23) {
                        e22222222222222222 = th23;
                        Slog.e(TAG, "Failure starting SerialService", e22222222222222222);
                        traceEnd();
                        traceBeginAndSlog("StartHardwarePropertiesManagerService");
                        hardwarePropertiesManagerService = new HardwarePropertiesManagerService(context);
                        ServiceManager.addService("hardware_properties", hardwarePropertiesManagerService);
                        hardwarePropertiesManagerService2 = hardwarePropertiesManagerService;
                        traceEnd();
                        traceBeginAndSlog("StartTwilightService");
                        this.mSystemServiceManager.startService(TwilightService.class);
                        traceEnd();
                        if (NightDisplayController.isAvailable(context)) {
                        }
                        traceBeginAndSlog("StartJobScheduler");
                        this.mSystemServiceManager.startService(JobSchedulerService.class);
                        traceEnd();
                        traceBeginAndSlog("StartSoundTrigger");
                        this.mSystemServiceManager.startService(SoundTriggerService.class);
                        traceEnd();
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartDiskStatsService");
                        ServiceManager.addService("diskstats", new DiskStatsService(context));
                        traceEnd();
                        Slog.i(TAG, "+OppoMultimediaService");
                        oppoMultimediaService = new OppoMultimediaService(context);
                        ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                        Slog.i(TAG, "-OppoMultimediaService");
                        multimediaService = oppoMultimediaService;
                        traceBeginAndSlog("CabcService");
                        Slog.i(TAG, "Cabc Service");
                        cabcService = new CabcService(context);
                        ServiceManager.addService("cabc", cabcService);
                        cabcService2 = cabcService;
                        Trace.traceEnd(524288);
                        traceBeginAndSlog("OppoRoundCornerService");
                        oppoRoundCornerService = null;
                        if (SystemProperties.getInt("persist.sys.enable.rc", 0) == 1) {
                        }
                        traceBeginAndSlog("StartHypnusService");
                        Slog.i(TAG, "Hypnus Service");
                        hypnusService = new HypnusService(context);
                        ServiceManager.addService("hypnus", hypnusService);
                        hypnusService2 = hypnusService;
                        traceEnd();
                        Slog.i(TAG, "Oppo Customize Service");
                        ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                        Slog.i(TAG, "Oppo Service");
                        oppoService2 = new OppoService(context);
                        ServiceManager.addService("OPPO", oppoService2);
                        oppoService = oppoService2;
                        Slog.i(TAG, "OppoAutoInstallService");
                        oppoAutoInstallService = new OppoAutoInstallService(context);
                        ServiceManager.addService("oppoautoinstall", oppoAutoInstallService);
                        oppoAutoInstallService2 = oppoAutoInstallService;
                        Slog.i(TAG, "OPPO Usage Service");
                        oppoUsageService = new OppoUsageService(context);
                        ServiceManager.addService("usage", oppoUsageService);
                        usageService = oppoUsageService;
                        if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                        }
                        if (this.mOnlyCore) {
                        }
                        if (this.mOnlyCore ? context.getResources().getBoolean(17956963) : false) {
                        }
                        traceBeginAndSlog("StartNetworkTimeUpdateService");
                        networkTimeUpdateService = new NetworkTimeUpdateService(context);
                        ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                        networkTimeUpdater = networkTimeUpdateService;
                        traceEnd();
                        traceBeginAndSlog("StartCommonTimeManagementService");
                        commonTimeManagementService = new CommonTimeManagementService(context);
                        ServiceManager.addService("commontime_management", commonTimeManagementService);
                        commonTimeMgmtService = commonTimeManagementService;
                        traceEnd();
                        if (disableNetwork) {
                        }
                        traceBeginAndSlog("StartEmergencyAffordanceService");
                        this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                        traceEnd();
                        if (disableNonCoreServices) {
                        }
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("AddCoverageService");
                        ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                        traceEnd();
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
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartShortcutServiceLifecycle");
                        this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                        traceEnd();
                        traceBeginAndSlog("StartLauncherAppsService");
                        this.mSystemServiceManager.startService(LauncherAppsService.class);
                        traceEnd();
                        Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                        this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                        Slog.i(TAG, "Secrecy Service");
                        this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                        traceBeginAndSlog("StartMediaProjectionManager");
                        this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                        traceEnd();
                        if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                        }
                        if (disableCameraService) {
                        }
                        traceBeginAndSlog("StartJitCompilation");
                        VMRuntime.getRuntime().startJitCompilation();
                        traceEnd();
                        traceBeginAndSlog("StartMmsService");
                        mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                        }
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
                        if (enableWigig) {
                        }
                        Slog.i(TAG, "wms systemReady");
                        wm.systemReady();
                        traceEnd();
                        config = wm.computeNewConfiguration(0);
                        metrics = new DisplayMetrics();
                        ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                        context.getResources().updateConfiguration(config, metrics);
                        systemTheme = context.getTheme();
                        if (systemTheme.getChangingConfigurations() != 0) {
                        }
                        traceBeginAndSlog("MakePowerManagerServiceReady");
                        Slog.i(TAG, "power manager systemReady");
                        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                        traceEnd();
                        if (this.mSecrecyService != null) {
                        }
                        if (this.mFingerprintService != null) {
                        }
                        if (this.mFaceService != null) {
                        }
                        if (this.mBiometricsService != null) {
                        }
                        traceBeginAndSlog("MakePackageManagerServiceReady");
                        Slog.i(TAG, "Package systemReady");
                        this.mPackageManagerService.systemReady();
                        traceEnd();
                        traceBeginAndSlog("MakeDisplayManagerServiceReady");
                        Slog.i(TAG, "DisplayManager systemReady");
                        this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                        traceEnd();
                        this.mSystemServiceManager.setSafeMode(false);
                        networkManagementF = networkManagement;
                        networkStatsF = networkStats;
                        networkPolicyF = networkPolicy;
                        connectivityF = connectivity;
                        networkScoreF = networkScore;
                        locationF = location;
                        countryDetectorF = countryDetector;
                        networkTimeUpdaterF = networkTimeUpdater;
                        commonTimeMgmtServiceF = commonTimeMgmtService;
                        inputManagerF = inputManager;
                        telephonyRegistryF = telephonyRegistry;
                        mediaRouterF = mediaRouter;
                        mmsServiceF = mmsService;
                        windowManagerF = wm;
                        multimediaServiceF = multimediaService;
                        oppoRoundCornerServiceF = oppoRoundCornerService;
                        usageServiceF = usageService;
                        mOppoService = oppoService;
                        Slog.i(TAG, "Ams systemReady");
                        this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
                    }
                    traceEnd();
                }
                traceBeginAndSlog("StartHardwarePropertiesManagerService");
                try {
                    hardwarePropertiesManagerService = new HardwarePropertiesManagerService(context);
                    ServiceManager.addService("hardware_properties", hardwarePropertiesManagerService);
                    hardwarePropertiesManagerService2 = hardwarePropertiesManagerService;
                } catch (Throwable th24) {
                    e22222222222222222 = th24;
                    Slog.e(TAG, "Failure starting HardwarePropertiesManagerService", e22222222222222222);
                    traceEnd();
                    traceBeginAndSlog("StartTwilightService");
                    this.mSystemServiceManager.startService(TwilightService.class);
                    traceEnd();
                    if (NightDisplayController.isAvailable(context)) {
                    }
                    traceBeginAndSlog("StartJobScheduler");
                    this.mSystemServiceManager.startService(JobSchedulerService.class);
                    traceEnd();
                    traceBeginAndSlog("StartSoundTrigger");
                    this.mSystemServiceManager.startService(SoundTriggerService.class);
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartDiskStatsService");
                    ServiceManager.addService("diskstats", new DiskStatsService(context));
                    traceEnd();
                    Slog.i(TAG, "+OppoMultimediaService");
                    oppoMultimediaService = new OppoMultimediaService(context);
                    ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                    Slog.i(TAG, "-OppoMultimediaService");
                    multimediaService = oppoMultimediaService;
                    traceBeginAndSlog("CabcService");
                    Slog.i(TAG, "Cabc Service");
                    cabcService = new CabcService(context);
                    ServiceManager.addService("cabc", cabcService);
                    cabcService2 = cabcService;
                    Trace.traceEnd(524288);
                    traceBeginAndSlog("OppoRoundCornerService");
                    oppoRoundCornerService = null;
                    if (SystemProperties.getInt("persist.sys.enable.rc", 0) == 1) {
                    }
                    traceBeginAndSlog("StartHypnusService");
                    Slog.i(TAG, "Hypnus Service");
                    hypnusService = new HypnusService(context);
                    ServiceManager.addService("hypnus", hypnusService);
                    hypnusService2 = hypnusService;
                    traceEnd();
                    Slog.i(TAG, "Oppo Customize Service");
                    ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                    Slog.i(TAG, "Oppo Service");
                    oppoService2 = new OppoService(context);
                    ServiceManager.addService("OPPO", oppoService2);
                    oppoService = oppoService2;
                    Slog.i(TAG, "OppoAutoInstallService");
                    oppoAutoInstallService = new OppoAutoInstallService(context);
                    ServiceManager.addService("oppoautoinstall", oppoAutoInstallService);
                    oppoAutoInstallService2 = oppoAutoInstallService;
                    Slog.i(TAG, "OPPO Usage Service");
                    oppoUsageService = new OppoUsageService(context);
                    ServiceManager.addService("usage", oppoUsageService);
                    usageService = oppoUsageService;
                    if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                    }
                    if (this.mOnlyCore) {
                    }
                    if (this.mOnlyCore ? context.getResources().getBoolean(17956963) : false) {
                    }
                    traceBeginAndSlog("StartNetworkTimeUpdateService");
                    networkTimeUpdateService = new NetworkTimeUpdateService(context);
                    ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                    networkTimeUpdater = networkTimeUpdateService;
                    traceEnd();
                    traceBeginAndSlog("StartCommonTimeManagementService");
                    commonTimeManagementService = new CommonTimeManagementService(context);
                    ServiceManager.addService("commontime_management", commonTimeManagementService);
                    commonTimeMgmtService = commonTimeManagementService;
                    traceEnd();
                    if (disableNetwork) {
                    }
                    traceBeginAndSlog("StartEmergencyAffordanceService");
                    this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("AddCoverageService");
                    ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                    traceEnd();
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
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartShortcutServiceLifecycle");
                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                    traceEnd();
                    traceBeginAndSlog("StartLauncherAppsService");
                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                    traceEnd();
                    Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                    this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                    Slog.i(TAG, "Secrecy Service");
                    this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                    traceBeginAndSlog("StartMediaProjectionManager");
                    this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                    traceEnd();
                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                    }
                    if (disableCameraService) {
                    }
                    traceBeginAndSlog("StartJitCompilation");
                    VMRuntime.getRuntime().startJitCompilation();
                    traceEnd();
                    traceBeginAndSlog("StartMmsService");
                    mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                    }
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
                    if (enableWigig) {
                    }
                    Slog.i(TAG, "wms systemReady");
                    wm.systemReady();
                    traceEnd();
                    config = wm.computeNewConfiguration(0);
                    metrics = new DisplayMetrics();
                    ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                    context.getResources().updateConfiguration(config, metrics);
                    systemTheme = context.getTheme();
                    if (systemTheme.getChangingConfigurations() != 0) {
                    }
                    traceBeginAndSlog("MakePowerManagerServiceReady");
                    Slog.i(TAG, "power manager systemReady");
                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                    traceEnd();
                    if (this.mSecrecyService != null) {
                    }
                    if (this.mFingerprintService != null) {
                    }
                    if (this.mFaceService != null) {
                    }
                    if (this.mBiometricsService != null) {
                    }
                    traceBeginAndSlog("MakePackageManagerServiceReady");
                    Slog.i(TAG, "Package systemReady");
                    this.mPackageManagerService.systemReady();
                    traceEnd();
                    traceBeginAndSlog("MakeDisplayManagerServiceReady");
                    Slog.i(TAG, "DisplayManager systemReady");
                    this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                    traceEnd();
                    this.mSystemServiceManager.setSafeMode(false);
                    networkManagementF = networkManagement;
                    networkStatsF = networkStats;
                    networkPolicyF = networkPolicy;
                    connectivityF = connectivity;
                    networkScoreF = networkScore;
                    locationF = location;
                    countryDetectorF = countryDetector;
                    networkTimeUpdaterF = networkTimeUpdater;
                    commonTimeMgmtServiceF = commonTimeMgmtService;
                    inputManagerF = inputManager;
                    telephonyRegistryF = telephonyRegistry;
                    mediaRouterF = mediaRouter;
                    mmsServiceF = mmsService;
                    windowManagerF = wm;
                    multimediaServiceF = multimediaService;
                    oppoRoundCornerServiceF = oppoRoundCornerService;
                    usageServiceF = usageService;
                    mOppoService = oppoService;
                    Slog.i(TAG, "Ams systemReady");
                    this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
                }
                traceEnd();
            }
            traceBeginAndSlog("StartTwilightService");
            this.mSystemServiceManager.startService(TwilightService.class);
            traceEnd();
            if (NightDisplayController.isAvailable(context)) {
                traceBeginAndSlog("StartNightDisplay");
                this.mSystemServiceManager.startService(NightDisplayService.class);
                traceEnd();
            }
            traceBeginAndSlog("StartJobScheduler");
            this.mSystemServiceManager.startService(JobSchedulerService.class);
            traceEnd();
            traceBeginAndSlog("StartSoundTrigger");
            this.mSystemServiceManager.startService(SoundTriggerService.class);
            traceEnd();
            if (disableNonCoreServices) {
                if (!disableTrustManager) {
                    traceBeginAndSlog("StartTrustManager");
                    this.mSystemServiceManager.startService(TrustManagerService.class);
                    traceEnd();
                }
                if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                    traceBeginAndSlog("StartBackupManager");
                    this.mSystemServiceManager.startService(BACKUP_MANAGER_SERVICE_CLASS);
                    traceEnd();
                }
                if (this.mPackageManager.hasSystemFeature("android.software.app_widgets") || context.getResources().getBoolean(17956945)) {
                    traceBeginAndSlog("StartAppWidgerService");
                    this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                    traceEnd();
                }
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
            }
            traceBeginAndSlog("StartDiskStatsService");
            try {
                ServiceManager.addService("diskstats", new DiskStatsService(context));
            } catch (Throwable e222222222222222222) {
                reportWtf("starting DiskStats Service", e222222222222222222);
            }
            traceEnd();
            try {
                Slog.i(TAG, "+OppoMultimediaService");
                oppoMultimediaService = new OppoMultimediaService(context);
                ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                Slog.i(TAG, "-OppoMultimediaService");
                multimediaService = oppoMultimediaService;
            } catch (Throwable th25) {
                e222222222222222222 = th25;
                reportWtf("starting OppoMultimediaService", e222222222222222222);
                traceBeginAndSlog("CabcService");
                Slog.i(TAG, "Cabc Service");
                cabcService = new CabcService(context);
                ServiceManager.addService("cabc", cabcService);
                cabcService2 = cabcService;
                Trace.traceEnd(524288);
                traceBeginAndSlog("OppoRoundCornerService");
                oppoRoundCornerService = null;
                if (SystemProperties.getInt("persist.sys.enable.rc", 0) == 1) {
                }
                traceBeginAndSlog("StartHypnusService");
                Slog.i(TAG, "Hypnus Service");
                hypnusService = new HypnusService(context);
                ServiceManager.addService("hypnus", hypnusService);
                hypnusService2 = hypnusService;
                traceEnd();
                Slog.i(TAG, "Oppo Customize Service");
                ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                Slog.i(TAG, "Oppo Service");
                oppoService2 = new OppoService(context);
                ServiceManager.addService("OPPO", oppoService2);
                oppoService = oppoService2;
                Slog.i(TAG, "OppoAutoInstallService");
                oppoAutoInstallService = new OppoAutoInstallService(context);
                ServiceManager.addService("oppoautoinstall", oppoAutoInstallService);
                oppoAutoInstallService2 = oppoAutoInstallService;
                Slog.i(TAG, "OPPO Usage Service");
                oppoUsageService = new OppoUsageService(context);
                ServiceManager.addService("usage", oppoUsageService);
                usageService = oppoUsageService;
                if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                }
                if (this.mOnlyCore) {
                }
                if (this.mOnlyCore ? context.getResources().getBoolean(17956963) : false) {
                }
                traceBeginAndSlog("StartNetworkTimeUpdateService");
                networkTimeUpdateService = new NetworkTimeUpdateService(context);
                ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                networkTimeUpdater = networkTimeUpdateService;
                traceEnd();
                traceBeginAndSlog("StartCommonTimeManagementService");
                commonTimeManagementService = new CommonTimeManagementService(context);
                ServiceManager.addService("commontime_management", commonTimeManagementService);
                commonTimeMgmtService = commonTimeManagementService;
                traceEnd();
                if (disableNetwork) {
                }
                traceBeginAndSlog("StartEmergencyAffordanceService");
                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                traceEnd();
                if (disableNonCoreServices) {
                }
                if (disableNonCoreServices) {
                }
                traceBeginAndSlog("AddCoverageService");
                ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                traceEnd();
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
                if (disableNonCoreServices) {
                }
                traceBeginAndSlog("StartShortcutServiceLifecycle");
                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                traceEnd();
                traceBeginAndSlog("StartLauncherAppsService");
                this.mSystemServiceManager.startService(LauncherAppsService.class);
                traceEnd();
                Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                Slog.i(TAG, "Secrecy Service");
                this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                traceBeginAndSlog("StartMediaProjectionManager");
                this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                traceEnd();
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                }
                if (disableCameraService) {
                }
                traceBeginAndSlog("StartJitCompilation");
                VMRuntime.getRuntime().startJitCompilation();
                traceEnd();
                traceBeginAndSlog("StartMmsService");
                mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                }
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
                if (enableWigig) {
                }
                Slog.i(TAG, "wms systemReady");
                wm.systemReady();
                traceEnd();
                config = wm.computeNewConfiguration(0);
                metrics = new DisplayMetrics();
                ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                context.getResources().updateConfiguration(config, metrics);
                systemTheme = context.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                }
                traceBeginAndSlog("MakePowerManagerServiceReady");
                Slog.i(TAG, "power manager systemReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                traceEnd();
                if (this.mSecrecyService != null) {
                }
                if (this.mFingerprintService != null) {
                }
                if (this.mFaceService != null) {
                }
                if (this.mBiometricsService != null) {
                }
                traceBeginAndSlog("MakePackageManagerServiceReady");
                Slog.i(TAG, "Package systemReady");
                this.mPackageManagerService.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                Slog.i(TAG, "DisplayManager systemReady");
                this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                traceEnd();
                this.mSystemServiceManager.setSafeMode(false);
                networkManagementF = networkManagement;
                networkStatsF = networkStats;
                networkPolicyF = networkPolicy;
                connectivityF = connectivity;
                networkScoreF = networkScore;
                locationF = location;
                countryDetectorF = countryDetector;
                networkTimeUpdaterF = networkTimeUpdater;
                commonTimeMgmtServiceF = commonTimeMgmtService;
                inputManagerF = inputManager;
                telephonyRegistryF = telephonyRegistry;
                mediaRouterF = mediaRouter;
                mmsServiceF = mmsService;
                windowManagerF = wm;
                multimediaServiceF = multimediaService;
                oppoRoundCornerServiceF = oppoRoundCornerService;
                usageServiceF = usageService;
                mOppoService = oppoService;
                Slog.i(TAG, "Ams systemReady");
                this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
            }
            traceBeginAndSlog("CabcService");
            try {
                Slog.i(TAG, "Cabc Service");
                cabcService = new CabcService(context);
                ServiceManager.addService("cabc", cabcService);
                cabcService2 = cabcService;
            } catch (Throwable th26) {
                e222222222222222222 = th26;
                reportWtf("starting Cabc Service", e222222222222222222);
                Trace.traceEnd(524288);
                traceBeginAndSlog("OppoRoundCornerService");
                oppoRoundCornerService = null;
                if (SystemProperties.getInt("persist.sys.enable.rc", 0) == 1) {
                }
                traceBeginAndSlog("StartHypnusService");
                Slog.i(TAG, "Hypnus Service");
                hypnusService = new HypnusService(context);
                ServiceManager.addService("hypnus", hypnusService);
                hypnusService2 = hypnusService;
                traceEnd();
                Slog.i(TAG, "Oppo Customize Service");
                ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                Slog.i(TAG, "Oppo Service");
                oppoService2 = new OppoService(context);
                ServiceManager.addService("OPPO", oppoService2);
                oppoService = oppoService2;
                Slog.i(TAG, "OppoAutoInstallService");
                oppoAutoInstallService = new OppoAutoInstallService(context);
                ServiceManager.addService("oppoautoinstall", oppoAutoInstallService);
                oppoAutoInstallService2 = oppoAutoInstallService;
                Slog.i(TAG, "OPPO Usage Service");
                oppoUsageService = new OppoUsageService(context);
                ServiceManager.addService("usage", oppoUsageService);
                usageService = oppoUsageService;
                if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                }
                if (this.mOnlyCore) {
                }
                if (this.mOnlyCore ? context.getResources().getBoolean(17956963) : false) {
                }
                traceBeginAndSlog("StartNetworkTimeUpdateService");
                networkTimeUpdateService = new NetworkTimeUpdateService(context);
                ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                networkTimeUpdater = networkTimeUpdateService;
                traceEnd();
                traceBeginAndSlog("StartCommonTimeManagementService");
                commonTimeManagementService = new CommonTimeManagementService(context);
                ServiceManager.addService("commontime_management", commonTimeManagementService);
                commonTimeMgmtService = commonTimeManagementService;
                traceEnd();
                if (disableNetwork) {
                }
                traceBeginAndSlog("StartEmergencyAffordanceService");
                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                traceEnd();
                if (disableNonCoreServices) {
                }
                if (disableNonCoreServices) {
                }
                traceBeginAndSlog("AddCoverageService");
                ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                traceEnd();
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
                if (disableNonCoreServices) {
                }
                traceBeginAndSlog("StartShortcutServiceLifecycle");
                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                traceEnd();
                traceBeginAndSlog("StartLauncherAppsService");
                this.mSystemServiceManager.startService(LauncherAppsService.class);
                traceEnd();
                Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                Slog.i(TAG, "Secrecy Service");
                this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                traceBeginAndSlog("StartMediaProjectionManager");
                this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                traceEnd();
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                }
                if (disableCameraService) {
                }
                traceBeginAndSlog("StartJitCompilation");
                VMRuntime.getRuntime().startJitCompilation();
                traceEnd();
                traceBeginAndSlog("StartMmsService");
                mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                }
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
                if (enableWigig) {
                }
                Slog.i(TAG, "wms systemReady");
                wm.systemReady();
                traceEnd();
                config = wm.computeNewConfiguration(0);
                metrics = new DisplayMetrics();
                ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                context.getResources().updateConfiguration(config, metrics);
                systemTheme = context.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                }
                traceBeginAndSlog("MakePowerManagerServiceReady");
                Slog.i(TAG, "power manager systemReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                traceEnd();
                if (this.mSecrecyService != null) {
                }
                if (this.mFingerprintService != null) {
                }
                if (this.mFaceService != null) {
                }
                if (this.mBiometricsService != null) {
                }
                traceBeginAndSlog("MakePackageManagerServiceReady");
                Slog.i(TAG, "Package systemReady");
                this.mPackageManagerService.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                Slog.i(TAG, "DisplayManager systemReady");
                this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                traceEnd();
                this.mSystemServiceManager.setSafeMode(false);
                networkManagementF = networkManagement;
                networkStatsF = networkStats;
                networkPolicyF = networkPolicy;
                connectivityF = connectivity;
                networkScoreF = networkScore;
                locationF = location;
                countryDetectorF = countryDetector;
                networkTimeUpdaterF = networkTimeUpdater;
                commonTimeMgmtServiceF = commonTimeMgmtService;
                inputManagerF = inputManager;
                telephonyRegistryF = telephonyRegistry;
                mediaRouterF = mediaRouter;
                mmsServiceF = mmsService;
                windowManagerF = wm;
                multimediaServiceF = multimediaService;
                oppoRoundCornerServiceF = oppoRoundCornerService;
                usageServiceF = usageService;
                mOppoService = oppoService;
                Slog.i(TAG, "Ams systemReady");
                this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
            }
            Trace.traceEnd(524288);
            traceBeginAndSlog("OppoRoundCornerService");
            oppoRoundCornerService = null;
            try {
                if (SystemProperties.getInt("persist.sys.enable.rc", 0) == 1) {
                    Slog.i(TAG, "OppoRoundCornerService Service support");
                    OppoRoundCornerService oppoRoundCornerService2 = new OppoRoundCornerService(context);
                    try {
                        ServiceManager.addService("OppoRoundConerService", oppoRoundCornerService2);
                        oppoRoundCornerService = oppoRoundCornerService2;
                    } catch (Throwable th27) {
                        e222222222222222222 = th27;
                        oppoRoundCornerService = oppoRoundCornerService2;
                        reportWtf("starting OppoRoundCornerService Service", e222222222222222222);
                        traceBeginAndSlog("StartHypnusService");
                        Slog.i(TAG, "Hypnus Service");
                        hypnusService = new HypnusService(context);
                        ServiceManager.addService("hypnus", hypnusService);
                        hypnusService2 = hypnusService;
                        traceEnd();
                        Slog.i(TAG, "Oppo Customize Service");
                        ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                        Slog.i(TAG, "Oppo Service");
                        oppoService2 = new OppoService(context);
                        ServiceManager.addService("OPPO", oppoService2);
                        oppoService = oppoService2;
                        Slog.i(TAG, "OppoAutoInstallService");
                        oppoAutoInstallService = new OppoAutoInstallService(context);
                        ServiceManager.addService("oppoautoinstall", oppoAutoInstallService);
                        oppoAutoInstallService2 = oppoAutoInstallService;
                        Slog.i(TAG, "OPPO Usage Service");
                        oppoUsageService = new OppoUsageService(context);
                        ServiceManager.addService("usage", oppoUsageService);
                        usageService = oppoUsageService;
                        if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                        }
                        if (this.mOnlyCore) {
                        }
                        if (this.mOnlyCore ? context.getResources().getBoolean(17956963) : false) {
                        }
                        traceBeginAndSlog("StartNetworkTimeUpdateService");
                        networkTimeUpdateService = new NetworkTimeUpdateService(context);
                        ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                        networkTimeUpdater = networkTimeUpdateService;
                        traceEnd();
                        traceBeginAndSlog("StartCommonTimeManagementService");
                        commonTimeManagementService = new CommonTimeManagementService(context);
                        ServiceManager.addService("commontime_management", commonTimeManagementService);
                        commonTimeMgmtService = commonTimeManagementService;
                        traceEnd();
                        if (disableNetwork) {
                        }
                        traceBeginAndSlog("StartEmergencyAffordanceService");
                        this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                        traceEnd();
                        if (disableNonCoreServices) {
                        }
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("AddCoverageService");
                        ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                        traceEnd();
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
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartShortcutServiceLifecycle");
                        this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                        traceEnd();
                        traceBeginAndSlog("StartLauncherAppsService");
                        this.mSystemServiceManager.startService(LauncherAppsService.class);
                        traceEnd();
                        Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                        this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                        Slog.i(TAG, "Secrecy Service");
                        this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                        traceBeginAndSlog("StartMediaProjectionManager");
                        this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                        traceEnd();
                        if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                        }
                        if (disableCameraService) {
                        }
                        traceBeginAndSlog("StartJitCompilation");
                        VMRuntime.getRuntime().startJitCompilation();
                        traceEnd();
                        traceBeginAndSlog("StartMmsService");
                        mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                        }
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
                        if (enableWigig) {
                        }
                        Slog.i(TAG, "wms systemReady");
                        wm.systemReady();
                        traceEnd();
                        config = wm.computeNewConfiguration(0);
                        metrics = new DisplayMetrics();
                        ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                        context.getResources().updateConfiguration(config, metrics);
                        systemTheme = context.getTheme();
                        if (systemTheme.getChangingConfigurations() != 0) {
                        }
                        traceBeginAndSlog("MakePowerManagerServiceReady");
                        Slog.i(TAG, "power manager systemReady");
                        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                        traceEnd();
                        if (this.mSecrecyService != null) {
                        }
                        if (this.mFingerprintService != null) {
                        }
                        if (this.mFaceService != null) {
                        }
                        if (this.mBiometricsService != null) {
                        }
                        traceBeginAndSlog("MakePackageManagerServiceReady");
                        Slog.i(TAG, "Package systemReady");
                        this.mPackageManagerService.systemReady();
                        traceEnd();
                        traceBeginAndSlog("MakeDisplayManagerServiceReady");
                        Slog.i(TAG, "DisplayManager systemReady");
                        this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                        traceEnd();
                        this.mSystemServiceManager.setSafeMode(false);
                        networkManagementF = networkManagement;
                        networkStatsF = networkStats;
                        networkPolicyF = networkPolicy;
                        connectivityF = connectivity;
                        networkScoreF = networkScore;
                        locationF = location;
                        countryDetectorF = countryDetector;
                        networkTimeUpdaterF = networkTimeUpdater;
                        commonTimeMgmtServiceF = commonTimeMgmtService;
                        inputManagerF = inputManager;
                        telephonyRegistryF = telephonyRegistry;
                        mediaRouterF = mediaRouter;
                        mmsServiceF = mmsService;
                        windowManagerF = wm;
                        multimediaServiceF = multimediaService;
                        oppoRoundCornerServiceF = oppoRoundCornerService;
                        usageServiceF = usageService;
                        mOppoService = oppoService;
                        Slog.i(TAG, "Ams systemReady");
                        this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
                    }
                }
            } catch (Throwable th28) {
                e222222222222222222 = th28;
                reportWtf("starting OppoRoundCornerService Service", e222222222222222222);
                traceBeginAndSlog("StartHypnusService");
                Slog.i(TAG, "Hypnus Service");
                hypnusService = new HypnusService(context);
                ServiceManager.addService("hypnus", hypnusService);
                hypnusService2 = hypnusService;
                traceEnd();
                Slog.i(TAG, "Oppo Customize Service");
                ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                Slog.i(TAG, "Oppo Service");
                oppoService2 = new OppoService(context);
                ServiceManager.addService("OPPO", oppoService2);
                oppoService = oppoService2;
                Slog.i(TAG, "OppoAutoInstallService");
                oppoAutoInstallService = new OppoAutoInstallService(context);
                ServiceManager.addService("oppoautoinstall", oppoAutoInstallService);
                oppoAutoInstallService2 = oppoAutoInstallService;
                Slog.i(TAG, "OPPO Usage Service");
                oppoUsageService = new OppoUsageService(context);
                ServiceManager.addService("usage", oppoUsageService);
                usageService = oppoUsageService;
                if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                }
                if (this.mOnlyCore) {
                }
                if (this.mOnlyCore ? context.getResources().getBoolean(17956963) : false) {
                }
                traceBeginAndSlog("StartNetworkTimeUpdateService");
                networkTimeUpdateService = new NetworkTimeUpdateService(context);
                ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                networkTimeUpdater = networkTimeUpdateService;
                traceEnd();
                traceBeginAndSlog("StartCommonTimeManagementService");
                commonTimeManagementService = new CommonTimeManagementService(context);
                ServiceManager.addService("commontime_management", commonTimeManagementService);
                commonTimeMgmtService = commonTimeManagementService;
                traceEnd();
                if (disableNetwork) {
                }
                traceBeginAndSlog("StartEmergencyAffordanceService");
                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                traceEnd();
                if (disableNonCoreServices) {
                }
                if (disableNonCoreServices) {
                }
                traceBeginAndSlog("AddCoverageService");
                ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                traceEnd();
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
                if (disableNonCoreServices) {
                }
                traceBeginAndSlog("StartShortcutServiceLifecycle");
                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                traceEnd();
                traceBeginAndSlog("StartLauncherAppsService");
                this.mSystemServiceManager.startService(LauncherAppsService.class);
                traceEnd();
                Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                Slog.i(TAG, "Secrecy Service");
                this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                traceBeginAndSlog("StartMediaProjectionManager");
                this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                traceEnd();
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                }
                if (disableCameraService) {
                }
                traceBeginAndSlog("StartJitCompilation");
                VMRuntime.getRuntime().startJitCompilation();
                traceEnd();
                traceBeginAndSlog("StartMmsService");
                mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                }
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
                if (enableWigig) {
                }
                Slog.i(TAG, "wms systemReady");
                wm.systemReady();
                traceEnd();
                config = wm.computeNewConfiguration(0);
                metrics = new DisplayMetrics();
                ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                context.getResources().updateConfiguration(config, metrics);
                systemTheme = context.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                }
                traceBeginAndSlog("MakePowerManagerServiceReady");
                Slog.i(TAG, "power manager systemReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                traceEnd();
                if (this.mSecrecyService != null) {
                }
                if (this.mFingerprintService != null) {
                }
                if (this.mFaceService != null) {
                }
                if (this.mBiometricsService != null) {
                }
                traceBeginAndSlog("MakePackageManagerServiceReady");
                Slog.i(TAG, "Package systemReady");
                this.mPackageManagerService.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                Slog.i(TAG, "DisplayManager systemReady");
                this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                traceEnd();
                this.mSystemServiceManager.setSafeMode(false);
                networkManagementF = networkManagement;
                networkStatsF = networkStats;
                networkPolicyF = networkPolicy;
                connectivityF = connectivity;
                networkScoreF = networkScore;
                locationF = location;
                countryDetectorF = countryDetector;
                networkTimeUpdaterF = networkTimeUpdater;
                commonTimeMgmtServiceF = commonTimeMgmtService;
                inputManagerF = inputManager;
                telephonyRegistryF = telephonyRegistry;
                mediaRouterF = mediaRouter;
                mmsServiceF = mmsService;
                windowManagerF = wm;
                multimediaServiceF = multimediaService;
                oppoRoundCornerServiceF = oppoRoundCornerService;
                usageServiceF = usageService;
                mOppoService = oppoService;
                Slog.i(TAG, "Ams systemReady");
                this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
            }
            traceBeginAndSlog("StartHypnusService");
            try {
                Slog.i(TAG, "Hypnus Service");
                hypnusService = new HypnusService(context);
                ServiceManager.addService("hypnus", hypnusService);
                hypnusService2 = hypnusService;
            } catch (Throwable th29) {
                e222222222222222222 = th29;
                reportWtf("starting Hypnus Service", e222222222222222222);
                traceEnd();
                Slog.i(TAG, "Oppo Customize Service");
                ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                Slog.i(TAG, "Oppo Service");
                oppoService2 = new OppoService(context);
                ServiceManager.addService("OPPO", oppoService2);
                oppoService = oppoService2;
                Slog.i(TAG, "OppoAutoInstallService");
                oppoAutoInstallService = new OppoAutoInstallService(context);
                ServiceManager.addService("oppoautoinstall", oppoAutoInstallService);
                oppoAutoInstallService2 = oppoAutoInstallService;
                Slog.i(TAG, "OPPO Usage Service");
                oppoUsageService = new OppoUsageService(context);
                ServiceManager.addService("usage", oppoUsageService);
                usageService = oppoUsageService;
                if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                }
                if (this.mOnlyCore) {
                }
                if (this.mOnlyCore ? context.getResources().getBoolean(17956963) : false) {
                }
                traceBeginAndSlog("StartNetworkTimeUpdateService");
                networkTimeUpdateService = new NetworkTimeUpdateService(context);
                ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                networkTimeUpdater = networkTimeUpdateService;
                traceEnd();
                traceBeginAndSlog("StartCommonTimeManagementService");
                commonTimeManagementService = new CommonTimeManagementService(context);
                ServiceManager.addService("commontime_management", commonTimeManagementService);
                commonTimeMgmtService = commonTimeManagementService;
                traceEnd();
                if (disableNetwork) {
                }
                traceBeginAndSlog("StartEmergencyAffordanceService");
                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                traceEnd();
                if (disableNonCoreServices) {
                }
                if (disableNonCoreServices) {
                }
                traceBeginAndSlog("AddCoverageService");
                ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                traceEnd();
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
                if (disableNonCoreServices) {
                }
                traceBeginAndSlog("StartShortcutServiceLifecycle");
                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                traceEnd();
                traceBeginAndSlog("StartLauncherAppsService");
                this.mSystemServiceManager.startService(LauncherAppsService.class);
                traceEnd();
                Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                Slog.i(TAG, "Secrecy Service");
                this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                traceBeginAndSlog("StartMediaProjectionManager");
                this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                traceEnd();
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                }
                if (disableCameraService) {
                }
                traceBeginAndSlog("StartJitCompilation");
                VMRuntime.getRuntime().startJitCompilation();
                traceEnd();
                traceBeginAndSlog("StartMmsService");
                mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                }
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
                if (enableWigig) {
                }
                Slog.i(TAG, "wms systemReady");
                wm.systemReady();
                traceEnd();
                config = wm.computeNewConfiguration(0);
                metrics = new DisplayMetrics();
                ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                context.getResources().updateConfiguration(config, metrics);
                systemTheme = context.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                }
                traceBeginAndSlog("MakePowerManagerServiceReady");
                Slog.i(TAG, "power manager systemReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                traceEnd();
                if (this.mSecrecyService != null) {
                }
                if (this.mFingerprintService != null) {
                }
                if (this.mFaceService != null) {
                }
                if (this.mBiometricsService != null) {
                }
                traceBeginAndSlog("MakePackageManagerServiceReady");
                Slog.i(TAG, "Package systemReady");
                this.mPackageManagerService.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                Slog.i(TAG, "DisplayManager systemReady");
                this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                traceEnd();
                this.mSystemServiceManager.setSafeMode(false);
                networkManagementF = networkManagement;
                networkStatsF = networkStats;
                networkPolicyF = networkPolicy;
                connectivityF = connectivity;
                networkScoreF = networkScore;
                locationF = location;
                countryDetectorF = countryDetector;
                networkTimeUpdaterF = networkTimeUpdater;
                commonTimeMgmtServiceF = commonTimeMgmtService;
                inputManagerF = inputManager;
                telephonyRegistryF = telephonyRegistry;
                mediaRouterF = mediaRouter;
                mmsServiceF = mmsService;
                windowManagerF = wm;
                multimediaServiceF = multimediaService;
                oppoRoundCornerServiceF = oppoRoundCornerService;
                usageServiceF = usageService;
                mOppoService = oppoService;
                Slog.i(TAG, "Ams systemReady");
                this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
            }
            traceEnd();
            try {
                Slog.i(TAG, "Oppo Customize Service");
                ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
            } catch (Throwable e2222222222222222222) {
                Slog.e(TAG, "Failure starting Oppo Customize Service", e2222222222222222222);
            }
            try {
                Slog.i(TAG, "Oppo Service");
                oppoService2 = new OppoService(context);
                try {
                    ServiceManager.addService("OPPO", oppoService2);
                    oppoService = oppoService2;
                } catch (Throwable th30) {
                    e2222222222222222222 = th30;
                    oppoService = oppoService2;
                    Slog.e(TAG, "Failure starting Oppo Service", e2222222222222222222);
                    Slog.i(TAG, "OppoAutoInstallService");
                    oppoAutoInstallService = new OppoAutoInstallService(context);
                    ServiceManager.addService("oppoautoinstall", oppoAutoInstallService);
                    oppoAutoInstallService2 = oppoAutoInstallService;
                    Slog.i(TAG, "OPPO Usage Service");
                    oppoUsageService = new OppoUsageService(context);
                    ServiceManager.addService("usage", oppoUsageService);
                    usageService = oppoUsageService;
                    if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                    }
                    if (this.mOnlyCore) {
                    }
                    if (this.mOnlyCore ? context.getResources().getBoolean(17956963) : false) {
                    }
                    traceBeginAndSlog("StartNetworkTimeUpdateService");
                    networkTimeUpdateService = new NetworkTimeUpdateService(context);
                    ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                    networkTimeUpdater = networkTimeUpdateService;
                    traceEnd();
                    traceBeginAndSlog("StartCommonTimeManagementService");
                    commonTimeManagementService = new CommonTimeManagementService(context);
                    ServiceManager.addService("commontime_management", commonTimeManagementService);
                    commonTimeMgmtService = commonTimeManagementService;
                    traceEnd();
                    if (disableNetwork) {
                    }
                    traceBeginAndSlog("StartEmergencyAffordanceService");
                    this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                    traceEnd();
                    if (disableNonCoreServices) {
                    }
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("AddCoverageService");
                    ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                    traceEnd();
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
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartShortcutServiceLifecycle");
                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                    traceEnd();
                    traceBeginAndSlog("StartLauncherAppsService");
                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                    traceEnd();
                    Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                    this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                    Slog.i(TAG, "Secrecy Service");
                    this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                    traceBeginAndSlog("StartMediaProjectionManager");
                    this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                    traceEnd();
                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                    }
                    if (disableCameraService) {
                    }
                    traceBeginAndSlog("StartJitCompilation");
                    VMRuntime.getRuntime().startJitCompilation();
                    traceEnd();
                    traceBeginAndSlog("StartMmsService");
                    mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                    }
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
                    if (enableWigig) {
                    }
                    Slog.i(TAG, "wms systemReady");
                    wm.systemReady();
                    traceEnd();
                    config = wm.computeNewConfiguration(0);
                    metrics = new DisplayMetrics();
                    ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                    context.getResources().updateConfiguration(config, metrics);
                    systemTheme = context.getTheme();
                    if (systemTheme.getChangingConfigurations() != 0) {
                    }
                    traceBeginAndSlog("MakePowerManagerServiceReady");
                    Slog.i(TAG, "power manager systemReady");
                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                    traceEnd();
                    if (this.mSecrecyService != null) {
                    }
                    if (this.mFingerprintService != null) {
                    }
                    if (this.mFaceService != null) {
                    }
                    if (this.mBiometricsService != null) {
                    }
                    traceBeginAndSlog("MakePackageManagerServiceReady");
                    Slog.i(TAG, "Package systemReady");
                    this.mPackageManagerService.systemReady();
                    traceEnd();
                    traceBeginAndSlog("MakeDisplayManagerServiceReady");
                    Slog.i(TAG, "DisplayManager systemReady");
                    this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                    traceEnd();
                    this.mSystemServiceManager.setSafeMode(false);
                    networkManagementF = networkManagement;
                    networkStatsF = networkStats;
                    networkPolicyF = networkPolicy;
                    connectivityF = connectivity;
                    networkScoreF = networkScore;
                    locationF = location;
                    countryDetectorF = countryDetector;
                    networkTimeUpdaterF = networkTimeUpdater;
                    commonTimeMgmtServiceF = commonTimeMgmtService;
                    inputManagerF = inputManager;
                    telephonyRegistryF = telephonyRegistry;
                    mediaRouterF = mediaRouter;
                    mmsServiceF = mmsService;
                    windowManagerF = wm;
                    multimediaServiceF = multimediaService;
                    oppoRoundCornerServiceF = oppoRoundCornerService;
                    usageServiceF = usageService;
                    mOppoService = oppoService;
                    Slog.i(TAG, "Ams systemReady");
                    this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
                }
            } catch (Throwable th31) {
                e2222222222222222222 = th31;
                Slog.e(TAG, "Failure starting Oppo Service", e2222222222222222222);
                Slog.i(TAG, "OppoAutoInstallService");
                oppoAutoInstallService = new OppoAutoInstallService(context);
                ServiceManager.addService("oppoautoinstall", oppoAutoInstallService);
                oppoAutoInstallService2 = oppoAutoInstallService;
                Slog.i(TAG, "OPPO Usage Service");
                oppoUsageService = new OppoUsageService(context);
                ServiceManager.addService("usage", oppoUsageService);
                usageService = oppoUsageService;
                if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                }
                if (this.mOnlyCore) {
                }
                if (this.mOnlyCore ? context.getResources().getBoolean(17956963) : false) {
                }
                traceBeginAndSlog("StartNetworkTimeUpdateService");
                networkTimeUpdateService = new NetworkTimeUpdateService(context);
                ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                networkTimeUpdater = networkTimeUpdateService;
                traceEnd();
                traceBeginAndSlog("StartCommonTimeManagementService");
                commonTimeManagementService = new CommonTimeManagementService(context);
                ServiceManager.addService("commontime_management", commonTimeManagementService);
                commonTimeMgmtService = commonTimeManagementService;
                traceEnd();
                if (disableNetwork) {
                }
                traceBeginAndSlog("StartEmergencyAffordanceService");
                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                traceEnd();
                if (disableNonCoreServices) {
                }
                if (disableNonCoreServices) {
                }
                traceBeginAndSlog("AddCoverageService");
                ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                traceEnd();
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
                if (disableNonCoreServices) {
                }
                traceBeginAndSlog("StartShortcutServiceLifecycle");
                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                traceEnd();
                traceBeginAndSlog("StartLauncherAppsService");
                this.mSystemServiceManager.startService(LauncherAppsService.class);
                traceEnd();
                Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                Slog.i(TAG, "Secrecy Service");
                this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                traceBeginAndSlog("StartMediaProjectionManager");
                this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                traceEnd();
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                }
                if (disableCameraService) {
                }
                traceBeginAndSlog("StartJitCompilation");
                VMRuntime.getRuntime().startJitCompilation();
                traceEnd();
                traceBeginAndSlog("StartMmsService");
                mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                }
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
                if (enableWigig) {
                }
                Slog.i(TAG, "wms systemReady");
                wm.systemReady();
                traceEnd();
                config = wm.computeNewConfiguration(0);
                metrics = new DisplayMetrics();
                ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                context.getResources().updateConfiguration(config, metrics);
                systemTheme = context.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                }
                traceBeginAndSlog("MakePowerManagerServiceReady");
                Slog.i(TAG, "power manager systemReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                traceEnd();
                if (this.mSecrecyService != null) {
                }
                if (this.mFingerprintService != null) {
                }
                if (this.mFaceService != null) {
                }
                if (this.mBiometricsService != null) {
                }
                traceBeginAndSlog("MakePackageManagerServiceReady");
                Slog.i(TAG, "Package systemReady");
                this.mPackageManagerService.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                Slog.i(TAG, "DisplayManager systemReady");
                this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                traceEnd();
                this.mSystemServiceManager.setSafeMode(false);
                networkManagementF = networkManagement;
                networkStatsF = networkStats;
                networkPolicyF = networkPolicy;
                connectivityF = connectivity;
                networkScoreF = networkScore;
                locationF = location;
                countryDetectorF = countryDetector;
                networkTimeUpdaterF = networkTimeUpdater;
                commonTimeMgmtServiceF = commonTimeMgmtService;
                inputManagerF = inputManager;
                telephonyRegistryF = telephonyRegistry;
                mediaRouterF = mediaRouter;
                mmsServiceF = mmsService;
                windowManagerF = wm;
                multimediaServiceF = multimediaService;
                oppoRoundCornerServiceF = oppoRoundCornerService;
                usageServiceF = usageService;
                mOppoService = oppoService;
                Slog.i(TAG, "Ams systemReady");
                this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
            }
            try {
                Slog.i(TAG, "OppoAutoInstallService");
                oppoAutoInstallService = new OppoAutoInstallService(context);
                ServiceManager.addService("oppoautoinstall", oppoAutoInstallService);
                oppoAutoInstallService2 = oppoAutoInstallService;
            } catch (Throwable th32) {
                e2222222222222222222 = th32;
                Slog.e(TAG, "Start OppoAutoInstallService failed for:", e2222222222222222222);
                Slog.i(TAG, "OPPO Usage Service");
                oppoUsageService = new OppoUsageService(context);
                ServiceManager.addService("usage", oppoUsageService);
                usageService = oppoUsageService;
                if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                }
                if (this.mOnlyCore) {
                }
                if (this.mOnlyCore ? context.getResources().getBoolean(17956963) : false) {
                }
                traceBeginAndSlog("StartNetworkTimeUpdateService");
                networkTimeUpdateService = new NetworkTimeUpdateService(context);
                ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                networkTimeUpdater = networkTimeUpdateService;
                traceEnd();
                traceBeginAndSlog("StartCommonTimeManagementService");
                commonTimeManagementService = new CommonTimeManagementService(context);
                ServiceManager.addService("commontime_management", commonTimeManagementService);
                commonTimeMgmtService = commonTimeManagementService;
                traceEnd();
                if (disableNetwork) {
                }
                traceBeginAndSlog("StartEmergencyAffordanceService");
                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                traceEnd();
                if (disableNonCoreServices) {
                }
                if (disableNonCoreServices) {
                }
                traceBeginAndSlog("AddCoverageService");
                ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                traceEnd();
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
                if (disableNonCoreServices) {
                }
                traceBeginAndSlog("StartShortcutServiceLifecycle");
                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                traceEnd();
                traceBeginAndSlog("StartLauncherAppsService");
                this.mSystemServiceManager.startService(LauncherAppsService.class);
                traceEnd();
                Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                Slog.i(TAG, "Secrecy Service");
                this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                traceBeginAndSlog("StartMediaProjectionManager");
                this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                traceEnd();
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                }
                if (disableCameraService) {
                }
                traceBeginAndSlog("StartJitCompilation");
                VMRuntime.getRuntime().startJitCompilation();
                traceEnd();
                traceBeginAndSlog("StartMmsService");
                mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                }
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
                if (enableWigig) {
                }
                Slog.i(TAG, "wms systemReady");
                wm.systemReady();
                traceEnd();
                config = wm.computeNewConfiguration(0);
                metrics = new DisplayMetrics();
                ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                context.getResources().updateConfiguration(config, metrics);
                systemTheme = context.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                }
                traceBeginAndSlog("MakePowerManagerServiceReady");
                Slog.i(TAG, "power manager systemReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                traceEnd();
                if (this.mSecrecyService != null) {
                }
                if (this.mFingerprintService != null) {
                }
                if (this.mFaceService != null) {
                }
                if (this.mBiometricsService != null) {
                }
                traceBeginAndSlog("MakePackageManagerServiceReady");
                Slog.i(TAG, "Package systemReady");
                this.mPackageManagerService.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                Slog.i(TAG, "DisplayManager systemReady");
                this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                traceEnd();
                this.mSystemServiceManager.setSafeMode(false);
                networkManagementF = networkManagement;
                networkStatsF = networkStats;
                networkPolicyF = networkPolicy;
                connectivityF = connectivity;
                networkScoreF = networkScore;
                locationF = location;
                countryDetectorF = countryDetector;
                networkTimeUpdaterF = networkTimeUpdater;
                commonTimeMgmtServiceF = commonTimeMgmtService;
                inputManagerF = inputManager;
                telephonyRegistryF = telephonyRegistry;
                mediaRouterF = mediaRouter;
                mmsServiceF = mmsService;
                windowManagerF = wm;
                multimediaServiceF = multimediaService;
                oppoRoundCornerServiceF = oppoRoundCornerService;
                usageServiceF = usageService;
                mOppoService = oppoService;
                Slog.i(TAG, "Ams systemReady");
                this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
            }
            try {
                Slog.i(TAG, "OPPO Usage Service");
                oppoUsageService = new OppoUsageService(context);
                ServiceManager.addService("usage", oppoUsageService);
                usageService = oppoUsageService;
            } catch (Throwable th33) {
                e2222222222222222222 = th33;
                Slog.e(TAG, "Start OppoUsageService failed for:", e2222222222222222222);
                if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                }
                if (this.mOnlyCore) {
                }
                if (this.mOnlyCore ? context.getResources().getBoolean(17956963) : false) {
                }
                traceBeginAndSlog("StartNetworkTimeUpdateService");
                networkTimeUpdateService = new NetworkTimeUpdateService(context);
                ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                networkTimeUpdater = networkTimeUpdateService;
                traceEnd();
                traceBeginAndSlog("StartCommonTimeManagementService");
                commonTimeManagementService = new CommonTimeManagementService(context);
                ServiceManager.addService("commontime_management", commonTimeManagementService);
                commonTimeMgmtService = commonTimeManagementService;
                traceEnd();
                if (disableNetwork) {
                }
                traceBeginAndSlog("StartEmergencyAffordanceService");
                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                traceEnd();
                if (disableNonCoreServices) {
                }
                if (disableNonCoreServices) {
                }
                traceBeginAndSlog("AddCoverageService");
                ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                traceEnd();
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
                if (disableNonCoreServices) {
                }
                traceBeginAndSlog("StartShortcutServiceLifecycle");
                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                traceEnd();
                traceBeginAndSlog("StartLauncherAppsService");
                this.mSystemServiceManager.startService(LauncherAppsService.class);
                traceEnd();
                Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                Slog.i(TAG, "Secrecy Service");
                this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                traceBeginAndSlog("StartMediaProjectionManager");
                this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                traceEnd();
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                }
                if (disableCameraService) {
                }
                traceBeginAndSlog("StartJitCompilation");
                VMRuntime.getRuntime().startJitCompilation();
                traceEnd();
                traceBeginAndSlog("StartMmsService");
                mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                }
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
                if (enableWigig) {
                }
                Slog.i(TAG, "wms systemReady");
                wm.systemReady();
                traceEnd();
                config = wm.computeNewConfiguration(0);
                metrics = new DisplayMetrics();
                ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                context.getResources().updateConfiguration(config, metrics);
                systemTheme = context.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                }
                traceBeginAndSlog("MakePowerManagerServiceReady");
                Slog.i(TAG, "power manager systemReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                traceEnd();
                if (this.mSecrecyService != null) {
                }
                if (this.mFingerprintService != null) {
                }
                if (this.mFaceService != null) {
                }
                if (this.mBiometricsService != null) {
                }
                traceBeginAndSlog("MakePackageManagerServiceReady");
                Slog.i(TAG, "Package systemReady");
                this.mPackageManagerService.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                Slog.i(TAG, "DisplayManager systemReady");
                this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                traceEnd();
                this.mSystemServiceManager.setSafeMode(false);
                networkManagementF = networkManagement;
                networkStatsF = networkStats;
                networkPolicyF = networkPolicy;
                connectivityF = connectivity;
                networkScoreF = networkScore;
                locationF = location;
                countryDetectorF = countryDetector;
                networkTimeUpdaterF = networkTimeUpdater;
                commonTimeMgmtServiceF = commonTimeMgmtService;
                inputManagerF = inputManager;
                telephonyRegistryF = telephonyRegistry;
                mediaRouterF = mediaRouter;
                mmsServiceF = mmsService;
                windowManagerF = wm;
                multimediaServiceF = multimediaService;
                oppoRoundCornerServiceF = oppoRoundCornerService;
                usageServiceF = usageService;
                mOppoService = oppoService;
                Slog.i(TAG, "Ams systemReady");
                this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
            }
            if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                try {
                    Slog.i(TAG, "Chatty Service");
                    ChattyManagerService chattyManagerService = new ChattyManagerService(context);
                } catch (Throwable e22222222222222222222) {
                    reportWtf("starting Chatty Service", e22222222222222222222);
                }
            }
            if (this.mOnlyCore ? context.getResources().getBoolean(17956963) : false) {
                traceBeginAndSlog("StartTimeZoneRulesManagerService");
                this.mSystemServiceManager.startService(TIME_ZONE_RULES_MANAGER_SERVICE_CLASS);
                traceEnd();
            }
            if (!(disableNetwork || (disableNetworkTime ^ 1) == 0)) {
                traceBeginAndSlog("StartNetworkTimeUpdateService");
                networkTimeUpdateService = new NetworkTimeUpdateService(context);
                ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                networkTimeUpdater = networkTimeUpdateService;
                traceEnd();
            }
            traceBeginAndSlog("StartCommonTimeManagementService");
            try {
                commonTimeManagementService = new CommonTimeManagementService(context);
                ServiceManager.addService("commontime_management", commonTimeManagementService);
                commonTimeMgmtService = commonTimeManagementService;
            } catch (Throwable th34) {
                e22222222222222222222 = th34;
                reportWtf("starting CommonTimeManagementService service", e22222222222222222222);
                traceEnd();
                if (disableNetwork) {
                }
                traceBeginAndSlog("StartEmergencyAffordanceService");
                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                traceEnd();
                if (disableNonCoreServices) {
                }
                if (disableNonCoreServices) {
                }
                traceBeginAndSlog("AddCoverageService");
                ServiceManager.addService(CoverageService.COVERAGE_SERVICE, new CoverageService());
                traceEnd();
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
                if (disableNonCoreServices) {
                }
                traceBeginAndSlog("StartShortcutServiceLifecycle");
                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                traceEnd();
                traceBeginAndSlog("StartLauncherAppsService");
                this.mSystemServiceManager.startService(LauncherAppsService.class);
                traceEnd();
                Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                Slog.i(TAG, "Secrecy Service");
                this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                traceBeginAndSlog("StartMediaProjectionManager");
                this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                traceEnd();
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                }
                if (disableCameraService) {
                }
                traceBeginAndSlog("StartJitCompilation");
                VMRuntime.getRuntime().startJitCompilation();
                traceEnd();
                traceBeginAndSlog("StartMmsService");
                mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                }
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
                if (enableWigig) {
                }
                Slog.i(TAG, "wms systemReady");
                wm.systemReady();
                traceEnd();
                config = wm.computeNewConfiguration(0);
                metrics = new DisplayMetrics();
                ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                context.getResources().updateConfiguration(config, metrics);
                systemTheme = context.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                }
                traceBeginAndSlog("MakePowerManagerServiceReady");
                Slog.i(TAG, "power manager systemReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                traceEnd();
                if (this.mSecrecyService != null) {
                }
                if (this.mFingerprintService != null) {
                }
                if (this.mFaceService != null) {
                }
                if (this.mBiometricsService != null) {
                }
                traceBeginAndSlog("MakePackageManagerServiceReady");
                Slog.i(TAG, "Package systemReady");
                this.mPackageManagerService.systemReady();
                traceEnd();
                traceBeginAndSlog("MakeDisplayManagerServiceReady");
                Slog.i(TAG, "DisplayManager systemReady");
                this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                traceEnd();
                this.mSystemServiceManager.setSafeMode(false);
                networkManagementF = networkManagement;
                networkStatsF = networkStats;
                networkPolicyF = networkPolicy;
                connectivityF = connectivity;
                networkScoreF = networkScore;
                locationF = location;
                countryDetectorF = countryDetector;
                networkTimeUpdaterF = networkTimeUpdater;
                commonTimeMgmtServiceF = commonTimeMgmtService;
                inputManagerF = inputManager;
                telephonyRegistryF = telephonyRegistry;
                mediaRouterF = mediaRouter;
                mmsServiceF = mmsService;
                windowManagerF = wm;
                multimediaServiceF = multimediaService;
                oppoRoundCornerServiceF = oppoRoundCornerService;
                usageServiceF = usageService;
                mOppoService = oppoService;
                Slog.i(TAG, "Ams systemReady");
                this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
            }
            traceEnd();
            if (disableNetwork) {
                traceBeginAndSlog("CertBlacklister");
                try {
                    CertBlacklister certBlacklister = new CertBlacklister(context);
                } catch (Throwable e222222222222222222222) {
                    reportWtf("starting CertBlacklister", e222222222222222222222);
                }
                traceEnd();
            }
            if (!(disableNetwork || (disableNonCoreServices ^ 1) == 0)) {
                traceBeginAndSlog("StartEmergencyAffordanceService");
                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                traceEnd();
            }
            if (disableNonCoreServices) {
                traceBeginAndSlog("StartDreamManager");
                this.mSystemServiceManager.startService(DreamManagerService.class);
                traceEnd();
            }
            if (disableNonCoreServices) {
                traceBeginAndSlog("AddGraphicsStatsService");
                ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, new GraphicsStatsService(context));
                traceEnd();
            }
            if (!disableNonCoreServices && CoverageService.ENABLED) {
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
            if (disableNonCoreServices) {
                traceBeginAndSlog("StartMediaRouterService");
                try {
                    MediaRouterService mediaRouterService = new MediaRouterService(context);
                    try {
                        ServiceManager.addService("media_router", mediaRouterService);
                        mediaRouter = mediaRouterService;
                    } catch (Throwable th35) {
                        e222222222222222222222 = th35;
                        mediaRouter = mediaRouterService;
                        reportWtf("starting MediaRouterService", e222222222222222222222);
                        traceEnd();
                        Slog.i(TAG, BiometricsService.TAG_NAME);
                        this.mBiometricsService = (BiometricsService) this.mSystemServiceManager.startService(BiometricsService.class);
                        if (this.mPackageManager.hasSystemFeature("android.hardware.fingerprint")) {
                        }
                        if (this.mPackageManager.hasSystemFeature("oppo.hardware.face.support")) {
                        }
                        traceBeginAndSlog("StartBackgroundDexOptService");
                        BackgroundDexOptService.schedule(context);
                        traceEnd();
                        Slog.i(TAG, "Oppo Engineer Service");
                        this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                        traceBeginAndSlog("StartPruneInstantAppsJobService");
                        PruneInstantAppsJobService.schedule(context);
                        traceEnd();
                        traceBeginAndSlog("StartShortcutServiceLifecycle");
                        this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                        traceEnd();
                        traceBeginAndSlog("StartLauncherAppsService");
                        this.mSystemServiceManager.startService(LauncherAppsService.class);
                        traceEnd();
                        Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                        this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                        Slog.i(TAG, "Secrecy Service");
                        this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                        traceBeginAndSlog("StartMediaProjectionManager");
                        this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                        traceEnd();
                        if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                        }
                        if (disableCameraService) {
                        }
                        traceBeginAndSlog("StartJitCompilation");
                        VMRuntime.getRuntime().startJitCompilation();
                        traceEnd();
                        traceBeginAndSlog("StartMmsService");
                        mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                        traceEnd();
                        if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                        }
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
                        if (enableWigig) {
                        }
                        Slog.i(TAG, "wms systemReady");
                        wm.systemReady();
                        traceEnd();
                        config = wm.computeNewConfiguration(0);
                        metrics = new DisplayMetrics();
                        ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                        context.getResources().updateConfiguration(config, metrics);
                        systemTheme = context.getTheme();
                        if (systemTheme.getChangingConfigurations() != 0) {
                        }
                        traceBeginAndSlog("MakePowerManagerServiceReady");
                        Slog.i(TAG, "power manager systemReady");
                        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                        traceEnd();
                        if (this.mSecrecyService != null) {
                        }
                        if (this.mFingerprintService != null) {
                        }
                        if (this.mFaceService != null) {
                        }
                        if (this.mBiometricsService != null) {
                        }
                        traceBeginAndSlog("MakePackageManagerServiceReady");
                        Slog.i(TAG, "Package systemReady");
                        this.mPackageManagerService.systemReady();
                        traceEnd();
                        traceBeginAndSlog("MakeDisplayManagerServiceReady");
                        Slog.i(TAG, "DisplayManager systemReady");
                        this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                        traceEnd();
                        this.mSystemServiceManager.setSafeMode(false);
                        networkManagementF = networkManagement;
                        networkStatsF = networkStats;
                        networkPolicyF = networkPolicy;
                        connectivityF = connectivity;
                        networkScoreF = networkScore;
                        locationF = location;
                        countryDetectorF = countryDetector;
                        networkTimeUpdaterF = networkTimeUpdater;
                        commonTimeMgmtServiceF = commonTimeMgmtService;
                        inputManagerF = inputManager;
                        telephonyRegistryF = telephonyRegistry;
                        mediaRouterF = mediaRouter;
                        mmsServiceF = mmsService;
                        windowManagerF = wm;
                        multimediaServiceF = multimediaService;
                        oppoRoundCornerServiceF = oppoRoundCornerService;
                        usageServiceF = usageService;
                        mOppoService = oppoService;
                        Slog.i(TAG, "Ams systemReady");
                        this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
                    }
                } catch (Throwable th36) {
                    e222222222222222222222 = th36;
                    reportWtf("starting MediaRouterService", e222222222222222222222);
                    traceEnd();
                    Slog.i(TAG, BiometricsService.TAG_NAME);
                    this.mBiometricsService = (BiometricsService) this.mSystemServiceManager.startService(BiometricsService.class);
                    if (this.mPackageManager.hasSystemFeature("android.hardware.fingerprint")) {
                    }
                    if (this.mPackageManager.hasSystemFeature("oppo.hardware.face.support")) {
                    }
                    traceBeginAndSlog("StartBackgroundDexOptService");
                    BackgroundDexOptService.schedule(context);
                    traceEnd();
                    Slog.i(TAG, "Oppo Engineer Service");
                    this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                    traceBeginAndSlog("StartPruneInstantAppsJobService");
                    PruneInstantAppsJobService.schedule(context);
                    traceEnd();
                    traceBeginAndSlog("StartShortcutServiceLifecycle");
                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                    traceEnd();
                    traceBeginAndSlog("StartLauncherAppsService");
                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                    traceEnd();
                    Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                    this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
                    Slog.i(TAG, "Secrecy Service");
                    this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                    traceBeginAndSlog("StartMediaProjectionManager");
                    this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                    traceEnd();
                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                    }
                    if (disableCameraService) {
                    }
                    traceBeginAndSlog("StartJitCompilation");
                    VMRuntime.getRuntime().startJitCompilation();
                    traceEnd();
                    traceBeginAndSlog("StartMmsService");
                    mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                    traceEnd();
                    if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
                    }
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
                    if (enableWigig) {
                    }
                    Slog.i(TAG, "wms systemReady");
                    wm.systemReady();
                    traceEnd();
                    config = wm.computeNewConfiguration(0);
                    metrics = new DisplayMetrics();
                    ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
                    context.getResources().updateConfiguration(config, metrics);
                    systemTheme = context.getTheme();
                    if (systemTheme.getChangingConfigurations() != 0) {
                    }
                    traceBeginAndSlog("MakePowerManagerServiceReady");
                    Slog.i(TAG, "power manager systemReady");
                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                    traceEnd();
                    if (this.mSecrecyService != null) {
                    }
                    if (this.mFingerprintService != null) {
                    }
                    if (this.mFaceService != null) {
                    }
                    if (this.mBiometricsService != null) {
                    }
                    traceBeginAndSlog("MakePackageManagerServiceReady");
                    Slog.i(TAG, "Package systemReady");
                    this.mPackageManagerService.systemReady();
                    traceEnd();
                    traceBeginAndSlog("MakeDisplayManagerServiceReady");
                    Slog.i(TAG, "DisplayManager systemReady");
                    this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                    traceEnd();
                    this.mSystemServiceManager.setSafeMode(false);
                    networkManagementF = networkManagement;
                    networkStatsF = networkStats;
                    networkPolicyF = networkPolicy;
                    connectivityF = connectivity;
                    networkScoreF = networkScore;
                    locationF = location;
                    countryDetectorF = countryDetector;
                    networkTimeUpdaterF = networkTimeUpdater;
                    commonTimeMgmtServiceF = commonTimeMgmtService;
                    inputManagerF = inputManager;
                    telephonyRegistryF = telephonyRegistry;
                    mediaRouterF = mediaRouter;
                    mmsServiceF = mmsService;
                    windowManagerF = wm;
                    multimediaServiceF = multimediaService;
                    oppoRoundCornerServiceF = oppoRoundCornerService;
                    usageServiceF = usageService;
                    mOppoService = oppoService;
                    Slog.i(TAG, "Ams systemReady");
                    this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
                }
                traceEnd();
                if (this.mPackageManager.hasSystemFeature("android.hardware.fingerprint") || this.mPackageManager.hasSystemFeature("oppo.hardware.face.support")) {
                    Slog.i(TAG, BiometricsService.TAG_NAME);
                    this.mBiometricsService = (BiometricsService) this.mSystemServiceManager.startService(BiometricsService.class);
                }
                if (this.mPackageManager.hasSystemFeature("android.hardware.fingerprint")) {
                    traceBeginAndSlog("StartFingerprintSensor");
                    Slog.i(TAG, "Fingerprint Service");
                    this.mFingerprintService = (FingerprintService) this.mSystemServiceManager.startService(FingerprintService.class);
                    traceEnd();
                }
                if (this.mPackageManager.hasSystemFeature("oppo.hardware.face.support")) {
                    Slog.i(TAG, "Face Service");
                    this.mFaceService = (FaceService) this.mSystemServiceManager.startService(FaceService.class);
                }
                traceBeginAndSlog("StartBackgroundDexOptService");
                try {
                    BackgroundDexOptService.schedule(context);
                } catch (Throwable e2222222222222222222222) {
                    reportWtf("starting StartBackgroundDexOptService", e2222222222222222222222);
                }
                traceEnd();
                Slog.i(TAG, "Oppo Engineer Service");
                this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                traceBeginAndSlog("StartPruneInstantAppsJobService");
                try {
                    PruneInstantAppsJobService.schedule(context);
                } catch (Throwable e22222222222222222222222) {
                    reportWtf("StartPruneInstantAppsJobService", e22222222222222222222222);
                }
                traceEnd();
            }
            traceBeginAndSlog("StartShortcutServiceLifecycle");
            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
            traceEnd();
            traceBeginAndSlog("StartLauncherAppsService");
            this.mSystemServiceManager.startService(LauncherAppsService.class);
            traceEnd();
            try {
                Slog.i(TAG, "starting OppoPowerConsumedMonitor service");
                this.mSystemServiceManager.startService(OppoPowerConsumedMonitorService.class);
            } catch (Throwable e222222222222222222222222) {
                Slog.e(TAG, "Failure starting OppoPowerConsumedMonitor Service", e222222222222222222222222);
            }
            Slog.i(TAG, "Secrecy Service");
            this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
        }
        if (!(disableNonCoreServices || (disableMediaProjection ^ 1) == 0)) {
            traceBeginAndSlog("StartMediaProjectionManager");
            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
            traceEnd();
        }
        if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
            traceBeginAndSlog("StartWearConnectivityService");
            this.mSystemServiceManager.startService(WEAR_CONNECTIVITY_SERVICE_CLASS);
            traceEnd();
            if (!disableNonCoreServices) {
                traceBeginAndSlog("StartWearTimeService");
                this.mSystemServiceManager.startService(WEAR_DISPLAY_SERVICE_CLASS);
                this.mSystemServiceManager.startService(WEAR_TIME_SERVICE_CLASS);
                traceEnd();
                if (enableLeftyService) {
                    traceBeginAndSlog("StartWearLeftyService");
                    this.mSystemServiceManager.startService(WEAR_LEFTY_SERVICE_CLASS);
                    traceEnd();
                }
            }
        }
        if (disableCameraService) {
            traceBeginAndSlog("StartCameraServiceProxy");
            this.mSystemServiceManager.startService(CameraServiceProxy.class);
            traceEnd();
        }
        traceBeginAndSlog("StartJitCompilation");
        VMRuntime.getRuntime().startJitCompilation();
        traceEnd();
        traceBeginAndSlog("StartMmsService");
        mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
        traceEnd();
        if (this.mPackageManager.hasSystemFeature("android.software.autofill")) {
            traceBeginAndSlog("StartAutoFillService");
            this.mSystemServiceManager.startService(AUTO_FILL_MANAGER_SERVICE_CLASS);
            traceEnd();
        }
        traceBeginAndSlog("MakeVibratorServiceReady");
        try {
            vibrator.systemReady();
        } catch (Throwable e2222222222222222222222222) {
            reportWtf("making Vibrator Service ready", e2222222222222222222222222);
        }
        traceEnd();
        traceBeginAndSlog("MakeLockSettingsServiceReady");
        if (lockSettings != null) {
            try {
                lockSettings.systemReady();
            } catch (Throwable e22222222222222222222222222) {
                reportWtf("making Lock Settings Service ready", e22222222222222222222222222);
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
        if (enableWigig) {
            try {
                Slog.i(TAG, "calling onBootPhase for Wigig Services");
                obj.getClass().getMethod("onBootPhase", new Class[]{Integer.TYPE}).invoke(obj, new Object[]{new Integer(500)});
                obj2.getClass().getMethod("onBootPhase", new Class[]{Integer.TYPE}).invoke(obj2, new Object[]{new Integer(500)});
            } catch (Throwable e222222222222222222222222222) {
                reportWtf("Wigig services ready", e222222222222222222222222222);
            }
        }
        try {
            Slog.i(TAG, "wms systemReady");
            wm.systemReady();
        } catch (Throwable e2222222222222222222222222222) {
            reportWtf("making Window Manager Service ready", e2222222222222222222222222222);
        }
        traceEnd();
        config = wm.computeNewConfiguration(0);
        metrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).getDefaultDisplay().getMetrics(metrics);
        context.getResources().updateConfiguration(config, metrics);
        systemTheme = context.getTheme();
        if (systemTheme.getChangingConfigurations() != 0) {
            systemTheme.rebase();
        }
        traceBeginAndSlog("MakePowerManagerServiceReady");
        try {
            Slog.i(TAG, "power manager systemReady");
            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
        } catch (Throwable e22222222222222222222222222222) {
            reportWtf("making Power Manager Service ready", e22222222222222222222222222222);
        }
        traceEnd();
        try {
            if (this.mSecrecyService != null) {
                Slog.i(TAG, "Secrecy systemReady");
                this.mSecrecyService.systemReady();
            }
        } catch (Throwable e222222222222222222222222222222) {
            reportWtf("making secrecy Service ready", e222222222222222222222222222222);
        }
        try {
            if (this.mFingerprintService != null) {
                Slog.i(TAG, "Fingerprint systemReady");
                this.mFingerprintService.systemReady();
            }
        } catch (Throwable e2222222222222222222222222222222) {
            reportWtf("making Fingerprint Service ready", e2222222222222222222222222222222);
        }
        try {
            if (this.mFaceService != null) {
                Slog.i(TAG, "Face Service systemReady");
                this.mFaceService.systemReady();
            }
        } catch (Throwable e22222222222222222222222222222222) {
            reportWtf("making Face Service ready", e22222222222222222222222222222222);
        }
        try {
            if (this.mBiometricsService != null) {
                Slog.i(TAG, "Biometrics systemReady");
                this.mBiometricsService.systemReady();
            }
        } catch (Throwable e222222222222222222222222222222222) {
            reportWtf("making Biometrics Service ready", e222222222222222222222222222222222);
        }
        traceBeginAndSlog("MakePackageManagerServiceReady");
        try {
            Slog.i(TAG, "Package systemReady");
            this.mPackageManagerService.systemReady();
        } catch (Throwable e2222222222222222222222222222222222) {
            reportWtf("making Package Manager Service ready", e2222222222222222222222222222222222);
        }
        traceEnd();
        traceBeginAndSlog("MakeDisplayManagerServiceReady");
        try {
            Slog.i(TAG, "DisplayManager systemReady");
            this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
        } catch (Throwable e22222222222222222222222222222222222) {
            reportWtf("making Display Manager Service ready", e22222222222222222222222222222222222);
        }
        traceEnd();
        this.mSystemServiceManager.setSafeMode(false);
        networkManagementF = networkManagement;
        networkStatsF = networkStats;
        networkPolicyF = networkPolicy;
        connectivityF = connectivity;
        networkScoreF = networkScore;
        locationF = location;
        countryDetectorF = countryDetector;
        networkTimeUpdaterF = networkTimeUpdater;
        commonTimeMgmtServiceF = commonTimeMgmtService;
        inputManagerF = inputManager;
        telephonyRegistryF = telephonyRegistry;
        mediaRouterF = mediaRouter;
        mmsServiceF = mmsService;
        windowManagerF = wm;
        multimediaServiceF = multimediaService;
        oppoRoundCornerServiceF = oppoRoundCornerService;
        usageServiceF = usageService;
        mOppoService = oppoService;
        Slog.i(TAG, "Ams systemReady");
        this.mActivityManagerService.systemReady(new com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo.AnonymousClass1(this, context, windowManagerF, networkScoreF, networkManagementF, networkPolicyF, networkStatsF, connectivityF, locationF, countryDetectorF, networkTimeUpdaterF, multimediaServiceF, oppoRoundCornerServiceF, commonTimeMgmtServiceF, inputManagerF, telephonyRegistryF, mediaRouterF, usageServiceF, mmsService, mOppoService), BOOT_TIMINGS_TRACE_LOG);
    }

    /* renamed from: lambda$-com_android_server_SystemServer_42995 */
    static /* synthetic */ void m8lambda$-com_android_server_SystemServer_42995() {
        try {
            Slog.i(TAG, "SecondaryZygotePreload");
            TimingsTraceLog traceLog = new TimingsTraceLog(SYSTEM_SERVER_TIMING_ASYNC_TAG, 524288);
            traceLog.traceBegin("SecondaryZygotePreload");
            if (!Process.zygoteProcess.preloadDefault(Build.SUPPORTED_32_BIT_ABIS[0])) {
                Slog.e(TAG, "Unable to preload default resources");
            }
            traceLog.traceEnd();
        } catch (Exception ex) {
            Slog.e(TAG, "Exception preloading default resources", ex);
        }
    }

    /* renamed from: lambda$-com_android_server_SystemServer_47959 */
    static /* synthetic */ void m9lambda$-com_android_server_SystemServer_47959() {
        TimingsTraceLog traceLog = new TimingsTraceLog(SYSTEM_SERVER_TIMING_ASYNC_TAG, 524288);
        traceLog.traceBegin(START_HIDL_SERVICES);
        startHidlServices();
        traceLog.traceEnd();
    }

    /* renamed from: lambda$-com_android_server_SystemServer_104889 */
    /* synthetic */ void m10lambda$-com_android_server_SystemServer_104889(Context context, WindowManagerService windowManagerF, NetworkScoreService networkScoreF, NetworkManagementService networkManagementF, NetworkPolicyManagerService networkPolicyF, NetworkStatsService networkStatsF, ConnectivityService connectivityF, LocationManagerService locationF, CountryDetectorService countryDetectorF, NetworkTimeUpdateService networkTimeUpdaterF, OppoMultimediaService multimediaServiceF, OppoRoundCornerService oppoRoundCornerServiceF, CommonTimeManagementService commonTimeMgmtServiceF, InputManagerService inputManagerF, TelephonyRegistry telephonyRegistryF, MediaRouterService mediaRouterF, OppoUsageService usageServiceF, MmsServiceBroker mmsServiceF, OppoService mOppoService) {
        Slog.i(TAG, "Making services ready");
        traceBeginAndSlog("StartActivityManagerReadyPhase");
        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_ACTIVITY_MANAGER_READY);
        traceEnd();
        traceBeginAndSlog("StartObservingNativeCrashes");
        try {
            this.mActivityManagerService.startObservingNativeCrashes();
        } catch (Throwable e) {
            reportWtf("observing native crashes", e);
        }
        traceEnd();
        String WEBVIEW_PREPARATION = "WebViewFactoryPreparation";
        Future webviewPrep = null;
        if (!this.mOnlyCore) {
            webviewPrep = SystemServerInitThreadPool.get().submit(new AnonymousClass2(this), "WebViewFactoryPreparation");
        }
        if (this.mPackageManager.hasSystemFeature("android.hardware.type.automotive")) {
            traceBeginAndSlog("StartCarServiceHelperService");
            this.mSystemServiceManager.startService(CarServiceHelperService.class);
            traceEnd();
        }
        traceBeginAndSlog("StartSystemUI");
        try {
            startSystemUi(context, windowManagerF);
        } catch (Throwable e2) {
            reportWtf("starting System UI", e2);
        }
        traceEnd();
        traceBeginAndSlog("MakeNetworkScoreReady");
        if (networkScoreF != null) {
            try {
                networkScoreF.systemReady();
            } catch (Throwable e22) {
                reportWtf("making Network Score Service ready", e22);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeNetworkManagementServiceReady");
        if (networkManagementF != null) {
            try {
                networkManagementF.systemReady();
            } catch (Throwable e222) {
                reportWtf("making Network Managment Service ready", e222);
            }
        }
        CountDownLatch networkPolicyInitReadySignal = null;
        if (networkPolicyF != null) {
            networkPolicyInitReadySignal = networkPolicyF.networkScoreAndNetworkManagementServiceReady();
        }
        traceEnd();
        traceBeginAndSlog("MakeNetworkStatsServiceReady");
        if (networkStatsF != null) {
            try {
                networkStatsF.systemReady();
            } catch (Throwable e2222) {
                reportWtf("making Network Stats Service ready", e2222);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeConnectivityServiceReady");
        if (connectivityF != null) {
            try {
                connectivityF.systemReady();
            } catch (Throwable e22222) {
                reportWtf("making Connectivity Service ready", e22222);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeNetworkPolicyServiceReady");
        if (networkPolicyF != null) {
            try {
                networkPolicyF.systemReady(networkPolicyInitReadySignal);
            } catch (Throwable e222222) {
                reportWtf("making Network Policy Service ready", e222222);
            }
        }
        traceEnd();
        traceBeginAndSlog("StartWatchdog");
        Watchdog.getInstance().start();
        traceEnd();
        this.mPackageManagerService.waitForAppDataPrepared();
        traceBeginAndSlog("PhaseThirdPartyAppsCanStart");
        if (webviewPrep != null) {
            ConcurrentUtils.waitForFutureNoInterrupt(webviewPrep, "WebViewFactoryPreparation");
        }
        this.mSystemServiceManager.startBootPhase(600);
        traceEnd();
        traceBeginAndSlog("MakeLocationServiceReady");
        if (locationF != null) {
            try {
                locationF.systemRunning();
            } catch (Throwable e2222222) {
                reportWtf("Notifying Location Service running", e2222222);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeCountryDetectionServiceReady");
        if (countryDetectorF != null) {
            try {
                countryDetectorF.systemRunning();
            } catch (Throwable e22222222) {
                reportWtf("Notifying CountryDetectorService running", e22222222);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeNetworkTimeUpdateReady");
        if (networkTimeUpdaterF != null) {
            try {
                networkTimeUpdaterF.systemRunning();
            } catch (Throwable e222222222) {
                reportWtf("Notifying NetworkTimeService running", e222222222);
            }
        }
        if (multimediaServiceF != null) {
            try {
                multimediaServiceF.systemRunning();
            } catch (Throwable e2222222222) {
                reportWtf("Notifying MultiMediaService running", e2222222222);
            }
        }
        if (SystemProperties.getInt("persist.sys.enable.rc", 0) == 1 && oppoRoundCornerServiceF != null) {
            Slog.i(TAG, "OppoRoundCornerService Service  init...");
            oppoRoundCornerServiceF.init();
        }
        traceEnd();
        traceBeginAndSlog("MakeCommonTimeManagementServiceReady");
        if (commonTimeMgmtServiceF != null) {
            try {
                commonTimeMgmtServiceF.systemRunning();
            } catch (Throwable e22222222222) {
                reportWtf("Notifying CommonTimeManagementService running", e22222222222);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeInputManagerServiceReady");
        if (inputManagerF != null) {
            try {
                inputManagerF.systemRunning();
            } catch (Throwable e222222222222) {
                reportWtf("Notifying InputManagerService running", e222222222222);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeTelephonyRegistryReady");
        if (telephonyRegistryF != null) {
            try {
                telephonyRegistryF.systemRunning();
            } catch (Throwable e2222222222222) {
                reportWtf("Notifying TelephonyRegistry running", e2222222222222);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeMediaRouterServiceReady");
        if (mediaRouterF != null) {
            try {
                mediaRouterF.systemRunning();
            } catch (Throwable e22222222222222) {
                reportWtf("Notifying MediaRouterService running", e22222222222222);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeUsageServiceFReady");
        if (usageServiceF != null) {
            try {
                usageServiceF.systemReady();
            } catch (Throwable e222222222222222) {
                reportWtf("Notifying OppoUsageService running", e222222222222222);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeMmsServiceReady");
        if (mmsServiceF != null) {
            try {
                mmsServiceF.systemRunning();
            } catch (Throwable e2222222222222222) {
                reportWtf("Notifying MmsService running", e2222222222222222);
            }
        }
        traceEnd();
        traceBeginAndSlog("MakeNetworkScoreServiceReady");
        if (networkScoreF != null) {
            try {
                networkScoreF.systemRunning();
            } catch (Throwable e22222222222222222) {
                reportWtf("Notifying NetworkScoreService running", e22222222222222222);
            }
        }
        traceEnd();
        traceBeginAndSlog("IncidentDaemonReady");
        try {
            IIncidentManager incident = IIncidentManager.Stub.asInterface(ServiceManager.checkService("incident"));
            if (incident != null) {
                incident.systemRunning();
            }
        } catch (Throwable e222222222222222222) {
            reportWtf("Notifying incident daemon running", e222222222222222222);
        }
        if (mOppoService != null) {
            try {
                mOppoService.systemReady();
            } catch (Throwable e2222222222222222222) {
                reportWtf("Notifying mOppoService running", e2222222222222222222);
            }
        }
        traceEnd();
    }

    /* renamed from: lambda$-com_android_server_SystemServer_105794 */
    /* synthetic */ void m11lambda$-com_android_server_SystemServer_105794() {
        Slog.i(TAG, "WebViewFactoryPreparation");
        TimingsTraceLog traceLog = new TimingsTraceLog(SYSTEM_SERVER_TIMING_ASYNC_TAG, 524288);
        traceLog.traceBegin("WebViewFactoryPreparation");
        ConcurrentUtils.waitForFutureNoInterrupt(this.mZygotePreload, "Zygote preload");
        this.mZygotePreload = null;
        this.mWebViewUpdateService.prepareWebViewInSystemServer();
        traceLog.traceEnd();
    }

    static final void startSystemUi(Context context, WindowManagerService windowManager) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(OppoFreeFormManagerService.FREEFORM_CALLER_PKG, "com.android.systemui.SystemUIService"));
        intent.addFlags(256);
        context.startServiceAsUser(intent, UserHandle.SYSTEM);
        windowManager.onSystemUiStarted();
    }

    private static void traceBeginAndSlog(String name) {
        Slog.i(TAG, name);
        BOOT_TIMINGS_TRACE_LOG.traceBegin(name);
    }

    private static void traceEnd() {
        BOOT_TIMINGS_TRACE_LOG.traceEnd();
    }
}
