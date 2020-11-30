package com.android.server.vibrator;

import android.content.Context;
import android.os.OppoNativeOneShotVibrationEffect;
import android.os.OppoNativeWaveformVibrationEffect;
import android.os.SystemProperties;
import android.os.VibrationEffect;
import android.util.Slog;
import com.android.server.IOppoVibratorCallback;
import com.android.server.IOppoVibratorFeature;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import java.io.PrintWriter;
import java.util.Arrays;

public class OppoVibratorFeature implements IOppoVibratorFeature {
    public static final String FEATURE_WAVEFORM_VIBRATOR = "oppo.feature.vibrator.waveform.support";
    private static final String TAG = "OppoVibratorFeature";
    private static boolean mOppoDebug = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static OppoVibratorFeature sOppoVibratorFeature = null;
    private Context mContext;
    private boolean mCurrentVibReadyStop;
    private boolean mInited;
    private boolean mIsSupportWaveformVibrator;
    private LinearMotorVibratorControler mLMVibratorControler;
    private IOppoVibratorCallback mOppoVibratorCallback;

    private OppoVibratorFeature() {
        this.mCurrentVibReadyStop = false;
        this.mLMVibratorControler = null;
        this.mOppoVibratorCallback = null;
        this.mIsSupportWaveformVibrator = false;
        this.mInited = false;
    }

    private static class InstanceHolder {
        static final OppoVibratorFeature INSTANCE = new OppoVibratorFeature();

        private InstanceHolder() {
        }
    }

    public static OppoVibratorFeature getInstance(Context context) {
        if (mOppoDebug) {
            Slog.d(TAG, "getInstance.");
        }
        OppoVibratorFeature instance = InstanceHolder.INSTANCE;
        instance.init(context);
        return instance;
    }

    private void init(Context context) {
        if (!this.mInited) {
            this.mContext = context;
            this.mIsSupportWaveformVibrator = this.mContext.getPackageManager().hasSystemFeature(FEATURE_WAVEFORM_VIBRATOR);
            if (mOppoDebug) {
                Slog.d(TAG, "OppoVibratorFeature init. mIsSupportWaveformVibrator:" + this.mIsSupportWaveformVibrator);
            }
            if (this.mIsSupportWaveformVibrator) {
                this.mLMVibratorControler = new LinearMotorVibratorControler(this.mContext);
            }
            this.mInited = true;
        }
    }

    public void setOppoVibratorCallback(IOppoVibratorCallback callback) {
        if (mOppoDebug) {
            Slog.d("IOppoVibratorFeature", "impl setOppoVibratorCallback. support:" + this.mIsSupportWaveformVibrator);
        }
        this.mOppoVibratorCallback = callback;
        if (this.mIsSupportWaveformVibrator) {
            this.mLMVibratorControler.setOppoVibratorCallback(this.mOppoVibratorCallback);
        }
    }

    public void logVibratorPatterns(long[] timings, int[] amplitudes, int len) {
        if (mOppoDebug) {
            Slog.d(TAG, "impl logVibratorPatterns.");
        }
        if (mOppoDebug && len == amplitudes.length) {
            String patterns = StringUtils.EMPTY;
            for (int i = 0; i < timings.length; i++) {
                patterns = patterns + StringUtils.SPACE + timings[i] + StringUtils.SPACE + amplitudes[i];
            }
            if (mOppoDebug) {
                Slog.d(TAG, "Vibrating with patterns: " + patterns);
            }
        }
    }

    public void dynamicallyConfigLogTag(PrintWriter pw, String[] args) {
        if (mOppoDebug) {
            Slog.d(TAG, "impl dynamicallyConfigLogTag");
        }
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
        boolean on = "1".equals(args[2]);
        pw.println("dynamicallyConfigLogTag, tag: " + tag + ", on: " + on);
        if ("all".equals(tag)) {
            mOppoDebug = on;
            IOppoVibratorCallback iOppoVibratorCallback = this.mOppoVibratorCallback;
            if (iOppoVibratorCallback != null) {
                iOppoVibratorCallback.onDebugFlagSwitch(on);
            }
            if (this.mIsSupportWaveformVibrator) {
                this.mLMVibratorControler.updateLogTagSwitch(on);
            }
        }
    }

    public boolean cancelScreenOffReceiver() {
        if (!mOppoDebug) {
            return true;
        }
        Slog.d(TAG, "impl cancelScreenOffReceiver");
        return true;
    }

    public boolean ignoreVibrateForOneShotEffect(VibrationEffect curVibEffect, VibrationEffect newEffect) {
        if (mOppoDebug) {
            Slog.d(TAG, "impl ignoreVibrateForOneShotEffect. support:" + this.mIsSupportWaveformVibrator);
        }
        if (!this.mIsSupportWaveformVibrator) {
            return false;
        }
        if (curVibEffect == null || newEffect == null) {
            Slog.w(TAG, "impl ignoreVibrateForOneShotEffect: illegel effect!");
            return false;
        } else if (!(newEffect instanceof OppoNativeOneShotVibrationEffect) || !(curVibEffect instanceof OppoNativeOneShotVibrationEffect) || !hasTimeoutLongerThan(((OppoNativeOneShotVibrationEffect) newEffect).getDuration(), curVibEffect)) {
            return false;
        } else {
            if (!mOppoDebug) {
                return true;
            }
            Slog.d(TAG, "Ignoring incoming vibration in favor of current vibration");
            return true;
        }
    }

