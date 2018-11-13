package com.android.server.power;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.IActivityManager;
import android.app.ProgressDialog;
import android.bluetooth.IBluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.hardware.fingerprint.IFingerprintService;
import android.media.AudioAttributes;
import android.media.AudioAttributes.Builder;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.nfc.INfcAdapter;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IOppoUsageService;
import android.os.OppoManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RecoverySystem;
import android.os.RecoverySystem.ProgressListener;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.SystemVibrator;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.IStorageManager;
import android.os.storage.IStorageShutdownObserver;
import android.provider.Settings.System;
import android.util.ArrayMap;
import android.util.Log;
import android.util.TimingsTraceLog;
import android.view.IWindowManager;
import android.view.IWindowManager.Stub;
import com.android.internal.telephony.ITelephony;
import com.android.server.AgingCriticalEvent;
import com.android.server.LocalServices;
import com.android.server.LocationManagerService;
import com.android.server.RescueParty;
import com.android.server.am.OppoAppStartupManager;
import com.android.server.am.OppoCrashClearManager;
import com.android.server.am.OppoProcessManager;
import com.android.server.display.OppoBrightUtils;
import com.android.server.face.FaceDaemonWrapper;
import com.android.server.pm.PackageManagerService;
import com.android.server.statusbar.StatusBarManagerInternal;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ShutdownThread extends Thread {
    private static final int ACTIVITY_MANAGER_STOP_PERCENT = 4;
    private static final int BROADCAST_STOP_PERCENT = 2;
    private static final int MAX_BROADCAST_TIME = 5000;
    private static final int MAX_RADIO_WAIT_TIME = 3000;
    private static final int MAX_SHUTDOWN_WAIT_TIME = 10000;
    private static final int MAX_UNCRYPT_WAIT_TIME = 900000;
    private static final String METRICS_FILE_BASENAME = "/data/system/shutdown-metrics";
    private static String METRIC_AM = "shutdown_activity_manager";
    private static String METRIC_BT = "shutdown_bt";
    private static String METRIC_NFC = "shutdown_nfc";
    private static String METRIC_PM = "shutdown_package_manager";
    private static String METRIC_RADIO = "shutdown_radio";
    private static String METRIC_RADIOS = "shutdown_radios";
    private static String METRIC_SEND_BROADCAST = "shutdown_send_shutdown_broadcast";
    private static String METRIC_SM = "shutdown_storage_manager";
    private static String METRIC_SYSTEM_SERVER = "shutdown_system_server";
    private static final int MIN_SHUTDOWN_ANIMATION_PLAY_TIME_FOR_CMCC = 3000;
    private static final int MOUNT_SERVICE_STOP_PERCENT = 20;
    private static final String OEM_BOOTANIMATION_FILE = "/oem/media/shutdownanimation.zip";
    private static final int PACKAGE_MANAGER_STOP_PERCENT = 6;
    private static final int PHONE_STATE_POLL_SLEEP_MSEC = 100;
    private static final int RADIO_STOP_PERCENT = 18;
    public static final String REBOOT_SAFEMODE_PROPERTY = "persist.sys.safemode";
    public static final String RO_SAFEMODE_PROPERTY = "ro.sys.safemode";
    public static final String SHUTDOWN_ACTION_PROPERTY = "sys.shutdown.requested";
    private static final int SHUTDOWN_VIBRATE_MS = 500;
    private static final String SYSTEM_BOOTANIMATION_FILE = "/system/media/bootanimation/rbootanimation.zip";
    private static final String SYSTEM_ENCRYPTED_BOOTANIMATION_FILE = "/system/media/shutdownanimation-encrypted.zip";
    private static final String TAG = "ShutdownThread";
    private static final ArrayMap<String, Long> TRON_METRICS = new ArrayMap();
    private static final int VENDOR_SUBSYS_MAX_WAIT_MS = 10000;
    private static final int VENDOR_SUBSYS_STATE_CHECK_INTERVAL_MS = 100;
    private static final AudioAttributes VIBRATION_ATTRIBUTES = new Builder().setContentType(4).setUsage(13).build();
    private static boolean bIsAnimationtimeForCmcc = false;
    private static long beginAnimationTime = 0;
    private static long endAnimationTime = 0;
    private static AtomicBoolean hasVibrate = new AtomicBoolean(false);
    private static AudioManager mAudioManager = null;
    private static String mReason = null;
    private static boolean mReboot = false;
    private static boolean mRebootHasProgressBar = false;
    private static boolean mRebootSafeMode = false;
    private static final boolean mSpew = true;
    private static AlertDialog sConfirmDialog;
    private static final ShutdownThread sInstance = new ShutdownThread();
    private static boolean sIsStarted = false;
    private static final Object sIsStartedGuard = new Object();
    private boolean mActionDone;
    private final Object mActionDoneSync = new Object();
    private Context mContext;
    private WakeLock mCpuWakeLock;
    private Handler mHandler;
    private PowerManager mPowerManager;
    private ProgressDialog mProgressDialog;
    private WakeLock mScreenWakeLock;

    private static class CloseDialogReceiver extends BroadcastReceiver implements OnDismissListener {
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

    private ShutdownThread() {
    }

    public static void shutdown(Context context, String reason, boolean confirm) {
        int i = 0;
        mReboot = false;
        mRebootSafeMode = false;
        Log.d(TAG, "!!! Request to shutdown !!!");
        StackTraceElement[] stack = new Throwable().getStackTrace();
        int length = stack.length;
        while (i < length) {
            Log.d(TAG, "    |----" + stack[i].toString());
            i++;
        }
        resetBrightnessAdj(context);
        Log.v(TAG, "oppomanager sync cache to emmc");
        OppoManager.syncCacheToEmmc();
        mReason = reason;
        shutdownInner(context, confirm);
    }

    /* JADX WARNING: Missing block: B:17:0x0071, code:
            r1 = r10.getResources().getInteger(17694802);
     */
    /* JADX WARNING: Missing block: B:18:0x007e, code:
            if (mRebootSafeMode == false) goto L_0x00f7;
     */
    /* JADX WARNING: Missing block: B:19:0x0080, code:
            r2 = 17040736;
     */
    /* JADX WARNING: Missing block: B:20:0x0083, code:
            android.util.Log.d(TAG, "Notifying thread to start shutdown longPressBehavior=" + r1);
     */
    /* JADX WARNING: Missing block: B:21:0x009d, code:
            if (r11 == false) goto L_0x0105;
     */
    /* JADX WARNING: Missing block: B:22:0x009f, code:
            r0 = new com.android.server.power.ShutdownThread.CloseDialogReceiver(r10);
     */
    /* JADX WARNING: Missing block: B:23:0x00a6, code:
            if (sConfirmDialog == null) goto L_0x00ad;
     */
    /* JADX WARNING: Missing block: B:24:0x00a8, code:
            sConfirmDialog.dismiss();
     */
    /* JADX WARNING: Missing block: B:25:0x00ad, code:
            r4 = new android.app.AlertDialog.Builder(r10);
     */
    /* JADX WARNING: Missing block: B:26:0x00b4, code:
            if (mRebootSafeMode == false) goto L_0x0101;
     */
    /* JADX WARNING: Missing block: B:27:0x00b6, code:
            r3 = 17040737;
     */
    /* JADX WARNING: Missing block: B:28:0x00b9, code:
            sConfirmDialog = r4.setTitle(r3).setMessage(r2).setPositiveButton(17039379, new com.android.server.power.ShutdownThread.AnonymousClass1()).setNegativeButton(17039369, null).create();
            r0.dialog = sConfirmDialog;
            sConfirmDialog.setOnDismissListener(r0);
            sConfirmDialog.getWindow().setType(2009);
            sConfirmDialog.show();
     */
    /* JADX WARNING: Missing block: B:29:0x00f3, code:
            return;
     */
    /* JADX WARNING: Missing block: B:33:0x00f7, code:
            if (r1 != 2) goto L_0x00fd;
     */
    /* JADX WARNING: Missing block: B:34:0x00f9, code:
            r2 = 17040861;
     */
    /* JADX WARNING: Missing block: B:35:0x00fd, code:
            r2 = 17040860;
     */
    /* JADX WARNING: Missing block: B:36:0x0101, code:
            r3 = 17040720;
     */
    /* JADX WARNING: Missing block: B:37:0x0105, code:
            beginShutdownSequence(r10);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void shutdownInner(final Context context, boolean confirm) {
        String str;
        AgingCriticalEvent instance = AgingCriticalEvent.getInstance();
        String str2 = AgingCriticalEvent.EVENT_USER_POWER_OFF;
        String[] strArr = new String[2];
        if (mReboot) {
            str = "reboot, reason:" + mReason + " mRebootSafeMode:" + mRebootSafeMode;
        } else {
            str = "shutdown, reason:" + mReason;
        }
        strArr[0] = str;
        if (confirm) {
            str = "confirmed";
        } else {
            str = "noNeedConfirm";
        }
        strArr[1] = str;
        instance.writeEvent(str2, strArr);
        context.assertRuntimeOverlayThemable();
        synchronized (sIsStartedGuard) {
            if (sIsStarted) {
                Log.d(TAG, "Request to shutdown already running, returning.");
            }
        }
    }

    public static void reboot(Context context, String reason, boolean confirm) {
        Log.v(TAG, "oppomanager sync cache to emmc");
        OppoManager.syncCacheToEmmc();
        if ("sau".equals(reason) || "silence".equals(reason)) {
            SystemProperties.set("sys.bootsilence", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
        }
        Log.d(TAG, "!!! Request to reboot !!!");
        for (StackTraceElement element : new Throwable().getStackTrace()) {
            Log.d(TAG, "    |----" + element.toString());
        }
        resetBrightnessAdj(context);
        mReboot = true;
        mRebootSafeMode = false;
        mRebootHasProgressBar = false;
        mReason = reason;
        shutdownInner(context, confirm);
    }

    public static void rebootSafeMode(Context context, boolean confirm) {
        if (!((UserManager) context.getSystemService("user")).hasUserRestriction("no_safe_boot")) {
            resetBrightnessAdj(context);
            mReboot = true;
            mRebootSafeMode = true;
            mRebootHasProgressBar = false;
            mReason = null;
            shutdownInner(context, confirm);
        }
    }

    private static ProgressDialog showShutdownDialog(Context context) {
        ProgressDialog pd = new ProgressDialog(context);
        if (mReason != null && mReason.startsWith("recovery-update")) {
            boolean exists;
            if (RecoverySystem.UNCRYPT_PACKAGE_FILE.exists()) {
                exists = RecoverySystem.BLOCK_MAP_FILE.exists() ^ 1;
            } else {
                exists = false;
            }
            mRebootHasProgressBar = exists;
            pd.setTitle(context.getText(17040744));
            if (mRebootHasProgressBar) {
                pd.setMax(100);
                pd.setProgress(0);
                pd.setIndeterminate(false);
                pd.setProgressNumberFormat(null);
                pd.setProgressStyle(1);
                pd.setMessage(context.getText(17040742));
            } else if (showSysuiReboot()) {
                return null;
            } else {
                pd.setIndeterminate(true);
                pd.setMessage(context.getText(17040743));
            }
        } else if (mReason == null || !mReason.equals("recovery")) {
            if (showSysuiReboot()) {
                return null;
            }
            pd.setTitle(context.getText(17040720));
            pd.setMessage(context.getText(17040862));
            pd.setIndeterminate(true);
        } else if (RescueParty.isAttemptingFactoryReset()) {
            pd.setTitle(context.getText(17040720));
            pd.setMessage(context.getText(17040862));
            pd.setIndeterminate(true);
        } else {
            pd.setTitle(context.getText(17040740));
            pd.setMessage(context.getText(17040739));
            pd.setIndeterminate(true);
        }
        pd.setCancelable(false);
        pd.getWindow().setType(2009);
        return pd;
    }

    private static boolean showSysuiReboot() {
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

    private static void lockDevice() {
        try {
            Stub.asInterface(ServiceManager.getService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).updateRotation(false, false);
        } catch (RemoteException e) {
            Log.w(TAG, "boot animation can not lock device!");
        }
    }

    public static void stopTopApplication(final Context context) {
        new Thread(new Runnable() {
            public void run() {
                int i = 0;
                ActivityManager activityManager = (ActivityManager) context.getSystemService(OppoAppStartupManager.TYPE_ACTIVITY);
                List<RecentTaskInfo> apps = activityManager.getRecentTasks(1, 2);
                if (apps.size() > 0) {
                    RecentTaskInfo topApp = (RecentTaskInfo) apps.get(0);
                    String packageName = topApp.baseIntent.getComponent().getPackageName();
                    String className = topApp.baseIntent.getComponent().getClassName();
                    ActivityInfo homeInfo = new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").resolveActivityInfo(context.getPackageManager(), 0);
                    if (homeInfo != null && homeInfo.packageName.equals(packageName)) {
                        i = homeInfo.name.equals(className);
                    }
                    if (i == 0) {
                        Log.i(ShutdownThread.TAG, "force stop package " + packageName);
                        activityManager.forceStopPackage(packageName);
                    }
                }
            }
        }).start();
    }

    private static boolean checkAnimationFileExist() {
        if (new File(OEM_BOOTANIMATION_FILE).exists() || new File(SYSTEM_BOOTANIMATION_FILE).exists() || new File(SYSTEM_ENCRYPTED_BOOTANIMATION_FILE).exists()) {
            return true;
        }
        return false;
    }

    private static boolean isSilentMode() {
        return mAudioManager.isSilentMode();
    }

    private static void showShutdownAnimation() {
        if (bIsAnimationtimeForCmcc) {
            beginAnimationTime = SystemClock.elapsedRealtime() + 3000;
        }
        SystemProperties.set("service.bootanim.exit", "0");
        Log.i(TAG, "show ShutdownAnimation...");
        SystemProperties.set("ctl.start", "rbootanim");
    }

    private static void postDelayed(final Runnable r, final long delayMillis) {
        new Thread() {
            public void run() {
                try {
                    AnonymousClass3.sleep(delayMillis);
                } catch (Exception e) {
                }
                r.run();
            }
        }.start();
    }

    /* JADX WARNING: Removed duplicated region for block: B:33:0x00cf  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x012a  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00ae A:{Catch:{ Exception -> 0x0101 }} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00cf  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x012a  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x012a  */
    /* JADX WARNING: Missing block: B:12:0x0019, code:
            r6 = r15.getPackageManager();
     */
    /* JADX WARNING: Missing block: B:13:0x001d, code:
            if (r6 == null) goto L_0x0037;
     */
    /* JADX WARNING: Missing block: B:15:0x0026, code:
            if (r6.hasSystemFeature("oppo.cmcc.mp") != false) goto L_0x0031;
     */
    /* JADX WARNING: Missing block: B:17:0x002f, code:
            if (r6.hasSystemFeature("oppo.cmcc.test") == false) goto L_0x0037;
     */
    /* JADX WARNING: Missing block: B:18:0x0031, code:
            beginAnimationTime = 0;
            bIsAnimationtimeForCmcc = true;
     */
    /* JADX WARNING: Missing block: B:19:0x0037, code:
            sInstance.mProgressDialog = showShutdownDialog(r15);
            sInstance.mContext = r15;
            sInstance.mPowerManager = (android.os.PowerManager) r15.getSystemService("power");
            sInstance.mCpuWakeLock = null;
     */
    /* JADX WARNING: Missing block: B:21:?, code:
            sInstance.mCpuWakeLock = sInstance.mPowerManager.newWakeLock(1, "ShutdownThread-cpu");
            sInstance.mCpuWakeLock.setReferenceCounted(false);
            sInstance.mCpuWakeLock.acquire();
     */
    /* JADX WARNING: Missing block: B:42:0x00e3, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:43:0x00e4, code:
            android.util.Log.w(TAG, "No permission to acquire wake lock", r2);
            sInstance.mCpuWakeLock = null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void beginShutdownSequence(Context context) {
        synchronized (sIsStartedGuard) {
            if (sIsStarted) {
                Log.d(TAG, "Shutdown sequence already running, returning.");
                return;
            }
            sIsStarted = true;
        }
        IFingerprintService fingerprintService;
        AudioManager audioManager;
        int i;
        IWindowManager windowManger;
        try {
            fingerprintService = IFingerprintService.Stub.asInterface(ServiceManager.getService("fingerprint"));
            if (fingerprintService != null) {
                fingerprintService.setFingerprintEnabled(false);
            }
        } catch (Exception e) {
            Log.w(TAG, "FingerprintServie disable failed!", e);
        }
        audioManager = (AudioManager) context.getSystemService("audio");
        Log.i(TAG, "mute audios...");
        audioManager.requestAudioFocus(null, 3, 1);
        for (i = 0; i < AudioSystem.getNumStreamTypes(); i++) {
            if (i != 1) {
                audioManager.setStreamMute(i, true);
            }
            if (!audioManager.isSilentMode()) {
                audioManager.setStreamMute(1, false);
            }
        }
        windowManger = Stub.asInterface(ServiceManager.checkService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR));
        try {
            windowManger.setEventDispatching(false);
            windowManger.setShowLockForBootAnimation(true);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        SystemClock.sleep(50);
        if (checkAnimationFileExist()) {
            lockDevice();
            showShutdownAnimation();
        }
        sInstance.mHandler = new Handler() {
        };
        sInstance.start();
        audioManager = (AudioManager) context.getSystemService("audio");
        Log.i(TAG, "mute audios...");
        audioManager.requestAudioFocus(null, 3, 1);
        while (i < AudioSystem.getNumStreamTypes()) {
        }
        windowManger = Stub.asInterface(ServiceManager.checkService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR));
        windowManger.setEventDispatching(false);
        windowManger.setShowLockForBootAnimation(true);
        SystemClock.sleep(50);
        if (checkAnimationFileExist()) {
        }
        sInstance.mHandler = /* anonymous class already generated */;
        sInstance.start();
        sInstance.mScreenWakeLock = null;
        if (sInstance.mPowerManager.isScreenOn()) {
            try {
                sInstance.mScreenWakeLock = sInstance.mPowerManager.newWakeLock(26, "ShutdownThread-screen");
                sInstance.mScreenWakeLock.setReferenceCounted(false);
                sInstance.mScreenWakeLock.acquire();
            } catch (SecurityException e3) {
                Log.w(TAG, "No permission to acquire wake lock", e3);
                sInstance.mScreenWakeLock = null;
            }
        }
        fingerprintService = IFingerprintService.Stub.asInterface(ServiceManager.getService("fingerprint"));
        if (fingerprintService != null) {
        }
        audioManager = (AudioManager) context.getSystemService("audio");
        Log.i(TAG, "mute audios...");
        audioManager.requestAudioFocus(null, 3, 1);
        while (i < AudioSystem.getNumStreamTypes()) {
        }
        windowManger = Stub.asInterface(ServiceManager.checkService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR));
        windowManger.setEventDispatching(false);
        windowManger.setShowLockForBootAnimation(true);
        SystemClock.sleep(50);
        if (checkAnimationFileExist()) {
        }
        sInstance.mHandler = /* anonymous class already generated */;
        sInstance.start();
        SystemClock.sleep(50);
        if (checkAnimationFileExist()) {
        }
        sInstance.mHandler = /* anonymous class already generated */;
        sInstance.start();
    }

    void actionDone() {
        synchronized (this.mActionDoneSync) {
            this.mActionDone = true;
            this.mActionDoneSync.notifyAll();
        }
    }

    private static void delayForPlayAnimation() {
        if (beginAnimationTime > 0) {
            endAnimationTime = beginAnimationTime - SystemClock.elapsedRealtime();
            if (endAnimationTime > 0) {
                try {
                    Thread.currentThread();
                    Thread.sleep(endAnimationTime);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Shutdown stop bootanimation Thread.currentThread().sleep exception!");
                }
            }
        }
    }

    public void run() {
        String str;
        long delay;
        TimingsTraceLog shutdownTimingLog = newTimingsLog();
        shutdownTimingLog.traceBegin("SystemServerShutdown");
        metricStarted(METRIC_SYSTEM_SERVER);
        BroadcastReceiver br = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                ShutdownThread.this.actionDone();
            }
        };
        StringBuilder append = new StringBuilder().append(mReboot ? LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON : "0");
        if (mReason != null) {
            str = mReason;
        } else {
            str = "";
        }
        SystemProperties.set(SHUTDOWN_ACTION_PROPERTY, append.append(str).toString());
        if (mRebootSafeMode) {
            SystemProperties.set(REBOOT_SAFEMODE_PROPERTY, LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
        }
        metricStarted(METRIC_SEND_BROADCAST);
        shutdownTimingLog.traceBegin("SendShutdownBroadcast");
        Log.i(TAG, "Sending shutdown broadcast...");
        postDelayed(new Runnable() {
            public void run() {
                if (ShutdownThread.hasVibrate.compareAndSet(false, true)) {
                    Log.i(ShutdownThread.TAG, "wait shutdown timeout! force shutdown now");
                    if (ShutdownThread.this.mContext != null) {
                        try {
                            new SystemVibrator(ShutdownThread.this.mContext).vibrate(500, ShutdownThread.VIBRATION_ATTRIBUTES);
                        } catch (Exception e) {
                            Log.w(ShutdownThread.TAG, "Failed to vibrate during shutdown.", e);
                        }
                        Log.i(ShutdownThread.TAG, "wait shutdown timeout! vibrate for better user experience");
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e2) {
                        }
                    }
                    if (ShutdownThread.mReboot) {
                        PowerManagerService.lowLevelReboot(ShutdownThread.mReason);
                    } else {
                        PowerManagerService.lowLevelShutdown(ShutdownThread.mReason);
                    }
                }
            }
        }, 10000);
        this.mActionDone = false;
        Intent intent = new Intent("android.intent.action.ACTION_SHUTDOWN");
        intent.addFlags(285212672);
        this.mContext.sendOrderedBroadcastAsUser(intent, UserHandle.ALL, null, br, this.mHandler, 0, null, null);
        long endTime = SystemClock.elapsedRealtime() + FaceDaemonWrapper.TIMEOUT_FACED_BINDERCALL_CHECK;
        synchronized (this.mActionDoneSync) {
            while (!this.mActionDone) {
                delay = endTime - SystemClock.elapsedRealtime();
                if (delay <= 0) {
                    Log.w(TAG, "Shutdown broadcast timed out");
                    break;
                }
                if (mRebootHasProgressBar) {
                    sInstance.setRebootProgress((int) (((((double) (FaceDaemonWrapper.TIMEOUT_FACED_BINDERCALL_CHECK - delay)) * 1.0d) * 2.0d) / 5000.0d), null);
                }
                try {
                    this.mActionDoneSync.wait(Math.min(delay, 100));
                } catch (InterruptedException e) {
                }
            }
        }
        if (mRebootHasProgressBar) {
            sInstance.setRebootProgress(2, null);
        }
        shutdownTimingLog.traceEnd();
        metricEnded(METRIC_SEND_BROADCAST);
        Log.i(TAG, "Shutting down activity manager...");
        shutdownTimingLog.traceBegin("ShutdownActivityManager");
        metricStarted(METRIC_AM);
        IActivityManager am = IActivityManager.Stub.asInterface(ServiceManager.checkService(OppoAppStartupManager.TYPE_ACTIVITY));
        if (am != null) {
            try {
                am.shutdown(5000);
            } catch (RemoteException e2) {
            }
        }
        if (mRebootHasProgressBar) {
            sInstance.setRebootProgress(4, null);
        }
        shutdownTimingLog.traceEnd();
        metricEnded(METRIC_AM);
        Log.i(TAG, "Shutting down package manager...");
        shutdownTimingLog.traceBegin("ShutdownPackageManager");
        metricStarted(METRIC_PM);
        PackageManagerService pm = (PackageManagerService) ServiceManager.getService("package");
        if (pm != null) {
            pm.shutdown();
        }
        if (mRebootHasProgressBar) {
            sInstance.setRebootProgress(6, null);
        }
        shutdownTimingLog.traceEnd();
        metricEnded(METRIC_PM);
        shutdownTimingLog.traceBegin("ShutdownRadios");
        metricStarted(METRIC_RADIOS);
        shutdownRadios(OppoBrightUtils.HIGH_BRIGHTNESS_LUX_STEP);
        if (mRebootHasProgressBar) {
            sInstance.setRebootProgress(18, null);
        }
        shutdownTimingLog.traceEnd();
        metricEnded(METRIC_RADIOS);
        Log.i(TAG, "Shutting down OppoUsageService");
        try {
            IOppoUsageService usageService = IOppoUsageService.Stub.asInterface(ServiceManager.getService("usage"));
            if (usageService != null) {
                usageService.shutDown();
            }
        } catch (Exception e3) {
            Log.w(TAG, "Shutting down OppoUsageService failed!", e3);
        }
        AnonymousClass7 anonymousClass7 = new IStorageShutdownObserver.Stub() {
            public void onShutDownComplete(int statusCode) throws RemoteException {
                Log.w(ShutdownThread.TAG, "Result code " + statusCode + " from StorageManagerService.shutdown");
                ShutdownThread.this.actionDone();
            }
        };
        Log.i(TAG, "Shutting down StorageManagerService");
        shutdownTimingLog.traceBegin("ShutdownStorageManager");
        metricStarted(METRIC_SM);
        this.mActionDone = false;
        long endShutTime = SystemClock.elapsedRealtime() + 10000;
        synchronized (this.mActionDoneSync) {
            try {
                IStorageManager storageManager = IStorageManager.Stub.asInterface(ServiceManager.checkService(OppoProcessManager.RESUME_REASON_MOUNT_STR));
                if (storageManager != null) {
                    storageManager.shutdown(anonymousClass7);
                } else {
                    Log.w(TAG, "StorageManagerService unavailable for shutdown");
                }
            } catch (Exception e32) {
                Log.e(TAG, "Exception during StorageManagerService shutdown", e32);
            }
            while (!this.mActionDone) {
                delay = endShutTime - SystemClock.elapsedRealtime();
                if (delay <= 0) {
                    Log.w(TAG, "StorageManager shutdown wait timed out");
                    break;
                }
                if (mRebootHasProgressBar) {
                    sInstance.setRebootProgress(((int) (((((double) (10000 - delay)) * 1.0d) * 2.0d) / 10000.0d)) + 18, null);
                }
                try {
                    this.mActionDoneSync.wait(Math.min(delay, 100));
                } catch (InterruptedException e4) {
                }
            }
        }
        shutdownTimingLog.traceEnd();
        metricEnded(METRIC_SM);
        if (bIsAnimationtimeForCmcc) {
            Log.i(TAG, "delayForPlayAnimation shutdown...");
            delayForPlayAnimation();
        }
        if (mRebootHasProgressBar) {
            sInstance.setRebootProgress(20, null);
            uncrypt();
        }
        try {
            Stub.asInterface(ServiceManager.checkService(OppoProcessManager.RESUME_REASON_VISIBLE_WINDOW_STR)).setShowLockForBootAnimation(false);
        } catch (Exception e322) {
            e322.printStackTrace();
        }
        shutdownTimingLog.traceEnd();
        metricEnded(METRIC_SYSTEM_SERVER);
        saveMetrics(mReboot);
        rebootOrShutdown(this.mContext, mReboot, mReason);
        return;
    }

    private static TimingsTraceLog newTimingsLog() {
        return new TimingsTraceLog("ShutdownTiming", 524288);
    }

    private static void metricStarted(String metricKey) {
        synchronized (TRON_METRICS) {
            TRON_METRICS.put(metricKey, Long.valueOf(SystemClock.elapsedRealtime() * -1));
        }
    }

    private static void metricEnded(String metricKey) {
        synchronized (TRON_METRICS) {
            TRON_METRICS.put(metricKey, Long.valueOf(SystemClock.elapsedRealtime() + ((Long) TRON_METRICS.get(metricKey)).longValue()));
        }
    }

    private void setRebootProgress(final int progress, final CharSequence message) {
        this.mHandler.post(new Runnable() {
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

    private void shutdownRadios(int timeout) {
        final long endTime = SystemClock.elapsedRealtime() + ((long) timeout);
        final boolean[] done = new boolean[1];
        final int i = timeout;
        Thread t = new Thread() {
            /* JADX WARNING: Removed duplicated region for block: B:35:0x00e6  */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void run() {
                boolean nfcOff;
                boolean bluetoothReadyForShutdown;
                int radioOff;
                TimingsTraceLog shutdownTimingsTraceLog = ShutdownThread.newTimingsLog();
                INfcAdapter nfc = INfcAdapter.Stub.asInterface(ServiceManager.checkService("nfc"));
                ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
                IBluetoothManager bluetooth = IBluetoothManager.Stub.asInterface(ServiceManager.checkService("bluetooth_manager"));
                if (nfc != null) {
                    try {
                        nfcOff = nfc.getState() == 1;
                    } catch (RemoteException ex) {
                        Log.e(ShutdownThread.TAG, "RemoteException during NFC shutdown", ex);
                        nfcOff = true;
                    }
                } else {
                    nfcOff = true;
                }
                if (!nfcOff) {
                    Log.w(ShutdownThread.TAG, "Turning off NFC...");
                    ShutdownThread.metricStarted(ShutdownThread.METRIC_NFC);
                    nfc.disable(false);
                }
                if (bluetooth != null) {
                    try {
                        bluetoothReadyForShutdown = bluetooth.getState() == 10;
                    } catch (RemoteException ex2) {
                        Log.e(ShutdownThread.TAG, "RemoteException during bluetooth shutdown", ex2);
                        bluetoothReadyForShutdown = true;
                    }
                } else {
                    bluetoothReadyForShutdown = true;
                }
                if (!bluetoothReadyForShutdown) {
                    Log.w(ShutdownThread.TAG, "Disabling Bluetooth...");
                    ShutdownThread.metricStarted(ShutdownThread.METRIC_BT);
                    bluetooth.disable(ShutdownThread.this.mContext.getPackageName(), false);
                }
                if (phone != null) {
                    try {
                        radioOff = phone.needMobileRadioShutdown() ^ 1;
                    } catch (RemoteException ex22) {
                        Log.e(ShutdownThread.TAG, "RemoteException during radio shutdown", ex22);
                        radioOff = 1;
                    }
                } else {
                    radioOff = 1;
                }
                if (radioOff == 0) {
                    Log.w(ShutdownThread.TAG, "Turning off cellular radios...");
                    ShutdownThread.metricStarted(ShutdownThread.METRIC_RADIO);
                    phone.shutdownMobileRadios();
                }
                Log.i(ShutdownThread.TAG, "Waiting for NFC, Bluetooth and Radio...");
                long j = endTime;
                long elapsedRealtime = SystemClock.elapsedRealtime();
                while (true) {
                    long delay = j - elapsedRealtime;
                    if (delay > 0) {
                        if (ShutdownThread.mRebootHasProgressBar) {
                            ShutdownThread.sInstance.setRebootProgress(((int) (((((double) (((long) i) - delay)) * 1.0d) * 12.0d) / ((double) i))) + 6, null);
                        }
                        if (!bluetoothReadyForShutdown) {
                            try {
                                if (bluetooth.getState() == 10 || bluetooth.getState() == 16) {
                                    bluetoothReadyForShutdown = true;
                                    if (bluetoothReadyForShutdown) {
                                        Log.i(ShutdownThread.TAG, "Bluetooth turned off.");
                                        ShutdownThread.metricEnded(ShutdownThread.METRIC_BT);
                                        shutdownTimingsTraceLog.logDuration("ShutdownBt", ((Long) ShutdownThread.TRON_METRICS.get(ShutdownThread.METRIC_BT)).longValue());
                                    }
                                } else {
                                    bluetoothReadyForShutdown = bluetooth.getState() == 15;
                                    if (bluetoothReadyForShutdown) {
                                    }
                                }
                            } catch (RemoteException ex222) {
                                Log.e(ShutdownThread.TAG, "RemoteException during bluetooth shutdown", ex222);
                                bluetoothReadyForShutdown = true;
                            }
                        }
                        if (radioOff == 0) {
                            try {
                                radioOff = phone.needMobileRadioShutdown() ^ 1;
                            } catch (RemoteException ex2222) {
                                Log.e(ShutdownThread.TAG, "RemoteException during radio shutdown", ex2222);
                                radioOff = 1;
                            }
                            if (radioOff != 0) {
                                Log.i(ShutdownThread.TAG, "Radio turned off.");
                                ShutdownThread.metricEnded(ShutdownThread.METRIC_RADIO);
                                shutdownTimingsTraceLog.logDuration("ShutdownRadio", ((Long) ShutdownThread.TRON_METRICS.get(ShutdownThread.METRIC_RADIO)).longValue());
                            }
                        }
                        if (!nfcOff) {
                            try {
                                nfcOff = nfc.getState() == 1;
                            } catch (RemoteException ex22222) {
                                Log.e(ShutdownThread.TAG, "RemoteException during NFC shutdown", ex22222);
                                nfcOff = true;
                            }
                            if (nfcOff) {
                                Log.i(ShutdownThread.TAG, "NFC turned off.");
                                ShutdownThread.metricEnded(ShutdownThread.METRIC_NFC);
                                shutdownTimingsTraceLog.logDuration("ShutdownNfc", ((Long) ShutdownThread.TRON_METRICS.get(ShutdownThread.METRIC_NFC)).longValue());
                            }
                        }
                        if (radioOff != 0 && bluetoothReadyForShutdown && nfcOff) {
                            Log.i(ShutdownThread.TAG, "NFC, Radio and Bluetooth shutdown complete.");
                            done[0] = true;
                            return;
                        }
                        SystemClock.sleep(100);
                        j = endTime;
                        elapsedRealtime = SystemClock.elapsedRealtime();
                    } else {
                        return;
                    }
                }
            }
        };
        t.start();
        try {
            t.join((long) timeout);
        } catch (InterruptedException e) {
        }
        if (!done[0]) {
            Log.w(TAG, "Timed out waiting for NFC, Radio and Bluetooth shutdown.");
        }
    }

    public static void rebootOrShutdown(Context context, boolean reboot, String reason) {
        String subsysProp = SystemProperties.get("vendor.peripheral.shutdown_critical_list", "ERROR");
        if (!subsysProp.equals("ERROR")) {
            boolean okToShutdown;
            int i;
            Log.i(TAG, "Shutdown critical subsyslist is :" + subsysProp + ": ");
            Log.i(TAG, "Waiting for a maximum of 10000ms");
            String[] subsysList = subsysProp.split(" ");
            int wait_count = 0;
            do {
                okToShutdown = true;
                for (String str : subsysList) {
                    if (SystemProperties.get("vendor.peripheral." + str + ".state", "ERROR").equals("ONLINE")) {
                        okToShutdown = false;
                    }
                }
                if (!okToShutdown) {
                    SystemClock.sleep(100);
                    wait_count++;
                }
                if (okToShutdown) {
                    break;
                }
            } while (wait_count < 100);
            if (okToShutdown) {
                Log.i(TAG, "Vendor subsystem(s) shutdown successful");
            } else {
                for (i = 0; i < subsysList.length; i++) {
                    if (SystemProperties.get("vendor.peripheral." + subsysList[i] + ".state", "ERROR").equals("ONLINE")) {
                        Log.w(TAG, "Subsystem " + subsysList[i] + "did not shut down within timeout");
                    }
                }
            }
        }
        if (reboot) {
            Log.i(TAG, "Rebooting, reason: " + reason);
            PowerManagerService.lowLevelReboot(reason);
            Log.e(TAG, "Reboot failed, will attempt shutdown instead");
            reason = null;
        } else if (context != null) {
            try {
                new SystemVibrator(context).vibrate(500, VIBRATION_ATTRIBUTES);
            } catch (Exception e) {
                Log.w(TAG, "Failed to vibrate during shutdown.", e);
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e2) {
            }
        }
        Log.i(TAG, "Performing low-level shutdown...");
        oppoPanelShutdown();
        if (hasVibrate.compareAndSet(false, true)) {
            PowerManagerService.lowLevelShutdown(reason);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:57:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x009e  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00b6 A:{SYNTHETIC, Splitter: B:36:0x00b6} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x00c9 A:{Catch:{ IOException -> 0x00bc }} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00bb A:{SYNTHETIC, Splitter: B:39:0x00bb} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void saveMetrics(boolean reboot) {
        IOException e;
        Throwable th;
        StringBuilder metricValue = new StringBuilder();
        metricValue.append("reboot:");
        metricValue.append(reboot ? "y" : OppoCrashClearManager.CRASH_COUNT);
        int metricsSize = TRON_METRICS.size();
        for (int i = 0; i < metricsSize; i++) {
            String name = (String) TRON_METRICS.keyAt(i);
            long value = ((Long) TRON_METRICS.valueAt(i)).longValue();
            if (value < 0) {
                Log.e(TAG, "metricEnded wasn't called for " + name);
            } else {
                metricValue.append(',').append(name).append(':').append(value);
            }
        }
        File tmp = new File("/data/system/shutdown-metrics.tmp");
        boolean saved = false;
        Throwable th2 = null;
        FileOutputStream fos = null;
        try {
            FileOutputStream fos2 = new FileOutputStream(tmp);
            try {
                fos2.write(metricValue.toString().getBytes(StandardCharsets.UTF_8));
                saved = true;
                if (fos2 != null) {
                    try {
                        fos2.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    try {
                        throw th2;
                    } catch (IOException e2) {
                        e = e2;
                        fos = fos2;
                    }
                } else {
                    if (!saved) {
                        tmp.renameTo(new File("/data/system/shutdown-metrics.txt"));
                    }
                }
            } catch (Throwable th4) {
                th = th4;
                fos = fos2;
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (Throwable th5) {
                        if (th2 == null) {
                            th2 = th5;
                        } else if (th2 != th5) {
                            th2.addSuppressed(th5);
                        }
                    }
                }
                if (th2 == null) {
                    try {
                        throw th2;
                    } catch (IOException e3) {
                        e = e3;
                        Log.e(TAG, "Cannot save shutdown metrics", e);
                        if (!saved) {
                        }
                    }
                } else {
                    throw th;
                }
            }
        } catch (Throwable th6) {
            th = th6;
            if (fos != null) {
            }
            if (th2 == null) {
            }
        }
    }

    private void uncrypt() {
        Log.i(TAG, "Calling uncrypt and monitoring the progress...");
        final ProgressListener progressListener = new ProgressListener() {
            public void onProgress(int status) {
                if (status >= 0 && status < 100) {
                    ShutdownThread.sInstance.setRebootProgress(((int) ((((double) status) * 80.0d) / 100.0d)) + 20, ShutdownThread.this.mContext.getText(17040741));
                } else if (status == 100) {
                    ShutdownThread.sInstance.setRebootProgress(status, ShutdownThread.this.mContext.getText(17040743));
                }
            }
        };
        final boolean[] done = new boolean[]{false};
        Thread t = new Thread() {
            public void run() {
                RecoverySystem rs = (RecoverySystem) ShutdownThread.this.mContext.getSystemService("recovery");
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
                FileUtils.stringToFile(RecoverySystem.UNCRYPT_STATUS_FILE, String.format("uncrypt_time: %d\nuncrypt_error: %d\n", new Object[]{Integer.valueOf(900), Integer.valueOf(100)}));
            } catch (IOException e2) {
                Log.e(TAG, "Failed to write timeout message to uncrypt status", e2);
            }
        }
    }

    private static void resetBrightnessAdj(Context context) {
        OppoBrightUtils oppoBrightUtils = OppoBrightUtils.getInstance();
        if (OppoBrightUtils.mManualBrightness == 0) {
            OppoBrightUtils.mManualBrightness = OppoBrightUtils.mBrightnessOverrideAdj;
            OppoBrightUtils.mManulAtAmbientLux = OppoBrightUtils.mBrightnessOverrideAmbientLux;
        }
        OppoBrightUtils.mManualBrightnessBackup = OppoBrightUtils.mManualBrightness;
        OppoBrightUtils.mManualAmbientLuxBackup = OppoBrightUtils.mManulAtAmbientLux;
        System.putFloatForUser(context.getContentResolver(), "screen_auto_brightness_adj", (float) OppoBrightUtils.mManualBrightness, -2);
        System.putFloatForUser(context.getContentResolver(), "autobrightness_manul_ambient", OppoBrightUtils.mManulAtAmbientLux, -2);
        OppoBrightUtils.mManualBrightness = 0;
        OppoBrightUtils.mManulAtAmbientLux = OppoBrightUtils.MIN_LUX_LIMITI;
        OppoBrightUtils.mSaveBrightnessByShutdown = true;
        Log.d(TAG, "brightness backup = " + OppoBrightUtils.mManualBrightnessBackup);
    }

    private static void oppoPanelShutdown() {
        OppoBrightUtils.getInstance().writeShutdownFlagNodeValue();
    }
}
