package android.engineer;

import android.engineer.IOppoEngineerManager;
import android.os.RemoteException;
import android.os.ServiceManager;

public class OppoEngineerManager {
    private static final String ENGINEER_SERVICE_NAME = "engineer";

    private OppoEngineerManager() {
    }

    private static IOppoEngineerManager init() {
        return IOppoEngineerManager.Stub.asInterface(ServiceManager.getService("engineer"));
    }

    public static void turnButtonLightOn(int brightness) {
        IOppoEngineerManager manager = init();
        if (manager != null) {
            try {
                manager.turnButtonLightOn(brightness);
            } catch (RemoteException e) {
            }
        }
    }

    public static void turnButtonLightOff() {
        IOppoEngineerManager manager = init();
        if (manager != null) {
            try {
                manager.turnButtonLightOff();
            } catch (RemoteException e) {
            }
        }
    }

    public static void turnBreathLightOn(int color) {
        IOppoEngineerManager manager = init();
        if (manager != null) {
            try {
                manager.turnBreathLightOn(color);
            } catch (RemoteException e) {
            }
        }
    }

    public static void turnBreathLightFlashOn(int color) {
        IOppoEngineerManager manager = init();
        if (manager != null) {
            try {
                manager.turnBreathLightFlashOn(color);
            } catch (RemoteException e) {
            }
        }
    }

    public static void turnBreathLightOff() {
        IOppoEngineerManager manager = init();
        if (manager != null) {
            try {
                manager.turnBreathLightOff();
            } catch (RemoteException e) {
            }
        }
    }

    public static void setTorchState(String state) {
        IOppoEngineerManager manager = init();
        if (manager != null) {
            try {
                manager.setTorchState(state);
            } catch (RemoteException e) {
            }
        }
    }

