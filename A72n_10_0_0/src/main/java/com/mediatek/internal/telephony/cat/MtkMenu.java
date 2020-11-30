package com.mediatek.internal.telephony.cat;

import android.os.Parcel;
import android.os.Parcelable;
import com.android.internal.telephony.cat.Menu;

public class MtkMenu extends Menu {
    public static final Parcelable.Creator<MtkMenu> CREATOR = new Parcelable.Creator<MtkMenu>() {
        /* class com.mediatek.internal.telephony.cat.MtkMenu.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MtkMenu createFromParcel(Parcel in) {
            return new MtkMenu(in);
        }

        @Override // android.os.Parcelable.Creator
        public MtkMenu[] newArray(int size) {
            return new MtkMenu[size];
        }
    };
    public int mFromMD;
    public byte[] nextActionIndicator;

    public MtkMenu() {
        this.nextActionIndicator = null;
        this.mFromMD = 0;
    }

    private MtkMenu(Parcel in) {
        super(in);
        this.mFromMD = in.readInt();
        int naiLen = in.readInt();
        if (naiLen <= 0) {
            this.nextActionIndicator = null;
        } else {
            this.nextActionIndicator = new byte[naiLen];
            in.readByteArray(this.nextActionIndicator);
        }
        MtkCatLog.d("[MtkMenu]", "Menu: " + this.mFromMD);
    }

    public void writeToParcel(Parcel dest, int flags) {
        MtkMenu.super.writeToParcel(dest, flags);
        dest.writeInt(this.mFromMD);
        byte[] bArr = this.nextActionIndicator;
        dest.writeInt(bArr == null ? -1 : bArr.length);
        byte[] bArr2 = this.nextActionIndicator;
        if (bArr2 != null && bArr2.length > 0) {
            dest.writeByteArray(bArr2);
        }
        MtkCatLog.w("[MtkMenu]", "writeToParcel: " + this.mFromMD);
    }

    public int getSetUpMenuFlag() {
        return this.mFromMD;
    }

    public void setSetUpMenuFlag(int FromMD) {
        this.mFromMD = FromMD;
    }
}
