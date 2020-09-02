package com.color.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.color.app.IColorAccessControlObserver;
import java.util.List;
import java.util.Map;

public interface IColorAccessControlManager extends IInterface {
    void addAccessControlPassForUser(String str, int i, int i2) throws RemoteException;

    boolean getApplicationAccessControlEnabledAsUser(String str, int i) throws RemoteException;

    Map getPrivacyAppInfo(int i) throws RemoteException;

    boolean isAccessControlPassForUser(String str, int i) throws RemoteException;

    boolean registerAccessControlObserver(IColorAccessControlObserver iColorAccessControlObserver) throws RemoteException;

    void setPrivacyAppsInfoForUser(Map map, boolean z, int i) throws RemoteException;

    boolean unregisterAccessControlObserver(IColorAccessControlObserver iColorAccessControlObserver) throws RemoteException;

    void updateRusList(int i, List<String> list, List<String> list2) throws RemoteException;

    public static class Default implements IColorAccessControlManager {
        @Override // com.color.app.IColorAccessControlManager
        public void setPrivacyAppsInfoForUser(Map privacyInfo, boolean enabled, int userId) throws RemoteException {
        }

        @Override // com.color.app.IColorAccessControlManager
        public boolean getApplicationAccessControlEnabledAsUser(String packageName, int userId) throws RemoteException {
            return false;
        }

        @Override // com.color.app.IColorAccessControlManager
        public void addAccessControlPassForUser(String packageName, int windowMode, int userId) throws RemoteException {
        }

        @Override // com.color.app.IColorAccessControlManager
        public void updateRusList(int type, List<String> list, List<String> list2) throws RemoteException {
        }

        @Override // com.color.app.IColorAccessControlManager
        public Map getPrivacyAppInfo(int userId) throws RemoteException {
            return null;
        }

        @Override // com.color.app.IColorAccessControlManager
        public boolean isAccessControlPassForUser(String packageName, int userId) throws RemoteException {
            return false;
        }

        @Override // com.color.app.IColorAccessControlManager
        public boolean registerAccessControlObserver(IColorAccessControlObserver observer) throws RemoteException {
            return false;
        }

        @Override // com.color.app.IColorAccessControlManager
        public boolean unregisterAccessControlObserver(IColorAccessControlObserver observer) throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IColorAccessControlManager {
        private static final String DESCRIPTOR = "com.color.app.IColorAccessControlManager";
        static final int TRANSACTION_addAccessControlPassForUser = 3;
        static final int TRANSACTION_getApplicationAccessControlEnabledAsUser = 2;
        static final int TRANSACTION_getPrivacyAppInfo = 5;
        static final int TRANSACTION_isAccessControlPassForUser = 6;
        static final int TRANSACTION_registerAccessControlObserver = 7;
        static final int TRANSACTION_setPrivacyAppsInfoForUser = 1;
        static final int TRANSACTION_unregisterAccessControlObserver = 8;
        static final int TRANSACTION_updateRusList = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IColorAccessControlManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IColorAccessControlManager)) {
                return new Proxy(obj);
            }
            return (IColorAccessControlManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "setPrivacyAppsInfoForUser";
                case 2:
                    return "getApplicationAccessControlEnabledAsUser";
                case 3:
                    return "addAccessControlPassForUser";
                case 4:
                    return "updateRusList";
                case 5:
                    return "getPrivacyAppInfo";
                case 6:
                    return "isAccessControlPassForUser";
                case 7:
                    return "registerAccessControlObserver";
                case 8:
                    return "unregisterAccessControlObserver";
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
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        setPrivacyAppsInfoForUser(data.readHashMap(getClass().getClassLoader()), data.readInt() != 0, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        boolean applicationAccessControlEnabledAsUser = getApplicationAccessControlEnabledAsUser(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(applicationAccessControlEnabledAsUser ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        addAccessControlPassForUser(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        updateRusList(data.readInt(), data.createStringArrayList(), data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        Map _result = getPrivacyAppInfo(data.readInt());
                        reply.writeNoException();
                        reply.writeMap(_result);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isAccessControlPassForUser = isAccessControlPassForUser(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isAccessControlPassForUser ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerAccessControlObserver = registerAccessControlObserver(IColorAccessControlObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerAccessControlObserver ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        boolean unregisterAccessControlObserver = unregisterAccessControlObserver(IColorAccessControlObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(unregisterAccessControlObserver ? 1 : 0);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IColorAccessControlManager {
            public static IColorAccessControlManager sDefaultImpl;
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

            @Override // com.color.app.IColorAccessControlManager
            public void setPrivacyAppsInfoForUser(Map privacyInfo, boolean enabled, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeMap(privacyInfo);
                    _data.writeInt(enabled ? 1 : 0);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setPrivacyAppsInfoForUser(privacyInfo, enabled, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.color.app.IColorAccessControlManager
            public boolean getApplicationAccessControlEnabledAsUser(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getApplicationAccessControlEnabledAsUser(packageName, userId);
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

            @Override // com.color.app.IColorAccessControlManager
            public void addAccessControlPassForUser(String packageName, int windowMode, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(windowMode);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addAccessControlPassForUser(packageName, windowMode, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.color.app.IColorAccessControlManager
            public void updateRusList(int type, List<String> addList, List<String> deleteList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeStringList(addList);
                    _data.writeStringList(deleteList);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateRusList(type, addList, deleteList);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.color.app.IColorAccessControlManager
            public Map getPrivacyAppInfo(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPrivacyAppInfo(userId);
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

            @Override // com.color.app.IColorAccessControlManager
            public boolean isAccessControlPassForUser(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isAccessControlPassForUser(packageName, userId);
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

            @Override // com.color.app.IColorAccessControlManager
            public boolean registerAccessControlObserver(IColorAccessControlObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerAccessControlObserver(observer);
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

            @Override // com.color.app.IColorAccessControlManager
            public boolean unregisterAccessControlObserver(IColorAccessControlObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unregisterAccessControlObserver(observer);
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

        public static boolean setDefaultImpl(IColorAccessControlManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IColorAccessControlManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
