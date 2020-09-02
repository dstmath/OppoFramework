package android.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IColorStatusBar extends IInterface {
    void notifyMultiWindowFocusChanged(int i) throws RemoteException;

    void setStatusBarFunction(int i, String str) throws RemoteException;

    void toggleSplitScreen(int i) throws RemoteException;

    void topIsFullscreen(boolean z) throws RemoteException;

    void updateNavBarVisibility(int i) throws RemoteException;

    void updateNavBarVisibilityWithPkg(int i, String str) throws RemoteException;

    public static class Default implements IColorStatusBar {
        @Override // android.app.IColorStatusBar
        public void topIsFullscreen(boolean fullscreen) throws RemoteException {
        }

        @Override // android.app.IColorStatusBar
        public void notifyMultiWindowFocusChanged(int state) throws RemoteException {
        }

        @Override // android.app.IColorStatusBar
        public void toggleSplitScreen(int mode) throws RemoteException {
        }

        @Override // android.app.IColorStatusBar
        public void setStatusBarFunction(int functionCode, String pkgName) throws RemoteException {
        }

        @Override // android.app.IColorStatusBar
        public void updateNavBarVisibility(int navBarVis) throws RemoteException {
        }

        @Override // android.app.IColorStatusBar
        public void updateNavBarVisibilityWithPkg(int navBarVis, String pkgName) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IColorStatusBar {
        private static final String DESCRIPTOR = "android.app.IColorStatusBar";
        static final int TRANSACTION_notifyMultiWindowFocusChanged = 2;
        static final int TRANSACTION_setStatusBarFunction = 4;
        static final int TRANSACTION_toggleSplitScreen = 3;
        static final int TRANSACTION_topIsFullscreen = 1;
        static final int TRANSACTION_updateNavBarVisibility = 5;
        static final int TRANSACTION_updateNavBarVisibilityWithPkg = 6;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IColorStatusBar asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IColorStatusBar)) {
                return new Proxy(obj);
            }
            return (IColorStatusBar) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "topIsFullscreen";
                case 2:
                    return "notifyMultiWindowFocusChanged";
                case 3:
                    return "toggleSplitScreen";
                case 4:
                    return "setStatusBarFunction";
                case 5:
                    return "updateNavBarVisibility";
                case 6:
                    return "updateNavBarVisibilityWithPkg";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        topIsFullscreen(data.readInt() != 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        notifyMultiWindowFocusChanged(data.readInt());
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        toggleSplitScreen(data.readInt());
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        setStatusBarFunction(data.readInt(), data.readString());
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        updateNavBarVisibility(data.readInt());
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        updateNavBarVisibilityWithPkg(data.readInt(), data.readString());
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IColorStatusBar {
            public static IColorStatusBar sDefaultImpl;
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

            @Override // android.app.IColorStatusBar
            public void topIsFullscreen(boolean fullscreen) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fullscreen ? 1 : 0);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().topIsFullscreen(fullscreen);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IColorStatusBar
            public void notifyMultiWindowFocusChanged(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyMultiWindowFocusChanged(state);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IColorStatusBar
            public void toggleSplitScreen(int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().toggleSplitScreen(mode);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IColorStatusBar
            public void setStatusBarFunction(int functionCode, String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(functionCode);
                    _data.writeString(pkgName);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setStatusBarFunction(functionCode, pkgName);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IColorStatusBar
            public void updateNavBarVisibility(int navBarVis) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(navBarVis);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().updateNavBarVisibility(navBarVis);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.app.IColorStatusBar
            public void updateNavBarVisibilityWithPkg(int navBarVis, String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(navBarVis);
                    _data.writeString(pkgName);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().updateNavBarVisibilityWithPkg(navBarVis, pkgName);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IColorStatusBar impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IColorStatusBar getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
