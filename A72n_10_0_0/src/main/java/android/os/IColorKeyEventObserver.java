package android.os;

import android.view.KeyEvent;

public interface IColorKeyEventObserver extends IInterface {
    void onKeyEvent(KeyEvent keyEvent) throws RemoteException;

    public static class Default implements IColorKeyEventObserver {
        @Override // android.os.IColorKeyEventObserver
        public void onKeyEvent(KeyEvent info) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IColorKeyEventObserver {
        private static final String DESCRIPTOR = "android.os.IColorKeyEventObserver";
        static final int TRANSACTION_onKeyEvent = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IColorKeyEventObserver asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IColorKeyEventObserver)) {
                return new Proxy(obj);
            }
            return (IColorKeyEventObserver) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "onKeyEvent";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            KeyEvent _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = KeyEvent.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onKeyEvent(_arg0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IColorKeyEventObserver {
            public static IColorKeyEventObserver sDefaultImpl;
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

            @Override // android.os.IColorKeyEventObserver
            public void onKeyEvent(KeyEvent info) throws RemoteException {
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
                        Stub.getDefaultImpl().onKeyEvent(info);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IColorKeyEventObserver impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IColorKeyEventObserver getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
