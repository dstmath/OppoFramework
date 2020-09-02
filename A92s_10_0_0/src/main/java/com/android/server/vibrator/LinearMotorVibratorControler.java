package com.android.server.vibrator;

import android.content.Context;
import android.os.OppoMirrorLinearMotorVibratorManager;
import android.os.OppoNativeOneShotVibrationEffect;
import android.os.OppoNativeWaveformVibrationEffect;
import android.os.Process;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.VibrationEffect;
import android.util.Slog;
import com.android.server.IOppoVibratorCallback;
import com.oppo.reflect.RefMethod;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import oppo.util.OppoStatistics;

public class LinearMotorVibratorControler {
    public static final int DEFAULT_LINEAR_MOTOR_AMPLITUDE = 175;
    private static final long ONE_DAY = 86400;
    private static final String TAG = "LinearMotorVibratorControler";
    private static boolean mOppoDebug = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private Context mContext = null;
    private long mLastReportDcsTime = 0;
    private volatile LinearMotorVibrateThread mLinearMotorVibrateThread;
    private HashMap<String, DcsVibrationData> mReportDcsDataMap = new HashMap<>();
    /* access modifiers changed from: private */
    public IOppoVibratorCallback mVibratorCallback = null;

    public LinearMotorVibratorControler(Context context) {
        this.mContext = context;
    }

    public void setOppoVibratorCallback(IOppoVibratorCallback callback) {
        this.mVibratorCallback = callback;
    }

    public void cancelLMThread() {
        if (this.mLinearMotorVibrateThread != null) {
            this.mLinearMotorVibrateThread.cancel();
            this.mLinearMotorVibrateThread = null;
        }
    }

    public boolean startCustomizeVibratorImplLocked(VibrationEffect effect, int vibUid, int vibUsageHint) {
        if (effect == null) {
            Slog.e(TAG, "startCustomizeVibratorImpl: illegal effect!");
            return false;
        } else if (effect instanceof OppoNativeOneShotVibrationEffect) {
            Trace.asyncTraceBegin(8388608, "vibration", 0);
            OppoNativeOneShotVibrationEffect oneShot = (OppoNativeOneShotVibrationEffect) effect;
            doLinearMotorVibratorOn(oneShot.getId(), oneShot.getDuration(), oneShot.getEffectStrength(), vibUid, vibUsageHint);
            IOppoVibratorCallback iOppoVibratorCallback = this.mVibratorCallback;
            if (iOppoVibratorCallback != null) {
                iOppoVibratorCallback.onVibrationEndLocked(effect.getDuration());
            }
            return true;
        } else if (!(effect instanceof OppoNativeWaveformVibrationEffect)) {
            return false;
        } else {
            Trace.asyncTraceBegin(8388608, "vibration", 0);
            this.mLinearMotorVibrateThread = new LinearMotorVibrateThread((OppoNativeWaveformVibrationEffect) effect, vibUid, vibUsageHint);
            this.mLinearMotorVibrateThread.start();
            return true;
        }
    }

    public void turnOffLinearMotorVibratorImpl() {
        try {
            if (OppoMirrorLinearMotorVibratorManager.turnOffLinearMotorVibrator != null) {
                OppoMirrorLinearMotorVibratorManager.turnOffLinearMotorVibrator.call((Object) null, new Object[0]);
            } else {
                Slog.e(TAG, "turnOffLinearMotorVibratorImpl failed for method empty.");
            }
        } catch (Exception e) {
            Slog.e(TAG, "turnOffLinearMotorVibratorImpl failed!", e);
        }
    }

