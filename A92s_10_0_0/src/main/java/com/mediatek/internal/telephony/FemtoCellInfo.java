package com.mediatek.internal.telephony;

import android.os.Parcel;
import android.os.Parcelable;

public class FemtoCellInfo implements Parcelable {
    public static final Parcelable.Creator<FemtoCellInfo> CREATOR = new Parcelable.Creator<FemtoCellInfo>() {
        /* class com.mediatek.internal.telephony.FemtoCellInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public FemtoCellInfo createFromParcel(Parcel in) {
            return new FemtoCellInfo(in.readInt(), in.readInt(), in.readString(), in.readString(), in.readString(), in.readInt());
        }

        @Override // android.os.Parcelable.Creator
        public FemtoCellInfo[] newArray(int size) {
            return new FemtoCellInfo[size];
        }
    };
    public static final int CSG_ICON_TYPE_ALLOWED = 1;
    public static final int CSG_ICON_TYPE_NOT_ALLOWED = 0;
    public static final int CSG_ICON_TYPE_OPERATOR = 2;
    public static final int CSG_ICON_TYPE_OPERATOR_UNAUTHORIZED = 3;
    private int csgIconType;
    private int csgId;
    private String homeNodeBName;
    private String operatorAlphaLong;
    private String operatorNumeric;
    private int rat = 0;

    public int getCsgId() {
        return this.csgId;
    }

    public int getCsgIconType() {
        return this.csgIconType;
    }

    public String getHomeNodeBName() {
        return this.homeNodeBName;
    }

    public int getCsgRat() {
        return this.rat;
    }

    public String getOperatorNumeric() {
        return this.operatorNumeric;
    }

    public String getOperatorAlphaLong() {
        return this.operatorAlphaLong;
    }

    public FemtoCellInfo(int csgId2, int csgIconType2, String homeNodeBName2, String operatorNumeric2, String operatorAlphaLong2, int rat2) {
        this.csgId = csgId2;
        this.csgIconType = csgIconType2;
        this.homeNodeBName = homeNodeBName2;
        this.operatorNumeric = operatorNumeric2;
        this.operatorAlphaLong = operatorAlphaLong2;
        this.rat = rat2;
    }

    public String toString() {
        return "FemtoCellInfo " + this.csgId + "/" + this.csgIconType + "/" + this.homeNodeBName + "/" + this.operatorNumeric + "/" + this.operatorAlphaLong + "/" + this.rat;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.csgId);
        dest.writeInt(this.csgIconType);
        dest.writeString(this.homeNodeBName);
        dest.writeString(this.operatorNumeric);
        dest.writeString(this.operatorAlphaLong);
        dest.writeInt(this.rat);
    }
}
