package com.mediatek.mmsdk;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IMMSdkService extends IInterface {
    int connectFeatureManager(BinderHolder binderHolder) throws RemoteException;

    int existCallbackClient() throws RemoteException;

    public static class Default implements IMMSdkService {
        @Override // com.mediatek.mmsdk.IMMSdkService
        public int connectFeatureManager(BinderHolder featureManager) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.mmsdk.IMMSdkService
        public int existCallbackClient() throws RemoteException {
            return 0;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMMSdkService {
        private static final String DESCRIPTOR = "com.mediatek.mmsdk.IMMSdkService";
        static final int TRANSACTION_connectFeatureManager = 1;
        static final int TRANSACTION_existCallbackClient = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMMSdkService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMMSdkService)) {
                return new Proxy(obj);
            }
            return (IMMSdkService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                BinderHolder _arg0 = new BinderHolder();
                int _result = connectFeatureManager(_arg0);
                reply.writeNoException();
                reply.writeInt(_result);
                reply.writeInt(1);
                _arg0.writeToParcel(reply, 1);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int _result2 = existCallbackClient();
                reply.writeNoException();
                reply.writeInt(_result2);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IMMSdkService {
            public static IMMSdkService sDefaultImpl;
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

            @Override // com.mediatek.mmsdk.IMMSdkService
            public int connectFeatureManager(BinderHolder featureManager) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().connectFeatureManager(featureManager);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        featureManager.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.mmsdk.IMMSdkService
            public int existCallbackClient() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().existCallbackClient();
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

        public static boolean setDefaultImpl(IMMSdkService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMMSdkService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
