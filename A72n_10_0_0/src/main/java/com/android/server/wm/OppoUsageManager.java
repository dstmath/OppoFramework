package com.android.server.wm;

public class OppoUsageManager {
    private static final String OPPOUSAGEMANAGER_CLASS_NAME = "android.os.OppoUsageManager";
    private static final String TAG = "OppoUsageManager";
    private static Object sOppoUsageManager = null;

    private static void getManager() {
        try {
            Class<?> className = Class.forName(OPPOUSAGEMANAGER_CLASS_NAME);
            Object result = className.getMethod("getOppoUsageManager", new Class[0]).invoke(className, new Object[0]);
            if (result != null) {
                sOppoUsageManager = result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean writeAppUsageHistoryRecord(String appName, String dateTime) {
        getManager();
        try {
            Object result = Class.forName(OPPOUSAGEMANAGER_CLASS_NAME).getMethod("writeAppUsageHistoryRecord", String.class, String.class).invoke(sOppoUsageManager, appName, dateTime);
            if (result != null) {
                return ((Boolean) result).booleanValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean recordApkDeleteEvent(String packageName, String callerAppPkgName, String dateTime) {
        getManager();
        try {
            Object result = Class.forName(OPPOUSAGEMANAGER_CLASS_NAME).getMethod("recordApkDeleteEvent", String.class, String.class, String.class).invoke(sOppoUsageManager, packageName, callerAppPkgName, dateTime);
            if (result != null) {
                return ((Boolean) result).booleanValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static byte[] readOppoFile(String path, int startPosition, int length) {
        getManager();
        try {
            Object result = Class.forName(OPPOUSAGEMANAGER_CLASS_NAME).getMethod("readOppoFile", String.class, Integer.TYPE, Integer.TYPE).invoke(sOppoUsageManager, path, Integer.valueOf(startPosition), Integer.valueOf(length));
            if (result != null) {
                return (byte[]) result;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int saveOppoFile(int fileMax, String path, int offset, boolean append, int length, byte[] data) {
        getManager();
        try {
            Object result = Class.forName(OPPOUSAGEMANAGER_CLASS_NAME).getMethod("saveOppoFile", Integer.TYPE, String.class, Integer.TYPE, Boolean.TYPE, Integer.TYPE, byte[].class).invoke(sOppoUsageManager, Integer.valueOf(fileMax), path, Integer.valueOf(offset), Boolean.valueOf(append), Integer.valueOf(length), data);
            if (result != null) {
                return ((Integer) result).intValue();
            }
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int getFileSize(String path) {
        getManager();
        try {
            Object result = Class.forName(OPPOUSAGEMANAGER_CLASS_NAME).getMethod("getFileSize", String.class).invoke(sOppoUsageManager, path);
            if (result != null) {
                return ((Integer) result).intValue();
            }
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
