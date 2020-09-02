package android.os;

import android.util.Log;

public class OppoBasePowerManager {
    private static final int BRIGHTNESS_ELEVEN_BITS = 2047;
    public static int BRIGHTNESS_MULTIBITS_ON = 255;
    private static final int BRIGHTNESS_TEN_BITS = 1023;
    public static final int GO_TO_SLEEP_REASON_FINGERPRINT = 10;
    public static final int GO_TO_SLEEP_REASON_PROXIMITY = 9;
    public static final int WAKE_REASON_DOUBLE_HOME = 99;
    public static final int WAKE_REASON_DOUBLE_TAP_SCREEN = 100;
    public static final int WAKE_REASON_LIFT_HAND = 101;
    public static final int WAKE_REASON_PROXIMITY = 97;
    public static final int WAKE_REASON_WINDOWMANAGER_TURN_SCREENON = 102;
    public static final String WAKE_UP_DUE_TO_DOUBLE_HOME = "android.service.fingerprint:DOUBLE_HOME";
    public static final String WAKE_UP_DUE_TO_DOUBLE_TAP_SCREEN = "oppo.wakeup.gesture:DOUBLE_TAP_SCREEN";
    public static final String WAKE_UP_DUE_TO_FINGERPRINT = "android.service.fingerprint:WAKEUP";
    public static final String WAKE_UP_DUE_TO_LIFT_HAND = "oppo.wakeup.gesture:LIFT_HAND";
    public static final String WAKE_UP_DUE_TO_PROXIMITY = "android.service.power:proximity";
    public static final String WAKE_UP_DUE_TO_WINDOWMANAGER_TURN_SCREENON = "android.server.wm:SCREEN_ON_FLAG";
    public static final int WAKE_UP_REASON_FINGERPRINT = 98;

    /* access modifiers changed from: package-private */
    public void printStackTraceInfo(String methodName) {
        StackTraceElement[] stack;
        if ("user".equals(Build.TYPE) && (stack = new Throwable().getStackTrace()) != null) {
            int i = 0;
            while (i < 5 && i < stack.length) {
                Log.i("PowerManager", methodName + "    |----" + stack[i].toString());
                i++;
            }
        }
    }
}
