package com.color.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import java.lang.reflect.Array;

public abstract class ColorBaseWaveView extends View {
    /* renamed from: -com-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues */
    private static final /* synthetic */ int[] f0-com-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues = null;
    protected int mColorEnd;
    protected int mColorStart;
    protected int mFadeAnimationTime;
    protected ValueAnimator mInAnimator;
    protected float mNoiseOffset;
    protected double mNoiseSize;
    protected ValueAnimator mOutAnimator;
    protected Paint mPaint;
    protected int mParticleNum;
    protected int mRadianRectHeight;
    protected int mRadianRectWidth;
    protected int mScreenHeight;
    protected int mScreenWidth;
    protected int mWaveAnimationPeriod;
    protected int mWaveAnimationSpeed;
    protected int mWaveAnimationSpeedIncall;
    protected int mWaveAnimationSpeedNormal;
    protected int mWaveBaseWidth;
    protected int mWaveColorAnimationTime;
    protected int mWaveColorAnimationTimeIncall;
    protected int mWaveColorAnimationTimeNotification;
    protected int mWaveIncrementWidth;
    protected int mWaveParticleSize;
    protected int mWaveViewHeight;

    public enum ColorType {
        RED,
        BLUE,
        GREEN,
        ORANGE
    }

    protected class Noise {
        float octaves;
        float persistence;
        int seed;
        float smoothing;

        public Noise(int seed, float octaves, float persistence) {
            this.seed = seed;
            this.octaves = octaves;
            this.persistence = persistence;
            this.smoothing = 4.0f;
        }

        public Noise(ColorBaseWaveView this$0, int seed, float octaves) {
            this(seed, octaves, 0.25f);
        }

        public Noise(ColorBaseWaveView this$0, int seed) {
            this(seed, 4.0f, 0.25f);
        }

        public float rawNoise(float x) {
            int n = (((int) x) << 13) ^ ((int) x);
            return 1.0f - (((float) (((((((n * n) * 15731) * this.seed) + (this.seed * 789221)) * n) + (this.seed * 1376312589)) & Integer.MAX_VALUE)) / 1.07374182E9f);
        }

        public float rawNoise2D(float x, float y) {
            return rawNoise((57.0f * y) + x);
        }

        public float smoothNoise(float x) {
            return ((rawNoise(x) / 2.0f) + (rawNoise(x - 1.0f) / this.smoothing)) + (rawNoise(x + 1.0f) / this.smoothing);
        }

        public float smoothNoise2D(float x, float y) {
            return ((rawNoise2D(x, y) / 4.0f) + ((((rawNoise2D(x, y - 1.0f) + rawNoise2D(x, y + 1.0f)) + rawNoise2D(x - 1.0f, y)) + rawNoise2D(x + 1.0f, y)) / 8.0f)) + ((((rawNoise2D(x - 1.0f, y - 1.0f) + rawNoise2D(x - 1.0f, y + 1.0f)) + rawNoise2D(x + 1.0f, y - 1.0f)) + rawNoise2D(x + 1.0f, y + 1.0f)) / 16.0f);
        }

        public float linearInterpolate(float a, float b, float x) {
            return ((1.0f - x) * a) + (b * x);
        }

        public float cosineInterpolate(float a, float b, float x) {
            float f = (1.0f - ((float) Math.cos(((double) x) * 3.141592653589793d))) / 2.0f;
            return ((1.0f - f) * a) + (b * f);
        }

        public float interpolateNoise(float x) {
            return cosineInterpolate(smoothNoise((float) Math.floor((double) x)), smoothNoise(((float) Math.floor((double) x)) + 1.0f), (float) (((double) x) - Math.floor((double) x)));
        }

        public float interpolateNoise2D(double x, double y) {
            return cosineInterpolate(cosineInterpolate(smoothNoise2D((float) Math.floor(x), (float) Math.floor(y)), smoothNoise2D(((float) Math.floor(x)) + 1.0f, (float) Math.floor(y)), (float) (x - ((double) ((float) Math.floor(x))))), cosineInterpolate(smoothNoise2D((float) Math.floor(x), ((float) Math.floor(y)) + 1.0f), smoothNoise2D(((float) Math.floor(x)) + 1.0f, ((float) Math.floor(y)) + 1.0f), (float) (x - ((double) ((float) Math.floor(x))))), (float) (y - ((double) ((float) Math.floor(y)))));
        }

        public float perlinNoise(double v, float x) {
            float total = 0.0f;
            for (int i = 0; ((float) i) < this.octaves; i++) {
                float amplitude = (float) Math.pow((double) this.persistence, (double) i);
                total += interpolateNoise(x * ((float) Math.pow(2.0d, (double) i))) * amplitude;
            }
            return total;
        }

        public float perlinNoise2D(double x, double y) {
            float total = 0.0f;
            for (int i = 0; ((float) i) < this.octaves; i++) {
                float frequency = (float) Math.pow(2.0d, (double) i);
                total += interpolateNoise2D(((double) frequency) * x, ((double) frequency) * y) * ((float) Math.pow((double) this.persistence, (double) i));
            }
            return total;
        }

