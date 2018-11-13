package com.suntek.mway.rcs.client.aidl.plugin.entity.emoticon;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ResultBO implements Parcelable {
    public static final Creator<ResultBO> CREATOR = new Creator<ResultBO>() {
        public ResultBO createFromParcel(Parcel source) {
            return new ResultBO(source);
        }

        public ResultBO[] newArray(int size) {
            return new ResultBO[size];
        }
    };
    private boolean resultFlag;
    private String resultMsg;
    private Object resultObj;

    public ResultBO(Parcel source) {
        readFromParcel(source);
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBooleanArray(new boolean[]{this.resultFlag});
        dest.writeString(this.resultMsg);
        dest.writeValue(this.resultObj);
    }

    public void readFromParcel(Parcel source) {
        boolean[] val = new boolean[1];
        source.readBooleanArray(val);
        this.resultFlag = val[0];
        this.resultMsg = source.readString();
        this.resultObj = source.readValue(getClass().getClassLoader());
    }

    public int describeContents() {
        return 0;
    }

    public boolean isResultFlag() {
        return this.resultFlag;
    }

    public void setResultFlag(boolean resultFlag) {
        this.resultFlag = resultFlag;
    }

    public String getResultMsg() {
        return this.resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public Object getResultObj() {
        return this.resultObj;
    }

    public void setResultObj(Object resultObj) {
        this.resultObj = resultObj;
    }
}
