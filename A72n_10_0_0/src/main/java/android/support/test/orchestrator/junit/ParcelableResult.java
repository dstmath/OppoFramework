package android.support.test.orchestrator.junit;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public final class ParcelableResult implements Parcelable {
    public static final Parcelable.Creator<ParcelableResult> CREATOR = new Parcelable.Creator<ParcelableResult>() {
        /* class android.support.test.orchestrator.junit.ParcelableResult.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ParcelableResult createFromParcel(Parcel in) {
            return new ParcelableResult(in);
        }

        @Override // android.os.Parcelable.Creator
        public ParcelableResult[] newArray(int size) {
            return new ParcelableResult[size];
        }
    };
    private final List<ParcelableFailure> mFailures;

    private ParcelableResult(Parcel in) {
        this.mFailures = new ArrayList();
        for (Object failure : in.readArray(ParcelableFailure[].class.getClassLoader())) {
            this.mFailures.add((ParcelableFailure) failure);
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeArray(this.mFailures.toArray());
    }
}
