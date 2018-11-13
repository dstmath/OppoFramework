package com.suntek.mway.rcs.client.aidl.plugin.entity.cloudfile;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class Result implements Parcelable {
    public static final Creator<Result> CREATOR = new Creator<Result>() {
        public Result createFromParcel(Parcel source) {
            return new Result(source);
        }

        public Result[] newArray(int size) {
            return new Result[size];
        }
    };
    private String httpCode;
    private String mcloudDesc;
    private Error mcloudError;
    private String serverCode;
    private String socketCode;

    public Result(Parcel source) {
        readFromParcel(source);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mcloudError.ordinal());
        dest.writeString(this.mcloudDesc);
        dest.writeString(this.socketCode);
        dest.writeString(this.httpCode);
        dest.writeString(this.serverCode);
    }

    public void readFromParcel(Parcel source) {
        this.mcloudError = Error.valueOf(source.readInt());
        this.mcloudDesc = source.readString();
        this.socketCode = source.readString();
        this.httpCode = source.readString();
        this.serverCode = source.readString();
    }

    public Error getMcloudError() {
        return this.mcloudError;
    }

    public void setMcloudError(Error mcloudError) {
        this.mcloudError = mcloudError;
    }

    public String getMcloudDesc() {
        return this.mcloudDesc;
    }

    public void setMcloudDesc(String mcloudDesc) {
        this.mcloudDesc = mcloudDesc;
    }

    public String getSocketCode() {
        return this.socketCode;
    }

    public void setSocketCode(String socketCode) {
        this.socketCode = socketCode;
    }

    public String getHttpCode() {
        return this.httpCode;
    }

    public void setHttpCode(String httpCode) {
        this.httpCode = httpCode;
    }

    public String getServerCode() {
        return this.serverCode;
    }

    public void setServerCode(String serverCode) {
        this.serverCode = serverCode;
    }
}
