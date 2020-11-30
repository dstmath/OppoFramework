package com.mediatek.internal.telephony.cat;

import android.os.Parcel;
import android.os.Parcelable;

public class DefaultBearerDesc extends BearerDesc {
    public static final Parcelable.Creator<DefaultBearerDesc> CREATOR = new Parcelable.Creator<DefaultBearerDesc>() {
        /* class com.mediatek.internal.telephony.cat.DefaultBearerDesc.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DefaultBearerDesc createFromParcel(Parcel in) {
            return new DefaultBearerDesc(in);
        }

        @Override // android.os.Parcelable.Creator
        public DefaultBearerDesc[] newArray(int size) {
            return new DefaultBearerDesc[size];
        }
    };

    public DefaultBearerDesc() {
        this.bearerType = 3;
    }

    private DefaultBearerDesc(Parcel in) {
        this.bearerType = in.readInt();
    }

    @Override // com.mediatek.internal.telephony.cat.BearerDesc
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.bearerType);
    }
}
