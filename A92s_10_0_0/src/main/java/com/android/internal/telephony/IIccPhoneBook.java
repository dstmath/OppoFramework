package com.android.internal.telephony;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.internal.telephony.uicc.AdnRecord;
import java.util.List;

public interface IIccPhoneBook extends IInterface {
    List<AdnRecord> getAdnRecordsInEf(int i) throws RemoteException;

    List<AdnRecord> getAdnRecordsInEfForSubscriber(int i, int i2) throws RemoteException;

    int[] getAdnRecordsSize(int i) throws RemoteException;

    int[] getAdnRecordsSizeForSubscriber(int i, int i2) throws RemoteException;

    boolean updateAdnRecordsInEfByIndex(int i, String str, String str2, int i2, String str3) throws RemoteException;

    boolean updateAdnRecordsInEfByIndexForSubscriber(int i, int i2, String str, String str2, int i3, String str3) throws RemoteException;

    boolean updateAdnRecordsInEfBySearch(int i, String str, String str2, String str3, String str4, String str5) throws RemoteException;

    boolean updateAdnRecordsInEfBySearchForSubscriber(int i, int i2, String str, String str2, String str3, String str4, String str5) throws RemoteException;

    public static class Default implements IIccPhoneBook {
        @Override // com.android.internal.telephony.IIccPhoneBook
        public List<AdnRecord> getAdnRecordsInEf(int efid) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IIccPhoneBook
        public List<AdnRecord> getAdnRecordsInEfForSubscriber(int subId, int efid) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IIccPhoneBook
        public boolean updateAdnRecordsInEfBySearch(int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber, String pin2) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IIccPhoneBook
        public boolean updateAdnRecordsInEfBySearchForSubscriber(int subId, int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber, String pin2) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IIccPhoneBook
        public boolean updateAdnRecordsInEfByIndex(int efid, String newTag, String newPhoneNumber, int index, String pin2) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IIccPhoneBook
        public boolean updateAdnRecordsInEfByIndexForSubscriber(int subId, int efid, String newTag, String newPhoneNumber, int index, String pin2) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IIccPhoneBook
        public int[] getAdnRecordsSize(int efid) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IIccPhoneBook
        public int[] getAdnRecordsSizeForSubscriber(int subId, int efid) throws RemoteException {
            return null;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IIccPhoneBook {
        private static final String DESCRIPTOR = "com.android.internal.telephony.IIccPhoneBook";
        static final int TRANSACTION_getAdnRecordsInEf = 1;
        static final int TRANSACTION_getAdnRecordsInEfForSubscriber = 2;
        static final int TRANSACTION_getAdnRecordsSize = 7;
        static final int TRANSACTION_getAdnRecordsSizeForSubscriber = 8;
        static final int TRANSACTION_updateAdnRecordsInEfByIndex = 5;
        static final int TRANSACTION_updateAdnRecordsInEfByIndexForSubscriber = 6;
        static final int TRANSACTION_updateAdnRecordsInEfBySearch = 3;
        static final int TRANSACTION_updateAdnRecordsInEfBySearchForSubscriber = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IIccPhoneBook asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IIccPhoneBook)) {
                return new Proxy(obj);
            }
            return (IIccPhoneBook) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        List<AdnRecord> _result = getAdnRecordsInEf(data.readInt());
                        reply.writeNoException();
                        reply.writeTypedList(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        List<AdnRecord> _result2 = getAdnRecordsInEfForSubscriber(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeTypedList(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean updateAdnRecordsInEfBySearch = updateAdnRecordsInEfBySearch(data.readInt(), data.readString(), data.readString(), data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(updateAdnRecordsInEfBySearch ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean updateAdnRecordsInEfBySearchForSubscriber = updateAdnRecordsInEfBySearchForSubscriber(data.readInt(), data.readInt(), data.readString(), data.readString(), data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(updateAdnRecordsInEfBySearchForSubscriber ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean updateAdnRecordsInEfByIndex = updateAdnRecordsInEfByIndex(data.readInt(), data.readString(), data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(updateAdnRecordsInEfByIndex ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean updateAdnRecordsInEfByIndexForSubscriber = updateAdnRecordsInEfByIndexForSubscriber(data.readInt(), data.readInt(), data.readString(), data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(updateAdnRecordsInEfByIndexForSubscriber ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result3 = getAdnRecordsSize(data.readInt());
                        reply.writeNoException();
                        reply.writeIntArray(_result3);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result4 = getAdnRecordsSizeForSubscriber(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeIntArray(_result4);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IIccPhoneBook {
            public static IIccPhoneBook sDefaultImpl;
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

            @Override // com.android.internal.telephony.IIccPhoneBook
            public List<AdnRecord> getAdnRecordsInEf(int efid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(efid);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAdnRecordsInEf(efid);
                    }
                    _reply.readException();
                    List<AdnRecord> _result = _reply.createTypedArrayList(AdnRecord.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IIccPhoneBook
            public List<AdnRecord> getAdnRecordsInEfForSubscriber(int subId, int efid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(efid);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAdnRecordsInEfForSubscriber(subId, efid);
                    }
                    _reply.readException();
                    List<AdnRecord> _result = _reply.createTypedArrayList(AdnRecord.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IIccPhoneBook
            public boolean updateAdnRecordsInEfBySearch(int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber, String pin2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(efid);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(oldTag);
                        try {
                            _data.writeString(oldPhoneNumber);
                            try {
                                _data.writeString(newTag);
                            } catch (Throwable th2) {
                                th = th2;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(newPhoneNumber);
                        try {
                            _data.writeString(pin2);
                            boolean _result = false;
                            if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() != 0) {
                                    _result = true;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            boolean updateAdnRecordsInEfBySearch = Stub.getDefaultImpl().updateAdnRecordsInEfBySearch(efid, oldTag, oldPhoneNumber, newTag, newPhoneNumber, pin2);
                            _reply.recycle();
                            _data.recycle();
                            return updateAdnRecordsInEfBySearch;
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.android.internal.telephony.IIccPhoneBook
            public boolean updateAdnRecordsInEfBySearchForSubscriber(int subId, int efid, String oldTag, String oldPhoneNumber, String newTag, String newPhoneNumber, String pin2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(subId);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(efid);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(oldTag);
                        try {
                            _data.writeString(oldPhoneNumber);
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeString(newTag);
                            _data.writeString(newPhoneNumber);
                            _data.writeString(pin2);
                            boolean _result = false;
                            if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() != 0) {
                                    _result = true;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            boolean updateAdnRecordsInEfBySearchForSubscriber = Stub.getDefaultImpl().updateAdnRecordsInEfBySearchForSubscriber(subId, efid, oldTag, oldPhoneNumber, newTag, newPhoneNumber, pin2);
                            _reply.recycle();
                            _data.recycle();
                            return updateAdnRecordsInEfBySearchForSubscriber;
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.android.internal.telephony.IIccPhoneBook
            public boolean updateAdnRecordsInEfByIndex(int efid, String newTag, String newPhoneNumber, int index, String pin2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(efid);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(newTag);
                        try {
                            _data.writeString(newPhoneNumber);
                            try {
                                _data.writeInt(index);
                            } catch (Throwable th2) {
                                th = th2;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(pin2);
                        try {
                            boolean _result = false;
                            if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() != 0) {
                                    _result = true;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            boolean updateAdnRecordsInEfByIndex = Stub.getDefaultImpl().updateAdnRecordsInEfByIndex(efid, newTag, newPhoneNumber, index, pin2);
                            _reply.recycle();
                            _data.recycle();
                            return updateAdnRecordsInEfByIndex;
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.android.internal.telephony.IIccPhoneBook
            public boolean updateAdnRecordsInEfByIndexForSubscriber(int subId, int efid, String newTag, String newPhoneNumber, int index, String pin2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(subId);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(efid);
                        try {
                            _data.writeString(newTag);
                            try {
                                _data.writeString(newPhoneNumber);
                            } catch (Throwable th2) {
                                th = th2;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(index);
                        try {
                            _data.writeString(pin2);
                            boolean _result = false;
                            if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() != 0) {
                                    _result = true;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            boolean updateAdnRecordsInEfByIndexForSubscriber = Stub.getDefaultImpl().updateAdnRecordsInEfByIndexForSubscriber(subId, efid, newTag, newPhoneNumber, index, pin2);
                            _reply.recycle();
                            _data.recycle();
                            return updateAdnRecordsInEfByIndexForSubscriber;
                        } catch (Throwable th5) {
                            th = th5;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.android.internal.telephony.IIccPhoneBook
            public int[] getAdnRecordsSize(int efid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(efid);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAdnRecordsSize(efid);
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IIccPhoneBook
            public int[] getAdnRecordsSizeForSubscriber(int subId, int efid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    _data.writeInt(efid);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAdnRecordsSizeForSubscriber(subId, efid);
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IIccPhoneBook impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IIccPhoneBook getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
