package com.oppo.atlas;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.oppo.atlas.IOppoAtlasAudioCallback;
import com.oppo.atlas.IOppoAtlasServiceCallback;

public interface IOppoMMAtlasService extends IInterface {
    String getParameters(String str) throws RemoteException;

    void registerAudioCallback(IOppoAtlasAudioCallback iOppoAtlasAudioCallback) throws RemoteException;

    void registerCallback(IOppoAtlasServiceCallback iOppoAtlasServiceCallback) throws RemoteException;

    void setEvent(int i, String str) throws RemoteException;

    void setParameters(String str) throws RemoteException;

    void unRegisterAudioCallback(IOppoAtlasAudioCallback iOppoAtlasAudioCallback) throws RemoteException;

    void unRegisterCallback(IOppoAtlasServiceCallback iOppoAtlasServiceCallback) throws RemoteException;

    public static class Default implements IOppoMMAtlasService {
        @Override // com.oppo.atlas.IOppoMMAtlasService
        public void setEvent(int event, String value) throws RemoteException {
        }

        @Override // com.oppo.atlas.IOppoMMAtlasService
        public void setParameters(String keyValuePairs) throws RemoteException {
        }

        @Override // com.oppo.atlas.IOppoMMAtlasService
        public String getParameters(String keyValuePairs) throws RemoteException {
            return null;
        }

        @Override // com.oppo.atlas.IOppoMMAtlasService
        public void registerCallback(IOppoAtlasServiceCallback callback) throws RemoteException {
        }

        @Override // com.oppo.atlas.IOppoMMAtlasService
        public void unRegisterCallback(IOppoAtlasServiceCallback callback) throws RemoteException {
        }

        @Override // com.oppo.atlas.IOppoMMAtlasService
        public void registerAudioCallback(IOppoAtlasAudioCallback callback) throws RemoteException {
        }

        @Override // com.oppo.atlas.IOppoMMAtlasService
        public void unRegisterAudioCallback(IOppoAtlasAudioCallback callback) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOppoMMAtlasService {
        private static final String DESCRIPTOR = "com.oppo.atlas.IOppoMMAtlasService";
        static final int TRANSACTION_getParameters = 3;
        static final int TRANSACTION_registerAudioCallback = 6;
        static final int TRANSACTION_registerCallback = 4;
        static final int TRANSACTION_setEvent = 1;
        static final int TRANSACTION_setParameters = 2;
        static final int TRANSACTION_unRegisterAudioCallback = 7;
        static final int TRANSACTION_unRegisterCallback = 5;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOppoMMAtlasService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOppoMMAtlasService)) {
                return new Proxy(obj);
            }
            return (IOppoMMAtlasService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "setEvent";
                case 2:
                    return "setParameters";
                case 3:
                    return "getParameters";
                case 4:
                    return "registerCallback";
                case 5:
                    return "unRegisterCallback";
                case 6:
                    return "registerAudioCallback";
                case 7:
                    return "unRegisterAudioCallback";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        setEvent(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        setParameters(data.readString());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        String _result = getParameters(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        registerCallback(IOppoAtlasServiceCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        unRegisterCallback(IOppoAtlasServiceCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        registerAudioCallback(IOppoAtlasAudioCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        unRegisterAudioCallback(IOppoAtlasAudioCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IOppoMMAtlasService {
            public static IOppoMMAtlasService sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // com.oppo.atlas.IOppoMMAtlasService
            public void setEvent(int event, String value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(event);
                    _data.writeString(value);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setEvent(event, value);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.atlas.IOppoMMAtlasService
            public void setParameters(String keyValuePairs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(keyValuePairs);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setParameters(keyValuePairs);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.atlas.IOppoMMAtlasService
            public String getParameters(String keyValuePairs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(keyValuePairs);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getParameters(keyValuePairs);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.atlas.IOppoMMAtlasService
            public void registerCallback(IOppoAtlasServiceCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.atlas.IOppoMMAtlasService
            public void unRegisterCallback(IOppoAtlasServiceCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unRegisterCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.atlas.IOppoMMAtlasService
            public void registerAudioCallback(IOppoAtlasAudioCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerAudioCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.atlas.IOppoMMAtlasService
            public void unRegisterAudioCallback(IOppoAtlasAudioCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unRegisterAudioCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IOppoMMAtlasService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOppoMMAtlasService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
