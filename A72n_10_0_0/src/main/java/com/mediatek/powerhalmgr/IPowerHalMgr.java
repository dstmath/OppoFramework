package com.mediatek.powerhalmgr;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.IRemoteCallback;
import android.os.Parcel;
import android.os.RemoteException;

public interface IPowerHalMgr extends IInterface {
    void UpdateManagementPkt(int i, String str) throws RemoteException;

    void getCpuCap() throws RemoteException;

    void getCpuRTInfo() throws RemoteException;

    void getGpuCap() throws RemoteException;

    void getGpuRTInfo() throws RemoteException;

    boolean isDupPacketPredictionStarted() throws RemoteException;

    void mtkCusPowerHint(int i, int i2) throws RemoteException;

    int perfLockAcquire(int i, int i2, int[] iArr) throws RemoteException;

    void perfLockRelease(int i) throws RemoteException;

    int querySysInfo(int i, int i2) throws RemoteException;

    boolean registerDuplicatePacketPredictionEvent(IRemoteCallback iRemoteCallback) throws RemoteException;

    void reloadwhitelist() throws RemoteException;

    void scnConfig(int i, int i2, int i3, int i4, int i5, int i6) throws RemoteException;

    void scnDisable(int i) throws RemoteException;

    void scnEnable(int i, int i2) throws RemoteException;

    int scnReg() throws RemoteException;

    void scnUltraCfg(int i, int i2, int i3, int i4, int i5, int i6) throws RemoteException;

    void scnUnreg(int i) throws RemoteException;

    void setForegroundSports() throws RemoteException;

    void setPredictInfo(String str, int i) throws RemoteException;

    void setSysInfo(int i, String str) throws RemoteException;

    int setSysInfoSync(int i, String str) throws RemoteException;

    boolean startDuplicatePacketPrediction() throws RemoteException;

    boolean stopDuplicatePacketPrediction() throws RemoteException;

    boolean unregisterDuplicatePacketPredictionEvent(IRemoteCallback iRemoteCallback) throws RemoteException;

    boolean updateMultiDuplicatePacketLink(DupLinkInfo[] dupLinkInfoArr) throws RemoteException;

    public static class Default implements IPowerHalMgr {
        @Override // com.mediatek.powerhalmgr.IPowerHalMgr
        public int scnReg() throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.powerhalmgr.IPowerHalMgr
        public void scnConfig(int handle, int cmd, int param_1, int param_2, int param_3, int param_4) throws RemoteException {
        }

        @Override // com.mediatek.powerhalmgr.IPowerHalMgr
        public void scnUnreg(int handle) throws RemoteException {
        }

        @Override // com.mediatek.powerhalmgr.IPowerHalMgr
        public void scnEnable(int handle, int timeout) throws RemoteException {
        }

        @Override // com.mediatek.powerhalmgr.IPowerHalMgr
        public void scnDisable(int handle) throws RemoteException {
        }

        @Override // com.mediatek.powerhalmgr.IPowerHalMgr
        public void scnUltraCfg(int handle, int ultracmd, int param_1, int param_2, int param_3, int param_4) throws RemoteException {
        }

        @Override // com.mediatek.powerhalmgr.IPowerHalMgr
        public void mtkCusPowerHint(int hint, int data) throws RemoteException {
        }

        @Override // com.mediatek.powerhalmgr.IPowerHalMgr
        public void reloadwhitelist() throws RemoteException {
        }

        @Override // com.mediatek.powerhalmgr.IPowerHalMgr
        public void getCpuCap() throws RemoteException {
        }

        @Override // com.mediatek.powerhalmgr.IPowerHalMgr
        public void getGpuCap() throws RemoteException {
        }

        @Override // com.mediatek.powerhalmgr.IPowerHalMgr
        public void getGpuRTInfo() throws RemoteException {
        }

        @Override // com.mediatek.powerhalmgr.IPowerHalMgr
        public void getCpuRTInfo() throws RemoteException {
        }

        @Override // com.mediatek.powerhalmgr.IPowerHalMgr
        public void UpdateManagementPkt(int type, String packet) throws RemoteException {
        }

        @Override // com.mediatek.powerhalmgr.IPowerHalMgr
        public void setForegroundSports() throws RemoteException {
        }

        @Override // com.mediatek.powerhalmgr.IPowerHalMgr
        public void setSysInfo(int type, String data) throws RemoteException {
        }

