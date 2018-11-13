package org.codeaurora.btmultisim;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IBluetoothDsdaService extends IInterface {

    public static abstract class Stub extends Binder implements IBluetoothDsdaService {
        private static final String DESCRIPTOR = "org.codeaurora.btmultisim.IBluetoothDsdaService";
        static final int TRANSACTION_answerOnThisSubAllowed = 11;
        static final int TRANSACTION_canDoCallSwap = 8;
        static final int TRANSACTION_getTotalCallsOnSub = 5;
        static final int TRANSACTION_handleMultiSimPreciseCallStateChange = 3;
        static final int TRANSACTION_hasCallsOnBothSubs = 9;
        static final int TRANSACTION_isFakeMultiPartyCall = 10;
        static final int TRANSACTION_isSwitchSubAllowed = 6;
        static final int TRANSACTION_phoneSubChanged = 2;
        static final int TRANSACTION_processQueryPhoneState = 4;
        static final int TRANSACTION_setCurrentSub = 1;
        static final int TRANSACTION_switchSub = 7;

        private static class Proxy implements IBluetoothDsdaService {
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

            public void setCurrentSub(int sub) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sub);
                    this.mRemote.transact(Stub.TRANSACTION_setCurrentSub, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void phoneSubChanged() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_phoneSubChanged, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void handleMultiSimPreciseCallStateChange(int ForegroundCallState, int RingingCallState, String RingingNumber, int NumberType, int BackgroundCallState, int numHeldCallsonSub) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(ForegroundCallState);
                    _data.writeInt(RingingCallState);
                    _data.writeString(RingingNumber);
                    _data.writeInt(NumberType);
                    _data.writeInt(BackgroundCallState);
                    _data.writeInt(numHeldCallsonSub);
                    this.mRemote.transact(Stub.TRANSACTION_handleMultiSimPreciseCallStateChange, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void processQueryPhoneState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_processQueryPhoneState, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getTotalCallsOnSub(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_getTotalCallsOnSub, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isSwitchSubAllowed() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isSwitchSubAllowed, _data, _reply, 0);
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

            public void switchSub() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_switchSub, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean canDoCallSwap() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_canDoCallSwap, _data, _reply, 0);
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

            public boolean hasCallsOnBothSubs() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_hasCallsOnBothSubs, _data, _reply, 0);
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

            public boolean isFakeMultiPartyCall() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isFakeMultiPartyCall, _data, _reply, 0);
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

            public boolean answerOnThisSubAllowed() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_answerOnThisSubAllowed, _data, _reply, 0);
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

        public static IBluetoothDsdaService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IBluetoothDsdaService)) {
                return new Proxy(obj);
            }
            return (IBluetoothDsdaService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = 0;
            boolean _result;
            switch (code) {
                case TRANSACTION_setCurrentSub /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    setCurrentSub(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_phoneSubChanged /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    phoneSubChanged();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_handleMultiSimPreciseCallStateChange /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    handleMultiSimPreciseCallStateChange(data.readInt(), data.readInt(), data.readString(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_processQueryPhoneState /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    processQueryPhoneState();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getTotalCallsOnSub /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    int _result2 = getTotalCallsOnSub(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_isSwitchSubAllowed /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isSwitchSubAllowed();
                    reply.writeNoException();
                    if (_result) {
                        i = TRANSACTION_setCurrentSub;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_switchSub /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    switchSub();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_canDoCallSwap /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = canDoCallSwap();
                    reply.writeNoException();
                    if (_result) {
                        i = TRANSACTION_setCurrentSub;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_hasCallsOnBothSubs /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = hasCallsOnBothSubs();
                    reply.writeNoException();
                    if (_result) {
                        i = TRANSACTION_setCurrentSub;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_isFakeMultiPartyCall /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isFakeMultiPartyCall();
                    reply.writeNoException();
                    if (_result) {
                        i = TRANSACTION_setCurrentSub;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_answerOnThisSubAllowed /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = answerOnThisSubAllowed();
                    reply.writeNoException();
                    if (_result) {
                        i = TRANSACTION_setCurrentSub;
                    }
                    reply.writeInt(i);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean answerOnThisSubAllowed() throws RemoteException;

    boolean canDoCallSwap() throws RemoteException;

    int getTotalCallsOnSub(int i) throws RemoteException;

    void handleMultiSimPreciseCallStateChange(int i, int i2, String str, int i3, int i4, int i5) throws RemoteException;

    boolean hasCallsOnBothSubs() throws RemoteException;

    boolean isFakeMultiPartyCall() throws RemoteException;

    boolean isSwitchSubAllowed() throws RemoteException;

    void phoneSubChanged() throws RemoteException;

    void processQueryPhoneState() throws RemoteException;

    void setCurrentSub(int i) throws RemoteException;

    void switchSub() throws RemoteException;
}
