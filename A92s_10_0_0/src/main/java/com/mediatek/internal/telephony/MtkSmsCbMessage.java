package com.mediatek.internal.telephony;

import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.SmsCbCmasInfo;
import android.telephony.SmsCbEtwsInfo;
import android.telephony.SmsCbLocation;
import android.telephony.SmsCbMessage;
import java.util.Arrays;

public class MtkSmsCbMessage extends SmsCbMessage {
    public static final Parcelable.Creator<MtkSmsCbMessage> CREATOR = new Parcelable.Creator<MtkSmsCbMessage>() {
        /* class com.mediatek.internal.telephony.MtkSmsCbMessage.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MtkSmsCbMessage createFromParcel(Parcel in) {
            return new MtkSmsCbMessage(in);
        }

        @Override // android.os.Parcelable.Creator
        public MtkSmsCbMessage[] newArray(int size) {
            return new MtkSmsCbMessage[size];
        }
    };
    private int mMaxWaitTime;
    private final byte[] mWac;

    public MtkSmsCbMessage(int messageFormat, int geographicalScope, int serialNumber, SmsCbLocation location, int serviceCategory, String language, String body, int priority, SmsCbEtwsInfo etwsWarningInfo, SmsCbCmasInfo cmasWarningInfo, byte[] wac) {
        super(messageFormat, geographicalScope, serialNumber, location, serviceCategory, language, body, priority, etwsWarningInfo, cmasWarningInfo);
        if (wac != null) {
            this.mWac = Arrays.copyOf(wac, wac.length);
        } else {
            this.mWac = null;
        }
        this.mMaxWaitTime = 255;
    }

    public MtkSmsCbMessage(Parcel in) {
        super(in);
        this.mWac = in.readBlob();
        this.mMaxWaitTime = 255;
    }

    public void writeToParcel(Parcel dest, int flags) {
        MtkSmsCbMessage.super.writeToParcel(dest, flags);
        dest.writeBlob(this.mWac);
    }

    public String toString() {
        return "MtkSmsCbMessage {" + MtkSmsCbMessage.super.toString() + ", wac=" + this.mWac + '}';
    }

    public byte[] getWac() {
        return this.mWac;
    }

    public int getMaxWaitTime() {
        return this.mMaxWaitTime;
    }

    public void setMaxWaitTime(int time) {
        this.mMaxWaitTime = time;
    }
}
