package com.qti.geofence;

import android.app.PendingIntent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IGeofenceService extends IInterface {

    public static abstract class Stub extends Binder implements IGeofenceService {
        private static final String DESCRIPTOR = "com.qti.geofence.IGeofenceService";
        static final int TRANSACTION_addGeofence = 3;
        static final int TRANSACTION_addGeofenceObj = 8;
        static final int TRANSACTION_pauseGeofence = 6;
        static final int TRANSACTION_recoverGeofences = 11;
        static final int TRANSACTION_registerCallback = 1;
        static final int TRANSACTION_registerPendingIntent = 9;
        static final int TRANSACTION_removeGeofence = 4;
        static final int TRANSACTION_resumeGeofence = 7;
        static final int TRANSACTION_unregisterCallback = 2;
        static final int TRANSACTION_unregisterPendingIntent = 10;
        static final int TRANSACTION_updateGeofence = 5;

        private static class Proxy implements IGeofenceService {
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

            public void registerCallback(IGeofenceCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterCallback(IGeofenceCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int addGeofence(double latitude, double longitude, double radius, int transitionTypes, int responsiveness, int confidence, int dwellTime, int dwellTimeMask) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeDouble(latitude);
                    _data.writeDouble(longitude);
                    _data.writeDouble(radius);
                    _data.writeInt(transitionTypes);
                    _data.writeInt(responsiveness);
                    _data.writeInt(confidence);
                    _data.writeInt(dwellTime);
                    _data.writeInt(dwellTimeMask);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeGeofence(int geofenceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(geofenceId);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateGeofence(int geofenceId, int transitionTypes, int notifyResponsiveness) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(geofenceId);
                    _data.writeInt(transitionTypes);
                    _data.writeInt(notifyResponsiveness);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void pauseGeofence(int geofenceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(geofenceId);
                    this.mRemote.transact(Stub.TRANSACTION_pauseGeofence, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void resumeGeofence(int geofenceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(geofenceId);
                    this.mRemote.transact(Stub.TRANSACTION_resumeGeofence, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int addGeofenceObj(GeofenceData gfData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (gfData != null) {
                        _data.writeInt(1);
                        gfData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerPendingIntent(PendingIntent notifyIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (notifyIntent != null) {
                        _data.writeInt(1);
                        notifyIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_registerPendingIntent, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterPendingIntent(PendingIntent notifyIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (notifyIntent != null) {
                        _data.writeInt(1);
                        notifyIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_unregisterPendingIntent, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void recoverGeofences(List<GeofenceData> gfList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(gfList);
                    this.mRemote.transact(Stub.TRANSACTION_recoverGeofences, _data, _reply, 0);
                    _reply.readException();
                    _reply.readTypedList(gfList, GeofenceData.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IGeofenceService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGeofenceService)) {
                return new Proxy(obj);
            }
            return (IGeofenceService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            PendingIntent _arg0;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    registerCallback(com.qti.geofence.IGeofenceCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    unregisterCallback(com.qti.geofence.IGeofenceCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    _result = addGeofence(data.readDouble(), data.readDouble(), data.readDouble(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    removeGeofence(data.readInt());
                    reply.writeNoException();
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    updateGeofence(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_pauseGeofence /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    pauseGeofence(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_resumeGeofence /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    resumeGeofence(data.readInt());
                    reply.writeNoException();
                    return true;
                case 8:
                    GeofenceData _arg02;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg02 = (GeofenceData) GeofenceData.CREATOR.createFromParcel(data);
                    } else {
                        _arg02 = null;
                    }
                    _result = addGeofenceObj(_arg02);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_registerPendingIntent /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    registerPendingIntent(_arg0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_unregisterPendingIntent /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    unregisterPendingIntent(_arg0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_recoverGeofences /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<GeofenceData> _arg03 = data.createTypedArrayList(GeofenceData.CREATOR);
                    recoverGeofences(_arg03);
                    reply.writeNoException();
                    reply.writeTypedList(_arg03);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int addGeofence(double d, double d2, double d3, int i, int i2, int i3, int i4, int i5) throws RemoteException;

    int addGeofenceObj(GeofenceData geofenceData) throws RemoteException;

    void pauseGeofence(int i) throws RemoteException;

    void recoverGeofences(List<GeofenceData> list) throws RemoteException;

    void registerCallback(IGeofenceCallback iGeofenceCallback) throws RemoteException;

    void registerPendingIntent(PendingIntent pendingIntent) throws RemoteException;

    void removeGeofence(int i) throws RemoteException;

    void resumeGeofence(int i) throws RemoteException;

    void unregisterCallback(IGeofenceCallback iGeofenceCallback) throws RemoteException;

    void unregisterPendingIntent(PendingIntent pendingIntent) throws RemoteException;

    void updateGeofence(int i, int i2, int i3) throws RemoteException;
}
