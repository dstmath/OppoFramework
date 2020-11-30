package com.android.server.power;

public interface IColorWakeLockEx {
    long getActiveSince();

    String getLockLevelStringEx();

    int getOwnerUid();

    String getPackageName();

    String getTagName();

    long getTotalTime();

    int getWakeLockFlags();

    boolean isDisable();

    boolean isDisabledByHans();
}
