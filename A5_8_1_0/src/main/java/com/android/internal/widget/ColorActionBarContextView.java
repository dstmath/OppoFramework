package com.android.internal.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.AnimatorSet.Builder;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.text.style.SuggestionSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ActionMenuPresenter;
import android.widget.ActionMenuView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.view.menu.ActionMenuItemView;
import com.android.internal.view.menu.MenuBuilder;
import com.color.animation.ColorAnimatorUtil;
import com.color.animation.ColorAnimatorWrapper;
import com.color.util.ColorChangeTextUtil;
import com.color.util.ColorContextUtil;
import com.color.util.ColorLog;
import com.color.util.ColorLogKey.ActionBar;
import com.color.util.ColorLogKey.BottomMenu;
import com.color.util.ColorViewUtil;
import com.color.widget.ColorOptionMenuPresenter;
import com.color.widget.ColorOptionMenuView;
import java.util.ArrayList;
import java.util.List;
import oppo.content.res.OppoFontUtils;

public class ColorActionBarContextView extends ActionBarContextView implements UserAnimatorListener {
    private static final String TAG = "ColorActionBarContextView";
    private static final float TEXT_SIZE_SCALE = 0.88f;
    private int mActionBarHeight;
    private ColorStateList mActionMenuTextColor;
    private int mDuration;
    private final Rect mGlobalVisibleRect;
    private boolean mHasTranslationY;
    private int mHeightMeasureSpec;
    private AnimatorListener mHideListener;
    private List<AnimatorListener> mHideListenerList;
    private int mIdCloseButton;
    private int mIdTitle;
    private boolean mInLayout;
    private boolean mInMeasure;
    private Interpolator mInterpolator;
    private boolean mIsShrinkOnce;
    private boolean mIsShrinkTwice;
    private int mLayoutRight;
    private Typeface mMediumTypeface;
    private float mMenuItemTextSize;
    private int mMenuViewMaxWidth;
    private final int[] mScreenLocation;
    private AnimatorListener mShowListener;
    private List<AnimatorListener> mShowListenerList;
    private boolean mShowingFlags;
    private Drawable mSplitBackground;
    private int mStatusBarHeight;
    private float mSubTitleTextSize;
    private ColorStateList mSubtitleTextColor;
    protected final Class<?> mTagClass;
    private int mTitleMaxWidth;
    private ColorStateList mTitleTextColor;
    private float mTitleTextSize;
    private UserAnimator mUserAfterAnimation;
    private List<UserAnimator> mUserAnimations;
    private UserAnimator mUserBeforeAnimation;
    private UserAnimator mUserWithAnimation;
    private int mWidthMeasureSpec;

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

    private class UserListenerAdapter implements AnimatorListener {
        private final UserAnimatorListener mListener;
        private final boolean mShow;

        public UserListenerAdapter(UserAnimatorListener listener, boolean show) {
            this.mListener = listener;
            this.mShow = show;
        }

        public void onAnimationStart(Animator animation) {
            this.mListener.onAnimationStart(animation, this.mShow);
        }

        public void onAnimationEnd(Animator animation) {
            this.mListener.onAnimationEnd(animation, this.mShow);
        }

        public void onAnimationCancel(Animator animation) {
            this.mListener.onAnimationCancel(animation, this.mShow);
        }

        public void onAnimationRepeat(Animator animation) {
            this.mListener.onAnimationRepeat(animation, this.mShow);
        }
    }

    public ColorActionBarContextView(Context context) {
        this(context, null);
    }

