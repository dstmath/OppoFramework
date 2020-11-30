package com.nxp.nfc;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface INxpNfcAdapterExtras extends IInterface {
    boolean accessControlForCOSU(int i) throws RemoteException;

    Bundle getAtr(String str) throws RemoteException;

    boolean reset(String str) throws RemoteException;

    public static class Default implements INxpNfcAdapterExtras {
        @Override // com.nxp.nfc.INxpNfcAdapterExtras
        public boolean reset(String pkg) throws RemoteException {
            return false;
        }

        @Override // com.nxp.nfc.INxpNfcAdapterExtras
        public Bundle getAtr(String pkg) throws RemoteException {
            return null;
        }

        @Override // com.nxp.nfc.INxpNfcAdapterExtras
        public boolean accessControlForCOSU(int mode) throws RemoteException {
            return false;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INxpNfcAdapterExtras {
        private static final String DESCRIPTOR = "com.nxp.nfc.INxpNfcAdapterExtras";
        static final int TRANSACTION_accessControlForCOSU = 3;
        static final int TRANSACTION_getAtr = 2;
        static final int TRANSACTION_reset = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INxpNfcAdapterExtras asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INxpNfcAdapterExtras)) {
                return new Proxy(obj);
            }
            return (INxpNfcAdapterExtras) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                boolean reset = reset(data.readString());
                reply.writeNoException();
                reply.writeInt(reset ? 1 : 0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                Bundle _result = getAtr(data.readString());
                reply.writeNoException();
                if (_result != null) {
                    reply.writeInt(1);
                    _result.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                boolean accessControlForCOSU = accessControlForCOSU(data.readInt());
                reply.writeNoException();
                reply.writeInt(accessControlForCOSU ? 1 : 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements INxpNfcAdapterExtras {
            public static INxpNfcAdapterExtras sDefaultImpl;
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

            @Override // com.nxp.nfc.INxpNfcAdapterExtras
            public boolean reset(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().reset(pkg);
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

            @Override // com.nxp.nfc.INxpNfcAdapterExtras
            public Bundle getAtr(String pkg) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAtr(pkg);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
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

            @Override // com.nxp.nfc.INxpNfcAdapterExtras
            public boolean accessControlForCOSU(int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().accessControlForCOSU(mode);
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

        public static boolean setDefaultImpl(INxpNfcAdapterExtras impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INxpNfcAdapterExtras getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
