package com.qti.izat;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.qti.debugreport.IDebugReportService;
import com.qti.flp.IFlpService;
import com.qti.flp.ITestService;
import com.qti.geofence.IGeofenceService;
import com.qti.wifidbreceiver.IWiFiDBReceiver;

public interface IIzatService extends IInterface {

    public static abstract class Stub extends Binder implements IIzatService {
        private static final String DESCRIPTOR = "com.qti.izat.IIzatService";
        static final int TRANSACTION_getDebugReportService = 5;
        static final int TRANSACTION_getFlpService = 1;
        static final int TRANSACTION_getGeofenceService = 3;
        static final int TRANSACTION_getTestService = 2;
        static final int TRANSACTION_getVersion = 4;
        static final int TRANSACTION_getWiFiDBReceiver = 6;

        private static class Proxy implements IIzatService {
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

            public IFlpService getFlpService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    IFlpService _result = com.qti.flp.IFlpService.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ITestService getTestService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    ITestService _result = com.qti.flp.ITestService.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IGeofenceService getGeofenceService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    IGeofenceService _result = com.qti.geofence.IGeofenceService.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IDebugReportService getDebugReportService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    IDebugReportService _result = com.qti.debugreport.IDebugReportService.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IWiFiDBReceiver getWiFiDBReceiver() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getWiFiDBReceiver, _data, _reply, 0);
                    _reply.readException();
                    IWiFiDBReceiver _result = com.qti.wifidbreceiver.IWiFiDBReceiver.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IIzatService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IIzatService)) {
                return new Proxy(obj);
            }
            return (IIzatService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            IBinder iBinder = null;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    IFlpService _result = getFlpService();
                    reply.writeNoException();
                    if (_result != null) {
                        iBinder = _result.asBinder();
                    }
                    reply.writeStrongBinder(iBinder);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    ITestService _result2 = getTestService();
                    reply.writeNoException();
                    if (_result2 != null) {
                        iBinder = _result2.asBinder();
                    }
                    reply.writeStrongBinder(iBinder);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    IGeofenceService _result3 = getGeofenceService();
                    reply.writeNoException();
                    if (_result3 != null) {
                        iBinder = _result3.asBinder();
                    }
                    reply.writeStrongBinder(iBinder);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    String _result4 = getVersion();
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    IDebugReportService _result5 = getDebugReportService();
                    reply.writeNoException();
                    if (_result5 != null) {
                        iBinder = _result5.asBinder();
                    }
                    reply.writeStrongBinder(iBinder);
                    return true;
                case TRANSACTION_getWiFiDBReceiver /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    IWiFiDBReceiver _result6 = getWiFiDBReceiver();
                    reply.writeNoException();
                    if (_result6 != null) {
                        iBinder = _result6.asBinder();
                    }
                    reply.writeStrongBinder(iBinder);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    IDebugReportService getDebugReportService() throws RemoteException;

    IFlpService getFlpService() throws RemoteException;

    IGeofenceService getGeofenceService() throws RemoteException;

    ITestService getTestService() throws RemoteException;

    String getVersion() throws RemoteException;

    IWiFiDBReceiver getWiFiDBReceiver() throws RemoteException;
}
