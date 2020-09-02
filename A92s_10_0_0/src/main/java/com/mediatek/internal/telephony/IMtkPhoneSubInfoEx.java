package com.mediatek.internal.telephony;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;

public interface IMtkPhoneSubInfoEx extends IInterface {
    String getIsimDomainForSubscriber(int i) throws RemoteException;

    String getIsimGbabp() throws RemoteException;

    String getIsimGbabpForSubscriber(int i) throws RemoteException;

    String getIsimImpiForSubscriber(int i) throws RemoteException;

    String[] getIsimImpuForSubscriber(int i) throws RemoteException;

    String getIsimIstForSubscriber(int i) throws RemoteException;

    String[] getIsimPcscfForSubscriber(int i) throws RemoteException;

    byte[] getIsimPsismsc() throws RemoteException;

    byte[] getIsimPsismscForSubscriber(int i) throws RemoteException;

    String getLine1PhoneNumberForSubscriber(int i, String str) throws RemoteException;

    int getMncLength() throws RemoteException;

    int getMncLengthForSubscriber(int i) throws RemoteException;

    String getUsimGbabp() throws RemoteException;

    String getUsimGbabpForSubscriber(int i) throws RemoteException;

    byte[] getUsimPsismsc() throws RemoteException;

    byte[] getUsimPsismscForSubscriber(int i) throws RemoteException;

    boolean getUsimService(int i, String str) throws RemoteException;

    boolean getUsimServiceForSubscriber(int i, int i2, String str) throws RemoteException;

    byte[] getUsimSmsp() throws RemoteException;

    byte[] getUsimSmspForSubscriber(int i) throws RemoteException;

    void setIsimGbabp(String str, Message message) throws RemoteException;

    void setIsimGbabpForSubscriber(int i, String str, Message message) throws RemoteException;

    void setUsimGbabp(String str, Message message) throws RemoteException;

    void setUsimGbabpForSubscriber(int i, String str, Message message) throws RemoteException;

