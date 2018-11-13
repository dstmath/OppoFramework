package com.color.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.AnimatorSet.Builder;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.ActionBar;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.color.actionbar.app.ColorActionBarUtil.ActionBarCallback;
import com.color.screenshot.ColorLongshotUnsupported;
import com.color.util.ColorChangeTextUtil;

public class ColorSearchViewAnim extends LinearLayout implements OnClickListener, ColorLongshotUnsupported {
    private static final float ALPHA_FOREGROUND = 0.5f;
    private static final boolean DEBUG = false;
    private static final int SEARCH_ANIM = 1002;
    private static final int SEARCH_ANIMING = 2;
    private static final int SEARCH_HIDE = 1001;
    private static final int SEARCH_SHOW = 1000;
    private static final int SEARCH_SHRINK = 0;
    private static final int SEARCH_STRETCH = 1;
    private static final String TAG = "ColorSearchViewAnim";
    private ActionBarCallback mActionBarCallback;
    private final AnimatorListener mActionBarHideListener;
    private final AnimatorListener mActionBarShowListener;
    private AutoCompleteTextView mAutoComplete;
    private int mClorSearchViewAnimWidth;
    private AnimatorSet mCurrentShowAnim;
    private View mForeground;
    private int mForegroundBg;
    private ImageButton mImageButton;
    private int mImageButtonLeft;
    private int mImageButtonWidth;
    private boolean mIsActionBarShow;
    private boolean mIsCallWindowFocus;
    private boolean mIsHasFoucus;
    private boolean mIsShowForeground;
    private boolean mIsTriggerActionBarAnim;
    private int mLimitPaddingR;
    private OnAnimationListener mOnAnimationListener;
    private OnClickTextButtonListener mOnClickTextButtonListener;
    private final ViewGroup mSearchLayout;
    private final int mSearchMarginRight;
    private LinearLayout mSearchSrc;
    private int mSearchState;
    private final ColorSearchView mSearchView;
    private int mSearchViewState;
    private final Runnable mShowActionBarRunnable;
    private final AnimatorListener mShowForegroundListener;
    private final Runnable mShowImeRunnable;
    private final TextView mTextButton;
    private int mTextButtonMargin;
    private int mTextButtonWidth;

    private abstract class BaseAnimListener extends AnimatorListenerAdapter {
        private boolean mIsCancel;

        /* synthetic */ BaseAnimListener(ColorSearchViewAnim this$0, BaseAnimListener -this1) {
            this();
        }

        private BaseAnimListener() {
            this.mIsCancel = ColorSearchViewAnim.DEBUG;
        }

        public void onAnimationCancel(Animator animation) {
            this.mIsCancel = true;
        }

        public void onAnimationEnd(Animator animation) {
            this.mIsCancel = ColorSearchViewAnim.DEBUG;
        }

        boolean isCancel() {
            return this.mIsCancel;
        }
    }

    private class ActionBarAnimListener extends BaseAnimListener {
        private boolean mIsCancel = ColorSearchViewAnim.DEBUG;
        private final boolean mIsShow;

        public ActionBarAnimListener(boolean isShow) {
            super(ColorSearchViewAnim.this, null);
            this.mIsShow = isShow;
        }

        public void onAnimationCancel(Animator animation) {
            this.mIsCancel = true;
        }

        public void onAnimationEnd(Animator animation) {
            this.mIsCancel = ColorSearchViewAnim.DEBUG;
        }

        boolean isCancel() {
            return this.mIsCancel;
        }
    }

    private abstract class TargetAnimListener extends BaseAnimListener {
        private final float mEndValue;
        private final float mStartValue;
        private final View mTarget;

        public TargetAnimListener(View target, float startValue, float endValue) {
            super(ColorSearchViewAnim.this, null);
            this.mTarget = target;
            this.mStartValue = startValue;
            this.mEndValue = endValue;
        }

        public View getTarget() {
            return this.mTarget;
        }

        public float getStartValue() {
            return this.mStartValue;
        }

        public float getEndValue() {
            return this.mEndValue;
        }
    }

    private class AnimAlphaListener extends TargetAnimListener {
        public AnimAlphaListener(View target, float startValue, float endValue) {
            super(target, startValue, endValue);
        }

        public void onAnimationEnd(Animator animation) {
        }
    }

    private class AnimWidthListener extends TargetAnimListener {
        public AnimWidthListener(View target, float startValue, float endValue) {
            super(target, startValue, endValue);
        }

