package com.android.internal.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.AnimatorSet.Builder;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.ActionBar;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.Log;
import android.view.CollapsibleActionView;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ActionMenuPresenter;
import android.widget.ActionMenuView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ColorSpinner;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.view.menu.ActionMenuItemView;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.view.menu.MenuItemImpl;
import com.android.internal.view.menu.MenuPresenter.Callback;
import com.color.util.ColorChangeTextUtil;
import com.color.util.ColorContextUtil;
import com.color.util.ColorLog;
import com.color.util.ColorLogKey;
import com.color.util.ColorViewUtil;
import com.color.view.animation.ColorPathInterpolator;
import com.color.widget.ColorLoadingView;
import com.color.widget.ColorOptionMenuPresenter;
import com.color.widget.ColorOptionMenuView;
import com.color.widget.ColorSpinnerCallback;
import com.color.widget.ColorSpinnerCallback.DropdownDismissCallback;
import oppo.content.res.OppoFontUtils;

public class ColorActionBarView extends ActionBarView implements ColorSpinnerCallback {
    private static final int DEFAULT_CUSTOM_GRAVITY = 8388627;
    private static final int MAX_WIDTH_TABSCROLLVIEW = 699;
    private static final String OPPO_WIDGET_ANIM_DISABLE = "oppo.widget.animation.disabled";
    private static final String TAG = "ColorActionBarView";
    private static final float TEXT_SIZE_SCALE = 0.88f;
    private ColorStateList mActionMenuTextColor = null;
    private boolean mAnimationDisabled = true;
    private CharSequence mBackTitle = null;
    private DropdownDismissCallback mDropdownDismissCallback = null;
    private OnItemClickListener mDropdownItemClickListener = null;
    private boolean mDropdownUpdateAfterAnim = false;
    private int mHeightMeasureSpec = -1;
    private int mHintTextPadding = 0;
    private float mHintTextSize = 0.0f;
    private int mIdActionBarSpinner = -1;
    private int mIdActionMenuPresenter = -1;
    private int mIdHomeAsUp = -1;
    private int mIdProgressCircular = -1;
    private int mIdTitle = -1;
    private boolean mInLayout = false;
    private boolean mInMeasure = false;
    private int mIndeterminateProgressStyle;
    private boolean mIsMainActionBar = false;
    private boolean mIsShrinkOnce = false;
    private boolean mIsShrinkTwice = false;
    private int mLayoutBottom = -1;
    private int mLayoutLeft = -1;
    private int mLayoutRight = -1;
    private int mLayoutTop = -1;
    private float mMainTitleSize = 0.0f;
    private Typeface mMediumTypeface = null;
    private float mMenuItemTextSize = 0.0f;
    private int mMenuViewMaxWidth = 0;
    private float mNormalTitleSize = 0.0f;
    private int mProgressMarginRight = 0;
    private float mSubTitleTextSize = 0.0f;
    private ColorStateList mSubtitleTextColor = null;
    private int mTabScrollViewMaxWidth = 699;
    protected final Class<?> mTagClass = getClass();
    private int mTitleLayoutPadding = 0;
    private int mTitleMaxWidth = 0;
    private ColorStateList mTitleTextColor = null;
    private float mTitleTextSize = 0.0f;
    private boolean mWasHomeEnabled = false;
    private int mWidthMeasureSpec = -1;

    static class ColorHomeView extends HomeView {
        private TextView mBackTitleView;
        private CharSequence mDefaultBackTitleText;
        private ImageView mIconView;
        private int mStartOffset;
        private int mTitlePaddingLeft;
        private ImageView mUpView;
        private int mUpWidth;

        public ColorHomeView(Context context) {
            this(context, null);
        }

        public ColorHomeView(Context context, AttributeSet attrs) {
            super(context, attrs);
            this.mTitlePaddingLeft = 0;
            this.mBackTitleView = null;
            this.mDefaultBackTitleText = null;
            if (isOppoStyle()) {
                this.mTitlePaddingLeft = getResources().getDimensionPixelSize(201655322);
                this.mDefaultBackTitleText = getResources().getString(201590122);
            }
        }

        public int getUpWidth() {
            if (isOppoStyle()) {
                return this.mTitlePaddingLeft;
            }
            return super.getUpWidth();
        }

        public CharSequence getDefaultBackTitleText() {
            return this.mDefaultBackTitleText;
        }

        public void setShowUp(boolean isUp) {
            super.setShowUp(isUp);
            if (isOppoStyle()) {
                this.mBackTitleView.setVisibility(isUp ? 0 : 8);
            }
        }

        public void setShowIcon(boolean showIcon) {
            super.setShowIcon(showIcon);
            if (isOppoStyle()) {
                this.mIconView.setVisibility(8);
            }
        }

        public View getUpView() {
            return this.mUpView;
        }

        public View getIconView() {
            return this.mIconView;
        }

        public View getBackTitleView() {
            return this.mBackTitleView;
        }

        protected void onFinishInflate() {
            super.onFinishInflate();
            if (isOppoStyle()) {
                this.mUpView = (ImageView) findViewById(R.id.up);
                this.mIconView = (ImageView) findViewById(R.id.home);
                this.mBackTitleView = new TextView(getContext(), null, 201392685);
                this.mBackTitleView.setId(201458956);
                this.mBackTitleView.setSingleLine(true);
                this.mBackTitleView.setEllipsize(TruncateAt.END);
                LayoutParams lp = new LayoutParams(-2, -2);
                lp.gravity = 16;
                this.mBackTitleView.-wrap18(lp);
            }
        }

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            if (isOppoStyle()) {
                measureChildWithMargins(this.mUpView, widthMeasureSpec, 0, heightMeasureSpec, 0);
                LayoutParams upLp = (LayoutParams) this.mUpView.getLayoutParams();
                int upMargins = upLp.leftMargin + upLp.rightMargin;
                this.mUpWidth = this.mUpView.getMeasuredWidth();
                this.mStartOffset = this.mUpWidth + upMargins;
                int width = this.mUpView.getVisibility() == 8 ? 0 : this.mStartOffset;
                int height = (upLp.topMargin + this.mUpView.getMeasuredHeight()) + upLp.bottomMargin;
                if (this.mIconView.getVisibility() != 8) {
                    measureChildWithMargins(this.mIconView, widthMeasureSpec, width, heightMeasureSpec, 0);
                    LayoutParams iconLp = (LayoutParams) this.mIconView.getLayoutParams();
                    width += (iconLp.leftMargin + this.mIconView.getMeasuredWidth()) + iconLp.rightMargin;
                    height = Math.max(height, (iconLp.topMargin + this.mIconView.getMeasuredHeight()) + iconLp.bottomMargin);
                } else if (upMargins < 0) {
                    width -= upMargins;
                }
                int widthMode = MeasureSpec.getMode(widthMeasureSpec);
                int heightMode = MeasureSpec.getMode(heightMeasureSpec);
                int widthSize = MeasureSpec.getSize(widthMeasureSpec);
                int heightSize = MeasureSpec.getSize(heightMeasureSpec);
                switch (widthMode) {
                    case Integer.MIN_VALUE:
                        width = Math.min(width, widthSize);
                        break;
                    case 1073741824:
                        width = widthSize;
                        break;
                }
                switch (heightMode) {
                    case Integer.MIN_VALUE:
                        height = Math.min(height, heightSize);
                        break;
                    case 1073741824:
                        height = heightSize;
                        break;
                }
                -wrap6(width, height);
                return;
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            if (isOppoStyle()) {
                int iconRight;
                int iconLeft;
                int vCenter = (b - t) / 2;
                boolean isLayoutRtl = isLayoutRtl();
                int width = getWidth();
                int upOffset = 0;
                if (this.mUpView.getVisibility() != 8) {
                    int upRight;
                    int upLeft;
                    LayoutParams upLp = (LayoutParams) this.mUpView.getLayoutParams();
                    int upHeight = this.mUpView.getMeasuredHeight();
                    int upWidth = this.mUpView.getMeasuredWidth();
                    upOffset = (upLp.leftMargin + upWidth) + upLp.rightMargin;
                    int upTop = vCenter - (upHeight / 2);
                    int upBottom = upTop + upHeight;
                    if (isLayoutRtl) {
                        upRight = width;
                        upLeft = width - upWidth;
                        r -= upOffset;
                    } else {
                        upRight = upWidth;
                        upLeft = 0;
                        l += upOffset;
                    }
                    this.mUpView.layout(upLeft, upTop, upRight, upBottom);
                }
                LayoutParams iconLp = (LayoutParams) this.mIconView.getLayoutParams();
                int iconHeight = this.mIconView.getMeasuredHeight();
                int iconWidth = this.mIconView.getMeasuredWidth();
                int iconTop = Math.max(iconLp.topMargin, vCenter - (iconHeight / 2));
                int iconBottom = iconTop + iconHeight;
                if (isLayoutRtl) {
                    iconRight = width - upOffset;
                    iconLeft = iconRight - iconWidth;
                } else {
                    iconLeft = upOffset;
                    iconRight = upOffset + iconWidth;
                }
                this.mIconView.layout(iconLeft, iconTop, iconRight, iconBottom);
                return;
            }
            super.onLayout(changed, l, t, r, b);
        }

