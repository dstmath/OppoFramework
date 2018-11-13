package com.android.server.wm;

import android.app.ActivityManager.StackId;
import android.net.arp.OppoArpPeer;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.usb.descriptors.UsbDescriptor;
import java.util.ArrayDeque;
import java.util.function.Consumer;

class WindowLayersController {
    private static final String OPPO_LAUNCHER = "com.oppo.launcher.Launcher";
    private static boolean mAdjustLauncherLayer = SystemProperties.getBoolean("persist.sys.adjustlaunchlayer", true);
    private boolean mAboveImeTarget;
    private ArrayDeque<WindowState> mAboveImeTargetAppWindows = new ArrayDeque();
    private boolean mAnyLayerChanged;
    private final Consumer<WindowState> mAssignWindowLayersConsumer = new -$Lambda$YIZfR4m-B8z_tYbP2x4OJ3o7OYE(UsbDescriptor.CLASSID_BILLBOARD, this);
    private ArrayDeque<WindowState> mAssistantWindows = new ArrayDeque();
    private int mCurBaseLayer;
    private int mCurLayer;
    private WindowState mDockDivider = null;
    private ArrayDeque<WindowState> mDockedWindows = new ArrayDeque();
    private int mHighestApplicationLayer;
    private int mHighestDockedAffectedLayer;
    private int mHighestLayerInImeTargetBaseLayer;
    private WindowState mImeTarget;
    private ArrayDeque<WindowState> mInputMethodWindows = new ArrayDeque();
    private WindowState mLanucherWin = null;
    private ArrayDeque<WindowState> mPinnedWindows = new ArrayDeque();
    private ArrayDeque<WindowState> mReplacingWindows = new ArrayDeque();
    private final WindowManagerService mService;
    private WindowState mVisibleNoFullScreenWin = null;
    private WindowState mWallpaperWin = null;

    WindowLayersController(WindowManagerService service) {
        this.mService = service;
    }

    /* renamed from: lambda$-com_android_server_wm_WindowLayersController_4392 */
    /* synthetic */ void m248lambda$-com_android_server_wm_WindowLayersController_4392(WindowState w) {
        boolean layerChanged = false;
        int oldLayer = w.mLayer;
        if (w.mBaseLayer == this.mCurBaseLayer) {
            this.mCurLayer += 5;
        } else {
            int i = w.mBaseLayer;
            this.mCurLayer = i;
            this.mCurBaseLayer = i;
        }
        assignAnimLayer(w, this.mCurLayer);
        if (!(w.mLayer == oldLayer && w.mWinAnimator.mAnimLayer == oldLayer)) {
            layerChanged = true;
            this.mAnyLayerChanged = true;
        }
        if (w.mAppToken != null) {
            this.mHighestApplicationLayer = Math.max(this.mHighestApplicationLayer, w.mWinAnimator.mAnimLayer);
        }
        if (this.mImeTarget != null && w.mBaseLayer == this.mImeTarget.mBaseLayer) {
            this.mHighestLayerInImeTargetBaseLayer = Math.max(this.mHighestLayerInImeTargetBaseLayer, w.mWinAnimator.mAnimLayer);
        }
        if (w.getAppToken() != null && StackId.isResizeableByDockedStack(w.getStackId())) {
            this.mHighestDockedAffectedLayer = Math.max(this.mHighestDockedAffectedLayer, w.mWinAnimator.mAnimLayer);
        }
        collectSpecialWindows(w);
        if (layerChanged) {
            w.scheduleAnimationIfDimming();
        }
    }

    final void assignWindowLayers(DisplayContent dc) {
        if (WindowManagerDebugConfig.DEBUG_LAYERS) {
            Slog.v("WindowManager", "Assigning layers based", new RuntimeException("here").fillInStackTrace());
        }
        reset();
        dc.forAllWindows(this.mAssignWindowLayersConsumer, false);
        adjustSpecialWindows();
        if (this.mService.mAccessibilityController != null && this.mAnyLayerChanged && dc.getDisplayId() == 0) {
            this.mService.mAccessibilityController.onWindowLayersChangedLocked();
        }
        if (WindowManagerDebugConfig.DEBUG_LAYERS) {
            logDebugLayers(dc);
        }
    }

