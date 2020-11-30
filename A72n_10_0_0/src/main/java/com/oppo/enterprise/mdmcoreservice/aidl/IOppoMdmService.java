package com.oppo.enterprise.mdmcoreservice.aidl;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IOppoMdmService extends IInterface {
    IBinder getManager(String str) throws RemoteException;

    boolean isPackageContainsOppoCertificates(String str) throws RemoteException;

    public static abstract class Stub extends Binder implements IOppoMdmService {
        private static final String DESCRIPTOR = "com.oppo.enterprise.mdmcoreservice.aidl.IOppoMdmService";
        static final int TRANSACTION_getManager = 1;
        static final int TRANSACTION_isPackageContainsOppoCertificates = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOppoMdmService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOppoMdmService)) {
                return new Proxy(obj);
            }
            return (IOppoMdmService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _result = getManager(data.readString());
                        reply.writeNoException();
                        reply.writeStrongBinder(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isPackageContainsOppoCertificates = isPackageContainsOppoCertificates(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isPackageContainsOppoCertificates ? 1 : 0);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IOppoMdmService {
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IOppoMdmService
            public IBinder getManager(String strManagerName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(strManagerName);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readStrongBinder();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IOppoMdmService
            public boolean isPackageContainsOppoCertificates(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
