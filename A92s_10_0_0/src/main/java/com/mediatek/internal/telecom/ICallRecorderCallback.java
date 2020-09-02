package com.mediatek.internal.telecom;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ICallRecorderCallback extends IInterface {
    void onRecordEvent(int i, String str) throws RemoteException;

    void onRecordStateChanged(int i) throws RemoteException;

    public static class Default implements ICallRecorderCallback {
        @Override // com.mediatek.internal.telecom.ICallRecorderCallback
        public void onRecordStateChanged(int state) throws RemoteException {
        }

        @Override // com.mediatek.internal.telecom.ICallRecorderCallback
        public void onRecordEvent(int eventId, String eventContent) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ICallRecorderCallback {
        private static final String DESCRIPTOR = "com.mediatek.internal.telecom.ICallRecorderCallback";
        static final int TRANSACTION_onRecordEvent = 2;
        static final int TRANSACTION_onRecordStateChanged = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ICallRecorderCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICallRecorderCallback)) {
                return new Proxy(obj);
            }
            return (ICallRecorderCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onRecordStateChanged(data.readInt());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onRecordEvent(data.readInt(), data.readString());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements ICallRecorderCallback {
            public static ICallRecorderCallback sDefaultImpl;
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

            @Override // com.mediatek.internal.telecom.ICallRecorderCallback
            public void onRecordStateChanged(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onRecordStateChanged(state);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telecom.ICallRecorderCallback
            public void onRecordEvent(int eventId, String eventContent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(eventId);
                    _data.writeString(eventContent);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onRecordEvent(eventId, eventContent);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ICallRecorderCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ICallRecorderCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
