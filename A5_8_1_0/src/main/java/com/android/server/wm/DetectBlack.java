package com.android.server.wm;

import android.os.Debug;
import android.util.Log;
import android.util.Slog;
import java.util.function.Consumer;

class DetectBlack {
    static final String TAG = "DetectBlack";
    private static DetectBlack mInstance = null;
    private static final boolean mVerbosePrint = Log.isLoggable(TAG, 2);
    private boolean mIsLastBlack = false;
    private final WindowManagerService mService;

    protected DetectBlack(WindowManagerService service) {
        this.mService = service;
    }

    public static void initInstance(WindowManagerService service) {
        mInstance = new DetectBlack(service);
    }

    public static DetectBlack getInstance() {
        return mInstance;
    }

    boolean checkHasSurface() {
        return checkHasSurface(null);
    }

    boolean checkHasSurface(WindowSurfaceController surfaceSkipCheck) {
        boolean z = true;
        if (isNeedCheckSurace()) {
            WindowState surface = null;
            synchronized (this.mService.mWindowMap) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    surface = this.mService.mRoot.getWindow(new -$Lambda$MMmeixlGtfvS8ONgeh16gZdfDeA(this, surfaceSkipCheck));
                } finally {
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            }
            if (surface != null) {
                z = false;
            }
            this.mIsLastBlack = z;
            if (this.mIsLastBlack && WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                int screenWindowY = this.mService.getDefaultDisplayContentLocked().getDisplayInfo().logicalHeight / 2;
                Slog.i(TAG, "checkHasSurface no surface! screenWindowX:" + (this.mService.getDefaultDisplayContentLocked().getDisplayInfo().logicalWidth / 2) + " screenWindowY:" + screenWindowY + Debug.getCallers(10));
            }
            return this.mIsLastBlack ^ 1;
        }
        this.mIsLastBlack = false;
        return true;
    }

    /* JADX WARNING: Missing block: B:9:0x001d, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    /* renamed from: lambda$-com_android_server_wm_DetectBlack_1698 */
    /* synthetic */ boolean m205lambda$-com_android_server_wm_DetectBlack_1698(WindowSurfaceController surfaceSkipCheck, WindowState w) {
        if (!(w.mWinAnimator == null || w.mWinAnimator.mSurfaceController == null || !w.mWinAnimator.mSurfaceController.getShown())) {
            WindowSurfaceController surfaceCtl = w.mWinAnimator.mSurfaceController;
            if (!(surfaceCtl == null || surfaceCtl == surfaceSkipCheck || !isWindowSurfaceContrlInScreenCenter(surfaceCtl))) {
                if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                    Slog.i(TAG, "checkHasSurface found surface win:" + w);
                }
                return true;
            }
        }
        return false;
    }

    boolean isWindowSurfaceContrlInScreenCenter(WindowSurfaceController surfaceCtl) {
        int screenWindowX = this.mService.getDefaultDisplayContentLocked().getDisplayInfo().logicalWidth / 2;
        int screenWindowY = this.mService.getDefaultDisplayContentLocked().getDisplayInfo().logicalHeight / 2;
        if (surfaceCtl.getX() > ((float) screenWindowX) || ((float) screenWindowX) >= surfaceCtl.getX() + surfaceCtl.getWidth() || surfaceCtl.getY() > ((float) screenWindowY) || ((float) screenWindowY) >= surfaceCtl.getY() + surfaceCtl.getHeight()) {
            if (mVerbosePrint) {
                Slog.v(TAG, "checkHasSurface isWindowSurfaceContrlInScreenCenter return false surfaceCtl:" + surfaceCtl);
            }
            return false;
        }
        if (mVerbosePrint) {
            Slog.v(TAG, "checkHasSurface isWindowSurfaceContrlInScreenCenter return true surfaceCtl:" + surfaceCtl);
        }
        return true;
    }

    boolean isLastCheckBlack() {
        return this.mIsLastBlack;
    }

    private void dumpWindowListStatus() {
        synchronized (this.mService.mWindowMap) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mService.mRoot.forAllWindows((Consumer) -$Lambda$-ShbHzWzMvKATSUwSngPXEFkvyU.$INST$0, false);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* renamed from: lambda$-com_android_server_wm_DetectBlack_4182 */
    static /* synthetic */ void m204lambda$-com_android_server_wm_DetectBlack_4182(WindowState w) {
        Slog.i(TAG, "dumpWindowListStatus w:" + w + " w.mWinAnimator:" + w.mWinAnimator + " shown:");
        if (w.mWinAnimator != null) {
            WindowSurfaceController surfaceCtl = w.mWinAnimator.mSurfaceController;
            Slog.i(TAG, "dumpWindowListStatus w:" + w + " surfaceCtl:" + surfaceCtl);
            if (surfaceCtl != null) {
                Slog.i(TAG, "dumpWindowListStatus w:" + w + " surfaceCtl.getX():" + surfaceCtl.getX() + " surfaceCtl.getY():" + surfaceCtl.getY() + " surfaceCtl.getWidth():" + surfaceCtl.getWidth() + " surfaceCtl.getHeight():" + surfaceCtl.getHeight() + "shown:" + surfaceCtl.getShown());
                return;
            }
            return;
        }
        Slog.i(TAG, "dumpWindowListStatus w:" + w + " surfaceCtl is null");
    }

    private boolean isNeedCheckSurace() {
        DisplayContent dc = this.mService.getDefaultDisplayContentLocked();
        if (dc.isStackVisible(3) || dc.isStackVisible(2) || this.mService.mPolicy.isKeyguardShowingAndNotOccluded()) {
            return false;
        }
        return true;
    }
}
