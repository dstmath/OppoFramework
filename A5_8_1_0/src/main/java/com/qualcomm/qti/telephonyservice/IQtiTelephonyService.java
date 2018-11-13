package com.qualcomm.qti.telephonyservice;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IQtiTelephonyService extends IInterface {

    public static abstract class Stub extends Binder implements IQtiTelephonyService {
        private static final String DESCRIPTOR = "com.qualcomm.qti.telephonyservice.IQtiTelephonyService";
        static final int TRANSACTION_gbaInit = 2;
        static final int TRANSACTION_getImpi = 3;
        static final int TRANSACTION_getVersion = 1;

        private static class Proxy implements IQtiTelephonyService {
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

            public String getVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public KsNafResponse gbaInit(byte[] securityProtocol, String nafFullyQualifiedDomainName, int slotId, int applicationType, boolean forceBootStrapping) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    KsNafResponse _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(securityProtocol);
                    _data.writeString(nafFullyQualifiedDomainName);
                    _data.writeInt(slotId);
                    _data.writeInt(applicationType);
                    if (forceBootStrapping) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (KsNafResponse) KsNafResponse.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] getImpi(int slotId, int applicationType, boolean secure) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(slotId);
                    _data.writeInt(applicationType);
                    if (secure) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
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

        public static IQtiTelephonyService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IQtiTelephonyService)) {
                return new Proxy(obj);
            }
            return (IQtiTelephonyService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    String _result = getVersion();
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    KsNafResponse _result2 = gbaInit(data.createByteArray(), data.readString(), data.readInt(), data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(1);
                        _result2.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    byte[] _result3 = getImpi(data.readInt(), data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeByteArray(_result3);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    KsNafResponse gbaInit(byte[] bArr, String str, int i, int i2, boolean z) throws RemoteException;

    byte[] getImpi(int i, int i2, boolean z) throws RemoteException;

    String getVersion() throws RemoteException;
}
