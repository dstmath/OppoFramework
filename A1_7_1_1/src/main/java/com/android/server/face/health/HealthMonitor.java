package com.android.server.face.health;

import android.content.Context;
import android.os.Message;
import android.os.Process;
import com.android.server.ServiceThread;
import com.android.server.face.FaceService;
import com.android.server.face.dcs.DcsFaceUtil;
import com.android.server.face.tool.ExHandler;
import com.android.server.face.utils.LogUtil;
import java.util.HashMap;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class HealthMonitor {
    public static final String[] FACED_NATIVE_NAME = null;
    public static final int MSG_BINDERCALL_CHECK = 0;
    private String TAG;
    public HashMap<String, String> mActiveApiVector;
    private Context mContext;
    private ExHandler mHandler;
    private boolean mIsProcessRunning;
    private boolean mIsTestWorking;
    private String mMonitorName;
    private int mMonitoringPid;
    private ServiceThread mServiceThread;
    private volatile boolean mStopChecking;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.face.health.HealthMonitor.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.face.health.HealthMonitor.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.face.health.HealthMonitor.<clinit>():void");
    }

    public HealthMonitor(Context context, String monitorName) {
        this.TAG = "FaceService.HealthMonitor";
        this.mStopChecking = false;
        this.mActiveApiVector = new HashMap();
        this.mIsProcessRunning = false;
        this.mMonitoringPid = -1;
        this.mIsTestWorking = false;
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
        this.mStopChecking = false;
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
        DcsFaceUtil dcs = DcsFaceUtil.getDcsFaceUtil(this.mContext);
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
