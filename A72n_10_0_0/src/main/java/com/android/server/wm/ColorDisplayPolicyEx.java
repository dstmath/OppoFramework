package com.android.server.wm;

import android.app.ColorStatusBarManager;
import android.app.StatusBarManager;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Slog;
import android.view.IApplicationToken;
import android.view.WindowManager;
import com.android.server.LocalServices;
import com.android.server.statusbar.ColorStatusBarManagerInternal;
import com.android.server.statusbar.StatusBarManagerInternal;
import com.color.zoomwindow.ColorZoomWindowManager;
import java.util.ArrayList;
import java.util.Iterator;

public class ColorDisplayPolicyEx extends ColorDummyDisplayPolicyEx {
    private static final String TAG = "ColorDisplayPolicyEx";
    private static ArrayList<String> sIgnoreWindowTitle = new ArrayList<>();
    private ColorStatusBarManagerInternal mColorStatusBarManagerInternal;
    final Runnable mUpdateRotationTrueRunnable = new Runnable() {
        /* class com.android.server.wm.ColorDisplayPolicyEx.AnonymousClass1 */

        public void run() {
            if (ColorDisplayPolicyEx.this.mColorDpInner != null) {
                ColorDisplayPolicyEx.this.mColorDpInner.getWindowManagerService().updateRotation(true, true);
            }
        }
    };

    static {
        sIgnoreWindowTitle.add("ColorOSEdgePanel");
        sIgnoreWindowTitle.add("ColorGameDock");
    }

    public ColorDisplayPolicyEx(DisplayPolicy displayPolicy) {
        super(displayPolicy);
    }

    public boolean isNavGestureMode() {
        if (this.mNavigationBarMode == 2 || this.mNavigationBarMode == 3) {
            return true;
        }
        return false;
    }

    public void updateNavigationBarHideState() {
        boolean z = false;
        if (1 == SystemProperties.getInt("oppo.hide.navigationbar", 0)) {
            z = true;
        }
        this.mHideNavigationBar = z;
    }

    public boolean isNavBarHidden() {
        return this.mHideNavigationBar;
    }

    public boolean isNavBarFollowScreenRotation() {
        Slog.d(TAG, "isNavBarFollowScreenRotation mNavBarPositionFollowScreenRotation " + this.mNavBarPositionFollowScreenRotation + "mNavigationBarMode " + this.mNavigationBarMode);
        if (this.mNavBarPositionFollowScreenRotation == 0 && this.mNavigationBarMode == 2) {
            return false;
        }
        return true;
    }

    public void loadGestureBarHeight(Resources res, int portraitRotation, int upsideDownRotation, int landscapeRotation, int seascapeRotation) {
        int[] iArr = this.mNavigationBarHeightForRotationGestrue;
        int[] iArr2 = this.mNavigationBarHeightForRotationGestrue;
        int[] iArr3 = this.mNavigationBarHeightForRotationGestrue;
        int[] iArr4 = this.mNavigationBarHeightForRotationGestrue;
        int dimensionPixelSize = res.getDimensionPixelSize(201654487);
        iArr4[seascapeRotation] = dimensionPixelSize;
        iArr3[landscapeRotation] = dimensionPixelSize;
        iArr2[upsideDownRotation] = dimensionPixelSize;
        iArr[portraitRotation] = dimensionPixelSize;
    }

    public int caculateDisplayFrame(int position, Rect cutoutSafeUnrestricted, int def) {
        if ((!this.mHideNavigationBar && !this.mIsNavBarImmersive) || !this.mDisplayPolicy.hasNavigationBar()) {
            return def;
        }
        if (position == 1) {
            return cutoutSafeUnrestricted.left;
        }
        if (position == 2) {
            return cutoutSafeUnrestricted.right;
        }
        if (position != 4) {
            return def;
        }
        return cutoutSafeUnrestricted.bottom;
    }

    public void updateNavigationFrame(int position, int rotation, Rect navigationFrame, Rect cutoutSafeUnrestricted) {
        if (isNavGestureMode()) {
            if (position == 1) {
                navigationFrame.set(navigationFrame.left, navigationFrame.top, cutoutSafeUnrestricted.left + getGestureBarHeight(rotation), navigationFrame.bottom);
            } else if (position == 2) {
                navigationFrame.set(cutoutSafeUnrestricted.right - getGestureBarHeight(rotation), navigationFrame.top, navigationFrame.right, navigationFrame.bottom);
            } else if (position == 4) {
                navigationFrame.set(navigationFrame.left, cutoutSafeUnrestricted.bottom - getGestureBarHeight(rotation), navigationFrame.right, navigationFrame.bottom);
            }
        }
    }

