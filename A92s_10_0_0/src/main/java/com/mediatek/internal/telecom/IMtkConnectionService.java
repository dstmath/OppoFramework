package com.mediatek.internal.telecom;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telecom.ConnectionRequest;
import android.telecom.Logging.Session;
import android.telecom.PhoneAccountHandle;
import com.mediatek.internal.telecom.IMtkConnectionServiceAdapter;
import java.util.List;

public interface IMtkConnectionService extends IInterface {
    void addMtkConnectionServiceAdapter(IMtkConnectionServiceAdapter iMtkConnectionServiceAdapter) throws RemoteException;

    void blindAssuredEct(String str, String str2, int i) throws RemoteException;

    void clearMtkConnectionServiceAdapter() throws RemoteException;

    void createConference(PhoneAccountHandle phoneAccountHandle, String str, ConnectionRequest connectionRequest, List<String> list, boolean z, Session.Info info) throws RemoteException;

    void explicitCallTransfer(String str) throws RemoteException;

    IBinder getBinder() throws RemoteException;

    void handleOrderedOperation(String str, String str2, String str3) throws RemoteException;

    void hangupAll(String str) throws RemoteException;

    void inviteConferenceParticipants(String str, List<String> list) throws RemoteException;

    void rejectWithCause(String str, int i) throws RemoteException;

    public static class Default implements IMtkConnectionService {
        @Override // com.mediatek.internal.telecom.IMtkConnectionService
        public IBinder getBinder() throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telecom.IMtkConnectionService
        public void addMtkConnectionServiceAdapter(IMtkConnectionServiceAdapter adapter) throws RemoteException {
        }

        @Override // com.mediatek.internal.telecom.IMtkConnectionService
        public void clearMtkConnectionServiceAdapter() throws RemoteException {
        }

        @Override // com.mediatek.internal.telecom.IMtkConnectionService
        public void hangupAll(String callId) throws RemoteException {
        }

        @Override // com.mediatek.internal.telecom.IMtkConnectionService
        public void handleOrderedOperation(String callId, String currentOperation, String pendingOperation) throws RemoteException {
        }

        @Override // com.mediatek.internal.telecom.IMtkConnectionService
        public void explicitCallTransfer(String callId) throws RemoteException {
        }

        @Override // com.mediatek.internal.telecom.IMtkConnectionService
        public void blindAssuredEct(String callId, String number, int type) throws RemoteException {
        }

        @Override // com.mediatek.internal.telecom.IMtkConnectionService
        public void inviteConferenceParticipants(String conferenceCallId, List<String> list) throws RemoteException {
        }

        @Override // com.mediatek.internal.telecom.IMtkConnectionService
        public void createConference(PhoneAccountHandle connectionManagerPhoneAccount, String conferenceCallId, ConnectionRequest request, List<String> list, boolean isIncoming, Session.Info sessionInfo) throws RemoteException {
        }

