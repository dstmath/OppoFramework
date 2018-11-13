package com.oppo.widget;

import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;

public class OppoFingerWrongIn implements Interpolator {
    private final Interpolator WRONGLARGE = new PathInterpolator(0.444f, 0.0f, 0.444f, 1.0f);
    private final Interpolator WRONGSMALL = new PathInterpolator(0.35f, 0.0f, 0.7f, 1.0f);
    private float mLargeAndPause = (this.mWrongLargeTime + this.mWrongPauseTime);
    private float mWrongLargeTime = 208.0f;
    private float mWrongPauseTime = 83.0f;
    private float mWrongSmallTime = 104.0f;
    private float mWrongTotalTime = ((this.mWrongLargeTime + this.mWrongPauseTime) + this.mWrongSmallTime);

    public float getInterpolation(float input) {
        if (input < this.mWrongLargeTime / this.mWrongTotalTime) {
            return this.WRONGLARGE.getInterpolation(input / (this.mWrongLargeTime / this.mWrongTotalTime));
        }
        if (this.mWrongLargeTime / this.mWrongTotalTime < input && input < this.mLargeAndPause / this.mWrongTotalTime) {
            return 1.0f;
        }
        if (input > this.mLargeAndPause / this.mWrongTotalTime) {
            return 1.0f - this.WRONGSMALL.getInterpolation((input - (this.mLargeAndPause / this.mWrongTotalTime)) / (1.0f - (this.mLargeAndPause / this.mWrongTotalTime)));
        }
        return 0.0f;
    }
}
