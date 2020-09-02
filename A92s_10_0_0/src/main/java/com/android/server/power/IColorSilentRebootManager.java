package com.android.server.power;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.os.Handler;

public interface IColorSilentRebootManager extends IOppoCommonFeature {
    public static final IColorSilentRebootManager DEFAULT = new IColorSilentRebootManager() {
        /* class com.android.server.power.IColorSilentRebootManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorSilentRebootManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorSilentRebootManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void postProcessBlackLightTask(Handler hanlder) {
    }

    default void init(IColorPowerManagerServiceEx pmsEx) {
    }
}
