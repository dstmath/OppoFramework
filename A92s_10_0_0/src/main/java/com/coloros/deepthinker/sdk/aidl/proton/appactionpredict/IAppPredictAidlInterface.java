package com.coloros.deepthinker.sdk.aidl.proton.appactionpredict;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IAppPredictAidlInterface extends IInterface {
    PredictResult getAppPredictResult(String str) throws RemoteException;

    List<PredictResult> getAppPredictResultMap(String str) throws RemoteException;

    PredictAABResult getPredictAABResult() throws RemoteException;

    public static class Default implements IAppPredictAidlInterface {
        @Override // com.coloros.deepthinker.sdk.aidl.proton.appactionpredict.IAppPredictAidlInterface
        public PredictAABResult getPredictAABResult() throws RemoteException {
            return null;
        }

        @Override // com.coloros.deepthinker.sdk.aidl.proton.appactionpredict.IAppPredictAidlInterface
        public List<PredictResult> getAppPredictResultMap(String callerName) throws RemoteException {
            return null;
        }

        @Override // com.coloros.deepthinker.sdk.aidl.proton.appactionpredict.IAppPredictAidlInterface
        public PredictResult getAppPredictResult(String callerName) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IAppPredictAidlInterface {
        private static final String DESCRIPTOR = "com.coloros.deepthinker.sdk.aidl.proton.appactionpredict.IAppPredictAidlInterface";
        static final int TRANSACTION_getAppPredictResult = 3;
        static final int TRANSACTION_getAppPredictResultMap = 2;
        static final int TRANSACTION_getPredictAABResult = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAppPredictAidlInterface asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAppPredictAidlInterface)) {
                return new Proxy(obj);
            }
            return (IAppPredictAidlInterface) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "getPredictAABResult";
            }
            if (transactionCode == 2) {
                return "getAppPredictResultMap";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "getAppPredictResult";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                PredictAABResult _result = getPredictAABResult();
                reply.writeNoException();
                if (_result != null) {
                    reply.writeInt(1);
                    _result.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                List<PredictResult> _result2 = getAppPredictResultMap(data.readString());
                reply.writeNoException();
                reply.writeTypedList(_result2);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                PredictResult _result3 = getAppPredictResult(data.readString());
                reply.writeNoException();
                if (_result3 != null) {
                    reply.writeInt(1);
                    _result3.writeToParcel(reply, 1);
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

        private static class Proxy implements IAppPredictAidlInterface {
            public static IAppPredictAidlInterface sDefaultImpl;
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

            @Override // com.coloros.deepthinker.sdk.aidl.proton.appactionpredict.IAppPredictAidlInterface
            public PredictAABResult getPredictAABResult() throws RemoteException {
                PredictAABResult _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPredictAABResult();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = PredictAABResult.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.coloros.deepthinker.sdk.aidl.proton.appactionpredict.IAppPredictAidlInterface
            public List<PredictResult> getAppPredictResultMap(String callerName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerName);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAppPredictResultMap(callerName);
                    }
                    _reply.readException();
                    List<PredictResult> _result = _reply.createTypedArrayList(PredictResult.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.coloros.deepthinker.sdk.aidl.proton.appactionpredict.IAppPredictAidlInterface
            public PredictResult getAppPredictResult(String callerName) throws RemoteException {
                PredictResult _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callerName);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAppPredictResult(callerName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = PredictResult.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
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

        public static boolean setDefaultImpl(IAppPredictAidlInterface impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IAppPredictAidlInterface getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