        public void setBackTitle(CharSequence title) {
            this.mBackTitleView.setTextSize(0, (float) getResources().getDimensionPixelSize(((double) getResources().getConfiguration().fontScale) <= 1.0d ? 201654416 : 201654420));
            this.mBackTitleView.setText(title);
        }

        public void setBackTitleTextColor(ColorStateList textColor) {
            this.mBackTitleView.setTextColor(textColor);
        }
    }

    class ExpandedSearchActionViewMenuPresenter extends ExpandedActionViewMenuPresenter {
        private static final long ANIM_DURATION = 300;
        private static final int DISPLAY_HOME_FLAGS = 6;
        private static final float MIN_WIDTH_FRACTION = 0.1f;
        private final Interpolator ANIM_INTERPOLATOR = ColorPathInterpolator.create();
        private final ActionBar.LayoutParams LAYOUT_PARAMS = new ActionBar.LayoutParams(8388629);
        private int mContentWidth = 0;
        private AnimatorSet mSearchActionViewAnim = null;
        private int mTitleLeft = 0;

        ExpandedSearchActionViewMenuPresenter() {
            super();
        }

        public boolean expandItemActionView(MenuBuilder menu, MenuItemImpl item) {
            ColorActionBarView.this.mExpandedActionView = item.getActionView();
            if (!(ColorActionBarView.this.mExpandedActionView instanceof SearchView) || !ColorActionBarView.this.isOppoStyle()) {
                return super.expandItemActionView(menu, item);
            }
            if (this.mSearchActionViewAnim != null) {
                this.mSearchActionViewAnim.end();
            }
            this.mContentWidth = ColorActionBarView.this.getMeasuredWidth();
            ColorActionBarView.this.mExpandedHomeLayout.setIcon(null);
            this.mCurrentExpandedItem = item;
            if (ColorActionBarView.this.mExpandedActionView.getParent() != ColorActionBarView.this) {
                ColorActionBarView.this.addView(ColorActionBarView.this.mExpandedActionView, this.LAYOUT_PARAMS);
            }
            if (ColorActionBarView.this.mExpandedHomeLayout.getParent() != ColorActionBarView.this.mUpGoerFive) {
                ColorActionBarView.this.mUpGoerFive.addView(ColorActionBarView.this.mExpandedHomeLayout);
            }
            ColorActionBarView.this.mHomeLayout.setVisibility(8);
            if (ColorActionBarView.this.mTabScrollView != null) {
                ColorActionBarView.this.mTabScrollView.setVisibility(8);
            }
            if (ColorActionBarView.this.mSpinner != null) {
                ColorActionBarView.this.mSpinner.setVisibility(8);
            }
            View customNavView = ColorActionBarView.this.getCustomView();
            if (customNavView != null) {
                customNavView.setVisibility(8);
            }
            ColorActionBarView.this.setHomeButtonEnabled(false, false);
            ColorActionBarView.this.requestLayout();
            item.setActionViewExpanded(true);
            if (ColorActionBarView.this.mExpandedActionView instanceof CollapsibleActionView) {
                ((CollapsibleActionView) ColorActionBarView.this.mExpandedActionView).onActionViewExpanded();
            }
            Animator animSearchView = getSearchViewAnimation(true);
            Animator animExpandedHome = getExpandedHomeAnimation(true);
            Animator animTitleLayout = getTitleLayoutAnimation(true);
            this.mSearchActionViewAnim = playAnimators(animSearchView, animExpandedHome, animTitleLayout);
            this.mSearchActionViewAnim.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    ExpandedSearchActionViewMenuPresenter.this.mSearchActionViewAnim = null;
                }
            });
            this.mSearchActionViewAnim.start();
            return true;
        }

        public boolean collapseItemActionView(MenuBuilder menu, final MenuItemImpl item) {
            if (!(ColorActionBarView.this.mExpandedActionView instanceof SearchView) || !ColorActionBarView.this.isOppoStyle()) {
                return super.collapseItemActionView(menu, item);
            }
            if (this.mSearchActionViewAnim != null) {
                this.mSearchActionViewAnim.end();
            }
            Animator animSearchView = getSearchViewAnimation(false);
            Animator animExpandedHome = getExpandedHomeAnimation(false);
            Animator animTitleLayout = getTitleLayoutAnimation(false);
            Animator animMenuView = getMenuViewAnimation(false);
            this.mSearchActionViewAnim = playAnimators(animSearchView, animExpandedHome, animTitleLayout, animMenuView);
            this.mSearchActionViewAnim.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    ExpandedSearchActionViewMenuPresenter.this.mSearchActionViewAnim = null;
                    if (ColorActionBarView.this.mExpandedActionView instanceof CollapsibleActionView) {
                        ((CollapsibleActionView) ColorActionBarView.this.mExpandedActionView).onActionViewCollapsed();
                    }
                    ColorActionBarView.this.removeView(ColorActionBarView.this.mExpandedActionView);
                    ColorActionBarView.this.mUpGoerFive.removeView(ColorActionBarView.this.mExpandedHomeLayout);
                    ColorActionBarView.this.mExpandedActionView = null;
                    if ((ColorActionBarView.this.getDisplayOptions() & 6) != 0) {
                        ColorActionBarView.this.mHomeLayout.setVisibility(0);
                    }
                    if (ColorActionBarView.this.mTabScrollView != null) {
                        ColorActionBarView.this.mTabScrollView.setVisibility(0);
                    }
                    if (ColorActionBarView.this.mSpinner != null) {
                        ColorActionBarView.this.mSpinner.setVisibility(0);
                    }
                    View customNavView = ColorActionBarView.this.getCustomView();
                    if (customNavView != null) {
                        customNavView.setVisibility(0);
                    }
                    ColorActionBarView.this.mExpandedHomeLayout.setIcon(null);
                    ExpandedSearchActionViewMenuPresenter.this.mCurrentExpandedItem = null;
                    ColorActionBarView.this.setHomeButtonEnabled(ColorActionBarView.this.mWasHomeEnabled);
                    ColorActionBarView.this.requestLayout();
                    item.setActionViewExpanded(false);
                }
            });
            this.mSearchActionViewAnim.start();
            return true;
        }

        private AnimatorSet playAnimators(Animator... animators) {
            AnimatorSet animSet = new AnimatorSet();
            Builder builder = null;
            for (Animator anim : animators) {
                if (anim != null) {
                    if (builder == null) {
                        builder = animSet.play(anim);
                    } else {
                        builder.with(anim);
                    }
                }
            }
            animSet.setDuration(ANIM_DURATION);
            animSet.setInterpolator(this.ANIM_INTERPOLATOR);
            return animSet;
        }

        private Animator getSearchViewAnimation(boolean isExpand) {
            ObjectAnimator animSearchView;
            if (isExpand) {
                ViewGroup.LayoutParams lp = ColorActionBarView.this.mExpandedActionView.getLayoutParams();
                lp.width = 0;
                ColorActionBarView.this.mExpandedActionView.-wrap18(lp);
                animSearchView = ObjectAnimator.ofFloat(ColorActionBarView.this.mExpandedActionView, "alpha", new float[]{0.0f, 1.0f});
                animSearchView.addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animation) {
                        ViewGroup.LayoutParams lp = ColorActionBarView.this.mExpandedActionView.getLayoutParams();
                        if (lp != null) {
                            float value = ((Float) animation.getAnimatedValue()).floatValue();
                            if (value > ExpandedSearchActionViewMenuPresenter.MIN_WIDTH_FRACTION && value <= 1.0f) {
                                lp.width = (int) (((float) ExpandedSearchActionViewMenuPresenter.this.mContentWidth) * value);
                                ColorActionBarView.this.mExpandedActionView.-wrap18(lp);
                            }
                        }
                    }
                });
                animSearchView.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        ViewGroup.LayoutParams lp = ColorActionBarView.this.mExpandedActionView.getLayoutParams();
                        lp.width = (int) (((float) ExpandedSearchActionViewMenuPresenter.this.mContentWidth) * 1.0f);
                        ColorActionBarView.this.mExpandedActionView.-wrap18(lp);
                        ColorActionBarView.this.mExpandedActionView.setAlpha(1.0f);
                    }
                });
                return animSearchView;
            }
            final float widthEnd = ((float) ColorActionBarView.this.mExpandedActionView.getMeasuredWidth()) * MIN_WIDTH_FRACTION;
            animSearchView = ObjectAnimator.ofFloat(ColorActionBarView.this.mExpandedActionView, "width", new float[]{(float) ColorActionBarView.this.mExpandedActionView.getMeasuredWidth(), widthEnd});
            animSearchView.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    ViewGroup.LayoutParams lp = ColorActionBarView.this.mExpandedActionView.getLayoutParams();
                    lp.width = (int) ((Float) animation.getAnimatedValue()).floatValue();
                    ColorActionBarView.this.mExpandedActionView.-wrap18(lp);
                    ColorActionBarView.this.mExpandedActionView.setAlpha(1.0f - animation.getAnimatedFraction());
                }
            });
            animSearchView.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    ViewGroup.LayoutParams lp = ColorActionBarView.this.mExpandedActionView.getLayoutParams();
                    if (lp != null) {
                        lp.width = (int) widthEnd;
                        ColorActionBarView.this.mExpandedActionView.-wrap18(lp);
                    }
                    ColorActionBarView.this.mExpandedActionView.setAlpha(0.0f);
                }
            });
            return animSearchView;
        }

        private Animator getExpandedHomeAnimation(boolean isExpand) {
            if ((ColorActionBarView.this.getDisplayOptions() & 6) != 0) {
                return null;
            }
            int width = ColorActionBarView.this.mExpandedHomeLayout.getWidth();
            ObjectAnimator animExpandedHome;
            if (isExpand) {
                if (width == 0) {
                    ColorActionBarView.this.mExpandedHomeLayout.measure(ColorActionBarView.this.makeUnspecifiedMeasureSpec(), ColorActionBarView.this.makeUnspecifiedMeasureSpec());
                    width = ColorActionBarView.this.mExpandedHomeLayout.getMeasuredWidth();
                }
                float translationXStart = (float) (-width);
                animExpandedHome = ObjectAnimator.ofFloat(ColorActionBarView.this.mExpandedHomeLayout, "translationX", new float[]{translationXStart, 0.0f});
                animExpandedHome.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        ColorActionBarView.this.mExpandedHomeLayout.setTranslationX(0.0f);
                    }
                });
                return animExpandedHome;
            }
            final float translationXEnd = (float) (-width);
            animExpandedHome = ObjectAnimator.ofFloat(ColorActionBarView.this.mExpandedHomeLayout, "translationX", new float[]{0.0f, translationXEnd});
            animExpandedHome.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    ColorActionBarView.this.mExpandedHomeLayout.setTranslationX(translationXEnd);
                }
            });
            return animExpandedHome;
        }

        private Animator getTitleLayoutAnimation(boolean isExpand) {
            ObjectAnimator animAlpha;
            ObjectAnimator animX;
            AnimatorSet animSet;
            if (isExpand) {
                if (ColorActionBarView.this.mTitleLayout == null) {
                    return null;
                }
                this.mTitleLeft = ColorActionBarView.this.mTitleLayout.getLeft();
                animAlpha = ObjectAnimator.ofFloat(ColorActionBarView.this.mTitleLayout, "alpha", new float[]{1.0f, 0.0f});
                float xStart = (float) this.mTitleLeft;
                animX = ObjectAnimator.ofFloat(ColorActionBarView.this.mTitleLayout, "x", new float[]{xStart, 0.0f});
                animSet = new AnimatorSet();
                animSet.playTogether(new Animator[]{animAlpha, animX});
                animSet.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        ColorActionBarView.this.mTitleLayout.setAlpha(0.0f);
                        ColorActionBarView.this.mTitleLayout.setX(0.0f);
                        ColorActionBarView.this.mTitleLayout.setVisibility(8);
                    }
                });
                return animSet;
            } else if ((ColorActionBarView.this.getDisplayOptions() & 8) == 0) {
                return null;
            } else {
                if (ColorActionBarView.this.mTitleLayout == null) {
                    ColorActionBarView.this.initTitle();
                    return null;
                }
                ColorActionBarView.this.mTitleLayout.setVisibility(0);
                animAlpha = ObjectAnimator.ofFloat(ColorActionBarView.this.mTitleLayout, "alpha", new float[]{0.0f, 1.0f});
                final float xEnd = (float) this.mTitleLeft;
                animX = ObjectAnimator.ofFloat(ColorActionBarView.this.mTitleLayout, "x", new float[]{0.0f, xEnd});
                animSet = new AnimatorSet();
                animSet.playTogether(new Animator[]{animAlpha, animX});
                animSet.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        ColorActionBarView.this.mTitleLayout.setAlpha(1.0f);
                        ColorActionBarView.this.mTitleLayout.setX(xEnd);
                    }
                });
                return animSet;
            }
        }

        private Animator getMenuViewAnimation(boolean isExpand) {
            if (ColorActionBarView.this.mMenuView == null || (isExpand ^ 1) == 0) {
                return null;
            }
            ObjectAnimator animAlpha = ObjectAnimator.ofFloat(ColorActionBarView.this.mMenuView, "alpha", new float[]{0.0f, 1.0f});
            animAlpha.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    ColorActionBarView.this.mMenuView.setAlpha(1.0f);
                }
            });
            return animAlpha;
        }
    }

    public ColorActionBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ColorLog.d(ColorLogKey.ActionBar.DISP, this.mTagClass, TAG);
        if (isOppoStyle()) {
            this.mIdTitle = ColorContextUtil.getResId(context, 201458902);
            this.mIdHomeAsUp = ColorContextUtil.getResId(context, 201458864);
            this.mIdActionMenuPresenter = ColorContextUtil.getResId(context, 201458897);
            this.mIdActionBarSpinner = ColorContextUtil.getResId(context, 201458898);
            this.mIdProgressCircular = ColorContextUtil.getResId(context, 201458688);
            this.mProgressMarginRight = getResources().getDimensionPixelSize(201655464);
            this.mUpGoerFive.setOnClickListener(null);
            this.mHomeLayout.setOnClickListener(this.mUpClickListener);
            this.mHomeLayout.setId(201458692);
            this.mExpandedHomeLayout.setId(201458693);
            TypedArray a = context.obtainStyledAttributes(attrs, android.R.styleable.ActionBar, R.attr.actionBarStyle, 0);
            this.mIndeterminateProgressStyle = a.getResourceId(14, 0);
            a.recycle();
            this.mTabScrollViewMaxWidth = getResources().getDimensionPixelSize(201655470);
            this.mTitleLayoutPadding = getResources().getDimensionPixelSize(201655322);
            this.mHintTextPadding = getResources().getDimensionPixelSize(201655556);
            this.mMenuViewMaxWidth = getResources().getDimensionPixelSize(201655557);
            this.mMenuItemTextSize = (float) getResources().getDimensionPixelSize(201654415);
            this.mNormalTitleSize = (float) getResources().getDimensionPixelSize(201654417);
            this.mSubTitleTextSize = (float) getResources().getDimensionPixelSize(201654407);
            this.mMainTitleSize = (float) getResources().getDimensionPixelSize(201654421);
            float fontScale = getResources().getConfiguration().fontScale;
            this.mMenuItemTextSize = ColorChangeTextUtil.getSuitableFontSize(this.mMenuItemTextSize, fontScale, 2);
            this.mNormalTitleSize = ColorChangeTextUtil.getSuitableFontSize(this.mNormalTitleSize, fontScale, 2);
            this.mSubTitleTextSize = ColorChangeTextUtil.getSuitableFontSize(this.mSubTitleTextSize, fontScale, 2);
            this.mMainTitleSize = ColorChangeTextUtil.getSuitableFontSize(this.mMainTitleSize, fontScale, 2);
            this.mTitleTextSize = this.mNormalTitleSize;
            this.mHintTextSize = this.mSubTitleTextSize;
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isOppoStyle()) {
            this.mInMeasure = true;
            this.mWidthMeasureSpec = widthMeasureSpec;
            this.mHeightMeasureSpec = heightMeasureSpec;
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            this.mInMeasure = false;
            View customView = getCustomView();
            if (customView != null) {
                ViewGroup.LayoutParams lp = generateLayoutParams(customView.getLayoutParams());
                ActionBar.LayoutParams ablp = lp instanceof ActionBar.LayoutParams ? (ActionBar.LayoutParams) lp : null;
                int horizontalMargin = 0;
                int verticalMargin = 0;
                if (ablp != null) {
                    horizontalMargin = ablp.leftMargin + ablp.rightMargin;
                    verticalMargin = ablp.topMargin + ablp.bottomMargin;
                }
                int customNavHeightMode = this.mContentHeight <= 0 ? Integer.MIN_VALUE : lp.height != -2 ? 1073741824 : Integer.MIN_VALUE;
                int height = (this.mContentHeight >= 0 ? this.mContentHeight : MeasureSpec.getSize(heightMeasureSpec)) - (getPaddingTop() + getPaddingBottom());
                int width = MeasureSpec.getSize(widthMeasureSpec);
                if (lp.height >= 0) {
                    height = Math.min(lp.height, height);
                }
                int customNavHeight = Math.max(0, height - verticalMargin);
                int customNavWidthMode = lp.width != -2 ? 1073741824 : Integer.MIN_VALUE;
                if (lp.width >= 0) {
                    width = Math.min(lp.width, width);
                }
                customView.measure(MeasureSpec.makeMeasureSpec(Math.max(0, width - horizontalMargin), customNavWidthMode), MeasureSpec.makeMeasureSpec(customNavHeight, customNavHeightMode));
            }
            if (this.mMenuView != null) {
                setMenuViewTextSize();
                measureMenuView();
            }
            if (this.mUpGoerFive != null) {
                ColorHomeView homeView;
                if (this.mTitleLayout != null) {
                    this.mHintTextPadding = getResources().getDimensionPixelSize(201655556);
                    int upWidth = 0;
                    if (this.mHomeLayout != null) {
                        homeView = (ColorHomeView) this.mHomeLayout;
                        homeView.setBackTitle(null);
                        if (this.mIsMainActionBar) {
                            homeView.setVisibility(8);
                        }
                        if (homeView.getVisibility() == 0) {
                            View upView = homeView.getUpView();
                            if (upView != null) {
                                upWidth = upView.getMeasuredWidth();
                                this.mHintTextPadding = upWidth;
                            }
                        }
                    }
                    int space = 0;
                    if (this.mMenuView != null) {
                        space = (this.mMenuView.getPaddingLeft() > this.mMenuView.getPaddingRight() ? this.mMenuView.getPaddingLeft() : this.mMenuView.getPaddingRight()) + 0;
                        int childCount = this.mMenuView.getChildCount();
                        int itemPadding = getResources().getDimensionPixelSize(201655498);
                        if (this.mIsMainActionBar || isUpViewVisible()) {
                            if (childCount != 0) {
                                if (childCount == 1) {
                                    space += this.mMenuView.getChildAt(0).getMeasuredWidth();
                                } else if (childCount == 2) {
                                    space += (this.mMenuView.getChildAt(0).getMeasuredWidth() + this.mMenuView.getChildAt(1).getMeasuredWidth()) + itemPadding;
                                } else if (childCount == 3) {
                                    space += (itemPadding * 2) + ((this.mMenuView.getChildAt(0).getMeasuredWidth() + this.mMenuView.getChildAt(1).getMeasuredWidth()) + this.mMenuView.getChildAt(2).getMeasuredWidth());
                                } else {
                                    space = (getWidthSize() - getResources().getDimensionPixelSize(201655445)) / 2;
                                }
                            }
                            space += upWidth;
                        } else {
                            if (childCount != 0) {
                                int childOnLeft;
                                int childOnRight;
                                if (childCount == 1) {
                                    space += this.mMenuView.getChildAt(0).getMeasuredWidth();
                                } else if (childCount == 2) {
                                    childOnLeft = this.mMenuView.getChildAt(0).getMeasuredWidth();
                                    childOnRight = this.mMenuView.getChildAt(1).getMeasuredWidth();
                                    if (childOnLeft <= childOnRight) {
                                        childOnLeft = childOnRight;
                                    }
                                    space += childOnLeft;
                                } else if (childCount == 3) {
                                    childOnLeft = this.mMenuView.getChildAt(0).getMeasuredWidth();
                                    childOnRight = this.mMenuView.getChildAt(1).getMeasuredWidth() + this.mMenuView.getChildAt(2).getMeasuredWidth();
                                    if (childOnLeft <= childOnRight) {
                                        childOnLeft = childOnRight;
                                    }
                                    space = (space + childOnLeft) + itemPadding;
                                } else if (childCount == 4) {
                                    childOnLeft = this.mMenuView.getChildAt(0).getMeasuredWidth() + this.mMenuView.getChildAt(1).getMeasuredWidth();
                                    childOnRight = this.mMenuView.getChildAt(2).getMeasuredWidth() + this.mMenuView.getChildAt(3).getMeasuredWidth();
                                    if (childOnLeft <= childOnRight) {
                                        childOnLeft = childOnRight;
                                    }
                                    space = (space + childOnLeft) + itemPadding;
                                } else if (childCount == 5) {
                                    childOnLeft = this.mMenuView.getChildAt(0).getMeasuredWidth() + this.mMenuView.getChildAt(1).getMeasuredWidth();
                                    childOnRight = (this.mMenuView.getChildAt(2).getMeasuredWidth() + this.mMenuView.getChildAt(3).getMeasuredWidth()) + this.mMenuView.getChildAt(4).getMeasuredWidth();
                                    if (childOnLeft <= childOnRight) {
                                        childOnLeft = childOnRight;
                                    }
                                    space = (space + childOnLeft) + (itemPadding * 2);
                                } else {
                                    space = (getWidthSize() - getResources().getDimensionPixelSize(201655445)) / 2;
                                }
                            }
                            if (space <= upWidth) {
                                space = upWidth;
                            }
                            space *= 2;
                        }
                    }
                    if (this.mIsMainActionBar) {
                        this.mTitleTextSize = this.mMainTitleSize;
                    } else {
                        this.mTitleTextSize = this.mNormalTitleSize;
                    }
                    int widthSize = getWidthSize() - space;
                    this.mTitleMaxWidth = widthSize;
                    setTitleLayoutTextSize();
                    if (this.mIsMainActionBar) {
                        this.mTitleLayout.setPadding(this.mTitleLayoutPadding, 0, this.mTitleLayoutPadding, 0);
                    } else if (isUpViewVisible()) {
                        this.mTitleLayout.setPaddingRelative(0, 0, this.mTitleLayoutPadding, 0);
                    }
                    this.mTitleLayout.measure(makeAtMostMeasureSpec(widthSize), makeUnspecifiedMeasureSpec());
                }
                if (hasEmbeddedTabs() && this.mTabScrollView != null && this.mTabScrollView.getVisibility() == 0) {
                    homeView = (ColorHomeView) this.mHomeLayout;
                    if (homeView != null) {
                        if (this.mIsMainActionBar) {
                            homeView.setVisibility(8);
                        }
                        homeView.setBackTitle(null);
                    }
                }
            }
            return;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (isOppoStyle()) {
            this.mInLayout = true;
            this.mLayoutLeft = l;
            this.mLayoutRight = r;
            this.mLayoutTop = t;
            this.mLayoutBottom = b;
            super.onLayout(changed, l, t, r, b);
            this.mInLayout = false;
            if (this.mMenuView != null) {
                positionMenuView();
            }
            if (this.mUpGoerFive != null) {
                positioUpGoerFive();
            }
            return;
        }
        super.onLayout(changed, l, t, r, b);
    }

    protected int measureChildView(View child, int availableWidth, int childSpecHeight, int spacing) {
        if (this.mInMeasure && child == this.mTabScrollView) {
            return measureTabScrollView(availableWidth, childSpecHeight);
        }
        if (!isOppoStyle() || (this.mInMeasure ^ 1) != 0) {
            return super.measureChildView(child, availableWidth, childSpecHeight, spacing);
        }
        if (child == this.mMenuView) {
            return availableWidth;
        }
        if (child != this.mUpGoerFive) {
            return super.measureChildView(child, availableWidth, childSpecHeight, spacing);
        }
        child.measure(makeExactlyMeasureSpec(getWidthSize()), makeExactlyMeasureSpec(getHeightSize()));
        return availableWidth;
    }

    protected int positionChild(View child, int x, int y, int contentHeight, boolean reverse) {
        if (!isOppoStyle() || (this.mInLayout ^ 1) != 0) {
            return super.positionChild(child, x, y, contentHeight, reverse);
        }
        if (child == this.mMenuView || child == this.mUpGoerFive) {
            return x;
        }
        if (child == this.mTabScrollView) {
            return positionChildCenterHorizontal(child, x, y, contentHeight, reverse);
        }
        if (child == this.mListNavLayout) {
            int listNavX;
            if (getMainActionBar() || isUpViewVisible()) {
                listNavX = positionChildLeft(child, x, y, contentHeight, reverse);
            } else {
                listNavX = positionChildCenterHorizontal(child, x, y, contentHeight, reverse);
            }
            return listNavX;
        } else if (child == this.mIndeterminateProgressView) {
            return super.positionChild(child, this.mLayoutRight - this.mProgressMarginRight, y, contentHeight, reverse);
        } else {
            return super.positionChild(child, x, y, contentHeight, reverse);
        }
    }

    public void initIndeterminateProgress() {
        if (isOppoStyle()) {
            this.mIndeterminateProgressView = new ColorLoadingView(getContext(), null, 0, this.mIndeterminateProgressStyle);
            this.mIndeterminateProgressView.setId(this.mIdProgressCircular);
            this.mIndeterminateProgressView.setVisibility(8);
            addView(this.mIndeterminateProgressView);
            return;
        }
        super.initIndeterminateProgress();
    }

    public Animator setupAnimatorToVisibility(int visibility, long duration) {
        Animator anim = super.setupAnimatorToVisibility(visibility, duration);
        if (isOppoStyle() && this.mOptionMenuPresenter != null && this.mOptionsMenu != null && visibility == 0) {
            this.mOptionMenuPresenter.initForMenu(this.mOptionsMenu.getContext(), this.mOptionsMenu);
        }
        return anim;
    }

    public void setMenu(Menu menu, Callback cb) {
        if (!isOppoStyle()) {
            super.setMenu(menu, cb);
        } else if (menu != this.mOptionsMenu) {
            ViewGroup oldParent;
            if (this.mOptionsMenu != null) {
                this.mOptionsMenu.removeMenuPresenter(this.mActionMenuPresenter);
                this.mOptionsMenu.removeMenuPresenter(this.mExpandedMenuPresenter);
                this.mOptionsMenu.removeMenuPresenter(this.mOptionMenuPresenter);
            }
            this.mOptionsMenu = (MenuBuilder) menu;
            if (this.mOptionMenuView != null) {
                oldParent = (ViewGroup) this.mOptionMenuView.getParent();
                if (oldParent != null) {
                    oldParent.removeView(this.mOptionMenuView);
                }
            }
            if (this.mMenuView != null) {
                oldParent = (ViewGroup) this.mMenuView.getParent();
                if (oldParent != null) {
                    oldParent.removeView(this.mMenuView);
                }
            }
            if (this.mActionMenuPresenter == null) {
                this.mActionMenuPresenter = new ActionMenuPresenter(getContext());
                this.mActionMenuPresenter.setCallback(cb);
                this.mActionMenuPresenter.setId(this.mIdActionMenuPresenter);
                this.mExpandedMenuPresenter = new ExpandedSearchActionViewMenuPresenter();
            }
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(-2, -1);
            this.mActionMenuPresenter.setExpandedActionViewsExclusive(getResources().getBoolean(202114058));
            configPresenters(this.mOptionsMenu);
            ActionMenuView menuView = (ActionMenuView) this.mActionMenuPresenter.getMenuView(this);
            oldParent = (ViewGroup) menuView.getParent();
            if (!(oldParent == null || oldParent == this)) {
                oldParent.removeView(menuView);
            }
            addView((View) menuView, layoutParams);
            this.mMenuView = menuView;
            if (this.mSplitActionBar) {
                if (this.mOptionMenuPresenter == null) {
                    this.mOptionMenuPresenter = new ColorOptionMenuPresenter(getContext());
                    this.mOptionMenuPresenter.setCallback(cb);
                }
                if (this.mOptionsMenu != null) {
                    this.mOptionsMenu.addMenuPresenter(this.mOptionMenuPresenter, this.mPopupContext);
                } else {
                    this.mOptionMenuPresenter.initForMenu(this.mPopupContext, null);
                    this.mOptionMenuPresenter.updateMenuView(true);
                }
                View optionMenuView = (ColorOptionMenuView) this.mOptionMenuPresenter.getMenuView(this.mSplitView);
                layoutParams.width = -1;
                layoutParams.height = -2;
                if (this.mSplitView != null) {
                    ViewGroup oldSplitParent = (ViewGroup) optionMenuView.getParent();
                    if (!(oldSplitParent == null || oldSplitParent == this.mSplitView)) {
                        oldSplitParent.removeView(optionMenuView);
                    }
                    this.mSplitView.addView(optionMenuView, layoutParams);
                } else {
                    optionMenuView.setLayoutParams(layoutParams);
                }
                this.mOptionMenuView = optionMenuView;
            }
            if (this.mActionMenuTextColor != null) {
                setActionMenuTextColor(this.mActionMenuTextColor);
            }
        }
    }

    public void setSplitToolbar(boolean splitActionBar) {
        if (isOppoStyle()) {
            if (!this.mSplitActionBar) {
                ViewGroup oldParent;
                if (this.mOptionMenuView != null) {
                    oldParent = (ViewGroup) this.mOptionMenuView.getParent();
                    if (oldParent != null) {
                        oldParent.removeView(this.mOptionMenuView);
                    }
                    if (this.mSplitView != null) {
                        this.mSplitView.addView(this.mOptionMenuView);
                    }
                    this.mOptionMenuView.getLayoutParams().width = -1;
                }
                if (this.mMenuView != null) {
                    oldParent = (ViewGroup) this.mMenuView.getParent();
                    if (oldParent != null) {
                        oldParent.removeView(this.mMenuView);
                    }
                    if (isOppoStyle()) {
                        addView(this.mMenuView);
                        this.mMenuView.getLayoutParams().width = -2;
                    } else {
                        if (this.mSplitView != null) {
                            this.mSplitView.addView(this.mMenuView);
                        }
                        this.mMenuView.getLayoutParams().width = -1;
                    }
                    this.mMenuView.requestLayout();
                }
                ActionMenuView tempMenuView = this.mMenuView;
                this.mMenuView = null;
                super.setSplitToolbar(true);
                this.mMenuView = tempMenuView;
            }
            return;
        }
        super.setSplitToolbar(splitActionBar);
    }

    public void setEmbeddedTabView(ScrollingTabContainerView tabs) {
        super.setEmbeddedTabView(tabs);
        if (isOppoStyle()) {
            updateTitleVisibility();
        }
    }

    public void setNavigationMode(int mode) {
        if (isOppoStyle()) {
            if (mode != getNavigationMode()) {
                switch (mode) {
                    case 1:
                        if (this.mSpinner == null) {
                            this.mSpinner = new ColorSpinner(getContext(), null, R.attr.actionDropDownStyle);
                            this.mSpinner.setId(this.mIdActionBarSpinner);
                            this.mListNavLayout = new LinearLayout(getContext(), null, R.attr.actionBarTabBarStyle);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -1);
                            params.gravity = 17;
                            this.mListNavLayout.addView(this.mSpinner, (ViewGroup.LayoutParams) params);
                        }
                        ((ColorSpinner) this.mSpinner).setSpinnerTextSize(this.mTitleTextSize);
                        ((ColorSpinner) this.mSpinner).setDropdownDismissCallback(this.mDropdownDismissCallback);
                        ((ColorSpinner) this.mSpinner).setOnItemClickListener(this.mDropdownItemClickListener);
                        ((ColorSpinner) this.mSpinner).setDropdownUpdateAfterAnim(this.mDropdownUpdateAfterAnim);
                        break;
                }
            }
            super.setNavigationMode(mode);
            updateTitleVisibility();
            return;
        }
        super.setNavigationMode(mode);
    }

    public void setSubtitle(CharSequence subtitle) {
        super.setSubtitle(subtitle);
        if (isOppoStyle()) {
            updateTitleVisibility();
        }
    }

    public void setHomeButtonEnabled(boolean enable) {
        if (isOppoStyle()) {
            this.mWasHomeEnabled = enable;
        }
        super.setHomeButtonEnabled(enable);
    }

    void setTitleImpl(CharSequence title) {
        super.setTitleImpl(title);
        if (isOppoStyle()) {
            updateTitleVisibility();
        }
    }

    void initTitle() {
        super.initTitle();
        if (isOppoStyle() && this.mTitleLayout != null) {
            if (OppoFontUtils.isFlipFontUsed) {
                this.mMediumTypeface = Typeface.DEFAULT;
            } else {
                try {
                    this.mMediumTypeface = Typeface.createFromFile("/system/fonts/ColorOSUI-Medium.ttf");
                } catch (Exception e) {
                    this.mMediumTypeface = Typeface.DEFAULT;
                    Log.e(TAG, "create special typeface failed");
                }
            }
            TextView titleView = (TextView) this.mTitleLayout.findViewById(ColorContextUtil.getResId(getContext(), 201458902));
            if (!(titleView == null || this.mMediumTypeface == null)) {
                titleView.setTypeface(this.mMediumTypeface);
            }
            if (!(titleView == null || this.mTitleTextColor == null)) {
                titleView.setTextColor(this.mTitleTextColor);
            }
            TextView subtitleView = (TextView) this.mTitleLayout.findViewById(ColorContextUtil.getResId(getContext(), 201458903));
            if (!(subtitleView == null || this.mSubtitleTextColor == null)) {
                subtitleView.setTextColor(this.mSubtitleTextColor);
            }
            updateTitleVisibility();
        }
    }

    int hookSetCustomViewPosX(ActionBar.LayoutParams ablp, int xpos, int hgravity, int navWidth) {
        if (!isOppoStyle()) {
            return super.hookSetCustomViewPosX(ablp, xpos, hgravity, navWidth);
        }
        boolean isLayoutRtl = isLayoutRtl();
        int layoutDirection = getLayoutDirection();
        int leftMargin = ablp != null ? ablp.leftMargin : 0;
        int rightMargin = ablp != null ? ablp.rightMargin : 0;
        int gravity = ablp != null ? ablp.gravity : DEFAULT_CUSTOM_GRAVITY;
        hgravity = gravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK;
        if (gravity == 0) {
            hgravity = Gravity.START;
        }
        switch (Gravity.getAbsoluteGravity(hgravity, layoutDirection)) {
            case 1:
                xpos = ((((((this.mLayoutRight - this.mLayoutLeft) - getPaddingRight()) - getPaddingLeft()) - navWidth) / 2) + leftMargin) - rightMargin;
                break;
            case 3:
                if (!isLayoutRtl) {
                    xpos = getPaddingLeft() + leftMargin;
                    break;
                }
                xpos = (((this.mLayoutRight - this.mLayoutLeft) - getPaddingRight()) - navWidth) - rightMargin;
                break;
            case 5:
                if (!isLayoutRtl) {
                    xpos = (((this.mLayoutRight - this.mLayoutLeft) - getPaddingRight()) - navWidth) - rightMargin;
                    break;
                }
                xpos = getPaddingLeft() + leftMargin;
                break;
        }
        return xpos;
    }

    public void setDropdownDismissCallback(DropdownDismissCallback callback) {
        this.mDropdownDismissCallback = callback;
        if (this.mSpinner instanceof ColorSpinner) {
            ((ColorSpinner) this.mSpinner).setDropdownDismissCallback(callback);
        }
    }

    public void setDropdownItemClickListener(OnItemClickListener listener) {
        this.mDropdownItemClickListener = listener;
        if (this.mSpinner instanceof ColorSpinner) {
            ((ColorSpinner) this.mSpinner).setOnItemClickListener(listener);
        }
    }

    public void setDropdownUpdateAfterAnim(boolean update) {
        this.mDropdownUpdateAfterAnim = update;
        if (this.mSpinner instanceof ColorSpinner) {
            ((ColorSpinner) this.mSpinner).setDropdownUpdateAfterAnim(update);
        }
    }

    public boolean isDropDownShowing() {
        if (this.mSpinner instanceof ColorSpinner) {
            return ((ColorSpinner) this.mSpinner).isDropDownShowing();
        }
        return false;
    }

    private void doCustomViewAnim(View customView) {
        if (customView != null && !this.mAnimationDisabled && 8 != customView.getVisibility()) {
            String packageName = getContext().getApplicationInfo().packageName;
            if ((packageName.equals("com.android.settings") || (packageName.equals("com.qualcomm.wfd.client") ^ 1) == 0) && customView.getWidth() == 0) {
                customView.setAnimation(AnimationUtils.loadAnimation(getContext(), 201982983));
            }
        }
    }

    private void doHomeAsUpAnim() {
        View homeAsUp = findViewById(this.mIdHomeAsUp);
        if (homeAsUp != null && (this.mAnimationDisabled ^ 1) != 0) {
            homeAsUp.setAnimation(AnimationUtils.loadAnimation(getContext(), 201982982));
        }
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateTitleVisibility() {
        if (this.mTitleLayout != null) {
            switch (getNavigationMode()) {
                case 1:
                    this.mTitleLayout.setVisibility(8);
                    break;
                case 2:
                    if (hasEmbeddedTabs()) {
                        this.mTitleLayout.setVisibility(8);
                        break;
                    }
                default:
                    if (this.mExpandedActionView == null && (!TextUtils.isEmpty(getTitle()) || !TextUtils.isEmpty(getSubtitle()))) {
                        this.mTitleLayout.setVisibility(0);
                        break;
                    } else {
                        this.mTitleLayout.setVisibility(8);
                        break;
                    }
                    break;
            }
        }
    }

    private void measureMenuView() {
        int verticalPadding = getPaddingTop() + getPaddingBottom();
        this.mMenuView.measure(makeExactlyMeasureSpec(getWidthSize()), makeExactlyMeasureSpec(getHeightSize() - verticalPadding));
    }

    private void positionMenuView() {
        boolean isLayoutRtl = isLayoutRtl();
        positionChildCenterHorizontal(this.mMenuView, isLayoutRtl ? (this.mLayoutRight - this.mLayoutLeft) - getPaddingRight() : getPaddingLeft(), getPaddingTop(), ((this.mLayoutBottom - this.mLayoutTop) - getPaddingTop()) - getPaddingBottom(), isLayoutRtl);
    }

    private void positioUpGoerFive() {
        boolean isLayoutRtl = isLayoutRtl();
        int x = isLayoutRtl ? (this.mLayoutRight - this.mLayoutLeft) - getPaddingRight() : getPaddingLeft();
        int y = getPaddingTop();
        int contentHeight = ((this.mLayoutBottom - this.mLayoutTop) - getPaddingTop()) - getPaddingBottom();
        if (this.mUpGoerFive != null) {
            this.mUpGoerFive.setLeft(this.mUpGoerFive.getLeft() + 1);
            positionChildCenterHorizontal(this.mUpGoerFive, x, y, contentHeight, isLayoutRtl);
        }
    }

    private int positionChildCenterHorizontal(View child, int x, int y, int contentHeight, boolean reverse) {
        super.positionChild(child, (this.mLayoutRight - child.getMeasuredWidth()) / 2, y, contentHeight, false);
        return x;
    }

    private int positionChildLeft(View child, int x, int y, int contentHeight, boolean reverse) {
        int homeWidth = 0;
        if (this.mHomeLayout != null) {
            ColorHomeView homeView = this.mHomeLayout;
            if (homeView.getVisibility() == 0) {
                View upView = homeView.getUpView();
                if (upView != null) {
                    homeWidth = upView.getMeasuredWidth();
                }
            }
        }
        int childX = this.mLayoutLeft;
        if (isLayoutRtl()) {
            childX = this.mLayoutRight - child.getMeasuredWidth();
            if (this.mIsMainActionBar) {
                childX -= this.mTitleLayoutPadding;
            }
            if (isUpViewVisible()) {
                childX -= homeWidth;
            }
        } else {
            if (this.mIsMainActionBar) {
                childX += this.mTitleLayoutPadding;
            }
            if (isUpViewVisible()) {
                childX += homeWidth;
            }
        }
        super.positionChild(child, childX, y, contentHeight, false);
        return x;
    }

    private int getWidthSize() {
        return MeasureSpec.getSize(this.mWidthMeasureSpec);
    }

    private int getHeightSize() {
        return this.mContentHeight >= 0 ? this.mContentHeight : MeasureSpec.getSize(this.mHeightMeasureSpec);
    }

    private int measureTabScrollView(int availableWidth, int childSpecHeight) {
        this.mTabScrollView.measure(makeAtMostMeasureSpec(isOppoStyle() ? this.mTabScrollViewMaxWidth : availableWidth), makeExactlyMeasureSpec(childSpecHeight));
        return availableWidth;
    }

    private int makeUnspecifiedMeasureSpec() {
        return ColorViewUtil.makeUnspecifiedMeasureSpec();
    }

    private int makeAtMostMeasureSpec(int measureSize) {
        return ColorViewUtil.makeAtMostMeasureSpec(measureSize);
    }

    private int makeExactlyMeasureSpec(int measureSize) {
        return ColorViewUtil.makeExactlyMeasureSpec(measureSize);
    }

    public void setBackTitle(CharSequence title) {
        this.mBackTitle = title;
        ((ColorHomeView) this.mHomeLayout).setBackTitle(title);
    }

    public void setMainActionBar(boolean isMain) {
        this.mIsMainActionBar = isMain;
    }

    public boolean getMainActionBar() {
        return this.mIsMainActionBar;
    }

    public boolean isUpViewVisible() {
        if (this.mHomeLayout == null) {
            return true;
        }
        View upView = this.mHomeLayout.getUpView();
        if (upView == null || upView.getVisibility() == 0) {
            return true;
        }
        return false;
    }

    public void setBackTitleTextColor(ColorStateList textColor) {
        ((ColorHomeView) this.mHomeLayout).setBackTitleTextColor(textColor);
    }

    public void setTitleTextColor(ColorStateList textColor) {
        this.mTitleTextColor = textColor;
    }

    public void setSubitleTextColor(ColorStateList textColor) {
        this.mSubtitleTextColor = textColor;
    }

    public void setActionMenuTextColor(ColorStateList textColor) {
        this.mActionMenuTextColor = textColor;
        if (this.mMenuView != null) {
            int childCount = this.mMenuView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                ActionMenuItemView itemView = (ActionMenuItemView) this.mMenuView.getChildAt(i);
                if (itemView != null) {
                    itemView.setTextColor(textColor);
                }
            }
        }
    }

    public boolean isMenuItemWidthValid() {
        boolean isValid = false;
        if (this.mMenuView != null) {
            int childCount = this.mMenuView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View itemView = this.mMenuView.getChildAt(i);
                if ((itemView instanceof ActionMenuItemView) && itemView.getMeasuredWidth() == 0) {
                    isValid = true;
                }
            }
        }
        return isValid;
    }

    public void setMenuViewTextSize() {
        if (this.mMenuView != null) {
            int size = this.mMenuView.getChildCount();
            for (int i = 0; i < size; i++) {
                View itemView = this.mMenuView.getChildAt(i);
                if (itemView instanceof ActionMenuItemView) {
                    ((ActionMenuItemView) itemView).setTextSize(0, (float) ((int) this.mMenuItemTextSize));
                }
            }
        }
    }

    public void setTitleLayoutTextSize() {
        if (this.mTitleLayout != null) {
            TextView titleView = (TextView) this.mTitleLayout.findViewById(this.mIdTitle);
            if (titleView != null) {
                setTextGravity(titleView);
                titleView.setTextSize(0, (float) ((int) this.mTitleTextSize));
            }
            TextView subtitleView = (TextView) this.mTitleLayout.findViewById(ColorContextUtil.getResId(getContext(), 201458903));
            if (subtitleView != null) {
                setTextGravity(subtitleView);
                subtitleView.setTextSize(0, (float) ((int) this.mSubTitleTextSize));
            }
        }
        if (getParent() instanceof ColorActionBarContainer) {
            ColorActionBarContainer abC = (ColorActionBarContainer) getParent();
            TextView hintTextView = (TextView) abC.findViewById(201458958);
            int hintTextMaxWidth = abC.getMeasuredWidth() - (this.mHintTextPadding * 2);
            if (!(hintTextView == null || (TextUtils.isEmpty(hintTextView.getText()) ^ 1) == 0)) {
                setHintTextMargin(hintTextView);
                hintTextView.setTextSize(0, (float) ((int) this.mHintTextSize));
            }
        }
        setSpinnerTextSize();
    }

    public void autoChangeActionBarTextSize() {
        float menuItemTextSize = this.mMenuItemTextSize;
        float titleTextSize = this.mTitleTextSize;
        float subTitleTextSize = this.mSubTitleTextSize;
        float hintTextSize = this.mHintTextSize;
    }

    public void shrinkMenuItemTextSize(ActionMenuItemView itemView) {
        int textShrinkWidth = 0;
        if (itemView != null && itemView.hasText()) {
            CharSequence text = itemView.getItemData().getTitle();
            if (text != null) {
                itemView.setTextSize(0, (float) ((int) this.mMenuItemTextSize));
                int viewWidth = (this.mMenuViewMaxWidth - itemView.getPaddingLeft()) - itemView.getPaddingRight();
                if (((int) itemView.getPaint().measureText(text.toString())) > viewWidth && viewWidth > 0) {
                    this.mIsShrinkOnce = true;
                    itemView.setTextSize(0, ((float) ((int) this.mMenuItemTextSize)) * TEXT_SIZE_SCALE);
                    textShrinkWidth = (int) itemView.getPaint().measureText(text.toString());
                }
                if (textShrinkWidth > viewWidth && viewWidth > 0) {
                    this.mIsShrinkTwice = true;
                }
                itemView.setTextSize(0, (float) ((int) this.mMenuItemTextSize));
            }
        }
    }

    public void shrinkTitleTextSize(TextView textView) {
        int titleViewWidth = (this.mTitleMaxWidth - this.mTitleLayout.getPaddingLeft()) - this.mTitleLayout.getPaddingRight();
        float textSize = textView.getTextSize();
        if (!TextUtils.isEmpty(textView.getText())) {
            String text = textView.getText().toString();
            int titleShrinkWidth = 0;
            if (((int) textView.getPaint().measureText(text)) > titleViewWidth && titleViewWidth > 0) {
                this.mIsShrinkOnce = true;
                textView.setTextSize(0, ((float) ((int) textSize)) * TEXT_SIZE_SCALE);
                titleShrinkWidth = (int) textView.getPaint().measureText(text);
            }
            if (titleShrinkWidth > titleViewWidth && titleViewWidth > 0) {
                this.mIsShrinkTwice = true;
            }
        }
    }

    public void setTextGravity(TextView tv) {
        if (this.mIsMainActionBar || isUpViewVisible()) {
            tv.setGravity(Gravity.START);
            tv.setTextAlignment(5);
            return;
        }
        tv.setGravity(17);
    }

    public void setHintTextMargin(TextView tv) {
        LayoutParams params = (LayoutParams) tv.getLayoutParams();
        if (params != null) {
            if (this.mIsMainActionBar || isUpViewVisible()) {
                params.setMarginStart(this.mHintTextPadding);
                params.setMarginEnd(this.mTitleLayoutPadding);
                params.gravity = Gravity.START;
            } else {
                params.gravity = 1;
                params.leftMargin = this.mHintTextPadding;
                params.rightMargin = this.mHintTextPadding;
            }
        }
        tv.-wrap18(params);
    }

    public void setSpinnerTextSize() {
        if (1 == getNavigationMode() && this.mSpinner != null && (this.mSpinner instanceof ColorSpinner)) {
            ((ColorSpinner) this.mSpinner).setSpinnerTextSize(this.mTitleTextSize);
        }
    }

    public void updateHomeAccessibility(boolean homeEnabled) {
        if (this.mHomeLayout != null) {
            if (homeEnabled) {
                this.mHomeLayout.setImportantForAccessibility(0);
                this.mHomeLayout.setContentDescription(buildHomeContentDescription());
            } else {
                this.mHomeLayout.setContentDescription(null);
                this.mHomeLayout.setImportantForAccessibility(2);
            }
        }
        TextView titleView = null;
        TextView subtitleView = null;
        TextView textView = null;
        if (this.mTitleLayout != null) {
            titleView = (TextView) this.mTitleLayout.findViewById(this.mIdTitle);
            subtitleView = (TextView) this.mTitleLayout.findViewById(ColorContextUtil.getResId(getContext(), 201458903));
            if (getParent() instanceof ColorActionBarContainer) {
                textView = (TextView) ((ColorActionBarContainer) getParent()).findViewById(201458958);
            }
        }
        if (titleView != null) {
            if (TextUtils.isEmpty(getTitle())) {
                titleView.setContentDescription(null);
                titleView.setImportantForAccessibility(2);
            } else {
                titleView.setImportantForAccessibility(1);
                titleView.setContentDescription(getTitle());
            }
        }
        if (subtitleView != null) {
            if (TextUtils.isEmpty(getSubtitle())) {
                subtitleView.setContentDescription(null);
                subtitleView.setImportantForAccessibility(2);
            } else {
                subtitleView.setImportantForAccessibility(1);
                subtitleView.setContentDescription(getSubtitle());
            }
        }
        if (textView == null) {
            return;
        }
        if (TextUtils.isEmpty(textView.getText())) {
            textView.setContentDescription(null);
            textView.setImportantForAccessibility(2);
            return;
        }
        textView.setImportantForAccessibility(1);
        textView.setContentDescription(textView.getText());
    }
}
