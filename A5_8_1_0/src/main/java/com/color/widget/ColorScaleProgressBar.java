package com.color.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.IntArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.widget.ProgressBar;
import com.android.internal.widget.ExploreByTouchHelper;
import java.util.ArrayList;
import oppo.R;

public class ColorScaleProgressBar extends View {
    public static final int DEFAULT = 0;
    public static final int MIDDLE = 1;
    public static final int SCALETYPE = 0;
    public static final int STEPLESSTYPE = 1;
    private final boolean DEBUG;
    private final String TAG;
    private AccessibilityEventSender mAccessibilityEventSender;
    private float mAdsorbValue;
    private Context mContext;
    private Drawable mCutDrawable;
    private int mCutDrawableHeight;
    private int mCutDrawableWidth;
    private Drawable mDefaultDrawable;
    private int mDefaultDrawableHeight;
    private int mDefaultDrawableWidth;
    private int mDefaultNumber;
    private PatternExploreByTouchHelper mExploreByTouchHelper;
    private boolean mInit;
    private boolean mIsDragging;
    private boolean mIsUserSeekable;
    private ArrayList<ColorScaleProgressHelper> mItems;
    private int mMax;
    private int mNumber;
    private float mOffsetHalfWidth;
    private OnProgressChangeListener mOnProgressChangeListener;
    private OnPositionChangeListener mOnStateChangeListener;
    private int mProgress;
    private float mSectionWidth;
    private Drawable mThumbDrawable;
    private int mThumbHeight;
    private int mThumbPos;
    private Rect mThumbRect;
    private int mThumbWidth;
    private float mTouchDownX;
    private int mTouchSlop;
    private int mTrackBarPostion;
    private int mType;
    private float mUserAdsorbValue;
    private int mUserDrawableWidth;
    private int mViewHeight;
    private int mViewWidth;
    private int mWidth;

    private class AccessibilityEventSender implements Runnable {
        /* synthetic */ AccessibilityEventSender(ColorScaleProgressBar this$0, AccessibilityEventSender -this1) {
            this();
        }

        private AccessibilityEventSender() {
        }

        public void run() {
            ColorScaleProgressBar.this.announceForAccessibility(ColorScaleProgressBar.this.mProgress + "");
        }
    }

    public interface OnPositionChangeListener {
        void OnPositionChanged(ColorScaleProgressBar colorScaleProgressBar, int i);
    }

    public interface OnProgressChangeListener {
        void onProgressChanged(ColorScaleProgressBar colorScaleProgressBar, int i);

        void onStartTrackingTouch(ColorScaleProgressBar colorScaleProgressBar);

        void onStopTrackingTouch(ColorScaleProgressBar colorScaleProgressBar);
    }

    private final class PatternExploreByTouchHelper extends ExploreByTouchHelper {
        private Rect mTempRect = new Rect();
        private int mVirtualViewId = -1;

        public PatternExploreByTouchHelper(View forView) {
            super(forView);
        }

        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(host, info);
            info.addAction(AccessibilityAction.ACTION_SET_PROGRESS);
            if (ColorScaleProgressBar.this.isEnabled()) {
                int progress = ColorScaleProgressBar.this.getProgress();
                if (progress > 0) {
                    info.addAction(AccessibilityAction.ACTION_SCROLL_BACKWARD);
                }
                if (progress < ColorScaleProgressBar.this.getMax()) {
                    info.addAction(AccessibilityAction.ACTION_SCROLL_FORWARD);
                }
            }
        }

        protected int getVirtualViewAt(float x, float y) {
            if (x < 0.0f || x > ((float) ColorScaleProgressBar.this.mViewWidth) || y < 0.0f || y > ((float) ColorScaleProgressBar.this.mViewHeight)) {
                return -1;
            }
            return 0;
        }

        protected void getVisibleVirtualViews(IntArray virtualViewIds) {
            for (int i = 0; i < 1; i++) {
                virtualViewIds.add(i);
            }
        }

        protected void onPopulateEventForVirtualView(int virtualViewId, AccessibilityEvent event) {
            event.getText().add(getClass().getSimpleName());
            event.setItemCount(ColorScaleProgressBar.this.mMax);
            event.setCurrentItemIndex(ColorScaleProgressBar.this.mProgress);
        }

