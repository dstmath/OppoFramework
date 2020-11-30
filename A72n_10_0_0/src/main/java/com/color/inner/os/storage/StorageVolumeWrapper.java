package com.color.inner.os.storage;

import android.os.storage.OppoBaseStorageVolume;
import android.os.storage.StorageVolume;
import android.util.Log;
import com.color.util.ColorTypeCastingHelper;

public class StorageVolumeWrapper {
    private static final String TAG = "StorageVolumeWrapper";

    private StorageVolumeWrapper() {
    }

    public static String getPath(StorageVolume storageVolume) {
        try {
            return storageVolume.getPath();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static int getReadOnlyType(StorageVolume storageVolume) {
        try {
            return typeCasting(storageVolume).getReadOnlyType();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }

    public static int getFatVolumeId(StorageVolume storageVolume) {
        return storageVolume.getFatVolumeId();
    }

    private static OppoBaseStorageVolume typeCasting(StorageVolume storageVolume) {
        return (OppoBaseStorageVolume) ColorTypeCastingHelper.typeCasting(OppoBaseStorageVolume.class, storageVolume);
    }
}
