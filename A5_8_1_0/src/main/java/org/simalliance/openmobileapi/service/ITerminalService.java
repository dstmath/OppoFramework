package org.simalliance.openmobileapi.service;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ITerminalService extends IInterface {

    public static abstract class Stub extends Binder implements ITerminalService {
        private static final String DESCRIPTOR = "org.simalliance.openmobileapi.service.ITerminalService";
        static final int TRANSACTION_getAtr = 4;
        static final int TRANSACTION_getSeStateChangedAction = 7;
        static final int TRANSACTION_internalCloseLogicalChannel = 2;
        static final int TRANSACTION_internalOpenLogicalChannel = 1;
        static final int TRANSACTION_internalTransmit = 3;
        static final int TRANSACTION_isCardPresent = 5;
        static final int TRANSACTION_simIOExchange = 6;

        private static class Proxy implements ITerminalService {
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

            public OpenLogicalChannelResponse internalOpenLogicalChannel(byte[] aid, byte p2, SmartcardError error) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    OpenLogicalChannelResponse _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(aid);
                    _data.writeByte(p2);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (OpenLogicalChannelResponse) OpenLogicalChannelResponse.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    if (_reply.readInt() != 0) {
                        error.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void internalCloseLogicalChannel(int channelNumber, SmartcardError error) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(channelNumber);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        error.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] internalTransmit(byte[] command, SmartcardError error) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(command);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    if (_reply.readInt() != 0) {
                        error.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] getAtr() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isCardPresent() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
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

            public byte[] simIOExchange(int fileID, String filePath, byte[] cmd, SmartcardError error) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(fileID);
                    _data.writeString(filePath);
                    _data.writeByteArray(cmd);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    if (_reply.readInt() != 0) {
                        error.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getSeStateChangedAction() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
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

        public static ITerminalService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITerminalService)) {
                return new Proxy(obj);
            }
            return (ITerminalService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            byte[] _arg0;
            int _arg02;
            SmartcardError _arg1;
            byte[] _result;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.createByteArray();
                    byte _arg12 = data.readByte();
                    SmartcardError _arg2 = new SmartcardError();
                    OpenLogicalChannelResponse _result2 = internalOpenLogicalChannel(_arg0, _arg12, _arg2);
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(1);
                        _result2.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    if (_arg2 != null) {
                        reply.writeInt(1);
                        _arg2.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = data.readInt();
                    _arg1 = new SmartcardError();
                    internalCloseLogicalChannel(_arg02, _arg1);
                    reply.writeNoException();
                    if (_arg1 != null) {
                        reply.writeInt(1);
                        _arg1.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.createByteArray();
                    _arg1 = new SmartcardError();
                    _result = internalTransmit(_arg0, _arg1);
                    reply.writeNoException();
                    reply.writeByteArray(_result);
                    if (_arg1 != null) {
                        reply.writeInt(1);
                        _arg1.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getAtr();
                    reply.writeNoException();
                    reply.writeByteArray(_result);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result3 = isCardPresent();
                    reply.writeNoException();
                    reply.writeInt(_result3 ? 1 : 0);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = data.readInt();
                    String _arg13 = data.readString();
                    byte[] _arg22 = data.createByteArray();
                    SmartcardError _arg3 = new SmartcardError();
                    _result = simIOExchange(_arg02, _arg13, _arg22, _arg3);
                    reply.writeNoException();
                    reply.writeByteArray(_result);
                    if (_arg3 != null) {
                        reply.writeInt(1);
                        _arg3.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    String _result4 = getSeStateChangedAction();
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    byte[] getAtr() throws RemoteException;

    String getSeStateChangedAction() throws RemoteException;

    void internalCloseLogicalChannel(int i, SmartcardError smartcardError) throws RemoteException;

    OpenLogicalChannelResponse internalOpenLogicalChannel(byte[] bArr, byte b, SmartcardError smartcardError) throws RemoteException;

    byte[] internalTransmit(byte[] bArr, SmartcardError smartcardError) throws RemoteException;

    boolean isCardPresent() throws RemoteException;

    byte[] simIOExchange(int i, String str, byte[] bArr, SmartcardError smartcardError) throws RemoteException;
}
