package com.color.oshare;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IColorOshareCallback extends IInterface {
    void onDeviceChanged(List<ColorOshareDevice> list) throws RemoteException;

    void onSendSwitchChanged(boolean z) throws RemoteException;

    public static class Default implements IColorOshareCallback {
        @Override // com.color.oshare.IColorOshareCallback
        public void onDeviceChanged(List<ColorOshareDevice> list) throws RemoteException {
        }

        @Override // com.color.oshare.IColorOshareCallback
        public void onSendSwitchChanged(boolean isOn) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IColorOshareCallback {
        private static final String DESCRIPTOR = "com.color.oshare.IColorOshareCallback";
        static final int TRANSACTION_onDeviceChanged = 1;
        static final int TRANSACTION_onSendSwitchChanged = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IColorOshareCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IColorOshareCallback)) {
                return new Proxy(obj);
            }
            return (IColorOshareCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onDeviceChanged";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "onSendSwitchChanged";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onDeviceChanged(data.createTypedArrayList(ColorOshareDevice.CREATOR));
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onSendSwitchChanged(data.readInt() != 0);
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IColorOshareCallback {
            public static IColorOshareCallback sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // com.color.oshare.IColorOshareCallback
            public void onDeviceChanged(List<ColorOshareDevice> deviceList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(deviceList);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onDeviceChanged(deviceList);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.color.oshare.IColorOshareCallback
            public void onSendSwitchChanged(boolean isOn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isOn ? 1 : 0);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onSendSwitchChanged(isOn);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IColorOshareCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IColorOshareCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
