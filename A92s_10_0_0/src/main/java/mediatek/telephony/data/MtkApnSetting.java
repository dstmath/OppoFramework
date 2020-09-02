package mediatek.telephony.data;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.ServiceState;
import android.telephony.data.ApnSetting;
import android.text.TextUtils;
import com.mediatek.internal.telephony.MtkPhoneConstants;
import java.util.Iterator;
import java.util.List;
import mediatek.telephony.MtkTelephony;

public class MtkApnSetting extends ApnSetting {
    private static final boolean DBG = true;
    private static final String LOG_TAG = "MtkApnSetting";
    public static final int MTK_TYPE_ALL = 63999;
    public static final int MVNO_TYPE_PNN = 4;
    public static final int TYPE_BIP = 16384;
    public static final int TYPE_RCS = 8192;
    public static final int TYPE_VSIM = 32768;
    public static final int TYPE_WAP = 2048;
    public static final int TYPE_XCAP = 4096;
    private static final boolean VDBG;
    public final int inactiveTimer;
    public final String oppoPaidSelect = "";

    static {
        boolean z;
        if (SystemProperties.get("ro.build.type").equals("eng")) {
            z = DBG;
        } else {
            z = false;
        }
        VDBG = z;
        APN_TYPE_STRING_MAP.put(MtkPhoneConstants.APN_TYPE_WAP, 2048);
        APN_TYPE_STRING_MAP.put(MtkPhoneConstants.APN_TYPE_XCAP, 4096);
        APN_TYPE_STRING_MAP.put(MtkPhoneConstants.APN_TYPE_RCS, 8192);
        APN_TYPE_STRING_MAP.put(MtkPhoneConstants.APN_TYPE_BIP, 16384);
        APN_TYPE_STRING_MAP.put(MtkPhoneConstants.APN_TYPE_VSIM, 32768);
        APN_TYPE_INT_MAP.put(2048, MtkPhoneConstants.APN_TYPE_WAP);
        APN_TYPE_INT_MAP.put(4096, MtkPhoneConstants.APN_TYPE_XCAP);
        APN_TYPE_INT_MAP.put(8192, MtkPhoneConstants.APN_TYPE_RCS);
        APN_TYPE_INT_MAP.put(16384, MtkPhoneConstants.APN_TYPE_BIP);
        APN_TYPE_INT_MAP.put(32768, MtkPhoneConstants.APN_TYPE_VSIM);
        MVNO_TYPE_STRING_MAP.put("pnn", 4);
        MVNO_TYPE_INT_MAP.put(4, "pnn");
    }

    public MtkApnSetting(int id, String operatorNumeric, String entryName, String apnName, String proxyAddress, int proxyPort, Uri mmsc, String mmsProxyAddress, int mmsProxyPort, String user, String password, int authType, int apnTypeBitmask, int protocol, int roamingProtocol, boolean carrierEnabled, int networkTypeBitmask, int profileId, boolean modemCognitive, int maxConns, int waitTime, int maxConnsTime, int mtu, int mvnoType, String mvnoMatchData, int apnSetId, int carrierId, int skip464xlat, int inactiveTimer2) {
        super(new ApnSetting.Builder().setId(id).setOperatorNumeric(operatorNumeric).setEntryName(entryName).setApnName(apnName).setProxyAddress(proxyAddress).setProxyPort(proxyPort).setMmsc(mmsc).setMmsProxyAddress(mmsProxyAddress).setMmsProxyPort(mmsProxyPort).setUser(user).setPassword(password).setAuthType(authType).setApnTypeBitmask(apnTypeBitmask).setProtocol(protocol).setRoamingProtocol(roamingProtocol).setCarrierEnabled(carrierEnabled).setNetworkTypeBitmask(networkTypeBitmask).setProfileId(profileId).setModemCognitive(DBG).setMaxConns(maxConns).setWaitTime(waitTime).setMaxConnsTime(maxConnsTime).setMtu(mtu).setMvnoType(mvnoType).setMvnoMatchData(mvnoMatchData).setApnSetId(apnSetId).setCarrierId(carrierId).setSkip464Xlat(skip464xlat));
        this.inactiveTimer = inactiveTimer2;
    }

