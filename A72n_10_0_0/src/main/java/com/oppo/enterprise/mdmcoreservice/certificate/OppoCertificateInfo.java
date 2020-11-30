package com.oppo.enterprise.mdmcoreservice.certificate;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class OppoCertificateInfo implements Parcelable {
    public static final Parcelable.Creator<OppoCertificateInfo> CREATOR = new Parcelable.Creator<OppoCertificateInfo>() {
        /* class com.oppo.enterprise.mdmcoreservice.certificate.OppoCertificateInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public OppoCertificateInfo createFromParcel(Parcel in) {
            return new OppoCertificateInfo(in);
        }

        @Override // android.os.Parcelable.Creator
        public OppoCertificateInfo[] newArray(int size) {
            return new OppoCertificateInfo[size];
        }
    };
    private List<String> apiList;
    private final String apkHash;
    private List<String> deviceIds;
    private Date lastVerityTime;
    public final String licenceCode;
    public final String packageName;
    private List<String> permissions;
    private final String signature;
    private final String type;
    private String validFrom;
    private String validTo;
    private int verityStatus;

    public OppoCertificateInfo(String packageName2, String licenceCode2, String[] permissions2, String[] apiList2, String[] deviceIds2, String validFrom2, String validTo2, String apkHash2, String signature2, String type2) {
        this.permissions = null;
        this.apiList = null;
        this.deviceIds = null;
        this.verityStatus = 5;
        this.lastVerityTime = null;
        this.packageName = packageName2;
        this.licenceCode = licenceCode2;
        if (permissions2 != null) {
            this.permissions = new ArrayList();
            for (String str : permissions2) {
                this.permissions.add(str.trim());
            }
        }
        if (apiList2 != null) {
            this.apiList = new ArrayList();
            for (String str2 : apiList2) {
                this.apiList.add(str2.trim());
            }
        }
        if (deviceIds2 != null) {
            this.deviceIds = new ArrayList();
            for (String str3 : deviceIds2) {
                this.deviceIds.add(str3.trim());
            }
        }
        this.validFrom = validFrom2;
        this.validTo = validTo2;
        this.apkHash = apkHash2;
        this.signature = signature2;
        this.type = type2;
    }

    private OppoCertificateInfo(Parcel dest) {
        this.permissions = null;
        this.apiList = null;
        this.deviceIds = null;
        this.verityStatus = 5;
        this.lastVerityTime = null;
        this.packageName = dest.readString().intern();
        this.licenceCode = dest.readString().intern();
        dest.readList(this.permissions, String.class.getClassLoader());
        dest.readList(this.apiList, String.class.getClassLoader());
        dest.readList(this.deviceIds, String.class.getClassLoader());
        this.validFrom = dest.readString().intern();
        this.validTo = dest.readString().intern();
        this.apkHash = dest.readString().intern();
        this.signature = dest.readString().intern();
        this.type = dest.readString().intern();
    }

    public synchronized void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.packageName);
        dest.writeString(this.licenceCode);
        dest.writeList(this.permissions);
        dest.writeList(this.apiList);
        dest.writeList(this.deviceIds);
        dest.writeString(this.validFrom);
        dest.writeString(this.validTo);
        dest.writeString(this.apkHash);
        dest.writeString(this.signature);
        dest.writeString(this.type);
    }

    public int describeContents() {
        return 0;
    }

    public synchronized String toString() {
        String out;
        out = "CER: " + this.packageName + " licenceCode:" + this.licenceCode + " type:" + this.type + " valid from: " + this.validFrom + "to:" + this.validTo;
        if (this.permissions != null) {
            out = out + " perm:";
            Iterator<String> it = this.permissions.iterator();
            while (it.hasNext()) {
                out = out + it.next() + ";";
            }
        }
        if (this.apiList != null) {
            out = out + " api:";
            Iterator<String> it2 = this.apiList.iterator();
            while (it2.hasNext()) {
                out = out + it2.next() + ";";
            }
        }
        if (this.deviceIds != null) {
            out = out + " deviceIds:";
            Iterator<String> it3 = this.deviceIds.iterator();
            while (it3.hasNext()) {
                out = out + it3.next() + ";";
            }
        }
        return out + " HASH: " + this.apkHash + " SIG: " + this.signature;
    }

    public synchronized boolean compare(OppoCertificateInfo other) {
        return toString().equals(other.toString());
    }

    public synchronized boolean isTypeOnline() {
        return "online".equals(this.type);
    }

    public static String verityStatusToString(int status) {
        switch (status) {
            case 1:
                return "sucess";
            case 2:
                return "invalid";
            case 3:
                return "outOfDate";
            case 4:
                return "disabled";
            case 5:
                return "needVerify";
            default:
                return "unknow";
        }
    }

    public synchronized boolean havePermission(String permission, boolean checkValid, Date now) {
        if (checkValid) {
            if (!checkValidate(now)) {
                return false;
            }
        }
        if (this.permissions == null || !this.permissions.contains(permission)) {
            return false;
        }
        return true;
    }

    private synchronized boolean haveAPI(String api) {
        if (this.apiList == null || !this.apiList.contains(api)) {
            return false;
        }
        return true;
    }

    public synchronized boolean canCallAPI(String api, String perm, Date now) {
        boolean z = false;
        if (!checkValidate(now)) {
            return false;
        }
        if (havePermission(perm, false, now) && haveAPI(api)) {
            z = true;
        }
        return z;
    }

    public int getVerityStatus() {
        return this.verityStatus;
    }

    public void setVerityStatus(int status) {
        this.verityStatus = status;
    }

    public synchronized List<String> getSupportDeviceIDs() {
        return this.deviceIds;
    }

    private synchronized boolean checkValidate(Date now) {
        boolean z;
        z = true;
        if (getVerityStatus() != 1 || !validateFromTo(this.validFrom, this.validTo, now)) {
            z = false;
        }
        return z;
    }

    public synchronized void setLastVerityTime(Date d) {
        this.lastVerityTime = d;
    }

    public synchronized void updateFromToDate(String from, String to) {
        this.validFrom = from;
        this.validTo = to;
    }

    public synchronized boolean isRemoteVerifyNeeded(Date now) {
        if (!isTypeOnline()) {
            Log.d("OppoCertificateVerifier", "Not online type");
            return false;
        }
        if (now == null) {
            now = new Date();
        }
        if (this.lastVerityTime == null || now.getTime() - this.lastVerityTime.getTime() >= 28800000) {
            return true;
        }
        Log.d("OppoCertificateVerifier", "now:" + now.getTime() + ";lastVerityTime:" + this.lastVerityTime.getTime());
        return false;
    }

    private synchronized boolean validateFromTo(String validFrom2, String validTo2, Date now) {
        Date from = null;
        Date to = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (validFrom2 != null) {
            try {
                from = df.parse(validFrom2);
            } catch (ParseException e) {
            }
        }
        if (validTo2 != null) {
            try {
                to = df.parse(validTo2);
            } catch (ParseException e2) {
            }
        }
        if (from != null && to != null && !now.before(from) && !now.after(to)) {
            return true;
        }
        Log.d("OppoCertificateVerifier", "Invalid date time form:" + from + "to" + to + ", now is " + now);
        return false;
    }
}
