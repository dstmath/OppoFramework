package com.coloros.eventhub.sdk.aidl;

import android.os.Parcel;
import android.os.Parcelable;
import com.coloros.deepthinker.sdk.common.utils.SDKLog;
import java.util.Arrays;

public class DeviceEvent implements Parcelable {
    public static final Parcelable.Creator<DeviceEvent> CREATOR = new Parcelable.Creator<DeviceEvent>() {
        /* class com.coloros.eventhub.sdk.aidl.DeviceEvent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DeviceEvent createFromParcel(Parcel in) {
            return new DeviceEvent(in);
        }

        @Override // android.os.Parcelable.Creator
        public DeviceEvent[] newArray(int size) {
            return new DeviceEvent[size];
        }
    };
    private static final String TAG = "DeviceEvent";
    private int mEventStateType;
    private int mEventType;

    public static class Builder {
        private int mEventStateType = -1;
        private int mEventType = -1;

        public Builder setEventType(int eventType) {
            if (!EventType.sEventTypes.contains(Integer.valueOf(eventType))) {
                SDKLog.e(DeviceEvent.TAG, "Invalid event type, not yet supported.");
            }
            this.mEventType = eventType;
            return this;
        }

        public Builder setEventStateType(int eventStateType) {
            if (!(eventStateType == 0 || eventStateType == 1)) {
                SDKLog.e(DeviceEvent.TAG, "Invalid stateType.");
            }
            this.mEventStateType = eventStateType;
            return this;
        }

        public DeviceEvent build() {
            if (this.mEventType == -1) {
                SDKLog.e(DeviceEvent.TAG, "EventType not yet configured.");
            } else if (this.mEventStateType == -1) {
                SDKLog.w(DeviceEvent.TAG, "use default state type.");
                this.mEventStateType = 0;
            }
            return new DeviceEvent(this.mEventType, this.mEventStateType);
        }
    }

    private DeviceEvent(int eventType, int eventStateType) {
        this.mEventType = eventType;
        this.mEventStateType = eventStateType;
    }

    public int getEventType() {
        return this.mEventType;
    }

    public int getEventStateType() {
        return this.mEventStateType;
    }

    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (this == object) {
            return true;
        }
        if (!(object instanceof DeviceEvent)) {
            return false;
        }
        DeviceEvent event = (DeviceEvent) object;
        if (this.mEventType == event.getEventType() && this.mEventStateType == event.getEventStateType()) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return Arrays.hashCode(new Object[]{Integer.valueOf(this.mEventType), Integer.valueOf(this.mEventStateType)});
    }

    public DeviceEvent(Parcel in) {
        this.mEventType = in.readInt();
        this.mEventStateType = in.readInt();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int i) {
        dest.writeInt(this.mEventType);
        dest.writeInt(this.mEventStateType);
    }
}
