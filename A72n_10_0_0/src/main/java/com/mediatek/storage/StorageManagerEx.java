package com.mediatek.storage;

import android.os.Environment;
import android.os.SystemProperties;
import android.util.Log;
import java.io.File;

public class StorageManagerEx {
    private static final String DIR_ANDROID = "Android";
    private static final String DIR_CACHE = "cache";
    private static final String DIR_DATA = "data";
    private static final String PROP_DEVICE_TABLET = "tablet";
    private static final String PROP_DEVICE_TYPE = "ro.build.characteristics";
    private static final String PROP_SD_DEFAULT_PATH = "persist.vendor.sys.sd.defaultpath";
    private static final String PROP_SD_EXTERNAL_PATH = "vold.path.external_sd";
    private static final String PROP_SD_SWAP = "vold.swap.state";
    private static final String PROP_SD_SWAP_FALSE = "0";
    private static final String PROP_SD_SWAP_TRUE = "1";
    private static final String STORAGE_PATH_EMULATED = "/storage/emulated/";
    private static final String STORAGE_PATH_SD1 = "/storage/sdcard0";
    private static final String STORAGE_PATH_SD1_ICS = "/mnt/sdcard";
    private static final String STORAGE_PATH_SD2 = "/storage/sdcard1";
    private static final String STORAGE_PATH_SD2_ICS = "/mnt/sdcard2";
    private static final String STORAGE_PATH_SHARE_SD = "/storage/emulated/0";
    private static final String TAG = "StorageManagerEx";

    public static String getDefaultPath() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        Log.i(TAG, " Default path taken as primary storage, path=" + path);
        return path;
    }

    public static void setDefaultPath(String path) {
        Log.i(TAG, "setDefaultPath path=" + path);
        if (path == null) {
            Log.e(TAG, "setDefaultPath error! path=null");
            return;
        }
        try {
            SystemProperties.set(PROP_SD_DEFAULT_PATH, path);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException when set default path:" + e);
        }
    }

    public static File getExternalCacheDir(String packageName) {
        if (packageName == null) {
            Log.w(TAG, "packageName = null!");
            return null;
        }
        File externalCacheDir = Environment.buildPath(new File(getDefaultPath()), new String[]{DIR_ANDROID, DIR_DATA, packageName, DIR_CACHE});
        Log.d(TAG, "getExternalCacheDir path = " + externalCacheDir);
        return externalCacheDir;
    }

    public static String getExternalStoragePath() {
        String path = null;
        try {
            path = SystemProperties.get(PROP_SD_EXTERNAL_PATH);
            Log.i(TAG, "getExternalStoragePath path=" + path);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "IllegalArgumentException when getExternalStoragePath:" + e);
        }
        Log.d(TAG, "getExternalStoragePath path=" + path);
        return path;
    }

    public static String getInternalStoragePath() {
        Log.d(TAG, "getInternalStoragePath path= null");
        return null;
    }

    public static String getInternalStoragePathForLogger() {
        String path = getInternalStoragePath();
        Log.i(TAG, "getInternalStoragePathForLogger raw path=" + path);
        if (path != null && path.startsWith(STORAGE_PATH_EMULATED)) {
            path = STORAGE_PATH_SHARE_SD;
        }
        Log.i(TAG, "getInternalStoragePathForLogger path=" + path);
        return path;
    }
}
