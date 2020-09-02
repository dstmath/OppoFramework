package com.android.server.notification;

import android.app.Notification;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import com.android.server.notification.NotificationManagerService;

public interface IColorNotificationManagerServiceInner {
    default PackageManager getPackageManager() {
        return null;
    }

    default RankingHelper getRankingHelper() {
        return null;
    }

    default Handler getWorkerHandler() {
        return null;
    }

    default boolean isTelevision() {
        return false;
    }

    default NotificationUsageStats getNotificationUsageStats() {
        return null;
    }

    default NotificationManagerService.NotificationListeners getNotificationListeners() {
        return null;
    }

    default SnoozeHelper getSnoozeHelper() {
        return null;
    }

    default String getSoundNotificationKey() {
        return null;
    }

    default String getVibrateNotificationKey() {
        return null;
    }

    default boolean isUseAttentionLight() {
        return false;
    }

    default void setSoundNotificationKey(String key) {
    }

    default void setVibrateNotificationKey(String key) {
    }

    default long[] getFallbackVibrationPattern() {
        return null;
    }

    default void checkCallerIsSameApp(String pkg) {
    }

    default void checkCallerIsSystemOrSameApp(String pkg) {
    }

    default void checkCallerIsSystem() {
    }

    default void doChannelWarningToast(CharSequence toastText) {
    }

    default boolean checkDisqualifyingFeatures(int userId, int callingUid, int id, String tag, NotificationRecord r, boolean isAutogroup) {
        return false;
    }

    default boolean isNotificationForCurrentUser(NotificationRecord record) {
        return false;
    }

    default boolean playSound(NotificationRecord record, Uri soundUri) {
        return false;
    }

    default boolean playVibration(NotificationRecord record, long[] vibration, boolean delayVibForSound) {
        return false;
    }

    default void clearSoundLocked() {
    }

    default void clearVibrateLocked() {
    }

    default boolean removeFromNotificationListsLocked(NotificationRecord r) {
        return false;
    }

    default void cancelNotificationLocked(NotificationRecord r, boolean sendDelete, int reason, int rank, int count, boolean wasPosted, String listenerName) {
    }

    default void updateNotificationPulse() {
    }

    default IBinder getWhitelistToken() {
        return null;
    }

    default void checkRestrictedCategories(Notification notification) {
    }

    default PreferencesHelper getPreferencesHelper() {
        return null;
    }
}
