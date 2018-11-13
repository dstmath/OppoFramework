package com.android.internal.app;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.AnimatorSet.Builder;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.animation.Interpolator;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toolbar;
import com.android.internal.R;
import com.android.internal.app.WindowDecorActionBar.ActionModeImpl;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.widget.ActionBarContainer;
import com.android.internal.widget.ActionBarOverlayLayout;
import com.android.internal.widget.ColorActionBarContextView;
import com.android.internal.widget.ColorActionBarOverlayLayout;
import com.android.internal.widget.ColorActionBarView;
import com.android.internal.widget.ColorScrollingTabContainerView;
import com.android.internal.widget.DecorToolbar;
import com.android.internal.widget.ScrollingTabContainerView;
import com.color.actionbar.app.ColorActionBarUtil.ActionBarCallback;
import com.color.actionbar.app.ColorActionBarUtil.ScrollTabCallback;
import com.color.animation.ColorAnimatorUtil;
import com.color.animation.ColorAnimatorWrapper;
import com.color.util.ColorContextUtil;
import com.color.util.ColorLog;
import com.color.util.ColorLogKey.ActionBar;
import com.color.util.ColorLogKey.BottomMenu;
import com.color.view.animation.ColorPathInterpolator;
import com.color.widget.ColorActionModeCallback;
import com.color.widget.ColorBottomMenuCallback;
import com.color.widget.ColorBottomMenuView;
import com.color.widget.ColorSpinnerCallback;
import com.color.widget.ColorSpinnerCallback.DropdownDismissCallback;
import java.util.ArrayList;
import java.util.List;

