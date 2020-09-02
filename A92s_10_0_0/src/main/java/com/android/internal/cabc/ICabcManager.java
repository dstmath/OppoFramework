package com.android.internal.cabc;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ICabcManager extends IInterface {
    void closeCabc() throws RemoteException;

    int getMode() throws RemoteException;

    void openCabc() throws RemoteException;

    void setMode(int i) throws RemoteException;

    public static class Default implements ICabcManager {
        @Override // com.android.internal.cabc.ICabcManager
        public void setMode(int mode) throws RemoteException {
        }

        @Override // com.android.internal.cabc.ICabcManager
        public int getMode() throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.cabc.ICabcManager
        public void closeCabc() throws RemoteException {
        }

        @Override // com.android.internal.cabc.ICabcManager
        public void openCabc() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ICabcManager {
        private static final String DESCRIPTOR = "com.android.internal.cabc.ICabcManager";
        static final int TRANSACTION_closeCabc = 3;
        static final int TRANSACTION_getMode = 2;
        static final int TRANSACTION_openCabc = 4;
        static final int TRANSACTION_setMode = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ICabcManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICabcManager)) {
                return new Proxy(obj);
            }
            return (ICabcManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "setMode";
            }
            if (transactionCode == 2) {
                return "getMode";
            }
            if (transactionCode == 3) {
                return "closeCabc";
            }
            if (transactionCode != 4) {
                return null;
            }
            return "openCabc";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                setMode(data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int _result = getMode();
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                closeCabc();
                reply.writeNoException();
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                openCabc();
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements ICabcManager {
            public static ICabcManager sDefaultImpl;
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

            @Override // com.android.internal.cabc.ICabcManager
            public void setMode(int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setMode(mode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.cabc.ICabcManager
            public int getMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMode();
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

            @Override // com.android.internal.cabc.ICabcManager
            public void closeCabc() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().closeCabc();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.cabc.ICabcManager
            public void openCabc() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().openCabc();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ICabcManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ICabcManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
