package com.mediatek.common.voicecommand;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IVoiceCommandListener extends IInterface {
    void onVoiceCommandNotified(int i, int i2, Bundle bundle) throws RemoteException;

    public static class Default implements IVoiceCommandListener {
        @Override // com.mediatek.common.voicecommand.IVoiceCommandListener
        public void onVoiceCommandNotified(int mainAction, int subAction, Bundle extraData) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IVoiceCommandListener {
        private static final String DESCRIPTOR = "com.mediatek.common.voicecommand.IVoiceCommandListener";
        static final int TRANSACTION_onVoiceCommandNotified = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IVoiceCommandListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVoiceCommandListener)) {
                return new Proxy(obj);
            }
            return (IVoiceCommandListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg2;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _arg0 = data.readInt();
                int _arg1 = data.readInt();
                if (data.readInt() != 0) {
                    _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg2 = null;
                }
                onVoiceCommandNotified(_arg0, _arg1, _arg2);
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IVoiceCommandListener {
            public static IVoiceCommandListener sDefaultImpl;
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

            @Override // com.mediatek.common.voicecommand.IVoiceCommandListener
            public void onVoiceCommandNotified(int mainAction, int subAction, Bundle extraData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mainAction);
                    _data.writeInt(subAction);
                    if (extraData != null) {
                        _data.writeInt(1);
                        extraData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onVoiceCommandNotified(mainAction, subAction, extraData);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IVoiceCommandListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IVoiceCommandListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
