package com.android.server.power;

import android.common.OppoFeatureList;
import android.content.Context;

public interface IColorPowerManagerServiceEx extends IOppoPowerManagerServiceEx {
    public static final IColorPowerManagerServiceEx DEFAULT = new IColorPowerManagerServiceEx() {
        /* class com.android.server.power.IColorPowerManagerServiceEx.AnonymousClass1 */
    };
    public static final String NAME = "IColorPowerManagerServiceEx";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorPowerManagerServiceEx;
    }

    default IColorPowerManagerServiceEx getDefault() {
        return DEFAULT;
    }

    default Context getContext() {
        return null;
    }
}
