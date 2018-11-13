package com.suntek.mway.rcs.client.aidl.plugin.entity.emoticon;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class EmojiPackageBO implements Parcelable {
    public static final Creator<EmojiPackageBO> CREATOR = new Creator<EmojiPackageBO>() {
        public EmojiPackageBO createFromParcel(Parcel source) {
            return new EmojiPackageBO(source);
        }

        public EmojiPackageBO[] newArray(int size) {
            return new EmojiPackageBO[size];
        }
    };
    private String packageCpId;
    private String packageCpName;
    private String packageDesc;
    private String packageIcon;
    private String packageId;
    private String packageName;
    private String packagePrice;
    private String packageSize;
    private String packageState;
    private String packageUseTime;
    private String packageZipIcon;
    private String packageZipName;
    private String packageZipPath;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.packageId);
        dest.writeString(this.packageName);
        dest.writeString(this.packageIcon);
        dest.writeString(this.packageSize);
        dest.writeString(this.packageState);
        dest.writeString(this.packagePrice);
        dest.writeString(this.packageUseTime);
        dest.writeString(this.packageCpId);
        dest.writeString(this.packageCpName);
        dest.writeString(this.packageDesc);
        dest.writeString(this.packageZipIcon);
        dest.writeString(this.packageZipName);
        dest.writeString(this.packageZipPath);
    }

    public void readFromParcel(Parcel source) {
        this.packageId = source.readString();
        this.packageName = source.readString();
        this.packageIcon = source.readString();
        this.packageSize = source.readString();
        this.packageState = source.readString();
        this.packagePrice = source.readString();
        this.packageUseTime = source.readString();
        this.packageCpId = source.readString();
        this.packageCpName = source.readString();
        this.packageDesc = source.readString();
        this.packageZipIcon = source.readString();
        this.packageZipName = source.readString();
        this.packageZipPath = source.readString();
    }

    public EmojiPackageBO(Parcel source) {
        readFromParcel(source);
    }

    public String getPackageId() {
        return this.packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageIcon() {
        return this.packageIcon;
    }

    public void setPackageIcon(String packageIcon) {
        this.packageIcon = packageIcon;
    }

    public String getPackageSize() {
        return this.packageSize;
    }

    public void setPackageSize(String packageSize) {
        this.packageSize = packageSize;
    }

    public String getPackageState() {
        return this.packageState;
    }

    public void setPackageState(String packageState) {
        this.packageState = packageState;
    }

    public String getPackagePrice() {
        return this.packagePrice;
    }

    public void setPackagePrice(String packagePrice) {
        this.packagePrice = packagePrice;
    }

    public String getPackageUseTime() {
        return this.packageUseTime;
    }

    public void setPackageUseTime(String packageUseTime) {
        this.packageUseTime = packageUseTime;
    }

    public String getPackageCpId() {
        return this.packageCpId;
    }

    public void setPackageCpId(String packageCpId) {
        this.packageCpId = packageCpId;
    }

    public String getPackageCpName() {
        return this.packageCpName;
    }

    public void setPackageCpName(String packageCpName) {
        this.packageCpName = packageCpName;
    }

    public String getPackageDesc() {
        return this.packageDesc;
    }

    public void setPackageDesc(String packageDesc) {
        this.packageDesc = packageDesc;
    }

    public String getPackageZipIcon() {
        return this.packageZipIcon;
    }

    public void setPackageZipIcon(String packageZipIcon) {
        this.packageZipIcon = packageZipIcon;
    }

    public String getPackageZipName() {
        return this.packageZipName;
    }

    public void setPackageZipName(String packageZipName) {
        this.packageZipName = packageZipName;
    }

    public String getPackageZipPath() {
        return this.packageZipPath;
    }

    public void setPackageZipPath(String packageZipPath) {
        this.packageZipPath = packageZipPath;
    }
}
