package com.android.internal.telecom;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telecom.CallAudioState;
import android.telecom.ConnectionRequest;
import android.telecom.PhoneAccountHandle;
import java.util.List;

public interface IConnectionService extends IInterface {

    public static abstract class Stub extends Binder implements IConnectionService {
        private static final String DESCRIPTOR = "com.android.internal.telecom.IConnectionService";
        static final int TRANSACTION_abort = 4;
        static final int TRANSACTION_addConnectionServiceAdapter = 1;
        static final int TRANSACTION_answer = 6;
        static final int TRANSACTION_answerVideo = 5;
        static final int TRANSACTION_blindAssuredEct = 26;
        static final int TRANSACTION_conference = 16;
        static final int TRANSACTION_createConference = 28;
        static final int TRANSACTION_createConnection = 3;
        static final int TRANSACTION_disconnect = 9;
        static final int TRANSACTION_explicitCallTransfer = 25;
        static final int TRANSACTION_handleOrderedOperation = 29;
        static final int TRANSACTION_hangupAll = 24;
        static final int TRANSACTION_hold = 11;
        static final int TRANSACTION_inviteConferenceParticipants = 27;
        static final int TRANSACTION_mergeConference = 18;
        static final int TRANSACTION_onCallAudioStateChanged = 13;
        static final int TRANSACTION_onExtrasChanged = 23;
        static final int TRANSACTION_onPostDialContinue = 20;
        static final int TRANSACTION_playDtmfTone = 14;
        static final int TRANSACTION_pullExternalCall = 21;
        static final int TRANSACTION_reject = 7;
        static final int TRANSACTION_rejectWithMessage = 8;
        static final int TRANSACTION_removeConnectionServiceAdapter = 2;
        static final int TRANSACTION_sendCallEvent = 22;
        static final int TRANSACTION_silence = 10;
        static final int TRANSACTION_splitFromConference = 17;
        static final int TRANSACTION_stopDtmfTone = 15;
        static final int TRANSACTION_swapConference = 19;
        static final int TRANSACTION_unhold = 12;

        private static class Proxy implements IConnectionService {
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

