package android.os;

import java.util.Map;

public interface IOppoService extends IInterface {
    void StartLogCoreService() throws RemoteException;

    void assertKernelPanic() throws RemoteException;

    boolean closeFlashLight() throws RemoteException;

    boolean copyFile(String str, String str2) throws RemoteException;

    boolean copyFileForDcs(String str, String str2) throws RemoteException;

    boolean deleteFile(String str) throws RemoteException;

    void deleteFileForDcs(String str) throws RemoteException;

    void deleteSystemLogFile() throws RemoteException;

    String getFlashLightState() throws RemoteException;

    String getOppoLogInfoString(int i) throws RemoteException;

    boolean iScoreLogServiceRunning() throws RemoteException;

    boolean openFlashLight() throws RemoteException;

    void sendDeleteStampId(String str) throws RemoteException;

    void sendOnStampEvent(String str, Map map) throws RemoteException;

    void startOppoFileEncodeHelperService() throws RemoteException;

    void startSensorLog(boolean z) throws RemoteException;

    void stopSensorLog() throws RemoteException;

    void unbindCoreLogService() throws RemoteException;

    void unbindOppoFileEncodeHelperService() throws RemoteException;

    public static class Default implements IOppoService {
        @Override // android.os.IOppoService
        public void startSensorLog(boolean isOutPutFile) throws RemoteException {
        }

        @Override // android.os.IOppoService
        public void stopSensorLog() throws RemoteException {
        }

        @Override // android.os.IOppoService
        public boolean openFlashLight() throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoService
        public boolean closeFlashLight() throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoService
        public String getFlashLightState() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoService
        public boolean iScoreLogServiceRunning() throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoService
        public void StartLogCoreService() throws RemoteException {
        }

        @Override // android.os.IOppoService
        public void unbindCoreLogService() throws RemoteException {
        }

        @Override // android.os.IOppoService
        public void assertKernelPanic() throws RemoteException {
        }

        @Override // android.os.IOppoService
        public String getOppoLogInfoString(int index) throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoService
        public void deleteSystemLogFile() throws RemoteException {
        }

        @Override // android.os.IOppoService
        public boolean copyFileForDcs(String srcPath, String destPath) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoService
        public void deleteFileForDcs(String srcPath) throws RemoteException {
        }

        @Override // android.os.IOppoService
        public void startOppoFileEncodeHelperService() throws RemoteException {
        }

        @Override // android.os.IOppoService
        public void unbindOppoFileEncodeHelperService() throws RemoteException {
        }

        @Override // android.os.IOppoService
        public boolean copyFile(String destPath, String srcPath) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoService
        public boolean deleteFile(String path) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoService
        public void sendOnStampEvent(String eventId, Map map) throws RemoteException {
        }

