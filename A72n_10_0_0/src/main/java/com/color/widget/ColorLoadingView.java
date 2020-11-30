package com.color.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import com.android.internal.widget.ColorViewExplorerByTouchHelper;
import com.color.util.ColorContextUtil;
import com.oppo.internal.R;

public class ColorLoadingView extends View {
    private static final boolean DEBUG = false;
    public static final int DEFAULT_TYPE = 1;
    private static final float LARGE_POINT_END_ALPHA = 1.0f;
    private static final float LARGE_POINT_START_ALPHA = 0.215f;
    public static final int LARGE_TYPE = 2;
    private static final float MEDIUM_POINT_END_ALPHA = 0.4f;
    private static final float MEDIUM_POINT_START_ALPHA = 0.1f;
    public static final int MEDIUM_TYPE = 1;
    private static final int ONE_CIRCLE_DEGREE = 360;
    private static final int ONE_CYCLE_DURATION = 960;
    private static final int PROGRESS_POINT_COUNT = 12;
    private static final String TAG = "ColorLoadingView";
    private String mAccessDescription;
    private float mCenterX;
    private float mCenterY;
    private ColorViewExplorerByTouchHelper.ColorViewTalkBalkInteraction mColorViewTalkBalkInteraction;
    private Context mContext;
    private float mCurrentDegree;
    private float mEndAlpha;
    private int mHeight;
    private int mLoadingType;
    private int mLoadingViewColor;
    private int mPointRadius;
    private float[] mPointsAlpha;
    private ValueAnimator mProgressAnimator;
    private Paint mProgressPaint;
    private float mStartAlpha;
    private float mStepDegree;
    private int mStrokeDefaultWidth;
    private int mStrokeLargeWidth;
    private int mStrokeMediumWidth;
    private int mStrokeWidth;
    private ColorViewExplorerByTouchHelper mTouchHelper;
    private int mWidth;

    public ColorLoadingView(Context context) {
        this(context, null);
    }

