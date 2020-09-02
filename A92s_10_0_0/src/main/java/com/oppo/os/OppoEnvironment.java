package com.oppo.os;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import java.io.File;

public class OppoEnvironment {
    public static final File PARENT_STORAGE_DIRECTORY = getDirectory("EXTERNAL_STORAGE", "/storage/sdcard0");
    public static final File SUB_STORAGE_DIRECTORY = getDirectory("INTERNAL_STORAGE", "/storage/sdcard0/external_sd");
    private static final String TAG = "OppoEnvironment";
    private static String externalSdDir;
    private static String internalSdDir;
    private static StorageManager mStorageManager;

    private static void update(Context context) {
        StorageVolume[] volumes;
        mStorageManager = (StorageManager) context.getSystemService("storage");
        StorageManager storageManager = mStorageManager;
        if (storageManager != null && (volumes = storageManager.getVolumeList()) != null) {
            for (int i = 0; i < volumes.length; i++) {
                if (volumes[i].isRemovable()) {
                    externalSdDir = volumes[i].getPath();
                } else {
                    internalSdDir = volumes[i].getPath();
                }
            }
        }
    }

    public static File getInternalSdDirectory(Context context) {
        update(context);
        String str = internalSdDir;
        if (str == null) {
            return null;
        }
        return new File(str);
    }

    public static File getExternalSdDirectory(Context context) {
        update(context);
        String str = externalSdDir;
        if (str == null) {
            return null;
        }
        return new File(str);
    }

    public static String getInternalSdState(Context context) {
        update(context);
        String str = internalSdDir;
        if (str == null) {
            return null;
        }
        return mStorageManager.getVolumeState(str);
    }

    public static String getExternalSdState(Context context) {
        update(context);
        String str = externalSdDir;
        if (str == null) {
            return null;
        }
        return mStorageManager.getVolumeState(str);
    }

    public static boolean isExternalSDRemoved(Context context) {
        update(context);
        String str = externalSdDir;
        if (str == null) {
            return true;
        }
        return Environment.MEDIA_REMOVED.equals(mStorageManager.getVolumeState(str));
    }

    static File getDirectory(String variableName, String defaultPath) {
        File file;
        String path = System.getenv(variableName);
        if (path != null) {
            file = new File(path);
        }
        return file;
    }
}
