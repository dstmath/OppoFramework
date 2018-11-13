package com.mediatek.apm.test.service;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface ITestAmPolicyMakerService extends IInterface {

    public static abstract class Stub extends Binder implements ITestAmPolicyMakerService {

        private static class a implements ITestAmPolicyMakerService {
            private IBinder A;

            a(IBinder iBinder) {
                this.A = iBinder;
            }

            public IBinder asBinder() {
                return this.A;
            }

            public void startFrc(String str, int i, List<String> list) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.mediatek.apm.test.service.ITestAmPolicyMakerService");
                    obtain.writeString(str);
                    obtain.writeInt(i);
                    obtain.writeStringList(list);
                    this.A.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void stopFrc(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.mediatek.apm.test.service.ITestAmPolicyMakerService");
                    obtain.writeString(str);
                    this.A.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public List<String> getFrcPackageList(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.mediatek.apm.test.service.ITestAmPolicyMakerService");
                    obtain.writeString(str);
                    this.A.transact(3, obtain, obtain2, 0);
                    obtain2.readException();
                    List<String> createStringArrayList = obtain2.createStringArrayList();
                    return createStringArrayList;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void updateFrcExtraAllowList(String str, List<String> list) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.mediatek.apm.test.service.ITestAmPolicyMakerService");
                    obtain.writeString(str);
                    obtain.writeStringList(list);
                    this.A.transact(4, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void startSuppression(String str, int i, int i2, String str2, List<String> list) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.mediatek.apm.test.service.ITestAmPolicyMakerService");
                    obtain.writeString(str);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeString(str2);
                    obtain.writeStringList(list);
                    this.A.transact(5, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void stopSuppression(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.mediatek.apm.test.service.ITestAmPolicyMakerService");
                    obtain.writeString(str);
                    this.A.transact(6, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void updateSuppressionExtraAllowList(String str, List<String> list) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.mediatek.apm.test.service.ITestAmPolicyMakerService");
                    obtain.writeString(str);
                    obtain.writeStringList(list);
                    this.A.transact(7, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public List<String> getSuppressionList() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.mediatek.apm.test.service.ITestAmPolicyMakerService");
                    this.A.transact(8, obtain, obtain2, 0);
                    obtain2.readException();
                    List<String> createStringArrayList = obtain2.createStringArrayList();
                    return createStringArrayList;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public boolean isPackageInSuppression(String str, String str2, int i) throws RemoteException {
                boolean z = false;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.mediatek.apm.test.service.ITestAmPolicyMakerService");
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    obtain.writeInt(i);
                    this.A.transact(9, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() != 0) {
                        z = true;
                    }
                    obtain2.recycle();
                    obtain.recycle();
                    return z;
                } catch (Throwable th) {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, "com.mediatek.apm.test.service.ITestAmPolicyMakerService");
        }

        public static ITestAmPolicyMakerService asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.mediatek.apm.test.service.ITestAmPolicyMakerService");
            if (queryLocalInterface != null && (queryLocalInterface instanceof ITestAmPolicyMakerService)) {
                return (ITestAmPolicyMakerService) queryLocalInterface;
            }
            return new a(iBinder);
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            int i3 = 0;
            List frcPackageList;
            switch (i) {
                case 1:
                    parcel.enforceInterface("com.mediatek.apm.test.service.ITestAmPolicyMakerService");
                    startFrc(parcel.readString(), parcel.readInt(), parcel.createStringArrayList());
                    parcel2.writeNoException();
                    return true;
                case 2:
                    parcel.enforceInterface("com.mediatek.apm.test.service.ITestAmPolicyMakerService");
                    stopFrc(parcel.readString());
                    parcel2.writeNoException();
                    return true;
                case 3:
                    parcel.enforceInterface("com.mediatek.apm.test.service.ITestAmPolicyMakerService");
                    frcPackageList = getFrcPackageList(parcel.readString());
                    parcel2.writeNoException();
                    parcel2.writeStringList(frcPackageList);
                    return true;
                case 4:
                    parcel.enforceInterface("com.mediatek.apm.test.service.ITestAmPolicyMakerService");
                    updateFrcExtraAllowList(parcel.readString(), parcel.createStringArrayList());
                    parcel2.writeNoException();
                    return true;
                case 5:
                    parcel.enforceInterface("com.mediatek.apm.test.service.ITestAmPolicyMakerService");
                    startSuppression(parcel.readString(), parcel.readInt(), parcel.readInt(), parcel.readString(), parcel.createStringArrayList());
                    parcel2.writeNoException();
                    return true;
                case 6:
                    parcel.enforceInterface("com.mediatek.apm.test.service.ITestAmPolicyMakerService");
                    stopSuppression(parcel.readString());
                    parcel2.writeNoException();
                    return true;
                case 7:
                    parcel.enforceInterface("com.mediatek.apm.test.service.ITestAmPolicyMakerService");
                    updateSuppressionExtraAllowList(parcel.readString(), parcel.createStringArrayList());
                    parcel2.writeNoException();
                    return true;
                case 8:
                    parcel.enforceInterface("com.mediatek.apm.test.service.ITestAmPolicyMakerService");
                    frcPackageList = getSuppressionList();
                    parcel2.writeNoException();
                    parcel2.writeStringList(frcPackageList);
                    return true;
                case 9:
                    parcel.enforceInterface("com.mediatek.apm.test.service.ITestAmPolicyMakerService");
                    boolean isPackageInSuppression = isPackageInSuppression(parcel.readString(), parcel.readString(), parcel.readInt());
                    parcel2.writeNoException();
                    if (isPackageInSuppression) {
                        i3 = 1;
                    }
                    parcel2.writeInt(i3);
                    return true;
                case 1598968902:
                    parcel2.writeString("com.mediatek.apm.test.service.ITestAmPolicyMakerService");
                    return true;
                default:
                    return super.onTransact(i, parcel, parcel2, i2);
            }
        }
    }

    List<String> getFrcPackageList(String str) throws RemoteException;

    List<String> getSuppressionList() throws RemoteException;

    boolean isPackageInSuppression(String str, String str2, int i) throws RemoteException;

    void startFrc(String str, int i, List<String> list) throws RemoteException;

    void startSuppression(String str, int i, int i2, String str2, List<String> list) throws RemoteException;

    void stopFrc(String str) throws RemoteException;

    void stopSuppression(String str) throws RemoteException;

    void updateFrcExtraAllowList(String str, List<String> list) throws RemoteException;

    void updateSuppressionExtraAllowList(String str, List<String> list) throws RemoteException;
}
