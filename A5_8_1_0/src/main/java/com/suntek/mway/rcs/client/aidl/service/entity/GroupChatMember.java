package com.suntek.mway.rcs.client.aidl.service.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class GroupChatMember implements Parcelable {
    public static final String ALLOWED_TO_BE_CHAIRMAN = "gpmanage";
    public static final int CHAIRMAN = 1;
    public static final Creator<GroupChatMember> CREATOR = new Creator<GroupChatMember>() {
        public GroupChatMember createFromParcel(Parcel in) {
            return new GroupChatMember(in);
        }

        public GroupChatMember[] newArray(int size) {
            return new GroupChatMember[size];
        }
    };
    public static final int MEMBER = 0;
    private String alias;
    private long date;
    private String etag;
    private String etype;
    private long groupId;
    private long id;
    private String imageCode;
    private String imageType;
    private String number;
    private int role = -1;

    public GroupChatMember(Parcel source) {
        readFromParcel(source);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.groupId);
        dest.writeString(this.number);
        dest.writeString(this.alias);
        dest.writeInt(this.role);
        dest.writeString(this.etype);
        dest.writeString(this.etag);
        dest.writeString(this.imageType);
        dest.writeString(this.imageCode);
        dest.writeLong(this.date);
    }

    public void readFromParcel(Parcel source) {
        this.id = source.readLong();
        this.groupId = source.readLong();
        this.number = source.readString();
        this.alias = source.readString();
        this.role = source.readInt();
        this.etype = source.readString();
        this.etag = source.readString();
        this.imageType = source.readString();
        this.imageCode = source.readString();
        this.date = source.readLong();
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getGroupId() {
        return this.groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public String getNumber() {
        return this.number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getAlias() {
        return this.alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public int getRole() {
        return this.role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getEtype() {
        return this.etype;
    }

    public void setEtype(String etype) {
        this.etype = etype;
    }

    public String getEtag() {
        return this.etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getImageType() {
        return this.imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    public String getImageCode() {
        return this.imageCode;
    }

    public void setImageCode(String imageCode) {
        this.imageCode = imageCode;
    }

    public long getDate() {
        return this.date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
