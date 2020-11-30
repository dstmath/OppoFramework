package android.telephony.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemProperties;
import android.provider.Telephony;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.SmsManager;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.telephony.PhoneConstants;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ApnSetting implements Parcelable {
    protected static Map<Integer, String> APN_TYPE_INT_MAP = new ArrayMap();
    protected static Map<String, Integer> APN_TYPE_STRING_MAP = new ArrayMap();
    public static final int AUTH_TYPE_CHAP = 2;
    public static final int AUTH_TYPE_NONE = 0;
    public static final int AUTH_TYPE_PAP = 1;
    public static final int AUTH_TYPE_PAP_OR_CHAP = 3;
    public static final Parcelable.Creator<ApnSetting> CREATOR = new Parcelable.Creator<ApnSetting>() {
        /* class android.telephony.data.ApnSetting.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ApnSetting createFromParcel(Parcel in) {
            return ApnSetting.readFromParcel(in);
        }

        @Override // android.os.Parcelable.Creator
        public ApnSetting[] newArray(int size) {
            return new ApnSetting[size];
        }
    };
    private static final String LOG_TAG = "ApnSetting";
    public static final int MVNO_TYPE_GID = 2;
    public static final int MVNO_TYPE_ICCID = 3;
    public static final int MVNO_TYPE_IMSI = 1;
    protected static Map<Integer, String> MVNO_TYPE_INT_MAP = new ArrayMap();
    public static final int MVNO_TYPE_SPN = 0;
    protected static Map<String, Integer> MVNO_TYPE_STRING_MAP = new ArrayMap();
    protected static Map<Integer, String> PROTOCOL_INT_MAP = new ArrayMap();
    public static final int PROTOCOL_IP = 0;
    public static final int PROTOCOL_IPV4V6 = 2;
    public static final int PROTOCOL_IPV6 = 1;
    public static final int PROTOCOL_NON_IP = 4;
    public static final int PROTOCOL_PPP = 3;
    protected static Map<String, Integer> PROTOCOL_STRING_MAP = new ArrayMap();
    public static final int PROTOCOL_UNSTRUCTURED = 5;
    public static final int TYPE_ALL = 255;
    public static final int TYPE_CBS = 128;
    public static final int TYPE_DEFAULT = 17;
    public static final int TYPE_DUN = 8;
    public static final int TYPE_EMERGENCY = 512;
    public static final int TYPE_FOTA = 32;
    public static final int TYPE_HIPRI = 16;
    public static final int TYPE_IA = 256;
    public static final int TYPE_IMS = 64;
    public static final int TYPE_MCX = 1024;
    public static final int TYPE_MMS = 2;
    public static final int TYPE_NONE = 0;
    public static final int TYPE_SUPL = 4;
    public static final int UNSET_MTU = 0;
    private static final int UNSPECIFIED_INT = -1;
    private static final String UNSPECIFIED_STRING = "";
    private static final String V2_FORMAT_REGEX = "^\\[ApnSettingV2\\]\\s*";
    private static final String V3_FORMAT_REGEX = "^\\[ApnSettingV3\\]\\s*";
    private static final String V4_FORMAT_REGEX = "^\\[ApnSettingV4\\]\\s*";
    private static final String V5_FORMAT_REGEX = "^\\[ApnSettingV5\\]\\s*";
    private static final String V6_FORMAT_REGEX = "^\\[ApnSettingV6\\]\\s*";
    private static final String V7_FORMAT_REGEX = "^\\[ApnSettingV7\\]\\s*";
    private static final boolean VDBG = false;
    private static Method sMethodFromStringEx;
    private static Method sMethodGetApnTypeStringEx;
    private static Method sMethodGetApnTypesBitmaskFromStringEx;
    private final String mApnName;
    private final int mApnSetId;
    protected int mApnTypeBitmask;
    private int mAuthType;
    private final boolean mCarrierEnabled;
    private final int mCarrierId;
    private final String mEntryName;
    private final int mId;
    private final int mMaxConns;
    private final int mMaxConnsTime;
    private final String mMmsProxyAddress;
    private final int mMmsProxyPort;
    private final Uri mMmsc;
    private final int mMtu;
    private final String mMvnoMatchData;
    private final int mMvnoType;
    private final int mNetworkTypeBitmask;
    private final String mOperatorNumeric;
    private final String mPassword;
    private boolean mPermanentFailed = false;
    private final boolean mPersistent;
    private final int mProfileId;
    private final int mProtocol;
    private final String mProxyAddress;
    private final int mProxyPort;
    private final int mRoamingProtocol;
    private final int mSkip464Xlat;
    private final String mUser;
    private final int mWaitTime;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ApnType {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface AuthType {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface MvnoType {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ProtocolType {
    }

    static {
        APN_TYPE_STRING_MAP.put("*", 255);
        APN_TYPE_STRING_MAP.put(PhoneConstants.APN_TYPE_DEFAULT, 17);
        APN_TYPE_STRING_MAP.put(PhoneConstants.APN_TYPE_MMS, 2);
        APN_TYPE_STRING_MAP.put(PhoneConstants.APN_TYPE_SUPL, 4);
        APN_TYPE_STRING_MAP.put(PhoneConstants.APN_TYPE_DUN, 8);
        APN_TYPE_STRING_MAP.put(PhoneConstants.APN_TYPE_HIPRI, 16);
        APN_TYPE_STRING_MAP.put(PhoneConstants.APN_TYPE_FOTA, 32);
        APN_TYPE_STRING_MAP.put(PhoneConstants.APN_TYPE_IMS, 64);
        APN_TYPE_STRING_MAP.put(PhoneConstants.APN_TYPE_CBS, 128);
        APN_TYPE_STRING_MAP.put(PhoneConstants.APN_TYPE_IA, 256);
        APN_TYPE_STRING_MAP.put(PhoneConstants.APN_TYPE_EMERGENCY, 512);
        APN_TYPE_STRING_MAP.put(PhoneConstants.APN_TYPE_MCX, 1024);
        APN_TYPE_INT_MAP.put(17, PhoneConstants.APN_TYPE_DEFAULT);
        APN_TYPE_INT_MAP.put(2, PhoneConstants.APN_TYPE_MMS);
        APN_TYPE_INT_MAP.put(4, PhoneConstants.APN_TYPE_SUPL);
        APN_TYPE_INT_MAP.put(8, PhoneConstants.APN_TYPE_DUN);
        APN_TYPE_INT_MAP.put(16, PhoneConstants.APN_TYPE_HIPRI);
        APN_TYPE_INT_MAP.put(32, PhoneConstants.APN_TYPE_FOTA);
        APN_TYPE_INT_MAP.put(64, PhoneConstants.APN_TYPE_IMS);
        APN_TYPE_INT_MAP.put(128, PhoneConstants.APN_TYPE_CBS);
        APN_TYPE_INT_MAP.put(256, PhoneConstants.APN_TYPE_IA);
        APN_TYPE_INT_MAP.put(512, PhoneConstants.APN_TYPE_EMERGENCY);
        APN_TYPE_INT_MAP.put(1024, PhoneConstants.APN_TYPE_MCX);
        PROTOCOL_STRING_MAP.put("IP", 0);
        PROTOCOL_STRING_MAP.put("IPV6", 1);
        PROTOCOL_STRING_MAP.put("IPV4V6", 2);
        PROTOCOL_STRING_MAP.put("PPP", 3);
        PROTOCOL_STRING_MAP.put("NON-IP", 4);
        PROTOCOL_STRING_MAP.put("UNSTRUCTURED", 5);
        PROTOCOL_INT_MAP.put(0, "IP");
        PROTOCOL_INT_MAP.put(1, "IPV6");
        PROTOCOL_INT_MAP.put(2, "IPV4V6");
        PROTOCOL_INT_MAP.put(3, "PPP");
        PROTOCOL_INT_MAP.put(4, "NON-IP");
        PROTOCOL_INT_MAP.put(5, "UNSTRUCTURED");
        MVNO_TYPE_STRING_MAP.put("spn", 0);
        MVNO_TYPE_STRING_MAP.put(SubscriptionManager.IMSI, 1);
        MVNO_TYPE_STRING_MAP.put("gid", 2);
        MVNO_TYPE_STRING_MAP.put("iccid", 3);
        MVNO_TYPE_INT_MAP.put(0, "spn");
        MVNO_TYPE_INT_MAP.put(1, SubscriptionManager.IMSI);
        MVNO_TYPE_INT_MAP.put(2, "gid");
        MVNO_TYPE_INT_MAP.put(3, "iccid");
        if (SystemProperties.get("ro.vendor.mtk_telephony_add_on_policy", WifiEnterpriseConfig.ENGINE_DISABLE).equals(WifiEnterpriseConfig.ENGINE_DISABLE)) {
            Class<?> clz = null;
            try {
                clz = Class.forName("mediatek.telephony.data.MtkApnSetting");
            } catch (Exception e) {
                Rlog.d(LOG_TAG, e.toString());
            }
            if (clz != null) {
                try {
                    sMethodFromStringEx = clz.getDeclaredMethod("fromStringEx", String[].class, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Boolean.TYPE, Integer.TYPE, Integer.TYPE, Boolean.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, String.class, Integer.TYPE, Integer.TYPE, Integer.TYPE);
                    sMethodFromStringEx.setAccessible(true);
                } catch (Exception e2) {
                    Rlog.d(LOG_TAG, e2.toString());
                }
            }
            try {
                sMethodGetApnTypeStringEx = clz.getDeclaredMethod("getApnTypeStringEx", Integer.TYPE);
                sMethodGetApnTypeStringEx.setAccessible(true);
            } catch (Exception e3) {
                Rlog.d(LOG_TAG, e3.toString());
            }
            try {
                sMethodGetApnTypesBitmaskFromStringEx = clz.getDeclaredMethod("getApnTypesBitmaskFromStringEx", String.class);
                sMethodGetApnTypesBitmaskFromStringEx.setAccessible(true);
            } catch (Exception e4) {
                Rlog.d(LOG_TAG, e4.toString());
            }
        }
    }

    public int getMtu() {
        return this.mMtu;
    }

    public int getProfileId() {
        return this.mProfileId;
    }

    public boolean isPersistent() {
        return this.mPersistent;
    }

    public int getMaxConns() {
        return this.mMaxConns;
    }

    public int getWaitTime() {
        return this.mWaitTime;
    }

    public int getMaxConnsTime() {
        return this.mMaxConnsTime;
    }

    public String getMvnoMatchData() {
        return this.mMvnoMatchData;
    }

    public int getApnSetId() {
        return this.mApnSetId;
    }

    public boolean getPermanentFailed() {
        return this.mPermanentFailed;
    }

    public void setPermanentFailed(boolean permanentFailed) {
        this.mPermanentFailed = permanentFailed;
    }

    public String getEntryName() {
        return this.mEntryName;
    }

    public String getApnName() {
        return this.mApnName;
    }

    @Deprecated
    public InetAddress getProxyAddress() {
        return inetAddressFromString(this.mProxyAddress);
    }

    public String getProxyAddressAsString() {
        return this.mProxyAddress;
    }

    public int getProxyPort() {
        return this.mProxyPort;
    }

    public Uri getMmsc() {
        return this.mMmsc;
    }

    @Deprecated
    public InetAddress getMmsProxyAddress() {
        return inetAddressFromString(this.mMmsProxyAddress);
    }

    public String getMmsProxyAddressAsString() {
        return this.mMmsProxyAddress;
    }

    public int getMmsProxyPort() {
        return this.mMmsProxyPort;
    }

    public String getUser() {
        return this.mUser;
    }

    public String getPassword() {
        return this.mPassword;
    }

    public int getAuthType() {
        return this.mAuthType;
    }

    public int getApnTypeBitmask() {
        return this.mApnTypeBitmask;
    }

    public int getId() {
        return this.mId;
    }

    public String getOperatorNumeric() {
        return this.mOperatorNumeric;
    }

    public int getProtocol() {
        return this.mProtocol;
    }

    public int getRoamingProtocol() {
        return this.mRoamingProtocol;
    }

    public boolean isEnabled() {
        return this.mCarrierEnabled;
    }

    public int getNetworkTypeBitmask() {
        return this.mNetworkTypeBitmask;
    }

    public int getMvnoType() {
        return this.mMvnoType;
    }

    public int getCarrierId() {
        return this.mCarrierId;
    }

    public int getSkip464Xlat() {
        return this.mSkip464Xlat;
    }

    protected ApnSetting(Builder builder) {
        this.mEntryName = builder.mEntryName;
        this.mApnName = builder.mApnName;
        this.mProxyAddress = builder.mProxyAddress;
        this.mProxyPort = builder.mProxyPort;
        this.mMmsc = builder.mMmsc;
        this.mMmsProxyAddress = builder.mMmsProxyAddress;
        this.mMmsProxyPort = builder.mMmsProxyPort;
        this.mUser = builder.mUser;
        this.mPassword = builder.mPassword;
        this.mAuthType = builder.mAuthType;
        this.mApnTypeBitmask = builder.mApnTypeBitmask;
        this.mId = builder.mId;
        this.mOperatorNumeric = builder.mOperatorNumeric;
        this.mProtocol = builder.mProtocol;
        this.mRoamingProtocol = builder.mRoamingProtocol;
        this.mMtu = builder.mMtu;
        this.mCarrierEnabled = builder.mCarrierEnabled;
        this.mNetworkTypeBitmask = builder.mNetworkTypeBitmask;
        this.mProfileId = builder.mProfileId;
        this.mPersistent = builder.mModemCognitive;
        this.mMaxConns = builder.mMaxConns;
        this.mWaitTime = builder.mWaitTime;
        this.mMaxConnsTime = builder.mMaxConnsTime;
        this.mMvnoType = builder.mMvnoType;
        this.mMvnoMatchData = builder.mMvnoMatchData;
        this.mApnSetId = builder.mApnSetId;
        this.mCarrierId = builder.mCarrierId;
        this.mSkip464Xlat = builder.mSkip464Xlat;
    }

    public static ApnSetting makeApnSetting(int id, String operatorNumeric, String entryName, String apnName, String proxyAddress, int proxyPort, Uri mmsc, String mmsProxyAddress, int mmsProxyPort, String user, String password, int authType, int mApnTypeBitmask2, int protocol, int roamingProtocol, boolean carrierEnabled, int networkTypeBitmask, int profileId, boolean modemCognitive, int maxConns, int waitTime, int maxConnsTime, int mtu, int mvnoType, String mvnoMatchData, int apnSetId, int carrierId, int skip464xlat) {
        return new Builder().setId(id).setOperatorNumeric(operatorNumeric).setEntryName(entryName).setApnName(apnName).setProxyAddress(proxyAddress).setProxyPort(proxyPort).setMmsc(mmsc).setMmsProxyAddress(mmsProxyAddress).setMmsProxyPort(mmsProxyPort).setUser(user).setPassword(password).setAuthType(authType).setApnTypeBitmask(mApnTypeBitmask2).setProtocol(protocol).setRoamingProtocol(roamingProtocol).setCarrierEnabled(carrierEnabled).setNetworkTypeBitmask(networkTypeBitmask).setProfileId(profileId).setModemCognitive(modemCognitive).setMaxConns(maxConns).setWaitTime(waitTime).setMaxConnsTime(maxConnsTime).setMtu(mtu).setMvnoType(mvnoType).setMvnoMatchData(mvnoMatchData).setApnSetId(apnSetId).setCarrierId(carrierId).setSkip464Xlat(skip464xlat).buildWithoutCheck();
    }

    public static ApnSetting makeApnSetting(int id, String operatorNumeric, String entryName, String apnName, String proxyAddress, int proxyPort, Uri mmsc, String mmsProxyAddress, int mmsProxyPort, String user, String password, int authType, int mApnTypeBitmask2, int protocol, int roamingProtocol, boolean carrierEnabled, int networkTypeBitmask, int profileId, boolean modemCognitive, int maxConns, int waitTime, int maxConnsTime, int mtu, int mvnoType, String mvnoMatchData) {
        return makeApnSetting(id, operatorNumeric, entryName, apnName, proxyAddress, proxyPort, mmsc, mmsProxyAddress, mmsProxyPort, user, password, authType, mApnTypeBitmask2, protocol, roamingProtocol, carrierEnabled, networkTypeBitmask, profileId, modemCognitive, maxConns, waitTime, maxConnsTime, mtu, mvnoType, mvnoMatchData, 0, -1, -1);
    }

    public static ApnSetting makeApnSetting(Cursor cursor) {
        int networkTypeBitmask;
        int apnTypesBitmask = getApnTypesBitmaskFromString(cursor.getString(cursor.getColumnIndexOrThrow("type")));
        int networkTypeBitmask2 = cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Carriers.NETWORK_TYPE_BITMASK));
        if (networkTypeBitmask2 == 0) {
            networkTypeBitmask = ServiceState.convertBearerBitmaskToNetworkTypeBitmask(cursor.getInt(cursor.getColumnIndexOrThrow("bearer_bitmask")));
        } else {
            networkTypeBitmask = networkTypeBitmask2;
        }
        return makeApnSetting(cursor.getInt(cursor.getColumnIndexOrThrow("_id")), cursor.getString(cursor.getColumnIndexOrThrow("numeric")), cursor.getString(cursor.getColumnIndexOrThrow("name")), cursor.getString(cursor.getColumnIndexOrThrow("apn")), cursor.getString(cursor.getColumnIndexOrThrow("proxy")), portFromString(cursor.getString(cursor.getColumnIndexOrThrow("port"))), UriFromString(cursor.getString(cursor.getColumnIndexOrThrow("mmsc"))), cursor.getString(cursor.getColumnIndexOrThrow("mmsproxy")), portFromString(cursor.getString(cursor.getColumnIndexOrThrow("mmsport"))), cursor.getString(cursor.getColumnIndexOrThrow("user")), cursor.getString(cursor.getColumnIndexOrThrow("password")), cursor.getInt(cursor.getColumnIndexOrThrow("authtype")), apnTypesBitmask, getProtocolIntFromString(cursor.getString(cursor.getColumnIndexOrThrow("protocol"))), getProtocolIntFromString(cursor.getString(cursor.getColumnIndexOrThrow("roaming_protocol"))), cursor.getInt(cursor.getColumnIndexOrThrow("carrier_enabled")) == 1, networkTypeBitmask, cursor.getInt(cursor.getColumnIndexOrThrow("profile_id")), cursor.getInt(cursor.getColumnIndexOrThrow("modem_cognitive")) == 1, cursor.getInt(cursor.getColumnIndexOrThrow("max_conns")), cursor.getInt(cursor.getColumnIndexOrThrow("wait_time")), cursor.getInt(cursor.getColumnIndexOrThrow("max_conns_time")), cursor.getInt(cursor.getColumnIndexOrThrow("mtu")), getMvnoTypeIntFromString(cursor.getString(cursor.getColumnIndexOrThrow("mvno_type"))), cursor.getString(cursor.getColumnIndexOrThrow("mvno_match_data")), cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Carriers.APN_SET_ID)), cursor.getInt(cursor.getColumnIndexOrThrow("carrier_id")), cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Carriers.SKIP_464XLAT)));
    }

    public static ApnSetting makeApnSetting(ApnSetting apn) {
        return makeApnSetting(apn.mId, apn.mOperatorNumeric, apn.mEntryName, apn.mApnName, apn.mProxyAddress, apn.mProxyPort, apn.mMmsc, apn.mMmsProxyAddress, apn.mMmsProxyPort, apn.mUser, apn.mPassword, apn.mAuthType, apn.mApnTypeBitmask, apn.mProtocol, apn.mRoamingProtocol, apn.mCarrierEnabled, apn.mNetworkTypeBitmask, apn.mProfileId, apn.mPersistent, apn.mMaxConns, apn.mWaitTime, apn.mMaxConnsTime, apn.mMtu, apn.mMvnoType, apn.mMvnoMatchData, apn.mApnSetId, apn.mCarrierId, apn.mSkip464Xlat);
    }

    public static ApnSetting fromString(String data) {
        int version;
        String data2;
        int authType;
        int skip464xlat;
        int carrierId;
        int apnSetId;
        String mvnoMatchData;
        String mvnoType;
        int mtu;
        int maxConnsTime;
        int waitTime;
        int maxConns;
        boolean modemCognitive;
        int profileId;
        boolean carrierEnabled;
        String roamingProtocol;
        String protocol;
        String[] typeArray;
        int bearerBitmask;
        int networkTypeBitmask;
        char c;
        Exception e;
        if (data == null) {
            return null;
        }
        if (data.matches("^\\[ApnSettingV7\\]\\s*.*")) {
            version = 7;
            data2 = data.replaceFirst(V7_FORMAT_REGEX, "");
        } else if (data.matches("^\\[ApnSettingV6\\]\\s*.*")) {
            version = 6;
            data2 = data.replaceFirst(V6_FORMAT_REGEX, "");
        } else if (data.matches("^\\[ApnSettingV5\\]\\s*.*")) {
            version = 5;
            data2 = data.replaceFirst(V5_FORMAT_REGEX, "");
        } else if (data.matches("^\\[ApnSettingV4\\]\\s*.*")) {
            version = 4;
            data2 = data.replaceFirst(V4_FORMAT_REGEX, "");
        } else if (data.matches("^\\[ApnSettingV3\\]\\s*.*")) {
            version = 3;
            data2 = data.replaceFirst(V3_FORMAT_REGEX, "");
        } else if (data.matches("^\\[ApnSettingV2\\]\\s*.*")) {
            version = 2;
            data2 = data.replaceFirst(V2_FORMAT_REGEX, "");
        } else {
            version = 1;
            data2 = data;
        }
        String[] a = data2.split("\\s*,\\s*", -1);
        if (a.length < 14) {
            return null;
        }
        try {
            authType = Integer.parseInt(a[12]);
        } catch (NumberFormatException e2) {
            authType = 0;
        }
        int profileId2 = 0;
        boolean modemCognitive2 = false;
        int maxConns2 = 0;
        int waitTime2 = 0;
        int maxConnsTime2 = 0;
        int mtu2 = 0;
        String mvnoType2 = "";
        String mvnoMatchData2 = "";
        int apnSetId2 = 0;
        int carrierId2 = -1;
        int networkTypeBitmask2 = 0;
        if (version == 1) {
            String[] typeArray2 = new String[(a.length - 13)];
            System.arraycopy(a, 13, typeArray2, 0, a.length - 13);
            roamingProtocol = PROTOCOL_INT_MAP.get(0);
            carrierEnabled = true;
            typeArray = typeArray2;
            profileId = 0;
            modemCognitive = false;
            maxConns = 0;
            waitTime = 0;
            maxConnsTime = 0;
            mtu = 0;
            mvnoType = mvnoType2;
            mvnoMatchData = mvnoMatchData2;
            apnSetId = 0;
            carrierId = -1;
            skip464xlat = -1;
            protocol = PROTOCOL_INT_MAP.get(0);
            bearerBitmask = 0;
        } else if (a.length < 18) {
            return null;
        } else {
            String[] typeArray3 = a[13].split("\\s*\\|\\s*");
            protocol = a[14];
            String roamingProtocol2 = a[15];
            boolean carrierEnabled2 = Boolean.parseBoolean(a[16]);
            int bearerBitmask2 = ServiceState.getBitmaskFromString(a[17]);
            if (a.length > 22) {
                modemCognitive2 = Boolean.parseBoolean(a[19]);
                try {
                    profileId2 = Integer.parseInt(a[18]);
                    maxConns2 = Integer.parseInt(a[20]);
                    waitTime2 = Integer.parseInt(a[21]);
                    maxConnsTime2 = Integer.parseInt(a[22]);
                } catch (NumberFormatException e3) {
                }
            }
            if (a.length > 23) {
                try {
                    mtu2 = Integer.parseInt(a[23]);
                } catch (NumberFormatException e4) {
                }
            }
            if (a.length > 25) {
                String mvnoType3 = a[24];
                mvnoMatchData2 = a[25];
                mvnoType2 = mvnoType3;
            }
            if (a.length > 26) {
                networkTypeBitmask2 = ServiceState.getBitmaskFromString(a[26]);
            }
            if (a.length > 27) {
                apnSetId2 = Integer.parseInt(a[27]);
            }
            if (a.length > 28) {
                carrierId2 = Integer.parseInt(a[28]);
            }
            if (a.length > 29) {
                try {
                    skip464xlat = Integer.parseInt(a[29]);
                    roamingProtocol = roamingProtocol2;
                    typeArray = typeArray3;
                    profileId = profileId2;
                    modemCognitive = modemCognitive2;
                    maxConns = maxConns2;
                    waitTime = waitTime2;
                    maxConnsTime = maxConnsTime2;
                    mtu = mtu2;
                    mvnoType = mvnoType2;
                    mvnoMatchData = mvnoMatchData2;
                    apnSetId = apnSetId2;
                    carrierId = carrierId2;
                    bearerBitmask = bearerBitmask2;
                    carrierEnabled = carrierEnabled2;
                } catch (NumberFormatException e5) {
                }
            }
            roamingProtocol = roamingProtocol2;
            typeArray = typeArray3;
            profileId = profileId2;
            modemCognitive = modemCognitive2;
            maxConns = maxConns2;
            waitTime = waitTime2;
            maxConnsTime = maxConnsTime2;
            mtu = mtu2;
            mvnoType = mvnoType2;
            mvnoMatchData = mvnoMatchData2;
            apnSetId = apnSetId2;
            carrierId = carrierId2;
            skip464xlat = -1;
            bearerBitmask = bearerBitmask2;
            carrierEnabled = carrierEnabled2;
        }
        if (networkTypeBitmask2 == 0) {
            networkTypeBitmask = ServiceState.convertBearerBitmaskToNetworkTypeBitmask(bearerBitmask);
        } else {
            networkTypeBitmask = networkTypeBitmask2;
        }
        try {
            if (sMethodFromStringEx != null) {
                Method method = sMethodFromStringEx;
                Object[] objArr = new Object[18];
                objArr[0] = a;
                c = 1;
                try {
                    objArr[1] = Integer.valueOf(authType);
                    objArr[2] = Integer.valueOf(getApnTypesBitmaskFromString(TextUtils.join(SmsManager.REGEX_PREFIX_DELIMITER, typeArray)));
                    objArr[3] = Integer.valueOf(getProtocolIntFromString(protocol));
                    objArr[4] = Integer.valueOf(getProtocolIntFromString(roamingProtocol));
                    objArr[5] = Boolean.valueOf(carrierEnabled);
                    objArr[6] = Integer.valueOf(networkTypeBitmask);
                    objArr[7] = Integer.valueOf(profileId);
                    objArr[8] = Boolean.valueOf(modemCognitive);
                    objArr[9] = Integer.valueOf(maxConns);
                    objArr[10] = Integer.valueOf(waitTime);
                    objArr[11] = Integer.valueOf(maxConnsTime);
                    objArr[12] = Integer.valueOf(mtu);
                    objArr[13] = Integer.valueOf(getMvnoTypeIntFromString(mvnoType));
                    objArr[14] = mvnoMatchData;
                    objArr[15] = Integer.valueOf(apnSetId);
                    objArr[16] = Integer.valueOf(carrierId);
                    objArr[17] = Integer.valueOf(skip464xlat);
                    return (ApnSetting) method.invoke(null, objArr);
                } catch (Exception e6) {
                    e = e6;
                    Rlog.d(LOG_TAG, e.toString());
                    return makeApnSetting(-1, a[10] + a[11], a[0], a[c], a[2], portFromString(a[3]), UriFromString(a[7]), a[8], portFromString(a[9]), a[4], a[5], authType, getApnTypesBitmaskFromString(TextUtils.join(SmsManager.REGEX_PREFIX_DELIMITER, typeArray)), getProtocolIntFromString(protocol), getProtocolIntFromString(roamingProtocol), carrierEnabled, networkTypeBitmask, profileId, modemCognitive, maxConns, waitTime, maxConnsTime, mtu, getMvnoTypeIntFromString(mvnoType), mvnoMatchData, apnSetId, carrierId, skip464xlat);
                }
            } else {
                c = 1;
                return makeApnSetting(-1, a[10] + a[11], a[0], a[c], a[2], portFromString(a[3]), UriFromString(a[7]), a[8], portFromString(a[9]), a[4], a[5], authType, getApnTypesBitmaskFromString(TextUtils.join(SmsManager.REGEX_PREFIX_DELIMITER, typeArray)), getProtocolIntFromString(protocol), getProtocolIntFromString(roamingProtocol), carrierEnabled, networkTypeBitmask, profileId, modemCognitive, maxConns, waitTime, maxConnsTime, mtu, getMvnoTypeIntFromString(mvnoType), mvnoMatchData, apnSetId, carrierId, skip464xlat);
            }
        } catch (Exception e7) {
            e = e7;
            c = 1;
            Rlog.d(LOG_TAG, e.toString());
            return makeApnSetting(-1, a[10] + a[11], a[0], a[c], a[2], portFromString(a[3]), UriFromString(a[7]), a[8], portFromString(a[9]), a[4], a[5], authType, getApnTypesBitmaskFromString(TextUtils.join(SmsManager.REGEX_PREFIX_DELIMITER, typeArray)), getProtocolIntFromString(protocol), getProtocolIntFromString(roamingProtocol), carrierEnabled, networkTypeBitmask, profileId, modemCognitive, maxConns, waitTime, maxConnsTime, mtu, getMvnoTypeIntFromString(mvnoType), mvnoMatchData, apnSetId, carrierId, skip464xlat);
        }
    }

    public static List<ApnSetting> arrayFromString(String data) {
        List<ApnSetting> retVal = new ArrayList<>();
        if (TextUtils.isEmpty(data)) {
            return retVal;
        }
        for (String apnString : data.split("\\s*;\\s*")) {
            ApnSetting apn = fromString(apnString);
            if (apn != null) {
                retVal.add(apn);
            }
        }
        return retVal;
    }

    public String toString() {
        return "[ApnSettingV7] " + this.mEntryName + ", " + this.mId + ", " + this.mOperatorNumeric + ", " + this.mApnName + ", " + this.mProxyAddress + ", " + UriToString(this.mMmsc) + ", " + this.mMmsProxyAddress + ", " + portToString(this.mMmsProxyPort) + ", " + portToString(this.mProxyPort) + ", " + this.mAuthType + ", " + TextUtils.join(" | ", getApnTypesStringFromBitmask(this.mApnTypeBitmask).split(SmsManager.REGEX_PREFIX_DELIMITER)) + ", " + PROTOCOL_INT_MAP.get(Integer.valueOf(this.mProtocol)) + ", " + PROTOCOL_INT_MAP.get(Integer.valueOf(this.mRoamingProtocol)) + ", " + this.mCarrierEnabled + ", " + this.mProfileId + ", " + this.mPersistent + ", " + this.mMaxConns + ", " + this.mWaitTime + ", " + this.mMaxConnsTime + ", " + this.mMtu + ", " + MVNO_TYPE_INT_MAP.get(Integer.valueOf(this.mMvnoType)) + ", " + this.mMvnoMatchData + ", " + this.mPermanentFailed + ", " + this.mNetworkTypeBitmask + ", " + this.mApnSetId + ", " + this.mCarrierId + ", " + this.mSkip464Xlat;
    }

    public boolean hasMvnoParams() {
        return !TextUtils.isEmpty(getMvnoTypeStringFromInt(this.mMvnoType)) && !TextUtils.isEmpty(this.mMvnoMatchData);
    }

    /* access modifiers changed from: protected */
    public boolean hasApnType(int type) {
        return (this.mApnTypeBitmask & type) == type;
    }

    public boolean canHandleType(int type) {
        if (this.mCarrierEnabled && hasApnType(type)) {
            return true;
        }
        return false;
    }

    private boolean typeSameAny(ApnSetting first, ApnSetting second) {
        if ((first.mApnTypeBitmask & second.mApnTypeBitmask) != 0) {
            return true;
        }
        return false;
    }

    public boolean equals(Object o) {
        if (!(o instanceof ApnSetting)) {
            return false;
        }
        ApnSetting other = (ApnSetting) o;
        if (!this.mEntryName.equals(other.mEntryName) || !Objects.equals(Integer.valueOf(this.mId), Integer.valueOf(other.mId)) || !Objects.equals(this.mOperatorNumeric, other.mOperatorNumeric) || !Objects.equals(this.mApnName, other.mApnName) || !Objects.equals(this.mProxyAddress, other.mProxyAddress) || !Objects.equals(this.mMmsc, other.mMmsc) || !Objects.equals(this.mMmsProxyAddress, other.mMmsProxyAddress) || !Objects.equals(Integer.valueOf(this.mMmsProxyPort), Integer.valueOf(other.mMmsProxyPort)) || !Objects.equals(Integer.valueOf(this.mProxyPort), Integer.valueOf(other.mProxyPort)) || !Objects.equals(this.mUser, other.mUser) || !Objects.equals(this.mPassword, other.mPassword) || !Objects.equals(Integer.valueOf(this.mAuthType), Integer.valueOf(other.mAuthType)) || !Objects.equals(Integer.valueOf(this.mApnTypeBitmask), Integer.valueOf(other.mApnTypeBitmask)) || !Objects.equals(Integer.valueOf(this.mProtocol), Integer.valueOf(other.mProtocol)) || !Objects.equals(Integer.valueOf(this.mRoamingProtocol), Integer.valueOf(other.mRoamingProtocol)) || !Objects.equals(Boolean.valueOf(this.mCarrierEnabled), Boolean.valueOf(other.mCarrierEnabled)) || !Objects.equals(Integer.valueOf(this.mProfileId), Integer.valueOf(other.mProfileId)) || !Objects.equals(Boolean.valueOf(this.mPersistent), Boolean.valueOf(other.mPersistent)) || !Objects.equals(Integer.valueOf(this.mMaxConns), Integer.valueOf(other.mMaxConns)) || !Objects.equals(Integer.valueOf(this.mWaitTime), Integer.valueOf(other.mWaitTime)) || !Objects.equals(Integer.valueOf(this.mMaxConnsTime), Integer.valueOf(other.mMaxConnsTime)) || !Objects.equals(Integer.valueOf(this.mMtu), Integer.valueOf(other.mMtu)) || !Objects.equals(Integer.valueOf(this.mMvnoType), Integer.valueOf(other.mMvnoType)) || !Objects.equals(this.mMvnoMatchData, other.mMvnoMatchData) || !Objects.equals(Integer.valueOf(this.mNetworkTypeBitmask), Integer.valueOf(other.mNetworkTypeBitmask)) || !Objects.equals(Integer.valueOf(this.mApnSetId), Integer.valueOf(other.mApnSetId)) || !Objects.equals(Integer.valueOf(this.mCarrierId), Integer.valueOf(other.mCarrierId)) || !Objects.equals(Integer.valueOf(this.mSkip464Xlat), Integer.valueOf(other.mSkip464Xlat))) {
            return false;
        }
        return true;
    }

    public boolean equals(Object o, boolean isDataRoaming) {
        if (!(o instanceof ApnSetting)) {
            return false;
        }
        ApnSetting other = (ApnSetting) o;
        if (!this.mEntryName.equals(other.mEntryName) || !Objects.equals(this.mOperatorNumeric, other.mOperatorNumeric) || !Objects.equals(this.mApnName, other.mApnName) || !Objects.equals(this.mProxyAddress, other.mProxyAddress) || !Objects.equals(this.mMmsc, other.mMmsc) || !Objects.equals(this.mMmsProxyAddress, other.mMmsProxyAddress) || !Objects.equals(Integer.valueOf(this.mMmsProxyPort), Integer.valueOf(other.mMmsProxyPort)) || !Objects.equals(Integer.valueOf(this.mProxyPort), Integer.valueOf(other.mProxyPort)) || !Objects.equals(this.mUser, other.mUser) || !Objects.equals(this.mPassword, other.mPassword) || !Objects.equals(Integer.valueOf(this.mAuthType), Integer.valueOf(other.mAuthType)) || !Objects.equals(Integer.valueOf(this.mApnTypeBitmask), Integer.valueOf(other.mApnTypeBitmask))) {
            return false;
        }
        if (!isDataRoaming && !Objects.equals(Integer.valueOf(this.mProtocol), Integer.valueOf(other.mProtocol))) {
            return false;
        }
        if ((!isDataRoaming || Objects.equals(Integer.valueOf(this.mRoamingProtocol), Integer.valueOf(other.mRoamingProtocol))) && Objects.equals(Boolean.valueOf(this.mCarrierEnabled), Boolean.valueOf(other.mCarrierEnabled)) && Objects.equals(Integer.valueOf(this.mProfileId), Integer.valueOf(other.mProfileId)) && Objects.equals(Boolean.valueOf(this.mPersistent), Boolean.valueOf(other.mPersistent)) && Objects.equals(Integer.valueOf(this.mMaxConns), Integer.valueOf(other.mMaxConns)) && Objects.equals(Integer.valueOf(this.mWaitTime), Integer.valueOf(other.mWaitTime)) && Objects.equals(Integer.valueOf(this.mMaxConnsTime), Integer.valueOf(other.mMaxConnsTime)) && Objects.equals(Integer.valueOf(this.mMtu), Integer.valueOf(other.mMtu)) && Objects.equals(Integer.valueOf(this.mMvnoType), Integer.valueOf(other.mMvnoType)) && Objects.equals(this.mMvnoMatchData, other.mMvnoMatchData) && Objects.equals(Integer.valueOf(this.mApnSetId), Integer.valueOf(other.mApnSetId)) && Objects.equals(Integer.valueOf(this.mCarrierId), Integer.valueOf(other.mCarrierId)) && Objects.equals(Integer.valueOf(this.mSkip464Xlat), Integer.valueOf(other.mSkip464Xlat))) {
            return true;
        }
        return false;
    }

    public boolean similar(ApnSetting other) {
        return !canHandleType(8) && !other.canHandleType(8) && Objects.equals(this.mApnName, other.mApnName) && !typeSameAny(this, other) && xorEquals(this.mProxyAddress, other.mProxyAddress) && xorEqualsInt(this.mProxyPort, other.mProxyPort) && xorEquals(Integer.valueOf(this.mProtocol), Integer.valueOf(other.mProtocol)) && xorEquals(Integer.valueOf(this.mRoamingProtocol), Integer.valueOf(other.mRoamingProtocol)) && Objects.equals(Boolean.valueOf(this.mCarrierEnabled), Boolean.valueOf(other.mCarrierEnabled)) && Objects.equals(Integer.valueOf(this.mProfileId), Integer.valueOf(other.mProfileId)) && Objects.equals(Integer.valueOf(this.mMvnoType), Integer.valueOf(other.mMvnoType)) && Objects.equals(this.mMvnoMatchData, other.mMvnoMatchData) && xorEquals(this.mMmsc, other.mMmsc) && xorEquals(this.mMmsProxyAddress, other.mMmsProxyAddress) && xorEqualsInt(this.mMmsProxyPort, other.mMmsProxyPort) && Objects.equals(Integer.valueOf(this.mNetworkTypeBitmask), Integer.valueOf(other.mNetworkTypeBitmask)) && Objects.equals(Integer.valueOf(this.mApnSetId), Integer.valueOf(other.mApnSetId)) && Objects.equals(Integer.valueOf(this.mCarrierId), Integer.valueOf(other.mCarrierId)) && Objects.equals(Integer.valueOf(this.mSkip464Xlat), Integer.valueOf(other.mSkip464Xlat));
    }

    private boolean xorEquals(Object first, Object second) {
        return first == null || second == null || first.equals(second);
    }

    private boolean xorEqualsInt(int first, int second) {
        return first == -1 || second == -1 || Objects.equals(Integer.valueOf(first), Integer.valueOf(second));
    }

    private String nullToEmpty(String stringValue) {
        return stringValue == null ? "" : stringValue;
    }

    public ContentValues toContentValues() {
        ContentValues apnValue = new ContentValues();
        apnValue.put("numeric", nullToEmpty(this.mOperatorNumeric));
        apnValue.put("name", nullToEmpty(this.mEntryName));
        apnValue.put("apn", nullToEmpty(this.mApnName));
        apnValue.put("proxy", nullToEmpty(this.mProxyAddress));
        apnValue.put("port", nullToEmpty(portToString(this.mProxyPort)));
        apnValue.put("mmsc", nullToEmpty(UriToString(this.mMmsc)));
        apnValue.put("mmsport", nullToEmpty(portToString(this.mMmsProxyPort)));
        apnValue.put("mmsproxy", nullToEmpty(this.mMmsProxyAddress));
        apnValue.put("user", nullToEmpty(this.mUser));
        apnValue.put("password", nullToEmpty(this.mPassword));
        apnValue.put("authtype", Integer.valueOf(this.mAuthType));
        apnValue.put("type", nullToEmpty(getApnTypesStringFromBitmask(this.mApnTypeBitmask)));
        apnValue.put("protocol", getProtocolStringFromInt(this.mProtocol));
        apnValue.put("roaming_protocol", getProtocolStringFromInt(this.mRoamingProtocol));
        apnValue.put("carrier_enabled", Boolean.valueOf(this.mCarrierEnabled));
        apnValue.put("mvno_type", getMvnoTypeStringFromInt(this.mMvnoType));
        apnValue.put(Telephony.Carriers.NETWORK_TYPE_BITMASK, Integer.valueOf(this.mNetworkTypeBitmask));
        apnValue.put("carrier_id", Integer.valueOf(this.mCarrierId));
        apnValue.put(Telephony.Carriers.SKIP_464XLAT, Integer.valueOf(this.mSkip464Xlat));
        return apnValue;
    }

    public List<Integer> getApnTypes() {
        List<Integer> types = new ArrayList<>();
        for (Integer type : APN_TYPE_INT_MAP.keySet()) {
            if ((this.mApnTypeBitmask & type.intValue()) == type.intValue()) {
                types.add(type);
            }
        }
        return types;
    }

    public static String getApnTypesStringFromBitmask(int apnTypeBitmask) {
        List<String> types = new ArrayList<>();
        for (Integer type : APN_TYPE_INT_MAP.keySet()) {
            if ((type.intValue() & apnTypeBitmask) == type.intValue()) {
                types.add(APN_TYPE_INT_MAP.get(type));
            }
        }
        return TextUtils.join(SmsManager.REGEX_PREFIX_DELIMITER, types);
    }

    public static String getApnTypeString(int apnType) {
        try {
            if (sMethodGetApnTypeStringEx != null) {
                Bundle b = (Bundle) sMethodGetApnTypeStringEx.invoke(null, Integer.valueOf(apnType));
                if (b != null) {
                    return b.getString("result");
                }
            }
        } catch (Exception e) {
            Rlog.d(LOG_TAG, e.toString());
        }
        if (apnType == 255) {
            return "*";
        }
        String apnTypeString = APN_TYPE_INT_MAP.get(Integer.valueOf(apnType));
        return apnTypeString == null ? "Unknown" : apnTypeString;
    }

    public static int getApnTypesBitmaskFromString(String types) {
        try {
            if (sMethodGetApnTypesBitmaskFromStringEx != null) {
                Bundle b = (Bundle) sMethodGetApnTypesBitmaskFromStringEx.invoke(null, types);
                if (b != null) {
                    return b.getInt("result");
                }
            }
        } catch (Exception e) {
            Rlog.d(LOG_TAG, e.toString());
        }
        if (TextUtils.isEmpty(types)) {
            return 255;
        }
        int result = 0;
        for (String str : types.split(SmsManager.REGEX_PREFIX_DELIMITER)) {
            Integer type = APN_TYPE_STRING_MAP.get(str.toLowerCase());
            if (type != null) {
                result |= type.intValue();
            }
        }
        return result;
    }

    public static int getMvnoTypeIntFromString(String mvnoType) {
        Integer mvnoTypeInt = MVNO_TYPE_STRING_MAP.get(TextUtils.isEmpty(mvnoType) ? mvnoType : mvnoType.toLowerCase());
        if (mvnoTypeInt == null) {
            return -1;
        }
        return mvnoTypeInt.intValue();
    }

    public static String getMvnoTypeStringFromInt(int mvnoType) {
        String mvnoTypeString = MVNO_TYPE_INT_MAP.get(Integer.valueOf(mvnoType));
        return mvnoTypeString == null ? "" : mvnoTypeString;
    }

    public static int getProtocolIntFromString(String protocol) {
        Integer protocolInt = PROTOCOL_STRING_MAP.get(protocol);
        if (protocolInt == null) {
            return -1;
        }
        return protocolInt.intValue();
    }

    public static String getProtocolStringFromInt(int protocol) {
        String protocolString = PROTOCOL_INT_MAP.get(Integer.valueOf(protocol));
        return protocolString == null ? "" : protocolString;
    }

    protected static Uri UriFromString(String uri) {
        if (TextUtils.isEmpty(uri)) {
            return null;
        }
        return Uri.parse(uri);
    }

    protected static String UriToString(Uri uri) {
        if (uri == null) {
            return null;
        }
        return uri.toString();
    }

    public static InetAddress inetAddressFromString(String inetAddress) {
        if (TextUtils.isEmpty(inetAddress)) {
            return null;
        }
        try {
            return InetAddress.getByName(inetAddress);
        } catch (UnknownHostException e) {
            Log.e(LOG_TAG, "Can't parse InetAddress from string: unknown host.");
            return null;
        }
    }

    public static String inetAddressToString(InetAddress inetAddress) {
        if (inetAddress == null) {
            return null;
        }
        String inetAddressString = inetAddress.toString();
        if (TextUtils.isEmpty(inetAddressString)) {
            return null;
        }
        String hostName = inetAddressString.substring(0, inetAddressString.indexOf("/"));
        String address = inetAddressString.substring(inetAddressString.indexOf("/") + 1);
        if (!TextUtils.isEmpty(hostName) || !TextUtils.isEmpty(address)) {
            return TextUtils.isEmpty(hostName) ? address : hostName;
        }
        return null;
    }

    protected static int portFromString(String strPort) {
        if (TextUtils.isEmpty(strPort)) {
            return -1;
        }
        try {
            return Integer.parseInt(strPort);
        } catch (NumberFormatException e) {
            Log.e(LOG_TAG, "Can't parse port from String");
            return -1;
        }
    }

    protected static String portToString(int port) {
        if (port == -1) {
            return null;
        }
        return Integer.toString(port);
    }

    public boolean canSupportNetworkType(int networkType) {
        if (networkType != 16 || (((long) this.mNetworkTypeBitmask) & 3) == 0) {
            return ServiceState.bitmaskHasTech(this.mNetworkTypeBitmask, networkType);
        }
        return true;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mId);
        dest.writeString(this.mOperatorNumeric);
        dest.writeString(this.mEntryName);
        dest.writeString(this.mApnName);
        dest.writeString(this.mProxyAddress);
        dest.writeInt(this.mProxyPort);
        dest.writeValue(this.mMmsc);
        dest.writeString(this.mMmsProxyAddress);
        dest.writeInt(this.mMmsProxyPort);
        dest.writeString(this.mUser);
        dest.writeString(this.mPassword);
        dest.writeInt(this.mAuthType);
        dest.writeInt(this.mApnTypeBitmask);
        dest.writeInt(this.mProtocol);
        dest.writeInt(this.mRoamingProtocol);
        dest.writeBoolean(this.mCarrierEnabled);
        dest.writeInt(this.mMvnoType);
        dest.writeInt(this.mNetworkTypeBitmask);
        dest.writeInt(this.mApnSetId);
        dest.writeInt(this.mCarrierId);
        dest.writeInt(this.mSkip464Xlat);
    }

    /* access modifiers changed from: private */
    public static ApnSetting readFromParcel(Parcel in) {
        return makeApnSetting(in.readInt(), in.readString(), in.readString(), in.readString(), in.readString(), in.readInt(), (Uri) in.readValue(Uri.class.getClassLoader()), in.readString(), in.readInt(), in.readString(), in.readString(), in.readInt(), in.readInt(), in.readInt(), in.readInt(), in.readBoolean(), in.readInt(), 0, false, 0, 0, 0, 0, in.readInt(), null, in.readInt(), in.readInt(), in.readInt());
    }

    public static class Builder {
        private String mApnName;
        private int mApnSetId;
        private int mApnTypeBitmask;
        private int mAuthType;
        private boolean mCarrierEnabled;
        private int mCarrierId = -1;
        private String mEntryName;
        private int mId;
        private int mMaxConns;
        private int mMaxConnsTime;
        private String mMmsProxyAddress;
        private int mMmsProxyPort = -1;
        private Uri mMmsc;
        private boolean mModemCognitive;
        private int mMtu;
        private String mMvnoMatchData;
        private int mMvnoType = -1;
        private int mNetworkTypeBitmask;
        private String mOperatorNumeric;
        private String mPassword;
        private int mProfileId;
        private int mProtocol = -1;
        private String mProxyAddress;
        private int mProxyPort = -1;
        private int mRoamingProtocol = -1;
        private int mSkip464Xlat = -1;
        private String mUser;
        private int mWaitTime;

        public Builder setId(int id) {
            this.mId = id;
            return this;
        }

        public Builder setMtu(int mtu) {
            this.mMtu = mtu;
            return this;
        }

        public Builder setProfileId(int profileId) {
            this.mProfileId = profileId;
            return this;
        }

        public Builder setModemCognitive(boolean modemCognitive) {
            this.mModemCognitive = modemCognitive;
            return this;
        }

        public Builder setMaxConns(int maxConns) {
            this.mMaxConns = maxConns;
            return this;
        }

        public Builder setWaitTime(int waitTime) {
            this.mWaitTime = waitTime;
            return this;
        }

        public Builder setMaxConnsTime(int maxConnsTime) {
            this.mMaxConnsTime = maxConnsTime;
            return this;
        }

        public Builder setMvnoMatchData(String mvnoMatchData) {
            this.mMvnoMatchData = mvnoMatchData;
            return this;
        }

        public Builder setApnSetId(int apnSetId) {
            this.mApnSetId = apnSetId;
            return this;
        }

        public Builder setEntryName(String entryName) {
            this.mEntryName = entryName;
            return this;
        }

        public Builder setApnName(String apnName) {
            this.mApnName = apnName;
            return this;
        }

        @Deprecated
        public Builder setProxyAddress(InetAddress proxy) {
            this.mProxyAddress = ApnSetting.inetAddressToString(proxy);
            return this;
        }

        public Builder setProxyAddress(String proxy) {
            this.mProxyAddress = proxy;
            return this;
        }

        public Builder setProxyPort(int port) {
            this.mProxyPort = port;
            return this;
        }

        public Builder setMmsc(Uri mmsc) {
            this.mMmsc = mmsc;
            return this;
        }

        @Deprecated
        public Builder setMmsProxyAddress(InetAddress mmsProxy) {
            this.mMmsProxyAddress = ApnSetting.inetAddressToString(mmsProxy);
            return this;
        }

        public Builder setMmsProxyAddress(String mmsProxy) {
            this.mMmsProxyAddress = mmsProxy;
            return this;
        }

        public Builder setMmsProxyPort(int mmsPort) {
            this.mMmsProxyPort = mmsPort;
            return this;
        }

        public Builder setUser(String user) {
            this.mUser = user;
            return this;
        }

        public Builder setPassword(String password) {
            this.mPassword = password;
            return this;
        }

        public Builder setAuthType(int authType) {
            this.mAuthType = authType;
            return this;
        }

        public Builder setApnTypeBitmask(int apnTypeBitmask) {
            this.mApnTypeBitmask = apnTypeBitmask;
            return this;
        }

        public Builder setOperatorNumeric(String operatorNumeric) {
            this.mOperatorNumeric = operatorNumeric;
            return this;
        }

        public Builder setProtocol(int protocol) {
            this.mProtocol = protocol;
            return this;
        }

        public Builder setRoamingProtocol(int roamingProtocol) {
            this.mRoamingProtocol = roamingProtocol;
            return this;
        }

        public Builder setCarrierEnabled(boolean carrierEnabled) {
            this.mCarrierEnabled = carrierEnabled;
            return this;
        }

        public Builder setNetworkTypeBitmask(int networkTypeBitmask) {
            this.mNetworkTypeBitmask = networkTypeBitmask;
            return this;
        }

        public Builder setMvnoType(int mvnoType) {
            this.mMvnoType = mvnoType;
            return this;
        }

        public Builder setCarrierId(int carrierId) {
            this.mCarrierId = carrierId;
            return this;
        }

        public Builder setSkip464Xlat(int skip464xlat) {
            this.mSkip464Xlat = skip464xlat;
            return this;
        }

        public ApnSetting build() {
            if ((this.mApnTypeBitmask & 255) == 0 || TextUtils.isEmpty(this.mApnName) || TextUtils.isEmpty(this.mEntryName)) {
                return null;
            }
            return new ApnSetting(this);
        }

        public ApnSetting buildWithoutCheck() {
            return new ApnSetting(this);
        }
    }
}
