package com.android.internal.widget;

import android.content.Context;
import android.graphics.Rect;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.Window;
import com.android.internal.R;
import com.android.internal.policy.PhoneWindow;
import java.util.ArrayList;

public class DecorCaptionView extends ViewGroup implements View.OnTouchListener, GestureDetector.OnGestureListener {
    private static final String TAG = "DecorCaptionView";
    private View mCaption;
    private boolean mCheckForDragging;
    private View mClickTarget;
    private View mClose;
    private final Rect mCloseRect = new Rect();
    private View mContent;
    private int mDragSlop;
    private boolean mDragging = false;
    private GestureDetector mGestureDetector;
    private View mMaximize;
    private final Rect mMaximizeRect = new Rect();
    private boolean mOverlayWithAppContent = false;
    private PhoneWindow mOwner = null;
    private int mRootScrollY;
    private boolean mShow = false;
    private ArrayList<View> mTouchDispatchList = new ArrayList<>(2);
    private int mTouchDownX;
    private int mTouchDownY;

    public DecorCaptionView(Context context) {
        super(context);
        init(context);
    }

    public DecorCaptionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DecorCaptionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        this.mDragSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mGestureDetector = new GestureDetector(context, this);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mCaption = getChildAt(0);
    }

    public void setPhoneWindow(PhoneWindow owner, boolean show) {
        this.mOwner = owner;
        this.mShow = show;
        this.mOverlayWithAppContent = owner.isOverlayWithDecorCaptionEnabled();
        if (this.mOverlayWithAppContent) {
            this.mCaption.setBackgroundColor(0);
        }
        updateCaptionVisibility();
        this.mOwner.getDecorView().setOutlineProvider(ViewOutlineProvider.BOUNDS);
        this.mMaximize = findViewById(R.id.maximize_window);
        this.mClose = findViewById(R.id.close_window);
    }

    @Override // android.view.ViewGroup
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == 0) {
            int x = (int) ev.getX();
            int y = (int) ev.getY();
            if (this.mMaximizeRect.contains(x, y - this.mRootScrollY)) {
                this.mClickTarget = this.mMaximize;
            }
            if (this.mCloseRect.contains(x, y - this.mRootScrollY)) {
                this.mClickTarget = this.mClose;
            }
        }
        return this.mClickTarget != null;
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        if (this.mClickTarget == null) {
            return false;
        }
        this.mGestureDetector.onTouchEvent(event);
        int action = event.getAction();
        if (action == 1 || action == 3) {
            this.mClickTarget = null;
        }
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x002f, code lost:
        if (r7 != 3) goto L_0x0071;
     */
    @Override // android.view.View.OnTouchListener
    public boolean onTouch(View v, MotionEvent e) {
        int x = (int) e.getX();
        int y = (int) e.getY();
        boolean fromMouse = e.getToolType(e.getActionIndex()) == 3;
        boolean primaryButton = (e.getButtonState() & 1) != 0;
        int actionMasked = e.getActionMasked();
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    if (!this.mDragging && this.mCheckForDragging && (fromMouse || passedSlop(x, y))) {
                        this.mCheckForDragging = false;
                        this.mDragging = true;
                        startMovingTask(e.getRawX(), e.getRawY());
                    }
                }
            }
            if (this.mDragging) {
                if (actionMasked == 1) {
                    finishMovingTask();
                }
                this.mDragging = false;
                return !this.mCheckForDragging;
            }
        } else if (!this.mShow) {
            return false;
        } else {
            if (!fromMouse || primaryButton) {
                this.mCheckForDragging = true;
                this.mTouchDownX = x;
                this.mTouchDownY = y;
            }
        }
        return this.mDragging || this.mCheckForDragging;
    }

    @Override // android.view.ViewGroup
    public ArrayList<View> buildTouchDispatchChildList() {
        this.mTouchDispatchList.ensureCapacity(3);
        View view = this.mCaption;
        if (view != null) {
            this.mTouchDispatchList.add(view);
        }
        View view2 = this.mContent;
        if (view2 != null) {
            this.mTouchDispatchList.add(view2);
        }
        return this.mTouchDispatchList;
    }

    @Override // android.view.ViewGroup
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    private boolean passedSlop(int x, int y) {
        return Math.abs(x - this.mTouchDownX) > this.mDragSlop || Math.abs(y - this.mTouchDownY) > this.mDragSlop;
    }

    public void onConfigurationChanged(boolean show) {
        this.mShow = show;
        updateCaptionVisibility();
    }

    @Override // android.view.ViewGroup
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (!(params instanceof ViewGroup.MarginLayoutParams)) {
            throw new IllegalArgumentException("params " + params + " must subclass MarginLayoutParams");
        } else if (index >= 2 || getChildCount() >= 2) {
            throw new IllegalStateException("DecorCaptionView can only handle 1 client view");
        } else {
            super.addView(child, 0, params);
            this.mContent = child;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int captionHeight;
        if (this.mCaption.getVisibility() != 8) {
            measureChildWithMargins(this.mCaption, widthMeasureSpec, 0, heightMeasureSpec, 0);
            captionHeight = this.mCaption.getMeasuredHeight();
        } else {
            captionHeight = 0;
        }
        View view = this.mContent;
        if (view != null) {
            if (this.mOverlayWithAppContent) {
                measureChildWithMargins(view, widthMeasureSpec, 0, heightMeasureSpec, 0);
            } else {
                measureChildWithMargins(view, widthMeasureSpec, 0, heightMeasureSpec, captionHeight);
            }
        }
        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), View.MeasureSpec.getSize(heightMeasureSpec));
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View, android.view.ViewGroup
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int captionHeight;
        if (this.mCaption.getVisibility() != 8) {
            View view = this.mCaption;
            view.layout(0, 0, view.getMeasuredWidth(), this.mCaption.getMeasuredHeight());
            captionHeight = this.mCaption.getBottom() - this.mCaption.getTop();
            this.mMaximize.getHitRect(this.mMaximizeRect);
            this.mClose.getHitRect(this.mCloseRect);
        } else {
            captionHeight = 0;
            this.mMaximizeRect.setEmpty();
            this.mCloseRect.setEmpty();
        }
        View view2 = this.mContent;
        if (view2 != null) {
            if (this.mOverlayWithAppContent) {
                view2.layout(0, 0, view2.getMeasuredWidth(), this.mContent.getMeasuredHeight());
            } else {
                view2.layout(0, captionHeight, view2.getMeasuredWidth(), this.mContent.getMeasuredHeight() + captionHeight);
            }
        }
        this.mOwner.notifyRestrictedCaptionAreaCallback(this.mMaximize.getLeft(), this.mMaximize.getTop(), this.mClose.getRight(), this.mClose.getBottom());
    }

    private void updateCaptionVisibility() {
        this.mCaption.setVisibility(this.mShow ? 0 : 8);
        this.mCaption.setOnTouchListener(this);
    }

    private void toggleFreeformWindowingMode() {
        Window.WindowControllerCallback callback = this.mOwner.getWindowControllerCallback();
        if (callback != null) {
            try {
                callback.toggleFreeformWindowingMode();
            } catch (RemoteException e) {
                Log.e(TAG, "Cannot change task workspace.");
            }
        }
    }

    public boolean isCaptionShowing() {
        return this.mShow;
    }

    public int getCaptionHeight() {
        View view = this.mCaption;
        if (view != null) {
            return view.getHeight();
        }
        return 0;
    }

    public void removeContentView() {
        View view = this.mContent;
        if (view != null) {
            removeView(view);
            this.mContent = null;
        }
    }

    public View getCaption() {
        return this.mCaption;
    }

    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new ViewGroup.MarginLayoutParams(getContext(), attrs);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new ViewGroup.MarginLayoutParams(-1, -1);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new ViewGroup.MarginLayoutParams(p);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.ViewGroup
    public boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof ViewGroup.MarginLayoutParams;
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public void onShowPress(MotionEvent e) {
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onSingleTapUp(MotionEvent e) {
        View view = this.mClickTarget;
        if (view == this.mMaximize) {
            toggleFreeformWindowingMode();
        } else if (view == this.mClose) {
            this.mOwner.dispatchOnWindowDismissed(true, false);
        }
        return true;
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public void onLongPress(MotionEvent e) {
    }

    @Override // android.view.GestureDetector.OnGestureListener
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    public void onRootViewScrollYChanged(int scrollY) {
        View view = this.mCaption;
        if (view != null) {
            this.mRootScrollY = scrollY;
            view.setTranslationY((float) scrollY);
        }
    }
}
