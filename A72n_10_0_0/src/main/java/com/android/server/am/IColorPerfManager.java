package com.android.server.am;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.os.Bundle;
import java.util.List;

public interface IColorPerfManager extends IOppoCommonFeature {
    public static final IColorPerfManager DEFAULT = new IColorPerfManager() {
        /* class com.android.server.am.IColorPerfManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorPerfManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorPerfManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorActivityManagerServiceEx amsEx) {
    }

    default boolean dumpProcPerfData(Bundle bundle) {
        return false;
    }

    default List<String> getProcCommonInfoList(int type) {
        return null;
    }
}
