package android.app;

import android.content.Context;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;

public class ColorStatusBarManager implements IColorStatusBarManager {
    private IBinder mRemote = null;

    public void showNavigationBar() {
        if (this.mRemote == null) {
            this.mRemote = ServiceManager.getService(Context.STATUS_BAR_SERVICE);
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IColorStatusBarManager.DESCRIPTOR);
        try {
            this.mRemote.transact(10002, data, reply, 0);
        } catch (RemoteException e) {
        }
        data.recycle();
        reply.recycle();
    }

    public void hideNavigationBar() {
        if (this.mRemote == null) {
            this.mRemote = ServiceManager.getService(Context.STATUS_BAR_SERVICE);
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IColorStatusBarManager.DESCRIPTOR);
        try {
            this.mRemote.transact(10003, data, reply, 0);
        } catch (RemoteException e) {
        }
        data.recycle();
        reply.recycle();
    }

    public void updateTransitionView(int state) {
        if (this.mRemote == null) {
            this.mRemote = ServiceManager.getService(Context.STATUS_BAR_SERVICE);
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken(IColorStatusBarManager.DESCRIPTOR);
        data.writeInt(state);
        try {
            this.mRemote.transact(10004, data, reply, 0);
        } catch (RemoteException e) {
        }
        data.recycle();
        reply.recycle();
    }
}
