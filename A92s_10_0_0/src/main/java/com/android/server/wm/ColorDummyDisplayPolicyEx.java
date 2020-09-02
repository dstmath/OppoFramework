package com.android.server.wm;

import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Handler;
import android.view.IApplicationToken;
import com.android.server.statusbar.StatusBarManagerInternal;
import com.color.util.ColorTypeCastingHelper;

public class ColorDummyDisplayPolicyEx implements IColorDisplayPolicyEx {
    public static final String KEY_NAVIGATIONBAR_HIDDEN = "oppo.hide.navigationbar";
    public static final String KEY_NAVIGATIONBAR_MODE = "hide_navigationbar_enable";
    public static final String KEY_NAV_BAR_HIDE_STATE = "manual_hide_navigationbar";
    public static final String KEY_NAV_BAR_IMMERSIVE = "nav_bar_immersive";
    protected static final int MODE_NAVIGATIONBAR = 0;
    protected static final int MODE_NAVIGATIONBAR_GESTURE = 2;
    protected static final int MODE_NAVIGATIONBAR_GESTURE_SIDE = 3;
    protected static final int MODE_NAVIGATIONBAR_NONE = -1;
    protected static final int MODE_NAVIGATIONBAR_WITH_HIDE = 1;
    public static final int NAV_BAR_HIDE_STATE_HIDE = 1;
    public static final int NAV_BAR_HIDE_STATE_NONE = -1;
    public static final int NAV_BAR_HIDE_STATE_SHOW = 0;
    public static final String SYSTEM_BAR_ID = "systembar_id";
    public static final String SYSTEM_BAR_STATE = "systembar_state";
    public static final int SYSTEM_UI_FLAG_FOCUS_TOP_OR_LEFT = 64;
    protected static final Rect mTmpNavigationFrameForGesture = new Rect();
    OppoBaseDisplayPolicy mBaseDp;
    IColorDisplayPolicyInner mColorDpInner;
    final DisplayPolicy mDisplayPolicy;
    protected boolean mHideNavigationBar = false;
    protected boolean mIsNavBarImmersive = false;
    protected int mLastNavigationBarHideState = -1;
    protected int mLastNavigationBarMode = -1;
    protected int mLastNavigationBarState = 0;
    protected int mLastWindowFocusFlags;
    protected int[] mNavigationBarHeightForRotationGestrue = new int[4];
    protected int mNavigationBarHideState = 0;
    protected int mNavigationBarMode = 0;
    protected OppoWindowManagerInternal mOppoWindowManagerInternal;
    protected boolean mTopActivityIsFullscreen = false;
    public WindowState mTopDockedWindowState = null;

    public ColorDummyDisplayPolicyEx(DisplayPolicy displayPolicy) {
        this.mDisplayPolicy = displayPolicy;
        this.mBaseDp = (OppoBaseDisplayPolicy) ColorTypeCastingHelper.typeCasting(OppoBaseDisplayPolicy.class, displayPolicy);
        OppoBaseDisplayPolicy oppoBaseDisplayPolicy = this.mBaseDp;
        if (oppoBaseDisplayPolicy != null) {
            this.mColorDpInner = oppoBaseDisplayPolicy.mColorDpInner;
        }
    }

    @Override // com.android.server.wm.IColorDisplayPolicyEx
    public boolean isNavGestureMode() {
        return false;
    }

    @Override // com.android.server.wm.IColorDisplayPolicyEx
    public void updateNavigationBarHideState() {
    }

    @Override // com.android.server.wm.IColorDisplayPolicyEx
    public boolean isNavBarHidden() {
        return false;
    }

    @Override // com.android.server.wm.IColorDisplayPolicyEx
    public void loadGestureBarHeight(Resources res, int portraitRotation, int upsideDownRotation, int landscapeRotation, int seascapeRotation) {
    }

    @Override // com.android.server.wm.IColorDisplayPolicyEx
    public int caculateDisplayFrame(int position, Rect cutoutSafeUnrestricted, int def) {
        return def;
    }

    @Override // com.android.server.wm.IColorDisplayPolicyEx
    public void updateNavigationFrame(int position, int rotation, Rect navigationFrame, Rect cutoutSafeUnrestricted) {
    }

    @Override // com.android.server.wm.IColorDisplayPolicyEx
    public void updateGestureStatus() {
    }

    @Override // com.android.server.wm.IColorDisplayPolicyEx
    public boolean isNavBarImmersive() {
        return false;
    }

    @Override // com.android.server.wm.IColorDisplayPolicyEx
    public boolean getTopActivityIsFullscreen() {
        return false;
    }

    @Override // com.android.server.wm.IColorDisplayPolicyEx
    public void topActivityStatusChanged(boolean topActivityIsFullscreen) {
    }

    @Override // com.android.server.wm.IColorDisplayPolicyEx
    public boolean isStatusBarExpanded(WindowState statusBar) {
        return false;
    }

    @Override // com.android.server.wm.IColorDisplayPolicyEx
    public void handleMultiWindowFocusChanged(WindowState focusedWindow, IApplicationToken focusedApp, Handler handler) {
    }

    @Override // com.android.server.wm.IColorDisplayPolicyEx
    public WindowState getTopDockedWindowState() {
        return null;
    }

    @Override // com.android.server.wm.IColorDisplayPolicyEx
    public void setTopDockedWindowState(WindowState window) {
    }

    @Override // com.android.server.wm.IColorDisplayPolicyEx
    public void setSystemUiVisibility(StatusBarManagerInternal statusBar, int displayId, int vis, int fullscreenStackVis, int dockedStackVis, int mask, Rect fullscreenBounds, Rect dockedBounds, boolean isNavbarColorManagedByIme, WindowState win, int navBarVis) {
        statusBar.setSystemUiVisibility(displayId, vis, fullscreenStackVis, dockedStackVis, -1, fullscreenBounds, dockedBounds, isNavbarColorManagedByIme, win.toString());
    }

    @Override // com.android.server.wm.IColorDisplayPolicyEx
    public int getNavBarVisibility(boolean isKeyguard, boolean forceShowNavBar, boolean screenOn) {
        return 0;
    }

    @Override // com.android.server.wm.IColorDisplayPolicyEx
    public void notifyWindowStateChange(int visibility) {
    }

    @Override // com.android.server.wm.IColorDisplayPolicyEx
    public boolean isWindowTitleEquals(WindowState win, String title) {
        return win != null && win.getAttrs().getTitle().equals(title);
    }

    @Override // com.android.server.wm.IColorDisplayPolicyEx
    public int getSysUiFlagsForSplitScreen(WindowState win, int sysUiFlag) {
        return sysUiFlag;
    }

    @Override // com.android.server.wm.IColorDisplayPolicyEx
    public boolean isGameDockWindow(WindowState win) {
        return false;
    }

    @Override // com.android.server.wm.IColorDisplayPolicyEx
    public void layoutGameDockWindowLw(DisplayFrames displayFrames, Rect cf, Rect of, Rect df, Rect pf) {
    }

    @Override // com.android.server.wm.IColorDisplayPolicyEx
    public boolean willSkipSystemUI(String title) {
        return false;
    }

    @Override // com.android.server.wm.IColorDisplayPolicyEx
    public boolean isIncludedNextPage(WindowState win) {
        return false;
    }
}
