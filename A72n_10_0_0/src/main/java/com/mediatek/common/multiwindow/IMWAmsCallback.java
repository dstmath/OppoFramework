package com.mediatek.common.multiwindow;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IMWAmsCallback extends IInterface {
    String findProcessNameByToken(IBinder iBinder) throws RemoteException;

    int findStackIdByTask(int i) throws RemoteException;

    int findStackIdByToken(IBinder iBinder) throws RemoteException;

    boolean moveActivityTaskToFront(IBinder iBinder) throws RemoteException;

    void restoreStack(IBinder iBinder, boolean z) throws RemoteException;

    public static class Default implements IMWAmsCallback {
        @Override // com.mediatek.common.multiwindow.IMWAmsCallback
        public void restoreStack(IBinder token, boolean toMax) throws RemoteException {
        }

        @Override // com.mediatek.common.multiwindow.IMWAmsCallback
        public String findProcessNameByToken(IBinder token) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.common.multiwindow.IMWAmsCallback
        public boolean moveActivityTaskToFront(IBinder token) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.common.multiwindow.IMWAmsCallback
        public int findStackIdByToken(IBinder token) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.common.multiwindow.IMWAmsCallback
        public int findStackIdByTask(int taskId) throws RemoteException {
            return 0;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMWAmsCallback {
        private static final String DESCRIPTOR = "com.mediatek.common.multiwindow.IMWAmsCallback";
        static final int TRANSACTION_findProcessNameByToken = 2;
        static final int TRANSACTION_findStackIdByTask = 5;
        static final int TRANSACTION_findStackIdByToken = 4;
        static final int TRANSACTION_moveActivityTaskToFront = 3;
        static final int TRANSACTION_restoreStack = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMWAmsCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMWAmsCallback)) {
                return new Proxy(obj);
            }
            return (IMWAmsCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                restoreStack(data.readStrongBinder(), data.readInt() != 0);
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                String _result = findProcessNameByToken(data.readStrongBinder());
                reply.writeNoException();
                reply.writeString(_result);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                boolean moveActivityTaskToFront = moveActivityTaskToFront(data.readStrongBinder());
                reply.writeNoException();
                reply.writeInt(moveActivityTaskToFront ? 1 : 0);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                int _result2 = findStackIdByToken(data.readStrongBinder());
                reply.writeNoException();
                reply.writeInt(_result2);
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                int _result3 = findStackIdByTask(data.readInt());
                reply.writeNoException();
                reply.writeInt(_result3);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IMWAmsCallback {
            public static IMWAmsCallback sDefaultImpl;
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

            @Override // com.mediatek.common.multiwindow.IMWAmsCallback
            public void restoreStack(IBinder token, boolean toMax) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(toMax ? 1 : 0);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().restoreStack(token, toMax);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMWAmsCallback
            public String findProcessNameByToken(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().findProcessNameByToken(token);
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

            @Override // com.mediatek.common.multiwindow.IMWAmsCallback
            public boolean moveActivityTaskToFront(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().moveActivityTaskToFront(token);
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

            @Override // com.mediatek.common.multiwindow.IMWAmsCallback
            public int findStackIdByToken(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().findStackIdByToken(token);
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

            @Override // com.mediatek.common.multiwindow.IMWAmsCallback
            public int findStackIdByTask(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().findStackIdByTask(taskId);
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
        }

        public static boolean setDefaultImpl(IMWAmsCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMWAmsCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
