package com.oppo.screenmode;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.CompatibilityInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DisplayInfo;
import android.view.View;

public interface IPswScreenModeFeature extends IOppoCommonFeature {
    public static final IPswScreenModeFeature DEFAULT = new IPswScreenModeFeature() {
        /* class com.oppo.screenmode.IPswScreenModeFeature.AnonymousClass1 */
    };
    public static final String NAME = "IPswScreenModeFeature";

    @Override // android.common.IOppoCommonFeature
    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IPswScreenModeFeature;
    }

    @Override // android.common.IOppoCommonFeature
    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(Context context) {
        Log.d(NAME, "default init");
    }

    default void setRefreshRate(IBinder token, int rate) {
    }

    default void setRefreshRate(View view, int rate) {
    }

    default boolean requestRefreshRate(boolean open, int rate) {
        return false;
    }

    default boolean setHighTemperatureStatus(int status, int rate) {
        return false;
    }

    default void enterDCAndLowBrightnessMode(boolean enter) {
    }

    default boolean isDisplayCompat(String packageName, int uid) {
        return false;
    }

    default void enterPSMode(boolean enter) {
    }

    default void enterPSModeOnRate(boolean enter, int rate) {
    }

    default boolean getGameList(Bundle outBundle) {
        return false;
    }

    default void overrideDisplayMetricsIfNeed(DisplayMetrics inoutDm) {
    }

    default void applyCompatInfo(CompatibilityInfo compatInfo, DisplayMetrics outMetrics) {
    }

    default void updateCompatDensityIfNeed(int density) {
    }

    default boolean supportDisplayCompat(String pkg, int uid) {
        return false;
    }

    default boolean supportDisplayCompat() {
        return false;
    }

    default int displayCompatDensity(int density) {
        return density;
    }

    default void setSupportDisplayCompat(boolean support) {
    }

    default void initDisplayCompat(ApplicationInfo appInfo) {
    }

    default void updateCompatRealSize(DisplayInfo displayInfo, Point outSize) {
    }
}
