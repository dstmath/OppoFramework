package com.suntek.mway.rcs.client.aidl.plugin.entity.contact;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class Auth implements Parcelable {
    public static final Creator<Auth> CREATOR = new Creator<Auth>() {
        public Auth createFromParcel(Parcel source) {
            return new Auth(source);
        }

        public Auth[] newArray(int size) {
            return new Auth[size];
        }
    };
    private String aoiToken;
    private String channelId;
    private String contactUserId;
    private String contact_session;
    private String deviceId;
    private boolean enableSync;
    private int error_code;
    private String error_message;
    private String imei;
    private boolean isAutoSync;
    private boolean isLocalIntent;
    private boolean isThirdPart = false;
    private String message;
    private int result_code;
    private String session;
    private String sessionkey;
    private String syncFrequency;
    private String syncSn;
    private String token;
    private String userId;
    private String username;
    private String version;

    public Auth(Parcel source) {
        readFromParcel(source);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel paramParcel, int paramInt) {
        int i;
        int i2 = 1;
        paramParcel.writeString(this.username);
        paramParcel.writeString(this.session);
        paramParcel.writeString(this.userId);
        paramParcel.writeString(this.sessionkey);
        paramParcel.writeString(this.message);
        paramParcel.writeString(this.imei);
        paramParcel.writeString(this.deviceId);
        paramParcel.writeString(this.token);
        paramParcel.writeString(this.version);
        paramParcel.writeString(this.contact_session);
        paramParcel.writeInt(this.enableSync ? 1 : 0);
        if (this.isAutoSync) {
            i = 1;
        } else {
            i = 0;
        }
        paramParcel.writeInt(i);
        paramParcel.writeString(this.syncSn);
        paramParcel.writeString(this.aoiToken);
        paramParcel.writeString(this.contactUserId);
        if (this.isLocalIntent) {
            i = 1;
        } else {
            i = 0;
        }
        paramParcel.writeInt(i);
        paramParcel.writeString(this.channelId);
        if (!this.isThirdPart) {
            i2 = 0;
        }
        paramParcel.writeInt(i2);
        paramParcel.writeInt(this.error_code);
        paramParcel.writeString(this.error_message);
        paramParcel.writeString(this.syncFrequency);
        paramParcel.writeInt(this.result_code);
    }

    public void readFromParcel(Parcel paramParcel) {
        setUsername(paramParcel.readString());
        setSession(paramParcel.readString());
        setUserId(paramParcel.readString());
        setSessionkey(paramParcel.readString());
        setMessage(paramParcel.readString());
        setImei(paramParcel.readString());
        setDeviceId(paramParcel.readString());
        setToken(paramParcel.readString());
        setVersion(paramParcel.readString());
        setContact_session(paramParcel.readString());
        setEnableSync(paramParcel.readInt() > 0);
        setAutoSync(paramParcel.readInt() > 0);
        setSyncSn(paramParcel.readString());
        setAoiToken(paramParcel.readString());
        setContactUserId(paramParcel.readString());
        setLocalIntent(paramParcel.readInt() > 0);
        setChannelId(paramParcel.readString());
        setThirdPart(paramParcel.readInt() > 0);
        setError_code(paramParcel.readInt());
        setError_message(paramParcel.readString());
        setSyncFrequency(paramParcel.readString());
        setResult_code(paramParcel.readInt());
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSession() {
        return this.session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSessionkey() {
        return this.sessionkey;
    }

    public void setSessionkey(String sessionkey) {
        this.sessionkey = sessionkey;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImei() {
        return this.imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getContact_session() {
        return this.contact_session;
    }

    public void setContact_session(String Contact_session) {
        this.contact_session = Contact_session;
    }

    public boolean isEnableSync() {
        return this.enableSync;
    }

    public void setEnableSync(boolean enableSync) {
        this.enableSync = enableSync;
    }

    public boolean isAutoSync() {
        return this.isAutoSync;
    }

    public void setAutoSync(boolean isAutoSync) {
        this.isAutoSync = isAutoSync;
    }

    public String getSyncSn() {
        return this.syncSn;
    }

    public void setSyncSn(String syncSn) {
        this.syncSn = syncSn;
    }

    public String getAoiToken() {
        return this.aoiToken;
    }

    public void setAoiToken(String aoiToken) {
        this.aoiToken = aoiToken;
    }

    public String getContactUserId() {
        return this.contactUserId;
    }

    public void setContactUserId(String contactUserId) {
        this.contactUserId = contactUserId;
    }

    public boolean isLocalIntent() {
        return this.isLocalIntent;
    }

    public void setLocalIntent(boolean isLocalIntent) {
        this.isLocalIntent = isLocalIntent;
    }

    public String getChannelId() {
        return this.channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public boolean isThirdPart() {
        return this.isThirdPart;
    }

    public void setThirdPart(boolean isThirdPart) {
        this.isThirdPart = isThirdPart;
    }

    public int getError_code() {
        return this.error_code;
    }

    public void setError_code(int error_code) {
        this.error_code = error_code;
    }

    public String getError_message() {
        return this.error_message;
    }

    public void setError_message(String error_message) {
        this.error_message = error_message;
    }

    public String getSyncFrequency() {
        return this.syncFrequency;
    }

    public void setSyncFrequency(String syncFrequency) {
        this.syncFrequency = syncFrequency;
    }

    public int getResult_code() {
        return this.result_code;
    }

    public void setResult_code(int result_code) {
        this.result_code = result_code;
    }
}
