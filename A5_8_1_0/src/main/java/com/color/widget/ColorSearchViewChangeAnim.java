package com.color.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.ActionBar;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SearchView;
import com.color.actionbar.app.ColorActionBarUtil.ActionBarCallback;
import com.color.screenshot.ColorLongshotUnsupported;
import com.color.util.ColorLog;

public class ColorSearchViewChangeAnim extends LinearLayout implements OnClickListener, ColorLongshotUnsupported {
    private static final float ALPHA_FOREGROUND = 0.5f;
    private static final int ONE_TWO_EIGHT = 128;
    private static final int SEARCH_ANIM = 1002;
    private static final int SEARCH_HIDE = 1001;
    private static final int SEARCH_SHOW = 1000;
    private static final int TWO_ZERO_ZERO = 200;
    private ActionBarCallback mActionBarCallback;
    private final AnimatorListener mActionBarHideListener;
    private final AnimatorListener mActionBarShowListener;
    private AnimatorSet mCurrentShowAnim;
    private View mForeground;
    private final ImageView mHomeBack;
    private int mHomeBackStartUpX;
    private int mHomeBackWidth;
    private boolean mIsActionBarShow;
    private boolean mIsCallWindowFocus;
    private boolean mIsTriggerActionBarAnim;
    private OnAnimationListener mOnAnimationListener;
    private OnClickHomebackListener mOnClickHomebackListener;
    private final ViewGroup mSearchLayout;
    private final int mSearchMarginLeft;
    private int mSearchState;
    private final SearchView mSearchView;
    private int mSearchViewRight;
    private int mSearchViewShowWidth;
    private int mSearchViewStartUpX;
    private int mSearchViewWidth;
    private final Runnable mShowActionBarRunnable;
    private final AnimatorListener mShowForegroundListener;
    private final Runnable mShowImeRunnable;
    protected final Class<?> mTagClass;
    private final int mUpMarginLeft;

    private abstract class BaseAnimListener extends AnimatorListenerAdapter {
        private boolean mIsCancel;

        /* synthetic */ BaseAnimListener(ColorSearchViewChangeAnim this$0, BaseAnimListener -this1) {
            this();
        }

        private BaseAnimListener() {
            this.mIsCancel = false;
        }

        public void onAnimationCancel(Animator animation) {
            this.mIsCancel = true;
        }

        public void onAnimationEnd(Animator animation) {
            this.mIsCancel = false;
        }

        boolean isCancel() {
            return this.mIsCancel;
        }
    }

    private class ActionBarAnimListener extends BaseAnimListener {
        private boolean mIsCancel = false;
        private final boolean mIsShow;

        public ActionBarAnimListener(boolean isShow) {
            super(ColorSearchViewChangeAnim.this, null);
            this.mIsShow = isShow;
        }

        public void onAnimationCancel(Animator animation) {
            this.mIsCancel = true;
        }

