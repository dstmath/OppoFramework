package com.android.internal.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.Map;

public interface IHypnusService extends IInterface {
    int HypnusClrPerfData() throws RemoteException;

    void HypnusSetDisplayState(int i) throws RemoteException;

    int HypnusSetPerfData(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, int i10) throws RemoteException;

    int HypnusSetScenePerfData(int i) throws RemoteException;

    void gameStatusBroadCast(String str, String str2, String str3, Map map) throws RemoteException;

    String getPackageHashInfo(String str) throws RemoteException;

    String hypnusGetBenchModeState() throws RemoteException;

    String hypnusGetHighPerfModeState() throws RemoteException;

    String hypnusGetPMState() throws RemoteException;

    void hypnusSetAction(int i, int i2) throws RemoteException;

    void hypnusSetBurst(int i, int i2, int i3) throws RemoteException;

    void hypnusSetNotification(int i, int i2, long j, int i3, int i4, int i5) throws RemoteException;

    void hypnusSetScene(int i, String str) throws RemoteException;

    void hypnusSetSignatureAction(int i, int i2, String str) throws RemoteException;

    void hypnusSetWifiPowerSaveMode(int i, int i2) throws RemoteException;

    boolean isHypnusOK() throws RemoteException;

    public static class Default implements IHypnusService {
        @Override // com.android.internal.app.IHypnusService
        public void hypnusSetScene(int pid, String processName) throws RemoteException {
        }

        @Override // com.android.internal.app.IHypnusService
        public void hypnusSetAction(int action, int timeout) throws RemoteException {
        }

        @Override // com.android.internal.app.IHypnusService
        public void hypnusSetBurst(int tid, int type, int timeout) throws RemoteException {
        }

        @Override // com.android.internal.app.IHypnusService
        public void hypnusSetWifiPowerSaveMode(int type, int timeout) throws RemoteException {
        }

        @Override // com.android.internal.app.IHypnusService
        public void hypnusSetNotification(int msg_src, int msg_type, long msg_time, int pid, int v0, int v1) throws RemoteException {
        }

        @Override // com.android.internal.app.IHypnusService
        public boolean isHypnusOK() throws RemoteException {
            return false;
        }

        @Override // com.android.internal.app.IHypnusService
        public void hypnusSetSignatureAction(int action, int timeout, String signatureStr) throws RemoteException {
        }

        @Override // com.android.internal.app.IHypnusService
        public int HypnusSetPerfData(int small_max, int small_min, int small_cores, int big_max, int big_min, int big_cores, int gpu_max, int gpu_min, int gpu_cores, int flags) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.app.IHypnusService
        public int HypnusClrPerfData() throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.app.IHypnusService
        public int HypnusSetScenePerfData(int scene) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.app.IHypnusService
        public void gameStatusBroadCast(String action, String pkgName, String className, Map extArgs) throws RemoteException {
        }

