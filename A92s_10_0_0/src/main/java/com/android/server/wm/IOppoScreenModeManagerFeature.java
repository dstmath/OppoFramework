package com.android.server.wm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.IBinder;

public interface IOppoScreenModeManagerFeature extends IOppoCommonFeature {
    public static final boolean DEBUG = false;
    public static final IOppoScreenModeManagerFeature DEFAULT = new IOppoScreenModeManagerFeature() {
        /* class com.android.server.wm.IOppoScreenModeManagerFeature.AnonymousClass1 */
    };
    public static final String NAME = "IOppoScreenModeManagerFeature";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IOppoScreenModeManagerFeature;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(WindowManagerService service, Context context) {
    }

    default void setRefreshRate(IBinder token, int rate) {
    }

    default int getPreferredModeId(WindowState w, int candidateMode) {
        return 0;
    }

    default int updateGlobalModeId(int modeId) {
        return 0;
    }

    default void onSetDensityForUser(int density, int userId) {
    }

    default int adjustDensityForUser(int density, int userId) {
        return density;
    }

    default void setCurrentUser(int userId) {
    }

    default void updateScreenSplitMode(boolean mode) {
    }

    default void startAnimation(boolean start) {
    }

    default void enterDCAndLowBrightnessMode(boolean enter) {
    }

    default void enterPSMode(boolean enter) {
    }

    default void enterPSModeOnRate(boolean enter, int rate) {
    }

    default void overrideCompatInfoIfNeed(ApplicationInfo ai) {
    }

    default boolean isDisplayCompat(String packageName, int uid) {
        return false;
    }

    default float overrideScaleIfNeed(WindowState win) {
        return win.mGlobalScale;
    }
}
