package android.hardware.alipay;

import android.hardware.alipay.IAlipayAuthenticatorCallback;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAlipayService extends IInterface {
    byte[] alipayFaceInvokeCommand(byte[] bArr) throws RemoteException;

    byte[] alipayInvokeCommand(byte[] bArr) throws RemoteException;

    void authenticate(IBinder iBinder, String str, int i, IAlipayAuthenticatorCallback iAlipayAuthenticatorCallback) throws RemoteException;

    int cancel(String str) throws RemoteException;

    void enroll(IBinder iBinder, String str, int i, IAlipayAuthenticatorCallback iAlipayAuthenticatorCallback) throws RemoteException;

    String getDeviceModel() throws RemoteException;

    int getFingerprintIconDiameter() throws RemoteException;

    int getFingerprintIconExternalCircleXY(String str) throws RemoteException;

    int getSupportBIOTypes() throws RemoteException;

    int getSupportIFAAVersion() throws RemoteException;

    void upgrade(String str) throws RemoteException;

    public static class Default implements IAlipayService {
        @Override // android.hardware.alipay.IAlipayService
        public byte[] alipayInvokeCommand(byte[] inbuf) throws RemoteException {
            return null;
        }

        @Override // android.hardware.alipay.IAlipayService
        public byte[] alipayFaceInvokeCommand(byte[] inbuf) throws RemoteException {
            return null;
        }

        @Override // android.hardware.alipay.IAlipayService
        public void authenticate(IBinder token, String reqId, int flags, IAlipayAuthenticatorCallback callback) throws RemoteException {
        }

        @Override // android.hardware.alipay.IAlipayService
        public void enroll(IBinder token, String reqId, int flags, IAlipayAuthenticatorCallback callback) throws RemoteException {
        }

        @Override // android.hardware.alipay.IAlipayService
        public int cancel(String reqId) throws RemoteException {
            return 0;
        }

        @Override // android.hardware.alipay.IAlipayService
        public void upgrade(String path) throws RemoteException {
        }

        @Override // android.hardware.alipay.IAlipayService
        public int getSupportBIOTypes() throws RemoteException {
            return 0;
        }

        @Override // android.hardware.alipay.IAlipayService
        public int getSupportIFAAVersion() throws RemoteException {
            return 0;
        }

        @Override // android.hardware.alipay.IAlipayService
        public String getDeviceModel() throws RemoteException {
            return null;
        }

        @Override // android.hardware.alipay.IAlipayService
        public int getFingerprintIconDiameter() throws RemoteException {
            return 0;
        }

        @Override // android.hardware.alipay.IAlipayService
        public int getFingerprintIconExternalCircleXY(String coord) throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IAlipayService {
        private static final String DESCRIPTOR = "android.hardware.alipay.IAlipayService";
        static final int TRANSACTION_alipayFaceInvokeCommand = 2;
        static final int TRANSACTION_alipayInvokeCommand = 1;
        static final int TRANSACTION_authenticate = 3;
        static final int TRANSACTION_cancel = 5;
        static final int TRANSACTION_enroll = 4;
        static final int TRANSACTION_getDeviceModel = 9;
        static final int TRANSACTION_getFingerprintIconDiameter = 10;
        static final int TRANSACTION_getFingerprintIconExternalCircleXY = 11;
        static final int TRANSACTION_getSupportBIOTypes = 7;
        static final int TRANSACTION_getSupportIFAAVersion = 8;
        static final int TRANSACTION_upgrade = 6;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAlipayService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAlipayService)) {
                return new Proxy(obj);
            }
            return (IAlipayService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "alipayInvokeCommand";
                case 2:
                    return "alipayFaceInvokeCommand";
                case 3:
                    return "authenticate";
                case 4:
                    return "enroll";
                case 5:
                    return "cancel";
                case 6:
                    return "upgrade";
                case 7:
                    return "getSupportBIOTypes";
                case 8:
                    return "getSupportIFAAVersion";
                case 9:
                    return "getDeviceModel";
                case 10:
                    return "getFingerprintIconDiameter";
                case 11:
                    return "getFingerprintIconExternalCircleXY";
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
                        byte[] _result = alipayInvokeCommand(data.createByteArray());
                        reply.writeNoException();
                        reply.writeByteArray(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result2 = alipayFaceInvokeCommand(data.createByteArray());
                        reply.writeNoException();
                        reply.writeByteArray(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        authenticate(data.readStrongBinder(), data.readString(), data.readInt(), IAlipayAuthenticatorCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        enroll(data.readStrongBinder(), data.readString(), data.readInt(), IAlipayAuthenticatorCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = cancel(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        upgrade(data.readString());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = getSupportBIOTypes();
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = getSupportIFAAVersion();
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        String _result6 = getDeviceModel();
                        reply.writeNoException();
                        reply.writeString(_result6);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = getFingerprintIconDiameter();
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = getFingerprintIconExternalCircleXY(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IAlipayService {
            public static IAlipayService sDefaultImpl;
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

            @Override // android.hardware.alipay.IAlipayService
            public byte[] alipayInvokeCommand(byte[] inbuf) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(inbuf);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().alipayInvokeCommand(inbuf);
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

            @Override // android.hardware.alipay.IAlipayService
            public byte[] alipayFaceInvokeCommand(byte[] inbuf) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(inbuf);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().alipayFaceInvokeCommand(inbuf);
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

            @Override // android.hardware.alipay.IAlipayService
            public void authenticate(IBinder token, String reqId, int flags, IAlipayAuthenticatorCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeString(reqId);
                    _data.writeInt(flags);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().authenticate(token, reqId, flags, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.alipay.IAlipayService
            public void enroll(IBinder token, String reqId, int flags, IAlipayAuthenticatorCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeString(reqId);
                    _data.writeInt(flags);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().enroll(token, reqId, flags, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.alipay.IAlipayService
            public int cancel(String reqId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(reqId);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().cancel(reqId);
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

            @Override // android.hardware.alipay.IAlipayService
            public void upgrade(String path) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(path);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().upgrade(path);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.alipay.IAlipayService
            public int getSupportBIOTypes() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSupportBIOTypes();
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

            @Override // android.hardware.alipay.IAlipayService
            public int getSupportIFAAVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSupportIFAAVersion();
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

            @Override // android.hardware.alipay.IAlipayService
            public String getDeviceModel() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDeviceModel();
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

            @Override // android.hardware.alipay.IAlipayService
            public int getFingerprintIconDiameter() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFingerprintIconDiameter();
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

            @Override // android.hardware.alipay.IAlipayService
            public int getFingerprintIconExternalCircleXY(String coord) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(coord);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFingerprintIconExternalCircleXY(coord);
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

        public static boolean setDefaultImpl(IAlipayService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IAlipayService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
