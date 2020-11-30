package com.mediatek.powerhalmgr;

import android.os.Parcel;
import android.os.Parcelable;

public class DupLinkInfo implements Parcelable {
    public static final Parcelable.Creator<DupLinkInfo> CREATOR = new Parcelable.Creator<DupLinkInfo>() {
        /* class com.mediatek.powerhalmgr.DupLinkInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DupLinkInfo createFromParcel(Parcel in) {
            return new DupLinkInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public DupLinkInfo[] newArray(int size) {
            return new DupLinkInfo[size];
        }
    };
    private String mDstIp;
    private int mDstPort;
    private int mProto;
    private String mSrcIp;
    private int mSrcPort;

    public int describeContents() {
        return 0;
    }

    public String getSrcIp() {
        return this.mSrcIp;
    }

    public String getDstIp() {
        return this.mDstIp;
    }

    public int getSrcPort() {
        return this.mSrcPort;
    }

    public int getDstPort() {
        return this.mDstPort;
    }

    public int getProto() {
        return this.mProto;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mSrcIp);
        out.writeString(this.mDstIp);
        out.writeInt(this.mSrcPort);
        out.writeInt(this.mDstPort);
        out.writeInt(this.mProto);
    }

    public void readFromParcel(Parcel in) {
        this.mSrcIp = in.readString();
        this.mDstIp = in.readString();
        this.mSrcPort = in.readInt();
        this.mDstPort = in.readInt();
        this.mProto = in.readInt();
    }

    public DupLinkInfo(String src_ip, String dst_ip, int src_port, int dst_port, int proto) {
        this.mSrcIp = src_ip;
        this.mDstIp = dst_ip;
        this.mSrcPort = src_port;
        this.mDstPort = dst_port;
        this.mProto = proto;
    }

    private DupLinkInfo(Parcel in) {
        this.mSrcIp = in.readString();
        this.mDstIp = in.readString();
        this.mSrcPort = in.readInt();
        this.mDstPort = in.readInt();
        this.mProto = in.readInt();
    }

    public String toString() {
        return "DupLinkInfo(" + this.mSrcIp + "," + this.mDstIp + "," + this.mSrcPort + "," + this.mDstPort + "," + this.mProto + ")";
    }
}
