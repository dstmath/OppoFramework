package com.suntek.mway.rcs.client.aidl.plugin.entity.richscrn;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ResultInfo implements Parcelable {
    public static final Creator<ResultInfo> CREATOR = new Creator<ResultInfo>() {
        public ResultInfo createFromParcel(Parcel source) {
            return new ResultInfo(source);
        }

        public ResultInfo[] newArray(int size) {
            return new ResultInfo[size];
        }
    };
    private boolean isSuccess;
    private String resultMsg;

    public ResultInfo(Parcel source) {
        readFromParcel(source);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBooleanArray(new boolean[]{this.isSuccess});
        dest.writeString(this.resultMsg);
    }

    public void readFromParcel(Parcel source) {
        boolean[] value = new boolean[1];
        source.readBooleanArray(value);
        this.isSuccess = value[0];
        this.resultMsg = source.readString();
    }

    public boolean isSuccess() {
        return this.isSuccess;
    }

    public void setSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public String getResultMsg() {
        return this.resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }
}
