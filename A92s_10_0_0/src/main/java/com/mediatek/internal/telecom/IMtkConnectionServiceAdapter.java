package com.mediatek.internal.telecom;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.telecom.ConnectionRequest;
import android.telecom.DisconnectCause;
import android.telecom.ParcelableConference;

public interface IMtkConnectionServiceAdapter extends IInterface {
    void handleCreateConferenceComplete(String str, ConnectionRequest connectionRequest, ParcelableConference parcelableConference, DisconnectCause disconnectCause) throws RemoteException;

    public static class Default implements IMtkConnectionServiceAdapter {
        @Override // com.mediatek.internal.telecom.IMtkConnectionServiceAdapter
        public void handleCreateConferenceComplete(String conferenceId, ConnectionRequest request, ParcelableConference conference, DisconnectCause disconnectCause) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMtkConnectionServiceAdapter {
        private static final String DESCRIPTOR = "com.mediatek.internal.telecom.IMtkConnectionServiceAdapter";
        static final int TRANSACTION_handleCreateConferenceComplete = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMtkConnectionServiceAdapter asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMtkConnectionServiceAdapter)) {
                return new Proxy(obj);
            }
            return (IMtkConnectionServiceAdapter) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ConnectionRequest _arg1;
            ParcelableConference _arg2;
            DisconnectCause _arg3;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                String _arg0 = data.readString();
                if (data.readInt() != 0) {
                    _arg1 = (ConnectionRequest) ConnectionRequest.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                if (data.readInt() != 0) {
                    _arg2 = (ParcelableConference) ParcelableConference.CREATOR.createFromParcel(data);
                } else {
                    _arg2 = null;
                }
                if (data.readInt() != 0) {
                    _arg3 = (DisconnectCause) DisconnectCause.CREATOR.createFromParcel(data);
                } else {
                    _arg3 = null;
                }
                handleCreateConferenceComplete(_arg0, _arg1, _arg2, _arg3);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IMtkConnectionServiceAdapter {
            public static IMtkConnectionServiceAdapter sDefaultImpl;
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

            @Override // com.mediatek.internal.telecom.IMtkConnectionServiceAdapter
            public void handleCreateConferenceComplete(String conferenceId, ConnectionRequest request, ParcelableConference conference, DisconnectCause disconnectCause) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(conferenceId);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (conference != null) {
                        _data.writeInt(1);
                        conference.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (disconnectCause != null) {
                        _data.writeInt(1);
                        disconnectCause.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().handleCreateConferenceComplete(conferenceId, request, conference, disconnectCause);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IMtkConnectionServiceAdapter impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMtkConnectionServiceAdapter getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
