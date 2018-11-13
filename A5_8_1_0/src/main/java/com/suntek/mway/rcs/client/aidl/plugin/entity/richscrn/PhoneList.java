package com.suntek.mway.rcs.client.aidl.plugin.entity.richscrn;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;

public class PhoneList implements Parcelable {
    public static final Creator<PhoneList> CREATOR = new Creator<PhoneList>() {
        public PhoneList createFromParcel(Parcel source) {
            return new PhoneList(source);
        }

        public PhoneList[] newArray(int size) {
            return new PhoneList[size];
        }
    };
    private ArrayList<String> phoneList;

    public ArrayList<String> getPhoneList() {
        return this.phoneList;
    }

    public void setPhoneList(ArrayList<String> phoneList) {
        this.phoneList = phoneList;
    }

    public PhoneList(Parcel source) {
        readFromParcel(source);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(this.phoneList);
    }

    public void readFromParcel(Parcel source) {
        this.phoneList = source.createStringArrayList();
    }
}
