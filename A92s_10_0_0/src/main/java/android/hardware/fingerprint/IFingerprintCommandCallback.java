package android.hardware.fingerprint;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IFingerprintCommandCallback extends IInterface {
    void onFingerprintCmd(int i, byte[] bArr) throws RemoteException;

    public static class Default implements IFingerprintCommandCallback {
        @Override // android.hardware.fingerprint.IFingerprintCommandCallback
        public void onFingerprintCmd(int cmdId, byte[] result) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IFingerprintCommandCallback {
        private static final String DESCRIPTOR = "android.hardware.fingerprint.IFingerprintCommandCallback";
        static final int TRANSACTION_onFingerprintCmd = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IFingerprintCommandCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IFingerprintCommandCallback)) {
                return new Proxy(obj);
            }
            return (IFingerprintCommandCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "onFingerprintCmd";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onFingerprintCmd(data.readInt(), data.createByteArray());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IFingerprintCommandCallback {
            public static IFingerprintCommandCallback sDefaultImpl;
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

            @Override // android.hardware.fingerprint.IFingerprintCommandCallback
            public void onFingerprintCmd(int cmdId, byte[] result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cmdId);
                    _data.writeByteArray(result);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onFingerprintCmd(cmdId, result);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IFingerprintCommandCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IFingerprintCommandCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
