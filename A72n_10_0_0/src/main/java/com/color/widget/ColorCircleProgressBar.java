package com.color.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import com.color.util.ColorContextUtil;
import com.oppo.internal.R;
import java.util.ArrayList;

public class ColorCircleProgressBar extends View {
    public static final int ACCURACY = 2;
    private static final int ALPHA_SHOW_DURATION = 360;
    private static final float BASE_PROGRESS_POINT_ALPHA = 0.215f;
    public static final int DEFAULT_TYPE = 0;
    public static final int LARGE_TYPE = 2;
    public static final int MEDIUM_TYPE = 1;
    private static final int ONE_CIRCLE_DEGREE = 360;
    public static final int ORIGINAL_ANGLE = -90;
    private static final int PROGRESS_POINT_COUNT = 360;
    private static final String TAG = "ColorCircleProgressBar";
    private static final int TIMEOUT_SEND_ACCESSIBILITY_EVENT = 10;
    private final boolean DEBUG;
    private float arcRadius;
    private AccessibilityEventSender mAccessibilityEventSender;
    private RectF mArcRect;
    private Paint mBackGroundPaint;
    private float mCenterX;
    private float mCenterY;
    private Context mContext;
    private int mCurrentStepProgress;
    private int mHalfStrokeWidth;
    private int mHalfWidth;
    private int mHeight;
    private AccessibilityManager mManager;
    private int mMax;
    private ArrayList<ProgressPoint> mPointList;
    private int mPointRadius;
    private int mPreStepProgress;
    private int mProgress;
    private int mProgressBarBgCircleColor;
    private int mProgressBarColor;
    private int mProgressBarType;
    private Paint mProgressPaint;
    private float mStepDegree;
    private int mStrokeDefaultWidth;
    private int mStrokeLargeWidth;
    private int mStrokeMediumWidth;
    private int mStrokeWidth;
    private int mWidth;

    public ColorCircleProgressBar(Context context) {
        this(context, null);
    }

