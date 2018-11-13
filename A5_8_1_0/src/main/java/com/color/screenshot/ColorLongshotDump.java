package com.color.screenshot;

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemProperties;
import android.provider.SettingsStringUtil;

public class ColorLongshotDump implements Parcelable {
    public static final Creator<ColorLongshotDump> CREATOR = new Creator<ColorLongshotDump>() {
        public ColorLongshotDump createFromParcel(Parcel in) {
            return new ColorLongshotDump(in);
        }

        public ColorLongshotDump[] newArray(int size) {
            return new ColorLongshotDump[size];
        }
    };
    public static final boolean DBG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    public static final String TAG = "LongshotDump";
    private ColorLongshotComponentName mComponent = null;
    private int mDumpCount = 0;
    private int mScrollCount = 0;
    private Rect mScrollRect = new Rect();
    private long mSpendCalc = 0;
    private long mSpendDump = 0;
    private long mSpendPack = 0;

    public ColorLongshotDump(Parcel in) {
        readFromParcel(in);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[rect=");
        sb.append(this.mScrollRect);
        sb.append(SettingsStringUtil.DELIMITER);
        sb.append(this.mComponent);
        sb.append(":dump=");
        sb.append(this.mSpendDump);
        sb.append(":calc=");
        sb.append(this.mSpendCalc);
        sb.append(":pack=");
        sb.append(this.mSpendPack);
        sb.append(":dumpCount=");
        sb.append(this.mDumpCount);
        sb.append(":scrollCount=");
        sb.append(this.mScrollCount);
        sb.append("]");
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        this.mScrollRect.writeToParcel(out, flags);
        writeComponent(out, flags);
        out.writeLong(this.mSpendDump);
        out.writeLong(this.mSpendCalc);
        out.writeLong(this.mSpendPack);
        out.writeInt(this.mDumpCount);
    }

    public void readFromParcel(Parcel in) {
        this.mScrollRect.readFromParcel(in);
        readComponent(in);
        this.mSpendDump = in.readLong();
        this.mSpendCalc = in.readLong();
        this.mSpendPack = in.readLong();
        this.mDumpCount = in.readInt();
    }

    public void setScrollRect(Rect rect) {
        this.mScrollRect.set(rect);
    }

    public void setScrollComponent(ColorLongshotComponentName component) {
        this.mComponent = component;
    }

    public void setSpendDump(long spendDump) {
        this.mSpendDump = spendDump;
    }

    public void setSpendCalc(long spendCalc) {
        this.mSpendCalc = spendCalc;
    }

    public void setSpendPack(long spendPack) {
        this.mSpendPack = spendPack;
    }

    public void setDumpCount(int dumpCount) {
        this.mDumpCount = dumpCount;
    }

    public void setScrollCount(int scrollCount) {
        this.mScrollCount = scrollCount;
    }

    public long getTotalSpend() {
        return (this.mSpendDump + this.mSpendCalc) + this.mSpendPack;
    }

    public int getDumpCount() {
        return this.mDumpCount;
    }

    private void writeComponent(Parcel out, int flags) {
        if (this.mComponent != null) {
            out.writeInt(1);
            this.mComponent.writeToParcel(out, flags);
            return;
        }
        out.writeInt(0);
    }

    private void readComponent(Parcel in) {
        if (1 == in.readInt()) {
            this.mComponent.readFromParcel(in);
        }
    }
}
