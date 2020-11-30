package oppo.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;

public class HotspotClient implements Parcelable {
    public static final Parcelable.Creator<HotspotClient> CREATOR = new Parcelable.Creator<HotspotClient>() {
        /* class oppo.net.wifi.HotspotClient.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public HotspotClient createFromParcel(Parcel in) {
            String readString = in.readString();
            boolean z = true;
            if (in.readByte() != 1) {
                z = false;
            }
            return new HotspotClient(readString, z, in.readString(), in.readString());
        }

        @Override // android.os.Parcelable.Creator
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

    public HotspotClient(String address, boolean blocked, String name2, String time) {
        this.deviceAddress = address;
        this.isBlocked = blocked;
        this.name = name2;
        this.conTime = time;
    }

    public HotspotClient(String address, boolean blocked, String name2) {
        this.deviceAddress = address;
        this.isBlocked = blocked;
        this.name = name2;
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
        sbuf.append(" deviceAddress: ");
        sbuf.append(this.deviceAddress);
        sbuf.append('\n');
        sbuf.append(" isBlocked: ");
        sbuf.append(this.isBlocked);
        sbuf.append("\n");
        sbuf.append(" name: ");
        sbuf.append(this.name);
        sbuf.append("\n");
        sbuf.append(" conTime: ");
        sbuf.append(this.conTime);
        sbuf.append("\n");
        return sbuf.toString();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.deviceAddress);
        dest.writeByte(this.isBlocked ? (byte) 1 : 0);
        dest.writeString(this.name);
        dest.writeString(this.conTime);
    }
}
