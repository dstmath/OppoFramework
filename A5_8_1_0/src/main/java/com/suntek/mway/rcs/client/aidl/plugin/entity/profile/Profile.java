package com.suntek.mway.rcs.client.aidl.plugin.entity.profile;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Profile extends BaseModel implements Parcelable, Serializable {
    public static final Creator<Profile> CREATOR = new Creator<Profile>() {
        public Profile createFromParcel(Parcel source) {
            return new Profile(source);
        }

        public Profile[] newArray(int size) {
            return new Profile[size];
        }
    };
    private static final long serialVersionUID = -8351378268771321120L;
    private String birthday;
    private String companyAddress;
    private String companyDuty;
    private String companyFax;
    private String companyName;
    private String companyTel;
    private String email;
    private String firstName;
    private String homeAddress;
    private String lastName;
    private ArrayList<TelephoneModel> otherTels;

    public Profile(Parcel source) {
        readFromParcel(source);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.homeAddress);
        dest.writeString(this.email);
        dest.writeString(this.birthday);
        dest.writeString(this.companyName);
        dest.writeString(this.companyDuty);
        dest.writeString(this.companyTel);
        dest.writeString(this.companyAddress);
        dest.writeString(this.companyFax);
        dest.writeString(this.firstName);
        dest.writeString(this.lastName);
        dest.writeList(this.otherTels);
    }

    public void readFromParcel(Parcel source) {
        super.readFromParcel(source);
        this.homeAddress = source.readString();
        this.email = source.readString();
        this.birthday = source.readString();
        this.companyName = source.readString();
        this.companyDuty = source.readString();
        this.companyTel = source.readString();
        this.companyAddress = source.readString();
        this.companyFax = source.readString();
        this.firstName = source.readString();
        this.lastName = source.readString();
        this.otherTels = new ArrayList();
        source.readList(this.otherTels, getClass().getClassLoader());
    }

    public String getHomeAddress() {
        return this.homeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBirthday() {
        return this.birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getCompanyName() {
        return this.companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyDuty() {
        return this.companyDuty;
    }

    public void setCompanyDuty(String companyDuty) {
        this.companyDuty = companyDuty;
    }

    public String getCompanyTel() {
        return this.companyTel;
    }

    public void setCompanyTel(String companyTel) {
        this.companyTel = companyTel;
    }

    public String getCompanyAddress() {
        return this.companyAddress;
    }

    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
    }

    public String getCompanyFax() {
        return this.companyFax;
    }

    public void setCompanyFax(String companyFax) {
        this.companyFax = companyFax;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getDisplayName() {
        String first = this.firstName;
        String last = this.lastName;
        boolean isLettersOnly = true ? isNullOrLettersOnly(first) : false ? isNullOrLettersOnly(last) : false;
        if (first == null) {
            first = "";
        }
        if (last == null) {
            last = "";
        }
        if (isLettersOnly) {
            return first + " " + last;
        }
        return last + first;
    }

    private boolean isNullOrLettersOnly(String text) {
        if (text == null) {
            return true;
        }
        return isLettersOnly(text.trim());
    }

    public static boolean isLettersOnly(CharSequence str) {
        int len = str.length();
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if ((c < 'a' || c > 'z') && (c < 'A' || c > 'Z')) {
                return false;
            }
        }
        return true;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public ArrayList<TelephoneModel> getOtherTels() {
        if (this.otherTels == null) {
            return new ArrayList();
        }
        return this.otherTels;
    }

    public void setOtherTels(ArrayList<TelephoneModel> otherTels) {
        this.otherTels = otherTels;
    }

    public String toString() {
        String str = null;
        List<String> list = new ArrayList();
        list.add("homeAddress=" + this.homeAddress);
        list.add("email=" + this.email);
        list.add("birthday=" + this.birthday);
        list.add("companyName=" + this.companyName);
        list.add("companyDuty=" + this.companyDuty);
        list.add("companyTel=" + this.companyTel);
        list.add("companyAddress=" + this.companyAddress);
        list.add("companyFax=" + this.companyFax);
        list.add("firstName=" + this.firstName);
        list.add("lastName=" + this.lastName);
        StringBuilder append = new StringBuilder().append("otherTels=");
        if (this.otherTels != null) {
            str = this.otherTels.toString();
        }
        list.add(append.append(str).toString());
        list.add("account=" + getAccount());
        list.add("etag=" + getEtag());
        return list.toString();
    }
}