        public void onPopulateAccessibilityEvent(View host, AccessibilityEvent event) {
            super.onPopulateAccessibilityEvent(host, event);
        }

        protected void onPopulateNodeForVirtualView(int virtualViewId, AccessibilityNodeInfo node) {
            node.setContentDescription(ColorScaleProgressBar.this.mProgress + "");
            node.setClassName(ProgressBar.class.getName());
            node.setBoundsInParent(getBoundsForVirtualView(virtualViewId));
        }

        protected boolean onPerformActionForVirtualView(int virtualViewId, int action, Bundle arguments) {
            sendEventForVirtualView(virtualViewId, 4);
            return false;
        }

        private Rect getBoundsForVirtualView(int virtualViewId) {
            Rect bounds = this.mTempRect;
            bounds.left = 0;
            bounds.top = 0;
            bounds.right = ColorScaleProgressBar.this.mViewWidth;
            bounds.bottom = ColorScaleProgressBar.this.mViewHeight;
            return bounds;
        }
    }

    public ColorScaleProgressBar(Context context) {
        this(context, null);
    }

    public ColorScaleProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 201393256);
    }

    public ColorScaleProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.TAG = "ColorScaleProgressBar";
        this.DEBUG = false;
        this.mDefaultDrawableWidth = 0;
        this.mUserDrawableWidth = -1;
        this.mDefaultDrawableHeight = 0;
        this.mCutDrawableHeight = 0;
        this.mCutDrawableWidth = 0;
        this.mNumber = -1;
        this.mDefaultNumber = 3;
        this.mSectionWidth = 0.0f;
        this.mThumbWidth = 0;
        this.mThumbHeight = 0;
        this.mItems = new ArrayList();
        this.mTouchSlop = 0;
        this.mOffsetHalfWidth = 0.0f;
        this.mViewWidth = 0;
        this.mViewHeight = 0;
        this.mThumbPos = -1;
        this.mWidth = 150;
        this.mType = 0;
        this.mTrackBarPostion = 0;
        this.mProgress = 0;
        this.mMax = 100;
        this.mInit = false;
        this.mThumbRect = new Rect();
        this.mAdsorbValue = -1.0f;
        this.mUserAdsorbValue = -1.0f;
        this.mIsUserSeekable = true;
        this.mIsDragging = false;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorScaleProgressBar, defStyle, 0);
        this.mDefaultDrawable = a.getDrawable(1);
        this.mCutDrawable = a.getDrawable(2);
        this.mThumbDrawable = a.getDrawable(3);
        this.mViewHeight = a.getDimensionPixelSize(4, this.mWidth);
        this.mViewWidth = a.getDimensionPixelSize(0, 0);
        this.mType = a.getInteger(5, 0);
        a.recycle();
        this.mDefaultDrawableWidth = getResources().getDimensionPixelSize(201655432);
        if (this.mDefaultDrawable != null) {
            this.mDefaultDrawableHeight = this.mDefaultDrawable.getIntrinsicHeight();
        }
        this.mCutDrawableHeight = getResources().getDimensionPixelSize(201655434);
        if (this.mCutDrawable != null) {
            this.mCutDrawableWidth = this.mCutDrawable.getIntrinsicWidth();
        }
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        if (this.mThumbDrawable.isStateful()) {
            this.mThumbDrawable.setState(getDrawableState());
        }
        if (this.mViewWidth != 0) {
            this.mDefaultDrawableWidth = this.mViewWidth;
        }
        this.mExploreByTouchHelper = new PatternExploreByTouchHelper(this);
        setAccessibilityDelegate(this.mExploreByTouchHelper);
        setImportantForAccessibility(1);
        this.mContext = context;
        this.mExploreByTouchHelper.invalidateRoot();
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        Drawable thumb = this.mThumbDrawable;
        if (thumb != null && thumb.isStateful()) {
            thumb.setState(getDrawableState());
        }
        Drawable cutthumb = this.mCutDrawable;
        if (cutthumb != null && cutthumb.isStateful()) {
            cutthumb.setState(getDrawableState());
        }
        Drawable defaultthumb = this.mDefaultDrawable;
        if (defaultthumb != null && defaultthumb.isStateful()) {
            defaultthumb.setState(getDrawableState());
        }
    }

    private void initSizeinfo() {
        if (this.mUserDrawableWidth > 0) {
            this.mDefaultDrawableWidth = this.mUserDrawableWidth;
        }
        int cutTotalWidth = 0;
        if (this.mNumber >= 0) {
            this.mDefaultNumber = this.mNumber;
        }
        if (this.mThumbDrawable != null) {
            this.mThumbWidth = this.mThumbDrawable.getIntrinsicWidth();
            this.mThumbHeight = this.mThumbDrawable.getIntrinsicHeight();
        }
        this.mOffsetHalfWidth = ((float) (this.mThumbWidth - this.mCutDrawableWidth)) / 2.0f;
        if (this.mType == 0) {
            cutTotalWidth = this.mCutDrawableWidth * (this.mDefaultNumber + 1);
        } else if (this.mType == 1) {
            cutTotalWidth = this.mCutDrawableWidth * this.mDefaultNumber;
        }
        this.mViewWidth = (int) (((float) this.mDefaultDrawableWidth) + (this.mOffsetHalfWidth * 2.0f));
        float restWidth = ((float) (this.mViewWidth - cutTotalWidth)) - (this.mOffsetHalfWidth * 2.0f);
        if (this.mDefaultNumber > 0) {
            if (this.mType == 0) {
                this.mSectionWidth = restWidth / ((float) this.mDefaultNumber);
            } else if (this.mType == 1) {
                if (this.mDefaultNumber >= 1 && this.mTrackBarPostion == 1) {
                    this.mSectionWidth = restWidth / ((float) (this.mDefaultNumber + 1));
                } else if (this.mDefaultNumber == 1 && this.mTrackBarPostion == 0) {
                    this.mSectionWidth = restWidth / ((float) this.mDefaultNumber);
                } else {
                    this.mSectionWidth = restWidth / ((float) (this.mDefaultNumber - 1));
                }
            }
        } else if (this.mDefaultNumber == 0) {
            this.mSectionWidth = restWidth;
        }
        for (int i = 0; i <= this.mDefaultNumber; i++) {
            this.mItems.add(new Integer(0).intValue(), new ColorScaleProgressHelper());
        }
        if (this.mUserAdsorbValue >= 0.0f) {
            this.mAdsorbValue = this.mUserAdsorbValue;
        }
        if (this.mAdsorbValue == -1.0f) {
            this.mAdsorbValue = (float) (this.mThumbWidth / 2);
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        initSizeinfo();
        setMeasuredDimension(this.mViewWidth, this.mViewHeight);
    }

    protected void onDraw(Canvas canvas) {
        int defaultRight = 0;
        int thumbLeft = 0;
        int thumbTop = (int) (((float) (this.mViewHeight - this.mThumbHeight)) / 2.0f);
        float left = this.mOffsetHalfWidth;
        if (this.mCutDrawable != null) {
            int i;
            float cutLeft;
            float cutRight;
            int cutTop;
            int cutBottom;
            if (this.mType == 0) {
                for (i = 0; i <= this.mDefaultNumber; i++) {
                    if (getLayoutDirection() == 1) {
                        cutLeft = (((float) this.mViewWidth) - ((((float) i) * (((float) this.mCutDrawableWidth) + this.mSectionWidth)) + left)) - ((float) this.mCutDrawableWidth);
                    } else {
                        cutLeft = left + (((float) i) * (((float) this.mCutDrawableWidth) + this.mSectionWidth));
                    }
                    cutRight = cutLeft + ((float) this.mCutDrawableWidth);
                    cutTop = thumbTop + ((this.mThumbHeight - this.mCutDrawableHeight) / 2);
                    cutBottom = cutTop + this.mCutDrawableHeight;
                    ((ColorScaleProgressHelper) this.mItems.get(i)).setLeft(cutLeft);
                    ((ColorScaleProgressHelper) this.mItems.get(i)).setRight(cutRight);
                    this.mCutDrawable.setBounds((int) cutLeft, cutTop, (int) cutRight, cutBottom);
                    this.mCutDrawable.draw(canvas);
                }
            } else if (this.mType == 1 && this.mDefaultNumber != 0) {
                if (this.mDefaultNumber < 1 || this.mTrackBarPostion != 1) {
                    for (i = 0; i <= this.mDefaultNumber - 1; i++) {
                        if (getLayoutDirection() == 1) {
                            cutLeft = (((float) this.mViewWidth) - ((((float) i) * (((float) this.mCutDrawableWidth) + this.mSectionWidth)) + left)) - ((float) this.mCutDrawableWidth);
                        } else {
                            cutLeft = left + (((float) i) * (((float) this.mCutDrawableWidth) + this.mSectionWidth));
                        }
                        cutRight = cutLeft + ((float) this.mCutDrawableWidth);
                        cutTop = thumbTop + ((this.mThumbHeight - this.mCutDrawableHeight) / 2);
                        cutBottom = cutTop + this.mCutDrawableHeight;
                        ((ColorScaleProgressHelper) this.mItems.get(i)).setLeft(cutLeft);
                        ((ColorScaleProgressHelper) this.mItems.get(i)).setRight(cutRight);
                        this.mCutDrawable.setBounds((int) cutLeft, cutTop, (int) cutRight, cutBottom);
                        this.mCutDrawable.draw(canvas);
                    }
                } else {
                    for (i = 0; i <= this.mDefaultNumber - 1; i++) {
                        if (getLayoutDirection() == 1) {
                            cutLeft = (((float) this.mViewWidth) - (((((float) (i + 1)) * this.mSectionWidth) + left) + ((float) (this.mCutDrawableWidth * i)))) - ((float) this.mCutDrawableWidth);
                        } else {
                            cutLeft = ((((float) (i + 1)) * this.mSectionWidth) + left) + ((float) (this.mCutDrawableWidth * i));
                        }
                        cutRight = cutLeft + ((float) this.mCutDrawableWidth);
                        cutTop = thumbTop + ((this.mThumbHeight - this.mCutDrawableHeight) / 2);
                        cutBottom = cutTop + this.mCutDrawableHeight;
                        ((ColorScaleProgressHelper) this.mItems.get(i)).setLeft(cutLeft);
                        ((ColorScaleProgressHelper) this.mItems.get(i)).setRight(cutRight);
                        this.mCutDrawable.setBounds((int) cutLeft, cutTop, (int) cutRight, cutBottom);
                        this.mCutDrawable.draw(canvas);
                    }
                }
            }
        }
        if (this.mDefaultDrawable != null) {
            int defaultLeft = (int) left;
            int defaultTop = thumbTop + ((this.mThumbHeight - this.mDefaultDrawableHeight) / 2);
            defaultRight = defaultLeft + this.mDefaultDrawableWidth;
            this.mDefaultDrawable.setBounds(defaultLeft, defaultTop, defaultRight, defaultTop + this.mDefaultDrawableHeight);
            this.mDefaultDrawable.draw(canvas);
        }
        if (this.mThumbDrawable != null) {
            float scale;
            if (this.mDefaultNumber > 0) {
                if (this.mThumbPos >= 0 && this.mType == 0) {
                    thumbLeft = (int) (((ColorScaleProgressHelper) this.mItems.get(this.mThumbPos)).getLeft() - this.mOffsetHalfWidth);
                }
                if (this.mProgress >= 0 && this.mType == 1) {
                    scale = this.mMax > 0 ? ((float) this.mProgress) / ((float) this.mMax) : 0.0f;
                    if (getLayoutDirection() == 1) {
                        thumbLeft = (this.mViewWidth - ((int) (((float) this.mDefaultDrawableWidth) * scale))) - this.mThumbWidth;
                    } else {
                        thumbLeft = (int) (((float) this.mDefaultDrawableWidth) * scale);
                    }
                }
            } else {
                scale = this.mMax > 0 ? ((float) this.mProgress) / ((float) this.mMax) : 0.0f;
                if (getLayoutDirection() == 1) {
                    thumbLeft = (this.mViewWidth - ((int) (((float) this.mDefaultDrawableWidth) * scale))) - this.mThumbWidth;
                } else {
                    thumbLeft = (int) (((float) this.mDefaultDrawableWidth) * scale);
                }
            }
            if (thumbLeft < 0) {
                thumbLeft = 0;
            }
            if (thumbLeft > ((int) (((float) (defaultRight - this.mCutDrawableWidth)) - this.mOffsetHalfWidth))) {
                thumbLeft = (int) (((float) (defaultRight - this.mCutDrawableWidth)) - this.mOffsetHalfWidth);
            }
            this.mThumbDrawable.setBounds(thumbLeft, thumbTop, thumbLeft + this.mThumbWidth, thumbTop + this.mThumbHeight);
            this.mThumbDrawable.draw(canvas);
            this.mThumbRect = this.mThumbDrawable.getBounds();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!this.mIsUserSeekable || (isEnabled() ^ 1) != 0) {
            return false;
        }
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case 0:
                this.mTouchDownX = event.getX();
                if (!isTouchView(this.mTouchDownX, y)) {
                    return true;
                }
                break;
            case 1:
                onStopTrackingTouch();
                invalidate();
                break;
            case 2:
                if (!this.mIsDragging) {
                    onStartTrackingTouch();
                    attemptClaimDrag();
                }
                float f;
                if (this.mType == 0) {
                    f = x;
                    invalidateThumb(setTouchViewX(x));
                } else if (this.mType == 1) {
                    this.mInit = true;
                    this.mThumbRect.left = (int) x;
                    if (getLayoutDirection() == 1) {
                        this.mProgress = Math.round(((((float) this.mDefaultDrawableWidth) - x) / ((float) this.mDefaultDrawableWidth)) * ((float) this.mMax));
                    } else {
                        this.mProgress = Math.round((x / ((float) this.mDefaultDrawableWidth)) * ((float) this.mMax));
                    }
                    if (this.mDefaultNumber > 0) {
                        f = x + ((float) (this.mThumbWidth / 2));
                        if (this.mTrackBarPostion == 0) {
                            f = setTouchViewX(f);
                        }
                        invalidateStepLessThumb(f);
                    }
                    setProgressLimt(this.mProgress);
                    if (AccessibilityManager.getInstance(this.mContext).isEnabled()) {
                        scheduleAccessibilityEventSender();
                    }
                    if (this.mOnProgressChangeListener != null) {
                        this.mOnProgressChangeListener.onProgressChanged(this, this.mProgress);
                    }
                }
                invalidate();
                break;
            case 3:
                onStopTrackingTouch();
                invalidate();
                break;
        }
        return true;
    }

    private void attemptClaimDrag() {
        if (this.mParent != null) {
            this.mParent.requestDisallowInterceptTouchEvent(true);
        }
    }

    protected void onDetachedFromWindow() {
        if (this.mAccessibilityEventSender != null) {
            removeCallbacks(this.mAccessibilityEventSender);
        }
        super.onDetachedFromWindow();
    }

    private void scheduleAccessibilityEventSender() {
        if (this.mAccessibilityEventSender == null) {
            this.mAccessibilityEventSender = new AccessibilityEventSender(this, null);
        } else {
            removeCallbacks(this.mAccessibilityEventSender);
        }
        postDelayed(this.mAccessibilityEventSender, 100);
    }

    private void setProgressLimt(int progress) {
        if (progress <= 0) {
            this.mProgress = 0;
        }
        if (progress >= this.mMax) {
            this.mProgress = this.mMax;
        }
    }

    private void invalidateStepLessThumb(float upX) {
        int position = -1;
        int i = 0;
        while (i < this.mDefaultNumber - 1) {
            if (getLayoutDirection() == 1) {
                if (upX <= ((ColorScaleProgressHelper) this.mItems.get(i)).getRight() && upX >= ((ColorScaleProgressHelper) this.mItems.get(i)).getLeft()) {
                    position = i;
                } else if (upX <= ((ColorScaleProgressHelper) this.mItems.get(i)).getLeft() && upX >= ((ColorScaleProgressHelper) this.mItems.get(i + 1)).getRight()) {
                    position = i;
                }
            } else if (upX >= ((ColorScaleProgressHelper) this.mItems.get(i)).getLeft() && upX <= ((ColorScaleProgressHelper) this.mItems.get(i)).getRight()) {
                position = i;
            } else if (upX >= ((ColorScaleProgressHelper) this.mItems.get(i)).getRight() && upX <= ((ColorScaleProgressHelper) this.mItems.get(i + 1)).getLeft()) {
                position = i;
            }
            i++;
        }
        if (position >= 0 && Math.abs((upX - (Math.abs(((ColorScaleProgressHelper) this.mItems.get(position + 1)).getLeft() - ((ColorScaleProgressHelper) this.mItems.get(position + 1)).getRight()) / 2.0f)) - ((ColorScaleProgressHelper) this.mItems.get(position + 1)).getLeft()) <= this.mAdsorbValue) {
            this.mThumbPos = position + 1;
            this.mInit = false;
            getProgressValue(this.mThumbPos);
        } else if (position >= 0 && Math.abs((upX - (Math.abs(((ColorScaleProgressHelper) this.mItems.get(position)).getLeft() - ((ColorScaleProgressHelper) this.mItems.get(position)).getRight()) / 2.0f)) - ((ColorScaleProgressHelper) this.mItems.get(position)).getLeft()) <= this.mAdsorbValue) {
            this.mThumbPos = position;
            this.mInit = false;
            getProgressValue(this.mThumbPos);
        } else if (this.mTrackBarPostion != 1 || upX < 0.0f || upX > ((ColorScaleProgressHelper) this.mItems.get(0)).getLeft()) {
            if (this.mTrackBarPostion != 1 || upX < ((ColorScaleProgressHelper) this.mItems.get(this.mDefaultNumber - 1)).getRight() || upX > ((float) this.mViewWidth)) {
                this.mInit = true;
                this.mThumbRect.left = (int) (upX - ((float) (this.mThumbWidth / 2)));
                if (getLayoutDirection() == 1) {
                    this.mProgress = Math.round(((((float) this.mDefaultDrawableWidth) - ((upX - ((float) (this.mThumbWidth / 2))) + ((float) (this.mCutDrawableWidth / 2)))) / ((float) this.mDefaultDrawableWidth)) * ((float) this.mMax));
                } else {
                    this.mProgress = Math.round((((upX - ((float) (this.mThumbWidth / 2))) + ((float) (this.mCutDrawableWidth / 2))) / ((float) this.mDefaultDrawableWidth)) * ((float) this.mMax));
                }
            } else if (upX - ((ColorScaleProgressHelper) this.mItems.get(this.mDefaultNumber - 1)).getRight() <= this.mAdsorbValue) {
                this.mThumbPos = this.mDefaultNumber - 1;
                this.mInit = false;
                getProgressValue(this.mThumbPos);
            }
        } else if (((ColorScaleProgressHelper) this.mItems.get(0)).getLeft() - upX <= this.mAdsorbValue) {
            this.mThumbPos = 0;
            this.mInit = false;
            getProgressValue(this.mThumbPos);
        }
    }

    private void getProgressValue(int position) {
        float thumbMiddle = (((ColorScaleProgressHelper) this.mItems.get(position)).getLeft() - this.mOffsetHalfWidth) + ((float) (this.mCutDrawableWidth / 2));
        if (getLayoutDirection() == 1) {
            this.mProgress = Math.round(((((float) this.mDefaultDrawableWidth) - thumbMiddle) / ((float) this.mDefaultDrawableWidth)) * ((float) this.mMax));
        } else {
            this.mProgress = Math.round((thumbMiddle / ((float) this.mDefaultDrawableWidth)) * ((float) this.mMax));
        }
        if (this.mOnStateChangeListener != null) {
            this.mOnStateChangeListener.OnPositionChanged(this, position);
        }
    }

    private void invalidateThumb(float moveX) {
        int moveXInt = (int) moveX;
        for (int i = 0; i <= this.mDefaultNumber; i++) {
            int middleLeft = (int) ((((((ColorScaleProgressHelper) this.mItems.get(i)).getRight() - ((ColorScaleProgressHelper) this.mItems.get(i)).getLeft()) / 2.0f) + ((ColorScaleProgressHelper) this.mItems.get(i)).getLeft()) - (this.mSectionWidth / 2.0f));
            if (moveXInt > middleLeft && moveXInt < ((int) this.mSectionWidth) + middleLeft) {
                this.mThumbPos = i;
                break;
            }
        }
        if (this.mOnStateChangeListener != null) {
            this.mOnStateChangeListener.OnPositionChanged(this, this.mThumbPos);
        }
    }

    private boolean isTouchView(float x, float y) {
        if (x < 0.0f || x > ((float) this.mViewWidth) || y < 0.0f || y > ((float) this.mThumbHeight)) {
            return false;
        }
        return true;
    }

    private float setTouchViewX(float x) {
        int endPosition = 0;
        if (this.mType == 1) {
            endPosition = this.mDefaultNumber - 1;
        } else if (this.mType == 0) {
            endPosition = this.mDefaultNumber;
        }
        if (getLayoutDirection() == 1) {
            if (x <= ((ColorScaleProgressHelper) this.mItems.get(endPosition)).getRight()) {
                x = ((ColorScaleProgressHelper) this.mItems.get(endPosition)).getRight();
            }
            return x >= ((ColorScaleProgressHelper) this.mItems.get(0)).getLeft() ? ((ColorScaleProgressHelper) this.mItems.get(0)).getLeft() : x;
        } else {
            if (x >= ((ColorScaleProgressHelper) this.mItems.get(endPosition)).getLeft()) {
                x = ((ColorScaleProgressHelper) this.mItems.get(endPosition)).getLeft();
            }
            if (x <= ((ColorScaleProgressHelper) this.mItems.get(0)).getRight()) {
                return ((ColorScaleProgressHelper) this.mItems.get(0)).getRight();
            }
            return x;
        }
    }

    public int getThumbIndex() {
        return this.mThumbPos;
    }

    public void setThumbIndex(int index) {
        if (index >= 0) {
            this.mThumbPos = index;
        }
    }

    public void setOnPositionChangeListener(OnPositionChangeListener listener) {
        this.mOnStateChangeListener = listener;
    }

    public void setNumber(int number) {
        this.mNumber = number;
    }

    public void setTrackBarNumber(int number, int trackBarPostion) {
        this.mNumber = number;
        this.mTrackBarPostion = trackBarPostion;
    }

    public void setProgress(int progress) {
        if (progress >= 0) {
            this.mProgress = progress;
            invalidate();
        }
    }

    public int getProgress() {
        return this.mProgress;
    }

    public int getMax() {
        return this.mMax;
    }

    public void setMax(int max) {
        if (max < 0) {
            max = 0;
        }
        if (max != this.mMax) {
            this.mMax = max;
            postInvalidate();
            if (this.mProgress > max) {
                this.mProgress = max;
            }
        }
    }

    public void setOnProgressChangeListener(OnProgressChangeListener l) {
        this.mOnProgressChangeListener = l;
    }

    void onStartTrackingTouch() {
        this.mIsDragging = true;
        if (this.mOnProgressChangeListener != null) {
            this.mOnProgressChangeListener.onStartTrackingTouch(this);
        }
    }

    void onStopTrackingTouch() {
        this.mIsDragging = false;
        if (this.mOnProgressChangeListener != null) {
            this.mOnProgressChangeListener.onStopTrackingTouch(this);
        }
    }

    public void setAdsorbValue(int progress) {
        if (progress >= 0) {
            if (progress > this.mMax) {
                progress = this.mMax;
            }
            this.mUserAdsorbValue = (float) Math.round((((float) progress) / ((float) this.mMax)) * ((float) this.mDefaultDrawableWidth));
        }
    }

    public void setViewWidth(int width) {
        if (width > 0) {
            this.mUserDrawableWidth = width;
        }
    }

    public boolean onHoverEvent(MotionEvent event) {
        boolean a = AccessibilityManager.getInstance(this.mContext).isTouchExplorationEnabled();
        if (AccessibilityManager.getInstance(this.mContext).isTouchExplorationEnabled()) {
            int action = event.getAction();
            switch (action) {
                case 7:
                    event.setAction(2);
                    break;
                case 9:
                    event.setAction(0);
                    break;
                case 10:
                    event.setAction(1);
                    break;
            }
            onTouchEvent(event);
            event.setAction(action);
        }
        return super.onHoverEvent(event);
    }

    public void onInitializeAccessibilityEventInternal(AccessibilityEvent event) {
        super.onInitializeAccessibilityEventInternal(event);
        event.setItemCount(this.mMax + 0);
        event.setCurrentItemIndex(this.mProgress);
    }

    protected boolean dispatchHoverEvent(MotionEvent event) {
        return super.dispatchHoverEvent(event) | this.mExploreByTouchHelper.dispatchHoverEvent(event);
    }
}
