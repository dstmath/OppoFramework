package com.mediatek.server;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.INetworkPolicyManager;
import android.net.INetworkStatsService;
import android.os.IBinder;
import android.os.IInterface;
import android.os.INetworkManagementService;
import android.os.ServiceManager;
import android.os.StrictMode;
import android.os.SystemProperties;
import android.util.Slog;
import android.util.TimingsTraceLog;
import com.android.server.NetworkManagementService;
import com.android.server.SystemService;
import com.android.server.SystemServiceManager;
import com.android.server.net.NetworkPolicyManagerService;
import com.android.server.net.NetworkStatsService;
import com.mediatek.omadm.PalConstDefs;
import com.mediatek.search.SearchEngineManagerService;
import dalvik.system.PathClassLoader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;

public class MtkSystemServerImpl extends MtkSystemServer {
    private static TimingsTraceLog BOOT_TIMINGS_TRACE_LOG = null;
    private static final String FULLSCREEN_SWITCH_SERVICE_CLASS = "com.mediatek.fullscreenswitch.FullscreenSwitchService";
    private static final String HDMI_LOCAL_SERVICE_CLASS = "com.mediatek.hdmilocalservice.HdmiLocalService";
    private static final String MTK_ALARM_MANAGER_SERVICE_CLASS = "com.mediatek.server.MtkAlarmManagerService";
    private static final String MTK_FM_RADIO_SERVICE_CLASS = "com.mediatek.fmradioservice.FmRadioService";
    private static final String MTK_OMADM_SERVICE_CLASS = "com.mediatek.omadm.OmadmService";
    private static final String MTK_STORAGE_MANAGER_SERVICE_CLASS = "com.mediatek.server.MtkStorageManagerService$MtkStorageManagerServiceLifecycle";
    private static final String MTK_VOW_BRIDGE_SERVICE_CLASS = "com.mediatek.server.vow.VoiceWakeupBridgeService";
    private static final String POWER_HAL_SERVICE_CLASS = "com.mediatek.powerhalservice.PowerHalMgrService";
    private static final String SEARCH_ENGINE_SERVICE_CLASS = "com.mediatek.search.SearchEngineManagerService";
    private static final String TAG = "MtkSystemServerImpl";
    private boolean mMTPROF_disable = false;
    private Context mSystemContext;
    private SystemServiceManager mSystemServiceManager;

    public void setPrameters(TimingsTraceLog btt, SystemServiceManager ssm, Context context) {
        BOOT_TIMINGS_TRACE_LOG = btt;
        this.mSystemServiceManager = ssm;
        this.mSystemContext = context;
    }

    public void startMtkBootstrapServices() {
        Slog.i(TAG, "startMtkBootstrapServices");
    }

    public void startMtkCoreServices() {
        Slog.i(TAG, "startMtkCoreServices ");
    }

