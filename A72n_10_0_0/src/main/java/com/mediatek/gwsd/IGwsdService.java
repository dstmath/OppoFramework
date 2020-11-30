package com.mediatek.gwsd;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.mediatek.gwsd.IGwsdListener;

public interface IGwsdService extends IInterface {
    void addListener(int i, IGwsdListener iGwsdListener) throws RemoteException;

    boolean isDataAvailableForGwsdDualSim(boolean z) throws RemoteException;

    void removeListener(int i) throws RemoteException;

    void setAutoRejectModeEnabled(int i, boolean z) throws RemoteException;

    void setCallValidTimer(int i, int i2) throws RemoteException;

    void setGwsdDualSimEnabled(boolean z) throws RemoteException;

    void setIgnoreSameNumberInterval(int i, int i2) throws RemoteException;

    void setUserModeEnabled(int i, boolean z) throws RemoteException;

    void syncGwsdInfo(int i, boolean z, boolean z2) throws RemoteException;

    public static class Default implements IGwsdService {
        @Override // com.mediatek.gwsd.IGwsdService
        public void addListener(int phoneId, IGwsdListener listener) throws RemoteException {
        }

        @Override // com.mediatek.gwsd.IGwsdService
        public void removeListener(int phoneId) throws RemoteException {
        }

        @Override // com.mediatek.gwsd.IGwsdService
        public void setUserModeEnabled(int phoneId, boolean action) throws RemoteException {
        }

        @Override // com.mediatek.gwsd.IGwsdService
        public void setAutoRejectModeEnabled(int phoneId, boolean action) throws RemoteException {
        }

        @Override // com.mediatek.gwsd.IGwsdService
        public void syncGwsdInfo(int phoneId, boolean userEnable, boolean autoReject) throws RemoteException {
        }

        @Override // com.mediatek.gwsd.IGwsdService
        public void setCallValidTimer(int phoneId, int timer) throws RemoteException {
        }

        @Override // com.mediatek.gwsd.IGwsdService
        public void setIgnoreSameNumberInterval(int phoneId, int interna) throws RemoteException {
        }

        @Override // com.mediatek.gwsd.IGwsdService
        public void setGwsdDualSimEnabled(boolean action) throws RemoteException {
        }

        @Override // com.mediatek.gwsd.IGwsdService
        public boolean isDataAvailableForGwsdDualSim(boolean gwsdDualSimStatus) throws RemoteException {
            return false;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IGwsdService {
        private static final String DESCRIPTOR = "com.mediatek.gwsd.IGwsdService";
        static final int TRANSACTION_addListener = 1;
        static final int TRANSACTION_isDataAvailableForGwsdDualSim = 9;
        static final int TRANSACTION_removeListener = 2;
        static final int TRANSACTION_setAutoRejectModeEnabled = 4;
        static final int TRANSACTION_setCallValidTimer = 6;
        static final int TRANSACTION_setGwsdDualSimEnabled = 8;
        static final int TRANSACTION_setIgnoreSameNumberInterval = 7;
        static final int TRANSACTION_setUserModeEnabled = 3;
        static final int TRANSACTION_syncGwsdInfo = 5;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IGwsdService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGwsdService)) {
                return new Proxy(obj);
            }
            return (IGwsdService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                boolean _arg0 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        addListener(data.readInt(), IGwsdListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        removeListener(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        setUserModeEnabled(_arg02, _arg0);
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        setAutoRejectModeEnabled(_arg03, _arg0);
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        boolean _arg1 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        syncGwsdInfo(_arg04, _arg1, _arg0);
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        setCallValidTimer(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        setIgnoreSameNumberInterval(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        setGwsdDualSimEnabled(_arg0);
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        boolean isDataAvailableForGwsdDualSim = isDataAvailableForGwsdDualSim(_arg0);
                        reply.writeNoException();
                        reply.writeInt(isDataAvailableForGwsdDualSim ? 1 : 0);
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
        public static class Proxy implements IGwsdService {
            public static IGwsdService sDefaultImpl;
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

            @Override // com.mediatek.gwsd.IGwsdService
            public void addListener(int phoneId, IGwsdListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addListener(phoneId, listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.gwsd.IGwsdService
            public void removeListener(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeListener(phoneId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.gwsd.IGwsdService
            public void setUserModeEnabled(int phoneId, boolean action) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(action ? 1 : 0);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setUserModeEnabled(phoneId, action);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.gwsd.IGwsdService
            public void setAutoRejectModeEnabled(int phoneId, boolean action) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(action ? 1 : 0);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAutoRejectModeEnabled(phoneId, action);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.gwsd.IGwsdService
            public void syncGwsdInfo(int phoneId, boolean userEnable, boolean autoReject) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    int i = 1;
                    _data.writeInt(userEnable ? 1 : 0);
                    if (!autoReject) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().syncGwsdInfo(phoneId, userEnable, autoReject);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.gwsd.IGwsdService
            public void setCallValidTimer(int phoneId, int timer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(timer);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setCallValidTimer(phoneId, timer);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.gwsd.IGwsdService
            public void setIgnoreSameNumberInterval(int phoneId, int interna) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(interna);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setIgnoreSameNumberInterval(phoneId, interna);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.gwsd.IGwsdService
            public void setGwsdDualSimEnabled(boolean action) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(action ? 1 : 0);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setGwsdDualSimEnabled(action);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.gwsd.IGwsdService
            public boolean isDataAvailableForGwsdDualSim(boolean gwsdDualSimStatus) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(gwsdDualSimStatus ? 1 : 0);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isDataAvailableForGwsdDualSim(gwsdDualSimStatus);
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

        public static boolean setDefaultImpl(IGwsdService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IGwsdService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
