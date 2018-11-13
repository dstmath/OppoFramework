package com.android.server;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.os.SystemProperties;
import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import oppo.util.OppoStatistics;

public class CheckBlockedException {
    private static final String TAG = "CheckBlockedException";
    private static CheckBlockedException sCheckBlcokExp;
    private boolean mBootComplete = false;
    private Context mContext;
    private boolean mDebugHungtask = false;
    private final String mLastExceptionProc = "/proc/sys/kernel/hung_task_oppo_kill";
    private final String mLastExceptionProperty = "persist.hungtask.oppo.kill";
    private boolean mReleaseVersion = false;
    private HandlerThread threadCheckBlockedExceptionThread = new HandlerThread("CheckBlockedExceptionThread");
    private ArrayList<StateWatch> valueWatchArray = new ArrayList();

    private CheckBlockedException() {
        this.threadCheckBlockedExceptionThread.start();
        this.mReleaseVersion = SystemProperties.getBoolean("ro.build.release_type", false);
        this.mDebugHungtask = SystemProperties.getBoolean("persist.debug.hungtask", false);
    }

    public void addStateWatch(StateWatch watch) {
        this.valueWatchArray.add(watch);
    }

    void triggerDetect() {
        if (isDectectEnabled() && this.mContext != null && (isBootComplete() ^ 1) == 0) {
            for (StateWatch watch : this.valueWatchArray) {
                watch.triggerDetect();
            }
        }
    }

    public Looper getCheckLoop() {
        return this.threadCheckBlockedExceptionThread.getLooper();
    }

    public static CheckBlockedException getInstance() {
        if (sCheckBlcokExp == null) {
            sCheckBlcokExp = new CheckBlockedException();
        }
        return sCheckBlcokExp;
    }

    public void setContext(Context context) {
        if (this.mContext == null) {
            this.mContext = context;
        }
    }

    public Context getContext() {
        return this.mContext;
    }

    public boolean isReleaseVersion() {
        return this.mReleaseVersion;
    }

    boolean isDectectEnabled() {
        return !this.mReleaseVersion ? this.mDebugHungtask : true;
    }

    boolean isBootComplete() {
        if (this.mBootComplete) {
            return true;
        }
        this.mBootComplete = SystemProperties.getBoolean("sys.boot_completed", false);
        if (this.mBootComplete) {
            CheckLastRebootExceptionMsgFromProc();
            CheckLastRebootExceptionMsgFromProperty();
        }
        return this.mBootComplete;
    }

    private void CheckLastRebootExceptionMsgFromProc() {
        Exception e;
        if (new File("/proc/sys/kernel/hung_task_oppo_kill").exists()) {
            try {
                BufferedReader in = new BufferedReader(new FileReader("/proc/sys/kernel/hung_task_oppo_kill"));
                try {
                    final String strSend = in.readLine().trim();
                    if (in != null) {
                        in.close();
                    }
                    if (!strSend.isEmpty()) {
                        Log.i(TAG, "CheckLastRebootExceptionMsgFromProc strSend:" + strSend);
                        new Handler(getInstance().getCheckLoop()).postDelayed(new Runnable() {
                            public void run() {
                                CheckBlockedException.this.sendDcsMsg(strSend);
                                CheckBlockedException.this.WriteLastExceptionMsgToProc(" ");
                            }
                        }, 60000);
                    }
                    BufferedReader bufferedReader = in;
                } catch (Exception e2) {
                    e = e2;
                    e.printStackTrace();
                }
            } catch (Exception e3) {
                e = e3;
                e.printStackTrace();
            }
        }
    }

    public void WriteLastExceptionMsgToProc(String strMsg) {
        Exception e;
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("/proc/sys/kernel/hung_task_oppo_kill"));
            try {
                bw.write(strMsg, 0, strMsg.length());
                bw.close();
                if (bw != null) {
                    bw.close();
                }
                Log.i(TAG, "WriteLastExceptionMsgToProc strMsg:" + strMsg);
                BufferedWriter bufferedWriter = bw;
            } catch (Exception e2) {
                e = e2;
                e.printStackTrace();
            }
        } catch (Exception e3) {
            e = e3;
            e.printStackTrace();
        }
    }

    private void CheckLastRebootExceptionMsgFromProperty() {
        final String strSend = SystemProperties.get("persist.hungtask.oppo.kill");
        if (strSend != null && (strSend.isEmpty() ^ 1) != 0) {
            Log.i(TAG, "CheckLastRebootExceptionMsgFromProperty strSend:" + strSend);
            new Handler(getInstance().getCheckLoop()).postDelayed(new Runnable() {
                public void run() {
                    CheckBlockedException.this.sendDcsMsg(strSend);
                    SystemProperties.set("persist.hungtask.oppo.kill", "");
                }
            }, 60000);
        }
    }

    public static void rebootSystemServer() {
        Log.i(TAG, "rebootSystemServer, we kill it's parent zygote");
        Process.killProcess(Process.myPpid());
    }

    private synchronized void sendDcsMsg(String strMsg) {
        try {
            int nSplitIndex = strMsg.indexOf(44);
            if (nSplitIndex != -1 || strMsg.length() <= nSplitIndex + 1) {
                Map<String, String> logMap = new HashMap();
                logMap.put("HungTaskModule", strMsg.substring(0, nSplitIndex));
                logMap.put("HungTaskMsg", strMsg.substring(nSplitIndex + 1));
                logMap.put("isReleaseVersion", getInstance().isReleaseVersion() ? LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON : "0");
                OppoStatistics.onCommon(getContext(), "HungTaskTag", "HungTaskEventID", logMap, false);
            } else {
                Log.i(TAG, "sendDCSMsg strMsg format is wrong, strMsg:" + strMsg);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void DeathHealerDumpStack(final String reason) {
        Thread dumpThread = new Thread() {
            public void run() {
                Watchdog.getInstance().DumpStackAndAddDropbox(true, reason);
            }
        };
        dumpThread.start();
        try {
            dumpThread.join(30000);
        } catch (InterruptedException e) {
            Log.w(TAG, "DeathHealerDumpStack, DumpStackAndAddDropbox timeout!");
        }
    }
}
