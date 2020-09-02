package com.coloros.deepthinker.sdk.aidl.proton.deepsleep;

import android.os.Parcel;
import android.os.Parcelable;

public class TotalPredictResult implements Parcelable {
    public static final Parcelable.Creator<TotalPredictResult> CREATOR = new Parcelable.Creator<TotalPredictResult>() {
        /* class com.coloros.deepthinker.sdk.aidl.proton.deepsleep.TotalPredictResult.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public TotalPredictResult createFromParcel(Parcel parcel) {
            TotalPredictResult result = new TotalPredictResult(null, null, null, null);
            result.setSleepCluster((DeepSleepCluster) parcel.readParcelable(getClass().getClassLoader()));
            result.setWakeCluster((DeepSleepCluster) parcel.readParcelable(getClass().getClassLoader()));
            result.setOptimalSleepConfig((TrainConfig) parcel.readParcelable(getClass().getClassLoader()));
            result.setOptimalWakeConfig((TrainConfig) parcel.readParcelable(getClass().getClassLoader()));
            return result;
        }

        @Override // android.os.Parcelable.Creator
        public TotalPredictResult[] newArray(int size) {
            return new TotalPredictResult[size];
        }
    };
    private static final String NULL = "null";
    private static final String TAG = "TotalPredictResult";
    private TrainConfig mOptimalSleepConfig = null;
    private TrainConfig mOptimalWakeConfig = null;
    private DeepSleepCluster mSleepCluster = null;
    private DeepSleepCluster mWakeCluster = null;

    public TotalPredictResult(DeepSleepCluster sleepCluster, DeepSleepCluster wakeCluster) {
        this.mSleepCluster = sleepCluster;
        this.mWakeCluster = wakeCluster;
    }

    public TotalPredictResult(DeepSleepCluster sleepCluster, DeepSleepCluster wakeCluster, TrainConfig sleepConfig, TrainConfig wakeConfig) {
        this.mSleepCluster = sleepCluster;
        this.mWakeCluster = wakeCluster;
        this.mOptimalSleepConfig = sleepConfig;
        this.mOptimalWakeConfig = wakeConfig;
    }

    public DeepSleepCluster getSleepCluster() {
        return this.mSleepCluster;
    }

    public void setSleepCluster(DeepSleepCluster sleepCluster) {
        this.mSleepCluster = sleepCluster;
    }

    public DeepSleepCluster getWakeCluster() {
        return this.mWakeCluster;
    }

    public void setWakeCluster(DeepSleepCluster wakeCluster) {
        this.mWakeCluster = wakeCluster;
    }

    public void setOptimalSleepConfig(TrainConfig trainConfig) {
        this.mOptimalSleepConfig = trainConfig;
    }

    public void setOptimalWakeConfig(TrainConfig trainConfig) {
        this.mOptimalWakeConfig = trainConfig;
    }

    public TrainConfig getOptimalSleepConfig() {
        return this.mOptimalSleepConfig;
    }

    public TrainConfig getOptimalWakeConfig() {
        return this.mOptimalWakeConfig;
    }

    public String toString() {
        DeepSleepCluster deepSleepCluster = this.mSleepCluster;
        String wakeConfig = NULL;
        String sleepString = deepSleepCluster != null ? deepSleepCluster.toString() : wakeConfig;
        DeepSleepCluster deepSleepCluster2 = this.mWakeCluster;
        String wakeString = deepSleepCluster2 != null ? deepSleepCluster2.toString() : wakeConfig;
        TrainConfig trainConfig = this.mOptimalSleepConfig;
        String sleepConfig = trainConfig != null ? trainConfig.toString() : wakeConfig;
        TrainConfig trainConfig2 = this.mOptimalWakeConfig;
        if (trainConfig2 != null) {
            wakeConfig = trainConfig2.toString();
        }
        return String.format("mSleepCluster=%s,mSleepConfig=%s,mWakeCluster=%s,mWakeConfig=%s", sleepString, sleepConfig, wakeString, wakeConfig);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(this.mSleepCluster, i);
        parcel.writeParcelable(this.mWakeCluster, i);
        parcel.writeParcelable(this.mOptimalSleepConfig, i);
        parcel.writeParcelable(this.mOptimalWakeConfig, i);
    }
}
