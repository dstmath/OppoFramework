package com.suntek.mway.rcs.client.aidl.plugin.entity.cloudfile;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Map;

public class FileNode implements Parcelable {
    public static final Creator<FileNode> CREATOR = new Creator<FileNode>() {
        public FileNode createFromParcel(Parcel source) {
            return new FileNode(source);
        }

        public FileNode[] newArray(int size) {
            return new FileNode[size];
        }
    };
    private String bigThumbURL;
    private String createTime;
    private String digest;
    private int dirLevel;
    private long eTag;
    private Map<String, String> fields;
    private String fullPathInID;
    private String id;
    private boolean isFile;
    private boolean isFixed;
    private boolean isNeedUpdate;
    private boolean isNeedUpload;
    private boolean isShared;
    private boolean isSuccess;
    private String localBigThumbPath;
    private String localPath;
    private String localThumbPath;
    private String name;
    private String oldName;
    private String oldRemotePath;
    private String parentID;
    private String parentPath;
    private String remotePath;
    private String shareParentID;
    private ShareType shareType;
    private long size;
    private String suffix;
    private String thumbnailURL;
    private FileType type;
    private String updateTime;
    private long uploadSize;
    private long version;

    public enum FileType {
        photo,
        audio,
        video,
        document,
        application,
        all,
        searchByName,
        searchByExt;

        public static FileType valueOf(int ordinal) {
            if (ordinal >= 0 && ordinal < values().length) {
                return values()[ordinal];
            }
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
    }

    public enum Order {
        name,
        name_revers,
        createdate,
        createdate_revers,
        updatedate,
        updatedate_revers;

        public static Order valueOf(int ordinal) {
            if (ordinal >= 0 && ordinal < values().length) {
                return values()[ordinal];
            }
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
    }

    public enum ShareType {
        outlink,
        p2pshare,
        both;

        public static ShareType valueOf(int ordinal) {
            if (ordinal >= 0 && ordinal < values().length) {
                return values()[ordinal];
            }
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
    }

    public enum SyncType {
        noSync,
        autoSync,
        forceSync;

        public static SyncType valueOf(int ordinal) {
            if (ordinal >= 0 && ordinal < values().length) {
                return values()[ordinal];
            }
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
    }

    public enum ThumbType {
        bigThumb,
        middleThumb,
        smallThumb;

        public static ThumbType valueOf(int ordinal) {
            if (ordinal >= 0 && ordinal < values().length) {
                return values()[ordinal];
            }
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
    }

    public FileNode(Parcel source) {
        readFromParcel(source);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.suffix);
        dest.writeString(this.createTime);
        dest.writeString(this.updateTime);
        dest.writeString(this.digest);
        dest.writeString(this.remotePath);
        dest.writeString(this.oldName);
        dest.writeString(this.oldRemotePath);
        dest.writeString(this.localPath);
        dest.writeString(this.parentPath);
        dest.writeString(this.thumbnailURL);
        dest.writeString(this.localThumbPath);
        dest.writeString(this.bigThumbURL);
        dest.writeString(this.localBigThumbPath);
        dest.writeString(this.id);
        dest.writeString(this.parentID);
        dest.writeString(this.fullPathInID);
        dest.writeString(this.shareParentID);
        dest.writeBooleanArray(new boolean[]{this.isFile});
        dest.writeBooleanArray(new boolean[]{this.isShared});
        dest.writeBooleanArray(new boolean[]{this.isFixed});
        dest.writeBooleanArray(new boolean[]{this.isNeedUpdate});
        dest.writeBooleanArray(new boolean[]{this.isNeedUpload});
        dest.writeBooleanArray(new boolean[]{this.isSuccess});
        dest.writeLong(this.uploadSize);
        dest.writeInt(this.dirLevel);
        dest.writeLong(this.eTag);
        dest.writeLong(this.size);
        dest.writeLong(this.version);
        dest.writeInt(this.shareType.ordinal());
        dest.writeInt(this.type.ordinal());
        dest.writeMap(this.fields);
    }

