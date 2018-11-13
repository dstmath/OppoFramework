package com.suntek.mway.rcs.client.aidl.plugin.entity.profile;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public class ProfileName implements Parcelable {
    public static final Creator<ProfileName> CREATOR = new Creator<ProfileName>() {
        public ProfileName createFromParcel(Parcel source) {
            return new ProfileName(source);
        }

        public ProfileName[] newArray(int size) {
            return new ProfileName[size];
        }
    };
    private String displayName = null;
    private String familyName = null;
    private String firstName = null;
    private String givenName = null;
    private String middleName = null;
    private String namePrefix = null;
    private String nameSuffix = null;
    private String nickName = null;

    public ProfileName(Parcel source) {
        readFromParcel(source);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.displayName);
        dest.writeString(this.nickName);
        dest.writeString(this.familyName);
        dest.writeString(this.firstName);
        dest.writeString(this.givenName);
        dest.writeString(this.middleName);
        dest.writeString(this.nameSuffix);
        dest.writeString(this.namePrefix);
    }

    public void readFromParcel(Parcel source) {
        this.displayName = source.readString();
        this.nickName = source.readString();
        this.familyName = source.readString();
        this.firstName = source.readString();
        this.givenName = source.readString();
        this.middleName = source.readString();
        this.nameSuffix = source.readString();
        this.namePrefix = source.readString();
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getNickName() {
        return this.nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getFamilyName() {
        return this.familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getGivenName() {
        return this.givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getMiddleName() {
        return this.middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getNameSuffix() {
        return this.nameSuffix;
    }

    public void setNameSuffix(String nameSuffix) {
        this.nameSuffix = nameSuffix;
    }

    public String getNamePrefix() {
        return this.namePrefix;
    }

    public void setNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    public String toString() {
        List<String> list = new ArrayList();
        list.add("displayName=" + this.displayName);
        list.add("nickName=" + this.nickName);
        list.add("familyName=" + this.familyName);
        list.add("firstName=" + this.firstName);
        list.add("givenName=" + this.givenName);
        list.add("middleName=" + this.middleName);
        list.add("nameSuffix=" + this.nameSuffix);
        list.add("namePrefix=" + this.namePrefix);
        return list.toString();
    }
}
