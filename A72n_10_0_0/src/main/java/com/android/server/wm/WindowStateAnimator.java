package com.android.server.wm;

import android.common.OppoFeatureCache;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Debug;
import android.os.Trace;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.view.DisplayInfo;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.android.server.display.OppoBrightUtils;
import com.android.server.pm.DumpState;
import com.android.server.policy.WindowManagerPolicy;
import com.mediatek.server.wm.WindowManagerDebugger;
import java.io.PrintWriter;

/* access modifiers changed from: package-private */
public class WindowStateAnimator {
    static final int COMMIT_DRAW_PENDING = 2;
    static final int DRAW_PENDING = 1;
    static final int HAS_DRAWN = 4;
    static final int NO_SURFACE = 0;
    static final int PRESERVED_SURFACE_LAYER = 1;
    static final int READY_TO_SHOW = 3;
    static final int STACK_CLIP_AFTER_ANIM = 0;
    static final int STACK_CLIP_BEFORE_ANIM = 1;
    static final int STACK_CLIP_NONE = 2;
    static final String TAG = "WindowManager";
    static final int WINDOW_FREEZE_LAYER = 2000000;
    float mAlpha = OppoBrightUtils.MIN_LUX_LIMITI;
    boolean mAnimationIsEntrance;
    final WindowAnimator mAnimator;
    int mAttrType;
    boolean mChildrenDetached = false;
    final Context mContext;
    private boolean mDestroyPreservedSurfaceUponRedraw;
    int mDrawState;
    float mDsDx = 1.0f;
    float mDsDy = OppoBrightUtils.MIN_LUX_LIMITI;
    float mDtDx = OppoBrightUtils.MIN_LUX_LIMITI;
    float mDtDy = 1.0f;
    boolean mEnterAnimationPending;
    boolean mEnteringAnimation;
    float mExtraHScale = 1.0f;
    float mExtraVScale = 1.0f;
    boolean mForceScaleUntilResize;
    boolean mHaveMatrix;
    final boolean mIsWallpaper;
    float mLastAlpha = OppoBrightUtils.MIN_LUX_LIMITI;
    Rect mLastClipRect = new Rect();
    private float mLastDsDx = 1.0f;
    private float mLastDsDy = OppoBrightUtils.MIN_LUX_LIMITI;
    private float mLastDtDx = OppoBrightUtils.MIN_LUX_LIMITI;
    private float mLastDtDy = 1.0f;
    Rect mLastFinalClipRect = new Rect();
    boolean mLastHidden;
    private boolean mOffsetPositionForStackResize;
    private WindowSurfaceController mPendingDestroySurface;
    boolean mPipAnimationStarted = false;
    final WindowManagerPolicy mPolicy;
    private final SurfaceControl.Transaction mReparentTransaction = new SurfaceControl.Transaction();
    boolean mReportSurfaceResized;
    final WindowManagerService mService;
    final Session mSession;
    float mShownAlpha = OppoBrightUtils.MIN_LUX_LIMITI;
    WindowSurfaceController mSurfaceController;
    boolean mSurfaceDestroyDeferred;
    int mSurfaceFormat;
    boolean mSurfaceResized;
    private final Rect mSystemDecorRect = new Rect();
    private Rect mTmpAnimatingBounds = new Rect();
    Rect mTmpClipRect = new Rect();
    private final Point mTmpPos = new Point();
    private final Rect mTmpSize = new Rect();
    private Rect mTmpSourceBounds = new Rect();
    Rect mTmpStackBounds = new Rect();
    private final SurfaceControl.Transaction mTmpTransaction = new SurfaceControl.Transaction();
    private final WallpaperController mWallpaperControllerLocked;
    final WindowState mWin;
    int mXOffset = 0;
    int mYOffset = 0;

    /* access modifiers changed from: package-private */
    public String drawStateToString() {
        int i = this.mDrawState;
        if (i == 0) {
            return "NO_SURFACE";
        }
        if (i == 1) {
            return "DRAW_PENDING";
        }
        if (i == 2) {
            return "COMMIT_DRAW_PENDING";
        }
        if (i == 3) {
            return "READY_TO_SHOW";
        }
        if (i != 4) {
            return Integer.toString(i);
        }
        return "HAS_DRAWN";
    }

    WindowStateAnimator(WindowState win) {
        WindowManagerService service = win.mWmService;
        this.mService = service;
        this.mAnimator = service.mAnimator;
        this.mPolicy = service.mPolicy;
        this.mContext = service.mContext;
        this.mWin = win;
        this.mSession = win.mSession;
        this.mAttrType = win.mAttrs.type;
        this.mIsWallpaper = win.mIsWallpaper;
        this.mWallpaperControllerLocked = win.getDisplayContent().mWallpaperController;
    }

    /* access modifiers changed from: package-private */
    public void onAnimationFinished() {
        Trace.traceBegin(32, "win animation done");
        if (WindowManagerDebugConfig.DEBUG_ANIM) {
            StringBuilder sb = new StringBuilder();
            sb.append("Animation done in ");
            sb.append(this);
            sb.append(": exiting=");
            sb.append(this.mWin.mAnimatingExit);
            sb.append(", reportedVisible=");
            sb.append(this.mWin.mAppToken != null ? this.mWin.mAppToken.reportedVisible : false);
            Slog.v("WindowManager", sb.toString());
        }
        this.mWin.checkPolicyVisibilityChange();
        DisplayContent displayContent = this.mWin.getDisplayContent();
        if (this.mAttrType == 2000 && this.mWin.isVisibleByPolicy() && displayContent != null) {
            displayContent.setLayoutNeeded();
        }
        this.mWin.onExitAnimationDone();
        int displayId = this.mWin.getDisplayId();
        int pendingLayoutChanges = 8;
        if (displayContent.mWallpaperController.isWallpaperTarget(this.mWin)) {
            pendingLayoutChanges = 8 | 4;
        }
        this.mAnimator.setPendingLayoutChanges(displayId, pendingLayoutChanges);
        if (WindowManagerDebugConfig.DEBUG_LAYOUT_REPEATS) {
            this.mService.mWindowPlacerLocked.debugLayoutRepeats("WindowStateAnimator", this.mAnimator.getPendingLayoutChanges(displayId));
        }
        if (this.mWin.mAppToken != null) {
            this.mWin.mAppToken.updateReportedVisibilityLocked();
        }
        Trace.traceEnd(32);
    }

