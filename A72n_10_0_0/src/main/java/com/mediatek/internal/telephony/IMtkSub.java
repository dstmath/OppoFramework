package com.mediatek.internal.telephony;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IMtkSub extends IInterface {
    int getSubIdUsingPhoneId(int i) throws RemoteException;

    MtkSubscriptionInfo getSubInfo(String str, int i) throws RemoteException;

    MtkSubscriptionInfo getSubInfoForIccId(String str, String str2) throws RemoteException;

    void setDefaultDataSubIdWithoutCapabilitySwitch(int i) throws RemoteException;

    void setDefaultFallbackSubId(int i, int i2) throws RemoteException;

    public static class Default implements IMtkSub {
        @Override // com.mediatek.internal.telephony.IMtkSub
        public MtkSubscriptionInfo getSubInfo(String callingPackage, int subId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkSub
        public MtkSubscriptionInfo getSubInfoForIccId(String callingPackage, String iccId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkSub
        public int getSubIdUsingPhoneId(int phoneId) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.IMtkSub
        public void setDefaultFallbackSubId(int subId, int subscriptionType) throws RemoteException {
        }

        @Override // com.mediatek.internal.telephony.IMtkSub
        public void setDefaultDataSubIdWithoutCapabilitySwitch(int subId) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMtkSub {
        private static final String DESCRIPTOR = "com.mediatek.internal.telephony.IMtkSub";
        static final int TRANSACTION_getSubIdUsingPhoneId = 3;
        static final int TRANSACTION_getSubInfo = 1;
        static final int TRANSACTION_getSubInfoForIccId = 2;
        static final int TRANSACTION_setDefaultDataSubIdWithoutCapabilitySwitch = 5;
        static final int TRANSACTION_setDefaultFallbackSubId = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMtkSub asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMtkSub)) {
                return new Proxy(obj);
            }
            return (IMtkSub) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                MtkSubscriptionInfo _result = getSubInfo(data.readString(), data.readInt());
                reply.writeNoException();
                if (_result != null) {
                    reply.writeInt(1);
                    _result.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                MtkSubscriptionInfo _result2 = getSubInfoForIccId(data.readString(), data.readString());
                reply.writeNoException();
                if (_result2 != null) {
                    reply.writeInt(1);
                    _result2.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                int _result3 = getSubIdUsingPhoneId(data.readInt());
                reply.writeNoException();
                reply.writeInt(_result3);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                setDefaultFallbackSubId(data.readInt(), data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                setDefaultDataSubIdWithoutCapabilitySwitch(data.readInt());
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IMtkSub {
            public static IMtkSub sDefaultImpl;
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

            @Override // com.mediatek.internal.telephony.IMtkSub
            public MtkSubscriptionInfo getSubInfo(String callingPackage, int subId) throws RemoteException {
                MtkSubscriptionInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSubInfo(callingPackage, subId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = MtkSubscriptionInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkSub
            public MtkSubscriptionInfo getSubInfoForIccId(String callingPackage, String iccId) throws RemoteException {
                MtkSubscriptionInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPackage);
                    _data.writeString(iccId);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSubInfoForIccId(callingPackage, iccId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = MtkSubscriptionInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkSub
            public int getSubIdUsingPhoneId(int phoneId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSubIdUsingPhoneId(phoneId);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkSub
            public void setDefaultFallbackSubId(int subId, int subscriptionType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(subscriptionType);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDefaultFallbackSubId(subId, subscriptionType);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkSub
            public void setDefaultDataSubIdWithoutCapabilitySwitch(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDefaultDataSubIdWithoutCapabilitySwitch(subId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IMtkSub impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMtkSub getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
