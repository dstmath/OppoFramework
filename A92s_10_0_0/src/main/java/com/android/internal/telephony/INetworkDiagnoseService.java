package com.android.internal.telephony;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface INetworkDiagnoseService extends IInterface {
    String getApConfigInfo() throws RemoteException;

    String getCellInfo() throws RemoteException;

    String getOtherInfo() throws RemoteException;

    String getServiceStateInfo() throws RemoteException;

    String getSignalInfo() throws RemoteException;

    public static class Default implements INetworkDiagnoseService {
        @Override // com.android.internal.telephony.INetworkDiagnoseService
        public String getApConfigInfo() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.INetworkDiagnoseService
        public String getServiceStateInfo() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.INetworkDiagnoseService
        public String getSignalInfo() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.INetworkDiagnoseService
        public String getCellInfo() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.INetworkDiagnoseService
        public String getOtherInfo() throws RemoteException {
            return null;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INetworkDiagnoseService {
        private static final String DESCRIPTOR = "com.android.internal.telephony.INetworkDiagnoseService";
        static final int TRANSACTION_getApConfigInfo = 1;
        static final int TRANSACTION_getCellInfo = 4;
        static final int TRANSACTION_getOtherInfo = 5;
        static final int TRANSACTION_getServiceStateInfo = 2;
        static final int TRANSACTION_getSignalInfo = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INetworkDiagnoseService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INetworkDiagnoseService)) {
                return new Proxy(obj);
            }
            return (INetworkDiagnoseService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                String _result = getApConfigInfo();
                reply.writeNoException();
                reply.writeString(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                String _result2 = getServiceStateInfo();
                reply.writeNoException();
                reply.writeString(_result2);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                String _result3 = getSignalInfo();
                reply.writeNoException();
                reply.writeString(_result3);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                String _result4 = getCellInfo();
                reply.writeNoException();
                reply.writeString(_result4);
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                String _result5 = getOtherInfo();
                reply.writeNoException();
                reply.writeString(_result5);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements INetworkDiagnoseService {
            public static INetworkDiagnoseService sDefaultImpl;
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

            @Override // com.android.internal.telephony.INetworkDiagnoseService
            public String getApConfigInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getApConfigInfo();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.INetworkDiagnoseService
            public String getServiceStateInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getServiceStateInfo();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.INetworkDiagnoseService
            public String getSignalInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSignalInfo();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.INetworkDiagnoseService
            public String getCellInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCellInfo();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.INetworkDiagnoseService
            public String getOtherInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getOtherInfo();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(INetworkDiagnoseService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INetworkDiagnoseService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