    /* access modifiers changed from: package-private */
    public void hide(SurfaceControl.Transaction transaction, String reason) {
        if (!this.mLastHidden) {
            this.mLastHidden = true;
            markPreservedSurfaceForDestroy();
            WindowSurfaceController windowSurfaceController = this.mSurfaceController;
            if (windowSurfaceController != null) {
                windowSurfaceController.hide(transaction, reason);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void hide(String reason) {
        hide(this.mTmpTransaction, reason);
        SurfaceControl.mergeToGlobalTransaction(this.mTmpTransaction);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x004e, code lost:
        if (com.mediatek.server.wm.WindowManagerDebugger.WMS_DEBUG_ENG != false) goto L_0x0050;
     */
    public boolean finishDrawingLocked() {
        boolean startingWindow = this.mWin.mAttrs.type == 3;
        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW && startingWindow) {
            Slog.v("WindowManager", "Finishing drawing window " + this.mWin + ": mDrawState=" + drawStateToString());
        }
        if (this.mDrawState != 1) {
            return false;
        }
        if (!WindowManagerDebugConfig.DEBUG_ANIM && !WindowManagerDebugConfig.SHOW_TRANSACTIONS && !WindowManagerDebugConfig.DEBUG_ORIENTATION) {
            WindowManagerDebugger windowManagerDebugger = this.mService.mWindowManagerDebugger;
        }
        Slog.v("WindowManager", "finishDrawingLocked: mDrawState=COMMIT_DRAW_PENDING " + this.mWin + " in " + this.mSurfaceController);
        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW && startingWindow) {
            Slog.v("WindowManager", "Draw state now committed in " + this.mWin);
        }
        this.mDrawState = 2;
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean commitFinishDrawingLocked() {
        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW_VERBOSE && this.mWin.mAttrs.type == 3) {
            Slog.i("WindowManager", "commitFinishDrawingLocked: " + this.mWin + " cur mDrawState=" + drawStateToString());
        }
        int i = this.mDrawState;
        if (i != 2 && i != 3) {
            return false;
        }
        if (WindowManagerDebugConfig.DEBUG_ANIM) {
            Slog.i("WindowManager", "commitFinishDrawingLocked: mDrawState=READY_TO_SHOW " + this.mSurfaceController);
        }
        this.mDrawState = 3;
        AppWindowToken atoken = this.mWin.mAppToken;
        if (atoken == null || atoken.canShowWindows() || this.mWin.mAttrs.type == 3) {
            return this.mWin.performShowLocked();
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void preserveSurfaceLocked() {
        if (this.mDestroyPreservedSurfaceUponRedraw) {
            this.mSurfaceDestroyDeferred = false;
            if (!(this.mPendingDestroySurface == null || this.mSurfaceController == null)) {
                if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                    Slog.i("WindowManager", "reparent in preserveSurfaceLocked from: " + this.mSurfaceController + " to: " + this.mPendingDestroySurface);
                }
                this.mReparentTransaction.reparentChildren(this.mSurfaceController.mSurfaceControl, this.mPendingDestroySurface.mSurfaceControl.getHandle()).apply();
            }
            destroySurfaceLocked();
            this.mSurfaceDestroyDeferred = true;
            return;
        }
        if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
            WindowManagerService.logSurface(this.mWin, "SET FREEZE LAYER", false);
        }
        WindowSurfaceController windowSurfaceController = this.mSurfaceController;
        if (windowSurfaceController != null) {
            windowSurfaceController.mSurfaceControl.setLayer(1);
        }
        this.mDestroyPreservedSurfaceUponRedraw = true;
        this.mSurfaceDestroyDeferred = true;
        destroySurfaceLocked();
    }

    /* access modifiers changed from: package-private */
    public void destroyPreservedSurfaceLocked() {
        if (this.mDestroyPreservedSurfaceUponRedraw) {
            if (!(this.mSurfaceController == null || this.mPendingDestroySurface == null || (this.mWin.mAppToken != null && this.mWin.mAppToken.isRelaunching()))) {
                this.mReparentTransaction.reparentChildren(this.mPendingDestroySurface.mSurfaceControl, this.mSurfaceController.mSurfaceControl.getHandle()).apply();
            }
            destroyDeferredSurfaceLocked();
            this.mDestroyPreservedSurfaceUponRedraw = false;
        }
    }

    /* access modifiers changed from: package-private */
    public void markPreservedSurfaceForDestroy() {
        if (this.mDestroyPreservedSurfaceUponRedraw && !this.mService.mDestroyPreservedSurface.contains(this.mWin)) {
            this.mService.mDestroyPreservedSurface.add(this.mWin);
        }
    }

    private int getLayerStack() {
        return this.mWin.getDisplayContent().getDisplay().getLayerStack();
    }

    /* access modifiers changed from: package-private */
    public void resetDrawState() {
        this.mDrawState = 1;
        if (this.mWin.mAppToken != null) {
            if (!this.mWin.mAppToken.isSelfAnimating()) {
                this.mWin.mAppToken.clearAllDrawn();
            } else {
                this.mWin.mAppToken.deferClearAllDrawn = true;
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0158 A[Catch:{ OutOfResourcesException -> 0x028f, Exception -> 0x0287 }] */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x015a A[Catch:{ OutOfResourcesException -> 0x028f, Exception -> 0x0287 }] */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x016e A[SYNTHETIC, Splitter:B:57:0x016e] */
    /* JADX WARNING: Removed duplicated region for block: B:72:0x01e3  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x0219  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x0261  */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x026c  */
    public WindowSurfaceController createSurfaceLocked(int windowType, int ownerUid) {
        int windowType2;
        int flags;
        String str;
        Exception e;
        int flags2;
        WindowState w = this.mWin;
        WindowSurfaceController windowSurfaceController = this.mSurfaceController;
        if (windowSurfaceController != null) {
            return windowSurfaceController;
        }
        this.mChildrenDetached = false;
        if ((this.mWin.mAttrs.privateFlags & DumpState.DUMP_DEXOPT) != 0) {
            windowType2 = 441731;
        } else {
            windowType2 = windowType;
        }
        w.setHasSurface(false);
        if (WindowManagerDebugConfig.DEBUG_ANIM || WindowManagerDebugConfig.DEBUG_ORIENTATION) {
            Slog.i("WindowManager", "createSurface " + this + ": mDrawState=DRAW_PENDING");
        }
        resetDrawState();
        this.mService.makeWindowFreezingScreenIfNeededLocked(w);
        WindowManager.LayoutParams attrs = w.mAttrs;
        if (this.mService.isSecureLocked(w)) {
            flags = 4 | 128;
        } else {
            flags = 4;
        }
        calculateSurfaceBounds(w, attrs, this.mTmpSize);
        int width = this.mTmpSize.width();
        int height = this.mTmpSize.height();
        if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
            Slog.v("WindowManager", "Creating surface in session " + this.mSession.mSurfaceSession + " window " + this + " w=" + width + " h=" + height + " x=" + this.mTmpSize.left + " y=" + this.mTmpSize.top + " format=" + attrs.format + " flags=" + flags);
        }
        this.mLastClipRect.set(0, 0, 0, 0);
        try {
            int format = (attrs.flags & DumpState.DUMP_SERVICE_PERMISSIONS) != 0 ? -3 : attrs.format;
            if (!PixelFormat.formatHasAlpha(attrs.format)) {
                try {
                    if (attrs.surfaceInsets.left == 0 && attrs.surfaceInsets.top == 0 && attrs.surfaceInsets.right == 0 && attrs.surfaceInsets.bottom == 0 && !w.isDragResizing()) {
                        flags2 = flags | 1024;
                        this.mSurfaceController = new WindowSurfaceController(this.mSession.mSurfaceSession, attrs.getTitle().toString(), width, height, format, flags2, this, windowType2, ownerUid);
                        this.mSurfaceController.setColorSpaceAgnostic((attrs.privateFlags & DumpState.DUMP_SERVICE_PERMISSIONS) == 0);
                        setOffsetPositionForStackResize(false);
                        this.mSurfaceFormat = format;
                        w.setHasSurface(true);
                        if (!WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                            try {
                                if (!WindowManagerDebugConfig.SHOW_SURFACE_ALLOC) {
                                    WindowManagerDebugger windowManagerDebugger = this.mService.mWindowManagerDebugger;
                                    if (!WindowManagerDebugger.WMS_DEBUG_ENG) {
                                        str = "WindowManager";
                                        if (WindowManagerService.localLOGV) {
                                            Slog.v(str, "Got surface: " + this.mSurfaceController + ", set left=" + w.getFrameLw().left + " top=" + w.getFrameLw().top);
                                        }
                                        if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                                            Slog.i(str, ">>> OPEN TRANSACTION createSurfaceLocked");
                                            WindowManagerService.logSurface(w, "CREATE pos=(" + w.getFrameLw().left + "," + w.getFrameLw().top + ") (" + width + "x" + height + ") HIDE", false);
                                        }
                                        this.mLastHidden = true;
                                        if (WindowManagerService.localLOGV) {
                                            Slog.v(str, "Created surface " + this);
                                        }
                                        return this.mSurfaceController;
                                    }
                                }
                            } catch (Surface.OutOfResourcesException e2) {
                                str = "WindowManager";
                                Slog.w(str, "OutOfResourcesException creating surface");
                                this.mService.mRoot.reclaimSomeSurfaceMemory(this, "create", true);
                                this.mDrawState = 0;
                                return null;
                            } catch (Exception e3) {
                                e = e3;
                                str = "WindowManager";
                                Slog.e(str, "Exception creating surface (parent dead?)", e);
                                this.mDrawState = 0;
                                return null;
                            }
                        }
                        str = "WindowManager";
                        try {
                            Slog.i(str, "  CREATE SURFACE " + this.mSurfaceController + " IN SESSION " + this.mSession.mSurfaceSession + ": pid=" + this.mSession.mPid + " format=" + attrs.format + " flags=0x" + Integer.toHexString(flags2) + " / " + this);
                            if (WindowManagerService.localLOGV) {
                            }
                            if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                            }
                            this.mLastHidden = true;
                            if (WindowManagerService.localLOGV) {
                            }
                            return this.mSurfaceController;
                        } catch (Surface.OutOfResourcesException e4) {
                            Slog.w(str, "OutOfResourcesException creating surface");
                            this.mService.mRoot.reclaimSomeSurfaceMemory(this, "create", true);
                            this.mDrawState = 0;
                            return null;
                        } catch (Exception e5) {
                            e = e5;
                            Slog.e(str, "Exception creating surface (parent dead?)", e);
                            this.mDrawState = 0;
                            return null;
                        }
                    }
                } catch (Surface.OutOfResourcesException e6) {
                    str = "WindowManager";
                    Slog.w(str, "OutOfResourcesException creating surface");
                    this.mService.mRoot.reclaimSomeSurfaceMemory(this, "create", true);
                    this.mDrawState = 0;
                    return null;
                } catch (Exception e7) {
                    e = e7;
                    str = "WindowManager";
                    Slog.e(str, "Exception creating surface (parent dead?)", e);
                    this.mDrawState = 0;
                    return null;
                }
            }
            flags2 = flags;
            try {
            } catch (Surface.OutOfResourcesException e8) {
                str = "WindowManager";
                Slog.w(str, "OutOfResourcesException creating surface");
                this.mService.mRoot.reclaimSomeSurfaceMemory(this, "create", true);
                this.mDrawState = 0;
                return null;
            } catch (Exception e9) {
                e = e9;
                str = "WindowManager";
                Slog.e(str, "Exception creating surface (parent dead?)", e);
                this.mDrawState = 0;
                return null;
            }
            try {
                this.mSurfaceController = new WindowSurfaceController(this.mSession.mSurfaceSession, attrs.getTitle().toString(), width, height, format, flags2, this, windowType2, ownerUid);
                this.mSurfaceController.setColorSpaceAgnostic((attrs.privateFlags & DumpState.DUMP_SERVICE_PERMISSIONS) == 0);
                setOffsetPositionForStackResize(false);
                this.mSurfaceFormat = format;
                w.setHasSurface(true);
                if (!WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                }
                str = "WindowManager";
                Slog.i(str, "  CREATE SURFACE " + this.mSurfaceController + " IN SESSION " + this.mSession.mSurfaceSession + ": pid=" + this.mSession.mPid + " format=" + attrs.format + " flags=0x" + Integer.toHexString(flags2) + " / " + this);
                if (WindowManagerService.localLOGV) {
                }
                if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                }
                this.mLastHidden = true;
                if (WindowManagerService.localLOGV) {
                }
                return this.mSurfaceController;
            } catch (Surface.OutOfResourcesException e10) {
                str = "WindowManager";
                Slog.w(str, "OutOfResourcesException creating surface");
                this.mService.mRoot.reclaimSomeSurfaceMemory(this, "create", true);
                this.mDrawState = 0;
                return null;
            } catch (Exception e11) {
                e = e11;
                str = "WindowManager";
                Slog.e(str, "Exception creating surface (parent dead?)", e);
                this.mDrawState = 0;
                return null;
            }
        } catch (Surface.OutOfResourcesException e12) {
            str = "WindowManager";
            Slog.w(str, "OutOfResourcesException creating surface");
            this.mService.mRoot.reclaimSomeSurfaceMemory(this, "create", true);
            this.mDrawState = 0;
            return null;
        } catch (Exception e13) {
            e = e13;
            str = "WindowManager";
            Slog.e(str, "Exception creating surface (parent dead?)", e);
            this.mDrawState = 0;
            return null;
        }
    }

    private void calculateSurfaceBounds(WindowState w, WindowManager.LayoutParams attrs, Rect outSize) {
        outSize.setEmpty();
        if ((attrs.flags & 16384) != 0) {
            outSize.right = w.mRequestedWidth;
            outSize.bottom = w.mRequestedHeight;
        } else if (w.isDragResizing()) {
            DisplayInfo displayInfo = w.getDisplayInfo();
            outSize.right = displayInfo.logicalWidth;
            outSize.bottom = displayInfo.logicalHeight;
        } else {
            w.getCompatFrameSize(outSize);
        }
        if (outSize.width() < 1) {
            outSize.right = 1;
        }
        if (outSize.height() < 1) {
            outSize.bottom = 1;
        }
        outSize.inset(-attrs.surfaceInsets.left, -attrs.surfaceInsets.top, -attrs.surfaceInsets.right, -attrs.surfaceInsets.bottom);
    }

    /* access modifiers changed from: package-private */
    public boolean hasSurface() {
        WindowSurfaceController windowSurfaceController = this.mSurfaceController;
        return windowSurfaceController != null && windowSurfaceController.hasSurface();
    }

    /* access modifiers changed from: package-private */
    public void destroySurfaceLocked() {
        AppWindowToken wtoken = this.mWin.mAppToken;
        if (wtoken != null && this.mWin == wtoken.startingWindow) {
            wtoken.startingDisplayed = false;
        }
        if (this.mSurfaceController != null) {
            if (!this.mDestroyPreservedSurfaceUponRedraw) {
                this.mWin.mHidden = true;
            }
            try {
                if (WindowManagerDebugConfig.DEBUG_VISIBILITY) {
                    WindowManagerService.logWithStack("WindowManager", "Window " + this + " destroying surface " + this.mSurfaceController + ", session " + this.mSession);
                }
                if (!this.mSurfaceDestroyDeferred) {
                    if (WindowManagerDebugConfig.SHOW_TRANSACTIONS || WindowManagerDebugConfig.SHOW_SURFACE_ALLOC) {
                        WindowManagerService.logSurface(this.mWin, "DESTROY", true);
                    }
                    destroySurface();
                } else if (!(this.mSurfaceController == null || this.mPendingDestroySurface == this.mSurfaceController)) {
                    if (this.mPendingDestroySurface != null) {
                        if (WindowManagerDebugConfig.SHOW_TRANSACTIONS || WindowManagerDebugConfig.SHOW_SURFACE_ALLOC) {
                            WindowManagerService.logSurface(this.mWin, "DESTROY PENDING", true);
                        }
                        this.mPendingDestroySurface.destroyNotInTransaction();
                    }
                    this.mPendingDestroySurface = this.mSurfaceController;
                }
                if (!this.mDestroyPreservedSurfaceUponRedraw) {
                    this.mWallpaperControllerLocked.hideWallpapers(this.mWin);
                }
            } catch (RuntimeException e) {
                Slog.w("WindowManager", "Exception thrown when destroying Window " + this + " surface " + this.mSurfaceController + " session " + this.mSession + ": " + e.toString());
            }
            this.mWin.setHasSurface(false);
            WindowSurfaceController windowSurfaceController = this.mSurfaceController;
            if (windowSurfaceController != null) {
                windowSurfaceController.setShown(false);
            }
            this.mSurfaceController = null;
            this.mDrawState = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void destroyDeferredSurfaceLocked() {
        try {
            if (this.mPendingDestroySurface != null) {
                if (WindowManagerDebugConfig.SHOW_TRANSACTIONS || WindowManagerDebugConfig.SHOW_SURFACE_ALLOC) {
                    WindowManagerService.logSurface(this.mWin, "DESTROY PENDING", true);
                }
                this.mPendingDestroySurface.destroyNotInTransaction();
                if (!this.mDestroyPreservedSurfaceUponRedraw) {
                    this.mWallpaperControllerLocked.hideWallpapers(this.mWin);
                }
            }
        } catch (RuntimeException e) {
            Slog.w("WindowManager", "Exception thrown when destroying Window " + this + " surface " + this.mPendingDestroySurface + " session " + this.mSession + ": " + e.toString());
        }
        this.mSurfaceDestroyDeferred = false;
        this.mPendingDestroySurface = null;
    }

    /* access modifiers changed from: package-private */
    public void computeShownFrameLocked() {
        ScreenRotationAnimation screenRotationAnimation = this.mAnimator.getScreenRotationAnimationLocked(this.mWin.getDisplayId());
        boolean screenAnimation = screenRotationAnimation != null && screenRotationAnimation.isAnimating() && (this.mWin.mForceSeamlesslyRotate ^ true);
        if (screenAnimation) {
            Rect frame = this.mWin.getFrameLw();
            float[] tmpFloats = this.mService.mTmpFloats;
            Matrix tmpMatrix = this.mWin.mTmpMatrix;
            if (screenRotationAnimation.isRotating()) {
                float w = (float) frame.width();
                float h = (float) frame.height();
                if (w < 1.0f || h < 1.0f) {
                    tmpMatrix.reset();
                } else {
                    tmpMatrix.setScale((2.0f / w) + 1.0f, (2.0f / h) + 1.0f, w / 2.0f, h / 2.0f);
                }
            } else {
                tmpMatrix.reset();
            }
            tmpMatrix.postScale(this.mWin.mGlobalScale, this.mWin.mGlobalScale);
            tmpMatrix.postTranslate((float) this.mWin.mAttrs.surfaceInsets.left, (float) this.mWin.mAttrs.surfaceInsets.top);
            this.mHaveMatrix = true;
            tmpMatrix.getValues(tmpFloats);
            this.mDsDx = tmpFloats[0];
            this.mDtDx = tmpFloats[3];
            this.mDtDy = tmpFloats[1];
            this.mDsDy = tmpFloats[4];
            this.mShownAlpha = this.mAlpha;
            if ((!this.mService.mLimitedAlphaCompositing || !PixelFormat.formatHasAlpha(this.mWin.mAttrs.format) || this.mWin.isIdentityMatrix(this.mDsDx, this.mDtDx, this.mDtDy, this.mDsDy)) && screenAnimation) {
                this.mShownAlpha *= screenRotationAnimation.getEnterTransformation().getAlpha();
            }
            if (WindowManagerDebugConfig.DEBUG_ANIM || WindowManagerService.localLOGV) {
                float f = this.mShownAlpha;
                if (((double) f) == 1.0d || ((double) f) == 0.0d) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("computeShownFrameLocked: Animating ");
                    sb.append(this);
                    sb.append(" mAlpha=");
                    sb.append(this.mAlpha);
                    sb.append(" screen=");
                    sb.append(screenAnimation ? Float.valueOf(screenRotationAnimation.getEnterTransformation().getAlpha()) : "null");
                    Slog.v("WindowManager", sb.toString());
                }
            }
        } else if ((!this.mIsWallpaper || !this.mService.mRoot.mWallpaperActionPending) && !this.mWin.isDragResizeChanged()) {
            if (WindowManagerService.localLOGV) {
                Slog.v("WindowManager", "computeShownFrameLocked: " + this + " not attached, mAlpha=" + this.mAlpha);
            }
            this.mShownAlpha = this.mAlpha;
            this.mHaveMatrix = false;
            this.mDsDx = this.mWin.mGlobalScale;
            this.mDtDx = OppoBrightUtils.MIN_LUX_LIMITI;
            this.mDtDy = OppoBrightUtils.MIN_LUX_LIMITI;
            this.mDsDy = this.mWin.mGlobalScale;
        }
    }

    private boolean calculateCrop(Rect clipRect) {
        WindowState w = this.mWin;
        DisplayContent displayContent = w.getDisplayContent();
        clipRect.setEmpty();
        if (displayContent == null || w.getWindowConfiguration().tasksAreFloating() || w.mForceSeamlesslyRotate || w.mAttrs.type == 2013) {
            return false;
        }
        if (WindowManagerDebugConfig.DEBUG_WINDOW_CROP) {
            Slog.d("WindowManager", "Updating crop win=" + w + " mLastCrop=" + this.mLastClipRect);
        }
        w.calculatePolicyCrop(this.mSystemDecorRect);
        if (WindowManagerDebugConfig.DEBUG_WINDOW_CROP) {
            Slog.d("WindowManager", "Applying decor to crop win=" + w + " mDecorFrame=" + w.getDecorFrame() + " mSystemDecorRect=" + this.mSystemDecorRect);
        }
        clipRect.set(this.mSystemDecorRect);
        if (WindowManagerDebugConfig.DEBUG_WINDOW_CROP) {
            Slog.d("WindowManager", "win=" + w + " Initial clip rect: " + clipRect);
        }
        w.expandForSurfaceInsets(clipRect);
        clipRect.offset(w.mAttrs.surfaceInsets.left, w.mAttrs.surfaceInsets.top);
        if (WindowManagerDebugConfig.DEBUG_WINDOW_CROP) {
            Slog.d("WindowManager", "win=" + w + " Clip rect after stack adjustment=" + clipRect);
        }
        w.transformClipRectFromScreenToSurfaceSpace(clipRect);
        return true;
    }

    private void applyCrop(Rect clipRect, boolean recoveringMemory) {
        if (WindowManagerDebugConfig.DEBUG_WINDOW_CROP) {
            Slog.d("WindowManager", "applyCrop: win=" + this.mWin + " clipRect=" + clipRect);
        }
        if (clipRect == null) {
            this.mSurfaceController.clearCropInTransaction(recoveringMemory);
        } else if (!clipRect.equals(this.mLastClipRect)) {
            this.mLastClipRect.set(clipRect);
            this.mSurfaceController.setCropInTransaction(clipRect, recoveringMemory);
        }
    }

    /* access modifiers changed from: package-private */
    public void setSurfaceBoundariesLocked(boolean recoveringMemory) {
        boolean wasForceScaled;
        Rect clipRect;
        Rect clipRect2;
        boolean allowStretching;
        Rect clipRect3;
        float th;
        if (this.mSurfaceController != null) {
            WindowState w = this.mWin;
            WindowManager.LayoutParams attrs = this.mWin.getAttrs();
            Task task = w.getTask();
            calculateSurfaceBounds(w, attrs, this.mTmpSize);
            this.mExtraHScale = 1.0f;
            this.mExtraVScale = 1.0f;
            boolean wasForceScaled2 = this.mForceScaleUntilResize;
            boolean relayout = !w.mRelayoutCalled || w.mInRelayout;
            if (relayout) {
                this.mSurfaceResized = this.mSurfaceController.setBufferSizeInTransaction(this.mTmpSize.width(), this.mTmpSize.height(), recoveringMemory);
            } else {
                this.mSurfaceResized = false;
            }
            this.mForceScaleUntilResize = this.mForceScaleUntilResize && !this.mSurfaceResized;
            Rect clipRect4 = null;
            if (calculateCrop(this.mTmpClipRect)) {
                clipRect4 = this.mTmpClipRect;
            }
            float surfaceWidth = (float) this.mSurfaceController.getWidth();
            float surfaceHeight = (float) this.mSurfaceController.getHeight();
            Rect insets = attrs.surfaceInsets;
            if (isForceScaled()) {
                float surfaceContentWidth = surfaceWidth - ((float) (insets.left + insets.right));
                float surfaceContentHeight = surfaceHeight - ((float) (insets.top + insets.bottom));
                if (!this.mForceScaleUntilResize) {
                    this.mSurfaceController.forceScaleableInTransaction(true);
                }
                int posX = 0;
                int posY = 0;
                wasForceScaled = wasForceScaled2;
                task.mStack.getDimBounds(this.mTmpStackBounds);
                task.mStack.getFinalAnimationSourceHintBounds(this.mTmpSourceBounds);
                if (!this.mTmpSourceBounds.isEmpty() || ((this.mWin.mLastRelayoutContentInsets.width() <= 0 && this.mWin.mLastRelayoutContentInsets.height() <= 0) || task.mStack.lastAnimatingBoundsWasToFullscreen())) {
                    allowStretching = false;
                } else {
                    this.mTmpSourceBounds.set(task.mStack.mPreAnimationBounds);
                    this.mTmpSourceBounds.inset(this.mWin.mLastRelayoutContentInsets);
                    allowStretching = true;
                }
                this.mTmpStackBounds.intersectUnchecked(w.getParentFrame());
                this.mTmpSourceBounds.intersectUnchecked(w.getParentFrame());
                this.mTmpAnimatingBounds.intersectUnchecked(w.getParentFrame());
                if (!this.mTmpSourceBounds.isEmpty()) {
                    task.mStack.getFinalAnimationBounds(this.mTmpAnimatingBounds);
                    float initialWidth = (float) this.mTmpSourceBounds.width();
                    float tw = (surfaceContentWidth - ((float) this.mTmpStackBounds.width())) / (surfaceContentWidth - ((float) this.mTmpAnimatingBounds.width()));
                    this.mExtraHScale = (initialWidth + ((((float) this.mTmpAnimatingBounds.width()) - initialWidth) * tw)) / initialWidth;
                    if (allowStretching) {
                        float initialHeight = (float) this.mTmpSourceBounds.height();
                        float th2 = (surfaceContentHeight - ((float) this.mTmpStackBounds.height())) / (surfaceContentHeight - ((float) this.mTmpAnimatingBounds.height()));
                        this.mExtraVScale = (((((float) this.mTmpAnimatingBounds.height()) - initialHeight) * tw) + initialHeight) / initialHeight;
                        th = th2;
                    } else {
                        th = tw;
                        this.mExtraVScale = this.mExtraHScale;
                    }
                    int posX2 = 0 - ((int) ((this.mExtraHScale * tw) * ((float) this.mTmpSourceBounds.left)));
                    posY = 0 - ((int) ((this.mExtraVScale * th) * ((float) this.mTmpSourceBounds.top)));
                    clipRect3 = this.mTmpClipRect;
                    clipRect3.set((int) (((float) (insets.left + this.mTmpSourceBounds.left)) * tw), (int) (((float) (insets.top + this.mTmpSourceBounds.top)) * th), insets.left + ((int) (surfaceWidth - ((surfaceWidth - ((float) this.mTmpSourceBounds.right)) * tw))), insets.top + ((int) (surfaceHeight - ((surfaceHeight - ((float) this.mTmpSourceBounds.bottom)) * th))));
                    posX = posX2;
                } else {
                    this.mExtraHScale = ((float) this.mTmpStackBounds.width()) / surfaceContentWidth;
                    this.mExtraVScale = ((float) this.mTmpStackBounds.height()) / surfaceContentHeight;
                    clipRect3 = null;
                }
                this.mSurfaceController.setPositionInTransaction((float) Math.floor((double) ((int) (((float) (posX - ((int) (((float) attrs.x) * (1.0f - this.mExtraHScale))))) + (((float) insets.left) * (1.0f - this.mExtraHScale))))), (float) Math.floor((double) ((int) (((float) (posY - ((int) (((float) attrs.y) * (1.0f - this.mExtraVScale))))) + (((float) insets.top) * (1.0f - this.mExtraVScale))))), recoveringMemory);
                if (!this.mPipAnimationStarted) {
                    this.mForceScaleUntilResize = true;
                    this.mPipAnimationStarted = true;
                }
                clipRect = clipRect3;
            } else {
                wasForceScaled = wasForceScaled2;
                this.mPipAnimationStarted = false;
                if (!w.mSeamlesslyRotated) {
                    int xOffset = this.mXOffset;
                    int yOffset = this.mYOffset;
                    if (!this.mOffsetPositionForStackResize) {
                        clipRect2 = clipRect4;
                    } else if (relayout) {
                        setOffsetPositionForStackResize(false);
                        WindowSurfaceController windowSurfaceController = this.mSurfaceController;
                        windowSurfaceController.deferTransactionUntil(windowSurfaceController.getHandle(), this.mWin.getFrameNumber());
                        clipRect2 = clipRect4;
                    } else {
                        TaskStack stack = this.mWin.getStack();
                        Point point = this.mTmpPos;
                        point.x = 0;
                        point.y = 0;
                        if (stack != null) {
                            stack.getRelativeDisplayedPosition(point);
                        }
                        xOffset = -this.mTmpPos.x;
                        yOffset = -this.mTmpPos.y;
                        if (clipRect4 != null) {
                            clipRect2 = clipRect4;
                            clipRect2.right += this.mTmpPos.x;
                            clipRect2.bottom += this.mTmpPos.y;
                        } else {
                            clipRect2 = clipRect4;
                        }
                    }
                    this.mSurfaceController.setPositionInTransaction((float) xOffset, (float) yOffset, recoveringMemory);
                } else {
                    clipRect2 = clipRect4;
                }
                clipRect = clipRect2;
            }
            if (wasForceScaled && !this.mForceScaleUntilResize) {
                WindowSurfaceController windowSurfaceController2 = this.mSurfaceController;
                windowSurfaceController2.deferTransactionUntil(windowSurfaceController2.getHandle(), this.mWin.getFrameNumber());
                this.mSurfaceController.forceScaleableInTransaction(false);
            }
            if (!w.mSeamlesslyRotated) {
                applyCrop(clipRect, recoveringMemory);
                this.mSurfaceController.setMatrixInTransaction(this.mDsDx * w.mHScale * this.mExtraHScale, this.mDtDx * w.mVScale * this.mExtraVScale, this.mDtDy * w.mHScale * this.mExtraHScale, this.mDsDy * w.mVScale * this.mExtraVScale, recoveringMemory);
            }
            if (this.mSurfaceResized) {
                this.mReportSurfaceResized = true;
                this.mAnimator.setPendingLayoutChanges(w.getDisplayId(), 4);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void getContainerRect(Rect rect) {
        Task task = this.mWin.getTask();
        if (task != null) {
            task.getDimBounds(rect);
            return;
        }
        rect.bottom = 0;
        rect.right = 0;
        rect.top = 0;
        rect.left = 0;
    }

    /* access modifiers changed from: package-private */
    public void prepareSurfaceLocked(boolean recoveringMemory) {
        WindowState w = this.mWin;
        if (hasSurface()) {
            boolean displayed = false;
            computeShownFrameLocked();
            setSurfaceBoundariesLocked(recoveringMemory);
            if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                WindowManagerDebugger windowManagerDebugger = this.mService.mWindowManagerDebugger;
                boolean z = this.mIsWallpaper;
                WindowState windowState = this.mWin;
                windowManagerDebugger.debugPrepareSurfaceLocked("WindowManager", z, windowState, windowState.mWallpaperVisible, w.isOnScreen(), w.mPolicyVisibility, w.mHasSurface, w.mDestroying, this.mLastHidden);
            }
            if (this.mIsWallpaper && !w.mWallpaperVisible) {
                hide("prepareSurfaceLocked");
            } else if (w.isParentWindowHidden() || !w.isOnScreen()) {
                hide("prepareSurfaceLocked");
                if (w != this.mWallpaperControllerLocked.getWallpaperTarget() || !w.toString().contains("com.oppo.launcher.Launcher")) {
                    this.mWallpaperControllerLocked.hideWallpapers(w);
                }
                if (w.getOrientationChanging() && w.isGoneForLayoutLw()) {
                    w.setOrientationChanging(false);
                    if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                        Slog.v("WindowManager", "Orientation change skips hidden " + w);
                    }
                }
            } else if (this.mLastAlpha == this.mShownAlpha && this.mLastDsDx == this.mDsDx && this.mLastDtDx == this.mDtDx && this.mLastDsDy == this.mDsDy && this.mLastDtDy == this.mDtDy && w.mLastHScale == w.mHScale && w.mLastVScale == w.mVScale && !this.mLastHidden) {
                if (WindowManagerDebugConfig.DEBUG_ANIM && this.mWin.isAnimating()) {
                    Slog.v("WindowManager", "prepareSurface: No changes in animation for " + this);
                }
                displayed = true;
            } else {
                displayed = true;
                this.mLastAlpha = this.mShownAlpha;
                this.mLastDsDx = this.mDsDx;
                this.mLastDtDx = this.mDtDx;
                this.mLastDsDy = this.mDsDy;
                this.mLastDtDy = this.mDtDy;
                w.mLastHScale = w.mHScale;
                w.mLastVScale = w.mVScale;
                if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                    WindowManagerService.logSurface(w, "controller=" + this.mSurfaceController + "alpha=" + this.mShownAlpha + " matrix=[" + this.mDsDx + "*" + w.mHScale + "," + this.mDtDx + "*" + w.mVScale + "][" + this.mDtDy + "*" + w.mHScale + "," + this.mDsDy + "*" + w.mVScale + "]", false);
                }
                if (this.mSurfaceController.prepareToShowInTransaction(this.mShownAlpha, this.mDsDx * w.mHScale * this.mExtraHScale, this.mDtDx * w.mVScale * this.mExtraVScale, this.mDtDy * w.mHScale * this.mExtraHScale, this.mDsDy * w.mVScale * this.mExtraVScale, recoveringMemory) && this.mDrawState == 4 && this.mLastHidden) {
                    if (showSurfaceRobustlyLocked()) {
                        markPreservedSurfaceForDestroy();
                        this.mAnimator.requestRemovalOfReplacedWindows(w);
                        this.mLastHidden = false;
                        if (this.mIsWallpaper) {
                            w.dispatchWallpaperVisibility(true);
                        }
                        if (!w.getDisplayContent().getLastHasContent()) {
                            this.mAnimator.setPendingLayoutChanges(w.getDisplayId(), 8);
                            if (WindowManagerDebugConfig.DEBUG_LAYOUT_REPEATS) {
                                this.mService.mWindowPlacerLocked.debugLayoutRepeats("showSurfaceRobustlyLocked " + w, this.mAnimator.getPendingLayoutChanges(w.getDisplayId()));
                            }
                        }
                    } else {
                        w.setOrientationChanging(false);
                    }
                }
                if (hasSurface()) {
                    w.mToken.hasVisible = true;
                }
            }
            if (w.getOrientationChanging()) {
                if (!w.isDrawnLw()) {
                    this.mAnimator.mBulkUpdateParams &= -5;
                    this.mAnimator.mLastWindowFreezeSource = w;
                    if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                        Slog.v("WindowManager", "Orientation continue waiting for draw in " + w);
                    }
                } else {
                    w.setOrientationChanging(false);
                    if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                        Slog.v("WindowManager", "Orientation change complete in " + w);
                    }
                }
            }
            if (displayed) {
                w.mToken.hasVisible = true;
            }
        } else if (w.getOrientationChanging() && w.isGoneForLayoutLw()) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v("WindowManager", "Orientation change skips hidden " + w);
            }
            w.setOrientationChanging(false);
        }
    }

    /* access modifiers changed from: package-private */
    public void setTransparentRegionHintLocked(Region region) {
        WindowSurfaceController windowSurfaceController = this.mSurfaceController;
        if (windowSurfaceController == null) {
            Slog.w("WindowManager", "setTransparentRegionHint: null mSurface after mHasSurface true");
        } else {
            windowSurfaceController.setTransparentRegionHint(region);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean setWallpaperOffset(int dx, int dy) {
        if (this.mXOffset == dx && this.mYOffset == dy) {
            return false;
        }
        this.mXOffset = dx;
        this.mYOffset = dy;
        try {
            if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                Slog.i("WindowManager", ">>> OPEN TRANSACTION setWallpaperOffset");
            }
            this.mService.openSurfaceTransaction();
            this.mSurfaceController.setPositionInTransaction((float) dx, (float) dy, false);
            applyCrop(null, false);
            this.mService.closeSurfaceTransaction("setWallpaperOffset");
            if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                Slog.i("WindowManager", "<<< CLOSE TRANSACTION setWallpaperOffset");
            }
            return true;
        } catch (RuntimeException e) {
            Slog.w("WindowManager", "Error positioning surface of " + this.mWin + " pos=(" + dx + "," + dy + ")", e);
            this.mService.closeSurfaceTransaction("setWallpaperOffset");
            if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                Slog.i("WindowManager", "<<< CLOSE TRANSACTION setWallpaperOffset");
            }
            return true;
        } catch (Throwable th) {
            this.mService.closeSurfaceTransaction("setWallpaperOffset");
            if (WindowManagerDebugConfig.SHOW_LIGHT_TRANSACTIONS) {
                Slog.i("WindowManager", "<<< CLOSE TRANSACTION setWallpaperOffset");
            }
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean tryChangeFormatInPlaceLocked() {
        if (this.mSurfaceController == null) {
            return false;
        }
        WindowManager.LayoutParams attrs = this.mWin.getAttrs();
        if (((attrs.flags & DumpState.DUMP_SERVICE_PERMISSIONS) != 0 ? -3 : attrs.format) != this.mSurfaceFormat) {
            return false;
        }
        setOpaqueLocked(!PixelFormat.formatHasAlpha(attrs.format));
        return true;
    }

    /* access modifiers changed from: package-private */
    public void setOpaqueLocked(boolean isOpaque) {
        WindowSurfaceController windowSurfaceController = this.mSurfaceController;
        if (windowSurfaceController != null) {
            windowSurfaceController.setOpaque(isOpaque);
        }
    }

    /* access modifiers changed from: package-private */
    public void setSecureLocked(boolean isSecure) {
        WindowSurfaceController windowSurfaceController = this.mSurfaceController;
        if (windowSurfaceController != null) {
            windowSurfaceController.setSecure(isSecure);
        }
    }

    /* access modifiers changed from: package-private */
    public void setColorSpaceAgnosticLocked(boolean agnostic) {
        WindowSurfaceController windowSurfaceController = this.mSurfaceController;
        if (windowSurfaceController != null) {
            windowSurfaceController.setColorSpaceAgnostic(agnostic);
        }
    }

    private boolean showSurfaceRobustlyLocked() {
        if (this.mWin.getWindowConfiguration().windowsAreScaleable()) {
            this.mSurfaceController.forceScaleableInTransaction(true);
        }
        if (!this.mSurfaceController.showRobustlyInTransaction()) {
            return false;
        }
        WindowSurfaceController windowSurfaceController = this.mPendingDestroySurface;
        if (windowSurfaceController != null && this.mDestroyPreservedSurfaceUponRedraw) {
            windowSurfaceController.mSurfaceControl.hide();
            this.mPendingDestroySurface.reparentChildrenInTransaction(this.mSurfaceController);
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void applyEnterAnimationLocked() {
        int transit;
        if (!this.mWin.mSkipEnterAnimationForSeamlessReplacement) {
            if (this.mEnterAnimationPending) {
                this.mEnterAnimationPending = false;
                transit = 1;
            } else {
                transit = 3;
            }
            if (this.mAttrType != 1) {
                applyAnimationLocked(transit, true);
            }
            if (this.mService.mAccessibilityController != null) {
                this.mService.mAccessibilityController.onWindowTransitionLocked(this.mWin, transit);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean applyAnimationLocked(int transit, boolean isEntrance) {
        if (this.mWin.isSelfAnimating() && this.mAnimationIsEntrance == isEntrance) {
            return true;
        }
        if (isEntrance && this.mWin.mAttrs.type == 2011) {
            this.mWin.getDisplayContent().adjustForImeIfNeeded();
            this.mWin.setDisplayLayoutNeeded();
            this.mService.mWindowPlacerLocked.requestTraversal();
        }
        Trace.traceBegin(32, "WSA#applyAnimationLocked");
        if (!this.mWin.mToken.okToAnimate()) {
            this.mWin.cancelAnimation();
        } else if (transit != 5 || !OppoFeatureCache.get(IColorStartingWindowManager.DEFAULT).setStartingWindowExitAnimation(this.mWin)) {
            int anim = this.mWin.getDisplayContent().getDisplayPolicy().selectAnimationLw(this.mWin, transit);
            int attr = -1;
            Animation a = null;
            if (anim != 0) {
                a = anim != -1 ? AnimationUtils.loadAnimation(this.mContext, anim) : null;
            } else if (!isEntrance || !OppoFeatureCache.get(IColorStartingWindowManager.DEFAULT).checkAppWindowAnimating(this.mWin.mAppToken)) {
                if (transit == 1) {
                    attr = 0;
                } else if (transit == 2) {
                    attr = 1;
                } else if (transit == 3) {
                    attr = 2;
                } else if (transit == 4) {
                    attr = 3;
                }
                if (attr >= 0) {
                    a = this.mWin.getDisplayContent().mAppTransition.loadAnimationAttr(this.mWin.mAttrs, attr, 0);
                }
            } else {
                Trace.traceEnd(32);
                return true;
            }
            if (WindowManagerDebugConfig.DEBUG_ANIM) {
                Slog.v("WindowManager", "applyAnimation: win=" + this + " anim=" + anim + " attr=0x" + Integer.toHexString(attr) + " a=" + a + " transit=" + transit + " type=" + this.mAttrType + " isEntrance=" + isEntrance + " Callers " + Debug.getCallers(3));
            }
            if (a != null) {
                if (WindowManagerDebugConfig.DEBUG_ANIM) {
                    WindowManagerService.logWithStack("WindowManager", "Loaded animation " + a + " for " + this);
                }
                this.mWin.startAnimation(a);
                this.mAnimationIsEntrance = isEntrance;
            }
        } else {
            Trace.traceEnd(32);
            return true;
        }
        if (!isEntrance && this.mWin.mAttrs.type == 2011) {
            this.mWin.getDisplayContent().adjustForImeIfNeeded();
        }
        Trace.traceEnd(32);
        return isAnimationSet(this.mWin);
    }

    private boolean isAnimationSet(WindowContainer wc) {
        if (!(wc instanceof WindowState) && !(wc instanceof AppWindowToken)) {
            return false;
        }
        if (wc.isSelfAnimating() || isAnimationSet(wc.getParent())) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        this.mLastClipRect.writeToProto(proto, 1146756268033L);
        WindowSurfaceController windowSurfaceController = this.mSurfaceController;
        if (windowSurfaceController != null) {
            windowSurfaceController.writeToProto(proto, 1146756268034L);
        }
        proto.write(1159641169923L, this.mDrawState);
        this.mSystemDecorRect.writeToProto(proto, 1146756268036L);
        proto.end(token);
    }

    public void dump(PrintWriter pw, String prefix, boolean dumpAll) {
        if (this.mAnimationIsEntrance) {
            pw.print(prefix);
            pw.print(" mAnimationIsEntrance=");
            pw.print(this.mAnimationIsEntrance);
        }
        WindowSurfaceController windowSurfaceController = this.mSurfaceController;
        if (windowSurfaceController != null) {
            windowSurfaceController.dump(pw, prefix, dumpAll);
        }
        if (dumpAll) {
            pw.print(prefix);
            pw.print("mDrawState=");
            pw.print(drawStateToString());
            pw.print(prefix);
            pw.print(" mLastHidden=");
            pw.println(this.mLastHidden);
            pw.print(prefix);
            pw.print("mSystemDecorRect=");
            this.mSystemDecorRect.printShortString(pw);
            pw.print(" mLastClipRect=");
            this.mLastClipRect.printShortString(pw);
            if (!this.mLastFinalClipRect.isEmpty()) {
                pw.print(" mLastFinalClipRect=");
                this.mLastFinalClipRect.printShortString(pw);
            }
            pw.println();
        }
        if (this.mPendingDestroySurface != null) {
            pw.print(prefix);
            pw.print("mPendingDestroySurface=");
            pw.println(this.mPendingDestroySurface);
        }
        if (this.mSurfaceResized || this.mSurfaceDestroyDeferred) {
            pw.print(prefix);
            pw.print("mSurfaceResized=");
            pw.print(this.mSurfaceResized);
            pw.print(" mSurfaceDestroyDeferred=");
            pw.println(this.mSurfaceDestroyDeferred);
        }
        if (!(this.mShownAlpha == 1.0f && this.mAlpha == 1.0f && this.mLastAlpha == 1.0f)) {
            pw.print(prefix);
            pw.print("mShownAlpha=");
            pw.print(this.mShownAlpha);
            pw.print(" mAlpha=");
            pw.print(this.mAlpha);
            pw.print(" mLastAlpha=");
            pw.println(this.mLastAlpha);
        }
        if (this.mHaveMatrix || this.mWin.mGlobalScale != 1.0f) {
            pw.print(prefix);
            pw.print("mGlobalScale=");
            pw.print(this.mWin.mGlobalScale);
            pw.print(" mDsDx=");
            pw.print(this.mDsDx);
            pw.print(" mDtDx=");
            pw.print(this.mDtDx);
            pw.print(" mDtDy=");
            pw.print(this.mDtDy);
            pw.print(" mDsDy=");
            pw.println(this.mDsDy);
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("WindowStateAnimator{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(' ');
        sb.append(this.mWin.mAttrs.getTitle());
        sb.append('}');
        return sb.toString();
    }

    /* access modifiers changed from: package-private */
    public void reclaimSomeSurfaceMemory(String operation, boolean secure) {
        this.mService.mRoot.reclaimSomeSurfaceMemory(this, operation, secure);
    }

    /* access modifiers changed from: package-private */
    public boolean getShown() {
        WindowSurfaceController windowSurfaceController = this.mSurfaceController;
        if (windowSurfaceController != null) {
            return windowSurfaceController.getShown();
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void destroySurface() {
        try {
            if (this.mSurfaceController != null) {
                this.mSurfaceController.destroyNotInTransaction();
            }
        } catch (RuntimeException e) {
            Slog.w("WindowManager", "Exception thrown when destroying surface " + this + " surface " + this.mSurfaceController + " session " + this.mSession + ": " + e);
        } catch (Throwable th) {
            this.mWin.setHasSurface(false);
            this.mSurfaceController = null;
            this.mDrawState = 0;
            throw th;
        }
        this.mWin.setHasSurface(false);
        this.mSurfaceController = null;
        this.mDrawState = 0;
    }

    /* access modifiers changed from: package-private */
    public boolean isForceScaled() {
        Task task = this.mWin.getTask();
        if (task == null || !task.mStack.isForceScaled()) {
            return this.mForceScaleUntilResize;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void detachChildren() {
        WindowSurfaceController windowSurfaceController = this.mSurfaceController;
        if (windowSurfaceController != null) {
            windowSurfaceController.detachChildren();
        }
        this.mChildrenDetached = true;
    }

    /* access modifiers changed from: package-private */
    public void setOffsetPositionForStackResize(boolean offsetPositionForStackResize) {
        this.mOffsetPositionForStackResize = offsetPositionForStackResize;
    }
}