    public void updateOppoVibratorStopStatus(boolean isReadyToStop) {
        if (mOppoDebug) {
            Slog.d(TAG, "impl updateOppoVibratorStopStatus");
        }
        this.mCurrentVibReadyStop = isReadyToStop;
    }

    public boolean isReadyToStopVibrator() {
        if (mOppoDebug) {
            Slog.d(TAG, "impl isReadyToStopVibrator");
        }
        return this.mCurrentVibReadyStop;
    }

    public VibrationEffect applyLinearMotorVibrator(int uid, String opPkg, VibrationEffect effect) {
        if (mOppoDebug) {
            Slog.d(TAG, "impl applyLinearMotorVibrator. support:" + this.mIsSupportWaveformVibrator);
        }
        if (!this.mIsSupportWaveformVibrator) {
            return null;
        }
        if (effect instanceof VibrationEffect.OneShot) {
            this.mLMVibratorControler.reportVibrationToDcsIfNeed(uid, opPkg, ((VibrationEffect.OneShot) effect).getAmplitude());
            return VibrationEffect.createOneShot(effect.getDuration(), LinearMotorVibratorControler.DEFAULT_LINEAR_MOTOR_AMPLITUDE);
        } else if (!(effect instanceof VibrationEffect.Waveform)) {
            return null;
        } else {
            VibrationEffect.Waveform waveform = (VibrationEffect.Waveform) effect;
            this.mLMVibratorControler.reportVibrationToDcsIfNeed(uid, opPkg, waveform.getAmplitudes());
            int repeat = waveform.getRepeatIndex();
            if (repeat != -1) {
                return null;
            }
            int[] adjustedAmplitudes = Arrays.copyOf(waveform.getAmplitudes(), waveform.getAmplitudes().length);
            for (int i = 0; i < adjustedAmplitudes.length; i++) {
                if (adjustedAmplitudes[i] != 0) {
                    adjustedAmplitudes[i] = 175;
                }
            }
            return VibrationEffect.createWaveform(waveform.getTimings(), adjustedAmplitudes, repeat);
        }
    }

    public void cancelLinearMotorVibrator() {
        if (mOppoDebug) {
            Slog.d(TAG, "impl cancelLinearMotorVibrator. support:" + this.mIsSupportWaveformVibrator);
        }
        if (this.mIsSupportWaveformVibrator) {
            this.mLMVibratorControler.cancelLMThread();
        }
    }

    public boolean startCustomizeVibratorLocked(VibrationEffect effect, int vibUid, int vibUsageHint) {
        if (mOppoDebug) {
            Slog.d(TAG, "impl startCustomizeVibrator. support:" + this.mIsSupportWaveformVibrator);
        }
        if (!this.mIsSupportWaveformVibrator) {
            return false;
        }
        return this.mLMVibratorControler.startCustomizeVibratorImplLocked(effect, vibUid, vibUsageHint);
    }

    public boolean isOppoNativeVibrationEffect(VibrationEffect effect) {
        if (mOppoDebug) {
            Slog.d(TAG, "impl isOppoNativeVibrationEffect");
        }
        if (effect == null) {
            return false;
        }
        if ((effect instanceof OppoNativeWaveformVibrationEffect) || (effect instanceof OppoNativeOneShotVibrationEffect)) {
            return true;
        }
        return false;
    }

    public boolean isOppoNativeWaveformEffect(VibrationEffect effect) {
        if (mOppoDebug) {
            Slog.d(TAG, "impl isOppoNativeWaveformEffect");
        }
        if (effect == null) {
            return false;
        }
        return effect instanceof OppoNativeWaveformVibrationEffect;
    }

    public void turnOffLinearMotorVibrator() {
        LinearMotorVibratorControler linearMotorVibratorControler;
        if (mOppoDebug) {
            Slog.d(TAG, "impl turnOffLinearMotorVibrator. support:" + this.mIsSupportWaveformVibrator);
        }
        if (this.mIsSupportWaveformVibrator && (linearMotorVibratorControler = this.mLMVibratorControler) != null) {
            linearMotorVibratorControler.turnOffLinearMotorVibratorImpl();
        }
    }

    private void logoutTagConfigHelp(PrintWriter pw) {
        pw.println("********************** Help begin:**********************");
        pw.println("1. open all log in VibratorService");
        pw.println("cmd: dumpsys vibrator log all 0/1");
        pw.println("----------------------------------");
        pw.println("********************** Help end.  **********************");
    }

    private boolean hasTimeoutLongerThan(long millis, VibrationEffect effect) {
        long duration = effect.getDuration();
        return duration >= 0 && duration > millis;
    }
}
