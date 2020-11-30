package com.mediatek.common.voiceextension;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IVoiceExtCommandListener extends IInterface {
    void onCommandRecognized(int i, int i2, String str) throws RemoteException;

    void onError(int i) throws RemoteException;

    void onPauseRecognition(int i) throws RemoteException;

    void onResumeRecognition(int i) throws RemoteException;

    void onSetCommands(int i) throws RemoteException;

    void onStartRecognition(int i) throws RemoteException;

    void onStopRecognition(int i) throws RemoteException;

    public static class Default implements IVoiceExtCommandListener {
        @Override // com.mediatek.common.voiceextension.IVoiceExtCommandListener
        public void onStartRecognition(int retCode) throws RemoteException {
        }

        @Override // com.mediatek.common.voiceextension.IVoiceExtCommandListener
        public void onStopRecognition(int retCode) throws RemoteException {
        }

        @Override // com.mediatek.common.voiceextension.IVoiceExtCommandListener
        public void onPauseRecognition(int retCode) throws RemoteException {
        }

        @Override // com.mediatek.common.voiceextension.IVoiceExtCommandListener
        public void onResumeRecognition(int retCode) throws RemoteException {
        }

        @Override // com.mediatek.common.voiceextension.IVoiceExtCommandListener
        public void onCommandRecognized(int retCode, int commandId, String commandStr) throws RemoteException {
        }

        @Override // com.mediatek.common.voiceextension.IVoiceExtCommandListener
        public void onError(int retCode) throws RemoteException {
        }

        @Override // com.mediatek.common.voiceextension.IVoiceExtCommandListener
        public void onSetCommands(int retCode) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IVoiceExtCommandListener {
        private static final String DESCRIPTOR = "com.mediatek.common.voiceextension.IVoiceExtCommandListener";
        static final int TRANSACTION_onCommandRecognized = 5;
        static final int TRANSACTION_onError = 6;
        static final int TRANSACTION_onPauseRecognition = 3;
        static final int TRANSACTION_onResumeRecognition = 4;
        static final int TRANSACTION_onSetCommands = 7;
        static final int TRANSACTION_onStartRecognition = 1;
        static final int TRANSACTION_onStopRecognition = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IVoiceExtCommandListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVoiceExtCommandListener)) {
                return new Proxy(obj);
            }
            return (IVoiceExtCommandListener) iin;
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
                        onStartRecognition(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onStopRecognition(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        onPauseRecognition(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        onResumeRecognition(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        onCommandRecognized(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        onError(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        onSetCommands(data.readInt());
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
        public static class Proxy implements IVoiceExtCommandListener {
            public static IVoiceExtCommandListener sDefaultImpl;
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

            @Override // com.mediatek.common.voiceextension.IVoiceExtCommandListener
            public void onStartRecognition(int retCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(retCode);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onStartRecognition(retCode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.voiceextension.IVoiceExtCommandListener
            public void onStopRecognition(int retCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(retCode);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onStopRecognition(retCode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.voiceextension.IVoiceExtCommandListener
            public void onPauseRecognition(int retCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(retCode);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onPauseRecognition(retCode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.voiceextension.IVoiceExtCommandListener
            public void onResumeRecognition(int retCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(retCode);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onResumeRecognition(retCode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.voiceextension.IVoiceExtCommandListener
            public void onCommandRecognized(int retCode, int commandId, String commandStr) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(retCode);
                    _data.writeInt(commandId);
                    _data.writeString(commandStr);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onCommandRecognized(retCode, commandId, commandStr);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.voiceextension.IVoiceExtCommandListener
            public void onError(int retCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(retCode);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onError(retCode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.voiceextension.IVoiceExtCommandListener
            public void onSetCommands(int retCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(retCode);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onSetCommands(retCode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IVoiceExtCommandListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IVoiceExtCommandListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
