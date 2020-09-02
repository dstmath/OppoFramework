package com.color.favorite;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class ColorFavoriteQueryResult implements Parcelable {
    public static final Parcelable.Creator<ColorFavoriteQueryResult> CREATOR = new Parcelable.Creator<ColorFavoriteQueryResult>() {
        /* class com.color.favorite.ColorFavoriteQueryResult.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ColorFavoriteQueryResult createFromParcel(Parcel in) {
            return new ColorFavoriteQueryResult(in);
        }

        @Override // android.os.Parcelable.Creator
        public ColorFavoriteQueryResult[] newArray(int size) {
            return new ColorFavoriteQueryResult[size];
        }
    };
    public static final String EXTRA_DATA = "data";
    public static final String EXTRA_ERROR = "error";
    private static final String TAG = "ColorFavoriteQueryResult";
    private final Bundle mBundle = new Bundle();

    public ColorFavoriteQueryResult() {
    }

    public ColorFavoriteQueryResult(Parcel in) {
        readFromParcel(in);
    }

    public String toString() {
        return "Result=" + this.mBundle;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        this.mBundle.writeToParcel(out, flags);
    }

    public void readFromParcel(Parcel in) {
        this.mBundle.readFromParcel(in);
    }

    public Bundle getBundle() {
        return this.mBundle;
    }
}
