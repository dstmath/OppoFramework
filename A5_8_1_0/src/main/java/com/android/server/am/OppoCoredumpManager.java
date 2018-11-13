package com.android.server.am;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.OppoAssertTip;
import android.os.Process;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.face.FaceDaemonWrapper;
import java.io.File;
import java.util.ArrayList;

public class OppoCoredumpManager {
    private static final String ACTION_QCOMLOGGER_STATE_CHANGED = "oppo.intent.action.StartOrStopLogcat";
    private static final String TAG = "OppoCoredumpManager";
    private static OppoCoredumpManager mInstance = null;
    private OppoAssertTip mAssertProxy = null;
    private Context mContext = null;

    private OppoCoredumpManager(Context context) {
        this.mContext = context;
        this.mAssertProxy = OppoAssertTip.getInstance();
        if (isParentPidChanged()) {
            showAsserttip();
        }
        recordParentPid();
        registCoredumpBroadcast(context);
    }

    public static OppoCoredumpManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new OppoCoredumpManager(context);
        }
        return mInstance;
    }

    private boolean isParentPidChanged() {
        int curParent = Process.myPpid();
        int preParent = SystemProperties.getInt("sys.coredump.parent.pid", -1);
        if (preParent == -1 || curParent == preParent) {
            return false;
        }
        return true;
    }

    private void recordParentPid() {
        SystemProperties.set("sys.coredump.parent.pid", Integer.toString(Process.myPpid()));
    }

    private void showAsserttip() {
        boolean isDebugVersion = !SystemProperties.getBoolean("ro.build.release_type", false);
        boolean isLogOn = SystemProperties.getBoolean("persist.sys.assert.panic", false);
        File coredumpFile = new File("/data/core");
        if (coredumpFile != null) {
            String[] coredumps = coredumpFile.list();
            boolean isCoredump = (coredumps == null || coredumps.length == 0) ? false : true;
            if (isCoredump && coredumps.length > 2) {
                new Thread(new Runnable() {
                    public void run() {
                        int i;
                        ArrayList<File> logcatCore = new ArrayList();
                        ArrayList<File> systemCore = new ArrayList();
                        OppoCoredumpManager.this.getCoreFiles(logcatCore, "logcat");
                        OppoCoredumpManager.this.getCoreFiles(systemCore, "app_process64");
                        if (logcatCore.size() >= 2) {
                            for (i = 1; i < logcatCore.size(); i++) {
                                ((File) logcatCore.get(i)).delete();
                            }
                        }
                        if (logcatCore.size() >= 2) {
                            for (i = 1; i < systemCore.size(); i++) {
                                ((File) systemCore.get(i)).delete();
                            }
                        }
                    }
                }).start();
            }
            if (isDebugVersion && isLogOn && isCoredump) {
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(FaceDaemonWrapper.TIMEOUT_FACED_BINDERCALL_CHECK);
                            StringBuilder sb = new StringBuilder(1024);
                            sb.append("critical service native crash! \n");
                            sb.append("please grab coredump log using adb pull /data/core xxx \n");
                            sb.append("then please send this log to jiemin.zhu of android team, thank you!\n");
                            OppoCoredumpManager.this.mAssertProxy.requestShowAssertMessage(sb.toString());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
    }

    private void getCoreFiles(ArrayList<File> fileList, String classify) {
        File[] allFiles = new File("/data/core/").listFiles();
        for (File file : allFiles) {
            if (file.isFile() && file.getName().contains(classify)) {
                fileList.add(file);
            }
        }
    }

    private void registCoredumpBroadcast(Context context) {
        IntentFilter coredumpFilter = new IntentFilter();
        coredumpFilter.addAction("oppo.intent.action.StartOrStopLogcat");
        context.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("oppo.intent.action.StartOrStopLogcat")) {
                    final int enable = intent.getIntExtra("enable", 0);
                    Log.i(OppoCoredumpManager.TAG, "qcom log change to " + enable);
                    new Thread(new Runnable() {
                        public void run() {
                            if (enable > 0) {
                                Process.setSystemServerCoredump(1);
                            } else {
                                Process.setSystemServerCoredump(0);
                            }
                        }
                    }).start();
                }
            }
        }, coredumpFilter);
    }
}
