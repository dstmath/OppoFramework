package android.os;

import com.color.os.IColorScreenStatusListener;

public class ColorCommonPowerManager implements IColorCommonPowerManager {
    private final IBinder mRemote;

    public ColorCommonPowerManager(IBinder remote) {
        this.mRemote = remote;
    }

    @Override // android.os.IColorCommonPowerManager
    public void registerScreenStatusListener(IColorScreenStatusListener listener) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBasePowerManager.DESCRIPTOR);
            if (listener != null) {
                data.writeInt(1);
                data.writeStrongBinder(listener.asBinder());
            } else {
                data.writeInt(0);
            }
            transact(ColorCommonPowerTransaction.REGISTER_SCREEN_STATUS_LISTENER, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    @Override // android.os.IColorCommonPowerManager
    public void unregisterScreenStatusListener(IColorScreenStatusListener listener) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(IColorBasePowerManager.DESCRIPTOR);
            if (listener != null) {
                data.writeInt(1);
                data.writeStrongBinder(listener.asBinder());
            } else {
                data.writeInt(0);
            }
            transact(ColorCommonPowerTransaction.UNREGISTER_SCREEN_STATUS_LISTENER, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    private void transact(ColorCommonPowerTransaction code, Parcel data, Parcel reply, int flags) throws RemoteException {
        this.mRemote.transact(code.ordinal() + 10001, data, reply, flags);
    }
}