    /* JADX WARN: Type inference failed for: r9v7, types: [com.mediatek.search.SearchEngineManagerService, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void startMtkOtherServices() {
        Context context = this.mSystemContext;
        Slog.i(TAG, "startOtherMtkService ");
        boolean disableSearchManager = SystemProperties.getBoolean("config.disable_searchmanager", false);
        boolean disableNonCoreServices = SystemProperties.getBoolean("config.disable_noncore", false);
        boolean enableHdmiServices = !PalConstDefs.EMPTY_STRING.equals(SystemProperties.get("ro.vendor.mtk_tb_hdmi"));
        boolean enableOmadmServices = !PalConstDefs.EMPTY_STRING.equals(SystemProperties.get("persist.vendor.omadm_support"));
        if (!disableNonCoreServices && !disableSearchManager) {
            traceBeginAndSlog("StartSearchEngineManagerService");
            try {
                ServiceManager.addService("search_engine_service", (IBinder) new SearchEngineManagerService(context));
            } catch (Throwable e) {
                Slog.e(TAG, "StartSearchEngineManagerService " + e.toString());
            }
        }
        if (enableOmadmServices) {
            traceBeginAndSlog("StartOmadmService");
            try {
                startService(MTK_OMADM_SERVICE_CLASS);
            } catch (Throwable e2) {
                reportWtf("starting OmadmService", e2);
            }
            traceEnd();
        }
        try {
            if (Class.forName("com.mediatek.fmradio.FmRadioPackageManager") != null) {
                traceBeginAndSlog("addService FmRadioService");
                try {
                    startService(MTK_FM_RADIO_SERVICE_CLASS);
                } catch (Throwable e3) {
                    reportWtf("starting FmRadioService", e3);
                }
                traceEnd();
            }
        } catch (Exception e4) {
            Slog.e(TAG, "com.mediatek.fmradio.FmRadioPackageManager not found ");
        }
        if (enableHdmiServices) {
            traceBeginAndSlog("StartHdmiLocalService");
            try {
                startService(HDMI_LOCAL_SERVICE_CLASS);
            } catch (Throwable e5) {
                reportWtf("starting HdmiLocalService", e5);
            }
            traceEnd();
        }
        traceBeginAndSlog("StartPowerHalMgrService");
        try {
            startService(POWER_HAL_SERVICE_CLASS);
        } catch (Throwable e6) {
            reportWtf("starting PowerHalMgrService", e6);
        }
        traceEnd();
        if ("1".equals(SystemProperties.get("ro.vendor.fullscreen_switch"))) {
            traceBeginAndSlog("addService FullscreenSwitchService");
            try {
                ServiceManager.addService("FullscreenSwitchService", ((IInterface) Class.forName(FULLSCREEN_SWITCH_SERVICE_CLASS).getConstructor(Context.class).newInstance(context)).asBinder());
            } catch (Throwable e7) {
                reportWtf("starting FullscreenSwitchService", e7);
            }
            traceEnd();
        }
        PackageManager pm = this.mSystemContext.getPackageManager();
        if (pm != null && pm.hasSystemFeature("com.mediatek.hardware.vow.2e2k")) {
            traceBeginAndSlog("addService VoiceWakeupBridgeService");
            try {
                startService(MTK_VOW_BRIDGE_SERVICE_CLASS);
            } catch (Throwable e8) {
                reportWtf("starting VoiceWakeupBridgeService", e8);
            }
            traceEnd();
        }
    }

    public Object getMtkConnectivityService(NetworkManagementService networkManagement, NetworkStatsService networkStats, NetworkPolicyManagerService networkPolicy) {
        try {
            Constructor clazzConstructfunc = new PathClassLoader("/system/framework/mediatek-framework-net.jar", this.mSystemContext.getClassLoader()).loadClass("com.android.server.MtkConnectivityService").getConstructor(Context.class, INetworkManagementService.class, INetworkStatsService.class, INetworkPolicyManager.class);
            clazzConstructfunc.setAccessible(true);
            return clazzConstructfunc.newInstance(this.mSystemContext, networkManagement, networkStats, networkPolicy);
        } catch (Exception e) {
            Slog.e(TAG, "No MtkConnectivityService! Used AOSP for instead!", e);
            return null;
        }
    }

    public boolean startMtkAlarmManagerService() {
        traceBeginAndSlog("startMtkAlarmManagerService");
        try {
            startService(MTK_ALARM_MANAGER_SERVICE_CLASS);
            traceEnd();
            return true;
        } catch (Throwable e) {
            Slog.e(TAG, "Exception while starting MtkAlarmManagerService" + e.toString());
            return false;
        }
    }

    public boolean startMtkStorageManagerService() {
        if (!SystemProperties.get("ro.vendor.mtk_privacy_protection_lock").equals("1")) {
            Slog.i(TAG, "PPL not supported, retruning, will start AOSP StorageManagerService");
            return false;
        }
        traceBeginAndSlog("StartMtkStorageManagerService");
        try {
            startService(MTK_STORAGE_MANAGER_SERVICE_CLASS);
            traceEnd();
            return true;
        } catch (Throwable e) {
            Slog.e(TAG, "Exception while starting MtkStorageManagerService" + e.toString());
            return false;
        }
    }

    private SystemService startService(String className) {
        try {
            return this.mSystemServiceManager.startService(Class.forName(className));
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Failed to create service " + className + ": service class not found, usually indicates that the caller should have called PackageManager.hasSystemFeature() to check whether the feature is available on this device before trying to start the services that implement it", ex);
        }
    }

    private static void traceBeginAndSlog(String name) {
        Slog.i(TAG, name);
        BOOT_TIMINGS_TRACE_LOG.traceBegin(name);
    }

    private static void traceEnd() {
        BOOT_TIMINGS_TRACE_LOG.traceEnd();
    }

    private void reportWtf(String msg, Throwable e) {
        Slog.w(TAG, "***********************************************");
        Slog.wtf(TAG, "BOOT FAILURE " + msg, e);
    }

    public void addBootEvent(String bootevent) {
        if (!this.mMTPROF_disable) {
            StrictMode.ThreadPolicy oldPolicy = StrictMode.getThreadPolicy();
            if (bootevent.contains("AP_Init")) {
                StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder(oldPolicy).permitDiskWrites().build());
            }
            FileOutputStream fbp = null;
            try {
                fbp = new FileOutputStream("/proc/bootprof");
                fbp.write(bootevent.getBytes());
                fbp.flush();
                try {
                    fbp.close();
                } catch (IOException e) {
                    Slog.e("BOOTPROF", "Failure close /proc/bootprof entry", e);
                }
            } catch (FileNotFoundException e2) {
                Slog.e("BOOTPROF", "Failure open /proc/bootprof, not found!", e2);
                if (fbp != null) {
                    fbp.close();
                }
            } catch (IOException e3) {
                Slog.e("BOOTPROF", "Failure open /proc/bootprof entry", e3);
                if (fbp != null) {
                    fbp.close();
                }
            } catch (Throwable th) {
                if (fbp != null) {
                    try {
                        fbp.close();
                    } catch (IOException e4) {
                        Slog.e("BOOTPROF", "Failure close /proc/bootprof entry", e4);
                    }
                }
                throw th;
            }
            if (bootevent.contains("AP_Init")) {
                StrictMode.setThreadPolicy(oldPolicy);
            }
        }
    }
}
