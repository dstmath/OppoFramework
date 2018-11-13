package com.suntek.mway.rcs.client.aidl.plugin.entity.pubacct;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.LinkedList;
import java.util.List;

public class MenuInfo implements Parcelable {
    public static final Creator<MenuInfo> CREATOR = new Creator<MenuInfo>() {
        public MenuInfo createFromParcel(Parcel source) {
            return new MenuInfo(source);
        }

        public MenuInfo[] newArray(int size) {
            return new MenuInfo[size];
        }
    };
    private String commandId;
    private int priority;
    private List<MenuInfo> subMenuList;
    private String title;
    private int type;

    public MenuInfo(Parcel source) {
        readFromParcel(source);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.commandId);
        dest.writeString(this.title);
        dest.writeInt(this.type);
        dest.writeInt(this.priority);
        dest.writeList(this.subMenuList);
    }

    public void readFromParcel(Parcel source) {
        this.commandId = source.readString();
        this.title = source.readString();
        this.type = source.readInt();
        this.priority = source.readInt();
        this.subMenuList = new LinkedList();
        source.readList(this.subMenuList, getClass().getClassLoader());
    }

    public String getCommandId() {
        return this.commandId;
    }

    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public List<MenuInfo> getSubMenuList() {
        return this.subMenuList;
    }

    public void setSubMenuList(List<MenuInfo> subMenuList) {
        this.subMenuList = subMenuList;
    }

    public String toString() {
        StringBuffer sbuffer = new StringBuffer();
        sbuffer.append("commandId=").append(this.commandId).append(",title=").append(this.title).append(",type=").append(this.type).append(",priority=").append(this.priority);
        if (this.subMenuList != null && this.subMenuList.size() > 0) {
            sbuffer.append(",subMenuList=").append("[");
            for (MenuInfo menu : this.subMenuList) {
                sbuffer.append(menu.toString());
            }
            sbuffer.append("]");
        }
        return sbuffer.toString();
    }
}
