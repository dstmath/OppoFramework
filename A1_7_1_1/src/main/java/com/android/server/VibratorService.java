package com.android.server;

import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.input.InputManager;
import android.hardware.input.InputManager.InputDeviceListener;
import android.media.AudioAttributes;
import android.media.AudioAttributes.Builder;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.IVibratorService.Stub;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.PowerManagerInternal;
import android.os.PowerManagerInternal.LowPowerModeListener;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.os.WorkSource;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.util.Slog;
import com.android.internal.app.IAppOpsService;
import com.android.internal.app.IBatteryStats;
import com.android.server.oppo.IElsaManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

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
public class VibratorService extends Stub implements InputDeviceListener {
    private static boolean DEBUG = false;
    private static final String SYSTEM_UI_PACKAGE = "com.android.systemui";
    private static final String TAG = "VibratorService";
    private final IAppOpsService mAppOpsService;
    private final IBatteryStats mBatteryStatsService;
    private final Context mContext;
    private int mCurVibUid;
    private Vibration mCurrentVibration;
    private final Handler mH;
    private InputManager mIm;
    private boolean mInputDeviceListenerRegistered;
    private final ArrayList<Vibrator> mInputDeviceVibrators;
    BroadcastReceiver mIntentReceiver;
    private boolean mLowPowerMode;
    private PowerManagerInternal mPowerManagerInternal;
    private final LinkedList<VibrationInfo> mPreviousVibrations;
    private final int mPreviousVibrationsLimit;
    private SettingsObserver mSettingObserver;
    volatile VibrateThread mThread;
    private final WorkSource mTmpWorkSource;
    private boolean mVibrateInputDevicesSetting;
    private final Runnable mVibrationRunnable;
    private final LinkedList<Vibration> mVibrations;
    private final WakeLock mWakeLock;

