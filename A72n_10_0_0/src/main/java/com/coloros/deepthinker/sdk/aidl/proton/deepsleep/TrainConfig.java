package com.coloros.deepthinker.sdk.aidl.proton.deepsleep;

import android.os.Parcel;
import android.os.Parcelable;

public class TrainConfig implements Parcelable {
    private static final int CONFIG_DATA_LENGTH = 4;
    public static final Parcelable.Creator<TrainConfig> CREATOR = new Parcelable.Creator<TrainConfig>() {
        /* class com.coloros.deepthinker.sdk.aidl.proton.deepsleep.TrainConfig.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public TrainConfig createFromParcel(Parcel source) {
            TrainConfig trainConfig = new TrainConfig(0, 0.0d, 0);
            trainConfig.mClusterMinPoints = source.readInt();
            trainConfig.mClusterEps = source.readDouble();
            trainConfig.mDayForPredict = source.readInt();
            trainConfig.mType = source.readInt();
            return trainConfig;
        }

        @Override // android.os.Parcelable.Creator
        public TrainConfig[] newArray(int size) {
            return new TrainConfig[size];
        }
    };
    private static final int MULTIPLE = 1000;
    private static final int PRIME_NUM = 31;
    private static final double SIGMA = 1.0E-4d;
    private static final String SPLIT = ",";
    private double mClusterEps;
    private int mClusterMinPoints;
    private int mDayForPredict;
    private int mType = -1;

    public TrainConfig(int clusterMinPoints, double clusterEps, int dayForPredict) {
        this.mClusterMinPoints = clusterMinPoints;
        this.mClusterEps = clusterEps;
        this.mDayForPredict = dayForPredict;
    }

    public TrainConfig(String config) {
        String[] configDatas = config.split(",");
        if (configDatas.length == 4) {
            this.mClusterMinPoints = Integer.valueOf(configDatas[0]).intValue();
            this.mClusterEps = Double.valueOf(configDatas[1]).doubleValue();
            this.mDayForPredict = Integer.valueOf(configDatas[2]).intValue();
            this.mType = Integer.valueOf(configDatas[3]).intValue();
        }
    }

    public int getClusterMinPoints() {
        return this.mClusterMinPoints;
    }

    public void setClusterMinPoints(int clusterMinPoints) {
        this.mClusterMinPoints = clusterMinPoints;
    }

    public double getClusterEps() {
        return this.mClusterEps;
    }

    public void setClusterEps(double clusterEps) {
        this.mClusterEps = clusterEps;
    }

    public int getdayForPredict() {
        return this.mDayForPredict;
    }

    public void setdayForPredict(int dayForPredict) {
        this.mDayForPredict = dayForPredict;
    }

    public int getType() {
        return this.mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public String spliceParameter() {
        return this.mClusterMinPoints + "," + this.mClusterEps + "," + this.mDayForPredict + "," + this.mType;
    }

    public String toString() {
        return "TrainConfig{mClusterMinPoints=" + this.mClusterMinPoints + ", mClusterEps=" + this.mClusterEps + ", mDayForPredict=" + this.mDayForPredict + ", mType=" + this.mType + '}';
    }

    public int hashCode() {
        return (((((this.mClusterMinPoints * 31) + ((int) (this.mClusterEps * 1000.0d))) * 31) + this.mDayForPredict) * 31) + this.mType;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TrainConfig)) {
            return false;
        }
        TrainConfig config = (TrainConfig) obj;
        if (this.mClusterMinPoints != config.getClusterMinPoints() || Math.abs(this.mClusterEps - config.getClusterEps()) < SIGMA || this.mDayForPredict != config.getdayForPredict()) {
            return false;
        }
        if (this.mType == config.getType()) {
            return true;
        }
        return false;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mClusterMinPoints);
        dest.writeDouble(this.mClusterEps);
        dest.writeInt(this.mDayForPredict);
        dest.writeInt(this.mType);
    }
}
