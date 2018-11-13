package com.android.server.engineer;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.LocalServices;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

public class OppoEngineerUtils {
    private static final String CLEAR_ITEM_METHOD = "cleanItem";
    private static final String GET_SECRECY_STATE_METHOD = "getSecrecyState";
    private static final String OPPO_MANAGER_CLASS_NAME = "android.os.OppoManager";
    private static final int SECRECY_ADB_TYPE = 4;
    private static final String SECRECY_INTERNAL_CLASS_NAME = "android.secrecy.SecrecyManagerInternal";
    private static final String SECRECY_SUPPORT_FEATURE = "oppo.secrecy.support";
    private static final String SYNC_CACHE_TO_EMMC_METHOD = "syncCacheToEmmc";
    private static final String TAG = "OppoEngineerUtils";
    private static final String WRITE_LOG_TO_PARTION_METHOD = "writeLogToPartition";

    private OppoEngineerUtils() {
    }

    public static boolean isMtkPlatform() {
        return SystemProperties.get("ro.board.platform", "oppo").toLowerCase().startsWith("mt");
    }

    public static boolean isSecrecyEncryptState(Context context) {
        if (!context.getPackageManager().hasSystemFeature(SECRECY_SUPPORT_FEATURE)) {
            return false;
        }
        Object secrecyManagerInternal = null;
        try {
            secrecyManagerInternal = LocalServices.getService(Class.forName(SECRECY_INTERNAL_CLASS_NAME));
        } catch (ClassNotFoundException e) {
            Slog.i(TAG, "ClassNotFoundException found");
        }
        if (secrecyManagerInternal != null) {
            Object result = invokeDeclaredMethod(secrecyManagerInternal, SECRECY_INTERNAL_CLASS_NAME, GET_SECRECY_STATE_METHOD, new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(4)});
            if (result != null) {
                return ((Boolean) result).booleanValue();
            }
            Slog.i(TAG, "result is null");
            return false;
        }
        Slog.i(TAG, "secrecyManagerInternal is null");
        return false;
    }

    public static int writeLogToPartition(int type, String logstring, String tagString, String issue, String desc) {
        Object result = invokeDeclaredMethod(null, OPPO_MANAGER_CLASS_NAME, WRITE_LOG_TO_PARTION_METHOD, new Class[]{Integer.TYPE, String.class, String.class, String.class, String.class}, new Object[]{Integer.valueOf(type), logstring, tagString, issue, desc});
        if (result != null) {
            return ((Integer) result).intValue();
        }
        return -1;
    }

    public static int cleanItem(int id) {
        Object result = invokeDeclaredMethod(null, OPPO_MANAGER_CLASS_NAME, CLEAR_ITEM_METHOD, new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(id)});
        if (result != null) {
            return ((Integer) result).intValue();
        }
        return -1;
    }

    public static int syncCacheToEmmc() {
        Object result = invokeDeclaredMethod(null, OPPO_MANAGER_CLASS_NAME, SYNC_CACHE_TO_EMMC_METHOD, null, null);
        if (result != null) {
            return ((Integer) result).intValue();
        }
        return -1;
    }

    private static Object invokeDeclaredMethod(Object target, String clsName, String methodName, Class[] parameterTypes, Object[] args) {
        Slog.i(TAG, target + " invokeDeclaredMethod : " + clsName + "." + methodName);
        Object result = null;
        try {
            Method method = Class.forName(clsName).getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (ClassNotFoundException e) {
            Slog.i(TAG, "ClassNotFoundException : " + e.getMessage());
            return result;
        } catch (NoSuchMethodException e2) {
            Slog.i(TAG, "NoSuchMethodException : " + e2.getMessage());
            return result;
        } catch (IllegalAccessException e3) {
            Slog.i(TAG, "IllegalAccessException : " + e3.getMessage());
            return result;
        } catch (InvocationTargetException e4) {
            Slog.i(TAG, "InvocationTargetException : " + e4.getMessage());
            return result;
        } catch (SecurityException e5) {
            Slog.i(TAG, "SecurityException : " + e5.getMessage());
            return result;
        } catch (Exception e6) {
            Slog.e(TAG, "Exception : " + e6.getMessage());
            return result;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x00cd A:{SYNTHETIC, Splitter: B:29:0x00cd} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int readIntFromFile(String path, int defaultValue) {
        String tempString;
        IOException e;
        Throwable th;
        BufferedReader reader = null;
        int result = defaultValue;
        try {
            BufferedReader reader2 = new BufferedReader(new FileReader(new File(path)));
            try {
                tempString = reader2.readLine();
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e1) {
                        Slog.e(TAG, "readIntFromFile io close exception :" + e1.getMessage());
                    }
                }
                reader = reader2;
            } catch (IOException e2) {
                e = e2;
                reader = reader2;
            } catch (Throwable th2) {
                th = th2;
                reader = reader2;
                if (reader != null) {
                }
                throw th;
            }
        } catch (IOException e3) {
            e = e3;
            tempString = null;
            try {
                Slog.e(TAG, "readIntFromFile io exception:" + e.getMessage());
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e12) {
                        Slog.e(TAG, "readIntFromFile io close exception :" + e12.getMessage());
                    }
                }
                try {
                    result = Integer.valueOf(tempString).intValue();
                } catch (NumberFormatException e4) {
                    Slog.e(TAG, "readIntFromFile NumberFormatException:" + e4.getMessage());
                }
                Slog.i(TAG, "readIntFromFile path:" + path + ", result:" + result + ", defaultValue:" + defaultValue);
                return result;
            } catch (Throwable th3) {
                th = th3;
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e122) {
                        Slog.e(TAG, "readIntFromFile io close exception :" + e122.getMessage());
                    }
                }
                throw th;
            }
        }
        if (!(tempString == null || ("".equals(tempString) ^ 1) == 0)) {
            result = Integer.valueOf(tempString).intValue();
        }
        Slog.i(TAG, "readIntFromFile path:" + path + ", result:" + result + ", defaultValue:" + defaultValue);
        return result;
    }

    public static String transferByteArrayToString(byte[] byteArray) {
        if (byteArray == null || byteArray.length <= 0) {
            return null;
        }
        int arrayLength = byteArray.length;
        int contentLength = 0;
        int i = 0;
        while (i < arrayLength && byteArray[i] != (byte) 0) {
            contentLength = i + 1;
            i++;
        }
        return new String(byteArray, 0, contentLength, StandardCharsets.UTF_8);
    }
}
