package com.qti.geofence;

import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IGeofenceCallback extends IInterface {

    public static abstract class Stub extends Binder implements IGeofenceCallback {
        private static final String DESCRIPTOR = "com.qti.geofence.IGeofenceCallback";
        static final int TRANSACTION_onEngineReportStatus = 3;
        static final int TRANSACTION_onRequestResultReturned = 2;
        static final int TRANSACTION_onTransitionEvent = 1;

        private static class Proxy implements IGeofenceCallback {
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

            public void onTransitionEvent(int geofenceHwId, int event, Location location) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(geofenceHwId);
                    _data.writeInt(event);
                    if (location != null) {
                        _data.writeInt(1);
                        location.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onRequestResultReturned(int geofenceHwId, int requestType, int result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(geofenceHwId);
                    _data.writeInt(requestType);
                    _data.writeInt(result);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onEngineReportStatus(int status, Location location) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    if (location != null) {
                        _data.writeInt(1);
                        location.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IGeofenceCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGeofenceCallback)) {
                return new Proxy(obj);
            }
            return (IGeofenceCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _arg0;
            switch (code) {
                case 1:
                    Location _arg2;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    int _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg2 = (Location) Location.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    onTransitionEvent(_arg0, _arg1, _arg2);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    onRequestResultReturned(data.readInt(), data.readInt(), data.readInt());
                    return true;
                case 3:
                    Location _arg12;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg12 = (Location) Location.CREATOR.createFromParcel(data);
                    } else {
                        _arg12 = null;
                    }
                    onEngineReportStatus(_arg0, _arg12);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onEngineReportStatus(int i, Location location) throws RemoteException;

    void onRequestResultReturned(int i, int i2, int i3) throws RemoteException;

    void onTransitionEvent(int i, int i2, Location location) throws RemoteException;
}
