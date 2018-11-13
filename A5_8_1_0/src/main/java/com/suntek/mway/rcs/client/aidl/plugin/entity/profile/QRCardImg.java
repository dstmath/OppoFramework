package com.suntek.mway.rcs.client.aidl.plugin.entity.profile;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class QRCardImg extends BaseModel implements Parcelable, Serializable {
    public static final Creator<QRCardImg> CREATOR = new Creator<QRCardImg>() {
        public QRCardImg createFromParcel(Parcel source) {
            return new QRCardImg(source);
        }

        public QRCardImg[] newArray(int size) {
            return new QRCardImg[size];
        }
    };
    private static final long serialVersionUID = 4674425471217216710L;
    private boolean businessFlag;
    private String description;
    private String imgBase64Str;
    private String imgEncoding = "BASE64";
    private IMAGE_TYPE imgType = IMAGE_TYPE.PNG;

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

    public QRCardImg(Parcel source) {
        readFromParcel(source);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeBooleanArray(new boolean[]{this.businessFlag});
        dest.writeInt(this.imgType.ordinal());
        dest.writeString(this.imgEncoding);
        dest.writeString(this.description);
        dest.writeString(this.imgBase64Str);
    }

    public void readFromParcel(Parcel source) {
        super.readFromParcel(source);
        boolean[] val = new boolean[1];
        source.readBooleanArray(val);
        this.businessFlag = val[0];
        this.imgType = IMAGE_TYPE.valueOf(source.readInt());
        this.imgEncoding = source.readString();
        this.description = source.readString();
        this.imgBase64Str = source.readString();
    }

    public boolean isBusinessFlag() {
        return this.businessFlag;
    }

    public void setBusinessFlag(boolean businessFlag) {
        this.businessFlag = businessFlag;
    }

    public IMAGE_TYPE getImgType() {
        return this.imgType;
    }

    public void setImgType(IMAGE_TYPE imgType) {
        this.imgType = imgType;
    }

    public String getImgEncoding() {
        return this.imgEncoding;
    }

    public void setImgEncoding(String imgEncoding) {
        this.imgEncoding = imgEncoding;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImgBase64Str() {
        return this.imgBase64Str;
    }

    public void setImgBase64Str(String imgBase64Str) {
        this.imgBase64Str = imgBase64Str;
    }

    public String toString() {
        List<String> list = new ArrayList();
        list.add("businessFlag=" + this.businessFlag);
        list.add("imgEncoding=" + this.imgEncoding);
        list.add("imgBase64Str=" + this.imgBase64Str);
        list.add("imgType=" + this.imgType);
        list.add("description=" + this.description);
        list.add("account=" + getAccount());
        list.add("etag=" + getEtag());
        return list.toString();
    }
}
