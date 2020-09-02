package com.color.inner.content.pm;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageParser;
import android.content.pm.Signature;
import android.util.Log;
import java.io.File;

public class PackageParserWrapper {
    private static final String TAG = "PackageParserWrapper";

    public static PackageWrapper parsePackage(File packageFile, int flags) {
        try {
            PackageParser.Package pkg = new PackageParser().parsePackage(packageFile, flags);
            if (pkg != null) {
                return new PackageWrapper(pkg);
            }
        } catch (PackageParser.PackageParserException e) {
            Log.e(TAG, "parsePackage() PackageParserException: " + e);
        } catch (Exception e2) {
            Log.e(TAG, "parsePackage() Exception: " + e2);
        }
        return null;
    }

    public static void collectCertificates(PackageWrapper packageWrapper, boolean skipVerify) {
        if (packageWrapper != null && packageWrapper.mPackage != null) {
            try {
                PackageParser.collectCertificates(packageWrapper.mPackage, skipVerify);
            } catch (PackageParser.PackageParserException e) {
                Log.e(TAG, "collectCertificates() PackageParserException: " + e);
            } catch (Exception e2) {
                Log.e(TAG, "collectCertificates() Exception: " + e2);
            }
        }
    }

    public static class PackageWrapper {
        /* access modifiers changed from: private */
        public PackageParser.Package mPackage;

        private PackageWrapper(PackageParser.Package pkg) {
            this.mPackage = pkg;
        }

        public Signature[] getSignatures() {
            PackageParser.Package packageR = this.mPackage;
            if (packageR == null || packageR.mSigningDetails == null) {
                return null;
            }
            return this.mPackage.mSigningDetails.signatures;
        }

        public String getVersionName() {
            try {
                if (this.mPackage != null) {
                    return this.mPackage.mVersionName;
                }
                return null;
            } catch (Throwable e) {
                Log.e(PackageParserWrapper.TAG, e.toString());
                return null;
            }
        }

        public int getVersionCode() {
            try {
                if (this.mPackage != null) {
                    return this.mPackage.mVersionCode;
                }
                return -1;
            } catch (Throwable e) {
                Log.e(PackageParserWrapper.TAG, e.toString());
                return -1;
            }
        }

        public String getPackageName() {
            try {
                if (this.mPackage != null) {
                    return this.mPackage.packageName;
                }
                return null;
            } catch (Throwable e) {
                Log.e(PackageParserWrapper.TAG, e.toString());
                return null;
            }
        }

        public ApplicationInfo getApplicationInfo() {
            try {
                if (this.mPackage != null) {
                    return this.mPackage.applicationInfo;
                }
                return null;
            } catch (Throwable e) {
                Log.e(PackageParserWrapper.TAG, e.toString());
                return null;
            }
        }
    }
}
