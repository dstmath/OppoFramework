package com.color.widget;

import com.color.widget.ColorViewPager.OnPageMenuChangeListener;

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
    private OnPageMenuChangeListener mOnPageMenuChangeListener = null;
    private ColorViewPager mPager = null;

    public ColorPagerMenuDelegate(ColorViewPager pager) {
        this.mPager = pager;
    }

    void setOnPageMenuChangeListener(OnPageMenuChangeListener listener) {
        this.mOnPageMenuChangeListener = listener;
    }

    void bindSplitMenuCallback(ColorBottomMenuCallback callback) {
        this.mCallback = callback;
    }

    void setSettleState() {
        this.mIsBeingSettled = true;
    }

    void onPageMenuSelected(int position) {
        this.mLastItem = this.mPager.getCurrentItem();
        this.mNextItem = position;
        if (this.mPager.getDragState() || this.mIsBeingSettled) {
            setMenuUpdateMode(2);
        }
        if (this.mOnPageMenuChangeListener != null) {
            this.mOnPageMenuChangeListener.onPageMenuSelected(position);
        }
    }

    void onPageMenuScrollStateChanged(int state) {
        if (this.mPager.getScrollState() == 0) {
            this.mIsBeingSettled = false;
            setMenuUpdateMode(1);
        }
        if (this.mOnPageMenuChangeListener != null) {
            this.mOnPageMenuChangeListener.onPageMenuScrollStateChanged(state);
        }
    }

    void pageMenuScrolled(int currentPage, float pageOffset) {
        float menuOffset = getMenuOffset(currentPage, pageOffset);
        if (this.mLastMenuOffset != menuOffset) {
            if (menuOffset == 1.0f || menuOffset < this.mLastMenuOffset) {
                onPageMenuScrollDataChanged();
            }
            this.mLastMenuOffset = menuOffset;
        }
        onPageMenuScrolled(-1, menuOffset);
    }

    void updateNextItem(float deltaX) {
        boolean z = true;
        int currentPage = this.mPager.infoForCurrentScrollPosition().position;
        if (this.mPager.isLayoutRtl() ? deltaX >= 0.0f : deltaX <= 0.0f) {
            z = false;
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

    void updateDirection(boolean direction) {
        this.mLastDirection = this.mNextDirection;
        this.mNextDirection = direction;
    }

    private void setMenuUpdateMode(int updateMode) {
        if (this.mCallback != null) {
            this.mCallback.setMenuUpdateMode(updateMode);
        }
    }

    private float getMenuOffset(int currentPage, float pageOffset) {
        float totalOffset;
        if (this.mNextItem == this.mLastItem) {
            totalOffset = pageOffset;
        } else {
            totalOffset = ((((float) currentPage) + pageOffset) - ((float) Math.min(this.mNextItem, this.mLastItem))) / ((float) Math.abs(this.mNextItem - this.mLastItem));
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
        if (this.mOnPageMenuChangeListener != null) {
            this.mOnPageMenuChangeListener.onPageMenuScrolled(index, offset);
        }
    }

    private void onPageMenuScrollDataChanged() {
        if (this.mOnPageMenuChangeListener != null) {
            this.mOnPageMenuChangeListener.onPageMenuScrollDataChanged();
        }
    }
}
