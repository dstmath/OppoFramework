package com.oppo.os;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ILinearmotorVibratorService extends IInterface {
    void cancelVibrate(WaveformEffect waveformEffect, IBinder iBinder) throws RemoteException;

    void vibrate(int i, String str, WaveformEffect waveformEffect, IBinder iBinder) throws RemoteException;

    public static class Default implements ILinearmotorVibratorService {
        @Override // com.oppo.os.ILinearmotorVibratorService
        public void vibrate(int uid, String opPkg, WaveformEffect we, IBinder token) throws RemoteException {
        }

        @Override // com.oppo.os.ILinearmotorVibratorService
        public void cancelVibrate(WaveformEffect we, IBinder token) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ILinearmotorVibratorService {
        private static final String DESCRIPTOR = "com.oppo.os.ILinearmotorVibratorService";
        static final int TRANSACTION_cancelVibrate = 2;
        static final int TRANSACTION_vibrate = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ILinearmotorVibratorService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ILinearmotorVibratorService)) {
                return new Proxy(obj);
            }
            return (ILinearmotorVibratorService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "vibrate";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "cancelVibrate";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            WaveformEffect _arg2;
            WaveformEffect _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _arg02 = data.readInt();
                String _arg1 = data.readString();
                if (data.readInt() != 0) {
                    _arg2 = WaveformEffect.CREATOR.createFromParcel(data);
                } else {
                    _arg2 = null;
                }
                vibrate(_arg02, _arg1, _arg2, data.readStrongBinder());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = WaveformEffect.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                cancelVibrate(_arg0, data.readStrongBinder());
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
        public static class Proxy implements ILinearmotorVibratorService {
            public static ILinearmotorVibratorService sDefaultImpl;
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

            @Override // com.oppo.os.ILinearmotorVibratorService
            public void vibrate(int uid, String opPkg, WaveformEffect we, IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(opPkg);
                    if (we != null) {
                        _data.writeInt(1);
                        we.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(token);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().vibrate(uid, opPkg, we, token);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.os.ILinearmotorVibratorService
            public void cancelVibrate(WaveformEffect we, IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (we != null) {
                        _data.writeInt(1);
                        we.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(token);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().cancelVibrate(we, token);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ILinearmotorVibratorService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ILinearmotorVibratorService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
