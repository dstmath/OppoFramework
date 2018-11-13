package com.color.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.color.util.ColorChangeTextUtil;
import com.color.widget.ColorViewPager.OnPageChangeListener;
import java.util.ArrayList;
import oppo.R;
import oppo.content.res.OppoFontUtils;

public class ColorScrollingTabView extends HorizontalScrollView implements OnPageChangeListener {
    private static final int COUNT_FOUR = 4;
    private static final int COUNT_ONE = 1;
    private static final int COUNT_THREE = 3;
    private static final int COUNT_TWO = 2;
    private static final boolean DBG = false;
    private static final String TAG = "ColorScrollingTabView";
    private static final float TEXT_SIZE_SCALE = 0.88f;
    private Drawable mBackground;
    private int mDefautlTabTextSize;
    private ArrayList<String> mDrawText;
    private int mFirstScaledTabTextSize;
    int mFocusLineColor;
    int mFocusLineHeight;
    private int mMaxMeasuredTabTextWiddth;
    private int mMaxTabWidth;
    private int mMaxTabWidthFirstLevel;
    private int mMaxTabWidthSecondLevel;
    private Typeface mMediumTypeface;
    private int mMinTextWidthMoreThanFour;
    private ColorViewPager mPager;
    int mPrevSelected;
    private int mScrollingTabContainerWidth;
    private int mSecondSCaledTabTextSize;
    int mSidePadding;
    private int mSplitedAveragWidth;
    private int mTabHeight;
    private ColorTabStrip mTabStrip;
    private int mTabTextSize;
    private int mTabViewMaxPadding;
    private int mTabViewPadding;
    private Paint mTextPaint;
    private ArrayList<Integer> mTitleWidthList;
    private ArrayList<Integer> mTitleWidthShrinkList;
    private ArrayList<Integer> mTitleWidthShrinkTwiceList;

    private class ColorTabStrip extends LinearLayout {
        private int mIndexForSelection;
        private int mSelectedLeft;
        private int mSelectedRight;
        private final Paint mSelectedUnderlinePaint;
        private int mSelectedUnderlineThickness;
        private View mSelectedView;
        private float mSelectionOffset;

        public ColorTabStrip(ColorScrollingTabView this$0, Context context) {
            this(context, null);
        }

        public ColorTabStrip(Context context, AttributeSet attrs) {
            super(context, attrs);
            this.mSelectedUnderlineThickness = ColorScrollingTabView.this.mFocusLineHeight;
            int underlineColor = ColorScrollingTabView.this.mFocusLineColor;
            this.mSelectedUnderlinePaint = new Paint();
            this.mSelectedUnderlinePaint.setColor(underlineColor);
            setGravity(17);
            setWillNotDraw(ColorScrollingTabView.DBG);
        }

        void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            this.mIndexForSelection = position;
            this.mSelectionOffset = positionOffset;
            scrollTitle();
            invalidate();
        }

        private void scrollTitle() {
            if (getChildCount() > 0) {
                selectedTitle(this.mIndexForSelection);
                if (hasScrolled() && shouldAdjust(this.mSelectedLeft, this.mSelectedRight)) {
                    ColorScrollingTabView.this.scrollBy(this.mSelectedLeft - getMiddleViewLeft(this.mSelectedRight - this.mSelectedLeft), 0);
                }
            }
        }

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int childCount = getChildCount();
            int measuredWidth = 0;
            for (int i = 0; i < childCount; i++) {
                int childIndex = i;
                int tabWidth = ColorScrollingTabView.this.getMaxTabWidth(childCount, childIndex);
                getChildAt(childIndex).measure(MeasureSpec.makeMeasureSpec(tabWidth, 1073741824), heightMeasureSpec);
                measuredWidth += tabWidth;
            }
            setMeasuredDimension(measuredWidth, MeasureSpec.getSize(heightMeasureSpec));
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

