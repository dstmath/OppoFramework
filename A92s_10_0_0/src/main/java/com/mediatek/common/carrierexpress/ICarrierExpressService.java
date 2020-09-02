package com.mediatek.common.carrierexpress;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;
import java.util.Map;

public interface ICarrierExpressService extends IInterface {
    String getActiveOpPack() throws RemoteException;

    Map getAllOpPackList() throws RemoteException;

    String getOpPackFromSimInfo(String str) throws RemoteException;

    List getOperatorSubIdList(String str) throws RemoteException;

    void setOpPackActive(String str, String str2, int i) throws RemoteException;

    public static class Default implements ICarrierExpressService {
        @Override // com.mediatek.common.carrierexpress.ICarrierExpressService
        public String getActiveOpPack() throws RemoteException {
            return null;
        }

        @Override // com.mediatek.common.carrierexpress.ICarrierExpressService
        public String getOpPackFromSimInfo(String mcc_mnc) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.common.carrierexpress.ICarrierExpressService
        public void setOpPackActive(String opPack, String opSubId, int mainSlot) throws RemoteException {
        }

        @Override // com.mediatek.common.carrierexpress.ICarrierExpressService
        public Map getAllOpPackList() throws RemoteException {
            return null;
        }

        @Override // com.mediatek.common.carrierexpress.ICarrierExpressService
        public List getOperatorSubIdList(String opPack) throws RemoteException {
            return null;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ICarrierExpressService {
        private static final String DESCRIPTOR = "com.mediatek.common.carrierexpress.ICarrierExpressService";
        static final int TRANSACTION_getActiveOpPack = 1;
        static final int TRANSACTION_getAllOpPackList = 4;
        static final int TRANSACTION_getOpPackFromSimInfo = 2;
        static final int TRANSACTION_getOperatorSubIdList = 5;
        static final int TRANSACTION_setOpPackActive = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ICarrierExpressService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICarrierExpressService)) {
                return new Proxy(obj);
            }
            return (ICarrierExpressService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                String _result = getActiveOpPack();
                reply.writeNoException();
                reply.writeString(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                String _result2 = getOpPackFromSimInfo(data.readString());
                reply.writeNoException();
                reply.writeString(_result2);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                setOpPackActive(data.readString(), data.readString(), data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                Map _result3 = getAllOpPackList();
                reply.writeNoException();
                reply.writeMap(_result3);
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                List _result4 = getOperatorSubIdList(data.readString());
                reply.writeNoException();
                reply.writeList(_result4);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements ICarrierExpressService {
            public static ICarrierExpressService sDefaultImpl;
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

            @Override // com.mediatek.common.carrierexpress.ICarrierExpressService
            public String getActiveOpPack() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getActiveOpPack();
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

            @Override // com.mediatek.common.carrierexpress.ICarrierExpressService
            public String getOpPackFromSimInfo(String mcc_mnc) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(mcc_mnc);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getOpPackFromSimInfo(mcc_mnc);
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

            @Override // com.mediatek.common.carrierexpress.ICarrierExpressService
            public void setOpPackActive(String opPack, String opSubId, int mainSlot) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(opPack);
                    _data.writeString(opSubId);
                    _data.writeInt(mainSlot);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setOpPackActive(opPack, opSubId, mainSlot);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.carrierexpress.ICarrierExpressService
            public Map getAllOpPackList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAllOpPackList();
                    }
                    _reply.readException();
                    Map _result = _reply.readHashMap(getClass().getClassLoader());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.carrierexpress.ICarrierExpressService
            public List getOperatorSubIdList(String opPack) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(opPack);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getOperatorSubIdList(opPack);
                    }
                    _reply.readException();
                    List _result = _reply.readArrayList(getClass().getClassLoader());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ICarrierExpressService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ICarrierExpressService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
