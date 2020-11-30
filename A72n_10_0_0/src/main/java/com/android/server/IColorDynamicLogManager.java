package com.android.server;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import java.io.PrintWriter;

public interface IColorDynamicLogManager extends IOppoCommonFeature {
    public static final IColorDynamicLogManager DEFAULT = new IColorDynamicLogManager() {
        /* class com.android.server.IColorDynamicLogManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorDynamicLogManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorDynamicLogManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void handleDynamicLog(PrintWriter pw, String[] args, int opti) {
    }
}
