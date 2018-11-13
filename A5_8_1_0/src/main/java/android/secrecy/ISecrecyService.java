package android.secrecy;

import android.content.pm.ActivityInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISecrecyService extends IInterface {

    public static abstract class Stub extends Binder implements ISecrecyService {
        private static final String DESCRIPTOR = "android.secrecy.ISecrecyService";
        static final int TRANSACTION_generateCipherFromKey = 4;
        static final int TRANSACTION_generateTokenFromKey = 2;
        static final int TRANSACTION_getSecrecyKey = 3;
        static final int TRANSACTION_getSecrecyState = 1;
        static final int TRANSACTION_isInEncryptedAppList = 7;
        static final int TRANSACTION_isSecrecySupport = 6;
        static final int TRANSACTION_registerSecrecyServiceReceiver = 5;

        private static class Proxy implements ISecrecyService {
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

            public boolean getSecrecyState(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String generateTokenFromKey() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getSecrecyKey(byte[] key) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (key == null) {
                        _data.writeInt(-1);
                    } else {
                        _data.writeInt(key.length);
                    }
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.readByteArray(key);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] generateCipherFromKey(int cipherLength) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(cipherLength);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerSecrecyServiceReceiver(ISecrecyServiceReceiver receiver) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (receiver != null) {
                        iBinder = receiver.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isSecrecySupport() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isInEncryptedAppList(ActivityInfo info, String callingPackage, int callingUid, int callingPid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callingPackage);
                    _data.writeInt(callingUid);
                    _data.writeInt(callingPid);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISecrecyService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISecrecyService)) {
                return new Proxy(obj);
            }
            return (ISecrecyService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _result;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getSecrecyState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    String _result2 = generateTokenFromKey();
                    reply.writeNoException();
                    reply.writeString(_result2);
                    return true;
                case 3:
                    byte[] _arg0;
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0_length = data.readInt();
                    if (_arg0_length < 0) {
                        _arg0 = null;
                    } else {
                        _arg0 = new byte[_arg0_length];
                    }
                    _result = getSecrecyKey(_arg0);
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    reply.writeByteArray(_arg0);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    byte[] _result3 = generateCipherFromKey(data.readInt());
                    reply.writeNoException();
                    reply.writeByteArray(_result3);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    _result = registerSecrecyServiceReceiver(android.secrecy.ISecrecyServiceReceiver.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isSecrecySupport();
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 7:
                    ActivityInfo _arg02;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg02 = (ActivityInfo) ActivityInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg02 = null;
                    }
                    _result = isInEncryptedAppList(_arg02, data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    byte[] generateCipherFromKey(int i) throws RemoteException;

    String generateTokenFromKey() throws RemoteException;

    boolean getSecrecyKey(byte[] bArr) throws RemoteException;

    boolean getSecrecyState(int i) throws RemoteException;

    boolean isInEncryptedAppList(ActivityInfo activityInfo, String str, int i, int i2) throws RemoteException;

    boolean isSecrecySupport() throws RemoteException;

    boolean registerSecrecyServiceReceiver(ISecrecyServiceReceiver iSecrecyServiceReceiver) throws RemoteException;
}
