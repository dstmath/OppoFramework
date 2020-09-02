package com.android.server.wm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import android.content.res.Configuration;

public interface IColorWatermarkManager extends IOppoCommonFeature {
    public static final IColorWatermarkManager DEFAULT = new IColorWatermarkManager() {
        /* class com.android.server.wm.IColorWatermarkManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorWatermarkManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorWatermarkManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorWindowManagerServiceEx wms) {
    }

    default boolean shouldShowTalkbackWatermark(Context context) {
        return false;
    }

    default void createTalkbackWatermark() {
    }

    default void showWatermarkIfNeeded(boolean flag) {
    }

    default void draw() {
    }

    default void onConfigurationChanged(Configuration configuration) {
    }

    default void positionSurface(int defaultDw, int defaultDh) {
    }
}
