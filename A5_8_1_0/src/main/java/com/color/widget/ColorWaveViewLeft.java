package com.color.widget;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Path;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import com.color.widget.ColorBaseWaveView.ColorType;

public class ColorWaveViewLeft extends ColorBaseWaveView {
    /* renamed from: -com-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues */
    private static final /* synthetic */ int[] f3-com-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues = null;
    private AnimatorSet mB4AnimatorSet;
    private ValueAnimator mColorAnimatorA1;
    private int mColorB1;
    private int mColorB4;
    private int mColorStartB4Before;
    private boolean mIncallType;
    private Path mLinePath;
    private ValueAnimator mNoiseValueAnimator;
    private Noise mNoisegenerator;

    /* renamed from: -getcom-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues */
    private static /* synthetic */ int[] m3-getcom-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues() {
        if (f3-com-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues != null) {
            return f3-com-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues;
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
        f3-com-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues = iArr;
        return iArr;
    }

    public ColorWaveViewLeft(Context context) {
        this(context, null);
    }

    public ColorWaveViewLeft(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ColorWaveViewLeft(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mIncallType = false;
        this.mNoisegenerator = new Noise(this, 30);
        this.mLinePath = new Path();
        setColorType(ColorType.RED);
    }

    public void setColorType(ColorType type) {
        super.setColorType(type);
        switch (m3-getcom-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues()[type.ordinal()]) {
            case 1:
                this.mColorStartB4Before = getResources().getColor(201720925, this.mContext.getTheme());
                break;
            case 2:
                this.mColorStartB4Before = getResources().getColor(201720930, this.mContext.getTheme());
                break;
            case 3:
                this.mColorStartB4Before = getResources().getColor(201720935, this.mContext.getTheme());
                break;
            case 4:
                this.mColorStartB4Before = getResources().getColor(201720920, this.mContext.getTheme());
                break;
        }
        initAnimation();
    }

    protected void initAnimation() {
        this.mNoiseValueAnimator = ValueAnimator.ofFloat(new float[]{0.0f, (float) this.mWaveAnimationSpeed});
        this.mNoiseValueAnimator.setDuration((long) this.mWaveAnimationPeriod);
        this.mNoiseValueAnimator.setRepeatCount(-1);
        this.mNoiseValueAnimator.setRepeatMode(1);
        this.mNoiseValueAnimator.setInterpolator(new LinearInterpolator());
        this.mNoiseValueAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                ColorWaveViewLeft.this.mNoiseOffset = ((Float) animation.getAnimatedValue()).floatValue();
                ColorWaveViewLeft.this.invalidate();
            }
        });
        this.mColorAnimatorA1 = ValueAnimator.ofArgb(new int[]{this.mColorStart, this.mColorEnd});
        this.mColorAnimatorA1.setDuration((long) this.mWaveColorAnimationTime);
        this.mColorAnimatorA1.setEvaluator(new ArgbEvaluator());
        this.mColorAnimatorA1.setRepeatCount(-1);
        this.mColorAnimatorA1.setRepeatMode(2);
        this.mColorAnimatorA1.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ColorWaveViewLeft.this.mColorB1 = ((Integer) valueAnimator.getAnimatedValue()).intValue();
            }
        });
        ValueAnimator mColorAnimatorB4 = ValueAnimator.ofArgb(new int[]{this.mColorStart, this.mColorEnd});
        mColorAnimatorB4.setDuration((long) this.mWaveColorAnimationTime);
        mColorAnimatorB4.setEvaluator(new ArgbEvaluator());
        mColorAnimatorB4.setRepeatCount(-1);
        mColorAnimatorB4.setRepeatMode(2);
        mColorAnimatorB4.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ColorWaveViewLeft.this.mColorB4 = ((Integer) valueAnimator.getAnimatedValue()).intValue();
            }
        });
        ValueAnimator mColorAnimatorB4Before = ValueAnimator.ofArgb(new int[]{this.mColorStartB4Before, this.mColorStart});
        mColorAnimatorB4Before.setDuration((long) (((double) this.mWaveColorAnimationTime) * 0.8d));
        mColorAnimatorB4Before.setEvaluator(new ArgbEvaluator());
        mColorAnimatorB4Before.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ColorWaveViewLeft.this.mColorB4 = ((Integer) valueAnimator.getAnimatedValue()).intValue();
            }
        });
        this.mB4AnimatorSet = new AnimatorSet();
        this.mB4AnimatorSet.play(mColorAnimatorB4).after(mColorAnimatorB4Before);
    }

    public void startAnimation() {
        this.mNoiseValueAnimator.start();
        this.mInAnimator.start();
        this.mColorAnimatorA1.start();
        this.mB4AnimatorSet.start();
    }

    public void stopAnimation() {
        this.mOutAnimator.start();
    }

    protected void onDraw(Canvas canvas) {
        this.mPaint.setShader(new LinearGradient(0.0f, 0.0f, 0.0f, (float) getHeight(), new int[]{this.mColorB1, this.mColorB4}, null, TileMode.CLAMP));
        int pointNum = getHeight() / this.mWaveParticleSize;
        this.mLinePath.reset();
        this.mLinePath.moveTo(0.0f, (float) getHeight());
        for (int i = pointNum; i > 0; i--) {
            float y = (float) (this.mWaveParticleSize * i);
            this.mLinePath.lineTo((float) (((double) this.mWaveBaseWidth) + ((((double) this.mWaveIncrementWidth) * ((-(Math.cos(((double) ((((float) i) / ((float) pointNum)) * 2.0f)) * 3.141592653589793d) - 1.0d)) / 2.0d)) * (((double) this.mNoisegenerator.perlinNoise2D((this.mNoiseSize * ((double) y)) - ((double) (this.mNoiseOffset * 2.0f)), (double) (this.mIncallType ? 0.0f : this.mNoiseOffset / 10.0f))) + 0.5d))), y);
        }
        this.mLinePath.lineTo(0.0f, 0.0f);
        this.mLinePath.close();
        canvas.drawPath(this.mLinePath, this.mPaint);
        super.onDraw(canvas);
    }

    public void setIncallType() {
        this.mIncallType = true;
        this.mWaveAnimationSpeed = this.mWaveAnimationSpeedIncall;
        this.mWaveColorAnimationTime = this.mWaveColorAnimationTimeIncall;
        initAnimation();
    }

    public void setNotificationType() {
        this.mIncallType = false;
        this.mWaveAnimationSpeed = this.mWaveAnimationSpeedNormal;
        this.mWaveColorAnimationTime = this.mWaveColorAnimationTimeNotification;
        initAnimation();
    }
}