    public void updateGestureStatus() {
        this.mNavigationBarMode = Settings.Secure.getIntForUser(this.mDisplayPolicy.getContext().getContentResolver(), "hide_navigationbar_enable", 0, -2);
        this.mIsNavBarImmersive = Settings.Secure.getIntForUser(this.mDisplayPolicy.getContext().getContentResolver(), "nav_bar_immersive", 0, -2) == 1;
        this.mNavigationBarHideState = Settings.Secure.getInt(this.mDisplayPolicy.getContext().getContentResolver(), "manual_hide_navigationbar", 0);
        if (this.mLastNavigationBarMode != this.mNavigationBarMode) {
            this.mLastNavigationBarMode = this.mNavigationBarMode;
            if (this.mColorDpInner != null) {
                this.mColorDpInner.getHandler().post(this.mUpdateRotationTrueRunnable);
            }
        }
        this.mNavBarPositionFollowScreenRotation = Settings.Secure.getInt(this.mDisplayPolicy.getContext().getContentResolver(), "follow_rotation_gesture_bar_enable", 1);
        Slog.d(TAG, "updateGestureStatus " + this.mNavBarPositionFollowScreenRotation);
    }

    public boolean isNavBarImmersive() {
        return this.mIsNavBarImmersive;
    }

    private int getGestureBarHeight(int rotation) {
        return this.mNavigationBarHeightForRotationGestrue[rotation];
    }

    public boolean getTopActivityIsFullscreen() {
        return this.mTopActivityIsFullscreen;
    }

    public void topActivityStatusChanged(boolean topActivityIsFullscreen) {
        try {
            new ColorStatusBarManager().topIsFullscreen(topActivityIsFullscreen);
        } catch (RemoteException e) {
        }
        this.mTopActivityIsFullscreen = topActivityIsFullscreen;
    }

    public boolean isStatusBarExpanded(WindowState statusBar) {
        if (statusBar == null) {
            return false;
        }
        WindowManager.LayoutParams statusBarAttrs = statusBar.getAttrs();
        if (statusBarAttrs.height == -1 && statusBarAttrs.width == -1) {
            return true;
        }
        return false;
    }

