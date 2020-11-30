package android.os;

public interface IOppoGromService extends IInterface {
    boolean getGromInit() throws RemoteException;

    String getGromMsg() throws RemoteException;

    int gromGetCode(String str) throws RemoteException;

    boolean gromGetCustomize(String str) throws RemoteException;

    boolean gromGetUpdated(String str) throws RemoteException;

    public static class Default implements IOppoGromService {
        @Override // android.os.IOppoGromService
        public boolean getGromInit() throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoGromService
        public int gromGetCode(String mImei) throws RemoteException {
            return 0;
        }

        @Override // android.os.IOppoGromService
        public boolean gromGetCustomize(String mImei) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoGromService
        public boolean gromGetUpdated(String mImei) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoGromService
        public String getGromMsg() throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOppoGromService {
        private static final String DESCRIPTOR = "android.os.IOppoGromService";
        static final int TRANSACTION_getGromInit = 1;
        static final int TRANSACTION_getGromMsg = 5;
        static final int TRANSACTION_gromGetCode = 2;
        static final int TRANSACTION_gromGetCustomize = 3;
        static final int TRANSACTION_gromGetUpdated = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOppoGromService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOppoGromService)) {
                return new Proxy(obj);
            }
            return (IOppoGromService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "getGromInit";
            }
            if (transactionCode == 2) {
                return "gromGetCode";
            }
            if (transactionCode == 3) {
                return "gromGetCustomize";
            }
            if (transactionCode == 4) {
                return "gromGetUpdated";
            }
            if (transactionCode != 5) {
                return null;
            }
            return "getGromMsg";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                boolean gromInit = getGromInit();
                reply.writeNoException();
                reply.writeInt(gromInit ? 1 : 0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int _result = gromGetCode(data.readString());
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                boolean gromGetCustomize = gromGetCustomize(data.readString());
                reply.writeNoException();
                reply.writeInt(gromGetCustomize ? 1 : 0);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                boolean gromGetUpdated = gromGetUpdated(data.readString());
                reply.writeNoException();
                reply.writeInt(gromGetUpdated ? 1 : 0);
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                String _result2 = getGromMsg();
                reply.writeNoException();
                reply.writeString(_result2);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IOppoGromService {
            public static IOppoGromService sDefaultImpl;
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

            @Override // android.os.IOppoGromService
            public boolean getGromInit() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getGromInit();
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

            @Override // android.os.IOppoGromService
            public int gromGetCode(String mImei) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(mImei);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().gromGetCode(mImei);
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

            @Override // android.os.IOppoGromService
            public boolean gromGetCustomize(String mImei) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(mImei);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().gromGetCustomize(mImei);
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

            @Override // android.os.IOppoGromService
            public boolean gromGetUpdated(String mImei) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(mImei);
                    boolean _result = false;
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().gromGetUpdated(mImei);
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

            @Override // android.os.IOppoGromService
            public String getGromMsg() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getGromMsg();
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
        }

        public static boolean setDefaultImpl(IOppoGromService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOppoGromService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
