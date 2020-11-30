package com.color.inner.os;

import android.os.Parcel;
import android.util.ArraySet;
import android.util.Log;

public class ParcelWrapper {
    private static final String TAG = "ParcelWrapper";

    public static void writeStringNoHelper(Parcel parcel, String val) {
        try {
            parcel.writeStringNoHelper(val);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static String readStringNoHelper(Parcel parcel) {
        try {
            return parcel.readStringNoHelper();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static final String[] readStringArray(Parcel parcel) {
        try {
            return parcel.readStringArray();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static ArraySet<? extends Object> readArraySet(Parcel parcel, ClassLoader loader) {
        return parcel.readArraySet(loader);
    }

    public static void writeArraySet(Parcel parcel, ArraySet<? extends Object> val) {
        parcel.writeArraySet(val);
    }
}
