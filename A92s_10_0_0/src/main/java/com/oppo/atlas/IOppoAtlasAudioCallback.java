package com.oppo.atlas;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IOppoAtlasAudioCallback extends IInterface {
    void onErrorVoiceChanger(int i) throws RemoteException;

    void onPlaybackStateChanged(int i, int i2) throws RemoteException;

    void onRecordingStateChanged(int i, int i2) throws RemoteException;

    public static class Default implements IOppoAtlasAudioCallback {
        @Override // com.oppo.atlas.IOppoAtlasAudioCallback
        public void onPlaybackStateChanged(int pid, int state) throws RemoteException {
        }

        @Override // com.oppo.atlas.IOppoAtlasAudioCallback
        public void onRecordingStateChanged(int pid, int state) throws RemoteException {
        }

        @Override // com.oppo.atlas.IOppoAtlasAudioCallback
        public void onErrorVoiceChanger(int state) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOppoAtlasAudioCallback {
        private static final String DESCRIPTOR = "com.oppo.atlas.IOppoAtlasAudioCallback";
        static final int TRANSACTION_onErrorVoiceChanger = 3;
        static final int TRANSACTION_onPlaybackStateChanged = 1;
        static final int TRANSACTION_onRecordingStateChanged = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOppoAtlasAudioCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOppoAtlasAudioCallback)) {
                return new Proxy(obj);
            }
            return (IOppoAtlasAudioCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onPlaybackStateChanged";
            }
            if (transactionCode == 2) {
                return "onRecordingStateChanged";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "onErrorVoiceChanger";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onPlaybackStateChanged(data.readInt(), data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onRecordingStateChanged(data.readInt(), data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onErrorVoiceChanger(data.readInt());
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IOppoAtlasAudioCallback {
            public static IOppoAtlasAudioCallback sDefaultImpl;
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

            @Override // com.oppo.atlas.IOppoAtlasAudioCallback
            public void onPlaybackStateChanged(int pid, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(state);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onPlaybackStateChanged(pid, state);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.atlas.IOppoAtlasAudioCallback
            public void onRecordingStateChanged(int pid, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(state);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onRecordingStateChanged(pid, state);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.atlas.IOppoAtlasAudioCallback
            public void onErrorVoiceChanger(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onErrorVoiceChanger(state);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IOppoAtlasAudioCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOppoAtlasAudioCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