        @Override // com.mediatek.powerhalmgr.IPowerHalMgr
        public boolean startDuplicatePacketPrediction() throws RemoteException {
            return false;
        }

        @Override // com.mediatek.powerhalmgr.IPowerHalMgr
        public boolean stopDuplicatePacketPrediction() throws RemoteException {
            return false;
        }

        @Override // com.mediatek.powerhalmgr.IPowerHalMgr
        public boolean isDupPacketPredictionStarted() throws RemoteException {
            return false;
        }

        @Override // com.mediatek.powerhalmgr.IPowerHalMgr
        public boolean registerDuplicatePacketPredictionEvent(IRemoteCallback listener) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.powerhalmgr.IPowerHalMgr
        public boolean unregisterDuplicatePacketPredictionEvent(IRemoteCallback listener) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.powerhalmgr.IPowerHalMgr
        public boolean updateMultiDuplicatePacketLink(DupLinkInfo[] linkList) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.powerhalmgr.IPowerHalMgr
        public void setPredictInfo(String pack_name, int uid) throws RemoteException {
        }

        @Override // com.mediatek.powerhalmgr.IPowerHalMgr
        public int perfLockAcquire(int handle, int duration, int[] list) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.powerhalmgr.IPowerHalMgr
        public void perfLockRelease(int handle) throws RemoteException {
        }

