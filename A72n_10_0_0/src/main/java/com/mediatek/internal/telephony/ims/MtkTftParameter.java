package com.mediatek.internal.telephony.ims;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Iterator;

public class MtkTftParameter implements Parcelable {
    public static final Parcelable.Creator<MtkTftParameter> CREATOR = new Parcelable.Creator<MtkTftParameter>() {
        /* class com.mediatek.internal.telephony.ims.MtkTftParameter.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MtkTftParameter createFromParcel(Parcel source) {
            return MtkTftParameter.readFrom(source);
        }

        @Override // android.os.Parcelable.Creator
        public MtkTftParameter[] newArray(int size) {
            return new MtkTftParameter[size];
        }
    };
    public ArrayList<Integer> mLinkedPacketFilterIdList;

    public MtkTftParameter(ArrayList<Integer> linkedPacketFilterIdList) {
        this.mLinkedPacketFilterIdList = linkedPacketFilterIdList;
    }

    public static MtkTftParameter readFrom(Parcel p) {
        int linkedPfNumber = p.readInt();
        ArrayList<Integer> linkedPacketFilterIdList = new ArrayList<>();
        for (int i = 0; i < linkedPfNumber; i++) {
            linkedPacketFilterIdList.add(Integer.valueOf(p.readInt()));
        }
        return new MtkTftParameter(linkedPacketFilterIdList);
    }

    public void writeTo(Parcel p) {
        p.writeInt(this.mLinkedPacketFilterIdList.size());
        Iterator<Integer> it = this.mLinkedPacketFilterIdList.iterator();
        while (it.hasNext()) {
            p.writeInt(it.next().intValue());
        }
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("LinkedPacketFilterIdList[");
        Iterator<Integer> it = this.mLinkedPacketFilterIdList.iterator();
        while (it.hasNext()) {
            buf.append(it.next() + " ");
        }
        buf.append("]");
        return buf.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        writeTo(dest);
    }
}
