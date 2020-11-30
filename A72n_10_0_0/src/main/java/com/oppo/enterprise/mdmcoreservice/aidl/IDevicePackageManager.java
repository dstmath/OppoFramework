package com.oppo.enterprise.mdmcoreservice.aidl;

import android.content.ComponentName;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;
import java.util.Map;

public interface IDevicePackageManager extends IInterface {
    void addDisabledDeactivateMdmPackages(List<String> list) throws RemoteException;

    void addDisallowedUninstallPackages(ComponentName componentName, List<String> list) throws RemoteException;

    boolean clearAllSuperWhiteList(ComponentName componentName) throws RemoteException;

    void clearApplicationUserData(String str) throws RemoteException;

    boolean clearDefaultBrowser() throws RemoteException;

    boolean clearSuperWhiteList(ComponentName componentName, List<String> list) throws RemoteException;

    void deleteApplicationCacheFiles(ComponentName componentName, String str) throws RemoteException;

    boolean getAdbInstallUninstallDisabled(ComponentName componentName) throws RemoteException;

    String getAppPermission(String str) throws RemoteException;

    List<String> getClearAppName() throws RemoteException;

    String getDefaultBrowser() throws RemoteException;

    String getDefaultDialerPackage(ComponentName componentName) throws RemoteException;

    String getDefaultMessage() throws RemoteException;

    List<String> getDisabledDeactivateMdmPackages(ComponentName componentName) throws RemoteException;

    List<String> getDisallowUninstallPackageList(ComponentName componentName) throws RemoteException;

    List<String> getSuperWhiteList() throws RemoteException;

    List<String> getSysAppList(ComponentName componentName, List list) throws RemoteException;

    String getSystemDialerPackage(ComponentName componentName) throws RemoteException;

    void installPackage(ComponentName componentName, String str, int i) throws RemoteException;

    void removeAllDisabledDeactivateMdmPackages(ComponentName componentName) throws RemoteException;

    void removeAllDisallowedUninstallPackages(ComponentName componentName) throws RemoteException;

    void removeDisabledDeactivateMdmPackages(List<String> list) throws RemoteException;

    void removeDisallowedUninstallPackages(ComponentName componentName, List<String> list) throws RemoteException;

    void setAdbInstallUninstallDisabled(ComponentName componentName, boolean z) throws RemoteException;

    boolean setAppPermission(String str, String str2, boolean z) throws RemoteException;

    void setDefaultApplication(ComponentName componentName, ComponentName componentName2, String str) throws RemoteException;

    boolean setDefaultBrowser(String str) throws RemoteException;

    void setDefaultDialer(String str) throws RemoteException;

    boolean setDefaultMessage(String str) throws RemoteException;

    boolean setSuperWhiteList(ComponentName componentName, List<String> list) throws RemoteException;

    void setSysAppList(ComponentName componentName, Map map, Bundle bundle) throws RemoteException;

    void uninstallPackage(ComponentName componentName, String str, int i) throws RemoteException;

