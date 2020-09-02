package com.android.internal.app;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ILMServiceManager extends IInterface {
    boolean enableBoost(int i, int i2, int i3, int i4) throws RemoteException;

    boolean enableMobileBoost() throws RemoteException;

    String getLuckyMoneyInfo(int i) throws RemoteException;

    Bundle getModeData(int i, int i2, int i3) throws RemoteException;

    Bundle getModeEnableInfo(int i, int i2) throws RemoteException;

    Bundle getSwitchInfo() throws RemoteException;

    boolean isInitialized() throws RemoteException;

    void writeDCS(Bundle bundle) throws RemoteException;

    public static class Default implements ILMServiceManager {
        @Override // com.android.internal.app.ILMServiceManager
        public Bundle getModeEnableInfo(int type, int versionCode) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.app.ILMServiceManager
        public String getLuckyMoneyInfo(int type) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.app.ILMServiceManager
        public boolean enableBoost(int pid, int uid, int timeout, int code) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.app.ILMServiceManager
        public Bundle getModeData(int type, int versionCode, int defaultValue) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.app.ILMServiceManager
        public Bundle getSwitchInfo() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.app.ILMServiceManager
        public boolean enableMobileBoost() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.app.ILMServiceManager
        public void writeDCS(Bundle data) throws RemoteException {
        }

        @Override // com.android.internal.app.ILMServiceManager
        public boolean isInitialized() throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ILMServiceManager {
        private static final String DESCRIPTOR = "com.android.internal.app.ILMServiceManager";
        static final int TRANSACTION_enableBoost = 3;
        static final int TRANSACTION_enableMobileBoost = 6;
        static final int TRANSACTION_getLuckyMoneyInfo = 2;
        static final int TRANSACTION_getModeData = 4;
        static final int TRANSACTION_getModeEnableInfo = 1;
        static final int TRANSACTION_getSwitchInfo = 5;
        static final int TRANSACTION_isInitialized = 8;
        static final int TRANSACTION_writeDCS = 7;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ILMServiceManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ILMServiceManager)) {
                return new Proxy(obj);
            }
            return (ILMServiceManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "getModeEnableInfo";
                case 2:
                    return "getLuckyMoneyInfo";
                case 3:
                    return "enableBoost";
                case 4:
                    return "getModeData";
                case 5:
                    return "getSwitchInfo";
                case 6:
                    return "enableMobileBoost";
                case 7:
                    return "writeDCS";
                case 8:
                    return "isInitialized";
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
            Bundle _arg0;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result = getModeEnableInfo(data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        String _result2 = getLuckyMoneyInfo(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean enableBoost = enableBoost(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(enableBoost ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result3 = getModeData(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result4 = getSwitchInfo();
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean enableMobileBoost = enableMobileBoost();
                        reply.writeNoException();
                        reply.writeInt(enableMobileBoost ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        writeDCS(_arg0);
                        reply.writeNoException();
                        if (_arg0 != null) {
                            reply.writeInt(1);
                            _arg0.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isInitialized = isInitialized();
                        reply.writeNoException();
                        reply.writeInt(isInitialized ? 1 : 0);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements ILMServiceManager {
            public static ILMServiceManager sDefaultImpl;
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

            @Override // com.android.internal.app.ILMServiceManager
            public Bundle getModeEnableInfo(int type, int versionCode) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeInt(versionCode);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getModeEnableInfo(type, versionCode);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bundle.CREATOR.createFromParcel(_reply);
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

            @Override // com.android.internal.app.ILMServiceManager
            public String getLuckyMoneyInfo(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLuckyMoneyInfo(type);
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

            @Override // com.android.internal.app.ILMServiceManager
            public boolean enableBoost(int pid, int uid, int timeout, int code) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeInt(timeout);
                    _data.writeInt(code);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().enableBoost(pid, uid, timeout, code);
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

            @Override // com.android.internal.app.ILMServiceManager
            public Bundle getModeData(int type, int versionCode, int defaultValue) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeInt(versionCode);
                    _data.writeInt(defaultValue);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getModeData(type, versionCode, defaultValue);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bundle.CREATOR.createFromParcel(_reply);
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

            @Override // com.android.internal.app.ILMServiceManager
            public Bundle getSwitchInfo() throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSwitchInfo();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bundle.CREATOR.createFromParcel(_reply);
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

            @Override // com.android.internal.app.ILMServiceManager
            public boolean enableMobileBoost() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().enableMobileBoost();
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

            @Override // com.android.internal.app.ILMServiceManager
            public void writeDCS(Bundle data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        if (_reply.readInt() != 0) {
                            data.readFromParcel(_reply);
                        }
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().writeDCS(data);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.ILMServiceManager
            public boolean isInitialized() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isInitialized();
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

        public static boolean setDefaultImpl(ILMServiceManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ILMServiceManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
