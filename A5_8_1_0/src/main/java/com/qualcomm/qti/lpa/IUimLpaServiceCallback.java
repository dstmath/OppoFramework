package com.qualcomm.qti.lpa;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IUimLpaServiceCallback extends IInterface {

    public static abstract class Stub extends Binder implements IUimLpaServiceCallback {
        private static final String DESCRIPTOR = "com.qualcomm.qti.lpa.IUimLpaServiceCallback";
        static final int TRANSACTION_uimLpaAddProfileResponse = 1;
        static final int TRANSACTION_uimLpaDeleteProfileResponse = 4;
        static final int TRANSACTION_uimLpaDisableProfileResponse = 3;
        static final int TRANSACTION_uimLpaDownloadProgressIndication = 11;
        static final int TRANSACTION_uimLpaEnableProfileResponse = 2;
        static final int TRANSACTION_uimLpaGetEidResponse = 8;
        static final int TRANSACTION_uimLpaGetProfilesResponse = 7;
        static final int TRANSACTION_uimLpaGetSrvAddrResponse = 9;
        static final int TRANSACTION_uimLpaRadioStateIndication = 12;
        static final int TRANSACTION_uimLpaSetSrvAddrResponse = 10;
        static final int TRANSACTION_uimLpaUpdateNicknameResponse = 5;
        static final int TRANSACTION_uimLpaeUICCMemoryResetResponse = 6;

        private static class Proxy implements IUimLpaServiceCallback {
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

            public void uimLpaAddProfileResponse(int slot, int token, int responseCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(token);
                    _data.writeInt(responseCode);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void uimLpaEnableProfileResponse(int slot, int token, int responseCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(token);
                    _data.writeInt(responseCode);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void uimLpaDisableProfileResponse(int slot, int token, int responseCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(token);
                    _data.writeInt(responseCode);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void uimLpaDeleteProfileResponse(int slot, int token, int responseCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(token);
                    _data.writeInt(responseCode);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void uimLpaUpdateNicknameResponse(int slot, int token, int responseCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(token);
                    _data.writeInt(responseCode);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void uimLpaeUICCMemoryResetResponse(int slot, int token, int responseCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(token);
                    _data.writeInt(responseCode);
                    this.mRemote.transact(Stub.TRANSACTION_uimLpaeUICCMemoryResetResponse, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void uimLpaGetProfilesResponse(int slot, int token, int responseCode, UimLpaProfile[] profiles) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(token);
                    _data.writeInt(responseCode);
                    _data.writeTypedArray(profiles, 0);
                    this.mRemote.transact(Stub.TRANSACTION_uimLpaGetProfilesResponse, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void uimLpaGetEidResponse(int slot, int token, int responseCode, byte[] eid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(token);
                    _data.writeInt(responseCode);
                    _data.writeByteArray(eid);
                    this.mRemote.transact(Stub.TRANSACTION_uimLpaGetEidResponse, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void uimLpaGetSrvAddrResponse(int slot, int token, int responseCode, String smdp, String smds) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(token);
                    _data.writeInt(responseCode);
                    _data.writeString(smdp);
                    _data.writeString(smds);
                    this.mRemote.transact(Stub.TRANSACTION_uimLpaGetSrvAddrResponse, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void uimLpaSetSrvAddrResponse(int slot, int token, int responseCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(token);
                    _data.writeInt(responseCode);
                    this.mRemote.transact(Stub.TRANSACTION_uimLpaSetSrvAddrResponse, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void uimLpaDownloadProgressIndication(int slot, int responseCode, UimLpaDownloadProgress progress) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(responseCode);
                    if (progress != null) {
                        _data.writeInt(1);
                        progress.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_uimLpaDownloadProgressIndication, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void uimLpaRadioStateIndication(int slot, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slot);
                    _data.writeInt(state);
                    this.mRemote.transact(Stub.TRANSACTION_uimLpaRadioStateIndication, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IUimLpaServiceCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IUimLpaServiceCallback)) {
                return new Proxy(obj);
            }
            return (IUimLpaServiceCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    uimLpaAddProfileResponse(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    uimLpaEnableProfileResponse(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    uimLpaDisableProfileResponse(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    uimLpaDeleteProfileResponse(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    uimLpaUpdateNicknameResponse(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_uimLpaeUICCMemoryResetResponse /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    uimLpaeUICCMemoryResetResponse(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_uimLpaGetProfilesResponse /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    uimLpaGetProfilesResponse(data.readInt(), data.readInt(), data.readInt(), (UimLpaProfile[]) data.createTypedArray(UimLpaProfile.CREATOR));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_uimLpaGetEidResponse /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    uimLpaGetEidResponse(data.readInt(), data.readInt(), data.readInt(), data.createByteArray());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_uimLpaGetSrvAddrResponse /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    uimLpaGetSrvAddrResponse(data.readInt(), data.readInt(), data.readInt(), data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_uimLpaSetSrvAddrResponse /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    uimLpaSetSrvAddrResponse(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_uimLpaDownloadProgressIndication /*11*/:
                    UimLpaDownloadProgress _arg2;
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0 = data.readInt();
                    int _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg2 = (UimLpaDownloadProgress) UimLpaDownloadProgress.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    uimLpaDownloadProgressIndication(_arg0, _arg1, _arg2);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_uimLpaRadioStateIndication /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    uimLpaRadioStateIndication(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void uimLpaAddProfileResponse(int i, int i2, int i3) throws RemoteException;

    void uimLpaDeleteProfileResponse(int i, int i2, int i3) throws RemoteException;

    void uimLpaDisableProfileResponse(int i, int i2, int i3) throws RemoteException;

    void uimLpaDownloadProgressIndication(int i, int i2, UimLpaDownloadProgress uimLpaDownloadProgress) throws RemoteException;

    void uimLpaEnableProfileResponse(int i, int i2, int i3) throws RemoteException;

    void uimLpaGetEidResponse(int i, int i2, int i3, byte[] bArr) throws RemoteException;

    void uimLpaGetProfilesResponse(int i, int i2, int i3, UimLpaProfile[] uimLpaProfileArr) throws RemoteException;

    void uimLpaGetSrvAddrResponse(int i, int i2, int i3, String str, String str2) throws RemoteException;

    void uimLpaRadioStateIndication(int i, int i2) throws RemoteException;

    void uimLpaSetSrvAddrResponse(int i, int i2, int i3) throws RemoteException;

    void uimLpaUpdateNicknameResponse(int i, int i2, int i3) throws RemoteException;

    void uimLpaeUICCMemoryResetResponse(int i, int i2, int i3) throws RemoteException;
}