        protected void onDraw(Canvas canvas) {
            if (getChildCount() > 0) {
                selectedTitle(this.mIndexForSelection);
                if (hasScrolled() && shouldAdjust(this.mSelectedLeft, this.mSelectedRight)) {
                    int drawWidth = this.mSelectedRight - this.mSelectedLeft;
                    this.mSelectedLeft = getMiddleViewLeft(drawWidth);
                    this.mSelectedRight = this.mSelectedLeft + drawWidth;
                }
                int height = getHeight();
                canvas.drawRect((float) this.mSelectedLeft, (float) (height - this.mSelectedUnderlineThickness), (float) this.mSelectedRight, (float) height, this.mSelectedUnderlinePaint);
            }
        }

        private int getMiddleViewLeft(int width) {
            return (ColorScrollingTabView.this.getScrollX() + (ColorScrollingTabView.this.getWidth() / 2)) - (width / 2);
        }

        private void selectedTitle(int index) {
            this.mSelectedView = getChildAt(index);
            this.mSelectedLeft = this.mSelectedView.getLeft();
            this.mSelectedRight = this.mSelectedView.getRight();
        }

        private boolean hasScrolled() {
            boolean hasNextTab = ColorScrollingTabView.this.isRtl() ? this.mIndexForSelection > 0 ? true : ColorScrollingTabView.DBG : this.mIndexForSelection < getChildCount() + -1 ? true : ColorScrollingTabView.DBG;
            if (this.mSelectionOffset <= 0.0f || !hasNextTab) {
                return ColorScrollingTabView.DBG;
            }
            int i;
            int i2 = this.mIndexForSelection;
            if (ColorScrollingTabView.this.isRtl()) {
                i = -1;
            } else {
                i = 1;
            }
            View nextTitle = getChildAt(i + i2);
            int nextLeft = nextTitle.getLeft();
            int nextRight = nextTitle.getRight();
            this.mSelectedLeft = (int) ((this.mSelectionOffset * ((float) nextLeft)) + ((1.0f - this.mSelectionOffset) * ((float) this.mSelectedLeft)));
            this.mSelectedRight = (int) ((this.mSelectionOffset * ((float) nextRight)) + ((1.0f - this.mSelectionOffset) * ((float) this.mSelectedRight)));
            return true;
        }

