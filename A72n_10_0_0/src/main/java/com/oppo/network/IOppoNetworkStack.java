package com.oppo.network;

import android.net.Network;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.oppo.network.IOppoNetScoreChange;

public interface IOppoNetworkStack extends IInterface {
    boolean disableDUALWIFIUid(int i) throws RemoteException;

    boolean disableMPTCPUid(int i) throws RemoteException;

    boolean disableSLAPUid(int i) throws RemoteException;

    boolean enableDUALWIFIUid(int i) throws RemoteException;

    boolean enableMPTCPUid(int i) throws RemoteException;

    boolean enableSLAUid(int i) throws RemoteException;

    void enableScreenshotDetect() throws RemoteException;

    int getNetworkScore(Network network) throws RemoteException;

    String getOppoNetworkStackInfo() throws RemoteException;

    int getPortalResult(Network network, int i) throws RemoteException;

    boolean isGatewayConflict(Network network) throws RemoteException;

    void registerTcpScoreChange(IOppoNetScoreChange iOppoNetScoreChange) throws RemoteException;

    void setOppoNetworkStackConfig(String str) throws RemoteException;

    void unregisterTcpScoreChange(IOppoNetScoreChange iOppoNetScoreChange) throws RemoteException;

    public static class Default implements IOppoNetworkStack {
        @Override // com.oppo.network.IOppoNetworkStack
        public void registerTcpScoreChange(IOppoNetScoreChange scorechange) throws RemoteException {
        }

        @Override // com.oppo.network.IOppoNetworkStack
        public void unregisterTcpScoreChange(IOppoNetScoreChange scorechange) throws RemoteException {
        }

        @Override // com.oppo.network.IOppoNetworkStack
        public int getPortalResult(Network network, int timeout) throws RemoteException {
            return 0;
        }

        @Override // com.oppo.network.IOppoNetworkStack
        public boolean isGatewayConflict(Network network) throws RemoteException {
            return false;
        }

        @Override // com.oppo.network.IOppoNetworkStack
        public void enableScreenshotDetect() throws RemoteException {
        }

        @Override // com.oppo.network.IOppoNetworkStack
        public boolean enableMPTCPUid(int uid) throws RemoteException {
            return false;
        }

        @Override // com.oppo.network.IOppoNetworkStack
        public boolean disableMPTCPUid(int uid) throws RemoteException {
            return false;
        }

        @Override // com.oppo.network.IOppoNetworkStack
        public boolean enableSLAUid(int uid) throws RemoteException {
            return false;
        }

        @Override // com.oppo.network.IOppoNetworkStack
        public boolean disableSLAPUid(int uid) throws RemoteException {
            return false;
        }

        @Override // com.oppo.network.IOppoNetworkStack
        public boolean enableDUALWIFIUid(int uid) throws RemoteException {
            return false;
        }

        @Override // com.oppo.network.IOppoNetworkStack
        public boolean disableDUALWIFIUid(int uid) throws RemoteException {
            return false;
        }

        @Override // com.oppo.network.IOppoNetworkStack
        public int getNetworkScore(Network network) throws RemoteException {
            return 0;
        }

        @Override // com.oppo.network.IOppoNetworkStack
        public String getOppoNetworkStackInfo() throws RemoteException {
            return null;
        }

