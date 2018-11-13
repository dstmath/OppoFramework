package com.suntek.mway.rcs.client.aidl.plugin.entity.pubacct;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class PublicAccountsDetail implements Parcelable {
    public static final Creator<PublicAccountsDetail> CREATOR = new Creator<PublicAccountsDetail>() {
        public PublicAccountsDetail createFromParcel(Parcel source) {
            return new PublicAccountsDetail(source);
        }

        public PublicAccountsDetail[] newArray(int size) {
            return new PublicAccountsDetail[size];
        }
    };
    private int acceptstatus;
    private int activeStatus;
    private String addr;
    private String company;
    private String email;
    private String field;
    private String intro;
    private String logoType;
    private String logoUrl;
    private String menuString;
    private String menuTimestamp;
    private int menuType;
    private String name;
    private String number;
    private String paUuid;
    private String qrCode;
    private int recommendLevel;
    private String sipUri;
    private int subscribeStatus;
    private String tel;
    private String type;
    private String updateTime;
    private String zip;

    public PublicAccountsDetail(Parcel source) {
        readFromParcel(source);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.paUuid);
        dest.writeString(this.name);
        dest.writeString(this.company);
        dest.writeString(this.intro);
        dest.writeString(this.type);
        dest.writeInt(this.recommendLevel);
        dest.writeString(this.updateTime);
        dest.writeInt(this.menuType);
        dest.writeString(this.menuTimestamp);
        dest.writeInt(this.subscribeStatus);
        dest.writeInt(this.acceptstatus);
        dest.writeInt(this.activeStatus);
        dest.writeString(this.tel);
        dest.writeString(this.email);
        dest.writeString(this.zip);
        dest.writeString(this.addr);
        dest.writeString(this.field);
        dest.writeString(this.qrCode);
        dest.writeString(this.logoUrl);
        dest.writeString(this.sipUri);
        dest.writeString(this.number);
        dest.writeString(this.logoType);
        dest.writeString(this.menuString);
    }

    public void readFromParcel(Parcel source) {
        this.paUuid = source.readString();
        this.name = source.readString();
        this.company = source.readString();
        this.intro = source.readString();
        this.type = source.readString();
        this.recommendLevel = source.readInt();
        this.updateTime = source.readString();
        this.menuType = source.readInt();
        this.menuTimestamp = source.readString();
        this.subscribeStatus = source.readInt();
        this.acceptstatus = source.readInt();
        this.activeStatus = source.readInt();
        this.tel = source.readString();
        this.email = source.readString();
        this.zip = source.readString();
        this.addr = source.readString();
        this.field = source.readString();
        this.qrCode = source.readString();
        this.logoUrl = source.readString();
        this.sipUri = source.readString();
        this.number = source.readString();
        this.logoType = source.readString();
        this.menuString = source.readString();
    }

    public String getPaUuid() {
        return this.paUuid;
    }

    public void setPaUuid(String paUuid) {
        this.paUuid = paUuid;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompany() {
        return this.company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getIntro() {
        return this.intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getRecommendLevel() {
        return this.recommendLevel;
    }

    public void setRecommendLevel(int recommendLevel) {
        this.recommendLevel = recommendLevel;
    }

    public String getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public int getMenuType() {
        return this.menuType;
    }

    public void setMenuType(int menuType) {
        this.menuType = menuType;
    }

    public String getMenuTimestamp() {
        return this.menuTimestamp;
    }

    public void setMenuTimestamp(String menuTimestamp) {
        this.menuTimestamp = menuTimestamp;
    }

    public int getSubscribeStatus() {
        return this.subscribeStatus;
    }

    public void setSubscribeStatus(int subscribeStatus) {
        this.subscribeStatus = subscribeStatus;
    }

    public int getAcceptstatus() {
        return this.acceptstatus;
    }

    public void setAcceptstatus(int acceptstatus) {
        this.acceptstatus = acceptstatus;
    }

    public int getActiveStatus() {
        return this.activeStatus;
    }

    public void setActiveStatus(int activeStatus) {
        this.activeStatus = activeStatus;
    }

    public String getTel() {
        return this.tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getZip() {
        return this.zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getAddr() {
        return this.addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getField() {
        return this.field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getQrCode() {
        return this.qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getLogoUrl() {
        return this.logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getSipUri() {
        return this.sipUri;
    }

    public void setSipUri(String sipUri) {
        this.sipUri = sipUri;
    }

    public String getLogoType() {
        return this.logoType;
    }

    public void setLogoType(String logoType) {
        this.logoType = logoType;
    }

    public String getMenuString() {
        return this.menuString;
    }

    public void setMenuString(String menuString) {
        this.menuString = menuString;
    }

    public String toString() {
        StringBuffer sbuffer = new StringBuffer();
        sbuffer.append("paUuid=").append(this.paUuid).append(",name=").append(this.name).append(",number=").append(this.number).append(",logoUrl=").append(this.logoUrl).append(",recommendLevel=").append(this.recommendLevel).append(",sipUri=").append(this.sipUri).append(",company=").append(this.company).append(",intro=").append(this.intro).append(",type=").append(this.type).append(",updateTime=").append(this.updateTime).append(",menuType=").append(this.menuType).append(",menuTimestamp=").append(this.menuTimestamp).append(",subscribeStatus=").append(this.subscribeStatus).append(",acceptstatus=").append(this.acceptstatus).append(",activeStatus=").append(this.activeStatus).append(",tel=").append(this.tel).append(",email=").append(this.email).append(",zip=").append(this.zip).append(",addr=").append(this.addr).append(",field=").append(this.field).append(",qrCode=").append(this.qrCode).append(",logoType=").append(this.logoType).append(",menuString=").append(this.menuString);
        return sbuffer.toString();
    }

    public String getNumber() {
        return this.number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
