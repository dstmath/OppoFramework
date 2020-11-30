package com.mediatek.internal.telephony.ims;

import android.os.Parcel;
import android.os.Parcelable;

public class MtkDedicateDataCallResponse implements Parcelable {
    public static final Parcelable.Creator<MtkDedicateDataCallResponse> CREATOR = new Parcelable.Creator<MtkDedicateDataCallResponse>() {
        /* class com.mediatek.internal.telephony.ims.MtkDedicateDataCallResponse.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public MtkDedicateDataCallResponse createFromParcel(Parcel p) {
            return MtkDedicateDataCallResponse.readFrom(p);
        }

        @Override // android.os.Parcelable.Creator
        public MtkDedicateDataCallResponse[] newArray(int size) {
            return new MtkDedicateDataCallResponse[size];
        }
    };
    public static final String REASON_BEARER_ACTIVATION = "activation";
    public static final String REASON_BEARER_DEACTIVATION = "deactivation";
    public static final String REASON_BEARER_MODIFICATION = "modification";
    public int mActive;
    public int mBearerId;
    public int mCid;
    public int mDefaultCid;
    public int mFailCause;
    public int mInterfaceId;
    public MtkQosStatus mMtkQosStatus;
    public MtkTftStatus mMtkTftStatus;
    public String mPcscfAddress;
    public int mSignalingFlag;

    public enum SetupResult {
        SUCCESS,
        FAIL;
        
        public int failCause = 0;

        private SetupResult() {
        }
    }

    public MtkDedicateDataCallResponse(int interfaceId, int defaultId, int cid, int active, int signalingFlag, int bearerId, int faileCause, MtkQosStatus qosStatus, MtkTftStatus tftStatus, String pcscf) {
        this.mInterfaceId = interfaceId;
        this.mDefaultCid = defaultId;
        this.mCid = cid;
        this.mActive = active;
        this.mSignalingFlag = signalingFlag;
        this.mBearerId = bearerId;
        this.mFailCause = faileCause;
        this.mMtkQosStatus = qosStatus;
        this.mMtkTftStatus = tftStatus;
        this.mPcscfAddress = pcscf;
    }

    public static MtkDedicateDataCallResponse readFrom(Parcel p) {
        MtkQosStatus qosStatus;
        MtkTftStatus tftStatus;
        String pcscf;
        int interfaceId = p.readInt();
        int defaultCid = p.readInt();
        int cid = p.readInt();
        int active = p.readInt();
        int signalingFlag = p.readInt();
        int bearerId = p.readInt();
        int failCause = p.readInt();
        if (p.readInt() == 1) {
            qosStatus = MtkQosStatus.readFrom(p);
        } else {
            qosStatus = null;
        }
        if (p.readInt() == 1) {
            tftStatus = MtkTftStatus.readFrom(p);
        } else {
            tftStatus = null;
        }
        if (p.readInt() == 1) {
            pcscf = p.readString();
        } else {
            pcscf = null;
        }
        return new MtkDedicateDataCallResponse(interfaceId, defaultCid, cid, active, signalingFlag, bearerId, failCause, qosStatus, tftStatus, pcscf);
    }

    public String toString() {
        return "[interfaceId=" + this.mInterfaceId + ", defaultCid=" + this.mDefaultCid + ", cid=" + this.mCid + ", active=" + this.mActive + ", signalingFlag=" + this.mSignalingFlag + ", bearerId=" + this.mBearerId + ", failCause=" + this.mFailCause + ", QOS=" + this.mMtkQosStatus + ", TFT=" + this.mMtkTftStatus + ", PCSCF=" + this.mPcscfAddress + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mInterfaceId);
        dest.writeInt(this.mDefaultCid);
        dest.writeInt(this.mCid);
        dest.writeInt(this.mActive);
        dest.writeInt(this.mSignalingFlag);
        dest.writeInt(this.mBearerId);
        dest.writeInt(this.mFailCause);
        int i = 0;
        dest.writeInt(this.mMtkQosStatus == null ? 0 : 1);
        MtkQosStatus mtkQosStatus = this.mMtkQosStatus;
        if (mtkQosStatus != null) {
            mtkQosStatus.writeTo(dest);
        }
        dest.writeInt(this.mMtkTftStatus == null ? 0 : 1);
        MtkTftStatus mtkTftStatus = this.mMtkTftStatus;
        if (mtkTftStatus != null) {
            mtkTftStatus.writeTo(dest);
        }
        if (this.mPcscfAddress != null) {
            i = 1;
        }
        dest.writeInt(i);
        dest.writeString(this.mPcscfAddress);
    }
}
