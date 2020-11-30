package android.support.test.internal.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import com.alibaba.fastjson.asm.Opcodes;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ProcessUtil {
    private static final List<Integer> RETRY_WAIT_INTERVALS = Collections.unmodifiableList(Arrays.asList(8, 8, 16, 32, 64, Integer.valueOf((int) Opcodes.IOR), 256));
    private static String processName;

    public static String getCurrentProcessName(Context context) {
        if (!TextUtils.isEmpty(processName)) {
            return processName;
        }
        try {
            processName = getCurrentProcessNameUsingActivityManager(context);
            if (processName.isEmpty()) {
                Log.w("ProcessUtil", "Could not figure out process name using ActivityManager, falling back to use /proc. Note that processName fetched from /proc may be truncated!");
                processName = getCurrentProcessNameUsingProc();
                if (processName.isEmpty()) {
                    Log.w("ProcessUtil", "Could not figure out process name /proc either");
                }
            }
            return processName;
        } catch (SecurityException e) {
            Log.i("ProcessUtil", "Could not read process name from ActivityManager (isolatedProcess?)");
            return "";
        }
    }

    static String getCurrentProcessNameUsingActivityManager(Context context) {
        InterruptedException ie;
        int pid = Process.myPid();
        ActivityManager activityManager = (ActivityManager) context.getSystemService("activity");
        if (activityManager != null) {
            List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
            if (runningAppProcesses == null) {
                int retryAttempt = 0;
                while (runningAppProcesses == null && retryAttempt < RETRY_WAIT_INTERVALS.size()) {
                    try {
                        Log.i("ProcessUtil", "Waiting for running app processes...");
                        int retryAttempt2 = retryAttempt + 1;
                        try {
                            Thread.sleep((long) RETRY_WAIT_INTERVALS.get(retryAttempt).intValue());
                            runningAppProcesses = activityManager.getRunningAppProcesses();
                            retryAttempt = retryAttempt2;
                        } catch (InterruptedException e) {
                            ie = e;
                            Log.w("ProcessUtil", "Interrupted while waiting for running app processes", ie);
                            return "";
                        }
                    } catch (InterruptedException e2) {
                        ie = e2;
                        Log.w("ProcessUtil", "Interrupted while waiting for running app processes", ie);
                        return "";
                    }
                }
            }
            for (ActivityManager.RunningAppProcessInfo processInfo : emptyIfNull(runningAppProcesses)) {
                if (processInfo.pid == pid) {
                    return processInfo.processName;
                }
            }
            Log.w("ProcessUtil", "Couldn't get running processes from ActivityManager!");
            return "";
        }
        StringBuilder sb = new StringBuilder(89);
        sb.append("ActivityManager#getRunningAppProcesses did not return an entry matching pid = ");
        sb.append(pid);
        Log.w("ProcessUtil", sb.toString());
        return "";
    }

    private static <E> Iterable<E> emptyIfNull(Iterable<E> iterable) {
        return iterable == null ? Collections.emptyList() : iterable;
    }

    static String getCurrentProcessNameUsingProc() {
        BufferedReader br = null;
        String processName2 = "";
        try {
            br = new BufferedReader(new FileReader("/proc/self/cmdline"));
            processName2 = br.readLine().trim();
            try {
                br.close();
            } catch (Exception e) {
                Log.w("ProcessUtil", e.getMessage(), e);
            }
        } catch (IOException e2) {
            Log.e("ProcessUtil", e2.getMessage(), e2);
            if (br != null) {
                br.close();
            }
        } catch (Throwable th) {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e3) {
                    Log.w("ProcessUtil", e3.getMessage(), e3);
                }
            }
            throw th;
        }
        return processName2;
    }

    static void resetProcessName() {
        processName = "";
    }
}