        @Override // android.os.IOppoService
        public void sendDeleteStampId(String eventId) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOppoService {
        private static final String DESCRIPTOR = "android.os.IOppoService";
        static final int TRANSACTION_StartLogCoreService = 7;
        static final int TRANSACTION_assertKernelPanic = 9;
        static final int TRANSACTION_closeFlashLight = 4;
        static final int TRANSACTION_copyFile = 16;
        static final int TRANSACTION_copyFileForDcs = 12;
        static final int TRANSACTION_deleteFile = 17;
        static final int TRANSACTION_deleteFileForDcs = 13;
        static final int TRANSACTION_deleteSystemLogFile = 11;
        static final int TRANSACTION_getFlashLightState = 5;
        static final int TRANSACTION_getOppoLogInfoString = 10;
        static final int TRANSACTION_iScoreLogServiceRunning = 6;
        static final int TRANSACTION_openFlashLight = 3;
        static final int TRANSACTION_sendDeleteStampId = 19;
        static final int TRANSACTION_sendOnStampEvent = 18;
        static final int TRANSACTION_startOppoFileEncodeHelperService = 14;
        static final int TRANSACTION_startSensorLog = 1;
        static final int TRANSACTION_stopSensorLog = 2;
        static final int TRANSACTION_unbindCoreLogService = 8;
        static final int TRANSACTION_unbindOppoFileEncodeHelperService = 15;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOppoService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOppoService)) {
                return new Proxy(obj);
            }
            return (IOppoService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "startSensorLog";
                case 2:
                    return "stopSensorLog";
                case 3:
                    return "openFlashLight";
                case 4:
                    return "closeFlashLight";
                case 5:
                    return "getFlashLightState";
                case 6:
                    return "iScoreLogServiceRunning";
                case 7:
                    return "StartLogCoreService";
                case 8:
                    return "unbindCoreLogService";
                case 9:
                    return "assertKernelPanic";
                case 10:
                    return "getOppoLogInfoString";
                case 11:
                    return "deleteSystemLogFile";
                case 12:
                    return "copyFileForDcs";
                case 13:
                    return "deleteFileForDcs";
                case 14:
                    return "startOppoFileEncodeHelperService";
                case 15:
                    return "unbindOppoFileEncodeHelperService";
                case 16:
                    return "copyFile";
                case 17:
                    return "deleteFile";
                case 18:
                    return "sendOnStampEvent";
                case 19:
                    return "sendDeleteStampId";
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
                        startSensorLog(data.readInt() != 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        stopSensorLog();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean openFlashLight = openFlashLight();
                        reply.writeNoException();
                        reply.writeInt(openFlashLight ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean closeFlashLight = closeFlashLight();
                        reply.writeNoException();
                        reply.writeInt(closeFlashLight ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        String _result = getFlashLightState();
                        reply.writeNoException();
                        reply.writeString(_result);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean iScoreLogServiceRunning = iScoreLogServiceRunning();
                        reply.writeNoException();
                        reply.writeInt(iScoreLogServiceRunning ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        StartLogCoreService();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        unbindCoreLogService();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        assertKernelPanic();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        String _result2 = getOppoLogInfoString(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result2);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        deleteSystemLogFile();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        boolean copyFileForDcs = copyFileForDcs(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(copyFileForDcs ? 1 : 0);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        deleteFileForDcs(data.readString());
                        reply.writeNoException();
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        startOppoFileEncodeHelperService();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        unbindOppoFileEncodeHelperService();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        boolean copyFile = copyFile(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(copyFile ? 1 : 0);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        boolean deleteFile = deleteFile(data.readString());
                        reply.writeNoException();
                        reply.writeInt(deleteFile ? 1 : 0);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        sendOnStampEvent(data.readString(), data.readHashMap(getClass().getClassLoader()));
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        sendDeleteStampId(data.readString());
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

        private static class Proxy implements IOppoService {
            public static IOppoService sDefaultImpl;
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

            @Override // android.os.IOppoService
            public void startSensorLog(boolean isOutPutFile) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isOutPutFile ? 1 : 0);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().startSensorLog(isOutPutFile);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoService
            public void stopSensorLog() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().stopSensorLog();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoService
            public boolean openFlashLight() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().openFlashLight();
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

            @Override // android.os.IOppoService
            public boolean closeFlashLight() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().closeFlashLight();
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

            @Override // android.os.IOppoService
            public String getFlashLightState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFlashLightState();
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

            @Override // android.os.IOppoService
            public boolean iScoreLogServiceRunning() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().iScoreLogServiceRunning();
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

            @Override // android.os.IOppoService
            public void StartLogCoreService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().StartLogCoreService();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoService
            public void unbindCoreLogService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().unbindCoreLogService();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoService
            public void assertKernelPanic() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().assertKernelPanic();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoService
            public String getOppoLogInfoString(int index) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(index);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getOppoLogInfoString(index);
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

            @Override // android.os.IOppoService
            public void deleteSystemLogFile() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(11, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().deleteSystemLogFile();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoService
            public boolean copyFileForDcs(String srcPath, String destPath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(srcPath);
                    _data.writeString(destPath);
                    boolean _result = false;
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().copyFileForDcs(srcPath, destPath);
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

            @Override // android.os.IOppoService
            public void deleteFileForDcs(String srcPath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(srcPath);
                    if (this.mRemote.transact(13, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().deleteFileForDcs(srcPath);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoService
            public void startOppoFileEncodeHelperService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(14, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().startOppoFileEncodeHelperService();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoService
            public void unbindOppoFileEncodeHelperService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(15, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().unbindOppoFileEncodeHelperService();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoService
            public boolean copyFile(String destPath, String srcPath) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(destPath);
                    _data.writeString(srcPath);
                    boolean _result = false;
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().copyFile(destPath, srcPath);
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

            @Override // android.os.IOppoService
            public boolean deleteFile(String path) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(path);
                    boolean _result = false;
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().deleteFile(path);
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

            @Override // android.os.IOppoService
            public void sendOnStampEvent(String eventId, Map map) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(eventId);
                    _data.writeMap(map);
                    if (this.mRemote.transact(18, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sendOnStampEvent(eventId, map);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoService
            public void sendDeleteStampId(String eventId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(eventId);
                    if (this.mRemote.transact(19, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sendDeleteStampId(eventId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IOppoService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOppoService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
