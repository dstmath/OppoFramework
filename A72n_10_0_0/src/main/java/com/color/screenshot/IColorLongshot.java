package com.color.screenshot;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.color.screenshot.IColorLongshotCallback;

public interface IColorLongshot extends IInterface {
    void notifyOverScroll(ColorLongshotEvent colorLongshotEvent) throws RemoteException;

    void start(IColorLongshotCallback iColorLongshotCallback) throws RemoteException;

    void stop() throws RemoteException;

    public static class Default implements IColorLongshot {
        @Override // com.color.screenshot.IColorLongshot
        public void start(IColorLongshotCallback callback) throws RemoteException {
        }

        @Override // com.color.screenshot.IColorLongshot
        public void stop() throws RemoteException {
        }

        @Override // com.color.screenshot.IColorLongshot
        public void notifyOverScroll(ColorLongshotEvent event) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IColorLongshot {
        private static final String DESCRIPTOR = "com.color.screenshot.IColorLongshot";
        static final int TRANSACTION_notifyOverScroll = 3;
        static final int TRANSACTION_start = 1;
        static final int TRANSACTION_stop = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IColorLongshot asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IColorLongshot)) {
                return new Proxy(obj);
            }
            return (IColorLongshot) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "start";
            }
            if (transactionCode == 2) {
                return "stop";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "notifyOverScroll";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ColorLongshotEvent _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                start(IColorLongshotCallback.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                stop();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = ColorLongshotEvent.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                notifyOverScroll(_arg0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IColorLongshot {
            public static IColorLongshot sDefaultImpl;
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

            @Override // com.color.screenshot.IColorLongshot
            public void start(IColorLongshotCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().start(callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.color.screenshot.IColorLongshot
            public void stop() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().stop();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.color.screenshot.IColorLongshot
            public void notifyOverScroll(ColorLongshotEvent event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (event != null) {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyOverScroll(event);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IColorLongshot impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IColorLongshot getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
