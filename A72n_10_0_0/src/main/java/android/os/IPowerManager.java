package android.os;

import android.annotation.UnsupportedAppUsage;
import java.util.Map;

public interface IPowerManager extends IInterface {
    void acquireWakeLock(IBinder iBinder, int i, String str, String str2, WorkSource workSource, String str3) throws RemoteException;

    void acquireWakeLockWithUid(IBinder iBinder, int i, String str, String str2, int i2) throws RemoteException;

    void boostScreenBrightness(long j) throws RemoteException;

    void crash(String str) throws RemoteException;

    boolean forceSuspend() throws RemoteException;

    int getCurrentChargeStateForSale() throws RemoteException;

    int getDefaultScreenBrightnessSetting() throws RemoteException;

    boolean getDisplayAodStatus() throws RemoteException;

    long getFrameworksBlockedTime() throws RemoteException;

    int getLastShutdownReason() throws RemoteException;

    int getLastSleepReason() throws RemoteException;

    int getMaximumScreenBrightnessSetting() throws RemoteException;

    int getMinimumScreenBrightnessSetting() throws RemoteException;

    int getPowerSaveModeTrigger() throws RemoteException;

    PowerSaveState getPowerSaveState(int i) throws RemoteException;

    int getScreenState() throws RemoteException;

    Map getTopAppBlocked(int i) throws RemoteException;

    int[] getWakeLockedUids() throws RemoteException;

    @UnsupportedAppUsage
    void goToSleep(long j, int i, int i2) throws RemoteException;

    boolean isDeviceIdleMode() throws RemoteException;

    @UnsupportedAppUsage
    boolean isInteractive() throws RemoteException;

    boolean isLightDeviceIdleMode() throws RemoteException;

    boolean isPowerSaveMode() throws RemoteException;

    boolean isScreenBrightnessBoosted() throws RemoteException;

    boolean isWakeLockLevelSupported(int i) throws RemoteException;

    void nap(long j) throws RemoteException;

    void powerHint(int i, int i2) throws RemoteException;

    @UnsupportedAppUsage
    void reboot(boolean z, String str, boolean z2) throws RemoteException;

    void rebootSafeMode(boolean z, boolean z2) throws RemoteException;

    @UnsupportedAppUsage
    void releaseWakeLock(IBinder iBinder, int i) throws RemoteException;

    void resumeChargeForSale() throws RemoteException;

    boolean setAdaptivePowerSaveEnabled(boolean z) throws RemoteException;

    boolean setAdaptivePowerSavePolicy(BatterySaverPolicyConfig batterySaverPolicyConfig) throws RemoteException;

    void setAttentionLight(boolean z, int i) throws RemoteException;

    void setDozeAfterScreenOff(boolean z) throws RemoteException;

    void setDozeOverride(int i, int i2) throws RemoteException;

    boolean setDynamicPowerSaveHint(boolean z, int i) throws RemoteException;

    boolean setPowerSaveModeEnabled(boolean z) throws RemoteException;

    void setStayOnSetting(int i) throws RemoteException;

    void shutdown(boolean z, String str, boolean z2) throws RemoteException;

    void stopChargeForSale() throws RemoteException;

    void updateWakeLockUids(IBinder iBinder, int[] iArr) throws RemoteException;

    void updateWakeLockWorkSource(IBinder iBinder, WorkSource workSource, String str) throws RemoteException;

    @UnsupportedAppUsage
    void userActivity(long j, int i, int i2) throws RemoteException;

    void wakeUp(long j, int i, String str, String str2) throws RemoteException;

    public static class Default implements IPowerManager {
        @Override // android.os.IPowerManager
        public void acquireWakeLock(IBinder lock, int flags, String tag, String packageName, WorkSource ws, String historyTag) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public void acquireWakeLockWithUid(IBinder lock, int flags, String tag, String packageName, int uidtoblame) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public void releaseWakeLock(IBinder lock, int flags) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public void updateWakeLockUids(IBinder lock, int[] uids) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public void powerHint(int hintId, int data) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public void updateWakeLockWorkSource(IBinder lock, WorkSource ws, String historyTag) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public boolean isWakeLockLevelSupported(int level) throws RemoteException {
            return false;
        }

