package com.suntek.mway.rcs.client.aidl.plugin.entity.pubacct;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.LinkedList;
import java.util.List;

public class MenuInfoMode implements Parcelable {
    public static final Creator<MenuInfoMode> CREATOR = new Creator<MenuInfoMode>() {
        public MenuInfoMode createFromParcel(Parcel source) {
            return new MenuInfoMode(source);
        }

        public MenuInfoMode[] newArray(int size) {
            return new MenuInfoMode[size];
        }
    };
    private List<MenuInfo> menuInfoList;
    private String menutimestamp;
    private String paUuid;

    public MenuInfoMode(Parcel source) {
        readFromParcel(source);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.paUuid);
        dest.writeString(this.menutimestamp);
        dest.writeList(this.menuInfoList);
    }

    public void readFromParcel(Parcel source) {
        this.paUuid = source.readString();
        this.menutimestamp = source.readString();
        this.menuInfoList = new LinkedList();
        source.readList(this.menuInfoList, getClass().getClassLoader());
    }

    public String getPaUuid() {
        return this.paUuid;
    }

    public void setPaUuid(String paUuid) {
        this.paUuid = paUuid;
    }

    public String getMenutimestamp() {
        return this.menutimestamp;
    }

    public void setMenutimestamp(String menutimestamp) {
        this.menutimestamp = menutimestamp;
    }

    public List<MenuInfo> getMenuInfoList() {
        return this.menuInfoList;
    }

    public void setMenuInfoList(List<MenuInfo> menuInfoList) {
        this.menuInfoList = menuInfoList;
    }
}
