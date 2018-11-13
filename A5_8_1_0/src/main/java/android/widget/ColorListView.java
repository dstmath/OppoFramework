package android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AbsListView.MultiChoiceModeWrapper;
import oppo.R;

public class ColorListView extends ListView {
    private static final boolean DBG = false;
    private static final boolean DBG_MOTION = false;
    private static final int ID_CHECKBOX = 201457665;
    private static final int INVALID_SCROLL_CHOICE_POSITION = -2;
    private static final long SCROLL_CHOICE_SCROLL_DELAY = 50;
    public static final int SCROLL_HORIZONTAL_LTR = 1;
    public static final int SCROLL_HORIZONTAL_NULL = 0;
    public static final int SCROLL_HORIZONTAL_RTL = 2;
    private static final String TAG = "ColorListView";
    private AccelerateInterpolator mAccelerateInterpolator;
    private CheckBox mCheckBox;
    private CheckableListener mCheckableListener;
    private boolean mChoiceModeByLongPressEnabled;
    private int mCurPosition;
    private DecelerateInterpolator mDecelerateInterpolator;
    private Runnable mDelayedScroll;
    private int mDividerItemHeight;
    private boolean mFillDivider;
    private int mFirstPos;
    private boolean mFlag;
    private boolean mHasMoved;
    private boolean mIsNotDrawFirstLine;
    private boolean mIsNotifyCheckedStateEnabled;
    private int mItemBottom;
    private int mItemCount;
    private int mItemHeight;
    private boolean mItemToAppear;
    private int mItemTop;
    private int mLastPos;
    private int mLastPosition;
    private int mLasterPosition;
    private int mLeftOffset;
    private boolean mMultiChoice;
    private float mPointY;
    private float mPositionOffset;
    private int mReferPosition;
    private int mRightOffset;
    private int mScrollDirection;
    private ScrollMultiChoiceListener mScrollMultiChoiceListener;
    private boolean mSpringEnabled;
    private boolean mUpScroll;
    private View mViewPager;

    public interface CheckableListener {
        boolean isItemCheckable(View view, int i, long j);

        void onUncheckableItemClick(AdapterView<?> adapterView, View view, int i, long j);
    }

    class MultiChoiceModeCallbackWrapper extends MultiChoiceModeWrapper {
        private MultiChoiceModeListener mWrapped = null;

        MultiChoiceModeCallbackWrapper() {
            super(ColorListView.this);
        }

