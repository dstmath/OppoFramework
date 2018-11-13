package com.android.server.power;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
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
import android.hardware.fingerprint.IFingerprintService;
import android.hardware.fingerprint.IFingerprintService.Stub;
import android.media.AudioAttributes;
import android.media.AudioSystem;
import android.net.ConnectivityManager;
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
import android.os.storage.IMountService;
import android.os.storage.IMountShutdownObserver;
import android.provider.Settings.System;
import android.util.Log;
import android.view.IWindowManager;
import com.android.internal.app.ShutdownManager;
import com.android.internal.telephony.ITelephony;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.AgingCriticalEvent;
import com.android.server.LocationManagerService;
import com.android.server.am.OppoProcessManager;
import com.android.server.display.OppoBrightUtils;
import com.android.server.oppo.IElsaManager;
import com.android.server.pm.PackageManagerService;
import com.mediatek.common.MPlugin;
import com.mediatek.common.bootanim.IBootAnimExt;
import java.io.File;
import java.io.IOException;
import java.lang.Thread.State;

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
public final class ShutdownThread extends Thread {
    private static final String ACTION_PRE_SHUTDOWN = "android.intent.action.ACTION_PRE_SHUTDOWN";
    private static final String ACTION_SHUTDOWN_IPO = "android.intent.action.ACTION_SHUTDOWN_IPO";
    private static final int ACTIVITY_MANAGER_STOP_PERCENT = 4;
    public static final String AUDIT_SAFEMODE_PROPERTY = "persist.sys.audit_safemode";
    private static final int BROADCAST_STOP_PERCENT = 2;
    private static final int IPO_SHUTDOWN_FLOW = 1;
    private static final int MAX_BROADCAST_TIME = 5000;
    private static final int MAX_RADIO_WAIT_TIME = 2500;
    private static final int MAX_SHUTDOWN_WAIT_TIME = 10000;
    private static final int MAX_UNCRYPT_WAIT_TIME = 900000;
    private static final int MIN_SHUTDOWN_ANIMATION_PLAY_TIME = 3000;
    private static final int MOUNT_SERVICE_STOP_PERCENT = 20;
    private static final int NORMAL_SHUTDOWN_FLOW = 0;
    private static final int PACKAGE_MANAGER_STOP_PERCENT = 6;
    private static final int PHONE_STATE_POLL_SLEEP_MSEC = 100;
    private static final int RADIO_STOP_PERCENT = 18;
    public static final String REBOOT_SAFEMODE_PROPERTY = "persist.sys.safemode";
    public static final String RO_SAFEMODE_PROPERTY = "ro.sys.safemode";
    public static final String SHUTDOWN_ACTION_PROPERTY = "sys.shutdown.requested";
    private static final int SHUTDOWN_VIBRATE_MS = 250;
    private static final String TAG = "ShutdownThread";
    private static final AudioAttributes VIBRATION_ATTRIBUTES = null;
    private static boolean bConfirmForAnimation = false;
    private static boolean bPlayaudio = false;
    private static long beginAnimationTime = 0;
    private static final String changeToNormalMessage = "change shutdown flow from ipo to normal";
    private static String command = null;
    private static long endAnimationTime = 0;
    private static Runnable mDelayDim = null;
    private static boolean mEnableAnimating = false;
    private static final Object mEnableAnimatingSync = null;
    private static IBootAnimExt mIBootAnim = null;
    private static String mReason = null;
    private static boolean mReboot = false;
    private static boolean mRebootHasProgressBar = false;
    private static boolean mRebootSafeMode = false;
    private static Object mShutdownThreadSync = null;
    private static final boolean mSpew = true;
    private static AlertDialog sConfirmDialog;
    private static final ShutdownThread sInstance = null;
    private static boolean sIsStarted;
    private static Object sIsStartedGuard;
    private boolean mActionDone;
    private final Object mActionDoneSync;
    private Context mContext;
    private WakeLock mCpuWakeLock;
    private Handler mHandler;
    private PowerManager mPowerManager;
    private ProgressDialog mProgressDialog;
    private WakeLock mScreenWakeLock;
    private int mShutdownFlow;
    private ShutdownManager mShutdownManager;

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

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.power.ShutdownThread.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.power.ShutdownThread.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.power.ShutdownThread.<clinit>():void");
    }

    private ShutdownThread() {
        this.mActionDoneSync = new Object();
        this.mShutdownManager = ShutdownManager.getInstance();
    }

    public static void EnableAnimating(boolean enable) {
        synchronized (mEnableAnimatingSync) {
            mEnableAnimating = enable;
        }
    }

    public static void shutdown(Context context, String reason, boolean confirm) {
        mReboot = false;
        mRebootSafeMode = false;
        mReason = reason;
        Log.d(TAG, "!!! Request to shutdown !!!");
        for (StackTraceElement element : new Throwable().getStackTrace()) {
            Log.d(TAG, "    |----" + element.toString());
        }
        resetBrightnessAdj(context);
        if (SystemProperties.getBoolean("ro.monkey", false)) {
            Log.d(TAG, "Cannot request to shutdown when Monkey is running, returning.");
            return;
        }
        Log.v(TAG, "oppomanager sync cache to emmc");
        OppoManager.syncCacheToEmmc();
        shutdownInner(context, confirm);
    }

    /* JADX WARNING: Missing block: B:17:0x006e, code:
            r1 = r10.getResources().getInteger(17694798);
     */
    /* JADX WARNING: Missing block: B:18:0x007b, code:
            if (mRebootSafeMode == false) goto L_0x00ff;
     */
    /* JADX WARNING: Missing block: B:19:0x007d, code:
            r2 = 17039658;
     */
    /* JADX WARNING: Missing block: B:20:0x0080, code:
            android.util.Log.d(TAG, "Notifying thread to start shutdown longPressBehavior=" + r1);
     */
    /* JADX WARNING: Missing block: B:21:0x009a, code:
            if (r11 == false) goto L_0x010f;
     */
    /* JADX WARNING: Missing block: B:22:0x009c, code:
            r0 = new com.android.server.power.ShutdownThread.CloseDialogReceiver(r10);
     */
    /* JADX WARNING: Missing block: B:23:0x00a3, code:
            if (sConfirmDialog == null) goto L_0x00aa;
     */
    /* JADX WARNING: Missing block: B:24:0x00a5, code:
            sConfirmDialog.dismiss();
     */
    /* JADX WARNING: Missing block: B:25:0x00aa, code:
            bConfirmForAnimation = r11;
            android.util.Log.d(TAG, "PowerOff dialog doesn't exist. Create it first");
            r4 = new android.app.AlertDialog.Builder(r10);
     */
    /* JADX WARNING: Missing block: B:26:0x00bc, code:
            if (mRebootSafeMode == false) goto L_0x010b;
     */
    /* JADX WARNING: Missing block: B:27:0x00be, code:
            r3 = 17039657;
     */
    /* JADX WARNING: Missing block: B:28:0x00c1, code:
            sConfirmDialog = r4.setTitle(r3).setMessage(r2).setPositiveButton(17039379, new com.android.server.power.ShutdownThread.AnonymousClass2()).setNegativeButton(17039369, null).create();
            r0.dialog = sConfirmDialog;
            sConfirmDialog.setOnDismissListener(r0);
            sConfirmDialog.getWindow().setType(2009);
            sConfirmDialog.show();
     */
    /* JADX WARNING: Missing block: B:29:0x00fb, code:
            return;
     */
    /* JADX WARNING: Missing block: B:33:0x00ff, code:
            if (r1 != 2) goto L_0x0106;
     */
    /* JADX WARNING: Missing block: B:34:0x0101, code:
            r2 = 17039656;
     */
    /* JADX WARNING: Missing block: B:35:0x0106, code:
            r2 = 17039655;
     */
    /* JADX WARNING: Missing block: B:36:0x010b, code:
            r3 = 17039644;
     */
    /* JADX WARNING: Missing block: B:37:0x010f, code:
            beginShutdownSequence(r10);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static void shutdownInner(final Context context, boolean confirm) {
        AgingCriticalEvent instance = AgingCriticalEvent.getInstance();
        String str = AgingCriticalEvent.EVENT_USER_POWER_OFF;
        String[] strArr = new String[2];
        strArr[0] = mReboot ? "reboot, reason:" + mReason + " mRebootSafeMode:" + mRebootSafeMode : "shutdown, reason:" + mReason;
        strArr[1] = confirm ? "confirmed" : "noNeedConfirm";
        instance.writeEvent(str, strArr);
        synchronized (sIsStartedGuard) {
            if (sIsStarted) {
                Log.d(TAG, "Request to shutdown already running, returning.");
            }
        }
    }

    public static void reboot(Context context, String reason, boolean confirm) {
        int i = 0;
        mReboot = true;
        mRebootSafeMode = false;
        mRebootHasProgressBar = false;
        mReason = reason;
        resetBrightnessAdj(context);
        Log.d(TAG, "reboot");
        Log.v(TAG, "oppomanager sync cache to emmc");
        OppoManager.syncCacheToEmmc();
        if ("sau".equals(reason) || "silence".equals(reason)) {
            SystemProperties.set("sys.bootsilence", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
        }
        StackTraceElement[] stack = new Throwable().getStackTrace();
        int length = stack.length;
        while (i < length) {
            Log.d(TAG, "     |----" + stack[i].toString());
            i++;
        }
        shutdownInner(context, confirm);
    }

    public static void rebootSafeMode(Context context, boolean confirm) {
        if (!((UserManager) context.getSystemService("user")).hasUserRestriction("no_safe_boot")) {
            resetBrightnessAdj(context);
            mReboot = true;
            mRebootSafeMode = true;
            mRebootHasProgressBar = false;
            mReason = null;
            Log.d(TAG, "rebootSafeMode");
            shutdownInner(context, confirm);
        }
    }

    private static boolean configShutdownAnimation(Context context) {
        boolean mShutOffAnimation = false;
        PowerManager pm = (PowerManager) context.getSystemService("power");
        if (bConfirmForAnimation || pm.isScreenOn()) {
            bPlayaudio = true;
        } else {
            bPlayaudio = false;
        }
        try {
            String cust = SystemProperties.get("persist.operator.optr");
            if (mIBootAnim == null) {
                mIBootAnim = (IBootAnimExt) MPlugin.createInstance(IBootAnimExt.class.getName(), context);
            }
            if (cust == null || !cust.equals("CUST")) {
                return mIBootAnim.isCustBootAnim();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return mShutOffAnimation;
        }
    }

    private static int getScreenTurnOffTime(Context context) {
        try {
            if (mIBootAnim == null) {
                mIBootAnim = (IBootAnimExt) MPlugin.createInstance(IBootAnimExt.class.getName(), context);
            }
            Log.d(TAG, "IBootAnim get screenTurnOffTime : " + 3000);
            return 3000;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:88:0x020c  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0164  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0147 A:{Catch:{ Exception -> 0x01fd }} */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x0155  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0164  */
    /* JADX WARNING: Removed duplicated region for block: B:88:0x020c  */
    /* JADX WARNING: Missing block: B:12:0x0016, code:
            r0 = (android.media.AudioManager) r14.getSystemService("audio");
            android.util.Log.i(TAG, "mute audios...");
            r5 = 0;
     */
    /* JADX WARNING: Missing block: B:14:0x002d, code:
            if (r5 >= android.media.AudioSystem.getNumStreamTypes()) goto L_0x0047;
     */
    /* JADX WARNING: Missing block: B:16:0x0030, code:
            if (r5 == 1) goto L_0x0036;
     */
    /* JADX WARNING: Missing block: B:17:0x0032, code:
            r0.setStreamMute(r5, true);
     */
    /* JADX WARNING: Missing block: B:19:0x003a, code:
            if (r0.isSilentMode() != false) goto L_0x0041;
     */
    /* JADX WARNING: Missing block: B:20:0x003c, code:
            r0.setStreamMute(1, false);
     */
    /* JADX WARNING: Missing block: B:21:0x0041, code:
            r5 = r5 + 1;
     */
    /* JADX WARNING: Missing block: B:25:0x0047, code:
            r7 = new android.app.ProgressDialog(r14);
     */
    /* JADX WARNING: Missing block: B:26:0x0055, code:
            if ("recovery-update".equals(mReason) == false) goto L_0x0190;
     */
    /* JADX WARNING: Missing block: B:28:0x005d, code:
            if (android.os.RecoverySystem.UNCRYPT_PACKAGE_FILE.exists() == false) goto L_0x017d;
     */
    /* JADX WARNING: Missing block: B:30:0x0065, code:
            if (android.os.RecoverySystem.BLOCK_MAP_FILE.exists() == false) goto L_0x017a;
     */
    /* JADX WARNING: Missing block: B:31:0x0067, code:
            r9 = false;
     */
    /* JADX WARNING: Missing block: B:32:0x0068, code:
            mRebootHasProgressBar = r9;
            r7.setTitle(r14.getText(17039648));
     */
    /* JADX WARNING: Missing block: B:33:0x0076, code:
            if (mRebootHasProgressBar == false) goto L_0x0180;
     */
    /* JADX WARNING: Missing block: B:34:0x0078, code:
            r7.setMax(100);
            r7.setProgress(0);
            r7.setIndeterminate(false);
            r7.setProgressNumberFormat(null);
            r7.setProgressStyle(1);
            r7.setMessage(r14.getText(17039649));
     */
    /* JADX WARNING: Missing block: B:35:0x0097, code:
            r7.setCancelable(false);
            r7.getWindow().setType(2009);
            sInstance.mContext = r14;
            sInstance.mPowerManager = (android.os.PowerManager) r14.getSystemService("power");
            sInstance.mHandler = new com.android.server.power.ShutdownThread.AnonymousClass3();
            beginAnimationTime = 0;
            r8 = getScreenTurnOffTime(r14);
            r10 = mEnableAnimatingSync;
     */
    /* JADX WARNING: Missing block: B:36:0x00c9, code:
            monitor-enter(r10);
     */
    /* JADX WARNING: Missing block: B:39:0x00cc, code:
            if (mEnableAnimating == false) goto L_0x00e6;
     */
    /* JADX WARNING: Missing block: B:40:0x00ce, code:
            if (true == false) goto L_0x01cf;
     */
    /* JADX WARNING: Missing block: B:41:0x00d0, code:
            android.util.Log.d(TAG, "mIBootAnim.isCustBootAnim() is true");
            bootanimCust(r14);
     */
    /* JADX WARNING: Missing block: B:42:0x00dc, code:
            sInstance.mHandler.postDelayed(mDelayDim, (long) r8);
     */
    /* JADX WARNING: Missing block: B:43:0x00e6, code:
            monitor-exit(r10);
     */
    /* JADX WARNING: Missing block: B:44:0x00e7, code:
            sInstance.mCpuWakeLock = null;
     */
    /* JADX WARNING: Missing block: B:46:?, code:
            sInstance.mCpuWakeLock = sInstance.mPowerManager.newWakeLock(1, "ShutdownThread-cpu");
            sInstance.mCpuWakeLock.setReferenceCounted(false);
            sInstance.mCpuWakeLock.acquire();
     */
    /* JADX WARNING: Missing block: B:67:0x017a, code:
            r9 = true;
     */
    /* JADX WARNING: Missing block: B:68:0x017d, code:
            r9 = false;
     */
    /* JADX WARNING: Missing block: B:69:0x0180, code:
            r7.setIndeterminate(true);
            r7.setMessage(r14.getText(17039651));
     */
    /* JADX WARNING: Missing block: B:71:0x0199, code:
            if ("recovery".equals(mReason) == false) goto L_0x01b5;
     */
    /* JADX WARNING: Missing block: B:72:0x019b, code:
            r7.setTitle(r14.getText(17039652));
            r7.setMessage(r14.getText(17039653));
            r7.setIndeterminate(true);
     */
    /* JADX WARNING: Missing block: B:73:0x01b5, code:
            r7.setTitle(r14.getText(17039644));
            r7.setMessage(r14.getText(17039654));
            r7.setIndeterminate(true);
     */
    /* JADX WARNING: Missing block: B:75:?, code:
            r7.show();
            sInstance.mProgressDialog = r7;
     */
    /* JADX WARNING: Missing block: B:79:0x01db, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:80:0x01dc, code:
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
        try {
            fingerprintService = Stub.asInterface(ServiceManager.getService("fingerprint"));
            if (fingerprintService != null) {
                fingerprintService.setFingerprintEnabled(false);
            }
        } catch (Exception e) {
            Log.w(TAG, "FingerprintServie disable failed!", e);
        }
        if (sInstance.getState() != State.NEW && !sInstance.isAlive()) {
            sInstance.start();
        } else if (sInstance.mShutdownFlow == 1) {
            Log.d(TAG, "ShutdownThread exists already");
            checkShutdownFlow();
            synchronized (mShutdownThreadSync) {
                mShutdownThreadSync.notify();
            }
        } else {
            Log.e(TAG, "Thread state is not normal! froce to shutdown!");
            delayForPlayAnimation();
            sInstance.mPowerManager.goToSleep(SystemClock.uptimeMillis(), 7, 0);
            PowerManagerService.lowLevelShutdown(mReason);
        }
        if (sInstance.getState() != State.NEW) {
        }
        if (sInstance.mShutdownFlow == 1) {
        }
        sInstance.mScreenWakeLock = null;
        if (sInstance.mPowerManager.isScreenOn()) {
            try {
                sInstance.mScreenWakeLock = sInstance.mPowerManager.newWakeLock(26, "ShutdownThread-screen");
                sInstance.mScreenWakeLock.setReferenceCounted(false);
                sInstance.mScreenWakeLock.acquire();
            } catch (SecurityException e2) {
                Log.w(TAG, "No permission to acquire wake lock", e2);
                sInstance.mScreenWakeLock = null;
            }
        }
        fingerprintService = Stub.asInterface(ServiceManager.getService("fingerprint"));
        if (fingerprintService != null) {
        }
        if (sInstance.getState() != State.NEW) {
        }
        if (sInstance.mShutdownFlow == 1) {
        }
    }

    private static void bootanimCust(Context context) {
        IWindowManager wm;
        SystemProperties.set("service.shutanim.running", "0");
        Log.i(TAG, "set service.shutanim.running to 0");
        try {
            boolean isRotaionEnabled;
            if (System.getInt(context.getContentResolver(), "accelerometer_rotation", 1) != 0) {
                isRotaionEnabled = true;
            } else {
                isRotaionEnabled = false;
            }
            if (isRotaionEnabled) {
                wm = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
                if (wm != null) {
                    wm.freezeRotation(0);
                }
                System.putInt(context.getContentResolver(), "accelerometer_rotation", 0);
                System.putInt(context.getContentResolver(), "accelerometer_rotation_restore", 1);
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "check Rotation: context object is null when get Rotation");
        } catch (RemoteException e2) {
            e2.printStackTrace();
        }
        beginAnimationTime = SystemClock.elapsedRealtime() + 3000;
        try {
            wm = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
            if (wm != null) {
                wm.setEventDispatching(false);
            }
        } catch (RemoteException e22) {
            e22.printStackTrace();
        }
        startBootAnimation();
    }

    private static void startBootAnimation() {
        Log.d(TAG, "Set 'service.bootanim.exit' = 0).");
        SystemProperties.set("service.bootanim.exit", "0");
        if (bPlayaudio) {
            SystemProperties.set("ctl.start", "banim_shutmp3");
            Log.d(TAG, "bootanim:shut mp3");
            return;
        }
        SystemProperties.set("ctl.start", "banim_shutnomp3");
        Log.d(TAG, "bootanim:shut nomp3");
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
                    if (endAnimationTime < 400) {
                        Thread.currentThread();
                        Thread.sleep(endAnimationTime);
                    } else {
                        Thread.currentThread();
                        Thread.sleep(400);
                    }
                } catch (InterruptedException e) {
                    Log.e(TAG, "Shutdown stop bootanimation Thread.currentThread().sleep exception!");
                }
            }
        }
    }

    private static void checkShutdownFlow() {
        String IPODisableProp = SystemProperties.get("sys.ipo.disable");
        boolean isIPOEnabled = !IPODisableProp.equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
        boolean isIPOsupport = SystemProperties.get("ro.mtk_ipo_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
        boolean passIPOEncryptionCondition = checkEncryption();
        boolean isSafeMode = false;
        try {
            IWindowManager wm = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
            if (wm != null) {
                isSafeMode = wm.isSafeModeEnabled();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "checkShutdownFlow: IPO_Support=" + isIPOsupport + " mReboot=" + mReboot + " sys.ipo.disable=" + IPODisableProp + " isSafeMode=" + isSafeMode + " passEncryptionCondition=" + passIPOEncryptionCondition);
        if (isIPOsupport && !mReboot && isIPOEnabled && !isSafeMode && passIPOEncryptionCondition) {
            try {
                isIPOEnabled = System.getInt(sInstance.mContext.getContentResolver(), "ipo_setting", 1) == 1;
                if (!isIPOEnabled) {
                    sInstance.mShutdownFlow = 0;
                } else if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(SystemProperties.get("sys.ipo.battlow"))) {
                    sInstance.mShutdownFlow = 0;
                } else {
                    sInstance.mShutdownFlow = 1;
                }
                Log.d(TAG, "checkShutdownFlow: isIPOEnabled=" + isIPOEnabled + " mShutdownFlow=" + sInstance.mShutdownFlow);
                return;
            } catch (NullPointerException e2) {
                Log.e(TAG, "checkShutdownFlow: fail to get IPO setting");
                sInstance.mShutdownFlow = 0;
                return;
            }
        }
        sInstance.mShutdownFlow = 0;
    }

    private void switchToLauncher() {
        Log.i(TAG, "set launcher as foreground");
        Intent intent1 = new Intent("android.intent.action.MAIN");
        intent1.addCategory("android.intent.category.HOME");
        intent1.setFlags(268435456);
        this.mContext.startActivity(intent1);
    }

    public void run() {
        checkShutdownFlow();
        while (this.mShutdownFlow == 1) {
            this.mShutdownManager.saveStates(this.mContext);
            this.mShutdownManager.enterShutdown(this.mContext);
            switchToLauncher();
            running();
        }
        if (this.mShutdownFlow != 1) {
            this.mShutdownManager.enterShutdown(this.mContext);
            switchToLauncher();
            running();
        }
    }

    private void running() {
        String str;
        long delay;
        command = SystemProperties.get("sys.ipo.pwrdncap");
        BroadcastReceiver br = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                ShutdownThread.this.actionDone();
            }
        };
        StringBuilder append = new StringBuilder().append(mReboot ? LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON : "0");
        if (mReason != null) {
            str = mReason;
        } else {
            str = IElsaManager.EMPTY_PACKAGE;
        }
        SystemProperties.set(SHUTDOWN_ACTION_PROPERTY, append.append(str).toString());
        if (mRebootSafeMode) {
            SystemProperties.set(REBOOT_SAFEMODE_PROPERTY, LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
        }
        Log.i(TAG, "Sending shutdown broadcast...");
        this.mActionDone = false;
        this.mContext.sendBroadcast(new Intent(ACTION_PRE_SHUTDOWN));
        Intent intent = new Intent("android.intent.action.ACTION_SHUTDOWN");
        intent.putExtra("_mode", this.mShutdownFlow);
        intent.addFlags(268435456);
        this.mContext.sendOrderedBroadcastAsUser(intent, UserHandle.ALL, null, br, this.mHandler, 0, null, null);
        long endTime = SystemClock.elapsedRealtime() + 5000;
        synchronized (this.mActionDoneSync) {
            while (!this.mActionDone) {
                delay = endTime - SystemClock.elapsedRealtime();
                if (delay <= 0) {
                    Log.w(TAG, "Shutdown broadcast ACTION_SHUTDOWN timed out");
                    if (this.mShutdownFlow == 1) {
                        Log.d(TAG, "change shutdown flow from ipo to normal: ACTION_SHUTDOWN timeout");
                        this.mShutdownFlow = 0;
                    }
                } else {
                    if (mRebootHasProgressBar) {
                        sInstance.setRebootProgress((int) (((((double) (5000 - delay)) * 1.0d) * 2.0d) / 5000.0d), null);
                    }
                    try {
                        this.mActionDoneSync.wait(Math.min(delay, 100));
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
        if (mRebootHasProgressBar) {
            sInstance.setRebootProgress(2, null);
        }
        if (this.mShutdownFlow == 1) {
            this.mActionDone = false;
            intent = new Intent("android.intent.action.ACTION_SHUTDOWN_IPO");
            intent.addFlags(268435456);
            this.mContext.sendOrderedBroadcast(intent, null, br, this.mHandler, 0, null, null);
            long endTimeIPO = SystemClock.elapsedRealtime() + 5000;
            synchronized (this.mActionDoneSync) {
                while (!this.mActionDone) {
                    delay = endTimeIPO - SystemClock.elapsedRealtime();
                    if (delay <= 0) {
                        Log.w(TAG, "Shutdown broadcast ACTION_SHUTDOWN_IPO timed out");
                        if (this.mShutdownFlow == 1) {
                            Log.d(TAG, "change shutdown flow from ipo to normal: ACTION_SHUTDOWN_IPO timeout");
                            this.mShutdownFlow = 0;
                        }
                    } else {
                        try {
                            this.mActionDoneSync.wait(delay);
                        } catch (InterruptedException e2) {
                        }
                    }
                }
            }
        }
        if (this.mShutdownFlow != 1) {
            Log.i(TAG, "Shutting down activity manager...");
            IActivityManager am = ActivityManagerNative.asInterface(ServiceManager.checkService("activity"));
            if (am != null) {
                try {
                    am.shutdown(MAX_BROADCAST_TIME);
                } catch (RemoteException e3) {
                }
            }
            if (mRebootHasProgressBar) {
                sInstance.setRebootProgress(4, null);
            }
        }
        Log.i(TAG, "Shutting down package manager...");
        PackageManagerService pm = (PackageManagerService) ServiceManager.getService("package");
        if (pm != null) {
            pm.shutdown();
        }
        if (mRebootHasProgressBar) {
            sInstance.setRebootProgress(6, null);
        }
        Log.i(TAG, "Shutting down radios...");
        shutdownRadios(MAX_RADIO_WAIT_TIME);
        if (mRebootHasProgressBar) {
            sInstance.setRebootProgress(18, null);
        }
        Log.i(TAG, "Shutting down OppoUsageService");
        try {
            IOppoUsageService usageService = IOppoUsageService.Stub.asInterface(ServiceManager.getService("usage"));
            if (usageService != null) {
                usageService.shutDown();
            }
        } catch (Throwable e4) {
            Log.w(TAG, "Shutting down OppoUsageService failed!", e4);
        }
        if (this.mShutdownFlow == 1 && (command.equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON) || command.equals("3"))) {
            Log.i(TAG, "bypass MountService!");
        } else {
            IMountShutdownObserver anonymousClass5 = new IMountShutdownObserver.Stub() {
                public void onShutDownComplete(int statusCode) throws RemoteException {
                    Log.w(ShutdownThread.TAG, "Result code " + statusCode + " from MountService.shutdown");
                    if (statusCode < 0) {
                        ShutdownThread.this.mShutdownFlow = 0;
                    }
                    ShutdownThread.this.actionDone();
                }
            };
            Log.i(TAG, "Shutting down MountService");
            this.mActionDone = false;
            long endShutTime = SystemClock.elapsedRealtime() + 10000;
            synchronized (this.mActionDoneSync) {
                try {
                    IMountService mount = IMountService.Stub.asInterface(ServiceManager.checkService(OppoProcessManager.RESUME_REASON_MOUNT_STR));
                    if (mount != null) {
                        mount.shutdown(anonymousClass5);
                    } else {
                        Log.w(TAG, "MountService unavailable for shutdown");
                    }
                } catch (Throwable e42) {
                    Log.e(TAG, "Exception during MountService shutdown", e42);
                }
                while (!this.mActionDone) {
                    delay = endShutTime - SystemClock.elapsedRealtime();
                    if (delay <= 0) {
                        Log.w(TAG, "Shutdown wait timed out");
                        if (this.mShutdownFlow == 1) {
                            Log.d(TAG, "change shutdown flow from ipo to normal: MountService");
                            this.mShutdownFlow = 0;
                        }
                    } else {
                        if (mRebootHasProgressBar) {
                            sInstance.setRebootProgress(((int) (((((double) (10000 - delay)) * 1.0d) * 2.0d) / 10000.0d)) + 18, null);
                        }
                        try {
                            this.mActionDoneSync.wait(Math.min(delay, 100));
                        } catch (InterruptedException e5) {
                        }
                    }
                }
            }
            Log.i(TAG, "MountService shut done...");
        }
        Log.i(TAG, "set service.shutanim.running to 1");
        SystemProperties.set("service.shutanim.running", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
        if (this.mShutdownFlow == 1) {
            if (this.mContext != null) {
                try {
                    new SystemVibrator(this.mContext).vibrate(250, VIBRATION_ATTRIBUTES);
                } catch (Throwable e422) {
                    Log.w(TAG, "Failed to vibrate during shutdown.", e422);
                }
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e6) {
                }
            }
            Log.i(TAG, "Performing ipo low-level shutdown...");
            delayForPlayAnimation();
            if (sInstance.mScreenWakeLock != null && sInstance.mScreenWakeLock.isHeld()) {
                sInstance.mScreenWakeLock.release();
            }
            sInstance.mHandler.removeCallbacks(mDelayDim);
            this.mShutdownManager.shutdown(this.mContext);
            this.mShutdownManager.finishShutdown(this.mContext);
            if (sInstance.mProgressDialog != null) {
                sInstance.mProgressDialog.dismiss();
            } else if (beginAnimationTime > 0) {
                Log.i(TAG, "service.bootanim.exit = 1");
                SystemProperties.set("service.bootanim.exit", LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
            }
            synchronized (sIsStartedGuard) {
                sIsStarted = false;
            }
            sInstance.mPowerManager.wakeUp(SystemClock.uptimeMillis(), "shutdown");
            sInstance.mCpuWakeLock.acquire(2000);
            synchronized (mShutdownThreadSync) {
                try {
                    mShutdownThreadSync.wait();
                } catch (InterruptedException e7) {
                    e7.printStackTrace();
                }
            }
            return;
        }
        if (mRebootHasProgressBar) {
            sInstance.setRebootProgress(20, null);
            uncrypt();
        }
        if ((mReboot && mReason != null && mReason.equals("recovery")) || !mReboot) {
            delayForPlayAnimation();
        }
        sInstance.mPowerManager.goToSleep(SystemClock.uptimeMillis(), 7, 0);
        AudioSystem.setParameters("REBOOT=1");
        rebootOrShutdown(this.mContext, mReboot, mReason);
        return;
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
        boolean bypassRadioOff = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).isNetworkSupported(0) ? this.mShutdownFlow == 1 ? !command.equals("2") ? command.equals("3") : true : false : true;
        final long endTime = SystemClock.elapsedRealtime() + ((long) timeout);
        final boolean[] done = new boolean[1];
        final int i = timeout;
        Thread t = new Thread() {
            /* JADX WARNING: Removed duplicated region for block: B:39:0x00dd A:{LOOP_START, LOOP:0: B:39:0x00dd->B:38:0x00d0, PHI: r1 r2 r6 r8 } */
            /* JADX WARNING: Removed duplicated region for block: B:26:0x0092  */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void run() {
                boolean nfcOff;
                boolean bluetoothOff;
                boolean radioOff;
                long delay;
                Log.w(ShutdownThread.TAG, "task run");
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
                    nfc.disable(false);
                }
                if (bluetooth != null) {
                    try {
                        bluetoothOff = bluetooth.getState() == 10;
                    } catch (RemoteException ex2) {
                        Log.e(ShutdownThread.TAG, "RemoteException during bluetooth shutdown", ex2);
                        bluetoothOff = true;
                    }
                } else {
                    bluetoothOff = true;
                }
                if (!bluetoothOff) {
                    Log.w(ShutdownThread.TAG, "Disabling Bluetooth...");
                    bluetooth.disable(false);
                }
                if (phone != null) {
                    try {
                        if (phone.needMobileRadioShutdown()) {
                            radioOff = false;
                            if (!(radioOff || ShutdownThread.this.mShutdownFlow == 1)) {
                                Log.w(ShutdownThread.TAG, "Turning off cellular radios...");
                                phone.shutdownMobileRadios();
                            }
                            Log.i(ShutdownThread.TAG, "Waiting for NFC, Bluetooth and Radio...");
                            delay = endTime - SystemClock.elapsedRealtime();
                            if (bypassRadioOff) {
                                while (delay > 0) {
                                    if (ShutdownThread.mRebootHasProgressBar) {
                                        ShutdownThread.sInstance.setRebootProgress(((int) (((((double) (((long) i) - delay)) * 1.0d) * 12.0d) / ((double) i))) + 6, null);
                                    }
                                    if (!bluetoothOff) {
                                        try {
                                            bluetoothOff = bluetooth.getState() == 10;
                                        } catch (RemoteException ex22) {
                                            Log.e(ShutdownThread.TAG, "RemoteException during bluetooth shutdown", ex22);
                                            bluetoothOff = true;
                                        }
                                        if (bluetoothOff) {
                                            Log.i(ShutdownThread.TAG, "Bluetooth turned off.");
                                        }
                                    }
                                    if (!radioOff) {
                                        try {
                                            radioOff = !phone.needMobileRadioShutdown();
                                        } catch (RemoteException ex222) {
                                            Log.e(ShutdownThread.TAG, "RemoteException during radio shutdown", ex222);
                                            radioOff = true;
                                        }
                                        if (radioOff) {
                                            Log.i(ShutdownThread.TAG, "Radio turned off.");
                                        }
                                    }
                                    if (!nfcOff) {
                                        try {
                                            nfcOff = nfc.getState() == 1;
                                        } catch (RemoteException ex2222) {
                                            Log.e(ShutdownThread.TAG, "RemoteException during NFC shutdown", ex2222);
                                            nfcOff = true;
                                        }
                                        if (nfcOff) {
                                            Log.i(ShutdownThread.TAG, "NFC turned off.");
                                        }
                                    }
                                    if (radioOff && bluetoothOff && nfcOff) {
                                        Log.i(ShutdownThread.TAG, "NFC, Radio and Bluetooth shutdown complete.");
                                        done[0] = true;
                                        return;
                                    }
                                    SystemClock.sleep(100);
                                    delay = endTime - SystemClock.elapsedRealtime();
                                }
                                return;
                            }
                            done[0] = true;
                            Log.i(ShutdownThread.TAG, "bypass RadioOff!");
                            return;
                        }
                    } catch (RemoteException ex22222) {
                        Log.e(ShutdownThread.TAG, "RemoteException during radio shutdown", ex22222);
                        radioOff = true;
                    }
                }
                radioOff = true;
                Log.w(ShutdownThread.TAG, "Turning off cellular radios...");
                phone.shutdownMobileRadios();
                Log.i(ShutdownThread.TAG, "Waiting for NFC, Bluetooth and Radio...");
                delay = endTime - SystemClock.elapsedRealtime();
                if (bypassRadioOff) {
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
            if (this.mShutdownFlow == 1) {
                Log.d(TAG, "change shutdown flow from ipo to normal: BT/MD");
                this.mShutdownFlow = 0;
            }
        }
    }

    public static void rebootOrShutdown(Context context, boolean reboot, String reason) {
        if (reboot) {
            Log.i(TAG, "Rebooting, reason: " + reason);
            PowerManagerService.lowLevelReboot(reason);
            Log.e(TAG, "Reboot failed, will attempt shutdown instead");
            reason = null;
        } else if (context != null) {
            try {
                new SystemVibrator(context).vibrate(250, VIBRATION_ATTRIBUTES);
            } catch (Exception e) {
                Log.w(TAG, "Failed to vibrate during shutdown.", e);
            }
            try {
                Thread.sleep(250);
            } catch (InterruptedException e2) {
            }
        }
        Log.i(TAG, "Performing low-level shutdown...");
        PowerManagerService.lowLevelShutdown(reason);
    }

    private static boolean checkEncryption() {
        boolean z = true;
        String encryptionProgress = SystemProperties.get("vold.encrypt_progress");
        String state = SystemProperties.get("ro.crypto.state");
        String cryptoType = SystemProperties.get("ro.crypto.type");
        int passwordQuality = new LockPatternUtils(sInstance.mContext).getKeyguardStoredPasswordQuality(ActivityManager.getCurrentUser());
        if (!encryptionProgress.equals("100") && !encryptionProgress.equals(IElsaManager.EMPTY_PACKAGE)) {
            Log.e(TAG, "encryption in progress");
            return false;
        } else if (!state.equals("encrypted")) {
            Log.d(TAG, "ro.crypto.state: " + state);
            return true;
        } else if (cryptoType.equals("file")) {
            Log.d(TAG, "FBE: PasswordQuality:" + passwordQuality);
            if (passwordQuality != 0) {
                z = false;
            }
            return z;
        } else {
            if (cryptoType.equals("block")) {
                try {
                    IMountService service = IMountService.Stub.asInterface(ServiceManager.checkService(OppoProcessManager.RESUME_REASON_MOUNT_STR));
                    if (service != null) {
                        int type = service.getPasswordType();
                        Log.d(TAG, "FDE: phone encrypted type: " + type);
                        if (type != 1) {
                            z = false;
                        }
                        return z;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error calling mount service " + e);
                }
            }
            return false;
        }
    }

    private void uncrypt() {
        Log.i(TAG, "Calling uncrypt and monitoring the progress...");
        final ProgressListener progressListener = new ProgressListener() {
            public void onProgress(int status) {
                if (status >= 0 && status < 100) {
                    ShutdownThread.sInstance.setRebootProgress(((int) ((((double) status) * 80.0d) / 100.0d)) + 20, ShutdownThread.this.mContext.getText(17039650));
                } else if (status == 100) {
                    ShutdownThread.sInstance.setRebootProgress(status, ShutdownThread.this.mContext.getText(17039651));
                }
            }
        };
        final boolean[] done = new boolean[1];
        done[0] = false;
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
            Object[] objArr = new Object[2];
            objArr[0] = Integer.valueOf(900);
            objArr[1] = Integer.valueOf(100);
            try {
                FileUtils.stringToFile(RecoverySystem.UNCRYPT_STATUS_FILE, String.format("uncrypt_time: %d\nuncrypt_error: %d\n", objArr));
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
}
