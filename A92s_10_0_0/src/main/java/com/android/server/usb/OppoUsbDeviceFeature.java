package com.android.server.usb;

import android.hardware.usb.UsbManager;
import android.os.SystemProperties;
import android.util.Slog;

public class OppoUsbDeviceFeature implements IOppoUsbDeviceFeature {
    private static final boolean DBG_FUN = true;
    private static final boolean DBG_METHOD = true;
    private static final String TAG = "OppoUsbDeviceFeature";
    private boolean mIsInited;

    private OppoUsbDeviceFeature() {
        this.mIsInited = false;
    }

    private static class InstanceHolder {
        static final OppoUsbDeviceFeature INSTANCE = new OppoUsbDeviceFeature();

        private InstanceHolder() {
        }
    }

    public static OppoUsbDeviceFeature getInstance() {
        Slog.d(TAG, "getInstance.");
        OppoUsbDeviceFeature instance = InstanceHolder.INSTANCE;
        instance.init();
        return instance;
    }

    private void init() {
        if (!this.mIsInited) {
            this.mIsInited = true;
        }
    }

    public void setUsbAccessoryStartFlag() {
        Slog.d(TAG, "setUsbAccessoryStartFlag");
        if ("false".equals(SystemProperties.get("persist.sys.accessory", "false"))) {
            Slog.d(TAG, "persist.sys.accessory got");
            SystemProperties.set("persist.sys.accessory", "true");
        }
    }

    public void initUsbAccessoryStartFlag() {
        Slog.d(TAG, "initUsbAccessoryStartFlag");
        if ("true".equals(SystemProperties.get("persist.sys.accessory", "false"))) {
            Slog.d(TAG, "accessory state init");
            SystemProperties.set("persist.sys.accessory", "false");
        }
    }

    public boolean getUsbAccessoryStartFlag(long functions) {
        Slog.d(TAG, "getUsbAccessoryStartFlag");
        if (!"true".equals(SystemProperties.get("persist.sys.accessory", "false")) || !containsFunction(UsbManager.usbFunctionsToString(functions), "midi")) {
            return false;
        }
        Slog.i(TAG, "usb accessory mode,will not response the default usb config before accessory plug out");
        return true;
    }

    public void initUsbPlugFlag() {
        Slog.d(TAG, "initUsbPlugFlag");
        SystemProperties.set("persist.sys.usb.plugout", "false");
    }

    public void setUsbPlugFlag(boolean connected) {
        Slog.d(TAG, "setUsbPlugFlag");
        if (connected) {
            SystemProperties.set("persist.sys.usb.plugout", "false");
        } else {
            SystemProperties.set("persist.sys.usb.plugout", "true");
        }
    }

    public boolean usbFunctionsShouldForceStart() {
        Slog.d(TAG, "usbFunctionsShouldForceStart");
        if ("false".equals(SystemProperties.get("persist.sys.permission.enable", "true"))) {
            return true;
        }
        if (!SystemProperties.getBoolean("persist.sys.usb.boot_enable", false) || "adb".equals(SystemProperties.get("sys.usb.state", "midi"))) {
            return false;
        }
        return true;
    }

    public boolean usbFunctionsShuoldUseDefault(String functions) {
        Slog.d(TAG, "usbFunctionsShuoldUseDefault");
        Slog.i(TAG, "usb plug out, current functions=" + functions);
        if (!containsFunction(functions, "rndis")) {
            return false;
        }
        Slog.i(TAG, "usb plug out, rndis should close!");
        return true;
    }

    public void printFinishBootInfo(OppoUsbDeviceFinishBootInfo bootInfo) {
        Slog.d(TAG, "printFinishBootInfo");
        StringBuilder sb = new StringBuilder();
        sb.append("bootInfo:");
        sb.append(bootInfo != null ? bootInfo.toString() : "null");
        Slog.d(TAG, sb.toString());
    }

    public long getChargingFunctions() {
        Slog.d(TAG, "getChargingFunctions");
        if (SystemProperties.getBoolean("persist.sys.usb.boot_enable", false)) {
            return 1;
        }
        return 8;
    }

