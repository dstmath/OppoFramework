package com.suntek.mway.rcs.client.aidl.plugin.entity.pubacct;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class PublicAccountReqEntity implements Parcelable {
    public static final Creator<PublicAccountReqEntity> CREATOR = new Creator<PublicAccountReqEntity>() {
        public PublicAccountReqEntity createFromParcel(Parcel source) {
            return new PublicAccountReqEntity(source);
        }

        public PublicAccountReqEntity[] newArray(int size) {
            return new PublicAccountReqEntity[size];
        }
    };
    private String logo;
    private String name;
    private String paUuid;
    private int recommendLevel;

    public PublicAccountReqEntity(Parcel source) {
        readFromParcel(source);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.paUuid);
        dest.writeString(this.name);
        dest.writeInt(this.recommendLevel);
        dest.writeString(this.logo);
    }

    public void readFromParcel(Parcel source) {
        this.paUuid = source.readString();
        this.name = source.readString();
        this.recommendLevel = source.readInt();
        this.logo = source.readString();
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

    public int getRecommendLevel() {
        return this.recommendLevel;
    }

    public void setRecommendLevel(int recommendLevel) {
        this.recommendLevel = recommendLevel;
    }

    public String getLogo() {
        return this.logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }
}