    public static class Default implements IMtkPhoneSubInfoEx {
        @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
        public boolean getUsimService(int service, String callingPackage) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
        public String getUsimGbabp() throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
        public String getUsimGbabpForSubscriber(int subId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
        public void setUsimGbabp(String gbabp, Message onComplete) throws RemoteException {
        }

        @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
        public void setUsimGbabpForSubscriber(int subId, String gbabp, Message onComplete) throws RemoteException {
        }

        @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
        public String getIsimGbabp() throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
        public String getIsimGbabpForSubscriber(int subId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
        public void setIsimGbabp(String gbabp, Message onComplete) throws RemoteException {
        }

        @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
        public void setIsimGbabpForSubscriber(int subId, String gbabp, Message onComplete) throws RemoteException {
        }

        @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
        public boolean getUsimServiceForSubscriber(int subId, int service, String callingPackage) throws RemoteException {
            return false;
        }

        @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
        public byte[] getUsimPsismsc() throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
        public byte[] getUsimPsismscForSubscriber(int subId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
        public byte[] getUsimSmsp() throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
        public byte[] getUsimSmspForSubscriber(int subId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
        public int getMncLength() throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
        public int getMncLengthForSubscriber(int subId) throws RemoteException {
            return 0;
        }

        @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
        public String getIsimImpiForSubscriber(int subId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
        public String getIsimDomainForSubscriber(int subId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
        public String[] getIsimImpuForSubscriber(int subId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
        public String getIsimIstForSubscriber(int subId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
        public String[] getIsimPcscfForSubscriber(int subId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
        public byte[] getIsimPsismsc() throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
        public byte[] getIsimPsismscForSubscriber(int subId) throws RemoteException {
            return null;
        }

        @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
        public String getLine1PhoneNumberForSubscriber(int subId, String callingPackage) throws RemoteException {
            return null;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMtkPhoneSubInfoEx {
        private static final String DESCRIPTOR = "com.mediatek.internal.telephony.IMtkPhoneSubInfoEx";
        static final int TRANSACTION_getIsimDomainForSubscriber = 18;
        static final int TRANSACTION_getIsimGbabp = 6;
        static final int TRANSACTION_getIsimGbabpForSubscriber = 7;
        static final int TRANSACTION_getIsimImpiForSubscriber = 17;
        static final int TRANSACTION_getIsimImpuForSubscriber = 19;
        static final int TRANSACTION_getIsimIstForSubscriber = 20;
        static final int TRANSACTION_getIsimPcscfForSubscriber = 21;
        static final int TRANSACTION_getIsimPsismsc = 22;
        static final int TRANSACTION_getIsimPsismscForSubscriber = 23;
        static final int TRANSACTION_getLine1PhoneNumberForSubscriber = 24;
        static final int TRANSACTION_getMncLength = 15;
        static final int TRANSACTION_getMncLengthForSubscriber = 16;
        static final int TRANSACTION_getUsimGbabp = 2;
        static final int TRANSACTION_getUsimGbabpForSubscriber = 3;
        static final int TRANSACTION_getUsimPsismsc = 11;
        static final int TRANSACTION_getUsimPsismscForSubscriber = 12;
        static final int TRANSACTION_getUsimService = 1;
        static final int TRANSACTION_getUsimServiceForSubscriber = 10;
        static final int TRANSACTION_getUsimSmsp = 13;
        static final int TRANSACTION_getUsimSmspForSubscriber = 14;
        static final int TRANSACTION_setIsimGbabp = 8;
        static final int TRANSACTION_setIsimGbabpForSubscriber = 9;
        static final int TRANSACTION_setUsimGbabp = 4;
        static final int TRANSACTION_setUsimGbabpForSubscriber = 5;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMtkPhoneSubInfoEx asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMtkPhoneSubInfoEx)) {
                return new Proxy(obj);
            }
            return (IMtkPhoneSubInfoEx) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Message _arg1;
            Message _arg2;
            Message _arg12;
            Message _arg22;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        boolean usimService = getUsimService(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(usimService ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        String _result = getUsimGbabp();
                        reply.writeNoException();
                        reply.writeString(_result);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        String _result2 = getUsimGbabpForSubscriber(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result2);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = (Message) Message.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        setUsimGbabp(_arg0, _arg1);
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        String _arg13 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = (Message) Message.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        setUsimGbabpForSubscriber(_arg02, _arg13, _arg2);
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        String _result3 = getIsimGbabp();
                        reply.writeNoException();
                        reply.writeString(_result3);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        String _result4 = getIsimGbabpForSubscriber(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result4);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg03 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = (Message) Message.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        setIsimGbabp(_arg03, _arg12);
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        String _arg14 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = (Message) Message.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        setIsimGbabpForSubscriber(_arg04, _arg14, _arg22);
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        boolean usimServiceForSubscriber = getUsimServiceForSubscriber(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(usimServiceForSubscriber ? 1 : 0);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result5 = getUsimPsismsc();
                        reply.writeNoException();
                        reply.writeByteArray(_result5);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result6 = getUsimPsismscForSubscriber(data.readInt());
                        reply.writeNoException();
                        reply.writeByteArray(_result6);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result7 = getUsimSmsp();
                        reply.writeNoException();
                        reply.writeByteArray(_result7);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result8 = getUsimSmspForSubscriber(data.readInt());
                        reply.writeNoException();
                        reply.writeByteArray(_result8);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = getMncLength();
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        int _result10 = getMncLengthForSubscriber(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        String _result11 = getIsimImpiForSubscriber(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result11);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        String _result12 = getIsimDomainForSubscriber(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result12);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result13 = getIsimImpuForSubscriber(data.readInt());
                        reply.writeNoException();
                        reply.writeStringArray(_result13);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        String _result14 = getIsimIstForSubscriber(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result14);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result15 = getIsimPcscfForSubscriber(data.readInt());
                        reply.writeNoException();
                        reply.writeStringArray(_result15);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result16 = getIsimPsismsc();
                        reply.writeNoException();
                        reply.writeByteArray(_result16);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result17 = getIsimPsismscForSubscriber(data.readInt());
                        reply.writeNoException();
                        reply.writeByteArray(_result17);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        String _result18 = getLine1PhoneNumberForSubscriber(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeString(_result18);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IMtkPhoneSubInfoEx {
            public static IMtkPhoneSubInfoEx sDefaultImpl;
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

            @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
            public boolean getUsimService(int service, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(service);
                    _data.writeString(callingPackage);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUsimService(service, callingPackage);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
            public String getUsimGbabp() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUsimGbabp();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
            public String getUsimGbabpForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUsimGbabpForSubscriber(subId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
            public void setUsimGbabp(String gbabp, Message onComplete) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(gbabp);
                    if (onComplete != null) {
                        _data.writeInt(1);
                        onComplete.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setUsimGbabp(gbabp, onComplete);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
            public void setUsimGbabpForSubscriber(int subId, String gbabp, Message onComplete) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(gbabp);
                    if (onComplete != null) {
                        _data.writeInt(1);
                        onComplete.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setUsimGbabpForSubscriber(subId, gbabp, onComplete);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
            public String getIsimGbabp() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIsimGbabp();
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
            public String getIsimGbabpForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIsimGbabpForSubscriber(subId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
            public void setIsimGbabp(String gbabp, Message onComplete) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(gbabp);
                    if (onComplete != null) {
                        _data.writeInt(1);
                        onComplete.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setIsimGbabp(gbabp, onComplete);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
            public void setIsimGbabpForSubscriber(int subId, String gbabp, Message onComplete) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(gbabp);
                    if (onComplete != null) {
                        _data.writeInt(1);
                        onComplete.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setIsimGbabpForSubscriber(subId, gbabp, onComplete);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
            public boolean getUsimServiceForSubscriber(int subId, int service, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(service);
                    _data.writeString(callingPackage);
                    boolean _result = false;
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUsimServiceForSubscriber(subId, service, callingPackage);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
            public byte[] getUsimPsismsc() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUsimPsismsc();
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
            public byte[] getUsimPsismscForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUsimPsismscForSubscriber(subId);
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
            public byte[] getUsimSmsp() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUsimSmsp();
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
            public byte[] getUsimSmspForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUsimSmspForSubscriber(subId);
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
            public int getMncLength() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMncLength();
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

            @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
            public int getMncLengthForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMncLengthForSubscriber(subId);
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

            @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
            public String getIsimImpiForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIsimImpiForSubscriber(subId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
            public String getIsimDomainForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIsimDomainForSubscriber(subId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
            public String[] getIsimImpuForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIsimImpuForSubscriber(subId);
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
            public String getIsimIstForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIsimIstForSubscriber(subId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
            public String[] getIsimPcscfForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIsimPcscfForSubscriber(subId);
                    }
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
            public byte[] getIsimPsismsc() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIsimPsismsc();
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
            public byte[] getIsimPsismscForSubscriber(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIsimPsismscForSubscriber(subId);
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.mediatek.internal.telephony.IMtkPhoneSubInfoEx
            public String getLine1PhoneNumberForSubscriber(int subId, String callingPackage) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeString(callingPackage);
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLine1PhoneNumberForSubscriber(subId, callingPackage);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IMtkPhoneSubInfoEx impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMtkPhoneSubInfoEx getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
