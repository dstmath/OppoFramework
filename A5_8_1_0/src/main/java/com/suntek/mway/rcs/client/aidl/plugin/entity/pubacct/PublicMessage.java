package com.suntek.mway.rcs.client.aidl.plugin.entity.pubacct;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class PublicMessage implements Parcelable {
    public static final String AUDIO = "40";
    public static final Creator<PublicMessage> CREATOR = new Creator<PublicMessage>() {
        public PublicMessage createFromParcel(Parcel in) {
            return new PublicMessage(in);
        }

        public PublicMessage[] newArray(int size) {
            return new PublicMessage[size];
        }
    };
    public static final String IMAGE = "20";
    public static final String LOCATION = "19";
    public static final String SYNC_DETAIL = "72";
    public static final String SYNC_SUBSCRIBE = "71";
    public static final String TEXT = "10";
    public static final String TOPIC = "50";
    public static final String TOPIC_MORE = "52";
    public static final String TOPIC_SINGLE = "51";
    public static final String VCARD = "18";
    public static final String VEDIO = "30";
    protected int activeStatus;
    protected String createtime;
    protected int forwardable;
    protected String msgtype;
    protected String paUuid;

    public PublicMessage(Parcel source) {
        readFromParcel(source);
    }

    public String getCreatetime() {
        return this.createtime;
    }

    public void setCreatetime(String createtime) {
        this.createtime = createtime;
    }

    public int getForwardable() {
        return this.forwardable;
    }

    public void setForwardable(int forwardable) {
        this.forwardable = forwardable;
    }

    public String getMsgtype() {
        return this.msgtype;
    }

    public void setMsgtype(String msgtype) {
        this.msgtype = msgtype;
    }

    public int getActiveStatus() {
        return this.activeStatus;
    }

    public void setActiveStatus(int activeStatus) {
        this.activeStatus = activeStatus;
    }

    public String getPaUuid() {
        return this.paUuid;
    }

    public void setPaUuid(String paUuid) {
        this.paUuid = paUuid;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
    }

    public void readFromParcel(Parcel source) {
    }
}
