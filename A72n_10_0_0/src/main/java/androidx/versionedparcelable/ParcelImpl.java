package androidx.versionedparcelable;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelImpl implements Parcelable {
    public static final Parcelable.Creator<ParcelImpl> CREATOR = new Parcelable.Creator<ParcelImpl>() {
        /* class androidx.versionedparcelable.ParcelImpl.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ParcelImpl createFromParcel(Parcel in) {
            return new ParcelImpl(in);
        }

        @Override // android.os.Parcelable.Creator
        public ParcelImpl[] newArray(int size) {
            return new ParcelImpl[size];
        }
    };
    private final VersionedParcelable mParcel;

    protected ParcelImpl(Parcel in) {
        this.mParcel = new VersionedParcelParcel(in).readVersionedParcelable();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        new VersionedParcelParcel(dest).writeVersionedParcelable(this.mParcel);
    }
}
