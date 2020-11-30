package com.android.server.engineer;

import android.content.ComponentName;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Slog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

/* access modifiers changed from: package-private */
public class OppoEngineerUtils {
    private static final String CLEAR_ITEM_METHOD = "cleanItem";
    private static final String CRYPTOENG_CLASS = "vendor.oppo.hardware.cryptoeng.V1_0.ICryptoeng";
    public static final int ERROR_FAILED = -2;
    public static final int ERROR_NOT_SUPPORTED = -1;
    private static final String FINGERPRINT_PAY_CLASS = "vendor.oppo.hardware.biometrics.fingerprintpay.V1_0.IFingerprintPay";
    private static final String OPPO_MANAGER_CLASS_NAME = "android.os.OppoManager";
    public static final int STATUS_OK = 0;
    private static final String SYNC_CACHE_TO_EMMC_METHOD = "syncCacheToEmmc";
    private static final String TAG = "OppoEngineerUtils";
    private static final String WRITE_LOG_TO_PARTION_METHOD = "writeLogToPartition";

    private OppoEngineerUtils() {
    }

    static boolean isMtkPlatform() {
        return SystemProperties.get("ro.board.platform", "oppo").toLowerCase().startsWith("mt");
    }

    static int writeLogToPartition(int type, String logstring, String tagString, String issue, String desc) {
        Object result = invokeDeclaredMethod(null, OPPO_MANAGER_CLASS_NAME, WRITE_LOG_TO_PARTION_METHOD, new Class[]{Integer.TYPE, String.class, String.class, String.class, String.class}, new Object[]{Integer.valueOf(type), logstring, tagString, issue, desc});
        if (result != null) {
            return ((Integer) result).intValue();
        }
        return -1;
    }

