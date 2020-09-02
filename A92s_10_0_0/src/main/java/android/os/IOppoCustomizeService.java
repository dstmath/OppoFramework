package android.os;

import android.content.ComponentName;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import java.util.List;

public interface IOppoCustomizeService extends IInterface {
    void addDisallowUninstallApps(List<String> list) throws RemoteException;

    void addDisallowedRunningApp(List<String> list) throws RemoteException;

    void addDisallowedUninstallPackages(List<String> list) throws RemoteException;

    void addInstallPackageBlacklist(int i, List<String> list) throws RemoteException;

    void addInstallPackageWhitelist(int i, List<String> list) throws RemoteException;

    void addNetworkRestriction(int i, List<String> list) throws RemoteException;

    void addProtectApplication(String str) throws RemoteException;

    void allowDrawOverlays(String str) throws RemoteException;

    void allowGetUsageStats(String str) throws RemoteException;

    Bitmap captureFullScreen() throws RemoteException;

    void clearAppData(String str) throws RemoteException;

    void deviceReboot() throws RemoteException;

    void deviceShutDown() throws RemoteException;

    void disableInstallSource() throws RemoteException;

    void enableInstallSource() throws RemoteException;

    String executeShellToSetIptables(String str) throws RemoteException;

    List<String> getAppInstallationPolicies(int i) throws RemoteException;

    List<String> getAppRuntimeExceptionInfo() throws RemoteException;

    List<String> getAppUninstallationPolicies(int i) throws RemoteException;

    List<String> getClearAppName() throws RemoteException;

    List<String> getDisallowUninstallPackageList() throws RemoteException;

    List<String> getProtectApplicationList() throws RemoteException;

    List<String> getallDisallowUninstallApps() throws RemoteException;

    boolean isDeviceRoot() throws RemoteException;

    boolean isStatusBarExpandPanelDisabled() throws RemoteException;

    void killAppProcess(String str) throws RemoteException;

    String[] listImei() throws RemoteException;

    void openCloseGps(boolean z) throws RemoteException;

    void openCloseNFC(boolean z) throws RemoteException;

    void removeAllDisallowedUninstallPackages() throws RemoteException;

    void removeDisallowUninstallApps(List<String> list) throws RemoteException;

    void removeDisallowedUninstallPackages(List<String> list) throws RemoteException;

    void removeNetworkRestriction(int i, List<String> list) throws RemoteException;

    void removeNetworkRestrictionAll(int i) throws RemoteException;

    void removeProtectApplication(String str) throws RemoteException;

    void resetFactory() throws RemoteException;

    void setAccessibilityEnabled(ComponentName componentName, boolean z) throws RemoteException;

    void setAirplaneMode(boolean z) throws RemoteException;

    void setAppInstallationPolicies(int i, List<String> list) throws RemoteException;

    void setAppUninstallationPolicies(int i, List<String> list) throws RemoteException;

    void setDB(String str, int i) throws RemoteException;

    void setDataEnabled(boolean z) throws RemoteException;

    void setDevelopmentEnabled(boolean z) throws RemoteException;

    boolean setDeviceOwner(ComponentName componentName) throws RemoteException;

    void setEmmAdmin(ComponentName componentName, boolean z) throws RemoteException;

    void setNetworkRestriction(int i) throws RemoteException;

    void setProp(String str, String str2) throws RemoteException;

    void setSDCardFormatted() throws RemoteException;

    void setSettingsRestriction(String str, boolean z) throws RemoteException;

    void setStatusBarExpandPanelDisabled(boolean z) throws RemoteException;

    void updateConfiguration(Configuration configuration) throws RemoteException;

