package com.mediatek.capctrl.aidl;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IMtkCapCtrl extends IInterface {
    int abortCertificate() throws RemoteException;

    int enableCapabaility(String str, int i) throws RemoteException;

    AuthResponse routeAuthMessage(byte[] bArr) throws RemoteException;

    CertResponse routeCertificate(byte[] bArr, byte[] bArr2) throws RemoteException;

    public static class Default implements IMtkCapCtrl {
        @Override // com.mediatek.capctrl.aidl.IMtkCapCtrl
        public CertResponse routeCertificate(byte[] cert, byte[] msg) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.capctrl.aidl.IMtkCapCtrl
        public AuthResponse routeAuthMessage(byte[] msg) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.capctrl.aidl.IMtkCapCtrl
        public int enableCapabaility(String id, int toActive) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.capctrl.aidl.IMtkCapCtrl
        public int abortCertificate() throws RemoteException {
            return 0;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMtkCapCtrl {
        private static final String DESCRIPTOR = "com.mediatek.capctrl.aidl.IMtkCapCtrl";
        static final int TRANSACTION_abortCertificate = 4;
        static final int TRANSACTION_enableCapabaility = 3;
        static final int TRANSACTION_routeAuthMessage = 2;
        static final int TRANSACTION_routeCertificate = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMtkCapCtrl asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMtkCapCtrl)) {
                return new Proxy(obj);
            }
            return (IMtkCapCtrl) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == TRANSACTION_routeCertificate) {
                data.enforceInterface(DESCRIPTOR);
                CertResponse _result = routeCertificate(data.createByteArray(), data.createByteArray());
                reply.writeNoException();
                if (_result != null) {
                    reply.writeInt(TRANSACTION_routeCertificate);
                    _result.writeToParcel(reply, TRANSACTION_routeCertificate);
                } else {
                    reply.writeInt(0);
                }
                return true;
            } else if (code == TRANSACTION_routeAuthMessage) {
                data.enforceInterface(DESCRIPTOR);
                AuthResponse _result2 = routeAuthMessage(data.createByteArray());
                reply.writeNoException();
                if (_result2 != null) {
                    reply.writeInt(TRANSACTION_routeCertificate);
                    _result2.writeToParcel(reply, TRANSACTION_routeCertificate);
                } else {
                    reply.writeInt(0);
                }
                return true;
            } else if (code == TRANSACTION_enableCapabaility) {
                data.enforceInterface(DESCRIPTOR);
                int _result3 = enableCapabaility(data.readString(), data.readInt());
                reply.writeNoException();
                reply.writeInt(_result3);
                return true;
            } else if (code == TRANSACTION_abortCertificate) {
                data.enforceInterface(DESCRIPTOR);
                int _result4 = abortCertificate();
                reply.writeNoException();
                reply.writeInt(_result4);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IMtkCapCtrl {
            public static IMtkCapCtrl sDefaultImpl;
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

            @Override // com.mediatek.capctrl.aidl.IMtkCapCtrl
            public CertResponse routeCertificate(byte[] cert, byte[] msg) throws RemoteException {
                CertResponse _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(cert);
                    _data.writeByteArray(msg);
                    if (!this.mRemote.transact(Stub.TRANSACTION_routeCertificate, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().routeCertificate(cert, msg);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = CertResponse.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.capctrl.aidl.IMtkCapCtrl
            public AuthResponse routeAuthMessage(byte[] msg) throws RemoteException {
                AuthResponse _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(msg);
                    if (!this.mRemote.transact(Stub.TRANSACTION_routeAuthMessage, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().routeAuthMessage(msg);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = AuthResponse.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.capctrl.aidl.IMtkCapCtrl
            public int enableCapabaility(String id, int toActive) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(id);
                    _data.writeInt(toActive);
                    if (!this.mRemote.transact(Stub.TRANSACTION_enableCapabaility, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().enableCapabaility(id, toActive);
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

            @Override // com.mediatek.capctrl.aidl.IMtkCapCtrl
            public int abortCertificate() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_abortCertificate, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().abortCertificate();
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

        public static boolean setDefaultImpl(IMtkCapCtrl impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMtkCapCtrl getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
