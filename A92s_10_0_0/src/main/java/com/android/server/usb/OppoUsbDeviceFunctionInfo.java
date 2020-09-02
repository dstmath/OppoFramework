package com.android.server.usb;

public class OppoUsbDeviceFunctionInfo {
    private String mContentStr = null;

    public OppoUsbDeviceFunctionInfo(String functions, String oemFunctions, String currentFunctions, boolean currentFunctionsApplied, boolean forceRestart, String currentOemFunctions) {
        this.mContentStr = "UsbFunc[" + "fun:" + functions + ", oemFun:" + oemFunctions + ", curFun:" + currentFunctions + ", curFunApplied:" + currentFunctionsApplied + ", forceRestart:" + forceRestart + ", curOemFun:" + currentOemFunctions;
    }

    public String toString() {
        return this.mContentStr;
    }
}
