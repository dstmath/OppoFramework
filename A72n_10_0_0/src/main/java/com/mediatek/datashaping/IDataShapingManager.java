package com.mediatek.datashaping;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IDataShapingManager extends IInterface {
    void disableDataShaping() throws RemoteException;

    void enableDataShaping() throws RemoteException;

    boolean isDataShapingWhitelistApp(String str) throws RemoteException;

    boolean openLteDataUpLinkGate(boolean z) throws RemoteException;

    void setDeviceIdleMode(boolean z) throws RemoteException;

    public static class Default implements IDataShapingManager {
        @Override // com.mediatek.datashaping.IDataShapingManager
        public void enableDataShaping() throws RemoteException {
        }

        @Override // com.mediatek.datashaping.IDataShapingManager
        public void disableDataShaping() throws RemoteException {
        }

        @Override // com.mediatek.datashaping.IDataShapingManager
        public boolean openLteDataUpLinkGate(boolean isForce) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.datashaping.IDataShapingManager
        public void setDeviceIdleMode(boolean enabled) throws RemoteException {
        }

        @Override // com.mediatek.datashaping.IDataShapingManager
        public boolean isDataShapingWhitelistApp(String packageName) throws RemoteException {
            return false;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IDataShapingManager {
        private static final String DESCRIPTOR = "com.mediatek.datashaping.IDataShapingManager";
        static final int TRANSACTION_disableDataShaping = 2;
        static final int TRANSACTION_enableDataShaping = 1;
        static final int TRANSACTION_isDataShapingWhitelistApp = 5;
        static final int TRANSACTION_openLteDataUpLinkGate = 3;
        static final int TRANSACTION_setDeviceIdleMode = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDataShapingManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDataShapingManager)) {
                return new Proxy(obj);
            }
            return (IDataShapingManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                enableDataShaping();
                reply.writeNoException();
                return true;
            } else if (code != 2) {
                boolean _arg0 = false;
                if (code == 3) {
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = true;
                    }
                    boolean openLteDataUpLinkGate = openLteDataUpLinkGate(_arg0);
                    reply.writeNoException();
                    reply.writeInt(openLteDataUpLinkGate ? 1 : 0);
                    return true;
                } else if (code == 4) {
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = true;
                    }
                    setDeviceIdleMode(_arg0);
                    reply.writeNoException();
                    return true;
                } else if (code == 5) {
                    data.enforceInterface(DESCRIPTOR);
                    boolean isDataShapingWhitelistApp = isDataShapingWhitelistApp(data.readString());
                    reply.writeNoException();
                    reply.writeInt(isDataShapingWhitelistApp ? 1 : 0);
                    return true;
                } else if (code != 1598968902) {
                    return super.onTransact(code, data, reply, flags);
                } else {
                    reply.writeString(DESCRIPTOR);
                    return true;
                }
            } else {
                data.enforceInterface(DESCRIPTOR);
                disableDataShaping();
                reply.writeNoException();
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IDataShapingManager {
            public static IDataShapingManager sDefaultImpl;
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

            @Override // com.mediatek.datashaping.IDataShapingManager
            public void enableDataShaping() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().enableDataShaping();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.datashaping.IDataShapingManager
            public void disableDataShaping() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().disableDataShaping();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.datashaping.IDataShapingManager
            public boolean openLteDataUpLinkGate(boolean isForce) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(isForce ? 1 : 0);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().openLteDataUpLinkGate(isForce);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.datashaping.IDataShapingManager
            public void setDeviceIdleMode(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled ? 1 : 0);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDeviceIdleMode(enabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.datashaping.IDataShapingManager
            public boolean isDataShapingWhitelistApp(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isDataShapingWhitelistApp(packageName);
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

        public static boolean setDefaultImpl(IDataShapingManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IDataShapingManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
