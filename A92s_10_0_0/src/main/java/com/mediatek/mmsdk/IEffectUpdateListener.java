package com.mediatek.mmsdk;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.mediatek.mmsdk.IEffectUser;

public interface IEffectUpdateListener extends IInterface {
    void onEffectUpdated(IEffectUser iEffectUser, int i) throws RemoteException;

    public static class Default implements IEffectUpdateListener {
        @Override // com.mediatek.mmsdk.IEffectUpdateListener
        public void onEffectUpdated(IEffectUser effect, int info) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IEffectUpdateListener {
        private static final String DESCRIPTOR = "com.mediatek.mmsdk.IEffectUpdateListener";
        static final int TRANSACTION_onEffectUpdated = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IEffectUpdateListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IEffectUpdateListener)) {
                return new Proxy(obj);
            }
            return (IEffectUpdateListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onEffectUpdated(IEffectUser.Stub.asInterface(data.readStrongBinder()), data.readInt());
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IEffectUpdateListener {
            public static IEffectUpdateListener sDefaultImpl;
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

            @Override // com.mediatek.mmsdk.IEffectUpdateListener
            public void onEffectUpdated(IEffectUser effect, int info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(effect != null ? effect.asBinder() : null);
                    _data.writeInt(info);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onEffectUpdated(effect, info);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IEffectUpdateListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IEffectUpdateListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
