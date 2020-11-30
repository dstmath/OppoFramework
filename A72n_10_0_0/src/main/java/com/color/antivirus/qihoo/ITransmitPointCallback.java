package com.color.antivirus.qihoo;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ITransmitPointCallback extends IInterface {
    void notifyInstallApp(String[] strArr) throws RemoteException;

    void notifyProcessDied(int i, int i2) throws RemoteException;

    void notifyPushId(int i, int i2, int i3) throws RemoteException;

    public static class Default implements ITransmitPointCallback {
        @Override // com.color.antivirus.qihoo.ITransmitPointCallback
        public void notifyPushId(int uid, int pid, int bid) throws RemoteException {
        }

        @Override // com.color.antivirus.qihoo.ITransmitPointCallback
        public void notifyProcessDied(int uid, int pid) throws RemoteException {
        }

        @Override // com.color.antivirus.qihoo.ITransmitPointCallback
        public void notifyInstallApp(String[] apkInfoArray) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ITransmitPointCallback {
        private static final String DESCRIPTOR = "com.color.antivirus.qihoo.ITransmitPointCallback";
        static final int TRANSACTION_notifyInstallApp = 3;
        static final int TRANSACTION_notifyProcessDied = 2;
        static final int TRANSACTION_notifyPushId = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITransmitPointCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITransmitPointCallback)) {
                return new Proxy(obj);
            }
            return (ITransmitPointCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "notifyPushId";
            }
            if (transactionCode == 2) {
                return "notifyProcessDied";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "notifyInstallApp";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                notifyPushId(data.readInt(), data.readInt(), data.readInt());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                notifyProcessDied(data.readInt(), data.readInt());
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                notifyInstallApp(data.createStringArray());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ITransmitPointCallback {
            public static ITransmitPointCallback sDefaultImpl;
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

            @Override // com.color.antivirus.qihoo.ITransmitPointCallback
            public void notifyPushId(int uid, int pid, int bid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    _data.writeInt(bid);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyPushId(uid, pid, bid);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.color.antivirus.qihoo.ITransmitPointCallback
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

            @Override // com.color.antivirus.qihoo.ITransmitPointCallback
            public void notifyInstallApp(String[] apkInfoArray) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(apkInfoArray);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyInstallApp(apkInfoArray);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ITransmitPointCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ITransmitPointCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
