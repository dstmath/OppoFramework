package com.android.internal.policy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.UnsupportedAppUsage;
import android.app.ActivityThread;
import android.app.WindowConfiguration;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.LinearGradient;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.RecordingCanvas;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.ContextThemeWrapper;
import android.view.InputQueue;
import android.view.KeyEvent;
import android.view.KeyboardShortcutGroup;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.ThreadedRenderer;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewRootImpl;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowCallbacks;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import com.android.internal.R;
import com.android.internal.policy.PhoneWindow;
import com.android.internal.view.FloatingActionMode;
import com.android.internal.view.RootViewSurfaceTaker;
import com.android.internal.view.StandaloneActionMode;
import com.android.internal.view.menu.ContextMenuBuilder;
import com.android.internal.view.menu.MenuHelper;
import com.android.internal.widget.ActionBarContextView;
import com.android.internal.widget.BackgroundFallback;
import com.android.internal.widget.DecorCaptionView;
import com.android.internal.widget.FloatingToolbar;
import com.color.statusbar.ColorStatusBarController;
import com.mediatek.view.ViewDebugManager;
import com.oppo.debug.InputLog;
import java.util.List;

public class DecorView extends FrameLayout implements RootViewSurfaceTaker, WindowCallbacks {
    private static final int ACTION_MODE_FADE_ALPHA_DURATION = 100;
    private static final boolean DEBUG_MEASURE = false;
    private static boolean DEBUG_OPPO_SYSTEMBAR = false;
    private static final int DECOR_SHADOW_FOCUSED_HEIGHT_IN_DIP = 20;
    private static final int DECOR_SHADOW_UNFOCUSED_HEIGHT_IN_DIP = 5;
    public static final ColorViewAttributes NAVIGATION_BAR_COLOR_VIEW_ATTRIBUTES = new ColorViewAttributes(2, 134217728, 80, 5, 3, Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME, 16908336, 0);
    private static final ViewOutlineProvider PIP_OUTLINE_PROVIDER = new ViewOutlineProvider() {
        /* class com.android.internal.policy.DecorView.AnonymousClass1 */

        @Override // android.view.ViewOutlineProvider
        public void getOutline(View view, Outline outline) {
            outline.setRect(0, 0, view.getWidth(), view.getHeight());
            outline.setAlpha(1.0f);
        }
    };
    private static final int SCRIM_LIGHT = -419430401;
    public static final ColorViewAttributes STATUS_BAR_COLOR_VIEW_ATTRIBUTES = new ColorViewAttributes(4, 67108864, 48, 3, 5, Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME, 16908335, 1024);
    private static final boolean SWEEP_OPEN_MENU = false;
    private static final String TAG = "DecorView";
    private boolean mAllowUpdateElevation = false;
    private boolean mApplyFloatingHorizontalInsets = false;
    private boolean mApplyFloatingVerticalInsets = false;
    private float mAvailableWidth;
    private BackdropFrameRenderer mBackdropFrameRenderer = null;
    private final BackgroundFallback mBackgroundFallback = new BackgroundFallback();
    private Insets mBackgroundInsets = Insets.NONE;
    private final Rect mBackgroundPadding = new Rect();
    private final int mBarEnterExitDuration;
    private Drawable mCaptionBackgroundDrawable;
    private boolean mChanging;
    ViewGroup mContentRoot;
    private DecorCaptionView mDecorCaptionView;
    int mDefaultOpacity = -1;
    private int mDownY;
    private boolean mDrawLegacyNavigationBarBackground;
    private final Rect mDrawingBounds = new Rect();
    private boolean mElevationAdjustedForStack = false;
    /* access modifiers changed from: private */
    public ObjectAnimator mFadeAnim;
    private final int mFeatureId;
    /* access modifiers changed from: private */
    public ActionMode mFloatingActionMode;
    private View mFloatingActionModeOriginatingView;
    private final Rect mFloatingInsets = new Rect();
    private FloatingToolbar mFloatingToolbar;
    private ViewTreeObserver.OnPreDrawListener mFloatingToolbarPreDrawListener;
    final boolean mForceWindowDrawsBarBackgrounds;
    private final Rect mFrameOffsets = new Rect();
    private final Rect mFramePadding = new Rect();
    private boolean mHasCaption = false;
    private final Interpolator mHideInterpolator;
    private final Paint mHorizontalResizeShadowPaint = new Paint();
    private boolean mIsInPictureInPictureMode;
    private Drawable.Callback mLastBackgroundDrawableCb = null;
    private Insets mLastBackgroundInsets = Insets.NONE;
    @UnsupportedAppUsage
    private int mLastBottomInset = 0;
    boolean mLastClipToOutline = false;
    private boolean mLastHasBottomStableInset = false;
    private boolean mLastHasLeftStableInset = false;
    private boolean mLastHasRightStableInset = false;
    private boolean mLastHasTopStableInset = false;
    @UnsupportedAppUsage
    private int mLastLeftInset = 0;
    private Drawable mLastOriginalBackgroundDrawable;
    private ViewOutlineProvider mLastOutlineProvider;
    @UnsupportedAppUsage
    private int mLastRightInset = 0;
    private boolean mLastShouldAlwaysConsumeSystemBars = false;
    private int mLastTopInset = 0;
    private int mLastWindowFlags = 0;
    private final Paint mLegacyNavigationBarBackgroundPaint = new Paint();
    String mLogTag = TAG;
    private Drawable mMenuBackground;
    private final ColorViewState mNavigationColorViewState = new ColorViewState(NAVIGATION_BAR_COLOR_VIEW_ATTRIBUTES);
    private Drawable mOriginalBackgroundDrawable;
    private Rect mOutsets = new Rect();
    ActionMode mPrimaryActionMode;
    /* access modifiers changed from: private */
    public PopupWindow mPrimaryActionModePopup;
    /* access modifiers changed from: private */
    public ActionBarContextView mPrimaryActionModeView;
    private int mResizeMode = -1;
    private final int mResizeShadowSize;
    private Drawable mResizingBackgroundDrawable;
    private int mRootScrollY = 0;
    private final int mSemiTransparentBarColor;
    private final Interpolator mShowInterpolator;
    /* access modifiers changed from: private */
    public Runnable mShowPrimaryActionModePopup;
    private final ColorViewState mStatusColorViewState = new ColorViewState(STATUS_BAR_COLOR_VIEW_ATTRIBUTES);
    private View mStatusGuard;
    private Rect mTempRect;
    private Drawable mUserCaptionBackgroundDrawable;
    private final Paint mVerticalResizeShadowPaint = new Paint();
    private boolean mWatchingForMenu;
    /* access modifiers changed from: private */
    @UnsupportedAppUsage
    public PhoneWindow mWindow;
    int mWindowMode = -1;
    private boolean mWindowResizeCallbacksAdded = false;

    DecorView(Context context, int featureId, PhoneWindow window, WindowManager.LayoutParams params) {
        super(context);
        boolean z;
        this.mFeatureId = featureId;
        this.mShowInterpolator = AnimationUtils.loadInterpolator(context, 17563662);
        this.mHideInterpolator = AnimationUtils.loadInterpolator(context, 17563663);
        this.mBarEnterExitDuration = context.getResources().getInteger(R.integer.dock_enter_exit_duration);
        if (ActivityThread.inCptWhiteList(31, context.getApplicationInfo().packageName)) {
            z = false;
        } else {
            z = context.getResources().getBoolean(R.bool.config_forceWindowDrawsStatusBarBackground) && context.getApplicationInfo().targetSdkVersion >= 24;
        }
        this.mForceWindowDrawsBarBackgrounds = z;
        this.mSemiTransparentBarColor = context.getResources().getColor(R.color.system_bar_background_semi_transparent, null);
        updateAvailableWidth();
        setWindow(window);
        updateLogTag(params);
        this.mResizeShadowSize = context.getResources().getDimensionPixelSize(R.dimen.resize_shadow_size);
        initResizingPaints();
        this.mLegacyNavigationBarBackgroundPaint.setColor(-16777216);
        DEBUG_OPPO_SYSTEMBAR = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    }

    /* access modifiers changed from: package-private */
    public void setBackgroundFallback(Drawable fallbackDrawable) {
        this.mBackgroundFallback.setDrawable(fallbackDrawable);
        setWillNotDraw(getBackground() == null && !this.mBackgroundFallback.hasFallback());
    }

