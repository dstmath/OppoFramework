package com.color.antivirus.tencent;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ITRPEng extends IInterface {
    int getVersion() throws RemoteException;

    int setAction(int i, int i2, int i3) throws RemoteException;

    void setBroadcastTarget(String str) throws RemoteException;

    void setDebug(boolean z) throws RemoteException;

    void setForegroundApp(String str) throws RemoteException;

    void setPackageToFilter(Bundle bundle) throws RemoteException;

    void updateConfig(Bundle bundle) throws RemoteException;

    public static class Default implements ITRPEng {
        @Override // com.color.antivirus.tencent.ITRPEng
        public int setAction(int actionID, int uid, int pid) throws RemoteException {
            return 0;
        }

        @Override // com.color.antivirus.tencent.ITRPEng
        public void setForegroundApp(String packageName) throws RemoteException {
        }

        @Override // com.color.antivirus.tencent.ITRPEng
        public void setPackageToFilter(Bundle inBundle) throws RemoteException {
        }

        @Override // com.color.antivirus.tencent.ITRPEng
        public void updateConfig(Bundle inBundle) throws RemoteException {
        }

        @Override // com.color.antivirus.tencent.ITRPEng
        public void setBroadcastTarget(String targetPackageName) throws RemoteException {
        }

        @Override // com.color.antivirus.tencent.ITRPEng
        public int getVersion() throws RemoteException {
            return 0;
        }

        @Override // com.color.antivirus.tencent.ITRPEng
        public void setDebug(boolean debug) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ITRPEng {
        private static final String DESCRIPTOR = "com.color.antivirus.tencent.ITRPEng";
        static final int TRANSACTION_getVersion = 6;
        static final int TRANSACTION_setAction = 1;
        static final int TRANSACTION_setBroadcastTarget = 5;
        static final int TRANSACTION_setDebug = 7;
        static final int TRANSACTION_setForegroundApp = 2;
        static final int TRANSACTION_setPackageToFilter = 3;
        static final int TRANSACTION_updateConfig = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITRPEng asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITRPEng)) {
                return new Proxy(obj);
            }
            return (ITRPEng) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "setAction";
                case 2:
                    return "setForegroundApp";
                case 3:
                    return "setPackageToFilter";
                case 4:
                    return "updateConfig";
                case 5:
                    return "setBroadcastTarget";
                case 6:
                    return "getVersion";
                case 7:
                    return "setDebug";
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
            Bundle _arg02;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = setAction(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        setForegroundApp(data.readString());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        setPackageToFilter(_arg0);
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        updateConfig(_arg02);
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        setBroadcastTarget(data.readString());
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = getVersion();
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        setDebug(data.readInt() != 0);
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

        /* access modifiers changed from: private */
        public static class Proxy implements ITRPEng {
            public static ITRPEng sDefaultImpl;
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

            @Override // com.color.antivirus.tencent.ITRPEng
            public int setAction(int actionID, int uid, int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(actionID);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setAction(actionID, uid, pid);
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

            @Override // com.color.antivirus.tencent.ITRPEng
            public void setForegroundApp(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setForegroundApp(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.color.antivirus.tencent.ITRPEng
            public void setPackageToFilter(Bundle inBundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (inBundle != null) {
                        _data.writeInt(1);
                        inBundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPackageToFilter(inBundle);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.color.antivirus.tencent.ITRPEng
            public void updateConfig(Bundle inBundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (inBundle != null) {
                        _data.writeInt(1);
                        inBundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateConfig(inBundle);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.color.antivirus.tencent.ITRPEng
            public void setBroadcastTarget(String targetPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(targetPackageName);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setBroadcastTarget(targetPackageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.color.antivirus.tencent.ITRPEng
            public int getVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getVersion();
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

            @Override // com.color.antivirus.tencent.ITRPEng
            public void setDebug(boolean debug) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(debug ? 1 : 0);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDebug(debug);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ITRPEng impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ITRPEng getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
