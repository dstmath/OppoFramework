package com.mediatek.hdmi;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface NvRAMAgent extends IInterface {

    public static abstract class Stub extends Binder implements NvRAMAgent {
        private static final String DESCRIPTOR = "NvRAMAgent";
        static final int TRANSACTION_READFILE = 1;
        static final int TRANSACTION_WRITEFILE = 2;

        private static class Proxy implements NvRAMAgent {
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

            public byte[] readFile(int fileLid) throws RemoteException {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                try {
                    data.writeInterfaceToken(Stub.DESCRIPTOR);
                    data.writeInt(fileLid);
                    this.mRemote.transact(1, data, reply, 0);
                    reply.readException();
                    byte[] result = reply.createByteArray();
                    return result;
                } finally {
                    reply.recycle();
                    data.recycle();
                }
            }

            public int writeFile(int fileLid, byte[] buff) throws RemoteException {
                Parcel data = Parcel.obtain();
                Parcel reply = Parcel.obtain();
                try {
                    data.writeInterfaceToken(Stub.DESCRIPTOR);
                    data.writeInt(fileLid);
                    data.writeByteArray(buff);
                    this.mRemote.transact(2, data, reply, 0);
                    reply.readException();
                    int result = reply.readInt();
                    return result;
                } finally {
                    reply.recycle();
                    data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static NvRAMAgent asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof NvRAMAgent)) {
                return new Proxy(obj);
            }
            return (NvRAMAgent) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    byte[] myResult = readFile(data.readInt());
                    reply.writeNoException();
                    reply.writeByteArray(myResult);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    int myResult2 = writeFile(data.readInt(), data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(myResult2);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    byte[] readFile(int i) throws RemoteException;

    int writeFile(int i, byte[] bArr) throws RemoteException;
}
