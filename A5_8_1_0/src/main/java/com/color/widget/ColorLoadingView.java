package com.color.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;
import com.android.internal.widget.ColorViewExplorerByTouchHelper;
import com.android.internal.widget.ColorViewExplorerByTouchHelper.ColorViewTalkBalkInteraction;
import oppo.R;

public class ColorLoadingView extends ProgressBar {
    private static final long CIRCLE_DECREASE_MAX_DEGREE = 225;
    private static final long CIRCLE_INCREASE_MAX_DEGREE = 495;
    private static final int CIRCLE_MODE = 1;
    private static final int DEFAULT_TYPE = 0;
    private static final int LARGE_TYPE = 2;
    private static final int MEDIUM_TYPE = 1;
    private static final long ONE_CIRCLE_DEGREE = 360;
    private static final long ONE_CIRCLE_TIME = 1500;
    private static final int POINT_MODE = 0;
    private static final long QUATER_CIRCLE_DEGREE = 90;
    private static final int RECT_LEFT = 0;
    private static final int RECT_TOP = 0;
    private static final long ROTATE_TIME = 3000;
    private LinearInterpolator linearInterpolator;
    private String mAccessDescription;
    private ColorViewTalkBalkInteraction mColorViewTalkBalkInteraction;
    private Context mContext;
    private long mDelayedTime;
    private float mFirstLoadingPointAngle;
    private boolean mFlagStart;
    private int mHeight;
    private final int mLargeFlag;
    private int mLoadingMode;
    private int mLoadingType;
    private final int mMediumFlag;
    private Paint mPaint;
    private int mPaintColor;
    private Path mPath;
    private Paint mPointPaint;
    private RectF mRectF;
    private float mRotateAngle;
    private float mRotateRadian;
    private float mRotationX;
    private float mRotationY;
    private float mSecondLoadingPointAlpha;
    private float mSecondLoadingPointAngle;
    private boolean mSetType;
    private long mStartTime;
    private final int mStrokeDefaultWidth;
    private final int mStrokeLargeWidth;
    private int mStrokeWidth;
    private float mThirdLoadingPointAlpha;
    private float mThirdLoadingPointAngle;
    private final ColorViewExplorerByTouchHelper mTouchHelper;
    private int mWidth;

    public ColorLoadingView(Context context) {
        this(context, null);
    }

