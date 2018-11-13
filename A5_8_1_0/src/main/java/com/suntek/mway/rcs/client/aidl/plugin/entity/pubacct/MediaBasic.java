package com.suntek.mway.rcs.client.aidl.plugin.entity.pubacct;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class MediaBasic implements Parcelable {
    public static final Creator<MediaBasic> CREATOR = new Creator<MediaBasic>() {
        public MediaBasic createFromParcel(Parcel source) {
            return new MediaBasic(source);
        }

        public MediaBasic[] newArray(int size) {
            return new MediaBasic[size];
        }
    };
    private String createTime;
    private String duration;
    private String fileSize;
    private String fileType;
    private String mediaUuid;
    private String originalLink;
    private String paUuid;
    private String thumbLink;
    private String title;

    public MediaBasic(Parcel source) {
        readFromParcel(source);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.thumbLink);
        dest.writeString(this.originalLink);
        dest.writeString(this.title);
        dest.writeString(this.fileSize);
        dest.writeString(this.duration);
        dest.writeString(this.fileType);
        dest.writeString(this.createTime);
        dest.writeString(this.mediaUuid);
        dest.writeString(this.paUuid);
    }

    public void readFromParcel(Parcel source) {
        this.thumbLink = source.readString();
        this.originalLink = source.readString();
        this.title = source.readString();
        this.fileSize = source.readString();
        this.duration = source.readString();
        this.fileType = source.readString();
        this.createTime = source.readString();
        this.mediaUuid = source.readString();
        this.paUuid = source.readString();
    }

    public String getThumbLink() {
        return this.thumbLink;
    }

    public void setThumbLink(String thumbLink) {
        this.thumbLink = thumbLink;
    }

    public String getOriginalLink() {
        return this.originalLink;
    }

    public void setOriginalLink(String originalLink) {
        this.originalLink = originalLink;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFileSize() {
        return this.fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getDuration() {
        return this.duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getFileType() {
        return this.fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getMediaUuid() {
        return this.mediaUuid;
    }

    public void setMediaUuid(String mediaUuid) {
        this.mediaUuid = mediaUuid;
    }

    public String getPaUuid() {
        return this.paUuid;
    }

    public void setPaUuid(String paUuid) {
        this.paUuid = paUuid;
    }

    public String toString() {
        StringBuffer sbuffer = new StringBuffer();
        if (this.thumbLink != null) {
            sbuffer.append(",thumbLink=").append(this.thumbLink);
        }
        if (this.originalLink != null) {
            sbuffer.append(",originalLink=").append(this.originalLink);
        }
        if (this.title != null) {
            sbuffer.append(",title=").append(this.title);
        }
        if (this.fileSize != null) {
            sbuffer.append(",fileSize=").append(this.fileSize);
        }
        if (this.duration != null) {
            sbuffer.append(",duration=").append(this.duration);
        }
        if (this.fileType != null) {
            sbuffer.append(",fileType=").append(this.fileType);
        }
        if (this.createTime != null) {
            sbuffer.append(",createTime=").append(this.createTime);
        }
        if (this.mediaUuid != null) {
            sbuffer.append(",mediaUuid=").append(this.mediaUuid);
        }
        if (this.paUuid != null) {
            sbuffer.append(",paUuid=").append(this.paUuid);
        }
        if (sbuffer.length() > 1) {
            return sbuffer.substring(1).toString();
        }
        return "";
    }
}
