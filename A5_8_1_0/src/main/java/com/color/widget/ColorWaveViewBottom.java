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

public class ColorWaveViewBottom extends ColorBaseWaveView {
    /* renamed from: -com-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues */
    private static final /* synthetic */ int[] f2-com-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues = null;
    private AnimatorSet mA3AnimatorSet;
    private AnimatorSet mA4AnimatorSet;
    private int mColorA3;
    private int mColorA4;
    private int mColorStartA3Before;
    private int mColorStartA4Before;
    private RectF mOvalLeft;
    private RectF mOvalRight;
    private Path mPath;

    /* renamed from: -getcom-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues */
    private static /* synthetic */ int[] m2-getcom-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues() {
        if (f2-com-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues != null) {
            return f2-com-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues;
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
        f2-com-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues = iArr;
        return iArr;
    }

    public ColorWaveViewBottom(Context context) {
        this(context, null);
    }

    public ColorWaveViewBottom(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ColorWaveViewBottom(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mOvalLeft = new RectF();
        this.mOvalRight = new RectF();
        this.mPath = new Path();
        initPath();
        drawPath();
        setColorType(ColorType.RED);
    }

    protected void initAnimation() {
        ValueAnimator mColorAnimatorA4 = ValueAnimator.ofArgb(new int[]{this.mColorStart, this.mColorEnd});
        mColorAnimatorA4.setDuration((long) this.mWaveColorAnimationTime);
        mColorAnimatorA4.setEvaluator(new ArgbEvaluator());
        mColorAnimatorA4.setRepeatCount(-1);
        mColorAnimatorA4.setRepeatMode(2);
        mColorAnimatorA4.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ColorWaveViewBottom.this.mColorA4 = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                ColorWaveViewBottom.this.postInvalidate();
            }
        });
        ValueAnimator mColorAnimatorA3 = ValueAnimator.ofArgb(new int[]{this.mColorStart, this.mColorEnd});
        mColorAnimatorA3.setDuration((long) this.mWaveColorAnimationTime);
        mColorAnimatorA3.setEvaluator(new ArgbEvaluator());
        mColorAnimatorA3.setRepeatCount(-1);
        mColorAnimatorA3.setRepeatMode(2);
        mColorAnimatorA3.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ColorWaveViewBottom.this.mColorA3 = ((Integer) valueAnimator.getAnimatedValue()).intValue();
            }
        });
        ValueAnimator mColorAnimatorA3Before = ValueAnimator.ofArgb(new int[]{this.mColorStartA3Before, this.mColorStart});
        mColorAnimatorA3Before.setDuration((long) (((double) this.mWaveColorAnimationTime) * 0.6d));
        mColorAnimatorA3Before.setEvaluator(new ArgbEvaluator());
        mColorAnimatorA3Before.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ColorWaveViewBottom.this.mColorA3 = ((Integer) valueAnimator.getAnimatedValue()).intValue();
            }
        });
        ValueAnimator mColorAnimatorA4Before = ValueAnimator.ofArgb(new int[]{this.mColorStartA4Before, this.mColorStart});
        mColorAnimatorA4Before.setDuration((long) (((double) this.mWaveColorAnimationTime) * 0.8d));
        mColorAnimatorA4Before.setEvaluator(new ArgbEvaluator());
        mColorAnimatorA4Before.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ColorWaveViewBottom.this.mColorA4 = ((Integer) valueAnimator.getAnimatedValue()).intValue();
                ColorWaveViewBottom.this.postInvalidate();
            }
        });
        this.mA3AnimatorSet = new AnimatorSet();
        this.mA3AnimatorSet.play(mColorAnimatorA3).after(mColorAnimatorA3Before);
        this.mA4AnimatorSet = new AnimatorSet();
        this.mA4AnimatorSet.play(mColorAnimatorA4).after(mColorAnimatorA4Before);
    }

    public void setColorType(ColorType type) {
        super.setColorType(type);
        switch (m2-getcom-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues()[type.ordinal()]) {
            case 1:
                this.mColorStartA3Before = getResources().getColor(201720924, this.mContext.getTheme());
                this.mColorStartA4Before = getResources().getColor(201720925, this.mContext.getTheme());
                break;
            case 2:
                this.mColorStartA3Before = getResources().getColor(201720929, this.mContext.getTheme());
                this.mColorStartA4Before = getResources().getColor(201720930, this.mContext.getTheme());
                break;
            case 3:
                this.mColorStartA3Before = getResources().getColor(201720934, this.mContext.getTheme());
                this.mColorStartA4Before = getResources().getColor(201720935, this.mContext.getTheme());
                break;
            case 4:
                this.mColorStartA3Before = getResources().getColor(201720919, this.mContext.getTheme());
                this.mColorStartA4Before = getResources().getColor(201720920, this.mContext.getTheme());
                break;
        }
        initAnimation();
    }

    public void startAnimation() {
        this.mInAnimator.start();
        this.mA3AnimatorSet.start();
        this.mA4AnimatorSet.start();
    }

    public void stopAnimation() {
        this.mOutAnimator.start();
    }

    private void initPath() {
        this.mOvalLeft.left = (float) this.mWaveBaseWidth;
        this.mOvalLeft.top = 0.0f;
        this.mOvalLeft.right = (float) (this.mRadianRectWidth + this.mWaveBaseWidth);
        this.mOvalLeft.bottom = (float) (this.mRadianRectHeight - this.mWaveBaseWidth);
        this.mOvalRight.left = (float) ((this.mScreenWidth - this.mRadianRectWidth) - this.mWaveBaseWidth);
        this.mOvalRight.top = 0.0f;
        this.mOvalRight.right = (float) (this.mScreenWidth - this.mWaveBaseWidth);
        this.mOvalRight.bottom = (float) (this.mRadianRectHeight - this.mWaveBaseWidth);
    }

    private void drawPath() {
        this.mPath.reset();
        this.mPath.moveTo((float) this.mWaveBaseWidth, 0.0f);
        this.mPath.arcTo(this.mOvalLeft, 180.0f, -90.0f, false);
        this.mPath.lineTo((float) ((this.mScreenWidth - this.mRadianRectWidth) - this.mWaveBaseWidth), (float) (this.mRadianRectHeight - this.mWaveBaseWidth));
        this.mPath.arcTo(this.mOvalRight, 90.0f, -90.0f, false);
        this.mPath.lineTo((float) (this.mScreenWidth - this.mWaveBaseWidth), 0.0f);
        this.mPath.lineTo((float) this.mScreenWidth, 0.0f);
        this.mPath.lineTo((float) this.mScreenWidth, (float) this.mRadianRectHeight);
        this.mPath.lineTo(0.0f, (float) this.mRadianRectHeight);
        this.mPath.lineTo(0.0f, 0.0f);
        this.mPath.lineTo((float) this.mWaveBaseWidth, 0.0f);
    }

    protected void onDraw(Canvas canvas) {
        float f = 0.0f;
        float f2 = 0.0f;
        this.mPaint.setShader(new LinearGradient(0.0f, f, (float) this.mScreenWidth, f2, new int[]{this.mColorA4, this.mColorA3}, null, TileMode.CLAMP));
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
}
