package com.suntek.mway.rcs.client.aidl.plugin.entity.profile;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public class QRCardBusinessFlag extends BaseModel implements Parcelable {
    public static final Creator<QRCardBusinessFlag> CREATOR = new Creator<QRCardBusinessFlag>() {
        public QRCardBusinessFlag createFromParcel(Parcel source) {
            return new QRCardBusinessFlag(source);
        }

        public QRCardBusinessFlag[] newArray(int size) {
            return new QRCardBusinessFlag[size];
        }
    };
    private boolean businessFlag;

    public QRCardBusinessFlag(Parcel source) {
        readFromParcel(source);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeBooleanArray(new boolean[]{this.businessFlag});
    }

    public void readFromParcel(Parcel source) {
        super.readFromParcel(source);
        boolean[] val = new boolean[1];
        source.readBooleanArray(val);
        this.businessFlag = val[0];
    }

    public boolean isBusinessFlag() {
        return this.businessFlag;
    }

    public void setBusinessFlag(boolean businessFlag) {
        this.businessFlag = businessFlag;
    }

    public String toString() {
        List<String> list = new ArrayList();
        list.add("businessFlag=" + this.businessFlag);
        list.add("account=" + getAccount());
        list.add("etag=" + getEtag());
        return list.toString();
    }
}
