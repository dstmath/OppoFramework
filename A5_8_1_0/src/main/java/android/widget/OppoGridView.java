package android.widget;

import android.R;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.RemotableViewMethod;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.GridLayoutAnimationController.AnimationParameters;
import android.widget.AbsListView.AdapterDataSetObserver;
import android.widget.AbsListView.LayoutParams;
import android.widget.AbsListView.RecycleBin;
import android.widget.RemoteViews.RemoteView;
import com.google.android.collect.Lists;
import com.oppo.widget.OppoTouchSearchView;
import java.util.ArrayList;

@RemoteView
public class OppoGridView extends AbsListView {
    public static final int AUTO_FIT = -1;
    private static final ArrayList<FixedViewInfo> EMPTY_INFO_LIST = new ArrayList();
    public static final int NO_STRETCH = 0;
    public static final int STRETCH_COLUMN_WIDTH = 2;
    public static final int STRETCH_SPACING = 1;
    public static final int STRETCH_SPACING_UNIFORM = 3;
    private static final String TAG = "OppoGridView";
    private int mColumnWidth;
    private ArrayList<FixedViewInfo> mFooterViewInfos;
    private int mGravity;
    private ArrayList<FixedViewInfo> mHeaderViewInfos;
    private int mHorizontalSpacing;
    private int mNumColumns;
    private View mReferenceView;
    private View mReferenceViewInSelectedRow;
    private int mRequestedColumnWidth;
    private int mRequestedHorizontalSpacing;
    private int mRequestedNumColumns;
    private int mStretchMode;
    private final Rect mTempRect;
    private int mVerticalSpacing;

    private class FixedViewInfo {
        Object data;
        boolean isSelectable;
        View view;

        /* synthetic */ FixedViewInfo(OppoGridView this$0, FixedViewInfo -this1) {
            this();
        }

        private FixedViewInfo() {
        }
    }

    private class HeaderViewListAdapter implements WrapperListAdapter, Filterable {
        private final ListAdapter mAdapter;
        boolean mAreAllFixedViewsSelectable;
        ArrayList<FixedViewInfo> mFooterViewInfos;
        ArrayList<FixedViewInfo> mHeaderViewInfos;
        private final boolean mIsFilterable;

        public HeaderViewListAdapter(ArrayList<FixedViewInfo> headerViewInfos, ArrayList<FixedViewInfo> footerViewInfos, ListAdapter adapter) {
            boolean areAllListInfosSelectable;
            this.mAdapter = adapter;
            this.mIsFilterable = adapter instanceof Filterable;
            if (headerViewInfos == null) {
                this.mHeaderViewInfos = OppoGridView.EMPTY_INFO_LIST;
            } else {
                this.mHeaderViewInfos = headerViewInfos;
            }
            if (footerViewInfos == null) {
                this.mFooterViewInfos = OppoGridView.EMPTY_INFO_LIST;
            } else {
                this.mFooterViewInfos = footerViewInfos;
            }
            if (areAllListInfosSelectable(this.mHeaderViewInfos)) {
                areAllListInfosSelectable = areAllListInfosSelectable(this.mFooterViewInfos);
            } else {
                areAllListInfosSelectable = false;
            }
            this.mAreAllFixedViewsSelectable = areAllListInfosSelectable;
        }

        public int getHeadersCount() {
            return this.mHeaderViewInfos.size();
        }

        public int getFootersCount() {
            return this.mFooterViewInfos.size();
        }

        public boolean isEmpty() {
            return this.mAdapter != null ? this.mAdapter.isEmpty() : true;
        }

        private boolean areAllListInfosSelectable(ArrayList<FixedViewInfo> infos) {
            if (infos != null) {
                for (FixedViewInfo info : infos) {
                    if (!info.isSelectable) {
                        return false;
                    }
                }
            }
            return true;
        }

        public boolean removeHeader(View v) {
            boolean z = false;
            for (int i = 0; i < this.mHeaderViewInfos.size(); i++) {
                if (((FixedViewInfo) this.mHeaderViewInfos.get(i)).view == v) {
                    this.mHeaderViewInfos.remove(i);
                    if (areAllListInfosSelectable(this.mHeaderViewInfos)) {
                        z = areAllListInfosSelectable(this.mFooterViewInfos);
                    }
                    this.mAreAllFixedViewsSelectable = z;
                    return true;
                }
            }
            return false;
        }

        public boolean removeFooter(View v) {
            boolean z = false;
            for (int i = 0; i < this.mFooterViewInfos.size(); i++) {
                if (((FixedViewInfo) this.mFooterViewInfos.get(i)).view == v) {
                    this.mFooterViewInfos.remove(i);
                    if (areAllListInfosSelectable(this.mHeaderViewInfos)) {
                        z = areAllListInfosSelectable(this.mFooterViewInfos);
                    }
                    this.mAreAllFixedViewsSelectable = z;
                    return true;
                }
            }
            return false;
        }

        public int getCount() {
            int count = getFootersCount() + getHeadersCount();
            if (this.mAdapter != null) {
                return count + this.mAdapter.getCount();
            }
            return count;
        }

        public boolean areAllItemsEnabled() {
            if (this.mAdapter == null) {
                return true;
            }
            return this.mAreAllFixedViewsSelectable ? this.mAdapter.areAllItemsEnabled() : false;
        }

        public boolean isEnabled(int position) {
            int numHeaders = getHeadersCount();
            if (position < numHeaders) {
                return ((FixedViewInfo) this.mHeaderViewInfos.get(position)).isSelectable;
            }
            int adjPosition = position - numHeaders;
            int adapterCount = 0;
            if (this.mAdapter != null) {
                adapterCount = this.mAdapter.getCount();
                if (adjPosition < adapterCount) {
                    return this.mAdapter.isEnabled(adjPosition);
                }
            }
            return ((FixedViewInfo) this.mFooterViewInfos.get(adjPosition - adapterCount)).isSelectable;
        }

        public Object getItem(int position) {
            int numHeaders = getHeadersCount();
            if (position < numHeaders) {
                return ((FixedViewInfo) this.mHeaderViewInfos.get(position)).data;
            }
            int adjPosition = position - numHeaders;
            int adapterCount = 0;
            if (this.mAdapter != null) {
                adapterCount = this.mAdapter.getCount();
                if (adjPosition < adapterCount) {
                    return this.mAdapter.getItem(adjPosition);
                }
            }
            return ((FixedViewInfo) this.mFooterViewInfos.get(adjPosition - adapterCount)).data;
        }

        public long getItemId(int position) {
            int numHeaders = getHeadersCount();
            if (this.mAdapter != null && position >= numHeaders) {
                int adjPosition = position - numHeaders;
                if (adjPosition < this.mAdapter.getCount()) {
                    return this.mAdapter.getItemId(adjPosition);
                }
            }
            return -1;
        }

        public boolean hasStableIds() {
            if (this.mAdapter != null) {
                return this.mAdapter.hasStableIds();
            }
            return false;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            int numHeaders = getHeadersCount();
            if (position < numHeaders) {
                return ((FixedViewInfo) this.mHeaderViewInfos.get(position)).view;
            }
            int adjPosition = position - numHeaders;
            int adapterCount = 0;
            if (this.mAdapter != null) {
                adapterCount = this.mAdapter.getCount();
                if (adjPosition < adapterCount) {
                    return this.mAdapter.getView(adjPosition, convertView, parent);
                }
            }
            if (getFootersCount() > 0) {
                return ((FixedViewInfo) this.mFooterViewInfos.get(adjPosition - adapterCount)).view;
            }
            return null;
        }

        public int getItemViewType(int position) {
            int numHeaders = getHeadersCount();
            if (this.mAdapter != null && position >= numHeaders) {
                int adjPosition = position - numHeaders;
                if (adjPosition < this.mAdapter.getCount()) {
                    return this.mAdapter.getItemViewType(adjPosition);
                }
            }
            return -2;
        }

        public int getViewTypeCount() {
            if (this.mAdapter != null) {
                return this.mAdapter.getViewTypeCount();
            }
            return 1;
        }

        public void registerDataSetObserver(DataSetObserver observer) {
            if (this.mAdapter != null) {
                this.mAdapter.registerDataSetObserver(observer);
            }
        }

        public void unregisterDataSetObserver(DataSetObserver observer) {
            if (this.mAdapter != null) {
                this.mAdapter.unregisterDataSetObserver(observer);
            }
        }

        public Filter getFilter() {
            if (this.mIsFilterable) {
                return ((Filterable) this.mAdapter).getFilter();
            }
            return null;
        }

        public ListAdapter getWrappedAdapter() {
            return this.mAdapter;
        }
    }

    public OppoGridView(Context context) {
        this(context, null);
    }

