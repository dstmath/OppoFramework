package android.view;

import android.annotation.UnsupportedAppUsage;
import android.graphics.Bitmap;
import android.graphics.OppoBaseRenderNode;
import android.graphics.Rect;
import android.text.ITextJustificationHooks;
import android.text.Layout;
import com.color.screenshot.ColorLongshotViewInfo;
import com.color.screenshot.ColorLongshotViewInt;
import java.util.ArrayList;

public abstract class OppoBaseView implements ColorLongshotViewInt {
    public static final int TYPE_FORCE_DARK_ALGORITHM_COLOROS = 1;
    public static final int TYPE_FORCE_DARK_ALGORITHM_GOOGLE = 2;
    protected Layout mLayout;
    protected ITextJustificationHooks mTextJustificationHooksImpl;
    protected IColorViewHooks mViewHooks;

    public abstract void addViewToScreenModeViewList(String str);

    /* access modifiers changed from: protected */
    public abstract boolean awakenScrollBars();

    /* access modifiers changed from: protected */
    public abstract boolean canScrollVertically(int i);

    /* access modifiers changed from: protected */
    public abstract int computeVerticalScrollExtent();

    /* access modifiers changed from: protected */
    public abstract int computeVerticalScrollOffset();

    /* access modifiers changed from: protected */
    public abstract int computeVerticalScrollRange();

    public abstract boolean containInScreenModeViewList(String str);

    /* access modifiers changed from: protected */
    public abstract OppoBaseRenderNode getRenderNode();

    public abstract ArrayList<String> getScreenModeViewList();

    public abstract int getScrollX();

    public abstract int getScrollY();

    @UnsupportedAppUsage
    public abstract IColorBaseViewRoot getViewRootImpl();

    /* access modifiers changed from: protected */
    public abstract int getVisibility();

    /* access modifiers changed from: protected */
    public abstract void invalidate();

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public abstract void invalidateParentCaches();

    public abstract boolean isScreenModeViewListEmpty();

    /* access modifiers changed from: protected */
    public abstract boolean isVisibleToUser();

    /* access modifiers changed from: protected */
    public abstract void onOverScrolled(int i, int i2, boolean z, boolean z2);

    /* access modifiers changed from: protected */
    public abstract void onScrollChanged(int i, int i2, int i3, int i4);

    /* access modifiers changed from: protected */
    public abstract boolean overScrollBy(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, boolean z);

    public abstract void postInvalidateOnAnimation();

    public abstract void removeViewFromScreenModeViewList(String str);

    /* access modifiers changed from: protected */
    public abstract void setValueScrollX(int i);

    /* access modifiers changed from: protected */
    public abstract void setValueScrollY(int i);

    public void setScrollXForColor(int x) {
        if (getScrollX() != x) {
            int oldX = getScrollX();
            setValueScrollX(x);
            invalidateParentCaches();
            onScrollChanged(getScrollX(), getScrollY(), oldX, getScrollY());
            if (!awakenScrollBars()) {
                postInvalidateOnAnimation();
            }
        }
    }

    public void setScrollYForColor(int y) {
        if (getScrollY() != y) {
            int oldY = getScrollY();
            setValueScrollY(y);
            invalidateParentCaches();
            onScrollChanged(getScrollX(), getScrollY(), getScrollX(), oldY);
            if (!awakenScrollBars()) {
                postInvalidateOnAnimation();
            }
        }
    }

    public boolean isOppoStyle() {
        IColorViewHooks iColorViewHooks = this.mViewHooks;
        return iColorViewHooks != null && iColorViewHooks.isOppoStyle();
    }

    public boolean isColorStyle() {
        IColorViewHooks iColorViewHooks = this.mViewHooks;
        return iColorViewHooks != null && iColorViewHooks.isColorStyle();
    }

    /* access modifiers changed from: protected */
    public void hookPerformClick() {
        IColorViewHooks iColorViewHooks = this.mViewHooks;
        if (iColorViewHooks != null) {
            iColorViewHooks.performClick();
        }
    }

    @Override // com.color.screenshot.ColorLongshotViewBase
    public int computeLongScrollRange() {
        return computeVerticalScrollRange();
    }

    @Override // com.color.screenshot.ColorLongshotViewBase
    public int computeLongScrollOffset() {
        return computeVerticalScrollOffset();
    }

    @Override // com.color.screenshot.ColorLongshotViewBase
    public int computeLongScrollExtent() {
        return computeVerticalScrollExtent();
    }

    @Override // com.color.screenshot.ColorLongshotViewBase
    public boolean canLongScroll() {
        return canScrollVertically(1);
    }

    @Override // com.color.screenshot.ColorLongshotViewBase
    public boolean isLongshotVisibleToUser() {
        if (getVisibility() != 0) {
            return false;
        }
        return isVisibleToUser();
    }

    @Override // com.color.screenshot.ColorLongshotViewBase
    public boolean findViewsLongshotInfo(ColorLongshotViewInfo info) {
        return this.mViewHooks.findViewsLongshotInfo(info);
    }

    @Override // com.color.screenshot.ColorLongshotViewInt
    public void onLongshotOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        onOverScrolled(scrollX, scrollY, clampedX, clampedY);
    }

    /* access modifiers changed from: protected */
    public boolean hookOverScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        int maxOverScrollY2;
        boolean isLongshotConnected = this.mViewHooks.isLongshotConnected();
        if (isLongshotConnected) {
            maxOverScrollY2 = 0;
        } else {
            maxOverScrollY2 = maxOverScrollY;
        }
        boolean result = overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY2, isTouchEvent);
        if (isLongshotConnected) {
            return this.mViewHooks.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY2, isTouchEvent, getScrollY(), result);
        }
        return result;
    }

    public void setUsageForceDarkAlgorithmType(int type) {
        OppoBaseRenderNode mRenderNode = getRenderNode();
        if (mRenderNode != null) {
            mRenderNode.setUsageForceDarkAlgorithmType(type);
            invalidate();
        }
    }

    public void setUsageHint(int usageHint) {
        OppoBaseRenderNode mRenderNode = getRenderNode();
        if (mRenderNode != null) {
            mRenderNode.setUsageHint(usageHint);
        }
    }

    public void setParaSpacing(float add) {
        ITextJustificationHooks iTextJustificationHooks = this.mTextJustificationHooksImpl;
        if (iTextJustificationHooks != null) {
            iTextJustificationHooks.setTextViewParaSpacing(this, add, this.mLayout);
        }
    }

    public float getParaSpacing() {
        ITextJustificationHooks iTextJustificationHooks = this.mTextJustificationHooksImpl;
        if (iTextJustificationHooks != null) {
            return iTextJustificationHooks.getTextViewParaSpacing(this);
        }
        return 0.0f;
    }

    public Bitmap getColorCustomDrawingCache(Rect clip, int viewTop) {
        return null;
    }

    public void updateColorNavigationGuardColor(int color) {
    }
}
