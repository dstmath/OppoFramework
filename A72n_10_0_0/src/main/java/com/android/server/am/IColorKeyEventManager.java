package com.android.server.am;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.os.IColorKeyEventObserver;

public interface IColorKeyEventManager extends IOppoCommonFeature {
    public static final IColorKeyEventManager DEFAULT = new IColorKeyEventManager() {
        /* class com.android.server.am.IColorKeyEventManager.AnonymousClass1 */
    };
    public static final String NAME = "ColorKeyEventManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorKeyEventManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default boolean registerKeyEventObserver(String observerFingerPrint, IColorKeyEventObserver observer, int listenFlag) {
        return false;
    }

    default boolean unregisterKeyEventObserver(String observerFingerPrint) {
        return false;
    }
}
