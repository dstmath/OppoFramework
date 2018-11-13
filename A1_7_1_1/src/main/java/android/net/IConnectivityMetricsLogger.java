package android.net;

import android.app.PendingIntent;
import android.net.ConnectivityMetricsEvent.Reference;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IConnectivityMetricsLogger extends IInterface {

    public static abstract class Stub extends Binder implements IConnectivityMetricsLogger {
        private static final String DESCRIPTOR = "android.net.IConnectivityMetricsLogger";
        static final int TRANSACTION_getEvents = 3;
        static final int TRANSACTION_logEvent = 1;
        static final int TRANSACTION_logEvents = 2;
        static final int TRANSACTION_register = 4;
        static final int TRANSACTION_unregister = 5;

        private static class Proxy implements IConnectivityMetricsLogger {
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

            public long logEvent(ConnectivityMetricsEvent event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (event != null) {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long logEvents(ConnectivityMetricsEvent[] events) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedArray(events, 0);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ConnectivityMetricsEvent[] getEvents(Reference reference) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (reference != null) {
                        _data.writeInt(1);
                        reference.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    ConnectivityMetricsEvent[] _result = (ConnectivityMetricsEvent[]) _reply.createTypedArray(ConnectivityMetricsEvent.CREATOR);
                    if (_reply.readInt() != 0) {
                        reference.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean register(PendingIntent newEventsIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (newEventsIntent != null) {
                        _data.writeInt(1);
                        newEventsIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregister(PendingIntent newEventsIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (newEventsIntent != null) {
                        _data.writeInt(1);
                        newEventsIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(5, _data, _reply, 0);
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

        public static IConnectivityMetricsLogger asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IConnectivityMetricsLogger)) {
                return new Proxy(obj);
            }
            return (IConnectivityMetricsLogger) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = 0;
            long _result;
            PendingIntent _arg0;
            switch (code) {
                case 1:
                    ConnectivityMetricsEvent _arg02;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg02 = (ConnectivityMetricsEvent) ConnectivityMetricsEvent.CREATOR.createFromParcel(data);
                    } else {
                        _arg02 = null;
                    }
                    _result = logEvent(_arg02);
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    _result = logEvents((ConnectivityMetricsEvent[]) data.createTypedArray(ConnectivityMetricsEvent.CREATOR));
                    reply.writeNoException();
                    reply.writeLong(_result);
                    return true;
                case 3:
                    Reference _arg03;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg03 = (Reference) Reference.CREATOR.createFromParcel(data);
                    } else {
                        _arg03 = null;
                    }
                    ConnectivityMetricsEvent[] _result2 = getEvents(_arg03);
                    reply.writeNoException();
                    reply.writeTypedArray(_result2, 1);
                    if (_arg03 != null) {
                        reply.writeInt(1);
                        _arg03.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    boolean _result3 = register(_arg0);
                    reply.writeNoException();
                    if (_result3) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    unregister(_arg0);
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    ConnectivityMetricsEvent[] getEvents(Reference reference) throws RemoteException;

    long logEvent(ConnectivityMetricsEvent connectivityMetricsEvent) throws RemoteException;

    long logEvents(ConnectivityMetricsEvent[] connectivityMetricsEventArr) throws RemoteException;

    boolean register(PendingIntent pendingIntent) throws RemoteException;

    void unregister(PendingIntent pendingIntent) throws RemoteException;
}
