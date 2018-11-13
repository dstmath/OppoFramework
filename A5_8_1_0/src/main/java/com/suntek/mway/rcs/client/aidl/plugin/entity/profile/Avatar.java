package com.suntek.mway.rcs.client.aidl.plugin.entity.profile;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Avatar extends BaseModel implements Parcelable, Serializable {
    public static final Creator<Avatar> CREATOR = new Creator<Avatar>() {
        public Avatar createFromParcel(Parcel source) {
            return new Avatar(source);
        }

        public Avatar[] newArray(int size) {
            return new Avatar[size];
        }
    };
    public static final String GIF = "GIF";
    public static final String JPG = "JPG";
    public static final String PNG = "PNG";
    private static final long serialVersionUID = -47856440160432L;
    private IMAGE_TYPE avatarImgType = IMAGE_TYPE.PNG;
    private String imgBase64Str;
    private String imgEncoding = "BASE64";

    public enum IMAGE_TYPE {
        PNG,
        JPG,
        GIF;

        public static IMAGE_TYPE valueOf(int ordinal) {
            if (ordinal >= 0 && ordinal < values().length) {
                return values()[ordinal];
            }
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
    }

    public Avatar(Parcel source) {
        readFromParcel(source);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.avatarImgType.ordinal());
        dest.writeString(this.imgEncoding);
        dest.writeString(this.imgBase64Str);
    }

    public void readFromParcel(Parcel source) {
        super.readFromParcel(source);
        this.avatarImgType = IMAGE_TYPE.valueOf(source.readInt());
        this.imgEncoding = source.readString();
        this.imgBase64Str = source.readString();
    }

    public IMAGE_TYPE getAvatarImgType() {
        return this.avatarImgType;
    }

    public void setAvatarImgType(IMAGE_TYPE avatarImgType) {
        this.avatarImgType = avatarImgType;
    }

    public String getImgBase64Str() {
        return this.imgBase64Str;
    }

    public void setImgBase64Str(String imgBase64Str) {
        this.imgBase64Str = imgBase64Str;
    }

    public String toString() {
        List<String> list = new ArrayList();
        list.add("avatarImgType=" + this.avatarImgType);
        list.add("account=" + getAccount());
        list.add("etag=" + getEtag());
        list.add("imgEncoding=" + this.imgEncoding);
        list.add("imgBase64Str=" + this.imgBase64Str);
        return list.toString();
    }
}
