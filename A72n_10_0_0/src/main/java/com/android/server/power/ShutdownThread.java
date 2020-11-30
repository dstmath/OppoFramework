package com.android.server.power;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.admin.SecurityLog;
import android.common.OppoFeatureCache;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.fingerprint.IFingerprintService;
import android.media.AudioAttributes;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IOppoUsageService;
import android.os.OppoManager;
import android.os.PowerManager;
import android.os.RecoverySystem;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.SystemVibrator;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.IStorageManager;
import android.os.storage.IStorageShutdownObserver;
import android.util.ArrayMap;
import android.util.Log;
import android.util.TimingsTraceLog;
import android.view.IWindowManager;
import com.android.internal.telephony.ITelephony;
import com.android.server.AgingCriticalEvent;
import com.android.server.LocalServices;
import com.android.server.RescueParty;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.Settings;
import com.android.server.statusbar.StatusBarManagerInternal;
import com.android.server.theia.NoFocusWindow;
import com.mediatek.server.MtkSystemServiceFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import libcore.io.IoUtils;

public class ShutdownThread extends Thread {
    private static final int ACTION_DONE_POLL_WAIT_MS = 500;
    private static final int ACTIVITY_MANAGER_STOP_PERCENT = 4;
    private static final int BROADCAST_STOP_PERCENT = 2;
    private static final int MAX_BROADCAST_TIME = 5000;
    private static final int MAX_RADIO_WAIT_TIME = 3000;
    private static final int MAX_SHUTDOWN_WAIT_TIME = 10000;
    private static final int MAX_UNCRYPT_WAIT_TIME = 900000;
    private static final String METRICS_FILE_BASENAME = "/data/system/shutdown-metrics";
    private static String METRIC_AM = "shutdown_activity_manager";
    private static String METRIC_PM = "shutdown_package_manager";
    private static String METRIC_RADIO = "shutdown_radio";
    private static String METRIC_RADIOS = "shutdown_radios";
    private static String METRIC_SEND_BROADCAST = "shutdown_send_shutdown_broadcast";
    private static String METRIC_SHUTDOWN_TIME_START = "begin_shutdown";
    private static String METRIC_SYSTEM_SERVER = "shutdown_system_server";
    private static final int MOUNT_SERVICE_STOP_PERCENT = 20;
    private static final int PACKAGE_MANAGER_STOP_PERCENT = 6;
    private static final int RADIOS_STATE_POLL_SLEEP_MS = 100;
    private static final int RADIO_STOP_PERCENT = 18;
    public static final String REBOOT_SAFEMODE_PROPERTY = "persist.sys.safemode";
    public static final String RO_SAFEMODE_PROPERTY = "ro.sys.safemode";
    public static final String SHUTDOWN_ACTION_PROPERTY = "sys.shutdown.requested";
    private static final int SHUTDOWN_VIBRATE_MS = 500;
    private static final String TAG = "ShutdownThread";
    private static final ArrayMap<String, Long> TRON_METRICS = new ArrayMap<>();
    private static final AudioAttributes VIBRATION_ATTRIBUTES = new AudioAttributes.Builder().setContentType(4).setUsage(13).build();
    protected static String mReason;
    protected static boolean mReboot;
    protected static boolean mRebootHasProgressBar;
    protected static boolean mRebootSafeMode;
    private static AlertDialog sConfirmDialog;
    protected static final ShutdownThread sInstance = MtkSystemServiceFactory.getInstance().makeMtkShutdownThread();
    private static boolean sIsStarted = false;
    private static final Object sIsStartedGuard = new Object();
    private boolean mActionDone;
    private final Object mActionDoneSync = new Object();
    protected Context mContext;
    private PowerManager.WakeLock mCpuWakeLock;
    protected Handler mHandler;
    protected PowerManager mPowerManager;
    private ProgressDialog mProgressDialog;
    protected PowerManager.WakeLock mScreenWakeLock;

