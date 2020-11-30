package android.os;

import android.os.Parcelable;

public class OppoThermalState implements Parcelable {
    public static final Parcelable.Creator<OppoThermalState> CREATOR = new Parcelable.Creator<OppoThermalState>() {
        /* class android.os.OppoThermalState.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public OppoThermalState createFromParcel(Parcel in) {
            return new OppoThermalState(in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readInt() == 1, in.readInt(), in.readInt(), in.readInt());
        }

        @Override // android.os.Parcelable.Creator
        public OppoThermalState[] newArray(int size) {
            return new OppoThermalState[size];
        }
    };
    int mBatteryCurrent;
    int mBatteryLevel;
    int mBatteryRm;
    int mBatteryTemperature;
    int mChargeId;
    int mFast2Normal;
    int mFcc;
    boolean mIsFastCharge;
    int mPlugType;
    int mThermalHeat;
    int mThermalHeat1;
    int mThermalHeat2;
    int mThermalHeat3;

    public OppoThermalState(int plugType, int fcc, int batteryRm, int thermalHeat, int thermalHeat1, int thermalHeat2, int thermalHeat3, int fast2Normal, int chargeId, boolean isFastCharge, int batteryCurrent, int batteryLevel, int batteryTemperature) {
        this.mPlugType = plugType;
        this.mFcc = fcc;
        this.mBatteryRm = batteryRm;
        this.mThermalHeat = thermalHeat;
        this.mThermalHeat1 = thermalHeat1;
        this.mThermalHeat2 = thermalHeat2;
        this.mThermalHeat3 = thermalHeat3;
        this.mFast2Normal = fast2Normal;
        this.mChargeId = chargeId;
        this.mIsFastCharge = isFastCharge;
        this.mBatteryCurrent = batteryCurrent;
        this.mBatteryLevel = batteryLevel;
        this.mBatteryTemperature = batteryTemperature;
    }

    public String toString() {
        return "OppoThermalState{pluginType:" + this.mPlugType + ", fcc:" + this.mFcc + ", mBatteryRm:" + this.mBatteryRm + ", mThermalHeat:" + this.mThermalHeat + ", mThermalHeat1:" + this.mThermalHeat1 + ", mThermalHeat2:" + this.mThermalHeat2 + ", mThermalHeat3:" + this.mThermalHeat3 + ", mFast2Normal:" + this.mFast2Normal + ", mChargeId:" + this.mChargeId + ", mIsFastCharge:" + this.mIsFastCharge + ", mBatteryCurrent:" + this.mBatteryCurrent + ", mBatteryLevel:" + this.mBatteryLevel + ", mBatteryTemperature:" + this.mBatteryTemperature + "}";
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mPlugType);
        dest.writeInt(this.mFcc);
        dest.writeInt(this.mBatteryRm);
        dest.writeInt(this.mThermalHeat);
        dest.writeInt(this.mThermalHeat1);
        dest.writeInt(this.mThermalHeat2);
        dest.writeInt(this.mThermalHeat3);
        dest.writeInt(this.mFast2Normal);
        dest.writeInt(this.mChargeId);
        dest.writeInt(this.mIsFastCharge ? 1 : 0);
        dest.writeInt(this.mBatteryCurrent);
        dest.writeInt(this.mBatteryLevel);
        dest.writeInt(this.mBatteryTemperature);
    }

    public int getPlugType() {
        return this.mPlugType;
    }

    public int getFcc() {
        return this.mFcc;
    }

    public int getBatteryRm() {
        return this.mBatteryRm;
    }

    public int getThermalHeat(int index) {
        if (index == 0) {
            return this.mThermalHeat;
        }
        if (index == 1) {
            return this.mThermalHeat1;
        }
        if (index == 2) {
            return this.mThermalHeat2;
        }
        if (index != 3) {
            return -1;
        }
        return this.mThermalHeat3;
    }

    public int getFast2Normal() {
        return this.mFast2Normal;
    }

    public int getChargeId() {
        return this.mChargeId;
    }

    public boolean getIsFastCharge() {
        return this.mIsFastCharge;
    }

    public int getBatteryCurrent() {
        return this.mBatteryCurrent;
    }

    public int getBatteryLevel() {
        return this.mBatteryLevel;
    }

    public int getBatteryTemperature() {
        return this.mBatteryTemperature;
    }
}
