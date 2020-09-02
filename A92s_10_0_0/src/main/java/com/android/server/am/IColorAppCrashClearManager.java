package com.android.server.am;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import com.android.server.wm.IColorActivityRecordEx;

public interface IColorAppCrashClearManager extends IOppoCommonFeature {
    public static final IColorAppCrashClearManager DEFAULT = new IColorAppCrashClearManager() {
        /* class com.android.server.am.IColorAppCrashClearManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorAppCrashClearManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorAppCrashClearManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorActivityManagerServiceEx amsEx) {
    }

    default void collectCrashInfo(IColorActivityRecordEx record) {
    }

    default void resetStartTime(IColorActivityRecordEx record) {
    }

    default void clearAppUserData(ProcessRecord app) {
    }
}
