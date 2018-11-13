package com.qualcomm.wfd;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.qualcomm.wfd.WfdEnums.NetType;
import com.qualcomm.wfd.WfdEnums.WFDDeviceType;

public class WfdDevice implements Parcelable {
    public static final Creator<WfdDevice> CREATOR = new Creator<WfdDevice>() {
        public WfdDevice createFromParcel(Parcel in) {
            return new WfdDevice(in);
        }

        public WfdDevice[] newArray(int size) {
            return new WfdDevice[size];
        }
    };
    public String addressOfAP;
    public Bundle capabilities;
    public int coupledSinkStatus;
    public int decoderLatency;
    public String deviceName;
    public int deviceType;
    public int extSupport;
    public String ipAddress;
    public boolean isAvailableForSession;
    public String macAddress;
    public int netType;
    public int preferredConnectivity;
    public int rtspPort;

    public WfdDevice() {
        this.deviceType = WFDDeviceType.UNKNOWN.getCode();
        this.netType = NetType.UNKNOWN_NET.ordinal();
        this.rtspPort = -1;
        this.capabilities = new Bundle();
    }

    public WfdDevice(Parcel in) {
        readFromParcel(in);
    }

    private void readFromParcel(Parcel in) {
        boolean z = false;
        this.deviceType = in.readInt();
        this.netType = in.readInt();
        this.macAddress = in.readString();
        this.deviceName = in.readString();
        this.ipAddress = in.readString();
        this.rtspPort = in.readInt();
        this.decoderLatency = in.readInt();
        if (in.readByte() != (byte) 0) {
            z = true;
        }
        this.isAvailableForSession = z;
        this.preferredConnectivity = in.readInt();
        this.addressOfAP = in.readString();
        this.coupledSinkStatus = in.readInt();
        this.extSupport = in.readInt();
        this.capabilities = in.readBundle();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.deviceType);
        dest.writeInt(this.netType);
        dest.writeString(this.macAddress);
        dest.writeString(this.deviceName);
        dest.writeString(this.ipAddress);
        dest.writeInt(this.rtspPort);
        dest.writeInt(this.decoderLatency);
        dest.writeByte((byte) (this.isAvailableForSession ? 1 : 0));
        dest.writeInt(this.preferredConnectivity);
        dest.writeString(this.addressOfAP);
        dest.writeInt(this.coupledSinkStatus);
        dest.writeInt(this.extSupport);
        dest.writeBundle(this.capabilities);
    }
}