    public static ApnSetting makeApnSetting(Cursor cursor, int mtu, int inactiveTimer2) {
        int networkTypeBitmask;
        int apnTypesBitmask = getApnTypesBitmaskFromString(cursor.getString(cursor.getColumnIndexOrThrow("type")));
        int networkTypeBitmask2 = cursor.getInt(cursor.getColumnIndexOrThrow(MtkTelephony.Carriers.NETWORK_TYPE_BITMASK));
        if (networkTypeBitmask2 == 0) {
            networkTypeBitmask = ServiceState.convertBearerBitmaskToNetworkTypeBitmask(cursor.getInt(cursor.getColumnIndexOrThrow(MtkTelephony.Carriers.BEARER_BITMASK)));
        } else {
            networkTypeBitmask = networkTypeBitmask2;
        }
        return new MtkApnSetting(cursor.getInt(cursor.getColumnIndexOrThrow("_id")), cursor.getString(cursor.getColumnIndexOrThrow(MtkTelephony.Carriers.NUMERIC)), cursor.getString(cursor.getColumnIndexOrThrow("name")), cursor.getString(cursor.getColumnIndexOrThrow("apn")), emptyToNull(cursor.getString(cursor.getColumnIndexOrThrow(MtkTelephony.Carriers.PROXY))), portFromString(cursor.getString(cursor.getColumnIndexOrThrow(MtkTelephony.Carriers.PORT))), UriFromString(cursor.getString(cursor.getColumnIndexOrThrow(MtkTelephony.Carriers.MMSC))), emptyToNull(cursor.getString(cursor.getColumnIndexOrThrow(MtkTelephony.Carriers.MMSPROXY))), portFromString(cursor.getString(cursor.getColumnIndexOrThrow(MtkTelephony.Carriers.MMSPORT))), cursor.getString(cursor.getColumnIndexOrThrow(MtkTelephony.Carriers.USER)), cursor.getString(cursor.getColumnIndexOrThrow(MtkTelephony.Carriers.PASSWORD)), cursor.getInt(cursor.getColumnIndexOrThrow(MtkTelephony.Carriers.AUTH_TYPE)), apnTypesBitmask, getProtocolIntFromString(cursor.getString(cursor.getColumnIndexOrThrow(MtkTelephony.Carriers.PROTOCOL))), getProtocolIntFromString(cursor.getString(cursor.getColumnIndexOrThrow(MtkTelephony.Carriers.ROAMING_PROTOCOL))), cursor.getInt(cursor.getColumnIndexOrThrow(MtkTelephony.Carriers.CARRIER_ENABLED)) == 1, networkTypeBitmask, cursor.getInt(cursor.getColumnIndexOrThrow(MtkTelephony.Carriers.PROFILE_ID)), cursor.getInt(cursor.getColumnIndexOrThrow(MtkTelephony.Carriers.MODEM_PERSIST)) == 1, cursor.getInt(cursor.getColumnIndexOrThrow(MtkTelephony.Carriers.MAX_CONNECTIONS)), cursor.getInt(cursor.getColumnIndexOrThrow(MtkTelephony.Carriers.WAIT_TIME_RETRY)), cursor.getInt(cursor.getColumnIndexOrThrow(MtkTelephony.Carriers.TIME_LIMIT_FOR_MAX_CONNECTIONS)), mtu, getMvnoTypeIntFromString(cursor.getString(cursor.getColumnIndexOrThrow(MtkTelephony.Carriers.MVNO_TYPE))), cursor.getString(cursor.getColumnIndexOrThrow(MtkTelephony.Carriers.MVNO_MATCH_DATA)), cursor.getInt(cursor.getColumnIndexOrThrow(MtkTelephony.Carriers.APN_SET_ID)), cursor.getInt(cursor.getColumnIndexOrThrow(MtkTelephony.Carriers.CARRIER_ID)), cursor.getInt(cursor.getColumnIndexOrThrow(MtkTelephony.Carriers.SKIP_464XLAT)), inactiveTimer2);
    }

    private static ApnSetting fromStringEx(String[] a, int authType, int apnTypeBitmask, int protocol, int roamingProtocol, boolean carrierEnabled, int networkTypeBitmask, int profileId, boolean modemCognitive, int maxConns, int waitTime, int maxConnsTime, int mtu, int mvnoType, String mvnoMatchData, int apnSetId, int carrierId, int skip464xlat) {
        int inactiveTimer2 = 0;
        if (a.length > 29) {
            try {
                inactiveTimer2 = Integer.parseInt(a[29]);
            } catch (NumberFormatException e) {
                Rlog.e(LOG_TAG, "NumberFormatException, inactive timer = " + a[29]);
            } catch (Exception e2) {
                Rlog.d(LOG_TAG, e2.toString());
            }
        }
        return new MtkApnSetting(-1, a[10] + a[11], a[0], a[1], a[2], portFromString(a[3]), UriFromString(a[7]), a[8], portFromString(a[9]), a[4], a[5], authType, apnTypeBitmask, protocol, roamingProtocol, carrierEnabled, networkTypeBitmask, profileId, modemCognitive, maxConns, waitTime, maxConnsTime, mtu, mvnoType, mvnoMatchData, apnSetId, carrierId, skip464xlat, inactiveTimer2);
    }

