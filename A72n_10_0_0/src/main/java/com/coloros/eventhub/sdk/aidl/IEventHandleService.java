package com.coloros.eventhub.sdk.aidl;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.coloros.eventhub.sdk.aidl.IEventCallback;

public interface IEventHandleService extends IInterface {
    boolean registerCallback(IEventCallback iEventCallback, int i, EventRequestConfig eventRequestConfig) throws RemoteException;

    void triggerHookEvent(TriggerEvent triggerEvent) throws RemoteException;

    boolean unregisterCallback(int i) throws RemoteException;

    public static class Default implements IEventHandleService {
        @Override // com.coloros.eventhub.sdk.aidl.IEventHandleService
        public void triggerHookEvent(TriggerEvent triggerEvent) throws RemoteException {
        }

        @Override // com.coloros.eventhub.sdk.aidl.IEventHandleService
        public boolean registerCallback(IEventCallback callback, int fingerPrint, EventRequestConfig config) throws RemoteException {
            return false;
        }

        @Override // com.coloros.eventhub.sdk.aidl.IEventHandleService
        public boolean unregisterCallback(int fingerPrint) throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IEventHandleService {
        private static final String DESCRIPTOR = "com.coloros.eventhub.sdk.aidl.IEventHandleService";
        static final int TRANSACTION_registerCallback = 2;
        static final int TRANSACTION_triggerHookEvent = 1;
        static final int TRANSACTION_unregisterCallback = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IEventHandleService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IEventHandleService)) {
                return new Proxy(obj);
            }
            return (IEventHandleService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "triggerHookEvent";
            }
            if (transactionCode == 2) {
                return "registerCallback";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "unregisterCallback";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            TriggerEvent _arg0;
            EventRequestConfig _arg2;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = TriggerEvent.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                triggerHookEvent(_arg0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                IEventCallback _arg02 = IEventCallback.Stub.asInterface(data.readStrongBinder());
                int _arg1 = data.readInt();
                if (data.readInt() != 0) {
                    _arg2 = EventRequestConfig.CREATOR.createFromParcel(data);
                } else {
                    _arg2 = null;
                }
                boolean registerCallback = registerCallback(_arg02, _arg1, _arg2);
                reply.writeNoException();
                reply.writeInt(registerCallback ? 1 : 0);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                boolean unregisterCallback = unregisterCallback(data.readInt());
                reply.writeNoException();
                reply.writeInt(unregisterCallback ? 1 : 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IEventHandleService {
            public static IEventHandleService sDefaultImpl;
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

            @Override // com.coloros.eventhub.sdk.aidl.IEventHandleService
            public void triggerHookEvent(TriggerEvent triggerEvent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (triggerEvent != null) {
                        _data.writeInt(1);
                        triggerEvent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().triggerHookEvent(triggerEvent);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.coloros.eventhub.sdk.aidl.IEventHandleService
            public boolean registerCallback(IEventCallback callback, int fingerPrint, EventRequestConfig config) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeInt(fingerPrint);
                    boolean _result = true;
                    if (config != null) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerCallback(callback, fingerPrint, config);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.coloros.eventhub.sdk.aidl.IEventHandleService
            public boolean unregisterCallback(int fingerPrint) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fingerPrint);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unregisterCallback(fingerPrint);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IEventHandleService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IEventHandleService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
