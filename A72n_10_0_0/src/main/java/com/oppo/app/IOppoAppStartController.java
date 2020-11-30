package com.oppo.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IOppoAppStartController extends IInterface {
    void appStartMonitor(String str, String str2, String str3, String str4, String str5) throws RemoteException;

    void notifyPreventIndulge(String str) throws RemoteException;

    void preventStartMonitor(String str, String str2, String str3, String str4, String str5) throws RemoteException;

    public static class Default implements IOppoAppStartController {
        @Override // com.oppo.app.IOppoAppStartController
        public void appStartMonitor(String pkgName, String exceptionClass, String exceptionMsg, String exceptionTrace, String monitorType) throws RemoteException {
        }

        @Override // com.oppo.app.IOppoAppStartController
        public void preventStartMonitor(String callerPkg, String calledPkg, String startMode, String preventMode, String reason) throws RemoteException {
        }

        @Override // com.oppo.app.IOppoAppStartController
        public void notifyPreventIndulge(String pkgName) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOppoAppStartController {
        private static final String DESCRIPTOR = "com.oppo.app.IOppoAppStartController";
        static final int TRANSACTION_appStartMonitor = 1;
        static final int TRANSACTION_notifyPreventIndulge = 3;
        static final int TRANSACTION_preventStartMonitor = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOppoAppStartController asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOppoAppStartController)) {
                return new Proxy(obj);
            }
            return (IOppoAppStartController) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "appStartMonitor";
            }
            if (transactionCode == 2) {
                return "preventStartMonitor";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "notifyPreventIndulge";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                appStartMonitor(data.readString(), data.readString(), data.readString(), data.readString(), data.readString());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                preventStartMonitor(data.readString(), data.readString(), data.readString(), data.readString(), data.readString());
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                notifyPreventIndulge(data.readString());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IOppoAppStartController {
            public static IOppoAppStartController sDefaultImpl;
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

            @Override // com.oppo.app.IOppoAppStartController
            public void appStartMonitor(String pkgName, String exceptionClass, String exceptionMsg, String exceptionTrace, String monitorType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeString(exceptionClass);
                    _data.writeString(exceptionMsg);
                    _data.writeString(exceptionTrace);
                    _data.writeString(monitorType);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().appStartMonitor(pkgName, exceptionClass, exceptionMsg, exceptionTrace, monitorType);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.oppo.app.IOppoAppStartController
            public void preventStartMonitor(String callerPkg, String calledPkg, String startMode, String preventMode, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPkg);
                    _data.writeString(calledPkg);
                    _data.writeString(startMode);
                    _data.writeString(preventMode);
                    _data.writeString(reason);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().preventStartMonitor(callerPkg, calledPkg, startMode, preventMode, reason);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.oppo.app.IOppoAppStartController
            public void notifyPreventIndulge(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyPreventIndulge(pkgName);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IOppoAppStartController impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOppoAppStartController getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
