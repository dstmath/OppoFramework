package com.android.server.fingerprint;

import android.os.Message;
import android.os.Process;
import android.os.SystemProperties;
import com.android.server.ServiceThread;
import com.android.server.fingerprint.tool.ExHandler;
import com.android.server.fingerprint.util.LogUtil;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;

public class HealthMonitor {
    public static final String[] FINGERPRINTD_NATIVE_NAME = new String[]{"/vendor/bin/hw/vendor.oppo.hardware.biometrics.fingerprint@2.1-service"};
    public static final int MSG_BINDERCALL_CHECK = 0;
    private String TAG = "FingerprintService.HealthMonitor";
    public HashMap<String, String> mActiveApiVector = new HashMap();
    private ExHandler mHandler;
    private boolean mIsFingerprintdRunning = false;
    private boolean mIsTestWorking = false;
    private int mLastBinderCallFingerprintdPid;
    private String mMonitorName;
    private int mMonitoringPid = -1;
    private String mPropFingerprintdBingercall = "debug.bindercall.api";
    private ServiceThread mServiceThread;
    private volatile boolean mStopChecking = false;

    public HealthMonitor(String monitorName) {
        this.mMonitorName = monitorName;
        this.mServiceThread = new ServiceThread(this.TAG, 10, true);
        this.mServiceThread.start();
        initHandler();
    }

    public void fingerprintdSystemReady(int fingerprintdPid) {
        this.mIsFingerprintdRunning = true;
        this.mMonitoringPid = fingerprintdPid;
        LogUtil.d(this.TAG, "fingerprintdSystemReady mMonitoringPid = " + this.mMonitoringPid);
        if (fingerprintdPid == -1) {
            this.mMonitoringPid = getFingerprintdPid();
        }
    }

    public void notifyFingerprintdDied() {
        LogUtil.d(this.TAG, "notifyFingerprintdDied");
        this.mIsFingerprintdRunning = false;
        this.mMonitoringPid = -1;
        this.mActiveApiVector.clear();
        this.mHandler.removeMessages(0);
    }

    public void start(String startApiName, long delay, String session) {
        if (!this.mIsFingerprintdRunning || this.mMonitoringPid == -1) {
            LogUtil.e(this.TAG, "fingerprintd is restarting, startApiName = " + startApiName + ", session = " + session);
            return;
        }
        this.mStopChecking = false;
        LogUtil.d(this.TAG, "start " + startApiName + ", session = " + session + ", delay = " + delay + ", mMonitoringPid = " + this.mMonitoringPid);
        this.mActiveApiVector.put(session, startApiName);
        Message msg = this.mHandler.obtainMessage(0);
        msg.obj = session;
        msg.arg1 = this.mMonitoringPid;
        this.mHandler.sendMessageDelayed(msg, delay);
    }

    public void stop(String stopApiName, String session) {
        if (!this.mIsFingerprintdRunning || this.mMonitoringPid == -1) {
            LogUtil.e(this.TAG, "fingerprintd is restarting, stopApiName = " + stopApiName + ", session = " + session);
        } else if (this.mIsTestWorking) {
            LogUtil.d(this.TAG, "kill will happen, stopApiName = " + stopApiName + ", session = " + session + ", mIsTestWorking = " + this.mIsTestWorking);
            this.mIsTestWorking = false;
        } else if (this.mHandler.hasMessages(0, session)) {
            LogUtil.d(this.TAG, "stop  " + stopApiName + ", session = " + session);
            this.mHandler.removeMessages(0, session);
            this.mStopChecking = true;
            this.mActiveApiVector.remove(session);
        } else {
            LogUtil.e(this.TAG, "no message, session = " + session);
        }
    }

    private void initHandler() {
        this.mHandler = new ExHandler(this.mServiceThread.getLooper()) {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0:
                        HealthMonitor.this.handleFingerprintdBinderCallCheck((String) msg.obj, msg.arg1);
                        return;
                    default:
                        LogUtil.w(HealthMonitor.this.TAG, "Unknown message:" + msg.what);
                        return;
                }
            }
        };
    }

    public void handleFingerprintdBinderCallCheck(String session, int monitoringPid) {
        LogUtil.d(this.TAG, "handleFingerprintdBinderCallCheck apiName = " + ((String) this.mActiveApiVector.get(session)) + ", session = " + session);
        int currentFingerprintdPid = getFingerprintdPid();
        if (currentFingerprintdPid != monitoringPid || monitoringPid == -1) {
            LogUtil.d(this.TAG, "fingerprintd has been died, skip killing");
        } else {
            resetFingerprintd(currentFingerprintdPid);
        }
    }

    private void resetFingerprintd(int pid) {
        if (FingerprintService.DEBUG) {
            LogUtil.d(this.TAG, "do no resetFingerprintd pid = " + pid + " in debug mode");
            return;
        }
        if (pid != -1) {
            this.mIsFingerprintdRunning = false;
            this.mActiveApiVector.clear();
            this.mHandler.removeMessages(0);
            Process.sendSignal(pid, 3);
            LogUtil.e(this.TAG, "resetFingerprintd, pid = " + pid);
        }
    }

    public int getFingerprintdPid() {
        int[] pids = Process.getPidsForCommands(FINGERPRINTD_NATIVE_NAME);
        if (pids != null) {
            for (int pid : pids) {
                LogUtil.d(this.TAG, " pid = " + pid);
            }
        }
        if (pids == null || pids.length != 1) {
            return -1;
        }
        return pids[0];
    }

    void dump(FileDescriptor fd, PrintWriter pw, String[] args, String prefix) {
        this.mIsTestWorking = false;
        String subPrefix = "  " + prefix;
        pw.print(subPrefix);
        pw.println("mLastBinderCallFingerprintdPid = " + this.mLastBinderCallFingerprintdPid);
        pw.print(subPrefix);
        pw.println("currentBinderCall = " + SystemProperties.get(this.mPropFingerprintdBingercall, ""));
        pw.print(subPrefix);
        pw.println("currentFingerprintdPid = " + getFingerprintdPid());
        pw.print(subPrefix);
        pw.println("mIsTestWorking = " + this.mIsTestWorking);
    }
}
