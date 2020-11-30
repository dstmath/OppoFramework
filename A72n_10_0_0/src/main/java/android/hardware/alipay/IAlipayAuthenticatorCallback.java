package android.hardware.alipay;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAlipayAuthenticatorCallback extends IInterface {
    void onAuthenticationError(int i) throws RemoteException;

    void onAuthenticationFailed(int i) throws RemoteException;

    void onAuthenticationStatus(int i) throws RemoteException;

    void onAuthenticationSucceeded() throws RemoteException;

    public static class Default implements IAlipayAuthenticatorCallback {
        @Override // android.hardware.alipay.IAlipayAuthenticatorCallback
        public void onAuthenticationError(int errorCode) throws RemoteException {
        }

        @Override // android.hardware.alipay.IAlipayAuthenticatorCallback
        public void onAuthenticationStatus(int status) throws RemoteException {
        }

        @Override // android.hardware.alipay.IAlipayAuthenticatorCallback
        public void onAuthenticationSucceeded() throws RemoteException {
        }

        @Override // android.hardware.alipay.IAlipayAuthenticatorCallback
        public void onAuthenticationFailed(int errorCode) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IAlipayAuthenticatorCallback {
        private static final String DESCRIPTOR = "android.hardware.alipay.IAlipayAuthenticatorCallback";
        static final int TRANSACTION_onAuthenticationError = 1;
        static final int TRANSACTION_onAuthenticationFailed = 4;
        static final int TRANSACTION_onAuthenticationStatus = 2;
        static final int TRANSACTION_onAuthenticationSucceeded = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAlipayAuthenticatorCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAlipayAuthenticatorCallback)) {
                return new Proxy(obj);
            }
            return (IAlipayAuthenticatorCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onAuthenticationError";
            }
            if (transactionCode == 2) {
                return "onAuthenticationStatus";
            }
            if (transactionCode == 3) {
                return "onAuthenticationSucceeded";
            }
            if (transactionCode != 4) {
                return null;
            }
            return "onAuthenticationFailed";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onAuthenticationError(data.readInt());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onAuthenticationStatus(data.readInt());
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onAuthenticationSucceeded();
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                onAuthenticationFailed(data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IAlipayAuthenticatorCallback {
            public static IAlipayAuthenticatorCallback sDefaultImpl;
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

            @Override // android.hardware.alipay.IAlipayAuthenticatorCallback
            public void onAuthenticationError(int errorCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(errorCode);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onAuthenticationError(errorCode);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.alipay.IAlipayAuthenticatorCallback
            public void onAuthenticationStatus(int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onAuthenticationStatus(status);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.alipay.IAlipayAuthenticatorCallback
            public void onAuthenticationSucceeded() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onAuthenticationSucceeded();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.alipay.IAlipayAuthenticatorCallback
            public void onAuthenticationFailed(int errorCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(errorCode);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onAuthenticationFailed(errorCode);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IAlipayAuthenticatorCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IAlipayAuthenticatorCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
