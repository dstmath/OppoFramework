package com.qualcomm.wfd;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.qualcomm.wfd.WfdEnums.SessionState;

public class WfdStatus implements Parcelable {
    public static final Creator<WfdStatus> CREATOR = new Creator<WfdStatus>() {
        public WfdStatus createFromParcel(Parcel source) {
            WfdStatus ret = new WfdStatus();
            ret.state = source.readInt();
            ret.sessionId = source.readInt();
            ret.connectedDevice = (WfdDevice) source.readValue(WfdDevice.class.getClassLoader());
            return ret;
        }

        public WfdStatus[] newArray(int size) {
            return new WfdStatus[size];
        }
    };
    public WfdDevice connectedDevice = null;
    public int sessionId = -1;
    public int state = SessionState.INVALID.ordinal();

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.state);
        dest.writeInt(this.sessionId);
        dest.writeValue(this.connectedDevice);
    }
}
