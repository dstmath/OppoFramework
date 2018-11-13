package com.color.widget;

import android.R;
import android.animation.Animator;
import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import com.color.util.ColorChangeTextUtil;
import com.color.util.ColorLog;
import com.color.widget.ColorBottomMenuCallback.Updater;
import java.util.ArrayList;
import java.util.List;

public abstract class ColorBottomMenuView extends View implements ColorBottomMenuCallback {
    private static final int AVERAGE_WEIGHT = 5;
    private static final float DISABLED_DIM_AMOUNT = 0.3f;
    private static final int MAX_ALPHA = 255;
    private static final int MAX_ITEM_COUNT = 5;
    private static final int MENU_DISABLED = 32;
    private static final int MENU_ENABLED = 0;
    private static final int MENU_ENABLED_MASK = 32;
    private static final float MENU_ITEM_BIG = 0.56f;
    private static final int MENU_ITEM_COUNT_FIVE = 5;
    private static final int MENU_ITEM_COUNT_FOUR = 4;
    private static final int MENU_ITEM_COUNT_ONE = 1;
    private static final int MENU_ITEM_COUNT_THREE = 3;
    private static final int MENU_ITEM_COUNT_TWO = 2;
    private static final float MENU_ITEM_MIDDLE = 0.33f;
    private static final float MENU_ITEM_NORMAL = 0.3f;
    private static final float MENU_ITEM_TEXT_WIDTH_BIG = 0.27f;
    private static final float MENU_ITEM_TEXT_WIDTH_MIDDLE = 0.21f;
    private static final float MENU_ITEM_TEXT_WIDTH_NORMAL = 0.16f;
    private static final int MENU_PFLAG_DRAWABLE_STATE_DIRTY = 1024;
    private static final int MENU_PFLAG_PREPRESSED = 33554432;
    private static final int MENU_PFLAG_PRESSED = 16384;
    private static final int MENU_PFLAG_SELECTED = 4;
    private static final int MENU_VIEW_DISPLAY_NORMAL = 1;
    private static final int MENU_VIEW_DISPLAY_TEXT = 2;
    private static final int[][] MENU_VIEW_SETS;
    private static final int MENU_VIEW_STATE_ACCELERATED = 64;
    private static final int MENU_VIEW_STATE_ACTIVATED = 32;
    private static final int MENU_VIEW_STATE_DRAG_CAN_ACCEPT = 256;
    private static final int MENU_VIEW_STATE_DRAG_HOVERED = 512;
    private static final int MENU_VIEW_STATE_ENABLED = 8;
    private static final int MENU_VIEW_STATE_FOCUSED = 4;
    private static final int MENU_VIEW_STATE_HOVERED = 128;
    private static final int[] MENU_VIEW_STATE_IDS = new int[]{16842909, 1, 16842913, 2, 16842908, 4, 16842910, 8, 16842919, 16, 16843518, 32, 16843547, MENU_VIEW_STATE_ACCELERATED, 16843623, MENU_VIEW_STATE_HOVERED, 16843624, 256, 16843625, 512};
    private static final int MENU_VIEW_STATE_PRESSED = 16;
    private static final int MENU_VIEW_STATE_SELECTED = 2;
    private static final int[][][] MENU_VIEW_STATE_SETS;
    private static final int MENU_VIEW_STATE_WINDOW_FOCUSED = 1;
    private static final int MENU_VIEW_STYLEABLE_LENGTH = R.styleable.ViewDrawableStates.length;
    private static final char NEW_LINE = '\n';
    private static final float TEXT_SIZE_SCALE = 0.88f;
    private int mActivePointerId;
    private final Updater mAnimateUpdater;
    private final Drawable mBackgroundDrawable;
    private final int mBackgroundHeight;
    private int mButtonTextSize;
    private int mCachedWidth;
    private Context mContext;
    protected final DrawItems mCurrItems;
    private final ColorStateList mDefaultTabTextColor;
    private final ColorStateList mDefaultTextColor;
    private int mDefaultTextSize;
    private final float mDensity;
    private final Updater mDirectUpdater;
    private int mDisplayMode;
    private final int mDoubleLinesDifferentSize;
    private final int mDoubleLinesIconMarginTop;
    private final int mDoubleLinesTextMarginTop;
    protected int mDownTouchedPosition;
    private boolean mHasPerformedLongPress;
    private final int mIconHeight;
    private final int mIconWidth;
    private boolean mIsUpdateLocked;
    private int mMaxTextWidth;
    private final int mNavExtraWidth;
    private final int mNavKeyWidth;
    protected final DrawItems mNextItems;
    private final int mOffsetX;
    private CheckForLongPress mPendingCheckForLongPress;
    private CheckForTap mPendingCheckForTap;
    private PerformClick mPerformClick;
    private final int mSingleLineDifferentSize;
    private int mSingleLineIconMarginTop;
    private int mSingleLineTextMarginTop;
    private int mTabButtonLargeTextSize;
    private int mTabButtonMiddleTextSize;
    private int mTabButtonSmallTextSize;
    private int mTabButtonTextSize;
    private int mTabMaxTextWidth;
    private int mTabTextSize;
    protected final Class<?> mTagClass;
    protected ColorStateList mTextColor;
    private final int mTextPadding;
    private UnsetPressedState mUnsetPressedState;
    private final UpdateAdapter mUpdateAdapter;
    private Animator mUpdateAnimator;
    private ColorStateList mUserTextColor;
    private int mUserTextSize;

    public class DrawItem {
        private final Rect mBounds = new Rect();
        private int mDifferentSize = 0;
        private int mIconMarginTop = 0;
        private float mIconY = ((float) ColorBottomMenuView.this.mBackgroundHeight);
        private boolean mIsBoundsChanged = true;
        private int[] mMenuIconState = new int[ColorBottomMenuView.MENU_VIEW_STYLEABLE_LENGTH];
        private final MenuItem mMenuItem;
        private int mMenuPrivateFlags = 0;
        private int mMenuViewFlags = 0;
        private int mNewLinePos = -1;
        private int mPosition = -1;
        private int mTextMarginTop = 0;
        private final TextPaint mTextPaint = new TextPaint(1);

        public DrawItem(MenuItem menuItem, int position) {
            this.mMenuItem = menuItem;
            this.mPosition = position;
            this.mTextPaint.setAntiAlias(true);
            int textSize = ColorBottomMenuView.this.mUserTextSize;
            if (textSize == 0) {
                textSize = ColorBottomMenuView.this.mDefaultTextSize;
            }
            this.mTextPaint.setTypeface(Typeface.DEFAULT);
            this.mTextPaint.setTextSize((float) textSize);
            this.mTextPaint.density = ColorBottomMenuView.this.mDensity;
            setTop(0);
            setBottom(ColorBottomMenuView.this.mBackgroundHeight);
        }

