package android.operator;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.Map;

public interface IOppoOperatorManager extends IInterface {
    Map getConfigMap(Bundle bundle) throws RemoteException;

    void grantCustomizedRuntimePermissions() throws RemoteException;

    boolean hasFeatureDynamiclyEnabeld(String str) throws RemoteException;

    boolean isDynamicFeatureEnabled() throws RemoteException;

    boolean isInSimTriggeredSystemBlackList(String str) throws RemoteException;

    void notifySmartCustomizationStart() throws RemoteException;

    void testAidl() throws RemoteException;

    public static class Default implements IOppoOperatorManager {
        @Override // android.operator.IOppoOperatorManager
        public void testAidl() throws RemoteException {
        }

        @Override // android.operator.IOppoOperatorManager
        public Map getConfigMap(Bundle bundle) throws RemoteException {
            return null;
        }

        @Override // android.operator.IOppoOperatorManager
        public void grantCustomizedRuntimePermissions() throws RemoteException {
        }

        @Override // android.operator.IOppoOperatorManager
        public boolean isDynamicFeatureEnabled() throws RemoteException {
            return false;
        }

        @Override // android.operator.IOppoOperatorManager
        public void notifySmartCustomizationStart() throws RemoteException {
        }

        @Override // android.operator.IOppoOperatorManager
        public boolean hasFeatureDynamiclyEnabeld(String name) throws RemoteException {
            return false;
        }

        @Override // android.operator.IOppoOperatorManager
        public boolean isInSimTriggeredSystemBlackList(String pkgName) throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOppoOperatorManager {
        private static final String DESCRIPTOR = "android.operator.IOppoOperatorManager";
        static final int TRANSACTION_getConfigMap = 2;
        static final int TRANSACTION_grantCustomizedRuntimePermissions = 3;
        static final int TRANSACTION_hasFeatureDynamiclyEnabeld = 6;
        static final int TRANSACTION_isDynamicFeatureEnabled = 4;
        static final int TRANSACTION_isInSimTriggeredSystemBlackList = 7;
        static final int TRANSACTION_notifySmartCustomizationStart = 5;
        static final int TRANSACTION_testAidl = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOppoOperatorManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOppoOperatorManager)) {
                return new Proxy(obj);
            }
            return (IOppoOperatorManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "testAidl";
                case 2:
                    return "getConfigMap";
                case 3:
                    return "grantCustomizedRuntimePermissions";
                case 4:
                    return "isDynamicFeatureEnabled";
                case 5:
                    return "notifySmartCustomizationStart";
                case 6:
                    return "hasFeatureDynamiclyEnabeld";
                case 7:
                    return "isInSimTriggeredSystemBlackList";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg0;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        testAidl();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        Map _result = getConfigMap(_arg0);
                        reply.writeNoException();
                        reply.writeMap(_result);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        grantCustomizedRuntimePermissions();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isDynamicFeatureEnabled = isDynamicFeatureEnabled();
                        reply.writeNoException();
                        reply.writeInt(isDynamicFeatureEnabled ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        notifySmartCustomizationStart();
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean hasFeatureDynamiclyEnabeld = hasFeatureDynamiclyEnabeld(data.readString());
                        reply.writeNoException();
                        reply.writeInt(hasFeatureDynamiclyEnabeld ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isInSimTriggeredSystemBlackList = isInSimTriggeredSystemBlackList(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isInSimTriggeredSystemBlackList ? 1 : 0);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IOppoOperatorManager {
            public static IOppoOperatorManager sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // android.operator.IOppoOperatorManager
            public void testAidl() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().testAidl();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.operator.IOppoOperatorManager
            public Map getConfigMap(Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getConfigMap(bundle);
                    }
                    _reply.readException();
                    Map _result = _reply.readHashMap(getClass().getClassLoader());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.operator.IOppoOperatorManager
            public void grantCustomizedRuntimePermissions() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().grantCustomizedRuntimePermissions();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.operator.IOppoOperatorManager
            public boolean isDynamicFeatureEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isDynamicFeatureEnabled();
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

            @Override // android.operator.IOppoOperatorManager
            public void notifySmartCustomizationStart() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifySmartCustomizationStart();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.operator.IOppoOperatorManager
            public boolean hasFeatureDynamiclyEnabeld(String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    boolean _result = false;
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasFeatureDynamiclyEnabeld(name);
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

            @Override // android.operator.IOppoOperatorManager
            public boolean isInSimTriggeredSystemBlackList(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    boolean _result = false;
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isInSimTriggeredSystemBlackList(pkgName);
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

        public static boolean setDefaultImpl(IOppoOperatorManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOppoOperatorManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
