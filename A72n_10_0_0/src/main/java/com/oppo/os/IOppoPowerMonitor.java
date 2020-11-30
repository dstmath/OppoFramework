package com.oppo.os;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IOppoPowerMonitor extends IInterface {
    void recordAlarmWakeupEvent() throws RemoteException;

    void recordAppWakeupEvent(int i, String str) throws RemoteException;

    void resetWakeupEventRecords() throws RemoteException;

    public static class Default implements IOppoPowerMonitor {
        @Override // com.oppo.os.IOppoPowerMonitor
        public void recordAlarmWakeupEvent() throws RemoteException {
        }

        @Override // com.oppo.os.IOppoPowerMonitor
        public void recordAppWakeupEvent(int alarmType, String alarmPackageName) throws RemoteException {
        }

        @Override // com.oppo.os.IOppoPowerMonitor
        public void resetWakeupEventRecords() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOppoPowerMonitor {
        private static final String DESCRIPTOR = "com.oppo.os.IOppoPowerMonitor";
        static final int TRANSACTION_recordAlarmWakeupEvent = 1;
        static final int TRANSACTION_recordAppWakeupEvent = 2;
        static final int TRANSACTION_resetWakeupEventRecords = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOppoPowerMonitor asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOppoPowerMonitor)) {
                return new Proxy(obj);
            }
            return (IOppoPowerMonitor) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "recordAlarmWakeupEvent";
            }
            if (transactionCode == 2) {
                return "recordAppWakeupEvent";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "resetWakeupEventRecords";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                recordAlarmWakeupEvent();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                recordAppWakeupEvent(data.readInt(), data.readString());
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                resetWakeupEventRecords();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IOppoPowerMonitor {
            public static IOppoPowerMonitor sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // com.oppo.os.IOppoPowerMonitor
            public void recordAlarmWakeupEvent() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().recordAlarmWakeupEvent();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.oppo.os.IOppoPowerMonitor
            public void recordAppWakeupEvent(int alarmType, String alarmPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(alarmType);
                    _data.writeString(alarmPackageName);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().recordAppWakeupEvent(alarmType, alarmPackageName);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.oppo.os.IOppoPowerMonitor
            public void resetWakeupEventRecords() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().resetWakeupEventRecords();
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IOppoPowerMonitor impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOppoPowerMonitor getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