    public Drawable getBackgroundFallback() {
        return this.mBackgroundFallback.getDrawable();
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean gatherTransparentRegion(Region region) {
        return gatherTransparentRegion(this.mStatusColorViewState, region) || gatherTransparentRegion(this.mNavigationColorViewState, region) || super.gatherTransparentRegion(region);
    }

    /* access modifiers changed from: package-private */
    public boolean gatherTransparentRegion(ColorViewState colorViewState, Region region) {
        if (colorViewState.view == null || !colorViewState.visible || !isResizing()) {
            return false;
        }
        return colorViewState.view.gatherTransparentRegion(region);
    }

    @Override // android.view.View
    public void onDraw(Canvas c) {
        super.onDraw(c);
        this.mBackgroundFallback.draw(this, this.mContentRoot, c, this.mWindow.mContentParent, this.mStatusColorViewState.view, this.mNavigationColorViewState.view);
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean handled;
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        boolean isDown = action == 0;
        if (InputLog.DEBUG) {
            InputLog.d(TAG, " dispatchKeyEvent, keyCode = " + keyCode + ", action = " + action + ", isDown = " + isDown);
        }
        if (InputLog.DEBUG) {
            InputLog.d(TAG, " event.getRepeatCount() = " + event.getRepeatCount() + ", isDestroyed() = " + this.mWindow.isDestroyed());
        }
        if (isDown && event.getRepeatCount() == 0) {
            if (this.mWindow.mPanelChordingKey > 0 && this.mWindow.mPanelChordingKey != keyCode && dispatchKeyShortcutEvent(event)) {
                return true;
            }
            if (this.mWindow.mPreparedPanel != null && this.mWindow.mPreparedPanel.isOpen) {
                PhoneWindow phoneWindow = this.mWindow;
                if (phoneWindow.performPanelShortcut(phoneWindow.mPreparedPanel, keyCode, event, 0)) {
                    return true;
                }
            }
        }
        if (!this.mWindow.isDestroyed()) {
            Window.Callback cb = this.mWindow.getCallback();
            if (cb == null || this.mFeatureId >= 0) {
                handled = super.dispatchKeyEvent(event);
            } else {
                handled = cb.dispatchKeyEvent(event);
            }
            if (InputLog.DEBUG) {
                InputLog.d(TAG, "dispatchKeyEvent = " + event + " cb = " + cb + ", mFeatureId = " + this.mFeatureId + ", handled = " + handled);
            }
            if (handled) {
                return true;
            }
        }
        if (isDown) {
            return this.mWindow.onKeyDown(this.mFeatureId, event.getKeyCode(), event);
        }
        return this.mWindow.onKeyUp(this.mFeatureId, event.getKeyCode(), event);
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchKeyShortcutEvent(KeyEvent ev) {
        if (this.mWindow.mPreparedPanel != null) {
            PhoneWindow phoneWindow = this.mWindow;
            if (phoneWindow.performPanelShortcut(phoneWindow.mPreparedPanel, ev.getKeyCode(), ev, 1)) {
                if (this.mWindow.mPreparedPanel != null) {
                    this.mWindow.mPreparedPanel.isHandled = true;
                }
                return true;
            }
        }
        Window.Callback cb = this.mWindow.getCallback();
        if ((cb == null || this.mWindow.isDestroyed() || this.mFeatureId >= 0) ? super.dispatchKeyShortcutEvent(ev) : cb.dispatchKeyShortcutEvent(ev)) {
            return true;
        }
        PhoneWindow.PanelFeatureState st = this.mWindow.getPanelState(0, false);
        if (st != null && this.mWindow.mPreparedPanel == null) {
            this.mWindow.preparePanel(st, ev);
            boolean handled = this.mWindow.performPanelShortcut(st, ev.getKeyCode(), ev, 1);
            st.isPrepared = false;
            if (handled) {
                return true;
            }
        }
        return false;
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Window.Callback cb = this.mWindow.getCallback();
        return (cb == null || this.mWindow.isDestroyed() || this.mFeatureId >= 0) ? super.dispatchTouchEvent(ev) : cb.dispatchTouchEvent(ev);
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchTrackballEvent(MotionEvent ev) {
        Window.Callback cb = this.mWindow.getCallback();
        return (cb == null || this.mWindow.isDestroyed() || this.mFeatureId >= 0) ? super.dispatchTrackballEvent(ev) : cb.dispatchTrackballEvent(ev);
    }

    @Override // android.view.View
    public boolean dispatchGenericMotionEvent(MotionEvent ev) {
        Window.Callback cb = this.mWindow.getCallback();
        return (cb == null || this.mWindow.isDestroyed() || this.mFeatureId >= 0) ? super.dispatchGenericMotionEvent(ev) : cb.dispatchGenericMotionEvent(ev);
    }

    public boolean superDispatchKeyEvent(KeyEvent event) {
        if (InputLog.DEBUG) {
            InputLog.d(TAG, " superDispatchKeyEvent(), event = " + KeyEvent.keyCodeToString(event.getKeyCode()));
        }
        if (event.getKeyCode() == 4) {
            int action = event.getAction();
            ActionMode actionMode = this.mPrimaryActionMode;
            if (actionMode != null) {
                if (action == 1) {
                    actionMode.finish();
                }
                if (InputLog.DEBUG) {
                    InputLog.d(TAG, " KeyEvent.KEYCODE_BACK consumed by " + this.mPrimaryActionMode);
                }
                return true;
            }
        }
        if (super.dispatchKeyEvent(event)) {
            return true;
        }
        if (getViewRootImpl() == null || !getViewRootImpl().dispatchUnhandledKeyEvent(event)) {
            return false;
        }
        return true;
    }

    public boolean superDispatchKeyShortcutEvent(KeyEvent event) {
        return super.dispatchKeyShortcutEvent(event);
    }

    public boolean superDispatchTouchEvent(MotionEvent event) {
        return super.dispatchTouchEvent(event);
    }

    public boolean superDispatchTrackballEvent(MotionEvent event) {
        return super.dispatchTrackballEvent(event);
    }

    public boolean superDispatchGenericMotionEvent(MotionEvent event) {
        return super.dispatchGenericMotionEvent(event);
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        return onInterceptTouchEvent(event);
    }

    private boolean isOutOfInnerBounds(int x, int y) {
        return x < 0 || y < 0 || x > getWidth() || y > getHeight();
    }

    private boolean isOutOfBounds(int x, int y) {
        return x < -5 || y < -5 || x > getWidth() + 5 || y > getHeight() + 5;
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (this.mHasCaption && isShowingCaption() && action == 0 && isOutOfInnerBounds((int) event.getX(), (int) event.getY())) {
            return true;
        }
        if (this.mFeatureId < 0 || action != 0 || !isOutOfBounds((int) event.getX(), (int) event.getY())) {
            return false;
        }
        this.mWindow.closePanel(this.mFeatureId);
        return true;
    }

    @Override // android.view.View, android.view.accessibility.AccessibilityEventSource
    public void sendAccessibilityEvent(int eventType) {
        if (AccessibilityManager.getInstance(this.mContext).isEnabled()) {
            int i = this.mFeatureId;
            if ((i == 0 || i == 6 || i == 2 || i == 5) && getChildCount() == 1) {
                getChildAt(0).sendAccessibilityEvent(eventType);
            } else {
                super.sendAccessibilityEvent(eventType);
            }
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchPopulateAccessibilityEventInternal(AccessibilityEvent event) {
        Window.Callback cb = this.mWindow.getCallback();
        if (cb == null || this.mWindow.isDestroyed() || !cb.dispatchPopulateAccessibilityEvent(event)) {
            return super.dispatchPopulateAccessibilityEventInternal(event);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public boolean setFrame(int l, int t, int r, int b) {
        boolean changed = super.setFrame(l, t, r, b);
        if (changed) {
            Rect drawingBounds = this.mDrawingBounds;
            getDrawingRect(drawingBounds);
            Drawable fg = getForeground();
            if (fg != null) {
                Rect frameOffsets = this.mFrameOffsets;
                drawingBounds.left += frameOffsets.left;
                drawingBounds.top += frameOffsets.top;
                drawingBounds.right -= frameOffsets.right;
                drawingBounds.bottom -= frameOffsets.bottom;
                fg.setBounds(drawingBounds);
                Rect framePadding = this.mFramePadding;
                drawingBounds.left += framePadding.left - frameOffsets.left;
                drawingBounds.top += framePadding.top - frameOffsets.top;
                drawingBounds.right -= framePadding.right - frameOffsets.right;
                drawingBounds.bottom -= framePadding.bottom - frameOffsets.bottom;
            }
            Drawable bg = super.getBackground();
            if (bg != null) {
                bg.setBounds(drawingBounds);
            }
        }
        return changed;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0087  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x014a  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x014d  */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x0153  */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x0175  */
    /* JADX WARNING: Removed duplicated region for block: B:82:? A[RETURN, SYNTHETIC] */
    @Override // android.widget.FrameLayout, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMeasureSpec2;
        int heightMeasureSpec2;
        boolean measure;
        TypedValue tv;
        int min;
        int mode;
        int mode2;
        TypedValue tvh;
        int h;
        int w;
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        boolean isPortrait = getResources().getConfiguration().orientation == 1;
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        boolean fixedWidth = false;
        this.mApplyFloatingHorizontalInsets = false;
        if (widthMode == Integer.MIN_VALUE) {
            PhoneWindow phoneWindow = this.mWindow;
            TypedValue tvw = isPortrait ? phoneWindow.mFixedWidthMinor : phoneWindow.mFixedWidthMajor;
            if (!(tvw == null || tvw.type == 0)) {
                if (tvw.type == 5) {
                    w = (int) tvw.getDimension(metrics);
                } else if (tvw.type == 6) {
                    w = (int) tvw.getFraction((float) metrics.widthPixels, (float) metrics.widthPixels);
                } else {
                    w = 0;
                }
                int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
                if (w > 0) {
                    widthMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(Math.min(w, widthSize), 1073741824);
                    fixedWidth = true;
                } else {
                    int widthMeasureSpec3 = View.MeasureSpec.makeMeasureSpec((widthSize - this.mFloatingInsets.left) - this.mFloatingInsets.right, Integer.MIN_VALUE);
                    this.mApplyFloatingHorizontalInsets = true;
                    widthMeasureSpec2 = widthMeasureSpec3;
                }
                this.mApplyFloatingVerticalInsets = false;
                if (heightMode == Integer.MIN_VALUE) {
                    if (isPortrait) {
                        tvh = this.mWindow.mFixedHeightMajor;
                    } else {
                        tvh = this.mWindow.mFixedHeightMinor;
                    }
                    if (!(tvh == null || tvh.type == 0)) {
                        if (tvh.type == 5) {
                            h = (int) tvh.getDimension(metrics);
                        } else if (tvh.type == 6) {
                            h = (int) tvh.getFraction((float) metrics.heightPixels, (float) metrics.heightPixels);
                        } else {
                            h = 0;
                        }
                        int heightSize = View.MeasureSpec.getSize(heightMeasureSpec);
                        if (h > 0) {
                            heightMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(Math.min(h, heightSize), 1073741824);
                        } else if ((this.mWindow.getAttributes().flags & 256) == 0) {
                            heightMeasureSpec2 = View.MeasureSpec.makeMeasureSpec((heightSize - this.mFloatingInsets.top) - this.mFloatingInsets.bottom, Integer.MIN_VALUE);
                            this.mApplyFloatingVerticalInsets = true;
                        }
                        getOutsets(this.mOutsets);
                        if ((this.mOutsets.top > 0 || this.mOutsets.bottom > 0) && (mode2 = View.MeasureSpec.getMode(heightMeasureSpec2)) != 0) {
                            heightMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(this.mOutsets.top + View.MeasureSpec.getSize(heightMeasureSpec2) + this.mOutsets.bottom, mode2);
                        }
                        if ((this.mOutsets.left > 0 || this.mOutsets.right > 0) && (mode = View.MeasureSpec.getMode(widthMeasureSpec2)) != 0) {
                            widthMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(this.mOutsets.left + View.MeasureSpec.getSize(widthMeasureSpec2) + this.mOutsets.right, mode);
                        }
                        super.onMeasure(widthMeasureSpec2, heightMeasureSpec2);
                        int width = getMeasuredWidth();
                        measure = false;
                        int widthMeasureSpec4 = View.MeasureSpec.makeMeasureSpec(width, 1073741824);
                        if (!fixedWidth && widthMode == Integer.MIN_VALUE) {
                            PhoneWindow phoneWindow2 = this.mWindow;
                            tv = isPortrait ? phoneWindow2.mMinWidthMinor : phoneWindow2.mMinWidthMajor;
                            if (tv.type != 0) {
                                if (tv.type == 5) {
                                    min = (int) tv.getDimension(metrics);
                                } else if (tv.type == 6) {
                                    float f = this.mAvailableWidth;
                                    min = (int) tv.getFraction(f, f);
                                } else {
                                    min = 0;
                                }
                                if (width < min) {
                                    widthMeasureSpec4 = View.MeasureSpec.makeMeasureSpec(min, 1073741824);
                                    measure = true;
                                }
                            }
                        }
                        if (measure) {
                            super.onMeasure(widthMeasureSpec4, heightMeasureSpec2);
                            return;
                        }
                        return;
                    }
                }
                heightMeasureSpec2 = heightMeasureSpec;
                getOutsets(this.mOutsets);
                heightMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(this.mOutsets.top + View.MeasureSpec.getSize(heightMeasureSpec2) + this.mOutsets.bottom, mode2);
                widthMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(this.mOutsets.left + View.MeasureSpec.getSize(widthMeasureSpec2) + this.mOutsets.right, mode);
                super.onMeasure(widthMeasureSpec2, heightMeasureSpec2);
                int width2 = getMeasuredWidth();
                measure = false;
                int widthMeasureSpec42 = View.MeasureSpec.makeMeasureSpec(width2, 1073741824);
                PhoneWindow phoneWindow22 = this.mWindow;
                if (isPortrait) {
                }
                if (tv.type != 0) {
                }
                if (measure) {
                }
            }
        }
        widthMeasureSpec2 = widthMeasureSpec;
        this.mApplyFloatingVerticalInsets = false;
        if (heightMode == Integer.MIN_VALUE) {
        }
        heightMeasureSpec2 = heightMeasureSpec;
        getOutsets(this.mOutsets);
        heightMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(this.mOutsets.top + View.MeasureSpec.getSize(heightMeasureSpec2) + this.mOutsets.bottom, mode2);
        widthMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(this.mOutsets.left + View.MeasureSpec.getSize(widthMeasureSpec2) + this.mOutsets.right, mode);
        super.onMeasure(widthMeasureSpec2, heightMeasureSpec2);
        int width22 = getMeasuredWidth();
        measure = false;
        int widthMeasureSpec422 = View.MeasureSpec.makeMeasureSpec(width22, 1073741824);
        PhoneWindow phoneWindow222 = this.mWindow;
        if (isPortrait) {
        }
        if (tv.type != 0) {
        }
        if (measure) {
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.view.ViewGroup, android.view.View
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        getOutsets(this.mOutsets);
        if (this.mOutsets.left > 0) {
            offsetLeftAndRight(-this.mOutsets.left);
        }
        if (this.mOutsets.top > 0) {
            offsetTopAndBottom(-this.mOutsets.top);
        }
        if (this.mApplyFloatingVerticalInsets) {
            offsetTopAndBottom(this.mFloatingInsets.top);
        }
        if (this.mApplyFloatingHorizontalInsets) {
            offsetLeftAndRight(this.mFloatingInsets.left);
        }
        updateElevation();
        this.mAllowUpdateElevation = true;
        if (changed && this.mResizeMode == 1) {
            getViewRootImpl().requestInvalidateRootRenderNode();
        }
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Drawable drawable = this.mMenuBackground;
        if (drawable != null) {
            drawable.draw(canvas);
        }
    }

    @Override // android.view.ViewParent, android.view.ViewGroup
    public boolean showContextMenuForChild(View originalView) {
        return showContextMenuForChildInternal(originalView, Float.NaN, Float.NaN);
    }

    @Override // android.view.ViewParent, android.view.ViewGroup
    public boolean showContextMenuForChild(View originalView, float x, float y) {
        return showContextMenuForChildInternal(originalView, x, y);
    }

    private boolean showContextMenuForChildInternal(View originalView, float x, float y) {
        MenuHelper helper;
        if (this.mWindow.mContextMenuHelper != null) {
            this.mWindow.mContextMenuHelper.dismiss();
            this.mWindow.mContextMenuHelper = null;
        }
        PhoneWindow.PhoneWindowMenuCallback callback = this.mWindow.mContextMenuCallback;
        if (this.mWindow.mContextMenu == null) {
            this.mWindow.mContextMenu = new ContextMenuBuilder(getContext());
            this.mWindow.mContextMenu.setCallback(callback);
        } else {
            this.mWindow.mContextMenu.clearAll();
        }
        boolean isPopup = !Float.isNaN(x) && !Float.isNaN(y);
        if (isPopup) {
            helper = this.mWindow.mContextMenu.showPopup(getContext(), originalView, x, y);
        } else {
            helper = this.mWindow.mContextMenu.showDialog(originalView, originalView.getWindowToken());
        }
        if (helper != null) {
            callback.setShowDialogForSubmenu(!isPopup);
            helper.setPresenterCallback(callback);
        }
        this.mWindow.mContextMenuHelper = helper;
        return helper != null;
    }

    @Override // android.view.ViewParent, android.view.ViewGroup
    public ActionMode startActionModeForChild(View originalView, ActionMode.Callback callback) {
        return startActionModeForChild(originalView, callback, 0);
    }

    @Override // android.view.ViewParent, android.view.ViewGroup
    public ActionMode startActionModeForChild(View child, ActionMode.Callback callback, int type) {
        return startActionMode(child, callback, type);
    }

    @Override // android.view.View
    public ActionMode startActionMode(ActionMode.Callback callback) {
        return startActionMode(callback, 0);
    }

    @Override // android.view.View
    public ActionMode startActionMode(ActionMode.Callback callback, int type) {
        return startActionMode(this, callback, type);
    }

    private ActionMode startActionMode(View originatingView, ActionMode.Callback callback, int type) {
        ActionMode.Callback2 wrappedCallback = new ActionModeCallback2Wrapper(callback);
        ActionMode mode = null;
        if (this.mWindow.getCallback() != null && !this.mWindow.isDestroyed()) {
            try {
                mode = this.mWindow.getCallback().onWindowStartingActionMode(wrappedCallback, type);
            } catch (AbstractMethodError e) {
                if (type == 0) {
                    try {
                        mode = this.mWindow.getCallback().onWindowStartingActionMode(wrappedCallback);
                    } catch (AbstractMethodError e2) {
                    }
                }
            }
        }
        if (mode == null) {
            mode = createActionMode(type, wrappedCallback, originatingView);
            if (mode == null || !wrappedCallback.onCreateActionMode(mode, mode.getMenu())) {
                mode = null;
            } else {
                setHandledActionMode(mode);
            }
        } else if (mode.getType() == 0) {
            cleanupPrimaryActionMode();
            this.mPrimaryActionMode = mode;
        } else if (mode.getType() == 1) {
            ActionMode actionMode = this.mFloatingActionMode;
            if (actionMode != null) {
                actionMode.finish();
            }
            this.mFloatingActionMode = mode;
        }
        if (!(mode == null || this.mWindow.getCallback() == null || this.mWindow.isDestroyed())) {
            try {
                this.mWindow.getCallback().onActionModeStarted(mode);
            } catch (AbstractMethodError e3) {
            }
        }
        return mode;
    }

    private void cleanupPrimaryActionMode() {
        ActionMode actionMode = this.mPrimaryActionMode;
        if (actionMode != null) {
            actionMode.finish();
            this.mPrimaryActionMode = null;
        }
        ActionBarContextView actionBarContextView = this.mPrimaryActionModeView;
        if (actionBarContextView != null) {
            actionBarContextView.killMode();
        }
    }

    /* access modifiers changed from: private */
    public void cleanupFloatingActionModeViews() {
        FloatingToolbar floatingToolbar = this.mFloatingToolbar;
        if (floatingToolbar != null) {
            floatingToolbar.dismiss();
            this.mFloatingToolbar = null;
        }
        View view = this.mFloatingActionModeOriginatingView;
        if (view != null) {
            if (this.mFloatingToolbarPreDrawListener != null) {
                view.getViewTreeObserver().removeOnPreDrawListener(this.mFloatingToolbarPreDrawListener);
                this.mFloatingToolbarPreDrawListener = null;
            }
            this.mFloatingActionModeOriginatingView = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void startChanging() {
        this.mChanging = true;
    }

    /* access modifiers changed from: package-private */
    public void finishChanging() {
        this.mChanging = false;
        drawableChanged();
    }

    public void setWindowBackground(Drawable drawable) {
        if (this.mOriginalBackgroundDrawable != drawable) {
            this.mOriginalBackgroundDrawable = drawable;
            updateBackgroundDrawable();
            boolean z = false;
            if (drawable != null) {
                if (this.mWindow.isTranslucent() || this.mWindow.isShowingWallpaper()) {
                    z = true;
                }
                this.mResizingBackgroundDrawable = enforceNonTranslucentBackground(drawable, z);
            } else {
                Drawable drawable2 = this.mWindow.mBackgroundDrawable;
                Drawable drawable3 = this.mWindow.mBackgroundFallbackDrawable;
                if (this.mWindow.isTranslucent() || this.mWindow.isShowingWallpaper()) {
                    z = true;
                }
                this.mResizingBackgroundDrawable = getResizingBackgroundDrawable(drawable2, drawable3, z);
            }
            Drawable drawable4 = this.mResizingBackgroundDrawable;
            if (drawable4 != null) {
                drawable4.getPadding(this.mBackgroundPadding);
            } else {
                this.mBackgroundPadding.setEmpty();
            }
            drawableChanged();
        }
    }

    @Override // android.view.View
    public void setBackgroundDrawable(Drawable background) {
        if (this.mOriginalBackgroundDrawable != background) {
            this.mOriginalBackgroundDrawable = background;
            updateBackgroundDrawable();
            if (!View.sBrokenWindowBackground) {
                drawableChanged();
            }
        }
    }

    public void setWindowFrame(Drawable drawable) {
        if (getForeground() != drawable) {
            setForeground(drawable);
            if (drawable != null) {
                drawable.getPadding(this.mFramePadding);
            } else {
                this.mFramePadding.setEmpty();
            }
            drawableChanged();
        }
    }

    @Override // android.view.View
    public void onWindowSystemUiVisibilityChanged(int visible) {
        updateColorViews(null, true);
        updateDecorCaptionStatus(getResources().getConfiguration());
        View view = this.mStatusGuard;
        if (view != null && view.getVisibility() == 0) {
            updateStatusGuardColor();
        }
    }

    @Override // android.view.View
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        WindowManager.LayoutParams attrs = this.mWindow.getAttributes();
        this.mFloatingInsets.setEmpty();
        if ((attrs.flags & 256) == 0) {
            if (attrs.height == -2) {
                this.mFloatingInsets.top = insets.getSystemWindowInsetTop();
                this.mFloatingInsets.bottom = insets.getSystemWindowInsetBottom();
                insets = insets.inset(0, insets.getSystemWindowInsetTop(), 0, insets.getSystemWindowInsetBottom());
            }
            if (this.mWindow.getAttributes().width == -2) {
                this.mFloatingInsets.left = insets.getSystemWindowInsetTop();
                this.mFloatingInsets.right = insets.getSystemWindowInsetBottom();
                insets = insets.inset(insets.getSystemWindowInsetLeft(), 0, insets.getSystemWindowInsetRight(), 0);
            }
        }
        this.mFrameOffsets.set(insets.getSystemWindowInsetsAsRect());
        WindowInsets insets2 = updateStatusGuard(updateColorViews(insets, true));
        if (getForeground() != null) {
            drawableChanged();
        }
        return insets2;
    }

    @Override // android.view.ViewGroup
    public boolean isTransitionGroup() {
        return false;
    }

    public static int getColorViewTopInset(int stableTop, int systemTop) {
        return Math.min(stableTop, systemTop);
    }

    public static int getColorViewBottomInset(int stableBottom, int systemBottom) {
        return Math.min(stableBottom, systemBottom);
    }

    public static int getColorViewRightInset(int stableRight, int systemRight) {
        return Math.min(stableRight, systemRight);
    }

    public static int getColorViewLeftInset(int stableLeft, int systemLeft) {
        return Math.min(stableLeft, systemLeft);
    }

    public static boolean isNavBarToRightEdge(int bottomInset, int rightInset) {
        return bottomInset == 0 && rightInset > 0;
    }

    public static boolean isNavBarToLeftEdge(int bottomInset, int leftInset) {
        return bottomInset == 0 && leftInset > 0;
    }

    public static int getNavBarSize(int bottomInset, int rightInset, int leftInset) {
        if (isNavBarToRightEdge(bottomInset, rightInset)) {
            return rightInset;
        }
        return isNavBarToLeftEdge(bottomInset, leftInset) ? leftInset : bottomInset;
    }

    public static void getNavigationBarRect(int canvasWidth, int canvasHeight, Rect stableInsets, Rect contentInsets, Rect outRect, float scale) {
        int bottomInset = (int) (((float) getColorViewBottomInset(stableInsets.bottom, contentInsets.bottom)) * scale);
        int leftInset = (int) (((float) getColorViewLeftInset(stableInsets.left, contentInsets.left)) * scale);
        int rightInset = (int) (((float) getColorViewLeftInset(stableInsets.right, contentInsets.right)) * scale);
        int size = getNavBarSize(bottomInset, rightInset, leftInset);
        if (isNavBarToRightEdge(bottomInset, rightInset)) {
            outRect.set(canvasWidth - size, 0, canvasWidth, canvasHeight);
        } else if (isNavBarToLeftEdge(bottomInset, leftInset)) {
            outRect.set(0, 0, size, canvasHeight);
        } else {
            outRect.set(0, canvasHeight - size, canvasWidth, canvasHeight);
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:180:0x028d  */
    /* JADX WARNING: Removed duplicated region for block: B:181:0x029c  */
    /* JADX WARNING: Removed duplicated region for block: B:184:0x02a7  */
    /* JADX WARNING: Removed duplicated region for block: B:186:? A[RETURN, SYNTHETIC] */
    public WindowInsets updateColorViews(WindowInsets insets, boolean animate) {
        int i;
        WindowInsets insets2;
        boolean disallowAnimate;
        int navBarColor;
        int statusBarSideInset;
        ViewRootImpl vri;
        WindowManager.LayoutParams attrs = this.mWindow.getAttributes();
        int sysUiVisibility = attrs.systemUiVisibility | getWindowSystemUiVisibility();
        boolean isImeWindow = this.mWindow.getAttributes().type == 2011;
        if (!this.mWindow.mIsFloating || isImeWindow) {
            boolean disallowAnimate2 = (!isLaidOut()) | (((this.mLastWindowFlags ^ attrs.flags) & Integer.MIN_VALUE) != 0);
            this.mLastWindowFlags = attrs.flags;
            if (insets != null) {
                this.mLastTopInset = getColorViewTopInset(insets.getStableInsetTop(), insets.getSystemWindowInsetTop());
                this.mLastBottomInset = ColorStatusBarController.getInstance().getBottomInset(isImeWindow, getColorViewBottomInset(insets.getStableInsetBottom(), insets.getSystemWindowInsetBottom()), insets);
                this.mLastRightInset = getColorViewRightInset(insets.getStableInsetRight(), insets.getSystemWindowInsetRight());
                this.mLastLeftInset = getColorViewRightInset(insets.getStableInsetLeft(), insets.getSystemWindowInsetLeft());
                boolean hasTopStableInset = insets.getStableInsetTop() != 0;
                boolean disallowAnimate3 = disallowAnimate2 | (hasTopStableInset != this.mLastHasTopStableInset);
                this.mLastHasTopStableInset = hasTopStableInset;
                boolean hasBottomStableInset = insets.getStableInsetBottom() != 0;
                boolean disallowAnimate4 = disallowAnimate3 | (hasBottomStableInset != this.mLastHasBottomStableInset);
                this.mLastHasBottomStableInset = hasBottomStableInset;
                boolean hasRightStableInset = insets.getStableInsetRight() != 0;
                boolean disallowAnimate5 = disallowAnimate4 | (hasRightStableInset != this.mLastHasRightStableInset);
                this.mLastHasRightStableInset = hasRightStableInset;
                boolean hasLeftStableInset = insets.getStableInsetLeft() != 0;
                boolean z = hasLeftStableInset != this.mLastHasLeftStableInset;
                this.mLastHasLeftStableInset = hasLeftStableInset;
                this.mLastShouldAlwaysConsumeSystemBars = insets.shouldAlwaysConsumeSystemBars();
                disallowAnimate = disallowAnimate5 | z;
            } else {
                disallowAnimate = disallowAnimate2;
            }
            boolean navBarToRightEdge = isNavBarToRightEdge(this.mLastBottomInset, this.mLastRightInset);
            boolean navBarToLeftEdge = isNavBarToLeftEdge(this.mLastBottomInset, this.mLastLeftInset);
            int navBarSize = getNavBarSize(this.mLastBottomInset, this.mLastRightInset, this.mLastLeftInset);
            int navBarColor2 = isImeWindow ? ColorStatusBarController.getInstance().getNavigationGuardColor() : calculateNavigationBarColor();
            if (this.mContext.getPackageManager().isClosedSuperFirewall()) {
                navBarColor = this.mWindow.mNavigationBarColor;
            } else {
                navBarColor = navBarColor2;
            }
            i = 0;
            updateColorViewInt(this.mNavigationColorViewState, sysUiVisibility, navBarColor, this.mWindow.mNavigationBarDividerColor, navBarSize, navBarToRightEdge || navBarToLeftEdge, navBarToLeftEdge, 0, animate && !disallowAnimate, this.mForceWindowDrawsBarBackgrounds);
            boolean oldDrawLegacy = this.mDrawLegacyNavigationBarBackground;
            this.mDrawLegacyNavigationBarBackground = this.mNavigationColorViewState.visible && (this.mWindow.getAttributes().flags & Integer.MIN_VALUE) == 0;
            this.mDrawLegacyNavigationBarBackground &= ColorStatusBarController.getInstance().shouldDrawLegacyNavigationBarBackground();
            if (!(oldDrawLegacy == this.mDrawLegacyNavigationBarBackground || (vri = getViewRootImpl()) == null)) {
                vri.requestInvalidateRootRenderNode();
            }
            boolean statusBarNeedsRightInset = navBarToRightEdge && this.mNavigationColorViewState.present;
            boolean statusBarNeedsLeftInset = navBarToLeftEdge && this.mNavigationColorViewState.present;
            if (statusBarNeedsRightInset) {
                statusBarSideInset = this.mLastRightInset;
            } else {
                statusBarSideInset = statusBarNeedsLeftInset ? this.mLastLeftInset : 0;
            }
            updateColorViewInt(this.mStatusColorViewState, sysUiVisibility, calculateStatusBarColor(), 0, this.mLastTopInset, false, statusBarNeedsLeftInset, statusBarSideInset, animate && !disallowAnimate, this.mForceWindowDrawsBarBackgrounds);
        } else {
            i = 0;
        }
        int i2 = (sysUiVisibility & 2) != 0 ? 1 : i;
        int i3 = (!(this.mForceWindowDrawsBarBackgrounds && (attrs.flags & Integer.MIN_VALUE) == 0 && (sysUiVisibility & 512) == 0 && i2 == 0) && (!this.mLastShouldAlwaysConsumeSystemBars || i2 == 0)) ? i : 1;
        int i4 = (!((attrs.flags & Integer.MIN_VALUE) != 0 && (sysUiVisibility & 512) == 0 && i2 == 0) && i3 == 0) ? i : 1;
        int consumedTop = ((((sysUiVisibility & 1024) != 0 || (attrs.flags & 256) != 0 || (attrs.flags & 65536) != 0 || !this.mForceWindowDrawsBarBackgrounds || this.mLastTopInset == 0) && (!this.mLastShouldAlwaysConsumeSystemBars || (((sysUiVisibility & 4) != 0 || (attrs.flags & 1024) != 0) ? 1 : i) == 0)) ? i : 1) != 0 ? this.mLastTopInset : i;
        int consumedRight = i4 != 0 ? this.mLastRightInset : i;
        int consumedBottom = i4 != 0 ? this.mLastBottomInset : i;
        int consumedLeft = i4 != 0 ? this.mLastLeftInset : i;
        ViewGroup viewGroup = this.mContentRoot;
        if (viewGroup != null && (viewGroup.getLayoutParams() instanceof ViewGroup.MarginLayoutParams)) {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) this.mContentRoot.getLayoutParams();
            if (!(lp.topMargin == consumedTop && lp.rightMargin == consumedRight && lp.bottomMargin == consumedBottom && lp.leftMargin == consumedLeft)) {
                lp.topMargin = consumedTop;
                lp.rightMargin = consumedRight;
                lp.bottomMargin = consumedBottom;
                lp.leftMargin = consumedLeft;
                this.mContentRoot.setLayoutParams(lp);
                if (insets == null) {
                    requestApplyInsets();
                }
            }
            if (insets != null) {
                insets2 = insets.inset(consumedLeft, consumedTop, consumedRight, consumedBottom);
                if (i3 == 0) {
                    this.mBackgroundInsets = Insets.of(this.mLastLeftInset, i, this.mLastRightInset, this.mLastBottomInset);
                } else {
                    this.mBackgroundInsets = Insets.NONE;
                }
                updateBackgroundDrawable();
                if (insets2 == null) {
                    return insets2.consumeStableInsets();
                }
                return insets2;
            }
        }
        insets2 = insets;
        if (i3 == 0) {
        }
        updateBackgroundDrawable();
        if (insets2 == null) {
        }
    }

    private void updateBackgroundDrawable() {
        if (this.mBackgroundInsets == null) {
            this.mBackgroundInsets = Insets.NONE;
        }
        if (!this.mBackgroundInsets.equals(this.mLastBackgroundInsets) || this.mLastOriginalBackgroundDrawable != this.mOriginalBackgroundDrawable) {
            if (this.mOriginalBackgroundDrawable == null || this.mBackgroundInsets.equals(Insets.NONE)) {
                super.setBackgroundDrawable(this.mOriginalBackgroundDrawable);
            } else {
                super.setBackgroundDrawable(new InsetDrawable(this.mOriginalBackgroundDrawable, this.mBackgroundInsets.left, this.mBackgroundInsets.top, this.mBackgroundInsets.right, this.mBackgroundInsets.bottom) {
                    /* class com.android.internal.policy.DecorView.AnonymousClass2 */

                    @Override // android.graphics.drawable.InsetDrawable, android.graphics.drawable.Drawable, android.graphics.drawable.DrawableWrapper
                    public boolean getPadding(Rect padding) {
                        return getDrawable().getPadding(padding);
                    }
                });
            }
            this.mLastBackgroundInsets = this.mBackgroundInsets;
            this.mLastOriginalBackgroundDrawable = this.mOriginalBackgroundDrawable;
        }
    }

    @Override // android.view.View
    public Drawable getBackground() {
        return this.mOriginalBackgroundDrawable;
    }

    private int calculateStatusBarColor() {
        return calculateBarColor(this.mWindow.getAttributes().flags, 67108864, this.mSemiTransparentBarColor, this.mWindow.mStatusBarColor, getWindowSystemUiVisibility(), 8192, this.mWindow.mEnsureStatusBarContrastWhenTransparent);
    }

    private int calculateNavigationBarColor() {
        return calculateBarColor(this.mWindow.getAttributes().flags, 134217728, this.mSemiTransparentBarColor, this.mWindow.mNavigationBarColor, getWindowSystemUiVisibility(), 16, this.mWindow.mEnsureNavigationBarContrastWhenTransparent && (getContext().getResources().getBoolean(R.bool.config_navBarNeedsScrim) || ColorStatusBarController.getInstance().isSupportNavBarScrim(getContext())));
    }

    public static int calculateBarColor(int flags, int translucentFlag, int semiTransparentBarColor, int barColor, int sysuiVis, int lightSysuiFlag, boolean scrimTransparent) {
        if ((flags & translucentFlag) != 0) {
            return semiTransparentBarColor;
        }
        if ((Integer.MIN_VALUE & flags) == 0 && lightSysuiFlag == 8192) {
            return -16777216;
        }
        if (!scrimTransparent || Color.alpha(barColor) != 0) {
            return barColor;
        }
        return (sysuiVis & lightSysuiFlag) != 0 ? SCRIM_LIGHT : semiTransparentBarColor;
    }

    private int getCurrentColor(ColorViewState state) {
        if (state.visible) {
            return state.color;
        }
        return 0;
    }

    /* JADX INFO: Multiple debug info for r11v10 'vis'  int: [D('rightMargin' int), D('vis' int)] */
    /* JADX INFO: Multiple debug info for r13v7 'leftMargin'  int: [D('leftMargin' int), D('visibilityChanged' boolean)] */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00e8, code lost:
        if (r6.leftMargin != r13) goto L_0x00f8;
     */
    private void updateColorViewInt(final ColorViewState state, int sysUiVis, int color, int dividerColor, int size, boolean verticalBar, boolean seascape, int sideMargin, boolean animate, boolean force) {
        int resolvedGravity;
        boolean visibilityChanged;
        int leftMargin;
        int vis;
        state.present = state.attributes.isPresent(sysUiVis, this.mWindow.getAttributes().flags, force);
        boolean show = state.attributes.isVisible(state.present, color, this.mWindow.getAttributes().flags, force);
        boolean showView = show && !isResizing() && size > 0;
        boolean visibilityChanged2 = false;
        View view = state.view;
        int resolvedWidth = -1;
        int resolvedHeight = verticalBar ? -1 : size;
        if (verticalBar) {
            resolvedWidth = size;
        }
        if (verticalBar) {
            ColorViewAttributes colorViewAttributes = state.attributes;
            resolvedGravity = seascape ? colorViewAttributes.seascapeGravity : colorViewAttributes.horizontalGravity;
        } else {
            resolvedGravity = state.attributes.verticalGravity;
        }
        if (view != null) {
            int vis2 = showView ? 0 : 4;
            boolean visibilityChanged3 = state.targetVisibility != vis2;
            state.targetVisibility = vis2;
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) view.getLayoutParams();
            int rightMargin = seascape ? 0 : sideMargin;
            int leftMargin2 = seascape ? sideMargin : 0;
            if (lp.height == resolvedHeight && lp.width == resolvedWidth && lp.gravity == resolvedGravity) {
                vis = rightMargin;
                if (lp.rightMargin == vis) {
                    visibilityChanged = visibilityChanged3;
                    leftMargin = leftMargin2;
                } else {
                    visibilityChanged = visibilityChanged3;
                    leftMargin = leftMargin2;
                }
            } else {
                vis = rightMargin;
                visibilityChanged = visibilityChanged3;
                leftMargin = leftMargin2;
            }
            lp.height = resolvedHeight;
            lp.width = resolvedWidth;
            lp.gravity = resolvedGravity;
            lp.rightMargin = vis;
            lp.leftMargin = leftMargin;
            view.setLayoutParams(lp);
            if (showView) {
                setColor(view, color, dividerColor, verticalBar, seascape);
            }
            if (this.mWindow.getAttributes().type == 2011 && state == this.mNavigationColorViewState && view.getParent() == null) {
                addView(view, lp);
                updateColorViewTranslations();
            }
            visibilityChanged2 = visibilityChanged;
        } else if (showView) {
            View view2 = new View(this.mContext);
            view = view2;
            state.view = view2;
            setColor(view, color, dividerColor, verticalBar, seascape);
            view.setTransitionName(state.attributes.transitionName);
            view.setId(state.attributes.id);
            visibilityChanged2 = true;
            view.setVisibility(4);
            state.targetVisibility = 0;
            FrameLayout.LayoutParams lp2 = new FrameLayout.LayoutParams(resolvedWidth, resolvedHeight, resolvedGravity);
            if (seascape) {
                lp2.leftMargin = sideMargin;
            } else {
                lp2.rightMargin = sideMargin;
            }
            addView(view, lp2);
            updateColorViewTranslations();
        }
        if (visibilityChanged2) {
            view.animate().cancel();
            if (!animate || isResizing()) {
                int i = 0;
                view.setAlpha(1.0f);
                if (!showView) {
                    i = 4;
                }
                view.setVisibility(i);
            } else if (showView) {
                if (view.getVisibility() != 0) {
                    view.setVisibility(0);
                    view.setAlpha(0.0f);
                }
                view.animate().alpha(1.0f).setInterpolator(this.mShowInterpolator).setDuration((long) this.mBarEnterExitDuration);
            } else {
                view.animate().alpha(0.0f).setInterpolator(this.mHideInterpolator).setDuration((long) this.mBarEnterExitDuration).withEndAction(new Runnable() {
                    /* class com.android.internal.policy.DecorView.AnonymousClass3 */

                    public void run() {
                        state.view.setAlpha(1.0f);
                        state.view.setVisibility(4);
                    }
                });
            }
        }
        state.visible = show;
        state.color = color;
        if (DEBUG_OPPO_SYSTEMBAR) {
            Log.d(TAG, "window title:" + ((Object) this.mWindow.getAttributes().getTitle()) + " systemUiHideFlag:" + Integer.toHexString(state.attributes.systemUiHideFlag) + " showView:" + showView + " color:" + Integer.toHexString(color));
        }
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: android.graphics.drawable.InsetDrawable.<init>(android.graphics.drawable.Drawable, int, int, int, int):void
     arg types: [android.graphics.drawable.ColorDrawable, int, int, int, int]
     candidates:
      android.graphics.drawable.InsetDrawable.<init>(android.graphics.drawable.Drawable, float, float, float, float):void
      android.graphics.drawable.InsetDrawable.<init>(android.graphics.drawable.Drawable, int, int, int, int):void */
    private static void setColor(View v, int color, int dividerColor, boolean verticalBar, boolean seascape) {
        if (dividerColor != 0) {
            Pair<Boolean, Boolean> dir = (Pair) v.getTag();
            if (dir != null && dir.first.booleanValue() == verticalBar && dir.second.booleanValue() == seascape) {
                LayerDrawable d = (LayerDrawable) v.getBackground();
                ((ColorDrawable) ((InsetDrawable) d.getDrawable(1)).getDrawable()).setColor(color);
                ((ColorDrawable) d.getDrawable(0)).setColor(dividerColor);
                return;
            }
            int size = Math.round(TypedValue.applyDimension(1, 1.0f, v.getContext().getResources().getDisplayMetrics()));
            v.setBackground(new LayerDrawable(new Drawable[]{new ColorDrawable(dividerColor), new InsetDrawable((Drawable) new ColorDrawable(color), (!verticalBar || seascape) ? 0 : size, !verticalBar ? size : 0, (!verticalBar || !seascape) ? 0 : size, 0)}));
            v.setTag(new Pair(Boolean.valueOf(verticalBar), Boolean.valueOf(seascape)));
            return;
        }
        v.setTag(null);
        v.setBackgroundColor(color);
    }

    private void updateColorViewTranslations() {
        int rootScrollY = this.mRootScrollY;
        float f = 0.0f;
        if (this.mStatusColorViewState.view != null) {
            this.mStatusColorViewState.view.setTranslationY(rootScrollY > 0 ? (float) rootScrollY : 0.0f);
        }
        if (this.mNavigationColorViewState.view != null) {
            View view = this.mNavigationColorViewState.view;
            if (rootScrollY < 0) {
                f = (float) rootScrollY;
            }
            view.setTranslationY(f);
        }
    }

    private WindowInsets updateStatusGuard(WindowInsets insets) {
        boolean showStatusGuard;
        int i;
        WindowInsets insets2 = insets;
        ActionBarContextView actionBarContextView = this.mPrimaryActionModeView;
        if (actionBarContextView == null) {
            showStatusGuard = false;
            i = 0;
        } else if (actionBarContextView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) this.mPrimaryActionModeView.getLayoutParams();
            boolean mlpChanged = false;
            mlpChanged = false;
            if (this.mPrimaryActionModeView.isShown()) {
                if (this.mTempRect == null) {
                    this.mTempRect = new Rect();
                }
                WindowInsets innerInsets = this.mWindow.mContentParent.computeSystemWindowInsets(insets2, this.mTempRect);
                int newTopMargin = innerInsets.getSystemWindowInsetTop();
                int newLeftMargin = innerInsets.getSystemWindowInsetLeft();
                int newRightMargin = innerInsets.getSystemWindowInsetRight();
                WindowInsets rootInsets = getRootWindowInsets();
                int newGuardLeftMargin = rootInsets.getSystemWindowInsetLeft();
                int newGuardRightMargin = rootInsets.getSystemWindowInsetRight();
                if (!(mlp.topMargin == newTopMargin && mlp.leftMargin == newLeftMargin && mlp.rightMargin == newRightMargin)) {
                    mlpChanged = true;
                    mlp.topMargin = newTopMargin;
                    mlp.leftMargin = newLeftMargin;
                    mlp.rightMargin = newRightMargin;
                }
                if (newTopMargin <= 0 || this.mStatusGuard != null) {
                    View view = this.mStatusGuard;
                    if (view != null) {
                        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) view.getLayoutParams();
                        if (!(lp.height == mlp.topMargin && lp.leftMargin == newGuardLeftMargin && lp.rightMargin == newGuardRightMargin)) {
                            lp.height = mlp.topMargin;
                            lp.leftMargin = newGuardLeftMargin;
                            lp.rightMargin = newGuardRightMargin;
                            this.mStatusGuard.setLayoutParams(lp);
                        }
                    }
                } else {
                    this.mStatusGuard = new View(this.mContext);
                    this.mStatusGuard.setVisibility(8);
                    FrameLayout.LayoutParams lp2 = new FrameLayout.LayoutParams(-1, mlp.topMargin, 51);
                    lp2.leftMargin = newGuardLeftMargin;
                    lp2.rightMargin = newGuardRightMargin;
                    addView(this.mStatusGuard, indexOfChild(this.mStatusColorViewState.view), lp2);
                }
                boolean nonOverlay = true;
                boolean showStatusGuard2 = this.mStatusGuard != null;
                if (showStatusGuard2 && this.mStatusGuard.getVisibility() != 0) {
                    updateStatusGuardColor();
                }
                if ((this.mWindow.getLocalFeaturesPrivate() & 1024) != 0) {
                    nonOverlay = false;
                }
                if (nonOverlay && showStatusGuard2) {
                    insets2 = insets2.inset(0, insets.getSystemWindowInsetTop(), 0, 0);
                }
                showStatusGuard = showStatusGuard2;
                i = 0;
            } else {
                showStatusGuard = false;
                if (mlp.topMargin == 0 && mlp.leftMargin == 0 && mlp.rightMargin == 0) {
                    i = 0;
                } else {
                    mlpChanged = true;
                    i = 0;
                    mlp.topMargin = 0;
                }
            }
            if (mlpChanged) {
                this.mPrimaryActionModeView.setLayoutParams(mlp);
            }
        } else {
            showStatusGuard = false;
            i = 0;
        }
        View view2 = this.mStatusGuard;
        if (view2 != null) {
            if (!showStatusGuard) {
                i = 8;
            }
            view2.setVisibility(i);
        }
        return insets2;
    }

    @Override // android.view.OppoBaseView
    public void updateColorNavigationGuardColor(int color) {
        if (!this.mContext.getPackageManager().isClosedSuperFirewall()) {
            ColorStatusBarController.getInstance().updateColorNavigationGuardColor(getContext(), color, this.mWindow.getNavigationBarColor(), this);
            updateColorViews(null, false);
        }
    }

    private void updateStatusGuardColor() {
        int i;
        boolean lightStatusBar = (getWindowSystemUiVisibility() & 8192) != 0;
        View view = this.mStatusGuard;
        if (lightStatusBar) {
            i = this.mContext.getColor(R.color.decor_view_status_guard_light);
        } else {
            i = this.mContext.getColor(R.color.decor_view_status_guard);
        }
        view.setBackgroundColor(i);
    }

    public void updatePictureInPictureOutlineProvider(boolean isInPictureInPictureMode) {
        if (this.mIsInPictureInPictureMode != isInPictureInPictureMode) {
            if (isInPictureInPictureMode) {
                Window.WindowControllerCallback callback = this.mWindow.getWindowControllerCallback();
                if (callback != null && callback.isTaskRoot()) {
                    super.setOutlineProvider(PIP_OUTLINE_PROVIDER);
                }
            } else {
                ViewOutlineProvider outlineProvider = getOutlineProvider();
                ViewOutlineProvider viewOutlineProvider = this.mLastOutlineProvider;
                if (outlineProvider != viewOutlineProvider) {
                    setOutlineProvider(viewOutlineProvider);
                }
            }
            this.mIsInPictureInPictureMode = isInPictureInPictureMode;
        }
    }

    @Override // android.view.View
    public void setOutlineProvider(ViewOutlineProvider provider) {
        super.setOutlineProvider(provider);
        this.mLastOutlineProvider = provider;
    }

    private void drawableChanged() {
        if (!this.mChanging) {
            Rect framePadding = this.mFramePadding;
            if (framePadding == null) {
                framePadding = new Rect();
            }
            Rect backgroundPadding = this.mBackgroundPadding;
            if (backgroundPadding == null) {
                backgroundPadding = new Rect();
            }
            setPadding(framePadding.left + backgroundPadding.left, framePadding.top + backgroundPadding.top, framePadding.right + backgroundPadding.right, framePadding.bottom + backgroundPadding.bottom);
            requestLayout();
            invalidate();
            int opacity = -1;
            if (getResources().getConfiguration().windowConfiguration.hasWindowShadow()) {
                opacity = -3;
            } else {
                Drawable bg = getBackground();
                Drawable fg = getForeground();
                if (bg != null) {
                    if (fg == null) {
                        opacity = bg.getOpacity();
                    } else if (framePadding.left > 0 || framePadding.top > 0 || framePadding.right > 0 || framePadding.bottom > 0) {
                        opacity = -3;
                    } else {
                        int fop = fg.getOpacity();
                        int bop = bg.getOpacity();
                        if (fop == -1 || bop == -1) {
                            opacity = -1;
                        } else if (fop == 0) {
                            opacity = bop;
                        } else if (bop == 0) {
                            opacity = fop;
                        } else {
                            opacity = Drawable.resolveOpacity(fop, bop);
                        }
                    }
                }
            }
            this.mDefaultOpacity = opacity;
            if (this.mFeatureId < 0) {
                this.mWindow.setDefaultWindowFormat(opacity);
            }
        }
    }

    @Override // android.view.View
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (this.mWindow.hasFeature(0) && !hasWindowFocus && this.mWindow.mPanelChordingKey != 0) {
            this.mWindow.closePanel(0);
        }
        Window.Callback cb = this.mWindow.getCallback();
        if (cb != null && !this.mWindow.isDestroyed() && this.mFeatureId < 0) {
            cb.onWindowFocusChanged(hasWindowFocus);
        }
        ActionMode actionMode = this.mPrimaryActionMode;
        if (actionMode != null) {
            actionMode.onWindowFocusChanged(hasWindowFocus);
        }
        ActionMode actionMode2 = this.mFloatingActionMode;
        if (actionMode2 != null) {
            actionMode2.onWindowFocusChanged(hasWindowFocus);
        }
        updateElevation();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window.Callback cb = this.mWindow.getCallback();
        if (cb != null && !this.mWindow.isDestroyed() && this.mFeatureId < 0) {
            cb.onAttachedToWindow();
        }
        if (this.mFeatureId == -1) {
            this.mWindow.openPanelsAfterRestore();
        }
        if (!this.mWindowResizeCallbacksAdded) {
            getViewRootImpl().addWindowCallbacks(this);
            this.mWindowResizeCallbacksAdded = true;
        } else {
            BackdropFrameRenderer backdropFrameRenderer = this.mBackdropFrameRenderer;
            if (backdropFrameRenderer != null) {
                backdropFrameRenderer.onConfigurationChange();
            }
        }
        this.mWindow.onViewRootImplSet(getViewRootImpl());
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Window.Callback cb = this.mWindow.getCallback();
        if (cb != null && this.mFeatureId < 0) {
            cb.onDetachedFromWindow();
        }
        if (this.mWindow.mDecorContentParent != null) {
            this.mWindow.mDecorContentParent.dismissPopups();
        }
        if (this.mPrimaryActionModePopup != null) {
            removeCallbacks(this.mShowPrimaryActionModePopup);
            if (this.mPrimaryActionModePopup.isShowing()) {
                this.mPrimaryActionModePopup.dismiss();
            }
            this.mPrimaryActionModePopup = null;
        }
        FloatingToolbar floatingToolbar = this.mFloatingToolbar;
        if (floatingToolbar != null) {
            floatingToolbar.dismiss();
            this.mFloatingToolbar = null;
        }
        PhoneWindow.PanelFeatureState st = this.mWindow.getPanelState(0, false);
        if (!(st == null || st.menu == null || this.mFeatureId >= 0)) {
            st.menu.close();
        }
        releaseThreadedRenderer();
        if (this.mWindowResizeCallbacksAdded) {
            getViewRootImpl().removeWindowCallbacks(this);
            this.mWindowResizeCallbacksAdded = false;
        }
    }

    @Override // android.view.View
    public void onCloseSystemDialogs(String reason) {
        if (this.mFeatureId >= 0) {
            this.mWindow.closeAllPanels();
        }
    }

    @Override // com.android.internal.view.RootViewSurfaceTaker
    public SurfaceHolder.Callback2 willYouTakeTheSurface() {
        if (this.mFeatureId < 0) {
            return this.mWindow.mTakeSurfaceCallback;
        }
        return null;
    }

    @Override // com.android.internal.view.RootViewSurfaceTaker
    public InputQueue.Callback willYouTakeTheInputQueue() {
        if (this.mFeatureId < 0) {
            return this.mWindow.mTakeInputQueueCallback;
        }
        return null;
    }

    @Override // com.android.internal.view.RootViewSurfaceTaker
    public void setSurfaceType(int type) {
        this.mWindow.setType(type);
    }

    @Override // com.android.internal.view.RootViewSurfaceTaker
    public void setSurfaceFormat(int format) {
        this.mWindow.setFormat(format);
    }

    @Override // com.android.internal.view.RootViewSurfaceTaker
    public void setSurfaceKeepScreenOn(boolean keepOn) {
        if (keepOn) {
            this.mWindow.addFlags(128);
        } else {
            this.mWindow.clearFlags(128);
        }
    }

    @Override // com.android.internal.view.RootViewSurfaceTaker
    public void onRootViewScrollYChanged(int rootScrollY) {
        this.mRootScrollY = rootScrollY;
        DecorCaptionView decorCaptionView = this.mDecorCaptionView;
        if (decorCaptionView != null) {
            decorCaptionView.onRootViewScrollYChanged(rootScrollY);
        }
        updateColorViewTranslations();
    }

    @Override // android.view.View
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (ViewDebugManager.DEBUG_USER) {
            Log.v("PhoneWindow", "DecorView setVisiblity: visibility = " + visibility + ", Parent = " + getParent() + ", this = " + this);
        }
    }

    private ActionMode createActionMode(int type, ActionMode.Callback2 callback, View originatingView) {
        if (type != 1) {
            return createStandaloneActionMode(callback);
        }
        return createFloatingActionMode(originatingView, callback);
    }

    private void setHandledActionMode(ActionMode mode) {
        if (mode.getType() == 0) {
            setHandledPrimaryActionMode(mode);
        } else if (mode.getType() == 1) {
            setHandledFloatingActionMode(mode);
        }
    }

    private ActionMode createStandaloneActionMode(ActionMode.Callback callback) {
        Context actionBarContext;
        endOnGoingFadeAnimation();
        cleanupPrimaryActionMode();
        ActionBarContextView actionBarContextView = this.mPrimaryActionModeView;
        boolean z = false;
        if (actionBarContextView == null || !actionBarContextView.isAttachedToWindow()) {
            if (this.mWindow.isFloating()) {
                TypedValue outValue = new TypedValue();
                Resources.Theme baseTheme = this.mContext.getTheme();
                baseTheme.resolveAttribute(16843825, outValue, true);
                if (outValue.resourceId != 0) {
                    Resources.Theme actionBarTheme = this.mContext.getResources().newTheme();
                    actionBarTheme.setTo(baseTheme);
                    actionBarTheme.applyStyle(outValue.resourceId, true);
                    actionBarContext = new ContextThemeWrapper(this.mContext, 0);
                    actionBarContext.getTheme().setTo(actionBarTheme);
                } else {
                    actionBarContext = this.mContext;
                }
                this.mPrimaryActionModeView = new ActionBarContextView(actionBarContext);
                this.mPrimaryActionModePopup = new PopupWindow(actionBarContext, (AttributeSet) null, (int) R.attr.actionModePopupWindowStyle);
                this.mPrimaryActionModePopup.setWindowLayoutType(2);
                this.mPrimaryActionModePopup.setContentView(this.mPrimaryActionModeView);
                this.mPrimaryActionModePopup.setWidth(-1);
                actionBarContext.getTheme().resolveAttribute(16843499, outValue, true);
                this.mPrimaryActionModeView.setContentHeight(TypedValue.complexToDimensionPixelSize(outValue.data, actionBarContext.getResources().getDisplayMetrics()));
                this.mPrimaryActionModePopup.setHeight(-2);
                this.mShowPrimaryActionModePopup = new Runnable() {
                    /* class com.android.internal.policy.DecorView.AnonymousClass4 */

                    public void run() {
                        DecorView.this.mPrimaryActionModePopup.showAtLocation(DecorView.this.mPrimaryActionModeView.getApplicationWindowToken(), 55, 0, 0);
                        DecorView.this.endOnGoingFadeAnimation();
                        if (DecorView.this.shouldAnimatePrimaryActionModeView()) {
                            DecorView decorView = DecorView.this;
                            ObjectAnimator unused = decorView.mFadeAnim = ObjectAnimator.ofFloat(decorView.mPrimaryActionModeView, View.ALPHA, 0.0f, 1.0f);
                            DecorView.this.mFadeAnim.addListener(new AnimatorListenerAdapter() {
                                /* class com.android.internal.policy.DecorView.AnonymousClass4.AnonymousClass1 */

                                @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
                                public void onAnimationStart(Animator animation) {
                                    DecorView.this.mPrimaryActionModeView.setVisibility(0);
                                }

                                @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
                                public void onAnimationEnd(Animator animation) {
                                    DecorView.this.mPrimaryActionModeView.setAlpha(1.0f);
                                    ObjectAnimator unused = DecorView.this.mFadeAnim = null;
                                }
                            });
                            DecorView.this.mFadeAnim.start();
                            return;
                        }
                        DecorView.this.mPrimaryActionModeView.setAlpha(1.0f);
                        DecorView.this.mPrimaryActionModeView.setVisibility(0);
                    }
                };
            } else {
                ViewStub stub = (ViewStub) findViewById(R.id.action_mode_bar_stub);
                if (stub != null) {
                    this.mPrimaryActionModeView = (ActionBarContextView) stub.inflate();
                    this.mPrimaryActionModePopup = null;
                }
            }
        }
        ActionBarContextView actionBarContextView2 = this.mPrimaryActionModeView;
        if (actionBarContextView2 == null) {
            return null;
        }
        actionBarContextView2.killMode();
        Context context = this.mPrimaryActionModeView.getContext();
        ActionBarContextView actionBarContextView3 = this.mPrimaryActionModeView;
        if (this.mPrimaryActionModePopup == null) {
            z = true;
        }
        return new StandaloneActionMode(context, actionBarContextView3, callback, z);
    }

    /* access modifiers changed from: private */
    public void endOnGoingFadeAnimation() {
        ObjectAnimator objectAnimator = this.mFadeAnim;
        if (objectAnimator != null) {
            objectAnimator.end();
        }
    }

    private void setHandledPrimaryActionMode(ActionMode mode) {
        endOnGoingFadeAnimation();
        this.mPrimaryActionMode = mode;
        this.mPrimaryActionMode.invalidate();
        this.mPrimaryActionModeView.initForMode(this.mPrimaryActionMode);
        if (this.mPrimaryActionModePopup != null) {
            post(this.mShowPrimaryActionModePopup);
        } else if (shouldAnimatePrimaryActionModeView()) {
            this.mFadeAnim = ObjectAnimator.ofFloat(this.mPrimaryActionModeView, View.ALPHA, 0.0f, 1.0f);
            if (isOppoStyle()) {
                this.mFadeAnim.setDuration(100L);
            }
            this.mFadeAnim.addListener(new AnimatorListenerAdapter() {
                /* class com.android.internal.policy.DecorView.AnonymousClass5 */

                @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
                public void onAnimationStart(Animator animation) {
                    DecorView.this.mPrimaryActionModeView.setVisibility(0);
                }

                @Override // android.animation.Animator.AnimatorListener, android.animation.AnimatorListenerAdapter
                public void onAnimationEnd(Animator animation) {
                    DecorView.this.mPrimaryActionModeView.setAlpha(1.0f);
                    ObjectAnimator unused = DecorView.this.mFadeAnim = null;
                }
            });
            this.mFadeAnim.start();
        } else {
            this.mPrimaryActionModeView.setAlpha(1.0f);
            this.mPrimaryActionModeView.setVisibility(0);
        }
        this.mPrimaryActionModeView.sendAccessibilityEvent(32);
    }

    /* access modifiers changed from: package-private */
    public boolean shouldAnimatePrimaryActionModeView() {
        return isLaidOut();
    }

    private ActionMode createFloatingActionMode(View originatingView, ActionMode.Callback2 callback) {
        ActionMode actionMode = this.mFloatingActionMode;
        if (actionMode != null) {
            actionMode.finish();
        }
        cleanupFloatingActionModeViews();
        this.mFloatingToolbar = new FloatingToolbar(this.mWindow);
        final FloatingActionMode mode = new FloatingActionMode(this.mContext, callback, originatingView, this.mFloatingToolbar);
        this.mFloatingActionModeOriginatingView = originatingView;
        this.mFloatingToolbarPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
            /* class com.android.internal.policy.DecorView.AnonymousClass6 */

            @Override // android.view.ViewTreeObserver.OnPreDrawListener
            public boolean onPreDraw() {
                mode.updateViewLocationInWindow();
                return true;
            }
        };
        return mode;
    }

    private void setHandledFloatingActionMode(ActionMode mode) {
        this.mFloatingActionMode = mode;
        this.mFloatingActionMode.invalidate();
        this.mFloatingActionModeOriginatingView.getViewTreeObserver().addOnPreDrawListener(this.mFloatingToolbarPreDrawListener);
    }

    /* access modifiers changed from: package-private */
    public void enableCaption(boolean attachedAndVisible) {
        if (this.mHasCaption != attachedAndVisible) {
            this.mHasCaption = attachedAndVisible;
            if (getForeground() != null) {
                drawableChanged();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setWindow(PhoneWindow phoneWindow) {
        this.mWindow = phoneWindow;
        Context context = getContext();
        if (context instanceof DecorContext) {
            ((DecorContext) context).setPhoneWindow(this.mWindow);
        }
    }

    @Override // android.view.View
    public Resources getResources() {
        return getContext().getResources();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateDecorCaptionStatus(newConfig);
        updateAvailableWidth();
        initializeElevation();
    }

    @Override // android.view.View
    public void onMovedToDisplay(int displayId, Configuration config) {
        super.onMovedToDisplay(displayId, config);
        getContext().updateDisplay(displayId);
    }

    private boolean isFillingScreen(Configuration config) {
        return (config.windowConfiguration.getWindowingMode() == 1) && ((getWindowSystemUiVisibility() | getSystemUiVisibility()) & 4) != 0;
    }

    private void updateDecorCaptionStatus(Configuration config) {
        boolean displayWindowDecor = config.windowConfiguration.hasWindowDecorCaption() && !isFillingScreen(config);
        if (this.mDecorCaptionView != null || !displayWindowDecor) {
            DecorCaptionView decorCaptionView = this.mDecorCaptionView;
            if (decorCaptionView != null) {
                decorCaptionView.onConfigurationChanged(displayWindowDecor);
                enableCaption(displayWindowDecor);
                return;
            }
            return;
        }
        this.mDecorCaptionView = createDecorCaptionView(this.mWindow.getLayoutInflater());
        DecorCaptionView decorCaptionView2 = this.mDecorCaptionView;
        if (decorCaptionView2 != null) {
            if (decorCaptionView2.getParent() == null) {
                addView(this.mDecorCaptionView, 0, new ViewGroup.LayoutParams(-1, -1));
            }
            removeView(this.mContentRoot);
            this.mDecorCaptionView.addView(this.mContentRoot, new ViewGroup.MarginLayoutParams(-1, -1));
        }
    }

    /* access modifiers changed from: package-private */
    public void onResourcesLoaded(LayoutInflater inflater, int layoutResource) {
        if (this.mBackdropFrameRenderer != null) {
            loadBackgroundDrawablesIfNeeded();
            this.mBackdropFrameRenderer.onResourcesLoaded(this, this.mResizingBackgroundDrawable, this.mCaptionBackgroundDrawable, this.mUserCaptionBackgroundDrawable, getCurrentColor(this.mStatusColorViewState), getCurrentColor(this.mNavigationColorViewState));
        }
        this.mDecorCaptionView = createDecorCaptionView(inflater);
        View root = inflater.inflate(layoutResource, (ViewGroup) null);
        DecorCaptionView decorCaptionView = this.mDecorCaptionView;
        if (decorCaptionView != null) {
            if (decorCaptionView.getParent() == null) {
                addView(this.mDecorCaptionView, new ViewGroup.LayoutParams(-1, -1));
            }
            this.mDecorCaptionView.addView(root, new ViewGroup.MarginLayoutParams(-1, -1));
        } else {
            addView(root, 0, new ViewGroup.LayoutParams(-1, -1));
        }
        this.mContentRoot = (ViewGroup) root;
        initializeElevation();
    }

    private void loadBackgroundDrawablesIfNeeded() {
        if (this.mResizingBackgroundDrawable == null) {
            this.mResizingBackgroundDrawable = getResizingBackgroundDrawable(this.mWindow.mBackgroundDrawable, this.mWindow.mBackgroundFallbackDrawable, this.mWindow.isTranslucent() || this.mWindow.isShowingWallpaper());
            if (this.mResizingBackgroundDrawable == null) {
                String str = this.mLogTag;
                Log.w(str, "Failed to find background drawable for PhoneWindow=" + this.mWindow);
            }
        }
        if (this.mCaptionBackgroundDrawable == null) {
            this.mCaptionBackgroundDrawable = getContext().getDrawable(R.drawable.decor_caption_title_focused);
        }
        Drawable drawable = this.mResizingBackgroundDrawable;
        if (drawable != null) {
            this.mLastBackgroundDrawableCb = drawable.getCallback();
            this.mResizingBackgroundDrawable.setCallback(null);
        }
    }

    private DecorCaptionView createDecorCaptionView(LayoutInflater inflater) {
        DecorCaptionView decorCaptionView = null;
        boolean z = true;
        for (int i = getChildCount() - 1; i >= 0 && decorCaptionView == null; i--) {
            View view = getChildAt(i);
            if (view instanceof DecorCaptionView) {
                decorCaptionView = (DecorCaptionView) view;
                removeViewAt(i);
            }
        }
        WindowManager.LayoutParams attrs = this.mWindow.getAttributes();
        boolean isApplication = attrs.type == 1 || attrs.type == 2 || attrs.type == 4;
        WindowConfiguration winConfig = getResources().getConfiguration().windowConfiguration;
        if (this.mWindow.isFloating() || !isApplication || !winConfig.hasWindowDecorCaption()) {
            decorCaptionView = null;
        } else {
            if (decorCaptionView == null) {
                decorCaptionView = inflateDecorCaptionView(inflater);
            }
            decorCaptionView.setPhoneWindow(this.mWindow, true);
        }
        if (decorCaptionView == null) {
            z = false;
        }
        enableCaption(z);
        return decorCaptionView;
    }

    private DecorCaptionView inflateDecorCaptionView(LayoutInflater inflater) {
        Context context = getContext();
        DecorCaptionView view = (DecorCaptionView) LayoutInflater.from(context).inflate(R.layout.decor_caption, (ViewGroup) null);
        setDecorCaptionShade(context, view);
        return view;
    }

    private void setDecorCaptionShade(Context context, DecorCaptionView view) {
        int shade = this.mWindow.getDecorCaptionShade();
        if (shade == 1) {
            setLightDecorCaptionShade(view);
        } else if (shade != 2) {
            TypedValue value = new TypedValue();
            context.getTheme().resolveAttribute(16843827, value, true);
            if (((double) Color.luminance(value.data)) < 0.5d) {
                setLightDecorCaptionShade(view);
            } else {
                setDarkDecorCaptionShade(view);
            }
        } else {
            setDarkDecorCaptionShade(view);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateDecorCaptionShade() {
        if (this.mDecorCaptionView != null) {
            setDecorCaptionShade(getContext(), this.mDecorCaptionView);
        }
    }

    private void setLightDecorCaptionShade(DecorCaptionView view) {
        view.findViewById(R.id.maximize_window).setBackgroundResource(R.drawable.decor_maximize_button_light);
        view.findViewById(R.id.close_window).setBackgroundResource(R.drawable.decor_close_button_light);
    }

    private void setDarkDecorCaptionShade(DecorCaptionView view) {
        view.findViewById(R.id.maximize_window).setBackgroundResource(R.drawable.decor_maximize_button_dark);
        view.findViewById(R.id.close_window).setBackgroundResource(R.drawable.decor_close_button_dark);
    }

    public static Drawable getResizingBackgroundDrawable(Drawable backgroundDrawable, Drawable fallbackDrawable, boolean windowTranslucent) {
        if (backgroundDrawable != null) {
            return enforceNonTranslucentBackground(backgroundDrawable, windowTranslucent);
        }
        if (fallbackDrawable != null) {
            return enforceNonTranslucentBackground(fallbackDrawable, windowTranslucent);
        }
        return new ColorDrawable(-16777216);
    }

    private static Drawable enforceNonTranslucentBackground(Drawable drawable, boolean windowTranslucent) {
        if (!windowTranslucent && (drawable instanceof ColorDrawable)) {
            ColorDrawable colorDrawable = (ColorDrawable) drawable;
            int color = colorDrawable.getColor();
            if (Color.alpha(color) != 255) {
                ColorDrawable copy = (ColorDrawable) colorDrawable.getConstantState().newDrawable().mutate();
                copy.setColor(Color.argb(255, Color.red(color), Color.green(color), Color.blue(color)));
                return copy;
            }
        }
        return drawable;
    }

    /* access modifiers changed from: package-private */
    public void clearContentView() {
        DecorCaptionView decorCaptionView = this.mDecorCaptionView;
        if (decorCaptionView != null) {
            decorCaptionView.removeContentView();
            return;
        }
        for (int i = getChildCount() - 1; i >= 0; i--) {
            View v = getChildAt(i);
            if (!(v == this.mStatusColorViewState.view || v == this.mNavigationColorViewState.view || v == this.mStatusGuard)) {
                removeViewAt(i);
            }
        }
    }

    @Override // android.view.WindowCallbacks
    public void onWindowSizeIsChanging(Rect newBounds, boolean fullscreen, Rect systemInsets, Rect stableInsets) {
        BackdropFrameRenderer backdropFrameRenderer = this.mBackdropFrameRenderer;
        if (backdropFrameRenderer != null) {
            backdropFrameRenderer.setTargetRect(newBounds, fullscreen, systemInsets, stableInsets);
        }
    }

    @Override // android.view.WindowCallbacks
    public void onWindowDragResizeStart(Rect initialBounds, boolean fullscreen, Rect systemInsets, Rect stableInsets, int resizeMode) {
        if (this.mWindow.isDestroyed()) {
            releaseThreadedRenderer();
        } else if (this.mBackdropFrameRenderer == null) {
            ThreadedRenderer renderer = getThreadedRenderer();
            if (renderer != null) {
                loadBackgroundDrawablesIfNeeded();
                this.mBackdropFrameRenderer = new BackdropFrameRenderer(this, renderer, initialBounds, this.mResizingBackgroundDrawable, this.mCaptionBackgroundDrawable, this.mUserCaptionBackgroundDrawable, getCurrentColor(this.mStatusColorViewState), getCurrentColor(this.mNavigationColorViewState), fullscreen, systemInsets, stableInsets);
                updateElevation();
                updateColorViews(null, false);
            }
            this.mResizeMode = resizeMode;
            getViewRootImpl().requestInvalidateRootRenderNode();
        }
    }

    @Override // android.view.WindowCallbacks
    public void onWindowDragResizeEnd() {
        releaseThreadedRenderer();
        updateColorViews(null, false);
        this.mResizeMode = -1;
        getViewRootImpl().requestInvalidateRootRenderNode();
    }

    @Override // android.view.WindowCallbacks
    public boolean onContentDrawn(int offsetX, int offsetY, int sizeX, int sizeY) {
        BackdropFrameRenderer backdropFrameRenderer = this.mBackdropFrameRenderer;
        if (backdropFrameRenderer == null) {
            return false;
        }
        return backdropFrameRenderer.onContentDrawn(offsetX, offsetY, sizeX, sizeY);
    }

    @Override // android.view.WindowCallbacks
    public void onRequestDraw(boolean reportNextDraw) {
        BackdropFrameRenderer backdropFrameRenderer = this.mBackdropFrameRenderer;
        if (backdropFrameRenderer != null) {
            backdropFrameRenderer.onRequestDraw(reportNextDraw);
        } else if (reportNextDraw && isAttachedToWindow()) {
            getViewRootImpl().reportDrawFinish();
        }
    }

    @Override // android.view.WindowCallbacks
    public void onPostDraw(RecordingCanvas canvas) {
        drawResizingShadowIfNeeded(canvas);
        drawLegacyNavigationBarBackground(canvas);
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: android.graphics.LinearGradient.<init>(float, float, float, float, int[], float[], android.graphics.Shader$TileMode):void
     arg types: [int, int, int, float, int[], float[], android.graphics.Shader$TileMode]
     candidates:
      android.graphics.LinearGradient.<init>(float, float, float, float, int, int, android.graphics.Shader$TileMode):void
      android.graphics.LinearGradient.<init>(float, float, float, float, long, long, android.graphics.Shader$TileMode):void
      android.graphics.LinearGradient.<init>(float, float, float, float, long[], float[], android.graphics.Shader$TileMode):void
      android.graphics.LinearGradient.<init>(float, float, float, float, int[], float[], android.graphics.Shader$TileMode):void */
    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: android.graphics.LinearGradient.<init>(float, float, float, float, int[], float[], android.graphics.Shader$TileMode):void
     arg types: [int, int, float, int, int[], float[], android.graphics.Shader$TileMode]
     candidates:
      android.graphics.LinearGradient.<init>(float, float, float, float, int, int, android.graphics.Shader$TileMode):void
      android.graphics.LinearGradient.<init>(float, float, float, float, long, long, android.graphics.Shader$TileMode):void
      android.graphics.LinearGradient.<init>(float, float, float, float, long[], float[], android.graphics.Shader$TileMode):void
      android.graphics.LinearGradient.<init>(float, float, float, float, int[], float[], android.graphics.Shader$TileMode):void */
    private void initResizingPaints() {
        int startColor = this.mContext.getResources().getColor(R.color.resize_shadow_start_color, null);
        int endColor = this.mContext.getResources().getColor(R.color.resize_shadow_end_color, null);
        int middleColor = (startColor + endColor) / 2;
        this.mHorizontalResizeShadowPaint.setShader(new LinearGradient(0.0f, 0.0f, 0.0f, (float) this.mResizeShadowSize, new int[]{startColor, middleColor, endColor}, new float[]{0.0f, 0.3f, 1.0f}, Shader.TileMode.CLAMP));
        this.mVerticalResizeShadowPaint.setShader(new LinearGradient(0.0f, 0.0f, (float) this.mResizeShadowSize, 0.0f, new int[]{startColor, middleColor, endColor}, new float[]{0.0f, 0.3f, 1.0f}, Shader.TileMode.CLAMP));
    }

    private void drawResizingShadowIfNeeded(RecordingCanvas canvas) {
        if (this.mResizeMode == 1 && !this.mWindow.mIsFloating && !this.mWindow.isTranslucent() && !this.mWindow.isShowingWallpaper()) {
            canvas.save();
            canvas.translate(0.0f, (float) (getHeight() - this.mFrameOffsets.bottom));
            canvas.drawRect(0.0f, 0.0f, (float) getWidth(), (float) this.mResizeShadowSize, this.mHorizontalResizeShadowPaint);
            canvas.restore();
            canvas.save();
            canvas.translate((float) (getWidth() - this.mFrameOffsets.right), 0.0f);
            canvas.drawRect(0.0f, 0.0f, (float) this.mResizeShadowSize, (float) getHeight(), this.mVerticalResizeShadowPaint);
            canvas.restore();
        }
    }

    private void drawLegacyNavigationBarBackground(RecordingCanvas canvas) {
        View v;
        if (this.mDrawLegacyNavigationBarBackground && (v = this.mNavigationColorViewState.view) != null) {
            canvas.drawRect((float) v.getLeft(), (float) v.getTop(), (float) v.getRight(), (float) v.getBottom(), this.mLegacyNavigationBarBackgroundPaint);
        }
    }

    private void releaseThreadedRenderer() {
        Drawable.Callback callback;
        Drawable drawable = this.mResizingBackgroundDrawable;
        if (!(drawable == null || (callback = this.mLastBackgroundDrawableCb) == null)) {
            drawable.setCallback(callback);
            this.mLastBackgroundDrawableCb = null;
        }
        BackdropFrameRenderer backdropFrameRenderer = this.mBackdropFrameRenderer;
        if (backdropFrameRenderer != null) {
            backdropFrameRenderer.releaseRenderer();
            this.mBackdropFrameRenderer = null;
            updateElevation();
        }
    }

    private boolean isResizing() {
        return this.mBackdropFrameRenderer != null;
    }

    private void initializeElevation() {
        this.mAllowUpdateElevation = false;
        updateElevation();
    }

    private void updateElevation() {
        float elevation = 0.0f;
        boolean wasAdjustedForStack = this.mElevationAdjustedForStack;
        int windowingMode = getResources().getConfiguration().windowConfiguration.getWindowingMode();
        float f = 5.0f;
        if (windowingMode == 5 && !isResizing()) {
            if (hasWindowFocus()) {
                f = 20.0f;
            }
            float elevation2 = f;
            if (!this.mAllowUpdateElevation) {
                elevation2 = 20.0f;
            }
            elevation = dipToPx(elevation2);
            this.mElevationAdjustedForStack = true;
        } else if (windowingMode == 2) {
            elevation = dipToPx(5.0f);
            this.mElevationAdjustedForStack = true;
        } else {
            this.mElevationAdjustedForStack = false;
        }
        if ((!wasAdjustedForStack && !this.mElevationAdjustedForStack) || getElevation() == elevation) {
            return;
        }
        if (!isResizing()) {
            this.mWindow.setElevation(elevation);
        } else {
            setElevation(elevation);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isShowingCaption() {
        DecorCaptionView decorCaptionView = this.mDecorCaptionView;
        return decorCaptionView != null && decorCaptionView.isCaptionShowing();
    }

    /* access modifiers changed from: package-private */
    public int getCaptionHeight() {
        if (isShowingCaption()) {
            return this.mDecorCaptionView.getCaptionHeight();
        }
        return 0;
    }

    private float dipToPx(float dip) {
        return TypedValue.applyDimension(1, dip, getResources().getDisplayMetrics());
    }

    /* access modifiers changed from: package-private */
    public void setUserCaptionBackgroundDrawable(Drawable drawable) {
        this.mUserCaptionBackgroundDrawable = drawable;
        BackdropFrameRenderer backdropFrameRenderer = this.mBackdropFrameRenderer;
        if (backdropFrameRenderer != null) {
            backdropFrameRenderer.setUserCaptionBackgroundDrawable(drawable);
        }
    }

    private static String getTitleSuffix(WindowManager.LayoutParams params) {
        if (params == null) {
            return "";
        }
        String[] split = params.getTitle().toString().split("\\.");
        if (split.length > 0) {
            return split[split.length - 1];
        }
        return "";
    }

    /* access modifiers changed from: package-private */
    public void updateLogTag(WindowManager.LayoutParams params) {
        this.mLogTag = "DecorView[" + getTitleSuffix(params) + "]";
    }

    private void updateAvailableWidth() {
        Resources res = getResources();
        this.mAvailableWidth = TypedValue.applyDimension(1, (float) res.getConfiguration().screenWidthDp, res.getDisplayMetrics());
    }

    @Override // android.view.View
    public void requestKeyboardShortcuts(List<KeyboardShortcutGroup> list, int deviceId) {
        PhoneWindow.PanelFeatureState st = this.mWindow.getPanelState(0, false);
        Menu menu = st != null ? st.menu : null;
        if (!this.mWindow.isDestroyed() && this.mWindow.getCallback() != null) {
            this.mWindow.getCallback().onProvideKeyboardShortcuts(list, menu, deviceId);
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    public void dispatchPointerCaptureChanged(boolean hasCapture) {
        super.dispatchPointerCaptureChanged(hasCapture);
        if (!this.mWindow.isDestroyed() && this.mWindow.getCallback() != null) {
            this.mWindow.getCallback().onPointerCaptureChanged(hasCapture);
        }
    }

    @Override // android.view.View
    public int getAccessibilityViewId() {
        return 2147483646;
    }

    @Override // android.view.View
    public String toString() {
        if (this.mWindow == null) {
            return "DecorView@" + Integer.toHexString(hashCode()) + "[null]";
        }
        return "DecorView@" + Integer.toHexString(hashCode()) + "[" + getTitleSuffix(this.mWindow.getAttributes()) + "]";
    }

    private static class ColorViewState {
        final ColorViewAttributes attributes;
        int color;
        boolean present = false;
        int targetVisibility = 4;
        View view = null;
        boolean visible;

        ColorViewState(ColorViewAttributes attributes2) {
            this.attributes = attributes2;
        }
    }

    public static class ColorViewAttributes {
        final int hideWindowFlag;
        final int horizontalGravity;
        final int id;
        final int seascapeGravity;
        final int systemUiHideFlag;
        final String transitionName;
        final int translucentFlag;
        final int verticalGravity;

        private ColorViewAttributes(int systemUiHideFlag2, int translucentFlag2, int verticalGravity2, int horizontalGravity2, int seascapeGravity2, String transitionName2, int id2, int hideWindowFlag2) {
            this.id = id2;
            this.systemUiHideFlag = systemUiHideFlag2;
            this.translucentFlag = translucentFlag2;
            this.verticalGravity = verticalGravity2;
            this.horizontalGravity = horizontalGravity2;
            this.seascapeGravity = seascapeGravity2;
            this.transitionName = transitionName2;
            this.hideWindowFlag = hideWindowFlag2;
        }

        public boolean isPresent(int sysUiVis, int windowFlags, boolean force) {
            return (this.systemUiHideFlag & sysUiVis) == 0 && (this.hideWindowFlag & windowFlags) == 0 && ((Integer.MIN_VALUE & windowFlags) != 0 || force);
        }

        public boolean isVisible(boolean present, int color, int windowFlags, boolean force) {
            return present && (-16777216 & color) != 0 && ((this.translucentFlag & windowFlags) == 0 || force);
        }

        public boolean isVisible(int sysUiVis, int color, int windowFlags, boolean force) {
            return isVisible(isPresent(sysUiVis, windowFlags, force), color, windowFlags, force);
        }
    }

    private class ActionModeCallback2Wrapper extends ActionMode.Callback2 {
        private final ActionMode.Callback mWrapped;

        public ActionModeCallback2Wrapper(ActionMode.Callback wrapped) {
            this.mWrapped = wrapped;
        }

        @Override // android.view.ActionMode.Callback
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return this.mWrapped.onCreateActionMode(mode, menu);
        }

        @Override // android.view.ActionMode.Callback
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            DecorView.this.requestFitSystemWindows();
            return this.mWrapped.onPrepareActionMode(mode, menu);
        }

        @Override // android.view.ActionMode.Callback
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return this.mWrapped.onActionItemClicked(mode, item);
        }

        @Override // android.view.ActionMode.Callback
        public void onDestroyActionMode(ActionMode mode) {
            boolean isPrimary;
            this.mWrapped.onDestroyActionMode(mode);
            boolean isFloating = false;
            if (DecorView.this.mContext.getApplicationInfo().targetSdkVersion >= 23) {
                isPrimary = mode == DecorView.this.mPrimaryActionMode;
                if (mode == DecorView.this.mFloatingActionMode) {
                    isFloating = true;
                }
                if (!isPrimary && mode.getType() == 0) {
                    Log.e(DecorView.this.mLogTag, "Destroying unexpected ActionMode instance of TYPE_PRIMARY; " + mode + " was not the current primary action mode! Expected " + DecorView.this.mPrimaryActionMode);
                }
                if (!isFloating && mode.getType() == 1) {
                    Log.e(DecorView.this.mLogTag, "Destroying unexpected ActionMode instance of TYPE_FLOATING; " + mode + " was not the current floating action mode! Expected " + DecorView.this.mFloatingActionMode);
                }
            } else {
                isPrimary = mode.getType() == 0;
                if (mode.getType() == 1) {
                    isFloating = true;
                }
            }
            if (isPrimary) {
                if (DecorView.this.mPrimaryActionModePopup != null) {
                    DecorView decorView = DecorView.this;
                    decorView.removeCallbacks(decorView.mShowPrimaryActionModePopup);
                }
                if (DecorView.this.mPrimaryActionModeView != null) {
                    DecorView.this.endOnGoingFadeAnimation();
                    final ActionBarContextView lastActionModeView = DecorView.this.mPrimaryActionModeView;
                    DecorView decorView2 = DecorView.this;
                    ObjectAnimator unused = decorView2.mFadeAnim = ObjectAnimator.ofFloat(decorView2.mPrimaryActionModeView, View.ALPHA, 1.0f, 0.0f);
                    if (DecorView.this.isOppoStyle()) {
                        DecorView.this.mFadeAnim.setDuration(100L);
                    }
                    DecorView.this.mFadeAnim.addListener(new Animator.AnimatorListener() {
                        /* class com.android.internal.policy.DecorView.ActionModeCallback2Wrapper.AnonymousClass1 */

                        @Override // android.animation.Animator.AnimatorListener
                        public void onAnimationStart(Animator animation) {
                        }

                        @Override // android.animation.Animator.AnimatorListener
                        public void onAnimationEnd(Animator animation) {
                            if (lastActionModeView == DecorView.this.mPrimaryActionModeView) {
                                lastActionModeView.setVisibility(8);
                                if (DecorView.this.mPrimaryActionModePopup != null) {
                                    DecorView.this.mPrimaryActionModePopup.dismiss();
                                }
                                lastActionModeView.killMode();
                                ObjectAnimator unused = DecorView.this.mFadeAnim = null;
                                DecorView.this.requestApplyInsets();
                            }
                        }

                        @Override // android.animation.Animator.AnimatorListener
                        public void onAnimationCancel(Animator animation) {
                        }

                        @Override // android.animation.Animator.AnimatorListener
                        public void onAnimationRepeat(Animator animation) {
                        }
                    });
                    DecorView.this.mFadeAnim.start();
                }
                DecorView.this.mPrimaryActionMode = null;
            } else if (isFloating) {
                DecorView.this.cleanupFloatingActionModeViews();
                ActionMode unused2 = DecorView.this.mFloatingActionMode = null;
            }
            if (DecorView.this.mWindow.getCallback() != null && !DecorView.this.mWindow.isDestroyed()) {
                try {
                    DecorView.this.mWindow.getCallback().onActionModeFinished(mode);
                } catch (AbstractMethodError e) {
                }
            }
            DecorView.this.requestFitSystemWindows();
        }

        @Override // android.view.ActionMode.Callback2
        public void onGetContentRect(ActionMode mode, View view, Rect outRect) {
            ActionMode.Callback callback = this.mWrapped;
            if (callback instanceof ActionMode.Callback2) {
                ((ActionMode.Callback2) callback).onGetContentRect(mode, view, outRect);
            } else {
                super.onGetContentRect(mode, view, outRect);
            }
        }
    }

    private int getWindowMode() {
        Window.WindowControllerCallback callback = this.mWindow.getWindowControllerCallback();
        if (callback == null) {
            return -1;
        }
        try {
            return callback.getWindowMode();
        } catch (RemoteException e) {
            Log.e(this.mLogTag, "Failed to get the windowMode of a PhoneWindow.");
            return -1;
        }
    }

    private boolean adjustWindowMode(boolean isConfig, WindowConfiguration winConfig) {
        boolean z;
        int windowMode = getWindowMode();
        winConfig.setWindowingMode(windowMode);
        if (windowMode == this.mWindowMode) {
            return false;
        }
        this.mWindowMode = windowMode;
        if (isConfig && this.mWindowMode == 1 && !(z = this.mLastClipToOutline)) {
            setClipToOutline(z);
        }
        return true;
    }

    private boolean remeasureChildWithMarginsIfNeed(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mDecorCaptionView == null || getChildCount() <= 1 || this.mDecorCaptionView.getParent() != this) {
            return false;
        }
        View caption = this.mDecorCaptionView.getCaption();
        if (!(caption == null || caption.getVisibility() == 8)) {
            int captionHeight = caption.getMeasuredHeight();
            for (int i = 0; i < getChildCount(); i++) {
                View childView = getChildAt(i);
                if (childView != this.mDecorCaptionView) {
                    measureChildWithMargins(childView, widthMeasureSpec, 0, heightMeasureSpec, captionHeight);
                }
            }
        }
        return true;
    }

    private boolean relayoutChildIfNeed() {
        if (this.mDecorCaptionView == null || getChildCount() <= 1 || this.mDecorCaptionView.getParent() != this) {
            return false;
        }
        View caption = this.mDecorCaptionView.getCaption();
        if (!(caption == null || caption.getVisibility() == 8)) {
            int captionHeight = caption.getMeasuredHeight();
            for (int i = 0; i < getChildCount(); i++) {
                View childView = getChildAt(i);
                if (childView != this.mDecorCaptionView && childView.getTop() < captionHeight) {
                    int deltaY = captionHeight - childView.getTop();
                    childView.layout(childView.getLeft(), childView.getTop() + deltaY, childView.getLeft() + childView.getMeasuredWidth(), childView.getTop() + deltaY + childView.getMeasuredHeight());
                }
            }
        }
        return true;
    }
}
