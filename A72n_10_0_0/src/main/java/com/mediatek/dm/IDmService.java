package com.mediatek.dm;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IDmService extends IInterface {
    int getDmSupported() throws RemoteException;

    boolean getImcProvision(int i, int i2) throws RemoteException;

    boolean setImcProvision(int i, int i2, int i3) throws RemoteException;

    public static class Default implements IDmService {
        @Override // com.mediatek.dm.IDmService
        public int getDmSupported() throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.dm.IDmService
        public boolean getImcProvision(int phoneId, int feature) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.dm.IDmService
        public boolean setImcProvision(int phoneId, int feature, int pvs_en) throws RemoteException {
            return false;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IDmService {
        private static final String DESCRIPTOR = "com.mediatek.dm.IDmService";
        static final int TRANSACTION_getDmSupported = 1;
        static final int TRANSACTION_getImcProvision = 2;
        static final int TRANSACTION_setImcProvision = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDmService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDmService)) {
                return new Proxy(obj);
            }
            return (IDmService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _result = getDmSupported();
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                boolean imcProvision = getImcProvision(data.readInt(), data.readInt());
                reply.writeNoException();
                reply.writeInt(imcProvision ? 1 : 0);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                boolean imcProvision2 = setImcProvision(data.readInt(), data.readInt(), data.readInt());
                reply.writeNoException();
                reply.writeInt(imcProvision2 ? 1 : 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IDmService {
            public static IDmService sDefaultImpl;
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

            @Override // com.mediatek.dm.IDmService
            public int getDmSupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDmSupported();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.dm.IDmService
            public boolean getImcProvision(int phoneId, int feature) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(feature);
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getImcProvision(phoneId, feature);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.dm.IDmService
            public boolean setImcProvision(int phoneId, int feature, int pvs_en) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(feature);
                    _data.writeInt(pvs_en);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setImcProvision(phoneId, feature, pvs_en);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
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

        public static boolean setDefaultImpl(IDmService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IDmService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
