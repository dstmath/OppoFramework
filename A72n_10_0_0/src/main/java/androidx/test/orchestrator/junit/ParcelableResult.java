package androidx.test.orchestrator.junit;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public final class ParcelableResult implements Parcelable {
    public static final Parcelable.Creator<ParcelableResult> CREATOR = new Parcelable.Creator<ParcelableResult>() {
        /* class androidx.test.orchestrator.junit.ParcelableResult.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ParcelableResult createFromParcel(Parcel in) {
            return new ParcelableResult(in);
        }

        @Override // android.os.Parcelable.Creator
        public ParcelableResult[] newArray(int size) {
            return new ParcelableResult[size];
        }
    };
    private final List<ParcelableFailure> failures;

    public ParcelableResult(Result result) {
        this.failures = new ArrayList();
        for (Failure failure : result.getFailures()) {
            this.failures.add(new ParcelableFailure(failure));
        }
    }

    private ParcelableResult(Parcel in) {
        this.failures = new ArrayList();
        for (Object failure : in.readArray(ParcelableFailure[].class.getClassLoader())) {
            this.failures.add((ParcelableFailure) failure);
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeArray(this.failures.toArray());
    }
}