    public void readFromParcel(Parcel source) {
        this.name = source.readString();
        this.suffix = source.readString();
        this.createTime = source.readString();
        this.updateTime = source.readString();
        this.digest = source.readString();
        this.remotePath = source.readString();
        this.oldName = source.readString();
        this.oldRemotePath = source.readString();
        this.localPath = source.readString();
        this.parentPath = source.readString();
        this.thumbnailURL = source.readString();
        this.localThumbPath = source.readString();
        this.bigThumbURL = source.readString();
        this.localBigThumbPath = source.readString();
        this.id = source.readString();
        this.parentID = source.readString();
        this.fullPathInID = source.readString();
        this.shareParentID = source.readString();
        this.isFile = source.createBooleanArray()[0];
        this.isShared = source.createBooleanArray()[0];
        this.isFixed = source.createBooleanArray()[0];
        this.isNeedUpdate = source.createBooleanArray()[0];
        this.isNeedUpload = source.createBooleanArray()[0];
        this.isSuccess = source.createBooleanArray()[0];
        this.uploadSize = source.readLong();
        this.dirLevel = source.readInt();
        this.eTag = source.readLong();
        this.size = source.readLong();
        this.version = source.readLong();
        this.shareType = ShareType.valueOf(source.readInt());
        this.type = FileType.valueOf(source.readInt());
        this.fields = source.readHashMap(getClass().getClassLoader());
    }

    public boolean isFile() {
        return this.isFile;
    }

    public void setFile(boolean isFile) {
        this.isFile = isFile;
    }

    public boolean isShared() {
        return this.isShared;
    }

    public void setShared(boolean isShared) {
        this.isShared = isShared;
    }

    public boolean isFixed() {
        return this.isFixed;
    }

    public void setFixed(boolean isFixed) {
        this.isFixed = isFixed;
    }

    public boolean isNeedUpdate() {
        return this.isNeedUpdate;
    }

    public void setNeedUpdate(boolean isNeedUpdate) {
        this.isNeedUpdate = isNeedUpdate;
    }

    public boolean isNeedUpload() {
        return this.isNeedUpload;
    }

    public void setNeedUpload(boolean isNeedUpload) {
        this.isNeedUpload = isNeedUpload;
    }

    public boolean isSuccess() {
        return this.isSuccess;
    }

    public void setSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public long getUploadSize() {
        return this.uploadSize;
    }

    public void setUploadSize(long uploadSize) {
        this.uploadSize = uploadSize;
    }

    public FileType getType() {
        return this.type;
    }

    public void setType(FileType type) {
        this.type = type;
    }

    public int getDirLevel() {
        return this.dirLevel;
    }

    public void setDirLevel(int dirLevel) {
        this.dirLevel = dirLevel;
    }

    public long geteTag() {
        return this.eTag;
    }

    public void seteTag(long eTag) {
        this.eTag = eTag;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSuffix() {
        return this.suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public ShareType getShareType() {
        return this.shareType;
    }

    public void setShareType(ShareType shareType) {
        this.shareType = shareType;
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

    public long getSize() {
        return this.size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getDigest() {
        return this.digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String getRemotePath() {
        return this.remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public String getOldName() {
        return this.oldName;
    }

    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

    public String getOldRemotePath() {
        return this.oldRemotePath;
    }

    public void setOldRemotePath(String oldRemotePath) {
        this.oldRemotePath = oldRemotePath;
    }

    public String getLocalPath() {
        return this.localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getParentPath() {
        return this.parentPath;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    public String getThumbnailURL() {
        return this.thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    public String getLocalThumbPath() {
        return this.localThumbPath;
    }

    public void setLocalThumbPath(String localThumbPath) {
        this.localThumbPath = localThumbPath;
    }

    public String getBigThumbURL() {
        return this.bigThumbURL;
    }

    public void setBigThumbURL(String bigThumbURL) {
        this.bigThumbURL = bigThumbURL;
    }

    public String getLocalBigThumbPath() {
        return this.localBigThumbPath;
    }

    public void setLocalBigThumbPath(String localBigThumbPath) {
        this.localBigThumbPath = localBigThumbPath;
    }

    public long getVersion() {
        return this.version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentID() {
        return this.parentID;
    }

    public void setParentID(String parentID) {
        this.parentID = parentID;
    }

    public String getFullPathInID() {
        return this.fullPathInID;
    }

    public void setFullPathInID(String fullPathInID) {
        this.fullPathInID = fullPathInID;
    }

    public String getShareParentID() {
        return this.shareParentID;
    }

    public void setShareParentID(String shareParentID) {
        this.shareParentID = shareParentID;
    }

    public Map<String, String> getFields() {
        return this.fields;
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }
}
