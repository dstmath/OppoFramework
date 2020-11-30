package com.mediatek.wfo;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IWifiOffloadListener extends IInterface {
    void onAllowWifiOff() throws RemoteException;

    void onHandover(int i, int i2, int i3) throws RemoteException;

    void onRequestImsSwitch(int i, boolean z) throws RemoteException;

    void onRoveOut(int i, boolean z, int i2) throws RemoteException;

    void onWfcStateChanged(int i, int i2) throws RemoteException;

    void onWifiPdnOOSStateChanged(int i, int i2) throws RemoteException;

    public static class Default implements IWifiOffloadListener {
        @Override // com.mediatek.wfo.IWifiOffloadListener
        public void onHandover(int simIdx, int stage, int ratType) throws RemoteException {
        }

        @Override // com.mediatek.wfo.IWifiOffloadListener
        public void onRoveOut(int simIdx, boolean roveOut, int rssi) throws RemoteException {
        }

        @Override // com.mediatek.wfo.IWifiOffloadListener
        public void onRequestImsSwitch(int simIdx, boolean isImsOn) throws RemoteException {
        }

        @Override // com.mediatek.wfo.IWifiOffloadListener
        public void onWifiPdnOOSStateChanged(int simIdx, int oosState) throws RemoteException {
        }

        @Override // com.mediatek.wfo.IWifiOffloadListener
        public void onAllowWifiOff() throws RemoteException {
        }

        @Override // com.mediatek.wfo.IWifiOffloadListener
        public void onWfcStateChanged(int simIdx, int state) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IWifiOffloadListener {
        private static final String DESCRIPTOR = "com.mediatek.wfo.IWifiOffloadListener";
        static final int TRANSACTION_onAllowWifiOff = 5;
        static final int TRANSACTION_onHandover = 1;
        static final int TRANSACTION_onRequestImsSwitch = 3;
        static final int TRANSACTION_onRoveOut = 2;
        static final int TRANSACTION_onWfcStateChanged = 6;
        static final int TRANSACTION_onWifiPdnOOSStateChanged = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IWifiOffloadListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IWifiOffloadListener)) {
                return new Proxy(obj);
            }
            return (IWifiOffloadListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                boolean _arg1 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        onHandover(data.readInt(), data.readInt(), data.readInt());
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        onRoveOut(_arg0, _arg1, data.readInt());
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        onRequestImsSwitch(_arg02, _arg1);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        onWifiPdnOOSStateChanged(data.readInt(), data.readInt());
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        onAllowWifiOff();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        onWfcStateChanged(data.readInt(), data.readInt());
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
        public static class Proxy implements IWifiOffloadListener {
            public static IWifiOffloadListener sDefaultImpl;
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

            @Override // com.mediatek.wfo.IWifiOffloadListener
            public void onHandover(int simIdx, int stage, int ratType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(simIdx);
                    _data.writeInt(stage);
                    _data.writeInt(ratType);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onHandover(simIdx, stage, ratType);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.mediatek.wfo.IWifiOffloadListener
            public void onRoveOut(int simIdx, boolean roveOut, int rssi) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(simIdx);
                    _data.writeInt(roveOut ? 1 : 0);
                    _data.writeInt(rssi);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onRoveOut(simIdx, roveOut, rssi);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.mediatek.wfo.IWifiOffloadListener
            public void onRequestImsSwitch(int simIdx, boolean isImsOn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(simIdx);
                    _data.writeInt(isImsOn ? 1 : 0);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onRequestImsSwitch(simIdx, isImsOn);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.mediatek.wfo.IWifiOffloadListener
            public void onWifiPdnOOSStateChanged(int simIdx, int oosState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(simIdx);
                    _data.writeInt(oosState);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onWifiPdnOOSStateChanged(simIdx, oosState);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.mediatek.wfo.IWifiOffloadListener
            public void onAllowWifiOff() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onAllowWifiOff();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.mediatek.wfo.IWifiOffloadListener
            public void onWfcStateChanged(int simIdx, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(simIdx);
                    _data.writeInt(state);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onWfcStateChanged(simIdx, state);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IWifiOffloadListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IWifiOffloadListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
