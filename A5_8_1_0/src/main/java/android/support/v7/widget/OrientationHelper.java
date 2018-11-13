package android.support.v7.widget;

import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.LayoutParams;
import android.view.View;

public abstract class OrientationHelper {
    public static final int HORIZONTAL = 0;
    private static final int INVALID_SIZE = Integer.MIN_VALUE;
    public static final int VERTICAL = 1;
    private int mLastTotalSpace;
    protected final LayoutManager mLayoutManager;

    /* synthetic */ OrientationHelper(LayoutManager layoutManager, OrientationHelper -this1) {
        this(layoutManager);
    }

    public abstract int getDecoratedEnd(View view);

    public abstract int getDecoratedMeasurement(View view);

    public abstract int getDecoratedMeasurementInOther(View view);

    public abstract int getDecoratedStart(View view);

    public abstract int getEnd();

    public abstract int getEndAfterPadding();

    public abstract int getEndPadding();

    public abstract int getStartAfterPadding();

    public abstract int getTotalSpace();

    public abstract void offsetChild(View view, int i);

    public abstract void offsetChildren(int i);

    private OrientationHelper(LayoutManager layoutManager) {
        this.mLastTotalSpace = Integer.MIN_VALUE;
        this.mLayoutManager = layoutManager;
    }

    public void onLayoutComplete() {
        this.mLastTotalSpace = getTotalSpace();
    }

    public int getTotalSpaceChange() {
        return Integer.MIN_VALUE == this.mLastTotalSpace ? 0 : getTotalSpace() - this.mLastTotalSpace;
    }

    public static OrientationHelper createOrientationHelper(LayoutManager layoutManager, int orientation) {
        switch (orientation) {
            case 0:
                return createHorizontalHelper(layoutManager);
            case 1:
                return createVerticalHelper(layoutManager);
            default:
                throw new IllegalArgumentException("invalid orientation");
        }
    }

    public static OrientationHelper createHorizontalHelper(LayoutManager layoutManager) {
        return new OrientationHelper(layoutManager) {
            public int getEndAfterPadding() {
                return this.mLayoutManager.getWidth() - this.mLayoutManager.getPaddingRight();
            }

            public int getEnd() {
                return this.mLayoutManager.getWidth();
            }

            public void offsetChildren(int amount) {
                this.mLayoutManager.offsetChildrenHorizontal(amount);
            }

            public int getStartAfterPadding() {
                return this.mLayoutManager.getPaddingLeft();
            }

            public int getDecoratedMeasurement(View view) {
                LayoutParams params = (LayoutParams) view.getLayoutParams();
                return (this.mLayoutManager.getDecoratedMeasuredWidth(view) + params.leftMargin) + params.rightMargin;
            }

            public int getDecoratedMeasurementInOther(View view) {
                LayoutParams params = (LayoutParams) view.getLayoutParams();
                return (this.mLayoutManager.getDecoratedMeasuredHeight(view) + params.topMargin) + params.bottomMargin;
            }

            public int getDecoratedEnd(View view) {
                return this.mLayoutManager.getDecoratedRight(view) + ((LayoutParams) view.getLayoutParams()).rightMargin;
            }

            public int getDecoratedStart(View view) {
                return this.mLayoutManager.getDecoratedLeft(view) - ((LayoutParams) view.getLayoutParams()).leftMargin;
            }

            public int getTotalSpace() {
                return (this.mLayoutManager.getWidth() - this.mLayoutManager.getPaddingLeft()) - this.mLayoutManager.getPaddingRight();
            }

            public void offsetChild(View view, int offset) {
                view.offsetLeftAndRight(offset);
            }

            public int getEndPadding() {
                return this.mLayoutManager.getPaddingRight();
            }
        };
    }

    public static OrientationHelper createVerticalHelper(LayoutManager layoutManager) {
        return new OrientationHelper(layoutManager) {
            public int getEndAfterPadding() {
                return this.mLayoutManager.getHeight() - this.mLayoutManager.getPaddingBottom();
            }

            public int getEnd() {
                return this.mLayoutManager.getHeight();
            }

            public void offsetChildren(int amount) {
                this.mLayoutManager.offsetChildrenVertical(amount);
            }

            public int getStartAfterPadding() {
                return this.mLayoutManager.getPaddingTop();
            }

            public int getDecoratedMeasurement(View view) {
                LayoutParams params = (LayoutParams) view.getLayoutParams();
                return (this.mLayoutManager.getDecoratedMeasuredHeight(view) + params.topMargin) + params.bottomMargin;
            }

            public int getDecoratedMeasurementInOther(View view) {
                LayoutParams params = (LayoutParams) view.getLayoutParams();
                return (this.mLayoutManager.getDecoratedMeasuredWidth(view) + params.leftMargin) + params.rightMargin;
            }

            public int getDecoratedEnd(View view) {
                return this.mLayoutManager.getDecoratedBottom(view) + ((LayoutParams) view.getLayoutParams()).bottomMargin;
            }

            public int getDecoratedStart(View view) {
                return this.mLayoutManager.getDecoratedTop(view) - ((LayoutParams) view.getLayoutParams()).topMargin;
            }

            public int getTotalSpace() {
                return (this.mLayoutManager.getHeight() - this.mLayoutManager.getPaddingTop()) - this.mLayoutManager.getPaddingBottom();
            }

            public void offsetChild(View view, int offset) {
                view.offsetTopAndBottom(offset);
            }

            public int getEndPadding() {
                return this.mLayoutManager.getPaddingBottom();
            }
        };
    }
}
