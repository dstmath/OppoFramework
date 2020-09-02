package android.service.euicc;

import android.annotation.UnsupportedAppUsage;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IGetEuiccProfileInfoListCallback extends IInterface {
    @UnsupportedAppUsage
    void onComplete(GetEuiccProfileInfoListResult getEuiccProfileInfoListResult) throws RemoteException;

    public static class Default implements IGetEuiccProfileInfoListCallback {
        @Override // android.service.euicc.IGetEuiccProfileInfoListCallback
        public void onComplete(GetEuiccProfileInfoListResult result) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IGetEuiccProfileInfoListCallback {
        private static final String DESCRIPTOR = "android.service.euicc.IGetEuiccProfileInfoListCallback";
        static final int TRANSACTION_onComplete = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IGetEuiccProfileInfoListCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGetEuiccProfileInfoListCallback)) {
                return new Proxy(obj);
            }
            return (IGetEuiccProfileInfoListCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "onComplete";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            GetEuiccProfileInfoListResult _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = GetEuiccProfileInfoListResult.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onComplete(_arg0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IGetEuiccProfileInfoListCallback {
            public static IGetEuiccProfileInfoListCallback sDefaultImpl;
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

            @Override // android.service.euicc.IGetEuiccProfileInfoListCallback
            public void onComplete(GetEuiccProfileInfoListResult result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (result != null) {
                        _data.writeInt(1);
                        result.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onComplete(result);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IGetEuiccProfileInfoListCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IGetEuiccProfileInfoListCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
