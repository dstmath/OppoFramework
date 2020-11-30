package com.mediatek.common.multiwindow;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IMWPhoneWindowCallback extends IInterface {
    void setFloatDecorVisibility(int i) throws RemoteException;

    void setWindowType(IBinder iBinder, int i) throws RemoteException;

    public static class Default implements IMWPhoneWindowCallback {
        @Override // com.mediatek.common.multiwindow.IMWPhoneWindowCallback
        public void setWindowType(IBinder token, int windowType) throws RemoteException {
        }

        @Override // com.mediatek.common.multiwindow.IMWPhoneWindowCallback
        public void setFloatDecorVisibility(int visibility) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMWPhoneWindowCallback {
        private static final String DESCRIPTOR = "com.mediatek.common.multiwindow.IMWPhoneWindowCallback";
        static final int TRANSACTION_setFloatDecorVisibility = 2;
        static final int TRANSACTION_setWindowType = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMWPhoneWindowCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMWPhoneWindowCallback)) {
                return new Proxy(obj);
            }
            return (IMWPhoneWindowCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                setWindowType(data.readStrongBinder(), data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                setFloatDecorVisibility(data.readInt());
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
        public static class Proxy implements IMWPhoneWindowCallback {
            public static IMWPhoneWindowCallback sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // com.mediatek.common.multiwindow.IMWPhoneWindowCallback
            public void setWindowType(IBinder token, int windowType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(windowType);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setWindowType(token, windowType);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMWPhoneWindowCallback
            public void setFloatDecorVisibility(int visibility) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(visibility);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setFloatDecorVisibility(visibility);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IMWPhoneWindowCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMWPhoneWindowCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
