package android.os.storage;

import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.util.IndentingPrintWriter;

public abstract class OppoBaseStorageVolume implements Parcelable {
    public int mReadonlyType;

    protected OppoBaseStorageVolume() {
    }

    protected OppoBaseStorageVolume(int readonlyType) {
        this.mReadonlyType = readonlyType;
    }

    public int getReadOnlyType() {
        return this.mReadonlyType;
    }

    /* access modifiers changed from: protected */
    public void setReadOnlyType(int readonlyType) {
        this.mReadonlyType = readonlyType;
    }

    /* access modifiers changed from: protected */
    public void initFromParcel(Parcel in) {
        this.mReadonlyType = in.readInt();
    }

    public void dump(IndentingPrintWriter pw) {
        pw.printPair("mReadonlyType", Integer.valueOf(this.mReadonlyType));
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.mReadonlyType);
    }
}
