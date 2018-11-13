package com.suntek.mway.rcs.client.aidl.plugin.entity.profile;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BaseModel implements Parcelable, Serializable {
    public static final Creator<BaseModel> CREATOR = new Creator<BaseModel>() {
        public BaseModel createFromParcel(Parcel source) {
            return new BaseModel(source);
        }

        public BaseModel[] newArray(int size) {
            return new BaseModel[size];
        }
    };
    private static final long serialVersionUID = -1599075631882399753L;
    private String account;
    private String etag;

    public BaseModel(Parcel source) {
        readFromParcel(source);
    }

    public void writeToParcel(Parcel dest, int arg1) {
        dest.writeString(this.account);
        dest.writeString(this.etag);
    }

    public void readFromParcel(Parcel source) {
        this.account = source.readString();
        this.etag = source.readString();
    }

    public String getAccount() {
        return this.account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getEtag() {
        return this.etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String toString() {
        List<String> list = new ArrayList();
        list.add("account=" + this.account);
        list.add("etag=" + this.etag);
        return list.toString();
    }

    public int describeContents() {
        return 0;
    }
}
