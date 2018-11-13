package com.qti.wifidbreceiver;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IWiFiDBReceiverResponseListener extends IInterface {

    public static abstract class Stub extends Binder implements IWiFiDBReceiverResponseListener {
        private static final String DESCRIPTOR = "com.qti.wifidbreceiver.IWiFiDBReceiverResponseListener";
        static final int TRANSACTION_onAPListAvailable = 1;
        static final int TRANSACTION_onServiceRequest = 3;
        static final int TRANSACTION_onStatusUpdate = 2;

        private static class Proxy implements IWiFiDBReceiverResponseListener {
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

            public void onAPListAvailable(List<APInfo> apInfoList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(apInfoList);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onStatusUpdate(boolean isSuccess, String error) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!isSuccess) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeString(error);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onServiceRequest() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IWiFiDBReceiverResponseListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IWiFiDBReceiverResponseListener)) {
                return new Proxy(obj);
            }
            return (IWiFiDBReceiverResponseListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    onAPListAvailable(data.createTypedArrayList(APInfo.CREATOR));
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    onStatusUpdate(data.readInt() != 0, data.readString());
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    onServiceRequest();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onAPListAvailable(List<APInfo> list) throws RemoteException;

    void onServiceRequest() throws RemoteException;

    void onStatusUpdate(boolean z, String str) throws RemoteException;
}
