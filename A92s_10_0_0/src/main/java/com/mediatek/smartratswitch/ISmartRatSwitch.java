package com.mediatek.smartratswitch;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISmartRatSwitch extends IInterface {
    void forceDisable5G() throws RemoteException;

    void forceEnable5G() throws RemoteException;

    int getLastOperation() throws RemoteException;

    void resetKeepRatDuration() throws RemoteException;

    void resetNextTput(String str) throws RemoteException;

    void setCustAppProperty(String str, int i) throws RemoteException;

    void setKeepRatDuration(int i) throws RemoteException;

    void setNextTput(String str, int i, int i2) throws RemoteException;

    public static class Default implements ISmartRatSwitch {
        @Override // com.mediatek.smartratswitch.ISmartRatSwitch
        public void setCustAppProperty(String package_name, int param) throws RemoteException {
        }

        @Override // com.mediatek.smartratswitch.ISmartRatSwitch
        public int getLastOperation() throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.smartratswitch.ISmartRatSwitch
        public void forceEnable5G() throws RemoteException {
        }

        @Override // com.mediatek.smartratswitch.ISmartRatSwitch
        public void forceDisable5G() throws RemoteException {
        }

        @Override // com.mediatek.smartratswitch.ISmartRatSwitch
        public void setKeepRatDuration(int KeepRatDuration) throws RemoteException {
        }

        @Override // com.mediatek.smartratswitch.ISmartRatSwitch
        public void resetKeepRatDuration() throws RemoteException {
        }

        @Override // com.mediatek.smartratswitch.ISmartRatSwitch
        public void setNextTput(String package_name, int Tput_tx, int Tput_rx) throws RemoteException {
        }

        @Override // com.mediatek.smartratswitch.ISmartRatSwitch
        public void resetNextTput(String package_name) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ISmartRatSwitch {
        private static final String DESCRIPTOR = "com.mediatek.smartratswitch.ISmartRatSwitch";
        static final int TRANSACTION_forceDisable5G = 4;
        static final int TRANSACTION_forceEnable5G = 3;
        static final int TRANSACTION_getLastOperation = 2;
        static final int TRANSACTION_resetKeepRatDuration = 6;
        static final int TRANSACTION_resetNextTput = 8;
        static final int TRANSACTION_setCustAppProperty = 1;
        static final int TRANSACTION_setKeepRatDuration = 5;
        static final int TRANSACTION_setNextTput = 7;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISmartRatSwitch asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISmartRatSwitch)) {
                return new Proxy(obj);
            }
            return (ISmartRatSwitch) iin;
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
                        setCustAppProperty(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = getLastOperation();
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        forceEnable5G();
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        forceDisable5G();
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        setKeepRatDuration(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        resetKeepRatDuration();
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        setNextTput(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        resetNextTput(data.readString());
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements ISmartRatSwitch {
            public static ISmartRatSwitch sDefaultImpl;
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

            @Override // com.mediatek.smartratswitch.ISmartRatSwitch
            public void setCustAppProperty(String package_name, int param) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(package_name);
                    _data.writeInt(param);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setCustAppProperty(package_name, param);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.smartratswitch.ISmartRatSwitch
            public int getLastOperation() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLastOperation();
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

            @Override // com.mediatek.smartratswitch.ISmartRatSwitch
            public void forceEnable5G() throws RemoteException {
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
                    Stub.getDefaultImpl().forceEnable5G();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.smartratswitch.ISmartRatSwitch
            public void forceDisable5G() throws RemoteException {
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
                    Stub.getDefaultImpl().forceDisable5G();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.smartratswitch.ISmartRatSwitch
            public void setKeepRatDuration(int KeepRatDuration) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(KeepRatDuration);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setKeepRatDuration(KeepRatDuration);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.smartratswitch.ISmartRatSwitch
            public void resetKeepRatDuration() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resetKeepRatDuration();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.smartratswitch.ISmartRatSwitch
            public void setNextTput(String package_name, int Tput_tx, int Tput_rx) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(package_name);
                    _data.writeInt(Tput_tx);
                    _data.writeInt(Tput_rx);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setNextTput(package_name, Tput_tx, Tput_rx);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.smartratswitch.ISmartRatSwitch
            public void resetNextTput(String package_name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(package_name);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resetNextTput(package_name);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ISmartRatSwitch impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ISmartRatSwitch getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
