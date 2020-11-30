package com.color.app;

import android.content.res.OppoThemeResources;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;
import java.util.HashSet;
import java.util.List;

public final class ColorAppSwitchConfig implements Parcelable {
    public static final Parcelable.Creator<ColorAppSwitchConfig> CREATOR = new Parcelable.Creator<ColorAppSwitchConfig>() {
        /* class com.color.app.ColorAppSwitchConfig.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ColorAppSwitchConfig createFromParcel(Parcel source) {
            return new ColorAppSwitchConfig(source);
        }

        @Override // android.os.Parcelable.Creator
        public ColorAppSwitchConfig[] newArray(int size) {
            return new ColorAppSwitchConfig[size];
        }
    };
    public static final int TYPE_ACTIVITY = 1;
    public static final int TYPE_PACKAGE = 2;
    public HashSet<String> mActivitySet = new HashSet<>();
    private SparseArray<List<String>> mConfigs = new SparseArray<>();
    public HashSet<String> mPackageSet = new HashSet<>();
    public int observerFingerPrint;

    public ColorAppSwitchConfig() {
    }

    public ColorAppSwitchConfig(Parcel source) {
        this.mConfigs = source.readSparseArray(null);
        if (this.mConfigs == null) {
            this.mConfigs = new SparseArray<>();
        }
        initSearchSet(1);
        initSearchSet(2);
        this.observerFingerPrint = source.readInt();
    }

    private void initSearchSet(int type) {
        List<String> configList = this.mConfigs.get(type);
        if (type == 1) {
            this.mActivitySet.clear();
            if (configList != null && configList.size() != 0) {
                this.mActivitySet.addAll(configList);
            }
        } else if (type == 2) {
            this.mPackageSet.clear();
            if (configList != null && configList.size() != 0) {
                this.mPackageSet.addAll(configList);
            }
        }
    }

    public List<String> getConfigs(int type) {
        return this.mConfigs.get(type);
    }

    public void addAppConfig(int type, List<String> list) {
        this.mConfigs.put(type, list);
        initSearchSet(type);
    }

    public void removeAppConfig(int type) {
        this.mConfigs.remove(type);
        initSearchSet(type);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSparseArray(this.mConfigs);
        dest.writeInt(this.observerFingerPrint);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("ColorAppSwitchConfig = { ");
        String pkg = ("" + this.mConfigs).replace(".", "@@").replace("com", "TOM").replace("coloros", "CO").replace("nearme", "NM").replace(OppoThemeResources.OPPO_PACKAGE, "OP");
        sb.append(" mConfigs = " + pkg);
        sb.append(" observerFingerPrint = " + this.observerFingerPrint);
        sb.append("}");
        return sb.toString();
    }
}
