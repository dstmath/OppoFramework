package com.android.server.pm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;

public interface IColorExpDefaultBrowserManager extends IOppoCommonFeature {
    public static final IColorExpDefaultBrowserManager DEFAULT = new IColorExpDefaultBrowserManager() {
        /* class com.android.server.pm.IColorExpDefaultBrowserManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorExpDefaultBrowserManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorExpDefaultBrowserManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorPackageManagerServiceEx ex) {
    }

    default void setExpDefaultBrowser() {
    }
}
