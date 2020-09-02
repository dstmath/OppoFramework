package com.mediatek.internal.telephony;

public class MtkPhoneConstants {
    public static final int APN_AUTH_TYPE_MAX_NUM = 7;
    public static final int APN_INACTIVE_TIMER_KEY = 3;
    public static final int APN_MAX_INACTIVE_TIMER = 536870911;
    public static final String APN_TYPE_BIP = "bip";
    public static final String APN_TYPE_RCS = "rcs";
    public static final String APN_TYPE_VSIM = "vsim";
    public static final String APN_TYPE_WAP = "wap";
    public static final String APN_TYPE_XCAP = "xcap";
    public static final int IMS_STATE_DISABLED = 0;
    public static final int IMS_STATE_DISABLING = 3;
    public static final int IMS_STATE_ENABLE = 1;
    public static final int IMS_STATE_ENABLING = 2;
    public static final String LTE_ACCESS_STRATUM_STATE_CONNECTED = "connected";
    public static final String LTE_ACCESS_STRATUM_STATE_IDLE = "idle";
    public static final String LTE_ACCESS_STRATUM_STATE_KEY = "lteAccessStratumState";
    public static final String LTE_ACCESS_STRATUM_STATE_UNKNOWN = "unknown";
    public static final String[] MTK_APN_TYPES = {"default", "mms", "supl", "dun", "hipri", "fota", "ims", "cbs", "ia", "emergency", "mcx", APN_TYPE_WAP, APN_TYPE_XCAP, APN_TYPE_RCS, APN_TYPE_BIP, APN_TYPE_VSIM};
    public static final String MVNO_TYPE_GID = "gid";
    public static final String MVNO_TYPE_IMSI = "imsi";
    public static final String MVNO_TYPE_NONE = "";
    public static final String MVNO_TYPE_PNN = "pnn";
    public static final String MVNO_TYPE_SPN = "spn";
    public static final String PHONE_TYPE_KEY = "phoneType";
    public static final String PROPERTY_CAPABILITY_SWITCH = "persist.vendor.radio.simswitch";
    public static final String PS_NETWORK_TYPE_KEY = "psNetworkType";
    public static final int RAT_TYPE_KEY = 1000;
    public static final int RAT_TYPE_MAX = 4;
    public static final int RAT_TYPE_MOBILE_3GPP = 1;
    public static final int RAT_TYPE_MOBILE_3GPP2 = 3;
    public static final int RAT_TYPE_UNSPEC = 0;
    public static final int RAT_TYPE_WIFI = 2;
    public static final String SHARED_DEFAULT_APN_KEY = "sharedDefaultApn";
    public static final int UT_CSFB_ONCE = 1;
    public static final int UT_CSFB_PS_PREFERRED = 0;
    public static final int UT_CSFB_UNTIL_NEXT_BOOT = 2;
}
