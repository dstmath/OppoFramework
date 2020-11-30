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
import android.util.IntArray;
import android.util.Log;
import android.util.SparseArray;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class UIFirstUtils {
    private static final long APP_MOVE_TO_BG_DELAY = 1000;
    public static final int APP_STATUS_MOVE_TO_BG = 2;
    public static final int APP_STATUS_MOVE_TO_FG = 1;
    public static final int APP_STATUS_PROC_DIE = 3;
    public static final int APP_STATUS_START_ACTIVITY = 0;
    private static final boolean DEBUG = (!SystemProperties.getBoolean("ro.build.release_type", false));
    private static final long LAUNCH_BOOST_DURATION = 3000;
    private static final int MSG_APP_MOVE_TO_BG = 2;
    private static final int MSG_APP_MOVE_TO_BG_DELAY = 3;
    private static final int MSG_APP_STATUS_CHANGED = 0;
    private static final int MSG_DISABLE_LAUNCH_BOOST = 1;
    private static final String TAG = "UIFirstUtils";
    private static final String UIFIRST_LAUNCH_BOOST_PATH = "/proc/sys/kernel/launcher_boost_enabled";
    private static UIFirstUtils sInstance = null;
    private SparseArray<IntArray> mBoostingProcs = new SparseArray<>();
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
                    if (UIFirstUtils.DEBUG) {
                        Log.d(UIFirstUtils.TAG, "disable launch boost..");
                    }
                    UIFirstUtils.writeProcNode(UIFirstUtils.UIFIRST_LAUNCH_BOOST_PATH, WifiEnterpriseConfig.ENGINE_DISABLE);
                } else if (i == 2) {
                    UIFirstUtils.this.handleAppStatusChanged((AppStatusParam) msg.obj);
                } else if (i == 3) {
                    int appPid = msg.arg1;
                    int renderThreadTid = msg.arg2;
                    UIFirstUtils.setUxThread(appPid, appPid, false);
                    UIFirstUtils.setUxThread(appPid, renderThreadTid, false);
                    int[] hwuiTasks = (int[]) msg.obj;
                    for (int i2 : hwuiTasks) {
                        UIFirstUtils.setUxThread(appPid, i2, false);
                    }
                    UIFirstUtils.this.uxTrace(appPid, renderThreadTid, hwuiTasks, false);
                }
            } catch (Exception e) {
                Log.w(UIFirstUtils.TAG, "exception in handle msg :");
                e.printStackTrace();
            }
        }
    }

    public void acquireLaunchBoost() {
        if (DEBUG) {
            Log.d(TAG, "acquireLaunchBoost");
        }
        if (this.mHandler.hasMessages(1)) {
            this.mHandler.removeMessages(1);
        } else {
            writeProcNode(UIFIRST_LAUNCH_BOOST_PATH, WifiEnterpriseConfig.ENGINE_ENABLE);
        }
        this.mHandler.sendEmptyMessageDelayed(1, LAUNCH_BOOST_DURATION);
    }

    /* access modifiers changed from: package-private */
    public IntArray getUxThreadForPid(int pid) {
        IntArray list = new IntArray(4);
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
                        list.add(Integer.valueOf(p.getName()).intValue());
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

    private void boostThreadsToUx(int pid, IntArray threads, boolean boost) {
        if (threads == null || threads.size() == 0) {
            Log.w(TAG, "skip boostThreadsToUx for " + pid + "due to null thread list");
            return;
        }
        if (DEBUG) {
            Log.d(TAG, "boostThreadsToUx :" + pid + " to " + boost);
        }
        String tids = "" + pid;
        for (int i = 0; i < threads.size(); i++) {
            int thread = threads.get(i);
            setUxThread(pid, thread, boost);
            tids = tids + SmsManager.REGEX_PREFIX_DELIMITER + thread;
        }
        if (boost) {
            Trace.asyncTraceBegin(64, "UX Boost: " + tids, pid);
            return;
        }
        Trace.asyncTraceEnd(64, "UX Boost: " + tids, pid);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAppStatusChanged(AppStatusParam param) {
        IntArray uxThreads;
        IntArray uxThreads2;
        if (param != null) {
            if (DEBUG) {
                Log.d(TAG, "handleAppStatusChanged --->" + param.status + " for pid " + param.pid);
            }
            int i = param.status;
            if (i == 0) {
                setUxThread(param.pid, 0, true);
            } else if (i == 1) {
                boolean isLauncher = false;
                if (param.data != null && "com.oppo.launcher".equals(param.data.getString("packageName"))) {
                    isLauncher = true;
                    this.mHandler.removeMessages(2);
                }
                synchronized (this.mBoostingProcs) {
                    uxThreads = this.mBoostingProcs.get(param.pid);
                }
                if (uxThreads == null || uxThreads.indexOf(param.renderTid) == -1 || (isLauncher && uxThreads.size() <= 2)) {
                    uxThreads = getUxThreadForPid(param.pid);
                    if (uxThreads.indexOf(param.pid) == -1) {
                        uxThreads.add(param.pid);
                    }
                    if (param.renderTid != 0 && uxThreads.indexOf(param.renderTid) == -1) {
                        uxThreads.add(param.renderTid);
                    }
                }
                boostThreadsToUx(param.pid, uxThreads, true);
                synchronized (this.mBoostingProcs) {
                    if (DEBUG) {
                        Log.d(TAG, "updating uxThread for " + param.pid);
                    }
                    this.mBoostingProcs.put(param.pid, uxThreads);
                }
            } else if (i != 2) {
                if (i == 3) {
                    if (DEBUG) {
                        Log.d(TAG, "app:" + param.pid + "died");
                    }
                    synchronized (this.mBoostingProcs) {
                        this.mBoostingProcs.remove(param.pid);
                    }
                }
            } else if (param.data == null || !"com.oppo.launcher".equals(param.data.getString("packageName"))) {
                synchronized (this.mBoostingProcs) {
                    uxThreads2 = this.mBoostingProcs.get(param.pid);
                }
                if (uxThreads2 != null) {
                    boostThreadsToUx(param.pid, uxThreads2, false);
                }
            } else {
                Message msg = Message.obtain();
                msg.what = 2;
                param.data = null;
                msg.obj = param;
                this.mHandler.sendMessageDelayed(msg, 1000);
            }
        }
    }

    public void onAppStatusChanged(int Status, int pid, Bundle b) {
        onAppStatusChanged(Status, pid, 0, b);
    }

    public void onAppStatusChanged(int Status, int pid, int renderTid) {
        onAppStatusChanged(Status, pid, renderTid, (Bundle) null);
    }

    public void onAppStatusChanged(int Status, int pid, String packageName, int renderTid) {
        Bundle b = new Bundle();
        b.putString("packageName", packageName);
        onAppStatusChanged(Status, pid, renderTid, b);
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

    public static void setUxThreadValue(int pid, int tid, String value) {
        if (pid != 0 && tid != 0) {
            writeProcNode("proc/" + pid + "/task/" + tid + "/static_ux", value);
            if (DEBUG) {
                Log.d(TAG, "setUxThread:" + pid + "@" + tid + " to " + value);
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

    /* access modifiers changed from: package-private */
    public void uxTrace(int appPid, int renderThreadTid, int[] threads, boolean boost) {
        if (Trace.isTagEnabled(64)) {
            StringBuilder tids = new StringBuilder();
            tids.append(appPid);
            if (renderThreadTid > 0) {
                tids.append(SmsManager.REGEX_PREFIX_DELIMITER);
                tids.append(renderThreadTid);
            }
            if (threads.length > 0) {
                tids.append(SmsManager.REGEX_PREFIX_DELIMITER);
                tids.append((String) Arrays.stream(threads).filter($$Lambda$UIFirstUtils$5wetPcpZUAJlG1Eur2vF83Q6HHs.INSTANCE).mapToObj($$Lambda$UIFirstUtils$WlytLbIpxvl6cKneUHinMxfjYhI.INSTANCE).collect(Collectors.joining(SmsManager.REGEX_PREFIX_DELIMITER)));
            }
            String tidsStr = tids.toString();
            if (boost) {
                Trace.asyncTraceBegin(64, "UX Boost: " + tidsStr, appPid);
                return;
            }
            Trace.asyncTraceEnd(64, "UX Boost: " + tidsStr, appPid);
        }
    }

    static /* synthetic */ boolean lambda$uxTrace$0(int e) {
        return e > 0;
    }

    public void adjustUxProcess(int status, int appPid, int renderThreadTid, IntArray hwuiTasks, String packageName, boolean isRemoteAnimation) {
        if (status == 1) {
            if (packageName != null && "com.oppo.launcher".equals(packageName)) {
                this.mHandler.removeMessages(3);
            }
            String value = isRemoteAnimation ? WifiEnterpriseConfig.ENGINE_ENABLE : "2";
            setUxThreadValue(appPid, appPid, value);
            setUxThreadValue(appPid, renderThreadTid, value);
            for (int i = 0; i < hwuiTasks.size(); i++) {
                setUxThread(appPid, hwuiTasks.get(i), true);
            }
            uxTrace(appPid, renderThreadTid, hwuiTasks.toArray(), true);
        } else if (status == 2) {
            if (packageName == null || !"com.oppo.launcher".equals(packageName)) {
                setUxThread(appPid, appPid, false);
                setUxThread(appPid, renderThreadTid, false);
                for (int i2 = 0; i2 < hwuiTasks.size(); i2++) {
                    setUxThread(appPid, hwuiTasks.get(i2), false);
                }
                uxTrace(appPid, renderThreadTid, hwuiTasks.toArray(), false);
                return;
            }
            this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 3, appPid, renderThreadTid, hwuiTasks.toArray()), 1000);
        }
    }
}
