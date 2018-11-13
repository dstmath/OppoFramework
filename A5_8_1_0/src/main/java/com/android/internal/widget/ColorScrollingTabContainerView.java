package com.android.internal.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.ActionBar.Tab;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.text.Layout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.color.actionbar.app.ColorActionBarUtil.ScrollTabCallback;
import com.color.util.ColorChangeTextUtil;
import com.color.util.ColorContextUtil;
import java.util.ArrayList;
import oppo.R;
import oppo.content.res.OppoFontUtils;

public class ColorScrollingTabContainerView extends ScrollingTabContainerView implements ScrollTabCallback {
    private static final int COUNT_FOUR = 4;
    private static final int COUNT_ONE = 1;
    private static final int COUNT_THREE = 3;
    private static final int COUNT_TWO = 2;
    private static final boolean DBG = false;
    private static final String SUSPENSION = ".";
    private static final String TAG = "ColorScrollingTabContainerView";
    private static final float TEXT_SIZE_SCALE = 0.88f;
    private AnimationImpl mAnimation = null;
    private int mDefautlTabTextSize = 0;
    private int mFirstScaledTabTextSize = 0;
    private boolean mHasEmbeddedTabs = false;
    private int mIndexForSelection;
    private int mMaxMeasuredTabTextWiddth = 0;
    private int mMaxTabWidthFirstLevel = 0;
    private int mMaxTabWidthSecondLevel = 0;
    private int mMaxTextWidth = 0;
    private Typeface mMediumTypeface = null;
    private int mMinTextWidthMoreThanFour = 0;
    private int mScrollingTabContainerWidth = 0;
    private int mSecondSCaledTabTextSize = 0;
    private float mSelectionOffset;
    private int mSplitedAveragWidth = 0;
    private int mTabTextSize = 0;
    private int mTabTextSizeSelected = 0;
    private int mTabViewMaxPadding;
    private int mTabViewPadding;
    private Paint mTextPaint = null;
    private ArrayList<Integer> mTitleWidthList = new ArrayList();
    private ArrayList<Integer> mTitleWidthShrinkList = new ArrayList();
    private ArrayList<Integer> mTitleWidthShrinkTwiceList = new ArrayList();

    private class AnimationImpl implements OnClickListener, AnimatorUpdateListener {
        private Drawable mAfterSelected;
        private float mAnimateTabOffset;
        private float mAnimateTabOutLength;
        private int mAnimateTabWidth;
        private Drawable mBeforeSelected;
        private int mDx;
        private Drawable mHeadSelected;
        private boolean mIsBegin;
        private boolean mIsCleared;
        private boolean mIsClicked;
        private boolean mIsDrag;
        private boolean mIsTransparent;
        private int mItemWidth;
        private Layout mLayout;
        private float mLeftOffset;
        private Drawable mMiddleSelected;
        private Drawable mMoveDrawable;
        private final AnimatorListener mMoveListener;
        private Drawable mNomalUnselected;
        private float mRightOffset;
        private Drawable mScorllingTabBackground;
        private int mSelectedTextAlpha;
        private OppoDrawableHolder mShapeHolder;
        private boolean mShowAnimationByClick;
        private int mState;
        private Drawable mTailSelected;

        /* synthetic */ AnimationImpl(ColorScrollingTabContainerView this$0, Context context, AnimationImpl -this2) {
            this(context);
        }

        private AnimationImpl(Context context) {
            this.mSelectedTextAlpha = 255;
            this.mScorllingTabBackground = null;
            this.mMoveDrawable = null;
            this.mHeadSelected = null;
            this.mMiddleSelected = null;
            this.mTailSelected = null;
            this.mAfterSelected = null;
            this.mBeforeSelected = null;
            this.mNomalUnselected = null;
            this.mShapeHolder = null;
            this.mShowAnimationByClick = false;
            this.mIsDrag = false;
            this.mIsBegin = false;
            this.mIsClicked = false;
            this.mIsCleared = false;
            this.mIsTransparent = false;
            this.mLeftOffset = 0.0f;
            this.mRightOffset = 0.0f;
            this.mAnimateTabOutLength = 0.0f;
            this.mAnimateTabOffset = 0.0f;
            this.mAnimateTabWidth = 0;
            this.mItemWidth = 0;
            this.mState = 0;
            this.mLayout = null;
            this.mDx = 0;
            this.mMoveListener = new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator animation) {
                    AnimationImpl.this.mShowAnimationByClick = true;
                    AnimationImpl.this.mIsBegin = true;
                    if (!AnimationImpl.this.mIsCleared) {
                        AnimationImpl.this.clearSelectTabBackground();
                    }
                }

