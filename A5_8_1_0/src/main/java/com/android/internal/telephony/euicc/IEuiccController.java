package com.android.internal.telephony.euicc;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telephony.euicc.DownloadableSubscription;
import android.telephony.euicc.EuiccInfo;

public interface IEuiccController extends IInterface {

    public static abstract class Stub extends Binder implements IEuiccController {
        private static final String DESCRIPTOR = "com.android.internal.telephony.euicc.IEuiccController";
        static final int TRANSACTION_continueOperation = 1;
        static final int TRANSACTION_deleteSubscription = 7;
        static final int TRANSACTION_downloadSubscription = 5;
        static final int TRANSACTION_eraseSubscriptions = 10;
        static final int TRANSACTION_getDefaultDownloadableSubscriptionList = 3;
        static final int TRANSACTION_getDownloadableSubscriptionMetadata = 2;
        static final int TRANSACTION_getEid = 4;
        static final int TRANSACTION_getEuiccInfo = 6;
        static final int TRANSACTION_retainSubscriptionsForFactoryReset = 11;
        static final int TRANSACTION_switchToSubscription = 8;
        static final int TRANSACTION_updateSubscriptionNickname = 9;

        private static class Proxy implements IEuiccController {
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

            public void continueOperation(Intent resolutionIntent, Bundle resolutionExtras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (resolutionIntent != null) {
                        _data.writeInt(1);
                        resolutionIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (resolutionExtras != null) {
                        _data.writeInt(1);
                        resolutionExtras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void getDownloadableSubscriptionMetadata(DownloadableSubscription subscription, String callingPackage, PendingIntent callbackIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (subscription != null) {
                        _data.writeInt(1);
                        subscription.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    if (callbackIntent != null) {
                        _data.writeInt(1);
                        callbackIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void getDefaultDownloadableSubscriptionList(String callingPackage, PendingIntent callbackIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    if (callbackIntent != null) {
                        _data.writeInt(1);
                        callbackIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public String getEid() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void downloadSubscription(DownloadableSubscription subscription, boolean switchAfterDownload, String callingPackage, PendingIntent callbackIntent) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (subscription != null) {
                        _data.writeInt(1);
                        subscription.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!switchAfterDownload) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeString(callingPackage);
                    if (callbackIntent != null) {
                        _data.writeInt(1);
                        callbackIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public EuiccInfo getEuiccInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    EuiccInfo _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (EuiccInfo) EuiccInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deleteSubscription(int subscriptionId, String callingPackage, PendingIntent callbackIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subscriptionId);
                    _data.writeString(callingPackage);
                    if (callbackIntent != null) {
                        _data.writeInt(1);
                        callbackIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void switchToSubscription(int subscriptionId, String callingPackage, PendingIntent callbackIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subscriptionId);
                    _data.writeString(callingPackage);
                    if (callbackIntent != null) {
                        _data.writeInt(1);
                        callbackIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void updateSubscriptionNickname(int subscriptionId, String nickname, PendingIntent callbackIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subscriptionId);
                    _data.writeString(nickname);
                    if (callbackIntent != null) {
                        _data.writeInt(1);
                        callbackIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void eraseSubscriptions(PendingIntent callbackIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callbackIntent != null) {
                        _data.writeInt(1);
                        callbackIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void retainSubscriptionsForFactoryReset(PendingIntent callbackIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callbackIntent != null) {
                        _data.writeInt(1);
                        callbackIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(11, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IEuiccController asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IEuiccController)) {
                return new Proxy(obj);
            }
            return (IEuiccController) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            DownloadableSubscription _arg0;
            String _arg1;
            PendingIntent _arg2;
            int _arg02;
            PendingIntent _arg03;
            switch (code) {
                case 1:
                    Intent _arg04;
                    Bundle _arg12;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg04 = (Intent) Intent.CREATOR.createFromParcel(data);
                    } else {
                        _arg04 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg12 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg12 = null;
                    }
                    continueOperation(_arg04, _arg12);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (DownloadableSubscription) DownloadableSubscription.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        _arg2 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    getDownloadableSubscriptionMetadata(_arg0, _arg1, _arg2);
                    return true;
                case 3:
                    PendingIntent _arg13;
                    data.enforceInterface(DESCRIPTOR);
                    String _arg05 = data.readString();
                    if (data.readInt() != 0) {
                        _arg13 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        _arg13 = null;
                    }
                    getDefaultDownloadableSubscriptionList(_arg05, _arg13);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    String _result = getEid();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case 5:
                    PendingIntent _arg3;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (DownloadableSubscription) DownloadableSubscription.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    boolean _arg14 = data.readInt() != 0;
                    String _arg22 = data.readString();
                    if (data.readInt() != 0) {
                        _arg3 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        _arg3 = null;
                    }
                    downloadSubscription(_arg0, _arg14, _arg22, _arg3);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    EuiccInfo _result2 = getEuiccInfo();
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(1);
                        _result2.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = data.readInt();
                    _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        _arg2 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    deleteSubscription(_arg02, _arg1, _arg2);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = data.readInt();
                    _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        _arg2 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    switchToSubscription(_arg02, _arg1, _arg2);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = data.readInt();
                    _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        _arg2 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    updateSubscriptionNickname(_arg02, _arg1, _arg2);
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg03 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        _arg03 = null;
                    }
                    eraseSubscriptions(_arg03);
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg03 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    } else {
                        _arg03 = null;
                    }
                    retainSubscriptionsForFactoryReset(_arg03);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void continueOperation(Intent intent, Bundle bundle) throws RemoteException;

    void deleteSubscription(int i, String str, PendingIntent pendingIntent) throws RemoteException;

    void downloadSubscription(DownloadableSubscription downloadableSubscription, boolean z, String str, PendingIntent pendingIntent) throws RemoteException;

    void eraseSubscriptions(PendingIntent pendingIntent) throws RemoteException;

    void getDefaultDownloadableSubscriptionList(String str, PendingIntent pendingIntent) throws RemoteException;

    void getDownloadableSubscriptionMetadata(DownloadableSubscription downloadableSubscription, String str, PendingIntent pendingIntent) throws RemoteException;

    String getEid() throws RemoteException;

    EuiccInfo getEuiccInfo() throws RemoteException;

    void retainSubscriptionsForFactoryReset(PendingIntent pendingIntent) throws RemoteException;

    void switchToSubscription(int i, String str, PendingIntent pendingIntent) throws RemoteException;

    void updateSubscriptionNickname(int i, String str, PendingIntent pendingIntent) throws RemoteException;
}
