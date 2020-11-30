package com.android.server.wm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.os.Bundle;

public interface IColorBreenoManager extends IOppoCommonFeature {
    public static final IColorBreenoManager DEFAULT = new IColorBreenoManager() {
        /* class com.android.server.wm.IColorBreenoManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorBreenoManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorBreenoManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorWindowManagerServiceEx wms) {
    }

    default boolean isBreeno() {
        return false;
    }

    default boolean inDragWindowing() {
        return false;
    }

    default boolean hasColorDragWindowAnimation() {
        return false;
    }

    default boolean stepAnimation(long currentTime) {
        return false;
    }

    default void startColorDragWindow(String packageName, int resId, int mode, Bundle options) {
    }

    default void setBreenoState(String winName) {
    }

    default boolean canMagnificationSpec(WindowState win) {
        return false;
    }

    default void recoveryState() {
    }
}
