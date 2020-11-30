package android.os;

import android.util.Log;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/* access modifiers changed from: package-private */
public class LooperMsgTimeTracker {
    private static final long DISPATCH_TIMEOUT = 1500;
    private static final String TAG = "ANR_LOG";
    private Message mCurrentMsg;
    private long mStartTime;

    LooperMsgTimeTracker() {
    }

    public void start(Message msg) {
        this.mStartTime = SystemClock.uptimeMillis();
        this.mCurrentMsg = msg;
    }

    public void stop() {
        long cost = SystemClock.uptimeMillis() - this.mStartTime;
        if (cost >= DISPATCH_TIMEOUT) {
            dumpMsgListWhenAnr(cost);
        }
    }

    private void dumpMsgListWhenAnr(long cost) {
        try {
            Log.e(TAG, ">>> msg's executing time is too long");
            Log.e(TAG, "Blocked msg = " + getStringLiteOfMessage(this.mCurrentMsg, this.mStartTime + cost, true) + " , cost  = " + cost + " ms");
            int n = 0;
            Log.e(TAG, ">>>Current msg List is:");
            Message tmp = Looper.myLooper().mQueue.mMessages;
            while (true) {
                if (tmp == null) {
                    break;
                }
                n++;
                if (n > 10) {
                    break;
                }
                Log.e(TAG, "Current msg <" + n + "> = " + getStringLiteOfMessage(tmp, this.mStartTime + cost, true));
                tmp = tmp.next;
            }
            Log.e(TAG, ">>>CURRENT MSG DUMP OVER<<<");
        } catch (Exception e) {
            Log.e(TAG, "Failure log ANR msg." + e);
        }
    }

    private String getStringLiteOfMessage(Message msg, long nowTime, boolean showObj) {
        Object contentObj = callDeclaredMethod(msg, "android.os.Message", "toStringLite", new Class[]{Long.TYPE, Boolean.TYPE}, new Object[]{Long.valueOf(nowTime), Boolean.valueOf(showObj)});
        if (contentObj == null || !(contentObj instanceof String)) {
            return null;
        }
        return (String) contentObj;
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