    private final class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean SelfChange) {
            VibratorService.this.updateInputDeviceVibrators();
        }
    }

    private class VibrateThread extends Thread {
        boolean mDone;
        final Vibration mVibration;

        VibrateThread(Vibration vib) {
            this.mVibration = vib;
            VibratorService.this.mTmpWorkSource.set(vib.mUid);
            VibratorService.this.mWakeLock.setWorkSource(VibratorService.this.mTmpWorkSource);
            VibratorService.this.mWakeLock.acquire();
        }

        private void delay(long duration) {
            if (duration > 0) {
                long bedtime = duration + SystemClock.uptimeMillis();
                while (true) {
                    try {
                        wait(duration);
                    } catch (InterruptedException e) {
                    }
                    if (!this.mDone) {
                        duration = bedtime - SystemClock.uptimeMillis();
                        if (duration <= 0) {
                            return;
                        }
                    } else {
                        return;
                    }
                }
            }
        }

        public void run() {
            Process.setThreadPriority(-8);
            synchronized (this) {
                long[] pattern = this.mVibration.mPattern;
                int len = pattern.length;
                int repeat = this.mVibration.mRepeat;
                int uid = this.mVibration.mUid;
                int usageHint = this.mVibration.mUsageHint;
                int index = 0;
                long duration = 0;
                while (true) {
                    int index2 = index;
                    if (this.mDone) {
                        break;
                    }
                    if (index2 < len) {
                        duration += pattern[index2];
                        index2++;
                    }
                    delay(duration);
                    if (this.mDone) {
                        index = index2;
                        break;
                    } else if (index2 < len) {
                        index = index2 + 1;
                        duration = pattern[index2];
                        if (duration > 0) {
                            VibratorService.this.doVibratorOn(duration, uid, usageHint);
                        }
                    } else if (repeat < 0) {
                        index = index2;
                        break;
                    } else {
                        index = repeat;
                        duration = 0;
                    }
                }
                VibratorService.this.mWakeLock.release();
            }
            synchronized (VibratorService.this.mVibrations) {
                if (VibratorService.this.mThread == this) {
                    VibratorService.this.mThread = null;
                }
                if (!this.mDone) {
                    VibratorService.this.unlinkVibration(this.mVibration);
                    VibratorService.this.startNextVibrationLocked();
                }
            }
        }
    }

    private class Vibration implements DeathRecipient {
        private final String mOpPkg;
        private final long[] mPattern;
        private final int mRepeat;
        private final long mStartTime;
        private final long mTimeout;
        private final IBinder mToken;
        private final int mUid;
        private final int mUsageHint;

        Vibration(VibratorService this$0, IBinder token, long millis, int usageHint, int uid, String opPkg) {
            this(token, millis, null, 0, usageHint, uid, opPkg);
        }

        Vibration(VibratorService this$0, IBinder token, long[] pattern, int repeat, int usageHint, int uid, String opPkg) {
            this(token, 0, pattern, repeat, usageHint, uid, opPkg);
        }

        private Vibration(IBinder token, long millis, long[] pattern, int repeat, int usageHint, int uid, String opPkg) {
            this.mToken = token;
            this.mTimeout = millis;
            this.mStartTime = SystemClock.uptimeMillis();
            this.mPattern = pattern;
            this.mRepeat = repeat;
            this.mUsageHint = usageHint;
            this.mUid = uid;
            this.mOpPkg = opPkg;
        }

        public void binderDied() {
            synchronized (VibratorService.this.mVibrations) {
                VibratorService.this.mVibrations.remove(this);
                if (this == VibratorService.this.mCurrentVibration) {
                    VibratorService.this.doCancelVibrateLocked();
                    VibratorService.this.startNextVibrationLocked();
                }
            }
        }

        public boolean hasLongerTimeout(long millis) {
            if (this.mTimeout != 0 && this.mStartTime + this.mTimeout >= SystemClock.uptimeMillis() + millis) {
                return true;
            }
            return false;
        }

        public boolean isSystemHapticFeedback() {
            if ((this.mUid == 1000 || this.mUid == 0 || VibratorService.SYSTEM_UI_PACKAGE.equals(this.mOpPkg)) && this.mRepeat < 0) {
                return true;
            }
            return false;
        }
    }

    private static class VibrationInfo {
        String opPkg;
        long[] pattern;
        int repeat;
        long startTime;
        long timeout;
        int uid;
        int usageHint;

        public VibrationInfo(long timeout, long startTime, long[] pattern, int repeat, int usageHint, int uid, String opPkg) {
            this.timeout = timeout;
            this.startTime = startTime;
            this.pattern = pattern;
            this.repeat = repeat;
            this.usageHint = usageHint;
            this.uid = uid;
            this.opPkg = opPkg;
        }

        public String toString() {
            return "timeout: " + this.timeout + ", startTime: " + this.startTime + ", pattern: " + Arrays.toString(this.pattern) + ", repeat: " + this.repeat + ", usageHint: " + this.usageHint + ", uid: " + this.uid + ", opPkg: " + this.opPkg;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.VibratorService.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.VibratorService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.VibratorService.<clinit>():void");
    }

    static native boolean vibratorExists();

    static native void vibratorInit();

    static native void vibratorOff();

    static native void vibratorOn(long j);

    VibratorService(Context context) {
        this.mTmpWorkSource = new WorkSource();
        this.mH = new Handler();
        this.mInputDeviceVibrators = new ArrayList();
        this.mCurVibUid = -1;
        this.mVibrationRunnable = new Runnable() {
            public void run() {
                synchronized (VibratorService.this.mVibrations) {
                    VibratorService.this.doCancelVibrateLocked();
                    VibratorService.this.startNextVibrationLocked();
                }
            }
        };
        this.mIntentReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                    synchronized (VibratorService.this.mVibrations) {
                        if (!(VibratorService.this.mCurrentVibration == null || VibratorService.this.mCurrentVibration.isSystemHapticFeedback())) {
                            VibratorService.this.doCancelVibrateLocked();
                        }
                        Iterator<Vibration> it = VibratorService.this.mVibrations.iterator();
                        while (it.hasNext()) {
                            Vibration vibration = (Vibration) it.next();
                            if (vibration != VibratorService.this.mCurrentVibration) {
                                VibratorService.this.unlinkVibration(vibration);
                                it.remove();
                            }
                        }
                    }
                }
            }
        };
        vibratorInit();
        vibratorOff();
        this.mContext = context;
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "*vibrator*");
        this.mWakeLock.setReferenceCounted(true);
        this.mAppOpsService = IAppOpsService.Stub.asInterface(ServiceManager.getService("appops"));
        this.mBatteryStatsService = IBatteryStats.Stub.asInterface(ServiceManager.getService("batterystats"));
        this.mPreviousVibrationsLimit = this.mContext.getResources().getInteger(17694881);
        this.mVibrations = new LinkedList();
        this.mPreviousVibrations = new LinkedList();
    }

    public void systemReady() {
        this.mIm = (InputManager) this.mContext.getSystemService(InputManager.class);
        this.mSettingObserver = new SettingsObserver(this.mH);
        this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        this.mPowerManagerInternal.registerLowPowerModeObserver(new LowPowerModeListener() {
            public void onLowPowerModeChanged(boolean enabled) {
                VibratorService.this.updateInputDeviceVibrators();
            }
        });
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor("vibrate_input_devices"), true, this.mSettingObserver, -1);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                VibratorService.this.updateInputDeviceVibrators();
            }
        }, new IntentFilter("android.intent.action.USER_SWITCHED"), null, this.mH);
        updateInputDeviceVibrators();
    }

    public boolean hasVibrator() {
        return doVibratorExists();
    }

    private void verifyIncomingUid(int uid) {
        if (uid != Binder.getCallingUid() && Binder.getCallingPid() != Process.myPid()) {
            this.mContext.enforcePermission("android.permission.UPDATE_APP_OPS_STATS", Binder.getCallingPid(), Binder.getCallingUid(), null);
        }
    }

    public void vibrate(int uid, String opPkg, long milliseconds, int usageHint, IBinder token) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.VIBRATE") != 0) {
            throw new SecurityException("Requires VIBRATE permission");
        }
        verifyIncomingUid(uid);
        if (milliseconds > 0 && (this.mCurrentVibration == null || !this.mCurrentVibration.hasLongerTimeout(milliseconds))) {
            if (DEBUG) {
                Slog.d(TAG, "Vibrating for " + milliseconds + " ms.");
            }
            Vibration vib = new Vibration(this, token, milliseconds, usageHint, uid, opPkg);
            long ident = Binder.clearCallingIdentity();
            try {
                synchronized (this.mVibrations) {
                    removeVibrationLocked(token);
                    doCancelVibrateLocked();
                    addToPreviousVibrationsLocked(vib);
                    startVibrationLocked(vib);
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    private boolean isAll0(long[] pattern) {
        for (long j : pattern) {
            if (j != 0) {
                return false;
            }
        }
        return true;
    }

    public void vibratePattern(int uid, String packageName, long[] pattern, int repeat, int usageHint, IBinder token) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.VIBRATE") != 0) {
            throw new SecurityException("Requires VIBRATE permission");
        }
        verifyIncomingUid(uid);
        long identity = Binder.clearCallingIdentity();
        try {
            if (DEBUG) {
                String s = IElsaManager.EMPTY_PACKAGE;
                for (long j : pattern) {
                    s = s + " " + j;
                }
                Slog.d(TAG, "Vibrating with pattern:" + s);
            }
            if (!(pattern == null || pattern.length == 0)) {
                if (!(isAll0(pattern) || repeat >= pattern.length || token == null)) {
                    Vibration vib = new Vibration(this, token, pattern, repeat, usageHint, uid, packageName);
                    try {
                        token.linkToDeath(vib, 0);
                        synchronized (this.mVibrations) {
                            removeVibrationLocked(token);
                            doCancelVibrateLocked();
                            if (repeat >= 0) {
                                this.mVibrations.addFirst(vib);
                                startNextVibrationLocked();
                            } else {
                                startVibrationLocked(vib);
                            }
                            addToPreviousVibrationsLocked(vib);
                        }
                        Binder.restoreCallingIdentity(identity);
                        return;
                    } catch (RemoteException e) {
                        Binder.restoreCallingIdentity(identity);
                        return;
                    }
                }
            }
            Binder.restoreCallingIdentity(identity);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void addToPreviousVibrationsLocked(Vibration vib) {
        if (this.mPreviousVibrations.size() > this.mPreviousVibrationsLimit) {
            this.mPreviousVibrations.removeFirst();
        }
        this.mPreviousVibrations.addLast(new VibrationInfo(vib.mTimeout, vib.mStartTime, vib.mPattern, vib.mRepeat, vib.mUsageHint, vib.mUid, vib.mOpPkg));
    }

    public void cancelVibrate(IBinder token) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.VIBRATE", "cancelVibrate");
        long identity = Binder.clearCallingIdentity();
        try {
            synchronized (this.mVibrations) {
                if (removeVibrationLocked(token) == this.mCurrentVibration) {
                    if (DEBUG) {
                        Slog.d(TAG, "Canceling vibration.");
                    }
                    doCancelVibrateLocked();
                    startNextVibrationLocked();
                }
            }
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void doCancelVibrateLocked() {
        if (this.mThread != null) {
            synchronized (this.mThread) {
                this.mThread.mDone = true;
                this.mThread.notify();
            }
            this.mThread = null;
        }
        doVibratorOff();
        this.mH.removeCallbacks(this.mVibrationRunnable);
        reportFinishVibrationLocked();
    }

    private void startNextVibrationLocked() {
        if (this.mVibrations.size() <= 0) {
            reportFinishVibrationLocked();
            this.mCurrentVibration = null;
            return;
        }
        startVibrationLocked((Vibration) this.mVibrations.getFirst());
    }

    private void startVibrationLocked(Vibration vib) {
        try {
            if (this.mLowPowerMode && vib.mUsageHint != 6) {
                return;
            }
            if (vib.mUsageHint != 6 || shouldVibrateForRingtone()) {
                int mode = this.mAppOpsService.checkAudioOperation(3, vib.mUsageHint, vib.mUid, vib.mOpPkg);
                if (mode == 0) {
                    mode = this.mAppOpsService.startOperation(AppOpsManager.getToken(this.mAppOpsService), 3, vib.mUid, vib.mOpPkg);
                }
                if (mode == 0) {
                    this.mCurrentVibration = vib;
                    if (vib.mTimeout != 0) {
                        doVibratorOn(vib.mTimeout, vib.mUid, vib.mUsageHint);
                        this.mH.postDelayed(this.mVibrationRunnable, vib.mTimeout);
                    } else {
                        this.mThread = new VibrateThread(vib);
                        this.mThread.start();
                    }
                    return;
                }
                if (mode == 2) {
                    Slog.w(TAG, "Would be an error: vibrate from uid " + vib.mUid);
                }
            }
        } catch (RemoteException e) {
        }
    }

    private boolean shouldVibrateForRingtone() {
        boolean z = true;
        int ringerMode = ((AudioManager) this.mContext.getSystemService("audio")).getRingerModeInternal();
        if (System.getInt(this.mContext.getContentResolver(), "vibrate_when_ringing", 0) != 0) {
            if (ringerMode == 0) {
                z = false;
            }
            return z;
        }
        if (ringerMode != 1) {
            z = false;
        }
        return z;
    }

    private void reportFinishVibrationLocked() {
        if (this.mCurrentVibration != null) {
            try {
                this.mAppOpsService.finishOperation(AppOpsManager.getToken(this.mAppOpsService), 3, this.mCurrentVibration.mUid, this.mCurrentVibration.mOpPkg);
            } catch (RemoteException e) {
            }
            this.mCurrentVibration = null;
        }
    }

    private Vibration removeVibrationLocked(IBinder token) {
        ListIterator<Vibration> iter = this.mVibrations.listIterator(0);
        while (iter.hasNext()) {
            Vibration vib = (Vibration) iter.next();
            if (vib.mToken == token) {
                iter.remove();
                unlinkVibration(vib);
                return vib;
            }
        }
        if (this.mCurrentVibration == null || this.mCurrentVibration.mToken != token) {
            return null;
        }
        unlinkVibration(this.mCurrentVibration);
        return this.mCurrentVibration;
    }

    private void unlinkVibration(Vibration vib) {
        if (vib.mPattern != null) {
            vib.mToken.unlinkToDeath(vib, 0);
        }
    }

    private void updateInputDeviceVibrators() {
        boolean z = true;
        synchronized (this.mVibrations) {
            doCancelVibrateLocked();
            synchronized (this.mInputDeviceVibrators) {
                this.mVibrateInputDevicesSetting = false;
                try {
                    if (System.getIntForUser(this.mContext.getContentResolver(), "vibrate_input_devices", -2) <= 0) {
                        z = false;
                    }
                    this.mVibrateInputDevicesSetting = z;
                } catch (SettingNotFoundException e) {
                }
                this.mLowPowerMode = this.mPowerManagerInternal.getLowPowerModeEnabled();
                if (this.mVibrateInputDevicesSetting) {
                    if (!this.mInputDeviceListenerRegistered) {
                        this.mInputDeviceListenerRegistered = true;
                        this.mIm.registerInputDeviceListener(this, this.mH);
                    }
                } else if (this.mInputDeviceListenerRegistered) {
                    this.mInputDeviceListenerRegistered = false;
                    this.mIm.unregisterInputDeviceListener(this);
                }
                this.mInputDeviceVibrators.clear();
                if (this.mVibrateInputDevicesSetting) {
                    int[] ids = this.mIm.getInputDeviceIds();
                    for (int inputDevice : ids) {
                        Vibrator vibrator = this.mIm.getInputDevice(inputDevice).getVibrator();
                        if (vibrator.hasVibrator()) {
                            this.mInputDeviceVibrators.add(vibrator);
                        }
                    }
                }
            }
            startNextVibrationLocked();
        }
    }

    public void onInputDeviceAdded(int deviceId) {
        updateInputDeviceVibrators();
    }

    public void onInputDeviceChanged(int deviceId) {
        updateInputDeviceVibrators();
    }

    public void onInputDeviceRemoved(int deviceId) {
        updateInputDeviceVibrators();
    }

    private boolean doVibratorExists() {
        return vibratorExists();
    }

    private void doVibratorOn(long millis, int uid, int usageHint) {
        synchronized (this.mInputDeviceVibrators) {
            if (DEBUG) {
                Slog.d(TAG, "Turning vibrator on for " + millis + " ms.");
            }
            try {
                this.mBatteryStatsService.noteVibratorOn(uid, millis);
                this.mCurVibUid = uid;
            } catch (RemoteException e) {
            }
            int vibratorCount = this.mInputDeviceVibrators.size();
            if (vibratorCount != 0) {
                AudioAttributes attributes = new Builder().setUsage(usageHint).build();
                for (int i = 0; i < vibratorCount; i++) {
                    ((Vibrator) this.mInputDeviceVibrators.get(i)).vibrate(millis, attributes);
                }
            } else {
                vibratorOn(millis);
            }
        }
    }

    private void doVibratorOff() {
        synchronized (this.mInputDeviceVibrators) {
            if (DEBUG) {
                Slog.d(TAG, "Turning vibrator off.");
            }
            if (this.mCurVibUid >= 0) {
                try {
                    this.mBatteryStatsService.noteVibratorOff(this.mCurVibUid);
                } catch (RemoteException e) {
                }
                this.mCurVibUid = -1;
            }
            int vibratorCount = this.mInputDeviceVibrators.size();
            if (vibratorCount != 0) {
                for (int i = 0; i < vibratorCount; i++) {
                    ((Vibrator) this.mInputDeviceVibrators.get(i)).cancel();
                }
            } else {
                vibratorOff();
            }
        }
    }

    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump vibrator service from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            return;
        }
        pw.println("Previous vibrations:");
        synchronized (this.mVibrations) {
            for (VibrationInfo info : this.mPreviousVibrations) {
                pw.print("  ");
                pw.println(info.toString());
            }
        }
        if (args.length > 0 && "log".equals(args[0])) {
            dynamicallyConfigLogTag(pw, args);
        }
    }

    protected void dynamicallyConfigLogTag(PrintWriter pw, String[] args) {
        pw.println("dynamicallyConfigLogTag, args.length:" + args.length);
        for (int index = 0; index < args.length; index++) {
            pw.println("dynamicallyConfigLogTag, args[" + index + "]: " + args[index]);
        }
        if (args.length != 3) {
            pw.println("********** Invalid argument! Get detail help as bellow: **********");
            logoutTagConfigHelp(pw);
            return;
        }
        String tag = args[1];
        boolean on = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(args[2]);
        pw.println("dynamicallyConfigLogTag, tag: " + tag + ", on: " + on);
        if ("all".equals(tag)) {
            DEBUG = on;
        }
    }

    protected void logoutTagConfigHelp(PrintWriter pw) {
        pw.println("********************** Help begin:**********************");
        pw.println("1. open all log in VibratorService");
        pw.println("cmd: dumpsys vibrator log all 0/1");
        pw.println("----------------------------------");
        pw.println("********************** Help end.  **********************");
    }
}
