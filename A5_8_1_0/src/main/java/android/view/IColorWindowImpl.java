package android.view;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import com.color.view.analysis.ColorWindowNode;
import java.io.FileDescriptor;
import java.util.List;

public class IColorWindowImpl implements IColorWindow {
    private static final String TAG = "IColorWindowImpl";
    private final IBinder mRemote;

    private IColorWindowImpl(IWindow client) {
        this(client.asBinder());
    }

    private IColorWindowImpl(IBinder remote) {
        this.mRemote = remote;
    }

    public static IColorWindow asInterface(IWindow client) {
        if (client == null) {
            return null;
        }
        return new IColorWindowImpl(client);
    }

    public static IColorWindow asInterface(IBinder remote) {
        return new IColorWindowImpl(remote);
    }

    public void longshotNotifyConnected(boolean isConnected) throws RemoteException {
        int i = 1;
        Parcel data = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorWindow.DESCRIPTOR);
            if (!isConnected) {
                i = 0;
            }
            data.writeInt(i);
            this.mRemote.transact(10002, data, null, 1);
        } finally {
            data.recycle();
        }
    }

    public void longshotDump(FileDescriptor fd, List<ColorWindowNode> systemWindows, List<ColorWindowNode> floatWindows) throws RemoteException {
        Parcel data = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorWindow.DESCRIPTOR);
            data.writeFileDescriptor(fd);
            if (systemWindows != null) {
                data.writeInt(1);
                data.writeTypedList(systemWindows);
            } else {
                data.writeInt(0);
            }
            if (floatWindows != null) {
                data.writeInt(1);
                data.writeTypedList(floatWindows);
            } else {
                data.writeInt(0);
            }
            this.mRemote.transact(10003, data, null, 1);
        } finally {
            data.recycle();
        }
    }

    public ColorWindowNode longshotCollectWindow(boolean isStatusBar, boolean isNavigationBar) throws RemoteException {
        int i = 1;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        ColorWindowNode colorWindowNode = null;
        try {
            int i2;
            data.writeInterfaceToken(IColorWindow.DESCRIPTOR);
            if (isStatusBar) {
                i2 = 1;
            } else {
                i2 = 0;
            }
            data.writeInt(i2);
            if (!isNavigationBar) {
                i = 0;
            }
            data.writeInt(i);
            this.mRemote.transact(10004, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                colorWindowNode = (ColorWindowNode) ColorWindowNode.CREATOR.createFromParcel(reply);
            }
            reply.recycle();
            data.recycle();
            return colorWindowNode;
        } catch (Throwable th) {
            reply.recycle();
            data.recycle();
        }
    }

    public void longshotInjectInput(InputEvent event, int mode) throws RemoteException {
        Parcel data = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorWindow.DESCRIPTOR);
            if (event != null) {
                data.writeInt(1);
                event.writeToParcel(data, 0);
            } else {
                data.writeInt(0);
            }
            data.writeInt(mode);
            this.mRemote.transact(10005, data, null, 1);
        } finally {
            data.recycle();
        }
    }

    public void longshotInjectInputBegin() throws RemoteException {
        Parcel data = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorWindow.DESCRIPTOR);
            this.mRemote.transact(10006, data, null, 1);
        } finally {
            data.recycle();
        }
    }

    public void longshotInjectInputEnd() throws RemoteException {
        Parcel data = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorWindow.DESCRIPTOR);
            this.mRemote.transact(10007, data, null, 1);
        } finally {
            data.recycle();
        }
    }
}
