package android.app;

import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class ColorNotificationManager extends ColorBaseNotificationManager implements IColorNotificationManager {
    private static final String TAG = "ColorNotificationManager";

    @Override // android.app.IColorNotificationManager
    public boolean shouldInterceptSound(String pkg, int uid) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorNotificationManager.DESCRIPTOR);
            data.writeString(pkg);
            data.writeInt(uid);
            this.mRemote.transact(10002, data, reply, 0);
            reply.readException();
            return Boolean.valueOf(reply.readString()).booleanValue();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorNotificationManager
    public boolean shouldKeepAlive(String pkg, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorNotificationManager.DESCRIPTOR);
            data.writeString(pkg);
            data.writeInt(userId);
            this.mRemote.transact(10003, data, reply, 0);
            reply.readException();
            return Boolean.valueOf(reply.readString()).booleanValue();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorNotificationManager
    public int getNavigationMode(String pkg, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorNotificationManager.DESCRIPTOR);
            data.writeString(pkg);
            data.writeInt(userId);
            this.mRemote.transact(10004, data, reply, 0);
            reply.readException();
            return reply.readInt();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorNotificationManager
    public boolean isDriveNavigationMode(String pkg, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorNotificationManager.DESCRIPTOR);
            data.writeString(pkg);
            data.writeInt(userId);
            this.mRemote.transact(10005, data, reply, 0);
            reply.readException();
            return Boolean.valueOf(reply.readString()).booleanValue();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorNotificationManager
    public boolean isNavigationMode(int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorNotificationManager.DESCRIPTOR);
            data.writeInt(userId);
            this.mRemote.transact(10006, data, reply, 0);
            reply.readException();
            return Boolean.valueOf(reply.readString()).booleanValue();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    /* JADX INFO: finally extract failed */
    @Override // android.app.IColorNotificationManager
    public String[] getEnableNavigationApps(int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        List<String> result = new ArrayList<>();
        try {
            data.writeInterfaceToken(IColorNotificationManager.DESCRIPTOR);
            data.writeInt(userId);
            this.mRemote.transact(10007, data, reply, 0);
            reply.readException();
            reply.readStringList(result);
            data.recycle();
            reply.recycle();
            return (String[]) result.toArray(new String[result.size()]);
        } catch (Throwable th) {
            data.recycle();
            reply.recycle();
            throw th;
        }
    }

    @Override // android.app.IColorNotificationManager
    public boolean isSuppressedByDriveMode(int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorNotificationManager.DESCRIPTOR);
            data.writeInt(userId);
            this.mRemote.transact(10008, data, reply, 0);
            reply.readException();
            return Boolean.valueOf(reply.readString()).booleanValue();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorNotificationManager
    public void setSuppressedByDriveMode(boolean mode, int userId) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorNotificationManager.DESCRIPTOR);
            data.writeString(String.valueOf(mode));
            data.writeInt(userId);
            this.mRemote.transact(10009, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorNotificationManager
    public String getOpenid(String pkg, int uid, String type) throws RemoteException {
        Log.d(TAG, "getOpenid--pkg:" + pkg + ",uid:" + uid + ",type:" + type);
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorNotificationManager.DESCRIPTOR);
            data.writeString(pkg);
            data.writeInt(uid);
            data.writeString(type);
            this.mRemote.transact(10010, data, reply, 0);
            reply.readException();
            return reply.readString();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorNotificationManager
    public void clearOpenid(String pkg, int uid, String type) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorNotificationManager.DESCRIPTOR);
            data.writeString(pkg);
            data.writeInt(uid);
            data.writeString(type);
            this.mRemote.transact(10011, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorNotificationManager
    public boolean checkGetOpenid(String pkg, int uid, String type) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorNotificationManager.DESCRIPTOR);
            data.writeString(pkg);
            data.writeInt(uid);
            data.writeString(type);
            this.mRemote.transact(10018, data, reply, 0);
            reply.readException();
            return Boolean.valueOf(reply.readString()).booleanValue();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorNotificationManager
    public void setBadgeOption(String pkg, int uid, int option) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorNotificationManager.DESCRIPTOR);
            data.writeString(pkg);
            data.writeInt(uid);
            data.writeInt(option);
            this.mRemote.transact(10012, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorNotificationManager
    public int getBadgeOption(String pkg, int uid) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorNotificationManager.DESCRIPTOR);
            data.writeString(pkg);
            data.writeInt(uid);
            this.mRemote.transact(10013, data, reply, 0);
            reply.readException();
            return reply.readInt();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorNotificationManager
    public boolean isNumbadgeSupport(String pkg, int uid) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorNotificationManager.DESCRIPTOR);
            data.writeString(pkg);
            data.writeInt(uid);
            this.mRemote.transact(10014, data, reply, 0);
            reply.readException();
            return Boolean.valueOf(reply.readString()).booleanValue();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorNotificationManager
    public void setNumbadgeSupport(String pkg, int uid, boolean support) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorNotificationManager.DESCRIPTOR);
            data.writeString(pkg);
            data.writeInt(uid);
            data.writeString(String.valueOf(support));
            this.mRemote.transact(10015, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorNotificationManager
    public int getStowOption(String pkg, int uid) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorNotificationManager.DESCRIPTOR);
            data.writeString(pkg);
            data.writeInt(uid);
            this.mRemote.transact(10016, data, reply, 0);
            reply.readException();
            return Integer.valueOf(reply.readString()).intValue();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.app.IColorNotificationManager
    public void setStowOption(String pkg, int uid, int option) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorNotificationManager.DESCRIPTOR);
            data.writeString(pkg);
            data.writeInt(uid);
            data.writeString(String.valueOf(option));
            this.mRemote.transact(10017, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }
}
