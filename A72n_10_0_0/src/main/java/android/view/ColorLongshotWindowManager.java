package android.view;

import android.graphics.Rect;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

public class ColorLongshotWindowManager extends ColorBaseWindowManager implements IColorLongshotWindowManager {
    private static final String TAG = "ColorLongshotWindowManager";

    @Override // android.view.IColorLongshotWindowManager
    public void getFocusedWindowFrame(Rect frame) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            if (frame != null) {
                data.writeInt(1);
                frame.writeToParcel(data, 0);
            } else {
                data.writeInt(0);
            }
            this.mRemote.transact(IColorLongshotWindowManager.GET_FOCUSED_WINDOW_FRAME, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                frame.readFromParcel(reply);
            }
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorLongshotWindowManager
    public int getLongshotSurfaceLayer() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            this.mRemote.transact(IColorLongshotWindowManager.GET_LONGSHOT_SURFACE_LAYER, data, reply, 0);
            reply.readException();
            return reply.readInt();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorLongshotWindowManager
    public int getLongshotSurfaceLayerByType(int type) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            data.writeInt(type);
            this.mRemote.transact(IColorLongshotWindowManager.GET_LONGSHOT_SURFACE_LAYER_BY_TYPE, data, reply, 0);
            reply.readException();
            return reply.readInt();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorLongshotWindowManager
    public void longshotInjectInput(InputEvent event, int mode) throws RemoteException {
        Parcel data = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            if (event != null) {
                data.writeInt(1);
                event.writeToParcel(data, 0);
            } else {
                data.writeInt(0);
            }
            data.writeInt(mode);
            this.mRemote.transact(IColorLongshotWindowManager.LONGSHOT_INJECT_INPUT, data, null, 1);
        } finally {
            data.recycle();
        }
    }

    @Override // android.view.IColorLongshotWindowManager
    public void longshotNotifyConnected(boolean isConnected) throws RemoteException {
        Parcel data = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            data.writeInt(isConnected ? 1 : 0);
            this.mRemote.transact(IColorLongshotWindowManager.LONGSHOT_NOTIFY_CONNECTED, data, null, 1);
        } finally {
            data.recycle();
        }
    }

    @Override // android.view.IColorLongshotWindowManager
    public boolean isNavigationBarVisible() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            boolean result = false;
            this.mRemote.transact(IColorLongshotWindowManager.IS_NAVIGATIONBAR_VISIBLE, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                result = true;
            }
            return result;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorLongshotWindowManager
    public boolean isVolumeShow() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            boolean result = false;
            this.mRemote.transact(IColorLongshotWindowManager.IS_VOLUME_SHOW, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                result = true;
            }
            return result;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorLongshotWindowManager
    public boolean isShortcutsPanelShow() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            boolean result = false;
            this.mRemote.transact(IColorLongshotWindowManager.IS_SHORTCUTS_PANEL_SHOW, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                result = true;
            }
            return result;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorLongshotWindowManager
    public void longshotInjectInputBegin() throws RemoteException {
        Parcel data = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            this.mRemote.transact(IColorLongshotWindowManager.LONGSHOT_INJECT_INPUT_BEGIN, data, null, 1);
        } finally {
            data.recycle();
        }
    }

    @Override // android.view.IColorLongshotWindowManager
    public boolean isKeyguardShowingAndNotOccluded() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            boolean result = false;
            this.mRemote.transact(IColorLongshotWindowManager.IS_KEYGUARD_SHOWING_AND_NOT_OCCLUDED, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                result = true;
            }
            return result;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorLongshotWindowManager
    public void longshotInjectInputEnd() throws RemoteException {
        Parcel data = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            this.mRemote.transact(IColorLongshotWindowManager.LONGSHOT_INJECT_INPUT_END, data, null, 1);
        } finally {
            data.recycle();
        }
    }

    @Override // android.view.IColorLongshotWindowManager
    public IBinder getLongshotWindowByType(int type) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            data.writeInt(type);
            this.mRemote.transact(IColorLongshotWindowManager.GET_LONGSHOT_WINDOW_BY_TYPE, data, reply, 0);
            reply.readException();
            return reply.readStrongBinder();
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorLongshotWindowManager
    public boolean isFloatAssistExpand() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            boolean result = false;
            this.mRemote.transact(IColorLongshotWindowManager.IS_FLOAT_ASSIST_EXPAND, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                result = true;
            }
            return result;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorLongshotWindowManager
    public boolean isEdgePanelExpand() throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBaseWindowManager.DESCRIPTOR);
            boolean result = false;
            this.mRemote.transact(IColorLongshotWindowManager.IS_EDGE_PANEL_EXPAND, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                result = true;
            }
            return result;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }
}
