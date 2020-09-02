package com.mediatek.common.voicecommand;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.mediatek.common.voicecommand.IVoiceCommandListener;

public interface IVoiceCommandManagerService extends IInterface {
    int registerListener(String str, IVoiceCommandListener iVoiceCommandListener) throws RemoteException;

    int sendCommand(String str, int i, int i2, Bundle bundle) throws RemoteException;

    int unregisterListener(String str, IVoiceCommandListener iVoiceCommandListener) throws RemoteException;

    public static class Default implements IVoiceCommandManagerService {
        @Override // com.mediatek.common.voicecommand.IVoiceCommandManagerService
        public int registerListener(String pkgName, IVoiceCommandListener listener) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.common.voicecommand.IVoiceCommandManagerService
        public int unregisterListener(String pkgName, IVoiceCommandListener listener) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.common.voicecommand.IVoiceCommandManagerService
        public int sendCommand(String pkgName, int mainAction, int subAction, Bundle extraData) throws RemoteException {
            return 0;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IVoiceCommandManagerService {
        private static final String DESCRIPTOR = "com.mediatek.common.voicecommand.IVoiceCommandManagerService";
        static final int TRANSACTION_registerListener = 1;
        static final int TRANSACTION_sendCommand = 3;
        static final int TRANSACTION_unregisterListener = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IVoiceCommandManagerService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVoiceCommandManagerService)) {
                return new Proxy(obj);
            }
            return (IVoiceCommandManagerService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg3;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _result = registerListener(data.readString(), IVoiceCommandListener.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int _result2 = unregisterListener(data.readString(), IVoiceCommandListener.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(_result2);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                String _arg0 = data.readString();
                int _arg1 = data.readInt();
                int _arg2 = data.readInt();
                if (data.readInt() != 0) {
                    _arg3 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg3 = null;
                }
                int _result3 = sendCommand(_arg0, _arg1, _arg2, _arg3);
                reply.writeNoException();
                reply.writeInt(_result3);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IVoiceCommandManagerService {
            public static IVoiceCommandManagerService sDefaultImpl;
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

            @Override // com.mediatek.common.voicecommand.IVoiceCommandManagerService
            public int registerListener(String pkgName, IVoiceCommandListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerListener(pkgName, listener);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.voicecommand.IVoiceCommandManagerService
            public int unregisterListener(String pkgName, IVoiceCommandListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unregisterListener(pkgName, listener);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.voicecommand.IVoiceCommandManagerService
            public int sendCommand(String pkgName, int mainAction, int subAction, Bundle extraData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(mainAction);
                    _data.writeInt(subAction);
                    if (extraData != null) {
                        _data.writeInt(1);
                        extraData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().sendCommand(pkgName, mainAction, subAction, extraData);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IVoiceCommandManagerService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IVoiceCommandManagerService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