    public void handleMultiWindowFocusChanged(WindowState focusedWindow, IApplicationToken focusedApp, Handler handler) {
        int windowFocusFlags;
        if (focusedWindow == null || focusedApp == null || this.mTopDockedWindowState == null || focusedApp != focusedWindow.getAppToken()) {
            windowFocusFlags = this.mLastWindowFocusFlags;
        } else if (focusedWindow == this.mTopDockedWindowState || (focusedWindow.getTask() != null && focusedWindow.getTask() == this.mTopDockedWindowState.getTask())) {
            windowFocusFlags = 0;
        } else {
            windowFocusFlags = 64;
        }
        if ((this.mLastWindowFocusFlags ^ windowFocusFlags) != 0) {
            handler.postDelayed(new Runnable(windowFocusFlags) {
                /* class com.android.server.wm.$$Lambda$ColorDisplayPolicyEx$cWhWWpRyzukDP0JY2RYIPv7uOwI */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    ColorDisplayPolicyEx.this.lambda$handleMultiWindowFocusChanged$0$ColorDisplayPolicyEx(this.f$1);
                }
            }, 200);
            this.mLastWindowFocusFlags = windowFocusFlags;
        }
    }

    public WindowState getTopDockedWindowState() {
        return this.mTopDockedWindowState;
    }

    public void setTopDockedWindowState(WindowState window) {
        this.mTopDockedWindowState = window;
    }

    public void setSystemUiVisibility(StatusBarManagerInternal statusBar, int displayId, int vis, int fullscreenStackVis, int dockedStackVis, int mask, Rect fullscreenBounds, Rect dockedBounds, boolean isNavbarColorManagedByIme, WindowState window, int navBarVis) {
        statusBar.setSystemUiVisibility(displayId, vis, fullscreenStackVis, dockedStackVis, -1, fullscreenBounds, dockedBounds, isNavbarColorManagedByIme, window.toString());
        ColorStatusBarManagerInternal colorStatusBar = getColorStatusBarManagerInternal();
        if (colorStatusBar != null) {
            colorStatusBar.updateNavBarVisibilityWithPkg(navBarVis, window.getAttrs().getTitle().toString());
        }
        if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
            Slog.d(TAG, "window title:" + ((Object) window.getAttrs().getTitle()) + " vis:" + Integer.toHexString(vis) + " navBarVis:" + Integer.toHexString(navBarVis));
        }
    }

    public int getNavBarVisibility(boolean isKeyguard, boolean forceShowNavBar, boolean screenOn) {
        int navBarVis = isKeyguard ? 0 | 1 : 0;
        int navBarVis2 = forceShowNavBar ? navBarVis | 4 : navBarVis;
        return screenOn ? navBarVis2 | 8 : navBarVis2;
    }

    /* access modifiers changed from: private */
    /* renamed from: notifyMultiWindowFocusChanged */
    public void lambda$handleMultiWindowFocusChanged$0$ColorDisplayPolicyEx(int state) {
        try {
            new ColorStatusBarManager().notifyMultiWindowFocusChanged(state);
        } catch (RemoteException e) {
        }
    }

    private ColorStatusBarManagerInternal getColorStatusBarManagerInternal() {
        ColorStatusBarManagerInternal colorStatusBarManagerInternal;
        if (this.mColorDpInner == null || this.mColorDpInner.getServiceAcquireLock() == null) {
            return null;
        }
        synchronized (this.mColorDpInner.getServiceAcquireLock()) {
            if (this.mColorStatusBarManagerInternal == null) {
                this.mColorStatusBarManagerInternal = (ColorStatusBarManagerInternal) LocalServices.getService(ColorStatusBarManagerInternal.class);
            }
            colorStatusBarManagerInternal = this.mColorStatusBarManagerInternal;
        }
        return colorStatusBarManagerInternal;
    }

    public void notifyWindowStateChange(int visibility) {
        int state = 0;
        if ((visibility & 2) != 0) {
            state = 2;
        }
        if (this.mLastNavigationBarState != state) {
            if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                Slog.d(TAG, "navigation bar mState: " + StatusBarManager.windowStateToString(state) + " visibility:" + Integer.toHexString(visibility));
            }
            this.mLastNavigationBarState = state;
            Bundle data = new Bundle();
            data.putInt("systembar_id", 2);
            data.putInt("systembar_state", state);
            if (this.mOppoWindowManagerInternal == null) {
                this.mOppoWindowManagerInternal = (OppoWindowManagerInternal) LocalServices.getService(OppoWindowManagerInternal.class);
            }
            this.mOppoWindowManagerInternal.notifyWindowStateChange(data);
        }
    }

    public int getSysUiFlagsForSplitScreen(WindowState win, int sysUiFlag) {
        if (win == null) {
            return sysUiFlag;
        }
        WindowManager.LayoutParams attrs = win.getAttrs();
        if ((sysUiFlag & 512) != 0 && (!((attrs.privateFlags & 131072) == 0 && (attrs.flags & Integer.MIN_VALUE) == 0) && win.inSplitScreenWindowingMode())) {
            sysUiFlag &= -513;
        }
        if (win.getWindowingMode() == ColorZoomWindowManager.WINDOWING_MODE_ZOOM) {
            return sysUiFlag | 256;
        }
        return sysUiFlag;
    }

    public boolean isGameDockWindow(WindowState win) {
        return isWindowTitleEquals(win, "ColorOSGameDock");
    }

    public void layoutGameDockWindowLw(DisplayFrames displayFrames, Rect cf, Rect of, Rect df, Rect pf) {
        cf.set(displayFrames.mUnrestricted);
        of.set(displayFrames.mUnrestricted);
        df.set(displayFrames.mUnrestricted);
        pf.set(displayFrames.mUnrestricted);
    }

    public boolean willSkipSystemUI(String title) {
        Iterator<String> it = sIgnoreWindowTitle.iterator();
        while (it.hasNext()) {
            if (title.contains(it.next())) {
                if (!WindowManagerDebugConfig.DEBUG_LAYOUT) {
                    return true;
                }
                Slog.d(TAG, "skip systemui flags in " + title);
                return true;
            }
        }
        return false;
    }

    public boolean isIncludedNextPage(WindowState win) {
        if (win == null) {
            return false;
        }
        String title = win.getAttrs().getTitle().toString();
        if (!title.contains("com.android.settings") && !title.contains("com.coloros.wirelesssettings") && !title.contains("com.coloros.bootreg")) {
            return false;
        }
        return true;
    }
}
