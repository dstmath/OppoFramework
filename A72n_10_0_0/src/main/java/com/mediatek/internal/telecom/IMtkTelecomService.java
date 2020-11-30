package com.mediatek.internal.telecom;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import java.util.List;

public interface IMtkTelecomService extends IInterface {
    List<PhoneAccountHandle> getAllPhoneAccountHandlesIncludingVirtual() throws RemoteException;

    List<PhoneAccount> getAllPhoneAccountsIncludingVirtual() throws RemoteException;

    boolean isInCall(String str) throws RemoteException;

    boolean isInVideoCall(String str) throws RemoteException;

    boolean isInVolteCall(String str) throws RemoteException;

    public static class Default implements IMtkTelecomService {
        @Override // com.mediatek.internal.telecom.IMtkTelecomService
        public boolean isInVideoCall(String callingPackage) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telecom.IMtkTelecomService
        public boolean isInVolteCall(String callingPackage) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telecom.IMtkTelecomService
        public List<PhoneAccount> getAllPhoneAccountsIncludingVirtual() throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telecom.IMtkTelecomService
        public List<PhoneAccountHandle> getAllPhoneAccountHandlesIncludingVirtual() throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telecom.IMtkTelecomService
        public boolean isInCall(String callingPackage) throws RemoteException {
            return false;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMtkTelecomService {
        private static final String DESCRIPTOR = "com.mediatek.internal.telecom.IMtkTelecomService";
        static final int TRANSACTION_getAllPhoneAccountHandlesIncludingVirtual = 4;
        static final int TRANSACTION_getAllPhoneAccountsIncludingVirtual = 3;
        static final int TRANSACTION_isInCall = 5;
        static final int TRANSACTION_isInVideoCall = 1;
        static final int TRANSACTION_isInVolteCall = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMtkTelecomService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMtkTelecomService)) {
                return new Proxy(obj);
            }
            return (IMtkTelecomService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                boolean isInVideoCall = isInVideoCall(data.readString());
                reply.writeNoException();
                reply.writeInt(isInVideoCall ? 1 : 0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                boolean isInVolteCall = isInVolteCall(data.readString());
                reply.writeNoException();
                reply.writeInt(isInVolteCall ? 1 : 0);
                return true;
            } else if (code == TRANSACTION_getAllPhoneAccountsIncludingVirtual) {
                data.enforceInterface(DESCRIPTOR);
                List<PhoneAccount> _result = getAllPhoneAccountsIncludingVirtual();
                reply.writeNoException();
                reply.writeTypedList(_result);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                List<PhoneAccountHandle> _result2 = getAllPhoneAccountHandlesIncludingVirtual();
                reply.writeNoException();
                reply.writeTypedList(_result2);
                return true;
            } else if (code == TRANSACTION_isInCall) {
                data.enforceInterface(DESCRIPTOR);
                boolean isInCall = isInCall(data.readString());
                reply.writeNoException();
                reply.writeInt(isInCall ? 1 : 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IMtkTelecomService {
            public static IMtkTelecomService sDefaultImpl;
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

            @Override // com.mediatek.internal.telecom.IMtkTelecomService
            public boolean isInVideoCall(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isInVideoCall(callingPackage);
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

            @Override // com.mediatek.internal.telecom.IMtkTelecomService
            public boolean isInVolteCall(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isInVolteCall(callingPackage);
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

            @Override // com.mediatek.internal.telecom.IMtkTelecomService
            public List<PhoneAccount> getAllPhoneAccountsIncludingVirtual() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getAllPhoneAccountsIncludingVirtual, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAllPhoneAccountsIncludingVirtual();
                    }
                    _reply.readException();
                    List<PhoneAccount> _result = _reply.createTypedArrayList(PhoneAccount.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telecom.IMtkTelecomService
            public List<PhoneAccountHandle> getAllPhoneAccountHandlesIncludingVirtual() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAllPhoneAccountHandlesIncludingVirtual();
                    }
                    _reply.readException();
                    List<PhoneAccountHandle> _result = _reply.createTypedArrayList(PhoneAccountHandle.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telecom.IMtkTelecomService
            public boolean isInCall(String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_isInCall, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isInCall(callingPackage);
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

        public static boolean setDefaultImpl(IMtkTelecomService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMtkTelecomService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