public class OppoWindowDecorActionBar extends WindowDecorActionBar implements ActionBarCallback, SearchAnimatorListener {
    private static final Interpolator ANIM_INTERPOLATOR = ColorPathInterpolator.create();
    private static final int CONTEXT_DISPLAY_NORMAL = 0;
    private static final int CONTEXT_DISPLAY_SPLIT = 1;
    private static final long FADE_IN_DURATION_MS = 200;
    private static final long FADE_OUT_DURATION_MS = 100;
    private static final String OPPO_WIDGET_ANIM_DISABLE = "oppo.widget.animation.disabled";
    private int mActionBarHeight = 0;
    private ColorActionBarView mActionBarView;
    private ColorActionModeCallback mActionModeCallback = null;
    private Activity mActivity = null;
    private final AnimatorListener mAllHideListener = new AnimatorListenerAdapter() {
        public void onAnimationCancel(Animator animation) {
            OppoWindowDecorActionBar.this.mContextView.onAnimationCancel(animation, false);
            OppoWindowDecorActionBar.this.onSearchAnimationCancel(animation, false);
        }

        public void onAnimationEnd(Animator animation) {
            ColorLog.d(ActionBar.DISP, OppoWindowDecorActionBar.this.mTagClass, "doHide : onAnimationEnd");
            OppoWindowDecorActionBar.this.setCurrentShowAnim(null);
            OppoWindowDecorActionBar.this.completeDeferredDestroyActionMode();
            OppoWindowDecorActionBar.this.mContextView.onAnimationEnd(animation, false);
            OppoWindowDecorActionBar.this.onSearchAnimationEnd(animation, false);
            if (OppoWindowDecorActionBar.this.mContainerView != null) {
                ObjectAnimator actionBarAnim = ObjectAnimator.ofFloat(OppoWindowDecorActionBar.this.mContainerView, View.TRANSLATION_Y, new float[]{0.0f});
                actionBarAnim.setDuration(0);
                actionBarAnim.start();
            }
            if (animation != null && OppoWindowDecorActionBar.this.mSearchLayout != null && OppoWindowDecorActionBar.this.mViewState != null && OppoWindowDecorActionBar.this.mStatusBarColor != OppoWindowDecorActionBar.this.mSearchBgColor && OppoWindowDecorActionBar.this.mIsSearchColor && OppoWindowDecorActionBar.this.mIsStatusBarColor) {
                OppoWindowDecorActionBar.this.setStatusBarColor(OppoWindowDecorActionBar.this.mSearchBgColor);
                OppoWindowDecorActionBar.this.mIsSearchColor = false;
                OppoWindowDecorActionBar.this.mIsStatusBarColor = false;
            }
        }

        public void onAnimationStart(Animator animation) {
            OppoWindowDecorActionBar.this.mContextView.onAnimationStart(animation, false);
            OppoWindowDecorActionBar.this.onSearchAnimationStart(animation, false);
        }

        public void onAnimationRepeat(Animator animation) {
            OppoWindowDecorActionBar.this.mContextView.onAnimationRepeat(animation, false);
            OppoWindowDecorActionBar.this.onSearchAnimationRepeat(animation, false);
        }
    };
    private final AnimatorListener mAllShowListener = new AnimatorListenerAdapter() {
        public void onAnimationCancel(Animator animation) {
            OppoWindowDecorActionBar.this.mContextView.onAnimationCancel(animation, true);
            OppoWindowDecorActionBar.this.onSearchAnimationCancel(animation, true);
        }

        public void onAnimationEnd(Animator animation) {
            ColorLog.d(ActionBar.DISP, OppoWindowDecorActionBar.this.mTagClass, "doShow : onAnimationEnd");
            OppoWindowDecorActionBar.this.setCurrentShowAnim(null);
            OppoWindowDecorActionBar.this.mContextView.onAnimationEnd(animation, true);
            OppoWindowDecorActionBar.this.onSearchAnimationEnd(animation, true);
            if (OppoWindowDecorActionBar.this.mSearchLayout != null && OppoWindowDecorActionBar.this.mViewState != null && animation != null && OppoWindowDecorActionBar.this.mStatusBarColor != OppoWindowDecorActionBar.this.mSearchBgColor && OppoWindowDecorActionBar.this.mIsSearchColor && OppoWindowDecorActionBar.this.mIsStatusBarColor) {
                OppoWindowDecorActionBar.this.setStatusBarColor(OppoWindowDecorActionBar.this.mStatusBarColor);
                OppoWindowDecorActionBar.this.mIsSearchColor = false;
                OppoWindowDecorActionBar.this.mIsStatusBarColor = false;
            }
        }

        public void onAnimationRepeat(Animator animation) {
            OppoWindowDecorActionBar.this.mContextView.onAnimationRepeat(animation, true);
            OppoWindowDecorActionBar.this.onSearchAnimationRepeat(animation, true);
        }

        public void onAnimationStart(Animator animation) {
            OppoWindowDecorActionBar.this.mContextView.onAnimationStart(animation, true);
            OppoWindowDecorActionBar.this.onSearchAnimationStart(animation, true);
        }
    };
    final AnimatorUpdateListener mAlphaUpdateListener = new AnimatorUpdateListener() {
        public void onAnimationUpdate(ValueAnimator animation) {
            float value = ((Float) animation.getAnimatedValue()).floatValue();
            int endColor = Color.rgb((int) (((float) OppoWindowDecorActionBar.this.mStatusBarRedColor) + (((float) (OppoWindowDecorActionBar.this.mSearchBgRedColor - OppoWindowDecorActionBar.this.mStatusBarRedColor)) * value)), (int) (((float) OppoWindowDecorActionBar.this.mStatusBarGreenColor) + (((float) (OppoWindowDecorActionBar.this.mSearchBgGreenColor - OppoWindowDecorActionBar.this.mStatusBarGreenColor)) * value)), (int) (((float) OppoWindowDecorActionBar.this.mStatusBarBlueColor) + (((float) (OppoWindowDecorActionBar.this.mSearchBgBlueColor - OppoWindowDecorActionBar.this.mStatusBarBlueColor)) * value)));
            OppoWindowDecorActionBar.this.mViewState.setBackgroundColor(endColor);
            if (endColor == OppoWindowDecorActionBar.this.mSearchBgColor) {
                OppoWindowDecorActionBar.this.setStatusBarColor(OppoWindowDecorActionBar.this.mSearchBgColor);
            }
        }
    };
    private boolean mAnimationDisabled = false;
    private AnimatorSet mAnimatorSet = null;
    private final AnimatorListener mContainerHideListener = new AnimatorListenerAdapter() {
        private boolean mIsCancel = false;

        public void onAnimationEnd(Animator animation) {
            View bgView;
            int visibility = this.mIsCancel ? 0 : 8;
            if (OppoWindowDecorActionBar.this.mContentAnimations && OppoWindowDecorActionBar.this.mContentView != null) {
                OppoWindowDecorActionBar.this.mContentView.setTranslationY(0.0f);
                OppoWindowDecorActionBar.this.mContainerView.setTranslationY(0.0f);
                bgView = OppoWindowDecorActionBar.this.getTranslucentBgView();
                if (bgView != null) {
                    bgView.setTranslationY(0.0f);
                }
            }
            if (OppoWindowDecorActionBar.this.mSplitView != null && OppoWindowDecorActionBar.this.mContextDisplayMode == 1 && OppoWindowDecorActionBar.this.mIsSplitHideWithActionBar) {
                OppoWindowDecorActionBar.this.mSplitView.setVisibility(visibility);
            }
            OppoWindowDecorActionBar.this.mContainerView.setVisibility(visibility);
            OppoWindowDecorActionBar.this.mContainerView.setTransitioning(false);
            bgView = OppoWindowDecorActionBar.this.getTranslucentBgView();
            if (bgView != null) {
                bgView.setVisibility(visibility);
            }
            if (OppoWindowDecorActionBar.this.mOverlayLayout != null) {
                OppoWindowDecorActionBar.this.mOverlayLayout.requestFitSystemWindows();
            }
            OppoWindowDecorActionBar.this.resizeScreenLayout(false);
            resetState();
        }

        public void onAnimationCancel(Animator animation) {
            this.mIsCancel = true;
        }

        private void resetState() {
            this.mIsCancel = false;
        }
    };
    private final AnimatorListener mContainerShowListener = new AnimatorListenerAdapter() {
        public void onAnimationEnd(Animator animation) {
            OppoWindowDecorActionBar.this.mContainerView.requestLayout();
            OppoWindowDecorActionBar.this.resizeScreenLayout(false);
        }
    };
    private ActionBarContainer mContainerView;
    private boolean mContentAnimations = true;
    private Drawable mContentForeground = null;
    private int mContentForegroundGravity = 119;
    private FrameLayout mContentLayout;
    private View mContentView;
    private Context mContext;
    private int mContextDisplayMode;
    private ColorActionBarContextView mContextView;
    private int mCurWindowVisibility = 0;
    private DecorToolbar mDecorToolbar;
    private boolean mHasEmbeddedTabs;
    private int mIdActionBar = -1;
    private int mIdActionBarContainer = -1;
    private int mIdActionContextBar = -1;
    private int mIdDecorContentParent = -1;
    private int mIdSplitActionBar = -1;
    private boolean mIsActionBarOverlay;
    private boolean mIsActionModeAnim = false;
    private boolean mIsNoTitle;
    private boolean mIsOppoStyle = false;
    private boolean mIsSearchBarMode = false;
    private boolean mIsSearchColor = false;
    private boolean mIsSplitHideWithActionBar = false;
    private boolean mIsStatusBarColor = false;
    private AnimatorListener mModeAnimatorListener = new ModeAnimatorListener(this, null);
    private ColorActionBarOverlayLayout mOverlayLayout;
    private int mScreenHeight = 0;
    private View mScreenLayout = null;
    private LayoutParams mScreenLayoutParams = null;
    private int mSearchBgBlueColor;
    private int mSearchBgColor;
    private int mSearchBgGreenColor;
    private int mSearchBgRedColor;
    private List<AnimatorListener> mSearchHideListenerList = new ArrayList();
    private ViewGroup mSearchLayout = null;
    private List<AnimatorListener> mSearchShowListenerList = new ArrayList();
    private List<UserAnimator> mSearchUserAnimations = new ArrayList();
    private UserAnimator mSearchUserWithAnimation = new UserAnimator("with");
    private boolean mShowHideAnimationEnabled;
    private ActionBarContainer mSplitView;
    private int mStackedTabHeight = 0;
    private int mStatusBarBlueColor;
    private int mStatusBarColor;
    private int mStatusBarGreenColor;
    private int mStatusBarHeight = 0;
    private int mStatusBarRedColor;
    private int mTabScrollState = 0;
    protected final Class<?> mTagClass = getClass();
    private View mViewState;

    private class ContainerUpdateListener implements AnimatorUpdateListener {
        private final AnimatorUpdateListener mListener;

        public ContainerUpdateListener(AnimatorUpdateListener listener) {
            this.mListener = listener;
        }

        public void onAnimationUpdate(ValueAnimator animation) {
            if (this.mListener != null) {
                this.mListener.onAnimationUpdate(animation);
            }
            View bgView = OppoWindowDecorActionBar.this.getTranslucentBgView();
            float value = ((Float) animation.getAnimatedValue()).floatValue();
            if (OppoWindowDecorActionBar.this.mContentAnimations && OppoWindowDecorActionBar.this.mContentView != null) {
                int limit = -OppoWindowDecorActionBar.this.mContainerView.getHeight();
                if (value >= ((float) limit)) {
                    OppoWindowDecorActionBar.this.mContentView.setTranslationY(value);
                    if (bgView != null) {
                        bgView.setTranslationY(value);
                    }
                } else {
                    OppoWindowDecorActionBar.this.mContentView.setTranslationY((float) limit);
                    if (bgView != null) {
                        bgView.setTranslationY((float) limit);
                    }
                }
            }
            OppoWindowDecorActionBar.this.mContainerView.setTranslationY(value);
            if (bgView != null) {
                bgView.setTranslationY(value);
            }
        }
    }

