package android.util;

import android.os.Parcel;

public abstract class OppoBaseMergedConfiguration {
    public int mColorForceDarkValue;
    public boolean mIsUseColorForceDark;

    public void writeParcel(Parcel dest, int flags) {
        dest.writeBoolean(this.mIsUseColorForceDark);
        dest.writeInt(this.mColorForceDarkValue);
    }

    public void readParcel(Parcel source) {
        this.mIsUseColorForceDark = source.readBoolean();
        this.mColorForceDarkValue = source.readInt();
    }
}
