package com.android.server;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Slog;
import com.android.internal.os.BackgroundThread;

public abstract class ColorAppActionTracker {
    private static final int MSG_BASE = 3333;
    private static final int MSG_SCREEN_OFF = 3336;
    private static final int MSG_SCREEN_ON = 3335;
    AppActionCallback mCallback = null;
    Context mContext = null;
    Handler mHandler = null;
    public long mLastScreenOffTime = 0;
    public long mLastScreenOnTime = 0;
    volatile boolean mStart = false;

    public interface AppActionCallback {
        void updateAppActionChange();
    }

    public abstract <T> T getTrackWhiteList(boolean z);

    public abstract void onStart();

    public abstract void onStop();

    public ColorAppActionTracker(Context context) {
        this.mContext = context;
        this.mHandler = new TrackHandler(BackgroundThread.get().getLooper());
    }

    public void handleScreenOn() {
    }

    public void handleScreenOff() {
    }

    public void setCallback(AppActionCallback callback) {
        this.mCallback = callback;
    }

    public void start() {
        if (this.mStart) {
            Slog.d("ColorAppActionTracker", "already start, ignore!");
            return;
        }
        this.mStart = true;
        onStart();
    }

    public void stop() {
        if (!this.mStart) {
            Slog.d("ColorAppActionTracker", "already stop, ignore!");
            return;
        }
        this.mStart = false;
        onStop();
    }

    public void screenOn() {
        this.mHandler.removeMessages(MSG_SCREEN_ON);
        this.mHandler.sendEmptyMessage(MSG_SCREEN_ON);
    }

    public void screenOff() {
        this.mHandler.removeMessages(MSG_SCREEN_OFF);
        this.mHandler.sendEmptyMessage(MSG_SCREEN_OFF);
    }

    public class TrackHandler extends Handler {
        public TrackHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            int i = message.what;
            if (i == ColorAppActionTracker.MSG_SCREEN_ON) {
                ColorAppActionTracker.this.mLastScreenOnTime = SystemClock.elapsedRealtime();
                ColorAppActionTracker.this.handleScreenOn();
            } else if (i == ColorAppActionTracker.MSG_SCREEN_OFF) {
                ColorAppActionTracker.this.mLastScreenOffTime = SystemClock.elapsedRealtime();
                ColorAppActionTracker.this.handleScreenOff();
            }
        }
    }
}