        public void onAnimationEnd(Animator animation) {
            this.mIsCancel = false;
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
            super(ColorSearchViewChangeAnim.this, null);
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
            getTarget().setAlpha(isCancel() ? getStartValue() : getEndValue());
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

    public interface OnClickHomebackListener {
        void onClickHomeback();
    }

    public ColorSearchViewChangeAnim(Context context) {
        this(context, null);
    }

    public ColorSearchViewChangeAnim(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mTagClass = getClass();
        this.mActionBarCallback = null;
        this.mCurrentShowAnim = null;
        this.mForeground = null;
        this.mOnClickHomebackListener = null;
        this.mOnAnimationListener = null;
        this.mIsTriggerActionBarAnim = false;
        this.mIsActionBarShow = false;
        this.mHomeBackWidth = 0;
        this.mSearchViewWidth = 0;
        this.mSearchViewShowWidth = 0;
        this.mSearchViewStartUpX = 0;
        this.mHomeBackStartUpX = 0;
        this.mSearchViewRight = 0;
        this.mSearchState = SEARCH_HIDE;
        this.mIsCallWindowFocus = true;
        this.mShowForegroundListener = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                ColorSearchViewChangeAnim.this.mCurrentShowAnim = null;
            }
        };
        this.mActionBarHideListener = new ActionBarAnimListener(this, false) {
            public void onAnimationEnd(Animator animation) {
                if (ColorSearchViewChangeAnim.SEARCH_ANIM == this.mSearchState && this.mIsTriggerActionBarAnim) {
                    if (!(this.mOnAnimationListener == null || (isCancel() ^ 1) == 0)) {
                        this.mOnAnimationListener.onShowAnimationEnd();
                    }
                    this.mSearchState = isCancel() ? ColorSearchViewChangeAnim.SEARCH_HIDE : ColorSearchViewChangeAnim.SEARCH_SHOW;
                    this.mCurrentShowAnim = null;
                    this.mIsTriggerActionBarAnim = false;
                }
                super.onAnimationEnd(animation);
            }
        };
        this.mActionBarShowListener = new ActionBarAnimListener(this, true) {
            public void onAnimationEnd(Animator animation) {
                if (ColorSearchViewChangeAnim.SEARCH_ANIM == this.mSearchState && this.mIsTriggerActionBarAnim) {
                    this.removeCallbacks(this.mShowActionBarRunnable);
                    if (!(this.mOnAnimationListener == null || (isCancel() ^ 1) == 0)) {
                        this.mOnAnimationListener.onHideAnimationEnd();
                    }
                    if (this.mForeground != null) {
                        this.mForeground.setVisibility(8);
                        this.mForeground.setAlpha(1.0f);
                    }
                    this.mSearchState = isCancel() ? ColorSearchViewChangeAnim.SEARCH_SHOW : ColorSearchViewChangeAnim.SEARCH_HIDE;
                    this.mIsTriggerActionBarAnim = false;
                }
                super.onAnimationEnd(animation);
            }
        };
        this.mShowActionBarRunnable = new Runnable() {
            public void run() {
                ColorSearchViewChangeAnim.this.showActionBar();
            }
        };
        this.mShowImeRunnable = new Runnable() {
            public void run() {
                ColorSearchViewChangeAnim.this.showSoftInput();
            }
        };
        setBackgroundResource(201852147);
        LayoutInflater.from(context).inflate(201917529, this, true);
        this.mSearchMarginLeft = context.getResources().getDimensionPixelSize(201655458);
        this.mUpMarginLeft = context.getResources().getDimensionPixelSize(201655430);
        this.mSearchLayout = (ViewGroup) findViewById(201458834);
        this.mHomeBack = (ImageView) findViewById(201458835);
        this.mSearchView = (SearchView) findViewById(201458836);
        this.mSearchView.onActionViewExpanded();
        unfocusSearchAutoComplete();
        this.mHomeBack.setOnClickListener(this);
        resetPosition(this.mSearchMarginLeft);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mIsCallWindowFocus = false;
        resetState();
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (this.mSearchView != null) {
            this.mSearchView.setFocusable(true);
            this.mSearchView.setFocusableInTouchMode(true);
            this.mSearchView.requestFocus();
        }
        return super.dispatchTouchEvent(ev);
    }

    public void onClick(View v) {
        if (v.getId() == 201458835 && this.mOnClickHomebackListener != null) {
            this.mOnClickHomebackListener.onClickHomeback();
        }
    }

    public boolean isLongshotUnsupported() {
        switch (this.mSearchState) {
            case SEARCH_SHOW /*1000*/:
                return hasInputText() ^ 1;
            case SEARCH_ANIM /*1002*/:
                return true;
            default:
                return false;
        }
    }

    public SearchView getSearchView() {
        return this.mSearchView;
    }

    public void setOnClickHomebackListener(OnClickHomebackListener listener) {
        this.mOnClickHomebackListener = listener;
    }

    public void setOnAnimationListener(OnAnimationListener listener) {
        this.mOnAnimationListener = listener;
    }

    public void setActionBar(ActionBar bar) {
        if (bar != null) {
            this.mIsActionBarShow = isActionBarShowing();
            this.mActionBarCallback = (ActionBarCallback) bar;
            this.mActionBarCallback.addHideListener(this.mActionBarHideListener);
            this.mActionBarCallback.addShowListener(this.mActionBarShowListener);
            this.mActionBarCallback.setSearchBarMode(true);
        }
    }

    public void setActionModeAnim(boolean flag) {
        if (this.mActionBarCallback != null) {
            this.mActionBarCallback.setActionModeAnim(flag);
        }
    }

