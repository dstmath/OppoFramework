package com.android.server.biometrics.fingerprint.tool;

import android.content.Context;
import android.os.Message;
import android.os.Process;
import android.os.SystemProperties;
import com.android.server.ServiceThread;
import com.android.server.biometrics.fingerprint.FingerprintService;
import com.android.server.biometrics.fingerprint.dcs.DcsFingerprintStatisticsUtil;
import com.android.server.biometrics.fingerprint.util.LogUtil;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;

public class HealthMonitor {
    public static final String[] FINGERPRINTD_NATIVE_NAME = {"/vendor/bin/hw/vendor.oppo.hardware.biometrics.fingerprint@2.1-service"};
    public static final int MSG_BINDERCALL_CHECK = 0;
    private String TAG = "FingerprintService.HealthMonitor";
    public HashMap<String, String> mActiveApiVector = new HashMap<>();
    private Context mContext;
    private ExHandler mHandler;
    private boolean mIsFingerprintdRunning = false;
    private boolean mIsTestWorking = false;
    private int mLastBinderCallFingerprintdPid;
    private String mMonitorName;
    private int mMonitoringPid = -1;
    private String mPropFingerprintdBingercall = "debug.bindercall.api";
    private ServiceThread mServiceThread;
    private volatile boolean mStopChecking = false;

    public HealthMonitor(Context context, String monitorName) {
        this.mContext = context;
        this.mMonitorName = monitorName;
        this.mServiceThread = new ServiceThread(this.TAG, 10, true);
        this.mServiceThread.start();
        initHandler();
    }

    public void fingerprintdSystemReady(int fingerprintdPid) {
        this.mIsFingerprintdRunning = true;
        this.mMonitoringPid = fingerprintdPid;
        String str = this.TAG;
        LogUtil.d(str, "fingerprintdSystemReady mMonitoringPid = " + this.mMonitoringPid);
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
            String str = this.TAG;
            LogUtil.e(str, "fingerprintd is restarting, startApiName = " + startApiName + ", session = " + session);
            return;
        }
        this.mStopChecking = false;
        String str2 = this.TAG;
        LogUtil.d(str2, "startMointor " + startApiName + ", session = " + session + ", delay = " + delay + ", mMonitoringPid = " + this.mMonitoringPid);
        this.mActiveApiVector.put(session, startApiName);
        Message msg = this.mHandler.obtainMessage(0);
        msg.obj = session;
        msg.arg1 = this.mMonitoringPid;
        this.mHandler.sendMessageDelayed(msg, delay);
    }

    public void stop(String stopApiName, String session) {
        if (!this.mIsFingerprintdRunning || this.mMonitoringPid == -1) {
            String str = this.TAG;
            LogUtil.e(str, "fingerprintd is restarting, stopApiName = " + stopApiName + ", session = " + session);
        } else if (this.mIsTestWorking) {
            String str2 = this.TAG;
            LogUtil.d(str2, "kill will happen, stopApiName = " + stopApiName + ", session = " + session + ", mIsTestWorking = " + this.mIsTestWorking);
            this.mIsTestWorking = false;
        } else if (!this.mHandler.hasMessages(0, session)) {
            String str3 = this.TAG;
            LogUtil.e(str3, "no message, session = " + session);
        } else {
            String str4 = this.TAG;
            LogUtil.d(str4, "endMointor  " + stopApiName + ", session = " + session);
            this.mHandler.removeMessages(0, session);
            this.mStopChecking = true;
            this.mActiveApiVector.remove(session);
        }
    }

    private void initHandler() {
        this.mHandler = new ExHandler(this.mServiceThread.getLooper()) {
            /* class com.android.server.biometrics.fingerprint.tool.HealthMonitor.AnonymousClass1 */

            @Override // com.android.server.biometrics.fingerprint.tool.ExHandler
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what != 0) {
                    String str = HealthMonitor.this.TAG;
                    LogUtil.w(str, "Unknown message:" + msg.what);
                    return;
                }
                HealthMonitor.this.handleFingerprintdBinderCallCheck((String) msg.obj, msg.arg1);
            }
        };
    }

    public void handleFingerprintdBinderCallCheck(String session, int monitoringPid) {
        String str = this.TAG;
        LogUtil.d(str, "handleFingerprintdBinderCallCheck apiName = " + this.mActiveApiVector.get(session) + ", session = " + session);
        int currentFingerprintdPid = getFingerprintdPid();
        if (currentFingerprintdPid != monitoringPid || monitoringPid == -1) {
            LogUtil.d(this.TAG, "fingerprintd has been died, skip killing");
        } else {
            resetFingerprintd(currentFingerprintdPid);
        }
        DcsFingerprintStatisticsUtil dcs = DcsFingerprintStatisticsUtil.getDcsFingerprintStatisticsUtil(this.mContext);
        String funcName = this.mActiveApiVector.get(session);
        if (dcs != null && funcName != null) {
            dcs.sendHealthTimeout(funcName);
        }
    }

    private void resetFingerprintd(int pid) {
        if (FingerprintService.FINGER_DEBUG) {
            String str = this.TAG;
            LogUtil.d(str, "do no resetFingerprintd pid = " + pid + " in debug mode");
        } else if (pid != -1) {
            this.mIsFingerprintdRunning = false;
            this.mActiveApiVector.clear();
            this.mHandler.removeMessages(0);
            Process.sendSignal(pid, 3);
            String str2 = this.TAG;
            LogUtil.e(str2, "resetFingerprintd, pid = " + pid);
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

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args, String prefix) {
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
