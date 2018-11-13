package com.suntek.mway.rcs.client.aidl.plugin.entity.pubacct;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class PublicMediaMessage extends PublicMessage implements Parcelable {
    public static final Creator<PublicMediaMessage> CREATOR = new Creator<PublicMediaMessage>() {
        public PublicMediaMessage createFromParcel(Parcel source) {
            return new PublicMediaMessage(source);
        }

        public PublicMediaMessage[] newArray(int size) {
            return new PublicMediaMessage[size];
        }
    };
    private PublicMediaContent media;

    public static class PublicMediaContent implements Parcelable {
        public static final Creator<PublicMediaContent> CREATOR = new Creator<PublicMediaContent>() {
            public PublicMediaContent createFromParcel(Parcel source) {
                return new PublicMediaContent(source);
            }

            public PublicMediaContent[] newArray(int size) {
                return new PublicMediaContent[size];
            }
        };
        private String duration;
        private String fileSize;
        private String fileType;
        private String mediaUuid;
        private String originalLink;
        private String thumbLink;
        private String title;

        public PublicMediaContent(Parcel source) {
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
            dest.writeString(this.mediaUuid);
        }

        public void readFromParcel(Parcel source) {
            this.thumbLink = source.readString();
            this.originalLink = source.readString();
            this.title = source.readString();
            this.fileSize = source.readString();
            this.duration = source.readString();
            this.fileType = source.readString();
            this.mediaUuid = source.readString();
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

        public String getMediaUuid() {
            return this.mediaUuid;
        }

        public void setMediaUuid(String mediaUuid) {
            this.mediaUuid = mediaUuid;
        }
    }

    public PublicMediaMessage(Parcel source) {
        readFromParcel(source);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.createtime);
        dest.writeInt(this.forwardable);
        dest.writeString(this.msgtype);
        dest.writeInt(this.activeStatus);
        dest.writeString(this.paUuid);
        dest.writeValue(this.media);
    }

    public void readFromParcel(Parcel source) {
        this.createtime = source.readString();
        this.forwardable = source.readInt();
        this.msgtype = source.readString();
        this.activeStatus = source.readInt();
        this.paUuid = source.readString();
        this.media = (PublicMediaContent) source.readValue(getClass().getClassLoader());
    }

    public PublicMediaContent getMedia() {
        return this.media;
    }

    public void setMedia(PublicMediaContent media) {
        this.media = media;
    }
}
