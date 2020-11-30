package com.color.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IColorAppSwitchObserver extends IInterface {
    void onActivityEnter(ColorAppEnterInfo colorAppEnterInfo) throws RemoteException;

    void onActivityExit(ColorAppExitInfo colorAppExitInfo) throws RemoteException;

    void onAppEnter(ColorAppEnterInfo colorAppEnterInfo) throws RemoteException;

    void onAppExit(ColorAppExitInfo colorAppExitInfo) throws RemoteException;

    public static class Default implements IColorAppSwitchObserver {
        @Override // com.color.app.IColorAppSwitchObserver
        public void onAppEnter(ColorAppEnterInfo info) throws RemoteException {
        }

        @Override // com.color.app.IColorAppSwitchObserver
        public void onAppExit(ColorAppExitInfo info) throws RemoteException {
        }

        @Override // com.color.app.IColorAppSwitchObserver
        public void onActivityEnter(ColorAppEnterInfo info) throws RemoteException {
        }

        @Override // com.color.app.IColorAppSwitchObserver
        public void onActivityExit(ColorAppExitInfo info) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IColorAppSwitchObserver {
        private static final String DESCRIPTOR = "com.color.app.IColorAppSwitchObserver";
        static final int TRANSACTION_onActivityEnter = 3;
        static final int TRANSACTION_onActivityExit = 4;
        static final int TRANSACTION_onAppEnter = 1;
        static final int TRANSACTION_onAppExit = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IColorAppSwitchObserver asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IColorAppSwitchObserver)) {
                return new Proxy(obj);
            }
            return (IColorAppSwitchObserver) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onAppEnter";
            }
            if (transactionCode == 2) {
                return "onAppExit";
            }
            if (transactionCode == 3) {
                return "onActivityEnter";
            }
            if (transactionCode != 4) {
                return null;
            }
            return "onActivityExit";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ColorAppEnterInfo _arg0;
            ColorAppExitInfo _arg02;
            ColorAppEnterInfo _arg03;
            ColorAppExitInfo _arg04;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = ColorAppEnterInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onAppEnter(_arg0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = ColorAppExitInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                onAppExit(_arg02);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg03 = ColorAppEnterInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg03 = null;
                }
                onActivityEnter(_arg03);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg04 = ColorAppExitInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg04 = null;
                }
                onActivityExit(_arg04);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IColorAppSwitchObserver {
            public static IColorAppSwitchObserver sDefaultImpl;
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

            @Override // com.color.app.IColorAppSwitchObserver
            public void onAppEnter(ColorAppEnterInfo info) throws RemoteException {
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
                        Stub.getDefaultImpl().onAppEnter(info);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.color.app.IColorAppSwitchObserver
            public void onAppExit(ColorAppExitInfo info) throws RemoteException {
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
                        Stub.getDefaultImpl().onAppExit(info);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.color.app.IColorAppSwitchObserver
            public void onActivityEnter(ColorAppEnterInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onActivityEnter(info);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.color.app.IColorAppSwitchObserver
            public void onActivityExit(ColorAppExitInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onActivityExit(info);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IColorAppSwitchObserver impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IColorAppSwitchObserver getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
