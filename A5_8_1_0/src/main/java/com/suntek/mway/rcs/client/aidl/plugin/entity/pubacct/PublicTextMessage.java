package com.suntek.mway.rcs.client.aidl.plugin.entity.pubacct;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class PublicTextMessage extends PublicMessage implements Parcelable {
    public static final Creator<PublicTextMessage> CREATOR = new Creator<PublicTextMessage>() {
        public PublicTextMessage createFromParcel(Parcel source) {
            return new PublicTextMessage(source);
        }

        public PublicTextMessage[] newArray(int size) {
            return new PublicTextMessage[size];
        }
    };
    private String content;

    public PublicTextMessage(Parcel source) {
        readFromParcel(source);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.createtime);
        dest.writeInt(this.forwardable);
        dest.writeString(this.msgtype);
        dest.writeString(this.content);
        dest.writeInt(this.activeStatus);
        dest.writeString(this.paUuid);
    }

    public void readFromParcel(Parcel source) {
        this.createtime = source.readString();
        this.forwardable = source.readInt();
        this.msgtype = source.readString();
        this.content = source.readString();
        this.activeStatus = source.readInt();
        this.paUuid = source.readString();
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
