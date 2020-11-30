package com.color.animation;

import android.view.animation.Interpolator;

abstract class LookupTableInterpolator implements Interpolator {
    private final float mStepSize = (1.0f / ((float) (this.mValues.length - 1)));
    private final float[] mValues;

    public LookupTableInterpolator(float[] values) {
        this.mValues = values;
    }

    @Override // android.animation.TimeInterpolator
    public float getInterpolation(float input) {
        if (input >= 1.0f) {
            return 1.0f;
        }
        if (input <= 0.0f) {
            return 0.0f;
        }
        float[] fArr = this.mValues;
        int position = Math.min((int) (((float) (fArr.length - 1)) * input), fArr.length - 2);
        float f = this.mStepSize;
        float[] fArr2 = this.mValues;
        return fArr2[position] + ((fArr2[position + 1] - fArr2[position]) * ((input - (((float) position) * f)) / f));
    }
}
