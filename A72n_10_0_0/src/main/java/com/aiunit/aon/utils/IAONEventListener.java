package com.aiunit.aon.utils;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.aiunit.aon.utils.core.FaceInfo;

public interface IAONEventListener extends IInterface {
    void onEvent(int i, int i2) throws RemoteException;

    void onEventParam(int i, int i2, FaceInfo faceInfo) throws RemoteException;

    public static class Default implements IAONEventListener {
        @Override // com.aiunit.aon.utils.IAONEventListener
        public void onEvent(int eventType, int event) throws RemoteException {
        }

        @Override // com.aiunit.aon.utils.IAONEventListener
        public void onEventParam(int eventType, int event, FaceInfo faceInfo) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IAONEventListener {
        private static final String DESCRIPTOR = "com.aiunit.aon.utils.IAONEventListener";
        static final int TRANSACTION_onEvent = 1;
        static final int TRANSACTION_onEventParam = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAONEventListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAONEventListener)) {
                return new Proxy(obj);
            }
            return (IAONEventListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onEvent";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "onEventParam";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            FaceInfo _arg2;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onEvent(data.readInt(), data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int _arg0 = data.readInt();
                int _arg1 = data.readInt();
                if (data.readInt() != 0) {
                    _arg2 = FaceInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg2 = null;
                }
                onEventParam(_arg0, _arg1, _arg2);
                reply.writeNoException();
                if (_arg2 != null) {
                    reply.writeInt(1);
                    _arg2.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IAONEventListener {
            public static IAONEventListener sDefaultImpl;
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

            @Override // com.aiunit.aon.utils.IAONEventListener
            public void onEvent(int eventType, int event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(eventType);
                    _data.writeInt(event);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onEvent(eventType, event);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.aiunit.aon.utils.IAONEventListener
            public void onEventParam(int eventType, int event, FaceInfo faceInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(eventType);
                    _data.writeInt(event);
                    if (faceInfo != null) {
                        _data.writeInt(1);
                        faceInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        if (_reply.readInt() != 0) {
                            faceInfo.readFromParcel(_reply);
                        }
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onEventParam(eventType, event, faceInfo);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IAONEventListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IAONEventListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
