package com.oppo.enterprise.mdmcoreservice.aidl;

import android.content.ComponentName;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IDeviceControlerManager extends IInterface {
    void addAccessibilityServiceToWhiteList(List<String> list) throws RemoteException;

    void clearDefaultInputMethod() throws RemoteException;

    void clearDefaultLauncher(ComponentName componentName) throws RemoteException;

    void deleteAccessibilityServiceWhiteList() throws RemoteException;

    void disableAccessibilityService(ComponentName componentName, ComponentName componentName2) throws RemoteException;

    void enableAccessibilityService(ComponentName componentName, ComponentName componentName2) throws RemoteException;

    boolean formatSDCard(ComponentName componentName, String str) throws RemoteException;

    List<ComponentName> getAccessibilityService(ComponentName componentName) throws RemoteException;

    List<String> getAccessibilityServiceWhiteList() throws RemoteException;

    boolean getAirplaneMode(ComponentName componentName) throws RemoteException;

    void getCertInstaller() throws RemoteException;

    String getDefaultInputMethod() throws RemoteException;

    String getDefaultLauncher() throws RemoteException;

    int[] getKeyguardPolicy(ComponentName componentName) throws RemoteException;

    boolean isAccessibilityServiceEnabled(ComponentName componentName) throws RemoteException;

    boolean isDisableKeyguardForgetPassword(ComponentName componentName) throws RemoteException;

    void rebootDevice(ComponentName componentName) throws RemoteException;

    void removeAccessibilityServiceFromWhiteList(List<String> list) throws RemoteException;

    void setAirplaneMode(ComponentName componentName, boolean z) throws RemoteException;

    void setCustomSettingsMenu(ComponentName componentName, List<String> list) throws RemoteException;

    boolean setDefaultInputMethod(String str) throws RemoteException;

    boolean setDefaultLauncher(ComponentName componentName, ComponentName componentName2) throws RemoteException;

    boolean setDisableKeyguardForgetPassword(ComponentName componentName, boolean z) throws RemoteException;

    void setKeyguardPolicy(ComponentName componentName, int[] iArr) throws RemoteException;

    boolean setSysTime(ComponentName componentName, long j) throws RemoteException;

    void shutdownDevice(ComponentName componentName) throws RemoteException;

    boolean wipeDeviceData() throws RemoteException;

    public static abstract class Stub extends Binder implements IDeviceControlerManager {
        private static final String DESCRIPTOR = "com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager";
        static final int TRANSACTION_addAccessibilityServiceToWhiteList = 8;
        static final int TRANSACTION_clearDefaultInputMethod = 27;
        static final int TRANSACTION_clearDefaultLauncher = 15;
        static final int TRANSACTION_deleteAccessibilityServiceWhiteList = 11;
        static final int TRANSACTION_disableAccessibilityService = 5;
        static final int TRANSACTION_enableAccessibilityService = 4;
        static final int TRANSACTION_formatSDCard = 3;
        static final int TRANSACTION_getAccessibilityService = 7;
        static final int TRANSACTION_getAccessibilityServiceWhiteList = 10;
        static final int TRANSACTION_getAirplaneMode = 19;
        static final int TRANSACTION_getCertInstaller = 20;
        static final int TRANSACTION_getDefaultInputMethod = 26;
        static final int TRANSACTION_getDefaultLauncher = 14;
        static final int TRANSACTION_getKeyguardPolicy = 24;
        static final int TRANSACTION_isAccessibilityServiceEnabled = 6;
        static final int TRANSACTION_isDisableKeyguardForgetPassword = 22;
        static final int TRANSACTION_rebootDevice = 2;
        static final int TRANSACTION_removeAccessibilityServiceFromWhiteList = 9;
        static final int TRANSACTION_setAirplaneMode = 18;
        static final int TRANSACTION_setCustomSettingsMenu = 17;
        static final int TRANSACTION_setDefaultInputMethod = 25;
        static final int TRANSACTION_setDefaultLauncher = 13;
        static final int TRANSACTION_setDisableKeyguardForgetPassword = 21;
        static final int TRANSACTION_setKeyguardPolicy = 23;
        static final int TRANSACTION_setSysTime = 12;
        static final int TRANSACTION_shutdownDevice = 1;
        static final int TRANSACTION_wipeDeviceData = 16;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDeviceControlerManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDeviceControlerManager)) {
                return new Proxy(obj);
            }
            return (IDeviceControlerManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ComponentName _arg0;
            ComponentName _arg02;
            ComponentName _arg03;
            if (code != 1598968902) {
                boolean _arg1 = false;
                ComponentName _arg04 = null;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        shutdownDevice(_arg04);
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        rebootDevice(_arg04);
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean formatSDCard = formatSDCard(_arg04, data.readString());
                        reply.writeNoException();
                        reply.writeInt(formatSDCard ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg04 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        enableAccessibilityService(_arg0, _arg04);
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg04 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        disableAccessibilityService(_arg02, _arg04);
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isAccessibilityServiceEnabled = isAccessibilityServiceEnabled(_arg04);
                        reply.writeNoException();
                        reply.writeInt(isAccessibilityServiceEnabled ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        List<ComponentName> _result = getAccessibilityService(_arg04);
                        reply.writeNoException();
                        reply.writeTypedList(_result);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        addAccessibilityServiceToWhiteList(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        removeAccessibilityServiceFromWhiteList(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result2 = getAccessibilityServiceWhiteList();
                        reply.writeNoException();
                        reply.writeStringList(_result2);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        deleteAccessibilityServiceWhiteList();
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean sysTime = setSysTime(_arg04, data.readLong());
                        reply.writeNoException();
                        reply.writeInt(sysTime ? 1 : 0);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg04 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean defaultLauncher = setDefaultLauncher(_arg03, _arg04);
                        reply.writeNoException();
                        reply.writeInt(defaultLauncher ? 1 : 0);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        String _result3 = getDefaultLauncher();
                        reply.writeNoException();
                        reply.writeString(_result3);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        clearDefaultLauncher(_arg04);
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        boolean wipeDeviceData = wipeDeviceData();
                        reply.writeNoException();
                        reply.writeInt(wipeDeviceData ? 1 : 0);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        setCustomSettingsMenu(_arg04, data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setAirplaneMode(_arg04, _arg1);
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean airplaneMode = getAirplaneMode(_arg04);
                        reply.writeNoException();
                        reply.writeInt(airplaneMode ? 1 : 0);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        getCertInstaller();
                        reply.writeNoException();
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        boolean disableKeyguardForgetPassword = setDisableKeyguardForgetPassword(_arg04, _arg1);
                        reply.writeNoException();
                        reply.writeInt(disableKeyguardForgetPassword ? 1 : 0);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        boolean isDisableKeyguardForgetPassword = isDisableKeyguardForgetPassword(_arg04);
                        reply.writeNoException();
                        reply.writeInt(isDisableKeyguardForgetPassword ? 1 : 0);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        setKeyguardPolicy(_arg04, data.createIntArray());
                        reply.writeNoException();
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                        }
                        int[] _result4 = getKeyguardPolicy(_arg04);
                        reply.writeNoException();
                        reply.writeIntArray(_result4);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        boolean defaultInputMethod = setDefaultInputMethod(data.readString());
                        reply.writeNoException();
                        reply.writeInt(defaultInputMethod ? 1 : 0);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        String _result5 = getDefaultInputMethod();
                        reply.writeNoException();
                        reply.writeString(_result5);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        clearDefaultInputMethod();
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IDeviceControlerManager {
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
            public void shutdownDevice(ComponentName componentName) throws RemoteException {
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
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
            public void rebootDevice(ComponentName componentName) throws RemoteException {
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
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
            public boolean formatSDCard(ComponentName componentName, String diskId) throws RemoteException {
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
                    _data.writeString(diskId);
                    this.mRemote.transact(3, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
            public void enableAccessibilityService(ComponentName admin, ComponentName componentName) throws RemoteException {
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
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
            public void disableAccessibilityService(ComponentName admin, ComponentName componentName) throws RemoteException {
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
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
            public boolean isAccessibilityServiceEnabled(ComponentName admin) throws RemoteException {
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
                    this.mRemote.transact(6, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
            public List<ComponentName> getAccessibilityService(ComponentName admin) throws RemoteException {
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
                    return _reply.createTypedArrayList(ComponentName.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
            public void addAccessibilityServiceToWhiteList(List<String> pkgList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(pkgList);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
            public void removeAccessibilityServiceFromWhiteList(List<String> pkgList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(pkgList);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
            public List<String> getAccessibilityServiceWhiteList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
            public void deleteAccessibilityServiceWhiteList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
            public boolean setSysTime(ComponentName admin, long millis) throws RemoteException {
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
                    _data.writeLong(millis);
                    this.mRemote.transact(12, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
            public boolean setDefaultLauncher(ComponentName admin, ComponentName home) throws RemoteException {
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
                    if (home != null) {
                        _data.writeInt(1);
                        home.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(13, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
            public String getDefaultLauncher() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
            public void clearDefaultLauncher(ComponentName admin) throws RemoteException {
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
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
            public boolean wipeDeviceData() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(16, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
            public void setCustomSettingsMenu(ComponentName admin, List<String> deleteMenus) throws RemoteException {
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
                    _data.writeStringList(deleteMenus);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
            public void setAirplaneMode(ComponentName admin, boolean on) throws RemoteException {
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
                    _data.writeInt(on ? 1 : 0);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
            public boolean getAirplaneMode(ComponentName admin) throws RemoteException {
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
            public void getCertInstaller() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
            public boolean setDisableKeyguardForgetPassword(ComponentName cn, boolean disable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (cn != null) {
                        _data.writeInt(1);
                        cn.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(disable ? 1 : 0);
                    this.mRemote.transact(21, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
            public boolean isDisableKeyguardForgetPassword(ComponentName cn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (cn != null) {
                        _data.writeInt(1);
                        cn.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(22, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
            public void setKeyguardPolicy(ComponentName cn, int[] policies) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (cn != null) {
                        _data.writeInt(1);
                        cn.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeIntArray(policies);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
            public int[] getKeyguardPolicy(ComponentName cn) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (cn != null) {
                        _data.writeInt(1);
                        cn.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createIntArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
            public boolean setDefaultInputMethod(String methodId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(methodId);
                    boolean _result = false;
                    this.mRemote.transact(25, _data, _reply, 0);
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

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
            public String getDefaultInputMethod() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.oppo.enterprise.mdmcoreservice.aidl.IDeviceControlerManager
            public void clearDefaultInputMethod() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
