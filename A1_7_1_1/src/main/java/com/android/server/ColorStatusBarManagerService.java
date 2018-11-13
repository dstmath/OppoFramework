package com.android.server;

import android.app.IColorClickTopCallback;
import android.app.IColorStatusBar;
import android.app.IColorStatusBar.Stub;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Parcel;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;
import com.android.server.statusbar.StatusBarManagerService;
import com.android.server.wm.WindowManagerService;

public class ColorStatusBarManagerService extends StatusBarManagerService {
    private static final String TAG = "ColorStatusBarManagerService";
    private RemoteCallbackList<IColorClickTopCallback> mClickTopCallbackList = new RemoteCallbackList();
    private Handler mColorHandler = null;
    private IColorStatusBar mColorStatusBar;

    public ColorStatusBarManagerService(Context context, WindowManagerService windowManager) {
        super(context, windowManager);
        HandlerThread handlerThread = new HandlerThread("ColorClickTopThread");
        handlerThread.start();
        this.mColorHandler = new Handler(handlerThread.getLooper());
    }

    public void showNavigationBar() {
        if (this.mColorStatusBar != null) {
            try {
                this.mColorStatusBar.onNavigationBarShow();
                return;
            } catch (RemoteException e) {
                Log.e(TAG, "showNavigationBar RemoteException!");
                return;
            }
        }
        Log.e(TAG, "showNavigationBar mColorStatusBar == null!");
    }

    public void hideNavigationBar() {
        if (this.mColorStatusBar != null) {
            try {
                this.mColorStatusBar.onNavigationBarHide();
                return;
            } catch (RemoteException e) {
                Log.e(TAG, "hideNavigationBar RemoteException!");
                return;
            }
        }
        Log.e(TAG, "hideNavigationBar mColorStatusBar == null!");
    }

    public void showNavigationBarFromIme() {
        if (this.mColorStatusBar != null) {
            try {
                this.mColorStatusBar.onNavigationBarShowFromIme();
            } catch (RemoteException e) {
            }
        }
    }

    public void hideNavigationBarFromIme() {
        if (this.mColorStatusBar != null) {
            try {
                this.mColorStatusBar.onNavigationBarHideFromIme();
            } catch (RemoteException e) {
            }
        }
    }

    private void registerColorStatusBar(IColorStatusBar callback) {
        this.mColorStatusBar = callback;
    }

    private void registerColorClickTopCallback(IColorClickTopCallback callback) {
        this.mClickTopCallbackList.register(callback);
    }

    private void unregisterColorClickTopCallback(IColorClickTopCallback callback) {
        this.mClickTopCallbackList.unregister(callback);
    }

    private synchronized void notifyClickTop() {
        int n = this.mClickTopCallbackList.beginBroadcast();
        for (int i = 0; i < n; i++) {
            try {
                ((IColorClickTopCallback) this.mClickTopCallbackList.getBroadcastItem(i)).onClickTopCallback();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        this.mClickTopCallbackList.finishBroadcast();
    }

    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case 10002:
                data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                showNavigationBar();
                return true;
            case 10003:
                data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                hideNavigationBar();
                return true;
            case 10004:
                data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                showNavigationBarFromIme();
                return true;
            case 10005:
                data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                hideNavigationBarFromIme();
                return true;
            case 20002:
                data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                registerColorStatusBar(Stub.asInterface(data.readStrongBinder()));
                return true;
            case 20003:
                data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                registerColorClickTopCallback(IColorClickTopCallback.Stub.asInterface(data.readStrongBinder()));
                return true;
            case 20004:
                data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                this.mColorHandler.post(new Runnable() {
                    public void run() {
                        ColorStatusBarManagerService.this.notifyClickTop();
                    }
                });
                return true;
            case 20005:
                data.enforceInterface("com.android.internal.statusbar.IStatusBarService");
                unregisterColorClickTopCallback(IColorClickTopCallback.Stub.asInterface(data.readStrongBinder()));
                return true;
            default:
                return super.onTransact(code, data, reply, flags);
        }
    }
}
