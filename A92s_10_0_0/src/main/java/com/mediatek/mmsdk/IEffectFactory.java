package com.mediatek.mmsdk;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.ArrayList;
import java.util.List;

public interface IEffectFactory extends IInterface {
    int createCallbackClient(EffectHalVersion effectHalVersion, BinderHolder binderHolder) throws RemoteException;

    int createEffectHal(EffectHalVersion effectHalVersion, BinderHolder binderHolder) throws RemoteException;

    int createEffectHalClient(EffectHalVersion effectHalVersion, BinderHolder binderHolder) throws RemoteException;

    int getAllSupportedEffectHal(List<String> list) throws RemoteException;

    int getSupportedVersion(String str, List<EffectHalVersion> list) throws RemoteException;

    public static class Default implements IEffectFactory {
        @Override // com.mediatek.mmsdk.IEffectFactory
        public int createCallbackClient(EffectHalVersion nameVersion, BinderHolder callbackClient) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.mmsdk.IEffectFactory
        public int createEffectHal(EffectHalVersion nameVersion, BinderHolder effectHal) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.mmsdk.IEffectFactory
        public int createEffectHalClient(EffectHalVersion nameVersion, BinderHolder effectHalClient) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.mmsdk.IEffectFactory
        public int getSupportedVersion(String effectName, List<EffectHalVersion> list) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.mmsdk.IEffectFactory
        public int getAllSupportedEffectHal(List<String> list) throws RemoteException {
            return 0;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IEffectFactory {
        private static final String DESCRIPTOR = "com.mediatek.mmsdk.IEffectFactory";
        static final int TRANSACTION_createCallbackClient = 1;
        static final int TRANSACTION_createEffectHal = 2;
        static final int TRANSACTION_createEffectHalClient = 3;
        static final int TRANSACTION_getAllSupportedEffectHal = 5;
        static final int TRANSACTION_getSupportedVersion = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IEffectFactory asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IEffectFactory)) {
                return new Proxy(obj);
            }
            return (IEffectFactory) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            EffectHalVersion _arg0;
            EffectHalVersion _arg02;
            EffectHalVersion _arg03;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = EffectHalVersion.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                BinderHolder _arg1 = new BinderHolder();
                int _result = createCallbackClient(_arg0, _arg1);
                reply.writeNoException();
                reply.writeInt(_result);
                reply.writeInt(1);
                _arg1.writeToParcel(reply, 1);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = EffectHalVersion.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                BinderHolder _arg12 = new BinderHolder();
                int _result2 = createEffectHal(_arg02, _arg12);
                reply.writeNoException();
                reply.writeInt(_result2);
                reply.writeInt(1);
                _arg12.writeToParcel(reply, 1);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg03 = EffectHalVersion.CREATOR.createFromParcel(data);
                } else {
                    _arg03 = null;
                }
                BinderHolder _arg13 = new BinderHolder();
                int _result3 = createEffectHalClient(_arg03, _arg13);
                reply.writeNoException();
                reply.writeInt(_result3);
                reply.writeInt(1);
                _arg13.writeToParcel(reply, 1);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                String _arg04 = data.readString();
                ArrayList arrayList = new ArrayList();
                int _result4 = getSupportedVersion(_arg04, arrayList);
                reply.writeNoException();
                reply.writeInt(_result4);
                reply.writeTypedList(arrayList);
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                List<String> _arg05 = new ArrayList<>();
                int _result5 = getAllSupportedEffectHal(_arg05);
                reply.writeNoException();
                reply.writeInt(_result5);
                reply.writeStringList(_arg05);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IEffectFactory {
            public static IEffectFactory sDefaultImpl;
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

            @Override // com.mediatek.mmsdk.IEffectFactory
            public int createCallbackClient(EffectHalVersion nameVersion, BinderHolder callbackClient) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (nameVersion != null) {
                        _data.writeInt(1);
                        nameVersion.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createCallbackClient(nameVersion, callbackClient);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        callbackClient.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.mmsdk.IEffectFactory
            public int createEffectHal(EffectHalVersion nameVersion, BinderHolder effectHal) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (nameVersion != null) {
                        _data.writeInt(1);
                        nameVersion.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createEffectHal(nameVersion, effectHal);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        effectHal.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.mmsdk.IEffectFactory
            public int createEffectHalClient(EffectHalVersion nameVersion, BinderHolder effectHalClient) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (nameVersion != null) {
                        _data.writeInt(1);
                        nameVersion.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createEffectHalClient(nameVersion, effectHalClient);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        effectHalClient.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.mmsdk.IEffectFactory
            public int getSupportedVersion(String effectName, List<EffectHalVersion> versions) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(effectName);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSupportedVersion(effectName, versions);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readTypedList(versions, EffectHalVersion.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.mmsdk.IEffectFactory
            public int getAllSupportedEffectHal(List<String> version) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAllSupportedEffectHal(version);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readStringList(version);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IEffectFactory impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IEffectFactory getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
