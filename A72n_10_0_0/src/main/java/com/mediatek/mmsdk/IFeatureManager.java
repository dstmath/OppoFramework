package com.mediatek.mmsdk;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IFeatureManager extends IInterface {
    int getEffectFactory(BinderHolder binderHolder) throws RemoteException;

    String getParameter(String str) throws RemoteException;

    int setParameter(String str, String str2) throws RemoteException;

    int setUp(EffectHalVersion effectHalVersion) throws RemoteException;

    int tearDown(EffectHalVersion effectHalVersion) throws RemoteException;

    public static class Default implements IFeatureManager {
        @Override // com.mediatek.mmsdk.IFeatureManager
        public int setParameter(String key, String value) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.mmsdk.IFeatureManager
        public String getParameter(String key) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.mmsdk.IFeatureManager
        public int setUp(EffectHalVersion version) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.mmsdk.IFeatureManager
        public int tearDown(EffectHalVersion version) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.mmsdk.IFeatureManager
        public int getEffectFactory(BinderHolder effectFactory) throws RemoteException {
            return 0;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IFeatureManager {
        private static final String DESCRIPTOR = "com.mediatek.mmsdk.IFeatureManager";
        static final int TRANSACTION_getEffectFactory = 5;
        static final int TRANSACTION_getParameter = 2;
        static final int TRANSACTION_setParameter = 1;
        static final int TRANSACTION_setUp = 3;
        static final int TRANSACTION_tearDown = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IFeatureManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IFeatureManager)) {
                return new Proxy(obj);
            }
            return (IFeatureManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            EffectHalVersion _arg0;
            EffectHalVersion _arg02;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _result = setParameter(data.readString(), data.readString());
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                String _result2 = getParameter(data.readString());
                reply.writeNoException();
                reply.writeString(_result2);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = EffectHalVersion.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                int _result3 = setUp(_arg0);
                reply.writeNoException();
                reply.writeInt(_result3);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = EffectHalVersion.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                int _result4 = tearDown(_arg02);
                reply.writeNoException();
                reply.writeInt(_result4);
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                BinderHolder _arg03 = new BinderHolder();
                int _result5 = getEffectFactory(_arg03);
                reply.writeNoException();
                reply.writeInt(_result5);
                reply.writeInt(1);
                _arg03.writeToParcel(reply, 1);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IFeatureManager {
            public static IFeatureManager sDefaultImpl;
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

            @Override // com.mediatek.mmsdk.IFeatureManager
            public int setParameter(String key, String value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeString(value);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setParameter(key, value);
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

            @Override // com.mediatek.mmsdk.IFeatureManager
            public String getParameter(String key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getParameter(key);
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

            @Override // com.mediatek.mmsdk.IFeatureManager
            public int setUp(EffectHalVersion version) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (version != null) {
                        _data.writeInt(1);
                        version.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setUp(version);
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

            @Override // com.mediatek.mmsdk.IFeatureManager
            public int tearDown(EffectHalVersion version) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (version != null) {
                        _data.writeInt(1);
                        version.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().tearDown(version);
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

            @Override // com.mediatek.mmsdk.IFeatureManager
            public int getEffectFactory(BinderHolder effectFactory) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getEffectFactory(effectFactory);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        effectFactory.readFromParcel(_reply);
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

        public static boolean setDefaultImpl(IFeatureManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IFeatureManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
