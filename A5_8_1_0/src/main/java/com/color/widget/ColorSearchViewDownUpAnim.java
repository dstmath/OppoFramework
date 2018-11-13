package com.color.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.AnimatorSet.Builder;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.View.OnClickListener;
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
import com.color.screenshot.ColorLongshotUnsupported;
import com.color.util.ColorChangeTextUtil;

public class ColorSearchViewDownUpAnim extends LinearLayout implements OnClickListener, ColorLongshotUnsupported {
    private static final float ALPHA_FOREGROUND = 0.5f;
    private static final int ANIM_DURATION = 200;
    private static final int SEARCH_ANIM = 1002;
    private static final int SEARCH_ANIMING = 2;
    private static final int SEARCH_DOWN = 1001;
    private static final int SEARCH_SHRINK = 0;
    private static final int SEARCH_STRETCH = 1;
    private static final int SEARCH_UP = 1000;
    private AutoCompleteTextView mAutoComplete;
    private int mContentHeight;
    private AnimatorSet mCurrentShowAnim;
    private View mForeground;
    private int mForegroundBg;
    private ImageButton mImageButton;
    private int mImageButtonLeft;
    private int mImageButtonWidth;
    private boolean mIsDownAndUp;
    private boolean mIsShowForeground;
    private int mLimitPaddingR;
    private OnAnimationListener mOnAnimationListener;
    private OnClickTextButtonListener mOnClickTextButtonListener;
    private Animator mSearchGroupActionAnim;
    private LinearLayout mSearchSrc;
    private int mSearchState;
    private ColorSearchView mSearchView;
    private LinearLayout mSearchViewGroup;
    private int mSearchViewState;
    private final AnimatorListener mShowForegroundListener;
    private final Runnable mShowImeRunnable;
    private TextView mTextButton;

    public interface OnAnimationListener {
        void onDownAnimationEnd();

        void onUpAnimationEnd();
    }

    public interface OnClickTextButtonListener {
        void onClickTextButton();
    }

    public ColorSearchViewDownUpAnim(Context context) {
        this(context, null);
    }

