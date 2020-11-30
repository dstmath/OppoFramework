package com.color.screenshot;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.color.screenshot.IColorScreenshotCallback;

public interface IColorScreenshot extends IInterface {
    boolean isEdit() throws RemoteException;

    void start(IColorScreenshotCallback iColorScreenshotCallback) throws RemoteException;

    void stop() throws RemoteException;

    public static class Default implements IColorScreenshot {
        @Override // com.color.screenshot.IColorScreenshot
        public void start(IColorScreenshotCallback callback) throws RemoteException {
        }

        @Override // com.color.screenshot.IColorScreenshot
        public void stop() throws RemoteException {
        }

        @Override // com.color.screenshot.IColorScreenshot
        public boolean isEdit() throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IColorScreenshot {
        private static final String DESCRIPTOR = "com.color.screenshot.IColorScreenshot";
        static final int TRANSACTION_isEdit = 3;
        static final int TRANSACTION_start = 1;
        static final int TRANSACTION_stop = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IColorScreenshot asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IColorScreenshot)) {
                return new Proxy(obj);
            }
            return (IColorScreenshot) iin;
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
            return "isEdit";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                start(IColorScreenshotCallback.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                stop();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                boolean isEdit = isEdit();
                reply.writeNoException();
                reply.writeInt(isEdit ? 1 : 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IColorScreenshot {
            public static IColorScreenshot sDefaultImpl;
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

            @Override // com.color.screenshot.IColorScreenshot
            public void start(IColorScreenshotCallback callback) throws RemoteException {
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

            @Override // com.color.screenshot.IColorScreenshot
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

            @Override // com.color.screenshot.IColorScreenshot
            public boolean isEdit() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isEdit();
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

        public static boolean setDefaultImpl(IColorScreenshot impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IColorScreenshot getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
