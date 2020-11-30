package android.net;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.Map;

public interface IOppoNetworkingControlManager extends IInterface {
    void factoryReset() throws RemoteException;

    Map getPolicyList() throws RemoteException;

    int getUidPolicy(int i) throws RemoteException;

    int[] getUidsWithPolicy(int i) throws RemoteException;

    void setUidPolicy(int i, int i2) throws RemoteException;

    public static class Default implements IOppoNetworkingControlManager {
        @Override // android.net.IOppoNetworkingControlManager
        public void setUidPolicy(int uid, int policy) throws RemoteException {
        }

        @Override // android.net.IOppoNetworkingControlManager
        public int getUidPolicy(int uid) throws RemoteException {
            return 0;
        }

        @Override // android.net.IOppoNetworkingControlManager
        public int[] getUidsWithPolicy(int policy) throws RemoteException {
            return null;
        }

        @Override // android.net.IOppoNetworkingControlManager
        public void factoryReset() throws RemoteException {
        }

        @Override // android.net.IOppoNetworkingControlManager
        public Map getPolicyList() throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOppoNetworkingControlManager {
        private static final String DESCRIPTOR = "android.net.IOppoNetworkingControlManager";
        static final int TRANSACTION_factoryReset = 4;
        static final int TRANSACTION_getPolicyList = 5;
        static final int TRANSACTION_getUidPolicy = 2;
        static final int TRANSACTION_getUidsWithPolicy = 3;
        static final int TRANSACTION_setUidPolicy = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOppoNetworkingControlManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOppoNetworkingControlManager)) {
                return new Proxy(obj);
            }
            return (IOppoNetworkingControlManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "setUidPolicy";
            }
            if (transactionCode == 2) {
                return "getUidPolicy";
            }
            if (transactionCode == 3) {
                return "getUidsWithPolicy";
            }
            if (transactionCode == 4) {
                return "factoryReset";
            }
            if (transactionCode != 5) {
                return null;
            }
            return "getPolicyList";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                setUidPolicy(data.readInt(), data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int _result = getUidPolicy(data.readInt());
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                int[] _result2 = getUidsWithPolicy(data.readInt());
                reply.writeNoException();
                reply.writeIntArray(_result2);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                factoryReset();
                reply.writeNoException();
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                Map _result3 = getPolicyList();
                reply.writeNoException();
                reply.writeMap(_result3);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IOppoNetworkingControlManager {
            public static IOppoNetworkingControlManager sDefaultImpl;
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

            @Override // android.net.IOppoNetworkingControlManager
            public void setUidPolicy(int uid, int policy) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(policy);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setUidPolicy(uid, policy);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IOppoNetworkingControlManager
            public int getUidPolicy(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUidPolicy(uid);
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

            @Override // android.net.IOppoNetworkingControlManager
            public int[] getUidsWithPolicy(int policy) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(policy);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUidsWithPolicy(policy);
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IOppoNetworkingControlManager
            public void factoryReset() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().factoryReset();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IOppoNetworkingControlManager
            public Map getPolicyList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPolicyList();
                    }
                    _reply.readException();
                    Map _result = _reply.readHashMap(getClass().getClassLoader());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IOppoNetworkingControlManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOppoNetworkingControlManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
