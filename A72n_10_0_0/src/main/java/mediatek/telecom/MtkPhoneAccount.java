package mediatek.telecom;

public final class MtkPhoneAccount {
    public static final int CAPABILITY_IS_VIRTUAL_LINE = 65536;
    public static final int CARRIER_CAPABILITY_ALLOW_ONE_VIDEO_CALL_ONLY = 32;
    private static final int CARRIER_CAPABILITY_BASE = 1;
    public static final int CARRIER_CAPABILITY_DISABLE_DISCONNECT_ALL_CALLS_WHEN_ECC = 2;
    public static final int CARRIER_CAPABILITY_DISABLE_MO_CALL_DURING_CONFERENCE = 128;
    public static final int CARRIER_CAPABILITY_DISABLE_RTT_AUTO_UPGRADE = 16;
    public static final int CARRIER_CAPABILITY_DISABLE_VT_OVER_WIFI = 4;
    public static final int CARRIER_CAPABILITY_DISALLOW_OUTGOING_CALLS_DURING_VIDEO_OR_VOICE_CALL = 64;
    public static final int CARRIER_CAPABILITY_RESUME_HOLD_CALL_AFTER_ACTIVE_CALL_END_BY_REMOTE = 1;
    public static final int CARRIER_CAPABILITY_ROAMING_BAR_GUARD = 8;
    public static final int CARRIER_CAPABILITY_SUPPORT_MTK_ENHANCED_CALL_BLOCKING = 256;
    public static final String EXTRA_PARENT_PHONE_ACCOUNT_HANDLE = "mediatek.telecom.extra.PARENT_PHONE_ACCOUNT_HANDLE";
    public static final String EXTRA_PHONE_ACCOUNT_CARRIER_CAPABILITIES = "mediatek.telecom.extra.PHONE_ACCOUNT_CARRIER_CAPABILITIES";
    public static final String EXTRA_PHONE_ACCOUNT_SORT_KEY = "android.telecom.extra.EXTRA_PHONE_ACCOUNT_SORT_KEY";
    private static final int MTK_CAPABILITY_BASE = 65536;

    public static boolean hasCarrierCapabilities(int capabilities, int capability) {
        return (capabilities & capability) == capability;
    }
}
