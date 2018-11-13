package com.android.server;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.app.ActivityThread;
import android.app.INotificationManager;
import android.app.usage.UsageStatsManagerInternal;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources.Theme;
import android.os.BaseBundle;
import android.os.Build;
import android.os.Environment;
import android.os.FactoryTest;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StrictMode;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.IMountService;
import android.os.storage.IMountService.Stub;
import android.provider.Settings.Global;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.util.Log;
import android.util.Slog;
import android.view.WindowManager;
import com.android.internal.app.NightDisplayController;
import com.android.internal.os.BinderInternal;
import com.android.internal.os.SamplingProfilerIntegration;
import com.android.internal.widget.ILockSettings;
import com.android.server.accessibility.AccessibilityManagerService;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.ActivityManagerService.Lifecycle;
import com.android.server.am.OppoProcessManager;
import com.android.server.audio.AudioService;
import com.android.server.camera.CameraService;
import com.android.server.clipboard.ClipboardService;
import com.android.server.connectivity.IpConnectivityMetrics;
import com.android.server.connectivity.MetricsLoggerService;
import com.android.server.devicepolicy.DevicePolicyManagerService;
import com.android.server.display.DisplayManagerService;
import com.android.server.display.NightDisplayService;
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
import com.android.server.oppo.CabcService;
import com.android.server.oppo.ChattyManagerService;
import com.android.server.oppo.HypnusService;
import com.android.server.oppo.IElsaManager;
import com.android.server.oppo.OppoCustomizeService;
import com.android.server.oppo.OppoExService;
import com.android.server.oppo.OppoService;
import com.android.server.oppo.OppoUsageService;
import com.android.server.os.SchedulingPolicyService;
import com.android.server.pm.BackgroundDexOptService;
import com.android.server.pm.Installer;
import com.android.server.pm.LauncherAppsService;
import com.android.server.pm.OtaDexoptService;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.ShortcutService;
import com.android.server.pm.UserManagerService.LifeCycle;
import com.android.server.power.PowerManagerService;
import com.android.server.power.ShutdownThread;
import com.android.server.restrictions.RestrictionsManagerService;
import com.android.server.retaildemo.RetailDemoModeService;
import com.android.server.secrecy.SecrecyService;
import com.android.server.soundtrigger.SoundTriggerService;
import com.android.server.statusbar.StatusBarManagerService;
import com.android.server.storage.DeviceStorageMonitorService;
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
import com.mediatek.hdmi.MtkHdmiManagerService;
import com.mediatek.msglogger.MessageMonitorService;
import com.mediatek.perfservice.IPerfServiceManager;
import com.mediatek.perfservice.PerfServiceImpl;
import com.mediatek.perfservice.PerfServiceManager;
import com.mediatek.runningbooster.RunningBoosterService;
import com.mediatek.search.SearchEngineManagerService;
import com.mediatek.sensorhub.SensorHubService;
import com.mediatek.suppression.service.SuppressionService;
import com.oppo.media.OppoMultimediaService;
import dalvik.system.VMRuntime;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
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
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public final class SystemServer {
    private static final String ACCOUNT_SERVICE_CLASS = "com.android.server.accounts.AccountManagerService$Lifecycle";
    private static final String APPDETECTION_SERVICE_CLASS = "com.mediatek.pq.AppDetectionService$Lifecycle";
    private static final String APPWIDGET_SERVICE_CLASS = "com.android.server.appwidget.AppWidgetService";
    private static final String BACKUP_MANAGER_SERVICE_CLASS = "com.android.server.backup.BackupManagerService$Lifecycle";
    private static final String BLOCK_MAP_FILE = "/cache/recovery/block.map";
    private static final String CONTENT_SERVICE_CLASS = "com.android.server.content.ContentService$Lifecycle";
    private static final String DATASHPAING_SERVICE_CLASS = "com.mediatek.datashaping.DataShapingService";
    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "JianHui.Yu@Plf.SDK, 2016-12-24 : Modify for use oppo style", property = OppoRomType.ROM)
    private static final int DEFAULT_SYSTEM_THEME = 201523202;
    private static final long EARLIEST_SUPPORTED_TIME = 86400000;
    private static final String ENCRYPTED_STATE = "1";
    private static final String ENCRYPTING_STATE = "trigger_restart_min_framework";
    private static final String ETHERNET_SERVICE_CLASS = "com.android.server.ethernet.EthernetService";
    private static final boolean IS_USER_BUILD = false;
    private static final String JOB_SCHEDULER_SERVICE_CLASS = "com.android.server.job.JobSchedulerService";
    private static final String LOCK_SETTINGS_SERVICE_CLASS = "com.android.server.LockSettingsService$Lifecycle";
    private static final String LWX_SERVICE_CLASS = "com.mediatek.server.lwx.LwxService";
    private static final String MIDI_SERVICE_CLASS = "com.android.server.midi.MidiService$Lifecycle";
    private static final String MOUNT_SERVICE_CLASS = "com.android.server.MountService$Lifecycle";
    private static final String PERSISTENT_DATA_BLOCK_PROP = "ro.frp.pst";
    private static final String PRINT_MANAGER_SERVICE_CLASS = "com.android.server.print.PrintManagerService";
    private static final String SEARCH_MANAGER_SERVICE_CLASS = "com.android.server.search.SearchManagerService$Lifecycle";
    private static final long SNAPSHOT_INTERVAL = 3600000;
    private static final String TAG = "SystemServer";
    private static final String THERMAL_OBSERVER_CLASS = "com.google.android.clockwork.ThermalObserver";
    private static final String UNCRYPT_PACKAGE_FILE = "/cache/recovery/uncrypt_file";
    private static final String USB_SERVICE_CLASS = "com.android.server.usb.UsbService$Lifecycle";
    private static final String USP_SERVICE_CLASS = "com.mediatek.usp.UspService";
    private static final String VOICE_RECOGNITION_MANAGER_SERVICE_CLASS = "com.android.server.voiceinteraction.VoiceInteractionManagerService";
    private static final String WALLPAPER_SERVICE_CLASS = "com.android.server.wallpaper.WallpaperManagerService$Lifecycle";
    private static final String WEAR_BLUETOOTH_SERVICE_CLASS = "com.google.android.clockwork.bluetooth.WearBluetoothService";
    private static final String WEAR_TIME_SERVICE_CLASS = "com.google.android.clockwork.time.WearTimeService";
    private static final String WEAR_WIFI_MEDIATOR_SERVICE_CLASS = "com.google.android.clockwork.wifi.WearWifiMediatorService";
    private static final String WIFI_NAN_SERVICE_CLASS = "com.android.server.wifi.nan.WifiNanService";
    private static final String WIFI_P2P_SERVICE_CLASS = "com.android.server.wifi.p2p.WifiP2pService";
    private static final String WIFI_SERVICE_CLASS = "com.android.server.wifi.WifiService";
    private static boolean mMTPROF_disable = false;
    private static final int sMaxBinderThreads = 31;
    private ActivityManagerService mActivityManagerService;
    private ContentResolver mContentResolver;
    private DisplayManagerService mDisplayManagerService;
    private EntropyMixer mEntropyMixer;
    private FaceService mFaceService;
    private final int mFactoryTestMode;
    private FingerprintService mFingerprintService;
    private boolean mFirstBoot;
    private boolean mOnlyCore;
    private OppoEngineerService mOppoEngineerService;
    private OppoLightsService mOppoLightsService;
    private PackageManager mPackageManager;
    private PackageManagerService mPackageManagerService;
    private PowerManagerService mPowerManagerService;
    private Timer mProfilerSnapshotTimer;
    private SecrecyService mSecrecyService;
    private Context mSystemContext;
    private SystemServiceManager mSystemServiceManager;
    private WebViewUpdateService mWebViewUpdateService;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.SystemServer.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.SystemServer.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.SystemServer.<clinit>():void");
    }

    private static native void startSensorService();

    public static void main(String[] args) {
        new SystemServer().run();
    }

    public SystemServer() {
        this.mOppoEngineerService = null;
        this.mFactoryTestMode = FactoryTest.getMode();
    }

    private void run() {
        try {
            Trace.traceBegin(2097152, "InitBeforeStartServices");
            if (System.currentTimeMillis() < 86400000) {
                Slog.w(TAG, "System clock is before 1970; setting to 1970.");
                SystemClock.setCurrentTimeMillis(86400000);
            }
            if (!SystemProperties.get("persist.sys.language").isEmpty()) {
                SystemProperties.set("persist.sys.locale", Locale.getDefault().toLanguageTag());
                SystemProperties.set("persist.sys.language", IElsaManager.EMPTY_PACKAGE);
                SystemProperties.set("persist.sys.country", IElsaManager.EMPTY_PACKAGE);
                SystemProperties.set("persist.sys.localevar", IElsaManager.EMPTY_PACKAGE);
            }
            Slog.i(TAG, "Entered the Android system server!");
            EventLog.writeEvent(EventLogTags.BOOT_PROGRESS_SYSTEM_RUN, SystemClock.uptimeMillis());
            addBootEvent("Android:SysServerInit_START");
            SystemProperties.set("persist.sys.dalvik.vm.lib.2", VMRuntime.getRuntime().vmLibrary());
            if (SamplingProfilerIntegration.isEnabled()) {
                SamplingProfilerIntegration.start();
                this.mProfilerSnapshotTimer = new Timer();
                this.mProfilerSnapshotTimer.schedule(new TimerTask() {
                    public void run() {
                        SamplingProfilerIntegration.writeSnapshot("system_server", null);
                    }
                }, SNAPSHOT_INTERVAL, SNAPSHOT_INTERVAL);
            }
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
            Runtime.getRuntime().exec("rm -r /data/piggybank");
        } catch (IOException e) {
            Slog.e(TAG, "system server init delete piggybank fail" + e);
        } catch (Throwable th) {
            Trace.traceEnd(2097152);
        }
        performPendingShutdown();
        createSystemContext();
        this.mSystemServiceManager = new SystemServiceManager(this.mSystemContext);
        LocalServices.addService(SystemServiceManager.class, this.mSystemServiceManager);
        Trace.traceEnd(2097152);
        SystemProperties.set("sys.oppo.boot_completed", "0");
        try {
            Trace.traceBegin(2097152, "StartServices");
            startBootstrapServices();
            startCoreServices();
            startOtherServices();
            Trace.traceEnd(2097152);
            if (StrictMode.conditionallyEnableDebugLogging()) {
                Slog.i(TAG, "Enabled StrictMode for system server main thread.");
            }
            addBootEvent("Android:SysServerInit_END");
            Looper.loop();
            throw new RuntimeException("Main thread loop unexpectedly exited");
        } catch (Throwable th2) {
            Trace.traceEnd(2097152);
        }
    }

    private void reportWtf(String msg, Throwable e) {
        Slog.w(TAG, "***********************************************");
        Slog.wtf(TAG, "BOOT FAILURE " + msg, e);
    }

    private static void addBootEvent(String bootevent) {
        if (!mMTPROF_disable) {
            try {
                FileOutputStream fbp = new FileOutputStream("/proc/bootprof");
                fbp.write(bootevent.getBytes());
                fbp.flush();
                fbp.close();
            } catch (FileNotFoundException e) {
                Slog.e("BOOTPROF", "Failure open /proc/bootprof, not found!", e);
            } catch (IOException e2) {
                Slog.e("BOOTPROF", "Failure open /proc/bootprof entry", e2);
            }
        }
    }

    private void performPendingShutdown() {
        String shutdownAction = SystemProperties.get(ShutdownThread.SHUTDOWN_ACTION_PROPERTY, IElsaManager.EMPTY_PACKAGE);
        if (shutdownAction != null && shutdownAction.length() > 0) {
            String reason;
            boolean reboot = shutdownAction.charAt(0) == '1';
            if (shutdownAction.length() > 1) {
                reason = shutdownAction.substring(1, shutdownAction.length());
            } else {
                reason = null;
            }
            if ("recovery-update".equals(reason)) {
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
            ShutdownThread.rebootOrShutdown(null, reboot, reason);
        }
    }

    private void createSystemContext() {
        this.mSystemContext = ActivityThread.systemMain().getSystemContext();
        this.mSystemContext.setTheme(DEFAULT_SYSTEM_THEME);
    }

    /* JADX WARNING: Removed duplicated region for block: B:24:0x013d  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x00b5  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0153  */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x00cc  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x00b5  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x013d  */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x00cc  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0153  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void startBootstrapServices() {
        Throwable e;
        String cryptState;
        Context context;
        boolean z;
        Installer installer = (Installer) this.mSystemServiceManager.startService(Installer.class);
        if (!IS_USER_BUILD) {
            try {
                MessageMonitorService msgMonitorService = new MessageMonitorService();
                try {
                    Slog.e(TAG, "Create message monitor service successfully .");
                    ServiceManager.addService("msgmonitorservice", msgMonitorService.asBinder());
                } catch (Throwable th) {
                    e = th;
                    Slog.e(TAG, "Starting message monitor service exception ", e);
                    Slog.i(TAG, "Ams Service");
                    this.mActivityManagerService = ((Lifecycle) this.mSystemServiceManager.startService(Lifecycle.class)).getService();
                    this.mActivityManagerService.setSystemServiceManager(this.mSystemServiceManager);
                    this.mActivityManagerService.setInstaller(installer);
                    Slog.i(TAG, "Power Service");
                    this.mPowerManagerService = (PowerManagerService) this.mSystemServiceManager.startService(PowerManagerService.class);
                    Trace.traceBegin(2097152, "InitPowerManagement");
                    this.mActivityManagerService.initPowerManagement();
                    Trace.traceEnd(2097152);
                    Slog.i(TAG, "Light Service");
                    this.mOppoLightsService = (OppoLightsService) this.mSystemServiceManager.startService(OppoLightsService.class);
                    Slog.i(TAG, "DisplayManager Service");
                    this.mDisplayManagerService = (DisplayManagerService) this.mSystemServiceManager.startService(DisplayManagerService.class);
                    this.mSystemServiceManager.startBootPhase(100);
                    cryptState = SystemProperties.get("vold.decrypt");
                    if (ENCRYPTING_STATE.equals(cryptState)) {
                    }
                    traceBeginAndSlog("StartPackageManagerService");
                    context = this.mSystemContext;
                    if (this.mFactoryTestMode != 0) {
                    }
                    this.mPackageManagerService = PackageManagerService.main(context, installer, z, this.mOnlyCore);
                    this.mFirstBoot = this.mPackageManagerService.isFirstBoot();
                    this.mPackageManager = this.mSystemContext.getPackageManager();
                    Trace.traceEnd(2097152);
                    traceBeginAndSlog("StartOtaDexOptService");
                    try {
                        OtaDexoptService.main(this.mSystemContext, this.mPackageManagerService);
                    } catch (Throwable e2) {
                        reportWtf("starting OtaDexOptService", e2);
                    } finally {
                        Trace.traceEnd(2097152);
                    }
                    traceBeginAndSlog("StartUserManagerService");
                    this.mSystemServiceManager.startService(LifeCycle.class);
                    Trace.traceEnd(2097152);
                    AttributeCache.init(this.mSystemContext);
                    this.mActivityManagerService.setSystemProcess();
                    this.mPackageManagerService.onAmsAddedtoServiceMgr();
                    Slog.i(TAG, "Sensor Service");
                    startSensorService();
                }
            } catch (Throwable th2) {
                e2 = th2;
                Slog.e(TAG, "Starting message monitor service exception ", e2);
                Slog.i(TAG, "Ams Service");
                this.mActivityManagerService = ((Lifecycle) this.mSystemServiceManager.startService(Lifecycle.class)).getService();
                this.mActivityManagerService.setSystemServiceManager(this.mSystemServiceManager);
                this.mActivityManagerService.setInstaller(installer);
                Slog.i(TAG, "Power Service");
                this.mPowerManagerService = (PowerManagerService) this.mSystemServiceManager.startService(PowerManagerService.class);
                Trace.traceBegin(2097152, "InitPowerManagement");
                this.mActivityManagerService.initPowerManagement();
                Trace.traceEnd(2097152);
                Slog.i(TAG, "Light Service");
                this.mOppoLightsService = (OppoLightsService) this.mSystemServiceManager.startService(OppoLightsService.class);
                Slog.i(TAG, "DisplayManager Service");
                this.mDisplayManagerService = (DisplayManagerService) this.mSystemServiceManager.startService(DisplayManagerService.class);
                this.mSystemServiceManager.startBootPhase(100);
                cryptState = SystemProperties.get("vold.decrypt");
                if (ENCRYPTING_STATE.equals(cryptState)) {
                }
                traceBeginAndSlog("StartPackageManagerService");
                context = this.mSystemContext;
                if (this.mFactoryTestMode != 0) {
                }
                this.mPackageManagerService = PackageManagerService.main(context, installer, z, this.mOnlyCore);
                this.mFirstBoot = this.mPackageManagerService.isFirstBoot();
                this.mPackageManager = this.mSystemContext.getPackageManager();
                Trace.traceEnd(2097152);
                traceBeginAndSlog("StartOtaDexOptService");
                OtaDexoptService.main(this.mSystemContext, this.mPackageManagerService);
                traceBeginAndSlog("StartUserManagerService");
                this.mSystemServiceManager.startService(LifeCycle.class);
                Trace.traceEnd(2097152);
                AttributeCache.init(this.mSystemContext);
                this.mActivityManagerService.setSystemProcess();
                this.mPackageManagerService.onAmsAddedtoServiceMgr();
                Slog.i(TAG, "Sensor Service");
                startSensorService();
            }
        }
        Slog.i(TAG, "Ams Service");
        this.mActivityManagerService = ((Lifecycle) this.mSystemServiceManager.startService(Lifecycle.class)).getService();
        this.mActivityManagerService.setSystemServiceManager(this.mSystemServiceManager);
        this.mActivityManagerService.setInstaller(installer);
        Slog.i(TAG, "Power Service");
        this.mPowerManagerService = (PowerManagerService) this.mSystemServiceManager.startService(PowerManagerService.class);
        Trace.traceBegin(2097152, "InitPowerManagement");
        this.mActivityManagerService.initPowerManagement();
        Trace.traceEnd(2097152);
        Slog.i(TAG, "Light Service");
        this.mOppoLightsService = (OppoLightsService) this.mSystemServiceManager.startService(OppoLightsService.class);
        Slog.i(TAG, "DisplayManager Service");
        this.mDisplayManagerService = (DisplayManagerService) this.mSystemServiceManager.startService(DisplayManagerService.class);
        this.mSystemServiceManager.startBootPhase(100);
        cryptState = SystemProperties.get("vold.decrypt");
        if (ENCRYPTING_STATE.equals(cryptState)) {
            Slog.w(TAG, "Detected encryption in progress - only parsing core apps");
            this.mOnlyCore = true;
        } else if ("1".equals(cryptState)) {
            Slog.w(TAG, "Device encrypted - only parsing core apps");
            this.mOnlyCore = true;
        }
        traceBeginAndSlog("StartPackageManagerService");
        context = this.mSystemContext;
        if (this.mFactoryTestMode != 0) {
            z = true;
        } else {
            z = false;
        }
        this.mPackageManagerService = PackageManagerService.main(context, installer, z, this.mOnlyCore);
        this.mFirstBoot = this.mPackageManagerService.isFirstBoot();
        this.mPackageManager = this.mSystemContext.getPackageManager();
        Trace.traceEnd(2097152);
        if (!(this.mOnlyCore || SystemProperties.getBoolean("config.disable_otadexopt", false))) {
            traceBeginAndSlog("StartOtaDexOptService");
            OtaDexoptService.main(this.mSystemContext, this.mPackageManagerService);
        }
        traceBeginAndSlog("StartUserManagerService");
        this.mSystemServiceManager.startService(LifeCycle.class);
        Trace.traceEnd(2097152);
        AttributeCache.init(this.mSystemContext);
        this.mActivityManagerService.setSystemProcess();
        this.mPackageManagerService.onAmsAddedtoServiceMgr();
        Slog.i(TAG, "Sensor Service");
        startSensorService();
    }

    private void startCoreServices() {
        Slog.i(TAG, "Battery Service");
        this.mSystemServiceManager.startService(BatteryService.class);
        Slog.i(TAG, "UsageStats Service");
        this.mSystemServiceManager.startService(UsageStatsService.class);
        this.mActivityManagerService.setUsageStatsManager((UsageStatsManagerInternal) LocalServices.getService(UsageStatsManagerInternal.class));
        this.mWebViewUpdateService = (WebViewUpdateService) this.mSystemServiceManager.startService(WebViewUpdateService.class);
    }

    /* JADX WARNING: Removed duplicated region for block: B:166:0x06df A:{SYNTHETIC, Splitter: B:166:0x06df} */
    /* JADX WARNING: Removed duplicated region for block: B:169:0x06f5  */
    /* JADX WARNING: Removed duplicated region for block: B:177:0x0738  */
    /* JADX WARNING: Removed duplicated region for block: B:202:0x07d4  */
    /* JADX WARNING: Removed duplicated region for block: B:205:0x07f1  */
    /* JADX WARNING: Removed duplicated region for block: B:248:0x091e A:{SYNTHETIC, Splitter: B:248:0x091e} */
    /* JADX WARNING: Removed duplicated region for block: B:257:0x0950  */
    /* JADX WARNING: Removed duplicated region for block: B:271:0x098a  */
    /* JADX WARNING: Removed duplicated region for block: B:278:0x09a3  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x09ba  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x09d2  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0a04  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0a1a  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x0a30  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x0a46  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0a51  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0b36 A:{SYNTHETIC, Splitter: B:327:0x0b36} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:271:0x098a  */
    /* JADX WARNING: Removed duplicated region for block: B:278:0x09a3  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x09ba  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x09d2  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0a04  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0a1a  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x0a30  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x0a46  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0a51  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0b36 A:{SYNTHETIC, Splitter: B:327:0x0b36} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x03d7  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x03f0  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x040e  */
    /* JADX WARNING: Removed duplicated region for block: B:125:0x057f  */
    /* JADX WARNING: Removed duplicated region for block: B:130:0x0598  */
    /* JADX WARNING: Removed duplicated region for block: B:139:0x05c9 A:{Catch:{ Throwable -> 0x1010 }} */
    /* JADX WARNING: Removed duplicated region for block: B:479:0x101d  */
    /* JADX WARNING: Removed duplicated region for block: B:142:0x060a  */
    /* JADX WARNING: Removed duplicated region for block: B:144:0x061e  */
    /* JADX WARNING: Removed duplicated region for block: B:166:0x06df A:{SYNTHETIC, Splitter: B:166:0x06df} */
    /* JADX WARNING: Removed duplicated region for block: B:169:0x06f5  */
    /* JADX WARNING: Removed duplicated region for block: B:177:0x0738  */
    /* JADX WARNING: Removed duplicated region for block: B:202:0x07d4  */
    /* JADX WARNING: Removed duplicated region for block: B:205:0x07f1  */
    /* JADX WARNING: Removed duplicated region for block: B:248:0x091e A:{SYNTHETIC, Splitter: B:248:0x091e} */
    /* JADX WARNING: Removed duplicated region for block: B:257:0x0950  */
    /* JADX WARNING: Removed duplicated region for block: B:271:0x098a  */
    /* JADX WARNING: Removed duplicated region for block: B:278:0x09a3  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x09ba  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x09d2  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0a04  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0a1a  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x0a30  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x0a46  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0a51  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0b36 A:{SYNTHETIC, Splitter: B:327:0x0b36} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:257:0x0950  */
    /* JADX WARNING: Removed duplicated region for block: B:271:0x098a  */
    /* JADX WARNING: Removed duplicated region for block: B:278:0x09a3  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x09ba  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x09d2  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0a04  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0a1a  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x0a30  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x0a46  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0a51  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0b36 A:{SYNTHETIC, Splitter: B:327:0x0b36} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:248:0x091e A:{SYNTHETIC, Splitter: B:248:0x091e} */
    /* JADX WARNING: Removed duplicated region for block: B:257:0x0950  */
    /* JADX WARNING: Removed duplicated region for block: B:271:0x098a  */
    /* JADX WARNING: Removed duplicated region for block: B:278:0x09a3  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x09ba  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x09d2  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0a04  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0a1a  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x0a30  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x0a46  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0a51  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0b36 A:{SYNTHETIC, Splitter: B:327:0x0b36} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x09ba  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x09d2  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0a04  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0a1a  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x0a30  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x0a46  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0a51  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0b36 A:{SYNTHETIC, Splitter: B:327:0x0b36} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x02c9  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x031d  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x035d  */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x02c9  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x031d  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x035d  */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:166:0x06df A:{SYNTHETIC, Splitter: B:166:0x06df} */
    /* JADX WARNING: Removed duplicated region for block: B:169:0x06f5  */
    /* JADX WARNING: Removed duplicated region for block: B:177:0x0738  */
    /* JADX WARNING: Removed duplicated region for block: B:202:0x07d4  */
    /* JADX WARNING: Removed duplicated region for block: B:205:0x07f1  */
    /* JADX WARNING: Removed duplicated region for block: B:248:0x091e A:{SYNTHETIC, Splitter: B:248:0x091e} */
    /* JADX WARNING: Removed duplicated region for block: B:257:0x0950  */
    /* JADX WARNING: Removed duplicated region for block: B:271:0x098a  */
    /* JADX WARNING: Removed duplicated region for block: B:278:0x09a3  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x09ba  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x09d2  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0a04  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0a1a  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x0a30  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x0a46  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0a51  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0b36 A:{SYNTHETIC, Splitter: B:327:0x0b36} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:308:0x0a70  */
    /* JADX WARNING: Removed duplicated region for block: B:545:0x11c7  */
    /* JADX WARNING: Removed duplicated region for block: B:320:0x0ad3  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0b36 A:{SYNTHETIC, Splitter: B:327:0x0b36} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x02c9  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x031d  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x035d  */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x09ba  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x09d2  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0a04  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0a1a  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x0a30  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x0a46  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0a51  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0b36 A:{SYNTHETIC, Splitter: B:327:0x0b36} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:308:0x0a70  */
    /* JADX WARNING: Removed duplicated region for block: B:320:0x0ad3  */
    /* JADX WARNING: Removed duplicated region for block: B:545:0x11c7  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0b36 A:{SYNTHETIC, Splitter: B:327:0x0b36} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:202:0x07d4  */
    /* JADX WARNING: Removed duplicated region for block: B:205:0x07f1  */
    /* JADX WARNING: Removed duplicated region for block: B:248:0x091e A:{SYNTHETIC, Splitter: B:248:0x091e} */
    /* JADX WARNING: Removed duplicated region for block: B:257:0x0950  */
    /* JADX WARNING: Removed duplicated region for block: B:271:0x098a  */
    /* JADX WARNING: Removed duplicated region for block: B:278:0x09a3  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x09ba  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x09d2  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0a04  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0a1a  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x0a30  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x0a46  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0a51  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0b36 A:{SYNTHETIC, Splitter: B:327:0x0b36} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:271:0x098a  */
    /* JADX WARNING: Removed duplicated region for block: B:278:0x09a3  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x09ba  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x09d2  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0a04  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0a1a  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x0a30  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x0a46  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0a51  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0b36 A:{SYNTHETIC, Splitter: B:327:0x0b36} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x02c9  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x031d  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x035d  */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:257:0x0950  */
    /* JADX WARNING: Removed duplicated region for block: B:271:0x098a  */
    /* JADX WARNING: Removed duplicated region for block: B:278:0x09a3  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x09ba  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x09d2  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0a04  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0a1a  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x0a30  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x0a46  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0a51  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0b36 A:{SYNTHETIC, Splitter: B:327:0x0b36} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x0531  */
    /* JADX WARNING: Removed duplicated region for block: B:125:0x057f  */
    /* JADX WARNING: Removed duplicated region for block: B:130:0x0598  */
    /* JADX WARNING: Removed duplicated region for block: B:139:0x05c9 A:{Catch:{ Throwable -> 0x1010 }} */
    /* JADX WARNING: Removed duplicated region for block: B:142:0x060a  */
    /* JADX WARNING: Removed duplicated region for block: B:479:0x101d  */
    /* JADX WARNING: Removed duplicated region for block: B:144:0x061e  */
    /* JADX WARNING: Removed duplicated region for block: B:166:0x06df A:{SYNTHETIC, Splitter: B:166:0x06df} */
    /* JADX WARNING: Removed duplicated region for block: B:169:0x06f5  */
    /* JADX WARNING: Removed duplicated region for block: B:177:0x0738  */
    /* JADX WARNING: Removed duplicated region for block: B:202:0x07d4  */
    /* JADX WARNING: Removed duplicated region for block: B:205:0x07f1  */
    /* JADX WARNING: Removed duplicated region for block: B:248:0x091e A:{SYNTHETIC, Splitter: B:248:0x091e} */
    /* JADX WARNING: Removed duplicated region for block: B:257:0x0950  */
    /* JADX WARNING: Removed duplicated region for block: B:271:0x098a  */
    /* JADX WARNING: Removed duplicated region for block: B:278:0x09a3  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x09ba  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x09d2  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0a04  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0a1a  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x0a30  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x0a46  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0a51  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0b36 A:{SYNTHETIC, Splitter: B:327:0x0b36} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:202:0x07d4  */
    /* JADX WARNING: Removed duplicated region for block: B:205:0x07f1  */
    /* JADX WARNING: Removed duplicated region for block: B:248:0x091e A:{SYNTHETIC, Splitter: B:248:0x091e} */
    /* JADX WARNING: Removed duplicated region for block: B:257:0x0950  */
    /* JADX WARNING: Removed duplicated region for block: B:271:0x098a  */
    /* JADX WARNING: Removed duplicated region for block: B:278:0x09a3  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x09ba  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x09d2  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0a04  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0a1a  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x0a30  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x0a46  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0a51  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0b36 A:{SYNTHETIC, Splitter: B:327:0x0b36} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x02c9  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x031d  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x035d  */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:459:0x0f9e  */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x0477  */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x04aa  */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x0531  */
    /* JADX WARNING: Removed duplicated region for block: B:125:0x057f  */
    /* JADX WARNING: Removed duplicated region for block: B:130:0x0598  */
    /* JADX WARNING: Removed duplicated region for block: B:139:0x05c9 A:{Catch:{ Throwable -> 0x1010 }} */
    /* JADX WARNING: Removed duplicated region for block: B:479:0x101d  */
    /* JADX WARNING: Removed duplicated region for block: B:142:0x060a  */
    /* JADX WARNING: Removed duplicated region for block: B:144:0x061e  */
    /* JADX WARNING: Removed duplicated region for block: B:166:0x06df A:{SYNTHETIC, Splitter: B:166:0x06df} */
    /* JADX WARNING: Removed duplicated region for block: B:169:0x06f5  */
    /* JADX WARNING: Removed duplicated region for block: B:177:0x0738  */
    /* JADX WARNING: Removed duplicated region for block: B:202:0x07d4  */
    /* JADX WARNING: Removed duplicated region for block: B:205:0x07f1  */
    /* JADX WARNING: Removed duplicated region for block: B:248:0x091e A:{SYNTHETIC, Splitter: B:248:0x091e} */
    /* JADX WARNING: Removed duplicated region for block: B:257:0x0950  */
    /* JADX WARNING: Removed duplicated region for block: B:271:0x098a  */
    /* JADX WARNING: Removed duplicated region for block: B:278:0x09a3  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x09ba  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x09d2  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0a04  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0a1a  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x0a30  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x0a46  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0a51  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0b36 A:{SYNTHETIC, Splitter: B:327:0x0b36} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:248:0x091e A:{SYNTHETIC, Splitter: B:248:0x091e} */
    /* JADX WARNING: Removed duplicated region for block: B:257:0x0950  */
    /* JADX WARNING: Removed duplicated region for block: B:271:0x098a  */
    /* JADX WARNING: Removed duplicated region for block: B:278:0x09a3  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x09ba  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x09d2  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0a04  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0a1a  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x0a30  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x0a46  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0a51  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0b36 A:{SYNTHETIC, Splitter: B:327:0x0b36} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x03d7  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x03f0  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x040e  */
    /* JADX WARNING: Removed duplicated region for block: B:125:0x057f  */
    /* JADX WARNING: Removed duplicated region for block: B:130:0x0598  */
    /* JADX WARNING: Removed duplicated region for block: B:139:0x05c9 A:{Catch:{ Throwable -> 0x1010 }} */
    /* JADX WARNING: Removed duplicated region for block: B:142:0x060a  */
    /* JADX WARNING: Removed duplicated region for block: B:479:0x101d  */
    /* JADX WARNING: Removed duplicated region for block: B:144:0x061e  */
    /* JADX WARNING: Removed duplicated region for block: B:166:0x06df A:{SYNTHETIC, Splitter: B:166:0x06df} */
    /* JADX WARNING: Removed duplicated region for block: B:169:0x06f5  */
    /* JADX WARNING: Removed duplicated region for block: B:177:0x0738  */
    /* JADX WARNING: Removed duplicated region for block: B:202:0x07d4  */
    /* JADX WARNING: Removed duplicated region for block: B:205:0x07f1  */
    /* JADX WARNING: Removed duplicated region for block: B:248:0x091e A:{SYNTHETIC, Splitter: B:248:0x091e} */
    /* JADX WARNING: Removed duplicated region for block: B:257:0x0950  */
    /* JADX WARNING: Removed duplicated region for block: B:271:0x098a  */
    /* JADX WARNING: Removed duplicated region for block: B:278:0x09a3  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x09ba  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x09d2  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0a04  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0a1a  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x0a30  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x0a46  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0a51  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0b36 A:{SYNTHETIC, Splitter: B:327:0x0b36} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:166:0x06df A:{SYNTHETIC, Splitter: B:166:0x06df} */
    /* JADX WARNING: Removed duplicated region for block: B:169:0x06f5  */
    /* JADX WARNING: Removed duplicated region for block: B:177:0x0738  */
    /* JADX WARNING: Removed duplicated region for block: B:202:0x07d4  */
    /* JADX WARNING: Removed duplicated region for block: B:205:0x07f1  */
    /* JADX WARNING: Removed duplicated region for block: B:248:0x091e A:{SYNTHETIC, Splitter: B:248:0x091e} */
    /* JADX WARNING: Removed duplicated region for block: B:257:0x0950  */
    /* JADX WARNING: Removed duplicated region for block: B:271:0x098a  */
    /* JADX WARNING: Removed duplicated region for block: B:278:0x09a3  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x09ba  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x09d2  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0a04  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0a1a  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x0a30  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x0a46  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0a51  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0b36 A:{SYNTHETIC, Splitter: B:327:0x0b36} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:248:0x091e A:{SYNTHETIC, Splitter: B:248:0x091e} */
    /* JADX WARNING: Removed duplicated region for block: B:257:0x0950  */
    /* JADX WARNING: Removed duplicated region for block: B:271:0x098a  */
    /* JADX WARNING: Removed duplicated region for block: B:278:0x09a3  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x09ba  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x09d2  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0a04  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0a1a  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x0a30  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x0a46  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0a51  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0b36 A:{SYNTHETIC, Splitter: B:327:0x0b36} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x0531  */
    /* JADX WARNING: Removed duplicated region for block: B:125:0x057f  */
    /* JADX WARNING: Removed duplicated region for block: B:130:0x0598  */
    /* JADX WARNING: Removed duplicated region for block: B:139:0x05c9 A:{Catch:{ Throwable -> 0x1010 }} */
    /* JADX WARNING: Removed duplicated region for block: B:479:0x101d  */
    /* JADX WARNING: Removed duplicated region for block: B:142:0x060a  */
    /* JADX WARNING: Removed duplicated region for block: B:144:0x061e  */
    /* JADX WARNING: Removed duplicated region for block: B:166:0x06df A:{SYNTHETIC, Splitter: B:166:0x06df} */
    /* JADX WARNING: Removed duplicated region for block: B:169:0x06f5  */
    /* JADX WARNING: Removed duplicated region for block: B:177:0x0738  */
    /* JADX WARNING: Removed duplicated region for block: B:202:0x07d4  */
    /* JADX WARNING: Removed duplicated region for block: B:205:0x07f1  */
    /* JADX WARNING: Removed duplicated region for block: B:248:0x091e A:{SYNTHETIC, Splitter: B:248:0x091e} */
    /* JADX WARNING: Removed duplicated region for block: B:257:0x0950  */
    /* JADX WARNING: Removed duplicated region for block: B:271:0x098a  */
    /* JADX WARNING: Removed duplicated region for block: B:278:0x09a3  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x09ba  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x09d2  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0a04  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0a1a  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x0a30  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x0a46  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0a51  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0b36 A:{SYNTHETIC, Splitter: B:327:0x0b36} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x0477  */
    /* JADX WARNING: Removed duplicated region for block: B:459:0x0f9e  */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x04aa  */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x0531  */
    /* JADX WARNING: Removed duplicated region for block: B:125:0x057f  */
    /* JADX WARNING: Removed duplicated region for block: B:130:0x0598  */
    /* JADX WARNING: Removed duplicated region for block: B:139:0x05c9 A:{Catch:{ Throwable -> 0x1010 }} */
    /* JADX WARNING: Removed duplicated region for block: B:142:0x060a  */
    /* JADX WARNING: Removed duplicated region for block: B:479:0x101d  */
    /* JADX WARNING: Removed duplicated region for block: B:144:0x061e  */
    /* JADX WARNING: Removed duplicated region for block: B:166:0x06df A:{SYNTHETIC, Splitter: B:166:0x06df} */
    /* JADX WARNING: Removed duplicated region for block: B:169:0x06f5  */
    /* JADX WARNING: Removed duplicated region for block: B:177:0x0738  */
    /* JADX WARNING: Removed duplicated region for block: B:202:0x07d4  */
    /* JADX WARNING: Removed duplicated region for block: B:205:0x07f1  */
    /* JADX WARNING: Removed duplicated region for block: B:248:0x091e A:{SYNTHETIC, Splitter: B:248:0x091e} */
    /* JADX WARNING: Removed duplicated region for block: B:257:0x0950  */
    /* JADX WARNING: Removed duplicated region for block: B:271:0x098a  */
    /* JADX WARNING: Removed duplicated region for block: B:278:0x09a3  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x09ba  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x09d2  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0a04  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0a1a  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x0a30  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x0a46  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0a51  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0b36 A:{SYNTHETIC, Splitter: B:327:0x0b36} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:459:0x0f9e  */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x0477  */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x04aa  */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x0531  */
    /* JADX WARNING: Removed duplicated region for block: B:125:0x057f  */
    /* JADX WARNING: Removed duplicated region for block: B:130:0x0598  */
    /* JADX WARNING: Removed duplicated region for block: B:139:0x05c9 A:{Catch:{ Throwable -> 0x1010 }} */
    /* JADX WARNING: Removed duplicated region for block: B:479:0x101d  */
    /* JADX WARNING: Removed duplicated region for block: B:142:0x060a  */
    /* JADX WARNING: Removed duplicated region for block: B:144:0x061e  */
    /* JADX WARNING: Removed duplicated region for block: B:166:0x06df A:{SYNTHETIC, Splitter: B:166:0x06df} */
    /* JADX WARNING: Removed duplicated region for block: B:169:0x06f5  */
    /* JADX WARNING: Removed duplicated region for block: B:177:0x0738  */
    /* JADX WARNING: Removed duplicated region for block: B:202:0x07d4  */
    /* JADX WARNING: Removed duplicated region for block: B:205:0x07f1  */
    /* JADX WARNING: Removed duplicated region for block: B:248:0x091e A:{SYNTHETIC, Splitter: B:248:0x091e} */
    /* JADX WARNING: Removed duplicated region for block: B:257:0x0950  */
    /* JADX WARNING: Removed duplicated region for block: B:271:0x098a  */
    /* JADX WARNING: Removed duplicated region for block: B:278:0x09a3  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x09ba  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x09d2  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0a04  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0a1a  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x0a30  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x0a46  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0a51  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0b36 A:{SYNTHETIC, Splitter: B:327:0x0b36} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x0477  */
    /* JADX WARNING: Removed duplicated region for block: B:459:0x0f9e  */
    /* JADX WARNING: Removed duplicated region for block: B:95:0x04aa  */
    /* JADX WARNING: Removed duplicated region for block: B:112:0x0531  */
    /* JADX WARNING: Removed duplicated region for block: B:125:0x057f  */
    /* JADX WARNING: Removed duplicated region for block: B:130:0x0598  */
    /* JADX WARNING: Removed duplicated region for block: B:139:0x05c9 A:{Catch:{ Throwable -> 0x1010 }} */
    /* JADX WARNING: Removed duplicated region for block: B:142:0x060a  */
    /* JADX WARNING: Removed duplicated region for block: B:479:0x101d  */
    /* JADX WARNING: Removed duplicated region for block: B:144:0x061e  */
    /* JADX WARNING: Removed duplicated region for block: B:166:0x06df A:{SYNTHETIC, Splitter: B:166:0x06df} */
    /* JADX WARNING: Removed duplicated region for block: B:169:0x06f5  */
    /* JADX WARNING: Removed duplicated region for block: B:177:0x0738  */
    /* JADX WARNING: Removed duplicated region for block: B:202:0x07d4  */
    /* JADX WARNING: Removed duplicated region for block: B:205:0x07f1  */
    /* JADX WARNING: Removed duplicated region for block: B:248:0x091e A:{SYNTHETIC, Splitter: B:248:0x091e} */
    /* JADX WARNING: Removed duplicated region for block: B:257:0x0950  */
    /* JADX WARNING: Removed duplicated region for block: B:271:0x098a  */
    /* JADX WARNING: Removed duplicated region for block: B:278:0x09a3  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x09ba  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x09d2  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0a04  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0a1a  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x0a30  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x0a46  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0a51  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0b36 A:{SYNTHETIC, Splitter: B:327:0x0b36} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:271:0x098a  */
    /* JADX WARNING: Removed duplicated region for block: B:278:0x09a3  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x09ba  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x09d2  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0a04  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0a1a  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x0a30  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x0a46  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0a51  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0b36 A:{SYNTHETIC, Splitter: B:327:0x0b36} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:202:0x07d4  */
    /* JADX WARNING: Removed duplicated region for block: B:205:0x07f1  */
    /* JADX WARNING: Removed duplicated region for block: B:248:0x091e A:{SYNTHETIC, Splitter: B:248:0x091e} */
    /* JADX WARNING: Removed duplicated region for block: B:257:0x0950  */
    /* JADX WARNING: Removed duplicated region for block: B:271:0x098a  */
    /* JADX WARNING: Removed duplicated region for block: B:278:0x09a3  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x09ba  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x09d2  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0a04  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0a1a  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x0a30  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x0a46  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0a51  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0b36 A:{SYNTHETIC, Splitter: B:327:0x0b36} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:248:0x091e A:{SYNTHETIC, Splitter: B:248:0x091e} */
    /* JADX WARNING: Removed duplicated region for block: B:257:0x0950  */
    /* JADX WARNING: Removed duplicated region for block: B:271:0x098a  */
    /* JADX WARNING: Removed duplicated region for block: B:278:0x09a3  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x09ba  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x09d2  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0a04  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0a1a  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x0a30  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x0a46  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0a51  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0b36 A:{SYNTHETIC, Splitter: B:327:0x0b36} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:202:0x07d4  */
    /* JADX WARNING: Removed duplicated region for block: B:205:0x07f1  */
    /* JADX WARNING: Removed duplicated region for block: B:248:0x091e A:{SYNTHETIC, Splitter: B:248:0x091e} */
    /* JADX WARNING: Removed duplicated region for block: B:257:0x0950  */
    /* JADX WARNING: Removed duplicated region for block: B:271:0x098a  */
    /* JADX WARNING: Removed duplicated region for block: B:278:0x09a3  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x09ba  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x09d2  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0a04  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0a1a  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x0a30  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x0a46  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0a51  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0b36 A:{SYNTHETIC, Splitter: B:327:0x0b36} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:166:0x06df A:{SYNTHETIC, Splitter: B:166:0x06df} */
    /* JADX WARNING: Removed duplicated region for block: B:169:0x06f5  */
    /* JADX WARNING: Removed duplicated region for block: B:177:0x0738  */
    /* JADX WARNING: Removed duplicated region for block: B:202:0x07d4  */
    /* JADX WARNING: Removed duplicated region for block: B:205:0x07f1  */
    /* JADX WARNING: Removed duplicated region for block: B:248:0x091e A:{SYNTHETIC, Splitter: B:248:0x091e} */
    /* JADX WARNING: Removed duplicated region for block: B:257:0x0950  */
    /* JADX WARNING: Removed duplicated region for block: B:271:0x098a  */
    /* JADX WARNING: Removed duplicated region for block: B:278:0x09a3  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x09ba  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x09d2  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0a04  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0a1a  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x0a30  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x0a46  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0a51  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0b36 A:{SYNTHETIC, Splitter: B:327:0x0b36} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:271:0x098a  */
    /* JADX WARNING: Removed duplicated region for block: B:278:0x09a3  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x09ba  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x09d2  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0a04  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0a1a  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x0a30  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x0a46  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0a51  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0b36 A:{SYNTHETIC, Splitter: B:327:0x0b36} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:248:0x091e A:{SYNTHETIC, Splitter: B:248:0x091e} */
    /* JADX WARNING: Removed duplicated region for block: B:257:0x0950  */
    /* JADX WARNING: Removed duplicated region for block: B:271:0x098a  */
    /* JADX WARNING: Removed duplicated region for block: B:278:0x09a3  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x09ba  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x09d2  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0a04  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0a1a  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x0a30  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x0a46  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0a51  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0b36 A:{SYNTHETIC, Splitter: B:327:0x0b36} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:248:0x091e A:{SYNTHETIC, Splitter: B:248:0x091e} */
    /* JADX WARNING: Removed duplicated region for block: B:257:0x0950  */
    /* JADX WARNING: Removed duplicated region for block: B:271:0x098a  */
    /* JADX WARNING: Removed duplicated region for block: B:278:0x09a3  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x09ba  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x09d2  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0a04  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0a1a  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x0a30  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x0a46  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0a51  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0b36 A:{SYNTHETIC, Splitter: B:327:0x0b36} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:248:0x091e A:{SYNTHETIC, Splitter: B:248:0x091e} */
    /* JADX WARNING: Removed duplicated region for block: B:257:0x0950  */
    /* JADX WARNING: Removed duplicated region for block: B:271:0x098a  */
    /* JADX WARNING: Removed duplicated region for block: B:278:0x09a3  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x09ba  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x09d2  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0a04  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0a1a  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x0a30  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x0a46  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0a51  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0b36 A:{SYNTHETIC, Splitter: B:327:0x0b36} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* JADX WARNING: Removed duplicated region for block: B:248:0x091e A:{SYNTHETIC, Splitter: B:248:0x091e} */
    /* JADX WARNING: Removed duplicated region for block: B:257:0x0950  */
    /* JADX WARNING: Removed duplicated region for block: B:271:0x098a  */
    /* JADX WARNING: Removed duplicated region for block: B:278:0x09a3  */
    /* JADX WARNING: Removed duplicated region for block: B:283:0x09ba  */
    /* JADX WARNING: Removed duplicated region for block: B:286:0x09d2  */
    /* JADX WARNING: Removed duplicated region for block: B:289:0x0a04  */
    /* JADX WARNING: Removed duplicated region for block: B:292:0x0a1a  */
    /* JADX WARNING: Removed duplicated region for block: B:295:0x0a30  */
    /* JADX WARNING: Removed duplicated region for block: B:298:0x0a46  */
    /* JADX WARNING: Removed duplicated region for block: B:300:0x0a51  */
    /* JADX WARNING: Removed duplicated region for block: B:327:0x0b36 A:{SYNTHETIC, Splitter: B:327:0x0b36} */
    /* JADX WARNING: Removed duplicated region for block: B:345:0x0bb7  */
    /* JADX WARNING: Removed duplicated region for block: B:351:0x0bdd  */
    /* JADX WARNING: Removed duplicated region for block: B:357:0x0c03 A:{SYNTHETIC, Splitter: B:357:0x0c03} */
    /* JADX WARNING: Removed duplicated region for block: B:367:0x0c5e  */
    /* JADX WARNING: Removed duplicated region for block: B:380:0x0cd8 A:{SYNTHETIC, Splitter: B:380:0x0cd8} */
    /* JADX WARNING: Removed duplicated region for block: B:387:0x0d40  */
    /* JADX WARNING: Removed duplicated region for block: B:405:0x0dc3 A:{Catch:{ Throwable -> 0x126e }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "JianHui.Yu@Plf.SDK, 2016-05-20 : Modify for Longshot", property = OppoRomType.ROM)
    private void startOtherServices() {
        Throwable e;
        OppoMultimediaService multimediaService;
        LocationManagerService location;
        CountryDetectorService countryDetector;
        ILockSettings lockSettings;
        IPerfServiceManager perfServiceMgr;
        AssetAtlasService atlas;
        MediaRouterService mediaRouter;
        OppoUsageService usageService;
        OppoService oppoService;
        Throwable e2;
        final MmsServiceBroker mmsService;
        Configuration config;
        DisplayMetrics metrics;
        Theme systemTheme;
        final NetworkManagementService networkManagementF;
        final NetworkStatsService networkStatsF;
        final NetworkPolicyManagerService networkPolicyF;
        final ConnectivityService connectivityF;
        final NetworkScoreService networkScoreF;
        final LocationManagerService locationF;
        final CountryDetectorService countryDetectorF;
        final NetworkTimeUpdateService networkTimeUpdaterF;
        final CommonTimeManagementService commonTimeMgmtServiceF;
        final AssetAtlasService atlasF;
        final InputManagerService inputManagerF;
        final TelephonyRegistry telephonyRegistryF;
        final MediaRouterService mediaRouterF;
        MmsServiceBroker mmsServiceF;
        final OppoMultimediaService multimediaServiceF;
        final IPerfServiceManager perfServiceF;
        final RunningBoosterService runningboosterF;
        final OppoUsageService usageServiceF;
        final OppoService mOppoService;
        AgingCriticalEvent instance;
        String str;
        String[] strArr;
        OppoUsageService oppoUsageService;
        HypnusService hypnusService;
        HypnusService hypnusService2;
        OppoService oppoService2;
        OppoMultimediaService oppoMultimediaService;
        CabcService cabcService;
        CabcService cabcService2;
        NetworkTimeUpdateService networkTimeUpdateService;
        CommonTimeManagementService commonTimeManagementService;
        AssetAtlasService assetAtlasService;
        MtkHdmiManagerService mtkHdmiManagerService;
        MtkHdmiManagerService mtkHdmiManagerService2;
        NetworkPolicyManagerService networkPolicyManagerService;
        ConnectivityService connectivityService;
        CountryDetectorService countryDetectorService;
        HardwarePropertiesManagerService hardwarePropertiesManagerService;
        HardwarePropertiesManagerService hardwarePropertiesManagerService2;
        final Context context = this.mSystemContext;
        VibratorService vibrator = null;
        IMountService mountService = null;
        NetworkManagementService networkManagement = null;
        NetworkStatsService networkStats = null;
        NetworkPolicyManagerService networkPolicy = null;
        ConnectivityService connectivity = null;
        NetworkScoreService networkScore = null;
        WindowManagerService wm = null;
        NetworkTimeUpdateService networkTimeUpdater = null;
        CommonTimeManagementService commonTimeMgmtService = null;
        InputManagerService inputManager = null;
        TelephonyRegistry telephonyRegistry = null;
        RunningBoosterService runningbooster = null;
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
        boolean disableSamplingProfiler = SystemProperties.getBoolean("config.disable_samplingprof", false);
        boolean isEmulator = SystemProperties.get("ro.kernel.qemu").equals("1");
        try {
            VibratorService vibratorService;
            ConsumerIrService consumerIrService;
            Slog.i(TAG, "Reading configuration...");
            SystemConfig.getInstance();
            traceBeginAndSlog("StartSchedulingPolicyService");
            ServiceManager.addService("scheduling_policy", new SchedulingPolicyService());
            Trace.traceEnd(2097152);
            this.mSystemServiceManager.startService(TelecomLoaderService.class);
            traceBeginAndSlog("StartTelephonyRegistry");
            TelephonyRegistry telephonyRegistry2 = new TelephonyRegistry(context);
            try {
                ServiceManager.addService("telephony.registry", telephonyRegistry2);
                Trace.traceEnd(2097152);
                traceBeginAndSlog("StartEntropyMixer");
                this.mEntropyMixer = new EntropyMixer(context);
                Trace.traceEnd(2097152);
                this.mContentResolver = context.getContentResolver();
                Slog.i(TAG, "Camera Service");
                this.mSystemServiceManager.startService(CameraService.class);
                traceBeginAndSlog("StartAccountManagerService");
                this.mSystemServiceManager.startService(ACCOUNT_SERVICE_CLASS);
                Trace.traceEnd(2097152);
                traceBeginAndSlog("StartContentService");
                this.mSystemServiceManager.startService(CONTENT_SERVICE_CLASS);
                Trace.traceEnd(2097152);
                traceBeginAndSlog("InstallSystemProviders");
                this.mActivityManagerService.installSystemProviders();
                Trace.traceEnd(2097152);
                traceBeginAndSlog("StartVibratorService");
                vibratorService = new VibratorService(context);
                try {
                    ServiceManager.addService("vibrator", vibratorService);
                    Trace.traceEnd(2097152);
                    traceBeginAndSlog("StartConsumerIrService");
                    consumerIrService = new ConsumerIrService(context);
                } catch (RuntimeException e3) {
                    e = e3;
                    telephonyRegistry = telephonyRegistry2;
                    vibrator = vibratorService;
                    Slog.e("System", "******************************************");
                    Slog.e("System", "************ Failure starting core service", e);
                    multimediaService = null;
                    location = null;
                    countryDetector = null;
                    lockSettings = null;
                    perfServiceMgr = null;
                    atlas = null;
                    mediaRouter = null;
                    usageService = null;
                    oppoService = null;
                    if (this.mFactoryTestMode != 1) {
                    }
                    Slog.i(TAG, "displayReady");
                    wm.displayReady();
                    try {
                        Slog.i(TAG, "mount service");
                        this.mSystemServiceManager.startService(MOUNT_SERVICE_CLASS);
                        mountService = Stub.asInterface(ServiceManager.getService(OppoProcessManager.RESUME_REASON_MOUNT_STR));
                    } catch (Throwable e22) {
                        reportWtf("starting Mount Service", e22);
                    }
                    this.mSystemServiceManager.startService(UiModeManagerService.class);
                    if (!this.mOnlyCore) {
                    }
                    Trace.traceBegin(2097152, "PerformFstrimIfNeeded");
                    Slog.i(TAG, "performFstrimIfNeeded");
                    this.mPackageManagerService.performFstrimIfNeeded();
                    Trace.traceEnd(2097152);
                    if (this.mFactoryTestMode != 1) {
                    }
                    this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                    }
                    VMRuntime.getRuntime().startJitCompilation();
                    mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                    this.mSystemServiceManager.startService(RetailDemoModeService.class);
                    Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                    vibrator.systemReady();
                    Trace.traceEnd(2097152);
                    Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                    if (lockSettings != null) {
                    }
                    Trace.traceEnd(2097152);
                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                    this.mSystemServiceManager.startBootPhase(500);
                    Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                    Slog.i(TAG, "wms systemReady");
                    wm.systemReady();
                    Trace.traceEnd(2097152);
                    config = wm.computeNewConfiguration();
                    metrics = new DisplayMetrics();
                    ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                    context.getResources().updateConfiguration(config, metrics);
                    systemTheme = context.getTheme();
                    if (systemTheme.getChangingConfigurations() != 0) {
                    }
                    Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                    Slog.i(TAG, "power manager systemReady");
                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                    Trace.traceEnd(2097152);
                    Trace.traceEnd(2097152);
                    Slog.i(TAG, "Fingerprint systemReady");
                    this.mFingerprintService.systemReady();
                    if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                    }
                    Slog.i(TAG, "Secrecy systemReady");
                    this.mSecrecyService.systemReady();
                    Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                    Slog.i(TAG, "Package systemReady");
                    this.mPackageManagerService.systemReady();
                    Trace.traceEnd(2097152);
                    Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                    Slog.i(TAG, "DisplayManager systemReady");
                    this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                    Trace.traceEnd(2097152);
                    networkManagementF = networkManagement;
                    networkStatsF = networkStats;
                    networkPolicyF = networkPolicy;
                    connectivityF = connectivity;
                    networkScoreF = networkScore;
                    locationF = location;
                    countryDetectorF = countryDetector;
                    networkTimeUpdaterF = networkTimeUpdater;
                    commonTimeMgmtServiceF = commonTimeMgmtService;
                    atlasF = atlas;
                    inputManagerF = inputManager;
                    telephonyRegistryF = telephonyRegistry;
                    mediaRouterF = mediaRouter;
                    mmsServiceF = mmsService;
                    multimediaServiceF = multimediaService;
                    perfServiceF = perfServiceMgr;
                    runningboosterF = runningbooster;
                    usageServiceF = usageService;
                    Slog.i(TAG, "Ams systemReady");
                    mOppoService = oppoService;
                    this.mActivityManagerService.systemReady(new Runnable() {
                        public void run() {
                            Slog.i(SystemServer.TAG, "Making services ready");
                            SystemServer.this.mSystemServiceManager.startBootPhase(SystemService.PHASE_ACTIVITY_MANAGER_READY);
                            Trace.traceBegin(2097152, "PhaseActivityManagerReady");
                            Trace.traceBegin(2097152, "StartObservingNativeCrashes");
                            try {
                                SystemServer.this.mActivityManagerService.startObservingNativeCrashes();
                            } catch (Throwable e) {
                                SystemServer.this.reportWtf("observing native crashes", e);
                            }
                            Trace.traceEnd(2097152);
                            if (!SystemServer.this.mOnlyCore) {
                                Slog.i(SystemServer.TAG, "WebViewFactory preparation");
                                Trace.traceBegin(2097152, "WebViewFactoryPreparation");
                                SystemServer.this.mWebViewUpdateService.prepareWebViewInSystemServer();
                                Trace.traceEnd(2097152);
                            }
                            try {
                                SystemServer.startPhone(context);
                            } catch (Throwable e2) {
                                SystemServer.this.reportWtf("starting startPhone", e2);
                            }
                            Trace.traceBegin(2097152, "StartSystemUI");
                            try {
                                SystemServer.startSystemUi(context);
                            } catch (Throwable e22) {
                                SystemServer.this.reportWtf("starting System UI", e22);
                            }
                            Trace.traceEnd(2097152);
                            Trace.traceBegin(2097152, "MakeNetworkScoreReady");
                            try {
                                if (networkScoreF != null) {
                                    networkScoreF.systemReady();
                                }
                            } catch (Throwable e222) {
                                SystemServer.this.reportWtf("making Network Score Service ready", e222);
                            }
                            Trace.traceEnd(2097152);
                            Trace.traceBegin(2097152, "MakeNetworkManagementServiceReady");
                            try {
                                if (networkManagementF != null) {
                                    networkManagementF.systemReady();
                                }
                            } catch (Throwable e2222) {
                                SystemServer.this.reportWtf("making Network Managment Service ready", e2222);
                            }
                            Trace.traceEnd(2097152);
                            SystemServer.addBootEvent("SystemServer:NetworkManagementService systemReady");
                            Trace.traceBegin(2097152, "MakeNetworkStatsServiceReady");
                            try {
                                if (networkStatsF != null) {
                                    networkStatsF.systemReady();
                                }
                            } catch (Throwable e22222) {
                                SystemServer.this.reportWtf("making Network Stats Service ready", e22222);
                            }
                            Trace.traceEnd(2097152);
                            SystemServer.addBootEvent("SystemServer:NetworkStatsService systemReady");
                            Trace.traceBegin(2097152, "MakeNetworkPolicyServiceReady");
                            try {
                                if (networkPolicyF != null) {
                                    networkPolicyF.systemReady();
                                }
                            } catch (Throwable e222222) {
                                SystemServer.this.reportWtf("making Network Policy Service ready", e222222);
                            }
                            Trace.traceEnd(2097152);
                            SystemServer.addBootEvent("SystemServer:NetworkPolicyManagerService systemReady");
                            Trace.traceBegin(2097152, "MakeConnectivityServiceReady");
                            try {
                                if (connectivityF != null) {
                                    connectivityF.systemReady();
                                }
                            } catch (Throwable e2222222) {
                                SystemServer.this.reportWtf("making Connectivity Service ready", e2222222);
                            }
                            Trace.traceEnd(2097152);
                            SystemServer.addBootEvent("SystemServer:ConnectivityService systemReady");
                            Watchdog.getInstance().start();
                            Trace.traceEnd(2097152);
                            Trace.traceBegin(2097152, "PhaseThirdPartyAppsCanStart");
                            SystemServer.this.mSystemServiceManager.startBootPhase(600);
                            try {
                                if (locationF != null) {
                                    locationF.systemRunning();
                                }
                            } catch (Throwable e22222222) {
                                SystemServer.this.reportWtf("Notifying Location Service running", e22222222);
                            }
                            if (!(!"user".equals(Build.TYPE) ? "userdebug".equals(Build.TYPE) : true)) {
                                SystemServer.this.testSystemServer(context);
                            }
                            try {
                                if (countryDetectorF != null) {
                                    countryDetectorF.systemRunning();
                                }
                            } catch (Throwable e222222222) {
                                SystemServer.this.reportWtf("Notifying CountryDetectorService running", e222222222);
                            }
                            try {
                                if (networkTimeUpdaterF != null) {
                                    networkTimeUpdaterF.systemRunning();
                                }
                            } catch (Throwable e2222222222) {
                                SystemServer.this.reportWtf("Notifying NetworkTimeService running", e2222222222);
                            }
                            try {
                                if (commonTimeMgmtServiceF != null) {
                                    commonTimeMgmtServiceF.systemRunning();
                                }
                            } catch (Throwable e22222222222) {
                                SystemServer.this.reportWtf("Notifying CommonTimeManagementService running", e22222222222);
                            }
                            try {
                                if (atlasF != null) {
                                    atlasF.systemRunning();
                                }
                            } catch (Throwable e222222222222) {
                                SystemServer.this.reportWtf("Notifying AssetAtlasService running", e222222222222);
                            }
                            try {
                                if (inputManagerF != null) {
                                    inputManagerF.systemRunning();
                                }
                            } catch (Throwable e2222222222222) {
                                SystemServer.this.reportWtf("Notifying InputManagerService running", e2222222222222);
                            }
                            try {
                                if (telephonyRegistryF != null) {
                                    telephonyRegistryF.systemRunning();
                                }
                            } catch (Throwable e22222222222222) {
                                SystemServer.this.reportWtf("Notifying TelephonyRegistry running", e22222222222222);
                            }
                            try {
                                if (mediaRouterF != null) {
                                    mediaRouterF.systemRunning();
                                }
                            } catch (Throwable e222222222222222) {
                                SystemServer.this.reportWtf("Notifying MediaRouterService running", e222222222222222);
                            }
                            try {
                                if (mmsService != null) {
                                    mmsService.systemRunning();
                                }
                            } catch (Throwable e2222222222222222) {
                                SystemServer.this.reportWtf("Notifying MmsService running", e2222222222222222);
                            }
                            try {
                                if (networkScoreF != null) {
                                    networkScoreF.systemRunning();
                                }
                            } catch (Throwable e22222222222222222) {
                                SystemServer.this.reportWtf("Notifying NetworkScoreService running", e22222222222222222);
                            }
                            try {
                                if (multimediaServiceF != null) {
                                    multimediaServiceF.systemRunning();
                                }
                            } catch (Throwable e222222222222222222) {
                                SystemServer.this.reportWtf("Notifying NetworkScoreService running", e222222222222222222);
                            }
                            try {
                                if (mOppoService != null) {
                                    mOppoService.systemReady();
                                }
                            } catch (Throwable e2222222222222222222) {
                                SystemServer.this.reportWtf("Notifying mOppoService running", e2222222222222222222);
                            }
                            Trace.traceEnd(2097152);
                            SystemServer.addBootEvent("SystemServer:PhaseThirdPartyAppsCanStart");
                            if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                                try {
                                    Trace.traceBegin(2097152, "MakePerfServiceReady");
                                    if (perfServiceF != null) {
                                        perfServiceF.systemReady();
                                    }
                                    Trace.traceEnd(2097152);
                                } catch (Throwable e22222222222222222222) {
                                    SystemServer.this.reportWtf("making PerfServiceManager ready", e22222222222222222222);
                                }
                            }
                            try {
                                if (runningboosterF != null) {
                                    runningboosterF.systemRunning();
                                }
                            } catch (Throwable e222222222222222222222) {
                                SystemServer.this.reportWtf("Notifying RunningBoosterService running", e222222222222222222222);
                            }
                            try {
                                if (usageServiceF != null) {
                                    usageServiceF.systemReady();
                                }
                            } catch (Throwable e2222222222222222222222) {
                                SystemServer.this.reportWtf("Notifying OppoUsageService running", e2222222222222222222222);
                            }
                        }
                    });
                }
            } catch (RuntimeException e4) {
                e = e4;
                telephonyRegistry = telephonyRegistry2;
                Slog.e("System", "******************************************");
                Slog.e("System", "************ Failure starting core service", e);
                multimediaService = null;
                location = null;
                countryDetector = null;
                lockSettings = null;
                perfServiceMgr = null;
                atlas = null;
                mediaRouter = null;
                usageService = null;
                oppoService = null;
                if (this.mFactoryTestMode != 1) {
                }
                Slog.i(TAG, "displayReady");
                wm.displayReady();
                Slog.i(TAG, "mount service");
                this.mSystemServiceManager.startService(MOUNT_SERVICE_CLASS);
                mountService = Stub.asInterface(ServiceManager.getService(OppoProcessManager.RESUME_REASON_MOUNT_STR));
                this.mSystemServiceManager.startService(UiModeManagerService.class);
                if (this.mOnlyCore) {
                }
                Trace.traceBegin(2097152, "PerformFstrimIfNeeded");
                Slog.i(TAG, "performFstrimIfNeeded");
                this.mPackageManagerService.performFstrimIfNeeded();
                Trace.traceEnd(2097152);
                if (this.mFactoryTestMode != 1) {
                }
                this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                }
                VMRuntime.getRuntime().startJitCompilation();
                mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                this.mSystemServiceManager.startService(RetailDemoModeService.class);
                Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                vibrator.systemReady();
                Trace.traceEnd(2097152);
                Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                if (lockSettings != null) {
                }
                Trace.traceEnd(2097152);
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                this.mSystemServiceManager.startBootPhase(500);
                Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                Slog.i(TAG, "wms systemReady");
                wm.systemReady();
                Trace.traceEnd(2097152);
                config = wm.computeNewConfiguration();
                metrics = new DisplayMetrics();
                ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                context.getResources().updateConfiguration(config, metrics);
                systemTheme = context.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                }
                Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                Slog.i(TAG, "power manager systemReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                Trace.traceEnd(2097152);
                Trace.traceEnd(2097152);
                Slog.i(TAG, "Fingerprint systemReady");
                this.mFingerprintService.systemReady();
                if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                }
                Slog.i(TAG, "Secrecy systemReady");
                this.mSecrecyService.systemReady();
                Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                Slog.i(TAG, "Package systemReady");
                this.mPackageManagerService.systemReady();
                Trace.traceEnd(2097152);
                Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                Slog.i(TAG, "DisplayManager systemReady");
                this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                Trace.traceEnd(2097152);
                networkManagementF = networkManagement;
                networkStatsF = networkStats;
                networkPolicyF = networkPolicy;
                connectivityF = connectivity;
                networkScoreF = networkScore;
                locationF = location;
                countryDetectorF = countryDetector;
                networkTimeUpdaterF = networkTimeUpdater;
                commonTimeMgmtServiceF = commonTimeMgmtService;
                atlasF = atlas;
                inputManagerF = inputManager;
                telephonyRegistryF = telephonyRegistry;
                mediaRouterF = mediaRouter;
                mmsServiceF = mmsService;
                multimediaServiceF = multimediaService;
                perfServiceF = perfServiceMgr;
                runningboosterF = runningbooster;
                usageServiceF = usageService;
                Slog.i(TAG, "Ams systemReady");
                mOppoService = oppoService;
                this.mActivityManagerService.systemReady(/* anonymous class already generated */);
            }
            ConsumerIrService consumerIr;
            try {
                ServiceManager.addService("consumer_ir", consumerIrService);
                Trace.traceEnd(2097152);
                traceBeginAndSlog("StartAlarmManagerService");
                Slog.i(TAG, "AlarmManager Service");
                this.mSystemServiceManager.startService(AlarmManagerService.class);
                Trace.traceEnd(2097152);
                traceBeginAndSlog("InitWatchdog");
                Watchdog.getInstance().init(context, this.mActivityManagerService);
                Trace.traceEnd(2097152);
                traceBeginAndSlog("StartInputManagerService");
                InputManagerService inputManagerService = new InputManagerService(context);
                try {
                    Trace.traceEnd(2097152);
                    traceBeginAndSlog("StartWindowManagerService");
                    wm = WindowManagerService.main(context, inputManagerService, this.mFactoryTestMode != 1, !this.mFirstBoot, this.mOnlyCore);
                    ServiceManager.addService("window", wm);
                    ServiceManager.addService("input", inputManagerService);
                    Trace.traceEnd(2097152);
                    traceBeginAndSlog("StartVrManagerService");
                    this.mSystemServiceManager.startService(VrManagerService.class);
                    Trace.traceEnd(2097152);
                    this.mActivityManagerService.setWindowManager(wm);
                    inputManagerService.setWindowManagerCallbacks(wm.getInputMonitor());
                    inputManagerService.start();
                    this.mDisplayManagerService.windowManagerAndInputReady();
                    if (isEmulator) {
                        Slog.i(TAG, "No Bluetooth Service (emulator)");
                    } else if (this.mFactoryTestMode == 1) {
                        Slog.i(TAG, "No Bluetooth Service (factory test)");
                    } else if (!context.getPackageManager().hasSystemFeature("android.hardware.bluetooth")) {
                        Slog.i(TAG, "No Bluetooth Service (Bluetooth Hardware Not Present)");
                    } else if (disableBluetooth) {
                        Slog.i(TAG, "Bluetooth Service disabled by config");
                    } else {
                        this.mSystemServiceManager.startService(BluetoothService.class);
                    }
                    traceBeginAndSlog("ConnectivityMetricsLoggerService");
                    this.mSystemServiceManager.startService(MetricsLoggerService.class);
                    Trace.traceEnd(2097152);
                    traceBeginAndSlog("IpConnectivityMetrics");
                    this.mSystemServiceManager.startService(IpConnectivityMetrics.class);
                    Trace.traceEnd(2097152);
                    traceBeginAndSlog("PinnerService");
                    this.mSystemServiceManager.startService(PinnerService.class);
                    Trace.traceEnd(2097152);
                    Slog.i(TAG, "ColorOs Service");
                    SystemServer.addService(context);
                    consumerIr = consumerIrService;
                    telephonyRegistry = telephonyRegistry2;
                    inputManager = inputManagerService;
                    vibrator = vibratorService;
                } catch (RuntimeException e5) {
                    e = e5;
                    consumerIr = consumerIrService;
                    telephonyRegistry = telephonyRegistry2;
                    inputManager = inputManagerService;
                    vibrator = vibratorService;
                    Slog.e("System", "******************************************");
                    Slog.e("System", "************ Failure starting core service", e);
                    multimediaService = null;
                    location = null;
                    countryDetector = null;
                    lockSettings = null;
                    perfServiceMgr = null;
                    atlas = null;
                    mediaRouter = null;
                    usageService = null;
                    oppoService = null;
                    if (this.mFactoryTestMode != 1) {
                    }
                    Slog.i(TAG, "displayReady");
                    wm.displayReady();
                    Slog.i(TAG, "mount service");
                    this.mSystemServiceManager.startService(MOUNT_SERVICE_CLASS);
                    mountService = Stub.asInterface(ServiceManager.getService(OppoProcessManager.RESUME_REASON_MOUNT_STR));
                    this.mSystemServiceManager.startService(UiModeManagerService.class);
                    if (this.mOnlyCore) {
                    }
                    Trace.traceBegin(2097152, "PerformFstrimIfNeeded");
                    Slog.i(TAG, "performFstrimIfNeeded");
                    this.mPackageManagerService.performFstrimIfNeeded();
                    Trace.traceEnd(2097152);
                    if (this.mFactoryTestMode != 1) {
                    }
                    this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                    }
                    VMRuntime.getRuntime().startJitCompilation();
                    mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                    this.mSystemServiceManager.startService(RetailDemoModeService.class);
                    Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                    vibrator.systemReady();
                    Trace.traceEnd(2097152);
                    Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                    if (lockSettings != null) {
                    }
                    Trace.traceEnd(2097152);
                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                    this.mSystemServiceManager.startBootPhase(500);
                    Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                    Slog.i(TAG, "wms systemReady");
                    wm.systemReady();
                    Trace.traceEnd(2097152);
                    config = wm.computeNewConfiguration();
                    metrics = new DisplayMetrics();
                    ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                    context.getResources().updateConfiguration(config, metrics);
                    systemTheme = context.getTheme();
                    if (systemTheme.getChangingConfigurations() != 0) {
                    }
                    Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                    Slog.i(TAG, "power manager systemReady");
                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                    Trace.traceEnd(2097152);
                    Trace.traceEnd(2097152);
                    Slog.i(TAG, "Fingerprint systemReady");
                    this.mFingerprintService.systemReady();
                    if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                    }
                    Slog.i(TAG, "Secrecy systemReady");
                    this.mSecrecyService.systemReady();
                    Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                    Slog.i(TAG, "Package systemReady");
                    this.mPackageManagerService.systemReady();
                    Trace.traceEnd(2097152);
                    Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                    Slog.i(TAG, "DisplayManager systemReady");
                    this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                    Trace.traceEnd(2097152);
                    networkManagementF = networkManagement;
                    networkStatsF = networkStats;
                    networkPolicyF = networkPolicy;
                    connectivityF = connectivity;
                    networkScoreF = networkScore;
                    locationF = location;
                    countryDetectorF = countryDetector;
                    networkTimeUpdaterF = networkTimeUpdater;
                    commonTimeMgmtServiceF = commonTimeMgmtService;
                    atlasF = atlas;
                    inputManagerF = inputManager;
                    telephonyRegistryF = telephonyRegistry;
                    mediaRouterF = mediaRouter;
                    mmsServiceF = mmsService;
                    multimediaServiceF = multimediaService;
                    perfServiceF = perfServiceMgr;
                    runningboosterF = runningbooster;
                    usageServiceF = usageService;
                    Slog.i(TAG, "Ams systemReady");
                    mOppoService = oppoService;
                    this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                }
            } catch (RuntimeException e6) {
                e = e6;
                consumerIr = consumerIrService;
                telephonyRegistry = telephonyRegistry2;
                vibrator = vibratorService;
                Slog.e("System", "******************************************");
                Slog.e("System", "************ Failure starting core service", e);
                multimediaService = null;
                location = null;
                countryDetector = null;
                lockSettings = null;
                perfServiceMgr = null;
                atlas = null;
                mediaRouter = null;
                usageService = null;
                oppoService = null;
                if (this.mFactoryTestMode != 1) {
                }
                Slog.i(TAG, "displayReady");
                wm.displayReady();
                Slog.i(TAG, "mount service");
                this.mSystemServiceManager.startService(MOUNT_SERVICE_CLASS);
                mountService = Stub.asInterface(ServiceManager.getService(OppoProcessManager.RESUME_REASON_MOUNT_STR));
                this.mSystemServiceManager.startService(UiModeManagerService.class);
                if (this.mOnlyCore) {
                }
                Trace.traceBegin(2097152, "PerformFstrimIfNeeded");
                Slog.i(TAG, "performFstrimIfNeeded");
                this.mPackageManagerService.performFstrimIfNeeded();
                Trace.traceEnd(2097152);
                if (this.mFactoryTestMode != 1) {
                }
                this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                }
                VMRuntime.getRuntime().startJitCompilation();
                mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                this.mSystemServiceManager.startService(RetailDemoModeService.class);
                Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                vibrator.systemReady();
                Trace.traceEnd(2097152);
                Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                if (lockSettings != null) {
                }
                Trace.traceEnd(2097152);
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                this.mSystemServiceManager.startBootPhase(500);
                Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                Slog.i(TAG, "wms systemReady");
                wm.systemReady();
                Trace.traceEnd(2097152);
                config = wm.computeNewConfiguration();
                metrics = new DisplayMetrics();
                ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                context.getResources().updateConfiguration(config, metrics);
                systemTheme = context.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                }
                Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                Slog.i(TAG, "power manager systemReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                Trace.traceEnd(2097152);
                Trace.traceEnd(2097152);
                Slog.i(TAG, "Fingerprint systemReady");
                this.mFingerprintService.systemReady();
                if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                }
                Slog.i(TAG, "Secrecy systemReady");
                this.mSecrecyService.systemReady();
                Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                Slog.i(TAG, "Package systemReady");
                this.mPackageManagerService.systemReady();
                Trace.traceEnd(2097152);
                Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                Slog.i(TAG, "DisplayManager systemReady");
                this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                Trace.traceEnd(2097152);
                networkManagementF = networkManagement;
                networkStatsF = networkStats;
                networkPolicyF = networkPolicy;
                connectivityF = connectivity;
                networkScoreF = networkScore;
                locationF = location;
                countryDetectorF = countryDetector;
                networkTimeUpdaterF = networkTimeUpdater;
                commonTimeMgmtServiceF = commonTimeMgmtService;
                atlasF = atlas;
                inputManagerF = inputManager;
                telephonyRegistryF = telephonyRegistry;
                mediaRouterF = mediaRouter;
                mmsServiceF = mmsService;
                multimediaServiceF = multimediaService;
                perfServiceF = perfServiceMgr;
                runningboosterF = runningbooster;
                usageServiceF = usageService;
                Slog.i(TAG, "Ams systemReady");
                mOppoService = oppoService;
                this.mActivityManagerService.systemReady(/* anonymous class already generated */);
            }
        } catch (RuntimeException e7) {
            e = e7;
            Slog.e("System", "******************************************");
            Slog.e("System", "************ Failure starting core service", e);
            multimediaService = null;
            location = null;
            countryDetector = null;
            lockSettings = null;
            perfServiceMgr = null;
            atlas = null;
            mediaRouter = null;
            usageService = null;
            oppoService = null;
            if (this.mFactoryTestMode != 1) {
            }
            Slog.i(TAG, "displayReady");
            wm.displayReady();
            Slog.i(TAG, "mount service");
            this.mSystemServiceManager.startService(MOUNT_SERVICE_CLASS);
            mountService = Stub.asInterface(ServiceManager.getService(OppoProcessManager.RESUME_REASON_MOUNT_STR));
            this.mSystemServiceManager.startService(UiModeManagerService.class);
            if (this.mOnlyCore) {
            }
            Trace.traceBegin(2097152, "PerformFstrimIfNeeded");
            Slog.i(TAG, "performFstrimIfNeeded");
            this.mPackageManagerService.performFstrimIfNeeded();
            Trace.traceEnd(2097152);
            if (this.mFactoryTestMode != 1) {
            }
            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
            if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
            }
            VMRuntime.getRuntime().startJitCompilation();
            mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
            this.mSystemServiceManager.startService(RetailDemoModeService.class);
            Trace.traceBegin(2097152, "MakeVibratorServiceReady");
            vibrator.systemReady();
            Trace.traceEnd(2097152);
            Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
            if (lockSettings != null) {
            }
            Trace.traceEnd(2097152);
            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
            this.mSystemServiceManager.startBootPhase(500);
            Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
            Slog.i(TAG, "wms systemReady");
            wm.systemReady();
            Trace.traceEnd(2097152);
            config = wm.computeNewConfiguration();
            metrics = new DisplayMetrics();
            ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
            context.getResources().updateConfiguration(config, metrics);
            systemTheme = context.getTheme();
            if (systemTheme.getChangingConfigurations() != 0) {
            }
            Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
            Slog.i(TAG, "power manager systemReady");
            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
            Trace.traceEnd(2097152);
            Trace.traceEnd(2097152);
            Slog.i(TAG, "Fingerprint systemReady");
            this.mFingerprintService.systemReady();
            if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
            }
            Slog.i(TAG, "Secrecy systemReady");
            this.mSecrecyService.systemReady();
            Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
            Slog.i(TAG, "Package systemReady");
            this.mPackageManagerService.systemReady();
            Trace.traceEnd(2097152);
            Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
            Slog.i(TAG, "DisplayManager systemReady");
            this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
            Trace.traceEnd(2097152);
            networkManagementF = networkManagement;
            networkStatsF = networkStats;
            networkPolicyF = networkPolicy;
            connectivityF = connectivity;
            networkScoreF = networkScore;
            locationF = location;
            countryDetectorF = countryDetector;
            networkTimeUpdaterF = networkTimeUpdater;
            commonTimeMgmtServiceF = commonTimeMgmtService;
            atlasF = atlas;
            inputManagerF = inputManager;
            telephonyRegistryF = telephonyRegistry;
            mediaRouterF = mediaRouter;
            mmsServiceF = mmsService;
            multimediaServiceF = multimediaService;
            perfServiceF = perfServiceMgr;
            runningboosterF = runningbooster;
            usageServiceF = usageService;
            Slog.i(TAG, "Ams systemReady");
            mOppoService = oppoService;
            this.mActivityManagerService.systemReady(/* anonymous class already generated */);
        }
        multimediaService = null;
        location = null;
        countryDetector = null;
        lockSettings = null;
        perfServiceMgr = null;
        atlas = null;
        mediaRouter = null;
        usageService = null;
        oppoService = null;
        if (this.mFactoryTestMode != 1) {
            this.mSystemServiceManager.startService(InputMethodManagerService.Lifecycle.class);
            traceBeginAndSlog("StartAccessibilityManagerService");
            try {
                ServiceManager.addService("accessibility", new AccessibilityManagerService(context));
            } catch (Throwable e222) {
                reportWtf("starting Accessibility Manager", e222);
            }
            Trace.traceEnd(2097152);
        }
        try {
            Slog.i(TAG, "displayReady");
            wm.displayReady();
        } catch (Throwable e2222) {
            reportWtf("making display ready", e2222);
        }
        if (!(this.mFactoryTestMode == 1 || disableStorage || "0".equals(SystemProperties.get("system_init.startmountservice")))) {
            Slog.i(TAG, "mount service");
            this.mSystemServiceManager.startService(MOUNT_SERVICE_CLASS);
            mountService = Stub.asInterface(ServiceManager.getService(OppoProcessManager.RESUME_REASON_MOUNT_STR));
        }
        this.mSystemServiceManager.startService(UiModeManagerService.class);
        if (this.mOnlyCore) {
            Trace.traceBegin(2097152, "UpdatePackagesIfNeeded");
            try {
                this.mPackageManagerService.updatePackagesIfNeeded();
            } catch (Throwable e22222) {
                reportWtf("update packages", e22222);
            }
            Trace.traceEnd(2097152);
        }
        Trace.traceBegin(2097152, "PerformFstrimIfNeeded");
        try {
            Slog.i(TAG, "performFstrimIfNeeded");
            this.mPackageManagerService.performFstrimIfNeeded();
        } catch (Throwable e222222) {
            reportWtf("performing fstrim", e222222);
        }
        Trace.traceEnd(2097152);
        if (this.mFactoryTestMode != 1) {
            if (!disableNonCoreServices) {
                traceBeginAndSlog("StartLockSettingsService");
                try {
                    this.mSystemServiceManager.startService(LOCK_SETTINGS_SERVICE_CLASS);
                    lockSettings = ILockSettings.Stub.asInterface(ServiceManager.getService("lock_settings"));
                } catch (Throwable e2222222) {
                    reportWtf("starting LockSettingsService service", e2222222);
                }
                Trace.traceEnd(2097152);
                if (!SystemProperties.get(PERSISTENT_DATA_BLOCK_PROP).equals(IElsaManager.EMPTY_PACKAGE)) {
                    this.mSystemServiceManager.startService(PersistentDataBlockService.class);
                }
                this.mSystemServiceManager.startService(DeviceIdleController.class);
                Slog.i(TAG, "DevicePolicyManagerService");
                this.mSystemServiceManager.startService(DevicePolicyManagerService.Lifecycle.class);
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
                        e2222222 = th;
                        statusBarManagerService = colorStatusBarManagerService;
                        reportWtf("starting StatusBarManagerService", e2222222);
                        Trace.traceEnd(2097152);
                        if (!disableNonCoreServices) {
                        }
                        if (!disableNetwork) {
                        }
                        this.mSystemServiceManager.startService(TextServicesManagerService.Lifecycle.class);
                        if (!disableNetwork) {
                        }
                        if (!disableNonCoreServices) {
                        }
                        if (!disableNonCoreServices) {
                        }
                        Trace.traceBegin(2097152, "WaitForAsecScan");
                        try {
                            mountService.waitForAsecScan();
                        } catch (RemoteException e8) {
                        }
                        Trace.traceEnd(2097152);
                        Slog.i(TAG, "Oppo Expand Service");
                        ServiceManager.addService("OPPOExService", new OppoExService(context, wm));
                        if (this.mOppoLightsService != null) {
                        }
                        Slog.i(TAG, "NotificationManagerService");
                        this.mSystemServiceManager.startService(NotificationManagerService.class);
                        networkPolicy.bindNotificationManager(INotificationManager.Stub.asInterface(ServiceManager.getService(NotificationManagerService.NOTIFICATON_TITLE_NAME)));
                        if (context.getPackageManager().hasSystemFeature("oppo.support.single.partition")) {
                        }
                        if (!disableLocation) {
                        }
                        traceBeginAndSlog("StartSearchManagerService");
                        try {
                            this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                        } catch (Throwable e22222222) {
                            reportWtf("starting Search Service", e22222222);
                        }
                        Trace.traceEnd(2097152);
                        try {
                            Slog.i(TAG, "Search Engine Service");
                            ServiceManager.addService("search_engine", new SearchEngineManagerService(context));
                        } catch (Throwable e222222222) {
                            reportWtf("starting Search Engine Service", e222222222);
                        }
                        this.mSystemServiceManager.startService(DropBoxManagerService.class);
                        instance = AgingCriticalEvent.getInstance();
                        str = AgingCriticalEvent.EVENT_SYSTEM_BOOTUP;
                        strArr = new String[1];
                        strArr[0] = "systemserver pid:" + Process.myPid();
                        instance.writeEvent(str, strArr);
                        traceBeginAndSlog("StartWallpaperManagerService");
                        this.mSystemServiceManager.startService(WALLPAPER_SERVICE_CLASS);
                        Trace.traceEnd(2097152);
                        traceBeginAndSlog("StartAudioService");
                        this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                        Trace.traceEnd(2097152);
                        if ("1".equals(SystemProperties.get("ro.mtk_sensorhub_support"))) {
                        }
                        if (!disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartWiredAccessoryManager");
                        inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                        Trace.traceEnd(2097152);
                        if (!disableNonCoreServices) {
                        }
                        this.mSystemServiceManager.startService(TwilightService.class);
                        if (NightDisplayController.isAvailable(context)) {
                        }
                        this.mSystemServiceManager.startService(JobSchedulerService.class);
                        this.mSystemServiceManager.startService(SoundTriggerService.class);
                        if (!disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartDiskStatsService");
                        ServiceManager.addService("diskstats", new DiskStatsService(context));
                        Trace.traceEnd(2097152);
                        Slog.i(TAG, "OPPO Usage Service");
                        oppoUsageService = new OppoUsageService(context);
                        try {
                            ServiceManager.addService("usage", oppoUsageService);
                            usageService = oppoUsageService;
                        } catch (Throwable th2) {
                            e222222222 = th2;
                            usageService = oppoUsageService;
                            Slog.e(TAG, "Start OppoUsageService failed for:", e222222222);
                            Slog.i(TAG, "Hypnus Service");
                            hypnusService = new HypnusService(context);
                            ServiceManager.addService("hypnus", hypnusService);
                            hypnusService2 = hypnusService;
                            Slog.i(TAG, "Oppo Service");
                            oppoService2 = new OppoService(context);
                            ServiceManager.addService("OPPO", oppoService2);
                            oppoService = oppoService2;
                            Slog.i(TAG, "+OppoMultimediaService");
                            oppoMultimediaService = new OppoMultimediaService(context);
                            ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                            Slog.i(TAG, "-OppoMultimediaService");
                            multimediaService = oppoMultimediaService;
                            Slog.i(TAG, "Oppo Customize Service");
                            ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                            if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                            }
                            traceBeginAndSlog("CabcService");
                            Slog.i(TAG, "Cabc Service");
                            cabcService = new CabcService(context);
                            ServiceManager.addService("cabc", cabcService);
                            cabcService2 = cabcService;
                            if (!disableSamplingProfiler) {
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
                                Trace.traceEnd(2097152);
                                traceBeginAndSlog("StartCommonTimeManagementService");
                                commonTimeManagementService = new CommonTimeManagementService(context);
                                ServiceManager.addService("commontime_management", commonTimeManagementService);
                                commonTimeMgmtService = commonTimeManagementService;
                                Trace.traceEnd(2097152);
                                if (!disableNetwork) {
                                }
                                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                                if (!disableNonCoreServices) {
                                }
                                traceBeginAndSlog("StartAssetAtlasService");
                                assetAtlasService = new AssetAtlasService(context);
                                ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService);
                                atlas = assetAtlasService;
                                Trace.traceEnd(2097152);
                                if (!disableNonCoreServices) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                                }
                                this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                                Slog.i(TAG, "Media Session Service");
                                this.mSystemServiceManager.startService(MediaSessionService.class);
                                if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                                }
                                if (!disableNonCoreServices) {
                                }
                                Slog.i(TAG, "Oppo Engineer Service");
                                this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                this.mSystemServiceManager.startService(LauncherAppsService.class);
                                if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                                }
                                Slog.i(TAG, "HDMI Manager Service");
                                mtkHdmiManagerService = new MtkHdmiManagerService(context);
                                ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                                mtkHdmiManagerService2 = mtkHdmiManagerService;
                                if (!"no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                                }
                                if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                                }
                                if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                                }
                                Slog.i(TAG, "Secrecy Service");
                                this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                                this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                                if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                                }
                                VMRuntime.getRuntime().startJitCompilation();
                                mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                this.mSystemServiceManager.startService(RetailDemoModeService.class);
                                Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                                vibrator.systemReady();
                                Trace.traceEnd(2097152);
                                Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                                if (lockSettings != null) {
                                }
                                Trace.traceEnd(2097152);
                                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                                this.mSystemServiceManager.startBootPhase(500);
                                Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                                Slog.i(TAG, "wms systemReady");
                                wm.systemReady();
                                Trace.traceEnd(2097152);
                                config = wm.computeNewConfiguration();
                                metrics = new DisplayMetrics();
                                ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                                context.getResources().updateConfiguration(config, metrics);
                                systemTheme = context.getTheme();
                                if (systemTheme.getChangingConfigurations() != 0) {
                                }
                                Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                                Slog.i(TAG, "power manager systemReady");
                                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                Trace.traceEnd(2097152);
                                Trace.traceEnd(2097152);
                                Slog.i(TAG, "Fingerprint systemReady");
                                this.mFingerprintService.systemReady();
                                if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                                }
                                Slog.i(TAG, "Secrecy systemReady");
                                this.mSecrecyService.systemReady();
                                Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                                Slog.i(TAG, "Package systemReady");
                                this.mPackageManagerService.systemReady();
                                Trace.traceEnd(2097152);
                                Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                                Slog.i(TAG, "DisplayManager systemReady");
                                this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                                Trace.traceEnd(2097152);
                                networkManagementF = networkManagement;
                                networkStatsF = networkStats;
                                networkPolicyF = networkPolicy;
                                connectivityF = connectivity;
                                networkScoreF = networkScore;
                                locationF = location;
                                countryDetectorF = countryDetector;
                                networkTimeUpdaterF = networkTimeUpdater;
                                commonTimeMgmtServiceF = commonTimeMgmtService;
                                atlasF = atlas;
                                inputManagerF = inputManager;
                                telephonyRegistryF = telephonyRegistry;
                                mediaRouterF = mediaRouter;
                                mmsServiceF = mmsService;
                                multimediaServiceF = multimediaService;
                                perfServiceF = perfServiceMgr;
                                runningboosterF = runningbooster;
                                usageServiceF = usageService;
                                Slog.i(TAG, "Ams systemReady");
                                mOppoService = oppoService;
                                this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                            }
                            Trace.traceEnd(2097152);
                            traceBeginAndSlog("StartCommonTimeManagementService");
                            commonTimeManagementService = new CommonTimeManagementService(context);
                            ServiceManager.addService("commontime_management", commonTimeManagementService);
                            commonTimeMgmtService = commonTimeManagementService;
                            Trace.traceEnd(2097152);
                            if (disableNetwork) {
                            }
                            this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartAssetAtlasService");
                            assetAtlasService = new AssetAtlasService(context);
                            try {
                                ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService);
                                atlas = assetAtlasService;
                            } catch (Throwable th4) {
                                e222222222 = th4;
                                atlas = assetAtlasService;
                                reportWtf("starting AssetAtlasService", e222222222);
                                Trace.traceEnd(2097152);
                                if (disableNonCoreServices) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                                }
                                this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                                Slog.i(TAG, "Media Session Service");
                                this.mSystemServiceManager.startService(MediaSessionService.class);
                                if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                                }
                                if (disableNonCoreServices) {
                                }
                                Slog.i(TAG, "Oppo Engineer Service");
                                this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                this.mSystemServiceManager.startService(LauncherAppsService.class);
                                if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                                }
                                Slog.i(TAG, "HDMI Manager Service");
                                mtkHdmiManagerService = new MtkHdmiManagerService(context);
                                ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                                mtkHdmiManagerService2 = mtkHdmiManagerService;
                                if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                                }
                                if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                                }
                                if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                                }
                                Slog.i(TAG, "Secrecy Service");
                                this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                                this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                                if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                                }
                                VMRuntime.getRuntime().startJitCompilation();
                                mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                this.mSystemServiceManager.startService(RetailDemoModeService.class);
                                Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                                vibrator.systemReady();
                                Trace.traceEnd(2097152);
                                Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                                if (lockSettings != null) {
                                }
                                Trace.traceEnd(2097152);
                                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                                this.mSystemServiceManager.startBootPhase(500);
                                Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                                Slog.i(TAG, "wms systemReady");
                                wm.systemReady();
                                Trace.traceEnd(2097152);
                                config = wm.computeNewConfiguration();
                                metrics = new DisplayMetrics();
                                ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                                context.getResources().updateConfiguration(config, metrics);
                                systemTheme = context.getTheme();
                                if (systemTheme.getChangingConfigurations() != 0) {
                                }
                                Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                                Slog.i(TAG, "power manager systemReady");
                                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                Trace.traceEnd(2097152);
                                Trace.traceEnd(2097152);
                                Slog.i(TAG, "Fingerprint systemReady");
                                this.mFingerprintService.systemReady();
                                if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                                }
                                Slog.i(TAG, "Secrecy systemReady");
                                this.mSecrecyService.systemReady();
                                Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                                Slog.i(TAG, "Package systemReady");
                                this.mPackageManagerService.systemReady();
                                Trace.traceEnd(2097152);
                                Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                                Slog.i(TAG, "DisplayManager systemReady");
                                this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                                Trace.traceEnd(2097152);
                                networkManagementF = networkManagement;
                                networkStatsF = networkStats;
                                networkPolicyF = networkPolicy;
                                connectivityF = connectivity;
                                networkScoreF = networkScore;
                                locationF = location;
                                countryDetectorF = countryDetector;
                                networkTimeUpdaterF = networkTimeUpdater;
                                commonTimeMgmtServiceF = commonTimeMgmtService;
                                atlasF = atlas;
                                inputManagerF = inputManager;
                                telephonyRegistryF = telephonyRegistry;
                                mediaRouterF = mediaRouter;
                                mmsServiceF = mmsService;
                                multimediaServiceF = multimediaService;
                                perfServiceF = perfServiceMgr;
                                runningboosterF = runningbooster;
                                usageServiceF = usageService;
                                Slog.i(TAG, "Ams systemReady");
                                mOppoService = oppoService;
                                this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                            }
                            Trace.traceEnd(2097152);
                            if (disableNonCoreServices) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                            }
                            this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                            Slog.i(TAG, "Media Session Service");
                            this.mSystemServiceManager.startService(MediaSessionService.class);
                            if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                            }
                            if (disableNonCoreServices) {
                            }
                            Slog.i(TAG, "Oppo Engineer Service");
                            this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                            this.mSystemServiceManager.startService(LauncherAppsService.class);
                            if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                            }
                            Slog.i(TAG, "HDMI Manager Service");
                            mtkHdmiManagerService = new MtkHdmiManagerService(context);
                            try {
                                ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                                mtkHdmiManagerService2 = mtkHdmiManagerService;
                            } catch (Throwable th5) {
                                e222222222 = th5;
                                mtkHdmiManagerService2 = mtkHdmiManagerService;
                                Slog.e(TAG, "Failure starting MtkHdmiManager", e222222222);
                                if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                                }
                                if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                                }
                                if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                                }
                                Slog.i(TAG, "Secrecy Service");
                                this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                                this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                                if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                                }
                                VMRuntime.getRuntime().startJitCompilation();
                                mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                this.mSystemServiceManager.startService(RetailDemoModeService.class);
                                Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                                vibrator.systemReady();
                                Trace.traceEnd(2097152);
                                Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                                if (lockSettings != null) {
                                }
                                Trace.traceEnd(2097152);
                                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                                this.mSystemServiceManager.startBootPhase(500);
                                Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                                Slog.i(TAG, "wms systemReady");
                                wm.systemReady();
                                Trace.traceEnd(2097152);
                                config = wm.computeNewConfiguration();
                                metrics = new DisplayMetrics();
                                ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                                context.getResources().updateConfiguration(config, metrics);
                                systemTheme = context.getTheme();
                                if (systemTheme.getChangingConfigurations() != 0) {
                                }
                                Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                                Slog.i(TAG, "power manager systemReady");
                                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                Trace.traceEnd(2097152);
                                Trace.traceEnd(2097152);
                                Slog.i(TAG, "Fingerprint systemReady");
                                this.mFingerprintService.systemReady();
                                if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                                }
                                Slog.i(TAG, "Secrecy systemReady");
                                this.mSecrecyService.systemReady();
                                Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                                Slog.i(TAG, "Package systemReady");
                                this.mPackageManagerService.systemReady();
                                Trace.traceEnd(2097152);
                                Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                                Slog.i(TAG, "DisplayManager systemReady");
                                this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                                Trace.traceEnd(2097152);
                                networkManagementF = networkManagement;
                                networkStatsF = networkStats;
                                networkPolicyF = networkPolicy;
                                connectivityF = connectivity;
                                networkScoreF = networkScore;
                                locationF = location;
                                countryDetectorF = countryDetector;
                                networkTimeUpdaterF = networkTimeUpdater;
                                commonTimeMgmtServiceF = commonTimeMgmtService;
                                atlasF = atlas;
                                inputManagerF = inputManager;
                                telephonyRegistryF = telephonyRegistry;
                                mediaRouterF = mediaRouter;
                                mmsServiceF = mmsService;
                                multimediaServiceF = multimediaService;
                                perfServiceF = perfServiceMgr;
                                runningboosterF = runningbooster;
                                usageServiceF = usageService;
                                Slog.i(TAG, "Ams systemReady");
                                mOppoService = oppoService;
                                this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                            }
                            if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                            }
                            if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                            }
                            if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                            }
                            Slog.i(TAG, "Secrecy Service");
                            this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                            if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                            }
                            VMRuntime.getRuntime().startJitCompilation();
                            mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                            this.mSystemServiceManager.startService(RetailDemoModeService.class);
                            Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                            vibrator.systemReady();
                            Trace.traceEnd(2097152);
                            Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                            if (lockSettings != null) {
                            }
                            Trace.traceEnd(2097152);
                            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                            this.mSystemServiceManager.startBootPhase(500);
                            Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                            Slog.i(TAG, "wms systemReady");
                            wm.systemReady();
                            Trace.traceEnd(2097152);
                            config = wm.computeNewConfiguration();
                            metrics = new DisplayMetrics();
                            ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                            context.getResources().updateConfiguration(config, metrics);
                            systemTheme = context.getTheme();
                            if (systemTheme.getChangingConfigurations() != 0) {
                            }
                            Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                            Slog.i(TAG, "power manager systemReady");
                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                            Trace.traceEnd(2097152);
                            Trace.traceEnd(2097152);
                            Slog.i(TAG, "Fingerprint systemReady");
                            this.mFingerprintService.systemReady();
                            if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                            }
                            Slog.i(TAG, "Secrecy systemReady");
                            this.mSecrecyService.systemReady();
                            Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                            Slog.i(TAG, "Package systemReady");
                            this.mPackageManagerService.systemReady();
                            Trace.traceEnd(2097152);
                            Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                            Slog.i(TAG, "DisplayManager systemReady");
                            this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                            Trace.traceEnd(2097152);
                            networkManagementF = networkManagement;
                            networkStatsF = networkStats;
                            networkPolicyF = networkPolicy;
                            connectivityF = connectivity;
                            networkScoreF = networkScore;
                            locationF = location;
                            countryDetectorF = countryDetector;
                            networkTimeUpdaterF = networkTimeUpdater;
                            commonTimeMgmtServiceF = commonTimeMgmtService;
                            atlasF = atlas;
                            inputManagerF = inputManager;
                            telephonyRegistryF = telephonyRegistry;
                            mediaRouterF = mediaRouter;
                            mmsServiceF = mmsService;
                            multimediaServiceF = multimediaService;
                            perfServiceF = perfServiceMgr;
                            runningboosterF = runningbooster;
                            usageServiceF = usageService;
                            Slog.i(TAG, "Ams systemReady");
                            mOppoService = oppoService;
                            this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                        }
                        Slog.i(TAG, "Hypnus Service");
                        hypnusService = new HypnusService(context);
                        try {
                            ServiceManager.addService("hypnus", hypnusService);
                            hypnusService2 = hypnusService;
                        } catch (Throwable th6) {
                            e222222222 = th6;
                            hypnusService2 = hypnusService;
                            reportWtf("starting Hypnus Service", e222222222);
                            Slog.i(TAG, "Oppo Service");
                            oppoService2 = new OppoService(context);
                            ServiceManager.addService("OPPO", oppoService2);
                            oppoService = oppoService2;
                            Slog.i(TAG, "+OppoMultimediaService");
                            oppoMultimediaService = new OppoMultimediaService(context);
                            ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                            Slog.i(TAG, "-OppoMultimediaService");
                            multimediaService = oppoMultimediaService;
                            Slog.i(TAG, "Oppo Customize Service");
                            ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                            if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                            }
                            traceBeginAndSlog("CabcService");
                            Slog.i(TAG, "Cabc Service");
                            cabcService = new CabcService(context);
                            ServiceManager.addService("cabc", cabcService);
                            cabcService2 = cabcService;
                            if (disableSamplingProfiler) {
                            }
                            traceBeginAndSlog("StartNetworkTimeUpdateService");
                            networkTimeUpdateService = new NetworkTimeUpdateService(context);
                            ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                            networkTimeUpdater = networkTimeUpdateService;
                            Trace.traceEnd(2097152);
                            traceBeginAndSlog("StartCommonTimeManagementService");
                            commonTimeManagementService = new CommonTimeManagementService(context);
                            ServiceManager.addService("commontime_management", commonTimeManagementService);
                            commonTimeMgmtService = commonTimeManagementService;
                            Trace.traceEnd(2097152);
                            if (disableNetwork) {
                            }
                            this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartAssetAtlasService");
                            assetAtlasService = new AssetAtlasService(context);
                            ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService);
                            atlas = assetAtlasService;
                            Trace.traceEnd(2097152);
                            if (disableNonCoreServices) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                            }
                            this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                            Slog.i(TAG, "Media Session Service");
                            this.mSystemServiceManager.startService(MediaSessionService.class);
                            if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                            }
                            if (disableNonCoreServices) {
                            }
                            Slog.i(TAG, "Oppo Engineer Service");
                            this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                            this.mSystemServiceManager.startService(LauncherAppsService.class);
                            if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                            }
                            Slog.i(TAG, "HDMI Manager Service");
                            mtkHdmiManagerService = new MtkHdmiManagerService(context);
                            ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                            mtkHdmiManagerService2 = mtkHdmiManagerService;
                            if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                            }
                            if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                            }
                            if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                            }
                            Slog.i(TAG, "Secrecy Service");
                            this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                            if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                            }
                            VMRuntime.getRuntime().startJitCompilation();
                            mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                            this.mSystemServiceManager.startService(RetailDemoModeService.class);
                            Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                            vibrator.systemReady();
                            Trace.traceEnd(2097152);
                            Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                            if (lockSettings != null) {
                            }
                            Trace.traceEnd(2097152);
                            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                            this.mSystemServiceManager.startBootPhase(500);
                            Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                            Slog.i(TAG, "wms systemReady");
                            wm.systemReady();
                            Trace.traceEnd(2097152);
                            config = wm.computeNewConfiguration();
                            metrics = new DisplayMetrics();
                            ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                            context.getResources().updateConfiguration(config, metrics);
                            systemTheme = context.getTheme();
                            if (systemTheme.getChangingConfigurations() != 0) {
                            }
                            Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                            Slog.i(TAG, "power manager systemReady");
                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                            Trace.traceEnd(2097152);
                            Trace.traceEnd(2097152);
                            Slog.i(TAG, "Fingerprint systemReady");
                            this.mFingerprintService.systemReady();
                            if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                            }
                            Slog.i(TAG, "Secrecy systemReady");
                            this.mSecrecyService.systemReady();
                            Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                            Slog.i(TAG, "Package systemReady");
                            this.mPackageManagerService.systemReady();
                            Trace.traceEnd(2097152);
                            Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                            Slog.i(TAG, "DisplayManager systemReady");
                            this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                            Trace.traceEnd(2097152);
                            networkManagementF = networkManagement;
                            networkStatsF = networkStats;
                            networkPolicyF = networkPolicy;
                            connectivityF = connectivity;
                            networkScoreF = networkScore;
                            locationF = location;
                            countryDetectorF = countryDetector;
                            networkTimeUpdaterF = networkTimeUpdater;
                            commonTimeMgmtServiceF = commonTimeMgmtService;
                            atlasF = atlas;
                            inputManagerF = inputManager;
                            telephonyRegistryF = telephonyRegistry;
                            mediaRouterF = mediaRouter;
                            mmsServiceF = mmsService;
                            multimediaServiceF = multimediaService;
                            perfServiceF = perfServiceMgr;
                            runningboosterF = runningbooster;
                            usageServiceF = usageService;
                            Slog.i(TAG, "Ams systemReady");
                            mOppoService = oppoService;
                            this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                        }
                        Slog.i(TAG, "Oppo Service");
                        oppoService2 = new OppoService(context);
                        try {
                            ServiceManager.addService("OPPO", oppoService2);
                            oppoService = oppoService2;
                        } catch (Throwable th7) {
                            e222222222 = th7;
                            oppoService = oppoService2;
                            Slog.e(TAG, "Failure starting Oppo Service", e222222222);
                            Slog.i(TAG, "+OppoMultimediaService");
                            oppoMultimediaService = new OppoMultimediaService(context);
                            ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                            Slog.i(TAG, "-OppoMultimediaService");
                            multimediaService = oppoMultimediaService;
                            Slog.i(TAG, "Oppo Customize Service");
                            ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                            if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                            }
                            traceBeginAndSlog("CabcService");
                            Slog.i(TAG, "Cabc Service");
                            cabcService = new CabcService(context);
                            ServiceManager.addService("cabc", cabcService);
                            cabcService2 = cabcService;
                            if (disableSamplingProfiler) {
                            }
                            traceBeginAndSlog("StartNetworkTimeUpdateService");
                            networkTimeUpdateService = new NetworkTimeUpdateService(context);
                            ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                            networkTimeUpdater = networkTimeUpdateService;
                            Trace.traceEnd(2097152);
                            traceBeginAndSlog("StartCommonTimeManagementService");
                            commonTimeManagementService = new CommonTimeManagementService(context);
                            ServiceManager.addService("commontime_management", commonTimeManagementService);
                            commonTimeMgmtService = commonTimeManagementService;
                            Trace.traceEnd(2097152);
                            if (disableNetwork) {
                            }
                            this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartAssetAtlasService");
                            assetAtlasService = new AssetAtlasService(context);
                            ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService);
                            atlas = assetAtlasService;
                            Trace.traceEnd(2097152);
                            if (disableNonCoreServices) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                            }
                            this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                            Slog.i(TAG, "Media Session Service");
                            this.mSystemServiceManager.startService(MediaSessionService.class);
                            if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                            }
                            if (disableNonCoreServices) {
                            }
                            Slog.i(TAG, "Oppo Engineer Service");
                            this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                            this.mSystemServiceManager.startService(LauncherAppsService.class);
                            if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                            }
                            Slog.i(TAG, "HDMI Manager Service");
                            mtkHdmiManagerService = new MtkHdmiManagerService(context);
                            ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                            mtkHdmiManagerService2 = mtkHdmiManagerService;
                            if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                            }
                            if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                            }
                            if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                            }
                            Slog.i(TAG, "Secrecy Service");
                            this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                            if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                            }
                            VMRuntime.getRuntime().startJitCompilation();
                            mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                            this.mSystemServiceManager.startService(RetailDemoModeService.class);
                            Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                            vibrator.systemReady();
                            Trace.traceEnd(2097152);
                            Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                            if (lockSettings != null) {
                            }
                            Trace.traceEnd(2097152);
                            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                            this.mSystemServiceManager.startBootPhase(500);
                            Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                            Slog.i(TAG, "wms systemReady");
                            wm.systemReady();
                            Trace.traceEnd(2097152);
                            config = wm.computeNewConfiguration();
                            metrics = new DisplayMetrics();
                            ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                            context.getResources().updateConfiguration(config, metrics);
                            systemTheme = context.getTheme();
                            if (systemTheme.getChangingConfigurations() != 0) {
                            }
                            Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                            Slog.i(TAG, "power manager systemReady");
                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                            Trace.traceEnd(2097152);
                            Trace.traceEnd(2097152);
                            Slog.i(TAG, "Fingerprint systemReady");
                            this.mFingerprintService.systemReady();
                            if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                            }
                            Slog.i(TAG, "Secrecy systemReady");
                            this.mSecrecyService.systemReady();
                            Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                            Slog.i(TAG, "Package systemReady");
                            this.mPackageManagerService.systemReady();
                            Trace.traceEnd(2097152);
                            Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                            Slog.i(TAG, "DisplayManager systemReady");
                            this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                            Trace.traceEnd(2097152);
                            networkManagementF = networkManagement;
                            networkStatsF = networkStats;
                            networkPolicyF = networkPolicy;
                            connectivityF = connectivity;
                            networkScoreF = networkScore;
                            locationF = location;
                            countryDetectorF = countryDetector;
                            networkTimeUpdaterF = networkTimeUpdater;
                            commonTimeMgmtServiceF = commonTimeMgmtService;
                            atlasF = atlas;
                            inputManagerF = inputManager;
                            telephonyRegistryF = telephonyRegistry;
                            mediaRouterF = mediaRouter;
                            mmsServiceF = mmsService;
                            multimediaServiceF = multimediaService;
                            perfServiceF = perfServiceMgr;
                            runningboosterF = runningbooster;
                            usageServiceF = usageService;
                            Slog.i(TAG, "Ams systemReady");
                            mOppoService = oppoService;
                            this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                        }
                        Slog.i(TAG, "+OppoMultimediaService");
                        oppoMultimediaService = new OppoMultimediaService(context);
                        try {
                            ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                            Slog.i(TAG, "-OppoMultimediaService");
                            multimediaService = oppoMultimediaService;
                        } catch (Throwable th8) {
                            e222222222 = th8;
                            multimediaService = oppoMultimediaService;
                            reportWtf("starting OppoMultimediaService", e222222222);
                            Slog.i(TAG, "Oppo Customize Service");
                            ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                            if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                            }
                            traceBeginAndSlog("CabcService");
                            Slog.i(TAG, "Cabc Service");
                            cabcService = new CabcService(context);
                            ServiceManager.addService("cabc", cabcService);
                            cabcService2 = cabcService;
                            if (disableSamplingProfiler) {
                            }
                            traceBeginAndSlog("StartNetworkTimeUpdateService");
                            networkTimeUpdateService = new NetworkTimeUpdateService(context);
                            ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                            networkTimeUpdater = networkTimeUpdateService;
                            Trace.traceEnd(2097152);
                            traceBeginAndSlog("StartCommonTimeManagementService");
                            commonTimeManagementService = new CommonTimeManagementService(context);
                            ServiceManager.addService("commontime_management", commonTimeManagementService);
                            commonTimeMgmtService = commonTimeManagementService;
                            Trace.traceEnd(2097152);
                            if (disableNetwork) {
                            }
                            this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartAssetAtlasService");
                            assetAtlasService = new AssetAtlasService(context);
                            ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService);
                            atlas = assetAtlasService;
                            Trace.traceEnd(2097152);
                            if (disableNonCoreServices) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                            }
                            this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                            Slog.i(TAG, "Media Session Service");
                            this.mSystemServiceManager.startService(MediaSessionService.class);
                            if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                            }
                            if (disableNonCoreServices) {
                            }
                            Slog.i(TAG, "Oppo Engineer Service");
                            this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                            this.mSystemServiceManager.startService(LauncherAppsService.class);
                            if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                            }
                            Slog.i(TAG, "HDMI Manager Service");
                            mtkHdmiManagerService = new MtkHdmiManagerService(context);
                            ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                            mtkHdmiManagerService2 = mtkHdmiManagerService;
                            if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                            }
                            if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                            }
                            if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                            }
                            Slog.i(TAG, "Secrecy Service");
                            this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                            if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                            }
                            VMRuntime.getRuntime().startJitCompilation();
                            mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                            this.mSystemServiceManager.startService(RetailDemoModeService.class);
                            Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                            vibrator.systemReady();
                            Trace.traceEnd(2097152);
                            Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                            if (lockSettings != null) {
                            }
                            Trace.traceEnd(2097152);
                            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                            this.mSystemServiceManager.startBootPhase(500);
                            Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                            Slog.i(TAG, "wms systemReady");
                            wm.systemReady();
                            Trace.traceEnd(2097152);
                            config = wm.computeNewConfiguration();
                            metrics = new DisplayMetrics();
                            ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                            context.getResources().updateConfiguration(config, metrics);
                            systemTheme = context.getTheme();
                            if (systemTheme.getChangingConfigurations() != 0) {
                            }
                            Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                            Slog.i(TAG, "power manager systemReady");
                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                            Trace.traceEnd(2097152);
                            Trace.traceEnd(2097152);
                            Slog.i(TAG, "Fingerprint systemReady");
                            this.mFingerprintService.systemReady();
                            if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                            }
                            Slog.i(TAG, "Secrecy systemReady");
                            this.mSecrecyService.systemReady();
                            Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                            Slog.i(TAG, "Package systemReady");
                            this.mPackageManagerService.systemReady();
                            Trace.traceEnd(2097152);
                            Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                            Slog.i(TAG, "DisplayManager systemReady");
                            this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                            Trace.traceEnd(2097152);
                            networkManagementF = networkManagement;
                            networkStatsF = networkStats;
                            networkPolicyF = networkPolicy;
                            connectivityF = connectivity;
                            networkScoreF = networkScore;
                            locationF = location;
                            countryDetectorF = countryDetector;
                            networkTimeUpdaterF = networkTimeUpdater;
                            commonTimeMgmtServiceF = commonTimeMgmtService;
                            atlasF = atlas;
                            inputManagerF = inputManager;
                            telephonyRegistryF = telephonyRegistry;
                            mediaRouterF = mediaRouter;
                            mmsServiceF = mmsService;
                            multimediaServiceF = multimediaService;
                            perfServiceF = perfServiceMgr;
                            runningboosterF = runningbooster;
                            usageServiceF = usageService;
                            Slog.i(TAG, "Ams systemReady");
                            mOppoService = oppoService;
                            this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                        }
                        Slog.i(TAG, "Oppo Customize Service");
                        ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                        if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                        }
                        traceBeginAndSlog("CabcService");
                        Slog.i(TAG, "Cabc Service");
                        cabcService = new CabcService(context);
                        try {
                            ServiceManager.addService("cabc", cabcService);
                            cabcService2 = cabcService;
                        } catch (Throwable th9) {
                            e222222222 = th9;
                            cabcService2 = cabcService;
                            reportWtf("starting Cabc Service", e222222222);
                            if (disableSamplingProfiler) {
                            }
                            traceBeginAndSlog("StartNetworkTimeUpdateService");
                            networkTimeUpdateService = new NetworkTimeUpdateService(context);
                            ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                            networkTimeUpdater = networkTimeUpdateService;
                            Trace.traceEnd(2097152);
                            traceBeginAndSlog("StartCommonTimeManagementService");
                            commonTimeManagementService = new CommonTimeManagementService(context);
                            ServiceManager.addService("commontime_management", commonTimeManagementService);
                            commonTimeMgmtService = commonTimeManagementService;
                            Trace.traceEnd(2097152);
                            if (disableNetwork) {
                            }
                            this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartAssetAtlasService");
                            assetAtlasService = new AssetAtlasService(context);
                            ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService);
                            atlas = assetAtlasService;
                            Trace.traceEnd(2097152);
                            if (disableNonCoreServices) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                            }
                            this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                            Slog.i(TAG, "Media Session Service");
                            this.mSystemServiceManager.startService(MediaSessionService.class);
                            if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                            }
                            if (disableNonCoreServices) {
                            }
                            Slog.i(TAG, "Oppo Engineer Service");
                            this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                            this.mSystemServiceManager.startService(LauncherAppsService.class);
                            if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                            }
                            Slog.i(TAG, "HDMI Manager Service");
                            mtkHdmiManagerService = new MtkHdmiManagerService(context);
                            ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                            mtkHdmiManagerService2 = mtkHdmiManagerService;
                            if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                            }
                            if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                            }
                            if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                            }
                            Slog.i(TAG, "Secrecy Service");
                            this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                            if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                            }
                            VMRuntime.getRuntime().startJitCompilation();
                            mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                            this.mSystemServiceManager.startService(RetailDemoModeService.class);
                            Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                            vibrator.systemReady();
                            Trace.traceEnd(2097152);
                            Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                            if (lockSettings != null) {
                            }
                            Trace.traceEnd(2097152);
                            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                            this.mSystemServiceManager.startBootPhase(500);
                            Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                            Slog.i(TAG, "wms systemReady");
                            wm.systemReady();
                            Trace.traceEnd(2097152);
                            config = wm.computeNewConfiguration();
                            metrics = new DisplayMetrics();
                            ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                            context.getResources().updateConfiguration(config, metrics);
                            systemTheme = context.getTheme();
                            if (systemTheme.getChangingConfigurations() != 0) {
                            }
                            Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                            Slog.i(TAG, "power manager systemReady");
                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                            Trace.traceEnd(2097152);
                            Trace.traceEnd(2097152);
                            Slog.i(TAG, "Fingerprint systemReady");
                            this.mFingerprintService.systemReady();
                            if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                            }
                            Slog.i(TAG, "Secrecy systemReady");
                            this.mSecrecyService.systemReady();
                            Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                            Slog.i(TAG, "Package systemReady");
                            this.mPackageManagerService.systemReady();
                            Trace.traceEnd(2097152);
                            Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                            Slog.i(TAG, "DisplayManager systemReady");
                            this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                            Trace.traceEnd(2097152);
                            networkManagementF = networkManagement;
                            networkStatsF = networkStats;
                            networkPolicyF = networkPolicy;
                            connectivityF = connectivity;
                            networkScoreF = networkScore;
                            locationF = location;
                            countryDetectorF = countryDetector;
                            networkTimeUpdaterF = networkTimeUpdater;
                            commonTimeMgmtServiceF = commonTimeMgmtService;
                            atlasF = atlas;
                            inputManagerF = inputManager;
                            telephonyRegistryF = telephonyRegistry;
                            mediaRouterF = mediaRouter;
                            mmsServiceF = mmsService;
                            multimediaServiceF = multimediaService;
                            perfServiceF = perfServiceMgr;
                            runningboosterF = runningbooster;
                            usageServiceF = usageService;
                            Slog.i(TAG, "Ams systemReady");
                            mOppoService = oppoService;
                            this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                        }
                        if (disableSamplingProfiler) {
                        }
                        traceBeginAndSlog("StartNetworkTimeUpdateService");
                        try {
                            networkTimeUpdateService = new NetworkTimeUpdateService(context);
                            ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                            networkTimeUpdater = networkTimeUpdateService;
                        } catch (Throwable th10) {
                            e222222222 = th10;
                            reportWtf("starting NetworkTimeUpdate service", e222222222);
                            Trace.traceEnd(2097152);
                            traceBeginAndSlog("StartCommonTimeManagementService");
                            commonTimeManagementService = new CommonTimeManagementService(context);
                            ServiceManager.addService("commontime_management", commonTimeManagementService);
                            commonTimeMgmtService = commonTimeManagementService;
                            Trace.traceEnd(2097152);
                            if (disableNetwork) {
                            }
                            this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartAssetAtlasService");
                            assetAtlasService = new AssetAtlasService(context);
                            ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService);
                            atlas = assetAtlasService;
                            Trace.traceEnd(2097152);
                            if (disableNonCoreServices) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                            }
                            this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                            Slog.i(TAG, "Media Session Service");
                            this.mSystemServiceManager.startService(MediaSessionService.class);
                            if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                            }
                            if (disableNonCoreServices) {
                            }
                            Slog.i(TAG, "Oppo Engineer Service");
                            this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                            this.mSystemServiceManager.startService(LauncherAppsService.class);
                            if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                            }
                            Slog.i(TAG, "HDMI Manager Service");
                            mtkHdmiManagerService = new MtkHdmiManagerService(context);
                            ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                            mtkHdmiManagerService2 = mtkHdmiManagerService;
                            if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                            }
                            if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                            }
                            if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                            }
                            Slog.i(TAG, "Secrecy Service");
                            this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                            if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                            }
                            VMRuntime.getRuntime().startJitCompilation();
                            mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                            this.mSystemServiceManager.startService(RetailDemoModeService.class);
                            Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                            vibrator.systemReady();
                            Trace.traceEnd(2097152);
                            Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                            if (lockSettings != null) {
                            }
                            Trace.traceEnd(2097152);
                            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                            this.mSystemServiceManager.startBootPhase(500);
                            Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                            Slog.i(TAG, "wms systemReady");
                            wm.systemReady();
                            Trace.traceEnd(2097152);
                            config = wm.computeNewConfiguration();
                            metrics = new DisplayMetrics();
                            ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                            context.getResources().updateConfiguration(config, metrics);
                            systemTheme = context.getTheme();
                            if (systemTheme.getChangingConfigurations() != 0) {
                            }
                            Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                            Slog.i(TAG, "power manager systemReady");
                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                            Trace.traceEnd(2097152);
                            Trace.traceEnd(2097152);
                            Slog.i(TAG, "Fingerprint systemReady");
                            this.mFingerprintService.systemReady();
                            if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                            }
                            Slog.i(TAG, "Secrecy systemReady");
                            this.mSecrecyService.systemReady();
                            Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                            Slog.i(TAG, "Package systemReady");
                            this.mPackageManagerService.systemReady();
                            Trace.traceEnd(2097152);
                            Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                            Slog.i(TAG, "DisplayManager systemReady");
                            this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                            Trace.traceEnd(2097152);
                            networkManagementF = networkManagement;
                            networkStatsF = networkStats;
                            networkPolicyF = networkPolicy;
                            connectivityF = connectivity;
                            networkScoreF = networkScore;
                            locationF = location;
                            countryDetectorF = countryDetector;
                            networkTimeUpdaterF = networkTimeUpdater;
                            commonTimeMgmtServiceF = commonTimeMgmtService;
                            atlasF = atlas;
                            inputManagerF = inputManager;
                            telephonyRegistryF = telephonyRegistry;
                            mediaRouterF = mediaRouter;
                            mmsServiceF = mmsService;
                            multimediaServiceF = multimediaService;
                            perfServiceF = perfServiceMgr;
                            runningboosterF = runningbooster;
                            usageServiceF = usageService;
                            Slog.i(TAG, "Ams systemReady");
                            mOppoService = oppoService;
                            this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                        }
                        Trace.traceEnd(2097152);
                        traceBeginAndSlog("StartCommonTimeManagementService");
                        commonTimeManagementService = new CommonTimeManagementService(context);
                        try {
                            ServiceManager.addService("commontime_management", commonTimeManagementService);
                            commonTimeMgmtService = commonTimeManagementService;
                        } catch (Throwable th11) {
                            e222222222 = th11;
                            commonTimeMgmtService = commonTimeManagementService;
                            reportWtf("starting CommonTimeManagementService service", e222222222);
                            Trace.traceEnd(2097152);
                            if (disableNetwork) {
                            }
                            this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartAssetAtlasService");
                            assetAtlasService = new AssetAtlasService(context);
                            ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService);
                            atlas = assetAtlasService;
                            Trace.traceEnd(2097152);
                            if (disableNonCoreServices) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                            }
                            this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                            Slog.i(TAG, "Media Session Service");
                            this.mSystemServiceManager.startService(MediaSessionService.class);
                            if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                            }
                            if (disableNonCoreServices) {
                            }
                            Slog.i(TAG, "Oppo Engineer Service");
                            this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                            this.mSystemServiceManager.startService(LauncherAppsService.class);
                            if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                            }
                            Slog.i(TAG, "HDMI Manager Service");
                            mtkHdmiManagerService = new MtkHdmiManagerService(context);
                            ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                            mtkHdmiManagerService2 = mtkHdmiManagerService;
                            if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                            }
                            if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                            }
                            if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                            }
                            Slog.i(TAG, "Secrecy Service");
                            this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                            if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                            }
                            VMRuntime.getRuntime().startJitCompilation();
                            mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                            this.mSystemServiceManager.startService(RetailDemoModeService.class);
                            Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                            vibrator.systemReady();
                            Trace.traceEnd(2097152);
                            Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                            if (lockSettings != null) {
                            }
                            Trace.traceEnd(2097152);
                            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                            this.mSystemServiceManager.startBootPhase(500);
                            Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                            Slog.i(TAG, "wms systemReady");
                            wm.systemReady();
                            Trace.traceEnd(2097152);
                            config = wm.computeNewConfiguration();
                            metrics = new DisplayMetrics();
                            ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                            context.getResources().updateConfiguration(config, metrics);
                            systemTheme = context.getTheme();
                            if (systemTheme.getChangingConfigurations() != 0) {
                            }
                            Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                            Slog.i(TAG, "power manager systemReady");
                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                            Trace.traceEnd(2097152);
                            Trace.traceEnd(2097152);
                            Slog.i(TAG, "Fingerprint systemReady");
                            this.mFingerprintService.systemReady();
                            if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                            }
                            Slog.i(TAG, "Secrecy systemReady");
                            this.mSecrecyService.systemReady();
                            Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                            Slog.i(TAG, "Package systemReady");
                            this.mPackageManagerService.systemReady();
                            Trace.traceEnd(2097152);
                            Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                            Slog.i(TAG, "DisplayManager systemReady");
                            this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                            Trace.traceEnd(2097152);
                            networkManagementF = networkManagement;
                            networkStatsF = networkStats;
                            networkPolicyF = networkPolicy;
                            connectivityF = connectivity;
                            networkScoreF = networkScore;
                            locationF = location;
                            countryDetectorF = countryDetector;
                            networkTimeUpdaterF = networkTimeUpdater;
                            commonTimeMgmtServiceF = commonTimeMgmtService;
                            atlasF = atlas;
                            inputManagerF = inputManager;
                            telephonyRegistryF = telephonyRegistry;
                            mediaRouterF = mediaRouter;
                            mmsServiceF = mmsService;
                            multimediaServiceF = multimediaService;
                            perfServiceF = perfServiceMgr;
                            runningboosterF = runningbooster;
                            usageServiceF = usageService;
                            Slog.i(TAG, "Ams systemReady");
                            mOppoService = oppoService;
                            this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                        }
                        Trace.traceEnd(2097152);
                        if (disableNetwork) {
                        }
                        this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartAssetAtlasService");
                        try {
                            assetAtlasService = new AssetAtlasService(context);
                            ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService);
                            atlas = assetAtlasService;
                        } catch (Throwable th12) {
                            e222222222 = th12;
                            reportWtf("starting AssetAtlasService", e222222222);
                            Trace.traceEnd(2097152);
                            if (disableNonCoreServices) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                            }
                            this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                            Slog.i(TAG, "Media Session Service");
                            this.mSystemServiceManager.startService(MediaSessionService.class);
                            if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                            }
                            if (disableNonCoreServices) {
                            }
                            Slog.i(TAG, "Oppo Engineer Service");
                            this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                            this.mSystemServiceManager.startService(LauncherAppsService.class);
                            if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                            }
                            Slog.i(TAG, "HDMI Manager Service");
                            mtkHdmiManagerService = new MtkHdmiManagerService(context);
                            ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                            mtkHdmiManagerService2 = mtkHdmiManagerService;
                            if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                            }
                            if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                            }
                            if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                            }
                            Slog.i(TAG, "Secrecy Service");
                            this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                            if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                            }
                            VMRuntime.getRuntime().startJitCompilation();
                            mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                            this.mSystemServiceManager.startService(RetailDemoModeService.class);
                            Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                            vibrator.systemReady();
                            Trace.traceEnd(2097152);
                            Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                            if (lockSettings != null) {
                            }
                            Trace.traceEnd(2097152);
                            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                            this.mSystemServiceManager.startBootPhase(500);
                            Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                            Slog.i(TAG, "wms systemReady");
                            wm.systemReady();
                            Trace.traceEnd(2097152);
                            config = wm.computeNewConfiguration();
                            metrics = new DisplayMetrics();
                            ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                            context.getResources().updateConfiguration(config, metrics);
                            systemTheme = context.getTheme();
                            if (systemTheme.getChangingConfigurations() != 0) {
                            }
                            Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                            Slog.i(TAG, "power manager systemReady");
                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                            Trace.traceEnd(2097152);
                            Trace.traceEnd(2097152);
                            Slog.i(TAG, "Fingerprint systemReady");
                            this.mFingerprintService.systemReady();
                            if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                            }
                            Slog.i(TAG, "Secrecy systemReady");
                            this.mSecrecyService.systemReady();
                            Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                            Slog.i(TAG, "Package systemReady");
                            this.mPackageManagerService.systemReady();
                            Trace.traceEnd(2097152);
                            Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                            Slog.i(TAG, "DisplayManager systemReady");
                            this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                            Trace.traceEnd(2097152);
                            networkManagementF = networkManagement;
                            networkStatsF = networkStats;
                            networkPolicyF = networkPolicy;
                            connectivityF = connectivity;
                            networkScoreF = networkScore;
                            locationF = location;
                            countryDetectorF = countryDetector;
                            networkTimeUpdaterF = networkTimeUpdater;
                            commonTimeMgmtServiceF = commonTimeMgmtService;
                            atlasF = atlas;
                            inputManagerF = inputManager;
                            telephonyRegistryF = telephonyRegistry;
                            mediaRouterF = mediaRouter;
                            mmsServiceF = mmsService;
                            multimediaServiceF = multimediaService;
                            perfServiceF = perfServiceMgr;
                            runningboosterF = runningbooster;
                            usageServiceF = usageService;
                            Slog.i(TAG, "Ams systemReady");
                            mOppoService = oppoService;
                            this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                        }
                        Trace.traceEnd(2097152);
                        if (disableNonCoreServices) {
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                        }
                        this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                        Slog.i(TAG, "Media Session Service");
                        this.mSystemServiceManager.startService(MediaSessionService.class);
                        if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                        }
                        if (disableNonCoreServices) {
                        }
                        Slog.i(TAG, "Oppo Engineer Service");
                        this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                        this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                        this.mSystemServiceManager.startService(LauncherAppsService.class);
                        if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                        }
                        try {
                            Slog.i(TAG, "HDMI Manager Service");
                            mtkHdmiManagerService = new MtkHdmiManagerService(context);
                            ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                            mtkHdmiManagerService2 = mtkHdmiManagerService;
                        } catch (Throwable th13) {
                            e222222222 = th13;
                            Slog.e(TAG, "Failure starting MtkHdmiManager", e222222222);
                            if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                            }
                            if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                            }
                            if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                            }
                            Slog.i(TAG, "Secrecy Service");
                            this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                            if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                            }
                            VMRuntime.getRuntime().startJitCompilation();
                            mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                            this.mSystemServiceManager.startService(RetailDemoModeService.class);
                            Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                            vibrator.systemReady();
                            Trace.traceEnd(2097152);
                            Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                            if (lockSettings != null) {
                            }
                            Trace.traceEnd(2097152);
                            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                            this.mSystemServiceManager.startBootPhase(500);
                            Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                            Slog.i(TAG, "wms systemReady");
                            wm.systemReady();
                            Trace.traceEnd(2097152);
                            config = wm.computeNewConfiguration();
                            metrics = new DisplayMetrics();
                            ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                            context.getResources().updateConfiguration(config, metrics);
                            systemTheme = context.getTheme();
                            if (systemTheme.getChangingConfigurations() != 0) {
                            }
                            Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                            Slog.i(TAG, "power manager systemReady");
                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                            Trace.traceEnd(2097152);
                            Trace.traceEnd(2097152);
                            Slog.i(TAG, "Fingerprint systemReady");
                            this.mFingerprintService.systemReady();
                            if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                            }
                            Slog.i(TAG, "Secrecy systemReady");
                            this.mSecrecyService.systemReady();
                            Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                            Slog.i(TAG, "Package systemReady");
                            this.mPackageManagerService.systemReady();
                            Trace.traceEnd(2097152);
                            Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                            Slog.i(TAG, "DisplayManager systemReady");
                            this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                            Trace.traceEnd(2097152);
                            networkManagementF = networkManagement;
                            networkStatsF = networkStats;
                            networkPolicyF = networkPolicy;
                            connectivityF = connectivity;
                            networkScoreF = networkScore;
                            locationF = location;
                            countryDetectorF = countryDetector;
                            networkTimeUpdaterF = networkTimeUpdater;
                            commonTimeMgmtServiceF = commonTimeMgmtService;
                            atlasF = atlas;
                            inputManagerF = inputManager;
                            telephonyRegistryF = telephonyRegistry;
                            mediaRouterF = mediaRouter;
                            mmsServiceF = mmsService;
                            multimediaServiceF = multimediaService;
                            perfServiceF = perfServiceMgr;
                            runningboosterF = runningbooster;
                            usageServiceF = usageService;
                            Slog.i(TAG, "Ams systemReady");
                            mOppoService = oppoService;
                            this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                        }
                        if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                        }
                        if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                        }
                        if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                        }
                        Slog.i(TAG, "Secrecy Service");
                        this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                        this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                        if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                        }
                        VMRuntime.getRuntime().startJitCompilation();
                        mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                        this.mSystemServiceManager.startService(RetailDemoModeService.class);
                        Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                        vibrator.systemReady();
                        Trace.traceEnd(2097152);
                        Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                        if (lockSettings != null) {
                        }
                        Trace.traceEnd(2097152);
                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                        this.mSystemServiceManager.startBootPhase(500);
                        Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                        Slog.i(TAG, "wms systemReady");
                        wm.systemReady();
                        Trace.traceEnd(2097152);
                        config = wm.computeNewConfiguration();
                        metrics = new DisplayMetrics();
                        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                        context.getResources().updateConfiguration(config, metrics);
                        systemTheme = context.getTheme();
                        if (systemTheme.getChangingConfigurations() != 0) {
                        }
                        Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                        Slog.i(TAG, "power manager systemReady");
                        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                        Trace.traceEnd(2097152);
                        Trace.traceEnd(2097152);
                        Slog.i(TAG, "Fingerprint systemReady");
                        this.mFingerprintService.systemReady();
                        if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                        }
                        Slog.i(TAG, "Secrecy systemReady");
                        this.mSecrecyService.systemReady();
                        Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                        Slog.i(TAG, "Package systemReady");
                        this.mPackageManagerService.systemReady();
                        Trace.traceEnd(2097152);
                        Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                        Slog.i(TAG, "DisplayManager systemReady");
                        this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                        Trace.traceEnd(2097152);
                        networkManagementF = networkManagement;
                        networkStatsF = networkStats;
                        networkPolicyF = networkPolicy;
                        connectivityF = connectivity;
                        networkScoreF = networkScore;
                        locationF = location;
                        countryDetectorF = countryDetector;
                        networkTimeUpdaterF = networkTimeUpdater;
                        commonTimeMgmtServiceF = commonTimeMgmtService;
                        atlasF = atlas;
                        inputManagerF = inputManager;
                        telephonyRegistryF = telephonyRegistry;
                        mediaRouterF = mediaRouter;
                        mmsServiceF = mmsService;
                        multimediaServiceF = multimediaService;
                        perfServiceF = perfServiceMgr;
                        runningboosterF = runningbooster;
                        usageServiceF = usageService;
                        Slog.i(TAG, "Ams systemReady");
                        mOppoService = oppoService;
                        this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                    }
                } catch (Throwable th14) {
                    e222222222 = th14;
                    reportWtf("starting StatusBarManagerService", e222222222);
                    Trace.traceEnd(2097152);
                    if (disableNonCoreServices) {
                    }
                    if (disableNetwork) {
                    }
                    this.mSystemServiceManager.startService(TextServicesManagerService.Lifecycle.class);
                    if (disableNetwork) {
                    }
                    if (disableNonCoreServices) {
                    }
                    if (disableNonCoreServices) {
                    }
                    Trace.traceBegin(2097152, "WaitForAsecScan");
                    mountService.waitForAsecScan();
                    Trace.traceEnd(2097152);
                    Slog.i(TAG, "Oppo Expand Service");
                    ServiceManager.addService("OPPOExService", new OppoExService(context, wm));
                    if (this.mOppoLightsService != null) {
                    }
                    Slog.i(TAG, "NotificationManagerService");
                    this.mSystemServiceManager.startService(NotificationManagerService.class);
                    networkPolicy.bindNotificationManager(INotificationManager.Stub.asInterface(ServiceManager.getService(NotificationManagerService.NOTIFICATON_TITLE_NAME)));
                    if (context.getPackageManager().hasSystemFeature("oppo.support.single.partition")) {
                    }
                    if (disableLocation) {
                    }
                    traceBeginAndSlog("StartSearchManagerService");
                    this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                    Trace.traceEnd(2097152);
                    Slog.i(TAG, "Search Engine Service");
                    ServiceManager.addService("search_engine", new SearchEngineManagerService(context));
                    this.mSystemServiceManager.startService(DropBoxManagerService.class);
                    instance = AgingCriticalEvent.getInstance();
                    str = AgingCriticalEvent.EVENT_SYSTEM_BOOTUP;
                    strArr = new String[1];
                    strArr[0] = "systemserver pid:" + Process.myPid();
                    instance.writeEvent(str, strArr);
                    traceBeginAndSlog("StartWallpaperManagerService");
                    this.mSystemServiceManager.startService(WALLPAPER_SERVICE_CLASS);
                    Trace.traceEnd(2097152);
                    traceBeginAndSlog("StartAudioService");
                    this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                    Trace.traceEnd(2097152);
                    if ("1".equals(SystemProperties.get("ro.mtk_sensorhub_support"))) {
                    }
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartWiredAccessoryManager");
                    inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                    Trace.traceEnd(2097152);
                    if (disableNonCoreServices) {
                    }
                    this.mSystemServiceManager.startService(TwilightService.class);
                    if (NightDisplayController.isAvailable(context)) {
                    }
                    this.mSystemServiceManager.startService(JobSchedulerService.class);
                    this.mSystemServiceManager.startService(SoundTriggerService.class);
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartDiskStatsService");
                    ServiceManager.addService("diskstats", new DiskStatsService(context));
                    Trace.traceEnd(2097152);
                    Slog.i(TAG, "OPPO Usage Service");
                    oppoUsageService = new OppoUsageService(context);
                    ServiceManager.addService("usage", oppoUsageService);
                    usageService = oppoUsageService;
                    Slog.i(TAG, "Hypnus Service");
                    hypnusService = new HypnusService(context);
                    ServiceManager.addService("hypnus", hypnusService);
                    hypnusService2 = hypnusService;
                    Slog.i(TAG, "Oppo Service");
                    oppoService2 = new OppoService(context);
                    ServiceManager.addService("OPPO", oppoService2);
                    oppoService = oppoService2;
                    Slog.i(TAG, "+OppoMultimediaService");
                    oppoMultimediaService = new OppoMultimediaService(context);
                    ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                    Slog.i(TAG, "-OppoMultimediaService");
                    multimediaService = oppoMultimediaService;
                    Slog.i(TAG, "Oppo Customize Service");
                    ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                    if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                    }
                    traceBeginAndSlog("CabcService");
                    Slog.i(TAG, "Cabc Service");
                    cabcService = new CabcService(context);
                    ServiceManager.addService("cabc", cabcService);
                    cabcService2 = cabcService;
                    if (disableSamplingProfiler) {
                    }
                    traceBeginAndSlog("StartNetworkTimeUpdateService");
                    networkTimeUpdateService = new NetworkTimeUpdateService(context);
                    ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                    networkTimeUpdater = networkTimeUpdateService;
                    Trace.traceEnd(2097152);
                    traceBeginAndSlog("StartCommonTimeManagementService");
                    commonTimeManagementService = new CommonTimeManagementService(context);
                    ServiceManager.addService("commontime_management", commonTimeManagementService);
                    commonTimeMgmtService = commonTimeManagementService;
                    Trace.traceEnd(2097152);
                    if (disableNetwork) {
                    }
                    this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartAssetAtlasService");
                    assetAtlasService = new AssetAtlasService(context);
                    ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService);
                    atlas = assetAtlasService;
                    Trace.traceEnd(2097152);
                    if (disableNonCoreServices) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                    }
                    this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                    Slog.i(TAG, "Media Session Service");
                    this.mSystemServiceManager.startService(MediaSessionService.class);
                    if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                    }
                    if (disableNonCoreServices) {
                    }
                    Slog.i(TAG, "Oppo Engineer Service");
                    this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                    if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                    }
                    Slog.i(TAG, "HDMI Manager Service");
                    mtkHdmiManagerService = new MtkHdmiManagerService(context);
                    ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                    mtkHdmiManagerService2 = mtkHdmiManagerService;
                    if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                    }
                    if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                    }
                    if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                    }
                    Slog.i(TAG, "Secrecy Service");
                    this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                    this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                    }
                    VMRuntime.getRuntime().startJitCompilation();
                    mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                    this.mSystemServiceManager.startService(RetailDemoModeService.class);
                    Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                    vibrator.systemReady();
                    Trace.traceEnd(2097152);
                    Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                    if (lockSettings != null) {
                    }
                    Trace.traceEnd(2097152);
                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                    this.mSystemServiceManager.startBootPhase(500);
                    Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                    Slog.i(TAG, "wms systemReady");
                    wm.systemReady();
                    Trace.traceEnd(2097152);
                    config = wm.computeNewConfiguration();
                    metrics = new DisplayMetrics();
                    ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                    context.getResources().updateConfiguration(config, metrics);
                    systemTheme = context.getTheme();
                    if (systemTheme.getChangingConfigurations() != 0) {
                    }
                    Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                    Slog.i(TAG, "power manager systemReady");
                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                    Trace.traceEnd(2097152);
                    Trace.traceEnd(2097152);
                    Slog.i(TAG, "Fingerprint systemReady");
                    this.mFingerprintService.systemReady();
                    if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                    }
                    Slog.i(TAG, "Secrecy systemReady");
                    this.mSecrecyService.systemReady();
                    Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                    Slog.i(TAG, "Package systemReady");
                    this.mPackageManagerService.systemReady();
                    Trace.traceEnd(2097152);
                    Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                    Slog.i(TAG, "DisplayManager systemReady");
                    this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                    Trace.traceEnd(2097152);
                    networkManagementF = networkManagement;
                    networkStatsF = networkStats;
                    networkPolicyF = networkPolicy;
                    connectivityF = connectivity;
                    networkScoreF = networkScore;
                    locationF = location;
                    countryDetectorF = countryDetector;
                    networkTimeUpdaterF = networkTimeUpdater;
                    commonTimeMgmtServiceF = commonTimeMgmtService;
                    atlasF = atlas;
                    inputManagerF = inputManager;
                    telephonyRegistryF = telephonyRegistry;
                    mediaRouterF = mediaRouter;
                    mmsServiceF = mmsService;
                    multimediaServiceF = multimediaService;
                    perfServiceF = perfServiceMgr;
                    runningboosterF = runningbooster;
                    usageServiceF = usageService;
                    Slog.i(TAG, "Ams systemReady");
                    mOppoService = oppoService;
                    this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                }
                Trace.traceEnd(2097152);
            }
            if (disableNonCoreServices) {
                traceBeginAndSlog("StartClipboardService");
                try {
                    ServiceManager.addService("clipboard", new ClipboardService(context));
                } catch (Throwable e2222222222) {
                    reportWtf("starting Clipboard Service", e2222222222);
                }
                Trace.traceEnd(2097152);
            }
            if (disableNetwork) {
                traceBeginAndSlog("StartNetworkManagementService");
                try {
                    networkManagement = NetworkManagementService.create(context);
                    ServiceManager.addService("network_management", networkManagement);
                } catch (Throwable e22222222222) {
                    reportWtf("starting NetworkManagement Service", e22222222222);
                }
                Trace.traceEnd(2097152);
            }
            if (!(disableNonCoreServices || disableTextServices)) {
                this.mSystemServiceManager.startService(TextServicesManagerService.Lifecycle.class);
            }
            if (disableNetwork) {
                traceBeginAndSlog("StartNetworkScoreService");
                try {
                    NetworkScoreService networkScoreService = new NetworkScoreService(context);
                    try {
                        ServiceManager.addService("network_score", networkScoreService);
                        networkScore = networkScoreService;
                    } catch (Throwable th15) {
                        e22222222222 = th15;
                        networkScore = networkScoreService;
                        reportWtf("starting Network Score Service", e22222222222);
                        Trace.traceEnd(2097152);
                        traceBeginAndSlog("StartNetworkStatsService");
                        networkStats = NetworkStatsService.create(context, networkManagement);
                        ServiceManager.addService("netstats", networkStats);
                        Trace.traceEnd(2097152);
                        traceBeginAndSlog("StartNetworkPolicyManagerService");
                        networkPolicyManagerService = new NetworkPolicyManagerService(context, this.mActivityManagerService, networkStats, networkManagement);
                        try {
                            ServiceManager.addService("netpolicy", networkPolicyManagerService);
                            networkPolicy = networkPolicyManagerService;
                        } catch (Throwable th16) {
                            e22222222222 = th16;
                            networkPolicy = networkPolicyManagerService;
                            reportWtf("starting NetworkPolicy Service", e22222222222);
                            Trace.traceEnd(2097152);
                            if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.nan")) {
                            }
                            Slog.i(TAG, "wifi service");
                            this.mSystemServiceManager.startService(WIFI_P2P_SERVICE_CLASS);
                            this.mSystemServiceManager.startService(WIFI_SERVICE_CLASS);
                            this.mSystemServiceManager.startService("com.android.server.wifi.scanner.WifiScanningService");
                            if (!disableRtt) {
                            }
                            this.mSystemServiceManager.startService(ETHERNET_SERVICE_CLASS);
                            traceBeginAndSlog("StartConnectivityService");
                            connectivityService = new ConnectivityService(context, networkManagement, networkStats, networkPolicy);
                            ServiceManager.addService("connectivity", connectivityService);
                            networkStats.bindConnectivityManager(connectivityService);
                            networkPolicy.bindConnectivityManager(connectivityService);
                            connectivity = connectivityService;
                            Trace.traceEnd(2097152);
                            traceBeginAndSlog("StartNsdService");
                            ServiceManager.addService("servicediscovery", NsdService.create(context));
                            Trace.traceEnd(2097152);
                            if ("1".equals(SystemProperties.get("persist.mtk.datashaping.support"))) {
                            }
                            traceBeginAndSlog("StartLwxService");
                            this.mSystemServiceManager.startService(LWX_SERVICE_CLASS);
                            Trace.traceEnd(2097152);
                            if (disableNonCoreServices) {
                            }
                            if (disableNonCoreServices) {
                            }
                            Trace.traceBegin(2097152, "WaitForAsecScan");
                            mountService.waitForAsecScan();
                            Trace.traceEnd(2097152);
                            Slog.i(TAG, "Oppo Expand Service");
                            ServiceManager.addService("OPPOExService", new OppoExService(context, wm));
                            if (this.mOppoLightsService != null) {
                            }
                            Slog.i(TAG, "NotificationManagerService");
                            this.mSystemServiceManager.startService(NotificationManagerService.class);
                            networkPolicy.bindNotificationManager(INotificationManager.Stub.asInterface(ServiceManager.getService(NotificationManagerService.NOTIFICATON_TITLE_NAME)));
                            if (context.getPackageManager().hasSystemFeature("oppo.support.single.partition")) {
                            }
                            if (disableLocation) {
                            }
                            traceBeginAndSlog("StartSearchManagerService");
                            this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                            Trace.traceEnd(2097152);
                            Slog.i(TAG, "Search Engine Service");
                            ServiceManager.addService("search_engine", new SearchEngineManagerService(context));
                            this.mSystemServiceManager.startService(DropBoxManagerService.class);
                            instance = AgingCriticalEvent.getInstance();
                            str = AgingCriticalEvent.EVENT_SYSTEM_BOOTUP;
                            strArr = new String[1];
                            strArr[0] = "systemserver pid:" + Process.myPid();
                            instance.writeEvent(str, strArr);
                            traceBeginAndSlog("StartWallpaperManagerService");
                            this.mSystemServiceManager.startService(WALLPAPER_SERVICE_CLASS);
                            Trace.traceEnd(2097152);
                            traceBeginAndSlog("StartAudioService");
                            this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                            Trace.traceEnd(2097152);
                            if ("1".equals(SystemProperties.get("ro.mtk_sensorhub_support"))) {
                            }
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartWiredAccessoryManager");
                            inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                            Trace.traceEnd(2097152);
                            if (disableNonCoreServices) {
                            }
                            this.mSystemServiceManager.startService(TwilightService.class);
                            if (NightDisplayController.isAvailable(context)) {
                            }
                            this.mSystemServiceManager.startService(JobSchedulerService.class);
                            this.mSystemServiceManager.startService(SoundTriggerService.class);
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartDiskStatsService");
                            ServiceManager.addService("diskstats", new DiskStatsService(context));
                            Trace.traceEnd(2097152);
                            Slog.i(TAG, "OPPO Usage Service");
                            oppoUsageService = new OppoUsageService(context);
                            ServiceManager.addService("usage", oppoUsageService);
                            usageService = oppoUsageService;
                            Slog.i(TAG, "Hypnus Service");
                            hypnusService = new HypnusService(context);
                            ServiceManager.addService("hypnus", hypnusService);
                            hypnusService2 = hypnusService;
                            Slog.i(TAG, "Oppo Service");
                            oppoService2 = new OppoService(context);
                            ServiceManager.addService("OPPO", oppoService2);
                            oppoService = oppoService2;
                            Slog.i(TAG, "+OppoMultimediaService");
                            oppoMultimediaService = new OppoMultimediaService(context);
                            ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                            Slog.i(TAG, "-OppoMultimediaService");
                            multimediaService = oppoMultimediaService;
                            Slog.i(TAG, "Oppo Customize Service");
                            ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                            if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                            }
                            traceBeginAndSlog("CabcService");
                            Slog.i(TAG, "Cabc Service");
                            cabcService = new CabcService(context);
                            ServiceManager.addService("cabc", cabcService);
                            cabcService2 = cabcService;
                            if (disableSamplingProfiler) {
                            }
                            traceBeginAndSlog("StartNetworkTimeUpdateService");
                            networkTimeUpdateService = new NetworkTimeUpdateService(context);
                            ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                            networkTimeUpdater = networkTimeUpdateService;
                            Trace.traceEnd(2097152);
                            traceBeginAndSlog("StartCommonTimeManagementService");
                            commonTimeManagementService = new CommonTimeManagementService(context);
                            ServiceManager.addService("commontime_management", commonTimeManagementService);
                            commonTimeMgmtService = commonTimeManagementService;
                            Trace.traceEnd(2097152);
                            if (disableNetwork) {
                            }
                            this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartAssetAtlasService");
                            assetAtlasService = new AssetAtlasService(context);
                            ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService);
                            atlas = assetAtlasService;
                            Trace.traceEnd(2097152);
                            if (disableNonCoreServices) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                            }
                            this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                            Slog.i(TAG, "Media Session Service");
                            this.mSystemServiceManager.startService(MediaSessionService.class);
                            if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                            }
                            if (disableNonCoreServices) {
                            }
                            Slog.i(TAG, "Oppo Engineer Service");
                            this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                            this.mSystemServiceManager.startService(LauncherAppsService.class);
                            if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                            }
                            Slog.i(TAG, "HDMI Manager Service");
                            mtkHdmiManagerService = new MtkHdmiManagerService(context);
                            ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                            mtkHdmiManagerService2 = mtkHdmiManagerService;
                            if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                            }
                            if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                            }
                            if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                            }
                            Slog.i(TAG, "Secrecy Service");
                            this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                            if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                            }
                            VMRuntime.getRuntime().startJitCompilation();
                            mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                            this.mSystemServiceManager.startService(RetailDemoModeService.class);
                            Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                            vibrator.systemReady();
                            Trace.traceEnd(2097152);
                            Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                            if (lockSettings != null) {
                            }
                            Trace.traceEnd(2097152);
                            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                            this.mSystemServiceManager.startBootPhase(500);
                            Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                            Slog.i(TAG, "wms systemReady");
                            wm.systemReady();
                            Trace.traceEnd(2097152);
                            config = wm.computeNewConfiguration();
                            metrics = new DisplayMetrics();
                            ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                            context.getResources().updateConfiguration(config, metrics);
                            systemTheme = context.getTheme();
                            if (systemTheme.getChangingConfigurations() != 0) {
                            }
                            Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                            Slog.i(TAG, "power manager systemReady");
                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                            Trace.traceEnd(2097152);
                            Trace.traceEnd(2097152);
                            Slog.i(TAG, "Fingerprint systemReady");
                            this.mFingerprintService.systemReady();
                            if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                            }
                            Slog.i(TAG, "Secrecy systemReady");
                            this.mSecrecyService.systemReady();
                            Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                            Slog.i(TAG, "Package systemReady");
                            this.mPackageManagerService.systemReady();
                            Trace.traceEnd(2097152);
                            Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                            Slog.i(TAG, "DisplayManager systemReady");
                            this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                            Trace.traceEnd(2097152);
                            networkManagementF = networkManagement;
                            networkStatsF = networkStats;
                            networkPolicyF = networkPolicy;
                            connectivityF = connectivity;
                            networkScoreF = networkScore;
                            locationF = location;
                            countryDetectorF = countryDetector;
                            networkTimeUpdaterF = networkTimeUpdater;
                            commonTimeMgmtServiceF = commonTimeMgmtService;
                            atlasF = atlas;
                            inputManagerF = inputManager;
                            telephonyRegistryF = telephonyRegistry;
                            mediaRouterF = mediaRouter;
                            mmsServiceF = mmsService;
                            multimediaServiceF = multimediaService;
                            perfServiceF = perfServiceMgr;
                            runningboosterF = runningbooster;
                            usageServiceF = usageService;
                            Slog.i(TAG, "Ams systemReady");
                            mOppoService = oppoService;
                            this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                        }
                        Trace.traceEnd(2097152);
                        if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.nan")) {
                        }
                        Slog.i(TAG, "wifi service");
                        this.mSystemServiceManager.startService(WIFI_P2P_SERVICE_CLASS);
                        this.mSystemServiceManager.startService(WIFI_SERVICE_CLASS);
                        this.mSystemServiceManager.startService("com.android.server.wifi.scanner.WifiScanningService");
                        if (disableRtt) {
                        }
                        this.mSystemServiceManager.startService(ETHERNET_SERVICE_CLASS);
                        traceBeginAndSlog("StartConnectivityService");
                        connectivityService = new ConnectivityService(context, networkManagement, networkStats, networkPolicy);
                        try {
                            ServiceManager.addService("connectivity", connectivityService);
                            networkStats.bindConnectivityManager(connectivityService);
                            networkPolicy.bindConnectivityManager(connectivityService);
                            connectivity = connectivityService;
                        } catch (Throwable th17) {
                            e22222222222 = th17;
                            connectivity = connectivityService;
                            reportWtf("starting Connectivity Service", e22222222222);
                            Trace.traceEnd(2097152);
                            traceBeginAndSlog("StartNsdService");
                            ServiceManager.addService("servicediscovery", NsdService.create(context));
                            Trace.traceEnd(2097152);
                            if ("1".equals(SystemProperties.get("persist.mtk.datashaping.support"))) {
                            }
                            traceBeginAndSlog("StartLwxService");
                            this.mSystemServiceManager.startService(LWX_SERVICE_CLASS);
                            Trace.traceEnd(2097152);
                            if (disableNonCoreServices) {
                            }
                            if (disableNonCoreServices) {
                            }
                            Trace.traceBegin(2097152, "WaitForAsecScan");
                            mountService.waitForAsecScan();
                            Trace.traceEnd(2097152);
                            Slog.i(TAG, "Oppo Expand Service");
                            ServiceManager.addService("OPPOExService", new OppoExService(context, wm));
                            if (this.mOppoLightsService != null) {
                            }
                            Slog.i(TAG, "NotificationManagerService");
                            this.mSystemServiceManager.startService(NotificationManagerService.class);
                            networkPolicy.bindNotificationManager(INotificationManager.Stub.asInterface(ServiceManager.getService(NotificationManagerService.NOTIFICATON_TITLE_NAME)));
                            if (context.getPackageManager().hasSystemFeature("oppo.support.single.partition")) {
                            }
                            if (disableLocation) {
                            }
                            traceBeginAndSlog("StartSearchManagerService");
                            this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                            Trace.traceEnd(2097152);
                            Slog.i(TAG, "Search Engine Service");
                            ServiceManager.addService("search_engine", new SearchEngineManagerService(context));
                            this.mSystemServiceManager.startService(DropBoxManagerService.class);
                            instance = AgingCriticalEvent.getInstance();
                            str = AgingCriticalEvent.EVENT_SYSTEM_BOOTUP;
                            strArr = new String[1];
                            strArr[0] = "systemserver pid:" + Process.myPid();
                            instance.writeEvent(str, strArr);
                            traceBeginAndSlog("StartWallpaperManagerService");
                            this.mSystemServiceManager.startService(WALLPAPER_SERVICE_CLASS);
                            Trace.traceEnd(2097152);
                            traceBeginAndSlog("StartAudioService");
                            this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                            Trace.traceEnd(2097152);
                            if ("1".equals(SystemProperties.get("ro.mtk_sensorhub_support"))) {
                            }
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartWiredAccessoryManager");
                            inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                            Trace.traceEnd(2097152);
                            if (disableNonCoreServices) {
                            }
                            this.mSystemServiceManager.startService(TwilightService.class);
                            if (NightDisplayController.isAvailable(context)) {
                            }
                            this.mSystemServiceManager.startService(JobSchedulerService.class);
                            this.mSystemServiceManager.startService(SoundTriggerService.class);
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartDiskStatsService");
                            ServiceManager.addService("diskstats", new DiskStatsService(context));
                            Trace.traceEnd(2097152);
                            Slog.i(TAG, "OPPO Usage Service");
                            oppoUsageService = new OppoUsageService(context);
                            ServiceManager.addService("usage", oppoUsageService);
                            usageService = oppoUsageService;
                            Slog.i(TAG, "Hypnus Service");
                            hypnusService = new HypnusService(context);
                            ServiceManager.addService("hypnus", hypnusService);
                            hypnusService2 = hypnusService;
                            Slog.i(TAG, "Oppo Service");
                            oppoService2 = new OppoService(context);
                            ServiceManager.addService("OPPO", oppoService2);
                            oppoService = oppoService2;
                            Slog.i(TAG, "+OppoMultimediaService");
                            oppoMultimediaService = new OppoMultimediaService(context);
                            ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                            Slog.i(TAG, "-OppoMultimediaService");
                            multimediaService = oppoMultimediaService;
                            Slog.i(TAG, "Oppo Customize Service");
                            ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                            if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                            }
                            traceBeginAndSlog("CabcService");
                            Slog.i(TAG, "Cabc Service");
                            cabcService = new CabcService(context);
                            ServiceManager.addService("cabc", cabcService);
                            cabcService2 = cabcService;
                            if (disableSamplingProfiler) {
                            }
                            traceBeginAndSlog("StartNetworkTimeUpdateService");
                            networkTimeUpdateService = new NetworkTimeUpdateService(context);
                            ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                            networkTimeUpdater = networkTimeUpdateService;
                            Trace.traceEnd(2097152);
                            traceBeginAndSlog("StartCommonTimeManagementService");
                            commonTimeManagementService = new CommonTimeManagementService(context);
                            ServiceManager.addService("commontime_management", commonTimeManagementService);
                            commonTimeMgmtService = commonTimeManagementService;
                            Trace.traceEnd(2097152);
                            if (disableNetwork) {
                            }
                            this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartAssetAtlasService");
                            assetAtlasService = new AssetAtlasService(context);
                            ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService);
                            atlas = assetAtlasService;
                            Trace.traceEnd(2097152);
                            if (disableNonCoreServices) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                            }
                            this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                            Slog.i(TAG, "Media Session Service");
                            this.mSystemServiceManager.startService(MediaSessionService.class);
                            if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                            }
                            if (disableNonCoreServices) {
                            }
                            Slog.i(TAG, "Oppo Engineer Service");
                            this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                            this.mSystemServiceManager.startService(LauncherAppsService.class);
                            if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                            }
                            Slog.i(TAG, "HDMI Manager Service");
                            mtkHdmiManagerService = new MtkHdmiManagerService(context);
                            ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                            mtkHdmiManagerService2 = mtkHdmiManagerService;
                            if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                            }
                            if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                            }
                            if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                            }
                            Slog.i(TAG, "Secrecy Service");
                            this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                            if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                            }
                            VMRuntime.getRuntime().startJitCompilation();
                            mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                            this.mSystemServiceManager.startService(RetailDemoModeService.class);
                            Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                            vibrator.systemReady();
                            Trace.traceEnd(2097152);
                            Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                            if (lockSettings != null) {
                            }
                            Trace.traceEnd(2097152);
                            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                            this.mSystemServiceManager.startBootPhase(500);
                            Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                            Slog.i(TAG, "wms systemReady");
                            wm.systemReady();
                            Trace.traceEnd(2097152);
                            config = wm.computeNewConfiguration();
                            metrics = new DisplayMetrics();
                            ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                            context.getResources().updateConfiguration(config, metrics);
                            systemTheme = context.getTheme();
                            if (systemTheme.getChangingConfigurations() != 0) {
                            }
                            Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                            Slog.i(TAG, "power manager systemReady");
                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                            Trace.traceEnd(2097152);
                            Trace.traceEnd(2097152);
                            Slog.i(TAG, "Fingerprint systemReady");
                            this.mFingerprintService.systemReady();
                            if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                            }
                            Slog.i(TAG, "Secrecy systemReady");
                            this.mSecrecyService.systemReady();
                            Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                            Slog.i(TAG, "Package systemReady");
                            this.mPackageManagerService.systemReady();
                            Trace.traceEnd(2097152);
                            Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                            Slog.i(TAG, "DisplayManager systemReady");
                            this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                            Trace.traceEnd(2097152);
                            networkManagementF = networkManagement;
                            networkStatsF = networkStats;
                            networkPolicyF = networkPolicy;
                            connectivityF = connectivity;
                            networkScoreF = networkScore;
                            locationF = location;
                            countryDetectorF = countryDetector;
                            networkTimeUpdaterF = networkTimeUpdater;
                            commonTimeMgmtServiceF = commonTimeMgmtService;
                            atlasF = atlas;
                            inputManagerF = inputManager;
                            telephonyRegistryF = telephonyRegistry;
                            mediaRouterF = mediaRouter;
                            mmsServiceF = mmsService;
                            multimediaServiceF = multimediaService;
                            perfServiceF = perfServiceMgr;
                            runningboosterF = runningbooster;
                            usageServiceF = usageService;
                            Slog.i(TAG, "Ams systemReady");
                            mOppoService = oppoService;
                            this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                        }
                        Trace.traceEnd(2097152);
                        traceBeginAndSlog("StartNsdService");
                        ServiceManager.addService("servicediscovery", NsdService.create(context));
                        Trace.traceEnd(2097152);
                        if ("1".equals(SystemProperties.get("persist.mtk.datashaping.support"))) {
                        }
                        traceBeginAndSlog("StartLwxService");
                        try {
                            this.mSystemServiceManager.startService(LWX_SERVICE_CLASS);
                        } catch (Throwable e222222222222) {
                            reportWtf("starting LwxService", e222222222222);
                        }
                        Trace.traceEnd(2097152);
                        if (disableNonCoreServices) {
                        }
                        if (disableNonCoreServices) {
                        }
                        Trace.traceBegin(2097152, "WaitForAsecScan");
                        mountService.waitForAsecScan();
                        Trace.traceEnd(2097152);
                        Slog.i(TAG, "Oppo Expand Service");
                        ServiceManager.addService("OPPOExService", new OppoExService(context, wm));
                        if (this.mOppoLightsService != null) {
                        }
                        Slog.i(TAG, "NotificationManagerService");
                        this.mSystemServiceManager.startService(NotificationManagerService.class);
                        networkPolicy.bindNotificationManager(INotificationManager.Stub.asInterface(ServiceManager.getService(NotificationManagerService.NOTIFICATON_TITLE_NAME)));
                        if (context.getPackageManager().hasSystemFeature("oppo.support.single.partition")) {
                        }
                        if (disableLocation) {
                        }
                        traceBeginAndSlog("StartSearchManagerService");
                        this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                        Trace.traceEnd(2097152);
                        Slog.i(TAG, "Search Engine Service");
                        ServiceManager.addService("search_engine", new SearchEngineManagerService(context));
                        this.mSystemServiceManager.startService(DropBoxManagerService.class);
                        instance = AgingCriticalEvent.getInstance();
                        str = AgingCriticalEvent.EVENT_SYSTEM_BOOTUP;
                        strArr = new String[1];
                        strArr[0] = "systemserver pid:" + Process.myPid();
                        instance.writeEvent(str, strArr);
                        traceBeginAndSlog("StartWallpaperManagerService");
                        this.mSystemServiceManager.startService(WALLPAPER_SERVICE_CLASS);
                        Trace.traceEnd(2097152);
                        traceBeginAndSlog("StartAudioService");
                        this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                        Trace.traceEnd(2097152);
                        if ("1".equals(SystemProperties.get("ro.mtk_sensorhub_support"))) {
                        }
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartWiredAccessoryManager");
                        inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                        Trace.traceEnd(2097152);
                        if (disableNonCoreServices) {
                        }
                        this.mSystemServiceManager.startService(TwilightService.class);
                        if (NightDisplayController.isAvailable(context)) {
                        }
                        this.mSystemServiceManager.startService(JobSchedulerService.class);
                        this.mSystemServiceManager.startService(SoundTriggerService.class);
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartDiskStatsService");
                        ServiceManager.addService("diskstats", new DiskStatsService(context));
                        Trace.traceEnd(2097152);
                        Slog.i(TAG, "OPPO Usage Service");
                        oppoUsageService = new OppoUsageService(context);
                        ServiceManager.addService("usage", oppoUsageService);
                        usageService = oppoUsageService;
                        Slog.i(TAG, "Hypnus Service");
                        hypnusService = new HypnusService(context);
                        ServiceManager.addService("hypnus", hypnusService);
                        hypnusService2 = hypnusService;
                        Slog.i(TAG, "Oppo Service");
                        oppoService2 = new OppoService(context);
                        ServiceManager.addService("OPPO", oppoService2);
                        oppoService = oppoService2;
                        Slog.i(TAG, "+OppoMultimediaService");
                        oppoMultimediaService = new OppoMultimediaService(context);
                        ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                        Slog.i(TAG, "-OppoMultimediaService");
                        multimediaService = oppoMultimediaService;
                        Slog.i(TAG, "Oppo Customize Service");
                        ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                        if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                        }
                        traceBeginAndSlog("CabcService");
                        Slog.i(TAG, "Cabc Service");
                        cabcService = new CabcService(context);
                        ServiceManager.addService("cabc", cabcService);
                        cabcService2 = cabcService;
                        if (disableSamplingProfiler) {
                        }
                        traceBeginAndSlog("StartNetworkTimeUpdateService");
                        networkTimeUpdateService = new NetworkTimeUpdateService(context);
                        ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                        networkTimeUpdater = networkTimeUpdateService;
                        Trace.traceEnd(2097152);
                        traceBeginAndSlog("StartCommonTimeManagementService");
                        commonTimeManagementService = new CommonTimeManagementService(context);
                        ServiceManager.addService("commontime_management", commonTimeManagementService);
                        commonTimeMgmtService = commonTimeManagementService;
                        Trace.traceEnd(2097152);
                        if (disableNetwork) {
                        }
                        this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartAssetAtlasService");
                        assetAtlasService = new AssetAtlasService(context);
                        ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService);
                        atlas = assetAtlasService;
                        Trace.traceEnd(2097152);
                        if (disableNonCoreServices) {
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                        }
                        this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                        Slog.i(TAG, "Media Session Service");
                        this.mSystemServiceManager.startService(MediaSessionService.class);
                        if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                        }
                        if (disableNonCoreServices) {
                        }
                        Slog.i(TAG, "Oppo Engineer Service");
                        this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                        this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                        this.mSystemServiceManager.startService(LauncherAppsService.class);
                        if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                        }
                        Slog.i(TAG, "HDMI Manager Service");
                        mtkHdmiManagerService = new MtkHdmiManagerService(context);
                        ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                        mtkHdmiManagerService2 = mtkHdmiManagerService;
                        if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                        }
                        if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                        }
                        if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                        }
                        Slog.i(TAG, "Secrecy Service");
                        this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                        this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                        if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                        }
                        VMRuntime.getRuntime().startJitCompilation();
                        mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                        this.mSystemServiceManager.startService(RetailDemoModeService.class);
                        Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                        vibrator.systemReady();
                        Trace.traceEnd(2097152);
                        Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                        if (lockSettings != null) {
                        }
                        Trace.traceEnd(2097152);
                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                        this.mSystemServiceManager.startBootPhase(500);
                        Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                        Slog.i(TAG, "wms systemReady");
                        wm.systemReady();
                        Trace.traceEnd(2097152);
                        config = wm.computeNewConfiguration();
                        metrics = new DisplayMetrics();
                        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                        context.getResources().updateConfiguration(config, metrics);
                        systemTheme = context.getTheme();
                        if (systemTheme.getChangingConfigurations() != 0) {
                        }
                        Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                        Slog.i(TAG, "power manager systemReady");
                        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                        Trace.traceEnd(2097152);
                        Trace.traceEnd(2097152);
                        Slog.i(TAG, "Fingerprint systemReady");
                        this.mFingerprintService.systemReady();
                        if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                        }
                        Slog.i(TAG, "Secrecy systemReady");
                        this.mSecrecyService.systemReady();
                        Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                        Slog.i(TAG, "Package systemReady");
                        this.mPackageManagerService.systemReady();
                        Trace.traceEnd(2097152);
                        Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                        Slog.i(TAG, "DisplayManager systemReady");
                        this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                        Trace.traceEnd(2097152);
                        networkManagementF = networkManagement;
                        networkStatsF = networkStats;
                        networkPolicyF = networkPolicy;
                        connectivityF = connectivity;
                        networkScoreF = networkScore;
                        locationF = location;
                        countryDetectorF = countryDetector;
                        networkTimeUpdaterF = networkTimeUpdater;
                        commonTimeMgmtServiceF = commonTimeMgmtService;
                        atlasF = atlas;
                        inputManagerF = inputManager;
                        telephonyRegistryF = telephonyRegistry;
                        mediaRouterF = mediaRouter;
                        mmsServiceF = mmsService;
                        multimediaServiceF = multimediaService;
                        perfServiceF = perfServiceMgr;
                        runningboosterF = runningbooster;
                        usageServiceF = usageService;
                        Slog.i(TAG, "Ams systemReady");
                        mOppoService = oppoService;
                        this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                    }
                } catch (Throwable th18) {
                    e222222222222 = th18;
                    reportWtf("starting Network Score Service", e222222222222);
                    Trace.traceEnd(2097152);
                    traceBeginAndSlog("StartNetworkStatsService");
                    networkStats = NetworkStatsService.create(context, networkManagement);
                    ServiceManager.addService("netstats", networkStats);
                    Trace.traceEnd(2097152);
                    traceBeginAndSlog("StartNetworkPolicyManagerService");
                    networkPolicyManagerService = new NetworkPolicyManagerService(context, this.mActivityManagerService, networkStats, networkManagement);
                    ServiceManager.addService("netpolicy", networkPolicyManagerService);
                    networkPolicy = networkPolicyManagerService;
                    Trace.traceEnd(2097152);
                    if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.nan")) {
                    }
                    Slog.i(TAG, "wifi service");
                    this.mSystemServiceManager.startService(WIFI_P2P_SERVICE_CLASS);
                    this.mSystemServiceManager.startService(WIFI_SERVICE_CLASS);
                    this.mSystemServiceManager.startService("com.android.server.wifi.scanner.WifiScanningService");
                    if (disableRtt) {
                    }
                    this.mSystemServiceManager.startService(ETHERNET_SERVICE_CLASS);
                    traceBeginAndSlog("StartConnectivityService");
                    connectivityService = new ConnectivityService(context, networkManagement, networkStats, networkPolicy);
                    ServiceManager.addService("connectivity", connectivityService);
                    networkStats.bindConnectivityManager(connectivityService);
                    networkPolicy.bindConnectivityManager(connectivityService);
                    connectivity = connectivityService;
                    Trace.traceEnd(2097152);
                    traceBeginAndSlog("StartNsdService");
                    ServiceManager.addService("servicediscovery", NsdService.create(context));
                    Trace.traceEnd(2097152);
                    if ("1".equals(SystemProperties.get("persist.mtk.datashaping.support"))) {
                    }
                    traceBeginAndSlog("StartLwxService");
                    this.mSystemServiceManager.startService(LWX_SERVICE_CLASS);
                    Trace.traceEnd(2097152);
                    if (disableNonCoreServices) {
                    }
                    if (disableNonCoreServices) {
                    }
                    Trace.traceBegin(2097152, "WaitForAsecScan");
                    mountService.waitForAsecScan();
                    Trace.traceEnd(2097152);
                    Slog.i(TAG, "Oppo Expand Service");
                    ServiceManager.addService("OPPOExService", new OppoExService(context, wm));
                    if (this.mOppoLightsService != null) {
                    }
                    Slog.i(TAG, "NotificationManagerService");
                    this.mSystemServiceManager.startService(NotificationManagerService.class);
                    networkPolicy.bindNotificationManager(INotificationManager.Stub.asInterface(ServiceManager.getService(NotificationManagerService.NOTIFICATON_TITLE_NAME)));
                    if (context.getPackageManager().hasSystemFeature("oppo.support.single.partition")) {
                    }
                    if (disableLocation) {
                    }
                    traceBeginAndSlog("StartSearchManagerService");
                    this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                    Trace.traceEnd(2097152);
                    Slog.i(TAG, "Search Engine Service");
                    ServiceManager.addService("search_engine", new SearchEngineManagerService(context));
                    this.mSystemServiceManager.startService(DropBoxManagerService.class);
                    instance = AgingCriticalEvent.getInstance();
                    str = AgingCriticalEvent.EVENT_SYSTEM_BOOTUP;
                    strArr = new String[1];
                    strArr[0] = "systemserver pid:" + Process.myPid();
                    instance.writeEvent(str, strArr);
                    traceBeginAndSlog("StartWallpaperManagerService");
                    this.mSystemServiceManager.startService(WALLPAPER_SERVICE_CLASS);
                    Trace.traceEnd(2097152);
                    traceBeginAndSlog("StartAudioService");
                    this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                    Trace.traceEnd(2097152);
                    if ("1".equals(SystemProperties.get("ro.mtk_sensorhub_support"))) {
                    }
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartWiredAccessoryManager");
                    inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                    Trace.traceEnd(2097152);
                    if (disableNonCoreServices) {
                    }
                    this.mSystemServiceManager.startService(TwilightService.class);
                    if (NightDisplayController.isAvailable(context)) {
                    }
                    this.mSystemServiceManager.startService(JobSchedulerService.class);
                    this.mSystemServiceManager.startService(SoundTriggerService.class);
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartDiskStatsService");
                    ServiceManager.addService("diskstats", new DiskStatsService(context));
                    Trace.traceEnd(2097152);
                    Slog.i(TAG, "OPPO Usage Service");
                    oppoUsageService = new OppoUsageService(context);
                    ServiceManager.addService("usage", oppoUsageService);
                    usageService = oppoUsageService;
                    Slog.i(TAG, "Hypnus Service");
                    hypnusService = new HypnusService(context);
                    ServiceManager.addService("hypnus", hypnusService);
                    hypnusService2 = hypnusService;
                    Slog.i(TAG, "Oppo Service");
                    oppoService2 = new OppoService(context);
                    ServiceManager.addService("OPPO", oppoService2);
                    oppoService = oppoService2;
                    Slog.i(TAG, "+OppoMultimediaService");
                    oppoMultimediaService = new OppoMultimediaService(context);
                    ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                    Slog.i(TAG, "-OppoMultimediaService");
                    multimediaService = oppoMultimediaService;
                    Slog.i(TAG, "Oppo Customize Service");
                    ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                    if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                    }
                    traceBeginAndSlog("CabcService");
                    Slog.i(TAG, "Cabc Service");
                    cabcService = new CabcService(context);
                    ServiceManager.addService("cabc", cabcService);
                    cabcService2 = cabcService;
                    if (disableSamplingProfiler) {
                    }
                    traceBeginAndSlog("StartNetworkTimeUpdateService");
                    networkTimeUpdateService = new NetworkTimeUpdateService(context);
                    ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                    networkTimeUpdater = networkTimeUpdateService;
                    Trace.traceEnd(2097152);
                    traceBeginAndSlog("StartCommonTimeManagementService");
                    commonTimeManagementService = new CommonTimeManagementService(context);
                    ServiceManager.addService("commontime_management", commonTimeManagementService);
                    commonTimeMgmtService = commonTimeManagementService;
                    Trace.traceEnd(2097152);
                    if (disableNetwork) {
                    }
                    this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartAssetAtlasService");
                    assetAtlasService = new AssetAtlasService(context);
                    ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService);
                    atlas = assetAtlasService;
                    Trace.traceEnd(2097152);
                    if (disableNonCoreServices) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                    }
                    this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                    Slog.i(TAG, "Media Session Service");
                    this.mSystemServiceManager.startService(MediaSessionService.class);
                    if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                    }
                    if (disableNonCoreServices) {
                    }
                    Slog.i(TAG, "Oppo Engineer Service");
                    this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                    if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                    }
                    Slog.i(TAG, "HDMI Manager Service");
                    mtkHdmiManagerService = new MtkHdmiManagerService(context);
                    ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                    mtkHdmiManagerService2 = mtkHdmiManagerService;
                    if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                    }
                    if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                    }
                    if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                    }
                    Slog.i(TAG, "Secrecy Service");
                    this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                    this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                    }
                    VMRuntime.getRuntime().startJitCompilation();
                    mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                    this.mSystemServiceManager.startService(RetailDemoModeService.class);
                    Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                    vibrator.systemReady();
                    Trace.traceEnd(2097152);
                    Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                    if (lockSettings != null) {
                    }
                    Trace.traceEnd(2097152);
                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                    this.mSystemServiceManager.startBootPhase(500);
                    Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                    Slog.i(TAG, "wms systemReady");
                    wm.systemReady();
                    Trace.traceEnd(2097152);
                    config = wm.computeNewConfiguration();
                    metrics = new DisplayMetrics();
                    ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                    context.getResources().updateConfiguration(config, metrics);
                    systemTheme = context.getTheme();
                    if (systemTheme.getChangingConfigurations() != 0) {
                    }
                    Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                    Slog.i(TAG, "power manager systemReady");
                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                    Trace.traceEnd(2097152);
                    Trace.traceEnd(2097152);
                    Slog.i(TAG, "Fingerprint systemReady");
                    this.mFingerprintService.systemReady();
                    if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                    }
                    Slog.i(TAG, "Secrecy systemReady");
                    this.mSecrecyService.systemReady();
                    Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                    Slog.i(TAG, "Package systemReady");
                    this.mPackageManagerService.systemReady();
                    Trace.traceEnd(2097152);
                    Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                    Slog.i(TAG, "DisplayManager systemReady");
                    this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                    Trace.traceEnd(2097152);
                    networkManagementF = networkManagement;
                    networkStatsF = networkStats;
                    networkPolicyF = networkPolicy;
                    connectivityF = connectivity;
                    networkScoreF = networkScore;
                    locationF = location;
                    countryDetectorF = countryDetector;
                    networkTimeUpdaterF = networkTimeUpdater;
                    commonTimeMgmtServiceF = commonTimeMgmtService;
                    atlasF = atlas;
                    inputManagerF = inputManager;
                    telephonyRegistryF = telephonyRegistry;
                    mediaRouterF = mediaRouter;
                    mmsServiceF = mmsService;
                    multimediaServiceF = multimediaService;
                    perfServiceF = perfServiceMgr;
                    runningboosterF = runningbooster;
                    usageServiceF = usageService;
                    Slog.i(TAG, "Ams systemReady");
                    mOppoService = oppoService;
                    this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                }
                Trace.traceEnd(2097152);
                traceBeginAndSlog("StartNetworkStatsService");
                try {
                    networkStats = NetworkStatsService.create(context, networkManagement);
                    ServiceManager.addService("netstats", networkStats);
                } catch (Throwable e2222222222222) {
                    reportWtf("starting NetworkStats Service", e2222222222222);
                }
                Trace.traceEnd(2097152);
                traceBeginAndSlog("StartNetworkPolicyManagerService");
                try {
                    networkPolicyManagerService = new NetworkPolicyManagerService(context, this.mActivityManagerService, networkStats, networkManagement);
                    ServiceManager.addService("netpolicy", networkPolicyManagerService);
                    networkPolicy = networkPolicyManagerService;
                } catch (Throwable th19) {
                    e2222222222222 = th19;
                    reportWtf("starting NetworkPolicy Service", e2222222222222);
                    Trace.traceEnd(2097152);
                    if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.nan")) {
                    }
                    Slog.i(TAG, "wifi service");
                    this.mSystemServiceManager.startService(WIFI_P2P_SERVICE_CLASS);
                    this.mSystemServiceManager.startService(WIFI_SERVICE_CLASS);
                    this.mSystemServiceManager.startService("com.android.server.wifi.scanner.WifiScanningService");
                    if (disableRtt) {
                    }
                    this.mSystemServiceManager.startService(ETHERNET_SERVICE_CLASS);
                    traceBeginAndSlog("StartConnectivityService");
                    connectivityService = new ConnectivityService(context, networkManagement, networkStats, networkPolicy);
                    ServiceManager.addService("connectivity", connectivityService);
                    networkStats.bindConnectivityManager(connectivityService);
                    networkPolicy.bindConnectivityManager(connectivityService);
                    connectivity = connectivityService;
                    Trace.traceEnd(2097152);
                    traceBeginAndSlog("StartNsdService");
                    ServiceManager.addService("servicediscovery", NsdService.create(context));
                    Trace.traceEnd(2097152);
                    if ("1".equals(SystemProperties.get("persist.mtk.datashaping.support"))) {
                    }
                    traceBeginAndSlog("StartLwxService");
                    this.mSystemServiceManager.startService(LWX_SERVICE_CLASS);
                    Trace.traceEnd(2097152);
                    if (disableNonCoreServices) {
                    }
                    if (disableNonCoreServices) {
                    }
                    Trace.traceBegin(2097152, "WaitForAsecScan");
                    mountService.waitForAsecScan();
                    Trace.traceEnd(2097152);
                    Slog.i(TAG, "Oppo Expand Service");
                    ServiceManager.addService("OPPOExService", new OppoExService(context, wm));
                    if (this.mOppoLightsService != null) {
                    }
                    Slog.i(TAG, "NotificationManagerService");
                    this.mSystemServiceManager.startService(NotificationManagerService.class);
                    networkPolicy.bindNotificationManager(INotificationManager.Stub.asInterface(ServiceManager.getService(NotificationManagerService.NOTIFICATON_TITLE_NAME)));
                    if (context.getPackageManager().hasSystemFeature("oppo.support.single.partition")) {
                    }
                    if (disableLocation) {
                    }
                    traceBeginAndSlog("StartSearchManagerService");
                    this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                    Trace.traceEnd(2097152);
                    Slog.i(TAG, "Search Engine Service");
                    ServiceManager.addService("search_engine", new SearchEngineManagerService(context));
                    this.mSystemServiceManager.startService(DropBoxManagerService.class);
                    instance = AgingCriticalEvent.getInstance();
                    str = AgingCriticalEvent.EVENT_SYSTEM_BOOTUP;
                    strArr = new String[1];
                    strArr[0] = "systemserver pid:" + Process.myPid();
                    instance.writeEvent(str, strArr);
                    traceBeginAndSlog("StartWallpaperManagerService");
                    this.mSystemServiceManager.startService(WALLPAPER_SERVICE_CLASS);
                    Trace.traceEnd(2097152);
                    traceBeginAndSlog("StartAudioService");
                    this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                    Trace.traceEnd(2097152);
                    if ("1".equals(SystemProperties.get("ro.mtk_sensorhub_support"))) {
                    }
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartWiredAccessoryManager");
                    inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                    Trace.traceEnd(2097152);
                    if (disableNonCoreServices) {
                    }
                    this.mSystemServiceManager.startService(TwilightService.class);
                    if (NightDisplayController.isAvailable(context)) {
                    }
                    this.mSystemServiceManager.startService(JobSchedulerService.class);
                    this.mSystemServiceManager.startService(SoundTriggerService.class);
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartDiskStatsService");
                    ServiceManager.addService("diskstats", new DiskStatsService(context));
                    Trace.traceEnd(2097152);
                    Slog.i(TAG, "OPPO Usage Service");
                    oppoUsageService = new OppoUsageService(context);
                    ServiceManager.addService("usage", oppoUsageService);
                    usageService = oppoUsageService;
                    Slog.i(TAG, "Hypnus Service");
                    hypnusService = new HypnusService(context);
                    ServiceManager.addService("hypnus", hypnusService);
                    hypnusService2 = hypnusService;
                    Slog.i(TAG, "Oppo Service");
                    oppoService2 = new OppoService(context);
                    ServiceManager.addService("OPPO", oppoService2);
                    oppoService = oppoService2;
                    Slog.i(TAG, "+OppoMultimediaService");
                    oppoMultimediaService = new OppoMultimediaService(context);
                    ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                    Slog.i(TAG, "-OppoMultimediaService");
                    multimediaService = oppoMultimediaService;
                    Slog.i(TAG, "Oppo Customize Service");
                    ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                    if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                    }
                    traceBeginAndSlog("CabcService");
                    Slog.i(TAG, "Cabc Service");
                    cabcService = new CabcService(context);
                    ServiceManager.addService("cabc", cabcService);
                    cabcService2 = cabcService;
                    if (disableSamplingProfiler) {
                    }
                    traceBeginAndSlog("StartNetworkTimeUpdateService");
                    networkTimeUpdateService = new NetworkTimeUpdateService(context);
                    ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                    networkTimeUpdater = networkTimeUpdateService;
                    Trace.traceEnd(2097152);
                    traceBeginAndSlog("StartCommonTimeManagementService");
                    commonTimeManagementService = new CommonTimeManagementService(context);
                    ServiceManager.addService("commontime_management", commonTimeManagementService);
                    commonTimeMgmtService = commonTimeManagementService;
                    Trace.traceEnd(2097152);
                    if (disableNetwork) {
                    }
                    this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartAssetAtlasService");
                    assetAtlasService = new AssetAtlasService(context);
                    ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService);
                    atlas = assetAtlasService;
                    Trace.traceEnd(2097152);
                    if (disableNonCoreServices) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                    }
                    this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                    Slog.i(TAG, "Media Session Service");
                    this.mSystemServiceManager.startService(MediaSessionService.class);
                    if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                    }
                    if (disableNonCoreServices) {
                    }
                    Slog.i(TAG, "Oppo Engineer Service");
                    this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                    if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                    }
                    Slog.i(TAG, "HDMI Manager Service");
                    mtkHdmiManagerService = new MtkHdmiManagerService(context);
                    ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                    mtkHdmiManagerService2 = mtkHdmiManagerService;
                    if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                    }
                    if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                    }
                    if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                    }
                    Slog.i(TAG, "Secrecy Service");
                    this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                    this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                    }
                    VMRuntime.getRuntime().startJitCompilation();
                    mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                    this.mSystemServiceManager.startService(RetailDemoModeService.class);
                    Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                    vibrator.systemReady();
                    Trace.traceEnd(2097152);
                    Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                    if (lockSettings != null) {
                    }
                    Trace.traceEnd(2097152);
                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                    this.mSystemServiceManager.startBootPhase(500);
                    Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                    Slog.i(TAG, "wms systemReady");
                    wm.systemReady();
                    Trace.traceEnd(2097152);
                    config = wm.computeNewConfiguration();
                    metrics = new DisplayMetrics();
                    ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                    context.getResources().updateConfiguration(config, metrics);
                    systemTheme = context.getTheme();
                    if (systemTheme.getChangingConfigurations() != 0) {
                    }
                    Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                    Slog.i(TAG, "power manager systemReady");
                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                    Trace.traceEnd(2097152);
                    Trace.traceEnd(2097152);
                    Slog.i(TAG, "Fingerprint systemReady");
                    this.mFingerprintService.systemReady();
                    if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                    }
                    Slog.i(TAG, "Secrecy systemReady");
                    this.mSecrecyService.systemReady();
                    Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                    Slog.i(TAG, "Package systemReady");
                    this.mPackageManagerService.systemReady();
                    Trace.traceEnd(2097152);
                    Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                    Slog.i(TAG, "DisplayManager systemReady");
                    this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                    Trace.traceEnd(2097152);
                    networkManagementF = networkManagement;
                    networkStatsF = networkStats;
                    networkPolicyF = networkPolicy;
                    connectivityF = connectivity;
                    networkScoreF = networkScore;
                    locationF = location;
                    countryDetectorF = countryDetector;
                    networkTimeUpdaterF = networkTimeUpdater;
                    commonTimeMgmtServiceF = commonTimeMgmtService;
                    atlasF = atlas;
                    inputManagerF = inputManager;
                    telephonyRegistryF = telephonyRegistry;
                    mediaRouterF = mediaRouter;
                    mmsServiceF = mmsService;
                    multimediaServiceF = multimediaService;
                    perfServiceF = perfServiceMgr;
                    runningboosterF = runningbooster;
                    usageServiceF = usageService;
                    Slog.i(TAG, "Ams systemReady");
                    mOppoService = oppoService;
                    this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                }
                Trace.traceEnd(2097152);
                if (context.getPackageManager().hasSystemFeature("android.hardware.wifi.nan")) {
                    this.mSystemServiceManager.startService(WIFI_NAN_SERVICE_CLASS);
                } else {
                    Slog.i(TAG, "No Wi-Fi NAN Service (NAN support Not Present)");
                }
                Slog.i(TAG, "wifi service");
                this.mSystemServiceManager.startService(WIFI_P2P_SERVICE_CLASS);
                this.mSystemServiceManager.startService(WIFI_SERVICE_CLASS);
                this.mSystemServiceManager.startService("com.android.server.wifi.scanner.WifiScanningService");
                if (disableRtt) {
                    this.mSystemServiceManager.startService("com.android.server.wifi.RttService");
                }
                if (this.mPackageManager.hasSystemFeature("android.hardware.ethernet") || this.mPackageManager.hasSystemFeature("android.hardware.usb.host")) {
                    this.mSystemServiceManager.startService(ETHERNET_SERVICE_CLASS);
                }
                traceBeginAndSlog("StartConnectivityService");
                try {
                    connectivityService = new ConnectivityService(context, networkManagement, networkStats, networkPolicy);
                    ServiceManager.addService("connectivity", connectivityService);
                    networkStats.bindConnectivityManager(connectivityService);
                    networkPolicy.bindConnectivityManager(connectivityService);
                    connectivity = connectivityService;
                } catch (Throwable th20) {
                    e2222222222222 = th20;
                    reportWtf("starting Connectivity Service", e2222222222222);
                    Trace.traceEnd(2097152);
                    traceBeginAndSlog("StartNsdService");
                    ServiceManager.addService("servicediscovery", NsdService.create(context));
                    Trace.traceEnd(2097152);
                    if ("1".equals(SystemProperties.get("persist.mtk.datashaping.support"))) {
                    }
                    traceBeginAndSlog("StartLwxService");
                    this.mSystemServiceManager.startService(LWX_SERVICE_CLASS);
                    Trace.traceEnd(2097152);
                    if (disableNonCoreServices) {
                    }
                    if (disableNonCoreServices) {
                    }
                    Trace.traceBegin(2097152, "WaitForAsecScan");
                    mountService.waitForAsecScan();
                    Trace.traceEnd(2097152);
                    Slog.i(TAG, "Oppo Expand Service");
                    ServiceManager.addService("OPPOExService", new OppoExService(context, wm));
                    if (this.mOppoLightsService != null) {
                    }
                    Slog.i(TAG, "NotificationManagerService");
                    this.mSystemServiceManager.startService(NotificationManagerService.class);
                    networkPolicy.bindNotificationManager(INotificationManager.Stub.asInterface(ServiceManager.getService(NotificationManagerService.NOTIFICATON_TITLE_NAME)));
                    if (context.getPackageManager().hasSystemFeature("oppo.support.single.partition")) {
                    }
                    if (disableLocation) {
                    }
                    traceBeginAndSlog("StartSearchManagerService");
                    this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                    Trace.traceEnd(2097152);
                    Slog.i(TAG, "Search Engine Service");
                    ServiceManager.addService("search_engine", new SearchEngineManagerService(context));
                    this.mSystemServiceManager.startService(DropBoxManagerService.class);
                    instance = AgingCriticalEvent.getInstance();
                    str = AgingCriticalEvent.EVENT_SYSTEM_BOOTUP;
                    strArr = new String[1];
                    strArr[0] = "systemserver pid:" + Process.myPid();
                    instance.writeEvent(str, strArr);
                    traceBeginAndSlog("StartWallpaperManagerService");
                    this.mSystemServiceManager.startService(WALLPAPER_SERVICE_CLASS);
                    Trace.traceEnd(2097152);
                    traceBeginAndSlog("StartAudioService");
                    this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                    Trace.traceEnd(2097152);
                    if ("1".equals(SystemProperties.get("ro.mtk_sensorhub_support"))) {
                    }
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartWiredAccessoryManager");
                    inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                    Trace.traceEnd(2097152);
                    if (disableNonCoreServices) {
                    }
                    this.mSystemServiceManager.startService(TwilightService.class);
                    if (NightDisplayController.isAvailable(context)) {
                    }
                    this.mSystemServiceManager.startService(JobSchedulerService.class);
                    this.mSystemServiceManager.startService(SoundTriggerService.class);
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartDiskStatsService");
                    ServiceManager.addService("diskstats", new DiskStatsService(context));
                    Trace.traceEnd(2097152);
                    Slog.i(TAG, "OPPO Usage Service");
                    oppoUsageService = new OppoUsageService(context);
                    ServiceManager.addService("usage", oppoUsageService);
                    usageService = oppoUsageService;
                    Slog.i(TAG, "Hypnus Service");
                    hypnusService = new HypnusService(context);
                    ServiceManager.addService("hypnus", hypnusService);
                    hypnusService2 = hypnusService;
                    Slog.i(TAG, "Oppo Service");
                    oppoService2 = new OppoService(context);
                    ServiceManager.addService("OPPO", oppoService2);
                    oppoService = oppoService2;
                    Slog.i(TAG, "+OppoMultimediaService");
                    oppoMultimediaService = new OppoMultimediaService(context);
                    ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                    Slog.i(TAG, "-OppoMultimediaService");
                    multimediaService = oppoMultimediaService;
                    Slog.i(TAG, "Oppo Customize Service");
                    ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                    if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                    }
                    traceBeginAndSlog("CabcService");
                    Slog.i(TAG, "Cabc Service");
                    cabcService = new CabcService(context);
                    ServiceManager.addService("cabc", cabcService);
                    cabcService2 = cabcService;
                    if (disableSamplingProfiler) {
                    }
                    traceBeginAndSlog("StartNetworkTimeUpdateService");
                    networkTimeUpdateService = new NetworkTimeUpdateService(context);
                    ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                    networkTimeUpdater = networkTimeUpdateService;
                    Trace.traceEnd(2097152);
                    traceBeginAndSlog("StartCommonTimeManagementService");
                    commonTimeManagementService = new CommonTimeManagementService(context);
                    ServiceManager.addService("commontime_management", commonTimeManagementService);
                    commonTimeMgmtService = commonTimeManagementService;
                    Trace.traceEnd(2097152);
                    if (disableNetwork) {
                    }
                    this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartAssetAtlasService");
                    assetAtlasService = new AssetAtlasService(context);
                    ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService);
                    atlas = assetAtlasService;
                    Trace.traceEnd(2097152);
                    if (disableNonCoreServices) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                    }
                    this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                    Slog.i(TAG, "Media Session Service");
                    this.mSystemServiceManager.startService(MediaSessionService.class);
                    if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                    }
                    if (disableNonCoreServices) {
                    }
                    Slog.i(TAG, "Oppo Engineer Service");
                    this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                    if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                    }
                    Slog.i(TAG, "HDMI Manager Service");
                    mtkHdmiManagerService = new MtkHdmiManagerService(context);
                    ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                    mtkHdmiManagerService2 = mtkHdmiManagerService;
                    if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                    }
                    if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                    }
                    if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                    }
                    Slog.i(TAG, "Secrecy Service");
                    this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                    this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                    }
                    VMRuntime.getRuntime().startJitCompilation();
                    mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                    this.mSystemServiceManager.startService(RetailDemoModeService.class);
                    Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                    vibrator.systemReady();
                    Trace.traceEnd(2097152);
                    Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                    if (lockSettings != null) {
                    }
                    Trace.traceEnd(2097152);
                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                    this.mSystemServiceManager.startBootPhase(500);
                    Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                    Slog.i(TAG, "wms systemReady");
                    wm.systemReady();
                    Trace.traceEnd(2097152);
                    config = wm.computeNewConfiguration();
                    metrics = new DisplayMetrics();
                    ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                    context.getResources().updateConfiguration(config, metrics);
                    systemTheme = context.getTheme();
                    if (systemTheme.getChangingConfigurations() != 0) {
                    }
                    Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                    Slog.i(TAG, "power manager systemReady");
                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                    Trace.traceEnd(2097152);
                    Trace.traceEnd(2097152);
                    Slog.i(TAG, "Fingerprint systemReady");
                    this.mFingerprintService.systemReady();
                    if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                    }
                    Slog.i(TAG, "Secrecy systemReady");
                    this.mSecrecyService.systemReady();
                    Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                    Slog.i(TAG, "Package systemReady");
                    this.mPackageManagerService.systemReady();
                    Trace.traceEnd(2097152);
                    Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                    Slog.i(TAG, "DisplayManager systemReady");
                    this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                    Trace.traceEnd(2097152);
                    networkManagementF = networkManagement;
                    networkStatsF = networkStats;
                    networkPolicyF = networkPolicy;
                    connectivityF = connectivity;
                    networkScoreF = networkScore;
                    locationF = location;
                    countryDetectorF = countryDetector;
                    networkTimeUpdaterF = networkTimeUpdater;
                    commonTimeMgmtServiceF = commonTimeMgmtService;
                    atlasF = atlas;
                    inputManagerF = inputManager;
                    telephonyRegistryF = telephonyRegistry;
                    mediaRouterF = mediaRouter;
                    mmsServiceF = mmsService;
                    multimediaServiceF = multimediaService;
                    perfServiceF = perfServiceMgr;
                    runningboosterF = runningbooster;
                    usageServiceF = usageService;
                    Slog.i(TAG, "Ams systemReady");
                    mOppoService = oppoService;
                    this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                }
                Trace.traceEnd(2097152);
                traceBeginAndSlog("StartNsdService");
                try {
                    ServiceManager.addService("servicediscovery", NsdService.create(context));
                } catch (Throwable e22222222222222) {
                    reportWtf("starting Service Discovery Service", e22222222222222);
                }
                Trace.traceEnd(2097152);
                if ("1".equals(SystemProperties.get("persist.mtk.datashaping.support"))) {
                    traceBeginAndSlog("StartDataShapingService");
                    try {
                        this.mSystemServiceManager.startService(DATASHPAING_SERVICE_CLASS);
                    } catch (Throwable e222222222222222) {
                        reportWtf("starting DataShapingService", e222222222222222);
                    }
                    Trace.traceEnd(2097152);
                }
                if ("1".equals(SystemProperties.get("ro.mtk_lwa_support")) || "1".equals(SystemProperties.get("ro.mtk_lwi_support"))) {
                    traceBeginAndSlog("StartLwxService");
                    this.mSystemServiceManager.startService(LWX_SERVICE_CLASS);
                    Trace.traceEnd(2097152);
                }
            }
            if (disableNonCoreServices) {
                traceBeginAndSlog("StartUpdateLockService");
                try {
                    ServiceManager.addService("updatelock", new UpdateLockService(context));
                } catch (Throwable e2222222222222222) {
                    reportWtf("starting UpdateLockService", e2222222222222222);
                }
                Trace.traceEnd(2097152);
            }
            if (disableNonCoreServices) {
                this.mSystemServiceManager.startService(RecoverySystemService.class);
            }
            if (!(mountService == null || this.mOnlyCore)) {
                Trace.traceBegin(2097152, "WaitForAsecScan");
                mountService.waitForAsecScan();
                Trace.traceEnd(2097152);
            }
            try {
                Slog.i(TAG, "Oppo Expand Service");
                ServiceManager.addService("OPPOExService", new OppoExService(context, wm));
            } catch (Throwable e22222222222222222) {
                Slog.e(TAG, "Failure starting Oppo Service", e22222222222222222);
            }
            try {
                if (this.mOppoLightsService != null) {
                    Slog.i(TAG, "OppoLightsService.systemReady");
                    this.mOppoLightsService.systemReady();
                }
            } catch (Throwable e222222222222222222) {
                reportWtf("making OppoLightsService ready", e222222222222222222);
            }
            Slog.i(TAG, "NotificationManagerService");
            this.mSystemServiceManager.startService(NotificationManagerService.class);
            networkPolicy.bindNotificationManager(INotificationManager.Stub.asInterface(ServiceManager.getService(NotificationManagerService.NOTIFICATON_TITLE_NAME)));
            if (context.getPackageManager().hasSystemFeature("oppo.support.single.partition")) {
                this.mSystemServiceManager.startService(OppoDeviceStorageMonitorService.class);
                Slog.i(TAG, "single partition, add OppoDeviceStorageMonitorService");
            } else {
                this.mSystemServiceManager.startService(DeviceStorageMonitorService.class);
                Slog.i(TAG, "not single partition, add DeviceStorageMonitorService");
            }
            if (disableLocation) {
                traceBeginAndSlog("StartLocationManagerService");
                try {
                    LocationManagerService locationManagerService = new LocationManagerService(context);
                    try {
                        ServiceManager.addService("location", locationManagerService);
                        location = locationManagerService;
                    } catch (Throwable th21) {
                        e222222222222222222 = th21;
                        location = locationManagerService;
                        reportWtf("starting Location Manager", e222222222222222222);
                        Trace.traceEnd(2097152);
                        traceBeginAndSlog("StartCountryDetectorService");
                        countryDetectorService = new CountryDetectorService(context);
                        try {
                            ServiceManager.addService("country_detector", countryDetectorService);
                            countryDetector = countryDetectorService;
                        } catch (Throwable th22) {
                            e222222222222222222 = th22;
                            countryDetector = countryDetectorService;
                            reportWtf("starting Country Detector", e222222222222222222);
                            Trace.traceEnd(2097152);
                            traceBeginAndSlog("StartSearchManagerService");
                            this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                            Trace.traceEnd(2097152);
                            Slog.i(TAG, "Search Engine Service");
                            ServiceManager.addService("search_engine", new SearchEngineManagerService(context));
                            this.mSystemServiceManager.startService(DropBoxManagerService.class);
                            instance = AgingCriticalEvent.getInstance();
                            str = AgingCriticalEvent.EVENT_SYSTEM_BOOTUP;
                            strArr = new String[1];
                            strArr[0] = "systemserver pid:" + Process.myPid();
                            instance.writeEvent(str, strArr);
                            traceBeginAndSlog("StartWallpaperManagerService");
                            this.mSystemServiceManager.startService(WALLPAPER_SERVICE_CLASS);
                            Trace.traceEnd(2097152);
                            traceBeginAndSlog("StartAudioService");
                            this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                            Trace.traceEnd(2097152);
                            if ("1".equals(SystemProperties.get("ro.mtk_sensorhub_support"))) {
                            }
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartWiredAccessoryManager");
                            inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                            Trace.traceEnd(2097152);
                            if (disableNonCoreServices) {
                            }
                            this.mSystemServiceManager.startService(TwilightService.class);
                            if (NightDisplayController.isAvailable(context)) {
                            }
                            this.mSystemServiceManager.startService(JobSchedulerService.class);
                            this.mSystemServiceManager.startService(SoundTriggerService.class);
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartDiskStatsService");
                            ServiceManager.addService("diskstats", new DiskStatsService(context));
                            Trace.traceEnd(2097152);
                            Slog.i(TAG, "OPPO Usage Service");
                            oppoUsageService = new OppoUsageService(context);
                            ServiceManager.addService("usage", oppoUsageService);
                            usageService = oppoUsageService;
                            Slog.i(TAG, "Hypnus Service");
                            hypnusService = new HypnusService(context);
                            ServiceManager.addService("hypnus", hypnusService);
                            hypnusService2 = hypnusService;
                            Slog.i(TAG, "Oppo Service");
                            oppoService2 = new OppoService(context);
                            ServiceManager.addService("OPPO", oppoService2);
                            oppoService = oppoService2;
                            Slog.i(TAG, "+OppoMultimediaService");
                            oppoMultimediaService = new OppoMultimediaService(context);
                            ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                            Slog.i(TAG, "-OppoMultimediaService");
                            multimediaService = oppoMultimediaService;
                            Slog.i(TAG, "Oppo Customize Service");
                            ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                            if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                            }
                            traceBeginAndSlog("CabcService");
                            Slog.i(TAG, "Cabc Service");
                            cabcService = new CabcService(context);
                            ServiceManager.addService("cabc", cabcService);
                            cabcService2 = cabcService;
                            if (disableSamplingProfiler) {
                            }
                            traceBeginAndSlog("StartNetworkTimeUpdateService");
                            networkTimeUpdateService = new NetworkTimeUpdateService(context);
                            ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                            networkTimeUpdater = networkTimeUpdateService;
                            Trace.traceEnd(2097152);
                            traceBeginAndSlog("StartCommonTimeManagementService");
                            commonTimeManagementService = new CommonTimeManagementService(context);
                            ServiceManager.addService("commontime_management", commonTimeManagementService);
                            commonTimeMgmtService = commonTimeManagementService;
                            Trace.traceEnd(2097152);
                            if (disableNetwork) {
                            }
                            this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartAssetAtlasService");
                            assetAtlasService = new AssetAtlasService(context);
                            ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService);
                            atlas = assetAtlasService;
                            Trace.traceEnd(2097152);
                            if (disableNonCoreServices) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                            }
                            this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                            Slog.i(TAG, "Media Session Service");
                            this.mSystemServiceManager.startService(MediaSessionService.class);
                            if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                            }
                            if (disableNonCoreServices) {
                            }
                            Slog.i(TAG, "Oppo Engineer Service");
                            this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                            this.mSystemServiceManager.startService(LauncherAppsService.class);
                            if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                            }
                            Slog.i(TAG, "HDMI Manager Service");
                            mtkHdmiManagerService = new MtkHdmiManagerService(context);
                            ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                            mtkHdmiManagerService2 = mtkHdmiManagerService;
                            if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                            }
                            if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                            }
                            if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                            }
                            Slog.i(TAG, "Secrecy Service");
                            this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                            if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                            }
                            VMRuntime.getRuntime().startJitCompilation();
                            mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                            this.mSystemServiceManager.startService(RetailDemoModeService.class);
                            Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                            vibrator.systemReady();
                            Trace.traceEnd(2097152);
                            Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                            if (lockSettings != null) {
                            }
                            Trace.traceEnd(2097152);
                            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                            this.mSystemServiceManager.startBootPhase(500);
                            Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                            Slog.i(TAG, "wms systemReady");
                            wm.systemReady();
                            Trace.traceEnd(2097152);
                            config = wm.computeNewConfiguration();
                            metrics = new DisplayMetrics();
                            ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                            context.getResources().updateConfiguration(config, metrics);
                            systemTheme = context.getTheme();
                            if (systemTheme.getChangingConfigurations() != 0) {
                            }
                            Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                            Slog.i(TAG, "power manager systemReady");
                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                            Trace.traceEnd(2097152);
                            Trace.traceEnd(2097152);
                            Slog.i(TAG, "Fingerprint systemReady");
                            this.mFingerprintService.systemReady();
                            if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                            }
                            Slog.i(TAG, "Secrecy systemReady");
                            this.mSecrecyService.systemReady();
                            Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                            Slog.i(TAG, "Package systemReady");
                            this.mPackageManagerService.systemReady();
                            Trace.traceEnd(2097152);
                            Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                            Slog.i(TAG, "DisplayManager systemReady");
                            this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                            Trace.traceEnd(2097152);
                            networkManagementF = networkManagement;
                            networkStatsF = networkStats;
                            networkPolicyF = networkPolicy;
                            connectivityF = connectivity;
                            networkScoreF = networkScore;
                            locationF = location;
                            countryDetectorF = countryDetector;
                            networkTimeUpdaterF = networkTimeUpdater;
                            commonTimeMgmtServiceF = commonTimeMgmtService;
                            atlasF = atlas;
                            inputManagerF = inputManager;
                            telephonyRegistryF = telephonyRegistry;
                            mediaRouterF = mediaRouter;
                            mmsServiceF = mmsService;
                            multimediaServiceF = multimediaService;
                            perfServiceF = perfServiceMgr;
                            runningboosterF = runningbooster;
                            usageServiceF = usageService;
                            Slog.i(TAG, "Ams systemReady");
                            mOppoService = oppoService;
                            this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                        }
                        Trace.traceEnd(2097152);
                        traceBeginAndSlog("StartSearchManagerService");
                        this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                        Trace.traceEnd(2097152);
                        Slog.i(TAG, "Search Engine Service");
                        ServiceManager.addService("search_engine", new SearchEngineManagerService(context));
                        this.mSystemServiceManager.startService(DropBoxManagerService.class);
                        instance = AgingCriticalEvent.getInstance();
                        str = AgingCriticalEvent.EVENT_SYSTEM_BOOTUP;
                        strArr = new String[1];
                        strArr[0] = "systemserver pid:" + Process.myPid();
                        instance.writeEvent(str, strArr);
                        traceBeginAndSlog("StartWallpaperManagerService");
                        this.mSystemServiceManager.startService(WALLPAPER_SERVICE_CLASS);
                        Trace.traceEnd(2097152);
                        traceBeginAndSlog("StartAudioService");
                        this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                        Trace.traceEnd(2097152);
                        if ("1".equals(SystemProperties.get("ro.mtk_sensorhub_support"))) {
                        }
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartWiredAccessoryManager");
                        inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                        Trace.traceEnd(2097152);
                        if (disableNonCoreServices) {
                        }
                        this.mSystemServiceManager.startService(TwilightService.class);
                        if (NightDisplayController.isAvailable(context)) {
                        }
                        this.mSystemServiceManager.startService(JobSchedulerService.class);
                        this.mSystemServiceManager.startService(SoundTriggerService.class);
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartDiskStatsService");
                        ServiceManager.addService("diskstats", new DiskStatsService(context));
                        Trace.traceEnd(2097152);
                        Slog.i(TAG, "OPPO Usage Service");
                        oppoUsageService = new OppoUsageService(context);
                        ServiceManager.addService("usage", oppoUsageService);
                        usageService = oppoUsageService;
                        Slog.i(TAG, "Hypnus Service");
                        hypnusService = new HypnusService(context);
                        ServiceManager.addService("hypnus", hypnusService);
                        hypnusService2 = hypnusService;
                        Slog.i(TAG, "Oppo Service");
                        oppoService2 = new OppoService(context);
                        ServiceManager.addService("OPPO", oppoService2);
                        oppoService = oppoService2;
                        Slog.i(TAG, "+OppoMultimediaService");
                        oppoMultimediaService = new OppoMultimediaService(context);
                        ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                        Slog.i(TAG, "-OppoMultimediaService");
                        multimediaService = oppoMultimediaService;
                        Slog.i(TAG, "Oppo Customize Service");
                        ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                        if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                        }
                        traceBeginAndSlog("CabcService");
                        Slog.i(TAG, "Cabc Service");
                        cabcService = new CabcService(context);
                        ServiceManager.addService("cabc", cabcService);
                        cabcService2 = cabcService;
                        if (disableSamplingProfiler) {
                        }
                        traceBeginAndSlog("StartNetworkTimeUpdateService");
                        networkTimeUpdateService = new NetworkTimeUpdateService(context);
                        ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                        networkTimeUpdater = networkTimeUpdateService;
                        Trace.traceEnd(2097152);
                        traceBeginAndSlog("StartCommonTimeManagementService");
                        commonTimeManagementService = new CommonTimeManagementService(context);
                        ServiceManager.addService("commontime_management", commonTimeManagementService);
                        commonTimeMgmtService = commonTimeManagementService;
                        Trace.traceEnd(2097152);
                        if (disableNetwork) {
                        }
                        this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartAssetAtlasService");
                        assetAtlasService = new AssetAtlasService(context);
                        ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService);
                        atlas = assetAtlasService;
                        Trace.traceEnd(2097152);
                        if (disableNonCoreServices) {
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                        }
                        this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                        Slog.i(TAG, "Media Session Service");
                        this.mSystemServiceManager.startService(MediaSessionService.class);
                        if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                        }
                        if (disableNonCoreServices) {
                        }
                        Slog.i(TAG, "Oppo Engineer Service");
                        this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                        this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                        this.mSystemServiceManager.startService(LauncherAppsService.class);
                        if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                        }
                        Slog.i(TAG, "HDMI Manager Service");
                        mtkHdmiManagerService = new MtkHdmiManagerService(context);
                        ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                        mtkHdmiManagerService2 = mtkHdmiManagerService;
                        if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                        }
                        if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                        }
                        if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                        }
                        Slog.i(TAG, "Secrecy Service");
                        this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                        this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                        if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                        }
                        VMRuntime.getRuntime().startJitCompilation();
                        mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                        this.mSystemServiceManager.startService(RetailDemoModeService.class);
                        Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                        vibrator.systemReady();
                        Trace.traceEnd(2097152);
                        Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                        if (lockSettings != null) {
                        }
                        Trace.traceEnd(2097152);
                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                        this.mSystemServiceManager.startBootPhase(500);
                        Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                        Slog.i(TAG, "wms systemReady");
                        wm.systemReady();
                        Trace.traceEnd(2097152);
                        config = wm.computeNewConfiguration();
                        metrics = new DisplayMetrics();
                        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                        context.getResources().updateConfiguration(config, metrics);
                        systemTheme = context.getTheme();
                        if (systemTheme.getChangingConfigurations() != 0) {
                        }
                        Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                        Slog.i(TAG, "power manager systemReady");
                        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                        Trace.traceEnd(2097152);
                        Trace.traceEnd(2097152);
                        Slog.i(TAG, "Fingerprint systemReady");
                        this.mFingerprintService.systemReady();
                        if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                        }
                        Slog.i(TAG, "Secrecy systemReady");
                        this.mSecrecyService.systemReady();
                        Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                        Slog.i(TAG, "Package systemReady");
                        this.mPackageManagerService.systemReady();
                        Trace.traceEnd(2097152);
                        Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                        Slog.i(TAG, "DisplayManager systemReady");
                        this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                        Trace.traceEnd(2097152);
                        networkManagementF = networkManagement;
                        networkStatsF = networkStats;
                        networkPolicyF = networkPolicy;
                        connectivityF = connectivity;
                        networkScoreF = networkScore;
                        locationF = location;
                        countryDetectorF = countryDetector;
                        networkTimeUpdaterF = networkTimeUpdater;
                        commonTimeMgmtServiceF = commonTimeMgmtService;
                        atlasF = atlas;
                        inputManagerF = inputManager;
                        telephonyRegistryF = telephonyRegistry;
                        mediaRouterF = mediaRouter;
                        mmsServiceF = mmsService;
                        multimediaServiceF = multimediaService;
                        perfServiceF = perfServiceMgr;
                        runningboosterF = runningbooster;
                        usageServiceF = usageService;
                        Slog.i(TAG, "Ams systemReady");
                        mOppoService = oppoService;
                        this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                    }
                } catch (Throwable th23) {
                    e222222222222222222 = th23;
                    reportWtf("starting Location Manager", e222222222222222222);
                    Trace.traceEnd(2097152);
                    traceBeginAndSlog("StartCountryDetectorService");
                    countryDetectorService = new CountryDetectorService(context);
                    ServiceManager.addService("country_detector", countryDetectorService);
                    countryDetector = countryDetectorService;
                    Trace.traceEnd(2097152);
                    traceBeginAndSlog("StartSearchManagerService");
                    this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                    Trace.traceEnd(2097152);
                    Slog.i(TAG, "Search Engine Service");
                    ServiceManager.addService("search_engine", new SearchEngineManagerService(context));
                    this.mSystemServiceManager.startService(DropBoxManagerService.class);
                    instance = AgingCriticalEvent.getInstance();
                    str = AgingCriticalEvent.EVENT_SYSTEM_BOOTUP;
                    strArr = new String[1];
                    strArr[0] = "systemserver pid:" + Process.myPid();
                    instance.writeEvent(str, strArr);
                    traceBeginAndSlog("StartWallpaperManagerService");
                    this.mSystemServiceManager.startService(WALLPAPER_SERVICE_CLASS);
                    Trace.traceEnd(2097152);
                    traceBeginAndSlog("StartAudioService");
                    this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                    Trace.traceEnd(2097152);
                    if ("1".equals(SystemProperties.get("ro.mtk_sensorhub_support"))) {
                    }
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartWiredAccessoryManager");
                    inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                    Trace.traceEnd(2097152);
                    if (disableNonCoreServices) {
                    }
                    this.mSystemServiceManager.startService(TwilightService.class);
                    if (NightDisplayController.isAvailable(context)) {
                    }
                    this.mSystemServiceManager.startService(JobSchedulerService.class);
                    this.mSystemServiceManager.startService(SoundTriggerService.class);
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartDiskStatsService");
                    ServiceManager.addService("diskstats", new DiskStatsService(context));
                    Trace.traceEnd(2097152);
                    Slog.i(TAG, "OPPO Usage Service");
                    oppoUsageService = new OppoUsageService(context);
                    ServiceManager.addService("usage", oppoUsageService);
                    usageService = oppoUsageService;
                    Slog.i(TAG, "Hypnus Service");
                    hypnusService = new HypnusService(context);
                    ServiceManager.addService("hypnus", hypnusService);
                    hypnusService2 = hypnusService;
                    Slog.i(TAG, "Oppo Service");
                    oppoService2 = new OppoService(context);
                    ServiceManager.addService("OPPO", oppoService2);
                    oppoService = oppoService2;
                    Slog.i(TAG, "+OppoMultimediaService");
                    oppoMultimediaService = new OppoMultimediaService(context);
                    ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                    Slog.i(TAG, "-OppoMultimediaService");
                    multimediaService = oppoMultimediaService;
                    Slog.i(TAG, "Oppo Customize Service");
                    ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                    if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                    }
                    traceBeginAndSlog("CabcService");
                    Slog.i(TAG, "Cabc Service");
                    cabcService = new CabcService(context);
                    ServiceManager.addService("cabc", cabcService);
                    cabcService2 = cabcService;
                    if (disableSamplingProfiler) {
                    }
                    traceBeginAndSlog("StartNetworkTimeUpdateService");
                    networkTimeUpdateService = new NetworkTimeUpdateService(context);
                    ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                    networkTimeUpdater = networkTimeUpdateService;
                    Trace.traceEnd(2097152);
                    traceBeginAndSlog("StartCommonTimeManagementService");
                    commonTimeManagementService = new CommonTimeManagementService(context);
                    ServiceManager.addService("commontime_management", commonTimeManagementService);
                    commonTimeMgmtService = commonTimeManagementService;
                    Trace.traceEnd(2097152);
                    if (disableNetwork) {
                    }
                    this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartAssetAtlasService");
                    assetAtlasService = new AssetAtlasService(context);
                    ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService);
                    atlas = assetAtlasService;
                    Trace.traceEnd(2097152);
                    if (disableNonCoreServices) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                    }
                    this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                    Slog.i(TAG, "Media Session Service");
                    this.mSystemServiceManager.startService(MediaSessionService.class);
                    if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                    }
                    if (disableNonCoreServices) {
                    }
                    Slog.i(TAG, "Oppo Engineer Service");
                    this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                    if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                    }
                    Slog.i(TAG, "HDMI Manager Service");
                    mtkHdmiManagerService = new MtkHdmiManagerService(context);
                    ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                    mtkHdmiManagerService2 = mtkHdmiManagerService;
                    if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                    }
                    if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                    }
                    if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                    }
                    Slog.i(TAG, "Secrecy Service");
                    this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                    this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                    }
                    VMRuntime.getRuntime().startJitCompilation();
                    mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                    this.mSystemServiceManager.startService(RetailDemoModeService.class);
                    Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                    vibrator.systemReady();
                    Trace.traceEnd(2097152);
                    Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                    if (lockSettings != null) {
                    }
                    Trace.traceEnd(2097152);
                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                    this.mSystemServiceManager.startBootPhase(500);
                    Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                    Slog.i(TAG, "wms systemReady");
                    wm.systemReady();
                    Trace.traceEnd(2097152);
                    config = wm.computeNewConfiguration();
                    metrics = new DisplayMetrics();
                    ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                    context.getResources().updateConfiguration(config, metrics);
                    systemTheme = context.getTheme();
                    if (systemTheme.getChangingConfigurations() != 0) {
                    }
                    Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                    Slog.i(TAG, "power manager systemReady");
                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                    Trace.traceEnd(2097152);
                    Trace.traceEnd(2097152);
                    Slog.i(TAG, "Fingerprint systemReady");
                    this.mFingerprintService.systemReady();
                    if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                    }
                    Slog.i(TAG, "Secrecy systemReady");
                    this.mSecrecyService.systemReady();
                    Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                    Slog.i(TAG, "Package systemReady");
                    this.mPackageManagerService.systemReady();
                    Trace.traceEnd(2097152);
                    Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                    Slog.i(TAG, "DisplayManager systemReady");
                    this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                    Trace.traceEnd(2097152);
                    networkManagementF = networkManagement;
                    networkStatsF = networkStats;
                    networkPolicyF = networkPolicy;
                    connectivityF = connectivity;
                    networkScoreF = networkScore;
                    locationF = location;
                    countryDetectorF = countryDetector;
                    networkTimeUpdaterF = networkTimeUpdater;
                    commonTimeMgmtServiceF = commonTimeMgmtService;
                    atlasF = atlas;
                    inputManagerF = inputManager;
                    telephonyRegistryF = telephonyRegistry;
                    mediaRouterF = mediaRouter;
                    mmsServiceF = mmsService;
                    multimediaServiceF = multimediaService;
                    perfServiceF = perfServiceMgr;
                    runningboosterF = runningbooster;
                    usageServiceF = usageService;
                    Slog.i(TAG, "Ams systemReady");
                    mOppoService = oppoService;
                    this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                }
                Trace.traceEnd(2097152);
                traceBeginAndSlog("StartCountryDetectorService");
                try {
                    countryDetectorService = new CountryDetectorService(context);
                    ServiceManager.addService("country_detector", countryDetectorService);
                    countryDetector = countryDetectorService;
                } catch (Throwable th24) {
                    e222222222222222222 = th24;
                    reportWtf("starting Country Detector", e222222222222222222);
                    Trace.traceEnd(2097152);
                    traceBeginAndSlog("StartSearchManagerService");
                    this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                    Trace.traceEnd(2097152);
                    Slog.i(TAG, "Search Engine Service");
                    ServiceManager.addService("search_engine", new SearchEngineManagerService(context));
                    this.mSystemServiceManager.startService(DropBoxManagerService.class);
                    instance = AgingCriticalEvent.getInstance();
                    str = AgingCriticalEvent.EVENT_SYSTEM_BOOTUP;
                    strArr = new String[1];
                    strArr[0] = "systemserver pid:" + Process.myPid();
                    instance.writeEvent(str, strArr);
                    traceBeginAndSlog("StartWallpaperManagerService");
                    this.mSystemServiceManager.startService(WALLPAPER_SERVICE_CLASS);
                    Trace.traceEnd(2097152);
                    traceBeginAndSlog("StartAudioService");
                    this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
                    Trace.traceEnd(2097152);
                    if ("1".equals(SystemProperties.get("ro.mtk_sensorhub_support"))) {
                    }
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartWiredAccessoryManager");
                    inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
                    Trace.traceEnd(2097152);
                    if (disableNonCoreServices) {
                    }
                    this.mSystemServiceManager.startService(TwilightService.class);
                    if (NightDisplayController.isAvailable(context)) {
                    }
                    this.mSystemServiceManager.startService(JobSchedulerService.class);
                    this.mSystemServiceManager.startService(SoundTriggerService.class);
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartDiskStatsService");
                    ServiceManager.addService("diskstats", new DiskStatsService(context));
                    Trace.traceEnd(2097152);
                    Slog.i(TAG, "OPPO Usage Service");
                    oppoUsageService = new OppoUsageService(context);
                    ServiceManager.addService("usage", oppoUsageService);
                    usageService = oppoUsageService;
                    Slog.i(TAG, "Hypnus Service");
                    hypnusService = new HypnusService(context);
                    ServiceManager.addService("hypnus", hypnusService);
                    hypnusService2 = hypnusService;
                    Slog.i(TAG, "Oppo Service");
                    oppoService2 = new OppoService(context);
                    ServiceManager.addService("OPPO", oppoService2);
                    oppoService = oppoService2;
                    Slog.i(TAG, "+OppoMultimediaService");
                    oppoMultimediaService = new OppoMultimediaService(context);
                    ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                    Slog.i(TAG, "-OppoMultimediaService");
                    multimediaService = oppoMultimediaService;
                    Slog.i(TAG, "Oppo Customize Service");
                    ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                    if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                    }
                    traceBeginAndSlog("CabcService");
                    Slog.i(TAG, "Cabc Service");
                    cabcService = new CabcService(context);
                    ServiceManager.addService("cabc", cabcService);
                    cabcService2 = cabcService;
                    if (disableSamplingProfiler) {
                    }
                    traceBeginAndSlog("StartNetworkTimeUpdateService");
                    networkTimeUpdateService = new NetworkTimeUpdateService(context);
                    ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                    networkTimeUpdater = networkTimeUpdateService;
                    Trace.traceEnd(2097152);
                    traceBeginAndSlog("StartCommonTimeManagementService");
                    commonTimeManagementService = new CommonTimeManagementService(context);
                    ServiceManager.addService("commontime_management", commonTimeManagementService);
                    commonTimeMgmtService = commonTimeManagementService;
                    Trace.traceEnd(2097152);
                    if (disableNetwork) {
                    }
                    this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartAssetAtlasService");
                    assetAtlasService = new AssetAtlasService(context);
                    ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService);
                    atlas = assetAtlasService;
                    Trace.traceEnd(2097152);
                    if (disableNonCoreServices) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                    }
                    this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                    Slog.i(TAG, "Media Session Service");
                    this.mSystemServiceManager.startService(MediaSessionService.class);
                    if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                    }
                    if (disableNonCoreServices) {
                    }
                    Slog.i(TAG, "Oppo Engineer Service");
                    this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                    if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                    }
                    Slog.i(TAG, "HDMI Manager Service");
                    mtkHdmiManagerService = new MtkHdmiManagerService(context);
                    ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                    mtkHdmiManagerService2 = mtkHdmiManagerService;
                    if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                    }
                    if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                    }
                    if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                    }
                    Slog.i(TAG, "Secrecy Service");
                    this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                    this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                    }
                    VMRuntime.getRuntime().startJitCompilation();
                    mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                    this.mSystemServiceManager.startService(RetailDemoModeService.class);
                    Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                    vibrator.systemReady();
                    Trace.traceEnd(2097152);
                    Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                    if (lockSettings != null) {
                    }
                    Trace.traceEnd(2097152);
                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                    this.mSystemServiceManager.startBootPhase(500);
                    Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                    Slog.i(TAG, "wms systemReady");
                    wm.systemReady();
                    Trace.traceEnd(2097152);
                    config = wm.computeNewConfiguration();
                    metrics = new DisplayMetrics();
                    ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                    context.getResources().updateConfiguration(config, metrics);
                    systemTheme = context.getTheme();
                    if (systemTheme.getChangingConfigurations() != 0) {
                    }
                    Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                    Slog.i(TAG, "power manager systemReady");
                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                    Trace.traceEnd(2097152);
                    Trace.traceEnd(2097152);
                    Slog.i(TAG, "Fingerprint systemReady");
                    this.mFingerprintService.systemReady();
                    if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                    }
                    Slog.i(TAG, "Secrecy systemReady");
                    this.mSecrecyService.systemReady();
                    Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                    Slog.i(TAG, "Package systemReady");
                    this.mPackageManagerService.systemReady();
                    Trace.traceEnd(2097152);
                    Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                    Slog.i(TAG, "DisplayManager systemReady");
                    this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                    Trace.traceEnd(2097152);
                    networkManagementF = networkManagement;
                    networkStatsF = networkStats;
                    networkPolicyF = networkPolicy;
                    connectivityF = connectivity;
                    networkScoreF = networkScore;
                    locationF = location;
                    countryDetectorF = countryDetector;
                    networkTimeUpdaterF = networkTimeUpdater;
                    commonTimeMgmtServiceF = commonTimeMgmtService;
                    atlasF = atlas;
                    inputManagerF = inputManager;
                    telephonyRegistryF = telephonyRegistry;
                    mediaRouterF = mediaRouter;
                    mmsServiceF = mmsService;
                    multimediaServiceF = multimediaService;
                    perfServiceF = perfServiceMgr;
                    runningboosterF = runningbooster;
                    usageServiceF = usageService;
                    Slog.i(TAG, "Ams systemReady");
                    mOppoService = oppoService;
                    this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                }
                Trace.traceEnd(2097152);
            }
            if (!(disableNonCoreServices || disableSearchManager)) {
                traceBeginAndSlog("StartSearchManagerService");
                this.mSystemServiceManager.startService(SEARCH_MANAGER_SERVICE_CLASS);
                Trace.traceEnd(2097152);
                Slog.i(TAG, "Search Engine Service");
                ServiceManager.addService("search_engine", new SearchEngineManagerService(context));
            }
            this.mSystemServiceManager.startService(DropBoxManagerService.class);
            instance = AgingCriticalEvent.getInstance();
            str = AgingCriticalEvent.EVENT_SYSTEM_BOOTUP;
            strArr = new String[1];
            strArr[0] = "systemserver pid:" + Process.myPid();
            instance.writeEvent(str, strArr);
            if (!disableNonCoreServices && context.getResources().getBoolean(17956942)) {
                traceBeginAndSlog("StartWallpaperManagerService");
                this.mSystemServiceManager.startService(WALLPAPER_SERVICE_CLASS);
                Trace.traceEnd(2097152);
            }
            traceBeginAndSlog("StartAudioService");
            this.mSystemServiceManager.startService(AudioService.Lifecycle.class);
            Trace.traceEnd(2097152);
            if ("1".equals(SystemProperties.get("ro.mtk_sensorhub_support"))) {
                try {
                    Slog.d(TAG, "SensorHubService");
                    ServiceManager.addService("sensorhubservice", new SensorHubService(context));
                } catch (Throwable e2222222222222222222) {
                    Slog.e(TAG, "starting SensorHub Service", e2222222222222222222);
                }
            }
            if (disableNonCoreServices) {
                Slog.i(TAG, "DockObserver");
                this.mSystemServiceManager.startService(DockObserver.class);
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                    this.mSystemServiceManager.startService(THERMAL_OBSERVER_CLASS);
                }
            }
            traceBeginAndSlog("StartWiredAccessoryManager");
            try {
                inputManager.setWiredAccessoryCallbacks(new WiredAccessoryManager(context, inputManager));
            } catch (Throwable e22222222222222222222) {
                reportWtf("starting WiredAccessoryManager", e22222222222222222222);
            }
            Trace.traceEnd(2097152);
            if (disableNonCoreServices) {
                if (this.mPackageManager.hasSystemFeature("android.software.midi")) {
                    this.mSystemServiceManager.startService(MIDI_SERVICE_CLASS);
                }
                if (this.mPackageManager.hasSystemFeature("android.hardware.usb.host") || this.mPackageManager.hasSystemFeature("android.hardware.usb.accessory")) {
                    Trace.traceBegin(2097152, "StartUsbService");
                    this.mSystemServiceManager.startService(USB_SERVICE_CLASS);
                    Trace.traceEnd(2097152);
                }
                if (!disableSerial) {
                    traceBeginAndSlog("StartSerialService");
                    try {
                        SerialService serialService = new SerialService(context);
                        SerialService serialService2;
                        try {
                            ServiceManager.addService("serial", serialService);
                            serialService2 = serialService;
                        } catch (Throwable th25) {
                            e22222222222222222222 = th25;
                            serialService2 = serialService;
                            Slog.e(TAG, "Failure starting SerialService", e22222222222222222222);
                            Trace.traceEnd(2097152);
                            Trace.traceBegin(2097152, "StartHardwarePropertiesManagerService");
                            hardwarePropertiesManagerService = new HardwarePropertiesManagerService(context);
                            try {
                                ServiceManager.addService("hardware_properties", hardwarePropertiesManagerService);
                                hardwarePropertiesManagerService2 = hardwarePropertiesManagerService;
                            } catch (Throwable th26) {
                                e22222222222222222222 = th26;
                                hardwarePropertiesManagerService2 = hardwarePropertiesManagerService;
                                Slog.e(TAG, "Failure starting HardwarePropertiesManagerService", e22222222222222222222);
                                Trace.traceEnd(2097152);
                                this.mSystemServiceManager.startService(TwilightService.class);
                                if (NightDisplayController.isAvailable(context)) {
                                }
                                this.mSystemServiceManager.startService(JobSchedulerService.class);
                                this.mSystemServiceManager.startService(SoundTriggerService.class);
                                if (disableNonCoreServices) {
                                }
                                traceBeginAndSlog("StartDiskStatsService");
                                ServiceManager.addService("diskstats", new DiskStatsService(context));
                                Trace.traceEnd(2097152);
                                Slog.i(TAG, "OPPO Usage Service");
                                oppoUsageService = new OppoUsageService(context);
                                ServiceManager.addService("usage", oppoUsageService);
                                usageService = oppoUsageService;
                                Slog.i(TAG, "Hypnus Service");
                                hypnusService = new HypnusService(context);
                                ServiceManager.addService("hypnus", hypnusService);
                                hypnusService2 = hypnusService;
                                Slog.i(TAG, "Oppo Service");
                                oppoService2 = new OppoService(context);
                                ServiceManager.addService("OPPO", oppoService2);
                                oppoService = oppoService2;
                                Slog.i(TAG, "+OppoMultimediaService");
                                oppoMultimediaService = new OppoMultimediaService(context);
                                ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                                Slog.i(TAG, "-OppoMultimediaService");
                                multimediaService = oppoMultimediaService;
                                Slog.i(TAG, "Oppo Customize Service");
                                ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                                if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                                }
                                traceBeginAndSlog("CabcService");
                                Slog.i(TAG, "Cabc Service");
                                cabcService = new CabcService(context);
                                ServiceManager.addService("cabc", cabcService);
                                cabcService2 = cabcService;
                                if (disableSamplingProfiler) {
                                }
                                traceBeginAndSlog("StartNetworkTimeUpdateService");
                                networkTimeUpdateService = new NetworkTimeUpdateService(context);
                                ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                                networkTimeUpdater = networkTimeUpdateService;
                                Trace.traceEnd(2097152);
                                traceBeginAndSlog("StartCommonTimeManagementService");
                                commonTimeManagementService = new CommonTimeManagementService(context);
                                ServiceManager.addService("commontime_management", commonTimeManagementService);
                                commonTimeMgmtService = commonTimeManagementService;
                                Trace.traceEnd(2097152);
                                if (disableNetwork) {
                                }
                                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                                if (disableNonCoreServices) {
                                }
                                traceBeginAndSlog("StartAssetAtlasService");
                                assetAtlasService = new AssetAtlasService(context);
                                ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService);
                                atlas = assetAtlasService;
                                Trace.traceEnd(2097152);
                                if (disableNonCoreServices) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                                }
                                this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                                Slog.i(TAG, "Media Session Service");
                                this.mSystemServiceManager.startService(MediaSessionService.class);
                                if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                                }
                                if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                                }
                                if (disableNonCoreServices) {
                                }
                                Slog.i(TAG, "Oppo Engineer Service");
                                this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                                this.mSystemServiceManager.startService(LauncherAppsService.class);
                                if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                                }
                                Slog.i(TAG, "HDMI Manager Service");
                                mtkHdmiManagerService = new MtkHdmiManagerService(context);
                                ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                                mtkHdmiManagerService2 = mtkHdmiManagerService;
                                if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                                }
                                if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                                }
                                if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                                }
                                Slog.i(TAG, "Secrecy Service");
                                this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                                this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                                if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                                }
                                VMRuntime.getRuntime().startJitCompilation();
                                mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                                this.mSystemServiceManager.startService(RetailDemoModeService.class);
                                Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                                vibrator.systemReady();
                                Trace.traceEnd(2097152);
                                Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                                if (lockSettings != null) {
                                }
                                Trace.traceEnd(2097152);
                                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                                this.mSystemServiceManager.startBootPhase(500);
                                Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                                Slog.i(TAG, "wms systemReady");
                                wm.systemReady();
                                Trace.traceEnd(2097152);
                                config = wm.computeNewConfiguration();
                                metrics = new DisplayMetrics();
                                ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                                context.getResources().updateConfiguration(config, metrics);
                                systemTheme = context.getTheme();
                                if (systemTheme.getChangingConfigurations() != 0) {
                                }
                                Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                                Slog.i(TAG, "power manager systemReady");
                                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                                Trace.traceEnd(2097152);
                                Trace.traceEnd(2097152);
                                Slog.i(TAG, "Fingerprint systemReady");
                                this.mFingerprintService.systemReady();
                                if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                                }
                                Slog.i(TAG, "Secrecy systemReady");
                                this.mSecrecyService.systemReady();
                                Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                                Slog.i(TAG, "Package systemReady");
                                this.mPackageManagerService.systemReady();
                                Trace.traceEnd(2097152);
                                Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                                Slog.i(TAG, "DisplayManager systemReady");
                                this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                                Trace.traceEnd(2097152);
                                networkManagementF = networkManagement;
                                networkStatsF = networkStats;
                                networkPolicyF = networkPolicy;
                                connectivityF = connectivity;
                                networkScoreF = networkScore;
                                locationF = location;
                                countryDetectorF = countryDetector;
                                networkTimeUpdaterF = networkTimeUpdater;
                                commonTimeMgmtServiceF = commonTimeMgmtService;
                                atlasF = atlas;
                                inputManagerF = inputManager;
                                telephonyRegistryF = telephonyRegistry;
                                mediaRouterF = mediaRouter;
                                mmsServiceF = mmsService;
                                multimediaServiceF = multimediaService;
                                perfServiceF = perfServiceMgr;
                                runningboosterF = runningbooster;
                                usageServiceF = usageService;
                                Slog.i(TAG, "Ams systemReady");
                                mOppoService = oppoService;
                                this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                            }
                            Trace.traceEnd(2097152);
                            this.mSystemServiceManager.startService(TwilightService.class);
                            if (NightDisplayController.isAvailable(context)) {
                            }
                            this.mSystemServiceManager.startService(JobSchedulerService.class);
                            this.mSystemServiceManager.startService(SoundTriggerService.class);
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartDiskStatsService");
                            ServiceManager.addService("diskstats", new DiskStatsService(context));
                            Trace.traceEnd(2097152);
                            Slog.i(TAG, "OPPO Usage Service");
                            oppoUsageService = new OppoUsageService(context);
                            ServiceManager.addService("usage", oppoUsageService);
                            usageService = oppoUsageService;
                            Slog.i(TAG, "Hypnus Service");
                            hypnusService = new HypnusService(context);
                            ServiceManager.addService("hypnus", hypnusService);
                            hypnusService2 = hypnusService;
                            Slog.i(TAG, "Oppo Service");
                            oppoService2 = new OppoService(context);
                            ServiceManager.addService("OPPO", oppoService2);
                            oppoService = oppoService2;
                            Slog.i(TAG, "+OppoMultimediaService");
                            oppoMultimediaService = new OppoMultimediaService(context);
                            ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                            Slog.i(TAG, "-OppoMultimediaService");
                            multimediaService = oppoMultimediaService;
                            Slog.i(TAG, "Oppo Customize Service");
                            ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                            if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                            }
                            traceBeginAndSlog("CabcService");
                            Slog.i(TAG, "Cabc Service");
                            cabcService = new CabcService(context);
                            ServiceManager.addService("cabc", cabcService);
                            cabcService2 = cabcService;
                            if (disableSamplingProfiler) {
                            }
                            traceBeginAndSlog("StartNetworkTimeUpdateService");
                            networkTimeUpdateService = new NetworkTimeUpdateService(context);
                            ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                            networkTimeUpdater = networkTimeUpdateService;
                            Trace.traceEnd(2097152);
                            traceBeginAndSlog("StartCommonTimeManagementService");
                            commonTimeManagementService = new CommonTimeManagementService(context);
                            ServiceManager.addService("commontime_management", commonTimeManagementService);
                            commonTimeMgmtService = commonTimeManagementService;
                            Trace.traceEnd(2097152);
                            if (disableNetwork) {
                            }
                            this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                            if (disableNonCoreServices) {
                            }
                            traceBeginAndSlog("StartAssetAtlasService");
                            assetAtlasService = new AssetAtlasService(context);
                            ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService);
                            atlas = assetAtlasService;
                            Trace.traceEnd(2097152);
                            if (disableNonCoreServices) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                            }
                            this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                            Slog.i(TAG, "Media Session Service");
                            this.mSystemServiceManager.startService(MediaSessionService.class);
                            if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                            }
                            if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                            }
                            if (disableNonCoreServices) {
                            }
                            Slog.i(TAG, "Oppo Engineer Service");
                            this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                            this.mSystemServiceManager.startService(LauncherAppsService.class);
                            if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                            }
                            Slog.i(TAG, "HDMI Manager Service");
                            mtkHdmiManagerService = new MtkHdmiManagerService(context);
                            ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                            mtkHdmiManagerService2 = mtkHdmiManagerService;
                            if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                            }
                            if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                            }
                            if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                            }
                            Slog.i(TAG, "Secrecy Service");
                            this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                            if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                            }
                            VMRuntime.getRuntime().startJitCompilation();
                            mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                            this.mSystemServiceManager.startService(RetailDemoModeService.class);
                            Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                            vibrator.systemReady();
                            Trace.traceEnd(2097152);
                            Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                            if (lockSettings != null) {
                            }
                            Trace.traceEnd(2097152);
                            this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                            this.mSystemServiceManager.startBootPhase(500);
                            Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                            Slog.i(TAG, "wms systemReady");
                            wm.systemReady();
                            Trace.traceEnd(2097152);
                            config = wm.computeNewConfiguration();
                            metrics = new DisplayMetrics();
                            ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                            context.getResources().updateConfiguration(config, metrics);
                            systemTheme = context.getTheme();
                            if (systemTheme.getChangingConfigurations() != 0) {
                            }
                            Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                            Slog.i(TAG, "power manager systemReady");
                            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                            Trace.traceEnd(2097152);
                            Trace.traceEnd(2097152);
                            Slog.i(TAG, "Fingerprint systemReady");
                            this.mFingerprintService.systemReady();
                            if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                            }
                            Slog.i(TAG, "Secrecy systemReady");
                            this.mSecrecyService.systemReady();
                            Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                            Slog.i(TAG, "Package systemReady");
                            this.mPackageManagerService.systemReady();
                            Trace.traceEnd(2097152);
                            Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                            Slog.i(TAG, "DisplayManager systemReady");
                            this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                            Trace.traceEnd(2097152);
                            networkManagementF = networkManagement;
                            networkStatsF = networkStats;
                            networkPolicyF = networkPolicy;
                            connectivityF = connectivity;
                            networkScoreF = networkScore;
                            locationF = location;
                            countryDetectorF = countryDetector;
                            networkTimeUpdaterF = networkTimeUpdater;
                            commonTimeMgmtServiceF = commonTimeMgmtService;
                            atlasF = atlas;
                            inputManagerF = inputManager;
                            telephonyRegistryF = telephonyRegistry;
                            mediaRouterF = mediaRouter;
                            mmsServiceF = mmsService;
                            multimediaServiceF = multimediaService;
                            perfServiceF = perfServiceMgr;
                            runningboosterF = runningbooster;
                            usageServiceF = usageService;
                            Slog.i(TAG, "Ams systemReady");
                            mOppoService = oppoService;
                            this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                        }
                    } catch (Throwable th27) {
                        e22222222222222222222 = th27;
                        Slog.e(TAG, "Failure starting SerialService", e22222222222222222222);
                        Trace.traceEnd(2097152);
                        Trace.traceBegin(2097152, "StartHardwarePropertiesManagerService");
                        hardwarePropertiesManagerService = new HardwarePropertiesManagerService(context);
                        ServiceManager.addService("hardware_properties", hardwarePropertiesManagerService);
                        hardwarePropertiesManagerService2 = hardwarePropertiesManagerService;
                        Trace.traceEnd(2097152);
                        this.mSystemServiceManager.startService(TwilightService.class);
                        if (NightDisplayController.isAvailable(context)) {
                        }
                        this.mSystemServiceManager.startService(JobSchedulerService.class);
                        this.mSystemServiceManager.startService(SoundTriggerService.class);
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartDiskStatsService");
                        ServiceManager.addService("diskstats", new DiskStatsService(context));
                        Trace.traceEnd(2097152);
                        Slog.i(TAG, "OPPO Usage Service");
                        oppoUsageService = new OppoUsageService(context);
                        ServiceManager.addService("usage", oppoUsageService);
                        usageService = oppoUsageService;
                        Slog.i(TAG, "Hypnus Service");
                        hypnusService = new HypnusService(context);
                        ServiceManager.addService("hypnus", hypnusService);
                        hypnusService2 = hypnusService;
                        Slog.i(TAG, "Oppo Service");
                        oppoService2 = new OppoService(context);
                        ServiceManager.addService("OPPO", oppoService2);
                        oppoService = oppoService2;
                        Slog.i(TAG, "+OppoMultimediaService");
                        oppoMultimediaService = new OppoMultimediaService(context);
                        ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                        Slog.i(TAG, "-OppoMultimediaService");
                        multimediaService = oppoMultimediaService;
                        Slog.i(TAG, "Oppo Customize Service");
                        ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                        if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                        }
                        traceBeginAndSlog("CabcService");
                        Slog.i(TAG, "Cabc Service");
                        cabcService = new CabcService(context);
                        ServiceManager.addService("cabc", cabcService);
                        cabcService2 = cabcService;
                        if (disableSamplingProfiler) {
                        }
                        traceBeginAndSlog("StartNetworkTimeUpdateService");
                        networkTimeUpdateService = new NetworkTimeUpdateService(context);
                        ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                        networkTimeUpdater = networkTimeUpdateService;
                        Trace.traceEnd(2097152);
                        traceBeginAndSlog("StartCommonTimeManagementService");
                        commonTimeManagementService = new CommonTimeManagementService(context);
                        ServiceManager.addService("commontime_management", commonTimeManagementService);
                        commonTimeMgmtService = commonTimeManagementService;
                        Trace.traceEnd(2097152);
                        if (disableNetwork) {
                        }
                        this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                        if (disableNonCoreServices) {
                        }
                        traceBeginAndSlog("StartAssetAtlasService");
                        assetAtlasService = new AssetAtlasService(context);
                        ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService);
                        atlas = assetAtlasService;
                        Trace.traceEnd(2097152);
                        if (disableNonCoreServices) {
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                        }
                        this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                        Slog.i(TAG, "Media Session Service");
                        this.mSystemServiceManager.startService(MediaSessionService.class);
                        if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                        }
                        if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                        }
                        if (disableNonCoreServices) {
                        }
                        Slog.i(TAG, "Oppo Engineer Service");
                        this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                        this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                        this.mSystemServiceManager.startService(LauncherAppsService.class);
                        if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                        }
                        Slog.i(TAG, "HDMI Manager Service");
                        mtkHdmiManagerService = new MtkHdmiManagerService(context);
                        ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                        mtkHdmiManagerService2 = mtkHdmiManagerService;
                        if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                        }
                        if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                        }
                        if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                        }
                        Slog.i(TAG, "Secrecy Service");
                        this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                        this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                        if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                        }
                        VMRuntime.getRuntime().startJitCompilation();
                        mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                        this.mSystemServiceManager.startService(RetailDemoModeService.class);
                        Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                        vibrator.systemReady();
                        Trace.traceEnd(2097152);
                        Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                        if (lockSettings != null) {
                        }
                        Trace.traceEnd(2097152);
                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                        this.mSystemServiceManager.startBootPhase(500);
                        Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                        Slog.i(TAG, "wms systemReady");
                        wm.systemReady();
                        Trace.traceEnd(2097152);
                        config = wm.computeNewConfiguration();
                        metrics = new DisplayMetrics();
                        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                        context.getResources().updateConfiguration(config, metrics);
                        systemTheme = context.getTheme();
                        if (systemTheme.getChangingConfigurations() != 0) {
                        }
                        Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                        Slog.i(TAG, "power manager systemReady");
                        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                        Trace.traceEnd(2097152);
                        Trace.traceEnd(2097152);
                        Slog.i(TAG, "Fingerprint systemReady");
                        this.mFingerprintService.systemReady();
                        if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                        }
                        Slog.i(TAG, "Secrecy systemReady");
                        this.mSecrecyService.systemReady();
                        Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                        Slog.i(TAG, "Package systemReady");
                        this.mPackageManagerService.systemReady();
                        Trace.traceEnd(2097152);
                        Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                        Slog.i(TAG, "DisplayManager systemReady");
                        this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                        Trace.traceEnd(2097152);
                        networkManagementF = networkManagement;
                        networkStatsF = networkStats;
                        networkPolicyF = networkPolicy;
                        connectivityF = connectivity;
                        networkScoreF = networkScore;
                        locationF = location;
                        countryDetectorF = countryDetector;
                        networkTimeUpdaterF = networkTimeUpdater;
                        commonTimeMgmtServiceF = commonTimeMgmtService;
                        atlasF = atlas;
                        inputManagerF = inputManager;
                        telephonyRegistryF = telephonyRegistry;
                        mediaRouterF = mediaRouter;
                        mmsServiceF = mmsService;
                        multimediaServiceF = multimediaService;
                        perfServiceF = perfServiceMgr;
                        runningboosterF = runningbooster;
                        usageServiceF = usageService;
                        Slog.i(TAG, "Ams systemReady");
                        mOppoService = oppoService;
                        this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                    }
                    Trace.traceEnd(2097152);
                }
                Trace.traceBegin(2097152, "StartHardwarePropertiesManagerService");
                try {
                    hardwarePropertiesManagerService = new HardwarePropertiesManagerService(context);
                    ServiceManager.addService("hardware_properties", hardwarePropertiesManagerService);
                    hardwarePropertiesManagerService2 = hardwarePropertiesManagerService;
                } catch (Throwable th28) {
                    e22222222222222222222 = th28;
                    Slog.e(TAG, "Failure starting HardwarePropertiesManagerService", e22222222222222222222);
                    Trace.traceEnd(2097152);
                    this.mSystemServiceManager.startService(TwilightService.class);
                    if (NightDisplayController.isAvailable(context)) {
                    }
                    this.mSystemServiceManager.startService(JobSchedulerService.class);
                    this.mSystemServiceManager.startService(SoundTriggerService.class);
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartDiskStatsService");
                    ServiceManager.addService("diskstats", new DiskStatsService(context));
                    Trace.traceEnd(2097152);
                    Slog.i(TAG, "OPPO Usage Service");
                    oppoUsageService = new OppoUsageService(context);
                    ServiceManager.addService("usage", oppoUsageService);
                    usageService = oppoUsageService;
                    Slog.i(TAG, "Hypnus Service");
                    hypnusService = new HypnusService(context);
                    ServiceManager.addService("hypnus", hypnusService);
                    hypnusService2 = hypnusService;
                    Slog.i(TAG, "Oppo Service");
                    oppoService2 = new OppoService(context);
                    ServiceManager.addService("OPPO", oppoService2);
                    oppoService = oppoService2;
                    Slog.i(TAG, "+OppoMultimediaService");
                    oppoMultimediaService = new OppoMultimediaService(context);
                    ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                    Slog.i(TAG, "-OppoMultimediaService");
                    multimediaService = oppoMultimediaService;
                    Slog.i(TAG, "Oppo Customize Service");
                    ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                    if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                    }
                    traceBeginAndSlog("CabcService");
                    Slog.i(TAG, "Cabc Service");
                    cabcService = new CabcService(context);
                    ServiceManager.addService("cabc", cabcService);
                    cabcService2 = cabcService;
                    if (disableSamplingProfiler) {
                    }
                    traceBeginAndSlog("StartNetworkTimeUpdateService");
                    networkTimeUpdateService = new NetworkTimeUpdateService(context);
                    ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                    networkTimeUpdater = networkTimeUpdateService;
                    Trace.traceEnd(2097152);
                    traceBeginAndSlog("StartCommonTimeManagementService");
                    commonTimeManagementService = new CommonTimeManagementService(context);
                    ServiceManager.addService("commontime_management", commonTimeManagementService);
                    commonTimeMgmtService = commonTimeManagementService;
                    Trace.traceEnd(2097152);
                    if (disableNetwork) {
                    }
                    this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                    if (disableNonCoreServices) {
                    }
                    traceBeginAndSlog("StartAssetAtlasService");
                    assetAtlasService = new AssetAtlasService(context);
                    ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService);
                    atlas = assetAtlasService;
                    Trace.traceEnd(2097152);
                    if (disableNonCoreServices) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                    }
                    this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                    Slog.i(TAG, "Media Session Service");
                    this.mSystemServiceManager.startService(MediaSessionService.class);
                    if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                    }
                    if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                    }
                    if (disableNonCoreServices) {
                    }
                    Slog.i(TAG, "Oppo Engineer Service");
                    this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                    if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                    }
                    Slog.i(TAG, "HDMI Manager Service");
                    mtkHdmiManagerService = new MtkHdmiManagerService(context);
                    ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                    mtkHdmiManagerService2 = mtkHdmiManagerService;
                    if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                    }
                    if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                    }
                    if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                    }
                    Slog.i(TAG, "Secrecy Service");
                    this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                    this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                    }
                    VMRuntime.getRuntime().startJitCompilation();
                    mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                    this.mSystemServiceManager.startService(RetailDemoModeService.class);
                    Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                    vibrator.systemReady();
                    Trace.traceEnd(2097152);
                    Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                    if (lockSettings != null) {
                    }
                    Trace.traceEnd(2097152);
                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                    this.mSystemServiceManager.startBootPhase(500);
                    Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                    Slog.i(TAG, "wms systemReady");
                    wm.systemReady();
                    Trace.traceEnd(2097152);
                    config = wm.computeNewConfiguration();
                    metrics = new DisplayMetrics();
                    ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                    context.getResources().updateConfiguration(config, metrics);
                    systemTheme = context.getTheme();
                    if (systemTheme.getChangingConfigurations() != 0) {
                    }
                    Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                    Slog.i(TAG, "power manager systemReady");
                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                    Trace.traceEnd(2097152);
                    Trace.traceEnd(2097152);
                    Slog.i(TAG, "Fingerprint systemReady");
                    this.mFingerprintService.systemReady();
                    if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                    }
                    Slog.i(TAG, "Secrecy systemReady");
                    this.mSecrecyService.systemReady();
                    Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                    Slog.i(TAG, "Package systemReady");
                    this.mPackageManagerService.systemReady();
                    Trace.traceEnd(2097152);
                    Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                    Slog.i(TAG, "DisplayManager systemReady");
                    this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                    Trace.traceEnd(2097152);
                    networkManagementF = networkManagement;
                    networkStatsF = networkStats;
                    networkPolicyF = networkPolicy;
                    connectivityF = connectivity;
                    networkScoreF = networkScore;
                    locationF = location;
                    countryDetectorF = countryDetector;
                    networkTimeUpdaterF = networkTimeUpdater;
                    commonTimeMgmtServiceF = commonTimeMgmtService;
                    atlasF = atlas;
                    inputManagerF = inputManager;
                    telephonyRegistryF = telephonyRegistry;
                    mediaRouterF = mediaRouter;
                    mmsServiceF = mmsService;
                    multimediaServiceF = multimediaService;
                    perfServiceF = perfServiceMgr;
                    runningboosterF = runningbooster;
                    usageServiceF = usageService;
                    Slog.i(TAG, "Ams systemReady");
                    mOppoService = oppoService;
                    this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                }
                Trace.traceEnd(2097152);
            }
            this.mSystemServiceManager.startService(TwilightService.class);
            if (NightDisplayController.isAvailable(context)) {
                this.mSystemServiceManager.startService(NightDisplayService.class);
            }
            this.mSystemServiceManager.startService(JobSchedulerService.class);
            this.mSystemServiceManager.startService(SoundTriggerService.class);
            if (disableNonCoreServices) {
                if (this.mPackageManager.hasSystemFeature("android.software.backup")) {
                    this.mSystemServiceManager.startService(BACKUP_MANAGER_SERVICE_CLASS);
                }
                if (this.mPackageManager.hasSystemFeature("android.software.app_widgets") || context.getResources().getBoolean(17957040)) {
                    this.mSystemServiceManager.startService(APPWIDGET_SERVICE_CLASS);
                }
                if (this.mPackageManager.hasSystemFeature("android.software.voice_recognizers")) {
                    this.mSystemServiceManager.startService(VOICE_RECOGNITION_MANAGER_SERVICE_CLASS);
                }
                if (GestureLauncherService.isGestureLauncherEnabled(context.getResources())) {
                    Slog.i(TAG, "Gesture Launcher Service");
                    this.mSystemServiceManager.startService(GestureLauncherService.class);
                }
                this.mSystemServiceManager.startService(SensorNotificationService.class);
                this.mSystemServiceManager.startService(ContextHubSystemService.class);
            }
            traceBeginAndSlog("StartDiskStatsService");
            try {
                ServiceManager.addService("diskstats", new DiskStatsService(context));
            } catch (Throwable e222222222222222222222) {
                reportWtf("starting DiskStats Service", e222222222222222222222);
            }
            Trace.traceEnd(2097152);
            try {
                Slog.i(TAG, "OPPO Usage Service");
                oppoUsageService = new OppoUsageService(context);
                ServiceManager.addService("usage", oppoUsageService);
                usageService = oppoUsageService;
            } catch (Throwable th29) {
                e222222222222222222222 = th29;
                Slog.e(TAG, "Start OppoUsageService failed for:", e222222222222222222222);
                Slog.i(TAG, "Hypnus Service");
                hypnusService = new HypnusService(context);
                ServiceManager.addService("hypnus", hypnusService);
                hypnusService2 = hypnusService;
                Slog.i(TAG, "Oppo Service");
                oppoService2 = new OppoService(context);
                ServiceManager.addService("OPPO", oppoService2);
                oppoService = oppoService2;
                Slog.i(TAG, "+OppoMultimediaService");
                oppoMultimediaService = new OppoMultimediaService(context);
                ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                Slog.i(TAG, "-OppoMultimediaService");
                multimediaService = oppoMultimediaService;
                Slog.i(TAG, "Oppo Customize Service");
                ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                }
                traceBeginAndSlog("CabcService");
                Slog.i(TAG, "Cabc Service");
                cabcService = new CabcService(context);
                ServiceManager.addService("cabc", cabcService);
                cabcService2 = cabcService;
                if (disableSamplingProfiler) {
                }
                traceBeginAndSlog("StartNetworkTimeUpdateService");
                networkTimeUpdateService = new NetworkTimeUpdateService(context);
                ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                networkTimeUpdater = networkTimeUpdateService;
                Trace.traceEnd(2097152);
                traceBeginAndSlog("StartCommonTimeManagementService");
                commonTimeManagementService = new CommonTimeManagementService(context);
                ServiceManager.addService("commontime_management", commonTimeManagementService);
                commonTimeMgmtService = commonTimeManagementService;
                Trace.traceEnd(2097152);
                if (disableNetwork) {
                }
                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                if (disableNonCoreServices) {
                }
                traceBeginAndSlog("StartAssetAtlasService");
                assetAtlasService = new AssetAtlasService(context);
                ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService);
                atlas = assetAtlasService;
                Trace.traceEnd(2097152);
                if (disableNonCoreServices) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                }
                this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                Slog.i(TAG, "Media Session Service");
                this.mSystemServiceManager.startService(MediaSessionService.class);
                if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                }
                if (disableNonCoreServices) {
                }
                Slog.i(TAG, "Oppo Engineer Service");
                this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                this.mSystemServiceManager.startService(LauncherAppsService.class);
                if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                }
                Slog.i(TAG, "HDMI Manager Service");
                mtkHdmiManagerService = new MtkHdmiManagerService(context);
                ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                mtkHdmiManagerService2 = mtkHdmiManagerService;
                if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                }
                if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                }
                if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                }
                Slog.i(TAG, "Secrecy Service");
                this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                }
                VMRuntime.getRuntime().startJitCompilation();
                mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                this.mSystemServiceManager.startService(RetailDemoModeService.class);
                Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                vibrator.systemReady();
                Trace.traceEnd(2097152);
                Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                if (lockSettings != null) {
                }
                Trace.traceEnd(2097152);
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                this.mSystemServiceManager.startBootPhase(500);
                Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                Slog.i(TAG, "wms systemReady");
                wm.systemReady();
                Trace.traceEnd(2097152);
                config = wm.computeNewConfiguration();
                metrics = new DisplayMetrics();
                ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                context.getResources().updateConfiguration(config, metrics);
                systemTheme = context.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                }
                Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                Slog.i(TAG, "power manager systemReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                Trace.traceEnd(2097152);
                Trace.traceEnd(2097152);
                Slog.i(TAG, "Fingerprint systemReady");
                this.mFingerprintService.systemReady();
                if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                }
                Slog.i(TAG, "Secrecy systemReady");
                this.mSecrecyService.systemReady();
                Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                Slog.i(TAG, "Package systemReady");
                this.mPackageManagerService.systemReady();
                Trace.traceEnd(2097152);
                Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                Slog.i(TAG, "DisplayManager systemReady");
                this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                Trace.traceEnd(2097152);
                networkManagementF = networkManagement;
                networkStatsF = networkStats;
                networkPolicyF = networkPolicy;
                connectivityF = connectivity;
                networkScoreF = networkScore;
                locationF = location;
                countryDetectorF = countryDetector;
                networkTimeUpdaterF = networkTimeUpdater;
                commonTimeMgmtServiceF = commonTimeMgmtService;
                atlasF = atlas;
                inputManagerF = inputManager;
                telephonyRegistryF = telephonyRegistry;
                mediaRouterF = mediaRouter;
                mmsServiceF = mmsService;
                multimediaServiceF = multimediaService;
                perfServiceF = perfServiceMgr;
                runningboosterF = runningbooster;
                usageServiceF = usageService;
                Slog.i(TAG, "Ams systemReady");
                mOppoService = oppoService;
                this.mActivityManagerService.systemReady(/* anonymous class already generated */);
            }
            try {
                Slog.i(TAG, "Hypnus Service");
                hypnusService = new HypnusService(context);
                ServiceManager.addService("hypnus", hypnusService);
                hypnusService2 = hypnusService;
            } catch (Throwable th30) {
                e222222222222222222222 = th30;
                reportWtf("starting Hypnus Service", e222222222222222222222);
                Slog.i(TAG, "Oppo Service");
                oppoService2 = new OppoService(context);
                ServiceManager.addService("OPPO", oppoService2);
                oppoService = oppoService2;
                Slog.i(TAG, "+OppoMultimediaService");
                oppoMultimediaService = new OppoMultimediaService(context);
                ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                Slog.i(TAG, "-OppoMultimediaService");
                multimediaService = oppoMultimediaService;
                Slog.i(TAG, "Oppo Customize Service");
                ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                }
                traceBeginAndSlog("CabcService");
                Slog.i(TAG, "Cabc Service");
                cabcService = new CabcService(context);
                ServiceManager.addService("cabc", cabcService);
                cabcService2 = cabcService;
                if (disableSamplingProfiler) {
                }
                traceBeginAndSlog("StartNetworkTimeUpdateService");
                networkTimeUpdateService = new NetworkTimeUpdateService(context);
                ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                networkTimeUpdater = networkTimeUpdateService;
                Trace.traceEnd(2097152);
                traceBeginAndSlog("StartCommonTimeManagementService");
                commonTimeManagementService = new CommonTimeManagementService(context);
                ServiceManager.addService("commontime_management", commonTimeManagementService);
                commonTimeMgmtService = commonTimeManagementService;
                Trace.traceEnd(2097152);
                if (disableNetwork) {
                }
                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                if (disableNonCoreServices) {
                }
                traceBeginAndSlog("StartAssetAtlasService");
                assetAtlasService = new AssetAtlasService(context);
                ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService);
                atlas = assetAtlasService;
                Trace.traceEnd(2097152);
                if (disableNonCoreServices) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                }
                this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                Slog.i(TAG, "Media Session Service");
                this.mSystemServiceManager.startService(MediaSessionService.class);
                if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                }
                if (disableNonCoreServices) {
                }
                Slog.i(TAG, "Oppo Engineer Service");
                this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                this.mSystemServiceManager.startService(LauncherAppsService.class);
                if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                }
                Slog.i(TAG, "HDMI Manager Service");
                mtkHdmiManagerService = new MtkHdmiManagerService(context);
                ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                mtkHdmiManagerService2 = mtkHdmiManagerService;
                if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                }
                if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                }
                if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                }
                Slog.i(TAG, "Secrecy Service");
                this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                }
                VMRuntime.getRuntime().startJitCompilation();
                mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                this.mSystemServiceManager.startService(RetailDemoModeService.class);
                Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                vibrator.systemReady();
                Trace.traceEnd(2097152);
                Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                if (lockSettings != null) {
                }
                Trace.traceEnd(2097152);
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                this.mSystemServiceManager.startBootPhase(500);
                Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                Slog.i(TAG, "wms systemReady");
                wm.systemReady();
                Trace.traceEnd(2097152);
                config = wm.computeNewConfiguration();
                metrics = new DisplayMetrics();
                ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                context.getResources().updateConfiguration(config, metrics);
                systemTheme = context.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                }
                Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                Slog.i(TAG, "power manager systemReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                Trace.traceEnd(2097152);
                Trace.traceEnd(2097152);
                Slog.i(TAG, "Fingerprint systemReady");
                this.mFingerprintService.systemReady();
                if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                }
                Slog.i(TAG, "Secrecy systemReady");
                this.mSecrecyService.systemReady();
                Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                Slog.i(TAG, "Package systemReady");
                this.mPackageManagerService.systemReady();
                Trace.traceEnd(2097152);
                Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                Slog.i(TAG, "DisplayManager systemReady");
                this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                Trace.traceEnd(2097152);
                networkManagementF = networkManagement;
                networkStatsF = networkStats;
                networkPolicyF = networkPolicy;
                connectivityF = connectivity;
                networkScoreF = networkScore;
                locationF = location;
                countryDetectorF = countryDetector;
                networkTimeUpdaterF = networkTimeUpdater;
                commonTimeMgmtServiceF = commonTimeMgmtService;
                atlasF = atlas;
                inputManagerF = inputManager;
                telephonyRegistryF = telephonyRegistry;
                mediaRouterF = mediaRouter;
                mmsServiceF = mmsService;
                multimediaServiceF = multimediaService;
                perfServiceF = perfServiceMgr;
                runningboosterF = runningbooster;
                usageServiceF = usageService;
                Slog.i(TAG, "Ams systemReady");
                mOppoService = oppoService;
                this.mActivityManagerService.systemReady(/* anonymous class already generated */);
            }
            try {
                Slog.i(TAG, "Oppo Service");
                oppoService2 = new OppoService(context);
                ServiceManager.addService("OPPO", oppoService2);
                oppoService = oppoService2;
            } catch (Throwable th31) {
                e222222222222222222222 = th31;
                Slog.e(TAG, "Failure starting Oppo Service", e222222222222222222222);
                Slog.i(TAG, "+OppoMultimediaService");
                oppoMultimediaService = new OppoMultimediaService(context);
                ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                Slog.i(TAG, "-OppoMultimediaService");
                multimediaService = oppoMultimediaService;
                Slog.i(TAG, "Oppo Customize Service");
                ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                }
                traceBeginAndSlog("CabcService");
                Slog.i(TAG, "Cabc Service");
                cabcService = new CabcService(context);
                ServiceManager.addService("cabc", cabcService);
                cabcService2 = cabcService;
                if (disableSamplingProfiler) {
                }
                traceBeginAndSlog("StartNetworkTimeUpdateService");
                networkTimeUpdateService = new NetworkTimeUpdateService(context);
                ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                networkTimeUpdater = networkTimeUpdateService;
                Trace.traceEnd(2097152);
                traceBeginAndSlog("StartCommonTimeManagementService");
                commonTimeManagementService = new CommonTimeManagementService(context);
                ServiceManager.addService("commontime_management", commonTimeManagementService);
                commonTimeMgmtService = commonTimeManagementService;
                Trace.traceEnd(2097152);
                if (disableNetwork) {
                }
                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                if (disableNonCoreServices) {
                }
                traceBeginAndSlog("StartAssetAtlasService");
                assetAtlasService = new AssetAtlasService(context);
                ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService);
                atlas = assetAtlasService;
                Trace.traceEnd(2097152);
                if (disableNonCoreServices) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                }
                this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                Slog.i(TAG, "Media Session Service");
                this.mSystemServiceManager.startService(MediaSessionService.class);
                if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                }
                if (disableNonCoreServices) {
                }
                Slog.i(TAG, "Oppo Engineer Service");
                this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                this.mSystemServiceManager.startService(LauncherAppsService.class);
                if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                }
                Slog.i(TAG, "HDMI Manager Service");
                mtkHdmiManagerService = new MtkHdmiManagerService(context);
                ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                mtkHdmiManagerService2 = mtkHdmiManagerService;
                if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                }
                if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                }
                if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                }
                Slog.i(TAG, "Secrecy Service");
                this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                }
                VMRuntime.getRuntime().startJitCompilation();
                mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                this.mSystemServiceManager.startService(RetailDemoModeService.class);
                Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                vibrator.systemReady();
                Trace.traceEnd(2097152);
                Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                if (lockSettings != null) {
                }
                Trace.traceEnd(2097152);
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                this.mSystemServiceManager.startBootPhase(500);
                Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                Slog.i(TAG, "wms systemReady");
                wm.systemReady();
                Trace.traceEnd(2097152);
                config = wm.computeNewConfiguration();
                metrics = new DisplayMetrics();
                ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                context.getResources().updateConfiguration(config, metrics);
                systemTheme = context.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                }
                Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                Slog.i(TAG, "power manager systemReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                Trace.traceEnd(2097152);
                Trace.traceEnd(2097152);
                Slog.i(TAG, "Fingerprint systemReady");
                this.mFingerprintService.systemReady();
                if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                }
                Slog.i(TAG, "Secrecy systemReady");
                this.mSecrecyService.systemReady();
                Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                Slog.i(TAG, "Package systemReady");
                this.mPackageManagerService.systemReady();
                Trace.traceEnd(2097152);
                Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                Slog.i(TAG, "DisplayManager systemReady");
                this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                Trace.traceEnd(2097152);
                networkManagementF = networkManagement;
                networkStatsF = networkStats;
                networkPolicyF = networkPolicy;
                connectivityF = connectivity;
                networkScoreF = networkScore;
                locationF = location;
                countryDetectorF = countryDetector;
                networkTimeUpdaterF = networkTimeUpdater;
                commonTimeMgmtServiceF = commonTimeMgmtService;
                atlasF = atlas;
                inputManagerF = inputManager;
                telephonyRegistryF = telephonyRegistry;
                mediaRouterF = mediaRouter;
                mmsServiceF = mmsService;
                multimediaServiceF = multimediaService;
                perfServiceF = perfServiceMgr;
                runningboosterF = runningbooster;
                usageServiceF = usageService;
                Slog.i(TAG, "Ams systemReady");
                mOppoService = oppoService;
                this.mActivityManagerService.systemReady(/* anonymous class already generated */);
            }
            try {
                Slog.i(TAG, "+OppoMultimediaService");
                oppoMultimediaService = new OppoMultimediaService(context);
                ServiceManager.addService("multimediaDaemon", oppoMultimediaService);
                Slog.i(TAG, "-OppoMultimediaService");
                multimediaService = oppoMultimediaService;
            } catch (Throwable th32) {
                e222222222222222222222 = th32;
                reportWtf("starting OppoMultimediaService", e222222222222222222222);
                Slog.i(TAG, "Oppo Customize Service");
                ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
                if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                }
                traceBeginAndSlog("CabcService");
                Slog.i(TAG, "Cabc Service");
                cabcService = new CabcService(context);
                ServiceManager.addService("cabc", cabcService);
                cabcService2 = cabcService;
                if (disableSamplingProfiler) {
                }
                traceBeginAndSlog("StartNetworkTimeUpdateService");
                networkTimeUpdateService = new NetworkTimeUpdateService(context);
                ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                networkTimeUpdater = networkTimeUpdateService;
                Trace.traceEnd(2097152);
                traceBeginAndSlog("StartCommonTimeManagementService");
                commonTimeManagementService = new CommonTimeManagementService(context);
                ServiceManager.addService("commontime_management", commonTimeManagementService);
                commonTimeMgmtService = commonTimeManagementService;
                Trace.traceEnd(2097152);
                if (disableNetwork) {
                }
                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                if (disableNonCoreServices) {
                }
                traceBeginAndSlog("StartAssetAtlasService");
                assetAtlasService = new AssetAtlasService(context);
                ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService);
                atlas = assetAtlasService;
                Trace.traceEnd(2097152);
                if (disableNonCoreServices) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                }
                this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                Slog.i(TAG, "Media Session Service");
                this.mSystemServiceManager.startService(MediaSessionService.class);
                if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                }
                if (disableNonCoreServices) {
                }
                Slog.i(TAG, "Oppo Engineer Service");
                this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                this.mSystemServiceManager.startService(LauncherAppsService.class);
                if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                }
                Slog.i(TAG, "HDMI Manager Service");
                mtkHdmiManagerService = new MtkHdmiManagerService(context);
                ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                mtkHdmiManagerService2 = mtkHdmiManagerService;
                if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                }
                if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                }
                if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                }
                Slog.i(TAG, "Secrecy Service");
                this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                }
                VMRuntime.getRuntime().startJitCompilation();
                mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                this.mSystemServiceManager.startService(RetailDemoModeService.class);
                Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                vibrator.systemReady();
                Trace.traceEnd(2097152);
                Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                if (lockSettings != null) {
                }
                Trace.traceEnd(2097152);
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                this.mSystemServiceManager.startBootPhase(500);
                Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                Slog.i(TAG, "wms systemReady");
                wm.systemReady();
                Trace.traceEnd(2097152);
                config = wm.computeNewConfiguration();
                metrics = new DisplayMetrics();
                ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                context.getResources().updateConfiguration(config, metrics);
                systemTheme = context.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                }
                Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                Slog.i(TAG, "power manager systemReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                Trace.traceEnd(2097152);
                Trace.traceEnd(2097152);
                Slog.i(TAG, "Fingerprint systemReady");
                this.mFingerprintService.systemReady();
                if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                }
                Slog.i(TAG, "Secrecy systemReady");
                this.mSecrecyService.systemReady();
                Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                Slog.i(TAG, "Package systemReady");
                this.mPackageManagerService.systemReady();
                Trace.traceEnd(2097152);
                Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                Slog.i(TAG, "DisplayManager systemReady");
                this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                Trace.traceEnd(2097152);
                networkManagementF = networkManagement;
                networkStatsF = networkStats;
                networkPolicyF = networkPolicy;
                connectivityF = connectivity;
                networkScoreF = networkScore;
                locationF = location;
                countryDetectorF = countryDetector;
                networkTimeUpdaterF = networkTimeUpdater;
                commonTimeMgmtServiceF = commonTimeMgmtService;
                atlasF = atlas;
                inputManagerF = inputManager;
                telephonyRegistryF = telephonyRegistry;
                mediaRouterF = mediaRouter;
                mmsServiceF = mmsService;
                multimediaServiceF = multimediaService;
                perfServiceF = perfServiceMgr;
                runningboosterF = runningbooster;
                usageServiceF = usageService;
                Slog.i(TAG, "Ams systemReady");
                mOppoService = oppoService;
                this.mActivityManagerService.systemReady(/* anonymous class already generated */);
            }
            try {
                Slog.i(TAG, "Oppo Customize Service");
                ServiceManager.addService("oppocustomize", new OppoCustomizeService(context));
            } catch (Throwable e2222222222222222222222) {
                Slog.e(TAG, "Failure starting Oppo Customize Service", e2222222222222222222222);
            }
            if (this.mPackageManager.hasSystemFeature("oppo.chattylog.support")) {
                try {
                    Slog.i(TAG, "Chatty Service");
                    ChattyManagerService chattyManagerService = new ChattyManagerService(context);
                } catch (Throwable e22222222222222222222222) {
                    reportWtf("starting Chatty Service", e22222222222222222222222);
                }
            }
            traceBeginAndSlog("CabcService");
            try {
                Slog.i(TAG, "Cabc Service");
                cabcService = new CabcService(context);
                ServiceManager.addService("cabc", cabcService);
                cabcService2 = cabcService;
            } catch (Throwable th33) {
                e22222222222222222222222 = th33;
                reportWtf("starting Cabc Service", e22222222222222222222222);
                if (disableSamplingProfiler) {
                }
                traceBeginAndSlog("StartNetworkTimeUpdateService");
                networkTimeUpdateService = new NetworkTimeUpdateService(context);
                ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                networkTimeUpdater = networkTimeUpdateService;
                Trace.traceEnd(2097152);
                traceBeginAndSlog("StartCommonTimeManagementService");
                commonTimeManagementService = new CommonTimeManagementService(context);
                ServiceManager.addService("commontime_management", commonTimeManagementService);
                commonTimeMgmtService = commonTimeManagementService;
                Trace.traceEnd(2097152);
                if (disableNetwork) {
                }
                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                if (disableNonCoreServices) {
                }
                traceBeginAndSlog("StartAssetAtlasService");
                assetAtlasService = new AssetAtlasService(context);
                ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService);
                atlas = assetAtlasService;
                Trace.traceEnd(2097152);
                if (disableNonCoreServices) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                }
                this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                Slog.i(TAG, "Media Session Service");
                this.mSystemServiceManager.startService(MediaSessionService.class);
                if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                }
                if (disableNonCoreServices) {
                }
                Slog.i(TAG, "Oppo Engineer Service");
                this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                this.mSystemServiceManager.startService(LauncherAppsService.class);
                if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                }
                Slog.i(TAG, "HDMI Manager Service");
                mtkHdmiManagerService = new MtkHdmiManagerService(context);
                ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                mtkHdmiManagerService2 = mtkHdmiManagerService;
                if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                }
                if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                }
                if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                }
                Slog.i(TAG, "Secrecy Service");
                this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                }
                VMRuntime.getRuntime().startJitCompilation();
                mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                this.mSystemServiceManager.startService(RetailDemoModeService.class);
                Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                vibrator.systemReady();
                Trace.traceEnd(2097152);
                Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                if (lockSettings != null) {
                }
                Trace.traceEnd(2097152);
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                this.mSystemServiceManager.startBootPhase(500);
                Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                Slog.i(TAG, "wms systemReady");
                wm.systemReady();
                Trace.traceEnd(2097152);
                config = wm.computeNewConfiguration();
                metrics = new DisplayMetrics();
                ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                context.getResources().updateConfiguration(config, metrics);
                systemTheme = context.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                }
                Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                Slog.i(TAG, "power manager systemReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                Trace.traceEnd(2097152);
                Trace.traceEnd(2097152);
                Slog.i(TAG, "Fingerprint systemReady");
                this.mFingerprintService.systemReady();
                if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                }
                Slog.i(TAG, "Secrecy systemReady");
                this.mSecrecyService.systemReady();
                Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                Slog.i(TAG, "Package systemReady");
                this.mPackageManagerService.systemReady();
                Trace.traceEnd(2097152);
                Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                Slog.i(TAG, "DisplayManager systemReady");
                this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                Trace.traceEnd(2097152);
                networkManagementF = networkManagement;
                networkStatsF = networkStats;
                networkPolicyF = networkPolicy;
                connectivityF = connectivity;
                networkScoreF = networkScore;
                locationF = location;
                countryDetectorF = countryDetector;
                networkTimeUpdaterF = networkTimeUpdater;
                commonTimeMgmtServiceF = commonTimeMgmtService;
                atlasF = atlas;
                inputManagerF = inputManager;
                telephonyRegistryF = telephonyRegistry;
                mediaRouterF = mediaRouter;
                mmsServiceF = mmsService;
                multimediaServiceF = multimediaService;
                perfServiceF = perfServiceMgr;
                runningboosterF = runningbooster;
                usageServiceF = usageService;
                Slog.i(TAG, "Ams systemReady");
                mOppoService = oppoService;
                this.mActivityManagerService.systemReady(/* anonymous class already generated */);
            }
            if (disableSamplingProfiler) {
                traceBeginAndSlog("StartSamplingProfilerService");
                try {
                    ServiceManager.addService("samplingprofiler", new SamplingProfilerService(context));
                } catch (Throwable e222222222222222222222222) {
                    reportWtf("starting SamplingProfiler Service", e222222222222222222222222);
                }
                Trace.traceEnd(2097152);
            }
            if (!(disableNetwork || disableNetworkTime)) {
                traceBeginAndSlog("StartNetworkTimeUpdateService");
                networkTimeUpdateService = new NetworkTimeUpdateService(context);
                ServiceManager.addService("network_time_update_service", networkTimeUpdateService);
                networkTimeUpdater = networkTimeUpdateService;
                Trace.traceEnd(2097152);
            }
            traceBeginAndSlog("StartCommonTimeManagementService");
            try {
                commonTimeManagementService = new CommonTimeManagementService(context);
                ServiceManager.addService("commontime_management", commonTimeManagementService);
                commonTimeMgmtService = commonTimeManagementService;
            } catch (Throwable th34) {
                e222222222222222222222222 = th34;
                reportWtf("starting CommonTimeManagementService service", e222222222222222222222222);
                Trace.traceEnd(2097152);
                if (disableNetwork) {
                }
                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
                if (disableNonCoreServices) {
                }
                traceBeginAndSlog("StartAssetAtlasService");
                assetAtlasService = new AssetAtlasService(context);
                ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService);
                atlas = assetAtlasService;
                Trace.traceEnd(2097152);
                if (disableNonCoreServices) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                }
                this.mSystemServiceManager.startService(RestrictionsManagerService.class);
                Slog.i(TAG, "Media Session Service");
                this.mSystemServiceManager.startService(MediaSessionService.class);
                if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                }
                if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                }
                if (disableNonCoreServices) {
                }
                Slog.i(TAG, "Oppo Engineer Service");
                this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                this.mSystemServiceManager.startService(LauncherAppsService.class);
                if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                }
                Slog.i(TAG, "HDMI Manager Service");
                mtkHdmiManagerService = new MtkHdmiManagerService(context);
                ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                mtkHdmiManagerService2 = mtkHdmiManagerService;
                if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                }
                if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                }
                if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                }
                Slog.i(TAG, "Secrecy Service");
                this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                }
                VMRuntime.getRuntime().startJitCompilation();
                mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                this.mSystemServiceManager.startService(RetailDemoModeService.class);
                Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                vibrator.systemReady();
                Trace.traceEnd(2097152);
                Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                if (lockSettings != null) {
                }
                Trace.traceEnd(2097152);
                this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                this.mSystemServiceManager.startBootPhase(500);
                Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                Slog.i(TAG, "wms systemReady");
                wm.systemReady();
                Trace.traceEnd(2097152);
                config = wm.computeNewConfiguration();
                metrics = new DisplayMetrics();
                ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                context.getResources().updateConfiguration(config, metrics);
                systemTheme = context.getTheme();
                if (systemTheme.getChangingConfigurations() != 0) {
                }
                Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                Slog.i(TAG, "power manager systemReady");
                this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                Trace.traceEnd(2097152);
                Trace.traceEnd(2097152);
                Slog.i(TAG, "Fingerprint systemReady");
                this.mFingerprintService.systemReady();
                if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                }
                Slog.i(TAG, "Secrecy systemReady");
                this.mSecrecyService.systemReady();
                Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                Slog.i(TAG, "Package systemReady");
                this.mPackageManagerService.systemReady();
                Trace.traceEnd(2097152);
                Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                Slog.i(TAG, "DisplayManager systemReady");
                this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                Trace.traceEnd(2097152);
                networkManagementF = networkManagement;
                networkStatsF = networkStats;
                networkPolicyF = networkPolicy;
                connectivityF = connectivity;
                networkScoreF = networkScore;
                locationF = location;
                countryDetectorF = countryDetector;
                networkTimeUpdaterF = networkTimeUpdater;
                commonTimeMgmtServiceF = commonTimeMgmtService;
                atlasF = atlas;
                inputManagerF = inputManager;
                telephonyRegistryF = telephonyRegistry;
                mediaRouterF = mediaRouter;
                mmsServiceF = mmsService;
                multimediaServiceF = multimediaService;
                perfServiceF = perfServiceMgr;
                runningboosterF = runningbooster;
                usageServiceF = usageService;
                Slog.i(TAG, "Ams systemReady");
                mOppoService = oppoService;
                this.mActivityManagerService.systemReady(/* anonymous class already generated */);
            }
            Trace.traceEnd(2097152);
            if (disableNetwork) {
                traceBeginAndSlog("CertBlacklister");
                try {
                    CertBlacklister certBlacklister = new CertBlacklister(context);
                } catch (Throwable e2222222222222222222222222) {
                    reportWtf("starting CertBlacklister", e2222222222222222222222222);
                }
                Trace.traceEnd(2097152);
            }
            if (!(disableNetwork || disableNonCoreServices)) {
                this.mSystemServiceManager.startService(EmergencyAffordanceService.class);
            }
            if (disableNonCoreServices) {
                this.mSystemServiceManager.startService(DreamManagerService.class);
            }
            if (!(disableNonCoreServices || SystemProperties.getBoolean("ro.hwui.disable_asset_atlas", false))) {
                traceBeginAndSlog("StartAssetAtlasService");
                assetAtlasService = new AssetAtlasService(context);
                ServiceManager.addService(AssetAtlasService.ASSET_ATLAS_SERVICE, assetAtlasService);
                atlas = assetAtlasService;
                Trace.traceEnd(2097152);
            }
            if (disableNonCoreServices) {
                ServiceManager.addService(GraphicsStatsService.GRAPHICS_STATS_SERVICE, new GraphicsStatsService(context));
            }
            if (this.mPackageManager.hasSystemFeature("android.software.print")) {
                this.mSystemServiceManager.startService(PRINT_MANAGER_SERVICE_CLASS);
            }
            this.mSystemServiceManager.startService(RestrictionsManagerService.class);
            Slog.i(TAG, "Media Session Service");
            this.mSystemServiceManager.startService(MediaSessionService.class);
            if (this.mPackageManager.hasSystemFeature("android.hardware.hdmi.cec")) {
                this.mSystemServiceManager.startService(HdmiControlService.class);
            }
            if (this.mPackageManager.hasSystemFeature("android.software.live_tv")) {
                this.mSystemServiceManager.startService(TvInputManagerService.class);
            }
            if (this.mPackageManager.hasSystemFeature("android.software.picture_in_picture")) {
                this.mSystemServiceManager.startService(MediaResourceMonitorService.class);
            }
            if (this.mPackageManager.hasSystemFeature("android.software.leanback")) {
                this.mSystemServiceManager.startService(TvRemoteService.class);
            }
            if (disableNonCoreServices) {
                traceBeginAndSlog("StartMediaRouterService");
                try {
                    MediaRouterService mediaRouterService = new MediaRouterService(context);
                    try {
                        ServiceManager.addService("media_router", mediaRouterService);
                        mediaRouter = mediaRouterService;
                    } catch (Throwable th35) {
                        e2222222222222222222222222 = th35;
                        mediaRouter = mediaRouterService;
                        reportWtf("starting MediaRouterService", e2222222222222222222222222);
                        Trace.traceEnd(2097152);
                        if (!disableTrustManager) {
                        }
                        Slog.i(TAG, "Fingerprint Service");
                        this.mFingerprintService = (FingerprintService) this.mSystemServiceManager.startService(FingerprintService.class);
                        if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                        }
                        traceBeginAndSlog("StartBackgroundDexOptService");
                        BackgroundDexOptService.schedule(context);
                        Trace.traceEnd(2097152);
                        Slog.i(TAG, "Oppo Engineer Service");
                        this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                        this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                        this.mSystemServiceManager.startService(LauncherAppsService.class);
                        if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                        }
                        Slog.i(TAG, "HDMI Manager Service");
                        mtkHdmiManagerService = new MtkHdmiManagerService(context);
                        ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                        mtkHdmiManagerService2 = mtkHdmiManagerService;
                        if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                        }
                        if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                        }
                        if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                        }
                        Slog.i(TAG, "Secrecy Service");
                        this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                        this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                        if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                        }
                        VMRuntime.getRuntime().startJitCompilation();
                        mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                        this.mSystemServiceManager.startService(RetailDemoModeService.class);
                        Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                        vibrator.systemReady();
                        Trace.traceEnd(2097152);
                        Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                        if (lockSettings != null) {
                        }
                        Trace.traceEnd(2097152);
                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                        this.mSystemServiceManager.startBootPhase(500);
                        Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                        Slog.i(TAG, "wms systemReady");
                        wm.systemReady();
                        Trace.traceEnd(2097152);
                        config = wm.computeNewConfiguration();
                        metrics = new DisplayMetrics();
                        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                        context.getResources().updateConfiguration(config, metrics);
                        systemTheme = context.getTheme();
                        if (systemTheme.getChangingConfigurations() != 0) {
                        }
                        Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                        Slog.i(TAG, "power manager systemReady");
                        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                        Trace.traceEnd(2097152);
                        Trace.traceEnd(2097152);
                        Slog.i(TAG, "Fingerprint systemReady");
                        this.mFingerprintService.systemReady();
                        if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                        }
                        Slog.i(TAG, "Secrecy systemReady");
                        this.mSecrecyService.systemReady();
                        Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                        Slog.i(TAG, "Package systemReady");
                        this.mPackageManagerService.systemReady();
                        Trace.traceEnd(2097152);
                        Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                        Slog.i(TAG, "DisplayManager systemReady");
                        this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                        Trace.traceEnd(2097152);
                        networkManagementF = networkManagement;
                        networkStatsF = networkStats;
                        networkPolicyF = networkPolicy;
                        connectivityF = connectivity;
                        networkScoreF = networkScore;
                        locationF = location;
                        countryDetectorF = countryDetector;
                        networkTimeUpdaterF = networkTimeUpdater;
                        commonTimeMgmtServiceF = commonTimeMgmtService;
                        atlasF = atlas;
                        inputManagerF = inputManager;
                        telephonyRegistryF = telephonyRegistry;
                        mediaRouterF = mediaRouter;
                        mmsServiceF = mmsService;
                        multimediaServiceF = multimediaService;
                        perfServiceF = perfServiceMgr;
                        runningboosterF = runningbooster;
                        usageServiceF = usageService;
                        Slog.i(TAG, "Ams systemReady");
                        mOppoService = oppoService;
                        this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                    }
                } catch (Throwable th36) {
                    e2222222222222222222222222 = th36;
                    reportWtf("starting MediaRouterService", e2222222222222222222222222);
                    Trace.traceEnd(2097152);
                    if (disableTrustManager) {
                    }
                    Slog.i(TAG, "Fingerprint Service");
                    this.mFingerprintService = (FingerprintService) this.mSystemServiceManager.startService(FingerprintService.class);
                    if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                    }
                    traceBeginAndSlog("StartBackgroundDexOptService");
                    BackgroundDexOptService.schedule(context);
                    Trace.traceEnd(2097152);
                    Slog.i(TAG, "Oppo Engineer Service");
                    this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
                    this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
                    this.mSystemServiceManager.startService(LauncherAppsService.class);
                    if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                    }
                    Slog.i(TAG, "HDMI Manager Service");
                    mtkHdmiManagerService = new MtkHdmiManagerService(context);
                    ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                    mtkHdmiManagerService2 = mtkHdmiManagerService;
                    if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                    }
                    if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                    }
                    if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                    }
                    Slog.i(TAG, "Secrecy Service");
                    this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                    this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                    }
                    VMRuntime.getRuntime().startJitCompilation();
                    mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                    this.mSystemServiceManager.startService(RetailDemoModeService.class);
                    Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                    vibrator.systemReady();
                    Trace.traceEnd(2097152);
                    Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                    if (lockSettings != null) {
                    }
                    Trace.traceEnd(2097152);
                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                    this.mSystemServiceManager.startBootPhase(500);
                    Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                    Slog.i(TAG, "wms systemReady");
                    wm.systemReady();
                    Trace.traceEnd(2097152);
                    config = wm.computeNewConfiguration();
                    metrics = new DisplayMetrics();
                    ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                    context.getResources().updateConfiguration(config, metrics);
                    systemTheme = context.getTheme();
                    if (systemTheme.getChangingConfigurations() != 0) {
                    }
                    Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                    Slog.i(TAG, "power manager systemReady");
                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                    Trace.traceEnd(2097152);
                    Trace.traceEnd(2097152);
                    Slog.i(TAG, "Fingerprint systemReady");
                    this.mFingerprintService.systemReady();
                    if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                    }
                    Slog.i(TAG, "Secrecy systemReady");
                    this.mSecrecyService.systemReady();
                    Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                    Slog.i(TAG, "Package systemReady");
                    this.mPackageManagerService.systemReady();
                    Trace.traceEnd(2097152);
                    Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                    Slog.i(TAG, "DisplayManager systemReady");
                    this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                    Trace.traceEnd(2097152);
                    networkManagementF = networkManagement;
                    networkStatsF = networkStats;
                    networkPolicyF = networkPolicy;
                    connectivityF = connectivity;
                    networkScoreF = networkScore;
                    locationF = location;
                    countryDetectorF = countryDetector;
                    networkTimeUpdaterF = networkTimeUpdater;
                    commonTimeMgmtServiceF = commonTimeMgmtService;
                    atlasF = atlas;
                    inputManagerF = inputManager;
                    telephonyRegistryF = telephonyRegistry;
                    mediaRouterF = mediaRouter;
                    mmsServiceF = mmsService;
                    multimediaServiceF = multimediaService;
                    perfServiceF = perfServiceMgr;
                    runningboosterF = runningbooster;
                    usageServiceF = usageService;
                    Slog.i(TAG, "Ams systemReady");
                    mOppoService = oppoService;
                    this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                }
                Trace.traceEnd(2097152);
                if (disableTrustManager) {
                    this.mSystemServiceManager.startService(TrustManagerService.class);
                }
                if (this.mPackageManager.hasSystemFeature("android.hardware.fingerprint") && (this.mPackageManager.hasSystemFeature("oppo.front.press.fingerprint.sensor") || this.mPackageManager.hasSystemFeature("oppo.front.touch.fingerprint.sensor") || this.mPackageManager.hasSystemFeature("oppo.back.touch.fingerprint.sensor"))) {
                    Slog.i(TAG, "Fingerprint Service");
                    this.mFingerprintService = (FingerprintService) this.mSystemServiceManager.startService(FingerprintService.class);
                }
                if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                    Slog.i(TAG, "Face Service");
                    this.mFaceService = (FaceService) this.mSystemServiceManager.startService(FaceService.class);
                } else {
                    SystemProperties.set("ctl.stop", "faced");
                }
                traceBeginAndSlog("StartBackgroundDexOptService");
                try {
                    BackgroundDexOptService.schedule(context);
                } catch (Throwable e22222222222222222222222222) {
                    reportWtf("starting BackgroundDexOptService", e22222222222222222222222222);
                }
                Trace.traceEnd(2097152);
            }
            Slog.i(TAG, "Oppo Engineer Service");
            this.mOppoEngineerService = (OppoEngineerService) this.mSystemServiceManager.startService(OppoEngineerService.class);
            this.mSystemServiceManager.startService(ShortcutService.Lifecycle.class);
            this.mSystemServiceManager.startService(LauncherAppsService.class);
            if (SystemProperties.get("ro.mtk_perfservice_support").equals("1")) {
                try {
                    PerfServiceManager perfServiceManager = new PerfServiceManager(context);
                    try {
                        PerfServiceImpl perfServiceImpl = new PerfServiceImpl(context, perfServiceManager);
                        Slog.d("perfservice", "perfService=" + perfServiceImpl);
                        if (perfServiceImpl != null) {
                            ServiceManager.addService("mtk-perfservice", perfServiceImpl.asBinder());
                        }
                        perfServiceMgr = perfServiceManager;
                    } catch (Throwable th37) {
                        e22222222222222222222222222 = th37;
                        perfServiceMgr = perfServiceManager;
                        Slog.e(TAG, "perfservice Failure starting PerfService", e22222222222222222222222222);
                        Slog.i(TAG, "HDMI Manager Service");
                        mtkHdmiManagerService = new MtkHdmiManagerService(context);
                        ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                        mtkHdmiManagerService2 = mtkHdmiManagerService;
                        if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                        }
                        if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                        }
                        if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                        }
                        Slog.i(TAG, "Secrecy Service");
                        this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                        this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                        if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                        }
                        VMRuntime.getRuntime().startJitCompilation();
                        mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                        this.mSystemServiceManager.startService(RetailDemoModeService.class);
                        Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                        vibrator.systemReady();
                        Trace.traceEnd(2097152);
                        Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                        if (lockSettings != null) {
                        }
                        Trace.traceEnd(2097152);
                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                        this.mSystemServiceManager.startBootPhase(500);
                        Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                        Slog.i(TAG, "wms systemReady");
                        wm.systemReady();
                        Trace.traceEnd(2097152);
                        config = wm.computeNewConfiguration();
                        metrics = new DisplayMetrics();
                        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                        context.getResources().updateConfiguration(config, metrics);
                        systemTheme = context.getTheme();
                        if (systemTheme.getChangingConfigurations() != 0) {
                        }
                        Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                        Slog.i(TAG, "power manager systemReady");
                        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                        Trace.traceEnd(2097152);
                        Trace.traceEnd(2097152);
                        Slog.i(TAG, "Fingerprint systemReady");
                        this.mFingerprintService.systemReady();
                        if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                        }
                        Slog.i(TAG, "Secrecy systemReady");
                        this.mSecrecyService.systemReady();
                        Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                        Slog.i(TAG, "Package systemReady");
                        this.mPackageManagerService.systemReady();
                        Trace.traceEnd(2097152);
                        Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                        Slog.i(TAG, "DisplayManager systemReady");
                        this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                        Trace.traceEnd(2097152);
                        networkManagementF = networkManagement;
                        networkStatsF = networkStats;
                        networkPolicyF = networkPolicy;
                        connectivityF = connectivity;
                        networkScoreF = networkScore;
                        locationF = location;
                        countryDetectorF = countryDetector;
                        networkTimeUpdaterF = networkTimeUpdater;
                        commonTimeMgmtServiceF = commonTimeMgmtService;
                        atlasF = atlas;
                        inputManagerF = inputManager;
                        telephonyRegistryF = telephonyRegistry;
                        mediaRouterF = mediaRouter;
                        mmsServiceF = mmsService;
                        multimediaServiceF = multimediaService;
                        perfServiceF = perfServiceMgr;
                        runningboosterF = runningbooster;
                        usageServiceF = usageService;
                        Slog.i(TAG, "Ams systemReady");
                        mOppoService = oppoService;
                        this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                    }
                } catch (Throwable th38) {
                    e22222222222222222222222222 = th38;
                    Slog.e(TAG, "perfservice Failure starting PerfService", e22222222222222222222222222);
                    Slog.i(TAG, "HDMI Manager Service");
                    mtkHdmiManagerService = new MtkHdmiManagerService(context);
                    ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                    mtkHdmiManagerService2 = mtkHdmiManagerService;
                    if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                    }
                    if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                    }
                    if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                    }
                    Slog.i(TAG, "Secrecy Service");
                    this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                    this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                    }
                    VMRuntime.getRuntime().startJitCompilation();
                    mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                    this.mSystemServiceManager.startService(RetailDemoModeService.class);
                    Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                    vibrator.systemReady();
                    Trace.traceEnd(2097152);
                    Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                    if (lockSettings != null) {
                    }
                    Trace.traceEnd(2097152);
                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                    this.mSystemServiceManager.startBootPhase(500);
                    Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                    Slog.i(TAG, "wms systemReady");
                    wm.systemReady();
                    Trace.traceEnd(2097152);
                    config = wm.computeNewConfiguration();
                    metrics = new DisplayMetrics();
                    ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                    context.getResources().updateConfiguration(config, metrics);
                    systemTheme = context.getTheme();
                    if (systemTheme.getChangingConfigurations() != 0) {
                    }
                    Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                    Slog.i(TAG, "power manager systemReady");
                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                    Trace.traceEnd(2097152);
                    Trace.traceEnd(2097152);
                    Slog.i(TAG, "Fingerprint systemReady");
                    this.mFingerprintService.systemReady();
                    if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                    }
                    Slog.i(TAG, "Secrecy systemReady");
                    this.mSecrecyService.systemReady();
                    Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                    Slog.i(TAG, "Package systemReady");
                    this.mPackageManagerService.systemReady();
                    Trace.traceEnd(2097152);
                    Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                    Slog.i(TAG, "DisplayManager systemReady");
                    this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                    Trace.traceEnd(2097152);
                    networkManagementF = networkManagement;
                    networkStatsF = networkStats;
                    networkPolicyF = networkPolicy;
                    connectivityF = connectivity;
                    networkScoreF = networkScore;
                    locationF = location;
                    countryDetectorF = countryDetector;
                    networkTimeUpdaterF = networkTimeUpdater;
                    commonTimeMgmtServiceF = commonTimeMgmtService;
                    atlasF = atlas;
                    inputManagerF = inputManager;
                    telephonyRegistryF = telephonyRegistry;
                    mediaRouterF = mediaRouter;
                    mmsServiceF = mmsService;
                    multimediaServiceF = multimediaService;
                    perfServiceF = perfServiceMgr;
                    runningboosterF = runningbooster;
                    usageServiceF = usageService;
                    Slog.i(TAG, "Ams systemReady");
                    mOppoService = oppoService;
                    this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                }
            }
            if (!disableNonCoreServices && SystemProperties.get("ro.mtk_hdmi_support").equals("1")) {
                Slog.i(TAG, "HDMI Manager Service");
                mtkHdmiManagerService = new MtkHdmiManagerService(context);
                ServiceManager.addService("mtkhdmi", mtkHdmiManagerService.asBinder());
                mtkHdmiManagerService2 = mtkHdmiManagerService;
            }
            if ("no".equals(SystemProperties.get("ro.mtk_carrierexpress_pack", "no"))) {
                traceBeginAndSlog("StartUspService");
                try {
                    this.mSystemServiceManager.startService(USP_SERVICE_CLASS);
                } catch (Throwable e222222222222222222222222222) {
                    reportWtf("starting usp Service", e222222222222222222222222222);
                }
                Trace.traceEnd(2097152);
            }
            if ("1".equals(SystemProperties.get("ro.globalpq.support"))) {
                traceBeginAndSlog("StartAppDetectionService");
                try {
                    this.mSystemServiceManager.startService(APPDETECTION_SERVICE_CLASS);
                } catch (Throwable e2222222222222222222222222222) {
                    reportWtf("Starting AppDetectionService", e2222222222222222222222222222);
                }
                Trace.traceEnd(2097152);
            }
            if ("1".equals(SystemProperties.get("persist.runningbooster.support"))) {
                try {
                    ((SuppressionService) this.mSystemServiceManager.startService(SuppressionService.class)).setActivityManager(this.mActivityManagerService);
                    Slog.i(TAG, "RunningBoosterService");
                    RunningBoosterService runningBoosterService = new RunningBoosterService(context);
                    try {
                        ServiceManager.addService("running_booster", runningBoosterService.asBinder());
                        runningbooster = runningBoosterService;
                    } catch (Throwable th39) {
                        e2222222222222222222222222222 = th39;
                        runningbooster = runningBoosterService;
                        reportWtf("starting RunningBoosterService", e2222222222222222222222222222);
                        Slog.i(TAG, "Secrecy Service");
                        this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                        this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                        if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                        }
                        VMRuntime.getRuntime().startJitCompilation();
                        mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                        this.mSystemServiceManager.startService(RetailDemoModeService.class);
                        Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                        vibrator.systemReady();
                        Trace.traceEnd(2097152);
                        Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                        if (lockSettings != null) {
                        }
                        Trace.traceEnd(2097152);
                        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                        this.mSystemServiceManager.startBootPhase(500);
                        Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                        Slog.i(TAG, "wms systemReady");
                        wm.systemReady();
                        Trace.traceEnd(2097152);
                        config = wm.computeNewConfiguration();
                        metrics = new DisplayMetrics();
                        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                        context.getResources().updateConfiguration(config, metrics);
                        systemTheme = context.getTheme();
                        if (systemTheme.getChangingConfigurations() != 0) {
                        }
                        Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                        Slog.i(TAG, "power manager systemReady");
                        this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                        Trace.traceEnd(2097152);
                        Trace.traceEnd(2097152);
                        Slog.i(TAG, "Fingerprint systemReady");
                        this.mFingerprintService.systemReady();
                        if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                        }
                        Slog.i(TAG, "Secrecy systemReady");
                        this.mSecrecyService.systemReady();
                        Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                        Slog.i(TAG, "Package systemReady");
                        this.mPackageManagerService.systemReady();
                        Trace.traceEnd(2097152);
                        Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                        Slog.i(TAG, "DisplayManager systemReady");
                        this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                        Trace.traceEnd(2097152);
                        networkManagementF = networkManagement;
                        networkStatsF = networkStats;
                        networkPolicyF = networkPolicy;
                        connectivityF = connectivity;
                        networkScoreF = networkScore;
                        locationF = location;
                        countryDetectorF = countryDetector;
                        networkTimeUpdaterF = networkTimeUpdater;
                        commonTimeMgmtServiceF = commonTimeMgmtService;
                        atlasF = atlas;
                        inputManagerF = inputManager;
                        telephonyRegistryF = telephonyRegistry;
                        mediaRouterF = mediaRouter;
                        mmsServiceF = mmsService;
                        multimediaServiceF = multimediaService;
                        perfServiceF = perfServiceMgr;
                        runningboosterF = runningbooster;
                        usageServiceF = usageService;
                        Slog.i(TAG, "Ams systemReady");
                        mOppoService = oppoService;
                        this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                    }
                } catch (Throwable th40) {
                    e2222222222222222222222222222 = th40;
                    reportWtf("starting RunningBoosterService", e2222222222222222222222222222);
                    Slog.i(TAG, "Secrecy Service");
                    this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
                    this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
                    if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
                    }
                    VMRuntime.getRuntime().startJitCompilation();
                    mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
                    this.mSystemServiceManager.startService(RetailDemoModeService.class);
                    Trace.traceBegin(2097152, "MakeVibratorServiceReady");
                    vibrator.systemReady();
                    Trace.traceEnd(2097152);
                    Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
                    if (lockSettings != null) {
                    }
                    Trace.traceEnd(2097152);
                    this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
                    this.mSystemServiceManager.startBootPhase(500);
                    Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
                    Slog.i(TAG, "wms systemReady");
                    wm.systemReady();
                    Trace.traceEnd(2097152);
                    config = wm.computeNewConfiguration();
                    metrics = new DisplayMetrics();
                    ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
                    context.getResources().updateConfiguration(config, metrics);
                    systemTheme = context.getTheme();
                    if (systemTheme.getChangingConfigurations() != 0) {
                    }
                    Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
                    Slog.i(TAG, "power manager systemReady");
                    this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
                    Trace.traceEnd(2097152);
                    Trace.traceEnd(2097152);
                    Slog.i(TAG, "Fingerprint systemReady");
                    this.mFingerprintService.systemReady();
                    if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                    }
                    Slog.i(TAG, "Secrecy systemReady");
                    this.mSecrecyService.systemReady();
                    Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
                    Slog.i(TAG, "Package systemReady");
                    this.mPackageManagerService.systemReady();
                    Trace.traceEnd(2097152);
                    Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
                    Slog.i(TAG, "DisplayManager systemReady");
                    this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
                    Trace.traceEnd(2097152);
                    networkManagementF = networkManagement;
                    networkStatsF = networkStats;
                    networkPolicyF = networkPolicy;
                    connectivityF = connectivity;
                    networkScoreF = networkScore;
                    locationF = location;
                    countryDetectorF = countryDetector;
                    networkTimeUpdaterF = networkTimeUpdater;
                    commonTimeMgmtServiceF = commonTimeMgmtService;
                    atlasF = atlas;
                    inputManagerF = inputManager;
                    telephonyRegistryF = telephonyRegistry;
                    mediaRouterF = mediaRouter;
                    mmsServiceF = mmsService;
                    multimediaServiceF = multimediaService;
                    perfServiceF = perfServiceMgr;
                    runningboosterF = runningbooster;
                    usageServiceF = usageService;
                    Slog.i(TAG, "Ams systemReady");
                    mOppoService = oppoService;
                    this.mActivityManagerService.systemReady(/* anonymous class already generated */);
                }
            }
            Slog.i(TAG, "Secrecy Service");
            this.mSecrecyService = (SecrecyService) this.mSystemServiceManager.startService(SecrecyService.class);
        }
        if (!(disableNonCoreServices || disableMediaProjection)) {
            this.mSystemServiceManager.startService(MediaProjectionManagerService.class);
        }
        if (context.getPackageManager().hasSystemFeature("android.hardware.type.watch")) {
            this.mSystemServiceManager.startService(WEAR_BLUETOOTH_SERVICE_CLASS);
            this.mSystemServiceManager.startService(WEAR_WIFI_MEDIATOR_SERVICE_CLASS);
            if (!disableNonCoreServices) {
                this.mSystemServiceManager.startService(WEAR_TIME_SERVICE_CLASS);
            }
        }
        VMRuntime.getRuntime().startJitCompilation();
        mmsService = (MmsServiceBroker) this.mSystemServiceManager.startService(MmsServiceBroker.class);
        if (Global.getInt(this.mContentResolver, "device_provisioned", 0) == 0 || UserManager.isDeviceInDemoMode(this.mSystemContext)) {
            this.mSystemServiceManager.startService(RetailDemoModeService.class);
        }
        Trace.traceBegin(2097152, "MakeVibratorServiceReady");
        try {
            vibrator.systemReady();
        } catch (Throwable e22222222222222222222222222222) {
            reportWtf("making Vibrator Service ready", e22222222222222222222222222222);
        }
        Trace.traceEnd(2097152);
        Trace.traceBegin(2097152, "MakeLockSettingsServiceReady");
        if (lockSettings != null) {
            try {
                lockSettings.systemReady();
            } catch (Throwable e222222222222222222222222222222) {
                reportWtf("making Lock Settings Service ready", e222222222222222222222222222222);
            }
        }
        Trace.traceEnd(2097152);
        this.mSystemServiceManager.startBootPhase(SystemService.PHASE_LOCK_SETTINGS_READY);
        this.mSystemServiceManager.startBootPhase(500);
        Trace.traceBegin(2097152, "MakeWindowManagerServiceReady");
        try {
            Slog.i(TAG, "wms systemReady");
            wm.systemReady();
        } catch (Throwable e2222222222222222222222222222222) {
            reportWtf("making Window Manager Service ready", e2222222222222222222222222222222);
        }
        Trace.traceEnd(2097152);
        config = wm.computeNewConfiguration();
        metrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getMetrics(metrics);
        context.getResources().updateConfiguration(config, metrics);
        systemTheme = context.getTheme();
        if (systemTheme.getChangingConfigurations() != 0) {
            systemTheme.rebase();
        }
        Trace.traceBegin(2097152, "MakePowerManagerServiceReady");
        try {
            Slog.i(TAG, "power manager systemReady");
            this.mPowerManagerService.systemReady(this.mActivityManagerService.getAppOpsService());
            Trace.traceEnd(2097152);
        } catch (Throwable e22222222222222222222222222222222) {
            reportWtf("making Power Manager Service ready", e22222222222222222222222222222222);
        }
        Trace.traceEnd(2097152);
        try {
            if (this.mPackageManager.hasSystemFeature("android.hardware.fingerprint") && (this.mPackageManager.hasSystemFeature("oppo.front.press.fingerprint.sensor") || this.mPackageManager.hasSystemFeature("oppo.front.touch.fingerprint.sensor") || this.mPackageManager.hasSystemFeature("oppo.back.touch.fingerprint.sensor"))) {
                Slog.i(TAG, "Fingerprint systemReady");
                this.mFingerprintService.systemReady();
            }
        } catch (Throwable e222222222222222222222222222222222) {
            reportWtf("making Fingerprint Service ready", e222222222222222222222222222222222);
        }
        try {
            if (this.mPackageManager.hasSystemFeature(FaceService.FEATURE_FACE)) {
                Slog.i(TAG, "Face Service systemReady");
                this.mFaceService.systemReady();
            }
        } catch (Throwable e2222222222222222222222222222222222) {
            reportWtf("making Face Service ready", e2222222222222222222222222222222222);
        }
        try {
            Slog.i(TAG, "Secrecy systemReady");
            this.mSecrecyService.systemReady();
        } catch (Throwable e22222222222222222222222222222222222) {
            reportWtf("making secrecy Service ready", e22222222222222222222222222222222222);
        }
        Trace.traceBegin(2097152, "MakePackageManagerServiceReady");
        try {
            Slog.i(TAG, "Package systemReady");
            this.mPackageManagerService.systemReady();
        } catch (Throwable e222222222222222222222222222222222222) {
            reportWtf("making Package Manager Service ready", e222222222222222222222222222222222222);
        }
        Trace.traceEnd(2097152);
        Trace.traceBegin(2097152, "MakeDisplayManagerServiceReady");
        try {
            Slog.i(TAG, "DisplayManager systemReady");
            this.mDisplayManagerService.systemReady(false, this.mOnlyCore);
        } catch (Throwable e2222222222222222222222222222222222222) {
            reportWtf("making Display Manager Service ready", e2222222222222222222222222222222222222);
        }
        Trace.traceEnd(2097152);
        networkManagementF = networkManagement;
        networkStatsF = networkStats;
        networkPolicyF = networkPolicy;
        connectivityF = connectivity;
        networkScoreF = networkScore;
        locationF = location;
        countryDetectorF = countryDetector;
        networkTimeUpdaterF = networkTimeUpdater;
        commonTimeMgmtServiceF = commonTimeMgmtService;
        atlasF = atlas;
        inputManagerF = inputManager;
        telephonyRegistryF = telephonyRegistry;
        mediaRouterF = mediaRouter;
        mmsServiceF = mmsService;
        multimediaServiceF = multimediaService;
        perfServiceF = perfServiceMgr;
        runningboosterF = runningbooster;
        usageServiceF = usageService;
        Slog.i(TAG, "Ams systemReady");
        mOppoService = oppoService;
        this.mActivityManagerService.systemReady(/* anonymous class already generated */);
    }

    final void testSystemServer(Context context) {
        IntentFilter testFilter = new IntentFilter();
        BroadcastReceiver broadcastReceiver = null;
        if (SystemProperties.get("persist.sys.anr_sys_key").equals("1")) {
            testFilter.addAction("android.intent.action.BOOT_COMPLETED");
            broadcastReceiver = new BroadcastReceiver() {
                public void onReceive(final Context context, Intent intent) {
                    if (intent.getAction() == "android.intent.action.BOOT_COMPLETED") {
                        new Handler().post(new Runnable() {
                            public void run() {
                                Log.i("ANR_DEBUG", "=== Start BadService2 ===");
                                Intent intent = new Intent("com.android.badservicesysserver");
                                intent.setPackage("com.android.badservicesysserver");
                                ComponentName ret = context.startService(intent);
                                if (ret != null) {
                                    Log.i("ANR_DEBUG", "=== result to start BadService2 === Name: " + ret.toString());
                                } else {
                                    Log.i("ANR_DEBUG", "=== result to start BadService2 === Name: Null ");
                                }
                            }
                        });
                    }
                }
            };
        } else if (SystemProperties.get("persist.sys.test_system_service").equals("1")) {
            testFilter.addAction("mediatek.intent.action.TEST_SERVICE");
            broadcastReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals("mediatek.intent.action.TEST_SERVICE")) {
                        String serviceName = intent.getStringExtra("SERVICE_NAME");
                        if (serviceName != null) {
                            try {
                                SystemServer.this.mSystemServiceManager.startService(serviceName);
                            } catch (Throwable e) {
                                SystemServer.this.reportWtf("starting" + serviceName + " Service", e);
                            }
                        }
                    }
                }
            };
        }
        if (broadcastReceiver != null) {
            context.registerReceiver(broadcastReceiver, testFilter);
        }
    }

    static final void startSystemUi(Context context) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.android.systemui", "com.android.systemui.SystemUIService"));
        intent.addFlags(256);
        context.startServiceAsUser(intent, UserHandle.SYSTEM);
    }

    static final void startPhone(Context context) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.android.phone", "com.android.phone.StartPhoneService"));
        Slog.d(TAG, "Starting service: " + intent);
        context.startServiceAsUser(intent, UserHandle.OWNER);
    }

    private static void traceBeginAndSlog(String name) {
        Trace.traceBegin(2097152, name);
        Slog.i(TAG, name);
    }
}
