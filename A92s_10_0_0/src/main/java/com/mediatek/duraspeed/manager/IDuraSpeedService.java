package com.mediatek.duraspeed.manager;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IDuraSpeedService extends IInterface {
    List<String> getPlatformWhitelist() throws RemoteException;

    void setAppWhitelist(List<String> list) throws RemoteException;

    public static class Default implements IDuraSpeedService {
        @Override // com.mediatek.duraspeed.manager.IDuraSpeedService
        public List<String> getPlatformWhitelist() throws RemoteException {
            return null;
        }

        @Override // com.mediatek.duraspeed.manager.IDuraSpeedService
        public void setAppWhitelist(List<String> list) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IDuraSpeedService {
        private static final String DESCRIPTOR = "com.mediatek.duraspeed.manager.IDuraSpeedService";
        static final int TRANSACTION_getPlatformWhitelist = 1;
        static final int TRANSACTION_setAppWhitelist = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDuraSpeedService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDuraSpeedService)) {
                return new Proxy(obj);
            }
            return (IDuraSpeedService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                List<String> _result = getPlatformWhitelist();
                reply.writeNoException();
                reply.writeStringList(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                setAppWhitelist(data.createStringArrayList());
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IDuraSpeedService {
            public static IDuraSpeedService sDefaultImpl;
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

            @Override // com.mediatek.duraspeed.manager.IDuraSpeedService
            public List<String> getPlatformWhitelist() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPlatformWhitelist();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.duraspeed.manager.IDuraSpeedService
            public void setAppWhitelist(List<String> appWhitelist) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(appWhitelist);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAppWhitelist(appWhitelist);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IDuraSpeedService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IDuraSpeedService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
