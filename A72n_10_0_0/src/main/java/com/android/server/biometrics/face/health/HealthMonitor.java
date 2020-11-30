package com.android.server.biometrics.face.health;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;
import android.os.Message;
import android.os.Process;
import com.android.server.ServiceThread;
import com.android.server.am.IColorAppStartupManager;
import com.android.server.biometrics.BiometricWakeupManagerService;
import com.android.server.biometrics.face.dcs.DcsUtil;
import com.android.server.biometrics.face.tool.ExHandler;
import com.android.server.biometrics.face.utils.LogUtil;
import com.android.server.biometrics.face.utils.Utils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class HealthMonitor {
    public static final String CAMERA_NATIVE_NAME = "/system/bin/cameraserver";
    public static final String[] CAMERA_NATIVE_NAME_ARRAY = {CAMERA_NATIVE_NAME};
    public static final String FACED_NATIVE_NAME = "/vendor/bin/hw/vendor.oppo.hardware.biometrics.face@1.0-service";
    public static final String[] FACED_NATIVE_NAME_ARRAY = {FACED_NATIVE_NAME};
    public static final int MSG_BINDERCALL_CHECK = 0;
    public static final int MSG_FACE_PROCESS_DIED = 1;
    public static final int MSG_UNBLOCK_SCREEN_ON = 13;
    private static int mCameraPid = -1;
    private static ExHandler mSubHandler;
    private static Object sMutex = new Object();
    private static HealthMonitor sSingleInstance;
    private String TAG = "FaceService.HealthMonitor";
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
        this.mServiceThread = new ServiceThread("FaceHealth", 10, true);
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

    public static HealthMonitor getHealthMonitor(Context context, ExHandler exHandler) {
        mSubHandler = exHandler;
        return getHealthMonitor(context);
    }

    public void demoProcessSystemReady(int pid) {
        this.mIsProcessRunning = true;
        this.mMonitoringPid = pid;
        mCameraPid = getDemoProcessPid(CAMERA_NATIVE_NAME);
        String str = this.TAG;
        LogUtil.d(str, "processSystemReady mMonitoringPid = " + this.mMonitoringPid + ", mCameraPid = " + mCameraPid);
        if (pid == -1) {
            this.mMonitoringPid = getDemoProcessPid(FACED_NATIVE_NAME);
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
        this.mHandler.post(new Runnable() {
            /* class com.android.server.biometrics.face.health.$$Lambda$HealthMonitor$amnAZQEq4AzUgE4CxcUySDBe38c */

            public final void run() {
                HealthMonitor.this.lambda$notifyDemoProcessDied$0$HealthMonitor();
            }
        });
        this.mHandler.removeMessages(0);
    }

    public /* synthetic */ void lambda$notifyDemoProcessDied$0$HealthMonitor() {
        this.mActiveApiVector.clear();
    }

    public void start(String startApiName, long delay, String session) {
        if (!this.mIsProcessRunning || this.mMonitoringPid == -1) {
            String str = this.TAG;
            LogUtil.e(str, "demo process is restarting, startApiName = " + startApiName + ", session = " + session);
            return;
        }
        String str2 = this.TAG;
        LogUtil.d(str2, "start " + startApiName + ", session = " + session + ", delay = " + delay + ", mMonitoringPid = " + this.mMonitoringPid + ", mCameraPid = " + mCameraPid);
        this.mHandler.post(new Runnable(session, startApiName) {
            /* class com.android.server.biometrics.face.health.$$Lambda$HealthMonitor$JYYAtz9RR8BVRlKnxuUDMuaYKoU */
            private final /* synthetic */ String f$1;
            private final /* synthetic */ String f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                HealthMonitor.this.lambda$start$1$HealthMonitor(this.f$1, this.f$2);
            }
        });
        Message msg = this.mHandler.obtainMessage(0);
        msg.obj = session;
        msg.arg1 = this.mMonitoringPid;
        this.mHandler.sendMessageDelayed(msg, delay);
    }

    public /* synthetic */ void lambda$start$1$HealthMonitor(String session, String startApiName) {
        this.mActiveApiVector.put(session, startApiName);
        if (HealthState.OPEN_CAMERA.equals(startApiName) || HealthState.START_PREVIEW.equals(startApiName) || "stopPreview".equals(startApiName) || HealthState.RELEASE_CAMERA.equals(startApiName)) {
            mCameraPid = getDemoProcessPid(CAMERA_NATIVE_NAME);
        }
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
            this.mHandler.post(new Runnable(session) {
                /* class com.android.server.biometrics.face.health.$$Lambda$HealthMonitor$5_TLv6gAdLUxVgKJ4O5d2K2xgDA */
                private final /* synthetic */ String f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    HealthMonitor.this.lambda$stop$2$HealthMonitor(this.f$1);
                }
            });
        }
    }

    public /* synthetic */ void lambda$stop$2$HealthMonitor(String session) {
        this.mActiveApiVector.remove(session);
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
                    String str = HealthMonitor.this.TAG;
                    LogUtil.w(str, "Unknown message:" + msg.what);
                } else {
                    String str2 = HealthMonitor.this.TAG;
                    LogUtil.e(str2, "MSG_FACE_PROCESS_DIED, msg.obj = " + ((String) msg.obj));
                    HealthMonitor.this.handleFaceProcessDied((String) msg.obj, msg.arg1);
                }
            }
        };
    }

    public void handleDemoProcessBinderCallCheck(String session, int monitoringPid) {
        int currentDemoProcessPid;
        int i;
        ExHandler exHandler;
        String funcName = this.mActiveApiVector.get(session);
        DcsUtil dcs = DcsUtil.getDcsUtil(this.mContext);
        if (!(dcs == null || funcName == null)) {
            dcs.sendHealthTimeout(funcName);
        }
        if (!HealthState.BLOCKSCREENON.equals(funcName) || (exHandler = mSubHandler) == null) {
            if (HealthState.OPEN_CAMERA.equals(funcName) || HealthState.START_PREVIEW.equals(funcName) || "stopPreview".equals(funcName) || HealthState.RELEASE_CAMERA.equals(funcName)) {
                currentDemoProcessPid = getDemoProcessPid(CAMERA_NATIVE_NAME);
            } else {
                currentDemoProcessPid = getDemoProcessPid(FACED_NATIVE_NAME);
            }
            String str = this.TAG;
            LogUtil.d(str, "handleDemoProcessBinderCallCheck apiName = " + funcName + ", session = " + session + ", currentDemoProcessPid = " + currentDemoProcessPid + ", monitoringPid = " + monitoringPid + ", mCameraPid = " + mCameraPid);
            if ((currentDemoProcessPid != monitoringPid || monitoringPid == -1) && (currentDemoProcessPid != (i = mCameraPid) || i == -1)) {
                LogUtil.d(this.TAG, "skip killing");
                return;
            }
            resetProcess(currentDemoProcessPid);
            int i2 = mCameraPid;
            if (currentDemoProcessPid == i2 && i2 != -1) {
                this.mIsProcessRunning = true;
                return;
            }
            return;
        }
        Message msg = exHandler.obtainMessage(13);
        msg.obj = BiometricWakeupManagerService.UNBLOCK_SCREEN_ON_BY_CAMERA_TIMEOUT;
        mSubHandler.sendMessageDelayed(msg, 0);
        LogUtil.d(this.TAG, "handleDemoProcessBinderCallCheck, MSG_UNBLOCK_SCREEN_ON");
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

    public int getDemoProcessPid(String processName) {
        String[] nativeNameArray;
        int pid = -1;
        if (FACED_NATIVE_NAME.equals(processName)) {
            nativeNameArray = FACED_NATIVE_NAME_ARRAY;
        } else if (CAMERA_NATIVE_NAME.equals(processName)) {
            nativeNameArray = CAMERA_NATIVE_NAME_ARRAY;
        } else {
            nativeNameArray = new String[0];
        }
        int[] pids = Process.getPidsForCommands(nativeNameArray);
        if (pids != null && pids.length == 1) {
            pid = pids[0];
        }
        String str = this.TAG;
        LogUtil.d(str, " processName = " + processName + ", pid = " + pid);
        return pid;
    }

    public int getFaceProcessMemory() {
        int memsize = 0;
        ActivityManager activityManager = this.mActivityManager;
        if (activityManager != null) {
            Debug.MemoryInfo[] memoryInfos = activityManager.getProcessMemoryInfo(new int[]{getDemoProcessPid(FACED_NATIVE_NAME)});
            if (memoryInfos != null && memoryInfos.length > 0) {
                memsize = memoryInfos[0].getSummaryTotalPss();
            }
            String str = this.TAG;
            LogUtil.w(str, "getFaceProcessMemory, memsize = " + memsize + " KB");
        }
        return memsize;
    }
}
