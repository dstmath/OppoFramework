package com.color.util;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;

public class ColorReflectData implements Parcelable {
    public static final Parcelable.Creator<ColorReflectData> CREATOR = new Parcelable.Creator<ColorReflectData>() {
        /* class com.color.util.ColorReflectData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ColorReflectData createFromParcel(Parcel source) {
            return new ColorReflectData(source);
        }

        @Override // android.os.Parcelable.Creator
        public ColorReflectData[] newArray(int size) {
            return new ColorReflectData[size];
        }
    };
    private static final ArrayList<ColorReflectWidget> mLocalReflectList = new ArrayList<>();
    private ArrayList<ColorReflectWidget> mReflectAppList = new ArrayList<>();
    private boolean mReflectEnable = true;
    private ArrayList<ColorReflectWidget> mReflectList = new ArrayList<>();

    static {
        mLocalReflectList.add(ColorReflectWidget.DEFAULT_WIDGET);
        mLocalReflectList.add(ColorReflectWidget.DEFAULT_WIDGET_WECHAT_1420);
    }

    public ColorReflectData() {
    }

    protected ColorReflectData(Parcel in) {
        boolean z = true;
        this.mReflectEnable = in.readByte() == 0 ? false : z;
        this.mReflectList = in.createTypedArrayList(ColorReflectWidget.CREATOR);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.mReflectEnable ? (byte) 1 : 0);
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
        ArrayList<ColorReflectWidget> arrayList = this.mReflectList;
        if (arrayList != null) {
            arrayList.clear();
        }
    }

    public void addReflectWidget(ColorReflectWidget widget) {
        if (this.mReflectList == null) {
            this.mReflectList = new ArrayList<>();
        }
        this.mReflectList.add(widget);
    }

    public String toString() {
        return "ColorReflectData{mReflectEnable=" + this.mReflectEnable + ", mReflectList=" + this.mReflectList + '}';
    }

    private ColorReflectWidget findWidgetImpl(ArrayList<ColorReflectWidget> list, Context context, String appName, String className) {
        for (int i = 0; i < list.size(); i++) {
            ColorReflectWidget widget = list.get(i);
            if (className.equals(widget.getClassName())) {
                return widget;
            }
        }
        return null;
    }

    public ColorReflectWidget findWidget(Context context, String appName, String className) {
        ArrayList<ColorReflectWidget> arrayList = this.mReflectAppList;
        if (arrayList == null || arrayList.size() < 1) {
            return null;
        }
        return findWidgetImpl(this.mReflectAppList, context, appName, className);
    }

    public void initList(String appName, int version) {
        this.mReflectAppList.clear();
        ArrayList<ColorReflectWidget> arrayList = this.mReflectList;
        if (arrayList == null || arrayList.size() < 1) {
            initAppWidgetImpl(mLocalReflectList, this.mReflectAppList, appName, version);
        } else {
            initAppWidgetImpl(this.mReflectList, this.mReflectAppList, appName, version);
        }
    }

    private void initAppWidgetImpl(ArrayList<ColorReflectWidget> totalList, ArrayList<ColorReflectWidget> appList, String appName, int version) {
        for (int i = 0; i < totalList.size(); i++) {
            ColorReflectWidget widget = totalList.get(i);
            if (appName.equals(widget.getPackageName()) && version >= widget.getVersionCode()) {
                appList.add(widget);
            }
        }
        if (appList.size() < 1) {
            setReflectEnable(false);
        }
    }
}
