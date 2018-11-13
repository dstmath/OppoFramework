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

public class ColorWaveViewRight extends ColorBaseWaveView {
    /* renamed from: -com-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues */
    private static final /* synthetic */ int[] f4-com-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues = null;
    private AnimatorSet mB2AnimatorSet;
    private AnimatorSet mB3AnimatorSet;
    private int mColorB2;
    private int mColorB3;
    private int mColorStartB2Before;
    private int mColorStartB3Before;
    private boolean mIncallType;
    private Path mLinePath;
    LinearGradient mLinearGradient;
    private ValueAnimator mNoiseValueAnimator;
    private Noise mNoisegenerator;

    /* renamed from: -getcom-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues */
    private static /* synthetic */ int[] m4-getcom-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues() {
        if (f4-com-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues != null) {
            return f4-com-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues;
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
        f4-com-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues = iArr;
        return iArr;
    }

    public ColorWaveViewRight(Context context) {
        this(context, null);
    }

    public ColorWaveViewRight(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ColorWaveViewRight(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mIncallType = false;
        this.mNoisegenerator = new Noise(this, 30);
        this.mLinePath = new Path();
        this.mWaveBaseWidth -= 2;
        setColorType(ColorType.RED);
    }

    protected void initAnimation() {
        this.mNoiseValueAnimator = ValueAnimator.ofFloat(new float[]{0.0f, (float) this.mWaveAnimationSpeed});
        this.mNoiseValueAnimator.setDuration((long) this.mWaveAnimationPeriod);
        this.mNoiseValueAnimator.setRepeatCount(-1);
        this.mNoiseValueAnimator.setRepeatMode(1);
        this.mNoiseValueAnimator.setInterpolator(new LinearInterpolator());
        this.mNoiseValueAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                ColorWaveViewRight.this.mNoiseOffset = ((Float) animation.getAnimatedValue()).floatValue();
                ColorWaveViewRight.this.invalidate();
            }
        });
        ValueAnimator mColorAnimatorB2 = ValueAnimator.ofArgb(new int[]{this.mColorStart, this.mColorEnd});
        mColorAnimatorB2.setDuration((long) this.mWaveColorAnimationTime);
        mColorAnimatorB2.setEvaluator(new ArgbEvaluator());
        mColorAnimatorB2.setRepeatCount(-1);
        mColorAnimatorB2.setRepeatMode(2);
        mColorAnimatorB2.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ColorWaveViewRight.this.mColorB2 = ((Integer) valueAnimator.getAnimatedValue()).intValue();
            }
        });
        ValueAnimator mColorAnimatorB2Before = ValueAnimator.ofArgb(new int[]{this.mColorStartB2Before, this.mColorStart});
        mColorAnimatorB2Before.setDuration((long) (((double) this.mWaveColorAnimationTime) * 0.2d));
        mColorAnimatorB2Before.setEvaluator(new ArgbEvaluator());
        mColorAnimatorB2Before.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ColorWaveViewRight.this.mColorB2 = ((Integer) valueAnimator.getAnimatedValue()).intValue();
            }
        });
        ValueAnimator mColorAnimatorB3 = ValueAnimator.ofArgb(new int[]{this.mColorStart, this.mColorEnd});
        mColorAnimatorB3.setDuration((long) this.mWaveColorAnimationTime);
        mColorAnimatorB3.setEvaluator(new ArgbEvaluator());
        mColorAnimatorB3.setRepeatCount(-1);
        mColorAnimatorB3.setRepeatMode(2);
        mColorAnimatorB3.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ColorWaveViewRight.this.mColorB3 = ((Integer) valueAnimator.getAnimatedValue()).intValue();
            }
        });
        ValueAnimator mColorAnimatorB3Before = ValueAnimator.ofArgb(new int[]{this.mColorStartB3Before, this.mColorStart});
        mColorAnimatorB3Before.setDuration((long) (((double) this.mWaveColorAnimationTime) * 0.6d));
        mColorAnimatorB3Before.setEvaluator(new ArgbEvaluator());
        mColorAnimatorB3Before.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ColorWaveViewRight.this.mColorB3 = ((Integer) valueAnimator.getAnimatedValue()).intValue();
            }
        });
        this.mB2AnimatorSet = new AnimatorSet();
        this.mB2AnimatorSet.play(mColorAnimatorB2).after(mColorAnimatorB2Before);
        this.mB3AnimatorSet = new AnimatorSet();
        this.mB3AnimatorSet.play(mColorAnimatorB3).after(mColorAnimatorB3Before);
    }

    public void startAnimation() {
        this.mNoiseValueAnimator.start();
        this.mInAnimator.start();
        this.mB2AnimatorSet.start();
        this.mB3AnimatorSet.start();
    }

    public void stopAnimation() {
        this.mOutAnimator.start();
    }

    protected void onDraw(Canvas canvas) {
        this.mLinearGradient = new LinearGradient(0.0f, 0.0f, 0.0f, (float) getHeight(), new int[]{this.mColorB2, this.mColorB3}, null, TileMode.CLAMP);
        this.mPaint.setShader(this.mLinearGradient);
        this.mLinePath.reset();
        this.mLinePath.moveTo((float) getWidth(), 0.0f);
        for (int i = 0; i < this.mParticleNum; i++) {
            float y = (float) (this.mWaveParticleSize * i);
            this.mLinePath.lineTo((float) (((double) (getWidth() - this.mWaveBaseWidth)) - ((((double) this.mWaveIncrementWidth) * ((-(Math.cos(((double) ((((float) i) / ((float) this.mParticleNum)) * 2.0f)) * 3.141592653589793d) - 1.0d)) / 2.0d)) * (((double) this.mNoisegenerator.perlinNoise2D((this.mNoiseSize * ((double) y)) - ((double) (this.mNoiseOffset * 2.0f)), (double) (this.mIncallType ? 0.0f : this.mNoiseOffset / 10.0f))) + 0.5d))), y);
        }
        this.mLinePath.lineTo((float) getWidth(), (float) getHeight());
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

    public void setColorType(ColorType type) {
        super.setColorType(type);
        switch (m4-getcom-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues()[type.ordinal()]) {
            case 1:
                this.mColorStartB2Before = getResources().getColor(201720923, this.mContext.getTheme());
                this.mColorStartB3Before = getResources().getColor(201720924, this.mContext.getTheme());
                break;
            case 2:
                this.mColorStartB2Before = getResources().getColor(201720928, this.mContext.getTheme());
                this.mColorStartB3Before = getResources().getColor(201720929, this.mContext.getTheme());
                break;
            case 3:
                this.mColorStartB2Before = getResources().getColor(201720933, this.mContext.getTheme());
                this.mColorStartB3Before = getResources().getColor(201720934, this.mContext.getTheme());
                break;
            case 4:
                this.mColorStartB2Before = getResources().getColor(201720918, this.mContext.getTheme());
                this.mColorStartB3Before = getResources().getColor(201720919, this.mContext.getTheme());
                break;
        }
        initAnimation();
    }
}