    public ColorActionBarContextView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.actionModeStyle);
    }

    public ColorActionBarContextView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ColorActionBarContextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mTagClass = getClass();
        this.mHasTranslationY = false;
        this.mShowingFlags = true;
        this.mInMeasure = false;
        this.mInLayout = false;
        this.mDuration = 0;
        this.mStatusBarHeight = 0;
        this.mActionBarHeight = 0;
        this.mWidthMeasureSpec = -1;
        this.mHeightMeasureSpec = -1;
        this.mLayoutRight = -1;
        this.mIdCloseButton = -1;
        this.mIdTitle = -1;
        this.mScreenLocation = new int[2];
        this.mGlobalVisibleRect = new Rect();
        this.mShowListener = new UserListenerAdapter(this, true);
        this.mHideListener = new UserListenerAdapter(this, false);
        this.mShowListenerList = new ArrayList();
        this.mHideListenerList = new ArrayList();
        this.mUserWithAnimation = new UserAnimator("with");
        this.mUserAfterAnimation = new UserAnimator(SuggestionSpan.SUGGESTION_SPAN_PICKED_AFTER);
        this.mUserBeforeAnimation = new UserAnimator(SuggestionSpan.SUGGESTION_SPAN_PICKED_BEFORE);
        this.mUserAnimations = new ArrayList();
        this.mInterpolator = new DecelerateInterpolator();
        this.mSplitBackground = null;
        this.mMediumTypeface = null;
        this.mTitleTextColor = null;
        this.mSubtitleTextColor = null;
        this.mActionMenuTextColor = null;
        this.mIsShrinkOnce = false;
        this.mIsShrinkTwice = false;
        this.mTitleMaxWidth = 0;
        this.mMenuViewMaxWidth = 0;
        this.mMenuItemTextSize = 0.0f;
        this.mTitleTextSize = 0.0f;
        this.mSubTitleTextSize = 0.0f;
        if (isOppoStyle()) {
            this.mDuration = context.getResources().getInteger(202179604);
            this.mStatusBarHeight = context.getResources().getDimensionPixelSize(201654274);
            TypedArray a = context.obtainStyledAttributes(attrs, android.R.styleable.ActionMode, defStyleAttr, defStyleRes);
            this.mSplitBackground = a.getDrawable(4);
            a.recycle();
            TypedArray b = context.obtainStyledAttributes(android.R.styleable.Theme);
            this.mActionBarHeight = b.getDimensionPixelSize(139, 0);
            b.recycle();
            this.mIdCloseButton = ColorContextUtil.getResId(context, 201458888);
            this.mIdTitle = ColorContextUtil.getResId(context, 201458902);
            this.mUserAnimations.add(this.mUserWithAnimation);
            this.mUserAnimations.add(this.mUserAfterAnimation);
            this.mUserAnimations.add(this.mUserBeforeAnimation);
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
            this.mTitleTextSize = (float) context.getResources().getDimensionPixelSize(201654417);
            this.mSubTitleTextSize = (float) context.getResources().getDimensionPixelSize(201654407);
            this.mMenuItemTextSize = (float) context.getResources().getDimensionPixelSize(201654415);
            float fontScale = context.getResources().getConfiguration().fontScale;
            this.mTitleTextSize = ColorChangeTextUtil.getSuitableFontSize(this.mTitleTextSize, fontScale, 2);
            this.mSubTitleTextSize = ColorChangeTextUtil.getSuitableFontSize(this.mSubTitleTextSize, fontScale, 2);
            this.mMenuItemTextSize = ColorChangeTextUtil.getSuitableFontSize(this.mMenuItemTextSize, fontScale, 2);
            this.mMenuViewMaxWidth = context.getResources().getDimensionPixelSize(201655557);
        }
    }

    void initTitle() {
        super.initTitle();
        if (isOppoStyle()) {
            TextView title = (TextView) findViewById(this.mIdTitle);
            if (!(title == null || this.mMediumTypeface == null)) {
                title.setTypeface(this.mMediumTypeface);
            }
            if (!(title == null || this.mTitleTextColor == null)) {
                title.setTextColor(this.mTitleTextColor);
            }
            TextView subtitleView = (TextView) this.mTitleLayout.findViewById(ColorContextUtil.getResId(getContext(), 201458903));
            if (!(subtitleView == null || this.mSubtitleTextColor == null)) {
                subtitleView.setTextColor(this.mSubtitleTextColor);
            }
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isOppoStyle()) {
            this.mInMeasure = true;
            this.mWidthMeasureSpec = widthMeasureSpec;
            this.mHeightMeasureSpec = heightMeasureSpec;
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            this.mInMeasure = false;
            return;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (isOppoStyle()) {
            this.mInLayout = true;
            this.mLayoutRight = r;
            super.onLayout(changed, l, t, r, b);
            this.mInLayout = false;
            return;
        }
        super.onLayout(changed, l, t, r, b);
    }

    protected int measureChildView(View child, int availableWidth, int childSpecHeight, int spacing) {
        if (!isOppoStyle() || (this.mInMeasure ^ 1) != 0) {
            return super.measureChildView(child, availableWidth, childSpecHeight, spacing);
        }
        if (child == this.mMenuView) {
            int maxHeight = this.mContentHeight > 0 ? this.mContentHeight : getHeightSize();
            setMenuViewTextSize();
            child.measure(makeExactlyMeasureSpec(getWidthSize()), makeExactlyMeasureSpec(maxHeight));
            return availableWidth;
        } else if (child != this.mTitleLayout || this.mTitleLayout == null) {
            return super.measureChildView(child, availableWidth, childSpecHeight, spacing);
        } else {
            int space = 0;
            if (this.mMenuView != null) {
                space = (this.mMenuView.getPaddingLeft() > this.mMenuView.getPaddingRight() ? this.mMenuView.getPaddingLeft() : this.mMenuView.getPaddingRight()) + 0;
                int childCount = this.mMenuView.getChildCount();
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
                        space = (space + childOnLeft) + getResources().getDimensionPixelSize(201655498);
                    } else if (childCount == 4) {
                        childOnLeft = this.mMenuView.getChildAt(0).getMeasuredWidth() + this.mMenuView.getChildAt(1).getMeasuredWidth();
                        childOnRight = this.mMenuView.getChildAt(2).getMeasuredWidth() + this.mMenuView.getChildAt(3).getMeasuredWidth();
                        if (childOnLeft <= childOnRight) {
                            childOnLeft = childOnRight;
                        }
                        space = (space + childOnLeft) + getResources().getDimensionPixelSize(201655498);
                    } else if (childCount == 5) {
                        childOnLeft = this.mMenuView.getChildAt(0).getMeasuredWidth() + this.mMenuView.getChildAt(1).getMeasuredWidth();
                        childOnRight = (this.mMenuView.getChildAt(2).getMeasuredWidth() + this.mMenuView.getChildAt(3).getMeasuredWidth()) + this.mMenuView.getChildAt(4).getMeasuredWidth();
                        if (childOnLeft <= childOnRight) {
                            childOnLeft = childOnRight;
                        }
                        space = (space + childOnLeft) + (getResources().getDimensionPixelSize(201655498) * 2);
                    } else {
                        space = (MeasureSpec.getSize(this.mWidthMeasureSpec) - getResources().getDimensionPixelSize(201655445)) / 2;
                    }
                }
            }
            int titlePaddingLeft = getResources().getDimensionPixelSize(201655507);
            if (space <= titlePaddingLeft) {
                space = titlePaddingLeft;
            }
            int widthSize = MeasureSpec.getSize(this.mWidthMeasureSpec) - (space * 2);
            this.mTitleMaxWidth = widthSize;
            setTitleLayoutTextSize();
            this.mTitleLayout.setPadding(titlePaddingLeft, 0, titlePaddingLeft, 0);
            this.mTitleLayout.measure(MeasureSpec.makeMeasureSpec(widthSize, Integer.MIN_VALUE), childSpecHeight);
            return availableWidth;
        }
    }

    protected int positionChild(View child, int x, int y, int contentHeight, boolean reverse) {
        if (!isOppoStyle() || (this.mInLayout ^ 1) != 0) {
            return super.positionChild(child, x, y, contentHeight, reverse);
        }
        if (child != this.mTitleLayout) {
            return super.positionChild(child, x, y, contentHeight, reverse);
        }
        super.positionChild(child, (this.mLayoutRight - child.getMeasuredWidth()) / 2, y, contentHeight, false);
        return 0;
    }

    public void setSplitToolbar(boolean split) {
        if (isOppoStyle()) {
            if (!this.mSplitActionBar) {
                if (this.mActionMenuPresenter != null) {
                    LayoutParams layoutParams = new LayoutParams(-2, -1);
                    this.mMenuView = (ActionMenuView) this.mActionMenuPresenter.getMenuView(this);
                    this.mMenuView.setBackgroundDrawable(null);
                    ViewGroup oldParent = (ViewGroup) this.mMenuView.getParent();
                    if (oldParent != null) {
                        oldParent.removeView(this.mMenuView);
                    }
                    addView(this.mMenuView, layoutParams);
                }
                this.mSplitActionBar = true;
            }
            return;
        }
        super.setSplitToolbar(split);
    }

    public void initForMode(final ActionMode mode) {
        if (isOppoStyle()) {
            if (this.mClose == null) {
                this.mClose = LayoutInflater.from(getContext()).inflate(this.mCloseItemLayout, (ViewGroup) this, false);
                addView(this.mClose);
            } else if (this.mClose.getParent() == null) {
                addView(this.mClose);
            }
            View closeButton = this.mClose.findViewById(this.mIdCloseButton);
            closeButton.setVisibility(8);
            closeButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    mode.finish();
                }
            });
            MenuBuilder menu = (MenuBuilder) mode.getMenu();
            if (this.mActionMenuPresenter != null) {
                this.mActionMenuPresenter.dismissPopupMenus();
            }
            this.mActionMenuPresenter = new ActionMenuPresenter(getContext());
            this.mActionMenuPresenter.setReserveOverflow(true);
            LayoutParams layoutParams = new LayoutParams(-1, -1);
            menu.addMenuPresenter(this.mActionMenuPresenter, this.mPopupContext);
            this.mMenuView = (ActionMenuView) this.mActionMenuPresenter.getMenuView(this);
            this.mMenuView.setBackgroundDrawable(null);
            addView(this.mMenuView, layoutParams);
            ColorLog.d(ActionBar.MODE, this.mTagClass, "initForMode : mSplitActionBar=", Boolean.valueOf(this.mSplitActionBar), ", mSplitView=", this.mSplitView);
            if (this.mSplitActionBar && this.mSplitView != null) {
                this.mOptionMenuView = (ColorOptionMenuView) this.mSplitView.findViewById(201458882);
                if (this.mOptionMenuView != null) {
                    this.mOptionMenuPresenter = (ColorOptionMenuPresenter) this.mOptionMenuView.getPresenter();
                } else {
                    this.mOptionMenuPresenter = new ColorOptionMenuPresenter(getContext());
                    this.mOptionMenuView = (ColorOptionMenuView) this.mOptionMenuPresenter.getMenuView(this.mSplitView);
                    this.mOptionMenuView.setBackgroundDrawable(this.mSplitBackground);
                    layoutParams.width = -1;
                    layoutParams.height = this.mContentHeight;
                    this.mSplitView.addView(this.mOptionMenuView, layoutParams);
                }
                menu.addMenuPresenter(this.mOptionMenuPresenter, this.mPopupContext);
            }
            if (this.mActionMenuTextColor != null) {
                setActionMenuTextColor(this.mActionMenuTextColor);
            }
            return;
        }
        super.initForMode(mode);
    }

    public Animator setupAnimatorToVisibility(int visibility, long duration) {
        Animator anim = super.setupAnimatorToVisibility(visibility, duration);
        if (!isOppoStyle()) {
            return anim;
        }
        AnimatorSet set;
        if (anim instanceof AnimatorSet) {
            set = (AnimatorSet) anim;
        } else {
            set = new AnimatorSet();
            playUserAnimatorInternal(null, set, anim);
        }
        playUserAnimators(set, false);
        set.addListener(getUserAnimListener(visibility));
        return set;
    }

    public void onAnimationStart(Animator animation, boolean show) {
        List<AnimatorListener> list = show ? this.mShowListenerList : this.mHideListenerList;
        if (list != null) {
            for (AnimatorListener listener : list) {
                listener.onAnimationStart(animation);
            }
        }
    }

    public void onAnimationEnd(Animator animation, boolean show) {
        List<AnimatorListener> list = show ? this.mShowListenerList : this.mHideListenerList;
        if (list != null) {
            for (AnimatorListener listener : list) {
                listener.onAnimationEnd(animation);
            }
        }
    }

    public void onAnimationCancel(Animator animation, boolean show) {
        List<AnimatorListener> list = show ? this.mShowListenerList : this.mHideListenerList;
        if (list != null) {
            for (AnimatorListener listener : list) {
                listener.onAnimationCancel(animation);
            }
        }
    }

    public void onAnimationRepeat(Animator animation, boolean show) {
        List<AnimatorListener> list = show ? this.mShowListenerList : this.mHideListenerList;
        if (list != null) {
            for (AnimatorListener listener : list) {
                listener.onAnimationRepeat(animation);
            }
        }
    }

    public void addShowListener(AnimatorListener listener) {
        this.mShowListenerList.add(listener);
    }

    public void addHideListener(AnimatorListener listener) {
        this.mHideListenerList.add(listener);
    }

    public void addWithAnimator(Animator anim) {
        this.mUserWithAnimation.getAnimList().add(anim);
    }

    public void addWithAnimators(List<Animator> animList) {
        this.mUserWithAnimation.getAnimList().addAll(animList);
    }

    public void addAfterAnimator(Animator anim) {
        this.mUserAfterAnimation.getAnimList().add(anim);
    }

    public void addAfterAnimators(List<Animator> animList) {
        this.mUserAfterAnimation.getAnimList().addAll(animList);
    }

    public void addBeforeAnimator(Animator anim) {
        this.mUserBeforeAnimation.getAnimList().add(anim);
    }

    public void addBeforeAnimators(List<Animator> animList) {
        this.mUserBeforeAnimation.getAnimList().addAll(animList);
    }

    public void addWithAnimatorWrapper(ColorAnimatorWrapper anim) {
        this.mUserWithAnimation.getAnimWrapperList().add(anim);
    }

    public void addWithAnimatorWrappers(List<ColorAnimatorWrapper> animList) {
        this.mUserWithAnimation.getAnimWrapperList().addAll(animList);
    }

    public void addAfterAnimatorWrapper(ColorAnimatorWrapper anim) {
        this.mUserAfterAnimation.getAnimWrapperList().add(anim);
    }

    public void addAfterAnimatorWrappers(List<ColorAnimatorWrapper> animList) {
        this.mUserAfterAnimation.getAnimWrapperList().addAll(animList);
    }

    public void addBeforeAnimatorWrapper(ColorAnimatorWrapper anim) {
        this.mUserBeforeAnimation.getAnimWrapperList().add(anim);
    }

    public void addBeforeAnimatorWrappers(List<ColorAnimatorWrapper> animList) {
        this.mUserBeforeAnimation.getAnimWrapperList().addAll(animList);
    }

    public void playUserAnimators(Builder b, boolean flag) {
        playUserAnimatorsInternal(b, null, flag);
    }

    public void setShowingFlags(boolean isShow) {
        this.mShowingFlags = isShow;
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

    private void playUserAnimatorsInternal(Builder b, AnimatorSet set, boolean flag) {
        for (UserAnimator userAnim : this.mUserAnimations) {
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
        int updateMode = flag ? 0 : 1;
        if (this.mOptionMenuView != null) {
            this.mOptionMenuView.setMenuUpdateMode(updateMode);
        }
        if (this.mOptionMenuPresenter != null) {
            this.mOptionMenuPresenter.updateMenuView(true, b);
        }
        if (this.mOptionMenuView != null) {
            this.mOptionMenuView.setMenuUpdateMode(0);
        }
    }

    private void playUserAnimators(AnimatorSet set, boolean flag) {
        playUserAnimatorsInternal(null, set, flag);
    }

    private void initAnimator(Animator anim) {
        if (anim instanceof ObjectAnimator) {
            Object target = ((ObjectAnimator) anim).getTarget();
            if (target instanceof View) {
                ((View) target).setVisibility(0);
            }
        }
    }

    private int getViewHeight() {
        int height = getHeight();
        if (height == 0) {
            return this.mActionBarHeight;
        }
        return height;
    }

    private Builder playAlphaAnimator(AnimatorSet set, int visibility, boolean translationY) {
        int starting = getStartingAlpha(visibility);
        int ending = getEndingAlpha(visibility);
        setAlpha((float) starting);
        Animator anim = ObjectAnimator.ofFloat(this, "alpha", new float[]{(float) ending});
        anim.setDuration((long) this.mDuration);
        anim.setInterpolator(this.mInterpolator);
        return set.play(anim);
    }

    private void withTransAnimator(Builder b, int visibility, boolean translationY) {
        if (translationY) {
            int starting = getStartingY(visibility);
            int ending = getEndingY(visibility);
            setTranslationY((float) starting);
            Animator anim = ObjectAnimator.ofFloat(this, "translationY", new float[]{(float) ending});
            anim.setDuration((long) this.mDuration);
            anim.setInterpolator(this.mInterpolator);
            b.with(anim);
            return;
        }
        setTranslationY(0.0f);
    }

    private AnimatorListener getUserAnimListener(int visibility) {
        return visibility == 0 ? this.mShowListener : this.mHideListener;
    }

    private int getStartingY(int visibility) {
        int i = 0;
        if (getLayoutParams() == null) {
            return 0;
        }
        if (visibility == 0) {
            i = (-getViewHeight()) - ((MarginLayoutParams) getLayoutParams()).topMargin;
        }
        return i;
    }

    private int getEndingY(int visibility) {
        int i = 0;
        if (getLayoutParams() == null) {
            return 0;
        }
        if (visibility != 0) {
            i = (-getViewHeight()) - ((MarginLayoutParams) getLayoutParams()).topMargin;
        }
        return i;
    }

    private int getStartingAlpha(int visibility) {
        return visibility == 0 ? 0 : 1;
    }

    private int getEndingAlpha(int visibility) {
        return visibility == 0 ? 1 : 0;
    }

    private boolean isApproximate(int v1, int v2, int offset) {
        return Math.abs(v1 - v2) <= offset;
    }

    private boolean needTranslationY() {
        if (this.mShowingFlags && this.mHasTranslationY) {
            if (!getGlobalVisibleRect(this.mGlobalVisibleRect)) {
                return true;
            }
            getLocationOnScreen(this.mScreenLocation);
            ColorLog.d(ActionBar.MODE, this.mTagClass, "getRect=", Boolean.valueOf(getRect), ", mScreenLocation=(", Integer.valueOf(this.mScreenLocation[0]), ",", Integer.valueOf(this.mScreenLocation[1]), "), mGlobalVisibleRect=", this.mGlobalVisibleRect);
            return isApproximate(this.mScreenLocation[1], this.mStatusBarHeight, 1) || isApproximate(this.mScreenLocation[1], this.mStatusBarHeight - this.mActionBarHeight, 1);
        }
    }

    private int makeExactlyMeasureSpec(int measureSize) {
        return ColorViewUtil.makeExactlyMeasureSpec(measureSize);
    }

    private int getWidthSize() {
        return MeasureSpec.getSize(this.mWidthMeasureSpec);
    }

    private int getHeightSize() {
        return MeasureSpec.getSize(this.mHeightMeasureSpec);
    }

    public void setTitleTextColor(ColorStateList textColor) {
        this.mTitleTextColor = textColor;
    }

    public void setSubtitleTextColor(ColorStateList textColor) {
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

    public void setMenuViewTextSize() {
        if (this.mMenuView != null) {
            int childCount = this.mMenuView.getChildCount();
            for (int i = 0; i < childCount; i++) {
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
                titleView.setTextSize(0, (float) ((int) this.mTitleTextSize));
                titleView.setGravity(17);
            }
            TextView subtitleView = (TextView) this.mTitleLayout.findViewById(ColorContextUtil.getResId(getContext(), 201458903));
            if (subtitleView != null) {
                subtitleView.setTextSize(0, (float) ((int) this.mSubTitleTextSize));
                setSubTextMargin(subtitleView);
            }
        }
    }

    public void autoChangeActionBarTextSize() {
        float menuItemTextSize = this.mMenuItemTextSize;
        float titleTextSize = this.mTitleTextSize;
        float subTitleTextSize = this.mSubTitleTextSize;
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

    public void setSubTextMargin(TextView tv) {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) tv.getLayoutParams();
        if (lp != null) {
            lp.gravity = 17;
        }
        tv.-wrap18(lp);
    }
}
