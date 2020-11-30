package com.color.util;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class ColorDisplayCompatData implements Parcelable {
    public static final Parcelable.Creator<ColorDisplayCompatData> CREATOR = new Parcelable.Creator<ColorDisplayCompatData>() {
        /* class com.color.util.ColorDisplayCompatData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ColorDisplayCompatData createFromParcel(Parcel in) {
            return new ColorDisplayCompatData(in);
        }

        @Override // android.os.Parcelable.Creator
        public ColorDisplayCompatData[] newArray(int size) {
            return new ColorDisplayCompatData[size];
        }
    };
    private static final boolean DBG = false;
    private static final String TAG = "ColorDisplayCompatData";
    private List<String> mBlackList = new ArrayList();
    private HashMap<String, String> mCompatPackageList = new HashMap<>();
    private List<String> mCutoutLeftBlackList = new ArrayList();
    private int mDisplayCutoutType = 0;
    private boolean mEnableDisplayCompat = true;
    private boolean mHasHeteromorphismFeature = false;
    private List<String> mInstalledCompatList = new ArrayList();
    private List<String> mInstalledImeList = new ArrayList();
    private List<String> mInstalledThirdPartyAppList = new ArrayList();
    private List<String> mLocalCompatList = new ArrayList();
    private List<String> mLocalCutoutDefaultList = new ArrayList();
    private List<String> mLocalCutoutHideList = new ArrayList();
    private List<String> mLocalCutoutShowList = new ArrayList();
    private List<String> mLocalFullScreenList = new ArrayList();
    private List<String> mNeedAdjustSizeList = new ArrayList();
    private boolean mRusImmersiveDefault = false;
    private List<String> mRusImmersiveList = new ArrayList();
    private List<String> mRusNonImmersiveList = new ArrayList();
    private List<String> mShowDialogAppsList = new ArrayList();
    private List<String> mWhiteList = new ArrayList();

    public ColorDisplayCompatData() {
    }

    public ColorDisplayCompatData(Parcel in) {
        readFromParcel(in);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeStringList(this.mWhiteList);
        out.writeStringList(this.mBlackList);
        out.writeStringList(this.mRusImmersiveList);
        out.writeStringList(this.mRusNonImmersiveList);
        out.writeStringList(this.mLocalCompatList);
        out.writeStringList(this.mLocalFullScreenList);
        out.writeStringList(this.mLocalCutoutDefaultList);
        out.writeStringList(this.mLocalCutoutShowList);
        out.writeStringList(this.mLocalCutoutHideList);
        out.writeStringList(this.mInstalledCompatList);
        out.writeStringList(this.mInstalledImeList);
        out.writeStringList(this.mInstalledThirdPartyAppList);
        out.writeStringList(this.mShowDialogAppsList);
        out.writeStringList(this.mNeedAdjustSizeList);
        out.writeMap(this.mCompatPackageList);
        out.writeByte(this.mEnableDisplayCompat ? (byte) 1 : 0);
        out.writeByte(this.mHasHeteromorphismFeature ? (byte) 1 : 0);
        out.writeByte(this.mRusImmersiveDefault ? (byte) 1 : 0);
        out.writeInt(this.mDisplayCutoutType);
    }

    public void readFromParcel(Parcel in) {
        this.mWhiteList = in.createStringArrayList();
        this.mBlackList = in.createStringArrayList();
        this.mRusImmersiveList = in.createStringArrayList();
        this.mRusNonImmersiveList = in.createStringArrayList();
        this.mLocalCompatList = in.createStringArrayList();
        this.mLocalFullScreenList = in.createStringArrayList();
        this.mLocalCutoutDefaultList = in.createStringArrayList();
        this.mLocalCutoutShowList = in.createStringArrayList();
        this.mLocalCutoutHideList = in.createStringArrayList();
        this.mInstalledCompatList = in.createStringArrayList();
        this.mInstalledImeList = in.createStringArrayList();
        this.mInstalledThirdPartyAppList = in.createStringArrayList();
        this.mShowDialogAppsList = in.createStringArrayList();
        this.mNeedAdjustSizeList = in.createStringArrayList();
        this.mCompatPackageList = in.readHashMap(HashMap.class.getClassLoader());
        boolean z = true;
        this.mEnableDisplayCompat = in.readByte() != 0;
        this.mHasHeteromorphismFeature = in.readByte() != 0;
        if (in.readByte() == 0) {
            z = false;
        }
        this.mRusImmersiveDefault = z;
        this.mDisplayCutoutType = in.readInt();
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
        return this.mLocalCutoutDefaultList;
    }

    public void setLocalNonImmersiveList(List<String> localList) {
        this.mLocalCutoutDefaultList = localList;
    }

    public List<String> getLocalImmersiveList() {
        return this.mLocalCutoutShowList;
    }

    public void setLocalImmersiveList(List<String> localList) {
        this.mLocalCutoutShowList = localList;
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

    public int getDisplayCutoutType() {
        return this.mDisplayCutoutType;
    }

    public void setDisplayCutoutType(int type) {
        this.mDisplayCutoutType = type;
    }

    public void setLocalCutoutDefaultList(List<String> localList) {
        this.mLocalCutoutDefaultList = localList;
    }

    public List<String> getLocalCutoutDefaultList() {
        return this.mLocalCutoutDefaultList;
    }

    public void setLocalCutoutShowList(List<String> localList) {
        this.mLocalCutoutShowList = localList;
    }

    public List<String> getLocalCutoutShowList() {
        return this.mLocalCutoutShowList;
    }

    public void setLocalCutoutHideList(List<String> localList) {
        this.mLocalCutoutHideList = localList;
    }

    public List<String> getLocalCutoutHideList() {
        return this.mLocalCutoutHideList;
    }
}
