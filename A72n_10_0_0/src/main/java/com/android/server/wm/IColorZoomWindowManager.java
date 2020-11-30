package com.android.server.wm;

import android.app.ActivityOptions;
import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.MergedConfiguration;
import android.view.DisplayInfo;
import android.view.InputWindowHandle;
import android.view.animation.Animation;
import com.android.server.am.IColorActivityManagerServiceEx;
import com.color.zoomwindow.ColorZoomWindowInfo;
import com.color.zoomwindow.ColorZoomWindowRUSConfig;
import com.color.zoomwindow.IColorZoomWindowObserver;
import java.util.ArrayList;
import java.util.List;

public interface IColorZoomWindowManager extends IOppoCommonFeature {
    public static final IColorZoomWindowManager DEFAULT = new IColorZoomWindowManager() {
        /* class com.android.server.wm.IColorZoomWindowManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorZoomWindowManager";
    public static final int TRANSIT_ZOOM_CLOSE = 101;
    public static final int TRANSIT_ZOOM_OPEN = 100;
    public static final int WINDOW_STATE_INVALID = -1;
    public static final int WINDOW_STATE_ZOOM = 2;
    public static final boolean sDebugfDetail = SystemProperties.getBoolean("persist.sys.assert.panic", false);

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorZoomWindowManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorActivityManagerServiceEx amsEx, IColorActivityTaskManagerServiceEx atmEx) {
    }

    default void init(IColorWindowManagerServiceEx wmsEx) {
    }

    default int startZoomWindow(Intent intent, Bundle options, int userId, String callPkg) {
        return -1;
    }

    default boolean registerZoomWindowObserver(IColorZoomWindowObserver observer) {
        return false;
    }

    default boolean unregisterZoomWindowObserver(IColorZoomWindowObserver observer) {
        return false;
    }

    default ColorZoomWindowInfo getCurrentZoomWindowState() {
        return new ColorZoomWindowInfo();
    }

    default void setBubbleMode(boolean inBubbleMode) {
    }

    default void hideZoomWindow(int flag) {
    }

    default List<String> getZoomAppConfigList(int type) {
        return new ArrayList();
    }

    default ColorZoomWindowRUSConfig getZoomWindowConfig() {
        return new ColorZoomWindowRUSConfig();
    }

    default void setZoomWindowConfig(ColorZoomWindowRUSConfig config) {
    }

    default boolean isSupportZoomWindowMode() {
        return true;
    }

    default void shouldBeVisible(boolean stackShouldBeVisible, ActivityStack stack, ActivityRecord top) {
    }

    default void updateZoomStack(ActivityStack stack, ActivityOptions options, ActivityRecord startActivity, ActivityRecord sourceRecord) {
    }

    default void pauseZoomWindowActivity() {
    }

    default void notifyZoomActivityShown(ActivityRecord record) {
    }

    default void notifyWindowDied(ActivityStack stack) {
    }

    default Rect getTaskBound() {
        return new Rect();
    }

    default boolean updateZoonWindowTaskBound(Configuration config, ActivityStack stack) {
        return false;
    }

    default void windowZoomFrame(Rect pf, Rect df, Rect cf, Rect vf, WindowState win) {
    }

    default void updateInputVisibility(WindowState win, boolean updateBound) {
    }

    default Rect getScaledTaskBound() {
        return new Rect();
    }

    default int clearWindowFlagsIfNeed(WindowState win, int curFlags) {
        return curFlags;
    }

    default void handleTapOutsideTask(DisplayContent displayContent, Task task, int x, int y) {
    }

    default void amendWindowTapExcludeRegion(DisplayContent displayContent, Region region) {
    }

    default void prepareSurfaceFromDim(DisplayContent dc) {
    }

    default AnimationAdapter getZoomAnimationAdapter(AnimationAdapter adapter, AppWindowToken appToken, Animation a, int appStackClipMode, Rect transitStartRect, int transit, boolean enter) {
        return adapter;
    }

    default void adjustWindowCropForLeash(AppWindowToken appToken, Rect windowCrop) {
    }

    default void prepareZoomTransition(ActivityStack curStack, ActivityStack nextStack) {
    }

    default void adjustInputWindowHandle(InputMonitor monitor, WindowState win, InputWindowHandle handle) {
    }

    default void adjustWindowFrame(WindowState win, WindowFrames windowFrames) {
    }

    default int getWindowState(ActivityStack stack) {
        return -1;
    }

    default boolean validateWindowingMode(int windowingMode, ActivityRecord r, TaskRecord task, int activityType) {
        return false;
    }

    default void addTapExcluedWindow(WindowState win) {
    }

    default void removeTapExcluedWindow(WindowState win) {
    }

    default void gestureSwipeFromBottom() {
    }

    default void onAnimationFinished(ActivityRecord r) {
    }

    default void topResumedActivityChanged(ActivityRecord r) {
    }

    default void displayChanged(DisplayContent dc) {
    }

    default void debugLogUtil(String key, Object... vars) {
    }

    default DisplayInfo getZoomModeDisplayInfo(DisplayInfo info, int displayId, int callingUid) {
        return info;
    }

    default boolean shouldClearReusedActivity(ActivityRecord reusedActivity, ActivityOptions options, ActivityRecord startActivity, MergedConfiguration lastReportedConfiguration) {
        return false;
    }

    default boolean shouldIgnoreInputShownForResult(int uid, int displayId) {
        return false;
    }

    static boolean excludeWindowTypeFromTapOutTask(int windowType) {
        if (windowType == 2003 || windowType == 2038 || windowType == 2318 || windowType == 2314 || windowType == 2315) {
            return true;
        }
        return false;
    }
}
