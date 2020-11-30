package com.android.server.wm;

import android.animation.AnimationHandler;
import android.app.WindowConfiguration;
import android.common.OppoFeatureCache;
import android.content.pm.ApplicationInfo;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Insets;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.hardware.display.DisplayManagerInternal;
import android.metrics.LogMaker;
import android.os.Binder;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Trace;
import android.util.ArraySet;
import android.util.DisplayMetrics;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.DisplayInfo;
import android.view.ISystemGestureExclusionListener;
import android.view.IWindow;
import android.view.InputApplicationHandle;
import android.view.InputDevice;
import android.view.InputWindowHandle;
import android.view.MagnificationSpec;
import android.view.OppoScreenDragUtil;
import android.view.OppoWindowManager;
import android.view.RemoteAnimationDefinition;
import android.view.SurfaceControl;
import android.view.SurfaceSession;
import android.view.WindowManagerPolicyConstants;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.ToBooleanFunction;
import com.android.internal.util.function.TriConsumer;
import com.android.internal.util.function.pooled.PooledConsumer;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.server.AnimationThread;
import com.android.server.UiModeManagerService;
import com.android.server.connectivity.networkrecovery.dnsresolve.StringUtils;
import com.android.server.display.OppoBrightUtils;
import com.android.server.input.InputManagerService;
import com.android.server.pm.DumpState;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.slice.SliceClientPermissions;
import com.android.server.theia.NoFocusWindow;
import com.android.server.theia.TheiaUtil;
import com.android.server.usb.descriptors.UsbACInterface;
import com.android.server.usb.descriptors.UsbTerminalTypes;
import com.android.server.wm.DisplayContent;
import com.android.server.wm.WindowContainer;
import com.android.server.wm.WindowManagerService;
import com.android.server.wm.utils.DisplayRotationUtil;
import com.android.server.wm.utils.RegionUtils;
import com.android.server.wm.utils.RotationCache;
import com.android.server.wm.utils.WmDisplayCutout;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import oppo.util.OppoStatistics;

/* access modifiers changed from: package-private */
public class DisplayContent extends WindowContainer<DisplayChildWindowContainer> implements WindowManagerPolicy.DisplayContentInfo {
    static final int FORCE_SCALING_MODE_AUTO = 0;
    static final int FORCE_SCALING_MODE_DISABLED = 1;
    private static final String TAG = "WindowManager";
    @VisibleForTesting
    boolean isDefaultDisplay;
    private final AboveAppWindowContainers mAboveAppWindowsContainers = new AboveAppWindowContainers("mAboveAppWindowsContainers", this.mWmService);
    ActivityDisplay mAcitvityDisplay;
    final AppTransition mAppTransition;
    final AppTransitionController mAppTransitionController;
    private final Consumer<WindowState> mApplyPostLayoutPolicy = new Consumer() {
        /* class com.android.server.wm.$$Lambda$DisplayContent$JibsaX4YnJd0ta_wiDDdSpPjQk */

        @Override // java.util.function.Consumer
        public final void accept(Object obj) {
            DisplayContent.this.lambda$new$7$DisplayContent((WindowState) obj);
        }
    };
    private final Consumer<WindowState> mApplySurfaceChangesTransaction = new Consumer() {
        /* class com.android.server.wm.$$Lambda$DisplayContent$qxt4izS31fb0LF2uo_OF9DMa7gc */

        @Override // java.util.function.Consumer
        public final void accept(Object obj) {
            DisplayContent.this.lambda$new$8$DisplayContent((WindowState) obj);
        }
    };
    int mBaseDisplayDensity = 0;
    int mBaseDisplayHeight = 0;
    private Rect mBaseDisplayRect = new Rect();
    int mBaseDisplayWidth = 0;
    private final NonAppWindowContainers mBelowAppWindowsContainers = new NonAppWindowContainers("mBelowAppWindowsContainers", this.mWmService);
    BoundsAnimationController mBoundsAnimationController;
    final ArraySet<AppWindowToken> mChangingApps = new ArraySet<>();
    @VisibleForTesting
    final float mCloseToSquareMaxAspectRatio;
    final ArraySet<AppWindowToken> mClosingApps = new ArraySet<>();
    private final DisplayMetrics mCompatDisplayMetrics = new DisplayMetrics();
    float mCompatibleScreenScale;
    private final Predicate<WindowState> mComputeImeTargetPredicate = new Predicate() {
        /* class com.android.server.wm.$$Lambda$DisplayContent$TPj3OjTsuIg5GTLb5nMmFqIghA4 */

        @Override // java.util.function.Predicate
        public final boolean test(Object obj) {
            return DisplayContent.this.lambda$new$6$DisplayContent((WindowState) obj);
        }
    };
    WindowState mCurrentFocus = null;
    private int mDeferUpdateImeTargetCount;
    private boolean mDeferredRemoval;
    int mDeferredRotationPauseCount;
    private final Display mDisplay;
    private final RotationCache<DisplayCutout, WmDisplayCutout> mDisplayCutoutCache = new RotationCache<>(new RotationCache.RotationDependentComputation() {
        /* class com.android.server.wm.$$Lambda$DisplayContent$fiC19lMyd_rvza7hhOSw6bOM8 */

        @Override // com.android.server.wm.utils.RotationCache.RotationDependentComputation
        public final Object compute(Object obj, int i) {
            return DisplayContent.this.calculateDisplayCutoutForRotationUncached((DisplayCutout) obj, i);
        }
    });
    DisplayFrames mDisplayFrames;
    private final int mDisplayId;
    private final DisplayInfo mDisplayInfo = new DisplayInfo();
    private final DisplayMetrics mDisplayMetrics = new DisplayMetrics();
    private final DisplayPolicy mDisplayPolicy;
    private boolean mDisplayReady = false;
    private DisplayRotation mDisplayRotation;
    boolean mDisplayScalingDisabled;
    final DockedStackDividerController mDividerControllerLocked;
    final ArrayList<WindowToken> mExitingTokens = new ArrayList<>();
    private final ToBooleanFunction<WindowState> mFindFocusedWindow = new ToBooleanFunction() {
        /* class com.android.server.wm.$$Lambda$DisplayContent$7uZtakUXzuXqF_Qht5Uq7LUvubI */

        public final boolean apply(Object obj) {
            return DisplayContent.this.lambda$new$3$DisplayContent((WindowState) obj);
        }
    };
    AppWindowToken mFocusedApp = null;
    private boolean mHaveApp = false;
    private boolean mHaveBootMsg = false;
    private boolean mHaveKeyguard = true;
    private boolean mHaveWallpaper = false;
    private boolean mIgnoreRotationForApps;
    private final NonAppWindowContainers mImeWindowsContainers = new NonAppWindowContainers("mImeWindowsContainers", this.mWmService);
    DisplayCutout mInitialDisplayCutout;
    int mInitialDisplayDensity = 0;
    int mInitialDisplayHeight = 0;
    int mInitialDisplayWidth = 0;
    WindowState mInputMethodTarget;
    boolean mInputMethodTargetWaitingAnim;
    WindowState mInputMethodWindow;
    private InputMonitor mInputMonitor;
    private final InsetsStateController mInsetsStateController;
    private int mLastDispatchedSystemUiVisibility = 0;
    WindowState mLastFocus = null;
    private boolean mLastHasContent;
    private int mLastKeyguardForcedOrientation = -1;
    private int mLastOrientation = -1;
    private int mLastStatusBarVisibility = 0;
    private boolean mLastWallpaperVisible = false;
    private int mLastWindowForcedOrientation = -1;
    private boolean mLayoutNeeded;
    int mLayoutSeq = 0;
    private Point mLocationInParentWindow = new Point();
    ArrayList<WindowState> mLosingFocus = new ArrayList<>();
    private MagnificationSpec mMagnificationSpec;
    private int mMaxUiWidth;
    private MetricsLogger mMetricsLogger;
    final List<IBinder> mNoAnimationNotifyOnTransitionFinished = new ArrayList();
    final ArraySet<AppWindowToken> mOpeningApps = new ArraySet<>();
    private OppoDisplayModeManager mOppoDisplayModeManager;
    private SurfaceControl mOverlayLayer;
    private SurfaceControl mParentSurfaceControl;
    private WindowState mParentWindow;
    private final Consumer<WindowState> mPerformLayout = new Consumer() {
        /* class com.android.server.wm.$$Lambda$DisplayContent$qT01Aq6xt_ZOs86A1yDQeqmPFQ */

        @Override // java.util.function.Consumer
        public final void accept(Object obj) {
            DisplayContent.this.lambda$new$4$DisplayContent((WindowState) obj);
        }
    };
    private final Consumer<WindowState> mPerformLayoutAttached = new Consumer() {
        /* class com.android.server.wm.$$Lambda$DisplayContent$7voe_dEKk2BYMriCvPuvaznb9WQ */

        @Override // java.util.function.Consumer
        public final void accept(Object obj) {
            DisplayContent.this.lambda$new$5$DisplayContent((WindowState) obj);
        }
    };
    final PinnedStackController mPinnedStackControllerLocked;
    private final PointerEventDispatcher mPointerEventDispatcher;
    private InputWindowHandle mPortalWindowHandle;
    final DisplayMetrics mRealDisplayMetrics = new DisplayMetrics();
    private boolean mRemovingDisplay = false;
    private int mRotation = 0;
    private DisplayRotationUtil mRotationUtil = new DisplayRotationUtil();
    private final Consumer<WindowState> mScheduleToastTimeout = new Consumer() {
        /* class com.android.server.wm.$$Lambda$DisplayContent$hRKjZwmneu0T85LNNY6_Zcs4gKM */

        @Override // java.util.function.Consumer
        public final void accept(Object obj) {
            DisplayContent.this.lambda$new$2$DisplayContent((WindowState) obj);
        }
    };
    private final SurfaceSession mSession = new SurfaceSession();
    boolean mShouldOverrideDisplayConfiguration = true;
    boolean mSkipAppTransitionAnimation = false;
    private final Region mSystemGestureExclusion = new Region();
    private int mSystemGestureExclusionLimit;
    private final RemoteCallbackList<ISystemGestureExclusionListener> mSystemGestureExclusionListeners = new RemoteCallbackList<>();
    @VisibleForTesting
    final TaskTapPointerEventListener mTapDetector;
    final ArraySet<WindowState> mTapExcludeProvidingWindows = new ArraySet<>();
    final ArrayList<WindowState> mTapExcludedWindows = new ArrayList<>();
    private final TaskStackContainers mTaskStackContainers = new TaskStackContainers(this.mWmService);
    private final ApplySurfaceChangesTransactionState mTmpApplySurfaceChangesTransactionState = new ApplySurfaceChangesTransactionState();
    private final Rect mTmpBounds = new Rect();
    private final Configuration mTmpConfiguration = new Configuration();
    private final DisplayMetrics mTmpDisplayMetrics = new DisplayMetrics();
    private final float[] mTmpFloats = new float[9];
    private boolean mTmpInitial;
    private final Matrix mTmpMatrix = new Matrix();
    private boolean mTmpRecoveringMemory;
    private final Rect mTmpRect = new Rect();
    private final Rect mTmpRect2 = new Rect();
    private final RectF mTmpRectF = new RectF();
    private final Region mTmpRegion = new Region();
    private final TaskForResizePointSearchResult mTmpTaskForResizePointSearchResult = new TaskForResizePointSearchResult();
    private final LinkedList<AppWindowToken> mTmpUpdateAllDrawn = new LinkedList<>();
    private WindowState mTmpWindow;
    private WindowState mTmpWindow2;
    private final HashMap<IBinder, WindowToken> mTokenMap = new HashMap<>();
    private Region mTouchExcludeRegion = new Region();
    final UnknownAppVisibilityController mUnknownAppVisibilityController;
    private boolean mUpdateImeTarget;
    private final Consumer<WindowState> mUpdateWallpaperForAnimator = $$Lambda$DisplayContent$GuCKVzKP141d6J0gfRAjKtuBJUU.INSTANCE;
    private final Consumer<WindowState> mUpdateWindowsForAnimator = new Consumer() {
        /* class com.android.server.wm.$$Lambda$DisplayContent$0yxrqH9eGY2qTjH1u_BvaVrXCSA */

        @Override // java.util.function.Consumer
        public final void accept(Object obj) {
            DisplayContent.this.lambda$new$0$DisplayContent((WindowState) obj);
        }
    };
    boolean mWaitingForConfig;
    WallpaperController mWallpaperController;
    boolean mWallpaperMayChange = false;
    final ArrayList<WindowState> mWinAddedSinceNullFocus = new ArrayList<>();
    final ArrayList<WindowState> mWinRemovedSinceNullFocus = new ArrayList<>();
    private final float mWindowCornerRadius;
    private SurfaceControl mWindowingLayer;
    int pendingLayoutChanges;

    @Retention(RetentionPolicy.SOURCE)
    @interface ForceScalingMode {
    }

    public /* synthetic */ void lambda$new$0$DisplayContent(WindowState w) {
        WindowStateAnimator winAnimator = w.mWinAnimator;
        AppWindowToken atoken = w.mAppToken;
        if (winAnimator.mDrawState != 3) {
            return;
        }
        if ((atoken == null || atoken.canShowWindows()) && w.performShowLocked()) {
            this.pendingLayoutChanges |= 8;
            if (WindowManagerDebugConfig.DEBUG_LAYOUT_REPEATS) {
                this.mWmService.mWindowPlacerLocked.debugLayoutRepeats("updateWindowsAndWallpaperLocked 5", this.pendingLayoutChanges);
            }
        }
    }

    static /* synthetic */ void lambda$new$1(WindowState w) {
        AnimationAdapter anim;
        int color;
        TaskStack stack;
        WindowStateAnimator winAnimator = w.mWinAnimator;
        if (winAnimator.mSurfaceController != null && winAnimator.hasSurface()) {
            if (w.mAppToken != null) {
                anim = w.mAppToken.getAnimation();
            } else {
                anim = w.getAnimation();
            }
            if (anim != null && (color = anim.getBackgroundColor()) != 0 && (stack = w.getStack()) != null) {
                stack.setAnimationBackground(winAnimator, color);
            }
        }
    }

    public /* synthetic */ void lambda$new$2$DisplayContent(WindowState w) {
        int lostFocusUid = this.mTmpWindow.mOwnerUid;
        Handler handler = this.mWmService.mH;
        if (w.mAttrs.type == 2005 && w.mOwnerUid == lostFocusUid && !handler.hasMessages(52, w)) {
            handler.sendMessageDelayed(handler.obtainMessage(52, w), w.mAttrs.hideTimeoutMilliseconds);
        }
    }

