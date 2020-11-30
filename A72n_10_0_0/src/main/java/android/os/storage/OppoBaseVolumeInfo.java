package android.os.storage;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.android.internal.util.IndentingPrintWriter;

public abstract class OppoBaseVolumeInfo implements Parcelable {
    public int readOnlyType = -1;

    public int getReadOnlyType() {
        return this.readOnlyType;
    }

    public void setReadOnlyTypeValue(int value) {
        if (value < -1 || value > 2) {
            Log.w("OppoBaseVolumeInfo", "value illegal, must in [-1, 2]");
        } else {
            this.readOnlyType = value;
        }
    }

    /* access modifiers changed from: protected */
    public void initFromParcel(Parcel in) {
        this.readOnlyType = in.readInt();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(this.readOnlyType);
    }

    /* access modifiers changed from: protected */
    public void dump(IndentingPrintWriter pw) {
        pw.println();
        pw.printPair("readOnlyType", Integer.valueOf(this.readOnlyType));
    }
}
