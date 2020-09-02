package com.android.internal.policy;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IColorOSKeyguardService extends IInterface {
    void onKeyguardDoneForColorOS(boolean z) throws RemoteException;

    void sendCommandToApps(String str) throws RemoteException;

    void setNotificationListener(boolean z) throws RemoteException;

    public static class Default implements IColorOSKeyguardService {
        @Override // com.android.internal.policy.IColorOSKeyguardService
        public void onKeyguardDoneForColorOS(boolean keyguardDone) throws RemoteException {
        }

        @Override // com.android.internal.policy.IColorOSKeyguardService
        public void setNotificationListener(boolean isChanged) throws RemoteException {
        }

        @Override // com.android.internal.policy.IColorOSKeyguardService
        public void sendCommandToApps(String command) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IColorOSKeyguardService {
        private static final String DESCRIPTOR = "com.android.internal.policy.IColorOSKeyguardService";
        static final int TRANSACTION_onKeyguardDoneForColorOS = 1;
        static final int TRANSACTION_sendCommandToApps = 3;
        static final int TRANSACTION_setNotificationListener = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IColorOSKeyguardService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IColorOSKeyguardService)) {
                return new Proxy(obj);
            }
            return (IColorOSKeyguardService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onKeyguardDoneForColorOS";
            }
            if (transactionCode == 2) {
                return "setNotificationListener";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "sendCommandToApps";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _arg0 = false;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = true;
                }
                onKeyguardDoneForColorOS(_arg0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = true;
                }
                setNotificationListener(_arg0);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                sendCommandToApps(data.readString());
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IColorOSKeyguardService {
            public static IColorOSKeyguardService sDefaultImpl;
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

            @Override // com.android.internal.policy.IColorOSKeyguardService
            public void onKeyguardDoneForColorOS(boolean keyguardDone) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(keyguardDone ? 1 : 0);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onKeyguardDoneForColorOS(keyguardDone);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.policy.IColorOSKeyguardService
            public void setNotificationListener(boolean isChanged) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isChanged ? 1 : 0);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setNotificationListener(isChanged);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.policy.IColorOSKeyguardService
            public void sendCommandToApps(String command) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(command);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sendCommandToApps(command);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IColorOSKeyguardService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IColorOSKeyguardService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