    public String toString() {
        return super.toString() + ", " + this.inactiveTimer;
    }

    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeInt(this.inactiveTimer);
    }

    /* access modifiers changed from: protected */
    public boolean hasApnType(int type) {
        if (this.mApnTypeBitmask == 63999) {
            if (type == 64 || type == 512) {
                return false;
            }
        } else if (type == 8) {
            if (this.mApnTypeBitmask == 8) {
                return DBG;
            }
            return false;
        }
        return super.hasApnType(type);
    }

    private static Bundle getApnTypeStringEx(int apnType) {
        if (apnType != 63999) {
            return null;
        }
        Bundle b = new Bundle();
        b.putString("result", "*");
        return b;
    }

    private static Bundle getApnTypesBitmaskFromStringEx(String types) {
        if (!TextUtils.isEmpty(types) && !TextUtils.equals(types, "*")) {
            return null;
        }
        Bundle b = new Bundle();
        b.putInt("result", MTK_TYPE_ALL);
        return b;
    }

    public String toStringIgnoreName(boolean ignoreName) {
        StringBuilder sb = new StringBuilder();
        sb.append("[ApnSettingV6] ");
        if (!ignoreName) {
            sb.append(", ");
            sb.append(getEntryName());
        }
        sb.append(getId());
        sb.append(", ");
        sb.append(getOperatorNumeric());
        sb.append(", ");
        sb.append(getApnName());
        sb.append(", ");
        sb.append(getProxyAddressAsString());
        sb.append(", ");
        sb.append(UriToString(getMmsc()));
        sb.append(", ");
        sb.append(getMmsProxyAddressAsString());
        sb.append(", ");
        sb.append(portToString(getMmsProxyPort()));
        sb.append(", ");
        sb.append(portToString(getProxyPort()));
        sb.append(", ");
        sb.append(getAuthType());
        sb.append(", ");
        sb.append(TextUtils.join(" | ", getApnTypesStringFromBitmask(getApnTypeBitmask()).split(",")));
        sb.append(", ");
        sb.append((String) PROTOCOL_INT_MAP.get(Integer.valueOf(getProtocol())));
        sb.append(", ");
        sb.append((String) PROTOCOL_INT_MAP.get(Integer.valueOf(getRoamingProtocol())));
        sb.append(", ");
        sb.append(isEnabled());
        sb.append(", ");
        sb.append(getProfileId());
        sb.append(", ");
        sb.append(isPersistent());
        sb.append(", ");
        sb.append(getMaxConns());
        sb.append(", ");
        sb.append(getWaitTime());
        sb.append(", ");
        sb.append(getMaxConnsTime());
        sb.append(", ");
        sb.append(getMtu());
        sb.append(", ");
        sb.append((String) MVNO_TYPE_INT_MAP.get(Integer.valueOf(getMvnoType())));
        sb.append(", ");
        sb.append(getMvnoMatchData());
        sb.append(", ");
        sb.append(getPermanentFailed());
        sb.append(", ");
        sb.append(getNetworkTypeBitmask());
        sb.append(", ");
        sb.append(getApnSetId());
        sb.append(", ");
        sb.append(getCarrierId());
        Rlog.d(LOG_TAG, "toStringIgnoreName: sb = " + sb.toString() + ", ignoreName: " + ignoreName);
        return sb.toString();
    }

    public static String toStringIgnoreNameForList(List<ApnSetting> apnSettings, boolean ignoreName) {
        if (apnSettings == null || apnSettings.size() == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        Iterator<ApnSetting> it = apnSettings.iterator();
        while (it.hasNext()) {
            sb.append(((MtkApnSetting) it.next()).toStringIgnoreName(ignoreName));
        }
        return sb.toString();
    }

    private static String emptyToNull(String stringValue) {
        if (TextUtils.equals(stringValue, "")) {
            return null;
        }
        return stringValue;
    }
}
