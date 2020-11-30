package com.mediatek.common.voicecommand;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IVoiceWakeupInteractionCallback extends IInterface {
    void onVoiceWakeupDetected(int i) throws RemoteException;

    public static class Default implements IVoiceWakeupInteractionCallback {
        @Override // com.mediatek.common.voicecommand.IVoiceWakeupInteractionCallback
        public void onVoiceWakeupDetected(int commandId) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IVoiceWakeupInteractionCallback {
        private static final String DESCRIPTOR = "com.mediatek.common.voicecommand.IVoiceWakeupInteractionCallback";
        static final int TRANSACTION_onVoiceWakeupDetected = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IVoiceWakeupInteractionCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVoiceWakeupInteractionCallback)) {
                return new Proxy(obj);
            }
            return (IVoiceWakeupInteractionCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onVoiceWakeupDetected(data.readInt());
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
        public static class Proxy implements IVoiceWakeupInteractionCallback {
            public static IVoiceWakeupInteractionCallback sDefaultImpl;
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

            @Override // com.mediatek.common.voicecommand.IVoiceWakeupInteractionCallback
            public void onVoiceWakeupDetected(int commandId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(commandId);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onVoiceWakeupDetected(commandId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IVoiceWakeupInteractionCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IVoiceWakeupInteractionCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
