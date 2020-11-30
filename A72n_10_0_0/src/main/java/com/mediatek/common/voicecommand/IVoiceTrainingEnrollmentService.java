package com.mediatek.common.voicecommand;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IVoiceTrainingEnrollmentService extends IInterface {
    boolean enrollSoundModel(int i, int i2, String str, int i3) throws RemoteException;

    boolean unEnrollSoundModel() throws RemoteException;

    public static class Default implements IVoiceTrainingEnrollmentService {
        @Override // com.mediatek.common.voicecommand.IVoiceTrainingEnrollmentService
        public boolean enrollSoundModel(int traningMode, int commandId, String patternPath, int user) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.common.voicecommand.IVoiceTrainingEnrollmentService
        public boolean unEnrollSoundModel() throws RemoteException {
            return false;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IVoiceTrainingEnrollmentService {
        private static final String DESCRIPTOR = "com.mediatek.common.voicecommand.IVoiceTrainingEnrollmentService";
        static final int TRANSACTION_enrollSoundModel = 1;
        static final int TRANSACTION_unEnrollSoundModel = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IVoiceTrainingEnrollmentService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVoiceTrainingEnrollmentService)) {
                return new Proxy(obj);
            }
            return (IVoiceTrainingEnrollmentService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                boolean enrollSoundModel = enrollSoundModel(data.readInt(), data.readInt(), data.readString(), data.readInt());
                reply.writeNoException();
                reply.writeInt(enrollSoundModel ? 1 : 0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                boolean unEnrollSoundModel = unEnrollSoundModel();
                reply.writeNoException();
                reply.writeInt(unEnrollSoundModel ? 1 : 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IVoiceTrainingEnrollmentService {
            public static IVoiceTrainingEnrollmentService sDefaultImpl;
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

            @Override // com.mediatek.common.voicecommand.IVoiceTrainingEnrollmentService
            public boolean enrollSoundModel(int traningMode, int commandId, String patternPath, int user) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(traningMode);
                    _data.writeInt(commandId);
                    _data.writeString(patternPath);
                    _data.writeInt(user);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().enrollSoundModel(traningMode, commandId, patternPath, user);
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

            @Override // com.mediatek.common.voicecommand.IVoiceTrainingEnrollmentService
            public boolean unEnrollSoundModel() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unEnrollSoundModel();
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
        }

        public static boolean setDefaultImpl(IVoiceTrainingEnrollmentService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IVoiceTrainingEnrollmentService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
