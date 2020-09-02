package com.color.widget;

import com.color.widget.ColorViewPager;

class ColorPagerMenuDelegate {
    private static final float MENU_SCROLL_OFFSET = 0.3f;
    private static final float MENU_SCROLL_OFFSET_HIGH = 0.7f;
    private static final float MENU_SCROLL_OFFSET_LOW = 0.3f;
    private static final int MENU_SCROLL_STATE_DOWN = 1;
    private static final int MENU_SCROLL_STATE_IDLE = 0;
    private static final int MENU_SCROLL_STATE_OUT = 3;
    private static final int MENU_SCROLL_STATE_UP = 2;
    private ColorBottomMenuCallback mCallback = null;
    private boolean mIsBeingSettled = false;
    private boolean mLastDirection = true;
    private int mLastItem = -1;
    private float mLastMenuOffset = -1.0f;
    private int mMenuScrollState = 0;
    private boolean mNextDirection = true;
    private int mNextItem = -1;
    private ColorViewPager.OnPageMenuChangeListener mOnPageMenuChangeListener = null;
    private ColorViewPager mPager = null;

    public ColorPagerMenuDelegate(ColorViewPager pager) {
        this.mPager = pager;
    }

    /* access modifiers changed from: package-private */
    public void setOnPageMenuChangeListener(ColorViewPager.OnPageMenuChangeListener listener) {
        this.mOnPageMenuChangeListener = listener;
    }

    /* access modifiers changed from: package-private */
    public void bindSplitMenuCallback(ColorBottomMenuCallback callback) {
        this.mCallback = callback;
    }

    /* access modifiers changed from: package-private */
    public void setSettleState() {
        this.mIsBeingSettled = true;
    }

    /* access modifiers changed from: package-private */
    public void onPageMenuSelected(int position) {
        this.mLastItem = this.mPager.getCurrentItem();
        this.mNextItem = position;
        if (this.mPager.getDragState() || this.mIsBeingSettled) {
            setMenuUpdateMode(2);
        }
        ColorViewPager.OnPageMenuChangeListener onPageMenuChangeListener = this.mOnPageMenuChangeListener;
        if (onPageMenuChangeListener != null) {
            onPageMenuChangeListener.onPageMenuSelected(position);
        }
    }

    /* access modifiers changed from: package-private */
    public void onPageMenuScrollStateChanged(int state) {
        if (this.mPager.getScrollState() == 0) {
            this.mIsBeingSettled = false;
            setMenuUpdateMode(1);
        }
        ColorViewPager.OnPageMenuChangeListener onPageMenuChangeListener = this.mOnPageMenuChangeListener;
        if (onPageMenuChangeListener != null) {
            onPageMenuChangeListener.onPageMenuScrollStateChanged(state);
        }
    }

    /* access modifiers changed from: package-private */
    public void pageMenuScrolled(int currentPage, float pageOffset) {
        float menuOffset = getMenuOffset(currentPage, pageOffset);
        float f = this.mLastMenuOffset;
        if (f != menuOffset) {
            if (menuOffset == 1.0f || menuOffset < f) {
                onPageMenuScrollDataChanged();
            }
            this.mLastMenuOffset = menuOffset;
        }
        onPageMenuScrolled(-1, menuOffset);
    }

    /* access modifiers changed from: package-private */
    public void updateNextItem(float deltaX) {
        ColorViewPager.ItemInfo ii = this.mPager.infoForCurrentScrollPosition();
        if (ii != null) {
            int currentPage = ii.position;
            boolean z = false;
            if (!this.mPager.isLayoutRtl() ? deltaX > 0.0f : deltaX < 0.0f) {
                z = true;
            }
            updateDirection(z);
            if (this.mNextDirection) {
                this.mLastItem = currentPage;
                this.mNextItem = Math.min(currentPage + 1, this.mPager.getAdapter().getCount() - 1);
                return;
            }
            this.mLastItem = currentPage;
            this.mNextItem = currentPage;
        }
    }

    /* access modifiers changed from: package-private */
    public void updateDirection(boolean direction) {
        this.mLastDirection = this.mNextDirection;
        this.mNextDirection = direction;
    }

    private void setMenuUpdateMode(int updateMode) {
        ColorBottomMenuCallback colorBottomMenuCallback = this.mCallback;
        if (colorBottomMenuCallback != null) {
            colorBottomMenuCallback.setMenuUpdateMode(updateMode);
        }
    }

    private float getMenuOffset(int currentPage, float pageOffset) {
        float totalOffset;
        int i = this.mNextItem;
        int i2 = this.mLastItem;
        if (i == i2) {
            totalOffset = pageOffset;
        } else {
            totalOffset = ((((float) currentPage) + pageOffset) - ((float) Math.min(i, i2))) / ((float) Math.abs(this.mNextItem - this.mLastItem));
        }
        if (totalOffset > 0.0f && totalOffset <= 0.3f) {
            return totalOffset / 0.3f;
        }
        if (totalOffset > 0.3f && totalOffset < MENU_SCROLL_OFFSET_HIGH) {
            return 1.0f;
        }
        if (totalOffset >= MENU_SCROLL_OFFSET_HIGH) {
            return (1.0f - totalOffset) / 0.3f;
        }
        return 0.0f;
    }

    private void onPageMenuScrolled(int index, float offset) {
        ColorViewPager.OnPageMenuChangeListener onPageMenuChangeListener = this.mOnPageMenuChangeListener;
        if (onPageMenuChangeListener != null) {
            onPageMenuChangeListener.onPageMenuScrolled(index, offset);
        }
    }

    private void onPageMenuScrollDataChanged() {
        ColorViewPager.OnPageMenuChangeListener onPageMenuChangeListener = this.mOnPageMenuChangeListener;
        if (onPageMenuChangeListener != null) {
            onPageMenuChangeListener.onPageMenuScrollDataChanged();
        }
    }
}