        public void setWrapped(MultiChoiceModeListener wrapped) {
            this.mWrapped = wrapped;
            super.setWrapped(wrapped);
        }

        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            if (ColorListView.this.mIsNotifyCheckedStateEnabled) {
                this.mWrapped.onItemCheckedStateChanged(mode, position, id, checked);
            }
        }
    }

    public interface ScrollMultiChoiceListener {
        void onItemTouch(int i, View view);
    }

    public ColorListView(Context context) {
        this(context, null);
    }

    public ColorListView(Context context, AttributeSet attrs) {
        this(context, attrs, 201393152);
    }

    public ColorListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mIsNotDrawFirstLine = false;
        this.mIsNotifyCheckedStateEnabled = true;
        this.mChoiceModeByLongPressEnabled = false;
        this.mFlag = false;
        this.mMultiChoice = true;
        this.mUpScroll = true;
        this.mHasMoved = false;
        this.mCheckBox = null;
        this.mLastPosition = INVALID_SCROLL_CHOICE_POSITION;
        this.mLasterPosition = INVALID_SCROLL_CHOICE_POSITION;
        this.mCheckableListener = null;
        this.mDelayedScroll = new Runnable() {
            public void run() {
                if (ColorListView.this.mUpScroll) {
                    ColorListView.this.setSelection(ColorListView.this.mCurPosition - 1);
                    return;
                }
                ColorListView.this.setSelectionFromTop(ColorListView.this.mCurPosition + 1, ((ColorListView.this.getHeight() - ColorListView.this.getPaddingTop()) - ColorListView.this.getPaddingBottom()) - ColorListView.this.mItemHeight);
            }
        };
        this.mItemToAppear = true;
        this.mScrollDirection = 0;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OppoListView, defStyle, 0);
        setSpringEnabled(a.getBoolean(0, false));
        setFillDivider(a.getBoolean(1, false));
        int dividerItemHeight = a.getInt(2, 0);
        if (dividerItemHeight == 0) {
            dividerItemHeight = getResources().getDimensionPixelOffset(201655360);
        }
        setDividerItemHeight(dividerItemHeight);
        a.recycle();
        this.mLeftOffset = getResources().getDimensionPixelOffset(201655351);
        this.mRightOffset = getResources().getDimensionPixelOffset(201655352);
        this.mItemHeight = getResources().getDimensionPixelOffset(201655360);
        this.mAccelerateInterpolator = new AccelerateInterpolator();
        this.mDecelerateInterpolator = new DecelerateInterpolator();
    }

    public void setSpringEnabled(boolean springEnable) {
        this.mSpringEnabled = springEnable;
        if (this.mSpringEnabled) {
            setOverScrollMode(0);
        } else {
            setOverScrollMode(2);
        }
    }

    public boolean isSpringEnabled() {
        return getOverScrollMode() != 2;
    }

    public void setFillDivider(boolean fillDivider) {
        this.mFillDivider = fillDivider;
    }

    public boolean getFillDivider() {
        return this.mFillDivider;
    }

    public void setDividerItemHeight(int itemHeight) {
        this.mDividerItemHeight = itemHeight;
    }

    public int getDividerItemHeight() {
        return this.mDividerItemHeight;
    }

    public void setIsNotDrawFirstLine(boolean flag) {
        this.mIsNotDrawFirstLine = flag;
    }

    public boolean getIsNotDrawFirstLine() {
        return this.mIsNotDrawFirstLine;
    }

    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        boolean isFullScreen = false;
        int count = getChildCount();
        int scrollY = getScrollY();
        View lastchild = null;
        if (count > 0) {
            lastchild = getChildAt(count - 1);
        }
        if (lastchild != null && (lastchild.getBottom() >= getHeight() || lastchild.getBottom() + 1 >= getHeight())) {
            isFullScreen = true;
        }
        if (getFillDivider() && (isFullScreen ^ 1) != 0) {
            int dividerHeight = getDividerHeight();
            boolean drawDividers = dividerHeight > 0 ? getDivider() != null : false;
            int listBottom = (getBottom() - getTop()) - getListPaddingBottom();
            int fillBottom = 0;
            int dividerItemHeight = getDividerItemHeight();
            if (count > 0) {
                fillBottom = lastchild.getBottom();
            }
            Rect bounds = new Rect();
            bounds.left = getPaddingLeft();
            bounds.right = (getRight() - getLeft()) - getPaddingRight();
            if (this.mIsNotDrawFirstLine) {
                fillBottom = (fillBottom + dividerItemHeight) + dividerHeight;
            }
            while (dividerItemHeight > 0 && fillBottom < listBottom + scrollY && drawDividers) {
                bounds.top = fillBottom;
                bounds.bottom = fillBottom + dividerHeight;
                drawDivider(canvas, bounds, 0);
                fillBottom = bounds.bottom + dividerItemHeight;
            }
        }
    }

    public void drawDivider(Canvas canvas, Rect bounds, int childIndex) {
        Drawable divider = getDivider();
        divider.setBounds(bounds);
        divider.draw(canvas);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction() & 255) {
            case 0:
                if (this.mMultiChoice && isInScrollRange(ev)) {
                    return true;
                }
            case 1:
                this.mMultiChoice = true;
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    private boolean isInScrollRange(MotionEvent ev) {
        int curPosition = pointToPosition((int) ev.getX(), (int) ev.getY());
        int xRaw = (int) ev.getRawX();
        int[] location = new int[2];
        try {
            this.mCheckBox = (CheckBox) getChildAt(curPosition - getFirstVisiblePosition()).findViewById(ID_CHECKBOX);
            this.mCheckBox.getLocationOnScreen(location);
            int mLeftBorder = location[0] - this.mLeftOffset;
            int mRightBorder = location[0] + this.mRightOffset;
            if (this.mCheckBox.getVisibility() == 0 && xRaw > mLeftBorder && xRaw < mRightBorder && curPosition > getHeaderViewsCount() - 1 && curPosition < getCount() - getFooterViewsCount()) {
                return true;
            }
            if (ev.getActionMasked() == 0) {
                this.mMultiChoice = false;
            }
            return false;
        } catch (Exception e) {
            if (ev.getActionMasked() == 0) {
                this.mMultiChoice = false;
            }
            return false;
        }
    }

    /* JADX WARNING: Missing block: B:13:0x003b, code:
            if (r12.mFlag == false) goto L_0x00d0;
     */
    /* JADX WARNING: Missing block: B:15:0x0041, code:
            if (r12.mLastPosition == r12.mCurPosition) goto L_0x00d0;
     */
    /* JADX WARNING: Missing block: B:17:0x0046, code:
            if (r12.mCurPosition == -1) goto L_0x00d0;
     */
    /* JADX WARNING: Missing block: B:19:0x004a, code:
            if (r12.mScrollMultiChoiceListener == null) goto L_0x00d0;
     */
    /* JADX WARNING: Missing block: B:20:0x004c, code:
            r12.mHasMoved = true;
            removeCallbacks(r12.mDelayedScroll);
            r12.mScrollMultiChoiceListener.onItemTouch(r12.mCurPosition, getChildAt(r12.mCurPosition - getFirstVisiblePosition()));
     */
    /* JADX WARNING: Missing block: B:21:0x0070, code:
            if (java.lang.Math.abs(r12.mCurPosition - r12.mLastPosition) <= 1) goto L_0x009e;
     */
    /* JADX WARNING: Missing block: B:23:0x0074, code:
            if (r12.mCurPosition < 0) goto L_0x009e;
     */
    /* JADX WARNING: Missing block: B:25:0x0078, code:
            if (r12.mLastPosition < 0) goto L_0x009e;
     */
    /* JADX WARNING: Missing block: B:26:0x007a, code:
            r7 = r12.mLastPosition;
            r3 = r12.mCurPosition;
     */
    /* JADX WARNING: Missing block: B:27:0x0082, code:
            if (r12.mCurPosition >= r12.mLastPosition) goto L_0x0088;
     */
    /* JADX WARNING: Missing block: B:28:0x0084, code:
            r7 = r12.mCurPosition;
            r3 = r12.mLastPosition;
     */
    /* JADX WARNING: Missing block: B:29:0x0088, code:
            r4 = r7 + 1;
     */
    /* JADX WARNING: Missing block: B:30:0x008a, code:
            if (r4 >= r3) goto L_0x009e;
     */
    /* JADX WARNING: Missing block: B:31:0x008c, code:
            r12.mScrollMultiChoiceListener.onItemTouch(r4, getChildAt(r4 - getFirstVisiblePosition()));
            r4 = r4 + 1;
     */
    /* JADX WARNING: Missing block: B:33:0x00a1, code:
            if (r12.mLastPosition == INVALID_SCROLL_CHOICE_POSITION) goto L_0x00c8;
     */
    /* JADX WARNING: Missing block: B:35:0x00aa, code:
            if (r6 >= (r12.mItemHeight + getPaddingTop())) goto L_0x00d2;
     */
    /* JADX WARNING: Missing block: B:36:0x00ac, code:
            r2 = true;
     */
    /* JADX WARNING: Missing block: B:38:0x00b9, code:
            if (r6 <= ((getHeight() - r12.mItemHeight) - getPaddingBottom())) goto L_0x00d4;
     */
    /* JADX WARNING: Missing block: B:39:0x00bb, code:
            r1 = true;
     */
    /* JADX WARNING: Missing block: B:40:0x00bc, code:
            if (r2 == false) goto L_0x00d6;
     */
    /* JADX WARNING: Missing block: B:41:0x00be, code:
            r12.mUpScroll = true;
            postDelayed(r12.mDelayedScroll, SCROLL_CHOICE_SCROLL_DELAY);
     */
    /* JADX WARNING: Missing block: B:42:0x00c8, code:
            r12.mLasterPosition = r12.mLastPosition;
            r12.mLastPosition = r12.mCurPosition;
     */
    /* JADX WARNING: Missing block: B:44:0x00d1, code:
            return true;
     */
    /* JADX WARNING: Missing block: B:45:0x00d2, code:
            r2 = false;
     */
    /* JADX WARNING: Missing block: B:46:0x00d4, code:
            r1 = false;
     */
    /* JADX WARNING: Missing block: B:47:0x00d6, code:
            if (r1 == false) goto L_0x00c8;
     */
    /* JADX WARNING: Missing block: B:48:0x00d8, code:
            r12.mUpScroll = false;
            postDelayed(r12.mDelayedScroll, SCROLL_CHOICE_SCROLL_DELAY);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onTouchEvent(MotionEvent ev) {
        if (this.mMultiChoice && isInScrollRange(ev)) {
            int n = (int) ev.getY();
            this.mCurPosition = pointToPosition((int) ev.getX(), n);
            switch (ev.getActionMasked()) {
                case 0:
                    this.mFlag = true;
                    break;
                case 1:
                    this.mLastPosition = INVALID_SCROLL_CHOICE_POSITION;
                    this.mLasterPosition = INVALID_SCROLL_CHOICE_POSITION;
                    break;
                case 2:
                    break;
            }
        }
        switch (ev.getAction() & 255) {
            case 1:
                removeCallbacks(this.mDelayedScroll);
                if (!(this.mHasMoved || this.mLastPosition == INVALID_SCROLL_CHOICE_POSITION || this.mLastPosition != this.mLasterPosition)) {
                    performItemClick(getChildAt(this.mLastPosition - getFirstVisiblePosition()), this.mLastPosition, this.mAdapter.getItemId(this.mLastPosition));
                    break;
                }
            case 3:
                break;
        }
        this.mHasMoved = false;
        this.mUpScroll = true;
        this.mLastPosition = INVALID_SCROLL_CHOICE_POSITION;
        this.mLasterPosition = INVALID_SCROLL_CHOICE_POSITION;
        this.mFlag = false;
        this.mMultiChoice = true;
        return super.onTouchEvent(ev);
    }

    public void setScrollMultiChoiceListener(ScrollMultiChoiceListener listener) {
        this.mScrollMultiChoiceListener = listener;
    }

    public void setScrollDirection(int direction) {
        if (this.mScrollDirection != direction) {
            this.mScrollDirection = direction;
            layoutChildren();
        }
        this.mItemCount = getChildCount();
        this.mFirstPos = getFirstVisiblePosition();
        this.mLastPos = getLastVisiblePosition();
        if (this.mItemCount != 0) {
            this.mItemTop = getChildAt(0).getTop();
            this.mItemBottom = getChildAt(this.mItemCount - 1).getBottom();
        }
    }

    public void setScrollDirection(int direction, View pager) {
        setScrollDirection(direction);
        this.mViewPager = pager;
        recomputePositionInParent(this);
    }

    private void recomputePositionInParent(View view) {
        this.mItemTop += view.getTop();
        this.mItemBottom += view.getTop();
        if (this.mViewPager != null) {
            ViewParent viewParent = view.getParent();
            if (viewParent instanceof View) {
                View parent = (View) viewParent;
                if (!parent.equals(this.mViewPager)) {
                    recomputePositionInParent(parent);
                }
            }
        }
    }

    public void updateHorizontalPosition(float y, float offset, boolean toAppear) {
        this.mItemToAppear = toAppear;
        updateHorizontalPosition(y, offset);
    }

    public void updateHorizontalPosition(float y, float offset) {
        if (this.mItemCount != 0) {
            if (this.mPointY != y) {
                this.mPointY = y;
                this.mReferPosition = pointToPosition(getWidth() / 2, (int) (this.mPointY - ((float) this.mItemTop)));
            }
            if (this.mReferPosition == -1) {
                if (this.mPointY < ((float) this.mItemTop)) {
                    this.mReferPosition = this.mFirstPos;
                }
                if (this.mPointY > ((float) this.mItemTop)) {
                    this.mReferPosition = this.mLastPos;
                }
            }
            this.mPositionOffset = offset;
            layoutChildren();
        }
    }

    protected int calChildrenLeftPosition(int position, int childrenLeft) {
        int childLeft = childrenLeft;
        int count;
        if (this.mPositionOffset > 0.0f) {
            count = Math.max(Math.abs(this.mReferPosition - this.mFirstPos), Math.abs(this.mReferPosition - this.mLastPos));
            if (this.mScrollDirection == 2) {
                if (this.mItemToAppear) {
                    return childrenLeft + ((int) (((this.mAccelerateInterpolator.getInterpolation(((float) Math.abs(this.mReferPosition - position)) / ((float) this.mItemCount)) * (1.0f - this.mPositionOffset)) * ((float) getWidth())) * 4.0f));
                }
                if (this.mPositionOffset > 0.6f) {
                    this.mPositionOffset = 0.6f;
                }
                return childrenLeft - ((int) (((this.mDecelerateInterpolator.getInterpolation(1.0f - (((float) Math.abs(this.mReferPosition - position)) / ((float) count))) * this.mPositionOffset) * ((float) getWidth())) / 3.0f));
            } else if (this.mScrollDirection != 1) {
                return childLeft;
            } else {
                if (this.mItemToAppear) {
                    return childrenLeft - ((int) (((this.mAccelerateInterpolator.getInterpolation(((float) Math.abs(this.mReferPosition - position)) / ((float) this.mItemCount)) * this.mPositionOffset) * ((float) getWidth())) * 4.0f));
                }
                if (this.mPositionOffset < 0.4f) {
                    this.mPositionOffset = 0.4f;
                }
                return childrenLeft + ((int) (((this.mDecelerateInterpolator.getInterpolation(1.0f - (((float) Math.abs(this.mReferPosition - position)) / ((float) count))) * (1.0f - this.mPositionOffset)) * ((float) getWidth())) / 3.0f));
            }
        } else if (this.mPositionOffset >= 0.0f) {
            return childLeft;
        } else {
            count = Math.max(Math.abs(this.mReferPosition - this.mFirstPos), Math.abs(this.mReferPosition - this.mLastPos));
            if (this.mScrollDirection == 2) {
                return childrenLeft - ((int) (((this.mDecelerateInterpolator.getInterpolation(1.0f - (((float) Math.abs(this.mReferPosition - position)) / ((float) count))) * Math.abs(this.mPositionOffset)) * ((float) getWidth())) / 6.0f));
            }
            if (this.mScrollDirection == 1) {
                return childrenLeft + ((int) (((this.mDecelerateInterpolator.getInterpolation(1.0f - (((float) Math.abs(this.mReferPosition - position)) / ((float) count))) * Math.abs(this.mPositionOffset)) * ((float) getWidth())) / 6.0f));
            }
            return childLeft;
        }
    }

    public boolean performItemClick(View view, int position, long id) {
        if (this.mChoiceMode == 3 && this.mChoiceActionMode != null) {
            if (!isItemCheckable(view, position, id)) {
                return performUncheckableItemClick(view, position, id);
            }
            playSoundEffect(0);
        }
        return super.performItemClick(view, position, id);
    }

    public void setMultiChoiceModeListener(MultiChoiceModeListener listener) {
        if (this.mMultiChoiceModeCallback == null) {
            this.mMultiChoiceModeCallback = new MultiChoiceModeCallbackWrapper();
        }
        super.setMultiChoiceModeListener(listener);
    }

    private boolean performUncheckableItemClick(View view, int position, long id) {
        if (this.mCheckableListener == null) {
            return false;
        }
        playSoundEffect(0);
        this.mCheckableListener.onUncheckableItemClick(this, view, position, id);
        if (view != null) {
            view.sendAccessibilityEvent(1);
        }
        return true;
    }

    private boolean isItemCheckable(View view, int position, long id) {
        if (this.mCheckableListener != null) {
            return this.mCheckableListener.isItemCheckable(view, position, id);
        }
        return true;
    }

    public void setCheckableListener(CheckableListener listener) {
        this.mCheckableListener = listener;
    }

    public void setNotifyCheckedStateEnabled(boolean enable) {
        this.mIsNotifyCheckedStateEnabled = enable;
    }

    boolean performLongPress(View child, int longPressPosition, long longPressId, float x, float y) {
        if (this.mChoiceModeByLongPressEnabled) {
            return super.performLongPress(child, longPressPosition, longPressId, x, y);
        }
        int temp = this.mChoiceMode;
        this.mChoiceMode = 0;
        boolean result = super.performLongPress(child, longPressPosition, longPressId, x, y);
        this.mChoiceMode = temp;
        return result;
    }

    public void enableChoiceModeByLongPress(boolean enabled) {
        this.mChoiceModeByLongPressEnabled = enabled;
    }
}
