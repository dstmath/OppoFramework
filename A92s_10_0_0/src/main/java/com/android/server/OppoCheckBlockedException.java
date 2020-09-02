package com.android.server;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.os.SystemProperties;
import android.util.Log;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.color.util.ColorTypeCastingHelper;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import oppo.util.OppoStatistics;

public class OppoCheckBlockedException {
    private static final String TAG = "OppoCheckBlockedException";
    private static OppoCheckBlockedException sCheckBlcokExp;
    private boolean mBootComplete = false;
    private Context mContext;
    private boolean mDebugHungtask = false;
    private final String mLastExceptionProc = "/proc/sys/kernel/hung_task_oppo_kill";
    private final String mLastExceptionProperty = "persist.hungtask.oppo.kill";
    private boolean mReleaseVersion = false;
    private HandlerThread threadCheckBlockedExceptionThread = new HandlerThread("CheckBlockedExceptionThread");
    private ArrayList<OppoStateWatch> valueWatchArray = new ArrayList<>();

    private OppoCheckBlockedException() {
        this.threadCheckBlockedExceptionThread.start();
        this.mReleaseVersion = SystemProperties.getBoolean("ro.build.release_type", false);
        this.mDebugHungtask = SystemProperties.getBoolean("persist.debug.hungtask", false);
    }

    public void addStateWatch(OppoStateWatch watch) {
        this.valueWatchArray.add(watch);
    }

    /* access modifiers changed from: package-private */
    public void triggerDetect() {
        if (isDectectEnabled() && this.mContext != null && isBootComplete()) {
            Iterator<OppoStateWatch> it = this.valueWatchArray.iterator();
            while (it.hasNext()) {
                it.next().triggerDetect();
            }
        }
    }

    public Looper getCheckLoop() {
        return this.threadCheckBlockedExceptionThread.getLooper();
    }

    public static OppoCheckBlockedException getInstance() {
        if (sCheckBlcokExp == null) {
            sCheckBlcokExp = new OppoCheckBlockedException();
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

    /* access modifiers changed from: package-private */
    public boolean isDectectEnabled() {
        return this.mReleaseVersion || this.mDebugHungtask;
    }

    /* access modifiers changed from: package-private */
    public boolean isBootComplete() {
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
        if (new File("/proc/sys/kernel/hung_task_oppo_kill").exists()) {
            try {
                BufferedReader in = new BufferedReader(new FileReader("/proc/sys/kernel/hung_task_oppo_kill"));
                final String strSend = in.readLine().trim();
                in.close();
                if (!strSend.isEmpty()) {
                    Log.i(TAG, "CheckLastRebootExceptionMsgFromProc strSend:" + strSend);
                    new Handler(getInstance().getCheckLoop()).postDelayed(new Runnable() {
                        /* class com.android.server.OppoCheckBlockedException.AnonymousClass1 */

                        public void run() {
                            OppoCheckBlockedException.this.sendDcsMsg(strSend);
                            OppoCheckBlockedException.this.WriteLastExceptionMsgToProc(StringUtils.SPACE);
                        }
                    }, 60000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void WriteLastExceptionMsgToProc(String strMsg) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("/proc/sys/kernel/hung_task_oppo_kill"));
            bw.write(strMsg, 0, strMsg.length());
            bw.close();
            bw.close();
            Log.i(TAG, "WriteLastExceptionMsgToProc strMsg:" + strMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void CheckLastRebootExceptionMsgFromProperty() {
        final String strSend = SystemProperties.get("persist.hungtask.oppo.kill");
        if (strSend != null && !strSend.isEmpty()) {
            Log.i(TAG, "CheckLastRebootExceptionMsgFromProperty strSend:" + strSend);
            new Handler(getInstance().getCheckLoop()).postDelayed(new Runnable() {
                /* class com.android.server.OppoCheckBlockedException.AnonymousClass2 */

                public void run() {
                    OppoCheckBlockedException.this.sendDcsMsg(strSend);
                    SystemProperties.set("persist.hungtask.oppo.kill", "");
                }
            }, 60000);
        }
    }

    public static void rebootSystemServer() {
        Log.i(TAG, "rebootSystemServer, we kill it's parent zygote");
        Process.killProcess(Process.myPpid());
    }

    /* access modifiers changed from: private */
    public synchronized void sendDcsMsg(String strMsg) {
        try {
            int nSplitIndex = strMsg.indexOf(44);
            if (nSplitIndex != -1 || strMsg.length() <= nSplitIndex + 1) {
                Map<String, String> logMap = new HashMap<>();
                logMap.put("HungTaskModule", strMsg.substring(0, nSplitIndex));
                logMap.put("HungTaskMsg", strMsg.substring(nSplitIndex + 1));
                logMap.put("isReleaseVersion", getInstance().isReleaseVersion() ? "1" : "0");
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
            /* class com.android.server.OppoCheckBlockedException.AnonymousClass3 */

            public void run() {
                OppoBaseWatchdog baseWatchdog = OppoCheckBlockedException.typeCasting(Watchdog.getInstance());
                if (baseWatchdog != null) {
                    baseWatchdog.dumpStackAndAddDropbox(reason);
                }
            }
        };
        dumpThread.start();
        try {
            dumpThread.join(30000);
        } catch (InterruptedException e) {
            Log.w(TAG, "DeathHealerDumpStack, dumpStackAndAddDropbox timeout!");
        }
    }

    /* access modifiers changed from: private */
    public static OppoBaseWatchdog typeCasting(Watchdog dog) {
        if (dog != null) {
            return (OppoBaseWatchdog) ColorTypeCastingHelper.typeCasting(OppoBaseWatchdog.class, dog);
        }
        return null;
    }
}
