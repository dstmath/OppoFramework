package android.support.test.orchestrator.junit;

import android.os.Parcel;
import android.os.Parcelable;

public final class ParcelableFailure implements Parcelable {
    public static final Parcelable.Creator<ParcelableFailure> CREATOR = new Parcelable.Creator<ParcelableFailure>() {
        /* class android.support.test.orchestrator.junit.ParcelableFailure.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ParcelableFailure createFromParcel(Parcel in) {
            return new ParcelableFailure(in);
        }

        @Override // android.os.Parcelable.Creator
        public ParcelableFailure[] newArray(int size) {
            return new ParcelableFailure[size];
        }
    };
    private final ParcelableDescription mDescription;
    private final String mTrace;

    private ParcelableFailure(Parcel in) {
        this.mDescription = (ParcelableDescription) in.readParcelable(ParcelableDescription.class.getClassLoader());
        this.mTrace = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(this.mDescription, 0);
        out.writeString(this.mTrace);
    }
}
