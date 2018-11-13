package com.color.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.android.internal.widget.ColorViewExplorerByTouchHelper;
import com.android.internal.widget.ColorViewExplorerByTouchHelper.ColorViewTalkBalkInteraction;
import java.util.ArrayList;
import java.util.List;

public class ColorMenuView extends View {
    private static final int DISPLAYDSEN = 160;
    private static int IETMNUMBERS = 5;
    static final int MAX_MENU_ITEM_COUNTS = 5;
    static final int[] STATE_ENABLED = new int[]{VIEW_STATE_ENABLED};
    static final int[] STATE_NORMAL = new int[]{-16842919, VIEW_STATE_ENABLED};
    static final int[] STATE_PRESSED = new int[]{VIEW_STATE_PRESSED, VIEW_STATE_ENABLED};
    static final int[] STATE_UNENABLED = new int[]{-16842910};
    private static final String TAG = "ColorMenuView";
    static final int VIEW_STATE_ENABLED = 16842910;
    static final int VIEW_STATE_PRESSED = 16842919;
    private int[] mBottom;
    private List<ColorItem> mColorItemList;
    private ColorViewTalkBalkInteraction mColorViewTalkBalkInteraction;
    private int mIconTextDis;
    private boolean mIsSelected;
    private int mItemCounts;
    private int mItemHeight;
    private int mItemWidth;
    private int[] mLeft;
    private int mNormalColor;
    private Runnable mOnclickRunnable;
    private int mPaddingLeft;
    private int mPaddingTop;
    private Paint mPaint;
    private int[] mRight;
    private float mScale;
    private Rect mSelectRect;
    private int mSelectedColor;
    private int mSelectedPosition;
    private float mTextSize;
    private int[] mTop;
    private final ColorViewExplorerByTouchHelper mTouchHelper;

    public ColorMenuView(Context context) {
        this(context, null);
    }

