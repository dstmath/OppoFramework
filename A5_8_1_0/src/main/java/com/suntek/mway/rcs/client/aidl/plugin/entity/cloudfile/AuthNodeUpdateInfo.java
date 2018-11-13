package com.suntek.mway.rcs.client.aidl.plugin.entity.cloudfile;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class AuthNodeUpdateInfo implements Parcelable {
    public static final Creator<AuthNodeUpdateInfo> CREATOR = new Creator<AuthNodeUpdateInfo>() {
        public AuthNodeUpdateInfo createFromParcel(Parcel source) {
            return new AuthNodeUpdateInfo(source);
        }

        public AuthNodeUpdateInfo[] newArray(int size) {
            return new AuthNodeUpdateInfo[size];
        }
    };
    private String description;
    private String forceupdate;
    private String md5;
    private String name;
    private String size;
    private String updateMode;
    private String url;
    private String version;

    public AuthNodeUpdateInfo(Parcel source) {
        readFromParcel(source);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.version);
        dest.writeString(this.description);
        dest.writeString(this.url);
        dest.writeString(this.forceupdate);
        dest.writeString(this.md5);
        dest.writeString(this.size);
        dest.writeString(this.updateMode);
    }

    public void readFromParcel(Parcel source) {
        this.name = source.readString();
        this.version = source.readString();
        this.description = source.readString();
        this.url = source.readString();
        this.forceupdate = source.readString();
        this.md5 = source.readString();
        this.size = source.readString();
        this.updateMode = source.readString();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getForceupdate() {
        return this.forceupdate;
    }

    public void setForceupdate(String forceupdate) {
        this.forceupdate = forceupdate;
    }

    public String getMd5() {
        return this.md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getSize() {
        return this.size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getUpdateMode() {
        return this.updateMode;
    }

    public void setUpdateMode(String updateMode) {
        this.updateMode = updateMode;
    }
}