    /* renamed from: lambda$-com_android_server_wm_WindowLayersController_6572 */
    static /* synthetic */ void m247lambda$-com_android_server_wm_WindowLayersController_6572(WindowState w) {
        Slog.v("WindowManager", "Assign layer " + w + ": " + "mBase=" + w.mBaseLayer + " mLayer=" + w.mLayer + (w.mAppToken == null ? "" : " mAppLayer=" + w.mAppToken.getAnimLayerAdjustment()) + " =mAnimLayer=" + w.mWinAnimator.mAnimLayer);
    }

    private void logDebugLayers(DisplayContent dc) {
        dc.forAllWindows((Consumer) -$Lambda$-ShbHzWzMvKATSUwSngPXEFkvyU.$INST$7, false);
    }

    private void reset() {
        this.mPinnedWindows.clear();
        this.mInputMethodWindows.clear();
        this.mDockedWindows.clear();
        this.mAssistantWindows.clear();
        this.mReplacingWindows.clear();
        this.mDockDivider = null;
        this.mCurBaseLayer = 0;
        this.mCurLayer = 0;
        this.mAnyLayerChanged = false;
        this.mHighestApplicationLayer = 0;
        this.mHighestDockedAffectedLayer = 0;
        this.mHighestLayerInImeTargetBaseLayer = this.mImeTarget != null ? this.mImeTarget.mBaseLayer : 0;
        this.mImeTarget = this.mService.mInputMethodTarget;
        this.mAboveImeTarget = false;
        this.mAboveImeTargetAppWindows.clear();
        this.mLanucherWin = null;
        this.mWallpaperWin = null;
        this.mVisibleNoFullScreenWin = null;
    }

    private void collectSpecialWindows(WindowState w) {
        if (w.mAttrs.type == 2034) {
            this.mDockDivider = w;
            return;
        }
        if (w.mWillReplaceWindow) {
            this.mReplacingWindows.add(w);
        }
        if (w.mIsImWindow) {
            this.mInputMethodWindows.add(w);
            return;
        }
        if (this.mImeTarget != null) {
            if (w.getParentWindow() == this.mImeTarget && w.mSubLayer > 0) {
                this.mAboveImeTargetAppWindows.add(w);
            } else if (this.mAboveImeTarget && w.mAppToken != null) {
                this.mAboveImeTargetAppWindows.add(w);
            }
            if (w == this.mImeTarget) {
                this.mAboveImeTarget = true;
            }
        }
        if (mAdjustLauncherLayer && w.mHasSurface && (w.mIsImWindow ^ 1) != 0 && w.mWinAnimator != null && w.mWinAnimator.getShown()) {
            if (w.mIsWallpaper) {
                this.mWallpaperWin = w;
            } else if (w.mAttrs.getTitle() != null && w.mAttrs.getTitle().toString().contains(OPPO_LAUNCHER)) {
                this.mLanucherWin = w;
            } else if (!(this.mVisibleNoFullScreenWin != null || w.mAppToken == null || (w.mAppToken.fillsParent() ^ 1) == 0)) {
                this.mVisibleNoFullScreenWin = w;
            }
        }
        int stackId = w.getAppToken() != null ? w.getStackId() : -1;
        if (stackId == 4) {
            this.mPinnedWindows.add(w);
        } else if (stackId == 3 && (this.mService.mInFullscreeSplit ^ 1) != 0) {
            this.mDockedWindows.add(w);
        } else if (stackId == 6) {
            this.mAssistantWindows.add(w);
        }
    }

