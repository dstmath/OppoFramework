package com.suntek.mway.rcs.client.aidl.plugin.entity.pubacct;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.LinkedList;
import java.util.List;

public class MsgContent implements Parcelable {
    public static final Creator<MsgContent> CREATOR = new Creator<MsgContent>() {
        public MsgContent createFromParcel(Parcel source) {
            return new MsgContent(source);
        }

        public MsgContent[] newArray(int size) {
            return new MsgContent[size];
        }
    };
    private List<MediaArticle> articleList;
    private MediaBasic basic;
    private String createTime;
    private String mediaType;
    private String msgUuid;
    private String paUuid;
    private String smsDigest;
    private String text;

    public MsgContent(Parcel source) {
        readFromParcel(source);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mediaType);
        dest.writeString(this.createTime);
        dest.writeString(this.msgUuid);
        dest.writeString(this.smsDigest);
        dest.writeString(this.text);
        dest.writeString(this.paUuid);
        dest.writeValue(this.basic);
        dest.writeList(this.articleList);
    }

    public void readFromParcel(Parcel source) {
        this.mediaType = source.readString();
        this.createTime = source.readString();
        this.msgUuid = source.readString();
        this.smsDigest = source.readString();
        this.text = source.readString();
        this.paUuid = source.readString();
        this.basic = (MediaBasic) source.readValue(getClass().getClassLoader());
        this.articleList = new LinkedList();
        source.readList(this.articleList, getClass().getClassLoader());
    }

    public String getMediaType() {
        return this.mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getMsgUuid() {
        return this.msgUuid;
    }

    public void setMsgUuid(String msgUuid) {
        this.msgUuid = msgUuid;
    }

    public String getSmsDigest() {
        return this.smsDigest;
    }

    public void setSmsDigest(String smsDigest) {
        this.smsDigest = smsDigest;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPaUuid() {
        return this.paUuid;
    }

    public void setPaUuid(String paUuid) {
        this.paUuid = paUuid;
    }

    public MediaBasic getBasic() {
        return this.basic;
    }

    public void setBasic(MediaBasic basic) {
        this.basic = basic;
    }

    public List<MediaArticle> getArticleList() {
        return this.articleList;
    }

    public void setArticleList(List<MediaArticle> articleList) {
        this.articleList = articleList;
    }

    public String toString() {
        StringBuffer sbuffer = new StringBuffer();
        if (this.mediaType != null) {
            sbuffer.append(",mediaType=").append(this.mediaType);
        }
        if (this.createTime != null) {
            sbuffer.append(",createTime=").append(this.createTime);
        }
        if (this.msgUuid != null) {
            sbuffer.append(",msgUuid=").append(this.msgUuid);
        }
        if (this.smsDigest != null) {
            sbuffer.append(",smsDigest=").append(this.smsDigest);
        }
        if (this.text != null) {
            sbuffer.append(",text=").append(this.text);
        }
        if (this.paUuid != null) {
            sbuffer.append(",paUuid=").append(this.paUuid);
        }
        if (this.basic != null) {
            sbuffer.append(",basic=").append("{").append(this.basic.toString()).append("}");
        }
        if (this.articleList != null && this.articleList.size() > 0) {
            sbuffer.append(",articleList=").append("[");
            for (MediaArticle article : this.articleList) {
                sbuffer.append(article.toString());
            }
            sbuffer.append("]");
        }
        if (sbuffer.length() > 1) {
            return sbuffer.substring(1).toString();
        }
        return "";
    }
}
