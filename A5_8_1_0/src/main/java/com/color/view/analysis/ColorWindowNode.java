package com.color.view.analysis;

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemClock;
import android.provider.SettingsStringUtil;
import android.view.ColorLongshotViewContent;
import android.view.ColorLongshotViewUtils;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;

public final class ColorWindowNode implements Parcelable {
    public static final Creator<ColorWindowNode> CREATOR = new Creator<ColorWindowNode>() {
        public ColorWindowNode createFromParcel(Parcel in) {
            return new ColorWindowNode(in);
        }

        public ColorWindowNode[] newArray(int size) {
            return new ColorWindowNode[size];
        }
    };
    private String mClassName = null;
    private final Rect mCoverRect = new Rect(null);
    private final Rect mDecorRect = new Rect(null);
    private boolean mIsNavigationBar = false;
    private boolean mIsStatusBar = false;
    private String mPackageName = null;
    private int mSurfaceLayer = 0;
    private final Rect mTempRect = new Rect();
    private long mTimeSpend = 0;

    public ColorWindowNode(View view, boolean isStatusBar, boolean isNavigationBar) {
        long timeStart = SystemClock.uptimeMillis();
        view.getBoundsOnScreen(this.mDecorRect, true);
        if (view instanceof ViewGroup) {
            List<ColorLongshotViewContent> coverContents = new ArrayList();
            new ColorLongshotViewUtils(view.getContext()).findCoverRect(1, (ViewGroup) view, null, coverContents, null, null, null, true);
            for (ColorLongshotViewContent coverContent : coverContents) {
                coverContent.getView().getBoundsOnScreen(this.mTempRect, true);
                this.mCoverRect.union(this.mTempRect);
            }
        }
        if (this.mCoverRect.isEmpty()) {
            this.mCoverRect.set(this.mDecorRect);
        }
        this.mPackageName = view.getContext().getPackageName();
        this.mClassName = view.getClass().getName();
        this.mTimeSpend = SystemClock.uptimeMillis() - timeStart;
        this.mIsStatusBar = isStatusBar;
        this.mIsNavigationBar = isNavigationBar;
    }

    public ColorWindowNode(Parcel in) {
        readFromParcel(in);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Window[");
        if (this.mIsStatusBar) {
            sb.append("StatusBar][");
        } else if (this.mIsNavigationBar) {
            sb.append("NavigationBar][");
        }
        if (this.mPackageName != null) {
            sb.append("package=");
            sb.append(this.mPackageName.toString());
            sb.append(SettingsStringUtil.DELIMITER);
        }
        if (this.mClassName != null) {
            sb.append("class=");
            sb.append(this.mClassName.toString());
            sb.append(SettingsStringUtil.DELIMITER);
        }
        sb.append("decor=");
        sb.append(this.mDecorRect);
        sb.append("cover=");
        sb.append(this.mCoverRect);
        sb.append(":spend=");
        sb.append(this.mTimeSpend);
        sb.append("]");
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        int i;
        int i2 = 1;
        this.mDecorRect.writeToParcel(out, flags);
        this.mCoverRect.writeToParcel(out, flags);
        writeString(out, this.mPackageName);
        writeString(out, this.mClassName);
        out.writeLong(this.mTimeSpend);
        out.writeInt(this.mSurfaceLayer);
        if (this.mIsStatusBar) {
            i = 1;
        } else {
            i = 0;
        }
        out.writeInt(i);
        if (!this.mIsNavigationBar) {
            i2 = 0;
        }
        out.writeInt(i2);
    }

    public void readFromParcel(Parcel in) {
        boolean z;
        boolean z2 = true;
        this.mDecorRect.readFromParcel(in);
        this.mCoverRect.readFromParcel(in);
        this.mPackageName = readString(in);
        this.mClassName = readString(in);
        this.mTimeSpend = in.readLong();
        this.mSurfaceLayer = in.readInt();
        if (1 == in.readInt()) {
            z = true;
        } else {
            z = false;
        }
        this.mIsStatusBar = z;
        if (1 != in.readInt()) {
            z2 = false;
        }
        this.mIsNavigationBar = z2;
    }

    public Rect getDecorRect() {
        return this.mDecorRect;
    }

    public Rect getCoverRect() {
        return this.mCoverRect;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public String getClassName() {
        return this.mClassName;
    }

    public int getSurfaceLayer() {
        return this.mSurfaceLayer;
    }

    public void setSurfaceLayer(int surfaceLayer) {
        this.mSurfaceLayer = surfaceLayer;
    }

    public boolean isStatusBar() {
        return this.mIsStatusBar;
    }

    public void setStatusBar(boolean value) {
        this.mIsStatusBar = value;
    }

    public boolean isNavigationBar() {
        return this.mIsNavigationBar;
    }

    public void setNavigationBar(boolean value) {
        this.mIsNavigationBar = value;
    }

    private void writeString(Parcel out, String s) {
        if (s != null) {
            out.writeInt(1);
            out.writeString(s);
            return;
        }
        out.writeInt(0);
    }

    private String readString(Parcel in) {
        if (1 == in.readInt()) {
            return in.readString();
        }
        return null;
    }
}
