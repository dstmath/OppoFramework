package com.oppo.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IOppoPermissionRecordController extends IInterface {
    void notifyPermissionRecordInfo(String[] strArr, String[] strArr2, long[] jArr, int[] iArr) throws RemoteException;

    public static class Default implements IOppoPermissionRecordController {
        @Override // com.oppo.app.IOppoPermissionRecordController
        public void notifyPermissionRecordInfo(String[] packageNameList, String[] permissionNameList, long[] timeList, int[] resultList) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOppoPermissionRecordController {
        private static final String DESCRIPTOR = "com.oppo.app.IOppoPermissionRecordController";
        static final int TRANSACTION_notifyPermissionRecordInfo = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOppoPermissionRecordController asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOppoPermissionRecordController)) {
                return new Proxy(obj);
            }
            return (IOppoPermissionRecordController) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "notifyPermissionRecordInfo";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                notifyPermissionRecordInfo(data.createStringArray(), data.createStringArray(), data.createLongArray(), data.createIntArray());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IOppoPermissionRecordController {
            public static IOppoPermissionRecordController sDefaultImpl;
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

            @Override // com.oppo.app.IOppoPermissionRecordController
            public void notifyPermissionRecordInfo(String[] packageNameList, String[] permissionNameList, long[] timeList, int[] resultList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringArray(packageNameList);
                    _data.writeStringArray(permissionNameList);
                    _data.writeLongArray(timeList);
                    _data.writeIntArray(resultList);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyPermissionRecordInfo(packageNameList, permissionNameList, timeList, resultList);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IOppoPermissionRecordController impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOppoPermissionRecordController getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
