package com.mediatek.common.operamax;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ILoaderStateListener extends IInterface {
    void onSavingState(int i) throws RemoteException;

    void onTunnelState(int i) throws RemoteException;

    public static class Default implements ILoaderStateListener {
        @Override // com.mediatek.common.operamax.ILoaderStateListener
        public void onTunnelState(int state) throws RemoteException {
        }

        @Override // com.mediatek.common.operamax.ILoaderStateListener
        public void onSavingState(int state) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ILoaderStateListener {
        private static final String DESCRIPTOR = "com.mediatek.common.operamax.ILoaderStateListener";
        static final int TRANSACTION_onSavingState = 2;
        static final int TRANSACTION_onTunnelState = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ILoaderStateListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ILoaderStateListener)) {
                return new Proxy(obj);
            }
            return (ILoaderStateListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onTunnelState(data.readInt());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onSavingState(data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements ILoaderStateListener {
            public static ILoaderStateListener sDefaultImpl;
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

            @Override // com.mediatek.common.operamax.ILoaderStateListener
            public void onTunnelState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onTunnelState(state);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.operamax.ILoaderStateListener
            public void onSavingState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onSavingState(state);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ILoaderStateListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ILoaderStateListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
