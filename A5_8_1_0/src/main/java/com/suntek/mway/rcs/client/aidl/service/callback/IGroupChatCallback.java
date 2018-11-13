package com.suntek.mway.rcs.client.aidl.service.callback;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.suntek.mway.rcs.client.aidl.plugin.entity.profile.Avatar;

public interface IGroupChatCallback extends IInterface {

    public static abstract class Stub extends Binder implements IGroupChatCallback {
        private static final String DESCRIPTOR = "com.suntek.mway.rcs.client.aidl.service.callback.IGroupChatCallback";
        static final int TRANSACTION_onUpdateAvatar = 1;

        private static class Proxy implements IGroupChatCallback {
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

            public void onUpdateAvatar(Avatar avatar, int resultCode, String resultDesc) throws RemoteException {
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
                    this.mRemote.transact(1, _data, _reply, 0);
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

        public static IGroupChatCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGroupChatCallback)) {
                return new Proxy(obj);
            }
            return (IGroupChatCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    Avatar _arg0;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (Avatar) Avatar.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    onUpdateAvatar(_arg0, data.readInt(), data.readString());
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

    void onUpdateAvatar(Avatar avatar, int i, String str) throws RemoteException;
}