    private class ModeAnimatorListener extends AnimatorListenerAdapter {
        /* synthetic */ ModeAnimatorListener(OppoWindowDecorActionBar this$0, ModeAnimatorListener -this1) {
            this();
        }

        private ModeAnimatorListener() {
        }

        public void onAnimationEnd(Animator animation) {
            OppoWindowDecorActionBar.this.setAnimation(null);
        }
    }

    private class OppoActionModeImpl extends ActionModeImpl {
        private Callback mCallback = null;
        private MenuBuilder mSplitMenu = null;

        public OppoActionModeImpl(Context context, Callback callback) {
            super(context, callback);
            this.mCallback = callback;
            if (OppoWindowDecorActionBar.this.mActionModeCallback != null) {
                this.mSplitMenu = new MenuBuilder(context).setDefaultShowAsAction(1);
                this.mSplitMenu.setCallback(this);
            }
        }

        public boolean dispatchOnCreate() {
            boolean result = super.dispatchOnCreate();
            if (OppoWindowDecorActionBar.this.mActionModeCallback == null || this.mSplitMenu == null) {
                return result;
            }
            MenuBuilder menuBuilder = this.mSplitMenu;
            menuBuilder.stopDispatchingItemsChanged();
            try {
                menuBuilder = OppoWindowDecorActionBar.this.mActionModeCallback.onCreateSplitMenu(this, this.mSplitMenu);
                result |= menuBuilder;
            } finally {
                MenuBuilder menuBuilder2 = menuBuilder;
                this.mSplitMenu.startDispatchingItemsChanged();
                return result;
            }
            return result;
        }

        public void invalidate() {
            super.invalidate();
            if (!(OppoWindowDecorActionBar.this.mActionMode != this || OppoWindowDecorActionBar.this.mActionModeCallback == null || this.mSplitMenu == null)) {
                this.mSplitMenu.stopDispatchingItemsChanged();
                try {
                    OppoWindowDecorActionBar.this.mActionModeCallback.onPrepareSplitMenu(this, this.mSplitMenu);
                } finally {
                    this.mSplitMenu.startDispatchingItemsChanged();
                }
            }
        }

        public void finish() {
            if (OppoWindowDecorActionBar.this.mActionMode == this) {
                this.mCallback.onDestroyActionMode(this);
                this.mCallback = null;
                OppoWindowDecorActionBar.this.animateToMode(false);
                OppoWindowDecorActionBar.this.mContextView.closeMode();
                OppoWindowDecorActionBar.this.mDecorToolbar.getViewGroup().sendAccessibilityEvent(32);
                OppoWindowDecorActionBar.this.mOverlayLayout.setHideOnContentScrollEnabled(OppoWindowDecorActionBar.this.mHideOnContentScroll);
                OppoWindowDecorActionBar.this.mActionMode = null;
            }
        }

        public void dispatchOnStart() {
            if (OppoWindowDecorActionBar.this.mActionModeCallback != null) {
                OppoWindowDecorActionBar.this.mActionModeCallback.onStartActionMode(this);
            }
        }
    }

    private class UserAnimator {
        private final List<Animator> mAnimList = new ArrayList();
        private final List<ColorAnimatorWrapper> mAnimWrapperList = new ArrayList();
        private final String mTag;

        public UserAnimator(String tag) {
            this.mTag = tag;
        }

        public String getTag() {
            return this.mTag;
        }

        public List<Animator> getAnimList() {
            return this.mAnimList;
        }

        public List<ColorAnimatorWrapper> getAnimWrapperList() {
            return this.mAnimWrapperList;
        }
    }

    private OppoWindowDecorActionBar(Activity activity) {
        super(activity);
        this.mActivity = activity;
        this.mSearchUserAnimations.add(this.mSearchUserWithAnimation);
        init(activity.getWindow());
    }

    private OppoWindowDecorActionBar(Dialog dialog) {
        super(dialog);
        init(dialog.getWindow());
    }

    public static WindowDecorActionBar newInstance(Activity activity) {
        if (ColorContextUtil.isOppoStyle(activity)) {
            return new OppoWindowDecorActionBar(activity);
        }
        return new WindowDecorActionBar(activity);
    }

    public static WindowDecorActionBar newInstance(Dialog dialog) {
        if (ColorContextUtil.isOppoStyle(dialog.getContext())) {
            return new OppoWindowDecorActionBar(dialog);
        }
        return new WindowDecorActionBar(dialog);
    }

    public void onWindowVisibilityChanged(int visibility) {
        this.mCurWindowVisibility = visibility;
        super.onWindowVisibilityChanged(visibility);
    }

    public void setShowHideAnimationEnabled(boolean enabled) {
        this.mShowHideAnimationEnabled = enabled;
        super.setShowHideAnimationEnabled(enabled);
    }

    public void enableContentAnimations(boolean enabled) {
        this.mContentAnimations = enabled;
        super.enableContentAnimations(enabled);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (ColorContextUtil.isOppoStyle(getThemedContext())) {
            ensureTabsExist();
        } else {
            super.onConfigurationChanged(newConfig);
        }
    }

    void setHasEmbeddedTabs(boolean hasEmbeddedTabs) {
        if (this.mTabScrollView != null) {
            this.mTabScrollView.setContentHeight(hasEmbeddedTabs ? this.mActionBarHeight : this.mStackedTabHeight);
        }
        this.mHasEmbeddedTabs = hasEmbeddedTabs;
        super.setHasEmbeddedTabs(hasEmbeddedTabs);
    }

    void ensureTabsExist() {
        if (this.mTabScrollView == null) {
            ScrollingTabContainerView tabScroller = ColorScrollingTabContainerView.newInstance(this.mContext);
            tabScroller.setContentHeight(this.mHasEmbeddedTabs ? this.mActionBarHeight : this.mStackedTabHeight);
            if (this.mHasEmbeddedTabs) {
                tabScroller.setVisibility(0);
                this.mDecorToolbar.setEmbeddedTabView(tabScroller);
            } else {
                if (getNavigationMode() == 2) {
                    tabScroller.setVisibility(0);
                    if (this.mOverlayLayout != null) {
                        this.mOverlayLayout.requestApplyInsets();
                    }
                } else {
                    tabScroller.setVisibility(8);
                }
                this.mContainerView.setTabContainer(tabScroller);
            }
            this.mTabScrollView = tabScroller;
        }
    }

