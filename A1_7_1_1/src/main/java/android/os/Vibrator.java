package android.os;

import android.app.ActivityThread;
import android.content.Context;
import android.media.AudioAttributes;

public abstract class Vibrator {
    private final String mPackageName;

    public abstract void cancel();

    public abstract boolean hasVibrator();

    public abstract void vibrate(int i, String str, long j, AudioAttributes audioAttributes);

    public abstract void vibrate(int i, String str, long[] jArr, int i2, AudioAttributes audioAttributes);

    public Vibrator() {
        this.mPackageName = ActivityThread.currentPackageName();
    }

    protected Vibrator(Context context) {
        this.mPackageName = context.getOpPackageName();
    }

    public void vibrate(long milliseconds) {
        vibrate(milliseconds, null);
    }

    public void vibrate(long milliseconds, AudioAttributes attributes) {
        vibrate(Process.myUid(), this.mPackageName, milliseconds, attributes);
    }

    public void vibrate(long[] pattern, int repeat) {
        vibrate(pattern, repeat, null);
    }

    public void vibrate(long[] pattern, int repeat, AudioAttributes attributes) {
        vibrate(Process.myUid(), this.mPackageName, pattern, repeat, attributes);
    }
}
