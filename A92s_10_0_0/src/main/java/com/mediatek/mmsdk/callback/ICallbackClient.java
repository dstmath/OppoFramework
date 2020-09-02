package com.mediatek.mmsdk.callback;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.Surface;
import com.mediatek.mmsdk.BaseParameters;
import java.util.List;

public interface ICallbackClient extends IInterface {
    int setOutputSurfaces(List<Surface> list, List<BaseParameters> list2) throws RemoteException;

    long start() throws RemoteException;

    long stop() throws RemoteException;

    public static class Default implements ICallbackClient {
        @Override // com.mediatek.mmsdk.callback.ICallbackClient
        public long start() throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.mmsdk.callback.ICallbackClient
        public long stop() throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.mmsdk.callback.ICallbackClient
        public int setOutputSurfaces(List<Surface> list, List<BaseParameters> list2) throws RemoteException {
            return 0;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ICallbackClient {
        private static final String DESCRIPTOR = "com.mediatek.mmsdk.callback.ICallbackClient";
        static final int TRANSACTION_setOutputSurfaces = 3;
        static final int TRANSACTION_start = 1;
        static final int TRANSACTION_stop = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ICallbackClient asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICallbackClient)) {
                return new Proxy(obj);
            }
            return (ICallbackClient) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                long _result = start();
                reply.writeNoException();
                reply.writeLong(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                long _result2 = stop();
                reply.writeNoException();
                reply.writeLong(_result2);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                int _result3 = setOutputSurfaces(data.createTypedArrayList(Surface.CREATOR), data.createTypedArrayList(BaseParameters.CREATOR));
                reply.writeNoException();
                reply.writeInt(_result3);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements ICallbackClient {
            public static ICallbackClient sDefaultImpl;
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

            @Override // com.mediatek.mmsdk.callback.ICallbackClient
            public long start() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().start();
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.mmsdk.callback.ICallbackClient
            public long stop() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().stop();
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.mmsdk.callback.ICallbackClient
            public int setOutputSurfaces(List<Surface> output, List<BaseParameters> parameters) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(output);
                    _data.writeTypedList(parameters);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setOutputSurfaces(output, parameters);
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

        public static boolean setDefaultImpl(ICallbackClient impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ICallbackClient getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