    void animateToMode(boolean toActionMode) {
        Animator fadeOut;
        Animator fadeIn;
        finishAnimation();
        if (toActionMode) {
            showForActionMode();
        } else {
            hideForActionMode();
        }
        if (toActionMode) {
            fadeOut = this.mDecorToolbar.setupAnimatorToVisibility(8, FADE_OUT_DURATION_MS);
            fadeIn = this.mContextView.setupAnimatorToVisibility(0, FADE_IN_DURATION_MS);
        } else {
            fadeIn = this.mDecorToolbar.setupAnimatorToVisibility(0, FADE_IN_DURATION_MS);
            fadeOut = this.mContextView.setupAnimatorToVisibility(8, FADE_OUT_DURATION_MS);
        }
        AnimatorSet set = new AnimatorSet();
        set.playSequentially(new Animator[]{fadeOut, fadeIn});
        set.start();
        setAnimation(set);
        if (this.mTabScrollView != null && (this.mDecorToolbar.hasEmbeddedTabs() ^ 1) != 0 && getNavigationMode() == 2) {
            if (toActionMode) {
                this.mTabScrollView.setAlpha(0.3f);
                if (this.mTabScrollView instanceof ColorScrollingTabContainerView) {
                    ((ColorScrollingTabContainerView) this.mTabScrollView).setTabClickable(false);
                    return;
                }
                return;
            }
            this.mTabScrollView.setAlpha(1.0f);
            if (this.mTabScrollView instanceof ColorScrollingTabContainerView) {
                ((ColorScrollingTabContainerView) this.mTabScrollView).setTabClickable(true);
            }
        }
    }

    public ActionMode startActionMode(Callback callback) {
        if (this.mTabScrollState != 0) {
            return null;
        }
        if (this.mActionMode != null) {
            this.mActionMode.finish();
        }
        this.mOverlayLayout.setHideOnContentScrollEnabled(false);
        this.mContextView.killMode();
        OppoActionModeImpl mode = new OppoActionModeImpl(this.mContextView.getContext(), callback);
        if (!mode.dispatchOnCreate()) {
            return null;
        }
        mode.invalidate();
        mode.dispatchOnStart();
        this.mContextView.setShowingFlags(hookCheckShowingFlags());
        this.mContextView.initForMode(mode);
        animateToMode(true);
        if (!(this.mSplitView == null || this.mContextDisplayMode != 1 || this.mSplitView.getVisibility() == 0)) {
            this.mSplitView.setVisibility(0);
            if (this.mOverlayLayout != null) {
                this.mOverlayLayout.requestApplyInsets();
            }
        }
        this.mContextView.sendAccessibilityEvent(32);
        this.mActionMode = mode;
        return mode;
    }

    public void doShow(boolean fromSystem) {
        endCurrentShowAnim();
        restoreForeground();
        View bgView = getTranslucentBgView();
        if (bgView != null) {
            bgView.setVisibility(0);
        }
        this.mContainerView.setVisibility(0);
        if (this.mCurWindowVisibility == 0 && (this.mShowHideAnimationEnabled || fromSystem)) {
            Builder b;
            changeScreenHeight();
            if (bgView != null) {
                bgView.setTranslationY(0.0f);
            }
            this.mContainerView.setTranslationY(0.0f);
            float startingY = (float) (-getContainerHeight());
            if (fromSystem) {
                int[] topLeft = new int[]{0, 0};
                this.mContainerView.getLocationInWindow(topLeft);
                startingY -= (float) topLeft[1];
            }
            ColorLog.d(ActionBar.DISP, this.mTagClass, "doShow (do anim) : startingY=", Float.valueOf(startingY));
            this.mContainerView.setTranslationY(startingY);
            Animator c = null;
            if (bgView != null) {
                bgView.setTranslationY(startingY);
                c = ObjectAnimator.ofFloat(bgView, View.TRANSLATION_Y, new float[]{0.0f});
            }
            AnimatorSet anim = new AnimatorSet();
            ObjectAnimator a = ObjectAnimator.ofFloat(this.mContainerView, View.TRANSLATION_Y, new float[]{0.0f});
            a.addUpdateListener(this.mUpdateListener);
            a.addListener(this.mContainerShowListener);
            if (this.mViewState != null && (this.mViewState.getBackground() instanceof ColorDrawable)) {
                this.mIsStatusBarColor = true;
            }
            if (this.mSearchLayout != null && (((ViewGroup) this.mSearchLayout.getParent()).getBackground() instanceof ColorDrawable)) {
                this.mIsSearchColor = true;
            }
            if (this.mSearchLayout != null && this.mViewState != null && this.mStatusBarColor != this.mSearchBgColor && this.mIsSearchColor && this.mIsStatusBarColor) {
                ValueAnimator alpha = ValueAnimator.ofFloat(new float[]{1.0f, 0.0f});
                alpha.addUpdateListener(this.mAlphaUpdateListener);
                if (c != null) {
                    b = anim.play(a).with(alpha).with(c);
                } else {
                    b = anim.play(a).with(alpha);
                }
            } else if (c != null) {
                b = anim.play(a).with(c);
            } else {
                b = anim.play(a);
            }
            if (this.mContentAnimations && this.mContentView != null) {
                this.mContentView.setTranslationY(startingY);
                b.with(ObjectAnimator.ofFloat(this.mContentView, View.TRANSLATION_Y, new float[]{0.0f}));
            }
            if (this.mSplitView != null && this.mContextDisplayMode == 1 && this.mIsSplitHideWithActionBar) {
                this.mSplitView.setTranslationY((float) this.mSplitView.getHeight());
                this.mSplitView.setVisibility(0);
                b.with(ObjectAnimator.ofFloat(this.mSplitView, View.TRANSLATION_Y, new float[]{0.0f}));
            }
            this.mContextView.playUserAnimators(b, this.mIsActionModeAnim);
            playSearchUserAnimators(b, null, this.mIsActionModeAnim);
            anim.setInterpolator(ANIM_INTERPOLATOR);
            if (this.mIsSearchBarMode) {
                anim.setDuration(0);
            } else {
                anim.setDuration((long) this.mContext.getResources().getInteger(202179603));
            }
            anim.addListener(this.mAllShowListener);
            setCurrentShowAnim(anim);
            anim.start();
        } else {
            ColorLog.d(ActionBar.DISP, this.mTagClass, "doShow (no anim)");
            if (bgView != null) {
                bgView.setAlpha(1.0f);
                bgView.setTranslationY(0.0f);
            }
            this.mContainerView.setAlpha(1.0f);
            this.mContainerView.setTranslationY(0.0f);
            if (this.mContentAnimations && this.mContentView != null) {
                this.mContentView.setTranslationY(0.0f);
            }
            if (this.mSplitView != null && this.mContextDisplayMode == 1 && this.mIsSplitHideWithActionBar) {
                this.mSplitView.setAlpha(1.0f);
                this.mSplitView.setTranslationY(0.0f);
                this.mSplitView.setVisibility(0);
            }
            this.mContainerShowListener.onAnimationEnd(null);
            this.mAllShowListener.onAnimationEnd(null);
        }
        if (this.mOverlayLayout != null) {
            this.mOverlayLayout.requestFitSystemWindows();
        }
    }

