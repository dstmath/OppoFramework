package com.mediatek.internal.telecom;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.mediatek.internal.telecom.ICallRecorderCallback;

public interface ICallRecorderService extends IInterface {
    void setCallback(ICallRecorderCallback iCallRecorderCallback) throws RemoteException;

    void startVoiceRecord() throws RemoteException;

    void stopVoiceRecord() throws RemoteException;

    public static class Default implements ICallRecorderService {
        @Override // com.mediatek.internal.telecom.ICallRecorderService
        public void startVoiceRecord() throws RemoteException {
        }

        @Override // com.mediatek.internal.telecom.ICallRecorderService
        public void stopVoiceRecord() throws RemoteException {
        }

        @Override // com.mediatek.internal.telecom.ICallRecorderService
        public void setCallback(ICallRecorderCallback callback) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ICallRecorderService {
        private static final String DESCRIPTOR = "com.mediatek.internal.telecom.ICallRecorderService";
        static final int TRANSACTION_setCallback = 3;
        static final int TRANSACTION_startVoiceRecord = 1;
        static final int TRANSACTION_stopVoiceRecord = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ICallRecorderService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICallRecorderService)) {
                return new Proxy(obj);
            }
            return (ICallRecorderService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                startVoiceRecord();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                stopVoiceRecord();
                return true;
            } else if (code == TRANSACTION_setCallback) {
                data.enforceInterface(DESCRIPTOR);
                setCallback(ICallRecorderCallback.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements ICallRecorderService {
            public static ICallRecorderService sDefaultImpl;
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

            @Override // com.mediatek.internal.telecom.ICallRecorderService
            public void startVoiceRecord() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().startVoiceRecord();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telecom.ICallRecorderService
            public void stopVoiceRecord() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().stopVoiceRecord();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telecom.ICallRecorderService
            public void setCallback(ICallRecorderCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(Stub.TRANSACTION_setCallback, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setCallback(callback);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ICallRecorderService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ICallRecorderService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
