package android.os;

public interface IOppoAutoInstallService extends IInterface {

    public static abstract class Stub extends Binder implements IOppoAutoInstallService {
        private static final String DESCRIPTOR = "android.os.IOppoAutoInstallService";
        static final int TRANSACTION_doGr = 1;

        private static class Proxy implements IOppoAutoInstallService {
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

            public void doGr(String baseCodePath, String appName, String pkgName, String action) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(baseCodePath);
                    _data.writeString(appName);
                    _data.writeString(pkgName);
                    _data.writeString(action);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOppoAutoInstallService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOppoAutoInstallService)) {
                return new Proxy(obj);
            }
            return (IOppoAutoInstallService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    doGr(data.readString(), data.readString(), data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void doGr(String str, String str2, String str3, String str4) throws RemoteException;
}
