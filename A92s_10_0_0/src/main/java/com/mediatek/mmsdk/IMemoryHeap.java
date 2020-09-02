package com.mediatek.mmsdk;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IMemoryHeap extends IInterface {
    int getBase() throws RemoteException;

    int getFlags() throws RemoteException;

    int getHeapID(int i, int i2) throws RemoteException;

    int getOffset() throws RemoteException;

    int getSize() throws RemoteException;

    public static class Default implements IMemoryHeap {
        @Override // com.mediatek.mmsdk.IMemoryHeap
        public int getHeapID(int offset, int size) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.mmsdk.IMemoryHeap
        public int getBase() throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.mmsdk.IMemoryHeap
        public int getSize() throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.mmsdk.IMemoryHeap
        public int getFlags() throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.mmsdk.IMemoryHeap
        public int getOffset() throws RemoteException {
            return 0;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMemoryHeap {
        private static final String DESCRIPTOR = "com.mediatek.mmsdk.IMemoryHeap";
        static final int TRANSACTION_getBase = 2;
        static final int TRANSACTION_getFlags = 4;
        static final int TRANSACTION_getHeapID = 1;
        static final int TRANSACTION_getOffset = 5;
        static final int TRANSACTION_getSize = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMemoryHeap asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMemoryHeap)) {
                return new Proxy(obj);
            }
            return (IMemoryHeap) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _result = getHeapID(data.readInt(), data.readInt());
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int _result2 = getBase();
                reply.writeNoException();
                reply.writeInt(_result2);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                int _result3 = getSize();
                reply.writeNoException();
                reply.writeInt(_result3);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                int _result4 = getFlags();
                reply.writeNoException();
                reply.writeInt(_result4);
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                int _result5 = getOffset();
                reply.writeNoException();
                reply.writeInt(_result5);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IMemoryHeap {
            public static IMemoryHeap sDefaultImpl;
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

            @Override // com.mediatek.mmsdk.IMemoryHeap
            public int getHeapID(int offset, int size) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(offset);
                    _data.writeInt(size);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHeapID(offset, size);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.mmsdk.IMemoryHeap
            public int getBase() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getBase();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.mmsdk.IMemoryHeap
            public int getSize() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSize();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.mmsdk.IMemoryHeap
            public int getFlags() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFlags();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.mmsdk.IMemoryHeap
            public int getOffset() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getOffset();
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IMemoryHeap impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMemoryHeap getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
