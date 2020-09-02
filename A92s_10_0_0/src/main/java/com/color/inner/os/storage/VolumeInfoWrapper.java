package com.color.inner.os.storage;

import android.os.storage.DiskInfo;
import android.os.storage.VolumeInfo;
import java.io.File;

public class VolumeInfoWrapper {
    public static final int STATE_MOUNTED = 2;
    private static final String TAG = "VolumeInfoWrapper";
    private VolumeInfo mVolumeInfo;

    private VolumeInfoWrapper() {
    }

    public VolumeInfoWrapper(VolumeInfo vol) {
        this.mVolumeInfo = vol;
    }

    public String getId() {
        return this.mVolumeInfo.getId();
    }

    public File getPath() {
        return this.mVolumeInfo.getPath();
    }

    public String getStringPath() {
        return this.mVolumeInfo.path;
    }

    public String getFsType() {
        return this.mVolumeInfo.fsType;
    }

    public String getFsUuid() {
        return this.mVolumeInfo.getFsUuid();
    }

    public boolean isSd() {
        DiskInfo diskInfo = this.mVolumeInfo.getDisk();
        if (diskInfo == null || !diskInfo.isSd()) {
            return false;
        }
        return true;
    }
}