    static int cleanItem(int id) {
        Object result = invokeDeclaredMethod(null, OPPO_MANAGER_CLASS_NAME, CLEAR_ITEM_METHOD, new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(id)});
        if (result != null) {
            return ((Integer) result).intValue();
        }
        return -1;
    }

    static int syncCacheToEmmc() {
        Object result = invokeDeclaredMethod(null, OPPO_MANAGER_CLASS_NAME, SYNC_CACHE_TO_EMMC_METHOD, null, null);
        if (result != null) {
            return ((Integer) result).intValue();
        }
        return -1;
    }

    private static Object invokeDeclaredMethod(Object target, String clsName, String methodName, Class[] parameterTypes, Object[] args) {
        try {
            Method method = Class.forName(clsName).getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (Exception e) {
            Slog.i(TAG, e.getMessage());
            return null;
        }
    }

    static Object getDeclaredField(Object target, String clsName, String fieldName) {
        try {
            Field field = Class.forName(clsName).getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) {
            Slog.i(TAG, "getDeclaredField exception caught : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    static String readLineFromFile(String path) {
        StringBuilder sb;
        BufferedReader reader = null;
        String tempString = null;
        try {
            reader = new BufferedReader(new FileReader(new File(path)));
            tempString = reader.readLine();
            try {
                reader.close();
            } catch (IOException e) {
                e1 = e;
                sb = new StringBuilder();
            }
        } catch (IOException e2) {
            Slog.e(TAG, "readIntFromFile io exception:" + e2.getMessage());
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e3) {
                    e1 = e3;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    Slog.e(TAG, "readIntFromFile io close exception :" + e1.getMessage());
                }
            }
            throw th;
        }
        Slog.i(TAG, "readLineFromFile path:" + path + ", result:" + tempString);
        return tempString;
        sb.append("readIntFromFile io close exception :");
        sb.append(e1.getMessage());
        Slog.e(TAG, sb.toString());
        Slog.i(TAG, "readLineFromFile path:" + path + ", result:" + tempString);
        return tempString;
    }

    static int readIntFromFile(String path, int defaultValue) {
        BufferedReader reader = null;
        int result = defaultValue;
        String tempString = null;
        try {
            reader = new BufferedReader(new FileReader(new File(path)));
            tempString = reader.readLine();
            try {
                reader.close();
            } catch (IOException e1) {
                Slog.i(TAG, e1.getMessage());
            }
        } catch (IOException e) {
            Slog.i(TAG, e.getMessage());
            if (reader != null) {
                reader.close();
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e12) {
                    Slog.i(TAG, e12.getMessage());
                }
            }
            throw th;
        }
        if (!TextUtils.isEmpty(tempString)) {
            try {
                result = Integer.valueOf(tempString).intValue();
            } catch (NumberFormatException e2) {
                Slog.i(TAG, e2.getMessage());
            }
        }
        Slog.i(TAG, "readIntFromFile path:" + path + ", result:" + result + ", defaultValue:" + defaultValue);
        return result;
    }

    static String transferByteArrayToString(byte[] byteArray) {
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

    static String transferByteListToString(ArrayList<Byte> byteList) {
        if (byteList == null || byteList.size() <= 0) {
            return null;
        }
        byte[] byteArray = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            byteArray[i] = byteList.get(i).byteValue();
        }
        return transferByteArrayToString(byteArray);
    }

    static Object getCryptoSerice() {
        try {
            Class<?> c = Class.forName(CRYPTOENG_CLASS);
            return c.getMethod("getService", new Class[0]).invoke(c, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static ArrayList<Byte> cryptoengInvokeCommand(ArrayList<Byte> crypto) {
        try {
            Method get = Class.forName(CRYPTOENG_CLASS).getMethod("cryptoeng_invoke_command", ArrayList.class);
            Object cryptoSerice = getCryptoSerice();
            if (cryptoSerice == null) {
                return null;
            }
            return (ArrayList) get.invoke(cryptoSerice, crypto);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static Object getFingerprintPaySerice() {
        try {
            Class<?> c = Class.forName(FINGERPRINT_PAY_CLASS);
            return c.getMethod("getService", new Class[0]).invoke(c, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static int alikeyVerify(Object fpInstance) {
        try {
            return ((Integer) Class.forName(FINGERPRINT_PAY_CLASS).getMethod("alikey_verify", new Class[0]).invoke(fpInstance, new Object[0])).intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    static int alikeyWrite(Object fpInstance) {
        try {
            return ((Integer) Class.forName(FINGERPRINT_PAY_CLASS).getMethod("alikey_write", new Class[0]).invoke(fpInstance, new Object[0])).intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    static int getAlikeyStatus(Object fpInstance) {
        try {
            return ((Integer) Class.forName(FINGERPRINT_PAY_CLASS).getMethod("get_alikey_status", new Class[0]).invoke(fpInstance, new Object[0])).intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    static int enableRpmb(Object fpInstance) {
        try {
            return ((Integer) Class.forName(FINGERPRINT_PAY_CLASS).getMethod("enable_rpmb", new Class[0]).invoke(fpInstance, new Object[0])).intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    static int getRpmbEnableState(Object fpInstance) {
        try {
            return ((Integer) Class.forName(FINGERPRINT_PAY_CLASS).getMethod("get_rpmb_enable_state", new Class[0]).invoke(fpInstance, new Object[0])).intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    static boolean isBackCoverColorIdValid(String color) {
        if (TextUtils.isEmpty(color)) {
            return false;
        }
        return Pattern.matches("[a-f0-9A-F]{8}", color);
    }

    static boolean isOppoSerialNoValid(String serial) {
        if (TextUtils.isEmpty(serial)) {
            return false;
        }
        return Pattern.matches("[A-J]{15}", serial);
    }

    static String bytesToHexString(byte[] bArray) {
        if (bArray == null || bArray.length <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(bArray.length);
        for (byte aBArray : bArray) {
            sb.append(String.format(Locale.US, "%02x", Integer.valueOf(aBArray)));
        }
        return sb.toString();
    }

    static class CmdResult {
        public String errorMsg;
        public int result;
        public String successMsg;

        public CmdResult(int result2) {
            this.result = result2;
        }

        public CmdResult(int result2, String successMsg2, String errorMsg2) {
            this.result = result2;
            this.successMsg = successMsg2;
            this.errorMsg = errorMsg2;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00b9, code lost:
        if (0 != 0) goto L_0x009c;
     */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00e7  */
    static CmdResult executeCmd(String cmd) {
        int result = -1;
        Process proc = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = null;
        StringBuilder errorMsg = null;
        Runtime runtime = Runtime.getRuntime();
        Slog.i(TAG, "exec command: " + cmd);
        try {
            proc = runtime.exec(cmd);
            result = proc.waitFor();
            successMsg = new StringBuilder();
            errorMsg = new StringBuilder();
            BufferedReader successResult2 = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader errorResult2 = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            while (true) {
                String s = successResult2.readLine();
                if (s == null) {
                    break;
                }
                Slog.i(TAG, "exec command: " + s);
                successMsg.append(s);
            }
            while (true) {
                String s2 = errorResult2.readLine();
                if (s2 != null) {
                    Slog.i(TAG, "exec command: " + s2);
                    errorMsg.append(s2);
                } else {
                    try {
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            successResult2.close();
            errorResult2.close();
        } catch (Exception e2) {
            e2.printStackTrace();
            if (0 != 0) {
                try {
                    successResult.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
            if (0 != 0) {
                errorResult.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    successResult.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                    if (0 != 0) {
                        proc.destroy();
                    }
                    throw th;
                }
            }
            if (0 != 0) {
                errorResult.close();
            }
            if (0 != 0) {
            }
            throw th;
        }
        proc.destroy();
        String str = null;
        String sb = successMsg == null ? null : successMsg.toString();
        if (errorMsg != null) {
            str = errorMsg.toString();
        }
        return new CmdResult(result, sb, str);
    }

    static boolean isComponentEquals(ComponentName cp1, ComponentName cp2) {
        if (cp1 == null || cp2 == null || !cp1.getPackageName().equals(cp2.getPackageName())) {
            return false;
        }
        if (cp1.getClassName().equals(cp2.getClassName()) || cp1.getShortClassName().equals(cp2.getShortClassName())) {
            return true;
        }
        return false;
    }

    static boolean isServerLinkAvailable(String link) {
        if (TextUtils.isEmpty(link) || !link.contains(":")) {
            Slog.e(TAG, "server link is invalid");
            return false;
        }
        String[] linkEle = link.split(":");
        if (linkEle.length != 2 || !isIp4AddressAvailable(linkEle[0]) || !isPortAvailble(linkEle[1])) {
            return false;
        }
        return true;
    }

    static boolean isIp4AddressAvailable(String ip) {
        if (!TextUtils.isEmpty(ip)) {
            return ip.matches("^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$");
        }
        return false;
    }

    static boolean isPortAvailble(String port) {
        if (TextUtils.isEmpty(port)) {
            return false;
        }
        try {
            int portNum = Integer.parseInt(port);
            if (portNum >= 65535 || portNum <= 0) {
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
    }

    static int writeCriticalData(int id, String content) {
        try {
            Object obj = Class.forName(OPPO_MANAGER_CLASS_NAME).getMethod("writeCriticalData", Integer.TYPE, String.class).invoke(null, Integer.valueOf(id), content);
            if (obj != null) {
                return ((Integer) obj).intValue();
            }
            return -1;
        } catch (Exception e) {
            Slog.i(TAG, "OppoManager writeCriticalData" + e.getMessage());
            return -1;
        }
    }
}
