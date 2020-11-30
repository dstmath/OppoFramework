package android.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IOppoArmyManager extends IInterface {
    boolean addDisallowedRunningApp(List<String> list) throws RemoteException;

    void allowToUseSdcard(boolean z) throws RemoteException;

    List<String> getDisallowedRunningApp() throws RemoteException;

    boolean removeDisallowedRunningApp(List<String> list) throws RemoteException;

    public static class Default implements IOppoArmyManager {
        @Override // android.app.IOppoArmyManager
        public boolean addDisallowedRunningApp(List<String> list) throws RemoteException {
            return false;
        }

        @Override // android.app.IOppoArmyManager
        public boolean removeDisallowedRunningApp(List<String> list) throws RemoteException {
            return false;
        }

        @Override // android.app.IOppoArmyManager
        public List<String> getDisallowedRunningApp() throws RemoteException {
            return null;
        }

        @Override // android.app.IOppoArmyManager
        public void allowToUseSdcard(boolean allow) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOppoArmyManager {
        private static final String DESCRIPTOR = "android.app.IOppoArmyManager";
        static final int TRANSACTION_addDisallowedRunningApp = 1;
        static final int TRANSACTION_allowToUseSdcard = 4;
        static final int TRANSACTION_getDisallowedRunningApp = 3;
        static final int TRANSACTION_removeDisallowedRunningApp = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOppoArmyManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOppoArmyManager)) {
                return new Proxy(obj);
            }
            return (IOppoArmyManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "addDisallowedRunningApp";
            }
            if (transactionCode == 2) {
                return "removeDisallowedRunningApp";
            }
            if (transactionCode == 3) {
                return "getDisallowedRunningApp";
            }
            if (transactionCode != 4) {
                return null;
            }
            return "allowToUseSdcard";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                boolean addDisallowedRunningApp = addDisallowedRunningApp(data.createStringArrayList());
                reply.writeNoException();
                reply.writeInt(addDisallowedRunningApp ? 1 : 0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                boolean removeDisallowedRunningApp = removeDisallowedRunningApp(data.createStringArrayList());
                reply.writeNoException();
                reply.writeInt(removeDisallowedRunningApp ? 1 : 0);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                List<String> _result = getDisallowedRunningApp();
                reply.writeNoException();
                reply.writeStringList(_result);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                allowToUseSdcard(data.readInt() != 0);
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IOppoArmyManager {
            public static IOppoArmyManager sDefaultImpl;
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

            @Override // android.app.IOppoArmyManager
            public boolean addDisallowedRunningApp(List<String> appPkgNamesList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(appPkgNamesList);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addDisallowedRunningApp(appPkgNamesList);
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

            @Override // android.app.IOppoArmyManager
            public boolean removeDisallowedRunningApp(List<String> appPkgNamesList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(appPkgNamesList);
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeDisallowedRunningApp(appPkgNamesList);
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

            @Override // android.app.IOppoArmyManager
            public List<String> getDisallowedRunningApp() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDisallowedRunningApp();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IOppoArmyManager
            public void allowToUseSdcard(boolean allow) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(allow ? 1 : 0);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().allowToUseSdcard(allow);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IOppoArmyManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOppoArmyManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
