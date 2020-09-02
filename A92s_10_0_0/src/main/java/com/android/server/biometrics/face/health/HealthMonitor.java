package com.android.server.biometrics.face.health;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;
import android.os.Message;
import android.os.Process;
import com.android.server.ServiceThread;
import com.android.server.am.IColorAppStartupManager;
import com.android.server.biometrics.face.dcs.DcsUtil;
import com.android.server.biometrics.face.tool.ExHandler;
import com.android.server.biometrics.face.utils.LogUtil;
import com.android.server.biometrics.face.utils.Utils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class HealthMonitor {
    public static final String[] FACED_NATIVE_NAME = {"/vendor/bin/hw/vendor.oppo.hardware.biometrics.face@1.0-service"};
    public static final int MSG_BINDERCALL_CHECK = 0;
    public static final int MSG_FACE_PROCESS_DIED = 1;
    private static Object sMutex = new Object();
    private static HealthMonitor sSingleInstance;
    /* access modifiers changed from: private */
    public String TAG = "FaceService.HealthMonitor";
    public HashMap<String, String> mActiveApiVector = new HashMap<>();
    private ActivityManager mActivityManager = null;
    private Context mContext;
    private ExHandler mHandler;
    private boolean mIsProcessRunning = false;
    private boolean mIsTestWorking = false;
    private int mMonitoringPid = -1;
    private ServiceThread mServiceThread;

    private HealthMonitor(Context context) {
        this.mContext = context;
        this.mServiceThread = new ServiceThread(this.TAG, 10, true);
        this.mServiceThread.start();
        initHandler();
        this.mActivityManager = (ActivityManager) context.getSystemService(IColorAppStartupManager.TYPE_ACTIVITY);
    }

    public static HealthMonitor getHealthMonitor(Context context) {
        HealthMonitor healthMonitor;
        synchronized (sMutex) {
            if (sSingleInstance == null) {
                sSingleInstance = new HealthMonitor(context);
            }
            healthMonitor = sSingleInstance;
        }
        return healthMonitor;
    }

    public void demoProcessSystemReady(int pid) {
        this.mIsProcessRunning = true;
        this.mMonitoringPid = pid;
        String str = this.TAG;
        LogUtil.d(str, "processSystemReady mMonitoringPid = " + this.mMonitoringPid);
        if (pid == -1) {
            this.mMonitoringPid = getDemoProcessPid();
        }
    }

    public void notifyDemoProcessDied() {
        LogUtil.d(this.TAG, "notifyDemoProcessDied");
        Message msg = this.mHandler.obtainMessage(1);
        msg.obj = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        msg.arg1 = this.mMonitoringPid;
        String str = this.TAG;
        LogUtil.e(str, "notifyDemoProcessDied, msg.obj = " + ((String) msg.obj));
        this.mHandler.sendMessageDelayed(msg, 0);
        this.mIsProcessRunning = false;
        this.mMonitoringPid = -1;
        this.mActiveApiVector.clear();
        this.mHandler.removeMessages(0);
    }

    public void start(String startApiName, long delay, String session) {
        if (!this.mIsProcessRunning || this.mMonitoringPid == -1) {
            String str = this.TAG;
            LogUtil.e(str, "demo process is restarting, startApiName = " + startApiName + ", session = " + session);
            return;
        }
        String str2 = this.TAG;
        LogUtil.d(str2, "start " + startApiName + ", session = " + session + ", delay = " + delay + ", mMonitoringPid = " + this.mMonitoringPid);
        this.mActiveApiVector.put(session, startApiName);
        Message msg = this.mHandler.obtainMessage(0);
        msg.obj = session;
        msg.arg1 = this.mMonitoringPid;
        this.mHandler.sendMessageDelayed(msg, delay);
    }

    public void stop(String stopApiName, String session) {
        if (!this.mIsProcessRunning || this.mMonitoringPid == -1) {
            String str = this.TAG;
            LogUtil.e(str, "demo process is restarting, stopApiName = " + stopApiName + ", session = " + session);
        } else if (this.mIsTestWorking) {
            String str2 = this.TAG;
            LogUtil.d(str2, "kill will happen, stopApiName = " + stopApiName + ", session = " + session + ", mIsTestWorking = " + this.mIsTestWorking);
            this.mIsTestWorking = false;
        } else if (!this.mHandler.hasMessages(0, session)) {
            String str3 = this.TAG;
            LogUtil.e(str3, "no message, session = " + session);
        } else {
            String str4 = this.TAG;
            LogUtil.d(str4, "stop  " + stopApiName + ", session = " + session);
            this.mHandler.removeMessages(0, session);
            this.mActiveApiVector.remove(session);
        }
    }

    private void initHandler() {
        this.mHandler = new ExHandler(this.mServiceThread.getLooper()) {
            /* class com.android.server.biometrics.face.health.HealthMonitor.AnonymousClass1 */

            @Override // com.android.server.biometrics.face.tool.ExHandler
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                int i = msg.what;
                if (i == 0) {
                    HealthMonitor.this.handleDemoProcessBinderCallCheck((String) msg.obj, msg.arg1);
                } else if (i != 1) {
                    String access$000 = HealthMonitor.this.TAG;
                    LogUtil.w(access$000, "Unknown message:" + msg.what);
                } else {
                    String access$0002 = HealthMonitor.this.TAG;
                    LogUtil.e(access$0002, "MSG_FACE_PROCESS_DIED, msg.obj = " + ((String) msg.obj));
                    HealthMonitor.this.handleFaceProcessDied((String) msg.obj, msg.arg1);
                }
            }
        };
    }

    public void handleDemoProcessBinderCallCheck(String session, int monitoringPid) {
        String funcName = this.mActiveApiVector.get(session);
        String str = this.TAG;
        LogUtil.d(str, "handleDemoProcessBinderCallCheck apiName = " + funcName + ", session = " + session);
        if (!HealthState.OPEN_CAMERA.equals(funcName) && !HealthState.START_PREVIEW.equals(funcName) && !"stopPreview".equals(funcName) && !HealthState.RELEASE_CAMERA.equals(funcName)) {
            int currentDemoProcessPid = getDemoProcessPid();
            if (currentDemoProcessPid != monitoringPid || monitoringPid == -1) {
                LogUtil.d(this.TAG, "demo process has been died, skip killing");
            } else {
                resetProcess(currentDemoProcessPid);
            }
        }
        DcsUtil dcs = DcsUtil.getDcsUtil(this.mContext);
        if (dcs != null && funcName != null) {
            dcs.sendHealthTimeout(funcName);
        }
    }

    public void handleFaceProcessDied(String deathTime, int facePid) {
        String str = this.TAG;
        LogUtil.e(str, "handleFaceProcessDied, deathTime = " + deathTime);
        DcsUtil dcs = DcsUtil.getDcsUtil(this.mContext);
        if (dcs != null) {
            dcs.sendFaceProcessDeathInfo(deathTime, facePid);
        }
    }

    private void resetProcess(int pid) {
        if (!Utils.isReleaseVersion()) {
            String str = this.TAG;
            LogUtil.d(str, "do no resetProcess pid = " + pid + " in debug mode");
        } else if (pid != -1) {
            this.mIsProcessRunning = false;
            this.mActiveApiVector.clear();
            this.mHandler.removeMessages(0);
            Process.sendSignal(pid, 3);
            String str2 = this.TAG;
            LogUtil.e(str2, "resetProcess, pid = " + pid);
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

    public int getFaceProcessMemory() {
        int memsize = 0;
        ActivityManager activityManager = this.mActivityManager;
        if (activityManager != null) {
            Debug.MemoryInfo[] memoryInfos = activityManager.getProcessMemoryInfo(new int[]{getDemoProcessPid()});
            if (memoryInfos != null && memoryInfos.length > 0) {
                memsize = memoryInfos[0].getSummaryTotalPss();
            }
            String str = this.TAG;
            LogUtil.w(str, "getFaceProcessMemory, memsize = " + memsize + " KB");
        }
        return memsize;
    }
}
