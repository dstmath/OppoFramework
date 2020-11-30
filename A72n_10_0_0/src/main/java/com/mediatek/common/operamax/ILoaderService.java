package com.mediatek.common.operamax;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.mediatek.common.operamax.ILoaderStateListener;

public interface ILoaderService extends IInterface {
    void addDirectedApp(String str) throws RemoteException;

    void addDirectedHeaderField(String str, String str2) throws RemoteException;

    void addDirectedHost(String str) throws RemoteException;

    int getCompressLevel() throws RemoteException;

    String[] getDirectedAppList() throws RemoteException;

    String[] getDirectedHeaderFieldList() throws RemoteException;

    String[] getDirectedHostList() throws RemoteException;

    int getSavingState() throws RemoteException;

    int getTunnelState() throws RemoteException;

    boolean isAppDirected(String str) throws RemoteException;

    boolean isHeaderFieldDirected(String str, String str2) throws RemoteException;

    boolean isHostDirected(String str) throws RemoteException;

    void launchOperaMAX() throws RemoteException;

    void registerStateListener(ILoaderStateListener iLoaderStateListener) throws RemoteException;

    void removeAllDirectedApps() throws RemoteException;

    void removeAllDirectedHeaderFields() throws RemoteException;

    void removeAllDirectedHosts() throws RemoteException;

    void removeDirectedApp(String str) throws RemoteException;

    void removeDirectedHeaderField(String str, String str2) throws RemoteException;

    void removeDirectedHost(String str) throws RemoteException;

    void setCompressLevel(int i) throws RemoteException;

    void startSaving() throws RemoteException;

    void stopSaving() throws RemoteException;

    void unregisterStateListener(ILoaderStateListener iLoaderStateListener) throws RemoteException;

    public static class Default implements ILoaderService {
        @Override // com.mediatek.common.operamax.ILoaderService
        public void startSaving() throws RemoteException {
        }

        @Override // com.mediatek.common.operamax.ILoaderService
        public void stopSaving() throws RemoteException {
        }

        @Override // com.mediatek.common.operamax.ILoaderService
        public int getTunnelState() throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.common.operamax.ILoaderService
        public int getSavingState() throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.common.operamax.ILoaderService
        public void registerStateListener(ILoaderStateListener listener) throws RemoteException {
        }

        @Override // com.mediatek.common.operamax.ILoaderService
        public void unregisterStateListener(ILoaderStateListener listener) throws RemoteException {
        }

        @Override // com.mediatek.common.operamax.ILoaderService
        public void launchOperaMAX() throws RemoteException {
        }

        @Override // com.mediatek.common.operamax.ILoaderService
        public void addDirectedApp(String packageName) throws RemoteException {
        }

        @Override // com.mediatek.common.operamax.ILoaderService
        public void removeDirectedApp(String packageName) throws RemoteException {
        }

        @Override // com.mediatek.common.operamax.ILoaderService
        public void removeAllDirectedApps() throws RemoteException {
        }

        @Override // com.mediatek.common.operamax.ILoaderService
        public boolean isAppDirected(String packageName) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.common.operamax.ILoaderService
        public String[] getDirectedAppList() throws RemoteException {
            return null;
        }

        @Override // com.mediatek.common.operamax.ILoaderService
        public void addDirectedHost(String host) throws RemoteException {
        }

        @Override // com.mediatek.common.operamax.ILoaderService
        public void removeDirectedHost(String host) throws RemoteException {
        }

        @Override // com.mediatek.common.operamax.ILoaderService
        public void removeAllDirectedHosts() throws RemoteException {
        }

        @Override // com.mediatek.common.operamax.ILoaderService
        public boolean isHostDirected(String host) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.common.operamax.ILoaderService
        public String[] getDirectedHostList() throws RemoteException {
            return null;
        }

        @Override // com.mediatek.common.operamax.ILoaderService
        public void addDirectedHeaderField(String key, String value) throws RemoteException {
        }

        @Override // com.mediatek.common.operamax.ILoaderService
        public void removeDirectedHeaderField(String key, String value) throws RemoteException {
        }

        @Override // com.mediatek.common.operamax.ILoaderService
        public void removeAllDirectedHeaderFields() throws RemoteException {
        }

        @Override // com.mediatek.common.operamax.ILoaderService
        public boolean isHeaderFieldDirected(String key, String value) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.common.operamax.ILoaderService
        public String[] getDirectedHeaderFieldList() throws RemoteException {
            return null;
        }

        @Override // com.mediatek.common.operamax.ILoaderService
        public void setCompressLevel(int level) throws RemoteException {
        }

