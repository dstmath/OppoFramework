package com.mediatek.gnssdebugreport;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.mediatek.gnssdebugreport.IDebugReportCallback;

public interface IGnssDebugReportService extends IInterface {
    void addListener(IDebugReportCallback iDebugReportCallback) throws RemoteException;

    void removeListener(IDebugReportCallback iDebugReportCallback) throws RemoteException;

    boolean startDebug(IDebugReportCallback iDebugReportCallback) throws RemoteException;

    boolean stopDebug(IDebugReportCallback iDebugReportCallback) throws RemoteException;

    public static class Default implements IGnssDebugReportService {
        @Override // com.mediatek.gnssdebugreport.IGnssDebugReportService
        public boolean startDebug(IDebugReportCallback callback) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.gnssdebugreport.IGnssDebugReportService
        public boolean stopDebug(IDebugReportCallback callback) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.gnssdebugreport.IGnssDebugReportService
        public void addListener(IDebugReportCallback callback) throws RemoteException {
        }

        @Override // com.mediatek.gnssdebugreport.IGnssDebugReportService
        public void removeListener(IDebugReportCallback callback) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IGnssDebugReportService {
        private static final String DESCRIPTOR = "com.mediatek.gnssdebugreport.IGnssDebugReportService";
        static final int TRANSACTION_addListener = 3;
        static final int TRANSACTION_removeListener = 4;
        static final int TRANSACTION_startDebug = 1;
        static final int TRANSACTION_stopDebug = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IGnssDebugReportService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGnssDebugReportService)) {
                return new Proxy(obj);
            }
            return (IGnssDebugReportService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                boolean startDebug = startDebug(IDebugReportCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(startDebug ? 1 : 0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                boolean stopDebug = stopDebug(IDebugReportCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(stopDebug ? 1 : 0);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                addListener(IDebugReportCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                removeListener(IDebugReportCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IGnssDebugReportService {
            public static IGnssDebugReportService sDefaultImpl;
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

            @Override // com.mediatek.gnssdebugreport.IGnssDebugReportService
            public boolean startDebug(IDebugReportCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startDebug(callback);
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

            @Override // com.mediatek.gnssdebugreport.IGnssDebugReportService
            public boolean stopDebug(IDebugReportCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().stopDebug(callback);
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

            @Override // com.mediatek.gnssdebugreport.IGnssDebugReportService
            public void addListener(IDebugReportCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addListener(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.gnssdebugreport.IGnssDebugReportService
            public void removeListener(IDebugReportCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeListener(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IGnssDebugReportService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IGnssDebugReportService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
