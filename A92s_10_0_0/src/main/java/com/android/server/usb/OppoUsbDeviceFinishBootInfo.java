package com.android.server.usb;

public class OppoUsbDeviceFinishBootInfo {
    private String mContentStr = null;

    public OppoUsbDeviceFinishBootInfo(boolean connected, boolean bootCompleted, boolean currentUsbFunctionsReceived, boolean systemReady, boolean pendingBootBroadcast, boolean screenLocked, String screenUnlockedFunctions, boolean isAdbEnabled) {
        this.mContentStr = "UsbBootInfo[" + "connected:" + connected + ", bootComplete:" + bootCompleted + ", curUsbFunRec:" + currentUsbFunctionsReceived + ", systemReady:" + systemReady + ", pendingBoot:" + pendingBootBroadcast + ", screenLock:" + screenLocked + ", screenUnlock:" + screenUnlockedFunctions + ", adbEnable:" + isAdbEnabled;
    }

    public String toString() {
        return this.mContentStr;
    }
}
