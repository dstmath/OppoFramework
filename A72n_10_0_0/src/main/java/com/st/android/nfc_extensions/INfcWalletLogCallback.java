package com.st.android.nfc_extensions;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface INfcWalletLogCallback extends IInterface {
    void onFwLogReceived(int i, byte[] bArr) throws RemoteException;

    public static class Default implements INfcWalletLogCallback {
        @Override // com.st.android.nfc_extensions.INfcWalletLogCallback
        public void onFwLogReceived(int logtype, byte[] data) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INfcWalletLogCallback {
        private static final String DESCRIPTOR = "com.st.android.nfc_extensions.INfcWalletLogCallback";
        static final int TRANSACTION_onFwLogReceived = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INfcWalletLogCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INfcWalletLogCallback)) {
                return new Proxy(obj);
            }
            return (INfcWalletLogCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onFwLogReceived(data.readInt(), data.createByteArray());
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
        public static class Proxy implements INfcWalletLogCallback {
            public static INfcWalletLogCallback sDefaultImpl;
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

            @Override // com.st.android.nfc_extensions.INfcWalletLogCallback
            public void onFwLogReceived(int logtype, byte[] data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(logtype);
                    _data.writeByteArray(data);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onFwLogReceived(logtype, data);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(INfcWalletLogCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INfcWalletLogCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