    public ColorLoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 201393252);
    }

    public ColorLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, 201393252, 0);
    }

    public ColorLoadingView(Context context, AttributeSet attrs, int defStyle, int styleRes) {
        super(context, attrs, defStyle);
        this.mPointsAlpha = new float[12];
        this.mWidth = 0;
        this.mHeight = 0;
        this.mLoadingType = 1;
        this.mStrokeWidth = 0;
        this.mPointRadius = 0;
        this.mStepDegree = 30.0f;
        this.mAccessDescription = null;
        this.mStartAlpha = 0.1f;
        this.mEndAlpha = 0.4f;
        this.mColorViewTalkBalkInteraction = new ColorViewExplorerByTouchHelper.ColorViewTalkBalkInteraction() {
            /* class com.color.widget.ColorLoadingView.AnonymousClass2 */
            private int mVirtualViewId = -1;

            @Override // com.android.internal.widget.ColorViewExplorerByTouchHelper.ColorViewTalkBalkInteraction
            public void getItemBounds(int position, Rect rect) {
                if (position == 0) {
                    rect.set(0, 0, ColorLoadingView.this.mWidth, ColorLoadingView.this.mHeight);
                }
            }

            @Override // com.android.internal.widget.ColorViewExplorerByTouchHelper.ColorViewTalkBalkInteraction
            public void performAction(int virtualViewId, int actiontype, boolean resolvePara) {
            }

            @Override // com.android.internal.widget.ColorViewExplorerByTouchHelper.ColorViewTalkBalkInteraction
            public int getCurrentPosition() {
                return -1;
            }

            @Override // com.android.internal.widget.ColorViewExplorerByTouchHelper.ColorViewTalkBalkInteraction
            public int getItemCounts() {
                return 1;
            }

            @Override // com.android.internal.widget.ColorViewExplorerByTouchHelper.ColorViewTalkBalkInteraction
            public int getVirtualViewAt(float x, float y) {
                if (x < 0.0f || x > ((float) ColorLoadingView.this.mWidth) || y < 0.0f || y > ((float) ColorLoadingView.this.mHeight)) {
                    return -1;
                }
                return 0;
            }

            @Override // com.android.internal.widget.ColorViewExplorerByTouchHelper.ColorViewTalkBalkInteraction
            public CharSequence getItemDescription(int virtualViewId) {
                if (ColorLoadingView.this.mAccessDescription != null) {
                    return ColorLoadingView.this.mAccessDescription;
                }
                return getClass().getSimpleName();
            }

            @Override // com.android.internal.widget.ColorViewExplorerByTouchHelper.ColorViewTalkBalkInteraction
            public CharSequence getClassName() {
                return ColorLoadingView.class.getName();
            }

            @Override // com.android.internal.widget.ColorViewExplorerByTouchHelper.ColorViewTalkBalkInteraction
            public int getDisablePosition() {
                return -1;
            }
        };
        this.mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.colorLoadingView, defStyle, 0);
        int length = getResources().getDimensionPixelSize(201655396);
        this.mWidth = a.getDimensionPixelSize(1, length);
        this.mHeight = a.getDimensionPixelSize(2, length);
        this.mLoadingType = a.getInteger(3, 1);
        this.mLoadingViewColor = a.getColor(0, ColorContextUtil.getAttrColor(context, 201392730));
        a.recycle();
        this.mStrokeDefaultWidth = context.getResources().getDimensionPixelSize(201655393);
        this.mStrokeMediumWidth = context.getResources().getDimensionPixelSize(201655719);
        this.mStrokeLargeWidth = context.getResources().getDimensionPixelSize(201655542);
        this.mStrokeWidth = this.mStrokeDefaultWidth;
        int i = this.mLoadingType;
        if (1 == i) {
            this.mStrokeWidth = this.mStrokeMediumWidth;
            this.mStartAlpha = 0.1f;
            this.mEndAlpha = 0.4f;
        } else if (2 == i) {
            this.mStrokeWidth = this.mStrokeLargeWidth;
            this.mStartAlpha = LARGE_POINT_START_ALPHA;
            this.mEndAlpha = 1.0f;
        }
        this.mPointRadius = this.mStrokeWidth >> 1;
        this.mCenterX = (float) (this.mWidth >> 1);
        this.mCenterY = (float) (this.mHeight >> 1);
        this.mTouchHelper = new ColorViewExplorerByTouchHelper(this);
        this.mTouchHelper.setColorViewTalkBalkInteraction(this.mColorViewTalkBalkInteraction);
        setAccessibilityDelegate(this.mTouchHelper);
        setImportantForAccessibility(1);
        this.mAccessDescription = this.mContext.getString(201590170);
        this.mProgressPaint = new Paint(1);
        this.mProgressPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.mProgressPaint.setColor(this.mLoadingViewColor);
    }

    private void createAnimator() {
        this.mProgressAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        this.mProgressAnimator.setDuration(960L);
        this.mProgressAnimator.setInterpolator(new LinearInterpolator());
        this.mProgressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.color.widget.ColorLoadingView.AnonymousClass1 */

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                ColorLoadingView.this.setPointsAlpha(animation.getAnimatedFraction());
                ColorLoadingView.this.invalidate();
            }
        });
        this.mProgressAnimator.setRepeatMode(1);
        this.mProgressAnimator.setRepeatCount(-1);
        this.mProgressAnimator.setInterpolator(new LinearInterpolator());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setPointsAlpha(float fraction) {
        float deltaAlpha = this.mEndAlpha - this.mStartAlpha;
        int fullCount = ((int) ((1.0f - (0.083333336f + (0.083333336f * 4.0f))) / 0.083333336f)) + 1;
        for (int i = 0; i < 12; i++) {
            if (((float) i) > ((float) fullCount) + (fraction / 0.083333336f)) {
                this.mPointsAlpha[i] = (((-deltaAlpha) / (0.083333336f * 4.0f)) * (fraction - (((float) ((i - fullCount) - 1)) * 0.083333336f))) + (deltaAlpha / 4.0f) + this.mStartAlpha;
            } else if (((float) i) * 0.083333336f <= fraction && fraction < ((float) (i + 1)) * 0.083333336f) {
                this.mPointsAlpha[i] = ((deltaAlpha / 0.083333336f) * (fraction - (((float) i) * 0.083333336f))) + this.mStartAlpha;
            } else if (((float) (i + 1)) * 0.083333336f <= fraction && fraction <= ((float) (i + 5)) * 0.083333336f) {
                this.mPointsAlpha[i] = (((-deltaAlpha) / (0.083333336f * 4.0f)) * (fraction - (((float) i) * 0.083333336f))) + (deltaAlpha / 4.0f) + this.mEndAlpha;
            }
        }
    }

    private void destroyAnimator() {
        ValueAnimator valueAnimator = this.mProgressAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
            this.mProgressAnimator.removeAllListeners();
            this.mProgressAnimator.removeAllUpdateListeners();
            this.mProgressAnimator = null;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        createAnimator();
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        destroyAnimator();
    }

    private void startAnimations() {
        ValueAnimator valueAnimator = this.mProgressAnimator;
        if (valueAnimator != null) {
            if (valueAnimator.isRunning()) {
                this.mProgressAnimator.cancel();
            }
            this.mProgressAnimator.start();
        }
    }

    private void cancelAnimations() {
        ValueAnimator valueAnimator = this.mProgressAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == 0) {
            startAnimations();
        } else {
            cancelAnimations();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(this.mWidth, this.mHeight);
    }

    @Override // android.view.View
    public void onDraw(Canvas canvas) {
        canvas.save();
        for (int i = 0; i < 12; i++) {
            float f = this.mStepDegree;
            float f2 = this.mCenterX;
            canvas.rotate(f, f2, f2);
            this.mProgressPaint.setAlpha((int) (this.mPointsAlpha[i] * 255.0f));
            float f3 = this.mCenterX;
            int i2 = this.mPointRadius;
            canvas.drawCircle(f3, (float) i2, (float) i2, this.mProgressPaint);
        }
        canvas.restore();
    }
}
