package com.color.util;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;

public class ColorReflectData implements Parcelable {
    public static final Creator<ColorReflectData> CREATOR = new Creator<ColorReflectData>() {
        public ColorReflectData createFromParcel(Parcel source) {
            return new ColorReflectData(source);
        }

        public ColorReflectData[] newArray(int size) {
            return new ColorReflectData[size];
        }
    };
    private static final ArrayList<ColorReflectWidget> mLocalReflectList = new ArrayList();
    private ArrayList<ColorReflectWidget> mReflectAppList = new ArrayList();
    private boolean mReflectEnable = true;
    private ArrayList<ColorReflectWidget> mReflectList = new ArrayList();

    static {
        mLocalReflectList.add(ColorReflectWidget.DEFAULT_WIDGET);
    }

    protected ColorReflectData(Parcel in) {
        boolean z = true;
        if (in.readByte() == (byte) 0) {
            z = false;
        }
        this.mReflectEnable = z;
        this.mReflectList = in.createTypedArrayList(ColorReflectWidget.CREATOR);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.mReflectEnable ? (byte) 1 : (byte) 0);
        dest.writeTypedList(this.mReflectList);
    }

    public boolean isReflectEnable() {
        return this.mReflectEnable;
    }

    public void setReflectEnable(boolean enable) {
        this.mReflectEnable = enable;
    }

    public ArrayList<ColorReflectWidget> getReflectList() {
        return this.mReflectList;
    }

    public void setReflectList(ArrayList<ColorReflectWidget> reflectList) {
        this.mReflectList = reflectList;
    }

    public void clearList() {
        if (this.mReflectList != null) {
            this.mReflectList.clear();
        }
    }

    public void addReflectWidget(ColorReflectWidget widget) {
        if (this.mReflectList == null) {
            this.mReflectList = new ArrayList();
        }
        this.mReflectList.add(widget);
    }

    public String toString() {
        return "ColorReflectData{mReflectEnable=" + this.mReflectEnable + ", mReflectList=" + this.mReflectList + '}';
    }

    private ColorReflectWidget findWidgetImpl(ArrayList<ColorReflectWidget> list, Context context, String appName, String className) {
        for (int i = 0; i < list.size(); i++) {
            ColorReflectWidget widget = (ColorReflectWidget) list.get(i);
            if (className.equals(widget.getClassName())) {
                return widget;
            }
        }
        return null;
    }

    public ColorReflectWidget findWidget(Context context, String appName, String className) {
        if (this.mReflectAppList == null || this.mReflectAppList.size() < 1) {
            return null;
        }
        return findWidgetImpl(this.mReflectAppList, context, appName, className);
    }

    public void initList(String appName, int version) {
        this.mReflectAppList.clear();
        if (this.mReflectList == null || this.mReflectList.size() < 1) {
            initAppWidgetImpl(mLocalReflectList, this.mReflectAppList, appName, version);
        } else {
            initAppWidgetImpl(this.mReflectList, this.mReflectAppList, appName, version);
        }
    }

    private void initAppWidgetImpl(ArrayList<ColorReflectWidget> totalList, ArrayList<ColorReflectWidget> appList, String appName, int version) {
        for (int i = 0; i < totalList.size(); i++) {
            ColorReflectWidget widget = (ColorReflectWidget) totalList.get(i);
            if (appName.equals(widget.getPackageName()) && version >= widget.getVersionCode()) {
                appList.add(widget);
            }
        }
        if (appList.size() < 1) {
            setReflectEnable(false);
        }
    }
}
