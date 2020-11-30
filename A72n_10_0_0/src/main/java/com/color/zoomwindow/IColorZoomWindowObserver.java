package com.color.zoomwindow;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IColorZoomWindowObserver extends IInterface {
    void onInputMethodChanged(boolean z) throws RemoteException;

    void onZoomWindowDied(String str) throws RemoteException;

    void onZoomWindowHide(ColorZoomWindowInfo colorZoomWindowInfo) throws RemoteException;

    void onZoomWindowShow(ColorZoomWindowInfo colorZoomWindowInfo) throws RemoteException;

    public static class Default implements IColorZoomWindowObserver {
        @Override // com.color.zoomwindow.IColorZoomWindowObserver
        public void onZoomWindowShow(ColorZoomWindowInfo info) throws RemoteException {
        }

        @Override // com.color.zoomwindow.IColorZoomWindowObserver
        public void onZoomWindowHide(ColorZoomWindowInfo info) throws RemoteException {
        }

        @Override // com.color.zoomwindow.IColorZoomWindowObserver
        public void onZoomWindowDied(String appName) throws RemoteException {
        }

        @Override // com.color.zoomwindow.IColorZoomWindowObserver
        public void onInputMethodChanged(boolean isShown) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IColorZoomWindowObserver {
        private static final String DESCRIPTOR = "com.color.zoomwindow.IColorZoomWindowObserver";
        static final int TRANSACTION_onInputMethodChanged = 4;
        static final int TRANSACTION_onZoomWindowDied = 3;
        static final int TRANSACTION_onZoomWindowHide = 2;
        static final int TRANSACTION_onZoomWindowShow = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IColorZoomWindowObserver asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IColorZoomWindowObserver)) {
                return new Proxy(obj);
            }
            return (IColorZoomWindowObserver) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onZoomWindowShow";
            }
            if (transactionCode == 2) {
                return "onZoomWindowHide";
            }
            if (transactionCode == 3) {
                return "onZoomWindowDied";
            }
            if (transactionCode != 4) {
                return null;
            }
            return "onInputMethodChanged";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ColorZoomWindowInfo _arg0;
            ColorZoomWindowInfo _arg02;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = ColorZoomWindowInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onZoomWindowShow(_arg0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = ColorZoomWindowInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                onZoomWindowHide(_arg02);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onZoomWindowDied(data.readString());
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                onInputMethodChanged(data.readInt() != 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IColorZoomWindowObserver {
            public static IColorZoomWindowObserver sDefaultImpl;
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

            @Override // com.color.zoomwindow.IColorZoomWindowObserver
            public void onZoomWindowShow(ColorZoomWindowInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onZoomWindowShow(info);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.color.zoomwindow.IColorZoomWindowObserver
            public void onZoomWindowHide(ColorZoomWindowInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onZoomWindowHide(info);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.color.zoomwindow.IColorZoomWindowObserver
            public void onZoomWindowDied(String appName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(appName);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onZoomWindowDied(appName);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.color.zoomwindow.IColorZoomWindowObserver
            public void onInputMethodChanged(boolean isShown) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isShown ? 1 : 0);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onInputMethodChanged(isShown);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IColorZoomWindowObserver impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IColorZoomWindowObserver getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
