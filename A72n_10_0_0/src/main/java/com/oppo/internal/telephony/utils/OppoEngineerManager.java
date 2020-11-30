package com.oppo.internal.telephony.utils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class OppoEngineerManager {
    private static final String OPPOENGINEERMODE_CLASS_NAME = "android.engineer.OppoEngineerManager";
    private static final String TAG = "OppoEngineerManager";
    public static final int TYPE_ENGINEER_ACTIVITY_ITEM = 1;
    public static final int TYPE_ENGINEER_SERVICE_ITEM = 2;
    public static final int TYPE_ENGINEER_SHELL_COMMAND_ITEM = 3;

    public static void turnButtonLightOn(int brightness) {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            c.getMethod("turnButtonLightOn", Integer.TYPE).invoke(c, Integer.valueOf(brightness));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void turnButtonLightOff() {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            c.getMethod("turnButtonLightOff", new Class[0]).invoke(c, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void turnBreathLightOn(int color) {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            c.getMethod("turnBreathLightOn", Integer.TYPE).invoke(c, Integer.valueOf(color));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void turnBreathLightFlashOn(int color) {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            c.getMethod("turnBreathLightFlashOn", Integer.TYPE).invoke(c, Integer.valueOf(color));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void turnBreathLightOff() {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            c.getMethod("turnBreathLightOff", new Class[0]).invoke(c, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setTorchState(String state) {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            c.getMethod("setTorchState", String.class).invoke(c, state);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getDownloadStatus() {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return (String) c.getMethod("getDownloadStatus", new Class[0]).invoke(c, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isSerialPortEnabled() {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return ((Boolean) c.getMethod("isSerialPortEnabled", new Class[0]).invoke(c, new Object[0])).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean setSerialPortState(boolean enable) {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return ((Boolean) c.getMethod("setSerialPortState", Boolean.TYPE).invoke(c, Boolean.valueOf(enable))).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static byte[] getEmmcHealthInfo() {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return (byte[]) c.getMethod("getEmmcHealthInfo", new Class[0]).invoke(c, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isPartionWriteProtectDisabled() {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return ((Boolean) c.getMethod("isPartionWriteProtectDisabled", new Class[0]).invoke(c, new Object[0])).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean disablePartionWriteProtect(boolean disable) {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return ((Boolean) c.getMethod("disablePartionWriteProtect", Boolean.TYPE).invoke(c, Boolean.valueOf(disable))).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean resetWriteProtectState() {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return ((Boolean) c.getMethod("resetWriteProtectState", new Class[0]).invoke(c, new Object[0])).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getBackCoverColorId() {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return (String) c.getMethod("getBackCoverColorId", new Class[0]).invoke(c, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean setBackCoverColorId(String colorId) {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return ((Boolean) c.getMethod("setBackCoverColorId", String.class).invoke(c, colorId)).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getCarrierVersion() {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return (String) c.getMethod("getCarrierVersion", new Class[0]).invoke(c, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean setCarrierVersion(String version) {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return ((Boolean) c.getMethod("setCarrierVersion", String.class).invoke(c, version)).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getRegionNetlockStatus() {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return (String) c.getMethod("getRegionNetlockStatus", new Class[0]).invoke(c, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean setRegionNetlock(String lock) {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return ((Boolean) c.getMethod("setRegionNetlock", String.class).invoke(c, lock)).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getTelcelSimlockStatus() {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return (String) c.getMethod("getTelcelSimlockStatus", new Class[0]).invoke(c, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean setTelcelSimlock(String lock) {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return ((Boolean) c.getMethod("setTelcelSimlock", String.class).invoke(c, lock)).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getTelcelSimlockUnlockTimes() {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return (String) c.getMethod("getTelcelSimlockUnlockTimes", new Class[0]).invoke(c, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean setTelcelSimlockUnlockTimes(String times) {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return ((Boolean) c.getMethod("setTelcelSimlockUnlockTimes", String.class).invoke(c, times)).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getSingleDoubleCardStatus() {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return (String) c.getMethod("getSingleDoubleCardStatus", new Class[0]).invoke(c, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean setSingleDoubleCard(String state) {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return ((Boolean) c.getMethod("setSingleDoubleCard", String.class).invoke(c, state)).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static byte[] getProductLineTestResult() {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return (byte[]) c.getMethod("getProductLineTestResult", new Class[0]).invoke(c, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean setProductLineTestResult(int position, int result) {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return ((Boolean) c.getMethod("setProductLineTestResult", Integer.TYPE, Integer.TYPE).invoke(c, Integer.valueOf(position), Integer.valueOf(result))).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean resetProductLineTestResult() {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return ((Boolean) c.getMethod("resetProductLineTestResult", new Class[0]).invoke(c, new Object[0])).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static byte[] getCarrierVersionFromNvram() {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return (byte[]) c.getMethod("getCarrierVersionFromNvram", new Class[0]).invoke(c, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean saveCarrierVersionToNvram(byte[] version) {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return ((Boolean) c.getMethod("saveCarrierVersionToNvram", byte[].class).invoke(c, version)).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static byte[] getCalibrationStatusFromNvram() {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return (byte[]) c.getMethod("getCalibrationStatusFromNvram", new Class[0]).invoke(c, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getSimOperatorSwitchStatus() {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return (String) c.getMethod("getSimOperatorSwitchStatus", new Class[0]).invoke(c, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean setSimOperatorSwitch(String state) {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return ((Boolean) c.getMethod("setSimOperatorSwitch", String.class).invoke(c, state)).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getDeviceLockStatus() {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return (String) c.getMethod("getDeviceLockStatus", new Class[0]).invoke(c, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean setDeviceLockStatus(String state) {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return ((Boolean) c.getMethod("setDeviceLockStatus", String.class).invoke(c, state)).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getDeviceLockIMSI() {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return (String) c.getMethod("getDeviceLockIMSI", new Class[0]).invoke(c, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean setDeviceLockIMSI(String state) {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return ((Boolean) c.getMethod("setDeviceLockIMSI", String.class).invoke(c, state)).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getDeviceLockDays() {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return (String) c.getMethod("getDeviceLockDays", new Class[0]).invoke(c, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean setDeviceLockDays(String state) {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return ((Boolean) c.getMethod("setDeviceLockDays", String.class).invoke(c, state)).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getDeviceLockLastBindTime() {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return (String) c.getMethod("getDeviceLockLastBindTime", new Class[0]).invoke(c, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean setDeviceLockLastBindTime(String state) {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return ((Boolean) c.getMethod("setDeviceLockLastBindTime", String.class).invoke(c, state)).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getDeviceLockICCID() {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return (String) c.getMethod("getDeviceLockICCID", new Class[0]).invoke(c, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean setDeviceLockICCID(String state) {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return ((Boolean) c.getMethod("setDeviceLockICCID", String.class).invoke(c, state)).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getDeviceLockFirstBindTime() {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return (String) c.getMethod("getDeviceLockFirstBindTime", new Class[0]).invoke(c, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean setDeviceLockFirstBindTime(String state) {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return ((Boolean) c.getMethod("setDeviceLockFirstBindTime", String.class).invoke(c, state)).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getDeviceLockUnlockTime() {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return (String) c.getMethod("getDeviceLockUnlockTime", new Class[0]).invoke(c, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean setDeviceLockUnlockTime(String state) {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return ((Boolean) c.getMethod("setDeviceLockUnlockTime", String.class).invoke(c, state)).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getBootImgWaterMark() {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return (String) c.getMethod("getBootImgWaterMark", new Class[0]).invoke(c, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] readEngineerData(int type) {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return (byte[]) c.getMethod("readEngineerData", Integer.TYPE).invoke(c, Integer.valueOf(type));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean saveEngineerData(int type, byte[] engineerData, int length) {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return ((Boolean) c.getMethod("saveEngineerData", Integer.TYPE, byte[].class, Integer.TYPE).invoke(c, Integer.valueOf(type), engineerData, Integer.valueOf(length))).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void setSystemProperties(String key, String val) {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            c.getMethod("setSystemProperties", String.class, String.class).invoke(c, key, val);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getSystemProperties(String key, String val) {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return (String) c.getMethod("getSystemProperties", String.class, String.class).invoke(c, key, val);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static byte[] transferByteArrayList(List<Byte> byteArrayList) {
        if (byteArrayList == null || byteArrayList.size() == 0) {
            return null;
        }
        byte[] byteArray = new byte[byteArrayList.size()];
        for (int i = 0; i < byteArrayList.size(); i++) {
            byteArray[i] = byteArrayList.get(i).byteValue();
        }
        return byteArray;
    }

    public static ArrayList<Byte> transferByteArray(byte[] byteArray) {
        if (byteArray == null || byteArray.length == 0) {
            return null;
        }
        ArrayList<Byte> byteArrayList = new ArrayList<>();
        for (int i = 0; i < byteArray.length; i++) {
            byteArrayList.add(i, Byte.valueOf(byteArray[i]));
        }
        return byteArrayList;
    }

    public static String transferByteArrayToString(byte[] byteArray) {
        if (byteArray == null || byteArray.length <= 0) {
            return null;
        }
        int arrayLength = byteArray.length;
        int contentLength = 0;
        int i = 0;
        while (i < arrayLength && byteArray[i] != 0) {
            contentLength = i + 1;
            i++;
        }
        return new String(byteArray, 0, contentLength, StandardCharsets.UTF_8);
    }

    public static String transferByteListToString(ArrayList<Byte> byteList) {
        if (byteList == null || byteList.size() <= 0) {
            return null;
        }
        byte[] byteArray = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            byteArray[i] = byteList.get(i).byteValue();
        }
        return transferByteArrayToString(byteArray);
    }

    public static boolean isEngineerItemInBlackList(int type, String item) {
        try {
            Class<?> c = Class.forName(OPPOENGINEERMODE_CLASS_NAME);
            return ((Boolean) c.getMethod("isEngineerItemInBlackList", Integer.TYPE, String.class).invoke(c, Integer.valueOf(type), item)).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
