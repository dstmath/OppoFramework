package com.mediatek.internal.telephony.dataconnection;

import android.content.Context;
import android.os.AsyncResult;
import android.os.SystemProperties;
import android.telephony.PcoData;
import android.telephony.Rlog;
import android.telephony.data.ApnSetting;
import android.text.TextUtils;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.dataconnection.ApnContext;
import com.android.internal.telephony.uicc.IccUtils;
import com.mediatek.internal.telephony.MtkGsmCdmaPhone;
import com.mediatek.internal.telephony.uicc.MtkIccUtilsEx;
import java.util.ArrayList;

public class DataConnectionExt implements IDataConnectionExt {
    private static final String PROP_RIL_DATA_IMPI = "vendor.ril.data.impi";
    static final String TAG = "DataConnectionExt";
    String[] mImsMccMncList = {"405840", "405854", "405855", "405856", "405857", "405858", "405859", "405860", "405861", "405862", "405863", "405864", "405865", "405866", "405867", "405868", "405869", "405870", "405871", "405872", "405873", "405874"};

    public DataConnectionExt(Context context) {
    }

    @Override // com.mediatek.internal.telephony.dataconnection.IDataConnectionExt
    public boolean isDomesticRoamingEnabled() {
        return false;
    }

    @Override // com.mediatek.internal.telephony.dataconnection.IDataConnectionExt
    public boolean isDataAllowedAsOff(String apnType) {
        if (TextUtils.equals(apnType, "default") || TextUtils.equals(apnType, "mms") || TextUtils.equals(apnType, "dun")) {
            return false;
        }
        return true;
    }

    @Override // com.mediatek.internal.telephony.dataconnection.IDataConnectionExt
    public boolean isFdnEnableSupport() {
        return false;
    }

    @Override // com.mediatek.internal.telephony.dataconnection.IDataConnectionExt
    public void onDcActivated(String[] apnTypes, String ifc) {
    }

    @Override // com.mediatek.internal.telephony.dataconnection.IDataConnectionExt
    public void onDcDeactivated(String[] apnTypes, String ifc) {
    }

    @Override // com.mediatek.internal.telephony.dataconnection.IDataConnectionExt
    public long getDisconnectDoneRetryTimer(String reason, long defaultTimer) {
        if (MtkGsmCdmaPhone.REASON_RA_FAILED.equals(reason)) {
            return 90000;
        }
        return defaultTimer;
    }

    public void log(String text) {
        Rlog.d(TAG, text);
    }

    @Override // com.mediatek.internal.telephony.dataconnection.IDataConnectionExt
    public boolean isOnlySingleDcAllowed() {
        return false;
    }

    @Override // com.mediatek.internal.telephony.dataconnection.IDataConnectionExt
    public boolean ignoreDefaultDataUnselected(String apnType) {
        if (!TextUtils.equals(apnType, "ims") && !TextUtils.equals(apnType, "emergency") && !TextUtils.equals(apnType, "xcap") && !TextUtils.equals(apnType, "mms")) {
            return false;
        }
        log("ignoreDefaultDataUnselected, apnType = " + apnType);
        return true;
    }

    @Override // com.mediatek.internal.telephony.dataconnection.IDataConnectionExt
    public boolean ignoreDataRoaming(String apnType) {
        if (!TextUtils.equals(apnType, "ims")) {
            return false;
        }
        log("ignoreDataRoaming, apnType = " + apnType);
        return true;
    }

    @Override // com.mediatek.internal.telephony.dataconnection.IDataConnectionExt
    public void startDataRoamingStrategy(Phone phone) {
    }

    @Override // com.mediatek.internal.telephony.dataconnection.IDataConnectionExt
    public void stopDataRoamingStrategy() {
    }

    @Override // com.mediatek.internal.telephony.dataconnection.IDataConnectionExt
    public String getOperatorNumericFromImpi(String defaultValue, int phoneId) {
        log("getOperatorNumbericFromImpi got default mccmnc: " + defaultValue);
        String[] strArr = this.mImsMccMncList;
        if (strArr != null) {
            if (strArr.length != 0) {
                String strHexImpi = SystemProperties.get(PROP_RIL_DATA_IMPI + phoneId, "");
                if (strHexImpi.length() == 0) {
                    log("Returning default mccmnc: " + defaultValue);
                    return defaultValue;
                }
                String impi = MtkIccUtilsEx.parseImpiToString(IccUtils.hexStringToBytes(strHexImpi));
                log("impi=" + impi);
                if (impi != null) {
                    if (!impi.equals("")) {
                        int mccPosition = impi.indexOf("mcc");
                        int mncPosition = impi.indexOf("mnc");
                        if (mccPosition != -1) {
                            if (mncPosition != -1) {
                                String masterMccMnc = impi.substring(mccPosition + "mcc".length(), "mcc".length() + mccPosition + 3) + impi.substring("mnc".length() + mncPosition, "mnc".length() + mncPosition + 3);
                                log("master MccMnc: " + masterMccMnc);
                                if (masterMccMnc == null || masterMccMnc.equals("")) {
                                    log("Returning default mccmnc: " + defaultValue);
                                    return defaultValue;
                                }
                                String[] strArr2 = this.mImsMccMncList;
                                for (String mccMnc : strArr2) {
                                    if (masterMccMnc.equals(mccMnc)) {
                                        log("mccMnc matched:" + mccMnc);
                                        log("Returning mccmnc from IMPI: " + masterMccMnc);
                                        return masterMccMnc;
                                    }
                                }
                                log("Returning default mccmnc: " + defaultValue);
                                return defaultValue;
                            }
                        }
                        log("Returning default mccmnc: " + defaultValue);
                        return defaultValue;
                    }
                }
                log("Returning default mccmnc: " + defaultValue);
                return defaultValue;
            }
        }
        log("Returning default mccmnc: " + defaultValue);
        return defaultValue;
    }

    @Override // com.mediatek.internal.telephony.dataconnection.IDataConnectionExt
    public boolean isMeteredApnTypeByLoad() {
        return false;
    }

    @Override // com.mediatek.internal.telephony.dataconnection.IDataConnectionExt
    public boolean isMeteredApnType(String type, boolean isRoaming) {
        log("isMeteredApnType, apnType = " + type + ", isRoaming = " + isRoaming);
        if (TextUtils.equals(type, "default") || TextUtils.equals(type, "supl") || TextUtils.equals(type, "dun") || TextUtils.equals(type, "mms")) {
            return true;
        }
        return false;
    }

    @Override // com.mediatek.internal.telephony.dataconnection.IDataConnectionExt
    public boolean isPermanentCause(int cause) {
        return false;
    }

    @Override // com.mediatek.internal.telephony.dataconnection.IDataConnectionExt
    public void handlePcoDataAfterAttached(AsyncResult ar, Phone phone, ArrayList<ApnSetting> arrayList) {
    }

    @Override // com.mediatek.internal.telephony.dataconnection.IDataConnectionExt
    public boolean getIsPcoAllowedDefault() {
        return true;
    }

    @Override // com.mediatek.internal.telephony.dataconnection.IDataConnectionExt
    public void setIsPcoAllowedDefault(boolean allowed) {
    }

    @Override // com.mediatek.internal.telephony.dataconnection.IDataConnectionExt
    public int getPcoActionByApnType(ApnContext apnContext, PcoData pcoData) {
        return 0;
    }
}