        @Override // android.os.IPowerManager
        public void userActivity(long time, int event, int flags) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public void wakeUp(long time, int reason, String details, String opPackageName) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public void goToSleep(long time, int reason, int flags) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public void nap(long time) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public boolean isInteractive() throws RemoteException {
            return false;
        }

        @Override // android.os.IPowerManager
        public boolean isPowerSaveMode() throws RemoteException {
            return false;
        }

        @Override // android.os.IPowerManager
        public PowerSaveState getPowerSaveState(int serviceType) throws RemoteException {
            return null;
        }

        @Override // android.os.IPowerManager
        public boolean setPowerSaveModeEnabled(boolean mode) throws RemoteException {
            return false;
        }

        @Override // android.os.IPowerManager
        public boolean setDynamicPowerSaveHint(boolean powerSaveHint, int disableThreshold) throws RemoteException {
            return false;
        }

        @Override // android.os.IPowerManager
        public boolean setAdaptivePowerSavePolicy(BatterySaverPolicyConfig config) throws RemoteException {
            return false;
        }

        @Override // android.os.IPowerManager
        public boolean setAdaptivePowerSaveEnabled(boolean enabled) throws RemoteException {
            return false;
        }

        @Override // android.os.IPowerManager
        public int getPowerSaveModeTrigger() throws RemoteException {
            return 0;
        }

        @Override // android.os.IPowerManager
        public boolean isDeviceIdleMode() throws RemoteException {
            return false;
        }

        @Override // android.os.IPowerManager
        public boolean isLightDeviceIdleMode() throws RemoteException {
            return false;
        }

        @Override // android.os.IPowerManager
        public void reboot(boolean confirm, String reason, boolean wait) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public void rebootSafeMode(boolean confirm, boolean wait) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public void shutdown(boolean confirm, String reason, boolean wait) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public void crash(String message) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public int getLastShutdownReason() throws RemoteException {
            return 0;
        }

        @Override // android.os.IPowerManager
        public int getLastSleepReason() throws RemoteException {
            return 0;
        }

        @Override // android.os.IPowerManager
        public void setStayOnSetting(int val) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public void boostScreenBrightness(long time) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public int getScreenState() throws RemoteException {
            return 0;
        }

        @Override // android.os.IPowerManager
        public long getFrameworksBlockedTime() throws RemoteException {
            return 0;
        }

        @Override // android.os.IPowerManager
        public Map getTopAppBlocked(int n) throws RemoteException {
            return null;
        }

        @Override // android.os.IPowerManager
        public void stopChargeForSale() throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public void resumeChargeForSale() throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public int getCurrentChargeStateForSale() throws RemoteException {
            return 0;
        }

        @Override // android.os.IPowerManager
        public void setDozeOverride(int screenState, int screenBrightness) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public boolean getDisplayAodStatus() throws RemoteException {
            return false;
        }

        @Override // android.os.IPowerManager
        public int getMinimumScreenBrightnessSetting() throws RemoteException {
            return 0;
        }

        @Override // android.os.IPowerManager
        public int getMaximumScreenBrightnessSetting() throws RemoteException {
            return 0;
        }

        @Override // android.os.IPowerManager
        public int getDefaultScreenBrightnessSetting() throws RemoteException {
            return 0;
        }

        @Override // android.os.IPowerManager
        public boolean isScreenBrightnessBoosted() throws RemoteException {
            return false;
        }

        @Override // android.os.IPowerManager
        public void setAttentionLight(boolean on, int color) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public void setDozeAfterScreenOff(boolean on) throws RemoteException {
        }

        @Override // android.os.IPowerManager
        public boolean forceSuspend() throws RemoteException {
            return false;
        }

