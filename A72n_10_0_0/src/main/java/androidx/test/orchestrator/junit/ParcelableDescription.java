package androidx.test.orchestrator.junit;

import android.os.Parcel;
import android.os.Parcelable;
import org.junit.runner.Description;

public final class ParcelableDescription implements Parcelable {
    public static final Parcelable.Creator<ParcelableDescription> CREATOR = new Parcelable.Creator<ParcelableDescription>() {
        /* class androidx.test.orchestrator.junit.ParcelableDescription.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ParcelableDescription createFromParcel(Parcel in) {
            return new ParcelableDescription(in);
        }

        @Override // android.os.Parcelable.Creator
        public ParcelableDescription[] newArray(int size) {
            return new ParcelableDescription[size];
        }
    };
    private final String className;
    private final String displayName;
    private final String methodName;

    public ParcelableDescription(Description description) {
        this.className = description.getClassName();
        this.methodName = description.getMethodName();
        this.displayName = description.getDisplayName();
    }

    private ParcelableDescription(Parcel in) {
        this.className = getNonNullString(in);
        this.methodName = getNonNullString(in);
        this.displayName = getNonNullString(in);
    }

    private String getNonNullString(Parcel in) {
        String str = in.readString();
        return str == null ? "" : str;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.className);
        out.writeString(this.methodName);
        out.writeString(this.displayName);
    }
}
