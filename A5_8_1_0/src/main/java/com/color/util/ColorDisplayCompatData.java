package com.color.util;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class ColorDisplayCompatData implements Parcelable {
    public static final Creator<ColorDisplayCompatData> CREATOR = new Creator<ColorDisplayCompatData>() {
        public ColorDisplayCompatData createFromParcel(Parcel in) {
            return new ColorDisplayCompatData(in);
        }

        public ColorDisplayCompatData[] newArray(int size) {
            return new ColorDisplayCompatData[size];
        }
    };
    private static final boolean DBG = false;
    private static final String TAG = "ColorDisplayCompatData";
    private List<String> mBlackList = new ArrayList();
    private HashMap<String, String> mCompatPackageList = new HashMap();
    private boolean mEnableDisplayCompat = true;
    private boolean mHasHeteromorphismFeature = false;
    private List<String> mInstalledCompatList = new ArrayList();
    private List<String> mInstalledImeList = new ArrayList();
    private List<String> mInstalledThirdPartyAppList = new ArrayList();
    private List<String> mLocalCompatList = new ArrayList();
    private List<String> mLocalFullScreenList = new ArrayList();
    private List<String> mLocalImmersiveList = new ArrayList();
    private List<String> mLocalNonImmersiveList = new ArrayList();
    private List<String> mNeedAdjustSizeList = new ArrayList();
    private boolean mRusImmersiveDefault = true;
    private List<String> mRusImmersiveList = new ArrayList();
    private List<String> mRusNonImmersiveList = new ArrayList();
    private List<String> mShowDialogAppsList = new ArrayList();
    private List<String> mWhiteList = new ArrayList();

    public ColorDisplayCompatData(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        int i;
        int i2 = 1;
        out.writeStringList(this.mWhiteList);
        out.writeStringList(this.mBlackList);
        out.writeStringList(this.mRusImmersiveList);
        out.writeStringList(this.mRusNonImmersiveList);
        out.writeStringList(this.mLocalCompatList);
        out.writeStringList(this.mLocalFullScreenList);
        out.writeStringList(this.mLocalNonImmersiveList);
        out.writeStringList(this.mLocalImmersiveList);
        out.writeStringList(this.mInstalledCompatList);
        out.writeStringList(this.mInstalledImeList);
        out.writeStringList(this.mInstalledThirdPartyAppList);
        out.writeStringList(this.mShowDialogAppsList);
        out.writeStringList(this.mNeedAdjustSizeList);
        out.writeMap(this.mCompatPackageList);
        if (this.mEnableDisplayCompat) {
            i = 1;
        } else {
            i = 0;
        }
        out.writeByte((byte) i);
        if (this.mHasHeteromorphismFeature) {
            i = 1;
        } else {
            i = 0;
        }
        out.writeByte((byte) i);
        if (!this.mRusImmersiveDefault) {
            i2 = 0;
        }
        out.writeByte((byte) i2);
    }

    public void readFromParcel(Parcel in) {
        boolean z;
        boolean z2 = true;
        this.mWhiteList = in.createStringArrayList();
        this.mBlackList = in.createStringArrayList();
        this.mRusImmersiveList = in.createStringArrayList();
        this.mRusNonImmersiveList = in.createStringArrayList();
        this.mLocalCompatList = in.createStringArrayList();
        this.mLocalFullScreenList = in.createStringArrayList();
        this.mLocalNonImmersiveList = in.createStringArrayList();
        this.mLocalImmersiveList = in.createStringArrayList();
        this.mInstalledCompatList = in.createStringArrayList();
        this.mInstalledImeList = in.createStringArrayList();
        this.mInstalledThirdPartyAppList = in.createStringArrayList();
        this.mShowDialogAppsList = in.createStringArrayList();
        this.mNeedAdjustSizeList = in.createStringArrayList();
        this.mCompatPackageList = in.readHashMap(HashMap.class.getClassLoader());
        if (in.readByte() != (byte) 0) {
            z = true;
        } else {
            z = false;
        }
        this.mEnableDisplayCompat = z;
        if (in.readByte() != (byte) 0) {
            z = true;
        } else {
            z = false;
        }
        this.mHasHeteromorphismFeature = z;
        if (in.readByte() == (byte) 0) {
            z2 = false;
        }
        this.mRusImmersiveDefault = z2;
    }

    public HashMap<String, String> getCompatPackageList() {
        return this.mCompatPackageList;
    }

    public void setCompatPackageList(HashMap<String, String> compatPackageList) {
        this.mCompatPackageList = compatPackageList;
    }

    public boolean getDisplayCompatEnabled() {
        return this.mEnableDisplayCompat;
    }

    public void setDisplatOptEnabled(boolean enabled) {
        this.mEnableDisplayCompat = enabled;
    }

    public boolean hasHeteromorphismFeature() {
        return this.mHasHeteromorphismFeature;
    }

    public void setHasHeteromorphismFeature(boolean hasFeature) {
        this.mHasHeteromorphismFeature = hasFeature;
    }

    public List<String> getWhiteList() {
        return this.mWhiteList;
    }

    public void setWhiteList(List<String> whiteList) {
        this.mWhiteList = whiteList;
    }

    public List<String> getBlackList() {
        return this.mBlackList;
    }

    public void setBlackList(List<String> blackList) {
        this.mBlackList = blackList;
    }

    public List<String> getLocalCompatList() {
        return this.mLocalCompatList;
    }

    public void setLocalCompatList(List<String> localList) {
        this.mLocalCompatList = localList;
    }

    public List<String> getLocalFullScreenList() {
        return this.mLocalFullScreenList;
    }

    public void setLocalFullScreenList(List<String> localList) {
        this.mLocalFullScreenList = localList;
    }

    public List<String> getLocalNonImmersiveList() {
        return this.mLocalNonImmersiveList;
    }

    public void setLocalNonImmersiveList(List<String> localList) {
        this.mLocalNonImmersiveList = localList;
    }

    public List<String> getLocalImmersiveList() {
        return this.mLocalImmersiveList;
    }

    public void setLocalImmersiveList(List<String> localList) {
        this.mLocalImmersiveList = localList;
    }

    public List<String> getInstalledCompatList() {
        return this.mInstalledCompatList;
    }

    public void setInstalledCompatList(List<String> installedList) {
        this.mInstalledCompatList = installedList;
    }

    public List<String> getInstalledImeList() {
        return this.mInstalledImeList;
    }

    public void setInstalledImeList(List<String> imeList) {
        this.mInstalledImeList = imeList;
    }

    public List<String> getShowDialogAppList() {
        return this.mShowDialogAppsList;
    }

    public void setShowDialogAppList(List<String> localList) {
        this.mShowDialogAppsList = localList;
    }

    public boolean getRusImmersiveDefault() {
        return this.mRusImmersiveDefault;
    }

    public void setRusImmersiveDefault(boolean defaultValue) {
        this.mRusImmersiveDefault = defaultValue;
    }

    public List<String> getRusImmersiveList() {
        return this.mRusImmersiveList;
    }

    public void setRusImmersiveList(List<String> list) {
        this.mRusImmersiveList = list;
    }

    public List<String> getRusNonImmersiveList() {
        return this.mRusNonImmersiveList;
    }

    public void setRusNonImmersiveList(List<String> list) {
        this.mRusNonImmersiveList = list;
    }

    public List<String> getInstalledThirdPartyAppList() {
        return this.mInstalledThirdPartyAppList;
    }

    public void setInstalledThirdPartyAppList(List<String> list) {
        this.mInstalledThirdPartyAppList = list;
    }

    public List<String> getNeedAdjustSizeAppList() {
        return this.mNeedAdjustSizeList;
    }

    public void setNeedAdjustSizeAppList(List<String> list) {
        this.mNeedAdjustSizeList = list;
    }
}
