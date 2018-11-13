package com.suntek.mway.rcs.client.aidl.plugin.callback;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.suntek.mway.rcs.client.aidl.plugin.entity.contact.Auth;

public interface IContactSyncListener extends IInterface {

    public static abstract class Stub extends Binder implements IContactSyncListener {
        private static final String DESCRIPTOR = "com.suntek.mway.rcs.client.aidl.plugin.callback.IContactSyncListener";
        static final int TRANSACTION_onAuthSession = 2;
        static final int TRANSACTION_onExecuting = 3;
        static final int TRANSACTION_onHttpResponeText = 5;
        static final int TRANSACTION_onPreExecuteAuthSession = 1;
        static final int TRANSACTION_onProgress = 4;
        static final int TRANSACTION_onRunning = 8;
        static final int TRANSACTION_onSync = 6;
        static final int TRANSACTION_onThrowException = 7;

        private static class Proxy implements IContactSyncListener {
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

            public void onPreExecuteAuthSession(Auth auth) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (auth != null) {
                        _data.writeInt(1);
                        auth.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onAuthSession(Auth auth, boolean timeout) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (auth != null) {
                        _data.writeInt(1);
                        auth.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!timeout) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onExecuting(Auth auth, int syncAction) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (auth != null) {
                        _data.writeInt(1);
                        auth.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(syncAction);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onProgress(Auth auth, int contactAction, int value, int max) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (auth != null) {
                        _data.writeInt(1);
                        auth.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(contactAction);
                    _data.writeInt(value);
                    _data.writeInt(max);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onHttpResponeText(String message, String resultcode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(message);
                    _data.writeString(resultcode);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onSync(Auth auth, int syncAction, boolean success) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (auth != null) {
                        _data.writeInt(1);
                        auth.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(syncAction);
                    if (!success) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onThrowException(Auth auth, int syncAction, String exceptionMessage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (auth != null) {
                        _data.writeInt(1);
                        auth.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(syncAction);
                    _data.writeString(exceptionMessage);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onRunning() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IContactSyncListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IContactSyncListener)) {
                return new Proxy(obj);
            }
            return (IContactSyncListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Auth _arg0;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (Auth) Auth.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    onPreExecuteAuthSession(_arg0);
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (Auth) Auth.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    onAuthSession(_arg0, data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (Auth) Auth.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    onExecuting(_arg0, data.readInt());
                    reply.writeNoException();
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (Auth) Auth.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    onProgress(_arg0, data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    onHttpResponeText(data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (Auth) Auth.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    onSync(_arg0, data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (Auth) Auth.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    onThrowException(_arg0, data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    onRunning();
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onAuthSession(Auth auth, boolean z) throws RemoteException;

    void onExecuting(Auth auth, int i) throws RemoteException;

    void onHttpResponeText(String str, String str2) throws RemoteException;

    void onPreExecuteAuthSession(Auth auth) throws RemoteException;

    void onProgress(Auth auth, int i, int i2, int i3) throws RemoteException;

    void onRunning() throws RemoteException;

    void onSync(Auth auth, int i, boolean z) throws RemoteException;

    void onThrowException(Auth auth, int i, String str) throws RemoteException;
}