        @Override // com.mediatek.common.operamax.ILoaderService
        public int getCompressLevel() throws RemoteException {
            return 0;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ILoaderService {
        private static final String DESCRIPTOR = "com.mediatek.common.operamax.ILoaderService";
        static final int TRANSACTION_addDirectedApp = 8;
        static final int TRANSACTION_addDirectedHeaderField = 18;
        static final int TRANSACTION_addDirectedHost = 13;
        static final int TRANSACTION_getCompressLevel = 24;
        static final int TRANSACTION_getDirectedAppList = 12;
        static final int TRANSACTION_getDirectedHeaderFieldList = 22;
        static final int TRANSACTION_getDirectedHostList = 17;
        static final int TRANSACTION_getSavingState = 4;
        static final int TRANSACTION_getTunnelState = 3;
        static final int TRANSACTION_isAppDirected = 11;
        static final int TRANSACTION_isHeaderFieldDirected = 21;
        static final int TRANSACTION_isHostDirected = 16;
        static final int TRANSACTION_launchOperaMAX = 7;
        static final int TRANSACTION_registerStateListener = 5;
        static final int TRANSACTION_removeAllDirectedApps = 10;
        static final int TRANSACTION_removeAllDirectedHeaderFields = 20;
        static final int TRANSACTION_removeAllDirectedHosts = 15;
        static final int TRANSACTION_removeDirectedApp = 9;
        static final int TRANSACTION_removeDirectedHeaderField = 19;
        static final int TRANSACTION_removeDirectedHost = 14;
        static final int TRANSACTION_setCompressLevel = 23;
        static final int TRANSACTION_startSaving = 1;
        static final int TRANSACTION_stopSaving = 2;
        static final int TRANSACTION_unregisterStateListener = 6;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ILoaderService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ILoaderService)) {
                return new Proxy(obj);
            }
            return (ILoaderService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        startSaving();
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        stopSaving();
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = getTunnelState();
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = getSavingState();
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        registerStateListener(ILoaderStateListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterStateListener(ILoaderStateListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        launchOperaMAX();
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        addDirectedApp(data.readString());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        removeDirectedApp(data.readString());
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        removeAllDirectedApps();
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isAppDirected = isAppDirected(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isAppDirected ? 1 : 0);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result3 = getDirectedAppList();
                        reply.writeNoException();
                        reply.writeStringArray(_result3);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        addDirectedHost(data.readString());
                        reply.writeNoException();
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        removeDirectedHost(data.readString());
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        removeAllDirectedHosts();
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isHostDirected = isHostDirected(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isHostDirected ? 1 : 0);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result4 = getDirectedHostList();
                        reply.writeNoException();
                        reply.writeStringArray(_result4);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        addDirectedHeaderField(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        removeDirectedHeaderField(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_removeAllDirectedHeaderFields /* 20 */:
                        data.enforceInterface(DESCRIPTOR);
                        removeAllDirectedHeaderFields();
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_isHeaderFieldDirected /* 21 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isHeaderFieldDirected = isHeaderFieldDirected(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(isHeaderFieldDirected ? 1 : 0);
                        return true;
                    case TRANSACTION_getDirectedHeaderFieldList /* 22 */:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result5 = getDirectedHeaderFieldList();
                        reply.writeNoException();
                        reply.writeStringArray(_result5);
                        return true;
                    case TRANSACTION_setCompressLevel /* 23 */:
                        data.enforceInterface(DESCRIPTOR);
                        setCompressLevel(data.readInt());
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_getCompressLevel /* 24 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = getCompressLevel();
                        reply.writeNoException();
                        reply.writeInt(_result6);
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
        public static class Proxy implements ILoaderService {
            public static ILoaderService sDefaultImpl;
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

            @Override // com.mediatek.common.operamax.ILoaderService
            public void startSaving() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startSaving();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.operamax.ILoaderService
            public void stopSaving() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().stopSaving();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.operamax.ILoaderService
            public int getTunnelState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTunnelState();
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

            @Override // com.mediatek.common.operamax.ILoaderService
            public int getSavingState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSavingState();
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

            @Override // com.mediatek.common.operamax.ILoaderService
            public void registerStateListener(ILoaderStateListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerStateListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.operamax.ILoaderService
            public void unregisterStateListener(ILoaderStateListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterStateListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.operamax.ILoaderService
            public void launchOperaMAX() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().launchOperaMAX();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.operamax.ILoaderService
            public void addDirectedApp(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addDirectedApp(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.operamax.ILoaderService
            public void removeDirectedApp(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeDirectedApp(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.operamax.ILoaderService
            public void removeAllDirectedApps() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeAllDirectedApps();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.operamax.ILoaderService
            public boolean isAppDirected(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isAppDirected(packageName);
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

            @Override // com.mediatek.common.operamax.ILoaderService
            public String[] getDirectedAppList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDirectedAppList();
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.operamax.ILoaderService
            public void addDirectedHost(String host) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(host);
                    if (this.mRemote.transact(13, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addDirectedHost(host);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.operamax.ILoaderService
            public void removeDirectedHost(String host) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(host);
                    if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeDirectedHost(host);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.operamax.ILoaderService
            public void removeAllDirectedHosts() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(15, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeAllDirectedHosts();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.operamax.ILoaderService
            public boolean isHostDirected(String host) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(host);
                    boolean _result = false;
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isHostDirected(host);
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

            @Override // com.mediatek.common.operamax.ILoaderService
            public String[] getDirectedHostList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDirectedHostList();
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.operamax.ILoaderService
            public void addDirectedHeaderField(String key, String value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeString(value);
                    if (this.mRemote.transact(18, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addDirectedHeaderField(key, value);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.operamax.ILoaderService
            public void removeDirectedHeaderField(String key, String value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeString(value);
                    if (this.mRemote.transact(19, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeDirectedHeaderField(key, value);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.operamax.ILoaderService
            public void removeAllDirectedHeaderFields() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(Stub.TRANSACTION_removeAllDirectedHeaderFields, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeAllDirectedHeaderFields();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.operamax.ILoaderService
            public boolean isHeaderFieldDirected(String key, String value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeString(value);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_isHeaderFieldDirected, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isHeaderFieldDirected(key, value);
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

            @Override // com.mediatek.common.operamax.ILoaderService
            public String[] getDirectedHeaderFieldList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getDirectedHeaderFieldList, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDirectedHeaderFieldList();
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.operamax.ILoaderService
            public void setCompressLevel(int level) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(level);
                    if (this.mRemote.transact(Stub.TRANSACTION_setCompressLevel, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setCompressLevel(level);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.common.operamax.ILoaderService
            public int getCompressLevel() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(Stub.TRANSACTION_getCompressLevel, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCompressLevel();
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

        public static boolean setDefaultImpl(ILoaderService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ILoaderService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
