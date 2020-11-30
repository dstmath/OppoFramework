package com.mediatek.gwsd;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IGwsdListener extends IInterface {
    void onAddListenered(int i, String str) throws RemoteException;

    void onAutoRejectModeChanged(int i, String str) throws RemoteException;

    void onCallValidTimerChanged(int i, String str) throws RemoteException;

    void onIgnoreSameNumberIntervalChanged(int i, String str) throws RemoteException;

    void onSyncGwsdInfoFinished(int i, String str) throws RemoteException;

    void onSystemStateChanged(int i) throws RemoteException;

    void onUserSelectionModeChanged(int i, String str) throws RemoteException;

    public static class Default implements IGwsdListener {
        @Override // com.mediatek.gwsd.IGwsdListener
        public void onAddListenered(int status, String reason) throws RemoteException {
        }

        @Override // com.mediatek.gwsd.IGwsdListener
        public void onUserSelectionModeChanged(int status, String reason) throws RemoteException {
        }

        @Override // com.mediatek.gwsd.IGwsdListener
        public void onAutoRejectModeChanged(int status, String reason) throws RemoteException {
        }

        @Override // com.mediatek.gwsd.IGwsdListener
        public void onSyncGwsdInfoFinished(int status, String reason) throws RemoteException {
        }

        @Override // com.mediatek.gwsd.IGwsdListener
        public void onSystemStateChanged(int state) throws RemoteException {
        }

        @Override // com.mediatek.gwsd.IGwsdListener
        public void onCallValidTimerChanged(int status, String reason) throws RemoteException {
        }

        @Override // com.mediatek.gwsd.IGwsdListener
        public void onIgnoreSameNumberIntervalChanged(int status, String reason) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IGwsdListener {
        private static final String DESCRIPTOR = "com.mediatek.gwsd.IGwsdListener";
        static final int TRANSACTION_onAddListenered = 1;
        static final int TRANSACTION_onAutoRejectModeChanged = 3;
        static final int TRANSACTION_onCallValidTimerChanged = 6;
        static final int TRANSACTION_onIgnoreSameNumberIntervalChanged = 7;
        static final int TRANSACTION_onSyncGwsdInfoFinished = 4;
        static final int TRANSACTION_onSystemStateChanged = 5;
        static final int TRANSACTION_onUserSelectionModeChanged = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IGwsdListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGwsdListener)) {
                return new Proxy(obj);
            }
            return (IGwsdListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        onAddListenered(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onUserSelectionModeChanged(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        onAutoRejectModeChanged(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        onSyncGwsdInfoFinished(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        onSystemStateChanged(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        onCallValidTimerChanged(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        onIgnoreSameNumberIntervalChanged(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IGwsdListener {
            public static IGwsdListener sDefaultImpl;
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

            @Override // com.mediatek.gwsd.IGwsdListener
            public void onAddListenered(int status, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    _data.writeString(reason);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onAddListenered(status, reason);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.gwsd.IGwsdListener
            public void onUserSelectionModeChanged(int status, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    _data.writeString(reason);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onUserSelectionModeChanged(status, reason);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.gwsd.IGwsdListener
            public void onAutoRejectModeChanged(int status, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    _data.writeString(reason);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onAutoRejectModeChanged(status, reason);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.gwsd.IGwsdListener
            public void onSyncGwsdInfoFinished(int status, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    _data.writeString(reason);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onSyncGwsdInfoFinished(status, reason);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.gwsd.IGwsdListener
            public void onSystemStateChanged(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onSystemStateChanged(state);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.gwsd.IGwsdListener
            public void onCallValidTimerChanged(int status, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    _data.writeString(reason);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onCallValidTimerChanged(status, reason);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.gwsd.IGwsdListener
            public void onIgnoreSameNumberIntervalChanged(int status, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    _data.writeString(reason);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onIgnoreSameNumberIntervalChanged(status, reason);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IGwsdListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IGwsdListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
