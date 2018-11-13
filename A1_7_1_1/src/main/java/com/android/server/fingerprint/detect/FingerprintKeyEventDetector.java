package com.android.server.fingerprint.detect;

import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.android.server.fingerprint.detect.data.FingerprintDiedData;
import com.android.server.fingerprint.detect.data.FingerprintKeyEventData;
import com.android.server.fingerprint.detect.data.FingerprintResetData;
import com.android.server.fingerprint.detect.data.HardwareErrorData;
import com.android.server.fingerprint.tool.ExHandler;
import java.util.concurrent.ConcurrentLinkedDeque;

public class FingerprintKeyEventDetector implements IFingerprintKeyEventListener {
    /* renamed from: -com-android-server-fingerprint-detect-FingerprintKeyEventTypeSwitchesValues */
    private static final /* synthetic */ int[] f14x1e1d3b2f = null;
    public static final int MSG_KEYEVENT_HAPPEND = 0;
    public static final long TIMEOUT_DEAL_KEYEVENT_HAPPEND = 500;
    private String TAG = "FingerprintKeyEventDetector";
    private ExHandler mHandler;
    ConcurrentLinkedDeque<FingerprintKeyEventData> mKeyEventDataQueue = new ConcurrentLinkedDeque();

    /* renamed from: -getcom-android-server-fingerprint-detect-FingerprintKeyEventTypeSwitchesValues */
    private static /* synthetic */ int[] m27x15ba55d3() {
        if (f14x1e1d3b2f != null) {
            return f14x1e1d3b2f;
        }
        int[] iArr = new int[FingerprintKeyEventType.values().length];
        try {
            iArr[FingerprintKeyEventType.FINGERPRINTD_DIED.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[FingerprintKeyEventType.FINGERPRINTD_RESET_BY_HEALTHMONITOR.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[FingerprintKeyEventType.HARDWARE_ERROR_REPORT.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[FingerprintKeyEventType.NONE.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        f14x1e1d3b2f = iArr;
        return iArr;
    }

    public FingerprintKeyEventDetector(Looper looper) {
        initHandler(looper);
    }

    public void onFingerprintdDied(long time, int pid) {
        this.mKeyEventDataQueue.add(new FingerprintDiedData(FingerprintKeyEventType.FINGERPRINTD_DIED, time, pid));
        if (!this.mHandler.hasMessages(0)) {
            this.mHandler.sendEmptyMessageDelayed(0, 500);
        }
    }

    public void onFingerprintdResetByHealthMonitor(long time, int pid) {
        this.mKeyEventDataQueue.add(new FingerprintResetData(FingerprintKeyEventType.FINGERPRINTD_RESET_BY_HEALTHMONITOR, time, pid));
        if (!this.mHandler.hasMessages(0)) {
            this.mHandler.sendEmptyMessageDelayed(0, 500);
        }
    }

    public void onHardwareErrorReport(long time, int pid, int errorCode) {
        this.mKeyEventDataQueue.add(new HardwareErrorData(FingerprintKeyEventType.HARDWARE_ERROR_REPORT, time, pid, errorCode));
        if (!this.mHandler.hasMessages(0)) {
            this.mHandler.sendEmptyMessageDelayed(0, 500);
        }
    }

    private void dealHardwareErrorReport(HardwareErrorData keyeventData) {
    }

    private void dealFingerprintResetReport(FingerprintResetData keyeventData) {
    }

    private void dealFingerprindDiedReport(FingerprintDiedData keyeventData) {
    }

    private void dealKeyEventHappenend(FingerprintKeyEventData keyeventData) {
        switch (m27x15ba55d3()[keyeventData.getKeyEventType().ordinal()]) {
            case 1:
                dealFingerprindDiedReport((FingerprintDiedData) keyeventData);
                return;
            case 2:
                dealFingerprintResetReport((FingerprintResetData) keyeventData);
                return;
            case 3:
                dealHardwareErrorReport((HardwareErrorData) keyeventData);
                return;
            default:
                return;
        }
    }

    private void initHandler(Looper looper) {
        this.mHandler = new ExHandler(looper) {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        Log.d(FingerprintKeyEventDetector.this.TAG, "mKeyEventDataQueue.size = " + FingerprintKeyEventDetector.this.mKeyEventDataQueue.size());
                        FingerprintKeyEventData keyeventData = (FingerprintKeyEventData) FingerprintKeyEventDetector.this.mKeyEventDataQueue.poll();
                        if (keyeventData != null) {
                            FingerprintKeyEventDetector.this.dealKeyEventHappenend(keyeventData);
                        }
                        if (!FingerprintKeyEventDetector.this.mKeyEventDataQueue.isEmpty()) {
                            FingerprintKeyEventDetector.this.mHandler.sendEmptyMessageDelayed(0, 500);
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        };
    }
}
