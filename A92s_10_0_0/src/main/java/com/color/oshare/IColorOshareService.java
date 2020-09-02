package com.color.oshare;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.color.oshare.IColorOshareCallback;

public interface IColorOshareService extends IInterface {
    void cancelTask(ColorOshareDevice colorOshareDevice) throws RemoteException;

    boolean isSendOn() throws RemoteException;

    void pause() throws RemoteException;

    void registerCallback(IColorOshareCallback iColorOshareCallback) throws RemoteException;

    void resume() throws RemoteException;

    void scan() throws RemoteException;

    void sendData(Intent intent, ColorOshareDevice colorOshareDevice) throws RemoteException;

    void stop() throws RemoteException;

    void switchSend(boolean z) throws RemoteException;

    void unregisterCallback(IColorOshareCallback iColorOshareCallback) throws RemoteException;

    public static class Default implements IColorOshareService {
        @Override // com.color.oshare.IColorOshareService
        public boolean isSendOn() throws RemoteException {
            return false;
        }

        @Override // com.color.oshare.IColorOshareService
        public void switchSend(boolean isOn) throws RemoteException {
        }

        @Override // com.color.oshare.IColorOshareService
        public void scan() throws RemoteException {
        }

        @Override // com.color.oshare.IColorOshareService
        public void registerCallback(IColorOshareCallback callback) throws RemoteException {
        }

        @Override // com.color.oshare.IColorOshareService
        public void unregisterCallback(IColorOshareCallback callback) throws RemoteException {
        }

        @Override // com.color.oshare.IColorOshareService
        public void sendData(Intent intent, ColorOshareDevice target) throws RemoteException {
        }

        @Override // com.color.oshare.IColorOshareService
        public void cancelTask(ColorOshareDevice device) throws RemoteException {
        }

        @Override // com.color.oshare.IColorOshareService
        public void pause() throws RemoteException {
        }

        @Override // com.color.oshare.IColorOshareService
        public void resume() throws RemoteException {
        }

        @Override // com.color.oshare.IColorOshareService
        public void stop() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IColorOshareService {
        private static final String DESCRIPTOR = "com.color.oshare.IColorOshareService";
        static final int TRANSACTION_cancelTask = 7;
        static final int TRANSACTION_isSendOn = 1;
        static final int TRANSACTION_pause = 8;
        static final int TRANSACTION_registerCallback = 4;
        static final int TRANSACTION_resume = 9;
        static final int TRANSACTION_scan = 3;
        static final int TRANSACTION_sendData = 6;
        static final int TRANSACTION_stop = 10;
        static final int TRANSACTION_switchSend = 2;
        static final int TRANSACTION_unregisterCallback = 5;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IColorOshareService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IColorOshareService)) {
                return new Proxy(obj);
            }
            return (IColorOshareService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "isSendOn";
                case 2:
                    return "switchSend";
                case 3:
                    return "scan";
                case 4:
                    return "registerCallback";
                case 5:
                    return "unregisterCallback";
                case 6:
                    return "sendData";
                case 7:
                    return "cancelTask";
                case 8:
                    return "pause";
                case 9:
                    return "resume";
                case 10:
                    return "stop";
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
            Intent _arg0;
            ColorOshareDevice _arg1;
            ColorOshareDevice _arg02;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isSendOn = isSendOn();
                        reply.writeNoException();
                        reply.writeInt(isSendOn ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        switchSend(data.readInt() != 0);
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        scan();
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        registerCallback(IColorOshareCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterCallback(IColorOshareCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg1 = ColorOshareDevice.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        sendData(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = ColorOshareDevice.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        cancelTask(_arg02);
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        pause();
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        resume();
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        stop();
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

        private static class Proxy implements IColorOshareService {
            public static IColorOshareService sDefaultImpl;
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

            @Override // com.color.oshare.IColorOshareService
            public boolean isSendOn() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSendOn();
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

            @Override // com.color.oshare.IColorOshareService
            public void switchSend(boolean isOn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isOn ? 1 : 0);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().switchSend(isOn);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.color.oshare.IColorOshareService
            public void scan() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().scan();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.color.oshare.IColorOshareService
            public void registerCallback(IColorOshareCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.color.oshare.IColorOshareService
            public void unregisterCallback(IColorOshareCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.color.oshare.IColorOshareService
            public void sendData(Intent intent, ColorOshareDevice target) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (target != null) {
                        _data.writeInt(1);
                        target.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().sendData(intent, target);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.color.oshare.IColorOshareService
            public void cancelTask(ColorOshareDevice device) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cancelTask(device);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.color.oshare.IColorOshareService
            public void pause() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().pause();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.color.oshare.IColorOshareService
            public void resume() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resume();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.color.oshare.IColorOshareService
            public void stop() throws RemoteException {
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
                    Stub.getDefaultImpl().stop();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IColorOshareService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IColorOshareService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
