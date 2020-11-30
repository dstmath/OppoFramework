package com.android.server.locksettings;

public interface IOppoLockSettingsInner {
    public static final IOppoLockSettingsInner DEFAULT = new IOppoLockSettingsInner() {
        /* class com.android.server.locksettings.IOppoLockSettingsInner.AnonymousClass1 */
    };

    default long getSyntheticPasswordHandle(int userId) {
        return 0;
    }

    default boolean isSyntheticPasswordBasedCredential(int userId) {
        return false;
    }
}