    public boolean isTelecomRequirement(String functions) {
        Slog.d(TAG, "isTelecomRequirement");
        if (!SystemProperties.getBoolean("persist.sys.usbshare", false) || !containsFunction(functions, "rndis")) {
            return false;
        }
        Slog.i(TAG, "set Tethering rndis,serial_cdev,diag,adb...");
        return true;
    }

    public void printBootModeForDebug(String bootMode) {
        Slog.d(TAG, "printBootModeForDebug, bootMode:" + bootMode);
    }

    public boolean isNormalBoot() {
        Slog.d(TAG, "isNormalBoot");
        String bootMode = SystemProperties.get("ro.bootmode", "unknown");
        Slog.i(TAG, "bootMode = " + bootMode);
        return bootMode.equals("normal") || bootMode.equals("unknown");
    }

    public boolean ignoreCTARequirement(String functions) {
        Slog.d(TAG, "IgnoreCTARequirement");
        if (containsFunction(functions, "mtp") || containsFunction(functions, "ptp") || SystemProperties.getBoolean("persist.sys.allcommode", false) || ((!SystemProperties.getBoolean("persist.sys.permission.enable", true) && !SystemProperties.getBoolean("persist.sys.cta", false)) || ((SystemProperties.getBoolean("ro.debuggable", false) && !SystemProperties.getBoolean("persist.sys.cta", false)) || SystemProperties.getBoolean("persist.sys.usb.plugout", false) || containsFunction(functions, "accessory")))) {
            Slog.i(TAG, "ingore disable adb");
            return true;
        } else if (!containsFunction(functions, "adb") || containsFunction(functions, "rndis") || containsFunction(functions, "via_bypass")) {
            return true;
        } else {
            Slog.i(TAG, "try set disable adb");
            if (SystemProperties.getBoolean("persist.sys.usb.plugout", false)) {
                Slog.i(TAG, "usb has plug out, reset");
                SystemProperties.set("persist.sys.usb.plugout", "false");
            }
            return false;
        }
    }

    public boolean usbFunctionsNotForceRestart(String functions) {
        Slog.d(TAG, "usbFunctionsNotForceRestart");
        if (SystemProperties.getBoolean("persist.sys.usb.plugout", false)) {
            Slog.i(TAG, "usb has plug out");
            SystemProperties.set("persist.sys.usb.plugout", "false");
            if (isNormalBoot() || (!containsFunction(functions, "mtp") && !containsFunction(functions, "ptp"))) {
                return false;
            }
            return true;
        }
        return false;
    }

    public String resetFunctionsForCTA(String functions) {
        String functions2;
        Slog.d(TAG, "resetFunctionsForCTA");
        if (functions.equals("adb")) {
            functions2 = "midi";
        } else {
            functions2 = removeFunction(functions, "adb");
            if (functions2.equals("none")) {
                functions2 = "midi";
            }
        }
        if (SystemProperties.getBoolean("persist.sys.usb.boot_enable", false)) {
            return "adb";
        }
        return functions2;
    }

    public void printFunctionsForDebug(OppoUsbDeviceFunctionInfo functionInfo) {
        Slog.d(TAG, "printFunctionsForDebug");
        StringBuilder sb = new StringBuilder();
        sb.append("functionInfo:");
        sb.append(functionInfo != null ? functionInfo.toString() : "null");
        Slog.d(TAG, sb.toString());
    }

    public void usbConfigRecord(String functions) {
        Slog.d(TAG, "usbConfigRecord");
    }

    private boolean containsFunction(String functions, String function) {
        int index = functions.indexOf(function);
        if (index < 0) {
            return false;
        }
        if (index > 0 && functions.charAt(index - 1) != ',') {
            return false;
        }
        int charAfter = function.length() + index;
        if (charAfter >= functions.length() || functions.charAt(charAfter) == ',') {
            return true;
        }
        return false;
    }

    private String removeFunction(String functions, String function) {
        String[] split = functions.split(",");
        for (int i = 0; i < split.length; i++) {
            if (function.equals(split[i])) {
                split[i] = null;
            }
        }
        if (split.length == 1 && split[0] == null) {
            return "none";
        }
        StringBuilder builder = new StringBuilder();
        for (String s : split) {
            if (s != null) {
                if (builder.length() > 0) {
                    builder.append(",");
                }
                builder.append(s);
            }
        }
        return builder.toString();
    }
}
