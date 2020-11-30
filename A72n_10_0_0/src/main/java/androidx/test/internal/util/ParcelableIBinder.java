package androidx.test.internal.util;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableIBinder implements Parcelable {
    public static final Parcelable.Creator<ParcelableIBinder> CREATOR = new Parcelable.Creator<ParcelableIBinder>() {
        /* class androidx.test.internal.util.ParcelableIBinder.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ParcelableIBinder createFromParcel(Parcel in) {
            return new ParcelableIBinder(in);
        }

        @Override // android.os.Parcelable.Creator
        public ParcelableIBinder[] newArray(int size) {
            return new ParcelableIBinder[size];
        }
    };
    private final IBinder iBinder;

    public ParcelableIBinder(IBinder iBinder2) {
        this.iBinder = (IBinder) Checks.checkNotNull(iBinder2);
    }

    public IBinder getIBinder() {
        return this.iBinder;
    }

    protected ParcelableIBinder(Parcel in) {
        this.iBinder = in.readStrongBinder();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStrongBinder(this.iBinder);
    }
}
