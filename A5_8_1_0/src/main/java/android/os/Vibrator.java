package android.os;

import android.app.ActivityThread;
import android.content.Context;
import android.media.AudioAttributes;
import android.util.Log;

public abstract class Vibrator {
    private static final String TAG = "Vibrator";
    private final String mPackageName;

    public abstract void cancel();

    public abstract boolean hasAmplitudeControl();

    public abstract boolean hasVibrator();

    public abstract void vibrate(int i, String str, VibrationEffect vibrationEffect, AudioAttributes audioAttributes);

    public Vibrator() {
        this.mPackageName = ActivityThread.currentPackageName();
    }

    protected Vibrator(Context context) {
        this.mPackageName = context.getOpPackageName();
    }

    @Deprecated
    public void vibrate(long milliseconds) {
        vibrate(milliseconds, null);
    }

    @Deprecated
    public void vibrate(long milliseconds, AudioAttributes attributes) {
        try {
            vibrate(VibrationEffect.createOneShot(milliseconds, -1), attributes);
        } catch (IllegalArgumentException iae) {
            Log.e(TAG, "Failed to create VibrationEffect", iae);
        }
    }

    @Deprecated
    public void vibrate(long[] pattern, int repeat) {
        vibrate(pattern, repeat, null);
    }

    @Deprecated
    public void vibrate(long[] pattern, int repeat, AudioAttributes attributes) {
        if (repeat < -1 || repeat >= pattern.length) {
            Log.e(TAG, "vibrate called with repeat index out of bounds (pattern.length=" + pattern.length + ", index=" + repeat + ")");
            throw new ArrayIndexOutOfBoundsException();
        }
        try {
            vibrate(VibrationEffect.createWaveform(pattern, repeat), attributes);
        } catch (IllegalArgumentException iae) {
            Log.e(TAG, "Failed to create VibrationEffect", iae);
        }
    }

    public void vibrate(VibrationEffect vibe) {
        vibrate(vibe, null);
    }

    public void vibrate(VibrationEffect vibe, AudioAttributes attributes) {
        vibrate(Process.myUid(), this.mPackageName, vibe, attributes);
    }
}