    public OppoGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 16842865);
    }

    public OppoGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mNumColumns = -1;
        this.mHorizontalSpacing = 0;
        this.mVerticalSpacing = 0;
        this.mStretchMode = 2;
        this.mReferenceView = null;
        this.mReferenceViewInSelectedRow = null;
        this.mGravity = 3;
        this.mTempRect = new Rect();
        this.mHeaderViewInfos = Lists.newArrayList();
        this.mFooterViewInfos = Lists.newArrayList();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GridView, defStyle, 0);
        setHorizontalSpacing(a.getDimensionPixelOffset(1, 0));
        setVerticalSpacing(a.getDimensionPixelOffset(2, 0));
        int index = a.getInt(3, 2);
        if (index >= 0) {
            setStretchMode(index);
        }
        int columnWidth = a.getDimensionPixelOffset(4, -1);
        if (columnWidth > 0) {
            setColumnWidth(columnWidth);
        }
        setNumColumns(a.getInt(5, 1));
        index = a.getInt(0, -1);
        if (index >= 0) {
            setGravity(index);
        }
        a.recycle();
    }

    public ListAdapter getAdapter() {
        return this.mAdapter;
    }

    @RemotableViewMethod
    public void setRemoteViewsAdapter(Intent intent) {
        super.setRemoteViewsAdapter(intent);
    }

    public void setAdapter(ListAdapter adapter) {
        if (!(this.mAdapter == null || this.mDataSetObserver == null)) {
            this.mAdapter.unregisterDataSetObserver(this.mDataSetObserver);
        }
        resetList();
        this.mRecycler.clear();
        if (this.mHeaderViewInfos.size() > 0 || this.mFooterViewInfos.size() > 0) {
            this.mAdapter = new HeaderViewListAdapter(this.mHeaderViewInfos, this.mFooterViewInfos, adapter);
        } else {
            this.mAdapter = adapter;
        }
        this.mOldSelectedPosition = -1;
        this.mOldSelectedRowId = Long.MIN_VALUE;
        super.setAdapter(adapter);
        if (this.mAdapter != null) {
            int position;
            this.mOldItemCount = this.mItemCount;
            this.mItemCount = this.mAdapter.getCount();
            this.mDataChanged = true;
            checkFocus();
            this.mDataSetObserver = new AdapterDataSetObserver(this);
            this.mAdapter.registerDataSetObserver(this.mDataSetObserver);
            this.mRecycler.setViewTypeCount(this.mAdapter.getViewTypeCount());
            if (this.mStackFromBottom) {
                position = lookForSelectablePosition(this.mItemCount - 1, false);
            } else {
                position = lookForSelectablePosition(0, true);
            }
            setSelectedPositionInt(position);
            setNextSelectedPositionInt(position);
            checkSelectionChanged();
        } else {
            checkFocus();
            checkSelectionChanged();
        }
        requestLayout();
    }

    /* JADX WARNING: Missing block: B:4:0x000b, code:
            return -1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    int lookForSelectablePosition(int position, boolean lookDown) {
        if (this.mAdapter == null || isInTouchMode() || position < 0 || position >= this.mItemCount) {
            return -1;
        }
        return position;
    }

    void fillGap(boolean down) {
        int numColumns = this.mNumColumns;
        int verticalSpacing = this.mVerticalSpacing;
        int headerCount = this.mHeaderViewInfos.size();
        int footerCount = this.mFooterViewInfos.size();
        int count = getChildCount();
        int startOffset;
        int position;
        if (down) {
            int paddingTop = 0;
            if ((this.mGroupFlags & 34) == 34) {
                paddingTop = getListPaddingTop();
            }
            startOffset = count > 0 ? getChildAt(count - 1).getBottom() + verticalSpacing : paddingTop;
            position = this.mFirstPosition + count;
            if (this.mStackFromBottom) {
                position += numColumns - 1;
            }
            fillDown(position, startOffset);
            correctTooHigh(numColumns, verticalSpacing, getChildCount());
            return;
        }
        int nextOffset;
        int paddingBottom = 0;
        if ((this.mGroupFlags & 34) == 34) {
            paddingBottom = getListPaddingBottom();
        }
        startOffset = count > 0 ? getChildAt(0).getTop() - verticalSpacing : getHeight() - paddingBottom;
        position = this.mFirstPosition;
        if (position <= headerCount || position > this.mItemCount - footerCount) {
            nextOffset = 1;
        } else {
            nextOffset = numColumns;
        }
        if (this.mStackFromBottom) {
            position--;
        } else {
            position -= nextOffset;
        }
        fillUp(position, startOffset);
        correctTooLow(numColumns, verticalSpacing, getChildCount());
    }

    private View fillDown(int pos, int nextTop) {
        View selectedView = null;
        int end = this.mBottom - this.mTop;
        if ((this.mGroupFlags & 34) == 34) {
            end -= this.mListPadding.bottom;
        }
        int headerCount = this.mHeaderViewInfos.size();
        int footerCount = this.mFooterViewInfos.size();
        while (nextTop < end && pos < this.mItemCount) {
            View temp;
            int nextOffset;
            if (pos < headerCount || pos >= this.mItemCount - footerCount) {
                temp = makeHeaderFooter(pos, nextTop, true);
                nextOffset = 1;
            } else {
                temp = makeRow(pos, nextTop, true);
                if (this.mNumColumns + pos > this.mItemCount - footerCount) {
                    nextOffset = (this.mItemCount - footerCount) - pos;
                } else {
                    nextOffset = this.mNumColumns;
                }
            }
            if (temp != null) {
                selectedView = temp;
            }
            nextTop = this.mReferenceView.getBottom() + this.mVerticalSpacing;
            pos += nextOffset;
        }
        setVisibleRangeHint(this.mFirstPosition, (this.mFirstPosition + getChildCount()) - 1);
        return selectedView;
    }

    private View makeRow(int startPos, int y, boolean flow) {
        int last;
        int columnWidth = this.mColumnWidth;
        int horizontalSpacing = this.mHorizontalSpacing;
        int headerCount = this.mHeaderViewInfos.size();
        int footerCount = this.mFooterViewInfos.size();
        int nextLeft = this.mListPadding.left + (this.mStretchMode == 3 ? horizontalSpacing : 0);
        if (this.mStackFromBottom) {
            last = startPos + 1;
            startPos = Math.max(headerCount, (startPos - this.mNumColumns) + 1);
            if (last - startPos < this.mNumColumns) {
                nextLeft += (this.mNumColumns - (last - startPos)) * (columnWidth + horizontalSpacing);
            }
        } else {
            last = Math.min(this.mNumColumns + startPos, this.mItemCount - footerCount);
        }
        View selectedView = null;
        boolean hasFocus = shouldShowSelector();
        boolean inClick = touchModeDrawsInPressedState();
        int selectedPosition = this.mSelectedPosition;
        View child = null;
        int pos = startPos;
        while (pos < last) {
            boolean selected = pos == selectedPosition;
            child = makeAndAddView(pos, y, flow, nextLeft, selected, flow ? -1 : pos - startPos);
            nextLeft += columnWidth;
            if (pos < last - 1) {
                nextLeft += horizontalSpacing;
            }
            if (selected && (hasFocus || inClick)) {
                selectedView = child;
            }
            pos++;
        }
        this.mReferenceView = child;
        if (selectedView != null) {
            this.mReferenceViewInSelectedRow = this.mReferenceView;
        }
        return selectedView;
    }

    private View fillUp(int pos, int nextBottom) {
        View selectedView = null;
        int end = 0;
        if ((this.mGroupFlags & 34) == 34) {
            end = this.mListPadding.top;
        }
        int headerCount = this.mHeaderViewInfos.size();
        int footerCount = this.mFooterViewInfos.size();
        while (nextBottom > end && pos >= 0) {
            View temp;
            int nextOffset;
            if (pos < headerCount || pos > this.mItemCount - footerCount) {
                temp = makeHeaderFooter(pos, nextBottom, false);
                nextOffset = 1;
            } else if (pos == this.mItemCount - footerCount) {
                temp = makeHeaderFooter(pos, nextBottom, false);
                if (pos - this.mNumColumns < headerCount) {
                    nextOffset = pos - headerCount;
                } else {
                    nextOffset = this.mNumColumns;
                }
            } else if (pos == headerCount) {
                temp = makeRow(pos, nextBottom, false);
                nextOffset = 1;
            } else {
                temp = makeRow(pos, nextBottom, false);
                if (pos - this.mNumColumns < headerCount) {
                    nextOffset = pos - headerCount;
                } else {
                    nextOffset = this.mNumColumns;
                }
            }
            if (temp != null) {
                selectedView = temp;
            }
            nextBottom = this.mReferenceView.getTop() - this.mVerticalSpacing;
            this.mFirstPosition = pos;
            pos -= nextOffset;
        }
        if (this.mStackFromBottom) {
            this.mFirstPosition = Math.max(0, pos + 1);
        }
        setVisibleRangeHint(this.mFirstPosition, (this.mFirstPosition + getChildCount()) - 1);
        return selectedView;
    }

    private View fillFromTop(int nextTop) {
        this.mFirstPosition = Math.min(this.mFirstPosition, this.mSelectedPosition);
        this.mFirstPosition = Math.min(this.mFirstPosition, this.mItemCount - 1);
        if (this.mFirstPosition < 0) {
            this.mFirstPosition = 0;
        }
        this.mFirstPosition -= this.mFirstPosition % this.mNumColumns;
        return fillDown(this.mFirstPosition, nextTop);
    }

    private View fillFromBottom(int lastPosition, int nextBottom) {
        int invertedPosition = (this.mItemCount - 1) - Math.min(Math.max(lastPosition, this.mSelectedPosition), this.mItemCount - 1);
        return fillUp((this.mItemCount - 1) - (invertedPosition - (invertedPosition % this.mNumColumns)), nextBottom);
    }

    private View fillSelection(int childrenTop, int childrenBottom) {
        int rowStart;
        View sel;
        int selectedPosition = reconcileSelectedPosition();
        int numColumns = this.mNumColumns;
        int verticalSpacing = this.mVerticalSpacing;
        int headerCount = this.mHeaderViewInfos.size();
        int footerCount = this.mFooterViewInfos.size();
        int rowEnd = -1;
        int fadingEdgeLength = getVerticalFadingEdgeLength();
        int topSelectionPixel;
        if (selectedPosition < headerCount || selectedPosition >= this.mItemCount - footerCount) {
            rowStart = selectedPosition;
            topSelectionPixel = getTopSelectionPixel(childrenTop, fadingEdgeLength, selectedPosition);
            sel = makeHeaderFooter(selectedPosition, childrenTop, true);
        } else {
            int i;
            if (this.mStackFromBottom) {
                int lastGrid = (this.mItemCount - 1) - footerCount;
                int invertedSelection = lastGrid - selectedPosition;
                rowEnd = lastGrid - (invertedSelection - (invertedSelection % numColumns));
                rowStart = Math.max(headerCount, (rowEnd - numColumns) + 1);
            } else {
                rowStart = (selectedPosition - (selectedPosition % numColumns)) + headerCount;
            }
            topSelectionPixel = getTopSelectionPixel(childrenTop, fadingEdgeLength, rowStart);
            if (this.mStackFromBottom) {
                i = rowEnd;
            } else {
                i = rowStart;
            }
            sel = makeRow(i, topSelectionPixel, true);
        }
        this.mFirstPosition = rowStart;
        View referenceView = this.mReferenceView;
        int nextOffset;
        if (this.mStackFromBottom) {
            if (selectedPosition <= headerCount || selectedPosition > this.mItemCount - footerCount) {
                nextOffset = 1;
            } else {
                nextOffset = numColumns;
            }
            offsetChildrenTopAndBottom(getBottomSelectionPixel(childrenBottom, fadingEdgeLength, numColumns, rowStart) - referenceView.getBottom());
            fillUp(rowStart - 1, referenceView.getTop() - verticalSpacing);
            pinToTop(childrenTop);
            fillDown(rowEnd + nextOffset, referenceView.getBottom() + verticalSpacing);
            adjustViewsUpOrDown();
        } else {
            if (selectedPosition < headerCount || selectedPosition >= this.mItemCount - footerCount) {
                nextOffset = 1;
            } else {
                nextOffset = numColumns;
            }
            fillDown(rowStart + nextOffset, referenceView.getBottom() + verticalSpacing);
            pinToBottom(childrenBottom);
            fillUp(rowStart - nextOffset, referenceView.getTop() - verticalSpacing);
            adjustViewsUpOrDown();
        }
        return sel;
    }

    private void pinToTop(int childrenTop) {
        if (this.mFirstPosition == 0) {
            int offset = childrenTop - getChildAt(0).getTop();
            if (offset < 0) {
                offsetChildrenTopAndBottom(offset);
            }
        }
    }

    private void pinToBottom(int childrenBottom) {
        int count = getChildCount();
        if (this.mFirstPosition + count == this.mItemCount) {
            int offset = childrenBottom - getChildAt(count - 1).getBottom();
            if (offset > 0) {
                offsetChildrenTopAndBottom(offset);
            }
        }
    }

    int findMotionRow(int y) {
        int childCount = getChildCount();
        if (childCount > 0) {
            int numColumns = this.mNumColumns;
            int i;
            if (this.mStackFromBottom) {
                for (i = childCount - 1; i >= 0; i -= numColumns) {
                    if (y >= getChildAt(i).getTop()) {
                        return this.mFirstPosition + i;
                    }
                }
            } else {
                for (i = 0; i < childCount; i += numColumns) {
                    if (y <= getChildAt(i).getBottom()) {
                        return this.mFirstPosition + i;
                    }
                }
            }
        }
        return -1;
    }

    private View fillSpecific(int position, int top) {
        int motionRowStart;
        View temp;
        int numColumns = this.mNumColumns;
        int headerCount = this.mHeaderViewInfos.size();
        int footerCount = this.mFooterViewInfos.size();
        int motionRowEnd = -1;
        if (position < headerCount || position >= this.mItemCount - footerCount) {
            motionRowStart = position;
            temp = makeHeaderFooter(position, top, true);
        } else {
            int i;
            if (this.mStackFromBottom) {
                int lastGrid = (this.mItemCount - 1) - footerCount;
                int invertedSelection = lastGrid - position;
                motionRowEnd = lastGrid - (invertedSelection - (invertedSelection % numColumns));
                motionRowStart = Math.max(headerCount, (motionRowEnd - numColumns) + 1);
            } else {
                motionRowStart = (position - (position % numColumns)) + headerCount;
            }
            if (this.mStackFromBottom) {
                i = motionRowEnd;
            } else {
                i = motionRowStart;
            }
            temp = makeRow(i, top, true);
        }
        this.mFirstPosition = motionRowStart;
        View referenceView = this.mReferenceView;
        if (referenceView == null) {
            return null;
        }
        View below;
        View above;
        int verticalSpacing = this.mVerticalSpacing;
        int nextOffset;
        int childCount;
        if (this.mStackFromBottom) {
            if (position <= headerCount || position > this.mItemCount - footerCount) {
                nextOffset = 1;
            } else {
                nextOffset = numColumns;
            }
            below = fillDown(motionRowEnd + nextOffset, referenceView.getBottom() + verticalSpacing);
            adjustViewsUpOrDown();
            above = fillUp(motionRowStart - 1, referenceView.getTop() - verticalSpacing);
            childCount = getChildCount();
            if (childCount > 0) {
                correctTooLow(nextOffset, verticalSpacing, childCount);
            }
        } else {
            if (position < headerCount || position >= this.mItemCount - footerCount) {
                nextOffset = 1;
            } else {
                nextOffset = numColumns;
            }
            above = fillUp(motionRowStart - nextOffset, referenceView.getTop() - verticalSpacing);
            adjustViewsUpOrDown();
            below = fillDown(motionRowStart + nextOffset, referenceView.getBottom() + verticalSpacing);
            childCount = getChildCount();
            if (childCount > 0) {
                correctTooHigh(nextOffset, verticalSpacing, childCount);
            }
        }
        if (temp != null) {
            return temp;
        }
        if (above != null) {
            return above;
        }
        return below;
    }

    private void correctTooHigh(int numColumns, int verticalSpacing, int childCount) {
        if ((this.mFirstPosition + childCount) - 1 == this.mItemCount - 1 && childCount > 0) {
            int bottomOffset = ((this.mBottom - this.mTop) - this.mListPadding.bottom) - getChildAt(childCount - 1).getBottom();
            View firstChild = getChildAt(0);
            int firstTop = firstChild.getTop();
            if (bottomOffset <= 0) {
                return;
            }
            if (this.mFirstPosition > 0 || firstTop < this.mListPadding.top) {
                if (this.mFirstPosition == 0) {
                    bottomOffset = Math.min(bottomOffset, this.mListPadding.top - firstTop);
                }
                offsetChildrenTopAndBottom(bottomOffset);
                if (this.mFirstPosition > 0) {
                    int i = this.mFirstPosition;
                    if (this.mStackFromBottom) {
                        numColumns = 1;
                    }
                    fillUp(i - numColumns, firstChild.getTop() - verticalSpacing);
                    adjustViewsUpOrDown();
                }
            }
        }
    }

    private void correctTooLow(int numColumns, int verticalSpacing, int childCount) {
        if (this.mFirstPosition == 0 && childCount > 0) {
            int end = (this.mBottom - this.mTop) - this.mListPadding.bottom;
            int topOffset = getChildAt(0).getTop() - this.mListPadding.top;
            View lastChild = getChildAt(childCount - 1);
            int lastBottom = lastChild.getBottom();
            int lastPosition = (this.mFirstPosition + childCount) - 1;
            if (topOffset <= 0) {
                return;
            }
            if (lastPosition < this.mItemCount - 1 || lastBottom > end) {
                if (lastPosition == this.mItemCount - 1) {
                    topOffset = Math.min(topOffset, lastBottom - end);
                }
                offsetChildrenTopAndBottom(-topOffset);
                if (lastPosition < this.mItemCount - 1) {
                    if (!this.mStackFromBottom) {
                        numColumns = 1;
                    }
                    fillDown(lastPosition + numColumns, lastChild.getBottom() + verticalSpacing);
                    adjustViewsUpOrDown();
                }
            }
        }
    }

    private View fillFromSelection(int selectedTop, int childrenTop, int childrenBottom) {
        int rowStart;
        View sel;
        int fadingEdgeLength = getVerticalFadingEdgeLength();
        int selectedPosition = this.mSelectedPosition;
        int numColumns = this.mNumColumns;
        int verticalSpacing = this.mVerticalSpacing;
        int headerCount = this.mHeaderViewInfos.size();
        int footerCount = this.mFooterViewInfos.size();
        int rowEnd = -1;
        if (selectedPosition < headerCount || selectedPosition >= this.mItemCount - footerCount) {
            rowStart = selectedPosition;
            sel = makeHeaderFooter(selectedPosition, selectedTop, true);
        } else {
            int i;
            if (this.mStackFromBottom) {
                int lastGrid = (this.mItemCount - 1) - footerCount;
                int invertedSelection = lastGrid - selectedPosition;
                rowEnd = lastGrid - (invertedSelection - (invertedSelection % numColumns));
                rowStart = Math.max(headerCount, (rowEnd - numColumns) + 1);
            } else {
                rowStart = (selectedPosition - (selectedPosition % numColumns)) + headerCount;
            }
            if (this.mStackFromBottom) {
                i = rowEnd;
            } else {
                i = rowStart;
            }
            sel = makeRow(i, selectedTop, true);
        }
        int topSelectionPixel = getTopSelectionPixel(childrenTop, fadingEdgeLength, rowStart);
        int bottomSelectionPixel = getBottomSelectionPixel(childrenBottom, fadingEdgeLength, numColumns, rowStart);
        this.mFirstPosition = rowStart;
        View referenceView = this.mReferenceView;
        adjustForTopFadingEdge(referenceView, topSelectionPixel, bottomSelectionPixel);
        adjustForBottomFadingEdge(referenceView, topSelectionPixel, bottomSelectionPixel);
        int nextOffset;
        if (this.mStackFromBottom) {
            if (selectedPosition <= headerCount || selectedPosition > this.mItemCount - footerCount) {
                nextOffset = 1;
            } else {
                nextOffset = numColumns;
            }
            fillDown(rowEnd + nextOffset, referenceView.getBottom() + verticalSpacing);
            adjustViewsUpOrDown();
            fillUp(rowStart - 1, referenceView.getTop() - verticalSpacing);
        } else {
            if (selectedPosition < headerCount || selectedPosition >= this.mItemCount - footerCount) {
                nextOffset = 1;
            } else {
                nextOffset = numColumns;
            }
            fillUp(rowStart - nextOffset, referenceView.getTop() - verticalSpacing);
            adjustViewsUpOrDown();
            fillDown(rowStart + nextOffset, referenceView.getBottom() + verticalSpacing);
        }
        return sel;
    }

    private int getBottomSelectionPixel(int childrenBottom, int fadingEdgeLength, int numColumns, int rowStart) {
        int bottomSelectionPixel = childrenBottom;
        if ((rowStart + numColumns) - 1 < this.mItemCount - 1) {
            return childrenBottom - fadingEdgeLength;
        }
        return bottomSelectionPixel;
    }

    private int getTopSelectionPixel(int childrenTop, int fadingEdgeLength, int rowStart) {
        int topSelectionPixel = childrenTop;
        if (rowStart > 0) {
            return childrenTop + fadingEdgeLength;
        }
        return topSelectionPixel;
    }

    private void adjustForBottomFadingEdge(View childInSelectedRow, int topSelectionPixel, int bottomSelectionPixel) {
        if (childInSelectedRow != null && childInSelectedRow.getBottom() > bottomSelectionPixel) {
            offsetChildrenTopAndBottom(-Math.min(childInSelectedRow.getTop() - topSelectionPixel, childInSelectedRow.getBottom() - bottomSelectionPixel));
        }
    }

    private void adjustForTopFadingEdge(View childInSelectedRow, int topSelectionPixel, int bottomSelectionPixel) {
        if (childInSelectedRow != null && childInSelectedRow.getTop() < topSelectionPixel) {
            offsetChildrenTopAndBottom(Math.min(topSelectionPixel - childInSelectedRow.getTop(), bottomSelectionPixel - childInSelectedRow.getBottom()));
        }
    }

    @RemotableViewMethod
    public void smoothScrollToPosition(int position) {
        super.smoothScrollToPosition(position);
    }

    @RemotableViewMethod
    public void smoothScrollByOffset(int offset) {
        super.smoothScrollByOffset(offset);
    }

    private View moveSelection(int delta, int childrenTop, int childrenBottom) {
        int rowStart;
        int oldRowStart;
        View sel;
        View referenceView;
        int fadingEdgeLength = getVerticalFadingEdgeLength();
        int selectedPosition = this.mSelectedPosition;
        int numColumns = this.mNumColumns;
        int verticalSpacing = this.mVerticalSpacing;
        int rowEnd = -1;
        if (this.mStackFromBottom) {
            int invertedSelection = (this.mItemCount - 1) - selectedPosition;
            rowEnd = (this.mItemCount - 1) - (invertedSelection - (invertedSelection % numColumns));
            rowStart = Math.max(0, (rowEnd - numColumns) + 1);
            invertedSelection = (this.mItemCount - 1) - (selectedPosition - delta);
            oldRowStart = Math.max(0, (((this.mItemCount - 1) - (invertedSelection - (invertedSelection % numColumns))) - numColumns) + 1);
        } else {
            oldRowStart = (selectedPosition - delta) - ((selectedPosition - delta) % numColumns);
            rowStart = selectedPosition - (selectedPosition % numColumns);
        }
        int rowDelta = rowStart - oldRowStart;
        int topSelectionPixel = getTopSelectionPixel(childrenTop, fadingEdgeLength, rowStart);
        int bottomSelectionPixel = getBottomSelectionPixel(childrenBottom, fadingEdgeLength, numColumns, rowStart);
        this.mFirstPosition = rowStart;
        int i;
        int oldTop;
        if (rowDelta > 0) {
            int oldBottom;
            if (this.mReferenceViewInSelectedRow == null) {
                oldBottom = 0;
            } else {
                oldBottom = this.mReferenceViewInSelectedRow.getBottom();
            }
            if (this.mStackFromBottom) {
                i = rowEnd;
            } else {
                i = rowStart;
            }
            sel = makeRow(i, oldBottom + verticalSpacing, true);
            referenceView = this.mReferenceView;
            adjustForBottomFadingEdge(referenceView, topSelectionPixel, bottomSelectionPixel);
        } else if (rowDelta < 0) {
            oldTop = this.mReferenceViewInSelectedRow == null ? 0 : this.mReferenceViewInSelectedRow.getTop();
            if (this.mStackFromBottom) {
                i = rowEnd;
            } else {
                i = rowStart;
            }
            sel = makeRow(i, oldTop - verticalSpacing, false);
            referenceView = this.mReferenceView;
            adjustForTopFadingEdge(referenceView, topSelectionPixel, bottomSelectionPixel);
        } else {
            oldTop = this.mReferenceViewInSelectedRow == null ? 0 : this.mReferenceViewInSelectedRow.getTop();
            if (this.mStackFromBottom) {
                i = rowEnd;
            } else {
                i = rowStart;
            }
            sel = makeRow(i, oldTop, true);
            referenceView = this.mReferenceView;
        }
        if (this.mStackFromBottom) {
            fillDown(rowEnd + numColumns, referenceView.getBottom() + verticalSpacing);
            adjustViewsUpOrDown();
            fillUp(rowStart - 1, referenceView.getTop() - verticalSpacing);
        } else {
            fillUp(rowStart - numColumns, referenceView.getTop() - verticalSpacing);
            adjustViewsUpOrDown();
            fillDown(rowStart + numColumns, referenceView.getBottom() + verticalSpacing);
        }
        return sel;
    }

    private boolean determineColumns(int availableSpace) {
        int requestedHorizontalSpacing = this.mRequestedHorizontalSpacing;
        int stretchMode = this.mStretchMode;
        int requestedColumnWidth = this.mRequestedColumnWidth;
        boolean didNotInitiallyFit = false;
        if (this.mRequestedNumColumns != -1) {
            this.mNumColumns = this.mRequestedNumColumns;
        } else if (requestedColumnWidth > 0) {
            this.mNumColumns = (availableSpace + requestedHorizontalSpacing) / (requestedColumnWidth + requestedHorizontalSpacing);
        } else {
            this.mNumColumns = 2;
        }
        if (this.mNumColumns <= 0) {
            this.mNumColumns = 1;
        }
        switch (stretchMode) {
            case 0:
                this.mColumnWidth = requestedColumnWidth;
                this.mHorizontalSpacing = requestedHorizontalSpacing;
                break;
            default:
                int spaceLeftOver = (availableSpace - (this.mNumColumns * requestedColumnWidth)) - ((this.mNumColumns - 1) * requestedHorizontalSpacing);
                if (spaceLeftOver < 0) {
                    didNotInitiallyFit = true;
                }
                switch (stretchMode) {
                    case 1:
                        this.mColumnWidth = requestedColumnWidth;
                        if (this.mNumColumns <= 1) {
                            this.mHorizontalSpacing = requestedHorizontalSpacing + spaceLeftOver;
                            break;
                        }
                        this.mHorizontalSpacing = (spaceLeftOver / (this.mNumColumns - 1)) + requestedHorizontalSpacing;
                        break;
                    case 2:
                        this.mColumnWidth = (spaceLeftOver / this.mNumColumns) + requestedColumnWidth;
                        this.mHorizontalSpacing = requestedHorizontalSpacing;
                        break;
                    case 3:
                        this.mColumnWidth = requestedColumnWidth;
                        if (this.mNumColumns <= 1) {
                            this.mHorizontalSpacing = requestedHorizontalSpacing + spaceLeftOver;
                            break;
                        }
                        this.mHorizontalSpacing = (spaceLeftOver / (this.mNumColumns + 1)) + requestedHorizontalSpacing;
                        break;
                }
                break;
        }
        return didNotInitiallyFit;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int headerCount = this.mHeaderViewInfos.size();
        int footerCount = this.mFooterViewInfos.size();
        if (widthMode == 0) {
            if (this.mColumnWidth > 0) {
                widthSize = (this.mColumnWidth + this.mListPadding.left) + this.mListPadding.right;
            } else {
                widthSize = this.mListPadding.left + this.mListPadding.right;
            }
            widthSize += getVerticalScrollbarWidth();
        }
        boolean didNotInitiallyFit = determineColumns((widthSize - this.mListPadding.left) - this.mListPadding.right);
        int childHeight = 0;
        if (this.mAdapter == null) {
            i = 0;
        } else {
            i = this.mAdapter.getCount();
        }
        this.mItemCount = i;
        int count = this.mItemCount;
        if (count > headerCount) {
            View child = obtainView(headerCount, this.mIsScrap);
            LayoutParams p = (LayoutParams) child.getLayoutParams();
            if (p == null) {
                p = (LayoutParams) generateDefaultLayoutParams();
                child.setLayoutParams(p);
            }
            p.viewType = this.mAdapter.getItemViewType(0);
            p.forceAdd = true;
            child.measure(getChildMeasureSpec(MeasureSpec.makeMeasureSpec(this.mColumnWidth, 1073741824), 0, p.width), getChildMeasureSpec(MeasureSpec.makeMeasureSpec(0, 0), 0, p.height));
            childHeight = child.getMeasuredHeight();
            int childState = combineMeasuredStates(0, child.getMeasuredState());
            if (this.mRecycler.shouldRecycleViewType(p.viewType)) {
                this.mRecycler.addScrapView(child, -1);
            }
        }
        if (heightMode == 0) {
            heightSize = ((this.mListPadding.top + this.mListPadding.bottom) + childHeight) + (getVerticalFadingEdgeLength() * 2);
        }
        if (heightMode == Integer.MIN_VALUE) {
            int ourSize = this.mListPadding.top + this.mListPadding.bottom;
            int numColumns = this.mNumColumns;
            for (int i2 = headerCount; i2 < count - footerCount; i2 += numColumns) {
                ourSize += childHeight;
                if (i2 + numColumns < count) {
                    ourSize += this.mVerticalSpacing;
                }
                if (ourSize >= heightSize) {
                    ourSize = heightSize;
                    break;
                }
            }
            ourSize += measureHeaderFooter();
            if (ourSize >= heightSize) {
                ourSize = heightSize;
            }
            heightSize = ourSize;
        }
        if (widthMode == Integer.MIN_VALUE && this.mRequestedNumColumns != -1 && ((((this.mRequestedNumColumns * this.mColumnWidth) + ((this.mRequestedNumColumns - 1) * this.mHorizontalSpacing)) + this.mListPadding.left) + this.mListPadding.right > widthSize || didNotInitiallyFit)) {
            widthSize |= 16777216;
        }
        setMeasuredDimension(widthSize, heightSize);
        this.mWidthMeasureSpec = widthMeasureSpec;
    }

    protected void attachLayoutAnimationParameters(View child, ViewGroup.LayoutParams params, int index, int count) {
        AnimationParameters animationParams = params.layoutAnimationParameters;
        if (animationParams == null) {
            animationParams = new AnimationParameters();
            params.layoutAnimationParameters = animationParams;
        }
        animationParams.count = count;
        animationParams.index = index;
        animationParams.columnsCount = this.mNumColumns;
        animationParams.rowsCount = count / this.mNumColumns;
        if (this.mStackFromBottom) {
            int invertedIndex = (count - 1) - index;
            animationParams.column = (this.mNumColumns - 1) - (invertedIndex % this.mNumColumns);
            animationParams.row = (animationParams.rowsCount - 1) - (invertedIndex / this.mNumColumns);
            return;
        }
        animationParams.column = index % this.mNumColumns;
        animationParams.row = index / this.mNumColumns;
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x008d A:{Catch:{ all -> 0x01e7 }} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00d9 A:{Catch:{ all -> 0x01e7 }} */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0098 A:{Catch:{ all -> 0x01e7 }} */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x008d A:{Catch:{ all -> 0x01e7 }} */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0098 A:{Catch:{ all -> 0x01e7 }} */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00d9 A:{Catch:{ all -> 0x01e7 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void layoutChildren() {
        boolean blockLayoutRequests = this.mBlockLayoutRequests;
        if (!blockLayoutRequests) {
            this.mBlockLayoutRequests = true;
        }
        try {
            super.layoutChildren();
            invalidate();
            if (this.mAdapter == null) {
                resetList();
                invokeOnItemScrollListener();
                return;
            }
            boolean dataChanged;
            int childrenTop = this.mListPadding.top;
            int childrenBottom = (this.mBottom - this.mTop) - this.mListPadding.bottom;
            int childCount = getChildCount();
            int delta = 0;
            View oldSel = null;
            View oldFirst = null;
            View newSel = null;
            int index;
            switch (this.mLayoutMode) {
                case 2:
                    index = this.mNextSelectedPosition - this.mFirstPosition;
                    if (index >= 0 && index < childCount) {
                        newSel = getChildAt(index);
                    }
                case 1:
                case 3:
                case 4:
                case 5:
                    dataChanged = this.mDataChanged;
                    if (dataChanged) {
                        handleDataChanged();
                    }
                    if (this.mItemCount == 0) {
                        resetList();
                        invokeOnItemScrollListener();
                        if (!blockLayoutRequests) {
                            this.mBlockLayoutRequests = false;
                        }
                        return;
                    }
                    View sel;
                    setSelectedPositionInt(this.mNextSelectedPosition);
                    int firstPosition = this.mFirstPosition;
                    RecycleBin recycleBin = this.mRecycler;
                    if (dataChanged) {
                        for (int i = 0; i < childCount; i++) {
                            recycleBin.addScrapView(getChildAt(i), firstPosition + i);
                        }
                    } else {
                        recycleBin.fillActiveViews(childCount, firstPosition);
                    }
                    detachAllViewsFromParent();
                    recycleBin.removeSkippedScrap();
                    switch (this.mLayoutMode) {
                        case 1:
                            this.mFirstPosition = 0;
                            sel = fillFromTop(childrenTop);
                            adjustViewsUpOrDown();
                            break;
                        case 2:
                            if (newSel == null) {
                                sel = fillSelection(childrenTop, childrenBottom);
                                break;
                            } else {
                                sel = fillFromSelection(newSel.getTop(), childrenTop, childrenBottom);
                                break;
                            }
                        case 3:
                            sel = fillUp(this.mItemCount - 1, childrenBottom);
                            adjustViewsUpOrDown();
                            break;
                        case 4:
                            sel = fillSpecific(this.mSelectedPosition, this.mSpecificTop);
                            break;
                        case 5:
                            sel = fillSpecific(this.mSyncPosition, this.mSpecificTop);
                            break;
                        case 6:
                            sel = moveSelection(delta, childrenTop, childrenBottom);
                            break;
                        default:
                            int i2;
                            if (childCount != 0) {
                                if (this.mSelectedPosition >= 0 && this.mSelectedPosition < this.mItemCount) {
                                    i2 = this.mSelectedPosition;
                                    if (oldSel != null) {
                                        childrenTop = oldSel.getTop();
                                    }
                                    sel = fillSpecific(i2, childrenTop);
                                    break;
                                }
                                if (this.mFirstPosition >= this.mItemCount) {
                                    sel = fillSpecific(0, childrenTop);
                                    break;
                                }
                                i2 = this.mFirstPosition;
                                if (oldFirst != null) {
                                    childrenTop = oldFirst.getTop();
                                }
                                sel = fillSpecific(i2, childrenTop);
                                break;
                            } else if (!this.mStackFromBottom) {
                                i2 = (this.mAdapter == null || isInTouchMode()) ? -1 : 0;
                                setSelectedPositionInt(i2);
                                sel = fillFromTop(childrenTop);
                                break;
                            } else {
                                int last = this.mItemCount - 1;
                                if (this.mAdapter == null || isInTouchMode()) {
                                    i2 = -1;
                                } else {
                                    i2 = last;
                                }
                                setSelectedPositionInt(i2);
                                sel = fillFromBottom(last, childrenBottom);
                                break;
                            }
                            break;
                    }
                    recycleBin.scrapActiveViews();
                    if (sel != null) {
                        positionSelector(-1, sel);
                        this.mSelectedTop = sel.getTop();
                    } else if (this.mTouchMode <= 0 || this.mTouchMode >= 3) {
                        this.mSelectedTop = 0;
                        this.mSelectorRect.setEmpty();
                    } else {
                        View child = getChildAt(this.mMotionPosition - this.mFirstPosition);
                        if (child != null) {
                            positionSelector(this.mMotionPosition, child);
                        }
                    }
                    this.mLayoutMode = 0;
                    this.mDataChanged = false;
                    if (this.mPositionScrollAfterLayout != null) {
                        post(this.mPositionScrollAfterLayout);
                        this.mPositionScrollAfterLayout = null;
                    }
                    this.mNeedSync = false;
                    setNextSelectedPositionInt(this.mSelectedPosition);
                    updateScrollIndicators();
                    if (this.mItemCount > 0) {
                        checkSelectionChanged();
                    }
                    invokeOnItemScrollListener();
                    if (!blockLayoutRequests) {
                        this.mBlockLayoutRequests = false;
                    }
                    return;
                case 6:
                    if (this.mNextSelectedPosition >= 0) {
                        delta = this.mNextSelectedPosition - this.mSelectedPosition;
                    }
                    dataChanged = this.mDataChanged;
                    if (dataChanged) {
                    }
                    if (this.mItemCount == 0) {
                    }
                    break;
                default:
                    index = this.mSelectedPosition - this.mFirstPosition;
                    if (index >= 0 && index < childCount) {
                        oldSel = getChildAt(index);
                    }
                    oldFirst = getChildAt(0);
            }
            dataChanged = this.mDataChanged;
            if (dataChanged) {
            }
            if (this.mItemCount == 0) {
            }
        } finally {
            if (!blockLayoutRequests) {
                this.mBlockLayoutRequests = false;
            }
        }
    }

    private View makeAndAddView(int position, int y, boolean flow, int childrenLeft, boolean selected, int where) {
        View child;
        if (!this.mDataChanged) {
            child = this.mRecycler.getActiveView(position);
            if (child != null) {
                setupChild(child, position, y, flow, childrenLeft, selected, true, where);
                return child;
            }
        }
        child = obtainView(position, this.mIsScrap);
        setupChild(child, position, y, flow, childrenLeft, selected, this.mIsScrap[0], where);
        return child;
    }

    private void setupChild(View child, int position, int y, boolean flow, int childrenLeft, boolean selected, boolean recycled, int where) {
        int columnWidth;
        int childLeft;
        boolean isSelected = selected ? shouldShowSelector() : false;
        boolean updateChildSelected = isSelected != child.isSelected();
        int mode = this.mTouchMode;
        boolean isPressed = (mode <= 0 || mode >= 3) ? false : this.mMotionPosition == position;
        boolean updateChildPressed = isPressed != child.isPressed();
        int headerCount = this.mHeaderViewInfos.size();
        int footerCount = this.mFooterViewInfos.size();
        boolean needToMeasure = (!recycled || updateChildSelected) ? true : child.isLayoutRequested();
        ViewGroup.LayoutParams p = (LayoutParams) child.getLayoutParams();
        if (p == null) {
            p = (LayoutParams) generateDefaultLayoutParams();
        }
        p.viewType = this.mAdapter.getItemViewType(position);
        if ((!recycled || (p.forceAdd ^ 1) == 0) && !(p.recycledHeaderFooter && p.viewType == -2)) {
            p.forceAdd = false;
            if (p.viewType == -2) {
                p.recycledHeaderFooter = true;
            }
            addViewInLayout(child, where, p, true);
        } else {
            attachViewToParent(child, where, p);
        }
        if (updateChildSelected) {
            child.setSelected(isSelected);
            if (isSelected) {
                requestFocus();
            }
        }
        if (updateChildPressed) {
            child.setPressed(isPressed);
        }
        if (!(this.mChoiceMode == 0 || this.mCheckStates == null)) {
            if (child instanceof Checkable) {
                ((Checkable) child).setChecked(this.mCheckStates.get(position));
            } else if (getContext().getApplicationInfo().targetSdkVersion >= 11) {
                child.setActivated(this.mCheckStates.get(position));
            }
        }
        if (needToMeasure) {
            int childWidthSpec;
            if (position < headerCount || position >= this.mItemCount - footerCount) {
                childWidthSpec = getChildMeasureSpec(this.mWidthMeasureSpec, this.mListPadding.left + this.mListPadding.right, p.width);
            } else {
                childWidthSpec = getChildMeasureSpec(MeasureSpec.makeMeasureSpec(this.mColumnWidth, 1073741824), 0, p.width);
            }
            child.measure(childWidthSpec, getChildMeasureSpec(MeasureSpec.makeMeasureSpec(0, 0), 0, p.height));
        } else {
            cleanupLayoutState(child);
        }
        int w = child.getMeasuredWidth();
        int h = child.getMeasuredHeight();
        if (position < headerCount || position >= this.mItemCount - footerCount) {
            columnWidth = w;
        } else {
            columnWidth = this.mColumnWidth;
        }
        int childTop = flow ? y : y - h;
        switch (Gravity.getAbsoluteGravity(this.mGravity, getLayoutDirection()) & 7) {
            case 1:
                childLeft = childrenLeft + ((columnWidth - w) / 2);
                break;
            case 5:
                childLeft = (childrenLeft + columnWidth) - w;
                break;
            default:
                childLeft = childrenLeft;
                break;
        }
        if (needToMeasure) {
            child.layout(childLeft, childTop, childLeft + w, childTop + h);
        } else {
            child.offsetLeftAndRight(childLeft - child.getLeft());
            child.offsetTopAndBottom(childTop - child.getTop());
        }
        if (this.mCachingStarted) {
            child.setDrawingCacheEnabled(true);
        }
        if (recycled && ((LayoutParams) child.getLayoutParams()).scrappedFromPosition != position) {
            child.jumpDrawablesToCurrentState();
        }
    }

    public void setSelection(int position) {
        if (isInTouchMode()) {
            this.mResurrectToPosition = position;
        } else {
            setNextSelectedPositionInt(position);
        }
        this.mLayoutMode = 2;
        if (this.mPositionScroller != null) {
            this.mPositionScroller.stop();
        }
        requestLayout();
    }

    void setSelectionInt(int position) {
        int next;
        int previous;
        int previousSelectedPosition = this.mNextSelectedPosition;
        if (this.mPositionScroller != null) {
            this.mPositionScroller.stop();
        }
        setNextSelectedPositionInt(position);
        layoutChildren();
        if (this.mStackFromBottom) {
            next = (this.mItemCount - 1) - this.mNextSelectedPosition;
        } else {
            next = this.mNextSelectedPosition;
        }
        if (this.mStackFromBottom) {
            previous = (this.mItemCount - 1) - previousSelectedPosition;
        } else {
            previous = previousSelectedPosition;
        }
        if (next / this.mNumColumns != previous / this.mNumColumns) {
            awakenScrollBars();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return commonKey(keyCode, 1, event);
    }

    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        return commonKey(keyCode, repeatCount, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return commonKey(keyCode, 1, event);
    }

    private boolean commonKey(int keyCode, int count, KeyEvent event) {
        if (this.mAdapter == null) {
            return false;
        }
        if (this.mDataChanged) {
            layoutChildren();
        }
        boolean handled = false;
        int action = event.getAction();
        if (action != 1) {
            switch (keyCode) {
                case 19:
                    if (!event.hasNoModifiers()) {
                        if (event.hasModifiers(2)) {
                            if (!resurrectSelectionIfNeeded()) {
                                handled = fullScroll(33);
                                break;
                            }
                            handled = true;
                            break;
                        }
                    } else if (!resurrectSelectionIfNeeded()) {
                        handled = arrowScroll(33);
                        break;
                    } else {
                        handled = true;
                        break;
                    }
                    break;
                case 20:
                    if (!event.hasNoModifiers()) {
                        if (event.hasModifiers(2)) {
                            if (!resurrectSelectionIfNeeded()) {
                                handled = fullScroll(130);
                                break;
                            }
                            handled = true;
                            break;
                        }
                    } else if (!resurrectSelectionIfNeeded()) {
                        handled = arrowScroll(130);
                        break;
                    } else {
                        handled = true;
                        break;
                    }
                    break;
                case 21:
                    if (event.hasNoModifiers()) {
                        if (!resurrectSelectionIfNeeded()) {
                            handled = arrowScroll(17);
                            break;
                        }
                        handled = true;
                        break;
                    }
                    break;
                case 22:
                    if (event.hasNoModifiers()) {
                        if (!resurrectSelectionIfNeeded()) {
                            handled = arrowScroll(66);
                            break;
                        }
                        handled = true;
                        break;
                    }
                    break;
                case OppoTouchSearchView.MAX_SECTIONS_NUM_WITH_DOT /*23*/:
                case 66:
                    if (event.hasNoModifiers()) {
                        handled = resurrectSelectionIfNeeded();
                        if (!handled && event.getRepeatCount() == 0 && getChildCount() > 0) {
                            keyPressed();
                            handled = true;
                            break;
                        }
                    }
                    break;
                case 62:
                    if (this.mPopup == null || (this.mPopup.isShowing() ^ 1) != 0) {
                        if (!event.hasNoModifiers()) {
                            if (event.hasModifiers(1)) {
                                if (!resurrectSelectionIfNeeded()) {
                                    handled = pageScroll(33);
                                    break;
                                }
                                handled = true;
                                break;
                            }
                        } else if (!resurrectSelectionIfNeeded()) {
                            handled = pageScroll(130);
                            break;
                        } else {
                            handled = true;
                            break;
                        }
                    }
                    break;
                case 92:
                    if (!event.hasNoModifiers()) {
                        if (event.hasModifiers(2)) {
                            if (!resurrectSelectionIfNeeded()) {
                                handled = fullScroll(33);
                                break;
                            }
                            handled = true;
                            break;
                        }
                    } else if (!resurrectSelectionIfNeeded()) {
                        handled = pageScroll(33);
                        break;
                    } else {
                        handled = true;
                        break;
                    }
                    break;
                case 93:
                    if (!event.hasNoModifiers()) {
                        if (event.hasModifiers(2)) {
                            if (!resurrectSelectionIfNeeded()) {
                                handled = fullScroll(130);
                                break;
                            }
                            handled = true;
                            break;
                        }
                    } else if (!resurrectSelectionIfNeeded()) {
                        handled = pageScroll(130);
                        break;
                    } else {
                        handled = true;
                        break;
                    }
                    break;
                case 122:
                    if (event.hasNoModifiers()) {
                        if (!resurrectSelectionIfNeeded()) {
                            handled = fullScroll(33);
                            break;
                        }
                        handled = true;
                        break;
                    }
                    break;
                case 123:
                    if (event.hasNoModifiers()) {
                        if (!resurrectSelectionIfNeeded()) {
                            handled = fullScroll(130);
                            break;
                        }
                        handled = true;
                        break;
                    }
                    break;
            }
        }
        if (handled || sendToTextFilter(keyCode, count, event)) {
            return true;
        }
        switch (action) {
            case 0:
                return super.onKeyDown(keyCode, event);
            case 1:
                return super.onKeyUp(keyCode, event);
            case 2:
                return super.onKeyMultiple(keyCode, count, event);
            default:
                return false;
        }
    }

    boolean pageScroll(int direction) {
        int nextPage = -1;
        if (direction == 33) {
            nextPage = Math.max(0, this.mSelectedPosition - getChildCount());
        } else if (direction == 130) {
            nextPage = Math.min(this.mItemCount - 1, this.mSelectedPosition + getChildCount());
        }
        if (nextPage < 0) {
            return false;
        }
        setSelectionInt(nextPage);
        invokeOnItemScrollListener();
        awakenScrollBars();
        return true;
    }

    boolean fullScroll(int direction) {
        boolean moved = false;
        if (direction == 33) {
            this.mLayoutMode = 2;
            setSelectionInt(0);
            invokeOnItemScrollListener();
            moved = true;
        } else if (direction == 130) {
            this.mLayoutMode = 2;
            setSelectionInt(this.mItemCount - 1);
            invokeOnItemScrollListener();
            moved = true;
        }
        if (moved) {
            awakenScrollBars();
        }
        return moved;
    }

    boolean arrowScroll(int direction) {
        int endOfRowPos;
        int startOfRowPos;
        int selectedPosition = this.mSelectedPosition;
        int numColumns = this.mNumColumns;
        boolean moved = false;
        if (this.mStackFromBottom) {
            endOfRowPos = (this.mItemCount - 1) - ((((this.mItemCount - 1) - selectedPosition) / numColumns) * numColumns);
            startOfRowPos = Math.max(0, (endOfRowPos - numColumns) + 1);
        } else {
            startOfRowPos = (selectedPosition / numColumns) * numColumns;
            endOfRowPos = Math.min((startOfRowPos + numColumns) - 1, this.mItemCount - 1);
        }
        switch (direction) {
            case 17:
                if (selectedPosition > startOfRowPos) {
                    this.mLayoutMode = 6;
                    setSelectionInt(Math.max(0, selectedPosition - 1));
                    moved = true;
                    break;
                }
                break;
            case 33:
                if (startOfRowPos > 0) {
                    this.mLayoutMode = 6;
                    setSelectionInt(Math.max(0, selectedPosition - numColumns));
                    moved = true;
                    break;
                }
                break;
            case 66:
                if (selectedPosition < endOfRowPos) {
                    this.mLayoutMode = 6;
                    setSelectionInt(Math.min(selectedPosition + 1, this.mItemCount - 1));
                    moved = true;
                    break;
                }
                break;
            case 130:
                if (endOfRowPos < this.mItemCount - 1) {
                    this.mLayoutMode = 6;
                    setSelectionInt(Math.min(selectedPosition + numColumns, this.mItemCount - 1));
                    moved = true;
                    break;
                }
                break;
        }
        if (moved) {
            playSoundEffect(SoundEffectConstants.getContantForFocusDirection(direction));
            invokeOnItemScrollListener();
        }
        if (moved) {
            awakenScrollBars();
        }
        return moved;
    }

    boolean sequenceScroll(int direction) {
        int endOfRow;
        int startOfRow;
        int selectedPosition = this.mSelectedPosition;
        int numColumns = this.mNumColumns;
        int count = this.mItemCount;
        if (this.mStackFromBottom) {
            endOfRow = (count - 1) - ((((count - 1) - selectedPosition) / numColumns) * numColumns);
            startOfRow = Math.max(0, (endOfRow - numColumns) + 1);
        } else {
            startOfRow = (selectedPosition / numColumns) * numColumns;
            endOfRow = Math.min((startOfRow + numColumns) - 1, count - 1);
        }
        boolean moved = false;
        boolean showScroll = false;
        switch (direction) {
            case 1:
                if (selectedPosition > 0) {
                    this.mLayoutMode = 6;
                    setSelectionInt(selectedPosition - 1);
                    moved = true;
                    if (selectedPosition != startOfRow) {
                        showScroll = false;
                        break;
                    }
                    showScroll = true;
                    break;
                }
                break;
            case 2:
                if (selectedPosition < count - 1) {
                    this.mLayoutMode = 6;
                    setSelectionInt(selectedPosition + 1);
                    moved = true;
                    if (selectedPosition != endOfRow) {
                        showScroll = false;
                        break;
                    }
                    showScroll = true;
                    break;
                }
                break;
        }
        if (moved) {
            playSoundEffect(SoundEffectConstants.getContantForFocusDirection(direction));
            invokeOnItemScrollListener();
        }
        if (showScroll) {
            awakenScrollBars();
        }
        return moved;
    }

    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        int closestChildIndex = -1;
        if (gainFocus && previouslyFocusedRect != null) {
            previouslyFocusedRect.offset(this.mScrollX, this.mScrollY);
            Rect otherRect = this.mTempRect;
            int minDistance = Integer.MAX_VALUE;
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                if (isCandidateSelection(i, direction)) {
                    View other = getChildAt(i);
                    other.getDrawingRect(otherRect);
                    offsetDescendantRectToMyCoords(other, otherRect);
                    int distance = getDistance(previouslyFocusedRect, otherRect, direction);
                    if (distance < minDistance) {
                        minDistance = distance;
                        closestChildIndex = i;
                    }
                }
            }
        }
        if (closestChildIndex >= 0) {
            setSelection(this.mFirstPosition + closestChildIndex);
        } else {
            requestLayout();
        }
    }

    private boolean isCandidateSelection(int childIndex, int direction) {
        int rowEnd;
        int rowStart;
        boolean z = true;
        boolean z2 = false;
        int count = getChildCount();
        int invertedIndex = (count - 1) - childIndex;
        if (this.mStackFromBottom) {
            rowEnd = (count - 1) - (invertedIndex - (invertedIndex % this.mNumColumns));
            rowStart = Math.max(0, (rowEnd - this.mNumColumns) + 1);
        } else {
            rowStart = childIndex - (childIndex % this.mNumColumns);
            rowEnd = Math.max((this.mNumColumns + rowStart) - 1, count);
        }
        switch (direction) {
            case 1:
                if (childIndex == rowEnd && rowEnd == count - 1) {
                    z2 = true;
                }
                return z2;
            case 2:
                if (childIndex == rowStart && rowStart == 0) {
                    z2 = true;
                }
                return z2;
            case 17:
                if (childIndex != rowEnd) {
                    z = false;
                }
                return z;
            case 33:
                if (rowEnd != count - 1) {
                    z = false;
                }
                return z;
            case 66:
                if (childIndex != rowStart) {
                    z = false;
                }
                return z;
            case 130:
                if (rowStart != 0) {
                    z = false;
                }
                return z;
            default:
                throw new IllegalArgumentException("direction must be one of {FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT, FOCUS_FORWARD, FOCUS_BACKWARD}.");
        }
    }

    public void setGravity(int gravity) {
        if (this.mGravity != gravity) {
            this.mGravity = gravity;
            requestLayoutIfNecessary();
        }
    }

    public int getGravity() {
        return this.mGravity;
    }

    public void setHorizontalSpacing(int horizontalSpacing) {
        if (horizontalSpacing != this.mRequestedHorizontalSpacing) {
            this.mRequestedHorizontalSpacing = horizontalSpacing;
            requestLayoutIfNecessary();
        }
    }

    public int getHorizontalSpacing() {
        return this.mHorizontalSpacing;
    }

    public int getRequestedHorizontalSpacing() {
        return this.mRequestedHorizontalSpacing;
    }

    public void setVerticalSpacing(int verticalSpacing) {
        if (verticalSpacing != this.mVerticalSpacing) {
            this.mVerticalSpacing = verticalSpacing;
            requestLayoutIfNecessary();
        }
    }

    public int getVerticalSpacing() {
        return this.mVerticalSpacing;
    }

    public void setStretchMode(int stretchMode) {
        if (stretchMode != this.mStretchMode) {
            this.mStretchMode = stretchMode;
            requestLayoutIfNecessary();
        }
    }

    public int getStretchMode() {
        return this.mStretchMode;
    }

    public void setColumnWidth(int columnWidth) {
        if (columnWidth != this.mRequestedColumnWidth) {
            this.mRequestedColumnWidth = columnWidth;
            requestLayoutIfNecessary();
        }
    }

    public int getColumnWidth() {
        return this.mColumnWidth;
    }

    public int getRequestedColumnWidth() {
        return this.mRequestedColumnWidth;
    }

    public void setNumColumns(int numColumns) {
        if (numColumns != this.mRequestedNumColumns) {
            this.mRequestedNumColumns = numColumns;
            requestLayoutIfNecessary();
        }
    }

    @ExportedProperty
    public int getNumColumns() {
        return this.mNumColumns;
    }

    private void adjustViewsUpOrDown() {
        int childCount = getChildCount();
        if (childCount > 0) {
            int delta;
            if (this.mStackFromBottom) {
                delta = getChildAt(childCount - 1).getBottom() - (getHeight() - this.mListPadding.bottom);
                if (this.mFirstPosition + childCount < this.mItemCount) {
                    delta += this.mVerticalSpacing;
                }
                if (delta > 0) {
                    delta = 0;
                }
            } else {
                delta = getChildAt(0).getTop() - this.mListPadding.top;
                if (this.mFirstPosition != 0) {
                    delta -= this.mVerticalSpacing;
                }
                if (delta < 0) {
                    delta = 0;
                }
            }
            if (delta != 0) {
                offsetChildrenTopAndBottom(-delta);
            }
        }
    }

    protected int computeVerticalScrollExtent() {
        int count = getChildCount();
        if (count <= 0) {
            return 0;
        }
        int extent = getRowCount(count, this.mNumColumns) * 100;
        View view = getChildAt(0);
        int top = view.getTop();
        int height = view.getHeight();
        if (height > 0) {
            extent += (top * 100) / height;
        }
        view = getChildAt(count - 1);
        int bottom = view.getBottom();
        height = view.getHeight();
        if (height > 0) {
            extent -= ((bottom - getHeight()) * 100) / height;
        }
        return extent;
    }

    protected int computeVerticalScrollOffset() {
        if (this.mFirstPosition >= 0 && getChildCount() > 0) {
            View view = getChildAt(0);
            int top = view.getTop();
            int height = view.getHeight();
            if (height > 0) {
                int oddItemsOnFirstRow;
                int numColumns = this.mNumColumns;
                int rowCount = getRowCount(this.mItemCount, numColumns);
                if (isStackFromBottom()) {
                    oddItemsOnFirstRow = (rowCount * numColumns) - this.mItemCount;
                } else {
                    oddItemsOnFirstRow = 0;
                }
                return Math.max(((((this.mFirstPosition + oddItemsOnFirstRow) / numColumns) * 100) - ((top * 100) / height)) + ((int) (((((float) this.mScrollY) / ((float) getHeight())) * ((float) rowCount)) * 100.0f)), 0);
            }
        }
        return 0;
    }

    protected int computeVerticalScrollRange() {
        int rowCount = getRowCount(this.mItemCount, this.mNumColumns);
        int result = Math.max(rowCount * 100, 0);
        if (this.mScrollY != 0) {
            return result + Math.abs((int) (((((float) this.mScrollY) / ((float) getHeight())) * ((float) rowCount)) * 100.0f));
        }
        return result;
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(OppoGridView.class.getName());
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(OppoGridView.class.getName());
    }

    public void addHeaderView(View v, Object data, boolean isSelectable) {
        if (this.mAdapter == null || ((this.mAdapter instanceof HeaderViewListAdapter) ^ 1) == 0) {
            FixedViewInfo info = new FixedViewInfo(this, null);
            info.view = v;
            info.data = data;
            info.isSelectable = isSelectable;
            this.mHeaderViewInfos.add(info);
            if (this.mAdapter != null && this.mDataSetObserver != null) {
                this.mDataSetObserver.onChanged();
                return;
            }
            return;
        }
        throw new IllegalStateException("Cannot add header view to list -- setAdapter has already been called.");
    }

    public void addHeaderView(View v) {
        addHeaderView(v, null, true);
    }

    public int getHeaderViewsCount() {
        return this.mHeaderViewInfos.size();
    }

    public boolean removeHeaderView(View v) {
        if (this.mHeaderViewInfos.size() <= 0) {
            return false;
        }
        boolean result = false;
        if (this.mAdapter != null && ((HeaderViewListAdapter) this.mAdapter).removeHeader(v)) {
            if (this.mDataSetObserver != null) {
                this.mDataSetObserver.onChanged();
            }
            result = true;
        }
        removeFixedViewInfo(v, this.mHeaderViewInfos);
        return result;
    }

    public void addFooterView(View v, Object data, boolean isSelectable) {
        FixedViewInfo info = new FixedViewInfo(this, null);
        info.view = v;
        info.data = data;
        info.isSelectable = isSelectable;
        this.mFooterViewInfos.add(info);
        if (this.mAdapter != null && this.mDataSetObserver != null) {
            this.mDataSetObserver.onChanged();
        }
    }

    public void addFooterView(View v) {
        addFooterView(v, null, true);
    }

    public int getFooterViewsCount() {
        return this.mFooterViewInfos.size();
    }

    public boolean removeFooterView(View v) {
        if (this.mFooterViewInfos.size() <= 0) {
            return false;
        }
        boolean result = false;
        if (this.mAdapter != null && ((HeaderViewListAdapter) this.mAdapter).removeFooter(v)) {
            if (this.mDataSetObserver != null) {
                this.mDataSetObserver.onChanged();
            }
            result = true;
        }
        removeFixedViewInfo(v, this.mFooterViewInfos);
        return result;
    }

    private void removeFixedViewInfo(View v, ArrayList<FixedViewInfo> where) {
        int len = where.size();
        for (int i = 0; i < len; i++) {
            if (((FixedViewInfo) where.get(i)).view == v) {
                where.remove(i);
                return;
            }
        }
    }

    private int measureHeaderFooter() {
        int childHeight = 0;
        int childState = 0;
        int headerCount = this.mHeaderViewInfos.size();
        int footerCount = this.mFooterViewInfos.size();
        int i = 0;
        while (i < this.mItemCount) {
            if (i < headerCount || i >= this.mItemCount - footerCount) {
                View child = obtainView(i, this.mIsScrap);
                LayoutParams p = (LayoutParams) child.getLayoutParams();
                if (p == null) {
                    p = (LayoutParams) generateDefaultLayoutParams();
                    child.setLayoutParams(p);
                }
                p.viewType = this.mAdapter.getItemViewType(0);
                p.forceAdd = true;
                child.measure(getChildMeasureSpec(this.mWidthMeasureSpec, this.mListPadding.left + this.mListPadding.right, p.width), getChildMeasureSpec(MeasureSpec.makeMeasureSpec(0, 0), 0, p.height));
                childHeight += child.getMeasuredHeight();
                childState = combineMeasuredStates(childState, child.getMeasuredState());
                if (this.mRecycler.shouldRecycleViewType(p.viewType)) {
                    this.mRecycler.addScrapView(child, -1);
                }
            }
            i++;
        }
        return childHeight;
    }

    private View makeHeaderFooter(int startPos, int y, boolean flow) {
        int nextLeft = this.mListPadding.left;
        View selectedView = null;
        boolean hasFocus = shouldShowSelector();
        boolean inClick = touchModeDrawsInPressedState();
        int pos = startPos;
        boolean selected = startPos == this.mSelectedPosition;
        View child = makeAndAddView(startPos, y, flow, nextLeft, selected, flow ? -1 : 0);
        if (selected && (hasFocus || inClick)) {
            selectedView = child;
        }
        this.mReferenceView = child;
        if (selectedView != null) {
            this.mReferenceViewInSelectedRow = this.mReferenceView;
        }
        return selectedView;
    }

    private int getRowCount(int count, int numColumns) {
        int headerCount = this.mHeaderViewInfos.size();
        int footerCount = this.mFooterViewInfos.size();
        return ((((((count - headerCount) - footerCount) + numColumns) - 1) / numColumns) + headerCount) + footerCount;
    }

    private void clearRecycledState(ArrayList<FixedViewInfo> infos) {
        if (infos != null) {
            int count = infos.size();
            for (int i = 0; i < count; i++) {
                LayoutParams p = (LayoutParams) ((FixedViewInfo) infos.get(i)).view.getLayoutParams();
                if (p != null) {
                    p.recycledHeaderFooter = false;
                }
            }
        }
    }

    void resetList() {
        clearRecycledState(this.mHeaderViewInfos);
        clearRecycledState(this.mFooterViewInfos);
        super.resetList();
        this.mLayoutMode = 0;
    }
}
