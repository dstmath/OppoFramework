package com.android.server.face.health;

import android.content.Context;
import android.os.Message;
import android.os.Process;
import com.android.server.ServiceThread;
import com.android.server.face.FaceService;
import com.android.server.face.dcs.DcsUtil;
import com.android.server.face.tool.ExHandler;
import com.android.server.face.utils.LogUtil;
import java.util.HashMap;

public class HealthMonitor {
    public static final String[] FACED_NATIVE_NAME = new String[]{"/vendor/bin/hw/vendor.oppo.hardware.biometrics.face@1.0-service"};
    public static final int MSG_BINDERCALL_CHECK = 0;
    private String TAG = "FaceService.HealthMonitor";
    public HashMap<String, String> mActiveApiVector = new HashMap();
    private Context mContext;
    private ExHandler mHandler;
    private boolean mIsProcessRunning = false;
    private boolean mIsTestWorking = false;
    private String mMonitorName;
    private int mMonitoringPid = -1;
    private ServiceThread mServiceThread;

    public HealthMonitor(Context context, String monitorName) {
        this.mMonitorName = monitorName;
        this.mContext = context;
        this.mServiceThread = new ServiceThread(this.TAG, 10, true);
        this.mServiceThread.start();
        initHandler();
    }

    public void demoProcessSystemReady(int pid) {
        this.mIsProcessRunning = true;
        this.mMonitoringPid = pid;
        LogUtil.d(this.TAG, "processSystemReady mMonitoringPid = " + this.mMonitoringPid);
        if (pid == -1) {
            this.mMonitoringPid = getDemoProcessPid();
        }
    }

    public void notifyDemoProcessDied() {
        LogUtil.d(this.TAG, "notifyDemoProcessDied");
        this.mIsProcessRunning = false;
        this.mMonitoringPid = -1;
        this.mActiveApiVector.clear();
        this.mHandler.removeMessages(0);
    }

    public void start(String startApiName, long delay, String session) {
        if (!this.mIsProcessRunning || this.mMonitoringPid == -1) {
            LogUtil.e(this.TAG, "demo process is restarting, startApiName = " + startApiName + ", session = " + session);
            return;
        }
        LogUtil.d(this.TAG, "start " + startApiName + ", session = " + session + ", delay = " + delay + ", mMonitoringPid = " + this.mMonitoringPid);
        this.mActiveApiVector.put(session, startApiName);
        Message msg = this.mHandler.obtainMessage(0);
        msg.obj = session;
        msg.arg1 = this.mMonitoringPid;
        this.mHandler.sendMessageDelayed(msg, delay);
    }

    public void stop(String stopApiName, String session) {
        if (!this.mIsProcessRunning || this.mMonitoringPid == -1) {
            LogUtil.e(this.TAG, "demo process is restarting, stopApiName = " + stopApiName + ", session = " + session);
        } else if (this.mIsTestWorking) {
            LogUtil.d(this.TAG, "kill will happen, stopApiName = " + stopApiName + ", session = " + session + ", mIsTestWorking = " + this.mIsTestWorking);
            this.mIsTestWorking = false;
        } else if (this.mHandler.hasMessages(0, session)) {
            LogUtil.d(this.TAG, "stop  " + stopApiName + ", session = " + session);
            this.mHandler.removeMessages(0, session);
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
                        HealthMonitor.this.handleDemoProcessBinderCallCheck((String) msg.obj, msg.arg1);
                        return;
                    default:
                        LogUtil.w(HealthMonitor.this.TAG, "Unknown message:" + msg.what);
                        return;
                }
            }
        };
    }

    public void handleDemoProcessBinderCallCheck(String session, int monitoringPid) {
        LogUtil.d(this.TAG, "handleDemoProcessBinderCallCheck apiName = " + ((String) this.mActiveApiVector.get(session)) + ", session = " + session);
        int currentDemoProcessPid = getDemoProcessPid();
        if (currentDemoProcessPid != monitoringPid || monitoringPid == -1) {
            LogUtil.d(this.TAG, "demo process has been died, skip killing");
        } else {
            resetProcess(currentDemoProcessPid);
        }
        DcsUtil dcs = DcsUtil.getDcsUtil(this.mContext);
        String funcName = (String) this.mActiveApiVector.get(session);
        if (dcs != null && funcName != null) {
            dcs.sendHealthTimeout(funcName);
        }
    }

    private void resetProcess(int pid) {
        if (FaceService.DEBUG) {
            LogUtil.d(this.TAG, "do no resetProcess pid = " + pid + " in debug mode");
            return;
        }
        if (pid != -1) {
            this.mIsProcessRunning = false;
            this.mActiveApiVector.clear();
            this.mHandler.removeMessages(0);
            Process.sendSignal(pid, 3);
            LogUtil.e(this.TAG, "resetProcess, pid = " + pid);
        }
    }

    public int getDemoProcessPid() {
        int[] pids = Process.getPidsForCommands(FACED_NATIVE_NAME);
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
}
