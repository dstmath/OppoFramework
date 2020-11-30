package com.android.server.am;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;

public interface IColorKeyLayoutManager extends IOppoCommonFeature {
    public static final IColorKeyLayoutManager DEFAULT = new IColorKeyLayoutManager() {
        /* class com.android.server.am.IColorKeyLayoutManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorKeyLayoutManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorKeyLayoutManager;
    }

    default IColorKeyLayoutManager getDefault() {
        return DEFAULT;
    }

    default void init(ActivityManagerService ams) {
    }

    default void setGimbalLaunchPkg(String pkgName) {
    }
}
