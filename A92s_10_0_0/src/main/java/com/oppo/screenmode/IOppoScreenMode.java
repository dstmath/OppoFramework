package com.oppo.screenmode;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.oppo.screenmode.IOppoScreenModeCallback;

public interface IOppoScreenMode extends IInterface {
    void addCallback(IOppoScreenModeCallback iOppoScreenModeCallback) throws RemoteException;

    void enterDCAndLowBrightnessMode(boolean z) throws RemoteException;

    void enterPSMode(boolean z) throws RemoteException;

    void enterPSModeOnRate(boolean z, int i) throws RemoteException;

    String getDisableOverrideViewList(String str) throws RemoteException;

    boolean isDisplayCompat(String str, int i) throws RemoteException;

    void remove(IOppoScreenModeCallback iOppoScreenModeCallback) throws RemoteException;

    boolean requestGameRefreshRate(String str, int i) throws RemoteException;

    boolean requestRefreshRate(boolean z, int i) throws RemoteException;

    boolean requestRefreshRateWithToken(boolean z, int i, IBinder iBinder) throws RemoteException;

    void setClientRefreshRate(IBinder iBinder, int i) throws RemoteException;

    boolean setHighTemperatureStatus(int i, int i2) throws RemoteException;

    boolean supportDisplayCompat(String str, int i) throws RemoteException;

    public static class Default implements IOppoScreenMode {
        @Override // com.oppo.screenmode.IOppoScreenMode
        public void addCallback(IOppoScreenModeCallback callback) throws RemoteException {
        }

        @Override // com.oppo.screenmode.IOppoScreenMode
        public void remove(IOppoScreenModeCallback callback) throws RemoteException {
        }

        @Override // com.oppo.screenmode.IOppoScreenMode
        public void setClientRefreshRate(IBinder token, int rate) throws RemoteException {
        }

        @Override // com.oppo.screenmode.IOppoScreenMode
        public boolean requestRefreshRate(boolean open, int rate) throws RemoteException {
            return false;
        }

        @Override // com.oppo.screenmode.IOppoScreenMode
        public boolean supportDisplayCompat(String pkg, int uid) throws RemoteException {
            return false;
        }

        @Override // com.oppo.screenmode.IOppoScreenMode
        public boolean setHighTemperatureStatus(int status, int rate) throws RemoteException {
            return false;
        }

        @Override // com.oppo.screenmode.IOppoScreenMode
        public void enterDCAndLowBrightnessMode(boolean enter) throws RemoteException {
        }

        @Override // com.oppo.screenmode.IOppoScreenMode
        public boolean isDisplayCompat(String packageName, int uid) throws RemoteException {
            return false;
        }

        @Override // com.oppo.screenmode.IOppoScreenMode
        public void enterPSMode(boolean enter) throws RemoteException {
        }

        @Override // com.oppo.screenmode.IOppoScreenMode
        public void enterPSModeOnRate(boolean enter, int rate) throws RemoteException {
        }

        @Override // com.oppo.screenmode.IOppoScreenMode
        public boolean requestGameRefreshRate(String packageName, int rate) throws RemoteException {
            return false;
        }

        @Override // com.oppo.screenmode.IOppoScreenMode
        public boolean requestRefreshRateWithToken(boolean open, int rate, IBinder token) throws RemoteException {
            return false;
        }

