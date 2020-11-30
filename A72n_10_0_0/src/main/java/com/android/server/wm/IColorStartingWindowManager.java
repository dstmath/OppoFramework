package com.android.server.wm;

import android.common.OppoFeatureList;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.view.animation.Animation;
import com.android.internal.policy.PhoneWindow;
import com.android.server.IColorCustomCommonManager;
import com.android.server.policy.WindowManagerPolicy;

public interface IColorStartingWindowManager extends IColorCustomCommonManager {
    public static final IColorStartingWindowManager DEFAULT = new IColorStartingWindowManager() {
        /* class com.android.server.wm.IColorStartingWindowManager.AnonymousClass1 */
    };
    public static final String NAME = "ColorStartingWindowManager";

    default IColorStartingWindowManager getDefault() {
        return DEFAULT;
    }

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorStartingWindowManager;
    }

    default void init(WindowManagerService wms) {
    }

    default void reviseWindowFlagsForStarting(ActivityRecord activityRecord, boolean newTask, boolean taskSwitch, boolean processRunning, boolean fromRecents, boolean activityCreated) {
    }

    default int getStartingWindowType(int defaultTypeNone, int defaultTypeSplash, int defaultTypeSnapshot) {
        return -1;
    }

    default boolean checkSplashWindowFlag() {
        return false;
    }

    default boolean checkAppWindowAnimating(AppWindowToken token) {
        return false;
    }

    default boolean allowUseSnapshot(AppWindowToken token, boolean newTask, boolean taskSwitch, boolean processRunning, boolean activityCreated) {
        return true;
    }

    default void putSnapshot(AppWindowToken token) {
    }

    default BitmapDrawable getAppSnapshotBitmapDrawable(PhoneWindow window) {
        return null;
    }

    default boolean interceptRemoveStartingWindow(String packageName, Handler handler, WindowManagerPolicy.StartingSurface surface) {
        return false;
    }

    default boolean handleDestroySurfaces(String packageName, int type) {
        return false;
    }

    default void clearCacheWhenOnConfigurationChange(int changes) {
    }

    default void setAllowAppSnapshot(boolean allowAppSnapshot) {
    }

    default boolean setStartingWindowExitAnimation(WindowState windowState) {
        return false;
    }

    default Animation createAnimationForLauncherExit() {
        return null;
    }

    default boolean checkSkipTransitionAnimation(DisplayContent displayContent) {
        return false;
    }

    default void preloadAppSplash(int realCallingPid, SafeActivityOptions options, ActivityInfo activityInfo, String reason) {
    }

    default void onStartAppShotcut() {
    }

    default boolean skipAppTransitionAnimation() {
        return false;
    }

    default void setAppTransit(int transit) {
    }

    default int getAppTransit(int originTransit) {
        return originTransit;
    }

    default boolean clearStartingWindowWhenSnapshotDiffOrientation(AppWindowToken token) {
        return false;
    }

    default void reviseWindowFlagsForStarting(ActivityRecord activityRecord, boolean newTask, boolean taskSwitch, boolean fromRecents, boolean activityCreated) {
    }

    default void handleStartingWindow(PhoneWindow window) {
    }

    default int getStartingWindowType(int defaultTypeNone, int defaultTypeSplash) {
        return -1;
    }

    default int getStartingWindowType(int appTransition, String packageName, boolean fromRecents, boolean activityCreated, int defaultTypeSplash, int defaultTypeNone) {
        return -1;
    }

    default int reviseWindowFlagsForStarting(int windowFlags, ActivityRecord activityRecord) {
        return windowFlags;
    }

    default boolean checkSplashWindowFlag(int windowFlags) {
        return false;
    }

    default int resetTransit(int transit) {
        return transit;
    }

    default void preloadAppSplash(IBinder resultTo, ActivityInfo activityInfo) {
    }
}
