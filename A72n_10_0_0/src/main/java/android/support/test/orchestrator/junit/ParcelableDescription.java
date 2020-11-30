package android.support.test.orchestrator.junit;

import android.os.Parcel;
import android.os.Parcelable;

public final class ParcelableDescription implements Parcelable {
    public static final Parcelable.Creator<ParcelableDescription> CREATOR = new Parcelable.Creator<ParcelableDescription>() {
        /* class android.support.test.orchestrator.junit.ParcelableDescription.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ParcelableDescription createFromParcel(Parcel in) {
            return new ParcelableDescription(in);
        }

        @Override // android.os.Parcelable.Creator
        public ParcelableDescription[] newArray(int size) {
            return new ParcelableDescription[size];
        }
    };
    private final String mClassName;
    private final String mDisplayName;
    private final String mMethodName;

    private ParcelableDescription(Parcel in) {
        this.mClassName = getNonNullString(in);
        this.mMethodName = getNonNullString(in);
        this.mDisplayName = getNonNullString(in);
    }

    private String getNonNullString(Parcel in) {
        String str = in.readString();
        return str == null ? "" : str;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mClassName);
        out.writeString(this.mMethodName);
        out.writeString(this.mDisplayName);
    }
}
