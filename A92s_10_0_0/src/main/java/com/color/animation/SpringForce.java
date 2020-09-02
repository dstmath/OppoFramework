package com.color.animation;

import com.color.animation.DynamicAnimation;

public final class SpringForce implements Force {
    public static final float DAMPING_RATIO_HIGH_BOUNCY = 0.2f;
    public static final float DAMPING_RATIO_LOW_BOUNCY = 0.75f;
    public static final float DAMPING_RATIO_MEDIUM_BOUNCY = 0.5f;
    public static final float DAMPING_RATIO_NO_BOUNCY = 1.0f;
    public static final float STIFFNESS_HIGH = 10000.0f;
    public static final float STIFFNESS_LOW = 200.0f;
    public static final float STIFFNESS_MEDIUM = 1500.0f;
    public static final float STIFFNESS_VERY_LOW = 50.0f;
    private static final double UNSET = Double.MAX_VALUE;
    private static final double VELOCITY_THRESHOLD_MULTIPLIER = 62.5d;
    private double mDampedFreq;
    double mDampingRatio = 0.5d;
    private double mFinalPosition = UNSET;
    private double mGammaMinus;
    private double mGammaPlus;
    private boolean mInitialized = false;
    private final DynamicAnimation.MassState mMassState = new DynamicAnimation.MassState();
    double mNaturalFreq = Math.sqrt(1500.0d);
    private double mValueThreshold;
    private double mVelocityThreshold;

    public SpringForce() {
    }

    public SpringForce(float finalPosition) {
        this.mFinalPosition = (double) finalPosition;
    }

    public SpringForce setStiffness(float stiffness) {
        if (stiffness > 0.0f) {
            this.mNaturalFreq = Math.sqrt((double) stiffness);
            this.mInitialized = false;
            return this;
        }
        throw new IllegalArgumentException("Spring stiffness constant must be positive.");
    }

    public float getStiffness() {
        double d = this.mNaturalFreq;
        return (float) (d * d);
    }

    public SpringForce setDampingRatio(float dampingRatio) {
        if (dampingRatio >= 0.0f) {
            this.mDampingRatio = (double) dampingRatio;
            this.mInitialized = false;
            return this;
        }
        throw new IllegalArgumentException("Damping ratio must be non-negative");
    }

    public float getDampingRatio() {
        return (float) this.mDampingRatio;
    }

    public SpringForce setFinalPosition(float finalPosition) {
        this.mFinalPosition = (double) finalPosition;
        return this;
    }

    public float getFinalPosition() {
        return (float) this.mFinalPosition;
    }

    @Override // com.color.animation.Force
    public float getAcceleration(float lastDisplacement, float lastVelocity) {
        float lastDisplacement2 = lastDisplacement - getFinalPosition();
        double d = this.mNaturalFreq;
        return (float) (((-(d * d)) * ((double) lastDisplacement2)) - (((double) lastVelocity) * ((d * 2.0d) * this.mDampingRatio)));
    }

    @Override // com.color.animation.Force
    public boolean isAtEquilibrium(float value, float velocity) {
        if (((double) Math.abs(velocity)) >= this.mVelocityThreshold || ((double) Math.abs(value - getFinalPosition())) >= this.mValueThreshold) {
            return false;
        }
        return true;
    }

    private void init() {
        if (!this.mInitialized) {
            if (this.mFinalPosition != UNSET) {
                double d = this.mDampingRatio;
                if (d > 1.0d) {
                    double d2 = this.mNaturalFreq;
                    this.mGammaPlus = ((-d) * d2) + (d2 * Math.sqrt((d * d) - 1.0d));
                    double d3 = this.mDampingRatio;
                    double d4 = this.mNaturalFreq;
                    this.mGammaMinus = ((-d3) * d4) - (d4 * Math.sqrt((d3 * d3) - 1.0d));
                } else if (d >= 0.0d && d < 1.0d) {
                    this.mDampedFreq = this.mNaturalFreq * Math.sqrt(1.0d - (d * d));
                }
                this.mInitialized = true;
                return;
            }
            throw new IllegalStateException("Error: Final position of the spring must be set before the animation starts");
        }
    }

    /* access modifiers changed from: package-private */
    public DynamicAnimation.MassState updateValues(double lastDisplacement, double lastVelocity, long timeElapsed) {
        double sinCoeff;
        double cosCoeff;
        init();
        double deltaT = ((double) timeElapsed) / 1000.0d;
        double lastDisplacement2 = lastDisplacement - this.mFinalPosition;
        double displacement = this.mDampingRatio;
        if (displacement > 1.0d) {
            double d = this.mGammaMinus;
            double d2 = this.mGammaPlus;
            double coeffA = lastDisplacement2 - (((d * lastDisplacement2) - lastVelocity) / (d - d2));
            double coeffB = ((d * lastDisplacement2) - lastVelocity) / (d - d2);
            double displacement2 = (Math.pow(2.718281828459045d, d * deltaT) * coeffA) + (Math.pow(2.718281828459045d, this.mGammaPlus * deltaT) * coeffB);
            double d3 = this.mGammaMinus;
            double pow = coeffA * d3 * Math.pow(2.718281828459045d, d3 * deltaT);
            double d4 = this.mGammaPlus;
            sinCoeff = displacement2;
            cosCoeff = pow + (coeffB * d4 * Math.pow(2.718281828459045d, d4 * deltaT));
        } else if (displacement == 1.0d) {
            double d5 = this.mNaturalFreq;
            double coeffB2 = lastVelocity + (d5 * lastDisplacement2);
            sinCoeff = Math.pow(2.718281828459045d, (-d5) * deltaT) * (lastDisplacement2 + (coeffB2 * deltaT));
            double pow2 = (lastDisplacement2 + (coeffB2 * deltaT)) * Math.pow(2.718281828459045d, (-this.mNaturalFreq) * deltaT);
            double d6 = this.mNaturalFreq;
            cosCoeff = (pow2 * (-d6)) + (Math.pow(2.718281828459045d, (-d6) * deltaT) * coeffB2);
        } else {
            double d7 = 1.0d / this.mDampedFreq;
            double d8 = this.mNaturalFreq;
            double sinCoeff2 = d7 * ((displacement * d8 * lastDisplacement2) + lastVelocity);
            double displacement3 = Math.pow(2.718281828459045d, (-displacement) * d8 * deltaT) * ((Math.cos(this.mDampedFreq * deltaT) * lastDisplacement2) + (Math.sin(this.mDampedFreq * deltaT) * sinCoeff2));
            double d9 = this.mNaturalFreq;
            double lastDisplacement3 = this.mDampingRatio;
            double d10 = (-d9) * displacement3 * lastDisplacement3;
            double pow3 = Math.pow(2.718281828459045d, (-lastDisplacement3) * d9 * deltaT);
            double d11 = this.mDampedFreq;
            double sin = (-d11) * lastDisplacement2 * Math.sin(d11 * deltaT);
            double d12 = this.mDampedFreq;
            sinCoeff = displacement3;
            cosCoeff = d10 + (pow3 * (sin + (d12 * sinCoeff2 * Math.cos(d12 * deltaT))));
        }
        DynamicAnimation.MassState massState = this.mMassState;
        massState.mValue = (float) (this.mFinalPosition + sinCoeff);
        massState.mVelocity = (float) cosCoeff;
        return massState;
    }

    /* access modifiers changed from: package-private */
    public void setValueThreshold(double threshold) {
        this.mValueThreshold = Math.abs(threshold);
        this.mVelocityThreshold = this.mValueThreshold * VELOCITY_THRESHOLD_MULTIPLIER;
    }
}
