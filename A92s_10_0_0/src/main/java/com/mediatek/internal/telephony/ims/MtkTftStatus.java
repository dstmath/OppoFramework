package com.mediatek.internal.telephony.ims;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Iterator;

public class MtkTftStatus implements Parcelable {
    public static final Parcelable.Creator<MtkTftStatus> CREATOR = new Parcelable.Creator<MtkTftStatus>() {
        /* class com.mediatek.internal.telephony.ims.MtkTftStatus.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MtkTftStatus createFromParcel(Parcel source) {
            return MtkTftStatus.readFrom(source);
        }

        @Override // android.os.Parcelable.Creator
        public MtkTftStatus[] newArray(int size) {
            return new MtkTftStatus[size];
        }
    };
    public static final int OPCODE_ADD_PF = 3;
    public static final int OPCODE_CREATE_NEW_TFT = 1;
    public static final int OPCODE_DELETE_PF = 5;
    public static final int OPCODE_DELETE_TFT = 2;
    public static final int OPCODE_NOTFT_OP = 6;
    public static final int OPCODE_REPLACE_PF = 4;
    public static final int OPCODE_RESERVED = 7;
    public static final int OPCODE_SPARE = 0;
    public ArrayList<MtkPacketFilterInfo> mMtkPacketFilterInfoList;
    public MtkTftParameter mMtkTftParameter;
    public int mOperation = -1;

    public MtkTftStatus(int operation, ArrayList<MtkPacketFilterInfo> mtkPacketFilterInfo, MtkTftParameter mtkTftParameter) {
        this.mOperation = operation;
        this.mMtkPacketFilterInfoList = mtkPacketFilterInfo;
        this.mMtkTftParameter = mtkTftParameter;
    }

    public static MtkTftStatus readFrom(Parcel p) {
        int operation = p.readInt();
        int pfNumber = p.readInt();
        ArrayList<MtkPacketFilterInfo> pfList = new ArrayList<>();
        for (int i = 0; i < pfNumber; i++) {
            pfList.add(MtkPacketFilterInfo.readFrom(p));
        }
        return new MtkTftStatus(operation, pfList, MtkTftParameter.readFrom(p));
    }

    public void writeTo(Parcel p) {
        p.writeInt(this.mOperation);
        p.writeInt(this.mMtkPacketFilterInfoList.size());
        Iterator<MtkPacketFilterInfo> it = this.mMtkPacketFilterInfoList.iterator();
        while (it.hasNext()) {
            it.next().writeTo(p);
        }
        this.mMtkTftParameter.writeTo(p);
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("operation=" + this.mOperation + " [PacketFilterInfo");
        Iterator<MtkPacketFilterInfo> it = this.mMtkPacketFilterInfoList.iterator();
        while (it.hasNext()) {
            buf.append(it.next().toString());
        }
        buf.append("], TftParameter[" + this.mMtkTftParameter + "]]");
        return buf.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        writeTo(dest);
    }
}
