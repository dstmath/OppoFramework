package com.suntek.mway.rcs.client.aidl.service.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class GroupChat implements Parcelable {
    public static final Creator<GroupChat> CREATOR = new Creator<GroupChat>() {
        public GroupChat createFromParcel(Parcel in) {
            return new GroupChat(in);
        }

        public GroupChat[] newArray(int size) {
            return new GroupChat[size];
        }
    };
    public static final int INCOMING = 1;
    public static final int MESSAGE_NOT_RECEIVE = 2;
    public static final int MESSAGE_NOT_REMIND = 1;
    public static final int MESSAGE_RECEIVE_AND_REMIND = 0;
    public static final int OUTGOING = 2;
    public static final int STATUS_BOOTED = 11;
    public static final int STATUS_FAILED = 13;
    public static final int STATUS_GROUP_FULL = 16;
    public static final int STATUS_INITIATED = 1;
    public static final int STATUS_INVITED = 2;
    public static final int STATUS_PAUSE = 15;
    public static final int STATUS_QUITED = 10;
    public static final int STATUS_REJECT = 14;
    public static final int STATUS_STARTED = 3;
    public static final int STATUS_TERMINATED = 12;
    private String chairman;
    private String chatUri;
    private String contributionId;
    private String conversationId;
    private long date;
    private int direction;
    private long id;
    private int maxCount;
    private String owner;
    private int policy;
    private String remark;
    private int status;
    private String subject;
    private long threadId;

    public GroupChat(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.threadId);
        dest.writeString(this.subject);
        dest.writeString(this.chatUri);
        dest.writeInt(this.status);
        dest.writeString(this.chairman);
        dest.writeInt(this.direction);
        dest.writeInt(this.maxCount);
        dest.writeString(this.remark);
        dest.writeInt(this.policy);
        dest.writeLong(this.date);
        dest.writeString(this.conversationId);
        dest.writeString(this.contributionId);
        dest.writeString(this.owner);
    }

    public void readFromParcel(Parcel source) {
        this.id = source.readLong();
        this.threadId = source.readLong();
        this.subject = source.readString();
        this.chatUri = source.readString();
        this.status = source.readInt();
        this.chairman = source.readString();
        this.direction = source.readInt();
        this.maxCount = source.readInt();
        this.remark = source.readString();
        this.policy = source.readInt();
        this.date = source.readLong();
        this.conversationId = source.readString();
        this.contributionId = source.readString();
        this.owner = source.readString();
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getThreadId() {
        return this.threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public String getSubject() {
        return this.subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getChatUri() {
        return this.chatUri;
    }

    public void setChatUri(String chatUri) {
        this.chatUri = chatUri;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getChairman() {
        return this.chairman;
    }

    public void setChairman(String chairman) {
        this.chairman = chairman;
    }

    public int getDirection() {
        return this.direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getMaxCount() {
        return this.maxCount;
    }

    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }

    public String getRemark() {
        return this.remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getPolicy() {
        return this.policy;
    }

    public void setPolicy(int policy) {
        this.policy = policy;
    }

    public long getDate() {
        return this.date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getConversationId() {
        return this.conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getContributionId() {
        return this.contributionId;
    }

    public void setContributionId(String contributionId) {
        this.contributionId = contributionId;
    }

    public String getOwner() {
        return this.owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isGroupChatValid() {
        if (this.status == 1 || this.status == 2 || this.status == 3) {
            return true;
        }
        return false;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("GroupChat{").append("id=").append(this.id).append(",threadId=").append(this.threadId).append(",subject=").append(this.subject).append(",chatUri=").append(this.chatUri).append(",status=").append(this.status).append(",chairman=").append(this.chairman).append(",direction=").append(this.direction).append(",maxCount=").append(this.maxCount).append(",remark=").append(this.remark).append(",policy=").append(this.policy).append(",date=").append(this.date).append(",conversationId=").append(this.conversationId).append(",contributionId=").append(this.contributionId).append(",owner=").append(this.owner).append("}");
        return buf.toString();
    }
}