        @Override // com.android.internal.app.IHypnusService
        public String getPackageHashInfo(String pkgName) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.app.IHypnusService
        public String hypnusGetHighPerfModeState() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.app.IHypnusService
        public String hypnusGetPMState() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.app.IHypnusService
        public String hypnusGetBenchModeState() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.app.IHypnusService
        public void HypnusSetDisplayState(int state) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHypnusService {
        private static final String DESCRIPTOR = "com.android.internal.app.IHypnusService";
        static final int TRANSACTION_HypnusClrPerfData = 9;
        static final int TRANSACTION_HypnusSetDisplayState = 16;
        static final int TRANSACTION_HypnusSetPerfData = 8;
        static final int TRANSACTION_HypnusSetScenePerfData = 10;
        static final int TRANSACTION_gameStatusBroadCast = 11;
        static final int TRANSACTION_getPackageHashInfo = 12;
        static final int TRANSACTION_hypnusGetBenchModeState = 15;
        static final int TRANSACTION_hypnusGetHighPerfModeState = 13;
        static final int TRANSACTION_hypnusGetPMState = 14;
        static final int TRANSACTION_hypnusSetAction = 2;
        static final int TRANSACTION_hypnusSetBurst = 3;
        static final int TRANSACTION_hypnusSetNotification = 5;
        static final int TRANSACTION_hypnusSetScene = 1;
        static final int TRANSACTION_hypnusSetSignatureAction = 7;
        static final int TRANSACTION_hypnusSetWifiPowerSaveMode = 4;
        static final int TRANSACTION_isHypnusOK = 6;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHypnusService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHypnusService)) {
                return new Proxy(obj);
            }
            return (IHypnusService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "hypnusSetScene";
                case 2:
                    return "hypnusSetAction";
                case 3:
                    return "hypnusSetBurst";
                case 4:
                    return "hypnusSetWifiPowerSaveMode";
                case 5:
                    return "hypnusSetNotification";
                case 6:
                    return "isHypnusOK";
                case 7:
                    return "hypnusSetSignatureAction";
                case 8:
                    return "HypnusSetPerfData";
                case 9:
                    return "HypnusClrPerfData";
                case 10:
                    return "HypnusSetScenePerfData";
                case 11:
                    return "gameStatusBroadCast";
                case 12:
                    return "getPackageHashInfo";
                case 13:
                    return "hypnusGetHighPerfModeState";
                case 14:
                    return "hypnusGetPMState";
                case 15:
                    return "hypnusGetBenchModeState";
                case 16:
                    return "HypnusSetDisplayState";
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
                        hypnusSetScene(data.readInt(), data.readString());
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        hypnusSetAction(data.readInt(), data.readInt());
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        hypnusSetBurst(data.readInt(), data.readInt(), data.readInt());
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        hypnusSetWifiPowerSaveMode(data.readInt(), data.readInt());
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        hypnusSetNotification(data.readInt(), data.readInt(), data.readLong(), data.readInt(), data.readInt(), data.readInt());
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isHypnusOK = isHypnusOK();
                        reply.writeNoException();
                        reply.writeInt(isHypnusOK ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        hypnusSetSignatureAction(data.readInt(), data.readInt(), data.readString());
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = HypnusSetPerfData(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = HypnusClrPerfData();
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = HypnusSetScenePerfData(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        gameStatusBroadCast(data.readString(), data.readString(), data.readString(), data.readHashMap(getClass().getClassLoader()));
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        String _result4 = getPackageHashInfo(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result4);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        String _result5 = hypnusGetHighPerfModeState();
                        reply.writeNoException();
                        reply.writeString(_result5);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        String _result6 = hypnusGetPMState();
                        reply.writeNoException();
                        reply.writeString(_result6);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        String _result7 = hypnusGetBenchModeState();
                        reply.writeNoException();
                        reply.writeString(_result7);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        HypnusSetDisplayState(data.readInt());
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IHypnusService {
            public static IHypnusService sDefaultImpl;
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

            @Override // com.android.internal.app.IHypnusService
            public void hypnusSetScene(int pid, String processName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeString(processName);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().hypnusSetScene(pid, processName);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IHypnusService
            public void hypnusSetAction(int action, int timeout) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(action);
                    _data.writeInt(timeout);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().hypnusSetAction(action, timeout);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IHypnusService
            public void hypnusSetBurst(int tid, int type, int timeout) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(tid);
                    _data.writeInt(type);
                    _data.writeInt(timeout);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().hypnusSetBurst(tid, type, timeout);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IHypnusService
            public void hypnusSetWifiPowerSaveMode(int type, int timeout) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeInt(timeout);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().hypnusSetWifiPowerSaveMode(type, timeout);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IHypnusService
            public void hypnusSetNotification(int msg_src, int msg_type, long msg_time, int pid, int v0, int v1) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(msg_src);
                    } catch (Throwable th) {
                        th = th;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(msg_type);
                        try {
                            _data.writeLong(msg_time);
                        } catch (Throwable th2) {
                            th = th2;
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(pid);
                            try {
                                _data.writeInt(v0);
                                _data.writeInt(v1);
                                if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                                    _data.recycle();
                                    return;
                                }
                                Stub.getDefaultImpl().hypnusSetNotification(msg_src, msg_type, msg_time, pid, v0, v1);
                                _data.recycle();
                            } catch (Throwable th3) {
                                th = th3;
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.android.internal.app.IHypnusService
            public boolean isHypnusOK() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isHypnusOK();
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

            @Override // com.android.internal.app.IHypnusService
            public void hypnusSetSignatureAction(int action, int timeout, String signatureStr) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(action);
                    _data.writeInt(timeout);
                    _data.writeString(signatureStr);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().hypnusSetSignatureAction(action, timeout, signatureStr);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IHypnusService
            public int HypnusSetPerfData(int small_max, int small_min, int small_cores, int big_max, int big_min, int big_cores, int gpu_max, int gpu_min, int gpu_cores, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(small_max);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(small_min);
                        _data.writeInt(small_cores);
                        _data.writeInt(big_max);
                        _data.writeInt(big_min);
                        _data.writeInt(big_cores);
                        _data.writeInt(gpu_max);
                        _data.writeInt(gpu_min);
                        _data.writeInt(gpu_cores);
                        _data.writeInt(flags);
                        if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            int _result = _reply.readInt();
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        int HypnusSetPerfData = Stub.getDefaultImpl().HypnusSetPerfData(small_max, small_min, small_cores, big_max, big_min, big_cores, gpu_max, gpu_min, gpu_cores, flags);
                        _reply.recycle();
                        _data.recycle();
                        return HypnusSetPerfData;
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.android.internal.app.IHypnusService
            public int HypnusClrPerfData() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().HypnusClrPerfData();
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

            @Override // com.android.internal.app.IHypnusService
            public int HypnusSetScenePerfData(int scene) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(scene);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().HypnusSetScenePerfData(scene);
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

            @Override // com.android.internal.app.IHypnusService
            public void gameStatusBroadCast(String action, String pkgName, String className, Map extArgs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(action);
                    _data.writeString(pkgName);
                    _data.writeString(className);
                    _data.writeMap(extArgs);
                    if (this.mRemote.transact(11, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().gameStatusBroadCast(action, pkgName, className, extArgs);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IHypnusService
            public String getPackageHashInfo(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPackageHashInfo(pkgName);
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

            @Override // com.android.internal.app.IHypnusService
            public String hypnusGetHighPerfModeState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hypnusGetHighPerfModeState();
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

            @Override // com.android.internal.app.IHypnusService
            public String hypnusGetPMState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hypnusGetPMState();
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

            @Override // com.android.internal.app.IHypnusService
            public String hypnusGetBenchModeState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hypnusGetBenchModeState();
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

            @Override // com.android.internal.app.IHypnusService
            public void HypnusSetDisplayState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().HypnusSetDisplayState(state);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHypnusService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHypnusService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
