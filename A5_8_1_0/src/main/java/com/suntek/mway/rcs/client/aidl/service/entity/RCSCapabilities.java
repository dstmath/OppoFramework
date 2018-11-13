package com.suntek.mway.rcs.client.aidl.service.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.io.Serializable;

public class RCSCapabilities implements Parcelable, Serializable {
    public static final Creator<RCSCapabilities> CREATOR = new Creator<RCSCapabilities>() {
        public RCSCapabilities createFromParcel(Parcel source) {
            return new RCSCapabilities(source);
        }

        public RCSCapabilities[] newArray(int size) {
            return new RCSCapabilities[size];
        }
    };
    private static final long serialVersionUID = 5516256269504150135L;
    private boolean burnAfterReading = false;
    private boolean cloudFileSupported = false;
    private boolean cmccSupported = false;
    private boolean fileTransferStoreForwardSupported = false;
    private boolean fileTransferSupported = false;
    private boolean fileTransferThumbnailSupported = false;
    private boolean geolocationPullSupported = false;
    private boolean geolocationPushSupported = false;
    private boolean groupChatStoreForwardSupported = false;
    private boolean imSessionSupported = false;
    private boolean largeModeMsgSupported = false;
    private boolean pageModeMsgSupported = false;
    private boolean publicMsgSupported = false;
    private long timestamp = System.currentTimeMillis();
    private boolean vemotionSupported = false;

    public RCSCapabilities(Parcel source) {
        boolean z;
        boolean z2 = true;
        this.imSessionSupported = source.readInt() != 0;
        if (source.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.fileTransferSupported = z;
        if (source.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.geolocationPushSupported = z;
        if (source.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.geolocationPullSupported = z;
        if (source.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.fileTransferThumbnailSupported = z;
        if (source.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.fileTransferStoreForwardSupported = z;
        if (source.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.groupChatStoreForwardSupported = z;
        if (source.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.pageModeMsgSupported = z;
        if (source.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.largeModeMsgSupported = z;
        if (source.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.publicMsgSupported = z;
        if (source.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.vemotionSupported = z;
        if (source.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.cloudFileSupported = z;
        if (source.readInt() != 0) {
            z = true;
        } else {
            z = false;
        }
        this.cmccSupported = z;
        if (source.readInt() == 0) {
            z2 = false;
        }
        this.burnAfterReading = z2;
        this.timestamp = source.readLong();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        dest.writeInt(this.imSessionSupported ? 1 : 0);
        if (this.fileTransferSupported) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.geolocationPushSupported) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.geolocationPullSupported) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.fileTransferThumbnailSupported) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.fileTransferStoreForwardSupported) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.groupChatStoreForwardSupported) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.pageModeMsgSupported) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.largeModeMsgSupported) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.publicMsgSupported) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.vemotionSupported) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.cloudFileSupported) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.cmccSupported) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (!this.burnAfterReading) {
            i2 = 0;
        }
        dest.writeInt(i2);
        dest.writeLong(this.timestamp);
    }

    public boolean isImSessionSupported() {
        return this.imSessionSupported;
    }

    public void setImSessionSupported(boolean supported) {
        this.imSessionSupported = supported;
    }

    public boolean isFileTransferSupported() {
        return this.fileTransferSupported;
    }

    public void setFileTransferSupported(boolean supported) {
        this.fileTransferSupported = supported;
    }

    public boolean isGeolocationPushSupported() {
        return this.geolocationPushSupported;
    }

    public void setGeolocationPullSupported(boolean supported) {
        this.geolocationPullSupported = supported;
    }

    public boolean isGeolocationPullSupported() {
        return this.geolocationPullSupported;
    }

    public void setGeolocationPushSupported(boolean supported) {
        this.geolocationPushSupported = supported;
    }

    public boolean isFileTransferThumbnailSupported() {
        return this.fileTransferThumbnailSupported;
    }

    public void setFileTransferThumbnailSupported(boolean supported) {
        this.fileTransferThumbnailSupported = supported;
    }

    public boolean isFileTransferStoreForwardSupported() {
        return this.fileTransferStoreForwardSupported;
    }

    public void setFileTransferStoreForwardSupported(boolean supported) {
        this.fileTransferStoreForwardSupported = supported;
    }

    public boolean isGroupChatStoreForwardSupported() {
        return this.groupChatStoreForwardSupported;
    }

    public void setGroupChatStoreForwardSupported(boolean supported) {
        this.groupChatStoreForwardSupported = supported;
    }

    public boolean isPageModeMsgSupported() {
        return this.pageModeMsgSupported;
    }

    public void setPageModeMsgSupported(boolean pageModeMsg) {
        this.pageModeMsgSupported = pageModeMsg;
    }

    public boolean isLargeModeMsgSupported() {
        return this.largeModeMsgSupported;
    }

    public void setLargeModeMsgSupported(boolean largeModeMsg) {
        this.largeModeMsgSupported = largeModeMsg;
    }

    public boolean isPublicMsgSupported() {
        return this.publicMsgSupported;
    }

    public void setPublicMsgSupported(boolean publicMsg) {
        this.publicMsgSupported = publicMsg;
    }

    public boolean isVemotionSupported() {
        return this.vemotionSupported;
    }

    public void setVemotionSupported(boolean vemotion) {
        this.vemotionSupported = vemotion;
    }

    public boolean isCloudFileSupported() {
        return this.cloudFileSupported;
    }

    public void setCloudFileSupported(boolean cloudFileSupported) {
        this.cloudFileSupported = cloudFileSupported;
    }

    public boolean isCmccSupported() {
        return this.cmccSupported;
    }

    public void setCmccSupported(boolean cmcc) {
        this.cmccSupported = cmcc;
    }

    public boolean isBurnAfterReading() {
        return this.burnAfterReading;
    }

    public void setBurnAfterReading(boolean burnAfterReading) {
        this.burnAfterReading = burnAfterReading;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
