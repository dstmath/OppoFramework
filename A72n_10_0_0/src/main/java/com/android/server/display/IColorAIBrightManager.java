package com.android.server.display;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.os.Bundle;
import android.os.RemoteException;

public interface IColorAIBrightManager extends IOppoCommonFeature {
    public static final IColorAIBrightManager DEFAULT = new IColorAIBrightManager() {
        /* class com.android.server.display.IColorAIBrightManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorAIBrightManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorAIBrightManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(DisplayManagerService dms) {
    }

    default boolean setStateChanged(int msgId, Bundle extraData) throws RemoteException {
        return false;
    }
}
