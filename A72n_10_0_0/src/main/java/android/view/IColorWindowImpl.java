package android.view;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import com.color.direct.ColorDirectFindCmd;
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

    @Override // android.view.IColorLongshotWindow
    public void longshotNotifyConnected(boolean isConnected) throws RemoteException {
        Parcel data = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorWindow.DESCRIPTOR);
            data.writeInt(isConnected ? 1 : 0);
            this.mRemote.transact(10002, data, null, 1);
        } finally {
            data.recycle();
        }
    }

    @Override // android.view.IColorLongshotWindow
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

    @Override // android.view.IColorLongshotWindow
    public ColorWindowNode longshotCollectWindow(boolean isStatusBar, boolean isNavigationBar) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        ColorWindowNode result = null;
        try {
            data.writeInterfaceToken(IColorWindow.DESCRIPTOR);
            int i = 1;
            data.writeInt(isStatusBar ? 1 : 0);
            if (!isNavigationBar) {
                i = 0;
            }
            data.writeInt(i);
            this.mRemote.transact(10004, data, reply, 0);
            reply.readException();
            if (reply.readInt() != 0) {
                result = ColorWindowNode.CREATOR.createFromParcel(reply);
            }
            return result;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    @Override // android.view.IColorLongshotWindow
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

    @Override // android.view.IColorLongshotWindow
    public void longshotInjectInputBegin() throws RemoteException {
        Parcel data = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorWindow.DESCRIPTOR);
            this.mRemote.transact(10006, data, null, 1);
        } finally {
            data.recycle();
        }
    }

    @Override // android.view.IColorLongshotWindow
    public void longshotInjectInputEnd() throws RemoteException {
        Parcel data = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorWindow.DESCRIPTOR);
            this.mRemote.transact(10007, data, null, 1);
        } finally {
            data.recycle();
        }
    }

    @Override // android.view.IColorLongshotWindow
    public void screenshotDump(FileDescriptor fd) throws RemoteException {
        Parcel data = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorWindow.DESCRIPTOR);
            data.writeFileDescriptor(fd);
            this.mRemote.transact(10009, data, null, 1);
        } finally {
            data.recycle();
        }
    }

    @Override // android.view.IColorDirectWindow
    public void directFindCmd(ColorDirectFindCmd findCmd) throws RemoteException {
        Parcel data = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorWindow.DESCRIPTOR);
            if (findCmd != null) {
                data.writeInt(1);
                findCmd.writeToParcel(data, 0);
            } else {
                data.writeInt(0);
            }
            this.mRemote.transact(10008, data, null, 1);
        } finally {
            data.recycle();
        }
    }
}