        @Override // android.os.IPowerManager
        public int[] getWakeLockedUids() throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IPowerManager {
        private static final String DESCRIPTOR = "android.os.IPowerManager";
        static final int TRANSACTION_acquireWakeLock = 1;
        static final int TRANSACTION_acquireWakeLockWithUid = 2;
        static final int TRANSACTION_boostScreenBrightness = 29;
        static final int TRANSACTION_crash = 25;
        static final int TRANSACTION_forceSuspend = 44;
        static final int TRANSACTION_getCurrentChargeStateForSale = 35;
        static final int TRANSACTION_getDefaultScreenBrightnessSetting = 40;
        static final int TRANSACTION_getDisplayAodStatus = 37;
        static final int TRANSACTION_getFrameworksBlockedTime = 31;
        static final int TRANSACTION_getLastShutdownReason = 26;
        static final int TRANSACTION_getLastSleepReason = 27;
        static final int TRANSACTION_getMaximumScreenBrightnessSetting = 39;
        static final int TRANSACTION_getMinimumScreenBrightnessSetting = 38;
        static final int TRANSACTION_getPowerSaveModeTrigger = 19;
        static final int TRANSACTION_getPowerSaveState = 14;
        static final int TRANSACTION_getScreenState = 30;
        static final int TRANSACTION_getTopAppBlocked = 32;
        static final int TRANSACTION_getWakeLockedUids = 45;
        static final int TRANSACTION_goToSleep = 10;
        static final int TRANSACTION_isDeviceIdleMode = 20;
        static final int TRANSACTION_isInteractive = 12;
        static final int TRANSACTION_isLightDeviceIdleMode = 21;
        static final int TRANSACTION_isPowerSaveMode = 13;
        static final int TRANSACTION_isScreenBrightnessBoosted = 41;
        static final int TRANSACTION_isWakeLockLevelSupported = 7;
        static final int TRANSACTION_nap = 11;
        static final int TRANSACTION_powerHint = 5;
        static final int TRANSACTION_reboot = 22;
        static final int TRANSACTION_rebootSafeMode = 23;
        static final int TRANSACTION_releaseWakeLock = 3;
        static final int TRANSACTION_resumeChargeForSale = 34;
        static final int TRANSACTION_setAdaptivePowerSaveEnabled = 18;
        static final int TRANSACTION_setAdaptivePowerSavePolicy = 17;
        static final int TRANSACTION_setAttentionLight = 42;
        static final int TRANSACTION_setDozeAfterScreenOff = 43;
        static final int TRANSACTION_setDozeOverride = 36;
        static final int TRANSACTION_setDynamicPowerSaveHint = 16;
        static final int TRANSACTION_setPowerSaveModeEnabled = 15;
        static final int TRANSACTION_setStayOnSetting = 28;
        static final int TRANSACTION_shutdown = 24;
        static final int TRANSACTION_stopChargeForSale = 33;
        static final int TRANSACTION_updateWakeLockUids = 4;
        static final int TRANSACTION_updateWakeLockWorkSource = 6;
        static final int TRANSACTION_userActivity = 8;
        static final int TRANSACTION_wakeUp = 9;

        public Stub() {
            attachInterface(this, "android.os.IPowerManager");
        }

        public static IPowerManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("android.os.IPowerManager");
            if (iin == null || !(iin instanceof IPowerManager)) {
                return new Proxy(obj);
            }
            return (IPowerManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "acquireWakeLock";
                case 2:
                    return "acquireWakeLockWithUid";
                case 3:
                    return "releaseWakeLock";
                case 4:
                    return "updateWakeLockUids";
                case 5:
                    return "powerHint";
                case 6:
                    return "updateWakeLockWorkSource";
                case 7:
                    return "isWakeLockLevelSupported";
                case 8:
                    return "userActivity";
                case 9:
                    return "wakeUp";
                case 10:
                    return "goToSleep";
                case 11:
                    return "nap";
                case 12:
                    return "isInteractive";
                case 13:
                    return "isPowerSaveMode";
                case 14:
                    return "getPowerSaveState";
                case 15:
                    return "setPowerSaveModeEnabled";
                case 16:
                    return "setDynamicPowerSaveHint";
                case 17:
                    return "setAdaptivePowerSavePolicy";
                case 18:
                    return "setAdaptivePowerSaveEnabled";
                case 19:
                    return "getPowerSaveModeTrigger";
                case 20:
                    return "isDeviceIdleMode";
                case 21:
                    return "isLightDeviceIdleMode";
                case 22:
                    return "reboot";
                case 23:
                    return "rebootSafeMode";
                case 24:
                    return "shutdown";
                case 25:
                    return OppoManager.ISSUE_ANDROID_CRASH;
                case 26:
                    return "getLastShutdownReason";
                case 27:
                    return "getLastSleepReason";
                case 28:
                    return "setStayOnSetting";
                case 29:
                    return "boostScreenBrightness";
                case 30:
                    return "getScreenState";
                case 31:
                    return "getFrameworksBlockedTime";
                case 32:
                    return "getTopAppBlocked";
                case 33:
                    return "stopChargeForSale";
                case 34:
                    return "resumeChargeForSale";
                case 35:
                    return "getCurrentChargeStateForSale";
                case 36:
                    return "setDozeOverride";
                case 37:
                    return "getDisplayAodStatus";
                case 38:
                    return "getMinimumScreenBrightnessSetting";
                case 39:
                    return "getMaximumScreenBrightnessSetting";
                case 40:
                    return "getDefaultScreenBrightnessSetting";
                case 41:
                    return "isScreenBrightnessBoosted";
                case 42:
                    return "setAttentionLight";
                case 43:
                    return "setDozeAfterScreenOff";
                case 44:
                    return "forceSuspend";
                case 45:
                    return "getWakeLockedUids";
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
            WorkSource _arg4;
            WorkSource _arg1;
            BatterySaverPolicyConfig _arg0;
            if (code != 1598968902) {
                boolean _arg02 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface("android.os.IPowerManager");
                        IBinder _arg03 = data.readStrongBinder();
                        int _arg12 = data.readInt();
                        String _arg2 = data.readString();
                        String _arg3 = data.readString();
                        if (data.readInt() != 0) {
                            _arg4 = WorkSource.CREATOR.createFromParcel(data);
                        } else {
                            _arg4 = null;
                        }
                        acquireWakeLock(_arg03, _arg12, _arg2, _arg3, _arg4, data.readString());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface("android.os.IPowerManager");
                        acquireWakeLockWithUid(data.readStrongBinder(), data.readInt(), data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface("android.os.IPowerManager");
                        releaseWakeLock(data.readStrongBinder(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface("android.os.IPowerManager");
                        updateWakeLockUids(data.readStrongBinder(), data.createIntArray());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface("android.os.IPowerManager");
                        powerHint(data.readInt(), data.readInt());
                        return true;
                    case 6:
                        data.enforceInterface("android.os.IPowerManager");
                        IBinder _arg04 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg1 = WorkSource.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        updateWakeLockWorkSource(_arg04, _arg1, data.readString());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface("android.os.IPowerManager");
                        boolean isWakeLockLevelSupported = isWakeLockLevelSupported(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isWakeLockLevelSupported ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface("android.os.IPowerManager");
                        userActivity(data.readLong(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface("android.os.IPowerManager");
                        wakeUp(data.readLong(), data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface("android.os.IPowerManager");
                        goToSleep(data.readLong(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface("android.os.IPowerManager");
                        nap(data.readLong());
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface("android.os.IPowerManager");
                        boolean isInteractive = isInteractive();
                        reply.writeNoException();
                        reply.writeInt(isInteractive ? 1 : 0);
                        return true;
                    case 13:
                        data.enforceInterface("android.os.IPowerManager");
                        boolean isPowerSaveMode = isPowerSaveMode();
                        reply.writeNoException();
                        reply.writeInt(isPowerSaveMode ? 1 : 0);
                        return true;
                    case 14:
                        data.enforceInterface("android.os.IPowerManager");
                        PowerSaveState _result = getPowerSaveState(data.readInt());
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 15:
                        data.enforceInterface("android.os.IPowerManager");
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        boolean powerSaveModeEnabled = setPowerSaveModeEnabled(_arg02);
                        reply.writeNoException();
                        reply.writeInt(powerSaveModeEnabled ? 1 : 0);
                        return true;
                    case 16:
                        data.enforceInterface("android.os.IPowerManager");
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        boolean dynamicPowerSaveHint = setDynamicPowerSaveHint(_arg02, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(dynamicPowerSaveHint ? 1 : 0);
                        return true;
                    case 17:
                        data.enforceInterface("android.os.IPowerManager");
                        if (data.readInt() != 0) {
                            _arg0 = BatterySaverPolicyConfig.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        boolean adaptivePowerSavePolicy = setAdaptivePowerSavePolicy(_arg0);
                        reply.writeNoException();
                        reply.writeInt(adaptivePowerSavePolicy ? 1 : 0);
                        return true;
                    case 18:
                        data.enforceInterface("android.os.IPowerManager");
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        boolean adaptivePowerSaveEnabled = setAdaptivePowerSaveEnabled(_arg02);
                        reply.writeNoException();
                        reply.writeInt(adaptivePowerSaveEnabled ? 1 : 0);
                        return true;
                    case 19:
                        data.enforceInterface("android.os.IPowerManager");
                        int _result2 = getPowerSaveModeTrigger();
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 20:
                        data.enforceInterface("android.os.IPowerManager");
                        boolean isDeviceIdleMode = isDeviceIdleMode();
                        reply.writeNoException();
                        reply.writeInt(isDeviceIdleMode ? 1 : 0);
                        return true;
                    case 21:
                        data.enforceInterface("android.os.IPowerManager");
                        boolean isLightDeviceIdleMode = isLightDeviceIdleMode();
                        reply.writeNoException();
                        reply.writeInt(isLightDeviceIdleMode ? 1 : 0);
                        return true;
                    case 22:
                        data.enforceInterface("android.os.IPowerManager");
                        boolean _arg05 = data.readInt() != 0;
                        String _arg13 = data.readString();
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        reboot(_arg05, _arg13, _arg02);
                        reply.writeNoException();
                        return true;
                    case 23:
                        data.enforceInterface("android.os.IPowerManager");
                        boolean _arg06 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        rebootSafeMode(_arg06, _arg02);
                        reply.writeNoException();
                        return true;
                    case 24:
                        data.enforceInterface("android.os.IPowerManager");
                        boolean _arg07 = data.readInt() != 0;
                        String _arg14 = data.readString();
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        shutdown(_arg07, _arg14, _arg02);
                        reply.writeNoException();
                        return true;
                    case 25:
                        data.enforceInterface("android.os.IPowerManager");
                        crash(data.readString());
                        reply.writeNoException();
                        return true;
                    case 26:
                        data.enforceInterface("android.os.IPowerManager");
                        int _result3 = getLastShutdownReason();
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 27:
                        data.enforceInterface("android.os.IPowerManager");
                        int _result4 = getLastSleepReason();
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 28:
                        data.enforceInterface("android.os.IPowerManager");
                        setStayOnSetting(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 29:
                        data.enforceInterface("android.os.IPowerManager");
                        boostScreenBrightness(data.readLong());
                        reply.writeNoException();
                        return true;
                    case 30:
                        data.enforceInterface("android.os.IPowerManager");
                        int _result5 = getScreenState();
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 31:
                        data.enforceInterface("android.os.IPowerManager");
                        long _result6 = getFrameworksBlockedTime();
                        reply.writeNoException();
                        reply.writeLong(_result6);
                        return true;
                    case 32:
                        data.enforceInterface("android.os.IPowerManager");
                        Map _result7 = getTopAppBlocked(data.readInt());
                        reply.writeNoException();
                        reply.writeMap(_result7);
                        return true;
                    case 33:
                        data.enforceInterface("android.os.IPowerManager");
                        stopChargeForSale();
                        reply.writeNoException();
                        return true;
                    case 34:
                        data.enforceInterface("android.os.IPowerManager");
                        resumeChargeForSale();
                        reply.writeNoException();
                        return true;
                    case 35:
                        data.enforceInterface("android.os.IPowerManager");
                        int _result8 = getCurrentChargeStateForSale();
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 36:
                        data.enforceInterface("android.os.IPowerManager");
                        setDozeOverride(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 37:
                        data.enforceInterface("android.os.IPowerManager");
                        boolean displayAodStatus = getDisplayAodStatus();
                        reply.writeNoException();
                        reply.writeInt(displayAodStatus ? 1 : 0);
                        return true;
                    case 38:
                        data.enforceInterface("android.os.IPowerManager");
                        int _result9 = getMinimumScreenBrightnessSetting();
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    case 39:
                        data.enforceInterface("android.os.IPowerManager");
                        int _result10 = getMaximumScreenBrightnessSetting();
                        reply.writeNoException();
                        reply.writeInt(_result10);
                        return true;
                    case 40:
                        data.enforceInterface("android.os.IPowerManager");
                        int _result11 = getDefaultScreenBrightnessSetting();
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 41:
                        data.enforceInterface("android.os.IPowerManager");
                        boolean isScreenBrightnessBoosted = isScreenBrightnessBoosted();
                        reply.writeNoException();
                        reply.writeInt(isScreenBrightnessBoosted ? 1 : 0);
                        return true;
                    case 42:
                        data.enforceInterface("android.os.IPowerManager");
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        setAttentionLight(_arg02, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 43:
                        data.enforceInterface("android.os.IPowerManager");
                        if (data.readInt() != 0) {
                            _arg02 = true;
                        }
                        setDozeAfterScreenOff(_arg02);
                        reply.writeNoException();
                        return true;
                    case 44:
                        data.enforceInterface("android.os.IPowerManager");
                        boolean forceSuspend = forceSuspend();
                        reply.writeNoException();
                        reply.writeInt(forceSuspend ? 1 : 0);
                        return true;
                    case 45:
                        data.enforceInterface("android.os.IPowerManager");
                        int[] _result12 = getWakeLockedUids();
                        reply.writeNoException();
                        reply.writeIntArray(_result12);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString("android.os.IPowerManager");
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IPowerManager {
            public static IPowerManager sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return "android.os.IPowerManager";
            }

            @Override // android.os.IPowerManager
            public void acquireWakeLock(IBinder lock, int flags, String tag, String packageName, WorkSource ws, String historyTag) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    try {
                        _data.writeStrongBinder(lock);
                        try {
                            _data.writeInt(flags);
                        } catch (Throwable th2) {
                            th = th2;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeString(tag);
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
                        _data.writeString(packageName);
                        if (ws != null) {
                            _data.writeInt(1);
                            ws.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            _data.writeString(historyTag);
                            if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().acquireWakeLock(lock, flags, tag, packageName, ws, historyTag);
                            _reply.recycle();
                            _data.recycle();
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

            @Override // android.os.IPowerManager
            public void acquireWakeLockWithUid(IBinder lock, int flags, String tag, String packageName, int uidtoblame) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    _data.writeStrongBinder(lock);
                    _data.writeInt(flags);
                    _data.writeString(tag);
                    _data.writeString(packageName);
                    _data.writeInt(uidtoblame);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().acquireWakeLockWithUid(lock, flags, tag, packageName, uidtoblame);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void releaseWakeLock(IBinder lock, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    _data.writeStrongBinder(lock);
                    _data.writeInt(flags);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().releaseWakeLock(lock, flags);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void updateWakeLockUids(IBinder lock, int[] uids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    _data.writeStrongBinder(lock);
                    _data.writeIntArray(uids);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateWakeLockUids(lock, uids);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void powerHint(int hintId, int data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    _data.writeInt(hintId);
                    _data.writeInt(data);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().powerHint(hintId, data);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void updateWakeLockWorkSource(IBinder lock, WorkSource ws, String historyTag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    _data.writeStrongBinder(lock);
                    if (ws != null) {
                        _data.writeInt(1);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(historyTag);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateWakeLockWorkSource(lock, ws, historyTag);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public boolean isWakeLockLevelSupported(int level) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    _data.writeInt(level);
                    boolean _result = false;
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isWakeLockLevelSupported(level);
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

            @Override // android.os.IPowerManager
            public void userActivity(long time, int event, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    _data.writeLong(time);
                    _data.writeInt(event);
                    _data.writeInt(flags);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().userActivity(time, event, flags);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void wakeUp(long time, int reason, String details, String opPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    _data.writeLong(time);
                    _data.writeInt(reason);
                    _data.writeString(details);
                    _data.writeString(opPackageName);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().wakeUp(time, reason, details, opPackageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void goToSleep(long time, int reason, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    _data.writeLong(time);
                    _data.writeInt(reason);
                    _data.writeInt(flags);
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().goToSleep(time, reason, flags);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void nap(long time) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    _data.writeLong(time);
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().nap(time);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public boolean isInteractive() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    boolean _result = false;
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isInteractive();
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

            @Override // android.os.IPowerManager
            public boolean isPowerSaveMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    boolean _result = false;
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isPowerSaveMode();
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

            @Override // android.os.IPowerManager
            public PowerSaveState getPowerSaveState(int serviceType) throws RemoteException {
                PowerSaveState _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    _data.writeInt(serviceType);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPowerSaveState(serviceType);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = PowerSaveState.CREATOR.createFromParcel(_reply);
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

            @Override // android.os.IPowerManager
            public boolean setPowerSaveModeEnabled(boolean mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    boolean _result = true;
                    _data.writeInt(mode ? 1 : 0);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setPowerSaveModeEnabled(mode);
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

            @Override // android.os.IPowerManager
            public boolean setDynamicPowerSaveHint(boolean powerSaveHint, int disableThreshold) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    boolean _result = true;
                    _data.writeInt(powerSaveHint ? 1 : 0);
                    _data.writeInt(disableThreshold);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setDynamicPowerSaveHint(powerSaveHint, disableThreshold);
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

            @Override // android.os.IPowerManager
            public boolean setAdaptivePowerSavePolicy(BatterySaverPolicyConfig config) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    boolean _result = true;
                    if (config != null) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setAdaptivePowerSavePolicy(config);
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

            @Override // android.os.IPowerManager
            public boolean setAdaptivePowerSaveEnabled(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    boolean _result = true;
                    _data.writeInt(enabled ? 1 : 0);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setAdaptivePowerSaveEnabled(enabled);
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

            @Override // android.os.IPowerManager
            public int getPowerSaveModeTrigger() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    if (!this.mRemote.transact(19, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPowerSaveModeTrigger();
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

            @Override // android.os.IPowerManager
            public boolean isDeviceIdleMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    boolean _result = false;
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isDeviceIdleMode();
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

            @Override // android.os.IPowerManager
            public boolean isLightDeviceIdleMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    boolean _result = false;
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isLightDeviceIdleMode();
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

            @Override // android.os.IPowerManager
            public void reboot(boolean confirm, String reason, boolean wait) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    int i = 1;
                    _data.writeInt(confirm ? 1 : 0);
                    _data.writeString(reason);
                    if (!wait) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(22, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reboot(confirm, reason, wait);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void rebootSafeMode(boolean confirm, boolean wait) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    int i = 1;
                    _data.writeInt(confirm ? 1 : 0);
                    if (!wait) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(23, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().rebootSafeMode(confirm, wait);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void shutdown(boolean confirm, String reason, boolean wait) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    int i = 1;
                    _data.writeInt(confirm ? 1 : 0);
                    _data.writeString(reason);
                    if (!wait) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(24, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().shutdown(confirm, reason, wait);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void crash(String message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    _data.writeString(message);
                    if (this.mRemote.transact(25, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().crash(message);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public int getLastShutdownReason() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    if (!this.mRemote.transact(26, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLastShutdownReason();
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

            @Override // android.os.IPowerManager
            public int getLastSleepReason() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    if (!this.mRemote.transact(27, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLastSleepReason();
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

            @Override // android.os.IPowerManager
            public void setStayOnSetting(int val) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    _data.writeInt(val);
                    if (this.mRemote.transact(28, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setStayOnSetting(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void boostScreenBrightness(long time) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    _data.writeLong(time);
                    if (this.mRemote.transact(29, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().boostScreenBrightness(time);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public int getScreenState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    if (!this.mRemote.transact(30, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getScreenState();
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

            @Override // android.os.IPowerManager
            public long getFrameworksBlockedTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    if (!this.mRemote.transact(31, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFrameworksBlockedTime();
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

            @Override // android.os.IPowerManager
            public Map getTopAppBlocked(int n) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    _data.writeInt(n);
                    if (!this.mRemote.transact(32, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTopAppBlocked(n);
                    }
                    _reply.readException();
                    Map _result = _reply.readHashMap(getClass().getClassLoader());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void stopChargeForSale() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    if (this.mRemote.transact(33, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().stopChargeForSale();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void resumeChargeForSale() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    if (this.mRemote.transact(34, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resumeChargeForSale();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public int getCurrentChargeStateForSale() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    if (!this.mRemote.transact(35, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCurrentChargeStateForSale();
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

            @Override // android.os.IPowerManager
            public void setDozeOverride(int screenState, int screenBrightness) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    _data.writeInt(screenState);
                    _data.writeInt(screenBrightness);
                    if (this.mRemote.transact(36, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDozeOverride(screenState, screenBrightness);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public boolean getDisplayAodStatus() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    boolean _result = false;
                    if (!this.mRemote.transact(37, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDisplayAodStatus();
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

            @Override // android.os.IPowerManager
            public int getMinimumScreenBrightnessSetting() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    if (!this.mRemote.transact(38, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMinimumScreenBrightnessSetting();
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

            @Override // android.os.IPowerManager
            public int getMaximumScreenBrightnessSetting() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    if (!this.mRemote.transact(39, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMaximumScreenBrightnessSetting();
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

            @Override // android.os.IPowerManager
            public int getDefaultScreenBrightnessSetting() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    if (!this.mRemote.transact(40, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDefaultScreenBrightnessSetting();
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

            @Override // android.os.IPowerManager
            public boolean isScreenBrightnessBoosted() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    boolean _result = false;
                    if (!this.mRemote.transact(41, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isScreenBrightnessBoosted();
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

            @Override // android.os.IPowerManager
            public void setAttentionLight(boolean on, int color) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    _data.writeInt(on ? 1 : 0);
                    _data.writeInt(color);
                    if (this.mRemote.transact(42, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAttentionLight(on, color);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public void setDozeAfterScreenOff(boolean on) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    _data.writeInt(on ? 1 : 0);
                    if (this.mRemote.transact(43, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setDozeAfterScreenOff(on);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.os.IPowerManager
            public boolean forceSuspend() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    boolean _result = false;
                    if (!this.mRemote.transact(44, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().forceSuspend();
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

            @Override // android.os.IPowerManager
            public int[] getWakeLockedUids() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.os.IPowerManager");
                    if (!this.mRemote.transact(45, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWakeLockedUids();
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

        public static boolean setDefaultImpl(IPowerManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IPowerManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