        public float[][] perlinNoiseMap(int w, int h) {
            float[][] noise = (float[][]) Array.newInstance(Float.TYPE, new int[]{h, w});
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    noise[x][y] = perlinNoise2D((double) x, (double) y);
                }
            }
            return noise;
        }
    }

    /* renamed from: -getcom-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues */
    private static /* synthetic */ int[] m0-getcom-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues() {
        if (f0-com-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues != null) {
            return f0-com-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues;
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
        f0-com-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues = iArr;
        return iArr;
    }

    protected abstract void initAnimation();

    public abstract void setIncallType();

    public abstract void setNotificationType();

    public abstract void startAnimation();

    public abstract void stopAnimation();

    public ColorBaseWaveView(Context context) {
        this(context, null);
    }

    public ColorBaseWaveView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ColorBaseWaveView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mFadeAnimationTime = -1;
        this.mWaveColorAnimationTime = -1;
        this.mWaveColorAnimationTimeNotification = -1;
        this.mWaveColorAnimationTimeIncall = -1;
        this.mNoiseSize = -1.0d;
        this.mWaveBaseWidth = -1;
        this.mNoiseOffset = -1.0f;
        this.mWaveIncrementWidth = -1;
        this.mWaveParticleSize = 6;
        this.mWaveAnimationSpeedNormal = -1;
        this.mWaveAnimationSpeedIncall = -1;
        this.mWaveAnimationSpeed = -1;
        this.mWaveAnimationPeriod = -1;
        this.mPaint = new Paint(1);
        this.mWaveColorAnimationTimeNotification = getResources().getInteger(202179618);
        this.mWaveColorAnimationTimeIncall = getResources().getInteger(202179619);
        this.mWaveColorAnimationTime = this.mWaveColorAnimationTimeNotification;
        this.mFadeAnimationTime = getResources().getInteger(202179620);
        TypedValue typedValue = new TypedValue();
        getResources().getValue(201655597, typedValue, true);
        this.mNoiseSize = (double) typedValue.getFloat();
        this.mWaveBaseWidth = getResources().getDimensionPixelSize(201655591);
        this.mWaveIncrementWidth = getResources().getDimensionPixelSize(201655592);
        this.mWaveParticleSize = getResources().getDimensionPixelSize(201655593);
        this.mWaveViewHeight = getResources().getDimensionPixelSize(201655594);
        this.mWaveAnimationPeriod = getResources().getInteger(202179621);
        this.mNoiseOffset = (float) getResources().getInteger(202179622);
        this.mWaveAnimationSpeedNormal = getResources().getInteger(202179616);
        this.mWaveAnimationSpeedIncall = getResources().getInteger(202179617);
        this.mWaveAnimationSpeed = this.mWaveAnimationSpeedNormal;
        this.mParticleNum = this.mWaveViewHeight / this.mWaveParticleSize;
        this.mRadianRectWidth = getResources().getDimensionPixelSize(201655595);
        this.mRadianRectHeight = getResources().getDimensionPixelSize(201655596);
        getResources();
        this.mScreenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        getResources();
        this.mScreenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        initFadeAnimator();
    }

    private void initFadeAnimator() {
        this.mInAnimator = ValueAnimator.ofInt(new int[]{0, 255});
        this.mInAnimator.setDuration((long) this.mFadeAnimationTime);
        this.mInAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ColorBaseWaveView.this.mPaint.setAlpha(((Integer) valueAnimator.getAnimatedValue()).intValue());
            }
        });
        this.mInAnimator.addListener(new AnimatorListener() {
            public void onAnimationStart(Animator animator) {
                ColorBaseWaveView.this.setVisibility(0);
            }

            public void onAnimationEnd(Animator animator) {
            }

            public void onAnimationCancel(Animator animator) {
            }

            public void onAnimationRepeat(Animator animator) {
            }
        });
        this.mOutAnimator = ValueAnimator.ofInt(new int[]{255, 0});
        this.mOutAnimator.setDuration((long) this.mFadeAnimationTime);
        this.mOutAnimator.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ColorBaseWaveView.this.mPaint.setAlpha(((Integer) valueAnimator.getAnimatedValue()).intValue());
            }
        });
        this.mOutAnimator.addListener(new AnimatorListener() {
            public void onAnimationStart(Animator animator) {
            }

            public void onAnimationEnd(Animator animator) {
                ColorBaseWaveView.this.setVisibility(8);
            }

            public void onAnimationCancel(Animator animator) {
            }

            public void onAnimationRepeat(Animator animator) {
            }
        });
    }

    protected void setColorType(ColorType type) {
        switch (m0-getcom-color-widget-ColorBaseWaveView$ColorTypeSwitchesValues()[type.ordinal()]) {
            case 1:
                this.mColorStart = getResources().getColor(201720921, this.mContext.getTheme());
                this.mColorEnd = getResources().getColor(201720922, this.mContext.getTheme());
                return;
            case 2:
                this.mColorStart = getResources().getColor(201720926, this.mContext.getTheme());
                this.mColorEnd = getResources().getColor(201720927, this.mContext.getTheme());
                return;
            case 3:
                this.mColorStart = getResources().getColor(201720931, this.mContext.getTheme());
                this.mColorEnd = getResources().getColor(201720932, this.mContext.getTheme());
                return;
            case 4:
                this.mColorStart = getResources().getColor(201720916, this.mContext.getTheme());
                this.mColorEnd = getResources().getColor(201720917, this.mContext.getTheme());
                return;
            default:
                return;
        }
    }
}
