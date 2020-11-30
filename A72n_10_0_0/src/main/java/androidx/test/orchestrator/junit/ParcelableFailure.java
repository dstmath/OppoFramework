package androidx.test.orchestrator.junit;

import android.os.Parcel;
import android.os.Parcelable;
import org.junit.runner.notification.Failure;

public final class ParcelableFailure implements Parcelable {
    public static final Parcelable.Creator<ParcelableFailure> CREATOR = new Parcelable.Creator<ParcelableFailure>() {
        /* class androidx.test.orchestrator.junit.ParcelableFailure.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ParcelableFailure createFromParcel(Parcel in) {
            return new ParcelableFailure(in);
        }

        @Override // android.os.Parcelable.Creator
        public ParcelableFailure[] newArray(int size) {
            return new ParcelableFailure[size];
        }
    };
    private final ParcelableDescription description;
    private final String trace;

    public ParcelableFailure(Failure failure) {
        this.description = new ParcelableDescription(failure.getDescription());
        this.trace = failure.getTrace();
    }

    private ParcelableFailure(Parcel in) {
        this.description = (ParcelableDescription) in.readParcelable(ParcelableDescription.class.getClassLoader());
        this.trace = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(this.description, 0);
        out.writeString(this.trace);
    }
}
