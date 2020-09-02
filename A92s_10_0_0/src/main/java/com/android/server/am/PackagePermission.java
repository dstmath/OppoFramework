package com.android.server.am;

public class PackagePermission {
    long mAccept;
    int mId;
    String mPackageName;
    long mPrompt;
    long mReject;
    int mTrust;

    public PackagePermission copy() {
        PackagePermission copy = new PackagePermission();
        copy.mId = this.mId;
        copy.mPackageName = this.mPackageName;
        copy.mAccept = this.mAccept;
        copy.mReject = this.mReject;
        copy.mPrompt = this.mPrompt;
        copy.mTrust = this.mTrust;
        return copy;
    }

    public String toString() {
        return "[mPackageName=" + this.mPackageName + ", mAccept=" + this.mAccept + ", mReject=" + this.mReject + ", mPrompt=" + this.mPrompt + ", mTrust=" + this.mTrust + "]";
    }
}
