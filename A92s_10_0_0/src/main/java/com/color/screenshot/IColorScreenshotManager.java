package com.color.screenshot;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IColorScreenshotManager extends IInterface {
    boolean isLongshotDisabled() throws RemoteException;

    boolean isLongshotEnabled() throws RemoteException;

    boolean isLongshotMode() throws RemoteException;

    boolean isScreenshotEdit() throws RemoteException;

    boolean isScreenshotEnabled() throws RemoteException;

    boolean isScreenshotMode() throws RemoteException;

    boolean isScreenshotSupported() throws RemoteException;

    void notifyOverScroll(ColorLongshotEvent colorLongshotEvent) throws RemoteException;

    void reportLongshotDumpResult(ColorLongshotDump colorLongshotDump) throws RemoteException;

    void setLongshotEnabled(boolean z) throws RemoteException;

    void setScreenshotEnabled(boolean z) throws RemoteException;

    void stopLongshot() throws RemoteException;

    void takeLongshot(boolean z, boolean z2) throws RemoteException;

    void takeScreenshot(Bundle bundle) throws RemoteException;

    public static class Default implements IColorScreenshotManager {
        @Override // com.color.screenshot.IColorScreenshotManager
        public void takeScreenshot(Bundle extras) throws RemoteException {
        }

        @Override // com.color.screenshot.IColorScreenshotManager
        public boolean isScreenshotMode() throws RemoteException {
            return false;
        }

        @Override // com.color.screenshot.IColorScreenshotManager
        public boolean isScreenshotEdit() throws RemoteException {
            return false;
        }

        @Override // com.color.screenshot.IColorScreenshotManager
        public void takeLongshot(boolean statusBarVisible, boolean navBarVisible) throws RemoteException {
        }

        @Override // com.color.screenshot.IColorScreenshotManager
        public void stopLongshot() throws RemoteException {
        }

        @Override // com.color.screenshot.IColorScreenshotManager
        public boolean isLongshotMode() throws RemoteException {
            return false;
        }

        @Override // com.color.screenshot.IColorScreenshotManager
        public boolean isLongshotDisabled() throws RemoteException {
            return false;
        }

        @Override // com.color.screenshot.IColorScreenshotManager
        public void reportLongshotDumpResult(ColorLongshotDump result) throws RemoteException {
        }

        @Override // com.color.screenshot.IColorScreenshotManager
        public boolean isScreenshotSupported() throws RemoteException {
            return false;
        }

        @Override // com.color.screenshot.IColorScreenshotManager
        public void setScreenshotEnabled(boolean enabled) throws RemoteException {
        }

        @Override // com.color.screenshot.IColorScreenshotManager
        public boolean isScreenshotEnabled() throws RemoteException {
            return false;
        }

        @Override // com.color.screenshot.IColorScreenshotManager
        public void setLongshotEnabled(boolean enabled) throws RemoteException {
        }

        @Override // com.color.screenshot.IColorScreenshotManager
        public boolean isLongshotEnabled() throws RemoteException {
            return false;
        }

        @Override // com.color.screenshot.IColorScreenshotManager
        public void notifyOverScroll(ColorLongshotEvent event) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IColorScreenshotManager {
        private static final String DESCRIPTOR = "com.color.screenshot.IColorScreenshotManager";
        static final int TRANSACTION_isLongshotDisabled = 7;
        static final int TRANSACTION_isLongshotEnabled = 13;
        static final int TRANSACTION_isLongshotMode = 6;
        static final int TRANSACTION_isScreenshotEdit = 3;
        static final int TRANSACTION_isScreenshotEnabled = 11;
        static final int TRANSACTION_isScreenshotMode = 2;
        static final int TRANSACTION_isScreenshotSupported = 9;
        static final int TRANSACTION_notifyOverScroll = 14;
        static final int TRANSACTION_reportLongshotDumpResult = 8;
        static final int TRANSACTION_setLongshotEnabled = 12;
        static final int TRANSACTION_setScreenshotEnabled = 10;
        static final int TRANSACTION_stopLongshot = 5;
        static final int TRANSACTION_takeLongshot = 4;
        static final int TRANSACTION_takeScreenshot = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IColorScreenshotManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IColorScreenshotManager)) {
                return new Proxy(obj);
            }
            return (IColorScreenshotManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "takeScreenshot";
                case 2:
                    return "isScreenshotMode";
                case 3:
                    return "isScreenshotEdit";
                case 4:
                    return "takeLongshot";
                case 5:
                    return "stopLongshot";
                case 6:
                    return "isLongshotMode";
                case 7:
                    return "isLongshotDisabled";
                case 8:
                    return "reportLongshotDumpResult";
                case 9:
                    return "isScreenshotSupported";
                case 10:
                    return "setScreenshotEnabled";
                case 11:
                    return "isScreenshotEnabled";
                case 12:
                    return "setLongshotEnabled";
                case 13:
                    return "isLongshotEnabled";
                case 14:
                    return "notifyOverScroll";
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
            ColorLongshotDump _arg02;
            ColorLongshotEvent _arg03;
            if (code != 1598968902) {
                boolean _arg1 = false;
                boolean _arg04 = false;
                boolean _arg05 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        takeScreenshot(_arg0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isScreenshotMode = isScreenshotMode();
                        reply.writeNoException();
                        reply.writeInt(isScreenshotMode ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isScreenshotEdit = isScreenshotEdit();
                        reply.writeNoException();
                        reply.writeInt(isScreenshotEdit ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _arg06 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        takeLongshot(_arg06, _arg1);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        stopLongshot();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isLongshotMode = isLongshotMode();
                        reply.writeNoException();
                        reply.writeInt(isLongshotMode ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isLongshotDisabled = isLongshotDisabled();
                        reply.writeNoException();
                        reply.writeInt(isLongshotDisabled ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = ColorLongshotDump.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        reportLongshotDumpResult(_arg02);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isScreenshotSupported = isScreenshotSupported();
                        reply.writeNoException();
                        reply.writeInt(isScreenshotSupported ? 1 : 0);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = true;
                        }
                        setScreenshotEnabled(_arg05);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isScreenshotEnabled = isScreenshotEnabled();
                        reply.writeNoException();
                        reply.writeInt(isScreenshotEnabled ? 1 : 0);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = true;
                        }
                        setLongshotEnabled(_arg04);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isLongshotEnabled = isLongshotEnabled();
                        reply.writeNoException();
                        reply.writeInt(isLongshotEnabled ? 1 : 0);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = ColorLongshotEvent.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        notifyOverScroll(_arg03);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IColorScreenshotManager {
            public static IColorScreenshotManager sDefaultImpl;
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

            @Override // com.color.screenshot.IColorScreenshotManager
            public void takeScreenshot(Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().takeScreenshot(extras);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.color.screenshot.IColorScreenshotManager
            public boolean isScreenshotMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isScreenshotMode();
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

            @Override // com.color.screenshot.IColorScreenshotManager
            public boolean isScreenshotEdit() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isScreenshotEdit();
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

            @Override // com.color.screenshot.IColorScreenshotManager
            public void takeLongshot(boolean statusBarVisible, boolean navBarVisible) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 0;
                    _data.writeInt(statusBarVisible ? 1 : 0);
                    if (navBarVisible) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().takeLongshot(statusBarVisible, navBarVisible);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.color.screenshot.IColorScreenshotManager
            public void stopLongshot() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().stopLongshot();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.color.screenshot.IColorScreenshotManager
            public boolean isLongshotMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isLongshotMode();
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

            @Override // com.color.screenshot.IColorScreenshotManager
            public boolean isLongshotDisabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isLongshotDisabled();
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

            @Override // com.color.screenshot.IColorScreenshotManager
            public void reportLongshotDumpResult(ColorLongshotDump result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (result != null) {
                        _data.writeInt(1);
                        result.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().reportLongshotDumpResult(result);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.color.screenshot.IColorScreenshotManager
            public boolean isScreenshotSupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isScreenshotSupported();
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

            @Override // com.color.screenshot.IColorScreenshotManager
            public void setScreenshotEnabled(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled ? 1 : 0);
                    if (this.mRemote.transact(10, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setScreenshotEnabled(enabled);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.color.screenshot.IColorScreenshotManager
            public boolean isScreenshotEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isScreenshotEnabled();
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

            @Override // com.color.screenshot.IColorScreenshotManager
            public void setLongshotEnabled(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled ? 1 : 0);
                    if (this.mRemote.transact(12, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setLongshotEnabled(enabled);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.color.screenshot.IColorScreenshotManager
            public boolean isLongshotEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isLongshotEnabled();
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

            @Override // com.color.screenshot.IColorScreenshotManager
            public void notifyOverScroll(ColorLongshotEvent event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (event != null) {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(14, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyOverScroll(event);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IColorScreenshotManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IColorScreenshotManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
