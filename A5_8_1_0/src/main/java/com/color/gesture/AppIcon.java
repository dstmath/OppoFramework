package com.color.gesture;

import android.graphics.drawable.Icon;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public final class AppIcon implements Parcelable {
    public static final Creator<AppIcon> CREATOR = new Creator<AppIcon>() {
        public AppIcon createFromParcel(Parcel source) {
            return new AppIcon(source);
        }

        public AppIcon[] newArray(int size) {
            return new AppIcon[size];
        }
    };
    private static final String TAG = "AppIcon";
    private int mHeight;
    private Icon mIcon;
    private int mStartX;
    private int mStartY;
    private int mWidth;

    public AppIcon(Parcel source) {
        readFromParcel(source);
    }

    public int getStartX() {
        return this.mStartX;
    }

    public void setStartX(int startX) {
        this.mStartX = startX;
    }

    public int getStartY() {
        return this.mStartY;
    }

    public void setStartY(int startY) {
        this.mStartY = startY;
    }

    public int getWidth() {
        return this.mWidth;
    }

    public void setWidth(int width) {
        this.mWidth = width;
    }

    public int getHeight() {
        return this.mHeight;
    }

    public void setHeigth(int height) {
        this.mHeight = height;
    }

    public Icon getIcon() {
        return this.mIcon;
    }

    public void setIcon(Icon icon) {
        this.mIcon = icon;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        if (this.mIcon != null) {
            dest.writeByte((byte) 1);
            this.mIcon.writeToParcel(dest, flags);
        } else {
            dest.writeByte((byte) 0);
        }
        dest.writeInt(this.mStartX);
        dest.writeInt(this.mStartY);
        dest.writeInt(this.mWidth);
        dest.writeInt(this.mHeight);
    }

    private void readFromParcel(Parcel source) {
        if (source.readByte() != (byte) 0) {
            this.mIcon = (Icon) Icon.CREATOR.createFromParcel(source);
        } else {
            this.mIcon = null;
        }
        this.mStartX = source.readInt();
        this.mStartY = source.readInt();
        this.mWidth = source.readInt();
        this.mHeight = source.readInt();
    }
}
