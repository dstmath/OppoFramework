package com.qti.debugreport;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;

public class IZatUtcSpec implements Parcelable {
    public static final Creator<IZatUtcSpec> CREATOR = new Creator<IZatUtcSpec>() {
        public IZatUtcSpec createFromParcel(Parcel source) {
            return new IZatUtcSpec(source);
        }

        public IZatUtcSpec[] newArray(int size) {
            return new IZatUtcSpec[size];
        }
    };
    private static String TAG = "IZatUtcSpec";
    private static final boolean VERBOSE = Log.isLoggable(TAG, 2);
    private long mNanoSeconds;
    private long mWholeSeconds;

    public IZatUtcSpec(long seconds, long nanoseconds) {
        this.mWholeSeconds = seconds;
        this.mNanoSeconds = nanoseconds;
    }

    public IZatUtcSpec(Parcel source) {
        this.mWholeSeconds = source.readLong();
        this.mNanoSeconds = source.readLong();
    }

    public long getSeconds() {
        return this.mWholeSeconds;
    }

    public long getNanoSeconds() {
        return this.mNanoSeconds;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mWholeSeconds);
        dest.writeLong(this.mNanoSeconds);
    }
}
