package com.android.server.power;

import android.os.IBinder;
import com.android.server.power.PowerManagerService;
import java.util.ArrayList;

public interface IColorPowerManagerServiceInner {
    public static final IColorPowerManagerServiceInner DEFAULT = new IColorPowerManagerServiceInner() {
        /* class com.android.server.power.IColorPowerManagerServiceInner.AnonymousClass1 */
    };

    default boolean isInteractiveInternal() {
        return false;
    }

    default void setUserActivityValue(long value) {
    }

    default long getUserActivityValue() {
        return 0;
    }

    default int getMsgUserActivityTimeoutValue() {
        return 0;
    }

    default long getSleepTimeoutLocked() {
        return 0;
    }

    default long getScreenOffTimeoutLocked(long sleepTimeout) {
        return 0;
    }

    default long getScreenOffTimeoutSettingValue() {
        return 0;
    }

    default ArrayList<IColorWakeLockEx> getArrayListOfWakeLocks() {
        return null;
    }

    default void setKeyguardLockEverUnlockValue(Boolean value) {
    }

    default long getLastWakeTime() {
        return 0;
    }

    default long getLastSleepTime() {
        return 0;
    }

    default void releaseWakeLockInternal(IBinder lock, int flags) {
    }

    default PowerManagerService.WakeLock cloneWakeLock(PowerManagerService.WakeLock lock) {
        return null;
    }

    default void goToSleepInternal(long eventTime, int reason, int flags, int uid) {
    }

    default int getwakefulness() {
        return 0;
    }

    default boolean needScreenOnWakelockCheck() {
        return false;
    }

    default boolean isForgroundUid(int uid) {
        return false;
    }

    default int getUserActivitySummary() {
        return 0;
    }

    default boolean getDisabledByHans(PowerManagerService.WakeLock wakeLock) {
        return false;
    }

    default void setDisableByHans(PowerManagerService.WakeLock wakeLock, boolean value) {
    }

    default boolean setWakeLockDisabledStateLocked(PowerManagerService.WakeLock wakeLock) {
        return false;
    }

    default void notifyWakeLockReleasedLocked(PowerManagerService.WakeLock wakeLock) {
    }

    default void notifyWakeLockAcquiredLocked(PowerManagerService.WakeLock wakeLock) {
    }

    default void updatePowerStateLocked() {
    }

    default void updateDirtyByHans() {
    }
}
