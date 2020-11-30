package com.color.util;

import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.SmsManager;
import java.util.ArrayList;
import java.util.List;

public final class ColorProcDependData implements Parcelable {
    public static final Parcelable.Creator<ColorProcDependData> CREATOR = new Parcelable.Creator<ColorProcDependData>() {
        /* class com.color.util.ColorProcDependData.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ColorProcDependData createFromParcel(Parcel in) {
            return new ColorProcDependData(in);
        }

        @Override // android.os.Parcelable.Creator
        public ColorProcDependData[] newArray(int size) {
            return new ColorProcDependData[size];
        }
    };
    public List<ProcItem> mClients = new ArrayList();
    public String mPackageName = null;
    public int mPid = -1;
    public String mProcessName = null;
    public List<ProcItem> mServices = new ArrayList();
    public int mUid = -1;

    public ColorProcDependData() {
    }

    public ColorProcDependData(Parcel in) {
        readFromParcel(in);
    }

    public String toString() {
        return "pid=" + this.mPid + SmsManager.REGEX_PREFIX_DELIMITER + "uid=" + this.mUid + SmsManager.REGEX_PREFIX_DELIMITER + "pkg=" + this.mPackageName + SmsManager.REGEX_PREFIX_DELIMITER + "proc=" + this.mProcessName + SmsManager.REGEX_PREFIX_DELIMITER + "depend ON=" + this.mServices + SmsManager.REGEX_PREFIX_DELIMITER + "depend BY=" + this.mClients + SmsManager.REGEX_PREFIX_DELIMITER;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mPid);
        out.writeInt(this.mUid);
        out.writeString(this.mProcessName);
        out.writeString(this.mPackageName);
        out.writeTypedList(this.mServices);
        out.writeTypedList(this.mClients);
    }

    public void readFromParcel(Parcel in) {
        this.mPid = in.readInt();
        this.mUid = in.readInt();
        this.mProcessName = in.readString();
        this.mPackageName = in.readString();
        this.mServices = new ArrayList();
        in.readTypedList(this.mServices, ProcItem.CREATOR);
        this.mClients = new ArrayList();
        in.readTypedList(this.mClients, ProcItem.CREATOR);
    }

    public static final class ProcItem implements Parcelable {
        public static final Parcelable.Creator<ProcItem> CREATOR = new Parcelable.Creator<ProcItem>() {
            /* class com.color.util.ColorProcDependData.ProcItem.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public ProcItem createFromParcel(Parcel in) {
                return new ProcItem(in);
            }

            @Override // android.os.Parcelable.Creator
            public ProcItem[] newArray(int size) {
                return new ProcItem[size];
            }
        };
        public String packageName = null;
        public int pid = -1;
        public String processName = null;
        public int uid = -1;

        public ProcItem() {
        }

        public ProcItem(int uid2, int pid2, String packageName2, String processName2) {
            this.pid = pid2;
            this.uid = uid2;
            this.processName = processName2;
            this.packageName = packageName2;
        }

        public ProcItem(Parcel in) {
            readFromParcel(in);
        }

        public String toString() {
            return this.pid + "+" + this.uid + "+" + this.packageName + "+" + this.processName;
        }

        public boolean equals(Object obj) {
            String str;
            if (obj == null || !(obj instanceof ProcItem)) {
                return false;
            }
            if (this.pid == ((ProcItem) obj).pid && this.uid == ((ProcItem) obj).uid) {
                return true;
            }
            String str2 = this.packageName;
            if (str2 == null || !str2.equals(((ProcItem) obj).packageName) || (str = this.processName) == null || !str.equals(((ProcItem) obj).processName)) {
                return false;
            }
            return true;
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(this.pid);
            out.writeInt(this.uid);
            out.writeString(this.processName);
            out.writeString(this.packageName);
        }

        public void readFromParcel(Parcel in) {
            this.pid = in.readInt();
            this.uid = in.readInt();
            this.processName = in.readString();
            this.packageName = in.readString();
        }
    }
}
