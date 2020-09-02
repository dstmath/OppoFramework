package com.android.internal.telephony;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IOemLinkLatencyManager extends IInterface {
    boolean gameOptimizeExit() throws RemoteException;

    boolean gameOptimizeSetLoad(int i, String str) throws RemoteException;

    OemLinkLatencyInfo getCurrentLevel() throws RemoteException;

    long prioritizeDefaultDataSubscription(boolean z) throws RemoteException;

    void setLevel(long j, long j2, long j3) throws RemoteException;

    public static class Default implements IOemLinkLatencyManager {
        @Override // com.android.internal.telephony.IOemLinkLatencyManager
        public void setLevel(long rat, long uplink, long downlink) throws RemoteException {
        }

        @Override // com.android.internal.telephony.IOemLinkLatencyManager
        public OemLinkLatencyInfo getCurrentLevel() throws RemoteException {
            return null;
        }

        @Override // com.android.internal.telephony.IOemLinkLatencyManager
        public long prioritizeDefaultDataSubscription(boolean isEnabled) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.telephony.IOemLinkLatencyManager
        public boolean gameOptimizeSetLoad(int id, String pkgName) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.telephony.IOemLinkLatencyManager
        public boolean gameOptimizeExit() throws RemoteException {
            return false;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOemLinkLatencyManager {
        private static final String DESCRIPTOR = "com.android.internal.telephony.IOemLinkLatencyManager";
        static final int TRANSACTION_gameOptimizeExit = 5;
        static final int TRANSACTION_gameOptimizeSetLoad = 4;
        static final int TRANSACTION_getCurrentLevel = 2;
        static final int TRANSACTION_prioritizeDefaultDataSubscription = 3;
        static final int TRANSACTION_setLevel = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOemLinkLatencyManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOemLinkLatencyManager)) {
                return new Proxy(obj);
            }
            return (IOemLinkLatencyManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1) {
                boolean _arg0 = false;
                if (code == 2) {
                    data.enforceInterface(DESCRIPTOR);
                    OemLinkLatencyInfo _result = getCurrentLevel();
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(1);
                        _result.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                } else if (code == 3) {
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = true;
                    }
                    long _result2 = prioritizeDefaultDataSubscription(_arg0);
                    reply.writeNoException();
                    reply.writeLong(_result2);
                    return true;
                } else if (code == 4) {
                    data.enforceInterface(DESCRIPTOR);
                    boolean gameOptimizeSetLoad = gameOptimizeSetLoad(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(gameOptimizeSetLoad ? 1 : 0);
                    return true;
                } else if (code == 5) {
                    data.enforceInterface(DESCRIPTOR);
                    boolean gameOptimizeExit = gameOptimizeExit();
                    reply.writeNoException();
                    reply.writeInt(gameOptimizeExit ? 1 : 0);
                    return true;
                } else if (code != 1598968902) {
                    return super.onTransact(code, data, reply, flags);
                } else {
                    reply.writeString(DESCRIPTOR);
                    return true;
                }
            } else {
                data.enforceInterface(DESCRIPTOR);
                setLevel(data.readLong(), data.readLong(), data.readLong());
                reply.writeNoException();
                return true;
            }
        }

        private static class Proxy implements IOemLinkLatencyManager {
            public static IOemLinkLatencyManager sDefaultImpl;
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

            @Override // com.android.internal.telephony.IOemLinkLatencyManager
            public void setLevel(long rat, long uplink, long downlink) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeLong(rat);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(uplink);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeLong(downlink);
                        if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().setLevel(rat, uplink, downlink);
                        _reply.recycle();
                        _data.recycle();
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
            }

            @Override // com.android.internal.telephony.IOemLinkLatencyManager
            public OemLinkLatencyInfo getCurrentLevel() throws RemoteException {
                OemLinkLatencyInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCurrentLevel();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = OemLinkLatencyInfo.CREATOR.createFromParcel(_reply);
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

            @Override // com.android.internal.telephony.IOemLinkLatencyManager
            public long prioritizeDefaultDataSubscription(boolean isEnabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isEnabled ? 1 : 0);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().prioritizeDefaultDataSubscription(isEnabled);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.telephony.IOemLinkLatencyManager
            public boolean gameOptimizeSetLoad(int id, String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(id);
                    _data.writeString(pkgName);
                    boolean _result = false;
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().gameOptimizeSetLoad(id, pkgName);
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

            @Override // com.android.internal.telephony.IOemLinkLatencyManager
            public boolean gameOptimizeExit() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().gameOptimizeExit();
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
        }

        public static boolean setDefaultImpl(IOemLinkLatencyManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOemLinkLatencyManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
