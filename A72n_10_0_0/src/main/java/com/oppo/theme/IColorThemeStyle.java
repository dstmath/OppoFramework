package com.oppo.theme;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;

public interface IColorThemeStyle extends IOppoCommonFeature {
    public static final IColorThemeStyle DEFAULT = new IColorThemeStyle() {
        /* class com.oppo.theme.IColorThemeStyle.AnonymousClass1 */
    };
    public static final String NAME = "IColorThemeStyle";

    @Override // android.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorThemeStyle;
    }

    @Override // android.common.IOppoCommonFeature
    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default int getSystemThemeStyle(int theme) {
        return theme;
    }

    default int getDialogThemeStyle(int theme) {
        return theme;
    }
}
