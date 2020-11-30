package com.android.server.power;

import android.common.OppoFeatureList;
import android.util.Slog;

public interface IPswPowerManagerServiceEx extends IOppoPowerManagerServiceEx {
    public static final IPswPowerManagerServiceEx DEFAULT = new IPswPowerManagerServiceEx() {
        /* class com.android.server.power.IPswPowerManagerServiceEx.AnonymousClass1 */
    };
    public static final String NAME = "IPswPowerManagerServiceEx";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IPswPowerManagerServiceEx;
    }

    default IPswPowerManagerServiceEx getDefault() {
        return DEFAULT;
    }

    default void initOppoNwPowerStateManager() {
        Slog.d(NAME, "default initOppoNwPowerStateManager");
    }
}
