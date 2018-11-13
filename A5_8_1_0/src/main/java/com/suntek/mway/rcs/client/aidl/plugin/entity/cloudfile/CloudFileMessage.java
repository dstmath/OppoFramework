package com.suntek.mway.rcs.client.aidl.plugin.entity.cloudfile;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class CloudFileMessage implements Parcelable {
    public static final Creator<CloudFileMessage> CREATOR = new Creator<CloudFileMessage>() {
        public CloudFileMessage createFromParcel(Parcel source) {
            return new CloudFileMessage(source);
        }

        public CloudFileMessage[] newArray(int size) {
            return new CloudFileMessage[size];
        }
    };
    private String fileName;
    private long fileSize;
    private String shareUrl;

    public CloudFileMessage(String fileName, long fileSize, String shareUrl) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.shareUrl = shareUrl;
    }

    public CloudFileMessage(Parcel source) {
        readFromParcel(source);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.fileName);
        dest.writeLong(this.fileSize);
        dest.writeString(this.shareUrl);
    }

    public void readFromParcel(Parcel source) {
        this.fileName = source.readString();
        this.fileSize = source.readLong();
        this.shareUrl = source.readString();
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return this.fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getShareUrl() {
        return this.shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }
}
