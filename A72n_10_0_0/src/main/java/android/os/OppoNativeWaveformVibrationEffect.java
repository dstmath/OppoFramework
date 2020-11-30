package android.os;

import android.os.Parcelable;
import java.util.Arrays;
import java.util.Objects;

public class OppoNativeWaveformVibrationEffect extends VibrationEffect implements Parcelable {
    public static final Parcelable.Creator<OppoNativeWaveformVibrationEffect> CREATOR = new Parcelable.Creator<OppoNativeWaveformVibrationEffect>() {
        /* class android.os.OppoNativeWaveformVibrationEffect.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public OppoNativeWaveformVibrationEffect createFromParcel(Parcel in) {
            in.readInt();
            return new OppoNativeWaveformVibrationEffect(in);
        }

        @Override // android.os.Parcelable.Creator
        public OppoNativeWaveformVibrationEffect[] newArray(int size) {
            return new OppoNativeWaveformVibrationEffect[size];
        }
    };
    public static final int PARCEL_TOKEN_OPPPO_NATIVE_WAVEFORM = 4;
    private int mEffectStrength;
    private final int mRepeat;
    private final long[] mTimings;
    private final int[] mWaveformIds;

    public OppoNativeWaveformVibrationEffect(Parcel in) {
        this(in.createLongArray(), in.createIntArray(), in.readInt());
        this.mEffectStrength = in.readInt();
    }

    public OppoNativeWaveformVibrationEffect(long[] timings, int[] waveformIds, int repeat) {
        this.mTimings = new long[timings.length];
        System.arraycopy(timings, 0, this.mTimings, 0, timings.length);
        this.mWaveformIds = new int[waveformIds.length];
        System.arraycopy(waveformIds, 0, this.mWaveformIds, 0, waveformIds.length);
        this.mRepeat = repeat;
        this.mEffectStrength = 2;
    }

    public long[] getTimings() {
        return this.mTimings;
    }

    public int[] getWaveformIds() {
        return this.mWaveformIds;
    }

    public int getRepeatIndex() {
        return this.mRepeat;
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

    @Override // android.os.VibrationEffect
    public long getDuration() {
        if (this.mRepeat >= 0) {
            return Long.MAX_VALUE;
        }
        long duration = 0;
        for (long d : this.mTimings) {
            duration += d;
        }
        return duration;
    }

    private static boolean isValidEffectStrength(int strength) {
        if (strength == 0 || strength == 1 || strength == 2) {
            return true;
        }
        return false;
    }

    @Override // android.os.VibrationEffect
    public void validate() {
        long[] jArr = this.mTimings;
        if (jArr.length != this.mWaveformIds.length) {
            throw new IllegalArgumentException("timing and waveform arrays must be of equal length (timings.length=" + this.mTimings.length + ", waveforms.length=" + this.mWaveformIds.length + ")");
        } else if (hasNonZeroEntry(jArr)) {
            for (long timing : this.mTimings) {
                if (timing < 0) {
                    throw new IllegalArgumentException("timings must all be >= 0 (timings=" + Arrays.toString(this.mTimings) + ")");
                }
            }
            int i = this.mRepeat;
            if (i < -1 || i >= this.mTimings.length) {
                throw new IllegalArgumentException("repeat index must be within the bounds of the timings array (timings.length=" + this.mTimings.length + ", index=" + this.mRepeat + ")");
            }
        } else {
            throw new IllegalArgumentException("at least one timing must be non-zero (timings=" + Arrays.toString(this.mTimings) + ")");
        }
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OppoNativeWaveformVibrationEffect that = (OppoNativeWaveformVibrationEffect) o;
        if (this.mEffectStrength != that.mEffectStrength || this.mRepeat != that.mRepeat || !Arrays.equals(this.mWaveformIds, that.mWaveformIds) || !Arrays.equals(this.mTimings, that.mTimings)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return (((Objects.hash(Integer.valueOf(this.mEffectStrength), Integer.valueOf(this.mRepeat)) * 31) + Arrays.hashCode(this.mWaveformIds)) * 31) + Arrays.hashCode(this.mTimings);
    }

    public String toString() {
        return "OppoNativeWaveformVibrationEffect{mTimings=" + Arrays.toString(this.mTimings) + ", mWaveformIds=" + Arrays.toString(this.mWaveformIds) + ", mRepeat=" + this.mRepeat + ", mEffectStrength=" + this.mEffectStrength + "}";
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(4);
        out.writeLongArray(this.mTimings);
        out.writeIntArray(this.mWaveformIds);
        out.writeInt(this.mRepeat);
        out.writeInt(this.mEffectStrength);
    }

    private static boolean hasNonZeroEntry(long[] vals) {
        for (long val : vals) {
            if (val != 0) {
                return true;
            }
        }
        return false;
    }
}