    public ColorMenuView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mColorItemList = new ArrayList();
        this.mItemCounts = 0;
        this.mSelectRect = new Rect();
        this.mIsSelected = false;
        this.mSelectedPosition = -1;
        this.mTextSize = 30.0f;
        this.mScale = 0.0f;
        this.mOnclickRunnable = new Runnable() {
            public void run() {
            }
        };
        this.mColorViewTalkBalkInteraction = new ColorViewTalkBalkInteraction() {
            public void getItemBounds(int position, Rect rect) {
                ColorMenuView.this.getRect(position, rect);
            }

            public void performAction(int virtualViewId, int actiontype, boolean resolvePara) {
                ((ColorItem) ColorMenuView.this.mColorItemList.get(ColorMenuView.this.mSelectedPosition)).getOnItemClickListener().OnColorMenuItemClick(ColorMenuView.this.mSelectedPosition);
            }

            public int getCurrentPosition() {
                return ColorMenuView.this.mSelectedPosition;
            }

            public int getItemCounts() {
                return ColorMenuView.this.mColorItemList.size();
            }

            public int getVirtualViewAt(float x, float y) {
                return ColorMenuView.this.selectedIndex(x, y);
            }

            public CharSequence getItemDescription(int virtualViewId) {
                return ((ColorItem) ColorMenuView.this.mColorItemList.get(virtualViewId)).getText();
            }

            public CharSequence getClassName() {
                return null;
            }

            public int getDisablePosition() {
                return -1;
            }
        };
        this.mPaint = new Paint();
        this.mPaint.setTextAlign(Align.CENTER);
        this.mPaint.setAntiAlias(true);
        this.mSelectedColor = getResources().getColor(201720855);
        this.mNormalColor = getResources().getColor(201720856);
        this.mTextSize = (float) ((int) getResources().getDimension(201655409));
        this.mPaddingLeft = (int) getResources().getDimension(201655424);
        this.mPaddingTop = (int) getResources().getDimension(201655425);
        this.mItemHeight = (int) getResources().getDimension(201655426);
        this.mItemWidth = (int) getResources().getDimension(201655427);
        this.mIconTextDis = (int) getResources().getDimension(201655428);
        this.mPaint.setTextSize(this.mTextSize);
        this.mScale = (float) context.getResources().getDisplayMetrics().densityDpi;
        this.mTouchHelper = new ColorViewExplorerByTouchHelper(this);
        this.mTouchHelper.setColorViewTalkBalkInteraction(this.mColorViewTalkBalkInteraction);
        setAccessibilityDelegate(this.mTouchHelper);
        setImportantForAccessibility(1);
    }

    public void setColorItem(List<ColorItem> dl) {
        this.mColorItemList = dl;
        int size = dl.size();
        if (size > 5) {
            this.mItemCounts = 5;
            this.mColorItemList = this.mColorItemList.subList(0, 5);
        } else {
            this.mItemCounts = size;
        }
        for (int i = 0; i < this.mItemCounts; i++) {
            initStateListDrawable(i);
        }
        IETMNUMBERS = this.mItemCounts;
        this.mLeft = new int[this.mItemCounts];
        this.mTop = new int[this.mItemCounts];
        this.mRight = new int[this.mItemCounts];
        this.mBottom = new int[this.mItemCounts];
        this.mTouchHelper.invalidateRoot();
    }

    private void initStateListDrawable(int i) {
        Drawable drawable = ((ColorItem) this.mColorItemList.get(i)).getIcon();
        StateListDrawable statelistDrawable = new StateListDrawable();
        drawable.setState(STATE_PRESSED);
        statelistDrawable.addState(STATE_PRESSED, drawable.getCurrent());
        drawable.setState(STATE_ENABLED);
        statelistDrawable.addState(STATE_ENABLED, drawable.getCurrent());
        drawable.setState(STATE_UNENABLED);
        statelistDrawable.addState(STATE_UNENABLED, drawable.getCurrent());
        drawable.setState(STATE_NORMAL);
        statelistDrawable.addState(STATE_NORMAL, drawable.getCurrent());
        ((ColorItem) this.mColorItemList.get(i)).setIcon(statelistDrawable);
        ((ColorItem) this.mColorItemList.get(i)).getIcon().setCallback(this);
        clearState();
    }

    protected void drawableStateChanged() {
        if (this.mSelectedPosition >= 0 && this.mSelectedPosition < this.mItemCounts) {
            Drawable drawable = ((ColorItem) this.mColorItemList.get(this.mSelectedPosition)).getIcon();
            if (drawable != null && drawable.isStateful()) {
                drawable.setState(getDrawableState());
            }
        }
        super.drawableStateChanged();
    }

    protected boolean verifyDrawable(Drawable who) {
        super.verifyDrawable(who);
        return true;
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.mPaddingLeft = (int) getResources().getDimension(201655424);
        if (IETMNUMBERS < 5) {
            this.mPaddingLeft = ((getWidth() / IETMNUMBERS) - this.mItemWidth) / 2;
        }
        for (int i = 0; i < this.mItemCounts; i++) {
            getRect(i, this.mSelectRect);
            ColorItem ci = (ColorItem) this.mColorItemList.get(i);
            ci.getIcon().setBounds(this.mSelectRect);
            ci.getIcon().draw(canvas);
            if (this.mSelectedPosition == i && this.mIsSelected) {
                this.mPaint.setColor(this.mSelectedColor);
            } else {
                this.mPaint.setColor(this.mNormalColor);
            }
            canvas.drawText(ci.getText(), (float) ((this.mPaddingLeft + (this.mItemWidth / 2)) + ((getWidth() / IETMNUMBERS) * i)), ((float) ((this.mPaddingTop + this.mItemHeight) + this.mIconTextDis)) + (this.mTextSize / 2.0f), this.mPaint);
        }
    }

    private void getRect(int index, Rect rect) {
        this.mLeft[index] = this.mPaddingLeft + ((getWidth() / IETMNUMBERS) * index);
        this.mTop[index] = this.mPaddingTop;
        this.mRight[index] = this.mItemWidth + this.mLeft[index];
        this.mBottom[index] = this.mPaddingTop + this.mItemHeight;
        rect.set(this.mLeft[index], this.mTop[index], this.mRight[index], this.mBottom[index]);
    }

    private int selectedIndex(float eventX, float eventY) {
        int position = (int) (eventX / ((float) (getWidth() / IETMNUMBERS)));
        if (position < this.mItemCounts) {
            return position;
        }
        return -2;
    }

    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        int pointerCount = event.getPointerCount();
        int eventY = (int) event.getY();
        int eventX = (int) event.getX();
        boolean inMenuView = false;
        if (((float) eventY) < ((float) this.mItemHeight) * (this.mScale / 160.0f) && eventY > 0) {
            inMenuView = true;
        }
        switch (event.getAction()) {
            case 0:
                if (this.mSelectedPosition >= 0 && eventX > this.mLeft[this.mSelectedPosition] && eventX < this.mRight[this.mSelectedPosition]) {
                    this.mIsSelected = true;
                }
                return true;
            case 1:
                if (this.mSelectedPosition >= 0 && inMenuView) {
                    ((ColorItem) this.mColorItemList.get(this.mSelectedPosition)).getOnItemClickListener().OnColorMenuItemClick(this.mSelectedPosition);
                    this.mTouchHelper.sendEventForVirtualView(this.mSelectedPosition, 1);
                }
                clearState();
                return false;
            default:
                return true;
        }
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        Object obj = 1;
        float eventX = event.getX();
        float eventY = event.getY();
        if (event.getPointerCount() == 1 && eventY >= 0.0f) {
            switch (event.getAction()) {
                case 0:
                    this.mSelectedPosition = selectedIndex(event.getX(), event.getY());
                    if (this.mSelectedPosition < 0 || eventX <= ((float) this.mLeft[this.mSelectedPosition]) || eventX >= ((float) this.mRight[this.mSelectedPosition])) {
                        obj = null;
                    }
                    if (obj == null) {
                        this.mSelectedPosition = -1;
                        break;
                    }
                    break;
            }
        }
        clearState();
        return super.dispatchTouchEvent(event);
    }

    private void clearState() {
        for (ColorItem ci : this.mColorItemList) {
            Drawable d = ci.getIcon();
            if (d != null && d.isStateful()) {
                d.setState(STATE_NORMAL);
            }
        }
        this.mIsSelected = false;
        this.mSelectedPosition = -1;
        invalidate();
    }

    public void clearAccessibilityFocus() {
        if (this.mTouchHelper != null) {
            this.mTouchHelper.clearFocusedVirtualView();
        }
    }

    boolean restoreAccessibilityFocus(int position) {
        if (position < 0 || position >= this.mItemCounts) {
            return false;
        }
        if (this.mTouchHelper != null) {
            this.mTouchHelper.setFocusedVirtualView(position);
        }
        return true;
    }

    protected boolean dispatchHoverEvent(MotionEvent event) {
        if (this.mTouchHelper.dispatchHoverEvent(event)) {
            return true;
        }
        return super.dispatchHoverEvent(event);
    }
}
