package com.mediatek.common.omadm;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

public interface IOmadmManager extends IInterface {
    ParcelFileDescriptor inputStream(String str) throws RemoteException;

    public static class Default implements IOmadmManager {
        @Override // com.mediatek.common.omadm.IOmadmManager
        public ParcelFileDescriptor inputStream(String path) throws RemoteException {
            return null;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOmadmManager {
        private static final String DESCRIPTOR = "com.mediatek.common.omadm.IOmadmManager";
        static final int TRANSACTION_inputStream = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOmadmManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOmadmManager)) {
                return new Proxy(obj);
            }
            return (IOmadmManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                ParcelFileDescriptor _result = inputStream(data.readString());
                reply.writeNoException();
                if (_result != null) {
                    reply.writeInt(1);
                    _result.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IOmadmManager {
            public static IOmadmManager sDefaultImpl;
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

            @Override // com.mediatek.common.omadm.IOmadmManager
            public ParcelFileDescriptor inputStream(String path) throws RemoteException {
                ParcelFileDescriptor _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(path);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().inputStream(path);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ParcelFileDescriptor) ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IOmadmManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOmadmManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
