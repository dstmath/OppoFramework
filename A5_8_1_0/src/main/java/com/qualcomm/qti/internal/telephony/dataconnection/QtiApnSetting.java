package com.qualcomm.qti.internal.telephony.dataconnection;

import com.android.internal.telephony.dataconnection.ApnSetting;

public class QtiApnSetting extends ApnSetting {
    public int profileId;

    public enum ApnProfileType {
        PROFILE_TYPE_APN(0),
        PROFILE_TYPE_CDMA(1),
        PROFILE_TYPE_OMH(2);
        
        int id;

        private ApnProfileType(int i) {
            this.id = i;
        }

        public int getid() {
            return this.id;
        }
    }

    public QtiApnSetting(int id, String numeric, String carrier, String apn, String proxy, String port, String mmsc, String mmsProxy, String mmsPort, String user, String password, int authType, String[] types, String protocol, String roamingProtocol, boolean carrierEnabled, int bearer, int bearerBitMask, int profileId, boolean modemCognitive, int maxConns, int waitTime, int maxConnsTime, int mtu, String mvnoType, String mvnoMatchData) {
        super(id, numeric, carrier, apn, proxy, port, mmsc, mmsProxy, mmsPort, user, password, authType, types, protocol, roamingProtocol, carrierEnabled, bearer, bearerBitMask, profileId, modemCognitive, maxConns, waitTime, maxConnsTime, mtu, mvnoType, mvnoMatchData);
        this.profileId = profileId;
    }

    public ApnProfileType getApnProfileType() {
        return ApnProfileType.PROFILE_TYPE_APN;
    }

    public int getProfileId() {
        return this.profileId;
    }

    public String toShortString() {
        return "QtiApnSetting";
    }

    public String toHash() {
        return toString();
    }
}