        private boolean shouldAdjust(int left, int right) {
            int drawWidth = right - left;
            int scrollX = ColorScrollingTabView.this.getScrollX();
            int parentWidth = (ColorScrollingTabView.this.getWidth() - ColorScrollingTabView.this.getPaddingLeft()) - ColorScrollingTabView.this.getPaddingRight();
            int diff = getWidth() - parentWidth;
            if (diff == 0) {
                return ColorScrollingTabView.DBG;
            }
            if ((drawWidth / 2) + left > (parentWidth / 2) + scrollX && scrollX == 0) {
                return true;
            }
            if (scrollX > 0 && scrollX < diff) {
                return true;
            }
            if (right - (drawWidth / 2) >= (parentWidth / 2) + scrollX || scrollX != diff) {
                return ColorScrollingTabView.DBG;
            }
            return true;
        }
    }

    private class ColorTabView extends TextView {
        public ColorTabView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            if (ColorScrollingTabView.this.mTabStrip.getChildCount() > 4) {
                setPadding(ColorScrollingTabView.this.mTabViewMaxPadding, 0, ColorScrollingTabView.this.mTabViewMaxPadding, 0);
            } else {
                setPadding(ColorScrollingTabView.this.mTabViewPadding, 0, ColorScrollingTabView.this.mTabViewPadding, 0);
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            setTextSize(0, (float) ColorScrollingTabView.this.mTabTextSize);
        }
    }

    public ColorScrollingTabView(Context context) {
        this(context, null);
    }

    public ColorScrollingTabView(Context context, AttributeSet attrs) {
        this(context, attrs, 201393326);
    }

    public ColorScrollingTabView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mPrevSelected = -1;
        this.mMaxTabWidth = 0;
        this.mBackground = null;
        this.mTabHeight = 0;
        this.mFocusLineColor = 0;
        this.mFocusLineHeight = 3;
        this.mMediumTypeface = null;
        this.mTitleWidthList = new ArrayList();
        this.mTitleWidthShrinkList = new ArrayList();
        this.mTitleWidthShrinkTwiceList = new ArrayList();
        this.mDrawText = new ArrayList();
        this.mSplitedAveragWidth = 0;
        this.mScrollingTabContainerWidth = 0;
        this.mDefautlTabTextSize = 0;
        this.mFirstScaledTabTextSize = 0;
        this.mSecondSCaledTabTextSize = 0;
        this.mMaxTabWidthFirstLevel = 0;
        this.mMaxTabWidthSecondLevel = 0;
        this.mMaxMeasuredTabTextWiddth = 0;
        this.mMinTextWidthMoreThanFour = 0;
        this.mTextPaint = null;
        this.mTabTextSize = 0;
        setFillViewport(true);
        setOverScrollMode(2);
        setHorizontalScrollBarEnabled(DBG);
        this.mMaxTabWidth = getResources().getDimensionPixelOffset(201655469);
        this.mSidePadding = getResources().getDimensionPixelSize(201655475);
        this.mMaxTabWidthFirstLevel = getResources().getDimensionPixelSize(201655565);
        this.mMaxTabWidthSecondLevel = getResources().getDimensionPixelSize(201655566);
        this.mMinTextWidthMoreThanFour = getResources().getDimensionPixelOffset(201655567);
        this.mTabViewPadding = getResources().getDimensionPixelSize(201654457);
        this.mTabViewMaxPadding = getResources().getDimensionPixelSize(201654461);
        this.mDefautlTabTextSize = getResources().getDimensionPixelSize(201654415);
        this.mDefautlTabTextSize = (int) ColorChangeTextUtil.getSuitableFontSize((float) this.mDefautlTabTextSize, getResources().getConfiguration().fontScale, 2);
        this.mFirstScaledTabTextSize = this.mDefautlTabTextSize;
        this.mSecondSCaledTabTextSize = this.mDefautlTabTextSize;
        this.mTextPaint = new TextPaint();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorScrollingTabView, defStyle, 0);
        int N = a.getIndexCount();
        for (int i = 0; i < N; i++) {
            int attr = a.getIndex(i);
            if (attr == 0) {
                this.mBackground = a.getDrawable(attr);
            } else if (attr == 3) {
                this.mFocusLineColor = a.getColor(attr, 0);
            } else if (attr == 1) {
                this.mTabHeight = a.getDimensionPixelSize(attr, 0);
            } else if (attr == 2) {
                this.mFocusLineHeight = a.getDimensionPixelSize(attr, 0);
            }
        }
        this.mTabStrip = new ColorTabStrip(this, context);
        addView(this.mTabStrip, new FrameLayout.LayoutParams(-2, -1));
        setBackgroundDrawable(this.mBackground);
        a.recycle();
        if (OppoFontUtils.isFlipFontUsed) {
            this.mMediumTypeface = Typeface.DEFAULT;
        } else {
            this.mMediumTypeface = Typeface.DEFAULT_BOLD;
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.mScrollingTabContainerWidth = MeasureSpec.getSize(widthMeasureSpec);
        if (this.mTabStrip.getChildCount() > 4) {
            setPadding(0, getPaddingTop(), 0, getPaddingBottom());
        } else {
            setPadding(this.mTabViewMaxPadding, getPaddingTop(), this.mTabViewMaxPadding, getPaddingBottom());
            this.mScrollingTabContainerWidth -= this.mTabViewMaxPadding * 2;
        }
        getSplitedAveragWidth(this.mTabStrip.getChildCount());
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(this.mTabHeight, 1073741824));
    }

    public void setViewPager(ColorViewPager viewPager) {
        this.mPager = viewPager;
        addTabs(this.mPager.getAdapter());
    }

    private void addTabs(ColorPagerAdapter adapter) {
        this.mTabStrip.removeAllViews();
        removeAllTabTextWidth();
        int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            addTabTextWidth(i, adapter.getPageTitle(i).toString());
            addTab(adapter.getPageTitle(i), i);
        }
    }

    private void addTab(CharSequence tabTitle, final int position) {
        TextView textView = new ColorTabView(getContext(), null, 201393327);
        textView.setText(tabTitle);
        textView.setGravity(17);
        textView.setEllipsize(TruncateAt.END);
        textView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ColorScrollingTabView.this.mPager.setCurrentItem(ColorScrollingTabView.this.getRtlPosition(position), ColorScrollingTabView.DBG);
            }
        });
        this.mTabStrip.addView(textView, new LayoutParams(-2, -1));
        if (position == 0) {
            this.mPrevSelected = 0;
            setSelected(textView, true);
        }
    }

    private void getSplitedAveragWidth(int childCount) {
        switch (childCount) {
            case 1:
                this.mSplitedAveragWidth = this.mScrollingTabContainerWidth;
                break;
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
        computeStratchedbMaxTabWidth(childCount, getOverSizedTabCount(childCount));
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

    private void computeStratchedbMaxTabWidth(int tabCount, int maxTabCount) {
        if (tabCount == 2 || tabCount == 3) {
            maxTabCount = 0;
        }
        switch (tabCount) {
            case 1:
            case 2:
            case 3:
                this.mMaxTabWidth = this.mSplitedAveragWidth;
                return;
            case 4:
                if (maxTabCount == 1) {
                    this.mMaxTabWidth = Math.min(this.mMaxMeasuredTabTextWiddth, this.mMaxTabWidthFirstLevel);
                    this.mSplitedAveragWidth = (this.mScrollingTabContainerWidth - this.mMaxTabWidth) / (tabCount - 1);
                    this.mMaxTabWidth = Math.max(this.mScrollingTabContainerWidth - (this.mSplitedAveragWidth * (tabCount - 1)), this.mMaxTabWidth);
                    return;
                }
                this.mMaxTabWidth = this.mSplitedAveragWidth;
                return;
            default:
                this.mSplitedAveragWidth = this.mMaxTabWidthSecondLevel;
                this.mMaxTabWidth = this.mMaxTabWidthSecondLevel;
                return;
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

    public void removeAllTabTextWidth() {
        this.mTitleWidthList.clear();
        this.mTitleWidthShrinkList.clear();
        this.mTitleWidthShrinkTwiceList.clear();
    }

    private int getMaxTabWidth(int tabCount, int tabPosition) {
        int tabWidth = ((Integer) this.mTitleWidthList.get(tabPosition)).intValue() + ((tabCount > 4 ? this.mTabViewMaxPadding : this.mTabViewPadding) * 2);
        if (tabCount > 4) {
            if (tabWidth > this.mMaxTabWidth) {
                return this.mMaxTabWidth;
            }
            if (this.mMinTextWidthMoreThanFour > tabWidth) {
                return this.mMinTextWidthMoreThanFour;
            }
            return tabWidth;
        } else if (tabWidth > this.mSplitedAveragWidth) {
            return this.mScrollingTabContainerWidth - (this.mSplitedAveragWidth * (tabCount - 1));
        } else {
            if (tabWidth > this.mMaxTabWidth) {
                return this.mMaxTabWidth;
            }
            if (tabWidth <= this.mSplitedAveragWidth) {
                return this.mSplitedAveragWidth;
            }
            return tabWidth;
        }
    }

    public boolean isLayoutRtl() {
        boolean z = true;
        if (VERSION.SDK_INT <= 16) {
            return DBG;
        }
        if (getLayoutDirection() != 1) {
            z = DBG;
        }
        return z;
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        position = getRtlPosition(position);
        int tabStripChildCount = this.mTabStrip.getChildCount();
        if (tabStripChildCount != 0 && position >= 0 && position < tabStripChildCount) {
            this.mTabStrip.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }
    }

    public void onPageSelected(int position) {
        position = getRtlPosition(position);
        int tabStripChildCount = this.mTabStrip.getChildCount();
        if (tabStripChildCount != 0 && position >= 0 && position < tabStripChildCount) {
            if (this.mPrevSelected >= 0 && this.mPrevSelected < tabStripChildCount) {
                setSelected((TextView) this.mTabStrip.getChildAt(this.mPrevSelected), DBG);
            }
            setSelected((TextView) this.mTabStrip.getChildAt(position), true);
            this.mPrevSelected = position;
        }
    }

    public void onPageScrollStateChanged(int state) {
    }

    private int getRtlPosition(int position) {
        if (isRtl()) {
            return (this.mTabStrip.getChildCount() - 1) - position;
        }
        return position;
    }

    private void setSelected(TextView view, boolean selected) {
        if (view != null) {
            view.setSelected(selected);
        }
    }

    private boolean isRtl() {
        return DBG;
    }
}
