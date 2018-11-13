package com.suntek.mway.rcs.client.aidl.service.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public class SimpleMessage implements Parcelable {
    public static final Creator<SimpleMessage> CREATOR = new Creator<SimpleMessage>() {
        public SimpleMessage createFromParcel(Parcel in) {
            return new SimpleMessage(in);
        }

        public SimpleMessage[] newArray(int size) {
            return new SimpleMessage[size];
        }
    };
    private long messageRowId;
    private int storeType;

    public SimpleMessage(Parcel in) {
        readFromParcel(in);
    }

    public long getMessageRowId() {
        return this.messageRowId;
    }

    public void setMessageRowId(long messageRowId) {
        this.messageRowId = messageRowId;
    }

    public int getStoreType() {
        return this.storeType;
    }

    public void setStoreType(int storeType) {
        this.storeType = storeType;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.messageRowId);
        dest.writeInt(this.storeType);
    }

    public void readFromParcel(Parcel source) {
        this.messageRowId = source.readLong();
        this.storeType = source.readInt();
    }

    public String toString() {
        List<String> list = new ArrayList();
        list.add("messageRowId=" + this.messageRowId);
        list.add("storeType=" + this.storeType);
        return list.toString();
    }
}
