package com.android.server;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.os.Handler;
import android.os.Message;
import android.os.storage.DiskInfo;
import android.os.storage.VolumeInfo;
import android.util.Log;

public interface IOppoStorageManagerFeature extends IOppoCommonFeature {
    public static final IOppoStorageManagerFeature DEFAULT = new IOppoStorageManagerFeature() {
        /* class com.android.server.IOppoStorageManagerFeature.AnonymousClass1 */
    };
    public static final int H_FSTRIM = 4;
    public static final int H_SHUTDOWN = 3;
    public static final int H_VOLUME_MOUNT = 5;
    public static final String NAME = "IOppoStorageManagerFeature";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IOppoStorageManagerFeature;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void setStorageManagerHandler(Handler handler) {
        Log.d(NAME, "default setStorageManagerHandler");
    }

    default void setOppoStorageManagerCallback(IOppoStorageManagerCallback callback) {
        Log.d(NAME, "default setOppoStorageManagerCallback");
    }

    default boolean shouldHandleKeyguardStateChange(boolean isSecureKeyguardShowing) {
        Log.d(NAME, "default shouldHandleKeyguardStateChange");
        return false;
    }

    default boolean changeVolumeReadOnlyStateLocked(VolumeInfo vol, int newState, int unlockedUsersSize) {
        Log.d(NAME, "default changeVolumeReadOnlyStateLocked");
        return false;
    }

    default boolean shouldNotifyVolumeStateChanged(String newStateEnv, int userId, VolumeInfo vol) {
        Log.d(NAME, "default shouldNotifyVolumeStateChanged");
        return false;
    }

    default void onVolumeCheckingLocked(VolumeInfo vol, int currentUserId) {
        Log.d(NAME, "default onVolumeCheckingLocked");
    }

    default void onUnlockUser(int userId) {
        Log.d(NAME, "default onUnlockUser");
    }

    default boolean onStorageManagerMessageHandle(Message msg) {
        Log.d(NAME, "default onStorageManagerMessageHandle");
        return true;
    }

    default void onDiskStateChangedLocked(DiskInfo disk, int volumesSize, int unlockedUsersSize) {
        Log.d(NAME, "default onDiskStateChanged");
    }
}
