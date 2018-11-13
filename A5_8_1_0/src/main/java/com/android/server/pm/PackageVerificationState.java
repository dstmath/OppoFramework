package com.android.server.pm;

import android.util.SparseBooleanArray;

class PackageVerificationState {
    private final InstallArgs mArgs;
    private boolean mExtendedTimeout = false;
    private boolean mHasOptionalVerifier;
    private boolean mOptionalVerificationComplete;
    private boolean mOptionalVerificationPassed;
    private int mOptionalVerifierUid;
    private boolean mRequiredVerificationComplete;
    private boolean mRequiredVerificationPassed;
    private final int mRequiredVerifierUid;
    private boolean mSufficientVerificationComplete;
    private boolean mSufficientVerificationPassed;
    private final SparseBooleanArray mSufficientVerifierUids = new SparseBooleanArray();

    public PackageVerificationState(int requiredVerifierUid, InstallArgs args) {
        this.mRequiredVerifierUid = requiredVerifierUid;
        this.mArgs = args;
    }

    public InstallArgs getInstallArgs() {
        return this.mArgs;
    }

    public void addSufficientVerifier(int uid) {
        this.mSufficientVerifierUids.put(uid, true);
    }

    public void addOptionalVerifier(int uid) {
        this.mOptionalVerifierUid = uid;
        this.mHasOptionalVerifier = true;
    }

    public boolean setVerifierResponse(int uid, int code) {
        if (uid == this.mRequiredVerifierUid) {
            this.mRequiredVerificationComplete = true;
            switch (code) {
                case 1:
                    break;
                case 2:
                    this.mSufficientVerifierUids.clear();
                    break;
                default:
                    this.mRequiredVerificationPassed = false;
                    break;
            }
            this.mRequiredVerificationPassed = true;
            return true;
        } else if (this.mHasOptionalVerifier && uid == this.mOptionalVerifierUid) {
            this.mOptionalVerificationComplete = true;
            switch (code) {
                case 1:
                    this.mOptionalVerificationPassed = true;
                    break;
                default:
                    this.mOptionalVerificationPassed = false;
                    break;
            }
            return true;
        } else if (!this.mSufficientVerifierUids.get(uid)) {
            return false;
        } else {
            if (code == 1) {
                this.mSufficientVerificationComplete = true;
                this.mSufficientVerificationPassed = true;
            }
            this.mSufficientVerifierUids.delete(uid);
            if (this.mSufficientVerifierUids.size() == 0) {
                this.mSufficientVerificationComplete = true;
            }
            return true;
        }
    }

    public boolean isVerificationComplete() {
        if (this.mRequiredVerifierUid != -1 && (this.mRequiredVerificationComplete ^ 1) != 0) {
            return false;
        }
        if (this.mHasOptionalVerifier && (this.mOptionalVerificationComplete ^ 1) != 0) {
            return false;
        }
        if (this.mSufficientVerifierUids.size() == 0) {
            return true;
        }
        return this.mSufficientVerificationComplete;
    }

    public boolean isInstallAllowed() {
        if (this.mRequiredVerifierUid != -1 && (this.mRequiredVerificationPassed ^ 1) != 0) {
            return false;
        }
        if (this.mHasOptionalVerifier && (this.mOptionalVerificationPassed ^ 1) != 0) {
            return false;
        }
        if (this.mSufficientVerificationComplete) {
            return this.mSufficientVerificationPassed;
        }
        return true;
    }

    public void extendTimeout() {
        if (!this.mExtendedTimeout) {
            this.mExtendedTimeout = true;
        }
    }

    public boolean timeoutExtended() {
        return this.mExtendedTimeout;
    }
}