    public ColorSearchViewDownUpAnim(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mOnClickTextButtonListener = null;
        this.mContentHeight = 0;
        this.mSearchGroupActionAnim = null;
        this.mForeground = null;
        this.mCurrentShowAnim = null;
        this.mOnAnimationListener = null;
        this.mSearchViewState = 0;
        this.mSearchState = SEARCH_DOWN;
        this.mImageButton = null;
        this.mSearchSrc = null;
        this.mImageButtonLeft = 0;
        this.mImageButtonWidth = 0;
        this.mLimitPaddingR = 0;
        this.mAutoComplete = null;
        this.mIsShowForeground = true;
        this.mIsDownAndUp = false;
        this.mShowForegroundListener = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                ColorSearchViewDownUpAnim.this.mCurrentShowAnim = null;
            }
        };
        this.mShowImeRunnable = new Runnable() {
            public void run() {
                ColorSearchViewDownUpAnim.this.showSoftInput();
            }
        };
        setBackgroundColor(context.getResources().getColor(201720886));
        LayoutInflater.from(context).inflate(201917562, this, true);
        this.mLimitPaddingR = context.getResources().getDimensionPixelSize(201655510);
        this.mImageButtonWidth = context.getResources().getDimensionPixelSize(201655512);
        this.mForegroundBg = context.getColor(201720902);
        this.mSearchView = (ColorSearchView) findViewById(201458836);
        this.mTextButton = (TextView) findViewById(201458835);
        int textSize = getResources().getDimensionPixelSize(201654415);
        this.mTextButton.setTextSize(0, (float) ((int) ColorChangeTextUtil.getSuitableFontSize((float) textSize, getResources().getConfiguration().fontScale, 2)));
        this.mSearchViewGroup = (LinearLayout) findViewById(201458834);
        this.mTextButton.setOnClickListener(this);
        this.mSearchView.onActionViewExpanded();
        this.mSearchView.clearFocus();
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

    private Animator getSearchGroupActionAnim(boolean isExpand) {
        if (isExpand) {
            this.mForeground.setVisibility(0);
            float startY = (float) (-this.mContentHeight);
            return ObjectAnimator.ofFloat(this, "Y", new float[]{startY, 0.0f});
        }
        float endY = (float) (-this.mContentHeight);
        return ObjectAnimator.ofFloat(this, "Y", new float[]{0.0f, endY});
    }

    public void setSearchAutoCompleteFocus() {
        AutoCompleteTextView autoComplete = this.mSearchView.getSearchAutoComplete();
        if (autoComplete != null) {
            autoComplete.setFocusable(true);
            autoComplete.setFocusableInTouchMode(true);
            autoComplete.requestFocus();
        }
    }

    public void setSearchAutoCompleteUnFocus() {
        setImeVisibility(false);
        this.mSearchView.clearFocus();
        this.mSearchView.setFocusable(false);
        this.mSearchView.onWindowFocusChanged(false);
        AutoCompleteTextView autoComplete = this.mSearchView.getSearchAutoComplete();
        if (autoComplete != null) {
            autoComplete.setFocusable(false);
        }
    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (this.mSearchView != null) {
            this.mSearchView.setFocusable(true);
            this.mSearchView.setFocusableInTouchMode(true);
            this.mSearchView.requestFocus();
        }
        AutoCompleteTextView autoComplete = this.mSearchView.getSearchAutoComplete();
        if (!(autoComplete == null || (this.mIsDownAndUp ^ 1) == 0)) {
            autoComplete.setFocusable(false);
        }
        return super.dispatchTouchEvent(ev);
    }

    public void setImeVisibility(boolean visible) {
        removeCallbacks(this.mShowImeRunnable);
        if (visible) {
            post(this.mShowImeRunnable);
            setBackgroundResource(201850880);
            return;
        }
        hideSoftInput();
        setSearchViewBackground(null);
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

    private void resetSearchSrcPadding(int paddingLeft, int paddingRight) {
        if (this.mSearchSrc != null) {
            this.mSearchSrc.setPadding(paddingLeft, 0, paddingRight, 0);
        }
    }

    public void setActionBar(ActionBar bar) {
        this.mContentHeight = bar.getHeight();
    }

    public void setOnAnimationListener(OnAnimationListener listener) {
        this.mOnAnimationListener = listener;
    }

    public void startSearchViewDownAnim() {
        if (SEARCH_DOWN == this.mSearchState) {
            this.mSearchState = SEARCH_ANIM;
            this.mIsShowForeground = true;
            this.mIsDownAndUp = true;
            setSearchAutoCompleteFocus();
            if (this.mSearchGroupActionAnim != null) {
                this.mSearchGroupActionAnim.end();
            }
            setImeVisibility(true);
            this.mImageButton.setLeft(0);
            this.mAutoComplete.setLeft(this.mImageButtonWidth);
            this.mSearchGroupActionAnim = getSearchGroupActionAnim(true);
            this.mSearchGroupActionAnim.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    ColorSearchViewDownUpAnim.this.mSearchGroupActionAnim = null;
                    if (ColorSearchViewDownUpAnim.SEARCH_ANIM == ColorSearchViewDownUpAnim.this.mSearchState) {
                        if (ColorSearchViewDownUpAnim.this.mOnAnimationListener != null) {
                            ColorSearchViewDownUpAnim.this.mOnAnimationListener.onUpAnimationEnd();
                        }
                        ColorSearchViewDownUpAnim.this.mSearchState = ColorSearchViewDownUpAnim.SEARCH_UP;
                        ColorSearchViewDownUpAnim.this.mAutoComplete.setHapticFeedbackEnabled(true);
                    }
                }

                public void onAnimationStart(Animator animation) {
                    if (ColorSearchViewDownUpAnim.SEARCH_ANIM == ColorSearchViewDownUpAnim.this.mSearchState) {
                        LayoutParams lpSrcText = (LayoutParams) ColorSearchViewDownUpAnim.this.mAutoComplete.getLayoutParams();
                        if (lpSrcText != null) {
                            lpSrcText.weight = 1.0f;
                            lpSrcText.width = 0;
                            ColorSearchViewDownUpAnim.this.mAutoComplete.setLayoutParams(lpSrcText);
                            ColorSearchViewDownUpAnim.this.mAutoComplete.invalidate();
                        }
                    }
                }
            });
            this.mSearchGroupActionAnim.setDuration(200);
            this.mSearchGroupActionAnim.setInterpolator(new LinearInterpolator());
            this.mSearchGroupActionAnim.start();
        }
    }

    public void startSearchViewUpAnim() {
        if (SEARCH_UP == this.mSearchState) {
            this.mSearchState = SEARCH_ANIM;
            this.mIsShowForeground = false;
            if (this.mSearchGroupActionAnim != null) {
                this.mSearchGroupActionAnim.end();
            }
            setSearchAutoCompleteUnFocus();
            this.mSearchGroupActionAnim = getSearchGroupActionAnim(false);
            this.mSearchGroupActionAnim.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    ColorSearchViewDownUpAnim.this.mSearchGroupActionAnim = null;
                    if (ColorSearchViewDownUpAnim.SEARCH_ANIM == ColorSearchViewDownUpAnim.this.mSearchState) {
                        if (ColorSearchViewDownUpAnim.this.mOnAnimationListener != null) {
                            ColorSearchViewDownUpAnim.this.mOnAnimationListener.onDownAnimationEnd();
                        }
                        ColorSearchViewDownUpAnim.this.mSearchState = ColorSearchViewDownUpAnim.SEARCH_DOWN;
                        if (ColorSearchViewDownUpAnim.this.mForeground != null) {
                            ColorSearchViewDownUpAnim.this.mForeground.setVisibility(8);
                            ColorSearchViewDownUpAnim.this.mForeground.setAlpha(1.0f);
                        }
                        LayoutParams lpSrcText = (LayoutParams) ColorSearchViewDownUpAnim.this.mAutoComplete.getLayoutParams();
                        if (lpSrcText != null) {
                            lpSrcText.weight = 0.0f;
                            lpSrcText.width = -2;
                            ColorSearchViewDownUpAnim.this.mAutoComplete.setLayoutParams(lpSrcText);
                            ColorSearchViewDownUpAnim.this.mAutoComplete.invalidate();
                        }
                        ColorSearchViewDownUpAnim.this.mAutoComplete.setHapticFeedbackEnabled(false);
                    }
                    ColorSearchViewDownUpAnim.this.mIsDownAndUp = false;
                }

                public void onAnimationStart(Animator animation) {
                    if (ColorSearchViewDownUpAnim.SEARCH_ANIM == ColorSearchViewDownUpAnim.this.mSearchState && !TextUtils.isEmpty(ColorSearchViewDownUpAnim.this.mAutoComplete.getText())) {
                        ColorSearchViewDownUpAnim.this.mSearchView.setQuery(new String(), false);
                        ColorSearchViewDownUpAnim.this.resetSearchSrcPadding(0, ColorSearchViewDownUpAnim.this.mLimitPaddingR);
                    }
                    super.onAnimationStart(animation);
                }
            });
            this.mSearchGroupActionAnim.setDuration(200);
            this.mSearchGroupActionAnim.setInterpolator(new LinearInterpolator());
            this.mSearchGroupActionAnim.start();
        }
    }

    public ColorSearchView getSearchView() {
        return this.mSearchView;
    }

    public void showForeground() {
        if (this.mForeground != null && this.mIsShowForeground) {
            if (this.mCurrentShowAnim != null) {
                this.mCurrentShowAnim.end();
            }
            this.mForeground.setVisibility(0);
            this.mCurrentShowAnim = new AnimatorSet();
            Builder b = this.mCurrentShowAnim.play(ObjectAnimator.ofFloat(this.mSearchViewGroup, "translationY", new float[]{0.0f}));
            this.mCurrentShowAnim.setInterpolator(AnimationUtils.loadInterpolator(getContext(), 17563651));
            this.mCurrentShowAnim.setDuration(200);
            this.mCurrentShowAnim.addListener(this.mShowForegroundListener);
            this.mCurrentShowAnim.start();
        }
    }

    public void onClick(View v) {
        if (v.getId() == 201458835 && this.mOnClickTextButtonListener != null) {
            this.mOnClickTextButtonListener.onClickTextButton();
        }
    }

    public void setOnClickTextButtonListener(OnClickTextButtonListener listener) {
        this.mOnClickTextButtonListener = listener;
    }

    public void setForeground(View foreground) {
        this.mForeground = foreground;
        if (this.mForeground != null) {
            this.mForeground.setBackgroundColor(this.mForegroundBg);
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

    public void startSeachViewAnim() {
        this.mSearchViewState = 2;
        this.mIsShowForeground = true;
        this.mIsDownAndUp = true;
        setSearchAutoCompleteFocus();
        if (this.mImageButton != null) {
            this.mImageButtonLeft = this.mImageButton.getLeft();
            this.mImageButtonWidth = this.mImageButton.getWidth();
            if (this.mImageButtonWidth == 0) {
                this.mImageButtonWidth = this.mImageButton.getDrawable().getMinimumWidth();
            }
        }
        if (!(this.mForeground == null || (this.mForeground.isShown() ^ 1) == 0 || this.mSearchView == null || !this.mSearchView.getQuery().toString().isEmpty())) {
            this.mForeground.setVisibility(0);
        }
        setImeVisibility(true);
    }

    public void exitSearchViewAnim() {
        this.mSearchViewState = 2;
        this.mIsShowForeground = false;
        this.mIsDownAndUp = false;
        setSearchAutoCompleteUnFocus();
        if (this.mForeground != null && this.mForeground.isShown()) {
            this.mForeground.setVisibility(8);
        }
        if (!TextUtils.isEmpty(this.mAutoComplete.getText())) {
            this.mSearchView.setQuery(new String(), false);
            resetSearchSrcPadding(0, this.mLimitPaddingR);
        }
    }

    private boolean hasInputText() {
        return this.mAutoComplete.length() > 0;
    }

    private boolean isStretchLongshotUnsupported() {
        switch (this.mSearchViewState) {
            case 1:
                return hasInputText() ^ 1;
            case 2:
                return true;
            default:
                return false;
        }
    }

    public boolean isLongshotUnsupported() {
        switch (this.mSearchState) {
            case SEARCH_UP /*1000*/:
                return hasInputText() ^ 1;
            case SEARCH_ANIM /*1002*/:
                return true;
            default:
                return isStretchLongshotUnsupported();
        }
    }
}
