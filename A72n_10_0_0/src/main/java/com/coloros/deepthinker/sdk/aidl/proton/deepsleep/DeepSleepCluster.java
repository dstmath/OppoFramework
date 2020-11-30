package com.coloros.deepthinker.sdk.aidl.proton.deepsleep;

import android.os.Parcel;
import android.os.Parcelable;
import com.coloros.deepthinker.sdk.common.utils.SDKLog;

public class DeepSleepCluster implements Parcelable, Cloneable {
    public static final int ANOMALY_TYPE = -1;
    public static final Parcelable.Creator<DeepSleepCluster> CREATOR = new Parcelable.Creator<DeepSleepCluster>() {
        /* class com.coloros.deepthinker.sdk.aidl.proton.deepsleep.DeepSleepCluster.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DeepSleepCluster createFromParcel(Parcel source) {
            DeepSleepCluster cluster = new DeepSleepCluster(DeepSleepCluster.DEFAULT_MAX_DISTANCE, DeepSleepCluster.DEFAULT_MAX_DISTANCE);
            cluster.mSleepTimePeriod = source.readDouble();
            cluster.mWakeTimePeriod = source.readDouble();
            cluster.mMaxDistance = source.readDouble();
            cluster.mClusterId = source.readInt();
            cluster.mClusterNum = source.readInt();
            cluster.mSleepMinValue = source.readDouble();
            cluster.mSleepMaxValue = source.readDouble();
            cluster.mWakeMinValue = source.readDouble();
            cluster.mWakeMaxValue = source.readDouble();
            return cluster;
        }

        @Override // android.os.Parcelable.Creator
        public DeepSleepCluster[] newArray(int size) {
            return new DeepSleepCluster[size];
        }
    };
    private static final double DEFAULT_MAX_DISTANCE = 0.0d;
    private static final String TAG = "DeepSleepCluster";
    private int mClusterId = -1;
    private int mClusterNum = 0;
    private double mMaxDistance = DEFAULT_MAX_DISTANCE;
    private double mSleepMaxValue = DEFAULT_MAX_DISTANCE;
    private double mSleepMinValue = DEFAULT_MAX_DISTANCE;
    private double mSleepTimePeriod = DEFAULT_MAX_DISTANCE;
    private double mWakeMaxValue = DEFAULT_MAX_DISTANCE;
    private double mWakeMinValue = DEFAULT_MAX_DISTANCE;
    private double mWakeTimePeriod = DEFAULT_MAX_DISTANCE;

    public DeepSleepCluster(double sleep, double wake) {
        this.mSleepTimePeriod = sleep;
        this.mWakeTimePeriod = wake;
        this.mMaxDistance = DEFAULT_MAX_DISTANCE;
    }

    public DeepSleepCluster(double sleepTimePeriod, double wakeTimePeriod, double sleepMinValue, double sleepMaxValue, double wakeMinValue, double wakeMaxValue) {
        this.mSleepTimePeriod = sleepTimePeriod;
        this.mWakeTimePeriod = wakeTimePeriod;
        this.mMaxDistance = DEFAULT_MAX_DISTANCE;
        this.mSleepMinValue = sleepMinValue;
        this.mSleepMaxValue = sleepMaxValue;
        this.mWakeMinValue = wakeMinValue;
        this.mWakeMaxValue = wakeMaxValue;
    }

    public DeepSleepCluster(double sleep, double wake, double maxDistance) {
        this.mSleepTimePeriod = sleep;
        this.mWakeTimePeriod = wake;
        this.mMaxDistance = maxDistance;
    }

    public DeepSleepCluster(int clusterId, double sleep, double wake, double maxDistance) {
        this.mSleepTimePeriod = sleep;
        this.mWakeTimePeriod = wake;
        this.mMaxDistance = maxDistance;
        this.mClusterId = clusterId;
    }

    public double getSleepTimePeriod() {
        return this.mSleepTimePeriod;
    }

    public double getWakeTimePeriod() {
        return this.mWakeTimePeriod;
    }

    public double getMaxDistance() {
        return this.mMaxDistance;
    }

    public int getClusterId() {
        return this.mClusterId;
    }

    public int getClusterNum() {
        return this.mClusterNum;
    }

    public void setClusterId(int clusterId) {
        this.mClusterId = clusterId;
    }

    public void setMaxDistance(double maxDistance) {
        this.mMaxDistance = maxDistance;
    }

    public void setClusterNum(int num) {
        this.mClusterNum = num;
    }

    public void setSleepTimePeriod(double sleepTimePeriod) {
        this.mSleepTimePeriod = sleepTimePeriod;
    }

    public void setWakeTimePeriod(double wakeTimePeriod) {
        this.mWakeTimePeriod = wakeTimePeriod;
    }

    public double getSleepMinValue() {
        return this.mSleepMinValue;
    }

    public double getSleepMaxValue() {
        return this.mSleepMaxValue;
    }

    public double getWakeMinValue() {
        return this.mWakeMinValue;
    }

    public double getWakeMaxValue() {
        return this.mWakeMaxValue;
    }

    public String toString() {
        return String.format("DeepSleepCluster:clusterId=%d sleep=%.2f wake=%.2f clusterNum=%d maxDistance=%.2f", Integer.valueOf(this.mClusterId), Double.valueOf(this.mSleepTimePeriod), Double.valueOf(this.mWakeTimePeriod), Integer.valueOf(this.mClusterNum), Double.valueOf(this.mMaxDistance));
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.mSleepTimePeriod);
        dest.writeDouble(this.mWakeTimePeriod);
        dest.writeDouble(this.mMaxDistance);
        dest.writeInt(this.mClusterId);
        dest.writeInt(this.mClusterNum);
        dest.writeDouble(this.mSleepMinValue);
        dest.writeDouble(this.mSleepMaxValue);
        dest.writeDouble(this.mWakeMinValue);
        dest.writeDouble(this.mWakeMaxValue);
    }

    @Override // java.lang.Object
    public DeepSleepCluster clone() {
        try {
            return (DeepSleepCluster) super.clone();
        } catch (CloneNotSupportedException e) {
            SDKLog.e(TAG, e.getMessage());
            return null;
        }
    }
}
