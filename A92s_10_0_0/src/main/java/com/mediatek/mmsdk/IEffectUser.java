package com.mediatek.mmsdk;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.mediatek.mmsdk.IEffectUpdateListener;
import com.mediatek.mmsdk.IMemory;

public interface IEffectUser extends IInterface {
    boolean apply(ImageInfo imageInfo, IMemory iMemory, ImageInfo imageInfo2, IMemory iMemory2) throws RemoteException;

    String getName() throws RemoteException;

    boolean release() throws RemoteException;

    boolean setParameter(String str, int i) throws RemoteException;

    void setUpdateListener(IEffectUpdateListener iEffectUpdateListener) throws RemoteException;

    public static class Default implements IEffectUser {
        @Override // com.mediatek.mmsdk.IEffectUser
        public String getName() throws RemoteException {
            return null;
        }

        @Override // com.mediatek.mmsdk.IEffectUser
        public boolean apply(ImageInfo rSrcImage, IMemory srcData, ImageInfo rDestImage, IMemory destData) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.mmsdk.IEffectUser
        public boolean setParameter(String parameterKey, int value) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.mmsdk.IEffectUser
        public void setUpdateListener(IEffectUpdateListener listener) throws RemoteException {
        }

        @Override // com.mediatek.mmsdk.IEffectUser
        public boolean release() throws RemoteException {
            return false;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IEffectUser {
        private static final String DESCRIPTOR = "com.mediatek.mmsdk.IEffectUser";
        static final int TRANSACTION_apply = 2;
        static final int TRANSACTION_getName = 1;
        static final int TRANSACTION_release = 5;
        static final int TRANSACTION_setParameter = 3;
        static final int TRANSACTION_setUpdateListener = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IEffectUser asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IEffectUser)) {
                return new Proxy(obj);
            }
            return (IEffectUser) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ImageInfo _arg0;
            ImageInfo _arg2;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                String _result = getName();
                reply.writeNoException();
                reply.writeString(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = ImageInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                IMemory _arg1 = IMemory.Stub.asInterface(data.readStrongBinder());
                if (data.readInt() != 0) {
                    _arg2 = ImageInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg2 = null;
                }
                boolean apply = apply(_arg0, _arg1, _arg2, IMemory.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(apply ? 1 : 0);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                boolean parameter = setParameter(data.readString(), data.readInt());
                reply.writeNoException();
                reply.writeInt(parameter ? 1 : 0);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                setUpdateListener(IEffectUpdateListener.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                boolean release = release();
                reply.writeNoException();
                reply.writeInt(release ? 1 : 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IEffectUser {
            public static IEffectUser sDefaultImpl;
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

            @Override // com.mediatek.mmsdk.IEffectUser
            public String getName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getName();
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

            @Override // com.mediatek.mmsdk.IEffectUser
            public boolean apply(ImageInfo rSrcImage, IMemory srcData, ImageInfo rDestImage, IMemory destData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (rSrcImage != null) {
                        _data.writeInt(1);
                        rSrcImage.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    IBinder iBinder = null;
                    _data.writeStrongBinder(srcData != null ? srcData.asBinder() : null);
                    if (rDestImage != null) {
                        _data.writeInt(1);
                        rDestImage.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (destData != null) {
                        iBinder = destData.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().apply(rSrcImage, srcData, rDestImage, destData);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.mmsdk.IEffectUser
            public boolean setParameter(String parameterKey, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(parameterKey);
                    _data.writeInt(value);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setParameter(parameterKey, value);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.mmsdk.IEffectUser
            public void setUpdateListener(IEffectUpdateListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setUpdateListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.mmsdk.IEffectUser
            public boolean release() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().release();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
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

        public static boolean setDefaultImpl(IEffectUser impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IEffectUser getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
