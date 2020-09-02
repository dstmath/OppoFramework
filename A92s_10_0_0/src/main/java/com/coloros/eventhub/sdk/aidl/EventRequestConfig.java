package com.coloros.eventhub.sdk.aidl;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.ArraySet;
import com.coloros.deepthinker.sdk.common.utils.SDKLog;
import java.util.HashSet;
import java.util.Iterator;

public class EventRequestConfig implements Parcelable {
    public static final Parcelable.Creator<EventRequestConfig> CREATOR = new Parcelable.Creator<EventRequestConfig>() {
        /* class com.coloros.eventhub.sdk.aidl.EventRequestConfig.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public EventRequestConfig createFromParcel(Parcel in) {
            return new EventRequestConfig(in);
        }

        @Override // android.os.Parcelable.Creator
        public EventRequestConfig[] newArray(int size) {
            return new EventRequestConfig[size];
        }
    };
    private static final String TAG = "EventRequestConfig";
    private ArraySet<DeviceEvent> mDeviceEventSet;

    public EventRequestConfig(ArraySet<DeviceEvent> events) {
        if (events == null || events.size() == 0) {
            SDKLog.e(TAG, "Invalid event config,set is null or empty.");
            this.mDeviceEventSet = new ArraySet<>();
            return;
        }
        this.mDeviceEventSet = events;
    }

    public ArraySet<DeviceEvent> getDeviceEventSet() {
        if (this.mDeviceEventSet == null) {
            SDKLog.e(TAG, "Eventset not configured yet.");
            this.mDeviceEventSet = new ArraySet<>();
        }
        return this.mDeviceEventSet;
    }

    public HashSet<Integer> getAllEvents() {
        HashSet<Integer> hashSet = new HashSet<>();
        ArraySet<DeviceEvent> arraySet = this.mDeviceEventSet;
        if (arraySet != null) {
            Iterator<DeviceEvent> it = arraySet.iterator();
            while (it.hasNext()) {
                hashSet.add(Integer.valueOf(it.next().getEventType()));
            }
        }
        return hashSet;
    }

    /* JADX DEBUG: Type inference failed for r1v0. Raw type applied. Possible types: android.util.ArraySet<? extends java.lang.Object>, android.util.ArraySet<com.coloros.eventhub.sdk.aidl.DeviceEvent> */
    public EventRequestConfig(Parcel in) {
        this.mDeviceEventSet = in.readArraySet(EventRequestConfig.class.getClassLoader());
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int i) {
        dest.writeArraySet(this.mDeviceEventSet);
    }
}