        @Override // com.oppo.screenmode.IOppoScreenMode
        public String getDisableOverrideViewList(String key) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOppoScreenMode {
        private static final String DESCRIPTOR = "com.oppo.screenmode.IOppoScreenMode";
        static final int TRANSACTION_addCallback = 1;
        static final int TRANSACTION_enterDCAndLowBrightnessMode = 7;
        static final int TRANSACTION_enterPSMode = 9;
        static final int TRANSACTION_enterPSModeOnRate = 10;
        static final int TRANSACTION_getDisableOverrideViewList = 13;
        static final int TRANSACTION_isDisplayCompat = 8;
        static final int TRANSACTION_remove = 2;
        static final int TRANSACTION_requestGameRefreshRate = 11;
        static final int TRANSACTION_requestRefreshRate = 4;
        static final int TRANSACTION_requestRefreshRateWithToken = 12;
        static final int TRANSACTION_setClientRefreshRate = 3;
        static final int TRANSACTION_setHighTemperatureStatus = 6;
        static final int TRANSACTION_supportDisplayCompat = 5;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOppoScreenMode asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOppoScreenMode)) {
                return new Proxy(obj);
            }
            return (IOppoScreenMode) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "addCallback";
                case 2:
                    return "remove";
                case 3:
                    return "setClientRefreshRate";
                case 4:
                    return "requestRefreshRate";
                case 5:
                    return "supportDisplayCompat";
                case 6:
                    return "setHighTemperatureStatus";
                case 7:
                    return "enterDCAndLowBrightnessMode";
                case 8:
                    return "isDisplayCompat";
                case 9:
                    return "enterPSMode";
                case 10:
                    return "enterPSModeOnRate";
                case 11:
                    return "requestGameRefreshRate";
                case 12:
                    return "requestRefreshRateWithToken";
                case 13:
                    return "getDisableOverrideViewList";
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
                boolean _arg0 = false;
                boolean _arg02 = false;
                boolean _arg03 = false;
                boolean _arg04 = false;
                boolean _arg05 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        addCallback(IOppoScreenModeCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        remove(IOppoScreenModeCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        setClientRefreshRate(data.readStrongBinder(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        boolean requestRefreshRate = requestRefreshRate(_arg0, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(requestRefreshRate ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean supportDisplayCompat = supportDisplayCompat(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(supportDisplayCompat ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean highTemperatureStatus = setHighTemperatureStatus(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(highTemperatureStatus ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = true;
                        }
                        enterDCAndLowBrightnessMode(_arg05);
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isDisplayCompat = isDisplayCompat(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isDisplayCompat ? 1 : 0);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = true;
                        }
                        enterPSMode(_arg04);
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = true;
                        }
                        enterPSModeOnRate(_arg03, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        boolean requestGameRefreshRate = requestGameRefreshRate(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(requestGameRefreshRate ? 1 : 0);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        boolean requestRefreshRateWithToken = requestRefreshRateWithToken(_arg02, data.readInt(), data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeInt(requestRefreshRateWithToken ? 1 : 0);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        String _result = getDisableOverrideViewList(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IOppoScreenMode {
            public static IOppoScreenMode sDefaultImpl;
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

            @Override // com.oppo.screenmode.IOppoScreenMode
            public void addCallback(IOppoScreenModeCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.screenmode.IOppoScreenMode
            public void remove(IOppoScreenModeCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().remove(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.screenmode.IOppoScreenMode
            public void setClientRefreshRate(IBinder token, int rate) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(rate);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setClientRefreshRate(token, rate);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.screenmode.IOppoScreenMode
            public boolean requestRefreshRate(boolean open, int rate) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(open ? 1 : 0);
                    _data.writeInt(rate);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().requestRefreshRate(open, rate);
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

            @Override // com.oppo.screenmode.IOppoScreenMode
            public boolean supportDisplayCompat(String pkg, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    boolean _result = false;
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().supportDisplayCompat(pkg, uid);
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

            @Override // com.oppo.screenmode.IOppoScreenMode
            public boolean setHighTemperatureStatus(int status, int rate) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    _data.writeInt(rate);
                    boolean _result = false;
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setHighTemperatureStatus(status, rate);
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

            @Override // com.oppo.screenmode.IOppoScreenMode
            public void enterDCAndLowBrightnessMode(boolean enter) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enter ? 1 : 0);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().enterDCAndLowBrightnessMode(enter);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.screenmode.IOppoScreenMode
            public boolean isDisplayCompat(String packageName, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(uid);
                    boolean _result = false;
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isDisplayCompat(packageName, uid);
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

            @Override // com.oppo.screenmode.IOppoScreenMode
            public void enterPSMode(boolean enter) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enter ? 1 : 0);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().enterPSMode(enter);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.screenmode.IOppoScreenMode
            public void enterPSModeOnRate(boolean enter, int rate) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enter ? 1 : 0);
                    _data.writeInt(rate);
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().enterPSModeOnRate(enter, rate);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.screenmode.IOppoScreenMode
            public boolean requestGameRefreshRate(String packageName, int rate) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(rate);
                    boolean _result = false;
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().requestGameRefreshRate(packageName, rate);
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

            @Override // com.oppo.screenmode.IOppoScreenMode
            public boolean requestRefreshRateWithToken(boolean open, int rate, IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(open ? 1 : 0);
                    _data.writeInt(rate);
                    _data.writeStrongBinder(token);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().requestRefreshRateWithToken(open, rate, token);
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

            @Override // com.oppo.screenmode.IOppoScreenMode
            public String getDisableOverrideViewList(String key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDisableOverrideViewList(key);
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
        }

        public static boolean setDefaultImpl(IOppoScreenMode impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOppoScreenMode getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
