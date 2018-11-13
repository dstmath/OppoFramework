package com.suntek.mway.rcs.client.aidl.service.callback;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.suntek.mway.rcs.client.aidl.service.entity.RCSCapabilities;

public interface ICapabiltyListener extends IInterface {

    public static abstract class Stub extends Binder implements ICapabiltyListener {
        private static final String DESCRIPTOR = "com.suntek.mway.rcs.client.aidl.service.callback.ICapabiltyListener";
        static final int TRANSACTION_onCallback = 1;

        private static class Proxy implements ICapabiltyListener {
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

            public void onCallback(RCSCapabilities capabilities, int resultCode, String resultDesc, String number) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (capabilities != null) {
                        _data.writeInt(1);
                        capabilities.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(resultCode);
                    _data.writeString(resultDesc);
                    _data.writeString(number);
                    this.mRemote.transact(1, _data, _reply, 0);
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

        public static ICapabiltyListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICapabiltyListener)) {
                return new Proxy(obj);
            }
            return (ICapabiltyListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    RCSCapabilities _arg0;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (RCSCapabilities) RCSCapabilities.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    onCallback(_arg0, data.readInt(), data.readString(), data.readString());
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

    void onCallback(RCSCapabilities rCSCapabilities, int i, String str, String str2) throws RemoteException;
}
