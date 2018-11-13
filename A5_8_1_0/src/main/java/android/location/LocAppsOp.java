package android.location;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.HashMap;

public class LocAppsOp implements Parcelable {
    public static final int ALLOW = 1;
    public static final int CMD_REMOVE = 0;
    public static final int CMD_UPDATE = 1;
    public static final Creator<LocAppsOp> CREATOR = new Creator<LocAppsOp>() {
        public LocAppsOp createFromParcel(Parcel source) {
            return new LocAppsOp(source);
        }

        public LocAppsOp[] newArray(int size) {
            return new LocAppsOp[size];
        }
    };
    public static final int FG_ONLY = 2;
    public static final int GET_CFG_OP = 1;
    public static final int GET_OP_LEVEL_ONLY = 3;
    public static final int GET_SET_OP = 2;
    public static final int PROHIBIT = 3;
    private int mOpLevel;
    private HashMap<String, Integer> mOpList;

    public LocAppsOp() {
        this.mOpLevel = Integer.MAX_VALUE;
        this.mOpList = new HashMap();
    }

    public LocAppsOp(Parcel source) {
        this();
        readFromParcel(source);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mOpLevel);
        dest.writeMap(this.mOpList);
    }

    public void readFromParcel(Parcel source) {
        this.mOpLevel = source.readInt();
        source.readMap(this.mOpList, getClass().getClassLoader());
    }

    public int getOpLevel() {
        return this.mOpLevel;
    }

    public void setOpLevel(int opLevel) {
        this.mOpLevel = opLevel;
    }

    public HashMap<String, Integer> getAppsOp() {
        return this.mOpList;
    }

    public void setAppsOp(HashMap<String, Integer> opList) {
        this.mOpList = opList;
    }

    public void setAppOp(String name, int op) {
        this.mOpList.put(name, Integer.valueOf(op));
    }
}
