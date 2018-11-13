package com.suntek.mway.rcs.client.aidl.plugin.entity.cloudfile;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Map;

public class AuthNode implements Parcelable {
    public static final Creator<AuthNode> CREATOR = new Creator<AuthNode>() {
        public AuthNode createFromParcel(Parcel source) {
            return new AuthNode(source);
        }

        public AuthNode[] newArray(int size) {
            return new AuthNode[size];
        }
    };
    private byte[] captcha;
    private Map<String, String> fields;
    private boolean isOffline;
    private PwdType pwdType;
    private RegType regType;
    private ResetType resetType;
    private int timeout;
    private AuthNodeUpdateInfo updateInfo;
    private UserType userType;

    public enum PwdType {
        encrypted,
        original,
        dynamic,
        thirdParty;

        public static PwdType valueOf(int ordinal) {
            if (ordinal >= 0 && ordinal < values().length) {
                return values()[ordinal];
            }
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
    }

    public enum RegType {
        cellPhone;

        public static RegType valueOf(int ordinal) {
            if (ordinal >= 0 && ordinal < values().length) {
                return values()[ordinal];
            }
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
    }

    public enum ResetType {
        cellPhone,
        thirdParty;

        public static ResetType valueOf(int ordinal) {
            if (ordinal >= 0 && ordinal < values().length) {
                return values()[ordinal];
            }
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
    }

    public enum UserType {
        account,
        bindMobile,
        bindEmail,
        email;

        public static UserType valueOf(int ordinal) {
            if (ordinal >= 0 && ordinal < values().length) {
                return values()[ordinal];
            }
            throw new IndexOutOfBoundsException("Invalid ordinal");
        }
    }

    public AuthNode(Parcel source) {
        readFromParcel(source);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(this.captcha);
        dest.writeBooleanArray(new boolean[]{this.isOffline});
        dest.writeValue(this.updateInfo);
        dest.writeInt(this.timeout);
        dest.writeInt(this.userType.ordinal());
        dest.writeInt(this.pwdType.ordinal());
        dest.writeInt(this.regType.ordinal());
        dest.writeInt(this.resetType.ordinal());
        dest.writeMap(this.fields);
    }

    public void readFromParcel(Parcel source) {
        this.captcha = source.createByteArray();
        boolean[] val = new boolean[1];
        source.readBooleanArray(val);
        this.isOffline = val[0];
        this.updateInfo = (AuthNodeUpdateInfo) source.readValue(getClass().getClassLoader());
        this.userType = UserType.valueOf(source.readInt());
        this.pwdType = PwdType.valueOf(source.readInt());
        this.regType = RegType.valueOf(source.readInt());
        this.resetType = ResetType.valueOf(source.readInt());
        this.fields = source.readHashMap(getClass().getClassLoader());
    }

    public byte[] getCaptcha() {
        return this.captcha;
    }

    public void setCaptcha(byte[] captcha) {
        this.captcha = captcha;
    }

    public boolean isOffline() {
        return this.isOffline;
    }

    public void setOffline(boolean isOffline) {
        this.isOffline = isOffline;
    }

    public AuthNodeUpdateInfo getUpdateInfo() {
        return this.updateInfo;
    }

    public void setUpdateInfo(AuthNodeUpdateInfo updateInfo) {
        this.updateInfo = updateInfo;
    }

    public int getTimeout() {
        return this.timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public UserType getUserType() {
        return this.userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public PwdType getPwdType() {
        return this.pwdType;
    }

    public void setPwdType(PwdType pwdType) {
        this.pwdType = pwdType;
    }

    public RegType getRegType() {
        return this.regType;
    }

    public void setRegType(RegType regType) {
        this.regType = regType;
    }

    public ResetType getResetType() {
        return this.resetType;
    }

    public void setResetType(ResetType resetType) {
        this.resetType = resetType;
    }

    public Map<String, String> getFields() {
        return this.fields;
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }
}
