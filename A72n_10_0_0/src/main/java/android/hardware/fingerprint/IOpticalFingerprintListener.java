package android.hardware.fingerprint;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IOpticalFingerprintListener extends IInterface {
    void onOpticalFingerprintUpdate(int i) throws RemoteException;

    public static class Default implements IOpticalFingerprintListener {
        @Override // android.hardware.fingerprint.IOpticalFingerprintListener
        public void onOpticalFingerprintUpdate(int status) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOpticalFingerprintListener {
        private static final String DESCRIPTOR = "android.hardware.fingerprint.IOpticalFingerprintListener";
        static final int TRANSACTION_onOpticalFingerprintUpdate = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOpticalFingerprintListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOpticalFingerprintListener)) {
                return new Proxy(obj);
            }
            return (IOpticalFingerprintListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "onOpticalFingerprintUpdate";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onOpticalFingerprintUpdate(data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IOpticalFingerprintListener {
            public static IOpticalFingerprintListener sDefaultImpl;
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

            @Override // android.hardware.fingerprint.IOpticalFingerprintListener
            public void onOpticalFingerprintUpdate(int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onOpticalFingerprintUpdate(status);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IOpticalFingerprintListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOpticalFingerprintListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
