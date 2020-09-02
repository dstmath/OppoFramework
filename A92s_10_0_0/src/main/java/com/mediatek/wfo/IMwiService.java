package com.mediatek.wfo;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.mediatek.wfo.IWifiOffloadService;

public interface IMwiService extends IInterface {
    IWifiOffloadService getWfcHandlerInterface() throws RemoteException;

    public static class Default implements IMwiService {
        @Override // com.mediatek.wfo.IMwiService
        public IWifiOffloadService getWfcHandlerInterface() throws RemoteException {
            return null;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMwiService {
        private static final String DESCRIPTOR = "com.mediatek.wfo.IMwiService";
        static final int TRANSACTION_getWfcHandlerInterface = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMwiService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMwiService)) {
                return new Proxy(obj);
            }
            return (IMwiService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                IWifiOffloadService _result = getWfcHandlerInterface();
                reply.writeNoException();
                reply.writeStrongBinder(_result != null ? _result.asBinder() : null);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IMwiService {
            public static IMwiService sDefaultImpl;
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

            @Override // com.mediatek.wfo.IMwiService
            public IWifiOffloadService getWfcHandlerInterface() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWfcHandlerInterface();
                    }
                    _reply.readException();
                    IWifiOffloadService _result = IWifiOffloadService.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IMwiService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMwiService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
