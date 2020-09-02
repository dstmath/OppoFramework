package com.mediatek.mmsdk;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.mediatek.mmsdk.IMemoryHeap;

public interface IMemory extends IInterface {
    IMemoryHeap getMemory(int i, int i2) throws RemoteException;

    public static class Default implements IMemory {
        @Override // com.mediatek.mmsdk.IMemory
        public IMemoryHeap getMemory(int offset, int size) throws RemoteException {
            return null;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMemory {
        private static final String DESCRIPTOR = "com.mediatek.mmsdk.IMemory";
        static final int TRANSACTION_getMemory = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMemory asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMemory)) {
                return new Proxy(obj);
            }
            return (IMemory) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                IMemoryHeap _result = getMemory(data.readInt(), data.readInt());
                reply.writeNoException();
                reply.writeStrongBinder(_result != null ? _result.asBinder() : null);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IMemory {
            public static IMemory sDefaultImpl;
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

            @Override // com.mediatek.mmsdk.IMemory
            public IMemoryHeap getMemory(int offset, int size) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(offset);
                    _data.writeInt(size);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMemory(offset, size);
                    }
                    _reply.readException();
                    IMemoryHeap _result = IMemoryHeap.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IMemory impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMemory getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
