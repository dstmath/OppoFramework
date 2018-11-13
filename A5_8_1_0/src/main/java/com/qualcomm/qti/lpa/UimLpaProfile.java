package com.qualcomm.qti.lpa;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Arrays;

public class UimLpaProfile implements Parcelable {
    public static final Creator<UimLpaProfile> CREATOR = new Creator<UimLpaProfile>() {
        public UimLpaProfile createFromParcel(Parcel in) {
            return new UimLpaProfile(in);
        }

        public UimLpaProfile[] newArray(int size) {
            return new UimLpaProfile[size];
        }
    };
    public final int UIM_LPA_ICON_TYPE_JPEG = 0;
    public final int UIM_LPA_ICON_TYPE_PNG = 1;
    public byte[] iccid;
    public byte[] icon;
    public int iconType;
    public String nickname;
    public boolean nicknamePresent;
    public int profileClass;
    public String profileName;
    public boolean profileNamePresent;
    public int profilePolicyMask;
    public boolean profileState;
    public String spnName;
    public boolean spnNamePresent;

    public UimLpaProfile(Parcel in) {
        boolean z;
        boolean z2 = false;
        this.profileNamePresent = in.readInt() != 0;
        if (this.profileNamePresent) {
            this.profileName = in.readString();
        }
        if (in.readInt() == 0) {
            z = false;
        } else {
            z = true;
        }
        this.nicknamePresent = z;
        if (this.nicknamePresent) {
            this.nickname = in.readString();
        }
        if (in.readInt() == 0) {
            z = false;
        } else {
            z = true;
        }
        this.profileState = z;
        int iccidLen = in.readInt();
        if (iccidLen > 0) {
            this.iccid = new byte[iccidLen];
            in.readByteArray(this.iccid);
        }
        if (in.readInt() != 0) {
            z2 = true;
        }
        this.spnNamePresent = z2;
        if (this.spnNamePresent) {
            this.spnName = in.readString();
        }
        this.iconType = in.readInt();
        int iconLen = in.readInt();
        if (iconLen > 0) {
            this.icon = new byte[iconLen];
            in.readByteArray(this.icon);
        }
        this.profileClass = in.readInt();
        this.profilePolicyMask = in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        if (this.profileName != null) {
            dest.writeInt(1);
            dest.writeString(this.profileName);
        } else {
            dest.writeInt(0);
        }
        if (this.nickname != null) {
            dest.writeInt(1);
            dest.writeString(this.nickname);
        } else {
            dest.writeInt(0);
        }
        if (this.profileState) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (this.iccid != null) {
            dest.writeInt(this.iccid.length);
            dest.writeByteArray(this.iccid);
        } else {
            dest.writeInt(0);
        }
        if (this.spnName != null) {
            dest.writeInt(1);
            dest.writeString(this.spnName);
        } else {
            dest.writeInt(0);
        }
        dest.writeInt(this.iconType);
        if (this.icon != null) {
            dest.writeInt(this.icon.length);
            dest.writeByteArray(this.icon);
        } else {
            dest.writeInt(0);
        }
        dest.writeInt(this.profileClass);
        dest.writeInt(this.profilePolicyMask);
    }

    public void setProfileState(boolean profileState) {
        this.profileState = profileState;
    }

    public boolean getProfileState() {
        return this.profileState;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return this.nickname;
    }

    public void setProfilename(String profileName) {
        this.profileName = profileName;
    }

    public String getProfileName() {
        return this.profileName;
    }

    public void setIccid(byte[] iccid) {
        if (iccid != null) {
            this.iccid = Arrays.copyOf(iccid, iccid.length);
        }
    }

    public byte[] geticcid() {
        return this.iccid;
    }

    public void setSpnName(String spnName) {
        this.spnName = spnName;
    }

    public String getSpnName() {
        return this.spnName;
    }

    public void setIconType(int iconType) {
        this.iconType = iconType;
    }

    public int getIconType() {
        return this.iconType;
    }

    public void setIcon(byte[] icon) {
        if (icon != null) {
            this.icon = Arrays.copyOf(icon, icon.length);
        }
    }

    public byte[] getIcon() {
        return this.icon;
    }

    public void setProfileClass(int profileClass) {
        this.profileClass = profileClass;
    }

    public int getProfileClass() {
        return this.profileClass;
    }

    public void setProfilePolicy(int profilePolicyMask) {
        this.profilePolicyMask = profilePolicyMask;
    }

    public int getProfilePolicy() {
        return this.profilePolicyMask;
    }

    public String toString() {
        if (this.profileName != null) {
            return this.profileName.toString();
        }
        return new String("No name");
    }
}
