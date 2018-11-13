package com.qualcomm.qti.lpa;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class UimLpaDownloadProgress implements Parcelable {
    public static final Creator<UimLpaDownloadProgress> CREATOR = new Creator<UimLpaDownloadProgress>() {
        public UimLpaDownloadProgress createFromParcel(Parcel in) {
            return new UimLpaDownloadProgress(in);
        }

        public UimLpaDownloadProgress[] newArray(int size) {
            return new UimLpaDownloadProgress[size];
        }
    };
    private int cause;
    private int profilePolicyMask;
    private int progress;
    private int status;
    private boolean userConsent;

    public UimLpaDownloadProgress(int status, int cause, int progress) {
        this.status = status;
        this.cause = cause;
        this.progress = progress;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return this.status;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getProgress() {
        return this.progress;
    }

    public void setCause(int cause) {
        this.cause = cause;
    }

    public int getCause() {
        return this.cause;
    }

    public void setProfilePolicyMask(int policyMask) {
        this.profilePolicyMask = policyMask;
    }

    public int getProfilePolicyMask() {
        return this.profilePolicyMask;
    }

    public void setUserConsent(boolean userOk) {
        this.userConsent = userOk;
    }

    public boolean getUserConsent() {
        return this.userConsent;
    }

    public UimLpaDownloadProgress(Parcel in) {
        boolean z = false;
        this.status = in.readInt();
        this.cause = in.readInt();
        this.progress = in.readInt();
        this.profilePolicyMask = in.readInt();
        if (in.readByte() != (byte) 0) {
            z = true;
        }
        this.userConsent = z;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.status);
        dest.writeInt(this.cause);
        dest.writeInt(this.progress);
        dest.writeInt(this.profilePolicyMask);
        dest.writeByte((byte) (this.userConsent ? 1 : 0));
    }
}
