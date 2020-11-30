package androidx.test.runner.internal.deps.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class Codecs {
    private static final ClassLoader CLASS_LOADER = Codecs.class.getClassLoader();

    private Codecs() {
    }

    public static void writeParcelable(Parcel parcel, Parcelable parcelable) {
        if (parcelable == null) {
            parcel.writeInt(0);
            return;
        }
        parcel.writeInt(1);
        parcelable.writeToParcel(parcel, 0);
    }
}
