package com.android.server;

import android.os.storage.VolumeInfo;

public interface IOppoStorageManagerCallback {
    int getSystemUnlockedUserIdByIndexLocked(int i);

    VolumeInfo getVolumeInfoByIndexLocked(int i);

    void onCheckBeforeMount(String str);
}
