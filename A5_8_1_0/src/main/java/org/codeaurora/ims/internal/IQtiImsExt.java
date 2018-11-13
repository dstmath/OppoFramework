package org.codeaurora.ims.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IQtiImsExt extends IInterface {

    public static abstract class Stub extends Binder implements IQtiImsExt {
        private static final String DESCRIPTOR = "org.codeaurora.ims.internal.IQtiImsExt";
        static final int TRANSACTION_getCallForwardUncondTimer = 2;
        static final int TRANSACTION_getHandoverConfig = 14;
        static final int TRANSACTION_getPacketCount = 3;
        static final int TRANSACTION_getPacketErrorCount = 4;
        static final int TRANSACTION_getRcsAppConfig = 16;
        static final int TRANSACTION_getVvmAppConfig = 18;
        static final int TRANSACTION_querySsacStatus = 9;
        static final int TRANSACTION_queryVoltePreference = 13;
        static final int TRANSACTION_queryVopsStatus = 8;
        static final int TRANSACTION_registerForParticipantStatusInfo = 11;
        static final int TRANSACTION_resumePendingCall = 10;
        static final int TRANSACTION_sendCallDeflectRequest = 5;
        static final int TRANSACTION_sendCallTransferRequest = 6;
        static final int TRANSACTION_sendCancelModifyCall = 7;
        static final int TRANSACTION_setCallForwardUncondTimer = 1;
        static final int TRANSACTION_setHandoverConfig = 15;
        static final int TRANSACTION_setRcsAppConfig = 17;
        static final int TRANSACTION_setVvmAppConfig = 19;
        static final int TRANSACTION_updateVoltePreference = 12;

        private static class Proxy implements IQtiImsExt {
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

            public void setCallForwardUncondTimer(int phoneId, int startHour, int startMinute, int endHour, int endMinute, int action, int reason, int serviceClass, String dialingNumber, IQtiImsExtListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(startHour);
                    _data.writeInt(startMinute);
                    _data.writeInt(endHour);
                    _data.writeInt(endMinute);
                    _data.writeInt(action);
                    _data.writeInt(reason);
                    _data.writeInt(serviceClass);
                    _data.writeString(dialingNumber);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void getCallForwardUncondTimer(int phoneId, int reason, int serviceClass, IQtiImsExtListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(reason);
                    _data.writeInt(serviceClass);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void getPacketCount(int phoneId, IQtiImsExtListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void getPacketErrorCount(int phoneId, IQtiImsExtListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void sendCallDeflectRequest(int phoneId, String deflectNumber, IQtiImsExtListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeString(deflectNumber);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void sendCallTransferRequest(int phoneId, int type, String number, IQtiImsExtListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(type);
                    _data.writeString(number);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void sendCancelModifyCall(int phoneId, IQtiImsExtListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void queryVopsStatus(int phoneId, IQtiImsExtListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void querySsacStatus(int phoneId, IQtiImsExtListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void resumePendingCall(int phoneId, int videoState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(videoState);
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void registerForParticipantStatusInfo(int phoneId, IQtiImsExtListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(11, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void updateVoltePreference(int phoneId, int preference, IQtiImsExtListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(preference);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(12, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void queryVoltePreference(int phoneId, IQtiImsExtListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(13, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void getHandoverConfig(int phoneId, IQtiImsExtListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(14, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setHandoverConfig(int phoneId, int hoConfig, IQtiImsExtListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(hoConfig);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(15, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public int getRcsAppConfig(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setRcsAppConfig(int phoneId, int defaultSmsApp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(defaultSmsApp);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getVvmAppConfig(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setVvmAppConfig(int phoneId, int defaultVvmApp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeInt(defaultVvmApp);
                    this.mRemote.transact(19, _data, _reply, 0);
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

        public static IQtiImsExt asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IQtiImsExt)) {
                return new Proxy(obj);
            }
            return (IQtiImsExt) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    setCallForwardUncondTimer(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.readString(), org.codeaurora.ims.internal.IQtiImsExtListener.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    getCallForwardUncondTimer(data.readInt(), data.readInt(), data.readInt(), org.codeaurora.ims.internal.IQtiImsExtListener.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    getPacketCount(data.readInt(), org.codeaurora.ims.internal.IQtiImsExtListener.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    getPacketErrorCount(data.readInt(), org.codeaurora.ims.internal.IQtiImsExtListener.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    sendCallDeflectRequest(data.readInt(), data.readString(), org.codeaurora.ims.internal.IQtiImsExtListener.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    sendCallTransferRequest(data.readInt(), data.readInt(), data.readString(), org.codeaurora.ims.internal.IQtiImsExtListener.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    sendCancelModifyCall(data.readInt(), org.codeaurora.ims.internal.IQtiImsExtListener.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    queryVopsStatus(data.readInt(), org.codeaurora.ims.internal.IQtiImsExtListener.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    querySsacStatus(data.readInt(), org.codeaurora.ims.internal.IQtiImsExtListener.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    resumePendingCall(data.readInt(), data.readInt());
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    registerForParticipantStatusInfo(data.readInt(), org.codeaurora.ims.internal.IQtiImsExtListener.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    updateVoltePreference(data.readInt(), data.readInt(), org.codeaurora.ims.internal.IQtiImsExtListener.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    queryVoltePreference(data.readInt(), org.codeaurora.ims.internal.IQtiImsExtListener.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 14:
                    data.enforceInterface(DESCRIPTOR);
                    getHandoverConfig(data.readInt(), org.codeaurora.ims.internal.IQtiImsExtListener.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 15:
                    data.enforceInterface(DESCRIPTOR);
                    setHandoverConfig(data.readInt(), data.readInt(), org.codeaurora.ims.internal.IQtiImsExtListener.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getRcsAppConfig(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 17:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setRcsAppConfig(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 18:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getVvmAppConfig(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 19:
                    data.enforceInterface(DESCRIPTOR);
                    _result = setVvmAppConfig(data.readInt(), data.readInt());
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

    void getCallForwardUncondTimer(int i, int i2, int i3, IQtiImsExtListener iQtiImsExtListener) throws RemoteException;

    void getHandoverConfig(int i, IQtiImsExtListener iQtiImsExtListener) throws RemoteException;

    void getPacketCount(int i, IQtiImsExtListener iQtiImsExtListener) throws RemoteException;

    void getPacketErrorCount(int i, IQtiImsExtListener iQtiImsExtListener) throws RemoteException;

    int getRcsAppConfig(int i) throws RemoteException;

    int getVvmAppConfig(int i) throws RemoteException;

    void querySsacStatus(int i, IQtiImsExtListener iQtiImsExtListener) throws RemoteException;

    void queryVoltePreference(int i, IQtiImsExtListener iQtiImsExtListener) throws RemoteException;

    void queryVopsStatus(int i, IQtiImsExtListener iQtiImsExtListener) throws RemoteException;

    void registerForParticipantStatusInfo(int i, IQtiImsExtListener iQtiImsExtListener) throws RemoteException;

    void resumePendingCall(int i, int i2) throws RemoteException;

    void sendCallDeflectRequest(int i, String str, IQtiImsExtListener iQtiImsExtListener) throws RemoteException;

    void sendCallTransferRequest(int i, int i2, String str, IQtiImsExtListener iQtiImsExtListener) throws RemoteException;

    void sendCancelModifyCall(int i, IQtiImsExtListener iQtiImsExtListener) throws RemoteException;

    void setCallForwardUncondTimer(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, String str, IQtiImsExtListener iQtiImsExtListener) throws RemoteException;

    void setHandoverConfig(int i, int i2, IQtiImsExtListener iQtiImsExtListener) throws RemoteException;

    int setRcsAppConfig(int i, int i2) throws RemoteException;

    int setVvmAppConfig(int i, int i2) throws RemoteException;

    void updateVoltePreference(int i, int i2, IQtiImsExtListener iQtiImsExtListener) throws RemoteException;
}
