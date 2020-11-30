package com.oppo.enterprise.mdmcoreservice.aidl;

import android.content.ComponentName;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IDeviceApplicationManager extends IInterface {
    void addAppAlarmWhiteList(ComponentName componentName, List<String> list) throws RemoteException;

    void addDisallowedRunningApp(ComponentName componentName, List<String> list) throws RemoteException;

    void addPersistentApp(ComponentName componentName, List<String> list) throws RemoteException;

    void addScreenPinningApp(ComponentName componentName, String str) throws RemoteException;

    void addTrustedAppStore(ComponentName componentName, String str) throws RemoteException;

    void cleanBackgroundProcess(ComponentName componentName) throws RemoteException;

    void clearScreenPinningApp(ComponentName componentName, String str) throws RemoteException;

    void deleteTrustedAppStore(ComponentName componentName, String str) throws RemoteException;

    void enableTrustedAppStore(ComponentName componentName, boolean z) throws RemoteException;

    boolean forceStopPackage(ComponentName componentName, List<String> list) throws RemoteException;

    List<String> getAppAlarmWhiteList(ComponentName componentName) throws RemoteException;

    Bundle getApplicationSettings(ComponentName componentName, String str, String str2) throws RemoteException;

    int getComponentSettings(ComponentName componentName) throws RemoteException;

    List<String> getDisabledAppList(ComponentName componentName) throws RemoteException;

    List<String> getDisallowedRunningApp(ComponentName componentName) throws RemoteException;

    int getDrawOverlays(ComponentName componentName, String str) throws RemoteException;

    List<String> getPersistentApp(ComponentName componentName) throws RemoteException;

    String getScreenPinningApp(ComponentName componentName) throws RemoteException;

    String getTopAppPackageName(ComponentName componentName) throws RemoteException;

    List<String> getTrustedAppStore(ComponentName componentName) throws RemoteException;

    boolean isTrustedAppStoreEnabled(ComponentName componentName) throws RemoteException;

    void killApplicationProcess(ComponentName componentName, String str) throws RemoteException;

    boolean removeAllAppAlarmWhiteList(ComponentName componentName) throws RemoteException;

    void removeAllDisallowedRunningApp(ComponentName componentName) throws RemoteException;

    boolean removeAppAlarmWhiteList(ComponentName componentName, List<String> list) throws RemoteException;

    void removeDisallowedRunningApp(ComponentName componentName, List<String> list) throws RemoteException;

    void removePersistentApp(ComponentName componentName, List<String> list) throws RemoteException;

    void setApplicationSettings(ComponentName componentName, String str, Bundle bundle) throws RemoteException;

    void setComponentSettings(ComponentName componentName, int i) throws RemoteException;

    boolean setDisabledAppList(ComponentName componentName, List<String> list, int i) throws RemoteException;

    boolean setDrawOverlays(ComponentName componentName, String str, int i) throws RemoteException;

    public static abstract class Stub extends Binder implements IDeviceApplicationManager {
        private static final String DESCRIPTOR = "com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager";
        static final int TRANSACTION_addAppAlarmWhiteList = 25;
        static final int TRANSACTION_addDisallowedRunningApp = 4;
        static final int TRANSACTION_addPersistentApp = 1;
        static final int TRANSACTION_addScreenPinningApp = 29;
        static final int TRANSACTION_addTrustedAppStore = 8;
        static final int TRANSACTION_cleanBackgroundProcess = 21;
        static final int TRANSACTION_clearScreenPinningApp = 30;
        static final int TRANSACTION_deleteTrustedAppStore = 9;
        static final int TRANSACTION_enableTrustedAppStore = 10;
        static final int TRANSACTION_forceStopPackage = 19;
        static final int TRANSACTION_getAppAlarmWhiteList = 26;
        static final int TRANSACTION_getApplicationSettings = 13;
        static final int TRANSACTION_getComponentSettings = 16;
        static final int TRANSACTION_getDisabledAppList = 18;
        static final int TRANSACTION_getDisallowedRunningApp = 7;
        static final int TRANSACTION_getDrawOverlays = 24;
        static final int TRANSACTION_getPersistentApp = 3;
        static final int TRANSACTION_getScreenPinningApp = 31;
        static final int TRANSACTION_getTopAppPackageName = 22;
        static final int TRANSACTION_getTrustedAppStore = 12;
        static final int TRANSACTION_isTrustedAppStoreEnabled = 11;
        static final int TRANSACTION_killApplicationProcess = 20;
        static final int TRANSACTION_removeAllAppAlarmWhiteList = 28;
        static final int TRANSACTION_removeAllDisallowedRunningApp = 6;
        static final int TRANSACTION_removeAppAlarmWhiteList = 27;
        static final int TRANSACTION_removeDisallowedRunningApp = 5;
        static final int TRANSACTION_removePersistentApp = 2;
        static final int TRANSACTION_setApplicationSettings = 14;
        static final int TRANSACTION_setComponentSettings = 15;
        static final int TRANSACTION_setDisabledAppList = 17;
        static final int TRANSACTION_setDrawOverlays = 23;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDeviceApplicationManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDeviceApplicationManager)) {
                return new Proxy(obj);
            }
            return (IDeviceApplicationManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ComponentName _arg0;
            if (code != 1598968902) {
                boolean _arg1 = false;
                ComponentName _arg02 = null;
                ComponentName _arg03 = null;
                ComponentName _arg04 = null;
                ComponentName _arg05 = null;
                ComponentName _arg06 = null;
                ComponentName _arg07 = null;
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
                Bundle _arg2 = null;
                ComponentName _arg020 = null;
                ComponentName _arg021 = null;
                ComponentName _arg022 = null;
                ComponentName _arg023 = null;
                ComponentName _arg024 = null;
                ComponentName _arg025 = null;
                ComponentName _arg026 = null;
                ComponentName _arg027 = null;
                ComponentName _arg028 = null;
                ComponentName _arg029 = null;
                ComponentName _arg030 = null;
                ComponentName _arg031 = null;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        addPersistentApp(_arg02, data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg031 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        removePersistentApp(_arg031, data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg030 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        List<String> _result = getPersistentApp(_arg030);
                        reply.writeNoException();
                        reply.writeStringList(_result);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg029 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        addDisallowedRunningApp(_arg029, data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg028 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        removeDisallowedRunningApp(_arg028, data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg027 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        removeAllDisallowedRunningApp(_arg027);
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg026 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        List<String> _result2 = getDisallowedRunningApp(_arg026);
                        reply.writeNoException();
                        reply.writeStringList(_result2);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg025 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        addTrustedAppStore(_arg025, data.readString());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg024 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        deleteTrustedAppStore(_arg024, data.readString());
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg023 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        enableTrustedAppStore(_arg023, _arg1);
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg022 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isTrustedAppStoreEnabled = isTrustedAppStoreEnabled(_arg022);
                        reply.writeNoException();
                        reply.writeInt(isTrustedAppStoreEnabled ? 1 : 0);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg021 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        List<String> _result3 = getTrustedAppStore(_arg021);
                        reply.writeNoException();
                        reply.writeStringList(_result3);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg020 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        Bundle _result4 = getApplicationSettings(_arg020, data.readString(), data.readString());
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        String _arg12 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        setApplicationSettings(_arg0, _arg12, _arg2);
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg019 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        setComponentSettings(_arg019, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg018 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        int _result5 = getComponentSettings(_arg018);
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg017 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean disabledAppList = setDisabledAppList(_arg017, data.createStringArrayList(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(disabledAppList ? 1 : 0);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg016 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        List<String> _result6 = getDisabledAppList(_arg016);
                        reply.writeNoException();
                        reply.writeStringList(_result6);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg015 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean forceStopPackage = forceStopPackage(_arg015, data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(forceStopPackage ? 1 : 0);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg014 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        killApplicationProcess(_arg014, data.readString());
                        reply.writeNoException();
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg013 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        cleanBackgroundProcess(_arg013);
                        reply.writeNoException();
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg012 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        String _result7 = getTopAppPackageName(_arg012);
                        reply.writeNoException();
                        reply.writeString(_result7);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg011 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean drawOverlays = setDrawOverlays(_arg011, data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(drawOverlays ? 1 : 0);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg010 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        int _result8 = getDrawOverlays(_arg010, data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg09 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        addAppAlarmWhiteList(_arg09, data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        List<String> _result9 = getAppAlarmWhiteList(_arg08);
                        reply.writeNoException();
                        reply.writeStringList(_result9);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean removeAppAlarmWhiteList = removeAppAlarmWhiteList(_arg07, data.createStringArrayList());
                        reply.writeNoException();
                        reply.writeInt(removeAppAlarmWhiteList ? 1 : 0);
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean removeAllAppAlarmWhiteList = removeAllAppAlarmWhiteList(_arg06);
                        reply.writeNoException();
                        reply.writeInt(removeAllAppAlarmWhiteList ? 1 : 0);
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        addScreenPinningApp(_arg05, data.readString());
                        reply.writeNoException();
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        clearScreenPinningApp(_arg04, data.readString());
                        reply.writeNoException();
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        String _result10 = getScreenPinningApp(_arg03);
                        reply.writeNoException();
                        reply.writeString(_result10);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IDeviceApplicationManager {
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
            public void addPersistentApp(ComponentName admin, List<String> packageNames) throws RemoteException {
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
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
            public void removePersistentApp(ComponentName admin, List<String> packageNames) throws RemoteException {
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
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
            public List<String> getPersistentApp(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
            public void addDisallowedRunningApp(ComponentName admin, List<String> packageNames) throws RemoteException {
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
            public void removeDisallowedRunningApp(ComponentName admin, List<String> packageNames) throws RemoteException {
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
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
            public void removeAllDisallowedRunningApp(ComponentName admin) throws RemoteException {
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
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
            public List<String> getDisallowedRunningApp(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
            public void addTrustedAppStore(ComponentName compName, String appStorePkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (compName != null) {
                        _data.writeInt(1);
                        compName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(appStorePkgName);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
            public void deleteTrustedAppStore(ComponentName compName, String appStorePkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (compName != null) {
                        _data.writeInt(1);
                        compName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(appStorePkgName);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
            public void enableTrustedAppStore(ComponentName compName, boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (compName != null) {
                        _data.writeInt(1);
                        compName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(enable ? 1 : 0);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
            public boolean isTrustedAppStoreEnabled(ComponentName compName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (compName != null) {
                        _data.writeInt(1);
                        compName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(11, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
            public List<String> getTrustedAppStore(ComponentName compName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (compName != null) {
                        _data.writeInt(1);
                        compName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
            public Bundle getApplicationSettings(ComponentName compName, String tag, String cmdName) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (compName != null) {
                        _data.writeInt(1);
                        compName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(tag);
                    _data.writeString(cmdName);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
            public void setApplicationSettings(ComponentName compName, String tag, Bundle cmdBundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (compName != null) {
                        _data.writeInt(1);
                        compName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(tag);
                    if (cmdBundle != null) {
                        _data.writeInt(1);
                        cmdBundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
            public void setComponentSettings(ComponentName compName, int newState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (compName != null) {
                        _data.writeInt(1);
                        compName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(newState);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
            public int getComponentSettings(ComponentName compName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (compName != null) {
                        _data.writeInt(1);
                        compName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
            public boolean setDisabledAppList(ComponentName componentName, List<String> pkgs, int mode) throws RemoteException {
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
                    _data.writeStringList(pkgs);
                    _data.writeInt(mode);
                    this.mRemote.transact(17, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
            public List<String> getDisabledAppList(ComponentName componentName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (componentName != null) {
                        _data.writeInt(1);
                        componentName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
            public boolean forceStopPackage(ComponentName admin, List<String> pkgs) throws RemoteException {
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
                    _data.writeStringList(pkgs);
                    this.mRemote.transact(19, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
            public void killApplicationProcess(ComponentName admin, String packageName) throws RemoteException {
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
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
            public void cleanBackgroundProcess(ComponentName compName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (compName != null) {
                        _data.writeInt(1);
                        compName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
            public String getTopAppPackageName(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
            public boolean setDrawOverlays(ComponentName admin, String packageName, int mode) throws RemoteException {
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
                    _data.writeString(packageName);
                    _data.writeInt(mode);
                    this.mRemote.transact(23, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
            public int getDrawOverlays(ComponentName admin, String packageName) throws RemoteException {
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
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
            public void addAppAlarmWhiteList(ComponentName admin, List<String> packageNames) throws RemoteException {
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
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
            public List<String> getAppAlarmWhiteList(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
            public boolean removeAppAlarmWhiteList(ComponentName admin, List<String> packageNames) throws RemoteException {
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
                    _data.writeStringList(packageNames);
                    this.mRemote.transact(27, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
            public boolean removeAllAppAlarmWhiteList(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(28, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
            public void addScreenPinningApp(ComponentName compName, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (compName != null) {
                        _data.writeInt(1);
                        compName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
            public void clearScreenPinningApp(ComponentName compName, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (compName != null) {
                        _data.writeInt(1);
                        compName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceApplicationManager
            public String getScreenPinningApp(ComponentName compName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (compName != null) {
                        _data.writeInt(1);
                        compName.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