                public void onAnimationEnd(Animator animation) {
                    AnimationImpl.this.mShowAnimationByClick = false;
                    AnimationImpl.this.updateTabBackground(ColorScrollingTabContainerView.this.mTabLayout.getChildCount(), ColorScrollingTabContainerView.this.mSelectedTabIndex);
                    AnimationImpl.this.mIsBegin = false;
                    AnimationImpl.this.mIsClicked = false;
                    ColorScrollingTabContainerView.this.invalidate();
                }
            };
            Resources r = context.getResources();
            ColorScrollingTabContainerView.this.setOverScrollMode(2);
            ColorScrollingTabContainerView.this.mTabTextSize = r.getDimensionPixelSize(201654287);
            ColorScrollingTabContainerView.this.mTabTextSizeSelected = r.getDimensionPixelSize(201655356);
            ColorScrollingTabContainerView.this.mMinTextWidthMoreThanFour = r.getDimensionPixelOffset(201655567);
            TypedArray a = context.obtainStyledAttributes(null, R.styleable.ColorScrollingTabContainerView, 201392191, 0);
            this.mScorllingTabBackground = a.getDrawable(11);
            if (this.mScorllingTabBackground != null) {
                ColorScrollingTabContainerView.this.setBackground(this.mScorllingTabBackground);
            }
            this.mMoveDrawable = a.getDrawable(0);
            this.mHeadSelected = a.getDrawable(1);
            this.mMiddleSelected = a.getDrawable(2);
            this.mTailSelected = a.getDrawable(3);
            this.mAfterSelected = a.getDrawable(4);
            this.mBeforeSelected = a.getDrawable(5);
            this.mNomalUnselected = a.getDrawable(6);
            int textColor = a.getColor(10, 0);
            int textColorAlpha = Color.alpha(textColor);
            if (textColorAlpha > 0 && textColorAlpha <= 255) {
                this.mSelectedTextAlpha = textColorAlpha;
            }
            this.mAnimateTabOutLength = (float) a.getDimensionPixelSize(7, 0);
            this.mAnimateTabOffset = (float) a.getDimensionPixelSize(8, 0);
            this.mIsTransparent = a.getBoolean(12, false);
            ColorScrollingTabContainerView.this.mMaxTabWidthFirstLevel = ColorScrollingTabContainerView.this.getResources().getDimensionPixelSize(201655565);
            ColorScrollingTabContainerView.this.mMaxTabWidthSecondLevel = ColorScrollingTabContainerView.this.getResources().getDimensionPixelSize(201655566);
            ColorScrollingTabContainerView.this.mTabViewPadding = ColorScrollingTabContainerView.this.getResources().getDimensionPixelSize(201654457);
            ColorScrollingTabContainerView.this.mTabViewMaxPadding = ColorScrollingTabContainerView.this.getResources().getDimensionPixelSize(201654461);
            ColorScrollingTabContainerView.this.mDefautlTabTextSize = ColorScrollingTabContainerView.this.getResources().getDimensionPixelSize(201654415);
            ColorScrollingTabContainerView.this.mDefautlTabTextSize = (int) ColorChangeTextUtil.getSuitableFontSize((float) ColorScrollingTabContainerView.this.mDefautlTabTextSize, ColorScrollingTabContainerView.this.getResources().getConfiguration().fontScale, 2);
            ColorScrollingTabContainerView.this.mFirstScaledTabTextSize = ColorScrollingTabContainerView.this.mDefautlTabTextSize;
            ColorScrollingTabContainerView.this.mSecondSCaledTabTextSize = ColorScrollingTabContainerView.this.mDefautlTabTextSize;
            this.mShapeHolder = new OppoDrawableHolder(ColorScrollingTabContainerView.this, null);
            this.mShapeHolder.setX(-this.mAnimateTabOutLength);
            ColorScrollingTabContainerView.this.mTextPaint = new TextPaint();
            ColorScrollingTabContainerView.this.mTextPaint.setAntiAlias(true);
            ColorScrollingTabContainerView.this.mTextPaint.setColor(textColor);
            ColorScrollingTabContainerView.this.mTextPaint.setTextSize((float) ColorScrollingTabContainerView.this.mTabTextSizeSelected);
            ColorScrollingTabContainerView.this.mTextPaint.setTextAlign(Align.CENTER);
            a.recycle();
        }

        public void onClick(View view) {
            if (!this.mIsBegin && ColorScrollingTabContainerView.this.mTabLayout.getChildAt(ColorScrollingTabContainerView.this.mSelectedTabIndex) != view) {
                this.mIsClicked = true;
                ((TabView) view).getTab().select();
                int tabCount = ColorScrollingTabContainerView.this.mTabLayout.getChildCount();
                for (int i = 0; i < tabCount; i++) {
                    View child = ColorScrollingTabContainerView.this.mTabLayout.getChildAt(i);
                    if (child != view) {
                        child.setSelected(false);
                    }
                }
            }
        }

        public void onAnimationUpdate(ValueAnimator animation) {
            if (this.mShowAnimationByClick) {
                ColorScrollingTabContainerView.this.invalidate();
            }
        }

        public boolean isCleared() {
            return this.mIsCleared;
        }

        private int getTabTextPaddingLeft() {
            return ColorScrollingTabContainerView.this.getContext().getResources().getDimensionPixelSize(201655348);
        }

        private int getTabTextPaddingRight() {
            return ColorScrollingTabContainerView.this.getContext().getResources().getDimensionPixelSize(201655349);
        }

        private void addTab(TabView tabView) {
            updateTabBackground(ColorScrollingTabContainerView.this.mTabLayout.getChildCount(), ColorScrollingTabContainerView.this.mSelectedTabIndex);
        }

        private void addTab(TabView tabView, int position) {
            updateTabBackground(ColorScrollingTabContainerView.this.mTabLayout.getChildCount(), ColorScrollingTabContainerView.this.mSelectedTabIndex);
        }

        private BitmapDrawable createDrawable(int resId) {
            Resources res = ColorScrollingTabContainerView.this.getResources();
            return new BitmapDrawable(res, BitmapFactory.decodeResource(res, resId));
        }

        private void setTabSelected(int position) {
            int tabCount = ColorScrollingTabContainerView.this.mTabLayout.getChildCount();
            int oldeSelected = ColorScrollingTabContainerView.this.mSelectedTabIndex;
            ColorScrollingTabContainerView.this.mSelectedTabIndex = position;
            if (tabCount > 0) {
                this.mItemWidth = ColorScrollingTabContainerView.this.mTabLayout.getWidth() / tabCount;
            }
            this.mAnimateTabWidth = this.mItemWidth + (((int) this.mAnimateTabOutLength) * 2);
            if (!this.mIsClicked || !this.mShowAnimationByClick) {
                if (this.mIsClicked) {
                    AnimatorSet anim = new AnimatorSet();
                    float endTarsX = ((float) (ColorScrollingTabContainerView.this.getPaddingLeft() + (this.mItemWidth * ColorScrollingTabContainerView.this.mSelectedTabIndex))) - this.mAnimateTabOutLength;
                    float startTarsX = ((float) (ColorScrollingTabContainerView.this.getPaddingLeft() + (this.mItemWidth * oldeSelected))) - this.mAnimateTabOutLength;
                    ObjectAnimator.ofFloat(this.mShapeHolder, "x", new float[]{startTarsX, endTarsX}).addUpdateListener(this);
                    anim.setDuration((long) ((Math.abs(ColorScrollingTabContainerView.this.mSelectedTabIndex - oldeSelected) + 1) * 100));
                    anim.addListener(this.mMoveListener);
                    anim.start();
                } else {
                    int i = 0;
                    while (i < tabCount) {
                        View child = ColorScrollingTabContainerView.this.mTabLayout.getChildAt(i);
                        boolean isSelected = i == position;
                        child.setSelected(isSelected);
                        if (isSelected) {
                            ColorScrollingTabContainerView.this.animateToTab(position);
                        } else if (!this.mIsDrag) {
                            child.setBackground(this.mNomalUnselected);
                        }
                        i++;
                    }
                    if (!this.mIsDrag) {
                        updateTabBackground(tabCount, position);
                    }
                }
            }
        }

        private void updateScrollState(int state) {
            if (state == 0) {
                this.mIsDrag = false;
                if (this.mState == 1) {
                    updateTabBackground(ColorScrollingTabContainerView.this.mTabLayout.getChildCount(), ColorScrollingTabContainerView.this.mSelectedTabIndex);
                    this.mIsBegin = false;
                } else if (!(this.mIsClicked || (this.mShowAnimationByClick ^ 1) == 0)) {
                    updateTabBackground(ColorScrollingTabContainerView.this.mTabLayout.getChildCount(), ColorScrollingTabContainerView.this.mSelectedTabIndex);
                    this.mIsBegin = false;
                }
                ColorScrollingTabContainerView.this.invalidate();
            } else if (1 == state) {
                this.mIsDrag = true;
                this.mIsBegin = true;
                this.mShowAnimationByClick = false;
                if (this.mIsClicked) {
                    this.mShowAnimationByClick = false;
                    this.mIsClicked = false;
                }
            } else if (2 == state) {
                if (!this.mIsCleared) {
                    clearSelectTabBackground();
                }
                this.mIsDrag = true;
                this.mIsBegin = true;
            }
            this.mState = state;
        }

        private void updateAnimateTab(int position, float positionOffset, int positionOffsetPixels) {
            float offset = (((float) ColorScrollingTabContainerView.this.getPaddingLeft()) + (((float) this.mItemWidth) * (((float) position) + positionOffset))) - this.mAnimateTabOutLength;
            if (!this.mShowAnimationByClick && (this.mIsClicked ^ 1) != 0 && this.mState != 0) {
                this.mShapeHolder.setX(offset);
                ColorScrollingTabContainerView.this.mIndexForSelection = position;
                ColorScrollingTabContainerView.this.mSelectionOffset = positionOffset;
                ColorScrollingTabContainerView.this.invalidate();
            }
        }

        private void updateTabBackground(int count, int selected) {
            if (count != 0 && selected >= 0 && selected < count) {
                this.mIsCleared = false;
                ColorScrollingTabContainerView.this.mTabLayout.getChildAt(selected).setSelected(true);
                if (selected == 0) {
                    ColorScrollingTabContainerView.this.mTabLayout.getChildAt(selected).setBackground(this.mHeadSelected);
                    if (count != 1) {
                        ColorScrollingTabContainerView.this.mTabLayout.getChildAt(selected + 1).setBackground(this.mAfterSelected);
                    }
                } else if (count - 1 == selected) {
                    ColorScrollingTabContainerView.this.mTabLayout.getChildAt(selected - 1).setBackground(this.mBeforeSelected);
                    ColorScrollingTabContainerView.this.mTabLayout.getChildAt(selected).setBackground(this.mTailSelected);
                } else {
                    ColorScrollingTabContainerView.this.mTabLayout.getChildAt(selected - 1).setBackground(this.mBeforeSelected);
                    ColorScrollingTabContainerView.this.mTabLayout.getChildAt(selected).setBackground(this.mMiddleSelected);
                    ColorScrollingTabContainerView.this.mTabLayout.getChildAt(selected + 1).setBackground(this.mAfterSelected);
                }
            }
        }

        private void clearSelectTabBackground() {
            this.mIsCleared = true;
            int tabCount = ColorScrollingTabContainerView.this.mTabLayout.getChildCount();
            if (tabCount > 0) {
                if (this.mItemWidth <= 0) {
                    this.mItemWidth = ColorScrollingTabContainerView.this.mTabLayout.getWidth() / tabCount;
                    this.mAnimateTabWidth = this.mItemWidth + (((int) this.mAnimateTabOutLength) * 2);
                }
                if (!this.mIsClicked) {
                    this.mShapeHolder.setX(((float) (ColorScrollingTabContainerView.this.getPaddingLeft() + (this.mItemWidth * ColorScrollingTabContainerView.this.mSelectedTabIndex))) - this.mAnimateTabOutLength);
                }
                for (int i = 0; i < tabCount; i++) {
                    View child = ColorScrollingTabContainerView.this.mTabLayout.getChildAt(i);
                    child.setBackground(this.mNomalUnselected);
                    if (ColorScrollingTabContainerView.this.mSelectedTabIndex == i) {
                        child.setSelected(false);
                    }
                }
            }
        }

        private void drawTab(Canvas canvas) {
            if (this.mIsBegin) {
                canvas.save();
                if (this.mShapeHolder.getX() < (-this.mAnimateTabOutLength) + this.mAnimateTabOffset) {
                    this.mLeftOffset = this.mAnimateTabOffset - (this.mShapeHolder.getX() + this.mAnimateTabOutLength);
                    this.mRightOffset = 0.0f;
                } else if (this.mShapeHolder.getX() > ((((float) (ColorScrollingTabContainerView.this.mTabLayout.getWidth() - this.mAnimateTabWidth)) + this.mAnimateTabOutLength) - this.mAnimateTabOffset) + ((float) ColorScrollingTabContainerView.this.getPaddingLeft())) {
                    this.mRightOffset = this.mAnimateTabOffset - ((((float) (ColorScrollingTabContainerView.this.mTabLayout.getWidth() - this.mAnimateTabWidth)) + this.mAnimateTabOutLength) - this.mShapeHolder.getX());
                    this.mLeftOffset = 0.0f;
                } else {
                    this.mLeftOffset = 0.0f;
                    this.mRightOffset = 0.0f;
                }
                int childCount = ColorScrollingTabContainerView.this.mTabLayout.getChildCount();
                if (childCount > 0) {
                    View selectedTitle = ColorScrollingTabContainerView.this.mTabLayout.getChildAt(ColorScrollingTabContainerView.this.mIndexForSelection);
                    int selectedLeft = selectedTitle.getLeft();
                    int selectedRight = selectedTitle.getRight();
                    boolean hasNextTab = ColorScrollingTabContainerView.this.mIndexForSelection < childCount + -1;
                    if (ColorScrollingTabContainerView.this.mSelectionOffset > 0.0f && hasNextTab) {
                        View nextTitle = ColorScrollingTabContainerView.this.mTabLayout.getChildAt(ColorScrollingTabContainerView.this.mIndexForSelection + 1);
                        selectedLeft = (int) ((ColorScrollingTabContainerView.this.mSelectionOffset * ((float) nextTitle.getLeft())) + ((1.0f - ColorScrollingTabContainerView.this.mSelectionOffset) * ((float) selectedLeft)));
                        selectedRight = (int) ((ColorScrollingTabContainerView.this.mSelectionOffset * ((float) nextTitle.getRight())) + ((1.0f - ColorScrollingTabContainerView.this.mSelectionOffset) * ((float) selectedRight)));
                    }
                    canvas.translate((float) (ColorScrollingTabContainerView.this.getPaddingLeft() + selectedLeft), this.mShapeHolder.getY());
                    this.mMoveDrawable.setBounds(0, 0, selectedRight - selectedLeft, ColorScrollingTabContainerView.this.mContentHeight);
                    this.mMoveDrawable.draw(canvas);
                }
                canvas.restore();
            }
        }
    }

    private class OppoDrawableHolder {
        private float mX;
        private float mY;

        /* synthetic */ OppoDrawableHolder(ColorScrollingTabContainerView this$0, OppoDrawableHolder -this1) {
            this();
        }

        private OppoDrawableHolder() {
            this.mX = 0.0f;
            this.mY = 0.0f;
        }

        public void setX(float value) {
            this.mX = value;
        }

        public float getX() {
            return this.mX;
        }

        public void setY(float value) {
            this.mY = value;
        }

        public float getY() {
            return this.mY;
        }
    }

    private class OppoTabView extends TabView {
        public OppoTabView(Context context, Tab tab, boolean forList) {
            super(context, tab, forList);
        }

        public void update() {
            super.update();
        }

        public void setSelected(boolean selected) {
            TextView textView = this.mTextView;
            super.setSelected(selected);
        }

        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            if (ColorScrollingTabContainerView.this.mTabLayout.getChildCount() > 4) {
                setPadding(ColorScrollingTabContainerView.this.mTabViewMaxPadding, getPaddingTop(), ColorScrollingTabContainerView.this.mTabViewMaxPadding, getPaddingBottom());
            } else {
                setPadding(ColorScrollingTabContainerView.this.mTabViewPadding, getPaddingTop(), ColorScrollingTabContainerView.this.mTabViewPadding, getPaddingBottom());
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            if (this.mTextView != null) {
                this.mTextView.setTextSize(0, (float) ColorScrollingTabContainerView.this.mTabTextSize);
                this.mTextView.setGravity(17);
                this.mTextView.setEllipsize(TruncateAt.END);
            }
        }
    }

    private class TabLayoutView extends LinearLayout {
        public TabLayoutView(Context context, int defStyleAttr) {
            super(context, null, defStyleAttr);
        }

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int childCount = getChildCount();
            int measuredWidth = 0;
            for (int i = 0; i < childCount; i++) {
                int childIndex = i;
                int tabWidth = ColorScrollingTabContainerView.this.getMaxTabWidth(childCount, childIndex);
                getChildAt(childIndex).measure(MeasureSpec.makeMeasureSpec(tabWidth, 1073741824), heightMeasureSpec);
                measuredWidth += tabWidth;
            }
            -wrap6(measuredWidth, MeasureSpec.getSize(heightMeasureSpec));
        }

        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            int childLeft = getPaddingLeft();
            int childCount = getChildCount();
            int start = 0;
            int dir = 1;
            if (isLayoutRtl()) {
                start = childCount - 1;
                dir = -1;
            }
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(start + (dir * i));
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                childLeft += lp.leftMargin;
                int childWidth = child.getMeasuredWidth();
                child.layout(childLeft, t, childLeft + childWidth, b);
                childLeft = (childLeft + childWidth) + lp.leftMargin;
            }
        }
    }

    private ColorScrollingTabContainerView(Context context) {
        super(context);
        this.mAnimation = new AnimationImpl(this, context, null);
        if (OppoFontUtils.isFlipFontUsed) {
            this.mMediumTypeface = Typeface.DEFAULT;
        } else {
            this.mMediumTypeface = Typeface.DEFAULT_BOLD;
        }
    }

    public static ScrollingTabContainerView newInstance(Context context) {
        if (ColorContextUtil.isOppoStyle(context)) {
            return new ColorScrollingTabContainerView(context);
        }
        return new ScrollingTabContainerView(context);
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getParent() instanceof ColorActionBarView) {
            this.mHasEmbeddedTabs = true;
        } else if (getParent() instanceof ColorActionBarContainer) {
            this.mHasEmbeddedTabs = false;
        }
        int childCount = this.mTabLayout.getChildCount();
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        this.mScrollingTabContainerWidth = MeasureSpec.getSize(widthMeasureSpec);
        if (childCount > 4 || this.mHasEmbeddedTabs) {
            setPadding(0, getPaddingTop(), 0, getPaddingBottom());
        } else {
            setPadding(this.mTabViewMaxPadding, getPaddingTop(), this.mTabViewMaxPadding, getPaddingBottom());
            this.mScrollingTabContainerWidth -= this.mTabViewMaxPadding * 2;
        }
        if (childCount > 1 && (widthMode == 1073741824 || widthMode == Integer.MIN_VALUE)) {
            switch (childCount) {
                case 1:
                case 2:
                    this.mSplitedAveragWidth = this.mScrollingTabContainerWidth / 2;
                    break;
                case 3:
                    this.mSplitedAveragWidth = this.mScrollingTabContainerWidth / 3;
                    break;
                case 4:
                    this.mSplitedAveragWidth = this.mScrollingTabContainerWidth / 4;
                    break;
                default:
                    this.mSplitedAveragWidth = this.mMaxTabWidthSecondLevel;
                    break;
            }
        }
        this.mSplitedAveragWidth = this.mScrollingTabContainerWidth;
        computeStratchedbMaxTabWidth(childCount, getOverSizedTabCount(childCount));
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void animateToTab(int position) {
        super.animateToTab(position);
    }

    public void setTabSelected(int position) {
        this.mAnimation.setTabSelected(position);
    }

    TabView createTabView(Context context, Tab tab, boolean forAdapter) {
        TabView tabView = new OppoTabView(context, tab, forAdapter);
        if (forAdapter) {
            tabView.setBackgroundDrawable(null);
            tabView.-wrap18(new AbsListView.LayoutParams(-1, this.mContentHeight));
        } else {
            tabView.setFocusable(true);
            tabView.setOnClickListener(this.mAnimation);
        }
        return tabView;
    }

    public void addTab(Tab tab, boolean setSelected) {
        CharSequence tabText = tab.getText();
        if (!TextUtils.isEmpty(tabText)) {
            addTabTextWidth(this.mTitleWidthList.size(), tabText.toString());
        }
        super.addTab(tab, setSelected);
        this.mAnimation.addTab((TabView) this.mTabLayout.getChildAt(this.mTabLayout.getChildCount() - 1));
    }

    public void addTab(Tab tab, int position, boolean setSelected) {
        CharSequence tabText = tab.getText();
        if (!TextUtils.isEmpty(tabText)) {
            addTabTextWidth(position, tabText.toString());
        }
        super.addTab(tab, position, setSelected);
        this.mAnimation.addTab((TabView) this.mTabLayout.getChildAt(position), position);
    }

    public void removeTabAt(int position) {
        super.removeTabAt(position);
        removeTabTextWidthAt(position);
    }

    public void removeAllTabs() {
        super.removeAllTabs();
        removeAllTabTextWidth();
    }

    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        this.mAnimation.drawTab(canvas);
    }

    public void updateTabScrollPosition(int position, float positionOffset, int positionOffsetPixels) {
        this.mAnimation.updateAnimateTab(position, positionOffset, positionOffsetPixels);
        if (positionOffset != 0.0f && (this.mAnimation.isCleared() ^ 1) != 0) {
            this.mAnimation.clearSelectTabBackground();
        }
    }

    public void updateTabScrollState(int state) {
        this.mAnimation.updateScrollState(state);
    }

    public void setAllowCollapse(boolean allowCollapse) {
        super.setAllowCollapse(false);
    }

    public boolean isLayoutRtl() {
        boolean z = true;
        if (VERSION.SDK_INT <= 16) {
            return false;
        }
        if (getLayoutDirection() != 1) {
            z = false;
        }
        return z;
    }

    public void addTabTextWidth(int position, String tabText) {
        if (!TextUtils.isEmpty(tabText)) {
            this.mTextPaint.setTextSize((float) this.mDefautlTabTextSize);
            int textWidthTemp = (int) this.mTextPaint.measureText(tabText);
            this.mTitleWidthList.add(position, Integer.valueOf(textWidthTemp));
            this.mTitleWidthShrinkList.add(position, Integer.valueOf(textWidthTemp));
            this.mTitleWidthShrinkTwiceList.add(position, Integer.valueOf(textWidthTemp));
        }
    }

    public void removeTabTextWidthAt(int position) {
        this.mTitleWidthList.remove(position);
        this.mTitleWidthShrinkList.remove(position);
        this.mTitleWidthShrinkTwiceList.remove(position);
    }

    public void removeAllTabTextWidth() {
        this.mTitleWidthList.clear();
        this.mTitleWidthShrinkList.clear();
        this.mTitleWidthShrinkTwiceList.clear();
    }

    private void computeStratchedbMaxTabWidth(int tabCount, int maxTabCount) {
        if (tabCount == 2 || (!this.mHasEmbeddedTabs && tabCount == 3)) {
            maxTabCount = 0;
        }
        if (!this.mHasEmbeddedTabs) {
            switch (tabCount) {
                case 1:
                case 2:
                case 3:
                    this.mMaxTextWidth = this.mSplitedAveragWidth;
                    return;
                case 4:
                    if (maxTabCount == 1) {
                        this.mMaxTextWidth = Math.min(this.mMaxMeasuredTabTextWiddth, this.mMaxTabWidthFirstLevel);
                        this.mSplitedAveragWidth = (this.mScrollingTabContainerWidth - this.mMaxTextWidth) / (tabCount - 1);
                        this.mMaxTextWidth = Math.max(this.mScrollingTabContainerWidth - (this.mSplitedAveragWidth * (tabCount - 1)), this.mMaxTextWidth);
                        return;
                    }
                    this.mMaxTextWidth = this.mSplitedAveragWidth;
                    return;
                default:
                    this.mSplitedAveragWidth = this.mMaxTabWidthSecondLevel;
                    this.mMaxTextWidth = this.mMaxTabWidthSecondLevel;
                    return;
            }
        } else if (maxTabCount != 1 || tabCount <= 2) {
            this.mMaxTextWidth = this.mSplitedAveragWidth;
        } else {
            this.mMaxTextWidth = Math.min(this.mMaxMeasuredTabTextWiddth, this.mMaxTabWidthFirstLevel);
            this.mSplitedAveragWidth = (this.mScrollingTabContainerWidth - this.mMaxTextWidth) / (tabCount - 1);
            this.mMaxTextWidth = Math.max(this.mScrollingTabContainerWidth - (this.mSplitedAveragWidth * (tabCount - 1)), this.mMaxTextWidth);
        }
    }

    private int getMaxTabWidth(int tabCount, int tabPosition) {
        int tabWidth = ((Integer) this.mTitleWidthList.get(tabPosition)).intValue() + ((tabCount > 4 ? this.mTabViewMaxPadding : this.mTabViewPadding) * 2);
        if (tabCount > 4) {
            if (tabWidth > this.mMaxTextWidth) {
                return this.mMaxTextWidth;
            }
            if (this.mMinTextWidthMoreThanFour > tabWidth) {
                return this.mMinTextWidthMoreThanFour;
            }
            return tabWidth;
        } else if (tabWidth > this.mSplitedAveragWidth) {
            return this.mScrollingTabContainerWidth - (this.mSplitedAveragWidth * (tabCount - 1));
        } else {
            if (tabWidth > this.mMaxTextWidth) {
                return this.mMaxTextWidth;
            }
            if (tabWidth <= this.mSplitedAveragWidth) {
                return this.mSplitedAveragWidth;
            }
            return tabWidth;
        }
    }

    private int getOverSizedTabCount(int tabCount) {
        int i;
        int overlapedWidthTabCount = 0;
        int maxAvaliableWidthForText = this.mSplitedAveragWidth - ((tabCount > 4 ? this.mTabViewMaxPadding : this.mTabViewPadding) * 2);
        for (i = 0; i < tabCount; i++) {
            if (((Integer) this.mTitleWidthList.get(i)).intValue() > maxAvaliableWidthForText) {
                overlapedWidthTabCount++;
                this.mMaxMeasuredTabTextWiddth = ((tabCount > 4 ? this.mTabViewMaxPadding : this.mTabViewPadding) * 2) + ((Integer) this.mTitleWidthList.get(i)).intValue();
            }
        }
        if (overlapedWidthTabCount > 0) {
            overlapedWidthTabCount = 0;
            for (i = 0; i < tabCount; i++) {
                if (((Integer) this.mTitleWidthShrinkList.get(i)).intValue() > maxAvaliableWidthForText) {
                    overlapedWidthTabCount++;
                    this.mMaxMeasuredTabTextWiddth = ((tabCount > 4 ? this.mTabViewMaxPadding : this.mTabViewPadding) * 2) + ((Integer) this.mTitleWidthShrinkList.get(i)).intValue();
                }
            }
            if (overlapedWidthTabCount > 0) {
                overlapedWidthTabCount = 0;
                for (i = 0; i < tabCount; i++) {
                    if (((Integer) this.mTitleWidthShrinkTwiceList.get(i)).intValue() > maxAvaliableWidthForText) {
                        overlapedWidthTabCount++;
                        this.mMaxMeasuredTabTextWiddth = ((tabCount > 4 ? this.mTabViewMaxPadding : this.mTabViewPadding) * 2) + ((Integer) this.mTitleWidthShrinkTwiceList.get(i)).intValue();
                    }
                }
                this.mTabTextSize = this.mSecondSCaledTabTextSize;
            } else {
                this.mTabTextSize = this.mFirstScaledTabTextSize;
            }
        } else {
            this.mTabTextSize = this.mDefautlTabTextSize;
        }
        return overlapedWidthTabCount;
    }

    public void setTabClickable(boolean flag) {
        if (this.mTabLayout != null) {
            int count = this.mTabLayout.getChildCount();
            for (int i = 0; i < count; i++) {
                this.mTabLayout.getChildAt(i).setClickable(flag);
            }
        }
    }

    LinearLayout createTabLayout() {
        LinearLayout tabLayout = new TabLayoutView(getContext(), com.android.internal.R.attr.actionBarTabBarStyle);
        tabLayout.-wrap18(new LayoutParams(-2, -1));
        return tabLayout;
    }
}
