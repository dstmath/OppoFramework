package com.android.server.engineer;

import android.os.IHwBinder;
import android.util.Slog;
import java.util.ArrayList;
import java.util.List;
import vendor.oppo.engnative.engineer.V1_0.IEngineer;

class OppoEngineerNative {
    private static final String TAG = "OppoEngineerNative";
    /* access modifiers changed from: private */
    public static IEngineer sEngineer;
    private static HidlDeathRecipient sHidlDeathRecipient = new HidlDeathRecipient();

    OppoEngineerNative() {
    }

    private static void initEngineerHwService() {
        try {
            sEngineer = IEngineer.getService();
            if (sEngineer != null) {
                sEngineer.linkToDeath(sHidlDeathRecipient, 0);
            }
        } catch (Exception e) {
            Slog.e(TAG, "exception caught " + e.getMessage());
            sEngineer = null;
        }
    }

    private static class HidlDeathRecipient implements IHwBinder.DeathRecipient {
        private HidlDeathRecipient() {
        }

        public void serviceDied(long cookie) {
            Slog.d(OppoEngineerNative.TAG, "serviceDied! cookie = " + cookie);
            IEngineer unused = OppoEngineerNative.sEngineer = null;
        }
    }

    private static byte[] transferByteArrayList(List<Byte> byteArrayList) {
        if (byteArrayList == null || byteArrayList.size() == 0) {
            return null;
        }
        byte[] byteArray = new byte[byteArrayList.size()];
        for (int i = 0; i < byteArrayList.size(); i++) {
            byteArray[i] = byteArrayList.get(i).byteValue();
        }
        return byteArray;
    }

    private static ArrayList<Byte> transferByteArray(byte[] byteArray) {
        if (byteArray == null || byteArray.length == 0) {
            return null;
        }
        ArrayList<Byte> byteArrayList = new ArrayList<>();
        for (int i = 0; i < byteArray.length; i++) {
            byteArrayList.add(i, Byte.valueOf(byteArray[i]));
        }
        return byteArrayList;
    }

    static boolean nativeGetPartionWriteProtectState() {
        initEngineerHwService();
        try {
            if (sEngineer != null) {
                return sEngineer.getPartionWriteProtectState();
            }
            return false;
        } catch (Exception e) {
            Slog.e(TAG, "nativeGetPartionWriteProtectState exception caught " + e.getMessage());
            return false;
        }
    }

    static boolean nativeSetPartionWriteProtectState(boolean disable) {
        initEngineerHwService();
        try {
            if (sEngineer != null) {
                return sEngineer.setPartionWriteProtectState(disable);
            }
            return false;
        } catch (Exception e) {
            Slog.e(TAG, "nativeSetPartionWriteProtectState exception caught " + e.getMessage());
            return false;
        }
    }

    static byte[] nativeGetBadBatteryConfig(int offset, int size) {
        initEngineerHwService();
        try {
            if (sEngineer != null) {
                return transferByteArrayList(sEngineer.getBadBatteryConfig(offset, size));
            }
            return null;
        } catch (Exception e) {
            Slog.e(TAG, "nativeGetBadBatteryConfig exception caught " + e.getMessage());
            return null;
        }
    }

    static int nativeSetBatteryBatteryConfig(int offset, int size, byte[] data) {
        initEngineerHwService();
        try {
            if (sEngineer != null) {
                return sEngineer.setBatteryBatteryConfig(offset, size, transferByteArray(data));
            }
            return 0;
        } catch (Exception e) {
            Slog.e(TAG, "nativeSetBatteryBatteryConfig exception caught " + e.getMessage());
            return 0;
        }
    }

    static byte[] nativeGetProductLineTestResult() {
        initEngineerHwService();
        try {
            if (sEngineer != null) {
                return transferByteArrayList(sEngineer.getProductLineTestResult());
            }
            return null;
        } catch (Exception e) {
            Slog.e(TAG, "nativeGetProductLineTestResult exception caught " + e.getMessage());
            return null;
        }
    }

    static boolean nativeSetProductLineTestResult(int position, int result) {
        initEngineerHwService();
        try {
            if (sEngineer != null) {
                return sEngineer.setProductLineTestResult(position, result);
            }
            return false;
        } catch (Exception e) {
            Slog.e(TAG, "nativeSetProductLineTestResult exception caught " + e.getMessage());
            return false;
        }
    }

    static boolean nativeResetProductLineTestResult() {
        initEngineerHwService();
        try {
            if (sEngineer != null) {
                return sEngineer.resetProductLineTestResult();
            }
            return false;
        } catch (Exception e) {
            Slog.e(TAG, "nativeResetProductLineTestResult exception caught " + e.getMessage());
            return false;
        }
    }

    static byte[] nativeReadEngineerData(int type) {
        initEngineerHwService();
        try {
            if (sEngineer != null) {
                return transferByteArrayList(sEngineer.readEngineerData(type));
            }
            return null;
        } catch (Exception e) {
            Slog.e(TAG, "nativeReadEngineerData exception caught " + e.getMessage());
            return null;
        }
    }

    static boolean nativeSaveEngineerData(int type, byte[] engineerData, int length) {
        initEngineerHwService();
        try {
            if (sEngineer != null) {
                return sEngineer.saveEngineerData(type, transferByteArray(engineerData), length);
            }
            return false;
        } catch (Exception e) {
            Slog.e(TAG, "nativeSaveEngineerData exception caught " + e.getMessage());
            return false;
        }
    }

    static boolean nativeSaveOppoUsageRecords(String path, String usageRecord, boolean isSingleRecord) {
        initEngineerHwService();
        try {
            if (sEngineer != null) {
                return sEngineer.saveOppoUsageRecords(path, usageRecord, isSingleRecord);
            }
            return false;
        } catch (Exception e) {
            Slog.e(TAG, "nativeSaveOppoUsageRecords exception caught " + e.getMessage());
            return false;
        }
    }

    static byte[] native_getDownloadStatus() {
        return nativeReadEngineerData(100001);
    }

    static byte[] nativeReadOppoUsageRecords(String path) {
        initEngineerHwService();
        try {
            if (sEngineer != null) {
                return transferByteArrayList(sEngineer.readOppoUsageRecords(path));
            }
            return null;
        } catch (Exception e) {
            Slog.e(TAG, "nativeReadOppoUsageRecords exception caught " + e.getMessage());
            return null;
        }
    }

    static void nativeWriteData(String path, int offset, boolean fromEnd, int length, byte[] data) {
        initEngineerHwService();
        try {
            if (sEngineer != null) {
                sEngineer.writeData(path, offset, fromEnd, length, transferByteArray(data));
            }
        } catch (Exception e) {
            Slog.e(TAG, "nativeWriteData exception caught " + e.getMessage());
        }
    }
}
