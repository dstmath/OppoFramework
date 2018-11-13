package com.suntek.mway.rcs.client.aidl.plugin.callback;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.suntek.mway.rcs.client.aidl.plugin.entity.pubacct.MenuInfoMode;
import com.suntek.mway.rcs.client.aidl.plugin.entity.pubacct.MsgContent;
import com.suntek.mway.rcs.client.aidl.plugin.entity.pubacct.PublicAccounts;
import com.suntek.mway.rcs.client.aidl.plugin.entity.pubacct.PublicAccountsDetail;
import java.util.List;

public interface IPublicAccountCallbackAPI extends IInterface {

    public static abstract class Stub extends Binder implements IPublicAccountCallbackAPI {
        private static final String DESCRIPTOR = "com.suntek.mway.rcs.client.aidl.plugin.callback.IPublicAccountCallbackAPI";
        static final int TRANSACTION_respAddSubscribeAccount = 6;
        static final int TRANSACTION_respCancelSubscribeAccount = 7;
        static final int TRANSACTION_respComplainPublicAccount = 8;
        static final int TRANSACTION_respGetPreMessage = 1;
        static final int TRANSACTION_respGetPublicDetail = 4;
        static final int TRANSACTION_respGetPublicList = 5;
        static final int TRANSACTION_respGetPublicMenuInfo = 2;
        static final int TRANSACTION_respGetPublicRecommend = 9;
        static final int TRANSACTION_respGetUserSubscribePublicList = 3;
        static final int TRANSACTION_respSetAcceptStatus = 10;

        private static class Proxy implements IPublicAccountCallbackAPI {
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

            public void respGetPreMessage(boolean result, List<MsgContent> msgContent) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!result) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeTypedList(msgContent);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void respGetPublicMenuInfo(boolean result, MenuInfoMode menuInfoMode) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!result) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (menuInfoMode != null) {
                        _data.writeInt(1);
                        menuInfoMode.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void respGetUserSubscribePublicList(boolean result, List<PublicAccounts> pubAcctList) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (result) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeTypedList(pubAcctList);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void respGetPublicDetail(boolean result, PublicAccountsDetail accountDetail) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!result) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (accountDetail != null) {
                        _data.writeInt(1);
                        accountDetail.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void respGetPublicList(boolean result, List<PublicAccounts> accountList) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (result) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeTypedList(accountList);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void respAddSubscribeAccount(boolean result, PublicAccounts account) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!result) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void respCancelSubscribeAccount(boolean result, PublicAccounts account) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!result) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void respComplainPublicAccount(boolean result, PublicAccounts account) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!result) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (account != null) {
                        _data.writeInt(1);
                        account.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void respGetPublicRecommend(boolean result, List<PublicAccounts> accountList) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (result) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeTypedList(accountList);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void respSetAcceptStatus(boolean result, String uuid) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (result) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeString(uuid);
                    this.mRemote.transact(10, _data, _reply, 0);
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

        public static IPublicAccountCallbackAPI asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPublicAccountCallbackAPI)) {
                return new Proxy(obj);
            }
            return (IPublicAccountCallbackAPI) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _arg0;
            PublicAccounts _arg1;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    respGetPreMessage(data.readInt() != 0, data.createTypedArrayList(MsgContent.CREATOR));
                    reply.writeNoException();
                    return true;
                case 2:
                    MenuInfoMode _arg12;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt() != 0;
                    if (data.readInt() != 0) {
                        _arg12 = (MenuInfoMode) MenuInfoMode.CREATOR.createFromParcel(data);
                    } else {
                        _arg12 = null;
                    }
                    respGetPublicMenuInfo(_arg0, _arg12);
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    respGetUserSubscribePublicList(data.readInt() != 0, data.createTypedArrayList(PublicAccounts.CREATOR));
                    reply.writeNoException();
                    return true;
                case 4:
                    PublicAccountsDetail _arg13;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt() != 0;
                    if (data.readInt() != 0) {
                        _arg13 = (PublicAccountsDetail) PublicAccountsDetail.CREATOR.createFromParcel(data);
                    } else {
                        _arg13 = null;
                    }
                    respGetPublicDetail(_arg0, _arg13);
                    reply.writeNoException();
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    respGetPublicList(data.readInt() != 0, data.createTypedArrayList(PublicAccounts.CREATOR));
                    reply.writeNoException();
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt() != 0;
                    if (data.readInt() != 0) {
                        _arg1 = (PublicAccounts) PublicAccounts.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    respAddSubscribeAccount(_arg0, _arg1);
                    reply.writeNoException();
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt() != 0;
                    if (data.readInt() != 0) {
                        _arg1 = (PublicAccounts) PublicAccounts.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    respCancelSubscribeAccount(_arg0, _arg1);
                    reply.writeNoException();
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt() != 0;
                    if (data.readInt() != 0) {
                        _arg1 = (PublicAccounts) PublicAccounts.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    respComplainPublicAccount(_arg0, _arg1);
                    reply.writeNoException();
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    respGetPublicRecommend(data.readInt() != 0, data.createTypedArrayList(PublicAccounts.CREATOR));
                    reply.writeNoException();
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    respSetAcceptStatus(data.readInt() != 0, data.readString());
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

    void respAddSubscribeAccount(boolean z, PublicAccounts publicAccounts) throws RemoteException;

    void respCancelSubscribeAccount(boolean z, PublicAccounts publicAccounts) throws RemoteException;

    void respComplainPublicAccount(boolean z, PublicAccounts publicAccounts) throws RemoteException;

    void respGetPreMessage(boolean z, List<MsgContent> list) throws RemoteException;

    void respGetPublicDetail(boolean z, PublicAccountsDetail publicAccountsDetail) throws RemoteException;

    void respGetPublicList(boolean z, List<PublicAccounts> list) throws RemoteException;

    void respGetPublicMenuInfo(boolean z, MenuInfoMode menuInfoMode) throws RemoteException;

    void respGetPublicRecommend(boolean z, List<PublicAccounts> list) throws RemoteException;

    void respGetUserSubscribePublicList(boolean z, List<PublicAccounts> list) throws RemoteException;

    void respSetAcceptStatus(boolean z, String str) throws RemoteException;
}
