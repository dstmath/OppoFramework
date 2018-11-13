package com.android.server.am;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Slog;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

public class LockTaskNotify {
    private static final long SHOW_TOAST_MINIMUM_INTERVAL = 1000;
    private static final String TAG = "LockTaskNotify";
    private final Context mContext;
    private final H mHandler = new H(this, null);
    private long mLastShowToastTime;
    private Toast mLastToast;

    private final class H extends Handler {
        private static final int SHOW_TOAST = 3;

        /* synthetic */ H(LockTaskNotify this$0, H -this1) {
            this();
        }

        private H() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 3:
                    LockTaskNotify.this.handleShowToast(msg.arg1);
                    return;
                default:
                    return;
            }
        }
    }

    public LockTaskNotify(Context context) {
        this.mContext = context;
    }

    public void showToast(int lockTaskModeState) {
        this.mHandler.obtainMessage(3, lockTaskModeState, 0).sendToTarget();
    }

    public void handleShowToast(int lockTaskModeState) {
        String text = null;
        if (lockTaskModeState == 1) {
            text = this.mContext.getString(17040154);
        } else if (lockTaskModeState == 2) {
            text = this.mContext.getString(17040153);
        }
        if (text != null) {
            long showToastTime = SystemClock.elapsedRealtime();
            if (showToastTime - this.mLastShowToastTime < 1000) {
                Slog.i(TAG, "Ignore toast since it is requested in very short interval.");
                return;
            }
            if (this.mLastToast != null) {
                this.mLastToast.cancel();
            }
            this.mLastToast = makeAllUserToastAndShow(text);
            this.mLastShowToastTime = showToastTime;
        }
    }

    public void show(boolean starting) {
        int showString = 17040151;
        if (starting) {
            showString = 17040152;
        }
        makeAllUserToastAndShow(this.mContext.getString(showString));
    }

    private Toast makeAllUserToastAndShow(String text) {
        Toast toast = Toast.makeText(this.mContext, text, 1);
        LayoutParams windowParams = toast.getWindowParams();
        windowParams.privateFlags |= 16;
        toast.show();
        return toast;
    }
}