    public void doHide(boolean fromSystem) {
        endCurrentShowAnim();
        clearForeground();
        if (this.mCurWindowVisibility == 0 && (this.mShowHideAnimationEnabled || fromSystem)) {
            Builder b;
            changeScreenHeight();
            View bgView = getTranslucentBgView();
            if (bgView != null) {
                bgView.setAlpha(1.0f);
            }
            this.mContainerView.setAlpha(1.0f);
            this.mContainerView.setTransitioning(true);
            float endingY = (float) (-(this.mContainerView.getHeight() + this.mStatusBarHeight));
            View view = this.mContainerView.getRootView();
            this.mViewState = view.findViewById(R.id.statusBarBackground);
            if (this.mViewState != null) {
                if (this.mViewState.getBackground() instanceof ColorDrawable) {
                    this.mIsStatusBarColor = true;
                    this.mStatusBarColor = ((ColorDrawable) this.mViewState.getBackground()).getColor();
                }
                this.mStatusBarRedColor = Color.red(this.mStatusBarColor);
                this.mStatusBarGreenColor = Color.green(this.mStatusBarColor);
                this.mStatusBarBlueColor = Color.blue(this.mStatusBarColor);
            }
            this.mSearchLayout = (ViewGroup) view.findViewById(201458834);
            if (this.mSearchLayout != null) {
                ViewGroup search = (ViewGroup) this.mSearchLayout.getParent();
                if (search.getBackground() instanceof ColorDrawable) {
                    this.mIsSearchColor = true;
                    this.mSearchBgColor = ((ColorDrawable) search.getBackground()).getColor();
                }
                this.mSearchBgRedColor = Color.red(this.mSearchBgColor);
                this.mSearchBgGreenColor = Color.green(this.mSearchBgColor);
                this.mSearchBgBlueColor = Color.blue(this.mSearchBgColor);
            }
            if (fromSystem) {
                int[] topLeft = new int[]{0, 0};
                this.mContainerView.getLocationInWindow(topLeft);
                endingY -= (float) topLeft[1];
            }
            ColorLog.d(ActionBar.DISP, this.mTagClass, "doHide (do anim) : endingY=" + endingY);
            AnimatorSet anim = new AnimatorSet();
            ValueAnimator a = ValueAnimator.ofFloat(new float[]{0.0f, endingY});
            a.addUpdateListener(new ContainerUpdateListener(this.mUpdateListener));
            a.addListener(this.mContainerHideListener);
            if (this.mViewState == null || this.mSearchLayout == null || this.mStatusBarColor == this.mSearchBgColor || !this.mIsSearchColor || !this.mIsStatusBarColor) {
                b = anim.play(a);
            } else {
                ValueAnimator alpha = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
                alpha.addUpdateListener(this.mAlphaUpdateListener);
                b = anim.play(a).with(alpha);
            }
            if (this.mSplitView != null && this.mSplitView.getVisibility() == 0 && this.mIsSplitHideWithActionBar) {
                this.mSplitView.setAlpha(1.0f);
                b.with(ObjectAnimator.ofFloat(this.mSplitView, View.TRANSLATION_Y, new float[]{(float) this.mSplitView.getHeight()}));
            }
            this.mContextView.playUserAnimators(b, this.mIsActionModeAnim);
            playSearchUserAnimators(b, null, this.mIsActionModeAnim);
            anim.setInterpolator(ANIM_INTERPOLATOR);
            if (this.mIsSearchBarMode) {
                anim.setDuration(0);
            } else {
                anim.setDuration((long) this.mContext.getResources().getInteger(202179603));
            }
            anim.addListener(this.mAllHideListener);
            setCurrentShowAnim(anim);
            anim.start();
            return;
        }
        ColorLog.d(ActionBar.DISP, this.mTagClass, "doHide (no anim)");
        this.mContainerHideListener.onAnimationEnd(null);
        this.mAllHideListener.onAnimationEnd(null);
    }

    public void addShowListener(AnimatorListener listener) {
        this.mContextView.addShowListener(listener);
    }

    public void addHideListener(AnimatorListener listener) {
        this.mContextView.addHideListener(listener);
    }

    public void addWithAnimator(Animator anim) {
        this.mContextView.addWithAnimator(anim);
    }

    public void addWithAnimators(List<Animator> animList) {
        this.mContextView.addWithAnimators(animList);
    }

    public void addAfterAnimator(Animator anim) {
        this.mContextView.addAfterAnimator(anim);
    }

    public void addAfterAnimators(List<Animator> animList) {
        this.mContextView.addAfterAnimators(animList);
    }

    public void addBeforeAnimator(Animator anim) {
        this.mContextView.addBeforeAnimator(anim);
    }

    public void addBeforeAnimators(List<Animator> animList) {
        this.mContextView.addBeforeAnimators(animList);
    }

    public int getContentId() {
        return 16908290;
    }

    public int getHomeId() {
        return R.id.home;
    }

    public void updateTabScrollPosition(int position, float positionOffset, int positionOffsetPixels) {
        if (getNavigationMode() == 2 && (this.mTabScrollView instanceof ScrollTabCallback)) {
            ((ScrollTabCallback) this.mTabScrollView).updateTabScrollPosition(position, positionOffset, positionOffsetPixels);
        }
    }

    public void updateTabScrollState(int state) {
        if (getNavigationMode() == 2 && (this.mTabScrollView instanceof ScrollTabCallback)) {
            this.mTabScrollState = state;
            ((ScrollTabCallback) this.mTabScrollView).updateTabScrollState(state);
        }
    }

    public void updateMenuScrollPosition(int index, float offset) {
        if (this.mSplitView != null) {
            View splitMenuView = this.mSplitView.findViewById(201458882);
            if (splitMenuView instanceof ColorBottomMenuCallback) {
                ((ColorBottomMenuCallback) splitMenuView).updateMenuScrollPosition(index, offset);
            }
        }
    }

    public void updateMenuScrollState(int state) {
        if (this.mSplitView != null) {
            View splitMenuView = this.mSplitView.findViewById(201458882);
            if (splitMenuView instanceof ColorBottomMenuCallback) {
                this.mTabScrollState = state;
                ((ColorBottomMenuCallback) splitMenuView).updateMenuScrollState(state);
            }
        }
    }

