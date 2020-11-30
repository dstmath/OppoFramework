package com.android.server;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;

public interface IColorSmartDozeHelper extends IOppoCommonFeature {
    public static final IColorSmartDozeHelper DEFAULT = new IColorSmartDozeHelper() {
        /* class com.android.server.IColorSmartDozeHelper.AnonymousClass1 */
    };
    public static final String NAME = "IColorSmartDozeHelper";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorSmartDozeHelper;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init() {
    }

    default boolean isInSmartDozeEearlyTime() {
        return false;
    }

    default void enterSmartDozeIfNeeded(String reason) {
    }

    default void moveGpsExemption(boolean state) {
    }

    default void exitAlarmExemption() {
    }

    default boolean isInSmartDozeMotionMaintance() {
        return false;
    }

    default void exitSmartDoze() {
    }

    default boolean isInSmartDozeMode() {
        return false;
    }

    default boolean isSupportSmartDoze() {
        return false;
    }
}
