package com.mediatek.common.multiwindow;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IMWWmsCallback extends IInterface {
    void enableFocusedFrame(boolean z) throws RemoteException;

    void miniMaxTask(int i) throws RemoteException;

    void moveFloatingWindow(int i, int i2) throws RemoteException;

    void resizeFloatingWindow(int i, int i2, int i3) throws RemoteException;

    public static class Default implements IMWWmsCallback {
        @Override // com.mediatek.common.multiwindow.IMWWmsCallback
        public void moveFloatingWindow(int disX, int disY) throws RemoteException {
        }

        @Override // com.mediatek.common.multiwindow.IMWWmsCallback
        public void resizeFloatingWindow(int direction, int deltaX, int deltaY) throws RemoteException {
        }

        @Override // com.mediatek.common.multiwindow.IMWWmsCallback
        public void enableFocusedFrame(boolean enable) throws RemoteException {
        }

        @Override // com.mediatek.common.multiwindow.IMWWmsCallback
        public void miniMaxTask(int taskId) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMWWmsCallback {
        private static final String DESCRIPTOR = "com.mediatek.common.multiwindow.IMWWmsCallback";
        static final int TRANSACTION_enableFocusedFrame = 3;
        static final int TRANSACTION_miniMaxTask = 4;
        static final int TRANSACTION_moveFloatingWindow = 1;
        static final int TRANSACTION_resizeFloatingWindow = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMWWmsCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMWWmsCallback)) {
                return new Proxy(obj);
            }
            return (IMWWmsCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                moveFloatingWindow(data.readInt(), data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                resizeFloatingWindow(data.readInt(), data.readInt(), data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                enableFocusedFrame(data.readInt() != 0);
                reply.writeNoException();
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                miniMaxTask(data.readInt());
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IMWWmsCallback {
            public static IMWWmsCallback sDefaultImpl;
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

            @Override // com.mediatek.common.multiwindow.IMWWmsCallback
            public void moveFloatingWindow(int disX, int disY) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(disX);
                    _data.writeInt(disY);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().moveFloatingWindow(disX, disY);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMWWmsCallback
            public void resizeFloatingWindow(int direction, int deltaX, int deltaY) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(direction);
                    _data.writeInt(deltaX);
                    _data.writeInt(deltaY);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resizeFloatingWindow(direction, deltaX, deltaY);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMWWmsCallback
            public void enableFocusedFrame(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().enableFocusedFrame(enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.multiwindow.IMWWmsCallback
            public void miniMaxTask(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().miniMaxTask(taskId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IMWWmsCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMWWmsCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
