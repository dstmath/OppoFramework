package com.android.server;

import android.common.IOppoCommonFeature;

public interface IOppoSystemServerEx extends IOppoCommonFeature {
    default void startBootstrapServices() {
    }

    default void startCoreServices() {
    }

    default void startOtherServices() {
    }

    default void systemReady() {
    }

    default void systemRunning() {
    }
}