    public ColorLoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 201393252);
    }

    public ColorLoadingView(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, 201393252, 0);
    }

    public ColorLoadingView(Context context, AttributeSet attrs, int defStyle, int styleRes) {
        super(context, attrs, defStyle);
        this.mStrokeWidth = 0;
        this.mPaintColor = 0;
        this.mRotateRadian = 0.0f;
        this.mRotateAngle = 0.0f;
        this.mLoadingType = 0;
        this.mWidth = 0;
        this.mHeight = 0;
        this.mContext = null;
        this.mMediumFlag = 1;
        this.mLargeFlag = 2;
        this.mSetType = false;
        this.mDelayedTime = 0;
        this.mAccessDescription = null;
        this.mColorViewTalkBalkInteraction = new ColorViewTalkBalkInteraction() {
            private int mVirtualViewId = -1;

            public void getItemBounds(int position, Rect rect) {
                if (position == 0) {
                    rect.set(0, 0, ColorLoadingView.this.mWidth, ColorLoadingView.this.mHeight);
                }
            }

            public void performAction(int virtualViewId, int actiontype, boolean resolvePara) {
            }

            public int getCurrentPosition() {
                return -1;
            }

            public int getItemCounts() {
                return 1;
            }

            public int getVirtualViewAt(float x, float y) {
                if (x < 0.0f || x > ((float) ColorLoadingView.this.mWidth) || y < 0.0f || y > ((float) ColorLoadingView.this.mHeight)) {
                    return -1;
                }
                return 0;
            }

            public CharSequence getItemDescription(int virtualViewId) {
                if (ColorLoadingView.this.mAccessDescription != null) {
                    return ColorLoadingView.this.mAccessDescription;
                }
                return getClass().getSimpleName();
            }

            public CharSequence getClassName() {
                return ProgressBar.class.getName();
            }

            public int getDisablePosition() {
                return -1;
            }
        };
        this.mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.colorLoadingView, defStyle, 0);
        int length = getResources().getDimensionPixelSize(201655396);
        this.mWidth = a.getDimensionPixelSize(1, length);
        this.mHeight = a.getDimensionPixelSize(2, length);
        this.mLoadingType = a.getInteger(3, 0);
        this.mStrokeDefaultWidth = context.getResources().getDimensionPixelSize(201655393);
        this.mStrokeLargeWidth = context.getResources().getDimensionPixelSize(201655542);
        this.mStrokeWidth = this.mStrokeDefaultWidth;
        if (2 == this.mLoadingType) {
            this.mStrokeWidth = this.mStrokeLargeWidth;
        }
        this.mPaintColor = a.getColor(0, context.getResources().getColor(201720849));
        a.recycle();
        init();
        if (!this.mSetType) {
            setLoadViewPath();
        }
        this.linearInterpolator = new LinearInterpolator();
        this.mTouchHelper = new ColorViewExplorerByTouchHelper(this);
        this.mTouchHelper.setColorViewTalkBalkInteraction(this.mColorViewTalkBalkInteraction);
        setAccessibilityDelegate(this.mTouchHelper);
        setImportantForAccessibility(1);
        this.mAccessDescription = context.getString(201590170);
    }

    private void init() {
        this.mPaint = new Paint();
        this.mPaint.setStyle(Style.STROKE);
        this.mPaint.setStrokeWidth((float) this.mStrokeWidth);
        this.mPaint.setAntiAlias(true);
        this.mPaint.setDither(true);
        this.mPaint.setColor(this.mPaintColor);
        this.mStartTime = System.currentTimeMillis();
        this.mPointPaint = new Paint();
        this.mPointPaint.setStyle(Style.STROKE);
        this.mPointPaint.setStrokeWidth((float) this.mStrokeWidth);
        this.mPointPaint.setAntiAlias(true);
        this.mPointPaint.setDither(true);
        this.mPointPaint.setColor(this.mPaintColor);
        this.mPointPaint.setStrokeCap(Cap.ROUND);
        this.mFlagStart = true;
    }

    private void setLoadViewPath() {
        int rectLeft = this.mStrokeWidth + 0;
        int rectTop = this.mStrokeWidth + 0;
        int rectRight = this.mWidth - this.mStrokeWidth;
        int rectBottom = this.mHeight - this.mStrokeWidth;
        this.mRectF = new RectF((float) rectLeft, (float) rectTop, (float) rectRight, (float) rectBottom);
        this.mRotationX = (((float) (rectRight - rectLeft)) / 2.0f) + ((float) rectLeft);
        this.mRotationY = (((float) (rectBottom - rectTop)) / 2.0f) + ((float) rectTop);
        this.mPath = new Path();
    }

    public void setAnimationDelayedTime(long postDelayTime) {
        this.mDelayedTime = postDelayTime;
    }

    public void onDraw(Canvas canvas) {
        preparePathAndAngle();
        switch (this.mLoadingMode) {
            case 0:
                for (int i = 0; i < 3; i++) {
                    canvas.save();
                    canvas.translate(this.mRotationX, this.mRotationY);
                    switch (i) {
                        case 0:
                            canvas.rotate(this.mFirstLoadingPointAngle);
                            this.mPointPaint.setAlpha(255);
                            break;
                        case 1:
                            canvas.rotate(this.mSecondLoadingPointAngle);
                            this.mPointPaint.setAlpha((int) (this.mSecondLoadingPointAlpha * 255.0f));
                            break;
                        case 2:
                            canvas.rotate(this.mThirdLoadingPointAngle);
                            this.mPointPaint.setAlpha((int) (this.mThirdLoadingPointAlpha * 255.0f));
                            break;
                        default:
                            break;
                    }
                    canvas.drawPoint(0.0f, this.mRectF.top - this.mRotationY, this.mPointPaint);
                    canvas.restore();
                }
                return;
            case 1:
                canvas.drawPath(this.mPath, this.mPaint);
                return;
            default:
                return;
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(this.mWidth, this.mHeight);
    }

    private void preparePathAndAngle() {
        long time;
        if (this.mFlagStart) {
            this.mStartTime = System.currentTimeMillis();
            this.mFlagStart = false;
        }
        if (2 != this.mLoadingType) {
            time = (System.currentTimeMillis() - this.mStartTime) % ONE_CIRCLE_TIME;
        } else {
            time = (System.currentTimeMillis() - this.mStartTime) % ROTATE_TIME;
        }
        this.mPath.reset();
        if (2 != this.mLoadingType) {
            this.mLoadingMode = 1;
            this.mRotateAngle = getCircleRotateAngle(constrain(0.0f, 1.0f, ((float) time) / 1500.0f));
            this.mRotateRadian = getCircleRotateRadian(this.mRotateAngle);
            this.mPath.arcTo(this.mRectF, (this.mRotateAngle - this.mRotateRadian) - 90.0f, this.mRotateRadian);
        }
        if (2 == this.mLoadingType && time <= ONE_CIRCLE_TIME) {
            this.mLoadingMode = 1;
            this.mRotateAngle = getCircleRotateAngle(constrain(0.0f, 1.0f, ((float) time) / 1500.0f));
            this.mRotateRadian = getCircleRotateRadian(this.mRotateAngle);
            this.mPath.arcTo(this.mRectF, (this.mRotateAngle - this.mRotateRadian) - 90.0f, this.mRotateRadian);
        }
        if (2 == this.mLoadingType && time > ONE_CIRCLE_TIME) {
            this.mLoadingMode = 0;
            this.mFirstLoadingPointAngle = this.linearInterpolator.getInterpolation(constrain(0.0f, 1.0f, ((float) (time - ONE_CIRCLE_TIME)) / 1500.0f)) * 360.0f;
            this.mSecondLoadingPointAngle = getSecondPointAngle(this.mFirstLoadingPointAngle);
            this.mThirdLoadingPointAngle = getThirdPointAngle(this.mFirstLoadingPointAngle);
            this.mSecondLoadingPointAlpha = getSecondPointAlpha(this.mFirstLoadingPointAngle);
            this.mThirdLoadingPointAlpha = getThirdPointAlpha(this.mFirstLoadingPointAngle);
        }
        postOnAnimationDelayed(new Runnable() {
            public void run() {
                ColorLoadingView.this.invalidate();
            }
        }, this.mDelayedTime);
    }

    private float getThirdPointAlpha(float phase) {
        if (phase <= 90.0f || phase > 300.0f) {
            return 0.0f;
        }
        if (phase > 120.0f && phase <= 270.0f) {
            return 1.0f;
        }
        if (phase <= 90.0f || phase > 120.0f) {
            return (float) (1.0d - ((((double) (phase - 270.0f)) * 1.0d) / 30.0d));
        }
        return (float) ((((double) (phase - 90.0f)) * 1.0d) / 30.0d);
    }

    private float getSecondPointAlpha(float angle) {
        if (angle <= 30.0f) {
            return 0.0f;
        }
        if (angle > 60.0f && angle <= 330.0f) {
            return 1.0f;
        }
        if (angle <= 30.0f || angle > 60.0f) {
            return (float) (1.0d - ((((double) (angle - 330.0f)) * 1.0d) / 30.0d));
        }
        return (float) ((((double) (angle - 30.0f)) * 1.0d) / 30.0d);
    }

    private float getThirdPointAngle(float angle) {
        if (angle <= 270.0f || angle > 300.0f) {
            return this.mFirstLoadingPointAngle - 30.0f;
        }
        return (float) ((((((double) ((this.mFirstLoadingPointAngle - 270.0f) * 3.0f)) * 1.0d) / 2.0d) + 270.0d) - 30.0d);
    }

    private float getSecondPointAngle(float angle) {
        if (angle <= 330.0f) {
            return this.mFirstLoadingPointAngle - 15.0f;
        }
        return (float) ((((((double) ((this.mFirstLoadingPointAngle - 330.0f) * 3.0f)) * 1.0d) / 2.0d) + 330.0d) - 15.0d);
    }

    private float getCircleRotateAngle(float phase) {
        if (phase <= 0.5f) {
            return this.linearInterpolator.getInterpolation(2.0f * phase) * 495.0f;
        }
        return (this.linearInterpolator.getInterpolation((float) ((((double) phase) - 0.5d) * 2.0d)) * 225.0f) + 495.0f;
    }

    private float getCircleRotateRadian(float angle) {
        if (angle <= 495.0f) {
            return (float) ((((double) (270.0f * angle)) * 1.0d) / 495.0d);
        }
        return (float) (270.0d - ((((double) ((angle - 495.0f) * 270.0f)) * 1.0d) / 225.0d));
    }

    public static float constrain(float min, float max, float v) {
        return Math.max(min, Math.min(max, v));
    }

    protected boolean dispatchHoverEvent(MotionEvent event) {
        if (this.mTouchHelper == null || !this.mTouchHelper.dispatchHoverEvent(event)) {
            return super.dispatchHoverEvent(event);
        }
        return true;
    }
}