        @Override // com.oppo.network.IOppoNetworkStack
        public void setOppoNetworkStackConfig(String config) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOppoNetworkStack {
        private static final String DESCRIPTOR = "com.oppo.network.IOppoNetworkStack";
        static final int TRANSACTION_disableDUALWIFIUid = 11;
        static final int TRANSACTION_disableMPTCPUid = 7;
        static final int TRANSACTION_disableSLAPUid = 9;
        static final int TRANSACTION_enableDUALWIFIUid = 10;
        static final int TRANSACTION_enableMPTCPUid = 6;
        static final int TRANSACTION_enableSLAUid = 8;
        static final int TRANSACTION_enableScreenshotDetect = 5;
        static final int TRANSACTION_getNetworkScore = 12;
        static final int TRANSACTION_getOppoNetworkStackInfo = 13;
        static final int TRANSACTION_getPortalResult = 3;
        static final int TRANSACTION_isGatewayConflict = 4;
        static final int TRANSACTION_registerTcpScoreChange = 1;
        static final int TRANSACTION_setOppoNetworkStackConfig = 14;
        static final int TRANSACTION_unregisterTcpScoreChange = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOppoNetworkStack asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOppoNetworkStack)) {
                return new Proxy(obj);
            }
            return (IOppoNetworkStack) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "registerTcpScoreChange";
                case 2:
                    return "unregisterTcpScoreChange";
                case 3:
                    return "getPortalResult";
                case 4:
                    return "isGatewayConflict";
                case 5:
                    return "enableScreenshotDetect";
                case 6:
                    return "enableMPTCPUid";
                case 7:
                    return "disableMPTCPUid";
                case 8:
                    return "enableSLAUid";
                case 9:
                    return "disableSLAPUid";
                case 10:
                    return "enableDUALWIFIUid";
                case 11:
                    return "disableDUALWIFIUid";
                case 12:
                    return "getNetworkScore";
                case 13:
                    return "getOppoNetworkStackInfo";
                case 14:
                    return "setOppoNetworkStackConfig";
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
            Network _arg0;
            Network _arg02;
            Network _arg03;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        registerTcpScoreChange(IOppoNetScoreChange.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterTcpScoreChange(IOppoNetScoreChange.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Network.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        int _result = getPortalResult(_arg0, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = Network.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        boolean isGatewayConflict = isGatewayConflict(_arg02);
                        reply.writeNoException();
                        reply.writeInt(isGatewayConflict ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        enableScreenshotDetect();
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean enableMPTCPUid = enableMPTCPUid(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(enableMPTCPUid ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean disableMPTCPUid = disableMPTCPUid(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(disableMPTCPUid ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        boolean enableSLAUid = enableSLAUid(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(enableSLAUid ? 1 : 0);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        boolean disableSLAPUid = disableSLAPUid(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(disableSLAPUid ? 1 : 0);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        boolean enableDUALWIFIUid = enableDUALWIFIUid(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(enableDUALWIFIUid ? 1 : 0);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        boolean disableDUALWIFIUid = disableDUALWIFIUid(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(disableDUALWIFIUid ? 1 : 0);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = Network.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        int _result2 = getNetworkScore(_arg03);
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        String _result3 = getOppoNetworkStackInfo();
                        reply.writeNoException();
                        reply.writeString(_result3);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        setOppoNetworkStackConfig(data.readString());
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

        /* access modifiers changed from: private */
        public static class Proxy implements IOppoNetworkStack {
            public static IOppoNetworkStack sDefaultImpl;
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

            @Override // com.oppo.network.IOppoNetworkStack
            public void registerTcpScoreChange(IOppoNetScoreChange scorechange) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(scorechange != null ? scorechange.asBinder() : null);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerTcpScoreChange(scorechange);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.network.IOppoNetworkStack
            public void unregisterTcpScoreChange(IOppoNetScoreChange scorechange) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(scorechange != null ? scorechange.asBinder() : null);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterTcpScoreChange(scorechange);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.network.IOppoNetworkStack
            public int getPortalResult(Network network, int timeout) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network != null) {
                        _data.writeInt(1);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(timeout);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPortalResult(network, timeout);
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

            @Override // com.oppo.network.IOppoNetworkStack
            public boolean isGatewayConflict(Network network) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (network != null) {
                        _data.writeInt(1);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isGatewayConflict(network);
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

            @Override // com.oppo.network.IOppoNetworkStack
            public void enableScreenshotDetect() throws RemoteException {
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
                    Stub.getDefaultImpl().enableScreenshotDetect();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.network.IOppoNetworkStack
            public boolean enableMPTCPUid(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    boolean _result = false;
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().enableMPTCPUid(uid);
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

            @Override // com.oppo.network.IOppoNetworkStack
            public boolean disableMPTCPUid(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    boolean _result = false;
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().disableMPTCPUid(uid);
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

            @Override // com.oppo.network.IOppoNetworkStack
            public boolean enableSLAUid(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    boolean _result = false;
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().enableSLAUid(uid);
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

            @Override // com.oppo.network.IOppoNetworkStack
            public boolean disableSLAPUid(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    boolean _result = false;
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().disableSLAPUid(uid);
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

            @Override // com.oppo.network.IOppoNetworkStack
            public boolean enableDUALWIFIUid(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    boolean _result = false;
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().enableDUALWIFIUid(uid);
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

            @Override // com.oppo.network.IOppoNetworkStack
            public boolean disableDUALWIFIUid(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    boolean _result = false;
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().disableDUALWIFIUid(uid);
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

            @Override // com.oppo.network.IOppoNetworkStack
            public int getNetworkScore(Network network) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (network != null) {
                        _data.writeInt(1);
                        network.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNetworkScore(network);
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

            @Override // com.oppo.network.IOppoNetworkStack
            public String getOppoNetworkStackInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getOppoNetworkStackInfo();
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

            @Override // com.oppo.network.IOppoNetworkStack
            public void setOppoNetworkStackConfig(String config) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(config);
                    if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setOppoNetworkStackConfig(config);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IOppoNetworkStack impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOppoNetworkStack getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
