package com.suntek.mway.rcs.client.aidl.plugin.entity.cloudfile;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Map;

public class ConfNode implements Parcelable {
    public static final Creator<ConfNode> CREATOR = new Creator<ConfNode>() {
        public ConfNode createFromParcel(Parcel source) {
            return new ConfNode(source);
        }

        public ConfNode[] newArray(int size) {
            return new ConfNode[size];
        }
    };
    private Map<String, String> fields;
    private String version;

    public ConfNode(Parcel source) {
        readFromParcel(source);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.version);
        dest.writeMap(this.fields);
    }

    public void readFromParcel(Parcel source) {
        this.version = source.readString();
        this.fields = source.readHashMap(getClass().getClassLoader());
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getFields() {
        return this.fields;
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }
}
