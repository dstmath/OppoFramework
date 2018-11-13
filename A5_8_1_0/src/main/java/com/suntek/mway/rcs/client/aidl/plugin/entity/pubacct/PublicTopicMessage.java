package com.suntek.mway.rcs.client.aidl.plugin.entity.pubacct;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.LinkedList;
import java.util.List;

public class PublicTopicMessage extends PublicMessage implements Parcelable {
    public static final Creator<PublicTopicMessage> CREATOR = new Creator<PublicTopicMessage>() {
        public PublicTopicMessage createFromParcel(Parcel source) {
            return new PublicTopicMessage(source);
        }

        public PublicTopicMessage[] newArray(int size) {
            return new PublicTopicMessage[size];
        }
    };
    private List<PublicTopicContent> topics;

    public static class PublicTopicContent implements Parcelable {
        public static final Creator<PublicTopicContent> CREATOR = new Creator<PublicTopicContent>() {
            public PublicTopicContent createFromParcel(Parcel source) {
                return new PublicTopicContent(source);
            }

            public PublicTopicContent[] newArray(int size) {
                return new PublicTopicContent[size];
            }
        };
        private String author;
        private String bodyLink;
        private String mainText;
        private String mediaUuid;
        private String originalLink;
        private String sourceLink;
        private String thumbLink;
        private String title;

        public PublicTopicContent(Parcel source) {
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
            dest.writeString(this.mediaUuid);
            dest.writeString(this.mainText);
            dest.writeString(this.bodyLink);
        }

        public void readFromParcel(Parcel source) {
            this.title = source.readString();
            this.author = source.readString();
            this.thumbLink = source.readString();
            this.originalLink = source.readString();
            this.sourceLink = source.readString();
            this.mediaUuid = source.readString();
            this.mainText = source.readString();
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

        public String getMediaUuid() {
            return this.mediaUuid;
        }

        public void setMediaUuid(String mediaUuid) {
            this.mediaUuid = mediaUuid;
        }

        public String getMainText() {
            return this.mainText;
        }

        public void setMainText(String mainText) {
            this.mainText = mainText;
        }

        public String getBodyLink() {
            return this.bodyLink;
        }

        public void setBodyLink(String bodyLink) {
            this.bodyLink = bodyLink;
        }
    }

    public PublicTopicMessage(Parcel source) {
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
        dest.writeList(this.topics);
    }

    public void readFromParcel(Parcel source) {
        this.createtime = source.readString();
        this.forwardable = source.readInt();
        this.msgtype = source.readString();
        this.activeStatus = source.readInt();
        this.paUuid = source.readString();
        this.topics = new LinkedList();
        source.readList(this.topics, getClass().getClassLoader());
    }

    public List<PublicTopicContent> getTopics() {
        return this.topics;
    }

    public void setTopics(List<PublicTopicContent> topics) {
        this.topics = topics;
    }
}
