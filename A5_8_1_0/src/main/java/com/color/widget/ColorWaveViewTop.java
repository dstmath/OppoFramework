package com.color.widget;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import com.color.widget.ColorBaseWaveView.ColorType;

public class ColorWaveViewTop extends ColorBaseWaveView {
    /* renamed from: -com-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues */
    private static final /* synthetic */ int[] f5-com-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues = null;
    private AnimatorSet mA2AnimatorSet;
    private int mColorA1;
    private int mColorA2;
    private ValueAnimator mColorAnimatorA1;
    private int mColorStartA2Before;
    private RectF mOvalLeft;
    private RectF mOvalRight;
    private Path mPath;

    /* renamed from: -getcom-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues */
    private static /* synthetic */ int[] m5-getcom-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues() {
        if (f5-com-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues != null) {
            return f5-com-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues;
        }
        int[] iArr = new int[ColorType.values().length];
        try {
            iArr[ColorType.BLUE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ColorType.GREEN.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ColorType.ORANGE.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ColorType.RED.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        f5-com-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues = iArr;
        return iArr;
    }

    public ColorWaveViewTop(Context context) {
        this(context, null);
    }

    public ColorWaveViewTop(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ColorWaveViewTop(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mOvalLeft = new RectF();
        this.mOvalRight = new RectF();
        this.mPath = new Path();
        setColorType(ColorType.RED);
        initPath();
        drawPath();
    }

    protected void initAnimation() {
        this.mColorAnimatorA1 = ValueAnimator.ofArgb(new int[]{this.mColorStart, this.mColorEnd});
        this.mColorAnimatorA1.setDuration((long) this.mWaveColorAnimationTime);
        this.mColorAnimatorA1.setEvaluator(new ArgbEvaluator());
        this.mColorAnimatorA1.setRepeatCount(-1);
        this.mColorAnimatorA1.setRepeatMode(2);
        this.mColorAnimatorA1.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ColorWaveViewTop.this.mColorA1 = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                ColorWaveViewTop.this.postInvalidate();
            }
        });
        ValueAnimator mColorAnimatorA2 = ValueAnimator.ofArgb(new int[]{this.mColorStart, this.mColorEnd});
        mColorAnimatorA2.setDuration((long) this.mWaveColorAnimationTime);
        mColorAnimatorA2.setEvaluator(new ArgbEvaluator());
        mColorAnimatorA2.setRepeatCount(-1);
        mColorAnimatorA2.setRepeatMode(2);
        mColorAnimatorA2.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ColorWaveViewTop.this.mColorA2 = ((Integer) valueAnimator.getAnimatedValue()).intValue();
            }
        });
        ValueAnimator mColorAnimatorA2Before = ValueAnimator.ofArgb(new int[]{this.mColorStartA2Before, this.mColorStart});
        mColorAnimatorA2Before.setDuration((long) (((double) this.mWaveColorAnimationTime) * 0.2d));
        mColorAnimatorA2Before.setEvaluator(new ArgbEvaluator());
        mColorAnimatorA2Before.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ColorWaveViewTop.this.mColorA2 = ((Integer) valueAnimator.getAnimatedValue()).intValue();
            }
        });
        this.mA2AnimatorSet = new AnimatorSet();
        this.mA2AnimatorSet.play(mColorAnimatorA2).after(mColorAnimatorA2Before);
    }

    public void startAnimation() {
        this.mInAnimator.start();
        this.mColorAnimatorA1.start();
        this.mA2AnimatorSet.start();
    }

    public void stopAnimation() {
        this.mOutAnimator.start();
    }

    private void initPath() {
        this.mOvalLeft.left = (float) this.mWaveBaseWidth;
        this.mOvalLeft.top = (float) this.mWaveBaseWidth;
        this.mOvalLeft.right = (float) (this.mRadianRectWidth + this.mWaveBaseWidth);
        this.mOvalLeft.bottom = (float) this.mRadianRectHeight;
        this.mOvalRight.left = (float) ((this.mScreenWidth - this.mWaveBaseWidth) - this.mRadianRectWidth);
        this.mOvalRight.top = (float) this.mWaveBaseWidth;
        this.mOvalRight.right = (float) (this.mScreenWidth - this.mWaveBaseWidth);
        this.mOvalRight.bottom = (float) this.mRadianRectHeight;
    }

    private void drawPath() {
        this.mPath.reset();
        this.mPath.moveTo((float) this.mWaveBaseWidth, (float) this.mRadianRectHeight);
        this.mPath.arcTo(this.mOvalLeft, 180.0f, 90.0f, false);
        this.mPath.lineTo((float) ((this.mScreenWidth - this.mWaveBaseWidth) - this.mRadianRectWidth), 9.0f);
        this.mPath.arcTo(this.mOvalRight, 270.0f, 90.0f, false);
        this.mPath.lineTo((float) (this.mScreenWidth - this.mWaveBaseWidth), (float) this.mRadianRectHeight);
        this.mPath.lineTo((float) this.mScreenWidth, (float) this.mRadianRectHeight);
        this.mPath.lineTo((float) this.mScreenWidth, 0.0f);
        this.mPath.lineTo(0.0f, 0.0f);
        this.mPath.lineTo(0.0f, (float) this.mRadianRectHeight);
        this.mPath.lineTo((float) this.mWaveBaseWidth, (float) this.mRadianRectHeight);
    }

    protected void onDraw(Canvas canvas) {
        float f = 0.0f;
        float f2 = 0.0f;
        this.mPaint.setShader(new LinearGradient(0.0f, f, (float) this.mScreenWidth, f2, new int[]{this.mColorA1, this.mColorA2}, null, TileMode.CLAMP));
        canvas.drawPath(this.mPath, this.mPaint);
        super.onDraw(canvas);
    }

    public void setIncallType() {
        this.mWaveColorAnimationTime = this.mWaveColorAnimationTimeIncall;
        initAnimation();
    }

    public void setNotificationType() {
        this.mWaveColorAnimationTime = this.mWaveColorAnimationTimeNotification;
        initAnimation();
    }

    public void setColorType(ColorType type) {
        super.setColorType(type);
        switch (m5-getcom-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues()[type.ordinal()]) {
            case 1:
                this.mColorStartA2Before = getResources().getColor(201720923, this.mContext.getTheme());
                break;
            case 2:
                this.mColorStartA2Before = getResources().getColor(201720923, this.mContext.getTheme());
                break;
            case 3:
                this.mColorStartA2Before = getResources().getColor(201720923, this.mContext.getTheme());
                break;
            case 4:
                this.mColorStartA2Before = getResources().getColor(201720918, this.mContext.getTheme());
                break;
        }
        initAnimation();
    }
}
