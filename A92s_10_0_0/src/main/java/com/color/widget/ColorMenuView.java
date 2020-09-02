package com.color.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.android.internal.widget.ColorViewExplorerByTouchHelper;
import java.util.ArrayList;
import java.util.List;

public class ColorMenuView extends View {
    private static final int DISPLAYDSEN = 160;
    private static int IETMNUMBERS = 5;
    static final int MAX_MENU_ITEM_COUNTS = 5;
    static final int[] STATE_ENABLED = {16842910};
    static final int[] STATE_NORMAL = {-16842919, 16842910};
    static final int[] STATE_PRESSED = {16842919, 16842910};
    static final int[] STATE_UNENABLED = {-16842910};
    private static final String TAG = "ColorMenuView";
    static final int VIEW_STATE_ENABLED = 16842910;
    static final int VIEW_STATE_PRESSED = 16842919;
    private int[] mBottom;
    /* access modifiers changed from: private */
    public List<ColorItem> mColorItemList;
    private ColorViewExplorerByTouchHelper.ColorViewTalkBalkInteraction mColorViewTalkBalkInteraction;
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
    /* access modifiers changed from: private */
    public int mSelectedPosition;
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
            /* class com.color.widget.ColorMenuView.AnonymousClass1 */

            public void run() {
            }
        };
        this.mColorViewTalkBalkInteraction = new ColorViewExplorerByTouchHelper.ColorViewTalkBalkInteraction() {
            /* class com.color.widget.ColorMenuView.AnonymousClass2 */

            @Override // com.android.internal.widget.ColorViewExplorerByTouchHelper.ColorViewTalkBalkInteraction
            public void getItemBounds(int position, Rect rect) {
                ColorMenuView.this.getRect(position, rect);
            }

            @Override // com.android.internal.widget.ColorViewExplorerByTouchHelper.ColorViewTalkBalkInteraction
            public void performAction(int virtualViewId, int actiontype, boolean resolvePara) {
                ((ColorItem) ColorMenuView.this.mColorItemList.get(ColorMenuView.this.mSelectedPosition)).getOnItemClickListener().OnColorMenuItemClick(ColorMenuView.this.mSelectedPosition);
            }

            @Override // com.android.internal.widget.ColorViewExplorerByTouchHelper.ColorViewTalkBalkInteraction
            public int getCurrentPosition() {
                return ColorMenuView.this.mSelectedPosition;
            }

            @Override // com.android.internal.widget.ColorViewExplorerByTouchHelper.ColorViewTalkBalkInteraction
            public int getItemCounts() {
                return ColorMenuView.this.mColorItemList.size();
            }

            @Override // com.android.internal.widget.ColorViewExplorerByTouchHelper.ColorViewTalkBalkInteraction
            public int getVirtualViewAt(float x, float y) {
                return ColorMenuView.this.selectedIndex(x, y);
            }

            @Override // com.android.internal.widget.ColorViewExplorerByTouchHelper.ColorViewTalkBalkInteraction
            public CharSequence getItemDescription(int virtualViewId) {
                return ((ColorItem) ColorMenuView.this.mColorItemList.get(virtualViewId)).getText();
            }

            @Override // com.android.internal.widget.ColorViewExplorerByTouchHelper.ColorViewTalkBalkInteraction
            public CharSequence getClassName() {
                return null;
            }

            @Override // com.android.internal.widget.ColorViewExplorerByTouchHelper.ColorViewTalkBalkInteraction
            public int getDisablePosition() {
                return -1;
            }
        };
        this.mPaint = new Paint();
        this.mPaint.setTextAlign(Paint.Align.CENTER);
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
        int i = 0;
        while (true) {
            int i2 = this.mItemCounts;
            if (i < i2) {
                initStateListDrawable(i);
                i++;
            } else {
                IETMNUMBERS = i2;
                this.mLeft = new int[i2];
                this.mTop = new int[i2];
                this.mRight = new int[i2];
                this.mBottom = new int[i2];
                this.mTouchHelper.invalidateRoot();
                return;
            }
        }
    }

    private void initStateListDrawable(int i) {
        Drawable drawable = this.mColorItemList.get(i).getIcon();
        StateListDrawable statelistDrawable = new StateListDrawable();
        drawable.setState(STATE_PRESSED);
        statelistDrawable.addState(STATE_PRESSED, drawable.getCurrent());
        drawable.setState(STATE_ENABLED);
        statelistDrawable.addState(STATE_ENABLED, drawable.getCurrent());
        drawable.setState(STATE_UNENABLED);
        statelistDrawable.addState(STATE_UNENABLED, drawable.getCurrent());
        drawable.setState(STATE_NORMAL);
        statelistDrawable.addState(STATE_NORMAL, drawable.getCurrent());
        this.mColorItemList.get(i).setIcon(statelistDrawable);
        this.mColorItemList.get(i).getIcon().setCallback(this);
        clearState();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void drawableStateChanged() {
        Drawable drawable;
        int i = this.mSelectedPosition;
        if (i >= 0 && i < this.mItemCounts && (drawable = this.mColorItemList.get(i).getIcon()) != null && drawable.isStateful()) {
            drawable.setState(getDrawableState());
        }
        super.drawableStateChanged();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public boolean verifyDrawable(Drawable who) {
        super.verifyDrawable(who);
        return true;
    }

    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.mPaddingLeft = (int) getResources().getDimension(201655424);
        if (IETMNUMBERS < 5) {
            this.mPaddingLeft = ((getWidth() / IETMNUMBERS) - this.mItemWidth) / 2;
        }
        for (int i = 0; i < this.mItemCounts; i++) {
            getRect(i, this.mSelectRect);
            ColorItem ci = this.mColorItemList.get(i);
            ci.getIcon().setBounds(this.mSelectRect);
            ci.getIcon().draw(canvas);
            if (this.mSelectedPosition != i || !this.mIsSelected) {
                this.mPaint.setColor(this.mNormalColor);
            } else {
                this.mPaint.setColor(this.mSelectedColor);
            }
            canvas.drawText(ci.getText(), (float) (this.mPaddingLeft + (this.mItemWidth / 2) + ((getWidth() / IETMNUMBERS) * i)), ((float) (this.mPaddingTop + this.mItemHeight + this.mIconTextDis)) + (this.mTextSize / 2.0f), this.mPaint);
        }
    }

    /* access modifiers changed from: private */
    public void getRect(int index, Rect rect) {
        this.mLeft[index] = this.mPaddingLeft + ((getWidth() / IETMNUMBERS) * index);
        int[] iArr = this.mTop;
        int i = this.mPaddingTop;
        iArr[index] = i;
        int[] iArr2 = this.mRight;
        int i2 = this.mItemWidth;
        int[] iArr3 = this.mLeft;
        iArr2[index] = i2 + iArr3[index];
        int[] iArr4 = this.mBottom;
        iArr4[index] = i + this.mItemHeight;
        rect.set(iArr3[index], iArr[index], iArr2[index], iArr4[index]);
    }

    /* access modifiers changed from: private */
    public int selectedIndex(float eventX, float eventY) {
        int position = (int) (eventX / ((float) (getWidth() / IETMNUMBERS)));
        return position < this.mItemCounts ? position : -2;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        event.getPointerCount();
        int eventY = (int) event.getY();
        int eventX = (int) event.getX();
        boolean inMenuView = false;
        if (((float) eventY) < ((float) this.mItemHeight) * (this.mScale / 160.0f) && eventY > 0) {
            inMenuView = true;
        }
        int action = event.getAction();
        if (action == 0) {
            int i = this.mSelectedPosition;
            if (i >= 0 && eventX > this.mLeft[i] && eventX < this.mRight[i]) {
                this.mIsSelected = true;
            }
            return true;
        } else if (action != 1) {
            return true;
        } else {
            int i2 = this.mSelectedPosition;
            if (i2 >= 0 && inMenuView) {
                this.mColorItemList.get(i2).getOnItemClickListener().OnColorMenuItemClick(this.mSelectedPosition);
                this.mTouchHelper.sendEventForVirtualView(this.mSelectedPosition, 1);
            }
            clearState();
            return false;
        }
    }

    @Override // android.view.View
    public boolean dispatchTouchEvent(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();
        if (event.getPointerCount() != 1 || eventY < 0.0f) {
            clearState();
        } else if (event.getAction() == 0) {
            this.mSelectedPosition = selectedIndex(event.getX(), event.getY());
            int i = this.mSelectedPosition;
            if (i < 0 || eventX <= ((float) this.mLeft[i]) || eventX >= ((float) this.mRight[i])) {
                this.mSelectedPosition = -1;
            }
        }
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

    @Override // android.view.View
    public void clearAccessibilityFocus() {
        ColorViewExplorerByTouchHelper colorViewExplorerByTouchHelper = this.mTouchHelper;
        if (colorViewExplorerByTouchHelper != null) {
            colorViewExplorerByTouchHelper.clearFocusedVirtualView();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean restoreAccessibilityFocus(int position) {
        if (position < 0 || position >= this.mItemCounts) {
            return false;
        }
        ColorViewExplorerByTouchHelper colorViewExplorerByTouchHelper = this.mTouchHelper;
        if (colorViewExplorerByTouchHelper == null) {
            return true;
        }
        colorViewExplorerByTouchHelper.setFocusedVirtualView(position);
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public boolean dispatchHoverEvent(MotionEvent event) {
        if (this.mTouchHelper.dispatchHoverEvent(event)) {
            return true;
        }
        return super.dispatchHoverEvent(event);
    }
}
