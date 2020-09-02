package android.os;

import android.os.IOppoExInputCallBack;
import android.os.IOppoGestureCallBack;

public interface IOppoExService extends IInterface {
    void dealScreenoffGesture(int i) throws RemoteException;

    boolean getGestureState(int i) throws RemoteException;

    void pauseExInputEvent() throws RemoteException;

    boolean registerInputEvent(IOppoExInputCallBack iOppoExInputCallBack) throws RemoteException;

    boolean registerRawInputEvent(IOppoExInputCallBack iOppoExInputCallBack) throws RemoteException;

    boolean registerScreenoffGesture(IOppoGestureCallBack iOppoGestureCallBack) throws RemoteException;

    void resumeExInputEvent() throws RemoteException;

    void setGestureState(int i, boolean z) throws RemoteException;

    void unregisterInputEvent(IOppoExInputCallBack iOppoExInputCallBack) throws RemoteException;

    void unregisterScreenoffGesture(IOppoGestureCallBack iOppoGestureCallBack) throws RemoteException;

    public static class Default implements IOppoExService {
        @Override // android.os.IOppoExService
        public boolean registerInputEvent(IOppoExInputCallBack callBack) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoExService
        public boolean registerRawInputEvent(IOppoExInputCallBack callBack) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoExService
        public void unregisterInputEvent(IOppoExInputCallBack callBack) throws RemoteException {
        }

        @Override // android.os.IOppoExService
        public void pauseExInputEvent() throws RemoteException {
        }

        @Override // android.os.IOppoExService
        public void resumeExInputEvent() throws RemoteException {
        }

        @Override // android.os.IOppoExService
        public boolean registerScreenoffGesture(IOppoGestureCallBack callBack) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoExService
        public void unregisterScreenoffGesture(IOppoGestureCallBack callBack) throws RemoteException {
        }

        @Override // android.os.IOppoExService
        public void dealScreenoffGesture(int nGesture) throws RemoteException {
        }

        @Override // android.os.IOppoExService
        public void setGestureState(int nGesture, boolean isOpen) throws RemoteException {
        }

        @Override // android.os.IOppoExService
        public boolean getGestureState(int nGesture) throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOppoExService {
        private static final String DESCRIPTOR = "android.os.IOppoExService";
        static final int TRANSACTION_dealScreenoffGesture = 8;
        static final int TRANSACTION_getGestureState = 10;
        static final int TRANSACTION_pauseExInputEvent = 4;
        static final int TRANSACTION_registerInputEvent = 1;
        static final int TRANSACTION_registerRawInputEvent = 2;
        static final int TRANSACTION_registerScreenoffGesture = 6;
        static final int TRANSACTION_resumeExInputEvent = 5;
        static final int TRANSACTION_setGestureState = 9;
        static final int TRANSACTION_unregisterInputEvent = 3;
        static final int TRANSACTION_unregisterScreenoffGesture = 7;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOppoExService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOppoExService)) {
                return new Proxy(obj);
            }
            return (IOppoExService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "registerInputEvent";
                case 2:
                    return "registerRawInputEvent";
                case 3:
                    return "unregisterInputEvent";
                case 4:
                    return "pauseExInputEvent";
                case 5:
                    return "resumeExInputEvent";
                case 6:
                    return "registerScreenoffGesture";
                case 7:
                    return "unregisterScreenoffGesture";
                case 8:
                    return "dealScreenoffGesture";
                case 9:
                    return "setGestureState";
                case 10:
                    return "getGestureState";
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
                        boolean registerInputEvent = registerInputEvent(IOppoExInputCallBack.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerInputEvent ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerRawInputEvent = registerRawInputEvent(IOppoExInputCallBack.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerRawInputEvent ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterInputEvent(IOppoExInputCallBack.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        pauseExInputEvent();
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        resumeExInputEvent();
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerScreenoffGesture = registerScreenoffGesture(IOppoGestureCallBack.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerScreenoffGesture ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterScreenoffGesture(IOppoGestureCallBack.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        dealScreenoffGesture(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        setGestureState(data.readInt(), data.readInt() != 0);
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        boolean gestureState = getGestureState(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(gestureState ? 1 : 0);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IOppoExService {
            public static IOppoExService sDefaultImpl;
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

            @Override // android.os.IOppoExService
            public boolean registerInputEvent(IOppoExInputCallBack callBack) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callBack != null ? callBack.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerInputEvent(callBack);
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

            @Override // android.os.IOppoExService
            public boolean registerRawInputEvent(IOppoExInputCallBack callBack) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callBack != null ? callBack.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerRawInputEvent(callBack);
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

            @Override // android.os.IOppoExService
            public void unregisterInputEvent(IOppoExInputCallBack callBack) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callBack != null ? callBack.asBinder() : null);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterInputEvent(callBack);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoExService
            public void pauseExInputEvent() throws RemoteException {
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
                    Stub.getDefaultImpl().pauseExInputEvent();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoExService
            public void resumeExInputEvent() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resumeExInputEvent();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoExService
            public boolean registerScreenoffGesture(IOppoGestureCallBack callBack) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callBack != null ? callBack.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerScreenoffGesture(callBack);
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

            @Override // android.os.IOppoExService
            public void unregisterScreenoffGesture(IOppoGestureCallBack callBack) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callBack != null ? callBack.asBinder() : null);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterScreenoffGesture(callBack);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoExService
            public void dealScreenoffGesture(int nGesture) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(nGesture);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().dealScreenoffGesture(nGesture);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoExService
            public void setGestureState(int nGesture, boolean isOpen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(nGesture);
                    _data.writeInt(isOpen ? 1 : 0);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setGestureState(nGesture, isOpen);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoExService
            public boolean getGestureState(int nGesture) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(nGesture);
                    boolean _result = false;
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getGestureState(nGesture);
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
        }

        public static boolean setDefaultImpl(IOppoExService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOppoExService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
