package com.suntek.mway.rcs.client.aidl.plugin.entity.pubacct;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class MediaArticle implements Parcelable {
    public static final Creator<MediaArticle> CREATOR = new Creator<MediaArticle>() {
        public MediaArticle createFromParcel(Parcel source) {
            return new MediaArticle(source);
        }

        public MediaArticle[] newArray(int size) {
            return new MediaArticle[size];
        }
    };
    private String author;
    private String bodyLink;
    private String digest;
    private String mainText;
    private String mediaUuid;
    private String originalLink;
    private String sourceLink;
    private String thumbLink;
    private String title;

    public MediaArticle(Parcel source) {
        readFromParcel(source);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.author);
        dest.writeString(this.thumbLink);
        dest.writeString(this.originalLink);
        dest.writeString(this.sourceLink);
        dest.writeString(this.mainText);
        dest.writeString(this.mediaUuid);
        dest.writeString(this.digest);
        dest.writeString(this.bodyLink);
    }

    public void readFromParcel(Parcel source) {
        this.title = source.readString();
        this.author = source.readString();
        this.thumbLink = source.readString();
        this.originalLink = source.readString();
        this.sourceLink = source.readString();
        this.mainText = source.readString();
        this.mediaUuid = source.readString();
        this.digest = source.readString();
        this.bodyLink = source.readString();
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return this.author;
    }

    public void setAuthor(String author) {
        this.author = author;
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

    public String getSourceLink() {
        return this.sourceLink;
    }

    public void setSourceLink(String sourceLink) {
        this.sourceLink = sourceLink;
    }

    public String getMainText() {
        return this.mainText;
    }

    public void setMainText(String mainText) {
        this.mainText = mainText;
    }

    public String getMediaUuid() {
        return this.mediaUuid;
    }

    public void setMediaUuid(String mediaUuid) {
        this.mediaUuid = mediaUuid;
    }

    public String getDigest() {
        return this.digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String getBodyLink() {
        return this.bodyLink;
    }

    public void setBodyLink(String bodyLink) {
        this.bodyLink = bodyLink;
    }

    public String toString() {
        StringBuffer sbuffer = new StringBuffer();
        if (this.title != null) {
            sbuffer.append(",title=").append(this.title);
        }
        if (this.thumbLink != null) {
            sbuffer.append(",thumbLink=").append(this.thumbLink);
        }
        if (this.originalLink != null) {
            sbuffer.append(",originalLink=").append(this.originalLink);
        }
        if (this.sourceLink != null) {
            sbuffer.append(",sourceLink=").append(this.sourceLink);
        }
        if (this.mainText != null) {
            sbuffer.append(",mainText=").append(this.mainText);
        }
        if (this.mediaUuid != null) {
            sbuffer.append(",mediaUuid=").append(this.mediaUuid);
        }
        if (this.digest != null) {
            sbuffer.append(",digest=").append(this.digest);
        }
        if (this.bodyLink != null) {
            sbuffer.append(",bodyLink=").append(this.bodyLink);
        }
        if (sbuffer.length() > 1) {
            return sbuffer.substring(1).toString();
        }
        return "";
    }
}
