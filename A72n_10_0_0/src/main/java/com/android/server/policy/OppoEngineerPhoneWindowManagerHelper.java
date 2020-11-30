package com.android.server.policy;

import android.engineer.OppoEngineerManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

/* access modifiers changed from: package-private */
public class OppoEngineerPhoneWindowManagerHelper {
    private static final boolean DEBUG = false;
    private static final int HARDWARE_RESET_RECORD_FLAG_INDEX = 77;
    private static final int MARK_PWK_PRESSED_DELAY_IN_MILLIS = 2000;
    private static final int MSG_PWK_PRESSED = 100001;
    private static final int MSG_PWK_RELEASED = 100002;
    private static final String PWK_HARD_SHUTDOWN_THREAD_NAME = "ENG_PWK_MONITOR";
    private static final String TAG = OppoEngineerPhoneWindowManagerHelper.class.getSimpleName();
    private PwkMonitorHandler mHandler;
    private final Object mLock = new Object();
    private int mPwkHardShutdownCount = -1;

    OppoEngineerPhoneWindowManagerHelper() {
        HandlerThread monitorThread = new HandlerThread(PWK_HARD_SHUTDOWN_THREAD_NAME);
        monitorThread.start();
        this.mHandler = new PwkMonitorHandler(monitorThread.getLooper());
    }

    /* access modifiers changed from: package-private */
    public void onPwkPressed() {
        Message msg = this.mHandler.obtainMessage(MSG_PWK_PRESSED);
        msg.setAsynchronous(true);
        this.mHandler.sendMessageDelayed(msg, 2000);
    }

    /* access modifiers changed from: package-private */
    public void onPwkReleased() {
        this.mHandler.sendEmptyMessage(MSG_PWK_RELEASED);
    }

    /* access modifiers changed from: private */
    public class PwkMonitorHandler extends Handler {
        PwkMonitorHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case OppoEngineerPhoneWindowManagerHelper.MSG_PWK_PRESSED /* 100001 */:
                    synchronized (OppoEngineerPhoneWindowManagerHelper.this.mLock) {
                        if (OppoEngineerPhoneWindowManagerHelper.this.mPwkHardShutdownCount == -1) {
                            byte[] result = OppoEngineerManager.getProductLineTestResult();
                            if (result == null || result.length <= 77) {
                                OppoEngineerPhoneWindowManagerHelper.this.mPwkHardShutdownCount = -1;
                            } else {
                                OppoEngineerPhoneWindowManagerHelper.this.mPwkHardShutdownCount = result[77];
                                if (OppoEngineerPhoneWindowManagerHelper.this.mPwkHardShutdownCount >= 127 || OppoEngineerPhoneWindowManagerHelper.this.mPwkHardShutdownCount < 0) {
                                    OppoEngineerPhoneWindowManagerHelper.this.mPwkHardShutdownCount = 0;
                                }
                            }
                        }
                        if (OppoEngineerPhoneWindowManagerHelper.this.mPwkHardShutdownCount != -1) {
                            OppoEngineerManager.setProductLineTestResult(77, OppoEngineerPhoneWindowManagerHelper.this.mPwkHardShutdownCount + 1);
                        }
                    }
                    return;
                case OppoEngineerPhoneWindowManagerHelper.MSG_PWK_RELEASED /* 100002 */:
                    if (hasMessages(OppoEngineerPhoneWindowManagerHelper.MSG_PWK_PRESSED)) {
                        removeMessages(OppoEngineerPhoneWindowManagerHelper.MSG_PWK_PRESSED);
                        return;
                    }
                    synchronized (OppoEngineerPhoneWindowManagerHelper.this.mLock) {
                        if (OppoEngineerPhoneWindowManagerHelper.this.mPwkHardShutdownCount != -1) {
                            OppoEngineerManager.setProductLineTestResult(77, OppoEngineerPhoneWindowManagerHelper.this.mPwkHardShutdownCount);
                        }
                    }
                    return;
                default:
                    return;
            }
        }
    }
}
