package com.coloros.deepthinker.sdk.aidl.proton.deepsleep;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IDeepSleepPredict extends IInterface {
    DeepSleepPredictResult getDeepSleepPredictResult() throws RemoteException;

    TotalPredictResult getDeepSleepTotalPredictResult() throws RemoteException;

    SleepRecord getLastDeepSleepRecord() throws RemoteException;

    DeepSleepPredictResult getPredictResultWithFeedBack() throws RemoteException;

    public static class Default implements IDeepSleepPredict {
        @Override // com.coloros.deepthinker.sdk.aidl.proton.deepsleep.IDeepSleepPredict
        public DeepSleepPredictResult getDeepSleepPredictResult() throws RemoteException {
            return null;
        }

        @Override // com.coloros.deepthinker.sdk.aidl.proton.deepsleep.IDeepSleepPredict
        public SleepRecord getLastDeepSleepRecord() throws RemoteException {
            return null;
        }

        @Override // com.coloros.deepthinker.sdk.aidl.proton.deepsleep.IDeepSleepPredict
        public TotalPredictResult getDeepSleepTotalPredictResult() throws RemoteException {
            return null;
        }

        @Override // com.coloros.deepthinker.sdk.aidl.proton.deepsleep.IDeepSleepPredict
        public DeepSleepPredictResult getPredictResultWithFeedBack() throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IDeepSleepPredict {
        private static final String DESCRIPTOR = "com.coloros.deepthinker.sdk.aidl.proton.deepsleep.IDeepSleepPredict";
        static final int TRANSACTION_getDeepSleepPredictResult = 1;
        static final int TRANSACTION_getDeepSleepTotalPredictResult = 3;
        static final int TRANSACTION_getLastDeepSleepRecord = 2;
        static final int TRANSACTION_getPredictResultWithFeedBack = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDeepSleepPredict asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDeepSleepPredict)) {
                return new Proxy(obj);
            }
            return (IDeepSleepPredict) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "getDeepSleepPredictResult";
            }
            if (transactionCode == 2) {
                return "getLastDeepSleepRecord";
            }
            if (transactionCode == 3) {
                return "getDeepSleepTotalPredictResult";
            }
            if (transactionCode != 4) {
                return null;
            }
            return "getPredictResultWithFeedBack";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                DeepSleepPredictResult _result = getDeepSleepPredictResult();
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
                SleepRecord _result2 = getLastDeepSleepRecord();
                reply.writeNoException();
                if (_result2 != null) {
                    reply.writeInt(1);
                    _result2.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                TotalPredictResult _result3 = getDeepSleepTotalPredictResult();
                reply.writeNoException();
                if (_result3 != null) {
                    reply.writeInt(1);
                    _result3.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                DeepSleepPredictResult _result4 = getPredictResultWithFeedBack();
                reply.writeNoException();
                if (_result4 != null) {
                    reply.writeInt(1);
                    _result4.writeToParcel(reply, 1);
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
        public static class Proxy implements IDeepSleepPredict {
            public static IDeepSleepPredict sDefaultImpl;
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

            @Override // com.coloros.deepthinker.sdk.aidl.proton.deepsleep.IDeepSleepPredict
            public DeepSleepPredictResult getDeepSleepPredictResult() throws RemoteException {
                DeepSleepPredictResult _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDeepSleepPredictResult();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = DeepSleepPredictResult.CREATOR.createFromParcel(_reply);
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

            @Override // com.coloros.deepthinker.sdk.aidl.proton.deepsleep.IDeepSleepPredict
            public SleepRecord getLastDeepSleepRecord() throws RemoteException {
                SleepRecord _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLastDeepSleepRecord();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = SleepRecord.CREATOR.createFromParcel(_reply);
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

            @Override // com.coloros.deepthinker.sdk.aidl.proton.deepsleep.IDeepSleepPredict
            public TotalPredictResult getDeepSleepTotalPredictResult() throws RemoteException {
                TotalPredictResult _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDeepSleepTotalPredictResult();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = TotalPredictResult.CREATOR.createFromParcel(_reply);
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

            @Override // com.coloros.deepthinker.sdk.aidl.proton.deepsleep.IDeepSleepPredict
            public DeepSleepPredictResult getPredictResultWithFeedBack() throws RemoteException {
                DeepSleepPredictResult _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPredictResultWithFeedBack();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = DeepSleepPredictResult.CREATOR.createFromParcel(_reply);
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

        public static boolean setDefaultImpl(IDeepSleepPredict impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IDeepSleepPredict getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
