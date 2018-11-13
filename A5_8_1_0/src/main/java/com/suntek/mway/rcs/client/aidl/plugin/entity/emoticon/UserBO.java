package com.suntek.mway.rcs.client.aidl.plugin.entity.emoticon;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class UserBO implements Parcelable {
    public static final Creator<UserBO> CREATOR = new Creator<UserBO>() {
        public UserBO createFromParcel(Parcel source) {
            return new UserBO(source);
        }

        public UserBO[] newArray(int size) {
            return new UserBO[size];
        }
    };
    private String userLoginTime;
    private String userNick;
    private String userPhone;
    private String userState;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userPhone);
        dest.writeString(this.userNick);
        dest.writeString(this.userLoginTime);
        dest.writeString(this.userState);
    }

    public void readFromParcel(Parcel source) {
        this.userPhone = source.readString();
        this.userNick = source.readString();
        this.userLoginTime = source.readString();
        this.userState = source.readString();
    }

    public UserBO(Parcel source) {
        readFromParcel(source);
    }

    public String getUserPhone() {
        return this.userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserNick() {
        return this.userNick;
    }

    public void setUserNick(String userNick) {
        this.userNick = userNick;
    }

    public String getUserLoginTime() {
        return this.userLoginTime;
    }

    public void setUserLoginTime(String userLoginTime) {
        this.userLoginTime = userLoginTime;
    }

    public String getUserState() {
        return this.userState;
    }

    public void setUserState(String userState) {
        this.userState = userState;
    }
}
