package com.mediatek.common.voicecommand;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.mediatek.common.voicecommand.IVoiceWakeupInteractionCallback;

public interface IVoiceWakeupInteractionService extends IInterface {
    void registerCallback(IVoiceWakeupInteractionCallback iVoiceWakeupInteractionCallback) throws RemoteException;

    void setCurrentUserOnSwitch(int i) throws RemoteException;

    boolean startRecognition() throws RemoteException;

    boolean stopRecognition() throws RemoteException;

    public static class Default implements IVoiceWakeupInteractionService {
        @Override // com.mediatek.common.voicecommand.IVoiceWakeupInteractionService
        public void registerCallback(IVoiceWakeupInteractionCallback callback) throws RemoteException {
        }

        @Override // com.mediatek.common.voicecommand.IVoiceWakeupInteractionService
        public boolean startRecognition() throws RemoteException {
            return false;
        }

        @Override // com.mediatek.common.voicecommand.IVoiceWakeupInteractionService
        public boolean stopRecognition() throws RemoteException {
            return false;
        }

        @Override // com.mediatek.common.voicecommand.IVoiceWakeupInteractionService
        public void setCurrentUserOnSwitch(int userId) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IVoiceWakeupInteractionService {
        private static final String DESCRIPTOR = "com.mediatek.common.voicecommand.IVoiceWakeupInteractionService";
        static final int TRANSACTION_registerCallback = 1;
        static final int TRANSACTION_setCurrentUserOnSwitch = 4;
        static final int TRANSACTION_startRecognition = 2;
        static final int TRANSACTION_stopRecognition = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IVoiceWakeupInteractionService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVoiceWakeupInteractionService)) {
                return new Proxy(obj);
            }
            return (IVoiceWakeupInteractionService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                registerCallback(IVoiceWakeupInteractionCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                boolean startRecognition = startRecognition();
                reply.writeNoException();
                reply.writeInt(startRecognition ? 1 : 0);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                boolean stopRecognition = stopRecognition();
                reply.writeNoException();
                reply.writeInt(stopRecognition ? 1 : 0);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                setCurrentUserOnSwitch(data.readInt());
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
        public static class Proxy implements IVoiceWakeupInteractionService {
            public static IVoiceWakeupInteractionService sDefaultImpl;
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

            @Override // com.mediatek.common.voicecommand.IVoiceWakeupInteractionService
            public void registerCallback(IVoiceWakeupInteractionCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.voicecommand.IVoiceWakeupInteractionService
            public boolean startRecognition() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startRecognition();
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

            @Override // com.mediatek.common.voicecommand.IVoiceWakeupInteractionService
            public boolean stopRecognition() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().stopRecognition();
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

            @Override // com.mediatek.common.voicecommand.IVoiceWakeupInteractionService
            public void setCurrentUserOnSwitch(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setCurrentUserOnSwitch(userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IVoiceWakeupInteractionService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IVoiceWakeupInteractionService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
