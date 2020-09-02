package android.telephony.ims.aidl;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.ims.aidl.IImsConfigCallback;

public interface IImsConfig extends IInterface {
    void addImsConfigCallback(IImsConfigCallback iImsConfigCallback) throws RemoteException;

    int getConfigInt(int i) throws RemoteException;

    String getConfigString(int i) throws RemoteException;

    void removeImsConfigCallback(IImsConfigCallback iImsConfigCallback) throws RemoteException;

    int setConfigInt(int i, int i2) throws RemoteException;

    int setConfigString(int i, String str) throws RemoteException;

    public static class Default implements IImsConfig {
        @Override // android.telephony.ims.aidl.IImsConfig
        public void addImsConfigCallback(IImsConfigCallback c) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IImsConfig
        public void removeImsConfigCallback(IImsConfigCallback c) throws RemoteException {
        }

        @Override // android.telephony.ims.aidl.IImsConfig
        public int getConfigInt(int item) throws RemoteException {
            return 0;
        }

        @Override // android.telephony.ims.aidl.IImsConfig
        public String getConfigString(int item) throws RemoteException {
            return null;
        }

        @Override // android.telephony.ims.aidl.IImsConfig
        public int setConfigInt(int item, int value) throws RemoteException {
            return 0;
        }

        @Override // android.telephony.ims.aidl.IImsConfig
        public int setConfigString(int item, String value) throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IImsConfig {
        private static final String DESCRIPTOR = "android.telephony.ims.aidl.IImsConfig";
        static final int TRANSACTION_addImsConfigCallback = 1;
        static final int TRANSACTION_getConfigInt = 3;
        static final int TRANSACTION_getConfigString = 4;
        static final int TRANSACTION_removeImsConfigCallback = 2;
        static final int TRANSACTION_setConfigInt = 5;
        static final int TRANSACTION_setConfigString = 6;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IImsConfig asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IImsConfig)) {
                return new Proxy(obj);
            }
            return (IImsConfig) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "addImsConfigCallback";
                case 2:
                    return "removeImsConfigCallback";
                case 3:
                    return "getConfigInt";
                case 4:
                    return "getConfigString";
                case 5:
                    return "setConfigInt";
                case 6:
                    return "setConfigString";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        addImsConfigCallback(IImsConfigCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        removeImsConfigCallback(IImsConfigCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = getConfigInt(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        String _result2 = getConfigString(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result2);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = setConfigInt(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = setConfigString(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IImsConfig {
            public static IImsConfig sDefaultImpl;
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

            @Override // android.telephony.ims.aidl.IImsConfig
            public void addImsConfigCallback(IImsConfigCallback c) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(c != null ? c.asBinder() : null);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addImsConfigCallback(c);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IImsConfig
            public void removeImsConfigCallback(IImsConfigCallback c) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(c != null ? c.asBinder() : null);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeImsConfigCallback(c);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IImsConfig
            public int getConfigInt(int item) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(item);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getConfigInt(item);
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

            @Override // android.telephony.ims.aidl.IImsConfig
            public String getConfigString(int item) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(item);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getConfigString(item);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.telephony.ims.aidl.IImsConfig
            public int setConfigInt(int item, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(item);
                    _data.writeInt(value);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setConfigInt(item, value);
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

            @Override // android.telephony.ims.aidl.IImsConfig
            public int setConfigString(int item, String value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(item);
                    _data.writeString(value);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setConfigString(item, value);
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
        }

        public static boolean setDefaultImpl(IImsConfig impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IImsConfig getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
