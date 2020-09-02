package android.app;

import android.os.Parcel;
import android.os.RemoteException;

public class ColorStatusBarManager extends ColorBaseStatusBarManager implements IColorStatusBarManager {
    public static final int FLAG_INPUT_METHOD_SHOW = 4;
    public static final int FLAG_KEYGUARD_SHOW = 1;
    public static final int FLAG_SCREEN_ON = 8;
    public static final int FLAG_SCREEN_SHOT_SHOW = 2;
    public static final int TOGGLE_SPLIT_SCREEN_FROM_MENU = 2;
    public static final int TOGGLE_SPLIT_SCREEN_FROM_NONE = -1;
    public static final int TOGGLE_SPLIT_SCREEN_FROM_RECENT = 3;
    public static final int TOGGLE_SPLIT_SCREEN_FROM_SERVICE = 1;

    @Override // android.app.IColorStatusBarManager
    public void registerColorStatusBar(IColorStatusBar callback) throws RemoteException {
        Parcel data = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorStatusBarManager.DESCRIPTOR);
            data.writeStrongBinder(callback.asBinder());
            this.mRemote.transact(10002, data, null, 1);
        } finally {
            data.recycle();
        }
    }

    @Override // android.app.IColorStatusBarManager
    public void registerColorClickTopCallback(IColorClickTopCallback callback) throws RemoteException {
        Parcel data = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorStatusBarManager.DESCRIPTOR);
            data.writeStrongBinder(callback.asBinder());
            this.mRemote.transact(10003, data, null, 1);
        } finally {
            data.recycle();
        }
    }

    @Override // android.app.IColorStatusBarManager
    public void notifyClickTop() throws RemoteException {
        Parcel data = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorStatusBarManager.DESCRIPTOR);
            this.mRemote.transact(10004, data, null, 1);
        } finally {
            data.recycle();
        }
    }

    @Override // android.app.IColorStatusBarManager
    public void unregisterColorClickTopCallback(IColorClickTopCallback callback) throws RemoteException {
        Parcel data = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorStatusBarManager.DESCRIPTOR);
            data.writeStrongBinder(callback.asBinder());
            this.mRemote.transact(10005, data, null, 1);
        } finally {
            data.recycle();
        }
    }

    @Override // android.app.IColorStatusBarManager
    public boolean getTopIsFullscreen() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorStatusBarManager.DESCRIPTOR);
            boolean result = false;
            this.mRemote.transact(10006, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                result = true;
            }
            return result;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorStatusBarManager
    public void toggleSplitScreen(int mode) throws RemoteException {
        Parcel data = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorStatusBarManager.DESCRIPTOR);
            data.writeInt(mode);
            this.mRemote.transact(10007, data, null, 1);
        } finally {
            data.recycle();
        }
    }

    @Override // android.app.IColorStatusBarManager
    public boolean setStatusBarFunction(int functionCode, String pkgName) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorStatusBarManager.DESCRIPTOR);
            data.writeInt(functionCode);
            data.writeString(pkgName);
            boolean result = false;
            this.mRemote.transact(10008, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                result = true;
            }
            return result;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorStatusBarManager
    public void topIsFullscreen(boolean topActivityIsFullscreen) throws RemoteException {
        Parcel data = Parcel.obtain();
        int result = topActivityIsFullscreen ? 1 : 0;
        try {
            data.writeInterfaceToken(IColorStatusBarManager.DESCRIPTOR);
            data.writeInt(result);
            this.mRemote.transact(10009, data, null, 1);
        } finally {
            data.recycle();
        }
    }

    @Override // android.app.IColorStatusBarManager
    public void notifyMultiWindowFocusChanged(int state) throws RemoteException {
        Parcel data = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorStatusBarManager.DESCRIPTOR);
            data.writeInt(state);
            this.mRemote.transact(10010, data, null, 1);
        } finally {
            data.recycle();
        }
    }
}
