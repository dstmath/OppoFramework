package com.st.android.nfc_extensions;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.st.android.nfc_extensions.INfcWalletLogCallback;
import com.st.android.nfc_extensions.INfceeActionNtfCallback;

public interface INfcWalletAdapter extends IInterface {
    boolean keepEseSwpActive(boolean z) throws RemoteException;

    boolean registerNfceeActionNtfCallback(INfceeActionNtfCallback iNfceeActionNtfCallback) throws RemoteException;

    boolean registerStLogCallback(INfcWalletLogCallback iNfcWalletLogCallback) throws RemoteException;

    boolean rotateRfParameters(boolean z) throws RemoteException;

    boolean setMuteTech(boolean z, boolean z2, boolean z3) throws RemoteException;

    boolean setObserverMode(boolean z) throws RemoteException;

    boolean setSEFelicaCardEnabled(boolean z) throws RemoteException;

    boolean unregisterNfceeActionNtfCallback() throws RemoteException;

    boolean unregisterStLogCallback() throws RemoteException;

    public static class Default implements INfcWalletAdapter {
        @Override // com.st.android.nfc_extensions.INfcWalletAdapter
        public boolean keepEseSwpActive(boolean enable) throws RemoteException {
            return false;
        }

        @Override // com.st.android.nfc_extensions.INfcWalletAdapter
        public boolean registerStLogCallback(INfcWalletLogCallback cb) throws RemoteException {
            return false;
        }

        @Override // com.st.android.nfc_extensions.INfcWalletAdapter
        public boolean unregisterStLogCallback() throws RemoteException {
            return false;
        }

        @Override // com.st.android.nfc_extensions.INfcWalletAdapter
        public boolean setObserverMode(boolean enable) throws RemoteException {
            return false;
        }

        @Override // com.st.android.nfc_extensions.INfcWalletAdapter
        public boolean setMuteTech(boolean muteA, boolean muteB, boolean muteF) throws RemoteException {
            return false;
        }

        @Override // com.st.android.nfc_extensions.INfcWalletAdapter
        public boolean rotateRfParameters(boolean reset) throws RemoteException {
            return false;
        }

        @Override // com.st.android.nfc_extensions.INfcWalletAdapter
        public boolean setSEFelicaCardEnabled(boolean status) throws RemoteException {
            return false;
        }

        @Override // com.st.android.nfc_extensions.INfcWalletAdapter
        public boolean registerNfceeActionNtfCallback(INfceeActionNtfCallback cb) throws RemoteException {
            return false;
        }

