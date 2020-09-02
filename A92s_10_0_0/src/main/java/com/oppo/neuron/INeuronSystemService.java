package com.oppo.neuron;

import android.content.ContentValues;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.oppo.neuron.INeuronSystemEventListener;
import java.util.List;

public interface INeuronSystemService extends IInterface {
    List<String> getRecommendedApps(int i) throws RemoteException;

    void publishEvent(int i, ContentValues contentValues) throws RemoteException;

    void registerEventListener(INeuronSystemEventListener iNeuronSystemEventListener) throws RemoteException;

    public static class Default implements INeuronSystemService {
        @Override // com.oppo.neuron.INeuronSystemService
        public void publishEvent(int type, ContentValues contentValues) throws RemoteException {
        }

        @Override // com.oppo.neuron.INeuronSystemService
        public void registerEventListener(INeuronSystemEventListener listener) throws RemoteException {
        }

        @Override // com.oppo.neuron.INeuronSystemService
        public List<String> getRecommendedApps(int topK) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INeuronSystemService {
        private static final String DESCRIPTOR = "com.oppo.neuron.INeuronSystemService";
        static final int TRANSACTION_getRecommendedApps = 3;
        static final int TRANSACTION_publishEvent = 1;
        static final int TRANSACTION_registerEventListener = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INeuronSystemService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INeuronSystemService)) {
                return new Proxy(obj);
            }
            return (INeuronSystemService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "publishEvent";
            }
            if (transactionCode == 2) {
                return "registerEventListener";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "getRecommendedApps";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ContentValues _arg1;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _arg0 = data.readInt();
                if (data.readInt() != 0) {
                    _arg1 = ContentValues.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                publishEvent(_arg0, _arg1);
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                registerEventListener(INeuronSystemEventListener.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                List<String> _result = getRecommendedApps(data.readInt());
                reply.writeNoException();
                reply.writeStringList(_result);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements INeuronSystemService {
            public static INeuronSystemService sDefaultImpl;
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

            @Override // com.oppo.neuron.INeuronSystemService
            public void publishEvent(int type, ContentValues contentValues) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    if (contentValues != null) {
                        _data.writeInt(1);
                        contentValues.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().publishEvent(type, contentValues);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.neuron.INeuronSystemService
            public void registerEventListener(INeuronSystemEventListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerEventListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.neuron.INeuronSystemService
            public List<String> getRecommendedApps(int topK) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(topK);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRecommendedApps(topK);
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(INeuronSystemService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INeuronSystemService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
