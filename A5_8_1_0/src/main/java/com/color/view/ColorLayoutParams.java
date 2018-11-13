package com.color.view;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ColorLayoutParams implements Parcelable {
    public static final Creator<ColorLayoutParams> CREATOR = new Creator<ColorLayoutParams>() {
        public ColorLayoutParams createFromParcel(Parcel in) {
            return new ColorLayoutParams(in);
        }

        public ColorLayoutParams[] newArray(int size) {
            return new ColorLayoutParams[size];
        }
    };
    private static final int FLAG_CUSTOM_SYSTEM_BAR = 8;
    private static final int FLAG_FULL_SCREEN_WINDOW = 32;
    private static final int FLAG_HAS_NAVIGATION_BAR = 2;
    private static final int FLAG_HAS_STATUS_BAR = 1;
    private static final int FLAG_SKIP_SYSTEM_UI_VISIBILITY = 64;
    private static final int FLAG_SYSTEM_APP_WINDOW = 16;
    private static final int FLAG_UPDATE_NAVIGATION_BAR = 4;
    private static final int FLAG_USE_LAST_STATUS_BAR_TINT = 128;
    private int mNavigationBarColor = 0;
    private int mSystemBarFlags = 0;

    public ColorLayoutParams(Parcel in) {
        readFromParcel(in);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" CLP[");
        if (this.mSystemBarFlags != 0) {
            sb.append(formatHex(this.mSystemBarFlags, "sysBarFlg"));
        }
        if (this.mNavigationBarColor != 0) {
            sb.append(formatHex(this.mNavigationBarColor, "navColor"));
        }
        sb.append(" ]");
        return sb.toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ColorLayoutParams other = (ColorLayoutParams) obj;
        return this.mSystemBarFlags == other.mSystemBarFlags && this.mNavigationBarColor == other.mNavigationBarColor;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mSystemBarFlags);
        out.writeInt(this.mNavigationBarColor);
    }

    public void readFromParcel(Parcel in) {
        this.mSystemBarFlags = in.readInt();
        this.mNavigationBarColor = in.readInt();
    }

    public void set(ColorLayoutParams src) {
        this.mSystemBarFlags = src.mSystemBarFlags;
        this.mNavigationBarColor = src.mNavigationBarColor;
    }

    public void setHasStatusBar(boolean value) {
        setFlag(value, 1);
    }

    public boolean hasStatusBar() {
        return hasFlag(1);
    }

    public void setHasNavigationBar(boolean value) {
        setFlag(value, 2);
    }

    public boolean hasNavigationBar() {
        return hasFlag(2);
    }

    public void setUpdateNavigationBar(boolean value) {
        setFlag(value, 4);
    }

    public boolean isUpdateNavigationBar() {
        return hasFlag(4);
    }

    public void setCustomSystemBar(boolean value) {
        setFlag(value, 8);
    }

    public boolean isCustomSystemBar() {
        return hasFlag(8);
    }

    public void setSystemAppWindow(boolean value) {
        setFlag(value, 16);
    }

    public boolean isSystemAppWindow() {
        return hasFlag(16);
    }

    public void setFullScreenWindow(boolean value) {
        setFlag(value, 32);
    }

    public boolean isFullScreenWindow() {
        return hasFlag(32);
    }

    public void setSkipSystemUiVisibility(boolean value) {
        setFlag(value, 64);
    }

    public boolean getSkipSystemUiVisibility() {
        return hasFlag(64);
    }

    public void setUseLastStatusBarTint(boolean value) {
        setFlag(value, 128);
    }

    public boolean isUseLastStatusBarTint() {
        return hasFlag(128);
    }

    public void setNavigationBarColor(int value) {
        this.mNavigationBarColor = value;
    }

    public int getNavigationBarColor() {
        return this.mNavigationBarColor;
    }

    private String formatHex(int value, String name) {
        return String.format(" %s=#%08x", new Object[]{name, Integer.valueOf(value)});
    }

    private void setFlag(boolean value, int flag) {
        if (value) {
            this.mSystemBarFlags |= flag;
        } else {
            this.mSystemBarFlags &= ~flag;
        }
    }

    private boolean hasFlag(int flag) {
        return (this.mSystemBarFlags & flag) == flag;
    }
}
