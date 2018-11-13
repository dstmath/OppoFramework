package com.suntek.mway.rcs.client.aidl.plugin.entity.cloudfile;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.suntek.mway.rcs.client.aidl.plugin.entity.cloudfile.FileNode.FileType;
import java.util.Map;

public class TransNode implements Parcelable {
    public static final Creator<TransNode> CREATOR = new Creator<TransNode>() {
        public TransNode createFromParcel(Parcel source) {
            return new TransNode(source);
        }

        public TransNode[] newArray(int size) {
            return new TransNode[size];
        }
    };
    private String batchID;
    private long completeSize;
    private Map<String, String> fields;
    private FileNode file;
    private String id;
    private boolean isSuccess;
    private String localPath;
    private FileType mode;
    private long order;
    private String param;
    private int percent;
    private Result result;
    private int speed;
    private Status status;
    private TransType type;
    private String uploadID;
    private String url;

    public enum TransOper {
        NEW,
        OVER_WRITE,
        RESUME,
        GET_INFO;

        public static TransOper valueOf(int ordinal) {
            if (ordinal >= 0 && ordinal < values().length) {
                return values()[ordinal];
            }
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
    }

    public enum TransType {
        UPLOAD,
        DOWNLOAD,
        DOWNLOADTHUMBNAIL,
        DOWNLOADURL,
        BACKUP,
        RESTORE,
        SHOOT;

        public static TransType valueOf(int ordinal) {
            if (ordinal >= 0 && ordinal < values().length) {
                return values()[ordinal];
            }
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
    }

    public TransNode(Parcel source) {
        readFromParcel(source);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.url);
        dest.writeString(this.localPath);
        dest.writeString(this.param);
        dest.writeString(this.uploadID);
        dest.writeString(this.batchID);
        dest.writeLong(this.completeSize);
        dest.writeLong(this.order);
        dest.writeInt(this.speed);
        dest.writeInt(this.percent);
        dest.writeBooleanArray(new boolean[]{this.isSuccess});
        dest.writeInt(this.type.ordinal());
        dest.writeInt(this.mode.ordinal());
        dest.writeInt(this.status.ordinal());
        dest.writeValue(this.file);
        dest.writeValue(this.result);
        dest.writeMap(this.fields);
    }

    public void readFromParcel(Parcel source) {
        this.id = source.readString();
        this.url = source.readString();
        this.localPath = source.readString();
        this.param = source.readString();
        this.uploadID = source.readString();
        this.batchID = source.readString();
        this.completeSize = source.readLong();
        this.order = source.readLong();
        this.speed = source.readInt();
        this.percent = source.readInt();
        this.isSuccess = source.createBooleanArray()[0];
        this.type = TransType.valueOf(source.readInt());
        this.mode = FileType.valueOf(source.readInt());
        this.status = Status.valueOf(source.readInt());
        this.file = (FileNode) source.readValue(getClass().getClassLoader());
        this.result = (Result) source.readValue(getClass().getClassLoader());
        this.fields = source.readHashMap(getClass().getClassLoader());
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getCompleteSize() {
        return this.completeSize;
    }

    public void setCompleteSize(long completeSize) {
        this.completeSize = completeSize;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLocalPath() {
        return this.localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public TransType getType() {
        return this.type;
    }

    public void setType(TransType type) {
        this.type = type;
    }

    public FileNode getFile() {
        return this.file;
    }

    public void setFile(FileNode file) {
        this.file = file;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isSuccess() {
        return this.isSuccess;
    }

    public void setSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public String getUploadID() {
        return this.uploadID;
    }

    public void setUploadID(String uploadID) {
        this.uploadID = uploadID;
    }

    public String getBatchID() {
        return this.batchID;
    }

    public void setBatchID(String batchID) {
        this.batchID = batchID;
    }

    public int getSpeed() {
        return this.speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getPercent() {
        return this.percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public FileType getMode() {
        return this.mode;
    }

    public void setMode(FileType mode) {
        this.mode = mode;
    }

    public String getParam() {
        return this.param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public long getOrder() {
        return this.order;
    }

    public void setOrder(long order) {
        this.order = order;
    }

    public Result getResult() {
        return this.result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public Map<String, String> getFields() {
        return this.fields;
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }
}
