package com.coloros.deepthinker.sdk.aidl.proton.deepsleep;

import android.os.Parcel;
import android.os.Parcelable;

public class SleepRecord implements Parcelable {
    public static final Parcelable.Creator<SleepRecord> CREATOR = new Parcelable.Creator<SleepRecord>() {
        /* class com.coloros.deepthinker.sdk.aidl.proton.deepsleep.SleepRecord.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SleepRecord createFromParcel(Parcel source) {
            SleepRecord record = new SleepRecord(0, 0);
            record.mSleepTime = source.readLong();
            record.mWakeTime = source.readLong();
            return record;
        }

        @Override // android.os.Parcelable.Creator
        public SleepRecord[] newArray(int size) {
            return new SleepRecord[size];
        }
    };
    private long mSleepTime = 0;
    private long mWakeTime = 0;

    public SleepRecord(long sleepTime, long wakeTime) {
        this.mSleepTime = sleepTime;
        this.mWakeTime = wakeTime;
    }

    public long getSleepTime() {
        return this.mSleepTime;
    }

    public long getWakeTime() {
        return this.mWakeTime;
    }

    public void setSleepTime(long sleepTime) {
        this.mSleepTime = sleepTime;
    }

    public void setWakeTime(long wakeTime) {
        this.mWakeTime = wakeTime;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mSleepTime);
        dest.writeLong(this.mWakeTime);
    }
}