        public TextPaint getTextPaint() {
            return this.mTextPaint;
        }

        public MenuItem getMenuItem() {
            return this.mMenuItem;
        }

        public int getPosition() {
            return this.mPosition;
        }

        public boolean contains(int x, int y) {
            return this.mBounds.contains(x, y);
        }

        public void setLeft(int left) {
            this.mBounds.left = left;
        }

        public int getLeft() {
            return this.mBounds.left;
        }

        public void setRight(int right) {
            this.mBounds.right = right;
        }

        public int getRight() {
            return this.mBounds.right;
        }

        public void setTop(int top) {
            this.mBounds.top = top;
        }

        public int getTop() {
            return this.mBounds.top;
        }

        public void setBottom(int bottom) {
            this.mBounds.bottom = bottom;
        }

        public int getBottom() {
            return this.mBounds.bottom;
        }

        public void setBounds(Rect bounds) {
            this.mBounds.set(bounds);
        }

        public Rect getBounds() {
            return this.mBounds;
        }

        public void setIconY(float y) {
            this.mIconY = y;
        }

        public float getIconY() {
            return this.mIconY;
        }

        public void setNewLinePos(int pos) {
            this.mNewLinePos = pos;
        }

        public int getNewLinePos() {
            return this.mNewLinePos;
        }

        public void setDifferentSize(int different) {
            this.mDifferentSize = different;
        }

        public int getDifferentSize() {
            return this.mDifferentSize;
        }

        public void setIconMarginTop(int margin) {
            this.mIconMarginTop = margin;
        }

        public int getIconMarginTop() {
            return this.mIconMarginTop;
        }

        public void setTextMarginTop(int margin) {
            this.mTextMarginTop = margin;
        }

        public int getTextMarginTop() {
            return this.mTextMarginTop;
        }

        public int getMenuViewFlags() {
            return this.mMenuViewFlags;
        }

        public void setMenuViewFlags(int flags, int mask) {
            this.mMenuViewFlags = (this.mMenuViewFlags & (~mask)) | (flags & mask);
        }

        private boolean hasMenuViewFlags(int flags, int mask) {
            return (this.mMenuViewFlags & mask) == flags;
        }

        public int getMenuPrivateFlags() {
            return this.mMenuPrivateFlags;
        }

        public void setMenuPrivateFlags(int flags) {
            this.mMenuPrivateFlags = flags;
        }

        public void addMenuPrivateFlags(int flags) {
            this.mMenuPrivateFlags |= flags;
        }

        public void removeMenuPrivateFlags(int flags) {
            this.mMenuPrivateFlags &= ~flags;
        }

        private boolean hasMenuPrivateFlags(int flags) {
            return (this.mMenuPrivateFlags & flags) == flags;
        }

        public int[] getMenuIconState() {
            return this.mMenuIconState;
        }

        public void setMenuIconState(int[] state) {
            this.mMenuIconState = state;
        }

        public boolean isBoundsChanged() {
            return this.mIsBoundsChanged;
        }

        public void setBoundsChanged(boolean changed) {
            this.mIsBoundsChanged = changed;
        }
    }

    public class DrawItems extends ArrayList<DrawItem> {
        public void copyFrom(DrawItems drawItems) {
            clear();
            addAll(drawItems);
        }
    }

    private abstract class ItemStateRunnable implements Runnable {
        private int mPosition = -1;

        public ItemStateRunnable(int position) {
            setPosition(position);
        }

        public void setPosition(int position) {
            this.mPosition = position;
        }

        public int getPosition() {
            return this.mPosition;
        }

        public void run() {
            setPosition(-1);
        }
    }

    private class PerformClick extends ItemStateRunnable {
        public PerformClick(int position) {
            super(position);
        }

        public void run() {
            ColorLog.d("log.key.bottom_menu.press", ColorBottomMenuView.this.mTagClass, new Object[]{"PerformClick : run"});
            ColorBottomMenuView.this.performClick(getPosition());
            super.run();
        }
    }

    private class UnsetPressedState extends ItemStateRunnable {
        public UnsetPressedState(int position) {
            super(position);
        }

        public void run() {
            DrawItem drawItem = ColorBottomMenuView.this.getDrawItem(getPosition());
            if (drawItem != null) {
                ColorLog.d("log.key.bottom_menu.press", ColorBottomMenuView.this.mTagClass, new Object[]{"UnsetPressedState : run"});
                ColorBottomMenuView.this.setItemPressedInternal(drawItem, false, true);
            }
            super.run();
        }
    }

    private class UpdateAdapter implements Updater {
        private Updater mUpdater;

        /* synthetic */ UpdateAdapter(ColorBottomMenuView this$0, UpdateAdapter -this1) {
            this();
        }

        private UpdateAdapter() {
            this.mUpdater = null;
        }

        public Animator getUpdater(int currCount, int nextCount) {
            if (visibleFirst() || nextCount > 0) {
                ColorBottomMenuView.this.setVisibility(0);
            } else {
                ColorBottomMenuView.this.setVisibility(8);
            }
            if (this.mUpdater != null) {
                return this.mUpdater.getUpdater(currCount, nextCount);
            }
            return null;
        }

        public boolean visibleFirst() {
            if (this.mUpdater != null) {
                return this.mUpdater.visibleFirst();
            }
            return false;
        }

        public void setUpdater(Updater updater) {
            this.mUpdater = updater;
        }
    }

    abstract boolean performItemClick(MenuItem menuItem);

    static {
        int NUM_BITS = MENU_VIEW_STATE_IDS.length / 2;
        if (NUM_BITS != MENU_VIEW_STYLEABLE_LENGTH) {
            throw new IllegalStateException("MENU_VIEW_STATE_IDS array length does not match ViewDrawableStates style array");
        }
        int i;
        int j;
        int[] orderedIds = new int[MENU_VIEW_STATE_IDS.length];
        for (i = 0; i < MENU_VIEW_STYLEABLE_LENGTH; i++) {
            int menuViewState = R.styleable.ViewDrawableStates[i];
            for (j = 0; j < MENU_VIEW_STATE_IDS.length; j += 2) {
                if (MENU_VIEW_STATE_IDS[j] == menuViewState) {
                    orderedIds[i * 2] = menuViewState;
                    orderedIds[(i * 2) + 1] = MENU_VIEW_STATE_IDS[j + 1];
                }
            }
        }
        MENU_VIEW_STATE_SETS = new int[(1 << NUM_BITS)][][];
        MENU_VIEW_SETS = new int[(1 << NUM_BITS)][];
        for (i = 0; i < MENU_VIEW_SETS.length; i++) {
            MENU_VIEW_SETS[i] = new int[Integer.bitCount(i)];
            int pos = 0;
            for (j = 0; j < orderedIds.length; j += 2) {
                if ((orderedIds[j + 1] & i) != 0) {
                    int pos2 = pos + 1;
                    MENU_VIEW_SETS[i][pos] = orderedIds[j];
                    pos = pos2;
                }
            }
        }
    }