    private void adjustSpecialWindows() {
        int layer = this.mHighestDockedAffectedLayer + 1000;
        if (!this.mDockedWindows.isEmpty() && this.mHighestDockedAffectedLayer > 0) {
            while (!this.mDockedWindows.isEmpty()) {
                layer = assignAndIncreaseLayerIfNeeded((WindowState) this.mDockedWindows.remove(), layer);
            }
            layer = assignAndIncreaseLayerIfNeeded(this.mDockDivider, layer);
            while (!this.mAssistantWindows.isEmpty()) {
                WindowState window = (WindowState) this.mAssistantWindows.remove();
                if (window.mLayer > this.mHighestDockedAffectedLayer) {
                    layer = assignAndIncreaseLayerIfNeeded(window, layer);
                }
            }
        }
        layer = Math.max(layer, this.mHighestApplicationLayer + 5);
        while (!this.mReplacingWindows.isEmpty()) {
            layer = assignAndIncreaseLayerIfNeeded((WindowState) this.mReplacingWindows.remove(), layer);
        }
        while (!this.mPinnedWindows.isEmpty()) {
            layer = assignAndIncreaseLayerIfNeeded((WindowState) this.mPinnedWindows.remove(), layer);
        }
        if (this.mImeTarget != null) {
            if (this.mImeTarget.mAppToken == null) {
                layer = this.mHighestLayerInImeTargetBaseLayer + 5;
            }
            while (!this.mInputMethodWindows.isEmpty()) {
                layer = assignAndIncreaseLayerIfNeeded((WindowState) this.mInputMethodWindows.remove(), layer);
            }
            while (!this.mAboveImeTargetAppWindows.isEmpty()) {
                layer = assignAndIncreaseLayerIfNeeded((WindowState) this.mAboveImeTargetAppWindows.remove(), layer);
            }
        }
        if (this.mLanucherWin != null && this.mWallpaperWin != null && this.mVisibleNoFullScreenWin != null && this.mLanucherWin.mAttrs.type < OppoArpPeer.ARP_FIRST_RESPONSE_TIMEOUT && this.mVisibleNoFullScreenWin.mLayer > this.mWallpaperWin.mLayer && this.mVisibleNoFullScreenWin.mLayer < this.mLanucherWin.mLayer) {
            Slog.v("WindowManager", "changing " + this.mLanucherWin + " mLayer from " + this.mLanucherWin.mLayer + " to " + (this.mVisibleNoFullScreenWin.mLayer - 5));
            assignAnimLayer(this.mLanucherWin, this.mVisibleNoFullScreenWin.mLayer - 5);
        }
    }

    private int assignAndIncreaseLayerIfNeeded(WindowState win, int layer) {
        if (win == null) {
            return layer;
        }
        assignAnimLayer(win, layer);
        return layer + 5;
    }

    private void assignAnimLayer(WindowState w, int layer) {
        w.mLayer = layer;
        if (w.getAttrs() != null && w.getAttrs().type == 2301) {
            w.mLayer = w.mBaseLayer - 1;
        }
        w.mWinAnimator.mAnimLayer = w.getAnimLayerAdjustment() + w.getSpecialWindowAnimLayerAdjustment();
        if (w.mAppToken != null && w.mAppToken.mAppAnimator.thumbnailForceAboveLayer > 0) {
            if (w.mWinAnimator.mAnimLayer > w.mAppToken.mAppAnimator.thumbnailForceAboveLayer) {
                w.mAppToken.mAppAnimator.thumbnailForceAboveLayer = w.mWinAnimator.mAnimLayer;
            }
            int highestLayer = w.mAppToken.getHighestAnimLayer();
            if (highestLayer > 0 && w.mAppToken.mAppAnimator.thumbnail != null && w.mAppToken.mAppAnimator.thumbnailForceAboveLayer != highestLayer) {
                w.mAppToken.mAppAnimator.thumbnailForceAboveLayer = highestLayer;
                w.mAppToken.mAppAnimator.thumbnail.setLayer(highestLayer + 1);
            }
        }
    }
}
