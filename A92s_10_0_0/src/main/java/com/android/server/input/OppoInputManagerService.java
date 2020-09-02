package com.android.server.input;

import android.os.SystemProperties;
import com.oppo.debug.InputLog;
import java.io.PrintWriter;

public class OppoInputManagerService {
    public static final String PROPERTY_LIBINPUT = "persist.sys.libinput.enabled";
    public static final String PROPERTY_LIBINPUTFLINGER = "persist.sys.libinputflinger.enabled";
    static final String TAG = "InputManager";
    private InputManagerNativeCallback mInputManagerCallback = null;
    private final InputManagerService mInputManagerService;
    public boolean mIsOpenLibinput = SystemProperties.getBoolean(PROPERTY_LIBINPUT, false);
    public boolean mIsOpenLibinputflinger = SystemProperties.getBoolean(PROPERTY_LIBINPUTFLINGER, false);
    private final long mPtr;

    public interface InputManagerNativeCallback {
        void onNativeDynamicallyConfigLog(long j, boolean z, boolean z2);

        String requestAdjustNativeDump();
    }

    public OppoInputManagerService(long ptr, InputManagerService service) {
        this.mInputManagerService = service;
        this.mPtr = ptr;
        InputLog.initLogSwitch();
    }

    /* access modifiers changed from: protected */
    public void dynamicallyConfigLogTag(PrintWriter pw, String[] args) {
        pw.println("dynamicallyConfigLogTag, args.length:" + args.length);
        for (int index = 0; index < args.length; index++) {
            pw.println("dynamicallyConfigLogTag, args[" + index + "]: " + args[index]);
        }
        if (args.length != 3) {
            pw.println("********** Invalid argument! Get detail help as bellow: **********");
            logoutTagConfigHelp(pw);
            return;
        }
        String tag = args[1];
        boolean on = "1".equals(args[2]);
        pw.println("dynamicallyConfigLogTag, tag: " + tag + ", on: " + on);
        if ("all".equals(tag)) {
            if (on) {
                this.mIsOpenLibinputflinger = true;
                this.mIsOpenLibinput = true;
            } else {
                this.mIsOpenLibinputflinger = false;
                this.mIsOpenLibinput = false;
            }
            InputLog.dynamicLog(on);
            requestNativeConfigLog(this.mPtr, on, on);
        }
        if ("framework".equals(tag)) {
            InputLog.dynamicLog(on);
        }
        if ("libinputflinger".equals(tag)) {
            if (on) {
                this.mIsOpenLibinputflinger = true;
            } else {
                this.mIsOpenLibinputflinger = false;
            }
            requestNativeConfigLog(this.mPtr, this.mIsOpenLibinputflinger, this.mIsOpenLibinput);
        }
        if ("libinput".equals(tag)) {
            if (on) {
                this.mIsOpenLibinput = true;
            } else {
                this.mIsOpenLibinput = false;
            }
            requestNativeConfigLog(this.mPtr, this.mIsOpenLibinputflinger, this.mIsOpenLibinput);
        }
        SystemProperties.set(PROPERTY_LIBINPUTFLINGER, String.valueOf(this.mIsOpenLibinputflinger));
        SystemProperties.set(PROPERTY_LIBINPUT, String.valueOf(this.mIsOpenLibinput));
    }

    /* access modifiers changed from: protected */
    public void logoutTagConfigHelp(PrintWriter pw) {
        pw.println("********************** Help begin:**********************");
        pw.println("1. open all log in libinput, InputReader, InputDispatcher, framework");
        pw.println("cmd: dumpsys input log all 0/1");
        pw.println("2. open all log in framework");
        pw.println("cmd: dumpsys input log framework 0/1");
        pw.println("3. open all log in InputReader, InputDispatcher");
        pw.println("cmd: dumpsys input log libinputflinger 0/1");
        pw.println("4. open all log in libinput");
        pw.println("cmd: dumpsys input log libinput 0/1");
        pw.println("----------------------------------");
        pw.println("********************** Help end.  **********************");
    }

    public void enableNativeDynamicallyConfigLog() {
        if ("on".equals(InputLog.getCurrentLogSwitchValue())) {
            requestNativeConfigLog(this.mPtr, this.mIsOpenLibinputflinger, this.mIsOpenLibinput);
        }
    }

    public boolean dynamicallyAdjustDump(PrintWriter pw, String[] args) {
        String opt;
        int opti = 0;
        while (opti < args.length && (opt = args[opti]) != null && opt.length() > 0 && opt.charAt(0) == '-') {
            opti++;
            if ("-h".equals(opt)) {
                pw.println("input manager dump options:");
                pw.println("  [-h] [cmd] ...");
                pw.println("  cmd may be one of:");
                pw.println("    l[log]: dynamically adjust input log ");
                return true;
            }
            pw.println("Unknown argument: " + opt + "; use -h for help");
        }
        if (opti < args.length) {
            String cmd = args[opti];
            int opti2 = opti + 1;
            if ("log".equals(cmd) || "l".equals(cmd)) {
                dynamicallyConfigLogTag(pw, args);
                return true;
            } else if ("debug_switch".equals(cmd)) {
                boolean debug_switch = false;
                if ("on".equals(InputLog.getCurrentLogSwitchValue())) {
                    debug_switch = true;
                }
                pw.println("  all=" + debug_switch);
                return true;
            }
        }
        InputManagerNativeCallback inputManagerNativeCallback = this.mInputManagerCallback;
        String dumpStr = inputManagerNativeCallback != null ? inputManagerNativeCallback.requestAdjustNativeDump() : null;
        if (dumpStr != null) {
            pw.println(dumpStr);
            pw.println("Current Java LogSwitch : " + InputLog.getCurrentLogSwitchValue());
        }
        return false;
    }

    public void printinjectInputEventInternalErrorLog(int pid) {
        InputLog.w(TAG, "Input event injection from pid " + pid + " failed.");
    }

    public void setInputManagerNativeCallback(InputManagerNativeCallback callback) {
        this.mInputManagerCallback = callback;
    }

    private void requestNativeConfigLog(long ptr, boolean isOpenLibinputflinger, boolean isOpenLibinput) {
        InputManagerNativeCallback inputManagerNativeCallback = this.mInputManagerCallback;
        if (inputManagerNativeCallback != null) {
            inputManagerNativeCallback.onNativeDynamicallyConfigLog(ptr, isOpenLibinputflinger, isOpenLibinput);
        }
    }
}