    public ColorCircleProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 201393382);
    }

    public ColorCircleProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.DEBUG = false;
        this.mWidth = 0;
        this.mHeight = 0;
        this.mProgressBarType = 0;
        this.mStrokeWidth = 0;
        this.mPointRadius = 0;
        this.mMax = 100;
        this.mProgress = 0;
        this.mCurrentStepProgress = 0;
        this.mPreStepProgress = -1;
        this.mStepDegree = 1.0f;
        this.mPointList = new ArrayList<>();
        this.mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.colorCircleProgressBar, defStyleAttr, 0);
        int length = getResources().getDimensionPixelSize(201655396);
        this.mWidth = a.getDimensionPixelSize(5, length);
        this.mHeight = a.getDimensionPixelSize(6, length);
        this.mProgressBarType = a.getInteger(4, 0);
        int defaultColor = ColorContextUtil.getAttrColor(context, 201392730);
        this.mProgressBarBgCircleColor = a.getColor(3, ColorContextUtil.getAttrColor(context, 201392910));
        this.mProgressBarColor = a.getColor(0, defaultColor);
        this.mProgress = a.getInteger(1, this.mProgress);
        this.mMax = a.getInteger(2, this.mMax);
        a.recycle();
        this.mStrokeDefaultWidth = context.getResources().getDimensionPixelSize(201655393);
        this.mStrokeMediumWidth = context.getResources().getDimensionPixelSize(201655719);
        this.mStrokeLargeWidth = context.getResources().getDimensionPixelSize(201655542);
        this.mStrokeWidth = this.mStrokeDefaultWidth;
        int i = this.mProgressBarType;
        if (1 == i) {
            this.mStrokeWidth = this.mStrokeMediumWidth;
        } else if (2 == i) {
            this.mStrokeWidth = this.mStrokeLargeWidth;
        }
        this.mPointRadius = this.mStrokeWidth >> 1;
        this.mCenterX = (float) (this.mWidth >> 1);
        this.mCenterY = (float) (this.mHeight >> 1);
        init();
    }

    private void init() {
        if (Build.VERSION.SDK_INT > 16 && getImportantForAccessibility() == 0) {
            setImportantForAccessibility(1);
        }
        initBackgroundPaint();
        initProgressPaint();
        setProgress(this.mProgress);
        setMax(this.mMax);
        this.mManager = (AccessibilityManager) this.mContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
    }

    private void initProgressPaint() {
        this.mProgressPaint = new Paint(1);
        this.mProgressPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.mProgressPaint.setColor(this.mProgressBarColor);
        this.mProgressPaint.setStyle(Paint.Style.STROKE);
        this.mProgressPaint.setStrokeWidth((float) this.mStrokeWidth);
        this.mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    private void initBackgroundPaint() {
        this.mBackGroundPaint = new Paint(1);
        this.mBackGroundPaint.setColor(this.mProgressBarBgCircleColor);
        this.mBackGroundPaint.setStyle(Paint.Style.STROKE);
    }

    private void verifyProgress() {
        int i = this.mMax;
        if (i > 0) {
            this.mCurrentStepProgress = (int) (((float) this.mProgress) / (((float) i) / 360.0f));
            if (360 - this.mCurrentStepProgress < 2) {
                this.mCurrentStepProgress = 360;
            }
            this.mPreStepProgress = this.mCurrentStepProgress;
        } else {
            this.mCurrentStepProgress = 0;
            this.mPreStepProgress = 0;
        }
        invalidate();
    }

    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        drawBackgroudCicle(canvas);
        canvas.save();
        int i = this.mHalfWidth;
        canvas.rotate(-90.0f, (float) i, (float) i);
        canvas.drawArc(this.mArcRect, 0.0f, (float) this.mCurrentStepProgress, false, this.mProgressPaint);
        canvas.restore();
    }

    private void drawBackgroudCicle(Canvas canvas) {
        this.mBackGroundPaint.setStrokeWidth((float) this.mStrokeWidth);
        int i = this.mHalfWidth;
        canvas.drawCircle((float) i, (float) i, this.arcRadius, this.mBackGroundPaint);
    }

    public void setProgress(int progress) {
        Log.i(TAG, "setProgress: " + progress);
        if (progress < 0) {
            progress = 0;
        }
        if (progress > this.mMax) {
            progress = this.mMax;
        }
        if (progress != this.mProgress) {
            this.mProgress = progress;
        }
        verifyProgress();
        onProgressRefresh();
    }

    public int getProgress() {
        return this.mProgress;
    }

    public void setMax(int max) {
        if (max < 0) {
            max = 0;
        }
        if (max != this.mMax) {
            this.mMax = max;
            if (this.mProgress > max) {
                this.mProgress = max;
            }
        }
        verifyProgress();
    }

    public int getMax() {
        return this.mMax;
    }

    /* access modifiers changed from: package-private */
    public void onProgressRefresh() {
        AccessibilityManager accessibilityManager = this.mManager;
        if (accessibilityManager != null && accessibilityManager.isEnabled() && this.mManager.isTouchExplorationEnabled()) {
            scheduleAccessibilityEventSender();
        }
    }

    private void scheduleAccessibilityEventSender() {
        AccessibilityEventSender accessibilityEventSender = this.mAccessibilityEventSender;
        if (accessibilityEventSender == null) {
            this.mAccessibilityEventSender = new AccessibilityEventSender();
        } else {
            removeCallbacks(accessibilityEventSender);
        }
        postDelayed(this.mAccessibilityEventSender, 10);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(this.mWidth, this.mHeight);
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDetachedFromWindow() {
        AccessibilityEventSender accessibilityEventSender = this.mAccessibilityEventSender;
        if (accessibilityEventSender != null) {
            removeCallbacks(accessibilityEventSender);
        }
        super.onDetachedFromWindow();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public Parcelable onSaveInstanceState() {
        SavedState ss = new SavedState(super.onSaveInstanceState());
        ss.mProgress = this.mProgress;
        return ss;
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onSizeChanged(int i, int i1, int i2, int i3) {
        super.onSizeChanged(i, i1, i2, i3);
        this.mHalfStrokeWidth = this.mStrokeWidth / 2;
        this.mHalfWidth = getWidth() / 2;
        int i4 = this.mHalfWidth;
        this.arcRadius = (float) (i4 - this.mHalfStrokeWidth);
        float f = this.arcRadius;
        this.mArcRect = new RectF(((float) i4) - f, ((float) i4) - f, ((float) i4) + f, ((float) i4) + f);
    }

    @Override // android.view.View
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setProgress(ss.mProgress);
        requestLayout();
    }

    /* access modifiers changed from: private */
    public class AccessibilityEventSender implements Runnable {
        private AccessibilityEventSender() {
        }

        public void run() {
            ColorCircleProgressBar.this.sendAccessibilityEvent(4);
        }
    }

    /* access modifiers changed from: package-private */
    public static class SavedState extends View.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            /* class com.color.widget.ColorCircleProgressBar.SavedState.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int mProgress;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.mProgress = ((Integer) in.readValue(null)).intValue();
        }

        @Override // android.view.View.BaseSavedState, android.os.Parcelable, android.view.AbsSavedState
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeValue(Integer.valueOf(this.mProgress));
        }

        public String toString() {
            return "ColorCircleProgressBar.SavedState { " + Integer.toHexString(System.identityHashCode(this)) + " mProgress = " + this.mProgress + " }";
        }
    }

    private class ProgressPoint {
        float currentAlpha;

        public ProgressPoint() {
        }

        public float getCurrentAlpha() {
            return this.currentAlpha;
        }

        public void setCurrentAlpha(float currentAlpha2) {
            this.currentAlpha = currentAlpha2;
        }
    }
}
