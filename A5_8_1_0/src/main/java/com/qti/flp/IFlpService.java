package com.qti.flp;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IFlpService extends IInterface {

    public static abstract class Stub extends Binder implements IFlpService {
        private static final String DESCRIPTOR = "com.qti.flp.IFlpService";
        static final int TRANSACTION_getAllSupportedFeatures = 3;
        static final int TRANSACTION_pullLocations = 7;
        static final int TRANSACTION_registerCallback = 1;
        static final int TRANSACTION_registerForSessionStatus = 8;
        static final int TRANSACTION_startFlpSession = 4;
        static final int TRANSACTION_stopFlpSession = 6;
        static final int TRANSACTION_unregisterCallback = 2;
        static final int TRANSACTION_unregisterForSessionStatus = 9;
        static final int TRANSACTION_updateFlpSession = 5;

        private static class Proxy implements IFlpService {
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

            public void registerCallback(int sessionType, int id, ILocationCallback cb, long sessionStartTime) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionType);
                    _data.writeInt(id);
                    if (cb != null) {
                        iBinder = cb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeLong(sessionStartTime);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterCallback(int sessionType, ILocationCallback cb) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sessionType);
                    if (cb != null) {
                        iBinder = cb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getAllSupportedFeatures() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int startFlpSession(int id, int flags, long period_ms, int distance_interval, long trip_distance) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(id);
                    _data.writeInt(flags);
                    _data.writeLong(period_ms);
                    _data.writeInt(distance_interval);
                    _data.writeLong(trip_distance);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int updateFlpSession(int id, int flags, long period_ms, int distance_interval, long trip_distance) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(id);
                    _data.writeInt(flags);
                    _data.writeLong(period_ms);
                    _data.writeInt(distance_interval);
                    _data.writeLong(trip_distance);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int stopFlpSession(int id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(id);
                    this.mRemote.transact(Stub.TRANSACTION_stopFlpSession, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int pullLocations(ILocationCallback cb, long sessionStartTime, int id) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (cb != null) {
                        iBinder = cb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeLong(sessionStartTime);
                    _data.writeInt(id);
                    this.mRemote.transact(Stub.TRANSACTION_pullLocations, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerForSessionStatus(int id, ISessionStatusCallback cb) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(id);
                    if (cb != null) {
                        iBinder = cb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterForSessionStatus(ISessionStatusCallback cb) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (cb != null) {
                        iBinder = cb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterForSessionStatus, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IFlpService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IFlpService)) {
                return new Proxy(obj);
            }
            return (IFlpService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            int _arg0;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    registerCallback(data.readInt(), data.readInt(), com.qti.flp.ILocationCallback.Stub.asInterface(data.readStrongBinder()), data.readLong());
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterCallback(data.readInt(), com.qti.flp.ILocationCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getAllSupportedFeatures();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _result = startFlpSession(_arg0, data.readInt(), data.readLong(), data.readInt(), data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _result = updateFlpSession(_arg0, data.readInt(), data.readLong(), data.readInt(), data.readLong());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_stopFlpSession /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = stopFlpSession(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_pullLocations /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = pullLocations(com.qti.flp.ILocationCallback.Stub.asInterface(data.readStrongBinder()), data.readLong(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    registerForSessionStatus(data.readInt(), com.qti.flp.ISessionStatusCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_unregisterForSessionStatus /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterForSessionStatus(com.qti.flp.ISessionStatusCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int getAllSupportedFeatures() throws RemoteException;

    int pullLocations(ILocationCallback iLocationCallback, long j, int i) throws RemoteException;

    void registerCallback(int i, int i2, ILocationCallback iLocationCallback, long j) throws RemoteException;

    void registerForSessionStatus(int i, ISessionStatusCallback iSessionStatusCallback) throws RemoteException;

    int startFlpSession(int i, int i2, long j, int i3, long j2) throws RemoteException;

    int stopFlpSession(int i) throws RemoteException;

    void unregisterCallback(int i, ILocationCallback iLocationCallback) throws RemoteException;

    void unregisterForSessionStatus(ISessionStatusCallback iSessionStatusCallback) throws RemoteException;

    int updateFlpSession(int i, int i2, long j, int i3, long j2) throws RemoteException;
}