    public static abstract class Stub extends Binder implements IDevicePackageManager {
        private static final String DESCRIPTOR = "com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager";
        static final int TRANSACTION_addDisabledDeactivateMdmPackages = 22;
        static final int TRANSACTION_addDisallowedUninstallPackages = 3;
        static final int TRANSACTION_clearAllSuperWhiteList = 32;
        static final int TRANSACTION_clearApplicationUserData = 7;
        static final int TRANSACTION_clearDefaultBrowser = 20;
        static final int TRANSACTION_clearSuperWhiteList = 31;
        static final int TRANSACTION_deleteApplicationCacheFiles = 11;
        static final int TRANSACTION_getAdbInstallUninstallDisabled = 10;
        static final int TRANSACTION_getAppPermission = 13;
        static final int TRANSACTION_getClearAppName = 8;
        static final int TRANSACTION_getDefaultBrowser = 21;
        static final int TRANSACTION_getDefaultDialerPackage = 15;
        static final int TRANSACTION_getDefaultMessage = 18;
        static final int TRANSACTION_getDisabledDeactivateMdmPackages = 25;
        static final int TRANSACTION_getDisallowUninstallPackageList = 6;
        static final int TRANSACTION_getSuperWhiteList = 30;
        static final int TRANSACTION_getSysAppList = 28;
        static final int TRANSACTION_getSystemDialerPackage = 16;
        static final int TRANSACTION_installPackage = 1;
        static final int TRANSACTION_removeAllDisabledDeactivateMdmPackages = 24;
        static final int TRANSACTION_removeAllDisallowedUninstallPackages = 5;
        static final int TRANSACTION_removeDisabledDeactivateMdmPackages = 23;
        static final int TRANSACTION_removeDisallowedUninstallPackages = 4;
        static final int TRANSACTION_setAdbInstallUninstallDisabled = 9;
        static final int TRANSACTION_setAppPermission = 12;
        static final int TRANSACTION_setDefaultApplication = 26;
        static final int TRANSACTION_setDefaultBrowser = 19;
        static final int TRANSACTION_setDefaultDialer = 14;
        static final int TRANSACTION_setDefaultMessage = 17;
        static final int TRANSACTION_setSuperWhiteList = 29;
        static final int TRANSACTION_setSysAppList = 27;
        static final int TRANSACTION_uninstallPackage = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDevicePackageManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDevicePackageManager)) {
                return new Proxy(obj);
            }
            return (IDevicePackageManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ComponentName _arg0;
            ComponentName _arg02;
            if (code != 1598968902) {
                boolean _arg2 = false;
                ComponentName _arg03 = null;
                ComponentName _arg04 = null;
                ComponentName _arg05 = null;
                ComponentName _arg06 = null;
                ComponentName _arg07 = null;
                Bundle _arg22 = null;
                ComponentName _arg1 = null;
                ComponentName _arg08 = null;
                ComponentName _arg09 = null;
                ComponentName _arg010 = null;
                ComponentName _arg011 = null;
                ComponentName _arg012 = null;
                ComponentName _arg013 = null;
                ComponentName _arg014 = null;
                ComponentName _arg015 = null;
                ComponentName _arg016 = null;
                ComponentName _arg017 = null;
                ComponentName _arg018 = null;
                ComponentName _arg019 = null;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        installPackage(_arg03, data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg019 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        uninstallPackage(_arg019, data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg018 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        addDisallowedUninstallPackages(_arg018, data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg017 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        removeDisallowedUninstallPackages(_arg017, data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg016 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        removeAllDisallowedUninstallPackages(_arg016);
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg015 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        List<String> _result = getDisallowUninstallPackageList(_arg015);
                        reply.writeNoException();
                        reply.writeStringList(_result);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        clearApplicationUserData(data.readString());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result2 = getClearAppName();
                        reply.writeNoException();
                        reply.writeStringList(_result2);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg014 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        setAdbInstallUninstallDisabled(_arg014, _arg2);
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg013 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean adbInstallUninstallDisabled = getAdbInstallUninstallDisabled(_arg013);
                        reply.writeNoException();
                        reply.writeInt(adbInstallUninstallDisabled ? 1 : 0);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg012 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        deleteApplicationCacheFiles(_arg012, data.readString());
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg020 = data.readString();
                        String _arg12 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        boolean appPermission = setAppPermission(_arg020, _arg12, _arg2);
                        reply.writeNoException();
                        reply.writeInt(appPermission ? 1 : 0);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        String _result3 = getAppPermission(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result3);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        setDefaultDialer(data.readString());
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg011 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        String _result4 = getDefaultDialerPackage(_arg011);
                        reply.writeNoException();
                        reply.writeString(_result4);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg010 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        String _result5 = getSystemDialerPackage(_arg010);
                        reply.writeNoException();
                        reply.writeString(_result5);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        boolean defaultMessage = setDefaultMessage(data.readString());
                        reply.writeNoException();
                        reply.writeInt(defaultMessage ? 1 : 0);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        String _result6 = getDefaultMessage();
                        reply.writeNoException();
                        reply.writeString(_result6);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        boolean defaultBrowser = setDefaultBrowser(data.readString());
                        reply.writeNoException();
                        reply.writeInt(defaultBrowser ? 1 : 0);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        boolean clearDefaultBrowser = clearDefaultBrowser();
                        reply.writeNoException();
                        reply.writeInt(clearDefaultBrowser ? 1 : 0);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        String _result7 = getDefaultBrowser();
                        reply.writeNoException();
                        reply.writeString(_result7);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        addDisabledDeactivateMdmPackages(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        removeDisabledDeactivateMdmPackages(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg09 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        removeAllDisabledDeactivateMdmPackages(_arg09);
                        reply.writeNoException();
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        List<String> _result8 = getDisabledDeactivateMdmPackages(_arg08);
                        reply.writeNoException();
                        reply.writeStringList(_result8);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg1 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        setDefaultApplication(_arg0, _arg1, data.readString());
                        reply.writeNoException();
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        Map _arg13 = data.readHashMap(getClass().getClassLoader());
                        if (data.readInt() != 0) {
                            _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        setSysAppList(_arg02, _arg13, _arg22);
                        reply.writeNoException();
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        List<String> _result9 = getSysAppList(_arg07, data.readArrayList(getClass().getClassLoader()));
                        reply.writeNoException();
                        reply.writeStringList(_result9);
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean superWhiteList = setSuperWhiteList(_arg06, data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(superWhiteList ? 1 : 0);
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result10 = getSuperWhiteList();
                        reply.writeNoException();
                        reply.writeStringList(_result10);
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean clearSuperWhiteList = clearSuperWhiteList(_arg05, data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(clearSuperWhiteList ? 1 : 0);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean clearAllSuperWhiteList = clearAllSuperWhiteList(_arg04);
                        reply.writeNoException();
                        reply.writeInt(clearAllSuperWhiteList ? 1 : 0);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IDevicePackageManager {
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
            public void installPackage(ComponentName admin, String packagePath, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packagePath);
                    _data.writeInt(flags);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
            public void uninstallPackage(ComponentName admin, String packageName, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    _data.writeInt(flags);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
            public void addDisallowedUninstallPackages(ComponentName admin, List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringList(packageNames);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
            public void removeDisallowedUninstallPackages(ComponentName admin, List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringList(packageNames);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
            public void removeAllDisallowedUninstallPackages(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
            public List<String> getDisallowUninstallPackageList(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
            public void clearApplicationUserData(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
            public List<String> getClearAppName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
            public void setAdbInstallUninstallDisabled(ComponentName admin, boolean disabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(disabled ? 1 : 0);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
            public boolean getAdbInstallUninstallDisabled(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
            public void deleteApplicationCacheFiles(ComponentName admin, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
            public boolean setAppPermission(String appPackageName, String permissions, boolean fixed) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(appPackageName);
                    _data.writeString(permissions);
                    _data.writeInt(fixed ? 1 : 0);
                    boolean _result = false;
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
            public String getAppPermission(String appPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(appPackageName);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
            public void setDefaultDialer(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
            public String getDefaultDialerPackage(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
            public String getSystemDialerPackage(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
            public boolean setDefaultMessage(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
            public String getDefaultMessage() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
            public boolean setDefaultBrowser(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = false;
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
            public boolean clearDefaultBrowser() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
            public String getDefaultBrowser() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
            public void addDisabledDeactivateMdmPackages(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
            public void removeDisabledDeactivateMdmPackages(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
            public void removeAllDisabledDeactivateMdmPackages(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
            public List<String> getDisabledDeactivateMdmPackages(ComponentName admin) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
            public void setDefaultApplication(ComponentName admin, ComponentName componentName, String type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(type);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
            public void setSysAppList(ComponentName admin, Map maps, Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeMap(maps);
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
            public List<String> getSysAppList(ComponentName admin, List pkgNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (admin != null) {
                        _data.writeInt(1);
                        admin.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeList(pkgNames);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
            public boolean setSuperWhiteList(ComponentName componentName, List<String> list) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringList(list);
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
            public List<String> getSuperWhiteList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
            public boolean clearSuperWhiteList(ComponentName componentName, List<String> clearList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStringList(clearList);
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDevicePackageManager
            public boolean clearAllSuperWhiteList(ComponentName componentName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
