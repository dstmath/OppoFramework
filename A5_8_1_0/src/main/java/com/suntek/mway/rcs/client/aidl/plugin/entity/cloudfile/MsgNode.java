package com.suntek.mway.rcs.client.aidl.plugin.entity.cloudfile;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Map;

public class MsgNode implements Parcelable {
    public static final Creator<MsgNode> CREATOR = new Creator<MsgNode>() {
        public MsgNode createFromParcel(Parcel source) {
            return new MsgNode(source);
        }

        public MsgNode[] newArray(int size) {
            return new MsgNode[size];
        }
    };
    private byte[] attachment;
    private BoxType boxType;
    private String content;
    private Map<String, String> fields;
    private String id;
    private boolean isRead;
    private boolean isSend;
    private int locked;
    private MsgType msgType;
    private int number;
    private String receiver;
    private MsgResult result;
    private String sender;
    private int size;
    private String time;

    public enum BoxType {
        inbox,
        outbox,
        draft;

        public static BoxType valueOf(int ordinal) {
            if (ordinal >= 0 && ordinal < values().length) {
                return values()[ordinal];
            }
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
    }

    public enum MsgResult {
        success,
        duplication,
        fail,
        ignor;

        public static MsgResult valueOf(int ordinal) {
            if (ordinal >= 0 && ordinal < values().length) {
                return values()[ordinal];
            }
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
    }

    public enum MsgType {
        sms,
        mms;

        public static MsgType valueOf(int ordinal) {
            if (ordinal >= 0 && ordinal < values().length) {
                return values()[ordinal];
            }
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
    }

    public enum Order {
        date,
        date_Reverse,
        sender,
        sender_Reverse,
        receiver,
        receiver_Reverse,
        thread,
        thread_Reverse;

        public static Order valueOf(int ordinal) {
            if (ordinal >= 0 && ordinal < values().length) {
                return values()[ordinal];
            }
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
    }

    public MsgNode(Parcel source) {
        readFromParcel(source);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.content);
        dest.writeString(this.sender);
        dest.writeString(this.receiver);
        dest.writeString(this.time);
        dest.writeString(this.id);
        dest.writeInt(this.size);
        dest.writeInt(this.number);
        dest.writeInt(this.locked);
        dest.writeInt(this.msgType.ordinal());
        dest.writeInt(this.boxType.ordinal());
        dest.writeInt(this.result.ordinal());
        dest.writeBooleanArray(new boolean[]{this.isSend});
        dest.writeBooleanArray(new boolean[]{this.isRead});
        dest.writeByteArray(this.attachment);
        dest.writeMap(this.fields);
    }

    public void readFromParcel(Parcel source) {
        this.content = source.readString();
        this.sender = source.readString();
        this.receiver = source.readString();
        this.time = source.readString();
        this.id = source.readString();
        this.size = source.readInt();
        this.number = source.readInt();
        this.locked = source.readInt();
        this.msgType = MsgType.valueOf(source.readInt());
        this.boxType = BoxType.valueOf(source.readInt());
        this.result = MsgResult.valueOf(source.readInt());
        this.isSend = source.createBooleanArray()[0];
        this.isRead = source.createBooleanArray()[0];
        this.attachment = source.createByteArray();
        this.fields = source.readHashMap(getClass().getClassLoader());
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return this.sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return this.receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getTime() {
        return this.time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public MsgType getMsgType() {
        return this.msgType;
    }

    public void setMsgType(MsgType msgType) {
        this.msgType = msgType;
    }

    public BoxType getBoxType() {
        return this.boxType;
    }

    public void setBoxType(BoxType boxType) {
        this.boxType = boxType;
    }

    public MsgResult getResult() {
        return this.result;
    }

    public void setResult(MsgResult result) {
        this.result = result;
    }

    public boolean isSend() {
        return this.isSend;
    }

    public void setSend(boolean isSend) {
        this.isSend = isSend;
    }

    public boolean isRead() {
        return this.isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    public int getSize() {
        return this.size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getNumber() {
        return this.number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getLocked() {
        return this.locked;
    }

    public void setLocked(int locked) {
        this.locked = locked;
    }

    public byte[] getAttachment() {
        return this.attachment;
    }

    public void setAttachment(byte[] attachment) {
        this.attachment = attachment;
    }

    public Map<String, String> getFields() {
        return this.fields;
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }
}
