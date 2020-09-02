package com.android.server;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.IUidObserver;
import android.common.OppoFeatureCache;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.input.InputManager;
import android.icu.text.DateFormat;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.os.Binder;
import android.os.ExternalVibration;
import android.os.Handler;
import android.os.IBinder;
import android.os.IExternalVibratorService;
import android.os.IVibratorService;
import android.os.PowerManager;
import android.os.PowerManagerInternal;
import android.os.PowerSaveState;
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.os.ShellCommand;
import android.os.SystemClock;
import android.os.Trace;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.WorkSource;
import android.provider.DeviceConfig;
import android.provider.Settings;
import android.util.DebugUtils;
import android.util.Slog;
import android.util.SparseArray;
import android.util.StatsLog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.IBatteryStats;
import com.android.internal.util.DumpUtils;
import com.android.server.biometrics.fingerprint.tool.HealthState;
import com.android.server.job.controllers.JobStatus;
import com.android.server.notification.NotificationShellCmd;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

public class VibratorService extends IVibratorService.Stub implements InputManager.InputDeviceListener {
    /* access modifiers changed from: private */
    public static boolean DEBUG = false;
    private static final long[] DOUBLE_CLICK_EFFECT_FALLBACK_TIMINGS = {0, 30, 100, 30};
    private static final String EXTERNAL_VIBRATOR_SERVICE = "external_vibrator_service";
    private static final long MAX_HAPTIC_FEEDBACK_DURATION = 5000;
    private static final String RAMPING_RINGER_ENABLED = "ramping_ringer_enabled";
    private static final int SCALE_HIGH = 1;
    private static final float SCALE_HIGH_GAMMA = 0.5f;
    private static final int SCALE_LOW = -1;
    private static final float SCALE_LOW_GAMMA = 1.5f;
    private static final int SCALE_LOW_MAX_AMPLITUDE = 192;
    private static final int SCALE_MUTE = -100;
    private static final int SCALE_NONE = 0;
    private static final float SCALE_NONE_GAMMA = 1.0f;
    private static final int SCALE_VERY_HIGH = 2;
    private static final float SCALE_VERY_HIGH_GAMMA = 0.25f;
    private static final int SCALE_VERY_LOW = -2;
    private static final float SCALE_VERY_LOW_GAMMA = 2.0f;
    private static final int SCALE_VERY_LOW_MAX_AMPLITUDE = 168;
    private static final String SYSTEM_UI_PACKAGE = "com.android.systemui";
    private static final String TAG = "VibratorService";
    private final boolean mAllowPriorityVibrationsInLowPowerMode;
    private final AppOpsManager mAppOps;
    private final IBatteryStats mBatteryStatsService;
    /* access modifiers changed from: private */
    public final Context mContext;
    private int mCurVibUid = -1;
    /* access modifiers changed from: private */
    public ExternalVibration mCurrentExternalVibration;
    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public Vibration mCurrentVibration;
    private final int mDefaultVibrationAmplitude;
    private final SparseArray<VibrationEffect> mFallbackEffects;
    /* access modifiers changed from: private */
    public final Handler mH = new Handler();
    /* access modifiers changed from: private */
    public int mHapticFeedbackIntensity;
    private InputManager mIm;
    private boolean mInputDeviceListenerRegistered;
    /* access modifiers changed from: private */
    public final ArrayList<Vibrator> mInputDeviceVibrators = new ArrayList<>();
    BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        /* class com.android.server.VibratorService.AnonymousClass5 */

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                synchronized (VibratorService.this.mLock) {
                    if (VibratorService.this.mCurrentVibration != null && (!VibratorService.this.mCurrentVibration.isHapticFeedback() || !VibratorService.this.mCurrentVibration.isFromSystem())) {
                        VibratorService.this.doCancelVibrateLocked();
                    }
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private boolean mLowPowerMode;
    /* access modifiers changed from: private */
    public int mNotificationIntensity;
    /* access modifiers changed from: private */
    public IOppoVibratorFeature mOppoVibrateFeature = null;
    private IOppoVibratorCallback mOppoVibratorCallback = new IOppoVibratorCallback() {
        /* class com.android.server.VibratorService.AnonymousClass6 */

        @Override // com.android.server.IOppoVibratorCallback
        public void onVibrationEndLocked(long duration) {
            VibratorService.this.mH.postDelayed(VibratorService.this.mVibrationEndRunnable, duration);
        }

        @Override // com.android.server.IOppoVibratorCallback
        public boolean isInputDeviceVibratorsEmpty() {
            boolean isEmpty;
            synchronized (VibratorService.this.mInputDeviceVibrators) {
                isEmpty = VibratorService.this.mInputDeviceVibrators.isEmpty();
            }
            return isEmpty;
        }

        @Override // com.android.server.IOppoVibratorCallback
        public void informVibrationFinished() {
            VibratorService.this.onVibrationFinished();
        }

        @Override // com.android.server.IOppoVibratorCallback
        public void onDebugFlagSwitch(boolean enableLog) {
            boolean unused = VibratorService.DEBUG = enableLog;
        }

        @Override // com.android.server.IOppoVibratorCallback
        public void onAcquireVibratorWakelock(int uid) {
            VibratorService.this.mTmpWorkSource.set(uid);
            VibratorService.this.mWakeLock.setWorkSource(VibratorService.this.mTmpWorkSource);
            VibratorService.this.mWakeLock.acquire();
        }

        @Override // com.android.server.IOppoVibratorCallback
        public void onReleaseVibratorWakelock() {
            VibratorService.this.mWakeLock.release();
        }

        @Override // com.android.server.IOppoVibratorCallback
        public void onNoteVibratorOnLocked(int uid, long timing) {
            VibratorService.this.noteVibratorOnLocked(uid, timing);
        }
    };
    private PowerManagerInternal mPowerManagerInternal;
    private final LinkedList<VibrationInfo> mPreviousAlarmVibrations;
    /* access modifiers changed from: private */
    public final LinkedList<ExternalVibration> mPreviousExternalVibrations;
    private final LinkedList<VibrationInfo> mPreviousNotificationVibrations;
    private final LinkedList<VibrationInfo> mPreviousRingVibrations;
    private final LinkedList<VibrationInfo> mPreviousVibrations;
    /* access modifiers changed from: private */
    public final int mPreviousVibrationsLimit;
    /* access modifiers changed from: private */
    public final SparseArray<Integer> mProcStatesCache = new SparseArray<>();
    /* access modifiers changed from: private */
    public int mRingIntensity;
    private final SparseArray<ScaleLevel> mScaleLevels;
    private SettingsObserver mSettingObserver;
    private final boolean mSupportsAmplitudeControl;
    /* access modifiers changed from: private */
    public final boolean mSupportsExternalControl;
    /* access modifiers changed from: private */
    public volatile VibrateThread mThread;
    /* access modifiers changed from: private */
    public final WorkSource mTmpWorkSource = new WorkSource();
    private final IUidObserver mUidObserver = new IUidObserver.Stub() {
        /* class com.android.server.VibratorService.AnonymousClass1 */

        public void onUidStateChanged(int uid, int procState, long procStateSeq) {
            VibratorService.this.mProcStatesCache.put(uid, Integer.valueOf(procState));
        }

        public void onUidGone(int uid, boolean disabled) {
            VibratorService.this.mProcStatesCache.delete(uid);
        }

        public void onUidActive(int uid) {
        }

        public void onUidIdle(int uid, boolean disabled) {
        }

        public void onUidCachedChanged(int uid, boolean cached) {
        }
    };
    private boolean mVibrateInputDevicesSetting;
    /* access modifiers changed from: private */
    public final Runnable mVibrationEndRunnable = new Runnable() {
        /* class com.android.server.VibratorService.AnonymousClass4 */

        public void run() {
            VibratorService.this.onVibrationFinished();
        }
    };
    /* access modifiers changed from: private */
    public Vibrator mVibrator;
    private boolean mVibratorUnderExternalControl;
    /* access modifiers changed from: private */
    public final PowerManager.WakeLock mWakeLock;

    static native boolean vibratorExists();

    static native void vibratorInit();

    static native void vibratorOff();

    static native void vibratorOn(long j);

    static native long vibratorPerformEffect(long j, long j2);

    static native void vibratorSetAmplitude(int i);

    static native void vibratorSetExternalControl(boolean z);

    static native boolean vibratorSupportsAmplitudeControl();

    static native boolean vibratorSupportsExternalControl();

    /* access modifiers changed from: private */
    public class Vibration implements IBinder.DeathRecipient {
        public VibrationEffect effect;
        public final String opPkg;
        public VibrationEffect originalEffect;
        public final String reason;
        public final long startTime;
        public final long startTimeDebug;
        public final IBinder token;
        public final int uid;
        public final int usageHint;

        private Vibration(IBinder token2, VibrationEffect effect2, int usageHint2, int uid2, String opPkg2, String reason2) {
            this.token = token2;
            this.effect = effect2;
            this.startTime = SystemClock.elapsedRealtime();
            this.startTimeDebug = System.currentTimeMillis();
            this.usageHint = usageHint2;
            this.uid = uid2;
            this.opPkg = opPkg2;
            this.reason = reason2;
        }

        public void binderDied() {
            synchronized (VibratorService.this.mLock) {
                if (this == VibratorService.this.mCurrentVibration) {
                    VibratorService.this.doCancelVibrateLocked();
                }
            }
        }

        public boolean hasTimeoutLongerThan(long millis) {
            long duration = this.effect.getDuration();
            return duration >= 0 && duration > millis;
        }

        public boolean isHapticFeedback() {
            VibratorService vibratorService = VibratorService.this;
            if (VibratorService.isHapticFeedback(this.usageHint)) {
                return true;
            }
            VibrationEffect.Prebaked prebaked = this.effect;
            if (prebaked instanceof VibrationEffect.Prebaked) {
                int id = prebaked.getId();
                if (id == 0 || id == 1 || id == 2 || id == 3 || id == 4 || id == 5 || id == 21) {
                    return true;
                }
                Slog.w(VibratorService.TAG, "Unknown prebaked vibration effect, assuming it isn't haptic feedback.");
                return false;
            }
            long duration = prebaked.getDuration();
            if (duration < 0 || duration >= 5000) {
                return false;
            }
            return true;
        }

        public boolean isNotification() {
            VibratorService vibratorService = VibratorService.this;
            return VibratorService.isNotification(this.usageHint);
        }

        public boolean isRingtone() {
            VibratorService vibratorService = VibratorService.this;
            return VibratorService.isRingtone(this.usageHint);
        }

        public boolean isAlarm() {
            VibratorService vibratorService = VibratorService.this;
            return VibratorService.isAlarm(this.usageHint);
        }

        public boolean isFromSystem() {
            int i = this.uid;
            return i == 1000 || i == 0 || VibratorService.SYSTEM_UI_PACKAGE.equals(this.opPkg);
        }

        public VibrationInfo toInfo() {
            return new VibrationInfo(this.startTimeDebug, this.effect, this.originalEffect, this.usageHint, this.uid, this.opPkg, this.reason);
        }
    }

    private static class VibrationInfo {
        private final VibrationEffect mEffect;
        private final String mOpPkg;
        private final VibrationEffect mOriginalEffect;
        private final String mReason;
        private final long mStartTimeDebug;
        private final int mUid;
        private final int mUsageHint;

        public VibrationInfo(long startTimeDebug, VibrationEffect effect, VibrationEffect originalEffect, int usageHint, int uid, String opPkg, String reason) {
            this.mStartTimeDebug = startTimeDebug;
            this.mEffect = effect;
            this.mOriginalEffect = originalEffect;
            this.mUsageHint = usageHint;
            this.mUid = uid;
            this.mOpPkg = opPkg;
            this.mReason = reason;
        }

        public String toString() {
            return "startTime: " + DateFormat.getDateTimeInstance().format(new Date(this.mStartTimeDebug)) + ", effect: " + this.mEffect + ", originalEffect: " + this.mOriginalEffect + ", usageHint: " + this.mUsageHint + ", uid: " + this.mUid + ", opPkg: " + this.mOpPkg + ", reason: " + this.mReason;
        }
    }

    private static final class ScaleLevel {
        public final float gamma;
        public final int maxAmplitude;

        public ScaleLevel(float gamma2) {
            this(gamma2, 255);
        }

        public ScaleLevel(float gamma2, int maxAmplitude2) {
            this.gamma = gamma2;
            this.maxAmplitude = maxAmplitude2;
        }

        public String toString() {
            return "ScaleLevel{gamma=" + this.gamma + ", maxAmplitude=" + this.maxAmplitude + "}";
        }
    }

    /* JADX WARN: Type inference failed for: r0v9, types: [com.android.server.VibratorService$ExternalVibratorService, android.os.IBinder] */
    VibratorService(Context context) {
        vibratorInit();
        vibratorOff();
        this.mSupportsAmplitudeControl = vibratorSupportsAmplitudeControl();
        this.mSupportsExternalControl = vibratorSupportsExternalControl();
        this.mContext = context;
        this.mWakeLock = ((PowerManager) context.getSystemService("power")).newWakeLock(1, "*vibrator*");
        this.mWakeLock.setReferenceCounted(true);
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
        this.mBatteryStatsService = IBatteryStats.Stub.asInterface(ServiceManager.getService("batterystats"));
        this.mPreviousVibrationsLimit = this.mContext.getResources().getInteger(17694876);
        this.mDefaultVibrationAmplitude = this.mContext.getResources().getInteger(17694783);
        this.mAllowPriorityVibrationsInLowPowerMode = this.mContext.getResources().getBoolean(17891345);
        this.mPreviousRingVibrations = new LinkedList<>();
        this.mPreviousNotificationVibrations = new LinkedList<>();
        this.mPreviousAlarmVibrations = new LinkedList<>();
        this.mPreviousVibrations = new LinkedList<>();
        this.mPreviousExternalVibrations = new LinkedList<>();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_OFF");
        context.registerReceiver(this.mIntentReceiver, filter);
        VibrationEffect clickEffect = createEffectFromResource(17236088);
        VibrationEffect doubleClickEffect = VibrationEffect.createWaveform(DOUBLE_CLICK_EFFECT_FALLBACK_TIMINGS, -1);
        VibrationEffect heavyClickEffect = createEffectFromResource(17236042);
        VibrationEffect tickEffect = createEffectFromResource(17236004);
        this.mFallbackEffects = new SparseArray<>();
        this.mFallbackEffects.put(0, clickEffect);
        this.mFallbackEffects.put(1, doubleClickEffect);
        this.mFallbackEffects.put(2, tickEffect);
        this.mFallbackEffects.put(5, heavyClickEffect);
        this.mFallbackEffects.put(21, VibrationEffect.get(2, false));
        this.mScaleLevels = new SparseArray<>();
        this.mScaleLevels.put(-2, new ScaleLevel(2.0f, SCALE_VERY_LOW_MAX_AMPLITUDE));
        this.mScaleLevels.put(-1, new ScaleLevel(1.5f, SCALE_LOW_MAX_AMPLITUDE));
        this.mScaleLevels.put(0, new ScaleLevel(1.0f));
        this.mScaleLevels.put(1, new ScaleLevel(0.5f));
        this.mScaleLevels.put(2, new ScaleLevel(SCALE_VERY_HIGH_GAMMA));
        ServiceManager.addService(EXTERNAL_VIBRATOR_SERVICE, (IBinder) new ExternalVibratorService());
        initOppoVibratorFeature();
    }

    private VibrationEffect createEffectFromResource(int resId) {
        return createEffectFromTimings(getLongIntArray(this.mContext.getResources(), resId));
    }

    private static VibrationEffect createEffectFromTimings(long[] timings) {
        if (timings == null || timings.length == 0) {
            return null;
        }
        if (timings.length == 1) {
            return VibrationEffect.createOneShot(timings[0], -1);
        }
        return VibrationEffect.createWaveform(timings, -1);
    }

    public void systemReady() {
        Trace.traceBegin(8388608, "VibratorService#systemReady");
        try {
            this.mIm = (InputManager) this.mContext.getSystemService(InputManager.class);
            this.mVibrator = (Vibrator) this.mContext.getSystemService(Vibrator.class);
            this.mSettingObserver = new SettingsObserver(this.mH);
            this.mPowerManagerInternal = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
            this.mPowerManagerInternal.registerLowPowerModeObserver(new PowerManagerInternal.LowPowerModeListener() {
                /* class com.android.server.VibratorService.AnonymousClass2 */

                public int getServiceType() {
                    return 2;
                }

                public void onLowPowerModeChanged(PowerSaveState result) {
                    VibratorService.this.updateVibrators();
                }
            });
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("vibrate_input_devices"), true, this.mSettingObserver, -1);
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("haptic_feedback_intensity"), true, this.mSettingObserver, -1);
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("notification_vibration_intensity"), true, this.mSettingObserver, -1);
            this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("ring_vibration_intensity"), true, this.mSettingObserver, -1);
            this.mContext.registerReceiver(new BroadcastReceiver() {
                /* class com.android.server.VibratorService.AnonymousClass3 */

                public void onReceive(Context context, Intent intent) {
                    VibratorService.this.updateVibrators();
                }
            }, new IntentFilter("android.intent.action.USER_SWITCHED"), null, this.mH);
            try {
                ActivityManager.getService().registerUidObserver(this.mUidObserver, 3, -1, (String) null);
            } catch (RemoteException e) {
            }
            updateVibrators();
            checkUnregisterScreenOffReceiver();
        } finally {
            Trace.traceEnd(8388608);
        }
    }

    private final class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean SelfChange) {
            VibratorService.this.updateVibrators();
        }
    }

    public boolean hasVibrator() {
        return doVibratorExists();
    }

    public boolean hasAmplitudeControl() {
        boolean z;
        synchronized (this.mInputDeviceVibrators) {
            z = this.mSupportsAmplitudeControl && this.mInputDeviceVibrators.isEmpty();
        }
        return z;
    }

    private void verifyIncomingUid(int uid) {
        if (uid != Binder.getCallingUid() && Binder.getCallingPid() != Process.myPid()) {
            this.mContext.enforcePermission("android.permission.UPDATE_APP_OPS_STATS", Binder.getCallingPid(), Binder.getCallingUid(), null);
        }
    }

    private static boolean verifyVibrationEffect(VibrationEffect effect) {
        if (effect == null) {
            Slog.wtf(TAG, "effect must not be null");
            return false;
        }
        try {
            effect.validate();
            return true;
        } catch (Exception e) {
            Slog.wtf(TAG, "Encountered issue when verifying VibrationEffect.", e);
            return false;
        }
    }

    private static long[] getLongIntArray(Resources r, int resid) {
        int[] ar = r.getIntArray(resid);
        if (ar == null) {
            return null;
        }
        long[] out = new long[ar.length];
        for (int i = 0; i < ar.length; i++) {
            out[i] = (long) ar[i];
        }
        return out;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:100:0x0190, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0083, code lost:
        android.os.Trace.traceEnd(8388608);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0086, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0097, code lost:
        android.os.Trace.traceEnd(8388608);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x009a, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00b9, code lost:
        android.os.Trace.traceEnd(8388608);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00bc, code lost:
        return;
     */
    public void vibrate(int uid, String opPkg, VibrationEffect effect, int usageHint, String reason, IBinder token) {
        VibrationEffect effect2;
        Trace.traceBegin(8388608, "vibrate, reason = " + reason);
        try {
            if (this.mContext.checkCallingOrSelfPermission("android.permission.VIBRATE") != 0) {
                throw new SecurityException("Requires VIBRATE permission");
            } else if (token == null) {
                Slog.e(TAG, "token must not be null");
                Trace.traceEnd(8388608);
            } else {
                verifyIncomingUid(uid);
                if (!verifyVibrationEffect(effect)) {
                    Trace.traceEnd(8388608);
                    return;
                }
                synchronized (this.mLock) {
                    try {
                        if ((effect instanceof VibrationEffect.OneShot) && this.mCurrentVibration != null && (this.mCurrentVibration.effect instanceof VibrationEffect.OneShot)) {
                            VibrationEffect.OneShot newOneShot = (VibrationEffect.OneShot) effect;
                            VibrationEffect.OneShot currentOneShot = this.mCurrentVibration.effect;
                            if (this.mCurrentVibration.hasTimeoutLongerThan(newOneShot.getDuration()) && newOneShot.getAmplitude() == currentOneShot.getAmplitude()) {
                                if (DEBUG) {
                                    Slog.d(TAG, "Ignoring incoming vibration in favor of current vibration");
                                }
                            }
                        }
                        if (this.mCurrentExternalVibration == null) {
                            if (!this.mOppoVibrateFeature.ignoreVibrateForOneShotEffect(this.mCurrentVibration != null ? this.mCurrentVibration.effect : null, effect)) {
                                if (!isRepeatingVibration(effect) && this.mCurrentVibration != null && isRepeatingVibration(this.mCurrentVibration.effect)) {
                                    if (DEBUG) {
                                        Slog.d(TAG, "Ignoring incoming vibration in favor of alarm vibration");
                                    }
                                    if (this.mOppoVibrateFeature.isReadyToStopVibrator()) {
                                        this.mOppoVibrateFeature.updateOppoVibratorStopStatus(false);
                                    } else {
                                        Trace.traceEnd(8388608);
                                        return;
                                    }
                                }
                                VibrationEffect oppoEffect = this.mOppoVibrateFeature.applyLinearMotorVibrator(uid, opPkg, effect);
                                if (oppoEffect != null) {
                                    effect2 = oppoEffect;
                                } else {
                                    effect2 = effect;
                                }
                                try {
                                    Vibration vib = new Vibration(token, effect2, usageHint, uid, opPkg, reason);
                                    if (this.mProcStatesCache.get(uid, 7).intValue() <= 7 || vib.isNotification() || vib.isRingtone() || vib.isAlarm()) {
                                        linkVibration(vib);
                                        long ident = Binder.clearCallingIdentity();
                                        try {
                                            doCancelVibrateLocked();
                                            startVibrationLocked(vib);
                                            addToPreviousVibrationsLocked(vib);
                                            Trace.traceEnd(8388608);
                                        } finally {
                                            Binder.restoreCallingIdentity(ident);
                                        }
                                    } else {
                                        Slog.e(TAG, "Ignoring incoming vibration as process with uid = " + uid + " is background, usage = " + AudioAttributes.usageToString(vib.usageHint));
                                        Trace.traceEnd(8388608);
                                    }
                                } catch (Throwable th) {
                                    th = th;
                                    while (true) {
                                        try {
                                            break;
                                        } catch (Throwable th2) {
                                            th = th2;
                                        }
                                    }
                                    throw th;
                                }
                            } else if (DEBUG) {
                                Slog.d(TAG, "Ignoring incoming vibration in favor of current vibration");
                            }
                        } else if (DEBUG) {
                            Slog.d(TAG, "Ignoring incoming vibration for current external vibration");
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        while (true) {
                            break;
                        }
                        throw th;
                    }
                }
            }
        } catch (Throwable th4) {
            th = th4;
            Trace.traceEnd(8388608);
            throw th;
        }
    }

    private static boolean isRepeatingVibration(VibrationEffect effect) {
        return effect.getDuration() == JobStatus.NO_LATEST_RUNTIME;
    }

    private void addToPreviousVibrationsLocked(Vibration vib) {
        LinkedList<VibrationInfo> previousVibrations;
        if (vib.isRingtone()) {
            previousVibrations = this.mPreviousRingVibrations;
        } else if (vib.isNotification()) {
            previousVibrations = this.mPreviousNotificationVibrations;
        } else if (vib.isAlarm()) {
            previousVibrations = this.mPreviousAlarmVibrations;
        } else {
            previousVibrations = this.mPreviousVibrations;
        }
        if (previousVibrations.size() > this.mPreviousVibrationsLimit) {
            previousVibrations.removeFirst();
        }
        previousVibrations.addLast(vib.toInfo());
    }

    public void cancelVibrate(IBinder token) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.VIBRATE", "cancelVibrate");
        synchronized (this.mLock) {
            if (this.mCurrentVibration != null && this.mCurrentVibration.token == token) {
                if (DEBUG) {
                    Slog.d(TAG, "Canceling vibration.");
                }
                this.mOppoVibrateFeature.updateOppoVibratorStopStatus(true);
                long ident = Binder.clearCallingIdentity();
                try {
                    doCancelVibrateLocked();
                } finally {
                    Binder.restoreCallingIdentity(ident);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy({"mLock"})
    public void doCancelVibrateLocked() {
        Trace.asyncTraceEnd(8388608, "vibration", 0);
        Trace.traceBegin(8388608, "doCancelVibrateLocked");
        try {
            this.mH.removeCallbacks(this.mVibrationEndRunnable);
            if (this.mThread != null) {
                this.mThread.cancel();
                this.mThread = null;
            }
            if (this.mCurrentExternalVibration != null) {
                this.mCurrentExternalVibration.mute();
                this.mCurrentExternalVibration = null;
                setVibratorUnderExternalControl(false);
            }
            this.mOppoVibrateFeature.cancelLinearMotorVibrator();
            doVibratorOff();
            reportFinishVibrationLocked();
        } finally {
            Trace.traceEnd(8388608);
        }
    }

    public void onVibrationFinished() {
        if (DEBUG) {
            Slog.e(TAG, "Vibration finished, cleaning up");
        }
        synchronized (this.mLock) {
            doCancelVibrateLocked();
        }
    }

    @GuardedBy({"mLock"})
    private void startVibrationLocked(Vibration vib) {
        Trace.traceBegin(8388608, "startVibrationLocked");
        try {
            if (isAllowedToVibrateLocked(vib)) {
                int intensity = getCurrentIntensityLocked(vib);
                if (intensity == 0) {
                    Trace.traceEnd(8388608);
                } else if (!vib.isRingtone() || shouldVibrateForRingtone()) {
                    int mode = getAppOpMode(vib);
                    if (mode != 0) {
                        if (mode == 2) {
                            Slog.w(TAG, "Would be an error: vibrate from uid " + vib.uid);
                        }
                        Trace.traceEnd(8388608);
                        return;
                    }
                    applyVibrationIntensityScalingLocked(vib, intensity);
                    startVibrationInnerLocked(vib);
                    Trace.traceEnd(8388608);
                } else {
                    if (DEBUG) {
                        Slog.e(TAG, "Vibrate ignored, not vibrating for ringtones");
                    }
                    Trace.traceEnd(8388608);
                }
            }
        } finally {
            Trace.traceEnd(8388608);
        }
    }

    @GuardedBy({"mLock"})
    private void startVibrationInnerLocked(Vibration vib) {
        Trace.traceBegin(8388608, "startVibrationInnerLocked");
        try {
            this.mCurrentVibration = vib;
            if (vib.effect instanceof VibrationEffect.OneShot) {
                Trace.asyncTraceBegin(8388608, "vibration", 0);
                VibrationEffect.OneShot oneShot = vib.effect;
                doVibratorOn(oneShot.getDuration(), oneShot.getAmplitude(), vib.uid, vib.usageHint);
                this.mH.postDelayed(this.mVibrationEndRunnable, oneShot.getDuration());
            } else if (vib.effect instanceof VibrationEffect.Waveform) {
                Trace.asyncTraceBegin(8388608, "vibration", 0);
                this.mThread = new VibrateThread(vib.effect, vib.uid, vib.usageHint);
                this.mThread.start();
            } else if (vib.effect instanceof VibrationEffect.Prebaked) {
                Trace.asyncTraceBegin(8388608, "vibration", 0);
                long timeout = doVibratorPrebakedEffectLocked(vib);
                if (timeout > 0) {
                    this.mH.postDelayed(this.mVibrationEndRunnable, timeout);
                }
            } else if (!this.mOppoVibrateFeature.startCustomizeVibratorLocked(vib.effect, vib.uid, vib.usageHint)) {
                Slog.e(TAG, "Unknown vibration type, ignoring");
            }
        } finally {
            Trace.traceEnd(8388608);
        }
    }

    private boolean isAllowedToVibrateLocked(Vibration vib) {
        if (!this.mLowPowerMode || vib.usageHint == 6 || vib.usageHint == 4 || vib.usageHint == 11 || vib.usageHint == 7) {
            return true;
        }
        return false;
    }

    private int getCurrentIntensityLocked(Vibration vib) {
        if (vib.isRingtone()) {
            return this.mRingIntensity;
        }
        if (vib.isNotification()) {
            return this.mNotificationIntensity;
        }
        if (vib.isHapticFeedback()) {
            return this.mHapticFeedbackIntensity;
        }
        if (vib.isAlarm()) {
            return 3;
        }
        return 2;
    }

    private void applyVibrationIntensityScalingLocked(Vibration vib, int intensity) {
        int defaultIntensity;
        if (!this.mOppoVibrateFeature.isOppoNativeVibrationEffect(vib.effect)) {
            if (vib.effect instanceof VibrationEffect.Prebaked) {
                vib.effect.setEffectStrength(intensityToEffectStrength(intensity));
                return;
            }
            if (vib.isRingtone()) {
                defaultIntensity = this.mVibrator.getDefaultRingVibrationIntensity();
            } else if (vib.isNotification()) {
                defaultIntensity = this.mVibrator.getDefaultNotificationVibrationIntensity();
            } else if (vib.isHapticFeedback()) {
                defaultIntensity = this.mVibrator.getDefaultHapticFeedbackIntensity();
            } else if (vib.isAlarm()) {
                defaultIntensity = 3;
            } else {
                return;
            }
            ScaleLevel scale = this.mScaleLevels.get(intensity - defaultIntensity);
            if (scale == null) {
                Slog.e(TAG, "No configured scaling level! (current=" + intensity + ", default= " + defaultIntensity + ")");
                return;
            }
            VibrationEffect scaledEffect = null;
            if (vib.effect instanceof VibrationEffect.OneShot) {
                scaledEffect = vib.effect.resolve(this.mDefaultVibrationAmplitude).scale(scale.gamma, scale.maxAmplitude);
            } else if (vib.effect instanceof VibrationEffect.Waveform) {
                scaledEffect = vib.effect.resolve(this.mDefaultVibrationAmplitude).scale(scale.gamma, scale.maxAmplitude);
            } else {
                Slog.w(TAG, "Unable to apply intensity scaling, unknown VibrationEffect type");
            }
            if (scaledEffect != null) {
                vib.originalEffect = vib.effect;
                vib.effect = scaledEffect;
            }
        }
    }

    private boolean shouldVibrateForRingtone() {
        int ringerMode = ((AudioManager) this.mContext.getSystemService(AudioManager.class)).getRingerModeInternal();
        return Settings.System.getInt(this.mContext.getContentResolver(), "vibrate_when_ringing", 0) != 0 ? ringerMode != 0 : (Settings.Global.getInt(this.mContext.getContentResolver(), "apply_ramping_ringer", 0) == 0 || !DeviceConfig.getBoolean("telephony", RAMPING_RINGER_ENABLED, false)) ? ringerMode == 1 : ringerMode != 0;
    }

    private int getAppOpMode(Vibration vib) {
        int mode = this.mAppOps.checkAudioOpNoThrow(3, vib.usageHint, vib.uid, vib.opPkg);
        if (mode == 0) {
            return this.mAppOps.startOpNoThrow(3, vib.uid, vib.opPkg);
        }
        return mode;
    }

    @GuardedBy({"mLock"})
    private void reportFinishVibrationLocked() {
        Trace.traceBegin(8388608, "reportFinishVibrationLocked");
        try {
            if (this.mCurrentVibration != null) {
                this.mAppOps.finishOp(3, this.mCurrentVibration.uid, this.mCurrentVibration.opPkg);
                unlinkVibration(this.mCurrentVibration);
                this.mCurrentVibration = null;
                this.mOppoVibrateFeature.updateOppoVibratorStopStatus(false);
            }
        } finally {
            Trace.traceEnd(8388608);
        }
    }

    private void linkVibration(Vibration vib) {
        if ((vib.effect instanceof VibrationEffect.Waveform) || this.mOppoVibrateFeature.isOppoNativeWaveformEffect(vib.effect)) {
            try {
                vib.token.linkToDeath(vib, 0);
            } catch (RemoteException e) {
            }
        }
    }

    private void unlinkVibration(Vibration vib) {
        if ((vib.effect instanceof VibrationEffect.Waveform) || this.mOppoVibrateFeature.isOppoNativeWaveformEffect(vib.effect)) {
            vib.token.unlinkToDeath(vib, 0);
        }
    }

    /* access modifiers changed from: private */
    public void updateVibrators() {
        synchronized (this.mLock) {
            boolean devicesUpdated = updateInputDeviceVibratorsLocked();
            boolean lowPowerModeUpdated = updateLowPowerModeLocked();
            updateVibrationIntensityLocked();
            if (devicesUpdated || lowPowerModeUpdated) {
                doCancelVibrateLocked();
            }
        }
    }

    private boolean updateInputDeviceVibratorsLocked() {
        int[] ids;
        boolean changed = false;
        boolean vibrateInputDevices = false;
        try {
            vibrateInputDevices = Settings.System.getIntForUser(this.mContext.getContentResolver(), "vibrate_input_devices", -2) > 0;
        } catch (Settings.SettingNotFoundException e) {
        }
        if (vibrateInputDevices != this.mVibrateInputDevicesSetting) {
            changed = true;
            this.mVibrateInputDevicesSetting = vibrateInputDevices;
        }
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
        if (!this.mVibrateInputDevicesSetting) {
            return changed;
        }
        for (int i : this.mIm.getInputDeviceIds()) {
            Vibrator vibrator = this.mIm.getInputDevice(i).getVibrator();
            if (vibrator.hasVibrator()) {
                this.mInputDeviceVibrators.add(vibrator);
            }
        }
        return true;
    }

    private boolean updateLowPowerModeLocked() {
        boolean lowPowerMode = this.mPowerManagerInternal.getLowPowerState(2).batterySaverEnabled;
        if (lowPowerMode == this.mLowPowerMode) {
            return false;
        }
        this.mLowPowerMode = lowPowerMode;
        return true;
    }

    private void updateVibrationIntensityLocked() {
        this.mHapticFeedbackIntensity = Settings.System.getIntForUser(this.mContext.getContentResolver(), "haptic_feedback_intensity", this.mVibrator.getDefaultHapticFeedbackIntensity(), -2);
        this.mNotificationIntensity = Settings.System.getIntForUser(this.mContext.getContentResolver(), "notification_vibration_intensity", this.mVibrator.getDefaultNotificationVibrationIntensity(), -2);
        this.mRingIntensity = Settings.System.getIntForUser(this.mContext.getContentResolver(), "ring_vibration_intensity", this.mVibrator.getDefaultRingVibrationIntensity(), -2);
    }

    public void onInputDeviceAdded(int deviceId) {
        updateVibrators();
    }

    public void onInputDeviceChanged(int deviceId) {
        updateVibrators();
    }

    public void onInputDeviceRemoved(int deviceId) {
        updateVibrators();
    }

    private boolean doVibratorExists() {
        return vibratorExists();
    }

    /* access modifiers changed from: private */
    public void doVibratorOn(long millis, int amplitude, int uid, int usageHint) {
        Trace.traceBegin(8388608, "doVibratorOn");
        try {
            synchronized (this.mInputDeviceVibrators) {
                if (amplitude == -1) {
                    amplitude = this.mDefaultVibrationAmplitude;
                }
                if (DEBUG) {
                    Slog.d(TAG, "Turning vibrator on for " + millis + " ms with amplitude " + amplitude + ".");
                }
                noteVibratorOnLocked(uid, millis);
                int vibratorCount = this.mInputDeviceVibrators.size();
                if (vibratorCount != 0) {
                    AudioAttributes attributes = new AudioAttributes.Builder().setUsage(usageHint).build();
                    for (int i = 0; i < vibratorCount; i++) {
                        this.mInputDeviceVibrators.get(i).vibrate(millis, attributes);
                    }
                } else {
                    vibratorOn(millis);
                    doVibratorSetAmplitude(amplitude);
                }
            }
        } finally {
            Trace.traceEnd(8388608);
        }
    }

    /* access modifiers changed from: private */
    public void doVibratorSetAmplitude(int amplitude) {
        if (this.mSupportsAmplitudeControl) {
            vibratorSetAmplitude(amplitude);
        }
    }

    private void doVibratorOff() {
        Trace.traceBegin(8388608, "doVibratorOff");
        try {
            synchronized (this.mInputDeviceVibrators) {
                if (DEBUG) {
                    Slog.d(TAG, "Turning vibrator off.");
                }
                noteVibratorOffLocked();
                int vibratorCount = this.mInputDeviceVibrators.size();
                if (vibratorCount != 0) {
                    for (int i = 0; i < vibratorCount; i++) {
                        this.mInputDeviceVibrators.get(i).cancel();
                    }
                } else {
                    vibratorOff();
                    this.mOppoVibrateFeature.turnOffLinearMotorVibrator();
                }
            }
        } finally {
            Trace.traceEnd(8388608);
        }
    }

    @GuardedBy({"mLock"})
    private long doVibratorPrebakedEffectLocked(Vibration vib) {
        boolean usingInputDeviceVibrators;
        Trace.traceBegin(8388608, "doVibratorPrebakedEffectLocked");
        try {
            VibrationEffect.Prebaked prebaked = vib.effect;
            synchronized (this.mInputDeviceVibrators) {
                usingInputDeviceVibrators = !this.mInputDeviceVibrators.isEmpty();
            }
            if (!usingInputDeviceVibrators) {
                long timeout = vibratorPerformEffect((long) prebaked.getId(), (long) prebaked.getEffectStrength());
                if (timeout > 0) {
                    noteVibratorOnLocked(vib.uid, timeout);
                    return timeout;
                }
            }
            if (!prebaked.shouldFallback()) {
                Trace.traceEnd(8388608);
                return 0;
            }
            VibrationEffect effect = getFallbackEffect(prebaked.getId());
            if (effect == null) {
                Slog.w(TAG, "Failed to play prebaked effect, no fallback");
                Trace.traceEnd(8388608);
                return 0;
            }
            IBinder iBinder = vib.token;
            int i = vib.usageHint;
            int i2 = vib.uid;
            String str = vib.opPkg;
            Vibration fallbackVib = new Vibration(iBinder, effect, i, i2, str, vib.reason + " (fallback)");
            int intensity = getCurrentIntensityLocked(fallbackVib);
            linkVibration(fallbackVib);
            applyVibrationIntensityScalingLocked(fallbackVib, intensity);
            startVibrationInnerLocked(fallbackVib);
            Trace.traceEnd(8388608);
            return 0;
        } finally {
            Trace.traceEnd(8388608);
        }
    }

    private VibrationEffect getFallbackEffect(int effectId) {
        return this.mFallbackEffects.get(effectId);
    }

    private static int intensityToEffectStrength(int intensity) {
        if (intensity == 1) {
            return 0;
        }
        if (intensity == 2) {
            return 1;
        }
        if (intensity == 3) {
            return 2;
        }
        Slog.w(TAG, "Got unexpected vibration intensity: " + intensity);
        return 2;
    }

    /* access modifiers changed from: private */
    public static boolean isNotification(int usageHint) {
        if (usageHint == 5 || usageHint == 7 || usageHint == 8 || usageHint == 9) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: private */
    public static boolean isRingtone(int usageHint) {
        return usageHint == 6;
    }

    /* access modifiers changed from: private */
    public static boolean isHapticFeedback(int usageHint) {
        return usageHint == 13;
    }

    /* access modifiers changed from: private */
    public static boolean isAlarm(int usageHint) {
        return usageHint == 4;
    }

    /* access modifiers changed from: private */
    public void noteVibratorOnLocked(int uid, long millis) {
        try {
            this.mBatteryStatsService.noteVibratorOn(uid, millis);
            StatsLog.write_non_chained(84, uid, null, 1, millis);
            this.mCurVibUid = uid;
        } catch (RemoteException e) {
        }
    }

    private void noteVibratorOffLocked() {
        int i = this.mCurVibUid;
        if (i >= 0) {
            try {
                this.mBatteryStatsService.noteVibratorOff(i);
                StatsLog.write_non_chained(84, this.mCurVibUid, null, 0, 0);
            } catch (RemoteException e) {
            }
            this.mCurVibUid = -1;
        }
    }

    /* access modifiers changed from: private */
    public void setVibratorUnderExternalControl(boolean externalControl) {
        if (DEBUG) {
            if (externalControl) {
                Slog.d(TAG, "Vibrator going under external control.");
            } else {
                Slog.d(TAG, "Taking back control of vibrator.");
            }
        }
        this.mVibratorUnderExternalControl = externalControl;
        vibratorSetExternalControl(externalControl);
    }

    /* access modifiers changed from: private */
    public class VibrateThread extends Thread {
        private boolean mForceStop;
        private final int mUid;
        private final int mUsageHint;
        private final VibrationEffect.Waveform mWaveform;

        VibrateThread(VibrationEffect.Waveform waveform, int uid, int usageHint) {
            this.mWaveform = waveform;
            this.mUid = uid;
            this.mUsageHint = usageHint;
            VibratorService.this.mTmpWorkSource.set(uid);
            VibratorService.this.mWakeLock.setWorkSource(VibratorService.this.mTmpWorkSource);
        }

        private long delayLocked(long duration) {
            Trace.traceBegin(8388608, "delayLocked");
            long durationRemaining = duration;
            if (duration > 0) {
                try {
                    long bedtime = SystemClock.uptimeMillis() + duration;
                    do {
                        try {
                            wait(durationRemaining);
                        } catch (InterruptedException e) {
                        }
                        if (this.mForceStop) {
                            break;
                        }
                        durationRemaining = bedtime - SystemClock.uptimeMillis();
                    } while (durationRemaining > 0);
                    return duration - durationRemaining;
                } finally {
                    Trace.traceEnd(8388608);
                }
            } else {
                Trace.traceEnd(8388608);
                return 0;
            }
        }

        public void run() {
            Process.setThreadPriority(-8);
            VibratorService.this.mWakeLock.acquire();
            try {
                if (playWaveform()) {
                    VibratorService.this.onVibrationFinished();
                }
            } finally {
                VibratorService.this.mWakeLock.release();
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:19:0x0077  */
        /* JADX WARNING: Removed duplicated region for block: B:20:0x007c  */
        public boolean playWaveform() {
            boolean z;
            long onDuration;
            Trace.traceBegin(8388608, "playWaveform");
            try {
                synchronized (this) {
                    long[] timings = this.mWaveform.getTimings();
                    int[] amplitudes = this.mWaveform.getAmplitudes();
                    int len = timings.length;
                    int repeat = this.mWaveform.getRepeatIndex();
                    long finalDuration = 0;
                    VibratorService.this.mOppoVibrateFeature.logVibratorPatterns(timings, amplitudes, len);
                    int index = 0;
                    long waitTime = 0;
                    while (true) {
                        if (this.mForceStop) {
                            break;
                        } else if (index < len) {
                            int amplitude = amplitudes[index];
                            int index2 = index + 1;
                            long duration = timings[index];
                            finalDuration = duration;
                            if (duration <= 0) {
                                index = index2;
                            } else {
                                if (amplitude != 0) {
                                    if (waitTime <= 0) {
                                        onDuration = getTotalOnDuration(timings, amplitudes, index2 - 1, repeat);
                                        VibratorService.this.doVibratorOn(onDuration, amplitude, this.mUid, this.mUsageHint);
                                        long waitTime2 = delayLocked(duration);
                                        if (amplitude == 0) {
                                            waitTime = onDuration - waitTime2;
                                        } else {
                                            waitTime = onDuration;
                                        }
                                        index = index2;
                                    } else {
                                        VibratorService.this.doVibratorSetAmplitude(amplitude);
                                    }
                                }
                                onDuration = waitTime;
                                long waitTime22 = delayLocked(duration);
                                if (amplitude == 0) {
                                }
                                index = index2;
                            }
                        } else if (repeat >= 0) {
                            index = repeat;
                        } else if (finalDuration < 35) {
                            delayLocked(35 - finalDuration);
                        }
                    }
                    z = !this.mForceStop;
                }
                return z;
            } finally {
                Trace.traceEnd(8388608);
            }
        }

        public void cancel() {
            synchronized (this) {
                VibratorService.this.mThread.mForceStop = true;
                VibratorService.this.mThread.notify();
            }
        }

        private long getTotalOnDuration(long[] timings, int[] amplitudes, int startIndex, int repeatIndex) {
            int i = startIndex;
            long timing = 0;
            do {
                if (amplitudes[i] != 0) {
                    int i2 = i + 1;
                    timing += timings[i];
                    if (i2 < timings.length) {
                        i = i2;
                        continue;
                    } else if (repeatIndex >= 0) {
                        i = repeatIndex;
                        repeatIndex = -1;
                        continue;
                    }
                }
                return timing;
            } while (i != startIndex);
            return 1000;
        }
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            pw.println("Vibrator Service:");
            synchronized (this.mLock) {
                pw.print("  mCurrentVibration=");
                if (this.mCurrentVibration != null) {
                    pw.println(this.mCurrentVibration.toInfo().toString());
                } else {
                    pw.println("null");
                }
                pw.print("  mCurrentExternalVibration=");
                if (this.mCurrentExternalVibration != null) {
                    pw.println(this.mCurrentExternalVibration.toString());
                } else {
                    pw.println("null");
                }
                pw.println("  mVibratorUnderExternalControl=" + this.mVibratorUnderExternalControl);
                pw.println("  mLowPowerMode=" + this.mLowPowerMode);
                pw.println("  mHapticFeedbackIntensity=" + this.mHapticFeedbackIntensity);
                pw.println("  mNotificationIntensity=" + this.mNotificationIntensity);
                pw.println("  mRingIntensity=" + this.mRingIntensity);
                pw.println("");
                pw.println("  Previous ring vibrations:");
                Iterator<VibrationInfo> it = this.mPreviousRingVibrations.iterator();
                while (it.hasNext()) {
                    pw.print("    ");
                    pw.println(it.next().toString());
                }
                pw.println("  Previous notification vibrations:");
                Iterator<VibrationInfo> it2 = this.mPreviousNotificationVibrations.iterator();
                while (it2.hasNext()) {
                    pw.print("    ");
                    pw.println(it2.next().toString());
                }
                pw.println("  Previous alarm vibrations:");
                Iterator<VibrationInfo> it3 = this.mPreviousAlarmVibrations.iterator();
                while (it3.hasNext()) {
                    pw.print("    ");
                    pw.println(it3.next().toString());
                }
                pw.println("  Previous vibrations:");
                Iterator<VibrationInfo> it4 = this.mPreviousVibrations.iterator();
                while (it4.hasNext()) {
                    pw.print("    ");
                    pw.println(it4.next().toString());
                }
                pw.println("  Previous external vibrations:");
                Iterator<ExternalVibration> it5 = this.mPreviousExternalVibrations.iterator();
                while (it5.hasNext()) {
                    pw.print("    ");
                    pw.println(it5.next().toString());
                }
            }
            if (args.length > 0 && "log".equals(args[0])) {
                this.mOppoVibrateFeature.dynamicallyConfigLogTag(pw, args);
            }
        }
    }

    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) throws RemoteException {
        new VibratorShellCommand(this).exec(this, in, out, err, args, callback, resultReceiver);
    }

    final class ExternalVibratorService extends IExternalVibratorService.Stub {
        ExternalVibrationDeathRecipient mCurrentExternalDeathRecipient;

        ExternalVibratorService() {
        }

        /* JADX INFO: Multiple debug info for r3v2 int: [D('scaleLevel' int), D('currentIntensity' int)] */
        public int onExternalVibrationStart(ExternalVibration vib) {
            int currentIntensity;
            int defaultIntensity;
            int currentIntensity2;
            if (!VibratorService.this.mSupportsExternalControl) {
                return VibratorService.SCALE_MUTE;
            }
            if (ActivityManager.checkComponentPermission("android.permission.VIBRATE", vib.getUid(), -1, true) != 0) {
                Slog.w(VibratorService.TAG, "pkg=" + vib.getPackage() + ", uid=" + vib.getUid() + " tried to play externally controlled vibration without VIBRATE permission, ignoring.");
                return VibratorService.SCALE_MUTE;
            }
            synchronized (VibratorService.this.mLock) {
                if (!vib.equals(VibratorService.this.mCurrentExternalVibration)) {
                    if (VibratorService.this.mCurrentExternalVibration == null) {
                        VibratorService.this.doCancelVibrateLocked();
                        VibratorService.this.setVibratorUnderExternalControl(true);
                    }
                    ExternalVibration unused = VibratorService.this.mCurrentExternalVibration = vib;
                    this.mCurrentExternalDeathRecipient = new ExternalVibrationDeathRecipient();
                    VibratorService.this.mCurrentExternalVibration.linkToDeath(this.mCurrentExternalDeathRecipient);
                    if (VibratorService.this.mPreviousExternalVibrations.size() > VibratorService.this.mPreviousVibrationsLimit) {
                        VibratorService.this.mPreviousExternalVibrations.removeFirst();
                    }
                    VibratorService.this.mPreviousExternalVibrations.addLast(vib);
                    if (VibratorService.DEBUG) {
                        Slog.e(VibratorService.TAG, "Playing external vibration: " + vib);
                    }
                }
                int usage = vib.getAudioAttributes().getUsage();
                if (VibratorService.isRingtone(usage)) {
                    defaultIntensity = VibratorService.this.mVibrator.getDefaultRingVibrationIntensity();
                    currentIntensity = VibratorService.this.mRingIntensity;
                } else if (VibratorService.isNotification(usage)) {
                    defaultIntensity = VibratorService.this.mVibrator.getDefaultNotificationVibrationIntensity();
                    currentIntensity = VibratorService.this.mNotificationIntensity;
                } else if (VibratorService.isHapticFeedback(usage)) {
                    defaultIntensity = VibratorService.this.mVibrator.getDefaultHapticFeedbackIntensity();
                    currentIntensity = VibratorService.this.mHapticFeedbackIntensity;
                } else if (VibratorService.isAlarm(usage)) {
                    defaultIntensity = 3;
                    currentIntensity = 3;
                } else {
                    defaultIntensity = 0;
                    currentIntensity = 0;
                }
                currentIntensity2 = currentIntensity - defaultIntensity;
            }
            if (currentIntensity2 >= -2 && currentIntensity2 <= 2) {
                return currentIntensity2;
            }
            Slog.w(VibratorService.TAG, "Error in scaling calculations, ended up with invalid scale level " + currentIntensity2 + " for vibration " + vib);
            return 0;
        }

        public void onExternalVibrationStop(ExternalVibration vib) {
            synchronized (VibratorService.this.mLock) {
                if (vib.equals(VibratorService.this.mCurrentExternalVibration)) {
                    VibratorService.this.mCurrentExternalVibration.unlinkToDeath(this.mCurrentExternalDeathRecipient);
                    this.mCurrentExternalDeathRecipient = null;
                    ExternalVibration unused = VibratorService.this.mCurrentExternalVibration = null;
                    VibratorService.this.setVibratorUnderExternalControl(false);
                    if (VibratorService.DEBUG) {
                        Slog.e(VibratorService.TAG, "Stopping external vibration" + vib);
                    }
                }
            }
        }

        private class ExternalVibrationDeathRecipient implements IBinder.DeathRecipient {
            private ExternalVibrationDeathRecipient() {
            }

            public void binderDied() {
                synchronized (VibratorService.this.mLock) {
                    ExternalVibratorService.this.onExternalVibrationStop(VibratorService.this.mCurrentExternalVibration);
                }
            }
        }
    }

    private final class VibratorShellCommand extends ShellCommand {
        private final IBinder mToken;

        private final class CommonOptions {
            public boolean force;

            private CommonOptions() {
                this.force = false;
            }

            public void check(String opt) {
                if (((opt.hashCode() == 1497 && opt.equals("-f")) ? (char) 0 : 65535) == 0) {
                    this.force = true;
                }
            }
        }

        private VibratorShellCommand(IBinder token) {
            this.mToken = token;
        }

        public int onCommand(String cmd) {
            if ("vibrate".equals(cmd)) {
                return runVibrate();
            }
            if ("waveform".equals(cmd)) {
                return runWaveform();
            }
            if ("prebaked".equals(cmd)) {
                return runPrebaked();
            }
            if (!HealthState.CANCEL.equals(cmd)) {
                return handleDefaultCommands(cmd);
            }
            VibratorService.this.cancelVibrate(this.mToken);
            return 0;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0033, code lost:
            r3 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x0034, code lost:
            if (r1 != null) goto L_0x0036;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:16:0x0036, code lost:
            $closeResource(r2, r1);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:17:0x0039, code lost:
            throw r3;
         */
        private boolean checkDoNotDisturb(CommonOptions opts) {
            try {
                int zenMode = Settings.Global.getInt(VibratorService.this.mContext.getContentResolver(), "zen_mode");
                if (zenMode == 0 || opts.force) {
                    return false;
                }
                PrintWriter pw = getOutPrintWriter();
                pw.print("Ignoring because device is on DND mode ");
                pw.println(DebugUtils.flagsToString(Settings.Global.class, "ZEN_MODE_", zenMode));
                $closeResource(null, pw);
                return true;
            } catch (Settings.SettingNotFoundException e) {
                return false;
            }
        }

        private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
            if (x0 != null) {
                try {
                    x1.close();
                } catch (Throwable th) {
                    x0.addSuppressed(th);
                }
            } else {
                x1.close();
            }
        }

        private int runVibrate() {
            Trace.traceBegin(8388608, "runVibrate");
            try {
                CommonOptions commonOptions = new CommonOptions();
                while (true) {
                    String opt = getNextOption();
                    if (opt == null) {
                        break;
                    }
                    commonOptions.check(opt);
                }
                if (checkDoNotDisturb(commonOptions)) {
                    return 0;
                }
                long duration = Long.parseLong(getNextArgRequired());
                String description = getNextArg();
                if (description == null) {
                    description = NotificationShellCmd.CHANNEL_NAME;
                }
                VibratorService.this.vibrate(Binder.getCallingUid(), description, VibrationEffect.createOneShot(duration, -1), 0, "Shell Command", this.mToken);
                Trace.traceEnd(8388608);
                return 0;
            } finally {
                Trace.traceEnd(8388608);
            }
        }

        /* JADX WARNING: Removed duplicated region for block: B:23:0x0051 A[Catch:{ all -> 0x00fe }] */
        /* JADX WARNING: Removed duplicated region for block: B:29:0x006c A[Catch:{ all -> 0x00fe }] */
        private int runWaveform() {
            VibrationEffect effect;
            Trace.traceBegin(8388608, "runWaveform");
            String description = NotificationShellCmd.CHANNEL_NAME;
            int repeat = -1;
            ArrayList<Integer> amplitudesList = null;
            try {
                CommonOptions commonOptions = new CommonOptions();
                while (true) {
                    String opt = getNextOption();
                    boolean z = false;
                    if (opt == null) {
                        break;
                    }
                    int hashCode = opt.hashCode();
                    if (hashCode != 1492) {
                        if (hashCode != 1495) {
                            if (hashCode == 1509 && opt.equals("-r")) {
                                z = true;
                                if (z) {
                                    description = getNextArgRequired();
                                } else if (z) {
                                    repeat = Integer.parseInt(getNextArgRequired());
                                } else if (!z) {
                                    commonOptions.check(opt);
                                } else if (amplitudesList == null) {
                                    amplitudesList = new ArrayList<>();
                                }
                            }
                        } else if (opt.equals("-d")) {
                            if (z) {
                            }
                        }
                    } else if (opt.equals("-a")) {
                        z = true;
                        if (z) {
                        }
                    }
                    z = true;
                    if (z) {
                    }
                }
                if (checkDoNotDisturb(commonOptions)) {
                    return 0;
                }
                ArrayList<Long> timingsList = new ArrayList<>();
                while (true) {
                    String arg = getNextArg();
                    if (arg == null) {
                        break;
                    } else if (amplitudesList == null || amplitudesList.size() >= timingsList.size()) {
                        timingsList.add(Long.valueOf(Long.parseLong(arg)));
                    } else {
                        amplitudesList.add(Integer.valueOf(Integer.parseInt(arg)));
                    }
                }
                long[] timings = timingsList.stream().mapToLong($$Lambda$ELHKvd8JMVRD8rbALqYPKbDX2mM.INSTANCE).toArray();
                if (amplitudesList == null) {
                    effect = VibrationEffect.createWaveform(timings, repeat);
                } else {
                    effect = VibrationEffect.createWaveform(timings, amplitudesList.stream().mapToInt($$Lambda$UV1wDVoVlbcxpr8zevj_aMFtUGw.INSTANCE).toArray(), repeat);
                }
                VibratorService.this.vibrate(Binder.getCallingUid(), description, effect, 0, "Shell Command", this.mToken);
                Trace.traceEnd(8388608);
                return 0;
            } finally {
                Trace.traceEnd(8388608);
            }
        }

        private int runPrebaked() {
            Trace.traceBegin(8388608, "runPrebaked");
            try {
                CommonOptions commonOptions = new CommonOptions();
                while (true) {
                    String opt = getNextOption();
                    if (opt == null) {
                        break;
                    }
                    commonOptions.check(opt);
                }
                if (checkDoNotDisturb(commonOptions)) {
                    return 0;
                }
                int id = Integer.parseInt(getNextArgRequired());
                String description = getNextArg();
                VibratorService.this.vibrate(Binder.getCallingUid(), description == null ? NotificationShellCmd.CHANNEL_NAME : description, VibrationEffect.get(id, false), 0, "Shell Command", this.mToken);
                Trace.traceEnd(8388608);
                return 0;
            } finally {
                Trace.traceEnd(8388608);
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:10:0x007d, code lost:
            $closeResource(r0, r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0080, code lost:
            throw r1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:8:0x007a, code lost:
            r1 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:9:0x007b, code lost:
            if (r2 != null) goto L_0x007d;
         */
        public void onHelp() {
            PrintWriter pw = getOutPrintWriter();
            pw.println("Vibrator commands:");
            pw.println("  help");
            pw.println("    Prints this help text.");
            pw.println("");
            pw.println("  vibrate duration [description]");
            pw.println("    Vibrates for duration milliseconds; ignored when device is on DND ");
            pw.println("    (Do Not Disturb) mode.");
            pw.println("  waveform [-d description] [-r index] [-a] duration [amplitude] ...");
            pw.println("    Vibrates for durations and amplitudes in list;");
            pw.println("    ignored when device is on DND (Do Not Disturb) mode.");
            pw.println("    If -r is provided, the waveform loops back to the specified");
            pw.println("    index (e.g. 0 loops from the beginning)");
            pw.println("    If -a is provided, the command accepts duration-amplitude pairs;");
            pw.println("    otherwise, it accepts durations only and alternates off/on");
            pw.println("    Duration is in milliseconds; amplitude is a scale of 1-255.");
            pw.println("  prebaked effect-id [description]");
            pw.println("    Vibrates with prebaked effect; ignored when device is on DND ");
            pw.println("    (Do Not Disturb) mode.");
            pw.println("  cancel");
            pw.println("    Cancels any active vibration");
            pw.println("Common Options:");
            pw.println("  -f - Force. Ignore Do Not Disturb setting.");
            pw.println("");
            $closeResource(null, pw);
        }
    }

    private void initOppoVibratorFeature() {
        this.mOppoVibrateFeature = OppoFeatureCache.getOrCreate(IOppoVibratorFeature.DEFAULT, new Object[]{this.mContext});
        this.mOppoVibrateFeature.setOppoVibratorCallback(this.mOppoVibratorCallback);
    }

    private void checkUnregisterScreenOffReceiver() {
        if (this.mOppoVibrateFeature.cancelScreenOffReceiver()) {
            this.mContext.unregisterReceiver(this.mIntentReceiver);
        }
    }
}