    public void updateMenuScrollData() {
        if (this.mSplitView != null) {
            View splitMenuView = this.mSplitView.findViewById(201458882);
            if (splitMenuView instanceof ColorBottomMenuCallback) {
                ((ColorBottomMenuCallback) splitMenuView).updateMenuScrollData();
            }
        }
    }

    public void setMenuUpdateMode(int updateMode) {
        if (this.mSplitView != null) {
            View splitMenuView = this.mSplitView.findViewById(201458882);
            if (splitMenuView instanceof ColorBottomMenuCallback) {
                ((ColorBottomMenuCallback) splitMenuView).setMenuUpdateMode(updateMode);
            }
        }
    }

    public void lockMenuUpdate() {
        if (this.mSplitView != null) {
            View splitMenuView = this.mSplitView.findViewById(201458882);
            if (splitMenuView instanceof ColorBottomMenuCallback) {
                ((ColorBottomMenuCallback) splitMenuView).lockMenuUpdate();
            }
        }
    }

    public void unlockMenuUpdate() {
        if (this.mSplitView != null) {
            View splitMenuView = this.mSplitView.findViewById(201458882);
            if (splitMenuView instanceof ColorBottomMenuCallback) {
                ((ColorBottomMenuCallback) splitMenuView).unlockMenuUpdate();
            }
        }
    }

    public void setActionModeCallback(ColorActionModeCallback callback) {
        this.mActionModeCallback = callback;
    }

    public void setEmbeddedTabs(boolean embedded) {
        setHasEmbeddedTabs(embedded);
    }

    public void setSplitActionBarOverlay(boolean overlay) {
        if (this.mOverlayLayout != null) {
            this.mOverlayLayout.setSplitActionBarOverlay(overlay);
        }
    }

    public void setSplitHideWithActionBar(boolean hideWith) {
        this.mIsSplitHideWithActionBar = hideWith;
    }

    public void setSearchBarMode(boolean flag) {
        this.mIsSearchBarMode = flag ? this.mAnimationDisabled : false;
    }

    public void setActionModeAnim(boolean flag) {
        this.mIsActionModeAnim = flag;
    }

    public boolean hasEmbeddedTabs() {
        return this.mHasEmbeddedTabs;
    }

    public void cancelShowHide() {
        cancelCurrentShowAnim();
    }

    public void setDropdownDismissCallback(DropdownDismissCallback callback) {
        if (this.mDecorToolbar instanceof ColorSpinnerCallback) {
            ((ColorSpinnerCallback) this.mDecorToolbar).setDropdownDismissCallback(callback);
        }
    }

    public void setDropdownItemClickListener(OnItemClickListener listener) {
        if (this.mDecorToolbar instanceof ColorSpinnerCallback) {
            ((ColorSpinnerCallback) this.mDecorToolbar).setDropdownItemClickListener(listener);
        }
    }

    public void setDropdownUpdateAfterAnim(boolean update) {
        if (this.mDecorToolbar instanceof ColorSpinnerCallback) {
            ((ColorSpinnerCallback) this.mDecorToolbar).setDropdownUpdateAfterAnim(update);
        }
    }

    public boolean isDropDownShowing() {
        if (this.mDecorToolbar instanceof ColorSpinnerCallback) {
            return ((ColorSpinnerCallback) this.mDecorToolbar).isDropDownShowing();
        }
        return false;
    }

    public void addWithAnimatorWrapper(ColorAnimatorWrapper anim) {
        this.mContextView.addWithAnimatorWrapper(anim);
    }

    public void addWithAnimatorWrappers(List<ColorAnimatorWrapper> animList) {
        this.mContextView.addWithAnimatorWrappers(animList);
    }

    public void addAfterAnimatorWrapper(ColorAnimatorWrapper anim) {
        this.mContextView.addAfterAnimatorWrapper(anim);
    }

    public void addAfterAnimatorWrappers(List<ColorAnimatorWrapper> animList) {
        this.mContextView.addAfterAnimatorWrappers(animList);
    }

    public void addBeforeAnimatorWrapper(ColorAnimatorWrapper anim) {
        this.mContextView.addBeforeAnimatorWrapper(anim);
    }

    public void addBeforeAnimatorWrappers(List<ColorAnimatorWrapper> animList) {
        this.mContextView.addBeforeAnimatorWrappers(animList);
    }

    @Deprecated
    public void updateAnimateTab(int position, float positionOffset, int positionOffsetPixels) {
        updateTabScrollPosition(position, positionOffset, positionOffsetPixels);
    }

    @Deprecated
    public void updateScrollState(int state) {
        updateTabScrollState(state);
    }

    void updateVisibility(boolean fromSystem) {
        ColorLog.i(ActionBar.DISP, this.mTagClass, "updateVisibility : hookCheckShowingFlags()=", Boolean.valueOf(hookCheckShowingFlags()), ", mShowingForMode=", Boolean.valueOf(this.mShowingForMode), ", mIsActionModeAnim=", Boolean.valueOf(this.mIsActionModeAnim));
        if (!this.mIsActionModeAnim || hookCheckShowingFlags()) {
            super.updateVisibility(fromSystem);
            return;
        }
        if (this.mShowingForMode) {
            this.mNowShowing = false;
            doHide(fromSystem);
        } else {
            this.mNowShowing = true;
            doShow(fromSystem);
        }
    }

    public void selectTab(Tab tab) {
        if (this.mActionMode == null) {
            super.selectTab(tab);
        }
    }

    void init(View decor) {
        this.mContext = decor.getContext();
        this.mIsOppoStyle = ColorContextUtil.isOppoStyle(this.mContext);
        this.mIdActionBar = ColorContextUtil.getResId(this.mContext, 201458895);
        this.mIdSplitActionBar = ColorContextUtil.getResId(this.mContext, 201458896);
        this.mIdActionBarContainer = ColorContextUtil.getResId(this.mContext, 201458899);
        this.mIdActionContextBar = ColorContextUtil.getResId(this.mContext, 201458900);
        this.mIdDecorContentParent = ColorContextUtil.getResId(this.mContext, 201458901);
        this.mOverlayLayout = (ColorActionBarOverlayLayout) decor.findViewById(this.mIdDecorContentParent);
        this.mDecorToolbar = getDecorToolbar(decor.findViewById(this.mIdActionBar));
        this.mActionBarView = (ColorActionBarView) decor.findViewById(this.mIdActionBar);
        this.mContextView = (ColorActionBarContextView) decor.findViewById(this.mIdActionContextBar);
        this.mContainerView = (ActionBarContainer) decor.findViewById(this.mIdActionBarContainer);
        this.mSplitView = (ActionBarContainer) decor.findViewById(this.mIdSplitActionBar);
        if (this.mDecorToolbar == null || this.mContextView == null || this.mContainerView == null) {
            throw new IllegalStateException(getClass().getSimpleName() + " can only be used " + "with a compatible window decor layout");
        }
        this.mContextDisplayMode = this.mDecorToolbar.isSplit() ? 1 : 0;
        boolean hasEmbedded = this.mHasEmbeddedTabs;
        super.init(decor);
        if (this.mIsOppoStyle) {
            setHasEmbeddedTabs(hasEmbedded);
        }
    }

