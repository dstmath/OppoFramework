package com.android.server.location;

public final class OppoMotionConfig {
    private int mInterval;
    private int mSampleNum;
    private float mThresholdAngle;
    private float mThresholdEnergy;

    public OppoMotionConfig() {
    }

    public OppoMotionConfig(int num, int interval, float energy, float angle) {
        this.mSampleNum = num;
        this.mInterval = interval;
        this.mThresholdEnergy = energy;
        this.mThresholdAngle = angle;
    }

    public int getSampleNum() {
        return this.mSampleNum;
    }

    public void setSampleNum(int num) {
        this.mSampleNum = num;
    }

    public int getInterval() {
        return this.mInterval;
    }

    public void setInterval(int interval) {
        this.mInterval = interval;
    }

    public float getThresholdEnergy() {
        return this.mThresholdEnergy;
    }

    public void setThresholdEnergy(float energy) {
        this.mThresholdEnergy = energy;
    }

    public float getThresholdAngle() {
        return this.mThresholdAngle;
    }

    public void setThresholdAngle(float angle) {
        this.mThresholdAngle = angle;
    }

    public String toString() {
        return "SampleNum = " + this.mSampleNum + ", interval = " + this.mInterval + ", Energy = " + this.mThresholdEnergy + ", Angle = " + this.mThresholdAngle;
    }
}
