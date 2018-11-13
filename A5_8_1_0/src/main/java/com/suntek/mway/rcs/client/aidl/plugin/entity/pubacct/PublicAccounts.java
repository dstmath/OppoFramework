package com.suntek.mway.rcs.client.aidl.plugin.entity.pubacct;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.io.UnsupportedEncodingException;

public class PublicAccounts implements Parcelable, Comparable<PublicAccounts> {
    public static final Creator<PublicAccounts> CREATOR = new Creator<PublicAccounts>() {
        public PublicAccounts createFromParcel(Parcel source) {
            return new PublicAccounts(source);
        }

        public PublicAccounts[] newArray(int size) {
            return new PublicAccounts[size];
        }
    };
    private String logo;
    private String name;
    private String paUuid;
    private int recommendLevel;
    private String sipUri;
    private int subscribestatus;

    public PublicAccounts(Parcel source) {
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
        dest.writeString(this.sipUri);
        dest.writeInt(this.subscribestatus);
    }

    public void readFromParcel(Parcel source) {
        this.paUuid = source.readString();
        this.name = source.readString();
        this.recommendLevel = source.readInt();
        this.logo = source.readString();
        this.sipUri = source.readString();
        this.subscribestatus = source.readInt();
    }

    public String getLogo() {
        return this.logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPaUuid() {
        return this.paUuid;
    }

    public void setPaUuid(String paUuid) {
        this.paUuid = paUuid;
    }

    public int getRecommendLevel() {
        return this.recommendLevel;
    }

    public void setRecommendLevel(int recommendLevel) {
        this.recommendLevel = recommendLevel;
    }

    public String getSipUri() {
        return this.sipUri;
    }

    public void setSipUri(String sipUri) {
        this.sipUri = sipUri;
    }

    public int getSubscribestatus() {
        return this.subscribestatus;
    }

    public void setSubscribestatus(int subscribestatus) {
        this.subscribestatus = subscribestatus;
    }

    public String toString() {
        StringBuffer sbuffer = new StringBuffer();
        sbuffer.append("paUuid=").append(this.paUuid).append(",name=").append(this.name).append(",logo=").append(this.logo).append(",recommendLevel=").append(this.recommendLevel).append(",sipUri=").append(this.sipUri);
        return sbuffer.toString();
    }

    public int compareTo(PublicAccounts account) {
        int lenA = this.name.length();
        int lenB = account.getName().length();
        int lenComp = lenA >= lenB ? lenB : lenA;
        for (int i = 0; i < lenComp; i++) {
            int result = getHexString(this.name.charAt(0)).compareTo(getHexString(account.getName().charAt(0)));
            if (result != 0) {
                return result;
            }
        }
        if (lenA > lenB) {
            return 1;
        }
        if (lenA == lenB) {
            return 0;
        }
        return -1;
    }

    public static String getHexString(char c) {
        byte[] b = null;
        StringBuffer sb = new StringBuffer();
        try {
            b = new String(new char[]{c}).getBytes("gb2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        for (byte b2 : b) {
            sb.append(Integer.toHexString(b2 & 255));
        }
        return sb.toString();
    }
}