    public static class Default implements IOppoCustomizeService {
        @Override // android.os.IOppoCustomizeService
        public void deviceReboot() throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void deviceShutDown() throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void setSDCardFormatted() throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public boolean isDeviceRoot() throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoCustomizeService
        public void clearAppData(String packageName) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public List<String> getClearAppName() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public void setProp(String prop, String value) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void setDB(String key, int value) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void setSettingsRestriction(String key, boolean value) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void openCloseGps(boolean enable) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void openCloseNFC(boolean enable) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void setEmmAdmin(ComponentName cn2, boolean enable) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void setAccessibilityEnabled(ComponentName cn2, boolean enable) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void setDataEnabled(boolean enable) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public boolean setDeviceOwner(ComponentName cn2) throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoCustomizeService
        public void killAppProcess(String packageName) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void addProtectApplication(String packageName) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void removeProtectApplication(String packageName) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public List<String> getProtectApplicationList() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public Bitmap captureFullScreen() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public void resetFactory() throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void allowGetUsageStats(String packageName) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void allowDrawOverlays(String packageName) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void updateConfiguration(Configuration config) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void setAirplaneMode(boolean enable) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void setDevelopmentEnabled(boolean enable) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void addDisallowedUninstallPackages(List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void removeDisallowedUninstallPackages(List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void removeAllDisallowedUninstallPackages() throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public List<String> getDisallowUninstallPackageList() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public void disableInstallSource() throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void enableInstallSource() throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void addDisallowedRunningApp(List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void addInstallPackageBlacklist(int pattern, List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void addInstallPackageWhitelist(int pattern, List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void setAppInstallationPolicies(int mode, List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public List<String> getAppInstallationPolicies(int mode) throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public void setAppUninstallationPolicies(int mode, List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public List<String> getAppUninstallationPolicies(int mode) throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public List<String> getAppRuntimeExceptionInfo() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public String[] listImei() throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public String executeShellToSetIptables(String commandline) throws RemoteException {
            return null;
        }

        @Override // android.os.IOppoCustomizeService
        public void setNetworkRestriction(int pattern) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void addNetworkRestriction(int pattern, List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void removeNetworkRestriction(int pattern, List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void removeNetworkRestrictionAll(int pattern) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void setStatusBarExpandPanelDisabled(boolean disable) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public boolean isStatusBarExpandPanelDisabled() throws RemoteException {
            return false;
        }

        @Override // android.os.IOppoCustomizeService
        public void addDisallowUninstallApps(List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public void removeDisallowUninstallApps(List<String> list) throws RemoteException {
        }

        @Override // android.os.IOppoCustomizeService
        public List<String> getallDisallowUninstallApps() throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOppoCustomizeService {
        private static final String DESCRIPTOR = "android.os.IOppoCustomizeService";
        static final int TRANSACTION_addDisallowUninstallApps = 49;
        static final int TRANSACTION_addDisallowedRunningApp = 33;
        static final int TRANSACTION_addDisallowedUninstallPackages = 27;
        static final int TRANSACTION_addInstallPackageBlacklist = 34;
        static final int TRANSACTION_addInstallPackageWhitelist = 35;
        static final int TRANSACTION_addNetworkRestriction = 44;
        static final int TRANSACTION_addProtectApplication = 17;
        static final int TRANSACTION_allowDrawOverlays = 23;
        static final int TRANSACTION_allowGetUsageStats = 22;
        static final int TRANSACTION_captureFullScreen = 20;
        static final int TRANSACTION_clearAppData = 5;
        static final int TRANSACTION_deviceReboot = 1;
        static final int TRANSACTION_deviceShutDown = 2;
        static final int TRANSACTION_disableInstallSource = 31;
        static final int TRANSACTION_enableInstallSource = 32;
        static final int TRANSACTION_executeShellToSetIptables = 42;
        static final int TRANSACTION_getAppInstallationPolicies = 37;
        static final int TRANSACTION_getAppRuntimeExceptionInfo = 40;
        static final int TRANSACTION_getAppUninstallationPolicies = 39;
        static final int TRANSACTION_getClearAppName = 6;
        static final int TRANSACTION_getDisallowUninstallPackageList = 30;
        static final int TRANSACTION_getProtectApplicationList = 19;
        static final int TRANSACTION_getallDisallowUninstallApps = 51;
        static final int TRANSACTION_isDeviceRoot = 4;
        static final int TRANSACTION_isStatusBarExpandPanelDisabled = 48;
        static final int TRANSACTION_killAppProcess = 16;
        static final int TRANSACTION_listImei = 41;
        static final int TRANSACTION_openCloseGps = 10;
        static final int TRANSACTION_openCloseNFC = 11;
        static final int TRANSACTION_removeAllDisallowedUninstallPackages = 29;
        static final int TRANSACTION_removeDisallowUninstallApps = 50;
        static final int TRANSACTION_removeDisallowedUninstallPackages = 28;
        static final int TRANSACTION_removeNetworkRestriction = 45;
        static final int TRANSACTION_removeNetworkRestrictionAll = 46;
        static final int TRANSACTION_removeProtectApplication = 18;
        static final int TRANSACTION_resetFactory = 21;
        static final int TRANSACTION_setAccessibilityEnabled = 13;
        static final int TRANSACTION_setAirplaneMode = 25;
        static final int TRANSACTION_setAppInstallationPolicies = 36;
        static final int TRANSACTION_setAppUninstallationPolicies = 38;
        static final int TRANSACTION_setDB = 8;
        static final int TRANSACTION_setDataEnabled = 14;
        static final int TRANSACTION_setDevelopmentEnabled = 26;
        static final int TRANSACTION_setDeviceOwner = 15;
        static final int TRANSACTION_setEmmAdmin = 12;
        static final int TRANSACTION_setNetworkRestriction = 43;
        static final int TRANSACTION_setProp = 7;
        static final int TRANSACTION_setSDCardFormatted = 3;
        static final int TRANSACTION_setSettingsRestriction = 9;
        static final int TRANSACTION_setStatusBarExpandPanelDisabled = 47;
        static final int TRANSACTION_updateConfiguration = 24;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOppoCustomizeService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOppoCustomizeService)) {
                return new Proxy(obj);
            }
            return (IOppoCustomizeService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "deviceReboot";
                case 2:
                    return "deviceShutDown";
                case 3:
                    return "setSDCardFormatted";
                case 4:
                    return "isDeviceRoot";
                case 5:
                    return "clearAppData";
                case 6:
                    return "getClearAppName";
                case 7:
                    return "setProp";
                case 8:
                    return "setDB";
                case 9:
                    return "setSettingsRestriction";
                case 10:
                    return "openCloseGps";
                case 11:
                    return "openCloseNFC";
                case 12:
                    return "setEmmAdmin";
                case 13:
                    return "setAccessibilityEnabled";
                case 14:
                    return "setDataEnabled";
                case 15:
                    return "setDeviceOwner";
                case 16:
                    return "killAppProcess";
                case 17:
                    return "addProtectApplication";
                case 18:
                    return "removeProtectApplication";
                case 19:
                    return "getProtectApplicationList";
                case 20:
                    return "captureFullScreen";
                case 21:
                    return "resetFactory";
                case 22:
                    return "allowGetUsageStats";
                case 23:
                    return "allowDrawOverlays";
                case 24:
                    return "updateConfiguration";
                case 25:
                    return "setAirplaneMode";
                case 26:
                    return "setDevelopmentEnabled";
                case 27:
                    return "addDisallowedUninstallPackages";
                case 28:
                    return "removeDisallowedUninstallPackages";
                case 29:
                    return "removeAllDisallowedUninstallPackages";
                case 30:
                    return "getDisallowUninstallPackageList";
                case 31:
                    return "disableInstallSource";
                case 32:
                    return "enableInstallSource";
                case 33:
                    return "addDisallowedRunningApp";
                case 34:
                    return "addInstallPackageBlacklist";
                case 35:
                    return "addInstallPackageWhitelist";
                case 36:
                    return "setAppInstallationPolicies";
                case 37:
                    return "getAppInstallationPolicies";
                case 38:
                    return "setAppUninstallationPolicies";
                case 39:
                    return "getAppUninstallationPolicies";
                case 40:
                    return "getAppRuntimeExceptionInfo";
                case 41:
                    return "listImei";
                case 42:
                    return "executeShellToSetIptables";
                case 43:
                    return "setNetworkRestriction";
                case 44:
                    return "addNetworkRestriction";
                case 45:
                    return "removeNetworkRestriction";
                case 46:
                    return "removeNetworkRestrictionAll";
                case 47:
                    return "setStatusBarExpandPanelDisabled";
                case 48:
                    return "isStatusBarExpandPanelDisabled";
                case 49:
                    return "addDisallowUninstallApps";
                case 50:
                    return "removeDisallowUninstallApps";
                case 51:
                    return "getallDisallowUninstallApps";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ComponentName _arg0;
            ComponentName _arg02;
            ComponentName _arg03;
            Configuration _arg04;
            if (code != 1598968902) {
                boolean _arg1 = false;
                boolean _arg05 = false;
                boolean _arg06 = false;
                boolean _arg07 = false;
                boolean _arg08 = false;
                boolean _arg12 = false;
                boolean _arg13 = false;
                boolean _arg09 = false;
                boolean _arg010 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        deviceReboot();
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        deviceShutDown();
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        setSDCardFormatted();
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isDeviceRoot = isDeviceRoot();
                        reply.writeNoException();
                        reply.writeInt(isDeviceRoot ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        clearAppData(data.readString());
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result = getClearAppName();
                        reply.writeNoException();
                        reply.writeStringList(_result);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        setProp(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        setDB(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg011 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = true;
                        }
                        setSettingsRestriction(_arg011, _arg1);
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg010 = true;
                        }
                        openCloseGps(_arg010);
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg09 = true;
                        }
                        openCloseNFC(_arg09);
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg13 = true;
                        }
                        setEmmAdmin(_arg0, _arg13);
                        reply.writeNoException();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg12 = true;
                        }
                        setAccessibilityEnabled(_arg02, _arg12);
                        reply.writeNoException();
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = true;
                        }
                        setDataEnabled(_arg08);
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = ComponentName.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        boolean deviceOwner = setDeviceOwner(_arg03);
                        reply.writeNoException();
                        reply.writeInt(deviceOwner ? 1 : 0);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        killAppProcess(data.readString());
                        reply.writeNoException();
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        addProtectApplication(data.readString());
                        reply.writeNoException();
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        removeProtectApplication(data.readString());
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result2 = getProtectApplicationList();
                        reply.writeNoException();
                        reply.writeStringList(_result2);
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        Bitmap _result3 = captureFullScreen();
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        resetFactory();
                        reply.writeNoException();
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        allowGetUsageStats(data.readString());
                        reply.writeNoException();
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        allowDrawOverlays(data.readString());
                        reply.writeNoException();
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = Configuration.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        updateConfiguration(_arg04);
                        reply.writeNoException();
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = true;
                        }
                        setAirplaneMode(_arg07);
                        reply.writeNoException();
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = true;
                        }
                        setDevelopmentEnabled(_arg06);
                        reply.writeNoException();
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        addDisallowedUninstallPackages(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        removeDisallowedUninstallPackages(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        removeAllDisallowedUninstallPackages();
                        reply.writeNoException();
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result4 = getDisallowUninstallPackageList();
                        reply.writeNoException();
                        reply.writeStringList(_result4);
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        disableInstallSource();
                        reply.writeNoException();
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        enableInstallSource();
                        reply.writeNoException();
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        addDisallowedRunningApp(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        addInstallPackageBlacklist(data.readInt(), data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        addInstallPackageWhitelist(data.readInt(), data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        setAppInstallationPolicies(data.readInt(), data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result5 = getAppInstallationPolicies(data.readInt());
                        reply.writeNoException();
                        reply.writeStringList(_result5);
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        setAppUninstallationPolicies(data.readInt(), data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result6 = getAppUninstallationPolicies(data.readInt());
                        reply.writeNoException();
                        reply.writeStringList(_result6);
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result7 = getAppRuntimeExceptionInfo();
                        reply.writeNoException();
                        reply.writeStringList(_result7);
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result8 = listImei();
                        reply.writeNoException();
                        reply.writeStringArray(_result8);
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        String _result9 = executeShellToSetIptables(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result9);
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        setNetworkRestriction(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        addNetworkRestriction(data.readInt(), data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        removeNetworkRestriction(data.readInt(), data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 46:
                        data.enforceInterface(DESCRIPTOR);
                        removeNetworkRestrictionAll(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 47:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = true;
                        }
                        setStatusBarExpandPanelDisabled(_arg05);
                        reply.writeNoException();
                        return true;
                    case 48:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isStatusBarExpandPanelDisabled = isStatusBarExpandPanelDisabled();
                        reply.writeNoException();
                        reply.writeInt(isStatusBarExpandPanelDisabled ? 1 : 0);
                        return true;
                    case 49:
                        data.enforceInterface(DESCRIPTOR);
                        addDisallowUninstallApps(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 50:
                        data.enforceInterface(DESCRIPTOR);
                        removeDisallowUninstallApps(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 51:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result10 = getallDisallowUninstallApps();
                        reply.writeNoException();
                        reply.writeStringList(_result10);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IOppoCustomizeService {
            public static IOppoCustomizeService sDefaultImpl;
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

            @Override // android.os.IOppoCustomizeService
            public void deviceReboot() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().deviceReboot();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void deviceShutDown() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().deviceShutDown();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void setSDCardFormatted() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setSDCardFormatted();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public boolean isDeviceRoot() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isDeviceRoot();
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

            @Override // android.os.IOppoCustomizeService
            public void clearAppData(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().clearAppData(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public List<String> getClearAppName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getClearAppName();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void setProp(String prop, String value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(prop);
                    _data.writeString(value);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setProp(prop, value);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void setDB(String key, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeInt(value);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDB(key, value);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void setSettingsRestriction(String key, boolean value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeInt(value ? 1 : 0);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setSettingsRestriction(key, value);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void openCloseGps(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().openCloseGps(enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void openCloseNFC(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().openCloseNFC(enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void setEmmAdmin(ComponentName cn2, boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 1;
                    if (cn2 != null) {
                        _data.writeInt(1);
                        cn2.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!enable) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(12, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setEmmAdmin(cn2, enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void setAccessibilityEnabled(ComponentName cn2, boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 1;
                    if (cn2 != null) {
                        _data.writeInt(1);
                        cn2.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!enable) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(13, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAccessibilityEnabled(cn2, enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void setDataEnabled(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDataEnabled(enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public boolean setDeviceOwner(ComponentName cn2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (cn2 != null) {
                        _data.writeInt(1);
                        cn2.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setDeviceOwner(cn2);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void killAppProcess(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().killAppProcess(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void addProtectApplication(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(17, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addProtectApplication(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void removeProtectApplication(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(18, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeProtectApplication(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public List<String> getProtectApplicationList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getProtectApplicationList();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public Bitmap captureFullScreen() throws RemoteException {
                Bitmap _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().captureFullScreen();
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bitmap.CREATOR.createFromParcel(_reply);
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

            @Override // android.os.IOppoCustomizeService
            public void resetFactory() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(21, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resetFactory();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void allowGetUsageStats(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(22, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().allowGetUsageStats(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void allowDrawOverlays(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(23, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().allowDrawOverlays(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void updateConfiguration(Configuration config) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (config != null) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(24, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateConfiguration(config);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void setAirplaneMode(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(25, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAirplaneMode(enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void setDevelopmentEnabled(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(26, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDevelopmentEnabled(enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void addDisallowedUninstallPackages(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    if (this.mRemote.transact(27, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addDisallowedUninstallPackages(packageNames);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void removeDisallowedUninstallPackages(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    if (this.mRemote.transact(28, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeDisallowedUninstallPackages(packageNames);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void removeAllDisallowedUninstallPackages() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(29, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeAllDisallowedUninstallPackages();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public List<String> getDisallowUninstallPackageList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(30, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDisallowUninstallPackageList();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void disableInstallSource() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(31, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().disableInstallSource();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void enableInstallSource() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(32, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().enableInstallSource();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void addDisallowedRunningApp(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    if (this.mRemote.transact(33, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addDisallowedRunningApp(packageNames);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void addInstallPackageBlacklist(int pattern, List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pattern);
                    _data.writeStringList(packageNames);
                    if (this.mRemote.transact(34, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addInstallPackageBlacklist(pattern, packageNames);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void addInstallPackageWhitelist(int pattern, List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pattern);
                    _data.writeStringList(packageNames);
                    if (this.mRemote.transact(35, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addInstallPackageWhitelist(pattern, packageNames);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void setAppInstallationPolicies(int mode, List<String> appPackageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    _data.writeStringList(appPackageNames);
                    if (this.mRemote.transact(36, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAppInstallationPolicies(mode, appPackageNames);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public List<String> getAppInstallationPolicies(int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    if (!this.mRemote.transact(37, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAppInstallationPolicies(mode);
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void setAppUninstallationPolicies(int mode, List<String> appPackageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    _data.writeStringList(appPackageNames);
                    if (this.mRemote.transact(38, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAppUninstallationPolicies(mode, appPackageNames);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public List<String> getAppUninstallationPolicies(int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    if (!this.mRemote.transact(39, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAppUninstallationPolicies(mode);
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public List<String> getAppRuntimeExceptionInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(40, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAppRuntimeExceptionInfo();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public String[] listImei() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(41, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().listImei();
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

            @Override // android.os.IOppoCustomizeService
            public String executeShellToSetIptables(String commandline) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(commandline);
                    if (!this.mRemote.transact(42, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().executeShellToSetIptables(commandline);
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

            @Override // android.os.IOppoCustomizeService
            public void setNetworkRestriction(int pattern) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pattern);
                    if (this.mRemote.transact(43, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setNetworkRestriction(pattern);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void addNetworkRestriction(int pattern, List<String> list) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pattern);
                    _data.writeStringList(list);
                    if (this.mRemote.transact(44, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addNetworkRestriction(pattern, list);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void removeNetworkRestriction(int pattern, List<String> list) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pattern);
                    _data.writeStringList(list);
                    if (this.mRemote.transact(45, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeNetworkRestriction(pattern, list);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void removeNetworkRestrictionAll(int pattern) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pattern);
                    if (this.mRemote.transact(46, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeNetworkRestrictionAll(pattern);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void setStatusBarExpandPanelDisabled(boolean disable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(disable ? 1 : 0);
                    if (this.mRemote.transact(47, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setStatusBarExpandPanelDisabled(disable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public boolean isStatusBarExpandPanelDisabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(48, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isStatusBarExpandPanelDisabled();
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

            @Override // android.os.IOppoCustomizeService
            public void addDisallowUninstallApps(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    if (this.mRemote.transact(49, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addDisallowUninstallApps(packageNames);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public void removeDisallowUninstallApps(List<String> packageNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageNames);
                    if (this.mRemote.transact(50, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeDisallowUninstallApps(packageNames);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IOppoCustomizeService
            public List<String> getallDisallowUninstallApps() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(51, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getallDisallowUninstallApps();
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IOppoCustomizeService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOppoCustomizeService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
