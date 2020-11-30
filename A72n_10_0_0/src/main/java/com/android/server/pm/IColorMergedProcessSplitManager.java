package com.android.server.pm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;

public interface IColorMergedProcessSplitManager extends IOppoCommonFeature {
    public static final IColorMergedProcessSplitManager DEFAULT = new IColorMergedProcessSplitManager() {
        /* class com.android.server.pm.IColorMergedProcessSplitManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorMergedProcessSplitManager";

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorMergedProcessSplitManager;
    }

    default void init() {
    }

    default boolean isAppProcessNeedSplit(String processName) {
        return false;
    }
}
