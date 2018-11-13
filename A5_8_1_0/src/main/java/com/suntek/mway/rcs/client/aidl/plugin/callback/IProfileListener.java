package com.suntek.mway.rcs.client.aidl.plugin.callback;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.suntek.mway.rcs.client.aidl.plugin.entity.profile.Avatar;
import com.suntek.mway.rcs.client.aidl.plugin.entity.profile.Profile;
import com.suntek.mway.rcs.client.aidl.plugin.entity.profile.QRCardImg;
import com.suntek.mway.rcs.client.aidl.plugin.entity.profile.QRCardInfo;

public interface IProfileListener extends IInterface {

    public static abstract class Stub extends Binder implements IProfileListener {
        private static final String DESCRIPTOR = "com.suntek.mway.rcs.client.aidl.plugin.callback.IProfileListener";
        static final int TRANSACTION_onAvatarGet = 3;
        static final int TRANSACTION_onAvatarUpdated = 2;
        static final int TRANSACTION_onProfileGet = 4;
        static final int TRANSACTION_onProfileUpdated = 1;
        static final int TRANSACTION_onQRImgDecode = 6;
        static final int TRANSACTION_onQRImgGet = 5;

        private static class Proxy implements IProfileListener {
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

            public void onProfileUpdated(int resultCode, String resultDesc) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(resultCode);
                    _data.writeString(resultDesc);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onAvatarUpdated(int resultCode, String resultDesc) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(resultCode);
                    _data.writeString(resultDesc);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onAvatarGet(Avatar avatar, int resultCode, String resultDesc) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (avatar != null) {
                        _data.writeInt(1);
                        avatar.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(resultCode);
                    _data.writeString(resultDesc);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onProfileGet(Profile profile, int resultCode, String resultDesc) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (profile != null) {
                        _data.writeInt(1);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(resultCode);
                    _data.writeString(resultDesc);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onQRImgGet(QRCardImg qrImgObj, int resultCode, String resultDesc) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (qrImgObj != null) {
                        _data.writeInt(1);
                        qrImgObj.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(resultCode);
                    _data.writeString(resultDesc);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onQRImgDecode(QRCardInfo qrCardInfo, int resultCode, String resultDesc) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (qrCardInfo != null) {
                        _data.writeInt(1);
                        qrCardInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(resultCode);
                    _data.writeString(resultDesc);
                    this.mRemote.transact(6, _data, _reply, 0);
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

        public static IProfileListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IProfileListener)) {
                return new Proxy(obj);
            }
            return (IProfileListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    onProfileUpdated(data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    onAvatarUpdated(data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case 3:
                    Avatar _arg0;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (Avatar) Avatar.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    onAvatarGet(_arg0, data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case 4:
                    Profile _arg02;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg02 = (Profile) Profile.CREATOR.createFromParcel(data);
                    } else {
                        _arg02 = null;
                    }
                    onProfileGet(_arg02, data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case 5:
                    QRCardImg _arg03;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg03 = (QRCardImg) QRCardImg.CREATOR.createFromParcel(data);
                    } else {
                        _arg03 = null;
                    }
                    onQRImgGet(_arg03, data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case 6:
                    QRCardInfo _arg04;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg04 = (QRCardInfo) QRCardInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg04 = null;
                    }
                    onQRImgDecode(_arg04, data.readInt(), data.readString());
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

    void onAvatarGet(Avatar avatar, int i, String str) throws RemoteException;

    void onAvatarUpdated(int i, String str) throws RemoteException;

    void onProfileGet(Profile profile, int i, String str) throws RemoteException;

    void onProfileUpdated(int i, String str) throws RemoteException;

    void onQRImgDecode(QRCardInfo qRCardInfo, int i, String str) throws RemoteException;

    void onQRImgGet(QRCardImg qRCardImg, int i, String str) throws RemoteException;
}