    public ColorBottomMenuView(Context context) {
        this(context, null);
    }

    public ColorBottomMenuView(Context context, AttributeSet attrs) {
        this(context, attrs, 201393240);
    }

    public ColorBottomMenuView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        this.mTagClass = getClass();
        this.mNextItems = new DrawItems();
        this.mCurrItems = new DrawItems();
        this.mTextColor = null;
        this.mDownTouchedPosition = -1;
        this.mAnimateUpdater = new ColorBottomMenuAnimator(this);
        this.mDirectUpdater = new DirectUpdater(this, null);
        this.mUpdateAdapter = new UpdateAdapter(this, null);
        this.mUserTextColor = null;
        this.mUserTextSize = 0;
        this.mCachedWidth = 0;
        this.mHasPerformedLongPress = false;
        this.mUnsetPressedState = null;
        this.mPendingCheckForTap = null;
        this.mPendingCheckForLongPress = null;
        this.mPerformClick = null;
        this.mUpdateAnimator = null;
        this.mIsUpdateLocked = false;
        this.mActivePointerId = -1;
        setWillNotDraw(false);
        this.mContext = context;
        int textSize = getResources().getDimensionPixelSize(201655385);
        int tabButtonTextSize = getResources().getDimensionPixelSize(201655517);
        int backgroundHeight = getResources().getDimensionPixelSize(201655386);
        int maxIconSize = getResources().getDimensionPixelSize(201655436);
        this.mMaxTextWidth = getResources().getDimensionPixelSize(201655505);
        this.mTabMaxTextWidth = this.mMaxTextWidth;
        this.mSingleLineDifferentSize = getResources().getDimensionPixelSize(201655389);
        this.mDoubleLinesDifferentSize = getResources().getDimensionPixelSize(201655392);
        this.mSingleLineIconMarginTop = getResources().getDimensionPixelSize(201655387);
        this.mDoubleLinesIconMarginTop = getResources().getDimensionPixelSize(201655390);
        this.mSingleLineTextMarginTop = getResources().getDimensionPixelSize(201655388);
        this.mDoubleLinesTextMarginTop = getResources().getDimensionPixelSize(201655391);
        this.mDensity = getResources().getDisplayMetrics().density;
        TypedArray a = context.obtainStyledAttributes(attrs, oppo.R.styleable.ColorBottomMenuView, defStyle, 0);
        this.mDefaultTextSize = a.getDimensionPixelSize(0, textSize);
        this.mTabButtonLargeTextSize = a.getDimensionPixelSize(5, tabButtonTextSize);
        this.mTabButtonMiddleTextSize = getResources().getDimensionPixelSize(201655515);
        this.mTabButtonSmallTextSize = getResources().getDimensionPixelSize(201655516);
        float fontScale = getResources().getConfiguration().fontScale;
        this.mTabButtonLargeTextSize = (int) ColorChangeTextUtil.getSuitableFontSize((float) this.mTabButtonLargeTextSize, fontScale, 2);
        this.mTabButtonMiddleTextSize = (int) ColorChangeTextUtil.getSuitableFontSize((float) this.mTabButtonMiddleTextSize, fontScale, 2);
        this.mTabButtonSmallTextSize = (int) ColorChangeTextUtil.getSuitableFontSize((float) this.mTabButtonSmallTextSize, fontScale, 2);
        this.mDefaultTextSize = (int) ColorChangeTextUtil.getSuitableFontSize((float) this.mDefaultTextSize, fontScale, 2);
        this.mDefaultTextColor = a.getColorStateList(1);
        this.mDefaultTabTextColor = a.getColorStateList(6);
        this.mBackgroundHeight = a.getDimensionPixelSize(2, backgroundHeight);
        this.mBackgroundDrawable = a.getDrawable(3);
        a.recycle();
        if (this.mBackgroundDrawable != null) {
            setBackgroundDrawable(this.mBackgroundDrawable);
        }
        this.mIconWidth = maxIconSize;
        this.mIconHeight = maxIconSize;
        this.mNavKeyWidth = getResources().getDimensionPixelSize(201655448);
        this.mNavExtraWidth = getResources().getDimensionPixelSize(201655449);
        this.mTabButtonTextSize = this.mTabButtonSmallTextSize;
        this.mTextPadding = getResources().getDimensionPixelSize(201655554);
        this.mButtonTextSize = this.mDefaultTextSize;
        this.mOffsetX = getResources().getDimensionPixelSize(201655468);
        setMenuUpdateMode(0);
        setImportantForAccessibility(1);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthMeasureSpec, this.mBackgroundHeight);
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int count = this.mCurrItems.size();
        if (count > 0) {
            boolean isPortrait = isPortraitOriention();
            int measuredWidth = getMeasuredWidth();
            int itemWidth = getItemWidth(count);
            getWidthForPlainText(count);
            getDisplayTextSize(count);
            int itemHeight = getMeasuredHeight();
            for (int i = 0; i < count; i++) {
                DrawItem drawItem = (DrawItem) this.mCurrItems.get(i);
                if (drawItem.isBoundsChanged() || this.mCachedWidth != measuredWidth) {
                    updateItemBounds(drawItem, itemWidth, itemHeight, i, count, isPortrait);
                }
                if (drawItem.getMenuItem().getIcon() != null) {
                    onDrawText(canvas, i, drawItem, onDrawIcon(canvas, i, drawItem));
                }
                if (drawItem.getMenuItem().getIcon() == null) {
                    onDrawTextOnly(canvas, i, drawItem);
                }
            }
            this.mCachedWidth = measuredWidth;
        }
    }

    private int getItemWidth(int count) {
        this.mDisplayMode = getDisplayMode(count);
        switch (this.mDisplayMode) {
            case 1:
                int itemWidth;
                if (count > 3 || !isPortraitOriention()) {
                    itemWidth = getMeasuredWidth() / getIconWeight(count);
                } else {
                    itemWidth = this.mNavKeyWidth;
                }
                this.mMaxTextWidth = itemWidth - (this.mTextPadding * 2);
                return itemWidth;
            case 2:
                return getWidthForPlainText(count);
            default:
                return 0;
        }
    }

    private int getWidthForPlainText(int count) {
        int itemWidth;
        int measuredWidth = getMeasuredWidth();
        switch (count) {
            case 1:
                itemWidth = (int) (((float) measuredWidth) * MENU_ITEM_BIG);
                this.mTabMaxTextWidth = itemWidth;
                this.mTabButtonTextSize = this.mTabButtonLargeTextSize;
                break;
            case 2:
                itemWidth = (int) (((float) measuredWidth) * MENU_ITEM_MIDDLE);
                this.mTabMaxTextWidth = (int) (((float) measuredWidth) * MENU_ITEM_TEXT_WIDTH_BIG);
                this.mTabButtonTextSize = this.mTabButtonLargeTextSize;
                break;
            case 3:
                itemWidth = (int) (((float) measuredWidth) * 0.3f);
                this.mTabMaxTextWidth = (int) (((float) measuredWidth) * MENU_ITEM_TEXT_WIDTH_BIG);
                this.mTabButtonTextSize = this.mTabButtonLargeTextSize;
                break;
            case 4:
                this.mTabMaxTextWidth = (int) (((float) measuredWidth) * MENU_ITEM_TEXT_WIDTH_MIDDLE);
                itemWidth = measuredWidth / count;
                this.mTabButtonTextSize = this.mTabButtonMiddleTextSize;
                break;
            default:
                itemWidth = measuredWidth / count;
                this.mTabMaxTextWidth = itemWidth;
                this.mTabButtonTextSize = this.mTabButtonSmallTextSize;
                break;
        }
        this.mTabMaxTextWidth -= this.mTextPadding * 2;
        return itemWidth;
    }

    private int getDisplayMode(int count) {
        int iconNumber = 0;
        int textNumber = 0;
        for (int i = 0; i < count; i++) {
            DrawItem drawItem = (DrawItem) this.mCurrItems.get(i);
            if (drawItem.getMenuItem().getIcon() != null) {
                iconNumber++;
            }
            if (drawItem.getMenuItem().getTitle() != null) {
                textNumber++;
            }
        }
        if (textNumber == count && iconNumber == 0) {
            return 2;
        }
        return 1;
    }

    public boolean isPortraitOriention() {
        if (this.mContext instanceof Activity) {
            Display display = ((Activity) this.mContext).getWindowManager().getDefaultDisplay();
            return display.getWidth() <= display.getHeight();
        } else {
            return getResources().getConfiguration().orientation == 1;
        }
    }

    private void onDrawTextOnly(Canvas canvas, int position, DrawItem drawItem) {
        String text = (String) drawItem.getMenuItem().getTitle();
        if (!TextUtils.isEmpty(text)) {
            drawItem.mTextPaint.setTextSize((float) this.mTabTextSize);
            FontMetricsInt fmi = drawItem.mTextPaint.getFontMetricsInt();
            int textWidthTemp = (int) drawItem.mTextPaint.measureText(text);
            canvas.drawText(getDisplayText(text, drawItem, this.mTabMaxTextWidth), (float) (drawItem.getLeft() + (((drawItem.getRight() - drawItem.getLeft()) - (textWidthTemp <= this.mTabMaxTextWidth ? textWidthTemp : this.mTabMaxTextWidth)) / 2)), (float) ((((drawItem.getTextMarginTop() + (drawItem.getTextMarginTop() + getMeasuredHeight())) - fmi.ascent) - fmi.descent) / 2), drawItem.mTextPaint);
        }
    }

    private String getDisplayText(String rawString, DrawItem drawItem, int maxWidth) {
        int breakIndex = drawItem.mTextPaint.breakText(rawString, true, (float) maxWidth, null);
        if (breakIndex == rawString.length()) {
            return rawString;
        }
        return rawString.substring(0, breakIndex - 1) + "...";
    }

    private void getDisplayTextSize(int count) {
        this.mButtonTextSize = this.mDefaultTextSize;
        this.mTabTextSize = this.mTabButtonTextSize;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (isMultiPointerEvent(event)) {
            return true;
        }
        int x = (int) event.getX();
        int y = (int) event.getY();
        int action = event.getActionMasked();
        int position = getTouchedPosition(x, y);
        switch (action) {
            case 1:
            case 6:
                onTouchUp(event, position);
                break;
            case 3:
                onTouchCancel(event, position);
                break;
        }
        if (isItemEnabled(position)) {
            switch (action) {
                case 0:
                    onTouchDown(event, position);
                    break;
                case 2:
                    onTouchMove(event, position);
                    break;
            }
            return true;
        }
        resetDownTouchedPosition(position);
        return true;
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeRunnables();
        if (this.mUpdateAnimator != null) {
            this.mUpdateAnimator.cancel();
        }
    }

    public void updateMenuScrollPosition(int index, float offset) {
        int currCount = this.mCurrItems.size();
        int nextCount = this.mNextItems.size();
        if (currCount > 0 || nextCount > 0) {
            if (currCount <= 0) {
                setTranslationY(((float) this.mBackgroundHeight) * (1.0f - offset));
            } else if (nextCount <= 0) {
                setTranslationY(((float) this.mBackgroundHeight) * (1.0f - offset));
            } else {
                boolean needsInvalidate = updateIconY(offset, index);
            }
            invalidate();
        }
    }

    public void updateMenuScrollState(int state) {
        ColorLog.d("log.key.bottom_menu.update", this.mTagClass, new Object[]{"updateMenuScrollState : state=", Integer.valueOf(state)});
        switch (state) {
            case 0:
                setMenuUpdateMode(0);
                if (this.mNextItems.size() <= 0) {
                    setVisibility(8);
                    this.mCurrItems.clear();
                }
                setTranslationY(0.0f);
                return;
            case 1:
                resetNextItems();
                return;
            case 2:
                if (this.mNextItems.size() > 0) {
                    setVisibility(0);
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void updateMenuScrollData() {
        ColorLog.d("log.key.bottom_menu.update", this.mTagClass, new Object[]{"updateMenuScrollData"});
        updateCurrItems();
    }

    public void setMenuUpdateMode(int updateMode) {
        ColorLog.d("log.key.bottom_menu.update", this.mTagClass, new Object[]{"------------------setUpdateMode=", Integer.valueOf(updateMode)});
        switch (updateMode) {
            case 0:
                this.mUpdateAdapter.setUpdater(this.mDirectUpdater);
                return;
            case 1:
                this.mUpdateAdapter.setUpdater(this.mAnimateUpdater);
                return;
            default:
                this.mUpdateAdapter.setUpdater(null);
                return;
        }
    }

    public void lockMenuUpdate() {
        ColorLog.d("log.key.bottom_menu.update", this.mTagClass, new Object[]{"------------------lockMenuUpdate"});
        this.mIsUpdateLocked = true;
    }

    public void unlockMenuUpdate() {
        ColorLog.d("log.key.bottom_menu.update", this.mTagClass, new Object[]{"------------------unlockMenuUpdate"});
        this.mIsUpdateLocked = false;
    }

    public void update(List<MenuItem> menuItems, boolean cleared) {
        if (this.mUpdateAnimator != null) {
            this.mUpdateAnimator.end();
        }
        this.mUpdateAnimator = getUpdater(menuItems, cleared);
        if (this.mUpdateAnimator != null) {
            this.mUpdateAnimator.addListener(new 1(this));
            this.mUpdateAnimator.start();
        }
    }

    public void setTabTextColor(ColorStateList textColor) {
        if (textColor != null) {
            this.mUserTextColor = textColor;
        }
    }

    public void setTabTextSize(int textSize) {
        if (textSize != 0) {
            this.mUserTextSize = textSize;
        }
    }

    public void setItemEnabled(int position, boolean enabled) {
        setItemEnabledInternal(getDrawItem(position), enabled, true);
    }

    public boolean isItemEnabled(int position) {
        return isItemEnabledInternal(getDrawItem(position));
    }

    public void setItemPressed(int position, boolean pressed) {
        setItemPressedInternal(getDrawItem(position), pressed, true);
    }

    public boolean isItemPressed(int position) {
        return isItemPressedInternal(getDrawItem(position));
    }

    public void setItemSelected(int position, boolean selected) {
        setItemSelectedInternal(getDrawItem(position), selected, true);
    }

    public boolean isItemSelected(int position) {
        return isItemSelectedInternal(getDrawItem(position));
    }

    public Rect copyItemBounds(int position) {
        return copyItemBoundsInternal(getDrawItem(position));
    }

    public Animator getUpdater(List<MenuItem> menuItems, boolean cleared) {
        updateNextItems(menuItems);
        Animator anim = null;
        if (!this.mIsUpdateLocked) {
            anim = (cleared ? this.mUpdateAdapter : this.mDirectUpdater).getUpdater(this.mCurrItems.size(), this.mNextItems.size());
        }
        ColorLog.d("log.key.bottom_menu.update", this.mTagClass, new Object[]{"getUpdater : menuItems=", Integer.valueOf(getMenuCount(menuItems)), ", cleared=", Boolean.valueOf(cleared), ", mIsUpdateLocked=", Boolean.valueOf(this.mIsUpdateLocked), ", anim=", anim});
        return anim;
    }

    public boolean performClick(int position) {
        DrawItem drawItem = getDrawItem(position);
        if (drawItem == null) {
            return false;
        }
        ColorLog.d("log.key.bottom_menu.press", this.mTagClass, new Object[]{"performClick=", Integer.valueOf(position)});
        playSoundEffect(0);
        return performItemClick(drawItem.getMenuItem());
    }

    public boolean performLongClick(int position) {
        return false;
    }

    DrawItem createDrawItem(MenuItem menuItem, int position) {
        return new DrawItem(menuItem, position);
    }

    void updateNextItems(List<MenuItem> menuItems) {
        this.mNextItems.clear();
        if (menuItems != null) {
            synchronized (this.mNextItems) {
                DrawItem drawItem;
                this.mTextColor = this.mUserTextColor;
                if (this.mTextColor == null) {
                    this.mTextColor = this.mDefaultTextColor;
                }
                boolean isDoubleLines = false;
                int count = Math.min(5, menuItems.size());
                for (int i = 0; i < count; i++) {
                    initMenuState(i);
                    MenuItem menuItem = (MenuItem) menuItems.get(i);
                    Drawable icon = menuItem.getIcon();
                    String title = (String) menuItem.getTitle();
                    boolean hasTitle = TextUtils.isEmpty(title) ^ 1;
                    drawItem = createDrawItem(menuItem, i);
                    boolean checkable = menuItem.isCheckable();
                    boolean enabled = menuItem.isEnabled();
                    this.mNextItems.add(drawItem);
                    TextPaint textPaint = drawItem.getTextPaint();
                    if (!(this.mTextColor == this.mUserTextColor || checkable || icon == null)) {
                        this.mTextColor = this.mDefaultTabTextColor;
                    }
                    if (!(this.mTextColor == this.mUserTextColor || checkable || icon != null)) {
                        this.mTextColor = this.mDefaultTabTextColor;
                        textPaint.setTextSize((float) this.mTabButtonTextSize);
                    }
                    if (hasTitle && this.mTextColor != null) {
                        textPaint.setColor(this.mTextColor.getColorForState(getIconState((DrawItem) this.mNextItems.get(i)), this.mTextColor.getDefaultColor()));
                    }
                    setItemEnabledInternal(drawItem, enabled, false);
                    int newLinePos = getTextNewLinePos(title);
                    if (newLinePos >= 0) {
                        drawItem.setNewLinePos(newLinePos);
                        isDoubleLines = true;
                    }
                }
                for (DrawItem drawItem2 : this.mNextItems) {
                    drawItem2.setIconMarginTop(isDoubleLines ? this.mDoubleLinesIconMarginTop : this.mSingleLineIconMarginTop);
                    drawItem2.setTextMarginTop(isDoubleLines ? this.mDoubleLinesTextMarginTop : this.mSingleLineTextMarginTop);
                    drawItem2.setDifferentSize(isDoubleLines ? this.mDoubleLinesDifferentSize : this.mSingleLineDifferentSize);
                    if (drawItem2.getMenuItem().getIcon() == null) {
                        drawItem2.setTextMarginTop(0);
                    }
                }
                ColorLog.d("log.key.bottom_menu.item", this.mTagClass, new Object[]{"-----------------updateNextItems from MenuItems : ", Integer.valueOf(this.mNextItems.size())});
            }
        }
    }

    DrawItem getDrawItemInternal(DrawItems drawItems, int position) {
        if (position < 0 || position >= drawItems.size()) {
            return null;
        }
        return (DrawItem) drawItems.get(position);
    }

    DrawItem getDrawItem(int position) {
        return getDrawItemInternal(this.mCurrItems, position);
    }

    void setItemEnabledInternal(DrawItem drawItem, boolean enabled, boolean needsRefresh) {
        if (drawItem != null && drawItem.hasMenuViewFlags(0, 32) != enabled) {
            if (enabled) {
                drawItem.setMenuViewFlags(0, 32);
            } else {
                drawItem.setMenuViewFlags(32, 32);
            }
            if (needsRefresh) {
                refreshItemState(drawItem);
                invalidate();
            }
        }
    }

    boolean isItemEnabledInternal(DrawItem drawItem) {
        if (drawItem == null) {
            return false;
        }
        return drawItem.hasMenuViewFlags(0, 32);
    }

    void setItemPressedInternal(DrawItem drawItem, boolean pressed, boolean needsRefresh) {
        if (drawItem != null && drawItem.hasMenuPrivateFlags(16384) != pressed) {
            if (pressed) {
                drawItem.addMenuPrivateFlags(16384);
            } else {
                drawItem.removeMenuPrivateFlags(16384);
            }
            if (needsRefresh) {
                refreshItemState(drawItem);
                invalidate();
            }
        }
    }

    boolean isItemPressedInternal(DrawItem drawItem) {
        if (drawItem == null) {
            return false;
        }
        return drawItem.hasMenuPrivateFlags(16384);
    }

    void setItemSelectedInternal(DrawItem drawItem, boolean selected, boolean needsRefresh) {
        if (drawItem != null && drawItem.hasMenuPrivateFlags(4) != selected) {
            if (selected) {
                drawItem.addMenuPrivateFlags(4);
            } else {
                drawItem.removeMenuPrivateFlags(4);
            }
            if (needsRefresh) {
                refreshItemState(drawItem);
                invalidate();
            }
        }
    }

    boolean isItemSelectedInternal(DrawItem drawItem) {
        if (drawItem == null) {
            return false;
        }
        return drawItem.hasMenuPrivateFlags(4);
    }

    Rect copyItemBoundsInternal(DrawItem drawItem) {
        if (drawItem == null) {
            return null;
        }
        return new Rect(drawItem.getBounds());
    }

    void onPerformClick(int position) {
        ColorLog.d("log.key.bottom_menu.press", this.mTagClass, new Object[]{"onPerformClick"});
        updatePerformClickCallback(position);
        if (!post(this.mPerformClick)) {
            performClick(position);
        }
    }

    private boolean isMultiPointerEvent(MotionEvent event) {
        int pointerIndex = event.getActionIndex();
        if (event.getActionMasked() == 0) {
            this.mActivePointerId = event.getPointerId(pointerIndex);
            return false;
        }
        if (this.mActivePointerId == event.getPointerId(pointerIndex)) {
            return false;
        }
        return true;
    }

    private void updateItemBounds(DrawItem drawItem, int itemWidth, int itemHeight, int index, int count, boolean isPortrait) {
        if (count <= 3) {
            drawItem.setLeft(((getMeasuredWidth() - (count * itemWidth)) / 2) + (index * itemWidth));
        } else {
            drawItem.setLeft(itemWidth * index);
        }
        if (isLayoutRtl()) {
            if (count <= 3) {
                drawItem.setLeft(((getMeasuredWidth() - (count * itemWidth)) / 2) + (((count - 1) - index) * itemWidth));
            } else {
                drawItem.setLeft(getMeasuredWidth() - ((index + 1) * itemWidth));
            }
        }
        drawItem.setRight(drawItem.getLeft() + itemWidth);
        drawItem.setTop(0);
        drawItem.setBottom(itemHeight);
        drawItem.setBoundsChanged(false);
    }

    public boolean isLayoutRtl() {
        return getLayoutDirection() == 1;
    }

    private void setItemPrepressedInternal(DrawItem drawItem, boolean prepressed) {
        if (drawItem != null && drawItem.hasMenuPrivateFlags(MENU_PFLAG_PREPRESSED) != prepressed) {
            if (prepressed) {
                drawItem.addMenuPrivateFlags(MENU_PFLAG_PREPRESSED);
            } else {
                drawItem.removeMenuPrivateFlags(MENU_PFLAG_PREPRESSED);
            }
        }
    }

    private boolean isItemPrepressedInternal(DrawItem drawItem) {
        if (drawItem == null) {
            return false;
        }
        return drawItem.hasMenuPrivateFlags(MENU_PFLAG_PREPRESSED);
    }

    private boolean isItemPrepressed(int position) {
        return isItemPrepressedInternal(getDrawItem(position));
    }

    private int getItemAlpha(boolean enabled) {
        return (int) (enabled ? 255.0f : 76.5f);
    }

    private int[] onCreateIconState(DrawItem drawItem, int extraSpace) {
        int menuItemStateIndex = 0;
        int privateFlags = drawItem.getMenuPrivateFlags();
        if ((privateFlags & 16384) != 0) {
            menuItemStateIndex = 16;
        }
        if ((privateFlags & 4) != 0) {
            menuItemStateIndex |= 2;
        }
        if ((drawItem.getMenuViewFlags() & 32) == 0) {
            menuItemStateIndex |= 8;
        }
        if (hasWindowFocus()) {
            menuItemStateIndex |= 1;
        }
        int[] iconState = MENU_VIEW_STATE_SETS[drawItem.getPosition()][menuItemStateIndex];
        if (extraSpace == 0) {
            return iconState;
        }
        int[] fullState;
        if (iconState != null) {
            fullState = new int[(iconState.length + extraSpace)];
            System.arraycopy(iconState, 0, fullState, 0, iconState.length);
        } else {
            fullState = new int[extraSpace];
        }
        return fullState;
    }

    private int[] getIconState(DrawItem drawItem) {
        if ((drawItem.getMenuPrivateFlags() & 1024) != 0) {
            drawItem.setMenuIconState(onCreateIconState(drawItem, 0));
            drawItem.removeMenuPrivateFlags(1024);
        }
        return drawItem.getMenuIconState();
    }

    private void iconStateChanged(DrawItem drawItem) {
        Drawable icon = drawItem.getMenuItem().getIcon();
        int[] state = getIconState(drawItem);
        if (icon != null && icon.isStateful()) {
            icon.setState(state);
        }
    }

    private void refreshIconState(DrawItem drawItem) {
        drawItem.addMenuPrivateFlags(1024);
        iconStateChanged(drawItem);
    }

    private void refreshTextState(DrawItem drawItem) {
        if (!TextUtils.isEmpty(drawItem.getMenuItem().getTitle()) && this.mTextColor != null) {
            drawItem.getTextPaint().setColor(this.mTextColor.getColorForState(getIconState(drawItem), this.mTextColor.getDefaultColor()));
        }
    }

    private void refreshItemState(DrawItem drawItem) {
        if (drawItem != null) {
            refreshIconState(drawItem);
            refreshTextState(drawItem);
        }
    }

    private int getTextNewLinePos(String text) {
        if (text != null) {
            int length = text.length();
            for (int i = 0; i < length; i++) {
                if (NEW_LINE == text.charAt(i)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void removeRunnables() {
        removeUnsetPressCallback();
        removeTapCallback();
        removeLongPressCallback();
        removePerformClickCallback();
    }

    private void checkForLongClick(int delayOffset, int position) {
        this.mHasPerformedLongPress = false;
        updateLongPressCallback(position);
        this.mPendingCheckForLongPress.rememberWindowAttachCount();
        postDelayed(this.mPendingCheckForLongPress, (long) (ViewConfiguration.getLongPressTimeout() - delayOffset));
    }

    private void removeUnsetPressCallback() {
        if (this.mUnsetPressedState != null) {
            ColorLog.d("log.key.bottom_menu.press", this.mTagClass, new Object[]{"removeUnsetPressCallback"});
            removeCallbacks(this.mUnsetPressedState);
            DrawItem drawItem = getDrawItem(this.mUnsetPressedState.getPosition());
            if (isItemPressedInternal(drawItem)) {
                setItemPressedInternal(drawItem, false, true);
            }
            this.mUnsetPressedState.setPosition(-1);
        }
    }

    private void removeTapCallback() {
        if (this.mPendingCheckForTap != null) {
            ColorLog.d("log.key.bottom_menu.press", this.mTagClass, new Object[]{"removeTapCallback"});
            removeCallbacks(this.mPendingCheckForTap);
            setItemPrepressedInternal(getDrawItem(this.mPendingCheckForTap.getPosition()), false);
            this.mPendingCheckForTap.setPosition(-1);
        }
    }

    private void removeLongPressCallback() {
        if (this.mPendingCheckForLongPress != null) {
            ColorLog.d("log.key.bottom_menu.press", this.mTagClass, new Object[]{"removeLongPressCallback"});
            removeCallbacks(this.mPendingCheckForLongPress);
            this.mPendingCheckForLongPress.setPosition(-1);
        }
    }

    private void removePerformClickCallback() {
        if (this.mPerformClick != null) {
            ColorLog.d("log.key.bottom_menu.press", this.mTagClass, new Object[]{"removePerformClickCallback"});
            removeCallbacks(this.mPerformClick);
            this.mPerformClick.setPosition(-1);
        }
    }

    private void updateUnsetPressedCallback(int position) {
        if (this.mUnsetPressedState == null) {
            this.mUnsetPressedState = new UnsetPressedState(position);
        } else if (this.mUnsetPressedState.getPosition() != position) {
            removeUnsetPressCallback();
            this.mUnsetPressedState.setPosition(position);
        } else {
            removeCallbacks(this.mUnsetPressedState);
        }
    }

    private void updateTapCallback(int position) {
        if (this.mPendingCheckForTap == null) {
            this.mPendingCheckForTap = new CheckForTap(this, position);
        } else if (this.mPendingCheckForTap.getPosition() != position) {
            removeTapCallback();
            this.mPendingCheckForTap.setPosition(position);
        } else {
            removeCallbacks(this.mPendingCheckForTap);
        }
    }

    private void updateLongPressCallback(int position) {
        if (this.mPendingCheckForLongPress == null) {
            this.mPendingCheckForLongPress = new CheckForLongPress(this, position);
        } else if (this.mPendingCheckForLongPress.getPosition() != position) {
            removeLongPressCallback();
            this.mPendingCheckForLongPress.setPosition(position);
        } else {
            removeCallbacks(this.mPendingCheckForLongPress);
        }
    }

    private void updatePerformClickCallback(int position) {
        if (this.mPerformClick == null) {
            this.mPerformClick = new PerformClick(position);
        } else if (this.mPerformClick.getPosition() != position) {
            removePerformClickCallback();
            this.mPerformClick.setPosition(position);
        } else {
            removeCallbacks(this.mPerformClick);
        }
    }

    private void setDownTouchedPosition(int position) {
        this.mDownTouchedPosition = position;
    }

    private void resetDownTouchedPosition() {
        setDownTouchedPosition(-1);
    }

    private void resetTouchCheck() {
        setItemPressed(this.mDownTouchedPosition, false);
        removeTapCallback();
        removeLongPressCallback();
        resetDownTouchedPosition();
    }

    private void onTouchDown(MotionEvent event, int position) {
        DrawItem drawItem = getDrawItem(position);
        if (drawItem != null) {
            setDownTouchedPosition(position);
            this.mHasPerformedLongPress = false;
            setItemPrepressedInternal(drawItem, true);
            updateTapCallback(position);
            postDelayed(this.mPendingCheckForTap, (long) ViewConfiguration.getTapTimeout());
        }
    }

    private void onTouchMove(MotionEvent event, int position) {
        resetDownTouchedPosition(position);
    }

    private void resetDownTouchedPosition(int position) {
        if (this.mDownTouchedPosition != position) {
            removeTapCallback();
            DrawItem drawItem = getDrawItem(this.mDownTouchedPosition);
            if (isItemPressedInternal(drawItem)) {
                removeLongPressCallback();
                setItemPressedInternal(drawItem, false, true);
            }
        }
    }

    private void onTouchUp(MotionEvent event, int position) {
        DrawItem drawItem = getDrawItem(position);
        if (drawItem != null) {
            boolean prepressed = isItemPrepressedInternal(drawItem);
            if (isItemPressedInternal(drawItem) || prepressed) {
                if (prepressed) {
                    setItemPressedInternal(drawItem, true, true);
                }
                if (!this.mHasPerformedLongPress) {
                    removeLongPressCallback();
                    onPerformClick(position);
                }
                updateUnsetPressedCallback(position);
                if (prepressed) {
                    postDelayed(this.mUnsetPressedState, (long) ViewConfiguration.getPressedStateDuration());
                } else if (!post(this.mUnsetPressedState)) {
                    this.mUnsetPressedState.run();
                }
                removeTapCallback();
            }
        } else {
            resetTouchCheck();
        }
        resetDownTouchedPosition();
    }

    private void onTouchCancel(MotionEvent event, int position) {
        resetTouchCheck();
    }

    private void initMenuState(int index) {
        MENU_VIEW_STATE_SETS[index] = new int[MENU_VIEW_SETS.length][];
        System.arraycopy(MENU_VIEW_SETS, 0, MENU_VIEW_STATE_SETS[index], 0, MENU_VIEW_SETS.length);
    }

    private int getMenuCount(List<MenuItem> menuItems) {
        return menuItems != null ? menuItems.size() : 0;
    }

    private boolean updateIconY(float offset, int position) {
        boolean needsInvalidate = false;
        DrawItem drawItem = getDrawItem(position);
        int bottomY;
        int topY;
        if (drawItem != null) {
            bottomY = drawItem.getBottom();
            if (bottomY != 0) {
                topY = drawItem.getIconMarginTop();
                drawItem.setIconY(((float) topY) + (((float) (bottomY - topY)) * offset));
                if (drawItem.getMenuItem().getIcon() == null && drawItem.getMenuItem().getTitle() != null) {
                    updateTextTop(drawItem, offset);
                }
                needsInvalidate = true;
            }
            return needsInvalidate;
        }
        int count = this.mCurrItems.size();
        for (position = 0; position < count; position++) {
            drawItem = (DrawItem) this.mCurrItems.get(position);
            bottomY = drawItem.getBottom();
            if (bottomY != 0) {
                topY = drawItem.getIconMarginTop();
                drawItem.setIconY(((float) topY) + (((float) (bottomY - topY)) * offset));
                if (drawItem.getMenuItem().getIcon() == null && drawItem.getMenuItem().getTitle() != null) {
                    updateTextTop(drawItem, offset);
                }
                needsInvalidate = true;
            }
        }
        return needsInvalidate;
    }

    private void updateTextTop(DrawItem drawItem, float offset) {
        drawItem.setTextMarginTop((int) (((float) getMeasuredHeight()) * offset));
    }

    private void updateCurrItems() {
        removeRunnables();
        synchronized (this.mCurrItems) {
            this.mCurrItems.copyFrom(this.mNextItems);
            for (DrawItem drawItem : this.mCurrItems) {
                drawItem.setBoundsChanged(true);
                refreshItemState(drawItem);
            }
            ColorLog.d("log.key.bottom_menu.item", this.mTagClass, new Object[]{"-----------------updateCurrItems from NextItems : ", Integer.valueOf(this.mCurrItems.size())});
        }
        requestLayout();
    }

    private void resetNextItems() {
        synchronized (this.mNextItems) {
            this.mNextItems.copyFrom(this.mCurrItems);
            ColorLog.d("log.key.bottom_menu.item", this.mTagClass, new Object[]{"-----------------resetNextItems from CurrItems : ", Integer.valueOf(this.mNextItems.size())});
        }
    }

    private int getIconWeight(int count) {
        if (count <= 5) {
            return count;
        }
        return 5;
    }

    private int getTextLineOffset(DrawItem drawItem) {
        return 0;
    }

    private int onDrawIcon(Canvas canvas, int position, DrawItem drawItem) {
        int topTemp;
        Drawable icon = drawItem.getMenuItem().getIcon();
        int itemHeight = drawItem.getBottom() - drawItem.getTop();
        int left = drawItem.getLeft() + (((drawItem.getRight() - drawItem.getLeft()) - this.mIconWidth) / 2);
        int right = left + this.mIconWidth;
        if (TextUtils.isEmpty((String) drawItem.getMenuItem().getTitle())) {
            topTemp = (itemHeight - this.mIconHeight) / 2;
        } else {
            topTemp = (int) drawItem.getIconY();
        }
        int top = topTemp;
        int bottom = top + this.mIconHeight;
        icon.setBounds(left, top, right, bottom);
        icon.draw(canvas);
        return bottom;
    }

    private void onDrawText(Canvas canvas, int position, DrawItem drawItem, int iconBottom) {
        String text = (String) drawItem.getMenuItem().getTitle();
        if (!TextUtils.isEmpty(text)) {
            drawItem.mTextPaint.setTextSize((float) this.mButtonTextSize);
            int fontHeight = -drawItem.mTextPaint.getFontMetricsInt().top;
            int length = text.length();
            int marginTop = drawItem.getTextMarginTop();
            int diffSize = drawItem.getDifferentSize();
            int posNewLine = drawItem.getNewLinePos();
            int itemWidth = drawItem.getRight() - drawItem.getLeft();
            if (posNewLine < 0) {
                int textWidthTemp = (int) drawItem.mTextPaint.measureText(text);
                canvas.drawText(getDisplayText(text, drawItem, this.mMaxTextWidth), (float) (drawItem.getLeft() + ((itemWidth - (textWidthTemp <= this.mMaxTextWidth ? textWidthTemp : this.mMaxTextWidth)) / 2)), (float) ((((iconBottom + marginTop) + fontHeight) - diffSize) + getTextLineOffset(drawItem)), drawItem.mTextPaint);
            } else {
                String textLine1 = text.substring(0, posNewLine);
                int y = ((iconBottom + marginTop) + fontHeight) - diffSize;
                canvas.drawText(textLine1, (float) (drawItem.getLeft() + ((itemWidth - ((int) drawItem.mTextPaint.measureText(textLine1))) / 2)), (float) y, drawItem.mTextPaint);
                String textLine2 = text.substring(posNewLine + 1, length);
                canvas.drawText(textLine2, (float) (drawItem.getLeft() + ((itemWidth - ((int) drawItem.mTextPaint.measureText(textLine2))) / 2)), (float) (((y + marginTop) + fontHeight) - diffSize), drawItem.mTextPaint);
            }
        }
    }

    @OppoHook(level = OppoHookType.CHANGE_ACCESS, note = "Suying.You@Plf.SDK, 2017-12-27 : [-private] Modify for accessibility ", property = OppoRomType.ROM)
    int getTouchedPosition(int x, int y) {
        int count = this.mCurrItems.size();
        for (int i = 0; i < count; i++) {
            if (((DrawItem) this.mCurrItems.get(i)).contains(x, y)) {
                return i;
            }
        }
        return -1;
    }

    public void setRectBounds(int position, Rect rect) {
        if (position >= 0 && position < this.mCurrItems.size()) {
            DrawItem drawItem = getDrawItem(position);
            if (drawItem != null) {
                Rect itemRect = drawItem.mBounds;
                rect.set(itemRect.left, itemRect.top, itemRect.right, itemRect.bottom);
            }
        }
    }

    public CharSequence getMenuTitle(int virtualViewId) {
        if (virtualViewId < this.mCurrItems.size()) {
            DrawItem drawItem = (DrawItem) this.mCurrItems.get(virtualViewId);
            if (!(drawItem == null || drawItem.mMenuItem == null || drawItem.mMenuItem.getTitle() == null)) {
                return drawItem.mMenuItem.getTitle();
            }
        }
        return getClass().getSimpleName();
    }
}
