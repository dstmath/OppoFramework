package com.mediatek.aee;

import android.os.SystemProperties;
import android.util.Log;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExceptionLogImpl extends ExceptionLog {
    public static final byte AEE_EXCEPTION_JNI = 1;
    public static final byte AEE_WARNING_JNI = 0;
    public static final String TAG = "AES";
    private final String FILE_OBSERVER_NULL_PATH = "Unhandled exception in FileObserver com.android.server.BootReceiver";
    private final String SEND_NON_PROTECTED_BROADCAST = "Sending non-protected broadcast";
    private final String[] protectedBroadcastFilter = {"android.intent.action.CALL_EMERGENCY", "com.debug.loggerui.ADB_CMD", "com.mediatek.log2server.EXCEPTION_HAPPEND", "com.mediatek.omacp.capability.result", "com.mediatek.autounlock", "com.mtk.autotest.heartset.stop", "com.mtk.fts.ACTION", "com.android.systemui.demo", "ATG_MQTT_MqttService.pingSender"};

    private static native long SFMatter(long j, long j2);

    private static native void WDTMatter(long j);

    private static native boolean getNativeExceptionPidListImpl(int[] iArr);

    private static native void report(String str, String str2, String str3, String str4, String str5, long j);

    private static native void switchFtraceImpl(int i);

    private static native void systemreportImpl(byte b, String str, String str2, String str3, String str4);

    static {
        Log.i(TAG, "load Exception Log jni");
        System.loadLibrary("mediatek_exceptionlog");
    }

    public void handle(String type, String info, String pid) {
        long lpid;
        boolean z;
        Log.w(TAG, "Exception Log handling...");
        if (!type.startsWith("data_app") || info.contains("com.android.development") || SystemProperties.getInt("persist.vendor.mtk.aee.filter", 1) != 1) {
            String pkgs = "";
            String detail = "";
            long lpid2 = 0;
            String[] splitInfo = info.split("\n+");
            Pattern procMatcher = Pattern.compile("^Process:\\s+(.*)");
            Pattern pkgMatcher = Pattern.compile("^Package:\\s+(.*)");
            int length = splitInfo.length;
            String proc = "";
            int i = 0;
            while (i < length) {
                String s = splitInfo[i];
                Matcher m = procMatcher.matcher(s);
                if (m.matches()) {
                    lpid = lpid2;
                    proc = m.group(1);
                } else {
                    lpid = lpid2;
                }
                Matcher m2 = pkgMatcher.matcher(s);
                if (m2.matches()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(pkgs);
                    z = true;
                    sb.append(m2.group(1));
                    sb.append("\n");
                    pkgs = sb.toString();
                } else {
                    z = true;
                }
                i++;
                detail = detail;
                lpid2 = lpid;
            }
            long lpid3 = lpid2;
            if (!pid.equals("")) {
                lpid3 = Long.parseLong(pid);
            }
            if (type.equals("system_server_wtf")) {
                if (isSkipSystemWtfReport(info)) {
                    return;
                }
            }
            report(proc, pkgs, info, "Backtrace of all threads:\n\n", type, lpid3);
            return;
        }
        Log.w(TAG, "Skipped - do not care third party apk");
    }

    public void systemreport(byte Type, String Module, String Msg, String Path) {
        systemreportImpl(Type, Module, getThreadStackTrace(), Msg, Path);
    }

    public boolean getNativeExceptionPidList(int[] pidList) {
        return getNativeExceptionPidListImpl(pidList);
    }

    public void switchFtrace(int config) {
        switchFtraceImpl(config);
    }

    private static String getThreadStackTrace() {
        Writer traces = new StringWriter();
        try {
            Thread th = Thread.currentThread();
            StackTraceElement[] st = th.getStackTrace();
            StringBuilder sb = new StringBuilder();
            sb.append("\"");
            sb.append(th.getName());
            sb.append("\" ");
            sb.append(th.isDaemon() ? "daemon" : "");
            sb.append(" prio=");
            sb.append(th.getPriority());
            sb.append(" Thread id=");
            sb.append(th.getId());
            sb.append(" ");
            sb.append(th.getState());
            sb.append("\n");
            traces.write(sb.toString());
            for (StackTraceElement line : st) {
                traces.write("\t" + line + "\n");
            }
            traces.write("\n");
            return traces.toString();
        } catch (IOException e) {
            return "IOException";
        } catch (OutOfMemoryError e2) {
            return "java.lang.OutOfMemoryError";
        }
    }

    private static String getAllThreadStackTraces() {
        Map<Thread, StackTraceElement[]> st = Thread.getAllStackTraces();
        Writer traces = new StringWriter();
        try {
            for (Map.Entry<Thread, StackTraceElement[]> e : st.entrySet()) {
                StackTraceElement[] el = e.getValue();
                Thread th = e.getKey();
                StringBuilder sb = new StringBuilder();
                sb.append("\"");
                sb.append(th.getName());
                sb.append("\" ");
                sb.append(th.isDaemon() ? "daemon" : "");
                sb.append(" prio=");
                sb.append(th.getPriority());
                sb.append(" Thread id=");
                sb.append(th.getId());
                sb.append(" ");
                sb.append(th.getState());
                sb.append("\n");
                traces.write(sb.toString());
                for (StackTraceElement line : el) {
                    traces.write("\t" + line + "\n");
                }
                traces.write("\n");
            }
            return traces.toString();
        } catch (IOException e2) {
            return "IOException";
        } catch (OutOfMemoryError e3) {
            return "java.lang.OutOfMemoryError";
        }
    }

    public void WDTMatterJava(long lParam) {
        WDTMatter(lParam);
    }

    public long SFMatterJava(long setorget, long lParam) {
        return SFMatter(setorget, lParam);
    }

    private boolean isSkipSystemWtfReport(String info) {
        if (!isSkipReportFromProtectedBroadcast(info) && !isSkipReportFromNullFilePath(info)) {
            return false;
        }
        return true;
    }

    private boolean isSkipReportFromProtectedBroadcast(String info) {
        if (!info.contains("Sending non-protected broadcast")) {
            return false;
        }
        int i = 0;
        while (true) {
            String[] strArr = this.protectedBroadcastFilter;
            if (i >= strArr.length) {
                return false;
            }
            if (info.contains(strArr[i])) {
                return true;
            }
            i++;
        }
    }

    private boolean isSkipReportFromNullFilePath(String info) {
        if (info.contains("Unhandled exception in FileObserver com.android.server.BootReceiver")) {
            return true;
        }
        return false;
    }
}
