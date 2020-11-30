package com.oppo.enterprise.mdmcoreservice.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class DeviceVpnProfile implements Parcelable {
    public static final Parcelable.Creator<DeviceVpnProfile> CREATOR = new Parcelable.Creator<DeviceVpnProfile>() {
        /* class com.oppo.enterprise.mdmcoreservice.aidl.DeviceVpnProfile.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DeviceVpnProfile createFromParcel(Parcel source) {
            String readString = source.readString();
            String readString2 = source.readString();
            int readInt = source.readInt();
            String readString3 = source.readString();
            String readString4 = source.readString();
            String readString5 = source.readString();
            boolean z = true;
            if (source.readByte() != 1) {
                z = false;
            }
            return new DeviceVpnProfile(readString, readString2, readInt, readString3, readString4, readString5, z, source.readString(), source.readString(), source.readString(), source.readString(), source.readString(), source.readString());
        }

        @Override // android.os.Parcelable.Creator
        public DeviceVpnProfile[] newArray(int size) {
            return new DeviceVpnProfile[size];
        }
    };
    private String ipsecCaCert;
    private String ipsecIdentifier;
    private String ipsecSecret;
    private String ipsecServerCert;
    private String ipsecUserCert;
    private String key;
    private String l2tpSecret;
    private boolean mppe;
    private String name;
    private String password;
    private String server;
    private int type;
    private String username;

    public DeviceVpnProfile(String key2, String name2, int type2, String server2, String username2, String password2, boolean mppe2, String l2tpSecret2, String ipsecIdentifier2, String ipsecSecret2, String ipsecUserCert2, String ipsecCaCert2, String ipsecServerCert2) {
        this.key = key2;
        this.name = name2;
        this.type = type2;
        this.server = server2;
        this.username = username2;
        this.password = password2;
        this.mppe = mppe2;
        this.l2tpSecret = l2tpSecret2;
        this.ipsecIdentifier = ipsecIdentifier2;
        this.ipsecSecret = ipsecSecret2;
        this.ipsecUserCert = ipsecUserCert2;
        this.ipsecCaCert = ipsecCaCert2;
        this.ipsecServerCert = ipsecServerCert2;
    }

    public DeviceVpnProfile(String key2) {
        this.key = key2;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key2) {
        this.key = key2;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name2) {
        this.name = name2;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type2) {
        this.type = type2;
    }

    public String getServer() {
        return this.server;
    }

    public void setServer(String server2) {
        this.server = server2;
    }

    public String getUserName() {
        return this.username;
    }

    public void setUsername(String username2) {
        this.username = username2;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password2) {
        this.password = password2;
    }

    public boolean isMppe() {
        return this.mppe;
    }

    public void setMppe(boolean mppe2) {
        this.mppe = mppe2;
    }

    public String getL2tpSecret() {
        return this.l2tpSecret;
    }

    public void setL2tpSecret(String l2tpSecret2) {
        this.l2tpSecret = l2tpSecret2;
    }

    public String getIpsecIdentifier() {
        return this.ipsecIdentifier;
    }

    public void setIpsecIdentifier(String ipsecIdentifier2) {
        this.ipsecIdentifier = ipsecIdentifier2;
    }

    public String getIpsecSecret() {
        return this.ipsecSecret;
    }

    public void setIpsecSecret(String ipsecSecret2) {
        this.ipsecSecret = ipsecSecret2;
    }

    public String getIpsecUserCert() {
        return this.ipsecUserCert;
    }

    public void setIpsecUserCert(String ipsecUserCert2) {
        this.ipsecUserCert = ipsecUserCert2;
    }

    public String getIpsecCaCert() {
        return this.ipsecCaCert;
    }

    public void setIpsecCaCert(String ipsecCaCert2) {
        this.ipsecCaCert = ipsecCaCert2;
    }

    public String getIpsecServerCert() {
        return this.ipsecServerCert;
    }

    public void setIpsecServerCert(String ipsecServerCert2) {
        this.ipsecServerCert = ipsecServerCert2;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.key);
        dest.writeString(this.name);
        dest.writeInt(this.type);
        dest.writeString(this.server);
        dest.writeString(this.username);
        dest.writeString(this.password);
        dest.writeByte(this.mppe ? (byte) 1 : 0);
        dest.writeString(this.l2tpSecret);
        dest.writeString(this.ipsecIdentifier);
        dest.writeString(this.ipsecSecret);
        dest.writeString(this.ipsecUserCert);
        dest.writeString(this.ipsecCaCert);
        dest.writeString(this.ipsecServerCert);
    }
}
