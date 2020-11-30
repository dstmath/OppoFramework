package com.color.antivirus.qihoo;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.color.antivirus.qihoo.ITransmitPointCallback;
import com.color.antivirus.qihoo.IUidSetChangeCallbackInterface;

public interface ITransmitPointService extends IInterface {
    void notifyAddUid(int i) throws RemoteException;

    void notifyDeleteUid(int i) throws RemoteException;

    void notifyProcessDied(int i, int i2) throws RemoteException;

    void notifyUidSetChange(int[] iArr) throws RemoteException;

    void pushId(int i, int i2, int i3) throws RemoteException;

    void pushInstallApp(String[] strArr) throws RemoteException;

    void registerCallBack(ITransmitPointCallback iTransmitPointCallback) throws RemoteException;

    void registerUidChangeCallback(IUidSetChangeCallbackInterface iUidSetChangeCallbackInterface) throws RemoteException;

    public static class Default implements ITransmitPointService {
        @Override // com.color.antivirus.qihoo.ITransmitPointService
        public void pushId(int uid, int pid, int bid) throws RemoteException {
        }

        @Override // com.color.antivirus.qihoo.ITransmitPointService
        public void notifyProcessDied(int uid, int pid) throws RemoteException {
        }

        @Override // com.color.antivirus.qihoo.ITransmitPointService
        public void pushInstallApp(String[] apkInfoArray) throws RemoteException {
        }

        @Override // com.color.antivirus.qihoo.ITransmitPointService
        public void registerCallBack(ITransmitPointCallback pushIdCallBack) throws RemoteException {
        }

        @Override // com.color.antivirus.qihoo.ITransmitPointService
        public void notifyUidSetChange(int[] uidArray) throws RemoteException {
        }

        @Override // com.color.antivirus.qihoo.ITransmitPointService
        public void registerUidChangeCallback(IUidSetChangeCallbackInterface uidCallback) throws RemoteException {
        }

        @Override // com.color.antivirus.qihoo.ITransmitPointService
        public void notifyAddUid(int uid) throws RemoteException {
        }

        @Override // com.color.antivirus.qihoo.ITransmitPointService
        public void notifyDeleteUid(int uid) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ITransmitPointService {
        private static final String DESCRIPTOR = "com.color.antivirus.qihoo.ITransmitPointService";
        static final int TRANSACTION_notifyAddUid = 7;
        static final int TRANSACTION_notifyDeleteUid = 8;
        static final int TRANSACTION_notifyProcessDied = 2;
        static final int TRANSACTION_notifyUidSetChange = 5;
        static final int TRANSACTION_pushId = 1;
        static final int TRANSACTION_pushInstallApp = 3;
        static final int TRANSACTION_registerCallBack = 4;
        static final int TRANSACTION_registerUidChangeCallback = 6;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITransmitPointService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITransmitPointService)) {
                return new Proxy(obj);
            }
            return (ITransmitPointService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "pushId";
                case 2:
                    return "notifyProcessDied";
                case 3:
                    return "pushInstallApp";
                case 4:
                    return "registerCallBack";
                case 5:
                    return "notifyUidSetChange";
                case 6:
                    return "registerUidChangeCallback";
                case 7:
                    return "notifyAddUid";
                case 8:
                    return "notifyDeleteUid";
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
                        pushId(data.readInt(), data.readInt(), data.readInt());
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        notifyProcessDied(data.readInt(), data.readInt());
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        pushInstallApp(data.createStringArray());
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        registerCallBack(ITransmitPointCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        notifyUidSetChange(data.createIntArray());
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        registerUidChangeCallback(IUidSetChangeCallbackInterface.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        notifyAddUid(data.readInt());
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        notifyDeleteUid(data.readInt());
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
        public static class Proxy implements ITransmitPointService {
            public static ITransmitPointService sDefaultImpl;
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

            @Override // com.color.antivirus.qihoo.ITransmitPointService
            public void pushId(int uid, int pid, int bid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    _data.writeInt(bid);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().pushId(uid, pid, bid);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.color.antivirus.qihoo.ITransmitPointService
            public void notifyProcessDied(int uid, int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyProcessDied(uid, pid);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.color.antivirus.qihoo.ITransmitPointService
            public void pushInstallApp(String[] apkInfoArray) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(apkInfoArray);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().pushInstallApp(apkInfoArray);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.color.antivirus.qihoo.ITransmitPointService
            public void registerCallBack(ITransmitPointCallback pushIdCallBack) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(pushIdCallBack != null ? pushIdCallBack.asBinder() : null);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerCallBack(pushIdCallBack);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.color.antivirus.qihoo.ITransmitPointService
            public void notifyUidSetChange(int[] uidArray) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(uidArray);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyUidSetChange(uidArray);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.color.antivirus.qihoo.ITransmitPointService
            public void registerUidChangeCallback(IUidSetChangeCallbackInterface uidCallback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(uidCallback != null ? uidCallback.asBinder() : null);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerUidChangeCallback(uidCallback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.color.antivirus.qihoo.ITransmitPointService
            public void notifyAddUid(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyAddUid(uid);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.color.antivirus.qihoo.ITransmitPointService
            public void notifyDeleteUid(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyDeleteUid(uid);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ITransmitPointService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ITransmitPointService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
