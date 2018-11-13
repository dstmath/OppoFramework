package com.mediatek.fpspolicy;

import android.os.RemoteException;
import android.view.IDockedStackListener.Stub;
import android.view.WindowManagerGlobal;
import com.android.server.input.InputWindowHandle;

public class FpsInfo extends Stub {
    public static final int FLAG_MULTI_WINDOW = 1;
    private static FpsInfo sInstance;

    private native void nativeSetInputWindows(InputWindowHandle[] inputWindowHandleArr);

    private native void nativeSetWindowFlag(int i, int i2);

    private FpsInfo() {
        try {
            WindowManagerGlobal.getWindowManagerService().registerDockedStackListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setInputWindows(InputWindowHandle[] windowHandles) {
        nativeSetInputWindows(windowHandles);
    }

    public void setWindowFlag(int flag, int mask) {
        nativeSetWindowFlag(flag, mask);
    }

    public void onDividerVisibilityChanged(boolean visible) throws RemoteException {
    }

    public void onDockedStackExistsChanged(boolean exists) throws RemoteException {
        if (exists) {
            nativeSetWindowFlag(1, 1);
        } else {
            nativeSetWindowFlag(0, 1);
        }
    }

    public void onDockedStackMinimizedChanged(boolean minimized, long animDuration) throws RemoteException {
    }

    public void onAdjustedForImeChanged(boolean adjustedForIme, long animDuration) throws RemoteException {
    }

    public void onDockSideChanged(int newDockSide) throws RemoteException {
    }

    public static FpsInfo getInstance() {
        if (sInstance != null) {
            return sInstance;
        }
        sInstance = new FpsInfo();
        return sInstance;
    }
}
