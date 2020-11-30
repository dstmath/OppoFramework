package android.os;

import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.IColorBaseActivityManager;
import android.media.TtmlUtils;
import android.text.TextUtils;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class LooperMessageSuperviser {
    private static final String TAG = "LooperMessageSuperviser";
    private static final int THEIA_UI_DURATION = 3000;
    private boolean isTracing = false;

    private String getPackageName() {
        String packageName = ActivityThread.currentPackageName();
        if (packageName == null) {
            return "system_server";
        }
        return packageName;
    }

    private boolean isForegroudApp(int pid) {
        String forePid = SystemProperties.get("debug.junk.process.pid");
        String curPid = null;
        try {
            curPid = Integer.toString(pid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return forePid.equals(curPid);
    }

    private int getSchedGroup(int pid) {
        try {
            return Process.getProcessGroup(pid);
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }

    private int getThreadSchedulePolicy(int pid) {
        try {
            return Process.getThreadScheduler(pid);
        } catch (Exception e) {
            return 0;
        }
    }

    private void uploadLongTimeMessage(long processTime, Message message, long timeFirst, String procValue, int pid) {
        StringBuilder sb = new StringBuilder();
        new StringBuilder();
        sb.append("Package name: ");
        sb.append(getPackageName());
        sb.append(" [ ");
        sb.append("schedGroup: ");
        sb.append(getSchedGroup(pid));
        sb.append(" schedPolicy: ");
        sb.append(getThreadSchedulePolicy(pid));
        sb.append(" ]");
        sb.append(" process the message: ");
        sb.append(getStringLiteOfMessage(message, processTime + timeFirst, true));
        sb.append(" took ");
        sb.append(processTime);
        sb.append(" ms");
        if (!TextUtils.isEmpty(procValue)) {
            sb.append(" ( ");
            sb.append(procValue);
            sb.append(" )");
        }
        sb.append("\n");
        logP("Quality", "Blocked msg = " + sb.toString());
    }

    public void beginLooperMessage(Message msg, int pid) {
        if (OppoDebug.DEBUG_SYSTRACE_TAG) {
            Trace.traceBegin(8, msg.target.getTraceName(msg));
        }
    }

    public void endLooperMessage(Message msg, long mStartTime, int pid) {
        if (OppoDebug.DEBUG_SYSTRACE_TAG) {
            Trace.traceEnd(8);
        }
        if (isForegroudApp(pid)) {
            long processTime = SystemClock.uptimeMillis() - mStartTime;
            if (processTime >= ((long) OppoDebug.LOOPER_DELAY)) {
                uploadLongTimeMessage(processTime, msg, mStartTime, "", pid);
            }
            if (processTime >= 3000) {
                Object am = ActivityManager.getService();
                if (am == null) {
                    Log.w(TAG, "No activity manager; failed to Dropbox violation.");
                    return;
                }
                callDeclaredMethod(am, IColorBaseActivityManager.DESCRIPTOR, "sendTheiaEvent", new Class[]{String.class}, new Object[]{"theia"});
            }
        }
    }

    private String oppoUiFirstFunction_readProcNode(String procNode) {
        Object proValue = callDeclaredMethod(null, "com.oppo.uifirst.UIFirstUtils", "readProcNode", new Class[]{String.class}, new Object[]{procNode});
        if (proValue == null) {
            return null;
        }
        return (String) proValue;
    }

    private void oppoUiFirstFunction_writeProcNode(String procNode, String proValue) {
        callDeclaredMethod(null, "com.oppo.uifirst.UIFirstUtils", "writeProcNode", new Class[]{String.class, String.class}, new Object[]{procNode, proValue});
    }

    private String getStringLiteOfMessage(Message msg, long nowTime, boolean showObj) {
        Object contentObj = callDeclaredMethod(msg, "android.os.Message", "toStringLite", new Class[]{Long.TYPE, Boolean.TYPE}, new Object[]{Long.valueOf(nowTime), Boolean.valueOf(showObj)});
        if (contentObj == null || !(contentObj instanceof String)) {
            return null;
        }
        return (String) contentObj;
    }

    private void logP(String tag, String content) {
        callDeclaredMethod(null, "android.util.Log", TtmlUtils.TAG_P, new Class[]{String.class, String.class}, new Object[]{tag, content});
    }

    private Object callDeclaredMethod(Object target, String cls_name, String method_name, Class[] parameterTypes, Object[] args) {
        try {
            Method method = Class.forName(cls_name).getDeclaredMethod(method_name, parameterTypes);
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (ClassNotFoundException e) {
            Log.i(TAG, "ClassNotFoundException : " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (NoSuchMethodException e2) {
            Log.i(TAG, "NoSuchMethodException : " + e2.getMessage());
            e2.printStackTrace();
            return null;
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
            return null;
        } catch (InvocationTargetException e4) {
            e4.printStackTrace();
            return null;
        } catch (SecurityException e5) {
            e5.printStackTrace();
            return null;
        }
    }
}
