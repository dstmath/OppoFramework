package com.mediatek.internal.telephony.ims;

import android.os.Parcel;
import android.os.Parcelable;

public class MtkPacketFilterInfo implements Parcelable {
    public static final Parcelable.Creator<MtkPacketFilterInfo> CREATOR = new Parcelable.Creator<MtkPacketFilterInfo>() {
        /* class com.mediatek.internal.telephony.ims.MtkPacketFilterInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MtkPacketFilterInfo createFromParcel(Parcel source) {
            return MtkPacketFilterInfo.readFrom(source);
        }

        @Override // android.os.Parcelable.Creator
        public MtkPacketFilterInfo[] newArray(int size) {
            return new MtkPacketFilterInfo[size];
        }
    };
    public static final int IMC_BMP_FLOW_LABEL = 512;
    public static final int IMC_BMP_LOCAL_PORT_RANGE = 16;
    public static final int IMC_BMP_LOCAL_PORT_SINGLE = 8;
    public static final int IMC_BMP_NONE = 0;
    public static final int IMC_BMP_PROTOCOL = 4;
    public static final int IMC_BMP_REMOTE_PORT_RANGE = 64;
    public static final int IMC_BMP_REMOTE_PORT_SINGLE = 32;
    public static final int IMC_BMP_SPI = 128;
    public static final int IMC_BMP_TOS = 256;
    public static final int IMC_BMP_V4_ADDR = 1;
    public static final int IMC_BMP_V6_ADDR = 2;
    public String mAddress;
    public int mBitmap;
    public int mDirection;
    public int mFlowLabel;
    public int mId;
    public int mLocalPortHigh;
    public int mLocalPortLow;
    public String mMask;
    public int mNetworkPfIdentifier;
    public int mPrecedence;
    public int mProtocolNextHeader;
    public int mRemotePortHigh;
    public int mRemotePortLow;
    public int mSpi;
    public int mTos;
    public int mTosMask;

    public MtkPacketFilterInfo(int id, int precedence, int direction, int networkPfIdentifier, int bitmap, String address, String mask, int protocolNextHeader, int localPortLow, int localPortHigh, int remotePortLow, int remotePortHigh, int spi, int tos, int tosMask, int flowLabel) {
        this.mId = id;
        this.mPrecedence = precedence;
        this.mDirection = direction;
        this.mNetworkPfIdentifier = networkPfIdentifier;
        this.mBitmap = bitmap;
        this.mAddress = address;
        this.mMask = mask;
        this.mProtocolNextHeader = protocolNextHeader;
        this.mLocalPortLow = localPortLow;
        this.mLocalPortHigh = localPortHigh;
        this.mRemotePortLow = remotePortLow;
        this.mRemotePortHigh = remotePortHigh;
        this.mSpi = spi;
        this.mTos = tos;
        this.mTosMask = tosMask;
        this.mFlowLabel = flowLabel;
    }

    public static MtkPacketFilterInfo readFrom(Parcel p) {
        return new MtkPacketFilterInfo(p.readInt(), p.readInt(), p.readInt(), p.readInt(), p.readInt(), p.readString(), p.readString(), p.readInt(), p.readInt(), p.readInt(), p.readInt(), p.readInt(), p.readInt(), p.readInt(), p.readInt(), p.readInt());
    }

    public void writeTo(Parcel p) {
        p.writeInt(this.mId);
        p.writeInt(this.mPrecedence);
        p.writeInt(this.mDirection);
        p.writeInt(this.mNetworkPfIdentifier);
        p.writeInt(this.mBitmap);
        String str = this.mAddress;
        if (str == null) {
            str = "";
        }
        p.writeString(str);
        String str2 = this.mMask;
        if (str2 == null) {
            str2 = "";
        }
        p.writeString(str2);
        p.writeInt(this.mProtocolNextHeader);
        p.writeInt(this.mLocalPortLow);
        p.writeInt(this.mLocalPortHigh);
        p.writeInt(this.mRemotePortLow);
        p.writeInt(this.mRemotePortHigh);
        p.writeInt(this.mSpi);
        p.writeInt(this.mTos);
        p.writeInt(this.mTosMask);
        p.writeInt(this.mFlowLabel);
    }

    public String toString() {
        return "[id=" + this.mId + ", precedence=" + this.mPrecedence + ", direction=" + this.mDirection + ", networkPfIdentifier=" + this.mNetworkPfIdentifier + ", bitmap=" + Integer.toHexString(this.mBitmap) + ", address=" + this.mAddress + ", mask=" + this.mMask + ", protocolNextHeader=" + this.mProtocolNextHeader + ", localPortLow=" + this.mLocalPortLow + ", localPortHigh=" + this.mLocalPortHigh + ", remotePortLow=" + this.mRemotePortLow + ", remotePortHigh=" + this.mRemotePortHigh + ", spi=" + Integer.toHexString(this.mSpi) + ", tos=" + this.mTos + ", tosMask=" + this.mTosMask + ", flowLabel=" + Integer.toHexString(this.mFlowLabel) + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        writeTo(dest);
    }
}
