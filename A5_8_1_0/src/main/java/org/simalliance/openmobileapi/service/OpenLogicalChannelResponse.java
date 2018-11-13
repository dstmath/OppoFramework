package org.simalliance.openmobileapi.service;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class OpenLogicalChannelResponse implements Parcelable {
    public static final Creator<OpenLogicalChannelResponse> CREATOR = new Creator<OpenLogicalChannelResponse>() {
        public OpenLogicalChannelResponse createFromParcel(Parcel in) {
            return new OpenLogicalChannelResponse(in, null);
        }

        public OpenLogicalChannelResponse[] newArray(int size) {
            return new OpenLogicalChannelResponse[size];
        }
    };
    private int mChannelNumber;
    private byte[] mSelectResponse;

    public OpenLogicalChannelResponse(int channelNumber, byte[] selectResponse) {
        this.mChannelNumber = channelNumber;
        this.mSelectResponse = selectResponse;
    }

    private OpenLogicalChannelResponse(Parcel in) {
        this.mChannelNumber = in.readInt();
        this.mSelectResponse = in.createByteArray();
    }

    public int getChannel() {
        return this.mChannelNumber;
    }

    public byte[] getSelectResponse() {
        return this.mSelectResponse;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mChannelNumber);
        out.writeByteArray(this.mSelectResponse);
    }
}
