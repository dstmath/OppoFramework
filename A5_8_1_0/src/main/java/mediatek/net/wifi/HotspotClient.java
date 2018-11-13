package mediatek.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class HotspotClient implements Parcelable {
    public static final Creator<HotspotClient> CREATOR = new Creator<HotspotClient>() {
        public HotspotClient createFromParcel(Parcel in) {
            boolean z = true;
            String readString = in.readString();
            if (in.readByte() != (byte) 1) {
                z = false;
            }
            return new HotspotClient(readString, z, in.readString(), in.readString());
        }

        public HotspotClient[] newArray(int size) {
            return new HotspotClient[size];
        }
    };
    public String conTime;
    public String deviceAddress;
    public boolean isBlocked = false;
    public String name;

    public HotspotClient(String address, boolean blocked) {
        this.deviceAddress = address;
        this.isBlocked = blocked;
    }

    public HotspotClient(String address, boolean blocked, String name, String time) {
        this.deviceAddress = address;
        this.isBlocked = blocked;
        this.name = name;
        this.conTime = time;
    }

    public HotspotClient(String address, boolean blocked, String name) {
        this.deviceAddress = address;
        this.isBlocked = blocked;
        this.name = name;
    }

    public HotspotClient(HotspotClient source) {
        if (source != null) {
            this.deviceAddress = source.deviceAddress;
            this.isBlocked = source.isBlocked;
            this.name = source.name;
            this.conTime = source.conTime;
        }
    }

    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append(" deviceAddress: ").append(this.deviceAddress);
        sbuf.append(10);
        sbuf.append(" isBlocked: ").append(this.isBlocked);
        sbuf.append("\n");
        sbuf.append(" name: ").append(this.name);
        sbuf.append("\n");
        sbuf.append(" conTime: ").append(this.conTime);
        sbuf.append("\n");
        return sbuf.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.deviceAddress);
        dest.writeByte(this.isBlocked ? (byte) 1 : (byte) 0);
        dest.writeString(this.name);
        dest.writeString(this.conTime);
    }
}