            public void addConnectionServiceAdapter(IConnectionServiceAdapter adapter) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (adapter != null) {
                        iBinder = adapter.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void removeConnectionServiceAdapter(IConnectionServiceAdapter adapter) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (adapter != null) {
                        iBinder = adapter.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void createConnection(PhoneAccountHandle connectionManagerPhoneAccount, String callId, ConnectionRequest request, boolean isIncoming, boolean isUnknown) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    int i2;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (connectionManagerPhoneAccount != null) {
                        _data.writeInt(1);
                        connectionManagerPhoneAccount.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(callId);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (isIncoming) {
                        i2 = 1;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    if (!isUnknown) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void abort(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void answerVideo(String callId, int videoState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(videoState);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void answer(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void reject(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void rejectWithMessage(String callId, String message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeString(message);
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void disconnect(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void silence(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void hold(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(11, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void unhold(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(12, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onCallAudioStateChanged(String activeCallId, CallAudioState callAudioState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(activeCallId);
                    if (callAudioState != null) {
                        _data.writeInt(1);
                        callAudioState.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(13, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void playDtmfTone(String callId, char digit) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeInt(digit);
                    this.mRemote.transact(14, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void stopDtmfTone(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(15, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void conference(String conferenceCallId, String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(conferenceCallId);
                    _data.writeString(callId);
                    this.mRemote.transact(16, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void splitFromConference(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(17, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void mergeConference(String conferenceCallId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(conferenceCallId);
                    this.mRemote.transact(18, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void swapConference(String conferenceCallId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(conferenceCallId);
                    this.mRemote.transact(19, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onPostDialContinue(String callId, boolean proceed) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (!proceed) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(20, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void pullExternalCall(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(21, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void sendCallEvent(String callId, String event, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeString(event);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(22, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onExtrasChanged(String callId, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(23, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void hangupAll(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(24, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void explicitCallTransfer(String callId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    this.mRemote.transact(25, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void blindAssuredEct(String callId, String number, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeString(number);
                    _data.writeInt(type);
                    this.mRemote.transact(26, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void inviteConferenceParticipants(String conferenceCallId, List<String> numbers) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(conferenceCallId);
                    _data.writeStringList(numbers);
                    this.mRemote.transact(27, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void createConference(PhoneAccountHandle connectionManagerPhoneAccount, String conferenceCallId, ConnectionRequest request, List<String> numbers, boolean isIncoming) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (connectionManagerPhoneAccount != null) {
                        _data.writeInt(1);
                        connectionManagerPhoneAccount.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(conferenceCallId);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringList(numbers);
                    if (!isIncoming) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(28, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void handleOrderedOperation(String callId, String currentOperation, String pendingOperation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callId);
                    _data.writeString(currentOperation);
                    _data.writeString(pendingOperation);
                    this.mRemote.transact(29, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IConnectionService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IConnectionService)) {
                return new Proxy(obj);
            }
            return (IConnectionService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            PhoneAccountHandle _arg0;
            String _arg1;
            ConnectionRequest _arg2;
            String _arg02;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    addConnectionServiceAdapter(com.android.internal.telecom.IConnectionServiceAdapter.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    removeConnectionServiceAdapter(com.android.internal.telecom.IConnectionServiceAdapter.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        _arg2 = (ConnectionRequest) ConnectionRequest.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    createConnection(_arg0, _arg1, _arg2, data.readInt() != 0, data.readInt() != 0);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    abort(data.readString());
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    answerVideo(data.readString(), data.readInt());
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    answer(data.readString());
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    reject(data.readString());
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    rejectWithMessage(data.readString(), data.readString());
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    disconnect(data.readString());
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    silence(data.readString());
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    hold(data.readString());
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    unhold(data.readString());
                    return true;
                case 13:
                    CallAudioState _arg12;
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = data.readString();
                    if (data.readInt() != 0) {
                        _arg12 = (CallAudioState) CallAudioState.CREATOR.createFromParcel(data);
                    } else {
                        _arg12 = null;
                    }
                    onCallAudioStateChanged(_arg02, _arg12);
                    return true;
                case 14:
                    data.enforceInterface(DESCRIPTOR);
                    playDtmfTone(data.readString(), (char) data.readInt());
                    return true;
                case 15:
                    data.enforceInterface(DESCRIPTOR);
                    stopDtmfTone(data.readString());
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    conference(data.readString(), data.readString());
                    return true;
                case 17:
                    data.enforceInterface(DESCRIPTOR);
                    splitFromConference(data.readString());
                    return true;
                case 18:
                    data.enforceInterface(DESCRIPTOR);
                    mergeConference(data.readString());
                    return true;
                case 19:
                    data.enforceInterface(DESCRIPTOR);
                    swapConference(data.readString());
                    return true;
                case 20:
                    data.enforceInterface(DESCRIPTOR);
                    onPostDialContinue(data.readString(), data.readInt() != 0);
                    return true;
                case 21:
                    data.enforceInterface(DESCRIPTOR);
                    pullExternalCall(data.readString());
                    return true;
                case 22:
                    Bundle _arg22;
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = data.readString();
                    _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg22 = null;
                    }
                    sendCallEvent(_arg02, _arg1, _arg22);
                    return true;
                case 23:
                    Bundle _arg13;
                    data.enforceInterface(DESCRIPTOR);
                    _arg02 = data.readString();
                    if (data.readInt() != 0) {
                        _arg13 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg13 = null;
                    }
                    onExtrasChanged(_arg02, _arg13);
                    return true;
                case 24:
                    data.enforceInterface(DESCRIPTOR);
                    hangupAll(data.readString());
                    return true;
                case 25:
                    data.enforceInterface(DESCRIPTOR);
                    explicitCallTransfer(data.readString());
                    return true;
                case 26:
                    data.enforceInterface(DESCRIPTOR);
                    blindAssuredEct(data.readString(), data.readString(), data.readInt());
                    return true;
                case 27:
                    data.enforceInterface(DESCRIPTOR);
                    inviteConferenceParticipants(data.readString(), data.createStringArrayList());
                    return true;
                case 28:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (PhoneAccountHandle) PhoneAccountHandle.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        _arg2 = (ConnectionRequest) ConnectionRequest.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    createConference(_arg0, _arg1, _arg2, data.createStringArrayList(), data.readInt() != 0);
                    return true;
                case 29:
                    data.enforceInterface(DESCRIPTOR);
                    handleOrderedOperation(data.readString(), data.readString(), data.readString());
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void abort(String str) throws RemoteException;

    void addConnectionServiceAdapter(IConnectionServiceAdapter iConnectionServiceAdapter) throws RemoteException;

    void answer(String str) throws RemoteException;

    void answerVideo(String str, int i) throws RemoteException;

    void blindAssuredEct(String str, String str2, int i) throws RemoteException;

    void conference(String str, String str2) throws RemoteException;

    void createConference(PhoneAccountHandle phoneAccountHandle, String str, ConnectionRequest connectionRequest, List<String> list, boolean z) throws RemoteException;

    void createConnection(PhoneAccountHandle phoneAccountHandle, String str, ConnectionRequest connectionRequest, boolean z, boolean z2) throws RemoteException;

    void disconnect(String str) throws RemoteException;

    void explicitCallTransfer(String str) throws RemoteException;

    void handleOrderedOperation(String str, String str2, String str3) throws RemoteException;

    void hangupAll(String str) throws RemoteException;

    void hold(String str) throws RemoteException;

    void inviteConferenceParticipants(String str, List<String> list) throws RemoteException;

    void mergeConference(String str) throws RemoteException;

    void onCallAudioStateChanged(String str, CallAudioState callAudioState) throws RemoteException;

    void onExtrasChanged(String str, Bundle bundle) throws RemoteException;

    void onPostDialContinue(String str, boolean z) throws RemoteException;

    void playDtmfTone(String str, char c) throws RemoteException;

    void pullExternalCall(String str) throws RemoteException;

    void reject(String str) throws RemoteException;

    void rejectWithMessage(String str, String str2) throws RemoteException;

    void removeConnectionServiceAdapter(IConnectionServiceAdapter iConnectionServiceAdapter) throws RemoteException;

    void sendCallEvent(String str, String str2, Bundle bundle) throws RemoteException;

    void silence(String str) throws RemoteException;

    void splitFromConference(String str) throws RemoteException;

    void stopDtmfTone(String str) throws RemoteException;

    void swapConference(String str) throws RemoteException;

    void unhold(String str) throws RemoteException;
}
