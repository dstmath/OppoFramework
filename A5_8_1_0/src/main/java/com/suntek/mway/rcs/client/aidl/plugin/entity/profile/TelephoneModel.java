package com.suntek.mway.rcs.client.aidl.plugin.entity.profile;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TelephoneModel implements Parcelable, Serializable {
    public static final Creator<TelephoneModel> CREATOR = new Creator<TelephoneModel>() {
        public TelephoneModel createFromParcel(Parcel source) {
            return new TelephoneModel(source);
        }

        public TelephoneModel[] newArray(int size) {
            return new TelephoneModel[size];
        }
    };
    public static int TYPE_FIXED = 3;
    public static int TYPE_HOME = 1;
    public static int TYPE_MOBILE = 2;
    public static int TYPE_OTHER = 5;
    public static int TYPE_WORK = 4;
    private static final long serialVersionUID = 2509804884372655920L;
    private String telephone;
    private int type;

    public TelephoneModel(Parcel source) {
        this.type = source.readInt();
        this.telephone = source.readString();
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTelephone() {
        return this.telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int arg1) {
        dest.writeInt(this.type);
        dest.writeString(this.telephone);
    }

    public String toString() {
        List<String> list = new ArrayList();
        list.add("type=" + this.type);
        list.add("telephone=" + this.telephone);
        return list.toString();
    }
}