        @Override // com.mediatek.powerhalmgr.IPowerHalMgr
        public int querySysInfo(int cmd, int param) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.powerhalmgr.IPowerHalMgr
        public int setSysInfoSync(int type, String data) throws RemoteException {
            return 0;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IPowerHalMgr {
        private static final String DESCRIPTOR = "com.mediatek.powerhalmgr.IPowerHalMgr";
        static final int TRANSACTION_UpdateManagementPkt = 13;
        static final int TRANSACTION_getCpuCap = 9;
        static final int TRANSACTION_getCpuRTInfo = 12;
        static final int TRANSACTION_getGpuCap = 10;
        static final int TRANSACTION_getGpuRTInfo = 11;
        static final int TRANSACTION_isDupPacketPredictionStarted = 18;
        static final int TRANSACTION_mtkCusPowerHint = 7;
        static final int TRANSACTION_perfLockAcquire = 23;
        static final int TRANSACTION_perfLockRelease = 24;
        static final int TRANSACTION_querySysInfo = 25;
        static final int TRANSACTION_registerDuplicatePacketPredictionEvent = 19;
        static final int TRANSACTION_reloadwhitelist = 8;
        static final int TRANSACTION_scnConfig = 2;
        static final int TRANSACTION_scnDisable = 5;
        static final int TRANSACTION_scnEnable = 4;
        static final int TRANSACTION_scnReg = 1;
        static final int TRANSACTION_scnUltraCfg = 6;
        static final int TRANSACTION_scnUnreg = 3;
        static final int TRANSACTION_setForegroundSports = 14;
        static final int TRANSACTION_setPredictInfo = 22;
        static final int TRANSACTION_setSysInfo = 15;
        static final int TRANSACTION_setSysInfoSync = 26;
        static final int TRANSACTION_startDuplicatePacketPrediction = 16;
        static final int TRANSACTION_stopDuplicatePacketPrediction = 17;
        static final int TRANSACTION_unregisterDuplicatePacketPredictionEvent = 20;
        static final int TRANSACTION_updateMultiDuplicatePacketLink = 21;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPowerHalMgr asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPowerHalMgr)) {
                return new Proxy(obj);
            }
            return (IPowerHalMgr) iin;
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
                        int _result = scnReg();
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        scnConfig(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        scnUnreg(data.readInt());
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        scnEnable(data.readInt(), data.readInt());
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        scnDisable(data.readInt());
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        scnUltraCfg(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        mtkCusPowerHint(data.readInt(), data.readInt());
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        reloadwhitelist();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        getCpuCap();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        getGpuCap();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        getGpuRTInfo();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        getCpuRTInfo();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        UpdateManagementPkt(data.readInt(), data.readString());
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        setForegroundSports();
                        return true;
                    case TRANSACTION_setSysInfo /* 15 */:
                        data.enforceInterface(DESCRIPTOR);
                        setSysInfo(data.readInt(), data.readString());
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        boolean startDuplicatePacketPrediction = startDuplicatePacketPrediction();
                        reply.writeNoException();
                        reply.writeInt(startDuplicatePacketPrediction ? 1 : 0);
                        return true;
                    case TRANSACTION_stopDuplicatePacketPrediction /* 17 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean stopDuplicatePacketPrediction = stopDuplicatePacketPrediction();
                        reply.writeNoException();
                        reply.writeInt(stopDuplicatePacketPrediction ? 1 : 0);
                        return true;
                    case TRANSACTION_isDupPacketPredictionStarted /* 18 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isDupPacketPredictionStarted = isDupPacketPredictionStarted();
                        reply.writeNoException();
                        reply.writeInt(isDupPacketPredictionStarted ? 1 : 0);
                        return true;
                    case TRANSACTION_registerDuplicatePacketPredictionEvent /* 19 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerDuplicatePacketPredictionEvent = registerDuplicatePacketPredictionEvent(IRemoteCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerDuplicatePacketPredictionEvent ? 1 : 0);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        boolean unregisterDuplicatePacketPredictionEvent = unregisterDuplicatePacketPredictionEvent(IRemoteCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(unregisterDuplicatePacketPredictionEvent ? 1 : 0);
                        return true;
                    case TRANSACTION_updateMultiDuplicatePacketLink /* 21 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean updateMultiDuplicatePacketLink = updateMultiDuplicatePacketLink((DupLinkInfo[]) data.createTypedArray(DupLinkInfo.CREATOR));
                        reply.writeNoException();
                        reply.writeInt(updateMultiDuplicatePacketLink ? 1 : 0);
                        return true;
                    case TRANSACTION_setPredictInfo /* 22 */:
                        data.enforceInterface(DESCRIPTOR);
                        setPredictInfo(data.readString(), data.readInt());
                        return true;
                    case TRANSACTION_perfLockAcquire /* 23 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = perfLockAcquire(data.readInt(), data.readInt(), data.createIntArray());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case TRANSACTION_perfLockRelease /* 24 */:
                        data.enforceInterface(DESCRIPTOR);
                        perfLockRelease(data.readInt());
                        return true;
                    case TRANSACTION_querySysInfo /* 25 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = querySysInfo(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case TRANSACTION_setSysInfoSync /* 26 */:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = setSysInfoSync(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result4);
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
        public static class Proxy implements IPowerHalMgr {
            public static IPowerHalMgr sDefaultImpl;
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

            @Override // com.mediatek.powerhalmgr.IPowerHalMgr
            public int scnReg() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().scnReg();
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

            @Override // com.mediatek.powerhalmgr.IPowerHalMgr
            public void scnConfig(int handle, int cmd, int param_1, int param_2, int param_3, int param_4) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(handle);
                    } catch (Throwable th2) {
                        th = th2;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(cmd);
                        try {
                            _data.writeInt(param_1);
                            try {
                                _data.writeInt(param_2);
                                try {
                                    _data.writeInt(param_3);
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
                        try {
                            _data.writeInt(param_4);
                            try {
                                if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                                    _data.recycle();
                                    return;
                                }
                                Stub.getDefaultImpl().scnConfig(handle, cmd, param_1, param_2, param_3, param_4);
                                _data.recycle();
                            } catch (Throwable th6) {
                                th = th6;
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th7) {
                            th = th7;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th8) {
                        th = th8;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th9) {
                    th = th9;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.mediatek.powerhalmgr.IPowerHalMgr
            public void scnUnreg(int handle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(handle);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().scnUnreg(handle);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.mediatek.powerhalmgr.IPowerHalMgr
            public void scnEnable(int handle, int timeout) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(handle);
                    _data.writeInt(timeout);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().scnEnable(handle, timeout);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.mediatek.powerhalmgr.IPowerHalMgr
            public void scnDisable(int handle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(handle);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().scnDisable(handle);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.mediatek.powerhalmgr.IPowerHalMgr
            public void scnUltraCfg(int handle, int ultracmd, int param_1, int param_2, int param_3, int param_4) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(handle);
                    } catch (Throwable th2) {
                        th = th2;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(ultracmd);
                        try {
                            _data.writeInt(param_1);
                            try {
                                _data.writeInt(param_2);
                                try {
                                    _data.writeInt(param_3);
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
                        try {
                            _data.writeInt(param_4);
                            try {
                                if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                                    _data.recycle();
                                    return;
                                }
                                Stub.getDefaultImpl().scnUltraCfg(handle, ultracmd, param_1, param_2, param_3, param_4);
                                _data.recycle();
                            } catch (Throwable th6) {
                                th = th6;
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th7) {
                            th = th7;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th8) {
                        th = th8;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th9) {
                    th = th9;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.mediatek.powerhalmgr.IPowerHalMgr
            public void mtkCusPowerHint(int hint, int data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(hint);
                    _data.writeInt(data);
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().mtkCusPowerHint(hint, data);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.mediatek.powerhalmgr.IPowerHalMgr
            public void reloadwhitelist() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().reloadwhitelist();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.mediatek.powerhalmgr.IPowerHalMgr
            public void getCpuCap() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().getCpuCap();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.mediatek.powerhalmgr.IPowerHalMgr
            public void getGpuCap() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(10, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().getGpuCap();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.mediatek.powerhalmgr.IPowerHalMgr
            public void getGpuRTInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(11, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().getGpuRTInfo();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.mediatek.powerhalmgr.IPowerHalMgr
            public void getCpuRTInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(12, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().getCpuRTInfo();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.mediatek.powerhalmgr.IPowerHalMgr
            public void UpdateManagementPkt(int type, String packet) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeString(packet);
                    if (this.mRemote.transact(13, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().UpdateManagementPkt(type, packet);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.mediatek.powerhalmgr.IPowerHalMgr
            public void setForegroundSports() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(14, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setForegroundSports();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.mediatek.powerhalmgr.IPowerHalMgr
            public void setSysInfo(int type, String data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeString(data);
                    if (this.mRemote.transact(Stub.TRANSACTION_setSysInfo, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setSysInfo(type, data);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.mediatek.powerhalmgr.IPowerHalMgr
            public boolean startDuplicatePacketPrediction() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startDuplicatePacketPrediction();
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

            @Override // com.mediatek.powerhalmgr.IPowerHalMgr
            public boolean stopDuplicatePacketPrediction() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_stopDuplicatePacketPrediction, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().stopDuplicatePacketPrediction();
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

            @Override // com.mediatek.powerhalmgr.IPowerHalMgr
            public boolean isDupPacketPredictionStarted() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_isDupPacketPredictionStarted, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isDupPacketPredictionStarted();
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

            @Override // com.mediatek.powerhalmgr.IPowerHalMgr
            public boolean registerDuplicatePacketPredictionEvent(IRemoteCallback listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_registerDuplicatePacketPredictionEvent, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerDuplicatePacketPredictionEvent(listener);
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

            @Override // com.mediatek.powerhalmgr.IPowerHalMgr
            public boolean unregisterDuplicatePacketPredictionEvent(IRemoteCallback listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unregisterDuplicatePacketPredictionEvent(listener);
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

            @Override // com.mediatek.powerhalmgr.IPowerHalMgr
            public boolean updateMultiDuplicatePacketLink(DupLinkInfo[] linkList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    _data.writeTypedArray(linkList, 0);
                    if (!this.mRemote.transact(Stub.TRANSACTION_updateMultiDuplicatePacketLink, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateMultiDuplicatePacketLink(linkList);
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

            @Override // com.mediatek.powerhalmgr.IPowerHalMgr
            public void setPredictInfo(String pack_name, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pack_name);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(Stub.TRANSACTION_setPredictInfo, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setPredictInfo(pack_name, uid);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.mediatek.powerhalmgr.IPowerHalMgr
            public int perfLockAcquire(int handle, int duration, int[] list) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(handle);
                    _data.writeInt(duration);
                    _data.writeIntArray(list);
                    if (!this.mRemote.transact(Stub.TRANSACTION_perfLockAcquire, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().perfLockAcquire(handle, duration, list);
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

            @Override // com.mediatek.powerhalmgr.IPowerHalMgr
            public void perfLockRelease(int handle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(handle);
                    if (this.mRemote.transact(Stub.TRANSACTION_perfLockRelease, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().perfLockRelease(handle);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.mediatek.powerhalmgr.IPowerHalMgr
            public int querySysInfo(int cmd, int param) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cmd);
                    _data.writeInt(param);
                    if (!this.mRemote.transact(Stub.TRANSACTION_querySysInfo, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().querySysInfo(cmd, param);
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

            @Override // com.mediatek.powerhalmgr.IPowerHalMgr
            public int setSysInfoSync(int type, String data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    _data.writeString(data);
                    if (!this.mRemote.transact(Stub.TRANSACTION_setSysInfoSync, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setSysInfoSync(type, data);
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

        public static boolean setDefaultImpl(IPowerHalMgr impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IPowerHalMgr getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