    public void startSearchViewUpAnim() {
        if (SEARCH_HIDE == this.mSearchState) {
            this.mSearchState = SEARCH_ANIM;
            setSearchAutoCompleteFocus(false);
            this.mSearchLayout.setBackground(null);
            if (this.mActionBarCallback != null && isActionBarShowing()) {
                resetPosition(0);
                setImeVisibility(true);
                this.mSearchViewRight = this.mSearchLayout.getRight();
                if (this.mHomeBack != null) {
                    this.mHomeBackWidth = this.mHomeBack.getWidth();
                    this.mHomeBackStartUpX = this.mHomeBack.getLeft();
                }
                if (this.mHomeBackWidth == 0) {
                    this.mHomeBackWidth = this.mHomeBack.getBackground().getMinimumWidth();
                }
                if (this.mSearchView != null) {
                    this.mSearchViewStartUpX = this.mSearchView.getLeft();
                    this.mSearchViewWidth = this.mSearchView.getWidth();
                    this.mSearchViewShowWidth = getWidth() - (this.mSearchMarginLeft * 2);
                }
                ColorLog.i("log.key.search_view.anim", this.mTagClass, new Object[]{"startUpAnim ActionBar=", this.mActionBarCallback, ", isShowing()=" + isActionBarShowing(), ", mIsTriggerActionBarAnim=", Boolean.valueOf(this.mIsTriggerActionBarAnim), ", homeBackStartX=", Integer.valueOf(this.mHomeBackStartUpX), ", mSearchViewStartUpX=", Integer.valueOf(this.mSearchViewStartUpX), ", mSearchViewWidth=", Integer.valueOf(this.mSearchViewWidth), ", mHomeBackWidth=", Integer.valueOf(this.mHomeBackWidth), ", mSearchViewShowWidth=", Integer.valueOf(this.mSearchViewShowWidth), ", mSearchViewRight=", Integer.valueOf(this.mSearchViewRight)});
                ColorLog.i("log.key.search_view.anim", this.mTagClass, new Object[]{"mSearchLayout width == ", Integer.valueOf(this.mSearchLayout.getMeasuredWidth())});
                float startValue = (float) this.mSearchViewWidth;
                float endValue = (float) (this.mSearchViewRight - this.mUpMarginLeft);
                if (isLayoutRtl()) {
                    startValue = (float) this.mSearchViewWidth;
                    endValue = (float) ((this.mSearchViewRight - this.mSearchMarginLeft) - this.mUpMarginLeft);
                }
                ObjectAnimator animSearchWidth = ObjectAnimator.ofFloat(this.mSearchView, "width", new float[]{startValue, endValue});
                animSearchWidth.addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float newWidth = ((Float) animation.getAnimatedValue()).floatValue();
                        if (ColorSearchViewChangeAnim.this.mSearchView != null) {
                            LayoutParams lp = (LayoutParams) ColorSearchViewChangeAnim.this.mSearchView.getLayoutParams();
                            if (lp != null) {
                                lp.width = (int) newWidth;
                                ColorSearchViewChangeAnim.this.mSearchView.setLayoutParams(lp);
                            }
                        }
                    }
                });
                addSearchAnimation(animSearchWidth, new AnimWidthListener(this.mSearchView, startValue, endValue));
                ObjectAnimator animSearchX = ObjectAnimator.ofFloat(this.mSearchView, "x", new float[]{(float) this.mSearchMarginLeft, (float) this.mUpMarginLeft});
                animSearchX.addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float leftX = ((Float) animation.getAnimatedValue()).floatValue();
                        if (ColorSearchViewChangeAnim.this.mSearchView != null) {
                            ColorSearchViewChangeAnim.this.mSearchView.setLeft((int) leftX);
                        }
                    }
                });
                addSearchAnimation(animSearchX, new AnimXListener(this.mSearchView, startValue, endValue));
                startValue = (float) (-this.mHomeBackWidth);
                endValue = 0.0f;
                if (isLayoutRtl()) {
                    startValue = (float) this.mSearchViewRight;
                    endValue = (float) ((this.mSearchViewRight - this.mHomeBackWidth) - this.mSearchMarginLeft);
                }
                addSearchAnimation(ObjectAnimator.ofFloat(this.mHomeBack, "x", new float[]{startValue, endValue}), new AnimXListener(this.mHomeBack, startValue, endValue));
                if (!(this.mForeground == null || (this.mForeground.isShown() ^ 1) == 0 || this.mSearchView == null || !this.mSearchView.getQuery().toString().isEmpty())) {
                    this.mForeground.setVisibility(0);
                    addSearchAnimation(ObjectAnimator.ofFloat(this.mForeground, "alpha", new float[]{0.0f, ALPHA_FOREGROUND}), new AnimAlphaListener(this.mForeground, 0.0f, ALPHA_FOREGROUND));
                }
                hideActionBar();
            }
        }
    }

    public void startSearchViewDownAnim() {
        if (SEARCH_SHOW == this.mSearchState) {
            this.mSearchState = SEARCH_ANIM;
            unfocusSearchAutoComplete();
            if (this.mCurrentShowAnim != null) {
                this.mCurrentShowAnim.end();
            }
            if (!(this.mActionBarCallback == null || (isActionBarShowing() ^ 1) == 0)) {
                float startValue = 0.0f;
                float endValue = (float) (-this.mHomeBackWidth);
                if (isLayoutRtl()) {
                    startValue = (float) ((this.mSearchViewRight - this.mHomeBackWidth) - this.mSearchMarginLeft);
                    endValue = (float) this.mSearchViewRight;
                }
                addSearchAnimation(ObjectAnimator.ofFloat(this.mHomeBack, "x", new float[]{startValue, endValue}), new AnimXListener(this.mHomeBack, startValue, endValue));
                startValue = (float) this.mUpMarginLeft;
                endValue = (float) this.mSearchMarginLeft;
                if (isLayoutRtl()) {
                    startValue = (float) this.mSearchMarginLeft;
                    endValue = (float) this.mSearchMarginLeft;
                }
                addSearchAnimation(ObjectAnimator.ofFloat(this.mSearchView, "x", new float[]{startValue, endValue}), new AnimXListener(this.mSearchView, startValue, endValue));
                startValue = (float) (this.mSearchViewRight - this.mUpMarginLeft);
                endValue = (float) this.mSearchViewWidth;
                if (isLayoutRtl()) {
                    startValue = (float) ((this.mSearchViewRight - this.mSearchMarginLeft) - this.mUpMarginLeft);
                    endValue = (float) this.mSearchViewWidth;
                }
                ObjectAnimator animSearchWidth = ObjectAnimator.ofFloat(this.mSearchView, "width", new float[]{startValue, endValue});
                animSearchWidth.addUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float newWidth = ((Float) animation.getAnimatedValue()).floatValue();
                        LayoutParams lp = (LayoutParams) ColorSearchViewChangeAnim.this.mSearchView.getLayoutParams();
                        if (lp != null) {
                            lp.width = (int) newWidth;
                            ColorSearchViewChangeAnim.this.mSearchView.setLayoutParams(lp);
                        }
                    }
                });
                addSearchAnimation(animSearchWidth, new AnimWidthListener(this.mSearchView, startValue, endValue));
                if (this.mForeground != null && this.mForeground.isShown()) {
                    addSearchAnimation(ObjectAnimator.ofFloat(this.mForeground, "alpha", new float[]{ALPHA_FOREGROUND, 0.0f}), new AnimAlphaListener(this.mForeground, ALPHA_FOREGROUND, 0.0f));
                }
                post(this.mShowActionBarRunnable);
                ColorLog.i("log.key.search_view.anim", this.mTagClass, new Object[]{"startSearchViewDownAnim ActionBar=", this.mActionBarCallback, ", isShowing()=", Boolean.valueOf(isActionBarShowing()), ", mIsActionBarShow=", Boolean.valueOf(this.mIsTriggerActionBarAnim), ", mHomeBackWidth=", Integer.valueOf(this.mHomeBackWidth), ", mSearchViewStartUpX=", Integer.valueOf(this.mSearchViewStartUpX), ", mSearchViewShowWidth=", Integer.valueOf(this.mSearchViewShowWidth), ", mSearchViewWidth=", Integer.valueOf(this.mSearchViewWidth), ", mHomeBackStartUpX=", Integer.valueOf(this.mHomeBackStartUpX)});
            }
        }
    }

    public void setSearchViewDisabled() {
        AutoCompleteTextView autoComplete = this.mSearchView.getSearchAutoComplete();
        if (autoComplete != null) {
            autoComplete.setFocusableInTouchMode(false);
            autoComplete.setFocusable(false);
            autoComplete.setEnabled(false);
        }
        this.mSearchView.setSearchViewBackground();
    }

    public void setSearchViewEnabled() {
        AutoCompleteTextView autoComplete = this.mSearchView.getSearchAutoComplete();
        if (autoComplete != null) {
            autoComplete.setEnabled(true);
        }
        this.mSearchView.setSearchViewBackground();
    }

    public void setSearchAutoCompleteFocus(boolean isImeVisiblity) {
        AutoCompleteTextView autoComplete = this.mSearchView.getSearchAutoComplete();
        ColorLog.i("log.key.search_view.anim", this.mTagClass, new Object[]{"setSearchAutoCompleteFocus : autoComplete=", autoComplete});
        if (autoComplete != null) {
            autoComplete.setFocusable(true);
            autoComplete.setFocusableInTouchMode(true);
            autoComplete.requestFocus();
        }
        if (isImeVisiblity) {
            setImeVisibility(true);
        }
    }

    public void setStateRestore() {
        resetState();
    }

    public void setForeground(View foreground) {
        this.mForeground = foreground;
        if (this.mForeground != null) {
            this.mForeground.setBackgroundColor(Color.argb(ONE_TWO_EIGHT, 0, 0, 0));
        }
    }

    public void showForeground() {
        if (this.mForeground != null) {
            if (this.mCurrentShowAnim != null) {
                this.mCurrentShowAnim.end();
            }
            this.mForeground.setVisibility(0);
            this.mCurrentShowAnim = new AnimatorSet();
            this.mCurrentShowAnim.play(ObjectAnimator.ofFloat(this.mSearchLayout, "translationY", new float[]{0.0f})).with(ObjectAnimator.ofFloat(this.mForeground, "alpha", new float[]{0.0f, ALPHA_FOREGROUND}));
            this.mCurrentShowAnim.setInterpolator(AnimationUtils.loadInterpolator(getContext(), 17563651));
            this.mCurrentShowAnim.setDuration(200);
            this.mCurrentShowAnim.addListener(this.mShowForegroundListener);
            this.mCurrentShowAnim.start();
        }
    }

    public void restoreState() {
        if (this.mSearchState == SEARCH_SHOW) {
            this.mSearchState = SEARCH_HIDE;
            unfocusSearchAutoComplete();
            if (this.mCurrentShowAnim != null) {
                this.mCurrentShowAnim.end();
            }
            if (this.mHomeBack != null) {
                this.mHomeBack.setVisibility(8);
            }
            if (this.mSearchView != null) {
                resetPosition(this.mSearchMarginLeft);
                LayoutParams lp = (LayoutParams) this.mSearchView.getLayoutParams();
                if (lp != null) {
                    lp.width = this.mSearchViewWidth;
                    this.mSearchView.setLayoutParams(lp);
                }
            }
            if (this.mForeground != null) {
                this.mForeground.setVisibility(8);
                this.mForeground.setAlpha(1.0f);
            }
        }
    }

    private void resetState() {
        removeRunnables();
        cancelActionBarShowHide();
        unfocusSearchAutoComplete();
        hideSoftInput();
        if (this.mCurrentShowAnim != null) {
            this.mCurrentShowAnim.cancel();
        }
    }

    private void unfocusSearchAutoComplete() {
        setImeVisibility(false);
        this.mSearchView.clearFocus();
        this.mSearchView.setFocusable(false);
        if (this.mIsCallWindowFocus) {
            this.mSearchView.onWindowFocusChanged(false);
        } else {
            this.mIsCallWindowFocus = true;
        }
        AutoCompleteTextView autoComplete = this.mSearchView.getSearchAutoComplete();
        ColorLog.i("log.key.search_view.anim", this.mTagClass, new Object[]{"unfocusSearchAutoComplete : autoComplete= ", autoComplete});
        if (autoComplete != null) {
            autoComplete.setFocusable(false);
        }
    }

    private void setImeVisibility(boolean visible) {
        removeCallbacks(this.mShowImeRunnable);
        if (visible) {
            post(this.mShowImeRunnable);
            setBackgroundResource(201850880);
            return;
        }
        hideSoftInput();
        setBackgroundResource(201852147);
    }

    private void resetPosition(int paddingLeft) {
        if (this.mSearchView != null) {
            LayoutParams lp = new LayoutParams(-1, -2);
            lp.setMargins(paddingLeft, 0, paddingLeft, 0);
            this.mSearchView.setLayoutParams(lp);
        }
    }

    private boolean isActionBarShowing() {
        if (this.mActionBarCallback != null) {
            return ((ActionBar) this.mActionBarCallback).isShowing();
        }
        return false;
    }

    private void resetActionBar() {
        if (this.mActionBarCallback != null) {
            ActionBar bar = this.mActionBarCallback;
            bar.setShowHideAnimationEnabled(false);
            bar.show();
            bar.setShowHideAnimationEnabled(true);
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

    private void addSearchAnimation(Animator anim, AnimatorListener listener) {
        anim.addListener(listener);
        this.mActionBarCallback.addWithAnimator(anim);
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

    private boolean hasInputText() {
        if (this.mSearchView.getSearchAutoComplete().length() > 0) {
            return true;
        }
        return false;
    }
}
