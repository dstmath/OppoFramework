package com.suntek.mway.rcs.client.aidl.plugin.entity.profile;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class QRCardInfo extends BaseModel implements Parcelable, Serializable {
    public static final Creator<QRCardInfo> CREATOR = new Creator<QRCardInfo>() {
        public QRCardInfo createFromParcel(Parcel source) {
            return new QRCardInfo(source);
        }

        public QRCardInfo[] newArray(int size) {
            return new QRCardInfo[size];
        }
    };
    private static final long serialVersionUID = 5140344570754052168L;
    private String companyDuty;
    private String companyEmail;
    private String companyFax;
    private String companyName;
    private String companyTel;
    private String name;
    private String tel;

    public QRCardInfo(Parcel source) {
        readFromParcel(source);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.name);
        dest.writeString(this.tel);
        dest.writeString(this.companyTel);
        dest.writeString(this.companyFax);
        dest.writeString(this.companyName);
        dest.writeString(this.companyDuty);
        dest.writeString(this.companyEmail);
    }

    public void readFromParcel(Parcel source) {
        super.readFromParcel(source);
        this.name = source.readString();
        this.tel = source.readString();
        this.companyTel = source.readString();
        this.companyFax = source.readString();
        this.companyName = source.readString();
        this.companyDuty = source.readString();
        this.companyEmail = source.readString();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTel() {
        return this.tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getCompanyTel() {
        return this.companyTel;
    }

    public void setCompanyTel(String companyTel) {
        this.companyTel = companyTel;
    }

    public String getCompanyFax() {
        return this.companyFax;
    }

    public void setCompanyFax(String companyFax) {
        this.companyFax = companyFax;
    }

    public String getCompanyName() {
        return this.companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyDuty() {
        return this.companyDuty;
    }

    public void setCompanyDuty(String companyDuty) {
        this.companyDuty = companyDuty;
    }

    public String getCompanyEmail() {
        return this.companyEmail;
    }

    public void setCompanyEmail(String companyEmail) {
        this.companyEmail = companyEmail;
    }

    public String toString() {
        List<String> list = new ArrayList();
        list.add("name=" + this.name);
        list.add("tel=" + this.tel);
        list.add("companyTel=" + this.companyTel);
        list.add("companyFax=" + this.companyFax);
        list.add("companyName=" + this.companyName);
        list.add("companyDuty=" + this.companyDuty);
        list.add("companyEmail=" + this.companyEmail);
        list.add("account=" + getAccount());
        list.add("etag=" + getEtag());
        return list.toString();
    }
}