    public /* synthetic */ boolean lambda$new$3$DisplayContent(WindowState w) {
        AppWindowToken focusedApp = this.mFocusedApp;
        if (WindowManagerDebugConfig.DEBUG_FOCUS) {
            Slog.v("WindowManager", "Looking for focus: " + w + ", flags=" + w.mAttrs.flags + ", canReceive=" + w.canReceiveKeys());
        }
        if (!w.canReceiveKeys()) {
            return false;
        }
        AppWindowToken wtoken = w.mAppToken;
        if (wtoken != null && (wtoken.removed || wtoken.sendingToBottom)) {
            if (WindowManagerDebugConfig.DEBUG_FOCUS) {
                StringBuilder sb = new StringBuilder();
                sb.append("Skipping ");
                sb.append(wtoken);
                sb.append(" because ");
                sb.append(wtoken.removed ? "removed" : "sendingToBottom");
                Slog.v("WindowManager", sb.toString());
            }
            return false;
        } else if (focusedApp == null) {
            if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                Slog.v("WindowManager", "findFocusedWindow: focusedApp=null using new focus @ " + w);
            }
            this.mTmpWindow = w;
            return true;
        } else if (!focusedApp.windowsAreFocusable()) {
            if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                Slog.v("WindowManager", "findFocusedWindow: focusedApp windows not focusable using new focus @ " + w);
            }
            this.mTmpWindow = w;
            return true;
        } else if (wtoken == null || w.mAttrs.type == 3 || focusedApp.compareTo((WindowContainer) wtoken) <= 0) {
            if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                Slog.v("WindowManager", "findFocusedWindow: Found new focus @ " + w);
            }
            this.mTmpWindow = w;
            return true;
        } else {
            if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
                Slog.v("WindowManager", "findFocusedWindow: Reached focused app=" + focusedApp);
            }
            this.mTmpWindow = null;
            return true;
        }
    }

    public /* synthetic */ void lambda$new$4$DisplayContent(WindowState w) {
        boolean gone = (this.mTmpWindow != null && this.mWmService.mPolicy.canBeHiddenByKeyguardLw(w)) || w.isGoneForLayoutLw();
        if (WindowManagerDebugConfig.DEBUG_LAYOUT && !w.mLayoutAttached) {
            Slog.v("WindowManager", "1ST PASS " + w + ": gone=" + gone + " mHaveFrame=" + w.mHaveFrame + " mLayoutAttached=" + w.mLayoutAttached + " config reported=" + w.isLastConfigReportedToClient());
            AppWindowToken atoken = w.mAppToken;
            if (gone) {
                StringBuilder sb = new StringBuilder();
                sb.append("  GONE: mViewVisibility=");
                sb.append(w.mViewVisibility);
                sb.append(" mRelayoutCalled=");
                sb.append(w.mRelayoutCalled);
                sb.append(" hidden=");
                sb.append(w.mToken.isHidden());
                sb.append(" hiddenRequested=");
                sb.append(atoken != null && atoken.hiddenRequested);
                sb.append(" parentHidden=");
                sb.append(w.isParentWindowHidden());
                Slog.v("WindowManager", sb.toString());
            } else {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("  VIS: mViewVisibility=");
                sb2.append(w.mViewVisibility);
                sb2.append(" mRelayoutCalled=");
                sb2.append(w.mRelayoutCalled);
                sb2.append(" hidden=");
                sb2.append(w.mToken.isHidden());
                sb2.append(" hiddenRequested=");
                sb2.append(atoken != null && atoken.hiddenRequested);
                sb2.append(" parentHidden=");
                sb2.append(w.isParentWindowHidden());
                Slog.v("WindowManager", sb2.toString());
            }
        }
        if ((!gone || !w.mHaveFrame || w.mLayoutNeeded) && !w.mLayoutAttached) {
            OppoFeatureCache.get(IColorFullScreenDisplayManager.DEFAULT).injectorDisplayContentConsumer(this, w, this.mCurrentFocus);
            if (this.mTmpInitial) {
                w.resetContentChanged();
            }
            if (w.mAttrs.type == 2023) {
                this.mTmpWindow = w;
            }
            w.mLayoutNeeded = false;
            w.prelayout();
            boolean firstLayout = !w.isLaidOut();
            getDisplayPolicy().layoutWindowLw(w, null, this.mDisplayFrames);
            w.mLayoutSeq = this.mLayoutSeq;
            if (firstLayout) {
                w.updateLastInsetValues();
            }
            if (w.mAppToken != null) {
                w.mAppToken.layoutLetterbox(w);
            }
            if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                Slog.v("WindowManager", "  LAYOUT: mFrame=" + w.getFrameLw() + " mContainingFrame=" + w.getContainingFrame() + " mDisplayFrame=" + w.getDisplayFrameLw());
            }
        }
    }

    public /* synthetic */ void lambda$new$5$DisplayContent(WindowState w) {
        if (w.mLayoutAttached) {
            if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                Slog.v("WindowManager", "2ND PASS " + w + " mHaveFrame=" + w.mHaveFrame + " mViewVisibility=" + w.mViewVisibility + " mRelayoutCalled=" + w.mRelayoutCalled);
            }
            if (this.mTmpWindow != null && this.mWmService.mPolicy.canBeHiddenByKeyguardLw(w)) {
                return;
            }
            if ((w.mViewVisibility != 8 && w.mRelayoutCalled) || !w.mHaveFrame || w.mLayoutNeeded) {
                if (this.mTmpInitial) {
                    w.resetContentChanged();
                }
                w.mLayoutNeeded = false;
                w.prelayout();
                getDisplayPolicy().layoutWindowLw(w, w.getParentWindow(), this.mDisplayFrames);
                w.mLayoutSeq = this.mLayoutSeq;
                if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                    Slog.v("WindowManager", " LAYOUT: mFrame=" + w.getFrameLw() + " mContainingFrame=" + w.getContainingFrame() + " mDisplayFrame=" + w.getDisplayFrameLw());
                }
            }
        } else if (w.mAttrs.type == 2023) {
            this.mTmpWindow = this.mTmpWindow2;
        }
    }

    public /* synthetic */ boolean lambda$new$6$DisplayContent(WindowState w) {
        if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD && this.mUpdateImeTarget) {
            Slog.i("WindowManager", "Checking window @" + w + " fl=0x" + Integer.toHexString(w.mAttrs.flags));
        }
        return w.canBeImeTarget();
    }

    public /* synthetic */ void lambda$new$7$DisplayContent(WindowState w) {
        getDisplayPolicy().applyPostLayoutPolicyLw(w, w.mAttrs, w.getParentWindow(), this.mInputMethodTarget);
    }

    public /* synthetic */ void lambda$new$8$DisplayContent(WindowState w) {
        WindowSurfacePlacer surfacePlacer = this.mWmService.mWindowPlacerLocked;
        boolean z = false;
        boolean obscuredChanged = w.mObscured != this.mTmpApplySurfaceChangesTransactionState.obscured;
        RootWindowContainer root = this.mWmService.mRoot;
        w.mObscured = this.mTmpApplySurfaceChangesTransactionState.obscured;
        if (!this.mTmpApplySurfaceChangesTransactionState.obscured) {
            boolean isDisplayed = w.isDisplayedLw();
            if (isDisplayed && w.isObscuringDisplay()) {
                root.mObscuringWindow = w;
                this.mTmpApplySurfaceChangesTransactionState.obscured = true;
            }
            this.mTmpApplySurfaceChangesTransactionState.displayHasContent |= root.handleNotObscuredLocked(w, this.mTmpApplySurfaceChangesTransactionState.obscured, this.mTmpApplySurfaceChangesTransactionState.syswin);
            if (w.mHasSurface && isDisplayed) {
                int type = w.mAttrs.type;
                if (type == 2008 || type == 2010 || (w.mAttrs.privateFlags & 1024) != 0) {
                    this.mTmpApplySurfaceChangesTransactionState.syswin = true;
                }
                if (this.mTmpApplySurfaceChangesTransactionState.preferredRefreshRate == OppoBrightUtils.MIN_LUX_LIMITI && w.mAttrs.preferredRefreshRate != OppoBrightUtils.MIN_LUX_LIMITI) {
                    this.mTmpApplySurfaceChangesTransactionState.preferredRefreshRate = w.mAttrs.preferredRefreshRate;
                }
                OppoDisplayModeManager oppoDisplayModeManager = this.mOppoDisplayModeManager;
                if (w == getDisplayPolicy().mTopFullscreenOpaqueWindowState) {
                    z = true;
                }
                oppoDisplayModeManager.applyPreferredMode(w, z);
                int preferredModeId = getDisplayPolicy().getRefreshRatePolicy().getPreferredModeId(w);
                if (this.mTmpApplySurfaceChangesTransactionState.preferredModeId == 0 && preferredModeId != 0) {
                    this.mTmpApplySurfaceChangesTransactionState.preferredModeId = preferredModeId;
                }
            }
        }
        if (obscuredChanged && w.isVisibleLw() && this.mWallpaperController.isWallpaperTarget(w)) {
            this.mWallpaperController.updateWallpaperVisibility();
        }
        w.handleWindowMovedIfNeeded();
        WindowStateAnimator winAnimator = w.mWinAnimator;
        w.resetContentChanged();
        if (w.mHasSurface) {
            boolean committed = winAnimator.commitFinishDrawingLocked();
            if (this.isDefaultDisplay && committed) {
                if (w.mAttrs.type == 2023) {
                    this.pendingLayoutChanges |= 1;
                    if (WindowManagerDebugConfig.DEBUG_LAYOUT_REPEATS) {
                        surfacePlacer.debugLayoutRepeats("dream and commitFinishDrawingLocked true", this.pendingLayoutChanges);
                    }
                }
                if ((w.mAttrs.flags & DumpState.DUMP_DEXOPT) != 0) {
                    if (WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT) {
                        Slog.v("WindowManager", "First draw done in potential wallpaper target " + w);
                    }
                    this.mWallpaperMayChange = true;
                    this.pendingLayoutChanges |= 4;
                    if (WindowManagerDebugConfig.DEBUG_LAYOUT_REPEATS) {
                        surfacePlacer.debugLayoutRepeats("wallpaper and commitFinishDrawingLocked true", this.pendingLayoutChanges);
                    }
                }
            }
        }
        AppWindowToken atoken = w.mAppToken;
        if (atoken != null) {
            atoken.updateLetterboxSurface(w);
            if (atoken.updateDrawnWindowStates(w) && !this.mTmpUpdateAllDrawn.contains(atoken)) {
                this.mTmpUpdateAllDrawn.add(atoken);
            }
        }
        if (!this.mLosingFocus.isEmpty() && w.isFocused() && w.isDisplayedLw()) {
            this.mWmService.mH.obtainMessage(3, this).sendToTarget();
        }
        w.updateResizingWindowIfNeeded();
    }

    DisplayContent(Display display, WindowManagerService service, ActivityDisplay activityDisplay) {
        super(service);
        this.mAcitvityDisplay = activityDisplay;
        if (service.mRoot.getDisplayContent(display.getDisplayId()) == null) {
            this.mDisplay = display;
            this.mDisplayId = display.getDisplayId();
            this.mWallpaperController = new WallpaperController(this.mWmService, this);
            display.getDisplayInfo(this.mDisplayInfo);
            display.getMetrics(this.mDisplayMetrics);
            this.mSystemGestureExclusionLimit = (this.mWmService.mSystemGestureExclusionLimitDp * this.mDisplayMetrics.densityDpi) / 160;
            this.isDefaultDisplay = this.mDisplayId == 0;
            int i = this.mDisplayId;
            DisplayInfo displayInfo = this.mDisplayInfo;
            this.mDisplayFrames = new DisplayFrames(i, displayInfo, calculateDisplayCutoutForRotation(displayInfo.rotation));
            initializeDisplayBaseInfo();
            this.mAppTransition = new AppTransition(service.mContext, service, this);
            this.mAppTransition.registerListenerLocked(service.mActivityManagerAppTransitionNotifier);
            this.mAppTransitionController = new AppTransitionController(service, this);
            this.mUnknownAppVisibilityController = new UnknownAppVisibilityController(service, this);
            this.mBoundsAnimationController = new BoundsAnimationController(service.mContext, this.mAppTransition, AnimationThread.getHandler(), new AnimationHandler());
            InputManagerService inputManagerService = this.mWmService.mInputManager;
            this.mPointerEventDispatcher = new PointerEventDispatcher(inputManagerService.monitorInput("PointerEventDispatcher" + this.mDisplayId, this.mDisplayId));
            this.mTapDetector = new TaskTapPointerEventListener(this.mWmService, this);
            registerPointerEventListener(this.mTapDetector);
            registerPointerEventListener(this.mWmService.mMousePositionTracker);
            if (this.mWmService.mAtmService.getRecentTasks() != null) {
                registerPointerEventListener(this.mWmService.mAtmService.getRecentTasks().getInputListener());
            }
            this.mDisplayPolicy = OppoFeatureCache.get(IColorFullScreenDisplayManager.DEFAULT).createDisplayPolicy(service, this);
            this.mDisplayRotation = new DisplayRotation(service, this);
            this.mCloseToSquareMaxAspectRatio = service.mContext.getResources().getFloat(17105052);
            if (this.isDefaultDisplay) {
                this.mWmService.mPolicy.setDefaultDisplay(this);
            }
            if (this.mWmService.mDisplayReady) {
                this.mDisplayPolicy.onConfigurationChanged();
            }
            if (this.mWmService.mSystemReady) {
                this.mDisplayPolicy.systemReady();
            }
            this.mWindowCornerRadius = this.mDisplayPolicy.getWindowCornerRadius();
            this.mDividerControllerLocked = new DockedStackDividerController(service, this);
            this.mPinnedStackControllerLocked = new PinnedStackController(service, this);
            SurfaceControl.Builder b = this.mWmService.makeSurfaceBuilder(this.mSession).setOpaque(true).setContainerLayer();
            this.mWindowingLayer = b.setName("Display Root").build();
            this.mOverlayLayer = b.setName("Display Overlays").build();
            getPendingTransaction().setLayer(this.mWindowingLayer, 0).setLayerStack(this.mWindowingLayer, this.mDisplayId).show(this.mWindowingLayer).setLayer(this.mOverlayLayer, 1).setLayerStack(this.mOverlayLayer, this.mDisplayId).show(this.mOverlayLayer);
            getPendingTransaction().apply();
            super.addChild((DisplayContent) this.mBelowAppWindowsContainers, (Comparator<DisplayContent>) null);
            super.addChild((DisplayContent) this.mTaskStackContainers, (Comparator<DisplayContent>) null);
            super.addChild((DisplayContent) this.mAboveAppWindowsContainers, (Comparator<DisplayContent>) null);
            super.addChild((DisplayContent) this.mImeWindowsContainers, (Comparator<DisplayContent>) null);
            this.mWmService.mRoot.addChild((RootWindowContainer) this, (Comparator<RootWindowContainer>) null);
            this.mDisplayReady = true;
            this.mWmService.mAnimator.addDisplayLocked(this.mDisplayId);
            this.mInputMonitor = new InputMonitor(service, this.mDisplayId);
            this.mInsetsStateController = new InsetsStateController(this);
            this.mOppoDisplayModeManager = new OppoDisplayModeManager(service.mContext, this, this.mDisplay);
            return;
        }
        throw new IllegalArgumentException("Display with ID=" + display.getDisplayId() + " already exists=" + service.mRoot.getDisplayContent(display.getDisplayId()) + " new=" + display);
    }

    /* access modifiers changed from: package-private */
    public OppoDisplayModeManager getOppoDisplayModeManager() {
        return this.mOppoDisplayModeManager;
    }

    /* access modifiers changed from: package-private */
    public boolean isReady() {
        return this.mWmService.mDisplayReady && this.mDisplayReady;
    }

    /* access modifiers changed from: package-private */
    public int getDisplayId() {
        return this.mDisplayId;
    }

    /* access modifiers changed from: package-private */
    public float getWindowCornerRadius() {
        return this.mWindowCornerRadius;
    }

    /* access modifiers changed from: package-private */
    public WindowToken getWindowToken(IBinder binder) {
        return this.mTokenMap.get(binder);
    }

    /* access modifiers changed from: package-private */
    public AppWindowToken getAppWindowToken(IBinder binder) {
        WindowToken token = getWindowToken(binder);
        if (token == null) {
            return null;
        }
        return token.asAppWindowToken();
    }

    private void addWindowToken(IBinder binder, WindowToken token) {
        DisplayContent dc = this.mWmService.mRoot.getWindowTokenDisplay(token);
        if (dc != null) {
            throw new IllegalArgumentException("Can't map token=" + token + " to display=" + getName() + " already mapped to display=" + dc + " tokens=" + dc.mTokenMap);
        } else if (binder == null) {
            throw new IllegalArgumentException("Can't map token=" + token + " to display=" + getName() + " binder is null");
        } else if (token != null) {
            this.mTokenMap.put(binder, token);
            if (token.asAppWindowToken() == null) {
                switch (token.windowType) {
                    case 2011:
                    case 2012:
                        this.mImeWindowsContainers.addChild(token);
                        return;
                    case 2013:
                        this.mBelowAppWindowsContainers.addChild(token);
                        return;
                    default:
                        if (OppoFeatureCache.get(IColorBreenoManager.DEFAULT).isBreeno()) {
                            this.mBelowAppWindowsContainers.addChild(token);
                            return;
                        } else {
                            this.mAboveAppWindowsContainers.addChild(token);
                            return;
                        }
                }
            }
        } else {
            throw new IllegalArgumentException("Can't map null token to display=" + getName() + " binder=" + binder);
        }
    }

    /* access modifiers changed from: package-private */
    public WindowToken removeWindowToken(IBinder binder) {
        WindowToken token = this.mTokenMap.remove(binder);
        if (token != null && token.asAppWindowToken() == null) {
            token.setExiting();
        }
        return token;
    }

    /* access modifiers changed from: package-private */
    public void reParentWindowToken(WindowToken token) {
        DisplayContent prevDc = token.getDisplayContent();
        if (prevDc != this) {
            if (prevDc != null) {
                if (prevDc.mTokenMap.remove(token.token) != null && token.asAppWindowToken() == null) {
                    token.getParent().removeChild(token);
                }
                if (prevDc.mLastFocus == this.mCurrentFocus) {
                    prevDc.mLastFocus = null;
                }
            }
            addWindowToken(token.token, token);
        }
    }

    /* access modifiers changed from: package-private */
    public void removeAppToken(IBinder binder) {
        WindowToken token = removeWindowToken(binder);
        if (token == null) {
            Slog.w("WindowManager", "removeAppToken: Attempted to remove non-existing token: " + binder);
            return;
        }
        AppWindowToken appToken = token.asAppWindowToken();
        if (appToken == null) {
            Slog.w("WindowManager", "Attempted to remove non-App token: " + binder + " token=" + token);
            return;
        }
        appToken.onRemovedFromDisplay();
    }

    @Override // com.android.server.policy.WindowManagerPolicy.DisplayContentInfo
    public Display getDisplay() {
        return this.mDisplay;
    }

    /* access modifiers changed from: package-private */
    public DisplayInfo getDisplayInfo() {
        return this.mDisplayInfo;
    }

    /* access modifiers changed from: package-private */
    public DisplayMetrics getDisplayMetrics() {
        return this.mDisplayMetrics;
    }

    /* access modifiers changed from: package-private */
    public DisplayPolicy getDisplayPolicy() {
        return this.mDisplayPolicy;
    }

    @Override // com.android.server.policy.WindowManagerPolicy.DisplayContentInfo
    public DisplayRotation getDisplayRotation() {
        return this.mDisplayRotation;
    }

    /* access modifiers changed from: package-private */
    public void setInsetProvider(int type, WindowState win, TriConsumer<DisplayFrames, WindowState, Rect> frameProvider) {
        this.mInsetsStateController.getSourceProvider(type).setWindow(win, frameProvider);
    }

    /* access modifiers changed from: package-private */
    public InsetsStateController getInsetsStateController() {
        return this.mInsetsStateController;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setDisplayRotation(DisplayRotation displayRotation) {
        this.mDisplayRotation = displayRotation;
    }

    /* access modifiers changed from: package-private */
    public int getRotation() {
        return this.mRotation;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setRotation(int newRotation) {
        this.mRotation = newRotation;
        this.mDisplayRotation.setRotation(newRotation);
    }

    /* access modifiers changed from: package-private */
    public int getLastOrientation() {
        return this.mLastOrientation;
    }

    /* access modifiers changed from: package-private */
    public int getLastWindowForcedOrientation() {
        return this.mLastWindowForcedOrientation;
    }

    /* access modifiers changed from: package-private */
    public void registerRemoteAnimations(RemoteAnimationDefinition definition) {
        this.mAppTransitionController.registerRemoteAnimations(definition);
    }

    /* access modifiers changed from: package-private */
    public void pauseRotationLocked() {
        this.mDeferredRotationPauseCount++;
    }

    /* access modifiers changed from: package-private */
    public void resumeRotationLocked() {
        int i = this.mDeferredRotationPauseCount;
        if (i > 0) {
            this.mDeferredRotationPauseCount = i - 1;
            if (this.mDeferredRotationPauseCount == 0) {
                updateRotationAndSendNewConfigIfNeeded();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean rotationNeedsUpdate() {
        int lastOrientation = getLastOrientation();
        int oldRotation = getRotation();
        return oldRotation != this.mDisplayRotation.rotationForOrientation(lastOrientation, oldRotation);
    }

    /* access modifiers changed from: package-private */
    public void initializeDisplayOverrideConfiguration() {
        ActivityDisplay activityDisplay = this.mAcitvityDisplay;
        if (activityDisplay != null) {
            activityDisplay.onInitializeOverrideConfiguration(getRequestedOverrideConfiguration());
        }
    }

    /* access modifiers changed from: package-private */
    public void sendNewConfiguration() {
        this.mWmService.mH.obtainMessage(18, this).sendToTarget();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public boolean onDescendantOrientationChanged(IBinder freezeDisplayToken, ConfigurationContainer requestingContainer) {
        Configuration config = updateOrientationFromAppTokens(getRequestedOverrideConfiguration(), freezeDisplayToken, false);
        boolean handled = getDisplayRotation().respectAppRequestedOrientation();
        if (config == null) {
            return handled;
        }
        if (!handled || !(requestingContainer instanceof ActivityRecord)) {
            this.mWmService.mAtmService.updateDisplayOverrideConfigurationLocked(config, null, false, getDisplayId());
        } else {
            ActivityRecord activityRecord = (ActivityRecord) requestingContainer;
            boolean kept = this.mWmService.mAtmService.updateDisplayOverrideConfigurationLocked(config, activityRecord, false, getDisplayId());
            activityRecord.frozenBeforeDestroy = true;
            if (!kept) {
                this.mWmService.mAtmService.mRootActivityContainer.resumeFocusedStacksTopActivities();
            }
        }
        return handled;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public boolean handlesOrientationChangeFromDescendant() {
        return getDisplayRotation().respectAppRequestedOrientation();
    }

    /* access modifiers changed from: package-private */
    public boolean updateOrientationFromAppTokens() {
        return updateOrientationFromAppTokens(false);
    }

    /* access modifiers changed from: package-private */
    public Configuration updateOrientationFromAppTokens(Configuration currentConfig, IBinder freezeDisplayToken, boolean forceUpdate) {
        AppWindowToken atoken;
        if (!this.mDisplayReady) {
            return null;
        }
        if (updateOrientationFromAppTokens(forceUpdate)) {
            if (!(freezeDisplayToken == null || this.mWmService.mRoot.mOrientationChangeComplete || (atoken = getAppWindowToken(freezeDisplayToken)) == null)) {
                atoken.startFreezingScreen();
            }
            Configuration config = new Configuration();
            computeScreenConfiguration(config);
            return config;
        } else if (currentConfig == null) {
            return null;
        } else {
            this.mTmpConfiguration.unset();
            this.mTmpConfiguration.updateFrom(currentConfig);
            computeScreenConfiguration(this.mTmpConfiguration);
            if (currentConfig.diff(this.mTmpConfiguration) == 0) {
                return null;
            }
            this.mWaitingForConfig = true;
            setLayoutNeeded();
            int[] anim = new int[2];
            getDisplayPolicy().selectRotationAnimationLw(anim);
            this.mWmService.startFreezingDisplayLocked(anim[0], anim[1], this);
            return new Configuration(this.mTmpConfiguration);
        }
    }

    private boolean updateOrientationFromAppTokens(boolean forceUpdate) {
        int req = getOrientation();
        if (req == this.mLastOrientation && !forceUpdate) {
            return false;
        }
        this.mLastOrientation = req;
        this.mDisplayRotation.setCurrentOrientation(req);
        return updateRotationUnchecked(forceUpdate);
    }

    /* access modifiers changed from: package-private */
    public boolean updateRotationAndSendNewConfigIfNeeded() {
        boolean changed = updateRotationUnchecked(false);
        if (changed) {
            sendNewConfiguration();
        }
        return changed;
    }

    /* access modifiers changed from: package-private */
    public boolean updateRotationUnchecked() {
        return updateRotationUnchecked(false);
    }

    /* access modifiers changed from: package-private */
    public boolean updateRotationUnchecked(boolean forceUpdate) {
        if (!forceUpdate) {
            if (this.mDeferredRotationPauseCount > 0) {
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.v("WindowManager", "Deferring rotation, rotation is paused.");
                }
                return false;
            }
            ScreenRotationAnimation screenRotationAnimation = this.mWmService.mAnimator.getScreenRotationAnimationLocked(this.mDisplayId);
            if (screenRotationAnimation != null && screenRotationAnimation.isAnimating()) {
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.v("WindowManager", "Deferring rotation, animation in progress.");
                }
                return false;
            } else if (this.mWmService.mDisplayFrozen) {
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.v("WindowManager", "Deferring rotation, still finishing previous rotation");
                }
                return false;
            }
        }
        if (!this.mWmService.mDisplayEnabled) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v("WindowManager", "Deferring rotation, display is not enabled.");
            }
            return false;
        } else if (this.mWmService.isRotationLockForBootAnimation()) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v("WindowManager", "Do not rotate the screen while the phone is shutdown");
            }
            return false;
        } else {
            int oldRotation = this.mRotation;
            int lastOrientation = this.mLastOrientation;
            int rotation = this.mDisplayRotation.rotationForOrientation(lastOrientation, oldRotation);
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v("WindowManager", "Computed rotation=" + rotation + " for display id=" + this.mDisplayId + " based on lastOrientation=" + lastOrientation + " and oldRotation=" + oldRotation);
            }
            boolean mayRotateSeamlessly = this.mDisplayPolicy.shouldRotateSeamlessly(this.mDisplayRotation, oldRotation, rotation);
            if (mayRotateSeamlessly) {
                if (getWindow($$Lambda$DisplayContent$05CtqlkxQvjLanO8D5BmaCdILKQ.INSTANCE) != null && !forceUpdate) {
                    return false;
                }
                if (hasPinnedStack()) {
                    mayRotateSeamlessly = false;
                }
                int i = 0;
                while (true) {
                    if (i >= this.mWmService.mSessions.size()) {
                        break;
                    } else if (this.mWmService.mSessions.valueAt(i).hasAlertWindowSurfaces()) {
                        mayRotateSeamlessly = false;
                        break;
                    } else {
                        i++;
                    }
                }
            }
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v("WindowManager", "Display id=" + this.mDisplayId + " selected orientation " + lastOrientation + ", got rotation " + rotation);
            }
            if (oldRotation == rotation) {
                return false;
            }
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v("WindowManager", "Display id=" + this.mDisplayId + " rotation changed to " + rotation + " from " + oldRotation + ", lastOrientation=" + lastOrientation);
            }
            if (deltaRotation(rotation, oldRotation) != 2) {
                this.mWaitingForConfig = true;
            }
            this.mRotation = rotation;
            this.mWmService.mWindowsFreezingScreen = 1;
            this.mWmService.mH.sendNewMessageDelayed(11, this, 2000);
            setLayoutNeeded();
            int[] anim = new int[2];
            this.mDisplayPolicy.selectRotationAnimationLw(anim);
            if (!mayRotateSeamlessly) {
                this.mWmService.startFreezingDisplayLocked(anim[0], anim[1], this);
            } else {
                this.mWmService.startSeamlessRotation();
            }
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public void applyRotationLocked(int oldRotation, int rotation) {
        this.mDisplayRotation.setRotation(rotation);
        boolean rotateSeamlessly = this.mWmService.isRotatingSeamlessly();
        ScreenRotationAnimation screenRotationAnimation = rotateSeamlessly ? null : this.mWmService.mAnimator.getScreenRotationAnimationLocked(this.mDisplayId);
        updateDisplayAndOrientation(getConfiguration().uiMode, null);
        if (screenRotationAnimation != null && screenRotationAnimation.hasScreenshot() && screenRotationAnimation.setRotation(getPendingTransaction(), rotation, 10000, this.mWmService.getTransitionAnimationScaleLocked(), this.mDisplayInfo.logicalWidth, this.mDisplayInfo.logicalHeight)) {
            this.mWmService.scheduleAnimationLocked();
        }
        forAllWindows((Consumer<WindowState>) new Consumer(oldRotation, rotation, rotateSeamlessly) {
            /* class com.android.server.wm.$$Lambda$DisplayContent$3g7y7M5XrDR3cz8tOp9f3pwWbyQ */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;
            private final /* synthetic */ boolean f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                DisplayContent.this.lambda$applyRotationLocked$10$DisplayContent(this.f$1, this.f$2, this.f$3, (WindowState) obj);
            }
        }, true);
        this.mWmService.mDisplayManagerInternal.performTraversal(getPendingTransaction());
        scheduleAnimation();
        forAllWindows((Consumer<WindowState>) new Consumer(rotateSeamlessly) {
            /* class com.android.server.wm.$$Lambda$DisplayContent$XeeexVnAosqA0zfHVCT_Txqwl8 */
            private final /* synthetic */ boolean f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                DisplayContent.this.lambda$applyRotationLocked$11$DisplayContent(this.f$1, (WindowState) obj);
            }
        }, true);
        if (rotateSeamlessly) {
            this.mWmService.mH.sendNewMessageDelayed(54, this, 2000);
        }
        for (int i = this.mWmService.mRotationWatchers.size() - 1; i >= 0; i--) {
            WindowManagerService.RotationWatcher rotationWatcher = this.mWmService.mRotationWatchers.get(i);
            if (rotationWatcher.mDisplayId == this.mDisplayId) {
                try {
                    rotationWatcher.mWatcher.onRotationChanged(rotation);
                } catch (RemoteException e) {
                }
            }
        }
        if (screenRotationAnimation == null && this.mWmService.mAccessibilityController != null) {
            this.mWmService.mAccessibilityController.onRotationChangedLocked(this);
        }
    }

    public /* synthetic */ void lambda$applyRotationLocked$10$DisplayContent(int oldRotation, int rotation, boolean rotateSeamlessly, WindowState w) {
        w.seamlesslyRotateIfAllowed(getPendingTransaction(), oldRotation, rotation, rotateSeamlessly);
    }

    public /* synthetic */ void lambda$applyRotationLocked$11$DisplayContent(boolean rotateSeamlessly, WindowState w) {
        if (w.mHasSurface && !rotateSeamlessly) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v("WindowManager", "Set mOrientationChanging of " + w);
            }
            w.setOrientationChanging(true);
            this.mWmService.mRoot.mOrientationChangeComplete = false;
            w.mLastFreezeDuration = 0;
        }
        w.mReportOrientationChanged = true;
    }

    /* access modifiers changed from: package-private */
    public void configureDisplayPolicy() {
        int longSize;
        int shortSize;
        int width = this.mBaseDisplayWidth;
        int height = this.mBaseDisplayHeight;
        if (width > height) {
            shortSize = height;
            longSize = width;
        } else {
            shortSize = width;
            longSize = height;
        }
        int i = this.mBaseDisplayDensity;
        this.mDisplayPolicy.updateConfigurationAndScreenSizeDependentBehaviors();
        this.mDisplayRotation.configure(width, height, (shortSize * 160) / i, (longSize * 160) / i);
        DisplayFrames displayFrames = this.mDisplayFrames;
        DisplayInfo displayInfo = this.mDisplayInfo;
        displayFrames.onDisplayInfoUpdated(displayInfo, calculateDisplayCutoutForRotation(displayInfo.rotation));
        this.mIgnoreRotationForApps = isNonDecorDisplayCloseToSquare(0, width, height);
    }

    private boolean isNonDecorDisplayCloseToSquare(int rotation, int width, int height) {
        DisplayCutout displayCutout = calculateDisplayCutoutForRotation(rotation).getDisplayCutout();
        int uiMode = this.mWmService.mPolicy.getUiMode();
        int w = this.mDisplayPolicy.getNonDecorDisplayWidth(width, height, rotation, uiMode, displayCutout);
        int h = this.mDisplayPolicy.getNonDecorDisplayHeight(width, height, rotation, uiMode, displayCutout);
        return ((float) Math.max(w, h)) / ((float) Math.min(w, h)) <= this.mCloseToSquareMaxAspectRatio;
    }

    private DisplayInfo updateDisplayAndOrientation(int uiMode, Configuration outConfig) {
        int i = this.mRotation;
        boolean rotated = true;
        if (!(i == 1 || i == 3)) {
            rotated = false;
        }
        int dw = rotated ? this.mBaseDisplayHeight : this.mBaseDisplayWidth;
        int dh = rotated ? this.mBaseDisplayWidth : this.mBaseDisplayHeight;
        DisplayCutout displayCutout = calculateDisplayCutoutForRotation(this.mRotation).getDisplayCutout();
        int appWidth = this.mDisplayPolicy.getNonDecorDisplayWidth(dw, dh, this.mRotation, uiMode, displayCutout);
        int appHeight = this.mDisplayPolicy.getNonDecorDisplayHeight(dw, dh, this.mRotation, uiMode, displayCutout);
        DisplayInfo displayInfo = this.mDisplayInfo;
        displayInfo.rotation = this.mRotation;
        displayInfo.logicalWidth = dw;
        displayInfo.logicalHeight = dh;
        displayInfo.logicalDensityDpi = this.mBaseDisplayDensity;
        displayInfo.appWidth = appWidth;
        displayInfo.appHeight = appHeight;
        if (this.isDefaultDisplay) {
            displayInfo.getLogicalMetrics(this.mRealDisplayMetrics, CompatibilityInfo.DEFAULT_COMPATIBILITY_INFO, (Configuration) null);
        }
        this.mDisplayInfo.displayCutout = displayCutout.isEmpty() ? null : displayCutout;
        this.mDisplayInfo.getAppMetrics(this.mDisplayMetrics);
        if (this.mDisplayScalingDisabled) {
            this.mDisplayInfo.flags |= 1073741824;
        } else {
            this.mDisplayInfo.flags &= -1073741825;
        }
        DisplayInfo overrideDisplayInfo = null;
        computeSizeRangesAndScreenLayout(this.mDisplayInfo, rotated, uiMode, dw, dh, this.mDisplayMetrics.density, outConfig);
        if (this.mShouldOverrideDisplayConfiguration) {
            overrideDisplayInfo = this.mDisplayInfo;
        }
        this.mWmService.mDisplayManagerInternal.setDisplayInfoOverrideFromWindowManager(this.mDisplayId, overrideDisplayInfo);
        this.mBaseDisplayRect.set(0, 0, dw, dh);
        if (this.isDefaultDisplay) {
            this.mCompatibleScreenScale = CompatibilityInfo.computeCompatibleScaling(this.mDisplayMetrics, this.mCompatDisplayMetrics);
        }
        return this.mDisplayInfo;
    }

    /* access modifiers changed from: package-private */
    public WmDisplayCutout calculateDisplayCutoutForRotation(int rotation) {
        return this.mDisplayCutoutCache.getOrCompute(this.mInitialDisplayCutout, rotation);
    }

    /* access modifiers changed from: private */
    public WmDisplayCutout calculateDisplayCutoutForRotationUncached(DisplayCutout cutout, int rotation) {
        if (cutout == null || cutout == DisplayCutout.NO_CUTOUT) {
            return WmDisplayCutout.NO_CUTOUT;
        }
        if (rotation == 0) {
            return WmDisplayCutout.computeSafeInsets(cutout, this.mInitialDisplayWidth, this.mInitialDisplayHeight);
        }
        boolean rotated = true;
        if (!(rotation == 1 || rotation == 3)) {
            rotated = false;
        }
        return WmDisplayCutout.computeSafeInsets(DisplayCutout.fromBounds(this.mRotationUtil.getRotatedBounds(WmDisplayCutout.computeSafeInsets(cutout, this.mInitialDisplayWidth, this.mInitialDisplayHeight).getDisplayCutout().getBoundingRectsAll(), rotation, this.mInitialDisplayWidth, this.mInitialDisplayHeight)), rotated ? this.mInitialDisplayHeight : this.mInitialDisplayWidth, rotated ? this.mInitialDisplayWidth : this.mInitialDisplayHeight);
    }

    /* access modifiers changed from: package-private */
    public void computeScreenConfiguration(Configuration config) {
        int i;
        int i2;
        int i3;
        boolean rotated;
        int i4;
        DisplayInfo displayInfo = updateDisplayAndOrientation(config.uiMode, config);
        calculateBounds(displayInfo, this.mTmpBounds);
        config.windowConfiguration.setBounds(this.mTmpBounds);
        int dw = displayInfo.logicalWidth;
        int dh = displayInfo.logicalHeight;
        config.orientation = dw <= dh ? 1 : 2;
        config.windowConfiguration.setWindowingMode(getWindowingMode());
        config.windowConfiguration.setDisplayWindowingMode(getWindowingMode());
        config.windowConfiguration.setRotation(displayInfo.rotation);
        float density = this.mDisplayMetrics.density;
        config.screenWidthDp = (int) (((float) this.mDisplayPolicy.getConfigDisplayWidth(dw, dh, displayInfo.rotation, config.uiMode, displayInfo.displayCutout)) / density);
        config.screenHeightDp = (int) (((float) this.mDisplayPolicy.getConfigDisplayHeight(dw, dh, displayInfo.rotation, config.uiMode, displayInfo.displayCutout)) / density);
        this.mDisplayPolicy.getNonDecorInsetsLw(displayInfo.rotation, dw, dh, displayInfo.displayCutout, this.mTmpRect);
        int leftInset = this.mTmpRect.left;
        int topInset = this.mTmpRect.top;
        config.windowConfiguration.setAppBounds(leftInset, topInset, displayInfo.appWidth + leftInset, displayInfo.appHeight + topInset);
        boolean rotated2 = displayInfo.rotation == 1 || displayInfo.rotation == 3;
        int i5 = config.screenLayout & -769;
        if ((displayInfo.flags & 16) != 0) {
            i = 512;
        } else {
            i = 256;
        }
        config.screenLayout = i5 | i;
        config.compatScreenWidthDp = (int) (((float) config.screenWidthDp) / this.mCompatibleScreenScale);
        config.compatScreenHeightDp = (int) (((float) config.screenHeightDp) / this.mCompatibleScreenScale);
        config.compatSmallestScreenWidthDp = computeCompatSmallestWidth(rotated2, config.uiMode, dw, dh, displayInfo.displayCutout);
        config.densityDpi = displayInfo.logicalDensityDpi;
        if (!displayInfo.isHdr() || !this.mWmService.hasHdrSupport()) {
            i2 = 4;
        } else {
            i2 = 8;
        }
        if (!displayInfo.isWideColorGamut() || !this.mWmService.hasWideColorGamutSupport()) {
            i3 = 1;
        } else {
            i3 = 2;
        }
        config.colorMode = i2 | i3;
        config.touchscreen = 1;
        config.keyboard = 1;
        config.navigation = 1;
        int keyboardPresence = 0;
        int navigationPresence = 0;
        InputDevice[] devices = this.mWmService.mInputManager.getInputDevices();
        int len = devices != null ? devices.length : 0;
        int i6 = 0;
        while (i6 < len) {
            InputDevice device = devices[i6];
            if (device.isVirtual()) {
                rotated = rotated2;
            } else {
                rotated = rotated2;
                if (this.mWmService.mInputManager.canDispatchToDisplay(device.getId(), displayInfo.type == 5 ? 0 : this.mDisplayId)) {
                    int sources = device.getSources();
                    int presenceFlag = device.isExternal() ? 2 : 1;
                    if (!this.mWmService.mIsTouchDevice) {
                        config.touchscreen = 1;
                    } else if ((sources & UsbACInterface.FORMAT_II_AC3) == 4098) {
                        config.touchscreen = 3;
                    }
                    if ((sources & 65540) == 65540) {
                        config.navigation = 3;
                        navigationPresence |= presenceFlag;
                        i4 = 2;
                    } else if ((sources & UsbTerminalTypes.TERMINAL_IN_MIC) == 513 && config.navigation == 1) {
                        i4 = 2;
                        config.navigation = 2;
                        navigationPresence |= presenceFlag;
                    } else {
                        i4 = 2;
                    }
                    if (device.getKeyboardType() == i4) {
                        config.keyboard = i4;
                        keyboardPresence |= presenceFlag;
                    }
                }
            }
            i6++;
            rotated2 = rotated;
        }
        if (config.navigation == 1 && this.mWmService.mHasPermanentDpad) {
            config.navigation = 2;
            navigationPresence |= 1;
        }
        boolean hardKeyboardAvailable = config.keyboard != 1;
        if (hardKeyboardAvailable != this.mWmService.mHardKeyboardAvailable) {
            this.mWmService.mHardKeyboardAvailable = hardKeyboardAvailable;
            this.mWmService.mH.removeMessages(22);
            this.mWmService.mH.sendEmptyMessage(22);
        }
        this.mDisplayPolicy.updateConfigurationAndScreenSizeDependentBehaviors();
        config.keyboardHidden = 1;
        config.hardKeyboardHidden = 1;
        config.navigationHidden = 1;
        this.mWmService.mPolicy.adjustConfigurationLw(config, keyboardPresence, navigationPresence);
    }

    private int computeCompatSmallestWidth(boolean rotated, int uiMode, int dw, int dh, DisplayCutout displayCutout) {
        int unrotDh;
        int unrotDw;
        this.mTmpDisplayMetrics.setTo(this.mDisplayMetrics);
        DisplayMetrics tmpDm = this.mTmpDisplayMetrics;
        if (rotated) {
            unrotDw = dh;
            unrotDh = dw;
        } else {
            unrotDw = dw;
            unrotDh = dh;
        }
        return reduceCompatConfigWidthSize(reduceCompatConfigWidthSize(reduceCompatConfigWidthSize(reduceCompatConfigWidthSize(0, 0, uiMode, tmpDm, unrotDw, unrotDh, displayCutout), 1, uiMode, tmpDm, unrotDh, unrotDw, displayCutout), 2, uiMode, tmpDm, unrotDw, unrotDh, displayCutout), 3, uiMode, tmpDm, unrotDh, unrotDw, displayCutout);
    }

    private int reduceCompatConfigWidthSize(int curSize, int rotation, int uiMode, DisplayMetrics dm, int dw, int dh, DisplayCutout displayCutout) {
        dm.noncompatWidthPixels = this.mDisplayPolicy.getNonDecorDisplayWidth(dw, dh, rotation, uiMode, displayCutout);
        dm.noncompatHeightPixels = this.mDisplayPolicy.getNonDecorDisplayHeight(dw, dh, rotation, uiMode, displayCutout);
        int size = (int) (((((float) dm.noncompatWidthPixels) / CompatibilityInfo.computeCompatibleScaling(dm, (DisplayMetrics) null)) / dm.density) + 0.5f);
        return (curSize == 0 || size < curSize) ? size : curSize;
    }

    private void computeSizeRangesAndScreenLayout(DisplayInfo displayInfo, boolean rotated, int uiMode, int dw, int dh, float density, Configuration outConfig) {
        int unrotDh;
        int unrotDw;
        if (rotated) {
            unrotDw = dh;
            unrotDh = dw;
        } else {
            unrotDw = dw;
            unrotDh = dh;
        }
        displayInfo.smallestNominalAppWidth = 1073741824;
        displayInfo.smallestNominalAppHeight = 1073741824;
        displayInfo.largestNominalAppWidth = 0;
        displayInfo.largestNominalAppHeight = 0;
        adjustDisplaySizeRanges(displayInfo, 0, uiMode, unrotDw, unrotDh);
        adjustDisplaySizeRanges(displayInfo, 1, uiMode, unrotDh, unrotDw);
        adjustDisplaySizeRanges(displayInfo, 2, uiMode, unrotDw, unrotDh);
        adjustDisplaySizeRanges(displayInfo, 3, uiMode, unrotDh, unrotDw);
        if (outConfig != null) {
            int sl = reduceConfigLayout(reduceConfigLayout(reduceConfigLayout(reduceConfigLayout(Configuration.resetScreenLayout(outConfig.screenLayout), 0, density, unrotDw, unrotDh, uiMode, displayInfo.displayCutout), 1, density, unrotDh, unrotDw, uiMode, displayInfo.displayCutout), 2, density, unrotDw, unrotDh, uiMode, displayInfo.displayCutout), 3, density, unrotDh, unrotDw, uiMode, displayInfo.displayCutout);
            outConfig.smallestScreenWidthDp = (int) (((float) displayInfo.smallestNominalAppWidth) / density);
            outConfig.screenLayout = sl;
        }
    }

    private int reduceConfigLayout(int curLayout, int rotation, float density, int dw, int dh, int uiMode, DisplayCutout displayCutout) {
        int longSize = this.mDisplayPolicy.getNonDecorDisplayWidth(dw, dh, rotation, uiMode, displayCutout);
        int shortSize = this.mDisplayPolicy.getNonDecorDisplayHeight(dw, dh, rotation, uiMode, displayCutout);
        if (longSize < shortSize) {
            longSize = shortSize;
            shortSize = longSize;
        }
        return Configuration.reduceScreenLayout(curLayout, (int) (((float) longSize) / density), (int) (((float) shortSize) / density));
    }

    private void adjustDisplaySizeRanges(DisplayInfo displayInfo, int rotation, int uiMode, int dw, int dh) {
        DisplayCutout displayCutout = calculateDisplayCutoutForRotation(rotation).getDisplayCutout();
        int width = this.mDisplayPolicy.getConfigDisplayWidth(dw, dh, rotation, uiMode, displayCutout);
        if (width < displayInfo.smallestNominalAppWidth) {
            displayInfo.smallestNominalAppWidth = width;
        }
        if (width > displayInfo.largestNominalAppWidth) {
            displayInfo.largestNominalAppWidth = width;
        }
        int height = this.mDisplayPolicy.getConfigDisplayHeight(dw, dh, rotation, uiMode, displayCutout);
        if (height < displayInfo.smallestNominalAppHeight) {
            displayInfo.smallestNominalAppHeight = height;
        }
        if (height > displayInfo.largestNominalAppHeight) {
            displayInfo.largestNominalAppHeight = height;
        }
    }

    /* access modifiers changed from: package-private */
    public int getPreferredOptionsPanelGravity() {
        int rotation = getRotation();
        if (this.mInitialDisplayWidth < this.mInitialDisplayHeight) {
            if (rotation != 1) {
                return (rotation == 2 || rotation != 3) ? 81 : 8388691;
            }
            return 85;
        } else if (rotation == 1) {
            return 81;
        } else {
            if (rotation != 2) {
                return rotation != 3 ? 85 : 81;
            }
            return 8388691;
        }
    }

    /* access modifiers changed from: package-private */
    public DockedStackDividerController getDockedDividerController() {
        return this.mDividerControllerLocked;
    }

    /* access modifiers changed from: package-private */
    public PinnedStackController getPinnedStackController() {
        return this.mPinnedStackControllerLocked;
    }

    /* access modifiers changed from: package-private */
    public boolean hasAccess(int uid) {
        return this.mDisplay.hasAccess(uid);
    }

    /* access modifiers changed from: package-private */
    public boolean isPrivate() {
        return (this.mDisplay.getFlags() & 4) != 0;
    }

    /* access modifiers changed from: package-private */
    public TaskStack getHomeStack() {
        return this.mTaskStackContainers.getHomeStack();
    }

    /* access modifiers changed from: package-private */
    public TaskStack getSplitScreenPrimaryStack() {
        TaskStack stack = this.mTaskStackContainers.getSplitScreenPrimaryStack();
        if (stack == null || !stack.isVisible()) {
            return null;
        }
        return stack;
    }

    /* access modifiers changed from: package-private */
    public boolean hasSplitScreenPrimaryStack() {
        return getSplitScreenPrimaryStack() != null;
    }

    /* access modifiers changed from: package-private */
    public TaskStack getSplitScreenPrimaryStackIgnoringVisibility() {
        return this.mTaskStackContainers.getSplitScreenPrimaryStack();
    }

    /* access modifiers changed from: package-private */
    public TaskStack getPinnedStack() {
        return this.mTaskStackContainers.getPinnedStack();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean hasPinnedStack() {
        return this.mTaskStackContainers.getPinnedStack() != null;
    }

    /* access modifiers changed from: package-private */
    public TaskStack getTopStackInWindowingMode(int windowingMode) {
        return getStack(windowingMode, 0);
    }

    /* access modifiers changed from: package-private */
    public TaskStack getStack(int windowingMode, int activityType) {
        return this.mTaskStackContainers.getStack(windowingMode, activityType);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public WindowList<TaskStack> getStacks() {
        return this.mTaskStackContainers.mChildren;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public TaskStack getTopStack() {
        return this.mTaskStackContainers.getTopStack();
    }

    /* access modifiers changed from: package-private */
    public ArrayList<Task> getVisibleTasks() {
        return this.mTaskStackContainers.getVisibleTasks();
    }

    /* access modifiers changed from: package-private */
    public void onStackWindowingModeChanged(TaskStack stack) {
        this.mTaskStackContainers.onStackWindowingModeChanged(stack);
    }

    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.ConfigurationContainer
    public void onConfigurationChanged(Configuration newParentConfig) {
        int lastOrientation = getConfiguration().orientation;
        super.onConfigurationChanged(newParentConfig);
        DisplayPolicy displayPolicy = this.mDisplayPolicy;
        if (displayPolicy != null) {
            displayPolicy.onConfigurationChanged();
        }
        if (lastOrientation != getConfiguration().orientation) {
            getMetricsLogger().write(new LogMaker(1659).setSubtype(getConfiguration().orientation).addTaggedData(1660, Integer.valueOf(getDisplayId())));
            OppoFeatureCache.get(IColorFullScreenDisplayManager.DEFAULT).sendMessageToWmService(IColorFullScreenDisplayManager.CONFIG_DISPLAY_FULL_SCREEN_WINDOW, newParentConfig.orientation);
        }
        if (this.mPinnedStackControllerLocked != null && !hasPinnedStack()) {
            this.mPinnedStackControllerLocked.onDisplayInfoChanged(getDisplayInfo());
        }
    }

    /* access modifiers changed from: package-private */
    public void preOnConfigurationChanged() {
        if (getDockedDividerController() != null) {
            getDockedDividerController().onConfigurationChanged();
        }
        if (getPinnedStackController() != null) {
            getPinnedStackController().onConfigurationChanged();
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public boolean fillsParent() {
        return true;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public boolean isVisible() {
        return true;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void onAppTransitionDone() {
        super.onAppTransitionDone();
        this.mWmService.mWindowsChanged = true;
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public void setWindowingMode(int windowingMode) {
        super.setWindowingMode(windowingMode);
        super.setDisplayWindowingMode(windowingMode);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.ConfigurationContainer
    public void setDisplayWindowingMode(int windowingMode) {
        setWindowingMode(windowingMode);
    }

    private boolean skipTraverseChild(WindowContainer child) {
        if (child != this.mImeWindowsContainers || this.mInputMethodTarget == null || hasSplitScreenPrimaryStack()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public boolean forAllWindows(ToBooleanFunction<WindowState> callback, boolean traverseTopToBottom) {
        if (traverseTopToBottom) {
            for (int i = this.mChildren.size() - 1; i >= 0; i--) {
                DisplayChildWindowContainer child = (DisplayChildWindowContainer) this.mChildren.get(i);
                if (!skipTraverseChild(child) && child.forAllWindows(callback, traverseTopToBottom)) {
                    return true;
                }
            }
            return false;
        }
        int count = this.mChildren.size();
        for (int i2 = 0; i2 < count; i2++) {
            DisplayChildWindowContainer child2 = (DisplayChildWindowContainer) this.mChildren.get(i2);
            if (!skipTraverseChild(child2) && child2.forAllWindows(callback, traverseTopToBottom)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean forAllImeWindows(ToBooleanFunction<WindowState> callback, boolean traverseTopToBottom) {
        return this.mImeWindowsContainers.forAllWindows(callback, traverseTopToBottom);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public int getOrientation() {
        if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
            this.mWmService.mWindowManagerDebugger.debugGetOrientation("WindowManager", this.mWmService.mDisplayFrozen, this.mLastWindowForcedOrientation, this.mLastKeyguardForcedOrientation);
        }
        WindowManagerPolicy policy = this.mWmService.mPolicy;
        if (this.mIgnoreRotationForApps) {
            return 2;
        }
        if (!this.mWmService.mDisplayFrozen) {
            int orientation = this.mAboveAppWindowsContainers.getOrientation();
            if (orientation != -2) {
                return orientation;
            }
        } else if (this.mLastWindowForcedOrientation != -1) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v("WindowManager", "Display id=" + this.mDisplayId + " is frozen, return " + this.mLastWindowForcedOrientation);
            }
            return this.mLastWindowForcedOrientation;
        } else if (policy.isKeyguardLocked()) {
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v("WindowManager", "Display id=" + this.mDisplayId + " is frozen while keyguard locked, return " + this.mLastOrientation);
            }
            return this.mLastOrientation;
        }
        return this.mTaskStackContainers.getOrientation();
    }

    /* access modifiers changed from: package-private */
    public void updateDisplayInfo() {
        updateBaseDisplayMetricsIfNeeded();
        this.mDisplay.getDisplayInfo(this.mDisplayInfo);
        this.mDisplay.getMetrics(this.mDisplayMetrics);
        onDisplayChanged(this);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void onDisplayChanged(DisplayContent dc) {
        super.onDisplayChanged(dc);
        updateSystemGestureExclusionLimit();
        OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).displayChanged(dc);
        this.mBelowAppWindowsContainers.adjustForDisplayChanged();
    }

    /* access modifiers changed from: package-private */
    public void updateSystemGestureExclusionLimit() {
        this.mSystemGestureExclusionLimit = (this.mWmService.mSystemGestureExclusionLimitDp * this.mDisplayMetrics.densityDpi) / 160;
        updateSystemGestureExclusion();
    }

    /* access modifiers changed from: package-private */
    public void initializeDisplayBaseInfo() {
        DisplayInfo newDisplayInfo;
        DisplayManagerInternal displayManagerInternal = this.mWmService.mDisplayManagerInternal;
        if (!(displayManagerInternal == null || (newDisplayInfo = displayManagerInternal.getDisplayInfo(this.mDisplayId)) == null)) {
            this.mDisplayInfo.copyFrom(newDisplayInfo);
        }
        updateBaseDisplayMetrics(this.mDisplayInfo.logicalWidth, this.mDisplayInfo.logicalHeight, this.mDisplayInfo.logicalDensityDpi);
        this.mInitialDisplayWidth = this.mDisplayInfo.logicalWidth;
        this.mInitialDisplayHeight = this.mDisplayInfo.logicalHeight;
        this.mInitialDisplayDensity = this.mDisplayInfo.logicalDensityDpi;
        this.mInitialDisplayCutout = this.mDisplayInfo.displayCutout;
    }

    private void updateBaseDisplayMetricsIfNeeded() {
        this.mWmService.mDisplayManagerInternal.getNonOverrideDisplayInfo(this.mDisplayId, this.mDisplayInfo);
        int orientation = this.mDisplayInfo.rotation;
        boolean isDisplayDensityForced = false;
        boolean rotated = orientation == 1 || orientation == 3;
        DisplayInfo displayInfo = this.mDisplayInfo;
        int newWidth = rotated ? displayInfo.logicalHeight : displayInfo.logicalWidth;
        DisplayInfo displayInfo2 = this.mDisplayInfo;
        int newHeight = rotated ? displayInfo2.logicalWidth : displayInfo2.logicalHeight;
        int newDensity = this.mDisplayInfo.logicalDensityDpi;
        DisplayCutout newCutout = this.mDisplayInfo.displayCutout;
        if ((this.mInitialDisplayWidth == newWidth && this.mInitialDisplayHeight == newHeight && this.mInitialDisplayDensity == this.mDisplayInfo.logicalDensityDpi && Objects.equals(this.mInitialDisplayCutout, newCutout)) ? false : true) {
            boolean isDisplaySizeForced = (this.mBaseDisplayWidth == this.mInitialDisplayWidth && this.mBaseDisplayHeight == this.mInitialDisplayHeight) ? false : true;
            if (this.mBaseDisplayDensity != this.mInitialDisplayDensity) {
                isDisplayDensityForced = true;
            }
            updateBaseDisplayMetrics(isDisplaySizeForced ? this.mBaseDisplayWidth : newWidth, isDisplaySizeForced ? this.mBaseDisplayHeight : newHeight, isDisplayDensityForced ? this.mBaseDisplayDensity : newDensity);
            this.mInitialDisplayWidth = newWidth;
            this.mInitialDisplayHeight = newHeight;
            this.mInitialDisplayDensity = newDensity;
            this.mInitialDisplayCutout = newCutout;
            this.mWmService.reconfigureDisplayLocked(this);
        }
    }

    /* access modifiers changed from: package-private */
    public void setMaxUiWidth(int width) {
        if (WindowManagerDebugConfig.DEBUG_DISPLAY) {
            Slog.v("WindowManager", "Setting max ui width:" + width + " on display:" + getDisplayId());
        }
        this.mMaxUiWidth = width;
        updateBaseDisplayMetrics(this.mBaseDisplayWidth, this.mBaseDisplayHeight, this.mBaseDisplayDensity);
    }

    /* access modifiers changed from: package-private */
    public void updateBaseDisplayMetrics(int baseWidth, int baseHeight, int baseDensity) {
        int i;
        this.mBaseDisplayWidth = baseWidth;
        this.mBaseDisplayHeight = baseHeight;
        this.mBaseDisplayDensity = baseDensity;
        int i2 = this.mMaxUiWidth;
        if (i2 > 0 && (i = this.mBaseDisplayWidth) > i2) {
            this.mBaseDisplayHeight = (this.mBaseDisplayHeight * i2) / i;
            this.mBaseDisplayDensity = (this.mBaseDisplayDensity * i2) / i;
            this.mBaseDisplayWidth = i2;
            if (WindowManagerDebugConfig.DEBUG_DISPLAY) {
                Slog.v("WindowManager", "Applying config restraints:" + this.mBaseDisplayWidth + "x" + this.mBaseDisplayHeight + " at density:" + this.mBaseDisplayDensity + " on display:" + getDisplayId());
            }
        }
        this.mBaseDisplayRect.set(0, 0, this.mBaseDisplayWidth, this.mBaseDisplayHeight);
        updateBounds();
    }

    /* access modifiers changed from: package-private */
    public void setForcedDensity(int density, int userId) {
        boolean updateCurrent = true;
        if (density == this.mInitialDisplayDensity) {
        }
        if (userId != -2) {
            updateCurrent = false;
        }
        if (this.mWmService.mCurrentUserId == userId || updateCurrent) {
            this.mBaseDisplayDensity = density;
            this.mWmService.reconfigureDisplayLocked(this);
        }
        if (!updateCurrent) {
            if (density == this.mInitialDisplayDensity) {
                density = 0;
            }
            this.mWmService.mDisplayWindowSettings.setForcedDensity(this, density, userId);
        }
    }

    /* access modifiers changed from: package-private */
    public void setForcedScalingMode(int mode) {
        boolean z = true;
        if (mode != 1) {
            mode = 0;
        }
        if (mode == 0) {
            z = false;
        }
        this.mDisplayScalingDisabled = z;
        StringBuilder sb = new StringBuilder();
        sb.append("Using display scaling mode: ");
        sb.append(this.mDisplayScalingDisabled ? "off" : UiModeManagerService.Shell.NIGHT_MODE_STR_AUTO);
        Slog.i("WindowManager", sb.toString());
        this.mWmService.reconfigureDisplayLocked(this);
        this.mWmService.mDisplayWindowSettings.setForcedScalingMode(this, mode);
    }

    /* access modifiers changed from: package-private */
    public void setForcedSize(int width, int height) {
        boolean clear = this.mInitialDisplayWidth == width && this.mInitialDisplayHeight == height;
        if (!clear) {
            width = Math.min(Math.max(width, 200), this.mInitialDisplayWidth * 2);
            height = Math.min(Math.max(height, 200), this.mInitialDisplayHeight * 2);
        }
        Slog.i("WindowManager", "Using new display size: " + width + "x" + height);
        updateBaseDisplayMetrics(width, height, this.mBaseDisplayDensity);
        this.mWmService.reconfigureDisplayLocked(this);
        if (clear) {
            height = 0;
            width = 0;
        }
        this.mWmService.mDisplayWindowSettings.setForcedSize(this, width, height);
    }

    /* access modifiers changed from: package-private */
    public void getStableRect(Rect out) {
        out.set(this.mDisplayFrames.mStable);
    }

    /* access modifiers changed from: package-private */
    public void setStackOnDisplay(int stackId, boolean onTop, TaskStack stack) {
        if (WindowManagerDebugConfig.DEBUG_STACK) {
            Slog.d("WindowManager", "Create new stackId=" + stackId + " on displayId=" + this.mDisplayId);
        }
        this.mTaskStackContainers.addStackToDisplay(stack, onTop);
    }

    /* access modifiers changed from: package-private */
    public void moveStackToDisplay(TaskStack stack, boolean onTop) {
        DisplayContent prevDc = stack.getDisplayContent();
        if (prevDc == null) {
            throw new IllegalStateException("Trying to move stackId=" + stack.mStackId + " which is not currently attached to any display");
        } else if (prevDc.getDisplayId() != this.mDisplayId) {
            prevDc.mTaskStackContainers.removeChild(stack);
            this.mTaskStackContainers.addStackToDisplay(stack, onTop);
        } else {
            throw new IllegalArgumentException("Trying to move stackId=" + stack.mStackId + " to its current displayId=" + this.mDisplayId);
        }
    }

    /* access modifiers changed from: protected */
    public void addChild(DisplayChildWindowContainer child, Comparator<DisplayChildWindowContainer> comparator) {
        throw new UnsupportedOperationException("See DisplayChildWindowContainer");
    }

    /* access modifiers changed from: protected */
    public void addChild(DisplayChildWindowContainer child, int index) {
        throw new UnsupportedOperationException("See DisplayChildWindowContainer");
    }

    /* access modifiers changed from: protected */
    public void removeChild(DisplayChildWindowContainer child) {
        if (this.mRemovingDisplay) {
            super.removeChild((DisplayContent) child);
            return;
        }
        throw new UnsupportedOperationException("See DisplayChildWindowContainer");
    }

    /* access modifiers changed from: package-private */
    public void positionChildAt(int position, DisplayChildWindowContainer child, boolean includingParents) {
        getParent().positionChildAt(position, this, includingParents);
    }

    /* access modifiers changed from: package-private */
    public void positionStackAt(int position, TaskStack child, boolean includingParents) {
        this.mTaskStackContainers.positionChildAt(position, child, includingParents);
        layoutAndAssignWindowLayersIfNeeded();
    }

    /* access modifiers changed from: package-private */
    public boolean pointWithinAppWindow(int x, int y) {
        int[] targetWindowType = {-1};
        Consumer fn = PooledLambda.obtainConsumer(new BiConsumer(targetWindowType, x, y) {
            /* class com.android.server.wm.$$Lambda$DisplayContent$9GF6f8baPGZRvxJVeBknIuDUb_Y */
            private final /* synthetic */ int[] f$0;
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.function.BiConsumer
            public final void accept(Object obj, Object obj2) {
                DisplayContent.lambda$pointWithinAppWindow$12(this.f$0, this.f$1, this.f$2, (WindowState) obj, (Rect) obj2);
            }
        }, PooledLambda.__(WindowState.class), this.mTmpRect);
        forAllWindows((Consumer<WindowState>) fn, true);
        ((PooledConsumer) fn).recycle();
        return 1 <= targetWindowType[0] && targetWindowType[0] <= 99;
    }

    static /* synthetic */ void lambda$pointWithinAppWindow$12(int[] targetWindowType, int x, int y, WindowState w, Rect nonArg) {
        if (targetWindowType[0] == -1 && w.isOnScreen() && w.isVisibleLw() && w.getFrameLw().contains(x, y)) {
            targetWindowType[0] = w.mAttrs.type;
        }
    }

    /* access modifiers changed from: package-private */
    public Task findTaskForResizePoint(int x, int y) {
        int delta = WindowManagerService.dipToPixel(30, this.mDisplayMetrics);
        this.mTmpTaskForResizePointSearchResult.reset();
        int stackNdx = this.mTaskStackContainers.getChildCount();
        while (true) {
            stackNdx--;
            if (stackNdx < 0) {
                return null;
            }
            TaskStack stack = (TaskStack) this.mTaskStackContainers.getChildAt(stackNdx);
            if (!stack.getWindowConfiguration().canResizeTask()) {
                return null;
            }
            stack.findTaskForResizePoint(x, y, delta, this.mTmpTaskForResizePointSearchResult);
            if (this.mTmpTaskForResizePointSearchResult.searchDone) {
                return this.mTmpTaskForResizePointSearchResult.taskForResize;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateTouchExcludeRegion() {
        AppWindowToken appWindowToken = this.mFocusedApp;
        Task focusedTask = appWindowToken != null ? appWindowToken.getTask() : null;
        if (focusedTask == null) {
            this.mTouchExcludeRegion.setEmpty();
        } else {
            this.mTouchExcludeRegion.set(this.mBaseDisplayRect);
            int delta = WindowManagerService.dipToPixel(30, this.mDisplayMetrics);
            this.mTmpRect2.setEmpty();
            for (int stackNdx = this.mTaskStackContainers.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                ((TaskStack) this.mTaskStackContainers.getChildAt(stackNdx)).setTouchExcludeRegion(focusedTask, delta, this.mTouchExcludeRegion, this.mDisplayFrames.mContent, this.mTmpRect2);
            }
            if (!this.mTmpRect2.isEmpty()) {
                this.mTouchExcludeRegion.op(this.mTmpRect2, Region.Op.UNION);
            }
        }
        WindowState windowState = this.mInputMethodWindow;
        if (windowState != null && windowState.isVisibleLw()) {
            this.mInputMethodWindow.getTouchableRegion(this.mTmpRegion);
            this.mTouchExcludeRegion.op(this.mTmpRegion, Region.Op.UNION);
        }
        for (int i = this.mTapExcludedWindows.size() - 1; i >= 0; i--) {
            this.mTapExcludedWindows.get(i).getTouchableRegion(this.mTmpRegion);
            this.mTouchExcludeRegion.op(this.mTmpRegion, Region.Op.UNION);
        }
        amendWindowTapExcludeRegion(this.mTouchExcludeRegion);
        if (this.mDisplayId == 0 && getSplitScreenPrimaryStack() != null) {
            this.mDividerControllerLocked.getTouchRegion(this.mTmpRect);
            this.mTmpRegion.set(this.mTmpRect);
            this.mTouchExcludeRegion.op(this.mTmpRegion, Region.Op.UNION);
        }
        OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).amendWindowTapExcludeRegion(this, this.mTouchExcludeRegion);
        this.mTapDetector.setTouchExcludeRegion(this.mTouchExcludeRegion);
    }

    /* access modifiers changed from: package-private */
    public void amendWindowTapExcludeRegion(Region inOutRegion) {
        for (int i = this.mTapExcludeProvidingWindows.size() - 1; i >= 0; i--) {
            this.mTapExcludeProvidingWindows.valueAt(i).amendTapExcludeRegion(inOutRegion);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void switchUser() {
        super.switchUser();
        this.mWmService.mWindowsChanged = true;
        this.mDisplayPolicy.switchUser();
    }

    private void resetAnimationBackgroundAnimator() {
        for (int stackNdx = this.mTaskStackContainers.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
            ((TaskStack) this.mTaskStackContainers.getChildAt(stackNdx)).resetAnimationBackgroundAnimator();
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void removeIfPossible() {
        if (isAnimating()) {
            this.mDeferredRemoval = true;
        } else {
            removeImmediately();
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void removeImmediately() {
        this.mRemovingDisplay = true;
        try {
            this.mOpeningApps.clear();
            this.mClosingApps.clear();
            this.mChangingApps.clear();
            this.mUnknownAppVisibilityController.clear();
            this.mAppTransition.removeAppTransitionTimeoutCallbacks();
            handleAnimatingStoppedAndTransition();
            this.mWmService.stopFreezingDisplayLocked();
            super.removeImmediately();
            if (WindowManagerDebugConfig.DEBUG_DISPLAY) {
                Slog.v("WindowManager", "Removing display=" + this);
            }
            this.mPointerEventDispatcher.dispose();
            this.mWmService.mAnimator.removeDisplayLocked(this.mDisplayId);
            this.mWindowingLayer.release();
            this.mOverlayLayer.release();
            this.mInputMonitor.onDisplayRemoved();
            this.mDisplayReady = false;
            this.mRemovingDisplay = false;
            this.mWmService.mWindowPlacerLocked.requestTraversal();
        } catch (Throwable th) {
            this.mDisplayReady = false;
            this.mRemovingDisplay = false;
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public boolean checkCompleteDeferredRemoval() {
        if (super.checkCompleteDeferredRemoval() || !this.mDeferredRemoval) {
            return true;
        }
        removeImmediately();
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isRemovalDeferred() {
        return this.mDeferredRemoval;
    }

    /* access modifiers changed from: package-private */
    public boolean animateForIme(float interpolatedValue, float animationTarget, float dividerAnimationTarget) {
        boolean updated = false;
        for (int i = this.mTaskStackContainers.getChildCount() - 1; i >= 0; i--) {
            TaskStack stack = (TaskStack) this.mTaskStackContainers.getChildAt(i);
            if (stack != null && stack.isAdjustedForIme()) {
                if (interpolatedValue >= 1.0f && animationTarget == OppoBrightUtils.MIN_LUX_LIMITI && dividerAnimationTarget == OppoBrightUtils.MIN_LUX_LIMITI) {
                    stack.resetAdjustedForIme(true);
                    updated = true;
                } else {
                    DockedStackDividerController dockedStackDividerController = this.mDividerControllerLocked;
                    dockedStackDividerController.mLastAnimationProgress = dockedStackDividerController.getInterpolatedAnimationValue(interpolatedValue);
                    DockedStackDividerController dockedStackDividerController2 = this.mDividerControllerLocked;
                    dockedStackDividerController2.mLastDividerProgress = dockedStackDividerController2.getInterpolatedDividerValue(interpolatedValue);
                    updated |= stack.updateAdjustForIme(this.mDividerControllerLocked.mLastAnimationProgress, this.mDividerControllerLocked.mLastDividerProgress, false);
                }
                if (interpolatedValue >= 1.0f) {
                    stack.endImeAdjustAnimation();
                }
            }
        }
        return updated;
    }

    /* access modifiers changed from: package-private */
    public boolean clearImeAdjustAnimation() {
        boolean changed = false;
        for (int i = this.mTaskStackContainers.getChildCount() - 1; i >= 0; i--) {
            TaskStack stack = (TaskStack) this.mTaskStackContainers.getChildAt(i);
            if (stack != null && stack.isAdjustedForIme()) {
                stack.resetAdjustedForIme(true);
                changed = true;
            }
        }
        return changed;
    }

    /* access modifiers changed from: package-private */
    public void beginImeAdjustAnimation() {
        for (int i = this.mTaskStackContainers.getChildCount() - 1; i >= 0; i--) {
            TaskStack stack = (TaskStack) this.mTaskStackContainers.getChildAt(i);
            if (stack.isVisible() && stack.isAdjustedForIme()) {
                stack.beginImeAdjustAnimation();
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x00f6  */
    public void adjustForImeIfNeeded() {
        int imeHeight;
        int i;
        WindowState imeWin = this.mInputMethodWindow;
        boolean imeVisible = imeWin != null && imeWin.isVisibleLw() && imeWin.isDisplayedLw() && !this.mDividerControllerLocked.isImeHideRequested();
        TaskStack dockedStack = getSplitScreenPrimaryStack();
        boolean dockVisible = dockedStack != null;
        Task topDockedTask = dockVisible ? (Task) dockedStack.getTopChild() : null;
        TaskStack imeTargetStack = this.mWmService.getImeFocusStackLocked();
        int imeDockSide = (!dockVisible || imeTargetStack == null) ? -1 : imeTargetStack.getDockSide();
        boolean imeOnTop = imeDockSide == 2;
        int i2 = 4;
        boolean imeOnBottom = imeDockSide == 4;
        int imeHeight2 = this.mDisplayFrames.getInputMethodWindowVisibleHeight();
        boolean imeHeightChanged = imeVisible && imeHeight2 != this.mDividerControllerLocked.getImeHeightAdjustedFor();
        boolean dockMinimized = this.mDividerControllerLocked.isMinimizedDock() || (topDockedTask != null && imeOnBottom && !dockedStack.isAdjustedForIme() && dockedStack.getBounds().height() < topDockedTask.getBounds().height());
        if (imeVisible && dockVisible) {
            if (!imeOnTop && !imeOnBottom) {
                imeHeight = imeHeight2;
                for (i = this.mTaskStackContainers.getChildCount() - 1; i >= 0; i--) {
                    ((TaskStack) this.mTaskStackContainers.getChildAt(i)).resetAdjustedForIme(!dockVisible);
                }
                this.mDividerControllerLocked.setAdjustedForIme(false, false, dockVisible, imeWin, imeHeight);
                this.mPinnedStackControllerLocked.setAdjustedForIme(imeVisible, imeHeight);
            } else if (!dockMinimized) {
                int i3 = this.mTaskStackContainers.getChildCount() - 1;
                while (i3 >= 0) {
                    TaskStack stack = (TaskStack) this.mTaskStackContainers.getChildAt(i3);
                    boolean isDockedOnBottom = stack.getDockSide() == i2;
                    if (!stack.isVisible() || ((!imeOnBottom && !isDockedOnBottom) || !stack.inSplitScreenWindowingMode())) {
                        stack.resetAdjustedForIme(false);
                    } else {
                        stack.setAdjustedForIme(imeWin, imeOnBottom && imeHeightChanged);
                    }
                    i3--;
                    i2 = 4;
                }
                imeHeight = imeHeight2;
                this.mDividerControllerLocked.setAdjustedForIme(imeOnBottom, true, true, imeWin, imeHeight2);
                this.mPinnedStackControllerLocked.setAdjustedForIme(imeVisible, imeHeight);
            }
        }
        imeHeight = imeHeight2;
        while (i >= 0) {
        }
        this.mDividerControllerLocked.setAdjustedForIme(false, false, dockVisible, imeWin, imeHeight);
        this.mPinnedStackControllerLocked.setAdjustedForIme(imeVisible, imeHeight);
    }

    /* access modifiers changed from: package-private */
    public void prepareFreezingTaskBounds() {
        for (int stackNdx = this.mTaskStackContainers.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
            ((TaskStack) this.mTaskStackContainers.getChildAt(stackNdx)).prepareFreezingTaskBounds();
        }
    }

    /* access modifiers changed from: package-private */
    public void rotateBounds(int oldRotation, int newRotation, Rect bounds) {
        getBounds(this.mTmpRect, newRotation);
        rotateBounds(this.mTmpRect, oldRotation, newRotation, bounds);
    }

    /* access modifiers changed from: package-private */
    public void rotateBounds(Rect parentBounds, int oldRotation, int newRotation, Rect bounds) {
        createRotationMatrix(deltaRotation(newRotation, oldRotation), (float) parentBounds.width(), (float) parentBounds.height(), this.mTmpMatrix);
        this.mTmpRectF.set(bounds);
        this.mTmpMatrix.mapRect(this.mTmpRectF);
        this.mTmpRectF.round(bounds);
    }

    static int deltaRotation(int oldRotation, int newRotation) {
        int delta = newRotation - oldRotation;
        if (delta < 0) {
            return delta + 4;
        }
        return delta;
    }

    private static void createRotationMatrix(int rotation, float displayWidth, float displayHeight, Matrix outMatrix) {
        createRotationMatrix(rotation, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI, displayWidth, displayHeight, outMatrix);
    }

    static void createRotationMatrix(int rotation, float rectLeft, float rectTop, float displayWidth, float displayHeight, Matrix outMatrix) {
        if (rotation == 0) {
            outMatrix.reset();
        } else if (rotation == 1) {
            outMatrix.setRotate(90.0f, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI);
            outMatrix.postTranslate(displayWidth, OppoBrightUtils.MIN_LUX_LIMITI);
            outMatrix.postTranslate(-rectTop, rectLeft);
        } else if (rotation == 2) {
            outMatrix.reset();
        } else if (rotation == 3) {
            outMatrix.setRotate(270.0f, OppoBrightUtils.MIN_LUX_LIMITI, OppoBrightUtils.MIN_LUX_LIMITI);
            outMatrix.postTranslate(OppoBrightUtils.MIN_LUX_LIMITI, displayHeight);
            outMatrix.postTranslate(rectTop, OppoBrightUtils.MIN_LUX_LIMITI);
        }
    }

    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.ConfigurationContainer
    public void writeToProto(ProtoOutputStream proto, long fieldId, int logLevel) {
        if (logLevel != 2 || isVisible()) {
            long token = proto.start(fieldId);
            super.writeToProto(proto, 1146756268033L, logLevel);
            proto.write(1120986464258L, this.mDisplayId);
            for (int stackNdx = this.mTaskStackContainers.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                ((TaskStack) this.mTaskStackContainers.getChildAt(stackNdx)).writeToProto(proto, 2246267895811L, logLevel);
            }
            this.mDividerControllerLocked.writeToProto(proto, 1146756268036L);
            this.mPinnedStackControllerLocked.writeToProto(proto, 1146756268037L);
            for (int i = this.mAboveAppWindowsContainers.getChildCount() - 1; i >= 0; i--) {
                ((WindowToken) this.mAboveAppWindowsContainers.getChildAt(i)).writeToProto(proto, 2246267895814L, logLevel);
            }
            for (int i2 = this.mBelowAppWindowsContainers.getChildCount() - 1; i2 >= 0; i2--) {
                ((WindowToken) this.mBelowAppWindowsContainers.getChildAt(i2)).writeToProto(proto, 2246267895815L, logLevel);
            }
            for (int i3 = this.mImeWindowsContainers.getChildCount() - 1; i3 >= 0; i3--) {
                ((WindowToken) this.mImeWindowsContainers.getChildAt(i3)).writeToProto(proto, 2246267895816L, logLevel);
            }
            proto.write(1120986464265L, this.mBaseDisplayDensity);
            this.mDisplayInfo.writeToProto(proto, 1146756268042L);
            proto.write(1120986464267L, this.mRotation);
            ScreenRotationAnimation screenRotationAnimation = this.mWmService.mAnimator.getScreenRotationAnimationLocked(this.mDisplayId);
            if (screenRotationAnimation != null) {
                screenRotationAnimation.writeToProto(proto, 1146756268044L);
            }
            this.mDisplayFrames.writeToProto(proto, 1146756268045L);
            this.mAppTransition.writeToProto(proto, 1146756268048L);
            AppWindowToken appWindowToken = this.mFocusedApp;
            if (appWindowToken != null) {
                appWindowToken.writeNameToProto(proto, 1138166333455L);
            }
            for (int i4 = this.mOpeningApps.size() - 1; i4 >= 0; i4--) {
                this.mOpeningApps.valueAt(i4).mActivityRecord.writeIdentifierToProto(proto, 2246267895825L);
            }
            for (int i5 = this.mClosingApps.size() - 1; i5 >= 0; i5--) {
                this.mClosingApps.valueAt(i5).mActivityRecord.writeIdentifierToProto(proto, 2246267895826L);
            }
            for (int i6 = this.mChangingApps.size() - 1; i6 >= 0; i6--) {
                this.mChangingApps.valueAt(i6).mActivityRecord.writeIdentifierToProto(proto, 2246267895827L);
            }
            proto.end(token);
        }
    }

    @Override // com.android.server.wm.WindowContainer
    public void dump(PrintWriter pw, String prefix, boolean dumpAll) {
        super.dump(pw, prefix, dumpAll);
        pw.print(prefix);
        pw.print("Display: mDisplayId=");
        pw.println(this.mDisplayId);
        String subPrefix = "  " + prefix;
        pw.print(subPrefix);
        pw.print("init=");
        pw.print(this.mInitialDisplayWidth);
        pw.print("x");
        pw.print(this.mInitialDisplayHeight);
        pw.print(StringUtils.SPACE);
        pw.print(this.mInitialDisplayDensity);
        pw.print("dpi");
        if (!(this.mInitialDisplayWidth == this.mBaseDisplayWidth && this.mInitialDisplayHeight == this.mBaseDisplayHeight && this.mInitialDisplayDensity == this.mBaseDisplayDensity)) {
            pw.print(" base=");
            pw.print(this.mBaseDisplayWidth);
            pw.print("x");
            pw.print(this.mBaseDisplayHeight);
            pw.print(StringUtils.SPACE);
            pw.print(this.mBaseDisplayDensity);
            pw.print("dpi");
        }
        if (this.mDisplayScalingDisabled) {
            pw.println(" noscale");
        }
        pw.print(" cur=");
        pw.print(this.mDisplayInfo.logicalWidth);
        pw.print("x");
        pw.print(this.mDisplayInfo.logicalHeight);
        pw.print(" app=");
        pw.print(this.mDisplayInfo.appWidth);
        pw.print("x");
        pw.print(this.mDisplayInfo.appHeight);
        pw.print(" rng=");
        pw.print(this.mDisplayInfo.smallestNominalAppWidth);
        pw.print("x");
        pw.print(this.mDisplayInfo.smallestNominalAppHeight);
        pw.print("-");
        pw.print(this.mDisplayInfo.largestNominalAppWidth);
        pw.print("x");
        pw.println(this.mDisplayInfo.largestNominalAppHeight);
        pw.print(subPrefix + "deferred=" + this.mDeferredRemoval + " mLayoutNeeded=" + this.mLayoutNeeded);
        StringBuilder sb = new StringBuilder();
        sb.append(" mTouchExcludeRegion=");
        sb.append(this.mTouchExcludeRegion);
        pw.println(sb.toString());
        pw.println();
        pw.print(prefix);
        pw.print("mLayoutSeq=");
        pw.println(this.mLayoutSeq);
        pw.print(prefix);
        pw.print("mDeferredRotationPauseCount=");
        pw.println(this.mDeferredRotationPauseCount);
        pw.print("  mCurrentFocus=");
        pw.println(this.mCurrentFocus);
        if (this.mLastFocus != this.mCurrentFocus) {
            pw.print("  mLastFocus=");
            pw.println(this.mLastFocus);
        }
        if (this.mLosingFocus.size() > 0) {
            pw.println();
            pw.println("  Windows losing focus:");
            for (int i = this.mLosingFocus.size() - 1; i >= 0; i--) {
                WindowState w = this.mLosingFocus.get(i);
                pw.print("  Losing #");
                pw.print(i);
                pw.print(' ');
                pw.print(w);
                if (dumpAll) {
                    pw.println(":");
                    w.dump(pw, "    ", true);
                } else {
                    pw.println();
                }
            }
        }
        pw.print("  mFocusedApp=");
        pw.println(this.mFocusedApp);
        if (this.mLastStatusBarVisibility != 0) {
            pw.print("  mLastStatusBarVisibility=0x");
            pw.println(Integer.toHexString(this.mLastStatusBarVisibility));
        }
        pw.println();
        this.mWallpaperController.dump(pw, "  ");
        pw.println();
        pw.print("mSystemGestureExclusion=");
        if (this.mSystemGestureExclusionListeners.getRegisteredCallbackCount() > 0) {
            pw.println(this.mSystemGestureExclusion);
        } else {
            pw.println("<no lstnrs>");
        }
        pw.println();
        pw.println(prefix + "Application tokens in top down Z order:");
        for (int stackNdx = this.mTaskStackContainers.getChildCount() - 1; stackNdx >= 0; stackNdx += -1) {
            ((TaskStack) this.mTaskStackContainers.getChildAt(stackNdx)).dump(pw, prefix + "  ", dumpAll);
        }
        pw.println();
        if (!this.mExitingTokens.isEmpty()) {
            pw.println();
            pw.println("  Exiting tokens:");
            for (int i2 = this.mExitingTokens.size() - 1; i2 >= 0; i2--) {
                WindowToken token = this.mExitingTokens.get(i2);
                pw.print("  Exiting #");
                pw.print(i2);
                pw.print(' ');
                pw.print(token);
                pw.println(':');
                token.dump(pw, "    ", dumpAll);
            }
        }
        pw.println();
        TaskStack homeStack = getHomeStack();
        if (homeStack != null) {
            pw.println(prefix + "homeStack=" + homeStack.getName());
        }
        TaskStack pinnedStack = getPinnedStack();
        if (pinnedStack != null) {
            pw.println(prefix + "pinnedStack=" + pinnedStack.getName());
        }
        TaskStack splitScreenPrimaryStack = getSplitScreenPrimaryStack();
        if (splitScreenPrimaryStack != null) {
            pw.println(prefix + "splitScreenPrimaryStack=" + splitScreenPrimaryStack.getName());
        }
        pw.println();
        this.mDividerControllerLocked.dump(prefix, pw);
        pw.println();
        this.mPinnedStackControllerLocked.dump(prefix, pw);
        pw.println();
        this.mDisplayFrames.dump(prefix, pw);
        pw.println();
        this.mDisplayPolicy.dump(prefix, pw);
        pw.println();
        this.mDisplayRotation.dump(prefix, pw);
        pw.println();
        this.mInputMonitor.dump(pw, "  ");
        pw.println();
        this.mInsetsStateController.dump(prefix, pw);
    }

    public String toString() {
        return "Display " + this.mDisplayId + " info=" + this.mDisplayInfo + " stacks=" + this.mChildren;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.ConfigurationContainer
    public String getName() {
        return "Display " + this.mDisplayId + " name=\"" + this.mDisplayInfo.name + "\"";
    }

    /* access modifiers changed from: package-private */
    public boolean isStackVisible(int windowingMode) {
        TaskStack stack = getTopStackInWindowingMode(windowingMode);
        return stack != null && stack.isVisible();
    }

    /* access modifiers changed from: package-private */
    public WindowState getTouchableWinAtPointLocked(float xf, float yf) {
        return getWindow(new Predicate((int) xf, (int) yf) {
            /* class com.android.server.wm.$$Lambda$DisplayContent$_XfE1uZ9VUv6i0SxWUvqu69FNb4 */
            private final /* synthetic */ int f$1;
            private final /* synthetic */ int f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return DisplayContent.this.lambda$getTouchableWinAtPointLocked$13$DisplayContent(this.f$1, this.f$2, (WindowState) obj);
            }
        });
    }

    public /* synthetic */ boolean lambda$getTouchableWinAtPointLocked$13$DisplayContent(int x, int y, WindowState w) {
        int flags = w.mAttrs.flags;
        if (!w.isVisibleLw() || (flags & 16) != 0) {
            return false;
        }
        w.getVisibleBounds(this.mTmpRect);
        if (!this.mTmpRect.contains(x, y)) {
            return false;
        }
        w.getTouchableRegion(this.mTmpRegion);
        int touchFlags = flags & 40;
        if (this.mTmpRegion.contains(x, y) || touchFlags == 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean canAddToastWindowForUid(int uid) {
        return canAddToastWindowForUid(uid, true);
    }

    /* access modifiers changed from: package-private */
    public boolean canAddToastWindowForUid(int uid, boolean force) {
        if (1000 == uid || getWindow(new Predicate(uid) {
            /* class com.android.server.wm.$$Lambda$DisplayContent$2VlyMN8z2sOPqE9yfz3peRMI */
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return DisplayContent.lambda$canAddToastWindowForUid$14(this.f$0, (WindowState) obj);
            }
        }) != null) {
            return true;
        }
        if (!force) {
            if (getWindowNum(new Predicate(uid) {
                /* class com.android.server.wm.$$Lambda$DisplayContent$JYsrGdifTPH6ASJDC3B9YWMD2pw */
                private final /* synthetic */ int f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Predicate
                public final boolean test(Object obj) {
                    return DisplayContent.lambda$canAddToastWindowForUid$15(this.f$0, (WindowState) obj);
                }
            }) < 2) {
                return true;
            }
            return false;
        } else if (getWindow(new Predicate(uid) {
            /* class com.android.server.wm.$$Lambda$DisplayContent$TgWgYUCWfndkEPVcXMM7mhoCN0 */
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return DisplayContent.lambda$canAddToastWindowForUid$16(this.f$0, (WindowState) obj);
            }
        }) == null) {
            return true;
        } else {
            return false;
        }
    }

    static /* synthetic */ boolean lambda$canAddToastWindowForUid$14(int uid, WindowState w) {
        return w.mOwnerUid == uid && w.isFocused();
    }

    static /* synthetic */ boolean lambda$canAddToastWindowForUid$15(int uid, WindowState w) {
        return w.mAttrs.type == 2005 && w.mOwnerUid == uid && !w.mPermanentlyHidden && !w.mWindowRemovalAllowed;
    }

    static /* synthetic */ boolean lambda$canAddToastWindowForUid$16(int uid, WindowState w) {
        return w.mAttrs.type == 2005 && w.mOwnerUid == uid && !w.mPermanentlyHidden && !w.mWindowRemovalAllowed;
    }

    /* access modifiers changed from: package-private */
    public void scheduleToastWindowsTimeoutIfNeededLocked(WindowState oldFocus, WindowState newFocus) {
        if (oldFocus == null) {
            return;
        }
        if (newFocus == null || newFocus.mOwnerUid != oldFocus.mOwnerUid) {
            this.mTmpWindow = oldFocus;
            forAllWindows(this.mScheduleToastTimeout, false);
        }
    }

    /* access modifiers changed from: package-private */
    public WindowState findFocusedWindowIfNeeded(int topFocusedDisplayId) {
        if (this.mWmService.mPerDisplayFocusEnabled || topFocusedDisplayId == -1) {
            return findFocusedWindow();
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public WindowState findFocusedWindow() {
        this.mTmpWindow = null;
        forAllWindows(this.mFindFocusedWindow, true);
        WindowState windowState = this.mTmpWindow;
        if (windowState != null) {
            return windowState;
        }
        if (WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
            Slog.v("WindowManager", "findFocusedWindow: No focusable windows.");
        }
        return null;
    }

    public void uploadFocusWindowReborn(WindowState currentFocus, WindowState newFocus) {
        if (currentFocus == null && newFocus != null && NoFocusWindow.isNoFocusNow) {
            try {
                NoFocusWindow.isNoFocusNow = false;
                TheiaUtil.getInstance().logMap.put("rebornMoment", String.valueOf(System.currentTimeMillis()));
                OppoStatistics.onCommon(this.mWmService.mContext, "CriticalLog", "Theia", TheiaUtil.getInstance().logMap, false);
            } catch (Exception e) {
                Slog.e("WindowManager", "Failed to upload reborn DCS", e);
            } catch (Throwable th) {
                TheiaUtil.getInstance().logMap.clear();
                throw th;
            }
            TheiaUtil.getInstance().logMap.clear();
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x00c0  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00ff  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x0118  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x011e  */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0128  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x013c  */
    /* JADX WARNING: Removed duplicated region for block: B:60:0x0149  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0167  */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x016b  */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x0172  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x017c  */
    /* JADX WARNING: Removed duplicated region for block: B:75:0x018c  */
    public boolean updateFocusedWindowLocked(int mode, boolean updateInputWindows, int topFocusedDisplayId) {
        WindowState oldFocus;
        int focusChanged;
        boolean appInWhiteList;
        WindowState newFocus = findFocusedWindowIfNeeded(topFocusedDisplayId);
        WindowState windowState = this.mCurrentFocus;
        if (windowState == newFocus) {
            return false;
        }
        uploadFocusWindowReborn(windowState, newFocus);
        boolean imWindowChanged = false;
        if (this.mInputMethodWindow != null) {
            imWindowChanged = this.mInputMethodTarget != computeImeTarget(true);
            if (!(mode == 1 || mode == 3)) {
                assignWindowLayers(false);
            }
        }
        if (imWindowChanged) {
            this.mWmService.mWindowsChanged = true;
            setLayoutNeeded();
            newFocus = findFocusedWindowIfNeeded(topFocusedDisplayId);
        }
        if (this.mCurrentFocus != newFocus) {
            this.mWmService.mH.obtainMessage(2, this).sendToTarget();
        }
        if (!WindowManagerDebugConfig.DEBUG_FOCUS_LIGHT) {
            WindowManagerService windowManagerService = this.mWmService;
            if (!WindowManagerService.localLOGV) {
                Slog.v("WindowManager", "Changing focus from " + this.mCurrentFocus + " to " + newFocus + " displayId=" + getDisplayId());
                oldFocus = this.mCurrentFocus;
                this.mCurrentFocus = newFocus;
                this.mLosingFocus.remove(newFocus);
                if (newFocus != null) {
                    this.mWinAddedSinceNullFocus.clear();
                    this.mWinRemovedSinceNullFocus.clear();
                    if (newFocus.canReceiveKeys()) {
                        newFocus.mToken.paused = false;
                    }
                }
                if (newFocus != null || (newFocus.getAttrs().isDisableStatusBar != 1 && (newFocus.getAttrs().memoryType & DumpState.DUMP_DEXOPT) == 0)) {
                    if (this.mWmService.mDisableStatusBar) {
                        this.mWmService.mDisableStatusBar = false;
                        this.mWmService.disableStatusBar(false);
                    }
                } else if (!this.mWmService.mDisableStatusBar) {
                    this.mWmService.disableStatusBar(true);
                    this.mWmService.mDisableStatusBar = true;
                }
                focusChanged = getDisplayPolicy().focusChangedLw(oldFocus, newFocus);
                if (imWindowChanged && oldFocus != this.mInputMethodWindow) {
                    if (mode != 2) {
                        performLayout(true, updateInputWindows);
                        focusChanged &= -2;
                    } else if (mode == 3) {
                        assignWindowLayers(false);
                    }
                }
                if ((focusChanged & 1) != 0) {
                    setLayoutNeeded();
                    if (mode == 2) {
                        performLayout(true, updateInputWindows);
                    } else if (mode == 4) {
                        this.mWmService.mRoot.performSurfacePlacement(false);
                    }
                }
                if (mode != 1) {
                    getInputMonitor().setInputFocusLw(newFocus, updateInputWindows);
                }
                adjustForImeIfNeeded();
                appInWhiteList = false;
                if (oldFocus != null) {
                    ApplicationInfo aInfo = this.mWmService.getApplicationInfo(oldFocus.getAttrs().packageName, oldFocus.mOwnerUid);
                    appInWhiteList = (aInfo == null || (aInfo.oppoPrivateFlags & 2) == 0) ? false : true;
                }
                if (appInWhiteList) {
                    scheduleToastWindowsTimeoutIfNeededLocked(oldFocus, newFocus);
                } else {
                    Slog.w("WindowManager", "Skip toast check for application in whitelist.");
                }
                if (mode == 2) {
                    this.pendingLayoutChanges |= 8;
                }
                if (this.mCurrentFocus != null) {
                    this.mWmService.mNoFocusWindow.check(TheiaUtil.getInstance().getForegroundPackage());
                } else {
                    this.mWmService.mNoFocusWindow.cancelCheck(TheiaUtil.getInstance().getForegroundPackage());
                }
                return true;
            }
        }
        Slog.v("WindowManager", "Changing focus from " + this.mCurrentFocus + " to " + newFocus + " displayId=" + getDisplayId() + " Callers=" + Debug.getCallers(4));
        oldFocus = this.mCurrentFocus;
        this.mCurrentFocus = newFocus;
        this.mLosingFocus.remove(newFocus);
        if (newFocus != null) {
        }
        if (newFocus != null) {
        }
        if (this.mWmService.mDisableStatusBar) {
        }
        focusChanged = getDisplayPolicy().focusChangedLw(oldFocus, newFocus);
        if (mode != 2) {
        }
        if ((focusChanged & 1) != 0) {
        }
        if (mode != 1) {
        }
        adjustForImeIfNeeded();
        appInWhiteList = false;
        if (oldFocus != null) {
        }
        if (appInWhiteList) {
        }
        if (mode == 2) {
        }
        if (this.mCurrentFocus != null) {
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean setFocusedApp(AppWindowToken newFocus) {
        DisplayContent appDisplay;
        if (newFocus != null && (appDisplay = newFocus.getDisplayContent()) != this) {
            StringBuilder sb = new StringBuilder();
            sb.append(newFocus);
            sb.append(" is not on ");
            sb.append(getName());
            sb.append(" but ");
            sb.append(appDisplay != null ? appDisplay.getName() : "none");
            throw new IllegalStateException(sb.toString());
        } else if (this.mFocusedApp == newFocus) {
            return false;
        } else {
            this.mFocusedApp = newFocus;
            try {
                new OppoWindowManager().requestKeyguard("FocusedAppChanged");
            } catch (Exception e) {
            }
            getInputMonitor().setFocusedAppLw(newFocus);
            updateTouchExcludeRegion();
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public void assignWindowLayers(boolean setLayoutNeeded) {
        Trace.traceBegin(32, "assignWindowLayers");
        assignChildLayers(getPendingTransaction());
        if (setLayoutNeeded) {
            setLayoutNeeded();
        }
        scheduleAnimation();
        Trace.traceEnd(32);
    }

    /* access modifiers changed from: package-private */
    public void layoutAndAssignWindowLayersIfNeeded() {
        this.mWmService.mWindowsChanged = true;
        setLayoutNeeded();
        if (!this.mWmService.updateFocusedWindowLocked(3, false)) {
            assignWindowLayers(false);
        }
        this.mInputMonitor.setUpdateInputWindowsNeededLw();
        this.mWmService.mWindowPlacerLocked.performSurfacePlacement();
        this.mInputMonitor.updateInputWindowsLw(false);
    }

    /* access modifiers changed from: package-private */
    public boolean destroyLeakedSurfaces() {
        this.mTmpWindow = null;
        forAllWindows((Consumer<WindowState>) new Consumer() {
            /* class com.android.server.wm.$$Lambda$DisplayContent$QQ7dJYii6sHazj0RT2lgUVzR4o */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                DisplayContent.this.lambda$destroyLeakedSurfaces$17$DisplayContent((WindowState) obj);
            }
        }, false);
        return this.mTmpWindow != null;
    }

    public /* synthetic */ void lambda$destroyLeakedSurfaces$17$DisplayContent(WindowState w) {
        WindowStateAnimator wsa = w.mWinAnimator;
        if (wsa.mSurfaceController != null) {
            if (!this.mWmService.mSessions.contains(wsa.mSession)) {
                Slog.w("WindowManager", "LEAKED SURFACE (session doesn't exist): " + w + " surface=" + wsa.mSurfaceController + " token=" + w.mToken + " pid=" + w.mSession.mPid + " uid=" + w.mSession.mUid);
                wsa.destroySurface();
                this.mWmService.mForceRemoves.add(w);
                this.mTmpWindow = w;
            } else if (w.mAppToken != null && w.mAppToken.isClientHidden()) {
                Slog.w("WindowManager", "LEAKED SURFACE (app token hidden): " + w + " surface=" + wsa.mSurfaceController + " token=" + w.mAppToken);
                if (WindowManagerDebugConfig.SHOW_TRANSACTIONS) {
                    WindowManagerService.logSurface(w, "LEAK DESTROY", false);
                }
                wsa.destroySurface();
                this.mTmpWindow = w;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setInputMethodWindowLocked(WindowState win) {
        this.mInputMethodWindow = win;
        WindowState windowState = this.mInputMethodWindow;
        if (windowState != null) {
            this.mWmService.mAtmInternal.onImeWindowSetOnDisplay(windowState.mSession.mPid, this.mInputMethodWindow.getDisplayId());
        }
        computeImeTarget(true);
        this.mInsetsStateController.getSourceProvider(10).setWindow(win, null);
    }

    /* access modifiers changed from: package-private */
    public WindowState computeImeTarget(boolean updateImeTarget) {
        AppWindowToken token;
        WindowState betterTarget;
        AppWindowToken token2 = null;
        if (this.mInputMethodWindow == null) {
            if (updateImeTarget) {
                if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                    Slog.w("WindowManager", "Moving IM target from " + this.mInputMethodTarget + " to null since mInputMethodWindow is null");
                }
                setInputMethodTarget(null, this.mInputMethodTargetWaitingAnim);
            }
            return null;
        }
        WindowState curTarget = this.mInputMethodTarget;
        if (!canUpdateImeTarget()) {
            if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                Slog.w("WindowManager", "Defer updating IME target");
            }
            return curTarget;
        }
        this.mUpdateImeTarget = updateImeTarget;
        WindowState target = getWindow(this.mComputeImeTargetPredicate);
        if (!(target == null || target.mAttrs.type != 3 || (token = target.mAppToken) == null || (betterTarget = token.getImeTargetBelowWindow(target)) == null)) {
            target = betterTarget;
        }
        if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD && updateImeTarget) {
            Slog.v("WindowManager", "Proposed new IME target: " + target + " for display: " + getDisplayId());
        }
        if (curTarget == null || curTarget.mRemoved || !curTarget.isDisplayedLw() || !curTarget.isClosing()) {
            if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                Slog.v("WindowManager", "Desired input method target=" + target + " updateImeTarget=" + updateImeTarget);
            }
            String str = "";
            if (target == null) {
                if (updateImeTarget) {
                    if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Moving IM target from ");
                        sb.append(curTarget);
                        sb.append(" to null.");
                        if (WindowManagerDebugConfig.SHOW_STACK_CRAWLS) {
                            str = " Callers=" + Debug.getCallers(4);
                        }
                        sb.append(str);
                        Slog.w("WindowManager", sb.toString());
                    }
                    setInputMethodTarget(null, this.mInputMethodTargetWaitingAnim);
                }
                return null;
            }
            if (updateImeTarget) {
                if (curTarget != null) {
                    token2 = curTarget.mAppToken;
                }
                if (token2 != null) {
                    WindowState highestTarget = null;
                    if (token2.isSelfAnimating()) {
                        highestTarget = token2.getHighestAnimLayerWindow(curTarget);
                    }
                    if (highestTarget != null) {
                        if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                            Slog.v("WindowManager", this.mAppTransition + StringUtils.SPACE + highestTarget + " animating=" + highestTarget.isAnimating());
                        }
                        if (this.mAppTransition.isTransitionSet()) {
                            setInputMethodTarget(highestTarget, true);
                            return highestTarget;
                        }
                    }
                }
                if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("Moving IM target from ");
                    sb2.append(curTarget);
                    sb2.append(" to ");
                    sb2.append(target);
                    if (WindowManagerDebugConfig.SHOW_STACK_CRAWLS) {
                        str = " Callers=" + Debug.getCallers(4);
                    }
                    sb2.append(str);
                    Slog.w("WindowManager", sb2.toString());
                }
                setInputMethodTarget(target, false);
            }
            return target;
        }
        if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
            Slog.v("WindowManager", "Not changing target till current window is closing and not removed");
        }
        return curTarget;
    }

    /* access modifiers changed from: package-private */
    public void computeImeTargetIfNeeded(AppWindowToken candidate) {
        WindowState windowState = this.mInputMethodTarget;
        if (windowState != null && windowState.mAppToken == candidate) {
            try {
                if (this.mInputMethodWindow.mAnimatingExit && this.mInputMethodWindow.isSelfAnimating()) {
                    if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
                        Slog.d("WindowManager", "cancel ime exit animation before update ime target.");
                    }
                    this.mInputMethodWindow.cancelAnimation();
                }
            } catch (Exception e) {
            }
            computeImeTarget(true);
        }
    }

    private void setInputMethodTarget(WindowState target, boolean targetWaitingAnim) {
        if (target != this.mInputMethodTarget || this.mInputMethodTargetWaitingAnim != targetWaitingAnim) {
            this.mInputMethodTarget = target;
            this.mInputMethodTargetWaitingAnim = targetWaitingAnim;
            assignWindowLayers(false);
            this.mInsetsStateController.onImeTargetChanged(target);
            updateImeParent();
        }
    }

    private void updateImeParent() {
        SurfaceControl newParent = this.mMagnificationSpec != null ? this.mWindowingLayer : computeImeParent();
        if (newParent != null) {
            getPendingTransaction().reparent(this.mImeWindowsContainers.mSurfaceControl, newParent);
            scheduleAnimation();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public SurfaceControl computeImeParent() {
        WindowState windowState = this.mInputMethodTarget;
        if (windowState == null || windowState.mAppToken == null || this.mInputMethodTarget.getWindowingMode() != 1 || !this.mInputMethodTarget.mAppToken.matchParentBounds()) {
            return this.mWindowingLayer;
        }
        return this.mInputMethodTarget.mAppToken.getSurfaceControl();
    }

    /* access modifiers changed from: package-private */
    public boolean getNeedsMenu(WindowState top, WindowManagerPolicy.WindowState bottom) {
        if (top.mAttrs.needsMenuKey != 0) {
            return top.mAttrs.needsMenuKey == 1;
        }
        this.mTmpWindow = null;
        WindowState candidate = getWindow(new Predicate(top, bottom) {
            /* class com.android.server.wm.$$Lambda$DisplayContent$b7G1eB_4r9Z6UM5bu4wpJRt_vA */
            private final /* synthetic */ WindowState f$1;
            private final /* synthetic */ WindowManagerPolicy.WindowState f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return DisplayContent.this.lambda$getNeedsMenu$18$DisplayContent(this.f$1, this.f$2, (WindowState) obj);
            }
        });
        return candidate != null && candidate.mAttrs.needsMenuKey == 1;
    }

    public /* synthetic */ boolean lambda$getNeedsMenu$18$DisplayContent(WindowState top, WindowManagerPolicy.WindowState bottom, WindowState w) {
        if (w == top) {
            this.mTmpWindow = w;
        }
        if (this.mTmpWindow == null) {
            return false;
        }
        if (w.mAttrs.needsMenuKey == 0 && w != bottom) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void setLayoutNeeded() {
        if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
            Slog.w("WindowManager", "setLayoutNeeded: callers=" + Debug.getCallers(3));
        }
        this.mLayoutNeeded = true;
    }

    private void clearLayoutNeeded() {
        if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
            Slog.w("WindowManager", "clearLayoutNeeded: callers=" + Debug.getCallers(3));
        }
        this.mLayoutNeeded = false;
    }

    /* access modifiers changed from: package-private */
    public boolean isLayoutNeeded() {
        return this.mLayoutNeeded;
    }

    /* access modifiers changed from: package-private */
    public void dumpTokens(PrintWriter pw, boolean dumpAll) {
        if (!this.mTokenMap.isEmpty()) {
            pw.println("  Display #" + this.mDisplayId);
            for (WindowToken token : this.mTokenMap.values()) {
                pw.print("  ");
                pw.print(token);
                if (dumpAll) {
                    pw.println(':');
                    token.dump(pw, "    ", dumpAll);
                } else {
                    pw.println();
                }
            }
            if (!this.mOpeningApps.isEmpty() || !this.mClosingApps.isEmpty() || !this.mChangingApps.isEmpty()) {
                pw.println();
                if (this.mOpeningApps.size() > 0) {
                    pw.print("  mOpeningApps=");
                    pw.println(this.mOpeningApps);
                }
                if (this.mClosingApps.size() > 0) {
                    pw.print("  mClosingApps=");
                    pw.println(this.mClosingApps);
                }
                if (this.mChangingApps.size() > 0) {
                    pw.print("  mChangingApps=");
                    pw.println(this.mChangingApps);
                }
            }
            this.mUnknownAppVisibilityController.dump(pw, "  ");
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpWindowAnimators(PrintWriter pw, String subPrefix) {
        forAllWindows((Consumer<WindowState>) new Consumer(pw, subPrefix, new int[1]) {
            /* class com.android.server.wm.$$Lambda$DisplayContent$ZnrehCvLeZkDzITBNfIu5SmJl8 */
            private final /* synthetic */ PrintWriter f$0;
            private final /* synthetic */ String f$1;
            private final /* synthetic */ int[] f$2;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                DisplayContent.lambda$dumpWindowAnimators$19(this.f$0, this.f$1, this.f$2, (WindowState) obj);
            }
        }, false);
    }

    static /* synthetic */ void lambda$dumpWindowAnimators$19(PrintWriter pw, String subPrefix, int[] index, WindowState w) {
        WindowStateAnimator wAnim = w.mWinAnimator;
        pw.println(subPrefix + "Window #" + index[0] + ": " + wAnim);
        index[0] = index[0] + 1;
    }

    /* access modifiers changed from: package-private */
    public void startKeyguardExitOnNonAppWindows(boolean onWallpaper, boolean goingToShade) {
        forAllWindows((Consumer<WindowState>) new Consumer(onWallpaper, goingToShade) {
            /* class com.android.server.wm.$$Lambda$DisplayContent$Xq69KsFgstCoqxG1I6tal_xDeZg */
            private final /* synthetic */ boolean f$1;
            private final /* synthetic */ boolean f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                DisplayContent.lambda$startKeyguardExitOnNonAppWindows$20(WindowManagerPolicy.this, this.f$1, this.f$2, (WindowState) obj);
            }
        }, true);
    }

    static /* synthetic */ void lambda$startKeyguardExitOnNonAppWindows$20(WindowManagerPolicy policy, boolean onWallpaper, boolean goingToShade, WindowState w) {
        if (w.mAppToken == null && policy.canBeHiddenByKeyguardLw(w) && w.wouldBeVisibleIfPolicyIgnored() && !w.isVisible()) {
            w.startAnimation(policy.createHiddenByKeyguardExit(onWallpaper, goingToShade));
        }
    }

    /* access modifiers changed from: package-private */
    public boolean checkWaitingForWindows() {
        this.mHaveBootMsg = false;
        this.mHaveApp = false;
        this.mHaveWallpaper = false;
        this.mHaveKeyguard = true;
        WindowState visibleWindow = getWindow(new Predicate() {
            /* class com.android.server.wm.$$Lambda$DisplayContent$V4BUXhJHbL4C8UqYPgWz5GnOLuc */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return DisplayContent.this.lambda$checkWaitingForWindows$21$DisplayContent((WindowState) obj);
            }
        });
        if (visibleWindow != null) {
            if (WindowManagerDebugConfig.DEBUG_BOOT) {
                Slog.d("WindowManager", " We have a visible window but not drawn " + visibleWindow.toString());
            }
            return true;
        }
        boolean wallpaperEnabled = this.mWmService.mContext.getResources().getBoolean(17891451) && this.mWmService.mContext.getResources().getBoolean(17891393) && !this.mWmService.mOnlyCore;
        if (WindowManagerDebugConfig.DEBUG_SCREEN_ON || WindowManagerDebugConfig.DEBUG_BOOT) {
            Slog.i("WindowManager", "******** booted=" + this.mWmService.mSystemBooted + " msg=" + this.mWmService.mShowingBootMessages + " haveBoot=" + this.mHaveBootMsg + " haveApp=" + this.mHaveApp + " haveWall=" + this.mHaveWallpaper + " wallEnabled=" + wallpaperEnabled + " haveKeyguard=" + this.mHaveKeyguard);
        }
        if (!this.mWmService.mSystemBooted && !this.mHaveBootMsg) {
            return true;
        }
        if (!this.mWmService.mSystemBooted || ((this.mHaveApp || this.mHaveKeyguard) && (!wallpaperEnabled || this.mHaveWallpaper))) {
            return false;
        }
        return true;
    }

    public /* synthetic */ boolean lambda$checkWaitingForWindows$21$DisplayContent(WindowState w) {
        if (w.isVisibleLw() && !w.mObscured && !w.isDrawnLw()) {
            return true;
        }
        if (!w.isDrawnLw()) {
            return false;
        }
        if (w.mAttrs.type == 2021) {
            this.mHaveBootMsg = true;
            return false;
        } else if (w.mAttrs.type == 2 || w.mAttrs.type == 4) {
            this.mHaveApp = true;
            return false;
        } else if (w.mAttrs.type == 2013) {
            this.mHaveWallpaper = true;
            return false;
        } else if (w.mAttrs.type != 2000) {
            return false;
        } else {
            this.mHaveKeyguard = this.mWmService.mPolicy.isKeyguardDrawnLw();
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void updateWindowsForAnimator() {
        forAllWindows(this.mUpdateWindowsForAnimator, true);
    }

    /* access modifiers changed from: package-private */
    public void updateBackgroundForAnimator() {
        resetAnimationBackgroundAnimator();
        forAllWindows(this.mUpdateWallpaperForAnimator, true);
    }

    /* access modifiers changed from: package-private */
    public boolean isInputMethodClientFocus(int uid, int pid) {
        WindowState imFocus = computeImeTarget(false);
        if (imFocus == null) {
            return false;
        }
        if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
            Slog.i("WindowManager", "Desired input method target: " + imFocus);
            Slog.i("WindowManager", "Current focus: " + this.mCurrentFocus + " displayId=" + this.mDisplayId);
            Slog.i("WindowManager", "Last focus: " + this.mLastFocus + " displayId=" + this.mDisplayId);
        }
        if (WindowManagerDebugConfig.DEBUG_INPUT_METHOD) {
            Slog.i("WindowManager", "IM target uid/pid: " + imFocus.mSession.mUid + SliceClientPermissions.SliceAuthority.DELIMITER + imFocus.mSession.mPid);
            Slog.i("WindowManager", "Requesting client uid/pid: " + uid + SliceClientPermissions.SliceAuthority.DELIMITER + pid);
        }
        if (imFocus.mSession.mUid == uid && imFocus.mSession.mPid == pid) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean hasSecureWindowOnScreen() {
        return getWindow($$Lambda$DisplayContent$BqPkkf0KHtTsCqMhLrfi8cPuEOM.INSTANCE) != null;
    }

    static /* synthetic */ boolean lambda$hasSecureWindowOnScreen$22(WindowState w) {
        return w.isOnScreen() && (w.mAttrs.flags & 8192) != 0;
    }

    /* access modifiers changed from: package-private */
    public void statusBarVisibilityChanged(int visibility) {
        this.mLastStatusBarVisibility = visibility;
        updateStatusBarVisibilityLocked(getDisplayPolicy().adjustSystemUiVisibilityLw(visibility));
    }

    private boolean updateStatusBarVisibilityLocked(int visibility) {
        int i = this.mLastDispatchedSystemUiVisibility;
        if (i == visibility) {
            return false;
        }
        int globalDiff = (i ^ visibility) & 7 & (~visibility);
        this.mLastDispatchedSystemUiVisibility = visibility;
        if (this.isDefaultDisplay) {
            this.mWmService.mInputManager.setSystemUiVisibility(visibility);
        }
        updateSystemUiVisibility(visibility, globalDiff);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void updateSystemUiVisibility(int visibility, int globalDiff) {
        forAllWindows((Consumer<WindowState>) new Consumer(visibility, globalDiff) {
            /* class com.android.server.wm.$$Lambda$DisplayContent$xFPkaoz4REtKz_m7C2pHvmlLa8 */
            private final /* synthetic */ int f$0;
            private final /* synthetic */ int f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                DisplayContent.lambda$updateSystemUiVisibility$23(this.f$0, this.f$1, (WindowState) obj);
            }
        }, true);
    }

    static /* synthetic */ void lambda$updateSystemUiVisibility$23(int visibility, int globalDiff, WindowState w) {
        try {
            int curValue = w.mSystemUiVisibility;
            int diff = (curValue ^ visibility) & globalDiff;
            int newValue = ((~diff) & curValue) | (visibility & diff);
            if (newValue != curValue) {
                w.mSeq++;
                w.mSystemUiVisibility = newValue;
            }
            if (newValue != curValue || w.mAttrs.hasSystemUiListeners) {
                w.mClient.dispatchSystemUiVisibilityChanged(w.mSeq, visibility, newValue, diff);
            }
        } catch (RemoteException e) {
        }
    }

    /* access modifiers changed from: package-private */
    public void reevaluateStatusBarVisibility() {
        if (updateStatusBarVisibilityLocked(getDisplayPolicy().adjustSystemUiVisibilityLw(this.mLastStatusBarVisibility))) {
            this.mWmService.mWindowPlacerLocked.requestTraversal();
        }
    }

    /* access modifiers changed from: package-private */
    public void onWindowFreezeTimeout() {
        Slog.w("WindowManager", "Window freeze timeout expired.");
        if (!this.mWmService.mSimulateWindowFreezing) {
            this.mWmService.mWindowsFreezingScreen = 2;
        }
        forAllWindows((Consumer<WindowState>) new Consumer() {
            /* class com.android.server.wm.$$Lambda$DisplayContent$10gFuNls5SEUWr3M4H0ZEUG7dnY */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                DisplayContent.this.lambda$onWindowFreezeTimeout$24$DisplayContent((WindowState) obj);
            }
        }, true);
        this.mWmService.mWindowPlacerLocked.performSurfacePlacement();
    }

    public /* synthetic */ void lambda$onWindowFreezeTimeout$24$DisplayContent(WindowState w) {
        if (w.getOrientationChanging()) {
            w.orientationChangeTimedOut();
            w.mLastFreezeDuration = (int) (SystemClock.elapsedRealtime() - this.mWmService.mDisplayFreezeTime);
            Slog.w("WindowManager", "Force clearing orientation change: " + w);
        }
    }

    /* access modifiers changed from: package-private */
    public void waitForAllWindowsDrawn() {
        forAllWindows((Consumer<WindowState>) new Consumer(this.mWmService.mPolicy) {
            /* class com.android.server.wm.$$Lambda$DisplayContent$_dZRryJQYdwokr9IRKPu2KCZZu4 */
            private final /* synthetic */ WindowManagerPolicy f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                DisplayContent.this.lambda$waitForAllWindowsDrawn$25$DisplayContent(this.f$1, (WindowState) obj);
            }
        }, true);
    }

    public /* synthetic */ void lambda$waitForAllWindowsDrawn$25$DisplayContent(WindowManagerPolicy policy, WindowState w) {
        boolean keyguard = policy.isKeyguardHostWindow(w.mAttrs);
        if (!w.isVisibleLw()) {
            return;
        }
        if (w.mAppToken != null || keyguard) {
            w.mWinAnimator.mDrawState = 1;
            w.resetLastContentInsets();
            this.mWmService.mWaitingForDrawn.add(w);
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public void applySurfaceChangesTransaction(boolean recoveringMemory) {
        WindowSurfacePlacer surfacePlacer = this.mWmService.mWindowPlacerLocked;
        this.mTmpUpdateAllDrawn.clear();
        int repeats = 0;
        while (true) {
            repeats++;
            if (repeats > 6) {
                Slog.w("WindowManager", "Animation repeat aborted after too many iterations");
                clearLayoutNeeded();
                break;
            }
            if (WindowManagerDebugConfig.DEBUG_LAYOUT_REPEATS) {
                surfacePlacer.debugLayoutRepeats("On entry to LockedInner", this.pendingLayoutChanges);
            }
            if ((this.pendingLayoutChanges & 4) != 0) {
                this.mWallpaperController.adjustWallpaperWindows();
            }
            if ((this.pendingLayoutChanges & 2) != 0) {
                if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                    Slog.v("WindowManager", "Computing new config from layout");
                }
                if (updateOrientationFromAppTokens()) {
                    setLayoutNeeded();
                    sendNewConfiguration();
                }
            }
            if ((this.pendingLayoutChanges & 1) != 0) {
                setLayoutNeeded();
            }
            if (repeats < 4) {
                performLayout(repeats == 1, false);
            } else {
                Slog.w("WindowManager", "Layout repeat skipped after too many iterations");
            }
            this.pendingLayoutChanges = 0;
            Trace.traceBegin(32, "applyPostLayoutPolicy");
            try {
                this.mDisplayPolicy.beginPostLayoutPolicyLw();
                forAllWindows(this.mApplyPostLayoutPolicy, true);
                this.pendingLayoutChanges |= this.mDisplayPolicy.finishPostLayoutPolicyLw();
                Trace.traceEnd(32);
                if (WindowManagerDebugConfig.DEBUG_LAYOUT_REPEATS) {
                    surfacePlacer.debugLayoutRepeats("after finishPostLayoutPolicyLw", this.pendingLayoutChanges);
                }
                this.mInsetsStateController.onPostLayout();
                if (this.pendingLayoutChanges == 0) {
                    break;
                }
            } catch (Throwable th) {
                Trace.traceEnd(32);
                throw th;
            }
        }
        this.mTmpApplySurfaceChangesTransactionState.reset();
        this.mOppoDisplayModeManager.resetModeId();
        this.mTmpRecoveringMemory = recoveringMemory;
        Trace.traceBegin(32, "applyWindowSurfaceChanges");
        try {
            forAllWindows(this.mApplySurfaceChangesTransaction, true);
            Trace.traceEnd(32);
            prepareSurfaces();
            if (this.mTmpApplySurfaceChangesTransactionState.preferredRefreshRate == OppoBrightUtils.MIN_LUX_LIMITI && this.mTmpApplySurfaceChangesTransactionState.preferredModeId == 0) {
                this.mTmpApplySurfaceChangesTransactionState.preferredModeId = this.mOppoDisplayModeManager.getPreferredModeId();
            } else if (WindowManagerDebugConfig.DEBUG_WMS) {
                Slog.d("WindowManager", "third part app setRefresh, preferredRefreshRate:" + this.mTmpApplySurfaceChangesTransactionState.preferredRefreshRate + ", preferredModeId:" + this.mTmpApplySurfaceChangesTransactionState.preferredModeId);
            }
            this.mLastHasContent = this.mTmpApplySurfaceChangesTransactionState.displayHasContent;
            this.mWmService.mDisplayManagerInternal.setDisplayProperties(this.mDisplayId, this.mLastHasContent, this.mTmpApplySurfaceChangesTransactionState.preferredRefreshRate, this.mTmpApplySurfaceChangesTransactionState.preferredModeId, true);
            boolean wallpaperVisible = this.mWallpaperController.isWallpaperVisible();
            if (wallpaperVisible != this.mLastWallpaperVisible) {
                this.mLastWallpaperVisible = wallpaperVisible;
                this.mWmService.mWallpaperVisibilityListeners.notifyWallpaperVisibilityChanged(this);
            }
            while (!this.mTmpUpdateAllDrawn.isEmpty()) {
                this.mTmpUpdateAllDrawn.removeLast().updateAllDrawn();
            }
        } catch (Throwable th2) {
            Trace.traceEnd(32);
            throw th2;
        }
    }

    private void updateBounds() {
        calculateBounds(this.mDisplayInfo, this.mTmpBounds);
        setBounds(this.mTmpBounds);
        InputWindowHandle inputWindowHandle = this.mPortalWindowHandle;
        if (inputWindowHandle != null && this.mParentSurfaceControl != null) {
            inputWindowHandle.touchableRegion.getBounds(this.mTmpRect);
            if (!this.mTmpBounds.equals(this.mTmpRect)) {
                this.mPortalWindowHandle.touchableRegion.set(this.mTmpBounds);
                getPendingTransaction().setInputWindowInfo(this.mParentSurfaceControl, this.mPortalWindowHandle);
            }
        }
    }

    private void calculateBounds(DisplayInfo displayInfo, Rect out) {
        int rotation = displayInfo.rotation;
        boolean rotated = true;
        if (!(rotation == 1 || rotation == 3)) {
            rotated = false;
        }
        int physWidth = rotated ? this.mBaseDisplayHeight : this.mBaseDisplayWidth;
        int physHeight = rotated ? this.mBaseDisplayWidth : this.mBaseDisplayHeight;
        int width = displayInfo.logicalWidth;
        int left = (physWidth - width) / 2;
        int height = displayInfo.logicalHeight;
        int top = (physHeight - height) / 2;
        out.set(left, top, left + width, top + height);
    }

    private void getBounds(Rect out, int orientation) {
        getBounds(out);
        int rotationDelta = deltaRotation(this.mDisplayInfo.rotation, orientation);
        if (rotationDelta == 1 || rotationDelta == 3) {
            createRotationMatrix(rotationDelta, (float) this.mBaseDisplayWidth, (float) this.mBaseDisplayHeight, this.mTmpMatrix);
            this.mTmpRectF.set(out);
            this.mTmpMatrix.mapRect(this.mTmpRectF);
            this.mTmpRectF.round(out);
        }
    }

    /* access modifiers changed from: package-private */
    public int getNaturalOrientation() {
        return this.mBaseDisplayWidth < this.mBaseDisplayHeight ? 1 : 2;
    }

    /* access modifiers changed from: package-private */
    public void performLayout(boolean initial, boolean updateInputWindows) {
        Trace.traceBegin(32, "performLayout");
        try {
            performLayoutNoTrace(initial, updateInputWindows);
        } finally {
            Trace.traceEnd(32);
        }
    }

    private void performLayoutNoTrace(boolean initial, boolean updateInputWindows) {
        if (isLayoutNeeded()) {
            clearLayoutNeeded();
            int dw = this.mDisplayInfo.logicalWidth;
            int dh = this.mDisplayInfo.logicalHeight;
            if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                Slog.v("WindowManager", "-------------------------------------");
                Slog.v("WindowManager", "performLayout: needed=" + isLayoutNeeded() + " dw=" + dw + " dh=" + dh);
            }
            DisplayFrames displayFrames = this.mDisplayFrames;
            DisplayInfo displayInfo = this.mDisplayInfo;
            displayFrames.onDisplayInfoUpdated(displayInfo, calculateDisplayCutoutForRotation(displayInfo.rotation));
            this.mDisplayFrames.mRotation = this.mRotation;
            if (!OppoFeatureCache.get(IColorFullScreenDisplayManager.DEFAULT).performLayoutNoTrace(this.mDisplayPolicy, this.mDisplayFrames, getConfiguration().uiMode)) {
                this.mDisplayPolicy.beginLayoutLw(this.mDisplayFrames, getConfiguration().uiMode);
            }
            int seq = this.mLayoutSeq + 1;
            if (seq < 0) {
                seq = 0;
            }
            this.mLayoutSeq = seq;
            this.mTmpWindow = null;
            this.mTmpInitial = initial;
            forAllWindows(this.mPerformLayout, true);
            this.mTmpWindow2 = this.mTmpWindow;
            this.mTmpWindow = null;
            forAllWindows(this.mPerformLayoutAttached, true);
            this.mInputMonitor.layoutInputConsumers(dw, dh);
            this.mInputMonitor.setUpdateInputWindowsNeededLw();
            if (updateInputWindows) {
                this.mInputMonitor.updateInputWindowsLw(false);
            }
            this.mWmService.mH.sendEmptyMessage(41);
        }
    }

    /* access modifiers changed from: package-private */
    public Bitmap screenshotDisplayLocked(Bitmap.Config config) {
        if (!this.mWmService.mPolicy.isScreenOn()) {
            if (WindowManagerDebugConfig.DEBUG_SCREENSHOT) {
                Slog.i("WindowManager", "Attempted to take screenshot while display was off.");
            }
            return null;
        }
        int dw = this.mDisplayInfo.logicalWidth;
        int dh = this.mDisplayInfo.logicalHeight;
        if (dw <= 0 || dh <= 0) {
            return null;
        }
        boolean inRotation = false;
        Rect frame = new Rect(0, 0, dw, dh);
        int rot = this.mDisplay.getRotation();
        int i = 3;
        if (rot == 1 || rot == 3) {
            if (rot != 1) {
                i = 1;
            }
            rot = i;
        }
        convertCropForSurfaceFlinger(frame, rot, dw, dh);
        ScreenRotationAnimation screenRotationAnimation = this.mWmService.mAnimator.getScreenRotationAnimationLocked(0);
        if (screenRotationAnimation != null && screenRotationAnimation.isAnimating()) {
            inRotation = true;
        }
        if (WindowManagerDebugConfig.DEBUG_SCREENSHOT && inRotation) {
            Slog.v("WindowManager", "Taking screenshot while rotating");
        }
        Bitmap bitmap = SurfaceControl.screenshot(frame, dw, dh, inRotation, rot);
        if (bitmap == null) {
            Slog.w("WindowManager", "Failed to take screenshot");
            return null;
        }
        Bitmap ret = bitmap.createAshmemBitmap(config);
        bitmap.recycle();
        return ret;
    }

    private static void convertCropForSurfaceFlinger(Rect crop, int rot, int dw, int dh) {
        if (rot == 1) {
            int tmp = crop.top;
            crop.top = dw - crop.right;
            crop.right = crop.bottom;
            crop.bottom = dw - crop.left;
            crop.left = tmp;
        } else if (rot == 2) {
            int tmp2 = crop.top;
            crop.top = dh - crop.bottom;
            crop.bottom = dh - tmp2;
            int tmp3 = crop.right;
            crop.right = dw - crop.left;
            crop.left = dw - tmp3;
        } else if (rot == 3) {
            int tmp4 = crop.top;
            crop.top = crop.left;
            crop.left = dh - crop.bottom;
            crop.bottom = crop.right;
            crop.right = dh - tmp4;
        }
    }

    /* access modifiers changed from: package-private */
    public void onSeamlessRotationTimeout() {
        this.mTmpWindow = null;
        forAllWindows((Consumer<WindowState>) new Consumer() {
            /* class com.android.server.wm.$$Lambda$DisplayContent$fJACJZmXtOEEtwlcx1f9zVkhr30 */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                DisplayContent.this.lambda$onSeamlessRotationTimeout$26$DisplayContent((WindowState) obj);
            }
        }, true);
        if (this.mTmpWindow != null) {
            this.mWmService.mWindowPlacerLocked.performSurfacePlacement();
        }
    }

    public /* synthetic */ void lambda$onSeamlessRotationTimeout$26$DisplayContent(WindowState w) {
        if (w.mSeamlesslyRotated) {
            this.mTmpWindow = w;
            w.setDisplayLayoutNeeded();
            w.finishSeamlessRotation(true);
            this.mWmService.markForSeamlessRotation(w, false);
        }
    }

    /* access modifiers changed from: package-private */
    public void setExitingTokensHasVisible(boolean hasVisible) {
        for (int i = this.mExitingTokens.size() - 1; i >= 0; i--) {
            this.mExitingTokens.get(i).hasVisible = hasVisible;
        }
        this.mTaskStackContainers.setExitingTokensHasVisible(hasVisible);
    }

    /* access modifiers changed from: package-private */
    public void removeExistingTokensIfPossible() {
        for (int i = this.mExitingTokens.size() - 1; i >= 0; i--) {
            if (!this.mExitingTokens.get(i).hasVisible) {
                this.mExitingTokens.remove(i);
            }
        }
        this.mTaskStackContainers.removeExistingAppTokensIfPossible();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void onDescendantOverrideConfigurationChanged() {
        setLayoutNeeded();
        this.mWmService.requestTraversal();
    }

    /* access modifiers changed from: package-private */
    public boolean okToDisplay() {
        return this.mDisplayId == 0 ? !this.mWmService.mDisplayFrozen && this.mWmService.mDisplayEnabled && this.mWmService.mPolicy.isScreenOn() : this.mDisplayInfo.state == 2;
    }

    /* access modifiers changed from: package-private */
    public boolean okToAnimate() {
        return okToDisplay() && (this.mDisplayId != 0 || this.mWmService.mPolicy.okToAnimate());
    }

    /* access modifiers changed from: package-private */
    public static final class TaskForResizePointSearchResult {
        boolean searchDone;
        Task taskForResize;

        TaskForResizePointSearchResult() {
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            this.searchDone = false;
            this.taskForResize = null;
        }
    }

    private static final class ApplySurfaceChangesTransactionState {
        boolean displayHasContent;
        boolean obscured;
        int preferredModeId;
        float preferredRefreshRate;
        boolean syswin;

        private ApplySurfaceChangesTransactionState() {
        }

        /* access modifiers changed from: package-private */
        public void reset() {
            this.displayHasContent = false;
            this.obscured = false;
            this.syswin = false;
            this.preferredRefreshRate = OppoBrightUtils.MIN_LUX_LIMITI;
            this.preferredModeId = 0;
        }
    }

    private static final class ScreenshotApplicationState {
        WindowState appWin;
        int maxLayer;
        int minLayer;
        boolean screenshotReady;

        private ScreenshotApplicationState() {
        }

        /* access modifiers changed from: package-private */
        public void reset(boolean screenshotReady2) {
            this.appWin = null;
            int i = 0;
            this.maxLayer = 0;
            this.minLayer = 0;
            this.screenshotReady = screenshotReady2;
            if (!screenshotReady2) {
                i = Integer.MAX_VALUE;
            }
            this.minLayer = i;
        }
    }

    /* access modifiers changed from: package-private */
    public static class DisplayChildWindowContainer<E extends WindowContainer> extends WindowContainer<E> {
        DisplayChildWindowContainer(WindowManagerService service) {
            super(service);
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.WindowContainer
        public boolean fillsParent() {
            return true;
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.WindowContainer
        public boolean isVisible() {
            return true;
        }
    }

    /* access modifiers changed from: private */
    public final class TaskStackContainers extends DisplayChildWindowContainer<TaskStack> {
        SurfaceControl mAppAnimationLayer = null;
        SurfaceControl mBoostedAppAnimationLayer = null;
        SurfaceControl mHomeAppAnimationLayer = null;
        private TaskStack mHomeStack = null;
        private TaskStack mPinnedStack = null;
        SurfaceControl mSplitScreenDividerAnchor = null;
        private TaskStack mSplitScreenPrimaryStack = null;
        SurfaceControl mZoomControllerViewAnchor = null;

        TaskStackContainers(WindowManagerService service) {
            super(service);
            setOrientation(-2);
        }

        /* access modifiers changed from: package-private */
        public TaskStack getStack(int windowingMode, int activityType) {
            if (activityType == 2) {
                return this.mHomeStack;
            }
            if (windowingMode == 2) {
                return this.mPinnedStack;
            }
            if (windowingMode == 3) {
                return this.mSplitScreenPrimaryStack;
            }
            for (int i = DisplayContent.this.mTaskStackContainers.getChildCount() - 1; i >= 0; i--) {
                TaskStack stack = (TaskStack) DisplayContent.this.mTaskStackContainers.getChildAt(i);
                if ((activityType == 0 && windowingMode == stack.getWindowingMode()) || stack.isCompatible(windowingMode, activityType)) {
                    return stack;
                }
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        @VisibleForTesting
        public TaskStack getTopStack() {
            if (DisplayContent.this.mTaskStackContainers.getChildCount() > 0) {
                return (TaskStack) DisplayContent.this.mTaskStackContainers.getChildAt(DisplayContent.this.mTaskStackContainers.getChildCount() - 1);
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        public TaskStack getHomeStack() {
            if (this.mHomeStack == null && DisplayContent.this.mDisplayId == 0) {
                Slog.e("WindowManager", "getHomeStack: Returning null from this=" + this);
            }
            return this.mHomeStack;
        }

        /* access modifiers changed from: package-private */
        public TaskStack getPinnedStack() {
            return this.mPinnedStack;
        }

        /* access modifiers changed from: package-private */
        public TaskStack getSplitScreenPrimaryStack() {
            return this.mSplitScreenPrimaryStack;
        }

        /* access modifiers changed from: package-private */
        public ArrayList<Task> getVisibleTasks() {
            ArrayList<Task> visibleTasks = new ArrayList<>();
            forAllTasks(new Consumer(visibleTasks) {
                /* class com.android.server.wm.$$Lambda$DisplayContent$TaskStackContainers$rQnI0Y8R9ptQ09cGHwbCHDiG2FY */
                private final /* synthetic */ ArrayList f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    DisplayContent.TaskStackContainers.lambda$getVisibleTasks$0(this.f$0, (Task) obj);
                }
            });
            return visibleTasks;
        }

        static /* synthetic */ void lambda$getVisibleTasks$0(ArrayList visibleTasks, Task task) {
            if (task.isVisible()) {
                visibleTasks.add(task);
            }
        }

        /* access modifiers changed from: package-private */
        public void addStackToDisplay(TaskStack stack, boolean onTop) {
            addStackReferenceIfNeeded(stack);
            addChild(stack, onTop);
            stack.onDisplayChanged(DisplayContent.this);
        }

        /* access modifiers changed from: package-private */
        public void onStackWindowingModeChanged(TaskStack stack) {
            removeStackReferenceIfNeeded(stack);
            addStackReferenceIfNeeded(stack);
            if (stack == this.mPinnedStack && getTopStack() != stack) {
                positionChildAt(Integer.MAX_VALUE, stack, false);
            }
        }

        private void addStackReferenceIfNeeded(TaskStack stack) {
            if (stack.isActivityTypeHome()) {
                if (this.mHomeStack == null) {
                    this.mHomeStack = stack;
                } else {
                    throw new IllegalArgumentException("addStackReferenceIfNeeded: home stack=" + this.mHomeStack + " already exist on display=" + this + " stack=" + stack);
                }
            }
            int windowingMode = stack.getWindowingMode();
            if (windowingMode == 2) {
                if (this.mPinnedStack == null) {
                    this.mPinnedStack = stack;
                    return;
                }
                throw new IllegalArgumentException("addStackReferenceIfNeeded: pinned stack=" + this.mPinnedStack + " already exist on display=" + this + " stack=" + stack);
            } else if (windowingMode != 3) {
            } else {
                if (this.mSplitScreenPrimaryStack == null) {
                    this.mSplitScreenPrimaryStack = stack;
                    DisplayContent.this.mDividerControllerLocked.notifyDockedStackExistsChanged(true);
                    return;
                }
                throw new IllegalArgumentException("addStackReferenceIfNeeded: split-screen-primary stack=" + this.mSplitScreenPrimaryStack + " already exist on display=" + this + " stack=" + stack);
            }
        }

        private void removeStackReferenceIfNeeded(TaskStack stack) {
            if (stack == this.mHomeStack) {
                this.mHomeStack = null;
            } else if (stack == this.mPinnedStack) {
                this.mPinnedStack = null;
            } else if (stack == this.mSplitScreenPrimaryStack) {
                this.mSplitScreenPrimaryStack = null;
                this.mWmService.setDockedStackCreateStateLocked(0, null);
                DisplayContent.this.mDividerControllerLocked.notifyDockedStackExistsChanged(false);
            }
        }

        private void addChild(TaskStack stack, boolean toTop) {
            addChild((TaskStackContainers) stack, findPositionForStack(toTop ? this.mChildren.size() : 0, stack, true));
            DisplayContent.this.setLayoutNeeded();
        }

        /* access modifiers changed from: protected */
        public void removeChild(TaskStack stack) {
            super.removeChild((TaskStackContainers) stack);
            removeStackReferenceIfNeeded(stack);
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.WindowContainer
        public boolean isOnTop() {
            return true;
        }

        /* access modifiers changed from: package-private */
        public void positionChildAt(int position, TaskStack child, boolean includingParents) {
            int topChildPosition;
            if (!child.getWindowConfiguration().isAlwaysOnTop() || position == Integer.MAX_VALUE) {
                int targetPosition = findPositionForStack(position, child, false);
                super.positionChildAt(targetPosition, (int) child, includingParents);
                if (includingParents && targetPosition < (topChildPosition = getChildCount() - 1) && position >= topChildPosition) {
                    getParent().positionChildAt(Integer.MAX_VALUE, this, true);
                }
                DisplayContent.this.setLayoutNeeded();
                return;
            }
            Slog.w("WindowManager", "Ignoring move of always-on-top stack=" + this + " to bottom");
            super.positionChildAt(this.mChildren.indexOf(child), (int) child, false);
        }

        private int findPositionForStack(int requestedPosition, TaskStack stack, boolean adding) {
            if (stack.inPinnedWindowingMode()) {
                return Integer.MAX_VALUE;
            }
            int topChildPosition = this.mChildren.size() - 1;
            int belowAlwaysOnTopPosition = Integer.MIN_VALUE;
            int i = topChildPosition;
            while (true) {
                if (i >= 0) {
                    if (DisplayContent.this.getStacks().get(i) != stack && !DisplayContent.this.getStacks().get(i).isAlwaysOnTop()) {
                        belowAlwaysOnTopPosition = i;
                        break;
                    }
                    i--;
                } else {
                    break;
                }
            }
            int maxPosition = Integer.MAX_VALUE;
            int minPosition = Integer.MIN_VALUE;
            if (stack.isAlwaysOnTop()) {
                if (DisplayContent.this.hasPinnedStack()) {
                    maxPosition = DisplayContent.this.getStacks().indexOf(this.mPinnedStack) - 1;
                }
                minPosition = belowAlwaysOnTopPosition != Integer.MIN_VALUE ? belowAlwaysOnTopPosition : topChildPosition;
            } else {
                maxPosition = belowAlwaysOnTopPosition != Integer.MIN_VALUE ? belowAlwaysOnTopPosition : 0;
            }
            int targetPosition = Math.max(Math.min(requestedPosition, maxPosition), minPosition);
            int prevPosition = DisplayContent.this.getStacks().indexOf(stack);
            if (targetPosition == requestedPosition) {
                return targetPosition;
            }
            if (adding || targetPosition < prevPosition) {
                return targetPosition + 1;
            }
            return targetPosition;
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.WindowContainer
        public boolean forAllWindows(ToBooleanFunction<WindowState> callback, boolean traverseTopToBottom) {
            if (traverseTopToBottom) {
                if (!super.forAllWindows(callback, traverseTopToBottom) && !forAllExitingAppTokenWindows(callback, traverseTopToBottom)) {
                    return false;
                }
                return true;
            } else if (!forAllExitingAppTokenWindows(callback, traverseTopToBottom) && !super.forAllWindows(callback, traverseTopToBottom)) {
                return false;
            } else {
                return true;
            }
        }

        private boolean forAllExitingAppTokenWindows(ToBooleanFunction<WindowState> callback, boolean traverseTopToBottom) {
            if (traverseTopToBottom) {
                for (int i = this.mChildren.size() - 1; i >= 0; i--) {
                    AppTokenList appTokens = ((TaskStack) this.mChildren.get(i)).mExitingAppTokens;
                    for (int j = appTokens.size() - 1; j >= 0; j--) {
                        if (((AppWindowToken) appTokens.get(j)).forAllWindowsUnchecked(callback, traverseTopToBottom)) {
                            return true;
                        }
                    }
                }
                return false;
            }
            int count = this.mChildren.size();
            for (int i2 = 0; i2 < count; i2++) {
                AppTokenList appTokens2 = ((TaskStack) this.mChildren.get(i2)).mExitingAppTokens;
                int appTokensCount = appTokens2.size();
                for (int j2 = 0; j2 < appTokensCount; j2++) {
                    if (((AppWindowToken) appTokens2.get(j2)).forAllWindowsUnchecked(callback, traverseTopToBottom)) {
                        return true;
                    }
                }
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public void setExitingTokensHasVisible(boolean hasVisible) {
            for (int i = this.mChildren.size() - 1; i >= 0; i--) {
                AppTokenList appTokens = ((TaskStack) this.mChildren.get(i)).mExitingAppTokens;
                for (int j = appTokens.size() - 1; j >= 0; j--) {
                    ((AppWindowToken) appTokens.get(j)).hasVisible = hasVisible;
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void removeExistingAppTokensIfPossible() {
            for (int i = this.mChildren.size() - 1; i >= 0; i--) {
                AppTokenList appTokens = ((TaskStack) this.mChildren.get(i)).mExitingAppTokens;
                for (int j = appTokens.size() - 1; j >= 0; j--) {
                    AppWindowToken token = (AppWindowToken) appTokens.get(j);
                    if (!token.hasVisible && !DisplayContent.this.mClosingApps.contains(token) && (!token.mIsExiting || token.isEmpty())) {
                        cancelAnimation();
                        if (WindowManagerDebugConfig.DEBUG_ADD_REMOVE || WindowManagerDebugConfig.DEBUG_TOKEN_MOVEMENT) {
                            Slog.v("WindowManager", "performLayout: App token exiting now removed" + token);
                        }
                        token.removeIfPossible();
                    }
                }
            }
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.WindowContainer
        public int getOrientation() {
            int orientation;
            if (DisplayContent.this.isStackVisible(3) || DisplayContent.this.isStackVisible(5)) {
                TaskStack taskStack = this.mHomeStack;
                if (taskStack == null || !taskStack.isVisible() || !DisplayContent.this.mDividerControllerLocked.isMinimizedDock() || ((DisplayContent.this.mDividerControllerLocked.isHomeStackResizable() && this.mHomeStack.matchParentBounds()) || (orientation = this.mHomeStack.getOrientation()) == -2)) {
                    return -1;
                }
                return orientation;
            }
            int orientation2 = super.getOrientation();
            if (this.mWmService.mContext.getPackageManager().hasSystemFeature("android.hardware.type.automotive")) {
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.v("WindowManager", "Forcing UNSPECIFIED orientation in car for display id=" + DisplayContent.this.mDisplayId + ". Ignoring " + orientation2);
                }
                return -1;
            } else if (orientation2 == -2 || orientation2 == 3) {
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.v("WindowManager", "No app is requesting an orientation, return " + DisplayContent.this.mLastOrientation + " for display id=" + DisplayContent.this.mDisplayId);
                }
                return DisplayContent.this.mLastOrientation;
            } else {
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.v("WindowManager", "App is requesting an orientation, return " + orientation2 + " for display id=" + DisplayContent.this.mDisplayId);
                }
                return orientation2;
            }
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.WindowContainer
        public void assignChildLayers(SurfaceControl.Transaction t) {
            assignStackOrdering(t);
            for (int i = 0; i < this.mChildren.size(); i++) {
                ((TaskStack) this.mChildren.get(i)).assignChildLayers(t);
            }
        }

        /* access modifiers changed from: package-private */
        public void assignStackOrdering(SurfaceControl.Transaction t) {
            int layer;
            SurfaceControl surfaceControl;
            SurfaceControl surfaceControl2;
            int layer2 = 0;
            int layerForAnimationLayer = 0;
            int layerForBoostedAnimationLayer = 0;
            int layerForHomeAnimationLayer = 0;
            boolean adjustDividerLayer = false;
            for (int state = 0; state <= 2; state++) {
                for (int i = 0; i < this.mChildren.size(); i++) {
                    TaskStack s = (TaskStack) this.mChildren.get(i);
                    if ((state != 0 || s.isActivityTypeHome()) && ((state != 1 || (!s.isActivityTypeHome() && !s.isAlwaysOnTop())) && (state != 2 || s.isAlwaysOnTop()))) {
                        int layer3 = layer2 + 1;
                        s.assignLayer(t, layer2);
                        if (s.getWindowingMode() == WindowConfiguration.WINDOWING_MODE_ZOOM && (surfaceControl2 = this.mZoomControllerViewAnchor) != null) {
                            t.setLayer(surfaceControl2, layer3);
                            layer3++;
                        }
                        if (!s.inSplitScreenWindowingMode() || (surfaceControl = this.mSplitScreenDividerAnchor) == null) {
                            layer = layer3;
                        } else {
                            layer = layer3 + 1;
                            t.setLayer(surfaceControl, layer3);
                            if (s.isAppAnimating()) {
                                adjustDividerLayer = true;
                            }
                        }
                        if ((s.isTaskAnimating() || s.isAppAnimating()) && state != 2) {
                            layer2 = layer + 1;
                            layerForAnimationLayer = layer;
                        } else {
                            layer2 = layer;
                        }
                        if (state != 2) {
                            layerForBoostedAnimationLayer = layer2;
                            layer2++;
                        }
                    }
                }
                if (state == 0) {
                    layerForHomeAnimationLayer = layer2;
                    layer2++;
                }
            }
            SurfaceControl surfaceControl3 = this.mAppAnimationLayer;
            if (surfaceControl3 != null) {
                t.setLayer(surfaceControl3, layerForAnimationLayer);
            }
            SurfaceControl surfaceControl4 = this.mBoostedAppAnimationLayer;
            if (surfaceControl4 != null) {
                t.setLayer(surfaceControl4, layerForBoostedAnimationLayer);
            }
            SurfaceControl surfaceControl5 = this.mHomeAppAnimationLayer;
            if (surfaceControl5 != null) {
                t.setLayer(surfaceControl5, layerForHomeAnimationLayer);
            }
            SurfaceControl surfaceControl6 = this.mSplitScreenDividerAnchor;
            if (surfaceControl6 != null && adjustDividerLayer) {
                t.setLayer(surfaceControl6, layerForBoostedAnimationLayer + 1);
            }
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.WindowContainer
        public SurfaceControl getAppAnimationLayer(@WindowContainer.AnimationLayer int animationLayer) {
            if (animationLayer == 1) {
                return this.mBoostedAppAnimationLayer;
            }
            if (animationLayer != 2) {
                return this.mAppAnimationLayer;
            }
            return this.mHomeAppAnimationLayer;
        }

        /* access modifiers changed from: package-private */
        public SurfaceControl getSplitScreenDividerAnchor() {
            return this.mSplitScreenDividerAnchor;
        }

        /* access modifiers changed from: package-private */
        public SurfaceControl getZoomControllerViewAnchor() {
            return this.mZoomControllerViewAnchor;
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.WindowContainer, com.android.server.wm.ConfigurationContainer
        public void onParentChanged() {
            super.onParentChanged();
            if (getParent() != null) {
                this.mAppAnimationLayer = makeChildSurface(null).setName("animationLayer").build();
                this.mBoostedAppAnimationLayer = makeChildSurface(null).setName("boostedAnimationLayer").build();
                this.mHomeAppAnimationLayer = makeChildSurface(null).setName("homeAnimationLayer").build();
                this.mSplitScreenDividerAnchor = makeChildSurface(null).setName("splitScreenDividerAnchor").build();
                this.mZoomControllerViewAnchor = makeChildSurface(null).setName("zoomControllerViewAnchor").build();
                getPendingTransaction().show(this.mAppAnimationLayer).show(this.mBoostedAppAnimationLayer).show(this.mHomeAppAnimationLayer).show(this.mSplitScreenDividerAnchor);
                getPendingTransaction().show(this.mZoomControllerViewAnchor);
                scheduleAnimation();
                return;
            }
            this.mAppAnimationLayer.remove();
            this.mAppAnimationLayer = null;
            this.mBoostedAppAnimationLayer.remove();
            this.mBoostedAppAnimationLayer = null;
            this.mHomeAppAnimationLayer.remove();
            this.mHomeAppAnimationLayer = null;
            this.mSplitScreenDividerAnchor.remove();
            this.mSplitScreenDividerAnchor = null;
            this.mZoomControllerViewAnchor.remove();
            this.mZoomControllerViewAnchor = null;
        }
    }

    /* access modifiers changed from: private */
    public final class AboveAppWindowContainers extends NonAppWindowContainers {
        AboveAppWindowContainers(String name, WindowManagerService service) {
            super(name, service);
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.WindowContainer
        public SurfaceControl.Builder makeChildSurface(WindowContainer child) {
            SurfaceControl.Builder builder = super.makeChildSurface(child);
            if ((child instanceof WindowToken) && ((WindowToken) child).mRoundedCornerOverlay) {
                builder.setParent(null);
            }
            return builder;
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.WindowContainer
        public void assignChildLayers(SurfaceControl.Transaction t) {
            assignChildLayers(t, null);
        }

        /* access modifiers changed from: package-private */
        public void assignChildLayers(SurfaceControl.Transaction t, WindowContainer imeContainer) {
            boolean needAssignIme = (imeContainer == null || imeContainer.getSurfaceControl() == null) ? false : true;
            for (int j = 0; j < this.mChildren.size(); j++) {
                WindowToken wt = (WindowToken) this.mChildren.get(j);
                if (wt.windowType == 2318) {
                    wt.assignRelativeLayer(t, DisplayContent.this.mTaskStackContainers.getZoomControllerViewAnchor(), 1);
                } else if (wt.windowType == 2034) {
                    wt.assignRelativeLayer(t, DisplayContent.this.mTaskStackContainers.getSplitScreenDividerAnchor(), 1);
                } else if (wt.mRoundedCornerOverlay) {
                    wt.assignLayer(t, 1073741826);
                } else {
                    wt.assignLayer(t, j);
                    wt.assignChildLayers(t);
                    int layer = this.mWmService.mPolicy.getWindowLayerFromTypeLw(wt.windowType, wt.mOwnerCanManageAppTokens);
                    if (needAssignIme && layer >= this.mWmService.mPolicy.getWindowLayerFromTypeLw(2012, true)) {
                        imeContainer.assignRelativeLayer(t, wt.getSurfaceControl(), -1);
                        needAssignIme = false;
                    }
                }
            }
            if (needAssignIme) {
                imeContainer.assignRelativeLayer(t, getSurfaceControl(), Integer.MAX_VALUE);
            }
        }
    }

    /* access modifiers changed from: private */
    public class NonAppWindowContainers extends DisplayChildWindowContainer<WindowToken> {
        private final Dimmer mDimmer = new Dimmer(this);
        private final Predicate<WindowState> mGetOrientingWindow = new Predicate() {
            /* class com.android.server.wm.$$Lambda$DisplayContent$NonAppWindowContainers$m2B7QqNQSZc7N5DejF0qGwn6Pck */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return DisplayContent.NonAppWindowContainers.this.lambda$new$1$DisplayContent$NonAppWindowContainers((WindowState) obj);
            }
        };
        private final String mName;
        private final Rect mTmpDimBoundsRect = new Rect();
        private final Comparator<WindowToken> mWindowComparator = new Comparator() {
            /* class com.android.server.wm.$$Lambda$DisplayContent$NonAppWindowContainers$nqCymC3xR9b3qaeohnnJJpSiajc */

            @Override // java.util.Comparator
            public final int compare(Object obj, Object obj2) {
                return DisplayContent.NonAppWindowContainers.this.lambda$new$0$DisplayContent$NonAppWindowContainers((WindowToken) obj, (WindowToken) obj2);
            }
        };

        public /* synthetic */ int lambda$new$0$DisplayContent$NonAppWindowContainers(WindowToken token1, WindowToken token2) {
            return this.mWmService.mPolicy.getWindowLayerFromTypeLw(token1.windowType, token1.mOwnerCanManageAppTokens) < this.mWmService.mPolicy.getWindowLayerFromTypeLw(token2.windowType, token2.mOwnerCanManageAppTokens) ? -1 : 1;
        }

        public /* synthetic */ boolean lambda$new$1$DisplayContent$NonAppWindowContainers(WindowState w) {
            int req;
            if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                this.mWmService.mWindowManagerDebugger.debugGetOrientingWindow("WindowManager", w, w.mAttrs, w.isVisibleLw(), w.mLegacyPolicyVisibilityAfterAnim, w.mPolicyVisibility, w.mDestroying);
            }
            if (!w.isVisibleLw() || !w.mLegacyPolicyVisibilityAfterAnim || (req = w.mAttrs.screenOrientation) == -1 || req == 3 || req == -2) {
                return false;
            }
            AppWindowToken focusedApp = DisplayContent.this.mFocusedApp;
            if (focusedApp == null || !focusedApp.toString().contains("com.ea.game.pvz2_row")) {
                return true;
            }
            WindowManagerPolicy policy = this.mWmService.mPolicy;
            if (req != 5 || policy == null || !w.toString().contains("StatusBar") || policy.isKeyguardShowingAndNotOccluded()) {
                return true;
            }
            Slog.v("WindowManager", "StatusBar is SCREEN_ORIENTATION_NOSENSOR, force return false to act like SCREEN_ORIENTATION_UNSPECIFIED ");
            return false;
        }

        NonAppWindowContainers(String name, WindowManagerService service) {
            super(service);
            this.mName = name;
        }

        /* access modifiers changed from: package-private */
        public void addChild(WindowToken token) {
            addChild((NonAppWindowContainers) token, (Comparator<NonAppWindowContainers>) this.mWindowComparator);
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.WindowContainer
        public int getOrientation() {
            WindowManagerPolicy policy = this.mWmService.mPolicy;
            WindowState win = getWindow(this.mGetOrientingWindow);
            if (win != null) {
                int req = win.mAttrs.screenOrientation;
                if (policy.isKeyguardHostWindow(win.mAttrs)) {
                    DisplayContent.this.mLastKeyguardForcedOrientation = req;
                    if (this.mWmService.mKeyguardGoingAway) {
                        DisplayContent.this.mLastWindowForcedOrientation = -1;
                        return -2;
                    }
                }
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.v("WindowManager", win + " forcing orientation to " + req + " for display id=" + DisplayContent.this.mDisplayId);
                }
                return DisplayContent.this.mLastWindowForcedOrientation = req;
            }
            DisplayContent.this.mLastWindowForcedOrientation = -1;
            boolean isUnoccluding = DisplayContent.this.mAppTransition.getAppTransition() == 23 && DisplayContent.this.mUnknownAppVisibilityController.allResolved();
            if (policy.isKeyguardShowingAndNotOccluded() || isUnoccluding) {
                return DisplayContent.this.mLastKeyguardForcedOrientation;
            }
            return -2;
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.ConfigurationContainer
        public String getName() {
            return this.mName;
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.WindowContainer
        public Dimmer getDimmer() {
            return this.mDimmer;
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.WindowContainer
        public void prepareSurfaces() {
            this.mDimmer.resetDimStates();
            super.prepareSurfaces();
            getBounds(this.mTmpDimBoundsRect);
            if (this.mDimmer.updateDims(getPendingTransaction(), this.mTmpDimBoundsRect)) {
                scheduleAnimation();
            }
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.wm.WindowContainer
        public void applyMagnificationSpec(SurfaceControl.Transaction t, MagnificationSpec spec) {
            String str = this.mName;
            if (str != null && str.equals("mBelowAppWindowsContainers") && this.mSurfaceControl != null && OppoScreenDragUtil.isDragState()) {
                if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                    Slog.v("WindowManager", "setWindowCrop for Wallpaper width:" + DisplayContent.this.mDisplayInfo.logicalWidth + " height:" + DisplayContent.this.mDisplayInfo.logicalHeight);
                }
                t.setWindowCrop(this.mSurfaceControl, DisplayContent.this.mDisplayInfo.logicalWidth, DisplayContent.this.mDisplayInfo.logicalHeight);
            }
            super.applyMagnificationSpec(t, spec);
        }

        public void adjustForDisplayChanged() {
            if (getSurfaceControl() != null) {
                if (WindowManagerDebugConfig.DEBUG_LAYOUT) {
                    Slog.v("WindowManager", "DisplayChanged setWindowCrop for Wallpaper width:" + DisplayContent.this.mDisplayInfo.logicalWidth + " height:" + DisplayContent.this.mDisplayInfo.logicalHeight);
                }
                getPendingTransaction().setWindowCrop(getSurfaceControl(), DisplayContent.this.mDisplayInfo.logicalWidth, DisplayContent.this.mDisplayInfo.logicalHeight);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public SurfaceControl.Builder makeSurface(SurfaceSession s) {
        return this.mWmService.makeSurfaceBuilder(s).setParent(this.mWindowingLayer);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public SurfaceSession getSession() {
        return this.mSession;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public SurfaceControl.Builder makeChildSurface(WindowContainer child) {
        SurfaceControl.Builder b = this.mWmService.makeSurfaceBuilder(child != null ? child.getSession() : getSession()).setContainerLayer();
        if (child == null) {
            return b;
        }
        return b.setName(child.getName()).setParent(this.mWindowingLayer);
    }

    /* access modifiers changed from: package-private */
    public SurfaceControl.Builder makeOverlay() {
        return this.mWmService.makeSurfaceBuilder(this.mSession).setParent(this.mOverlayLayer);
    }

    /* access modifiers changed from: package-private */
    public void reparentToOverlay(SurfaceControl.Transaction transaction, SurfaceControl surface) {
        transaction.reparent(surface, this.mOverlayLayer);
    }

    /* access modifiers changed from: package-private */
    public void applyMagnificationSpec(MagnificationSpec spec) {
        if (((double) spec.scale) != 1.0d || OppoFeatureCache.get(IColorBreenoManager.DEFAULT).inDragWindowing()) {
            this.mMagnificationSpec = MagnificationSpec.obtain(spec);
        } else {
            this.mMagnificationSpec = null;
        }
        updateImeParent();
        applyMagnificationSpec(getPendingTransaction(), spec);
        getPendingTransaction().apply();
    }

    /* access modifiers changed from: package-private */
    public void reapplyMagnificationSpec() {
        if (this.mMagnificationSpec != null) {
            applyMagnificationSpec(getPendingTransaction(), this.mMagnificationSpec);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer, com.android.server.wm.ConfigurationContainer
    public void onParentChanged() {
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void assignChildLayers(SurfaceControl.Transaction t) {
        this.mBelowAppWindowsContainers.assignLayer(t, 0);
        this.mTaskStackContainers.assignLayer(t, 1);
        this.mAboveAppWindowsContainers.assignLayer(t, 2);
        WindowState imeTarget = this.mInputMethodTarget;
        boolean needAssignIme = true;
        if (imeTarget != null && !imeTarget.inSplitScreenWindowingMode() && !imeTarget.mToken.isAppAnimating() && imeTarget.getSurfaceControl() != null) {
            this.mImeWindowsContainers.assignRelativeLayer(t, imeTarget.getSurfaceControl(), 1);
            needAssignIme = false;
        }
        this.mBelowAppWindowsContainers.assignChildLayers(t);
        this.mTaskStackContainers.assignChildLayers(t);
        this.mAboveAppWindowsContainers.assignChildLayers(t, needAssignIme ? this.mImeWindowsContainers : null);
        this.mImeWindowsContainers.assignChildLayers(t);
    }

    /* access modifiers changed from: package-private */
    public void assignRelativeLayerForImeTargetChild(SurfaceControl.Transaction t, WindowContainer child) {
        child.assignRelativeLayer(t, this.mImeWindowsContainers.getSurfaceControl(), 1);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.WindowContainer
    public void prepareSurfaces() {
        Trace.traceBegin(32, "prepareSurfaces");
        try {
            ScreenRotationAnimation screenRotationAnimation = this.mWmService.mAnimator.getScreenRotationAnimationLocked(this.mDisplayId);
            SurfaceControl.Transaction transaction = getPendingTransaction();
            if (screenRotationAnimation != null && screenRotationAnimation.isAnimating()) {
                screenRotationAnimation.getEnterTransformation().getMatrix().getValues(this.mTmpFloats);
                transaction.setMatrix(this.mWindowingLayer, this.mTmpFloats[0], this.mTmpFloats[3], this.mTmpFloats[1], this.mTmpFloats[4]);
                transaction.setPosition(this.mWindowingLayer, this.mTmpFloats[2], this.mTmpFloats[5]);
                transaction.setAlpha(this.mWindowingLayer, screenRotationAnimation.getEnterTransformation().getAlpha());
            }
            super.prepareSurfaces();
            OppoFeatureCache.get(IColorZoomWindowManager.DEFAULT).prepareSurfaceFromDim(this);
            SurfaceControl.mergeToGlobalTransaction(transaction);
        } finally {
            Trace.traceEnd(32);
        }
    }

    /* access modifiers changed from: package-private */
    public void assignStackOrdering() {
        this.mTaskStackContainers.assignStackOrdering(getPendingTransaction());
    }

    /* access modifiers changed from: package-private */
    public void deferUpdateImeTarget() {
        this.mDeferUpdateImeTargetCount++;
    }

    /* access modifiers changed from: package-private */
    public void continueUpdateImeTarget() {
        int i = this.mDeferUpdateImeTargetCount;
        if (i != 0) {
            this.mDeferUpdateImeTargetCount = i - 1;
            if (this.mDeferUpdateImeTargetCount == 0) {
                computeImeTarget(true);
            }
        }
    }

    private boolean canUpdateImeTarget() {
        return this.mDeferUpdateImeTargetCount == 0;
    }

    /* access modifiers changed from: package-private */
    public InputMonitor getInputMonitor() {
        return this.mInputMonitor;
    }

    /* access modifiers changed from: package-private */
    public boolean getLastHasContent() {
        return this.mLastHasContent;
    }

    /* access modifiers changed from: package-private */
    public void registerPointerEventListener(WindowManagerPolicyConstants.PointerEventListener listener) {
        this.mPointerEventDispatcher.registerInputEventListener(listener);
    }

    /* access modifiers changed from: package-private */
    public void unregisterPointerEventListener(WindowManagerPolicyConstants.PointerEventListener listener) {
        this.mPointerEventDispatcher.unregisterInputEventListener(listener);
    }

    /* access modifiers changed from: package-private */
    public void prepareAppTransition(int transit, boolean alwaysKeepCurrent) {
        prepareAppTransition(transit, alwaysKeepCurrent, 0, false);
    }

    /* access modifiers changed from: package-private */
    public void prepareAppTransition(int transit, boolean alwaysKeepCurrent, int flags, boolean forceOverride) {
        if (this.mAppTransition.prepareAppTransitionLocked(transit, alwaysKeepCurrent, flags, forceOverride) && okToAnimate() && !OppoFeatureCache.get(IColorStartingWindowManager.DEFAULT).skipAppTransitionAnimation()) {
            this.mSkipAppTransitionAnimation = false;
        }
    }

    /* access modifiers changed from: package-private */
    public void executeAppTransition() {
        if (this.mAppTransition.isTransitionSet()) {
            if (WindowManagerDebugConfig.DEBUG_APP_TRANSITIONS || WindowManagerDebugConfig.DEBUG_WMS) {
                Slog.w("WindowManager", "Execute app transition: " + this.mAppTransition + ", displayId: " + this.mDisplayId + " Callers=" + Debug.getCallers(5));
            }
            this.mAppTransition.setReady();
            this.mWmService.mWindowPlacerLocked.requestTraversal();
        }
    }

    /* access modifiers changed from: package-private */
    public void handleAnimatingStoppedAndTransition() {
        this.mAppTransition.setIdle();
        for (int i = this.mNoAnimationNotifyOnTransitionFinished.size() - 1; i >= 0; i--) {
            this.mAppTransition.notifyAppTransitionFinishedLocked(this.mNoAnimationNotifyOnTransitionFinished.get(i));
        }
        this.mNoAnimationNotifyOnTransitionFinished.clear();
        this.mWallpaperController.hideDeferredWallpapersIfNeeded();
        onAppTransitionDone();
        int changes = 0 | 1;
        if (WindowManagerDebugConfig.DEBUG_WALLPAPER_LIGHT) {
            Slog.v("WindowManager", "Wallpaper layer changed: assigning layers + relayout");
        }
        computeImeTarget(true);
        this.mWallpaperMayChange = true;
        this.mWmService.mFocusMayChange = true;
        this.pendingLayoutChanges |= changes;
    }

    /* access modifiers changed from: package-private */
    public boolean isNextTransitionForward() {
        int transit = this.mAppTransition.getAppTransition();
        return transit == 6 || transit == 8 || transit == 10;
    }

    /* access modifiers changed from: package-private */
    public boolean supportsSystemDecorations() {
        return (this.mWmService.mDisplayWindowSettings.shouldShowSystemDecorsLocked(this) || (this.mDisplay.getFlags() & 64) != 0 || (this.mWmService.mForceDesktopModeOnExternalDisplays && !isUntrustedVirtualDisplay())) && this.mDisplayId != this.mWmService.mVr2dDisplayId;
    }

    /* access modifiers changed from: package-private */
    public boolean isUntrustedVirtualDisplay() {
        return this.mDisplay.getType() == 5 && this.mDisplay.getOwnerUid() != 1000;
    }

    /* access modifiers changed from: package-private */
    public void reparentDisplayContent(WindowState win, SurfaceControl sc) {
        this.mParentWindow = win;
        this.mParentSurfaceControl = sc;
        if (this.mPortalWindowHandle == null) {
            this.mPortalWindowHandle = createPortalWindowHandle(sc.toString());
        }
        getPendingTransaction().setInputWindowInfo(sc, this.mPortalWindowHandle).reparent(this.mWindowingLayer, sc).reparent(this.mOverlayLayer, sc);
    }

    /* access modifiers changed from: package-private */
    public WindowState getParentWindow() {
        return this.mParentWindow;
    }

    /* access modifiers changed from: package-private */
    public void updateLocation(WindowState win, int x, int y) {
        if (this.mParentWindow != win) {
            throw new IllegalArgumentException("The given window is not the parent window of this display.");
        } else if (this.mLocationInParentWindow.x != x || this.mLocationInParentWindow.y != y) {
            Point point = this.mLocationInParentWindow;
            point.x = x;
            point.y = y;
            if (this.mWmService.mAccessibilityController != null) {
                this.mWmService.mAccessibilityController.onSomeWindowResizedOrMovedLocked();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public Point getLocationInParentWindow() {
        return this.mLocationInParentWindow;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public SurfaceControl getWindowingLayer() {
        return this.mWindowingLayer;
    }

    /* access modifiers changed from: package-private */
    public boolean updateSystemGestureExclusion() {
        if (this.mSystemGestureExclusionListeners.getRegisteredCallbackCount() == 0) {
            return false;
        }
        Region systemGestureExclusion = calculateSystemGestureExclusion();
        try {
            if (this.mSystemGestureExclusion.equals(systemGestureExclusion)) {
                return false;
            }
            this.mSystemGestureExclusion.set(systemGestureExclusion);
            for (int i = this.mSystemGestureExclusionListeners.beginBroadcast() - 1; i >= 0; i--) {
                try {
                    this.mSystemGestureExclusionListeners.getBroadcastItem(i).onSystemGestureExclusionChanged(this.mDisplayId, systemGestureExclusion);
                } catch (RemoteException e) {
                    Slog.e("WindowManager", "Failed to notify SystemGestureExclusionListener", e);
                }
            }
            this.mSystemGestureExclusionListeners.finishBroadcast();
            systemGestureExclusion.recycle();
            return true;
        } finally {
            systemGestureExclusion.recycle();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Region calculateSystemGestureExclusion() {
        Region unhandled = Region.obtain();
        unhandled.set(0, 0, this.mDisplayFrames.mDisplayWidth, this.mDisplayFrames.mDisplayHeight);
        Rect leftEdge = this.mInsetsStateController.getSourceProvider(6).getSource().getFrame();
        Rect rightEdge = this.mInsetsStateController.getSourceProvider(7).getSource().getFrame();
        Region global = Region.obtain();
        Region touchableRegion = Region.obtain();
        Region local = Region.obtain();
        int i = this.mSystemGestureExclusionLimit;
        forAllWindows((Consumer<WindowState>) new Consumer(unhandled, touchableRegion, local, new int[]{i, i}, global, leftEdge, rightEdge) {
            /* class com.android.server.wm.$$Lambda$DisplayContent$K6rK2FF0eVvMG1okrjn0dRcNqM */
            private final /* synthetic */ Region f$1;
            private final /* synthetic */ Region f$2;
            private final /* synthetic */ Region f$3;
            private final /* synthetic */ int[] f$4;
            private final /* synthetic */ Region f$5;
            private final /* synthetic */ Rect f$6;
            private final /* synthetic */ Rect f$7;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
                this.f$7 = r8;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                DisplayContent.this.lambda$calculateSystemGestureExclusion$27$DisplayContent(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, (WindowState) obj);
            }
        }, true);
        local.recycle();
        touchableRegion.recycle();
        unhandled.recycle();
        return global;
    }

    public /* synthetic */ void lambda$calculateSystemGestureExclusion$27$DisplayContent(Region unhandled, Region touchableRegion, Region local, int[] remainingLeftRight, Region global, Rect leftEdge, Rect rightEdge, WindowState w) {
        if (!w.cantReceiveTouchInput() && w.isVisible() && (w.getAttrs().flags & 16) == 0 && !unhandled.isEmpty()) {
            w.getEffectiveTouchableRegion(touchableRegion);
            touchableRegion.op(unhandled, Region.Op.INTERSECT);
            if (w.isImplicitlyExcludingAllSystemGestures()) {
                local.set(touchableRegion);
            } else {
                RegionUtils.rectListToRegion(w.getSystemGestureExclusion(), local);
                local.scale(w.mGlobalScale);
                Rect frame = w.getWindowFrames().mFrame;
                local.translate(frame.left, frame.top);
                local.op(touchableRegion, Region.Op.INTERSECT);
            }
            if (needsGestureExclusionRestrictions(w, this.mLastDispatchedSystemUiVisibility)) {
                remainingLeftRight[0] = addToGlobalAndConsumeLimit(local, global, leftEdge, remainingLeftRight[0]);
                remainingLeftRight[1] = addToGlobalAndConsumeLimit(local, global, rightEdge, remainingLeftRight[1]);
                Region middle = Region.obtain(local);
                middle.op(leftEdge, Region.Op.DIFFERENCE);
                middle.op(rightEdge, Region.Op.DIFFERENCE);
                global.op(middle, Region.Op.UNION);
                middle.recycle();
            } else {
                global.op(local, Region.Op.UNION);
            }
            unhandled.op(touchableRegion, Region.Op.DIFFERENCE);
        }
    }

    private static boolean needsGestureExclusionRestrictions(WindowState win, int sysUiVisibility) {
        int type = win.mAttrs.type;
        return (((sysUiVisibility & UsbACInterface.FORMAT_II_AC3) == 4098) || type == 2011 || type == 2000 || win.getActivityType() == 2) ? false : true;
    }

    private static int addToGlobalAndConsumeLimit(Region local, Region global, Rect edge, int limit) {
        Region r = Region.obtain(local);
        r.op(edge, Region.Op.INTERSECT);
        int[] remaining = {limit};
        RegionUtils.forEachRectReverse(r, new Consumer(remaining, global) {
            /* class com.android.server.wm.$$Lambda$DisplayContent$lwl6jFKEKFlwbOSZU_NpSV_gwEk */
            private final /* synthetic */ int[] f$0;
            private final /* synthetic */ Region f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                DisplayContent.lambda$addToGlobalAndConsumeLimit$28(this.f$0, this.f$1, (Rect) obj);
            }
        });
        r.recycle();
        return remaining[0];
    }

    static /* synthetic */ void lambda$addToGlobalAndConsumeLimit$28(int[] remaining, Region global, Rect rect) {
        if (remaining[0] > 0) {
            int height = rect.height();
            if (height > remaining[0]) {
                rect.top = rect.bottom - remaining[0];
            }
            remaining[0] = remaining[0] - height;
            global.op(rect, Region.Op.UNION);
        }
    }

    /* access modifiers changed from: package-private */
    public void registerSystemGestureExclusionListener(ISystemGestureExclusionListener listener) {
        boolean changed;
        this.mSystemGestureExclusionListeners.register(listener);
        if (this.mSystemGestureExclusionListeners.getRegisteredCallbackCount() == 1) {
            changed = updateSystemGestureExclusion();
        } else {
            changed = false;
        }
        if (!changed) {
            try {
                listener.onSystemGestureExclusionChanged(this.mDisplayId, this.mSystemGestureExclusion);
            } catch (RemoteException e) {
                Slog.e("WindowManager", "Failed to notify SystemGestureExclusionListener during register", e);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unregisterSystemGestureExclusionListener(ISystemGestureExclusionListener listener) {
        this.mSystemGestureExclusionListeners.unregister(listener);
    }

    private InputWindowHandle createPortalWindowHandle(String name) {
        InputWindowHandle portalWindowHandle = new InputWindowHandle((InputApplicationHandle) null, (IWindow) null, -1);
        portalWindowHandle.name = name;
        portalWindowHandle.token = new Binder();
        portalWindowHandle.layoutParamsFlags = 8388648;
        getBounds(this.mTmpBounds);
        portalWindowHandle.touchableRegion.set(this.mTmpBounds);
        portalWindowHandle.scaleFactor = 1.0f;
        portalWindowHandle.ownerPid = Process.myPid();
        portalWindowHandle.ownerUid = Process.myUid();
        portalWindowHandle.portalToDisplayId = this.mDisplayId;
        return portalWindowHandle;
    }

    public void setForwardedInsets(Insets insets) {
        if (insets == null) {
            insets = Insets.NONE;
        }
        if (!this.mDisplayPolicy.getForwardedInsets().equals(insets)) {
            this.mDisplayPolicy.setForwardedInsets(insets);
            setLayoutNeeded();
            this.mWmService.mWindowPlacerLocked.requestTraversal();
        }
    }

    /* access modifiers changed from: protected */
    public MetricsLogger getMetricsLogger() {
        if (this.mMetricsLogger == null) {
            this.mMetricsLogger = new MetricsLogger();
        }
        return this.mMetricsLogger;
    }
}
