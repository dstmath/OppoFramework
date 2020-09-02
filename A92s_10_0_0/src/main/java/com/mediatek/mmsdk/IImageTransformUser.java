package com.mediatek.mmsdk;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.mediatek.mmsdk.IMemory;

public interface IImageTransformUser extends IInterface {
    boolean applyTransform(ImageInfo imageInfo, IMemory iMemory, ImageInfo imageInfo2, IMemory iMemory2) throws RemoteException;

    String getName() throws RemoteException;

    public static class Default implements IImageTransformUser {
        @Override // com.mediatek.mmsdk.IImageTransformUser
        public String getName() throws RemoteException {
            return null;
        }

        @Override // com.mediatek.mmsdk.IImageTransformUser
        public boolean applyTransform(ImageInfo rSrcImage, IMemory srcData, ImageInfo rDestImage, IMemory destData) throws RemoteException {
            return false;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IImageTransformUser {
        private static final String DESCRIPTOR = "com.mediatek.mmsdk.IImageTransformUser";
        static final int TRANSACTION_applyTransform = 2;
        static final int TRANSACTION_getName = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IImageTransformUser asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IImageTransformUser)) {
                return new Proxy(obj);
            }
            return (IImageTransformUser) iin;
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
                boolean applyTransform = applyTransform(_arg0, _arg1, _arg2, IMemory.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(applyTransform ? 1 : 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IImageTransformUser {
            public static IImageTransformUser sDefaultImpl;
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

            @Override // com.mediatek.mmsdk.IImageTransformUser
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

            @Override // com.mediatek.mmsdk.IImageTransformUser
            public boolean applyTransform(ImageInfo rSrcImage, IMemory srcData, ImageInfo rDestImage, IMemory destData) throws RemoteException {
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
                        return Stub.getDefaultImpl().applyTransform(rSrcImage, srcData, rDestImage, destData);
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
        }

        public static boolean setDefaultImpl(IImageTransformUser impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IImageTransformUser getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
