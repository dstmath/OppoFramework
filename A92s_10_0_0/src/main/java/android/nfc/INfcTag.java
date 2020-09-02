package android.nfc;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface INfcTag extends IInterface {
    boolean canMakeReadOnly(int i) throws RemoteException;

    int connect(int i, int i2) throws RemoteException;

    int formatNdef(int i, byte[] bArr) throws RemoteException;

    boolean getExtendedLengthApdusSupported() throws RemoteException;

    int getMaxTransceiveLength(int i) throws RemoteException;

    int[] getTechList(int i) throws RemoteException;

    int getTimeout(int i) throws RemoteException;

    boolean isNdef(int i) throws RemoteException;

    boolean isPresent(int i) throws RemoteException;

    boolean ndefIsWritable(int i) throws RemoteException;

    int ndefMakeReadOnly(int i) throws RemoteException;

    NdefMessage ndefRead(int i) throws RemoteException;

    int ndefWrite(int i, NdefMessage ndefMessage) throws RemoteException;

    int reconnect(int i) throws RemoteException;

    Tag rediscover(int i) throws RemoteException;

    void resetTimeouts() throws RemoteException;

    int setTimeout(int i, int i2) throws RemoteException;

    TransceiveResult transceive(int i, byte[] bArr, boolean z) throws RemoteException;

    public static class Default implements INfcTag {
        @Override // android.nfc.INfcTag
        public int connect(int nativeHandle, int technology) throws RemoteException {
            return 0;
        }

        @Override // android.nfc.INfcTag
        public int reconnect(int nativeHandle) throws RemoteException {
            return 0;
        }

        @Override // android.nfc.INfcTag
        public int[] getTechList(int nativeHandle) throws RemoteException {
            return null;
        }

        @Override // android.nfc.INfcTag
        public boolean isNdef(int nativeHandle) throws RemoteException {
            return false;
        }

        @Override // android.nfc.INfcTag
        public boolean isPresent(int nativeHandle) throws RemoteException {
            return false;
        }

        @Override // android.nfc.INfcTag
        public TransceiveResult transceive(int nativeHandle, byte[] data, boolean raw) throws RemoteException {
            return null;
        }

        @Override // android.nfc.INfcTag
        public NdefMessage ndefRead(int nativeHandle) throws RemoteException {
            return null;
        }

        @Override // android.nfc.INfcTag
        public int ndefWrite(int nativeHandle, NdefMessage msg) throws RemoteException {
            return 0;
        }

        @Override // android.nfc.INfcTag
        public int ndefMakeReadOnly(int nativeHandle) throws RemoteException {
            return 0;
        }

        @Override // android.nfc.INfcTag
        public boolean ndefIsWritable(int nativeHandle) throws RemoteException {
            return false;
        }

        @Override // android.nfc.INfcTag
        public int formatNdef(int nativeHandle, byte[] key) throws RemoteException {
            return 0;
        }

        @Override // android.nfc.INfcTag
        public Tag rediscover(int nativehandle) throws RemoteException {
            return null;
        }

        @Override // android.nfc.INfcTag
        public int setTimeout(int technology, int timeout) throws RemoteException {
            return 0;
        }

        @Override // android.nfc.INfcTag
        public int getTimeout(int technology) throws RemoteException {
            return 0;
        }

        @Override // android.nfc.INfcTag
        public void resetTimeouts() throws RemoteException {
        }

        @Override // android.nfc.INfcTag
        public boolean canMakeReadOnly(int ndefType) throws RemoteException {
            return false;
        }

        @Override // android.nfc.INfcTag
        public int getMaxTransceiveLength(int technology) throws RemoteException {
            return 0;
        }

        @Override // android.nfc.INfcTag
        public boolean getExtendedLengthApdusSupported() throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INfcTag {
        private static final String DESCRIPTOR = "android.nfc.INfcTag";
        static final int TRANSACTION_canMakeReadOnly = 16;
        static final int TRANSACTION_connect = 1;
        static final int TRANSACTION_formatNdef = 11;
        static final int TRANSACTION_getExtendedLengthApdusSupported = 18;
        static final int TRANSACTION_getMaxTransceiveLength = 17;
        static final int TRANSACTION_getTechList = 3;
        static final int TRANSACTION_getTimeout = 14;
        static final int TRANSACTION_isNdef = 4;
        static final int TRANSACTION_isPresent = 5;
        static final int TRANSACTION_ndefIsWritable = 10;
        static final int TRANSACTION_ndefMakeReadOnly = 9;
        static final int TRANSACTION_ndefRead = 7;
        static final int TRANSACTION_ndefWrite = 8;
        static final int TRANSACTION_reconnect = 2;
        static final int TRANSACTION_rediscover = 12;
        static final int TRANSACTION_resetTimeouts = 15;
        static final int TRANSACTION_setTimeout = 13;
        static final int TRANSACTION_transceive = 6;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INfcTag asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INfcTag)) {
                return new Proxy(obj);
            }
            return (INfcTag) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "connect";
                case 2:
                    return "reconnect";
                case 3:
                    return "getTechList";
                case 4:
                    return "isNdef";
                case 5:
                    return "isPresent";
                case 6:
                    return "transceive";
                case 7:
                    return "ndefRead";
                case 8:
                    return "ndefWrite";
                case 9:
                    return "ndefMakeReadOnly";
                case 10:
                    return "ndefIsWritable";
                case 11:
                    return "formatNdef";
                case 12:
                    return "rediscover";
                case 13:
                    return "setTimeout";
                case 14:
                    return "getTimeout";
                case 15:
                    return "resetTimeouts";
                case 16:
                    return "canMakeReadOnly";
                case 17:
                    return "getMaxTransceiveLength";
                case 18:
                    return "getExtendedLengthApdusSupported";
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
            NdefMessage _arg1;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = connect(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = reconnect(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result3 = getTechList(data.readInt());
                        reply.writeNoException();
                        reply.writeIntArray(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isNdef = isNdef(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isNdef ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isPresent = isPresent(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isPresent ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        TransceiveResult _result4 = transceive(data.readInt(), data.createByteArray(), data.readInt() != 0);
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        NdefMessage _result5 = ndefRead(data.readInt());
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = NdefMessage.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        int _result6 = ndefWrite(_arg0, _arg1);
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = ndefMakeReadOnly(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        boolean ndefIsWritable = ndefIsWritable(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(ndefIsWritable ? 1 : 0);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = formatNdef(data.readInt(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        Tag _result9 = rediscover(data.readInt());
                        reply.writeNoException();
                        if (_result9 != null) {
                            reply.writeInt(1);
                            _result9.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        int _result10 = setTimeout(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        int _result11 = getTimeout(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        resetTimeouts();
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        boolean canMakeReadOnly = canMakeReadOnly(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(canMakeReadOnly ? 1 : 0);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        int _result12 = getMaxTransceiveLength(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        boolean extendedLengthApdusSupported = getExtendedLengthApdusSupported();
                        reply.writeNoException();
                        reply.writeInt(extendedLengthApdusSupported ? 1 : 0);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements INfcTag {
            public static INfcTag sDefaultImpl;
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

            @Override // android.nfc.INfcTag
            public int connect(int nativeHandle, int technology) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(nativeHandle);
                    _data.writeInt(technology);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().connect(nativeHandle, technology);
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

            @Override // android.nfc.INfcTag
            public int reconnect(int nativeHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(nativeHandle);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().reconnect(nativeHandle);
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

            @Override // android.nfc.INfcTag
            public int[] getTechList(int nativeHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(nativeHandle);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTechList(nativeHandle);
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.nfc.INfcTag
            public boolean isNdef(int nativeHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(nativeHandle);
                    boolean _result = false;
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isNdef(nativeHandle);
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

            @Override // android.nfc.INfcTag
            public boolean isPresent(int nativeHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(nativeHandle);
                    boolean _result = false;
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isPresent(nativeHandle);
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

            @Override // android.nfc.INfcTag
            public TransceiveResult transceive(int nativeHandle, byte[] data, boolean raw) throws RemoteException {
                TransceiveResult _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(nativeHandle);
                    _data.writeByteArray(data);
                    _data.writeInt(raw ? 1 : 0);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().transceive(nativeHandle, data, raw);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = TransceiveResult.CREATOR.createFromParcel(_reply);
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

            @Override // android.nfc.INfcTag
            public NdefMessage ndefRead(int nativeHandle) throws RemoteException {
                NdefMessage _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(nativeHandle);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().ndefRead(nativeHandle);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = NdefMessage.CREATOR.createFromParcel(_reply);
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

            @Override // android.nfc.INfcTag
            public int ndefWrite(int nativeHandle, NdefMessage msg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(nativeHandle);
                    if (msg != null) {
                        _data.writeInt(1);
                        msg.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().ndefWrite(nativeHandle, msg);
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

            @Override // android.nfc.INfcTag
            public int ndefMakeReadOnly(int nativeHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(nativeHandle);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().ndefMakeReadOnly(nativeHandle);
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

            @Override // android.nfc.INfcTag
            public boolean ndefIsWritable(int nativeHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(nativeHandle);
                    boolean _result = false;
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().ndefIsWritable(nativeHandle);
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

            @Override // android.nfc.INfcTag
            public int formatNdef(int nativeHandle, byte[] key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(nativeHandle);
                    _data.writeByteArray(key);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().formatNdef(nativeHandle, key);
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

            @Override // android.nfc.INfcTag
            public Tag rediscover(int nativehandle) throws RemoteException {
                Tag _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(nativehandle);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().rediscover(nativehandle);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Tag.CREATOR.createFromParcel(_reply);
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

            @Override // android.nfc.INfcTag
            public int setTimeout(int technology, int timeout) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(technology);
                    _data.writeInt(timeout);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setTimeout(technology, timeout);
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

            @Override // android.nfc.INfcTag
            public int getTimeout(int technology) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(technology);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTimeout(technology);
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

            @Override // android.nfc.INfcTag
            public void resetTimeouts() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(15, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resetTimeouts();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.nfc.INfcTag
            public boolean canMakeReadOnly(int ndefType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(ndefType);
                    boolean _result = false;
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().canMakeReadOnly(ndefType);
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

            @Override // android.nfc.INfcTag
            public int getMaxTransceiveLength(int technology) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(technology);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMaxTransceiveLength(technology);
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

            @Override // android.nfc.INfcTag
            public boolean getExtendedLengthApdusSupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getExtendedLengthApdusSupported();
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

        public static boolean setDefaultImpl(INfcTag impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INfcTag getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
