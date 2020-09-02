package com.mediatek.vow;

import android.hardware.soundtrigger.IRecognitionStatusCallback;
import android.hardware.soundtrigger.SoundTrigger;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IVoiceWakeupBridge extends IInterface {
    int startRecognition(int i, SoundTrigger.KeyphraseSoundModel keyphraseSoundModel, IRecognitionStatusCallback iRecognitionStatusCallback, SoundTrigger.RecognitionConfig recognitionConfig) throws RemoteException;

    int stopRecognition(int i, IRecognitionStatusCallback iRecognitionStatusCallback) throws RemoteException;

    int unloadKeyphraseModel(int i) throws RemoteException;

    public static class Default implements IVoiceWakeupBridge {
        @Override // com.mediatek.vow.IVoiceWakeupBridge
        public int startRecognition(int keyphraseId, SoundTrigger.KeyphraseSoundModel soundModel, IRecognitionStatusCallback listener, SoundTrigger.RecognitionConfig recognitionConfig) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.vow.IVoiceWakeupBridge
        public int stopRecognition(int keyphraseId, IRecognitionStatusCallback listener) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.vow.IVoiceWakeupBridge
        public int unloadKeyphraseModel(int keyphaseId) throws RemoteException {
            return 0;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IVoiceWakeupBridge {
        private static final String DESCRIPTOR = "com.mediatek.vow.IVoiceWakeupBridge";
        static final int TRANSACTION_startRecognition = 1;
        static final int TRANSACTION_stopRecognition = 2;
        static final int TRANSACTION_unloadKeyphraseModel = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IVoiceWakeupBridge asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVoiceWakeupBridge)) {
                return new Proxy(obj);
            }
            return (IVoiceWakeupBridge) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            SoundTrigger.KeyphraseSoundModel _arg1;
            SoundTrigger.RecognitionConfig _arg3;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _arg0 = data.readInt();
                if (data.readInt() != 0) {
                    _arg1 = (SoundTrigger.KeyphraseSoundModel) SoundTrigger.KeyphraseSoundModel.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                IRecognitionStatusCallback _arg2 = IRecognitionStatusCallback.Stub.asInterface(data.readStrongBinder());
                if (data.readInt() != 0) {
                    _arg3 = (SoundTrigger.RecognitionConfig) SoundTrigger.RecognitionConfig.CREATOR.createFromParcel(data);
                } else {
                    _arg3 = null;
                }
                int _result = startRecognition(_arg0, _arg1, _arg2, _arg3);
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int _result2 = stopRecognition(data.readInt(), IRecognitionStatusCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(_result2);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                int _result3 = unloadKeyphraseModel(data.readInt());
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

        private static class Proxy implements IVoiceWakeupBridge {
            public static IVoiceWakeupBridge sDefaultImpl;
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

            @Override // com.mediatek.vow.IVoiceWakeupBridge
            public int startRecognition(int keyphraseId, SoundTrigger.KeyphraseSoundModel soundModel, IRecognitionStatusCallback listener, SoundTrigger.RecognitionConfig recognitionConfig) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(keyphraseId);
                    if (soundModel != null) {
                        _data.writeInt(1);
                        soundModel.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (recognitionConfig != null) {
                        _data.writeInt(1);
                        recognitionConfig.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startRecognition(keyphraseId, soundModel, listener, recognitionConfig);
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

            @Override // com.mediatek.vow.IVoiceWakeupBridge
            public int stopRecognition(int keyphraseId, IRecognitionStatusCallback listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(keyphraseId);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().stopRecognition(keyphraseId, listener);
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

            @Override // com.mediatek.vow.IVoiceWakeupBridge
            public int unloadKeyphraseModel(int keyphaseId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(keyphaseId);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unloadKeyphraseModel(keyphaseId);
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

        public static boolean setDefaultImpl(IVoiceWakeupBridge impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IVoiceWakeupBridge getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
