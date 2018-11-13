package android.support.v7.widget;

import android.content.Context;
import android.database.Observable;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityEventCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.CollectionInfoCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.CollectionItemInfoCompat;
import android.support.v4.view.accessibility.AccessibilityRecordCompat;
import android.support.v4.widget.EdgeEffectCompat;
import android.support.v4.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.FocusFinder;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Interpolator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class RecyclerView extends ViewGroup {
    private static final boolean DEBUG = false;
    private static final boolean DISPATCH_TEMP_DETACH = false;
    private static final boolean FORCE_INVALIDATE_DISPLAY_LIST = false;
    public static final int HORIZONTAL = 0;
    private static final int INVALID_POINTER = -1;
    public static final int INVALID_TYPE = -1;
    private static final int MAX_SCROLL_DURATION = 2000;
    public static final long NO_ID = -1;
    public static final int NO_POSITION = -1;
    public static final int SCROLL_STATE_DRAGGING = 1;
    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_SETTLING = 2;
    private static final String TAG = "RecyclerView";
    public static final int VERTICAL = 1;
    private static final Interpolator sQuinticInterpolator = null;
    private RecyclerViewAccessibilityDelegate mAccessibilityDelegate;
    private final AccessibilityManager mAccessibilityManager;
    private OnItemTouchListener mActiveOnItemTouchListener;
    private Adapter mAdapter;
    AdapterHelper mAdapterHelper;
    private boolean mAdapterUpdateDuringMeasure;
    private EdgeEffectCompat mBottomGlow;
    ChildHelper mChildHelper;
    private boolean mClipToPadding;
    private boolean mDataSetHasChangedAfterLayout;
    final List<View> mDisappearingViewsInLayoutPass;
    private boolean mEatRequestLayout;
    private boolean mFirstLayoutComplete;
    private boolean mHasFixedSize;
    private int mInitialTouchX;
    private int mInitialTouchY;
    private boolean mIsAttached;
    ItemAnimator mItemAnimator;
    private ItemAnimatorListener mItemAnimatorListener;
    private Runnable mItemAnimatorRunner;
    private final ArrayList<ItemDecoration> mItemDecorations;
    boolean mItemsAddedOrRemoved;
    boolean mItemsChanged;
    private int mLastTouchX;
    private int mLastTouchY;
    private LayoutManager mLayout;
    private boolean mLayoutRequestEaten;
    private EdgeEffectCompat mLeftGlow;
    private final int mMaxFlingVelocity;
    private final int mMinFlingVelocity;
    private final RecyclerViewDataObserver mObserver;
    private final ArrayList<OnItemTouchListener> mOnItemTouchListeners;
    private SavedState mPendingSavedState;
    private final boolean mPostUpdatesOnAnimation;
    private boolean mPostedAnimatorRunner;
    final Recycler mRecycler;
    private RecyclerListener mRecyclerListener;
    private EdgeEffectCompat mRightGlow;
    private boolean mRunningLayoutOrScroll;
    private OnScrollListener mScrollListener;
    private int mScrollPointerId;
    private int mScrollState;
    final State mState;
    private final Rect mTempRect;
    private EdgeEffectCompat mTopGlow;
    private final int mTouchSlop;
    private final Runnable mUpdateChildViewsRunnable;
    private VelocityTracker mVelocityTracker;
    private final ViewFlinger mViewFlinger;

    public static abstract class ItemAnimator {
        private long mAddDuration = 120;
        private long mChangeDuration = 250;
        private ArrayList<ItemAnimatorFinishedListener> mFinishedListeners = new ArrayList();
        private ItemAnimatorListener mListener = null;
        private long mMoveDuration = 250;
        private long mRemoveDuration = 120;
        private boolean mSupportsChangeAnimations = false;

        public interface ItemAnimatorFinishedListener {
            void onAnimationsFinished();
        }

        interface ItemAnimatorListener {
            void onAddFinished(ViewHolder viewHolder);

            void onChangeFinished(ViewHolder viewHolder);

            void onMoveFinished(ViewHolder viewHolder);

            void onRemoveFinished(ViewHolder viewHolder);
        }

        public abstract boolean animateAdd(ViewHolder viewHolder);

        public abstract boolean animateChange(ViewHolder viewHolder, ViewHolder viewHolder2, int i, int i2, int i3, int i4);

        public abstract boolean animateMove(ViewHolder viewHolder, int i, int i2, int i3, int i4);

        public abstract boolean animateRemove(ViewHolder viewHolder);

        public abstract void endAnimation(ViewHolder viewHolder);

        public abstract void endAnimations();

        public abstract boolean isRunning();

        public abstract void runPendingAnimations();

        public long getMoveDuration() {
            return this.mMoveDuration;
        }

        public void setMoveDuration(long moveDuration) {
            this.mMoveDuration = moveDuration;
        }

        public long getAddDuration() {
            return this.mAddDuration;
        }

        public void setAddDuration(long addDuration) {
            this.mAddDuration = addDuration;
        }

        public long getRemoveDuration() {
            return this.mRemoveDuration;
        }

        public void setRemoveDuration(long removeDuration) {
            this.mRemoveDuration = removeDuration;
        }

        public long getChangeDuration() {
            return this.mChangeDuration;
        }

        public void setChangeDuration(long changeDuration) {
            this.mChangeDuration = changeDuration;
        }

        public boolean getSupportsChangeAnimations() {
            return this.mSupportsChangeAnimations;
        }

        public void setSupportsChangeAnimations(boolean supportsChangeAnimations) {
            this.mSupportsChangeAnimations = supportsChangeAnimations;
        }

        void setListener(ItemAnimatorListener listener) {
            this.mListener = listener;
        }

        public final void dispatchRemoveFinished(ViewHolder item) {
            onRemoveFinished(item);
            if (this.mListener != null) {
                this.mListener.onRemoveFinished(item);
            }
        }

        public final void dispatchMoveFinished(ViewHolder item) {
            onMoveFinished(item);
            if (this.mListener != null) {
                this.mListener.onMoveFinished(item);
            }
        }

        public final void dispatchAddFinished(ViewHolder item) {
            onAddFinished(item);
            if (this.mListener != null) {
                this.mListener.onAddFinished(item);
            }
        }

        public final void dispatchChangeFinished(ViewHolder item, boolean oldItem) {
            onChangeFinished(item, oldItem);
            if (this.mListener != null) {
                this.mListener.onChangeFinished(item);
            }
        }

        public final void dispatchRemoveStarting(ViewHolder item) {
            onRemoveStarting(item);
        }

        public final void dispatchMoveStarting(ViewHolder item) {
            onMoveStarting(item);
        }

        public final void dispatchAddStarting(ViewHolder item) {
            onAddStarting(item);
        }

        public final void dispatchChangeStarting(ViewHolder item, boolean oldItem) {
            onChangeStarting(item, oldItem);
        }

        public final boolean isRunning(ItemAnimatorFinishedListener listener) {
            boolean running = isRunning();
            if (listener != null) {
                if (running) {
                    this.mFinishedListeners.add(listener);
                } else {
                    listener.onAnimationsFinished();
                }
            }
            return running;
        }

        public final void dispatchAnimationsFinished() {
            int count = this.mFinishedListeners.size();
            for (int i = 0; i < count; i++) {
                ((ItemAnimatorFinishedListener) this.mFinishedListeners.get(i)).onAnimationsFinished();
            }
            this.mFinishedListeners.clear();
        }

        public void onRemoveStarting(ViewHolder item) {
        }

        public void onRemoveFinished(ViewHolder item) {
        }

        public void onAddStarting(ViewHolder item) {
        }

        public void onAddFinished(ViewHolder item) {
        }

        public void onMoveStarting(ViewHolder item) {
        }

        public void onMoveFinished(ViewHolder item) {
        }

        public void onChangeStarting(ViewHolder item, boolean oldItem) {
        }

        public void onChangeFinished(ViewHolder item, boolean oldItem) {
        }
    }

    public static class LayoutParams extends MarginLayoutParams {
        final Rect mDecorInsets = new Rect();
        boolean mInsetsDirty = true;
        boolean mPendingInvalidate = false;
        ViewHolder mViewHolder;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(LayoutParams source) {
            super(source);
        }

        public boolean viewNeedsUpdate() {
            return this.mViewHolder.needsUpdate();
        }

        public boolean isViewInvalid() {
            return this.mViewHolder.isInvalid();
        }

        public boolean isItemRemoved() {
            return this.mViewHolder.isRemoved();
        }

        public boolean isItemChanged() {
            return this.mViewHolder.isChanged();
        }

        public int getViewPosition() {
            return this.mViewHolder.getPosition();
        }
    }

    public static abstract class LayoutManager {
        ChildHelper mChildHelper;
        RecyclerView mRecyclerView;
        private boolean mRequestedSimpleAnimations = false;
        @Nullable
        SmoothScroller mSmoothScroller;

        public abstract LayoutParams generateDefaultLayoutParams();

        void setRecyclerView(RecyclerView recyclerView) {
            if (recyclerView == null) {
                this.mRecyclerView = null;
                this.mChildHelper = null;
                return;
            }
            this.mRecyclerView = recyclerView;
            this.mChildHelper = recyclerView.mChildHelper;
        }

        public void requestLayout() {
            if (this.mRecyclerView != null) {
                this.mRecyclerView.requestLayout();
            }
        }

        public void assertInLayoutOrScroll(String message) {
            if (this.mRecyclerView != null) {
                this.mRecyclerView.assertInLayoutOrScroll(message);
            }
        }

        public void assertNotInLayoutOrScroll(String message) {
            if (this.mRecyclerView != null) {
                this.mRecyclerView.assertNotInLayoutOrScroll(message);
            }
        }

        public boolean supportsPredictiveItemAnimations() {
            return false;
        }

        public void onAttachedToWindow(RecyclerView view) {
        }

        @Deprecated
        public void onDetachedFromWindow(RecyclerView view) {
        }

        public void onDetachedFromWindow(RecyclerView view, Recycler recycler) {
            onDetachedFromWindow(view);
        }

        public boolean getClipToPadding() {
            return this.mRecyclerView != null ? this.mRecyclerView.mClipToPadding : false;
        }

        public void onLayoutChildren(Recycler recycler, State state) {
            Log.e(RecyclerView.TAG, "You must override onLayoutChildren(Recycler recycler, State state) ");
        }

        public boolean checkLayoutParams(LayoutParams lp) {
            return lp != null;
        }

        public LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams lp) {
            if (lp instanceof LayoutParams) {
                return new LayoutParams((LayoutParams) lp);
            }
            if (lp instanceof MarginLayoutParams) {
                return new LayoutParams((MarginLayoutParams) lp);
            }
            return new LayoutParams(lp);
        }

        public LayoutParams generateLayoutParams(Context c, AttributeSet attrs) {
            return new LayoutParams(c, attrs);
        }

        public int scrollHorizontallyBy(int dx, Recycler recycler, State state) {
            return 0;
        }

        public int scrollVerticallyBy(int dy, Recycler recycler, State state) {
            return 0;
        }

        public boolean canScrollHorizontally() {
            return false;
        }

        public boolean canScrollVertically() {
            return false;
        }

        public void scrollToPosition(int position) {
        }

        public void smoothScrollToPosition(RecyclerView recyclerView, State state, int position) {
            Log.e(RecyclerView.TAG, "You must override smoothScrollToPosition to support smooth scrolling");
        }

        public void startSmoothScroll(SmoothScroller smoothScroller) {
            if (!(this.mSmoothScroller == null || smoothScroller == this.mSmoothScroller || !this.mSmoothScroller.isRunning())) {
                this.mSmoothScroller.stop();
            }
            this.mSmoothScroller = smoothScroller;
            this.mSmoothScroller.start(this.mRecyclerView, this);
        }

        public boolean isSmoothScrolling() {
            return this.mSmoothScroller != null ? this.mSmoothScroller.isRunning() : false;
        }

        public int getLayoutDirection() {
            return ViewCompat.getLayoutDirection(this.mRecyclerView);
        }

        public void endAnimation(View view) {
            if (this.mRecyclerView.mItemAnimator != null) {
                this.mRecyclerView.mItemAnimator.endAnimation(RecyclerView.getChildViewHolderInt(view));
            }
        }

        public void addDisappearingView(View child) {
            addDisappearingView(child, -1);
        }

        public void addDisappearingView(View child, int index) {
            addViewInt(child, index, true);
        }

        public void addView(View child) {
            addView(child, -1);
        }

        public void addView(View child, int index) {
            addViewInt(child, index, false);
        }

        private void addViewInt(View child, int index, boolean disappearing) {
            ViewHolder holder = RecyclerView.getChildViewHolderInt(child);
            if (disappearing || holder.isRemoved()) {
                this.mRecyclerView.addToDisappearingList(child);
            } else {
                this.mRecyclerView.removeFromDisappearingList(child);
            }
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (holder.wasReturnedFromScrap() || holder.isScrap()) {
                if (holder.isScrap()) {
                    holder.unScrap();
                } else {
                    holder.clearReturnedFromScrapFlag();
                }
                this.mChildHelper.attachViewToParent(child, index, child.getLayoutParams(), false);
            } else if (child.getParent() == this.mRecyclerView) {
                int currentIndex = this.mChildHelper.indexOfChild(child);
                if (index == -1) {
                    index = this.mChildHelper.getChildCount();
                }
                if (currentIndex == -1) {
                    throw new IllegalStateException("Added View has RecyclerView as parent but view is not a real child. Unfiltered index:" + this.mRecyclerView.indexOfChild(child));
                } else if (currentIndex != index) {
                    this.mRecyclerView.mLayout.moveView(currentIndex, index);
                }
            } else {
                this.mChildHelper.addView(child, index, false);
                lp.mInsetsDirty = true;
                if (this.mSmoothScroller != null && this.mSmoothScroller.isRunning()) {
                    this.mSmoothScroller.onChildAttachedToWindow(child);
                }
            }
            if (lp.mPendingInvalidate) {
                holder.itemView.invalidate();
                lp.mPendingInvalidate = false;
            }
        }

        public void removeView(View child) {
            this.mChildHelper.removeView(child);
        }

        public void removeViewAt(int index) {
            if (getChildAt(index) != null) {
                this.mChildHelper.removeViewAt(index);
            }
        }

        public void removeAllViews() {
            for (int i = getChildCount() - 1; i >= 0; i--) {
                View child = getChildAt(i);
                this.mChildHelper.removeViewAt(i);
            }
        }

        public int getPosition(View view) {
            return ((LayoutParams) view.getLayoutParams()).getViewPosition();
        }

        public int getItemViewType(View view) {
            return RecyclerView.getChildViewHolderInt(view).getItemViewType();
        }

        public View findViewByPosition(int position) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                ViewHolder vh = RecyclerView.getChildViewHolderInt(child);
                if (vh != null && vh.getPosition() == position && !vh.shouldIgnore() && (this.mRecyclerView.mState.isPreLayout() || !vh.isRemoved())) {
                    return child;
                }
            }
            return null;
        }

        public void detachView(View child) {
            int ind = this.mChildHelper.indexOfChild(child);
            if (ind >= 0) {
                detachViewInternal(ind, child);
            }
        }

        public void detachViewAt(int index) {
            detachViewInternal(index, getChildAt(index));
        }

        private void detachViewInternal(int index, View view) {
            this.mChildHelper.detachViewFromParent(index);
        }

        public void attachView(View child, int index, LayoutParams lp) {
            ViewHolder vh = RecyclerView.getChildViewHolderInt(child);
            if (vh.isRemoved()) {
                this.mRecyclerView.addToDisappearingList(child);
            } else {
                this.mRecyclerView.removeFromDisappearingList(child);
            }
            this.mChildHelper.attachViewToParent(child, index, lp, vh.isRemoved());
        }

        public void attachView(View child, int index) {
            attachView(child, index, (LayoutParams) child.getLayoutParams());
        }

        public void attachView(View child) {
            attachView(child, -1);
        }

        public void removeDetachedView(View child) {
            this.mRecyclerView.removeDetachedView(child, false);
        }

        public void moveView(int fromIndex, int toIndex) {
            View view = getChildAt(fromIndex);
            if (view == null) {
                throw new IllegalArgumentException("Cannot move a child from non-existing index:" + fromIndex);
            }
            detachViewAt(fromIndex);
            attachView(view, toIndex);
        }

        public void detachAndScrapView(View child, Recycler recycler) {
            scrapOrRecycleView(recycler, this.mChildHelper.indexOfChild(child), child);
        }

        public void detachAndScrapViewAt(int index, Recycler recycler) {
            scrapOrRecycleView(recycler, index, getChildAt(index));
        }

        public void removeAndRecycleView(View child, Recycler recycler) {
            removeView(child);
            recycler.recycleView(child);
        }

        public void removeAndRecycleViewAt(int index, Recycler recycler) {
            View view = getChildAt(index);
            removeViewAt(index);
            recycler.recycleView(view);
        }

        public int getChildCount() {
            return this.mChildHelper != null ? this.mChildHelper.getChildCount() : 0;
        }

        public View getChildAt(int index) {
            return this.mChildHelper != null ? this.mChildHelper.getChildAt(index) : null;
        }

        public int getWidth() {
            return this.mRecyclerView != null ? this.mRecyclerView.getWidth() : 0;
        }

        public int getHeight() {
            return this.mRecyclerView != null ? this.mRecyclerView.getHeight() : 0;
        }

        public int getPaddingLeft() {
            return this.mRecyclerView != null ? this.mRecyclerView.getPaddingLeft() : 0;
        }

        public int getPaddingTop() {
            return this.mRecyclerView != null ? this.mRecyclerView.getPaddingTop() : 0;
        }

        public int getPaddingRight() {
            return this.mRecyclerView != null ? this.mRecyclerView.getPaddingRight() : 0;
        }

        public int getPaddingBottom() {
            return this.mRecyclerView != null ? this.mRecyclerView.getPaddingBottom() : 0;
        }

        public int getPaddingStart() {
            return this.mRecyclerView != null ? ViewCompat.getPaddingStart(this.mRecyclerView) : 0;
        }

        public int getPaddingEnd() {
            return this.mRecyclerView != null ? ViewCompat.getPaddingEnd(this.mRecyclerView) : 0;
        }

        public boolean isFocused() {
            return this.mRecyclerView != null ? this.mRecyclerView.isFocused() : false;
        }

        public boolean hasFocus() {
            return this.mRecyclerView != null ? this.mRecyclerView.hasFocus() : false;
        }

        public View getFocusedChild() {
            if (this.mRecyclerView == null) {
                return null;
            }
            View focused = this.mRecyclerView.getFocusedChild();
            if (focused == null || this.mChildHelper.isHidden(focused)) {
                return null;
            }
            return focused;
        }

        public int getItemCount() {
            Adapter a = null;
            if (this.mRecyclerView != null) {
                a = this.mRecyclerView.getAdapter();
            }
            return a != null ? a.getItemCount() : 0;
        }

        public void offsetChildrenHorizontal(int dx) {
            if (this.mRecyclerView != null) {
                this.mRecyclerView.offsetChildrenHorizontal(dx);
            }
        }

        public void offsetChildrenVertical(int dy) {
            if (this.mRecyclerView != null) {
                this.mRecyclerView.offsetChildrenVertical(dy);
            }
        }

        public void ignoreView(View view) {
            if (view.getParent() != this.mRecyclerView || this.mRecyclerView.indexOfChild(view) == -1) {
                throw new IllegalArgumentException("View should be fully attached to be ignored");
            }
            ViewHolder vh = RecyclerView.getChildViewHolderInt(view);
            vh.addFlags(128);
            this.mRecyclerView.mState.onViewIgnored(vh);
        }

        public void stopIgnoringView(View view) {
            ViewHolder vh = RecyclerView.getChildViewHolderInt(view);
            vh.stopIgnoring();
            vh.resetInternal();
            vh.addFlags(4);
        }

        public void detachAndScrapAttachedViews(Recycler recycler) {
            for (int i = getChildCount() - 1; i >= 0; i--) {
                scrapOrRecycleView(recycler, i, getChildAt(i));
            }
        }

        private void scrapOrRecycleView(Recycler recycler, int index, View view) {
            ViewHolder viewHolder = RecyclerView.getChildViewHolderInt(view);
            if (!viewHolder.shouldIgnore()) {
                if (!viewHolder.isInvalid() || viewHolder.isRemoved() || viewHolder.isChanged() || this.mRecyclerView.mAdapter.hasStableIds()) {
                    detachViewAt(index);
                    recycler.scrapView(view);
                } else {
                    removeViewAt(index);
                    recycler.recycleViewHolderInternal(viewHolder);
                }
            }
        }

        void removeAndRecycleScrapInt(Recycler recycler, boolean remove) {
            int scrapCount = recycler.getScrapCount();
            for (int i = 0; i < scrapCount; i++) {
                View scrap = recycler.getScrapViewAt(i);
                if (!RecyclerView.getChildViewHolderInt(scrap).shouldIgnore()) {
                    if (remove) {
                        this.mRecyclerView.removeDetachedView(scrap, false);
                    }
                    recycler.quickRecycleScrapView(scrap);
                }
            }
            recycler.clearScrap();
            if (remove && scrapCount > 0) {
                this.mRecyclerView.invalidate();
            }
        }

        public void measureChild(View child, int widthUsed, int heightUsed) {
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            Rect insets = this.mRecyclerView.getItemDecorInsetsForChild(child);
            child.measure(getChildMeasureSpec(getWidth(), (getPaddingLeft() + getPaddingRight()) + (widthUsed + (insets.left + insets.right)), lp.width, canScrollHorizontally()), getChildMeasureSpec(getHeight(), (getPaddingTop() + getPaddingBottom()) + (heightUsed + (insets.top + insets.bottom)), lp.height, canScrollVertically()));
        }

        public void measureChildWithMargins(View child, int widthUsed, int heightUsed) {
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            Rect insets = this.mRecyclerView.getItemDecorInsetsForChild(child);
            child.measure(getChildMeasureSpec(getWidth(), (((getPaddingLeft() + getPaddingRight()) + lp.leftMargin) + lp.rightMargin) + (widthUsed + (insets.left + insets.right)), lp.width, canScrollHorizontally()), getChildMeasureSpec(getHeight(), (((getPaddingTop() + getPaddingBottom()) + lp.topMargin) + lp.bottomMargin) + (heightUsed + (insets.top + insets.bottom)), lp.height, canScrollVertically()));
        }

        public static int getChildMeasureSpec(int parentSize, int padding, int childDimension, boolean canScroll) {
            int size = Math.max(0, parentSize - padding);
            int resultSize = 0;
            int resultMode = 0;
            if (canScroll) {
                if (childDimension >= 0) {
                    resultSize = childDimension;
                    resultMode = 1073741824;
                } else {
                    resultSize = 0;
                    resultMode = 0;
                }
            } else if (childDimension >= 0) {
                resultSize = childDimension;
                resultMode = 1073741824;
            } else if (childDimension == -1) {
                resultSize = size;
                resultMode = 1073741824;
            } else if (childDimension == -2) {
                resultSize = size;
                resultMode = Integer.MIN_VALUE;
            }
            return MeasureSpec.makeMeasureSpec(resultSize, resultMode);
        }

        public int getDecoratedMeasuredWidth(View child) {
            Rect insets = ((LayoutParams) child.getLayoutParams()).mDecorInsets;
            return (child.getMeasuredWidth() + insets.left) + insets.right;
        }

        public int getDecoratedMeasuredHeight(View child) {
            Rect insets = ((LayoutParams) child.getLayoutParams()).mDecorInsets;
            return (child.getMeasuredHeight() + insets.top) + insets.bottom;
        }

        public void layoutDecorated(View child, int left, int top, int right, int bottom) {
            Rect insets = ((LayoutParams) child.getLayoutParams()).mDecorInsets;
            child.layout(insets.left + left, insets.top + top, right - insets.right, bottom - insets.bottom);
        }

        public int getDecoratedLeft(View child) {
            return child.getLeft() - getLeftDecorationWidth(child);
        }

        public int getDecoratedTop(View child) {
            return child.getTop() - getTopDecorationHeight(child);
        }

        public int getDecoratedRight(View child) {
            return child.getRight() + getRightDecorationWidth(child);
        }

        public int getDecoratedBottom(View child) {
            return child.getBottom() + getBottomDecorationHeight(child);
        }

        public void calculateItemDecorationsForChild(View child, Rect outRect) {
            if (this.mRecyclerView == null) {
                outRect.set(0, 0, 0, 0);
            } else {
                outRect.set(this.mRecyclerView.getItemDecorInsetsForChild(child));
            }
        }

        public int getTopDecorationHeight(View child) {
            return ((LayoutParams) child.getLayoutParams()).mDecorInsets.top;
        }

        public int getBottomDecorationHeight(View child) {
            return ((LayoutParams) child.getLayoutParams()).mDecorInsets.bottom;
        }

        public int getLeftDecorationWidth(View child) {
            return ((LayoutParams) child.getLayoutParams()).mDecorInsets.left;
        }

        public int getRightDecorationWidth(View child) {
            return ((LayoutParams) child.getLayoutParams()).mDecorInsets.right;
        }

        public View onFocusSearchFailed(View focused, int direction, Recycler recycler, State state) {
            return null;
        }

        public View onInterceptFocusSearch(View focused, int direction) {
            return null;
        }

        public boolean requestChildRectangleOnScreen(RecyclerView parent, View child, Rect rect, boolean immediate) {
            int parentLeft = getPaddingLeft();
            int parentTop = getPaddingTop();
            int parentRight = getWidth() - getPaddingRight();
            int parentBottom = getHeight() - getPaddingBottom();
            int childLeft = child.getLeft() + rect.left;
            int childTop = child.getTop() + rect.top;
            int childRight = childLeft + rect.right;
            int childBottom = childTop + rect.bottom;
            int offScreenLeft = Math.min(0, childLeft - parentLeft);
            int offScreenTop = Math.min(0, childTop - parentTop);
            int offScreenRight = Math.max(0, childRight - parentRight);
            int offScreenBottom = Math.max(0, childBottom - parentBottom);
            int dx = ViewCompat.getLayoutDirection(parent) == 1 ? offScreenRight != 0 ? offScreenRight : offScreenLeft : offScreenLeft != 0 ? offScreenLeft : offScreenRight;
            int dy = offScreenTop != 0 ? offScreenTop : offScreenBottom;
            if (dx == 0 && dy == 0) {
                return false;
            }
            if (immediate) {
                parent.scrollBy(dx, dy);
            } else {
                parent.smoothScrollBy(dx, dy);
            }
            return true;
        }

        @Deprecated
        public boolean onRequestChildFocus(RecyclerView parent, View child, View focused) {
            return false;
        }

        public boolean onRequestChildFocus(RecyclerView parent, State state, View child, View focused) {
            return onRequestChildFocus(parent, child, focused);
        }

        public void onAdapterChanged(Adapter oldAdapter, Adapter newAdapter) {
        }

        public boolean onAddFocusables(RecyclerView recyclerView, ArrayList<View> arrayList, int direction, int focusableMode) {
            return false;
        }

        public void onItemsChanged(RecyclerView recyclerView) {
        }

        public void onItemsAdded(RecyclerView recyclerView, int positionStart, int itemCount) {
        }

        public void onItemsRemoved(RecyclerView recyclerView, int positionStart, int itemCount) {
        }

        public void onItemsUpdated(RecyclerView recyclerView, int positionStart, int itemCount) {
        }

        public void onItemsMoved(RecyclerView recyclerView, int from, int to, int itemCount) {
        }

        public int computeHorizontalScrollExtent(State state) {
            return 0;
        }

        public int computeHorizontalScrollOffset(State state) {
            return 0;
        }

        public int computeHorizontalScrollRange(State state) {
            return 0;
        }

        public int computeVerticalScrollExtent(State state) {
            return 0;
        }

        public int computeVerticalScrollOffset(State state) {
            return 0;
        }

        public int computeVerticalScrollRange(State state) {
            return 0;
        }

        public void onMeasure(Recycler recycler, State state, int widthSpec, int heightSpec) {
            int width;
            int height;
            int widthMode = MeasureSpec.getMode(widthSpec);
            int heightMode = MeasureSpec.getMode(heightSpec);
            int widthSize = MeasureSpec.getSize(widthSpec);
            int heightSize = MeasureSpec.getSize(heightSpec);
            switch (widthMode) {
                case Integer.MIN_VALUE:
                case 1073741824:
                    width = widthSize;
                    break;
                default:
                    width = getMinimumWidth();
                    break;
            }
            switch (heightMode) {
                case Integer.MIN_VALUE:
                case 1073741824:
                    height = heightSize;
                    break;
                default:
                    height = getMinimumHeight();
                    break;
            }
            setMeasuredDimension(width, height);
        }

        public void setMeasuredDimension(int widthSize, int heightSize) {
            this.mRecyclerView.setMeasuredDimension(widthSize, heightSize);
        }

        public int getMinimumWidth() {
            return ViewCompat.getMinimumWidth(this.mRecyclerView);
        }

        public int getMinimumHeight() {
            return ViewCompat.getMinimumHeight(this.mRecyclerView);
        }

        public Parcelable onSaveInstanceState() {
            return null;
        }

        public void onRestoreInstanceState(Parcelable state) {
        }

        void stopSmoothScroller() {
            if (this.mSmoothScroller != null) {
                this.mSmoothScroller.stop();
            }
        }

        private void onSmoothScrollerStopped(SmoothScroller smoothScroller) {
            if (this.mSmoothScroller == smoothScroller) {
                this.mSmoothScroller = null;
            }
        }

        public void onScrollStateChanged(int state) {
        }

        public void removeAndRecycleAllViews(Recycler recycler) {
            for (int i = getChildCount() - 1; i >= 0; i--) {
                if (!RecyclerView.getChildViewHolderInt(getChildAt(i)).shouldIgnore()) {
                    removeAndRecycleViewAt(i, recycler);
                }
            }
        }

        void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfoCompat info) {
            onInitializeAccessibilityNodeInfo(this.mRecyclerView.mRecycler, this.mRecyclerView.mState, info);
        }

        public void onInitializeAccessibilityNodeInfo(Recycler recycler, State state, AccessibilityNodeInfoCompat info) {
            info.setClassName(RecyclerView.class.getName());
            if (ViewCompat.canScrollVertically(this.mRecyclerView, -1) || ViewCompat.canScrollHorizontally(this.mRecyclerView, -1)) {
                info.addAction(8192);
                info.setScrollable(true);
            }
            if (ViewCompat.canScrollVertically(this.mRecyclerView, 1) || ViewCompat.canScrollHorizontally(this.mRecyclerView, 1)) {
                info.addAction(4096);
                info.setScrollable(true);
            }
            info.setCollectionInfo(CollectionInfoCompat.obtain(getRowCountForAccessibility(recycler, state), getColumnCountForAccessibility(recycler, state), isLayoutHierarchical(recycler, state), getSelectionModeForAccessibility(recycler, state)));
        }

        public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
            onInitializeAccessibilityEvent(this.mRecyclerView.mRecycler, this.mRecyclerView.mState, event);
        }

        public void onInitializeAccessibilityEvent(Recycler recycler, State state, AccessibilityEvent event) {
            boolean z = true;
            AccessibilityRecordCompat record = AccessibilityEventCompat.asRecord(event);
            if (this.mRecyclerView != null && record != null) {
                if (!(ViewCompat.canScrollVertically(this.mRecyclerView, 1) || ViewCompat.canScrollVertically(this.mRecyclerView, -1) || ViewCompat.canScrollHorizontally(this.mRecyclerView, -1))) {
                    z = ViewCompat.canScrollHorizontally(this.mRecyclerView, 1);
                }
                record.setScrollable(z);
                if (this.mRecyclerView.mAdapter != null) {
                    record.setItemCount(this.mRecyclerView.mAdapter.getItemCount());
                }
            }
        }

        void onInitializeAccessibilityNodeInfoForItem(View host, AccessibilityNodeInfoCompat info) {
            onInitializeAccessibilityNodeInfoForItem(this.mRecyclerView.mRecycler, this.mRecyclerView.mState, host, info);
        }

        public void onInitializeAccessibilityNodeInfoForItem(Recycler recycler, State state, View host, AccessibilityNodeInfoCompat info) {
            int columnIndexGuess;
            int rowIndexGuess = canScrollVertically() ? getPosition(host) : 0;
            if (canScrollHorizontally()) {
                columnIndexGuess = getPosition(host);
            } else {
                columnIndexGuess = 0;
            }
            info.setCollectionItemInfo(CollectionItemInfoCompat.obtain(rowIndexGuess, 1, columnIndexGuess, 1, false, false));
        }

        public void requestSimpleAnimationsInNextLayout() {
            this.mRequestedSimpleAnimations = true;
        }

        public int getSelectionModeForAccessibility(Recycler recycler, State state) {
            return 0;
        }

        public int getRowCountForAccessibility(Recycler recycler, State state) {
            int i = 1;
            if (this.mRecyclerView == null || this.mRecyclerView.mAdapter == null) {
                return 1;
            }
            if (canScrollVertically()) {
                i = this.mRecyclerView.mAdapter.getItemCount();
            }
            return i;
        }

        public int getColumnCountForAccessibility(Recycler recycler, State state) {
            int i = 1;
            if (this.mRecyclerView == null || this.mRecyclerView.mAdapter == null) {
                return 1;
            }
            if (canScrollHorizontally()) {
                i = this.mRecyclerView.mAdapter.getItemCount();
            }
            return i;
        }

        public boolean isLayoutHierarchical(Recycler recycler, State state) {
            return false;
        }

        boolean performAccessibilityAction(int action, Bundle args) {
            return performAccessibilityAction(this.mRecyclerView.mRecycler, this.mRecyclerView.mState, action, args);
        }

        public boolean performAccessibilityAction(Recycler recycler, State state, int action, Bundle args) {
            if (this.mRecyclerView == null) {
                return false;
            }
            int vScroll = 0;
            int hScroll = 0;
            switch (action) {
                case 4096:
                    if (ViewCompat.canScrollVertically(this.mRecyclerView, 1)) {
                        vScroll = (getHeight() - getPaddingTop()) - getPaddingBottom();
                    }
                    if (ViewCompat.canScrollHorizontally(this.mRecyclerView, 1)) {
                        hScroll = (getWidth() - getPaddingLeft()) - getPaddingRight();
                        break;
                    }
                    break;
                case 8192:
                    if (ViewCompat.canScrollVertically(this.mRecyclerView, -1)) {
                        vScroll = -((getHeight() - getPaddingTop()) - getPaddingBottom());
                    }
                    if (ViewCompat.canScrollHorizontally(this.mRecyclerView, -1)) {
                        hScroll = -((getWidth() - getPaddingLeft()) - getPaddingRight());
                        break;
                    }
                    break;
            }
            if (vScroll == 0 && hScroll == 0) {
                return false;
            }
            this.mRecyclerView.scrollBy(hScroll, vScroll);
            return true;
        }

        boolean performAccessibilityActionForItem(View view, int action, Bundle args) {
            return performAccessibilityActionForItem(this.mRecyclerView.mRecycler, this.mRecyclerView.mState, view, action, args);
        }

        public boolean performAccessibilityActionForItem(Recycler recycler, State state, View view, int action, Bundle args) {
            return false;
        }
    }

    public static abstract class SmoothScroller {
        private LayoutManager mLayoutManager;
        private boolean mPendingInitialRun;
        private RecyclerView mRecyclerView;
        private final Action mRecyclingAction = new Action(0, 0);
        private boolean mRunning;
        private int mTargetPosition = -1;
        private View mTargetView;

        public static class Action {
            public static final int UNDEFINED_DURATION = Integer.MIN_VALUE;
            private boolean changed;
            private int consecutiveUpdates;
            private int mDuration;
            private int mDx;
            private int mDy;
            private Interpolator mInterpolator;

            public Action(int dx, int dy) {
                this(dx, dy, Integer.MIN_VALUE, null);
            }

            public Action(int dx, int dy, int duration) {
                this(dx, dy, duration, null);
            }

            public Action(int dx, int dy, int duration, Interpolator interpolator) {
                this.changed = false;
                this.consecutiveUpdates = 0;
                this.mDx = dx;
                this.mDy = dy;
                this.mDuration = duration;
                this.mInterpolator = interpolator;
            }

            private void runIfNecessary(RecyclerView recyclerView) {
                if (this.changed) {
                    validate();
                    if (this.mInterpolator != null) {
                        recyclerView.mViewFlinger.smoothScrollBy(this.mDx, this.mDy, this.mDuration, this.mInterpolator);
                    } else if (this.mDuration == Integer.MIN_VALUE) {
                        recyclerView.mViewFlinger.smoothScrollBy(this.mDx, this.mDy);
                    } else {
                        recyclerView.mViewFlinger.smoothScrollBy(this.mDx, this.mDy, this.mDuration);
                    }
                    this.consecutiveUpdates++;
                    if (this.consecutiveUpdates > 10) {
                        Log.e(RecyclerView.TAG, "Smooth Scroll action is being updated too frequently. Make sure you are not changing it unless necessary");
                    }
                    this.changed = false;
                    return;
                }
                this.consecutiveUpdates = 0;
            }

            private void validate() {
                if (this.mInterpolator != null && this.mDuration < 1) {
                    throw new IllegalStateException("If you provide an interpolator, you must set a positive duration");
                } else if (this.mDuration < 1) {
                    throw new IllegalStateException("Scroll duration must be a positive number");
                }
            }

            public int getDx() {
                return this.mDx;
            }

            public void setDx(int dx) {
                this.changed = true;
                this.mDx = dx;
            }

            public int getDy() {
                return this.mDy;
            }

            public void setDy(int dy) {
                this.changed = true;
                this.mDy = dy;
            }

            public int getDuration() {
                return this.mDuration;
            }

            public void setDuration(int duration) {
                this.changed = true;
                this.mDuration = duration;
            }

            public Interpolator getInterpolator() {
                return this.mInterpolator;
            }

            public void setInterpolator(Interpolator interpolator) {
                this.changed = true;
                this.mInterpolator = interpolator;
            }

            public void update(int dx, int dy, int duration, Interpolator interpolator) {
                this.mDx = dx;
                this.mDy = dy;
                this.mDuration = duration;
                this.mInterpolator = interpolator;
                this.changed = true;
            }
        }

        protected abstract void onSeekTargetStep(int i, int i2, State state, Action action);

        protected abstract void onStart();

        protected abstract void onStop();

        protected abstract void onTargetFound(View view, State state, Action action);

        void start(RecyclerView recyclerView, LayoutManager layoutManager) {
            this.mRecyclerView = recyclerView;
            this.mLayoutManager = layoutManager;
            if (this.mTargetPosition == -1) {
                throw new IllegalArgumentException("Invalid target position");
            }
            this.mRecyclerView.mState.mTargetPosition = this.mTargetPosition;
            this.mRunning = true;
            this.mPendingInitialRun = true;
            this.mTargetView = findViewByPosition(getTargetPosition());
            onStart();
            this.mRecyclerView.mViewFlinger.postOnAnimation();
        }

        public void setTargetPosition(int targetPosition) {
            this.mTargetPosition = targetPosition;
        }

        public LayoutManager getLayoutManager() {
            return this.mLayoutManager;
        }

        protected final void stop() {
            if (this.mRunning) {
                onStop();
                this.mRecyclerView.mState.mTargetPosition = -1;
                this.mTargetView = null;
                this.mTargetPosition = -1;
                this.mPendingInitialRun = false;
                this.mRunning = false;
                this.mLayoutManager.onSmoothScrollerStopped(this);
                this.mLayoutManager = null;
                this.mRecyclerView = null;
            }
        }

        public boolean isPendingInitialRun() {
            return this.mPendingInitialRun;
        }

        public boolean isRunning() {
            return this.mRunning;
        }

        public int getTargetPosition() {
            return this.mTargetPosition;
        }

        private void onAnimation(int dx, int dy) {
            if (!this.mRunning || this.mTargetPosition == -1) {
                stop();
            }
            this.mPendingInitialRun = false;
            if (this.mTargetView != null) {
                if (getChildPosition(this.mTargetView) == this.mTargetPosition) {
                    onTargetFound(this.mTargetView, this.mRecyclerView.mState, this.mRecyclingAction);
                    this.mRecyclingAction.runIfNecessary(this.mRecyclerView);
                    stop();
                } else {
                    Log.e(RecyclerView.TAG, "Passed over target position while smooth scrolling.");
                    this.mTargetView = null;
                }
            }
            if (this.mRunning) {
                onSeekTargetStep(dx, dy, this.mRecyclerView.mState, this.mRecyclingAction);
                this.mRecyclingAction.runIfNecessary(this.mRecyclerView);
            }
        }

        public int getChildPosition(View view) {
            return this.mRecyclerView.getChildPosition(view);
        }

        public int getChildCount() {
            return this.mRecyclerView.mLayout.getChildCount();
        }

        public View findViewByPosition(int position) {
            return this.mRecyclerView.mLayout.findViewByPosition(position);
        }

        public void instantScrollToPosition(int position) {
            this.mRecyclerView.scrollToPosition(position);
        }

        protected void onChildAttachedToWindow(View child) {
            if (getChildPosition(child) == getTargetPosition()) {
                this.mTargetView = child;
            }
        }

        protected void normalize(PointF scrollVector) {
            double magnitute = Math.sqrt((double) ((scrollVector.x * scrollVector.x) + (scrollVector.y * scrollVector.y)));
            scrollVector.x = (float) (((double) scrollVector.x) / magnitute);
            scrollVector.y = (float) (((double) scrollVector.y) / magnitute);
        }
    }

    public static abstract class Adapter<VH extends ViewHolder> {
        private boolean mHasStableIds = false;
        private final AdapterDataObservable mObservable = new AdapterDataObservable();

        public abstract int getItemCount();

        public abstract void onBindViewHolder(VH vh, int i);

        public abstract VH onCreateViewHolder(ViewGroup viewGroup, int i);

        public final VH createViewHolder(ViewGroup parent, int viewType) {
            VH holder = onCreateViewHolder(parent, viewType);
            holder.mItemViewType = viewType;
            return holder;
        }

        public final void bindViewHolder(VH holder, int position) {
            holder.mPosition = position;
            if (hasStableIds()) {
                holder.mItemId = getItemId(position);
            }
            onBindViewHolder(holder, position);
            holder.setFlags(1, 7);
        }

        public int getItemViewType(int position) {
            return 0;
        }

        public void setHasStableIds(boolean hasStableIds) {
            if (hasObservers()) {
                throw new IllegalStateException("Cannot change whether this adapter has stable IDs while the adapter has registered observers.");
            }
            this.mHasStableIds = hasStableIds;
        }

        public long getItemId(int position) {
            return -1;
        }

        public final boolean hasStableIds() {
            return this.mHasStableIds;
        }

        public void onViewRecycled(VH vh) {
        }

        public void onViewAttachedToWindow(VH vh) {
        }

        public void onViewDetachedFromWindow(VH vh) {
        }

        public final boolean hasObservers() {
            return this.mObservable.hasObservers();
        }

        public void registerAdapterDataObserver(AdapterDataObserver observer) {
            this.mObservable.registerObserver(observer);
        }

        public void unregisterAdapterDataObserver(AdapterDataObserver observer) {
            this.mObservable.unregisterObserver(observer);
        }

        public final void notifyDataSetChanged() {
            this.mObservable.notifyChanged();
        }

        public final void notifyItemChanged(int position) {
            this.mObservable.notifyItemRangeChanged(position, 1);
        }

        public final void notifyItemRangeChanged(int positionStart, int itemCount) {
            this.mObservable.notifyItemRangeChanged(positionStart, itemCount);
        }

        public final void notifyItemInserted(int position) {
            this.mObservable.notifyItemRangeInserted(position, 1);
        }

        public final void notifyItemMoved(int fromPosition, int toPosition) {
            this.mObservable.notifyItemMoved(fromPosition, toPosition);
        }

        public final void notifyItemRangeInserted(int positionStart, int itemCount) {
            this.mObservable.notifyItemRangeInserted(positionStart, itemCount);
        }

        public final void notifyItemRemoved(int position) {
            this.mObservable.notifyItemRangeRemoved(position, 1);
        }

        public final void notifyItemRangeRemoved(int positionStart, int itemCount) {
            this.mObservable.notifyItemRangeRemoved(positionStart, itemCount);
        }
    }

    static class AdapterDataObservable extends Observable<AdapterDataObserver> {
        AdapterDataObservable() {
        }

        public boolean hasObservers() {
            return !this.mObservers.isEmpty();
        }

        public void notifyChanged() {
            for (int i = this.mObservers.size() - 1; i >= 0; i--) {
                ((AdapterDataObserver) this.mObservers.get(i)).onChanged();
            }
        }

        public void notifyItemRangeChanged(int positionStart, int itemCount) {
            for (int i = this.mObservers.size() - 1; i >= 0; i--) {
                ((AdapterDataObserver) this.mObservers.get(i)).onItemRangeChanged(positionStart, itemCount);
            }
        }

        public void notifyItemRangeInserted(int positionStart, int itemCount) {
            for (int i = this.mObservers.size() - 1; i >= 0; i--) {
                ((AdapterDataObserver) this.mObservers.get(i)).onItemRangeInserted(positionStart, itemCount);
            }
        }

        public void notifyItemRangeRemoved(int positionStart, int itemCount) {
            for (int i = this.mObservers.size() - 1; i >= 0; i--) {
                ((AdapterDataObserver) this.mObservers.get(i)).onItemRangeRemoved(positionStart, itemCount);
            }
        }

        public void notifyItemMoved(int fromPosition, int toPosition) {
            for (int i = this.mObservers.size() - 1; i >= 0; i--) {
                ((AdapterDataObserver) this.mObservers.get(i)).onItemRangeMoved(fromPosition, toPosition, 1);
            }
        }
    }

    public static abstract class AdapterDataObserver {
        public void onChanged() {
        }

        public void onItemRangeChanged(int positionStart, int itemCount) {
        }

        public void onItemRangeInserted(int positionStart, int itemCount) {
        }

        public void onItemRangeRemoved(int positionStart, int itemCount) {
        }

        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        }
    }

    private class ItemAnimatorRestoreListener implements ItemAnimatorListener {
        /* synthetic */ ItemAnimatorRestoreListener(RecyclerView this$0, ItemAnimatorRestoreListener itemAnimatorRestoreListener) {
            this();
        }

        private ItemAnimatorRestoreListener() {
        }

        public void onRemoveFinished(ViewHolder item) {
            item.setIsRecyclable(true);
            RecyclerView.this.removeAnimatingView(item.itemView);
            RecyclerView.this.removeDetachedView(item.itemView, false);
        }

        public void onAddFinished(ViewHolder item) {
            item.setIsRecyclable(true);
            if (item.isRecyclable()) {
                RecyclerView.this.removeAnimatingView(item.itemView);
            }
        }

        public void onMoveFinished(ViewHolder item) {
            item.setIsRecyclable(true);
            if (item.isRecyclable()) {
                RecyclerView.this.removeAnimatingView(item.itemView);
            }
        }

        public void onChangeFinished(ViewHolder item) {
            item.setIsRecyclable(true);
            if (item.mShadowedHolder != null && item.mShadowingHolder == null) {
                item.mShadowedHolder = null;
                item.setFlags(-65, item.mFlags);
            }
            item.mShadowingHolder = null;
            if (item.isRecyclable()) {
                RecyclerView.this.removeAnimatingView(item.itemView);
            }
        }
    }

    public static abstract class ItemDecoration {
        public void onDraw(Canvas c, RecyclerView parent, State state) {
            onDraw(c, parent);
        }

        @Deprecated
        public void onDraw(Canvas c, RecyclerView parent) {
        }

        public void onDrawOver(Canvas c, RecyclerView parent, State state) {
            onDrawOver(c, parent);
        }

        @Deprecated
        public void onDrawOver(Canvas c, RecyclerView parent) {
        }

        @Deprecated
        public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
            outRect.set(0, 0, 0, 0);
        }

        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
            getItemOffsets(outRect, ((LayoutParams) view.getLayoutParams()).getViewPosition(), parent);
        }
    }

    private static class ItemHolderInfo {
        int bottom;
        ViewHolder holder;
        int left;
        int right;
        int top;

        ItemHolderInfo(ViewHolder holder, int left, int top, int right, int bottom) {
            this.holder = holder;
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }
    }

    public interface OnItemTouchListener {
        boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent);

        void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent);
    }

    public static abstract class OnScrollListener {
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        }

        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        }
    }

    public static class RecycledViewPool {
        private static final int DEFAULT_MAX_SCRAP = 5;
        private int mAttachCount = 0;
        private SparseIntArray mMaxScrap = new SparseIntArray();
        private SparseArray<ArrayList<ViewHolder>> mScrap = new SparseArray();

        public void clear() {
            this.mScrap.clear();
        }

        public void setMaxRecycledViews(int viewType, int max) {
            this.mMaxScrap.put(viewType, max);
            ArrayList<ViewHolder> scrapHeap = (ArrayList) this.mScrap.get(viewType);
            if (scrapHeap != null) {
                while (scrapHeap.size() > max) {
                    scrapHeap.remove(scrapHeap.size() - 1);
                }
            }
        }

        public ViewHolder getRecycledView(int viewType) {
            ArrayList<ViewHolder> scrapHeap = (ArrayList) this.mScrap.get(viewType);
            if (scrapHeap == null || scrapHeap.isEmpty()) {
                return null;
            }
            int index = scrapHeap.size() - 1;
            ViewHolder scrap = (ViewHolder) scrapHeap.get(index);
            scrapHeap.remove(index);
            return scrap;
        }

        int size() {
            int count = 0;
            for (int i = 0; i < this.mScrap.size(); i++) {
                ArrayList<ViewHolder> viewHolders = (ArrayList) this.mScrap.valueAt(i);
                if (viewHolders != null) {
                    count += viewHolders.size();
                }
            }
            return count;
        }

        public void putRecycledView(ViewHolder scrap) {
            int viewType = scrap.getItemViewType();
            ArrayList scrapHeap = getScrapHeapForType(viewType);
            if (this.mMaxScrap.get(viewType) > scrapHeap.size()) {
                scrap.resetInternal();
                scrapHeap.add(scrap);
            }
        }

        void attach(Adapter adapter) {
            this.mAttachCount++;
        }

        void detach() {
            this.mAttachCount--;
        }

        void onAdapterChanged(Adapter oldAdapter, Adapter newAdapter, boolean compatibleWithPrevious) {
            if (oldAdapter != null) {
                detach();
            }
            if (!compatibleWithPrevious && this.mAttachCount == 0) {
                clear();
            }
            if (newAdapter != null) {
                attach(newAdapter);
            }
        }

        private ArrayList<ViewHolder> getScrapHeapForType(int viewType) {
            ArrayList<ViewHolder> scrap = (ArrayList) this.mScrap.get(viewType);
            if (scrap == null) {
                scrap = new ArrayList();
                this.mScrap.put(viewType, scrap);
                if (this.mMaxScrap.indexOfKey(viewType) < 0) {
                    this.mMaxScrap.put(viewType, 5);
                }
            }
            return scrap;
        }
    }

    public final class Recycler {
        private static final int DEFAULT_CACHE_SIZE = 2;
        final ArrayList<ViewHolder> mAttachedScrap = new ArrayList();
        final ArrayList<ViewHolder> mCachedViews = new ArrayList();
        private ArrayList<ViewHolder> mChangedScrap = null;
        private RecycledViewPool mRecyclerPool;
        private final List<ViewHolder> mUnmodifiableAttachedScrap = Collections.unmodifiableList(this.mAttachedScrap);
        private ViewCacheExtension mViewCacheExtension;
        private int mViewCacheMax = 2;

        public void clear() {
            this.mAttachedScrap.clear();
            recycleAndClearCachedViews();
        }

        public void setViewCacheSize(int viewCount) {
            this.mViewCacheMax = viewCount;
            for (int i = this.mCachedViews.size() - 1; i >= 0 && this.mCachedViews.size() > viewCount; i--) {
                tryToRecycleCachedViewAt(i);
            }
            while (this.mCachedViews.size() > viewCount) {
                this.mCachedViews.remove(this.mCachedViews.size() - 1);
            }
        }

        public List<ViewHolder> getScrapList() {
            return this.mUnmodifiableAttachedScrap;
        }

        boolean validateViewHolderForOffsetPosition(ViewHolder holder) {
            boolean z = true;
            if (holder.isRemoved()) {
                return true;
            }
            if (holder.mPosition < 0 || holder.mPosition >= RecyclerView.this.mAdapter.getItemCount()) {
                throw new IndexOutOfBoundsException("Inconsistency detected. Invalid view holder adapter position" + holder);
            } else if (!RecyclerView.this.mState.isPreLayout() && RecyclerView.this.mAdapter.getItemViewType(holder.mPosition) != holder.getItemViewType()) {
                return false;
            } else {
                if (!RecyclerView.this.mAdapter.hasStableIds()) {
                    return true;
                }
                if (holder.getItemId() != RecyclerView.this.mAdapter.getItemId(holder.mPosition)) {
                    z = false;
                }
                return z;
            }
        }

        public void bindViewToPosition(View view, int position) {
            boolean z = true;
            ViewHolder holder = RecyclerView.getChildViewHolderInt(view);
            if (holder == null) {
                throw new IllegalArgumentException("The view does not have a ViewHolder. You cannot pass arbitrary views to this method, they should be created by the Adapter");
            }
            int offsetPosition = RecyclerView.this.mAdapterHelper.findPositionOffset(position);
            if (offsetPosition < 0 || offsetPosition >= RecyclerView.this.mAdapter.getItemCount()) {
                throw new IndexOutOfBoundsException("Inconsistency detected. Invalid item position " + position + "(offset:" + offsetPosition + ")." + "state:" + RecyclerView.this.mState.getItemCount());
            }
            LayoutParams rvLayoutParams;
            RecyclerView.this.mAdapter.bindViewHolder(holder, offsetPosition);
            attachAccessibilityDelegate(view);
            if (RecyclerView.this.mState.isPreLayout()) {
                holder.mPreLayoutPosition = position;
            }
            android.view.ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp == null) {
                rvLayoutParams = (LayoutParams) RecyclerView.this.generateDefaultLayoutParams();
                holder.itemView.setLayoutParams(rvLayoutParams);
            } else if (RecyclerView.this.checkLayoutParams(lp)) {
                rvLayoutParams = (LayoutParams) lp;
            } else {
                rvLayoutParams = (LayoutParams) RecyclerView.this.generateLayoutParams(lp);
                holder.itemView.setLayoutParams(rvLayoutParams);
            }
            rvLayoutParams.mInsetsDirty = true;
            rvLayoutParams.mViewHolder = holder;
            if (holder.itemView.getParent() != null) {
                z = false;
            }
            rvLayoutParams.mPendingInvalidate = z;
        }

        public int convertPreLayoutPositionToPostLayout(int position) {
            if (position < 0 || position >= RecyclerView.this.mState.getItemCount()) {
                throw new IndexOutOfBoundsException("invalid position " + position + ". State " + "item count is " + RecyclerView.this.mState.getItemCount());
            } else if (RecyclerView.this.mState.isPreLayout()) {
                return RecyclerView.this.mAdapterHelper.findPositionOffset(position);
            } else {
                return position;
            }
        }

        public View getViewForPosition(int position) {
            return getViewForPosition(position, false);
        }

        View getViewForPosition(int position, boolean dryRun) {
            if (position < 0 || position >= RecyclerView.this.mState.getItemCount()) {
                throw new IndexOutOfBoundsException("Invalid item position " + position + "(" + position + "). Item count:" + RecyclerView.this.mState.getItemCount());
            }
            LayoutParams rvLayoutParams;
            boolean fromScrap = false;
            ViewHolder holder = null;
            if (RecyclerView.this.mState.isPreLayout()) {
                holder = getChangedScrapViewForPosition(position);
                fromScrap = holder != null;
            }
            if (holder == null) {
                holder = getScrapViewForPosition(position, -1, dryRun);
                if (holder != null) {
                    if (validateViewHolderForOffsetPosition(holder)) {
                        fromScrap = true;
                    } else {
                        if (!dryRun) {
                            holder.addFlags(4);
                            if (holder.isScrap()) {
                                RecyclerView.this.removeDetachedView(holder.itemView, false);
                                holder.unScrap();
                            } else if (holder.wasReturnedFromScrap()) {
                                holder.clearReturnedFromScrapFlag();
                            }
                            recycleViewHolderInternal(holder);
                        }
                        holder = null;
                    }
                }
            }
            if (holder == null) {
                int offsetPosition = RecyclerView.this.mAdapterHelper.findPositionOffset(position);
                if (offsetPosition < 0 || offsetPosition >= RecyclerView.this.mAdapter.getItemCount()) {
                    throw new IndexOutOfBoundsException("Inconsistency detected. Invalid item position " + position + "(offset:" + offsetPosition + ")." + "state:" + RecyclerView.this.mState.getItemCount());
                }
                int type = RecyclerView.this.mAdapter.getItemViewType(offsetPosition);
                if (RecyclerView.this.mAdapter.hasStableIds()) {
                    holder = getScrapViewForId(RecyclerView.this.mAdapter.getItemId(offsetPosition), type, dryRun);
                    if (holder != null) {
                        holder.mPosition = offsetPosition;
                        fromScrap = true;
                    }
                }
                if (holder == null && this.mViewCacheExtension != null) {
                    View view = this.mViewCacheExtension.getViewForPositionAndType(this, position, type);
                    if (view != null) {
                        holder = RecyclerView.this.getChildViewHolder(view);
                        if (holder == null) {
                            throw new IllegalArgumentException("getViewForPositionAndType returned a view which does not have a ViewHolder");
                        } else if (holder.shouldIgnore()) {
                            throw new IllegalArgumentException("getViewForPositionAndType returned a view that is ignored. You must call stopIgnoring before returning this view.");
                        }
                    }
                }
                if (holder == null) {
                    holder = getRecycledViewPool().getRecycledView(RecyclerView.this.mAdapter.getItemViewType(offsetPosition));
                    if (holder != null) {
                        holder.resetInternal();
                        if (RecyclerView.FORCE_INVALIDATE_DISPLAY_LIST) {
                            invalidateDisplayListInt(holder);
                        }
                    }
                }
                if (holder == null) {
                    holder = RecyclerView.this.mAdapter.createViewHolder(RecyclerView.this, RecyclerView.this.mAdapter.getItemViewType(offsetPosition));
                }
            }
            boolean bound = false;
            if (RecyclerView.this.mState.isPreLayout() && holder.isBound()) {
                holder.mPreLayoutPosition = position;
            } else if (!holder.isBound() || holder.needsUpdate() || holder.isInvalid()) {
                RecyclerView.this.mAdapter.bindViewHolder(holder, RecyclerView.this.mAdapterHelper.findPositionOffset(position));
                attachAccessibilityDelegate(holder.itemView);
                bound = true;
                if (RecyclerView.this.mState.isPreLayout()) {
                    holder.mPreLayoutPosition = position;
                }
            }
            android.view.ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp == null) {
                rvLayoutParams = (LayoutParams) RecyclerView.this.generateDefaultLayoutParams();
                holder.itemView.setLayoutParams(rvLayoutParams);
            } else if (RecyclerView.this.checkLayoutParams(lp)) {
                rvLayoutParams = (LayoutParams) lp;
            } else {
                rvLayoutParams = (LayoutParams) RecyclerView.this.generateLayoutParams(lp);
                holder.itemView.setLayoutParams(rvLayoutParams);
            }
            rvLayoutParams.mViewHolder = holder;
            if (!fromScrap) {
                bound = false;
            }
            rvLayoutParams.mPendingInvalidate = bound;
            return holder.itemView;
        }

        private void attachAccessibilityDelegate(View itemView) {
            if (RecyclerView.this.mAccessibilityManager != null && RecyclerView.this.mAccessibilityManager.isEnabled()) {
                if (ViewCompat.getImportantForAccessibility(itemView) == 0) {
                    ViewCompat.setImportantForAccessibility(itemView, 1);
                }
                if (!ViewCompat.hasAccessibilityDelegate(itemView)) {
                    ViewCompat.setAccessibilityDelegate(itemView, RecyclerView.this.mAccessibilityDelegate.getItemDelegate());
                }
            }
        }

        private void invalidateDisplayListInt(ViewHolder holder) {
            if (holder.itemView instanceof ViewGroup) {
                invalidateDisplayListInt((ViewGroup) holder.itemView, false);
            }
        }

        private void invalidateDisplayListInt(ViewGroup viewGroup, boolean invalidateThis) {
            for (int i = viewGroup.getChildCount() - 1; i >= 0; i--) {
                View view = viewGroup.getChildAt(i);
                if (view instanceof ViewGroup) {
                    invalidateDisplayListInt((ViewGroup) view, true);
                }
            }
            if (invalidateThis) {
                if (viewGroup.getVisibility() == 4) {
                    viewGroup.setVisibility(0);
                    viewGroup.setVisibility(4);
                } else {
                    int visibility = viewGroup.getVisibility();
                    viewGroup.setVisibility(4);
                    viewGroup.setVisibility(visibility);
                }
            }
        }

        public void recycleView(View view) {
            ViewHolder holder = RecyclerView.getChildViewHolderInt(view);
            if (holder.isScrap()) {
                holder.unScrap();
            } else if (holder.wasReturnedFromScrap()) {
                holder.clearReturnedFromScrapFlag();
            }
            recycleViewHolderInternal(holder);
        }

        void recycleViewInternal(View view) {
            recycleViewHolderInternal(RecyclerView.getChildViewHolderInt(view));
        }

        void recycleAndClearCachedViews() {
            for (int i = this.mCachedViews.size() - 1; i >= 0; i--) {
                tryToRecycleCachedViewAt(i);
            }
            this.mCachedViews.clear();
        }

        boolean tryToRecycleCachedViewAt(int cachedViewIndex) {
            ViewHolder viewHolder = (ViewHolder) this.mCachedViews.get(cachedViewIndex);
            if (!viewHolder.isRecyclable()) {
                return false;
            }
            getRecycledViewPool().putRecycledView(viewHolder);
            dispatchViewRecycled(viewHolder);
            this.mCachedViews.remove(cachedViewIndex);
            return true;
        }

        void recycleViewHolderInternal(ViewHolder holder) {
            if (holder.isScrap() || holder.itemView.getParent() != null) {
                throw new IllegalArgumentException("Scrapped or attached views may not be recycled. isScrap:" + holder.isScrap() + " isAttached:" + (holder.itemView.getParent() != null));
            } else if (holder.shouldIgnore()) {
                throw new IllegalArgumentException("Trying to recycle an ignored view holder. You should first call stopIgnoringView(view) before calling recycle.");
            } else {
                if (holder.isRecyclable()) {
                    boolean cached = false;
                    if (!holder.isInvalid() && ((RecyclerView.this.mState.mInPreLayout || !holder.isRemoved()) && !holder.isChanged())) {
                        if (this.mCachedViews.size() == this.mViewCacheMax && !this.mCachedViews.isEmpty()) {
                            int i = 0;
                            while (i < this.mCachedViews.size() && !tryToRecycleCachedViewAt(i)) {
                                i++;
                            }
                        }
                        if (this.mCachedViews.size() < this.mViewCacheMax) {
                            this.mCachedViews.add(holder);
                            cached = true;
                        }
                    }
                    if (!cached) {
                        getRecycledViewPool().putRecycledView(holder);
                        dispatchViewRecycled(holder);
                    }
                }
                RecyclerView.this.mState.onViewRecycled(holder);
            }
        }

        void quickRecycleScrapView(View view) {
            ViewHolder holder = RecyclerView.getChildViewHolderInt(view);
            holder.mScrapContainer = null;
            holder.clearReturnedFromScrapFlag();
            recycleViewHolderInternal(holder);
        }

        void scrapView(View view) {
            ViewHolder holder = RecyclerView.getChildViewHolderInt(view);
            holder.setScrapContainer(this);
            if (holder.isChanged() && RecyclerView.this.supportsChangeAnimations()) {
                if (this.mChangedScrap == null) {
                    this.mChangedScrap = new ArrayList();
                }
                this.mChangedScrap.add(holder);
            } else if (!holder.isInvalid() || holder.isRemoved() || RecyclerView.this.mAdapter.hasStableIds()) {
                this.mAttachedScrap.add(holder);
            } else {
                throw new IllegalArgumentException("Called scrap view with an invalid view. Invalid views cannot be reused from scrap, they should rebound from recycler pool.");
            }
        }

        void unscrapView(ViewHolder holder) {
            if (holder.isChanged() && RecyclerView.this.supportsChangeAnimations() && this.mChangedScrap != null) {
                this.mChangedScrap.remove(holder);
            } else {
                this.mAttachedScrap.remove(holder);
            }
            holder.mScrapContainer = null;
            holder.clearReturnedFromScrapFlag();
        }

        int getScrapCount() {
            return this.mAttachedScrap.size();
        }

        View getScrapViewAt(int index) {
            return ((ViewHolder) this.mAttachedScrap.get(index)).itemView;
        }

        void clearScrap() {
            this.mAttachedScrap.clear();
        }

        ViewHolder getChangedScrapViewForPosition(int position) {
            if (this.mChangedScrap != null) {
                int changedScrapSize = this.mChangedScrap.size();
                if (changedScrapSize != 0) {
                    ViewHolder holder;
                    int i = 0;
                    while (i < changedScrapSize) {
                        holder = (ViewHolder) this.mChangedScrap.get(i);
                        if (holder.wasReturnedFromScrap() || holder.getPosition() != position) {
                            i++;
                        } else {
                            holder.addFlags(32);
                            return holder;
                        }
                    }
                    if (RecyclerView.this.mAdapter.hasStableIds()) {
                        int offsetPosition = RecyclerView.this.mAdapterHelper.findPositionOffset(position);
                        if (offsetPosition > 0 && offsetPosition < RecyclerView.this.mAdapter.getItemCount()) {
                            long id = RecyclerView.this.mAdapter.getItemId(offsetPosition);
                            i = 0;
                            while (i < changedScrapSize) {
                                holder = (ViewHolder) this.mChangedScrap.get(i);
                                if (holder.wasReturnedFromScrap() || holder.getItemId() != id) {
                                    i++;
                                } else {
                                    holder.addFlags(32);
                                    return holder;
                                }
                            }
                        }
                    }
                    return null;
                }
            }
            return null;
        }

        /* JADX WARNING: Removed duplicated region for block: B:19:0x0084  */
        /* JADX WARNING: Removed duplicated region for block: B:24:0x00a4  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        ViewHolder getScrapViewForPosition(int position, int type, boolean dryRun) {
            int cacheSize;
            int scrapCount = this.mAttachedScrap.size();
            int i = 0;
            while (i < scrapCount) {
                ViewHolder holder = (ViewHolder) this.mAttachedScrap.get(i);
                if (holder.wasReturnedFromScrap() || holder.getPosition() != position || holder.isInvalid() || (!RecyclerView.this.mState.mInPreLayout && holder.isRemoved())) {
                    i++;
                } else if (type == -1 || holder.getItemViewType() == type) {
                    holder.addFlags(32);
                    return holder;
                } else {
                    Log.e(RecyclerView.TAG, "Scrap view for position " + position + " isn't dirty but has" + " wrong view type! (found " + holder.getItemViewType() + " but expected " + type + ")");
                    if (!dryRun) {
                        View view = RecyclerView.this.mChildHelper.findHiddenNonRemovedView(position, type);
                        if (view != null) {
                            RecyclerView.this.mItemAnimator.endAnimation(RecyclerView.this.getChildViewHolder(view));
                        }
                    }
                    cacheSize = this.mCachedViews.size();
                    i = 0;
                    while (i < cacheSize) {
                        holder = (ViewHolder) this.mCachedViews.get(i);
                        if (holder.isInvalid() || holder.getPosition() != position) {
                            i++;
                        } else {
                            if (!dryRun) {
                                this.mCachedViews.remove(i);
                            }
                            return holder;
                        }
                    }
                    return null;
                }
            }
            if (dryRun) {
            }
            cacheSize = this.mCachedViews.size();
            i = 0;
            while (i < cacheSize) {
            }
            return null;
        }

        ViewHolder getScrapViewForId(long id, int type, boolean dryRun) {
            int i;
            ViewHolder holder;
            for (i = this.mAttachedScrap.size() - 1; i >= 0; i--) {
                holder = (ViewHolder) this.mAttachedScrap.get(i);
                if (holder.getItemId() == id && !holder.wasReturnedFromScrap()) {
                    if (type == holder.getItemViewType()) {
                        holder.addFlags(32);
                        if (holder.isRemoved() && !RecyclerView.this.mState.isPreLayout()) {
                            holder.setFlags(2, 14);
                        }
                        return holder;
                    } else if (!dryRun) {
                        this.mAttachedScrap.remove(i);
                        RecyclerView.this.removeDetachedView(holder.itemView, false);
                        quickRecycleScrapView(holder.itemView);
                    }
                }
            }
            for (i = this.mCachedViews.size() - 1; i >= 0; i--) {
                holder = (ViewHolder) this.mCachedViews.get(i);
                if (holder.getItemId() == id) {
                    if (type == holder.getItemViewType()) {
                        if (!dryRun) {
                            this.mCachedViews.remove(i);
                        }
                        return holder;
                    } else if (!dryRun) {
                        tryToRecycleCachedViewAt(i);
                    }
                }
            }
            return null;
        }

        void dispatchViewRecycled(ViewHolder holder) {
            if (RecyclerView.this.mRecyclerListener != null) {
                RecyclerView.this.mRecyclerListener.onViewRecycled(holder);
            }
            if (RecyclerView.this.mAdapter != null) {
                RecyclerView.this.mAdapter.onViewRecycled(holder);
            }
            if (RecyclerView.this.mState != null) {
                RecyclerView.this.mState.onViewRecycled(holder);
            }
        }

        void onAdapterChanged(Adapter oldAdapter, Adapter newAdapter, boolean compatibleWithPrevious) {
            clear();
            getRecycledViewPool().onAdapterChanged(oldAdapter, newAdapter, compatibleWithPrevious);
        }

        void offsetPositionRecordsForMove(int from, int to) {
            int inBetweenOffset;
            int start;
            int end;
            if (from < to) {
                start = from;
                end = to;
                inBetweenOffset = -1;
            } else {
                start = to;
                end = from;
                inBetweenOffset = 1;
            }
            int cachedCount = this.mCachedViews.size();
            for (int i = 0; i < cachedCount; i++) {
                ViewHolder holder = (ViewHolder) this.mCachedViews.get(i);
                if (holder != null && holder.mPosition >= start && holder.mPosition <= end) {
                    if (holder.mPosition == from) {
                        holder.offsetPosition(to - from, false);
                    } else {
                        holder.offsetPosition(inBetweenOffset, false);
                    }
                }
            }
        }

        void offsetPositionRecordsForInsert(int insertedAt, int count) {
            int cachedCount = this.mCachedViews.size();
            for (int i = 0; i < cachedCount; i++) {
                ViewHolder holder = (ViewHolder) this.mCachedViews.get(i);
                if (holder != null && holder.getPosition() >= insertedAt) {
                    holder.offsetPosition(count, true);
                }
            }
        }

        void offsetPositionRecordsForRemove(int removedFrom, int count, boolean applyToPreLayout) {
            int removedEnd = removedFrom + count;
            int i = this.mCachedViews.size() - 1;
            while (i >= 0) {
                ViewHolder holder = (ViewHolder) this.mCachedViews.get(i);
                if (holder != null) {
                    if (holder.getPosition() >= removedEnd) {
                        holder.offsetPosition(-count, applyToPreLayout);
                    } else if (holder.getPosition() >= removedFrom && !tryToRecycleCachedViewAt(i)) {
                        holder.addFlags(4);
                    }
                }
                i--;
            }
        }

        void setViewCacheExtension(ViewCacheExtension extension) {
            this.mViewCacheExtension = extension;
        }

        void setRecycledViewPool(RecycledViewPool pool) {
            if (this.mRecyclerPool != null) {
                this.mRecyclerPool.detach();
            }
            this.mRecyclerPool = pool;
            if (pool != null) {
                this.mRecyclerPool.attach(RecyclerView.this.getAdapter());
            }
        }

        RecycledViewPool getRecycledViewPool() {
            if (this.mRecyclerPool == null) {
                this.mRecyclerPool = new RecycledViewPool();
            }
            return this.mRecyclerPool;
        }

        void viewRangeUpdate(int positionStart, int itemCount) {
            int positionEnd = positionStart + itemCount;
            int cachedCount = this.mCachedViews.size();
            for (int i = 0; i < cachedCount; i++) {
                ViewHolder holder = (ViewHolder) this.mCachedViews.get(i);
                if (holder != null) {
                    int pos = holder.getPosition();
                    if (pos >= positionStart && pos < positionEnd) {
                        holder.addFlags(2);
                    }
                }
            }
        }

        void markKnownViewsInvalid() {
            int i;
            if (RecyclerView.this.mAdapter == null || !RecyclerView.this.mAdapter.hasStableIds()) {
                for (i = this.mCachedViews.size() - 1; i >= 0; i--) {
                    if (!tryToRecycleCachedViewAt(i)) {
                        ((ViewHolder) this.mCachedViews.get(i)).addFlags(6);
                    }
                }
                return;
            }
            int cachedCount = this.mCachedViews.size();
            for (i = 0; i < cachedCount; i++) {
                ViewHolder holder = (ViewHolder) this.mCachedViews.get(i);
                if (holder != null) {
                    holder.addFlags(6);
                }
            }
        }

        void clearOldPositions() {
            int i;
            int cachedCount = this.mCachedViews.size();
            for (i = 0; i < cachedCount; i++) {
                ((ViewHolder) this.mCachedViews.get(i)).clearOldPosition();
            }
            int scrapCount = this.mAttachedScrap.size();
            for (i = 0; i < scrapCount; i++) {
                ((ViewHolder) this.mAttachedScrap.get(i)).clearOldPosition();
            }
            if (this.mChangedScrap != null) {
                int changedScrapCount = this.mChangedScrap.size();
                for (i = 0; i < changedScrapCount; i++) {
                    ((ViewHolder) this.mChangedScrap.get(i)).clearOldPosition();
                }
            }
        }

        void markItemDecorInsetsDirty() {
            int cachedCount = this.mCachedViews.size();
            for (int i = 0; i < cachedCount; i++) {
                LayoutParams layoutParams = (LayoutParams) ((ViewHolder) this.mCachedViews.get(i)).itemView.getLayoutParams();
                if (layoutParams != null) {
                    layoutParams.mInsetsDirty = true;
                }
            }
        }
    }

    public interface RecyclerListener {
        void onViewRecycled(ViewHolder viewHolder);
    }

    private class RecyclerViewDataObserver extends AdapterDataObserver {
        /* synthetic */ RecyclerViewDataObserver(RecyclerView this$0, RecyclerViewDataObserver recyclerViewDataObserver) {
            this();
        }

        private RecyclerViewDataObserver() {
        }

        public void onChanged() {
            RecyclerView.this.assertNotInLayoutOrScroll(null);
            if (RecyclerView.this.mAdapter.hasStableIds()) {
                RecyclerView.this.mState.mStructureChanged = true;
                RecyclerView.this.mDataSetHasChangedAfterLayout = true;
            } else {
                RecyclerView.this.mState.mStructureChanged = true;
                RecyclerView.this.mDataSetHasChangedAfterLayout = true;
            }
            if (!RecyclerView.this.mAdapterHelper.hasPendingUpdates()) {
                RecyclerView.this.requestLayout();
            }
        }

        public void onItemRangeChanged(int positionStart, int itemCount) {
            RecyclerView.this.assertNotInLayoutOrScroll(null);
            if (RecyclerView.this.mAdapterHelper.onItemRangeChanged(positionStart, itemCount)) {
                triggerUpdateProcessor();
            }
        }

        public void onItemRangeInserted(int positionStart, int itemCount) {
            RecyclerView.this.assertNotInLayoutOrScroll(null);
            if (RecyclerView.this.mAdapterHelper.onItemRangeInserted(positionStart, itemCount)) {
                triggerUpdateProcessor();
            }
        }

        public void onItemRangeRemoved(int positionStart, int itemCount) {
            RecyclerView.this.assertNotInLayoutOrScroll(null);
            if (RecyclerView.this.mAdapterHelper.onItemRangeRemoved(positionStart, itemCount)) {
                triggerUpdateProcessor();
            }
        }

        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            RecyclerView.this.assertNotInLayoutOrScroll(null);
            if (RecyclerView.this.mAdapterHelper.onItemRangeMoved(fromPosition, toPosition, itemCount)) {
                triggerUpdateProcessor();
            }
        }

        void triggerUpdateProcessor() {
            if (RecyclerView.this.mPostUpdatesOnAnimation && RecyclerView.this.mHasFixedSize && RecyclerView.this.mIsAttached) {
                ViewCompat.postOnAnimation(RecyclerView.this, RecyclerView.this.mUpdateChildViewsRunnable);
                return;
            }
            RecyclerView.this.mAdapterUpdateDuringMeasure = true;
            RecyclerView.this.requestLayout();
        }
    }

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = null;
        Parcelable mLayoutState;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.support.v7.widget.RecyclerView.SavedState.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.support.v7.widget.RecyclerView.SavedState.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.support.v7.widget.RecyclerView.SavedState.<clinit>():void");
        }

        SavedState(Parcel in) {
            super(in);
            this.mLayoutState = in.readParcelable(LayoutManager.class.getClassLoader());
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeParcelable(this.mLayoutState, 0);
        }

        private void copyFrom(SavedState other) {
            this.mLayoutState = other.mLayoutState;
        }
    }

    public static class State {
        private SparseArray<Object> mData;
        private int mDeletedInvisibleItemCountSincePreviousLayout;
        private boolean mInPreLayout;
        int mItemCount;
        ArrayMap<Long, ViewHolder> mOldChangedHolders;
        ArrayMap<ViewHolder, ItemHolderInfo> mPostLayoutHolderMap;
        ArrayMap<ViewHolder, ItemHolderInfo> mPreLayoutHolderMap;
        private int mPreviousLayoutItemCount;
        private boolean mRunPredictiveAnimations;
        private boolean mRunSimpleAnimations;
        private boolean mStructureChanged;
        private int mTargetPosition;

        public State() {
            this.mTargetPosition = -1;
            this.mPreLayoutHolderMap = new ArrayMap();
            this.mPostLayoutHolderMap = new ArrayMap();
            this.mOldChangedHolders = new ArrayMap();
            this.mItemCount = 0;
            this.mPreviousLayoutItemCount = 0;
            this.mDeletedInvisibleItemCountSincePreviousLayout = 0;
            this.mStructureChanged = false;
            this.mInPreLayout = false;
            this.mRunSimpleAnimations = false;
            this.mRunPredictiveAnimations = false;
        }

        State reset() {
            this.mTargetPosition = -1;
            if (this.mData != null) {
                this.mData.clear();
            }
            this.mItemCount = 0;
            this.mStructureChanged = false;
            return this;
        }

        public boolean isPreLayout() {
            return this.mInPreLayout;
        }

        public boolean willRunPredictiveAnimations() {
            return this.mRunPredictiveAnimations;
        }

        public boolean willRunSimpleAnimations() {
            return this.mRunSimpleAnimations;
        }

        public void remove(int resourceId) {
            if (this.mData != null) {
                this.mData.remove(resourceId);
            }
        }

        public <T> T get(int resourceId) {
            if (this.mData == null) {
                return null;
            }
            return this.mData.get(resourceId);
        }

        public void put(int resourceId, Object data) {
            if (this.mData == null) {
                this.mData = new SparseArray();
            }
            this.mData.put(resourceId, data);
        }

        public int getTargetScrollPosition() {
            return this.mTargetPosition;
        }

        public boolean hasTargetScrollPosition() {
            return this.mTargetPosition != -1;
        }

        public boolean didStructureChange() {
            return this.mStructureChanged;
        }

        public int getItemCount() {
            if (this.mInPreLayout) {
                return this.mPreviousLayoutItemCount - this.mDeletedInvisibleItemCountSincePreviousLayout;
            }
            return this.mItemCount;
        }

        public void onViewRecycled(ViewHolder holder) {
            this.mPreLayoutHolderMap.remove(holder);
            this.mPostLayoutHolderMap.remove(holder);
            if (this.mOldChangedHolders != null) {
                removeFrom(this.mOldChangedHolders, holder);
            }
        }

        public void onViewIgnored(ViewHolder holder) {
            onViewRecycled(holder);
        }

        private void removeFrom(ArrayMap<Long, ViewHolder> holderMap, ViewHolder holder) {
            for (int i = holderMap.size() - 1; i >= 0; i--) {
                if (holder == holderMap.valueAt(i)) {
                    holderMap.removeAt(i);
                    return;
                }
            }
        }

        public String toString() {
            return "State{mTargetPosition=" + this.mTargetPosition + ", mPreLayoutHolderMap=" + this.mPreLayoutHolderMap + ", mPostLayoutHolderMap=" + this.mPostLayoutHolderMap + ", mData=" + this.mData + ", mItemCount=" + this.mItemCount + ", mPreviousLayoutItemCount=" + this.mPreviousLayoutItemCount + ", mDeletedInvisibleItemCountSincePreviousLayout=" + this.mDeletedInvisibleItemCountSincePreviousLayout + ", mStructureChanged=" + this.mStructureChanged + ", mInPreLayout=" + this.mInPreLayout + ", mRunSimpleAnimations=" + this.mRunSimpleAnimations + ", mRunPredictiveAnimations=" + this.mRunPredictiveAnimations + '}';
        }
    }

    public static abstract class ViewCacheExtension {
        public abstract View getViewForPositionAndType(Recycler recycler, int i, int i2);
    }

    private class ViewFlinger implements Runnable {
        private boolean mEatRunOnAnimationRequest;
        private Interpolator mInterpolator;
        private int mLastFlingX;
        private int mLastFlingY;
        private boolean mReSchedulePostAnimationCallback;
        private ScrollerCompat mScroller;

        public ViewFlinger() {
            this.mInterpolator = RecyclerView.sQuinticInterpolator;
            this.mEatRunOnAnimationRequest = false;
            this.mReSchedulePostAnimationCallback = false;
            this.mScroller = ScrollerCompat.create(RecyclerView.this.getContext(), RecyclerView.sQuinticInterpolator);
        }

        public void run() {
            disableRunOnAnimationRequests();
            RecyclerView.this.consumePendingUpdateOperations();
            ScrollerCompat scroller = this.mScroller;
            SmoothScroller smoothScroller = RecyclerView.this.mLayout.mSmoothScroller;
            if (scroller.computeScrollOffset()) {
                int x = scroller.getCurrX();
                int y = scroller.getCurrY();
                int dx = x - this.mLastFlingX;
                int dy = y - this.mLastFlingY;
                int hresult = 0;
                int vresult = 0;
                this.mLastFlingX = x;
                this.mLastFlingY = y;
                int overscrollX = 0;
                int overscrollY = 0;
                if (RecyclerView.this.mAdapter != null) {
                    RecyclerView.this.eatRequestLayout();
                    RecyclerView.this.mRunningLayoutOrScroll = true;
                    if (dx != 0) {
                        hresult = RecyclerView.this.mLayout.scrollHorizontallyBy(dx, RecyclerView.this.mRecycler, RecyclerView.this.mState);
                        overscrollX = dx - hresult;
                    }
                    if (dy != 0) {
                        vresult = RecyclerView.this.mLayout.scrollVerticallyBy(dy, RecyclerView.this.mRecycler, RecyclerView.this.mState);
                        overscrollY = dy - vresult;
                    }
                    if (RecyclerView.this.supportsChangeAnimations()) {
                        int count = RecyclerView.this.mChildHelper.getChildCount();
                        for (int i = 0; i < count; i++) {
                            View view = RecyclerView.this.mChildHelper.getChildAt(i);
                            ViewHolder holder = RecyclerView.this.getChildViewHolder(view);
                            if (!(holder == null || holder.mShadowingHolder == null)) {
                                View shadowingView = holder.mShadowingHolder != null ? holder.mShadowingHolder.itemView : null;
                                if (shadowingView != null) {
                                    int left = view.getLeft();
                                    int top = view.getTop();
                                    if (left != shadowingView.getLeft() || top != shadowingView.getTop()) {
                                        shadowingView.layout(left, top, shadowingView.getWidth() + left, shadowingView.getHeight() + top);
                                    }
                                }
                            }
                        }
                    }
                    if (!(smoothScroller == null || smoothScroller.isPendingInitialRun() || !smoothScroller.isRunning())) {
                        int adapterSize = RecyclerView.this.mState.getItemCount();
                        if (adapterSize == 0) {
                            smoothScroller.stop();
                        } else if (smoothScroller.getTargetPosition() >= adapterSize) {
                            smoothScroller.setTargetPosition(adapterSize - 1);
                            smoothScroller.onAnimation(dx - overscrollX, dy - overscrollY);
                        } else {
                            smoothScroller.onAnimation(dx - overscrollX, dy - overscrollY);
                        }
                    }
                    RecyclerView.this.mRunningLayoutOrScroll = false;
                    RecyclerView.this.resumeRequestLayout(false);
                }
                boolean fullyConsumedScroll = dx == hresult && dy == vresult;
                if (!RecyclerView.this.mItemDecorations.isEmpty()) {
                    RecyclerView.this.invalidate();
                }
                if (ViewCompat.getOverScrollMode(RecyclerView.this) != 2) {
                    RecyclerView.this.considerReleasingGlowsOnScroll(dx, dy);
                }
                if (!(overscrollX == 0 && overscrollY == 0)) {
                    int vel = (int) scroller.getCurrVelocity();
                    int velX = 0;
                    if (overscrollX != x) {
                        velX = overscrollX < 0 ? -vel : overscrollX > 0 ? vel : 0;
                    }
                    int velY = 0;
                    if (overscrollY != y) {
                        velY = overscrollY < 0 ? -vel : overscrollY > 0 ? vel : 0;
                    }
                    if (ViewCompat.getOverScrollMode(RecyclerView.this) != 2) {
                        RecyclerView.this.absorbGlows(velX, velY);
                    }
                    if ((velX != 0 || overscrollX == x || scroller.getFinalX() == 0) && (velY != 0 || overscrollY == y || scroller.getFinalY() == 0)) {
                        scroller.abortAnimation();
                    }
                }
                if (!(hresult == 0 && vresult == 0)) {
                    RecyclerView.this.onScrollChanged(0, 0, 0, 0);
                    if (RecyclerView.this.mScrollListener != null) {
                        RecyclerView.this.mScrollListener.onScrolled(RecyclerView.this, hresult, vresult);
                    }
                }
                if (!RecyclerView.this.awakenScrollBars()) {
                    RecyclerView.this.invalidate();
                }
                if (scroller.isFinished() || !fullyConsumedScroll) {
                    RecyclerView.this.setScrollState(0);
                } else {
                    postOnAnimation();
                }
            }
            if (smoothScroller != null && smoothScroller.isPendingInitialRun()) {
                smoothScroller.onAnimation(0, 0);
            }
            enableRunOnAnimationRequests();
        }

        private void disableRunOnAnimationRequests() {
            this.mReSchedulePostAnimationCallback = false;
            this.mEatRunOnAnimationRequest = true;
        }

        private void enableRunOnAnimationRequests() {
            this.mEatRunOnAnimationRequest = false;
            if (this.mReSchedulePostAnimationCallback) {
                postOnAnimation();
            }
        }

        void postOnAnimation() {
            if (this.mEatRunOnAnimationRequest) {
                this.mReSchedulePostAnimationCallback = true;
            } else {
                ViewCompat.postOnAnimation(RecyclerView.this, this);
            }
        }

        public void fling(int velocityX, int velocityY) {
            RecyclerView.this.setScrollState(2);
            this.mLastFlingY = 0;
            this.mLastFlingX = 0;
            this.mScroller.fling(0, 0, velocityX, velocityY, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
            postOnAnimation();
        }

        public void smoothScrollBy(int dx, int dy) {
            smoothScrollBy(dx, dy, 0, 0);
        }

        public void smoothScrollBy(int dx, int dy, int vx, int vy) {
            smoothScrollBy(dx, dy, computeScrollDuration(dx, dy, vx, vy));
        }

        private float distanceInfluenceForSnapDuration(float f) {
            return (float) Math.sin((double) ((float) (((double) (f - 0.5f)) * 0.4712389167638204d)));
        }

        private int computeScrollDuration(int dx, int dy, int vx, int vy) {
            int duration;
            int absDx = Math.abs(dx);
            int absDy = Math.abs(dy);
            boolean horizontal = absDx > absDy;
            int velocity = (int) Math.sqrt((double) ((vx * vx) + (vy * vy)));
            int delta = (int) Math.sqrt((double) ((dx * dx) + (dy * dy)));
            int containerSize = horizontal ? RecyclerView.this.getWidth() : RecyclerView.this.getHeight();
            int halfContainerSize = containerSize / 2;
            float distance = ((float) halfContainerSize) + (((float) halfContainerSize) * distanceInfluenceForSnapDuration(Math.min(1.0f, (((float) delta) * 1.0f) / ((float) containerSize))));
            if (velocity > 0) {
                duration = Math.round(Math.abs(distance / ((float) velocity)) * 1000.0f) * 4;
            } else {
                if (!horizontal) {
                    absDx = absDy;
                }
                duration = (int) (((((float) absDx) / ((float) containerSize)) + 1.0f) * 300.0f);
            }
            return Math.min(duration, RecyclerView.MAX_SCROLL_DURATION);
        }

        public void smoothScrollBy(int dx, int dy, int duration) {
            smoothScrollBy(dx, dy, duration, RecyclerView.sQuinticInterpolator);
        }

        public void smoothScrollBy(int dx, int dy, int duration, Interpolator interpolator) {
            if (this.mInterpolator != interpolator) {
                this.mInterpolator = interpolator;
                this.mScroller = ScrollerCompat.create(RecyclerView.this.getContext(), interpolator);
            }
            RecyclerView.this.setScrollState(2);
            this.mLastFlingY = 0;
            this.mLastFlingX = 0;
            this.mScroller.startScroll(0, 0, dx, dy, duration);
            postOnAnimation();
        }

        public void stop() {
            RecyclerView.this.removeCallbacks(this);
            this.mScroller.abortAnimation();
        }
    }

    public static abstract class ViewHolder {
        static final int FLAG_BOUND = 1;
        static final int FLAG_CHANGED = 64;
        static final int FLAG_IGNORE = 128;
        static final int FLAG_INVALID = 4;
        static final int FLAG_NOT_RECYCLABLE = 16;
        static final int FLAG_REMOVED = 8;
        static final int FLAG_RETURNED_FROM_SCRAP = 32;
        static final int FLAG_UPDATE = 2;
        public final View itemView;
        private int mFlags;
        private int mIsRecyclableCount;
        long mItemId;
        int mItemViewType;
        int mOldPosition;
        int mPosition;
        int mPreLayoutPosition;
        private Recycler mScrapContainer;
        ViewHolder mShadowedHolder;
        ViewHolder mShadowingHolder;

        public ViewHolder(View itemView) {
            this.mPosition = -1;
            this.mOldPosition = -1;
            this.mItemId = -1;
            this.mItemViewType = -1;
            this.mPreLayoutPosition = -1;
            this.mShadowedHolder = null;
            this.mShadowingHolder = null;
            this.mIsRecyclableCount = 0;
            this.mScrapContainer = null;
            if (itemView == null) {
                throw new IllegalArgumentException("itemView may not be null");
            }
            this.itemView = itemView;
        }

        void flagRemovedAndOffsetPosition(int mNewPosition, int offset, boolean applyToPreLayout) {
            addFlags(8);
            offsetPosition(offset, applyToPreLayout);
            this.mPosition = mNewPosition;
        }

        void offsetPosition(int offset, boolean applyToPreLayout) {
            if (this.mOldPosition == -1) {
                this.mOldPosition = this.mPosition;
            }
            if (this.mPreLayoutPosition == -1) {
                this.mPreLayoutPosition = this.mPosition;
            }
            if (applyToPreLayout) {
                this.mPreLayoutPosition += offset;
            }
            this.mPosition += offset;
            if (this.itemView.getLayoutParams() != null) {
                ((LayoutParams) this.itemView.getLayoutParams()).mInsetsDirty = true;
            }
        }

        void clearOldPosition() {
            this.mOldPosition = -1;
            this.mPreLayoutPosition = -1;
        }

        void saveOldPosition() {
            if (this.mOldPosition == -1) {
                this.mOldPosition = this.mPosition;
            }
        }

        boolean shouldIgnore() {
            return (this.mFlags & 128) != 0;
        }

        public final int getPosition() {
            return this.mPreLayoutPosition == -1 ? this.mPosition : this.mPreLayoutPosition;
        }

        public final int getOldPosition() {
            return this.mOldPosition;
        }

        public final long getItemId() {
            return this.mItemId;
        }

        public final int getItemViewType() {
            return this.mItemViewType;
        }

        boolean isScrap() {
            return this.mScrapContainer != null;
        }

        void unScrap() {
            this.mScrapContainer.unscrapView(this);
        }

        boolean wasReturnedFromScrap() {
            return (this.mFlags & 32) != 0;
        }

        void clearReturnedFromScrapFlag() {
            this.mFlags &= -33;
        }

        void stopIgnoring() {
            this.mFlags &= -129;
        }

        void setScrapContainer(Recycler recycler) {
            this.mScrapContainer = recycler;
        }

        boolean isInvalid() {
            return (this.mFlags & 4) != 0;
        }

        boolean needsUpdate() {
            return (this.mFlags & 2) != 0;
        }

        boolean isChanged() {
            return (this.mFlags & 64) != 0;
        }

        boolean isBound() {
            return (this.mFlags & 1) != 0;
        }

        public boolean isRemoved() {
            return (this.mFlags & 8) != 0;
        }

        void setFlags(int flags, int mask) {
            this.mFlags = (this.mFlags & (~mask)) | (flags & mask);
        }

        void addFlags(int flags) {
            this.mFlags |= flags;
        }

        void resetInternal() {
            this.mFlags = 0;
            this.mPosition = -1;
            this.mOldPosition = -1;
            this.mItemId = -1;
            this.mPreLayoutPosition = -1;
            this.mIsRecyclableCount = 0;
            this.mShadowedHolder = null;
            this.mShadowingHolder = null;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder("ViewHolder{" + Integer.toHexString(hashCode()) + " position=" + this.mPosition + " id=" + this.mItemId + ", oldPos=" + this.mOldPosition + ", pLpos:" + this.mPreLayoutPosition);
            if (isScrap()) {
                sb.append(" scrap");
            }
            if (isInvalid()) {
                sb.append(" invalid");
            }
            if (!isBound()) {
                sb.append(" unbound");
            }
            if (needsUpdate()) {
                sb.append(" update");
            }
            if (isRemoved()) {
                sb.append(" removed");
            }
            if (shouldIgnore()) {
                sb.append(" ignored");
            }
            if (isChanged()) {
                sb.append(" changed");
            }
            if (!isRecyclable()) {
                sb.append(" not recyclable(").append(this.mIsRecyclableCount).append(")");
            }
            if (this.itemView.getParent() == null) {
                sb.append(" no parent");
            }
            sb.append("}");
            return sb.toString();
        }

        public final void setIsRecyclable(boolean recyclable) {
            this.mIsRecyclableCount = recyclable ? this.mIsRecyclableCount - 1 : this.mIsRecyclableCount + 1;
            if (this.mIsRecyclableCount < 0) {
                this.mIsRecyclableCount = 0;
                Log.e("View", "isRecyclable decremented below 0: unmatched pair of setIsRecyable() calls for " + this);
            } else if (!recyclable && this.mIsRecyclableCount == 1) {
                this.mFlags |= 16;
            } else if (recyclable && this.mIsRecyclableCount == 0) {
                this.mFlags &= -17;
            }
        }

        public final boolean isRecyclable() {
            if ((this.mFlags & 16) != 0 || ViewCompat.hasTransientState(this.itemView)) {
                return false;
            }
            return true;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.support.v7.widget.RecyclerView.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.support.v7.widget.RecyclerView.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v7.widget.RecyclerView.<clinit>():void");
    }

    public RecyclerView(Context context) {
        this(context, null);
    }

    public RecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecyclerView(Context context, AttributeSet attrs, int defStyle) {
        boolean z;
        boolean z2 = false;
        super(context, attrs, defStyle);
        this.mObserver = new RecyclerViewDataObserver(this, null);
        this.mRecycler = new Recycler();
        this.mDisappearingViewsInLayoutPass = new ArrayList();
        this.mUpdateChildViewsRunnable = new Runnable() {
            public void run() {
                if (RecyclerView.this.mAdapterHelper.hasPendingUpdates() && RecyclerView.this.mFirstLayoutComplete) {
                    if (RecyclerView.this.mDataSetHasChangedAfterLayout) {
                        RecyclerView.this.dispatchLayout();
                    } else {
                        RecyclerView.this.eatRequestLayout();
                        RecyclerView.this.mAdapterHelper.preProcess();
                        if (!RecyclerView.this.mLayoutRequestEaten) {
                            RecyclerView.this.rebindUpdatedViewHolders();
                        }
                        RecyclerView.this.resumeRequestLayout(true);
                    }
                }
            }
        };
        this.mTempRect = new Rect();
        this.mItemDecorations = new ArrayList();
        this.mOnItemTouchListeners = new ArrayList();
        this.mDataSetHasChangedAfterLayout = false;
        this.mRunningLayoutOrScroll = false;
        this.mItemAnimator = new DefaultItemAnimator();
        this.mScrollState = 0;
        this.mScrollPointerId = -1;
        this.mViewFlinger = new ViewFlinger();
        this.mState = new State();
        this.mItemsAddedOrRemoved = false;
        this.mItemsChanged = false;
        this.mItemAnimatorListener = new ItemAnimatorRestoreListener(this, null);
        this.mPostedAnimatorRunner = false;
        this.mItemAnimatorRunner = new Runnable() {
            public void run() {
                if (RecyclerView.this.mItemAnimator != null) {
                    RecyclerView.this.mItemAnimator.runPendingAnimations();
                }
                RecyclerView.this.mPostedAnimatorRunner = false;
            }
        };
        if (VERSION.SDK_INT >= 16) {
            z = true;
        } else {
            z = false;
        }
        this.mPostUpdatesOnAnimation = z;
        ViewConfiguration vc = ViewConfiguration.get(context);
        this.mTouchSlop = vc.getScaledTouchSlop();
        this.mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
        this.mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        if (ViewCompat.getOverScrollMode(this) == 2) {
            z2 = true;
        }
        setWillNotDraw(z2);
        this.mItemAnimator.setListener(this.mItemAnimatorListener);
        initAdapterManager();
        initChildrenHelper();
        if (ViewCompat.getImportantForAccessibility(this) == 0) {
            ViewCompat.setImportantForAccessibility(this, 1);
        }
        this.mAccessibilityManager = (AccessibilityManager) getContext().getSystemService("accessibility");
        setAccessibilityDelegateCompat(new RecyclerViewAccessibilityDelegate(this));
    }

    public RecyclerViewAccessibilityDelegate getCompatAccessibilityDelegate() {
        return this.mAccessibilityDelegate;
    }

    public void setAccessibilityDelegateCompat(RecyclerViewAccessibilityDelegate accessibilityDelegate) {
        this.mAccessibilityDelegate = accessibilityDelegate;
        ViewCompat.setAccessibilityDelegate(this, this.mAccessibilityDelegate);
    }

    private void initChildrenHelper() {
        this.mChildHelper = new ChildHelper(new Callback() {
            public int getChildCount() {
                return RecyclerView.this.getChildCount();
            }

            public void addView(View child, int index) {
                RecyclerView.this.addView(child, index);
                RecyclerView.this.dispatchChildAttached(child);
            }

            public int indexOfChild(View view) {
                return RecyclerView.this.indexOfChild(view);
            }

            public void removeViewAt(int index) {
                View child = RecyclerView.this.getChildAt(index);
                if (child != null) {
                    RecyclerView.this.dispatchChildDetached(child);
                }
                RecyclerView.this.removeViewAt(index);
            }

            public View getChildAt(int offset) {
                return RecyclerView.this.getChildAt(offset);
            }

            public void removeAllViews() {
                int count = getChildCount();
                for (int i = 0; i < count; i++) {
                    RecyclerView.this.dispatchChildDetached(getChildAt(i));
                }
                RecyclerView.this.removeAllViews();
            }

            public ViewHolder getChildViewHolder(View view) {
                return RecyclerView.getChildViewHolderInt(view);
            }

            public void attachViewToParent(View child, int index, android.view.ViewGroup.LayoutParams layoutParams) {
                RecyclerView.this.attachViewToParent(child, index, layoutParams);
            }

            public void detachViewFromParent(int offset) {
                RecyclerView.this.detachViewFromParent(offset);
            }
        });
    }

    void initAdapterManager() {
        this.mAdapterHelper = new AdapterHelper(new Callback() {
            public ViewHolder findViewHolder(int position) {
                return RecyclerView.this.findViewHolderForPosition(position, true);
            }

            public void offsetPositionsForRemovingInvisible(int start, int count) {
                RecyclerView.this.offsetPositionRecordsForRemove(start, count, true);
                RecyclerView.this.mItemsAddedOrRemoved = true;
                State state = RecyclerView.this.mState;
                state.mDeletedInvisibleItemCountSincePreviousLayout = state.mDeletedInvisibleItemCountSincePreviousLayout + count;
            }

            public void offsetPositionsForRemovingLaidOutOrNewView(int positionStart, int itemCount) {
                RecyclerView.this.offsetPositionRecordsForRemove(positionStart, itemCount, false);
                RecyclerView.this.mItemsAddedOrRemoved = true;
            }

            public void markViewHoldersUpdated(int positionStart, int itemCount) {
                RecyclerView.this.viewRangeUpdate(positionStart, itemCount);
                RecyclerView.this.mItemsChanged = true;
            }

            public void onDispatchFirstPass(UpdateOp op) {
                dispatchUpdate(op);
            }

            void dispatchUpdate(UpdateOp op) {
                switch (op.cmd) {
                    case 0:
                        RecyclerView.this.mLayout.onItemsAdded(RecyclerView.this, op.positionStart, op.itemCount);
                        return;
                    case 1:
                        RecyclerView.this.mLayout.onItemsRemoved(RecyclerView.this, op.positionStart, op.itemCount);
                        return;
                    case 2:
                        RecyclerView.this.mLayout.onItemsUpdated(RecyclerView.this, op.positionStart, op.itemCount);
                        return;
                    case 3:
                        RecyclerView.this.mLayout.onItemsMoved(RecyclerView.this, op.positionStart, op.itemCount, 1);
                        return;
                    default:
                        return;
                }
            }

            public void onDispatchSecondPass(UpdateOp op) {
                dispatchUpdate(op);
            }

            public void offsetPositionsForAdd(int positionStart, int itemCount) {
                RecyclerView.this.offsetPositionRecordsForInsert(positionStart, itemCount);
                RecyclerView.this.mItemsAddedOrRemoved = true;
            }

            public void offsetPositionsForMove(int from, int to) {
                RecyclerView.this.offsetPositionRecordsForMove(from, to);
                RecyclerView.this.mItemsAddedOrRemoved = true;
            }
        });
    }

    public void setHasFixedSize(boolean hasFixedSize) {
        this.mHasFixedSize = hasFixedSize;
    }

    public boolean hasFixedSize() {
        return this.mHasFixedSize;
    }

    public void setClipToPadding(boolean clipToPadding) {
        if (clipToPadding != this.mClipToPadding) {
            invalidateGlows();
        }
        this.mClipToPadding = clipToPadding;
        super.setClipToPadding(clipToPadding);
        if (this.mFirstLayoutComplete) {
            requestLayout();
        }
    }

    public void swapAdapter(Adapter adapter, boolean removeAndRecycleExistingViews) {
        setAdapterInternal(adapter, true, removeAndRecycleExistingViews);
        this.mDataSetHasChangedAfterLayout = true;
        requestLayout();
    }

    public void setAdapter(Adapter adapter) {
        setAdapterInternal(adapter, false, true);
        requestLayout();
    }

    private void setAdapterInternal(Adapter adapter, boolean compatibleWithPrevious, boolean removeAndRecycleViews) {
        if (this.mAdapter != null) {
            this.mAdapter.unregisterAdapterDataObserver(this.mObserver);
        }
        if (!compatibleWithPrevious || removeAndRecycleViews) {
            if (this.mItemAnimator != null) {
                this.mItemAnimator.endAnimations();
            }
            if (this.mLayout != null) {
                this.mLayout.removeAndRecycleAllViews(this.mRecycler);
                this.mLayout.removeAndRecycleScrapInt(this.mRecycler, true);
            }
        }
        this.mAdapterHelper.reset();
        Adapter oldAdapter = this.mAdapter;
        this.mAdapter = adapter;
        if (adapter != null) {
            adapter.registerAdapterDataObserver(this.mObserver);
        }
        if (this.mLayout != null) {
            this.mLayout.onAdapterChanged(oldAdapter, this.mAdapter);
        }
        this.mRecycler.onAdapterChanged(oldAdapter, this.mAdapter, compatibleWithPrevious);
        this.mState.mStructureChanged = true;
        markKnownViewsInvalid();
    }

    public Adapter getAdapter() {
        return this.mAdapter;
    }

    public void setRecyclerListener(RecyclerListener listener) {
        this.mRecyclerListener = listener;
    }

    public void setLayoutManager(LayoutManager layout) {
        if (layout != this.mLayout) {
            if (this.mLayout != null) {
                if (this.mIsAttached) {
                    this.mLayout.onDetachedFromWindow(this, this.mRecycler);
                }
                this.mLayout.setRecyclerView(null);
            }
            this.mRecycler.clear();
            this.mChildHelper.removeAllViewsUnfiltered();
            this.mLayout = layout;
            if (layout != null) {
                if (layout.mRecyclerView != null) {
                    throw new IllegalArgumentException("LayoutManager " + layout + " is already attached to a RecyclerView: " + layout.mRecyclerView);
                }
                this.mLayout.setRecyclerView(this);
                if (this.mIsAttached) {
                    this.mLayout.onAttachedToWindow(this);
                }
            }
            requestLayout();
        }
    }

    protected Parcelable onSaveInstanceState() {
        SavedState state = new SavedState(super.onSaveInstanceState());
        if (this.mPendingSavedState != null) {
            state.copyFrom(this.mPendingSavedState);
        } else if (this.mLayout != null) {
            state.mLayoutState = this.mLayout.onSaveInstanceState();
        } else {
            state.mLayoutState = null;
        }
        return state;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        this.mPendingSavedState = (SavedState) state;
        super.onRestoreInstanceState(this.mPendingSavedState.getSuperState());
        if (this.mLayout != null && this.mPendingSavedState.mLayoutState != null) {
            this.mLayout.onRestoreInstanceState(this.mPendingSavedState.mLayoutState);
        }
    }

    private void addAnimatingView(View view) {
        boolean alreadyParented = view.getParent() == this;
        this.mRecycler.unscrapView(getChildViewHolder(view));
        if (alreadyParented) {
            this.mChildHelper.hide(view);
        } else {
            this.mChildHelper.addView(view, true);
        }
    }

    private void removeAnimatingView(View view) {
        eatRequestLayout();
        if (this.mChildHelper.removeViewIfHidden(view)) {
            ViewHolder viewHolder = getChildViewHolderInt(view);
            this.mRecycler.unscrapView(viewHolder);
            this.mRecycler.recycleViewHolderInternal(viewHolder);
        }
        resumeRequestLayout(false);
    }

    public LayoutManager getLayoutManager() {
        return this.mLayout;
    }

    public RecycledViewPool getRecycledViewPool() {
        return this.mRecycler.getRecycledViewPool();
    }

    public void setRecycledViewPool(RecycledViewPool pool) {
        this.mRecycler.setRecycledViewPool(pool);
    }

    public void setViewCacheExtension(ViewCacheExtension extension) {
        this.mRecycler.setViewCacheExtension(extension);
    }

    public void setItemViewCacheSize(int size) {
        this.mRecycler.setViewCacheSize(size);
    }

    public int getScrollState() {
        return this.mScrollState;
    }

    private void setScrollState(int state) {
        if (state != this.mScrollState) {
            this.mScrollState = state;
            if (state != 2) {
                stopScrollersInternal();
            }
            if (this.mScrollListener != null) {
                this.mScrollListener.onScrollStateChanged(this, state);
            }
            this.mLayout.onScrollStateChanged(state);
        }
    }

    public void addItemDecoration(ItemDecoration decor, int index) {
        if (this.mLayout != null) {
            this.mLayout.assertNotInLayoutOrScroll("Cannot add item decoration during a scroll  or layout");
        }
        if (this.mItemDecorations.isEmpty()) {
            setWillNotDraw(false);
        }
        if (index < 0) {
            this.mItemDecorations.add(decor);
        } else {
            this.mItemDecorations.add(index, decor);
        }
        markItemDecorInsetsDirty();
        requestLayout();
    }

    public void addItemDecoration(ItemDecoration decor) {
        addItemDecoration(decor, -1);
    }

    public void removeItemDecoration(ItemDecoration decor) {
        if (this.mLayout != null) {
            this.mLayout.assertNotInLayoutOrScroll("Cannot remove item decoration during a scroll  or layout");
        }
        this.mItemDecorations.remove(decor);
        if (this.mItemDecorations.isEmpty()) {
            setWillNotDraw(ViewCompat.getOverScrollMode(this) == 2);
        }
        markItemDecorInsetsDirty();
        requestLayout();
    }

    public void setOnScrollListener(OnScrollListener listener) {
        this.mScrollListener = listener;
    }

    public void scrollToPosition(int position) {
        stopScroll();
        this.mLayout.scrollToPosition(position);
        awakenScrollBars();
    }

    public void smoothScrollToPosition(int position) {
        this.mLayout.smoothScrollToPosition(this, this.mState, position);
    }

    public void scrollTo(int x, int y) {
        throw new UnsupportedOperationException("RecyclerView does not support scrolling to an absolute position.");
    }

    public void scrollBy(int x, int y) {
        if (this.mLayout == null) {
            throw new IllegalStateException("Cannot scroll without a LayoutManager set. Call setLayoutManager with a non-null argument.");
        }
        boolean canScrollHorizontal = this.mLayout.canScrollHorizontally();
        boolean canScrollVertical = this.mLayout.canScrollVertically();
        if (canScrollHorizontal || canScrollVertical) {
            if (!canScrollHorizontal) {
                x = 0;
            }
            if (!canScrollVertical) {
                y = 0;
            }
            scrollByInternal(x, y);
        }
    }

    private void consumePendingUpdateOperations() {
        if (this.mAdapterHelper.hasPendingUpdates()) {
            this.mUpdateChildViewsRunnable.run();
        }
    }

    void scrollByInternal(int x, int y) {
        int overscrollX = 0;
        int overscrollY = 0;
        int hresult = 0;
        int vresult = 0;
        consumePendingUpdateOperations();
        if (this.mAdapter != null) {
            eatRequestLayout();
            this.mRunningLayoutOrScroll = true;
            if (x != 0) {
                hresult = this.mLayout.scrollHorizontallyBy(x, this.mRecycler, this.mState);
                overscrollX = x - hresult;
            }
            if (y != 0) {
                vresult = this.mLayout.scrollVerticallyBy(y, this.mRecycler, this.mState);
                overscrollY = y - vresult;
            }
            if (supportsChangeAnimations()) {
                int count = this.mChildHelper.getChildCount();
                for (int i = 0; i < count; i++) {
                    View view = this.mChildHelper.getChildAt(i);
                    ViewHolder holder = getChildViewHolder(view);
                    if (!(holder == null || holder.mShadowingHolder == null)) {
                        ViewHolder shadowingHolder = holder.mShadowingHolder;
                        View shadowingView = shadowingHolder != null ? shadowingHolder.itemView : null;
                        if (shadowingView != null) {
                            int left = view.getLeft();
                            int top = view.getTop();
                            if (left != shadowingView.getLeft() || top != shadowingView.getTop()) {
                                shadowingView.layout(left, top, shadowingView.getWidth() + left, shadowingView.getHeight() + top);
                            }
                        }
                    }
                }
            }
            this.mRunningLayoutOrScroll = false;
            resumeRequestLayout(false);
        }
        if (!this.mItemDecorations.isEmpty()) {
            invalidate();
        }
        if (ViewCompat.getOverScrollMode(this) != 2) {
            considerReleasingGlowsOnScroll(x, y);
            pullGlows(overscrollX, overscrollY);
        }
        if (!(hresult == 0 && vresult == 0)) {
            onScrollChanged(0, 0, 0, 0);
            if (this.mScrollListener != null) {
                this.mScrollListener.onScrolled(this, hresult, vresult);
            }
        }
        if (!awakenScrollBars()) {
            invalidate();
        }
    }

    protected int computeHorizontalScrollOffset() {
        if (this.mLayout.canScrollHorizontally()) {
            return this.mLayout.computeHorizontalScrollOffset(this.mState);
        }
        return 0;
    }

    protected int computeHorizontalScrollExtent() {
        return this.mLayout.canScrollHorizontally() ? this.mLayout.computeHorizontalScrollExtent(this.mState) : 0;
    }

    protected int computeHorizontalScrollRange() {
        return this.mLayout.canScrollHorizontally() ? this.mLayout.computeHorizontalScrollRange(this.mState) : 0;
    }

    protected int computeVerticalScrollOffset() {
        return this.mLayout.canScrollVertically() ? this.mLayout.computeVerticalScrollOffset(this.mState) : 0;
    }

    protected int computeVerticalScrollExtent() {
        return this.mLayout.canScrollVertically() ? this.mLayout.computeVerticalScrollExtent(this.mState) : 0;
    }

    protected int computeVerticalScrollRange() {
        return this.mLayout.canScrollVertically() ? this.mLayout.computeVerticalScrollRange(this.mState) : 0;
    }

    void eatRequestLayout() {
        if (!this.mEatRequestLayout) {
            this.mEatRequestLayout = true;
            this.mLayoutRequestEaten = false;
        }
    }

    void resumeRequestLayout(boolean performLayoutChildren) {
        if (this.mEatRequestLayout) {
            if (performLayoutChildren && this.mLayoutRequestEaten && this.mLayout != null && this.mAdapter != null) {
                dispatchLayout();
            }
            this.mEatRequestLayout = false;
            this.mLayoutRequestEaten = false;
        }
    }

    public void smoothScrollBy(int dx, int dy) {
        if (dx != 0 || dy != 0) {
            this.mViewFlinger.smoothScrollBy(dx, dy);
        }
    }

    public boolean fling(int velocityX, int velocityY) {
        if (Math.abs(velocityX) < this.mMinFlingVelocity) {
            velocityX = 0;
        }
        if (Math.abs(velocityY) < this.mMinFlingVelocity) {
            velocityY = 0;
        }
        velocityX = Math.max(-this.mMaxFlingVelocity, Math.min(velocityX, this.mMaxFlingVelocity));
        velocityY = Math.max(-this.mMaxFlingVelocity, Math.min(velocityY, this.mMaxFlingVelocity));
        if (velocityX == 0 && velocityY == 0) {
            return false;
        }
        this.mViewFlinger.fling(velocityX, velocityY);
        return true;
    }

    public void stopScroll() {
        setScrollState(0);
        stopScrollersInternal();
    }

    private void stopScrollersInternal() {
        this.mViewFlinger.stop();
        this.mLayout.stopSmoothScroller();
    }

    private void pullGlows(int overscrollX, int overscrollY) {
        if (overscrollX < 0) {
            ensureLeftGlow();
            this.mLeftGlow.onPull(((float) (-overscrollX)) / ((float) getWidth()));
        } else if (overscrollX > 0) {
            ensureRightGlow();
            this.mRightGlow.onPull(((float) overscrollX) / ((float) getWidth()));
        }
        if (overscrollY < 0) {
            ensureTopGlow();
            this.mTopGlow.onPull(((float) (-overscrollY)) / ((float) getHeight()));
        } else if (overscrollY > 0) {
            ensureBottomGlow();
            this.mBottomGlow.onPull(((float) overscrollY) / ((float) getHeight()));
        }
        if (overscrollX != 0 || overscrollY != 0) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private void releaseGlows() {
        int needsInvalidate = 0;
        if (this.mLeftGlow != null) {
            needsInvalidate = this.mLeftGlow.onRelease();
        }
        if (this.mTopGlow != null) {
            needsInvalidate |= this.mTopGlow.onRelease();
        }
        if (this.mRightGlow != null) {
            needsInvalidate |= this.mRightGlow.onRelease();
        }
        if (this.mBottomGlow != null) {
            needsInvalidate |= this.mBottomGlow.onRelease();
        }
        if (needsInvalidate != 0) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private void considerReleasingGlowsOnScroll(int dx, int dy) {
        int needsInvalidate = 0;
        if (!(this.mLeftGlow == null || this.mLeftGlow.isFinished() || dx <= 0)) {
            needsInvalidate = this.mLeftGlow.onRelease();
        }
        if (!(this.mRightGlow == null || this.mRightGlow.isFinished() || dx >= 0)) {
            needsInvalidate |= this.mRightGlow.onRelease();
        }
        if (!(this.mTopGlow == null || this.mTopGlow.isFinished() || dy <= 0)) {
            needsInvalidate |= this.mTopGlow.onRelease();
        }
        if (!(this.mBottomGlow == null || this.mBottomGlow.isFinished() || dy >= 0)) {
            needsInvalidate |= this.mBottomGlow.onRelease();
        }
        if (needsInvalidate != 0) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    void absorbGlows(int velocityX, int velocityY) {
        if (velocityX < 0) {
            ensureLeftGlow();
            this.mLeftGlow.onAbsorb(-velocityX);
        } else if (velocityX > 0) {
            ensureRightGlow();
            this.mRightGlow.onAbsorb(velocityX);
        }
        if (velocityY < 0) {
            ensureTopGlow();
            this.mTopGlow.onAbsorb(-velocityY);
        } else if (velocityY > 0) {
            ensureBottomGlow();
            this.mBottomGlow.onAbsorb(velocityY);
        }
        if (velocityX != 0 || velocityY != 0) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    void ensureLeftGlow() {
        if (this.mLeftGlow == null) {
            this.mLeftGlow = new EdgeEffectCompat(getContext());
            if (this.mClipToPadding) {
                this.mLeftGlow.setSize((getMeasuredHeight() - getPaddingTop()) - getPaddingBottom(), (getMeasuredWidth() - getPaddingLeft()) - getPaddingRight());
            } else {
                this.mLeftGlow.setSize(getMeasuredHeight(), getMeasuredWidth());
            }
        }
    }

    void ensureRightGlow() {
        if (this.mRightGlow == null) {
            this.mRightGlow = new EdgeEffectCompat(getContext());
            if (this.mClipToPadding) {
                this.mRightGlow.setSize((getMeasuredHeight() - getPaddingTop()) - getPaddingBottom(), (getMeasuredWidth() - getPaddingLeft()) - getPaddingRight());
            } else {
                this.mRightGlow.setSize(getMeasuredHeight(), getMeasuredWidth());
            }
        }
    }

    void ensureTopGlow() {
        if (this.mTopGlow == null) {
            this.mTopGlow = new EdgeEffectCompat(getContext());
            if (this.mClipToPadding) {
                this.mTopGlow.setSize((getMeasuredWidth() - getPaddingLeft()) - getPaddingRight(), (getMeasuredHeight() - getPaddingTop()) - getPaddingBottom());
            } else {
                this.mTopGlow.setSize(getMeasuredWidth(), getMeasuredHeight());
            }
        }
    }

    void ensureBottomGlow() {
        if (this.mBottomGlow == null) {
            this.mBottomGlow = new EdgeEffectCompat(getContext());
            if (this.mClipToPadding) {
                this.mBottomGlow.setSize((getMeasuredWidth() - getPaddingLeft()) - getPaddingRight(), (getMeasuredHeight() - getPaddingTop()) - getPaddingBottom());
            } else {
                this.mBottomGlow.setSize(getMeasuredWidth(), getMeasuredHeight());
            }
        }
    }

    void invalidateGlows() {
        this.mBottomGlow = null;
        this.mTopGlow = null;
        this.mRightGlow = null;
        this.mLeftGlow = null;
    }

    public View focusSearch(View focused, int direction) {
        View result = this.mLayout.onInterceptFocusSearch(focused, direction);
        if (result != null) {
            return result;
        }
        result = FocusFinder.getInstance().findNextFocus(this, focused, direction);
        if (result == null && this.mAdapter != null) {
            eatRequestLayout();
            result = this.mLayout.onFocusSearchFailed(focused, direction, this.mRecycler, this.mState);
            resumeRequestLayout(false);
        }
        if (result == null) {
            result = super.focusSearch(focused, direction);
        }
        return result;
    }

    public void requestChildFocus(View child, View focused) {
        boolean z = false;
        if (!(this.mLayout.onRequestChildFocus(this, this.mState, child, focused) || focused == null)) {
            this.mTempRect.set(0, 0, focused.getWidth(), focused.getHeight());
            offsetDescendantRectToMyCoords(focused, this.mTempRect);
            offsetRectIntoDescendantCoords(child, this.mTempRect);
            Rect rect = this.mTempRect;
            if (!this.mFirstLayoutComplete) {
                z = true;
            }
            requestChildRectangleOnScreen(child, rect, z);
        }
        super.requestChildFocus(child, focused);
    }

    public boolean requestChildRectangleOnScreen(View child, Rect rect, boolean immediate) {
        return this.mLayout.requestChildRectangleOnScreen(this, child, rect, immediate);
    }

    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        if (!this.mLayout.onAddFocusables(this, views, direction, focusableMode)) {
            super.addFocusables(views, direction, focusableMode);
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mIsAttached = true;
        this.mFirstLayoutComplete = false;
        if (this.mLayout != null) {
            this.mLayout.onAttachedToWindow(this);
        }
        this.mPostedAnimatorRunner = false;
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mItemAnimator != null) {
            this.mItemAnimator.endAnimations();
        }
        this.mFirstLayoutComplete = false;
        stopScroll();
        this.mIsAttached = false;
        if (this.mLayout != null) {
            this.mLayout.onDetachedFromWindow(this, this.mRecycler);
        }
        removeCallbacks(this.mItemAnimatorRunner);
    }

    void assertInLayoutOrScroll(String message) {
        if (!this.mRunningLayoutOrScroll) {
            if (message == null) {
                throw new IllegalStateException("Cannot call this method unless RecyclerView is computing a layout or scrolling");
            }
            throw new IllegalStateException(message);
        }
    }

    void assertNotInLayoutOrScroll(String message) {
        if (!this.mRunningLayoutOrScroll) {
            return;
        }
        if (message == null) {
            throw new IllegalStateException("Cannot call this method while RecyclerView is computing a layout or scrolling");
        }
        throw new IllegalStateException(message);
    }

    public void addOnItemTouchListener(OnItemTouchListener listener) {
        this.mOnItemTouchListeners.add(listener);
    }

    public void removeOnItemTouchListener(OnItemTouchListener listener) {
        this.mOnItemTouchListeners.remove(listener);
        if (this.mActiveOnItemTouchListener == listener) {
            this.mActiveOnItemTouchListener = null;
        }
    }

    private boolean dispatchOnItemTouchIntercept(MotionEvent e) {
        int action = e.getAction();
        if (action == 3 || action == 0) {
            this.mActiveOnItemTouchListener = null;
        }
        int listenerCount = this.mOnItemTouchListeners.size();
        int i = 0;
        while (i < listenerCount) {
            OnItemTouchListener listener = (OnItemTouchListener) this.mOnItemTouchListeners.get(i);
            if (!listener.onInterceptTouchEvent(this, e) || action == 3) {
                i++;
            } else {
                this.mActiveOnItemTouchListener = listener;
                return true;
            }
        }
        return false;
    }

    private boolean dispatchOnItemTouch(MotionEvent e) {
        int action = e.getAction();
        if (this.mActiveOnItemTouchListener != null) {
            if (action == 0) {
                this.mActiveOnItemTouchListener = null;
            } else {
                this.mActiveOnItemTouchListener.onTouchEvent(this, e);
                if (action == 3 || action == 1) {
                    this.mActiveOnItemTouchListener = null;
                }
                return true;
            }
        }
        if (action != 0) {
            int listenerCount = this.mOnItemTouchListeners.size();
            for (int i = 0; i < listenerCount; i++) {
                OnItemTouchListener listener = (OnItemTouchListener) this.mOnItemTouchListeners.get(i);
                if (listener.onInterceptTouchEvent(this, e)) {
                    this.mActiveOnItemTouchListener = listener;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean onInterceptTouchEvent(MotionEvent e) {
        if (dispatchOnItemTouchIntercept(e)) {
            cancelTouch();
            return true;
        }
        boolean z;
        boolean canScrollHorizontally = this.mLayout.canScrollHorizontally();
        boolean canScrollVertically = this.mLayout.canScrollVertically();
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(e);
        int action = MotionEventCompat.getActionMasked(e);
        int actionIndex = MotionEventCompat.getActionIndex(e);
        int x;
        switch (action) {
            case 0:
                this.mScrollPointerId = MotionEventCompat.getPointerId(e, 0);
                x = (int) (e.getX() + 0.5f);
                this.mLastTouchX = x;
                this.mInitialTouchX = x;
                x = (int) (e.getY() + 0.5f);
                this.mLastTouchY = x;
                this.mInitialTouchY = x;
                if (this.mScrollState == 2) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    setScrollState(1);
                    break;
                }
                break;
            case 1:
                this.mVelocityTracker.clear();
                break;
            case 2:
                int index = MotionEventCompat.findPointerIndex(e, this.mScrollPointerId);
                if (index >= 0) {
                    int x2 = (int) (MotionEventCompat.getX(e, index) + 0.5f);
                    int y = (int) (MotionEventCompat.getY(e, index) + 0.5f);
                    if (this.mScrollState != 1) {
                        int dx = x2 - this.mInitialTouchX;
                        int dy = y - this.mInitialTouchY;
                        boolean startScroll = false;
                        if (canScrollHorizontally && Math.abs(dx) > this.mTouchSlop) {
                            this.mLastTouchX = ((dx < 0 ? -1 : 1) * this.mTouchSlop) + this.mInitialTouchX;
                            startScroll = true;
                        }
                        if (canScrollVertically && Math.abs(dy) > this.mTouchSlop) {
                            this.mLastTouchY = ((dy < 0 ? -1 : 1) * this.mTouchSlop) + this.mInitialTouchY;
                            startScroll = true;
                        }
                        if (startScroll) {
                            getParent().requestDisallowInterceptTouchEvent(true);
                            setScrollState(1);
                            break;
                        }
                    }
                }
                Log.e(TAG, "Error processing scroll; pointer index for id " + this.mScrollPointerId + " not found. Did any MotionEvents get skipped?");
                return false;
                break;
            case 3:
                cancelTouch();
                break;
            case 5:
                this.mScrollPointerId = MotionEventCompat.getPointerId(e, actionIndex);
                x = (int) (MotionEventCompat.getX(e, actionIndex) + 0.5f);
                this.mLastTouchX = x;
                this.mInitialTouchX = x;
                x = (int) (MotionEventCompat.getY(e, actionIndex) + 0.5f);
                this.mLastTouchY = x;
                this.mInitialTouchY = x;
                break;
            case 6:
                onPointerUp(e);
                break;
        }
        if (this.mScrollState == 1) {
            z = true;
        } else {
            z = false;
        }
        return z;
    }

    public boolean onTouchEvent(MotionEvent e) {
        if (dispatchOnItemTouch(e)) {
            cancelTouch();
            return true;
        }
        boolean canScrollHorizontally = this.mLayout.canScrollHorizontally();
        boolean canScrollVertically = this.mLayout.canScrollVertically();
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(e);
        int action = MotionEventCompat.getActionMasked(e);
        int actionIndex = MotionEventCompat.getActionIndex(e);
        int x;
        switch (action) {
            case 0:
                this.mScrollPointerId = MotionEventCompat.getPointerId(e, 0);
                x = (int) (e.getX() + 0.5f);
                this.mLastTouchX = x;
                this.mInitialTouchX = x;
                x = (int) (e.getY() + 0.5f);
                this.mLastTouchY = x;
                this.mInitialTouchY = x;
                break;
            case 1:
                this.mVelocityTracker.computeCurrentVelocity(1000, (float) this.mMaxFlingVelocity);
                float xvel = canScrollHorizontally ? -VelocityTrackerCompat.getXVelocity(this.mVelocityTracker, this.mScrollPointerId) : 0.0f;
                float yvel = canScrollVertically ? -VelocityTrackerCompat.getYVelocity(this.mVelocityTracker, this.mScrollPointerId) : 0.0f;
                boolean fling = (xvel == 0.0f && yvel == 0.0f) ? false : fling((int) xvel, (int) yvel);
                if (!fling) {
                    setScrollState(0);
                }
                this.mVelocityTracker.clear();
                releaseGlows();
                break;
            case 2:
                int index = MotionEventCompat.findPointerIndex(e, this.mScrollPointerId);
                if (index >= 0) {
                    int x2 = (int) (MotionEventCompat.getX(e, index) + 0.5f);
                    int y = (int) (MotionEventCompat.getY(e, index) + 0.5f);
                    if (this.mScrollState != 1) {
                        int dx = x2 - this.mInitialTouchX;
                        int dy = y - this.mInitialTouchY;
                        boolean startScroll = false;
                        if (canScrollHorizontally && Math.abs(dx) > this.mTouchSlop) {
                            this.mLastTouchX = ((dx < 0 ? -1 : 1) * this.mTouchSlop) + this.mInitialTouchX;
                            startScroll = true;
                        }
                        if (canScrollVertically && Math.abs(dy) > this.mTouchSlop) {
                            this.mLastTouchY = ((dy < 0 ? -1 : 1) * this.mTouchSlop) + this.mInitialTouchY;
                            startScroll = true;
                        }
                        if (startScroll) {
                            getParent().requestDisallowInterceptTouchEvent(true);
                            setScrollState(1);
                        }
                    }
                    if (this.mScrollState == 1) {
                        scrollByInternal(canScrollHorizontally ? -(x2 - this.mLastTouchX) : 0, canScrollVertically ? -(y - this.mLastTouchY) : 0);
                    }
                    this.mLastTouchX = x2;
                    this.mLastTouchY = y;
                    break;
                }
                Log.e(TAG, "Error processing scroll; pointer index for id " + this.mScrollPointerId + " not found. Did any MotionEvents get skipped?");
                return false;
            case 3:
                cancelTouch();
                break;
            case 5:
                this.mScrollPointerId = MotionEventCompat.getPointerId(e, actionIndex);
                x = (int) (MotionEventCompat.getX(e, actionIndex) + 0.5f);
                this.mLastTouchX = x;
                this.mInitialTouchX = x;
                x = (int) (MotionEventCompat.getY(e, actionIndex) + 0.5f);
                this.mLastTouchY = x;
                this.mInitialTouchY = x;
                break;
            case 6:
                onPointerUp(e);
                break;
        }
        return true;
    }

    private void cancelTouch() {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.clear();
        }
        releaseGlows();
        setScrollState(0);
    }

    private void onPointerUp(MotionEvent e) {
        int newIndex = 0;
        int actionIndex = MotionEventCompat.getActionIndex(e);
        if (MotionEventCompat.getPointerId(e, actionIndex) == this.mScrollPointerId) {
            if (actionIndex == 0) {
                newIndex = 1;
            }
            this.mScrollPointerId = MotionEventCompat.getPointerId(e, newIndex);
            int x = (int) (MotionEventCompat.getX(e, newIndex) + 0.5f);
            this.mLastTouchX = x;
            this.mInitialTouchX = x;
            x = (int) (MotionEventCompat.getY(e, newIndex) + 0.5f);
            this.mLastTouchY = x;
            this.mInitialTouchY = x;
        }
    }

    protected void onMeasure(int widthSpec, int heightSpec) {
        if (this.mAdapterUpdateDuringMeasure) {
            eatRequestLayout();
            processAdapterUpdatesAndSetAnimationFlags();
            if (this.mState.mRunPredictiveAnimations) {
                this.mState.mInPreLayout = true;
            } else {
                this.mAdapterHelper.consumeUpdatesInOnePass();
                this.mState.mInPreLayout = false;
            }
            this.mAdapterUpdateDuringMeasure = false;
            resumeRequestLayout(false);
        }
        if (this.mAdapter != null) {
            this.mState.mItemCount = this.mAdapter.getItemCount();
        } else {
            this.mState.mItemCount = 0;
        }
        this.mLayout.onMeasure(this.mRecycler, this.mState, widthSpec, heightSpec);
        this.mState.mInPreLayout = false;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw || h != oldh) {
            invalidateGlows();
        }
    }

    public void setItemAnimator(ItemAnimator animator) {
        if (this.mItemAnimator != null) {
            this.mItemAnimator.endAnimations();
            this.mItemAnimator.setListener(null);
        }
        this.mItemAnimator = animator;
        if (this.mItemAnimator != null) {
            this.mItemAnimator.setListener(this.mItemAnimatorListener);
        }
    }

    public ItemAnimator getItemAnimator() {
        return this.mItemAnimator;
    }

    private boolean supportsChangeAnimations() {
        return this.mItemAnimator != null ? this.mItemAnimator.getSupportsChangeAnimations() : false;
    }

    private void postAnimationRunner() {
        if (!this.mPostedAnimatorRunner && this.mIsAttached) {
            ViewCompat.postOnAnimation(this, this.mItemAnimatorRunner);
            this.mPostedAnimatorRunner = true;
        }
    }

    private boolean predictiveItemAnimationsEnabled() {
        return this.mItemAnimator != null ? this.mLayout.supportsPredictiveItemAnimations() : false;
    }

    private void processAdapterUpdatesAndSetAnimationFlags() {
        boolean z = false;
        if (this.mDataSetHasChangedAfterLayout) {
            this.mAdapterHelper.reset();
            markKnownViewsInvalid();
            this.mLayout.onItemsChanged(this);
        }
        if (this.mItemAnimator == null || !this.mLayout.supportsPredictiveItemAnimations()) {
            this.mAdapterHelper.consumeUpdatesInOnePass();
        } else {
            this.mAdapterHelper.preProcess();
        }
        boolean animationTypeSupported = (!this.mItemsAddedOrRemoved || this.mItemsChanged) ? !this.mItemsAddedOrRemoved ? this.mItemsChanged ? supportsChangeAnimations() : false : true : true;
        State state = this.mState;
        boolean hasStableIds = (this.mFirstLayoutComplete && this.mItemAnimator != null && (this.mDataSetHasChangedAfterLayout || animationTypeSupported || this.mLayout.mRequestedSimpleAnimations)) ? this.mDataSetHasChangedAfterLayout ? this.mAdapter.hasStableIds() : true : false;
        state.mRunSimpleAnimations = hasStableIds;
        State state2 = this.mState;
        if (this.mState.mRunSimpleAnimations && animationTypeSupported && !this.mDataSetHasChangedAfterLayout) {
            z = predictiveItemAnimationsEnabled();
        }
        state2.mRunPredictiveAnimations = z;
    }

    void dispatchLayout() {
        if (this.mAdapter == null) {
            Log.e(TAG, "No adapter attached; skipping layout");
            return;
        }
        int count;
        int i;
        ViewHolder holder;
        View view;
        this.mDisappearingViewsInLayoutPass.clear();
        eatRequestLayout();
        this.mRunningLayoutOrScroll = true;
        processAdapterUpdatesAndSetAnimationFlags();
        State state = this.mState;
        ArrayMap arrayMap = (this.mState.mRunSimpleAnimations && this.mItemsChanged && supportsChangeAnimations()) ? new ArrayMap() : null;
        state.mOldChangedHolders = arrayMap;
        this.mItemsChanged = false;
        this.mItemsAddedOrRemoved = false;
        ArrayMap appearingViewInitialBounds = null;
        this.mState.mInPreLayout = this.mState.mRunPredictiveAnimations;
        this.mState.mItemCount = this.mAdapter.getItemCount();
        if (this.mState.mRunSimpleAnimations) {
            this.mState.mPreLayoutHolderMap.clear();
            this.mState.mPostLayoutHolderMap.clear();
            count = this.mChildHelper.getChildCount();
            for (i = 0; i < count; i++) {
                holder = getChildViewHolderInt(this.mChildHelper.getChildAt(i));
                if (!holder.shouldIgnore() && (!holder.isInvalid() || this.mAdapter.hasStableIds())) {
                    view = holder.itemView;
                    this.mState.mPreLayoutHolderMap.put(holder, new ItemHolderInfo(holder, view.getLeft(), view.getTop(), view.getRight(), view.getBottom()));
                }
            }
        }
        if (this.mState.mRunPredictiveAnimations) {
            saveOldPositions();
            if (this.mState.mOldChangedHolders != null) {
                count = this.mChildHelper.getChildCount();
                for (i = 0; i < count; i++) {
                    holder = getChildViewHolderInt(this.mChildHelper.getChildAt(i));
                    if (!(!holder.isChanged() || holder.isRemoved() || holder.shouldIgnore())) {
                        this.mState.mOldChangedHolders.put(Long.valueOf(getChangedHolderKey(holder)), holder);
                        this.mState.mPreLayoutHolderMap.remove(holder);
                    }
                }
            }
            boolean didStructureChange = this.mState.mStructureChanged;
            this.mState.mStructureChanged = false;
            this.mLayout.onLayoutChildren(this.mRecycler, this.mState);
            this.mState.mStructureChanged = didStructureChange;
            appearingViewInitialBounds = new ArrayMap();
            for (i = 0; i < this.mChildHelper.getChildCount(); i++) {
                boolean found = false;
                View child = this.mChildHelper.getChildAt(i);
                if (!getChildViewHolderInt(child).shouldIgnore()) {
                    for (int j = 0; j < this.mState.mPreLayoutHolderMap.size(); j++) {
                        if (((ViewHolder) this.mState.mPreLayoutHolderMap.keyAt(j)).itemView == child) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        appearingViewInitialBounds.put(child, new Rect(child.getLeft(), child.getTop(), child.getRight(), child.getBottom()));
                    }
                }
            }
            clearOldPositions();
            this.mAdapterHelper.consumePostponedUpdates();
        } else {
            clearOldPositions();
            this.mAdapterHelper.consumeUpdatesInOnePass();
            if (this.mState.mOldChangedHolders != null) {
                count = this.mChildHelper.getChildCount();
                for (i = 0; i < count; i++) {
                    holder = getChildViewHolderInt(this.mChildHelper.getChildAt(i));
                    if (!(!holder.isChanged() || holder.isRemoved() || holder.shouldIgnore())) {
                        this.mState.mOldChangedHolders.put(Long.valueOf(getChangedHolderKey(holder)), holder);
                        this.mState.mPreLayoutHolderMap.remove(holder);
                    }
                }
            }
        }
        this.mState.mItemCount = this.mAdapter.getItemCount();
        this.mState.mDeletedInvisibleItemCountSincePreviousLayout = 0;
        this.mState.mInPreLayout = false;
        this.mLayout.onLayoutChildren(this.mRecycler, this.mState);
        this.mState.mStructureChanged = false;
        this.mPendingSavedState = null;
        state = this.mState;
        boolean z = this.mState.mRunSimpleAnimations && this.mItemAnimator != null;
        state.mRunSimpleAnimations = z;
        if (this.mState.mRunSimpleAnimations) {
            long key;
            ArrayMap newChangedHolders = this.mState.mOldChangedHolders != null ? new ArrayMap() : null;
            count = this.mChildHelper.getChildCount();
            for (i = 0; i < count; i++) {
                holder = getChildViewHolderInt(this.mChildHelper.getChildAt(i));
                if (!holder.shouldIgnore()) {
                    view = holder.itemView;
                    key = getChangedHolderKey(holder);
                    if (newChangedHolders == null || this.mState.mOldChangedHolders.get(Long.valueOf(key)) == null) {
                        this.mState.mPostLayoutHolderMap.put(holder, new ItemHolderInfo(holder, view.getLeft(), view.getTop(), view.getRight(), view.getBottom()));
                    } else {
                        newChangedHolders.put(Long.valueOf(key), holder);
                    }
                }
            }
            processDisappearingList(appearingViewInitialBounds);
            for (i = this.mState.mPreLayoutHolderMap.size() - 1; i >= 0; i--) {
                if (!this.mState.mPostLayoutHolderMap.containsKey((ViewHolder) this.mState.mPreLayoutHolderMap.keyAt(i))) {
                    ItemHolderInfo disappearingItem = (ItemHolderInfo) this.mState.mPreLayoutHolderMap.valueAt(i);
                    this.mState.mPreLayoutHolderMap.removeAt(i);
                    removeDetachedView(disappearingItem.holder.itemView, false);
                    this.mRecycler.unscrapView(disappearingItem.holder);
                    animateDisappearance(disappearingItem);
                }
            }
            int postLayoutCount = this.mState.mPostLayoutHolderMap.size();
            if (postLayoutCount > 0) {
                for (i = postLayoutCount - 1; i >= 0; i--) {
                    ViewHolder itemHolder = (ViewHolder) this.mState.mPostLayoutHolderMap.keyAt(i);
                    ItemHolderInfo info = (ItemHolderInfo) this.mState.mPostLayoutHolderMap.valueAt(i);
                    if (this.mState.mPreLayoutHolderMap.isEmpty() || !this.mState.mPreLayoutHolderMap.containsKey(itemHolder)) {
                        this.mState.mPostLayoutHolderMap.removeAt(i);
                        animateAppearance(itemHolder, appearingViewInitialBounds != null ? (Rect) appearingViewInitialBounds.get(itemHolder.itemView) : null, info.left, info.top);
                    }
                }
            }
            count = this.mState.mPostLayoutHolderMap.size();
            for (i = 0; i < count; i++) {
                ViewHolder postHolder = (ViewHolder) this.mState.mPostLayoutHolderMap.keyAt(i);
                ItemHolderInfo postInfo = (ItemHolderInfo) this.mState.mPostLayoutHolderMap.valueAt(i);
                ItemHolderInfo preInfo = (ItemHolderInfo) this.mState.mPreLayoutHolderMap.get(postHolder);
                if (!(preInfo == null || postInfo == null || (preInfo.left == postInfo.left && preInfo.top == postInfo.top))) {
                    postHolder.setIsRecyclable(false);
                    if (this.mItemAnimator.animateMove(postHolder, preInfo.left, preInfo.top, postInfo.left, postInfo.top)) {
                        postAnimationRunner();
                    }
                }
            }
            for (i = (this.mState.mOldChangedHolders != null ? this.mState.mOldChangedHolders.size() : 0) - 1; i >= 0; i--) {
                key = ((Long) this.mState.mOldChangedHolders.keyAt(i)).longValue();
                ViewHolder oldHolder = (ViewHolder) this.mState.mOldChangedHolders.get(Long.valueOf(key));
                View oldView = oldHolder.itemView;
                if (!(oldHolder.shouldIgnore() || this.mRecycler.mChangedScrap == null || !this.mRecycler.mChangedScrap.contains(oldHolder))) {
                    animateChange(oldHolder, (ViewHolder) newChangedHolders.get(Long.valueOf(key)));
                }
            }
        }
        resumeRequestLayout(false);
        this.mLayout.removeAndRecycleScrapInt(this.mRecycler, !this.mState.mRunPredictiveAnimations);
        this.mState.mPreviousLayoutItemCount = this.mState.mItemCount;
        this.mDataSetHasChangedAfterLayout = false;
        this.mState.mRunSimpleAnimations = false;
        this.mState.mRunPredictiveAnimations = false;
        this.mRunningLayoutOrScroll = false;
        this.mLayout.mRequestedSimpleAnimations = false;
        if (this.mRecycler.mChangedScrap != null) {
            this.mRecycler.mChangedScrap.clear();
        }
        this.mState.mOldChangedHolders = null;
    }

    long getChangedHolderKey(ViewHolder holder) {
        return this.mAdapter.hasStableIds() ? holder.getItemId() : (long) holder.mPosition;
    }

    private void processDisappearingList(ArrayMap<View, Rect> appearingViews) {
        int count = this.mDisappearingViewsInLayoutPass.size();
        for (int i = 0; i < count; i++) {
            View view = (View) this.mDisappearingViewsInLayoutPass.get(i);
            ViewHolder vh = getChildViewHolderInt(view);
            ItemHolderInfo info = (ItemHolderInfo) this.mState.mPreLayoutHolderMap.remove(vh);
            if (!this.mState.isPreLayout()) {
                this.mState.mPostLayoutHolderMap.remove(vh);
            }
            if (appearingViews.remove(view) != null) {
                this.mLayout.removeAndRecycleView(view, this.mRecycler);
            } else if (info != null) {
                animateDisappearance(info);
            } else {
                animateDisappearance(new ItemHolderInfo(vh, view.getLeft(), view.getTop(), view.getRight(), view.getBottom()));
            }
        }
        this.mDisappearingViewsInLayoutPass.clear();
    }

    private void animateAppearance(ViewHolder itemHolder, Rect beforeBounds, int afterLeft, int afterTop) {
        View newItemView = itemHolder.itemView;
        if (beforeBounds == null || (beforeBounds.left == afterLeft && beforeBounds.top == afterTop)) {
            itemHolder.setIsRecyclable(false);
            if (this.mItemAnimator.animateAdd(itemHolder)) {
                postAnimationRunner();
                return;
            }
            return;
        }
        itemHolder.setIsRecyclable(false);
        if (this.mItemAnimator.animateMove(itemHolder, beforeBounds.left, beforeBounds.top, afterLeft, afterTop)) {
            postAnimationRunner();
        }
    }

    private void animateDisappearance(ItemHolderInfo disappearingItem) {
        View disappearingItemView = disappearingItem.holder.itemView;
        addAnimatingView(disappearingItemView);
        int oldLeft = disappearingItem.left;
        int oldTop = disappearingItem.top;
        int newLeft = disappearingItemView.getLeft();
        int newTop = disappearingItemView.getTop();
        if (oldLeft == newLeft && oldTop == newTop) {
            disappearingItem.holder.setIsRecyclable(false);
            if (this.mItemAnimator.animateRemove(disappearingItem.holder)) {
                postAnimationRunner();
                return;
            }
            return;
        }
        disappearingItem.holder.setIsRecyclable(false);
        disappearingItemView.layout(newLeft, newTop, disappearingItemView.getWidth() + newLeft, disappearingItemView.getHeight() + newTop);
        if (this.mItemAnimator.animateMove(disappearingItem.holder, oldLeft, oldTop, newLeft, newTop)) {
            postAnimationRunner();
        }
    }

    private void animateChange(ViewHolder oldHolder, ViewHolder newHolder) {
        int toLeft;
        int toTop;
        oldHolder.setIsRecyclable(false);
        removeDetachedView(oldHolder.itemView, false);
        addAnimatingView(oldHolder.itemView);
        oldHolder.mShadowedHolder = newHolder;
        this.mRecycler.unscrapView(oldHolder);
        int fromLeft = oldHolder.itemView.getLeft();
        int fromTop = oldHolder.itemView.getTop();
        if (newHolder == null || newHolder.shouldIgnore()) {
            toLeft = fromLeft;
            toTop = fromTop;
        } else {
            toLeft = newHolder.itemView.getLeft();
            toTop = newHolder.itemView.getTop();
            newHolder.setIsRecyclable(false);
            newHolder.mShadowingHolder = oldHolder;
        }
        if (this.mItemAnimator.animateChange(oldHolder, newHolder, fromLeft, fromTop, toLeft, toTop)) {
            postAnimationRunner();
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        eatRequestLayout();
        dispatchLayout();
        resumeRequestLayout(false);
        this.mFirstLayoutComplete = true;
    }

    public void requestLayout() {
        if (this.mEatRequestLayout) {
            this.mLayoutRequestEaten = true;
        } else {
            super.requestLayout();
        }
    }

    void markItemDecorInsetsDirty() {
        int childCount = this.mChildHelper.getUnfilteredChildCount();
        for (int i = 0; i < childCount; i++) {
            ((LayoutParams) this.mChildHelper.getUnfilteredChildAt(i).getLayoutParams()).mInsetsDirty = true;
        }
        this.mRecycler.markItemDecorInsetsDirty();
    }

    public void draw(Canvas c) {
        int restore;
        int padding;
        int draw;
        int i = 0;
        super.draw(c);
        int count = this.mItemDecorations.size();
        for (int i2 = 0; i2 < count; i2++) {
            ((ItemDecoration) this.mItemDecorations.get(i2)).onDrawOver(c, this, this.mState);
        }
        int needsInvalidate = false;
        if (!(this.mLeftGlow == null || this.mLeftGlow.isFinished())) {
            restore = c.save();
            padding = this.mClipToPadding ? getPaddingBottom() : 0;
            c.rotate(270.0f);
            c.translate((float) ((-getHeight()) + padding), 0.0f);
            needsInvalidate = this.mLeftGlow != null ? this.mLeftGlow.draw(c) : false;
            c.restoreToCount(restore);
        }
        if (!(this.mTopGlow == null || this.mTopGlow.isFinished())) {
            restore = c.save();
            if (this.mClipToPadding) {
                c.translate((float) getPaddingLeft(), (float) getPaddingTop());
            }
            if (this.mTopGlow != null) {
                draw = this.mTopGlow.draw(c);
            } else {
                draw = 0;
            }
            needsInvalidate |= draw;
            c.restoreToCount(restore);
        }
        if (!(this.mRightGlow == null || this.mRightGlow.isFinished())) {
            restore = c.save();
            int width = getWidth();
            padding = this.mClipToPadding ? getPaddingTop() : 0;
            c.rotate(90.0f);
            c.translate((float) (-padding), (float) (-width));
            if (this.mRightGlow != null) {
                draw = this.mRightGlow.draw(c);
            } else {
                draw = 0;
            }
            needsInvalidate |= draw;
            c.restoreToCount(restore);
        }
        if (!(this.mBottomGlow == null || this.mBottomGlow.isFinished())) {
            restore = c.save();
            c.rotate(180.0f);
            if (this.mClipToPadding) {
                c.translate((float) ((-getWidth()) + getPaddingRight()), (float) ((-getHeight()) + getPaddingBottom()));
            } else {
                c.translate((float) (-getWidth()), (float) (-getHeight()));
            }
            if (this.mBottomGlow != null) {
                i = this.mBottomGlow.draw(c);
            }
            needsInvalidate |= i;
            c.restoreToCount(restore);
        }
        if (needsInvalidate == 0 && this.mItemAnimator != null && this.mItemDecorations.size() > 0 && this.mItemAnimator.isRunning()) {
            needsInvalidate = 1;
        }
        if (needsInvalidate != 0) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void onDraw(Canvas c) {
        super.onDraw(c);
        int count = this.mItemDecorations.size();
        for (int i = 0; i < count; i++) {
            ((ItemDecoration) this.mItemDecorations.get(i)).onDraw(c, this, this.mState);
        }
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams ? this.mLayout.checkLayoutParams((LayoutParams) p) : false;
    }

    protected android.view.ViewGroup.LayoutParams generateDefaultLayoutParams() {
        if (this.mLayout != null) {
            return this.mLayout.generateDefaultLayoutParams();
        }
        throw new IllegalStateException("RecyclerView has no LayoutManager");
    }

    public android.view.ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        if (this.mLayout != null) {
            return this.mLayout.generateLayoutParams(getContext(), attrs);
        }
        throw new IllegalStateException("RecyclerView has no LayoutManager");
    }

    protected android.view.ViewGroup.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams p) {
        if (this.mLayout != null) {
            return this.mLayout.generateLayoutParams(p);
        }
        throw new IllegalStateException("RecyclerView has no LayoutManager");
    }

    void saveOldPositions() {
        int childCount = this.mChildHelper.getUnfilteredChildCount();
        for (int i = 0; i < childCount; i++) {
            ViewHolder holder = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i));
            if (!holder.shouldIgnore()) {
                holder.saveOldPosition();
            }
        }
    }

    void clearOldPositions() {
        int childCount = this.mChildHelper.getUnfilteredChildCount();
        for (int i = 0; i < childCount; i++) {
            ViewHolder holder = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i));
            if (!holder.shouldIgnore()) {
                holder.clearOldPosition();
            }
        }
        this.mRecycler.clearOldPositions();
    }

    void offsetPositionRecordsForMove(int from, int to) {
        int inBetweenOffset;
        int childCount = this.mChildHelper.getUnfilteredChildCount();
        int start;
        int end;
        if (from < to) {
            start = from;
            end = to;
            inBetweenOffset = -1;
        } else {
            start = to;
            end = from;
            inBetweenOffset = 1;
        }
        for (int i = 0; i < childCount; i++) {
            ViewHolder holder = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i));
            if (holder != null && holder.mPosition >= start && holder.mPosition <= end) {
                if (holder.mPosition == from) {
                    holder.offsetPosition(to - from, false);
                } else {
                    holder.offsetPosition(inBetweenOffset, false);
                }
                this.mState.mStructureChanged = true;
            }
        }
        this.mRecycler.offsetPositionRecordsForMove(from, to);
        requestLayout();
    }

    void offsetPositionRecordsForInsert(int positionStart, int itemCount) {
        int childCount = this.mChildHelper.getUnfilteredChildCount();
        for (int i = 0; i < childCount; i++) {
            ViewHolder holder = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i));
            if (!(holder == null || holder.shouldIgnore() || holder.mPosition < positionStart)) {
                holder.offsetPosition(itemCount, false);
                this.mState.mStructureChanged = true;
            }
        }
        this.mRecycler.offsetPositionRecordsForInsert(positionStart, itemCount);
        requestLayout();
    }

    void offsetPositionRecordsForRemove(int positionStart, int itemCount, boolean applyToPreLayout) {
        int positionEnd = positionStart + itemCount;
        int childCount = this.mChildHelper.getUnfilteredChildCount();
        for (int i = 0; i < childCount; i++) {
            ViewHolder holder = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i));
            if (!(holder == null || holder.shouldIgnore())) {
                if (holder.mPosition >= positionEnd) {
                    holder.offsetPosition(-itemCount, applyToPreLayout);
                    this.mState.mStructureChanged = true;
                } else if (holder.mPosition >= positionStart) {
                    holder.flagRemovedAndOffsetPosition(positionStart - 1, -itemCount, applyToPreLayout);
                    this.mState.mStructureChanged = true;
                }
            }
        }
        this.mRecycler.offsetPositionRecordsForRemove(positionStart, itemCount, applyToPreLayout);
        requestLayout();
    }

    void viewRangeUpdate(int positionStart, int itemCount) {
        int childCount = this.mChildHelper.getUnfilteredChildCount();
        int positionEnd = positionStart + itemCount;
        for (int i = 0; i < childCount; i++) {
            View child = this.mChildHelper.getUnfilteredChildAt(i);
            ViewHolder holder = getChildViewHolderInt(child);
            if (holder != null && !holder.shouldIgnore() && holder.mPosition >= positionStart && holder.mPosition < positionEnd) {
                holder.addFlags(2);
                if (supportsChangeAnimations()) {
                    holder.addFlags(64);
                }
                ((LayoutParams) child.getLayoutParams()).mInsetsDirty = true;
            }
        }
        this.mRecycler.viewRangeUpdate(positionStart, itemCount);
    }

    void rebindUpdatedViewHolders() {
        int childCount = this.mChildHelper.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ViewHolder holder = getChildViewHolderInt(this.mChildHelper.getChildAt(i));
            if (!(holder == null || holder.shouldIgnore())) {
                if (holder.isRemoved() || holder.isInvalid()) {
                    requestLayout();
                } else if (holder.needsUpdate()) {
                    if (holder.getItemViewType() != this.mAdapter.getItemViewType(holder.mPosition)) {
                        holder.addFlags(4);
                        requestLayout();
                    } else if (holder.isChanged() && supportsChangeAnimations()) {
                        requestLayout();
                    } else {
                        this.mAdapter.bindViewHolder(holder, holder.mPosition);
                    }
                }
            }
        }
    }

    void markKnownViewsInvalid() {
        int childCount = this.mChildHelper.getUnfilteredChildCount();
        for (int i = 0; i < childCount; i++) {
            ViewHolder holder = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i));
            if (!(holder == null || holder.shouldIgnore())) {
                holder.addFlags(6);
            }
        }
        markItemDecorInsetsDirty();
        this.mRecycler.markKnownViewsInvalid();
    }

    public void invalidateItemDecorations() {
        if (this.mItemDecorations.size() != 0) {
            if (this.mLayout != null) {
                this.mLayout.assertNotInLayoutOrScroll("Cannot invalidate item decorations during a scroll or layout");
            }
            markItemDecorInsetsDirty();
            requestLayout();
        }
    }

    public ViewHolder getChildViewHolder(View child) {
        Object parent = child.getParent();
        if (parent == null || parent == this) {
            return getChildViewHolderInt(child);
        }
        throw new IllegalArgumentException("View " + child + " is not a direct child of " + this);
    }

    static ViewHolder getChildViewHolderInt(View child) {
        if (child == null) {
            return null;
        }
        return ((LayoutParams) child.getLayoutParams()).mViewHolder;
    }

    public int getChildPosition(View child) {
        ViewHolder holder = getChildViewHolderInt(child);
        return holder != null ? holder.getPosition() : -1;
    }

    public long getChildItemId(View child) {
        long j = -1;
        if (this.mAdapter == null || !this.mAdapter.hasStableIds()) {
            return -1;
        }
        ViewHolder holder = getChildViewHolderInt(child);
        if (holder != null) {
            j = holder.getItemId();
        }
        return j;
    }

    public ViewHolder findViewHolderForPosition(int position) {
        return findViewHolderForPosition(position, false);
    }

    ViewHolder findViewHolderForPosition(int position, boolean checkNewPosition) {
        int childCount = this.mChildHelper.getUnfilteredChildCount();
        for (int i = 0; i < childCount; i++) {
            ViewHolder holder = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i));
            if (!(holder == null || holder.isRemoved())) {
                if (checkNewPosition) {
                    if (holder.mPosition == position) {
                        return holder;
                    }
                } else if (holder.getPosition() == position) {
                    return holder;
                }
            }
        }
        return null;
    }

    public ViewHolder findViewHolderForItemId(long id) {
        int childCount = this.mChildHelper.getUnfilteredChildCount();
        for (int i = 0; i < childCount; i++) {
            ViewHolder holder = getChildViewHolderInt(this.mChildHelper.getUnfilteredChildAt(i));
            if (holder != null && holder.getItemId() == id) {
                return holder;
            }
        }
        return null;
    }

    public View findChildViewUnder(float x, float y) {
        for (int i = this.mChildHelper.getChildCount() - 1; i >= 0; i--) {
            View child = this.mChildHelper.getChildAt(i);
            float translationX = ViewCompat.getTranslationX(child);
            float translationY = ViewCompat.getTranslationY(child);
            if (x >= ((float) child.getLeft()) + translationX && x <= ((float) child.getRight()) + translationX && y >= ((float) child.getTop()) + translationY && y <= ((float) child.getBottom()) + translationY) {
                return child;
            }
        }
        return null;
    }

    public void offsetChildrenVertical(int dy) {
        int childCount = this.mChildHelper.getChildCount();
        for (int i = 0; i < childCount; i++) {
            this.mChildHelper.getChildAt(i).offsetTopAndBottom(dy);
        }
    }

    public void onChildAttachedToWindow(View child) {
    }

    public void onChildDetachedFromWindow(View child) {
    }

    public void offsetChildrenHorizontal(int dx) {
        int childCount = this.mChildHelper.getChildCount();
        for (int i = 0; i < childCount; i++) {
            this.mChildHelper.getChildAt(i).offsetLeftAndRight(dx);
        }
    }

    Rect getItemDecorInsetsForChild(View child) {
        LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (!lp.mInsetsDirty) {
            return lp.mDecorInsets;
        }
        Rect insets = lp.mDecorInsets;
        insets.set(0, 0, 0, 0);
        int decorCount = this.mItemDecorations.size();
        for (int i = 0; i < decorCount; i++) {
            this.mTempRect.set(0, 0, 0, 0);
            ((ItemDecoration) this.mItemDecorations.get(i)).getItemOffsets(this.mTempRect, child, this, this.mState);
            insets.left += this.mTempRect.left;
            insets.top += this.mTempRect.top;
            insets.right += this.mTempRect.right;
            insets.bottom += this.mTempRect.bottom;
        }
        lp.mInsetsDirty = false;
        return insets;
    }

    private void dispatchChildDetached(View child) {
        if (this.mAdapter != null) {
            this.mAdapter.onViewDetachedFromWindow(getChildViewHolderInt(child));
        }
        onChildDetachedFromWindow(child);
    }

    private void dispatchChildAttached(View child) {
        if (this.mAdapter != null) {
            this.mAdapter.onViewAttachedToWindow(getChildViewHolderInt(child));
        }
        onChildAttachedToWindow(child);
    }

    private void removeFromDisappearingList(View child) {
        this.mDisappearingViewsInLayoutPass.remove(child);
    }

    private void addToDisappearingList(View child) {
        if (!this.mDisappearingViewsInLayoutPass.contains(child)) {
            this.mDisappearingViewsInLayoutPass.add(child);
        }
    }
}
