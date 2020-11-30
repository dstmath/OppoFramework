package com.color.zoomwindow;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class ColorZoomWindowRUSConfig implements Parcelable {
    public static final Parcelable.Creator<ColorZoomWindowRUSConfig> CREATOR = new Parcelable.Creator<ColorZoomWindowRUSConfig>() {
        /* class com.color.zoomwindow.ColorZoomWindowRUSConfig.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ColorZoomWindowRUSConfig createFromParcel(Parcel in) {
            return new ColorZoomWindowRUSConfig(in);
        }

        @Override // android.os.Parcelable.Creator
        public ColorZoomWindowRUSConfig[] newArray(int size) {
            return new ColorZoomWindowRUSConfig[size];
        }
    };
    private ColorZoomWindowRegion mColorZoomWindowRegion = new ColorZoomWindowRegion();
    private ColorZoomWindowSize mColorZoomWindowSize = new ColorZoomWindowSize();
    private float mCornerRadius;
    private List<String> mCpnList = new ArrayList();
    private List<String> mPkgList = new ArrayList();
    private List<String> mReplyPkgList = new ArrayList();
    private List<String> mUnSupportCpnList = new ArrayList();
    private int mVersion;
    private boolean mZoomWindowSwitch;

    public ColorZoomWindowRegion getColorZoomWindowRegion() {
        return this.mColorZoomWindowRegion;
    }

    public void setColorZoomWindowRegion(ColorZoomWindowRegion colorZoomWindowRegion) {
        this.mColorZoomWindowRegion = colorZoomWindowRegion;
    }

    public int getVersion() {
        return this.mVersion;
    }

    public void setVersion(int version) {
        this.mVersion = version;
    }

    public boolean getZoomWindowSwitch() {
        return this.mZoomWindowSwitch;
    }

    public void setZoomWindowSwitch(boolean ZoomWindowSwitch) {
        this.mZoomWindowSwitch = ZoomWindowSwitch;
    }

    public List<String> getPkgList() {
        return this.mPkgList;
    }

    public void setPkgList(List<String> PkgList) {
        this.mPkgList = PkgList;
    }

    public List<String> getReplyPkgList() {
        return this.mReplyPkgList;
    }

    public void setReplyPkgList(List<String> ReplyPkgList) {
        this.mReplyPkgList = ReplyPkgList;
    }

    public List<String> getCpnList() {
        return this.mCpnList;
    }

    public void setCpnList(List<String> CpnList) {
        this.mCpnList = CpnList;
    }

    public List<String> getUnSupportCpnList() {
        return this.mUnSupportCpnList;
    }

    public void setUnSupportCpnList(List<String> UnSupportCpnList) {
        this.mUnSupportCpnList = UnSupportCpnList;
    }

    public float getCornerRadius() {
        return this.mCornerRadius;
    }

    public void setCornerRadius(float CornerRadius) {
        this.mCornerRadius = CornerRadius;
    }

    public ColorZoomWindowSize getColorZoomWindowSize() {
        return this.mColorZoomWindowSize;
    }

    public void setColorZoomWindowSize(ColorZoomWindowSize ColorZoomWindowSize) {
        this.mColorZoomWindowSize = ColorZoomWindowSize;
    }

    public ColorZoomWindowRUSConfig() {
    }

    public ColorZoomWindowRUSConfig(Parcel in) {
        this.mVersion = in.readInt();
        this.mZoomWindowSwitch = in.readByte() != 0;
        this.mPkgList = in.createStringArrayList();
        this.mReplyPkgList = in.createStringArrayList();
        this.mCpnList = in.createStringArrayList();
        this.mUnSupportCpnList = in.createStringArrayList();
        this.mCornerRadius = in.readFloat();
        this.mColorZoomWindowSize = (ColorZoomWindowSize) in.readParcelable(ColorZoomWindowSize.class.getClassLoader());
        this.mColorZoomWindowRegion = (ColorZoomWindowRegion) in.readParcelable(ColorZoomWindowRegion.class.getClassLoader());
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mVersion);
        dest.writeByte(this.mZoomWindowSwitch ? (byte) 1 : 0);
        dest.writeStringList(this.mPkgList);
        dest.writeStringList(this.mReplyPkgList);
        dest.writeStringList(this.mCpnList);
        dest.writeStringList(this.mUnSupportCpnList);
        dest.writeFloat(this.mCornerRadius);
        dest.writeParcelable(this.mColorZoomWindowSize, flags);
        dest.writeParcelable(this.mColorZoomWindowRegion, flags);
    }

    public String toString() {
        return "version = " + this.mVersion + "\nZoomWindowSwitch = " + this.mZoomWindowSwitch + "\nCornerRadius = " + this.mCornerRadius + "\nZoom Window size = " + this.mColorZoomWindowSize.toString() + "\nRegion = " + this.mColorZoomWindowRegion.toString();
    }
}
