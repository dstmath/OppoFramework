package com.suntek.mway.rcs.client.aidl.plugin.entity.cloudfile;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Arrays;
import java.util.Map;

public class ShareNode implements Parcelable {
    public static final Creator<ShareNode> CREATOR = new Creator<ShareNode>() {
        public ShareNode createFromParcel(Parcel source) {
            return new ShareNode(source);
        }

        public ShareNode[] newArray(int size) {
            return new ShareNode[size];
        }
    };
    private String createTime;
    private String desc;
    private int downloads;
    private Map<String, String> fields;
    private FileNode[] file;
    private String id;
    private boolean isSuccess;
    private Order order;
    private ShareNode[] subShares;
    private String thumbUrl;
    private ShareType type;
    private String updateTime;
    private String url;

    public enum Order {
        createTime,
        createTime_Reverse,
        UpdateTime,
        UpdateTime_Reverse,
        Downloads,
        Downloads_Reverse;

        public static Order valueOf(int ordinal) {
            if (ordinal >= 0 && ordinal < values().length) {
                return values()[ordinal];
            }
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
    }

    public enum ShareType {
        sharedFile,
        sharedFolder,
        sharedGroup;

        public static ShareType valueOf(int ordinal) {
            if (ordinal >= 0 && ordinal < values().length) {
                return values()[ordinal];
            }
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
    }

    public ShareNode(Parcel source) {
        readFromParcel(source);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.url);
        dest.writeString(this.thumbUrl);
        dest.writeString(this.createTime);
        dest.writeString(this.updateTime);
        dest.writeInt(this.type.ordinal());
        dest.writeString(this.desc);
        dest.writeInt(this.downloads);
        dest.writeParcelableArray(this.file, flags);
        dest.writeParcelableArray(this.subShares, flags);
        dest.writeString(this.id);
        dest.writeInt(this.isSuccess ? 1 : 0);
        dest.writeInt(this.order.ordinal());
        dest.writeMap(this.fields);
    }

    public void readFromParcel(Parcel source) {
        boolean z;
        this.url = source.readString();
        this.thumbUrl = source.readString();
        this.createTime = source.readString();
        this.updateTime = source.readString();
        this.type = ShareType.valueOf(source.readInt());
        this.desc = source.readString();
        this.downloads = source.readInt();
        Parcelable[] fileParcelableArray = source.readParcelableArray(getClass().getClassLoader());
        this.file = new FileNode[0];
        if (fileParcelableArray != null) {
            this.file = (FileNode[]) Arrays.copyOf(fileParcelableArray, fileParcelableArray.length, FileNode[].class);
        }
        Parcelable[] subSharesParcelableArray = source.readParcelableArray(getClass().getClassLoader());
        this.subShares = new ShareNode[0];
        if (subSharesParcelableArray != null) {
            this.subShares = (ShareNode[]) Arrays.copyOf(subSharesParcelableArray, subSharesParcelableArray.length, ShareNode[].class);
        }
        this.id = source.readString();
        if (source.readInt() == 1) {
            z = true;
        } else {
            z = false;
        }
        this.isSuccess = z;
        this.order = Order.valueOf(source.readInt());
        this.fields = source.readHashMap(getClass().getClassLoader());
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumbUrl() {
        return this.thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public String getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public ShareType getType() {
        return this.type;
    }

    public void setType(ShareType type) {
        this.type = type;
    }

    public String getDesc() {
        return this.desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getDownloads() {
        return this.downloads;
    }

    public void setDownloads(int downloads) {
        this.downloads = downloads;
    }

    public FileNode[] getFile() {
        return this.file;
    }

    public void setFile(FileNode[] file) {
        this.file = file;
    }

    public ShareNode[] getSubShares() {
        return this.subShares;
    }

    public void setSubShares(ShareNode[] subShares) {
        this.subShares = subShares;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isSuccess() {
        return this.isSuccess;
    }

    public void setSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public Order getOrder() {
        return this.order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Map<String, String> getFields() {
        return this.fields;
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }
}
