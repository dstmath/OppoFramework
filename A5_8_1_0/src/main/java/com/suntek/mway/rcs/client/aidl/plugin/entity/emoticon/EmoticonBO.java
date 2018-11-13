package com.suntek.mway.rcs.client.aidl.plugin.entity.emoticon;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class EmoticonBO implements Parcelable {
    public static final Creator<EmoticonBO> CREATOR = new Creator<EmoticonBO>() {
        public EmoticonBO createFromParcel(Parcel source) {
            return new EmoticonBO(source);
        }

        public EmoticonBO[] newArray(int size) {
            return new EmoticonBO[size];
        }
    };
    private String emoticonDynamic;
    private byte[] emoticonDynamicByte;
    private String emoticonId;
    private String emoticonName;
    private String emoticonStatic;
    private byte[] emoticonStaticByte;
    private boolean isOnlyBrowse;
    private String packageId;
    private String userPhone;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.emoticonId);
        dest.writeString(this.emoticonName);
        dest.writeString(this.emoticonStatic);
        dest.writeString(this.emoticonDynamic);
        dest.writeString(this.packageId);
        dest.writeByteArray(this.emoticonStaticByte);
        dest.writeByteArray(this.emoticonDynamicByte);
        dest.writeString(this.userPhone);
        dest.writeBooleanArray(new boolean[]{this.isOnlyBrowse});
    }

    public void readFromParcel(Parcel source) {
        this.emoticonId = source.readString();
        this.emoticonName = source.readString();
        this.emoticonStatic = source.readString();
        this.emoticonDynamic = source.readString();
        this.packageId = source.readString();
        this.emoticonStaticByte = source.createByteArray();
        this.emoticonDynamicByte = source.createByteArray();
        this.userPhone = source.readString();
        boolean[] val = new boolean[1];
        source.readBooleanArray(val);
        this.isOnlyBrowse = val[0];
    }

    public EmoticonBO(Parcel source) {
        readFromParcel(source);
    }

    public String getEmoticonId() {
        return this.emoticonId;
    }

    public void setEmoticonId(String emoticonId) {
        this.emoticonId = emoticonId;
    }

    public String getEmoticonName() {
        return this.emoticonName;
    }

    public void setEmoticonName(String emoticonName) {
        this.emoticonName = emoticonName;
    }

    public String getEmoticonStatic() {
        return this.emoticonStatic;
    }

    public void setEmoticonStatic(String emoticonStatic) {
        this.emoticonStatic = emoticonStatic;
    }

    public String getEmoticonDynamic() {
        return this.emoticonDynamic;
    }

    public void setEmoticonDynamic(String emoticonDynamic) {
        this.emoticonDynamic = emoticonDynamic;
    }

    public String getPackageId() {
        return this.packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public byte[] getEmoticonStaticByte() {
        return this.emoticonStaticByte;
    }

    public void setEmoticonStaticByte(byte[] emoticonStaticByte) {
        this.emoticonStaticByte = emoticonStaticByte;
    }

    public byte[] getEmoticonDynamicByte() {
        return this.emoticonDynamicByte;
    }

    public void setEmoticonDynamicByte(byte[] emoticonDynamicByte) {
        this.emoticonDynamicByte = emoticonDynamicByte;
    }

    public String getUserPhone() {
        return this.userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public boolean isOnlyBrowse() {
        return this.isOnlyBrowse;
    }

    public void setOnlyBrowse(boolean isOnlyBrowse) {
        this.isOnlyBrowse = isOnlyBrowse;
    }
}