    public static void shutdown(Context context, String reason, boolean confirm) {
        mReboot = false;
        mRebootSafeMode = false;
        mReason = reason;
        OppoFeatureCache.getOrCreate(IPswShutdownFeature.DEFAULT, new Object[]{context}).showShutdownBacktrace(true);
        OppoFeatureCache.getOrCreate(IPswShutdownFeature.DEFAULT, new Object[]{context}).resetBrightnessAdj(context);
        Log.v(TAG, "oppomanager sync cache to emmc");
        OppoManager.syncCacheToEmmc();
        shutdownInner(context, confirm);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0066, code lost:
        r0 = r6.getResources().getInteger(17694826);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0073, code lost:
        if (com.android.server.power.ShutdownThread.mRebootSafeMode == false) goto L_0x0079;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0075, code lost:
        r1 = 17040895;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0079, code lost:
        if (r0 != 2) goto L_0x007f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x007b, code lost:
        r1 = 17041022;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x007f, code lost:
        r1 = 17041021;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0082, code lost:
        android.util.Log.d(com.android.server.power.ShutdownThread.TAG, "Notifying thread to start shutdown longPressBehavior=" + r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0099, code lost:
        if (r7 == false) goto L_0x00f1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x009b, code lost:
        r2 = new com.android.server.power.ShutdownThread.CloseDialogReceiver(r6);
        r3 = com.android.server.power.ShutdownThread.sConfirmDialog;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00a2, code lost:
        if (r3 == null) goto L_0x00a7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00a4, code lost:
        r3.dismiss();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00a7, code lost:
        r3 = new android.app.AlertDialog.Builder(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00ae, code lost:
        if (com.android.server.power.ShutdownThread.mRebootSafeMode == false) goto L_0x00b4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00b0, code lost:
        r4 = 17040896;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x00b4, code lost:
        r4 = 17040878;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00b7, code lost:
        com.android.server.power.ShutdownThread.sConfirmDialog = r3.setTitle(r4).setMessage(r1).setPositiveButton(17039379, new com.android.server.power.ShutdownThread.AnonymousClass1()).setNegativeButton(17039369, (android.content.DialogInterface.OnClickListener) null).create();
        r3 = com.android.server.power.ShutdownThread.sConfirmDialog;
        r2.dialog = r3;
        r3.setOnDismissListener(r2);
        com.android.server.power.ShutdownThread.sConfirmDialog.getWindow().setType(2009);
        com.android.server.power.ShutdownThread.sConfirmDialog.show();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00f1, code lost:
        beginShutdownSequence(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:?, code lost:
        return;
     */
    private static void shutdownInner(final Context context, boolean confirm) {
        String str;
        context.assertRuntimeOverlayThemable();
        AgingCriticalEvent instance = AgingCriticalEvent.getInstance();
        String[] strArr = new String[2];
        if (mReboot) {
            str = "reboot, reason:" + mReason + " mRebootSafeMode:" + mRebootSafeMode;
        } else {
            str = "shutdown, reason:" + mReason;
        }
        strArr[0] = str;
        strArr[1] = confirm ? "confirmed" : "noNeedConfirm";
        instance.writeEvent(AgingCriticalEvent.EVENT_USER_POWER_OFF, strArr);
        synchronized (sIsStartedGuard) {
            if (sIsStarted) {
                Log.d(TAG, "Request to shutdown already running, returning.");
            }
        }
    }

    /* access modifiers changed from: private */
    public static class CloseDialogReceiver extends BroadcastReceiver implements DialogInterface.OnDismissListener {
        public Dialog dialog;
        private Context mContext;

        CloseDialogReceiver(Context context) {
            this.mContext = context;
            context.registerReceiver(this, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
        }

        public void onReceive(Context context, Intent intent) {
            this.dialog.cancel();
        }

        public void onDismiss(DialogInterface unused) {
            this.mContext.unregisterReceiver(this);
        }
    }

    public static void reboot(Context context, String reason, boolean confirm) {
        if ("sau".equals(reason) || "silence".equals(reason)) {
            SystemProperties.set("sys.bootsilence", NoFocusWindow.HUNG_CONFIG_ENABLE);
        }
        Log.d(TAG, "!!! Request to reboot !!!");
        Log.v(TAG, "oppomanager sync cache to emmc");
        OppoManager.syncCacheToEmmc();
        OppoFeatureCache.getOrCreate(IPswShutdownFeature.DEFAULT, new Object[]{context}).showShutdownBacktrace(true);
        mReboot = true;
        mRebootSafeMode = false;
        mRebootHasProgressBar = false;
        mReason = reason;
        OppoFeatureCache.getOrCreate(IPswShutdownFeature.DEFAULT, new Object[]{context}).resetBrightnessAdj(context);
        shutdownInner(context, confirm);
    }

    public static void rebootSafeMode(Context context, boolean confirm) {
        if (!((UserManager) context.getSystemService("user")).hasUserRestriction("no_safe_boot")) {
            OppoFeatureCache.getOrCreate(IPswShutdownFeature.DEFAULT, new Object[]{context}).resetBrightnessAdj(context);
            mReboot = true;
            mRebootSafeMode = true;
            mRebootHasProgressBar = false;
            mReason = null;
            shutdownInner(context, confirm);
        }
    }

    private static ProgressDialog showShutdownDialog(Context context) {
        ProgressDialog pd = new ProgressDialog(context);
        String str = mReason;
        if (str == null || !str.startsWith("recovery-update")) {
            String str2 = mReason;
            if (str2 == null || !str2.equals("recovery")) {
                if (showSysuiReboot()) {
                    return null;
                }
                pd.setTitle(context.getText(17040878));
                pd.setMessage(context.getText(17041023));
                pd.setIndeterminate(true);
            } else if (RescueParty.isAttemptingFactoryReset()) {
                pd.setTitle(context.getText(17040878));
                pd.setMessage(context.getText(17041023));
                pd.setIndeterminate(true);
            } else {
                pd.setTitle(context.getText(17040898));
                pd.setMessage(context.getText(17040897));
                pd.setIndeterminate(true);
            }
        } else {
            mRebootHasProgressBar = RecoverySystem.UNCRYPT_PACKAGE_FILE.exists() && !RecoverySystem.BLOCK_MAP_FILE.exists();
            pd.setTitle(context.getText(17040902));
            if (mRebootHasProgressBar) {
                pd.setMax(100);
                pd.setProgress(0);
                pd.setIndeterminate(false);
                pd.setProgressNumberFormat(null);
                pd.setProgressStyle(1);
                pd.setMessage(context.getText(17040900));
            } else if (showSysuiReboot()) {
                return null;
            } else {
                pd.setIndeterminate(true);
                pd.setMessage(context.getText(17040901));
            }
        }
        pd.setCancelable(false);
        pd.getWindow().setType(2009);
        if (sInstance.mIsShowShutdownDialog(context)) {
            pd.show();
        }
        return pd;
    }

    private static boolean showSysuiReboot() {
        if (!sInstance.mIsShowShutdownSysui()) {
            return false;
        }
        Log.d(TAG, "Attempting to use SysUI shutdown UI");
        try {
            if (((StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class)).showShutdownUi(mReboot, mReason)) {
                Log.d(TAG, "SysUI handling shutdown UI");
                return true;
            }
        } catch (Exception e) {
        }
        Log.d(TAG, "SysUI is unavailable");
        return false;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0014, code lost:
        r0 = r8.getPackageManager();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0019, code lost:
        if (r0 == null) goto L_0x003e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0022, code lost:
        if (r0.hasSystemFeature("oppo.cmcc.mp") != false) goto L_0x002d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002b, code lost:
        if (r0.hasSystemFeature("oppo.cmcc.test") == false) goto L_0x003e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002d, code lost:
        android.common.OppoFeatureCache.getOrCreate(com.android.server.power.IPswShutdownFeature.DEFAULT, new java.lang.Object[]{r8}).setBeginAnimationTime(0, true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x003e, code lost:
        r3 = com.android.server.power.ShutdownThread.sInstance;
        r3.mContext = r8;
        r3.mPowerManager = (android.os.PowerManager) r8.getSystemService("power");
        r3 = com.android.server.power.ShutdownThread.sInstance;
        r3.mCpuWakeLock = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        r3.mCpuWakeLock = r3.mPowerManager.newWakeLock(1, "ShutdownThread-cpu");
        com.android.server.power.ShutdownThread.sInstance.mCpuWakeLock.setReferenceCounted(false);
        com.android.server.power.ShutdownThread.sInstance.mCpuWakeLock.acquire();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x006b, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x006c, code lost:
        android.util.Log.w(com.android.server.power.ShutdownThread.TAG, "No permission to acquire wake lock", r3);
        com.android.server.power.ShutdownThread.sInstance.mCpuWakeLock = null;
     */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00ba A[Catch:{ Exception -> 0x00be }] */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00db  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00f4  */
    /* JADX WARNING: Removed duplicated region for block: B:45:? A[RETURN, SYNTHETIC] */
    public static void beginShutdownSequence(Context context) {
        IFingerprintService fingerprintService;
        synchronized (sIsStartedGuard) {
            if (sIsStarted) {
                Log.d(TAG, "Shutdown sequence already running, returning.");
                return;
            }
            sIsStarted = true;
        }
        OppoFeatureCache.getOrCreate(IPswShutdownFeature.DEFAULT, new Object[]{context}).shutdownOppoService(context);
        if (SecurityLog.isLoggingEnabled()) {
            SecurityLog.writeEvent(210010, new Object[0]);
        }
        sInstance.mHandler = new Handler() {
            /* class com.android.server.power.ShutdownThread.AnonymousClass2 */
        };
        if (sInstance.mStartShutdownSeq(context)) {
            sInstance.start();
            return;
        }
        return;
        ShutdownThread shutdownThread = sInstance;
        shutdownThread.mScreenWakeLock = null;
        if (shutdownThread.mPowerManager.isScreenOn()) {
            try {
                sInstance.mScreenWakeLock = sInstance.mPowerManager.newWakeLock(26, "ShutdownThread-screen");
                sInstance.mScreenWakeLock.setReferenceCounted(false);
                sInstance.mScreenWakeLock.acquire();
            } catch (SecurityException e) {
                Log.w(TAG, "No permission to acquire wake lock", e);
                sInstance.mScreenWakeLock = null;
            }
        }
        try {
            fingerprintService = IFingerprintService.Stub.asInterface(ServiceManager.getService("fingerprint"));
            if (fingerprintService != null) {
                fingerprintService.setFingerprintEnabled(false);
            }
        } catch (Exception e2) {
            Log.w(TAG, "FingerprintServie disable failed!", e2);
        }
        OppoFeatureCache.getOrCreate(IPswShutdownFeature.DEFAULT, new Object[]{context}).shutdownOppoService(context);
        if (SecurityLog.isLoggingEnabled()) {
        }
        sInstance.mHandler = new Handler() {
            /* class com.android.server.power.ShutdownThread.AnonymousClass2 */
        };
        if (sInstance.mStartShutdownSeq(context)) {
        }
        fingerprintService = IFingerprintService.Stub.asInterface(ServiceManager.getService("fingerprint"));
        if (fingerprintService != null) {
        }
        OppoFeatureCache.getOrCreate(IPswShutdownFeature.DEFAULT, new Object[]{context}).shutdownOppoService(context);
        if (SecurityLog.isLoggingEnabled()) {
        }
        sInstance.mHandler = new Handler() {
            /* class com.android.server.power.ShutdownThread.AnonymousClass2 */
        };
        if (sInstance.mStartShutdownSeq(context)) {
        }
    }

    /* access modifiers changed from: package-private */
    public void actionDone() {
        synchronized (this.mActionDoneSync) {
            this.mActionDone = true;
            this.mActionDoneSync.notifyAll();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        android.util.Log.w(com.android.server.power.ShutdownThread.TAG, "Shutdown broadcast timed out");
        doShutdownDetect("47");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x00c4, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0100, code lost:
        if (com.android.server.power.ShutdownThread.mRebootHasProgressBar == false) goto L_0x0108;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0102, code lost:
        com.android.server.power.ShutdownThread.sInstance.setRebootProgress(2, null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0108, code lost:
        r2.traceEnd();
        metricEnded(com.android.server.power.ShutdownThread.METRIC_SEND_BROADCAST);
        android.util.Log.i(com.android.server.power.ShutdownThread.TAG, "Shutting down activity manager...");
        r2.traceBegin("ShutdownActivityManager");
        metricStarted(com.android.server.power.ShutdownThread.METRIC_AM);
        r10 = android.os.SystemClock.elapsedRealtime();
        r8 = android.app.IActivityManager.Stub.asInterface(android.os.ServiceManager.checkService(com.android.server.am.IColorAppStartupManager.TYPE_ACTIVITY));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0130, code lost:
        if (r8 == null) goto L_0x0139;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:?, code lost:
        r8.shutdown(5000);
     */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x0257 A[Catch:{ Exception -> 0x0263 }] */
    /* JADX WARNING: Removed duplicated region for block: B:78:0x025b A[Catch:{ Exception -> 0x0263 }] */
    public void run() {
        Throwable th;
        long startTime;
        IStorageManager storageManager;
        Intent intent;
        TimingsTraceLog shutdownTimingLog = newTimingsLog();
        shutdownTimingLog.traceBegin("SystemServerShutdown");
        metricShutdownStart();
        metricStarted(METRIC_SYSTEM_SERVER);
        doShutdownDetect("40");
        BroadcastReceiver br = new BroadcastReceiver() {
            /* class com.android.server.power.ShutdownThread.AnonymousClass3 */

            public void onReceive(Context context, Intent intent) {
                ShutdownThread.this.actionDone();
            }
        };
        StringBuilder sb = new StringBuilder();
        sb.append(mReboot ? NoFocusWindow.HUNG_CONFIG_ENABLE : "0");
        String str = mReason;
        if (str == null) {
            str = "";
        }
        sb.append(str);
        SystemProperties.set(SHUTDOWN_ACTION_PROPERTY, sb.toString());
        if (mRebootSafeMode) {
            SystemProperties.set(REBOOT_SAFEMODE_PROPERTY, NoFocusWindow.HUNG_CONFIG_ENABLE);
        }
        metricStarted(METRIC_SEND_BROADCAST);
        shutdownTimingLog.traceBegin("SendShutdownBroadcast");
        Log.i(TAG, "Sending shutdown broadcast...");
        OppoFeatureCache.getOrCreate(IPswShutdownFeature.DEFAULT, new Object[]{this.mContext}).checkShutdownTimeout(this.mContext, mReboot, mReason, 500, VIBRATION_ATTRIBUTES);
        this.mActionDone = false;
        Intent intent2 = new Intent("android.intent.action.ACTION_SHUTDOWN");
        intent2.addFlags(1342177280);
        this.mContext.sendOrderedBroadcastAsUser(intent2, UserHandle.ALL, null, br, this.mHandler, 0, null, null);
        long endTime = SystemClock.elapsedRealtime() + 5000;
        synchronized (this.mActionDoneSync) {
            while (true) {
                try {
                    if (this.mActionDone) {
                        break;
                    }
                    long delay = endTime - SystemClock.elapsedRealtime();
                    if (delay <= 0) {
                        break;
                    }
                    if (mRebootHasProgressBar) {
                        intent = intent2;
                        try {
                            sInstance.setRebootProgress((int) (((((double) (5000 - delay)) * 1.0d) * 2.0d) / 5000.0d), null);
                        } catch (Throwable th2) {
                            th = th2;
                        }
                    } else {
                        intent = intent2;
                    }
                    try {
                        this.mActionDoneSync.wait(Math.min(delay, 500L));
                    } catch (InterruptedException e) {
                    }
                    intent2 = intent;
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
        }
        if (mRebootHasProgressBar) {
            sInstance.setRebootProgress(4, null);
        }
        if (SystemClock.elapsedRealtime() - startTime > 5000) {
            doShutdownDetect("46");
        }
        shutdownTimingLog.traceEnd();
        metricEnded(METRIC_AM);
        Log.i(TAG, "Shutting down package manager...");
        shutdownTimingLog.traceBegin("ShutdownPackageManager");
        metricStarted(METRIC_PM);
        long startTime2 = SystemClock.elapsedRealtime();
        PackageManagerService pm = (PackageManagerService) ServiceManager.getService(Settings.ATTR_PACKAGE);
        if (pm != null) {
            pm.shutdown();
        }
        if (mRebootHasProgressBar) {
            sInstance.setRebootProgress(6, null);
        }
        if (SystemClock.elapsedRealtime() - startTime2 > 5000) {
            doShutdownDetect("45");
        }
        shutdownTimingLog.traceEnd();
        metricEnded(METRIC_PM);
        shutdownTimingLog.traceBegin("ShutdownRadios");
        metricStarted(METRIC_RADIOS);
        shutdownRadios(MAX_RADIO_WAIT_TIME);
        if (mRebootHasProgressBar) {
            sInstance.setRebootProgress(18, null);
        }
        shutdownTimingLog.traceEnd();
        metricEnded(METRIC_RADIOS);
        OppoFeatureCache.getOrCreate(IPswShutdownFeature.DEFAULT, new Object[]{this.mContext}).delayForPlayAnimation();
        Log.i(TAG, "record DCIM dellog");
        OppoFeatureCache.getOrCreate(IPswShutdownFeature.DEFAULT, new Object[]{this.mContext}).storeDellog();
        if (mRebootHasProgressBar) {
            sInstance.setRebootProgress(20, null);
            uncrypt();
        }
        mShutdownSeqFinish(this.mContext);
        IWindowManager.Stub.asInterface(ServiceManager.checkService("window"));
        shutdownTimingLog.traceEnd();
        metricEnded(METRIC_SYSTEM_SERVER);
        saveMetrics(mReboot, mReason);
        Log.i(TAG, "Shutting down OppoUsageService");
        try {
            IOppoUsageService usageService = IOppoUsageService.Stub.asInterface(ServiceManager.getService("usage"));
            if (usageService != null) {
                usageService.shutDown();
            }
        } catch (Exception e2) {
            Log.w(TAG, "Shutting down OppoUsageService failed!", e2);
        }
        IStorageShutdownObserver observer = new IStorageShutdownObserver.Stub() {
            /* class com.android.server.power.ShutdownThread.AnonymousClass4 */

            public void onShutDownComplete(int statusCode) throws RemoteException {
                Log.w(ShutdownThread.TAG, "Result code " + statusCode + " from StorageManagerService.shutdown");
                ShutdownThread.this.actionDone();
            }
        };
        Log.i(TAG, "Shutting down StorageManagerService");
        try {
            storageManager = IStorageManager.Stub.asInterface(ServiceManager.checkService("mount"));
            if (storageManager == null) {
                storageManager.shutdown(observer);
            } else {
                Log.w(TAG, "StorageManagerService unavailable for shutdown");
            }
        } catch (Exception e3) {
            Log.e(TAG, "Exception during StorageManagerService shutdown", e3);
        }
        Log.i(TAG, "Shutting down StorageManagerService end");
        rebootOrShutdown(this.mContext, mReboot, mReason);
        Log.i(TAG, "Shutting down StorageManagerService end");
        rebootOrShutdown(this.mContext, mReboot, mReason);
        IStorageShutdownObserver observer2 = new IStorageShutdownObserver.Stub() {
            /* class com.android.server.power.ShutdownThread.AnonymousClass4 */

            public void onShutDownComplete(int statusCode) throws RemoteException {
                Log.w(ShutdownThread.TAG, "Result code " + statusCode + " from StorageManagerService.shutdown");
                ShutdownThread.this.actionDone();
            }
        };
        Log.i(TAG, "Shutting down StorageManagerService");
        storageManager = IStorageManager.Stub.asInterface(ServiceManager.checkService("mount"));
        if (storageManager == null) {
        }
        Log.i(TAG, "Shutting down StorageManagerService end");
        rebootOrShutdown(this.mContext, mReboot, mReason);
    }

    /* access modifiers changed from: private */
    public static TimingsTraceLog newTimingsLog() {
        return new TimingsTraceLog("ShutdownTiming", 524288);
    }

    /* access modifiers changed from: private */
    public static void metricStarted(String metricKey) {
        synchronized (TRON_METRICS) {
            TRON_METRICS.put(metricKey, Long.valueOf(SystemClock.elapsedRealtime() * -1));
        }
    }

    /* access modifiers changed from: private */
    public static void metricEnded(String metricKey) {
        synchronized (TRON_METRICS) {
            TRON_METRICS.put(metricKey, Long.valueOf(SystemClock.elapsedRealtime() + TRON_METRICS.get(metricKey).longValue()));
        }
    }

    private static void metricShutdownStart() {
        synchronized (TRON_METRICS) {
            TRON_METRICS.put(METRIC_SHUTDOWN_TIME_START, Long.valueOf(System.currentTimeMillis()));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setRebootProgress(final int progress, final CharSequence message) {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.power.ShutdownThread.AnonymousClass5 */

            public void run() {
                if (ShutdownThread.this.mProgressDialog != null) {
                    ShutdownThread.this.mProgressDialog.setProgress(progress);
                    if (message != null) {
                        ShutdownThread.this.mProgressDialog.setMessage(message);
                    }
                }
            }
        });
    }

    private void shutdownRadios(final int timeout) {
        final long endTime = SystemClock.elapsedRealtime() + ((long) timeout);
        final boolean[] done = new boolean[1];
        Thread t = new Thread() {
            /* class com.android.server.power.ShutdownThread.AnonymousClass6 */

            /* JADX WARNING: Removed duplicated region for block: B:11:0x0026 A[Catch:{ RemoteException -> 0x0021 }] */
            /* JADX WARNING: Removed duplicated region for block: B:16:0x004e  */
            public void run() {
                boolean radioOff;
                long delay;
                TimingsTraceLog shutdownTimingsTraceLog = ShutdownThread.newTimingsLog();
                ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
                if (phone != null) {
                    try {
                        if (phone.needMobileRadioShutdown()) {
                            radioOff = false;
                            if (!radioOff) {
                                Log.w(ShutdownThread.TAG, "Turning off cellular radios...");
                                ShutdownThread.metricStarted(ShutdownThread.METRIC_RADIO);
                                phone.shutdownMobileRadios();
                            }
                            Log.i(ShutdownThread.TAG, "Waiting for Radio...");
                            delay = endTime - SystemClock.elapsedRealtime();
                            while (delay > 0) {
                                if (ShutdownThread.mRebootHasProgressBar) {
                                    int i = timeout;
                                    ShutdownThread.sInstance.setRebootProgress(((int) (((((double) (((long) i) - delay)) * 1.0d) * 12.0d) / ((double) i))) + 6, null);
                                }
                                if (!radioOff) {
                                    try {
                                        radioOff = !phone.needMobileRadioShutdown();
                                    } catch (RemoteException ex) {
                                        Log.e(ShutdownThread.TAG, "RemoteException during radio shutdown", ex);
                                        radioOff = true;
                                    }
                                    if (radioOff) {
                                        Log.i(ShutdownThread.TAG, "Radio turned off.");
                                        ShutdownThread.metricEnded(ShutdownThread.METRIC_RADIO);
                                        shutdownTimingsTraceLog.logDuration("ShutdownRadio", ((Long) ShutdownThread.TRON_METRICS.get(ShutdownThread.METRIC_RADIO)).longValue());
                                    }
                                }
                                if (radioOff) {
                                    Log.i(ShutdownThread.TAG, "Radio shutdown complete.");
                                    done[0] = true;
                                    return;
                                }
                                SystemClock.sleep(100);
                                delay = endTime - SystemClock.elapsedRealtime();
                            }
                        }
                    } catch (RemoteException ex2) {
                        Log.e(ShutdownThread.TAG, "RemoteException during radio shutdown", ex2);
                        radioOff = true;
                    }
                }
                radioOff = true;
                if (!radioOff) {
                }
                Log.i(ShutdownThread.TAG, "Waiting for Radio...");
                delay = endTime - SystemClock.elapsedRealtime();
                while (delay > 0) {
                }
            }
        };
        t.start();
        try {
            t.join((long) timeout);
        } catch (InterruptedException e) {
        }
        if (!done[0]) {
            Log.w(TAG, "Timed out waiting for Radio shutdown.");
        }
    }

    private static void doShutdownDetect(String cout) {
        FileWriter shutdown_detect = null;
        try {
            Log.e(TAG, "doShutdownDetect " + cout);
            shutdown_detect = new FileWriter(new File("/proc/shutdown_detect"));
            shutdown_detect.write(cout);
        } catch (IOException e) {
            Log.w(TAG, "Failed to write to /proc/shutdown_detect", e);
        } catch (Throwable th) {
            IoUtils.closeQuietly(shutdown_detect);
            throw th;
        }
        IoUtils.closeQuietly(shutdown_detect);
    }

    public static void rebootOrShutdown(Context context, boolean reboot, String reason) {
        if (reboot) {
            Log.i(TAG, "Rebooting, reason: " + reason);
            if (OppoFeatureCache.getOrCreate(IPswShutdownFeature.DEFAULT, new Object[]{context}).shouldDoLowLevelShutdown()) {
                PowerManagerService.lowLevelReboot(reason);
            }
            Log.e(TAG, "Reboot failed, will attempt shutdown instead");
        } else if (context != null) {
            if (OppoFeatureCache.getOrCreate(IPswShutdownFeature.DEFAULT, new Object[]{context}).shouldDoLowLevelShutdown()) {
                try {
                    new SystemVibrator(context).vibrate(500, VIBRATION_ATTRIBUTES);
                } catch (Exception e) {
                    Log.w(TAG, "Failed to vibrate during shutdown.", e);
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e2) {
                }
                sInstance.mLowLevelShutdownSeq(context);
                Log.i(TAG, "Performing low-level shutdown normal...");
                PowerManagerService.lowLevelShutdown(reason);
                return;
            }
            Log.i(TAG, "Shutdown process timeout noneed do lowLevelShutdown and vibrate");
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0094, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:?, code lost:
        r5.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0099, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x009a, code lost:
        r6.addSuppressed(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x009d, code lost:
        throw r7;
     */
    private static void saveMetrics(boolean reboot, String reason) {
        StringBuilder metricValue = new StringBuilder();
        metricValue.append("reboot:");
        metricValue.append(reboot ? "y" : "n");
        metricValue.append(",");
        metricValue.append("reason:");
        metricValue.append(reason);
        int metricsSize = TRON_METRICS.size();
        for (int i = 0; i < metricsSize; i++) {
            String name = TRON_METRICS.keyAt(i);
            long value = TRON_METRICS.valueAt(i).longValue();
            if (value < 0) {
                Log.e(TAG, "metricEnded wasn't called for " + name);
            } else {
                metricValue.append(',');
                metricValue.append(name);
                metricValue.append(':');
                metricValue.append(value);
            }
        }
        File tmp = new File("/data/system/shutdown-metrics.tmp");
        boolean saved = false;
        try {
            FileOutputStream fos = new FileOutputStream(tmp);
            fos.write(metricValue.toString().getBytes(StandardCharsets.UTF_8));
            saved = true;
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, "Cannot save shutdown metrics", e);
        }
        if (saved) {
            tmp.renameTo(new File("/data/system/shutdown-metrics.txt"));
        }
    }

    private void uncrypt() {
        Log.i(TAG, "Calling uncrypt and monitoring the progress...");
        final RecoverySystem.ProgressListener progressListener = new RecoverySystem.ProgressListener() {
            /* class com.android.server.power.ShutdownThread.AnonymousClass7 */

            public void onProgress(int status) {
                if (status >= 0 && status < 100) {
                    CharSequence msg = ShutdownThread.this.mContext.getText(17040899);
                    ShutdownThread.sInstance.setRebootProgress(((int) ((((double) status) * 80.0d) / 100.0d)) + 20, msg);
                } else if (status == 100) {
                    ShutdownThread.sInstance.setRebootProgress(status, ShutdownThread.this.mContext.getText(17040901));
                }
            }
        };
        final boolean[] done = {false};
        Thread t = new Thread() {
            /* class com.android.server.power.ShutdownThread.AnonymousClass8 */

            public void run() {
                RecoverySystem recoverySystem = (RecoverySystem) ShutdownThread.this.mContext.getSystemService("recovery");
                try {
                    RecoverySystem.processPackage(ShutdownThread.this.mContext, new File(FileUtils.readTextFile(RecoverySystem.UNCRYPT_PACKAGE_FILE, 0, null)), progressListener);
                } catch (IOException e) {
                    Log.e(ShutdownThread.TAG, "Error uncrypting file", e);
                }
                done[0] = true;
            }
        };
        t.start();
        try {
            t.join(900000);
        } catch (InterruptedException e) {
        }
        if (!done[0]) {
            Log.w(TAG, "Timed out waiting for uncrypt.");
            try {
                FileUtils.stringToFile(RecoverySystem.UNCRYPT_STATUS_FILE, String.format("uncrypt_time: %d\nuncrypt_error: %d\n", 900, 100));
            } catch (IOException e2) {
                Log.e(TAG, "Failed to write timeout message to uncrypt status", e2);
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean mIsShowShutdownSysui() {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean mIsShowShutdownDialog(Context c) {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean mStartShutdownSeq(Context c) {
        return true;
    }

    /* access modifiers changed from: protected */
    public void mShutdownSeqFinish(Context c) {
    }

    /* access modifiers changed from: protected */
    public void mLowLevelShutdownSeq(Context c) {
    }
}
