package com.oppo.uifirst;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemProperties;
import android.os.Trace;
import android.provider.SettingsStringUtil;
import android.telephony.SmsManager;
import android.util.Log;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class UIFirstUtils {
    public static final int APP_STATUS_MOVE_TO_BG = 2;
    public static final int APP_STATUS_MOVE_TO_FG = 1;
    public static final int APP_STATUS_PROC_DIE = 3;
    public static final int APP_STATUS_START_ACTIVITY = 0;
    private static final boolean DEBUG = (!SystemProperties.getBoolean("ro.build.release_type", false));
    private static final long LAUNCH_BOOST_DURATION = 3000;
    private static final int MSG_APP_STATUS_CHANGED = 0;
    private static final int MSG_DISABLE_LAUNCH_BOOST = 1;
    private static final String TAG = "UIFirstUtils";
    private static final String UIFIRST_LAUNCH_BOOST_PATH = "/proc/sys/kernel/launcher_boost_enabled";
    private static UIFirstUtils sInstance = null;
    private HashMap<Integer, ArrayList<Integer>> mBoostingProcs = new HashMap<>();
    private Handler mHandler;
    private ArrayList<String> mTargetUxThreads = new ArrayList<>();
    private HandlerThread mThread;

    private static native String readNativeProcNode(String str);

    private static native int writeNativeProcNode(String str, String str2);

    public static UIFirstUtils getInstance() {
        if (sInstance == null) {
            sInstance = new UIFirstUtils();
        }
        return sInstance;
    }

    /* access modifiers changed from: private */
    public class AppStatusParam {
        Bundle data;
        int pid;
        int renderTid;
        int status;

        private AppStatusParam() {
        }

        public String toString() {
            return "AppStatusParam @pid:" + this.pid + " renderTid:" + this.renderTid;
        }

        public boolean equals(Object obj) {
            AppStatusParam other = (AppStatusParam) obj;
            return this.pid == other.pid && this.renderTid == other.renderTid;
        }
    }

    private class UIFirstHandler extends Handler {
        public UIFirstHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            try {
                int i = msg.what;
                if (i == 0) {
                    UIFirstUtils.this.handleAppStatusChanged((AppStatusParam) msg.obj);
                } else if (i == 1) {
                    Log.d(UIFirstUtils.TAG, "disable launch boost..");
                    UIFirstUtils.writeProcNode(UIFirstUtils.UIFIRST_LAUNCH_BOOST_PATH, WifiEnterpriseConfig.ENGINE_DISABLE);
                }
            } catch (Exception e) {
                Log.w(UIFirstUtils.TAG, "exception in handle msg :");
                e.printStackTrace();
            }
        }
    }

    public void acquireLaunchBoost() {
        if (DEBUG) {
            Log.d(TAG, "acquireLaunchBoost:", new Throwable());
        }
        if (this.mHandler.hasMessages(1)) {
            this.mHandler.removeMessages(1);
        } else {
            writeProcNode(UIFIRST_LAUNCH_BOOST_PATH, WifiEnterpriseConfig.ENGINE_ENABLE);
        }
        this.mHandler.sendEmptyMessageDelayed(1, LAUNCH_BOOST_DURATION);
    }

    /* access modifiers changed from: package-private */
    public ArrayList<Integer> getUxThreadForPid(int pid) {
        ArrayList<Integer> list = new ArrayList<>();
        if (this.mTargetUxThreads.size() != 0) {
            Trace.traceBegin(64, "getUxThreadForPid:" + pid);
            try {
                File[] listFiles = new File("/proc/" + pid + "/task").listFiles();
                for (File p : listFiles) {
                    String threadName = readNativeProcNode(p.getAbsolutePath() + "/comm");
                    if (this.mTargetUxThreads.contains(threadName.trim())) {
                        if (DEBUG) {
                            Log.d(TAG, "add thread" + p.getName() + SettingsStringUtil.DELIMITER + threadName + " to Ux list for pid " + pid);
                        }
                        list.add(Integer.valueOf(p.getName()));
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "exception in getUxThreadForPid:" + pid);
                e.printStackTrace();
            }
            Trace.traceEnd(64);
        }
        return list;
    }

    private void boostThreadsToUx(int pid, ArrayList<Integer> threads, boolean boost) {
        if (threads == null || threads.size() == 0) {
            Log.w(TAG, "skip boostThreadsToUx for " + pid + "due to null thread list");
            return;
        }
        if (DEBUG) {
            Log.d(TAG, "boostThreadsToUx :" + pid + " to " + boost);
        }
        String tids = "" + pid;
        Iterator<Integer> it = threads.iterator();
        while (it.hasNext()) {
            Integer thread = it.next();
            setUxThread(pid, thread.intValue(), boost);
            tids = tids + SmsManager.REGEX_PREFIX_DELIMITER + thread;
        }
        if (boost) {
            Trace.asyncTraceBegin(64, "UX Boost: " + tids, pid);
            return;
        }
        Trace.asyncTraceEnd(64, "UX Boost: " + tids, pid);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.oppo.uifirst.UIFirstUtils.setUxThread(int, int, boolean):void
     arg types: [int, int, int]
     candidates:
      com.oppo.uifirst.UIFirstUtils.setUxThread(int, int, java.lang.String):void
      com.oppo.uifirst.UIFirstUtils.setUxThread(int, int, boolean):void */
    /* access modifiers changed from: private */
    public void handleAppStatusChanged(AppStatusParam param) {
        ArrayList<Integer> uxThreads;
        ArrayList<Integer> uxThreads2;
        if (param != null) {
            if (DEBUG) {
                Log.d(TAG, "handleAppStatusChanged --->" + param.status + " for pid " + param.pid);
            }
            int i = param.status;
            if (i == 0) {
                acquireLaunchBoost();
                setUxThread(param.pid, 0, true);
            } else if (i == 1) {
                ArrayList<Integer> uxThreads3 = getUxThreadForPid(param.pid);
                uxThreads3.add(Integer.valueOf(param.pid));
                if (param.renderTid != 0) {
                    uxThreads3.add(Integer.valueOf(param.renderTid));
                }
                boostThreadsToUx(param.pid, uxThreads3, true);
                synchronized (this.mBoostingProcs) {
                    if (DEBUG) {
                        Log.d(TAG, "updating uxThread for " + param.pid);
                    }
                    this.mBoostingProcs.put(Integer.valueOf(param.pid), uxThreads3);
                }
            } else if (i == 2) {
                synchronized (this.mBoostingProcs) {
                    uxThreads = this.mBoostingProcs.get(Integer.valueOf(param.pid));
                }
                if (uxThreads == null) {
                    Log.w(TAG, "ux threads for " + param.pid + " not found in cache, rebuild it");
                    uxThreads2 = getUxThreadForPid(param.pid);
                } else {
                    uxThreads2 = uxThreads;
                }
                boostThreadsToUx(param.pid, uxThreads2, false);
                synchronized (this.mBoostingProcs) {
                    this.mBoostingProcs.remove(Integer.valueOf(param.pid));
                }
            } else if (i == 3) {
                if (DEBUG) {
                    Log.d(TAG, "app:" + param.pid + "died");
                }
                synchronized (this.mBoostingProcs) {
                    this.mBoostingProcs.remove(Integer.valueOf(param.pid));
                }
            }
        }
    }

    public void onAppStatusChanged(int Status, int pid, Bundle b) {
        onAppStatusChanged(Status, pid, 0, b);
    }

    public void onAppStatusChanged(int Status, int pid, int renderTid) {
        onAppStatusChanged(Status, pid, renderTid, null);
    }

    public void onAppStatusChanged(int status, int pid, int renderTid, Bundle b) {
        if (DEBUG) {
            Log.d(TAG, "onAppStatusChanged :" + status + " for pid" + pid);
        }
        AppStatusParam param = new AppStatusParam();
        param.status = status;
        param.pid = pid;
        param.renderTid = renderTid;
        param.data = b;
        Message msg = Message.obtain();
        msg.what = 0;
        msg.obj = param;
        this.mHandler.sendMessage(msg);
    }

    private UIFirstUtils() {
        this.mTargetUxThreads.add("hwuiTask0");
        this.mTargetUxThreads.add("hwuiTask1");
        this.mThread = new HandlerThread(TAG, -2);
        this.mThread.start();
        this.mHandler = new UIFirstHandler(this.mThread.getLooper());
        setUxThread(Process.myPid(), this.mThread.getThreadId(), WifiEnterpriseConfig.ENGINE_ENABLE);
    }

    public static boolean inUxThreadStatus(int pid, int tid) {
        return WifiEnterpriseConfig.ENGINE_ENABLE.equals(readProcNode("proc/" + pid + "/task/" + tid + "/static_ux"));
    }

    public static void setUxThread(int pid, int tid, boolean boost) {
        if (pid != 0 && tid != 0) {
            writeProcNode("proc/" + pid + "/task/" + tid + "/static_ux", boost ? WifiEnterpriseConfig.ENGINE_ENABLE : WifiEnterpriseConfig.ENGINE_DISABLE);
            if (DEBUG) {
                Log.d(TAG, "setUxThread:" + pid + "@" + tid + " to " + inUxThreadStatus(pid, tid));
            }
        }
    }

    public static void setUxThread(int fgUiThreadPid, int fgRenderThreadTid, String val) {
        if (fgUiThreadPid != 0) {
            writeProcNode("proc/" + fgUiThreadPid + "/task/" + fgUiThreadPid + "/static_ux", val);
            if (fgRenderThreadTid != 0) {
                writeProcNode("proc/" + fgUiThreadPid + "/task/" + fgRenderThreadTid + "/static_ux", val);
            }
            if (WifiEnterpriseConfig.ENGINE_ENABLE.equals(val)) {
                Trace.asyncTraceBegin(64, "UX Boost: " + fgUiThreadPid, fgUiThreadPid);
            } else {
                Trace.asyncTraceEnd(64, "UX Boost: " + fgUiThreadPid, fgUiThreadPid);
            }
            if (DEBUG) {
                Log.d(TAG, "setUxThread:" + fgUiThreadPid + " to " + val);
            }
        }
    }

    public static void writeProcNode(String filePath, String val) {
        writeNativeProcNode(filePath, val);
    }

    public static String readProcNode(String filePath) {
        String[] firstline = readNativeProcNode(filePath).split("\n|\r");
        if (firstline.length < 1 || firstline[0] == null) {
            return null;
        }
        return firstline[0];
    }
}
