package com.mediatek.internal.telephony;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IMtkTelephonyRegistryEx extends IInterface {

    public static class Default implements IMtkTelephonyRegistryEx {
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMtkTelephonyRegistryEx {
        private static final String DESCRIPTOR = "com.mediatek.internal.telephony.IMtkTelephonyRegistryEx";

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMtkTelephonyRegistryEx asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMtkTelephonyRegistryEx)) {
                return new Proxy(obj);
            }
            return (IMtkTelephonyRegistryEx) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            }
            reply.writeString(DESCRIPTOR);
            return true;
        }

        private static class Proxy implements IMtkTelephonyRegistryEx {
            public static IMtkTelephonyRegistryEx sDefaultImpl;
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
        }

        public static boolean setDefaultImpl(IMtkTelephonyRegistryEx impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMtkTelephonyRegistryEx getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