        public void onAnimationEnd(Animator animation) {
            View target = getTarget();
            LayoutParams lp = (LayoutParams) target.getLayoutParams();
            if (lp != null) {
                lp.width = (int) (isCancel() ? getStartValue() : getEndValue());
                target.setLayoutParams(lp);
            }
        }
    }

    private class AnimXListener extends TargetAnimListener {
        public AnimXListener(View target, float startValue, float endValue) {
            super(target, startValue, endValue);
        }

        public void onAnimationEnd(Animator animation) {
            getTarget().setX(isCancel() ? getStartValue() : getEndValue());
        }
    }

    public interface OnAnimationListener {
        void onHideAnimationEnd();

        void onShowAnimationEnd();
    }

    public interface OnClickTextButtonListener {
        void onClickTextButton();
    }

    public ColorSearchViewAnim(Context context) {
        this(context, null);
    }

    public ColorSearchViewAnim(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mSearchViewState = 0;
        this.mActionBarCallback = null;
        this.mCurrentShowAnim = null;
        this.mForeground = null;
        this.mOnClickTextButtonListener = null;
        this.mOnAnimationListener = null;
        this.mIsTriggerActionBarAnim = DEBUG;
        this.mIsActionBarShow = DEBUG;
        this.mSearchState = SEARCH_HIDE;
        this.mIsCallWindowFocus = true;
        this.mTextButtonWidth = 0;
        this.mClorSearchViewAnimWidth = 0;
        this.mIsHasFoucus = DEBUG;
        this.mTextButtonMargin = 0;
        this.mImageButton = null;
        this.mSearchSrc = null;
        this.mImageButtonLeft = 0;
        this.mImageButtonWidth = 0;
        this.mLimitPaddingR = 0;
        this.mAutoComplete = null;
        this.mIsShowForeground = true;
        this.mShowImeRunnable = new Runnable() {
            public void run() {
                ColorSearchViewAnim.this.showSoftInput();
            }
        };
        this.mShowActionBarRunnable = new Runnable() {
            public void run() {
                ColorSearchViewAnim.this.showActionBar();
            }
        };
        this.mShowForegroundListener = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                ColorSearchViewAnim.this.mCurrentShowAnim = null;
            }
        };
        this.mActionBarHideListener = new ActionBarAnimListener(this, DEBUG) {
            public void onAnimationStart(Animator animation) {
                if (ColorSearchViewAnim.SEARCH_ANIM == this.mSearchState && this.mIsTriggerActionBarAnim) {
                    LayoutParams lpSrcText = (LayoutParams) this.mAutoComplete.getLayoutParams();
                    if (lpSrcText != null) {
                        lpSrcText.weight = 1.0f;
                        lpSrcText.width = 0;
                        this.mAutoComplete.setLayoutParams(lpSrcText);
                        this.mAutoComplete.invalidate();
                    }
                }
                super.onAnimationStart(animation);
            }

            public void onAnimationEnd(Animator animation) {
                if (ColorSearchViewAnim.SEARCH_ANIM == this.mSearchState && this.mIsTriggerActionBarAnim) {
                    if (!(this.mOnAnimationListener == null || (isCancel() ^ 1) == 0)) {
                        this.mOnAnimationListener.onShowAnimationEnd();
                    }
                    this.mSearchState = isCancel() ? ColorSearchViewAnim.SEARCH_HIDE : ColorSearchViewAnim.SEARCH_SHOW;
                    this.mCurrentShowAnim = null;
                    this.mIsTriggerActionBarAnim = ColorSearchViewAnim.DEBUG;
                    this.mAutoComplete.setHapticFeedbackEnabled(true);
                }
                super.onAnimationEnd(animation);
            }
        };
        this.mActionBarShowListener = new ActionBarAnimListener(this, true) {
            public void onAnimationStart(Animator animation) {
                if (ColorSearchViewAnim.SEARCH_ANIM == this.mSearchState && this.mIsTriggerActionBarAnim) {
                    if (!TextUtils.isEmpty(this.mAutoComplete.getText())) {
                        this.mSearchView.setQuery(new String(), ColorSearchViewAnim.DEBUG);
                        this.resetSearchSrcPadding(0, this.mLimitPaddingR);
                    }
                    LayoutParams lpSrcText = (LayoutParams) this.mAutoComplete.getLayoutParams();
                    if (lpSrcText != null) {
                        lpSrcText.weight = 0.0f;
                        lpSrcText.width = -2;
                        this.mAutoComplete.setLayoutParams(lpSrcText);
                        this.mAutoComplete.invalidate();
                    }
                }
                super.onAnimationStart(animation);
            }

            public void onAnimationEnd(Animator animation) {
                if (ColorSearchViewAnim.SEARCH_ANIM == this.mSearchState && this.mIsTriggerActionBarAnim) {
                    this.removeCallbacks(this.mShowActionBarRunnable);
                    if (!(this.mOnAnimationListener == null || (isCancel() ^ 1) == 0)) {
                        this.mOnAnimationListener.onHideAnimationEnd();
                    }
                    if (this.mForeground != null && this.mForeground.isShown()) {
                        this.mForeground.setVisibility(8);
                        this.mForeground.setAlpha(1.0f);
                    }
                    this.mSearchState = isCancel() ? ColorSearchViewAnim.SEARCH_SHOW : ColorSearchViewAnim.SEARCH_HIDE;
                    this.mIsTriggerActionBarAnim = ColorSearchViewAnim.DEBUG;
                    this.mAutoComplete.setHapticFeedbackEnabled(ColorSearchViewAnim.DEBUG);
                }
                super.onAnimationEnd(animation);
            }
        };
        setBackgroundColor(context.getResources().getColor(201720886));
        LayoutInflater.from(context).inflate(201917563, this, true);
        this.mSearchMarginRight = context.getResources().getDimensionPixelSize(201655506);
        this.mTextButtonMargin = context.getResources().getDimensionPixelSize(201655511);
        this.mLimitPaddingR = context.getResources().getDimensionPixelSize(201655510);
        this.mForegroundBg = context.getColor(201720902);
        this.mSearchLayout = (ViewGroup) findViewById(201458834);
        this.mTextButton = (TextView) findViewById(201458835);
        int textSize = getResources().getDimensionPixelSize(201654415);
        this.mTextButton.setTextSize(0, (float) ((int) ColorChangeTextUtil.getSuitableFontSize((float) textSize, getResources().getConfiguration().fontScale, 2)));
        this.mSearchView = (ColorSearchView) findViewById(201458836);
        this.mSearchView.onActionViewExpanded();
        this.mSearchView.clearFocus();
        this.mTextButton.setOnClickListener(this);
        resetPosition(this.mSearchMarginRight);
        if (this.mTextButton != null) {
            this.mTextButtonWidth = (int) this.mTextButton.getPaint().measureText((String) this.mTextButton.getText());
        }
        this.mImageButton = (ImageButton) findViewById(201458964);
        this.mSearchSrc = (LinearLayout) findViewById(201458965);
        this.mAutoComplete = this.mSearchView.getSearchAutoComplete();
        this.mTextButton.setAccessibilityDelegate(new AccessibilityDelegate() {
            public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
                accessibilityNodeInfo.setClassName(Button.class.getName());
            }
        });
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (this.mSearchView != null) {
            this.mSearchView.setFocusable(true);
            this.mSearchView.setFocusableInTouchMode(true);
            this.mSearchView.requestFocus();
        }
        if (!(this.mAutoComplete == null || (this.mIsHasFoucus ^ 1) == 0)) {
            this.mAutoComplete.setFocusable(DEBUG);
        }
        return super.dispatchTouchEvent(ev);
    }

    public void onClick(View v) {
        if (v.getId() == 201458835 && this.mOnClickTextButtonListener != null) {
            this.mOnClickTextButtonListener.onClickTextButton();
        }
    }

    private void resetPosition(int paddingRight) {
        if (this.mSearchView != null) {
            LayoutParams lp = new LayoutParams(-1, -2);
            lp.setMarginEnd(paddingRight);
            this.mSearchView.setLayoutParams(lp);
        }
    }

    private void resetSearchSrcPadding(int paddingLeft, int paddingRight) {
        if (this.mSearchSrc != null) {
            this.mSearchSrc.setPadding(paddingLeft, 0, paddingRight, 0);
        }
    }

    public void setSearchAutoCompleteFocus() {
        if (this.mAutoComplete != null) {
            this.mAutoComplete.setFocusable(true);
            this.mAutoComplete.setFocusableInTouchMode(true);
            this.mAutoComplete.requestFocus();
        }
    }

    public void setSearchAutoCompleteUnFocus() {
        setImeVisibility(DEBUG);
        this.mSearchView.clearFocus();
        this.mSearchView.setFocusable(DEBUG);
        if (this.mIsCallWindowFocus) {
            this.mSearchView.onWindowFocusChanged(DEBUG);
        } else {
            this.mIsCallWindowFocus = true;
        }
        if (this.mAutoComplete != null) {
            this.mAutoComplete.setFocusable(DEBUG);
        }
    }

    public void setImeVisibility(boolean visible) {
        removeCallbacks(this.mShowImeRunnable);
        if (visible) {
            post(this.mShowImeRunnable);
            setBackgroundResource(201850880);
            return;
        }
        setSearchViewBackground(null);
        hideSoftInput();
    }

    private void removeRunnables() {
        removeCallbacks(this.mShowActionBarRunnable);
        removeCallbacks(this.mShowImeRunnable);
    }

    private void showSoftInput() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService("input_method");
        if (imm != null) {
            imm.showSoftInputUnchecked(0, null);
        }
    }

    private void hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService("input_method");
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

    private void showActionBar() {
        if (this.mActionBarCallback != null) {
            this.mIsTriggerActionBarAnim = true;
            ((ActionBar) this.mActionBarCallback).show();
        }
    }

    private void hideActionBar() {
        if (this.mActionBarCallback != null) {
            this.mIsTriggerActionBarAnim = true;
            ((ActionBar) this.mActionBarCallback).hide();
        }
    }

    private void cancelActionBarShowHide() {
        if (this.mActionBarCallback != null) {
            this.mActionBarCallback.cancelShowHide();
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mIsCallWindowFocus = DEBUG;
    }

    private void resetState() {
        removeRunnables();
        cancelActionBarShowHide();
        setSearchAutoCompleteUnFocus();
        hideSoftInput();
        if (this.mCurrentShowAnim != null) {
            this.mCurrentShowAnim.cancel();
        }
    }

    private void addSearchAnimation(Animator anim, AnimatorListener listener) {
        anim.addListener(listener);
        this.mActionBarCallback.addSearchViewWithAnimator(anim);
    }

    private boolean isActionBarShowing() {
        if (this.mActionBarCallback != null) {
            return ((ActionBar) this.mActionBarCallback).isShowing();
        }
        return DEBUG;
    }

    private void startHorizontalStretchAnim(boolean isUpDownAnim) {
        float startValue;
        float endValue;
        boolean direction = isLayoutRtl();
        setImeVisibility(true);
        if (this.mImageButton != null) {
            this.mImageButtonLeft = this.mImageButton.getLeft();
            this.mImageButtonWidth = this.mImageButton.getWidth();
            if (this.mImageButtonWidth == 0) {
                this.mImageButtonWidth = this.mImageButton.getDrawable().getMinimumWidth();
            }
        }
        ObjectAnimator animSearchWidth = ObjectAnimator.ofFloat(this.mSearchView, "width", new float[]{(float) (this.mClorSearchViewAnimWidth - (this.mSearchMarginRight * 2)), (float) (((this.mClorSearchViewAnimWidth - this.mTextButtonWidth) - this.mSearchMarginRight) - (this.mTextButtonMargin * 2))});
        animSearchWidth.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float newWidth = ((Float) animation.getAnimatedValue()).floatValue();
                if (ColorSearchViewAnim.this.mSearchView != null) {
                    LayoutParams lp = (LayoutParams) ColorSearchViewAnim.this.mSearchView.getLayoutParams();
                    if (lp != null) {
                        lp.width = (int) newWidth;
                        ColorSearchViewAnim.this.mSearchView.setLayoutParams(lp);
                    }
                }
            }
        });
        if (isUpDownAnim) {
            addSearchAnimation(animSearchWidth, new AnimWidthListener(this.mSearchView, startValue, endValue));
        }
        if (isLayoutRtl()) {
            startValue = (float) (-this.mTextButtonWidth);
            endValue = (float) this.mSearchMarginRight;
        } else {
            startValue = (float) this.mClorSearchViewAnimWidth;
            endValue = (startValue - ((float) this.mTextButtonWidth)) - ((float) this.mTextButtonMargin);
        }
        ObjectAnimator animTextButtonX = ObjectAnimator.ofFloat(this.mTextButton, "X", new float[]{startValue, endValue});
        animTextButtonX.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float leftX = ((Float) animation.getAnimatedValue()).floatValue();
                if (ColorSearchViewAnim.this.mTextButton == null) {
                    return;
                }
                if (ColorSearchViewAnim.this.isLayoutRtl()) {
                    ColorSearchViewAnim.this.mTextButton.setLeft((int) leftX);
                } else {
                    ColorSearchViewAnim.this.mTextButton.setLeft(((int) leftX) - (ColorSearchViewAnim.this.mTextButtonMargin - ColorSearchViewAnim.this.mSearchMarginRight));
                }
            }
        });
        if (isUpDownAnim) {
            addSearchAnimation(animTextButtonX, new AnimXListener(this.mTextButton, startValue, endValue));
        }
        if (!(this.mForeground == null || (this.mForeground.isShown() ^ 1) == 0 || this.mSearchView == null || !this.mSearchView.getQuery().toString().isEmpty())) {
            this.mForeground.setVisibility(0);
        }
        if (!isUpDownAnim) {
            AnimatorSet animSet = new AnimatorSet();
            animSet.play(animSearchWidth).with(animTextButtonX);
            animSet.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    if (2 == ColorSearchViewAnim.this.mSearchViewState) {
                        ColorSearchViewAnim.this.mSearchViewState = 1;
                    }
                }
            });
            animSet.setDuration(200);
            animSet.setInterpolator(new LinearInterpolator());
            animSet.start();
        }
    }

    private void startHorizontalShrinkAnim(boolean isUpDownAnim) {
        float startValue;
        float endValue;
        if (isLayoutRtl()) {
            startValue = (float) this.mTextButtonWidth;
            endValue = (float) (-this.mSearchMarginRight);
        } else {
            startValue = (float) ((this.mClorSearchViewAnimWidth - this.mTextButtonWidth) - this.mTextButtonMargin);
            endValue = (float) this.mClorSearchViewAnimWidth;
        }
        ObjectAnimator animTextButtonX = ObjectAnimator.ofFloat(this.mTextButton, "x", new float[]{startValue, endValue});
        animTextButtonX.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float leftX = ((Float) animation.getAnimatedValue()).floatValue();
                if (ColorSearchViewAnim.this.mTextButton != null) {
                    ColorSearchViewAnim.this.mTextButton.setLeft((int) leftX);
                }
            }
        });
        if (isUpDownAnim) {
            addSearchAnimation(animTextButtonX, new AnimXListener(this.mTextButton, startValue, endValue));
        }
        ObjectAnimator animSearchWidth = ObjectAnimator.ofFloat(this.mSearchView, "width", new float[]{(float) (((this.mClorSearchViewAnimWidth - this.mTextButtonWidth) - this.mSearchMarginRight) - (this.mTextButtonMargin * 2)), (float) (this.mClorSearchViewAnimWidth - (this.mSearchMarginRight * 2))});
        animSearchWidth.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float newWidth = ((Float) animation.getAnimatedValue()).floatValue();
                LayoutParams lp = (LayoutParams) ColorSearchViewAnim.this.mSearchView.getLayoutParams();
                if (lp != null) {
                    lp.width = (int) newWidth;
                    ColorSearchViewAnim.this.mSearchView.setLayoutParams(lp);
                }
            }
        });
        if (isUpDownAnim) {
            addSearchAnimation(animSearchWidth, new AnimWidthListener(this.mSearchView, startValue, endValue));
        }
        if (this.mForeground != null && this.mForeground.isShown()) {
            this.mForeground.setVisibility(8);
        }
        if (!isUpDownAnim) {
            if (!TextUtils.isEmpty(this.mAutoComplete.getText())) {
                this.mSearchView.setQuery(new String(), DEBUG);
                resetSearchSrcPadding(0, this.mLimitPaddingR);
            }
            AnimatorSet animSet = new AnimatorSet();
            animSet.play(animSearchWidth).with(animTextButtonX);
            animSet.setDuration(200);
            animSet.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    if (2 == ColorSearchViewAnim.this.mSearchViewState) {
                        ColorSearchViewAnim.this.mSearchViewState = 0;
                    }
                }
            });
            animSet.setInterpolator(new LinearInterpolator());
            animSet.start();
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        if (SEARCH_ANIM != this.mSearchState && this.mSearchViewState != 2 && this.mSearchView != null && this.mTextButton != null) {
            this.mClorSearchViewAnimWidth = measuredWidth;
            if (this.mIsHasFoucus) {
                this.mSearchView.measure(MeasureSpec.makeMeasureSpec(((this.mClorSearchViewAnimWidth - this.mTextButtonWidth) - this.mSearchMarginRight) - (this.mTextButtonMargin * 2), 1073741824), MeasureSpec.makeMeasureSpec(this.mSearchView.getMeasuredHeight(), 1073741824));
                this.mTextButton.measure(MeasureSpec.makeMeasureSpec(this.mTextButtonWidth, 1073741824), heightMeasureSpec);
                return;
            }
            this.mSearchView.measure(MeasureSpec.makeMeasureSpec(this.mClorSearchViewAnimWidth - (this.mSearchMarginRight * 2), 1073741824), MeasureSpec.makeMeasureSpec(this.mSearchView.getMeasuredHeight(), 1073741824));
        }
    }

    public ColorSearchView getSearchView() {
        return this.mSearchView;
    }

    public void setOnClickTextButtonListener(OnClickTextButtonListener listener) {
        this.mOnClickTextButtonListener = listener;
    }

    public void setOnAnimationListener(OnAnimationListener listener) {
        this.mOnAnimationListener = listener;
    }

    public void setActionBar(ActionBar bar) {
        if (bar != null) {
            this.mIsActionBarShow = isActionBarShowing();
            this.mActionBarCallback = (ActionBarCallback) bar;
            this.mActionBarCallback.addSearchViewHideListener(this.mActionBarHideListener);
            this.mActionBarCallback.addSearchViewShowListener(this.mActionBarShowListener);
            this.mActionBarCallback.setSearchBarMode(true);
        }
    }

    public void setActionModeAnim(boolean flag) {
        if (this.mActionBarCallback != null) {
            this.mActionBarCallback.setActionModeAnim(flag);
        }
    }

    public void setSearchViewDisabled() {
        if (this.mAutoComplete != null) {
            this.mAutoComplete.setFocusableInTouchMode(DEBUG);
            this.mAutoComplete.setFocusable(DEBUG);
            this.mAutoComplete.setEnabled(DEBUG);
        }
        if (this.mImageButton != null) {
            this.mImageButton.setFocusableInTouchMode(DEBUG);
            this.mImageButton.setFocusable(DEBUG);
            this.mImageButton.setEnabled(DEBUG);
        }
        if (this.mSearchSrc != null) {
            this.mSearchSrc.setFocusableInTouchMode(DEBUG);
            this.mSearchSrc.setFocusable(DEBUG);
            this.mSearchSrc.setEnabled(DEBUG);
        }
        this.mSearchView.setSearchViewBackground();
    }

    public void setSearchViewEnabled() {
        if (this.mAutoComplete != null) {
            this.mAutoComplete.setEnabled(true);
        }
        if (this.mImageButton != null) {
            this.mImageButton.setEnabled(true);
        }
        if (this.mSearchSrc != null) {
            this.mSearchSrc.setEnabled(true);
        }
        this.mSearchView.setSearchViewBackground();
    }

    public void setForeground(View foreground) {
        this.mForeground = foreground;
        if (this.mForeground != null) {
            this.mForeground.setBackgroundColor(this.mForegroundBg);
        }
    }

    public void showForeground() {
        if (this.mForeground != null && this.mIsShowForeground) {
            if (this.mCurrentShowAnim != null) {
                this.mCurrentShowAnim.end();
            }
            this.mForeground.setVisibility(0);
            this.mCurrentShowAnim = new AnimatorSet();
            Builder b = this.mCurrentShowAnim.play(ObjectAnimator.ofFloat(this.mSearchLayout, "translationY", new float[]{0.0f}));
            this.mCurrentShowAnim.setInterpolator(AnimationUtils.loadInterpolator(getContext(), 17563651));
            this.mCurrentShowAnim.setDuration(200);
            this.mCurrentShowAnim.addListener(this.mShowForegroundListener);
            this.mCurrentShowAnim.start();
        }
    }

    public void setStateRestore() {
        resetState();
    }

    public void restoreState() {
        if (this.mSearchState == SEARCH_SHOW) {
            this.mSearchState = SEARCH_HIDE;
            setSearchAutoCompleteUnFocus();
            this.mIsHasFoucus = DEBUG;
            if (this.mCurrentShowAnim != null) {
                this.mCurrentShowAnim.end();
            }
            if (this.mSearchView != null) {
                resetPosition(this.mSearchMarginRight);
                LayoutParams lp = (LayoutParams) this.mSearchView.getLayoutParams();
                if (lp != null) {
                    lp.width = this.mClorSearchViewAnimWidth - (this.mSearchMarginRight * 2);
                    this.mSearchView.setLayoutParams(lp);
                }
            }
            if (this.mTextButton != null) {
                float endValue;
                if (isLayoutRtl()) {
                    endValue = (float) (-this.mSearchMarginRight);
                } else {
                    endValue = (float) this.mClorSearchViewAnimWidth;
                }
                this.mTextButton.setX(endValue);
                this.mTextButton.setVisibility(8);
            }
            if (!(TextUtils.isEmpty(this.mAutoComplete.getText()) || this.mSearchView == null)) {
                this.mSearchView.setQuery(new String(), DEBUG);
                resetSearchSrcPadding(0, this.mLimitPaddingR);
            }
            LayoutParams lpSrcText = (LayoutParams) this.mAutoComplete.getLayoutParams();
            if (lpSrcText != null) {
                lpSrcText.weight = 0.0f;
                lpSrcText.width = -2;
                this.mAutoComplete.setLayoutParams(lpSrcText);
            }
            if (this.mForeground != null) {
                this.mForeground.setVisibility(8);
                this.mForeground.setAlpha(1.0f);
            }
        }
    }

    public void startSearchViewUpAnim() {
        if (SEARCH_HIDE == this.mSearchState) {
            this.mIsShowForeground = true;
            this.mSearchState = SEARCH_ANIM;
            setSearchAutoCompleteFocus();
            this.mIsHasFoucus = true;
            this.mSearchLayout.setBackgroundDrawable(null);
            if (this.mActionBarCallback != null && isActionBarShowing()) {
                startHorizontalStretchAnim(true);
                hideActionBar();
            }
        }
    }

    public void startSearchViewDownAnim() {
        if (SEARCH_SHOW == this.mSearchState) {
            this.mIsShowForeground = DEBUG;
            this.mSearchState = SEARCH_ANIM;
            setSearchAutoCompleteUnFocus();
            this.mIsHasFoucus = DEBUG;
            if (this.mCurrentShowAnim != null) {
                this.mCurrentShowAnim.end();
            }
            if (!(this.mActionBarCallback == null || (isActionBarShowing() ^ 1) == 0)) {
                startHorizontalShrinkAnim(true);
                post(this.mShowActionBarRunnable);
            }
        }
    }

    public void startSearchViewShrinkAnim() {
        if (1 == this.mSearchViewState) {
            this.mIsShowForeground = DEBUG;
            this.mSearchViewState = 2;
            setSearchAutoCompleteUnFocus();
            this.mIsHasFoucus = DEBUG;
            if (this.mCurrentShowAnim != null) {
                this.mCurrentShowAnim.end();
            }
            startHorizontalShrinkAnim(DEBUG);
        }
    }

    public void startSearchViewStretchAnim() {
        if (this.mSearchViewState == 0) {
            this.mIsShowForeground = true;
            this.mSearchViewState = 2;
            setSearchAutoCompleteFocus();
            this.mIsHasFoucus = true;
            this.mSearchLayout.setBackgroundDrawable(null);
            startHorizontalStretchAnim(DEBUG);
        }
    }

    public void setSearchViewBg(int color) {
        setBackgroundColor(color);
    }

    public void setSearchViewBackground(Drawable drawable) {
        setBackgroundDrawable(drawable);
    }

    public void setSearchViewBackground(int resId) {
        setBackgroundDrawable(getContext().getDrawable(resId));
    }

    private boolean hasInputText() {
        return this.mAutoComplete.length() > 0 ? true : DEBUG;
    }

    private boolean isStretchLongshotUnsupported() {
        switch (this.mSearchViewState) {
            case 1:
                return hasInputText() ^ 1;
            case 2:
                return true;
            default:
                return DEBUG;
        }
    }

    public boolean isLongshotUnsupported() {
        switch (this.mSearchState) {
            case SEARCH_SHOW /*1000*/:
                return hasInputText() ^ 1;
            case SEARCH_ANIM /*1002*/:
                return true;
            default:
                return isStretchLongshotUnsupported();
        }
    }
}