    public static String getDownloadStatus() {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return null;
        }
        try {
            return manager.getDownloadStatus();
        } catch (RemoteException e) {
            return null;
        }
    }

    public static boolean isSerialPortEnabled() {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return false;
        }
        try {
            return manager.isSerialPortEnabled();
        } catch (RemoteException e) {
            return false;
        }
    }

    public static boolean setSerialPortState(boolean enable) {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return false;
        }
        try {
            return manager.setSerialPortState(enable);
        } catch (RemoteException e) {
            return false;
        }
    }

    public static byte[] getEmmcHealthInfo() {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return null;
        }
        try {
            return manager.getEmmcHealthInfo();
        } catch (RemoteException e) {
            return null;
        }
    }

    public static boolean isPartionWriteProtectDisabled() {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return false;
        }
        try {
            return manager.isPartionWriteProtectDisabled();
        } catch (RemoteException e) {
            return false;
        }
    }

    public static boolean disablePartionWriteProtect(boolean disable) {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return false;
        }
        try {
            return manager.disablePartionWriteProtect(disable);
        } catch (RemoteException e) {
            return false;
        }
    }

    public static boolean resetWriteProtectState() {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return false;
        }
        try {
            return manager.resetWriteProtectState();
        } catch (RemoteException e) {
            return false;
        }
    }

    public static String getBackCoverColorId() {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return null;
        }
        try {
            return manager.getBackCoverColorId();
        } catch (RemoteException e) {
            return null;
        }
    }

    public static boolean setBackCoverColorId(String colorId) {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return false;
        }
        try {
            return manager.setBackCoverColorId(colorId);
        } catch (RemoteException e) {
            return false;
        }
    }

    public static String getCarrierVersion() {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return null;
        }
        try {
            return manager.getCarrierVersion();
        } catch (RemoteException e) {
            return null;
        }
    }

    public static boolean setCarrierVersion(String version) {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return false;
        }
        try {
            return manager.setCarrierVersion(version);
        } catch (RemoteException e) {
            return false;
        }
    }

    public static String getRegionNetlockStatus() {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return null;
        }
        try {
            return manager.getRegionNetlockStatus();
        } catch (RemoteException e) {
            return null;
        }
    }

    public static boolean setRegionNetlock(String lock) {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return false;
        }
        try {
            return manager.setRegionNetlock(lock);
        } catch (RemoteException e) {
            return false;
        }
    }

    public static String getTelcelSimlockStatus() {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return null;
        }
        try {
            return manager.getTelcelSimlockStatus();
        } catch (RemoteException e) {
            return null;
        }
    }

    public static boolean setTelcelSimlock(String lock) {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return false;
        }
        try {
            return manager.setTelcelSimlock(lock);
        } catch (RemoteException e) {
            return false;
        }
    }

    public static String getTelcelSimlockUnlockTimes() {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return null;
        }
        try {
            return manager.getTelcelSimlockUnlockTimes();
        } catch (RemoteException e) {
            return null;
        }
    }

    public static boolean setTelcelSimlockUnlockTimes(String times) {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return false;
        }
        try {
            return manager.setTelcelSimlockUnlockTimes(times);
        } catch (RemoteException e) {
            return false;
        }
    }

    public static String getSingleDoubleCardStatus() {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return null;
        }
        try {
            return manager.getSingleDoubleCardStatus();
        } catch (RemoteException e) {
            return null;
        }
    }

    public static boolean setSingleDoubleCard(String state) {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return false;
        }
        try {
            return manager.setSingleDoubleCard(state);
        } catch (RemoteException e) {
            return false;
        }
    }

    public static byte[] getBadBatteryConfig(int offset, int size) {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return null;
        }
        try {
            return manager.getBadBatteryConfig(offset, size);
        } catch (RemoteException e) {
            return null;
        }
    }

    public static int setBatteryBatteryConfig(int offset, int size, byte[] data) {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return -1;
        }
        try {
            return manager.setBatteryBatteryConfig(offset, size, data);
        } catch (RemoteException e) {
            return -1;
        }
    }

    public static byte[] getProductLineTestResult() {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return null;
        }
        try {
            return manager.getProductLineTestResult();
        } catch (RemoteException e) {
            return null;
        }
    }

    public static boolean setProductLineTestResult(int position, int result) {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return false;
        }
        try {
            return manager.setProductLineTestResult(position, result);
        } catch (RemoteException e) {
            return false;
        }
    }

    public static boolean resetProductLineTestResult() {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return false;
        }
        try {
            return manager.resetProductLineTestResult();
        } catch (RemoteException e) {
            return false;
        }
    }

    public static byte[] getCarrierVersionFromNvram() {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return null;
        }
        try {
            return manager.getCarrierVersionFromNvram();
        } catch (RemoteException e) {
            return null;
        }
    }

    public static boolean saveCarrierVersionToNvram(byte[] version) {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return false;
        }
        try {
            return manager.saveCarrierVersionToNvram(version);
        } catch (RemoteException e) {
            return false;
        }
    }

    public static byte[] getCalibrationStatusFromNvram() {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return null;
        }
        try {
            return manager.getCalibrationStatusFromNvram();
        } catch (RemoteException e) {
            return null;
        }
    }

    public static String getSimOperatorSwitchStatus() {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return null;
        }
        try {
            return manager.getSimOperatorSwitchStatus();
        } catch (RemoteException e) {
            return null;
        }
    }

    public static boolean setSimOperatorSwitch(String state) {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return false;
        }
        try {
            return manager.setSimOperatorSwitch(state);
        } catch (RemoteException e) {
            return false;
        }
    }

    public static String getDeviceLockStatus() {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return null;
        }
        try {
            return manager.getDeviceLockStatus();
        } catch (RemoteException e) {
            return null;
        }
    }

    public static boolean setDeviceLockStatus(String state) {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return false;
        }
        try {
            return manager.setDeviceLockStatus(state);
        } catch (RemoteException e) {
            return false;
        }
    }

    public static String getDeviceLockIMSI() {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return null;
        }
        try {
            return manager.getDeviceLockIMSI();
        } catch (RemoteException e) {
            return null;
        }
    }

    public static boolean setDeviceLockIMSI(String state) {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return false;
        }
        try {
            return manager.setDeviceLockIMSI(state);
        } catch (RemoteException e) {
            return false;
        }
    }

    public static String getDeviceLockDays() {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return null;
        }
        try {
            return manager.getDeviceLockDays();
        } catch (RemoteException e) {
            return null;
        }
    }

    public static boolean setDeviceLockDays(String state) {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return false;
        }
        try {
            return manager.setDeviceLockDays(state);
        } catch (RemoteException e) {
            return false;
        }
    }

    public static String getDeviceLockLastBindTime() {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return null;
        }
        try {
            return manager.getDeviceLockLastBindTime();
        } catch (RemoteException e) {
            return null;
        }
    }

    public static boolean setDeviceLockLastBindTime(String state) {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return false;
        }
        try {
            return manager.setDeviceLockLastBindTime(state);
        } catch (RemoteException e) {
            return false;
        }
    }

    public static String getDeviceLockICCID() {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return null;
        }
        try {
            return manager.getDeviceLockICCID();
        } catch (RemoteException e) {
            return null;
        }
    }

    public static boolean setDeviceLockICCID(String state) {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return false;
        }
        try {
            return manager.setDeviceLockICCID(state);
        } catch (RemoteException e) {
            return false;
        }
    }

    public static String getDeviceLockFirstBindTime() {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return null;
        }
        try {
            return manager.getDeviceLockFirstBindTime();
        } catch (RemoteException e) {
            return null;
        }
    }

    public static boolean setDeviceLockFirstBindTime(String state) {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return false;
        }
        try {
            return manager.setDeviceLockFirstBindTime(state);
        } catch (RemoteException e) {
            return false;
        }
    }

    public static String getDeviceLockUnlockTime() {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return null;
        }
        try {
            return manager.getDeviceLockUnlockTime();
        } catch (RemoteException e) {
            return null;
        }
    }

    public static boolean setDeviceLockUnlockTime(String state) {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return false;
        }
        try {
            return manager.setDeviceLockUnlockTime(state);
        } catch (RemoteException e) {
            return false;
        }
    }

    public static String getBootImgWaterMark() {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return null;
        }
        try {
            return manager.getBootImgWaterMark();
        } catch (RemoteException e) {
            return null;
        }
    }

    public static byte[] readEngineerData(int type) {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return null;
        }
        try {
            return manager.readEngineerData(type);
        } catch (RemoteException e) {
            return null;
        }
    }

    public static boolean saveEngineerData(int type, byte[] engineerData, int length) {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return false;
        }
        try {
            return manager.saveEngineerData(type, engineerData, length);
        } catch (RemoteException e) {
            return false;
        }
    }

    public static boolean fastbootUnlock(byte[] data, int length) {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return false;
        }
        try {
            return manager.fastbootUnlock(data, length);
        } catch (RemoteException e) {
            return false;
        }
    }

    public static void setSystemProperties(String key, String val) {
        IOppoEngineerManager manager = init();
        if (manager != null) {
            try {
                manager.setSystemProperties(key, val);
            } catch (RemoteException e) {
            }
        }
    }

    public static String getSystemProperties(String key, String val) {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return null;
        }
        try {
            return manager.getSystemProperties(key, val);
        } catch (RemoteException e) {
            return null;
        }
    }

    public static boolean saveOppoUsageRecords(String path, String usageRecord, boolean isSingleRecord) {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return false;
        }
        try {
            return manager.saveOppoUsageRecords(path, usageRecord, isSingleRecord);
        } catch (RemoteException e) {
            return false;
        }
    }

    public static boolean isEngineerItemInBlackList(int type, String item) {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return false;
        }
        try {
            return manager.isEngineerItemInBlackList(type, item);
        } catch (RemoteException e) {
            return false;
        }
    }

    public static String getOppoUsageRecords(String path) {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return null;
        }
        try {
            return manager.getOppoUsageRecords(path);
        } catch (RemoteException e) {
            return null;
        }
    }

    public static boolean saveHeytapID(int type, String id) {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return false;
        }
        try {
            return manager.saveHeytapID(type, id);
        } catch (RemoteException e) {
            return false;
        }
    }

    public static String getHeytapID(int type) {
        IOppoEngineerManager manager = init();
        if (manager == null) {
            return null;
        }
        try {
            return manager.getHeytapID(type);
        } catch (RemoteException e) {
            return null;
        }
    }
}
