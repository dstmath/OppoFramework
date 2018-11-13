package com.qualcomm.qti.lpa;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IUimLpaService extends IInterface {

    public static abstract class Stub extends Binder implements IUimLpaService {
        private static final String DESCRIPTOR = "com.qualcomm.qti.lpa.IUimLpaService";
        static final int TRANSACTION_deregisterCallback = 2;
        static final int TRANSACTION_registerCallback = 1;
        static final int TRANSACTION_uimLpaAddProfile = 3;
        static final int TRANSACTION_uimLpaDeleteProfile = 6;
        static final int TRANSACTION_uimLpaDisableProfile = 5;
        static final int TRANSACTION_uimLpaEnableProfile = 4;
        static final int TRANSACTION_uimLpaGetEid = 10;
        static final int TRANSACTION_uimLpaGetProfiles = 9;
        static final int TRANSACTION_uimLpaGetSrvAddr = 12;
        static final int TRANSACTION_uimLpaSetSrvAddr = 13;
        static final int TRANSACTION_uimLpaUpdateNickname = 7;
        static final int TRANSACTION_uimLpaUserConsent = 11;
        static final int TRANSACTION_uimLpaeUICCMemoryReset = 8;

        private static class Proxy implements IUimLpaService {
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

            public int registerCallback(IUimLpaServiceCallback cb) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (cb != null) {
                        iBinder = cb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int deregisterCallback(IUimLpaServiceCallback cb) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (cb != null) {
                        iBinder = cb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int uimLpaAddProfile(int slot, int token, String activationCode, String confirmationCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(token);
                    _data.writeString(activationCode);
                    _data.writeString(confirmationCode);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int uimLpaEnableProfile(int slot, int token, byte[] iccid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(token);
                    _data.writeByteArray(iccid);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int uimLpaDisableProfile(int slot, int token, byte[] iccid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(token);
                    _data.writeByteArray(iccid);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int uimLpaDeleteProfile(int slot, int token, byte[] iccid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(token);
                    _data.writeByteArray(iccid);
                    this.mRemote.transact(Stub.TRANSACTION_uimLpaDeleteProfile, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int uimLpaUpdateNickname(int slot, int token, byte[] iccid, String nickname) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(token);
                    _data.writeByteArray(iccid);
                    _data.writeString(nickname);
                    this.mRemote.transact(Stub.TRANSACTION_uimLpaUpdateNickname, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int uimLpaeUICCMemoryReset(int slot, int token, int option) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(token);
                    _data.writeInt(option);
                    this.mRemote.transact(Stub.TRANSACTION_uimLpaeUICCMemoryReset, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int uimLpaGetProfiles(int slot, int token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(token);
                    this.mRemote.transact(Stub.TRANSACTION_uimLpaGetProfiles, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int uimLpaGetEid(int slot, int token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(token);
                    this.mRemote.transact(Stub.TRANSACTION_uimLpaGetEid, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int uimLpaUserConsent(int slot, int token, boolean userOk) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(token);
                    if (userOk) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_uimLpaUserConsent, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int uimLpaGetSrvAddr(int slot, int token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(token);
                    this.mRemote.transact(Stub.TRANSACTION_uimLpaGetSrvAddr, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int uimLpaSetSrvAddr(int slot, int token, String smdp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(token);
                    _data.writeString(smdp);
                    this.mRemote.transact(Stub.TRANSACTION_uimLpaSetSrvAddr, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IUimLpaService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IUimLpaService)) {
                return new Proxy(obj);
            }
            return (IUimLpaService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    _result = registerCallback(com.qualcomm.qti.lpa.IUimLpaServiceCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    _result = deregisterCallback(com.qualcomm.qti.lpa.IUimLpaServiceCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    _result = uimLpaAddProfile(data.readInt(), data.readInt(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _result = uimLpaEnableProfile(data.readInt(), data.readInt(), data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    _result = uimLpaDisableProfile(data.readInt(), data.readInt(), data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_uimLpaDeleteProfile /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = uimLpaDeleteProfile(data.readInt(), data.readInt(), data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_uimLpaUpdateNickname /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = uimLpaUpdateNickname(data.readInt(), data.readInt(), data.createByteArray(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_uimLpaeUICCMemoryReset /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = uimLpaeUICCMemoryReset(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_uimLpaGetProfiles /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = uimLpaGetProfiles(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_uimLpaGetEid /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = uimLpaGetEid(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_uimLpaUserConsent /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = uimLpaUserConsent(data.readInt(), data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_uimLpaGetSrvAddr /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = uimLpaGetSrvAddr(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_uimLpaSetSrvAddr /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = uimLpaSetSrvAddr(data.readInt(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int deregisterCallback(IUimLpaServiceCallback iUimLpaServiceCallback) throws RemoteException;

    int registerCallback(IUimLpaServiceCallback iUimLpaServiceCallback) throws RemoteException;

    int uimLpaAddProfile(int i, int i2, String str, String str2) throws RemoteException;

    int uimLpaDeleteProfile(int i, int i2, byte[] bArr) throws RemoteException;

    int uimLpaDisableProfile(int i, int i2, byte[] bArr) throws RemoteException;

    int uimLpaEnableProfile(int i, int i2, byte[] bArr) throws RemoteException;

    int uimLpaGetEid(int i, int i2) throws RemoteException;

    int uimLpaGetProfiles(int i, int i2) throws RemoteException;

    int uimLpaGetSrvAddr(int i, int i2) throws RemoteException;

    int uimLpaSetSrvAddr(int i, int i2, String str) throws RemoteException;

    int uimLpaUpdateNickname(int i, int i2, byte[] bArr, String str) throws RemoteException;

    int uimLpaUserConsent(int i, int i2, boolean z) throws RemoteException;

    int uimLpaeUICCMemoryReset(int i, int i2, int i3) throws RemoteException;
}
