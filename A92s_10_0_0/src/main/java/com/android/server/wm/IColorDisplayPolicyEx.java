package com.android.server.wm;

import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Handler;
import android.view.IApplicationToken;
import com.android.server.statusbar.StatusBarManagerInternal;

public interface IColorDisplayPolicyEx extends IOppoCommonFeature {
    public static final IColorDisplayPolicyEx DEFAULT = new IColorDisplayPolicyEx() {
        /* class com.android.server.wm.IColorDisplayPolicyEx.AnonymousClass1 */
    };
    public static final String NAME = "IColorDisplayPolicyEx";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorDisplayPolicyEx;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default boolean isNavGestureMode() {
        return false;
    }

    default void updateNavigationBarHideState() {
    }

    default boolean isNavBarHidden() {
        return false;
    }

    default void loadGestureBarHeight(Resources res, int portraitRotation, int upsideDownRotation, int landscapeRotation, int seascapeRotation) {
    }

    default int caculateDisplayFrame(int position, Rect cutoutSafeUnrestricted, int def) {
        return def;
    }

    default void updateNavigationFrame(int position, int rotation, Rect navigationFrame, Rect cutoutSafeUnrestricted) {
    }

    default void updateGestureStatus() {
    }

    default boolean isNavBarImmersive() {
        return false;
    }

    default boolean getTopActivityIsFullscreen() {
        return false;
    }

    default void topActivityStatusChanged(boolean topActivityIsFullscreen) {
    }

    default boolean isStatusBarExpanded(WindowState statusBar) {
        return false;
    }

    default void handleMultiWindowFocusChanged(WindowState focusedWindow, IApplicationToken focusedApp, Handler handler) {
    }

    default WindowState getTopDockedWindowState() {
        return null;
    }

    default void setTopDockedWindowState(WindowState window) {
    }

    default void setSystemUiVisibility(StatusBarManagerInternal statusBar, int displayId, int vis, int fullscreenStackVis, int dockedStackVis, int mask, Rect fullscreenBounds, Rect dockedBounds, boolean isNavbarColorManagedByIme, WindowState window, int navBarVis) {
    }

    default int getNavBarVisibility(boolean isKeyguard, boolean forceShowNavBar, boolean screenOn) {
        return 0;
    }

    default void notifyWindowStateChange(int visibility) {
    }

    default boolean isWindowTitleEquals(WindowState win, String title) {
        return win != null && win.getAttrs().getTitle().equals(title);
    }

    default int getSysUiFlagsForSplitScreen(WindowState win, int sysUiFlag) {
        return sysUiFlag;
    }

    default boolean isGameDockWindow(WindowState win) {
        return false;
    }

    default void layoutGameDockWindowLw(DisplayFrames displayFrames, Rect cf, Rect of, Rect df, Rect pf) {
    }

    default boolean willSkipSystemUI(String title) {
        return false;
    }

    default boolean isIncludedNextPage(WindowState win) {
        return false;
    }
}
