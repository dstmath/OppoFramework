package com.color.direct;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class ColorDirectFindResult implements Parcelable {
    public static final Parcelable.Creator<ColorDirectFindResult> CREATOR = new Parcelable.Creator<ColorDirectFindResult>() {
        /* class com.color.direct.ColorDirectFindResult.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ColorDirectFindResult createFromParcel(Parcel in) {
            return new ColorDirectFindResult(in);
        }

        @Override // android.os.Parcelable.Creator
        public ColorDirectFindResult[] newArray(int size) {
            return new ColorDirectFindResult[size];
        }
    };
    public static final String ERROR_NO_MAINWIN = "no_mainwin";
    public static final String ERROR_NO_TEXT = "no_text";
    public static final String ERROR_NO_VIEW = "no_view";
    public static final String ERROR_NO_VIEWROOT = "no_viewroot";
    public static final String ERROR_UNKNOWN_CMD = "unknown_cmd";
    public static final String EXTRA_ERROR = "direct_find_error";
    public static final String EXTRA_NO_IDNAMES = "no_idnames";
    public static final String EXTRA_RESULT_TEXT = "result_text";
    private static final String TAG = "ColorDirectFindResult";
    private final Bundle mBundle = new Bundle();

    public ColorDirectFindResult() {
    }

    public ColorDirectFindResult(Parcel in) {
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