    private void init(Window window) {
        TypedArray a = this.mContext.obtainStyledAttributes(oppo.R.styleable.OppoTheme);
        this.mIsSplitHideWithActionBar = a.getBoolean(2, false);
        a.recycle();
        this.mContentLayout = (FrameLayout) window.getDecorView().findViewById(16908290);
        this.mIsNoTitle = window.hasFeature(1);
        this.mIsActionBarOverlay = window.hasFeature(9);
        if (!this.mIsActionBarOverlay) {
            this.mContentView = this.mContentLayout;
        }
        this.mStatusBarHeight = this.mContext.getResources().getDimensionPixelSize(201654274);
        this.mContentForeground = this.mContentLayout.getForeground();
        this.mContentForegroundGravity = this.mContentLayout.getForegroundGravity();
        if (this.mIsNoTitle) {
            clearForeground();
        }
        initScreenInfo();
        this.mAnimationDisabled = this.mContext.getPackageManager().hasSystemFeature(OPPO_WIDGET_ANIM_DISABLE);
        this.mStackedTabHeight = this.mContext.getResources().getDimensionPixelSize(201655455);
    }

    private void initScreenInfo() {
        TypedArray a = this.mContext.obtainStyledAttributes(android.R.styleable.Theme);
        this.mActionBarHeight = a.getDimensionPixelSize(139, 0);
        a.recycle();
        ViewParent parent = this.mContainerView.getParent();
        if (!(parent instanceof ActionBarOverlayLayout)) {
            return;
        }
        if (this.mIsActionBarOverlay) {
            this.mScreenLayout = null;
            return;
        }
        this.mScreenLayout = (View) parent;
        this.mScreenLayoutParams = new LayoutParams(this.mScreenLayout.getLayoutParams());
        this.mScreenLayout.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                OppoWindowDecorActionBar.this.mScreenHeight = OppoWindowDecorActionBar.this.mScreenLayout.getMeasuredHeight();
            }
        });
    }

    private void clearForeground() {
        ColorLog.d(ActionBar.DISP, this.mTagClass, "clearForeground");
        this.mContentLayout.setForegroundGravity(119);
        this.mContentLayout.setForeground(null);
    }

    private void restoreForeground() {
        ColorLog.d(ActionBar.DISP, this.mTagClass, "restoreForeground");
        this.mContentLayout.setForegroundGravity(this.mContentForegroundGravity);
        this.mContentLayout.setForeground(this.mContentForeground);
    }

    private int getContainerHeight() {
        int containerHeight = this.mContainerView.getHeight();
        if (containerHeight == 0) {
            return this.mActionBarHeight;
        }
        return containerHeight;
    }

    private void resizeScreenLayout(boolean change) {
        if (this.mScreenLayout != null) {
            LayoutParams lp = this.mScreenLayout.getLayoutParams();
            if (change) {
                lp.height = this.mScreenHeight + getContainerHeight();
            } else if (this.mScreenLayoutParams != null) {
                lp.height = this.mScreenLayoutParams.height;
            }
            ColorLog.d(ActionBar.DISP, this.mTagClass, "resizeScreenLayout : " + lp.height);
            this.mScreenLayout.-wrap18(lp);
        }
    }

    private void changeScreenHeight() {
        if (!hookCheckShowingFlags()) {
            resizeScreenLayout(true);
        }
    }

    private void setCurrentShowAnim(Animator animator) {
        this.mCurrentShowAnim = animator;
    }

    private void endCurrentShowAnim() {
        if (this.mCurrentShowAnim != null) {
            this.mCurrentShowAnim.end();
        }
    }

    private void cancelCurrentShowAnim() {
        if (this.mCurrentShowAnim != null) {
            this.mCurrentShowAnim.cancel();
        }
    }

    private ScrollingTabContainerView getTabContainer() {
        return this.mHasEmbeddedTabs ? null : this.mTabScrollView;
    }

    private DecorToolbar getDecorToolbar(View view) {
        if (view instanceof DecorToolbar) {
            return (DecorToolbar) view;
        }
        if (view instanceof Toolbar) {
            return ((Toolbar) view).getWrapper();
        }
        String simpleName = "null";
        if (!(view == null || view.getClass() == null)) {
            simpleName = view.getClass().getSimpleName();
        }
        throw new IllegalStateException("Can't make a decor toolbar out of " + simpleName);
    }

    private void finishAnimation() {
        if (this.mAnimatorSet != null) {
            this.mAnimatorSet.end();
        }
    }

    private void setAnimation(AnimatorSet set) {
        if (set != null) {
            set.addListener(this.mModeAnimatorListener);
        }
        this.mAnimatorSet = set;
    }

    public void setBackTitle(CharSequence title) {
        if (this.mActionBarView != null) {
            this.mActionBarView.setBackTitle(title);
        }
    }

    public void setHintText(CharSequence hintText) {
        if (this.mContainerView != null) {
            TextView hintTextView = (TextView) this.mContainerView.findViewById(201458958);
            if (hintTextView != null) {
                hintTextView.setText(hintText);
            }
        }
    }

    public void setBackTitle(int resId) {
        setBackTitle(this.mContext.getResources().getString(resId));
    }

    public void setHintText(int resId) {
        setHintText(this.mContext.getResources().getString(resId));
    }

    public void setActionBarSubTitle(CharSequence subtitle) {
        setSubtitle(subtitle);
    }

    public void setActionBarSubTitle(int resId) {
        setActionBarSubTitle(this.mContext.getResources().getString(resId));
    }

    public void setMainActionBar(boolean isMain) {
        if (this.mActionBarView != null) {
            this.mActionBarView.setMainActionBar(isMain);
        }
    }

    private void setStatusBarColor(int color) {
        if (this.mActivity != null) {
            Window window = this.mActivity.getWindow();
            if (window != null) {
                window.setStatusBarColor(color);
            }
        }
    }

    private View getTranslucentBgView() {
        if (this.mOverlayLayout != null) {
            return this.mOverlayLayout.findViewById(201458966);
        }
        return null;
    }

    public void addSearchViewShowListener(AnimatorListener listener) {
        this.mSearchShowListenerList.add(listener);
    }

    public void addSearchViewHideListener(AnimatorListener listener) {
        this.mSearchHideListenerList.add(listener);
    }

    public void addSearchViewWithAnimator(Animator anim) {
        this.mSearchUserWithAnimation.getAnimList().add(anim);
    }

    public void playSearchUserAnimators(Builder b, AnimatorSet set, boolean flag) {
        for (UserAnimator userAnim : this.mSearchUserAnimations) {
            Animator anim;
            List<Animator> animList = userAnim.getAnimList();
            while (!animList.isEmpty()) {
                anim = (Animator) animList.remove(animList.size() - 1);
                initAnimator(anim);
                b = playUserAnimatorInternal(b, set, anim);
                dumpAnimation(anim, userAnim);
            }
            List<ColorAnimatorWrapper> animWrapList = userAnim.getAnimWrapperList();
            while (!animWrapList.isEmpty()) {
                ColorAnimatorWrapper animWrap = (ColorAnimatorWrapper) animWrapList.remove(animWrapList.size() - 1);
                animWrap.initialize();
                anim = animWrap.getAnimation();
                b = playUserAnimatorInternal(b, set, anim);
                dumpAnimation(anim, userAnim);
            }
        }
    }

    private void dumpAnimation(Animator anim, UserAnimator userAnim) {
        if (ColorLog.getDebug(BottomMenu.ANIM)) {
            ColorAnimatorUtil.dump(BottomMenu.ANIM2, this.mTagClass, anim, "play ", userAnim.getTag());
        }
    }

    private Builder playUserAnimatorInternal(Builder b, AnimatorSet set, Animator anim) {
        if (b == null && set != null) {
            return set.play(anim);
        }
        b.with(anim);
        return b;
    }

    private void initAnimator(Animator anim) {
        if (anim instanceof ObjectAnimator) {
            Object target = ((ObjectAnimator) anim).getTarget();
            if (target instanceof View) {
                ((View) target).setVisibility(0);
            }
        }
    }

    public void onSearchAnimationStart(Animator animation, boolean show) {
        List<AnimatorListener> list = show ? this.mSearchShowListenerList : this.mSearchHideListenerList;
        if (list != null) {
            for (AnimatorListener listener : list) {
                listener.onAnimationStart(animation);
            }
        }
    }

    public void onSearchAnimationEnd(Animator animation, boolean show) {
        List<AnimatorListener> list = show ? this.mSearchShowListenerList : this.mSearchHideListenerList;
        if (list != null) {
            for (AnimatorListener listener : list) {
                listener.onAnimationEnd(animation);
            }
        }
    }

    public void onSearchAnimationCancel(Animator animation, boolean show) {
        List<AnimatorListener> list = show ? this.mSearchShowListenerList : this.mSearchHideListenerList;
        if (list != null) {
            for (AnimatorListener listener : list) {
                listener.onAnimationCancel(animation);
            }
        }
    }

    public void onSearchAnimationRepeat(Animator animation, boolean show) {
        List<AnimatorListener> list = show ? this.mSearchShowListenerList : this.mSearchHideListenerList;
        if (list != null) {
            for (AnimatorListener listener : list) {
                listener.onAnimationRepeat(animation);
            }
        }
    }

    public void setColorWindowContentOverlay(Drawable overlay) {
        if (this.mOverlayLayout != null) {
            this.mOverlayLayout.setColorWindowContentOverlay(overlay);
        }
    }

    public void setColorBottomWindowContentOverlay(Drawable overlay) {
        if (this.mOverlayLayout != null) {
            this.mOverlayLayout.setColorBottomWindowContentOverlay(overlay);
        }
    }

    public void setIgnoreColorWindowContentOverlay(boolean isIgnore) {
        if (this.mOverlayLayout != null) {
            this.mOverlayLayout.setIgnoreColorWindowContentOverlay(isIgnore);
        }
    }

    public void setIgnoreColorBottomWindowContentOverlay(boolean isIgnore) {
        if (this.mOverlayLayout != null) {
            this.mOverlayLayout.setIgnoreColorBottomWindowContentOverlay(isIgnore);
        }
    }

    public void setStatusBarActionBarBg(Drawable drawable) {
        if (this.mOverlayLayout != null) {
            this.mOverlayLayout.setStatusBarActionBarBg(drawable);
        }
    }

    public void setSplitActionBarBg(Drawable drawable) {
        if (this.mSplitView != null) {
            View splitMenuView = this.mSplitView.findViewById(201458882);
            if (splitMenuView != null) {
                splitMenuView.setBackgroundDrawable(drawable);
            }
        }
    }

    public void setSplitActionBarTextColor(ColorStateList textColor) {
        if (this.mSplitView != null) {
            ColorBottomMenuView splitMenuView = (ColorBottomMenuView) this.mSplitView.findViewById(201458882);
            if (splitMenuView != null) {
                splitMenuView.setTabTextColor(textColor);
            }
        }
    }

    public void setBackTitleTextColor(ColorStateList textColor) {
        if (this.mActionBarView != null) {
            this.mActionBarView.setBackTitleTextColor(textColor);
        }
    }

    public void setTitleTextColor(int textColor) {
        TextView titleView;
        if (this.mActionBarView != null) {
            titleView = (TextView) this.mActionBarView.findViewById(ColorContextUtil.getResId(this.mContext, 201458902));
            if (titleView != null) {
                titleView.setTextColor(textColor);
            }
            this.mActionBarView.setTitleTextColor(ColorStateList.valueOf(textColor));
        }
        if (this.mContextView != null) {
            titleView = (TextView) this.mContextView.findViewById(ColorContextUtil.getResId(this.mContext, 201458902));
            if (titleView != null) {
                titleView.setTextColor(textColor);
            }
            this.mContextView.setTitleTextColor(ColorStateList.valueOf(textColor));
        }
    }

    public void setSubtitleTextColor(int textColor) {
        TextView subtitleView;
        if (this.mActionBarView != null) {
            subtitleView = (TextView) this.mActionBarView.findViewById(ColorContextUtil.getResId(this.mContext, 201458903));
            if (subtitleView != null) {
                subtitleView.setTextColor(textColor);
            }
            this.mActionBarView.setSubitleTextColor(ColorStateList.valueOf(textColor));
        }
        if (this.mContextView != null) {
            subtitleView = (TextView) this.mContextView.findViewById(ColorContextUtil.getResId(this.mContext, 201458903));
            if (subtitleView != null) {
                subtitleView.setTextColor(textColor);
            }
            this.mContextView.setSubtitleTextColor(ColorStateList.valueOf(textColor));
        }
    }

    public void setActionMenuTextColor(ColorStateList textColor) {
        if (this.mActionBarView != null) {
            this.mActionBarView.setActionMenuTextColor(textColor);
        }
        if (this.mContextView != null) {
            this.mContextView.setActionMenuTextColor(textColor);
        }
    }
}
