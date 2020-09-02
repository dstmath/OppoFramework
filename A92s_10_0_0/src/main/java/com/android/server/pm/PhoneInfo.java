package com.android.server.pm;

import android.util.Slog;

/* compiled from: PackageManagerXmlParse */
class PhoneInfo {
    static final String TAG = "PhoneInfo";
    private String phoneOperator;
    private String phoneProject;
    private String phonelightOS;
    private String phtoneCountry;
    private String phtoneEuexCountry;

    PhoneInfo(String project, String phonelightOS2, String country, String euexCountry, String operator) {
        this.phoneProject = project;
        this.phonelightOS = phonelightOS2;
        this.phtoneCountry = country;
        this.phtoneEuexCountry = euexCountry;
        this.phoneOperator = operator;
    }

    /* access modifiers changed from: package-private */
    public void printPhoneInfo() {
        Slog.d(TAG, "project info:" + this.phoneProject + " phonelightOS info:" + this.phonelightOS + " region info:" + this.phtoneCountry + "euex region info:" + this.phtoneEuexCountry + "operator info:" + this.phoneOperator);
    }

    /* access modifiers changed from: package-private */
    public String getPhoneProject() {
        return this.phoneProject;
    }

    /* access modifiers changed from: package-private */
    public String getPhoneOSInfo() {
        return this.phonelightOS;
    }

    /* access modifiers changed from: package-private */
    public String getPhtoneCountry() {
        return this.phtoneCountry;
    }

    /* access modifiers changed from: package-private */
    public String getPhtoneEuexCountry() {
        return this.phtoneEuexCountry;
    }

    /* access modifiers changed from: package-private */
    public String getPhoneOperator() {
        return this.phoneOperator;
    }
}
