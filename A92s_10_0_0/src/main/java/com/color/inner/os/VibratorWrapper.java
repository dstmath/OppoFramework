package com.color.inner.os;

import android.media.AudioAttributes;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import com.color.os.ColorVibratorConstant;
import java.lang.reflect.InvocationTargetException;

public class VibratorWrapper {
    public static final long LONG_MIDDLE_ONESHOT_TIME = 150;
    public static final long LONG_STRONG_ONESHOT_TIME = 400;
    public static final int MIDDLE_AMPLITUDE = 175;
    public static final long RAPID_MIDDLE_ONESHOT_TIME = 50;
    public static final int[] RAPID_MIDDLE_WAVEFORM_AMPLITUDE = ColorVibratorConstant.RAPID_MIDDLE_WAVEFORM_AMPLITUDE;
    public static final long[] RAPID_MIDDLE_WAVEFORM_TIME = ColorVibratorConstant.RAPID_MIDDLE_WAVEFORM_TIME;
    public static final int[] RAPID_STRONG_WAVEFORM_AMPLITUDE = ColorVibratorConstant.RAPID_STRONG_WAVEFORM_AMPLITUDE;
    public static final long[] RAPID_STRONG_WAVEFORM_TIME = ColorVibratorConstant.RAPID_STRONG_WAVEFORM_TIME;
    public static final long RAPID_WEAK_ONESHOT_TIME = 25;
    public static final int STRONG_AMPLITUDE = 250;
    private static final String TAG = "VibratorWrapper";
    public static final int WEAK_AMPLITUDE = 100;

    private VibratorWrapper() {
    }

    public static void linerMotorVibrate(Vibrator vibrator, VibrationEffect effect, AudioAttributes attributes) {
        try {
            Class.forName("android.os.Vibrator").getDeclaredMethod("linerMotorVibrate", VibrationEffect.class, AudioAttributes.class).invoke(vibrator, effect, attributes);
        } catch (NoSuchMethodException e) {
            Log.i(TAG, "NoSuchMethodException " + e.getStackTrace().toString());
        } catch (IllegalAccessException e2) {
            Log.i(TAG, "IllegalAccessException " + e2.getStackTrace().toString());
        } catch (InvocationTargetException e3) {
            Log.i(TAG, "InvocationTargetException " + e3.getStackTrace().toString());
        } catch (ClassNotFoundException e4) {
            Log.i(TAG, "ClassNotFoundException " + e4.getStackTrace().toString());
        }
    }
}
