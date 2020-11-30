package com.aiunit.aon.utils;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.aiunit.aon.utils.core.CommentResult;
import com.aiunit.aon.utils.core.ErrorResult;
import com.aiunit.aon.utils.core.InfoResult;

public interface IAONCallback extends IInterface {
    String getRequestID() throws RemoteException;

    void onDetectedError(ErrorResult errorResult) throws RemoteException;

    void onDetectedInfo(InfoResult infoResult) throws RemoteException;

    void onDetectedResult(CommentResult commentResult) throws RemoteException;

    public static class Default implements IAONCallback {
        @Override // com.aiunit.aon.utils.IAONCallback
        public String getRequestID() throws RemoteException {
            return null;
        }

        @Override // com.aiunit.aon.utils.IAONCallback
        public void onDetectedError(ErrorResult errorResult) throws RemoteException {
        }

        @Override // com.aiunit.aon.utils.IAONCallback
        public void onDetectedInfo(InfoResult infoResult) throws RemoteException {
        }

        @Override // com.aiunit.aon.utils.IAONCallback
        public void onDetectedResult(CommentResult commentResult) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IAONCallback {
        private static final String DESCRIPTOR = "com.aiunit.aon.utils.IAONCallback";
        static final int TRANSACTION_getRequestID = 1;
        static final int TRANSACTION_onDetectedError = 2;
        static final int TRANSACTION_onDetectedInfo = 3;
        static final int TRANSACTION_onDetectedResult = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAONCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAONCallback)) {
                return new Proxy(obj);
            }
            return (IAONCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "getRequestID";
            }
            if (transactionCode == 2) {
                return "onDetectedError";
            }
            if (transactionCode == 3) {
                return "onDetectedInfo";
            }
            if (transactionCode != 4) {
                return null;
            }
            return "onDetectedResult";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ErrorResult _arg0;
            InfoResult _arg02;
            CommentResult _arg03;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                String _result = getRequestID();
                reply.writeNoException();
                reply.writeString(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = ErrorResult.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onDetectedError(_arg0);
                reply.writeNoException();
                if (_arg0 != null) {
                    reply.writeInt(1);
                    _arg0.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = InfoResult.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                onDetectedInfo(_arg02);
                reply.writeNoException();
                if (_arg02 != null) {
                    reply.writeInt(1);
                    _arg02.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg03 = CommentResult.CREATOR.createFromParcel(data);
                } else {
                    _arg03 = null;
                }
                onDetectedResult(_arg03);
                reply.writeNoException();
                if (_arg03 != null) {
                    reply.writeInt(1);
                    _arg03.writeToParcel(reply, 1);
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
        public static class Proxy implements IAONCallback {
            public static IAONCallback sDefaultImpl;
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

            @Override // com.aiunit.aon.utils.IAONCallback
            public String getRequestID() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRequestID();
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

            @Override // com.aiunit.aon.utils.IAONCallback
            public void onDetectedError(ErrorResult errorResult) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (errorResult != null) {
                        _data.writeInt(1);
                        errorResult.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        if (_reply.readInt() != 0) {
                            errorResult.readFromParcel(_reply);
                        }
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onDetectedError(errorResult);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.aiunit.aon.utils.IAONCallback
            public void onDetectedInfo(InfoResult infoResult) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (infoResult != null) {
                        _data.writeInt(1);
                        infoResult.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        if (_reply.readInt() != 0) {
                            infoResult.readFromParcel(_reply);
                        }
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onDetectedInfo(infoResult);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.aiunit.aon.utils.IAONCallback
            public void onDetectedResult(CommentResult commentResult) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (commentResult != null) {
                        _data.writeInt(1);
                        commentResult.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        if (_reply.readInt() != 0) {
                            commentResult.readFromParcel(_reply);
                        }
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onDetectedResult(commentResult);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IAONCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IAONCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
