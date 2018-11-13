package com.suntek.mway.rcs.client.aidl.plugin.entity.richscrn;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class RichScrnShowing implements Parcelable {
    public static final Creator<RichScrnShowing> CREATOR = new Creator<RichScrnShowing>() {
        public RichScrnShowing createFromParcel(Parcel source) {
            return new RichScrnShowing(source);
        }

        public RichScrnShowing[] newArray(int size) {
            return new RichScrnShowing[size];
        }
    };
    private String cid;
    private String greeting;
    private String localSourceUrl;
    private String missdn;
    private String missdnAddress;
    private String sourceType;

    public RichScrnShowing(Parcel source) {
        readFromParcel(source);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.missdn);
        dest.writeString(this.cid);
        dest.writeString(this.greeting);
        dest.writeString(this.sourceType);
        dest.writeString(this.missdnAddress);
        dest.writeString(this.localSourceUrl);
    }

    public void readFromParcel(Parcel source) {
        this.missdn = source.readString();
        this.cid = source.readString();
        this.greeting = source.readString();
        this.sourceType = source.readString();
        this.missdnAddress = source.readString();
        this.localSourceUrl = source.readString();
    }

    public String getMissdn() {
        return this.missdn;
    }

    public void setMissdn(String missdn) {
        this.missdn = missdn;
    }

    public String getCid() {
        return this.cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getGreeting() {
        return this.greeting;
    }

    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }

    public String getSourceType() {
        return this.sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getMissdnAddress() {
        return this.missdnAddress;
    }

    public void setMissdnAddress(String missdnAddress) {
        this.missdnAddress = missdnAddress;
    }

    public String getLocalSourceUrl() {
        return this.localSourceUrl;
    }

    public void setLocalSourceUrl(String localSourceUrl) {
        this.localSourceUrl = localSourceUrl;
    }
}
