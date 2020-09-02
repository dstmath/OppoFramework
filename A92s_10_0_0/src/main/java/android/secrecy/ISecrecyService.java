package android.secrecy;

import android.content.pm.ActivityInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.secrecy.ISecrecyServiceReceiver;

public interface ISecrecyService extends IInterface {
    byte[] generateCipherFromKey(int i) throws RemoteException;

    String generateTokenFromKey() throws RemoteException;

    boolean getSecrecyState(int i) throws RemoteException;

    boolean isInEncryptedAppList(ActivityInfo activityInfo, String str, int i, int i2) throws RemoteException;

    boolean isKeyImported() throws RemoteException;

    boolean isSecrecySupport() throws RemoteException;

    boolean registerSecrecyServiceReceiver(ISecrecyServiceReceiver iSecrecyServiceReceiver) throws RemoteException;

    public static class Default implements ISecrecyService {
        @Override // android.secrecy.ISecrecyService
        public boolean getSecrecyState(int type) throws RemoteException {
            return false;
        }

        @Override // android.secrecy.ISecrecyService
        public String generateTokenFromKey() throws RemoteException {
            return null;
        }

        @Override // android.secrecy.ISecrecyService
        public boolean isKeyImported() throws RemoteException {
            return false;
        }

        @Override // android.secrecy.ISecrecyService
        public byte[] generateCipherFromKey(int cipherLength) throws RemoteException {
            return null;
        }

        @Override // android.secrecy.ISecrecyService
        public boolean registerSecrecyServiceReceiver(ISecrecyServiceReceiver receiver) throws RemoteException {
            return false;
        }

        @Override // android.secrecy.ISecrecyService
        public boolean isSecrecySupport() throws RemoteException {
            return false;
        }

        @Override // android.secrecy.ISecrecyService
        public boolean isInEncryptedAppList(ActivityInfo info, String callingPackage, int callingUid, int callingPid) throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ISecrecyService {
        private static final String DESCRIPTOR = "android.secrecy.ISecrecyService";
        static final int TRANSACTION_generateCipherFromKey = 4;
        static final int TRANSACTION_generateTokenFromKey = 2;
        static final int TRANSACTION_getSecrecyState = 1;
        static final int TRANSACTION_isInEncryptedAppList = 7;
        static final int TRANSACTION_isKeyImported = 3;
        static final int TRANSACTION_isSecrecySupport = 6;
        static final int TRANSACTION_registerSecrecyServiceReceiver = 5;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISecrecyService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISecrecyService)) {
                return new Proxy(obj);
            }
            return (ISecrecyService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "getSecrecyState";
                case 2:
                    return "generateTokenFromKey";
                case 3:
                    return "isKeyImported";
                case 4:
                    return "generateCipherFromKey";
                case 5:
                    return "registerSecrecyServiceReceiver";
                case 6:
                    return "isSecrecySupport";
                case 7:
                    return "isInEncryptedAppList";
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
            ActivityInfo _arg0;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        boolean secrecyState = getSecrecyState(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(secrecyState ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        String _result = generateTokenFromKey();
                        reply.writeNoException();
                        reply.writeString(_result);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isKeyImported = isKeyImported();
                        reply.writeNoException();
                        reply.writeInt(isKeyImported ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result2 = generateCipherFromKey(data.readInt());
                        reply.writeNoException();
                        reply.writeByteArray(_result2);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerSecrecyServiceReceiver = registerSecrecyServiceReceiver(ISecrecyServiceReceiver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerSecrecyServiceReceiver ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isSecrecySupport = isSecrecySupport();
                        reply.writeNoException();
                        reply.writeInt(isSecrecySupport ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ActivityInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        boolean isInEncryptedAppList = isInEncryptedAppList(_arg0, data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isInEncryptedAppList ? 1 : 0);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements ISecrecyService {
            public static ISecrecyService sDefaultImpl;
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

            @Override // android.secrecy.ISecrecyService
            public boolean getSecrecyState(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSecrecyState(type);
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

            @Override // android.secrecy.ISecrecyService
            public String generateTokenFromKey() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().generateTokenFromKey();
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

            @Override // android.secrecy.ISecrecyService
            public boolean isKeyImported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isKeyImported();
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

            @Override // android.secrecy.ISecrecyService
            public byte[] generateCipherFromKey(int cipherLength) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cipherLength);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().generateCipherFromKey(cipherLength);
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.secrecy.ISecrecyService
            public boolean registerSecrecyServiceReceiver(ISecrecyServiceReceiver receiver) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(receiver != null ? receiver.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerSecrecyServiceReceiver(receiver);
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

            @Override // android.secrecy.ISecrecyService
            public boolean isSecrecySupport() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSecrecySupport();
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

            @Override // android.secrecy.ISecrecyService
            public boolean isInEncryptedAppList(ActivityInfo info, String callingPackage, int callingUid, int callingPid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    _data.writeInt(callingUid);
                    _data.writeInt(callingPid);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isInEncryptedAppList(info, callingPackage, callingUid, callingPid);
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
        }

        public static boolean setDefaultImpl(ISecrecyService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ISecrecyService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
