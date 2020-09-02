package android.os;

import android.os.Parcelable;
import java.util.Objects;

public class OppoNativeOneShotVibrationEffect extends VibrationEffect implements Parcelable {
    public static final Parcelable.Creator<OppoNativeOneShotVibrationEffect> CREATOR = new Parcelable.Creator<OppoNativeOneShotVibrationEffect>() {
        /* class android.os.OppoNativeOneShotVibrationEffect.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public OppoNativeOneShotVibrationEffect createFromParcel(Parcel in) {
            in.readInt();
            return new OppoNativeOneShotVibrationEffect(in);
        }

        @Override // android.os.Parcelable.Creator
        public OppoNativeOneShotVibrationEffect[] newArray(int size) {
            return new OppoNativeOneShotVibrationEffect[size];
        }
    };
    public static final int PARCEL_TOKEN_OPPPO_NATIVE_ONESHOT = 5;
    private int mEffectStrength;
    private final long mTiming;
    private final int mWaveformId;

    public OppoNativeOneShotVibrationEffect(Parcel in) {
        this(in.readInt(), in.readLong());
        this.mEffectStrength = in.readInt();
    }

    public OppoNativeOneShotVibrationEffect(int waveformId, long timing) {
        this.mWaveformId = waveformId;
        this.mTiming = timing;
        this.mEffectStrength = 2;
    }

    public int getId() {
        return this.mWaveformId;
    }

    @Override // android.os.VibrationEffect
    public long getDuration() {
        return this.mTiming;
    }

    public void setEffectStrength(int strength) {
        if (isValidEffectStrength(strength)) {
            this.mEffectStrength = strength;
            return;
        }
        throw new IllegalArgumentException("Invalid effect strength: " + strength);
    }

    public int getEffectStrength() {
        return this.mEffectStrength;
    }

    private static boolean isValidEffectStrength(int strength) {
        if (strength == 0 || strength == 1 || strength == 2) {
            return true;
        }
        return false;
    }

    @Override // android.os.VibrationEffect
    public void validate() {
        if (this.mTiming <= 0) {
            throw new IllegalArgumentException("timing must be non-zero (timing=" + this.mTiming + ")");
        } else if (!isValidEffectStrength(this.mEffectStrength)) {
            throw new IllegalArgumentException("Unknown effect strength (value=" + this.mEffectStrength + ")");
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OppoNativeOneShotVibrationEffect that = (OppoNativeOneShotVibrationEffect) o;
        if (this.mWaveformId == that.mWaveformId && this.mTiming == that.mTiming && this.mEffectStrength == that.mEffectStrength) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.mWaveformId), Long.valueOf(this.mTiming), Integer.valueOf(this.mEffectStrength));
    }

    public String toString() {
        return "OppoNativeOneShotVibrationEffect{mWaveformId=" + this.mWaveformId + ", mTiming=" + this.mTiming + ", mEffectStrength=" + this.mEffectStrength + "}";
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(5);
        out.writeInt(this.mWaveformId);
        out.writeLong(this.mTiming);
        out.writeInt(this.mEffectStrength);
    }
}