        @Override // com.st.android.nfc_extensions.INfcWalletAdapter
        public boolean unregisterNfceeActionNtfCallback() throws RemoteException {
            return false;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INfcWalletAdapter {
        private static final String DESCRIPTOR = "com.st.android.nfc_extensions.INfcWalletAdapter";
        static final int TRANSACTION_keepEseSwpActive = 1;
        static final int TRANSACTION_registerNfceeActionNtfCallback = 8;
        static final int TRANSACTION_registerStLogCallback = 2;
        static final int TRANSACTION_rotateRfParameters = 6;
        static final int TRANSACTION_setMuteTech = 5;
        static final int TRANSACTION_setObserverMode = 4;
        static final int TRANSACTION_setSEFelicaCardEnabled = 7;
        static final int TRANSACTION_unregisterNfceeActionNtfCallback = 9;
        static final int TRANSACTION_unregisterStLogCallback = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INfcWalletAdapter asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INfcWalletAdapter)) {
                return new Proxy(obj);
            }
            return (INfcWalletAdapter) iin;
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
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        boolean keepEseSwpActive = keepEseSwpActive(_arg0);
                        reply.writeNoException();
                        reply.writeInt(keepEseSwpActive ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerStLogCallback = registerStLogCallback(INfcWalletLogCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerStLogCallback ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean unregisterStLogCallback = unregisterStLogCallback();
                        reply.writeNoException();
                        reply.writeInt(unregisterStLogCallback ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        boolean observerMode = setObserverMode(_arg0);
                        reply.writeNoException();
                        reply.writeInt(observerMode ? 1 : 0);
                        return true;
                    case TRANSACTION_setMuteTech /* 5 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _arg02 = data.readInt() != 0;
                        boolean _arg1 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        boolean muteTech = setMuteTech(_arg02, _arg1, _arg0);
                        reply.writeNoException();
                        reply.writeInt(muteTech ? 1 : 0);
                        return true;
                    case TRANSACTION_rotateRfParameters /* 6 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        boolean rotateRfParameters = rotateRfParameters(_arg0);
                        reply.writeNoException();
                        reply.writeInt(rotateRfParameters ? 1 : 0);
                        return true;
                    case TRANSACTION_setSEFelicaCardEnabled /* 7 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        boolean sEFelicaCardEnabled = setSEFelicaCardEnabled(_arg0);
                        reply.writeNoException();
                        reply.writeInt(sEFelicaCardEnabled ? 1 : 0);
                        return true;
                    case TRANSACTION_registerNfceeActionNtfCallback /* 8 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerNfceeActionNtfCallback = registerNfceeActionNtfCallback(INfceeActionNtfCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerNfceeActionNtfCallback ? 1 : 0);
                        return true;
                    case TRANSACTION_unregisterNfceeActionNtfCallback /* 9 */:
                        data.enforceInterface(DESCRIPTOR);
                        boolean unregisterNfceeActionNtfCallback = unregisterNfceeActionNtfCallback();
                        reply.writeNoException();
                        reply.writeInt(unregisterNfceeActionNtfCallback ? 1 : 0);
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
        public static class Proxy implements INfcWalletAdapter {
            public static INfcWalletAdapter sDefaultImpl;
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

            @Override // com.st.android.nfc_extensions.INfcWalletAdapter
            public boolean keepEseSwpActive(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(enable ? 1 : 0);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().keepEseSwpActive(enable);
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

            @Override // com.st.android.nfc_extensions.INfcWalletAdapter
            public boolean registerStLogCallback(INfcWalletLogCallback cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerStLogCallback(cb);
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

            @Override // com.st.android.nfc_extensions.INfcWalletAdapter
            public boolean unregisterStLogCallback() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unregisterStLogCallback();
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

            @Override // com.st.android.nfc_extensions.INfcWalletAdapter
            public boolean setObserverMode(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(enable ? 1 : 0);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setObserverMode(enable);
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

            @Override // com.st.android.nfc_extensions.INfcWalletAdapter
            public boolean setMuteTech(boolean muteA, boolean muteB, boolean muteF) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(muteA ? 1 : 0);
                    _data.writeInt(muteB ? 1 : 0);
                    _data.writeInt(muteF ? 1 : 0);
                    if (!this.mRemote.transact(Stub.TRANSACTION_setMuteTech, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setMuteTech(muteA, muteB, muteF);
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

            @Override // com.st.android.nfc_extensions.INfcWalletAdapter
            public boolean rotateRfParameters(boolean reset) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(reset ? 1 : 0);
                    if (!this.mRemote.transact(Stub.TRANSACTION_rotateRfParameters, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().rotateRfParameters(reset);
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

            @Override // com.st.android.nfc_extensions.INfcWalletAdapter
            public boolean setSEFelicaCardEnabled(boolean status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    _data.writeInt(status ? 1 : 0);
                    if (!this.mRemote.transact(Stub.TRANSACTION_setSEFelicaCardEnabled, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setSEFelicaCardEnabled(status);
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

            @Override // com.st.android.nfc_extensions.INfcWalletAdapter
            public boolean registerNfceeActionNtfCallback(INfceeActionNtfCallback cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_registerNfceeActionNtfCallback, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerNfceeActionNtfCallback(cb);
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

            @Override // com.st.android.nfc_extensions.INfcWalletAdapter
            public boolean unregisterNfceeActionNtfCallback() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(Stub.TRANSACTION_unregisterNfceeActionNtfCallback, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unregisterNfceeActionNtfCallback();
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

        public static boolean setDefaultImpl(INfcWalletAdapter impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INfcWalletAdapter getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