    /* access modifiers changed from: private */
    public void doLinearMotorVibratorOn(int waveformId, long timing, int strength, int uid, int usageHint) {
        Trace.traceBegin(8388608, "doLinearMotorVibratorOn");
        try {
            if (this.mVibratorCallback != null) {
                try {
                } catch (Exception e) {
                    e = e;
                    try {
                        Slog.e(TAG, "linearmotorVibratorOn failed.", e);
                        Trace.traceEnd(8388608);
                    } catch (Throwable th) {
                        th = th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    Trace.traceEnd(8388608);
                    throw th;
                }
                try {
                    this.mVibratorCallback.onNoteVibratorOnLocked(uid, timing);
                } catch (Exception e2) {
                    e = e2;
                    Slog.e(TAG, "linearmotorVibratorOn failed.", e);
                    Trace.traceEnd(8388608);
                } catch (Throwable th3) {
                    th = th3;
                    Trace.traceEnd(8388608);
                    throw th;
                }
            }
            boolean usingInputDeviceVibrators = true;
            if (this.mVibratorCallback != null) {
                usingInputDeviceVibrators = !this.mVibratorCallback.isInputDeviceVibratorsEmpty();
            }
            if (mOppoDebug) {
                StringBuilder sb = new StringBuilder();
                sb.append("doLinearMotorVibratorOn ");
                try {
                    sb.append(waveformId);
                    sb.append(",");
                    sb.append(timing);
                    sb.append(",");
                    sb.append(strength);
                    Slog.d(TAG, sb.toString());
                } catch (Exception e3) {
                    e = e3;
                    Slog.e(TAG, "linearmotorVibratorOn failed.", e);
                    Trace.traceEnd(8388608);
                } catch (Throwable th4) {
                    th = th4;
                    Trace.traceEnd(8388608);
                    throw th;
                }
            }
            if (!usingInputDeviceVibrators) {
                if (OppoMirrorLinearMotorVibratorManager.turnOnLinearmotorVibrator != null) {
                    RefMethod refMethod = OppoMirrorLinearMotorVibratorManager.turnOnLinearmotorVibrator;
                    Object[] objArr = new Object[3];
                    objArr[0] = Integer.valueOf(waveformId);
                    objArr[1] = Short.valueOf(convertEffectStrength(strength));
                    try {
                        objArr[2] = Boolean.valueOf(useRtpMode(usageHint));
                        refMethod.call((Object) null, objArr);
                    } catch (Exception e4) {
                        e = e4;
                        Slog.e(TAG, "linearmotorVibratorOn failed.", e);
                        Trace.traceEnd(8388608);
                    }
                } else {
                    Slog.e(TAG, "doLinearMotorVibratorOn failed for method empty.");
                }
            }
        } catch (Exception e5) {
            e = e5;
            Slog.e(TAG, "linearmotorVibratorOn failed.", e);
            Trace.traceEnd(8388608);
        } catch (Throwable th5) {
            th = th5;
            Trace.traceEnd(8388608);
            throw th;
        }
        Trace.traceEnd(8388608);
    }

    private boolean useRtpMode(int usageHint) {
        return usageHint == 14 || usageHint == 6 || usageHint == 4;
    }

    private short convertEffectStrength(int strength) {
        if (strength == 0) {
            return 800;
        }
        if (strength == 1) {
            return 1600;
        }
        if (strength != 2) {
            return 1;
        }
        return 2400;
    }

    private static class DcsVibrationData {
        private HashSet<Integer> mAmplitudes = new HashSet<>();
        private String mPkg;

        DcsVibrationData(String pkg) {
            this.mPkg = pkg;
        }

        /* access modifiers changed from: package-private */
        public void recordAmplitude(int... amplitude) {
            for (int amp : amplitude) {
                this.mAmplitudes.add(Integer.valueOf(amp));
            }
        }

        public String toString() {
            return "{packageName:" + this.mPkg + ",amplitudes:" + Arrays.toString(this.mAmplitudes.toArray(new Integer[0])) + "}";
        }
    }

    public void reportVibrationToDcsIfNeed(int uid, String pkg, int... amplitude) {
        if (UserHandle.isApp(uid)) {
            DcsVibrationData data = this.mReportDcsDataMap.get(pkg);
            if (data == null) {
                data = new DcsVibrationData(pkg);
                this.mReportDcsDataMap.put(pkg, data);
            }
            data.recordAmplitude(amplitude);
            long now = SystemClock.uptimeMillis();
            if ((now - this.mLastReportDcsTime) / 1000 > ONE_DAY) {
                notifyDCS();
                this.mLastReportDcsTime = now;
                this.mReportDcsDataMap.clear();
            }
        }
    }

    private void notifyDCS() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (DcsVibrationData data : this.mReportDcsDataMap.values()) {
            sb.append(data.toString());
            sb.append(",");
        }
        sb.delete(sb.length() - 1, sb.length());
        sb.append("]");
        HashMap<String, String> map = new HashMap<>();
        map.put("vibrations", sb.toString());
        OppoStatistics.onCommon(this.mContext, "20089", "event_android_vibrate_key", map, false);
    }

    private class LinearMotorVibrateThread extends Thread {
        private boolean mForceStop;
        private final int mUid;
        private final int mUsageHint;
        private final OppoNativeWaveformVibrationEffect mWaveform;

        LinearMotorVibrateThread(OppoNativeWaveformVibrationEffect waveform, int uid, int usageHint) {
            this.mWaveform = waveform;
            this.mUid = uid;
            this.mUsageHint = usageHint;
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
            if (LinearMotorVibratorControler.this.mVibratorCallback != null) {
                LinearMotorVibratorControler.this.mVibratorCallback.onAcquireVibratorWakelock(this.mUid);
                try {
                    if (playWaveform()) {
                        LinearMotorVibratorControler.this.mVibratorCallback.informVibrationFinished();
                    }
                } finally {
                    LinearMotorVibratorControler.this.mVibratorCallback.onReleaseVibratorWakelock();
                }
            } else {
                Slog.e(LinearMotorVibratorControler.TAG, "can't play vibrator before vibrator callback init!!!");
            }
        }

        public boolean playWaveform() {
            boolean z;
            long duration;
            Trace.traceBegin(8388608, "playWaveform");
            try {
                synchronized (this) {
                    long[] timings = this.mWaveform.getTimings();
                    int[] waveformIds = this.mWaveform.getWaveformIds();
                    int len = timings.length;
                    int repeat = this.mWaveform.getRepeatIndex();
                    z = false;
                    int index = 0;
                    while (true) {
                        if (this.mForceStop) {
                            break;
                        } else if (index < len) {
                            int waveformId = waveformIds[index];
                            int index2 = index + 1;
                            long duration2 = timings[index];
                            if (duration2 > 0) {
                                if (waveformId != -1) {
                                    duration = duration2;
                                    LinearMotorVibratorControler.this.doLinearMotorVibratorOn(waveformId, duration2, this.mWaveform.getEffectStrength(), this.mUid, this.mUsageHint);
                                } else {
                                    duration = duration2;
                                }
                                delayLocked(duration);
                            }
                            index = index2;
                        } else if (repeat < 0) {
                            break;
                        } else {
                            index = repeat;
                        }
                    }
                    if (!this.mForceStop) {
                        z = true;
                    }
                }
                return z;
            } finally {
                Trace.traceEnd(8388608);
            }
        }

        public void cancel() {
            synchronized (this) {
                this.mForceStop = true;
                notify();
            }
        }
    }

    public void updateLogTagSwitch(boolean enableLog) {
        mOppoDebug = enableLog;
    }
}