        @Override // com.mediatek.internal.telecom.IMtkConnectionService
        public void rejectWithCause(String callId, int cause) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMtkConnectionService {
        private static final String DESCRIPTOR = "com.mediatek.internal.telecom.IMtkConnectionService";
        static final int TRANSACTION_addMtkConnectionServiceAdapter = 2;
        static final int TRANSACTION_blindAssuredEct = 7;
        static final int TRANSACTION_clearMtkConnectionServiceAdapter = 3;
        static final int TRANSACTION_createConference = 9;
        static final int TRANSACTION_explicitCallTransfer = 6;
        static final int TRANSACTION_getBinder = 1;
        static final int TRANSACTION_handleOrderedOperation = 5;
        static final int TRANSACTION_hangupAll = 4;
        static final int TRANSACTION_inviteConferenceParticipants = 8;
        static final int TRANSACTION_rejectWithCause = 10;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMtkConnectionService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMtkConnectionService)) {
                return new Proxy(obj);
            }
            return (IMtkConnectionService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            PhoneAccountHandle _arg0;
            ConnectionRequest _arg2;
            Session.Info _arg5;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _result = getBinder();
                        reply.writeNoException();
                        reply.writeStrongBinder(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        addMtkConnectionServiceAdapter(IMtkConnectionServiceAdapter.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case TRANSACTION_clearMtkConnectionServiceAdapter /*{ENCODED_INT: 3}*/:
                        data.enforceInterface(DESCRIPTOR);
                        clearMtkConnectionServiceAdapter();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        hangupAll(data.readString());
                        return true;
                    case TRANSACTION_handleOrderedOperation /*{ENCODED_INT: 5}*/:
                        data.enforceInterface(DESCRIPTOR);
                        handleOrderedOperation(data.readString(), data.readString(), data.readString());
                        return true;
                    case TRANSACTION_explicitCallTransfer /*{ENCODED_INT: 6}*/:
                        data.enforceInterface(DESCRIPTOR);
                        explicitCallTransfer(data.readString());
                        return true;
                    case TRANSACTION_blindAssuredEct /*{ENCODED_INT: 7}*/:
                        data.enforceInterface(DESCRIPTOR);
                        blindAssuredEct(data.readString(), data.readString(), data.readInt());
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        inviteConferenceParticipants(data.readString(), data.createStringArrayList());
                        return true;
                    case TRANSACTION_createConference /*{ENCODED_INT: 9}*/:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        String _arg1 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = (ConnectionRequest) ConnectionRequest.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        List<String> _arg3 = data.createStringArrayList();
                        boolean _arg4 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg5 = (Session.Info) Session.Info.CREATOR.createFromParcel(data);
                        } else {
                            _arg5 = null;
                        }
                        createConference(_arg0, _arg1, _arg2, _arg3, _arg4, _arg5);
                        return true;
                    case TRANSACTION_rejectWithCause /*{ENCODED_INT: 10}*/:
                        data.enforceInterface(DESCRIPTOR);
                        rejectWithCause(data.readString(), data.readInt());
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

        private static class Proxy implements IMtkConnectionService {
            public static IMtkConnectionService sDefaultImpl;
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

            @Override // com.mediatek.internal.telecom.IMtkConnectionService
            public IBinder getBinder() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getBinder();
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telecom.IMtkConnectionService
            public void addMtkConnectionServiceAdapter(IMtkConnectionServiceAdapter adapter) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(adapter != null ? adapter.asBinder() : null);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().addMtkConnectionServiceAdapter(adapter);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telecom.IMtkConnectionService
            public void clearMtkConnectionServiceAdapter() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(Stub.TRANSACTION_clearMtkConnectionServiceAdapter, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().clearMtkConnectionServiceAdapter();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telecom.IMtkConnectionService
            public void hangupAll(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().hangupAll(callId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telecom.IMtkConnectionService
            public void handleOrderedOperation(String callId, String currentOperation, String pendingOperation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeString(currentOperation);
                    _data.writeString(pendingOperation);
                    if (this.mRemote.transact(Stub.TRANSACTION_handleOrderedOperation, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().handleOrderedOperation(callId, currentOperation, pendingOperation);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telecom.IMtkConnectionService
            public void explicitCallTransfer(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (this.mRemote.transact(Stub.TRANSACTION_explicitCallTransfer, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().explicitCallTransfer(callId);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telecom.IMtkConnectionService
            public void blindAssuredEct(String callId, String number, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeString(number);
                    _data.writeInt(type);
                    if (this.mRemote.transact(Stub.TRANSACTION_blindAssuredEct, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().blindAssuredEct(callId, number, type);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telecom.IMtkConnectionService
            public void inviteConferenceParticipants(String conferenceCallId, List<String> numbers) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(conferenceCallId);
                    _data.writeStringList(numbers);
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().inviteConferenceParticipants(conferenceCallId, numbers);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telecom.IMtkConnectionService
            public void createConference(PhoneAccountHandle connectionManagerPhoneAccount, String conferenceCallId, ConnectionRequest request, List<String> numbers, boolean isIncoming, Session.Info sessionInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (connectionManagerPhoneAccount != null) {
                        _data.writeInt(1);
                        connectionManagerPhoneAccount.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    try {
                        _data.writeString(conferenceCallId);
                        if (request != null) {
                            _data.writeInt(1);
                            request.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                    } catch (Throwable th) {
                        th = th;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeStringList(numbers);
                        _data.writeInt(isIncoming ? 1 : 0);
                        if (sessionInfo != null) {
                            _data.writeInt(1);
                            sessionInfo.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        if (this.mRemote.transact(Stub.TRANSACTION_createConference, _data, null, 1) || Stub.getDefaultImpl() == null) {
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().createConference(connectionManagerPhoneAccount, conferenceCallId, request, numbers, isIncoming, sessionInfo);
                        _data.recycle();
                    } catch (Throwable th3) {
                        th = th3;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.mediatek.internal.telecom.IMtkConnectionService
            public void rejectWithCause(String callId, int cause) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(cause);
                    if (this.mRemote.transact(Stub.TRANSACTION_rejectWithCause, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().rejectWithCause(callId, cause);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IMtkConnectionService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMtkConnectionService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
