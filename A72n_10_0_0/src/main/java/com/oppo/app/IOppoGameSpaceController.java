package com.oppo.app;

import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IOppoGameSpaceController extends IInterface {
    void dispatchGameDock(Bundle bundle) throws RemoteException;

    void gameExiting(String str) throws RemoteException;

    void gameStarting(Intent intent, String str, boolean z) throws RemoteException;

    boolean isGameDockAllowed() throws RemoteException;

    void videoStarting(Intent intent, String str) throws RemoteException;

    public static class Default implements IOppoGameSpaceController {
        @Override // com.oppo.app.IOppoGameSpaceController
        public void gameStarting(Intent intent, String pkg, boolean isResume) throws RemoteException {
        }

        @Override // com.oppo.app.IOppoGameSpaceController
        public void gameExiting(String pkg) throws RemoteException {
        }

        @Override // com.oppo.app.IOppoGameSpaceController
        public void videoStarting(Intent intent, String pkg) throws RemoteException {
        }

        @Override // com.oppo.app.IOppoGameSpaceController
        public void dispatchGameDock(Bundle bundle) throws RemoteException {
        }

        @Override // com.oppo.app.IOppoGameSpaceController
        public boolean isGameDockAllowed() throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOppoGameSpaceController {
        private static final String DESCRIPTOR = "com.oppo.app.IOppoGameSpaceController";
        static final int TRANSACTION_dispatchGameDock = 4;
        static final int TRANSACTION_gameExiting = 2;
        static final int TRANSACTION_gameStarting = 1;
        static final int TRANSACTION_isGameDockAllowed = 5;
        static final int TRANSACTION_videoStarting = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOppoGameSpaceController asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOppoGameSpaceController)) {
                return new Proxy(obj);
            }
            return (IOppoGameSpaceController) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "gameStarting";
            }
            if (transactionCode == 2) {
                return "gameExiting";
            }
            if (transactionCode == 3) {
                return "videoStarting";
            }
            if (transactionCode == 4) {
                return "dispatchGameDock";
            }
            if (transactionCode != 5) {
                return null;
            }
            return "isGameDockAllowed";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Intent _arg0;
            Intent _arg02;
            Bundle _arg03;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = Intent.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                gameStarting(_arg0, data.readString(), data.readInt() != 0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                gameExiting(data.readString());
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = Intent.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                videoStarting(_arg02, data.readString());
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg03 = Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg03 = null;
                }
                dispatchGameDock(_arg03);
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                boolean isGameDockAllowed = isGameDockAllowed();
                reply.writeNoException();
                reply.writeInt(isGameDockAllowed ? 1 : 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IOppoGameSpaceController {
            public static IOppoGameSpaceController sDefaultImpl;
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

            @Override // com.oppo.app.IOppoGameSpaceController
            public void gameStarting(Intent intent, String pkg, boolean isResume) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 0;
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(pkg);
                    if (isResume) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().gameStarting(intent, pkg, isResume);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.oppo.app.IOppoGameSpaceController
            public void gameExiting(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().gameExiting(pkg);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.oppo.app.IOppoGameSpaceController
            public void videoStarting(Intent intent, String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(pkg);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().videoStarting(intent, pkg);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.oppo.app.IOppoGameSpaceController
            public void dispatchGameDock(Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().dispatchGameDock(bundle);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.oppo.app.IOppoGameSpaceController
            public boolean isGameDockAllowed() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isGameDockAllowed();
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

        public static boolean setDefaultImpl(IOppoGameSpaceController impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOppoGameSpaceController getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
