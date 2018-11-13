package com.oppo.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IOppoAppStartController extends IInterface {

    public static abstract class Stub extends Binder implements IOppoAppStartController {
        private static final String DESCRIPTOR = "com.oppo.app.IOppoAppStartController";
        static final int TRANSACTION_appStartMonitor = 1;
        static final int TRANSACTION_preventStartMonitor = 2;

        private static class Proxy implements IOppoAppStartController {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void appStartMonitor(String pkgName, String exceptionClass, String exceptionMsg, String exceptionTrace, String monitorType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeString(exceptionClass);
                    _data.writeString(exceptionMsg);
                    _data.writeString(exceptionTrace);
                    _data.writeString(monitorType);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void preventStartMonitor(String callerPkg, String calledPkg, String startMode, String preventMode, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerPkg);
                    _data.writeString(calledPkg);
                    _data.writeString(startMode);
                    _data.writeString(preventMode);
                    _data.writeString(reason);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    appStartMonitor(data.readString(), data.readString(), data.readString(), data.readString(), data.readString());
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    preventStartMonitor(data.readString(), data.readString(), data.readString(), data.readString(), data.readString());
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void appStartMonitor(String str, String str2, String str3, String str4, String str5) throws RemoteException;

    void preventStartMonitor(String str, String str2, String str3, String str4, String str5) throws RemoteException;
}
