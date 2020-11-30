package com.android.server.display;

import android.common.OppoFeatureList;
import android.os.Bundle;

public interface IColorDisplayManagerServiceEx extends IOppoDisplayManagerServiceEx {
    public static final IColorDisplayManagerServiceEx DEFAULT = new IColorDisplayManagerServiceEx() {
        /* class com.android.server.display.IColorDisplayManagerServiceEx.AnonymousClass1 */
    };
    public static final String NAME = "IColorDisplayManagerServiceEx";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorDisplayManagerServiceEx;
    }

    default IColorDisplayManagerServiceEx getDefault() {
        return DEFAULT;
    }

    default boolean setStateChanged(int msgId, Bundle extraData) {
        return false;
    }
}
