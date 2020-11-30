package com.android.server.wm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.pm.ActivityInfo;

public interface IColorAppPhoneManager extends IOppoCommonFeature {
    public static final IColorAppPhoneManager DEFAULT = new IColorAppPhoneManager() {
        /* class com.android.server.wm.IColorAppPhoneManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorAppPhoneManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorAppPhoneManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorActivityTaskManagerServiceEx atms) {
    }

    default boolean isAppPhoneRefuseMode() {
        return false;
    }

    default void setAppPhoneRefuseMode(boolean mode) {
    }

    default boolean handleAppPhoneComing(ActivityInfo aInfo) {
        return false;
    }
}
